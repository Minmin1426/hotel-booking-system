package com.hotelbooking.wallet;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.wallet.dto.*;
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
@RequestMapping("/api/v1/admin/wallets")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminWalletController {

    private final WalletService walletService;
    private final JwtService jwtService;

    // GET /api/v1/admin/wallets?userId=&status=&page=0&size=20
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WalletResponse>>> listAllWallets(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<WalletResponse> wallets = walletService.listAllWallets(userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Wallets retrieved", wallets));
    }

    // PATCH /api/v1/admin/wallets/{walletId}/status
    @PatchMapping("/{walletId}/status")
    public ResponseEntity<ApiResponse<WalletResponse>> changeWalletStatus(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long walletId,
            @Valid @RequestBody WalletStatusRequest request) {
        Long adminId = extractUserId(authorizationHeader);
        WalletResponse response = walletService.changeWalletStatus(adminId, walletId, request);
        return ResponseEntity.ok(ApiResponse.success("Wallet status updated", response));
    }

    // POST /api/v1/admin/wallets/{walletId}/adjust
    @PostMapping("/{walletId}/adjust")
    public ResponseEntity<ApiResponse<WalletAdjustmentResponse>> manualAdjustment(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long walletId,
            @Valid @RequestBody WalletAdjustmentRequest request) {
        Long adminId = extractUserId(authorizationHeader);
        WalletAdjustmentResponse response = walletService.manualAdjustment(adminId, walletId, request);
        return ResponseEntity.ok(ApiResponse.success("Balance adjusted successfully", response));
    }

    private Long extractUserId(String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}
