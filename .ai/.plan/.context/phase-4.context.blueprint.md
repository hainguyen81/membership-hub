# Giai Đoạn 4: <!--PHASE_NAME_START-->Phân Phối Nghiệp Vụ Điểm Danh, Thông Báo, Frontend Di Động và Báo Cáo<!--PHASE_NAME_END-->

## 📊 Quản Lý Tài Liệu

| Mục | Chi tiết |
| :--- | :--- |
| **Mã bản thiết kế** | ARCH-20260828112120 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 4 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Phân Phối Nghiệp Vụ Điểm Danh, Thông Báo, Frontend Di Động và Báo Cáo<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn 4 tập trung hoàn thiện các nghiệp vụ cuối cùng của hệ thống membership-hub bao gồm quét QR điểm danh với cơ chế idempotency thông qua ràng buộc UNIQUE composite, quản lý thẻ thành viên và gia hạn, hệ thống thông báo đa kênh tích hợp FCM/APNs và Zalo Group, quản lý chương trình khuyến mãi và thông báo nội bộ, tích hợp Chatbot AI hỗ trợ khách hàng, xây dựng frontend Next.js mobile-first đa vai trò với FCM SDK, hỗ trợ đa ngôn ngữ qua middleware i18n và SEO hreflang, cùng các báo cáo điểm danh CSV. Toàn bộ cơ chế xử lý ngoại lệ mạng, retry queue, idempotency key, drain queue FIFO khi service restart và khôi phục hệ thống sau sự cố cũng được hiện thực hóa trong giai đoạn then chốt này.<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Baseline) |
| **Ngày giờ** | 2026/08/28 11:21:20 |
| **Tác giả** | Kiến trúc sư hệ thống doanh nghiệp (SA Agent) |
| **Phê duyệt** | Đang chờ đánh giá quản trị kỹ thuật |

## 1. Phạm Vi Hoạt Động và Mục Tiêu Của Giai Đoạn

Giai đoạn 4 thuộc dự án membership-hub tập trung xây dựng và hoàn thiện các nghiệp vụ cuối cùng của hệ thống quản lý thành viên đa trung tâm, bao gồm sáu trụ cột kỹ thuật chính được phân bổ từ bảng tổng hợp sản phẩm tổng thể. Trụ cột thứ nhất là module điểm danh QR trong `attendance-service`, xây dựng endpoint POST `/api/v1/attendance/scan` giải mã payload base64 chứa studentID và courseID, xác thực quan hệ ghi danh, tạo bản ghi Attendance với cơ chế idempotency thông qua ràng buộc UNIQUE composite `(student_id, course_id, attendance_date)` đã được thiết lập tại Giai đoạn 1, kèm theo retry queue phía client sử dụng IndexedDB xử lý sự cố mạng theo [EXC-001]. Trụ cột thứ hai là module thẻ thành viên với hai endpoint GET `/api/v1/students/{id}/card` tính toán `remaining_days = validity_days - (CURRENT_DATE - issue_date)` và POST `/api/v1/students/{id}/card/renew` nhận renewalDays từ 1-365 ngày, cập nhật validityDays và phát sự kiện Kafka `card.renewed`.

Trụ cột thứ ba là hệ thống thông báo đa kênh trong `notification-service` lắng nghe các Kafka topic `enrollment.created`, `teacher.assigned`, `attendance.scanned`, `card.renewed` và `system.recovered`, gửi push notification qua FCM/APNs kết hợp Zalo Group webhook, với worker retry tối đa 3 lần theo exponential backoff (1s, 4s, 16s) trước khi đánh dấu `delivered=false` theo [EXC-003], cùng cơ chế drain queue FIFO khi service restart thông qua `KafkaConsumer.seek()` theo [EXC-005]. Trụ cột thứ tư là module khuyến mãi và thông báo nội bộ với validation `discount_percent` trong khoảng 1-100, `title` tối đa 150 ký tự, `content` tối đa 2000 ký tự, hỗ trợ `endDate` tùy chọn (vĩnh viễn nếu null). Trụ cột thứ năm là tích hợp Chatbot AI sử dụng LLM gateway (OpenAI/Vertex AI) với ngưỡng confidence 0.7 để chuyển tiếp nhân viên hỗ trợ.

Trụ cột cuối cùng là frontend Next.js mobile-first tại `./sources/frontend/mobile-app/` với routing động theo role, tích hợp FCM SDK cho push notification, middleware i18n phát hiện locale qua cookie `NEXT_LOCALE` và `Accept-Language` header hỗ trợ ba ngôn ngữ en/vi/es, cấu hình SEO đa ngôn ngữ với hreflang tags và sitemap tự động, kèm báo cáo điểm danh CSV giới hạn dải ngày 30 ngày tại endpoint GET `/api/v1/reports/attendance`. Toàn bộ mã nguồn Java phải tuân thủ quy ước package `org.nlh4j.membershiphub.attendanceservice` và `org.nlh4j.membershiphub.notificationservice`, sử dụng OWASP-compliant PreparedStatement, áp dụng xác thực đầu vào nghiêm ngặt qua Bean Validation, ghi log audit cho mọi thao tác thay đổi trạng thái.

## 2. Phạm Vi Kỹ Thuật Được Phép và Ranh Giới Thư Mục

Danh sách kiểm tra kỹ thuật dưới đây định nghĩa 100% các tệp vật lý được phép khởi tạo trong phạm vi giai đoạn này, mỗi mục đại diện cho một tệp cụ thể kèm Tag ID truy vết:

* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceController.java` - [REQ-012], [REQ-013], [ARC-007]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceService.java` - [REQ-012], [REQ-013], [EXC-001], [EXC-002]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/QrPayloadDecoder.java` - [REQ-012], [ARC-007]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/RetryQueueConsumer.java` - [EXC-001], [EXC-005]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/StudentCardController.java` - [REQ-014], [REQ-015]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/StudentCardService.java` - [REQ-014], [REQ-015]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/ReportController.java` - [REQ-024]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceReportService.java` - [REQ-024]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/NotificationConsumer.java` - [REQ-016], [ARC-008], [EXC-003]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/ZaloGateway.java` - [REQ-016], [ARC-008]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/ChatbotController.java` - [REQ-019]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/ChatbotService.java` - [REQ-019]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/PromotionController.java` - [REQ-017]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/PromotionService.java` - [REQ-017], [EXC-004]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/AnnouncementController.java` - [REQ-018]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/AnnouncementService.java` - [REQ-018], [EXC-004]
* `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceControllerTest.java` - [REQ-012], [REQ-013], [EXC-002]
* `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/StudentCardControllerTest.java` - [REQ-014], [REQ-015]
* `./sources/backend/notification-service/src/test/java/org/nlh4j/membershiphub/notificationservice/NotificationConsumerTest.java` - [REQ-016], [EXC-003]
* `./sources/backend/notification-service/src/test/java/org/nlh4j/membershiphub/notificationservice/ChatbotServiceTest.java` - [REQ-019]
* `./sources/frontend/mobile-app/package.json` - [REQ-020], [ARC-009]
* `./sources/frontend/mobile-app/src/app/[locale]/layout.tsx` - [REQ-020], [REQ-022], [NFR-007]
* `./sources/frontend/mobile-app/src/app/[locale]/student/card/page.tsx` - [REQ-014], [REQ-020]
* `./sources/frontend/mobile-app/src/app/[locale]/student/attendance/page.tsx` - [REQ-012], [REQ-020]
* `./sources/frontend/mobile-app/src/lib/fcm.ts` - [REQ-021]
* `./sources/frontend/mobile-app/middleware.ts` - [REQ-022], [NFR-007]
* `./sources/frontend/mobile-app/next-i18next.config.js` - [REQ-023], [NFR-007]
* `./sources/frontend/mobile-app/src/app/sitemap.ts` - [REQ-023]
* `./sources/docs/architecture/AttendanceServiceBlueprint.md` - [REQ-012], [ARC-007]
* `./sources/docs/api/AttendanceApiContracts.md` - [REQ-012], [REQ-013], [REQ-024]
* `./sources/docs/operations/FrontendMobileManual.md` - [REQ-020], [REQ-021], [REQ-022], [REQ-023]

