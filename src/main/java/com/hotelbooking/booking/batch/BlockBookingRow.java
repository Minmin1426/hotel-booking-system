package com.hotelbooking.booking.batch;

import com.hotelbooking.booking.Booking;
import com.hotelbooking.hotel.Hotel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "block_booking_rows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockBookingRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_id")
    private Long rowId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_booking_id", nullable = false)
    private BlockBookingRequest blockBookingRequest;

    @Column(name = "guest_name", length = 100)
    private String guestName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    @Column(name = "room_type", length = 50)
    private String roomType;

    @Column(name = "quantity")
    @Builder.Default
    private Integer quantity = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "row_status", nullable = false, length = 20)
    @Builder.Default
    private RowStatus rowStatus = RowStatus.VALID;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void markInvalid(String message) {
        this.rowStatus = RowStatus.INVALID;
        this.errorMessage = message;
    }

    public void markUnavailable(String message) {
        this.rowStatus = RowStatus.UNAVAILABLE;
        this.errorMessage = message;
    }

    public void markBooked(Booking booking) {
        this.rowStatus = RowStatus.BOOKED;
        this.booking = booking;
    }

    public void markFailed(String message) {
        this.rowStatus = RowStatus.FAILED;
        this.errorMessage = message;
    }
}
