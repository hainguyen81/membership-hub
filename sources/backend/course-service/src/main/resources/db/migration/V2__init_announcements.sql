-- ============================================
-- Flyway Migration Script: V2__init_announcements.sql
-- Module: course-service
-- Purpose: Initialize announcements table for general announcements and notifications management
-- Traceability Tags: [DAT-010], [DAT-011], [DAT-012]
-- ============================================
-- Business Context:
--   This table supports [REQ-018] - Quản lý thông báo chung (CRUD) with auto-hide when expired.
--   Announcements are scoped to a specific center (center_id FK) and have an optional date range.
--   The CHECK constraint ensures logical date consistency: end_date must be NULL or >= start_date.
--   Indexes are created to optimize queries filtering by center and date range for active announcements.
-- ============================================

CREATE TABLE announcements (
    announcement_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    start_date DATE,
    end_date DATE,
    center_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_announcements PRIMARY KEY (announcement_id),
    CONSTRAINT fk_announcements_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT ck_announcements_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_announcements_center_id ON announcements(center_id);
CREATE INDEX idx_announcements_dates ON announcements(start_date, end_date);

-- ============================================
-- Flyway Migration Script: V3__init_system_settings.sql
-- Module: user-service
-- Purpose: Initialize system_settings table for global configuration parameters
-- Traceability Tags: [DAT-010], [DAT-011], [DAT-012]
-- ============================================
-- Business Context:
--   This table stores key-value configuration settings for the application (e.g., feature flags, thresholds).
--   Used by various services to retrieve dynamic settings without code changes.
--   Each setting has a unique key, a non‑null value, and an optional description for documentation.
-- ============================================

CREATE TABLE system_settings (
    setting_key VARCHAR(50) NOT NULL,
    setting_value TEXT NOT NULL,
    description VARCHAR(200),
    CONSTRAINT pk_system_settings PRIMARY KEY (setting_key)
);

CREATE INDEX idx_system_settings_key ON system_settings(setting_key);

-- ============================================
-- Flyway Migration Script: V3__init_audit_logs.sql
-- Module: user-service
-- Purpose: Initialize audit_logs table for comprehensive audit trail and compliance logging
-- Traceability Tags: [DAT-010], [DAT-011], [DAT-012]
-- ============================================
-- Business Context:
--   This table captures all critical business actions for compliance, forensic analysis, and reporting.
--   Supports [NFR-006] – audit log retention for 1 year, enabling GDPR/CCPA audit requirements.
--   Each log entry includes user context (if available), action performed, optional details, and timestamp.
--   Indexes on user_id and occurred_at accelerate query filtering for user‑specific or time‑range reports.
-- ============================================

CREATE TABLE audit_logs (
    log_id UUID NOT NULL,
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    occurred_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_audit_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT ck_audit_logs_action CHECK (action IN (
        'LOGIN_SUCCESS','LOGIN_FAILED','LOGOUT','ROLE_CHANGED','CENTER_ASSIGNED',
        'CENTER_UNASSIGNED','COURSE_ENROLLED','ATTENDANCE_RECORDED','CARD_RENEWED',
        'NOTIFICATION_SENT','PROMOTION_APPLIED','ANNOUNCEMENT_PUBLISHED','SYSTEM_SETTING_UPDATED'
    ))
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_occurred_at ON audit_logs(occurred_at);