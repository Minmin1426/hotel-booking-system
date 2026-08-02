package com.hotelbooking.booking.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockBookingRowDto {
    private Long rowId;
    private String guestName;
    private String email;
    private String phoneNumber;
    private Long hotelId;
    private String hotelName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String roomType;
    private Integer quantity;
    private Long bookingId;
    private String rowStatus;
    private String errorMessage;
}
