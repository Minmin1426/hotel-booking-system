package com.hotelbooking.payment.refund;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long policyId;

    @Column(nullable = false)
    private String name;

    @Column(name = "days_before_checkin", nullable = false)
    private Integer daysBeforeCheckin;

    @Column(name = "refund_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal refundPercentage;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
