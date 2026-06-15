package com.hotelbooking.controller;

import com.hotelbooking.dto.ApiResponse;
import com.hotelbooking.dto.request.RoomSearchRequest;
import com.hotelbooking.dto.response.RoomAvailabilityResponse;
import com.hotelbooking.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final RoomService roomService;

    /**
     * UC-09: View available rooms (real-time availability)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RoomAvailabilityResponse>>> searchAvailableRooms(
            @Valid @ModelAttribute RoomSearchRequest request) {
        log.info("Received request to search available rooms. HotelId: {}, CheckIn: {}, CheckOut: {}",
                request.getHotelId(), request.getCheckIn(), request.getCheckOut());

        List<RoomAvailabilityResponse> rooms = roomService.findAvailableRooms(request);

        return ResponseEntity.ok(
                ApiResponse.success("Available rooms retrieved successfully", rooms)
        );
    }
}
