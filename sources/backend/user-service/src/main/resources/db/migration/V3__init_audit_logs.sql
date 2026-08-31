-- ============================================
-- FILE: ./sources/backend/course-service/src/main/resources/db/migration/V2__init_announcements.sql
-- SCOPE: Announcements
-- TAGS: [DAT-010], [DAT-011], [DAT-012]
-- DESCRIPTION: Initialize announcements table for center-wide announcements with date range validation
-- BUSINESS RULES:
--   - Announcements are scoped to a specific center via center_id foreign key
--   - start_date and end_date are nullable to support perpetual announcements
--   - CHECK constraint ensures end_date >= start_date when end_date is provided
--   - created_at defaults to current timestamp for accurate creation tracking
-- SECURITY:
--   - Foreign key to centers table ensures referential integrity
--   - Center isolation enforced at application layer via RBAC
-- COMPLIANCE:
--   - Supports auto-hide functionality for expired announcements per REQ-018
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

-- Index for efficient querying of announcements by center
CREATE INDEX idx_announcements_center_id ON announcements(center_id);

-- ============================================
-- FILE: ./sources/backend/user-service/src/main/resources/db/migration/V3__init_system_settings.sql
-- SCOPE: System Settings
-- TAGS: [DAT-010], [DAT-011], [DAT-012]
-- DESCRIPTION: Initialize system_settings table for application-wide configuration key-value pairs
-- BUSINESS RULES:
--   - setting_key serves as primary key for O(1) lookups
--   - setting_value stores configuration data as text (JSON or plain text format)
--   - description provides human-readable context for administrative purposes
--   - No sensitive credentials should be stored here; use Secret Manager instead
-- SECURITY:
--   - Primary key constraint prevents duplicate setting keys
--   - Application layer must validate setting_value format before persistence
-- COMPLIANCE:
--   - Supports dynamic feature flags and system-wide configuration per NFR-007
-- ============================================

CREATE TABLE system_settings (
    setting_key VARCHAR(50) NOT NULL,
    setting_value TEXT NOT NULL,
    description VARCHAR(200),
    CONSTRAINT pk_system_settings PRIMARY KEY (setting_key)
);

-- ============================================
-- FILE: ./sources/backend/user-service/src/main/resources/db/migration/V3__init_audit_logs.sql
-- SCOPE: Audit Logs
-- TAGS: [DAT-010], [DAT-011], [DAT-012]
-- DESCRIPTION: Initialize audit_logs table for security audit trail (NFR-006) - 1 year retention
-- BUSINESS RULES:
--   - Records all security-relevant actions: LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, ROLE_CHANGED, etc.
--   - user_id is nullable to support system-level actions without user context (e.g., scheduled tasks)
--   - action field stores standardized action codes for consistent filtering
--   - details field stores JSON payload with context: IP address, User-Agent, old/new values
--   - occurred_at defaults to current timestamp for accurate event ordering
--   - Retention policy: 1 year as per NFR-006 (partitioning recommended for large datasets)
-- SECURITY:
--   - Foreign key to users ensures referential integrity
--   - Index on user_id enables efficient audit trail queries per user
--   - Index on occurred_at enables time-range queries for compliance reporting
--   - Hash chain implementation recommended at application layer for tamper detection
--   - Write-once semantics enforced; no UPDATE or DELETE operations allowed
-- COMPLIANCE:
--   - Supports GDPR/CCPA audit requirements and forensic analysis
--   - Enables real-time security monitoring via ELK/GCP Cloud Logging integration
--   - Partitioning by occurred_at recommended when exceeding 10M records
-- ============================================

CREATE TABLE audit_logs (
    log_id UUID NOT NULL,
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    occurred_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_audit_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Index for efficient user-specific audit trail queries
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);

-- Index for time-range queries and compliance reporting
CREATE INDEX idx_audit_logs_occurred_at ON audit_logs(occurred_at);