# Giai đoạn 3: <!--PHASE_NAME_START-->Phát Triển Nghiệp Vụ Khóa Học, Ghi Danh Và Điểm Danh QR<!--PHASE_NAME_END-->

## 📊 Kiểm Soát Tài Liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Bản Thiết Kế** | ARCH-20260829125322 |
| **Tên Dự Án** | membership-hub |
| **Giai đoạn** | 3 |
| **Tên Giai Đoạn** | <!--PHASE_NAME_START-->Phát Triển Nghiệp Vụ Khóa Học, Ghi Danh Và Điểm Danh QR<!--PHASE_NAME_END--> |
| **Mô Tả** | <!--PHASE_DESC_START-->Giai đoạn 3 tập trung kiến lập hoàn chỉnh hai miền nghiệp vụ cốt lõi là course-service và attendance-service trong hệ thống Membership Hub, bao gồm module quản lý khóa học với cơ chế xác thực chồng lấn lịch giáo viên thông qua trigger cơ sở dữ liệu, quy trình ghi danh học viên kèm tự động tạo tài khoản khi thiếu, tích hợp hàng đợi sự kiện Kafka để đẩy thông báo đa kênh khi phân công giáo viên và đăng ký khóa học, đồng thời triển khai API quét QR điểm danh với khóa tổng hợp idempotency đảm bảo mỗi học viên chỉ được ghi nhận một lần cho mỗi khóa học trong cùng một ngày. Toàn bộ tài sản mã nguồn, di trú schema, hợp đồng API, tài liệu kiến trúc và bộ xử lý ngoại lệ bản địa hóa tiếng Việt được kiến lập và truy vết đầy đủ bằng hệ thống thẻ TagID chuẩn doanh nghiệp.<!--PHASE_DESC_END--> |
| **Phiên Bản** | 1.0 (Đường Cơ Sở) |
| **Thời Gian** | 2026/08/29 12:53:22 |
| **Tác Giả** | Kiến Trúc Sư Hệ Thống Doanh Nghiệp (SA Agent) |
| **Phê Duyệt** | Đang Chờ Đánh Giá Quản Trị Kỹ Thuật |

## 1. Phạm Vi Hoạt Động & Mục Tiêu Giai Đoạn

Giai đoạn 3 thực hiện ba nhiệm vụ cốt lõi được phân bổ theo bảng tóm tắt đa giai đoạn: Nhiệm vụ 5 (quản lý khóa học với kiểm tra xung đột lịch trình), Nhiệm vụ 6 (ghi danh học viên và duyệt khóa học khả dụng), Nhiệm vụ 7 (quét QR điểm danh với đảm bảo idempotency). Phạm vi kéo dài từ Ngày 1 đến Ngày 5 với tổng cộng ba mươi mốt nhiệm vụ phụ được phân bổ cho bốn tác nhân chuyên biệt: Coder chịu trách nhiệm xây dựng thực thể JPA, repository, service, controller, exception handler và migration SQL; Tester xây dựng bộ kiểm thử đơn vị và tích hợp; Reviewer thực hiện đánh giá mã tĩnh và kiểm định logic bảo mật; Doc soạn thảo hợp đồng OpenAPI, sơ đồ Mermaid và checklist review.

Các tài sản kỹ thuật bắt buộc phải sinh ra bao gồm: mô-đun course-service với thực thể Course ánh xạ bảng courses, CourseRepository hỗ trợ truy vấn overlap, CourseService xử lý CRUD và phân công giáo viên, CourseController REST, CourseAssignmentService phát hành sự kiện Kafka; mô-đun attendance-service với thực thể Enrollment và Attendance, EnrollmentRepository, AttendanceService xử lý idempotency, AttendanceController endpoint scan, QrPayloadDecoder giải mã base64, AttendanceOutboxRelay đảm bảo xử lý FIFO khi khôi phục; hai tập lệnh di trú V2 và V3 bổ sung ràng buộc composite unique key và trigger overlap; bộ xử lý ngoại lệ cục bộ cho các mã lỗi TEACHER_SCHEDULE_OVERLAP, DUPLICATE_ENROLLMENT, DUPLICATE_ATTENDANCE, STUDENT_NOT_ENROLLED, INVALID_QR_PAYLOAD; hai hợp đồng OpenAPI YAML; sơ đồ Mermaid mô tả luồng phân công và ghi danh; checklist review. Toàn bộ tài sản phải được gắn thẻ truy xuất theo hệ thống TagID `[REQ-007]`, `[REQ-008]`, `[REQ-009]`, `[REQ-010]`, `[REQ-011]`, `[REQ-012]`, `[REQ-013]`, `[ARC-007]`, `[ARC-008]`, `[EXC-001]`, `[EXC-002]`, `[EXC-004]` để đảm bảo khả năng truy vết đầy đủ.

## 2. Phạm Vi Kỹ Thuật Cho Phép & Ranh Giới Thư Mục

Danh sách tệp vật lý và điểm cuối được phép sinh ra trong giai đoạn này:

* `./sources/backend/course-service/pom.xml` [ARC-000]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/Course.java` [DAT-003], [REQ-007]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseRepository.java` [REQ-007], [REQ-008]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/ScheduleOverlapValidator.java` [REQ-008], [EXC-004]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseService.java` [REQ-007], [REQ-008], [REQ-009], [EXC-004]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseAssignmentService.java` [REQ-009], [ARC-008]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseController.java` [REQ-007], [REQ-008], [REQ-009]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/CourseDto.java` [REQ-007]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/CourseCreateRequest.java` [REQ-008], [EXC-004]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/TeacherAssignmentRequest.java` [REQ-009]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/TeacherScheduleOverlapException.java` [REQ-008], [EXC-004]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/CourseNotFoundException.java` [REQ-007], [EXC-004]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/CourseHasEnrollmentsException.java` [REQ-008], [EXC-004]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/TeacherNotFoundException.java` [REQ-009], [EXC-004]
* `./sources/backend/course-service/src/main/resources/db/migration/V2__course_overlap_triggers.sql` [REQ-008], [DAT-003]
* `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/ScheduleOverlapValidatorTest.java` [REQ-008], [EXC-004]
* `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseServiceTest.java` [REQ-008], [EXC-004]
* `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseServicesTestSuite.java` [REQ-008], [REQ-009], [ARC-008]
* `./sources/backend/attendance-service/pom.xml` [ARC-000]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/Attendance.java` [DAT-005], [REQ-012], [REQ-013]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/Enrollment.java` [DAT-004], [REQ-010], [REQ-011]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/EnrollmentRepository.java` [REQ-010], [REQ-011], [DAT-004]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/EnrollmentService.java` [REQ-010], [REQ-011], [EXC-004], [ARC-008]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/EnrollmentController.java` [REQ-010], [REQ-011], [ARC-008]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceService.java` [REQ-012], [REQ-013], [EXC-001], [EXC-002], [ARC-007]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceController.java` [REQ-012], [REQ-013], [ARC-007]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/QrPayloadDecoder.java` [REQ-012], [ARC-007]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceOutboxRelay.java` [EXC-001], [EXC-005], [REQ-012]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/QrScanRequest.java` [REQ-012], [ARC-007]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/AttendanceResponse.java` [REQ-013], [EXC-002]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/EnrollmentRequest.java` [REQ-011]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/EnrollmentDto.java` [REQ-010]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/exception/DuplicateAttendanceException.java` [REQ-013], [EXC-002]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/exception/StudentNotEnrolledException.java` [REQ-012], [EXC-004]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/exception/InvalidQrPayloadException.java` [REQ-012], [EXC-004]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/exception/DuplicateEnrollmentException.java` [REQ-011], [EXC-004]
* `./sources/backend/attendance-service/src/main/resources/db/migration/V3__enrollment_unique_index.sql` [REQ-013], [DAT-004], [DAT-005]
* `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/QrPayloadDecoderTest.java` [REQ-012], [ARC-007]
* `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceTest.java` [REQ-013], [EXC-002]
* `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/EnrollmentIntegrationTest.java` [REQ-010], [REQ-011], [EXC-004], [ARC-008]
* `./sources/docs/contracts/course-openapi.yaml` [ARC-007], [ARC-008]
* `./sources/docs/contracts/attendance-openapi.yaml` [ARC-007], [ARC-008]
* `./sources/docs/diagrams/course-attendance-flow.mmd` [ARC-007], [ARC-008]
* `./sources/docs/reviews/phase-3-code-review-checklist.md` [REQ-008], [REQ-013]

