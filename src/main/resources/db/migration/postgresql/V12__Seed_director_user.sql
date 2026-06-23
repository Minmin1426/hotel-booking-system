-- V12__Seed_director_user.sql
-- Default Password: "Password123"
-- Hash: $2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS

INSERT INTO users (email, password_hash, full_name, role, status, created_at, updated_at)
VALUES ('director@hotel.com', '$2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS', 'Board Director', 'DIRECTOR', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;
