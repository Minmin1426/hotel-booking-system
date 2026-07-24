package com.hotelbooking.operations;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ops_room_group_allotments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomGroupAllotment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private String roomType;

    @Column(nullable = false)
    private Integer totalRoomsAvailable;

    @Column(nullable = false)
    private Integer maxGroupQuota;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentAllocatedCount = 0;

    private BigDecimal groupBasePrice;

    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
