package com.hotelbooking.room;
import com.hotelbooking.booking.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomLockService {
    boolean isRoomAvailableForPeriod(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut);
    List<RoomLock> lockRoomsForBooking(Booking booking, List<Long> roomIds);
    void renewLocksForBooking(Long bookingId);
    void releaseLocksForBooking(Long bookingId);
}
