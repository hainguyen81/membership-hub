-- [ARC-000], [REQ-012]
-- ====================================================================================
-- FILE: ./sources/backend/attendance-service/src/main/resources/db/migration/V1__attendance_init.sql
-- SCOPE: Attendance Service Database Migration - Initial Schema Setup
-- TRACEABILITY: [ARC-000] (System Architecture), [REQ-012] (QR Attendance Scan)
-- DESCRIPTION: Flyway database migration script for the attendance-service microservice.
--              Creates the core 'attendance' table with composite unique constraints 
--              to guarantee strict idempotency [REQ-013] and indexed lookup performance.
-- ====================================================================================

-- ------------------------------------------------------------------------------------
-- 1. ATTENDANCE TABLE DEFINITION
-- ------------------------------------------------------------------------------------
-- The attendance table tracks real-time QR check-ins for students attending courses.
-- A composite unique constraint on (student_id, course_id, attendance_date) ensures 
-- that a student can only be marked present once per course on any given day, 
-- fulfilling the idempotency scanning requirements [REQ-013].
-- ------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS attendance (
    -- Unique identifier for the attendance record (UUID v4)
    attendance_id UUID NOT NULL,
    
    -- Foreign key referencing the student (user_id) from the user-service domain
    student_id UUID NOT NULL,
    
    -- Foreign key referencing the course (course_id) from the course-service domain
    course_id UUID NOT NULL,
    
    -- The specific calendar date of the attendance scan (normalized to DATE type)
    attendance_date DATE NOT NULL,
    
    -- Exact timestamp when the QR code scan was processed and persisted
    timestamp TIMESTAMP NOT NULL DEFAULT clock_timestamp(),
    
    -- Idempotency key passed from the client or generated at scan time to prevent duplicates
    idempotency_key VARCHAR(100),
    
    -- Primary key constraint
    CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),
    
    -- Strict Business Constraint: Enforce idempotency per student, course, and day
    CONSTRAINT uq_attendance_student_course_date UNIQUE (student_id, course_id, attendance_date),
    
    -- Idempotency Key unique constraint if provided
    CONSTRAINT uq_attendance_idempotency_key UNIQUE (idempotency_key)
);

-- ------------------------------------------------------------------------------------
-- 2. INDEXING STRATEGY FOR HIGH-THROUGHPUT QUERIES
-- ------------------------------------------------------------------------------------
-- Create composite B-Tree index optimized for fast filtering by course and date
-- during reporting and dashboard aggregation queries.
-- ------------------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_attendance_course_date 
    ON attendance (course_id, attendance_date);

-- ------------------------------------------------------------------------------------
-- Create composite B-Tree index optimized for student history and attendance audits.
-- ------------------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_attendance_student_date 
    ON attendance (student_id, attendance_date);

-- ------------------------------------------------------------------------------------
-- Create index on idempotency key for rapid lookup during duplicate scan detection.
-- ------------------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_attendance_idempotency 
    ON attendance (idempotency_key) 
    WHERE idempotency_key IS NOT NULL;