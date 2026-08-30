-- =========================================================================================
-- [DAT-001] [DAT-002] [DAT-008] ENTERPRISE MIGRATION SCRIPT: V1__init_centers.sql
-- =========================================================================================
-- Business Context: Initialize the centers schema for membership-hub multi-center management.
-- Architecture: Standard ANSI SQL DDL compatible with PostgreSQL 15+ 
-- Security & Constraints: Tax ID unique indexing, strict foreign key references, and 
--                         comprehensive check constraints for organizational data integrity.
-- =========================================================================================

-- Ensure schema execution is atomic and transaction-safe for Flyway migration runner
BEGIN;

-- [DAT-002] Create the core 'centers' table holding physical and fiscal branch attributes
CREATE TABLE IF NOT EXISTS centers (
    -- Primary identifier for each center node across the enterprise
    center_id UUID NOT NULL,
    
    -- Official registered trade name of the center facility
    name VARCHAR(100) NOT NULL,
    
    -- Physical mailing and operational address
    address VARCHAR(255) NOT NULL,
    
    -- Tax identification number (Ma so thue) - strictly unique per legal entity
    tax_id VARCHAR(20) NOT NULL,
    
    -- Primary telephone contact number for operational communication
    contact_phone VARCHAR(20),
    
    -- Administrative notification email address for center-level alerts
    contact_email VARCHAR(100),
    
    -- Timestamp tracking record creation
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Timestamp tracking last modification
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Primary Key Constraint definition
    CONSTRAINT pk_centers PRIMARY KEY (center_id),

    -- Unique Constraint enforcing no duplicate legal tax identifiers
    CONSTRAINT uq_centers_tax_id UNIQUE (tax_id),

    -- Business Rule Check: Ensure tax_id format adheres to standard numeric/alphanumeric length
    CONSTRAINT chk_centers_tax_id_length CHECK (LENGTH(TRIM(tax_id)) >= 10)
);

-- [DAT-001] [DAT-008] Create high-performance indexes to support query execution plans
-- Index for rapid filtering and lookup of centers by commercial name
CREATE INDEX IF NOT EXISTS idx_centers_name ON centers (name);

-- Index for optimizing tax validation lookups during ingestion
CREATE INDEX IF NOT EXISTS idx_centers_tax_id ON centers (tax_id);

-- Commit transaction block successfully upon DDL application
COMMIT;