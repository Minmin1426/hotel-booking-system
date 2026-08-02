package com.hotelbooking.loyalty;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tier_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tier_id")
    private Long tierId;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // BRONZE, SILVER, GOLD, PLATINUM, *_BUSINESS

    @Column(name = "account_type", nullable = false, length = 30)
    private String accountType; // "CUSTOMER" | "CORPORATE_MEMBER"

    @Column(name = "min_annual_spend", nullable = false, precision = 18, scale = 2)
    private BigDecimal minAnnualSpend;

    @Column(name = "point_multiplier", nullable = false, precision = 3, scale = 2)
    private BigDecimal pointMultiplier;

    @Column(name = "max_spending_limit", precision = 18, scale = 2)
    private BigDecimal maxSpendingLimit;

    @Column(name = "priority_support", nullable = false)
    @Builder.Default
    private Boolean prioritySupport = false;

    @Column(name = "exclusive_voucher_access", nullable = false)
    @Builder.Default
    private Boolean exclusiveVoucherAccess = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
