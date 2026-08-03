package com.hotelbooking.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RestaurantReservationRepository extends JpaRepository<RestaurantReservation, Long> {
    Optional<RestaurantReservation> findByResCode(String resCode);

    @Query("SELECT r FROM RestaurantReservation r WHERE " +
           "LOWER(r.resCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.guestName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "r.guestPhone LIKE CONCAT('%', :search, '%')")
    List<RestaurantReservation> searchReservations(@Param("search") String search);
}
