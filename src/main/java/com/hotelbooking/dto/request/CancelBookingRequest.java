package com.hotelbooking.dto.request;

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
