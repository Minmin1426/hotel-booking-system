package com.hotelbooking.hotel;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.dto.PagedResponse;
import com.hotelbooking.hotel.dto.CreateReviewRequest;
import com.hotelbooking.hotel.dto.ReviewResponse;
import com.hotelbooking.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateReviewRequest request) {
        log.info("Received request to create review for booking ID: {} by user: {}", request.getBookingId(), currentUser.getEmail());
        ReviewResponse response = reviewService.createReview(currentUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Review submitted successfully", response));
    }

    @GetMapping("/hotels/{hotelId}/reviews")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getHotelReviews(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to get reviews for hotel ID: {}, page: {}, size: {}", hotelId, page, size);
        PagedResponse<ReviewResponse> response = reviewService.getReviewsForHotel(hotelId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", response));
    }
}
