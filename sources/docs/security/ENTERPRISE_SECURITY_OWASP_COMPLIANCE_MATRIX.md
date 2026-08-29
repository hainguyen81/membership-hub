```markdown
# 🛡️ ENTERPRISE SECURITY & OWASP COMPLIANCE MATRIX
// [DAT-004] // [DAT-005] // [DAT-006] // [DAT-007]

## 1. TỔNG QUAN HỆ THỐNG & ĐỐI TƯỢNG BẢO MẬT
Tài liệu này xác định ma trận tuân thủ bảo mật và tiêu chuẩn kỹ thuật OWASP Top 10 cho hệ thống **Membership Hub** (`org.nlh4j.membershiphub`), tập trung vào các thực thể dữ liệu cốt lõi được khởi tạo trong giai đoạn di trú schema phần 2 và phần 3 (`courses`, `enrollments`, `attendance`, `student_cards`).

---

## 2. MA TRẬN TRUY XUẤT NGUỒN GỐC & GẮN THẺ (TRACEABILITY MATRIX)
Mỗi bảng cơ sở dữ liệu và quy tắc bảo mật được ánh xạ trực tiếp đến các yêu cầu hệ thống và thẻ kiểm soát tuân thủ phi chức năng:

| Thành phần Cơ sở dữ liệu / Mô-đun | Mã Thẻ Định Danh (Tag ID) | Tiêu chuẩn OWASP / Bảo mật Áp dụng | Mô tả Mục đích Kỹ thuật |
| :--- | :--- | :--- | :--- |
| **Bảng `courses`** | `[DAT-004]`, `[REQ-007]`, `[REQ-008]` | A01:2021-Broken Access Control | Quản lý thông tin khóa học, phân công giáo viên và ràng buộc lịch trình tránh chồng lấn thời gian. |
| **Bảng `enrollments`** | `[DAT-005]`, `[REQ-010]`, `[REQ-011]` | A04:2021-Insecure Design | Quản lý quan hệ ghi danh học viên - khóa học với ràng buộc duy nhất (UNIQUE) chống đăng ký trùng lặp. |
| **Bảng `attendance`** | `[DAT-006]`, `[REQ-012]`, `[REQ-013]` | A01:2021-Broken Access Control | Ghi nhận điểm danh QR code với khóa tổng hợp độc nhất bảo đảm tính bất biến (idempotency). |
| **Bảng `student_cards`** | `[DAT-007]`, `[REQ-014]`, `[REQ-015]` | A07:2021-Identification and Authentication Failures | Quản lý thẻ thành viên, thời hạn hiệu lực và gia hạn thẻ an toàn qua cổng thanh toán. |

---

## 3. SƠ ĐỒ QUAN HỆ THỰC THỂ (ERD) - PHẦN 2 & 3
Phần này mô tả chi tiết cấu trúc các bảng dữ liệu liên quan trực tiếp đến quy trình khóa học, ghi danh, điểm danh và quản lý thẻ thành viên trong hệ thống phân tán Quarkus.

### 3.1. Bảng `courses` (Quản lý Khóa học)
* **Mục đích:** Lưu trữ thông tin chi tiết về các khóa học, khoảng thời gian diễn ra, giáo viên phụ trách và giới hạn số lượng học viên.
* **Ánh xạ Traceability:** `[DAT-004]`, `[REQ-007]`, `[REQ-008]`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `course_id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Định danh duy nhất của khóa học |
| `title` | VARCHAR(150) | NOT NULL, CHECK (char_length(title) <= 150) | Tiêu đề khóa học |
| `description` | TEXT | NULLABLE | Mô tả chi tiết nội dung khóa học |
| `start_date` | DATE | NOT NULL | Ngày bắt đầu khóa học |
| `end_date` | DATE | NOT NULL, CHECK (end_date >= start_date) | Ngày kết thúc khóa học |
| `teacher_id` | UUID | NOT NULL, FOREIGN KEY (`users.user_id`) | Định danh giáo viên phụ trách |
| `max_students` | INT | NOT NULL, DEFAULT 30 | Số lượng học viên tối đa cho phép |
| `center_id` | UUID | NULLABLE, FOREIGN KEY (`centers.center_id`) | Định danh trung tâm đào tạo trực thuộc |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo bản ghi |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm cập nhật bản ghi gần nhất |

---

