package com.hotelbooking.wallet.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletAdjustmentResponse {
    private Long transactionId;
    private Long walletId;
    private BigDecimal amountAdjusted;
    private BigDecimal newBalance;
    private String adjustmentType; // "CREDIT" | "DEBIT"
    private String reason;
}
