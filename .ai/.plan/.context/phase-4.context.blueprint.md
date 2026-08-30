# Giai đoạn 4: <!--PHASE_NAME_START-->Tích Hợp Thẻ Thành Viên, Thông Báo Đa Kênh, Khuyến Mãi, AI Chatbot và Hợp Đồng Mobile<!--PHASE_NAME_END-->

## 📊 Kiểm Soát Tài Liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Bản Thiết Kế** | ARCH-20260829225017 |
| **Tên Dự Án** | membership-hub |
| **Giai đoạn** | 4 |
| **Tên Giai Đoạn** | <!--PHASE_NAME_START-->Tích Hợp Thẻ Thành Viên, Thông Báo Đa Kênh, Khuyến Mãi, AI Chatbot và Hợp Đồng Mobile<!--PHASE_NAME_END--> |
| **Mô Tả** | <!--PHASE_DESC_START-->Giai đoạn 4 triển khai hệ thống quản lý thẻ thành viên số với logic gia hạn 1-365 ngày, hệ thống thông báo đa kênh thông qua Kafka với cơ chế retry 3 lần và dead-letter queue, quản lý CRUD khuyến mãi với chế độ perpetual, CRUD thông báo chung với auto-hide khi quá hạn, tích hợp AI Chatbot dựa trên Vertex AI Gemini với cơ chế escalate khi độ tin cậy thấp dưới 0.6, cùng với việc hoàn thiện hợp đồng tích hợp mobile app chuẩn OpenAPI 3.1 hỗ trợ bearer token và offline cache thông qua Service Worker kết hợp IndexedDB<!--PHASE_DESC_END--> |
| **Phiên Bản** | 1.0 (Baseline) |
| **Ngày Giờ** | 2026/08/29 22:50:17 |
| **Tác Giả** | Enterprise System Architect (SA Agent) |
| **Phê Duyệt** | Đang chờ Rà Soát Quản Trị Kỹ Thuật |

## 1. Phạm Vi Hoạt Động Và Mục Tiêu Giai Đoạn

Giai đoạn 4 đóng vai trò trụ cột nghiệp vụ thứ tư trong hệ thống membership-hub, tập trung vào việc hiện thực hóa bốn nhóm chức năng quan trọng bám sát các yêu cầu nghiệp vụ REQ-014 đến REQ-019 và REQ-021, đồng thời hoàn thiện hợp đồng tích hợp Mobile (ARC-009) và hợp đồng thông báo đa kênh (ARC-008). Phạm vi kỹ thuật cốt lõi của giai đoạn này bao gồm 8 nhiệm vụ backlog chính được phân bổ: Nhiệm vụ 14 (Xem thẻ thành viên và số ngày còn lại), Nhiệm vụ 15 (Gia hạn thẻ thành viên với renewal_days 1-365), Nhiệm vụ 16 (Kích hoạt thông báo đa kênh Push + Zalo), Nhiệm vụ 17 (Quản lý CRUD khuyến mãi), Nhiệm vụ 18 (Quản lý CRUD thông báo chung với auto-hide), Nhiệm vụ 19 (Tích hợp AI Chatbot Vertex AI), Nhiệm vụ 21 (Đẩy Push Notification qua FCM/APNs), Nhiệm vụ 29 (Hợp đồng tích hợp Notification đa kênh), và Nhiệm vụ 30 (Tích hợp Mobile App với Backend qua REST kèm offline cache). Theo kế hoạch phân bổ trong bảng tổng hợp đa giai đoạn 4.2, giai đoạn 4 được phân bổ chính xác khoảng ngày "Ngày 1 - 7" nghĩa là Relative_Z = 7 ngày làm việc liên tục.

Trên microservice `user-service`, giai đoạn này xây dựng REST API `GET /api/v1/students/{studentId}/card` trả về thông tin thẻ thành viên bao gồm `cardId`, `studentId`, `issueDate`, `endDate`, `totalValidityDays`, `usedDays`, `remainingDays`, `renewalCount` với logic tính toán `remainingDays` dựa trên `endDate - now()`. Endpoint `POST /api/v1/students/{studentId}/card/renew` xử lý gia hạn thẻ với `renewalDays` nằm trong khoảng 1-365 (theo [REQ-015] và [EXC-004]), cập nhật `endDate` và lưu lịch sử gia hạn vào bảng `CardRenewalHistory` phục vụ audit. Toàn bộ thao tác gia hạn phải sử dụng `@Transactional` đảm bảo ACID, đồng thời đẩy sự kiện Kafka `notification-queue` để worker gửi thông báo xác nhận cho học viên.

Trên microservice `center-service`, giai đoạn này triển khai REST CRUD `/api/v1/promotions` với các trường `code` UNIQUE, `discountPercent` 1-100, `startDate` và `endDate` tuỳ chọn, hỗ trợ perpetual promotion khi `endDate` là NULL thông qua cờ `isPerpetual`. REST CRUD `/api/v1/announcements` với các trường `title`, `content`, `startDate`, `expiryDate` tuỳ chọn, kèm scheduled task tự động đánh dấu `is_active=false` khi `expiryDate < CURRENT_DATE`. Logic validation đảm bảo tên mã khuyến mãi không trùng lặp và khoảng ngày hợp lệ.

Trên microservice `attendance-service`, giai đoạn này hiện thực hóa hệ thống thông báo đa kênh với REST endpoint `POST /api/v1/notifications/dispatch` nhận `NotificationDispatchRequest` chứa `notificationType` thuộc tập {PUSH, ZALO_GROUP, IN_APP}, `messageTitle`, `messageBody` và target audience. Sự kiện được publish lên Kafka topic `notification-queue` với 6 partition, retention 7 ngày, key là `dispatchId` UUID. Worker consumer `NotificationEventConsumer` xử lý message theo `notificationType`: nếu PUSH gọi `FcmClient` thông qua Firebase Admin SDK, nếu ZALO_GROUP gọi `ZaloBotClient` thông qua REST API `https://bot-api.zalo.me/v2/message`, nếu IN_APP lưu vào bảng `NotificationDispatch`. Cơ chế retry tối đa 3 lần với exponential backoff (1 phút, 5 phút, 15 phút) theo [EXC-003], sau đó chuyển trạng thái `DEAD_LETTER` và ghi audit log. Endpoint `POST /api/v1/devices/register` lưu trữ device token kết hợp platform iOS/Android/Web phục vụ push notification theo [REQ-021].

Trên microservice `course-service`, giai đoạn này triển khai REST endpoint `POST /api/v1/chatbot/query` tích hợp Vertex AI Gemini thông qua Google Cloud Vertex AI Java SDK. `VertexAiClient.query()` gọi `PredictionServiceClient.predict()` với endpoint `projects/membership-hub/locations/asia-southeast1/publishers/google/models/gemini-pro`, temperature 0.2, maxOutputTokens 512. Cơ chế session management lưu trữ lịch sử hội thoại vào bảng `ChatbotSession` với timeout 30 phút, tự động escalate sang nhân viên hỗ trợ khi `confidence < 0.6`. Scheduled task `cleanupExpiredSessions()` chạy mỗi 5 phút xóa session quá hạn 24 giờ.

Trên tầng frontend Next.js tại `./sources/frontend/web-app/`, giai đoạn này xây dựng `MembershipHubClient` TypeScript sử dụng Axios với request interceptor tự động attach Bearer token, response interceptor xử lý 401 bằng refresh token flow. Module `cacheService` sử dụng IndexedDB qua thư viện `idb` lưu trữ thẻ thành viên, danh sách thông báo, queue offline request với TTL 24 giờ. Service Worker xử lý background sync, gửi lại queued requests khi thiết bị online trở lại. Toàn bộ hợp đồng OpenAPI 3.1 YAML cho notification queue và mobile app được công bố tại `./sources/docs/contracts/` phục vụ mobile team tích hợp.

