```markdown
# ENTERPRISE DATA DICTIONARY SPECIFICATION
## 📋 Overview
**Document Identifier:** `./sources/docs/database/ENTERPRISE_DATA_DICTIONARY_SPEC.md`  
**Version:** 1.0 (Enterprise Baseline)  
**Date:** 2026/08/29 22:34:21  
**Prepared By:** Enterprise System Architect (SA Agent)  

---

## 📊 1. INTRODUCTION & SCOPE

This document serves as the definitive **Enterprise Data Dictionary Specification** for the **Membership Hub** platform. It enumerates all persistent data entities, their structural contracts, business semantics, and traceability to requirement artifacts across the multi-phase implementation roadmap.

---

## 📊 2. ENTERPRISE DATA MODEL – CORE TABLES

The Membership Hub relies on **12 core relational tables** defined via Flyway migrations (V1–V3). Each table is mapped to a unique traceability Tag ID (`[DAT-XXX]`) and includes detailed column specifications, constraints, indexes, and business semantics.

### 2.1 Table Catalog Overview

| Table Name | Traceability Tag ID | Migration File | Primary Key | Approx. Row Count (Est.) |
|------------|--------------------|----------------|------------|--------------------------|
| `roles` | `[DAT-001]` | `V1__init_users_and_roles.sql` | `role_id` (SMALLINT) | ~5 |
| `users` | `[DAT-001]` | `V1__init_users_and_roles.sql` | `user_id` (UUID) | ~10,000 |
| `centers` | `[DAT-002]` | `V1__init_centers.sql` | `center_id` (UUID) | ~50 |
| `courses` | `[DAT-003]` | `V1__init_courses.sql` | `course_id` (UUID) | ~2,000 |
| `enrollments` | `[DAT-004]` | `V1__init_enrollments_attendance.sql` | `enrollment_id` (UUID) | ~30,000 |
| `attendance` | `[DAT-005]` | `V1__init_enrollments_attendance.sql` | `attendance_id` (UUID) | ~500,000 |
| `student_cards` | `[DAT-006]` | `V2__init_student_cards.sql` | `card_id` (UUID) | ~8,000 |
| `notifications` | `[DAT-007]` | `V2__init_notifications.sql` | `notification_id` (UUID) | ~200,000 |
| `promotions` | `[DAT-009]` | `V2__init_promotions.sql` | `promo_id` (UUID) | ~500 |
| `announcements` | `[DAT-010]` | `V2__init_announcements.sql` | `announcement_id` (UUID) | ~1,000 |
| `system_settings` | `[DAT-011]` | `V3__init_system_settings.sql` | `setting_key` (VARCHAR) | ~20 |
| `audit_logs` | `[DAT-012]` | `V3__init_audit_logs.sql` | `log_id` (UUID) | >10M (partitioned) |

> **Note:** Tag IDs `[DAT-001]` and `[DAT-008]` both reference the same migration file `V1__init_users_and_roles.sql`. `[DAT-008]` is retained for backward compatibility with legacy tooling.

---

### 2.2 Detailed Table Specifications

Below each table is described with **Mermaid ERD diagram**, **column details**, **constraints**, **indexes**, and **business meaning**. All column names follow the `org.nlh4j.membershiphub` package naming convention and are stored in PostgreSQL with appropriate data types.

#### 2.2.1 Roles (`[DAT-001]`)

```mermaid
erDiagram {
    User }|--o{ Role : has
}
```

| Column | Data Type | Nullable | Default | Constraints & Indexes | Business Meaning |
|--------|-----------|----------|---------|----------------------|------------------|
| `role_id` | `SMALLINT` | **NO** | – | `PRIMARY KEY`, `UNIQUE` | System‑defined role identifier (1=SystemAdmin, 2=CenterAdmin, 3=Manager, 4=Teacher, 5=Student) |
| `name` | `VARCHAR(30)` | **NO** | – | `UNIQUE`, `CHECK (name IN ('SYSTEM_ADMIN','CENTER_ADMIN','MANAGER','TEACHER','STUDENT'))` | Human‑readable role name |
| `description` | `VARCHAR(200)` | **YES** | `NULL` | – | Detailed role description for UI tooltips |
| **Indexes** | – | – | – | `IDX_ROLES_NAME` (on `name`) | Accelerate role‑