package com.hotelbooking.mealticket.exception;

public class TicketExpiredException extends RuntimeException {
    public TicketExpiredException(String message) {
        super(message);
    }
}
