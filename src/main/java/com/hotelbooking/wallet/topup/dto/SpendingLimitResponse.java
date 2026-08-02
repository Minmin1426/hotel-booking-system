package com.hotelbooking.wallet.topup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingLimitResponse {
    private Long limitId;
    private Long groupId;
    private Long memberUserId;
    private BigDecimal perTransactionLimit;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private LocalDate effectiveFrom;
    private LocalDate effectiveUntil;
    private Long createdByUserId;
    private LocalDateTime createdAt;
}
