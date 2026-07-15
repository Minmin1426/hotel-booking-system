package com.hotelbooking.booking;
import java.time.LocalDate;

import com.hotelbooking.booking.dto.AdminBookingResponse;
import com.hotelbooking.booking.dto.AdminCreateBookingRequest;
import com.hotelbooking.booking.dto.AdminUpdateBookingRequest;
import com.hotelbooking.booking.dto.BookingConfirmResponse;
import com.hotelbooking.booking.dto.BookingHistoryResponse;
import com.hotelbooking.booking.dto.BookingRequest;
import com.hotelbooking.booking.dto.BookingResponse;
import com.hotelbooking.booking.dto.CancelBookingResponse;
import com.hotelbooking.booking.dto.DateValidationResponse;
import com.hotelbooking.booking.dto.UpdateBookingStatusRequest;
import com.hotelbooking.common.dto.PagedResponse;
import com.hotelbooking.payment.dto.PaymentConfirmRequest;

public interface BookingService {
    DateValidationResponse validateDates(LocalDate checkInDate, LocalDate checkOutDate);
    BookingResponse createBooking(BookingRequest request, String currentUserEmail);
    BookingResponse confirmBooking(Long bookingId);
    BookingResponse failBooking(Long bookingId);
    BookingResponse getBookingById(Long bookingId);
    void renewLock(Long bookingId);
    
    
    BookingConfirmResponse confirmBooking(PaymentConfirmRequest request);
    
    
    PagedResponse<BookingHistoryResponse> getBookingHistory(Long userId, int page, int size);

    
    AdminBookingResponse processBooking(Long bookingId, UpdateBookingStatusRequest request);

    
    CancelBookingResponse cancelBooking(Long bookingId, Long customerId);

    // Admin List Bookings Query
    PagedResponse<AdminBookingResponse> getAllBookings(int page, int size, String status, String paymentMethod, String search);

    BookingResponse adminCreateBooking(AdminCreateBookingRequest request);
    BookingResponse adminUpdateBooking(Long bookingId, AdminUpdateBookingRequest request);
    void adminDeleteBooking(Long bookingId);
}
