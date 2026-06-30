-- V15: Seed receptionist and housekeeper accounts
-- Description: Thêm tài khoản receptionist và housekeeper phục vụ kiểm thử và phân quyền mới
-- Mật khẩu mặc định: "Password123"

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'receptionist@hotel.com')
BEGIN
    INSERT INTO users (email, password_hash, full_name, role, status, phone_number, identification_number, created_at, updated_at)
    VALUES ('receptionist@hotel.com', '$2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS', N'Hotel Receptionist', 'RECEPTIONIST', 'ACTIVE', '0987654322', '123456789013', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
END;

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'housekeeper@hotel.com')
BEGIN
    INSERT INTO users (email, password_hash, full_name, role, status, phone_number, identification_number, created_at, updated_at)
    VALUES ('housekeeper@hotel.com', '$2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS', N'Hotel Housekeeper', 'HOUSEKEEPER', 'ACTIVE', '0987654323', '123456789014', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
END;
