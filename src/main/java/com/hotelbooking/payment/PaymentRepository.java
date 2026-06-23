package com.hotelbooking.payment;
import com.hotelbooking.report.dto.HotelRevenueDto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBookingBookingId(Long bookingId);

    Optional<Payment> findByBooking_BookingId(Long bookingId);

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByTransactionId(String transactionId);

    List<Payment> findByRefundStatusAndRefundRetryCountLessThan(String status, Integer maxRetry);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.status = 'SUCCESS' AND p.createdAt >= :startDate AND p.createdAt <= :endDate")
    BigDecimal sumRevenue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT new com.hotelbooking.report.dto.HotelRevenueDto(" +
           "h.hotelId, h.name, COALESCE(SUM(p.amount), 0), COUNT(DISTINCT b.bookingId)) " +
           "FROM Payment p " +
           "JOIN p.booking b " +
           "JOIN b.hotel h " +
           "WHERE p.status = 'SUCCESS' AND p.createdAt >= :startDate AND p.createdAt <= :endDate " +
           "GROUP BY h.hotelId, h.name " +
           "ORDER BY COALESCE(SUM(p.amount), 0) DESC")
    List<HotelRevenueDto> findRevenueByHotel(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT CAST(p.createdAt AS LocalDate), COALESCE(SUM(p.amount), 0), COUNT(DISTINCT b.bookingId) " +
           "FROM Payment p " +
           "JOIN p.booking b " +
           "WHERE p.status = 'SUCCESS' AND p.createdAt >= :startDate AND p.createdAt <= :endDate " +
           "GROUP BY CAST(p.createdAt AS LocalDate) " +
           "ORDER BY CAST(p.createdAt AS LocalDate) ASC")
    List<Object[]> findDailyRevenue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
