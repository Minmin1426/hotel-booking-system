package com.hotelbooking.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingHistoryResponse {
    private Long bookingId;
    private String bookingCode;
    private String hotelName;
    private String hotelLocation;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private BigDecimal totalAmount;
    private String status;          // PENDING | CONFIRMED | CANCELLED | COMPLETED
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
}
