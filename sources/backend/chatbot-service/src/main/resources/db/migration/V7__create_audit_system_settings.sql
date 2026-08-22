sql
-- [DAT-010], [DAT-011], [NFR-006] Migration V7: Create audit_log and system_settings tables for chatbot service
-- [DAT-010] Define audit_log table to capture comprehensive user actions and system events
-- [DAT-011] Define system_settings table for dynamic configuration storage
-- [NFR-006] Ensure all user actions are logged for audit compliance and stored for 1 year

-- [DAT-010] Create the audit_log table with fields for unique identification, user reference, action details, and metadata
CREATE TABLE audit_log (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- [DAT-010] Primary key for audit log entry
    user_id UUID REFERENCES users(user_id) ON DELETE SET NULL, -- [DAT-010] Foreign key to users, set null on delete
    action VARCHAR(100) NOT NULL, -- [DAT-010] Action performed by user
    details JSONB, -- [DAT-010] Detailed payload as JSON
    ip_address INET, -- [DAT-010] IP address of the request
    user_agent TEXT, -- [DAT-010] User agent string
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP -- [DAT-010] Timestamp of the action
);

-- [DAT-010] Create index on user_id and timestamp for efficient query performance
CREATE INDEX idx_audit_log_user_id_timestamp ON audit_log(user_id, timestamp);

-- [DAT-011] Create the system_settings table for storing key-value configuration pairs
CREATE TABLE system_settings (
    setting_key VARCHAR(100) PRIMARY KEY, -- [DAT-011] Unique key for configuration entry
    setting_value TEXT NOT NULL, -- [DAT-011] Configuration value
    description TEXT, -- [DAT-011] Human-readable description
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP -- [DAT-011] Last update timestamp
);

-- [DAT-011] Create index on setting_key for fast lookup
CREATE INDEX idx_system_settings_key ON system_settings(setting_key);