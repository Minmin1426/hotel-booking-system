-- V16__Refactor_payment_and_audit_log.sql

-- 1. Refactor payments table
ALTER TABLE payments ADD COLUMN currency VARCHAR(3);
ALTER TABLE payments DROP COLUMN refund_status;
ALTER TABLE payments RENAME COLUMN payment_time TO paid_at;

-- 2. Refactor payment_audit_logs table
ALTER TABLE payment_audit_logs RENAME COLUMN log_id TO audit_id;
ALTER TABLE payment_audit_logs ADD COLUMN payment_id BIGINT;
ALTER TABLE payment_audit_logs ADD COLUMN event_type VARCHAR(100) NOT NULL DEFAULT 'UNKNOWN';
ALTER TABLE payment_audit_logs ADD COLUMN status VARCHAR(50);
ALTER TABLE payment_audit_logs RENAME COLUMN request_payload TO payload;
ALTER TABLE payment_audit_logs DROP COLUMN response_payload;
ALTER TABLE payment_audit_logs DROP COLUMN action;
ALTER TABLE payment_audit_logs ADD COLUMN ip_address VARCHAR(45);
ALTER TABLE payment_audit_logs ADD COLUMN user_agent TEXT;