* **RÀNG BUỘC BẮT BUỘC VỀ BIỂU MẪU NỀN TẢNG**:
  - Cấu trúc package Java phải tuân thủ nghiêm ngặt quy ước `org.nlh4j.membershiphub.<tên-dịch-vụ>` cho mọi tệp nguồn.
  - Mọi thay đổi schema cơ sở dữ liệu phải thông qua tập tin migration Flyway versioned, cấm sửa đổi trực tiếp.
  - Tất cả REST endpoint phải khai báo JSON contract rõ ràng với request/response schema và HTTP status code tiêu chuẩn.
  - Cam kết OWASP Top 10: chuẩn bị câu lệnh parameterized chống SQL injection, escape output chống XSS.

## 3. Chỉ Thị Chức Năng Chuyên Biệt Cho Tác Nhân Phụ

*   **Coder**: Đóng vai trò Nhà Phát Triển Ứng Dụng Cao Cấp. Chịu trách nhiệm triển khai mã nguồn ứng dụng thuần túy trên cả hai dịch vụ backend course-service và attendance-service, bao gồm thực thể JPA ánh xạ bảng, repository truy cập dữ liệu với Panache, dịch vụ nghiệp vụ với annotation `@Transactional`, bộ điều khiển REST với `@Path` chuẩn, bộ giải mã QR base64, bộ phát hành sự kiện Kafka, bộ xử lý ngoại lệ bản địa hóa, và tập lệnh di trú Flyway DDL. Bị cấm viết bộ kiểm thử hoặc biểu mẫu hạ tầng.

* **Tester**: Đóng vai trò Trưởng Nhóm Kiểm Thử/Đảm Bảo Chất Lượng. Chuyên về kỹ thuật bộ kiểm thử, xác thực và cổng chất lượng. Chịu trách nhiệm sinh JUnit, kiểm thử tích hợp với Testcontainers, kiểm thử đơn vị với Mockito, kiểm thử tham số hóa và kịch bản xác thực idempotency. Bị cấm sửa đổi mã nguồn sản phẩm. Nếu nhiệm vụ phụ liên quan đến phạm vi tích hợp tổng thể hoặc đầu-cuối mà không thể khoanh vùng một tệp mã nguồn cụ thể, phải xuất chính xác chuỗi ký tự `INTEGRATION_SCOPE` làm tham số đầu tiên của cặp phân tách bằng dấu chấm phẩy.

* **Doc**: Đóng vai trò Chuyên Viên Viết Tài Liệu Kỹ Thuật và Kiến Trúc Sư Hệ Thống Doanh Nghiệp. Chuyên về biên soạn tài liệu đặc tả kỹ thuật toàn diện, hợp đồng OpenAPI 3.0.3, sơ đồ Mermaid mô tả luồng nghiệp vụ, checklist review và danh mục kiến trúc doanh nghiệp phù hợp với các lớp topology dự án đang hoạt động. Mỗi tệp tài liệu kỹ thuật được sinh ra phải được liệt kê dưới dạng thực thể đường dẫn tệp rõ ràng kết thúc bằng phần mở rộng `.md` hoặc `.yaml` hoặc `.mmd` và nằm hoàn toàn trong sơ đồ lưu trữ tập trung: `./sources/docs/`.

*   **Reviewer**: Chịu trách nhiệm xác minh trình biên dịch, cổng phân tích tĩnh và vá lỗi phòng thủ. Chuyên về đánh giá chất lượng mã, giải quyết lỗi biên dịch, sửa lỗ hổng bảo mật OWASP và xử lý các blocker cổng chất lượng SonarQube. Đánh giá tuân thủ quy ước RBAC, biên giới transaction, validation đầu vào và che giấu dữ liệu nhạy cảm.

*   **Docker**: Chuyên biệt về container hóa, kỹ thuật Dockerfile đa giai đoạn, tối ưu gói và đẩy tài sản ảnh ứng dụng đã xác minh lên DockerHub.

*   **GCP**: Chuyên về tự động hóa đám mây trong Google Cloud Platform. Chịu trách nhiệm xây dựng và đẩy ảnh lên Google Cloud Artifact Registry (GCR), điều phối môi trường container nguyên bản trên Google Cloud Run.

*   **GKE**: Chuyên về điều phối container sản xuất bên trong Google Kubernetes Engine. Chịu trách nhiệm xây dựng manifest triển khai Kubernetes, điều khiển định tuyến, cấu hình HPA, biểu đồ Helm và triển khai tải công việc microservice vào cụm GKE đang hoạt động.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn

Giai đoạn 3 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: một trăm phần trăm endpoint khóa học (GET, POST, PUT, DELETE) hoạt động đúng theo đặc tả với cơ chế xác thực overlap lịch giáo viên thông qua trigger cơ sở dữ liệu; module ghi danh cho phép học viên duyệt khóa học khả dụng và đăng ký với cơ chế tự động tạo tài khoản kèm phát hành sự kiện Kafka; API quét QR điểm danh hoạt động với cơ chế idempotency thông qua khóa tổng hợp UNIQUE đảm bảo mỗi lần quét trùng lặp chỉ trả về cờ duplicate mà không tạo bản ghi mới; cơ chế AttendanceOutboxRelay xử lý FIFO khi khôi phục dịch vụ sau sự cố; tập lệnh di trú V2 bổ sung trigger overlap và unique index; tập lệnh di trú V3 bổ sung composite unique key cho bảng enrollments và attendance; toàn bộ bộ xử lý ngoại lệ toàn cục chuẩn hóa phản hồi lỗi với mã lỗi và thông điệp bản địa hóa tiếng Việt; bộ kiểm thử đơn vị và tích hợp phủ sóng tối thiểu 85 phần trăm các luồng nghiệp vụ trọng yếu; hai hợp đồng OpenAPI YAML được soạn thảo đầy đủ với đặc tả schema và response codes; sơ đồ Mermaid mô tả luồng phân công và ghi danh; checklist review cập nhật đầy đủ. Một trăm phần trăm mã TagID `[REQ-007]`, `[REQ-008]`, `[REQ-009]`, `[REQ-010]`, `[REQ-011]`, `[REQ-012]`, `[REQ-013]`, `[ARC-007]`, `[ARC-008]`, `[EXC-001]`, `[EXC-002]`, `[EXC-004]` được ánh xạ chính xác trong báo cáo đánh giá cuối giai đoạn. Mọi vi phạm chuẩn OWASP Top 10 phải được phát hiện và khắc phục trong quá trình review.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->KHỞI TẠO MODULE KHÓA HỌC VÀ SCHEMA OVERLAP<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 1.1: Khởi tạo descriptor Maven cho course-service
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/course-service/pom.xml

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Khởi tạo descriptor Maven cho module `course-service` thừa kế từ `pom.xml` gốc `org.nlh4j:membershiphub:1.0.0-SNAPSHOT`. Khai báo `<groupId>org.nlh4j.membershiphub.courseservice</groupId>`, `<artifactId>course-service</artifactId>`, `<version>1.0.0-SNAPSHOT</version>`. Bao gồm các dependency Quarkus 3.15.1: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-messaging-kafka` cho sự kiện thông báo, `quarkus-smallrye-health`. Cấu hình `quarkus.smallrye-jwt.enabled=true` và `quarkus.hibernate-orm.database.generation=validate` để buộc sử dụng Flyway migrations. Tích hợp plugin `quarkus-maven-plugin` với các goal `build`, `generate-code`, `generate-code-tests`. Đảm bảo tất cả identifier ở dạng chữ thường alphanumeric, không chứa ký tự `-` hoặc `_`.

* **Database Schema DDL SQL Specification [DAT-XXX]:** Không có thay đổi schema cơ sở dữ liệu trong nhiệm vụ phụ này; bảng courses đã được tạo tại Giai đoạn 1.

* **API and Event Routing Contracts [REQ-007], [ARC-007]:** Khối này không xuất hiện vì nhiệm vụ tập trung vào descriptor Maven và chưa định nghĩa endpoint runtime.

* **Phase Localized Exception Handlers [EXC-XXX]:** Khối này không xuất hiện vì nhiệm vụ chỉ tập trung vào biểu mẫu xây dựng.

#### 📝 NHIỆM VỤ PHỤ 1.2: Tạo thực thể Course ánh xạ bảng courses
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/Course.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-003], [REQ-007]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo class `@Entity` trong package `org.nlh4j.membershiphub.courseservice` ánh xạ bảng `courses` với các trường: `courseId` kiểu UUID làm khóa chính sử dụng `@Id` và `@GeneratedValue`, ánh xạ cột `course_id` với `columnDefinition = "uuid"`; `title` kiểu String, `@Column(nullable = false, length = 150)` ánh xạ cột `title`; `description` kiểu String, ánh xạ cột `description` kiểu TEXT; `startDate` kiểu LocalDate, `@Column(name = "start_date", nullable = false)`; `endDate` kiểu LocalDate, `@Column(name = "end_date", nullable = false)`; `teacherId` kiểu UUID, `@Column(name = "teacher_id", nullable = false, columnDefinition = "uuid")`; `maxStudents` kiểu Integer, `@Column(name = "max_students", nullable = false)` giá trị mặc định 30. Thực thể phải kế thừa `PanacheEntityBase` để tận dụng các phương thức tiện ích của Panache. Áp dụng annotation `@Table(name = "courses")` để liên kết với bảng cơ sở dữ liệu. Mọi truy vấn đến thực thể phải sử dụng prepared statement thông qua Panache để chống SQL injection theo chuẩn OWASP A03.

* **Database Schema DDL SQL Specification [DAT-003]:** <!--START_DDL_MIGRATION-->
```sql
-- Tham chiếu schema đã tồn tại ở Giai đoạn 1; không thêm lệnh DDL mới trong nhiệm vụ này
```
<!--END_DDL_MIGRATION-->

* **Phase Localized Exception Handlers [EXC-004]:** <!--START_EXC_HANDLER-->
```json
{
  "TEACHER_SCHEDULE_OVERLAP": {
    "httpStatus": 409,
    "message": "Giáo viên đã có lịch trình chồng lấn trong khoảng thời gian khóa học được yêu cầu"
  }
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 1.3: Tạo CourseRepository với Panache
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseRepository.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-007], [REQ-008]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Khai báo interface `CourseRepository extends PanacheRepository<Course>` trong package `org.nlh4j.membershiphub.courseservice`. Định nghĩa query method `findByTeacherIdAndDateRangeOverlap(UUID teacherId, LocalDate startDate, LocalDate endDate, UUID excludeCourseId)` sử dụng JPQL với named parameters: `SELECT COUNT(c) FROM Course c WHERE c.teacherId = :teacherId AND c.courseId <> :excludeCourseId AND NOT (c.endDate < :startDate OR c.startDate > :endDate)`. Bổ sung method `findAllWithTeacher()` sử dụng `@Query` với JOIN FETCH để tránh N+1 query khi truy xuất danh sách khóa học kèm tên giáo viên. Thêm method `existsById(UUID courseId)` kế thừa từ Panache và `deleteById(UUID courseId)` với annotation `@Transactional` ở tầng service. Mọi truy vấn phải sử dụng named parameters để chống SQL injection.

* **API and Event Routing Contracts [REQ-007], [ARC-007]:** Khối này không xuất hiện vì repository là tầng truy cập dữ liệu; endpoint sẽ được thêm ở nhiệm vụ sau.

* **Phase Localized Exception Handlers [EXC-XXX]:** Khối này không xuất hiện vì nhiệm vụ không áp dụng logic ngoại lệ nghiệp vụ.

