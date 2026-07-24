package com.hotelbooking.booking.batch.exception;

import com.hotelbooking.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice(basePackages = "com.hotelbooking.booking.batch")
@Slf4j
public class BatchBookingExceptionHandler {


    @ExceptionHandler(BatchBookingException.class)
    public ResponseEntity<ApiResponse<Void>> handleBatchBookingException(BatchBookingException ex) {
        log.warn("Batch booking error: {} - {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.<Void>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        log.warn("File too large: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.<Void>builder()
                        .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                        .message("File size exceeds maximum allowed limit (10MB)")
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error in batch booking", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("An unexpected error occurred")
                        .build());
    }
}
