package com.hotelbooking.restaurant;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.restaurant.dto.RestaurantReservationResponse;
import com.hotelbooking.restaurant.dto.UpdateRestaurantReservationStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurant/reservations")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'RESTAURANT_STAFF', 'RECEPTIONIST', 'DIRECTOR')")
@RequiredArgsConstructor
@Slf4j
public class RestaurantReservationController {

    private final RestaurantReservationService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantReservationResponse>>> getReservations(
            @RequestParam(required = false) String search) {
        log.info("API request to fetch restaurant reservations, search={}", search);
        List<RestaurantReservationResponse> reservations = service.getActiveReservations(search);
        return ResponseEntity.ok(ApiResponse.success("Restaurant reservations retrieved successfully", reservations));
    }

    @PatchMapping("/{resCode}/status")
    public ResponseEntity<ApiResponse<RestaurantReservationResponse>> updateStatus(
            @PathVariable String resCode,
            @Valid @RequestBody UpdateRestaurantReservationStatusRequest request) {
        log.info("API request to update restaurant reservation resCode={} to {}", resCode, request.getStatus());
        RestaurantReservationResponse response = service.updateStatus(resCode, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", response));
    }
}
