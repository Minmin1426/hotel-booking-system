package com.hotelbooking.service;

import com.hotelbooking.dto.request.RoomSearchRequest;
import com.hotelbooking.dto.response.RoomAvailabilityResponse;

import java.util.List;

public interface RoomService {
    List<RoomAvailabilityResponse> findAvailableRooms(RoomSearchRequest request);
}
