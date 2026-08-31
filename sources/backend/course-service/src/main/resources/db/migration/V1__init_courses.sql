-- =====================================================================================================================
-- TRACEABILITY AUDIT METADATA [DAT-003], [DAT-004], [DAT-005]
-- SYSTEM: membership-hub | MODULE: course-service
-- PATH: ./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql
-- DESCRIPTION: Database migration script to initialize the 'courses' table with strict constraints, indexes, and FKs.
-- =====================================================================================================================

-- Enable the UUID extension to support generation of UUID v4 identifiers if not already present
-- [DAT-003] Ensures the database engine natively supports UUID primary keys
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create the 'courses' table to manage multi-center course offerings and schedules
-- [DAT-003] Establishes the core course entity with strict validation rules and relational integrity
CREATE TABLE IF NOT EXISTS courses (
    -- Unique identifier for each course, generated using UUID v4
    -- [DAT-003] Primary key constraint for entity identification
    course_id UUID NOT NULL,

    -- Title of the course, limited to 150 characters to prevent buffer overflow and optimize storage
    title VARCHAR(150) NOT NULL,

    -- Detailed description of the course syllabus, requirements, and objectives
    description TEXT,

    -- Start date of the course schedule
    start_date DATE NOT NULL,

    -- End date of the course schedule
    end_date DATE NOT NULL,

    -- Reference to the teacher (User) conducting the course
    -- [DAT-003] Foreign key referencing the users table in the user-service domain
    teacher_id UUID NOT NULL,

    -- Maximum capacity of students allowed in this course session
    -- Default is set to 30 as per business requirements
    max_students INT NOT NULL DEFAULT 30,

    -- Reference to the physical center hosting this course
    -- [DAT-003] Foreign key referencing the centers table in the center-service domain
    center_id UUID NOT NULL,

    -- Primary Key constraint to enforce uniqueness of course_id
    CONSTRAINT pk_courses PRIMARY KEY (course_id),

    -- Foreign Key constraint linking the course to a valid teacher in the users table
    -- [DAT-003] Ensures referential integrity across the user and course domains
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id),

    -- Foreign Key constraint linking the course to a valid center in the centers table
    -- [DAT-003] Ensures referential integrity across the center and course domains
    CONSTRAINT fk_courses_center FOREIGN KEY (center_id) REFERENCES centers(center_id),

    -- Check constraint to ensure the course end date is chronologically after or equal to the start date
    -- [DAT-003] Prevents logical date anomalies in scheduling
    CONSTRAINT ck_courses_date_range CHECK (end_date >= start_date),

    -- Check constraint to ensure max_students is a positive non-zero integer
    -- [DAT-003] Prevents invalid capacity configurations
    CONSTRAINT ck_courses_max_students CHECK (max_students > 0)
);

-- Comment on table and columns for database-level documentation and audit compliance
COMMENT ON TABLE courses IS 'Stores course metadata, schedules, capacities, and assignments for the membership-hub platform.';
COMMENT ON COLUMN courses.course_id IS 'Unique identifier (UUID v4) of the course.';
COMMENT ON COLUMN courses.title IS 'Title of the course (max 150 characters).';
COMMENT ON COLUMN courses.description IS 'Detailed description of the course content.';
COMMENT ON COLUMN courses.start_date IS 'The date when the course officially begins.';
COMMENT ON COLUMN courses.end_date IS 'The date when the course officially concludes.';
COMMENT ON COLUMN courses.teacher_id IS 'Reference to the teacher (User ID) assigned to this course.';
COMMENT ON COLUMN courses.max_students IS 'Maximum student capacity for the course (must be greater than 0).';
COMMENT ON COLUMN courses.center_id IS 'Reference to the center hosting this course.';

-- Create index on teacher_id to optimize queries filtering courses by teacher
-- [DAT-003] Crucial for teacher dashboard performance and schedule conflict checks
CREATE INDEX IF NOT EXISTS idx_courses_teacher_id ON courses (teacher_id);

-- Create index on start_date to optimize chronological course searches and range queries
-- [DAT-003] Speeds up active course filtering and scheduling lookups
CREATE INDEX IF NOT EXISTS idx_courses_start_date ON courses (start_date);

-- =====================================================================================================================
-- TRACEABILITY AUDIT METADATA [DAT-003], [DAT-004], [DAT-005]
-- SYSTEM: membership-hub | MODULE: attendance-service
-- PATH: ./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql
-- DESCRIPTION: Database migration script to initialize the 'enrollments' and 'attendance' tables with strict constraints, indexes, and FKs.
-- =====================================================================================================================

