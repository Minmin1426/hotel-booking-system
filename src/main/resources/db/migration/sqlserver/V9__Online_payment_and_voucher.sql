-- V9__Online_payment_and_voucher.sql
-- Description: Thêm các bảng và cột cho tính năng Online Payment, Hoàn tiền (SPEC-034) và Voucher (SPEC-035)

-- 1. SPEC-013 & SPEC-034 & SPEC-035: Cập nhật cấu trúc bảng bookings và payments
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'payment_status')
BEGIN
    ALTER TABLE bookings ADD payment_status VARCHAR(50) DEFAULT 'PENDING';
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('payments') AND name = 'gateway')
BEGIN
    ALTER TABLE payments ADD gateway VARCHAR(50);
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('payments') AND name = 'payment_time')
BEGIN
    ALTER TABLE payments ADD payment_time DATETIME2;
END;

-- Tạo index unique cho transaction_id (đã được tạo ở V8)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('payments') AND name = 'idx_payments_transaction_id')
BEGIN
    CREATE UNIQUE INDEX idx_payments_transaction_id ON payments(transaction_id) WHERE transaction_id IS NOT NULL;
END;

-- 2. SPEC-034: Thêm cột cho hoàn tiền vào payments
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('payments') AND name = 'refund_amount')
BEGIN
    ALTER TABLE payments ADD refund_amount DECIMAL(18,2);
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('payments') AND name = 'refund_time')
BEGIN
    ALTER TABLE payments ADD refund_time DATETIME2;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('payments') AND name = 'refund_status')
BEGIN
    ALTER TABLE payments ADD refund_status VARCHAR(50);
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('payments') AND name = 'refund_transaction_id')
BEGIN
    ALTER TABLE payments ADD refund_transaction_id VARCHAR(100);
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('payments') AND name = 'refund_retry_count')
BEGIN
    ALTER TABLE payments ADD refund_retry_count INT DEFAULT 0;
END;

-- 3. SPEC-035: Tạo bảng vouchers
IF OBJECT_ID('vouchers', 'U') IS NULL
BEGIN
    CREATE TABLE vouchers (
        voucher_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        code VARCHAR(50) NOT NULL UNIQUE,
        discount_type VARCHAR(20) NOT NULL, -- PERCENTAGE, FIXED_AMOUNT
        discount_value DECIMAL(18,2) NOT NULL,
        min_booking_value DECIMAL(18,2) DEFAULT 0,
        start_date DATETIME2,
        end_date DATETIME2,
        max_usage INT DEFAULT 0, -- 0 means unlimited
        current_usage INT DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
    CREATE INDEX idx_vouchers_code ON vouchers(code);
END;

-- 4. SPEC-035: Thêm cột voucher cho bookings
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'voucher_id')
BEGIN
    ALTER TABLE bookings ADD voucher_id BIGINT;
    ALTER TABLE bookings ADD CONSTRAINT fk_bookings_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(voucher_id);
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'discount_amount')
BEGIN
    ALTER TABLE bookings ADD discount_amount DECIMAL(18,2) DEFAULT 0;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'final_price')
BEGIN
    ALTER TABLE bookings ADD final_price DECIMAL(18,2);
END;

-- 5. Tạo bảng payment_audit_logs
IF OBJECT_ID('payment_audit_logs', 'U') IS NULL
BEGIN
    CREATE TABLE payment_audit_logs (
        log_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        transaction_id VARCHAR(100),
        action VARCHAR(100) NOT NULL,
        request_payload NVARCHAR(MAX),
        response_payload NVARCHAR(MAX),
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
    CREATE INDEX idx_payment_audit_logs_transaction_id ON payment_audit_logs(transaction_id);
END;

-- Cập nhật final_price cho các booking cũ
EXEC('UPDATE bookings SET final_price = total_amount WHERE final_price IS NULL');
