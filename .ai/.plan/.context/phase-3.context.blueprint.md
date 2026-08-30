# Giai đoạn 3: <!--PHASE_NAME_START-->Phát Triển Nghiệp Vụ Khoá Học Và Điểm Danh QR<!--PHASE_NAME_END-->

## 📊 Kiểm Soát Tài Liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Bản Thiết Kế** | ARCH-20260829225017 |
| **Tên Dự Án** | membership-hub |
| **Giai đoạn** | 3 |
| **Tên Giai Đoạn** | <!--PHASE_NAME_START-->Phát Triển Nghiệp Vụ Khoá Học Và Điểm Danh QR<!--PHASE_NAME_END--> |
| **Mô Tả** | <!--PHASE_DESC_START-->Giai đoạn 3 tập trung xây dựng toàn bộ luồng nghiệp vụ liên quan đến quản lý khoá học, phân công giáo viên, đăng ký khoá học của sinh viên và đặc biệt là luồng xử lý điểm danh QR với cơ chế idempotency, retry khi mất mạng và FIFO khi khôi phục dịch vụ. Giai đoạn này triển khai các REST API danh sách khoá học có phân trang, CRUD khoá học với logic overlap check dựa trên teacher_id và khoảng ngày sử dụng ràng buộc exclusion tại cơ sở dữ liệu PostgreSQL, endpoint gán/huỷ gán giáo viên kèm đẩy sự kiện Kafka, endpoint duyệt khoá học cho sinh viên với filter loại trừ enrollment tồn tại, endpoint đăng ký khoá học tự sinh tài khoản Student và đẩy Kafka enrollment-created, REST POST điểm danh QR với giải mã payload base64, kiểm tra enrollment, idempotency key, cơ chế retry sau mất mạng và FIFO khi khôi phục<!--PHASE_DESC_END--> |
| **Phiên Bản** | 1.0 (Baseline) |
| **Ngày Giờ** | 2026/08/29 22:50:17 |
| **Tác Giả** | Enterprise System Architect (SA Agent) |
| **Phê Duyệt** | Pending Technical Governance Review |

## 1. Phạm Vi Hoạt Động Và Mục Tiêu Giai Đoạn

Giai đoạn 3 đóng vai trò trụ cột nghiệp vụ thứ ba trong hệ thống membership-hub, tập trung vào việc hiện thực hóa toàn bộ luồng quản lý khoá học, phân công giáo viên, đăng ký khoá học và đặc biệt là luồng xử lý điểm danh QR với cơ chế idempotency, retry khi mất mạng và FIFO khi khôi phục dịch vụ. Phạm vi kỹ thuật cốt lõi của giai đoạn này bao gồm 7 nhiệm vụ backlog chính được phân bổ: Nhiệm vụ 9 (CRUD khoá học và kiểm tra xung đột lịch), Nhiệm vụ 10 (Gán/huỷ gán giáo viên cho khoá học), Nhiệm vụ 11 (Duyệt khoá học cho sinh viên), Nhiệm vụ 12 (Đăng ký khoá học cho sinh viên), Nhiệm vụ 13 (Ghi nhận điểm danh QR cho sinh viên) và Nhiệm vụ 28 (Luồng xử lý điểm danh QR đầu cuối). Theo kế hoạch phân bổ trong bảng tổng hợp đa giai đoạn, giai đoạn 3 được phân bổ chính xác 7 ngày làm việc, phù hợp với biên tính toán Relative_Z = 7.

Mục tiêu cốt lõi của giai đoạn là xây dựng các luồng nghiệp vụ trên hai microservices chính: `course-service` và `attendance-service`. Cụ thể, trên `course-service` sẽ hiện thực hóa REST API GET `/api/v1/courses` trả về danh sách phân trang gồm CourseID, Title, StartDate, EndDate, TeacherName với hỗ trợ sắp xếp và lọc. CRUD operations POST/PUT/DELETE `/api/v1/courses` chỉ dành cho SystemAdmin hoặc CenterAdmin với validation đầy đủ các trường title (max 150 ký tự), description (TEXT), start_date và end_date (ràng buộc end_date >= start_date), teacher_id (UUID, FK users), max_students (INT, default 30), center_id (UUID, FK centers). Logic overlap check được thực thi ở hai lớp: service layer sử dụng truy vấn JPQL kiểm tra xung đột lịch dựa trên teacher_id và khoảng ngày, database layer sử dụng ràng buộc EXCLUDE của PostgreSQL extension btree_gist để đảm bảo tính toàn vẹn dữ liệu ở mức atomic. Khi phát hiện xung đột, hệ thống ném `ScheduleConflictException` với mã lỗi `SCHEDULE_CONFLICT_409` tương ứng HTTP 409 Conflict.

Endpoint POST/DELETE `/api/v1/courses/{id}/teachers` cho phép SystemAdmin gán hoặc huỷ gán giáo viên với cập nhật bảng ánh xạ `course_teacher_mapping` có ràng buộc UNIQUE composite `(course_id, teacher_id)` và đẩy sự kiện Kafka lên topic `teacher-events` để notification-service tiêu thụ. Endpoint GET `/api/v1/students/courses/available` trả về danh sách khoá học khả dụng cho sinh viên hiện tại, loại trừ các khoá học đã đăng ký thông qua subquery `NOT EXISTS` kết hợp với index trên `(student_id, course_id)`. Endpoint POST `/api/v1/enrollments` xử lý đăng ký khoá học với logic tự sinh tài khoản Student nếu chưa tồn tại, kiểm tra capacity, tạo enrollment record, đẩy sự kiện Kafka `enrollment-events` lên topic tương ứng với payload chuẩn JSON.

