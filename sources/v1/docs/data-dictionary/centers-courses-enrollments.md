markdown
# Data Dictionary: Centers, Courses, Enrollments
*Document generated per enterprise data‑dictionary standards – traceability tags embedded throughout.*

<!-- [DAT-003] -->
## Table: centers
**Purpose:** Stores master data for each service center (tenant). Used for multi‑tenant isolation, RBAC scoping, and operational contact information.  
**Primary Key:** `center_id` (UUID, non‑null) – globally unique identifier for the center.  
**Columns:**

| Column Name | Data Type | Nullable | Default | Description & Business Rules |
|-------------|-----------|---------|---------|------------------------------|
| `center_id` | UUID | **No** | `gen_random_uuid()` | System‑generated surrogate key. |
| `name` | VARCHAR(100) | **No** | – | Human‑readable center name. |
| `address` | VARCHAR(255) | **No** | – | Physical address of the center. |
| `tax_id` | VARCHAR(13) | **No** | – | Tax identification number – **UNIQUE** and must match pattern `^[0-9]{10,13}$`. |
| `contact_phone` | VARCHAR(20) | Yes | – | Contact telephone for the center. |
| `contact_email` | VARCHAR(255) | Yes | – | Contact email – validated with `CHECK (contact_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$')`. |
| `created_at` | TIMESTAMP | **No** | `CURRENT_TIMESTAMP` | Record creation timestamp. |
| `updated_at` | TIMESTAMP | **No** | `CURRENT_TIMESTAMP` | Record last‑update timestamp (auto‑updated on row change). |

**Constraints & Indexes:**
- `PRIMARY KEY (center_id)`
- `UNIQUE (tax_id)` – enforces one‑to‑one tax identifier per center.
- `CHECK (tax_id ~ '^[0-9]{10,13}$')` – numeric tax ID validation.
- `CHECK (contact_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$')` – email format validation.
- `INDEX idx_centers_tax_id (tax_id)` – accelerates look‑ups by tax identifier.
- `INDEX idx_centers_name (name)` – supports fast center name searches.

**Foreign Keys / Relationships:**
- None – this table is a root entity for the multi‑tenant hierarchy.

---

<!-- [DAT-004] -->
## Table: courses
**Purpose:** Represents academic/training courses offered by a center. Enforces schedule integrity and teacher‑assignment rules.  
**Primary Key:** `course_id` (UUID, non‑null).  
**Columns:**

| Column Name | Data Type | Nullable | Default | Description & Business Rules |
|-------------|-----------|---------|---------|------------------------------|
| `course_id` | UUID | **No** | `gen_random_uuid()` | System‑generated surrogate key. |
| `title` | VARCHAR(150) | **No** | – | Course title – max 150 characters. |
| `description` | TEXT | Yes | – | Detailed course description. |
| `start_date` | DATE | **No** | – | Course start date – must be earlier than `end_date`. |
| `end_date` | DATE | **No** | – | Course end date. |
| `teacher_id` | UUID | **No** | – | Foreign key to `users.user_id` (the assigned teacher). |
| `max_students` | INT | **No** | `30` | Maximum enrollment capacity – must be ≥ 1. |
| `created_at` | TIMESTAMP | **No** | `CURRENT_TIMESTAMP` | Record creation timestamp. |
| `updated_at` | TIMESTAMP | **No** | `CURRENT_TIMESTAMP` | Record last‑update timestamp. |

**Constraints & Indexes:**
- `PRIMARY KEY (course_id)`
- `FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE CASCADE`
- `CHECK (start_date < end_date)` – prevents invalid date ranges.
- `CHECK (max_students >= 1)` – ensures capacity is positive.
- `INDEX idx_courses_teacher_id (teacher_id)` – speeds teacher‑course queries.
- `INDEX idx_courses_dates (start_date, end_date)` – supports schedule conflict detection.
- `INDEX idx_courses_title (title)` – enables fast title searches.

**Business Logic / Usage:**
- Schedule conflict detection is performed at application layer by checking overlapping `start_date`/`end_date` ranges for the same `teacher_id` (or center location if added later).
- Enrollment capacity is enforced via a trigger or application service that counts existing `enrollments` per `course_id`.

---

<!-- [DAT-005] -->
## Table: enrollments
**Purpose:** Tracks student enrollment in courses. Guarantees **idempotent** enrollment (one record per student per course) and supports reporting on participation.  
**Primary Key:** `enrollment_id` (UUID, non‑null).  
**Columns:**

| Column Name | Data Type | Nullable | Default | Description & Business Rules |
|-------------|-----------|---------|---------|------------------------------|
| `enrollment_id` | UUID | **No** | `gen_random_uuid()` | System‑generated surrogate key. |
| `student_id` | UUID | **No** | – | Foreign key to `users.user_id` (the enrolled student). |
| `course_id` | UUID | **No** | – | Foreign key to `courses.course_id`. |
| `enrollment_date` | TIMESTAMP | **No** | `CURRENT_TIMESTAMP` | Moment when the enrollment was recorded. |
| `created_at` | TIMESTAMP | **No** | `CURRENT_TIMESTAMP` | Record creation timestamp. |
| `updated_at` | TIMESTAMP | **No** | `CURRENT_TIMESTAMP` | Record last‑update timestamp. |

**Constraints & Indexes:**
- `PRIMARY KEY (enrollment_id)`
- `FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE`
- `FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE`
- `UNIQUE (student_id, course_id)` – enforces **idempotent** enrollment (one row per student‑course pair).
- `INDEX idx_enrollments_student_id (student_id)` – speeds student enrollment listing.
- `INDEX idx_enrollments_course_id (course_id)` – accelerates course‑enrollment counts.
- `INDEX idx_enrollments_date (enrollment_date)` – supports historical reporting.

**Business Logic / Usage:**
- The unique constraint on (`student_id`, `course_id`) guarantees that duplicate enrollment attempts are rejected at the database level, supporting the **attendance idempotency** requirement.
- Application services validate course capacity before inserting a new enrollment (checks `max_students` against current count of `enrollments` for the `course_id`).

---

## Traceability Matrix Reference
| Table | Tag ID |
|-------|--------|
| centers | [DAT-003] |
| courses | [DAT-004] |
| enrollments | [DAT-005] |

*All column definitions, constraints, and indexes are directly mapped to the corresponding requirement/traceability tags above. This document serves as the authoritative source for schema validation, development implementation, and audit compliance.*