package com.hotelbooking.service;

import com.hotelbooking.dto.HotelDetailResponse;
import com.hotelbooking.dto.HotelFilterRequest;
import com.hotelbooking.dto.HotelResponse;
import com.hotelbooking.dto.HotelSearchResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HotelService {
    Page<HotelSearchResponseDTO> searchHotels(String location, Pageable pageable);
    List<HotelResponse> getHotels(HotelFilterRequest request);
    HotelDetailResponse getHotelDetail(Long hotelId);
}