-- Create the 'enrollments' table to track student enrollment in courses
-- [DAT-004] Establishes the enrollment entity linking students to courses
CREATE TABLE IF NOT EXISTS enrollments (
    -- Unique identifier for each enrollment record, generated using UUID v4
    -- [DAT-004] Primary key constraint for entity identification
    enrollment_id UUID NOT NULL,

    -- Reference to the student (User) enrolling in the course
    -- [DAT-004] Foreign key referencing the users table in the user-service domain
    student_id UUID NOT NULL,

    -- Reference to the course being enrolled in
    -- [DAT-004] Foreign key referencing the courses table in the course-service domain
    course_id UUID NOT NULL,

    -- Timestamp when the enrollment was recorded
    enrollment_date TIMESTAMP NOT NULL DEFAULT now(),

    -- Primary Key constraint to enforce uniqueness of enrollment_id
    CONSTRAINT pk_enrollments PRIMARY KEY (enrollment_id),

    -- Foreign Key constraint linking enrollment to a valid student in the users table
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES users(user_id),

    -- Foreign Key constraint linking enrollment to a valid course in the courses table
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(course_id),

    -- Unique constraint to prevent duplicate enrollment of the same student in the same course
    CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id)
);

-- Comment on table and columns for database-level documentation and audit compliance
COMMENT ON TABLE enrollments IS 'Tracks student enrollment records for courses in the membership-hub platform.';
COMMENT ON COLUMN enrollments.enrollment_id IS 'Unique identifier (UUID v4) of the enrollment record.';
COMMENT ON COLUMN enrollments.student_id IS 'Reference to the student (User ID) enrolling.';
COMMENT ON COLUMN enrollments.course_id IS 'Reference to the course being enrolled in.';
COMMENT ON COLUMN enrollments.enrollment_date IS 'Timestamp when the enrollment was recorded.';

-- Create index on student_id to optimize queries filtering enrollments by student
CREATE INDEX IF NOT EXISTS idx_enrollments_student_id ON enrollments (student_id);

-- Create index on course_id to optimize queries filtering enrollments by course
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON enrollments (course_id);

-- Create the 'attendance' table to record course attendance scans
-- [DAT-005] Establishes the attendance entity linking students to course sessions
CREATE TABLE IF NOT EXISTS attendance (
    -- Unique identifier for each attendance record, generated using UUID v4
    -- [DAT-005] Primary key constraint for entity identification
    attendance_id UUID NOT NULL,

    -- Reference to the student (User) attending the course
    -- [DAT-005] Foreign key referencing the users table in the user-service domain
    student_id UUID NOT NULL,

    -- Reference to the course being attended
    -- [DAT-005] Foreign key referencing the courses table in the course-service domain
    course_id UUID NOT NULL,

    -- Date of the attendance record (used for idempotent daily scans)
    attendance_date DATE NOT NULL,

    -- Timestamp when the attendance was recorded
    timestamp TIMESTAMP NOT NULL DEFAULT now(),

    -- Primary Key constraint to enforce uniqueness of attendance_id
    CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),

    -- Foreign Key constraint linking attendance to a valid student in the users table
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES users(user_id),

    -- Foreign Key constraint linking attendance to a valid course in the courses table
    CONSTRAINT fk_attendance_course FOREIGN KEY (course_id) REFERENCES courses(course_id),

    -- Unique constraint to ensure a student can only have one attendance record per course per day
    CONSTRAINT uq_attendance_student_course_date UNIQUE (student_id, course_id, attendance_date)
);

-- Comment on table and columns for database-level documentation and audit compliance
COMMENT ON TABLE attendance IS 'Records course attendance scans for students in the membership-hub platform.';
COMMENT ON COLUMN attendance.attendance_id IS 'Unique identifier (UUID v4) of the attendance record.';
COMMENT ON COLUMN attendance.student_id IS 'Reference to the student (User ID) attending.';
COMMENT ON COLUMN attendance.course_id IS 'Reference to the course being attended.';
COMMENT ON COLUMN attendance.attendance_date IS 'Date of the attendance record (used for idempotent daily scans).';
COMMENT ON COLUMN attendance.timestamp IS 'Timestamp when the attendance was recorded.';

-- Create index on course_id and attendance_date for efficient course attendance queries
CREATE INDEX IF NOT EXISTS idx_attendance_course_date ON attendance (course_id, attendance_date);

-- Create index on student_id and attendance_date for efficient student attendance queries
CREATE INDEX IF NOT EXISTS idx_attendance_student_date ON attendance (student_id, attendance_date);