Trên `attendance-service`, giai đoạn này triển khai REST API POST `/api/v1/attendance/scan` với cơ chế xử lý điểm danh QR đầu cuối: giải mã payload base64 chứa studentID và courseID thông qua `QrPayloadDecoder`, kiểm tra enrollment tồn tại thông qua truy vấn JOIN, kiểm tra idempotency thông qua composite unique key `(student_id, course_id, attendance_date)`, tạo attendance record và publish sự kiện Kafka `attendance-events`. Hệ thống cũng tích hợp cơ chế retry queue xử lý `[EXC-001]` khi mất mạng, FIFO recovery xử lý `[EXC-005]` khi dịch vụ khôi phục, và cơ chế phát hiện trùng lặp xử lý `[EXC-002]` trả về response thành công với cờ `duplicate: true` thay vì tạo bản ghi mới. Mọi ngoại lệ nghiệp vụ được ánh xạ thông qua cơ chế `ExceptionMapper` tập trung với mã lỗi chuẩn hoá như `ENROLLMENT_REQUIRED_403`, `DUPLICATE_ATTENDANCE_200`, `INVALID_QR_400`.

Mục tiêu chính là toàn bộ REST API và Kafka producer/consumer có thể triển khai tức thì lên môi trường development ngay khi giai đoạn kết thúc, sẵn sàng cho giai đoạn 4 phát triển các luồng thẻ thành viên, thông báo đa kênh và AI chatbot. Tất cả thao tác nghiệp vụ phải ghi log kiểm toán thông qua cơ chế tập trung, đảm bảo dấu vết kiểm toán đầy đủ phục vụ tuân thủ [NFR-006] với thời gian lưu trữ tối thiểu 1 năm.

## 2. Phạm Vi Kỹ Thuật Cho Phép Và Ranh Giới Thư Mục

Danh sách đầy đủ các tệp tin vật lý được phép tạo mới trong giai đoạn 3, tuân thủ nghiêm ngặt quy ước gói `org.nlh4j.membershiphub` và ranh giới thư mục doanh nghiệp:

