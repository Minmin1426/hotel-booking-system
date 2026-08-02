package com.hotelbooking.payment.refund.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundPolicyResponse {
    private Long policyId;
    private String name;
    private Integer daysBeforeCheckin;
    private BigDecimal refundPercentage;
    private String description;
    private Integer priority;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
