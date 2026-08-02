package com.hotelbooking.hotel;
import com.hotelbooking.hotel.dto.HotelCreateRequest;
import com.hotelbooking.hotel.dto.HotelDetailResponse;
import com.hotelbooking.hotel.dto.HotelFilterRequest;
import com.hotelbooking.hotel.dto.HotelResponse;
import com.hotelbooking.hotel.dto.HotelSearchResponseDTO;
import com.hotelbooking.hotel.dto.HotelUpdateRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HotelService {
    Page<HotelSearchResponseDTO> searchHotels(String location, Pageable pageable);
    List<HotelResponse> getHotels(HotelFilterRequest request);
    HotelDetailResponse getHotelDetail(Long hotelId);
    HotelResponse createHotel(HotelCreateRequest request);
    HotelResponse updateHotel(Long id, HotelUpdateRequest request);
    void deleteHotel(Long id);
    String uploadImage(Long hotelId, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException;
}
