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

    @Column(name = "is_deposit", nullable = false)
    @Builder.Default
    private Boolean isDeposit = false;

    @Column(name = "deposit_ratio", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal depositRatio = BigDecimal.ONE;

    @Column(name = "countdown_end_time")
    private LocalDateTime countdownEndTime;

    @Column(name = "meal_refund_amount", precision = 18, scale = 2)
    private BigDecimal mealRefundAmount;

    @Column(name = "invoice_company_name")
    private String invoiceCompanyName;

    @Column(name = "invoice_tax_id", length = 50)
    private String invoiceTaxId;

    @Column(name = "invoice_company_address", length = 500)
    private String invoiceCompanyAddress;

    @Column(name = "invoice_company_email")
    private String invoiceCompanyEmail;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
