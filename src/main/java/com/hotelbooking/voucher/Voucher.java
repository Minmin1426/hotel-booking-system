package com.hotelbooking.voucher;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    // PERCENTAGE or FIXED_AMOUNT
    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType;

    @Column(name = "discount_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_booking_value", precision = 18, scale = 2)
    private BigDecimal minBookingValue;

    @Column(name = "max_discount", precision = 18, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "current_usage")
    private Integer currentUsage;

    // 010-voucher-store-front: Extended fields
    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "for_account_type", length = 30)
    @Builder.Default
    private String forAccountType = "ALL";

    @Column(name = "voucher_type", length = 50)
    @Builder.Default
    private String voucherType = "ROOM";

    @Column(name = "combo_meal_benefit", length = 255)
    private String comboMealBenefit;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Points required to claim this voucher from the shop (null = not available in shop)
    @Column(name = "points_cost")
    private Integer pointsCost;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private com.hotelbooking.user.User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Helpers ──────────────────────────────────────────────────────────────────

    public boolean hasAvailability() {
        if (!isActive) return false;
        if (currentUsage != null && maxUsage != null && currentUsage >= maxUsage) return false;
        LocalDateTime now = LocalDateTime.now();
        if (startDate != null && now.isBefore(startDate)) return false;
        if (endDate != null && now.isAfter(endDate)) return false;
        return true;
    }

    public boolean isForAccountType(String accountType) {
        return forAccountType == null || "ALL".equalsIgnoreCase(forAccountType) || forAccountType.equalsIgnoreCase(accountType);
    }

    public void incrementUsage() {
        this.currentUsage = (this.currentUsage == null ? 1 : this.currentUsage + 1);
    }
}
