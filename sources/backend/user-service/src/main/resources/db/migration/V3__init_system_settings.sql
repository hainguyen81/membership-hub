-- ============================================
-- FILE: V3__init_system_settings.sql
-- SCOPE: System Settings
-- TAGS: [DAT-011], [DOC-001]
-- ============================================
-- ANSI SQL compliant Flyway migration for global system configuration
-- No database-specific ENUMs or proprietary syntax used
-- Aligns with audit logging requirements [NFR-006]

CREATE TABLE system_settings (
    setting_key VARCHAR(50) NOT NULL,
    setting_value TEXT NOT NULL,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_system_settings PRIMARY KEY (setting_key),
    CONSTRAINT chk_system_settings_key_not_empty CHECK (setting_key <> ''),
    CONSTRAINT chk_system_settings_value_not_empty CHECK (setting_value <> '')
);

-- Insert default system configuration values using ANSI SQL MERGE for idempotency
MERGE INTO system_settings AS target
USING (VALUES
    ('jwt.access.token.expiry', '900', 'JWT access token expiry in seconds (15 minutes)'),
    ('jwt.refresh.token.expiry', '604800', 'JWT refresh token expiry in seconds (7 days)'),
    ('max.login.attempts', '5', 'Maximum failed login attempts before account lock'),
    ('attendance.qr.code.ttl', '300', 'QR code validity period for attendance in seconds'),
    ('notification.retry.max.attempts', '3', 'Maximum retry attempts for failed notifications'),
    ('password.bcrypt.cost.factor', '12', 'Bcrypt cost factor for password hashing'),
    ('kafka.attendance.topic.partitions', '12', 'Number of partitions for attendance scan topic'),
    ('kafka.notification.topic.partitions', '6', 'Number of partitions for notification outbound topic')
) AS source (setting_key, setting_value, description)
ON target.setting_key = source.setting_key
WHEN MATCHED THEN
    UPDATE SET setting_value = source.setting_value, description = source.description, updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (setting_key, setting_value, description, created_at, updated_at)
    VALUES (source.setting_key, source.setting_value, source.description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);