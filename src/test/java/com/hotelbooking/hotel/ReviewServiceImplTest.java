package com.hotelbooking.hotel;

import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.dto.PagedResponse;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.hotel.dto.CreateReviewRequest;
import com.hotelbooking.hotel.dto.ReviewResponse;
import com.hotelbooking.user.User;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User user;
    private Hotel hotel;
    private Booking booking;
    private CreateReviewRequest createRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .email("customer@hotel.com")
                .fullName("Test Customer")
                .build();

        hotel = Hotel.builder()
                .hotelId(1L)
                .name("Grand Palace")
                .rating(BigDecimal.ZERO)
                .build();

        booking = Booking.builder()
                .bookingId(10L)
                .bookingCode("BK-10")
                .user(user)
                .hotel(hotel)
                .status("COMPLETED")
                .checkInDate(LocalDateTime.now().minusDays(3))
                .checkOutDate(LocalDateTime.now().minusDays(1))
                .build();

        createRequest = CreateReviewRequest.builder()
                .bookingId(10L)
                .rating(5)
                .comment("Excellent room service and comfortable stay!")
                .build();
    }

    @Test
    void createReview_success() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingBookingId(10L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setReviewId(100L);
            r.setCreatedAt(LocalDateTime.now());
            return r;
        });
        when(reviewRepository.getAverageRatingForHotel(1L)).thenReturn(5.0);

        ReviewResponse response = reviewService.createReview(1L, createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getReviewId()).isEqualTo(100L);
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Excellent room service and comfortable stay!");
        assertThat(response.getCustomerName()).isEqualTo("Test Customer");

        verify(hotelRepository, times(1)).save(any(Hotel.class));
        assertThat(hotel.getRating()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
    }

    @Test
    void createReview_bookingNotFound_shouldThrowException() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(1L, createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void createReview_accessDenied_shouldThrowException() {
        User otherUser = User.builder().userId(2L).build();
        booking.setUser(otherUser);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> reviewService.createReview(1L, createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void createReview_bookingNotCompleted_shouldThrowException() {
        booking.setStatus("CONFIRMED");

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> reviewService.createReview(1L, createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("after check-out is completed");
    }

    @Test
    void createReview_alreadyReviewed_shouldThrowException() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingBookingId(10L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(1L, createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already been reviewed");
    }

    @Test
    void getReviewsForHotel_success() {
        Review review = Review.builder()
                .reviewId(100L)
                .user(user)
                .hotel(hotel)
                .booking(booking)
                .rating(5)
                .comment("Superb")
                .status("VISIBLE")
                .createdAt(LocalDateTime.now())
                .build();

        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(review), PageRequest.of(0, 5), 1);

        when(hotelRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findByHotelHotelIdAndStatusOrderByCreatedAtDesc(eq(1L), eq("VISIBLE"), any(Pageable.class)))
                .thenReturn(reviewPage);

        PagedResponse<ReviewResponse> result = reviewService.getReviewsForHotel(1L, 0, 5);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getComment()).isEqualTo("Superb");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }
}
