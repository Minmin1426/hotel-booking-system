package com.hotelbooking.report;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.booking.BookingRoomRepository;
import com.hotelbooking.common.dto.PagedResponse;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.hotel.Hotel;
import com.hotelbooking.hotel.Review;
import com.hotelbooking.hotel.ReviewRepository;
import com.hotelbooking.hotel.dto.ModerationRequest;
import com.hotelbooking.hotel.dto.ReviewResponse;
import com.hotelbooking.payment.PaymentRepository;
import com.hotelbooking.report.dto.BookingStatsResponse;
import com.hotelbooking.report.dto.DailyBookingStats;
import com.hotelbooking.report.dto.HotelRevenueDto;
import com.hotelbooking.report.dto.RevenuePeriodDto;
import com.hotelbooking.report.dto.RevenueReportResponse;
import com.hotelbooking.report.dto.RoomUsageResponse;
import com.hotelbooking.report.dto.StatusBreakdownDto;
import com.hotelbooking.room.Room;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private static final int MAX_PAGE_SIZE = 20;

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRoomRepository bookingRoomRepository;
    private final ReviewRepository reviewRepository;

    // ─── UC-24: Thống kê số lượng booking ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public BookingStatsResponse getBookingStatistics(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Fetch bookings grouped by status
        List<Object[]> statusCounts = bookingRepository.countBookingsByStatus(startDateTime, endDateTime);

        long totalBookings = 0;
        long confirmed = 0;
        long cancelled = 0;
        long pending = 0;

        List<StatusBreakdownDto> breakdownList = new ArrayList<>();
        Map<String, Long> statusMap = new HashMap<>();

        for (Object[] row : statusCounts) {
            String status = (String) row[0];
            Long count = (Long) row[1];
            statusMap.put(status, count);
            totalBookings += count;

            if ("CONFIRMED".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                confirmed += count;
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                cancelled = count;
            } else if ("PENDING".equalsIgnoreCase(status) || "PENDING_PAYMENT".equalsIgnoreCase(status)) {
                pending += count;
            }
        }

        for (Map.Entry<String, Long> entry : statusMap.entrySet()) {
            double percentage = totalBookings > 0 
                ? (double) entry.getValue() * 100.0 / totalBookings 
                : 0.0;
            // Round to 2 decimal places
            percentage = Math.round(percentage * 100.0) / 100.0;

            breakdownList.add(new StatusBreakdownDto(entry.getKey(), entry.getValue(), percentage));
        }

        // Fetch daily statistics
        List<DailyBookingStats> dailyStatsList = bookingRepository.findDailyStats(startDateTime, endDateTime);

        return BookingStatsResponse.builder()
                .totalBookings(totalBookings)
                .confirmedBookings(confirmed)
                .cancelledBookings(cancelled)
                .pendingBookings(pending)
                .dailyStatistics(dailyStatsList)
                .statusBreakdown(breakdownList)
                .build();
    }

    // ─── UC-25: Báo cáo doanh thu ──────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(String period, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        if (period == null || period.trim().isEmpty()) {
            period = "MONTH";
        }
        
        String upperPeriod = period.toUpperCase();
        if (!Arrays.asList("DAY", "MONTH", "QUARTER", "YEAR").contains(upperPeriod)) {
            throw new IllegalArgumentException("Invalid period type. Allowed values: DAY, MONTH, QUARTER, YEAR");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 1. Calculate Total Revenue
        BigDecimal totalRevenue = paymentRepository.sumRevenue(startDateTime, endDateTime);

        // 2. Fetch Revenue grouped by Hotel
        List<HotelRevenueDto> revenueByHotelList = paymentRepository.findRevenueByHotel(startDateTime, endDateTime);

        // 3. Fetch Daily Revenue and aggregate in Java for database independence
        List<Object[]> dailyRevenue = paymentRepository.findDailyRevenue(startDateTime, endDateTime);

        Map<String, RevenuePeriodDto> periodMap = new LinkedHashMap<>();

        for (Object[] row : dailyRevenue) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            Long count = (Long) row[2];

            String label;
            switch (upperPeriod) {
                case "DAY":
                    label = date.toString();
                    break;
                case "MONTH":
                    label = String.format("%d-%02d", date.getYear(), date.getMonthValue());
                    break;
                case "QUARTER":
                    int quarter = (date.getMonthValue() - 1) / 3 + 1;
                    label = String.format("%d-Q%d", date.getYear(), quarter);
                    break;
                case "YEAR":
                    label = String.valueOf(date.getYear());
                    break;
                default:
                    label = String.format("%d-%02d", date.getYear(), date.getMonthValue());
            }

            RevenuePeriodDto periodDto = periodMap.computeIfAbsent(
                    label, 
                    k -> new RevenuePeriodDto(k, BigDecimal.ZERO, 0L)
            );
            periodDto.setRevenue(periodDto.getRevenue().add(revenue));
            periodDto.setBookingCount(periodDto.getBookingCount() + count);
        }

        List<RevenuePeriodDto> periodRevenueList = new ArrayList<>(periodMap.values());

        return RevenueReportResponse.builder()
                .totalRevenue(totalRevenue)
                .periodRevenue(periodRevenueList)
                .revenueByHotel(revenueByHotelList)
                .build();
    }

    // ─── UC-26: Báo cáo sử dụng phòng ───────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RoomUsageResponse> getRoomUsageReport(LocalDate from, LocalDate to) {
        log.info("UC-26: getRoomUsageReport from={} to={}", from, to);

        if (from.isAfter(to)) {
            throw new BusinessException("Ngày bắt đầu phải trước ngày kết thúc");
        }

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);
        long periodDays = from.until(to, java.time.temporal.ChronoUnit.DAYS) + 1;

        // Lấy usage stats theo loại phòng
        List<Object[]> usageRows = bookingRoomRepository.getRoomUsageStats(fromDt, toDt);

        // Lấy tổng số phòng theo loại
        Map<String, Long> roomCountByType = bookingRoomRepository.getRoomCountByType()
                .stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (Long) r[1]
                ));

        return usageRows.stream().map(row -> {
            String roomType       = (String) row[0];
            long totalNights      = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            long totalBookings    = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            BigDecimal revenue    = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
            long totalRooms       = roomCountByType.getOrDefault(roomType, 1L);

            // occupancyRate = totalNights / (totalRooms * periodDays) * 100
            BigDecimal maxNights  = BigDecimal.valueOf(totalRooms * periodDays);
            BigDecimal occupancy  = maxNights.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(totalNights)
                            .divide(maxNights, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);

            return RoomUsageResponse.builder()
                    .roomType(roomType)
                    .totalNights(totalNights)
                    .totalBookings(totalBookings)
                    .totalRevenue(revenue)
                    .totalRooms(totalRooms)
                    .periodDays(periodDays)
                    .occupancyRate(occupancy)
                    .build();
        }).collect(Collectors.toList());
    }

    // ─── UC-30: Xuất Excel ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] exportRoomUsageToExcel(LocalDate from, LocalDate to) {
        log.info("UC-30: exportRoomUsageToExcel from={} to={}", from, to);

        List<RoomUsageResponse> data = getRoomUsageReport(from, to);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Room Usage Report");

            // ── Styles ──
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle percentStyle = workbook.createCellStyle();
            percentStyle.setAlignment(HorizontalAlignment.RIGHT);

            // ── Title row ──
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            String period = from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + " - " + to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            titleCell.setCellValue("BÁO CÁO SỬ DỤNG PHÒNG: " + period);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // ── Column headers ──
            String[] headers = {
                "Loại phòng", "Tổng số phòng", "Số đêm đặt",
                "Số booking", "Doanh thu (VND)", "Tỷ lệ sử dụng (%)", "Số ngày báo cáo"
            };
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Data rows ──
            int rowIdx = 3;
            for (RoomUsageResponse r : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getRoomType());
                Cell c1 = row.createCell(1); c1.setCellValue(r.getTotalRooms()); c1.setCellStyle(numberStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(r.getTotalNights()); c2.setCellStyle(numberStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(r.getTotalBookings()); c3.setCellStyle(numberStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(r.getTotalRevenue().doubleValue()); c4.setCellStyle(numberStyle);
                Cell c5 = row.createCell(5); c5.setCellValue(r.getOccupancyRate().doubleValue()); c5.setCellStyle(percentStyle);
                Cell c6 = row.createCell(6); c6.setCellValue(r.getPeriodDays()); c6.setCellStyle(numberStyle);
            }

            // ── Auto size columns ──
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("UC-30: Excel export complete, {} rows, {} bytes", data.size(), out.size());
            return out.toByteArray();

        } catch (Exception e) {
            log.error("UC-30: Excel export failed", e);
            throw new BusinessException("Xuất báo cáo Excel thất bại: " + e.getMessage());
        }
    }

    // ─── UC-31: Kiểm duyệt đánh giá ─────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getReviewsForModeration(String status, int page, int size) {
        log.info("UC-31: getReviewsForModeration status={}, page={}, size={}", status, page, size);

        int validatedSize = (size <= 0 || size > MAX_PAGE_SIZE) ? MAX_PAGE_SIZE : size;
        String statusFilter = "ALL".equalsIgnoreCase(status) ? null : status;

        Page<Review> reviewPage = reviewRepository.findAllByStatus(
                statusFilter, PageRequest.of(page, validatedSize));

        List<ReviewResponse> content = reviewPage.getContent().stream()
                .map(this::toReviewResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ReviewResponse>builder()
                .content(content)
                .page(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .first(reviewPage.isFirst())
                .last(reviewPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public ReviewResponse moderateReview(Long reviewId, ModerationRequest request, Long adminId) {
        log.info("UC-31: moderateReview id={}, action={}, adminId={}", reviewId, request.getAction(), adminId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review không tồn tại: " + reviewId));

        // BR: khi HIDE phải có lý do
        if ("HIDE".equals(request.getAction()) &&
                (request.getReason() == null || request.getReason().isBlank())) {
            throw new BusinessException("Phải cung cấp lý do khi ẩn review");
        }

        String newStatus = "HIDE".equals(request.getAction()) ? "HIDDEN" : "VISIBLE";
        review.setStatus(newStatus);
        review.setModeratedBy(adminId);
        review.setModeratedAt(LocalDateTime.now());
        review.setModerationReason("HIDE".equals(request.getAction()) ? request.getReason() : null);

        reviewRepository.save(review);
        log.info("UC-31: Review {} set to {} by admin {}", reviewId, newStatus, adminId);

        return toReviewResponse(review);
    }

    // ─── Mapper ──────────────────────────────────────────────────────────────

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
                .moderationReason(r.getModerationReason())
                .moderatedBy(r.getModeratedBy())
                .moderatedAt(r.getModeratedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
