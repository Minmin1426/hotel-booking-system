-- V10__Review_moderation.sql
-- Description: Add moderation fields to reviews table for UC-31 (Kiểm duyệt đánh giá)

-- Trạng thái review: VISIBLE | HIDDEN
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('reviews') AND name = 'status')
BEGIN
    ALTER TABLE reviews ADD status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE';
END;

-- Admin nào đã kiểm duyệt
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('reviews') AND name = 'moderated_by')
BEGIN
    ALTER TABLE reviews ADD moderated_by BIGINT NULL;
END;

-- Thời điểm kiểm duyệt
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('reviews') AND name = 'moderated_at')
BEGIN
    ALTER TABLE reviews ADD moderated_at DATETIME2 NULL;
END;

-- Lý do ẩn/xóa (audit log)
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('reviews') AND name = 'moderation_reason')
BEGIN
    ALTER TABLE reviews ADD moderation_reason NVARCHAR(500) NULL;
END;

-- Index for fast status filter
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('reviews') AND name = 'idx_reviews_status')
BEGIN
    CREATE INDEX idx_reviews_status ON reviews(status);
END;
