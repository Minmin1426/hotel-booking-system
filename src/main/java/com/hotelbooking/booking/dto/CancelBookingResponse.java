package com.hotelbooking.booking.dto;

public class CancelBookingResponse {

    private Long bookingId;
    private String bookingStatus;
    private String refundStatus;
    private String message;

    public CancelBookingResponse() {}

    public CancelBookingResponse(
            Long bookingId,
            String bookingStatus,
            String refundStatus,
            String message) {

        this.bookingId = bookingId;
        this.bookingStatus = bookingStatus;
        this.refundStatus = refundStatus;
        this.message = message;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
