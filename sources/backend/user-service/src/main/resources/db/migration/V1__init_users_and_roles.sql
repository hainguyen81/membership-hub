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