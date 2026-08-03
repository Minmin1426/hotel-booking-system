package com.hotelbooking.booking;
import com.hotelbooking.room.Room;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(BookingRoomId.class)
public class BookingRoom {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "price_at_booking", nullable = false, precision = 18, scale = 2)
    private BigDecimal priceAtBooking;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPriceAtBooking() { return priceAtBooking; }
    public void setPriceAtBooking(BigDecimal priceAtBooking) { this.priceAtBooking = priceAtBooking; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static BookingRoomBuilder builder() { return new BookingRoomBuilder(); }

    public static class BookingRoomBuilder {
        private Booking booking;
        private Room room;
        private Integer quantity = 1;
        private BigDecimal priceAtBooking;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public BookingRoomBuilder booking(Booking booking) { this.booking = booking; return this; }
        public BookingRoomBuilder room(Room room) { this.room = room; return this; }
        public BookingRoomBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public BookingRoomBuilder priceAtBooking(BigDecimal priceAtBooking) { this.priceAtBooking = priceAtBooking; return this; }
        public BookingRoomBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public BookingRoomBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public BookingRoom build() {
            BookingRoom br = new BookingRoom();
            br.setBooking(booking);
            br.setRoom(room);
            br.setQuantity(quantity != null ? quantity : 1);
            br.setPriceAtBooking(priceAtBooking);
            br.setCreatedAt(createdAt);
            br.setUpdatedAt(updatedAt);
            return br;
        }
    }
}
