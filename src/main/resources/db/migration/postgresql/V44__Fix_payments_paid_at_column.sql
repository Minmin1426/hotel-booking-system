-- V44__Fix_payments_paid_at_column.sql (PostgreSQL)
-- Ensure 'paid_at' column exists in payments table (rename from payment_time if present, or add column if missing)

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'payments' AND column_name = 'payment_time'
    ) THEN
        ALTER TABLE payments RENAME COLUMN payment_time TO paid_at;
    ELSIF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'payments' AND column_name = 'paid_at'
    ) THEN
        ALTER TABLE payments ADD COLUMN paid_at TIMESTAMP;
    END IF;
END $$;
