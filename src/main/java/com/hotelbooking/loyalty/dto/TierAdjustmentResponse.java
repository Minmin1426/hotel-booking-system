package com.hotelbooking.loyalty.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierAdjustmentResponse {
    private Long userId;
    private String previousTier;
    private String newTier;
    private String reason;
}
