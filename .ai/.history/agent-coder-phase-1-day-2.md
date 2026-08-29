# Day 2: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/resources/db/migration/V1__init_roles_and_users.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/resources/db/migration/V1__init_roles_and_users.sql`
*   Traceability Audit Tags For This Task: ['[DAT-001]', '[DAT-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
["Tạo tệp ./sources/backend/user-service/src/main/resources/db/migration/V1__init_roles_and_users.sql chứa DDL SQL chuẩn ANSI. Bảng roles (role_id SMALLINT PRIMARY KEY, name VARCHAR(30) UNIQUE NOT NULL, description VARCHAR(200)). Bảng users (user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), email VARCHAR(255) UNIQUE NOT NULL, password_hash CHAR(60) NOT NULL, full_name VARCHAR(100) NOT NULL, role_id SMALLINT NOT NULL, provider VARCHAR(20) NOT NULL DEFAULT 'local' với CHECK (provider IN ('local','firebase','google','facebook')), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (role_id) REFERENCES roles(role_id)). Tạo chỉ mục idx_users_email, idx_users_role_id. Chèn dữ liệu khởi tạo 5 vai trò RBAC: SystemAdmin, CenterAdmin, Manager, Teacher, Student."]

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-001]', '[DAT-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- ====================================================================================================
-- Enterprise Schema Migration Script: V1__init_roles_and_users.sql
-- Target Service: User Service (membership-hub)
-- Architecture Baseline: Quarkus 3.15.x / PostgreSQL 15+
--
-- Traceability Tags:
-- [DAT-001] Database Schema Definition for System Roles and User Accounts
-- [DAT-002] Relational Constraints, Indexing, and RBAC Seed Metadata
-- ====================================================================================================

-- ----------------------------------------------------------------------------------------------------
-- 1. ROLES TABLE DEFINITION [DAT-001]
-- Purpose: Defines the 5-tier Role-Based Access Control (RBAC) authorization matrix.
-- Constraint: Uses ANSI SQL standard types (SMALLINT, VARCHAR) without proprietary ENUMs for portability.
-- ----------------------------------------------------------------------------------------------------
CREATE TABLE roles (
    -- [DAT-001] Primary Key: Identifier for RBAC role level (1: SystemAdmin, 2: CenterAdmin, etc.)
    role_id SMALLINT NOT NULL,

    -- [DAT-001] Role Name: Unique immutable system descriptor for role matching in security filters
    name VARCHAR(30) NOT NULL,

    -- [DAT-001] Description: Human-readable narrative describing permission boundaries
    description VARCHAR(200),

    -- [DAT-001] Primary Key Constraint: Enforces uniqueness and primary identity indexing
    CONSTRAINT pk_roles PRIMARY KEY (role_id),

    -- [DAT-001] Unique Constraint: Prevents duplicate role naming within authorization domain
    CONSTRAINT uq_roles_name UNIQUE (name)
);

-- Descriptive comment metadata on table and columns for audit traceability
COMMENT ON TABLE roles IS 'Stores system-wide RBAC role definitions and metadata [DAT-001]';
COMMENT ON COLUMN roles.role_id IS 'Unique identifier for the RBAC role [DAT-001]';
COMMENT ON COLUMN roles.name IS 'System identity token for authorization checks (e.g., SystemAdmin) [DAT-001]';
COMMENT ON COLUMN roles.description IS 'Operational description of role capabilities and restrictions [DAT-001]';

-- ----------------------------------------------------------------------------------------------------
-- 2. USERS TABLE DEFINITION [DAT-001]
-- Purpose: Primary identity ledger storing credential hashes, profile data, and auth provider states.
-- Security: Stores passwords as 60-character BCrypt hashes. Supports OAuth2 social federations.
-- ----------------------------------------------------------------------------------------------------
CREATE TABLE users (
    -- [DAT-001] Primary Key: Cryptographically random UUID v4 identifier generated by database engine
    user_id UUID DEFAULT gen_random_uuid() NOT NULL,

    -- [DAT-001] User Email: RFC 5322 compliant unique email address acting as primary authentication identity
    email VARCHAR(255) NOT NULL,

    -- [DAT-001] Password Hash: Fixed-length 60-character container for OWASP-compliant BCrypt hashed credentials
    password_hash CHAR(60) NOT NULL,

    -- [DAT-001] Full Name: UTF-8 compatible personal identifier for user interfaces and certificates
    full_name VARCHAR(100) NOT NULL,

    -- [DAT-001] Role Reference: Foreign key linking to roles table enforcing valid RBAC role assignment
    role_id SMALLINT NOT NULL,

    -- [DAT-001] Auth Provider: Authentication realm (local credentials or federated OAuth2 providers)
    provider VARCHAR(20) DEFAULT 'local' NOT NULL,

    -- [DAT-001] Audit Timestamp: Creation timestamp recorded at database insertion time
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- [DAT-001] Audit Timestamp: Last modification timestamp maintained by application/trigger lifecycle
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- [DAT-001] Primary Key Constraint
    CONSTRAINT pk_users PRIMARY KEY (user_id),

    -- [DAT-001] Unique Constraint on Email: Blocks duplicate registrations at the database storage engine layer
    CONSTRAINT uq_users_email UNIQUE (email),

    -- [DAT-001] Foreign Key Constraint: Guarantees referential integrity between users and roles
    CONSTRAINT fk_users_role FOREIGN KEY (role_id)
        REFERENCES roles (role_id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,

    -- [DAT-001] Check Constraint: Enforces strict whitelist of supported identity providers (anti-injection rail)
    CONSTRAINT chk_users_provider CHECK (provider IN ('local', 'firebase', 'google', 'facebook'))
);

-- Descriptive comment metadata on users table
COMMENT ON TABLE users IS 'Master authentication and profile ledger for user accounts [DAT-001]';
COMMENT ON COLUMN users.user_id IS 'Unique UUID identifier for the user entity [DAT-001]';
COMMENT ON COLUMN users.email IS 'Unique validated email address for login and notifications [DAT-001]';
COMMENT ON COLUMN users.password_hash IS 'BCrypt password hash with cost factor >= 10 (CHAR(60)) [DAT-001]';
COMMENT ON COLUMN users.full_name IS 'Full legal or display name of the user [DAT-001]';
COMMENT ON COLUMN users.role_id IS 'Foreign key reference to roles.role_id [DAT-001]';
COMMENT ON COLUMN users.provider IS 'Identity provider token: local, firebase, google, facebook [DAT-001]';
COMMENT ON COLUMN users.created_at IS 'Immutable UTC timestamp of record creation [DAT-001]';
COMMENT ON COLUMN users.updated_at IS 'UTC timestamp of the most recent profile update [DAT-001]';

-- ----------------------------------------------------------------------------------------------------
-- 3. INDEX OPTIMIZATION RAIL [DAT-002]
-- Purpose: Delivers sub-second query performance (< 200ms) for auth lookups and role-filtered queries.
-- ----------------------------------------------------------------------------------------------------

-- [DAT-002] Index on email for rapid user credential lookups during authentication workflow (B-Tree)
CREATE INDEX idx_users_email ON users (email);

-- [DAT-002] Index on role_id to optimize RBAC authorization checks, join traversals, and administrative lists
CREATE INDEX idx_users_role_id ON users (role_id);

-- ----------------------------------------------------------------------------------------------------
-- 4. RBAC SEED DATA INGESTION [DAT-002]
-- Purpose: Pre-populates deterministic reference data for 5 system authorization tiers.
-- ----------------------------------------------------------------------------------------------------
INSERT INTO roles (role_id, name, description) VALUES
    (1, 'SystemAdmin', 'Quản trị viên hệ thống toàn cục - Toàn quyền cấu hình và giám sát hệ thống'),
    (2, 'CenterAdmin', 'Quản trị viên cấp trung tâm - Quản lý nhân sự, khóa học và học viên tại trung tâm'),
    (3, 'Manager', 'Quản lý cấp dưới - Hỗ trợ vận hành và điều phối lịch học tại trung tâm'),
    (4, 'Teacher', 'Giáo viên - Xem lịch giảng dạy, danh sách lớp học và quản lý điểm danh'),
    (5, 'Student', 'Học viên - Đăng ký khóa học, quét QR điểm danh và theo dõi thẻ thành viên');
```

# Day 2: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql`
*   Traceability Audit Tags For This Task: ['[DAT-003]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
["Tạo tệp ./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql định nghĩa bảng centers (center_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(100) NOT NULL, address VARCHAR(255) NOT NULL, tax_id VARCHAR(20) UNIQUE NOT NULL với CHECK (tax_id ~ '^[0-9]{10,13}$'), contact_phone VARCHAR(20), contact_email VARCHAR(100), admin_user_id UUID, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (admin_user_id) REFERENCES users(user_id)). Tạo chỉ mục idx_centers_tax_id, idx_centers_admin_user_id."]

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-003]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- =====================================================================
-- Enterprise Database Schema Migration Script (Flyway)
-- Module: center-service
-- Script Identifier: V1__init_centers.sql
-- Description: Initializes the primary 'centers' relational table,
--              establishes integrity constraints, regular expression
--              validations, foreign key linkages, and performance indices.
-- Traceability Tag: [DAT-003]
-- =====================================================================

-- Ensure pgcrypto or native gen_random_uuid() support is active for UUID v4 generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================================
-- Table: centers
-- Purpose: Stores physical and administrative training center entities.
-- Traceability Tag: [DAT-003]
-- =====================================================================
CREATE TABLE IF NOT EXISTS centers (
    -- Primary Key: Universally Unique Identifier (UUIDv4) to prevent enumeration attacks
    -- and ensure seamless distributed multi-service ID coordination.
    center_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Training center business display name, bounded to 100 characters to prevent buffer bloat.
    name VARCHAR(100) NOT NULL,

    -- Physical location address of the training facility, mandatory field.
    address VARCHAR(255) NOT NULL,

    -- Business / Tax Registration Identification Number:
    -- Must be strictly unique across all centers and comply with standard corporate tax ID formats.
    -- Bounded to 10 to 13 continuous decimal digits enforced via regular expression constraint.
    tax_id VARCHAR(20) UNIQUE NOT NULL,

    -- Optional direct administrative contact phone number.
    contact_phone VARCHAR(20),

    -- Optional administrative point-of-contact email address.
    contact_email VARCHAR(100),

    -- Foreign identifier referencing the administrative User assigned to manage this center.
    admin_user_id UUID,

    -- Record creation audit timestamp, automatically anchored to database server transaction time.
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Record last mutation timestamp for concurrency verification and audit tracking.
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key Constraint: Links the assigned center administrator to the core users table.
    CONSTRAINT fk_centers_admin FOREIGN KEY (admin_user_id) REFERENCES users(user_id) ON DELETE SET NULL,

    -- Check Constraint: Enforces that tax_id consists solely of 10 to 13 numerical digits [0-9],
    -- neutralizing malformed ingestion and mitigating SQL injection/format pollution vectors.
    CONSTRAINT chk_centers_taxid CHECK (tax_id ~ '^[0-9]{10,13}$')
);

-- =====================================================================
-- Performance & Lookup Indexing Strategy
-- Traceability Tag: [DAT-003]
-- =====================================================================

-- B-Tree index on tax_id to optimize frequent lookup, uniqueness checks, and reconciliation queries.
CREATE INDEX IF NOT EXISTS idx_centers_tax_id ON centers(tax_id);

-- B-Tree index on admin_user_id to accelerate foreign key join operations, filtering, and RBAC evaluations.
CREATE INDEX IF NOT EXISTS idx_centers_admin_user_id ON centers(admin_user_id);
```