* **BẮT BUỘC VỀ BỘ KHUNG NỀN TẢNG**: Khi khởi tạo blueprint vòng đời hoạt động (giới hạn cụ thể trong NGÀY 1 của Giai đoạn 4), cần phải tiêm và khai báo rõ ràng các bộ mô tả cấu trúc hạ tầng kho lưu trữ chính trước khi tạo bất kỳ thành phần mã nguồn nghiệp vụ nào. Đối với kiến trúc backend Microservices, phải thực thi định nghĩa đường dẫn bắt buộc của bộ mô tả dự án cha `./sources/backend/pom.xml` và các bộ mô tả module con riêng biệt `./sources/backend/<tên-dịch-vụ>/pom.xml`. Toàn bộ tài sản khung được tạo ra phải ánh xạ chặt chẽ tới mã theo dõi kiến trúc hệ thống `[ARC-000]`.

## 3. Chỉ Thị Chức Năng Chuyên Biệt Cho Các Sub-Agent

* **Coder**: Đóng vai trò Nhà phát triển ứng dụng cao cấp. Chịu trách nhiệm triển khai mã nguồn nghiệp vụ thuần túy trên dịch vụ backend attendance-service, notification-service và frontend mobile-app. Bị cấm viết bộ kiểm thử hoặc bản kê khai hạ tầng.
* **Tester**: Đóng vai trò Trưởng phòng QC/QA. Chuyên về kỹ thuật bộ kiểm thử, xác nhận hợp lệ và cổng gác chất lượng. Chịu trách nhiệm tạo JUnit, kiểm thử tích hợp sử dụng Testcontainers PostgreSQL và REST Assured. Bị cấm sửa đổi mã sản phẩm. Nếu nhiệm vụ phụ liên quan đến phạm vi tích hợp, phải xuất chính xác chuỗi ký tự `INTEGRATION_SCOPE` làm tham số đầu tiên của cặp dấu chấm phẩy.
* **Doc**: Đóng vai trò Technical Writer chính và Kiến trúc sư hệ thống doanh nghiệp. Chuyên biên soạn tài liệu đặc tả kỹ thuật toàn diện, tài liệu tham chiếu API, bản thiết kế kiến trúc dịch vụ và sơ đồ cơ sở dữ liệu. Mọi tệp tài liệu kỹ thuật được tạo ra phải được liệt kê dưới dạng thực thể đường dẫn tệp rõ ràng kết thúc bằng phần mở rộng `.md` và nằm hoàn toàn trong bố cục lưu trữ tập trung `./sources/docs/`.
* **Reviewer**: Chịu trách nhiệm xác minh trình biên dịch, cổng gác phân tích tĩnh và vá lỗi phòng thủ. Chuyên về kiểm toán chất lượng mã, giải quyết lỗi biên dịch, sửa lỗ hổng bảo mật OWASP và xử lý các blocker cổng chất lượng SonarQube.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn (DoD)

Giai đoạn 4 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: (1) Endpoint POST `/api/v1/attendance/scan` giải mã thành công payload base64, xác thực quan hệ ghi danh, tạo bản ghi Attendance và trả về HTTP 200 với payload `{success: true, duplicate: false}`. (2) Khi quét trùng lặp cùng ngày, hệ thống bắt lỗi `PersistenceException` SQLSTATE `23505` từ UNIQUE constraint `uq_attendance_composite`, trả về HTTP 200 với `{success: true, duplicate: true}` thay vì HTTP 409. (3) Endpoint GET `/api/v1/students/{id}/card` tính chính xác `remainingDays = validityDays - (CURRENT_DATE - issueDate)`, đảm bảo không âm. (4) Endpoint POST `/api/v1/students/{id}/card/renew` validate `renewalDays` trong khoảng 1-365, cập nhật `validityDays` và publish sự kiện Kafka topic `card.renewed`. (5) Endpoint GET `/api/v1/reports/attendance` validate `from <= to` và khoảng cách không quá 30 ngày, sinh CSV với 4 cột `StudentName, CourseName, AttendanceDate, Status` và trả về `Content-Type: text/csv`. (6) Notification consumer lắng nghe đầy đủ 5 Kafka channel, retry 3 lần exponential backoff, đánh dấu `delivered=false` khi thất bại. (7) Chatbot service gọi LLM gateway, cache session Redis TTL 30 phút, escalate khi confidence < 0.7. (8) Promotion CRUD validate `discount_percent` 1-100, hỗ trợ `endDate` null coi như vĩnh viễn. (9) Announcement CRUD validate `title` max 150, `content` max 2000, endpoint `/active` chỉ trả về bản ghi trong khoảng hiệu lực. (10) Frontend mobile-app build thành công, FCM SDK đăng ký device token, middleware i18n xử lý 3 locale en/vi/es. (11) SEO sitemap sinh đầy đủ hreflang cho từng locale. (12) 100% Tag ID giai đoạn 4 được ánh xạ đầy đủ trong mã nguồn và tài liệu.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->Khởi Tạo Attendance Service và Xử Lý Quét QR<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 1.1: Khởi tạo pom.xml cho attendance-service và khai báo REST endpoint quét QR
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/attendance-service/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [REQ-012], [ARC-007]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp pom.xml cho module attendance-service tại đường dẫn `./sources/backend/attendance-service/pom.xml` kế thừa từ `./sources/backend/pom.xml` thông qua khối `<parent>`. Khai báo `<artifactId>attendance-service</artifactId>`. Bổ sung đầy đủ dependency Quarkus cần thiết cho chức năng điểm danh QR: `quarkus-resteasy-reactive-jackson` cho REST endpoint reactive, `quarkus-hibernate-orm-panache` cho ORM với Panache, `quarkus-jdbc-postgresql` cho kết nối database, `quarkus-flyway` cho migration, `quarkus-smallrye-openapi` cho sinh tài liệu OpenAPI, `quarkus-hibernate-validator` cho Bean Validation, `quarkus-messaging-kafka` cho Kafka producer và consumer, `quarkus-arc` cho CDI, `quarkus-smallrye-jwt` cho xác thực JWT, `quarkus-redis-client` cho idempotency key với Redis SETNX. Thiết lập `<java.version>21</java.version>`, `<maven.compiler.source>21</maven.compiler.source>`, `<maven.compiler.target>21</maven.compiler.target>`. Cấu hình port 8085 trong application.properties và datasource chuyên biệt `membershiphub_attendance`. Đảm bảo tệp XML hợp lệ, biên dịch thành công thông qua `mvn -f ./sources/backend/attendance-service/pom.xml compile`.

