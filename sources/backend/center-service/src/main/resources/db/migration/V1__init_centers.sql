-- ============================================================================
-- FILE: V1__init_centers.sql
-- SCOPE: Centers table initialization for membership-hub system
-- TAGS: [DAT-002], [DAT-001], [DAT-008], [DAT-012], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-003], [NFR-006]
-- DESCRIPTION: Flyway migration script for Centers table with enterprise-grade
--             data integrity, multi-tenant isolation, and comprehensive audit
--             logging compliance for membership management system.
-- ============================================================================

-- ============================================================================
-- TABLE: Centers
-- PURPOSE: Stores center information for multi-center membership management
--          system with strict data validation and tenant isolation.
-- ============================================================================
CREATE TABLE centers (
    center_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tax_id VARCHAR(20) NOT NULL,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (center_id)
);

-- ============================================================================
-- CONSTRAINTS: Data integrity and business rule enforcement
-- ============================================================================

-- Unique tax identification number to prevent duplicate centers
ALTER TABLE centers
    ADD CONSTRAINT uq_centers_tax_id UNIQUE (tax_id);

-- Email format validation using standard email pattern
ALTER TABLE centers
    ADD CONSTRAINT chk_centers_email_format
        CHECK (contact_email IS NULL OR contact_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$');

-- Phone number format validation (supports international formats)
ALTER TABLE centers
    ADD CONSTRAINT chk_centers_phone_format
        CHECK (contact_phone IS NULL OR contact_phone ~ '^[+0-9 ()-]+$');

-- Tax ID numeric validation (10-13 digits for Vietnamese tax IDs)
ALTER TABLE centers
    ADD CONSTRAINT chk_centers_tax_id_numeric
        CHECK (tax_id ~ '^[0-9]{10,13}$');

-- ============================================================================
-- INDEXES: Query performance optimization for common access patterns
-- ============================================================================

-- Primary search index for center name lookups (case-insensitive)
CREATE INDEX idx_centers_name_lower ON centers(LOWER(name));

-- Tax ID lookup index for duplicate prevention and center identification
CREATE INDEX idx_centers_tax_id ON centers(tax_id);

-- Active centers listing index for dashboard and reporting
CREATE INDEX idx_centers_active_name ON centers(name) WHERE is_active = TRUE;

-- ============================================================================
-- AUDIT & COMPLIANCE: Integration with enterprise audit logging system
-- ============================================================================

-- Trigger for automatic audit log creation on center changes
CREATE OR REPLACE FUNCTION centers_audit_trigger()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO audit_logs (
        user_id,
        action,
        target_entity,
        target_id,
        old_value,
        new_value,
        ip_address,
        user_agent
    ) VALUES (
        COALESCE(NEW.center_id, OLD.center_id),
        'CENTER_' || COALESCE(
            CASE
                WHEN TG_OP = 'INSERT' THEN 'CREATED'
                WHEN TG_OP = 'UPDATE' THEN 'UPDATED'
                WHEN TG_OP = 'DELETE' THEN 'DELETED'
            END,
            'UNKNOWN'
        ),
        'centers',
        NEW.center_id,
        CASE
            WHEN TG_OP = 'UPDATE' THEN jsonb_build_object(
                'center_id', OLD.center_id,
                'name', OLD.name,
                'address', OLD.address,
                'tax_id', OLD.tax_id,
                'contact_phone', OLD.contact_phone,
                'contact_email', OLD.contact_email,
                'is_active', OLD.is_active
            )
            ELSE NULL
        END,
        CASE
            WHEN TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN jsonb_build_object(
                'center_id', NEW.center_id,
                'name', NEW.name,
                'address', NEW.address,
                'tax_id', NEW.tax_id,
                'contact_phone', NEW.contact_phone,
                'contact_email', NEW.contact_email,
                'is_active', NEW.is_active
            )
            ELSE NULL
        END,
        current_setting('app.current_ip'),
        current_setting('app.current_user_agent')
    );
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Apply audit trigger to centers table
CREATE TRIGGER centers_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON centers
    FOR EACH ROW
    EXECUTE FUNCTION centers_audit_trigger();

-- ============================================================================
-- MULTI-TENANT ISOLATION: Row-level security for center admin access
-- ============================================================================

-- Enable row-level security for centers table
ALTER TABLE centers ENABLE ROW LEVEL SECURITY;

-- Policy: System admins can access all centers
CREATE POLICY centers_policy_system_admin
    ON centers
    FOR ALL
    TO system_admin
    USING (TRUE);

-- Policy: Center admins can only access their own center
CREATE POLICY centers_policy_center_admin
    ON centers
    FOR ALL
    TO center_admin
    USING (center_id IN (
        SELECT center_id FROM center_admins
        WHERE user_id = current_user_id()
    ));

-- Policy: Public read access for authenticated users (limited fields)
CREATE POLICY centers_policy_public_read
    ON centers
    FOR SELECT
    TO authenticated
    USING (is_active = TRUE);

-- ============================================================================
-- PARTITIONING PREPARATION: For large-scale deployment
-- ============================================================================

-- Commented partition template for future scalability
-- ALTER TABLE centers
--     PARTITION BY RANGE (created_at)
--     PARTITION centers_p2024_q1 VALUES FROM ('2024-01-01') TO ('2024-04-01')
--     PARTITION centers_p2024_q2 VALUES FROM ('2024-04-01') TO ('2024-07-01')
--     PARTITION centers_p2024_q3 VALUES FROM ('2024-07-01') TO ('2024-10-01')
--     PARTITION centers_p2024_q4 VALUES FROM ('2024-10-01') TO ('2025-01-01')
--     DEFAULT PARTITION centers_default;