### 3.2. Bảng `enrollments` (Quản lý Ghi danh)
* **Mục đích:** Lưu trữ quan hệ đăng ký khóa học giữa học viên và các khóa học khả dụng.
* **Ánh xạ Traceability:** `[DAT-005]`, `[REQ-010]`, `[REQ-011]`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `enrollment_id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Định danh duy nhất của bản ghi ghi danh |
| `student_id` | UUID | NOT NULL, FOREIGN KEY (`users.user_id`) | Định danh học viên đăng ký |
| `course_id` | UUID | NOT NULL, FOREIGN KEY (`courses.course_id`) | Định danh khóa học được đăng ký |
| `enrollment_date` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm thực hiện ghi danh |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE', CHECK (status IN ('ACTIVE','DROPPED','COMPLETED')) | Trạng thái ghi danh hiện tại |

> **Ràng buộc Duy nhất (Idempotency / Unique Constraint):** `UNIQUE (student_id, course_id)` ngăn chặn học viên đăng ký nhiều lần vào cùng một khóa học.

---

### 3.3. Bảng `attendance` (Quản lý Điểm danh QR)
* **Mục đích:** Lưu trữ lịch sử điểm danh của học viên quét mã QR cho từng buổi học, bảo đảm tính chống trùng lặp.
* **Ánh xạ Traceability:** `[DAT-006]`, `[REQ-012]`, `[REQ-013]`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `attendance_id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Định danh duy nhất của bản ghi điểm danh |
| `student_id` | UUID | NOT NULL, FOREIGN KEY (`users.user_id`) | Định danh học viên điểm danh |
| `course_id` | UUID | NOT NULL, FOREIGN KEY (`courses.course_id`) | Định danh khóa học |
| `attendance_date` | DATE | NOT NULL | Ngày điểm danh |
| `timestamp` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm quét mã QR chính xác |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'PRESENT', CHECK (status IN ('PRESENT','ABSENT','LATE')) | Trạng thái chuyên cần |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo bản ghi hệ thống |

> **Khóa Tổng hợp Đảm bảo Idempotency:** Ràng buộc `UNIQUE (student_id, course_id, attendance_date)` bảo đảm trong một ngày học, học viên chỉ được ghi nhận điểm danh một lần duy nhất cho mỗi khóa học, ngăn chặn hoàn toàn việc gửi yêu cầu lặp lại (retry request).

---

### 3.4. Bảng `student_cards` (Quản lý Thẻ Thành viên)
* **Mục đích:** Lưu trữ thông tin thẻ thành viên, thời hạn hiệu lực và số ngày còn lại của học viên.
* **Ánh xạ Traceability:** `[DAT-007]`, `[REQ-014]`, `[REQ-015]`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `card_id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Định danh duy nhất của thẻ thành viên |
| `student_id` | UUID | NOT NULL, UNIQUE, FOREIGN KEY (`users.user_id`) | Định danh học viên sở hữu thẻ |
| `issue_date` | DATE | NOT NULL | Ngày phát hành thẻ |
| `validity_days` | INT | NOT NULL, CHECK (validity_days > 0) | Tổng số ngày hiệu lực của thẻ |
| `remaining_days` | INT | NOT NULL, CHECK (remaining_days >= 0) | Số ngày còn lại có thể sử dụng |
| `end_date` | DATE | NOT NULL | Ngày hết hạn hiệu lực của thẻ |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE', CHECK (status IN ('ACTIVE','EXPIRED','SUSPENDED')) | Trạng thái thẻ |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo bản ghi |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm cập nhật bản ghi |

---

## 4. QUAN HỆ KHÓA NGOẠI (FOREIGN KEY RELATIONSHIPS)
Toàn bộ các thực thể trên được liên kết chặt chẽ với nhau theo mô hình cơ sở dữ liệu quan hệ tuân thủ chuẩn ANSI SQL:
1. `courses.teacher_id` ➔ `users.user_id` (`[DAT-004]`)
2. `courses.center_id` ➔ `centers.center_id` (`[DAT-004]`)
3. `enrollments.student_id` ➔ `users.user_id` (`[DAT-005]`)
4. `enrollments.course_id` ➔ `courses.course_id` (`[DAT-005]`)
5. `attendance.student_id` ➔ `users.user_id` (`[DAT-006]`)
6. `attendance.course_id` ➔ `courses.course_id` (`[DAT-006]`)
7. `student_cards.student_id` ➔ `users.user_id` (`[DAT-007]`)

---

## 5. TIÊU CHUẨN AN NINH & BẢO VỆ DỮ LIỆU CÁ NHÂN (GDPR/CCPA)
* **Che giấu Dữ liệu Nhạy cảm (PII Masking):** Mọi thông tin định danh cá nhân liên quan đến học viên trong các bảng điểm danh và thẻ thành viên đều được lọc qua `PiiMaskingFilter` trước khi ghi nhật ký hệ thống.
* **Mã hóa Lưu trữ (At-Rest Encryption):** Dữ liệu trong PostgreSQL được mã hóa toàn diện theo chuẩn AES-256.
* **Kiểm toán Truy cập (Audit Trail):** Mọi thao tác thay đổi trạng thái thẻ thành viên hoặc ghi nhận điểm danh đều phát sinh sự kiện kiểm toán lưu giữ tối thiểu 1 năm trên Google Cloud Storage Coldline.
```
```