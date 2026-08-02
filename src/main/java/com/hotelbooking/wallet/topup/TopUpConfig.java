package com.hotelbooking.wallet.topup;

import com.hotelbooking.user.User;
import com.hotelbooking.wallet.Wallet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "topup_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopUpConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    @Column(name = "threshold_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal thresholdAmount;

    @Column(name = "topup_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal topupAmount;

    @Column(name = "payment_method_id", length = 255)
    private String paymentMethodId;

    @Column(name = "max_daily_auto_topup", nullable = false)
    @Builder.Default
    private Integer maxDailyAutoTopup = 5;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
