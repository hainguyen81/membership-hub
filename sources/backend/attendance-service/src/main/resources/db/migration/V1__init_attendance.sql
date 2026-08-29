-- [DAT-006]
-- ====================================================================================================
-- Enterprise Data Layer Migration: V1__init_attendance.sql
-- Module Subsystem: attendance-service
-- Traceability Audit Tag: [DAT-006]
-- Business Logic Context: 
--   This Flyway migration script initializes the core 'attendance' data structure for the 
--   membership-hub enterprise platform. It enforces strict relational integrity against the 
--   external 'users' and 'courses' tables while guaranteeing idempotency via a unique composite 
--   index key. This prevents duplicate attendance logs for the same student, course, and date.
--   
-- Database Constraints Enforced:
--   1. Primary Key: attendance_id (UUID generated via gen_random_uuid())
--   2. Foreign Keys: student_id references users(user_id), course_id references courses(course_id)
--   3. Check Constraint: status must strictly match ('PRESENT', 'ABSENT', 'LATE')
--   4. Idempotency Unique Constraint: uq_attendance_idempotency (student_id, course_id, attendance_date)
--   5. Performance Indexes: Optimized lookup indexes on student_id, course_id, and attendance_date
-- ====================================================================================================

-- Begin transaction block for schema migration safety
BEGIN;

-- [DAT-006] Create the attendance tracking table with secure ANSI-SQL definitions
CREATE TABLE attendance (
    -- Unique identifier for the individual attendance record entry
    attendance_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Identifier of the student scanning or registering attendance (maps to users table)
    student_id UUID NOT NULL,
    
    -- Identifier of the target course session (maps to courses table)
    course_id UUID NOT NULL,
    
    -- The specific calendar date for which the attendance applies
    attendance_date DATE NOT NULL,
    
    -- Precise timestamp when the attendance transaction was recorded
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Attendance status classification with strict domain restriction
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    
    -- Audit trail timestamp for record creation
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Enforce relational referential integrity to the global users microservice table
    CONSTRAINT fk_attendance_student 
        FOREIGN KEY (student_id) 
        REFERENCES users(user_id) 
        ON DELETE CASCADE,
        
    -- Enforce relational referential integrity to the course-service courses table
    CONSTRAINT fk_attendance_course 
        FOREIGN KEY (course_id) 
        REFERENCES courses(course_id) 
        ON DELETE CASCADE,
        
    -- Restrict status values to pre-approved business domains to prevent malformed injections
    CONSTRAINT chk_attendance_status 
        CHECK (status IN ('PRESENT', 'ABSENT', 'LATE')),
        
    -- [DAT-006] CRITICAL IDEMPOTENCY KEY: Guarantees that a student cannot check in twice 
    -- for the exact same course on the exact same date, neutralizing duplicate retry requests.
    CONSTRAINT uq_attendance_idempotency 
        UNIQUE (student_id, course_id, attendance_date)
);

-- [DAT-006] Create performance optimization index for queries filtering by student
CREATE INDEX idx_attendance_student_id 
    ON attendance (student_id);

-- [DAT-006] Create performance optimization index for queries filtering by course session
CREATE INDEX idx_attendance_course_id 
    ON attendance (course_id);

-- [DAT-006] Create performance optimization index for analytical reporting and date-range scans
CREATE INDEX idx_attendance_date 
    ON attendance (attendance_date);

-- Commit transaction block successfully upon complete execution
COMMIT;