/**
 * Migration Script: V2__course_schedule_exclusion.sql
 * Target Component Path: ./sources/backend/course-service/src/main/resources/db/migration/V2__course_schedule_exclusion.sql
 * Traceability Tags: [REQ-008], [EXC-001], [DAT-001], [NFR-003]
 * Enterprise Compliance: PostgreSQL btree_gist extension and GIST exclusion constraint setup.
 */

-- Ensure btree_gist extension is available for scalar and range operator overlapping checks
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Ensure date range check constraints are in place
ALTER TABLE courses
    DROP CONSTRAINT IF EXISTS chk_courses_date_range;

ALTER TABLE courses
    ADD CONSTRAINT chk_courses_date_range 
    CHECK (end_date >= start_date);

-- Apply EXCLUDE USING gist constraint to prevent teacher schedule overlaps
-- This enforces database-level conflict detection for [REQ-008]
ALTER TABLE courses
    DROP CONSTRAINT IF EXISTS ex_teacher_schedule_no_overlap;

ALTER TABLE courses
    ADD CONSTRAINT ex_teacher_schedule_no_overlap
    EXCLUDE USING gist (
        teacher_id WITH =,
        daterange(start_date, end_date, '[]') WITH &&
    )
    WHERE (teacher_id IS NOT NULL);

-- Create supplementary indices to optimize schedule range search queries
CREATE INDEX IF NOT EXISTS idx_courses_teacher_daterange 
    ON courses (teacher_id, start_date, end_date);