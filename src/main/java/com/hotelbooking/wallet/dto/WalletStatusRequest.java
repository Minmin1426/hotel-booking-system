package com.hotelbooking.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletStatusRequest {

    @NotBlank(message = "Status is required")
    private String status; // "ACTIVE" | "FROZEN" | "CLOSED"

    private String reason;
}
