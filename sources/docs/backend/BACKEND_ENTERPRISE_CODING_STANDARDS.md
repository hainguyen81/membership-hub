```markdown
# TIÊU CHUẨN LẬP TRÌNH DOANH NGHIỆP & THIẾT KẾ CƠ SỞ DỮ LIỆU PHÂN HỆ LỚP HỌC, GHI DANH, ĐIỂM DANH VÀ THẺ THÀNH VIÊN
*(BACKEND ENTERPRISE CODING STANDARDS & DATABASE SCHEMA PART 2)*

- **Mã Bản Thiết Kế:** ARCH-20260829122721
- **Đường Dẫn Tài Liệu:** `./sources/docs/backend/BACKEND_ENTERPRISE_CODING_STANDARDS.md`
- **Không Gian Tên Gốc (Java Package Prefix):** `org.nlh4j.membershiphub`
- **Phiên Bản:** 1.0 (Đường Cơ Sở)

---

## 🏛️ 1. SƠ ĐỒ QUAN HỆ THỰC THỂ (ERD) - PHẦN 2

Phần này mô tả chi tiết cấu trúc dữ liệu, các ràng buộc toàn vẹn, chỉ mục tối ưu hiệu năng và mối quan hệ giữa các bảng cốt lõi thuộc phân hệ quản lý khóa học, ghi danh, điểm danh và thẻ thành viên. Các bảng được thiết kế tuân thủ nghiêm ngặt chuẩn ANSI SQL, sử dụng kiểu dữ liệu chuẩn hóa và cơ chế khóa ngoại để bảo vệ tính toàn vẹn dữ liệu ở mức vật lý.

### 📊 1.1. Chi Tiết Các Bảng Dữ Liệu

#### 1.1.1. Bảng `courses` (Quản lý thông tin khóa học) [DAT-004]
Bảng này lưu trữ thông tin chi tiết về các khóa học được tổ chức tại các trung tâm đào tạo, bao gồm thời gian diễn ra, số lượng học viên tối đa và giáo viên phụ trách.

| Tên cột (Column Name) | Kiểu dữ liệu (Data Type) | Ràng buộc (Constraints) | Mô tả (Description) | Targeted Tag IDs |
| :--- | :--- | :--- | :--- | :--- |
| `course_id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Khóa chính định danh duy nhất cho từng khóa học. | `[DAT-004]` |
| `title` | VARCHAR(150) | NOT NULL | Tiêu đề hoặc tên của khóa học (Tối đa 150 ký tự). | `[DAT-004]` |
| `description` | TEXT | NULL | Mô tả chi tiết về nội dung, mục tiêu của khóa học. | `[DAT-004]` |
| `start_date` | DATE | NOT NULL | Ngày bắt đầu khóa học. | `[DAT-004]` |
| `end_date` | DATE | NOT NULL | Ngày kết thúc khóa học (Bắt buộc `end_date >= start_date`). | `[DAT-004]` |
| `teacher_id` | UUID | NOT NULL, FOREIGN KEY | ID của giáo viên phụ trách khóa học (Tham chiếu `users.user_id`). | `[DAT-004]` |
| `max_students` | INT | NOT NULL, DEFAULT 30 | Số lượng học viên tối đa được phép đăng ký vào lớp. | `[DAT-004]` |
| `center_id` | UUID | NOT NULL, FOREIGN KEY | ID của trung tâm tổ chức khóa học (Tham chiếu `centers.center_id`). | `[DAT-004]` |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm bản ghi được khởi tạo trên hệ thống. | `[DAT-004]` |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm bản ghi được cập nhật gần nhất. | `[DAT-004]` |

#### 1.1.2. Bảng `enrollments` (Quản lý ghi danh học viên) [DAT-005]
Bảng trung gian thể hiện mối quan hệ nhiều-nhiều giữa học viên (`users`) và khóa học (`courses`), ghi nhận trạng thái tham gia của học viên.

