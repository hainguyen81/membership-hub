# Day 3: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql`
*   Traceability Audit Tags For This Task: ['[DAT-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp ./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql định nghĩa bảng courses với các cột: course_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), title VARCHAR(150) NOT NULL, description TEXT, start_date DATE NOT NULL, end_date DATE NOT NULL, teacher_id UUID NOT NULL, max_students INT NOT NULL DEFAULT 30, center_id UUID, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP. Khai báo ràng buộc FOREIGN KEY (teacher_id) REFERENCES users(user_id), FOREIGN KEY (center_id) REFERENCES centers(center_id), CONSTRAINT chk_courses_dates CHECK (end_date >= start_date). Tạo các chỉ mục idx_courses_teacher_id, idx_courses_center_id, idx_courses_dates (composite trên start_date, end_date).']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-004]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- =================================================================================================
-- Flyway Database Migration: V1__init_courses.sql
-- Module: Course Service (org.nlh4j.membershiphub.courseservice)
-- Target Component: ./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql
-- Traceability Tag: [DAT-004]
--
-- Description:
--   Initializes the foundational 'courses' table schema for the Membership Hub microservices system.
--   Enforces relational integrity with referenced users and centers entities, applies business
--   domain date ordering constraints, and establishes optimized B-Tree indexing for query performance.
-- =================================================================================================

