package com.hotelbooking.operations;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ops_room_matrix_states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomMatrixState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hotelId;

    private Long roomId;

    @Column(nullable = false)
    private String roomNumber;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false)
    private String roomType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RoomStatus status = RoomStatus.AVAILABLE;

    private String currentGuestName;
    
    private String groupName;

    private Long assignedBookingId;

    private LocalDateTime lastHousekeepingAt;

    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum RoomStatus {
        AVAILABLE, OCCUPIED, HOUSEKEEPING, MAINTENANCE
    }
}
