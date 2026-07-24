package com.hotelbooking.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomMatrixRepository extends JpaRepository<RoomMatrixState, Long> {
    List<RoomMatrixState> findByHotelId(Long hotelId);
    List<RoomMatrixState> findByHotelIdAndFloor(Long hotelId, Integer floor);
    List<RoomMatrixState> findByHotelIdAndStatus(Long hotelId, RoomMatrixState.RoomStatus status);
}
