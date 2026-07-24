package com.hotelbooking.admin.exception;

public class InvalidNoteContentException extends RuntimeException {
    public InvalidNoteContentException() {
        super("Note content cannot be empty");
    }
}
