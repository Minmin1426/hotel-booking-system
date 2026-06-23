package com.hotelbooking.payment;
import com.hotelbooking.payment.dto.PaymentRequestDTO;
import com.hotelbooking.payment.dto.PaymentResponseDTO;
import com.hotelbooking.payment.dto.WebhookCallbackDTO;

public interface PaymentService {
    PaymentResponseDTO createPaymentRequest(PaymentRequestDTO requestDTO);
    void processWebhook(WebhookCallbackDTO callback, String signature, String rawPayload);
    void processRefund(Long bookingId);
    void retryFailedRefunds();
}
