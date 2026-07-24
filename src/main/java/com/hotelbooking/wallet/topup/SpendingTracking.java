package com.hotelbooking.wallet.topup;

import com.hotelbooking.user.User;
import com.hotelbooking.wallet.Group;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "spending_tracking",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "group_id", "period_type", "period_start"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tracking_id")
    private Long trackingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "period_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "total_spent", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;

    public void addSpending(BigDecimal amount) {
        this.totalSpent = this.totalSpent.add(amount);
    }
}
