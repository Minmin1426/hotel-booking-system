package com.hotelbooking.room;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomLockCleanupScheduler {

    private final RoomLockRepository roomLockRepository;
    private final BookingRepository bookingRepository;

    // Run every minute (60000 ms) to clean up expired room locks
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void cleanupExpiredRoomLocks() {
        log.debug("Checking for expired room locks...");
        try {
            LocalDateTime now = LocalDateTime.now();
            List<RoomLock> expiredLocks = roomLockRepository.findByExpiresAtBefore(now);
            if (!expiredLocks.isEmpty()) {
                log.info("Found {} expired room locks to clean up", expiredLocks.size());
                for (RoomLock lock : expiredLocks) {
                    Booking booking = lock.getBooking();
                    if ("PENDING".equalsIgnoreCase(booking.getStatus())) {
                        log.info("Booking {} has expired locks. Marking booking status as FAILED.", booking.getBookingCode());
                        booking.setStatus("FAILED");
                        bookingRepository.save(booking);
                    }
                    roomLockRepository.delete(lock);
                }
                log.info("Successfully cleaned up {} expired room locks.", expiredLocks.size());
            }
        } catch (Exception e) {
            log.error("Failed to cleanup expired room locks: {}", e.getMessage(), e);
        }
    }
}
