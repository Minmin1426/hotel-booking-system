package com.hotelbooking.operations;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ops_meal_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false, unique = true)
    private String packageCode;

    @Column(nullable = false)
    private String packageName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealCategory category;

    @Column(nullable = false)
    private BigDecimal pricePerPax;

    @Column(columnDefinition = "TEXT")
    private String dishesDescription;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum MealCategory {
        BREAKFAST_BUFFET, LUNCH_SET, DINNER_SET, GROUP_BANQUET
    }
}
