-- V26__Wallet_topup_spending_limits.sql (PostgreSQL)
-- Module: 013-wallet-topup-spending-limits

CREATE TABLE IF NOT EXISTS wallet_topup_methods (
    method_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS wallet_spending_limits (
    limit_id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES customer_wallets(wallet_id) ON DELETE CASCADE,
    daily_limit DECIMAL(18,2),
    monthly_limit DECIMAL(18,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_wallet_spending_wallet ON wallet_spending_limits(wallet_id);
