package com.hotelbooking.booking;
import com.hotelbooking.booking.dto.BookingConfirmResponse;
import com.hotelbooking.booking.dto.BookingHistoryResponse;
import com.hotelbooking.booking.dto.BookingRequest;
import com.hotelbooking.booking.dto.BookingResponse;
import com.hotelbooking.booking.dto.CancelBookingResponse;
import com.hotelbooking.booking.dto.DateValidationRequest;
import com.hotelbooking.booking.dto.DateValidationResponse;
import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.dto.PagedResponse;
import com.hotelbooking.payment.dto.PaymentConfirmRequest;
import com.hotelbooking.room.Room;
import com.hotelbooking.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    // UC-10: Chọn ngày check-in/check-out (validate stay period)
    @PostMapping("/validate-dates")
    public ResponseEntity<ApiResponse<DateValidationResponse>> validateDates(
            @Valid @RequestBody DateValidationRequest request) {
        log.info("UC-10 API: Validating stay period checkIn={}, checkOut={}", request.getCheckInDate(), request.getCheckOutDate());
        DateValidationResponse response = bookingService.validateDates(request.getCheckInDate(), request.getCheckOutDate());
        return ResponseEntity.ok(ApiResponse.success("Date validation completed", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN', 'DIRECTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody BookingRequest request) {
        String email = currentUser != null ? currentUser.getEmail() : "test@example.com";
        log.info("UC-11 API: Creating booking for user: {}", email);
        BookingResponse response = bookingService.createBooking(request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Booking created and room(s) locked successfully", response));
    }

    // UC-33: Tạm giữ phòng (Room Lock) - Gia hạn lock room
    @PutMapping("/{id}/lock/renew")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN', 'DIRECTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Void>> renewLock(
            @PathVariable("id") Long bookingId) {
        log.info("UC-33 API: Renewing room locks for booking ID: {}", bookingId);
        bookingService.renewLock(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Room locks renewed successfully", null));
    }

    // UC-12: Xác nhận booking (payment callback)
    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BookingConfirmResponse>> confirmBooking(
            @Valid @RequestBody PaymentConfirmRequest request) {
        log.info("UC-12 API: Confirming booking for bookingCode={}", request.getBookingCode());
        BookingConfirmResponse response = bookingService.confirmBooking(request);
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed successfully", response));
    }

    // UC-15: Xem lịch sử booking (paging)
    @GetMapping("/my-history")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN', 'DIRECTOR')")
    public ResponseEntity<ApiResponse<PagedResponse<BookingHistoryResponse>>> getMyBookingHistory(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = currentUser != null ? currentUser.getUserId() : 1L;
        log.info("UC-15 API: Retrieving booking history for userId={}, page={}, size={}",
                userId, page, size);
        PagedResponse<BookingHistoryResponse> response = bookingService.getBookingHistory(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Booking history retrieved", response));
    }

    // UC-14: Hủy đặt phòng
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN', 'DIRECTOR')")
    public ResponseEntity<ApiResponse<CancelBookingResponse>> cancelBooking(
            @PathVariable("id") Long bookingId,
            @AuthenticationPrincipal User currentUser) {
        String email = currentUser != null ? currentUser.getEmail() : "test@example.com";
        Long userId = currentUser != null ? currentUser.getUserId() : 1L;
        log.info("UC-14 API: Cancelling booking ID: {} for user: {}", bookingId, email);
        CancelBookingResponse response = bookingService.cancelBooking(bookingId, userId);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }

    // Retrieve single booking by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN', 'DIRECTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @PathVariable("id") Long bookingId) {
        log.info("API: Retrieving booking ID: {}", bookingId);
        BookingResponse response = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", response));
    }
}
