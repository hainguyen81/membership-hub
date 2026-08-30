-- ============================================
-- Flyway Migration Script: V3__init_system_settings.sql
-- Project: membership-hub
-- Service: user-service
-- Traceability Tags: [DAT-010], [DAT-011], [DAT-012]
-- SCOPE: System Settings Table Initialization
-- Description: Creates the system_settings table for storing application-wide
--              configuration parameters as key-value pairs with descriptions.
--              This enables dynamic system configuration management without
--              requiring application code changes or redeployments.
--              Supports feature flags, operational thresholds, and system metadata.
-- ============================================

-- [DAT-011] Create system_settings table
-- Business Context: Centralized configuration store for system parameters
-- Architecture: Part of user-service schema, consumed by all microservices via config queries
-- Security Note: Access to this table should be restricted to admin services only
CREATE TABLE system_settings (
    -- Primary key: unique configuration key identifier (VARCHAR 50, NOT NULL) [DAT-011]
    -- Example values: 'security.jwt.access_token_expiry', 'attendance.qr.timeout_seconds'
    setting_key VARCHAR(50) NOT NULL,
    
    -- Configuration value stored as TEXT to support JSON, numeric, boolean string representations [DAT-011]
    -- All values are stored as strings; application layer handles type casting and validation
    setting_value TEXT NOT NULL,
    
    -- Human-readable description of the configuration parameter (VARCHAR 200, nullable) [DAT-011]
    -- Used for admin UI display and documentation purposes
    description VARCHAR(200),
    
    -- Primary key constraint ensuring uniqueness and non-null setting_key [DAT-011]
    -- Automatically creates a unique btree index in PostgreSQL for fast lookups
    CONSTRAINT pk_system_settings PRIMARY KEY (setting_key)
);

-- [DAT-011] Table-level documentation for schema generation tools
-- This table implements a simple key-value store pattern for system configuration
-- No foreign keys required as this is a standalone configuration registry
-- No additional indexes needed beyond the primary key due to expected low row count (<1000 rows)

-- ============================================
-- End of Migration V3__init_system_settings.sql
-- Traceability Verification: [DAT-010], [DAT-011], [DAT-012]
-- ============================================