* **Hợp đồng định tuyến API và sự kiện [REQ-012], [REQ-013], [ARC-007]:** <!--START_API_CONTRACT-->
```json
{
  "openapi": "3.0.3",
  "info": { "title": "Attendance Service API", "version": "1.0.0" },
  "paths": {
    "/api/v1/attendance/scan": {
      "post": {
        "summary": "Quét QR điểm danh từ ứng dụng di động",
        "tags": ["Attendance"],
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": {
                "type": "object",
                "required": ["qrPayload"],
                "properties": {
                  "qrPayload": {
                    "type": "string",
                    "description": "Chuỗi base64 chứa studentID và courseID"
                  },
                  "idempotencyKey": {
                    "type": "string",
                    "description": "Khóa idempotency từ client retry queue"
                  }
                }
              }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Điểm danh thành công",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "success": { "type": "boolean" },
                    "duplicate": { "type": "boolean" },
                    "attendanceId": { "type": "string", "format": "uuid" },
                    "recordedAt": { "type": "string", "format": "date-time" }
                  }
                }
              }
            }
          },
          "400": { "description": "QR payload không hợp lệ" },
          "403": { "description": "Sinh viên chưa ghi danh khóa học" }
        }
      }
    }
  }
}
```
<!--END_API_CONTRACT-->

* **Bộ xử lý ngoại lệ cục bộ hóa của giai đoạn [EXC-002]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.attendanceservice.exception;

public class DuplicateAttendanceException extends RuntimeException {
    public DuplicateAttendanceException(String studentId, String courseId) {
        super("Điểm danh đã tồn tại cho sinh viên " + studentId + " trong khóa học " + courseId);
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 Nhiệm vụ phụ 1.2: Triển khai AttendanceController và QrPayloadDecoder
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-012], [REQ-013], [ARC-007], [EXC-002]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp `AttendanceController` tại đường dẫn `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceController.java` thuộc package `org.nlh4j.membershiphub.attendanceservice`. Sử dụng annotation `@Path("/api/v1/attendance")` của JAX-RS. Inject `AttendanceService` và `QrPayloadDecoder` thông qua CDI. Endpoint POST `/scan` nhận `ScanRequest` với trường `qrPayload` (chuỗi base64) và `idempotencyKey` (UUID tùy chọn từ client retry queue), gọi `qrPayloadDecoder.decode()` để trích xuất cặp `(studentId, courseId)`, sau đó ủy quyền cho `AttendanceService.recordAttendance()`. Trả về `Response` với mã 200 và JSON gồm `success`, `duplicate`, `attendanceId`, `recordedAt`. Bắt `PersistenceException` với SQLSTATE `23505` và tên constraint `uq_attendance_composite`, chuyển thành response thành công với cờ `duplicate=true` theo [EXC-002]. Gắn Tag ID trong comment Javadoc. Áp dụng OWASP A03 injection mitigation bằng cách sử dụng JPA named parameters. Tích hợp JWT filter chain đã cấu hình tại Giai đoạn 1 thông qua annotation `@Authenticated`.

* **Bộ xử lý ngoại lệ cục bộ hóa của giai đoạn [EXC-002]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.attendanceservice;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.exception.ConstraintViolationException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class DuplicateAttendanceExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    private static final Logger LOG = Logger.getLogger(DuplicateAttendanceExceptionMapper.class.getName());
    
    @Override
    public Response toResponse(ConstraintViolationException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("uq_attendance_composite")) {
            LOG.log(Level.INFO, "Duplicate attendance detected: {0}", message);
            return Response.ok()
                    .entity("{\"success\":true,\"duplicate\":true,\"message\":\"Attendance already recorded\"}")
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"success\":false,\"message\":\"Database integrity violation\"}")
                .build();
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 Nhiệm vụ phụ 1.3: Xây dựng bộ kiểm thử đơn vị cho endpoint điểm danh
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceControllerTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-012], [REQ-013], [EXC-002]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp kiểm thử tại đường dẫn `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceControllerTest.java` thuộc package `org.nlh4j.membershiphub.attendanceservice`. Sử dụng annotation `@QuarkusTest` để khởi tạo context. Mock `AttendanceService` bằng `@InjectMock`. Test case 1 (`testScanAttendanceSuccess`): Gửi POST với QR payload hợp lệ `eyJzdHVkZW50SWQiOiJ1dWlkLTEiLCJjb3Vyc2VJZCI6InV1aWQtMiJ9` (base64 của JSON `{"studentId":"uuid-1","courseId":"uuid-2"}`), kỳ vọng HTTP 200 và body có `success=true, duplicate=false`. Test case 2 (`testDuplicateScanReturnsIdempotent`): Gửi 2 lần cùng QR trong cùng ngày, mock service throw `PersistenceException` với SQLSTATE 23505 ở lần thứ hai, kỳ vọng response thứ hai có `success=true, duplicate=true`. Test case 3 (`testInvalidQrPayloadReturns400`): Gửi chuỗi base64 không phải JSON hợp lệ, kỳ vọng HTTP 400 với mã lỗi `ATTENDANCE_QR_INVALID`. Sử dụng RestAssured validate JSON schema. Gắn Tag ID trong comment test class.

#### 📝 Nhiệm vụ phụ 1.4: Biên soạn tài liệu kiến trúc cho attendance-service
##### Sub-Agent được phân công: Doc
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/docs/architecture/AttendanceServiceBlueprint.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-012], [ARC-007], [EXC-001], [EXC-002]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu Markdown tại đường dẫn `./sources/docs/architecture/AttendanceServiceBlueprint.md` mô tả kiến trúc tổng thể của `attendance-service`. Nội dung bao gồm: sơ đồ luồng quét QR từ mobile app → controller → service → database → Kafka publisher sử dụng Mermaid sequence diagram; mô tả chi tiết cơ chế idempotency thông qua UNIQUE constraint `uq_attendance_composite(student_id, course_id, attendance_date)` đã thiết lập tại Giai đoạn 1; chiến lược retry queue phía client sử dụng IndexedDB xử lý sự cố mạng theo [EXC-001]; mô tả cách xử lý ngoại lệ [EXC-002] khi quét trùng lặp. Tài liệu sử dụng tiếng Việt cho phần mô tả, giữ nguyên tiếng Anh cho thuật ngữ kỹ thuật và Tag ID. Có mục lục rõ ràng với các phần: Tổng quan service, Kiến trúc thành phần, Luồng quét QR Happy Path, Luồng quét QR Duplicate, Cơ chế Retry Queue, Exception Handling Matrix, Sơ đồ Mermaid. Gắn Tag ID trong tiêu đề mục liên quan.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->Quản Lý Thẻ Thành Viên và Báo Cáo CSV<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 2.1: Triển khai StudentCardController và StudentCardService
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/StudentCardController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-014], [REQ-015]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp `StudentCardController` tại đường dẫn `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/StudentCardController.java` thuộc package `org.nlh4j.membershiphub.attendanceservice`. Sử dụng annotation `@Path("/api/v1/students")`. Inject `StudentCardService` thông qua CDI. Endpoint GET `/{id}/card` truy vấn bảng `student_cards` theo `studentId`, tính `remainingDays = validityDays - (CURRENT_DATE - issueDate)`, đảm bảo không âm bằng hàm `Math.max(0, calculated)`, trả về JSON với `cardId`, `issueDate`, `validityDays`, `usedDays`, `remainingDays`. Endpoint POST `/{id}/card/renew` nhận body `RenewRequest` gồm `renewalDays` (int) và `paymentReference` (string), validate `renewalDays` trong khoảng 1-365, cập nhật `validityDays = validityDays + renewalDays`, publish sự kiện lên Kafka topic `card.renewed`. Nếu không tìm thấy `StudentCard` cho student, tạo bản ghi mới với `validityDays = renewalDays` và `issueDate = CURRENT_DATE` thay vì ném lỗi 404. Gắn Tag ID `[REQ-014]`, `[REQ-015]` trong Javadoc.

