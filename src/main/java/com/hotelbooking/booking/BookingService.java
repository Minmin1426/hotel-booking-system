package com.hotelbooking.booking;
import com.hotelbooking.booking.dto.AdminBookingResponse;
import com.hotelbooking.booking.dto.BookingConfirmResponse;
import com.hotelbooking.booking.dto.BookingHistoryResponse;
import com.hotelbooking.booking.dto.BookingRequest;
import com.hotelbooking.booking.dto.BookingResponse;
import com.hotelbooking.booking.dto.CancelBookingResponse;
import com.hotelbooking.booking.dto.DateValidationResponse;
import com.hotelbooking.booking.dto.UpdateBookingStatusRequest;
import com.hotelbooking.common.dto.PagedResponse;
import com.hotelbooking.payment.dto.PaymentConfirmRequest;

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
