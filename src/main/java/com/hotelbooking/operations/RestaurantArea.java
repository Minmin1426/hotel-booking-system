package com.hotelbooking.operations;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ops_restaurant_areas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private String areaName;

    @Column(nullable = false)
    private Integer seatingCapacity;

    @Column(nullable = false)
    private Integer tableCount;

    private Integer kitchenCapacity;

    private String foodSafetyCertUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AreaStatus status = AreaStatus.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum AreaStatus {
        ACTIVE, INACTIVE, MAINTENANCE, PENDING_APPROVAL
    }
}
