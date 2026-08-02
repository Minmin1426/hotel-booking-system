package com.hotelbooking.voucher;

import com.hotelbooking.voucher.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VoucherAdminService {
    VoucherStoreResponse createVoucher(Long adminId, VoucherAdminRequest request);
    VoucherStoreResponse updateVoucher(Long adminId, Long voucherId, VoucherAdminRequest request);
    void deactivateVoucher(Long adminId, Long voucherId);
    Page<VoucherStoreResponse> listVouchers(String status, String accountType, Pageable pageable);
    VoucherStatsResponse getVoucherStats(Long voucherId);
}
