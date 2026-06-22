package com.hotelbooking.service.impl;

import com.hotelbooking.dto.PaymentRequestDTO;
import com.hotelbooking.dto.PaymentResponseDTO;
import com.hotelbooking.dto.WebhookCallbackDTO;
import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.model.Booking;
import com.hotelbooking.model.Payment;
import com.hotelbooking.model.PaymentAuditLog;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.PaymentAuditLogRepository;
import com.hotelbooking.repository.PaymentRepository;
import com.hotelbooking.service.EmailService;
import com.hotelbooking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
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

    @Value("${payment.gateway.secret}")
    private String gatewaySecret;

    @Override
    @Transactional
    public PaymentResponseDTO createPaymentRequest(PaymentRequestDTO requestDTO) {
        Booking booking = bookingRepository.findById(requestDTO.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", requestDTO.getBookingId().toString()));

        if (!"PENDING_PAYMENT".equals(booking.getStatus())) {
            throw new BusinessException("Booking is not in PENDING_PAYMENT status");
        }

        String transactionId = UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .booking(booking)
                .paymentMethod(requestDTO.getPaymentMethod())
                .amount(booking.getTotalAmount())
                .status("PROCESSING")
                .transactionId(transactionId)
                .gateway(requestDTO.getPaymentMethod())
                .build();

        paymentRepository.save(payment);

        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .transactionId(transactionId)
                .action("CREATE_REQUEST")
                .requestPayload("Booking ID: " + booking.getBookingId() + ", Amount: " + booking.getTotalAmount())
                .build();
        auditLogRepository.save(auditLog);

        String paymentUrl = "https://mock-gateway.com/pay?txn=" + transactionId;

        return PaymentResponseDTO.builder()
                .transactionId(transactionId)
                .paymentUrl(paymentUrl)
                .build();
    }

    @Override
    @Transactional
    public void processWebhook(WebhookCallbackDTO callback, String signature, String rawPayload) {
        // 1. Verify HMAC Signature
        if (!verifySignature(rawPayload, signature)) {
            log.warn("Invalid webhook signature for transaction: {}", callback.getTransactionId());
            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .transactionId(callback.getTransactionId())
                    .action("WEBHOOK_FAILED_SIGNATURE")
                    .requestPayload(rawPayload)
                    .build();
            auditLogRepository.save(auditLog);
            throw new SecurityException("Invalid webhook signature");
        }

        // 2. Duplicate Check
        Payment payment = paymentRepository.findByTransactionId(callback.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", callback.getTransactionId()));

        if ("SUCCESS".equals(payment.getStatus()) || "FAILED".equals(payment.getStatus())) {
            log.info("Duplicate webhook callback for transaction: {}", callback.getTransactionId());
            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .transactionId(callback.getTransactionId())
                    .action("WEBHOOK_DUPLICATE")
                    .requestPayload(rawPayload)
                    .build();
            auditLogRepository.save(auditLog);
            return; // Ignore duplicate
        }

        // 3. Update Status
        payment.setStatus(callback.getStatus());
        payment.setPaymentTime(LocalDateTime.now());
        paymentRepository.save(payment);

        Booking booking = payment.getBooking();
        
        if ("SUCCESS".equals(callback.getStatus())) {
            booking.setPaymentStatus("SUCCESS");
            booking.setStatus("CONFIRMED");
            bookingRepository.save(booking);
            emailService.sendBookingConfirmationEmail(booking.getUser().getEmail(), booking.getBookingCode());
        } else {
            booking.setPaymentStatus("FAILED");
            // Booking keeps PENDING_PAYMENT status per AF-01
            bookingRepository.save(booking);
        }

        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .transactionId(callback.getTransactionId())
                .action("WEBHOOK_PROCESSED")
                .requestPayload(rawPayload)
                .responsePayload("Status updated to: " + callback.getStatus())
                .build();
        auditLogRepository.save(auditLog);
    }

    private boolean verifySignature(String payload, String signature) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(gatewaySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKeySpec);
            byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().equalsIgnoreCase(signature);
        } catch (Exception e) {
            log.error("Error verifying signature", e);
            return false;
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
