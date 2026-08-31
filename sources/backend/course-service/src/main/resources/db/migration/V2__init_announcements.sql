-- ============================================
-- FILE: V2__init_announcements.sql
-- SCOPE: Announcements
-- SERVICE: course-service
-- TAG ID: [DAT-010], [REQ-018]
-- ============================================

CREATE TABLE announcements (
    announcement_id UUID NOT NULL,
    center_id UUID NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    start_date DATE NULL,
    expiry_date DATE NULL,
    target_audience VARCHAR(20) NOT NULL DEFAULT 'ALL',
    published_by UUID NOT NULL,
    published_at TIMESTAMP NOT NULL DEFAULT now(),
    is_active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT pk_announcements PRIMARY KEY (announcement_id),
    CONSTRAINT fk_announcement_center FOREIGN KEY (center_id) REFERENCES centers(center_id) ON DELETE SET NULL,
    CONSTRAINT fk_announcement_publisher FOREIGN KEY (published_by) REFERENCES users(user_id) ON DELETE RESTRICT,
    CONSTRAINT chk_announcement_target CHECK (target_audience IN ('ALL', 'STUDENT', 'TEACHER', 'ADMIN')),
    CONSTRAINT chk_announcement_dates CHECK (expiry_date IS NULL OR expiry_date >= start_date)
);

-- Indexes for query patterns
CREATE INDEX idx_announcements_center_id ON announcements(center_id);
CREATE INDEX idx_announcements_active_expiry ON announcements(is_active, expiry_date);
CREATE INDEX idx_announcements_published_at ON announcements(published_at);