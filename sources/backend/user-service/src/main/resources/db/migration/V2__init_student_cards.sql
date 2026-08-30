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