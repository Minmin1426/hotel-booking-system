package com.hotelbooking.loyalty;

import com.hotelbooking.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tier_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "previous_tier", length = 50)
    private String previousTier;

    @Column(name = "new_tier", nullable = false, length = 50)
    private String newTier;

    @Column(nullable = false, length = 50)
    private String reason; // AUTO_PROMOTION | AUTO_DEMOTION | ADMIN_ADJUSTMENT

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
