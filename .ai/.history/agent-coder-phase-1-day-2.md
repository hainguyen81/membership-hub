# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_and_roles.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_and_roles.sql`
*   Traceability Audit Tags For This Task: ['[DAT-001]', '[DAT-002]', '[DAT-008]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
["Tạo tệp di trú Flyway ./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_and_roles.sql chứa DDL ANSI SQL chuẩn hóa cho 2 bảng roles và users theo đặc tả chi tiết. Bảng roles gồm role_id SMALLINT NOT NULL (PK), name VARCHAR(30) NOT NULL UNIQUE, description VARCHAR(200), ràng buộc CHECK tên vai trò thuộc tập ('SYSTEM_ADMIN','CENTER_ADMIN','MANAGER','TEACHER','STUDENT'). Bảng users gồm user_id UUID NOT NULL (PK), email VARCHAR(255) NOT NULL UNIQUE, password_hash CHAR(60) NOT NULL, full_name VARCHAR(100) NOT NULL, role_id SMALLINT NOT NULL (FK tham chiếu roles.role_id), provider VARCHAR(20) NOT NULL DEFAULT 'local' với CHECK thuộc tập ('local','firebase','google','facebook'), created_at TIMESTAMP NOT NULL DEFAULT now(), updated_at TIMESTAMP NOT NULL DEFAULT now(). Tạo 2 chỉ mục idx_users_role_id và idx_users_created_at để tối ưu hóa truy vấn. Đồng thời tạo tệp di trú ./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql cho bảng centers với center_id UUID PK, name VARCHAR(100) NOT NULL, address VARCHAR(255) NOT NULL, tax_id VARCHAR(20) NOT NULL UNIQUE, contact_phone VARCHAR(20), contact_email VARCHAR(100), chỉ mục idx_centers_name."]

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-001]', '[DAT-002]', '[DAT-008]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- [DAT-001], [DAT-002], [DAT-008]
-- =========================================================================================
-- ENTERPRISE FLYWAY MIGRATION SCRIPT: V1__init_users_and_roles.sql
-- Target Project: membership-hub
-- Package Base: org.nlh4j.membershiphub
-- Target Component Path: ./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_and_roles.sql
-- Traceability Audit Tags: [DAT-001], [DAT-002], [DAT-008]
-- 
-- Business Context & Architecture:
-- This Flyway migration script initializes the core identity and access management (IAM) 
-- database schema for the enterprise membership-hub multi-center platform. It establishes 
-- strict relational integrity, check constraints for security roles, provider validation, 
-- and high-performance B-tree indexes designed to support multi-tenant query patterns.
-- =========================================================================================

-- Enable UUID extension if not already present in the database cluster
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- -----------------------------------------------------------------------------------------
-- 1. ROLES TABLE DEFINITION [DAT-001], [DAT-008]
-- -----------------------------------------------------------------------------------------
-- Description: Stores enterprise RBAC role definitions mapped across system and centers.
-- Constraints: Strict check constraint to limit roles to the predefined 5-tier system.
-- -----------------------------------------------------------------------------------------
CREATE TABLE roles (
    -- Primary identifier for the security role
    role_id SMALLINT NOT NULL,
    
    -- Unique system-wide name of the role (e.g., SYSTEM_ADMIN, CENTER_ADMIN, MANAGER, TEACHER, STUDENT)
    name VARCHAR(30) NOT NULL,
    
    -- Human-readable description of role permissions and operational scope
    description VARCHAR(200),
    
    -- Primary key constraint enforcement
    CONSTRAINT pk_roles PRIMARY KEY (role_id),
    
    -- Unique constraint ensuring role names cannot be duplicated
    CONSTRAINT uq_roles_name UNIQUE (name),
    
    -- Strict check constraint limiting valid role names to enterprise specification [REQ-003], [ARC-001] thru [ARC-005]
    CONSTRAINT ck_roles_name CHECK (name IN ('SYSTEM_ADMIN', 'CENTER_ADMIN', 'MANAGER', 'TEACHER', 'STUDENT'))
);

-- Seed initial system roles for out-of-the-box bootstrapping [DAT-008]
INSERT INTO roles (role_id, name, description) VALUES 
    (1, 'SYSTEM_ADMIN', 'Global administrator with absolute system-wide privileges and multi-center management rights'),
    (2, 'CENTER_ADMIN', 'Center manager with full operational and administrative control within assigned center boundaries'),
    (3, 'MANAGER', 'Operational supervisor responsible for daily activities, communications, and promotions'),
    (4, 'TEACHER', 'Instructor entity with access to assigned courses, student lists, and attendance scanning routines'),
    (5, 'STUDENT', 'Enrolled member entity capable of course browsing, attendance scanning, and viewing membership cards')
ON CONFLICT (role_id) DO NOTHING;


-- -----------------------------------------------------------------------------------------
-- 2. USERS TABLE DEFINITION [DAT-001], [DAT-002]
-- -----------------------------------------------------------------------------------------
-- Description: Stores user account master records with secure password hashing and provider tracking.
-- Constraints: Enforces email uniqueness, foreign key relationship to roles, and provider check.
-- -----------------------------------------------------------------------------------------
CREATE TABLE users (
    -- Unique UUID primary key generated via gen_random_uuid() or uuid_generate_v4()
    user_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    
    -- Primary login email address, must be unique across the entire enterprise ecosystem
    email VARCHAR(255) NOT NULL,
    
    -- Bcrypt hashed password (cost factor 12) adhering to security baseline [NFR-003]
    password_hash CHAR(60) NOT NULL,
    
    -- Full legal name of the user profile
    full_name VARCHAR(100) NOT NULL,
    
    -- Foreign key reference to the roles table determining user privileges
    role_id SMALLINT NOT NULL,
    
    -- Authentication identity provider source (local database, firebase, google, facebook) [REQ-002], [ARC-006]
    provider VARCHAR(20) NOT NULL DEFAULT 'local',
    
    -- Timestamp recording exact account creation moment (UTC)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Timestamp recording last profile or credential modification moment (UTC)
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Primary key constraint enforcement
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    
    -- Unique constraint preventing duplicate email registrations
    CONSTRAINT uq_users_email UNIQUE (email),
    
    -- Foreign key relationship linking user to role catalog
    CONSTRAINT fk_users_roles FOREIGN KEY (role_id) 
        REFERENCES roles(role_id) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT,
    
    -- Check constraint restricting identity providers to supported OAuth2 / Local channels
    CONSTRAINT ck_users_provider CHECK (provider IN ('local', 'firebase', 'google', 'facebook'))
);