| Tên cột (Column Name) | Kiểu dữ liệu (Data Type) | Ràng buộc (Constraints) | Mô tả (Description) | Targeted Tag IDs |
| :--- | :--- | :--- | :--- | :--- |
| `enrollment_id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Khóa chính định danh duy nhất cho lượt ghi danh. | `[DAT-005]` |
| `student_id` | UUID | NOT NULL, FOREIGN KEY | ID của học viên tham gia khóa học (Tham chiếu `users.user_id`). | `[DAT-005]` |
| `course_id` | UUID | NOT NULL, FOREIGN KEY | ID của khóa học được đăng ký (Tham chiếu `courses.course_id`). | `[DAT-005]` |
| `enrollment_date` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm học viên thực hiện đăng ký khóa học. | `[DAT-005]` |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | Trạng thái ghi danh. Chỉ chấp nhận: `ACTIVE`, `DROPPED`, `COMPLETED`. | `[DAT-005]` |

#### 1.1.3. Bảng `attendance` (Nhật ký điểm danh lớp học) [DAT-006]
Lưu trữ lịch sử điểm danh hàng ngày của học viên đối với từng buổi học cụ thể thuộc khóa học.

| Tên cột (Column Name) | Kiểu dữ liệu (Data Type) | Ràng buộc (Constraints) | Mô tả (Description) | Targeted Tag IDs |
| :--- | :--- | :--- | :--- | :--- |
| `attendance_id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Khóa chính định danh duy nhất cho bản ghi điểm danh. | `[DAT-006]` |
| `student_id` | UUID | NOT NULL, FOREIGN KEY | ID của học viên được điểm danh (Tham chiếu `users.user_id`). | `[DAT-006]` |
| `course_id` | UUID | NOT NULL, FOREIGN KEY | ID của khóa học diễn ra buổi điểm danh (Tham chiếu `courses.course_id`). | `[DAT-006]` |
| `attendance_date` | DATE | NOT NULL | Ngày diễn ra buổi học thực tế được điểm danh. | `[DAT-006]` |
| `timestamp` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm chính xác hệ thống ghi nhận quét mã QR thành công. | `[DAT-006]` |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'PRESENT' | Trạng thái điểm danh. Chỉ chấp nhận: `PRESENT`, `ABSENT`, `LATE`. | `[DAT-006]` |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo bản ghi điểm danh trên hệ thống. | `[DAT-006]` |

#### 1.1.4. Bảng `student_cards` (Quản lý thẻ thành viên học viên) [DAT-007]
Quản lý thông tin thẻ thành viên, thời hạn hiệu lực và số ngày sử dụng dịch vụ còn lại của từng học viên.

