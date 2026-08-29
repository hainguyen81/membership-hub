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