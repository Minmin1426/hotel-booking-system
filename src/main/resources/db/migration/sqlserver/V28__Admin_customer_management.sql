-- V28__Admin_customer_management.sql (SQL Server)
-- Spec 015: Admin Customer Management

-- Add VIP fields to users
ALTER TABLE users ADD COLUMN is_vip BIT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN vip_marked_at DATETIME2;
ALTER TABLE users ADD COLUMN vip_marked_by BIGINT;

CREATE INDEX idx_users_is_vip ON users(is_vip) WHERE is_vip = 1;
CREATE INDEX idx_users_tier ON users(current_tier);
CREATE INDEX idx_users_account_type ON users(account_type);
CREATE INDEX idx_users_status ON users(status);

-- customer_notes table
CREATE TABLE customer_notes (
    note_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content NVARCHAR(MAX) NOT NULL,
    is_pinned BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_notes_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_notes_author FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_customer_notes_user ON customer_notes(user_id);

-- customer_activity_events table
CREATE TABLE customer_activity_events (
    event_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_type NVARCHAR(50) NOT NULL,
    event_summary NVARCHAR(255) NOT NULL,
    event_metadata NVARCHAR(MAX),
    actor_user_id BIGINT,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_activity_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_activity_actor FOREIGN KEY (actor_user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_activity_events_user ON customer_activity_events(user_id);
CREATE INDEX idx_activity_events_type ON customer_activity_events(event_type);
CREATE INDEX idx_activity_events_created ON customer_activity_events(created_at DESC);

-- bulk_action_logs table
CREATE TABLE bulk_action_logs (
    bulk_action_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    action_type NVARCHAR(50) NOT NULL,
    target_user_ids NVARCHAR(MAX) NOT NULL,
    payload NVARCHAR(MAX),
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_bulk_action_admin FOREIGN KEY (admin_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_bulk_action_logs_admin ON bulk_action_logs(admin_id);
CREATE INDEX idx_bulk_action_logs_created ON bulk_action_logs(created_at DESC);
