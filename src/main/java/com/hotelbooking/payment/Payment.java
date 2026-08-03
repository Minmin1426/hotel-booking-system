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

    @Column(name = "paid_at")
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

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    public String getRefundStatus() { return refundStatus; }
    public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public LocalDateTime getRefundTime() { return refundTime; }
    public void setRefundTime(LocalDateTime refundTime) { this.refundTime = refundTime; }
    public String getRefundTransactionId() { return refundTransactionId; }
    public void setRefundTransactionId(String refundTransactionId) { this.refundTransactionId = refundTransactionId; }
    public Integer getRefundRetryCount() { return refundRetryCount; }
    public void setRefundRetryCount(Integer refundRetryCount) { this.refundRetryCount = refundRetryCount; }
    public Boolean getIsDeposit() { return isDeposit; }
    public void setIsDeposit(Boolean isDeposit) { this.isDeposit = isDeposit; }
    public BigDecimal getDepositRatio() { return depositRatio; }
    public void setDepositRatio(BigDecimal depositRatio) { this.depositRatio = depositRatio; }
    public LocalDateTime getCountdownEndTime() { return countdownEndTime; }
    public void setCountdownEndTime(LocalDateTime countdownEndTime) { this.countdownEndTime = countdownEndTime; }
    public BigDecimal getMealRefundAmount() { return mealRefundAmount; }
    public void setMealRefundAmount(BigDecimal mealRefundAmount) { this.mealRefundAmount = mealRefundAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static PaymentBuilder builder() { return new PaymentBuilder(); }

    public static class PaymentBuilder {
        private Long paymentId;
        private Booking booking;
        private String paymentMethod;
        private BigDecimal amount;
        private String status;
        private String transactionId;
        private String gateway;
        private LocalDateTime paymentTime;
        private String refundStatus;
        private BigDecimal refundAmount;
        private LocalDateTime refundTime;
        private String refundTransactionId;
        private Integer refundRetryCount = 0;
        private Boolean isDeposit = false;
        private BigDecimal depositRatio = BigDecimal.ONE;
        private LocalDateTime countdownEndTime;
        private BigDecimal mealRefundAmount;

        public PaymentBuilder paymentId(Long paymentId) { this.paymentId = paymentId; return this; }
        public PaymentBuilder booking(Booking booking) { this.booking = booking; return this; }
        public PaymentBuilder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public PaymentBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public PaymentBuilder status(String status) { this.status = status; return this; }
        public PaymentBuilder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public PaymentBuilder gateway(String gateway) { this.gateway = gateway; return this; }
        public PaymentBuilder paymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; return this; }
        public PaymentBuilder refundStatus(String refundStatus) { this.refundStatus = refundStatus; return this; }
        public PaymentBuilder refundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; return this; }
        public PaymentBuilder refundTime(LocalDateTime refundTime) { this.refundTime = refundTime; return this; }
        public PaymentBuilder refundTransactionId(String refundTransactionId) { this.refundTransactionId = refundTransactionId; return this; }
        public PaymentBuilder refundRetryCount(Integer refundRetryCount) { this.refundRetryCount = refundRetryCount; return this; }
        public PaymentBuilder isDeposit(Boolean isDeposit) { this.isDeposit = isDeposit; return this; }
        public PaymentBuilder depositRatio(BigDecimal depositRatio) { this.depositRatio = depositRatio; return this; }
        public PaymentBuilder countdownEndTime(LocalDateTime countdownEndTime) { this.countdownEndTime = countdownEndTime; return this; }
        public PaymentBuilder mealRefundAmount(BigDecimal mealRefundAmount) { this.mealRefundAmount = mealRefundAmount; return this; }
        public Payment build() {
            Payment p = new Payment();
            p.setPaymentId(paymentId);
            p.setBooking(booking);
            p.setPaymentMethod(paymentMethod);
            p.setAmount(amount);
            p.setStatus(status);
            p.setTransactionId(transactionId);
            p.setGateway(gateway);
            p.setPaymentTime(paymentTime);
            p.setRefundStatus(refundStatus);
            p.setRefundAmount(refundAmount);
            p.setRefundTime(refundTime);
            p.setRefundTransactionId(refundTransactionId);
            p.setRefundRetryCount(refundRetryCount != null ? refundRetryCount : 0);
            p.setIsDeposit(isDeposit != null ? isDeposit : false);
            p.setDepositRatio(depositRatio != null ? depositRatio : BigDecimal.ONE);
            p.setCountdownEndTime(countdownEndTime);
            p.setMealRefundAmount(mealRefundAmount);
            return p;
        }
    }
}