Mục tiêu chính của giai đoạn là toàn bộ REST API backend, Kafka producer/consumer, OpenAPI contracts và frontend client library có thể tích hợp tức thì lên môi trường staging ngay khi kết thúc, đảm bảo mobile team có đủ tài liệu và SDK để phát triển ứng dụng native. Tất cả thao tác nghiệp vụ phải ghi log kiểm toán thông qua cơ chế tập trung, đảm bảo dấu vết kiểm toán đầy đủ phục vụ tuân thủ NFR-006 với thời gian lưu trữ tối thiểu 1 năm, mọi dữ liệu nhạy cảm phải được mã hóa AES-256 tại rest và truyền tải qua TLS 1.3 theo NFR-003.

## 2. Phạm Vi Kỹ Thuật Cho Phép Và Ranh Giới Thư Mục

Danh sách đầy đủ các tệp tin vật lý được phép tạo mới hoặc tái cấu trúc trong giai đoạn 4, tuân thủ nghiêm ngặt quy ước gói `org.nlh4j.membershiphub` và ranh giới thư mục doanh nghiệp, phân tách theo microservice tương ứng với từng nhóm nghiệp vụ được phân bổ trong bảng phân bổ giai đoạn 4.2 của bối cảnh dự án toàn cục:

* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/StudentCardController.java` — [REQ-014], [REQ-015]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/StudentCardService.java` — [REQ-014], [REQ-015], [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/StudentCardResponse.java` — [REQ-014]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/CardRenewalRequest.java` — [REQ-015]
* `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/StudentCardControllerTest.java` — [REQ-014], [REQ-015]
* `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/service/StudentCardServiceTest.java` — [REQ-015], [EXC-004]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/PromotionController.java` — [REQ-017]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/AnnouncementController.java` — [REQ-018]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/PromotionService.java` — [REQ-017]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/AnnouncementService.java` — [REQ-018]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/PromotionRequest.java` — [REQ-017]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/PromotionResponse.java` — [REQ-017]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/AnnouncementRequest.java` — [REQ-018]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/AnnouncementResponse.java` — [REQ-018]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/repository/PromotionRepository.java` — [REQ-017]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/repository/AnnouncementRepository.java` — [REQ-018]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/exception/DuplicatePromotionCodeException.java` — [REQ-017], [EXC-004]
* `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/PromotionServiceTest.java` — [REQ-017]
* `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/AnnouncementServiceTest.java` — [REQ-018]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/controller/NotificationController.java` — [REQ-016], [ARC-008]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/controller/DeviceController.java` — [REQ-021]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/NotificationDispatcherService.java` — [REQ-016], [ARC-008], [EXC-003]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/PushDeliveryService.java` — [REQ-021], [EXC-003]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/integration/ZaloBotClient.java` — [ARC-008]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/integration/FcmClient.java` — [REQ-021]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/NotificationDispatchRequest.java` — [REQ-016]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/NotificationDispatchResponse.java` — [REQ-016]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/DeviceRegistrationRequest.java` — [REQ-021]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/kafka/NotificationEventProducer.java` — [ARC-008]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/kafka/NotificationEventConsumer.java` — [ARC-008], [EXC-003]
* `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/NotificationDispatcherServiceTest.java` — [REQ-016], [EXC-003]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/ChatbotController.java` — [REQ-019]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/ChatbotService.java` — [REQ-019], [EXC-004]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/integration/VertexAiClient.java` — [REQ-019]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/ChatbotQueryRequest.java` — [REQ-019]
* `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/ChatbotResponse.java` — [REQ-019]
* `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/ChatbotServiceTest.java` — [REQ-019], [EXC-004]
* `./sources/frontend/web-app/src/lib/api/membershipHubClient.ts` — [ARC-009]
* `./sources/frontend/web-app/src/lib/api/notifications.ts` — [ARC-008], [REQ-021]
* `./sources/frontend/web-app/src/lib/offline/cacheService.ts` — [ARC-009]
* `./sources/frontend/web-app/src/sw/service-worker.ts` — [ARC-009]
* `./sources/frontend/web-app/src/types/notification.d.ts` — [ARC-008]
* `./sources/frontend/web-app/src/types/memberCard.d.ts` — [ARC-009]
* `./sources/frontend/web-app/src/test/notifications.spec.ts` — [ARC-008]
* `./sources/frontend/web-app/src/test/cacheService.spec.ts` — [ARC-009]
* `./sources/docs/contracts/notification-queue.openapi.yaml` — [ARC-008], [REQ-016], [DOC-001]
* `./sources/docs/contracts/mobile-app.openapi.yaml` — [ARC-009], [DOC-001]
* `./sources/docs/contracts/member-card.openapi.yaml` — [REQ-014], [DOC-001]

* **RÀNG BUỘC BẮT BUỘC VỀ BỘ KHUNG NỀN TẢNG**:
  - Tất cả tài sản mã nguồn ứng dụng trong giai đoạn 4 phải kế thừa bộ khung build descriptors đã được khởi tạo ở Giai đoạn 1, bao gồm `./sources/backend/pom.xml` (root parent) và 4 tệp con `./sources/backend/<service-name>/pom.xml` cho `user-service`, `center-service`, `course-service`, `attendance-service`.
  - Tệp `./sources/backend/pom.xml` và các tệp con KHÔNG được tái tạo trong giai đoạn này vì đã tồn tại từ Giai đoạn 1, đồng thời tệp `./sources/frontend/package.json` và `./sources/frontend/tsconfig.json` cũng đã được khởi tạo ở Giai đoạn 1.
  - Toàn bộ mã nguồn mới phải tuân thủ quy ước gói `org.nlh4j.membershiphub.<service-name>` và được truy vết bằng các mã thẻ quy định trong ma trận phân bổ ngay từng dòng lệnh, đảm bảo 100% khả năng truy nguyên từ yêu cầu nghiệp vụ đến triển khai thực tế.

## 3. Chỉ Thị Chức Năng Cho Từng Sub-Agent

* **Coder**: Đóng vai trò lập trình viên ứng dụng chính. Chịu trách nhiệm hiện thực hóa toàn bộ controller, service, DTO, exception handler, repository, Kafka producer/consumer, integration client (FcmClient, ZaloBotClient, VertexAiClient) trong package `controller`, `service`, `dto`, `repository`, `exception`, `kafka`, `integration` của `user-service`, `center-service`, `attendance-service` và `course-service`. Song song đó, Coder cũng chịu trách nhiệm phát triển tầng frontend TypeScript client tại `./sources/frontend/web-app/src/lib/api/`, module offline cache, Service Worker. Bị cấm viết bộ kiểm thử, tài liệu hoặc cấu hình hạ tầng.

* **Tester**: Đóng vai trò kiểm thử viên chính. Tạo bộ kiểm thử đơn vị JUnit 5 kết hợp Mockito cho `StudentCardController`, `StudentCardService`, `PromotionService`, `AnnouncementService`, `NotificationDispatcherService`, `ChatbotService`. Xây dựng bộ kiểm thử tích hợp sử dụng Testcontainers (PostgreSQL, Kafka) và Embedded Kafka. Đối với frontend, tạo bộ kiểm thử Jest kết hợp React Testing Library cho `notifications.spec.ts` và `cacheService.spec.ts` sử dụng `fake-indexeddb` để mock IndexedDB. Bị cấm sửa đổi mã nguồn sản phẩm.

* **Doc**: Soạn thảo các tài liệu OpenAPI 3.1 YAML tại `./sources/docs/contracts/` mô tả đầy đủ endpoint `/api/v1/students/{id}/card`, `/api/v1/students/{id}/card/renew`, `/api/v1/notifications/dispatch`, `/api/v1/devices/register`, `/api/v1/chatbot/query`. Tài liệu phải chứa security scheme BearerAuth, mô tả chi tiết request/response schema, mã lỗi chuẩn, ví dụ JSON, hướng dẫn retry và dead letter queue theo EXC-003.

* **Reviewer**: Thực hiện rà soát chất lượng mã nguồn theo checklist OWASP Top 10, đánh giá tính đúng đắn của logic retry queue với exponential backoff, dead letter handling, idempotency trong FcmClient, vertex AI confidence scoring. Xác minh tính bảo mật của việc sử dụng JPQL parameter binding, validation input với `@Valid`, mã hóa PII. Phát hiện sớm các vấn đề race condition, memory leak trong dead letter queue, thread safety trong NotificationEventConsumer.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn (DoD)

Giai đoạn 4 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: (1) Endpoint `GET /api/v1/students/{studentId}/card` trả về đầy đủ thông tin thẻ thành viên với `remainingDays` được tính toán chính xác; (2) Endpoint `POST /api/v1/students/{studentId}/card/renew` xử lý gia hạn với validation `renewalDays` trong khoảng 1-365 và lưu lịch sử vào bảng `CardRenewalHistory`; (3) REST CRUD `/api/v1/promotions` hỗ trợ perpetual promotion khi `endDate=null`, kiểm tra trùng lặp `code` UNIQUE; (4) REST CRUD `/api/v1/announcements` kèm scheduled task auto-hide khi quá expiry; (5) Endpoint `POST /api/v1/notifications/dispatch` publish sự kiện lên Kafka topic `notification-queue` với schema chuẩn; (6) Consumer `NotificationEventConsumer` xử lý PUSH, ZALO_GROUP, IN_APP đúng kênh, retry 3 lần với exponential backoff theo EXC-003, chuyển DEAD_LETTER sau khi hết retry; (7) Endpoint `POST /api/v1/devices/register` lưu device token với platform iOS/Android/Web; (8) Endpoint `POST /api/v1/chatbot/query` gọi Vertex AI Gemini, escalate khi confidence < 0.6, quản lý session 30 phút; (9) Frontend TypeScript client `MembershipHubClient` hỗ trợ bearer token auto-attach, refresh token flow khi 401; (10) Module `cacheService` sử dụng IndexedDB cache thẻ thành viên và queue offline request; (11) 100% thẻ truy vết REQ-014, REQ-015, REQ-016, REQ-017, REQ-018, REQ-019, REQ-021, ARC-008, ARC-009, EXC-003 được ánh xạ đầy đủ vào mã nguồn và tài liệu; (12) 100% bộ kiểm thử JUnit và Jest đạt trạng thái PASS với code coverage >= 80% cho các lớp controller, service và frontend module.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->TRIỂN KHAI STUDENTCARD API VÀ GIA HẠN THẺ THÀNH VIÊN<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 1.1: Xây dựng REST endpoint xem thông tin thẻ thành viên

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/StudentCardController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-014]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn tại đường dẫn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/StudentCardController.java` hiện thực hóa lớp `StudentCardController` với annotation `@Path("/api/v1/students")` và method `@GET` tại `@Path("/{studentId}/card")`. Inject `StudentCardService` qua CDI. Sử dụng `@PathParam("studentId")` cho UUID, `@Context SecurityContext` để lấy thông tin người dùng hiện tại. Trả về DTO `StudentCardResponse` chứa các trường `cardId`, `studentId`, `issueDate`, `endDate`, `totalValidityDays`, `usedDays`, `remainingDays`, `renewalCount`. Áp dụng `@RolesAllowed({"STUDENT", "CENTER_ADMIN", "SYSTEM_ADMIN"})` và kiểm tra student chỉ được xem thẻ của chính mình trừ khi role là admin. Trả về HTTP 404 với mã `CARD_NOT_FOUND` khi không tìm thấy thẻ. Truy vết đầy đủ theo [REQ-014].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi schema trong sub-task này
-- Sử dụng bảng StudentCards đã được tạo trong Phase 1
-- Truy vấn dữ liệu thẻ thành viên theo studentId
SELECT card_id, student_id, issue_date, end_date, validity_days, remaining_days
FROM StudentCards
WHERE student_id = :studentId
  AND is_active = true;
```
<!--END_DDL_MIGRATION-->

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```json
{
  "endpoint": "GET /api/v1/students/{studentId}/card",
  "request": {
    "pathParams": { "studentId": "UUID" },
    "headers": { "Authorization": "Bearer <jwt_token>" }
  },
  "response_200": {
    "cardId": "uuid",
    "studentId": "uuid",
    "issueDate": "2024-01-15",
    "endDate": "2025-01-15",
    "totalValidityDays": 365,
    "usedDays": 45,
    "remainingDays": 320,
    "renewalCount": 0
  },
  "response_404": {
    "error": "CARD_NOT_FOUND",
    "message": "Không tìm thấy thẻ thành viên cho học viên này"
  }
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 1.2: Kiểm thử đơn vị cho StudentCardController

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/StudentCardController.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/StudentCardControllerTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-014]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh test class JUnit 5 tại đường dẫn `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/StudentCardControllerTest.java` với `@QuarkusTest` annotation. Sử dụng `@InjectMock` để mock `StudentCardService`. Test case 1: `testGetCard_Success` gọi GET endpoint với studentId hợp lệ, mock service trả về `StudentCardResponse` mẫu, verify HTTP 200 và JSON body chứa đầy đủ trường `cardId`, `studentId`, `issueDate`, `endDate`, `totalValidityDays`, `usedDays`, `remainingDays`, `renewalCount`. Test case 2: `testGetCard_NotFound` mock service throw `NotFoundException`, verify HTTP 404 với error code `CARD_NOT_FOUND`. Test case 3: `testGetCard_Forbidden` gọi với student khác, verify HTTP 403 với mã `INSUFFICIENT_PRIVILEGES`. Sử dụng `RestAssured` để gọi endpoint, assert JSON path với `jsonPath()`. Verify authorization header được gửi đúng định dạng Bearer token. Truy vết [REQ-014].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Test scope không yêu cầu migration
-- Sử dụng H2 in-memory database cho test với schema đã sync
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 1.3: Tài liệu hóa API thẻ thành viên

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/contracts/member-card.openapi.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-014], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh file OpenAPI 3.1 YAML tại đường dẫn `./sources/docs/contracts/member-card.openapi.yaml` mô tả đầy đủ endpoint `GET /api/v1/students/{studentId}/card`. Bao gồm: mô tả nghiệp vụ, parameters (path, query, header), request body schema, response schemas (200, 401, 403, 404), ví dụ JSON cho mỗi response, security scheme BearerAuth. Thêm section xác thực phân quyền giải thích rằng chỉ student sở hữu hoặc admin mới có quyền truy cập. Bổ sung ví dụ curl command để gọi API. Truy vết [REQ-014] và [DOC-001].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Tài liệu không yêu cầu migration vật lý
-- Phần này chỉ tham chiếu bảng StudentCards trong phần mô tả
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 1.4: Xây dựng logic gia hạn thẻ với validation renewal_days

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/StudentCardService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-015], [EXC-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Thêm method `renewCard(UUID studentId, CardRenewalRequest request)` vào `StudentCardService` tại đường dẫn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/StudentCardService.java`. Bước 1: Validate `renewalDays` trong khoảng 1-365 sử dụng Bean Validation `@Min(1) @Max(365)`, ném `ConstraintViolationException` nếu sai theo [EXC-004]. Bước 2: Tìm `StudentCard` active theo studentId thông qua `StudentCardRepository.findByStudentId()`, throw `NotFoundException` với mã `CARD_NOT_FOUND` nếu không tồn tại. Bước 3: Lưu giá trị `previousEndDate`, tính `newEndDate = currentEndDate.plusDays(renewalDays)`. Bước 4: Cập nhật entity với `endDate = newEndDate`, `renewalCount = renewalCount + 1`, `lastRenewedAt = LocalDateTime.now()`. Bước 5: Tạo bản ghi `CardRenewalHistory` với `previousEndDate`, `newEndDate`, `paymentReference`, `renewedAt`. Bước 6: Publish event `card-renewed` lên Kafka topic `notification-queue` thông qua `NotificationEventProducer` để worker gửi thông báo xác nhận. Sử dụng `@Transactional` để đảm bảo ACID giữa cập nhật thẻ và lưu lịch sử. Truy vết [REQ-015] và [EXC-004].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Sử dụng bảng StudentCards đã tạo trong Phase 1
-- Logic tính toán end_date mới khi gia hạn
UPDATE StudentCards
SET end_date = end_date + (:renewalDays * INTERVAL '1 day'),
    renewal_count = renewal_count + 1,
    last_renewed_at = now(),
    updated_at = now()
