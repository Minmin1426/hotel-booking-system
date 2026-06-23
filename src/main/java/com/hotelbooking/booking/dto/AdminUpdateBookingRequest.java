package com.hotelbooking.booking.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateBookingRequest {

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private List<Long> roomIds;

    private String status; // PENDING | CONFIRMED | CANCELLED | COMPLETED | FAILED

    private String paymentMethod; // ONLINE | CASH | BANK_TRANSFER

    private String paymentStatus; // PENDING | SUCCESS | COMPLETED | FAILED | REFUND_PENDING

    private String voucherCode;
}
