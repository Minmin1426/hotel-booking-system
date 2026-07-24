package com.hotelbooking.wallet;

import com.hotelbooking.wallet.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WalletService {

    // ── User-facing ──────────────────────────────────────────────────────────────

    Wallet createPersonalWallet(Long userId);

    List<WalletResponse> getMyWallets(Long userId);

    WalletBalanceResponse getWalletBalance(Long userId, Long walletId);

    Page<WalletTransactionResponse> getTransactionHistory(
            Long userId, Long walletId, String type, Pageable pageable);

    DepositResponse deposit(Long userId, Long walletId, DepositRequest request);

    void confirmDeposit(Long walletId, Long transactionId);

    PayBookingResponse payBooking(Long userId, PayBookingRequest request);

    void refundToWallet(Long walletId, Long bookingId, java.math.BigDecimal amount);

    // ── Admin ──────────────────────────────────────────────────────────────────

    Page<WalletResponse> listAllWallets(Long userId, String status, Pageable pageable);

    WalletResponse changeWalletStatus(Long adminId, Long walletId, WalletStatusRequest request);

    WalletAdjustmentResponse manualAdjustment(Long adminId, Long walletId, WalletAdjustmentRequest request);
}
