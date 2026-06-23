-- V4: Seed staff account
-- Description: Thêm tài khoản staff mặc định phục vụ kiểm thử
-- Mật khẩu mặc định: "Password123"
-- Hash: $2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'staff@hotel.com')
BEGIN
    INSERT INTO users (email, password_hash, full_name, role, status, phone_number, identification_number, created_at, updated_at)
    VALUES ('staff@hotel.com', '$2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS', N'Hotel Staff', 'STAFF', 'ACTIVE', '0987654321', '123456789012', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
END;
