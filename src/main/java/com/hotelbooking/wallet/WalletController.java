package com.hotelbooking.wallet;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.wallet.dto.*;
import com.hotelbooking.wallet.topup.TopUpService;
import com.hotelbooking.wallet.topup.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/wallets")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class WalletController {

    private final WalletService walletService;
    private final TopUpService topUpService;
    private final JwtService jwtService;

    // GET /api/v1/users/me/wallets — get all wallets for current user
    @GetMapping
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getMyWallets(
            @RequestHeader("Authorization") String authorizationHeader) {
        Long userId = extractUserId(authorizationHeader);
        List<WalletResponse> wallets = walletService.getMyWallets(userId);
        return ResponseEntity.ok(ApiResponse.success("Wallets retrieved", wallets));
    }

    // GET /api/v1/users/me/wallets/{walletId}/balance
    @GetMapping("/{walletId}/balance")
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> getWalletBalance(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long walletId) {
        Long userId = extractUserId(authorizationHeader);
        WalletBalanceResponse balance = walletService.getWalletBalance(userId, walletId);
        return ResponseEntity.ok(ApiResponse.success("Balance retrieved", balance));
    }

    // GET /api/v1/users/me/wallets/{walletId}/transactions?type=&page=0&size=20
    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<ApiResponse<Page<WalletTransactionResponse>>> getTransactionHistory(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long walletId,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = extractUserId(authorizationHeader);
        Page<WalletTransactionResponse> history = walletService.getTransactionHistory(userId, walletId, type, pageable);
        return ResponseEntity.ok(ApiResponse.success("Transaction history retrieved", history));
    }

    // POST /api/v1/users/me/wallets/{walletId}/deposit
    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<ApiResponse<DepositResponse>> deposit(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long walletId,
            @Valid @RequestBody DepositRequest request) {
        Long userId = extractUserId(authorizationHeader);
        DepositResponse response = walletService.deposit(userId, walletId, request);
        return ResponseEntity.ok(ApiResponse.success("Deposit initiated", response));
    }

    // POST /api/v1/users/me/wallets/pay-booking
    @PostMapping("/pay-booking")
    public ResponseEntity<ApiResponse<PayBookingResponse>> payBooking(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody PayBookingRequest request) {
        Long userId = extractUserId(authorizationHeader);
        PayBookingResponse response = walletService.payBooking(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Booking paid successfully", response));
    }

    // PUT /api/v1/users/me/wallets/{walletId}/auto-topup
    @PutMapping("/{walletId}/auto-topup")
    public ResponseEntity<ApiResponse<com.hotelbooking.wallet.topup.dto.TopUpConfigResponse>> configureAutoTopUp(
            @PathVariable Long walletId,
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody TopUpConfigRequest request) {
        Long userId = extractUserId(authorizationHeader);
        com.hotelbooking.wallet.topup.dto.TopUpConfigResponse response =
                topUpService.configureAutoTopUp(userId, walletId, request);
        return ResponseEntity.ok(ApiResponse.success("Auto top-up configured", response));
    }

    // POST /api/v1/users/me/wallets/{walletId}/topup
    @PostMapping("/{walletId}/topup")
    public ResponseEntity<ApiResponse<TopUpResponse>> initiateManualTopUp(
            @PathVariable Long walletId,
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody TopUpRequest request) {
        Long userId = extractUserId(authorizationHeader);
        TopUpResponse response = topUpService.initiateManualTopUp(userId, walletId, request);
        return ResponseEntity.ok(ApiResponse.success("Top-up initiated", response));
    }

    // GET /api/v1/users/me/wallets/{walletId}/topup-history
    @GetMapping("/{walletId}/topup-history")
    public ResponseEntity<ApiResponse<Page<TopUpHistoryResponse>>> getTopUpHistory(
            @PathVariable Long walletId,
            @RequestHeader("Authorization") String authorizationHeader,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = extractUserId(authorizationHeader);
        Page<TopUpHistoryResponse> history = topUpService.getTopUpHistory(userId, walletId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Top-up history retrieved", history));
    }

    private Long extractUserId(String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}
