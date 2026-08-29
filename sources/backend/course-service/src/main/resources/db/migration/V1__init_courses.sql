-- =================================================================================================
-- Flyway Database Migration: V1__init_courses.sql
-- Module: Course Service (org.nlh4j.membershiphub.courseservice)
-- Target Component: ./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql
-- Traceability Tag: [DAT-004]
--
-- Description:
--   Initializes the foundational 'courses' table schema for the Membership Hub microservices system.
--   Enforces relational integrity with referenced users and centers entities, applies business
--   domain date ordering constraints, and establishes optimized B-Tree indexing for query performance.
-- =================================================================================================

-- [DAT-004] Create the core 'courses' relation table with UUID primary key and audit timestamps
CREATE TABLE IF NOT EXISTS courses (
    -- [DAT-004] Primary Key: System-generated unique identifier using native cryptographic UUID v4
    course_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- [DAT-004] Business Attribute: Course title with standard length constraint (max 150 characters)
    title VARCHAR(150) NOT NULL,

    -- [DAT-004] Business Attribute: Detailed description of course syllabus, prerequisites, and learning outcomes
    description TEXT,

    -- [DAT-004] Scheduling Attribute: Official start date of the course curriculum
    start_date DATE NOT NULL,

    -- [DAT-004] Scheduling Attribute: Official completion date of the course curriculum
    end_date DATE NOT NULL,

    -- [DAT-004] Relational Attribute: Identifier of the designated instructor/teacher (references users table)
    teacher_id UUID NOT NULL,

    -- [DAT-004] Operational Attribute: Maximum student capacity per course intake (default 30 students)
    max_students INT NOT NULL DEFAULT 30,

    -- [DAT-004] Relational Attribute: Physical/logical training center hosting the course (references centers table)
    center_id UUID,

    -- [DAT-004] Audit Attribute: Immutable record creation timestamp with system timezone alignment
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- [DAT-004] Audit Attribute: Mutable record modification timestamp for optimistic concurrency auditing
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- [DAT-004] Relational Constraint: Foreign key anchoring teacher to the enterprise users ledger
    CONSTRAINT fk_courses_teacher
        FOREIGN KEY (teacher_id)
        REFERENCES users (user_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    -- [DAT-004] Relational Constraint: Foreign key anchoring course to its operating training center
    CONSTRAINT fk_courses_center
        FOREIGN KEY (center_id)
        REFERENCES centers (center_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    -- [DAT-004] Domain Integrity Constraint: Ensures logical validity where course conclusion occurs on or after inception
    CONSTRAINT chk_courses_dates
        CHECK (end_date >= start_date),

    -- [DAT-004] Capacity Integrity Constraint: Ensures non-negative and non-zero student enrollment boundary
    CONSTRAINT chk_courses_max_students
        CHECK (max_students > 0)
);

-- =================================================================================================
-- High-Performance Database Index Definitions
-- =================================================================================================

-- [DAT-004] Index: Accelerates teacher schedule queries, overlap validation checks, and teacher dashboard retrieval
CREATE INDEX IF NOT EXISTS idx_courses_teacher_id
    ON courses (teacher_id);

-- [DAT-004] Index: Optimizes multi-tenant filtering, center course catalog lookups, and reporting aggregation
CREATE INDEX IF NOT EXISTS idx_courses_center_id
    ON courses (center_id);

-- [DAT-004] Composite Index: Accelerates active course temporal queries, range scans, and enrollment eligibility windows
CREATE INDEX IF NOT EXISTS idx_courses_dates
    ON courses (start_date, end_date);