-- =====================================================================
-- Enterprise Database Schema Migration Script (Flyway)
-- Module: center-service
-- Script Identifier: V1__init_centers.sql
-- Description: Initializes the primary 'centers' relational table,
--              establishes integrity constraints, regular expression
--              validations, foreign key linkages, and performance indices.
-- Traceability Tag: [DAT-003]
-- =====================================================================

-- Ensure pgcrypto or native gen_random_uuid() support is active for UUID v4 generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================================
-- Table: centers
-- Purpose: Stores physical and administrative training center entities.
-- Traceability Tag: [DAT-003]
-- =====================================================================
CREATE TABLE IF NOT EXISTS centers (
    -- Primary Key: Universally Unique Identifier (UUIDv4) to prevent enumeration attacks
    -- and ensure seamless distributed multi-service ID coordination.
    center_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Training center business display name, bounded to 100 characters to prevent buffer bloat.
    name VARCHAR(100) NOT NULL,

    -- Physical location address of the training facility, mandatory field.
    address VARCHAR(255) NOT NULL,

    -- Business / Tax Registration Identification Number:
    -- Must be strictly unique across all centers and comply with standard corporate tax ID formats.
    -- Bounded to 10 to 13 continuous decimal digits enforced via regular expression constraint.
    tax_id VARCHAR(20) UNIQUE NOT NULL,

    -- Optional direct administrative contact phone number.
    contact_phone VARCHAR(20),

    -- Optional administrative point-of-contact email address.
    contact_email VARCHAR(100),

    -- Foreign identifier referencing the administrative User assigned to manage this center.
    admin_user_id UUID,

    -- Record creation audit timestamp, automatically anchored to database server transaction time.
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Record last mutation timestamp for concurrency verification and audit tracking.
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key Constraint: Links the assigned center administrator to the core users table.
    CONSTRAINT fk_centers_admin FOREIGN KEY (admin_user_id) REFERENCES users(user_id) ON DELETE SET NULL,

    -- Check Constraint: Enforces that tax_id consists solely of 10 to 13 numerical digits [0-9],
    -- neutralizing malformed ingestion and mitigating SQL injection/format pollution vectors.
    CONSTRAINT chk_centers_taxid CHECK (tax_id ~ '^[0-9]{10,13}$')
);

-- =====================================================================
-- Performance & Lookup Indexing Strategy
-- Traceability Tag: [DAT-003]
-- =====================================================================

-- B-Tree index on tax_id to optimize frequent lookup, uniqueness checks, and reconciliation queries.
CREATE INDEX IF NOT EXISTS idx_centers_tax_id ON centers(tax_id);

-- B-Tree index on admin_user_id to accelerate foreign key join operations, filtering, and RBAC evaluations.
CREATE INDEX IF NOT EXISTS idx_centers_admin_user_id ON centers(admin_user_id);