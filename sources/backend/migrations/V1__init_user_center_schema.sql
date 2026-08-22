sql
-- [DAT-001] [DAT-003] Enterprise traceability tags for initial user and center schema migration
-- This migration script creates the foundational relational schema for the membership-hub system.
-- It defines three core tables: roles, users, and centers, with appropriate constraints, indexes, and data validation rules.
-- All constraints enforce data integrity, security, and business rules as per the architectural specification.

-- ------------------------------------------------------------
-- Table: roles
-- Purpose: Stores system roles used for RBAC (Role-Based Access Control).
-- ------------------------------------------------------------
CREATE TABLE roles (
    role_id SMALLINT PRIMARY KEY,                     -- Unique identifier for the role (e.g., 1=System Admin)
    name VARCHAR(30) NOT NULL UNIQUE,                -- Role name (e.g., 'System Admin', 'Center Admin')
    description VARCHAR(200)                         -- Optional description of the role's responsibilities
);

-- ------------------------------------------------------------
-- Table: users
-- Purpose: Stores user account information, authentication details, and role assignments.
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Globally unique identifier for the user
    email VARCHAR(255) NOT NULL UNIQUE,               -- User's email address (must be unique)
    password_hash CHAR(60) NOT NULL,                 -- Hashed password using bcrypt (60-char output)
    full_name VARCHAR(100) NOT NULL,                 -- Full name of the user
    role_id SMALLINT NOT NULL REFERENCES roles(role_id), -- Foreign key to roles.role_id (enforces referential integrity)
    provider VARCHAR(20) NOT NULL DEFAULT 'local',   -- Authentication provider: 'local', 'firebase', 'google', or 'facebook'
    CHECK (provider IN ('local', 'firebase', 'google', 'facebook')), -- Validate allowed provider values
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Record creation timestamp
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP  -- Record last modification timestamp
);

-- Indexes for the users table to optimize frequent query patterns
CREATE INDEX idx_users_email ON users(email);       -- Fast lookup by email (e.g., login, duplicate check)
CREATE INDEX idx_users_role_id ON users(role_id);    -- Fast lookup by role for RBAC queries

-- ------------------------------------------------------------
-- Table: centers
-- Purpose: Stores information about each service center (tenant) in the multi‑center deployment.
-- ------------------------------------------------------------
CREATE TABLE centers (
    center_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Unique identifier for the center
    name VARCHAR(100) NOT NULL,                         -- Center name
    address VARCHAR(255) NOT NULL,                      -- Physical address of the center
    tax_id VARCHAR(13) NOT NULL UNIQUE,                 -- Tax identification number (must be unique across centers)
    CHECK (tax_id ~ '^[0-9]{10,13}$'),                  -- Validate tax_id contains only digits and length 10‑13
    contact_phone VARCHAR(20),                          -- Optional contact phone number
    contact_email VARCHAR(255),                         -- Optional contact email (basic format validation)
    CHECK (contact_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$') -- Simple email format check
);

-- Index for the centers table to accelerate tax_id lookups (e.g., validation, reporting)
CREATE INDEX idx_centers_tax_id ON centers(tax_id);