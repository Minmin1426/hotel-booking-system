-- V26__Wallet_topup_spending_limits.sql (SQL Server)
-- Spec 013: Wallet Top-up & Spending Limits

-- ── TopUpConfig ──────────────────────────────────────────────────────────────────
CREATE TABLE topup_configs (
    config_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    wallet_id BIGINT NOT NULL,
    enabled BIT NOT NULL DEFAULT 0,
    threshold_amount DECIMAL(18, 2) NOT NULL DEFAULT 0,
    topup_amount DECIMAL(18, 2) NOT NULL,
    payment_method_id NVARCHAR(255),
    max_daily_auto_topup INT NOT NULL DEFAULT 5,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_topup_configs_users FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_topup_configs_wallets FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    CONSTRAINT UQ_topup_configs_wallet UNIQUE (wallet_id)
);

CREATE INDEX idx_topup_configs_wallet ON topup_configs(wallet_id);
CREATE INDEX idx_topup_configs_enabled ON topup_configs(wallet_id, enabled) WHERE enabled = 1;

-- ── TopUpHistory ────────────────────────────────────────────────────────────────
CREATE TABLE topup_history (
    history_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    payment_method NVARCHAR(30) NOT NULL DEFAULT 'STRIPE',
    stripe_session_id NVARCHAR(255),
    status NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_auto_topup BIT NOT NULL DEFAULT 0,
    failure_reason NVARCHAR(MAX),
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_topup_history_wallets FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE
);

CREATE INDEX idx_topup_history_wallet ON topup_history(wallet_id);
CREATE INDEX idx_topup_history_status ON topup_history(status);
CREATE INDEX idx_topup_history_created ON topup_history(created_at DESC);

-- ── SpendingLimit ────────────────────────────────────────────────────────────────
CREATE TABLE spending_limits (
    limit_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    group_id BIGINT NOT NULL,
    member_user_id BIGINT NOT NULL,
    per_transaction_limit DECIMAL(18, 2),
    daily_limit DECIMAL(18, 2),
    monthly_limit DECIMAL(18, 2),
    effective_from DATE NOT NULL DEFAULT CAST(SYSDATETIME() AS DATE),
    effective_until DATE,
    created_by BIGINT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_spending_limits_groups FOREIGN KEY (group_id) REFERENCES groups(group_id) ON DELETE CASCADE,
    CONSTRAINT FK_spending_limits_users_member FOREIGN KEY (member_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_spending_limits_users_creator FOREIGN KEY (created_by) REFERENCES users(user_id),
    CONSTRAINT UQ_spending_limits_group_member UNIQUE (group_id, member_user_id)
);

CREATE INDEX idx_spending_limits_member ON spending_limits(member_user_id);
CREATE INDEX idx_spending_limits_group ON spending_limits(group_id);

-- ── SpendingLimitHistory ────────────────────────────────────────────────────────
CREATE TABLE spending_limit_history (
    history_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    limit_id BIGINT NOT NULL,
    previous_limit DECIMAL(18, 2),
    new_limit DECIMAL(18, 2),
    changed_by BIGINT NOT NULL,
    reason NVARCHAR(MAX),
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_spending_limit_history_limits FOREIGN KEY (limit_id) REFERENCES spending_limits(limit_id) ON DELETE CASCADE,
    CONSTRAINT FK_spending_limit_history_users FOREIGN KEY (changed_by) REFERENCES users(user_id)
);

CREATE INDEX idx_spending_limit_history_limit ON spending_limit_history(limit_id);

-- ── SpendingTracking ────────────────────────────────────────────────────────────
CREATE TABLE spending_tracking (
    tracking_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    period_type NVARCHAR(10) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_spent DECIMAL(18, 2) NOT NULL DEFAULT 0,
    CONSTRAINT FK_spending_tracking_users FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_spending_tracking_groups FOREIGN KEY (group_id) REFERENCES groups(group_id) ON DELETE CASCADE,
    CONSTRAINT UQ_spending_tracking UNIQUE (user_id, group_id, period_type, period_start),
    CONSTRAINT CK_spending_tracking_period_type CHECK (period_type IN ('DAILY', 'MONTHLY'))
);

CREATE INDEX idx_spending_tracking_user ON spending_tracking(user_id, group_id);
CREATE INDEX idx_spending_tracking_period ON spending_tracking(period_type, period_start, period_end);
