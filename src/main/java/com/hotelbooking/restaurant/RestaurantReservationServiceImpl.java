package com.hotelbooking.restaurant;

import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.restaurant.dto.RestaurantReservationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RestaurantReservationServiceImpl implements RestaurantReservationService {

    private final RestaurantReservationRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantReservationResponse> getActiveReservations(String search) {
        log.info("Fetching restaurant reservations with search={}", search);
        List<RestaurantReservation> list;
        if (search == null || search.trim().isEmpty()) {
            list = repository.findAll();
        } else {
            list = repository.searchReservations(search.trim());
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public RestaurantReservationResponse updateStatus(String resCode, String status) {
        log.info("Updating status of restaurant reservation resCode={} to {}", resCode, status);
        RestaurantReservation res = repository.findByResCode(resCode)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant reservation not found with code: " + resCode));
        res.setStatus(status.toUpperCase());
        RestaurantReservation saved = repository.save(res);
        return mapToResponse(saved);
    }

    private RestaurantReservationResponse mapToResponse(RestaurantReservation res) {
        return RestaurantReservationResponse.builder()
                .resCode(res.getResCode())
                .guestName(res.getGuestName())
                .guestPhone(res.getGuestPhone())
                .pkgTitle(res.getPkgTitle())
                .date(res.getResDate())
                .time(res.getResTime())
                .holdLimit(res.getHoldLimit())
                .guests(res.getGuests())
                .price(res.getPrice())
                .status(res.getStatus())
                .notes(res.getNotes())
                .build();
    }
}
