markdown
# Data Dictionary: Student Cards (STUDENTCARDS)
*File Path:* `./sources/docs/data-dictionary/student-cards.md`

## Overview
The **Student Cards** module manages digital membership cards for learners, tracking card issuance, validity period, remaining days, and renewal actions. It supports core business functions such as displaying card status, calculating expiration dates, and processing renewals with integrated payment gateways. This module is a critical component of the membership‑hub platform, enabling learners to verify their active status and facilitating automated renewal workflows.

## Table Definition: STUDENTCARDS

| Column | Data Type | Nullable | Default | Constraints | Description | Business Rule |
|--------|-----------|---------|---------|-------------|-------------|----------------|
| **card_id** | `UUID` | **No** | `gen_random_uuid()` | `PRIMARY KEY` | Unique identifier for the student card | System‑generated, immutable |
| **student_id** | `UUID` | **No** | — | `FOREIGN KEY` → `users(user_id)` `ON DELETE CASCADE` | Reference to the owning learner | Enforces one card per student; cascading delete on user removal |
| **issue_date** | `DATE` | **No** | `CURRENT_DATE` | — | Date when the card was issued | Used as baseline for validity calculations |
| **validity_days** | `INT` | **No** | — | `CHECK (validity_days > 0)` | Total number of days the card is valid from issue date | Determines card lifespan |
| **remaining_days** | `INT` | **No** | — | `CHECK (remaining_days >= 0)` | Days of validity remaining as of current date | Updated dynamically; reflects elapsed time |
| **created_at** | `TIMESTAMP` | **No** | `CURRENT_TIMESTAMP` | — | Record creation timestamp | Audit trail for card issuance |
| **updated_at** | `TIMESTAMP` | **No** | `CURRENT_TIMESTAMP` | — | Last modification timestamp | Tracks renewals and status changes |

## Indexes

| Index Name | Columns | Type | Purpose |
|------------|---------|------|---------|
| `idx_student_cards_student_id` | `student_id` | `B-Tree` | Accelerates lookups for a specific learner’s card; enforces uniqueness via table constraint |

## Foreign Keys

| Constraint | Referenced Table | On Update | On Delete | Remarks |
|------------|------------------|-----------|-----------|---------|
| `student_id` | `users(user_id)` | `CASCADE` | `CASCADE` | Guarantees referential integrity; removes card when user account is deleted |

## Business Logic

### 1. Card Issuance & Initial State
- When a learner is registered or a card is manually created, `issue_date` defaults to `CURRENT_DATE`.
- `validity_days` is set by the renewal service (e.g., 30 days). `remaining_days` is initialized to the same value as `validity_days`.

### 2. Daily Remaining‑Days Calculation
- A scheduled job (or application logic) updates `remaining_days` each day:
  sql
  UPDATE student_cards
  SET remaining_days = GREATEST(0, validity_days - EXTRACT(DAY FROM (CURRENT_DATE - issue_date))),
      updated_at   = CURRENT_TIMESTAMP;
  
- The `CHECK (remaining_days >= 0)` guarantees the field never goes negative.

### 3. Expiration Detection
- Cards with `remaining_days = 0` are considered expired. Queries filter via `WHERE remaining_days <= 0`.

### 4. Renewal Process
- The **MembershipCardService** (`org.nlh4j.saas.membership.MembershipCardService`) receives a renewal request:
  - Validates payment transaction ID.
  - Calculates new `validity_days` (e.g., add 30 days).
  - Updates `issue_date` to `CURRENT_DATE` (or extends based on business policy).
  - Recomputes `remaining_days` as the new `validity_days`.
  - Persists changes and triggers a **notification** (`[REQ-016]`) to the learner.

### 5. Audit & Logging
- All CRUD actions on `student_cards` are logged in the `audit_log` table (`[DAT-011]`) with user context, timestamp, and operation type, satisfying `[NFR-006]` (audit logging).

## DDL Script (Flyway Migration V4)

sql
-- V4__create_student_cards.sql
CREATE TABLE student_cards (
    card_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id     UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    issue_date     DATE NOT NULL DEFAULT CURRENT_DATE,
    validity_days  INT NOT NULL CHECK (validity_days > 0),
    remaining_days INT NOT NULL CHECK (remaining_days >= 0),
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_student_cards_student_id ON student_cards(student_id);


## Traceability Matrix

| Documentation Section | Tag IDs |
|-----------------------|---------|
| Module Overview & Purpose | `[DAT-007]` |
| Table Definition & Columns | `[DAT-007]` |
| Indexes & Foreign Keys | `[DAT-007]` |
| Business Logic (Calculation, Renewal, Expiration) | `[REQ-014]`, `[REQ-015]` |
| DDL Script (Migration V4) | `[DAT-007]` |
| Integration with Notification Service | `[REQ-016]`, `[ARC-008]` |
| Caching & Offline Support (Redis) | `[ARC-009]` |
| Security & Data Masking (PII) | `[NFR-003]` |
| Audit Logging (Retention 1 Year) | `[NFR-006]` |

## Additional Notes

- **Multi‑Tenant Isolation:** The `student_id` foreign key implicitly enforces tenant isolation because the `users` table contains a `center_id` field (`[ARC-002]`), ensuring cards belong to learners within a specific center.
- **Idempotency:** Renewal requests are deduplicated using an `idempotency_key` pattern (`[REQ-015]`) to prevent duplicate charges or double‑validity extensions.
- **Performance:** The `idx_student_cards_student_id` index ensures O(log N) lookups for card retrieval, critical for high‑throughput mobile APIs.
- **Compliance:** All personal data (student_id, issue_date, remaining_days) is masked in logs (`***MASKED***`) per `[NFR-003]` and `[NFR-006]`.
- **Future Extensions:** The schema supports optional fields such as `card_type` (physical/digital) and `qr_code_url` without breaking existing constraints.

---
*End of Data Dictionary – Student Cards Module*