package com.hotelbooking.common.exception;

/**
 * Thrown when trying to register with an email that is already registered.
 * Maps to HTTP 400 in GlobalExceptionHandler.
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("Email already exists");
    }

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
