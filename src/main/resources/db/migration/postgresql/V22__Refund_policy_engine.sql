-- V22__Refund_policy_engine.sql (PostgreSQL)
-- Description: Configurable refund policy engine with audit trail
-- Module: 009-refund-policy-engine

CREATE TABLE IF NOT EXISTS refund_policies (
    policy_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    days_before_checkin INT NOT NULL,
    refund_percentage DECIMAL(5,2) NOT NULL,
    description VARCHAR(500),
    priority INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refund_audit_logs (
    audit_id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    payment_id BIGINT,
    original_amount DECIMAL(18,2),
    refund_percentage DECIMAL(5,2),
    refund_amount DECIMAL(18,2),
    override_by BIGINT,
    override_reason TEXT,
    previous_payment_status VARCHAR(50),
    new_payment_status VARCHAR(50),
    policy_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refund_audit_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE NO ACTION,
    CONSTRAINT fk_refund_audit_payment FOREIGN KEY (payment_id) REFERENCES payments(payment_id) ON DELETE NO ACTION,
    CONSTRAINT fk_refund_audit_policy FOREIGN KEY (policy_id) REFERENCES refund_policies(policy_id) ON DELETE NO ACTION,
    CONSTRAINT fk_refund_audit_override_by FOREIGN KEY (override_by) REFERENCES users(user_id) ON DELETE NO ACTION
);

INSERT INTO refund_policies (name, days_before_checkin, refund_percentage, description, priority, is_active)
VALUES
    ('Full refund — 7+ days', 7, 100.00, 'Full refund for early cancellations (7+ days before check-in)', 10, true),
    ('Partial refund — 3 to 6 days', 3, 50.00, 'Partial refund for medium advance notice (3-6 days before check-in)', 20, true),
    ('No refund — less than 3 days', 0, 0.00, 'No refund for late cancellations (less than 3 days before check-in)', 30, true)
ON CONFLICT DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_refund_policies_active ON refund_policies(is_active, priority);
CREATE INDEX IF NOT EXISTS idx_refund_policies_days ON refund_policies(days_before_checkin);
CREATE INDEX IF NOT EXISTS idx_refund_audit_booking ON refund_audit_logs(booking_id);
CREATE INDEX IF NOT EXISTS idx_refund_audit_payment ON refund_audit_logs(payment_id);
CREATE INDEX IF NOT EXISTS idx_refund_audit_created_at ON refund_audit_logs(created_at DESC);
