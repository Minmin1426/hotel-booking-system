-- V20__Customer_portal_profile.sql (PostgreSQL)
-- Description: Extend users table for customer portal — account type, corporate tax profile (CTP), Google linking
-- Module: 007-customer-portal-profile

ALTER TABLE users ADD COLUMN IF NOT EXISTS account_type VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER';
ALTER TABLE users ADD COLUMN IF NOT EXISTS google_subject_id VARCHAR(255) NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_users_google_subject ON users(google_subject_id) WHERE google_subject_id IS NOT NULL;

ALTER TABLE users ADD COLUMN IF NOT EXISTS company_name VARCHAR(255) NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS tax_code VARCHAR(50) NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS company_address TEXT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS billing_email VARCHAR(255) NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS ctp_status VARCHAR(50) NOT NULL DEFAULT 'NOT_SUBMITTED';
ALTER TABLE users ADD COLUMN IF NOT EXISTS ctp_verified_at TIMESTAMP NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS ctp_verified_by BIGINT NULL;

CREATE TABLE IF NOT EXISTS ctp_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    admin_id BIGINT NULL,
    previous_status VARCHAR(50) NOT NULL,
    new_status VARCHAR(50) NOT NULL,
    reason TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ctp_audit_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_ctp_audit_admin FOREIGN KEY (admin_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_ctp_audit_user_id ON ctp_audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_ctp_audit_admin_id ON ctp_audit_logs(admin_id);
CREATE INDEX IF NOT EXISTS idx_ctp_audit_created_at ON ctp_audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_users_ctp_status ON users(ctp_status);
CREATE INDEX IF NOT EXISTS idx_users_account_type ON users(account_type);
