package com.hotelbooking.admin.exception;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long userId) {
        super("Customer not found: " + userId);
    }
}
