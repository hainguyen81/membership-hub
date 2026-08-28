# Giai đoạn 3: <!--PHASE_NAME_START-->Quản Lý Khóa Học, Ghi Danh và Dashboard Phân Tích<!--PHASE_NAME_END-->

## 📊 Quản Lý Tài Liệu

| Mục | Chi tiết |
| :--- | :--- |
| **Mã bản thiết kế** | ARCH-20260828112120 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 3 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Quản Lý Khóa Học, Ghi Danh và Dashboard Phân Tích<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn 3 tập trung xây dựng hai microservice nghiệp vụ cốt lõi là course-service và enrollment-service trong hệ thống Quarkus. Giai đoạn này hiện thực hóa toàn bộ luồng quản lý khóa học gồm danh sách, CRUD với trigger PostgreSQL phát hiện xung đột lịch trình giáo viên, phân công giáo viên kèm phát sự kiện Kafka, đồng thời xây dựng luồng ghi danh cho phép duyệt khóa học khả dụng, tự động tạo tài khoản học viên khi ghi danh và phát thông báo đa kênh. Cuối cùng, giai đoạn hoàn thiện dashboard tổng hợp ghi danh với cache Redis 15 phút cung cấp chỉ số tổng học viên, khóa học đang hoạt động và buổi học sắp tới trong 7 ngày, đảm bảo hiệu năng sub-second cho 10.000 người dùng đồng thời theo NFR-001. Toàn bộ logic phải tuân thủ nguyên tắc bảo mật OWASP, sử dụng distributed lock chống race condition và idempotent producer cho Kafka.<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Đường cơ sở) |
| **Ngày giờ** | 2026/08/28 11:21:20 |
| **Tác giả** | Kiến trúc sư hệ thống doanh nghiệp (SA Agent) |
| **Phê duyệt** | Đang chờ đánh giá quản trị kỹ thuật |

## 1. Phạm Vi Hoạt Động & Mục Tiêu Của Giai Đoạn

Giai đoạn 3 thuộc dự án membership-hub tập trung xây dựng hai microservice nghiệp vụ trọng yếu gồm `course-service` và `enrollment-service` nhằm hiện thực hóa toàn bộ luồng quản lý khóa học và ghi danh học viên. Phạm vi hoạt động cốt lõi của giai đoạn này bao gồm năm trụ cột chính: (1) Triển khai endpoint GET `/api/v1/courses` trong course-service với định dạng lưới dữ liệu gồm `CourseID`, `Title`, `StartDate`, `EndDate`, `TeacherName`, đảm bảo phản hồi dưới 200ms theo NFR-001; (2) Xây dựng CRUD `/api/v1/courses` với trigger PostgreSQL `trg_teacher_schedule_conflict` tự động phát hiện xung đột lịch trình giáo viên, ném ngoại lệ `ScheduleConflictException` ánh xạ sang HTTP 409 mã `SCHEDULE_CONFLICT`; (3) Triển khai endpoint phân công giáo viên `POST /api/v1/courses/{id}/teachers` kèm phát sự kiện Kafka lên topic `course.teacher.assigned` với idempotent producer config; (4) Xây dựng endpoint duyệt khóa học `GET /api/v1/students/{id}/available-courses` loại trừ các khóa học đã ghi danh, hiển thị capacity và lịch trình; (5) Triển khai endpoint ghi danh `POST /api/v1/enrollments` với cơ chế auto-provisioning tạo tài khoản Student khi email chưa tồn tại, sử dụng distributed lock Redis SETNX chống race condition, kèm phát sự kiện Kafka lên topic `enrollment.created` cho hệ thống thông báo đa kênh; (6) Hoàn thiện dashboard tổng hợp `GET /api/v1/dashboard/enrollment-summary` aggregate các chỉ số `totalStudents`, `activeCourses`, `upcomingSessions` trong 7 ngày tới với cache Redis TTL 900 giây, circuit breaker fallback compute trực tiếp từ PostgreSQL khi cache lỗi.

Mục tiêu kỹ thuật cụ thể bao gồm việc tuân thủ nghiêm ngặt quy ước đặt tên package `org.nlh4j.membershiphub.courseservice` và `org.nlh4j.membershiphub.enrollmentservice` cho toàn bộ mã nguồn Java, áp dụng nguyên tắc PreparedStatement thông qua Hibernate ORM Panache và JPA named parameters để loại bỏ tuyệt đối SQL injection, sử dụng Bean Validation với các ràng buộc `@NotNull`, `@Size`, `@Pattern` cho mọi DTO đầu vào, tích hợp distributed lock Redis SETNX với key `enrollment:lock:{studentId}:{courseId}` TTL 5 giây để chống race condition, cấu hình Kafka producer với `enable.idempotence=true` và `acks=all` đảm bảo chính xác một lần gửi sự kiện, đồng thời xây dựng bộ exception mapper chuyển đổi mã lỗi nghiệp vụ sang HTTP status code chuẩn (409 cho xung đột lịch trình, 400 cho validation, 422 cho provisioning thất bại).

## 2. Phạm Vi Kỹ Thuật Được Phép & Ranh Giới Thư Mục