* **Hợp đồng định tuyến API và sự kiện [REQ-014], [REQ-015]:** <!--START_API_CONTRACT-->
```json
{
  "endpoints": [
    {
      "method": "GET",
      "path": "/api/v1/students/{id}/card",
      "summary": "Truy xuất thông tin thẻ thành viên",
      "responses": {
        "200": {
          "description": "Trả về thông tin thẻ",
          "content": {
            "application/json": {
              "schema": {
                "type": "object",
                "properties": {
                  "cardId": { "type": "string", "format": "uuid" },
                  "issueDate": { "type": "string", "format": "date" },
                  "validityDays": { "type": "integer" },
                  "usedDays": { "type": "integer" },
                  "remainingDays": { "type": "integer" }
                }
              }
            }
          }
        }
      }
    },
    {
      "method": "POST",
      "path": "/api/v1/students/{id}/card/renew",
      "summary": "Gia hạn thẻ thành viên",
      "requestBody": {
        "required": true,
        "content": {
          "application/json": {
            "schema": {
              "type": "object",
              "required": ["renewalDays", "paymentReference"],
              "properties": {
                "renewalDays": { "type": "integer", "minimum": 1, "maximum": 365 },
                "paymentReference": { "type": "string" }
              }
            }
          }
        }
      }
    }
  ]
}
```
<!--END_API_CONTRACT-->

#### 📝 Nhiệm vụ phụ 2.2: Xây dựng ReportController và AttendanceReportService cho báo cáo CSV
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/ReportController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-024]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp `ReportController` tại đường dẫn `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/ReportController.java` thuộc package `org.nlh4j.membershiphub.attendanceservice`. Sử dụng annotation `@Path("/api/v1/reports")`. Inject `AttendanceReportService` thông qua CDI. Endpoint GET `/attendance` nhận query params `centerId` (UUID required), `from` (date YYYY-MM-DD required), `to` (date YYYY-MM-DD required). Validate `from <= to` và khoảng cách giữa `from` và `to` không quá 30 ngày, nếu vi phạm ném `ReportDateRangeExceededException` trả về HTTP 400 với mã `REPORT_RANGE_EXCEEDED`. Sử dụng `EntityManager.createNativeQuery()` với named parameters để truy vấn `SELECT u.full_name, c.title, a.attendance_date, 'Present' FROM attendance a JOIN users u ON a.student_id = u.user_id JOIN courses c ON a.course_id = c.course_id WHERE c.center_id = :centerId AND a.attendance_date BETWEEN :from AND :to`. Sinh luồng CSV sử dụng Apache Commons CSV với header `StudentName,CourseName,AttendanceDate,Status`. Trả về response với `Content-Type: text/csv; charset=utf-8` và `Content-Disposition: attachment; filename="attendance-report-{centerId}.csv"`. Gắn Tag ID `[REQ-024]` trong Javadoc.

* **Bộ xử lý ngoại lệ cục bộ hóa của giai đoạn [EXC-004]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.attendanceservice.exception;

public class ReportDateRangeExceededException extends RuntimeException {
    public ReportDateRangeExceededException(long daysBetween) {
        super("Khoảng ngày báo cáo vượt quá giới hạn 30 ngày. Khoảng hiện tại: " + daysBetween + " ngày");
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 Nhiệm vụ phụ 2.3: Kiểm thử tích hợp cho module thẻ thành viên và báo cáo
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cận mục tiêu:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/StudentCardControllerTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-014], [REQ-015], [REQ-024]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp kiểm thử tại đường dẫn `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/StudentCardControllerTest.java` thuộc package `org.nlh4j.membershiphub.attendanceservice`. Sử dụng `@QuarkusTest` với Testcontainers PostgreSQL. Test case 1 (`testGetCardReturnsRemainingDays`): Insert bản ghi `student_cards` với `validityDays=90, issueDate=today-14`, gọi GET endpoint, kỳ vọng response có `usedDays=14, remainingDays=76`. Test case 2 (`testRenewCardValidatesRange`): Gửi POST với `renewalDays=400`, kỳ vọng HTTP 400 với mã lỗi `CARD_RENEWAL_INVALID_RANGE`. Test case 3 (`testRenewCardUpdatesValidity`): Gửi `renewalDays=30` hợp lệ, kỳ vọng `validityDays` được tăng đúng 30. Test case 4 (`testAttendanceReportCsv`): Insert 5 bản ghi attendance, gọi GET `/reports/attendance?centerId=...&from=2024-01-01&to=2024-01-31`, kỳ vọng response có `Content-Type: text/csv` và body chứa header `StudentName,CourseName,AttendanceDate,Status`. Test case 5 (`testAttendanceReportRangeExceeded`): Gửi `from=2024-01-01&to=2024-03-01` (60 ngày), kỳ vọng HTTP 400 với mã `REPORT_RANGE_EXCEEDED`.

#### 📝 Nhiệm vụ phụ 2.4: Biên soạn tài liệu API cho attendance-service
##### Sub-Agent được phân công: Doc
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/docs/api/AttendanceApiContracts.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-012], [REQ-013], [REQ-014], [REQ-015], [REQ-024]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu Markdown tại đường dẫn `./sources/docs/api/AttendanceApiContracts.md` mô tả đầy đủ các endpoint của attendance-service. Nội dung bao gồm: (1) Endpoint POST `/api/v1/attendance/scan` với request schema, response schema, validation rules, error codes `ATTENDANCE_QR_INVALID`, `ATTENDANCE_NOT_ENROLLED`. (2) Endpoint GET `/api/v1/students/{id}/card` với response schema và giải thích công thức tính `remainingDays = validityDays - (CURRENT_DATE - issueDate)`. (3) Endpoint POST `/api/v1/students/{id}/card/renew` với validation `renewalDays` 1-365 và luồng xử lý khi sinh viên chưa có thẻ. (4) Endpoint GET `/api/v1/reports/attendance` với query params, validation rules `from <= to` và `to - from <= 30 days`, định dạng CSV output với 4 cột `StudentName, CourseName, AttendanceDate, Status` kèm ví dụ thực tế 3 dòng dữ liệu. Tài liệu sử dụng tiếng Việt cho mô tả, giữ nguyên tiếng Anh cho schema và Tag ID. Có mục lục rõ ràng và bảng Tag ID mapping.

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->Notification Service, Chatbot AI và Kafka Consumer<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 3.1: Triển khai NotificationConsumer lắng nghe Kafka đa kênh
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/NotificationConsumer.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-016], [ARC-008], [EXC-003], [EXC-005]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp `NotificationConsumer` tại đường dẫn `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/NotificationConsumer.java` thuộc package `org.nlh4j.membershiphub.notificationservice`. Sử dụng annotation `@ApplicationScoped` và `@KafkaListener` của Quarkus Reactive Messaging lắng nghe các channel `enrollment-created`, `teacher-assigned`, `attendance-scanned`, `card-renewed`, `system-recovered` với group id `notification-service-group`. Với mỗi message nhận được, thực hiện: (1) Parse JSON payload thành `NotificationEvent` object, (2) Persist bản ghi `Notification` vào database với `delivered=false`, (3) Gọi `FcmGateway.sendPushNotification()` để gửi push notification tới mobile app, (4) Gọi `ZaloGateway.postToGroup()` để đăng tin nhắn vào group chat. Triển khai cơ chế retry với annotation `@Retry(maxRetries=3, delay=1, delayUnit=ChronoUnit.SECONDS, jitter=0, retryOn=Exception.class)` áp dụng exponential backoff thông qua custom `Backoff` handler (1s, 4s, 16s). Nếu vẫn thất bại sau 3 lần, cập nhật `delivered=false` và ghi log lỗi. Implement method `drainPendingMessages()` sử dụng `KafkaConsumer.seek()` để xử lý FIFO khi service restart theo [EXC-005], phát sự kiện `system.recovered` lên topic để thông báo tới user bị ảnh hưởng. Gắn Tag ID trong Javadoc.