#### 📝 NHIỆM VỤ PHỤ 1.4: Tạo ScheduleOverlapValidator với bộ test tham số hóa
##### Tác Nhân Được Phân Công: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/ScheduleOverlapValidator.java;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/ScheduleOverlapValidatorTest.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-008], [EXC-004]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo class `ScheduleOverlapValidator` với annotation `@ApplicationScoped` chứa method `validateNoOverlap(Course newCourse, List<Course> existingTeacherCourses)` trả về boolean. Thuật toán: với mỗi khóa học hiện tại, kiểm tra `newCourse.endDate < existing.startDate || newCourse.startDate > existing.endDate`; nếu có bất kỳ overlap nào, ném `TeacherScheduleOverlapException`. Sau đó viết `ScheduleOverlapValidatorTest` sử dụng JUnit 5, AssertJ và `@ParameterizedTest` với `@CsvSource` cho 5 trường hợp: (1) overlap đầu-cuối `(2026-01-01,2026-01-15)` vs `(2026-01-10,2026-01-20)`; (2) overlap hoàn toàn `(2026-02-01,2026-02-28)` vs `(2026-02-10,2026-02-15)`; (3) overlap một phần `(2026-03-01,2026-03-15)` vs `(2026-03-15,2026-03-30)`; (4) không overlap `(2026-04-01,2026-04-10)` vs `(2026-04-11,2026-04-20)`; (5) khóa học cùng giáo viên nhưng cùng ngày `(2026-05-01,2026-05-05)` vs `(2026-05-05,2026-05-10)`. Sử dụng mock entity thông qua constructor hoặc builder pattern.

* **Database Schema DDL SQL Specification [DAT-XXX]:** Không có thay đổi schema vì validator sử dụng mock entity.

* **API and Event Routing Contracts [REQ-008], [ARC-007]:** Khối này không xuất hiện vì validator là thành phần nội bộ không expose HTTP.

* **Phase Localized Exception Handlers [EXC-004]:** <!--START_EXC_HANDLER-->
```json
{
  "TEACHER_SCHEDULE_OVERLAP_HTTP_409": "Xác nhận validator ném TeacherScheduleOverlapException khi overlap được phát hiện với message bản địa hóa tiếng Việt"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 1.5: Tạo migration V2 bổ sung trigger overlap
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/course-service/src/main/resources/db/migration/V2__course_overlap_triggers.sql

* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-003], [REQ-008]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Đặt tệp Flyway migration theo quy ước `V2__course_overlap_triggers.sql` trong `course-service/src/main/resources/db/migration/`. Migration gồm bốn phần chính: (1) `ALTER TABLE courses ADD CONSTRAINT chk_courses_dates CHECK (end_date >= start_date);` để đảm bảo ngày kết thúc luôn sau hoặc bằng ngày bắt đầu; (2) `CREATE UNIQUE INDEX ux_courses_teacher_dates ON courses (teacher_id, start_date, end_date);` để hỗ trợ tra cứu nhanh; (3) function `fn_check_teacher_overlap()` sử dụng `RAISE EXCEPTION 'TEACHER_SCHEDULE_OVERLAP'` với `ERRCODE = '23514'` khi phát hiện overlap; (4) trigger `trg_courses_overlap_check BEFORE INSERT OR UPDATE` gọi function trên. Toàn bộ SQL phải tuân thủ ANSI SQL chuẩn, không sử dụng cú pháp đặc thù PostgreSQL không tương thích.

* **Database Schema DDL SQL Specification [DAT-003]:** <!--START_DDL_MIGRATION-->
```sql
-- =====================================================================
-- V2__course_overlap_triggers.sql
-- Bổ sung ràng buộc chồng lấn lịch giáo viên cho bảng courses
-- =====================================================================

ALTER TABLE courses
    ADD CONSTRAINT chk_courses_dates CHECK (end_date >= start_date);

ALTER TABLE courses
    ADD CONSTRAINT chk_courses_title_len CHECK (char_length(title) <= 150);

CREATE UNIQUE INDEX ux_courses_teacher_dates
    ON courses (teacher_id, start_date, end_date);

CREATE OR REPLACE FUNCTION fn_check_teacher_overlap()
RETURNS TRIGGER AS $$
DECLARE
    overlap_count INTEGER;
BEGIN
    SELECT COUNT(1)
      INTO overlap_count
      FROM courses c
     WHERE c.teacher_id = NEW.teacher_id
       AND c.course_id <> COALESCE(NEW.course_id, '00000000-0000-0000-0000-000000000000'::uuid)
       AND NOT (c.end_date < NEW.start_date OR c.start_date > NEW.end_date);

    IF overlap_count > 0 THEN
        RAISE EXCEPTION 'TEACHER_SCHEDULE_OVERLAP'
            USING ERRCODE = '23514',
                  HINT = 'Teacher already assigned to an overlapping course window';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_courses_overlap_check
    BEFORE INSERT OR UPDATE ON courses
    FOR EACH ROW
    EXECUTE FUNCTION fn_check_teacher_overlap();
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-008], [ARC-007]:** Khối này không xuất hiện vì migration tự động chạy khi service khởi động; không liên quan HTTP contract.

* **Phase Localized Exception Handlers [EXC-004]:** Khi trigger kích hoạt, exception `TEACHER_SCHEDULE_OVERLAP` được ném với SQLSTATE 23514, tầng service sẽ bắt và ánh xạ thành HTTP 409 với message bản địa hóa tiếng Việt.

#### 📝 NHIỆM VỤ PHỤ 1.6: Soạn sơ đồ Mermaid cho luồng khóa học
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/diagrams/course-attendance-flow.mmd

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-007], [ARC-008]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Soạn tệp Mermaid `sequenceDiagram` mô tả luồng nghiệp vụ khóa học và điểm danh tại `./sources/docs/diagrams/course-attendance-flow.mmd`. Sơ đồ phải bao gồm các participant: `Admin UI`, `course-service`, `attendance-service`, `Kafka Topic (notification.events)`, `notification-service`, `Mobile App`, `Zalo Group`. Luồng 1: Admin tạo khóa học thông qua `course-service`; trigger overlap check trong cơ sở dữ liệu; nếu trùng lặp trả lỗi; nếu thành công lưu bản ghi. Luồng 2: Admin gán giáo viên qua `PUT /api/v1/courses/{id}/teachers`; `course-service` phát hành sự kiện `TeacherAssigned` lên Kafka topic `notification.events`; `notification-service` consume; đẩy FCM tới Mobile App và tin nhắn tới Zalo Group. Luồng 3: Student duyệt khóa học qua `GET /api/v1/enrollments/browse`; đăng ký qua `POST /api/v1/enrollments`; `attendance-service` phát hành sự kiện `StudentEnrolled`. Sử dụng định dạng Mermaid 10.x với cú pháp `participant` và mũi tên `->>` rõ ràng.

* **Database Schema DDL SQL Specification [DAT-XXX]:** Không có thay đổi schema trong nhiệm vụ tài liệu.

