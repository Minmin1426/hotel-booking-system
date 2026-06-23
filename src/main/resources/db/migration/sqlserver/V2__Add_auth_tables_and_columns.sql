-- V2__Add_auth_tables_and_columns.sql
-- Description: Thêm cột/bảng cho tính năng xác thực, đăng xuất, lưu log login và reset mật khẩu

-- 1. Bổ sung các cột cho bảng users
ALTER TABLE users ADD failed_login_attempts INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD last_login_at DATETIME2 NULL;
ALTER TABLE users ADD last_logout_at DATETIME2 NULL;

-- 2. Tạo bảng login_audit_logs ghi nhận lịch sử đăng nhập
CREATE TABLE login_audit_logs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_login_audit_logs_email ON login_audit_logs(email);

-- 3. Tạo bảng revoked_tokens cho cơ chế đăng xuất (blacklist tokens)
CREATE TABLE revoked_tokens (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    token VARCHAR(1000) NOT NULL UNIQUE,
    token_type VARCHAR(50) NOT NULL, -- ACCESS hoặc REFRESH
    email VARCHAR(255) NOT NULL,
    expiry_time DATETIME2 NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_revoked_tokens_token ON revoked_tokens(token);

-- 4. Tạo bảng password_reset_tokens cho cơ chế quên/đổi mật khẩu
CREATE TABLE password_reset_tokens (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_time DATETIME2 NOT NULL,
    used BIT NOT NULL DEFAULT 0,
    user_id BIGINT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);

-- Seed tài khoản mặc định (nếu chưa có) sử dụng BCrypt strength 12
-- Mật khẩu mặc định: "Password123"
-- Hash: $2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@hotel.com')
BEGIN
    INSERT INTO users (email, password_hash, full_name, role, status, created_at, updated_at)
    VALUES ('admin@hotel.com', '$2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS', N'System Admin', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
END;

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'customer@hotel.com')
BEGIN
    INSERT INTO users (email, password_hash, full_name, role, status, created_at, updated_at)
    VALUES ('customer@hotel.com', '$2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS', N'Default Customer', 'CUSTOMER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
END;
