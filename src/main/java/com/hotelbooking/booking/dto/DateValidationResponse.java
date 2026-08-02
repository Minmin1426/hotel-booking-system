package com.hotelbooking.booking.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DateValidationResponse {
    private boolean valid;
    private long nights;
    private String message;
}
