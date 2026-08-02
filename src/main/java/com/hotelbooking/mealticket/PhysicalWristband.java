package com.hotelbooking.mealticket;

import com.hotelbooking.booking.Booking;
import com.hotelbooking.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "physical_wristbands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhysicalWristband {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wristband_id")
    private Long wristbandId;

    @Column(name = "wristband_code", nullable = false, unique = true, length = 50)
    private String wristbandCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // RED (All-Inclusive), BLUE (Breakfast Only), GOLD (VIP Lounge), GREEN (Child Pass)
    @Column(name = "color_code", nullable = false, length = 30)
    @Builder.Default
    private String colorCode = "BLUE";

    @Column(name = "package_name", length = 100)
    private String packageName;

    // ACTIVE, RETURNED, LOST
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_staff_id")
    private User issuedByStaff;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
