<!-- 
  [ARC-000], [REQ-012]
  ====================================================================================
  FILE: ./sources/backend/attendance-service/src/main/resources/db/migration/V1__attendance_init.sql
  SCOPE: Attendance Service Initial Migration
  TRACEABILITY: [ARC-000] (System Architecture), [REQ-012] (QR Attendance Scan)
  DESCRIPTION: ANSI SQL DDL for attendance tracking tables with idempotency constraints,
               composite unique keys, and indexed performance optimization for QR scan
               processing in membership-hub enterprise system.
  ====================================================================================
-->

-- ============================================
-- FLYWAY MIGRATION V1: Attendance Core Tables
-- ============================================

-- Create attendance table with idempotency guarantee
-- Composite unique (student_id, course_id, attendance_date) ensures 
-- a student cannot be marked present twice for the same course on same day
CREATE TABLE attendance (
    attendance_id UUID NOT NULL,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    idempotency_key VARCHAR(100),
    CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),
    CONSTRAINT uq_attendance_student_course_date 
        UNIQUE (student_id, course_id, attendance_date),
    CONSTRAINT fk_attendance_student 
        FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_course 
        FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
);

-- Index for course-based attendance queries (reporting, dashboards)
CREATE INDEX idx_attendance_course_date 
    ON attendance (course_id, attendance_date);

-- Index for student-based attendance queries (per-student reports)
CREATE INDEX idx_attendance_student_date 
    ON attendance (student_id, attendance_date);

-- Index for idempotency key lookups (retry scenarios, network recovery)
CREATE INDEX idx_attendance_idempotency_key 
    ON attendance (idempotency_key)
    WHERE idempotency_key IS NOT NULL;

-- Grant/revoke permissions comment block
-- Application layer must ensure: 
--   - Student role can only INSERT own attendance records
--   - Center Admin can SELECT attendance for their center's courses
--   - System Admin has full read/write access across all centers

-- ============================================
-- SEQUENCE FOR AUTOMATIC ID GENERATION (PostgreSQL)
-- ============================================
-- Note: Quarkus Panache typically handles UUID generation via 
--        @GeneratedValue or custom UUID generator in entity class
--        This sequence is provided for raw SQL operations if needed
CREATE SEQUENCE attendance_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 20;

-- ============================================
-- PERFORMANCE & OPTIMIZATION NOTES
-- ============================================
-- Rationale for composite unique constraint [REQ-013]:
--   - Guarantees idempotency for QR code scanning
--   - Prevents duplicate attendance records at DB level
--   - Enables efficient upsert patterns in application layer
--
-- Index strategy:
--   - idx_attendance_course_date: Optimized for 
--     "Get attendance records for course X on date Y" queries
--   - idx_attendance_student_date: Optimized for 
--     "Get attendance history for student S" queries
--   - idx_attendance_idempotency_key: Supports retry queue 
--     mechanism [EXC-001] when network connectivity is lost
--
-- All indexes use B-tree for optimal point-query performance
-- compatible with PostgreSQL 16+ and Quarkus Hibernate Reactive.

-- End of Migration V1