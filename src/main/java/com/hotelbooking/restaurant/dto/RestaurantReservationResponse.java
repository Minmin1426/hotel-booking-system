package com.hotelbooking.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantReservationResponse {
    private String resCode;
    private String guestName;
    private String guestPhone;
    private String pkgTitle;
    private LocalDate date;
    private String time;
    private String holdLimit;
    private Integer guests;
    private BigDecimal price;
    private String status;
    private String notes;
}
