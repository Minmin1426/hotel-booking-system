package com.hotelbooking.voucher;

import com.hotelbooking.voucher.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VoucherStoreService {
    Page<VoucherStoreResponse> getAvailableVouchers(Long userId, Pageable pageable);
    ClaimVoucherResponse claimVoucher(Long userId, String voucherCode);
    Page<UserVoucherResponse> getMyVouchers(Long userId, Pageable pageable);
    void applyVoucherUsage(Long userId, Long voucherId, Long bookingId);
    Page<VoucherStoreResponse> getShopVouchers(Long userId, Pageable pageable);
    ClaimVoucherResponse spendPointsForRandomVoucher(Long userId, Integer pointsCost);
}
