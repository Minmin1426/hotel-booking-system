package com.hotelbooking.room;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.hotel.Hotel;
import com.hotelbooking.hotel.HotelRepository;
import com.hotelbooking.room.dto.CreateRoomRequest;
import com.hotelbooking.room.dto.RoomAvailabilityResponse;
import com.hotelbooking.room.dto.RoomSearchRequest;
import com.hotelbooking.room.dto.UpdateRoomRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoomAvailabilityResponse> findAvailableRooms(RoomSearchRequest request) {
        log.info("Searching available rooms for hotelId={}, checkIn={}, checkOut={}",
                request.getHotelId(), request.getCheckIn(), request.getCheckOut());

        // Business rule: check-out must be after check-in
        if (!request.getCheckOut().isAfter(request.getCheckIn())) {
            throw new BusinessException("Check-out date must be after check-in date");
        }

        // Validate hotel exists and is active
        hotelRepository.findByHotelIdAndIsActiveTrue(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel not found or inactive: " + request.getHotelId()));

        // Normalise dates to start/end of day for full-day booking semantics
        LocalDateTime checkIn  = request.getCheckIn().atStartOfDay();
        LocalDateTime checkOut = request.getCheckOut().atTime(LocalTime.MAX);

        List<Room> availableRooms = roomRepository.findAvailableRooms(
                request.getHotelId(), checkIn, checkOut);

        log.info("Found {} available rooms for hotelId={}", availableRooms.size(), request.getHotelId());

        return availableRooms.stream()
                .map(this::toResponse)
                .toList();
    }

    private RoomAvailabilityResponse toResponse(Room room) {
        return RoomAvailabilityResponse.builder()
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .pricePerNight(room.getPrice())
                .status(room.getStatus())
                .hotelId(room.getHotel().getHotelId())
                .hotelName(room.getHotel().getName())
                .build();
    }

    @Override
    @Transactional
    public Room createRoom(CreateRoomRequest request) {
        log.info("Creating room: {} for hotelId: {}", request.getRoomNumber(), request.getHotelId());
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", request.getHotelId().toString()));

        boolean existed = roomRepository.existsByHotel_HotelIdAndRoomNumber(hotel.getHotelId(), request.getRoomNumber());
        if (existed) {
            throw new BusinessException("Room number already exists");
        }

        Room room = Room.builder()
                .hotel(hotel)
                .roomNumber(request.getRoomNumber())
                .price(request.getPrice())
                .roomType(request.getRoomType())
                .status("AVAILABLE")
                .build();

        return roomRepository.save(room);
    }

    @Override
    @Transactional
    public Room updateRoom(Long roomId, UpdateRoomRequest request) {
        log.info("Updating room ID: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId.toString()));

        room.setPrice(request.getPrice());
        if (request.getRoomType() != null) {
            room.setRoomType(request.getRoomType());
        }

        return roomRepository.save(room);
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
        log.info("Deleting room ID: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId.toString()));

        boolean activeBooking = bookingRepository.existsByRoomIdAndStatusIn(roomId, List.of("CONFIRMED", "PENDING"));
        if (activeBooking) {
            throw new BusinessException("Room has active booking");
        }

        room.setStatus("UNAVAILABLE");
        roomRepository.save(room);
    }

    @Override
    @Transactional
    public void updateAvailability(Long roomId, boolean available) {
        log.info("Updating availability for room ID: {}, available: {}", roomId, available);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId.toString()));

        room.setStatus(available ? "AVAILABLE" : "UNAVAILABLE");
        roomRepository.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomAvailabilityResponse> getRoomsByHotelId(Long hotelId) {
        log.info("Fetching all rooms for hotel ID: {}", hotelId);
        return roomRepository.findByHotel_HotelId(hotelId).stream()
                .map(this::toResponse)
                .toList();
    }
}
