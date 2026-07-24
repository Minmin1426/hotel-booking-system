package com.hotelbooking.report;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.dto.PagedResponse;
import com.hotelbooking.hotel.dto.ModerationRequest;
import com.hotelbooking.hotel.dto.ReviewResponse;
import com.hotelbooking.report.dto.BookingStatsResponse;
import com.hotelbooking.report.dto.CancellationReportResponse;
import com.hotelbooking.report.dto.GroupRevenueReportResponse;
import com.hotelbooking.report.dto.GroupSegmentReportResponse;
import com.hotelbooking.report.dto.RestaurantRevenueResponse;
import com.hotelbooking.report.dto.RevenueReportResponse;
import com.hotelbooking.report.dto.RoomUsageResponse;
import com.hotelbooking.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ReportController {

    private final ReportService reportService;

    /**
     * UC-24: Xem thống kê booking.
     * GET /api/v1/reports/bookings/statistics?startDate=2024-01-01&endDate=2024-01-31
     * Role: ADMIN
     */
    @GetMapping("/bookings/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingStatsResponse> getBookingStatistics(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("UC-24: GET /api/v1/reports/bookings/statistics from {} to {}", startDate, endDate);
        BookingStatsResponse response = reportService.getBookingStatistics(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * UC-25: Xem báo cáo doanh thu.
     * GET /api/v1/reports/revenue?period=MONTH&startDate=2024-01-01&endDate=2024-01-31
     * Role: DIRECTOR
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<RevenueReportResponse> getRevenueReport(
            @RequestParam(value = "period", defaultValue = "MONTH") String period,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("UC-25: GET /api/v1/reports/revenue period={} from {} to {}", period, startDate, endDate);
        RevenueReportResponse response = reportService.getRevenueReport(period, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * UC-26: Xem báo cáo sử dụng phòng.
     * GET /api/v1/reports/room-usage?from=2024-01-01&to=2024-01-31
     * Role: ADMIN, DIRECTOR
     */
    @GetMapping("/room-usage")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<ApiResponse<List<RoomUsageResponse>>> getRoomUsageReport(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        log.info("UC-26: GET /api/v1/reports/room-usage from={} to={}", from, to);
        List<RoomUsageResponse> report = reportService.getRoomUsageReport(from, to);
        return ResponseEntity.ok(ApiResponse.success("Room usage report retrieved", report));
    }

    /**
     * UC-30: Xuất báo cáo sử dụng phòng ra Excel.
     * GET /api/v1/reports/room-usage/export?from=2024-01-01&to=2024-01-31
     * Role: ADMIN, DIRECTOR
     */
    @GetMapping("/room-usage/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<byte[]> exportRoomUsageToExcel(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        log.info("UC-30: GET /api/v1/reports/room-usage/export from={} to={}", from, to);

        byte[] excelBytes = reportService.exportRoomUsageToExcel(from, to);

        String filename = "room-usage-"
                + from.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-to-"
                + to.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelBytes.length)
                .body(excelBytes);
    }

    /**
     * UC-31: Lấy danh sách reviews để kiểm duyệt.
     * GET /api/v1/reports/reviews?status=ALL&page=0&size=20
     * Role: ADMIN
     */
    @GetMapping("/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getReviewsForModeration(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("UC-31: GET /api/v1/reports/reviews status={} page={}", status, page);
        PagedResponse<ReviewResponse> response = reportService.getReviewsForModeration(status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved", response));
    }

    /**
     * UC-31: Ẩn hoặc hiện review vi phạm (lưu audit log).
     * PATCH /api/v1/reports/reviews/{id}/moderate
     * Role: ADMIN
     */
    @PatchMapping("/reviews/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReviewResponse>> moderateReview(
            @PathVariable Long id,
            @Valid @RequestBody ModerationRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("UC-31: PATCH /api/v1/reports/reviews/{}/moderate action={} adminId={}",
                id, request.getAction(), currentUser.getUserId());
        ReviewResponse response = reportService.moderateReview(id, request, currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Review moderated successfully", response));
    }

    @GetMapping("/cancellations")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<ApiResponse<CancellationReportResponse>> getCancellationReport(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success("Cancellation report retrieved", reportService.getCancellationReport(startDate, endDate)));
    }

    @GetMapping("/executive")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<ApiResponse<GroupRevenueReportResponse>> getExecutiveRevenueReport(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success("Executive revenue report retrieved", reportService.getExecutiveRevenueReport(startDate, endDate)));
    }

    @GetMapping("/segments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<ApiResponse<GroupSegmentReportResponse>> getGroupVsLeisureReport(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success("Group vs leisure report retrieved", reportService.getGroupVsLeisureReport(startDate, endDate)));
    }

    @GetMapping("/restaurant")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<ApiResponse<RestaurantRevenueResponse>> getRestaurantRevenueReport(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success("Restaurant revenue report retrieved", reportService.getRestaurantRevenueReport(startDate, endDate)));
    }

    @GetMapping("/executive/export")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<byte[]> exportExecutiveReport(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        byte[] bytes = reportService.exportExecutiveReport(startDate, endDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=executive-report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
