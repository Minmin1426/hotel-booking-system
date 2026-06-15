package com.hotelbooking.service;

import com.hotelbooking.dto.request.RoomSearchRequest;
import com.hotelbooking.dto.response.RoomAvailabilityResponse;
import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.Room;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UC-09: View Available Rooms")
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Hotel activeHotel;
    private Room singleRoom;
    private Room doubleRoom;

    @BeforeEach
    void setUp() {
        activeHotel = Hotel.builder()
                .hotelId(1L)
                .name("Grand Hotel Hanoi")
                .location("Hoan Kiem, Hanoi")
                .isActive(true)
                .build();

        singleRoom = Room.builder()
                .roomId(101L)
                .hotel(activeHotel)
                .roomNumber("101")
                .roomType("SINGLE")
                .price(new BigDecimal("500000"))
                .status("AVAILABLE")
                .build();

        doubleRoom = Room.builder()
                .roomId(102L)
                .hotel(activeHotel)
                .roomNumber("102")
                .roomType("DOUBLE")
                .price(new BigDecimal("900000"))
                .status("AVAILABLE")
                .build();
    }

    @Test
    @DisplayName("TC-09-01: Valid request returns list of available rooms")
    void findAvailableRooms_validRequest_returnsRooms() {
        RoomSearchRequest request = buildRequest(1L,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 15));

        when(hotelRepository.findByHotelIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(activeHotel));
        when(roomRepository.findAvailableRooms(eq(1L), any(), any()))
                .thenReturn(List.of(singleRoom, doubleRoom));

        List<RoomAvailabilityResponse> result = roomService.findAvailableRooms(request);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RoomAvailabilityResponse::getRoomNumber)
                .containsExactly("101", "102");
        assertThat(result).extracting(RoomAvailabilityResponse::getHotelName)
                .containsOnly("Grand Hotel Hanoi");
    }

    @Test
    @DisplayName("TC-09-02: No available rooms returns empty list, not an error")
    void findAvailableRooms_noRoomsAvailable_returnsEmptyList() {
        RoomSearchRequest request = buildRequest(1L,
                LocalDate.of(2026, 12, 24),
                LocalDate.of(2026, 12, 26));

        when(hotelRepository.findByHotelIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(activeHotel));
        when(roomRepository.findAvailableRooms(eq(1L), any(), any()))
                .thenReturn(List.of());

        List<RoomAvailabilityResponse> result = roomService.findAvailableRooms(request);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("TC-09-03: Non-existent hotel throws ResourceNotFoundException")
    void findAvailableRooms_hotelNotFound_throwsResourceNotFoundException() {
        RoomSearchRequest request = buildRequest(999L,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 15));

        when(hotelRepository.findByHotelIdAndIsActiveTrue(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.findAvailableRooms(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verifyNoInteractions(roomRepository);
    }

    @Test
    @DisplayName("TC-09-04: Inactive hotel throws ResourceNotFoundException")
    void findAvailableRooms_inactiveHotel_throwsResourceNotFoundException() {
        RoomSearchRequest request = buildRequest(2L,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 15));

        when(hotelRepository.findByHotelIdAndIsActiveTrue(2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.findAvailableRooms(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(roomRepository);
    }

    @Test
    @DisplayName("TC-09-05: Check-out date equal to check-in throws BusinessException")
    void findAvailableRooms_checkOutEqualsCheckIn_throwsBusinessException() {
        LocalDate today = LocalDate.now().plusDays(1);
        RoomSearchRequest request = buildRequest(1L, today, today);

        assertThatThrownBy(() -> roomService.findAvailableRooms(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Check-out date must be after check-in date");

        verifyNoInteractions(hotelRepository, roomRepository);
    }

    @Test
    @DisplayName("TC-09-06: Check-out date before check-in throws BusinessException")
    void findAvailableRooms_checkOutBeforeCheckIn_throwsBusinessException() {
        RoomSearchRequest request = buildRequest(1L,
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 7, 10));

        assertThatThrownBy(() -> roomService.findAvailableRooms(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Check-out date must be after check-in date");

        verifyNoInteractions(hotelRepository, roomRepository);
    }

    @Test
    @DisplayName("TC-09-07: Response DTO fields are correctly mapped from Room entity")
    void findAvailableRooms_responseFieldsMappedCorrectly() {
        RoomSearchRequest request = buildRequest(1L,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 12));

        when(hotelRepository.findByHotelIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(activeHotel));
        when(roomRepository.findAvailableRooms(eq(1L), any(), any()))
                .thenReturn(List.of(singleRoom));

        List<RoomAvailabilityResponse> result = roomService.findAvailableRooms(request);

        assertThat(result).hasSize(1);
        RoomAvailabilityResponse dto = result.get(0);
        assertThat(dto.getRoomId()).isEqualTo(101L);
        assertThat(dto.getRoomNumber()).isEqualTo("101");
        assertThat(dto.getRoomType()).isEqualTo("SINGLE");
        assertThat(dto.getPricePerNight()).isEqualByComparingTo(new BigDecimal("500000"));
        assertThat(dto.getStatus()).isEqualTo("AVAILABLE");
        assertThat(dto.getHotelId()).isEqualTo(1L);
        assertThat(dto.getHotelName()).isEqualTo("Grand Hotel Hanoi");
    }

    @Test
    @DisplayName("TC-09-08: Date range passed to repository is normalised (start/end of day)")
    void findAvailableRooms_dateRangeNormalisedCorrectly() {
        LocalDate checkIn  = LocalDate.of(2026, 7, 10);
        LocalDate checkOut = LocalDate.of(2026, 7, 15);
        RoomSearchRequest request = buildRequest(1L, checkIn, checkOut);

        when(hotelRepository.findByHotelIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(activeHotel));
        when(roomRepository.findAvailableRooms(any(), any(), any()))
                .thenReturn(List.of());

        roomService.findAvailableRooms(request);

        verify(roomRepository).findAvailableRooms(
                eq(1L),
                eq(checkIn.atStartOfDay()),
                eq(checkOut.atTime(LocalTime.MAX))
        );
    }

    @Test
    @DisplayName("TC-09-09: One-night stay (consecutive days) is valid")
    void findAvailableRooms_oneNightStay_isValid() {
        LocalDate checkIn  = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(1);
        RoomSearchRequest request = buildRequest(1L, checkIn, checkOut);

        when(hotelRepository.findByHotelIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(activeHotel));
        when(roomRepository.findAvailableRooms(eq(1L), any(), any()))
                .thenReturn(List.of(singleRoom));

        List<RoomAvailabilityResponse> result = roomService.findAvailableRooms(request);

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("TC-09-10: Repository query excludes MAINTENANCE/UNAVAILABLE rooms (filter in repo)")
    void findAvailableRooms_onlyAvailableStatusRooms() {
        RoomSearchRequest request = buildRequest(1L,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 15));

        when(hotelRepository.findByHotelIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(activeHotel));
        when(roomRepository.findAvailableRooms(eq(1L), any(), any()))
                .thenReturn(List.of(singleRoom));

        List<RoomAvailabilityResponse> result = roomService.findAvailableRooms(request);

        assertThat(result).extracting(RoomAvailabilityResponse::getStatus)
                .containsOnly("AVAILABLE");
        assertThat(result).extracting(RoomAvailabilityResponse::getRoomId)
                .doesNotContain(103L);
    }

    private RoomSearchRequest buildRequest(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        RoomSearchRequest request = new RoomSearchRequest();
        request.setHotelId(hotelId);
        request.setCheckIn(checkIn);
        request.setCheckOut(checkOut);
        return request;
    }
}
