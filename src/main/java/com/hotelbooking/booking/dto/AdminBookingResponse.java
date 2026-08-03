package com.hotelbooking.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminBookingResponse(
    Long bookingId,
    String bookingCode,
    String customerEmail,
    String hotelName,
    LocalDateTime checkInDate,
    LocalDateTime checkOutDate,
    BigDecimal totalAmount,
    String status,
    String paymentMethod,
    String paymentStatus,
    LocalDateTime updatedAt,
    String customerName,
    String customerPhone,
    List<String> roomNumbers,
    Integer adults,
    Integer children,
    Integer roomCount
) {}
