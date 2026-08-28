sql
-- ============================================================================
-- FLYWAY MIGRATION: V3__create_student_cards.sql
-- PROJECT: membership-hub
-- COMPONENT: Database Schema - Student Cards (Membership Cards)
-- TRACEABILITY TAGS: [DAT-007]
-- DESCRIPTION: Creates the student_cards table to store digital membership
--              card information including validity period and remaining days.
--              Supports membership renewal workflow and digital card display.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- TABLE: student_cards
-- PURPOSE: Stores digital membership card records for students
-- BUSINESS RULES:
--   - Each student can have one active membership card at a time
--   - Validity days must be positive (> 0)
--   - Remaining days cannot be negative (>= 0)
--   - Card is linked to a student via student_id foreign key
--   - On student deletion, cascade delete their membership cards
-- ----------------------------------------------------------------------------

-- Create the student_cards table with comprehensive constraints
CREATE TABLE IF NOT EXISTS student_cards (
    -- Primary key: unique identifier for each membership card
    card_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Foreign key to the users table (student)
    -- ON DELETE CASCADE ensures cards are removed when student is deleted
    student_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Date when the membership card was issued
    -- Defaults to current date on card creation
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    
    -- Total validity period of the card in days
    -- Must be positive (> 0) as per business rule
    validity_days INT NOT NULL CHECK (validity_days > 0),
    
    -- Remaining valid days calculated from issue_date + validity_days
    -- Cannot be negative (>= 0) as per business rule
    remaining_days INT NOT NULL CHECK (remaining_days >= 0),
    
    -- Audit timestamp: record creation time
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Audit timestamp: last update time
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- INDEXES
-- Optimize query performance for common access patterns
-- ----------------------------------------------------------------------------

-- Index for fast lookup of cards by student_id
-- Used when student views their membership card
CREATE INDEX IF NOT EXISTS idx_student_cards_student_id 
    ON student_cards(student_id);

-- ----------------------------------------------------------------------------
-- TRIGGER: Auto-update updated_at timestamp
-- Ensures updated_at is automatically set to current timestamp on row updates
-- ----------------------------------------------------------------------------

-- Create or replace function to update updated_at column
CREATE OR REPLACE FUNCTION update_student_cards_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to call the function before each update
DROP TRIGGER IF EXISTS trg_update_student_cards_updated_at ON student_cards;
CREATE TRIGGER trg_update_student_cards_updated_at
    BEFORE UPDATE ON student_cards
    FOR EACH ROW
    EXECUTE FUNCTION update_student_cards_updated_at();

-- ============================================================================
-- MIGRATION COMPLETED
-- Table: student_cards
-- Indexes: idx_student_cards_student_id
-- Triggers: trg_update_student_cards_updated_at
-- ============================================================================