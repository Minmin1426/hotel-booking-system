package com.hotelbooking.admin;

import com.hotelbooking.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_action_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bulk_action_id")
    private Long bulkActionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "target_user_ids", nullable = false, columnDefinition = "TEXT")
    private String targetUserIds;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
