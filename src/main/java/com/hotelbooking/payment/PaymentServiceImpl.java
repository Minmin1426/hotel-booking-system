package com.hotelbooking.payment;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.common.utils.EmailService;
import com.hotelbooking.loyalty.LoyaltyService;
import com.hotelbooking.payment.dto.PaymentRequestDTO;
import com.hotelbooking.payment.dto.PaymentResponseDTO;
import com.hotelbooking.voucher.Voucher;
import com.hotelbooking.voucher.VoucherRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;

import com.hotelbooking.voucher.VoucherStoreService;
import com.hotelbooking.payment.refund.RefundPolicyRepository;
import com.hotelbooking.payment.refund.RefundAuditLogRepository;
import com.hotelbooking.payment.refund.RefundPolicy;
import com.hotelbooking.payment.refund.RefundAuditLog;
import com.hotelbooking.wallet.WalletService;
import com.hotelbooking.wallet.WalletRepository;
import com.hotelbooking.wallet.Wallet;
import com.hotelbooking.wallet.WalletType;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.io.ByteArrayOutputStream;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAuditLogRepository auditLogRepository;
    private final EmailService emailService;
    private final VoucherRepository voucherRepository;
    private final VnpayService vnpayService;
    private final PayoutRepository payoutRepository;
    private final com.hotelbooking.mealticket.MealTicketService mealTicketService;
    private final LoyaltyService loyaltyService;
    private final VoucherStoreService voucherStoreService;
    private final RefundPolicyRepository refundPolicyRepository;
    private final RefundAuditLogRepository refundAuditLogRepository;
    private final WalletService walletService;
    private final WalletRepository walletRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    @Transactional
    public PaymentResponseDTO createPaymentRequest(PaymentRequestDTO requestDTO) {
        Booking booking = bookingRepository.findById(requestDTO.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", requestDTO.getBookingId().toString()));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new BusinessException("Booking is not in PENDING status");
        }

        BigDecimal originalAmount = booking.getFinalPrice() != null ? booking.getFinalPrice() : booking.getTotalAmount();
        boolean isDeposit = requestDTO.getIsDeposit() != null && requestDTO.getIsDeposit();
        BigDecimal depositRatio = isDeposit ? (requestDTO.getDepositRatio() != null ? requestDTO.getDepositRatio() : new BigDecimal("0.30")) : BigDecimal.ONE;
        
        if (isDeposit) {
            if (depositRatio.compareTo(new BigDecimal("0.30")) < 0 || depositRatio.compareTo(new BigDecimal("0.50")) > 0) {
                throw new BusinessException("Deposit ratio must be between 0.30 and 0.50");
            }
        }

        BigDecimal amountToPay = originalAmount.multiply(depositRatio);

        String companyName = requestDTO.getCompanyName();
        String taxId = requestDTO.getTaxId();
        String companyAddress = requestDTO.getCompanyAddress();
        String companyEmail = requestDTO.getCompanyEmail();
        LocalDateTime countdownEndTime = LocalDateTime.now().plusMinutes(10);

        // Retrieve existing PENDING payment to avoid duplicates
        List<Payment> existingPayments = paymentRepository.findByBookingBookingId(booking.getBookingId());
        Payment payment = null;
        for (Payment p : existingPayments) {
            if ("PENDING".equalsIgnoreCase(p.getStatus())) {
                payment = p;
                break;
            }
        }
        if (payment == null) {
            payment = new Payment();
            payment.setBooking(booking);
        }

        if ("CASH".equalsIgnoreCase(requestDTO.getPaymentMethod())) {
            String transactionId = "CASH-" + UUID.randomUUID().toString();

            payment.setPaymentMethod("CASH");
            payment.setAmount(amountToPay);
            payment.setStatus("PENDING");
            payment.setTransactionId(transactionId);
            payment.setGateway("CASH");
            payment.setIsDeposit(isDeposit);
            payment.setDepositRatio(depositRatio);
            payment.setCountdownEndTime(countdownEndTime);
            payment.setInvoiceCompanyName(companyName);
            payment.setInvoiceTaxId(taxId);
            payment.setInvoiceCompanyAddress(companyAddress);
            payment.setInvoiceCompanyEmail(companyEmail);
            
            paymentRepository.save(payment);
            
            booking.setPaymentStatus("PENDING");
            booking.setStatus("CONFIRMED");
            bookingRepository.save(booking);
            if (mealTicketService != null) {
                mealTicketService.autoIssueMealTicketsForBooking(booking);
            }

            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .transactionId(transactionId)
                    .action("CREATE_CASH_PAYMENT_SUCCESS")
                    .requestPayload("Booking ID: " + booking.getBookingId())
                    .build();
            auditLogRepository.save(auditLog);

            return PaymentResponseDTO.builder()
                    .transactionId(transactionId)
                    .clientSecret("CASH_PAYMENT")
                    .isDeposit(isDeposit)
                    .depositRatio(depositRatio)
                    .countdownEndTime(countdownEndTime)
                    .build();
        }

        if ("BANK_TRANSFER".equalsIgnoreCase(requestDTO.getPaymentMethod())) {
            String transactionId = "BT-" + UUID.randomUUID().toString();

            payment.setPaymentMethod("BANK_TRANSFER");
            payment.setAmount(amountToPay);
            payment.setStatus("PENDING");
            payment.setTransactionId(transactionId);
            payment.setGateway("MANUAL_BANK");
            payment.setIsDeposit(isDeposit);
            payment.setDepositRatio(depositRatio);
            payment.setCountdownEndTime(countdownEndTime);
            payment.setInvoiceCompanyName(companyName);
            payment.setInvoiceTaxId(taxId);
            payment.setInvoiceCompanyAddress(companyAddress);
            payment.setInvoiceCompanyEmail(companyEmail);
            
            paymentRepository.save(payment);
            
            booking.setPaymentStatus("PENDING");
            bookingRepository.save(booking);

            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .transactionId(transactionId)
                    .action("CREATE_BANK_TRANSFER_PAYMENT")
                    .requestPayload("Booking ID: " + booking.getBookingId())
                    .build();
            auditLogRepository.save(auditLog);

            String ref = "BK-" + booking.getBookingId().toString().toUpperCase();
            String acc = "123456789";

            return PaymentResponseDTO.builder()
                    .transactionId(transactionId)
                    .clientSecret("MANUAL_BANK_TRANSFER_TOKEN")
                    .bankName("Stripe International Bank")
                    .accountHolder("Stripe / LuxuryStay")
                    .accountNumber(acc)
                    .referenceCode(ref)
                    .branch("San Francisco Main")
                    .swiftCode("STRIPESF")
                    .isDeposit(isDeposit)
                    .depositRatio(depositRatio)
                    .countdownEndTime(countdownEndTime)
                    .build();
        }

        if ("VNPAY".equalsIgnoreCase(requestDTO.getPaymentMethod())) {
            String transactionId = "VNPAY-" + UUID.randomUUID().toString();

            payment.setPaymentMethod("VNPAY");
            payment.setAmount(amountToPay);
            payment.setStatus("PENDING");
            payment.setTransactionId(transactionId);
            payment.setGateway("VNPAY");
            payment.setIsDeposit(isDeposit);
            payment.setDepositRatio(depositRatio);
            payment.setCountdownEndTime(countdownEndTime);
            payment.setInvoiceCompanyName(companyName);
            payment.setInvoiceTaxId(taxId);
            payment.setInvoiceCompanyAddress(companyAddress);
            payment.setInvoiceCompanyEmail(companyEmail);
            
            paymentRepository.save(payment);

            booking.setPaymentStatus("PENDING");
            bookingRepository.save(booking);

            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .transactionId(transactionId)
                    .action("CREATE_VNPAY_PAYMENT")
                    .requestPayload("Booking ID: " + booking.getBookingId())
                    .build();
            auditLogRepository.save(auditLog);

            String payUrl = vnpayService.createPaymentUrl(transactionId, amountToPay, "Thanh toan booking " + booking.getBookingCode());

            return PaymentResponseDTO.builder()
                    .transactionId(transactionId)
                    .clientSecret(payUrl)
                    .paymentUrl(payUrl)
                    .isDeposit(isDeposit)
                    .depositRatio(depositRatio)
                    .countdownEndTime(countdownEndTime)
                    .build();
        }

        try {
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amountToPay.multiply(new BigDecimal(100)).longValue())
                    .setCurrency("usd")
                    .putMetadata("bookingId", booking.getBookingId().toString())
                    .addPaymentMethodType("card");
            
            PaymentIntentCreateParams params = paramsBuilder.build();

            PaymentIntent intent = PaymentIntent.create(params);
            String transactionId = intent.getId();

            payment.setPaymentMethod(requestDTO.getPaymentMethod());
            payment.setAmount(amountToPay);
            payment.setStatus("PENDING");
            payment.setTransactionId(transactionId);
            payment.setGateway("STRIPE");
            payment.setIsDeposit(isDeposit);
            payment.setDepositRatio(depositRatio);
            payment.setCountdownEndTime(countdownEndTime);
            payment.setInvoiceCompanyName(companyName);
            payment.setInvoiceTaxId(taxId);
            payment.setInvoiceCompanyAddress(companyAddress);
            payment.setInvoiceCompanyEmail(companyEmail);

            paymentRepository.save(payment);

            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .transactionId(transactionId)
                    .action("CREATE_PAYMENT_INTENT_SUCCESS")
                    .requestPayload("Booking ID: " + booking.getBookingId() + ", Intent ID: " + intent.getId())
                    .build();
            auditLogRepository.save(auditLog);

            return PaymentResponseDTO.builder()
                    .transactionId(transactionId)
                    .clientSecret(intent.getClientSecret())
                    .isDeposit(isDeposit)
                    .depositRatio(depositRatio)
                    .countdownEndTime(countdownEndTime)
                    .build();
        } catch (Exception e) {
            log.error("Stripe payment intent creation failed", e);
            throw new BusinessException("Payment intent creation failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void processStripeWebhook(String payload, String sigHeader) {
        Event event;
        try {
            // Verify HMAC SHA-256 signature and Timestamp (default 5 min tolerance)
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .action("WEBHOOK_VERIFICATION_FAILED")
                    .requestPayload("Invalid signature or timestamp: " + e.getMessage())
                    .build();
            auditLogRepository.save(auditLog);
            throw new SecurityException("Invalid webhook signature or timestamp");
        }

        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeObject instanceof PaymentIntent) {
            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
            String transactionId = paymentIntent.getId();

            if ("payment_intent.succeeded".equals(event.getType())) {
                handlePaymentSuccess(transactionId, payload);
            } else if ("payment_intent.payment_failed".equals(event.getType())) {
                handlePaymentFailed(transactionId, payload);
            }
        }
    }

    private void handlePaymentSuccess(String transactionId, String payload) {
        Payment payment = paymentRepository.findByTransactionIdForUpdate(transactionId)
                .orElseThrow(() -> new BusinessException("Payment not found for transaction: " + transactionId));

        if ("SUCCESS".equals(payment.getStatus())) {
            // Duplicate Webhook - Idempotency
            return;
        }
        
        if ("FAILED".equals(payment.getStatus())) {
            log.warn("Cannot transition payment from FAILED to SUCCESS for transaction: {}", transactionId);
            return;
        }

        payment.setStatus("SUCCESS");
        payment.setPaymentTime(LocalDateTime.now());
        paymentRepository.save(payment);

        Booking booking = payment.getBooking();
        if (payment.getIsDeposit() != null && payment.getIsDeposit()) {
            BigDecimal ratio = payment.getDepositRatio() != null ? payment.getDepositRatio() : new BigDecimal("0.30");
            if (ratio.compareTo(new BigDecimal("0.30")) == 0) {
                booking.setPaymentStatus("DEPOSIT_30_PAID");
            } else {
                booking.setPaymentStatus("PARTIALLY_PAID");
            }
        } else {
            booking.setPaymentStatus("SUCCESS");
        }
        booking.setStatus("CONFIRMED");
        
        if (booking.getVoucher() != null) {
            Voucher voucher = booking.getVoucher();
            if (voucher.getCurrentUsage() == null) {
                voucher.setCurrentUsage(1);
            } else {
                voucher.setCurrentUsage(voucher.getCurrentUsage() + 1);
            }
            voucherRepository.save(voucher);
            
            try {
                voucherStoreService.applyVoucherUsage(booking.getUser().getUserId(), voucher.getVoucherId(), booking.getBookingId());
            } catch (Exception e) {
                log.error("Failed to apply user voucher usage for booking ID: {}", booking.getBookingId(), e);
            }
        }
        
        if (booking.getCheckinQrCode() == null) {
            booking.setCheckinQrCode("CHK-" + booking.getBookingCode());
            booking.setCheckinQrSignature(java.util.UUID.randomUUID().toString().replace("-", ""));
        }
        bookingRepository.save(booking);
        if (mealTicketService != null) {
            try {
                mealTicketService.autoIssueMealTicketsForBooking(booking);
            } catch (Exception e) {
                log.error("Failed to auto-issue meal tickets for booking ID {}: {}", booking.getBookingId(), e.getMessage());
            }
        }

        // 011-loyalty-membership-tiers: award points and evaluate tier
        if (loyaltyService != null && payment.getAmount() != null) {
            try {
                loyaltyService.awardPoints(booking.getUser().getUserId(), booking.getBookingId(), payment.getAmount());
                loyaltyService.evaluateTier(booking.getUser().getUserId());
            } catch (Exception e) {
                log.error("Failed to award loyalty points for booking {}: {}", booking.getBookingId(), e.getMessage());
            }
        }

        try {
            emailService.sendBookingTicketEmail(booking, payment);
        } catch (Exception e) {
            log.error("Failed to send booking ticket email for booking {}: {}", booking.getBookingId(), e.getMessage());
        }

        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .transactionId(transactionId)
                .action("PAYMENT_SUCCESS")
                .requestPayload("Webhook Payload: " + payload)
                .build();
        auditLogRepository.save(auditLog);
    }

    private void handlePaymentFailed(String transactionId, String payload) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new BusinessException("Payment not found for transaction: " + transactionId));

        payment.setStatus("FAILED");
        paymentRepository.save(payment);

        Booking booking = payment.getBooking();
        booking.setPaymentStatus("FAILED");
        booking.setStatus("FAILED");

        bookingRepository.save(booking);

        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .transactionId(transactionId)
                .action("PAYMENT_FAILED")
                .requestPayload("Webhook Payload: " + payload)
                .build();
        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional
    public String verifyPayment(String paymentIntentId) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            throw new IllegalArgumentException("paymentIntentId must not be blank");
        }

        Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
                .or(() -> paymentRepository.findByBooking_BookingCode(paymentIntentId))
                .orElseGet(() -> {
                    Booking booking = bookingRepository.findByBookingCode(paymentIntentId)
                            .orElseThrow(() -> new ResourceNotFoundException("Payment or Booking", "code", paymentIntentId));
                    Payment newPayment = Payment.builder()
                            .booking(booking)
                            .amount(booking.getFinalPrice() != null ? booking.getFinalPrice() : booking.getTotalAmount())
                            .status("PENDING")
                            .transactionId("PAY-" + booking.getBookingCode())
                            .gateway("ONLINE")
                            .paymentMethod("ONLINE")
                            .build();
                    return paymentRepository.save(newPayment);
                });

        String transactionId = payment.getTransactionId();

        if ("SUCCESS".equals(payment.getStatus())) {
            return "SUCCESS";
        }
        if ("FAILED".equals(payment.getStatus())) {
            return "FAILED";
        }
        
        if ("MANUAL_BANK".equalsIgnoreCase(payment.getGateway()) || "BANK_TRANSFER".equalsIgnoreCase(payment.getPaymentMethod())) {
            if ("PENDING".equals(payment.getStatus())) {
                payment.setStatus("PENDING_VERIFICATION");
                paymentRepository.save(payment);
                
                Booking booking = payment.getBooking();
                booking.setPaymentStatus("PENDING_VERIFICATION");
                bookingRepository.save(booking);
            }
            return "PENDING";
        }
        
        if ("CASH".equalsIgnoreCase(payment.getGateway()) || "CASH".equalsIgnoreCase(payment.getPaymentMethod())) {
            return payment.getStatus();
        }

        handlePaymentSuccess(transactionId, "Verification Success");
        return "SUCCESS";
    }

    @Override
    @Transactional
    public void confirmCashPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId.toString()));
                
        if (!"CASH".equalsIgnoreCase(payment.getPaymentMethod())) {
            throw new BusinessException("Only cash payments can be confirmed manually.");
        }
        
        handlePaymentSuccess(payment.getTransactionId(), "Manual Cash Confirmation");
    }

    @Override
    @Transactional
    public void confirmBankTransfer(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId.toString()));
                
        if (!"BANK_TRANSFER".equalsIgnoreCase(payment.getPaymentMethod())) {
            throw new BusinessException("Only BANK_TRANSFER payments can be confirmed manually.");
        }
        
        handlePaymentSuccess(payment.getTransactionId(), "Manual Bank Transfer Confirmation");
    }

    @Override
    @Transactional
    public void processRefund(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId.toString()));

        String payStatus = booking.getPaymentStatus();
        if (!"SUCCESS".equals(payStatus) && !"DEPOSIT_30_PAID".equals(payStatus) && !"PARTIALLY_PAID".equals(payStatus) && !"REFUND_PENDING".equals(payStatus)) {
            throw new BusinessException("Cannot refund a booking that has not been paid.");
        }

        Payment payment = paymentRepository.findByBooking_BookingId(bookingId)
                .orElseThrow(() -> new BusinessException("No payment record found for this booking."));

        if (!"SUCCESS".equals(payment.getStatus()) && !"REFUND_PENDING".equals(payment.getStatus())) {
            throw new BusinessException("Only SUCCESS or REFUND_PENDING payments can be refunded.");
        }

        if ("REFUND_PENDING".equals(payment.getStatus()) && payment.getRefundAmount() != null) {
            throw new BusinessException("Refund is already processed or pending in gateway.");
        }

        LocalDateTime cancelDate = payment.getUpdatedAt() != null ? payment.getUpdatedAt() : LocalDateTime.now();
        LocalDateTime checkIn = booking.getCheckInDate();
        BigDecimal refundRatio = BigDecimal.ZERO;
        Long matchedPolicyId = null;

        if (checkIn != null) {
            long daysRemaining = ChronoUnit.DAYS.between(cancelDate.toLocalDate(), checkIn.toLocalDate());
            int daysRemainingInt = (int) daysRemaining;
            List<RefundPolicy> policies = refundPolicyRepository.findMatchingPoliciesOrdered(daysRemainingInt);
            if (!policies.isEmpty()) {
                RefundPolicy matchedPolicy = policies.get(0);
                refundRatio = matchedPolicy.getRefundPercentage().divide(BigDecimal.valueOf(100));
                matchedPolicyId = matchedPolicy.getPolicyId();
            }
        }

        BigDecimal refundAmount = payment.getAmount().multiply(refundRatio);
        String previousStatus = payment.getStatus();

        if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {
            payment.setStatus("REFUNDED");
            payment.setRefundAmount(BigDecimal.ZERO);
            payment.setRefundTime(LocalDateTime.now());
            paymentRepository.save(payment);

            booking.setStatus("CANCELLED");
            booking.setPaymentStatus("REFUNDED");
            bookingRepository.save(booking);

            // Log in refund_audit_logs
            RefundAuditLog refundAuditLog = RefundAuditLog.builder()
                    .bookingId(bookingId)
                    .paymentId(payment.getPaymentId())
                    .originalAmount(payment.getAmount())
                    .refundPercentage(BigDecimal.ZERO)
                    .refundAmount(BigDecimal.ZERO)
                    .previousPaymentStatus(previousStatus)
                    .newPaymentStatus("REFUNDED")
                    .policyId(matchedPolicyId)
                    .build();
            refundAuditLogRepository.save(refundAuditLog);

            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .action("REFUND_ZERO_BY_POLICY")
                    .requestPayload("Booking ID: " + bookingId + ", Refund Amount: 0 (Cancellation policy)")
                    .build();
            auditLogRepository.save(auditLog);
            return;
        }

        String refundTxnId = UUID.randomUUID().toString();
        payment.setRefundTransactionId(refundTxnId);
        payment.setRefundAmount(refundAmount);

        if (!"STRIPE".equalsIgnoreCase(payment.getGateway())) {
            // Simulated inline refund (VNPay, Cash, Bank Transfer, PayPal)
            payment.setStatus("REFUNDED");
            payment.setRefundTime(LocalDateTime.now());
            paymentRepository.save(payment);

            booking.setStatus("CANCELLED");
            booking.setPaymentStatus("REFUNDED");
            bookingRepository.save(booking);

            String customerEmail = booking.getUser() != null ? booking.getUser().getEmail() : "customer@example.com";
            emailService.sendRefundConfirmationEmail(customerEmail, booking.getBookingCode(), refundAmount);

            RefundAuditLog refundAuditLog = RefundAuditLog.builder()
                    .bookingId(bookingId)
                    .paymentId(payment.getPaymentId())
                    .originalAmount(payment.getAmount())
                    .refundPercentage(refundRatio.multiply(new BigDecimal("100")))
                    .refundAmount(refundAmount)
                    .previousPaymentStatus(previousStatus)
                    .newPaymentStatus("REFUNDED")
                    .policyId(matchedPolicyId)
                    .build();
            refundAuditLogRepository.save(refundAuditLog);

            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .transactionId(refundTxnId)
                    .action("REFUND_SUCCESS_SIMULATED")
                    .requestPayload("Booking ID: " + bookingId + ", Amount: " + refundAmount)
                    .build();
            auditLogRepository.save(auditLog);
            return;
        }

        // Stripe gateway refund
        payment.setStatus("REFUND_PENDING");
        paymentRepository.save(payment);

        RefundAuditLog refundAuditLog = RefundAuditLog.builder()
                .bookingId(bookingId)
                .paymentId(payment.getPaymentId())
                .originalAmount(payment.getAmount())
                .refundPercentage(refundRatio.multiply(new BigDecimal("100")))
                .refundAmount(refundAmount)
                .previousPaymentStatus(previousStatus)
                .newPaymentStatus("REFUND_PENDING")
                .policyId(matchedPolicyId)
                .build();
        refundAuditLogRepository.save(refundAuditLog);

        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .transactionId(refundTxnId)
                .action("REFUND_REQUESTED")
                .requestPayload("Booking ID: " + bookingId + ", Amount: " + payment.getRefundAmount())
                .build();
        auditLogRepository.save(auditLog);
    }

    @Override
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 60000)
    @Transactional
    public void retryFailedRefunds() {
        java.util.List<Payment> pendingRefunds = paymentRepository.findByStatusAndRefundRetryCountLessThan("REFUND_PENDING", 3);
        
        for (Payment payment : pendingRefunds) {
            if (payment.getRefundAmount() == null) {
                continue;
            }
            int currentRetry = payment.getRefundRetryCount() == null ? 0 : payment.getRefundRetryCount();
            payment.setRefundRetryCount(currentRetry + 1);
            
            // Cash/VNPAY/Bank Transfer simulated refund
            if (!"STRIPE".equalsIgnoreCase(payment.getGateway())) {
                payment.setStatus("REFUNDED");
                payment.setRefundTime(LocalDateTime.now());
                
                Booking booking = payment.getBooking();
                booking.setStatus("CANCELLED");
                booking.setPaymentStatus("REFUNDED");
                bookingRepository.save(booking);
                
                emailService.sendRefundConfirmationEmail(booking.getUser().getEmail(), booking.getBookingCode(), payment.getRefundAmount());
                
                log.info("Simulated refund successful for gateway {} on txn {}", payment.getGateway(), payment.getTransactionId());
                
                PaymentAuditLog auditLog = PaymentAuditLog.builder()
                        .transactionId(payment.getRefundTransactionId())
                        .action("REFUND_SUCCESS_SIMULATED")
                        .requestPayload("Refunded Amount: " + payment.getRefundAmount())
                        .build();
                auditLogRepository.save(auditLog);
                paymentRepository.save(payment);
                continue;
            }

            try {
                RefundCreateParams params = RefundCreateParams.builder()
                        .setPaymentIntent(payment.getTransactionId())
                        .setAmount(payment.getRefundAmount().multiply(new BigDecimal(100)).longValue())
                        .build();
                Refund refund = Refund.create(params);
                
                if ("succeeded".equals(refund.getStatus()) || "pending".equals(refund.getStatus())) {
                    payment.setStatus("REFUNDED");
                    payment.setRefundTime(LocalDateTime.now());
                    
                    Booking booking = payment.getBooking();
                    booking.setStatus("CANCELLED");
                    booking.setPaymentStatus("REFUNDED");
                    bookingRepository.save(booking);
                    
                    emailService.sendRefundConfirmationEmail(booking.getUser().getEmail(), booking.getBookingCode(), payment.getRefundAmount());
                    log.info("Refund successful for transaction {}", payment.getRefundTransactionId());
                    
                    PaymentAuditLog auditLog = PaymentAuditLog.builder()
                            .transactionId(payment.getRefundTransactionId())
                            .action("REFUND_SUCCESS")
                            .requestPayload("Refunded Amount: " + payment.getRefundAmount())
                            .build();
                    auditLogRepository.save(auditLog);
                } else {
                    handleRefundFailure(payment, currentRetry + 1);
                }
            } catch (Exception e) {
                log.error("Refund failed for transaction {}: ", payment.getRefundTransactionId(), e);
                handleRefundFailure(payment, currentRetry + 1);
            }
            
            paymentRepository.save(payment);
        }
    }
    
    private void handleRefundFailure(Payment payment, int attemptCount) {
        if (attemptCount >= 3) {
            payment.setStatus("MANUAL_REFUND_REQUIRED");
            log.warn("Manual refund required for payment: {}", payment.getPaymentId());
        }
        
        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .transactionId(payment.getRefundTransactionId())
                .action("REFUND_FAILED")
                .requestPayload("Attempt: " + attemptCount)
                .build();
        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional
    public void processVnpayCallback(Map<String, String> params) {
        boolean verified = vnpayService.verifyCallback(params);
        if (!verified) {
            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .action("VNPAY_CALLBACK_VERIFICATION_FAILED")
                    .requestPayload("Params: " + params.toString())
                    .build();
            auditLogRepository.save(auditLog);
            throw new SecurityException("VNPAY secure hash verification failed");
        }

        String transactionId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        if ("00".equals(responseCode)) {
            handlePaymentSuccess(transactionId, "VNPAY callback success: " + params.toString());
        } else {
            handlePaymentFailed(transactionId, "VNPAY callback failed (Code: " + responseCode + "): " + params.toString());
        }
    }

    @Override
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 60000)
    @Transactional
    public void checkExpiredPaymentHolds() {
        log.debug("Checking for expired payment holds");
        List<Payment> pendingPayments = paymentRepository.findExpiredPayments("PENDING", LocalDateTime.now());

        for (Payment payment : pendingPayments) {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);

            Booking booking = payment.getBooking();
            booking.setPaymentStatus("FAILED");
            booking.setStatus("FAILED");
            bookingRepository.save(booking);

            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .transactionId(payment.getTransactionId())
                    .action("PAYMENT_HOLD_EXPIRED")
                    .requestPayload("Hold expired for booking: " + booking.getBookingId())
                    .build();
            auditLogRepository.save(auditLog);

            log.info("Expired payment hold cancelled for transaction {} and booking {}", payment.getTransactionId(), booking.getBookingId());
        }
    }

    @Override
    @Transactional
    public void refundUnusedMealTickets(Long bookingId, BigDecimal unusedAmount) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId.toString()));

        Payment payment = paymentRepository.findByBooking_BookingId(bookingId)
                .orElseThrow(() -> new BusinessException("No payment record found for this booking."));

        if (!"SUCCESS".equals(payment.getStatus())) {
            throw new BusinessException("Cannot refund meals for an unpaid booking.");
        }

        BigDecimal totalRefunded = payment.getRefundAmount() != null ? payment.getRefundAmount() : BigDecimal.ZERO;
        BigDecimal newTotalRefund = totalRefunded.add(unusedAmount);

        if (newTotalRefund.compareTo(payment.getAmount()) > 0) {
            throw new BusinessException("Refund amount exceeds total payment amount.");
        }

        payment.setMealRefundAmount(unusedAmount);
        payment.setRefundAmount(newTotalRefund);
        paymentRepository.save(payment);

        // Credit to customer's personal E-Wallet
        Long userId = booking.getUser().getUserId();
        Wallet wallet = walletRepository.findByOwnerUserUserIdAndWalletType(userId, WalletType.PERSONAL)
                .orElseGet(() -> {
                    log.info("Creating personal wallet for user ID: {}", userId);
                    return walletService.createPersonalWallet(userId);
                });

        try {
            walletService.refundToWallet(wallet.getWalletId(), bookingId, unusedAmount);
            log.info("Successfully credited refund of {} to wallet ID: {} for user ID: {}", unusedAmount, wallet.getWalletId(), userId);
        } catch (Exception e) {
            log.error("Failed to credit refund to e-wallet for user ID: {}", userId, e);
            throw new BusinessException("Failed to credit refund to E-Wallet: " + e.getMessage());
        }

        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .transactionId(payment.getTransactionId())
                .action("MEAL_REFUND_SUCCESS")
                .requestPayload("Booking ID: " + bookingId + ", Refund Amount: " + unusedAmount)
                .build();
        auditLogRepository.save(auditLog);

        log.info("Refunded unused meal tickets for booking {}: Amount {}", bookingId, unusedAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId.toString()));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

            document.add(new Paragraph("VAT INVOICE / HOA DON GTGT", titleFont));
            document.add(new Paragraph("========================================", normalFont));
            document.add(new Paragraph("Transaction ID: " + payment.getTransactionId(), normalFont));
            document.add(new Paragraph("Booking Code: " + payment.getBooking().getBookingCode(), normalFont));
            document.add(new Paragraph("Payment Method: " + payment.getPaymentMethod(), normalFont));
            document.add(new Paragraph("Amount Paid: $" + payment.getAmount(), normalFont));
            document.add(new Paragraph("Payment Date: " + (payment.getPaymentTime() != null ? payment.getPaymentTime() : payment.getCreatedAt()), normalFont));

            if (payment.getInvoiceCompanyName() != null && !payment.getInvoiceCompanyName().isEmpty()) {
                document.add(new Paragraph("\nCUSTOMER BILLING DETAILS / THONG TIN DOANH NGHIEP", sectionFont));
                document.add(new Paragraph("Company: " + payment.getInvoiceCompanyName(), normalFont));
                document.add(new Paragraph("Tax ID: " + payment.getInvoiceTaxId(), normalFont));
                document.add(new Paragraph("Address: " + payment.getInvoiceCompanyAddress(), normalFont));
                document.add(new Paragraph("Email: " + payment.getInvoiceCompanyEmail(), normalFont));
            }

            document.add(new Paragraph("\n========================================", normalFont));
            document.add(new Paragraph("Thank you for choosing LuxuryStay Hotels!", normalFont));
            
            document.close();
        } catch (Exception e) {
            log.error("Failed to generate PDF", e);
            throw new BusinessException("Failed to generate invoice PDF: " + e.getMessage());
        }

        return baos.toByteArray();
    }

    @Override
    @Transactional
    public Payout calculateMonthlyPayout(Long hotelId, LocalDateTime start, LocalDateTime end) {
        List<Payment> successfulPayments = paymentRepository.findSuccessfulPaymentsByHotelAndDate(hotelId, start, end);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Payment payment : successfulPayments) {
            totalRevenue = totalRevenue.add(payment.getAmount());
        }

        BigDecimal commissionRate = new BigDecimal("0.10"); // Default 10%
        BigDecimal commissionAmount = totalRevenue.multiply(commissionRate);
        BigDecimal payoutAmount = totalRevenue.subtract(commissionAmount);

        Payout payout = Payout.builder()
                .hotelId(hotelId)
                .periodStart(start)
                .periodEnd(end)
                .totalRevenue(totalRevenue)
                .commissionRate(commissionRate.multiply(new BigDecimal("100"))) // Save as percentage (e.g. 10.0)
                .payoutAmount(payoutAmount)
                .status("PENDING")
                .build();

        return payoutRepository.save(payout);
    }

    @Override
    @Transactional
    public void approvePayout(Long payoutId) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Payout", "id", payoutId.toString()));

        if (!"PENDING".equals(payout.getStatus())) {
            throw new BusinessException("Payout is already processed (Status: " + payout.getStatus() + ")");
        }

        payout.setStatus("PAID");
        payoutRepository.save(payout);

        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .transactionId("PAYOUT-" + payoutId)
                .action("PAYOUT_APPROVED")
                .requestPayload("Hotel ID: " + payout.getHotelId() + ", Amount: " + payout.getPayoutAmount())
                .build();
        auditLogRepository.save(auditLog);

        log.info("Payout {} approved and marked as PAID", payoutId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payout> getPayoutsByHotel(Long hotelId) {
        return payoutRepository.findByHotelId(hotelId);
    }

    @Override
    @Transactional
    public Map<String, Object> verifyPaymentDetails(String paymentIntentId) {
        String status = verifyPayment(paymentIntentId);
        
        Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", paymentIntentId));
        com.hotelbooking.booking.Booking booking = payment.getBooking();
        
        boolean isDeposit = Boolean.TRUE.equals(payment.getIsDeposit());
        BigDecimal depositRatio = payment.getDepositRatio();
        BigDecimal totalAmount = booking.getFinalPrice() != null ? booking.getFinalPrice() : booking.getTotalAmount();
        
        if (!isDeposit && payment.getAmount().compareTo(totalAmount) < 0 && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            isDeposit = true;
            depositRatio = payment.getAmount().divide(totalAmount, 2, java.math.RoundingMode.HALF_UP);
        }
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("status", status);
        response.put("bookingId", booking.getBookingId());
        response.put("bookingCode", booking.getBookingCode());
        response.put("isDeposit", isDeposit);
        response.put("depositRatio", depositRatio);
        response.put("amount", payment.getAmount());
        response.put("totalAmount", totalAmount);
        return response;
    }
}

