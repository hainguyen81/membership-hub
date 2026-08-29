# Giai đoạn 4: <!--PHASE_NAME_START-->Tích Hợp Thông Báo, Báo Cáo, Đa Ngôn Ngữ Và Di Động<!--PHASE_NAME_END-->

## 📊 Kiểm Soát Tài Liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Bản Thiết Kế** | ARCH-20260829125322 |
| **Tên Dự Án** | membership-hub |
| **Giai đoạn** | 4 |
| **Tên Giai Đoạn** | <!--PHASE_NAME_START-->Tích Hợp Thông Báo, Báo Cáo, Đa Ngôn Ngữ Và Di Động<!--PHASE_NAME_END--> |
| **Mô Tả** | <!--PHASE_DESC_START-->Giai đoạn 4 tập trung kỹ thuật hóa toàn bộ luồng nghiệp vụ hậu điểm danh và trải nghiệm người dùng cuối của hệ thống Membership Hub, bao gồm quản lý thẻ thành viên với cơ chế gia hạn 1-365 ngày qua cổng thanh toán, hệ thống thông báo đa kênh FCM/APNs/Zalo với cơ chế retry tối đa 3 lần, quản lý khuyến mãi và thông báo quảng bá có hiệu lực theo thời hạn, tích hợp chatbot AI chăm sóc khách hàng với cơ chế leo thang, tối ưu hóa giao diện di động responsive theo vai trò kèm đăng ký device token, quốc tế hóa SEO đa ngôn ngữ en/vi/es với thẻ hreflang, báo cáo CSV điểm danh theo trung tâm kết hợp dashboard thời gian thực với chu kỳ làm mới cấu hình được, cùng cơ chế xử lý FIFO khi khôi phục dịch vụ sau sự cố đảm bảo không mất yêu cầu đang chờ. Toàn bộ tài sản mã nguồn, hợp đồng API, tài liệu kiến trúc và bộ xử lý ngoại lệ bản địa hóa tiếng Việt được kiến lập và truy vết đầy đủ bằng hệ thống thẻ TagID chuẩn doanh nghiệp.<!--PHASE_DESC_END--> |
| **Phiên Bản** | 1.0 (Đường Cơ Sở) |
| **Thời Gian** | 2026/08/29 12:53:22 |
| **Tác Giả** | Kiến Trúc Sư Hệ Thống Doanh Nghiệp (SA Agent) |
| **Phê Duyệt** | Đang Chờ Đánh Giá Quản Trị Kỹ Thuật |

## 1. Phạm Vi Hoạt Động & Mục Tiêu Giai Đoạn

Giai đoạn 4 thực hiện tám nhiệm vụ cốt lõi được phân bổ theo bảng tóm tắt đa giai đoạn: Nhiệm vụ 8 (quản lý thẻ thành viên và gia hạn), Nhiệm vụ 9 (hệ thống thông báo đa kênh), Nhiệm vụ 10 (quản lý khuyến mãi và thông báo quảng bá), Nhiệm vụ 11 (tích hợp chatbot AI chăm sóc khách hàng), Nhiệm vụ 12 (giao diện di động đa vai trò và thông báo đẩy), Nhiệm vụ 13 (quốc tế hóa và SEO đa ngôn ngữ), Nhiệm vụ 14 (báo cáo và bảng điều khiển phân tích), Nhiệm vụ 15 (khôi phục hệ thống sau sự cố và xử lý hàng đợi). Phạm vi kéo dài từ Ngày 1 đến Ngày 4 theo phân bổ chính thức trong ma trận tóm tắt đa giai đoạn.

Các tài sản kỹ thuật bắt buộc phải sinh ra bao gồm: module quản lý thẻ thành viên với `MembershipCardController` cung cấp endpoint `GET /api/v1/student-cards/me` và `POST /api/v1/student-cards/renew`; module thông báo đa kênh với `NotificationController`, `NotificationDispatchService`, `FcmApnsGatewayClient` và `ZaloGroupWebhookClient` hỗ trợ retry 3 lần theo cơ chế exponential backoff; module khuyến mãi và thông báo với `PromotionController` và `AnnouncementController` xử lý CRUD và lọc theo trạng thái hiệu lực; tích hợp chatbot AI với `AiChatbotClient` sử dụng MicroProfile Rest Client; giao diện di động Next.js với layout theo vai trò, i18n middleware phát hiện locale, sitemap đa ngôn ngữ; module báo cáo với `AttendanceReportController` xuất CSV và `EnrollmentDashboardController` cung cấp dashboard thời gian thực với chu kỳ làm mới cấu hình được; cơ chế xử lý FIFO thông qua hàng đợi bền vững khi khôi phục dịch vụ. Toàn bộ tài sản phải được gắn thẻ truy xuất theo hệ thống TagID `[REQ-014]`, `[REQ-015]`, `[REQ-016]`, `[REQ-017]`, `[REQ-018]`, `[REQ-019]`, `[REQ-020]`, `[REQ-021]`, `[REQ-022]`, `[REQ-023]`, `[REQ-024]`, `[REQ-025]`, `[ARC-008]`, `[ARC-009]`, `[NFR-007]`, `[EXC-003]`, `[EXC-005]` để đảm bảo khả năng truy vết đầy đủ.

## 2. Phạm Vi Kỹ Thuật Cho Phép & Ranh Giới Thư Mục

Danh sách tệp vật lý và điểm cuối được phép sinh ra trong giai đoạn này:

