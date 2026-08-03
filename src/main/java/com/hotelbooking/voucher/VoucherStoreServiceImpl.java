package com.hotelbooking.voucher;

import com.hotelbooking.booking.Booking;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import com.hotelbooking.voucher.dto.*;
import com.hotelbooking.voucher.exception.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherStoreServiceImpl implements VoucherStoreService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final com.hotelbooking.admin.CustomerActivityRecorder activityRecorder;
    private final com.hotelbooking.loyalty.LoyaltyService loyaltyService;

    // ── AC-030: Browse available vouchers ──────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherStoreResponse> getAvailableVouchers(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.hotelbooking.common.exception.ResourceNotFoundException("User", "id", userId.toString()));

        String accountType = user.getAccountType() != null ? user.getAccountType() : "CUSTOMER";
        Page<Voucher> vouchers = voucherRepository.findAvailableVouchers(
                accountType, LocalDateTime.now(), pageable);
        return vouchers.map(this::toStoreResponse);
    }

    // ── AC-031/032: Claim voucher ────────────────────────────────────────────

    @Override
    @Transactional
    public ClaimVoucherResponse claimVoucher(Long userId, String voucherCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.hotelbooking.common.exception.ResourceNotFoundException("User", "id", userId.toString()));

        Voucher voucher = voucherRepository.findByCode(voucherCode)
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found: " + voucherCode));

        // AC-030: Check availability
        if (!voucher.getIsActive()) {
            throw new VoucherNotAvailableException("Voucher is deactivated");
        }
        if (voucher.getStartDate() != null && LocalDateTime.now().isBefore(voucher.getStartDate())) {
            throw new VoucherNotAvailableException("Voucher is not yet active");
        }
        if (voucher.getEndDate() != null && LocalDateTime.now().isAfter(voucher.getEndDate())) {
            throw new VoucherNotAvailableException("Voucher has expired");
        }
        if (voucher.getCurrentUsage() != null && voucher.getMaxUsage() != null
                && voucher.getCurrentUsage() >= voucher.getMaxUsage()) {
            throw new VoucherExhaustedException("Voucher is fully redeemed");
        }

        // AC-031: Account type check
        if (!voucher.isForAccountType(user.getAccountType())) {
            throw new VoucherNotForAccountTypeException(
                    "VOUCHER_NOT_FOR_YOUR_ACCOUNT_TYPE: This voucher is only for " + voucher.getForAccountType() + " accounts");
        }

        // AC-031: Check not already claimed
        if (userVoucherRepository.findByUserUserIdAndVoucherVoucherId(userId, voucher.getVoucherId()).isPresent()) {
            throw new VoucherAlreadyClaimedException("VOUCHER_ALREADY_CLAIMED: You have already claimed this voucher");
        }

        // Create UserVoucher record
        UserVoucher userVoucher = UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .isUsed(false)
                .build();

        try {
            userVoucherRepository.save(userVoucher);
        } catch (DataIntegrityViolationException e) {
            // AC-031: Unique constraint prevents double-claim (idempotency)
            throw new VoucherAlreadyClaimedException("VOUCHER_ALREADY_CLAIMED: You have already claimed this voucher");
        }

        log.info("User {} claimed voucher {} ({})", userId, voucherCode, voucher.getName());

        // 015-admin-customer-management: Record activity
        activityRecorder.recordVoucherClaimed(userId, voucherCode);

        return ClaimVoucherResponse.builder()
                .voucherId(voucher.getVoucherId())
                .code(voucher.getCode())
                .name(voucher.getName())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .claimedAt(userVoucher.getClaimedAt())
                .message("Voucher claimed successfully")
                .build();
    }

    // ── AC-032: My voucher wallet ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<UserVoucherResponse> getMyVouchers(Long userId, Pageable pageable) {
        Page<UserVoucher> userVouchers = userVoucherRepository.findByUserUserIdOrderByClaimedAtDesc(userId, pageable);
        return userVouchers.map(this::toUserVoucherResponse);
    }

    // ── AC-034: Apply voucher usage on booking confirmation ────────────────────

    @Override
    @Transactional
    public void applyVoucherUsage(Long userId, Long voucherId, Long bookingId) {
        UserVoucher userVoucher = userVoucherRepository.findByUserUserIdAndVoucherVoucherId(userId, voucherId)
                .orElseThrow(() -> new VoucherNotClaimedException("VOUCHER_NOT_CLAIMED: Voucher is not in your wallet"));

        if (userVoucher.getIsUsed()) {
            throw new BusinessException("VOUCHER_ALREADY_USED: This voucher has already been used");
        }

        // Reload voucher with lock for usage increment
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found"));

        voucher.incrementUsage();
        voucherRepository.save(voucher);

        Booking booking = entityManager.getReference(Booking.class, bookingId);
        userVoucher.markUsed(booking);
        userVoucherRepository.save(userVoucher);

        // 015-admin-customer-management: Record redemption activity
        activityRecorder.recordVoucherRedeemed(userId, voucher.getCode(), bookingId);

        log.info("Voucher {} used by user {} for booking {}", voucherId, userId, bookingId);
    }

    // ── Voucher Shop: Browse available shop vouchers ─────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherStoreResponse> getShopVouchers(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.hotelbooking.common.exception.ResourceNotFoundException("User", "id", userId.toString()));

        String accountType = user.getAccountType() != null ? user.getAccountType() : "CUSTOMER";
        return voucherRepository.findShopVouchers(accountType, LocalDateTime.now(), pageable)
                .map(this::toStoreResponse);
    }

    // ── Voucher Shop: Spend points to claim a random voucher ─────────────────────

    @Override
    @Transactional
    public ClaimVoucherResponse spendPointsForRandomVoucher(Long userId, Integer pointsCost) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.hotelbooking.common.exception.ResourceNotFoundException("User", "id", userId.toString()));

        String accountType = user.getAccountType() != null ? user.getAccountType() : "CUSTOMER";

        // Find all available shop vouchers matching the points cost
        List<Voucher> candidates = voucherRepository.findShopVouchers(accountType, LocalDateTime.now(), Pageable.unpaged())
                .getContent()
                .stream()
                .filter(v -> v.getPointsCost() != null && v.getPointsCost().equals(pointsCost))
                .toList();

        if (candidates.isEmpty()) {
            throw new VoucherNotAvailableException("No vouchers available for " + pointsCost + " points");
        }

        // Pick a random one
        Voucher voucher = candidates.get(new java.util.Random().nextInt(candidates.size()));

        // Check not already claimed
        if (userVoucherRepository.findByUserUserIdAndVoucherVoucherId(userId, voucher.getVoucherId()).isPresent()) {
            throw new VoucherAlreadyClaimedException("You have already claimed this voucher");
        }

        // Deduct points
        loyaltyService.deductPoints(userId, pointsCost,
                "Spent " + pointsCost + " points to claim voucher: " + voucher.getCode(),
                null, voucher.getVoucherId());

        // Claim the voucher
        UserVoucher userVoucher = UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .isUsed(false)
                .build();

        try {
            userVoucherRepository.save(userVoucher);
        } catch (DataIntegrityViolationException e) {
            throw new VoucherAlreadyClaimedException("You have already claimed this voucher");
        }

        activityRecorder.recordVoucherClaimed(userId, voucher.getCode());

        log.info("User {} spent {} points to claim voucher {} ({})", userId, pointsCost, voucher.getCode(), voucher.getName());

        return ClaimVoucherResponse.builder()
                .voucherId(voucher.getVoucherId())
                .code(voucher.getCode())
                .name(voucher.getName())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .claimedAt(userVoucher.getClaimedAt())
                .message("You spent " + pointsCost + " points and won a voucher!")
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private VoucherStoreResponse toStoreResponse(Voucher v) {
        int remaining = (v.getMaxUsage() != null && v.getCurrentUsage() != null)
                ? v.getMaxUsage() - v.getCurrentUsage() : Integer.MAX_VALUE;
        return VoucherStoreResponse.builder()
                .voucherId(v.getVoucherId())
                .code(v.getCode())
                .name(v.getName())
                .description(v.getDescription())
                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .maxDiscount(v.getMaxDiscount())
                .minBookingValue(v.getMinBookingValue())
                .maxUsage(v.getMaxUsage())
                .currentUsage(v.getCurrentUsage())
                .remainingUsage(remaining)
                .startDate(v.getStartDate())
                .endDate(v.getEndDate())
                .forAccountType(v.getForAccountType())
                .pointsCost(v.getPointsCost())
                .build();
    }

    private UserVoucherResponse toUserVoucherResponse(UserVoucher uv) {
        Voucher v = uv.getVoucher();
        return UserVoucherResponse.builder()
                .id(uv.getId())
                .voucherId(v.getVoucherId())
                .voucherCode(v.getCode())
                .name(v.getName())
                .description(v.getDescription())
                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .maxDiscount(v.getMaxDiscount())
                .minBookingValue(v.getMinBookingValue())
                .endDate(v.getEndDate())
                .isUsed(uv.getIsUsed())
                .claimedAt(uv.getClaimedAt())
                .usedAt(uv.getUsedAt())
                .bookingId(uv.getBooking() != null ? uv.getBooking().getBookingId() : null)
                .build();
    }
}
