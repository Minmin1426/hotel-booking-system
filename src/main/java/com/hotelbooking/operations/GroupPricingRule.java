package com.hotelbooking.operations;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ops_group_pricing_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupPricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private Integer minRooms;

    @Column(nullable = false)
    private BigDecimal discountPercent;

    private BigDecimal weekendSurchargePercent;

    private BigDecimal peakSeasonMultiplier;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
