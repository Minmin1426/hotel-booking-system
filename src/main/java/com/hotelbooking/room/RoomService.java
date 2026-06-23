package com.hotelbooking.room;
import com.hotelbooking.room.dto.CreateRoomRequest;
import com.hotelbooking.room.dto.RoomAvailabilityResponse;
import com.hotelbooking.room.dto.RoomSearchRequest;
import com.hotelbooking.room.dto.UpdateRoomRequest;

import java.util.List;

public interface RoomService {
    List<RoomAvailabilityResponse> findAvailableRooms(RoomSearchRequest request);
    Room createRoom(CreateRoomRequest request);
    Room updateRoom(Long roomId, UpdateRoomRequest request);
    void deleteRoom(Long roomId);
    void updateAvailability(Long roomId, boolean available);
    List<RoomAvailabilityResponse> getRoomsByHotelId(Long hotelId);
}
