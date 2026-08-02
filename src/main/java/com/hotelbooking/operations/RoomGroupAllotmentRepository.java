package com.hotelbooking.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomGroupAllotmentRepository extends JpaRepository<RoomGroupAllotment, Long> {
    List<RoomGroupAllotment> findByHotelId(Long hotelId);
}
