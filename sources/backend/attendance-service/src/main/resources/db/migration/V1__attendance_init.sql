-- [ARC-000], [REQ-012]
-- =====================================================================================
-- FILE: ./sources/backend/attendance-service/src/main/resources/db/migration/V1__attendance_init.sql
-- SCOPE: Database Schema Migration for Attendance Service Microservice
-- TRACEABILITY: [ARC-000] (System Architecture), [REQ-012] (QR Attendance Scan & Validation)
-- DESCRIPTION: Flyway database migration script establishing tables, constraints, 
--              composite unique indexes for idempotency, foreign key relationships, 
--              and performance optimization indexes for the attendance microservice.
-- =====================================================================================

-- [REQ-012] Set statement timeout to prevent long-running locks during schema deployment
SET statement_timeout = 30000;

-- [REQ-012] Enable UUID extension if not already present globally in the database instance
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================================================
-- TABLE: attendance
-- PURPOSE: Stores real-time QR attendance scan records for students enrolled in courses.
-- TRACEABILITY: [REQ-012], [ARC-000], [DAT-004]
-- =====================================================================================
CREATE TABLE IF NOT EXISTS attendance (
    -- Primary unique identifier for the attendance scan record (UUID v4)
    attendance_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    
    -- Reference to the student user recording attendance (must exist in users table)
    student_id UUID NOT NULL,
    
    -- Reference to the course being attended (must exist in courses table)
    course_id UUID NOT NULL,
    
    -- Calendar date on which attendance is recorded (used for daily constraint enforcement)
    attendance_date DATE NOT NULL,
    
    -- Precise timestamp when the QR code scan was successfully processed and logged
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique idempotency key supplied by the client mobile app to prevent duplicate submissions
    idempotency_key VARCHAR(100) NULL,
    
    -- Status flag indicating processing state (RECORDED, DUPLICATE_SKIPPED, FAILED)
    status VARCHAR(30) NOT NULL DEFAULT 'RECORDED',
    
    -- Primary Key Constraint
    CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),
    
    -- Composite Unique Constraint ensuring strict idempotency per student, course, and date
    CONSTRAINT uq_attendance_student_course_date UNIQUE (student_id, course_id, attendance_date),
    
    -- Idempotency Key Unique Constraint across the entire table if provided
    CONSTRAINT uq_attendance_idempotency_key UNIQUE (idempotency_key)
);

-- =====================================================================================
-- INDEXES: attendance table performance tuning
-- TRACEABILITY: [REQ-012], [NFR-001] (P95 < 200ms latency requirement)
-- =====================================================================================

-- Index for high-speed lookup and filtering by course and date (frequently used by reporting)
CREATE INDEX IF NOT EXISTS idx_attendance_course_date 
    ON attendance (course_id, attendance_date);

-- Index for student attendance history queries and dashboard counters
CREATE INDEX IF NOT EXISTS idx_attendance_student_date 
    ON attendance (student_id, attendance_date);

-- Index for idempotency verification lookups on high ingestion throughput
CREATE INDEX IF NOT EXISTS idx_attendance_idempotency 
    ON attendance (idempotency_key) 
    WHERE idempotency_key IS NOT NULL;

-- =====================================================================================
-- TABLE: attendance_retry_queue
-- PURPOSE: Dead-letter and retry queue buffer for offline attendance synchronization
-- TRACEABILITY: [EXC-001], [EXC-005], [REQ-012]
-- =====================================================================================
CREATE TABLE IF NOT EXISTS attendance_retry_queue (
    -- Unique queue entry identifier
    queue_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    
    -- Original client-side idempotency key
    idempotency_key VARCHAR(100) NOT NULL,
    
    -- Encoded or raw QR payload string submitted during offline mode
    qr_payload TEXT NOT NULL,
    
    -- Number of delivery retry attempts executed so far
    attempt_count INT NOT NULL DEFAULT 0,
    
    -- Maximum permitted retry attempts before moving to permanent failure state
    max_attempts INT NOT NULL DEFAULT 3,
    
    -- Timestamp of the last failed processing attempt
    last_attempt_at TIMESTAMP NULL,
    
    -- Current processing status of the queued offline payload
    queue_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    
    -- Error message captured during the last failed attempt
    last_error_message TEXT NULL,
    
    -- Record creation timestamp
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Primary Key Constraint
    CONSTRAINT pk_attendance_retry_queue PRIMARY KEY (queue_id),
    
    -- Unique constraint on idempotency key within retry queue
    CONSTRAINT uq_retry_queue_idempotency UNIQUE (idempotency_key),
    
    -- Status validation check constraint
    CONSTRAINT chk_retry_queue_status CHECK (queue_status IN ('PENDING', 'PROCESSING', 'RETRY_SCHEDULED', 'COMPLETED', 'DEAD_LETTER'))
);

-- =====================================================================================
-- INDEXES: attendance_retry_queue performance and FIFO processing
-- TRACEABILITY: [EXC-005] (FIFO recovery order)
-- =====================================================================================

-- Index for fetching pending queue items ordered strictly by creation timestamp (FIFO enforcement)
CREATE INDEX IF NOT EXISTS idx_retry_queue_fifo 
    ON attendance_retry_queue (created_at ASC) 
    WHERE queue_status IN ('PENDING', 'RETRY_SCHEDULED');

-- =====================================================================================
-- MIGRATION COMPLETION LOGGING
-- TRACEABILITY: [ARC-000]
-- =====================================================================================
DO $$
BEGIN
    RAISE NOTICE 'Migration V1__attendance_init.sql executed successfully. Attendance tables and indexes established.';
END $$;