package com.hotelbooking.wallet;

import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import com.hotelbooking.wallet.dto.*;
import com.hotelbooking.wallet.exception.*;
import com.hotelbooking.wallet.topup.SpendingLimitService;
import com.hotelbooking.wallet.topup.SpendingTracking;
import com.hotelbooking.wallet.topup.SpendingTrackingRepository;
import com.hotelbooking.wallet.topup.PeriodType;
import com.hotelbooking.admin.CustomerActivityRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final SpendingLimitService spendingLimitService;
    private final SpendingTrackingRepository trackingRepository;
    private final CustomerActivityRecorder activityRecorder;

    // ── T006: Create PERSONAL wallet on user registration ─────────────────────

    @Override
    @Transactional
    public Wallet createPersonalWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (walletRepository.findByOwnerUserUserIdAndWalletType(userId, WalletType.PERSONAL).isPresent()) {
            log.warn("Wallet: PERSONAL wallet already exists for userId={}", userId);
            return walletRepository.findByOwnerUserUserIdAndWalletType(userId, WalletType.PERSONAL).get();
        }

        Wallet wallet = Wallet.builder()
                .ownerUser(user)
                .walletType(WalletType.PERSONAL)
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .status(WalletStatus.ACTIVE)
                .build();

        log.info("Wallet: Created PERSONAL wallet for userId={}", userId);
        return walletRepository.save(wallet);
    }

    // ── T007: Get my wallets ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<WalletResponse> getMyWallets(Long userId) {
        List<Wallet> wallets = walletRepository.findByOwnerUserUserId(userId);
        return wallets.stream().map(this::toWalletResponse).toList();
    }

    // ── T008: Get wallet balance ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public WalletBalanceResponse getWalletBalance(Long userId, Long walletId) {
        Wallet wallet = findWalletAndValidateOwnership(walletId, userId);
        return WalletBalanceResponse.builder()
                .walletId(wallet.getWalletId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .build();
    }

    // ── T009: Transaction history ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<WalletTransactionResponse> getTransactionHistory(
            Long userId, Long walletId, String type, Pageable pageable) {

        findWalletAndValidateOwnership(walletId, userId);

        Page<WalletTransaction> transactions;
        if (type != null && !type.isBlank()) {
            TransactionType txType = TransactionType.valueOf(type.toUpperCase());
            transactions = transactionRepository.findByWalletWalletIdAndTypeOrderByCreatedAtDesc(walletId, txType, pageable);
        } else {
            transactions = transactionRepository.findByWalletWalletIdOrderByCreatedAtDesc(walletId, pageable);
        }
        return transactions.map(this::toTransactionResponse);
    }

    // ── T010: Deposit (initiate) ──────────────────────────────────────────────

    @Override
    @Transactional
    public DepositResponse deposit(Long userId, Long walletId, DepositRequest request) {
        validateAmount(request.getAmount());

        Wallet wallet = findWalletAndValidateOwnership(walletId, userId);

        // Only ACTIVE wallets can accept deposits
        if (wallet.isFrozen()) {
            throw new WalletFrozenException("Wallet is frozen");
        }
        if (wallet.isClosed()) {
            throw new WalletClosedException("Wallet is closed");
        }

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal newBalance = balanceBefore.add(request.getAmount());

        // For STRIPE, we record as PENDING and return a payment URL.
        // The actual credit happens in confirmDeposit (Stripe webhook).
        // For BANK_TRANSFER, credit immediately.
        TransactionStatus txStatus = "BANK_TRANSFER".equalsIgnoreCase(request.getPaymentMethod())
                ? TransactionStatus.SUCCESS
                : TransactionStatus.PENDING;

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(txStatus == TransactionStatus.SUCCESS ? newBalance : balanceBefore)
                .status(txStatus)
                .paymentMethod(request.getPaymentMethod())
                .description("Wallet deposit via " + request.getPaymentMethod())
                .build();
        WalletTransaction saved = transactionRepository.save(transaction);

        if (txStatus == TransactionStatus.SUCCESS) {
            wallet.credit(request.getAmount());
            walletRepository.save(wallet);

            // 015-admin-customer-management: Record deposit activity
            activityRecorder.recordWalletDeposit(
                    userId, request.getAmount().toString(), null);
        }

        // TODO: Generate Stripe payment URL here (spec 013)
        String paymentUrl = null;

        log.info("Wallet: Deposit initiated for walletId={}, amount={}, status={}",
                walletId, request.getAmount(), txStatus);

        return DepositResponse.builder()
                .transactionId(saved.getTransactionId())
                .amount(request.getAmount())
                .newBalance(txStatus == TransactionStatus.SUCCESS ? newBalance : balanceBefore)
                .paymentUrl(paymentUrl)
                .paymentMethod(request.getPaymentMethod())
                .build();
    }

    // ── T011: Confirm deposit (Stripe webhook) ────────────────────────────────

    @Override
    @Transactional
    public void confirmDeposit(Long walletId, Long transactionId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        WalletTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId));

        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            log.warn("Wallet: Deposit already confirmed for transactionId={}", transactionId);
            return;
        }

        BigDecimal balanceBefore = wallet.getBalance();
        wallet.credit(transaction.getAmount());
        BigDecimal balanceAfter = wallet.getBalance();
        walletRepository.save(wallet);

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setBalanceAfter(balanceAfter);
        transactionRepository.save(transaction);

        log.info("Wallet: Deposit confirmed for walletId={}, amount={}, newBalance={}",
                walletId, transaction.getAmount(), balanceAfter);
    }

    // ── T012: Pay booking ────────────────────────────────────────────────────

    @Override
    @Transactional
    public PayBookingResponse payBooking(Long userId, PayBookingRequest request) {
        Wallet wallet = walletRepository.findByIdWithLock(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + request.getWalletId()));

        // Ownership check
        if (!wallet.getOwnerUser().getUserId().equals(userId)) {
            throw new WalletAccessDeniedException("You do not own this wallet");
        }

        // Wallet status check — FROZEN/CLOSED blocks PAYMENT
        if (wallet.isFrozen()) {
            throw new WalletFrozenException("Wallet is frozen");
        }
        if (wallet.isClosed()) {
            throw new WalletClosedException("Wallet is closed");
        }

        // Find booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", request.getBookingId()));

        // Validate booking belongs to user
        if (!booking.getUser().getUserId().equals(userId)) {
            throw new WalletAccessDeniedException("Booking does not belong to this user");
        }

        BigDecimal amount = booking.getTotalAmount();

        // AC-058: Spending limit enforcement (group wallets only)
        if (wallet.getGroup() != null) {
            spendingLimitService.checkLimit(userId, wallet.getGroup().getGroupId(), amount);
        }

        // Balance check
        if (!wallet.hasBalance(amount)) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        // Atomic deduction
        BigDecimal balanceBefore = wallet.getBalance();
        wallet.debit(amount);
        BigDecimal balanceAfter = wallet.getBalance();
        walletRepository.save(wallet);

        // Record transaction
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.PAYMENT)
                .amount(amount.negate()) // negative for payment
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .relatedBookingId(booking.getBookingId())
                .status(TransactionStatus.SUCCESS)
                .description("Payment for booking #" + booking.getBookingId())
                .build();
        WalletTransaction saved = transactionRepository.save(transaction);

        // Update booking status
        booking.setPaymentStatus("SUCCESS");
        // Keep booking status as-is (or set to CONFIRMED depending on flow)
        bookingRepository.save(booking);

        // AC-059: Update spending tracking for group wallets
        if (wallet.getGroup() != null) {
            updateSpendingTracking(wallet, userId, amount);
        }

        log.info("Wallet: Payment processed walletId={}, bookingId={}, amount={}, remaining={}",
                wallet.getWalletId(), booking.getBookingId(), amount, balanceAfter);

        return PayBookingResponse.builder()
                .transactionId(saved.getTransactionId())
                .walletId(wallet.getWalletId())
                .amountDeducted(amount)
                .remainingBalance(balanceAfter)
                .bookingStatus(booking.getStatus())
                .build();
    }

    // ── T013: Refund to wallet ────────────────────────────────────────────────

    @Override
    @Transactional
    public void refundToWallet(Long walletId, Long bookingId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        // FROZEN wallets still receive refunds
        if (wallet.isClosed()) {
            throw new WalletClosedException("Wallet is closed");
        }

        BigDecimal balanceBefore = wallet.getBalance();
        wallet.credit(amount);
        BigDecimal balanceAfter = wallet.getBalance();
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.REFUND)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .relatedBookingId(bookingId)
                .status(TransactionStatus.SUCCESS)
                .description("Refund for booking #" + bookingId)
                .build();
        transactionRepository.save(transaction);

        log.info("Wallet: Refund credited walletId={}, bookingId={}, amount={}, newBalance={}",
                walletId, bookingId, amount, balanceAfter);
    }

    // ── T015: Admin — list all wallets ───────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<WalletResponse> listAllWallets(Long userId, String status, Pageable pageable) {
        Page<Wallet> wallets;
        if (status != null && !status.isBlank()) {
            WalletStatus ws = WalletStatus.valueOf(status.toUpperCase());
            if (userId != null) {
                wallets = walletRepository.findByOwnerUserUserIdAndStatus(userId, ws, pageable);
            } else {
                wallets = walletRepository.findByStatus(ws, pageable);
            }
        } else {
            if (userId != null) {
                wallets = walletRepository.findByOwnerUserUserId(userId, pageable);
            } else {
                wallets = walletRepository.findAll(pageable);
            }
        }
        return wallets.map(this::toWalletResponse);
    }

    // ── T017: Admin — change wallet status ───────────────────────────────────

    @Override
    @Transactional
    public WalletResponse changeWalletStatus(Long adminId, Long walletId, WalletStatusRequest request) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        WalletStatus newStatus = WalletStatus.valueOf(request.getStatus().toUpperCase());
        wallet.setStatus(newStatus);
        Wallet saved = walletRepository.save(wallet);

        // Record adjustment audit
        WalletTransaction audit = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.ADJUSTMENT)
                .amount(BigDecimal.ZERO)
                .balanceBefore(wallet.getBalance())
                .balanceAfter(wallet.getBalance())
                .status(TransactionStatus.SUCCESS)
                .description("Admin " + adminId + " changed wallet status to " + newStatus
                        + (request.getReason() != null ? ": " + request.getReason() : ""))
                .build();
        transactionRepository.save(audit);

        log.info("Wallet: Admin {} changed wallet {} status to {}", adminId, walletId, newStatus);
        return toWalletResponse(saved);
    }

    // ── T016: Admin — manual adjustment ──────────────────────────────────────

    @Override
    @Transactional
    public WalletAdjustmentResponse manualAdjustment(Long adminId, Long walletId, WalletAdjustmentRequest request) {
        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal adjustedAmount;
        BigDecimal balanceAfter;

        if ("CREDIT".equalsIgnoreCase(request.getType())) {
            wallet.credit(request.getAmount());
            adjustedAmount = request.getAmount();
        } else if ("DEBIT".equalsIgnoreCase(request.getType())) {
            if (!wallet.hasBalance(request.getAmount())) {
                throw new InsufficientBalanceException("Cannot debit more than current balance");
            }
            wallet.debit(request.getAmount());
            adjustedAmount = request.getAmount().negate();
        } else {
            throw new InvalidAmountException("Type must be CREDIT or DEBIT");
        }

        balanceAfter = wallet.getBalance();
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.ADJUSTMENT)
                .amount(adjustedAmount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status(TransactionStatus.SUCCESS)
                .description("Admin adjustment by " + adminId + ": " + request.getReason())
                .build();
        WalletTransaction saved = transactionRepository.save(transaction);

        log.info("Wallet: Admin {} adjusted wallet {} by {} ({}), newBalance={}",
                adminId, walletId, adjustedAmount, request.getType(), balanceAfter);

        return WalletAdjustmentResponse.builder()
                .transactionId(saved.getTransactionId())
                .walletId(wallet.getWalletId())
                .amountAdjusted(adjustedAmount.abs())
                .newBalance(balanceAfter)
                .adjustmentType(request.getType().toUpperCase())
                .reason(request.getReason())
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Wallet findWalletAndValidateOwnership(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
        if (!wallet.getOwnerUser().getUserId().equals(userId)) {
            throw new WalletAccessDeniedException("You do not own this wallet");
        }
        return wallet;
    }

    private void updateSpendingTracking(Wallet wallet, Long userId, BigDecimal amount) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate monthStart = today.with(java.time.temporal.TemporalAdjusters.firstDayOfMonth());

        // Upsert daily tracking
        SpendingTracking daily = trackingRepository
                .findByUserUserIdAndGroupGroupIdAndPeriodTypeAndPeriodStart(
                        userId, wallet.getGroup().getGroupId(), PeriodType.DAILY, today)
                .orElse(SpendingTracking.builder()
                        .user(wallet.getOwnerUser())
                        .group(wallet.getGroup())
                        .periodType(PeriodType.DAILY)
                        .periodStart(today)
                        .periodEnd(today)
                        .totalSpent(java.math.BigDecimal.ZERO)
                        .build());
        daily.addSpending(amount);
        trackingRepository.save(daily);

        // Upsert monthly tracking
        SpendingTracking monthly = trackingRepository
                .findByUserUserIdAndGroupGroupIdAndPeriodTypeAndPeriodStart(
                        userId, wallet.getGroup().getGroupId(), PeriodType.MONTHLY, monthStart)
                .orElse(SpendingTracking.builder()
                        .user(wallet.getOwnerUser())
                        .group(wallet.getGroup())
                        .periodType(PeriodType.MONTHLY)
                        .periodStart(monthStart)
                        .periodEnd(today.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()))
                        .totalSpent(java.math.BigDecimal.ZERO)
                        .build());
        monthly.addSpending(amount);
        trackingRepository.save(monthly);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }

    private WalletResponse toWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
                .walletId(wallet.getWalletId())
                .ownerUserId(wallet.getOwnerUser().getUserId())
                .walletType(wallet.getWalletType())
                .groupId(wallet.getGroup() != null ? wallet.getGroup().getGroupId() : null)
                .groupName(wallet.getGroup() != null ? wallet.getGroup().getGroupName() : null)
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus())
                .build();
    }

    private WalletTransactionResponse toTransactionResponse(WalletTransaction tx) {
        return WalletTransactionResponse.builder()
                .transactionId(tx.getTransactionId())
                .walletId(tx.getWallet().getWalletId())
                .type(tx.getType())
                .amount(tx.getAmount())
                .balanceBefore(tx.getBalanceBefore())
                .balanceAfter(tx.getBalanceAfter())
                .relatedBookingId(tx.getRelatedBookingId())
                .status(tx.getStatus())
                .paymentMethod(tx.getPaymentMethod())
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
