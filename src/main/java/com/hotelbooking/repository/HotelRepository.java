package com.hotelbooking.repository;

import com.hotelbooking.model.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {
    Page<Hotel> findByIsActiveTrue(Pageable pageable);
    Page<Hotel> findByLocationContainingAndIsActiveTrue(String location, Pageable pageable);
    Optional<Hotel> findByHotelIdAndIsActiveTrue(Long hotelId);
}
