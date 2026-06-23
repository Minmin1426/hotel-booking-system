package com.hotelbooking.report;
import com.hotelbooking.common.dto.PagedResponse;
import com.hotelbooking.hotel.dto.ModerationRequest;
import com.hotelbooking.hotel.dto.ReviewResponse;
import com.hotelbooking.report.dto.BookingStatsResponse;
import com.hotelbooking.report.dto.RevenueReportResponse;
import com.hotelbooking.report.dto.RoomUsageResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    /**
     * UC-24: Thống kê số lượng booking (CONFIRMED, CANCELLED, PENDING).
     */
    BookingStatsResponse getBookingStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * UC-25: Báo cáo doanh thu (theo ngày/tháng/quý/năm).
     */
    RevenueReportResponse getRevenueReport(String period, LocalDate startDate, LocalDate endDate);

    /**
     * UC-26: Xem báo cáo sử dụng phòng theo khoảng thời gian.
     * Trả về tỷ lệ sử dụng từng loại phòng.
     */
    List<RoomUsageResponse> getRoomUsageReport(LocalDate from, LocalDate to);

    /**
     * UC-30: Xuất báo cáo sử dụng phòng ra file Excel.
     * Trả về byte[] để stream về client.
     * Business Rule: thời gian xử lý ≤ 5 giây.
     */
    byte[] exportRoomUsageToExcel(LocalDate from, LocalDate to);

    /**
     * UC-31: Lấy danh sách reviews để admin kiểm duyệt.
     * Filter theo status: ALL | VISIBLE | HIDDEN
     */
    PagedResponse<ReviewResponse> getReviewsForModeration(String status, int page, int size);

    /**
     * UC-31: Ẩn hoặc hiện review vi phạm. Lưu audit log.
     */
    ReviewResponse moderateReview(Long reviewId, ModerationRequest request, Long adminId);
}