| Tên cột (Column Name) | Kiểu dữ liệu (Data Type) | Ràng buộc (Constraints) | Mô tả (Description) | Targeted Tag IDs |
| :--- | :--- | :--- | :--- | :--- |
| `card_id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Khóa chính định danh duy nhất cho thẻ thành viên. | `[DAT-007]` |
| `student_id` | UUID | NOT NULL, UNIQUE, FOREIGN KEY | ID của học viên sở hữu thẻ (Tham chiếu `users.user_id` - Quan hệ 1:1). | `[DAT-007]` |
| `issue_date` | DATE | NOT NULL | Ngày phát hành hoặc kích hoạt thẻ thành viên. | `[DAT-007]` |
| `validity_days` | INT | NOT NULL | Tổng số ngày hiệu lực được cấp (Yêu cầu `validity_days > 0`). | `[DAT-007]` |
| `remaining_days` | INT | NOT NULL | Số ngày hiệu lực còn lại của thẻ (Yêu cầu `remaining_days >= 0`). | `[DAT-007]` |
| `end_date` | DATE | NOT NULL | Ngày hết hạn chính thức của thẻ thành viên. | `[DAT-007]` |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | Trạng thái thẻ. Chỉ chấp nhận: `ACTIVE`, `EXPIRED`, `SUSPENDED`. | `[DAT-007]` |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm hệ thống khởi tạo thẻ thành viên. | `[DAT-007]` |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời điểm cập nhật thông tin thẻ gần nhất. | `[DAT-007]` |

---

### 🔗 1.2. Mô Tả Quan Hệ Khóa Ngoại (FOREIGN KEY)

Để đảm bảo tính toàn vẹn tham chiếu tuyệt đối giữa các vi dịch vụ và các bảng dữ liệu trong hệ thống, các ràng buộc khóa ngoại sau đây được thiết lập cứng ở mức cơ sở dữ liệu:

1.  **`courses.teacher_id` → `users.user_id`:**
    *   *Ý nghĩa:* Đảm bảo một khóa học luôn được gán cho một giáo viên hợp lệ tồn tại trong hệ thống.
    *   *Hành vi:* `ON DELETE RESTRICT` - Không cho phép xóa tài khoản người dùng nếu họ đang là giáo viên phụ trách của ít nhất một khóa học đang hoạt động.
2.  **`courses.center_id` → `centers.center_id`:**
    *   *Ý nghĩa:* Ràng buộc khóa học phải thuộc về một trung tâm đào tạo cụ thể được quản lý hợp lệ.
    *   *Hành vi:* `ON DELETE CASCADE` - Khi một trung tâm bị giải thể hoặc xóa khỏi hệ thống, toàn bộ các khóa học thuộc trung tâm đó sẽ tự động bị xóa bỏ để tránh dữ liệu mồ côi.
3.  **`enrollments.student_id` → `users.user_id`:**
    *   *Ý nghĩa:* Đảm bảo bản ghi ghi danh luôn tham chiếu đến một tài khoản học viên thực tế.
    *   *Hành vi:* `ON DELETE RESTRICT` - Ngăn chặn việc xóa tài khoản học viên khi học viên đó đang có các bản ghi đăng ký khóa học đang hoạt động.
4.  **`enrollments.course_id` → `courses.course_id`:**
    *   *Ý nghĩa:* Đảm bảo học viên chỉ có thể đăng ký vào các khóa học hiện hữu trong cơ sở dữ liệu.
    *   *Hành vi:* `ON DELETE CASCADE` - Nếu một khóa học bị hủy bỏ hoặc xóa, toàn bộ danh sách ghi danh của học viên trong khóa học đó sẽ tự động được dọn dẹp sạch sẽ.
5.  **`attendance.student_id` → `users.user_id`:**
    *   *Ý nghĩa:* Đảm bảo dữ liệu điểm danh luôn gắn liền với một học viên cụ thể.
    *   *Hành vi:* `ON DELETE RESTRICT` - Bảo vệ lịch sử điểm danh, không cho phép xóa tài khoản học viên nếu tồn tại nhật ký điểm danh liên quan.
6.  **`attendance.course_id` → `courses.course_id`:**
    *   *Ý nghĩa:* Xác định chính xác buổi điểm danh thuộc về khóa học nào.
    *   *Hành vi:* `ON DELETE CASCADE` - Khi khóa học bị xóa, toàn bộ nhật ký điểm danh liên quan cũng sẽ bị xóa để giải phóng dung lượng lưu trữ.
7.  **`student_cards.student_id` → `users.user_id`:**
    *   *Ý nghĩa:* Thiết lập mối quan hệ sở hữu thẻ thành viên 1:1 với học viên. Mỗi học viên chỉ được sở hữu tối đa một thẻ thành viên hoạt động tại một thời điểm.
    *   *Hành vi:* `ON DELETE CASCADE` - Khi tài khoản học viên bị xóa hoàn toàn khỏi hệ thống, thẻ thành viên tương ứng của họ cũng sẽ tự động bị hủy bỏ.

---

### 🛡️ 1.3. Cơ Chế Khóa Tổng Hợp Đảm Bảo Tính Idempotency (Quét QR Điểm Danh)

Trong các hệ thống điểm danh thời gian thực qua thiết bị di động, rủi ro lớn nhất là việc học viên quét mã QR nhiều lần liên tục do kết nối mạng chập chờn hoặc thiết bị gửi trùng lặp yêu cầu (Double Submit). Để giải quyết triệt để vấn đề này ở mức cơ sở dữ liệu, hệ thống áp dụng ràng buộc khóa tổng hợp duy nhất (Composite Unique Key) trên bảng `attendance`:

```sql
ALTER TABLE attendance
ADD CONSTRAINT uq_attendance_idempotency UNIQUE (student_id, course_id, attendance_date);
```

#### Nguyên lý hoạt động và bảo vệ Idempotency:
*   **Khóa tổng hợp:** Sự kết hợp giữa ba trường `(student_id, course_id, attendance_date)` tạo thành một định danh duy nhất cho một trạng thái điểm danh của một học viên trong một ngày cụ thể của khóa học đó.
*   **Ngăn chặn trùng lặp dữ liệu:** Khi học viên quét mã QR lần đầu tiên trong ngày, hệ thống sẽ chèn thành công một bản ghi điểm danh mới với trạng thái `PRESENT` hoặc `LATE`. Nếu học viên tiếp tục quét mã QR lần thứ hai hoặc các yêu cầu trùng lặp được gửi đến do độ trễ mạng, hệ thống PostgreSQL sẽ lập tức chặn đứng hành động ghi bằng lỗi vi phạm ràng buộc duy nhất (`Unique Constraint Violation - SQLSTATE 23505`).
*   **Xử lý phía Backend:** Tầng dịch vụ (`attendance-service`) sẽ bắt ngoại lệ vi phạm ràng buộc này, không ném lỗi hệ thống (HTTP 500) mà xử lý mượt mà bằng cách trả về mã trạng thái HTTP 200 kèm theo cờ `duplicate: true` và thông điệp `"Điểm danh đã được ghi nhận trước đó trong ngày"`. Điều này đảm bảo tính Idempotency tuyệt đối cho API quét QR, giữ cho dữ liệu điểm danh luôn chính xác và không bị phình to bởi các bản ghi rác.

---

## 💻 2. TIÊU CHUẨN LẬP TRÌNH BACKEND DOANH NGHIỆP (QUARKUS & JAVA 21)

Để đảm bảo mã nguồn backend luôn sạch, dễ bảo trì, có hiệu năng cao và bảo mật tuyệt đối, toàn bộ đội ngũ kỹ sư phát triển bắt buộc phải tuân thủ các quy tắc lập trình nghiêm ngặt dưới đây.

### 📌 2.1. Quy Tắc Khai Báo Hằng Số Ở Cấp Lớp (Top-of-Class Constants Law)
Nghiêm cấm tuyệt đối việc viết mã cứng (hardcoding) các chuỗi ký tự, cờ lỗi, thời gian chờ (timeout), đường dẫn API hoặc các hệ số toán học trực tiếp bên trong thân của các hàm xử lý nghiệp vụ.
*   Tất cả các giá trị cấu hình hoặc giá trị tĩnh phải được trích xuất và khai báo tập trung ở phần đầu của lớp (trước các phương thức xử lý) dưới dạng các biến hằng số bất biến (`public static final` hoặc `private static final`).
*   Các khối logic bên dưới chỉ được phép tham chiếu đến các hằng số này thông qua tên biến đại diện.

*Ví dụ chuẩn mực:*
```java
package org.nlh4j.membershiphub.attendanceservice;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AttendanceService {

    // [0.2] KHAI BÁO HẰNG SỐ TẬP TRUNG Ở ĐẦU LỚP - TUÂN THỦ TUYỆT ĐỐI LUẬT CONSTANTS
    private static final Logger LOGGER = LoggerFactory.getLogger(AttendanceService.class);
    
    public static final String ERROR_DUPLICATE_ATTENDANCE = "ATTENDANCE_ALREADY_RECORDED";
    public static final String ERROR_STUDENT_NOT_ENROLLED = "STUDENT_NOT_ENROLLED_IN_COURSE";
    public static final int MAX_QR_SCAN_RETRY_LIMIT = 3;
    public static final String ATTENDANCE_STATUS_PRESENT = "PRESENT";

    // Các phương thức nghiệp vụ bên dưới chỉ tham chiếu đến hằng số đã khai báo
}
```

---

### 📝 2.2. Quy Tắc Ghi Chú Mã Nguồn Từng Dòng (Line-by-Line Commenting Law)
Mọi tệp tin mã nguồn Java, cấu hình XML (pom.xml), tệp di trú cơ sở dữ liệu SQL (Flyway) được tạo ra phải tích hợp đầy đủ các ghi chú giải thích chi tiết từng dòng hoặc từng khối logic.
*   Ghi chú phải làm rõ ngữ cảnh nghiệp vụ, kiến trúc hàm và các ràng buộc kỹ thuật liên quan.
*   **Bắt buộc tiêm mã định danh yêu cầu (Traceability Tag IDs):** Các kỹ sư phải chèn chính xác các thẻ theo dõi kỹ thuật (ví dụ: `// [REQ-013]`, `// [DAT-006]`) trực tiếp vào các dòng ghi chú để phục vụ công tác kiểm toán hệ thống tự động.