* **Hợp đồng định tuyến API và sự kiện [REQ-016], [ARC-008], [EXC-003]:** <!--START_API_CONTRACT-->
```json
{
  "kafka_channels": [
    {"name": "enrollment-created", "partitions": 3, "replication": 3},
    {"name": "teacher-assigned", "partitions": 3, "replication": 3},
    {"name": "attendance-scanned", "partitions": 6, "replication": 3},
    {"name": "card-renewed", "partitions": 3, "replication": 3},
    {"name": "system-recovered", "partitions": 1, "replication": 3}
  ],
  "retry_policy": {
    "max_attempts": 3,
    "backoff_strategy": "exponential",
    "delays_ms": [1000, 4000, 16000]
  }
}
```
<!--END_API_CONTRACT-->

#### 📝 Nhiệm vụ phụ 3.2: Triển khai ZaloGateway và ChatbotService
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/ZaloGateway.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-016], [REQ-019]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp `ZaloGateway` tại đường dẫn `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/ZaloGateway.java` thuộc package `org.nlh4j.membershiphub.notificationservice`. Inject `Vertx WebClient` để gọi Zalo OA API endpoint `https://openapi.zalo.me/v2.0/oa/message/cs`. Method `postToGroup(groupId, message)` gửi POST request với access token từ `application.properties` (`zalo.oa.access-token`). Sử dụng `@CircuitBreaker` của Resilience4j với `failureRateThreshold=50, waitDurationInOpenState=30s` để bảo vệ khi Zalo API down. Đồng thời tạo lớp `ChatbotService` tại đường dẫn `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/ChatbotService.java`. Sử dụng Vertx WebClient gọi OpenAI API endpoint `https://api.openai.com/v1/chat/completions` với model `gpt-4o-mini`. Method `processMessage(userId, sessionId, inputText)` kiểm tra session timeout 30 phút qua Redis key `chatbot:session:{sessionId}`, tạo prompt với context người dùng (role, centerId), gọi LLM API. Nếu confidence score dưới ngưỡng 0.7, tạo bản ghi `SupportEscalation` và gửi email thông báo. Cache session history trong Redis với TTL 30 phút. Gắn Tag ID `[REQ-019]` trong Javadoc.

* **Hợp đồng định tuyến API và sự kiện [REQ-019]:** <!--START_API_CONTRACT-->
```json
{
  "endpoint": "POST /api/v1/chatbot/message",
  "request_body": {
    "sessionId": "string-optional",
    "message": "string-required"
  },
  "response_body": {
    "sessionId": "string",
    "reply": "string",
    "confidence": 0.85,
    "escalated": false
  },
  "llm_config": {
    "model": "gpt-4o-mini",
    "max_tokens": 500,
    "temperature": 0.7
  },
  "escalation_threshold": 0.7
}
```
<!--END_API_CONTRACT-->

#### 📝 Nhiệm vụ phụ 3.3: Kiểm thử tích hợp cho NotificationConsumer và ChatbotService
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/backend/notification-service/src/test/java/org/nlh4j/membershiphub/notificationservice/NotificationConsumerTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-016], [REQ-019], [EXC-003]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp kiểm thử tại đường dẫn `./sources/backend/notification-service/src/test/java/org/nlh4j/membershiphub/notificationservice/NotificationConsumerTest.java` thuộc package `org.nlh4j.membershiphub.notificationservice`. Sử dụng `@QuarkusTest` với `@QuarkusTestResource` và Kafka testcontainer. Test case 1 (`testConsumeEnrollmentEvent`): Gửi message tới topic `enrollment-created` với payload `{"studentId":"uuid-1","courseId":"uuid-2","enrolledAt":"2024-01-15T08:00:00Z"}`, kỳ vọng notification được persist vào database với `delivered=true` và push FCM được gọi. Test case 2 (`testRetryOnFcmFailure`): Mock `FcmGateway` ném exception ở 2 lần đầu, thành công ở lần 3, kỳ vọng notification vẫn `delivered=true` sau lần retry thứ 3. Test case 3 (`testFcmFailureAfterMaxRetries`): Mock `FcmGateway` luôn ném exception, kỳ vọng `delivered=false` sau 3 lần retry. Test case 4 (`testChatbotLowConfidenceEscalates`): Mock OpenAI trả về confidence 0.5, kỳ vọng `SupportEscalation` được tạo. Gắn Tag ID trong comment test class.

#### 📝 Nhiệm vụ phụ 3.4: Đánh giá mã và tối ưu hóa notification-service
##### Sub-Agent được phân công: Reviewer
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/NotificationConsumer.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-016], [ARC-008], [EXC-003], [EXC-005]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Reviewer kiểm tra mã nguồn `NotificationConsumer`, `ZaloGateway`, `ChatbotService` để phát hiện các vấn đề tiềm ẩn: (1) Rò rỉ tài nguyên khi retry - đề xuất wrap FCM call trong try-with-resources, set timeout 5s cho mỗi lần gửi. (2) Điều kiện race condition khi xử lý message đồng thời từ Kafka - đề xuất sử dụng UNIQUE constraint trên cột `message_id` của bảng notifications và handle `DuplicateKeyException`. (3) Hiệu suất khi batch process - đề xuất sử dụng `@Blocking` annotation cho method consumer để tránh block event loop. (4) Memory leak trong Chatbot Redis session storage - đề xuất set EXPIRE 30 phút cho mỗi session key. (5) Xác nhận cơ chế idempotency khi xử lý message trùng lặp từ Kafka. Tạo báo cáo review tại đường dẫn `./sources/docs/review/phase4-day3-notification-review.md` liệt kê chi tiết từng issue phát hiện kèm severity và đề xuất fix cụ thể. Gắn Tag ID trong báo cáo.

### 🌤️ NGÀY 4: <!--DAY_HEADER_START-->Quản Lý Khuyến Mãi, Thông Báo Nội Bộ và Khởi Tạo Frontend<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 4.1: Triển khai PromotionController và PromotionService
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/PromotionController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-017], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp `PromotionController` tại đường dẫn `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/PromotionController.java` thuộc package `org.nlh4j.membershiphub.notificationservice`. Sử dụng annotation `@Path("/api/v1/promotions")`. Inject `PromotionService` thông qua CDI. Triển khai đầy đủ CRUD: (1) `POST /` tạo mới với body `PromotionRequest` gồm `name` (max 100), `description` (max 500), `code` (unique), `discountPercent` (1-100), `startDate` (optional), `endDate` (optional). Validate `discountPercent` ngoài 1-100 ném `InvalidDiscountException` trả về HTTP 400 với mã `PROMO_DISCOUNT_OUT_OF_RANGE`. Validate `code` đã tồn tại ném `DuplicatePromoCodeException` trả về HTTP 409 với mã `PROMO_CODE_DUPLICATE`. Validate `startDate <= endDate` (nếu cả hai được cung cấp). Nếu `endDate` là null, coi khuyến mãi là vĩnh viễn. (2) `GET /` hỗ trợ filter theo `centerId` và `activeOn` (YYYY-MM-DD), chỉ trả về khuyến mãi đang trong khoảng hiệu lực. (3) `PUT /{id}` cập nhật. (4) `DELETE /{id}` xóa mềm bằng cách set `is_active=false`. Gắn Tag ID `[REQ-017]`, `[EXC-004]` trong Javadoc.

