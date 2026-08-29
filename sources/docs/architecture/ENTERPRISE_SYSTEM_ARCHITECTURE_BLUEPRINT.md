```markdown
# 🏛️ ENTERPRISE SYSTEM ARCHITECTURE BLUEPRINT: MEMBERSHIP HUB
*(Enterprise Architecture Document - Database Entity Relationship & Traceability Specifications)*

## 📑 0. GLOBAL TRACEABILITY & CODE COMMENTING MANDATE
- **Top-of-Class Constants Declaration Law:** All deterministic or configuration values MUST be extracted and isolated as immutable constant parameters declared cohesively at the absolute top layer of the architectural specification.
- **Traceability Tag ID Injection:** This document explicitly incorporates and maps technical tracking Tag IDs (`[DAT-004]`, `[DAT-005]`, `[DAT-006]`, `[DAT-007]`, `[ARC-007]`, `[REQ-013]`) inline to form the absolute enterprise audit trail.

---

## 📊 1. SƠ ĐỒ QUAN HỆ THỰC THỂ - PHẦN 2 (COURSES, ENROLLMENTS, ATTENDANCE, STUDENT_CARDS)

Tài liệu này mô tả chi tiết lược đồ cơ sở dữ liệu quan hệ phần 2 của hệ thống **Membership Hub**, bao gồm các bảng cốt lõi phục vụ nghiệp vụ quản lý khóa học, ghi danh, điểm danh (với cơ chế idempotency bảo vệ bằng khóa tổng hợp), và quản lý thẻ thành viên. 

Các bảng được thiết kế chuẩn hóa theo mô hình quan hệ ANSI SQL, sử dụng kiểu dữ liệu UUID cho khóa chính và khóa ngoại, kết hợp chặt chẽ với các ràng buộc CHECK và UNIQUE để đảm bảo tính toàn vẹn dữ liệu ở cấp độ cơ sở dữ liệu.

---

### 📚 1.1. Bảng Khóa Học (`courses`)
Bảng `courses` lưu trữ thông tin chi tiết về các khóa học đào tạo được tổ chức tại các trung tâm, bao gồm thời gian bắt đầu, kết thúc, sức chứa tối đa và giáo viên phụ trách.

- **Mã định danh Traceability Tag:** `[DAT-004]`, `[REQ-007]`
- **Đường dẫn mô-đun vật lý:** `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `course_id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Khóa chính định danh duy nhất khóa học |
| `title` | VARCHAR(150) | NOT NULL | Tiêu đề hoặc tên của khóa học |
| `description` | TEXT | NULLABLE | Mô tả chi tiết nội dung chương trình học |
| `start_date` | DATE | NOT NULL | Ngày bắt đầu tổ chức khóa học |
| `end_date` | DATE | NOT NULL | Ngày kết thúc khóa học |
| `teacher_id` | UUID | NOT NULL, FOREIGN KEY (`users.user_id`) | Mã định danh giáo viên phụ trách giảng dạy |
| `max_students` | INT | NOT NULL, DEFAULT 30 | Số lượng học viên tối đa được phép ghi danh |
| `center_id` | UUID | NULLABLE, FOREIGN KEY (`centers.center_id`) | Mã định danh trung tâm tổ chức khóa học |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo bản ghi khóa học |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm cập nhật bản ghi gần nhất |

**Ràng buộc kiểm tra (Check Constraints & Indexes):**
- `CONSTRAINT chk_courses_dates CHECK (end_date >= start_date)`: Đảm bảo ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.
- `CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);`
- `CREATE INDEX idx_courses_center_id ON courses(center_id);`
- `CREATE INDEX idx_courses_dates ON courses(start_date, end_date);`

---

### 📝 1.2. Bảng Ghi Danh Học Viên (`enrollments`)
Bảng `enrollments` quản lý mối quan hệ nhiều-nhiều giữa học viên (`users`) và khóa học (`courses`), ghi nhận trạng thái tham gia của học viên.

- **Mã định danh Traceability Tag:** `[DAT-005]`, `[REQ-010]`, `[REQ-011]`
- **Đường dẫn mô-đun vật lý:** `./sources/backend/course-service/src/main/resources/db/migration/V2__init_enrollments.sql`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `enrollment_id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Khóa chính định danh bản ghi ghi danh |
| `student_id` | UUID | NOT NULL, FOREIGN KEY (`users.user_id`) | Mã định danh học viên đăng ký |
| `course_id` | UUID | NOT NULL, FOREIGN KEY (`courses.course_id`) | Mã định danh khóa học được đăng ký |
| `enrollment_date`| TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm học viên thực hiện ghi danh |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | Trạng thái ghi danh (`ACTIVE`, `DROPPED`, `COMPLETED`) |