Danh sách kiểm tra kỹ thuật dưới đây định nghĩa 100% các tệp vật lý được phép khởi tạo trong phạm vi giai đoạn này, mỗi mục đại diện cho một tệp cụ thể kèm Tag ID truy vết:

* `./sources/backend/course-service/pom.xml` — [ARC-000]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseController.java` — [REQ-007], [REQ-008]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseService.java` — [REQ-008]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/ScheduleValidator.java` — [REQ-008], [EXC-004]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/TeacherAssignmentService.java` — [REQ-009]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseBrowseService.java` — [REQ-010]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseRepository.java` — [REQ-007]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseEntity.java` — [REQ-007]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/TeacherEntity.java` — [REQ-009]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseMapper.java` — [REQ-007]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseNotFoundException.java` — [EXC-004]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/ScheduleConflictException.java` — [REQ-008], [EXC-004]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/ExceptionMappers.java` — [EXC-004]
* `./sources/backend/course-service/src/main/resources/db/migration/V3__course_module_indexes.sql` — [REQ-008]
* `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseControllerTest.java` — [REQ-007]
* `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/ScheduleValidatorTest.java` — [REQ-008]
* `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/TeacherAssignmentServiceTest.java` — [REQ-009]
* `./sources/backend/enrollment-service/pom.xml` — [ARC-000]
* `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentController.java` — [REQ-011]
* `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentService.java` — [REQ-011]
* `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/StudentProvisioningService.java` — [REQ-011]
* `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/DashboardService.java` — [REQ-025]
* `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentRepository.java` — [REQ-011]
* `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentEntity.java` — [REQ-011]
* `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/DashboardCacheService.java` — [REQ-025]
* `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/DashboardSummaryResponse.java` — [REQ-025]
* `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/KafkaEnrollmentProducer.java` — [ARC-008]
* `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/exception/DuplicateEnrollmentException.java` — [EXC-004]
* `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/exception/ExceptionMappers.java` — [EXC-004]
* `./sources/backend/enrollment-service/src/main/resources/application.properties` — [ARC-008]
* `./sources/backend/enrollment-service/src/test/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentControllerTest.java` — [REQ-011]
* `./sources/backend/enrollment-service/src/test/java/org/nlh4j/membershiphub/enrollmentservice/StudentProvisioningServiceTest.java` — [REQ-011]
* `./sources/backend/enrollment-service/src/test/java/org/nlh4j/membershiphub/enrollmentservice/DashboardServiceTest.java` — [REQ-025]
* `./sources/docs/api/course-service-api.md` — [REQ-007], [REQ-008], [REQ-009], [REQ-010]
* `./sources/docs/api/enrollment-service-api.md` — [REQ-011], [REQ-025]
* `./sources/docs/database/course-module-schema.md` — [REQ-008]

* **BẮT BUỘC VỀ BỘ KHUNG NỀN TẢNG**: Khi khởi tạo blueprint vòng đời hoạt động (giới hạn cụ thể trong Giai đoạn 3 - NGÀY 1), cần phải tiêm và khai báo rõ ràng các bộ mô tả cấu trúc hạ tầng kho lưu trữ chính trước khi tạo bất kỳ thành phần mã nguồn nghiệp vụ nào. Đối với kiến trúc backend Microservices, phải thực thi định nghĩa đường dẫn bắt buộc của bộ mô tả dự án cha `./sources/backend/pom.xml` và các bộ mô tả module con riêng biệt `./sources/backend/<tên-dịch-vụ>/pom.xml`. Toàn bộ tài sản khung được tạo ra phải ánh xạ chặt chẽ tới mã theo dõi kiến trúc hệ thống `[ARC-000]`.

## 3. Chỉ Thị Chức Năng Chuyên Biệt Cho Các Sub-Agent

*   **Coder**: Đóng vai trò Nhà phát triển ứng dụng cao cấp. Chịu trách nhiệm triển khai mã nguồn nghiệp vụ thuần túy trên dịch vụ backend course-service và enrollment-service. Bị cấm viết bộ kiểm thử hoặc bản kê khai hạ tầng.
* **Tester**: Đóng vai trò Trưởng phòng QC/QA. Chuyên về kỹ thuật bộ kiểm thử, xác nhận hợp lệ và cổng gác chất lượng. Chịu trách nhiệm tạo JUnit, kiểm thử tích hợp sử dụng Testcontainers PostgreSQL và REST Assured. Bị cấm sửa đổi mã sản phẩm. Nếu nhiệm vụ phụ liên quan đến phạm vi tích hợp, phải xuất chính xác chuỗi ký tự `INTEGRATION_SCOPE` làm tham số đầu tiên của cặp dấu chấm phẩy.
* **Doc**: Đóng vai trò Technical Writer chính và Kiến trúc sư hệ thống doanh nghiệp. Chuyên biên soạn tài liệu đặc tả kỹ thuật toàn diện, tài liệu tham chiếu API, bản thiết kế kiến trúc dịch vụ và sơ đồ cơ sở dữ liệu. Mọi tệp tài liệu kỹ thuật được tạo ra phải được liệt kê dưới dạng thực thể đường dẫn tệp rõ ràng kết thúc bằng phần mở rộng `.md` và nằm hoàn toàn trong bố cục lưu trữ tập trung `./sources/docs/`.
*   **Reviewer**: Chịu trách nhiệm xác minh trình biên dịch, cổng gác phân tích tĩnh và vá lỗi phòng thủ. Chuyên về kiểm toán chất lượng mã, giải quyết lỗi biên dịch, sửa lỗ hổng bảo mật OWASP và xử lý các blocker cổng chất lượng SonarQube.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn (DoD)