* **Bộ xử lý ngoại lệ cục bộ hóa của giai đoạn [EXC-004]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.notificationservice.exception;

public class InvalidDiscountException extends RuntimeException {
    public InvalidDiscountException(int discount) {
        super("Phần trăm giảm giá phải nằm trong khoảng 1-100. Giá trị cung cấp: " + discount);
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 Nhiệm vụ phụ 4.2: Triển khai AnnouncementController và AnnouncementService
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/AnnouncementController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-018], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp `AnnouncementController` tại đường dẫn `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/AnnouncementController.java` thuộc package `org.nlh4j.membershiphub.notificationservice`. Sử dụng annotation `@Path("/api/v1/announcements")`. Inject `AnnouncementService` thông qua CDI. Triển khai đầy đủ CRUD với validation: `title` max 150 ký tự (`ANNOUNCEMENT_TITLE_TOO_LONG`), `content` max 2000 ký tự (`ANNOUNCEMENT_CONTENT_TOO_LONG`). Hỗ trợ `startDate` và `endDate` tùy chọn; thông báo tự động ẩn khi quá `endDate`. Endpoint GET `/active` chỉ trả về thông báo đang trong khoảng hiệu lực thông qua query `SELECT * FROM announcements WHERE (start_date IS NULL OR start_date <= CURRENT_DATE) AND (end_date IS NULL OR end_date >= CURRENT_DATE)`. Gắn Tag ID `[REQ-018]`, `[EXC-004]` trong Javadoc.

#### 📝 Nhiệm vụ phụ 4.3: Khởi tạo frontend mobile-app package.json và layout chính
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/frontend/mobile-app/package.json`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-020], [ARC-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp `package.json` cho ứng dụng Next.js mobile-first tại đường dẫn `./sources/frontend/mobile-app/package.json`. Khai báo `name: "mobile-app"`, `version: "1.0.0"`, `private: true`. Cấu hình scripts: `dev: "next dev"`, `build: "next build"`, `start: "next start"`, `lint: "next lint"`, `type-check: "tsc --noEmit"`. Thêm dependencies: `next@14.2.5`, `react@18.3.1`, `react-dom@18.3.1`, `next-intl@3.20.0`, `firebase@10.13.0`, `axios@1.7.4`, `tailwindcss@3.4.10`, `html5-qrcode@2.3.8`, `idb@8.0.0` (IndexedDB wrapper). Thêm devDependencies: `typescript@5.5.4`, `@types/react@18.3.3`, `@types/node@20.14.10`, `eslint@8.57.0`, `prettier@3.3.3`, `vitest@2.0.5`, `@testing-library/react@16.0.0`. Đồng thời tạo file `[locale]/layout.tsx` tại `./sources/frontend/mobile-app/src/app/[locale]/layout.tsx` sử dụng `NextIntlClientProvider` để bọc toàn bộ ứng dụng, thiết lập font responsive với Tailwind CSS, navigation menu động dựa trên role từ JWT token. Gắn Tag ID `[REQ-020]`, `[ARC-009]` trong comment header.

#### 📝 Nhiệm vụ phụ 4.4: Kiểm thử tích hợp cho Promotion và Announcement
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/backend/notification-service/src/test/java/org/nlh4j/membershiphub/notificationservice/PromotionServiceIntegrationTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-017], [REQ-018], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp kiểm thử tích hợp tại đường dẫn `./sources/backend/notification-service/src/test/java/org/nlh4j/membershiphub/notificationservice/PromotionServiceIntegrationTest.java` thuộc package `org.nlh4j.membershiphub.notificationservice`. Sử dụng `@QuarkusTest` với PostgreSQL testcontainer. Test case 1 (`testCreatePromotionWithDuplicateCode`): Tạo promotion với `code=PROMO001` thành công, tạo lại với cùng code, kỳ vọng HTTP 409 với mã lỗi `PROMO_CODE_DUPLICATE`. Test case 2 (`testCreatePromotionWithInvalidDiscount`): Gửi `discountPercent=150`, kỳ vọng HTTP 400 với mã lỗi `PROMO_DISCOUNT_OUT_OF_RANGE`. Test case 3 (`testGetActiveAnnouncementsExcludesExpired`): Tạo 2 announcement, 1 đã hết hạn (`endDate=today-1`), gọi GET `/active`, kỳ vọng chỉ trả về 1 bản ghi còn hiệu lực. Test case 4 (`testPromotionEndDateOptional`): Tạo promotion không có `endDate`, kỳ vọng lưu thành công và GET trả về promotion đó. Test case 5 (`testAnnouncementContentTooLong`): Gửi `content` với 2001 ký tự, kỳ vọng HTTP 400 với mã lỗi `ANNOUNCEMENT_CONTENT_TOO_LONG`.

### 🌤️ NGÀY 5: <!--DAY_HEADER_START-->FCM SDK, Middleware i18n và SEO Đa Ngôn Ngữ<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 5.1: Tích hợp FCM SDK và xử lý push notification
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/frontend/mobile-app/src/lib/fcm.ts`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-021]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo module TypeScript tại đường dẫn `./sources/frontend/mobile-app/src/lib/fcm.ts`. Sử dụng `firebase/app` và `firebase/messaging` để khởi tạo Firebase app với config từ biến môi trường `NEXT_PUBLIC_FIREBASE_API_KEY`, `NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN`, `NEXT_PUBLIC_FIREBASE_PROJECT_ID`, `NEXT_PUBLIC_FIREBASE_SENDER_ID`, `NEXT_PUBLIC_FIREBASE_APP_ID`, `NEXT_PUBLIC_FIREBASE_VAPID_KEY`. Implement hàm `requestNotificationPermission()` sử dụng `Notification.requestPermission()` API của browser, gọi `getToken(messaging, { vapidKey })` để lấy FCM token. Implement hàm `registerDeviceToken(token, platform)` gọi API POST `/api/v1/users/me/devices` với payload `{token, platform}` và header `Authorization: Bearer ${jwt}`. Implement hàm `onMessageListener()` sử dụng `onMessage(messaging, callback)` xử lý foreground message và deep-link tới route tương ứng dựa trên `notification.data.route`. Sử dụng TypeScript strict mode, khai báo interface cho `NotificationPayload`. Gắn Tag ID `[REQ-021]` trong comment JSDoc.

