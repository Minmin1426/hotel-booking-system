package com.hotelbooking.hotel.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HotelFilterRequest {
    private String name;
    private String location;
    private Boolean isActive;
    private String sortBy; // price, rating, location
    private String sortDirection; // asc, desc
    private String keyword;
}