---

### 📊 2.3. Quy Tắc Ghi Nhật Ký Hệ Thống & Kiểm Toán Lỗi (Logging & Exception Auditing Law)
Hệ thống ghi nhật ký (Logging) là xương sống cho việc giám sát vận hành trên môi trường Cloud (GCP Cloud Logging, ELK). Do đó, cấu trúc ghi log phải tuân thủ các tiêu chuẩn sau:

1.  **Ghi log luồng nghiệp vụ:** Phải ghi log ở mức độ `INFO` hoặc `DEBUG` tại điểm bắt đầu (Entry point) và điểm kết thúc (Exit point) của mọi giao dịch nghiệp vụ hoặc luồng xử lý dữ liệu quan trọng. Log phải mang theo mã định danh theo dõi (Correlation ID hoặc Tenant ID/Student ID).
2.  **Che giấu dữ liệu nhạy cảm (Sensitive Data Masking):** Nghiêm cấm ghi nhận các thông tin nhạy cảm ở dạng văn bản rõ (cleartext) vào log bao gồm: Mật khẩu người dùng, mã PIN, khóa bí mật JWT, thông tin thẻ tín dụng, mã token truy cập. Phải sử dụng các hàm băm hoặc bộ lọc để che giấu dữ liệu trước khi ghi log.
3.  **Xử lý ngoại lệ nghiêm ngặt (Comprehensive Exception Logging):**
    *   Không để trống khối `catch` hoặc ghi log chung chung.
    *   Khi bắt được ngoại lệ, bắt buộc phải ghi log ở mức `ERROR` chứa đủ 3 thành phần: **Tên phân hệ bị lỗi, Thông điệp lỗi chi tiết từ hệ thống (Raw Exception Message), và Mã thẻ theo dõi yêu cầu (Tag ID)**.
    *   **Bảo toàn chuỗi nguyên nhân ngoại lệ (Exception Cause Chain Preservation):** Khi ném ra một ngoại lệ nghiệp vụ tùy chỉnh (Custom Business Exception) để trả về cho Client, bắt buộc phải truyền ngoại lệ gốc (`e` hoặc `throwable_cause`) vào hàm khởi tạo của ngoại lệ mới để không làm đứt gãy vết vết ngăn xếp (stack trace) phục vụ debug.

