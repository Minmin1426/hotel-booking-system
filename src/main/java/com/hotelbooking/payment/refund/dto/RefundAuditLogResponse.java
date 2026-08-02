package com.hotelbooking.payment.refund.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundAuditLogResponse {
    private Long auditId;
    private Long bookingId;
    private Long paymentId;
    private BigDecimal originalAmount;
    private BigDecimal refundPercentage;
    private BigDecimal refundAmount;
    private Long overrideBy;
    private String overrideReason;
    private String previousPaymentStatus;
    private String newPaymentStatus;
    private Long policyId;
    private LocalDateTime createdAt;
}