* `./sources/backend/notification-service/pom.xml` [ARC-000]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/controller/NotificationController.java` [REQ-016], [REQ-021], [ARC-008]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/service/NotificationDispatchService.java` [REQ-016], [REQ-021], [EXC-003]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/integration/FcmApnsGatewayClient.java` [REQ-021]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/integration/ZaloGroupWebhookClient.java` [REQ-016], [ARC-008]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/dto/NotificationDispatchRequest.java` [REQ-016], [ARC-008]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/dto/NotificationDispatchResponse.java` [REQ-016]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/exception/NotificationDeliveryException.java` [REQ-016], [EXC-003]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/exception/InvalidNotificationTargetException.java` [REQ-016]
* `./sources/backend/reporting-service/pom.xml` [ARC-000]
* `./sources/backend/reporting-service/src/main/java/org/nlh4j/membershiphub/reportingservice/controller/AttendanceReportController.java` [REQ-024]
* `./sources/backend/reporting-service/src/main/java/org/nlh4j/membershiphub/reportingservice/controller/EnrollmentDashboardController.java` [REQ-025]
* `./sources/backend/reporting-service/src/main/java/org/nlh4j/membershiphub/reportingservice/service/AttendanceCsvExportService.java` [REQ-024]
* `./sources/backend/reporting-service/src/main/java/org/nlh4j/membershiphub/reportingservice/service/DashboardMetricsService.java` [REQ-025]
* `./sources/backend/reporting-service/src/main/java/org/nlh4j/membershiphub/reportingservice/dto/AttendanceReportRow.java` [REQ-024]
* `./sources/backend/reporting-service/src/main/java/org/nlh4j/membershiphub/reportingservice/dto/DashboardSummaryResponse.java` [REQ-025]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/controller/MembershipCardController.java` [REQ-014], [REQ-015]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/StudentCardRenewalService.java` [REQ-015]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/MembershipCardQueryService.java` [REQ-014]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/MembershipCardResponse.java` [REQ-014]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/RenewalRequest.java` [REQ-015]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/exception/InvalidRenewalPeriodException.java` [REQ-015]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/exception/PaymentGatewayFailedException.java` [REQ-015]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/PromotionController.java` [REQ-017]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/AnnouncementController.java` [REQ-018]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/PromotionService.java` [REQ-017]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/AnnouncementService.java` [REQ-018]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/PromotionRequest.java` [REQ-017]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/AnnouncementRequest.java` [REQ-018]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/exception/DuplicatePromotionCodeException.java` [REQ-017]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/exception/InvalidPromotionPeriodException.java` [REQ-017]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/integration/AiChatbotClient.java` [REQ-019]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/ChatbotController.java` [REQ-019]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/ChatbotQuestionRequest.java` [REQ-019]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/ChatbotResponse.java` [REQ-019]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/ChatbotUnavailableException.java` [REQ-019]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/recovery/OutboxRelayScheduler.java` [EXC-001], [EXC-005], [REQ-012]
* `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/recovery/RecoveryNotificationService.java` [EXC-005]
* `./sources/frontend/web-app/package.json` [REQ-020], [REQ-022], [REQ-023]
* `./sources/frontend/web-app/src/app/[locale]/layout.tsx` [REQ-022], [REQ-023]
* `./sources/frontend/web-app/src/app/[locale]/dashboard/membership-card/page.tsx` [REQ-014]
* `./sources/frontend/web-app/src/app/[locale]/dashboard/reports/page.tsx` [REQ-024], [REQ-025]
* `./sources/frontend/web-app/src/app/sitemap.xml/route.ts` [REQ-023]
* `./sources/frontend/web-app/src/i18n/en/common.json` [REQ-022], [REQ-023], [NFR-007]
* `./sources/frontend/web-app/src/i18n/vi/common.json` [REQ-022], [REQ-023], [NFR-007]
* `./sources/frontend/web-app/src/i18n/es/common.json` [REQ-022], [REQ-023], [NFR-007]
* `./sources/frontend/web-app/src/middleware.ts` [REQ-022]
* `./sources/frontend/web-app/src/components/mobile/RoleBasedNavigation.tsx` [REQ-020]
* `./sources/frontend/web-app/src/components/notifications/NotificationCenter.tsx` [REQ-021]
* `./sources/frontend/web-app/src/components/promotion/PromotionBanner.tsx` [REQ-017]
* `./sources/frontend/web-app/src/components/announcement/AnnouncementModal.tsx` [REQ-018]
* `./sources/frontend/web-app/src/components/chatbot/ChatWidget.tsx` [REQ-019]
* `./sources/docs/architecture/notifications-fcm-apns.md` [DOC-001], [ARC-008]
* `./sources/docs/architecture/disaster-recovery-fifo.md` [DOC-001], [EXC-005]
* `./sources/docs/architecture/i18n-seo-strategy.md` [DOC-001], [REQ-022], [REQ-023]
* `./sources/docs/contracts/notification-openapi.yaml` [REQ-016], [REQ-021], [ARC-008]
* `./sources/docs/contracts/reporting-openapi.yaml` [REQ-024], [REQ-025]
* `./sources/docs/contracts/membership-card-openapi.yaml` [REQ-014], [REQ-015]
* `./sources/docs/diagrams/notification-flow.mmd` [ARC-008], [REQ-016]
* `./sources/docs/diagrams/recovery-fifo-flow.mmd` [EXC-005]

* **RÀNG BUỘC BẮT BUỘC VỀ BIỂU MẪU NỀN TẢNG**:
  - Cấu trúc package Java phải tuân thủ nghiêm ngặt quy ước `org.nlh4j.membershiphub.<tên-dịch-vụ>` cho mọi tệp nguồn backend.
  - Mọi thay đổi schema cơ sở dữ liệu phải thông qua tập tin migration Flyway versioned, cấm sửa đổi trực tiếp.
  - Tất cả REST endpoint phải khai báo JSON contract rõ ràng với request/response schema và HTTP status code tiêu chuẩn.
  - Cam kết OWASP Top 10: chuẩn bị câu lệnh parameterized chống SQL injection, escape output chống XSS, CSRF token cho form giao dịch.

## 3. Chỉ Thị Chức Năng Chuyên Biệt Cho Tác Nhân Phụ

*   **Coder**: Đóng vai trò Nhà Phát Triển Ứng Dụng Cao Cấp. Chịu trách nhiệm triển khai mã nguồn ứng dụng thuần túy trên các dịch vụ backend `notification-service`, `reporting-service`, `attendance-service`, `center-service`, `user-service` và lớp frontend Next.js, bao gồm controller REST, service nghiệp vụ, DTO, bộ xử lý ngoại lệ bản địa hóa, client tích hợp bên ngoài (FCM, APNs, Zalo, AI), và các thành phần React cho giao diện di động responsive. Bị cấm viết bộ kiểm thử hoặc biểu mẫu hạ tầng.

* **Tester**: Đóng vai trò Trưởng Nhóm Kiểm Thử/Đảm Bảo Chất Lượng. Chuyên về kỹ thuật bộ kiểm thử, xác thực và cổng chất lượng. Chịu trách nhiệm sinh JUnit, kiểm thử tích hợp với Testcontainers, kiểm thử đơn vị với Mockito, kiểm thử tham số hóa và kịch bản xác thực idempotency. Bị cấm sửa đổi mã nguồn sản phẩm. Nếu nhiệm vụ phụ liên quan đến phạm vi tích hợp tổng thể hoặc đầu-cuối mà không thể khoanh vùng một tệp mã nguồn cụ thể, phải xuất chính xác chuỗi ký tự `INTEGRATION_SCOPE` làm tham số đầu tiên của cặp phân tách bằng dấu chấm phẩy.

* **Doc**: Đóng vai trò Chuyên Viên Viết Tài Liệu Kỹ Thuật và Kiến Trúc Sư Hệ Thống Doanh Nghiệp. Chuyên về biên soạn tài liệu đặc tả kỹ thuật toàn diện, hợp đồng OpenAPI 3.0.3, sơ đồ Mermaid mô tả luồng nghiệp vụ, checklist review và danh mục kiến trúc doanh nghiệp phù hợp với các lớp topology dự án đang hoạt động. Mỗi tệp tài liệu kỹ thuật được sinh ra phải được liệt kê dưới dạng thực thể đường dẫn tệp rõ ràng kết thúc bằng phần mở rộng `.md` hoặc `.yaml` hoặc `.mmd` và nằm hoàn toàn trong sơ đồ lưu trữ tập trung: `./sources/docs/`.

*   **Reviewer**: Chịu trách nhiệm xác minh trình biên dịch, cổng phân tích tĩnh và vá lỗi phòng thủ. Chuyên về đánh giá chất lượng mã, giải quyết lỗi biên dịch, sửa lỗ hổng bảo mật OWASP và xử lý các blocker cổng chất lượng SonarQube. Đánh giá tuân thủ quy ước RBAC, biên giới transaction, validation đầu vào và che giấu dữ liệu nhạy cảm.

*   **Docker**: Chuyên biệt về container hóa, kỹ thuật Dockerfile đa giai đoạn, tối ưu gói và đẩy tài sản ảnh ứng dụng đã xác minh lên DockerHub.

*   **GCP**: Chuyên về tự động hóa đám mây trong Google Cloud Platform. Chịu trách nhiệm xây dựng và đẩy ảnh lên Google Cloud Artifact Registry (GCR), điều phối môi trường container nguyên bản trên Google Cloud Run.

*   **GKE**: Chuyên về điều phối container sản xuất bên trong Google Kubernetes Engine. Chịu trách nhiệm xây dựng manifest triển khai Kubernetes, điều khiển định tuyến, cấu hình HPA, biểu đồ Helm và triển khai tải công việc microservice vào cụm GKE đang hoạt động.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn

Giai đoạn 4 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: một trăm phần trăm endpoint quản lý thẻ thành viên (`GET /api/v1/student-cards/me`, `POST /api/v1/student-cards/renew`) hoạt động đúng theo đặc tả với xác thực `renewalDays` từ 1-365 và xử lý lỗi cổng thanh toán; hệ thống thông báo đa kênh với `POST /api/v1/notifications/dispatch` hỗ trợ FCM/APNs/Zalo kèm cơ chế retry tối đa 3 lần với thời gian chờ theo cấp số nhân; CRUD khuyến mãi và thông báo hoạt động đúng với bộ lọc `endDate` hết hạn và xử lý mã khuyến mãi trùng lặp; chatbot AI endpoint `POST /api/v1/chatbot/ask` hoạt động với cơ chế leo thang khi độ tin cậy thấp; giao diện di động responsive theo vai trò với layout đa ngôn ngữ; middleware i18n phát hiện locale chính xác theo thứ tự ưu tiên cookie, Accept-Language, mặc định; sitemap đa ngôn ngữ với thẻ hreflang; báo cáo CSV điểm danh và dashboard thời gian thực với chu kỳ làm mới cấu hình được; cơ chế xử lý FIFO khi khôi phục dịch vụ với thông báo sự kiện đã khôi phục; bộ kiểm thử đơn vị và tích hợp phủ sóng tối thiểu 85 phần trăm các luồng nghiệp vụ trọng yếu; ba hợp đồng OpenAPI YAML được soạn thảo đầy đủ với đặc tả schema và response codes; sơ đồ Mermaid mô tả luồng thông báo và khôi phục. Một trăm phần trăm mã TagID được phân bổ cho giai đoạn 4 phải được ánh xạ chính xác trong báo cáo đánh giá cuối giai đoạn.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->KHỞI TẠO DESCRIPTOR VÀ BỘ XỬ LÝ NGOẠI LỆ TẬP TRUNG CHO HẠ TẦNG PHỤ TRỢ<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 1.1: Khởi tạo descriptor Maven cho notification-service và reporting-service
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/notification-service/pom.xml

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Khởi tạo descriptor Maven cho module `notification-service` thừa kế từ `pom.xml` gốc. Khai báo `<groupId>org.nlh4j.membershiphub.notificationservice</groupId>`, `<artifactId>notification-service</artifactId>`. Bao gồm các dependency Quarkus 3.15.1: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-messaging-kafka` cho việc consume sự kiện từ `course-service` và `attendance-service`, `quarkus-rest-client` cho FCM/APNs/Zalo, `quarkus-smallrye-health`. Tích hợp plugin `quarkus-maven-plugin` với các goal `build`, `generate-code`, `generate-code-tests`. Cấu hình `quarkus.hibernate-orm.database.generation=validate` để buộc sử dụng Flyway migrations. Tất cả identifier ở dạng chữ thường alphanumeric, không chứa ký tự `-` hoặc `_`.

