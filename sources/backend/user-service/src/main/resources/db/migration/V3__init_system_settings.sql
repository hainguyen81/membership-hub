-- ============================================
-- Flyway Migration Script: V2__init_announcements.sql
-- Project: membership-hub
-- Service: course-service
-- Traceability Tags: [DAT-010], [DAT-011], [DAT-012]
-- SCOPE: Announcements Table Initialization
-- Description: Creates the announcements table for system-wide broadcast messages.
-- ============================================
CREATE TABLE announcements (
    announcement_id UUID PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    start_date DATE,
    end_date DATE,
    center_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_announcements_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT ck_announcements_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);
-- ============================================
-- End of Migration V2__init_announcements.sql
-- Traceability Verification: [DAT-010], [DAT-011], [DAT-012]
-- ============================================

-- ============================================
-- Flyway Migration Script: V3__init_system_settings.sql
-- Project: membership-hub
-- Service: user-service
-- Traceability Tags: [DAT-010], [DAT-011], [DAT-012]
-- SCOPE: System Settings Table Initialization
-- Description: Creates the system_settings table for storing application-wide
--              configuration parameters as key-value pairs with descriptions.
-- ============================================
CREATE TABLE system_settings (
    setting_key VARCHAR(50) NOT NULL,
    setting_value TEXT NOT NULL,
    description VARCHAR(200),
    CONSTRAINT pk_system_settings PRIMARY KEY (setting_key)
);
-- ============================================
-- End of Migration V3__init_system_settings.sql
-- Traceability Verification: [DAT-010], [DAT-011], [DAT-012]
-- ============================================

-- ============================================
-- Flyway Migration Script: V3__init_audit_logs.sql
-- Project: membership-hub
-- Service: user-service
-- Traceability Tags: [DAT-010], [DAT-011], [DAT-012]
-- SCOPE: Audit Logs Table Initialization
-- Description: Creates the audit_logs table for storing system audit events.
-- ============================================
CREATE TABLE audit_logs (
    log_id UUID PRIMARY KEY,
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    occurred_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_audit_logs_user_id (user_id),
    INDEX idx_audit_logs_occurred_at (occurred_at)
);
-- ============================================
-- End of Migration V3__init_audit_logs.sql
-- Traceability Verification: [DAT-010], [DAT-011], [DAT-012]
-- ============================================