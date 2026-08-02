package com.hotelbooking.user.ctp;

public class CtpAlreadyVerifiedException extends RuntimeException {
    public CtpAlreadyVerifiedException(String message) {
        super(message);
    }
}
