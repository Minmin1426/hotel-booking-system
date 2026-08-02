package com.hotelbooking.payment;
import com.hotelbooking.payment.dto.PaymentRequestDTO;
import com.hotelbooking.payment.dto.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO createPaymentRequest(PaymentRequestDTO requestDTO);
    String verifyPayment(String paymentIntentId);
    java.util.Map<String, Object> verifyPaymentDetails(String paymentIntentId);
    void processRefund(Long bookingId);
    void retryFailedRefunds();
    void processStripeWebhook(String payload, String sigHeader);
    void confirmCashPayment(Long paymentId);
    void confirmBankTransfer(Long paymentId);

    // Extended features for Member 4
    void processVnpayCallback(java.util.Map<String, String> params);
    void checkExpiredPaymentHolds();
    void refundUnusedMealTickets(Long bookingId, java.math.BigDecimal unusedAmount);
    byte[] generateInvoicePdf(Long paymentId);
    Payout calculateMonthlyPayout(Long hotelId, java.time.LocalDateTime start, java.time.LocalDateTime end);
    void approvePayout(Long payoutId);
    java.util.List<Payout> getPayoutsByHotel(Long hotelId);
}
