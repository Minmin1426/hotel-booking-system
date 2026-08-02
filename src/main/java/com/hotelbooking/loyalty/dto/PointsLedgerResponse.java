package com.hotelbooking.loyalty.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsLedgerResponse {
    private Long ledgerId;
    private Long bookingId;
    private Integer pointsEarned;
    private BigDecimal multiplierUsed;
    private Long runningBalance;
    private LocalDateTime createdAt;
}