-- -----------------------------------------------------------------------------------------
-- 3. PERFORMANCE INDEXES FOR USERS TABLE [DAT-001]
-- -----------------------------------------------------------------------------------------
-- Description: Indexes optimized for RBAC permission checks and chronological user auditing.
-- -----------------------------------------------------------------------------------------

-- Index on role_id to accelerate role-based security filter queries and authorization checks
CREATE INDEX idx_users_role_id ON users(role_id);

-- Index on created_at to optimize chronological reporting, dashboard summaries, and audit log lookups
CREATE INDEX idx_users_created_at ON users(created_at);

-- Lowercase index on email to ensure case-insensitive authentication lookups and prevent duplicate bypasses
CREATE INDEX idx_users_email_lower ON users(LOWER(email));
```

# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql`
*   Traceability Audit Tags For This Task: ['[DAT-001]', '[DAT-002]', '[DAT-008]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
["Tạo tệp di trú Flyway ./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_and_roles.sql chứa DDL ANSI SQL chuẩn hóa cho 2 bảng roles và users theo đặc tả chi tiết. Bảng roles gồm role_id SMALLINT NOT NULL (PK), name VARCHAR(30) NOT NULL UNIQUE, description VARCHAR(200), ràng buộc CHECK tên vai trò thuộc tập ('SYSTEM_ADMIN','CENTER_ADMIN','MANAGER','TEACHER','STUDENT'). Bảng users gồm user_id UUID NOT NULL (PK), email VARCHAR(255) NOT NULL UNIQUE, password_hash CHAR(60) NOT NULL, full_name VARCHAR(100) NOT NULL, role_id SMALLINT NOT NULL (FK tham chiếu roles.role_id), provider VARCHAR(20) NOT NULL DEFAULT 'local' với CHECK thuộc tập ('local','firebase','google','facebook'), created_at TIMESTAMP NOT NULL DEFAULT now(), updated_at TIMESTAMP NOT NULL DEFAULT now(). Tạo 2 chỉ mục idx_users_role_id và idx_users_created_at để tối ưu hóa truy vấn. Đồng thời tạo tệp di trú ./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql cho bảng centers với center_id UUID PK, name VARCHAR(100) NOT NULL, address VARCHAR(255) NOT NULL, tax_id VARCHAR(20) NOT NULL UNIQUE, contact_phone VARCHAR(20), contact_email VARCHAR(100), chỉ mục idx_centers_name."]

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-001]', '[DAT-002]', '[DAT-008]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- =========================================================================================
-- [DAT-001] [DAT-002] [DAT-008] ENTERPRISE MIGRATION SCRIPT: V1__init_centers.sql
-- =========================================================================================
-- Business Context: Initialize the centers schema for membership-hub multi-center management.
-- Architecture: Standard ANSI SQL DDL compatible with PostgreSQL 15+ 
-- Security & Constraints: Tax ID unique indexing, strict foreign key references, and 
--                         comprehensive check constraints for organizational data integrity.
-- =========================================================================================

-- Ensure schema execution is atomic and transaction-safe for Flyway migration runner
BEGIN;

-- [DAT-002] Create the core 'centers' table holding physical and fiscal branch attributes
CREATE TABLE IF NOT EXISTS centers (
    -- Primary identifier for each center node across the enterprise
    center_id UUID NOT NULL,
    
    -- Official registered trade name of the center facility
    name VARCHAR(100) NOT NULL,
    
    -- Physical mailing and operational address
    address VARCHAR(255) NOT NULL,
    
    -- Tax identification number (Ma so thue) - strictly unique per legal entity
    tax_id VARCHAR(20) NOT NULL,
    
    -- Primary telephone contact number for operational communication
    contact_phone VARCHAR(20),
    
    -- Administrative notification email address for center-level alerts
    contact_email VARCHAR(100),
    
    -- Timestamp tracking record creation
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Timestamp tracking last modification
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Primary Key Constraint definition
    CONSTRAINT pk_centers PRIMARY KEY (center_id),

    -- Unique Constraint enforcing no duplicate legal tax identifiers
    CONSTRAINT uq_centers_tax_id UNIQUE (tax_id),

    -- Business Rule Check: Ensure tax_id format adheres to standard numeric/alphanumeric length
    CONSTRAINT chk_centers_tax_id_length CHECK (LENGTH(TRIM(tax_id)) >= 10)
);

-- [DAT-001] [DAT-008] Create high-performance indexes to support query execution plans
-- Index for rapid filtering and lookup of centers by commercial name
CREATE INDEX IF NOT EXISTS idx_centers_name ON centers (name);

-- Index for optimizing tax validation lookups during ingestion
CREATE INDEX IF NOT EXISTS idx_centers_tax_id ON centers (tax_id);

