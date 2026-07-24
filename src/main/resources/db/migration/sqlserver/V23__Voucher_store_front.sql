-- V23__Voucher_store_front.sql (SQL Server)
-- Spec 010: Voucher Store Front

-- Extend vouchers table
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('vouchers') AND name = 'name')
    ALTER TABLE vouchers ADD name NVARCHAR(255);
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('vouchers') AND name = 'description')
    ALTER TABLE vouchers ADD description NVARCHAR(MAX);
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('vouchers') AND name = 'for_account_type')
    ALTER TABLE vouchers ADD for_account_type NVARCHAR(30) DEFAULT 'ALL';
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('vouchers') AND name = 'is_active')
    ALTER TABLE vouchers ADD is_active BIT NOT NULL DEFAULT 1;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('vouchers') AND name = 'created_by')
    ALTER TABLE vouchers ADD created_by BIGINT;

CREATE INDEX idx_vouchers_account_type ON vouchers(for_account_type);
CREATE INDEX idx_vouchers_active_dates ON vouchers(is_active, start_date, end_date) WHERE is_active = 1;

-- UserVoucher table
CREATE TABLE user_vouchers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    voucher_id BIGINT NOT NULL,
    claimed_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    is_used BIT NOT NULL DEFAULT 0,
    booking_id BIGINT,
    used_at DATETIME2,
    CONSTRAINT FK_user_vouchers_users FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_user_vouchers_vouchers FOREIGN KEY (voucher_id) REFERENCES vouchers(voucher_id) ON DELETE CASCADE,
    CONSTRAINT FK_user_vouchers_bookings FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE SET NULL,
    CONSTRAINT uq_user_voucher UNIQUE (user_id, voucher_id)
);

CREATE INDEX idx_user_vouchers_user ON user_vouchers(user_id);
CREATE INDEX idx_user_vouchers_voucher ON user_vouchers(voucher_id);
CREATE INDEX idx_user_vouchers_user_used ON user_vouchers(user_id, is_used);
