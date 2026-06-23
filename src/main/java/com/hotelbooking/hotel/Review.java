package com.hotelbooking.hotel;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.user.User;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private Integer rating; // 1–5

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    /**
     * UC-31: Trạng thái kiểm duyệt — VISIBLE | HIDDEN
     */
    @Column(nullable = false)
    @Builder.Default
    private String status = "VISIBLE";

    @Column(name = "moderated_by")
    private Long moderatedBy; // userId của admin

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    @Column(name = "moderation_reason", length = 500)
    private String moderationReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
