package com.hotelbooking.voucher;

import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import com.hotelbooking.voucher.dto.*;
import com.hotelbooking.voucher.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherAdminServiceImpl implements VoucherAdminService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;

    // ── AC-029: Create voucher ──────────────────────────────────────────────

    @Override
    @Transactional
    public VoucherStoreResponse createVoucher(Long adminId, VoucherAdminRequest request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new com.hotelbooking.common.exception.ResourceNotFoundException("User", "id", adminId.toString()));

        if (request.getCode() != null && voucherRepository.findByCode(request.getCode()).isPresent()) {
            throw new BusinessException("Voucher code already exists: " + request.getCode());
        }

        String code = request.getCode() != null ? request.getCode()
                : "V" + System.currentTimeMillis();

        Voucher voucher = Voucher.builder()
                .code(code)
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscount(request.getMaxDiscount())
                .minBookingValue(request.getMinBookingValue())
                .maxUsage(request.getMaxUsage())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .forAccountType(request.getForAccountType() != null ? request.getForAccountType() : "ALL")
                .isActive(true)
                .currentUsage(0)
                .createdBy(admin)
                .build();

        voucher = voucherRepository.save(voucher);
        log.info("Admin {} created voucher {} ({})", adminId, code, request.getName());
        return toStoreResponse(voucher);
    }

    // ── AC-029: Update voucher ─────────────────────────────────────────────

    @Override
    @Transactional
    public VoucherStoreResponse updateVoucher(Long adminId, Long voucherId, VoucherAdminRequest request) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found: " + voucherId));

        if (request.getName() != null) voucher.setName(request.getName());
        if (request.getDescription() != null) voucher.setDescription(request.getDescription());
        if (request.getDiscountType() != null) voucher.setDiscountType(request.getDiscountType());
        if (request.getDiscountValue() != null) voucher.setDiscountValue(request.getDiscountValue());
        if (request.getMaxDiscount() != null) voucher.setMaxDiscount(request.getMaxDiscount());
        if (request.getMinBookingValue() != null) voucher.setMinBookingValue(request.getMinBookingValue());
        if (request.getMaxUsage() != null) {
            // Cannot decrease below current usage
            if (request.getMaxUsage() < voucher.getCurrentUsage()) {
                throw new BusinessException("Cannot set maxUsage below current usage count");
            }
            voucher.setMaxUsage(request.getMaxUsage());
        }
        if (request.getStartDate() != null) voucher.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) voucher.setEndDate(request.getEndDate());
        if (request.getForAccountType() != null) voucher.setForAccountType(request.getForAccountType());

        voucher = voucherRepository.save(voucher);
        log.info("Admin {} updated voucher {}", adminId, voucherId);
        return toStoreResponse(voucher);
    }

    // ── AC-036: Deactivate voucher ─────────────────────────────────────────

    @Override
    @Transactional
    public void deactivateVoucher(Long adminId, Long voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found: " + voucherId));

        voucher.setIsActive(false);
        voucherRepository.save(voucher);
        log.info("Admin {} deactivated voucher {}", adminId, voucherId);
    }

    // ── AC-029: List all vouchers ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherStoreResponse> listVouchers(String status, String accountType, Pageable pageable) {
        Page<Voucher> vouchers = voucherRepository.findAll(pageable);
        return vouchers.map(this::toStoreResponse);
    }

    // ── AC-035: Voucher statistics ─────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public VoucherStatsResponse getVoucherStats(Long voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new VoucherNotFoundException("Voucher not found: " + voucherId));

        long totalClaims = userVoucherRepository.countTotalClaims(voucherId);
        long totalRedemptions = userVoucherRepository.countTotalRedemptions(voucherId);
        int remaining = (voucher.getMaxUsage() != null && voucher.getCurrentUsage() != null)
                ? voucher.getMaxUsage() - voucher.getCurrentUsage()
                : Integer.MAX_VALUE;

        // Sum discount given — estimate based on redemptions (actual calculation would need booking data)
        BigDecimal totalDiscount = BigDecimal.ZERO;

        return VoucherStatsResponse.builder()
                .voucherId(voucherId)
                .code(voucher.getCode())
                .name(voucher.getName())
                .totalClaims(totalClaims)
                .totalRedemptions(totalRedemptions)
                .remainingUsage(remaining)
                .totalDiscountGiven(totalDiscount)
                .build();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

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
                .build();
    }
}
