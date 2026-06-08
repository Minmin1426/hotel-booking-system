package com.hotelbooking.exception;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Standard error response body.
 * Matches format defined in AGENTS.md Section 6.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
