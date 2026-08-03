package com.hotelbooking.booking.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DateValidationResponse {
    private boolean valid;
    private long nights;
    private String message;

    public DateValidationResponse() {}
    public DateValidationResponse(boolean valid, long nights, String message) {
        this.valid = valid;
        this.nights = nights;
        this.message = message;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public long getNights() { return nights; }
    public void setNights(long nights) { this.nights = nights; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public static DateValidationResponseBuilder builder() {
        return new DateValidationResponseBuilder();
    }

    public static class DateValidationResponseBuilder {
        private boolean valid;
        private long nights;
        private String message;

        public DateValidationResponseBuilder valid(boolean valid) { this.valid = valid; return this; }
        public DateValidationResponseBuilder nights(long nights) { this.nights = nights; return this; }
        public DateValidationResponseBuilder message(String message) { this.message = message; return this; }
        public DateValidationResponse build() { return new DateValidationResponse(valid, nights, message); }
    }
}
