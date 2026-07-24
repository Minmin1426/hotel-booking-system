package com.hotelbooking.operations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class QrScanRequestDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String ticketCode;
        private Integer redeemCount = 1;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Boolean isValid;
        private String message;
        private String ticketCode;
        private String guestName;
        private String roomNumber;
        private String packageName;
        private Integer remainingMeals;
        private Integer totalMeals;
        private LocalDateTime scanTime;
    }
}
