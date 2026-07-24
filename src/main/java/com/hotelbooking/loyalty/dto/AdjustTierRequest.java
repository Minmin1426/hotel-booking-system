package com.hotelbooking.loyalty.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjustTierRequest {

    @NotBlank(message = "Tier name is required")
    private String tier; // BRONZE, SILVER, GOLD, PLATINUM, *_BUSINESS

    @NotBlank(message = "Reason is required")
    private String reason;
}
