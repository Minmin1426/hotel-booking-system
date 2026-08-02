package com.hotelbooking.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomLockRepository extends JpaRepository<RoomLock, Long> {

    @Query("SELECT l FROM RoomLock l JOIN l.booking b WHERE l.room.roomId = :roomId " +
           "AND l.expiresAt > :now " +
           "AND b.checkInDate < :checkOut " +
           "AND b.checkOutDate > :checkIn")
    List<RoomLock> findActiveLocksOverlapping(@Param("roomId") Long roomId,
                                             @Param("checkIn") LocalDateTime checkIn,
                                             @Param("checkOut") LocalDateTime checkOut,
                                             @Param("now") LocalDateTime now);

    List<RoomLock> findByBookingBookingId(Long bookingId);

    List<RoomLock> findByExpiresAtBefore(LocalDateTime time);

    @Modifying
    @Query("DELETE FROM RoomLock r WHERE r.booking.bookingId = :bookingId")
    void deleteByBookingBookingId(@Param("bookingId") Long bookingId);
}
