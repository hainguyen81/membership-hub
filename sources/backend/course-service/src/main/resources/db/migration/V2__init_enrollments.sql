-- [DAT-005]
-- =====================================================================
-- Enterprise Database Migration Script: V2__init_enrollments.sql
-- Project: membership-hub
-- Target Subsystem: course-service
-- Traceability Audit Tag: [DAT-005]
-- Business Logic Context: Initializes the 'enrollments' table to track 
-- student course registrations, enforcing strict relational integrity,
-- status checks, and composite uniqueness to prevent duplicate sign-ups.
-- Security & Performance: Leverages UUID primary keys, parameterized 
-- foreign keys, ANSI-compliant CHECK constraints, and targeted indexes 
-- to optimize join operations and search performance under high concurrency.
-- =====================================================================

-- Set search path to ensure DDL statements execute in the public schema securely
SET search_path TO public;

-- Drop table if exists to ensure idempotent re-runs in non-production test harnesses
-- (In production, Flyway handles versioned migration history tracking automatically)
-- [DAT-005]
DROP TABLE IF EXISTS enrollments CASCADE;

-- Create the enrollments relational data store
-- [DAT-005]
CREATE TABLE enrollments (
    -- Primary Key: Unique Universally Unique Identifier generated via cryptographic random function
    enrollment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Foreign Key Reference to the core Users system domain (student identity)
    student_id UUID NOT NULL,
    
    -- Foreign Key Reference to the Course system domain (course offering)
    course_id UUID NOT NULL,
    
    -- Timestamp capturing the exact moment the enrollment contract was established
    enrollment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Status attribute reflecting the operational lifecycle state of the student's enrollment
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    -- Audit trail columns for enterprise data governance and tracking
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Relational Integrity Constraints: Enforce referential mapping to existing parent entities
    -- [DAT-005]
    CONSTRAINT fk_enrollments_student 
        FOREIGN KEY (student_id) 
        REFERENCES users(user_id) 
        ON DELETE CASCADE,
        
    -- [DAT-005]
    CONSTRAINT fk_enrollments_course 
        FOREIGN KEY (course_id) 
        REFERENCES courses(course_id) 
        ON DELETE CASCADE,

    -- Domain Enumeration Constraint: Restrict status values to approved business states
    -- [DAT-005]
    CONSTRAINT chk_enrollments_status 
        CHECK (status IN ('ACTIVE', 'DROPPED', 'COMPLETED')),

    -- Idempotency & Business Uniqueness Rule: A student can only enroll in a specific course once
    -- [DAT-005]
    CONSTRAINT uq_enrollments_student_course 
        UNIQUE (student_id, course_id)
);

-- =====================================================================
-- Performance Optimization Indexes
-- [DAT-005]
-- =====================================================================

-- Index for high-performance lookup of enrollments filtered by student identity
-- [DAT-005]
CREATE INDEX idx_enrollments_student_id 
    ON enrollments(student_id);

-- Index for high-performance lookup of enrollments filtered by course offering
-- [DAT-005]
CREATE INDEX idx_enrollments_course_id 
    ON enrollments(course_id);

-- Index for rapid filtering and reporting based on enrollment operational status
-- [DAT-005]
CREATE INDEX idx_enrollments_status 
    ON enrollments(status);

-- =====================================================================
-- Trigger for Automatic Timestamp Maintenance (Audit Governance)
-- [DAT-005]
-- =====================================================================

-- Create or replace the function to automatically update the 'updated_at' timestamp column
-- [DAT-005]
CREATE OR REPLACE FUNCTION update_enrollment_timestamp_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Attach the update trigger to the enrollments table
-- [DAT-005]
CREATE TRIGGER trg_update_enrollments_modtime
    BEFORE UPDATE ON enrollments
    FOR EACH ROW
    EXECUTE FUNCTION update_enrollment_timestamp_column();