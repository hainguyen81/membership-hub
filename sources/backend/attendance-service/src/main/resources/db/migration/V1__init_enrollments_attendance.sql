-- ============================================================================
-- PROJECT: membership-hub
-- COMPONENT PATH: ./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql
-- TRACEABILITY TAGS: [DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [DAT-012], [REQ-013]
-- DESCRIPTION: Flyway migration script initializing enrollments and attendance tables
--              with strict ANSI SQL compliance, FK referential integrity, and
--              composite unique idempotency constraints.
-- ============================================================================

-- Table: enrollments
-- Purpose: Manages student course enrollments ensuring no duplicate registrations per course.
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id UUID NOT NULL,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    enrollment_date TIMESTAMP NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT pk_enrollments PRIMARY KEY (enrollment_id),
    CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id)
);

CREATE INDEX IF NOT EXISTS idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON enrollments(course_id);

-- Table: attendance
-- Purpose: Records student attendance per course and date, enforcing composite uniqueness
--          to guarantee idempotency for QR scan requests [REQ-013].
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id UUID NOT NULL,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),
    CONSTRAINT uq_attendance_student_course_date UNIQUE (student_id, course_id, attendance_date)
);

CREATE INDEX IF NOT EXISTS idx_attendance_course_date ON attendance(course_id, attendance_date);
CREATE INDEX IF NOT EXISTS idx_attendance_student_date ON attendance(student_id, attendance_date);