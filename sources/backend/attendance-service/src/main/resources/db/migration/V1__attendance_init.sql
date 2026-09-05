-- [ARC-000], [REQ-012]
-- ======================================================================================
-- FILE: V1__attendance_init.sql
-- SCOPE: Attendance Service - Core Schema Initialization
-- TRACEABILITY: [ARC-000] (System Architecture), [REQ-012] (QR Attendance Scan)
-- DESCRIPTION: Initializes the attendance tracking schema including idempotency support.
-- ======================================================================================

-- Enable UUID extension for primary key generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Table: attendance
-- Stores individual attendance records for students in specific courses.
-- Idempotency is enforced via a composite unique constraint on (student_id, course_id, attendance_date).
CREATE TABLE attendance (
    attendance_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    idempotency_key VARCHAR(100),
    
    -- Foreign key constraints assuming users and courses tables exist in the shared schema
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT fk_attendance_course FOREIGN KEY (course_id) REFERENCES courses(course_id),
    
    -- Idempotency constraint: Prevents duplicate attendance records for the same student/course/day
    CONSTRAINT uq_attendance_unique_day UNIQUE (student_id, course_id, attendance_date)
);

-- Index: idx_attendance_student_date
-- Optimizes queries for student attendance history reports.
CREATE INDEX idx_attendance_student_date ON attendance(student_id, attendance_date);

-- Index: idx_attendance_course_date
-- Optimizes queries for course-wide attendance reports.
CREATE INDEX idx_attendance_course_date ON attendance(course_id, attendance_date);

-- Table: attendance_retry_queue
-- Stores failed attendance scan events for retry logic [EXC-001].
CREATE TABLE attendance_retry_queue (
    retry_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payload JSONB NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Index: idx_retry_queue_status
-- Optimizes background worker polling for pending retries.
CREATE INDEX idx_retry_queue_status ON attendance_retry_queue(status, next_retry_at);