* `./sources/backend/course-service/pom.xml` — [ARC-000]
* `./sources/backend/course-service/src/main/resources/application.properties` — [ARC-000]
* `./sources/backend/course-service/src/main/resources/db/migration/V1__courses_init.sql` — [DAT-001], [DAT-004]
* `./sources/backend/course-service/src/main/resources/db/migration/V2__course_schedule_exclusion.sql` — [DAT-001]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseServiceApplication.java` — [ARC-000]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java` — [REQ-007], [REQ-008]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseTeacherController.java` — [REQ-009]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/StudentCourseBrowseController.java` — [REQ-010]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/EnrollmentController.java` — [REQ-011]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseService.java` — [REQ-007], [REQ-008], [EXC-001]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseTeacherService.java` — [REQ-009]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseBrowseService.java` — [REQ-010]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/EnrollmentService.java` — [REQ-011]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/repository/CourseRepository.java` — [REQ-007], [REQ-010]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/repository/EnrollmentRepository.java` — [REQ-011]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/CourseResponse.java` — [REQ-007]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/CourseCreateRequest.java` — [REQ-008]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/EnrollmentRequest.java` — [REQ-011]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/TeacherAssignRequest.java` — [REQ-009]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/ScheduleConflictException.java` — [EXC-001]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/EnrollmentNotFoundException.java` — [EXC-001]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/messaging/KafkaTeacherProducer.java` — [REQ-009]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/messaging/KafkaEnrollmentProducer.java` — [REQ-011]
* `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseServiceIntegrationTestSuite.java` — [REQ-008], [REQ-011]
* `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/controller/CourseControllerTest.java` — [REQ-007], [REQ-008]
* `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/service/CourseServiceTest.java` — [REQ-008], [REQ-009], [REQ-010]
* `./sources/backend/attendance-service/pom.xml` — [ARC-000]
* `./sources/backend/attendance-service/src/main/resources/application.properties` — [ARC-000]
* `./sources/backend/attendance-service/src/main/resources/db/migration/V1__attendance_init.sql` — [DAT-006]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceApplication.java` — [ARC-000]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/controller/AttendanceController.java` — [REQ-012], [REQ-013], [ARC-007]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/AttendanceService.java` — [REQ-012], [REQ-013], [ARC-007], [EXC-001], [EXC-002], [EXC-005]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/QrPayloadDecoder.java` — [REQ-012]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/repository/AttendanceRepository.java` — [REQ-012], [REQ-013]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/QrScanRequest.java` — [REQ-012]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/AttendanceResponse.java` — [REQ-012]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/exception/DuplicateAttendanceException.java` — [EXC-002]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/exception/EnrollmentRequiredException.java` — [EXC-001]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/messaging/KafkaAttendanceProducer.java` — [ARC-007]
* `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceIntegrationTestSuite.java` — [REQ-012], [REQ-013], [EXC-001], [EXC-002], [EXC-005]
* `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/service/AttendanceServiceTest.java` — [REQ-012], [REQ-013]
* `./sources/docs/architecture/course-architecture.md` — [REQ-007], [REQ-008], [REQ-009], [REQ-010], [REQ-011], [ARC-007], [DOC-001]
* `./sources/docs/architecture/attendance-architecture.md` — [REQ-012], [REQ-013], [ARC-007], [EXC-001], [EXC-002], [EXC-005], [DOC-001]
* `./sources/docs/api/course-openapi.yaml` — [REQ-007], [REQ-008], [REQ-009], [REQ-010], [REQ-011], [DOC-001]
* `./sources/docs/api/attendance-openapi.yaml` — [REQ-012], [REQ-013], [EXC-001], [EXC-005], [DOC-001]

* **RÀNG BUỘC BẮT BUỘC VỀ BỘ KHUNG NỀN TẢNG**:
  - Tất cả tài sản mã nguồn ứng dụng trong giai đoạn 3 phải kế thừa bộ khung build descriptors đã được khởi tạo ở Giai đoạn 1 (`./sources/backend/pom.xml`, `./sources/backend/<service-name>/pom.xml`).
  - Tệp `./sources/backend/pom.xml` và 4 tệp con KHÔNG được tái tạo trong giai đoạn này vì đã tồn tại từ Giai đoạn 1, ngoại trừ `./sources/backend/course-service/pom.xml` và `./sources/backend/attendance-service/pom.xml` đã được khởi tạo scaffold ở giai đoạn 1.
  - Toàn bộ mã nguồn mới phải tuân thủ quy ước gói `org.nlh4j.membershiphub.<service-name>` và được truy vết bằng các mã thẻ quy định.

## 3. Chỉ Thị Chức Năng Cho Từng Sub-Agent

* **Coder**: Đóng vai trò lập trình viên ứng dụng chính. Chịu trách nhiệm hiện thực hóa toàn bộ controller, service, DTO, exception handler, repository và Kafka producer trong package `controller`, `service`, `dto`, `repository`, `exception` và `messaging` của `course-service` và `attendance-service`. Bị cấm viết bộ kiểm thử, tài liệu hoặc cấu hình hạ tầng.

* **Tester**: Đóng vai trò kiểm thử viên chính. Tạo bộ kiểm thử đơn vị JUnit 5 kết hợp Mockito cho CourseController, CourseService, AttendanceController, AttendanceService. Xây dựng bộ kiểm thử tích hợp sử dụng Testcontainers (PostgreSQL, Kafka) cho cả hai service. Bị cấm sửa đổi mã nguồn sản phẩm.

* **Doc**: Soạn thảo 4 tài liệu Markdown quan trọng trong thư mục `./sources/docs/architecture/` và `./sources/docs/api/`: tài liệu kiến trúc course-service, tài liệu kiến trúc attendance-service, OpenAPI YAML cho course-openapi và OpenAPI YAML cho attendance-openapi. Tất cả tệp tài liệu phải kết thúc bằng phần mở rộng `.md` hoặc `.yaml`.

* **Reviewer**: Thực hiện rà soát chất lượng mã nguồn theo checklist OWASP Top 10, đánh giá tính đúng đắn của logic overlap check, idempotency, retry queue và FIFO recovery, xác minh tính bảo mật của việc sử dụng JPQL parameter binding và Kafka payload validation, phát hiện sớm các vấn đề race condition, memory leak và performance bottleneck.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn (DoD)

Giai đoạn 3 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: (1) Endpoint GET `/api/v1/courses` trả về danh sách phân trang đầy đủ CourseID, Title, StartDate, EndDate, TeacherName; (2) CRUD khoá học với logic overlap check ngăn chặn trùng lặp lịch giáo viên, kích hoạt ràng buộc EXCLUDE tại database; (3) Endpoint gán/huỷ gán giáo viên cập nhật bảng ánh xạ và đẩy sự kiện Kafka `teacher-events`; (4) Endpoint duyệt khoá học cho sinh viên loại trừ các khoá học đã đăng ký; (5) Endpoint đăng ký khoá học tự sinh tài khoản Student, kiểm tra capacity, đẩy sự kiện Kafka `enrollment-events`; (6) Endpoint điểm danh QR giải mã payload base64, kiểm tra enrollment, idempotency qua composite unique key, xử lý trùng lặp với cờ `duplicate`; (7) 100% thẻ truy vết `[REQ-007]`, `[REQ-008]`, `[REQ-009]`, `[REQ-010]`, `[REQ-011]`, `[REQ-012]`, `[REQ-013]`, `[ARC-007]`, `[EXC-001]`, `[EXC-002]`, `[EXC-005]` được ánh xạ đầy đủ vào mã nguồn và tài liệu; (8) 100% bộ kiểm thử JUnit đạt trạng thái PASS với code coverage >= 80% cho các lớp controller và service.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->KHỞI TẠO COURSE-SERVICE VÀ MODULE KHOÁ HỌC CƠ BẢN<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 1.1: Khởi tạo mô tả build và ứng dụng cho course-service

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `pom.xml` cho `course-service` tại đường dẫn `./sources/backend/course-service/pom.xml` thừa kế parent `./sources/backend/pom.xml` với groupId `org.nlh4j.membershiphub` và artifactId `course-service`. Khai báo các dependency Quarkus 3.15.1: `quarkus-resteasy-reactive-jackson` cho REST controller, `quarkus-hibernate-orm-panache` cho ORM, `quarkus-jdbc-postgresql` cho driver database, `quarkus-flyway` cho migration, `quarkus-smallrye-reactive-messaging-kafka` cho Kafka producer/consumer, `quarkus-hibernate-validator` cho Bean Validation, `quarkus-smallrye-openapi` cho OpenAPI. Dependencies test gồm `quarkus-junit5`, `rest-assured`, `mockito-core`, `org.testcontainers:postgresql:1.20.4`, `org.testcontainers:kafka:1.20.4`. Cấu hình plugin `quarkus-maven-plugin` 3.15.1 để build native image, `maven-surefire-plugin` 3.2.5 cho test runner. Tệp này phải biên dịch trống (blank compile) ngay từ đầu thông qua lệnh `mvn clean install -DskipTests` tại thư mục root.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Migration V1__courses_init.sql [DAT-001], [DAT-004]
-- Khởi tạo bảng courses với các ràng buộc FK và index tối ưu
CREATE TABLE courses (
    course_id UUID PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    teacher_id UUID,
    max_students INT NOT NULL DEFAULT 30,
    center_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id),
    CONSTRAINT fk_courses_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT chk_courses_date_range CHECK (end_date >= start_date),
    CONSTRAINT chk_courses_max_students CHECK (max_students > 0)
);

CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_courses_start_date ON courses(start_date);
CREATE INDEX idx_courses_center_id ON courses(center_id);
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 1.2: Kiểm thử tích hợp mô tả build course-service

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseServiceIntegrationTestSuite.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [REQ-007]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử tích hợp `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseServiceIntegrationTestSuite.java` sử dụng JUnit 5 Platform Launcher kết nối script shell `./sources/infra/test/maven-build-integration.sh` để xác minh `./sources/backend/course-service/pom.xml` biên dịch sạch. Test phải fail nếu dependency chưa khả dụng hoặc parent pom không hợp lệ. Sử dụng `ProcessBuilder` để thực thi lệnh Maven `mvn clean install -DskipTests`, kiểm tra `exit code` bằng 0, xác nhận file `target/quarkus-app/quarkus-run.jar` được tạo ra. Test phải verify tệp pom.xml chứa `<groupId>org.nlh4j.membershiphub</groupId>` và `<artifactId>course-service</artifactId>` đúng theo quy ước gói doanh nghiệp.

#### 📝 NHIỆM VỤ PHỤ 1.3: Tài liệu kiến trúc tổng quan course-service

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/architecture/course-architecture.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-007], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tài liệu Markdown tại `./sources/docs/architecture/course-architecture.md` mô tả kiến trúc `course-service` gồm sơ đồ C4 Container với các thành phần REST Controller, Service, Repository, Kafka Producer. Tài liệu phải liệt kê đầy đủ Tag ID `[REQ-007]`, `[REQ-008]`, `[REQ-009]`, `[REQ-010]`, `[REQ-011]`, `[ARC-007]` ánh xạ đến các endpoint tương ứng. Bao gồm sơ đồ Mermaid `flowchart` thể hiện luồng xử lý nghiệp vụ CRUD khoá học từ REST request đến database persistence và tích hợp Kafka cho các sự kiện `teacher-assigned` và `enrollment-created`. Ghi chú tuân thủ nguyên tắc idempotency, transactional outbox pattern cho Kafka.

#### 📝 NHIỆM VỤ PHỤ 1.4: Review mã nguồn khởi tạo pom và ứng dụng

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseServiceApplication.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer thực hiện đánh giá tệp `CourseServiceApplication.java` chứa annotation `@QuarkusMain` tại đường dẫn `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseServiceApplication.java`, đảm bảo cấu hình gói `org.nlh4j.membershiphub.courseservice` chính xác, không tham chiếu `com.example`. Xác nhận hàm `main` chuẩn Quarkus với `Quarkus.run(args)`, phát hiện sớm các vấn đề cấu hình và đề xuất bổ sung `quarkus-banner.txt` cho môi trường production. Tạo báo cáo review ngắn gọn với format: Phát hiện, Mức độ nghiêm trọng, Đề xuất.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->HIỆN THỰC HOÁ REST CONTROLLER VÀ SERVICE CHO KHOÁ HỌC VỚI LOGIC OVERLAP CHECK<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 2.1: Triển khai Course Controller và DTO

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-007], [REQ-008]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java` hiện thực hóa lớp `CourseController` với annotation `@Path("/api/v1/courses")`. Triển khai các endpoint: (1) `GET /api/v1/courses` với `@QueryParam` cho `page`, `size`, `sort` mặc định `size=20`, `sort=startDate,asc`, trả về danh sách phân trang gồm `courseId`, `title`, `startDate`, `endDate`, `teacherName` thông qua `Page<CourseResponse>`; (2) `POST /api/v1/courses` nhận `CourseCreateRequest` với `@Valid` và Bean Validation (`@NotNull title`, `@Size(max=150) title`, `@NotNull startDate`, `@NotNull endDate`, `@NotNull teacherId`, `@NotNull centerId`), gọi `CourseService.create()`; (3) `PUT /api/v1/courses/{id}` cập nhật; (4) `DELETE /api/v1/courses/{id}` xoá mềm. Áp dụng `@RolesAllowed({"SystemAdmin","CenterAdmin"})` cho POST/PUT/DELETE. Sử dụng JPQL parameter binding trong `CourseService` để tránh SQL injection theo OWASP A03.

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```json
{
  "endpoint": "GET /api/v1/courses?page=0&size=20&sort=startDate,asc",
  "headers": { "Authorization": "Bearer <jwt_token>" },
  "response_200": {
    "content": [
      {
        "courseId": "uuid",
        "title": "Lập trình Java cơ bản",
        "description": "Khoá học nhập môn Java",
        "startDate": "2026-09-01",
        "endDate": "2026-12-15",
        "teacherName": "Nguyen Van A",
        "maxStudents": 30,
        "centerId": "uuid"
      }
    ],
    "totalElements": 10,
    "totalPages": 1,
    "page": 0,
    "size": 20
  }
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 2.2: Kiểm thử đơn vị cho Course Controller

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/controller/CourseControllerTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-007], [REQ-008]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử đơn vị `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/controller/CourseControllerTest.java` sử dụng JUnit 5 kết hợp Mockito 5.7.0 và RestAssured 5.4.0. Mock `CourseService`. Tạo 6 test case: (1) `listCourses_byAuthenticatedUser_returns200WithPagination` xác minh GET trả về HTTP 200 với mảng `content` chứa các khoá học; (2) `createCourse_bySystemAdmin_returns201` xác minh SystemAdmin tạo mới thành công trả về HTTP 201; (3) `createCourse_withMissingTitle_returns400` xác minh thiếu trường `title` trả về HTTP 400 với mảng errors; (4) `createCourse_withEndDateBeforeStartDate_returns400` xác minh `endDate < startDate` trả về HTTP 400; (5) `createCourse_byTeacher_returns403` xác minh Teacher cố tạo khoá học bị từ chối với mã `INSUFFICIENT_PRIVILEGES`; (6) `createCourse_withScheduleConflict_returns409` xác minh trùng lịch giáo viên ném `ScheduleConflictException` trả về HTTP 409 với mã `SCHEDULE_CONFLICT_409`.

#### 📝 NHIỆM VỤ PHỤ 2.3: Tài liệu API cho Course Service

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/api/course-openapi.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-007], [REQ-008], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tệp OpenAPI 3.1 YAML tại `./sources/docs/api/course-openapi.yaml` mô tả endpoint `/api/v1/courses` bao gồm schema `CourseCreateRequest` với các trường `title`, `description`, `startDate`, `endDate`, `teacherId`, `centerId`, `maxStudents`, schema `CourseResponse` với các trường phản hồi, mã lỗi 400 (validation failed), 403 (insufficient privileges), 404 (course not found), 409 (schedule conflict). Tích hợp bearer token security scheme. Tham chiếu Tag ID `[REQ-007]`, `[REQ-008]`. Bổ sung ví dụ curl command cho mỗi endpoint.

#### 📝 NHIỆM VỤ PHỤ 2.4: Review logic overlap check và exclusion constraint

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-008], [EXC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer đánh giá logic `CourseService.create` và `CourseService.update` tại `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseService.java` đảm bảo có kiểm tra xung đột lịch giáo viên trước khi persist. Xác nhận sử dụng `PanacheRepository` với JPQL parameter binding, ném `ScheduleConflictException` khi phát hiện trùng lặp dựa trên `teacher_id` và khoảng ngày `daterange(start_date, end_date, '[]')`. Kiểm tra việc áp dụng ràng buộc `EXCLUDE USING gist` trong database migration V2. Đề xuất cải tiến nếu thiếu transaction boundary hoặc không sử dụng `@Transactional` đúng cách.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Migration V2__course_schedule_exclusion.sql [DAT-001]
-- Áp dụng ràng buộc exclusion chống xung đột lịch giáo viên
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE courses
    ADD CONSTRAINT ex_teacher_schedule_no_overlap
    EXCLUDE USING gist (
        teacher_id WITH =,
        daterange(start_date, end_date, '[]') WITH &&
    )
    WHERE (teacher_id IS NOT NULL);
```
<!--END_DDL_MIGRATION-->

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->GÁN GIÁO VIÊN VÀ PHÁT SINH SỰ KIỆN KAFKA TEACHER-ASSIGNED<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 3.1: Triển khai Controller gán giáo viên

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseTeacherController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-009], [ARC-007]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseTeacherController.java` hiện thực hóa lớp `CourseTeacherController` với `@Path("/api/v1/courses/{id}/teachers")`. Triển khai endpoint `POST` nhận `TeacherAssignRequest` chứa `teacherId` (UUID), inject `CourseTeacherService` và `KafkaTeacherProducer` để đẩy sự kiện `teacher-assigned` lên Kafka topic `teacher-events` sau khi ghi DB thành công. Endpoint `DELETE /{teacherId}` thực hiện huỷ gán. Áp dụng `@RolesAllowed({"SystemAdmin"})` và `@Valid` cho request body. Trả về HTTP 201/204 tương ứng. Sử dụng `@Transactional` đảm bảo atomic giữa DB write và Kafka publish theo mô hình Outbox.

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```json
{
  "endpoint": "POST /api/v1/courses/{id}/teachers",
  "requestBody": {
    "teacherId": "UUID"
  },
  "responseStatus": 201,
  "kafkaEvent": {
    "topic": "teacher-events",
    "key": "courseId",
    "payload": {
      "eventType": "teacher-assigned",
      "courseId": "UUID",
      "teacherId": "UUID",
      "assignedAt": "ISO-8601 timestamp"
    }
  }
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 3.2: Kiểm thử đơn vị service gán giáo viên

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseTeacherService.java;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/service/CourseServiceTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-009], [ARC-007]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Bổ sung test case trong tệp `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/service/CourseServiceTest.java` cho `CourseTeacherService.assign` và `CourseTeacherService.unassign`. Mock `CourseRepository` và `KafkaTeacherProducer`. Xác nhận rằng khi gán thành công, sự kiện Kafka được publish đúng topic `teacher-events` với payload chứa `eventType=teacher-assigned`, `courseId`, `teacherId`, `assignedAt`. Test trường hợp giáo viên đã tồn tại trong mapping ném `DataIntegrityViolationException` với mã `DUPLICATE_TEACHER_ASSIGNMENT_409`. Test trường hợp course không tồn tại ném `CourseNotFoundException` trả về HTTP 404.

