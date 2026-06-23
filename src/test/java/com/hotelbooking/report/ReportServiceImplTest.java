package com.hotelbooking.report;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRoomRepository;
import com.hotelbooking.common.dto.PagedResponse;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.hotel.Hotel;
import com.hotelbooking.hotel.Review;
import com.hotelbooking.hotel.ReviewRepository;
import com.hotelbooking.hotel.dto.ModerationRequest;
import com.hotelbooking.hotel.dto.ReviewResponse;
import com.hotelbooking.report.dto.RoomUsageResponse;
import com.hotelbooking.user.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportServiceImpl Tests — UC-26, UC-30, UC-31")
class ReportServiceImplTest {

    @Mock private BookingRoomRepository bookingRoomRepository;
    @Mock private ReviewRepository reviewRepository;

    @InjectMocks private ReportServiceImpl reportService;

    private Hotel hotel;
    private User customer;
    private User admin;
    private Booking booking;
    private Review visibleReview;
    private Review hiddenReview;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder().hotelId(1L).name("Grand Hotel").location("Hanoi").build();
        customer = User.builder().userId(10L).email("customer@test.com").fullName("Nguyen A").role("CUSTOMER").build();
        admin = User.builder().userId(99L).email("admin@test.com").fullName("Admin").role("ADMIN").build();
        booking = Booking.builder().bookingId(100L).bookingCode("BK-001").user(customer).hotel(hotel)
                .checkInDate(LocalDateTime.now().plusDays(1)).checkOutDate(LocalDateTime.now().plusDays(3))
                .totalAmount(new BigDecimal("1600000")).status("CONFIRMED").build();

        visibleReview = Review.builder().reviewId(1L).user(customer).hotel(hotel).booking(booking)
                .rating(5).comment("Tuyệt vời!").status("VISIBLE").createdAt(LocalDateTime.now()).build();

