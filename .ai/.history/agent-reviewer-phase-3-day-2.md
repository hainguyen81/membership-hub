# Day 2: model google/gemma-4-26b-a4b-it:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseService.java
* **📝 Prompt / Tasks / Data**:
None
* **📝 Response**:
None

# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/resources/db/migration/V2__course_schedule_exclusion.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/src/main/resources/db/migration/V2__course_schedule_exclusion.sql` (Must map to sources/backend/ or sources/frontend/)
*   Context Module Context Reference Path: `INTEGRATION_SCOPE`

### SOURCE CODE UNDER AUDIT (VERIFICATION TARGET)
* **Target Code Component Payload For Comprehensive Review:**
<EXISTING_CODE_UNDER_AUDIT>

</EXISTING_CODE_UNDER_AUDIT>


### ❌ REAL RAW COMPILER ERROR LOGS (CRITICAL FIX TARGET)
The codebase above triggered the following compiler or runtime exceptions. You MUST analyze this stack trace or log error text to pinpoint and auto-patch the root cause:
```text
True
```
*   Operational Modality Activated: COMPILER_FIXER_MODE


### 📋 EXECUTION SUB-TASKS TO ENFORCE
["Reviewer đánh giá logic CourseService.create và CourseService.update tại ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseService.java đảm bảo có kiểm tra xung đột lịch giáo viên trước khi persist. Xác nhận sử dụng PanacheRepository với JPQL parameter binding, ném ScheduleConflictException khi phát hiện trùng lặp dựa trên teacher_id và khoảng ngày daterange(start_date, end_date, '[]'). Kiểm tra việc áp dụng ràng buộc EXCLUDE USING gist trong database migration V2. Đề xuất cải tiến nếu thiếu transaction boundary hoặc không sử dụng @Transactional đúng cách. Đặc tả DDL SQL: Migration V2__course_schedule_exclusion.sql áp dụng ràng buộc exclusion chống xung đột lịch giáo viên sử dụng btree_gist extension và EXCLUDE USING gist với teacher_id WITH = và daterange(start_date, end_date, '[]') WITH &&."]

---

### ⚙️ LEAD REVIEWER/FIXER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, sub-task specifications, and potential compiler log streams provided above. Automatically evaluate the error signatures or static code anomalies to construct either a fully patched backend Java component or a frontend TypeScript/Next.js asset. 

Ensure that you read the exact Tag IDs from the `['[REQ-008]', '[EXC-001]']` variable, merge them seamlessly with any pre-existing tags inside the source payload, and permanently inject/preserve those exact Tag IDs within the generated code's Javadoc documentation or file header comments. The final code must be completely self-contained and compiler-ready. Emit the production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
/**
 * Migration Script: V2__course_schedule_exclusion.sql
 * Target Component Path: ./sources/backend/course-service/src/main/resources/db/migration/V2__course_schedule_exclusion.sql
 * Traceability Tags: [REQ-008], [EXC-001], [DAT-001], [NFR-003]
 * Enterprise Compliance: PostgreSQL btree_gist extension and GIST exclusion constraint setup.
 */

-- Ensure btree_gist extension is available for scalar and range operator overlapping checks
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Ensure date range check constraints are in place
ALTER TABLE courses
    DROP CONSTRAINT IF EXISTS chk_courses_date_range;

ALTER TABLE courses
    ADD CONSTRAINT chk_courses_date_range 
    CHECK (end_date >= start_date);

-- Apply EXCLUDE USING gist constraint to prevent teacher schedule overlaps
-- This enforces database-level conflict detection for [REQ-008]
ALTER TABLE courses
    DROP CONSTRAINT IF EXISTS ex_teacher_schedule_no_overlap;

ALTER TABLE courses
    ADD CONSTRAINT ex_teacher_schedule_no_overlap
    EXCLUDE USING gist (
        teacher_id WITH =,
        daterange(start_date, end_date, '[]') WITH &&
    )
    WHERE (teacher_id IS NOT NULL);

-- Create supplementary indices to optimize schedule range search queries
CREATE INDEX IF NOT EXISTS idx_courses_teacher_daterange 
    ON courses (teacher_id, start_date, end_date);
```

