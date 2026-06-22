package com.hotelbooking.repository;

import com.hotelbooking.model.BookingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRoomRepository extends JpaRepository<BookingRoom, Long> {

    /**
     * UC-26: Thống kê số đêm đặt phòng theo loại phòng trong khoảng thời gian.
     * Trả về [roomType, totalNights, totalBookings, totalRevenue]
     */
    @Query("""
        SELECT br.room.roomType,
               SUM(DATEDIFF(DAY, b.checkInDate, b.checkOutDate)),
               COUNT(DISTINCT b.bookingId),
               SUM(br.priceAtBooking * br.quantity)
        FROM BookingRoom br
        JOIN br.booking b
        WHERE b.status = 'CONFIRMED'
          AND b.checkInDate >= :from
          AND b.checkOutDate <= :to
        GROUP BY br.room.roomType
        ORDER BY br.room.roomType
        """)
    List<Object[]> getRoomUsageStats(@Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);

    /**
     * UC-26: Tổng số phòng hiện có theo loại (để tính tỷ lệ sử dụng).
     */
    @Query("""
        SELECT r.roomType, COUNT(r)
        FROM Room r
        WHERE r.status != 'MAINTENANCE'
        GROUP BY r.roomType
        """)
    List<Object[]> getRoomCountByType();
}
