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