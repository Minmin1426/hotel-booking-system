package com.hotelbooking.controller;

import com.hotelbooking.dto.HotelDetailResponse;
import com.hotelbooking.dto.HotelFilterRequest;
import com.hotelbooking.dto.HotelResponse;
import com.hotelbooking.dto.HotelSearchResponseDTO;
import com.hotelbooking.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;

    /**
     * UC-06: Search hotels by location (paginated)
     */
    @GetMapping("/search")
    public ResponseEntity<Page<HotelSearchResponseDTO>> searchHotels(
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Received hotel search request. Location: {}, Page: {}, Size: {}", location, page, size);

        // Enforce BR-03: Maximum of 20 records per page
        int clampedSize = size;
        if (clampedSize > 20) {
            clampedSize = 20;
        } else if (clampedSize <= 0) {
            clampedSize = 20;
        }

        int clampedPage = page;
        if (clampedPage < 0) {
            clampedPage = 0;
        }

        Pageable pageable = PageRequest.of(clampedPage, clampedSize);
        Page<HotelSearchResponseDTO> response = hotelService.searchHotels(location, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * UC-07: Filter hotels by criteria with sorting options
     */
    @GetMapping
    public ResponseEntity<List<HotelResponse>> getHotels(HotelFilterRequest request) {
        log.info("Received request to filter hotels. Name: {}, Location: {}, SortBy: {}", 
                request.getName(), request.getLocation(), request.getSortBy());
        List<HotelResponse> response = hotelService.getHotels(request);
        return ResponseEntity.ok(response);
    }

    /**
     * UC-08: Get detailed hotel information
     */
    @GetMapping("/{id}")
    public ResponseEntity<HotelDetailResponse> getHotelDetail(@PathVariable("id") Long id) {
        log.info("Received request for hotel detail. Hotel ID: {}", id);
        HotelDetailResponse response = hotelService.getHotelDetail(id);
        return ResponseEntity.ok(response);
    }
}
