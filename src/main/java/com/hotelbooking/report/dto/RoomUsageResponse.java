package com.hotelbooking.report.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * UC-26: Tỷ lệ sử dụng phòng theo loại trong khoảng thời gian.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomUsageResponse {

    private String roomType;
    private long totalNights;        // tổng số đêm đã được đặt
    private long totalBookings;      // số lượt booking
    private BigDecimal totalRevenue; // doanh thu từ loại phòng này
    private long totalRooms;         // tổng số phòng loại này hiện có
    private long periodDays;         // số ngày trong khoảng báo cáo
    private BigDecimal occupancyRate; // tỷ lệ sử dụng (%) = totalNights / (totalRooms * periodDays) * 100
}
