package com.hotelbooking.hotel;

import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.dto.PagedResponse;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.hotel.dto.CreateReviewRequest;
import com.hotelbooking.hotel.dto.ReviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(Long userId, CreateReviewRequest request) {
        log.info("Creating review for booking ID: {} by user ID: {}", request.getBookingId(), userId);

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + request.getBookingId()));

        if (!booking.getUser().getUserId().equals(userId)) {
            throw new BusinessException("Access denied: You do not own this booking");
        }

        if (!"COMPLETED".equalsIgnoreCase(booking.getStatus())) {
            throw new BusinessException("Reviews can only be submitted after check-out is completed");
        }

        if (reviewRepository.existsByBookingBookingId(request.getBookingId())) {
            throw new BusinessException("This booking has already been reviewed");
        }

        Review review = Review.builder()
                .user(booking.getUser())
                .hotel(booking.getHotel())
                .booking(booking)
                .rating(request.getRating())
                .comment(request.getComment())
                .status("VISIBLE")
                .build();

        Review savedReview = reviewRepository.save(review);

        // Recalculate average rating of the hotel
        Hotel hotel = booking.getHotel();
        Double avgRating = reviewRepository.getAverageRatingForHotel(hotel.getHotelId());
        hotel.setRating(avgRating != null ? BigDecimal.valueOf(avgRating) : null);
        hotelRepository.save(hotel);

        log.info("Successfully created review ID: {} and updated hotel ID: {} rating to {}", 
                savedReview.getReviewId(), hotel.getHotelId(), hotel.getRating());

        return toReviewResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getReviewsForHotel(Long hotelId, int page, int size) {
        log.info("Fetching reviews for hotel ID: {}, page: {}, size: {}", hotelId, page, size);
        
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel not found with ID: " + hotelId);
        }

        Pageable pageable = PageRequest.of(page, Math.min(size, 20));
        Page<Review> reviewPage = reviewRepository.findByHotelHotelIdAndStatusOrderByCreatedAtDesc(hotelId, "VISIBLE", pageable);

        List<ReviewResponse> content = reviewPage.getContent().stream()
                .map(this::toReviewResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ReviewResponse>builder()
                .page(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .first(reviewPage.isFirst())
                .last(reviewPage.isLast())
                .content(content)
                .build();
    }

    private ReviewResponse toReviewResponse(Review r) {
        return ReviewResponse.builder()
                .reviewId(r.getReviewId())
                .customerName(r.getUser().getFullName())
                .customerEmail(r.getUser().getEmail())
                .hotelName(r.getHotel().getName())
                .bookingCode(r.getBooking().getBookingCode())
                .rating(r.getRating())
                .comment(r.getComment())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
