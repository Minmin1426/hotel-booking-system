package com.hotelbooking.room;
import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.room.dto.CreateRoomRequest;
import com.hotelbooking.room.dto.RoomAvailabilityResponse;
import com.hotelbooking.room.dto.RoomSearchRequest;
import com.hotelbooking.room.dto.UpdateRoomRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Create room (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Room>> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        log.info("Received admin request to create room in hotel: {}", request.getHotelId());
        Room room = roomService.createRoom(request);
        return ResponseEntity.ok(ApiResponse.success("Room created successfully", room));
    }

    /**
     * Update room details (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Room>> updateRoom(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateRoomRequest request) {
        log.info("Received admin request to update room ID: {}", id);
        Room room = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success("Room updated successfully", room));
    }

    /**
     * Delete room (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable("id") Long id) {
        log.info("Received admin request to delete room ID: {}", id);
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success("Room deleted successfully", null));
    }

    /**
     * Update room availability (Admin only)
     */
    @PutMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateAvailability(
            @PathVariable("id") Long id,
            @RequestParam("available") boolean available) {
        log.info("Received admin request to update availability of room ID: {} to {}", id, available);
        roomService.updateAvailability(id, available);
        return ResponseEntity.ok(ApiResponse.success("Room availability updated successfully", null));
    }

    /**
     * Get all rooms for a hotel (Admin/Staff only)
     */
    @GetMapping("/hotel/{hotelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<RoomAvailabilityResponse>>> getRoomsByHotelId(@PathVariable Long hotelId) {
        log.info("Received request to get all rooms for hotel ID: {}", hotelId);
        List<RoomAvailabilityResponse> rooms = roomService.getRoomsByHotelId(hotelId);
        return ResponseEntity.ok(ApiResponse.success("Rooms retrieved successfully", rooms));
    }
}
