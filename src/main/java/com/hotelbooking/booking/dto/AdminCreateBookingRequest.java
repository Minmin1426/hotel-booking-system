package com.hotelbooking.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateBookingRequest {

    @NotNull(message = "User ID cannot be null")
    @Min(value = 1, message = "User ID must be a positive integer")
    private Long userId;

    @NotNull(message = "Hotel ID cannot be null")
    @Min(value = 1, message = "Hotel ID must be a positive integer")
    private Long hotelId;

    @NotNull(message = "Check-in date cannot be null")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date cannot be null")
    private LocalDate checkOutDate;

    @NotEmpty(message = "At least one room must be selected")
    private List<@Min(value = 1, message = "Room ID must be a positive integer") Long> roomIds;

    @Pattern(regexp = "^(?i)(CASH|VNPAY|CARD|STRIPE|ONLINE)$", message = "Payment method must be CASH, VNPAY, or CARD")
    private String paymentMethod; // e.g., CASH, VNPAY, CARD

    private String voucherCode;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public List<Long> getRoomIds() { return roomIds; }
    public void setRoomIds(List<Long> roomIds) { this.roomIds = roomIds; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
}
