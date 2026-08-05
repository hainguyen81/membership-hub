# Giai đoạn 2: <!--PHASE_NAME_START-->Xây dựng CRUD trung tâm, khóa học, ghi danh và điểm danh<!--PHASE_NAME_END-->

## 📊 Document Control

| Mục | Chi tiết |
| :--- | :--- |
| **ID Kiến trúc** | ARCH-20260804165526 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 2 |
| **Tên Giai đoạn** | <!--PHASE_NAME_START-->Xây dựng CRUD trung tâm, khóa học, ghi danh và điểm danh<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn này tập trung vào việc triển khai các dịch vụ CRUD cho trung tâm, khóa học, ghi danh và điểm danh, đồng thời xây dựng schema dữ liệu tương ứng, thực hiện kiểm thử, xử lý ngoại lệ và đảm bảo tuân thủ các tiêu chuẩn bảo mật OWASP.<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Baseline) |
| **Ngày/Giờ** | 2026/08/04 16:55:26 |
| **Tác giả** | Enterprise System Architect (SA Agent) |
| **Phê duyệt** | Pending Technical Governance Review |

## 1. Phạm vi và mục tiêu của Giai đoạn
Giai đoạn 2 thực hiện xây dựng các dịch vụ CRUD cho trung tâm, khóa học, ghi danh và điểm danh, bao gồm thiết kế schema dữ liệu, triển khai API REST, thực hiện kiểm thử đơn vị và tích hợp, xử lý ngoại lệ, và đảm bảo tuân thủ các tiêu chuẩn bảo mật OWASP.

## 2. Phạm vi kỹ thuật và ranh giới thư mục
- **Thư mục**  
  - `./sources/backend/center-service`  
  - `./sources/backend/course-service`  
  - `./sources/backend/enrollment-service`  
  - `./sources/backend/attendance-service`  
  - `./sources/backend/database/migrations`  
- **Endpoint REST**  
  - `GET /api/centers` → danh sách trung tâm  
  - `POST /api/centers` → tạo trung tâm  
  - `GET /api/courses` → danh sách khóa học  
  - `POST /api/courses` → tạo khóa học  
  - `POST /api/enrollments` → ghi danh học viên  
  - `POST /api/attendance` → ghi điểm danh

## 3. Hướng dẫn chức năng dành cho Sub-Agent
- **Coder**: Xây dựng controller, service, repository, validation, exception handling, unit test cho các dịch vụ.  
- **Tester**: Viết và thực thi test integration, kiểm tra idempotency, duplicate handling.  
- **Reviewer**: Đánh giá code quality, kiểm tra exception handling, performance.  
- **Doc**: Tài liệu chi tiết API, quy trình triển khai, hướng dẫn bảo mật.  
- **Docker / GCP / GKE**: Không áp dụng trong giai đoạn này.

## 4. Định nghĩa Hoàn thành Giai đoạn (DoD)
- Tất cả yêu cầu [REQ-004]–[REQ-013] và các dữ liệu [DAT-003]–[DAT-006] được triển khai và kiểm thử.  
- Coverage test ≥ 90 % cho các module center, course, enrollment, attendance.  
- Kiểm tra OWASP (SQLi, XSS, CSRF, JWT) đạt 100 %.  
- Mọi tag ID được map đầy đủ, không còn tag chưa được sử dụng.

## 5. Nhật ký thực thi kiến trúc theo ngày

### DAY 1: <!--DAY_HEADER_START-->XÂY DỰNG CRUD TRUNG TÂM<!--DAY_HEADER_END-->

#### SUB-TASK 1.1: Xây dựng API CRUD trung tâm, schema Centers, kiểm thử đơn vị
##### Được giao cho Sub-Agent: Coder
##### Yêu cầu thành phần & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu**: `./sources/backend/center-service`
* **Thẻ mã theo dõi**: <!--START_TAGS-->[REQ-004], [REQ-005], [REQ-006], [DAT-003]<!--END_TAGS-->

### DAY 2: <!--DAY_HEADER_START-->XÂY DỰNG CRUD KHÓA HỌC<!--DAY_HEADER_END-->

#### SUB-TASK 2.1: Xây dựng API CRUD khóa học, schema Courses, kiểm thử đơn vị
##### Được giao cho Sub-Agent: Coder
##### Yêu cầu thành phần & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu**: `./sources/backend/course-service`
* **Thẻ mã theo dõi**: <!--START_TAGS-->[REQ-007], [REQ-008], [REQ-009], [DAT-004]<!--END_TAGS-->

### DAY 3: <!--DAY_HEADER_START-->XÂY DỰNG GHI DANH VÀ ĐIỂM DANH<!--DAY_HEADER_END-->

#### SUB-TASK 3.1: Xây dựng API ghi danh, attendance, schema Enrollments & Attendance, exception handling, kiểm thử đơn vị
##### Được giao cho Sub-Agent: Coder
##### Yêu cầu thành phần & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu**: `./sources/backend/enrollment-service`
* **Thẻ mã theo dõi**: <!--START_TAGS-->[REQ-010], [REQ-011], [DAT-005]<!--END_TAGS-->

#### SUB-TASK 3.2: Viết test integration cho attendance, kiểm tra idempotency, duplicate handling
##### Được giao cho Sub-Agent: Tester
##### Yêu cầu thành phần & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu**: `./sources/backend/attendance-service;./sources/backend/attendance-service/src/test/java/com/membershiphub/attendance/AttendanceServiceTest.java`
* **Thẻ mã theo dõi**: <!--START_TAGS-->[REQ-012], [REQ-013], [DAT-006], [EXC-001], [EXC-002]<!--END_TAGS-->

#### SUB-TASK 3.3: Đánh giá code quality, exception handling, performance
##### Được giao cho Sub-Agent: Reviewer
##### Yêu cầu thành phần & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu**: `./sources/backend/enrollment-service`
* **Thẻ mã theo dõi**: <!--START_TAGS-->[REQ-010], [REQ-011], [DAT-005]<!--END_TAGS-->