-- V50: Ensure payment_audit_logs columns match the JPA Entity mappings dynamically (SQL Server)
-- Handles cases where columns were not correctly renamed in previous migration runs

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('payment_audit_logs') AND name = 'request_payload')
BEGIN
    EXEC sp_rename 'payment_audit_logs.request_payload', 'payload', 'COLUMN';
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('payment_audit_logs') AND name = 'payload')
BEGIN
    ALTER TABLE payment_audit_logs ADD payload NVARCHAR(MAX);
END;

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('payment_audit_logs') AND name = 'log_id')
BEGIN
    EXEC sp_rename 'payment_audit_logs.log_id', 'audit_id', 'COLUMN';
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('payment_audit_logs') AND name = 'action')
BEGIN
    ALTER TABLE payment_audit_logs ADD action NVARCHAR(100);
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('payment_audit_logs') AND name = 'response_payload')
BEGIN
    ALTER TABLE payment_audit_logs ADD response_payload NVARCHAR(MAX);
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('payment_audit_logs') AND name = 'transaction_id')
BEGIN
    ALTER TABLE payment_audit_logs ADD transaction_id NVARCHAR(100);
END;
