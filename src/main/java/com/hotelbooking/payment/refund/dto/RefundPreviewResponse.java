package com.hotelbooking.payment.refund.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundPreviewResponse {
    private Long bookingId;
    private BigDecimal originalAmount;
    private BigDecimal refundPercentage;
    private BigDecimal refundAmount;
    private String policyName;
    private String policyDescription;
    private Integer daysRemaining;
}