#### 📝 NHIỆM VỤ PHỤ 3.3: Tài liệu luồng Kafka gán giáo viên

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/architecture/course-architecture.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-009], [ARC-008], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Cập nhật tài liệu kiến trúc `./sources/docs/architecture/course-architecture.md` bổ sung sơ đồ Mermaid `sequenceDiagram` cho luồng gán giáo viên, mô tả cách Kafka topic `teacher-events` được publish bởi `KafkaTeacherProducer` và consume bởi notification-service để gửi push notification cho giáo viên. Tham chiếu Tag ID `[REQ-009]`, `[ARC-008]`. Bao gồm sơ đồ Mermaid `flowchart` thể hiện các bước: (1) SystemAdmin gọi POST, (2) Validate teacher tồn tại, (3) Lưu bản ghi course_teacher_mapping, (4) Publish Kafka event, (5) Notification-service consume và gửi push.

#### 📝 NHIỆM VỤ PHỤ 3.4: Review logic gán giáo viên và xử lý ngoại lệ

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/ScheduleConflictException.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-009], [EXC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer đánh giá lớp `ScheduleConflictException` tại `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/ScheduleConflictException.java` đảm bảo kế thừa `RuntimeException`, chứa message mô tả xung đột lịch rõ ràng. Xác nhận có `@ApplicationException` hoặc `ExceptionMapper` để trả về HTTP 409 với mã `SCHEDULE_CONFLICT_409`. Đề xuất bổ sung logging có cấu trúc với MDC tracking `teacherId`, `courseId`, `conflictDateRange` để phục vụ debug khi phát sinh lỗi. Tạo báo cáo review với format: Vấn đề, Mức độ, Đề xuất.

### 🌤️ NGÀY 4: <!--DAY_HEADER_START-->DUYỆT KHOÁ HỌC VÀ ĐĂNG KÝ SINH VIÊN VỚI KAFKA ENROLLMENT-CREATED<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 4.1: Triển khai endpoint duyệt khoá học cho sinh viên

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/StudentCourseBrowseController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-010]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/StudentCourseBrowseController.java` hiện thực hóa lớp `StudentCourseBrowseController` với endpoint `GET /api/v1/students/courses/available` được bảo vệ bởi `@RolesAllowed({"Student"})`. Inject `CourseBrowseService` để lấy danh sách khoá học loại trừ các khoá học sinh viên hiện tại đã đăng ký thông qua subquery `NOT EXISTS` trong JPQL. Trả về danh sách gồm `courseId`, `title`, `capacity`, `schedule`, `centerId`. Sử dụng `@Context SecurityContext` để lấy `studentId` từ JWT token. Trả về HTTP 200 với mảng JSON các khoá học khả dụng.

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```json
{
  "endpoint": "GET /api/v1/students/courses/available",
  "headers": { "Authorization": "Bearer <student_jwt_token>" },
  "response_200": [
    {
      "courseId": "uuid",
      "title": "Lập trình Python nâng cao",
      "capacity": 25,
      "maxStudents": 30,
      "schedule": "Thứ 2, 4, 6 | 18:00 - 20:00",
      "centerId": "uuid",
      "teacherName": "Tran Thi B"
    }
  ]
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 4.2: Kiểm thử đơn vị service duyệt khoá học

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseBrowseService.java;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/service/CourseServiceTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-010]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Bổ sung test case cho `CourseBrowseService.findAvailableCourses(studentId)` trong tệp `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/service/CourseServiceTest.java`. Mock `CourseRepository.findAvailableCourses`. Tạo 4 test case: (1) `findAvailableCourses_returnsEnrolledExclusion` xác minh trả về danh sách khoá học chưa đăng ký; (2) `findAvailableCourses_returnsEmptyWhenAllEnrolled` xác minh trả về mảng rỗng nếu sinh viên đã đăng ký hết; (3) `findAvailableCourses_handlesNullStudentId` xác minh xử lý đúng khi studentId null; (4) `findAvailableCourses_usesJoinWithEnrollments` xác minh truy vấn sử dụng `LEFT JOIN enrollments` với điều kiện `enrollment_id IS NULL`.

