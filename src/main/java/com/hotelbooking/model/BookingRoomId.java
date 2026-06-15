package com.hotelbooking.model;

import java.io.Serializable;
import java.util.Objects;

public class BookingRoomId implements Serializable {

    private Long booking;
    private Long room;

    public BookingRoomId() {}

    public BookingRoomId(Long booking, Long room) {
        this.booking = booking;
        this.room = room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookingRoomId that)) return false;
        return Objects.equals(booking, that.booking) && Objects.equals(room, that.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(booking, room);
    }
}