*Ví dụ chuẩn mực:*
```java
package org.nlh4j.membershiphub.attendanceservice;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;

@ApplicationScoped
public class AttendanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttendanceService.class);
    private static final String SUB_SYSTEM_NAME = "ATTENDANCE-SERVICE";

    @Inject
    AttendanceRepository attendanceRepository;

    @Transactional
    public AttendanceResponse recordAttendance(AttendanceRequest request) {
        // [REQ-012] Log điểm vào của giao dịch điểm danh
        LOGGER.info("[PROCESS] Bắt đầu xử lý điểm danh cho học viên: {}, Khóa học: {}", 
                    request.getStudentId(), request.getCourseId());

        try {
            Attendance attendance = new Attendance();
            attendance.studentId = request.getStudentId();
            attendance.courseId = request.getCourseId();
            attendance.attendanceDate = request.getAttendanceDate();
            
            attendanceRepository.persist(attendance);
            
            LOGGER.info("[SUCCESS] Ghi nhận điểm danh thành công cho học viên: {}", request.getStudentId());
            return new AttendanceResponse(attendance.attendanceId, false, "Điểm danh thành công");
            
        } catch (Exception e) {
            // [EXC-002] Bắt ngoại lệ vi phạm ràng buộc duy nhất để xử lý Idempotency
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                LOGGER.warn("[IDEMPOTENCY] Phát hiện yêu cầu điểm danh trùng lặp cho học viên: {} trong ngày: {}", 
                            request.getStudentId(), request.getAttendanceDate());
                return new AttendanceResponse(null, true, "Điểm danh đã được ghi nhận trước đó trong ngày");
            }
            
            // [0.3] Ghi log lỗi nghiêm ngặt chứa: Tên phân hệ, Thông điệp lỗi gốc, và Tag ID kiểm toán
            LOGGER.error("[CRITICAL FAIL] [{}] Xử lý điểm danh thất bại do lỗi hệ thống. Lỗi gốc: {}. Tag ID: [REQ-013]", 
                         SUB_SYSTEM_NAME, e.getMessage(), e);
            
            // Bảo toàn chuỗi nguyên nhân ngoại lệ khi ném ngoại lệ tùy chỉnh ra ngoài
            throw new AttendanceProcessingException("Lỗi hệ thống khi xử lý điểm danh học viên", e);
        }
    }
}
```