#### 📝 Nhiệm vụ phụ 5.2: Triển khai middleware i18n và phát hiện ngôn ngữ
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/frontend/mobile-app/middleware.ts`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-022], [NFR-007]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo Next.js middleware tại đường dẫn `./sources/frontend/mobile-app/middleware.ts`. Sử dụng `createMiddleware` từ `next-intl/middleware` với cấu hình: `locales: ["en", "vi", "es"]`, `defaultLocale: "en"`, `localePrefix: "always"`, `localeDetection: true`. Middleware đọc cookie `NEXT_LOCALE` trước tiên, nếu không có thì đọc `Accept-Language` header để phát hiện ngôn ngữ ưa thích. Nếu URL không chứa locale prefix, redirect sang URL có prefix phù hợp. Export `config.matcher` với pattern `["/((?!api|_next|.*\\..*).*)"]` để áp dụng cho tất cả route ngoại trừ API và static assets. Gắn Tag ID `[REQ-022]`, `[NFR-007]` trong comment header.

* **Hợp đồng định tuyến API và sự kiện [REQ-022], [NFR-007]:** <!--START_API_CONTRACT-->
```json
{
  "locale_resolution_priority": [
    "1. Cookie NEXT_LOCALE",
    "2. Accept-Language header",
    "3. defaultLocale (en)"
  ],
  "supported_locales": ["en", "vi", "es"],
  "middleware_config": {
    "locales": ["en", "vi", "es"],
    "defaultLocale": "en",
    "localePrefix": "always",
    "localeDetection": true
  }
}
```
<!--END_API_CONTRACT-->

#### 📝 Nhiệm vụ phụ 5.3: Cấu hình SEO đa ngôn ngữ và sitemap tự động
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/frontend/mobile-app/next-i18next.config.js`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-023], [NFR-007]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp `next-i18next.config.js` tại đường dẫn `./sources/frontend/mobile-app/next-i18next.config.js` cấu hình i18n với `defaultLocale: "en"`, `locales: ["en", "vi", "es"]`, `localeDetection: false` (đã xử lý ở middleware). Tạo file `src/app/sitemap.ts` tại đường dẫn `./sources/frontend/mobile-app/src/app/sitemap.ts` sử dụng Next.js `MetadataRoute.Sitemap` sinh sitemap đa locale với hreflang annotations cho từng trang. Mỗi page layout sử dụng `generateMetadata` để sinh `<html lang="...">`, `<link rel="alternate" hreflang="en|vi|es" href="..." />`, title và description động theo locale. Sitemap phải bao gồm tất cả 3 biến thể locale cho mỗi route (`""`, `/courses`, `/centers`, `/about`) với `alternates.languages` map đầy đủ. Gắn Tag ID `[REQ-023]`, `[NFR-007]` trong comment header.

#### 📝 Nhiệm vụ phụ 5.4: Kiểm thử E2E cho frontend mobile
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/frontend/mobile-app/e2e/locale-and-notification.spec.ts
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-020], [REQ-021], [REQ-022], [REQ-023]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp kiểm thử E2E tại đường dẫn `./sources/frontend/mobile-app/e2e/locale-and-notification.spec.ts` sử dụng Playwright. Test case 1 (`testLocaleDetectionFromAcceptLanguage`): Xóa cookie `NEXT_LOCALE`, set header `Accept-Language: vi-VN`, truy cập `/`, kỳ vọng URL redirect về `/vi`. Test case 2 (`testHreflangTagsPresent`): Truy cập `/en/courses`, kỳ vọng response HTML chứa `<link rel="alternate" hreflang="vi" href=".../vi/courses">` và tương tự cho `es`. Test case 3 (`testFcmTokenRegistration`): Mock `requestNotificationPermission` trả về token giả, kỳ vọng API `/api/v1/users/me/devices` được gọi với payload đúng. Test case 4 (`testSitemapContainsAllLocales`): Truy cập `/sitemap.xml`, kỳ vọng response chứa đủ URL cho cả 3 locale en/vi/es. Gắn Tag ID trong comment test file.

### 🌤️ NGÀY 6: <!--DAY_HEADER_START-->Giao Diện Sinh Viên, QR Scanner và Hoàn Thiện Frontend<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 6.1: Xây dựng trang thẻ thành viên cho sinh viên
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/frontend/mobile-app/src/app/[locale]/student/card/page.tsx`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-014], [REQ-020]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo trang Next.js client component tại đường dẫn `./sources/frontend/mobile-app/src/app/[locale]/student/card/page.tsx` sử dụng directive `"use client"`. Sử dụng `useEffect` và `useState` để fetch API GET `/api/v1/students/{id}/card` với header `Authorization: Bearer ${localStorage.getItem("jwt")}`. Hiển thị thiết kế thẻ trực quan với tên sinh viên, ngày cấp, tổng ngày hiệu lực, số ngày đã dùng, số ngày còn lại dưới dạng progress bar màu xanh (`bg-green-500`) khi `remainingDays > 30`, vàng (`bg-yellow-500`) khi `remainingDays > 7`, đỏ (`bg-red-500`) khi `remainingDays <= 7`. Nút "Gia hạn" mở modal chọn số ngày (30/60/90/180/365) và gọi POST endpoint. Sử dụng `useTranslations` từ `next-intl` cho đa ngôn ngữ. Gắn Tag ID `[REQ-014]`, `[REQ-020]` trong comment component.

#### 📝 Nhiệm vụ phụ 6.2: Xây dựng trang quét QR điểm danh với retry queue IndexedDB
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/frontend/mobile-app/src/app/[locale]/student/attendance/page.tsx`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-012], [REQ-020], [EXC-001]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo trang quét QR tại đường dẫn `./sources/frontend/mobile-app/src/app/[locale]/student/attendance/page.tsx` sử dụng directive `"use client"`. Sử dụng thư viện `html5-qrcode` để hiển thị camera preview với `facingMode: "environment"`. Khi quét thành công, lấy payload base64, tạo `idempotencyKey` bằng `crypto.randomUUID()`, gọi API POST `/api/v1/attendance/scan` với header `Authorization`. Implement hàm `sendScanWithRetry()` sử dụng thư viện `idb` để lưu request vào IndexedDB store `attendance-retry-queue` khi mất mạng (catch network error). Implement hàm `drainRetryQueue()` được gọi qua `window.addEventListener("online", drainRetryQueue)` để tự động retry theo thứ tự FIFO khi có kết nối trở lại. Sử dụng `navigator.onLine` để kiểm tra trạng thái mạng. Hiển thị thông báo thành công (màu xanh), duplicate (màu vàng), lỗi (màu đỏ). Sử dụng `useTranslations` cho đa ngôn ngữ. Gắn Tag ID `[REQ-012]`, `[REQ-020]`, `[EXC-001]` trong comment component.

#### 📝 Nhiệm vụ phụ 6.3: Kiểm thử component React với React Testing Library
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/frontend/mobile-app/src/app/[locale]/student/card/__tests__/page.test.tsx
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-014], [REQ-012], [REQ-020]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp kiểm thử tại đường dẫn `./sources/frontend/mobile-app/src/app/[locale]/student/card/__tests__/page.test.tsx` sử dụng Vitest + React Testing Library. Test case 1 (`rendersCardWithRemainingDays`): Mock fetch trả về `{validityDays: 90, usedDays: 14, remainingDays: 76}`, kỳ vọng hiển thị text "76" trên màn hình và progress bar có width ~84.4%. Test case 2 (`rendersNoCardMessage`): Mock fetch trả về null, kỳ vọng hiển thị "noCard" message. Test case 3 (`progressBarColorChanges`): Mock fetch trả về `{remainingDays: 10}`, kỳ vọng className chứa `bg-yellow-500`. Test case 4 (`rendersDuplicateStatus`): Mock fetch trả về `{success: true, duplicate: true}`, kỳ vọng hiển thị thông báo "Đã điểm danh" với màu vàng. Sử dụng `vi.mock()` để mock `fetch` global. Gắn Tag ID trong comment test file.