* **API and Event Routing Contracts [ARC-007], [ARC-008]:** Sơ đồ Mermaid đã mô tả trong tệp `.mmd`; sử dụng `sequenceDiagram` của Mermaid 10.x.

* **Phase Localized Exception Handlers [EXC-XXX]:** Khối này không xuất hiện vì tài liệu kiến trúc không chứa logic xử lý ngoại lệ.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->TRIỂN KHAI API KHÓA HỌC VÀ PHÂN CÔNG GIÁO VIÊN<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 2.1: Xây dựng CourseService xử lý CRUD và overlap
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseService.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-007], [REQ-008], [REQ-009]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo class `@ApplicationScoped` `CourseService` trong package `org.nlh4j.membershiphub.courseservice` inject `CourseRepository` và `CourseAssignmentService`. Phương thức `listAll()` trả về `List<CourseDto>`; `create(CourseCreateRequest req)` thực hiện validate (title length ≤ 150, endDate >= startDate, teacherId not null) rồi gọi `repository.persist()` để trigger DB bắt lỗi overlap. Phương thức `update(UUID id, CourseUpdateRequest req)` cập nhật các trường cho phép; nếu thay đổi teacherId thì trigger overlap check tự động. Phương thức `delete(UUID id)` kiểm tra enrollment tồn tại trước khi xóa, nếu có enrollment đang ACTIVE thì ném `CourseHasEnrollmentsException` với HTTP 409. Mọi phương thức ghi phải sử dụng `@Transactional` để đảm bảo tính nguyên tử. Inject `CourseAssignmentService` để phát hành sự kiện Kafka khi phân công giáo viên thành công. Tuân thủ OWASP A01 bằng cách kiểm tra quyền truy cập dựa trên vai trò.

* **API and Event Routing Contracts [REQ-007], [REQ-008], [REQ-009], [ARC-008]:** <!--START_API_CONTRACT-->
```json
{
  "service_methods": [
    "listAll(): List<CourseDto>",
    "create(CourseCreateRequest): CourseDto",
    "update(UUID, CourseUpdateRequest): CourseDto",
    "delete(UUID): void",
    "assignTeacher(UUID courseId, UUID teacherId): void -> publish TeacherAssigned"
  ]
}
```
<!--END_API_CONTRACT-->

* **Database Schema DDL SQL Specification [DAT-003]:** Tận dụng trigger `V2__course_overlap_triggers.sql` đã tạo Ngày 1.

