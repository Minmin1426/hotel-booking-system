package com.hotelbooking.booking.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingTicketDTO {
    private Long bookingId;
    private String bookingCode;
    private String checkinQrCode;
    private String checkinQrSignature;
    
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String identificationNumber;

    private Long hotelId;
    private String hotelName;
    private String hotelLocation;
    
    private Long roomId;
    private String roomNumber;
    private String roomType;

    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;

    private String status;
    private String paymentStatus;

    private BigDecimal totalPrice;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private String paymentMethod;
    private Boolean isDeposit;
    private Integer depositRatio;

    private String message;
}
