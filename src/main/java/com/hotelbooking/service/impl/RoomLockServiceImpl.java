package com.hotelbooking.service.impl;

import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.model.Booking;
import com.hotelbooking.model.Room;
import com.hotelbooking.model.RoomLock;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.RoomLockRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.service.RoomLockService;
import com.hotelbooking.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoomLockServiceImpl implements RoomLockService {

    private final RoomLockRepository roomLockRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final SystemSettingService systemSettingService;

    @Override
    @Transactional(readOnly = true)
    public boolean isRoomAvailableForPeriod(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        log.debug("Checking availability for room: {} from {} to {}", roomId, checkIn, checkOut);
        
        // 1. Check for overlapping confirmed bookings
        var confirmedBookings = bookingRepository.findConfirmedBookingsOverlapping(roomId, checkIn, checkOut);
        if (!confirmedBookings.isEmpty()) {
            log.debug("Room {} has {} overlapping confirmed booking(s)", roomId, confirmedBookings.size());
            return false;
        }

        // 2. Check for overlapping active locks
        var activeLocks = roomLockRepository.findActiveLocksOverlapping(roomId, checkIn, checkOut, LocalDateTime.now());
        if (!activeLocks.isEmpty()) {
            log.debug("Room {} has {} overlapping active lock(s)", roomId, activeLocks.size());
            return false;
        }

        return true;
    }

    @Override
    public List<RoomLock> lockRoomsForBooking(Booking booking, List<Long> roomIds) {
        log.info("Attempting to lock rooms {} for booking: {}", roomIds, booking.getBookingCode());
        
        List<RoomLock> locks = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(systemSettingService.getLockDurationMinutes());

        for (Long roomId : roomIds) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

            // Check availability for the stay period
            if (!isRoomAvailableForPeriod(roomId, booking.getCheckInDate(), booking.getCheckOutDate())) {
                log.warn("Failed to lock room number: {} (ID: {}) - not available", room.getRoomNumber(), roomId);
                throw new BusinessException(String.format("Room %s is already locked or booked for the selected dates", room.getRoomNumber()));
            }

            RoomLock lock = RoomLock.builder()
                    .room(room)
                    .booking(booking)
                    .lockedAt(now)
                    .expiresAt(expiresAt)
                    .build();
            
            locks.add(lock);
        }

        List<RoomLock> savedLocks = roomLockRepository.saveAll(locks);
        log.info("Successfully locked {} room(s) for booking: {} until {}", savedLocks.size(), booking.getBookingCode(), expiresAt);
        return savedLocks;
    }

    @Override
    public void renewLocksForBooking(Long bookingId) {
        log.info("Renewing locks for booking ID: {}", bookingId);
        List<RoomLock> locks = roomLockRepository.findByBookingBookingId(bookingId);
        if (locks.isEmpty()) {
            log.warn("No active locks found to renew for booking ID: {}", bookingId);
            throw new ResourceNotFoundException("No locks found for booking ID: " + bookingId);
        }

        // Validate that rooms are still available for this booking (checking against other transactions)
        Booking booking = locks.get(0).getBooking();
        for (RoomLock lock : locks) {
            Long roomId = lock.getRoom().getRoomId();
            
            // 1. Check other confirmed bookings
            var confirmed = bookingRepository.findConfirmedBookingsOverlapping(roomId, booking.getCheckInDate(), booking.getCheckOutDate());
            if (!confirmed.isEmpty()) {
                throw new BusinessException(String.format("Room %s is already booked for the selected dates", lock.getRoom().getRoomNumber()));
            }
            
            // 2. Check other active locks
            var activeLocks = roomLockRepository.findActiveLocksOverlapping(roomId, booking.getCheckInDate(), booking.getCheckOutDate(), LocalDateTime.now());
            for (RoomLock otherLock : activeLocks) {
                if (!otherLock.getBooking().getBookingId().equals(bookingId)) {
                    throw new BusinessException(String.format("Room %s is locked by another transaction", lock.getRoom().getRoomNumber()));
                }
            }
        }

        LocalDateTime newExpiresAt = LocalDateTime.now().plusMinutes(systemSettingService.getLockDurationMinutes());
        for (RoomLock lock : locks) {
            lock.setExpiresAt(newExpiresAt);
        }
        roomLockRepository.saveAll(locks);
        log.info("Successfully renewed {} lock(s) for booking ID: {} until {}", locks.size(), bookingId, newExpiresAt);
    }

    @Override
    public void releaseLocksForBooking(Long bookingId) {
        log.info("Releasing locks for booking ID: {}", bookingId);
        roomLockRepository.deleteByBookingBookingId(bookingId);
        log.info("Locks released for booking ID: {}", bookingId);
    }
}
