package com.hotelbooking.controller;

import com.hotelbooking.dto.HotelDetailResponse;
import com.hotelbooking.dto.HotelFilterRequest;
import com.hotelbooking.dto.HotelResponse;
import com.hotelbooking.dto.HotelSearchResponseDTO;
import com.hotelbooking.dto.HotelCreateRequest;
import com.hotelbooking.dto.HotelUpdateRequest;
import com.hotelbooking.dto.ApiResponse;
import com.hotelbooking.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * UC-20: Create a new hotel (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HotelResponse> createHotel(@jakarta.validation.Valid @RequestBody HotelCreateRequest request) {
        log.info("Received admin request to create hotel: {}", request.getName());
        HotelResponse response = hotelService.createHotel(request);
        return ResponseEntity.ok(response);
    }

    /**
     * UC-21: Update hotel details (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HotelResponse> updateHotel(
            @PathVariable("id") Long id,
            @jakarta.validation.Valid @RequestBody HotelUpdateRequest request) {
        log.info("Received admin request to update hotel ID: {}", id);
        HotelResponse response = hotelService.updateHotel(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * UC-27: Delete a hotel (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHotel(@PathVariable("id") Long id) {
        log.info("Received admin request to delete hotel ID: {}", id);
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * UC-24: Upload hotel image (Admin only)
     */
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadHotelImage(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file) throws java.io.IOException {
        log.info("Received admin request to upload image for hotel ID: {}", id);
        String path = hotelService.uploadImage(id, file);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", path));
    }
}
