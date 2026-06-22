package com.hotelbooking.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(nullable = false, length = 100)
    private String action; // e.g., CREATE_REQUEST, WEBHOOK_RECEIVED, VERIFY_FAILED

    @Column(name = "request_payload", columnDefinition = "NVARCHAR(MAX)")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "NVARCHAR(MAX)")
    private String responsePayload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
