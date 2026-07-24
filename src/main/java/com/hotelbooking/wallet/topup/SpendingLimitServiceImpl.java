package com.hotelbooking.wallet.topup;

import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import com.hotelbooking.wallet.Group;
import com.hotelbooking.wallet.GroupMembership;
import com.hotelbooking.wallet.GroupMembershipRepository;
import com.hotelbooking.wallet.GroupRepository;
import com.hotelbooking.wallet.topup.dto.SpendingLimitRequest;
import com.hotelbooking.wallet.topup.dto.SpendingLimitResponse;
import com.hotelbooking.wallet.topup.dto.SpendingStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpendingLimitServiceImpl implements SpendingLimitService {

    private final SpendingLimitRepository spendingLimitRepository;
    private final SpendingLimitHistoryRepository historyRepository;
    private final SpendingTrackingRepository trackingRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;

    @Override
    @Transactional
    public SpendingLimitResponse setMemberLimit(Long adminOrOwnerId, Long groupId, Long memberUserId,
                                                 SpendingLimitRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId.toString()));
        User member = userRepository.findById(memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", memberUserId.toString()));
        User setter = userRepository.findById(adminOrOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminOrOwnerId.toString()));

        // Validate setter is group owner
        boolean isGroupOwner = group.getOwnerUser().getUserId().equals(adminOrOwnerId);
        boolean isAdmin = "ADMIN".equals(setter.getRole());
        if (!isGroupOwner && !isAdmin) {
            throw new BusinessException("ACCESS_DENIED: Only the group owner or admin can set spending limits");
        }

        // Validate member belongs to group
        membershipRepository.findByGroupIdAndMemberUserId(groupId, memberUserId)
                .orElseThrow(() -> new BusinessException("ACCESS_DENIED: User is not a member of this group"));

        // Validate date range
        if (request.getEffectiveUntil() != null &&
                request.getEffectiveUntil().isBefore(request.getEffectiveFrom())) {
            throw new BusinessException("INVALID_DATE_RANGE: Effective until date must be after effective from date");
        }

        // Effective date must not be too far in the future (max 1 year)
        if (request.getEffectiveFrom().isAfter(LocalDate.now().plusYears(1))) {
            throw new BusinessException("INVALID_DATE_RANGE: Effective from date cannot be more than 1 year in the future");
        }

        // Upsert spending limit
        SpendingLimit limit = spendingLimitRepository
                .findByGroupGroupIdAndMemberUserUserId(groupId, memberUserId)
                .orElse(SpendingLimit.builder()
                        .group(group)
                        .memberUser(member)
                        .effectiveFrom(LocalDate.now())
                        .createdBy(setter)
                        .build());

        // Record history if updating existing limit
        if (limit.getLimitId() != null && request.getPerTransactionLimit() != null) {
            SpendingLimitHistory history = SpendingLimitHistory.builder()
                    .spendingLimit(limit)
                    .previousLimit(limit.getPerTransactionLimit())
                    .newLimit(request.getPerTransactionLimit())
                    .changedBy(setter)
                    .reason(request.getEffectiveUntil() != null ?
                            "Limit updated with effective date range" : "Limit updated")
                    .build();
            historyRepository.save(history);
        }

        limit.setPerTransactionLimit(request.getPerTransactionLimit());
        limit.setDailyLimit(request.getDailyLimit());
        limit.setMonthlyLimit(request.getMonthlyLimit());
        limit.setEffectiveFrom(request.getEffectiveFrom());
        limit.setEffectiveUntil(request.getEffectiveUntil());
        limit.setUpdatedAt(LocalDateTime.now());
        limit = spendingLimitRepository.save(limit);

        return toResponse(limit);
    }

    @Override
    public void checkLimit(Long userId, Long groupId, BigDecimal amount) {
        SpendingLimit limit = spendingLimitRepository
                .findEffectiveLimit(groupId, userId, LocalDate.now())
                .orElse(null); // null = unlimited

        if (limit == null) {
            return; // no limit set, allow
        }

        if (limit.hasUnlimitedSpending()) {
            return;
        }

        // Per-transaction check
        if (limit.getPerTransactionLimit() != null &&
                amount.compareTo(limit.getPerTransactionLimit()) > 0) {
            log.warn("SpendingLimit exceeded: PER_TRANSACTION {} < {} for user {}",
                    limit.getPerTransactionLimit(), amount, userId);
            throw new BusinessException("SPENDING_LIMIT_EXCEEDED: Per-transaction limit of " +
                    limit.getPerTransactionLimit() + " VND exceeded by " + amount + " VND");
        }

        // Daily check
        if (limit.getDailyLimit() != null) {
            BigDecimal dailySpent = trackingRepository.sumSpentForPeriod(
                    userId, groupId, PeriodType.DAILY, LocalDate.now());
            if (dailySpent.add(amount).compareTo(limit.getDailyLimit()) > 0) {
                log.warn("SpendingLimit exceeded: DAILY {} + {} > {} for user {}",
                        dailySpent, amount, limit.getDailyLimit(), userId);
                throw new BusinessException("SPENDING_LIMIT_EXCEEDED: Daily limit of " +
                        limit.getDailyLimit() + " VND exceeded. Remaining: " +
                        limit.getDailyLimit().subtract(dailySpent) + " VND");
            }
        }

        // Monthly check
        if (limit.getMonthlyLimit() != null) {
            LocalDate monthStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            BigDecimal monthlySpent = trackingRepository.sumSpentForPeriod(
                    userId, groupId, PeriodType.MONTHLY, monthStart);
            if (monthlySpent.add(amount).compareTo(limit.getMonthlyLimit()) > 0) {
                log.warn("SpendingLimit exceeded: MONTHLY {} + {} > {} for user {}",
                        monthlySpent, amount, limit.getMonthlyLimit(), userId);
                throw new BusinessException("SPENDING_LIMIT_EXCEEDED: Monthly limit of " +
                        limit.getMonthlyLimit() + " VND exceeded. Remaining: " +
                        limit.getMonthlyLimit().subtract(monthlySpent) + " VND");
            }
        }
    }

    @Override
    public SpendingStatusResponse getSpendingStatus(Long userId, Long groupId) {
        SpendingLimit limit = spendingLimitRepository
                .findEffectiveLimit(groupId, userId, LocalDate.now())
                .orElse(null);

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());

        BigDecimal dailySpent = trackingRepository.sumSpentForPeriod(userId, groupId, PeriodType.DAILY, today);
        BigDecimal monthlySpent = trackingRepository.sumSpentForPeriod(userId, groupId, PeriodType.MONTHLY, monthStart);

        BigDecimal perTxn = limit != null ? limit.getPerTransactionLimit() : null;
        BigDecimal dailyLimit = limit != null ? limit.getDailyLimit() : null;
        BigDecimal monthlyLimit = limit != null ? limit.getMonthlyLimit() : null;

        return SpendingStatusResponse.builder()
                .perTransactionLimit(perTxn)
                .dailyLimit(dailyLimit)
                .dailySpent(dailySpent)
                .remainingDaily(dailyLimit != null ? dailyLimit.subtract(dailySpent) : null)
                .monthlyLimit(monthlyLimit)
                .monthlySpent(monthlySpent)
                .remainingMonthly(monthlyLimit != null ? monthlyLimit.subtract(monthlySpent) : null)
                .build();
    }

    private SpendingLimitResponse toResponse(SpendingLimit limit) {
        return SpendingLimitResponse.builder()
                .limitId(limit.getLimitId())
                .groupId(limit.getGroup().getGroupId())
                .memberUserId(limit.getMemberUser().getUserId())
                .perTransactionLimit(limit.getPerTransactionLimit())
                .dailyLimit(limit.getDailyLimit())
                .monthlyLimit(limit.getMonthlyLimit())
                .effectiveFrom(limit.getEffectiveFrom())
                .effectiveUntil(limit.getEffectiveUntil())
                .createdByUserId(limit.getCreatedBy().getUserId())
                .createdAt(limit.getCreatedAt())
                .build();
    }
}
