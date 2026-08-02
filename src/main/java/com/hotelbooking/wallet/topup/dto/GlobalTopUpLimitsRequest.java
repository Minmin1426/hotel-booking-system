package com.hotelbooking.wallet.topup.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalTopUpLimitsRequest {
    @NotNull
    @DecimalMin(value = "1", message = "Max single top-up must be positive")
    private BigDecimal maxSingleTopUp;

    @NotNull
    @DecimalMin(value = "1", message = "Max wallet balance must be positive")
    private BigDecimal maxWalletBalance;
}
