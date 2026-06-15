package com.hotelbooking.service.impl;

import com.hotelbooking.dto.HotelDetailResponse;
import com.hotelbooking.dto.HotelFilterRequest;
import com.hotelbooking.dto.HotelImageDTO;
import com.hotelbooking.dto.HotelResponse;
import com.hotelbooking.dto.HotelSearchResponseDTO;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.Room;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.HotelSpecification;
import com.hotelbooking.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<HotelSearchResponseDTO> searchHotels(String location, Pageable pageable) {
        log.info("Searching hotels. Location: {}, Pageable: {}", location, pageable);
        Page<Hotel> hotels;
        if (location == null || location.trim().isEmpty()) {
            hotels = hotelRepository.findByIsActiveTrue(pageable);
        } else {
            hotels = hotelRepository.findByLocationContainingAndIsActiveTrue(location.trim(), pageable);
        }
        return hotels.map(this::convertToSearchDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponse> getHotels(HotelFilterRequest request) {
        log.info("Filtering hotels with criteria");
        Specification<Hotel> specification = HotelSpecification.filter(request);
        List<Hotel> hotels = hotelRepository.findAll(specification);

        List<HotelResponse> responses = hotels.stream()
                .map(this::convertToFilterResponse)
                .collect(Collectors.toCollection(ArrayList::new));

        // Sorting logic
        if (request.getSortBy() != null && !request.getSortBy().isBlank()) {
            boolean asc = request.getSortDirection() == null || !"desc".equalsIgnoreCase(request.getSortDirection());
            Comparator<HotelResponse> comparator = null;

            if ("price".equalsIgnoreCase(request.getSortBy())) {
                comparator = Comparator.comparing(
                    h -> h.getMinPrice() != null ? h.getMinPrice() : Double.MAX_VALUE
                );
            } else if ("rating".equalsIgnoreCase(request.getSortBy())) {
                comparator = Comparator.comparing(
                    h -> h.getRating() != null ? h.getRating() : 0.0
                );
            } else if ("location".equalsIgnoreCase(request.getSortBy()) || "distance".equalsIgnoreCase(request.getSortBy())) {
                comparator = Comparator.comparing(
                    h -> h.getLocation() != null ? h.getLocation() : ""
                );
            }

            if (comparator != null) {
                if (!asc) {
                    comparator = comparator.reversed();
                }
                responses.sort(comparator);
            }
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public HotelDetailResponse getHotelDetail(Long hotelId) {
        log.info("Fetching hotel details for hotelId: {}", hotelId);
        Hotel hotel = hotelRepository.findByHotelIdAndIsActiveTrue(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found or inactive: " + hotelId));

        return HotelDetailResponse.builder()
                .hotelId(hotel.getHotelId())
                .name(hotel.getName())
                .location(hotel.getLocation())
                .description(hotel.getDescription())
                .rating(hotel.getRating() != null ? hotel.getRating().doubleValue() : 0.0)
                .images(hotel.getImages().stream()
                        .map(img -> HotelImageDTO.builder()
                                .imageId(img.getImageId())
                                .imageUrl(img.getImageUrl())
                                .imageFormat(img.getImageFormat())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private HotelSearchResponseDTO convertToSearchDTO(Hotel hotel) {
        return HotelSearchResponseDTO.builder()
                .hotelId(hotel.getHotelId())
                .name(hotel.getName())
                .location(hotel.getLocation())
                .description(hotel.getDescription())
                .images(hotel.getImages().stream()
                        .map(img -> HotelImageDTO.builder()
                                .imageId(img.getImageId())
                                .imageUrl(img.getImageUrl())
                                .imageFormat(img.getImageFormat())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private HotelResponse convertToFilterResponse(Hotel hotel) {
        Double minPrice = hotel.getRooms().stream()
                .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                .map(Room::getPrice)
                .min(BigDecimal::compareTo)
                .map(BigDecimal::doubleValue)
                .orElse(null);

        return HotelResponse.builder()
                .hotelId(hotel.getHotelId())
                .name(hotel.getName())
                .location(hotel.getLocation())
                .description(hotel.getDescription())
                .isActive(hotel.getIsActive())
                .rating(hotel.getRating() != null ? hotel.getRating().doubleValue() : null)
                .minPrice(minPrice)
                .images(hotel.getImages().stream()
                        .map(img -> HotelImageDTO.builder()
                                .imageId(img.getImageId())
                                .imageUrl(img.getImageUrl())
                                .imageFormat(img.getImageFormat())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