#### 📝 NHIỆM VỤ PHỤ 1.2: Khởi tạo descriptor Maven cho reporting-service
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/reporting-service/pom.xml

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Khởi tạo descriptor Maven cho module `reporting-service` thừa kế từ `pom.xml` gốc. Khai báo `<groupId>org.nlh4j.membershiphub.reportingservice</groupId>`, `<artifactId>reporting-service</artifactId>`. Bao gồm các dependency Quarkus 3.15.1: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-scheduler` cho refresh dashboard tự động, `quarkus-smallrye-health`. Cấu hình `quarkus.hibernate-orm.database.generation=validate` để buộc sử dụng Flyway migrations. Tích hợp plugin `quarkus-maven-plugin` chuẩn.

#### 📝 NHIỆM VỤ PHỤ 1.3: Khởi tạo descriptor frontend và bổ sung dependency i18n, chatbot
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/frontend/web-app/package.json

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [REQ-020]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Cập nhật tệp `./sources/frontend/web-app/package.json` của ứng dụng Next.js. Bổ sung dependencies mới cho giai đoạn 4: `next-intl@3.20.0` để quản lý đa ngôn ngữ, `zustand@4.5.4` cho state management phía client, `react-qr-scanner@1.0.0-alpha.11` cho chức năng quét QR, `@react-oauth/google@0.12.1` cho OAuth Google, `socket.io-client@4.7.5` cho giao tiếp thời gian thực, `recharts@2.13.0` cho biểu đồ dashboard. Bổ sung devDependencies: `vitest@2.0.5` và `@testing-library/react@16.0.0` cho kiểm thử component. Đảm bảo phiên bản Next.js 14.2.15, React 18.3.1 và TypeScript 5.6.3 được giữ nguyên theo đường cơ sở.

#### 📝 NHIỆM VỤ PHỤ 1.4: Soạn thảo tài liệu blueprint kiến trúc cho giai đoạn 4
##### Tác Nhân Được Phân Côn: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/architecture/phase-4-blueprint.md

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-008], [ARC-009], [REQ-024], [REQ-025]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Soạn thảo tài liệu blueprint kiến trúc tại `./sources/docs/architecture/phase-4-blueprint.md` mô tả tổng quan giai đoạn 4. Tài liệu phải bao gồm: sơ đồ Mermaid `graph LR` mô tả topology triển khai với sáu vi dịch vụ (notification, reporting, attendance, center, user) và lớp frontend Next.js; bảng ma trận ánh xạ 17 mã TagID từ `[REQ-014]` đến `[EXC-005]` với thành phần kiến trúc tương ứng; sơ đồ tuần tự cho luồng thông báo đa kênh từ sự kiện nghiệp vụ đến FCM/APNs/Zalo; sơ đồ tuần tự cho luồng gia hạn thẻ thành viên từ phía client qua cổng thanh toán; mô tả chi tiết cơ chế xử lý FIFO khi khôi phục dịch vụ thông qua bảng `attendance_outbox`; checklist tuân thủ OWASP Top 10 cho từng module nghiệp vụ. Tài liệu sử dụng ngôn ngữ tiếng Việt cho phần mô tả, giữ nguyên tên thực thể và TagID ở dạng Technical English.

#### 📝 NHIỆM VỤ PHỤ 1.5: Kiểm thử tích hợp build Maven đa mô-đun cho giai đoạn 4
##### Tác Nhân Được Phân Côn: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/infra/test/maven-build-integration.sh

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [REQ-016], [REQ-024]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo script bash tại `./sources/infra/test/maven-build-integration.sh` mở rộng từ giai đoạn 1 để xác minh khả năng biên dịch đa mô-đun cho tất cả sáu vi dịch vụ bao gồm notification-service và reporting-service vừa khởi tạo. Script thực thi `mvn clean validate` tại `./sources/backend/`, kiểm tra sự tồn tại của descriptor `pom.xml` cho từng vi dịch vụ, xác minh dependency resolution từ BOM Quarkus 3.15.1 không có xung đột. Thoát với mã 0 nếu thành công, mã khác 0 nếu thất bại. In log rõ ràng cho từng vi dịch vụ.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->QUẢN LÝ THẺ THÀNH VIÊN, GIA HẠN VÀ HỆ THỐNG THÔNG BÁO ĐA KÊNH<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 2.1: Triển khai MembershipCardController và MembershipCardQueryService
##### Tác Nhân Được Phân Côn: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/controller/MembershipCardController.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-014], [REQ-015]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng class `@Path("/api/v1/student-cards")` `MembershipCardController` trong package `org.nlh4j.membershiphub.attendanceservice` inject `MembershipCardQueryService` và `StudentCardRenewalService`. Endpoint `GET /me` với annotation `@RolesAllowed({"Student"})` trích `studentId` từ JWT subject thông qua `@Context SecurityContext`; trả về `MembershipCardResponse` với response 200 chứa `cardId`, `studentId`, `issueDate`, `validityDays`, `remainingDays`, `status` và `endDate`. Endpoint `POST /renew` nhận `RenewalRequest` với `@Valid`; trả về 200 với `newEndDate` và `newRemainingDays` hoặc 402 với mã `PAYMENT_GATEWAY_FAILED` khi cổng thanh toán từ chối. Áp dụng `@Transactional` cho endpoint renew. Tích hợp `NotificationDispatchService` để gửi thông báo xác nhận gia hạn.

* **Phase Localized Exception Handlers [EXC-005]:** <!--START_EXC_HANDLER-->
```json
{
  "PAYMENT_GATEWAY_FAILED_HTTP_402": "Trả message 'Cổng thanh toán từ chối giao dịch, thẻ chưa được gia hạn' khi nhận phản hồi lỗi từ payment provider",
  "INVALID_RENEWAL_PERIOD_HTTP_400": "Trả message 'Số ngày gia hạn phải nằm trong khoảng 1-365' khi renewalDays ngoài phạm vi cho phép",
  "FIFO_PROCESSING_RECOVERY": "Đảm bảo giao dịch gia hạn nằm trong hàng đợi khi dịch vụ khôi phục được xử lý đúng thứ tự, tránh gia hạn hai lần"
}
```
<!--END_EXC_HANDLER-->

* **API and Event Routing Contracts [REQ-014], [REQ-015]:** <!--START_API_CONTRACT-->
```json
{
  "GET /api/v1/student-cards/me": {
    "description": "Lấy thông tin thẻ thành viên hiện tại của học viên",
    "response_200": {
      "cardId": "uuid",
      "studentId": "uuid",
      "issueDate": "YYYY-MM-DD",
      "endDate": "YYYY-MM-DD",
      "validityDays": 365,
      "remainingDays": 120,
      "status": "ACTIVE"
    }
  },
  "POST /api/v1/student-cards/renew": {
    "request": {
      "renewalDays": 30,
      "paymentTransactionId": "tx_abc123"
    },
    "response_200": {
      "cardId": "uuid",
      "newEndDate": "YYYY-MM-DD",
      "newRemainingDays": 150,
      "status": "ACTIVE"
    },
    "response_402": {
      "code": "PAYMENT_GATEWAY_FAILED",
      "message": "Cổng thanh toán từ chối giao dịch"
    }
  }
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 2.2: Triển khai StudentCardRenewalService với xử lý race condition
##### Tác Nhân Được Phân Côn: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/StudentCardRenewalService.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-015]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng class `@ApplicationScoped` `StudentCardRenewalService` trong package `org.nlh4j.membershiphub.attendanceservice` inject `EntityManager` và `PaymentGatewayClient`. Phương thức `renew(UUID studentId, int renewalDays, String paymentTransactionId)` thực hiện: (1) validate `renewalDays` nằm trong khoảng 1-365, nếu không ném `InvalidRenewalPeriodException`; (2) tìm `StudentCard` theo `studentId` với `@Version` để áp dụng optimistic locking; (3) gọi `PaymentGatewayClient.confirm(paymentTransactionId)`; (4) nếu thành công, cập nhật `endDate = endDate.plusDays(renewalDays)`, `remainingDays = remainingDays + renewalDays`; (5) nếu thất bại, ném `PaymentGatewayFailedException` với HTTP 402 và KHÔNG cập nhật `endDate`. Sử dụng `@Transactional(REQUIRES_NEW)` để đảm bảo giao dịch nguyên tử. Inject `NotificationDispatchService` để phát thông báo xác nhận sau khi gia hạn thành công.

* **Phase Localized Exception Handlers [EXC-005]:** <!--START_EXC_HANDLER-->
```json
{
  "OPTIMISTIC_LOCK_CONFLICT": "Ném ConcurrentModificationException khi hai yêu cầu gia hạn đồng thời, HTTP 409 với message 'Thẻ đang được cập nhật, vui lòng thử lại'",
  "PAYMENT_REVERSAL": "Đảo ngược giao dịch khi DB write thất bại sau khi payment gateway xác nhận thành công"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 2.3: Kiểm thử tích hợp cho MembershipCardController
##### Tác Nhân Được Phân Côn: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/controller/MembershipCardController.java;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/controller/MembershipCardControllerTest.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-014], [REQ-015]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo lớp `MembershipCardControllerTest` sử dụng `@QuarkusTest` với RestAssured. Các test case: (1) `testGetCard_Success` xác minh response 200 với đầy đủ trường và `remainingDays` đúng; (2) `testGetCard_WrongRole` với JWT giả lập vai trò Teacher, expect HTTP 403; (3) `testRenewCard_InvalidPeriod` với `renewalDays = 0` hoặc `> 365`, expect HTTP 400 với mã `INVALID_RENEWAL_PERIOD`; (4) `testRenewCard_PaymentFailed` sử dụng `@InjectMock PaymentGatewayClient` trả về lỗi, expect HTTP 402 và xác minh `endDate` không thay đổi thông qua query trực tiếp; (5) `testRenewCard_Success` xác minh `endDate` được cộng đúng số ngày và notification được publish. Sử dụng `TestTransaction` để rollback dữ liệu sau mỗi test.

#### 📝 NHIỆM VỤ PHỤ 2.4: Triển khai NotificationController và NotificationDispatchService
##### Tác Nhân Được Phân Côn: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/controller/NotificationController.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-016], [REQ-021], [ARC-008]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng class `@Path("/api/v1/notifications")` `NotificationController` trong package `org.nlh4j.membershiphub.notificationservice` inject `NotificationDispatchService`. Endpoint `POST /dispatch` với annotation `@RolesAllowed({"SystemAdmin", "CenterAdmin", "Manager"})` nhận `NotificationDispatchRequest` chứa `userId` (optional UUID), `groupZalo` (optional), `message` (required max 1000), `mediaUrl` (optional HTTPS), `priority` (enum HIGH/NORMAL/LOW). Trả về response 202 với `notificationId` và `status` là `QUEUED` ngay lập tức vì quá trình gửi là bất đồng bộ. Áp dụng `@Valid` cho request body. Endpoint `GET /history` với `@QueryParam("userId")` trả về danh sách thông báo đã gửi với response 200.

* **API and Event Routing Contracts [REQ-016], [REQ-021], [ARC-008]:** <!--START_API_CONTRACT-->
```json
{
  "POST /api/v1/notifications/dispatch": {
    "request": {
      "userId": "uuid (optional)",
      "groupZalo": "string (optional)",
      "message": "string (required, max 1000)",
      "mediaUrl": "string (optional, must be HTTPS)",
      "priority": "HIGH | NORMAL | LOW"
    },
    "response_202": {
      "notificationId": "uuid",
      "status": "QUEUED"
    }
  },
  "GET /api/v1/notifications/history": {
    "parameters": {
      "userId": "uuid"
    },
    "response_200": {
      "items": [
        {
          "notificationId": "uuid",
          "channel": "PUSH | ZALO | EMAIL | SMS",
          "message": "string",
          "status": "PENDING | SENT | FAILED | DELIVERED",
          "sentAt": "ISO-8601",
          "retryCount": 0
        }
      ]
    }
  }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-003]:** <!--START_EXC_HANDLER-->
```json
{
  "NOTIFICATION_DELIVERY_FAILED": "Ghi log cảnh báo, kích hoạt retry queue với thời gian chờ exponential backoff (1s, 5s, 30s), tối đa 3 lần theo EXC-003",
  "INVALID_NOTIFICATION_TARGET": "HTTP 400 khi cả userId và groupZalo đều null, message 'Phải cung cấp userId hoặc groupZalo'"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 2.5: Triển khai FcmApnsGatewayClient với circuit breaker
##### Tác Nhân Được Phân Côn: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/integration/FcmApnsGatewayClient.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-021]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng class `@ApplicationScoped` `FcmApnsGatewayClient` trong package `org.nlh4j.membershiphub.notificationservice.integration` sử dụng MicroProfile Rest Client. Khai báo annotation `@RegisterRestClient(baseUri = "https://fcm.googleapis.com")` với interface định nghĩa các endpoint FCM v1 API. Cấu hình timeout 5 giây cho mỗi yêu cầu thông qua `quarkus.rest-client.fcm.url` và `quarkus.rest-client.fcm.connect-timeout=5000`. Phương thức `sendPush(String deviceToken, String title, String body, Map<String,String> data)` thực hiện POST tới `/v1/projects/{projectId}/messages:send` với payload JSON đúng chuẩn FCM v1. Áp dụng annotation `@CircuitBreaker(requestVolumeThreshold=4, failureRatio=0.5, delay=10000)` để tránh lũ lụt yêu cầu khi FCM/APNs ngừng hoạt động. Tích hợp `@Retry(maxRetries=3, delay=1000, jitter=500)` cho cơ chế retry theo `EXC-003`.

* **Phase Localized Exception Handlers [EXC-003]:** <!--START_EXC_HANDLER-->
```json
{
  "FCM_DELIVERY_FAILED": "Ném NotificationDeliveryException sau khi retry hết 3 lần, HTTP 503 với message 'Dịch vụ thông báo tạm thời không khả dụng'",
  "CIRCUIT_BREAKER_OPEN": "Trả fail-fast response khi circuit breaker mở, log cảnh báo và lưu vào retry queue"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 2.6: Triển khai ZaloGroupWebhookClient
##### Tác Nhân Được Phân Côn: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/integration/ZaloGroupWebhookClient.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-016], [ARC-008]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng class `@ApplicationScoped` `ZaloGroupWebhookClient` trong package `org.nlh4j.membershiphub.notificationservice.integration` sử dụng `java.net.http.HttpClient` để gọi Zalo OA API. Cấu hình endpoint `https://openapi.zalo.me/v2.0/oa/message/cs` với access token lấy từ biến môi trường `ZALO_OA_ACCESS_TOKEN`. Phương thức `postToGroup(String groupId, String message, String mediaUrl)` xây dựng payload JSON theo đặc tả Zalo OA API, thực hiện POST request với timeout 5 giây, xử lý response code 200 thành công và các mã lỗi 4xx/5xx bằng cách ném `NotificationDeliveryException` để kích hoạt cơ chế retry. Tích hợp `@CircuitBreaker` tương tự FCM client.

#### 📝 NHIỆM VỤ PHỤ 2.7: Kiểm thử đơn vị cho NotificationDispatchService với cơ chế retry
##### Tác Nhân Được Phân Côn: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/service/NotificationDispatchService.java;./sources/backend/notification-service/src/test/java/org/nlh4j/membershiphub/notificationservice/service/NotificationDispatchServiceTest.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-016], [REQ-021], [EXC-003]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo lớp `NotificationDispatchServiceTest` với JUnit 5 và Mockito. Sử dụng `@InjectMock FcmApnsGatewayClient` và `@InjectMock ZaloGroupWebhookClient` để mô phỏng hành vi. Test case: (1) `testDispatch_Success` xác minh cả hai client được gọi và notification được lưu với status `SENT`; (2) `testDispatch_FcmFailsAllRetries` mô phỏng `IOException` ở cả ba lần retry, xác minh `Mockito.verify(fcmClient, Mockito.times(3))` được gọi và notification cuối cùng có status `FAILED` với `retryCount=3`; (3) `testDispatch_RetryThenSuccess` mô phỏng lỗi ở hai lần đầu và thành công ở lần thứ ba, xác minh status cuối cùng là `SENT`; (4) `testDispatch_CircuitBreakerOpens` sử dụng `Resilience4j` test utilities để xác minh khi tỷ lệ lỗi vượt ngưỡng, circuit breaker mở và các yêu cầu tiếp theo fail-fast.

#### 📝 NHIỆM VỤ PHỤ 2.8: Đánh giá mã nguồn module thẻ thành viên và thông báo
##### Tác Nhân Được Phân Côn: Reviewer
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/service/NotificationDispatchService.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-014], [REQ-015], [REQ-016], [EXC-003]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Thực hiện code review tập trung vào: (1) Logic gia hạn thẻ với `@Version` để chống race condition khi hai yêu cầu gia hạn đồng thời; (2) Cơ chế rollback khi payment gateway xác nhận thành công nhưng DB write thất bại; (3) Tính bất biến của `issueDate` sau khi gia hạn - không được phép thay đổi; (4) Sử dụng `Instant` thay vì `LocalDate` cho các phép tính múi giờ; (5) Cơ chế retry với exponential backoff đúng theo `EXC-003`; (6) Circuit breaker pattern tránh lũ lụt yêu cầu khi dịch vụ bên thứ ba ngừng hoạt động. Đề xuất chiến lược vá lỗi nếu phát hiện điểm nghẽn.

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->QUẢN LÝ KHUYẾN MÃI, THÔNG BÁO QUẢNG BÁ VÀ CHATBOT AI<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 3.1: Triển khai PromotionController và PromotionService
##### Tác Nhân Được Phân Côn: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/PromotionController.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-017]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng class `@Path("/api/v1/promotions")` `PromotionController` trong package `org.nlh4j.membershiphub.centerservice` inject `PromotionService`. Các endpoint: `GET /` trả về danh sách khuyến mãi với response 200; `POST /` với `@RolesAllowed({"SystemAdmin", "CenterAdmin", "Manager"})` nhận `PromotionRequest` chứa `name` (required max 100), `code` (required unique max 30), `discountPercent` (required 0-100), `startDate` (optional), `endDate` (optional, nếu null thì khuyến mãi vĩnh viễn), `description` (optional max 500). Trả về 201 với mã tạo thành công hoặc 409 với mã `DUPLICATE_PROMOTION_CODE`. Endpoint `PUT /{promoId}` cập nhật; `DELETE /{promoId}` xóa với response 204. Áp dụng `@Valid` cho tất cả request body.

* **API and Event Routing Contracts [REQ-017]:** <!--START_API_CONTRACT-->
```json
{
  "POST /api/v1/promotions": {
    "request": {
      "name": "Summer Discount 2024",
      "code": "SUMMER24",
      "discountPercent": 15,
      "startDate": "2024-06-01",
      "endDate": null,
      "description": "Applies to all courses"
    },
    "response_201": {
      "promoId": "uuid",
      "name": "string",
      "code": "string",
      "discountPercent": 15,
      "startDate": "YYYY-MM-DD",
      "endDate": "YYYY-MM-DD hoặc null",
      "active": true
    }
  }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-004]:** <!--START_EXC_HANDLER-->
```json
{
  "DUPLICATE_PROMOTION_CODE_HTTP_409": "Bắt ConstraintViolationException khi code trùng lặp, trả message 'Mã khuyến mãi đã tồn tại'",
  "INVALID_PROMOTION_PERIOD_HTTP_400": "Trả message 'Ngày kết thúc phải sau hoặc bằng ngày bắt đầu' khi endDate < startDate"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 3.2: Triển khai AnnouncementController với bộ lọc hiệu lực
##### Tác Nhân Được Phân Côn: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/AnnouncementController.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-018]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng class `@Path("/api/v1/announcements")` `AnnouncementController` trong package `org.nlh4j.membershiphub.centerservice` inject `AnnouncementService`. Các endpoint: `GET /active` trả về danh sách thông báo đang hiệu lực với response 200 - bộ lọc JPQL: `SELECT a FROM Announcement a WHERE a.endDate IS NULL OR a.endDate >= :today`; `GET /` trả về tất cả (yêu cầu quyền admin); `POST /` với `@RolesAllowed({"SystemAdmin", "CenterAdmin", "Manager"})` nhận `AnnouncementRequest` chứa `title` (required max 150), `content` (required max 2000), `startDate` (optional), `endDate` (optional). Trả về 201. Endpoint `PUT /{id}` cập nhật; `DELETE /{id}` xóa. Tích hợp cron job hàng ngày (`@Scheduled(cron = "0 0 1 * * ?")`) để tự động đánh dấu thông báo quá hạn.

* **API and Event Routing Contracts [REQ-018]:** <!--START_API_CONTRACT-->
```json
{
  "POST /api/v1/announcements": {
    "request": {
      "title": "Holiday Schedule",
      "content": "Center closed on December 25",
      "startDate": "2024-12-20",
      "endDate": "2024-12-31"
    },
    "response_201": {
      "announcementId": "uuid",
      "title": "string",
      "content": "string",
      "active": true
    }
  },
  "GET /api/v1/announcements/active": {
    "response_200": {
      "items": [
        {
          "announcementId": "uuid",
          "title": "string",
          "content": "string",
          "startDate": "YYYY-MM-DD",
          "endDate": "YYYY-MM-DD"
        }
      ]
    }
  }
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 3.3: Kiểm thử module khuyến mãi và thông báo
##### Tác Nhân Được Phân Côn: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/AnnouncementController.java;./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/controller/AnnouncementControllerTest.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-017], [REQ-018]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo lớp `AnnouncementControllerTest` với `@QuarkusTest`. Test case: (1) `testGetActiveAnnouncements_ExcludesExpired` tạo thông báo với `endDate` trong quá khứ, xác minh endpoint `/active` không trả về; (2) `testGetActiveAnnouncements_IncludesPerpetual` tạo thông báo với `endDate = null`, xác minh được trả về; (3) `testCreateAnnouncement_TitleTooLong` với `title` 151 ký tự, expect HTTP 400 với mã validation; (4) `testCreateAnnouncement_WrongRole` với JWT giả lập vai trò Student, expect HTTP 403; (5) `testCreatePromotion_DuplicateCode` với `code` đã tồn tại, expect HTTP 409 với mã `DUPLICATE_PROMOTION_CODE`. Sử dụng `TestTransaction` để rollback dữ liệu.

#### 📝 NHIỆM VỤ PHỤ 3.4: Triển khai AiChatbotClient với cơ chế leo thang
##### Tác Nhân Được Phân Côn: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/integration/AiChatbotClient.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-019]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng class `@ApplicationScoped` `AiChatbotClient` trong package `org.nlh4j.membershiphub.userservice.integration` sử dụng MicroProfile Rest Client. Khai báo annotation `@RegisterRestClient(baseUri = "https://api.openai.com")` với interface định nghĩa endpoint `/v1/chat/completions`. Cấu hình API key qua `quarkus.rest-client.openai.api-key=${OPENAI_API_KEY}`. Phương thức `ask(String sessionId, String questionText)` gọi OpenAI Chat Completion API với timeout 10 giây, parse response thành `ChatbotResponse` gồm `answerText` và `confidenceScore`. Nếu `confidenceScore < 0.6`, đặt `escalateToHuman = true` và trả message "Xin lỗi, tôi cần chuyển câu hỏi của bạn đến nhân viên hỗ trợ". Sử dụng `@CircuitBreaker` để xử lý khi AI service không khả dụng. Cache session trong 30 phút thông qua Caffeine cache.

* **API and Event Routing Contracts [REQ-019]:** <!--START_API_CONTRACT-->
```json
{
  "POST /api/v1/chatbot/ask": {
    "request": {
      "sessionId": "string",
      "questionText": "string"
    },
    "response_200": {
      "answerText": "string",
      "confidenceScore": 0.85,
      "escalateToHuman": false
    },
    "response_503": {
      "code": "CHATBOT_UNAVAILABLE",
      "message": "Dịch vụ chatbot tạm thời không khả dụng"
    }
  }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-004]:** <!--START_EXC_HANDLER-->
```json
{
  "CHATBOT_UNAVAILABLE_HTTP_503": "Khi AI service trả 5xx hoặc timeout, ném ChatbotUnavailableException, message 'Dịch vụ chatbot tạm thời không khả dụng, vui lòng thử lại sau'",
  "SESSION_EXPIRED": "Nếu sessionId không hoạt động trong 30 phút, tự động tạo phiên mới và thông báo cho người dùng"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 3.5: Kiểm thử tích hợp chatbot với WireMock
##### Tác Nhân Được Phân Côn: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/integration/AiChatbotClient.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/integration/AiChatbotClientTest.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-019]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo lớp `AiChatbotClientTest` sử dụng WireMock để mô phỏng OpenAI API. Test case: (1) `testAsk_LowConfidence` mô phỏng response với `confidence = 0.4`, xác minh `escalateToHuman = true` và `answerText` chứa thông báo leo thang; (2) `testAsk_HighConfidence` mô phỏng response với `confidence = 0.9`, xác minh `escalateToHuman = false`; (3) `testAsk_ServiceUnavailable` mô phỏng response 500 từ AI, xác minh ném `ChatbotUnavailableException`; (4) `testAsk_Timeout` mô phỏng response chậm 11 giây, xác minh timeout exception được ném. Sử dụng `@QuarkusTestResource(WireMockTestResource.class)` để khởi tạo WireMock server.

#### 📝 NHIỆM VỤ PHỤ 3.6: Soạn hợp đồng OpenAPI cho notification-service
##### Tác Nhân Được Phân Côn: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/contracts/notification-openapi.yaml

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-008], [REQ-016], [REQ-021]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Soạn file `notification-openapi.yaml` chuẩn OpenAPI 3.0.3 tại `./sources/docs/contracts/notification-openapi.yaml`. Mô tả 2 endpoint: `POST /api/v1/notifications/dispatch` với security scheme bearerAuth JWT, nhận `NotificationDispatchRequest` và trả 202 với `NotificationDispatchResponse`; `GET /api/v1/notifications/history` với query parameter `userId` trả 200 với danh sách `NotificationHistoryItem`. Định nghĩa schema `NotificationDispatchRequest` với validation `message` max 1000, `mediaUrl` phải là HTTPS, `priority` enum HIGH/NORMAL/LOW. Schema `NotificationHistoryItem` với `notificationId`, `channel` enum PUSH/ZALO/EMAIL/SMS, `status` enum PENDING/SENT/FAILED/DELIVERED, `sentAt` ISO-8601, `retryCount` integer 0-3. Khai báo response codes 202/400/401/403/503.

#### 📝 NHIỆM VỤ PHỤ 3.7: Soạn sơ đồ Mermaid cho luồng thông báo đa kênh
##### Tác Nhân Được Phân Côn: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/diagrams/notification-flow.mmd

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-008], [REQ-016], [REQ-021]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Soạn tệp Mermaid `sequenceDiagram` tại `./sources/docs/diagrams/notification-flow.mmd` mô tả luồng thông báo đa kênh. Các participant: `Admin UI`, `notification-service`, `FcmApnsGatewayClient`, `ZaloGroupWebhookClient`, `Mobile App`, `Zalo Group`. Luồng 1: Admin gửi yêu cầu dispatch -> notification-service nhận và lưu với status PENDING -> gọi FcmApnsGatewayClient gửi push -> FcmApnsGatewayClient thất bại -> notification-service retry với exponential backoff (1s, 5s, 30s) -> sau 3 lần thất bại, status = FAILED. Luồng 2: notification-service gọi ZaloGroupWebhookClient post tin nhắn -> Zalo Group nhận tin nhắn. Sử dụng `Note over` để giải thích cơ chế retry và circuit breaker.

### 🌤️ NGÀY 4: <!--DAY_HEADER_START-->BÁO CÁO CSV, DASHBOARD THỜI GIAN THỰC, I18N VÀ KHÔI PHỤC HỆ THỐNG<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 4.1: Triển khai AttendanceReportController và AttendanceCsvExportService
##### Tác Nhân Được Phân Côn: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/reporting-service/src/main/java/org/nlh4j/membershiphub/reportingservice/controller/AttendanceReportController.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-024]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng class `@Path("/api/v1/reports/attendance")` `AttendanceReportController` trong package `org.nlh4j.membershiphub.reportingservice` inject `AttendanceCsvExportService`. Endpoint `GET /export` với annotation `@RolesAllowed({"SystemAdmin", "CenterAdmin", "Manager"})` nhận query parameters: `centerId` (required UUID), `startDate` (required YYYY-MM-DD), `endDate` (required YYYY-MM-DD, max range 30 ngày). Trả về response 200 với `Content-Type: text/csv; charset=UTF-8` và `Content-Disposition: attachment; filename="attendance-{centerId}-{startDate}-{endDate}.csv"`. Body CSV chứa header `StudentName,CourseName,AttendanceDate,Status` và các dòng dữ liệu từ join bảng `attendance`, `users`, `courses`. Validate `endDate >= startDate` và `endDate - startDate <= 30 days`, nếu không ném `ConstraintViolationException` với HTTP 400.

* **API and Event Routing Contracts [REQ-024]:** <!--START_API_CONTRACT-->
```json
{
  "GET /api/v1/reports/attendance/export": {
    "parameters": {
      "centerId": "uuid (required)",
      "startDate": "YYYY-MM-DD (required)",
      "endDate": "YYYY-MM-DD (required, max 30 days range)"
    },
    "response_200": {
      "contentType": "text/csv",
      "headers": {
        "Content-Disposition": "attachment; filename=attendance-{centerId}-{startDate}-{endDate}.csv"
      },
      "body": "StudentName,CourseName,AttendanceDate,Status
Nguyen Van A,Java Basics,2024-01-15,PRESENT
..."
    }
  }
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 4.2: Triển khai EnrollmentDashboardController với cấu hình refresh interval
##### Tác Nhân Được Phân Côn: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/reporting-service/src/main/java/org/nlh4j/membershiphub/reportingservice/controller/EnrollmentDashboardController.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-025]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng class `@Path("/api/v1/reports/dashboard")` `EnrollmentDashboardController` trong package `org.nlh4j.membershiphub.reportingservice` inject `DashboardMetricsService`. Endpoint `GET /summary` với annotation `@RolesAllowed({"SystemAdmin", "CenterAdmin", "Manager"})` nhận query parameter `centerId` (required UUID) và trả về response 200 với `DashboardSummaryResponse` chứa: `totalStudents` (số học viên ACTIVE), `activeCourses` (số khóa học đang diễn ra), `upcomingSessions` (số buổi học trong 7 ngày tới), `refreshIntervalMinutes` (cấu hình từ `system_settings.setting_value` với key `dashboard_refresh_minutes`, mặc định 15). Tích hợp `@CacheResult(cacheName = "dashboard-summary")` với TTL = refreshIntervalMinutes để giảm tải database.

* **API and Event Routing Contracts [REQ-025]:** <!--START_API_CONTRACT-->
```json
{
  "GET /api/v1/reports/dashboard/summary": {
    "parameters": {
      "centerId": "uuid (required)"
    },
    "response_200": {
      "centerId": "uuid",
      "totalStudents": 245,
      "activeCourses": 18,
      "upcomingSessions": 42,
      "refreshIntervalMinutes": 15,
      "generatedAt": "ISO-8601"
    }
  }
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 4.3: Kiểm thử tích hợp cho reporting-service
##### Tác Nhân Được Phân Côn: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/reporting-service/src/main/java/org/nlh4j/membershiphub/reportingservice/controller/AttendanceReportController.java;./sources/backend/reporting-service/src/test/java/org/nlh4j/membershiphub/reportingservice/controller/ReportingControllerTest.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-024], [REQ-025]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo lớp `ReportingControllerTest` với `@QuarkusTest` và Testcontainers. Test case: (1) `testExportCsv_Success` tạo dữ liệu attendance giả lập, gọi endpoint export, xác minh Content-Type là `text/csv` và body chứa header `StudentName,CourseName,AttendanceDate,Status` cùng các dòng dữ liệu đúng; (2) `testExportCsv_DateRangeTooLarge` với khoảng cách 31 ngày, expect HTTP 400 với mã validation; (3) `testDashboardSummary_ReturnsCounts` xác minh response chứa `totalStudents`, `activeCourses`, `upcomingSessions` đúng với dữ liệu test; (4) `testDashboardSummary_RefreshIntervalFromSettings` xác minh `refreshIntervalMinutes` được đọc từ bảng `system_settings`. Sử dụng RestAssured để verify headers và body.

#### 📝 NHIỆM VỤ PHỤ 4.4: Triển khai OutboxRelayScheduler xử lý FIFO khi khôi phục
##### Tác Nhân Được Phân Côn: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/recovery/OutboxRelayScheduler.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[EXC-001], [EXC-005]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng class `@ApplicationScoped` `OutboxRelayScheduler` trong package `org.nlh4j.membershiphub.attendanceservice.recovery` với method `@Scheduled(every = "30s")` tên `processOutbox()`. Method này truy vấn bảng `attendance_outbox` (đã tạo ở giai đoạn 3) theo `created_at ASC` (FIFO) với status `PENDING`, sử dụng JPQL với named parameters: `SELECT o FROM AttendanceOutbox o WHERE o.status = 'PENDING' ORDER BY o.createdAt ASC`. Với mỗi bản ghi, gọi `AttendanceService.recordAttendance()` để xử lý lại. Nếu thành công, cập nhật `status = PROCESSED`; nếu thất bại, tăng `retry_count` (tối đa 3) trước khi chuyển `status = FAILED`. Tích hợp `RecoveryNotificationService` để gửi thông báo sự kiện đã khôi phục cho người dùng bị ảnh hưởng qua FCM.

* **Phase Localized Exception Handlers [EXC-005]:** <!--START_EXC_HANDLER-->
```json
{
  "RETRY_EXHAUSTED": "Sau 3 lần thất bại, status='FAILED', log cảnh báo và thông báo cho admin",
  "FIFO_PROCESSING": "Đảm bảo thứ tự xử lý theo created_at ASC, tránh tình trạng attendance ghi nhận không đúng thứ tự thời gian",
  "RECOVERY_NOTIFICATION_SENT": "Sau khi xử lý FIFO, gửi thông báo 'Yêu cầu điểm danh của bạn đã được xử lý' qua FCM"
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 4.5: Triển khai giao diện mobile responsive và middleware i18n
##### Tác Nhân Được Phân Côn: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/frontend/web-app/src/middleware.ts

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-020], [REQ-022], [REQ-023]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng middleware Next.js tại `./sources/frontend/web-app/src/middleware.ts` xử lý phát hiện locale. Middleware chặn mọi request và phát hiện ngôn ngữ theo thứ tự ưu tiên: (1) cookie `NEXT_LOCALE` lưu lựa chọn trước đó; (2) header `Accept-Language` của trình duyệt; (3) fallback `vi`. Khi phát hiện ngôn ngữ phù hợp, thực hiện `NextResponse.rewrite()` để chuyển hướng đến đường dẫn có tiền tố locale tương ứng (ví dụ: `/en/courses`, `/vi/khoa-hoc`). Danh sách locales hỗ trợ: `['en', 'vi', 'es']`. Tạo file `./sources/frontend/web-app/src/app/[locale]/layout.tsx` với `<html lang={locale}>` và thẻ `<link rel="alternate" hreflang={locale} href={absoluteUrl} />` cho mỗi locale. Tạo các file i18n JSON `./sources/frontend/web-app/src/i18n/{en,vi,es}/common.json` với các chuỗi dịch.

* **API and Event Routing Contracts [REQ-022], [REQ-023], [NFR-007]:** <!--START_API_CONTRACT-->
```json
{
  "GET /{locale}/dashboard": {
    "description": "Trang dashboard đa ngôn ngữ",
    "headers": {
      "Set-Cookie": "NEXT_LOCALE={locale}; Path=/; Max-Age=31536000"
    },
    "response_200": {
      "htmlLang": "en | vi | es",
      "hreflangLinks": [
        { "hreflang": "en", "href": "https://membershiphub.com/en/dashboard" },
        { "hreflang": "vi", "href": "https://membershiphub.com/vi/dashboard" },
        { "hreflang": "es", "href": "https://membershiphub.com/es/dashboard" }
      ]
    }
  }
}
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 4.6: Triển khai RoleBasedNavigation và trang membership card
##### Tác Nhân Được Phân Côn: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/frontend/web-app/src/app/[locale]/dashboard/membership-card/page.tsx

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-014], [REQ-020]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng trang thẻ thành viên tại `./sources/frontend/web-app/src/app/[locale]/dashboard/membership-card/page.tsx`. Trang sử dụng React Query (`useQuery`) để gọi `GET /api/v1/student-cards/me` và hiển thị thông tin: `validityDays`, `remainingDays`, `endDate`, `status` với màu sắc tương ứng (xanh cho ACTIVE, đỏ cho EXPIRED). Bao gồm nút "Gia hạn" mở modal chọn số ngày từ 1-365, gọi `POST /api/v1/student-cards/renew` với `paymentTransactionId` giả lập. Tạo component `./sources/frontend/web-app/src/components/mobile/RoleBasedNavigation.tsx` hiển thị menu điều hướng theo vai trò (Student thấy "Khóa học của tôi", "Thẻ thành viên"; Teacher thấy "Lịch giảng dạy", "Danh sách học viên"). Responsive sử dụng Tailwind CSS với breakpoint `md:` cho desktop, base cho mobile.

#### 📝 NHIỆM VỤ PHỤ 4.7: Kiểm thử component React với Vitest
##### Tác Nhân Được Phân Côn: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/frontend/web-app/src/components/mobile/RoleBasedNavigation.tsx;./sources/frontend/web-app/src/components/mobile/RoleBasedNavigation.test.tsx

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-020]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo lớp kiểm thử `RoleBasedNavigation.test.tsx` sử dụng Vitest và React Testing Library. Test case: (1) `testRendersStudentMenu` với prop `role="Student"`, xác minh hiển thị menu items "Khóa học của tôi" và "Thẻ thành viên", KHÔNG hiển thị "Lịch giảng dạy"; (2) `testRendersTeacherMenu` với `role="Teacher"`, xác minh hiển thị "Lịch giảng dạy" và "Danh sách học viên"; (3) `testMobileBreakpoint` sử dụng `window.matchMedia` mock với `width=375`, xác minh navigation collapse thành hamburger menu; (4) `testNavigationClick` mô phỏng click vào menu item, xác minh callback `onNavigate` được gọi với đúng path.

#### 📝 NHIỆM VỤ PHỤ 4.8: Soạn thảo tài liệu kiến trúc cho thông báo FCM/APNs
##### Tác Nhân Được Phân Côn: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/architecture/notifications-fcm-apns.md

* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [ARC-008], [REQ-016], [REQ-021]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Soạn thảo tài liệu kiến trúc tại `./sources/docs/architecture/notifications-fcm-apns.md`. Tài liệu phải bao gồm: sơ đồ Mermaid `sequenceDiagram` thể hiện luồng từ sự kiện nghiệp vụ (ghi danh, phân công giáo viên, thông báo quảng bá) -> notification-service nhận -> gọi FcmApnsGatewayClient hoặc ZaloGroupWebhookClient -> retry queue với exponential backoff; mô tả chi tiết cơ chế retry với thời gian chờ (1s, 5s, 30s) theo `EXC-003`; hướng dẫn cấu hình biến môi trường `FCM_SERVER_KEY`, `APNS_KEY_ID`, `ZALO_OA_ACCESS_TOKEN` thông qua Google Secret Manager; bảng mã lỗi HTTP với message bản địa hóa tiếng Việt; checklist bảo mật cho việc lưu trữ device token.

#### 📝 NHIỆM VỤ PHỤ 4.9: Soạn thảo tài liệu chiến lược khôi phục FIFO
##### Tác Nhân Được Phân Côn: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/architecture/disaster-recovery-fifo.md

* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [EXC-005]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Soạn thảo tài liệu tại `./sources/docs/architecture/disaster-recovery-fifo.md` mô tả chi tiết cơ chế xử lý FIFO khi khôi phục dịch vụ theo `EXC-005`. Tài liệu phải bao gồm: sơ đồ Mermaid `sequenceDiagram` thể hiện kịch bản dịch vụ attendance-service ngừng hoạt động -> yêu cầu điểm danh được lưu vào bảng `attendance_outbox` với status PENDING -> khi dịch vụ khôi phục, `OutboxRelayScheduler` xử lý theo `created_at ASC` (FIFO) -> gọi `AttendanceService.recordAttendance()` để ghi nhận -> cập nhật status PROCESSED -> gửi thông báo "Yêu cầu điểm danh của bạn đã được xử lý" qua FCM. Mô tả cấu trúc bảng `attendance_outbox` với các cột `id`, `payload`, `retry_count`, `status`, `created_at`. Hướng dẫn monitoring với alert khi số bản ghi PENDING vượt ngưỡng 1000.

#### 📝 NHIỆM VỤ PHỤ 4.10: Soạn thảo tài liệu chiến lược I18N và SEO
##### Tác Nhân Được Phân Côn: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/architecture/i18n-seo-strategy.md

* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [REQ-022], [REQ-023], [NFR-007]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Soạn thảo tài liệu tại `./sources/docs/architecture/i18n-seo-strategy.md` mô tả chiến lược quốc tế hóa và SEO. Tài liệu phải bao gồm: cấu trúc thư mục `src/i18n/{en,vi,es}/` với file `common.json` chứa các chuỗi dịch; hướng dẫn thêm ngôn ngữ mới (tạo folder mới, thêm vào `middleware.ts` locales array, cập nhật sitemap); checklist SEO bao gồm: thẻ `<html lang={locale}>`, thẻ meta `og:locale`, liên kết `hreflang` chéo giữa các phiên bản ngôn ngữ, tạo `sitemap.xml` đa ngôn ngữ tại `./sources/frontend/web-app/src/app/sitemap.xml/route.ts`. Mô tả cơ chế chuyển ngữ không tải lại trang sử dụng `next-intl` với `useTranslations` hook. Danh sách các trang cần dịch: dashboard, membership card, course list, reports.

#### 📝 NHIỆM VỤ PHỤ 4.11: Đánh giá tổng thể mã nguồn giai đoạn 4
##### Tác Nhân Được Phân Côn: Reviewer
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/service/NotificationDispatchService.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-016], [REQ-024], [REQ-025], [ARC-008], [ARC-009]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Thực hiện đánh giá tổng thể cuối giai đoạn 4. Tập trung vào: (1) Tuân thủ RBAC trên tất cả endpoint admin (dispatch notification, export report, create promotion); (2) Cơ chế retry với exponential backoff đúng theo `EXC-003`; (3) FIFO processing trong `OutboxRelayScheduler` đảm bảo thứ tự thời gian; (4) Xử lý timezone với `Instant` thay vì `LocalDate`; (5) Middleware i18n xử lý đúng thứ tự ưu tiên locale; (6) Responsive UI hoạt động trên mobile breakpoint 375px. Sinh báo cáo đánh giá với bảng tuân thủ cho từng Tag ID `[REQ-014]` đến `[EXC-005]`. Lập danh sách khuyến nghị cải tiến nếu phát hiện bất thường.

#### 📝 NHIỆM VỤ PHỤ 4.12: Soạn thảo tài liệu sơ đồ Mermaid cho luồng khôi phục
##### Tác Nhân Được Phân Côn: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/diagrams/recovery-fifo-flow.mmd

* **Traceability Tag Tokens:** <!--START_TAGS-->[EXC-005], [REQ-012]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Soạn tệp Mermaid `sequenceDiagram` tại `./sources/docs/diagrams/recovery-fifo-flow.mmd` mô tả chi tiết luồng khôi phục FIFO theo `EXC-005`. Các participant: `Mobile App`, `API Gateway`, `attendance-service`, `attendance_outbox` (Database), `OutboxRelayScheduler`, `notification-service`, `FCM`. Luồng: Mobile App gửi yêu cầu scan QR -> API Gateway định tuyến -> attendance-service nhận nhưng DB tạm thời không khả dụng -> lưu yêu cầu vào bảng `attendance_outbox` với status PENDING -> attendance-service khôi phục -> OutboxRelayScheduler chạy mỗi 30s -> truy vấn bảng theo created_at ASC (FIFO) -> gọi AttendanceService.recordAttendance() cho từng bản ghi -> cập nhật status PROCESSED -> notification-service gửi FCM thông báo "Yêu cầu điểm danh của bạn đã được xử lý" -> Mobile App nhận notification. Sử dụng `Note over` để giải thích cơ chế retry tối đa 3 lần.

#### 📝 NHIỆM VỤ PHỤ 4.13: Soạn thảo hợp đồng OpenAPI cho reporting-service
##### Tác Nhân Được Phân Côn: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/contracts/reporting-openapi.yaml

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-008], [REQ-024], [REQ-025]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Soạn file `reporting-openapi.yaml` chuẩn OpenAPI 3.0.3 tại `./sources/docs/contracts/reporting-openapi.yaml`. Mô tả 2 endpoint: `GET /api/v1/reports/attendance/export` với security scheme bearerAuth JWT, query parameters `centerId` (uuid), `startDate` (date), `endDate` (date), trả response 200 với `text/csv` content type hoặc 400 với mã validation khi date range > 30 ngày; `GET /api/v1/reports/dashboard/summary` với query parameter `centerId`, trả response 200 với schema `DashboardSummaryResponse` chứa `totalStudents`, `activeCourses`, `upcomingSessions`, `refreshIntervalMinutes`. Định nghĩa schema chi tiết với validation constraints. Bao gồm response codes 200/400/401/403/500.

#### 📝 NHIỆM VỤ PHỤ 4.14: Soạn thảo hợp đồng OpenAPI cho membership card
##### Tác Nhân Được Phân Côn: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/contracts/membership-card-openapi.yaml

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-014], [REQ-015]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Soạn file `membership-card-openapi.yaml` chuẩn OpenAPI 3.0.3 tại `./sources/docs/contracts/membership-card-openapi.yaml`. Mô tả 2 endpoint: `GET /api/v1/student-cards/me` với security scheme bearerAuth JWT và `@RolesAllowed({"Student"})`, trả response 200 với schema `MembershipCardResponse`; `POST /api/v1/student-cards/renew` nhận `RenewalRequest` với `renewalDays` (integer 1-365) và `paymentTransactionId` (string), trả response 200 với `newEndDate` và `newRemainingDays` hoặc 402 với mã `PAYMENT_GATEWAY_FAILED`. Định nghĩa schema `MembershipCardResponse` với `cardId`, `studentId`, `issueDate`, `endDate`, `validityDays`, `remainingDays`, `status` enum ACTIVE/EXPIRED/SUSPENDED. Bao gồm response codes 200/400/401/402/403.

#### 📝 NHIỆM VỤ PHỤ 4.15: Kiểm thử tích hợp cho OutboxRelayScheduler
##### Tác Nhân Được Phân Côn: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/recovery/OutboxRelayScheduler.java;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/recovery/OutboxRelaySchedulerTest.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[EXC-001], [EXC-005]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo lớp `OutboxRelaySchedulerTest` với `@QuarkusTest` và Testcontainers. Test case: (1) `testProcessOutbox_FifoOrder` tạo 3 bản ghi outbox với `created_at` tăng dần, trigger method `processOutbox()`, xác minh cả 3 được xử lý đúng thứ tự bằng cách kiểm tra `Mockito.verify` với `InOrder`; (2) `testProcessOutbox_RetryOnFailure` mô phỏng `AttendanceService.recordAttendance()` ném exception ở 2 lần đầu, xác minh `retry_count` được tăng lên 2; (3) `testProcessOutbox_RetryExhausted` mô phỏng lỗi ở 3 lần liên tiếp, xác minh status chuyển thành `FAILED`; (4) `testProcessOutbox_SendsRecoveryNotification` xác minh `RecoveryNotificationService` được gọi với đúng `studentId` sau khi xử lý thành công. Sử dụng `Awaitility` để đợi scheduler chạy.

<!--END_CHUNK_PART_4_FINAL-->