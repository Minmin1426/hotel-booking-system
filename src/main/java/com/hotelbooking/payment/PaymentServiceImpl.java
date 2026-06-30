package com.hotelbooking.payment;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.common.utils.EmailService;
import com.hotelbooking.payment.dto.PaymentRequestDTO;
import com.hotelbooking.payment.dto.PaymentResponseDTO;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
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
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAuditLogRepository auditLogRepository;
    private final EmailService emailService;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.success.url}")
    private String stripeSuccessUrl;

    @Value("${stripe.cancel.url}")
    private String stripeCancelUrl;

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

        try {
            if (stripeApiKey == null || stripeApiKey.startsWith("sk_test_placeholder") || stripeApiKey.trim().isEmpty()) {
                String transactionId = "mock_txn_" + UUID.randomUUID().toString();
                String clientSecret = "mock_secret_" + UUID.randomUUID().toString();

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
                        .responsePayload("Mock Secret: " + clientSecret)
                        .build();
                auditLogRepository.save(auditLog);

                return PaymentResponseDTO.builder()
                        .transactionId(transactionId)
                        .clientSecret(clientSecret)
                        .build();
            }

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(booking.getTotalAmount().multiply(new BigDecimal(100)).longValue())
                    .setCurrency("usd")
                    .putMetadata("bookingId", booking.getBookingId().toString())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            String transactionId = intent.getId();

            Payment payment = Payment.builder()
                    .booking(booking)
                    .paymentMethod(requestDTO.getPaymentMethod())
                    .amount(booking.getTotalAmount())
                    .status("PROCESSING")
                    .transactionId(transactionId)
                    .gateway("STRIPE")
                    .build();

            paymentRepository.save(payment);

            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .transactionId(transactionId)
                    .action("CREATE_STRIPE_PAYMENT_INTENT")
                    .requestPayload("Booking ID: " + booking.getBookingId() + ", Amount: " + booking.getTotalAmount())
                    .responsePayload("Intent ID: " + intent.getId())
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
    public void verifyPayment(String paymentIntentId) {
        Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", paymentIntentId));

        if ("SUCCESS".equals(payment.getStatus()) || "FAILED".equals(payment.getStatus())) {
            log.info("Payment already verified for intent: {}", paymentIntentId);
            return; // Ignore duplicate
        }

        try {
            String paymentStatus;
            if (paymentIntentId.startsWith("mock_txn_") || stripeApiKey == null || stripeApiKey.startsWith("sk_test_placeholder")) {
                paymentStatus = "succeeded";
            } else {
                PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
                paymentStatus = intent.getStatus();
            }

            if ("succeeded".equals(paymentStatus)) {
                payment.setStatus("SUCCESS");
                payment.setPaymentTime(LocalDateTime.now());
                
                Booking booking = payment.getBooking();
                booking.setPaymentStatus("SUCCESS");
                booking.setStatus("CONFIRMED");
                bookingRepository.save(booking);
                
                emailService.sendBookingConfirmationEmail(booking.getUser().getEmail(), booking.getBookingCode());
            } else {
                payment.setStatus("FAILED");
                payment.setPaymentTime(LocalDateTime.now());
                
                Booking booking = payment.getBooking();
                booking.setPaymentStatus("FAILED");
                bookingRepository.save(booking);
            }
            
            paymentRepository.save(payment);

            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .transactionId(paymentIntentId)
                    .action("VERIFY_PAYMENT")
                    .requestPayload("Intent ID: " + paymentIntentId)
                    .responsePayload("Stripe Status: " + paymentStatus)
                    .build();
            auditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            log.error("Error verifying payment session", e);
            throw new BusinessException("Failed to verify payment: " + e.getMessage());
        }
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

        if ("SUCCESS".equals(payment.getRefundStatus())) {
            throw new BusinessException("This booking has already been refunded.");
        }

        String refundTxnId = UUID.randomUUID().toString();
        payment.setRefundTransactionId(refundTxnId);
        payment.setRefundAmount(payment.getAmount()); // Full refund
        payment.setRefundTime(LocalDateTime.now());

        try {
            // Mock gateway call
            boolean isGatewaySuccess = true; // Simulating success for the mock gateway

            if (isGatewaySuccess) {
                payment.setRefundStatus("SUCCESS");
                booking.setStatus("CANCELLED"); // Cancel booking on refund
                
                emailService.sendRefundConfirmationEmail(booking.getUser().getEmail(), booking.getBookingCode(), payment.getRefundAmount());
            } else {
                payment.setRefundStatus("FAILED");
            }
        } catch (Exception e) {
            log.error("Refund gateway error: ", e);
            payment.setRefundStatus("FAILED");
        }

        paymentRepository.save(payment);
        bookingRepository.save(booking);

        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .transactionId(refundTxnId)
                .action("REFUND_REQUESTED")
                .requestPayload("Booking ID: " + bookingId + ", Amount: " + payment.getRefundAmount())
                .responsePayload("Status: " + payment.getRefundStatus())
                .build();
        auditLogRepository.save(auditLog);
    }

    @Override
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 60000)
    @Transactional
    public void retryFailedRefunds() {
        java.util.List<Payment> failedRefunds = paymentRepository.findByRefundStatusAndRefundRetryCountLessThan("FAILED", 3);
        
        for (Payment payment : failedRefunds) {
            int currentRetry = payment.getRefundRetryCount() == null ? 0 : payment.getRefundRetryCount();
            payment.setRefundRetryCount(currentRetry + 1);
            
            try {
                // Mock gateway call
                boolean isGatewaySuccess = true; // Assuming retry succeeds
                
                if (isGatewaySuccess) {
                    payment.setRefundStatus("SUCCESS");
                    payment.setRefundTime(LocalDateTime.now());
                    
                    Booking booking = payment.getBooking();
                    booking.setStatus("CANCELLED");
                    bookingRepository.save(booking);
                    
                    emailService.sendRefundConfirmationEmail(booking.getUser().getEmail(), booking.getBookingCode(), payment.getRefundAmount());
                    log.info("Retry {} successful for refund transaction {}", currentRetry + 1, payment.getRefundTransactionId());
                }
            } catch (Exception e) {
                log.error("Retry {} failed for refund transaction {}: ", currentRetry + 1, payment.getRefundTransactionId(), e);
            }
            
            paymentRepository.save(payment);
            
            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .transactionId(payment.getRefundTransactionId())
                    .action("REFUND_RETRY_" + (currentRetry + 1))
                    .responsePayload("Status: " + payment.getRefundStatus())
                    .build();
            auditLogRepository.save(auditLog);
        }
    }
}
