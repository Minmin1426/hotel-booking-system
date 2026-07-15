package com.hotelbooking.booking;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.booking.dto.AdminBookingResponse;
import com.hotelbooking.booking.dto.AdminCreateBookingRequest;
import com.hotelbooking.booking.dto.AdminUpdateBookingRequest;
import com.hotelbooking.booking.dto.BookingResponse;
import com.hotelbooking.booking.dto.UpdateBookingStatusRequest;
import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.dto.PagedResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
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

    // Process (Confirm / Reject) manual/offline bookings.
    @PatchMapping("/{bookingId}/status")
    public ResponseEntity<ApiResponse<AdminBookingResponse>> processBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody UpdateBookingStatusRequest request) {

        log.info("UC-22 REST request received to update bookingId={} status to {}", bookingId, request.status());
        AdminBookingResponse response = bookingService.processBooking(bookingId, request);
        return ResponseEntity.ok(ApiResponse.success("Booking processed successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> adminCreateBooking(
            @Valid @RequestBody AdminCreateBookingRequest request) {
        log.info("Admin request received to create booking for userId={}", request.getUserId());
        BookingResponse response = bookingService.adminCreateBooking(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.created("Booking created successfully by Admin", response));
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> adminUpdateBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody AdminUpdateBookingRequest request) {
        log.info("Admin request received to update bookingId={}", bookingId);
        BookingResponse response = bookingService.adminUpdateBooking(bookingId, request);
        return ResponseEntity.ok(ApiResponse.success("Booking updated successfully by Admin", response));
    }

    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminDeleteBooking(
            @PathVariable Long bookingId) {
        log.info("Admin request received to delete bookingId={}", bookingId);
        bookingService.adminDeleteBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking deleted successfully by Admin", null));
    }
}
