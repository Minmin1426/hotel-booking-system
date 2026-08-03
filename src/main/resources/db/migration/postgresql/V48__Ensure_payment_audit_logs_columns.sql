-- V48: Ensure payment_audit_logs columns match the JPA Entity mappings dynamically
-- Handles cases where columns were not correctly renamed in previous migration runs

DO $$
BEGIN
    -- 1. Check and rename request_payload to payload
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='payment_audit_logs' AND column_name='request_payload') THEN
        ALTER TABLE payment_audit_logs RENAME COLUMN request_payload TO payload;
    END IF;

    -- 2. Ensure payload column exists
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='payment_audit_logs' AND column_name='payload') THEN
        ALTER TABLE payment_audit_logs ADD COLUMN payload TEXT;
    END IF;

    -- 3. Check and rename log_id to audit_id
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='payment_audit_logs' AND column_name='log_id') THEN
        ALTER TABLE payment_audit_logs RENAME COLUMN log_id TO audit_id;
    END IF;

    -- 4. Ensure audit_id column exists
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='payment_audit_logs' AND column_name='audit_id') THEN
        ALTER TABLE payment_audit_logs ADD COLUMN audit_id BIGSERIAL;
    END IF;

    -- 5. Ensure action column exists
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='payment_audit_logs' AND column_name='action') THEN
        ALTER TABLE payment_audit_logs ADD COLUMN action VARCHAR(100);
    END IF;

    -- 6. Ensure response_payload column exists
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='payment_audit_logs' AND column_name='response_payload') THEN
        ALTER TABLE payment_audit_logs ADD COLUMN response_payload TEXT;
    END IF;

    -- 7. Ensure transaction_id column exists
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='payment_audit_logs' AND column_name='transaction_id') THEN
        ALTER TABLE payment_audit_logs ADD COLUMN transaction_id VARCHAR(100);
    END IF;

    -- 8. Ensure created_at column exists
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='payment_audit_logs' AND column_name='created_at') THEN
        ALTER TABLE payment_audit_logs ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;
