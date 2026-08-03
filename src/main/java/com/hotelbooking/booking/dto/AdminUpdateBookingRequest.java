package com.hotelbooking.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
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

    private List<@Min(value = 1, message = "Room ID must be a positive integer") Long> roomIds;

    private String status; // PENDING | CONFIRMED | CANCELLED | COMPLETED | FAILED

    @Pattern(regexp = "^(?i)(CASH|VNPAY|CARD|STRIPE|ONLINE)$", message = "Payment method must be CASH, VNPAY, or CARD")
    private String paymentMethod; // CASH | VNPAY | CARD

    private String paymentStatus; // PENDING | SUCCESS | COMPLETED | FAILED | REFUND_PENDING

    private String voucherCode;

    private String paymentUpdateReason;

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public List<Long> getRoomIds() { return roomIds; }
    public void setRoomIds(List<Long> roomIds) { this.roomIds = roomIds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
    public String getPaymentUpdateReason() { return paymentUpdateReason; }
    public void setPaymentUpdateReason(String paymentUpdateReason) { this.paymentUpdateReason = paymentUpdateReason; }
}