WHERE card_id = :cardId
  AND is_active = true;

-- Lưu lịch sử gia hạn
INSERT INTO CardRenewalHistory (renewal_id, card_id, student_id, renewal_days, previous_end_date, new_end_date, payment_reference, renewed_at)
VALUES (gen_random_uuid(), :cardId, :studentId, :renewalDays, :previousEndDate, :newEndDate, :paymentReference, now());
```
<!--END_DDL_MIGRATION-->

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```json
{
  "endpoint": "POST /api/v1/students/{studentId}/card/renew",
  "request": {
    "pathParams": { "studentId": "UUID" },
    "body": {
      "renewalDays": 30,
      "paymentReference": "PAY-2024-001234"
    }
  },
  "response_200": {
    "cardId": "uuid",
    "endDate": "2025-02-14",
    "renewalCount": 1
  }
}
```
<!--END_API_CONTRACT-->

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```java
// Xử lý theo [EXC-004]: validation renewalDays ngoài khoảng 1-365
if (request.getRenewalDays() < 1 || request.getRenewalDays() > 365) {
    throw new ConstraintViolationException(
        "renewalDays phải nằm trong khoảng 1-365",
        Set.of(ConstraintViolationImpl.forField("renewalDays"))
    );
}
```
<!--END_EXC_HANDLER-->

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->KIỂM THỬ GIA HẠN THẺ VÀ TRIỂN KHAI REST ENDPOINT GIA HẠN<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 2.1: Xây dựng REST endpoint gia hạn thẻ

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/StudentCardController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-015]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Thêm method `@POST` với `@Path("/{studentId}/card/renew")` trong `StudentCardController` tại đường dẫn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/StudentCardController.java`. Sử dụng `@Valid` annotation cho `CardRenewalRequest` body để kích hoạt Bean Validation. Inject `StudentCardService`. Trả về `Response.status(Response.Status.OK).entity(updatedCardResponse).build()`. Annotation `@RolesAllowed({"STUDENT"})` và kiểm tra student chỉ gia hạn thẻ của chính mình thông qua `@Context SecurityContext` lấy `userId` từ JWT. Xử lý exception bằng `@ExceptionHandler` cho `ConstraintViolationException` trả về HTTP 400 với danh sách field lỗi và mã `INVALID_RENEWAL_PAYLOAD`. Truy vết [REQ-015].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Endpoint layer không trực tiếp thao tác DDL
-- Logic được delegate xuống service layer đã triển khai ở Nhiệm vụ 1.4
```
<!--END_DDL_MIGRATION-->

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```java
// Xử lý [EXC-004]: validation thất bại trả về HTTP 400
@ExceptionHandler(ConstraintViolationException.class)
public Response handleValidation(ConstraintViolationException ex) {
    return Response.status(400)
        .entity(Map.of(
            "error", "INVALID_RENEWAL_PAYLOAD",
            "violations", ex.getConstraintViolations().stream()
                .map(v -> Map.of("field", v.getPropertyPath().toString(), "message", v.getMessage()))
                .toList()
        ))
        .build();
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 2.2: Kiểm thử tích hợp cho luồng gia hạn thẻ

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/CardRenewalIntegrationTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-015], [EXC-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh integration test tại đường dẫn `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/CardRenewalIntegrationTest.java` sử dụng `@QuarkusTest` với test profile active H2. Test scenario 1: `testRenewCard_Success` tạo StudentCard mẫu với `endDate` ban đầu, gọi POST renew với `renewalDays=30`, verify response 200 và `endDate` được cộng thêm 30 ngày, kiểm tra bản ghi `CardRenewalHistory` được tạo với `previousEndDate` và `newEndDate` đúng. Test scenario 2 theo [EXC-004]: `testRenewCard_InvalidDays` gọi với `renewalDays=400`, verify HTTP 400 với error code `INVALID_RENEWAL_PAYLOAD`. Test scenario 3: `testRenewCard_NotFound` gọi với studentId không tồn tại, verify HTTP 404 với mã `CARD_NOT_FOUND`. Test scenario 4: `testRenewCard_Forbidden` gọi với student khác, verify HTTP 403 với mã `INSUFFICIENT_PRIVILEGES`. Test scenario 5: `testRenewCard_ConcurrentRenew` sử dụng `CompletableFuture` chạy 2 request đồng thời, verify optimistic locking xử lý đúng không gây mất dữ liệu. Truy vết [REQ-015] và [EXC-004].

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```java
// Test case xác minh [EXC-004] được xử lý đúng
@Test
void testRenewCard_InvalidDays() {
    CardRenewalRequest request = new CardRenewalRequest();
    request.setRenewalDays(400);
    request.setPaymentReference("PAY-INVALID");

    given()
        .contentType(ContentType.JSON)
        .body(request)
    .when()
        .post("/api/v1/students/{id}/card/renew", studentId)
    .then()
        .statusCode(400)
        .body("error", equalTo("INVALID_RENEWAL_PAYLOAD"));
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 2.3: Kiểm thử đơn vị cho StudentCardService

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/StudentCardService.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/service/StudentCardServiceTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-015], [EXC-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh test class tại đường dẫn `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/service/StudentCardServiceTest.java` sử dụng JUnit 5 kết hợp Mockito 5.7.0. Mock `StudentCardRepository`, `CardRenewalHistoryRepository`, `NotificationEventProducer`. Test case 1: `testRenewCard_Success` mock repository trả về StudentCard active, mock producer xác nhận publish event lên Kafka, verify `endDate` được cộng đúng số ngày, `renewalCount` tăng 1, bản ghi `CardRenewalHistory` được tạo. Test case 2 theo [EXC-004]: `testRenewCard_InvalidDays_ThrowsException` gọi với `renewalDays=0` và `renewalDays=366`, verify throw `ConstraintViolationException`. Test case 3: `testRenewCard_CardNotFound` mock repository trả về Optional.empty, verify throw `NotFoundException` với mã `CARD_NOT_FOUND`. Test case 4: `testRenewCard_PublishesKafkaEvent` verify Kafka event được publish với `eventType=card-renewed`, `studentId`, `newEndDate`, `paymentReference`. Test case 5: `testRenewCard_Transactional` sử dụng `@QuarkusTransactionTest` verify khi một trong các thao tác DB fail thì toàn bộ transaction rollback. Truy vết [REQ-015] và [EXC-004].

#### 📝 NHIỆM VỤ PHỤ 2.4: Đánh giá mã nguồn StudentCardService

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/StudentCardService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-014], [REQ-015], [EXC-004], [NFR-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer đánh giá tệp `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/StudentCardService.java` đảm bảo: (1) Logic tính toán `remainingDays` chính xác dựa trên `endDate` và `currentDate`, xử lý đúng múi giờ Asia/Ho_Chi_Minh; (2) Phương thức `renewCard` sử dụng `@Transactional` với `propagation=REQUIRED` đảm bảo ACID; (3) Validation `renewalDays` được thực hiện ở cả Bean Validation và service layer theo defense-in-depth; (4) Truy vấn JPQL sử dụng parameter binding ngăn chặn SQL injection theo OWASP A03; (5) Mã hóa `paymentReference` sử dụng AES-256 trước khi lưu DB theo NFR-003; (6) Kiểm tra idempotency thông qua việc xử lý race condition khi hai request gia hạn đồng thời với optimistic locking `@Version`. Đề xuất cải tiến performance thông qua cache `StudentCard` trong Redis với TTL 300s. Tạo báo cáo review với format: Phát hiện, Mức độ nghiêm trọng, Đề xuất fix, File liên quan.

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->TRIỂN KHAI NOTIFICATION DISPATCHER VÀ KAFKA PRODUCER/CONSUMER<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 3.1: Xây dựng Kafka producer cho notification-queue

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/kafka/NotificationEventProducer.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-008], [REQ-016]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo class `NotificationEventProducer` tại đường dẫn `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/kafka/NotificationEventProducer.java` sử dụng SmallRye Reactive Messaging với `@Channel("notification-queue")` và `Emitter<NotificationEvent>`. Method `dispatch(NotificationDispatchRequest request)` chuyển đổi request thành `NotificationEvent` với UUID `dispatchId`, thời gian `createdAt`, validate enum `notificationType` thuộc tập {PUSH, ZALO_GROUP, IN_APP}. Sử dụng `Multi<NotificationEvent>` để hỗ trợ fan-out tới nhiều user. Cấu hình backpressure với buffer size 256. Log structured với MDC tracking `dispatchId` để phục vụ tracing. Implement interface `HealthCheck` để theo dõi trạng thái Kafka broker. Sử dụng annotation `@ApplicationScoped` cho CDI. Truy vết [ARC-008] và [REQ-016].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không yêu cầu migration mới
-- Producer ghi message trực tiếp lên Kafka topic notification-queue
```
<!--END_DDL_MIGRATION-->

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```json
{
  "kafka_topic": "notification-queue",
  "message_key": "dispatchId",
  "value_schema": {
    "dispatchId": "uuid",
    "notificationType": "PUSH|ZALO_GROUP|IN_APP",
    "targetUserId": "uuid|null",
    "targetGroupZalo": "string|null",
    "targetDeviceToken": "string|null",
    "messageTitle": "string",
    "messageBody": "string",
    "payload": "object|null",
    "attemptCount": "integer",
    "createdAt": "iso8601"
  }
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 3.2: Xây dựng Kafka consumer cho notification-queue

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/kafka/NotificationEventConsumer.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-008], [EXC-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo class `NotificationEventConsumer` tại đường dẫn `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/kafka/NotificationEventConsumer.java` với annotation `@Incoming("notification-queue")` nhận `NotificationEvent`. Implement logic xử lý theo `notificationType`: nếu `PUSH` gọi `FcmClient.sendPush`, nếu `ZALO_GROUP` gọi `ZaloBotClient.postMessage`, nếu `IN_APP` lưu vào `NotificationDispatchRepository`. Bọc logic trong try-catch theo [EXC-003]: nếu gửi thất bại, tăng `attemptCount`, nếu `attemptCount < maxAttempts` (3) thì re-emit message với delay exponential backoff (1 phút, 5 phút, 15 phút) sử dụng `ScheduledExecutorService`, nếu `attemptCount >= 3` thì đánh dấu `DEAD_LETTER` và publish lên dead letter topic `notification-queue.DLQ`. Cập nhật `NotificationDispatch.status` tương ứng. Sử dụng `@Acknowledgment(MANUAL)` để manual commit offset sau khi xử lý thành công. Truy vết [ARC-008] và [EXC-003].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Cập nhật trạng thái dispatch sau khi xử lý
UPDATE NotificationDispatch
SET status = :status,
    attempt_count = attempt_count + 1,
    last_attempt_at = now(),
    delivered_at = CASE WHEN :status = 'DELIVERED' THEN now() ELSE delivered_at END
WHERE dispatch_id = :dispatchId;
```
<!--END_DDL_MIGRATION-->

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```java
// Xử lý [EXC-003]: retry với exponential backoff
@Incoming("notification-queue")
@Acknowledgment(MANUAL)
public CompletionStage<Void> consume(Message<NotificationEvent> message) {
    NotificationEvent event = message.getPayload();
    try {
        deliveryService.deliver(event);
        event.setStatus(NotificationStatus.DELIVERED);
    } catch (DeliveryException e) {
        if (event.getAttemptCount() < MAX_ATTEMPTS) {
            long delayMs = (long) Math.pow(5, event.getAttemptCount()) * 60_000L;
            scheduledExecutor.schedule(() -> producer.retry(event), delayMs, TimeUnit.MILLISECONDS);
        } else {
            event.setStatus(NotificationStatus.DEAD_LETTER);
            deadLetterPublisher.publish(event);
        }
    } finally {
        dispatchRepository.updateStatus(event);
        return message.ack();
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 3.3: Kiểm thử cho luồng notification dispatcher

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/NotificationDispatcherService.java;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/NotificationDispatcherServiceTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-016], [ARC-008], [EXC-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh test class JUnit 5 tại đường dẫn `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/NotificationDispatcherServiceTest.java` với `@QuarkusTest`. Test case 1: `testDispatch_PushType` gọi `dispatcher.dispatch()` với type=PUSH, verify Kafka message được emit với schema đúng thông qua `InMemoryConnector`. Test case 2: `testConsume_PushDeliverySuccess` feed message vào consumer, mock `FcmClient` thành công, verify `NotificationDispatch` status=DELIVERED. Test case 3 theo [EXC-003]: `testConsume_RetryOnFailure` mock `FcmClient` throw exception lần 1, verify `attemptCount=1` và message được schedule retry với delay 1 phút. Test case 4 theo [EXC-003]: `testConsume_DeadLetterAfter3Attempts` giả lập 3 lần fail liên tiếp, verify status=DEAD_LETTER và dead letter publisher được gọi. Test case 5: `testDispatch_InvalidType` gọi với `notificationType` không hợp lệ, verify throw `IllegalArgumentException` với mã `INVALID_NOTIFICATION_TYPE`. Test case 6: `testConsume_ZaloGroupDelivery` mock `ZaloBotClient` trả về success, verify message Zalo được post đúng. Truy vết [REQ-016], [ARC-008] và [EXC-003].

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```java
// Test xác minh [EXC-003] retry logic
@Test
void testConsume_DeadLetterAfter3Attempts() {
    when(fcmClient.sendPush(any())).thenThrow(new DeliveryException("FCM_DOWN"));

    for (int i = 0; i < 3; i++) {
        consumer.consume(testMessage);
    }

    verify(deadLetterPublisher).publish(argThat(event ->
        event.getAttemptCount() == 3 &&
        event.getStatus() == NotificationStatus.DEAD_LETTER
    ));
}
```
<!--END_EXC_HANDLER-->

### 🌤️ NGÀY 4: <!--DAY_HEADER_START-->TRIỂN KHAI FCM CLIENT, ZALO BOT CLIENT VÀ PUSH DELIVERY<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 4.1: Xây dựng FCM integration cho push notification

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/integration/FcmClient.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-021], [EXC-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo class `FcmClient` tại đường dẫn `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/integration/FcmClient.java` sử dụng Firebase Admin SDK. Inject `FirebaseMessaging` instance thông qua CDI producer. Method `sendPush(String deviceToken, String title, String body, Map<String, String> data)` xây dựng `Message` object với `Notification` (title, body) và `AndroidConfig`/`ApnsConfig` tùy platform. Gọi `FirebaseMessaging.getInstance().send(message)` và trả về message ID. Xử lý exception theo [EXC-003]: bắt `FirebaseMessagingException` với mã lỗi `UNREGISTERED` hoặc `INVALID_ARGUMENT` thì throw `InvalidDeviceTokenException` (không retry, đánh dấu device inactive), các mã khác throw `DeliveryException` (retry được). Cấu hình timeout 10 giây cho mỗi request thông qua `HttpRequestOptions.setTimeout()`. Truy vết [REQ-021] và [EXC-003].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Đánh dấu device token không hợp lệ khi FCM trả lỗi UNREGISTERED
UPDATE DeviceToken
SET is_active = false,
    last_used_at = now()
WHERE device_token = :token;
```
<!--END_DDL_MIGRATION-->

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```java
// Xử lý [EXC-003]: phân loại lỗi để quyết định retry hay không
public void sendPush(String token, String title, String body, Map<String, String> data) {
    try {
        Message message = Message.builder()
            .setToken(token)
            .setNotification(Notification.builder().setTitle(title).setBody(body).build())
            .putAllData(data)
            .build();
        firebaseMessaging.send(message);
    } catch (FirebaseMessagingException e) {
        if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
            || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
            throw new InvalidDeviceTokenException(token, e);
        }
        throw new DeliveryException("FCM_DELIVERY_FAILED", e);
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 4.2: Xây dựng Zalo Bot Client

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/integration/ZaloBotClient.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-008], [EXC-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo class `ZaloBotClient` tại đường dẫn `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/integration/ZaloBotClient.java` sử dụng `RESTClient` (MicroProfile Rest Client) gọi Zalo Bot API. Method `postMessage(String groupId, String message)` gửi POST request tới endpoint `https://bot-api.zalo.me/v2/message` với body JSON `{recipient: {group_id: groupId}, message: {text: message}}`. Inject access token từ config `mp.rest.client.zalo.token` thông qua `@ConfigProperty`. Xử lý response theo [EXC-003]: nếu HTTP 401 token hết hạn, refresh token qua Zalo OAuth2 rồi retry 1 lần; nếu HTTP 404 group không tồn tại, throw `ZaloGroupNotFoundException`; các lỗi khác throw `DeliveryException`. Cấu hình timeout 15 giây. Sử dụng `@CircuitBreaker` với `requestVolumeThreshold=4`, `failureRatio=0.5` để tránh gọi liên tục khi Zalo API down. Truy vết [ARC-008] và [EXC-003].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Log lỗi gửi Zalo
INSERT INTO NotificationDispatch (dispatch_id, notification_type, target_group_zalo, status, attempt_count, created_at)
VALUES (:dispatchId, 'ZALO_GROUP', :groupId, 'FAILED', 1, now());
```
<!--END_DDL_MIGRATION-->

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```java
// Xử lý [EXC-003]: refresh token khi 401
@CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5)
public void postMessage(String groupId, String message) {
    try {
        zaloApi.sendMessage(accessToken, buildPayload(groupId, message));
    } catch (WebApplicationException e) {
        if (e.getResponse().getStatus() == 401) {
            accessToken = refreshZaloToken();
            zaloApi.sendMessage(accessToken, buildPayload(groupId, message));
        } else if (e.getResponse().getStatus() == 404) {
            throw new ZaloGroupNotFoundException(groupId, e);
        } else {
            throw new DeliveryException("ZALO_DELIVERY_FAILED", e);
        }
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 4.3: Đánh giá mã nguồn notification pipeline

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/kafka/NotificationEventConsumer.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-008], [REQ-016], [REQ-021], [EXC-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer đánh giá tệp `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/kafka/NotificationEventConsumer.java` tập trung vào: (1) Thread safety - verify sử dụng `ConcurrentHashMap` cho in-memory state, không có shared mutable state; (2) Memory leak - đảm bảo dead letter queue có bounded size và cleanup scheduler; (3) Idempotency - verify xử lý duplicate message qua `dispatchId` unique constraint trong bảng `NotificationDispatch`; (4) Error handling - đảm bảo mọi exception path đều log đầy đủ context với MDC và ghi vào audit log theo NFR-006; (5) Backpressure - kiểm tra Kafka consumer config `max.poll.records` phù hợp, tránh OOM khi message burst; (6) Sử dụng `@Acknowledgment(MANUAL)` đúng cách, tránh message loss khi consumer crash; (7) Phân tích EXPLAIN ANALYZE cho query `SELECT * FROM NotificationDispatch WHERE status = 'PENDING' AND attempt_count < 3 ORDER BY created_at ASC LIMIT 100` để đề xuất index tối ưu. Sinh báo cáo review với format: Vấn đề phát hiện, Mức độ nghiêm trọng (Critical/High/Medium/Low), Đề xuất fix cụ thể, File liên quan. Truy vết [ARC-008], [REQ-016], [REQ-021] và [EXC-003].

### 🌤️ NGÀY 5: <!--DAY_HEADER_START-->TRIỂN KHAI REST API CHO PROMOTION VÀ ANNOUNCEMENT CRUD<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 5.1: Xây dựng CRUD API cho Promotions

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/PromotionController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-017]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Triển khai REST controller tại đường dẫn `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/PromotionController.java` với 5 endpoints: GET `/api/v1/promotions` (list có phân trang, hỗ trợ lọc theo `isActive`, `centerId`), GET `/{promoId}` (chi tiết), POST `/` (tạo mới), PUT `/{promoId}` (cập nhật), DELETE `/{promoId}` (xóa mềm với `is_active=false`). Sử dụng `@Valid` cho `PromotionRequest` với validation: `code` max 30 chars UNIQUE, `name` max 100 chars, `discountPercent` 1-100, `startDate`/`endDate` nếu có phải hợp lệ. Logic perpetual: nếu `endDate=null` thì set `isPerpetual=true`. Inject `PromotionService` và `AuditLogService`. Áp dụng `@RolesAllowed({"CENTER_ADMIN", "MANAGER", "SYSTEM_ADMIN"})`. Kiểm tra center ownership cho CenterAdmin. Truy vết [REQ-017].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Sử dụng bảng Promotions đã định nghĩa trong bảng phân bổ DDL giai đoạn 4
-- Logic truy vấn danh sách khuyến mãi còn hiệu lực
SELECT promo_id, code, name, discount_percent, start_date, end_date, is_perpetual
FROM Promotions
WHERE is_active = true
  AND (is_perpetual = true OR (start_date <= CURRENT_DATE AND end_date >= CURRENT_DATE))
ORDER BY created_at DESC
LIMIT :pageSize OFFSET :offset;
```
<!--END_DDL_MIGRATION-->

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```json
{
  "endpoint": "POST /api/v1/promotions",
  "request_body": {
    "code": "SUMMER2024",
    "name": "Khuyến mãi hè 2024",
    "description": "Giảm 20% cho tất cả khóa học",
    "discountPercent": 20,
    "startDate": "2024-06-01",
    "endDate": "2024-08-31"
  },
  "response_201": {
    "promoId": "uuid",
    "code": "SUMMER2024",
    "isPerpetual": false,
    "createdAt": "2024-05-15T10:30:00Z"
  }
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 5.2: Xây dựng CRUD API cho Announcements

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/AnnouncementController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-018]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Triển khai REST controller tại đường dẫn `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/AnnouncementController.java` với các endpoints tương tự Promotion: GET `/api/v1/announcements` (lọc theo `targetAudience`, `activeOnly`, phân trang), GET `/{id}`, POST `/`, PUT `/{id}`, DELETE `/{id}`. Sử dụng `@Valid` cho `AnnouncementRequest`: `title` max 150 chars, `content` max 2000 chars, `expiryDate` optional, `targetAudience` thuộc tập {ALL, STUDENT, TEACHER, ADMIN}. Implement scheduled task với `@Scheduled(every = "1h")` chạy mỗi giờ để auto-hide announcement có `expiryDate < CURRENT_DATE` (set `is_active=false`). Inject `AnnouncementService`. Annotation `@RolesAllowed({"CENTER_ADMIN", "MANAGER", "SYSTEM_ADMIN"})`. Truy vết [REQ-018].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Auto-hide expired announcements thông qua scheduled task
UPDATE Announcements
SET is_active = false,
    updated_at = now()
WHERE expiry_date IS NOT NULL
  AND expiry_date < CURRENT_DATE
  AND is_active = true;
```
<!--END_DDL_MIGRATION-->

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```json
{
  "endpoint": "GET /api/v1/announcements",
  "query_params": {
    "targetAudience": "ALL|STUDENT|TEACHER|ADMIN",
    "activeOnly": true,
    "page": 0,
    "size": 20
  },
  "response_200": [
    {
      "announcementId": "uuid",
      "title": "Lịch nghỉ lễ 30/4",
      "content": "Trung tâm nghỉ lễ từ 30/4 đến 1/5",
      "startDate": "2024-04-25",
      "expiryDate": "2024-05-02",
      "isActive": true
    }
  ]
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 5.3: Kiểm thử cho PromotionService và AnnouncementService

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/PromotionService.java;./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/PromotionServiceTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-017], [REQ-018], [EXC-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh 2 test class JUnit 5 tại các đường dẫn `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/PromotionServiceTest.java` và `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/AnnouncementServiceTest.java`. Test PromotionService: (1) `testCreatePromotion_Success` tạo promotion hợp lệ, verify lưu DB và `isPerpetual=false` khi có `endDate`; (2) `testCreatePromotion_Perpetual` không truyền `endDate`, verify `isPerpetual=true`; (3) theo [EXC-004] `testCreatePromotion_DuplicateCode` trùng `code`, verify throw `DuplicatePromotionCodeException` với mã `DUPLICATE_PROMO_CODE_409`; (4) `testGetActivePromotions` chỉ trả về promotion còn hiệu lực; (5) `testCreatePromotion_InvalidDiscount` `discountPercent=0` hoặc `=101`, verify throw `ConstraintViolationException`. Test AnnouncementService: (1) `testCreateAnnouncement_Success`; (2) `testAutoHideExpired` insert announcement với `expiryDate=hôm qua`, chạy scheduled task, verify `is_active=false`; (3) `testGetByTargetAudience` lọc theo `targetAudience=STUDENT`; (4) `testCreateAnnouncement_InvalidTargetAudience` gửi giá trị không hợp lệ, verify throw exception. Truy vết [REQ-017], [REQ-018] và [EXC-004].

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```java
// Xử lý [EXC-004]: mã khuyến mãi trùng lặp
public class DuplicatePromotionCodeException extends RuntimeException {
    private final String code;
    public DuplicatePromotionCodeException(String code) {
        super("Mã khuyến mãi '" + code + "' đã tồn tại trong hệ thống");
        this.code = code;
    }
    public String getCode() { return code; }
}

// Ánh xạ sang HTTP 409 Conflict
@Provider
public class DuplicatePromotionCodeExceptionMapper implements ExceptionMapper<DuplicatePromotionCodeException> {
    @Override
    public Response toResponse(DuplicatePromotionCodeException ex) {
        return Response.status(Response.Status.CONFLICT)
            .entity(Map.of(
                "error", "DUPLICATE_PROMO_CODE_409",
                "message", ex.getMessage(),
                "code", ex.getCode()
            ))
            .build();
    }
}
```
<!--END_EXC_HANDLER-->

### 🌤️ NGÀY 6: <!--DAY_HEADER_START-->TRIỂN KHAI AI CHATBOT VỚI VERTEX AI GEMINI VÀ SESSION MANAGEMENT<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 6.1: Xây dựng Vertex AI Client cho Chatbot

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/integration/VertexAiClient.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-019]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo class `VertexAiClient` tại đường dẫn `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/integration/VertexAiClient.java` sử dụng Google Cloud Vertex AI Java SDK. Method `query(String question, String sessionContext)` gọi `PredictionServiceClient.predict()` với endpoint config cho Gemini model tại `projects/membership-hub/locations/asia-southeast1/publishers/google/models/gemini-pro`. Xây dựng prompt template bao gồm: system instruction (Membership Hub domain knowledge về khoá học, giáo viên, trung tâm, thẻ thành viên), conversation history từ `sessionContext` JSON, user question. Parse response trích xuất text answer và confidence score (0.0-1.0). Xử lý exception: `StatusRuntimeException` với code `UNAVAILABLE` throw `AiServiceUnavailableException`, code `DEADLINE_EXCEEDED` throw `AiTimeoutException`. Cấu hình timeout 30 giây, retry 1 lần cho lỗi transient. Inject API key/credentials qua MicroProfile Config `@ConfigProperty(name = "gcp.vertexai.credentials.path")`. Truy vết [REQ-019].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Lưu context của session để cung cấp cho AI trong các turn tiếp theo
UPDATE ChatbotSession
SET context = :contextJson,
    message_count = message_count + 1,
    last_activity_at = now()
WHERE session_id = :sessionId;
```
<!--END_DDL_MIGRATION-->

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```json
{
  "vertex_ai_request": {
    "endpoint": "projects/membership-hub/locations/asia-southeast1/publishers/google/models/gemini-pro",
    "instances": [{
      "prompt": "Bạn là trợ lý ảo của Membership Hub. Người dùng hỏi: {question}"
    }],
    "parameters": {
      "temperature": 0.2,
      "maxOutputTokens": 512,
      "topP": 0.8
    }
  },
  "vertex_ai_response": {
    "predictions": [{
      "content": "string",
      "confidence": "float"
    }]
  }
}
```
<!--END_API_CONTRACT-->

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```java
// Xử lý khi Vertex AI không khả dụng
public ChatbotResponse query(String question, String sessionContext) {
    try {
        PredictResponse response = predictionServiceClient.predict(endpointName, request);
        return parseResponse(response);
    } catch (StatusRuntimeException e) {
        if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
            throw new AiServiceUnavailableException("AI_SERVICE_UNAVAILABLE", e);
        } else if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
            throw new AiTimeoutException("AI_REQUEST_TIMEOUT", e);
        }
        throw e;
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 6.2: Xây dựng ChatbotService với session management

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/ChatbotService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-019], [EXC-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Triển khai `ChatbotService` tại đường dẫn `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/ChatbotService.java` với các method: (1) `createSession(UUID userId)` tạo `ChatbotSession` mới với `expiresAt = now + 30 phút`, sinh `sessionToken` UUID an toàn sử dụng `SecureRandom`; (2) `query(ChatbotQueryRequest request)` validate session token (throw `SessionExpiredException` theo [EXC-004] nếu quá hạn), gọi `VertexAiClient.query()`, lưu `ChatbotMessage` cho cả USER và BOT vào bảng `ChatbotMessage`, nếu `confidence < 0.6` thì set `escalatedToHuman=true` và thông báo `"Hệ thống sẽ chuyển câu hỏi tới nhân viên hỗ trợ"`, đồng thời publish sự kiện `chatbot-escalation` lên Kafka topic `notification-queue` cho Center Admin xử lý; (3) `cleanupExpiredSessions()` chạy scheduled task `@Scheduled(every = "5m")` xóa session quá hạn 24 giờ. Sử dụng `@Transactional` cho các thao tác DB. Truy vết [REQ-019] và [EXC-004].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Cleanup expired chatbot sessions chạy mỗi 5 phút
DELETE FROM ChatbotSession
WHERE expires_at < now() - INTERVAL '24' HOUR;
```
<!--END_DDL_MIGRATION-->

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```java
// Xử lý [EXC-004]: session hết hạn
public ChatbotResponse query(ChatbotQueryRequest request) {
    ChatbotSession session = sessionRepository.findByToken(request.getSessionToken())
        .orElseThrow(() -> new SessionNotFoundException("SESSION_NOT_FOUND"));

    if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
        throw new SessionExpiredException("CHATBOT_SESSION_EXPIRED");
    }

    ChatbotResponse response = vertexAiClient.query(request.getQuestion(), session.getContext());

    if (response.getConfidence() < 0.6) {
        response.setEscalated(true);
        response.setAnswer("Câu hỏi của bạn đang được chuyển tới nhân viên hỗ trợ.");
        notificationProducer.publishEscalation(session, request.getQuestion());
    }

    saveMessages(session, request.getQuestion(), response);
    return response;
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 6.3: Kiểm thử cho ChatbotService

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/ChatbotService.java;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/ChatbotServiceTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-019], [EXC-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh test class JUnit 5 tại đường dẫn `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/ChatbotServiceTest.java`. Test case 1: `testQuery_HighConfidence` mock `VertexAiClient` trả về `confidence=0.9`, verify response không escalated, message được lưu. Test case 2: `testQuery_LowConfidenceEscalation` mock `confidence=0.5`, verify `escalated=true` và answer thông báo chuyển nhân viên, Kafka event được publish. Test case 3 theo [EXC-004]: `testQuery_ExpiredSession` tạo session đã hết hạn, gọi query, verify throw `SessionExpiredException` với mã `CHATBOT_SESSION_EXPIRED`. Test case 4: `testQuery_AiUnavailable` mock `VertexAiClient` throw `AiServiceUnavailableException`, verify exception bubble up với mã `AI_SERVICE_UNAVAILABLE`. Test case 5: `testCreateSession_GenerateUniqueToken` tạo 100 session, verify tokens đều unique. Test case 6: `testQuery_NotFoundSession` gọi với `sessionToken` không tồn tại, verify throw `SessionNotFoundException` với mã `SESSION_NOT_FOUND`. Truy vết [REQ-019] và [EXC-004].

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```java
// Test xác minh [EXC-004] cho session hết hạn
@Test
void testQuery_ExpiredSession() {
    ChatbotSession expired = new ChatbotSession();
    expired.setExpiresAt(LocalDateTime.now().minusMinutes(5));
    when(sessionRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

    ChatbotQueryRequest request = new ChatbotQueryRequest();
    request.setSessionToken("expired-token");
    request.setQuestion("Hỏi gì đó");

    assertThrows(SessionExpiredException.class, () -> chatbotService.query(request));
}
```
<!--END_EXC_HANDLER-->

### 🌤️ NGÀY 7: <!--DAY_HEADER_START-->TÍCH HỢP MOBILE APP API, OFFLINE CACHE VÀ TÀI LIỆU HÓA OPENAPI CONTRACTS<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 7.1: Xây dựng API client cho mobile app

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/frontend/web-app/src/lib/api/membershipHubClient.ts`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-009]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh TypeScript class `MembershipHubClient` tại đường dẫn `./sources/frontend/web-app/src/lib/api/membershipHubClient.ts` sử dụng Axios. Cấu hình base URL từ `process.env.NEXT_PUBLIC_API_BASE_URL`. Implement request interceptor để tự động attach Bearer token từ localStorage key `auth_token`. Response interceptor xử lý 401 bằng cách gọi refresh token endpoint `/api/v1/auth/refresh` rồi retry request, nếu refresh fail thì redirect về trang login và xóa token khỏi localStorage. Methods: `getStudentCard(studentId)`, `renewCard(studentId, request)`, `dispatchNotification(request)`, `registerDevice(request)`, `queryChatbot(request)`. Sử dụng TypeScript generics cho response type safety. Xuất instance singleton `export const apiClient = new MembershipHubClient()`. Truy vết [ARC-009].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Frontend layer không trực tiếp truy cập DB
-- Tài liệu tham chiếu các endpoint backend đã thiết kế
```
<!--END_DDL_MIGRATION-->

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```typescript
// Type definitions cho API contracts
export interface StudentCardResponse {
  cardId: string;
  studentId: string;
  issueDate: string;
  endDate: string;
  totalValidityDays: number;
  usedDays: number;
  remainingDays: number;
  renewalCount: number;
}

export interface CardRenewalRequest {
  renewalDays: number;
  paymentReference: string;
}

export interface NotificationDispatchRequest {
  notificationType: 'PUSH' | 'ZALO_GROUP' | 'IN_APP';
  targetUserId?: string;
  targetGroupZalo?: string;
  messageTitle: string;
  messageBody: string;
  payload?: Record<string, unknown>;
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 7.2: Xây dựng offline cache với IndexedDB

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/frontend/web-app/src/lib/offline/cacheService.ts`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-009]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Triển khai `CacheService` tại đường dẫn `./sources/frontend/web-app/src/lib/offline/cacheService.ts` sử dụng IndexedDB qua thư viện `idb`. Cung cấp API: `cacheStudentCard(card: StudentCardResponse)`, `getCachedStudentCard(studentId: string)`, `cacheAnnouncements(list: AnnouncementResponse[])`, `getCachedAnnouncements()`, `queueOfflineRequest(request: QueuedRequest)`, `getQueuedRequests()`, `clearExpiredEntries()`. Sử dụng object store với index theo `studentId` và `timestamp`. Implement TTL logic - entries cũ hơn 24 giờ tự động bị xóa thông qua method `clearExpiredEntries()`. Background sync: khi online, lấy queued requests từ IndexedDB và gửi lại qua API thông qua `syncQueuedRequests()`. Lắng nghe sự kiện `online` của `window` để trigger sync. Truy vết [ARC-009].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Frontend layer không yêu cầu migration
-- Dữ liệu cache được lưu trong IndexedDB phía client
```
<!--END_DDL_MIGRATION-->

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```typescript
// Cache schema cho IndexedDB
interface CachedStudentCard {
  studentId: string;
  cardData: StudentCardResponse;
  cachedAt: number;
  expiresAt: number;
}

