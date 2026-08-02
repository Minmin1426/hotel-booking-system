package com.hotelbooking.common.utils;

public interface EmailService {
    void sendResetPasswordEmail(String email, String token);
    void sendBookingConfirmationEmail(String email, String bookingCode);
    void sendRefundConfirmationEmail(String email, String bookingCode, java.math.BigDecimal refundAmount);
    void sendEmail(String to, String subject, String body);
    void sendOtpEmail(String email, String fullName, String otpCode);
}
