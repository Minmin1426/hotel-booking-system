package com.hotelbooking.repository;

import com.hotelbooking.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // UC-31: lấy tất cả reviews, filter theo status
    @Query("SELECT r FROM Review r WHERE (:status IS NULL OR r.status = :status) ORDER BY r.createdAt DESC")
    Page<Review> findAllByStatus(@Param("status") String status, Pageable pageable);

    // UC-26: đếm bookings theo hotel và khoảng thời gian (room usage)
    @Query("SELECT COUNT(r) FROM Review r WHERE r.hotel.hotelId = :hotelId")
    long countByHotelId(@Param("hotelId") Long hotelId);
}