#### 📝 Nhiệm vụ phụ 6.4: Biên soạn tài liệu hướng dẫn sử dụng frontend mobile
##### Sub-Agent được phân công: Doc
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/docs/operations/FrontendMobileManual.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu Markdown tại đường dẫn `./sources/docs/operations/FrontendMobileManual.md` bằng tiếng Việt. Nội dung bao gồm: (1) Cấu trúc thư mục dự án `./sources/frontend/mobile-app/` với mô tả từng thư mục `src/app/[locale]/`, `src/lib/`, `e2e/`. (2) Hướng dẫn build/deploy sử dụng `npm run build` và `npm run start`. (3) Cấu hình biến môi trường Firebase trong tệp `.env.local` với các key `NEXT_PUBLIC_FIREBASE_*`. (4) Hướng dẫn sử dụng QR scanner với cơ chế retry queue IndexedDB, kèm sơ đồ Mermaid thể hiện flow xử lý khi mất mạng. (5) Cách thêm ngôn ngữ mới vào i18n config bằng cách thêm locale vào `middleware.ts` và tạo file translation trong `messages/`. (6) Cách tùy chỉnh SEO meta tags cho từng locale thông qua `generateMetadata`. (7) Sơ đồ Mermaid quy trình chuyển tiếp giữa các role trong navigation. Gắn Tag ID trong các mục liên quan.

### 🌤️ NGÀY 7: <!--DAY_HEADER_START-->Tổng Hợp, Đánh Giá và Kiểm Thử Hiệu Năng<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 7.1: Đánh giá mã và tối ưu hóa attendance-service
##### Sub-Agent được phân công: Reviewer
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-012], [REQ-013], [REQ-014], [REQ-015], [REQ-024], [ARC-007], [EXC-001], [EXC-002]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Reviewer kiểm tra mã nguồn `AttendanceService`, `StudentCardService` và `AttendanceReportService` để phát hiện các vấn đề tiềm ẩn: (1) Xử lý transaction boundary khi tạo Attendance và publish Kafka event - đề xuất sử dụng outbox pattern: ghi event vào bảng outbox, separate poller publish lên Kafka. (2) Khả năng race condition khi 2 request đồng thời cùng idempotencyKey - đề xuất sử dụng UNIQUE constraint trên cột `idempotency_key` của bảng attendance. (3) Hiệu suất truy vấn báo cáo CSV với dataset lớn - đề xuất sử dụng `StreamingOutput` và JPA `Stream<T>` để tránh load toàn bộ rows vào memory. (4) Khả năng deadlock khi insert Attendance và update StudentCard đồng thời - đề xuất sử dụng `pessimistic lock` cho bản ghi StudentCard khi renew. Tạo báo cáo review tại đường dẫn `./sources/docs/review/phase4-day7-attendance-review.md` liệt kê chi tiết từng issue phát hiện kèm severity (high/medium/low) và đề xuất fix cụ thể. Gắn Tag ID trong báo cáo.

#### 📝 Nhiệm vụ phụ 7.2: Tổng hợp tài liệu API contracts cho toàn bộ giai đoạn 4
##### Sub-Agent được phân công: Doc
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/docs/api/AttendanceApiContracts.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-012], [REQ-013], [REQ-014], [REQ-015], [REQ-016], [REQ-017], [REQ-018], [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [ARC-007], [ARC-008], [ARC-009], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Cập nhật và tổng hợp tài liệu `./sources/docs/api/AttendanceApiContracts.md` mô tả toàn bộ API contracts được triển khai trong Giai đoạn 4. Nội dung bao gồm: (1) Endpoint POST `/api/v1/attendance/scan` với idempotency flow. (2) Endpoint GET `/api/v1/students/{id}/card` với công thức tính remainingDays. (3) Endpoint POST `/api/v1/students/{id}/card/renew` với validation 1-365 ngày. (4) Endpoint GET `/api/v1/reports/attendance` với giới hạn 30 ngày và format CSV. (5) Endpoint POST `/api/v1/chatbot/message` với LLM gateway. (6) Endpoint CRUD `/api/v1/promotions` và `/api/v1/announcements`. Mỗi endpoint có đầy đủ request/response schema, validation rules, error codes, authentication requirements. Bổ sung sơ đồ Mermaid tổng quan kiến trúc tích hợp giữa attendance-service, notification-service, frontend mobile-app và Kafka event bus. Tài liệu sử dụng tiếng Việt cho mô tả, giữ nguyên tiếng Anh cho schema và Tag ID. Gắn Tag ID tương ứng trong từng mục.

#### 📝 Nhiệm vụ phụ 7.3: Kiểm thử hiệu năng và tải cho hệ thống
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/LoadTestSuite.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-012], [REQ-013], [REQ-014], [REQ-016], [REQ-024], [ARC-007], [ARC-008], [NFR-001]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo bộ kiểm thử tải tại đường dẫn `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/LoadTestSuite.java` thuộc package `org.nlh4j.membershiphub.attendanceservice` sử dụng Gatling. Kịch bản 1 (`attendanceScanLoadTest`): Mô phỏng 1000 sinh viên đồng thời quét QR trong 60 giây, kỳ vọng p95 response time < 200ms theo NFR-001, zero lỗi. Kịch bản 2 (`notificationBurstTest`): Publish 5000 message lên Kafka topic `attendance-scanned` trong 30 giây, kỳ vọng tất cả notification được gửi FCM thành công trong vòng 60 giây. Kịch bản 3 (`csvReportPerformanceTest`): Tạo 10.000 bản ghi attendance, request báo cáo CSV 30 ngày, kỳ vọng response < 5 giây. Sử dụng Gatling Simulation với `httpConf`, `scenario`, `setUp` inject ramp-up pattern. Gắn Tag ID trong comment test class.

#### 📝 Nhiệm vụ phụ 7.4: Đánh giá cuối cùng và xác nhận hoàn thành giai đoạn 4
##### Sub-Agent được phân công: Reviewer
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/docs/review/phase4-final-review.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-012], [REQ-013], [REQ-014], [REQ-015], [REQ-016], [REQ-017], [REQ-018], [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [ARC-007], [ARC-008], [ARC-009], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-001], [NFR-007]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Reviewer thực hiện đánh giá tổng thể cuối cùng cho toàn bộ Giai đoạn 4. Kiểm tra cross-cutting: (1) Tất cả 14 Tag ID yêu cầu nghiệp vụ từ [REQ-012] đến [REQ-024] đã được ánh xạ đầy đủ trong mã nguồn và tài liệu. (2) Tất cả 5 Tag ID ngoại lệ từ [EXC-001] đến [EXC-005] đã được hiện thực hóa với cơ chế xử lý phù hợp. (3) Tất cả 3 Tag ID kiến trúc [ARC-007], [ARC-008], [ARC-009] đã được tuân thủ. (4) Mọi mã nguồn Java tuân thủ package convention `org.nlh4j.membershiphub.attendanceservice` và `org.nlh4j.membershiphub.notificationservice`. (5) Frontend mobile-app tuân thủ cấu trúc Next.js 14.2.5 với App Router. (6) Tất cả endpoint REST trả về `Content-Type: application/json; charset=utf-8` và hỗ trợ `Accept-Language`. Tạo báo cáo tổng hợp tại đường dẫn `./sources/docs/review/phase4-final-review.md` liệt kê coverage matrix giữa Tag ID và file thực thi, các issue còn tồn đọng (nếu có), và xác nhận định nghĩa hoàn thành giai đoạn. Gắn Tag ID đầy đủ trong báo cáo.