package com.hotelbooking.loyalty.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierHistoryResponse {
    private Long historyId;
    private String previousTier;
    private String newTier;
    private String reason;
    private Long changedBy;
    private LocalDateTime changedAt;
}
