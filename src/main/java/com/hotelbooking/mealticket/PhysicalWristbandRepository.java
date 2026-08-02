package com.hotelbooking.mealticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhysicalWristbandRepository extends JpaRepository<PhysicalWristband, Long> {
    Optional<PhysicalWristband> findByWristbandCode(String wristbandCode);
    List<PhysicalWristband> findByBookingBookingId(Long bookingId);
    List<PhysicalWristband> findByBookingBookingIdAndStatus(Long bookingId, String status);
    boolean existsByWristbandCode(String wristbandCode);
}
