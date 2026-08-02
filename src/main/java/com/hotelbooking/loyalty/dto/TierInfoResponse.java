package com.hotelbooking.loyalty.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierInfoResponse {
    private Long userId;
    private String currentTier;
    private BigDecimal pointMultiplier;
    private BigDecimal annualSpend;
    private Long lifetimePoints;
    private String nextTier;
    private BigDecimal amountToNextTier;
    private Boolean prioritySupport;
    private Boolean exclusiveVoucherAccess;
    private LocalDateTime tierEvaluatedAt;
    private List<String> benefits;
}
