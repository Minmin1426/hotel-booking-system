package com.hotelbooking.mealticket.exception;

public class InvalidQrCodeException extends RuntimeException {
    public InvalidQrCodeException(String message) {
        super(message);
    }
}
