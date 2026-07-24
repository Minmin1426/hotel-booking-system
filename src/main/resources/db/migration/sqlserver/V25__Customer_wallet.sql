-- V25__Customer_wallet.sql (SQL Server variant)
-- Description: Customer wallet — groups, group_memberships, wallets, wallet_transactions
-- Module: 012-customer-wallet

-- 1. Groups (corporate groups)
CREATE TABLE [groups] (
    group_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    group_name NVARCHAR(255) NOT NULL,
    owner_user_id BIGINT NOT NULL,
    tax_code NVARCHAR(50),
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_groups_owner FOREIGN KEY (owner_user_id) REFERENCES users(user_id) ON DELETE NO ACTION
);

CREATE INDEX idx_groups_owner_user_id ON [groups](owner_user_id);

-- 2. Group memberships
CREATE TABLE group_memberships (
    membership_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    group_id BIGINT NOT NULL,
    member_user_id BIGINT NOT NULL,
    spending_limit DECIMAL(18,2) DEFAULT 0,
    joined_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_group_memberships_group FOREIGN KEY (group_id) REFERENCES [groups](group_id) ON DELETE CASCADE,
    CONSTRAINT fk_group_memberships_member FOREIGN KEY (member_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_group_member UNIQUE (group_id, member_user_id)
);

CREATE INDEX idx_group_memberships_group_id ON group_memberships(group_id);
CREATE INDEX idx_group_memberships_member_user_id ON group_memberships(member_user_id);

-- 3. Wallets
CREATE TABLE wallets (
    wallet_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    wallet_type NVARCHAR(20) NOT NULL, -- 'PERSONAL' | 'GROUP'
    group_id BIGINT NULL,
    balance DECIMAL(18,2) NOT NULL DEFAULT 0,
    currency NVARCHAR(10) NOT NULL DEFAULT 'VND',
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE' | 'FROZEN' | 'CLOSED'
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallets_owner FOREIGN KEY (owner_user_id) REFERENCES users(user_id) ON DELETE NO ACTION,
    CONSTRAINT fk_wallets_group FOREIGN KEY (group_id) REFERENCES [groups](group_id) ON DELETE SET NULL,
    CONSTRAINT uq_wallet_owner_type_group UNIQUE (owner_user_id, wallet_type, group_id),
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0)
);

CREATE INDEX idx_wallets_owner_user_id ON wallets(owner_user_id);
CREATE INDEX idx_wallets_group_id ON wallets(group_id);
CREATE INDEX idx_wallets_type ON wallets(wallet_type);
CREATE INDEX idx_wallets_status ON wallets(status);

-- 4. Wallet transactions
CREATE TABLE wallet_transactions (
    transaction_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    type NVARCHAR(20) NOT NULL, -- 'DEPOSIT' | 'PAYMENT' | 'REFUND' | 'ADJUSTMENT'
    amount DECIMAL(18,2) NOT NULL, -- Positive for credit, negative for debit
    balance_before DECIMAL(18,2) NOT NULL,
    balance_after DECIMAL(18,2) NOT NULL,
    related_booking_id BIGINT,
    status NVARCHAR(20) NOT NULL DEFAULT 'SUCCESS', -- 'PENDING' | 'SUCCESS' | 'FAILED'
    payment_method NVARCHAR(50), -- 'STRIPE' | 'BANK_TRANSFER' | null
    description NVARCHAR(MAX),
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_transactions_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    CONSTRAINT fk_wallet_transactions_booking FOREIGN KEY (related_booking_id) REFERENCES bookings(booking_id) ON DELETE NO ACTION
);

CREATE INDEX idx_wallet_transactions_wallet_id ON wallet_transactions(wallet_id);
CREATE INDEX idx_wallet_transactions_type ON wallet_transactions(type);
CREATE INDEX idx_wallet_transactions_created_at ON wallet_transactions(created_at DESC);
CREATE INDEX idx_wallet_transactions_related_booking ON wallet_transactions(related_booking_id);