#### 📝 NHIỆM VỤ PHỤ 4.3: Tài liệu API duyệt và đăng ký khoá học

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/api/course-openapi.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-010], [REQ-011], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Bổ sung vào tệp `./sources/docs/api/course-openapi.yaml` các endpoint `GET /api/v1/students/courses/available` và `POST /api/v1/enrollments` với mô tả chi tiết response schema, mã lỗi 403 (insufficient privileges), 404 (course not found), 409 (course full, already enrolled). Tham chiếu `[REQ-010]`, `[REQ-011]`. Bổ sung security scheme `BearerAuth` cho các endpoint bảo vệ. Bao gồm ví dụ request/response cho từng trường hợp sử dụng.

#### 📝 NHIỆM VỤ PHỤ 4.4: Review logic duyệt khoá học và performance

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/repository/CourseRepository.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-010], [NFR-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer đánh giá truy vấn `findAvailableCourses` trong tệp `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/repository/CourseRepository.java` đảm bảo sử dụng `LEFT JOIN` với bảng `enrollments` và subquery `NOT EXISTS` để loại trừ hiệu quả. Xác nhận có index trên `(student_id, course_id)` trong bảng `enrollments` đã được tạo ở Giai đoạn 1. Đề xuất materialized view `mv_available_courses` nếu dữ liệu lớn hơn 100.000 bản ghi. Tạo báo cáo review với EXPLAIN ANALYZE cho truy vấn mẫu.

### 🌤️ NGÀY 5: <!--DAY_HEADER_START-->TRIỂN KHAI ENROLLMENT VÀ SỰ KIỆN KAFKA ENROLLMENT-CREATED VỚI AUTO-CREATE STUDENT<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 5.1: Triển khai Enrollment Controller và Service

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/EnrollmentController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-011], [ARC-007]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/EnrollmentController.java` hiện thực hóa lớp `EnrollmentController` với `POST /api/v1/enrollments` nhận `EnrollmentRequest` chứa `courseId` (UUID). Trong `EnrollmentService` xử lý: (1) xác thực sinh viên đã đăng nhập thông qua JWT, (2) kiểm tra khoá học tồn tại và còn capacity, (3) sinh tài khoản Student tự động nếu email chưa tồn tại với role_id=5 và password tạm thời an toàn, (4) tạo bản ghi enrollment, (5) publish sự kiện Kafka `enrollment-events` lên topic `enrollment-events` với payload chứa `eventType=enrollment-created`, `enrollmentId`, `studentId`, `courseId`, `enrollmentDate`, `autoCreatedUser`. Trả về HTTP 201 với thông tin enrollment. Toàn bộ thao tác sử dụng `@Transactional` và ghi audit log.

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```json
{
  "endpoint": "POST /api/v1/enrollments",
  "requestBody": {
    "courseId": "UUID"
  },
  "responseStatus": 201,
  "kafkaEvent": {
    "topic": "enrollment-events",
    "key": "studentId",
    "payload": {
      "eventType": "enrollment-created",
      "enrollmentId": "UUID",
      "studentId": "UUID",
      "courseId": "UUID",
      "enrollmentDate": "ISO-8601 timestamp",
      "autoCreatedUser": "boolean"
    }
  }
}
```
<!--END_API_CONTRACT-->

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```java
// [EXC-001]: Xử lý khoá học đã đầy capacity
public class CourseFullException extends RuntimeException {
    public CourseFullException(UUID courseId) {
        super("Khoá học " + courseId + " đã đạt số lượng sinh viên tối đa");
    }
}

