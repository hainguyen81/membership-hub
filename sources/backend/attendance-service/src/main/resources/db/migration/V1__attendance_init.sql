-- [ARC-000], [REQ-012]
-- ======================================================================================
-- FILE: V1__attendance_init.sql
-- SCOPE: Attendance Service - Database Schema Initialization
-- TRACEABILITY: [ARC-000] (System Architecture), [REQ-012] (QR Attendance Scan)
-- DESCRIPTION: Initial database schema migration for the attendance-service microservice.
--              Defines core tables for attendance tracking with idempotency constraints.
-- ======================================================================================

-- 1. Create the attendance table to store scan records.
-- The table uses a composite unique constraint to ensure idempotency [REQ-013].
CREATE TABLE attendance (
    attendance_id UUID PRIMARY KEY,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    -- Idempotency key to prevent duplicate scans for the same student/course/date
    idempotency_key VARCHAR(100),
    
    -- Foreign key constraints assuming users and courses tables exist in the shared schema
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT fk_attendance_course FOREIGN KEY (course_id) REFERENCES courses(course_id),
    
    -- Composite unique constraint to enforce business rule: one scan per student per course per day
    CONSTRAINT uq_attendance_unique_day UNIQUE (student_id, course_id, attendance_date)
);

-- 2. Create indexes to optimize high-frequency read/write operations.
-- Index for student-centric reporting and history lookups.
CREATE INDEX idx_attendance_student_date ON attendance(student_id, attendance_date);

-- Index for course-centric attendance reporting and dashboarding.
CREATE INDEX idx_attendance_course_date ON attendance(course_id, attendance_date);

-- 3. Create a table for notification dispatch tracking [REQ-016].
-- This table tracks the status of outbound notifications triggered by attendance events.
CREATE TABLE notification_dispatch (
    dispatch_id UUID PRIMARY KEY,
    notification_type VARCHAR(30) NOT NULL, -- e.g., PUSH, ZALO_GROUP
    target_user_id UUID,
    message_body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, DELIVERED, FAILED
    attempt_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    last_attempt_at TIMESTAMP
);

-- Index for the background worker to efficiently poll pending notifications.
CREATE INDEX idx_notification_dispatch_status ON notification_dispatch(status, created_at);

-- 4. Create a table for device token management [REQ-021].
-- Stores push notification tokens for mobile devices.
CREATE TABLE device_token (
    device_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    device_token VARCHAR(255) NOT NULL UNIQUE,
    platform VARCHAR(20) NOT NULL, -- IOS, ANDROID
    is_active BOOLEAN NOT NULL DEFAULT true,
    registered_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_device_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Index for quick lookup of active devices for a specific user.
CREATE INDEX idx_device_user_active ON device_token(user_id, is_active);