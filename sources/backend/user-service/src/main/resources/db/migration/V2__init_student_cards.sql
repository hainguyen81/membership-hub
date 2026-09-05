-- ============================================
-- FILE: V2__init_student_cards.sql
-- SCOPE: Student Cards
-- TAG_ID: [DAT-006]
-- ============================================
-- ANSI SQL compliant migration for student_cards table
-- Tracks membership card validity period and remaining days
-- Implements OWASP security baseline with proper constraints
-- Supports renewal workflow and audit trail

CREATE TABLE student_cards (
    card_id UUID NOT NULL,
    student_id UUID NOT NULL,
    issue_date DATE NOT NULL,
    validity_days INT NOT NULL,
    remaining_days INT NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT pk_student_cards PRIMARY KEY (card_id),
    CONSTRAINT uq_student_cards_student UNIQUE (student_id),
    CONSTRAINT fk_student_cards_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT ck_student_cards_validity CHECK (validity_days > 0),
    CONSTRAINT ck_student_cards_remaining CHECK (remaining_days >= 0)
);

-- Index for expiration date queries (renewal reminders, expired card reports)
CREATE INDEX idx_student_cards_end_date ON student_cards(end_date);

-- Index for student card lookups and joins
CREATE INDEX idx_student_cards_student_id ON student_cards(student_id);