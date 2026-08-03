package com.hotelbooking.booking.dto;
import com.hotelbooking.hotel.Hotel;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

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

    @NotNull(message = "Adults count cannot be null")
    @Min(value = 1, message = "Adults count must be at least 1")
    private Integer adults;

    @NotNull(message = "Children count cannot be null")
    @Min(value = 0, message = "Children count cannot be negative")
    private Integer children;

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
    public Integer getAdults() { return adults; }
    public void setAdults(Integer adults) { this.adults = adults; }
    public Integer getChildren() { return children; }
    public void setChildren(Integer children) { this.children = children; }
}
