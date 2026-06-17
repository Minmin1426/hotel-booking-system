package com.hotelbooking.service;

import com.hotelbooking.dto.request.*;
import com.hotelbooking.dto.response.*;

import java.time.LocalDate;

public interface BookingService {
    DateValidationResponse validateDates(LocalDate checkInDate, LocalDate checkOutDate);
    BookingResponse createBooking(BookingRequest request, String currentUserEmail);
    BookingResponse confirmBooking(Long bookingId);
    BookingResponse failBooking(Long bookingId);
    BookingResponse getBookingById(Long bookingId);
    void renewLock(Long bookingId);
    
    // UC-12
    BookingConfirmResponse confirmBooking(PaymentConfirmRequest request);
    
    // UC-15
    PagedResponse<BookingHistoryResponse> getBookingHistory(Long userId, int page, int size);

    // UC-22
    AdminBookingResponse processBooking(Long bookingId, UpdateBookingStatusRequest request);

    // UC-14
    CancelBookingResponse cancelBooking(Long bookingId, Long customerId);

    // Admin List Bookings Query
    PagedResponse<AdminBookingResponse> getAllBookings(int page, int size, String status, String paymentMethod, String search);
}
