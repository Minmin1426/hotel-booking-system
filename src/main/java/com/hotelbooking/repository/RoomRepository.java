package com.hotelbooking.repository;

import com.hotelbooking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * UC-09: Returns all rooms for a hotel that are:
     *   1. Status = AVAILABLE
     *   2. Not booked in any CONFIRMED or PENDING booking that overlaps the requested date range
     */
    @Query("""
        SELECT r FROM Room r
        WHERE r.hotel.hotelId = :hotelId
          AND r.hotel.isActive = true
          AND r.status = 'AVAILABLE'
          AND r.roomId NOT IN (
              SELECT br.room.roomId
              FROM BookingRoom br
              JOIN br.booking b
              WHERE b.status IN ('CONFIRMED', 'PENDING')
                AND b.checkInDate  < :checkOut
                AND b.checkOutDate > :checkIn
          )
        ORDER BY r.roomType, r.price
    """)
    List<Room> findAvailableRooms(
            @Param("hotelId")  Long hotelId,
            @Param("checkIn")  LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut
    );

    boolean existsByHotel_HotelId(Long hotelId);
}
