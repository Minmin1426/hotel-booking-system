package com.hotelbooking.booking;
import com.hotelbooking.booking.dto.AdminBookingResponse;
import com.hotelbooking.booking.dto.BookingRequest;
import com.hotelbooking.booking.dto.BookingResponse;
import com.hotelbooking.booking.dto.CancelBookingResponse;
import com.hotelbooking.booking.dto.DateValidationResponse;
import com.hotelbooking.booking.dto.UpdateBookingStatusRequest;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.hotel.Hotel;
import com.hotelbooking.hotel.HotelRepository;
import com.hotelbooking.payment.Payment;
import com.hotelbooking.payment.PaymentRepository;
import com.hotelbooking.room.Room;
import com.hotelbooking.room.RoomLock;
import com.hotelbooking.room.RoomLockRepository;
import com.hotelbooking.room.RoomLockService;
import com.hotelbooking.room.RoomRepository;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomLockService roomLockService;
    @Mock
    private RoomLockRepository roomLockRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User testUser;
    private Hotel testHotel;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .fullName("Test User")
                .role("CUSTOMER")
                .status("ACTIVE")
                .build();

        testHotel = Hotel.builder()
                .hotelId(1L)
                .name("Grand Palace")
                .location("Hanoi")
                .isActive(true)
                .build();

        testRoom = Room.builder()
                .roomId(1L)
                .hotel(testHotel)
                .roomType("Deluxe")
                .price(BigDecimal.valueOf(100.00))
                .roomNumber("101")
                .build();
    }

    @Test
    void validateDates_checkInInPast_returnsInvalid() {
        LocalDate past = LocalDate.now().minusDays(1);
        LocalDate future = LocalDate.now().plusDays(2);

        DateValidationResponse response = bookingService.validateDates(past, future);

        assertFalse(response.isValid());
        assertEquals("Check-in date cannot be in the past", response.getMessage());
    }

    @Test
    void validateDates_checkOutBeforeCheckIn_returnsInvalid() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        DateValidationResponse response = bookingService.validateDates(today, yesterday);

        assertFalse(response.isValid());
        assertEquals("Check-out date must be after check-in date", response.getMessage());
    }

    @Test
    void validateDates_sameCheckInAndCheckOut_returnsInvalid() {
        LocalDate today = LocalDate.now();

        DateValidationResponse response = bookingService.validateDates(today, today);

        assertFalse(response.isValid());
        assertEquals("Check-out date must be after check-in date", response.getMessage());
    }

    @Test
    void validateDates_validStays_returnsValidAndNightsCount() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(3);

        DateValidationResponse response = bookingService.validateDates(checkIn, checkOut);

        assertTrue(response.isValid());
        assertEquals(3, response.getNights());
        assertEquals("Dates are valid", response.getMessage());
    }

    @Test
    void createBooking_success() {
        // Arrange
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);
        BookingRequest request = BookingRequest.builder()
                .hotelId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(1L))
                .paymentMethod("ONLINE")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        Booking savedBooking = Booking.builder()
                .bookingId(10L)
                .bookingCode("BK-TEST")
                .user(testUser)
                .hotel(testHotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(200.00))
                .status("PENDING")
                .build();
        BookingRoom br = BookingRoom.builder()
                .booking(savedBooking)
                .room(testRoom)
                .quantity(1)
                .priceAtBooking(testRoom.getPrice())
                .build();
        savedBooking.setBookingRooms(List.of(br));

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        RoomLock mockLock = RoomLock.builder()
                .lockId(100L)
                .room(testRoom)
                .booking(savedBooking)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        when(roomLockService.lockRoomsForBooking(any(Booking.class), anyList())).thenReturn(List.of(mockLock));

        // Act
        BookingResponse response = bookingService.createBooking(request, "test@example.com");

        // Assert
        assertNotNull(response);
        assertEquals("BK-TEST", response.getBookingCode());
        assertEquals("PENDING", response.getStatus());
        assertEquals(BigDecimal.valueOf(200.00), response.getTotalAmount());
        assertEquals(1, response.getRoomIds().size());
        assertEquals(1L, response.getRoomIds().get(0));
        assertNotNull(response.getLockExpiresAt());

        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(roomLockService).lockRoomsForBooking(any(Booking.class), eq(List.of(1L)));
    }

    @Test
    void confirmBooking_success() {
        // Arrange
        Booking booking = Booking.builder()
                .bookingId(10L)
                .bookingCode("BK-TEST")
                .user(testUser)
                .hotel(testHotel)
                .checkInDate(LocalDateTime.now())
                .checkOutDate(LocalDateTime.now().plusDays(2))
                .status("PENDING")
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();
        BookingRoom br = BookingRoom.builder()
                .booking(booking)
                .room(testRoom)
                .priceAtBooking(testRoom.getPrice())
                .build();
        booking.setBookingRooms(List.of(br));

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        // Act
        BookingResponse response = bookingService.confirmBooking(10L);

        // Assert
        assertEquals("CONFIRMED", response.getStatus());
        verify(roomLockService).releaseLocksForBooking(10L);
    }

    @Test
    void confirmBooking_notPending_throwsBusinessException() {
        // Arrange
        Booking booking = Booking.builder()
                .bookingId(10L)
                .status("CONFIRMED")
                .build();

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        // Act & Assert
        assertThrows(BusinessException.class, () -> bookingService.confirmBooking(10L));
    }

    @Test
    void cancelBooking_success() {
        // Arrange
        Booking booking = Booking.builder()
                .bookingId(10L)
                .bookingCode("BK-TEST")
                .user(testUser)
                .status("CONFIRMED")
                .build();

        Payment payment = Payment.builder()
                .paymentId(100L)
                .booking(booking)
                .paymentMethod("ONLINE")
                .status("SUCCESS")
                .amount(BigDecimal.valueOf(200.00))
                .build();

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingBookingId(10L)).thenReturn(List.of(payment));

        // Act
        CancelBookingResponse response = bookingService.cancelBooking(10L, 1L);

        // Assert
        assertEquals("CANCELLED", response.getBookingStatus());
        assertEquals("REFUND_PENDING", response.getRefundStatus());
        assertEquals("REFUND_PENDING", payment.getStatus());
        verify(roomLockService).releaseLocksForBooking(10L);
    }

    @Test
    void processBooking_adminConfirm_success() {
        // Arrange
        Booking booking = Booking.builder()
                .bookingId(10L)
                .bookingCode("BK-TEST")
                .user(testUser)
                .hotel(testHotel)
                .status("PENDING")
                .build();

        Payment payment = Payment.builder()
                .paymentId(100L)
                .booking(booking)
                .paymentMethod("BANK_TRANSFER")
                .status("PENDING")
                .amount(BigDecimal.valueOf(200.00))
                .build();

        UpdateBookingStatusRequest request = new UpdateBookingStatusRequest("CONFIRMED");

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingBookingId(10L)).thenReturn(List.of(payment));

        // Act
        AdminBookingResponse response = bookingService.processBooking(10L, request);

        // Assert
        assertEquals("CONFIRMED", response.status());
        assertEquals("COMPLETED", response.paymentStatus());
        verify(roomLockService).releaseLocksForBooking(10L);
    }
}
