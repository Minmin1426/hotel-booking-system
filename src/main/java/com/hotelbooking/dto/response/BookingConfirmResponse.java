package com.hotelbooking.dto.response;

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
public class BookingConfirmResponse {
    private Long bookingId;
    private String bookingCode;
    private String bookingStatus;    // CONFIRMED
    private String paymentStatus;    // SUCCESS
    private String transactionId;
    private BigDecimal totalAmount;
    private LocalDateTime confirmedAt;
    private String hotelName;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
}
