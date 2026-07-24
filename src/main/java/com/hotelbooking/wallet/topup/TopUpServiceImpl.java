package com.hotelbooking.wallet.topup;

import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.common.utils.EmailService;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import com.hotelbooking.wallet.Wallet;
import com.hotelbooking.wallet.WalletRepository;
import com.hotelbooking.wallet.topup.dto.*;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;

import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopUpServiceImpl implements TopUpService {

    private static final BigDecimal DEFAULT_MAX_SINGLE_TOPUP = new BigDecimal("50000000");
    private static final BigDecimal DEFAULT_MAX_WALLET_BALANCE = new BigDecimal("1000000000");

    private final TopUpConfigRepository configRepository;
    private final TopUpHistoryRepository historyRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private BigDecimal maxSingleTopUp = DEFAULT_MAX_SINGLE_TOPUP;
    private BigDecimal maxWalletBalance = DEFAULT_MAX_WALLET_BALANCE;

    // ── AC-054: Configure Auto Top-Up ───────────────────────────────────────────

    @Override
    @Transactional
    public TopUpConfigResponse configureAutoTopUp(Long userId, Long walletId, TopUpConfigRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", walletId.toString()));

        if (!wallet.getOwnerUser().getUserId().equals(userId)) {
            throw new BusinessException("ACCESS_DENIED: You do not own this wallet");
        }

        // Upsert config
        TopUpConfig config = configRepository.findByWalletWalletId(walletId)
                .orElse(TopUpConfig.builder()
                        .user(user)
                        .wallet(wallet)
                        .build());

        config.setEnabled(request.getEnabled());
        config.setThresholdAmount(request.getThresholdAmount());
        config.setTopupAmount(request.getTopupAmount());
        config.setPaymentMethodId(request.getPaymentMethodId());
        config = configRepository.save(config);

        log.info("Auto top-up configured for wallet {}: enabled={}, threshold={}, amount={}",
                walletId, config.getEnabled(), config.getThresholdAmount(), config.getTopupAmount());

        return TopUpConfigResponse.builder()
                .configId(config.getConfigId())
                .enabled(config.getEnabled())
                .thresholdAmount(config.getThresholdAmount())
                .topupAmount(config.getTopupAmount())
                .maxDailyAutoTopup(config.getMaxDailyAutoTopup())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    // ── AC-053: Manual Top-Up via Stripe Checkout ────────────────────────────────

    @Override
    @Transactional
    public TopUpResponse initiateManualTopUp(Long userId, Long walletId, TopUpRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", walletId.toString()));

        if (!wallet.getOwnerUser().getUserId().equals(userId)) {
            throw new BusinessException("ACCESS_DENIED: You do not own this wallet");
        }

        BigDecimal amount = request.getAmount();

        // AC-056: Limit enforcement
        if (amount.compareTo(maxSingleTopUp) > 0) {
            throw new BusinessException("TOPUP_AMOUNT_EXCEEDS_LIMIT: Maximum single top-up is " + maxSingleTopUp + " VND");
        }
        if (wallet.getBalance().add(amount).compareTo(maxWalletBalance) > 0) {
            throw new BusinessException("WALLET_BALANCE_LIMIT_EXCEEDED: Maximum wallet balance is " +
                    maxWalletBalance + " VND. Current: " + wallet.getBalance() + ", Requested: " + amount);
        }

        // Create Stripe Checkout Session
        Stripe.apiKey = stripeApiKey;
        String sessionId;
        String checkoutUrl;

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(baseUrl + "/wallet/topup/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(baseUrl + "/wallet/topup/cancel")
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("vnd")
                                    .setUnitAmount(amount.longValue())
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Wallet Top-Up: " + wallet.getWalletType().name())
                                            .putMetadata("walletId", walletId.toString())
                                            .build())
                                    .build())
                            .build())
                    .putMetadata("walletId", walletId.toString())
                    .putMetadata("userId", userId.toString())
                    .putMetadata("type", "MANUAL_TOPUP")
                    .build();

            Session session = Session.create(params);
            sessionId = session.getId();
            checkoutUrl = session.getUrl();
        } catch (Exception e) {
            log.error("Stripe Checkout session creation failed for wallet {}", walletId, e);
            throw new BusinessException("Failed to create Stripe Checkout session: " + e.getMessage());
        }

        // Record pending top-up
        TopUpHistory history = TopUpHistory.builder()
                .wallet(wallet)
                .amount(amount)
                .paymentMethod("STRIPE")
                .stripeSessionId(sessionId)
                .status("PENDING")
                .isAutoTopup(false)
                .build();
        historyRepository.save(history);

        log.info("Manual top-up initiated for wallet {}: amount={}, sessionId={}",
                walletId, amount, sessionId);

        return TopUpResponse.builder()
                .checkoutUrl(checkoutUrl)
                .sessionId(sessionId)
                .build();
    }

    // ── AC-053: Stripe Webhook — credit wallet on success ──────────────────────

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Stripe webhook signature verification failed: {}", e.getMessage());
            throw new SecurityException("Invalid Stripe webhook signature");
        }

        if (!"checkout.session.completed".equals(event.getType()) &&
                !"checkout.session.async_payment_succeeded".equals(event.getType())) {
            return;
        }

        Session session;
        try {
            session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
        } catch (Exception e) {
            log.error("Failed to deserialize Stripe session", e);
            return;
        }

        String sessionId = session.getId();
        String metadataType = session.getMetadata() != null ?
                session.getMetadata().get("type") : null;

        if (!"MANUAL_TOPUP".equals(metadataType)) {
            return; // Not a wallet top-up
        }

        TopUpHistory history = historyRepository.findByStripeSessionId(sessionId)
                .orElse(null);
        if (history == null) {
            log.warn("Top-up history not found for session {}", sessionId);
            return;
        }

        if ("SUCCESS".equals(history.getStatus())) {
            return; // Idempotent
        }

        Long walletId = history.getWallet().getWalletId();
        BigDecimal amount = history.getAmount();

        // Re-verify wallet balance limit after payment
        Wallet wallet = walletRepository.findById(walletId).orElse(null);
        if (wallet == null) {
            log.error("Wallet {} not found for top-up session {}", walletId, sessionId);
            history.setStatus("FAILED");
            history.setFailureReason("Wallet not found");
            historyRepository.save(history);
            return;
        }

        if (wallet.getBalance().add(amount).compareTo(maxWalletBalance) > 0) {
            log.warn("Top-up would exceed max wallet balance for wallet {}: {} + {} > {}",
                    walletId, wallet.getBalance(), amount, maxWalletBalance);
            history.setStatus("FAILED");
            history.setFailureReason("Wallet balance would exceed maximum limit of " + maxWalletBalance);
            historyRepository.save(history);
            emailService.sendEmail(
                    wallet.getOwnerUser().getEmail(),
                    "Wallet Top-Up Failed",
                    "Your wallet top-up of " + amount + " VND could not be processed. " +
                    "The transaction would exceed the maximum wallet balance limit.");
            return;
        }

        // Credit wallet
        wallet.credit(amount);
        walletRepository.save(wallet);

        history.setStatus("SUCCESS");
        historyRepository.save(history);

        log.info("Wallet {} credited with {} VND from top-up session {}",
                walletId, amount, sessionId);

        emailService.sendEmail(
                wallet.getOwnerUser().getEmail(),
                "Wallet Top-Up Successful",
                "Your wallet has been credited with " + amount + " VND. " +
                "New balance: " + wallet.getBalance() + " VND.");
    }

    @Override
    public Page<TopUpHistoryResponse> getTopUpHistory(Long userId, Long walletId, Pageable pageable) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", walletId.toString()));

        if (!wallet.getOwnerUser().getUserId().equals(userId)) {
            throw new BusinessException("ACCESS_DENIED: You do not own this wallet");
        }

        return historyRepository.findByWalletWalletIdOrderByCreatedAtDesc(walletId, pageable)
                .map(h -> TopUpHistoryResponse.builder()
                        .historyId(h.getHistoryId())
                        .amount(h.getAmount())
                        .paymentMethod(h.getPaymentMethod())
                        .stripeSessionId(h.getStripeSessionId())
                        .status(h.getStatus())
                        .isAutoTopup(h.getIsAutoTopup())
                        .failureReason(h.getFailureReason())
                        .createdAt(h.getCreatedAt())
                        .build());
    }

    // ── AC-054: Auto Top-Up Trigger ─────────────────────────────────────────────

    @Override
    @Transactional
    public void triggerAutoTopUpIfNeeded(Long walletId) {
        TopUpConfig config = configRepository.findByWalletWalletId(walletId)
                .orElse(null);
        if (config == null || !config.getEnabled()) {
            return;
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElse(null);
        if (wallet == null) return;

        // Check if wallet is below threshold
        if (wallet.getBalance().compareTo(config.getThresholdAmount()) >= 0) {
            return; // Above threshold, no auto top-up
        }

        // Check daily auto top-up limit
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        long todayCount = historyRepository.countSuccessfulAutoTopUpsToday(walletId, dayStart, dayEnd);
        if (todayCount >= config.getMaxDailyAutoTopup()) {
            log.warn("Auto top-up skipped for wallet {}: daily limit of {} reached",
                    walletId, config.getMaxDailyAutoTopup());
            throw new BusinessException("AUTO_TOPUP_LIMIT_REACHED: Daily auto top-up limit of " +
                    config.getMaxDailyAutoTopup() + " reached");
        }

        BigDecimal topupAmount = config.getTopupAmount();

        // AC-056: Re-verify limits
        if (topupAmount.compareTo(maxSingleTopUp) > 0) {
            topupAmount = maxSingleTopUp;
        }
        if (wallet.getBalance().add(topupAmount).compareTo(maxWalletBalance) > 0) {
            log.warn("Auto top-up for wallet {} would exceed max balance, skipping", walletId);
            return;
        }

        // Process auto top-up via Stripe Payment Intent
        processAutoTopUp(wallet, config, topupAmount);
    }

    private void processAutoTopUp(Wallet wallet, TopUpConfig config, BigDecimal amount) {
        Stripe.apiKey = stripeApiKey;

        // Record pending auto top-up
        TopUpHistory history = TopUpHistory.builder()
                .wallet(wallet)
                .amount(amount)
                .paymentMethod("STRIPE")
                .status("PENDING")
                .isAutoTopup(true)
                .build();
        history = historyRepository.save(history);

        try {
            com.stripe.model.PaymentIntent intent = com.stripe.model.PaymentIntent.create(
                    com.stripe.param.PaymentIntentCreateParams.builder()
                            .setAmount(amount.longValue() * 100)
                            .setCurrency("vnd")
                            .setPaymentMethod(config.getPaymentMethodId())
                            .setConfirm(true)
                            .putMetadata("walletId", wallet.getWalletId().toString())
                            .putMetadata("topupHistoryId", history.getHistoryId().toString())
                            .build());

            if ("succeeded".equals(intent.getStatus()) || "processing".equals(intent.getStatus())) {
                wallet.credit(amount);
                walletRepository.save(wallet);

                history.setStatus("SUCCESS");
                history.setStripeSessionId(intent.getId());
                historyRepository.save(history);

                emailService.sendEmail(
                        wallet.getOwnerUser().getEmail(),
                        "Auto Top-Up Successful",
                        "Your wallet has been automatically topped up with " + amount + " VND. " +
                        "New balance: " + wallet.getBalance() + " VND.");

                log.info("Auto top-up successful for wallet {}: +{} VND", wallet.getWalletId(), amount);
            } else {
                history.setStatus("FAILED");
                history.setFailureReason("Stripe payment status: " + intent.getStatus());
                historyRepository.save(history);
            }
        } catch (Exception e) {
            log.error("Auto top-up failed for wallet {}", wallet.getWalletId(), e);
            history.setStatus("FAILED");
            history.setFailureReason(e.getMessage());
            historyRepository.save(history);

            emailService.sendEmail(
                    wallet.getOwnerUser().getEmail(),
                    "Auto Top-Up Failed",
                    "Your automatic top-up of " + amount + " VND failed. " +
                    "Please check your payment method and try a manual top-up.");
        }
    }

    // ── Admin: Update global limits ──────────────────────────────────────────────

    public void updateGlobalLimits(BigDecimal newMaxSingleTopUp, BigDecimal newMaxWalletBalance) {
        this.maxSingleTopUp = newMaxSingleTopUp;
        this.maxWalletBalance = newMaxWalletBalance;
        log.info("Global top-up limits updated: maxSingleTopUp={}, maxWalletBalance={}",
                maxSingleTopUp, maxWalletBalance);
    }
}
