package com.hotelbooking.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateBookingRequest {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Hotel ID cannot be null")
    private Long hotelId;

    @NotNull(message = "Check-in date cannot be null")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date cannot be null")
    private LocalDate checkOutDate;

    @NotEmpty(message = "At least one room must be selected")
    private List<Long> roomIds;

    private String paymentMethod; // e.g., ONLINE, CASH, BANK_TRANSFER

    private String voucherCode;
}
