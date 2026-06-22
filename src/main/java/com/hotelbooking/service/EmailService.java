package com.hotelbooking.service;

public interface EmailService {
    void sendResetPasswordEmail(String email, String token);
    void sendBookingConfirmationEmail(String email, String bookingCode);
    void sendRefundConfirmationEmail(String email, String bookingCode, java.math.BigDecimal refundAmount);
}