Giai đoạn 3 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: (1) Endpoint GET `/api/v1/courses` trả về lưới dữ liệu khóa học với đầy đủ trường `courseId`, `title`, `startDate`, `endDate`, `teacherName` trong thời gian phản hồi dưới 200ms theo NFR-001. (2) Endpoint POST `/api/v1/courses` tạo khóa học mới thành công và trigger PostgreSQL `trg_teacher_schedule_conflict` phát hiện xung đột lịch trình giáo viên, trả về HTTP 409 mã `SCHEDULE_CONFLICT` khi có xung đột. (3) Endpoint PUT `/api/v1/courses/{id}` cập nhật khóa học thành công với `@PreUpdate` callback tự động cập nhật `updated_at`. (4) Endpoint DELETE `/api/v1/courses/{id}` kiểm tra quan hệ khóa ngoại `Enrollments.course_id`, trả về HTTP 422 nếu còn ghi danh. (5) Endpoint POST `/api/v1/courses/{id}/teachers` phân công giáo viên thành công, ghi lịch sử vào bảng `CourseTeacherHistory`, phát sự kiện Kafka lên topic `course.teacher.assigned` với idempotent producer config. (6) Endpoint GET `/api/v1/students/{id}/available-courses` loại trừ chính xác các khóa học đã ghi danh và chỉ hiển thị khóa học có `end_date >= CURRENT_DATE`. (7) Endpoint POST `/api/v1/enrollments` tự động tạo tài khoản Student khi email chưa tồn tại với role `Student`, sử dụng distributed lock Redis SETNX chống race condition, publish sự kiện Kafka lên topic `enrollment.created`. (8) Endpoint GET `/api/v1/dashboard/enrollment-summary` aggregate chính xác `totalStudents`, `activeCourses`, `upcomingSessions` trong 7 ngày tới, lưu cache Redis với TTL 900 giây, circuit breaker fallback compute từ PostgreSQL khi cache lỗi. (9) 100% Tag ID của giai đoạn (gồm [REQ-007] đến [REQ-011], [REQ-025], [ARC-008], [EXC-004]) được ánh xạ đầy đủ trong mã nguồn và tài liệu. (10) Mọi mã nguồn Java tuân thủ package convention `org.nlh4j.membershiphub.courseservice` và `org.nlh4j.membershiphub.enrollmentservice`. (11) Kafka producer sử dụng `enable.idempotence=true` và `acks=all` đảm bảo chính xác một lần gửi sự kiện. (12) Dashboard cache đáp ứng NFR-001 với độ trễ sub-second cho 10.000 người dùng đồng thời.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->Khởi Tạo Course Service Và Danh Sách Khóa Học<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 1.1: Khởi tạo pom.xml cho course-service và khai báo REST endpoint danh sách
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/course-service/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [REQ-007]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp pom.xml cho module course-service tại đường dẫn `./sources/backend/course-service/pom.xml` kế thừa từ `./sources/backend/pom.xml` thông qua khối `<parent>`. Khai báo `<artifactId>course-service</artifactId>`. Bổ sung đầy đủ dependency Quarkus cần thiết cho chức năng quản lý khóa học: `quarkus-resteasy-reactive-jackson` cho REST endpoint reactive, `quarkus-hibernate-orm-panache` cho ORM với Panache, `quarkus-jdbc-postgresql` cho kết nối database, `quarkus-flyway` cho migration, `quarkus-smallrye-openapi` cho sinh tài liệu OpenAPI, `quarkus-hibernate-validator` cho Bean Validation, `quarkus-messaging-kafka` cho Kafka producer với idempotent config, `quarkus-arc` cho CDI. Thiết lập `<java.version>21</java.version>`, `<maven.compiler.source>21</maven.compiler.source>`, `<maven.compiler.target>21</maven.compiler.target>`. Cấu hình port 8082 trong application.properties và datasource chuyên biệt `membershiphub_course`. Đảm bảo tệp XML hợp lệ, biên dịch thành công thông qua `mvn -f ./sources/backend/course-service/pom.xml compile`. Đồng thời tạo lớp `CourseEntity` tại đường dẫn `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseEntity.java` thuộc package `org.nlh4j.membershiphub.courseservice` ánh xạ bảng `Courses` với các cột `course_id` (UUID PK), `title` (VARCHAR 150), `description` (TEXT), `start_date` (DATE), `end_date` (DATE), `teacher_id` (UUID FK→users), `max_students` (INT default 30), `created_at` (TIMESTAMP), `updated_at` (TIMESTAMP). Tạo lớp `CourseRepository` kế thừa `PanacheRepositoryBase<CourseEntity, UUID>`. Tạo lớp `CourseController` chú thích `@Path("/api/v1/courses")` đăng ký endpoint GET trả về danh sách khóa học với lưới dữ liệu gồm `courseId`, `title`, `startDate`, `endDate`, `teacherName`. Inject `CourseService` và `CourseRepository` thông qua CDI. Toàn bộ mã nguồn Java phải khai báo package `org.nlh4j.membershiphub.courseservice` theo nguyên tắc chuẩn hóa Java package authority đã thiết lập, đảm bảo thư mục vật lý khớp 1:1 với cấu trúc package. Tuân thủ triệt để nguyên tắc dependency isolation: cấm import trực tiếp class từ user-service, center-service.

