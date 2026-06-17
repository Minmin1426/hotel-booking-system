package com.hotelbooking.exception;

/**
 * Thrown when booking dates conflict with another confirmed booking or active lock.
 */
public class BookingConflictException extends BusinessException {
    public BookingConflictException(String message) {
        super(message);
    }
}
