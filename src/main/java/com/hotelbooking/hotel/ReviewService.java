package com.hotelbooking.hotel;

import com.hotelbooking.common.dto.PagedResponse;
import com.hotelbooking.hotel.dto.CreateReviewRequest;
import com.hotelbooking.hotel.dto.ReviewResponse;

public interface ReviewService {
    ReviewResponse createReview(Long userId, CreateReviewRequest request);
    PagedResponse<ReviewResponse> getReviewsForHotel(Long hotelId, int page, int size);
}
