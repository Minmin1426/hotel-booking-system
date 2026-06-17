package com.hotelbooking.service;

import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.model.*;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.RoomLockRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.service.impl.RoomLockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomLockServiceImplTest {

    @Mock
    private RoomLockRepository roomLockRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomLockServiceImpl roomLockService;

    private Room testRoom;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testRoom = Room.builder()
                .roomId(1L)
                .roomNumber("101")
                .price(BigDecimal.valueOf(100.00))
                .build();

        testBooking = Booking.builder()
                .bookingId(10L)
                .bookingCode("BK-LOCK-TEST")
                .checkInDate(LocalDateTime.now().plusDays(1))
                .checkOutDate(LocalDateTime.now().plusDays(3))
                .status("PENDING")
                .build();
    }

    @Test
    void isRoomAvailableForPeriod_available_returnsTrue() {
        LocalDateTime checkIn = LocalDateTime.now().plusDays(1);
        LocalDateTime checkOut = checkIn.plusDays(2);

        when(bookingRepository.findConfirmedBookingsOverlapping(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());
        when(roomLockRepository.findActiveLocksOverlapping(eq(1L), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        boolean result = roomLockService.isRoomAvailableForPeriod(1L, checkIn, checkOut);

        assertTrue(result);
    }

    @Test
    void isRoomAvailableForPeriod_confirmedBookingOverlaps_returnsFalse() {
        LocalDateTime checkIn = LocalDateTime.now().plusDays(1);
        LocalDateTime checkOut = checkIn.plusDays(2);

        when(bookingRepository.findConfirmedBookingsOverlapping(eq(1L), any(), any()))
                .thenReturn(List.of(new Booking()));

        boolean result = roomLockService.isRoomAvailableForPeriod(1L, checkIn, checkOut);

        assertFalse(result);
    }

    @Test
    void isRoomAvailableForPeriod_activeLockOverlaps_returnsFalse() {
        LocalDateTime checkIn = LocalDateTime.now().plusDays(1);
        LocalDateTime checkOut = checkIn.plusDays(2);

        when(bookingRepository.findConfirmedBookingsOverlapping(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());
        when(roomLockRepository.findActiveLocksOverlapping(eq(1L), any(), any(), any()))
                .thenReturn(List.of(new RoomLock()));

        boolean result = roomLockService.isRoomAvailableForPeriod(1L, checkIn, checkOut);

        assertFalse(result);
    }

    @Test
    void lockRoomsForBooking_success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(bookingRepository.findConfirmedBookingsOverlapping(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());
        when(roomLockRepository.findActiveLocksOverlapping(eq(1L), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        when(roomLockRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<RoomLock> result = roomLockService.lockRoomsForBooking(testBooking, List.of(1L));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRoom, result.get(0).getRoom());
        assertEquals(testBooking, result.get(0).getBooking());
    }

    @Test
    void lockRoomsForBooking_roomNotAvailable_throwsBusinessException() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(bookingRepository.findConfirmedBookingsOverlapping(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());
        when(roomLockRepository.findActiveLocksOverlapping(eq(1L), any(), any(), any()))
                .thenReturn(List.of(new RoomLock()));

        assertThrows(BusinessException.class, () -> roomLockService.lockRoomsForBooking(testBooking, List.of(1L)));
    }

    @Test
    void renewLocksForBooking_success() {
        RoomLock mockLock = RoomLock.builder()
                .lockId(100L)
                .booking(testBooking)
                .room(testRoom)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(roomLockRepository.findByBookingBookingId(10L)).thenReturn(List.of(mockLock));

        roomLockService.renewLocksForBooking(10L);

        verify(roomLockRepository).saveAll(anyList());
    }

    @Test
    void renewLocksForBooking_noLocksFound_throwsResourceNotFoundException() {
        when(roomLockRepository.findByBookingBookingId(10L)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> roomLockService.renewLocksForBooking(10L));
    }

    @Test
    void renewLocksForBooking_alreadyBooked_throwsBusinessException() {
        RoomLock mockLock = RoomLock.builder()
                .lockId(100L)
                .booking(testBooking)
                .room(testRoom)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(roomLockRepository.findByBookingBookingId(10L)).thenReturn(List.of(mockLock));
        when(bookingRepository.findConfirmedBookingsOverlapping(eq(1L), any(), any()))
                .thenReturn(List.of(Booking.builder().bookingId(11L).status("CONFIRMED").build()));

        assertThrows(BusinessException.class, () -> roomLockService.renewLocksForBooking(10L));
    }

    @Test
    void renewLocksForBooking_lockedByOther_throwsBusinessException() {
        RoomLock mockLock = RoomLock.builder()
                .lockId(100L)
                .booking(testBooking)
                .room(testRoom)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(roomLockRepository.findByBookingBookingId(10L)).thenReturn(List.of(mockLock));
        when(bookingRepository.findConfirmedBookingsOverlapping(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());

        Booking otherBooking = Booking.builder().bookingId(12L).status("PENDING").build();
        RoomLock otherLock = RoomLock.builder()
                .lockId(200L)
                .room(testRoom)
                .booking(otherBooking)
                .build();
        when(roomLockRepository.findActiveLocksOverlapping(eq(1L), any(), any(), any()))
                .thenReturn(List.of(otherLock));

        assertThrows(BusinessException.class, () -> roomLockService.renewLocksForBooking(10L));
    }

    @Test
    void releaseLocksForBooking_success() {
        roomLockService.releaseLocksForBooking(10L);
        verify(roomLockRepository).deleteByBookingBookingId(10L);
    }
}
