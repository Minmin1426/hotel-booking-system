package com.hotelbooking.booking.dto;
import com.hotelbooking.hotel.Hotel;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotNull(message = "Hotel ID cannot be null")
    private Long hotelId;

    @NotNull(message = "Check-in date cannot be null")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date cannot be null")
    private LocalDate checkOutDate;

    @NotEmpty(message = "At least one room must be selected")
    private List<Long> roomIds;

    private String paymentMethod; // e.g., ONLINE, CASH, BANK_TRANSFER
}
