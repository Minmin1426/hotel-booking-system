package com.hotelbooking.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletAdjustmentRequest {

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Type is required (CREDIT or DEBIT)")
    private String type; // "CREDIT" | "DEBIT"

    @NotBlank(message = "Reason is required")
    private String reason;
}
