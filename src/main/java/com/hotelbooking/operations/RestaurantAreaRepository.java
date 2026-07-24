package com.hotelbooking.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantAreaRepository extends JpaRepository<RestaurantArea, Long> {
    List<RestaurantArea> findByHotelId(Long hotelId);
    List<RestaurantArea> findByStatus(RestaurantArea.AreaStatus status);
}