// [EXC-001]: Xử lý khoá học không tồn tại hoặc đã kết thúc
public class EnrollmentNotFoundException extends RuntimeException {
    public EnrollmentNotFoundException(UUID courseId) {
        super("Khoá học " + courseId + " không tồn tại hoặc đã kết thúc");
    }
}

// [EXC-001]: Xử lý sinh viên đã đăng ký trước đó
public class DuplicateEnrollmentException extends RuntimeException {
    public DuplicateEnrollmentException(UUID studentId, UUID courseId) {
        super("Sinh viên " + studentId + " đã đăng ký khoá học " + courseId);
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 5.2: Kiểm thử tích hợp enrollment

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseServiceIntegrationTestSuite.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-011], [ARC-007]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Bổ sung integration test trong tệp `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseServiceIntegrationTestSuite.java` sử dụng Testcontainers PostgreSQL 16-alpine và Embedded Kafka. Tạo 5 test case: (1) `enrollment_successful_returns201` xác minh đăng ký thành công sinh enrollment; (2) `enrollment_autoCreatesStudentForNewEmail` xác minh sinh viên chưa có được tự tạo với role Student; (3) `enrollment_fullCourseReturns409` xác minh khoá học đầy trả về HTTP 409 với mã `COURSE_FULL_409`; (4) `enrollment_duplicateReturns409` xác minh sinh viên đã đăng ký trả về HTTP 409 với mã `ALREADY_ENROLLED_409`; (5) `enrollment_publishesKafkaEvent` xác minh Kafka event được publish đúng topic `enrollment-events` với payload chuẩn.

#### 📝 NHIỆM VỤ PHỤ 5.3: Tài liệu luồng enrollment

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/architecture/course-architecture.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-011], [ARC-008], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Cập nhật tệp `./sources/docs/architecture/course-architecture.md` bổ sung sơ đồ Mermaid `sequenceDiagram` cho luồng đăng ký khoá học, mô tả cách Kafka topic `enrollment-events` được tiêu thụ bởi notification-service để gửi push notification cho sinh viên và Zalo OA cho trung tâm. Tham chiếu `[REQ-011]`, `[ARC-008]`. Bao gồm sơ đồ Mermaid `flowchart` thể hiện các bước: (1) Student gọi POST, (2) Validate course tồn tại, (3) Auto-create Student nếu cần, (4) Tạo enrollment, (5) Publish Kafka event, (6) Notification-service gửi push + Zalo.

