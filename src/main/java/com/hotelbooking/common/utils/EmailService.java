package com.hotelbooking.common.utils;

import com.hotelbooking.booking.Booking;
import com.hotelbooking.payment.Payment;
import java.math.BigDecimal;

public interface EmailService {
    void sendResetPasswordEmail(String email, String token);
    void sendBookingConfirmationEmail(String email, String bookingCode);
    void sendBookingTicketEmail(Booking booking, Payment payment);
    void sendBookingCancellationEmail(Booking booking, Payment payment);
    void sendRefundConfirmationEmail(String email, String bookingCode, BigDecimal refundAmount);
    void sendEmail(String to, String subject, String body);
}
