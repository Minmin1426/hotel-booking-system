package com.hotelbooking.common.exception;

/**
 * Thrown when a booking cannot be cancelled due to status or validation errors.
 */
public class BookingCancellationException extends BusinessException {
    public BookingCancellationException(String message) {
        super(message);
    }
}