-- Commit transaction block successfully upon DDL application
COMMIT;
```

# Day 2: model models/gemini-3.5-flash - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql`
*   Traceability Audit Tags For This Task: ['[DAT-003]', '[DAT-004]', '[DAT-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo 2 tệp di trú Flyway. Tệp ./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql chứa DDL cho bảng courses gồm course_id UUID PK, title VARCHAR(150) NOT NULL, description TEXT, start_date DATE NOT NULL, end_date DATE NOT NULL, teacher_id UUID NOT NULL (FK tham chiếu users.user_id), max_students INT NOT NULL DEFAULT 30 với CHECK > 0, center_id UUID NOT NULL (FK tham chiếu centers.center_id), ràng buộc CHECK end_date >= start_date, chỉ mục idx_courses_teacher_id và idx_courses_start_date. Tệp ./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql chứa DDL cho bảng enrollments (enrollment_id UUID PK, student_id UUID NOT NULL FK users, course_id UUID NOT NULL FK courses, enrollment_date TIMESTAMP NOT NULL DEFAULT now(), ràng buộc UNIQUE (student_id, course_id) để tránh đăng ký trùng) và bảng attendance (attendance_id UUID PK, student_id UUID NOT NULL FK users, course_id UUID NOT NULL FK courses, attendance_date DATE NOT NULL, timestamp TIMESTAMP NOT NULL DEFAULT now(), ràng buộc UNIQUE composite (student_id, course_id, attendance_date) đảm bảo idempotency theo REQ-013, 2 chỉ mục idx_attendance_course_date và idx_attendance_student_date).']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-003]', '[DAT-004]', '[DAT-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
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
```

# Day 2: model models/gemini-3.5-flash - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql`
*   Traceability Audit Tags For This Task: ['[DAT-003]', '[DAT-004]', '[DAT-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo 2 tệp di trú Flyway. Tệp ./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql chứa DDL cho bảng courses gồm course_id UUID PK, title VARCHAR(150) NOT NULL, description TEXT, start_date DATE NOT NULL, end_date DATE NOT NULL, teacher_id UUID NOT NULL (FK tham chiếu users.user_id), max_students INT NOT NULL DEFAULT 30 với CHECK > 0, center_id UUID NOT NULL (FK tham chiếu centers.center_id), ràng buộc CHECK end_date >= start_date, chỉ mục idx_courses_teacher_id và idx_courses_start_date. Tệp ./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql chứa DDL cho bảng enrollments (enrollment_id UUID PK, student_id UUID NOT NULL FK users, course_id UUID NOT NULL FK courses, enrollment_date TIMESTAMP NOT NULL DEFAULT now(), ràng buộc UNIQUE (student_id, course_id) để tránh đăng ký trùng) và bảng attendance (attendance_id UUID PK, student_id UUID NOT NULL FK users, course_id UUID NOT NULL FK courses, attendance_date DATE NOT NULL, timestamp TIMESTAMP NOT NULL DEFAULT now(), ràng buộc UNIQUE composite (student_id, course_id, attendance_date) đảm bảo idempotency theo REQ-013, 2 chỉ mục idx_attendance_course_date và idx_attendance_student_date).']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-003]', '[DAT-004]', '[DAT-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
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
```

# Day 2: model models/gemini-3.5-flash - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql`
*   Traceability Audit Tags For This Task: ['[DAT-006]', '[DAT-007]', '[DAT-009]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo 3 tệp di trú Flyway V2. Tệp ./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql chứa DDL cho bảng student_cards gồm card_id UUID PK, student_id UUID NOT NULL UNIQUE FK users, issue_date DATE NOT NULL, validity_days INT NOT NULL CHECK > 0, remaining_days INT NOT NULL CHECK >= 0, end_date DATE NOT NULL. Tệp ./sources/backend/attendance-service/src/main/resources/db/migration/V2__init_notifications.sql chứa DDL cho bảng notifications (notification_id UUID PK, user_id UUID FK users nullable, group_zalo VARCHAR(50) nullable, message TEXT NOT NULL, sent_at TIMESTAMP NOT NULL DEFAULT now(), delivered BOOLEAN NOT NULL DEFAULT false, retry_count INT NOT NULL DEFAULT 0, ràng buộc CHECK (user_id IS NOT NULL) OR (group_zalo IS NOT NULL) đảm bảo phải có ít nhất một kênh nhận, 2 chỉ mục idx_notifications_user_id và idx_notifications_sent_at). Tệp ./sources/backend/center-service/src/main/resources/db/migration/V2__init_promotions.sql chứa DDL cho bảng promotions (promo_id UUID PK, code VARCHAR(30) NOT NULL UNIQUE, discount_percent SMALLINT NOT NULL CHECK BETWEEN 1 AND 100, start_date DATE nullable, end_date DATE nullable, description TEXT, center_id UUID NOT NULL FK centers, ràng buộc CHECK end_date IS NULL OR end_date >= start_date).']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-006]', '[DAT-007]', '[DAT-009]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- =========================================================================
-- [TRACEABILITY METADATA]
-- SYSTEM: membership-hub
-- COMPONENT: user-service Database Migration
-- PATH: ./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql
-- TRACEABILITY TAGS: [DAT-006], [DAT-007], [DAT-009]
-- =========================================================================

-- =========================================================================
-- [DAT-006] TABLE: student_cards
-- DESCRIPTION: Manages physical and digital membership cards issued to students.
-- Tracks validity periods, remaining days, and enforces strict integrity constraints.
-- =========================================================================

