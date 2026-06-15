package com.hotelbooking.service.impl;

import com.hotelbooking.dto.request.RoomSearchRequest;
import com.hotelbooking.dto.response.RoomAvailabilityResponse;
import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.model.Room;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.service.RoomService;
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
}
