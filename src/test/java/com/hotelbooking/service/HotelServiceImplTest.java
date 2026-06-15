package com.hotelbooking.service;

import com.hotelbooking.dto.HotelDetailResponse;
import com.hotelbooking.dto.HotelFilterRequest;
import com.hotelbooking.dto.HotelResponse;
import com.hotelbooking.dto.HotelSearchResponseDTO;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.HotelImage;
import com.hotelbooking.model.Room;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.service.impl.HotelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private Hotel activeHotel;
    private HotelImage hotelImage;

    @BeforeEach
    void setUp() {
        activeHotel = Hotel.builder()
                .hotelId(1L)
                .name("InterContinental Hanoi Landmark72")
                .location("Hanoi, Vietnam")
                .description("Luxury hotel in Hanoi")
                .isActive(true)
                .rating(BigDecimal.valueOf(4.8))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .images(new ArrayList<>())
                .rooms(new ArrayList<>())
                .build();

        hotelImage = HotelImage.builder()
                .imageId(10L)
                .hotel(activeHotel)
                .imageUrl("https://example.com/landmark72.jpg")
                .imageFormat("JPEG")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        activeHotel.getImages().add(hotelImage);
    }

    @Test
    void searchHotels_WithLocationQuery_ReturnsFilteredActiveHotels() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Hotel> hotelPage = new PageImpl<>(List.of(activeHotel), pageable, 1);

        when(hotelRepository.findByLocationContainingAndIsActiveTrue(eq("Hanoi"), any(Pageable.class)))
                .thenReturn(hotelPage);

        Page<HotelSearchResponseDTO> result = hotelService.searchHotels("Hanoi", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        HotelSearchResponseDTO dto = result.getContent().get(0);
        assertThat(dto.getHotelId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("InterContinental Hanoi Landmark72");
        assertThat(dto.getLocation()).isEqualTo("Hanoi, Vietnam");
        assertThat(dto.getImages()).hasSize(1);
        assertThat(dto.getImages().get(0).getImageId()).isEqualTo(10L);
        assertThat(dto.getImages().get(0).getImageUrl()).isEqualTo("https://example.com/landmark72.jpg");

        verify(hotelRepository).findByLocationContainingAndIsActiveTrue(eq("Hanoi"), any(Pageable.class));
    }

    @Test
    void searchHotels_WithNullLocationQuery_ReturnsAllActiveHotels() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Hotel> hotelPage = new PageImpl<>(List.of(activeHotel), pageable, 1);

        when(hotelRepository.findByIsActiveTrue(any(Pageable.class)))
                .thenReturn(hotelPage);

        Page<HotelSearchResponseDTO> result = hotelService.searchHotels(null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(hotelRepository).findByIsActiveTrue(any(Pageable.class));
    }

    @Test
    void searchHotels_WithEmptyLocationQuery_ReturnsAllActiveHotels() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Hotel> hotelPage = new PageImpl<>(List.of(activeHotel), pageable, 1);

        when(hotelRepository.findByIsActiveTrue(any(Pageable.class)))
                .thenReturn(hotelPage);

        Page<HotelSearchResponseDTO> result = hotelService.searchHotels("   ", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(hotelRepository).findByIsActiveTrue(any(Pageable.class));
    }

    @Test
    void getHotels_success_AndSortingByPrice() {
        Room r1 = Room.builder().roomId(101L).hotel(activeHotel).price(BigDecimal.valueOf(150.00)).status("AVAILABLE").build();
        activeHotel.getRooms().add(r1);

        Hotel hotel2 = Hotel.builder()
                .hotelId(2L)
                .name("Sheraton Hanoi")
                .location("Hanoi, Vietnam")
                .description("5 Star Hotel")
                .isActive(true)
                .rating(BigDecimal.valueOf(4.5))
                .images(new ArrayList<>())
                .rooms(new ArrayList<>())
                .build();
        Room r2 = Room.builder().roomId(102L).hotel(hotel2).price(BigDecimal.valueOf(100.00)).status("AVAILABLE").build();
        hotel2.getRooms().add(r2);

        when(hotelRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(activeHotel, hotel2));

        HotelFilterRequest request = new HotelFilterRequest();
        request.setLocation("Hanoi");
        request.setSortBy("price");
        request.setSortDirection("asc");

        List<HotelResponse> result = hotelService.getHotels(request);

        assertThat(result).hasSize(2);
        // hotel2 price (100.0) < activeHotel price (150.0), so hotel2 should be first in asc order
        assertThat(result.get(0).getName()).isEqualTo("Sheraton Hanoi");
        assertThat(result.get(1).getName()).isEqualTo("InterContinental Hanoi Landmark72");
    }

    @Test
    void getHotels_empty_result() {
        when(hotelRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList());

        HotelFilterRequest request = new HotelFilterRequest();
        List<HotelResponse> result = hotelService.getHotels(request);

        assertThat(result).isEmpty();
    }

    @Test
    void getHotels_WithKeyword_ShouldTrimAndNormalizeWhitespace() {
        when(hotelRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(activeHotel));

        HotelFilterRequest request = new HotelFilterRequest();
        request.setKeyword("   InterContinental    Hanoi   ");

        List<HotelResponse> result = hotelService.getHotels(request);

        assertThat(result).hasSize(1);
        assertThat(request.getKeyword()).isEqualTo("InterContinentalHanoi");
    }

    @Test
    void getHotels_WithBlankKeyword_ShouldNormalizeToNull() {
        when(hotelRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(activeHotel));

        HotelFilterRequest request = new HotelFilterRequest();
        request.setKeyword("     ");

        List<HotelResponse> result = hotelService.getHotels(request);

        assertThat(result).hasSize(1);
        assertThat(request.getKeyword()).isNull();
    }

    @Test
    void getHotels_WithNameAndLocation_ShouldTrimAndNormalizeWhitespace() {
        when(hotelRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(activeHotel));

        HotelFilterRequest request = new HotelFilterRequest();
        request.setName("   InterContinental    Landmark   ");
        request.setLocation("   Hanoi    Vietnam   ");

        List<HotelResponse> result = hotelService.getHotels(request);

        assertThat(result).hasSize(1);
        assertThat(request.getName()).isEqualTo("InterContinentalLandmark");
        assertThat(request.getLocation()).isEqualTo("HanoiVietnam");
    }

    @Test
    void getHotels_WithBlankNameAndLocation_ShouldNormalizeToNull() {
        when(hotelRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(activeHotel));

        HotelFilterRequest request = new HotelFilterRequest();
        request.setName("   ");
        request.setLocation("     ");

        List<HotelResponse> result = hotelService.getHotels(request);

        assertThat(result).hasSize(1);
        assertThat(request.getName()).isNull();
        assertThat(request.getLocation()).isNull();
    }

    @Test
    void getHotelDetail_success() {
        when(hotelRepository.findByHotelIdAndIsActiveTrue(eq(1L)))
                .thenReturn(Optional.of(activeHotel));

        HotelDetailResponse response = hotelService.getHotelDetail(1L);

        assertThat(response).isNotNull();
        assertThat(response.getHotelId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("InterContinental Hanoi Landmark72");
        assertThat(response.getRating()).isEqualTo(4.8);
        assertThat(response.getImages()).hasSize(1);
    }

    @Test
    void getHotelDetail_NotFound_ThrowsException() {
        when(hotelRepository.findByHotelIdAndIsActiveTrue(eq(99L)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.getHotelDetail(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Hotel not found or inactive");
    }
}
