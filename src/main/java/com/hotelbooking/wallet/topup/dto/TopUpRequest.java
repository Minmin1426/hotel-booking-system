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
public class TopUpRequest {
    @NotNull
    @DecimalMin(value = "1", message = "Amount must be positive")
    private BigDecimal amount;
}
