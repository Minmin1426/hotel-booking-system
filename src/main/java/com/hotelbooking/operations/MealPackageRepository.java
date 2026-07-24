package com.hotelbooking.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealPackageRepository extends JpaRepository<MealPackage, Long> {
    List<MealPackage> findByHotelId(Long hotelId);
    List<MealPackage> findByHotelIdAndIsActiveTrue(Long hotelId);
}
