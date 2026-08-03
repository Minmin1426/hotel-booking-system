-- V44__Fix_payments_paid_at_column.sql (SQL Server)
-- Ensure 'paid_at' column exists in payments table (rename from payment_time if present, or add column if missing)

IF EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID('payments') AND name = 'payment_time'
)
BEGIN
    EXEC sp_rename 'payments.payment_time', 'paid_at', 'COLUMN';
END
ELSE IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID('payments') AND name = 'paid_at'
)
BEGIN
    ALTER TABLE payments ADD paid_at DATETIME2 NULL;
END
