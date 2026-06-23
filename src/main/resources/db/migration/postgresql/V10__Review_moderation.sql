-- V10__Review_moderation.sql
-- Description: Add moderation fields to reviews table for UC-31 (Kiểm duyệt đánh giá) (PostgreSQL)

ALTER TABLE reviews ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE';
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS moderated_by BIGINT NULL;
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS moderated_at TIMESTAMP NULL;
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS moderation_reason VARCHAR(500) NULL;

CREATE INDEX IF NOT EXISTS idx_reviews_status ON reviews(status);
