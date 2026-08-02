package com.hotelbooking.payment.refund;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "original_amount", precision = 18, scale = 2)
    private BigDecimal originalAmount;

    @Column(name = "refund_percentage", precision = 5, scale = 2)
    private BigDecimal refundPercentage;

    @Column(name = "refund_amount", precision = 18, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "override_by")
    private Long overrideBy;

    @Column(name = "override_reason", columnDefinition = "TEXT")
    private String overrideReason;

    @Column(name = "previous_payment_status", length = 50)
    private String previousPaymentStatus;

    @Column(name = "new_payment_status", length = 50)
    private String newPaymentStatus;

    @Column(name = "policy_id")
    private Long policyId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
