-- V24__Loyalty_membership_tiers.sql (PostgreSQL)
-- Module: 011-loyalty-membership-tiers

CREATE TABLE IF NOT EXISTS loyalty_profiles (
    user_id BIGINT PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    current_tier VARCHAR(20) DEFAULT 'BRONZE',
    points INT DEFAULT 0,
    total_spent DECIMAL(18,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS loyalty_tier_configs (
    tier_name VARCHAR(20) PRIMARY KEY,
    min_points INT NOT NULL,
    min_spent DECIMAL(18,2) NOT NULL,
    discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    perk_description TEXT
);

CREATE TABLE IF NOT EXISTS loyalty_history (
    history_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    points_delta INT NOT NULL,
    spent_delta DECIMAL(18,2) DEFAULT 0.00,
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO loyalty_tier_configs (tier_name, min_points, min_spent, discount_percentage, perk_description)
VALUES
    ('BRONZE', 0, 0.00, 0.00, 'Standard membership perks'),
    ('SILVER', 500, 5000000.00, 5.00, '5% discount on all room bookings'),
    ('GOLD', 2000, 20000000.00, 10.00, '10% discount on rooms + free breakfast ticket'),
    ('PLATINUM', 5000, 50000000.00, 15.00, '15% discount + room upgrade priority + free spa access')
ON CONFLICT DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_loyalty_history_user ON loyalty_history(user_id);
CREATE INDEX IF NOT EXISTS idx_loyalty_history_created ON loyalty_history(created_at DESC);
