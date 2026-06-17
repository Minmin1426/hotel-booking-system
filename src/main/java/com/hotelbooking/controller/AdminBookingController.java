package com.hotelbooking.controller;

import com.hotelbooking.dto.ApiResponse;
import com.hotelbooking.dto.request.UpdateBookingStatusRequest;
import com.hotelbooking.dto.response.AdminBookingResponse;
import com.hotelbooking.dto.response.PagedResponse;
import com.hotelbooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AdminBookingResponse>>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String search) {
        log.info("REST request to get all bookings for admin, page={}, size={}, status={}, paymentMethod={}, search={}", page, size, status, paymentMethod, search);
        PagedResponse<AdminBookingResponse> response = bookingService.getAllBookings(page, size, status, paymentMethod, search);
        return ResponseEntity.ok(ApiResponse.success("All bookings retrieved successfully", response));
    }

    // UC-22: Process (Confirm / Reject) manual/offline bookings.
    @PatchMapping("/{bookingId}/status")
    public ResponseEntity<ApiResponse<AdminBookingResponse>> processBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody UpdateBookingStatusRequest request) {

        log.info("UC-22 REST request received to update bookingId={} status to {}", bookingId, request.status());
        AdminBookingResponse response = bookingService.processBooking(bookingId, request);
        return ResponseEntity.ok(ApiResponse.success("Booking processed successfully", response));
    }
}
