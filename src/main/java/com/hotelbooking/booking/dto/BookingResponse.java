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
    private BigDecimal discountAmount;
    private BigDecimal serviceFee;
    private BigDecimal taxes;
    private BigDecimal finalPrice;
    private String voucherCode;
    private Integer adults;
    private Integer children;
    private Boolean isReviewed;
}
