-- V24__Extend_payment_and_voucher_features.sql
-- Description: Mở rộng tính năng payments, vouchers và thêm bảng payouts (SQL Server)

-- 1. Thêm các cột vào bảng payments
ALTER TABLE payments ADD is_deposit BIT NOT NULL DEFAULT 0;
ALTER TABLE payments ADD deposit_ratio DECIMAL(5,2) NOT NULL DEFAULT 1.0;
ALTER TABLE payments ADD countdown_end_time DATETIME2;
ALTER TABLE payments ADD meal_refund_amount DECIMAL(18,2);
ALTER TABLE payments ADD invoice_company_name NVARCHAR(255);
ALTER TABLE payments ADD invoice_tax_id VARCHAR(50);
ALTER TABLE payments ADD invoice_company_address NVARCHAR(500);
ALTER TABLE payments ADD invoice_company_email VARCHAR(255);

-- 2. Thêm các cột vào bảng vouchers
ALTER TABLE vouchers ADD voucher_type VARCHAR(50) NOT NULL DEFAULT 'ROOM';
ALTER TABLE vouchers ADD combo_meal_benefit VARCHAR(255);

-- 3. Tạo bảng payouts cho đối soát đối tác
CREATE TABLE payouts (
    payout_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    period_start DATETIME2 NOT NULL,
    period_end DATETIME2 NOT NULL,
    total_revenue DECIMAL(18,2) NOT NULL,
    commission_rate DECIMAL(5,2) NOT NULL,
    payout_amount DECIMAL(18,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payouts_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(hotel_id)
);

CREATE INDEX idx_payouts_hotel_id ON payouts(hotel_id);
