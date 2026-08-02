package com.hotelbooking.wallet.topup.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingLimitRequest {
    @DecimalMin(value = "0", message = "Per-transaction limit must be non-negative")
    private BigDecimal perTransactionLimit;

    @DecimalMin(value = "0", message = "Daily limit must be non-negative")
    private BigDecimal dailyLimit;

    @DecimalMin(value = "0", message = "Monthly limit must be non-negative")
    private BigDecimal monthlyLimit;

    @NotNull
    private LocalDate effectiveFrom;

    @Future
    private LocalDate effectiveUntil;
}
