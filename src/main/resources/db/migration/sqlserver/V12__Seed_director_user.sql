-- Seed director account
-- Default Password: "Password123"
-- Hash: $2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'director@hotel.com')
BEGIN
    INSERT INTO users (email, password_hash, full_name, role, status, created_at, updated_at)
    VALUES ('director@hotel.com', '$2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS', N'Board Director', 'DIRECTOR', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
END;