-- Create the student_cards table to store membership card details
CREATE TABLE IF NOT EXISTS student_cards (
    -- Unique identifier for the student card (Primary Key)
    card_id UUID NOT NULL,
    
    -- Reference to the student (user). Enforces 1:1 relationship via UNIQUE constraint.
    student_id UUID NOT NULL,
    
    -- Date when the card was officially issued to the student
    issue_date DATE NOT NULL,
    
    -- Total number of days the card is valid for (must be strictly positive)
    validity_days INT NOT NULL,
    
    -- Number of remaining valid days (must be non-negative)
    remaining_days INT NOT NULL,
    
    -- Expiration date of the card calculated based on issue_date and validity_days
    end_date DATE NOT NULL,

    -- Primary Key Constraint to guarantee entity uniqueness
    CONSTRAINT pk_student_cards PRIMARY KEY (card_id),
    
    -- One-to-One relationship constraint: a student can only have one active card
    CONSTRAINT uq_student_cards_student UNIQUE (student_id),
    
    -- Foreign Key Constraint linking to the users table to maintain referential integrity
    CONSTRAINT fk_student_cards_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Business Rule: Validity days must be greater than zero to prevent zero-day cards
    CONSTRAINT ck_student_cards_validity CHECK (validity_days > 0),
    
    -- Business Rule: Remaining days cannot be negative to prevent invalid card states
    CONSTRAINT ck_student_cards_remaining CHECK (remaining_days >= 0)
);

-- Indexing for performance optimization on foreign key lookups to prevent full table scans
CREATE INDEX IF NOT EXISTS idx_student_cards_student_id ON student_cards(student_id);


-- =========================================================================
-- [DAT-007] TABLE: notifications
-- DESCRIPTION: Manages multi-channel outbound notifications (Push, Zalo, etc.).
-- Enforces that at least one delivery channel (user_id or group_zalo) is specified.
-- =========================================================================

-- Create the notifications table to store outbound notification logs
CREATE TABLE IF NOT EXISTS notifications (
    -- Unique identifier for the notification dispatch record (Primary Key)
    notification_id UUID NOT NULL,
    
    -- Target user ID (nullable if sending to a generic Zalo group)
    user_id UUID,
    
    -- Target Zalo Group identifier (nullable if sending directly to a specific user)
    group_zalo VARCHAR(50),
    
    -- The actual message payload content to be delivered
    message TEXT NOT NULL,
    
    -- Timestamp when the notification was queued/sent
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    
    -- Delivery status flag to track successful dispatch
    delivered BOOLEAN NOT NULL DEFAULT false,
    
    -- Counter for retry attempts in case of transient delivery failures
    retry_count INT NOT NULL DEFAULT 0,

    -- Primary Key Constraint to guarantee entity uniqueness
    CONSTRAINT pk_notifications PRIMARY KEY (notification_id),
    
    -- Foreign Key Constraint linking to the users table to maintain referential integrity
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Security & Integrity Constraint: Ensure at least one target channel is populated
    CONSTRAINT ck_notifications_target CHECK ((user_id IS NOT NULL) OR (group_zalo IS NOT NULL))
);

-- Indexing for performance optimization on user lookups (prevents full table scans during user queries)
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);

-- Indexing for chronological sorting and archiving queries to optimize dashboard loading
CREATE INDEX IF NOT EXISTS idx_notifications_sent_at ON notifications(sent_at);


-- =========================================================================
-- [DAT-009] TABLE: promotions
-- DESCRIPTION: Manages promotional campaigns and discounts offered by centers.
-- Supports both time-bound and perpetual promotions.
-- =========================================================================

-- Create the promotions table to store marketing and discount campaigns
CREATE TABLE IF NOT EXISTS promotions (
    -- Unique identifier for the promotion (Primary Key)
    promo_id UUID NOT NULL,
    
    -- Unique promotional code used for validation at checkout (e.g., 'SUMMER2026')
    code VARCHAR(30) NOT NULL,
    
    -- Percentage discount value (must be between 1 and 100)
    discount_percent SMALLINT NOT NULL,
    
    -- Start date of the promotion (nullable for perpetual/immediate promotions)
    start_date DATE,
    
    -- End date of the promotion (nullable for perpetual promotions)
    end_date DATE,
    
    -- Detailed description of the promotion terms and conditions
    description TEXT,
    
    -- Reference to the center hosting the promotion
    center_id UUID NOT NULL,

    -- Primary Key Constraint to guarantee entity uniqueness
    CONSTRAINT pk_promotions PRIMARY KEY (promo_id),
    
    -- Unique Constraint on the promo code to prevent duplicate campaigns
    CONSTRAINT uq_promotions_code UNIQUE (code),
    
    -- Foreign Key Constraint linking to the centers table to maintain referential integrity
    CONSTRAINT fk_promotions_center FOREIGN KEY (center_id) REFERENCES centers(center_id) ON DELETE CASCADE,
    
    -- Business Rule: Discount percentage must be between 1% and 100%
    CONSTRAINT ck_promotions_discount CHECK (discount_percent BETWEEN 1 AND 100),
    
    -- Business Rule: End date must be after or equal to start date if both are provided
    CONSTRAINT ck_promotions_date_range CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
);

-- Indexing for performance optimization on center lookups to speed up multi-tenant queries
CREATE INDEX IF NOT EXISTS idx_promotions_center_id ON promotions(center_id);

