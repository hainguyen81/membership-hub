# Day 3: model models/gemini-3.5-flash - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql` (Must map to sources/backend/ or sources/frontend/)
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
['Đánh giá tệp ./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql xác minh các tiêu chí: (1) ràng buộc uq_attendance_idempotency UNIQUE (student_id, course_id, attendance_date) đảm bảo idempotency đúng theo yêu cầu REQ-013; (2) kiểu status dùng VARCHAR(20) kết hợp CHECK thay vì ENUM theo chuẩn ANSI SQL; (3) các chỉ mục idx_attendance_student_id, idx_attendance_course_id, idx_attendance_date phục vụ truy vấn theo ngày và theo học viên/khóa học. Đồng thời đánh giá schema courses, enrollments, student_cards xác nhận ràng buộc ngày end_date >= start_date, UNIQUE (student_id, course_id) trong enrollments ngăn đăng ký trùng. Lập báo cáo đánh giá với bảng tuân thủ cho từng TagID và danh sách khuyến nghị sửa lỗi nếu phát hiện bất thường.']

---

### ⚙️ LEAD REVIEWER/FIXER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, sub-task specifications, and potential compiler log streams provided above. Automatically evaluate the error signatures or static code anomalies to construct either a fully patched backend Java component or a frontend TypeScript/Next.js asset. 

Ensure that you read the exact Tag IDs from the `['[DAT-004]', '[DAT-005]', '[DAT-006]', '[DAT-007]']` variable, merge them seamlessly with any pre-existing tags inside the source payload, and permanently inject/preserve those exact Tag IDs within the generated code's Javadoc documentation or file header comments. The final code must be completely self-contained and compiler-ready. Emit the production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- =================================================================================================
-- BẢN THIẾT KẾ HỆ THỐNG DỮ LIỆU & KIỂM TOÁN TUÂN THỦ (DATA LAYER COMPLIANCE AUDIT)
-- Mã Bản Thiết Kế: ARCH-20260829122721
-- Đường Dẫn Thành Phần: ./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql
-- Phạm Vi Tích Hợp: INTEGRATION_SCOPE
--
-- 📊 MA TRẬN TRUY XUẤT NGUỒN GỐC & TUÂN THỦ TIÊU CHUẨN DOANH NGHIỆP (TRACEABILITY MATRIX):
-- ┌───────────┬──────────────────────────────────┬─────────────────────────────────────────────────┐
-- │ Tag ID    │ Tiêu Chí Kiểm Toán               │ Trạng Thái Thực Thi & Giải Pháp Kỹ Thuật        │
-- ├───────────┼──────────────────────────────────┼─────────────────────────────────────────────────┤
-- │ [DAT-004] │ Schema Bảng Courses              │ Đã cấu hình ràng buộc ngày end_date >= start_date│
-- │           │                                  │ và độ dài tiêu đề tối đa 150 ký tự.             │
-- ├───────────┼──────────────────────────────────┼─────────────────────────────────────────────────┤
-- │ [DAT-005] │ Schema Bảng Enrollments          │ Đã thiết lập khóa UNIQUE (student_id, course_id)│
-- │           │                                  │ để ngăn chặn việc đăng ký trùng lặp khóa học.   │
-- ├───────────┼──────────────────────────────────┼─────────────────────────────────────────────────┤
-- │ [DAT-006] │ Schema Bảng Attendance           │ Khởi tạo cấu trúc bảng điểm danh hoàn chỉnh     │
-- │           │                                  │ với các ràng buộc toàn vẹn dữ liệu chuẩn ANSI.  │
-- ├───────────┼──────────────────────────────────┼─────────────────────────────────────────────────┤
-- │ [DAT-007] │ Schema Bảng StudentCards         │ Thiết lập bảng thẻ thành viên, ràng buộc số ngày│
-- │           │                                  │ hiệu lực > 0 và số ngày còn lại >= 0.           │
-- ├───────────┼──────────────────────────────────┼─────────────────────────────────────────────────┤
-- │ [REQ-013] │ Đảm Bảo Tính Idempotency         │ Áp dụng ràng buộc uq_attendance_idempotency     │
-- │           │                                  │ UNIQUE (student_id, course_id, attendance_date).│
-- ├───────────┼──────────────────────────────────┼─────────────────────────────────────────────────┤
-- │ [EXC-002] │ Xử Lý Điểm Danh Trùng Lặp        │ Ràng buộc UNIQUE cấp DB hỗ trợ bắt ngoại lệ     │
-- │           │                                  │ và trả về cờ duplicate: true ở tầng ứng dụng.   │
-- ├───────────┼──────────────────────────────────┼─────────────────────────────────────────────────┤
-- │ [ARC-007] │ Giao Thức Điểm Danh QR           │ Tối ưu hóa các chỉ mục phục vụ truy vấn tần suất│
-- │           │                                  │ cao theo ngày và theo cặp học viên/khóa học.    │
-- └───────────┴──────────────────────────────────┴─────────────────────────────────────────────────┘
--
-- 📝 DANH SÁCH KHUYẾN NGHỊ BẢO MẬT & HIỆU NĂNG (DBA RECOMMENDATIONS):
-- 1. Sử dụng kiểu dữ liệu UUID chuẩn thay vì chuỗi văn bản để tối ưu hóa không gian lưu trữ và tốc độ index.
-- 2. Áp dụng VARCHAR kết hợp CHECK constraint thay vì ENUM để đảm bảo tính tương thích cao với chuẩn ANSI SQL.
-- 3. Phân vùng dữ liệu (Partitioning) theo năm/tháng đối với bảng `attendance` nếu quy mô dữ liệu vượt quá 10 triệu bản ghi.
-- =================================================================================================

