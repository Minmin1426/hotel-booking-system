package com.hotelbooking.operations;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ops_cancellation_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private String bookingCode;

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private String customerName;

    private String customerPhone;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private BigDecimal totalBookingAmount;

    @Column(nullable = false)
    private BigDecimal calculatedRefundAmount;

    private Integer refundPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    private String partnerNote;

    private LocalDateTime processedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }
}
