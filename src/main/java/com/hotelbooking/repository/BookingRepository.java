package com.hotelbooking.repository;

import com.hotelbooking.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b JOIN b.bookingRooms br WHERE br.room.roomId = :roomId " +
           "AND b.status = 'CONFIRMED' " +
           "AND b.checkInDate < :checkOut " +
           "AND b.checkOutDate > :checkIn")
    List<Booking> findConfirmedBookingsOverlapping(@Param("roomId") Long roomId,
                                                  @Param("checkIn") LocalDateTime checkIn,
                                                  @Param("checkOut") LocalDateTime checkOut);

    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId ORDER BY b.createdAt DESC")
    Page<Booking> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    Optional<Booking> findByBookingCode(String bookingCode);

    boolean existsByBookingCode(String bookingCode);

    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN b.payments p " +
           "WHERE (:status IS NULL OR b.status = :status) " +
           "AND (:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod) " +
           "AND (:search IS NULL OR LOWER(b.bookingCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "    OR LOWER(b.user.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "    OR LOWER(b.hotel.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Booking> findAllWithFilters(@Param("status") String status,
                                     @Param("paymentMethod") String paymentMethod,
                                     @Param("search") String search,
                                     Pageable pageable);
}
