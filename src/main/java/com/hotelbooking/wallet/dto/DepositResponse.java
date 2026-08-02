package com.hotelbooking.wallet.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositResponse {
    private Long transactionId;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String paymentUrl; // Stripe payment URL (populated for STRIPE method)
    private String paymentMethod;
}
