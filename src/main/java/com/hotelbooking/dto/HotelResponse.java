package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelResponse {
    private Long hotelId;
    private String name;
    private String location;
    private String description;
    private Boolean isActive;
    private Double rating;
    private Double minPrice;
    private List<HotelImageDTO> images;
}
