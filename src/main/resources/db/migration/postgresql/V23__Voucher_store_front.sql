-- V23__Voucher_store_front.sql (PostgreSQL)
-- Module: 010-voucher-store-front

CREATE TABLE IF NOT EXISTS user_vouchers (
    user_voucher_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    voucher_id BIGINT NOT NULL REFERENCES vouchers(voucher_id) ON DELETE CASCADE,
    claimed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'CLAIMED',
    booking_id BIGINT REFERENCES bookings(booking_id) ON DELETE SET NULL,
    CONSTRAINT uq_user_voucher UNIQUE(user_id, voucher_id)
);

ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS claim_limit_per_user INT DEFAULT 1;
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS used_count INT DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_user_vouchers_user ON user_vouchers(user_id);
CREATE INDEX IF NOT EXISTS idx_user_vouchers_voucher ON user_vouchers(voucher_id);
CREATE INDEX IF NOT EXISTS idx_user_vouchers_status ON user_vouchers(status);
