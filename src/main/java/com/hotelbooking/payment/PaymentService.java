package com.hotelbooking.payment;
import com.hotelbooking.payment.dto.PaymentRequestDTO;
import com.hotelbooking.payment.dto.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO createPaymentRequest(PaymentRequestDTO requestDTO);
    void verifyPayment(String paymentIntentId);
    void processRefund(Long bookingId);
    void retryFailedRefunds();
}
