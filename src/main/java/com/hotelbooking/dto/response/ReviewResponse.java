package com.hotelbooking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

/**
 * UC-31: Thông tin review để admin kiểm duyệt.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {

    private Long reviewId;
    private String customerName;
    private String customerEmail;
    private String hotelName;
    private String bookingCode;
    private Integer rating;
    private String comment;
    private String status;           // VISIBLE | HIDDEN
    private String moderationReason;
    private Long moderatedBy;
    private LocalDateTime moderatedAt;
    private LocalDateTime createdAt;
}
