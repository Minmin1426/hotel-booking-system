package com.hotelbooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HotelRevenueDto {
    private Long hotelId;
    private String hotelName;
    private BigDecimal revenue;
    private Long bookingCount;
}
