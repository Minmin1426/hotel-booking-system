-- V27__Meal_ticket_wallet.sql (SQL Server)
-- Spec 014: Meal Ticket Wallet

-- ── MealTicketType ─────────────────────────────────────────────────────────────
CREATE TABLE meal_ticket_types (
    type_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(30) NOT NULL UNIQUE,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(MAX),
    default_valid_days INT NOT NULL DEFAULT 30,
    default_price DECIMAL(18, 2) NOT NULL DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);

INSERT INTO meal_ticket_types (code, name, description, default_valid_days, default_price) VALUES
('BREAKFAST_BUFFET', 'Breakfast Buffet', 'Complimentary breakfast buffet at the main restaurant', 1, 350000),
('LUNCH_BUFFET', 'Lunch Buffet', 'Complimentary lunch buffet', 1, 550000),
('DINNER_BUFFET', 'Dinner Buffet', 'Complimentary dinner buffet', 1, 750000),
('ROOM_SERVICE', 'Room Service', 'Room service meal voucher', 1, 900000),
('MINIBAR_VOUCHER', 'Minibar Voucher', 'Minibar credit voucher', 7, 200000),
('SPA_VOUCHER', 'Spa Voucher', 'Spa treatment voucher', 30, 1500000);

-- ── MealTicket ─────────────────────────────────────────────────────────────────
CREATE TABLE meal_tickets (
    ticket_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    booking_id BIGINT,
    ticket_type NVARCHAR(30) NOT NULL,
    qr_code NVARCHAR(255) NOT NULL UNIQUE,
    qr_signature NVARCHAR(64) NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'UNUSED',
    issued_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    expires_at DATETIME2 NOT NULL,
    used_at DATETIME2,
    consumed_by_staff_id BIGINT,
    issued_by BIGINT,
    notes NVARCHAR(MAX),
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_meal_tickets_users FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_meal_tickets_bookings FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE SET NULL,
    CONSTRAINT FK_meal_tickets_types FOREIGN KEY (ticket_type) REFERENCES meal_ticket_types(code),
    CONSTRAINT FK_meal_tickets_staff FOREIGN KEY (consumed_by_staff_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT FK_meal_tickets_issuer FOREIGN KEY (issued_by) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_meal_tickets_user ON meal_tickets(user_id);
CREATE INDEX idx_meal_tickets_status ON meal_tickets(status);
CREATE INDEX idx_meal_tickets_expires ON meal_tickets(expires_at);
CREATE INDEX idx_meal_tickets_user_status ON meal_tickets(user_id, status);
CREATE INDEX idx_meal_tickets_booking ON meal_tickets(booking_id);

-- ── MealTicketAuditLog ─────────────────────────────────────────────────────────
CREATE TABLE meal_ticket_audit_log (
    audit_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    action NVARCHAR(20) NOT NULL,
    actor_user_id BIGINT,
    metadata NVARCHAR(MAX),
    timestamp DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_audit_tickets FOREIGN KEY (ticket_id) REFERENCES meal_tickets(ticket_id) ON DELETE CASCADE,
    CONSTRAINT FK_audit_users FOREIGN KEY (actor_user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_ticket ON meal_ticket_audit_log(ticket_id);
CREATE INDEX idx_audit_timestamp ON meal_ticket_audit_log(timestamp DESC);