* **Phase Localized Exception Handlers [EXC-004]:** <!--START_EXC_HANDLER-->
```json
{
  "TEACHER_SCHEDULE_OVERLAP": "Bắt PSQLException SQLSTATE 23514, ném TeacherScheduleOverlapException với message bản địa hóa",
  "COURSE_HAS_ENROLLMENTS": "HTTP 409 khi xóa khóa học đang có học viên đăng ký với message 'Khóa học đang có học viên đăng ký, không thể xóa'"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 2.2: Xây dựng CourseController REST
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseController.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-007], [REQ-008], [REQ-009]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo class `@Path("/api/v1/courses")` `CourseController` trong package `org.nlh4j.membershiphub.courseservice` inject `CourseService`. Định nghĩa các endpoint: `@GET` trả về `Response` 200 với danh sách DTO; `@POST` nhận `CourseCreateRequest` trả về 201 hoặc 409 với mã lỗi TEACHER_SCHEDULE_OVERLAP; `@PUT /{courseId}` cập nhật trả 200 hoặc 404 hoặc 409; `@DELETE /{courseId}` xóa trả 204 hoặc 409; `@PUT /{courseId}/teachers` phân công giáo viên trả 200 hoặc 404. Áp dụng `@RolesAllowed({"SystemAdmin", "CenterAdmin"})` cho POST/PUT/DELETE để thực thi RBAC. Sử dụng `@Valid` cho request body và `@NotNull` cho các trường bắt buộc. Response DTO phải ẩn các trường nhạy cảm theo OWASP A01.

* **Database Schema DDL SQL Specification [DAT-003]:** Không có thay đổi schema trong nhiệm vụ này.

* **API and Event Routing Contracts [REQ-007], [REQ-008], [REQ-009], [ARC-008]:** <!--START_API_CONTRACT-->
```json
{
  "endpoints": [
    "GET /api/v1/courses -> 200 List<CourseDto>",
    "POST /api/v1/courses -> 201/409",
    "PUT /api/v1/courses/{id} -> 200/404/409",
    "DELETE /api/v1/courses/{id} -> 204/409",
    "PUT /api/v1/courses/{id}/teachers -> 200/404"
  ]
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-004]:** <!--START_EXC_HANDLER-->
```json
{
  "TEACHER_SCHEDULE_OVERLAP_HTTP_409": "ConflictExceptionMapper trả 409 với payload {code, message} bản địa hóa",
  "COURSE_NOT_FOUND_HTTP_404": "NotFoundExceptionMapper trả 404 với message bản địa hóa",
  "COURSE_HAS_ENROLLMENTS_HTTP_409": "Trả 409 với message 'Khóa học đang có học viên đăng ký, không thể xóa'"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 2.3: Xây dựng CourseAssignmentService phát hành sự kiện Kafka
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseAssignmentService.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-009], [ARC-008]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo class `@ApplicationScoped` `CourseAssignmentService` trong package `org.nlh4j.membershiphub.courseservice` inject `Emitter<NotificationEvent>` với channel `notification-events` và `CourseRepository`. Phương thức `assignTeacher(UUID courseId, UUID teacherId)` kiểm tra sự tồn tại của course và teacher; cập nhật `courses.teacher_id` thông qua `repository.update()`; emit `NotificationEvent{type=TEACHER_ASSIGNED, courseId, teacherId, timestamp}` lên Kafka topic. Sử dụng `@Transactional` để đảm bảo DB write và Kafka emit nằm trong cùng transaction boundary an toàn; nếu Kafka emit thất bại thì rollback DB. Cấu hình `mp.messaging.outgoing.notification-events.connector=smallrye-kafka` và `mp.messaging.outgoing.notification-events.topic=notification-events` trong application.properties.

* **Database Schema DDL SQL Specification [DAT-003]:** Tận dụng cột `teacher_id` hiện có trong bảng `courses`.

* **API and Event Routing Contracts [REQ-009], [ARC-008]:** <!--START_API_CONTRACT-->
```json
{
  "kafka_topic": "notification-events",
  "event_schema": {
    "type": "TEACHER_ASSIGNED",
    "courseId": "uuid",
    "teacherId": "uuid",
    "timestamp": "ISO-8601"
  }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:** Ngoại lệ sẽ được xử lý ở tầng notification-service trong Giai đoạn 4.

#### 📝 NHIỆM VỤ PHỤ 2.4: Viết test tích hợp cho CourseService
##### Tác Nhân Được Phân Công: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseServicesTestSuite.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-008], [REQ-009]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo class `CourseServicesTestSuite` sử dụng `@QuarkusTest` với Testcontainers PostgreSQL 16. Các test case bao gồm: (1) `testCreateCourse_NoOverlap` expect 201 và trả về CourseDto với courseId; (2) `testCreateCourse_Overlap` expect `TeacherScheduleOverlapException` với message chứa "chồng lấn"; (3) `testUpdateCourse_ChangeTeacher_TriggersOverlapCheck` expect exception khi thay đổi teacherId dẫn đến overlap; (4) `testAssignTeacher_PublishesEvent` sử dụng `@InjectMock` `Emitter` để verify emit được gọi với payload đúng; (5) `testDeleteCourse_HasEnrollments` expect `CourseHasEnrollmentsException` với HTTP 409. Sử dụng RestAssured để gọi HTTP endpoint và verify status code. Migration `V2__course_overlap_triggers.sql` tự động chạy thông qua Flyway trong Testcontainer.

* **Database Schema DDL SQL Specification [DAT-003]:** Testcontainer tự động chạy `V2__course_overlap_triggers.sql` thông qua Flyway.

* **API and Event Routing Contracts [REQ-008], [REQ-009], [ARC-008]:** <!--START_API_CONTRACT-->
```json
{
  "test_scope": "INTEGRATION_SCOPE",
  "endpoints_under_test": [
    "POST /api/v1/courses",
    "PUT /api/v1/courses/{id}/teachers"
  ]
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-004]:** <!--START_EXC_HANDLER-->
```json
{
  "test_verification": "Assert TeacherScheduleOverlapException được ném với message chứa 'chồng lấn'"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 2.5: Đánh giá code CourseController và CourseService
##### Tác Nhân Được Phân Công: Reviewer
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseController.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-007], [REQ-008], [REQ-009]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Thực hiện code review tập trung vào năm khía cạnh: (1) Tuân thủ RBAC annotation cho endpoint nhạy cảm POST/PUT/DELETE; (2) Xử lý transaction boundary đúng giữa DB write và Kafka emit trong `CourseAssignmentService`; (3) Validation input đầy đủ sử dụng `@Valid`, `@NotNull`, `@Size` từ Jakarta Bean Validation; (4) Sử dụng `Optional` đúng cách trong lookup hoặc ném exception khi không tìm thấy; (5) Đảm bảo response DTO không lộ password hash hoặc trường nhạy cảm theo OWASP A01. Tạo file checklist `./sources/docs/reviews/phase-3-code-review-checklist.md` ghi nhận findings và đề xuất cải tiến.

* **Database Schema DDL SQL Specification [DAT-XXX]:** Không áp dụng cho nhiệm vụ đánh giá.

* **API and Event Routing Contracts [REQ-007], [REQ-008], [REQ-009], [ARC-008]:** <!--START_API_CONTRACT-->
```json
{
  "review_focus": [
    "RBAC enforcement on POST/PUT/DELETE",
    "Transaction boundary between DB and Kafka",
    "Input validation completeness",
    "Response DTO sensitivity",
    "OWASP A01 Broken Access Control"
  ]
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-004]:** <!--START_EXC_HANDLER-->
```json
{
  "review_checkpoint": "Đảm bảo TeacherScheduleOverlapExceptionMapper trả message bản địa hóa tiếng Việt và HTTP 409"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 2.6: Soạn hợp đồng OpenAPI cho course-service
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/contracts/course-openapi.yaml

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-007], [ARC-008]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Soạn file `course-openapi.yaml` chuẩn OpenAPI 3.0.3 tại `./sources/docs/contracts/course-openapi.yaml`. Mô tả 5 endpoint: `GET /api/v1/courses` trả về danh sách CourseDto với response 200; `POST /api/v1/courses` nhận `CourseCreateRequest` với các trường bắt buộc `title`, `startDate`, `endDate`, `teacherId` và trả 201 hoặc 409 với mã `TEACHER_SCHEDULE_OVERLAP`; `PUT /api/v1/courses/{courseId}` cập nhật với response 200/404/409; `DELETE /api/v1/courses/{courseId}` trả 204 hoặc 409 với mã `COURSE_HAS_ENROLLMENTS`; `PUT /api/v1/courses/{courseId}/teachers` nhận `TeacherAssignmentRequest{teacherId}` và trả 200 hoặc 404. Định nghĩa schema `CourseDto` với `courseId`, `title`, `startDate`, `endDate`, `teacherName`; schema `CourseCreateRequest` với validation `title` max 150, `dates` format YYYY-MM-DD, `maxStudents` từ 1 đến 500 mặc định 30. Khai báo security scheme bearerAuth với JWT.

* **Database Schema DDL SQL Specification [DAT-XXX]:** Tài liệu; không áp dụng schema.

* **API and Event Routing Contracts [ARC-007], [ARC-008]:** <!--START_API_CONTRACT-->
```yaml
openapi: 3.0.3
info:
  title: Course Service API
  version: 1.0.0
paths:
  /api/v1/courses:
    get:
      summary: List courses
      security:
        - bearerAuth: []
      responses:
        '200':
          description: Course grid
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/CourseDto'
    post:
      summary: Create course
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CourseCreateRequest'
      responses:
        '201': { description: Created }
        '409': { description: TEACHER_SCHEDULE_OVERLAP }
  /api/v1/courses/{courseId}/teachers:
    put:
      summary: Assign teacher
      security:
        - bearerAuth: []
      parameters:
        - name: courseId
          in: path
          required: true
          schema: { type: string, format: uuid }
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/TeacherAssignmentRequest'
      responses:
        '200': { description: Assigned, event published }
        '404': { description: Not found }
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-004]:** <!--START_EXC_HANDLER-->
```json
{
  "documented_errors": ["TEACHER_SCHEDULE_OVERLAP", "COURSE_NOT_FOUND", "COURSE_HAS_ENROLLMENTS"]
}
```
<!--END_EXC_HANDLER-->

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->KHỞI TẠO ATTENDANCE-SERVICE VÀ ENROLLMENT MODULE<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 3.1: Tạo descriptor Maven cho attendance-service
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/pom.xml

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo descriptor Maven `attendance-service` thừa kế từ `pom.xml` gốc. Khai báo `<groupId>org.nlh4j.membershiphub.attendanceservice</groupId>`, `<artifactId>attendance-service</artifactId>`. Bao gồm các dependency Quarkus 3.15.1: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-messaging-kafka`, `quarkus-smallrye-health`. Cấu hình `mp.messaging.outgoing.notification-events.connector=smallrye-kafka` và `mp.messaging.outgoing.notification-events.topic=notification-events`. Tích hợp plugin `quarkus-maven-plugin` chuẩn. Cấu hình `quarkus.hibernate-orm.database.generation=validate` để buộc sử dụng Flyway migrations.

* **Database Schema DDL SQL Specification [DAT-004], [DAT-005]:** Migration sẽ được thêm ở nhiệm vụ 3.4.

* **API and Event Routing Contracts [ARC-007], [ARC-008]:** <!--START_API_CONTRACT-->
```json
{
  "messaging_channels": {
    "outgoing": ["notification-events (Kafka topic)"]
  }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:** Khối này không xuất hiện vì nhiệm vụ chỉ tập trung vào descriptor Maven.

#### 📝 NHIỆM VỤ PHỤ 3.2: Tạo thực thể Enrollment ánh xạ bảng enrollments
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/Enrollment.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-004], [REQ-010], [REQ-011]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo class `@Entity` `Enrollment` trong package `org.nlh4j.membershiphub.attendanceservice` với các trường: `enrollmentId` kiểu UUID làm khóa chính; `studentId` kiểu UUID với `@Column(name = "student_id", nullable = false, columnDefinition = "uuid")`; `courseId` kiểu UUID với `@Column(name = "course_id", nullable = false, columnDefinition = "uuid")`; `enrollmentDate` kiểu LocalDateTime với `@Column(name = "enrollment_date", nullable = false)`, giá trị mặc định `LocalDateTime.now()`. Áp dụng `@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(name = "ux_enrollments_student_course", columnNames = {"student_id", "course_id"}))` để ngăn đăng ký trùng lặp tại tầng database. Thực thể kế thừa `PanacheEntityBase`.

* **Database Schema DDL SQL Specification [DAT-004]:** Unique constraint sẽ được áp dụng bởi `V3__enrollment_unique_index.sql` ở nhiệm vụ 3.4.

* **API and Event Routing Contracts [REQ-010], [REQ-011], [ARC-008]:** Khối này không xuất hiện vì thực thể chưa expose endpoint trong nhiệm vụ này.

* **Phase Localized Exception Handlers [EXC-004]:** <!--START_EXC_HANDLER-->
```json
{
  "DUPLICATE_ENROLLMENT": "Unique constraint violation -> HTTP 409 với message 'Học viên đã đăng ký khóa học này'"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 3.3: Tạo thực thể Attendance với composite key
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/Attendance.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-005], [REQ-012], [REQ-013]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo class `@Entity` `Attendance` trong package `org.nlh4j.membershiphub.attendanceservice` với các trường: `attendanceId` kiểu UUID làm khóa chính; `studentId` kiểu UUID; `courseId` kiểu UUID; `attendanceDate` kiểu LocalDate; `timestamp` kiểu LocalDateTime với giá trị mặc định `LocalDateTime.now()`; `status` kiểu String với `@Column(nullable = false, length = 20)`, giá trị mặc định `PRESENT`; `createdAt` kiểu LocalDateTime với `@Column(name = "created_at", nullable = false, updatable = false)`. Áp dụng `@Table(name = "attendance", uniqueConstraints = @UniqueConstraint(name = "ux_attendance_student_course_date", columnNames = {"student_id", "course_id", "attendance_date"}))` để đảm bảo idempotency ở tầng database theo yêu cầu REQ-013. Thực thể kế thừa `PanacheEntityBase`.