**Ràng buộc kiểm tra (Check Constraints & Indexes):**
- `CONSTRAINT chk_enrollments_status CHECK (status IN ('ACTIVE','DROPPED','COMPLETED'))`
- `CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id)`: Ngăn chặn học viên đăng ký trùng lặp cùng một khóa học nhiều lần.
- `CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);`
- `CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);`
- `CREATE INDEX idx_enrollments_status ON enrollments(status);`

---

### ⏱️ 1.3. Bảng Điểm Danh (`attendance`)
Bảng `attendance` lưu trữ lịch sử điểm danh của học viên trong các buổi học của khóa học. Bảng này tích hợp cơ chế **Idempotency** thông qua khóa tổng hợp duy nhất.

- **Mã định danh Traceability Tag:** `[DAT-006]`, `[REQ-012]`, `[REQ-013]`, `[ARC-007]`
- **Đường dẫn mô-đun vật lý:** `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `attendance_id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Khóa chính định danh bản ghi điểm danh |
| `student_id` | UUID | NOT NULL, FOREIGN KEY (`users.user_id`) | Mã định danh học viên thực hiện điểm danh |
| `course_id` | UUID | NOT NULL, FOREIGN KEY (`courses.course_id`) | Mã định danh khóa học diễn ra buổi học |
| `attendance_date`| DATE | NOT NULL | Ngày diễn ra buổi điểm danh |
| `timestamp` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian chính xác hệ thống ghi nhận quét QR |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'PRESENT' | Trạng thái có mặt (`PRESENT`, `ABSENT`, `LATE`) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo bản ghi trong cơ sở dữ liệu |

**Cơ chế Idempotency & Ràng buộc Quan trọng:**
- `CONSTRAINT uq_attendance_idempotency UNIQUE (student_id, course_id, attendance_date)`: **Khóa tổng hợp ba cột** này là cốt lõi của cơ chế idempotency điểm danh quét mã QR (`[REQ-013]`). Khi một học viên quét mã QR nhiều lần trong cùng một ngày cho cùng một khóa học, cơ sở dữ liệu sẽ chặn bản ghi trùng lặp ở tầng lưu trữ, ngăn chặn việc tính công hoặc tích lũy điểm danh thừa, đồng thời hệ thống trả về phản hồi thành công kèm cờ `duplicate: true`.
- `CONSTRAINT chk_attendance_status CHECK (status IN ('PRESENT','ABSENT','LATE'))`
- `CREATE INDEX idx_attendance_student_id ON attendance(student_id);`
- `CREATE INDEX idx_attendance_course_id ON attendance(course_id);`
- `CREATE INDEX idx_attendance_date ON attendance(attendance_date);`

---

### 💳 1.4. Bảng Thẻ Thành Viên Học Viên (`student_cards`)
Bảng `student_cards` quản lý thông tin thẻ thành viên điện tử của học viên, thời hạn hiệu lực, số ngày còn lại và trạng thái thẻ.

