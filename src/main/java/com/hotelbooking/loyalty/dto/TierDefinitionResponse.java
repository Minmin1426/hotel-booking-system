package com.hotelbooking.loyalty.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierDefinitionResponse {
    private Long tierId;
    private String name;
    private String accountType;
    private BigDecimal minAnnualSpend;
    private BigDecimal pointMultiplier;
    private BigDecimal maxSpendingLimit;
    private Boolean prioritySupport;
    private Boolean exclusiveVoucherAccess;
    private LocalDateTime createdAt;
}
