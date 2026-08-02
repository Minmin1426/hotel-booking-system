-- V15__Refactor_payment_and_audit_log.sql

-- 1. Refactor payments table
ALTER TABLE payments ADD currency VARCHAR(3);
ALTER TABLE payments DROP COLUMN refund_status;
EXEC sp_rename 'payments.payment_time', 'paid_at', 'COLUMN';

-- 2. Refactor payment_audit_logs table
EXEC sp_rename 'payment_audit_logs.log_id', 'audit_id', 'COLUMN';
ALTER TABLE payment_audit_logs ADD payment_id BIGINT;
ALTER TABLE payment_audit_logs ADD event_type NVARCHAR(100) NOT NULL DEFAULT 'UNKNOWN';
ALTER TABLE payment_audit_logs ADD status NVARCHAR(50);
EXEC sp_rename 'payment_audit_logs.request_payload', 'payload', 'COLUMN';
ALTER TABLE payment_audit_logs DROP COLUMN response_payload;
ALTER TABLE payment_audit_logs DROP COLUMN action;
ALTER TABLE payment_audit_logs ADD ip_address NVARCHAR(45);
ALTER TABLE payment_audit_logs ADD user_agent NVARCHAR(MAX);
