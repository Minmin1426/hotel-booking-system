-- V20__Customer_portal_profile.sql (SQL Server)
-- Description: Extend users table for customer portal — account type, corporate tax profile (CTP), Google linking
-- Module: 007-customer-portal-profile

-- 1. Add account type column
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'account_type')
BEGIN
    ALTER TABLE users ADD account_type NVARCHAR(50) NOT NULL DEFAULT 'CUSTOMER';
END;

-- 2. Add Google OAuth subject ID
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'google_subject_id')
BEGIN
    ALTER TABLE users ADD google_subject_id NVARCHAR(255) NULL;
END;

-- Create unique filtered index (allows multiple NULLs, enforces uniqueness for non-NULL)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uq_users_google_subject' AND object_id = OBJECT_ID('users'))
BEGIN
    CREATE UNIQUE INDEX uq_users_google_subject ON users(google_subject_id) WHERE google_subject_id IS NOT NULL;
END;

-- 3. Add CTP columns
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'ctp_status')
BEGIN
    ALTER TABLE users ADD company_name NVARCHAR(255) NULL;
    ALTER TABLE users ADD tax_code NVARCHAR(50) NULL;
    ALTER TABLE users ADD company_address NVARCHAR(MAX) NULL;
    ALTER TABLE users ADD billing_email NVARCHAR(255) NULL;
    ALTER TABLE users ADD ctp_status NVARCHAR(50) NOT NULL DEFAULT 'NOT_SUBMITTED';
    ALTER TABLE users ADD ctp_verified_at DATETIME2 NULL;
    ALTER TABLE users ADD ctp_verified_by BIGINT NULL;
END;

-- 4. Create CTP audit log table
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'ctp_audit_logs')
BEGIN
    CREATE TABLE ctp_audit_logs (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        admin_id BIGINT NULL,
        previous_status NVARCHAR(50) NOT NULL,
        new_status NVARCHAR(50) NOT NULL,
        reason NVARCHAR(MAX) NULL,
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

    ALTER TABLE ctp_audit_logs ADD CONSTRAINT fk_ctp_audit_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;
    ALTER TABLE ctp_audit_logs ADD CONSTRAINT fk_ctp_audit_admin FOREIGN KEY (admin_id) REFERENCES users(user_id) ON DELETE SET NULL;

    CREATE INDEX idx_ctp_audit_user_id ON ctp_audit_logs(user_id);
    CREATE INDEX idx_ctp_audit_admin_id ON ctp_audit_logs(admin_id);
    CREATE INDEX idx_ctp_audit_created_at ON ctp_audit_logs(created_at);
END;

-- 5. Create indexes
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_users_ctp_status' AND object_id = OBJECT_ID('users'))
BEGIN
    CREATE INDEX idx_users_ctp_status ON users(ctp_status);
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_users_account_type' AND object_id = OBJECT_ID('users'))
BEGIN
    CREATE INDEX idx_users_account_type ON users(account_type);
END;