* **Đặc tả DDL SQL lược đồ cơ sở dữ liệu [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- Bảng Courses đã được tạo tại V1__init_courses.sql; V3 bổ sung index tối ưu
CREATE INDEX IF NOT EXISTS idx_courses_teacher_id ON Courses(teacher_id);
CREATE INDEX IF NOT EXISTS idx_courses_start_end_date ON Courses(start_date, end_date);
```
<!--END_DDL_MIGRATION-->

* **Hợp đồng định tuyến API và sự kiện [REQ-007], [ARC-008]:** <!--START_API_CONTRACT-->
```json
{
  "openapi": "3.0.3",
  "info": { "title": "Course Service API", "version": "1.0.0" },
  "paths": {
    "/api/v1/courses": {
      "get": {
        "summary": "Danh sách khóa học",
        "tags": ["Course"],
        "responses": {
          "200": {
            "description": "Trả về lưới khóa học",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "courseId": { "type": "string", "format": "uuid" },
                      "title": { "type": "string" },
                      "startDate": { "type": "string", "format": "date" },
                      "endDate": { "type": "string", "format": "date" },
                      "teacherName": { "type": "string" }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
```
<!--END_API_CONTRACT-->

* **Bộ xử lý ngoại lệ cục bộ hóa của giai đoạn [EXC-004]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.courseservice;

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(String courseId) {
        super("Không tìm thấy khóa học với mã " + courseId);
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 Nhiệm vụ phụ 1.2: Xây dựng bộ kiểm thử tích hợp cho endpoint danh sách khóa học
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseControllerTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-007], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp kiểm thử tích hợp tại đường dẫn `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseControllerTest.java` thuộc package `org.nlh4j.membershiphub.courseservice`. Sử dụng annotation `@QuarkusTest` kết hợp `@TestHTTPEndpoint(CourseController.class)`. Mock `CourseRepository` bằng `@InjectMock` để trả về danh sách 3 khóa học mẫu với teacher name thuộc role Teacher. Test case 1 (`testListCoursesSuccess` - Happy Path): Gọi GET `/api/v1/courses` xác nhận HTTP 200, payload chứa 3 phần tử với đầy đủ trường `courseId`, `title`, `startDate`, `endDate`, `teacherName`. Test case 2 (`testListCoursesEmpty`): Mock trả về danh sách rỗng, xác nhận HTTP 200 với mảng rỗng. Test case 3 (`testListCoursesDatabaseError`): Mock `CourseRepository.listAll()` ném `SQLException`, xác nhận ExceptionMapper trả về HTTP 500 với mã lỗi `INTERNAL_SERVER_ERROR`. Sử dụng RestAssured validate schema JSON. Đảm bảo test chạy độc lập với test suite của service khác thông qua `@QuarkusTestResource(DatabaseH2Resource.class)` riêng biệt.

#### 📝 Nhiệm vụ phụ 1.3: Review code mới khởi tạo của course-service và phát hành tài liệu kỹ thuật
##### Sub-Agent được phân công: Reviewer
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [REQ-007], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Reviewer thực hiện kiểm tra mã nguồn `CourseController.java` và `CourseEntity.java` để xác nhận: (1) Không có field nào expose password hash hoặc secret key, (2) Sử dụng `@RolesAllowed` annotation cho endpoint nhạy cảm, (3) Sử dụng Java record thay vì mutable DTO cho response payload, (4) Tuân thủ Java package convention `org.nlh4j.membershiphub.courseservice` ở tất cả file Java, (5) Không có raw SQL injection, tất cả truy vấn đi qua JPA hoặc named query. Tạo báo cáo review tại đường dẫn `./sources/docs/review/phase3-day1-course-review.md` liệt kê chi tiết từng issue phát hiện và đề xuất fix cụ thể.

#### 📝 Nhiệm vụ phụ 1.4: Biên soạn tài liệu API contract cho course-service
##### Sub-Agent được phân công: Doc
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/docs/api/course-service-api.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [REQ-007], [ARC-008]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu Markdown tại đường dẫn `./sources/docs/api/course-service-api.md` mô tả endpoint GET `/api/v1/courses` với bảng response schema, HTTP status codes, ví dụ curl request và JSON response mẫu. Tài liệu phải chứa mô tả bằng tiếng Việt, mục lục rõ ràng, sơ đồ luồng dữ liệu Mermaid. Đảm bảo tài liệu chứa mục lục rõ ràng với các phần: Tổng quan service, Endpoint GET, Response schema, Mã lỗi, Ví dụ curl, Sơ đồ Mermaid sequence diagram thể hiện luồng request từ client đến database.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->CRUD Khóa Học, Kiểm Tra Xung Đột Lịch Trình và Phân Công Giáo Viên<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 2.1: Triển khai CRUD khóa học với trigger PostgreSQL kiểm tra xung đột giáo viên
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-008], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp `CourseService` tại đường dẫn `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseService.java` thuộc package `org.nlh4j.membershiphub.courseservice`. Sử dụng annotation `@ApplicationScoped`. Inject `CourseRepository`, `EntityManager` và `ScheduleValidator`. Triển khai các phương thức `createCourse`, `updateCourse`, `deleteCourse` với annotation `@Transactional`. Trong `createCourse`, trước khi persist gọi `scheduleValidator.validateNoConflict(teacherId, startDate, endDate)` thực hiện truy vấn `SELECT COUNT(*) FROM Courses WHERE teacher_id = :teacherId AND (start_date <= :endDate AND end_date >= :startDate)`. Nếu kết quả lớn hơn 0, ném `ScheduleConflictException` được map sang HTTP 409 với mã `SCHEDULE_CONFLICT`. Trong `updateCourse`, bảo đảm cập nhật cả `updated_at` qua `@PreUpdate` callback trên entity. Trong `deleteCourse`, kiểm tra quan hệ khóa ngoại `Enrollments.course_id` trước khi xóa; nếu còn ghi danh, ném `CourseHasEnrollmentsException` trả về lỗi 422. Mã hóa tiêu đề khóa học thông qua hàm `StringUtils.normalizeWhitespace` để chống XSS stored. Áp dụng OWASP A03 injection mitigation bằng cách sử dụng JPA named parameters, cấm nối chuỗi SQL. Đồng thời tạo tệp migration `./sources/backend/course-service/src/main/resources/db/migration/V3__course_module_indexes.sql` chứa trigger PostgreSQL `trg_teacher_schedule_conflict` tự động phát hiện xung đột lịch trình giáo viên bằng cách kiểm tra `EXISTS` trên bảng `Courses` với điều kiện khoảng thời gian giao nhau.

* **Đặc tả DDL SQL lược đồ cơ sở dữ liệu [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- V3__course_module_indexes.sql (bổ sung trigger kiểm tra xung đột)
CREATE OR REPLACE FUNCTION check_teacher_schedule_conflict()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM Courses
        WHERE teacher_id = NEW.teacher_id
          AND course_id <> COALESCE(NEW.course_id, '00000000-0000-0000-0000-000000000000'::uuid)
          AND start_date <= NEW.end_date
          AND end_date >= NEW.start_date
    ) THEN
        RAISE EXCEPTION 'SCHEDULE_CONFLICT: Giáo viên đã có khóa học trong khoảng thời gian này';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_teacher_schedule_conflict
BEFORE INSERT OR UPDATE ON Courses
FOR EACH ROW EXECUTE FUNCTION check_teacher_schedule_conflict();
```
<!--END_DDL_MIGRATION-->

* **Hợp đồng định tuyến API và sự kiện [REQ-008], [ARC-008]:** <!--START_API_CONTRACT-->
```json
{
  "endpoints": {
    "POST /api/v1/courses": {
      "requestBody": {
        "title": "string (max 150)",
        "startDate": "date",
        "endDate": "date",
        "teacherId": "uuid",
        "maxStudents": "integer (default 30)"
      },
      "responses": {
        "201": "Course created",
        "400": "INVALID_COURSE_DATA",
        "409": "SCHEDULE_CONFLICT"
      }
    },
    "PUT /api/v1/courses/{id}": { "responses": { "200": "Updated", "404": "Not found", "409": "SCHEDULE_CONFLICT" } },
    "DELETE /api/v1/courses/{id}": { "responses": { "204": "Deleted", "422": "HAS_ENROLLMENTS" } }
  }
}
```
<!--END_API_CONTRACT-->

* **Bộ xử lý ngoại lệ cục bộ hóa của giai đoạn [EXC-004]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.courseservice;

public class ScheduleConflictException extends RuntimeException {
    private final String teacherId;
    public ScheduleConflictException(String teacherId) {
        super("Khoảng thời gian khóa học bị trùng lặp với khóa học hiện tại của giáo viên " + teacherId);
        this.teacherId = teacherId;
    }
    public String getTeacherId() { return teacherId; }
}
```
<!--END_EXC_HANDLER-->

#### 📝 Nhiệm vụ phụ 2.2: Viết bộ test cho ScheduleValidator và trigger PostgreSQL xung đột
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/ScheduleValidatorTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-008], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp kiểm thử tại đường dẫn `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/ScheduleValidatorTest.java` thuộc package `org.nlh4j.membershiphub.courseservice`. Sử dụng `@QuarkusTest` với database PostgreSQL test container thực sự (Testcontainers) để trigger `trg_teacher_schedule_conflict` hoạt động. Test case 1 (`testScheduleConflictDetected`): Insert khóa học A với teacher X từ 2025-01-01 đến 2025-03-31. Insert khóa học B cùng teacher X từ 2025-02-01 đến 2025-04-30, kỳ vọng ném `ScheduleConflictException` với mã lỗi `SCHEDULE_CONFLICT`. Test case 2 (`testScheduleNoConflictDifferentTeacher`): Insert khóa học B với teacher Y khác, kỳ vọng thành công. Test case 3 (`testScheduleUpdateExtension`): Update khóa học A kéo dài đến 2025-05-31, kỳ vọng thành công vì không có khóa học khác của teacher X trong khoảng mới. Test case 4 (`testValidationEndDateBeforeStartDate`): Validate `EndDate < StartDate`, kỳ vọng ném `CourseValidationException` HTTP 400. Sử dụng AssertJ để verify exception message chứa chuỗi tiếng Việt chính xác.

#### 📝 Nhiệm vụ phụ 2.3: Triển khai phân công giáo viên và publish sự kiện Kafka
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/TeacherAssignmentService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-009], [ARC-008]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp `TeacherAssignmentService` tại đường dẫn `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/TeacherAssignmentService.java` thuộc package `org.nlh4j.membershiphub.courseservice`. Sử dụng annotation `@ApplicationScoped`. Inject `CourseRepository`, `EntityManager` và `KafkaEnrollmentProducer`. Tạo phương thức `assignTeacher(courseId, teacherId, assignedBy)` thực hiện cập nhật `Courses.teacher_id` và lưu lịch sử phân công vào bảng `CourseTeacherHistory` với cấu trúc `history_id UUID PK`, `course_id UUID FK→Courses ON DELETE CASCADE`, `teacher_id UUID FK→Users`, `assigned_by UUID FK→Users`, `assigned_at TIMESTAMP DEFAULT now()`, `revoked_at TIMESTAMP NULL`. Inject `KafkaEnrollmentProducer` để gửi sự kiện JSON tới topic `course.teacher.assigned` với key là courseId, payload gồm `courseId`, `teacherId`, `assignedBy`, `assignedAt`. Method `unassignTeacher(courseId, teacherId)` set `Courses.teacher_id = NULL`, ghi nhận `revoked_at` trong history, gửi sự kiện `course.teacher.unassigned`. Tất cả thao tác phải bọc trong `@Transactional(REQUIRES_NEW)` để đảm bảo Kafka publish rollback-safe. Áp dụng OWASP A01 access control bằng `@RolesAllowed("System Admin")` ở controller layer.

* **Đặc tả DDL SQL lược đồ cơ sở dữ liệu [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- V3__course_module_indexes.sql (bổ sung bảng lịch sử)
CREATE TABLE IF NOT EXISTS CourseTeacherHistory (
    history_id UUID PRIMARY KEY,
    course_id UUID NOT NULL REFERENCES Courses(course_id) ON DELETE CASCADE,
    teacher_id UUID NOT NULL REFERENCES Users(user_id),
    assigned_by UUID NOT NULL REFERENCES Users(user_id),
    assigned_at TIMESTAMP NOT NULL DEFAULT now(),
    revoked_at TIMESTAMP
);
CREATE INDEX idx_course_teacher_history_course ON CourseTeacherHistory(course_id);
CREATE INDEX idx_course_teacher_history_teacher ON CourseTeacherHistory(teacher_id);
```
<!--END_DDL_MIGRATION-->

* **Hợp đồng định tuyến API và sự kiện [REQ-009], [ARC-008]:** <!--START_API_CONTRACT-->
```json
{
  "endpoint": "POST /api/v1/courses/{id}/teachers",
  "requestBody": { "teacherId": "uuid" },
  "responses": { "200": "Assigned", "404": "CourseNotFound", "409": "SCHEDULE_CONFLICT" },
  "kafkaEvent": {
    "topic": "course.teacher.assigned",
    "payload": {
      "courseId": "uuid",
      "teacherId": "uuid",
      "assignedBy": "uuid",
      "assignedAt": "ISO-8601"
    }
  }
}
```
<!--END_API_CONTRACT-->

* **Bộ xử lý ngoại lệ cục bộ hóa của giai đoạn [EXC-004]:** <!--START_EXC_HANDLER-->
```java
// Exception mới cho phân công giáo viên không tồn tại
public class TeacherNotFoundException extends RuntimeException {
    public TeacherNotFoundException(String teacherId) {
        super("Không tìm thấy giáo viên với mã " + teacherId);
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 Nhiệm vụ phụ 2.4: Review code CRUD khóa học và cập nhật tài liệu Database Schema
##### Sub-Agent được phân công: Reviewer
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/ExceptionMappers.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-008], [REQ-009], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Reviewer kiểm tra `CourseService.java` và `TeacherAssignmentService.java` để xác nhận: (1) Không có SQL injection (chỉ dùng named parameters), (2) Kafka producer sử dụng idempotent producer config `enable.idempotence=true`, `acks=all` để chống duplicate event, (3) Transaction boundary đúng cách, (4) Không hardcode giá trị nhạy cảm. Tạo báo cáo review tại đường dẫn `./sources/docs/review/phase3-day2-course-review.md` liệt kê chi tiết từng issue phát hiện và đề xuất fix cụ thể. Đồng thời cập nhật tài liệu `./sources/docs/database/course-module-schema.md` mô tả trigger `trg_teacher_schedule_conflict`, bảng `CourseTeacherHistory`, index mới, kèm sơ đồ ERD Mermaid. Tài liệu phải chứa bảng mapping giữa Tag ID và thành phần database tương ứng.

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->Enrollment Service, Dashboard và Hoàn Thiện Giai Đoạn 3<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 3.1: Triển khai enrollment-service với endpoint duyệt khóa học và ghi danh tự động tạo tài khoản
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-010], [REQ-011], [ARC-008], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp pom.xml cho module enrollment-service tại đường dẫn `./sources/backend/enrollment-service/pom.xml` kế thừa từ `./sources/backend/pom.xml`. Tạo lớp `EnrollmentService` tại đường dẫn `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentService.java` thuộc package `org.nlh4j.membershiphub.enrollmentservice`. Sử dụng annotation `@ApplicationScoped`. Inject `EnrollmentRepository`, `StudentProvisioningService`, `KafkaEnrollmentProducer` và `RedisLockService`. Triển khai `getAvailableCourses(studentId)` thực hiện query `SELECT * FROM Courses WHERE course_id NOT IN (SELECT course_id FROM Enrollments WHERE student_id = :studentId) AND end_date >= CURRENT_DATE ORDER BY start_date ASC LIMIT 50`. Triển khai `enrollStudent(courseId, request)` với logic: (1) Lấy user từ JWT token, nếu email chưa tồn tại trong bảng `Users`, gọi `StudentProvisioningService.createStudent(email, fullName)` để tạo user mới với role `Student` và gửi mật khẩu tạm qua email queue, (2) Insert vào bảng `Enrollments`, (3) Publish sự kiện Kafka topic `enrollment.created` với payload chứa `studentId`, `courseId`, `centerId`, `enrolledAt`. Áp dụng distributed lock bằng Redis SETNX với key `enrollment:lock:{studentId}:{courseId}` TTL 5 giây để chống race condition khi hai request đồng thời. Toàn bộ Java class sử dụng package `org.nlh4j.membershiphub.enrollmentservice`.

* **Hợp đồng định tuyến API và sự kiện [REQ-010], [REQ-011], [ARC-008]:** <!--START_API_CONTRACT-->
```json
{
  "endpoints": {
    "GET /api/v1/students/{id}/available-courses": {
      "response": {
        "type": "array",
        "items": {
          "type": "object",
          "properties": {
            "courseId": { "type": "string", "format": "uuid" },
            "title": { "type": "string" },
            "startDate": { "type": "string", "format": "date" },
            "endDate": { "type": "string", "format": "date" },
            "teacherName": { "type": "string" },
            "availableSeats": { "type": "integer" }
          }
        }
      }
    },
    "POST /api/v1/enrollments": {
      "requestBody": {
        "courseId": "uuid",
        "email": "string (optional, dùng khi auto-provisioning)"
      },
      "responses": {
        "201": "Enrollment created",
        "200": "Already enrolled (duplicate)",
        "422": "STUDENT_PROVISIONING_FAILED"
      },
      "kafkaEvent": {
        "topic": "enrollment.created",
        "payload": {
          "studentId": "uuid",
          "courseId": "uuid",
          "centerId": "uuid",
          "enrolledAt": "ISO-8601"
        }
      }
    }
  }
}
```
<!--END_API_CONTRACT-->

* **Bộ xử lý ngoại lệ cục bộ hóa của giai đoạn [EXC-004]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.enrollmentservice.exception;

public class DuplicateEnrollmentException extends RuntimeException {
    public DuplicateEnrollmentException(String studentId, String courseId) {
        super("Học viên " + studentId + " đã ghi danh vào khóa học " + courseId);
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 Nhiệm vụ phụ 3.2: Viết test cho StudentProvisioningService và DashboardService
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/backend/enrollment-service/src/test/java/org/nlh4j/membershiphub/enrollmentservice/StudentProvisioningServiceTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-011], [REQ-025], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp kiểm thử tại đường dẫn `./sources/backend/enrollment-service/src/test/java/org/nlh4j/membershiphub/enrollmentservice/StudentProvisioningServiceTest.java` thuộc package `org.nlh4j.membershiphub.enrollmentservice`. Test 1 (`testCreateStudentNewEmail`): Mock user repository trả về empty, gọi `createStudent("new@example.com", "Nguyen Van A")`, xác nhận user mới được tạo với role `Student`, mật khẩu tạm được hash bằng bcrypt, email queue được gọi. Test 2 (`testCreateStudentExistingEmail`): Mock user repository trả về existing user, xác nhận method trả về user hiện tại thay vì tạo mới. Test 3 (`testEmailQueueFailureRollback`): Mock email queue ném exception, xác nhận rollback transaction và ném `StudentProvisioningException`. Đồng thời tạo tệp `./sources/backend/enrollment-service/src/test/java/org/nlh4j/membershiphub/enrollmentservice/DashboardServiceTest.java`. Cho `DashboardServiceTest`: Mock Redis cache miss, mock database trả về 150 students, 12 active courses, 5 upcoming sessions, xác nhận response JSON chứa `totalStudents: 150, activeCourses: 12, upcomingSessions: 5`. Test cache hit: Pre-populate Redis, gọi service, xác nhận database KHÔNG được truy vấn (verify bằng `Mockito.verify(repo, never()).count()`).

#### 📝 Nhiệm vụ phụ 3.3: Triển khai DashboardService với Redis cache 15 phút
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/DashboardService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-025], [NFR-001], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp `DashboardService` tại đường dẫn `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/DashboardService.java` thuộc package `org.nlh4j.membershiphub.enrollmentservice`. Sử dụng annotation `@ApplicationScoped`. Inject `DashboardCacheService`, `EntityManager` và `CourseRepository`. Triển khai method `getEnrollmentSummary(centerId)` thực hiện: (1) Kiểm tra Redis key `dashboard:enrollment:{centerId}`, nếu tồn tại trả về cached value, (2) Nếu cache miss, query database: `totalStudents = SELECT COUNT(*) FROM Users u JOIN Enrollments e ON u.user_id = e.student_id WHERE e.course_id IN (SELECT course_id FROM Courses WHERE center_id = :centerId)`, `activeCourses = SELECT COUNT(*) FROM Courses WHERE center_id = :centerId AND end_date >= CURRENT_DATE`, `upcomingSessions = SELECT COUNT(*) FROM CourseSessions WHERE course_id IN (...) AND session_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'`, (3) Lưu kết quả vào Redis với TTL 900 giây, (4) Trả về `DashboardSummaryResponse`. Sử dụng `@CacheResult(cacheName = "dashboard-cache")` của Quarkus cache API kết hợp Redis backend. Implement circuit breaker `@CircuitBreaker(requestVolumeThreshold=4, failureRatio=0.5, delay=10s)` cho trường hợp Redis down, fallback compute trực tiếp từ DB.

* **Đặc tả DDL SQL lược đồ cơ sở dữ liệu [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- View tổng hợp cho dashboard (tối ưu query)
CREATE OR REPLACE VIEW vw_enrollment_summary AS
SELECT
    c.center_id,
    COUNT(DISTINCT e.student_id) AS total_students,
    COUNT(DISTINCT CASE WHEN c.end_date >= CURRENT_DATE THEN c.course_id END) AS active_courses,
    COUNT(DISTINCT CASE WHEN cs.session_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days' THEN cs.session_id END) AS upcoming_sessions
FROM Courses c
LEFT JOIN Enrollments e ON c.course_id = e.course_id
LEFT JOIN CourseSessions cs ON c.course_id = cs.course_id
GROUP BY c.center_id;
```
<!--END_DDL_MIGRATION-->

* **Hợp đồng định tuyến API và sự kiện [REQ-025], [NFR-001]:** <!--START_API_CONTRACT-->
```json
{
  "endpoint": "GET /api/v1/dashboard/enrollment-summary",
  "queryParams": {
    "centerId": "uuid (optional, mặc định lấy tất cả)"
  },
  "response": {
    "type": "object",
    "properties": {
      "totalStudents": { "type": "integer" },
      "activeCourses": { "type": "integer" },
      "upcomingSessions": { "type": "integer" },
      "generatedAt": { "type": "string", "format": "date-time" },
      "cachedUntil": { "type": "string", "format": "date-time" }
    }
  },
  "performance": {
    "cacheTtl": 900,
    "targetLatencyMs": 50
  }
}
```
<!--END_API_CONTRACT-->

* **Bộ xử lý ngoại lệ cục bộ hóa của giai đoạn [EXC-004]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.enrollmentservice.exception;

public class DashboardCacheException extends RuntimeException {
    public DashboardCacheException(Throwable cause) {
        super("Không thể truy cập cache dashboard, hệ thống sẽ tự động fallback về truy vấn trực tiếp", cause);
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 Nhiệm vụ phụ 3.4: Review tổng thể enrollment-service và cập nhật tài liệu API
##### Sub-Agent được phân công: Reviewer
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/exception/ExceptionMappers.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-010], [REQ-011], [REQ-025], [ARC-008], [EXC-004], [NFR-001]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Reviewer kiểm tra toàn bộ enrollment-service để xác nhận: (1) Distributed lock Redis SETNX có TTL hợp lý, (2) Kafka idempotent producer được cấu hình đúng, (3) Dashboard cache TTL đáp ứng NFR-001 sub-second latency, (4) Circuit breaker có fallback an toàn, (5) Tất cả Java code tuân thủ package convention `org.nlh4j.membershiphub.enrollmentservice`. Tạo báo cáo review tại đường dẫn `./sources/docs/review/phase3-day3-enrollment-review.md` liệt kê chi tiết từng issue phát hiện và đề xuất fix cụ thể. Đồng thời cập nhật tài liệu `./sources/docs/api/enrollment-service-api.md` mô tả 3 endpoint (available-courses, enrollments, dashboard) với sơ đồ Mermaid sequence diagram thể hiện luồng auto-provisioning student và Kafka publish. Tài liệu phải có bảng Tag ID mapping và matrix exception handling.