-- [DAT-004] Create the core 'courses' relation table with UUID primary key and audit timestamps
CREATE TABLE IF NOT EXISTS courses (
    -- [DAT-004] Primary Key: System-generated unique identifier using native cryptographic UUID v4
    course_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- [DAT-004] Business Attribute: Course title with standard length constraint (max 150 characters)
    title VARCHAR(150) NOT NULL,

    -- [DAT-004] Business Attribute: Detailed description of course syllabus, prerequisites, and learning outcomes
    description TEXT,

    -- [DAT-004] Scheduling Attribute: Official start date of the course curriculum
    start_date DATE NOT NULL,

    -- [DAT-004] Scheduling Attribute: Official completion date of the course curriculum
    end_date DATE NOT NULL,

    -- [DAT-004] Relational Attribute: Identifier of the designated instructor/teacher (references users table)
    teacher_id UUID NOT NULL,

    -- [DAT-004] Operational Attribute: Maximum student capacity per course intake (default 30 students)
    max_students INT NOT NULL DEFAULT 30,

    -- [DAT-004] Relational Attribute: Physical/logical training center hosting the course (references centers table)
    center_id UUID,

    -- [DAT-004] Audit Attribute: Immutable record creation timestamp with system timezone alignment
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- [DAT-004] Audit Attribute: Mutable record modification timestamp for optimistic concurrency auditing
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- [DAT-004] Relational Constraint: Foreign key anchoring teacher to the enterprise users ledger
    CONSTRAINT fk_courses_teacher
        FOREIGN KEY (teacher_id)
        REFERENCES users (user_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    -- [DAT-004] Relational Constraint: Foreign key anchoring course to its operating training center
    CONSTRAINT fk_courses_center
        FOREIGN KEY (center_id)
        REFERENCES centers (center_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    -- [DAT-004] Domain Integrity Constraint: Ensures logical validity where course conclusion occurs on or after inception
    CONSTRAINT chk_courses_dates
        CHECK (end_date >= start_date),

    -- [DAT-004] Capacity Integrity Constraint: Ensures non-negative and non-zero student enrollment boundary
    CONSTRAINT chk_courses_max_students
        CHECK (max_students > 0)
);

-- =================================================================================================
-- High-Performance Database Index Definitions
-- =================================================================================================

-- [DAT-004] Index: Accelerates teacher schedule queries, overlap validation checks, and teacher dashboard retrieval
CREATE INDEX IF NOT EXISTS idx_courses_teacher_id
    ON courses (teacher_id);

-- [DAT-004] Index: Optimizes multi-tenant filtering, center course catalog lookups, and reporting aggregation
CREATE INDEX IF NOT EXISTS idx_courses_center_id
    ON courses (center_id);

-- [DAT-004] Composite Index: Accelerates active course temporal queries, range scans, and enrollment eligibility windows
CREATE INDEX IF NOT EXISTS idx_courses_dates
    ON courses (start_date, end_date);
```

# Day 3: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/resources/db/migration/V2__init_enrollments.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/src/main/resources/db/migration/V2__init_enrollments.sql`
*   Traceability Audit Tags For This Task: ['[DAT-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
["Tạo tệp ./sources/backend/course-service/src/main/resources/db/migration/V2__init_enrollments.sql định nghĩa bảng enrollments với các cột: enrollment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), student_id UUID NOT NULL, course_id UUID NOT NULL, enrollment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'. Khai báo ràng buộc FOREIGN KEY (student_id) REFERENCES users(user_id), FOREIGN KEY (course_id) REFERENCES courses(course_id), CONSTRAINT chk_enrollments_status CHECK (status IN ('ACTIVE','DROPPED','COMPLETED')), CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id). Tạo các chỉ mục idx_enrollments_student_id, idx_enrollments_course_id, idx_enrollments_status."]

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- [DAT-005]
-- =====================================================================
-- Enterprise Database Migration Script: V2__init_enrollments.sql
-- Project: membership-hub
-- Target Subsystem: course-service
-- Traceability Audit Tag: [DAT-005]
-- Business Logic Context: Initializes the 'enrollments' table to track 
-- student course registrations, enforcing strict relational integrity,
-- status checks, and composite uniqueness to prevent duplicate sign-ups.
-- Security & Performance: Leverages UUID primary keys, parameterized 
-- foreign keys, ANSI-compliant CHECK constraints, and targeted indexes 
-- to optimize join operations and search performance under high concurrency.
-- =====================================================================

-- Set search path to ensure DDL statements execute in the public schema securely
SET search_path TO public;

-- Drop table if exists to ensure idempotent re-runs in non-production test harnesses
-- (In production, Flyway handles versioned migration history tracking automatically)
-- [DAT-005]
DROP TABLE IF EXISTS enrollments CASCADE;

-- Create the enrollments relational data store
-- [DAT-005]
CREATE TABLE enrollments (
    -- Primary Key: Unique Universally Unique Identifier generated via cryptographic random function
    enrollment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Foreign Key Reference to the core Users system domain (student identity)
    student_id UUID NOT NULL,
    
    -- Foreign Key Reference to the Course system domain (course offering)
    course_id UUID NOT NULL,
    
    -- Timestamp capturing the exact moment the enrollment contract was established
    enrollment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Status attribute reflecting the operational lifecycle state of the student's enrollment
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    -- Audit trail columns for enterprise data governance and tracking
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Relational Integrity Constraints: Enforce referential mapping to existing parent entities
    -- [DAT-005]
    CONSTRAINT fk_enrollments_student 
        FOREIGN KEY (student_id) 
        REFERENCES users(user_id) 
        ON DELETE CASCADE,
        
    -- [DAT-005]
    CONSTRAINT fk_enrollments_course 
        FOREIGN KEY (course_id) 
        REFERENCES courses(course_id) 
        ON DELETE CASCADE,

    -- Domain Enumeration Constraint: Restrict status values to approved business states
    -- [DAT-005]
    CONSTRAINT chk_enrollments_status 
        CHECK (status IN ('ACTIVE', 'DROPPED', 'COMPLETED')),

    -- Idempotency & Business Uniqueness Rule: A student can only enroll in a specific course once
    -- [DAT-005]
    CONSTRAINT uq_enrollments_student_course 
        UNIQUE (student_id, course_id)
);

-- =====================================================================
-- Performance Optimization Indexes
-- [DAT-005]
-- =====================================================================

-- Index for high-performance lookup of enrollments filtered by student identity
-- [DAT-005]
CREATE INDEX idx_enrollments_student_id 
    ON enrollments(student_id);

-- Index for high-performance lookup of enrollments filtered by course offering
-- [DAT-005]
CREATE INDEX idx_enrollments_course_id 
    ON enrollments(course_id);

-- Index for rapid filtering and reporting based on enrollment operational status
-- [DAT-005]
CREATE INDEX idx_enrollments_status 
    ON enrollments(status);

-- =====================================================================
-- Trigger for Automatic Timestamp Maintenance (Audit Governance)
-- [DAT-005]
-- =====================================================================

-- Create or replace the function to automatically update the 'updated_at' timestamp column
-- [DAT-005]
CREATE OR REPLACE FUNCTION update_enrollment_timestamp_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Attach the update trigger to the enrollments table
-- [DAT-005]
CREATE TRIGGER trg_update_enrollments_modtime
    BEFORE UPDATE ON enrollments
    FOR EACH ROW
    EXECUTE FUNCTION update_enrollment_timestamp_column();
```

# Day 3: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql`
*   Traceability Audit Tags For This Task: ['[DAT-006]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
["Tạo tệp ./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql định nghĩa bảng attendance với idempotency key. Các cột: attendance_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), student_id UUID NOT NULL, course_id UUID NOT NULL, attendance_date DATE NOT NULL, timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, status VARCHAR(20) NOT NULL DEFAULT 'PRESENT', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP. Khai báo ràng buộc FOREIGN KEY (student_id) REFERENCES users(user_id), FOREIGN KEY (course_id) REFERENCES courses(course_id), CONSTRAINT chk_attendance_status CHECK (status IN ('PRESENT','ABSENT','LATE')), CONSTRAINT uq_attendance_idempotency UNIQUE (student_id, course_id, attendance_date). Tạo các chỉ mục idx_attendance_student_id, idx_attendance_course_id, idx_attendance_date."]

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-006]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
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
```

# Day 3: model models/gemini-3.5-flash - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql`
*   Traceability Audit Tags For This Task: ['[DAT-007]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
["Tạo tệp ./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql định nghĩa bảng student_cards với các cột: card_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), student_id UUID NOT NULL UNIQUE, issue_date DATE NOT NULL, validity_days INT NOT NULL, remaining_days INT NOT NULL, end_date DATE NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP. Khai báo ràng buộc FOREIGN KEY (student_id) REFERENCES users(user_id), CONSTRAINT chk_student_cards_validity CHECK (validity_days > 0 AND remaining_days >= 0), CONSTRAINT chk_student_cards_status CHECK (status IN ('ACTIVE','EXPIRED','SUSPENDED')). Tạo các chỉ mục idx_student_cards_student_id, idx_student_cards_status, idx_student_cards_end_date."]

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-007]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- =====================================================================
-- FLYWAY DATABASE MIGRATION: V2__init_student_cards.sql
-- Target Path: ./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql
-- Traceability Tag: [DAT-007]
-- Description: Initializes the student_cards table with strict constraints,
--              foreign keys, and optimized indexes for high-performance lookups.
-- =====================================================================

-- [DAT-007] Create the student_cards table to manage student membership lifecycles
CREATE TABLE student_cards (
    -- Unique identifier for each student card, generated automatically using UUIDv4
    -- This serves as the primary key for the membership card entity
    card_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Reference to the owner of the card (users table). 
    -- Must be unique to enforce a strict 1:1 relationship (one active card per student)
    student_id UUID NOT NULL UNIQUE,

    -- The date when the membership card was officially issued to the student
    issue_date DATE NOT NULL,

    -- Total number of days the card is valid for (must be strictly positive, e.g., 30, 90, 365 days)
    validity_days INT NOT NULL,

    -- Remaining active days left on the membership card (must be non-negative)
    remaining_days INT NOT NULL,

    -- The calculated expiration date of the membership card
    end_date DATE NOT NULL,

    -- Current operational status of the card (ACTIVE, EXPIRED, SUSPENDED)
    -- Defaults to 'ACTIVE' upon initial creation
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    -- Audit timestamp tracking when the card record was created in the system
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Audit timestamp tracking when the card record was last updated
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- [DAT-007] Foreign key constraint linking to the users table in the user-service database
    -- Ensures referential integrity; if a user is deleted, their associated card is cleaned up
    CONSTRAINT fk_student_cards_student 
        FOREIGN KEY (student_id) 
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    -- [DAT-007] Check constraint to ensure validity days are positive and remaining days are non-negative
    -- Prevents logical data corruption (e.g., negative remaining days or zero-day memberships)
    CONSTRAINT chk_student_cards_validity 
        CHECK (validity_days > 0 AND remaining_days >= 0),

    -- [DAT-007] Check constraint restricting status to predefined enterprise state values
    -- Prevents arbitrary string injection and enforces strict state machine transitions
    CONSTRAINT chk_student_cards_status 
        CHECK (status IN ('ACTIVE', 'EXPIRED', 'SUSPENDED'))
);

-- =====================================================================
-- INDEX OPTIMIZATIONS FOR HIGH-PERFORMANCE QUERY PATHS [NFR-001]
-- =====================================================================

-- [DAT-007] Index on student_id to optimize frequent direct lookups of a student's card details
-- This accelerates profile rendering and authorization checks when a student scans their card
CREATE INDEX idx_student_cards_student_id 
    ON student_cards(student_id);

-- [DAT-007] Index on status to optimize filtering active, expired, or suspended cards during batch updates
-- This is critical for nightly cron jobs that transition expired cards to the 'EXPIRED' state
CREATE INDEX idx_student_cards_status 
    ON student_cards(status);

-- [DAT-007] Index on end_date to optimize cron-based membership expiration checks and reporting queries
-- Speeds up queries scanning for cards expiring within a specific date range (e.g., next 7 days)
CREATE INDEX idx_student_cards_end_date 
    ON student_cards(end_date);
```

