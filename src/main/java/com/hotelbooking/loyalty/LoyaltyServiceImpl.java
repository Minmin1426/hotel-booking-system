package com.hotelbooking.loyalty;

import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.loyalty.dto.*;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import com.hotelbooking.admin.CustomerActivityRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyServiceImpl implements LoyaltyService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final TierDefinitionRepository tierDefinitionRepository;
    private final LoyaltyPointLedgerRepository ledgerRepository;
    private final TierHistoryRepository historyRepository;
    private final CustomerActivityRecorder activityRecorder;

    // ── T008: Get My Tier ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public TierInfoResponse getMyTier(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String accountType = user.isCorporateMember() ? "CORPORATE_MEMBER" : "CUSTOMER";

        // Get current tier definition
        TierDefinition currentDef = tierDefinitionRepository.findByName(user.getCurrentTier())
                .orElse(null);

        BigDecimal annualSpend = getAnnualSpend(userId);
        BigDecimal multiplier = currentDef != null ? currentDef.getPointMultiplier() : BigDecimal.ONE;

        // Lifetime points = latest running balance
        Long lifetimePoints = ledgerRepository.findFirstByUserUserIdOrderByCreatedAtDesc(userId)
                .map(LoyaltyPointLedger::getRunningBalance)
                .orElse(0L);

        // Next tier info
        String nextTier = null;
        BigDecimal amountToNextTier = null;
        List<TierDefinition> allTiers = tierDefinitionRepository
                .findByAccountTypeOrderByMinAnnualSpendAsc(accountType);

        for (TierDefinition tier : allTiers) {
            if (tier.getMinAnnualSpend().compareTo(annualSpend) > 0) {
                nextTier = tier.getName();
                amountToNextTier = tier.getMinAnnualSpend().subtract(annualSpend);
                break;
            }
        }

        List<String> benefits = new ArrayList<>();
        if (currentDef != null) {
            if (Boolean.TRUE.equals(currentDef.getPrioritySupport())) benefits.add("PRIORITY_SUPPORT");
            if (Boolean.TRUE.equals(currentDef.getExclusiveVoucherAccess())) benefits.add("EXCLUSIVE_VOUCHER_ACCESS");
            benefits.add(currentDef.getPointMultiplier() + "x points on bookings");
        }

        return TierInfoResponse.builder()
                .userId(userId)
                .currentTier(user.getCurrentTier())
                .pointMultiplier(multiplier)
                .annualSpend(annualSpend)
                .lifetimePoints(lifetimePoints)
                .nextTier(nextTier)
                .amountToNextTier(amountToNextTier)
                .prioritySupport(currentDef != null ? currentDef.getPrioritySupport() : false)
                .exclusiveVoucherAccess(currentDef != null ? currentDef.getExclusiveVoucherAccess() : false)
                .tierEvaluatedAt(user.getTierEvaluatedAt())
                .benefits(benefits)
                .build();
    }

    // ── T009: Get My Tier History ────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<TierHistoryResponse> getMyTierHistory(Long userId, Pageable pageable) {
        return historyRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toHistoryResponse);
    }

    // ── T006: Award Points ─────────────────────────────────────────────────

    @Override
    @Transactional
    public void awardPoints(Long userId, Long bookingId, BigDecimal paymentAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        TierDefinition tierDef = tierDefinitionRepository.findByName(user.getCurrentTier()).orElse(null);
        BigDecimal multiplier = tierDef != null ? tierDef.getPointMultiplier() : BigDecimal.ONE;

        // points = floor(amount × multiplier)
        BigDecimal pointsDecimal = paymentAmount.multiply(multiplier);
        int pointsEarned = pointsDecimal.setScale(0, RoundingMode.FLOOR).intValue();

        // Running balance
        Long previousBalance = ledgerRepository.findFirstByUserUserIdOrderByCreatedAtDesc(userId)
                .map(LoyaltyPointLedger::getRunningBalance)
                .orElse(0L);
        long newBalance = previousBalance + pointsEarned;

        // Get booking reference
        Booking booking = bookingId != null
                ? bookingRepository.findById(bookingId).orElse(null)
                : null;

        LoyaltyPointLedger entry = LoyaltyPointLedger.builder()
                .user(user)
                .booking(booking)
                .pointsEarned(pointsEarned)
                .multiplierUsed(multiplier)
                .runningBalance(newBalance)
                .build();
        ledgerRepository.save(entry);

        log.info("Loyalty: Awarded {} points to user {} (multiplier={})", pointsEarned, userId, multiplier);
    }

    // ── T007: Evaluate Tier ────────────────────────────────────────────────

    @Override
    @Transactional
    public void evaluateTier(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String accountType = user.isCorporateMember() ? "CORPORATE_MEMBER" : "CUSTOMER";
        BigDecimal annualSpend = getAnnualSpend(userId);

        // Find matching tier
        List<TierDefinition> matching = tierDefinitionRepository
                .findMatchingTiersOrdered(accountType, annualSpend);

        if (matching.isEmpty()) {
            log.warn("Loyalty: No tier definition found for accountType={}, spend={}", accountType, annualSpend);
            return;
        }

        TierDefinition newTierDef = matching.get(0);
        String previousTier = user.getCurrentTier();

        if (newTierDef.getName().equalsIgnoreCase(previousTier)) {
            // No change needed
            log.debug("Loyalty: Tier unchanged at {} for user {}", previousTier, userId);
            return;
        }

        // Determine reason
        String reason = isPromotion(previousTier, newTierDef.getName(), accountType)
                ? "AUTO_PROMOTION" : "AUTO_DEMOTION";

        // Update user tier
        user.setCurrentTier(newTierDef.getName());
        user.setTierEvaluatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Record history
        recordTierHistory(user, previousTier, newTierDef.getName(), reason, null);

        // 015-admin-customer-management: Record activity
        activityRecorder.recordTierChanged(userId, previousTier, newTierDef.getName(), null);

        log.info("Loyalty: User {} tier changed {} → {} ({})",
                userId, previousTier, newTierDef.getName(), reason);
    }

    // ── T010: Admin Adjust Tier ───────────────────────────────────────────

    @Override
    @Transactional
    public TierAdjustmentResponse adjustUserTier(Long adminId, Long userId, String tier, String reason) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "admin", adminId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Validate tier name exists
        TierDefinition tierDef = tierDefinitionRepository.findByName(tier)
                .orElseThrow(() -> new BusinessException("INVALID_TIER: Unknown tier name: " + tier));

        String accountType = user.isCorporateMember() ? "CORPORATE_MEMBER" : "CUSTOMER";
        if (!tierDef.getAccountType().equals(accountType)) {
            throw new BusinessException("INVALID_TIER: Tier " + tier + " is not valid for " + accountType);
        }

        String previousTier = user.getCurrentTier();
        user.setCurrentTier(tier);
        user.setTierEvaluatedAt(LocalDateTime.now());
        userRepository.save(user);

        recordTierHistory(user, previousTier, tier, "ADMIN_ADJUSTMENT", admin);

        log.info("Loyalty: Admin {} adjusted user {} tier from {} → {}: {}",
                adminId, userId, previousTier, tier, reason);

        return TierAdjustmentResponse.builder()
                .userId(userId)
                .previousTier(previousTier)
                .newTier(tier)
                .reason(reason)
                .build();
    }

    // ── T011: Get Tier Definitions ─────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TierDefinitionResponse> getTierDefinitions(String accountType) {
        if (accountType != null && !accountType.isBlank()) {
            return tierDefinitionRepository.findByAccountTypeOrderByMinAnnualSpendAsc(accountType)
                    .stream().map(this::toDefinitionResponse).toList();
        }
        return tierDefinitionRepository.findAll().stream()
                .map(this::toDefinitionResponse).toList();
    }

    // ── Admin: Update Tier Definition ───────────────────────────────────────

    @Override
    @Transactional
    public TierDefinitionResponse updateTierDefinition(Long tierId, TierDefinitionResponse request) {
        TierDefinition tier = tierDefinitionRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("TierDefinition", tierId));

        if (request.getName() != null) tier.setName(request.getName());
        if (request.getMinAnnualSpend() != null) tier.setMinAnnualSpend(request.getMinAnnualSpend());
        if (request.getPointMultiplier() != null) tier.setPointMultiplier(request.getPointMultiplier());
        if (request.getMaxSpendingLimit() != null) tier.setMaxSpendingLimit(request.getMaxSpendingLimit());
        if (request.getPrioritySupport() != null) tier.setPrioritySupport(request.getPrioritySupport());
        if (request.getExclusiveVoucherAccess() != null) tier.setExclusiveVoucherAccess(request.getExclusiveVoucherAccess());

        TierDefinition saved = tierDefinitionRepository.save(tier);
        log.info("Loyalty: Updated tier definition {}", tierId);
        return toDefinitionResponse(saved);
    }

    // ── Admin: Points Ledger ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<PointsLedgerResponse> getUserPointsLedger(Long userId, Pageable pageable) {
        return ledgerRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toLedgerResponse);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    BigDecimal getAnnualSpend(Long userId) {
        LocalDateTime twelveMonthsAgo = LocalDateTime.now().minus(365, ChronoUnit.DAYS);
        BigDecimal spend = ledgerRepository.sumSuccessfulPaymentsSince(userId, twelveMonthsAgo);
        return spend != null ? spend : BigDecimal.ZERO;
    }

    private void recordTierHistory(User user, String previousTier, String newTier, String reason, User changedBy) {
        TierHistory history = TierHistory.builder()
                .user(user)
                .previousTier(previousTier)
                .newTier(newTier)
                .reason(reason)
                .changedBy(changedBy)
                .build();
        historyRepository.save(history);
    }

    private boolean isPromotion(String previousTier, String newTier, String accountType) {
        List<TierDefinition> tiers = tierDefinitionRepository
                .findByAccountTypeOrderByMinAnnualSpendAsc(accountType);
        int prevIdx = -1, newIdx = -1;
        for (int i = 0; i < tiers.size(); i++) {
            if (tiers.get(i).getName().equalsIgnoreCase(previousTier)) prevIdx = i;
            if (tiers.get(i).getName().equalsIgnoreCase(newTier)) newIdx = i;
        }
        return newIdx > prevIdx;
    }

    private TierDefinitionResponse toDefinitionResponse(TierDefinition td) {
        return TierDefinitionResponse.builder()
                .tierId(td.getTierId())
                .name(td.getName())
                .accountType(td.getAccountType())
                .minAnnualSpend(td.getMinAnnualSpend())
                .pointMultiplier(td.getPointMultiplier())
                .maxSpendingLimit(td.getMaxSpendingLimit())
                .prioritySupport(td.getPrioritySupport())
                .exclusiveVoucherAccess(td.getExclusiveVoucherAccess())
                .createdAt(td.getCreatedAt())
                .build();
    }

    private TierHistoryResponse toHistoryResponse(TierHistory h) {
        return TierHistoryResponse.builder()
                .historyId(h.getHistoryId())
                .previousTier(h.getPreviousTier())
                .newTier(h.getNewTier())
                .reason(h.getReason())
                .changedBy(h.getChangedBy() != null ? h.getChangedBy().getUserId() : null)
                .changedAt(h.getCreatedAt())
                .build();
    }

    private PointsLedgerResponse toLedgerResponse(LoyaltyPointLedger l) {
        return PointsLedgerResponse.builder()
                .ledgerId(l.getLedgerId())
                .bookingId(l.getBooking() != null ? l.getBooking().getBookingId() : null)
                .pointsEarned(l.getPointsEarned())
                .multiplierUsed(l.getMultiplierUsed())
                .runningBalance(l.getRunningBalance())
                .createdAt(l.getCreatedAt())
                .build();
    }
}
