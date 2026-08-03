package com.hotelbooking.restaurant;

import com.hotelbooking.restaurant.dto.RestaurantReservationResponse;
import java.util.List;

public interface RestaurantReservationService {
    List<RestaurantReservationResponse> getActiveReservations(String search);
    RestaurantReservationResponse updateStatus(String resCode, String status);
}