        hiddenReview = Review.builder().reviewId(2L).user(customer).hotel(hotel).booking(booking)
                .rating(1).comment("Nội dung vi phạm").status("HIDDEN")
                .moderatedBy(99L).moderatedAt(LocalDateTime.now())
                .moderationReason("Ngôn ngữ không phù hợp").createdAt(LocalDateTime.now()).build();
    }

    // ─── UC-26 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UC-26-TC01: Báo cáo phòng trả về đúng tỷ lệ sử dụng")
    void getRoomUsageReport_returnsCorrectOccupancyRate() {
        // Arrange: 1 Standard room, 10 nights booked in 30-day period
        List<Object[]> usageRows = java.util.Collections.singletonList(new Object[]{"Standard", 10L, 3L, new BigDecimal("5000000")});
        List<Object[]> countRows = java.util.Collections.singletonList(new Object[]{"Standard", 2L}); // 2 phòng Standard

        when(bookingRoomRepository.getRoomUsageStats(any(), any())).thenReturn(usageRows);
        when(bookingRoomRepository.getRoomCountByType()).thenReturn(countRows);

        // Act
        List<RoomUsageResponse> result = reportService.getRoomUsageReport(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 30));

        // Assert: 10 nights / (2 rooms * 30 days) * 100 = 16.67%
        assertThat(result).hasSize(1);
        RoomUsageResponse r = result.get(0);
        assertThat(r.getRoomType()).isEqualTo("Standard");
        assertThat(r.getTotalNights()).isEqualTo(10);
        assertThat(r.getTotalBookings()).isEqualTo(3);
        assertThat(r.getOccupancyRate()).isEqualByComparingTo("16.67");
    }

    @Test
    @DisplayName("UC-26-TC02: Không có booking thì occupancy rate = 0")
    void getRoomUsageReport_zeroOccupancyWhenNoBookings() {
        when(bookingRoomRepository.getRoomUsageStats(any(), any())).thenReturn(List.of());
        when(bookingRoomRepository.getRoomCountByType()).thenReturn(List.of());

        List<RoomUsageResponse> result = reportService.getRoomUsageReport(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("UC-26-TC03: Throw BusinessException khi from > to")
    void getRoomUsageReport_throwsWhenFromAfterTo() {
        assertThatThrownBy(() ->
                reportService.getRoomUsageReport(
                        LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 1)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("trước");
    }

    @Test
    @DisplayName("UC-26-TC04: Nhiều loại phòng được báo cáo riêng biệt")
    void getRoomUsageReport_multipleRoomTypes() {
        List<Object[]> usageRows = List.of(
                new Object[]{"Standard", 20L, 5L, new BigDecimal("10000000")},
                new Object[]{"Deluxe",   15L, 4L, new BigDecimal("12000000")}
        );
        List<Object[]> countRows = List.of(
                new Object[]{"Standard", 3L},
                new Object[]{"Deluxe",   2L}
        );
        when(bookingRoomRepository.getRoomUsageStats(any(), any())).thenReturn(usageRows);
        when(bookingRoomRepository.getRoomCountByType()).thenReturn(countRows);

        List<RoomUsageResponse> result = reportService.getRoomUsageReport(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RoomUsageResponse::getRoomType)
                .containsExactly("Standard", "Deluxe");
    }

    // ─── UC-30 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UC-30-TC01: Excel export trả về byte array không rỗng")
    void exportRoomUsageToExcel_returnsNonEmptyBytes() {
        List<Object[]> usageRows = java.util.Collections.singletonList(new Object[]{"Suite", 5L, 2L, new BigDecimal("7500000")});
        List<Object[]> countRows = java.util.Collections.singletonList(new Object[]{"Suite", 1L});

        when(bookingRoomRepository.getRoomUsageStats(any(), any())).thenReturn(usageRows);
        when(bookingRoomRepository.getRoomCountByType()).thenReturn(countRows);

        byte[] result = reportService.exportRoomUsageToExcel(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertThat(result).isNotNull().isNotEmpty();
        // XLSX magic bytes: PK (50 4B)
        assertThat(result[0]).isEqualTo((byte) 0x50);
        assertThat(result[1]).isEqualTo((byte) 0x4B);
    }

    @Test
    @DisplayName("UC-30-TC02: Excel export khi không có dữ liệu vẫn tạo file")
    void exportRoomUsageToExcel_emptyDataStillCreatesFile() {
        when(bookingRoomRepository.getRoomUsageStats(any(), any())).thenReturn(List.of());
        when(bookingRoomRepository.getRoomCountByType()).thenReturn(List.of());

        byte[] result = reportService.exportRoomUsageToExcel(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertThat(result).isNotNull().isNotEmpty();
    }

    // ─── UC-31 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UC-31-TC01: Lấy danh sách reviews thành công")
    void getReviewsForModeration_returnsPagedResults() {
        var mockPage = new PageImpl<>(List.of(visibleReview, hiddenReview), PageRequest.of(0, 20), 2);
        when(reviewRepository.findAllByStatus(isNull(), any(Pageable.class))).thenReturn(mockPage);

        PagedResponse<ReviewResponse> response = reportService.getReviewsForModeration("ALL", 0, 20);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("UC-31-TC02: Filter reviews theo status VISIBLE")
    void getReviewsForModeration_filterByVisible() {
        var mockPage = new PageImpl<>(List.of(visibleReview), PageRequest.of(0, 20), 1);
        when(reviewRepository.findAllByStatus(eq("VISIBLE"), any(Pageable.class))).thenReturn(mockPage);

        PagedResponse<ReviewResponse> response = reportService.getReviewsForModeration("VISIBLE", 0, 20);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getStatus()).isEqualTo("VISIBLE");
    }

    @Test
    @DisplayName("UC-31-TC03: Ẩn review thành công với lý do")
    void moderateReview_hideSuccess() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(visibleReview));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ModerationRequest request = ModerationRequest.builder()
                .action("HIDE").reason("Ngôn ngữ không phù hợp").build();

        ReviewResponse response = reportService.moderateReview(1L, request, 99L);

        assertThat(response.getStatus()).isEqualTo("HIDDEN");
        assertThat(response.getModerationReason()).isEqualTo("Ngôn ngữ không phù hợp");
        assertThat(response.getModeratedBy()).isEqualTo(99L);
    }

    @Test
    @DisplayName("UC-31-TC04: Hiện lại review đã ẩn thành công")
    void moderateReview_showSuccess() {
        when(reviewRepository.findById(2L)).thenReturn(Optional.of(hiddenReview));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ModerationRequest request = ModerationRequest.builder().action("SHOW").build();

        ReviewResponse response = reportService.moderateReview(2L, request, 99L);

        assertThat(response.getStatus()).isEqualTo("VISIBLE");
        assertThat(response.getModerationReason()).isNull();
    }

    @Test
    @DisplayName("UC-31-TC05: Throw BusinessException khi HIDE không có lý do")
    void moderateReview_throwsWhenHideWithoutReason() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(visibleReview));

        ModerationRequest request = ModerationRequest.builder()
                .action("HIDE").reason("").build();

        assertThatThrownBy(() -> reportService.moderateReview(1L, request, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("lý do");
    }

    @Test
    @DisplayName("UC-31-TC06: Throw ResourceNotFoundException khi review không tồn tại")
    void moderateReview_throwsWhenReviewNotFound() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        ModerationRequest request = ModerationRequest.builder()
                .action("HIDE").reason("Test").build();

        assertThatThrownBy(() -> reportService.moderateReview(999L, request, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("UC-31-TC07: Audit log được ghi khi moderate")
    void moderateReview_auditFieldsAreSet() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(visibleReview));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ModerationRequest request = ModerationRequest.builder()
                .action("HIDE").reason("Vi phạm").build();

        reportService.moderateReview(1L, request, 99L);

        verify(reviewRepository).save(argThat(r ->
                r.getModeratedBy().equals(99L) &&
                r.getModeratedAt() != null &&
                "HIDDEN".equals(r.getStatus())
        ));
    }
}
