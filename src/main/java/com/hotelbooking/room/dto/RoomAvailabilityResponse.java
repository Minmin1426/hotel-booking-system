package com.hotelbooking.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAvailabilityResponse {
    private Long roomId;
    private String roomNumber;
    private String roomType;
    private BigDecimal pricePerNight;
    private String status;
    private Long hotelId;
    private String hotelName;
}
