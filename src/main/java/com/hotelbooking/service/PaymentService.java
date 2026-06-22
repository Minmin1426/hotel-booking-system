package com.hotelbooking.service;

import com.hotelbooking.dto.PaymentRequestDTO;
import com.hotelbooking.dto.PaymentResponseDTO;
import com.hotelbooking.dto.WebhookCallbackDTO;

public interface PaymentService {
    PaymentResponseDTO createPaymentRequest(PaymentRequestDTO requestDTO);
    void processWebhook(WebhookCallbackDTO callback, String signature, String rawPayload);
    void processRefund(Long bookingId);
    void retryFailedRefunds();
}
