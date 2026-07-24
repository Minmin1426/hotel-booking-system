package com.hotelbooking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSummaryDto {
    private Long bookingId;
    private String status;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String hotelName;
    private BigDecimal totalPrice;
    private String createdAt;
}
