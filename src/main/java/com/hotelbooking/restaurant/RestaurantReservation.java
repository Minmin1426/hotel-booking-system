package com.hotelbooking.restaurant;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "restaurant_reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "res_code", nullable = false, unique = true, length = 20)
    private String resCode;

    @Column(name = "guest_name", nullable = false, length = 100)
    private String guestName;

    @Column(name = "guest_phone", nullable = false, length = 20)
    private String guestPhone;

    @Column(name = "pkg_title", nullable = false, length = 100)
    private String pkgTitle;

    @Column(name = "res_date", nullable = false)
    private LocalDate resDate;

    @Column(name = "res_time", nullable = false, length = 10)
    private String resTime;

    @Column(name = "hold_limit", nullable = false, length = 10)
    private String holdLimit;

    @Column(nullable = false)
    private Integer guests;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 20)
    private String status; // HOLDING | ARRIVED | RELEASED | PAID

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