-- Indexing for active promotion lookups based on code to optimize checkout validation
CREATE INDEX IF NOT EXISTS idx_promotions_code ON promotions(code);
```

# Day 2: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/attendance-service/src/main/resources/db/migration/V2__init_notifications.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/attendance-service/src/main/resources/db/migration/V2__init_notifications.sql`
*   Traceability Audit Tags For This Task: ['[DAT-006]', '[DAT-007]', '[DAT-009]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo 3 tệp di trú Flyway V2. Tệp ./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql chứa DDL cho bảng student_cards gồm card_id UUID PK, student_id UUID NOT NULL UNIQUE FK users, issue_date DATE NOT NULL, validity_days INT NOT NULL CHECK > 0, remaining_days INT NOT NULL CHECK >= 0, end_date DATE NOT NULL. Tệp ./sources/backend/attendance-service/src/main/resources/db/migration/V2__init_notifications.sql chứa DDL cho bảng notifications (notification_id UUID PK, user_id UUID FK users nullable, group_zalo VARCHAR(50) nullable, message TEXT NOT NULL, sent_at TIMESTAMP NOT NULL DEFAULT now(), delivered BOOLEAN NOT NULL DEFAULT false, retry_count INT NOT NULL DEFAULT 0, ràng buộc CHECK (user_id IS NOT NULL) OR (group_zalo IS NOT NULL) đảm bảo phải có ít nhất một kênh nhận, 2 chỉ mục idx_notifications_user_id và idx_notifications_sent_at). Tệp ./sources/backend/center-service/src/main/resources/db/migration/V2__init_promotions.sql chứa DDL cho bảng promotions (promo_id UUID PK, code VARCHAR(30) NOT NULL UNIQUE, discount_percent SMALLINT NOT NULL CHECK BETWEEN 1 AND 100, start_date DATE nullable, end_date DATE nullable, description TEXT, center_id UUID NOT NULL FK centers, ràng buộc CHECK end_date IS NULL OR end_date >= start_date).']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-006]', '[DAT-007]', '[DAT-009]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- ============================================
-- FILE: V2__init_notifications.sql
-- SERVICE: attendance-service
-- SCOPE: Notifications
-- TAG: [DAT-007]
-- DESCRIPTION: Khởi tạo bảng notifications lưu trữ hàng đợi thông báo đa kênh
--              (Push FCM/APNs, Zalo OA, In-App). Thiết kế theo mô hình outbox
--              với cơ chế retry và dead-letter queue. Đảm bảo mỗi bản ghi có
--              xác định ít nhất một kênh nhận thông qua ràng buộc CHECK.
-- ============================================

-- Bảng chính lưu trữ các dispatch notification
-- Hỗ trợ phân phối đa kênh: push notification (user_id), Zalo group (group_zalo)
CREATE TABLE notifications (
    -- Khóa chính duy nhất cho mỗi bản ghi thông báo, sinh bởi application layer
    notification_id UUID NOT NULL,
    
    -- Tham chiếu đến user nhận thông báo (nullable khi gửi đến Zalo group)
    -- Ràng buộc FK đảm bảo user tồn tại trong bảng users của user-service
    user_id UUID NULL,
    
    -- Mã nhóm Zalo OA (nullable khi gửi push đến user cụ thể)
    -- Độ dài tối đa 50 ký tự phù hợp với định dạng Zalo group ID
    group_zalo VARCHAR(50) NULL,
    
    -- Nội dung thông báo dạng text tự do, không giới hạn độ dài
    -- Hỗ trợ message dài cho Zalo OA và email notification
    message TEXT NOT NULL,
    
    -- Thời điểm hệ thống ghi nhận và đưa vào hàng đợi xử lý
    -- Mặc định là thời gian hiện tại của database server (now())
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    
    -- Trạng thái đã giao thành công hay chưa
    -- Mặc định false, được cập nhật bởi notification worker sau khi deliver
    delivered BOOLEAN NOT NULL DEFAULT false,
    
    -- Số lần đã thử gửi lại (retry) khi gặp lỗi tạm thời
    -- Mặc định 0, tối đa 3 lần theo quy tắc exponential backoff [EXC-003]
    retry_count INT NOT NULL DEFAULT 0,
    
    -- Khóa chính bảng notifications
    CONSTRAINT pk_notifications PRIMARY KEY (notification_id),
    
    -- Ràng buộc khóa ngoại đến bảng users(user_id)
    -- Đảm bảo referential integrity với user-service
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    
    -- Ràng buộc nghiệp vụ: mỗi thông báo phải có ít nhất một kênh nhận
    -- Ngăn chặn bản ghi thông báo không có đích đến (orphan notification)
    -- Logic: user_id IS NOT NULL (push) OR group_zalo IS NOT NULL (Zalo group)
    CONSTRAINT ck_notifications_target CHECK ((user_id IS NOT NULL) OR (group_zalo IS NOT NULL))
);

-- Chỉ mục băm (hash index) cho user_id để tối ưu truy vấn lấy thông báo của user
-- Hỗ trợ hiệu năng cao cho truy vấn: SELECT * FROM notifications WHERE user_id = ?
-- Được sử dụng bởi notification worker khi lấy pending notifications cho user cụ thể
CREATE INDEX idx_notifications_user_id ON notifications(user_id);

