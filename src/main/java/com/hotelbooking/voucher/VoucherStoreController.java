package com.hotelbooking.voucher;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.voucher.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class VoucherStoreController {

    private final VoucherStoreService voucherStoreService;
    private final JwtService jwtService;

    // GET /api/v1/vouchers/available
    @GetMapping("/vouchers/available")
    public ResponseEntity<ApiResponse<Page<VoucherStoreResponse>>> getAvailableVouchers(
            @RequestHeader("Authorization") String authorizationHeader,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = extractUserId(authorizationHeader);
        Page<VoucherStoreResponse> vouchers = voucherStoreService.getAvailableVouchers(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Available vouchers retrieved", vouchers));
    }

    // POST /api/v1/users/me/vouchers/claim/{code}
    @PostMapping("/users/me/vouchers/claim/{code}")
    public ResponseEntity<ApiResponse<ClaimVoucherResponse>> claimVoucher(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String code) {
        Long userId = extractUserId(authorizationHeader);
        ClaimVoucherResponse response = voucherStoreService.claimVoucher(userId, code);
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    // GET /api/v1/users/me/vouchers
    @GetMapping("/users/me/vouchers")
    public ResponseEntity<ApiResponse<Page<UserVoucherResponse>>> getMyVouchers(
            @RequestHeader("Authorization") String authorizationHeader,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = extractUserId(authorizationHeader);
        Page<UserVoucherResponse> vouchers = voucherStoreService.getMyVouchers(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("My vouchers retrieved", vouchers));
    }

    private Long extractUserId(String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        return jwtService.extractUserId(token);
    }

    // GET /api/v1/vouchers/shop
    @GetMapping("/vouchers/shop")
    public ResponseEntity<ApiResponse<Page<VoucherStoreResponse>>> getShopVouchers(
            @RequestHeader("Authorization") String authorizationHeader,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = extractUserId(authorizationHeader);
        Page<VoucherStoreResponse> vouchers = voucherStoreService.getShopVouchers(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Shop vouchers retrieved", vouchers));
    }

    // POST /api/v1/users/me/vouchers/shop/claim
    @PostMapping("/users/me/vouchers/shop/claim")
    public ResponseEntity<ApiResponse<ClaimVoucherResponse>> spendPointsForVoucher(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, Object> body) {
        Long userId = extractUserId(authorizationHeader);
        Integer pointsCost = ((Number) body.get("pointsCost")).intValue();
        ClaimVoucherResponse response = voucherStoreService.spendPointsForRandomVoucher(userId, pointsCost);
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }
}
