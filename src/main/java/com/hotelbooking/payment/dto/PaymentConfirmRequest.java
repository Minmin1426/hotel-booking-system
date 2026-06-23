package com.hotelbooking.payment.dto;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.payment.Payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmRequest {

    @NotBlank(message = "Booking code is required")
    private String bookingCode;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // ONLINE | CASH | BANK_TRANSFER
}
