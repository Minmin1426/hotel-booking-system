package com.hotelbooking.user.ctp;

public class CtpNotFoundException extends RuntimeException {
    public CtpNotFoundException(String message) {
        super(message);
    }
}
