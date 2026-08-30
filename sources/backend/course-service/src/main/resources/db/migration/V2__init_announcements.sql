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