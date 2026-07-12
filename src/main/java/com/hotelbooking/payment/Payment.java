package com.hotelbooking.payment;
import com.hotelbooking.booking.Booking;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // ONLINE | CASH | BANK_TRANSFER

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status; // PENDING | SUCCESS | FAILED | REFUND_PENDING | REFUNDED | MANUAL_REFUND_REQUIRED

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "gateway")
    private String gateway;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "refund_status", length = 50)
    private String refundStatus;

    @Column(name = "refund_amount", precision = 18, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_time")
    private LocalDateTime refundTime;

    @Column(name = "refund_transaction_id")
    private String refundTransactionId;

    @Column(name = "refund_retry_count")
    @Builder.Default
    private Integer refundRetryCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
