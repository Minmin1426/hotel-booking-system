-- V28__Admin_customer_management.sql (PostgreSQL)
-- Module: 015-admin-customer-management

CREATE TABLE IF NOT EXISTS customer_activity_logs (
    log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    activity_type VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_customer_logs_user ON customer_activity_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_customer_logs_created ON customer_activity_logs(created_at DESC);
