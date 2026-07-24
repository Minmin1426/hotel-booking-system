-- V25__Customer_wallet.sql (PostgreSQL)
-- Module: 012-customer-wallet

CREATE TABLE IF NOT EXISTS customer_wallets (
    wallet_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS wallet_transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES customer_wallets(wallet_id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    balance_after DECIMAL(18,2) NOT NULL,
    reference_id VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_wallet_user ON customer_wallets(user_id);
CREATE INDEX IF NOT EXISTS idx_wallet_tx_wallet ON wallet_transactions(wallet_id);
CREATE INDEX IF NOT EXISTS idx_wallet_tx_created ON wallet_transactions(created_at DESC);
