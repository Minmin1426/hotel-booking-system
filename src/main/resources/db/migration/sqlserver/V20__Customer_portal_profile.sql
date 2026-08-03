-- V20__Customer_portal_profile.sql (SQL Server)
-- Description: Extend users table for customer portal — account type, corporate tax profile (CTP), Google linking
-- Module: 007-customer-portal-profile

-- Step 1: Add account_type as nullable first (avoids constraint-violation on existing rows)
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'account_type')
BEGIN
    ALTER TABLE users ADD account_type NVARCHAR(50) NULL;
    UPDATE users SET account_type = 'CUSTOMER' WHERE account_type IS NULL;
    ALTER TABLE users ALTER COLUMN account_type NVARCHAR(50) NOT NULL;
END;

-- Step 2: Add google_subject_id
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'google_subject_id')
    ALTER TABLE users ADD google_subject_id NVARCHAR(255) NULL;

-- Step 3: Create unique filtered index on google_subject_id
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uq_users_google_subject' AND object_id = OBJECT_ID('users'))
BEGIN
    DECLARE @sql NVARCHAR(500) = N'CREATE UNIQUE INDEX uq_users_google_subject ON users(google_subject_id) WHERE google_subject_id IS NOT NULL';
    EXEC sp_executesql @sql;
END;

-- Step 4: Add CTP columns
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'company_name')
    ALTER TABLE users ADD company_name NVARCHAR(255) NULL;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'tax_code')
    ALTER TABLE users ADD tax_code NVARCHAR(50) NULL;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'company_address')
    ALTER TABLE users ADD company_address NVARCHAR(MAX) NULL;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'billing_email')
    ALTER TABLE users ADD billing_email NVARCHAR(255) NULL;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'ctp_status')
BEGIN
    ALTER TABLE users ADD ctp_status NVARCHAR(50) NULL;
    UPDATE users SET ctp_status = 'NOT_SUBMITTED' WHERE ctp_status IS NULL;
    ALTER TABLE users ALTER COLUMN ctp_status NVARCHAR(50) NOT NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'ctp_verified_at')
    ALTER TABLE users ADD ctp_verified_at DATETIME2 NULL;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'ctp_verified_by')
    ALTER TABLE users ADD ctp_verified_by BIGINT NULL;

-- Step 5: Create CTP audit log table
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

-- Step 6: Create indexes on users table
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_users_ctp_status' AND object_id = OBJECT_ID('users'))
    CREATE INDEX idx_users_ctp_status ON users(ctp_status);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_users_account_type' AND object_id = OBJECT_ID('users'))
    CREATE INDEX idx_users_account_type ON users(account_type);
