package com.hotelbooking.wallet.topup;

import com.hotelbooking.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "spending_limit_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingLimitHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "limit_id", nullable = false)
    private SpendingLimit spendingLimit;

    @Column(name = "previous_limit", precision = 18, scale = 2)
    private BigDecimal previousLimit;

    @Column(name = "new_limit", precision = 18, scale = 2)
    private BigDecimal newLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
