-- V22__Refund_policy_engine.sql (SQL Server variant)
-- Description: Configurable refund policy engine with audit trail
-- Module: 009-refund-policy-engine

-- 1. Refund policies table
CREATE TABLE refund_policies (
    policy_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    days_before_checkin INT NOT NULL,
    refund_percentage DECIMAL(5,2) NOT NULL,
    description NVARCHAR(500),
    priority INT NOT NULL DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Refund audit logs table
CREATE TABLE refund_audit_logs (
    audit_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    payment_id BIGINT,
    original_amount DECIMAL(18,2),
    refund_percentage DECIMAL(5,2),
    refund_amount DECIMAL(18,2),
    override_by BIGINT,
    override_reason NVARCHAR(MAX),
    previous_payment_status NVARCHAR(50),
    new_payment_status NVARCHAR(50),
    policy_id BIGINT,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refund_audit_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE NO ACTION,
    CONSTRAINT fk_refund_audit_payment FOREIGN KEY (payment_id) REFERENCES payments(payment_id) ON DELETE NO ACTION,
    CONSTRAINT fk_refund_audit_policy FOREIGN KEY (policy_id) REFERENCES refund_policies(policy_id) ON DELETE NO ACTION,
    CONSTRAINT fk_refund_audit_override_by FOREIGN KEY (override_by) REFERENCES users(user_id) ON DELETE NO ACTION
);

-- 3. Seed default policy rules
INSERT INTO refund_policies (name, days_before_checkin, refund_percentage, description, priority, is_active)
VALUES
    (N'Full refund — 7+ days', 7, 100.00, N'Full refund for early cancellations (7+ days before check-in)', 10, 1),
    (N'Partial refund — 3 to 6 days', 3, 50.00, N'Partial refund for medium advance notice (3-6 days before check-in)', 20, 1),
    (N'No refund — less than 3 days', 0, 0.00, N'No refund for late cancellations (less than 3 days before check-in)', 30, 1);

-- 4. Indexes
CREATE INDEX idx_refund_policies_active ON refund_policies(is_active, priority);
CREATE INDEX idx_refund_policies_days ON refund_policies(days_before_checkin);
CREATE INDEX idx_refund_audit_booking ON refund_audit_logs(booking_id);
CREATE INDEX idx_refund_audit_payment ON refund_audit_logs(payment_id);
CREATE INDEX idx_refund_audit_created_at ON refund_audit_logs(created_at DESC);