* **Database Schema DDL SQL Specification [DAT-005]:** Composite unique key sẽ được áp dụng bởi `V3__enrollment_unique_index.sql` cho bảng attendance.

* **API and Event Routing Contracts [REQ-012], [REQ-013], [ARC-007]:** Khối này không xuất hiện vì thực thể chưa expose endpoint trong nhiệm vụ này.

* **Phase Localized Exception Handlers [EXC-002]:** <!--START_EXC_HANDLER-->
```json
{
  "DUPLICATE_ATTENDANCE": "Composite key violation -> trả về success với cờ duplicate: true và message 'Điểm danh đã được ghi nhận trước đó trong ngày'"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 3.4: Tạo migration V3 bổ sung composite unique key
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/src/main/resources/db/migration/V3__enrollment_unique_index.sql

* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-004], [DAT-005], [REQ-013]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Đặt tệp Flyway `V3__enrollment_unique_index.sql` trong `attendance-service/src/main/resources/db/migration/`. Nội dung migration bao gồm bốn lệnh: (1) `ALTER TABLE enrollments ADD CONSTRAINT ux_enrollments_student_course UNIQUE (student_id, course_id);` để ngăn đăng ký trùng lặp; (2) `ALTER TABLE attendance ADD CONSTRAINT ux_attendance_student_course_date UNIQUE (student_id, course_id, attendance_date);` để đảm bảo idempotency điểm danh; (3) `CREATE INDEX ix_attendance_course_date ON attendance (course_id, attendance_date);` để hỗ trợ truy vấn báo cáo; (4) `CREATE INDEX ix_enrollments_course ON enrollments (course_id);` để hỗ trợ truy vấn duyệt khóa học. SQL phải tuân thủ ANSI chuẩn, sử dụng `IF NOT EXISTS` khi cần thiết để idempotency migration.

* **Database Schema DDL SQL Specification [DAT-004], [DAT-005]:** <!--START_DDL_MIGRATION-->
```sql
-- =====================================================================
-- V3__enrollment_unique_index.sql
-- Bổ sung composite unique key cho enrollment và attendance
-- =====================================================================

ALTER TABLE enrollments
    ADD CONSTRAINT ux_enrollments_student_course UNIQUE (student_id, course_id);

ALTER TABLE attendance
    ADD CONSTRAINT ux_attendance_student_course_date UNIQUE (student_id, course_id, attendance_date);

CREATE INDEX ix_attendance_course_date ON attendance (course_id, attendance_date);
CREATE INDEX ix_enrollments_course ON enrollments (course_id);
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-013], [ARC-007]:** Khối này không xuất hiện vì migration tự động chạy khi service khởi động; không liên quan HTTP contract.

* **Phase Localized Exception Handlers [EXC-002]:** Unique constraint SQLSTATE 23505 bị bắt ở tầng service để trả về cờ `duplicate: true` thay vì lỗi.

#### 📝 NHIỆM VỤ PHỤ 3.5: Tạo EnrollmentRepository với custom query
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/EnrollmentRepository.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-010], [REQ-011]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo interface `EnrollmentRepository extends PanacheRepository<Enrollment>` trong package `org.nlh4j.membershiphub.attendanceservice`. Định nghĩa các phương thức: `findByStudentId(UUID studentId)` trả về `List<Enrollment>` sử dụng `find("studentId", studentId).list()`; `existsByStudentIdAndCourseId(UUID studentId, UUID courseId)` trả về `boolean` sử dụng `count("studentId = ?1 and courseId = ?2", studentId, courseId) > 0`; `findAvailableCoursesForStudent(UUID studentId, LocalDate today)` sử dụng JPQL với named parameters: `SELECT c FROM Course c WHERE c.courseId NOT IN (SELECT e.courseId FROM Enrollment e WHERE e.studentId = :studentId) AND c.startDate <= :today AND c.endDate >= :today`. Mọi truy vấn sử dụng named parameters để chống SQL injection theo OWASP A03.

