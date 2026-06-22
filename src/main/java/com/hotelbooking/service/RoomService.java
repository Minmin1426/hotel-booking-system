package com.hotelbooking.service;

import com.hotelbooking.dto.CreateRoomRequest;
import com.hotelbooking.dto.UpdateRoomRequest;
import com.hotelbooking.dto.request.RoomSearchRequest;
import com.hotelbooking.dto.response.RoomAvailabilityResponse;
import com.hotelbooking.model.Room;

import java.util.List;

public interface RoomService {
    List<RoomAvailabilityResponse> findAvailableRooms(RoomSearchRequest request);
    Room createRoom(CreateRoomRequest request);
    Room updateRoom(Long roomId, UpdateRoomRequest request);
    void deleteRoom(Long roomId);
    void updateAvailability(Long roomId, boolean available);
}
