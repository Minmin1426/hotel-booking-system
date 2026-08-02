package com.hotelbooking.wallet.topup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingStatusResponse {
    private BigDecimal perTransactionLimit;
    private BigDecimal dailyLimit;
    private BigDecimal dailySpent;
    private BigDecimal remainingDaily;
    private BigDecimal monthlyLimit;
    private BigDecimal monthlySpent;
    private BigDecimal remainingMonthly;
}
