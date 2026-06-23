package com.hotelbooking.booking.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long bookingId;
    private String bookingCode;
    private Long userId;
    private Long hotelId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalAmount;
    private String status;
    private List<Long> roomIds;
    private LocalDateTime lockExpiresAt;
}
