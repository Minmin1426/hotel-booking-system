package com.hotelbooking.mealticket;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_ticket_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealTicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_valid_days", nullable = false)
    @Builder.Default
    private Integer defaultValidDays = 30;

    @Column(name = "default_price", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal defaultPrice = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
