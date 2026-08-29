```markdown
# 🏛️ BẢN THIẾT KẾ VẬT LÝ CƠ SỞ DỮ LIỆU QUAN HỆ - PHẦN 2
*(Physical Relational Database Mapping & ERD Blueprint - Part 2)*

## 📑 THÔNG TIN KIỂM SOÁT TÀI LIỆU & TRUY VẾT (TRACEABILITY METADATA)

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Đường dẫn tệp vật lý** | `./sources/docs/database/PHYSICAL_ERD_RELATIONAL_MAPPING.md` |
| **Không gian tên Java gốc** | `org.nlh4j.membershiphub` |
| **Phiên bản thiết kế** | v1.0.0 (Đường cơ sở sản xuất) |
| **Trạng thái phê duyệt** | Đã xác minh kiến trúc |
| **Mã định danh truy vết** | `[DAT-004]`, `[DAT-005]`, `[DAT-006]`, `[DAT-007]`, `[REQ-012]`, `[REQ-013]`, `[ARC-007]`, `[EXC-002]` |

---

## 📊 1. SƠ ĐỒ QUAN HỆ THỰC THỂ (PHYSICAL ERD)

Dưới đây là sơ đồ quan hệ thực thể (ERD) chi tiết mô tả cấu trúc vật lý của các bảng thuộc phân hệ Khóa học, Ghi danh, Điểm danh và Thẻ thành viên, cùng mối liên kết chặt chẽ với các thực thể cốt lõi `users` và `centers` đã được thiết lập ở Giai đoạn trước.

```mermaid
erDiagram
    users {
        UUID user_id PK
        VARCHAR email UK
        CHAR password_hash
        VARCHAR full_name
        SMALLINT role_id FK
        VARCHAR provider
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    centers {
        UUID center_id PK
        VARCHAR name
        VARCHAR address
        VARCHAR tax_id UK
        VARCHAR contact_phone
        VARCHAR contact_email
        UUID admin_user_id FK
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    courses {
        UUID course_id PK
        VARCHAR title
        TEXT description
        DATE start_date
        DATE end_date
        UUID teacher_id FK
        INT max_students
        UUID center_id FK
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    enrollments {
        UUID enrollment_id PK
        UUID student_id FK
        UUID course_id FK
        TIMESTAMP enrollment_date
        VARCHAR status
    }

    attendance {
        UUID attendance_id PK
        UUID student_id FK
        UUID course_id FK
        DATE attendance_date
        TIMESTAMP timestamp
        VARCHAR status
        TIMESTAMP created_at
    }

    student_cards {
        UUID card_id PK
        UUID student_id FK "UNIQUE"
        DATE issue_date
        INT validity_days
        INT remaining_days
        DATE end_date
        VARCHAR status
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    users ||--o{ courses : "giảng dạy (teacher_id)"
    centers ||--o{ courses : "tổ chức (center_id)"
    users ||--o{ enrollments : "đăng ký (student_id)"
    courses ||--o{ enrollments : "chứa (course_id)"
    users ||--o{ attendance : "điểm danh (student_id)"
    courses ||--o{ attendance : "ghi nhận (course_id)"
    users ||--wy student_cards : "sở hữu (student_id)"
```

---

## 💾 2. ĐẶC TẢ CHI TIẾT CÁC BẢNG DỮ LIỆU (TABLE SCHEMAS)

### 📅 2.1. Bảng `courses` (Quản lý Khóa học)
*   **Mã định danh truy vết:** `[DAT-004]`, `[REQ-007]`, `[REQ-008]`, `[REQ-009]`
*   **Mô tả nghiệp vụ:** Lưu trữ thông tin chi tiết về các khóa học được tổ chức tại các trung tâm đào tạo, bao gồm thời gian diễn ra, giới hạn sĩ số và giáo viên phụ trách.

| Tên cột (Column Name) | Kiểu dữ liệu (Data Type) | Ràng buộc (Constraints) | Mô tả nghiệp vụ (Vietnamese) | Targeted Tag IDs |
| :--- | :--- | :--- | :--- | :--- |
| `course_id` | `UUID` | PRIMARY KEY, DEFAULT `gen_random_uuid()` | Khóa chính định danh duy nhất cho từng khóa học. | `[DAT-004]` |
| `title` | `VARCHAR(150)` | NOT NULL | Tiêu đề của khóa học (Tối đa 150 ký tự). | `[DAT-004]` |
| `description` | `TEXT` | NULL | Mô tả chi tiết về nội dung, mục tiêu của khóa học. | `[DAT-004]` |
| `start_date` | `DATE` | NOT NULL | Ngày bắt đầu khóa học. | `[DAT-004]`, `[REQ-008]` |
| `end_date` | `DATE` | NOT NULL | Ngày kết thúc khóa học. | `[DAT-004]`, `[REQ-008]` |
| `teacher_id` | `UUID` | NOT NULL, FOREIGN KEY | Khóa ngoại liên kết tới `users(user_id)` (Vai trò Giáo viên). | `[DAT-004]`, `[REQ-009]` |
| `max_students` | `INT` | NOT NULL, DEFAULT `30` | Sĩ số học viên tối đa được phép đăng ký vào khóa học. | `[DAT-004]` |
| `center_id` | `UUID` | NULL, FOREIGN KEY | Khóa ngoại liên kết tới `centers(center_id)`. | `[DAT-004]` |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT `CURRENT_TIMESTAMP` | Thời điểm bản ghi được khởi tạo hệ thống. | `[DAT-004]` |
| `updated_at` | `TIMESTAMP` | NOT NULL, DEFAULT `CURRENT_TIMESTAMP` | Thời điểm bản ghi được cập nhật gần nhất. | `[DAT-004]` |

#### 🔒 Ràng buộc & Chỉ mục (Constraints & Indexes):
*   **Ràng buộc kiểm tra (Check Constraints):**
    *   `chk_courses_dates`: `CHECK (end_date >= start_date)` - Đảm bảo ngày kết thúc không được xảy ra trước ngày bắt đầu.
    *   `chk_courses_title_len`: `CHECK (char_length(title) <= 150)` - Giới hạn độ dài tiêu đề nghiêm ngặt chống tràn bộ đệm.
*   **Chỉ mục vật lý (Physical Indexes):**
    *   `idx_courses_teacher_id` (Non-Unique): Tối ưu hóa truy vấn danh sách khóa học theo giáo viên phụ trách.
    *   `idx_courses_center_id` (Non-Unique): Tối ưu hóa lọc khóa học theo từng trung tâm (Multi-tenancy isolation).
    *   `idx_courses_dates` (Composite): Chỉ mục trên cặp `(start_date, end_date)` phục vụ tìm kiếm khóa học đang hoạt động.
    *   `ux_courses_teacher_dates` (Unique Composite): Chỉ mục duy nhất trên `(teacher_id, start_date, end_date)` để ngăn chặn việc phân lịch trùng lặp cho cùng một giáo viên tại một thời điểm.

---

### 📝 2.2. Bảng `enrollments` (Ghi danh Học viên)
*   **Mã định danh truy vết:** `[DAT-005]`, `[REQ-010]`, `[REQ-011]`
*   **Mô tả nghiệp vụ:** Quản lý mối quan hệ đăng ký học giữa Học viên (`users`) và Khóa học (`courses`), kiểm soát trạng thái học tập của học viên.

| Tên cột (Column Name) | Kiểu dữ liệu (Data Type) | Ràng buộc (Constraints) | Mô tả nghiệp vụ (Vietnamese) | Targeted Tag IDs |
| :--- | :--- | :--- | :--- | :--- |
| `enrollment_id` | `UUID` | PRIMARY KEY, DEFAULT `gen_random_uuid()` | Khóa chính định danh duy nhất cho lượt ghi danh. | `[DAT-005]` |
| `student_id` | `UUID` | NOT NULL, FOREIGN KEY | Khóa ngoại liên kết tới `users(user_id)` (Vai trò Học viên). | `[DAT-005]`, `[REQ-011]` |
| `course_id` | `UUID` | NOT NULL, FOREIGN KEY | Khóa ngoại liên kết tới `courses(course_id)`. | `[DAT-005]`, `[REQ-011]` |
| `enrollment_date` | `TIMESTAMP` | NOT NULL, DEFAULT `CURRENT_TIMESTAMP` | Thời điểm học viên thực hiện đăng ký khóa học. | `[DAT-005]` |
| `status` | `VARCHAR(20)` | NOT NULL, DEFAULT `'ACTIVE'` | Trạng thái ghi danh: `ACTIVE`, `DROPPED`, `COMPLETED`. | `[DAT-005]`, `[REQ-011]` |

#### 🔒 Ràng buộc & Chỉ mục (Constraints & Indexes):
*   **Ràng buộc kiểm tra (Check Constraints):**
    *   `chk_enrollments_status`: `CHECK (status IN ('ACTIVE', 'DROPPED', 'COMPLETED'))` - Giới hạn các trạng thái ghi danh hợp lệ.
*   **Ràng buộc duy nhất (Unique Constraints):**
    *   `uq_enrollments_student_course`: `UNIQUE (student_id, course_id)` - Ngăn chặn một học viên đăng ký trùng lặp nhiều lần vào cùng một khóa học.
*   **Chỉ mục vật lý (Physical Indexes):**
    *   `idx_enrollments_student_id` (Non-Unique): Tăng tốc độ truy vấn danh sách khóa học đã đăng ký của một học viên cụ thể.
    *   `idx_enrollments_course_id` (Non-Unique): Tăng tốc độ thống kê sĩ số và danh sách lớp học của một khóa học.
    *   `idx_enrollments_status` (Non-Unique): Hỗ trợ lọc nhanh các học viên đang hoạt động hoặc đã rút lui.

---

### 🎯 2.3. Bảng `attendance` (Điểm danh QR)
*   **Mã định danh truy vết:** `[DAT-006]`, `[REQ-012]`, `[REQ-013]`, `[ARC-007]`, `[EXC-002]`
*   **Mô tả nghiệp vụ:** Ghi nhận lịch sử điểm danh hàng ngày của học viên khi quét mã QR tại lớp học, tích hợp cơ chế chống trùng lặp dữ liệu tuyệt đối.

| Tên cột (Column Name) | Kiểu dữ liệu (Data Type) | Ràng buộc (Constraints) | Mô tả nghiệp vụ (Vietnamese) | Targeted Tag IDs |
| :--- | :--- | :--- | :--- | :--- |
| `attendance_id` | `UUID` | PRIMARY KEY, DEFAULT `gen_random_uuid()` | Khóa chính định danh duy nhất cho phiên điểm danh. | `[DAT-006]` |
| `student_id` | `UUID` | NOT NULL, FOREIGN KEY | Khóa ngoại liên kết tới `users(user_id)` (Học viên quét mã). | `[DAT-006]`, `[REQ-012]` |
| `course_id` | `UUID` | NOT NULL, FOREIGN KEY | Khóa ngoại liên kết tới `courses(course_id)`. | `[DAT-006]`, `[REQ-012]` |
| `attendance_date` | `DATE` | NOT NULL | Ngày diễn ra buổi học được điểm danh (Không chứa giờ). | `[DAT-006]`, `[REQ-013]` |
| `timestamp` | `TIMESTAMP` | NOT NULL, DEFAULT `CURRENT_TIMESTAMP` | Thời gian chính xác (giờ, phút, giây) hệ thống ghi nhận quét QR. | `[DAT-006]`, `[REQ-012]` |
| `status` | `VARCHAR(20)` | NOT NULL, DEFAULT `'PRESENT'` | Trạng thái điểm danh: `PRESENT`, `ABSENT`, `LATE`. | `[DAT-006]`, `[REQ-012]` |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT `CURRENT_TIMESTAMP` | Thời điểm bản ghi được tạo trong cơ sở dữ liệu. | `[DAT-006]` |

#### 🔒 Ràng buộc & Chỉ mục (Constraints & Indexes):
*   **Ràng buộc kiểm tra (Check Constraints):**
    *   `chk_attendance_status`: `CHECK (status IN ('PRESENT', 'ABSENT', 'LATE'))` - Đảm bảo tính nhất quán của dữ liệu điểm danh.
*   **Khóa tổng hợp duy nhất (Idempotency Composite Key):**
    *   `uq_attendance_idempotency`: `UNIQUE (student_id, course_id, attendance_date)` - **RÀO CHẮN IDEMPOTENCY CỐT LÕI**. Ràng buộc này ngăn chặn tuyệt đối việc ghi nhận điểm danh trùng lặp cho cùng một học viên trong cùng một ngày đối với một khóa học, ngay cả khi học viên quét mã QR nhiều lần do lỗi mạng hoặc độ trễ thiết bị di động.
*   **Chỉ mục vật lý (Physical Indexes):**
    *   `idx_attendance_student_id` (Non-Unique): Tối ưu hóa truy vấn lịch sử chuyên cần của một học viên.
    *   `idx_attendance_course_id` (Non-Unique): Tối ưu hóa báo cáo điểm danh theo lớp học.
    *   `idx_attendance_date` (Non-Unique): Hỗ trợ kết xuất báo cáo điểm danh hàng ngày nhanh chóng.

---

### 💳 2.4. Bảng `student_cards` (Thẻ thành viên Học viên)
*   **Mã định danh truy vết:** `[DAT-007]`, `[REQ-014]`, `[REQ-015]`
*   **Mô tả nghiệp vụ:** Quản lý thông tin thẻ thành viên, thời hạn hiệu lực, số ngày còn lại và trạng thái kích hoạt của học viên trong hệ thống.

| Tên cột (Column Name) | Kiểu dữ liệu (Data Type) | Ràng buộc (Constraints) | Mô tả nghiệp vụ (Vietnamese) | Targeted Tag IDs |
| :--- | :--- | :--- | :--- | :--- |
| `card_id` | `UUID` | PRIMARY KEY, DEFAULT `gen_random_uuid()` | Khóa chính định danh duy nhất cho thẻ thành viên. | `[DAT-007]` |
| `student_id` | `UUID` | NOT NULL, UNIQUE, FOREIGN KEY | Khóa ngoại liên kết tới `users(user_id)` (Mỗi học viên chỉ có tối đa 1 thẻ). | `[DAT-007]`, `[REQ-014]` |
| `issue_date` | `DATE` | NOT NULL | Ngày phát hành thẻ lần đầu tiên. | `[DAT-007]` |
| `validity_days` | `INT` | NOT NULL | Tổng số ngày hiệu lực được cấp (Cộng dồn khi gia hạn). | `[DAT-007]`, `[REQ-015]` |
| `remaining_days` | `INT` | NOT NULL | Số ngày hiệu lực còn lại của thẻ (Tự động trừ hàng ngày). | `[DAT-007]`, `[REQ-014]` |
| `end_date` | `DATE` | NOT NULL | Ngày hết hạn chính xác của thẻ thành viên. | `[DAT-007]`, `[REQ-015]` |
| `status` | `VARCHAR(20)` | NOT NULL, DEFAULT `'ACTIVE'` | Trạng thái hoạt động của thẻ: `ACTIVE`, `EXPIRED`, `SUSPENDED`. | `[DAT-007]`, `[REQ-014]` |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT `CURRENT_TIMESTAMP` | Thời điểm khởi tạo thẻ trên hệ thống. | `[DAT-007]` |
| `updated_at` | `TIMESTAMP` | NOT NULL, DEFAULT `CURRENT_TIMESTAMP` | Thời điểm cập nhật thông tin thẻ gần nhất. | `[DAT-007]` |

#### 🔒 Ràng buộc & Chỉ mục (Constraints & Indexes):
*   **Ràng buộc kiểm tra (Check Constraints):**
    *   `chk_student_cards_validity`: `CHECK (validity_days > 0 AND remaining_days >= 0)` - Đảm bảo số ngày hiệu lực luôn hợp lệ về mặt toán học.
    *   `chk_student_cards_status`: `CHECK (status IN ('ACTIVE', 'EXPIRED', 'SUSPENDED'))` - Kiểm soát trạng thái vòng đời của thẻ.
*   **Chỉ mục vật lý (Physical Indexes):**
    *   `idx_student_cards_student_id` (Unique): Đảm bảo tính duy nhất 1-1 giữa học viên và thẻ thành viên, tăng tốc độ tra cứu thẻ cá nhân.
    *   `idx_student_cards_status` (Non-Unique): Phục vụ các tiến trình tự động (Cron Job) quét và cập nhật trạng thái thẻ hết hạn hàng ngày.
    *   `idx_student_cards_end_date` (Non-Unique): Tối ưu hóa truy vấn cảnh báo thẻ sắp hết hạn gửi tới thiết bị di động.

---

## 🔗 3. ĐẶC TẢ QUAN HỆ KHÓA NGOẠI (FOREIGN KEY RELATIONSHIPS)

Để đảm bảo tính toàn vẹn tham chiếu tuyệt đối (Referential Integrity) tại tầng cơ sở dữ liệu, các ràng buộc khóa ngoại được thiết lập cứng với các quy tắc ứng xử dữ liệu rõ ràng:

1.  **`courses.teacher_id` ➔ `users.user_id`**
    *   *Loại quan hệ:* Một - Nhiều (One-to-Many). Một giáo viên có thể giảng dạy nhiều khóa học.
    *   *Hành vi:* `ON DELETE RESTRICT` - Không cho phép xóa tài khoản giáo viên nếu họ đang được phân công giảng dạy ít nhất một khóa học đang hoặc sắp diễn ra.
2.  **`courses.center_id` ➔ `centers.center_id`**
    *   *Loại quan hệ:* Một - Nhiều (One-to-Many). Một trung tâm quản lý nhiều khóa học.
    *   *Hành vi:* `ON DELETE CASCADE` - Khi một trung tâm ngừng hoạt động và bị xóa khỏi hệ thống, toàn bộ khóa học thuộc trung tâm đó sẽ tự động bị xóa để giải phóng tài nguyên.
3.  **`enrollments.student_id` ➔ `users.user_id`**
    *   *Loại quan hệ:* Một - Nhiều (One-to-Many). Một học viên có thể đăng ký nhiều khóa học.
    *   *Hành vi:* `ON DELETE RESTRICT` - Ngăn chặn việc xóa tài khoản học viên nếu tồn tại lịch sử ghi danh khóa học để bảo toàn dữ liệu tài chính và học tập.
4.  **`enrollments.course_id` ➔ `courses.course_id`**
    *   *Loại quan hệ:* Một - Nhiều (One-to-Many). Một khóa học chứa nhiều lượt ghi danh.
    *   *Hành vi:* `ON DELETE RESTRICT` - Không cho phép xóa khóa học nếu đã có học viên đăng ký thành công.
5.  **`attendance.student_id` ➔ `users.user_id`**
    *   *Loại quan hệ:* Một - Nhiều (One-to-Many). Một học viên có nhiều lượt điểm danh.
    *   *Hành vi:* `ON DELETE RESTRICT` - Bảo toàn dữ liệu chuyên cần phục vụ công tác kiểm toán và báo cáo.
6.  **`attendance.course_id` ➔ `courses.course_id`**
    *   *Loại quan hệ:* Một - Nhiều (One-to-Many). Một khóa học có nhiều lượt điểm danh hàng ngày.
    *   *Hành vi:* `ON DELETE RESTRICT` - Ngăn chặn xóa khóa học khi đã phát sinh dữ liệu điểm danh thực tế.
7.  **`student_cards.student_id` ➔ `users.user_id`**
    *   *Loại quan hệ:* Một - Một (One-to-One). Mỗi học viên sở hữu duy nhất một thẻ thành viên.
    *   *Hành vi:* `ON DELETE CASCADE` - Khi tài khoản học viên bị xóa hoàn toàn khỏi hệ thống (tuân thủ quyền xóa dữ liệu GDPR), thẻ thành viên liên kết sẽ tự động bị hủy bỏ.

---

## 🛡️ 4. CƠ CHẾ BẢO VỆ IDEMPOTENCY TRONG ĐIỂM DANH QR

Trong các hệ thống điểm danh thời gian thực sử dụng mã QR, hiện tượng "Double-Tap" (học viên quét mã liên tục do thiết bị phản hồi chậm) hoặc lỗi mạng gửi trùng lặp yêu cầu (Network Retry) là cực kỳ phổ biến. Nếu không được kiểm soát, hệ thống sẽ sinh ra hàng loạt bản ghi điểm danh trùng lặp cho cùng một buổi học, làm sai lệch báo cáo chuyên cần.

Hệ thống `membership-hub` giải quyết triệt để vấn đề này bằng cơ chế bảo vệ hai lớp:

### 4.1. Lớp Cơ sở dữ liệu (Database-Level Guard)
Sử dụng ràng buộc duy nhất tổng hợp (Unique Composite Constraint) trên bảng `attendance`:
```sql
ALTER TABLE attendance 
ADD CONSTRAINT uq_attendance_idempotency UNIQUE (student_id, course_id, attendance_date);
```
Khi có một yêu cầu điểm danh trùng lặp gửi tới, PostgreSQL sẽ lập tức chặn đứng giao dịch ở mức vật lý và ném ra mã lỗi vi phạm ràng buộc duy nhất (`SQLSTATE 23505`). Điều này đảm bảo dữ liệu không bao giờ bị sai lệch bất kể lỗi phát sinh từ tầng ứng dụng.

### 4.2. Lớp Ứng dụng (Application-Level Graceful Handling)
Tại vi dịch vụ `attendance-service` (Quarkus), thay vì trả về lỗi hệ thống 500 khi xảy ra vi phạm ràng buộc, khối xử lý ngoại lệ sẽ bắt mã lỗi `23505` và chuyển đổi thành một phản hồi thành công có chủ đích:
*   **HTTP Status Code:** `200 OK`
*   **JSON Response Payload:**
    ```json
    {
      "recorded": false,
      "duplicate": true,
      "message": "Điểm danh đã được ghi nhận trước đó trong ngày"
    }
    ```
Cơ chế này giúp ứng dụng di động của học viên hiển thị thông báo trạng thái "Đã điểm danh" một cách mượt mà, tránh gây hoang mang cho người dùng cuối [EXC-002].

---

## 🗺️ 5. MA TRẬN TRUY VẾT YÊU CẦU (TRACEABILITY MATRIX REFERENCE)

Để đảm bảo tính minh bạch và khả năng kiểm toán hệ thống, dưới đây là bảng ánh xạ trực tiếp giữa các thực thể cơ sở dữ liệu vật lý và các yêu cầu nghiệp vụ/kiến trúc tương ứng:

| Mã thực thể | Tên bảng vật lý | Mã yêu cầu nghiệp vụ (SRS) | Mã kiến trúc (Architecture) | Mã phi chức năng (NFR) |
| :--- | :--- | :--- | :--- | :--- |
| **`[DAT-004]`** | `courses` | `[REQ-007]`, `[REQ-008]`, `[REQ-009]` | `[ARC-000]` | `[NFR-001]`, `[NFR-003]` |
| **`[DAT-005]`** | `enrollments` | `[REQ-010]`, `[REQ-011]` | `[ARC-008]` | `[NFR-003]` |
| **`[DAT-006]`** | `attendance` | `[REQ-012]`, `[REQ-013]` | `[ARC-007]` | `[NFR-001]`, `[NFR-003]` |
| **`[DAT-007]`** | `student_cards` | `[REQ-014]`, `[REQ-015]` | `[ARC-009]` | `[NFR-003]`, `[NFR-008]` |

---

## ⚙️ 6. HƯỚNG DẪN DI TRÚ SCHEMA (FLYWAY MIGRATION SCRIPT)

Dưới đây là tập lệnh SQL di trú chuẩn ANSI SQL được triển khai trong dự án tại đường dẫn `./sources/backend/attendance-service/src/main/resources/db/migration/V3__enrollment_unique_index.sql` để thiết lập các ràng buộc vật lý nêu trên:

```sql
-- =====================================================================
-- V3__enrollment_unique_index.sql
-- Khởi tạo các ràng buộc duy nhất và chỉ mục tối ưu hóa hiệu năng
-- Phân hệ: Khóa học, Ghi danh, Điểm danh và Thẻ thành viên
-- Thiết kế bởi: Kiến Trúc Sư Hệ Thống Doanh Nghiệp
-- Tags: [DAT-004], [DAT-005], [DAT-006], [DAT-007], [REQ-013]
-- =====================================================================

-- 1. Áp dụng ràng buộc duy nhất cho bảng enrollments để tránh ghi danh trùng lặp
ALTER TABLE enrollments
    ADD CONSTRAINT ux_enrollments_student_course UNIQUE (student_id, course_id);

-- 2. Áp dụng khóa tổng hợp duy nhất (Idempotency Key) cho bảng attendance
ALTER TABLE attendance
    ADD CONSTRAINT ux_attendance_student_course_date UNIQUE (student_id, course_id, attendance_date);

-- 3. Tạo chỉ mục tối ưu hóa truy vấn tìm kiếm và kết xuất báo cáo điểm danh theo ngày
CREATE INDEX ix_attendance_course_date ON attendance (course_id, attendance_date);

-- 4. Tạo chỉ mục tối ưu hóa truy vấn danh sách học viên thuộc khóa học
CREATE INDEX ix_enrollments_course ON enrollments (course_id);

-- 5. Tạo chỉ mục hỗ trợ tiến trình tự động quét trạng thái thẻ thành viên hàng ngày
CREATE INDEX idx_student_cards_status_end ON student_cards (status, end_date);
```
```