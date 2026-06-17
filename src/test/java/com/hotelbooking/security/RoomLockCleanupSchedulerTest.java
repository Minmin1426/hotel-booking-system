package com.hotelbooking.security;

import com.hotelbooking.model.Booking;
import com.hotelbooking.model.RoomLock;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.RoomLockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomLockCleanupSchedulerTest {

    @Mock
    private RoomLockRepository roomLockRepository;
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private RoomLockCleanupScheduler scheduler;

    @Test
    void cleanupExpiredRoomLocks_noExpiredLocks_doesNothing() {
        when(roomLockRepository.findByExpiresAtBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        scheduler.cleanupExpiredRoomLocks();

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(roomLockRepository, never()).delete(any(RoomLock.class));
    }

    @Test
    void cleanupExpiredRoomLocks_expiredLocksExist_updatesBookingAndDeletesLock() {
        Booking booking = Booking.builder()
                .bookingId(10L)
                .bookingCode("BK-EXPIRED")
                .status("PENDING")
                .build();

        RoomLock lock = RoomLock.builder()
                .lockId(100L)
                .booking(booking)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(roomLockRepository.findByExpiresAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(lock));

        scheduler.cleanupExpiredRoomLocks();

        verify(bookingRepository).save(booking);
        verify(roomLockRepository).delete(lock);
    }
}