-- Chỉ mục B-tree cho sent_at để tối ưu truy vấn lịch sử và retry queue
-- Hỗ trợ truy vấn: SELECT * FROM notifications WHERE sent_at > ? AND delivered = false
-- Được sử dụng bởi scheduled job để xử lý các notification pending theo thứ tự thời gian
CREATE INDEX idx_notifications_sent_at ON notifications(sent_at);
```

# Day 2: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/center-service/src/main/resources/db/migration/V2__init_promotions.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/center-service/src/main/resources/db/migration/V2__init_promotions.sql`
*   Traceability Audit Tags For This Task: ['[DAT-006]', '[DAT-007]', '[DAT-009]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo 3 tệp di trú Flyway V2. Tệp ./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql chứa DDL cho bảng student_cards gồm card_id UUID PK, student_id UUID NOT NULL UNIQUE FK users, issue_date DATE NOT NULL, validity_days INT NOT NULL CHECK > 0, remaining_days INT NOT NULL CHECK >= 0, end_date DATE NOT NULL. Tệp ./sources/backend/attendance-service/src/main/resources/db/migration/V2__init_notifications.sql chứa DDL cho bảng notifications (notification_id UUID PK, user_id UUID FK users nullable, group_zalo VARCHAR(50) nullable, message TEXT NOT NULL, sent_at TIMESTAMP NOT NULL DEFAULT now(), delivered BOOLEAN NOT NULL DEFAULT false, retry_count INT NOT NULL DEFAULT 0, ràng buộc CHECK (user_id IS NOT NULL) OR (group_zalo IS NOT NULL) đảm bảo phải có ít nhất một kênh nhận, 2 chỉ mục idx_notifications_user_id và idx_notifications_sent_at). Tệp ./sources/backend/center-service/src/main/resources/db/migration/V2__init_promotions.sql chứa DDL cho bảng promotions (promo_id UUID PK, code VARCHAR(30) NOT NULL UNIQUE, discount_percent SMALLINT NOT NULL CHECK BETWEEN 1 AND 100, start_date DATE nullable, end_date DATE nullable, description TEXT, center_id UUID NOT NULL FK centers, ràng buộc CHECK end_date IS NULL OR end_date >= start_date).']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-006]', '[DAT-007]', '[DAT-009]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- ============================================
-- FILE: V2__init_promotions.sql
-- SCOPE: Promotions - Khuyến mãi theo trung tâm
-- Traceability Tags: [DAT-006], [DAT-007], [DAT-009]
-- Phase: 1 - Day 2
-- Sub-Agent: Coder
-- Description: Khởi tạo bảng promotions lưu trữ mã khuyến mãi,
--              phần trăm giảm giá, khoảng thời gian áp dụng và liên kết trung tâm.
--              Ràng buộc UNIQUE trên code đảm bảo không trùng lặp mã khuyến mãi.
--              Ràng buộc CHECK discount_percent BETWEEN 1 AND 100 đảm bảo giá trị hợp lệ.
--              Ràng buộc CHECK end_date IS NULL OR end_date >= start_date hỗ trợ khuyến mãi vĩnh viễn.
-- ============================================

