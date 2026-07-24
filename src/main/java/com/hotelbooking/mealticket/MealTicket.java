package com.hotelbooking.mealticket;

import com.hotelbooking.booking.Booking;
import com.hotelbooking.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "meal_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private MealTicketType ticketType;


    @Column(name = "qr_code", nullable = false, unique = true, length = 255)
    private String qrCode;

    @Column(name = "qr_signature", nullable = false, length = 64)
    private String qrSignature;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TicketStatus status = TicketStatus.UNUSED;

    @Column(name = "issued_at", nullable = false)
    @Builder.Default
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumed_by_staff_id")
    private User consumedByStaff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by")
    private User issuedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isUnused() {
        return this.status == TicketStatus.UNUSED;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isExpiredTicket() {
        return this.status == TicketStatus.EXPIRED;
    }

    public void consume(User staff) {
        this.status = TicketStatus.USED;
        this.usedAt = LocalDateTime.now();
        this.consumedByStaff = staff;
    }

    public void markExpired() {
        this.status = TicketStatus.EXPIRED;
    }
}
