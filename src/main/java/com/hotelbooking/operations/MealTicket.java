package com.hotelbooking.operations;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity(name = "OpsMealTicket")
@Table(name = "ops_meal_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticketCode;

    private Long bookingId;

    @Column(nullable = false)
    private String guestName;

    private String roomNumber;

    @Column(nullable = false)
    private Long mealPackageId;

    @Column(nullable = false)
    private String packageName;

    @Column(nullable = false)
    private Integer totalMeals;

    @Column(nullable = false)
    private Integer remainingMeals;

    private LocalDateTime validFrom;
    
    private LocalDateTime validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketStatus status = TicketStatus.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum TicketStatus {
        ACTIVE, EXHAUSTED, EXPIRED, CANCELLED
    }
}
