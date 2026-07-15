package com.hotelbooking.payment;
import com.hotelbooking.payment.dto.PaymentRequestDTO;
import com.hotelbooking.payment.dto.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO createPaymentRequest(PaymentRequestDTO requestDTO);
    String verifyPayment(String paymentIntentId);
    void processRefund(Long bookingId);
    void retryFailedRefunds();
    void processStripeWebhook(String payload, String sigHeader);
    void confirmCashPayment(Long paymentId);
    void confirmBankTransfer(Long paymentId);
}
