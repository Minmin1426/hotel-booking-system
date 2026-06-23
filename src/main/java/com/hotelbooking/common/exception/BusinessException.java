package com.hotelbooking.common.exception;

/**
 * Thrown when a business rule is violated.
 * Maps to HTTP 400 in GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