-- 1. KHỞI TẠO BẢNG COURSES [DAT-004]
CREATE TABLE IF NOT EXISTS courses (
    course_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    teacher_id UUID NOT NULL,
    max_students INT NOT NULL DEFAULT 30,
    center_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_courses_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_courses_title_len CHECK (char_length(title) <= 150)
);

-- Tạo chỉ mục tối ưu hóa truy vấn khóa học theo khoảng thời gian và trung tâm
CREATE INDEX IF NOT EXISTS idx_courses_dates ON courses (start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_courses_center_id ON courses (center_id);
CREATE INDEX IF NOT EXISTS idx_courses_teacher_id ON courses (teacher_id);


-- 2. KHỞI TẠO BẢNG ENROLLMENTS [DAT-005]
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    enrollment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses (course_id) ON DELETE CASCADE,
    CONSTRAINT chk_enrollments_status CHECK (status IN ('ACTIVE', 'DROPPED', 'COMPLETED')),
    CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id)
);

-- Tạo chỉ mục phục vụ tìm kiếm học viên và khóa học đã đăng ký
CREATE INDEX IF NOT EXISTS idx_enrollments_student_id ON enrollments (student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON enrollments (course_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_status ON enrollments (status);


-- 3. KHỞI TẠO BẢNG ATTENDANCE [DAT-006] [REQ-013] [EXC-002] [ARC-007]
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_course FOREIGN KEY (course_id) REFERENCES courses (course_id) ON DELETE CASCADE,
    CONSTRAINT chk_attendance_status CHECK (status IN ('PRESENT', 'ABSENT', 'LATE')),
    -- Khóa tổng hợp ngăn chặn điểm danh trùng lặp trong cùng một ngày (Idempotency Guard)
    CONSTRAINT uq_attendance_idempotency UNIQUE (student_id, course_id, attendance_date)
);

-- Tạo các chỉ mục hiệu năng cao phục vụ thống kê điểm danh và quét QR tần suất lớn
CREATE INDEX IF NOT EXISTS idx_attendance_student_id ON attendance (student_id);
CREATE INDEX IF NOT EXISTS idx_attendance_course_id ON attendance (course_id);
CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance (attendance_date);
CREATE INDEX IF NOT EXISTS idx_attendance_composite_lookup ON attendance (course_id, attendance_date);


-- 4. KHỞI TẠO BẢNG STUDENT_CARDS [DAT-007]
CREATE TABLE IF NOT EXISTS student_cards (
    card_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    issue_date DATE NOT NULL,
    validity_days INT NOT NULL,
    remaining_days INT NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_student_cards_validity CHECK (validity_days > 0 AND remaining_days >= 0),
    CONSTRAINT chk_student_cards_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'SUSPENDED')),
    CONSTRAINT uq_student_cards_student UNIQUE (student_id)
);

-- Tạo chỉ mục phục vụ tra cứu trạng thái thẻ thành viên của học viên
CREATE INDEX IF NOT EXISTS idx_student_cards_student_id ON student_cards (student_id);
CREATE INDEX IF NOT EXISTS idx_student_cards_status ON student_cards (status);
CREATE INDEX IF NOT EXISTS idx_student_cards_end_date ON student_cards (end_date);
```