- **Mã định danh Traceability Tag:** `[DAT-007]`, `[REQ-014]`, `[REQ-015]`
- **Đường dẫn mô-đun vật lý:** `./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `card_id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Khóa chính định danh thẻ thành viên |
| `student_id` | UUID | NOT NULL UNIQUE, FOREIGN KEY (`users.user_id`) | Mã định danh học viên sở hữu thẻ (1 học viên = 1 thẻ duy nhất) |
| `issue_date` | DATE | NOT NULL | Ngày phát hành thẻ ban đầu |
| `validity_days` | INT | NOT NULL | Tổng số ngày hiệu lực của thẻ |
| `remaining_days` | INT | NOT NULL | Số ngày hiệu lực còn lại tính đến hiện tại |
| `end_date` | DATE | NOT NULL | Ngày hết hạn chính thức của thẻ thành viên |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | Trạng thái thẻ (`ACTIVE`, `EXPIRED`, `SUSPENDED`) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo bản ghi thẻ |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm cập nhật bản ghi gần nhất |

**Ràng buộc kiểm tra (Check Constraints & Indexes):**
- `CONSTRAINT chk_student_cards_validity CHECK (validity_days > 0 AND remaining_days >= 0)`: Đảm bảo số ngày hợp lệ luôn dương và số ngày còn lại không âm.
- `CONSTRAINT chk_student_cards_status CHECK (status IN ('ACTIVE','EXPIRED','SUSPENDED'))`
- `CREATE INDEX idx_student_cards_student_id ON student_cards(student_id);`
- `CREATE INDEX idx_student_cards_status ON student_cards(status);`
- `CREATE INDEX idx_student_cards_end_date ON student_cards(end_date);`

---

## 🔗 2. MÔ TẢ QUAN HỆ KHÓA NGOẠI (FOREIGN KEY RELATIONSHIPS)

Toàn bộ hệ thống liên kết chặt chẽ qua các mối quan hệ khóa ngoại sau:
1. `courses.teacher_id` $\rightarrow$ `users.user_id`: Mỗi khóa học bắt buộc phải do một giáo viên phụ trách (`[DAT-004]`).
2. `courses.center_id` $\rightarrow$ `centers.center_id`: Khóa học trực thuộc một trung tâm đào tạo cụ thể (`[DAT-003]`, `[DAT-004]`).
3. `enrollments.student_id` $\rightarrow$ `users.user_id`: Liên kết học viên tham gia khóa học (`[DAT-005]`).
4. `enrollments.course_id` $\rightarrow$ `courses.course_id`: Liên kết khóa học nhận học viên ghi danh (`[DAT-005]`).
5. `attendance.student_id` $\rightarrow$ `users.user_id`: Xác định học viên thực hiện điểm danh (`[DAT-006]`).
6. `attendance.course_id` $\rightarrow$ `courses.course_id`: Xác định buổi học thuộc khóa học nào (`[DAT-006]`).
7. `student_cards.student_id` $\rightarrow$ `users.user_id`: Liên kết 1-1 giữa tài khoản học viên và thẻ thành viên (`[DAT-007]`).

---

## 🔍 3. TRACEABILITY MATRIX REFERENCE

| Mã Tag ID | Phân loại | Tên thực thể / Mô-đun liên quan | Vị trí tệp nguồn (Source Path) |
| :--- | :--- | :--- | :--- |
| `[DAT-004]` | Database Schema | Bảng `courses` | `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql` |
| `[DAT-005]` | Database Schema | Bảng `enrollments` | `./sources/backend/course-service/src/main/resources/db/migration/V2__init_enrollments.sql` |
| `[DAT-006]` | Database Schema | Bảng `attendance` | `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql` |
| `[DAT-007]` | Database Schema | Bảng `student_cards` | `./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql` |
| `[REQ-013]` | Business Rule | Idempotency quét QR | `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceService.java` |
| `[ARC-007]` | Architecture | Luồng điểm danh QR code | `./sources/docs/architecture/blueprint.md` |
```