#### 📝 NHIỆM VỤ PHỤ 5.4: Review xử lý auto-create student

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/EnrollmentService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-011], [EXC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer đánh giá logic auto-create Student account trong tệp `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/EnrollmentService.java` đảm bảo có transaction bao quát với `@Transactional`, kiểm tra email uniqueness, gán role Student mặc định (role_id=5), sinh password tạm thời an toàn bằng `SecureRandom` với độ dài 16 ký tự. Đề xuất xử lý race condition khi hai request đồng thời tạo cùng email thông qua `INSERT ... ON CONFLICT DO NOTHING` hoặc retry với exponential backoff. Tạo báo cáo review với phân tích rủi ro và đề xuất cải tiến.

### 🌤️ NGÀY 6: <!--DAY_HEADER_START-->KHỞI TẠO ATTENDANCE-SERVICE VÀ MODULE GIẢI MÃ QR PAYLOAD<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 6.1: Triển khai mô tả build và ứng dụng attendance-service

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/attendance-service/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [REQ-012]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `pom.xml` cho `attendance-service` tại đường dẫn `./sources/backend/attendance-service/pom.xml` thừa kế parent `./sources/backend/pom.xml`. Khai báo dependency Quarkus 3.15.1: `quarkus-resteasy-reactive-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-reactive-messaging-kafka`, `quarkus-hibernate-validator`, `quarkus-smallrye-openapi`, `quarkus-cache` (Caffeine). ArtifactId `attendance-service`, groupId `org.nlh4j.membershiphub`. Dependencies test gồm `quarkus-junit5`, `rest-assured`, `mockito-core`, `org.testcontainers:postgresql:1.20.4`, `org.testcontainers:kafka:1.20.4`. Tệp phải biên dịch trống thông qua `mvn clean install -DskipTests`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Migration V1__attendance_init.sql [DAT-006]
-- Khởi tạo bảng attendance với composite unique key đảm bảo idempotency
CREATE TABLE attendance (
    attendance_id UUID PRIMARY KEY,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    idempotency_key VARCHAR(100),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT fk_attendance_course FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT uq_attendance_unique_day UNIQUE (student_id, course_id, attendance_date)
);

CREATE INDEX idx_attendance_student_date ON attendance(student_id, attendance_date);
CREATE INDEX idx_attendance_course_date ON attendance(course_id, attendance_date);
CREATE INDEX idx_attendance_idempotency ON attendance(idempotency_key) WHERE idempotency_key IS NOT NULL;
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 6.2: Kiểm thử tích hợp mô tả build attendance-service

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceIntegrationTestSuite.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [REQ-012]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử tích hợp `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceIntegrationTestSuite.java` sử dụng JUnit 5 Platform Launcher kết nối script shell `./sources/infra/test/maven-build-integration.sh` để xác minh `./sources/backend/attendance-service/pom.xml` biên dịch sạch. Test phải fail nếu dependency chưa khả dụng, parent pom không hợp lệ, hoặc artifactId không khớp `attendance-service`. Verify file `target/quarkus-app/quarkus-run.jar` được tạo ra với kích thước hợp lệ.

#### 📝 NHIỆM VỤ PHỤ 6.3: Tài liệu kiến trúc attendance-service

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/architecture/attendance-architecture.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-012], [REQ-013], [ARC-007], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tài liệu Markdown tại `./sources/docs/architecture/attendance-architecture.md` mô tả kiến trúc `attendance-service` gồm sơ đồ C4 Container với các thành phần REST Controller, Service, Repository, Kafka Producer, QrPayloadDecoder. Tài liệu phải liệt kê đầy đủ Tag ID `[REQ-012]`, `[REQ-013]`, `[ARC-007]`, `[EXC-001]`, `[EXC-002]`, `[EXC-005]`. Bao gồm sơ đồ Mermaid `flowchart` mô tả các bước xử lý QR scan: (1) Mobile app scan QR, (2) Decode base64 payload, (3) Validate enrollment, (4) Check idempotency, (5) Persist hoặc trả duplicate, (6) Publish Kafka event.

#### 📝 NHIỆM VỤ PHỤ 6.4: Review mã nguồn khởi tạo attendance-service

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceApplication.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [REQ-012]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer đánh giá tệp `AttendanceServiceApplication.java` tại `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceApplication.java` chứa annotation `@QuarkusMain`, đảm bảo cấu hình gói `org.nlh4j.membershiphub.attendanceservice` chính xác. Xác nhận không có tham chiếu `com.example`, hàm `main` chuẩn Quarkus. Phát hiện sớm các vấn đề cấu hình như thiếu `quarkus.banner.enabled=false` cho production, sai cấu hình port, hoặc thiếu health check endpoint. Tạo báo cáo review ngắn gọn.

