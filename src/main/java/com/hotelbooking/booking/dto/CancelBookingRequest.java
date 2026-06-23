package com.hotelbooking.booking.dto;
import com.hotelbooking.booking.Booking;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelBookingRequest {

    @NotNull(message = "Booking ID cannot be null")
    private Long bookingId;

    private String cancellationReason;
}
