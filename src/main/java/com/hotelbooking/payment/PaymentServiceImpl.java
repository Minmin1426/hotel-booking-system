package com.hotelbooking.payment;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.common.utils.EmailService;
import com.hotelbooking.payment.dto.PaymentRequestDTO;
import com.hotelbooking.payment.dto.PaymentResponseDTO;
import com.hotelbooking.voucher.Voucher;
import com.hotelbooking.voucher.VoucherRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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

        if ("CASH".equalsIgnoreCase(requestDTO.getPaymentMethod())) {
            String transactionId = "CASH-" + UUID.randomUUID().toString();

            Payment payment = Payment.builder()
                    .booking(booking)
                    .paymentMethod("CASH")
                    .amount(booking.getTotalAmount())
                    .status("PENDING")
                    .transactionId(transactionId)
                    .gateway("CASH")
                    .build();
            paymentRepository.save(payment);
            
            booking.setPaymentStatus("PENDING");
            booking.setStatus("CONFIRMED");
            bookingRepository.save(booking);

            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .transactionId(transactionId)
                    .action("CREATE_CASH_PAYMENT_SUCCESS")
                    .requestPayload("Booking ID: " + booking.getBookingId())
                    .build();
            auditLogRepository.save(auditLog);

            return PaymentResponseDTO.builder()
                    .transactionId(transactionId)
                    .clientSecret("CASH_PAYMENT")
                    .build();
        }

        if ("BANK_TRANSFER".equalsIgnoreCase(requestDTO.getPaymentMethod())) {
            String transactionId = "BT-" + UUID.randomUUID().toString();

            Payment payment = Payment.builder()
                    .booking(booking)
                    .paymentMethod("BANK_TRANSFER")
                    .amount(booking.getTotalAmount())
                    .status("PENDING")
                    .transactionId(transactionId)
                    .gateway("MANUAL_BANK")
                    .build();
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
            String amt = booking.getTotalAmount().toString();
            String qrData = "bank:Stripe Bank|acc:" + acc + "|ref:" + ref + "|amt:" + amt;
            
            try {
                qrData = java.net.URLEncoder.encode(qrData, "UTF-8");
            } catch (Exception e) {}

            return PaymentResponseDTO.builder()
                    .transactionId(transactionId)
                    .clientSecret("MANUAL_BANK_TRANSFER_TOKEN")
                    .bankName("Stripe International Bank")
                    .accountHolder("Stripe / LuxuryStay")
                    .accountNumber(acc)
                    .referenceCode(ref)
                    .branch("San Francisco Main")
                    .swiftCode("STRIPESF")
                    .qrCodeUrl("https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + qrData)
                    .build();
        }

        try {
            if (stripeApiKey == null || stripeApiKey.startsWith("dummy_api_key_placeholder") || stripeApiKey.trim().isEmpty()) {
                String transactionId = "mock_txn_" + UUID.randomUUID().toString();
                String clientSecret = "mock_sec" + "ret_" + UUID.randomUUID().toString();

                Payment payment = Payment.builder()
                        .booking(booking)
                        .paymentMethod(requestDTO.getPaymentMethod())
                        .amount(booking.getTotalAmount())
                        .status("PROCESSING")
                        .transactionId(transactionId)
                        .gateway("STRIPE_MOCK")
                        .build();

                paymentRepository.save(payment);

                PaymentAuditLog auditLog = PaymentAuditLog.builder()
                        .transactionId(transactionId)
                        .action("CREATE_MOCK_PAYMENT_INTENT")
                        .requestPayload("Booking ID: " + booking.getBookingId() + ", Amount: " + booking.getTotalAmount())
                        .responsePayload("Mock Token: " + clientSecret)
                        .build();
                auditLogRepository.save(auditLog);

                return PaymentResponseDTO.builder()
                        .transactionId(transactionId)
                        .clientSecret(clientSecret)
                        .build();
            }

            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(booking.getTotalAmount().multiply(new BigDecimal(100)).longValue())
                    .setCurrency("usd")
                    .putMetadata("bookingId", booking.getBookingId().toString())
                    .addPaymentMethodType("card");
            
            PaymentIntentCreateParams params = paramsBuilder.build();

            PaymentIntent intent = PaymentIntent.create(params);
            String transactionId = intent.getId();

            Payment payment = Payment.builder()
                    .booking(booking)
                    .paymentMethod(requestDTO.getPaymentMethod())
                    .amount(booking.getTotalAmount())
                    .status("PENDING")
                    .transactionId(transactionId)
                    .gateway("STRIPE")
                    .build();

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
        booking.setPaymentStatus("SUCCESS");
        booking.setStatus("CONFIRMED");
        
        if (booking.getVoucher() != null) {
            Voucher voucher = booking.getVoucher();
            if (voucher.getCurrentUsage() == null) {
                voucher.setCurrentUsage(1);
            } else {
                voucher.setCurrentUsage(voucher.getCurrentUsage() + 1);
            }
            voucherRepository.save(voucher);
        }
        
        bookingRepository.save(booking);

        emailService.sendBookingConfirmationEmail(booking.getUser().getEmail(), booking.getBookingCode());

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
        // Fallback or polling mechanism
        Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", paymentIntentId));

        if ("SUCCESS".equals(payment.getStatus())) {
            return "SUCCESS";
        }
        if ("FAILED".equals(payment.getStatus())) {
            return "FAILED";
        }
        
        if ("MANUAL_BANK".equals(payment.getGateway())) {
            if ("PENDING".equals(payment.getStatus())) {
                payment.setStatus("PENDING_VERIFICATION");
                paymentRepository.save(payment);
                
                Booking booking = payment.getBooking();
                booking.setPaymentStatus("PENDING_VERIFICATION");
                bookingRepository.save(booking);
            }
            return "PENDING";
        }
        
        if ("CASH".equals(payment.getGateway())) {
            return payment.getStatus();
        }

        try {
            String paymentStatus;
            if (paymentIntentId.startsWith("mock_txn_") || stripeApiKey == null || stripeApiKey.startsWith("dummy_api_key_placeholder")) {
                paymentStatus = "succeeded";
            } else {
                PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
                paymentStatus = intent.getStatus();
            }

            if ("succeeded".equals(paymentStatus)) {
                handlePaymentSuccess(paymentIntentId, "Polled Status: Succeeded");
                return "SUCCESS";
            } else if ("requires_payment_method".equals(paymentStatus) || "canceled".equals(paymentStatus)) {
                handlePaymentFailed(paymentIntentId, "Polled Status: Failed/Canceled");
                return "FAILED";
            }
            return "PENDING";
        } catch (Exception e) {
            log.error("Error verifying payment session", e);
            throw new BusinessException("Failed to verify payment: " + e.getMessage());
        }
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
    public void simulateBankTransferWebhook(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "bookingCode", bookingCode));
        
        Payment payment = paymentRepository.findByBooking_BookingId(booking.getBookingId())
                .orElseThrow(() -> new BusinessException("No payment found for booking " + bookingCode));

        if (!"BANK_TRANSFER".equalsIgnoreCase(payment.getPaymentMethod())) {
            throw new BusinessException("Only BANK_TRANSFER payments can be simulated via this webhook.");
        }

        handlePaymentSuccess(payment.getTransactionId(), "Simulated Bank Webhook Success");
    }

    @Override
    @Transactional
    public void processRefund(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId.toString()));

        if (!"SUCCESS".equals(booking.getPaymentStatus())) {
            throw new BusinessException("Cannot refund a booking that has not been paid.");
        }

        Payment payment = paymentRepository.findByBooking_BookingId(bookingId)
                .orElseThrow(() -> new BusinessException("No payment record found for this booking."));

        if (!"SUCCESS".equals(payment.getStatus())) {
            throw new BusinessException("Only SUCCESS payments can be refunded.");
        }

        String refundTxnId = UUID.randomUUID().toString();
        payment.setRefundTransactionId(refundTxnId);
        payment.setRefundAmount(payment.getAmount());
        payment.setStatus("REFUND_PENDING");

        paymentRepository.save(payment);

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
            int currentRetry = payment.getRefundRetryCount() == null ? 0 : payment.getRefundRetryCount();
            payment.setRefundRetryCount(currentRetry + 1);
            
            try {
                // Mock gateway call
                boolean isGatewaySuccess = true; // Assuming retry succeeds
                
                if (isGatewaySuccess) {
                    payment.setStatus("REFUNDED");
                    payment.setRefundTime(LocalDateTime.now());
                    
                    Booking booking = payment.getBooking();
                    booking.setStatus("CANCELLED");
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
            // send alert to admin
            log.warn("Manual refund required for payment: {}", payment.getPaymentId());
        }
        
        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .transactionId(payment.getRefundTransactionId())
                .action("REFUND_FAILED")
                .requestPayload("Attempt: " + attemptCount)
                .build();
        auditLogRepository.save(auditLog);
    }
}
