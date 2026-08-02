package com.hotelbooking.voucher;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.voucher.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminVoucherController {

    private final VoucherAdminService voucherAdminService;
    private final JwtService jwtService;

    // POST /api/v1/admin/vouchers
    @PostMapping("/vouchers")
    public ResponseEntity<ApiResponse<VoucherStoreResponse>> createVoucher(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody VoucherAdminRequest request) {
        Long adminId = extractUserId(authorizationHeader);
        VoucherStoreResponse response = voucherAdminService.createVoucher(adminId, request);
        return ResponseEntity.ok(ApiResponse.success("Voucher created successfully", response));
    }

    // PUT /api/v1/admin/vouchers/{voucherId}
    @PutMapping("/vouchers/{voucherId}")
    public ResponseEntity<ApiResponse<VoucherStoreResponse>> updateVoucher(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long voucherId,
            @Valid @RequestBody VoucherAdminRequest request) {
        Long adminId = extractUserId(authorizationHeader);
        VoucherStoreResponse response = voucherAdminService.updateVoucher(adminId, voucherId, request);
        return ResponseEntity.ok(ApiResponse.success("Voucher updated successfully", response));
    }

    // DELETE /api/v1/admin/vouchers/{voucherId}
    @DeleteMapping("/vouchers/{voucherId}")
    public ResponseEntity<ApiResponse<String>> deactivateVoucher(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long voucherId) {
        Long adminId = extractUserId(authorizationHeader);
        voucherAdminService.deactivateVoucher(adminId, voucherId);
        return ResponseEntity.ok(ApiResponse.success("Voucher deactivated successfully", null));
    }

    // GET /api/v1/admin/vouchers
    @GetMapping("/vouchers")
    public ResponseEntity<ApiResponse<Page<VoucherStoreResponse>>> listVouchers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String accountType,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<VoucherStoreResponse> vouchers = voucherAdminService.listVouchers(status, accountType, pageable);
        return ResponseEntity.ok(ApiResponse.success("Vouchers retrieved", vouchers));
    }

    // GET /api/v1/admin/vouchers/{voucherId}/stats
    @GetMapping("/vouchers/{voucherId}/stats")
    public ResponseEntity<ApiResponse<VoucherStatsResponse>> getVoucherStats(
            @PathVariable Long voucherId) {
        VoucherStatsResponse stats = voucherAdminService.getVoucherStats(voucherId);
        return ResponseEntity.ok(ApiResponse.success("Voucher stats retrieved", stats));
    }

    private Long extractUserId(String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}
