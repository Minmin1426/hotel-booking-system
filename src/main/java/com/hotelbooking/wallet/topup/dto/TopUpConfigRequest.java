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
public class TopUpConfigRequest {
    @NotNull
    private Boolean enabled;

    @NotNull
    @DecimalMin(value = "0", message = "Threshold must be non-negative")
    private BigDecimal thresholdAmount;

    @NotNull
    @DecimalMin(value = "1", message = "Top-up amount must be positive")
    private BigDecimal topupAmount;

    private String paymentMethodId;
}