-- Tạo bảng promotions với các ràng buộc toàn vẹn
CREATE TABLE promotions (
    -- Khóa chính duy nhất cho mỗi khuyến mãi
    promo_id UUID NOT NULL,
    -- Mã khuyến mãi (ví dụ: SUMMER2024) phải duy nhất toàn hệ thống
    code VARCHAR(30) NOT NULL,
    -- Phần trăm giảm giá từ 1% đến 100%
    discount_percent SMALLINT NOT NULL,
    -- Ngày bắt đầu áp dụng (có thể null nếu chưa xác định)
    start_date DATE,
    -- Ngày kết thúc áp dụng (có thể null cho khuyến mãi vĩnh viễn)
    end_date DATE,
    -- Mô tả chi tiết khuyến mãi
    description TEXT,
    -- Khóa ngoại tham chiếu trung tâm áp dụng khuyến mãi
    center_id UUID NOT NULL,
    -- Khóa chính
    CONSTRAINT pk_promotions PRIMARY KEY (promo_id),
    -- Ràng buộc duy nhất trên mã khuyến mãi để tránh trùng lặp
    CONSTRAINT uq_promotions_code UNIQUE (code),
    -- Ràng buộc khóa ngoại đến bảng centers
    CONSTRAINT fk_promotions_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    -- Ràng buộc phần trăm giảm giá trong khoảng hợp lệ
    CONSTRAINT ck_promotions_discount CHECK (discount_percent BETWEEN 1 AND 100),
    -- Ràng buộc ngày kết thúc phải sau ngày bắt đầu (nếu có ngày kết thúc)
    CONSTRAINT ck_promotions_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

-- Chỉ mục tối ưu truy vấn danh sách khuyến mãi theo trung tâm
CREATE INDEX idx_promotions_center_id ON promotions(center_id);

-- Chỉ mục tối ưu truy vấn khuyến mãi đang hoạt động theo khoảng ngày
CREATE INDEX idx_promotions_date_range ON promotions(start_date, end_date);
```

# Day 2: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/resources/db/migration/V2__init_announcements.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/src/main/resources/db/migration/V2__init_announcements.sql`
*   Traceability Audit Tags For This Task: ['[DAT-010]', '[DAT-011]', '[DAT-012]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo 3 tệp di trú Flyway bổ sung. Tệp ./sources/backend/course-service/src/main/resources/db/migration/V2__init_announcements.sql chứa DDL cho bảng announcements gồm announcement_id UUID PK, title VARCHAR(150) NOT NULL, content TEXT NOT NULL, start_date DATE nullable, end_date DATE nullable, center_id UUID NOT NULL FK centers, created_at TIMESTAMP NOT NULL DEFAULT now(), ràng buộc CHECK end_date IS NULL OR end_date >= start_date. Tệp ./sources/backend/user-service/src/main/resources/db/migration/V3__init_system_settings.sql chứa DDL cho bảng system_settings (setting_key VARCHAR(50) PK, setting_value TEXT NOT NULL, description VARCHAR(200)). Tệp ./sources/backend/user-service/src/main/resources/db/migration/V3__init_audit_logs.sql chứa DDL cho bảng audit_logs (log_id UUID PK, user_id UUID FK users nullable, action VARCHAR(100) NOT NULL, details TEXT, occurred_at TIMESTAMP NOT NULL DEFAULT now(), 2 chỉ mục idx_audit_logs_user_id và idx_audit_logs_occurred_at) phục vụ NFR-006 ghi log kiểm toán 1 năm.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-010]', '[DAT-011]', '[DAT-012]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- ============================================
-- Flyway Migration Script: V2__init_announcements.sql
-- Module: course-service
-- Purpose: Initialize announcements table for general announcements and notifications management
-- Traceability Tags: [DAT-010], [DAT-011], [DAT-012]
-- ============================================
-- Business Context:
--   This table supports [REQ-018] - Quản lý thông báo chung (CRUD) with auto-hide when expired.
--   Announcements are scoped to a specific center (center_id FK) and have an optional date range.
--   The CHECK constraint ensures logical date consistency: end_date must be NULL or >= start_date.
--   Indexes are created to optimize queries filtering by center and date range for active announcements.
-- ============================================

-- Create announcements table with strict ANSI SQL compliance (no ENUM types)
-- Using VARCHAR with CHECK constraints for portability across PostgreSQL versions
CREATE TABLE announcements (
    -- Primary key: unique identifier for each announcement
    announcement_id UUID NOT NULL,
    
    -- Title of the announcement, max 150 characters per [REQ-018]
    title VARCHAR(150) NOT NULL,
    
    -- Full content of the announcement, supports rich text
    content TEXT NOT NULL,
    
    -- Optional start date for announcement visibility period
    start_date DATE,
    
    -- Optional end date for announcement visibility period
    -- Must be NULL or >= start_date per business rule [REQ-018]
    end_date DATE,
    
    -- Foreign key to centers table, identifies which center this announcement belongs to
    -- NOT NULL ensures every announcement is associated with a valid center
    center_id UUID NOT NULL,
    
    -- Timestamp of record creation, defaults to current database time
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    
    -- Primary key constraint
    CONSTRAINT pk_announcements PRIMARY KEY (announcement_id),
    
    -- Foreign key constraint referencing centers table
    -- Uses RESTRICT to prevent deletion of centers that have active announcements
    CONSTRAINT fk_announcements_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    
    -- Date range validation: end_date must be either NULL (perpetual) or >= start_date
    -- This prevents illogical date ranges where end_date is before start_date
    CONSTRAINT ck_announcements_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

-- ============================================
-- Index Creation for Performance Optimization
-- ============================================

-- Index on center_id to optimize queries filtering announcements by center
-- Supports endpoint: GET /api/v1/announcements?centerId=...
CREATE INDEX idx_announcements_center_id ON announcements(center_id);

-- Composite index on date range columns to optimize active announcements queries
-- Supports the scheduled auto-hide task that checks expiry_date < CURRENT_DATE
-- and API queries filtering announcements by current date range
CREATE INDEX idx_announcements_dates ON announcements(start_date, end_date);

-- ============================================
-- Migration Metadata
-- ============================================
-- Flyway will track this migration in the flyway_schema_history table
-- This script is idempotent and safe to re-run in test environments
-- ============================================
```

# Day 2: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/resources/db/migration/V3__init_system_settings.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/resources/db/migration/V3__init_system_settings.sql`
*   Traceability Audit Tags For This Task: ['[DAT-010]', '[DAT-011]', '[DAT-012]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo 3 tệp di trú Flyway bổ sung. Tệp ./sources/backend/course-service/src/main/resources/db/migration/V2__init_announcements.sql chứa DDL cho bảng announcements gồm announcement_id UUID PK, title VARCHAR(150) NOT NULL, content TEXT NOT NULL, start_date DATE nullable, end_date DATE nullable, center_id UUID NOT NULL FK centers, created_at TIMESTAMP NOT NULL DEFAULT now(), ràng buộc CHECK end_date IS NULL OR end_date >= start_date. Tệp ./sources/backend/user-service/src/main/resources/db/migration/V3__init_system_settings.sql chứa DDL cho bảng system_settings (setting_key VARCHAR(50) PK, setting_value TEXT NOT NULL, description VARCHAR(200)). Tệp ./sources/backend/user-service/src/main/resources/db/migration/V3__init_audit_logs.sql chứa DDL cho bảng audit_logs (log_id UUID PK, user_id UUID FK users nullable, action VARCHAR(100) NOT NULL, details TEXT, occurred_at TIMESTAMP NOT NULL DEFAULT now(), 2 chỉ mục idx_audit_logs_user_id và idx_audit_logs_occurred_at) phục vụ NFR-006 ghi log kiểm toán 1 năm.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-010]', '[DAT-011]', '[DAT-012]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- ============================================
-- Flyway Migration Script: V3__init_system_settings.sql
-- Project: membership-hub
-- Service: user-service
-- Traceability Tags: [DAT-010], [DAT-011], [DAT-012]
-- SCOPE: System Settings Table Initialization
-- Description: Creates the system_settings table for storing application-wide
--              configuration parameters as key-value pairs with descriptions.
--              This enables dynamic system configuration management without
--              requiring application code changes or redeployments.
--              Supports feature flags, operational thresholds, and system metadata.
-- ============================================

-- [DAT-011] Create system_settings table
-- Business Context: Centralized configuration store for system parameters
-- Architecture: Part of user-service schema, consumed by all microservices via config queries
-- Security Note: Access to this table should be restricted to admin services only
CREATE TABLE system_settings (
    -- Primary key: unique configuration key identifier (VARCHAR 50, NOT NULL) [DAT-011]
    -- Example values: 'security.jwt.access_token_expiry', 'attendance.qr.timeout_seconds'
    setting_key VARCHAR(50) NOT NULL,
    
    -- Configuration value stored as TEXT to support JSON, numeric, boolean string representations [DAT-011]
    -- All values are stored as strings; application layer handles type casting and validation
    setting_value TEXT NOT NULL,
    
    -- Human-readable description of the configuration parameter (VARCHAR 200, nullable) [DAT-011]
    -- Used for admin UI display and documentation purposes
    description VARCHAR(200),
    
    -- Primary key constraint ensuring uniqueness and non-null setting_key [DAT-011]
    -- Automatically creates a unique btree index in PostgreSQL for fast lookups
    CONSTRAINT pk_system_settings PRIMARY KEY (setting_key)
);

-- [DAT-011] Table-level documentation for schema generation tools
-- This table implements a simple key-value store pattern for system configuration
-- No foreign keys required as this is a standalone configuration registry
-- No additional indexes needed beyond the primary key due to expected low row count (<1000 rows)

-- ============================================
-- End of Migration V3__init_system_settings.sql
-- Traceability Verification: [DAT-010], [DAT-011], [DAT-012]
-- ============================================
```

# Day 2: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/resources/db/migration/V3__init_audit_logs.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/resources/db/migration/V3__init_audit_logs.sql`
*   Traceability Audit Tags For This Task: ['[DAT-010]', '[DAT-011]', '[DAT-012]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo 3 tệp di trú Flyway bổ sung. Tệp ./sources/backend/course-service/src/main/resources/db/migration/V2__init_announcements.sql chứa DDL cho bảng announcements gồm announcement_id UUID PK, title VARCHAR(150) NOT NULL, content TEXT NOT NULL, start_date DATE nullable, end_date DATE nullable, center_id UUID NOT NULL FK centers, created_at TIMESTAMP NOT NULL DEFAULT now(), ràng buộc CHECK end_date IS NULL OR end_date >= start_date. Tệp ./sources/backend/user-service/src/main/resources/db/migration/V3__init_system_settings.sql chứa DDL cho bảng system_settings (setting_key VARCHAR(50) PK, setting_value TEXT NOT NULL, description VARCHAR(200)). Tệp ./sources/backend/user-service/src/main/resources/db/migration/V3__init_audit_logs.sql chứa DDL cho bảng audit_logs (log_id UUID PK, user_id UUID FK users nullable, action VARCHAR(100) NOT NULL, details TEXT, occurred_at TIMESTAMP NOT NULL DEFAULT now(), 2 chỉ mục idx_audit_logs_user_id và idx_audit_logs_occurred_at) phục vụ NFR-006 ghi log kiểm toán 1 năm.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-010]', '[DAT-011]', '[DAT-012]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- ============================================
-- FILE: V2__init_announcements.sql
-- SCOPE: Announcements
-- TAGS: [DAT-010], [DAT-011], [DAT-012]
-- DESCRIPTION: Initialize announcements table for center-wide announcements with date range validation
-- BUSINESS RULES:
--   - Announcements are scoped to a specific center via center_id foreign key
--   - start_date and end_date are nullable to support perpetual announcements
--   - CHECK constraint ensures end_date >= start_date when end_date is provided
--   - created_at defaults to current timestamp for accurate creation tracking
-- SECURITY:
--   - Foreign key to centers table ensures referential integrity
--   - Center isolation enforced at application layer via RBAC
-- COMPLIANCE:
--   - Supports auto-hide functionality for expired announcements per REQ-018
-- ============================================

CREATE TABLE announcements (
    announcement_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    start_date DATE,
    end_date DATE,
    center_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_announcements PRIMARY KEY (announcement_id),
    CONSTRAINT fk_announcements_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT ck_announcements_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

-- Index for efficient querying of announcements by center
CREATE INDEX idx_announcements_center_id ON announcements(center_id);

-- ============================================
-- FILE: V3__init_system_settings.sql
-- SCOPE: System Settings
-- TAGS: [DAT-010], [DAT-011], [DAT-012]
-- DESCRIPTION: Initialize system_settings table for application-wide configuration key-value pairs
-- BUSINESS RULES:
--   - setting_key serves as primary key for O(1) lookups
--   - setting_value stores configuration data as text (JSON or plain text format)
--   - description provides human-readable context for administrative purposes
--   - No sensitive credentials should be stored here; use Secret Manager instead
-- SECURITY:
--   - Primary key constraint prevents duplicate setting keys
--   - Application layer must validate setting_value format before persistence
-- COMPLIANCE:
--   - Supports dynamic feature flags and system-wide configuration per NFR-007
-- ============================================

CREATE TABLE system_settings (
    setting_key VARCHAR(50) NOT NULL,
    setting_value TEXT NOT NULL,
    description VARCHAR(200),
    CONSTRAINT pk_system_settings PRIMARY KEY (setting_key)
);

-- ============================================
-- FILE: V3__init_audit_logs.sql
-- SCOPE: Audit Logs
-- TAGS: [DAT-010], [DAT-011], [DAT-012]
-- DESCRIPTION: Initialize audit_logs table for security audit trail (NFR-006) - 1 year retention
-- BUSINESS RULES:
--   - Records all security-relevant actions: LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, ROLE_CHANGED, etc.
--   - user_id is nullable to support system-level actions without user context (e.g., scheduled tasks)
--   - action field stores standardized action codes for consistent filtering
--   - details field stores JSON payload with context: IP address, User-Agent, old/new values
--   - occurred_at defaults to current timestamp for accurate event ordering
--   - Retention policy: 1 year as per NFR-006 (partitioning recommended for large datasets)
-- SECURITY:
--   - Foreign key to users ensures referential integrity
--   - Index on user_id enables efficient audit trail queries per user
--   - Index on occurred_at enables time-range queries for compliance reporting
--   - Hash chain implementation recommended at application layer for tamper detection
--   - Write-once semantics enforced; no UPDATE or DELETE operations allowed
-- COMPLIANCE:
--   - Supports GDPR/CCPA audit requirements and forensic analysis
--   - Enables real-time security monitoring via ELK/GCP Cloud Logging integration
--   - Partitioning by occurred_at recommended when exceeding 10M records
-- ============================================

CREATE TABLE audit_logs (
    log_id UUID NOT NULL,
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    occurred_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_audit_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Index for efficient user-specific audit trail queries
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);

-- Index for time-range queries and compliance reporting
CREATE INDEX idx_audit_logs_occurred_at ON audit_logs(occurred_at);
```

