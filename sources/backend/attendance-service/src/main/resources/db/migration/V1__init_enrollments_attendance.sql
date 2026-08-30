-- =================================================================================================
-- DATABASE MIGRATION SCRIPT: V1__init_enrollments_attendance.sql
-- TARGET PATH: ./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql
-- TARGET PROJECT: membership-hub
-- TRACEABILITY TAGS: [DAT-003], [DAT-004], [DAT-005]
-- DESCRIPTION: Initializes the database schema for the Attendance Service, including the
--              'enrollments' and 'attendance' tables with strict constraints, indexes, and
--              idempotency guarantees.
-- =================================================================================================

-- [DAT-004] Create the 'enrollments' table to manage student course registrations.
-- This table links students (users) to courses and prevents duplicate registrations.
CREATE TABLE IF NOT EXISTS enrollments (
    -- Unique identifier for each enrollment record (UUID format)
    enrollment_id UUID NOT NULL,
    
    -- Reference to the student (user) ID from the user-service/users table [DAT-004]
    student_id UUID NOT NULL,
    
    -- Reference to the course ID from the course-service/courses table [DAT-003]
    course_id UUID NOT NULL,
    
    -- Timestamp indicating when the student registered for the course
    enrollment_date TIMESTAMP NOT NULL DEFAULT now(),
    
    -- Primary Key constraint to enforce uniqueness of the enrollment ID
    CONSTRAINT pk_enrollments PRIMARY KEY (enrollment_id),
    
    -- Unique constraint to prevent a student from registering for the same course multiple times [DAT-004]
    CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id)
);

-- Create index on student_id to optimize queries filtering by student
CREATE INDEX IF NOT EXISTS idx_enrollments_student_id ON enrollments(student_id);

-- Create index on course_id to optimize queries filtering by course
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON enrollments(course_id);


-- [DAT-005] Create the 'attendance' table to record QR-based attendance scans.
-- This table enforces strict daily idempotency per student per course.
CREATE TABLE IF NOT EXISTS attendance (
    -- Unique identifier for each attendance record (UUID format)
    attendance_id UUID NOT NULL,
    
    -- Reference to the student (user) ID who scanned the QR code [DAT-005]
    student_id UUID NOT NULL,
    
    -- Reference to the course ID for which attendance is being recorded [DAT-003]
    course_id UUID NOT NULL,
    
    -- The specific calendar date of the attendance (YYYY-MM-DD)
    attendance_date DATE NOT NULL,
    
    -- Precise timestamp of when the QR scan transaction occurred
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    
    -- Primary Key constraint to enforce uniqueness of the attendance ID
    CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),
    
    -- Composite Unique constraint to guarantee idempotency [REQ-013], [DAT-005]
    -- Prevents duplicate attendance records for the same student, course, and date
    CONSTRAINT uq_attendance_student_course_date UNIQUE (student_id, course_id, attendance_date)
);

-- [DAT-005] Create index on course_id and attendance_date to optimize course-level daily reports
CREATE INDEX IF NOT EXISTS idx_attendance_course_date ON attendance(course_id, attendance_date);

-- [DAT-005] Create index on student_id and attendance_date to optimize student-level history lookups
CREATE INDEX IF NOT EXISTS idx_attendance_student_date ON attendance(student_id, attendance_date);