package com.hotelbooking.booking.batch.exception;

public class BatchBookingException extends RuntimeException {

    private final String errorCode;

    public BatchBookingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
