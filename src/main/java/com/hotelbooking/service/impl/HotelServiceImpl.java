package com.hotelbooking.service.impl;

import com.hotelbooking.dto.*;
import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.HotelImage;
import com.hotelbooking.model.Room;
import org.springframework.web.multipart.MultipartFile;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.HotelSpecification;
import com.hotelbooking.repository.RoomRepository;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

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
        log.info("Filtering hotels with criteria. Name: {}, Location: {}, Keyword: {}", 
                request.getName(), request.getLocation(), request.getKeyword());

        if (request.getKeyword() != null) {
            String normalized = request.getKeyword().replace(" ", "");
            request.setKeyword(normalized.isEmpty() ? null : normalized);
        }

        if (request.getName() != null) {
            String normalized = request.getName().replace(" ", "");
            request.setName(normalized.isEmpty() ? null : normalized);
        }

        if (request.getLocation() != null) {
            String normalized = request.getLocation().replace(" ", "");
            request.setLocation(normalized.isEmpty() ? null : normalized);
        }

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

    @Override
    @Transactional
    public HotelResponse createHotel(HotelCreateRequest request) {
        log.info("Creating new hotel: {}", request.getName());
        Hotel hotel = Hotel.builder()
                .name(request.getName())
                .location(request.getLocation())
                .description(request.getDescription())
                .isActive(true)
                .build();
        
        Hotel saved = hotelRepository.save(hotel);
        return convertToFilterResponse(saved);
    }

    @Override
    @Transactional
    public HotelResponse updateHotel(Long id, HotelUpdateRequest request) {
        log.info("Updating hotel ID: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id.toString()));

        hotel.setName(request.getName());
        hotel.setLocation(request.getLocation());
        hotel.setDescription(request.getDescription());
        hotel.setIsActive(request.getIsActive());

        Hotel saved = hotelRepository.save(hotel);
        return convertToFilterResponse(saved);
    }

    @Override
    @Transactional
    public void deleteHotel(Long id) {
        log.info("Deleting hotel. Hotel ID: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id.toString()));

        // Check if there are active bookings
        boolean hasActiveBookings = bookingRepository.existsByHotel_HotelIdAndStatus(id, "CONFIRMED") 
                || bookingRepository.existsByHotel_HotelIdAndStatus(id, "PENDING");
        
        if (hasActiveBookings) {
            throw new BusinessException("Cannot delete hotel because active bookings exist");
        }

        // Soft delete hotel
        hotel.setIsActive(false);
        hotelRepository.save(hotel);

        // Disable all rooms
        List<Room> rooms = roomRepository.findByHotel_HotelId(id);
        for (Room room : rooms) {
            room.setStatus("UNAVAILABLE");
        }
        roomRepository.saveAll(rooms);
    }

    @Override
    @Transactional
    public String uploadImage(Long hotelId, MultipartFile file) throws java.io.IOException {
        log.info("Uploading image for hotelId: {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", hotelId.toString()));

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new BusinessException("Invalid file name");
        }

        String lower = filename.toLowerCase();
        if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp"))) {
            throw new BusinessException("Only JPG, PNG, and WEBP formats are allowed");
        }

        java.io.File uploadDir = new java.io.File("uploads/hotels");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String savedName = UUID.randomUUID().toString() + "_" + filename;
        java.io.File destination = new java.io.File(uploadDir, savedName);
        file.transferTo(destination);

        HotelImage img = HotelImage.builder()
                .hotel(hotel)
                .imageUrl(destination.getPath().replace("\\", "/"))
                .imageFormat(file.getContentType())
                .build();
        
        hotel.getImages().add(img);
        hotelRepository.save(hotel);

        return destination.getPath().replace("\\", "/");
    }
}