interface QueuedRequest {
  id: string;
  endpoint: string;
  method: 'POST' | 'PUT' | 'DELETE';
  payload: unknown;
  retryCount: number;
  createdAt: number;
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 7.3: Tài liệu hóa OpenAPI contracts cho Notification Queue và Mobile App

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/contracts/notification-queue.openapi.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-008], [ARC-009], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh file OpenAPI 3.1 YAML tại các đường dẫn `./sources/docs/contracts/notification-queue.openapi.yaml` và `./sources/docs/contracts/mobile-app.openapi.yaml` mô tả đầy đủ: (1) Endpoint `POST /api/v1/notifications/dispatch` với request schema, response 202 Accepted; (2) Endpoint `POST /api/v1/devices/register`; (3) Endpoint `POST /api/v1/chatbot/query`; (4) Endpoint `POST /api/v1/students/{id}/card/renew`; (5) Endpoint `GET /api/v1/students/{id}/card`. Bao gồm security scheme BearerAuth, error responses chuẩn (400, 401, 403, 404, 409, 503), ví dụ JSON cho mỗi response, mô tả luồng retry và dead letter queue theo [EXC-003]. Bổ sung section hướng dẫn tích hợp cho mobile team với curl examples và code snippets TypeScript sử dụng `MembershipHubClient`. Truy vết [ARC-008], [ARC-009] và [DOC-001].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Tài liệu tham chiếu các bảng:
-- StudentCards, CardRenewalHistory, Promotions, Announcements,
-- NotificationDispatch, DeviceToken, ChatbotSession, ChatbotMessage
-- Phần này chỉ liệt kê tham chiếu, không sinh migration mới
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 7.4: Kiểm thử frontend cho notification module

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/frontend/web-app/src/test/notifications.spec.ts
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-008], [ARC-009]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh test file tại đường dẫn `./sources/frontend/web-app/src/test/notifications.spec.ts` sử dụng Jest và React Testing Library. Test case 1: `testNotificationsLoad_OnMount` render component, mock API success, verify danh sách notification hiển thị đầy đủ thông tin. Test case 2: `testOfflineFallback` giả lập mất mạng thông qua `navigator.onLine = false`, verify component hiển thị dữ liệu từ `cacheService`. Test case 3: `testQueueOfflineRequest` khi offline, gọi `dispatchNotification`, verify request được queue trong IndexedDB. Test case 4: `testBackgroundSync` khi online trở lại `navigator.onLine = true`, dispatch event `online`, verify queued requests được gửi lại qua API. Sử dụng `fake-indexeddb` để mock IndexedDB trong test. Bổ sung test cho `cacheService.spec.ts` tại `./sources/frontend/web-app/src/test/cacheService.spec.ts` kiểm tra TTL 24 giờ, clearExpiredEntries, getCachedStudentCard trả về null khi không có. Truy vết [ARC-008] và [ARC-009].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Test scope frontend không yêu cầu migration backend
-- Dữ liệu test sử dụng mock data
```
<!--END_DDL_MIGRATION-->