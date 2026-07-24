package com.hotelbooking.wallet.topup;

import com.hotelbooking.user.User;
import com.hotelbooking.wallet.Group;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "spending_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "limit_id")
    private Long limitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_user_id", nullable = false)
    private User memberUser;

    @Column(name = "per_transaction_limit", precision = 18, scale = 2)
    private BigDecimal perTransactionLimit; // null = unlimited

    @Column(name = "daily_limit", precision = 18, scale = 2)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", precision = 18, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "effective_from", nullable = false)
    @Builder.Default
    private LocalDate effectiveFrom = LocalDate.now();

    @Column(name = "effective_until")
    private LocalDate effectiveUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean isCurrentlyEffective() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(effectiveFrom) &&
                (effectiveUntil == null || !today.isAfter(effectiveUntil));
    }

    public boolean hasUnlimitedSpending() {
        return perTransactionLimit == null && dailyLimit == null && monthlyLimit == null;
    }
}