* **Database Schema DDL SQL Specification [DAT-004]:** Tận dụng index `ix_enrollments_course` đã tạo trong V3.

* **API and Event Routing Contracts [REQ-010], [REQ-011], [ARC-008]:** Khối này không xuất hiện vì repository là tầng truy cập dữ liệu; endpoint sẽ được thêm ở nhiệm vụ sau.

* **Phase Localized Exception Handlers [EXC-XXX]:** Khối này không xuất hiện vì nhiệm vụ không áp dụng logic ngoại lệ nghiệp vụ ở tầng repository.

#### 📝 NHIỆM VỤ PHỤ 3.6: Bổ sung checklist review cho enrollment module
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/reviews/phase-3-code-review-checklist.md

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-011], [ARC-008]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Bổ sung mục "Enrollment và Attendance Module" vào checklist tại `./sources/docs/reviews/phase-3-code-review-checklist.md`. Mục này phải bao gồm các điểm kiểm tra: (1) Unique constraint được khai báo đồng bộ giữa JPA annotation và Flyway migration; (2) Auto-provision tài khoản student khi đăng ký sử dụng transaction an toàn với idempotency check; (3) Notification event phải chứa đầy đủ thông tin `studentId`, `courseId`, `timestamp` theo định dạng ISO-8601; (4) Idempotency điểm danh được verify qua test với cờ `duplicate: true`; (5) Tuân thủ GDPR/CCPA khi xử lý dữ liệu cá nhân học viên theo NFR-008; (6) Không log mật khẩu hoặc thông tin nhạy cảm theo OWASP A09. Tài liệu sử dụng định dạng Markdown với checkbox `- [ ]` cho từng tiêu chí.

* **Database Schema DDL SQL Specification [DAT-XXX]:** Tài liệu; không áp dụng schema.

* **API and Event Routing Contracts [REQ-011], [ARC-008]:** <!--START_API_CONTRACT-->
```json
{
  "checklist_section": "Enrollment & Attendance Module",
  "review_criteria": [
    "JPA-DB unique constraint consistency",
    "Idempotent account provisioning",
    "Notification event completeness",
    "Idempotency verification",
    "GDPR/CCPA compliance",
    "No sensitive data in logs"
  ]
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:** Khối này không xuất hiện vì tài liệu không chứa logic xử lý ngoại lệ.

### 🌤️ NGÀY 4: <!--DAY_HEADER_START-->TRIỂN KHAI ENROLLMENT API VÀ DUYỆT KHÓA HỌC<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 4.1: Xây dựng EnrollmentService xử lý duyệt và đăng ký
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/EnrollmentService.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-010], [REQ-011]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo class `@ApplicationScoped` `EnrollmentService` trong package `org.nlh4j.membershiphub.attendanceservice` inject `EnrollmentRepository`, `UserServiceClient` (REST client tới user-service qua MicroProfile Rest Client) và `Emitter<NotificationEvent>`. Phương thức `browseAvailableCourses(UUID studentId)` gọi `repository.findAvailableCoursesForStudent(studentId, LocalDate.now())` và trả về danh sách. Phương thức `registerStudent(UUID courseId, UUID authenticatedStudentId)` thực hiện: (1) kiểm tra enrollment tồn tại; (2) gọi `UserServiceClient.provisionStudent` nếu tài khoản chưa tồn tại với idempotency check; (3) `repository.persist()` enrollment mới; (4) emit `NotificationEvent{type=STUDENT_ENROLLED, studentId, courseId, timestamp}`. Sử dụng `@Transactional` với `REQUIRES_NEW` cho persist. Bắt `ConstraintViolationException` để chuyển thành `DuplicateEnrollmentException`. Tuân thủ OWASP A01 bằng cách trích `studentId` từ JWT thay vì từ request body.

* **Database Schema DDL SQL Specification [DAT-004]:** Sử dụng unique constraint đã có để bắt duplicate.

* **API and Event Routing Contracts [REQ-010], [REQ-011], [ARC-008]:** <!--START_API_CONTRACT-->
```json
{
  "service_methods": [
    "browseAvailableCourses(UUID studentId): List<CourseDto>",
    "registerStudent(UUID courseId, UUID studentId): EnrollmentDto"
  ],
  "kafka_event": {
    "type": "STUDENT_ENROLLED",
    "studentId": "uuid",
    "courseId": "uuid",
    "timestamp": "ISO-8601"
  }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-004]:** <!--START_EXC_HANDLER-->
```json
{
  "DUPLICATE_ENROLLMENT": "Bắt ConstraintViolationException, trả HTTP 409 với message bản địa hóa 'Học viên đã đăng ký khóa học này'",
  "COURSE_NOT_ACTIVE": "HTTP 400 khi khóa học đã kết thúc với message 'Khóa học đã kết thúc, không thể đăng ký'"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 4.2: Xây dựng EnrollmentController REST
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/EnrollmentController.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-010], [REQ-011], [ARC-008]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo class `@Path("/api/v1/enrollments")` `EnrollmentController` trong package `org.nlh4j.membershiphub.attendanceservice` inject `EnrollmentService`. Định nghĩa `@GET /browse` với `@RolesAllowed({"Student"})` sử dụng `@Context SecurityContext` để trích `studentId` từ JWT subject; trả về danh sách khóa học khả dụng với response 200. Định nghĩa `@POST` nhận `EnrollmentRequest{courseId}` với `@Valid`; trả về `EnrollmentDto` với response 201 hoặc 409 với mã `DUPLICATE_ENROLLMENT`. Áp dụng `@Inject JsonWebToken jwt` để trích xuất thông tin xác thực. Sử dụng `@Transactional` trên phương thức POST để đảm bảo tính nguyên tử.

* **Database Schema DDL SQL Specification [DAT-004]:** Không thay đổi schema trong nhiệm vụ này.

* **API and Event Routing Contracts [REQ-010], [REQ-011], [ARC-008]:** <!--START_API_CONTRACT-->
```json
{
  "endpoints": [
    "GET /api/v1/enrollments/browse -> 200 List<CourseDto>",
    "POST /api/v1/enrollments -> 201 EnrollmentDto / 409 DUPLICATE_ENROLLMENT"
  ]
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-004]:** <!--START_EXC_HANDLER-->
```json
{
  "DUPLICATE_ENROLLMENT_HTTP_409": "Trả message 'Học viên đã đăng ký khóa học này'",
  "UNAUTHORIZED_HTTP_401": "Khi JWT thiếu hoặc không hợp lệ với message 'Yêu cầu cần xác thực'"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 4.3: Xây dựng QrPayloadDecoder utility
##### Tác Nhân Được Phân Côn