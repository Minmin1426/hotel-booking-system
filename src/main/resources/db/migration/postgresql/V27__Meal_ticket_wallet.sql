-- V27__Meal_ticket_wallet.sql (PostgreSQL)
-- Module: 014-meal-ticket-wallet

CREATE TABLE IF NOT EXISTS meal_tickets (
    ticket_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    ticket_type VARCHAR(50) NOT NULL,
    qr_code_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    used_at TIMESTAMP,
    used_by_staff_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_meal_tickets_user ON meal_tickets(user_id);
CREATE INDEX IF NOT EXISTS idx_meal_tickets_status ON meal_tickets(status);
