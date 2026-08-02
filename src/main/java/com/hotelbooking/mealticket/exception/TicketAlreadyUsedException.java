package com.hotelbooking.mealticket.exception;

public class TicketAlreadyUsedException extends RuntimeException {
    public TicketAlreadyUsedException(String message) {
        super(message);
    }
}