### 🌤️ NGÀY 7: <!--DAY_HEADER_START-->TRIỂN KHAI LUỒNG ĐIỂM DANH QR VỚI IDEMPOTENCY, RETRY VÀ FIFO RECOVERY<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 7.1: Triển khai Attendance Controller và Service

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/controller/AttendanceController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-012], [REQ-013], [ARC-007]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/controller/AttendanceController.java` hiện thực hóa lớp `AttendanceController` với `POST /api/v1/attendance/scan` nhận `QrScanRequest` chứa `qrPayload` (base64 string) và `idempotencyKey` (UUID hoặc hash). Inject `AttendanceService` và `QrPayloadDecoder`. Service thực hiện: (1) giải mã payload base64 lấy `studentId` và `courseId` thông qua `QrPayloadDecoder`, (2) kiểm tra enrollment tồn tại thông qua truy vấn JOIN bảng `enrollments`, (3) kiểm tra idempotency qua composite unique key `(student_id, course_id, attendance_date)`, (4) tạo attendance record nếu chưa tồn tại hoặc trả về response với cờ `duplicate: true` nếu đã tồn tại, (5) publish sự kiện Kafka `attendance-events`. Trả về HTTP 201 cho lần đầu, HTTP 200 với `duplicate: true` cho các lần sau.

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```json
{
  "endpoint": "POST /api/v1/attendance/scan",
  "requestBody": {
    "qrPayload": "base64 string chứa studentID và courseID",
    "idempotencyKey": "UUID hoặc hash string"
  },
  "responseSuccess": {
    "status": 201,
    "body": {
      "attendanceId": "UUID",
      "studentId": "UUID",
      "courseId": "UUID",
      "attendanceDate": "YYYY-MM-DD",
      "timestamp": "ISO-8601",
      "duplicate": false
    }
  },
  "responseDuplicate": {
    "status": 200,
    "body": {
      "message": "already recorded",
      "duplicate": true
    }
  },
  "kafkaEvent": {
    "topic": "attendance-events",
    "payload": {
      "eventType": "attendance-recorded",
      "studentId": "UUID",
      "courseId": "UUID",
      "timestamp": "ISO-8601"
    }
  }
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 7.2: Kiểm thử đơn vị attendance service

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/AttendanceService.java;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/service/AttendanceServiceTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-012], [REQ-013], [ARC-007]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử đơn vị `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/service/AttendanceServiceTest.java` sử dụng JUnit 5 kết hợp Mockito 5.7.0. Mock `AttendanceRepository`, `EnrollmentRepository`, `KafkaAttendanceProducer`, `QrPayloadDecoder`. Tạo 6 test case: (1) `scanAttendance_firstTime_returns201WithDuplicateFalse` xác minh quét QR thành công tạo attendance mới; (2) `scanAttendance_duplicateSameDay_returns200WithDuplicateTrue` xác minh quét trùng trong ngày trả duplicate flag; (3) `scanAttendance_studentNotEnrolled_throwsEnrollmentRequiredException` xác minh sinh viên chưa enroll ném `EnrollmentRequiredException`; (4) `scanAttendance_invalidPayload_throwsInvalidQrPayloadException` xác minh payload không hợp lệ ném exception; (5) `scanAttendance_publishesKafkaEvent` xác minh Kafka event được publish với payload đúng; (6) `scanAttendance_idempotencyKeyPreventsDuplication` xác minh cùng idempotency key chỉ tạo một bản ghi.

#### 📝 NHIỆM VỤ PHỤ 7.3: Tài liệu API và luồng retry cho attendance

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/api/attendance-openapi.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-012], [REQ-013], [EXC-001], [EXC-005], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tệp OpenAPI 3.1 YAML tại `./sources/docs/api/attendance-openapi.yaml` mô tả endpoint `POST /api/v1/attendance/scan` với schema `QrScanRequest` chứa `qrPayload` và `idempotencyKey`, schema `AttendanceResponse` chứa `attendanceId`, `studentId`, `courseId`, `attendanceDate`, `timestamp`, `duplicate`. Mô tả mã lỗi 400 (invalid QR payload), 403 (enrollment required), 409 (duplicate), 503 (service unavailable). Bổ sung mô tả cơ chế retry queue khi mất mạng `[EXC-001]` với exponential backoff và FIFO khi khôi phục `[EXC-005]`. Tham chiếu Tag ID đầy đủ. Bao gồm sơ đồ Mermaid `sequenceDiagram` thể hiện luồng xử lý từ mobile app đến database.

#### 📝 NHIỆM VỤ PHỤ 7.4: Review logic idempotency và xử lý ngoại lệ

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/exception/DuplicateAttendanceException.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-013], [EXC-001], [EXC-002], [EXC-005]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer đánh giá tệp `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/exception/DuplicateAttendanceException.java` và logic trong `AttendanceService` đảm bảo idempotency thông qua composite unique key `(student_id, course_id, attendance_date)`. Xác nhận cơ chế retry queue xử lý `[EXC-001]` với tối đa 3 lần retry và FIFO recovery `[EXC-005]` thông qua việc sử dụng `BlockingQueue` hoặc Kafka offset commit. Đề xuất tối ưu performance cho truy vấn tần suất cao thông qua Redis cache. Tạo báo cáo review với format: Vấn đề, Mức độ, Đề xuất.

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```java
// [EXC-001]: Xử lý khi sinh viên chưa đăng ký khoá học
public class EnrollmentRequiredException extends RuntimeException {
    public EnrollmentRequiredException(UUID studentId, UUID courseId) {
        super("Sinh viên " + studentId + " chưa đăng ký khoá học " + courseId);
    }
}

// [EXC-002]: Xử lý khi quét trùng trong cùng ngày - trả success với duplicate: true
public class DuplicateAttendanceException extends RuntimeException {
    private final boolean duplicate;
    public DuplicateAttendanceException(UUID studentId, UUID courseId, LocalDate date) {
        super("Điểm danh đã được ghi nhận cho sinh viên " + studentId + " ngày " + date);
        this.duplicate = true;
    }
    public boolean isDuplicate() { return duplicate; }
}

// [EXC-001]: Xử lý khi payload QR không hợp lệ
public class InvalidQrPayloadException extends RuntimeException {
    public InvalidQrPayloadException(String message) {
        super("Invalid QR payload: " + message);
    }
}
```
<!--END_EXC_HANDLER-->