---

## 📊 3. MA TRẬN THEO DÕI YÊU CẦU TOÀN CẦU (TRACEABILITY MATRIX)

Ma trận này thiết lập mối liên kết trực tiếp, minh bạch giữa các yêu cầu nghiệp vụ, thiết kế cơ sở dữ liệu vật lý và các tiêu chuẩn lập trình backend tương ứng, đảm bảo tính bao phủ 100% không có lỗ hổng thiết kế.

| Mã Tag ID (Tag ID) | Phân hệ kỹ thuật (Technical Module) | Thành phần cơ sở dữ liệu (Database Component) | Tiêu chuẩn lập trình & Ràng buộc (Coding & Constraint Standards) | Trạng thái tuân thủ (Compliance Status) |
| :--- | :--- | :--- | :--- | :--- |
| `[DAT-004]` | `course-service` | Bảng `courses` | Thiết lập khóa ngoại `teacher_id` tham chiếu `users.user_id` và `center_id` tham chiếu `centers.center_id`. Ràng buộc kiểm tra cứng `end_date >= start_date` ở mức DB. | **Đã tuân thủ (100%)** |
| `[DAT-005]` | `attendance-service` | Bảng `enrollments` | Thiết lập khóa ngoại kép tham chiếu đến `users` và `courses`. Áp dụng ràng buộc duy nhất `UNIQUE (student_id, course_id)` để ngăn chặn học viên đăng ký trùng lớp. | **Đã tuân thủ (100%)** |
| `[DAT-006]` | `attendance-service` | Bảng `attendance` | Thiết lập khóa ngoại tham chiếu đến học viên và khóa học. Áp dụng ràng buộc khóa tổng hợp `UNIQUE (student_id, course_id, attendance_date)` để bảo vệ tính Idempotency. | **Đã tuân thủ (100%)** |
| `[DAT-007]` | `attendance-service` | Bảng `student_cards` | Thiết lập quan hệ 1:1 bằng ràng buộc `UNIQUE (student_id)` tham chiếu đến bảng `users`. Ràng buộc kiểm tra `validity_days > 0` và `remaining_days >= 0`. | **Đã tuân thủ (100%)** |
| `[REQ-012]` | `attendance-service` | API Quét QR Điểm Danh | Giải mã payload QR dạng Base64, kiểm tra tính hợp lệ của định dạng dữ liệu trước khi thực hiện truy vấn cơ sở dữ liệu. | **Đã tuân thủ (100%)** |
| `[REQ-013]` | `attendance-service` | Cơ chế Idempotency | Bắt lỗi vi phạm ràng buộc duy nhất `SQLSTATE 23505` từ PostgreSQL, chuyển đổi thành phản hồi HTTP 200 với cờ `duplicate: true`. | **Đã tuân thủ (100%)** |
| `[ARC-007]` | API Gateway / Attendance | Luồng Điểm Danh QR | Định tuyến đồng bộ yêu cầu quét QR từ Mobile App qua API Gateway đến `attendance-service`, áp dụng bộ lọc xác thực JWT Bearer Token. | **Đã tuân thủ (100%)** |
| `[NFR-003]` | Toàn hệ thống | Bảo mật dữ liệu | Áp dụng mã hóa đường truyền TLS 1.3, mã hóa dữ liệu lưu trữ AES-256 cho PostgreSQL, ngăn chặn SQL Injection bằng Prepared Statements. | **Đã tuân thủ (100%)** |
| `[NFR-009]` | Hạ tầng cơ sở dữ liệu | Sao lưu & Phục hồi | Cấu hình Cloud SQL PostgreSQL tự động sao lưu hàng ngày, hỗ trợ khôi phục point-in-time trong vòng 24 giờ để đảm bảo an toàn dữ liệu. | **Đã tuân thủ (100%)** |
```