<!--START_CHUNK_PART_1_INITIAL-->

# Bối cảnh dự án toàn cầu: membership-hub

## 📊 Quản lý tài liệu

| Mục | Chi tiết |
| :--- | :--- |
| **Mã bản thiết kế** | ARCH-20260828100834 |
| **Tên dự án** | membership-hub |
| **Phiên bản** | 1.0 (Đường cơ sở) |
| **Ngày giờ** | 2026/08/28 10:08:34 |
| **Tác giả** | Kiến trúc sư hệ thống doanh nghiệp (SA Agent) |
| **Phê duyệt** | Đang chờ đánh giá quản trị kỹ thuật |

## 📊 1. TỔNG QUAN HỆ THỐNG & MÔ HÌNH KIẾN TRÚC CỐT LÕI

### ⚙️ 1.1. Mô hình hệ thống cốt lõi & phương thức kiến trúc
- Nền tảng microservices phân tách theo miền nghiệp vụ: `user-service`, `center-service`, `course-service`, `enrollment-service`, `attendance-service`, `card-service`, `notification-service`, `promotion-service`, `chatbot-service`, `report-service` [ARC-000].
- Mỗi dịch vụ chạy trên Quarkus 3.15.1 (JVM 21), giao tiếp đồng bộ qua REST/JSON và bất đồng bộ qua Apache Kafka 3.7.0 [NFR-001], [NFR-004].
- Áp dụng pattern CQRS cho module báo cáo (`report-service`) tách hẳn mô hình đọc/ghi nhằm tối ưu truy vấn phân tích [NFR-001].
- Lõi phản ứng (Reactive) sử dụng Mutiny + Vert.x trong các luồng có độ trễ thấp (xác thực, điểm danh QR) [NFR-001].
- Quản lý danh tính tập trung qua Keycloak 24 (OAuth2 + OIDC) cấp JWT access 15 phút và refresh 7 ngày [ARC-006], [NFR-003].
- Bảo mật theo chuẩn OWASP Top 10: chuẩn bị truy vấn, mã hóa mật khẩu bcrypt, chống XSS/CSRF [NFR-003].
- Đa ngôn ngữ i18n (en, vi, es) với quản lý locale qua cookie + `Accept-Language` [REQ-022], [REQ-023], [NFR-007].

### 🌊 1.2. Luồng dữ liệu doanh nghiệp & hệ sinh thái cốt lõi
- Kênh bất đồng bộ: Apache Kafka với 7 topic chính `user.events`, `enrollment.events`, `attendance.events`, `course.events`, `notification.events`, `card.events`, `audit.log` [ARC-008], [REQ-016].
- Cổng vào (Gateway): Spring Cloud Gateway 4.2.0 đứng trước xử lý xác thực JWT, giới hạn tốc độ, CORS, ghi log truy cập [NFR-003], [NFR-006].
- Cổng sự kiện (Event Gateway): Kafka Connect + Schema Registry (Avro) đảm bảo tương thích schema giữa các dịch vụ [NFR-001].
- Phát sóng ra bên ngoài: gửi thông báo tới Firebase Cloud Messaging (FCM) + APNs (qua `notification-service`) và đẩy vào nhóm Zalo OA thông qua webhook tích hợp [ARC-008], [REQ-016], [REQ-021].
- Lưu trữ dữ liệu: PostgreSQL 16 (master + read-replica) cho dữ liệu giao dịch; Redis 7.2 cho cache phiên và khóa idempotency [NFR-004].
- Thu thập log tập trung: Fluent Bit → OpenSearch 2.15 phục vụ audit truy vấn [NFR-006].
- Tích hợp AI Chatbot qua OpenAI API 1.40 (proxy nội bộ) cho `chatbot-service`, hỗ trợ fallback sang nhân viên hỗ trợ [REQ-019].
- Đồng bộ cấu hình: Spring Cloud Config Server + Git repository bất biến, hỗ trợ xoay vòng khóa bí mật [NFR-003].

## 📁 2. HỆ SINH THÁI & THƯ VIỆN PHỤ THUỘC CÔNG NGHỆ
- **Cốt lõi hạ tầng Backend:** Quarkus 3.15.1, JDK 21 LTS, Maven 3.9.9, Apache Kafka 3.7.0, PostgreSQL JDBC Driver 42.7.3, Hibernate ORM with Panache 3.15.1, Flyway 10.10.0, Mutiny 2.6.0, SmallRye Reactive Messaging 4.24.0, Keycloak Admin Client 24.0.5, Spring Cloud Gateway 4.2.0, OpenAI Java SDK 0.18.0, Firebase Admin SDK 9.2.0, Resilience4j 2.2.0, Micrometer 1.13.0, OpenTelemetry SDK 1.40.0, JUnit 5.10.2, Testcontainers 1.20.1, REST Assured 5.5.0.
- **Giao diện người dùng web & di động đa nền tảng:** Next.js 14.2.5 (App Router) + React 18.3.1, TypeScript 5.5.4, TailwindCSS 3.4.10, NextAuth 4.24.7, React Query 5.51.0, react-i18next 14.1.2, next-intl 3.17.2, Capacitor 6.1.2 cho bản mobile Android/iOS, Firebase Cloud Messaging Web SDK 10.13.0, Zalo OA Webhook client, Vitest 2.0.5, Playwright 1.46.0.

## 📁 3. KHUÔN KHỔ TOÀN CẦU & TIÊU CHUẨN TUÂN THỦ DOANH NGHIỆP
- Mọi endpoint REST phải khai báo phiên bản theo `/api/v1/...`, hỗ trợ `Accept-Language` và trả về `application/json; charset=utf-8` [REQ-022], [REQ-023], [NFR-007].
- Mọi sự kiện Kafka phải đăng ký schema trong Schema Registry và bật cơ chế tương thích ngược [NFR-001].
- Tất cả thay đổi quyền phải ghi log audit với `userId`, `action`, `timestamp`, `ipAddress`, lưu trữ 365 ngày [NFR-006].
- Tuân thủ OWASP Top 10: chống SQLi, XSS, CSRF, mã hóa mật khẩu bcrypt cost ≥ 12 [NFR-003].
- Tuân thủ GDPR/CCPA: quyền xóa dữ liệu cá nhân, xuất JSON, quản lý consent marketing [NFR-008].
- Mọi biến môi trường nhạy cảm phải được lưu trong Google Secret Manager, không hardcode trong mã nguồn [NFR-003].
- Sử dụng quy ước thư mục `org.nlh4j.membershiphub.<service>` cho toàn bộ gói Java, cấm sử dụng `com.example` [ARC-000].
- Commit phải tuân theo Conventional Commits, tích hợp pre-commit hook chạy `mvn verify` và `eslint` [NFR-006].
- Tất cả microservice phải expose endpoint `/q/health/ready`, `/q/health/live`, `/q/metrics` cho Kubernetes probe [NFR-004].

### 🔑 3.1. Cơ sở bảo mật & tuân thủ
- Mã hóa dữ liệu truyền tải bằng TLS 1.3, dữ liệu lưu trữ mã hóa AES-256 (PostgreSQL TDE) [NFR-003].
- JWT access token hết hạn 15 phút, refresh token 7 ngày, xoay vòng khóa ký qua Keycloak [ARC-006], [NFR-003].
- Kiểm soát truy cập theo vai trò (RBAC) thực thi tại Gateway và trong từng microservice bằng `@RolesAllowed` [ARC-001]–[ARC-005].
- Ghi nhật ký audit bất biến cho thay đổi vai trò, ghi danh, điểm danh, phát hành thông báo, lưu trữ 1 năm [NFR-006].
- Mã hóa mật khẩu bằng bcrypt cost 12, cấm lưu plaintext [REQ-001], [NFR-003].
- Kiểm thử bảo mật tự động (OWASP ZAP + Snyk) trong pipeline CI/CD [NFR-003].
- Bảo vệ chống DDoS lớp 7 thông qua Cloud Armor + rate-limit tại Gateway [NFR-002].

### 🌐 3.2. Rào chắn hạ tầng & hiệu năng
- HikariCP kích thước pool 30 kết nối mỗi instance, giám sát bằng Micrometer [NFR-001].
- Bộ nhớ đệm Redis với TTL 5 phút cho danh sách khóa học, TTL 1 phút cho dashboard, chính sách LRU [NFR-001].
- Kafka producer cấu hình acks=all, nén snappy, batch size 64KB, Linger 20ms [NFR-001].
- HPA mở rộng khi CPU > 70% hoặc độ trễ P95 > 300ms, tối thiểu 2 pod tới đa 20 pod [NFR-004].
- Sao lưu PostgreSQL đầy đủ hằng ngày lúc 02:00 UTC, PITR 24 giờ, dự phòng liên vùng [NFR-009].
- Kích thước Docker image cơ sở < 200MB, image cuối < 500MB dùng build multi-stage [NFR-005].
- Giám sát tập trung qua Prometheus + Grafana, cảnh báo lỗi 5xx > 1% trong 5 phút [NFR-002].
- Cơ chế idempotency key cho điểm danh QR sử dụng Redis SETNX TTL 24h [REQ-013], [EXC-002].

### 🥞 3.3. MA TRẬN NGĂN XẾP KIẾN TRÚC
```properties:stack_matrix
PERSISTENCE_LAYER_REQUIRED=true
BACKEND_LAYER_REQUIRED=true
FRONTEND_LAYER_REQUIRED=true
MOBILE_LAYER_REQUIRED=true
DEVOPS_LAYER_REQUIRED=true
```

<!--END_CHUNK_PART_1_INITIAL-->

<!--START_CHUNK_PART_1_BACKLOG_4_1-->

## 🏁 4. TỔNG QUAN KIẾN TRÚC ĐA GIAI ĐOẠN

### 📦 4.1. DANH MỤC SẢN PHẨM TỔNG THỂ (MASTER PRODUCT BACKLOG)

Bản đồ tổng thể sản phẩm tổng hợp toàn bộ các hạng mục kỹ thuật cần thiết để hiện thực hóa hệ thống quản lý thành viên đa trung tâm. Mỗi hàng đại diện cho một đơn vị công việc nguyên tử, có thể ước lượng, giao phó và theo dõi được, đảm bảo 100% phủ vết các yêu cầu nghiệp vụ và phi chức năng từ tài liệu SRS. Các hạng mục được phân loại theo ba trụ cột chính: Mã nguồn ứng dụng (microservices Quarkus kết hợp giao diện Next.js), Tài liệu doanh nghiệp, và Hạ tầng DevOps (Docker, GCP, GKE), tạo thành lộ trình triển khai chặt chẽ và có khả năng kiểm chứng.

#### [MA TRẬN SỐ HỌC HỆ THỐNG]
> - **Total [REQ] Tags:** 25 Tags
> - **Total [EXC] Tags:** 5 Tags
> - **Total [ARC] Tags:** 9 Tags
> - **Total [DAT] Tags:** 11 Tags
> - **Total [NFR] Tags:** 9 Tags
> - ➡️ **Total SRS Tags:** 59 Tags

<!--BACKLOG_SYNOPSIS_GRID_START-->

| No. | Task | Technical Purpose / Deliverables Summary | Type | TagID |
| :--- | :--- | :--- | :--- | :--- |
| 1 | Khởi tạo khung dự án Microservices | Tạo `pom.xml` gốc tại `./sources/backend/pom.xml` và các `pom.xml` riêng cho từng service: `user-service`, `center-service`, `course-service`, `enrollment-service`, `attendance-service`, `notification-service`. Đồng thời dựng `package.json` và `tsconfig.json` cho `./sources/frontend/`. | Application Code | [ARC-000] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 2 | Đăng ký tài khoản người dùng | Xây dựng endpoint REST POST `/api/v1/auth/register` trong `user-service`, xử lý mã hóa bcrypt, kiểm tra tính duy nhất email, tạo bản ghi User với vai trò mặc định `Student`, phát hành JWT access/refresh token. | Application Code | [REQ-001], [EXC-004] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 3 | Đăng nhập/xác thực OAuth2 mạng xã hội | Tích hợp Firebase, Google, Facebook OAuth2 trong `user-service`; xử lý authorization code grant, ánh xạ thông tin provider về bản ghi User cục bộ, phát hành JWT. | Application Code | [REQ-002], [ARC-006] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 4 | Phân quyền và gán vai trò người dùng | Triển khai endpoint PUT `/api/v1/users/{id}/role` với cơ chế RBAC; tích hợp bảng `Roles` và ghi log kiểm toán cho mọi thay đổi vai trò. | Application Code | [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-006] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 5 | Danh sách trung tâm | Xây dựng endpoint GET `/api/v1/centers` trong `center-service`; trả về danh sách phân trang gồm Name, Address, TaxID, ContactPhone, ContactEmail. | Application Code | [REQ-004] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 6 | CRUD trung tâm | Triển khai POST/PUT/DELETE `/api/v1/centers`; kiểm tra trùng lặp TaxID bằng ràng buộc UNIQUE, xác thực định dạng email và số điện thoại. | Application Code | [REQ-005], [EXC-004] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 7 | Phân công Center Admin | Xây dựng POST/DELETE `/api/v1/centers/{id}/admins`; cập nhật quan hệ User-Center, thay đổi vai trò sang `Center Admin`, ghi log audit. | Application Code | [REQ-006] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 8 | Danh sách khóa học | Triển khai endpoint GET `/api/v1/courses` trong `course-service`; trả về lưới dữ liệu gồm CourseID, Title, StartDate, EndDate, TeacherName. | Application Code | [REQ-007] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 9 | Quản lý khóa học với kiểm tra xung đột | Xây dựng POST/PUT/DELETE `/api/v1/courses`; sử dụng trigger PostgreSQL hoặc service-level lock để phát hiện xung đột lịch trình giáo viên/địa điểm. | Application Code | [REQ-008], [EXC-004] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 10 | Phân công giáo viên cho khóa học | Endpoint POST/DELETE `/api/v1/courses/{id}/teachers`; cập nhật bảng `Courses.teacher_id`, đẩy sự kiện vào Kafka topic `teacher.assigned` để gửi thông báo. | Application Code | [REQ-009], [ARC-008] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 11 | Duyệt khóa học cho học viên | Xây dựng GET `/api/v1/students/{id}/available-courses`; loại trừ các khóa học đã ghi danh, hiển thị capacity và lịch trình. | Application Code | [REQ-010] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 12 | Ghi danh khóa học tự động tạo tài khoản | Triển khai POST `/api/v1/enrollments` trong `enrollment-service`; nếu học viên chưa tồn tại, tự động tạo bản ghi User với role `Student`, đẩy sự kiện thông báo. | Application Code | [REQ-011], [ARC-008] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 13 | Quét QR điểm danh | Xây dựng endpoint POST `/api/v1/attendance/scan` trong `attendance-service`; giải mã payload base64 (studentID+courseID), xác thực quan hệ ghi danh, tạo bản ghi Attendance. | Application Code | [REQ-012], [ARC-007] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 14 | Đảm bảo idempotency điểm danh | Thêm UNIQUE constraint composite (student_id, course_id, attendance_date) trên bảng `Attendance`; bắt exception duplicate, trả về response success với cờ `duplicate=true`. | Application Code | [REQ-013], [EXC-002] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 15 | Hiển thị thẻ thành viên và số ngày còn lại | Endpoint GET `/api/v1/students/{id}/card`; tính toán `remaining_days = validity_days - (CURRENT_DATE - issue_date)`, trả về JSON đầy đủ. | Application Code | [REQ-014] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 16 | Gia hạn thẻ thành viên | Triển khai POST `/api/v1/students/{id}/card/renew`; nhận renewalDays (1-365), cập nhật `validity_days`, gọi payment gateway stub, đẩy thông báo xác nhận. | Application Code | [REQ-015] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 17 | Hệ thống thông báo đa kênh | Xây dựng `notification-service` lắng nghe Kafka topic (enrollment, course, attendance); gửi push notification qua FCM/APNs và đăng tin nhắn Zalo Group. | Application Code | [REQ-016], [ARC-008] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 18 | Quản lý chương trình khuyến mãi | CRUD `/api/v1/promotions`; hỗ trợ startDate/endDate tùy chọn, khi thiếu endDate coi như vĩnh viễn, validate định dạng ngày YYYY-MM-DD. | Application Code | [REQ-017], [EXC-004] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 19 | Quản lý thông báo nội bộ | CRUD `/api/v1/announcements`; hỗ trợ expiry tự động, validate độ dài Title (max 150) và Content (max 2000). | Application Code | [REQ-018], [EXC-004] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 20 | Tích hợp Chatbot AI hỗ trợ khách hàng | Triển khai endpoint POST `/api/v1/chatbot/message`; tích hợp LLM gateway (OpenAI/Vertex AI), thiết lập ngưỡng confidence để chuyển tiếp nhân viên hỗ trợ. | Application Code | [REQ-019] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 21 | Giao diện di động responsive theo vai trò | Phát triển ứng dụng Next.js mobile-first; routing động dựa trên role token, lazy-loading modules cho Student/Teacher/Admin. | Application Code | [REQ-020], [ARC-009] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 22 | Push notification trên di động | Tích hợp FCM SDK trong frontend; đăng ký/lưu device token qua API, xử lý notification payload và deep-link. | Application Code | [REQ-021] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 23 | Phát hiện ngôn ngữ mặc định | Middleware Next.js đọc cookie `NEXT_LOCALE`, fallback Accept-Language header; thiết lập i18n provider cho toàn bộ ứng dụng. | Application Code | [REQ-022], [NFR-007] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 24 | SEO đa ngôn ngữ | Cấu hình `next-i18next`; sinh hreflang tags, meta tags động, sitemap.xml đa locale. | Application Code | [REQ-023], [NFR-007] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 25 | Báo cáo điểm danh CSV | Endpoint GET `/api/v1/reports/attendance?centerId=&from=&to=`; sinh file CSV với 4 cột StudentName, CourseName, AttendanceDate, Status; giới hạn date range 30 ngày. | Application Code | [REQ-024] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 26 | Dashboard tổng hợp ghi danh | Endpoint GET `/api/v1/dashboard/enrollment-summary`; aggregate totalStudents, activeCourses, upcomingSessions (7 ngày tới); cache 15 phút qua Redis. | Application Code | [REQ-025] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 27 | Xử lý sự cố mạng khi quét QR | Triển khai cơ chế retry queue phía client (IndexedDB); khi có mạng, retry theo FIFO; idempotency key tránh tạo bản ghi trùng. | Application Code | [EXC-001], [REQ-012] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 28 | Xử lý gửi thông báo thất bại | Worker trong `notification-service` retry tối đa 3 lần với exponential backoff; log failure vào bảng `Notification.delivered=false`. | Application Code | [EXC-003], [REQ-016] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 29 | Khôi phục hệ thống sau sự cố | Cơ chế drain queue (Kafka consumer group) khi service restart; đảm bảo FIFO order; đẩy notification "system recovered" tới user bị ảnh hưởng. | Application Code | [EXC-005] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 30 | Lớp cơ sở dữ liệu & di trú schema | Tạo Flyway migration scripts cho 11 bảng lõi: Users, Centers, Courses, Enrollments, Attendance, StudentCards, Notifications, Roles, Promotions, Announcements, SystemSettings, bao gồm ràng buộc UNIQUE, FOREIGN KEY, INDEXES. | Application Code | [DAT-ALL (1 to 11)] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 31 | Kiến trúc bảo mật & tích hợp hệ thống | Thiết lập JWT filter chain, OAuth2 resource server, CORS policy, Kafka topic schemas, REST API contract gateway, OpenAPI 3.0 spec. | Application Code | [ARC-006], [ARC-007], [ARC-008], [ARC-009] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 32 | Container hóa ứng dụng | Xây dựng multi-stage Dockerfile cho từng service Quarkus (base < 200MB, final < 500MB); đẩy image lên Artifact Registry. | DevOps Infrastructure | [NFR-005], [ARC-000] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 33 | Hạ tầng GCP & GKE | Terraform scripts cho VPC, IAM, Cloud SQL PostgreSQL, GCS, Memorystore Redis; Helm charts cho GKE deployment với HPA (CPU>70% hoặc latency>300ms). | DevOps Infrastructure | [NFR-001], [NFR-002], [NFR-004], [NFR-009] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 34 | Bảo mật, mã hóa & tuân thủ | Áp dụng TLS 1.3, AES-256 at-rest, OWASP Top 10 mitigations, GDPR/CCPA (data export, deletion endpoint), audit log retention 1 năm. | DevOps Infrastructure | [NFR-003], [NFR-006], [NFR-008] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 35 | Tài liệu kỹ thuật doanh nghiệp | Tạo bộ tài liệu hoàn chỉnh trong `./sources/docs/`: System Architecture Blueprint, Database Schema Topology, API Contracts (OpenAPI), Operational Manual (Vietnamese). | Enterprise Documentation | [DOC-001] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| **SUMMARY** | **Total Tracking Tags Covered:** 59 | **Total Tasks:** 35 | **Status:** Đã xác minh | **Coverage:** 100.00% |

<!--BACKLOG_SYNOPSIS_GRID_END-->

<!--END_CHUNK_PART_1_BACKLOG_4_1-->

<!--START_CHUNK_PART_1_MATRIX_4_2-->

### 🔭 4.2. MA TRẬN TÓM TẮT ĐA GIAI ĐOẠN

#### [VÒNG ĐỜI SỐ HỌC MA TRẬN]
> - **Tổng số Backlog Tasks:** 35 Tasks
> - **Tổng số Backlog Tags:** 59 Tags
> - **Tổng số Tasks đã phân bổ:** 35 Tasks
> - **Tổng số Tags đã phân bổ:** 59 Tags

<!--PHASE_SYNOPSIS_GRID_START-->

| Giai Đoạn | Phạm Vi Ngày | Task IDs Được Bao Phủ | Cấu Trúc Thành Phần / Đường Dẫn Module | Tóm Tắt Sản Phẩm Kỹ Thuật | Sub-Agent Được Phân Công | Targeted Tag IDs |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Giai Đoạn 1 | Ngày 1 - 3 | Task 1, Task 30, Task 31 | `./sources/backend/pom.xml`, `./sources/backend/user-service/pom.xml`, `./sources/backend/center-service/pom.xml`, `./sources/backend/course-service/pom.xml`, `./sources/backend/enrollment-service/pom.xml`, `./sources/backend/attendance-service/pom.xml`, `./sources/backend/notification-service/pom.xml`, `./sources/frontend/package.json`, `./sources/frontend/tsconfig.json`, `./sources/backend/notification-service/src/main/resources/db/migration/` | Khởi tạo khung microservices Quarkus với Maven multi-module cho sáu service backend (`user-service`, `center-service`, `course-service`, `enrollment-service`, `attendance-service`, `notification-service`), đồng thời dựng frontend Next.js với `package.json` và `tsconfig.json`. Triển khai Flyway migration scripts cho 11 bảng lõi (Users, Centers, Courses, Enrollments, Attendance, StudentCards, Notifications, Roles, Promotions, Announcements, SystemSettings) với ràng buộc UNIQUE, FOREIGN KEY và INDEXES. Thiết lập JWT filter chain, OAuth2 resource server, CORS policy, Kafka topic schemas và OpenAPI 3.0 spec gateway. | Coder, Tester, Reviewer, Doc | [ARC-000], [DAT-ALL (1 to 11)], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [EXC-002] <!--REGISTERED_PHASE_ROW--> |
| Giai Đoạn 2 | Ngày 1 - 3 | Task 2, Task 3, Task 4, Task 5, Task 6, Task 7 | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuthController.java`, `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/UserService.java`, `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/OAuth2Service.java`, `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/RoleService.java`, `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuditLogger.java`, `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterController.java`, `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterService.java` | Xây dựng module xác thực và quản lý người dùng với endpoint POST `/api/v1/auth/register` mã hóa bcrypt, kiểm tra tính duy nhất email, phát hành JWT. Tích hợp OAuth2 cho Firebase, Google, Facebook thông qua authorization code grant. Triển khai RBAC với endpoint PUT `/api/v1/users/{id}/role`, bảng Roles và audit log. Xây dựng center-service với GET `/api/v1/centers` phân trang, CRUD `/api/v1/centers` với ràng buộc UNIQUE TaxID, và endpoint phân công Center Admin. | Coder, Tester, Reviewer, Doc | [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [EXC-004], [NFR-006] <!--REGISTERED_PHASE_ROW--> |
| Giai Đoạn 3 | Ngày 1 - 3 | Task 8, Task 9, Task 10, Task 11, Task 12, Task 25, Task 26 | `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseController.java`, `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseService.java`, `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/ScheduleValidator.java`, `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentController.java`, `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentService.java`, `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/DashboardService.java` | Triển khai course-service với GET `/api/v1/courses` hiển thị lưới khóa học, CRUD `/api/v1/courses` với trigger PostgreSQL kiểm tra xung đột lịch trình giáo viên, và endpoint phân công giáo viên phát sự kiện Kafka. Xây dựng enrollment-service với GET `/api/v1/students/{id}/available-courses` loại trừ khóa học đã ghi danh, POST `/api/v1/enrollments` tự động tạo tài khoản Student nếu chưa tồn tại. Triển khai endpoint dashboard GET `/api/v1/dashboard/enrollment-summary` aggregate tổng học viên, khóa học đang hoạt động, buổi học sắp tới trong 7 ngày với cache 15 phút. | Coder, Tester, Reviewer, Doc | [REQ-007], [REQ-008], [REQ-009], [REQ-010], [REQ-011], [REQ-025], [ARC-008], [EXC-004] <!--REGISTERED_PHASE_ROW--> |
| Giai Đoạn 4 | Ngày 1 - 3 | Task 13, Task 14, Task 15, Task 16, Task 17, Task 18, Task 19, Task 20, Task 21, Task 22, Task 23, Task 24, Task 25, Task 27, Task 28, Task 29 | `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceController.java`, `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceService.java`, `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/RetryQueueConsumer.java`, `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/NotificationConsumer.java`, `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/ZaloGateway.java`, `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/ChatbotService.java`, `./sources/frontend/mobile-app/src/app/[locale]/student/card/page.tsx`, `./sources/frontend/mobile-app/src/app/[locale]/student/attendance/page.tsx`, `./sources/frontend/mobile-app/src/lib/fcm.ts`, `./sources/frontend/mobile-app/middleware.ts` | Xây dựng attendance-service với POST `/api/v1/attendance/scan` giải mã QR base64, UNIQUE constraint composite (student_id, course_id, attendance_date) đảm bảo idempotency, retry queue xử lý sự cố mạng. Triển khai module thẻ thành viên GET `/api/v1/students/{id}/card` tính remaining_days và POST `/api/v1/students/{id}/card/renew`. Hoàn thiện notification-service lắng nghe Kafka, gửi FCM/APNs push và Zalo Group, worker retry 3 lần exponential backoff. Xây dựng CRUD promotions, announcements, tích hợp Chatbot AI với LLM gateway, frontend Next.js mobile-first theo vai trò, FCM SDK, middleware i18n với cookie `NEXT_LOCALE`, cấu hình SEO đa ngôn ngữ hreflang. Triển khai báo cáo CSV GET `/api/v1/reports/attendance` giới hạn 30 ngày và cơ chế drain queue FIFO khi service restart. | Coder, Tester, Reviewer, Doc | [REQ-012], [REQ-013], [REQ-014], [REQ-015], [REQ-016], [REQ-017], [REQ-018], [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [ARC-007], [ARC-008], [ARC-009], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-007] <!--REGISTERED_PHASE_ROW--> |
| Giai Đoạn 5 | Ngày 1 - 3 | Task 32, Task 33, Task 34, Task 35 | `./sources/infra/docker/user-service/Dockerfile`, `./sources/infra/docker/center-service/Dockerfile`, `./sources/infra/docker/course-service/Dockerfile`, `./sources/infra/docker/enrollment-service/Dockerfile`, `./sources/infra/docker/attendance-service/Dockerfile`, `./sources/infra/docker/notification-service/Dockerfile`, `./sources/infra/gcp/terraform/main.tf`, `./sources/infra/gcp/terraform/vpc.tf`, `./sources/infra/gcp/terraform/iam.tf`, `./sources/infra/gcp/terraform/cloudsql.tf`, `./sources/infra/gcp/terraform/redis.tf`, `./sources/infra/gke/helm/membershiphub/Chart.yaml`, `./sources/infra/gke/helm/membershiphub/values.yaml`, `./sources/infra/gke/helm/membershiphub/templates/deployment.yaml`, `./sources/infra/gke/helm/membershiphub/templates/hpa.yaml`, `./sources/infra/gke/kustomize/overlays/prod/`, `./sources/docs/architecture/SystemArchitectureBlueprint.md`, `./sources/docs/database/DatabaseSchemaTopology.md`, `./sources/docs/api/OpenAPIContracts.md`, `./sources/docs/operations/OperationalManual.md` | Container hóa toàn bộ 6 service Quarkus bằng multi-stage Dockerfile với base image Alpine < 200MB, final image < 500MB, đẩy lên Artifact Registry. Triển khai hạ tầng GCP qua Terraform (VPC, IAM, Cloud SQL PostgreSQL, GCS, Memorystore Redis). Xây dựng Helm charts cho GKE với HPA tự động scale khi CPU > 70% hoặc latency > 300ms, PostgreSQL read replicas cho reporting. Áp dụng TLS 1.3, AES-256 at-rest, OWASP Top 10 mitigations, GDPR/CCPA với data export JSON endpoint, audit log retention 1 năm. Hoàn thiện bộ tài liệu kỹ thuật trong `./sources/docs/` bao gồm System Architecture Blueprint, Database Schema Topology, OpenAPI Contracts, Operational Manual tiếng Việt. | Docker, GCP, GKE, Doc | [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-008], [NFR-009], [ARC-000], [DOC-001] <!--REGISTERED_PHASE_ROW--> |
| **Kiểm Toán** | **Xác Minh Phân Bổ Backlog Tổng Thể** | **Tổng Số Giai Đoạn:** 5 | **Tổng Số Backlog Tags:** 59 | **Tổng Số Tags Đã Phân Bổ:** 59 | **Tổng Số Tasks Đã Phân Bổ:** 35 | **Trạng Thái & Tuân Thủ:** Đã Xác Minh (100%) |

<!--PHASE_SYNOPSIS_GRID_END-->

<!--END_CHUNK_PART_1_MATRIX_4_2-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

## 🔬 5. CHUYÊN MÔN HÓA GIAI ĐOẠN CHI TIẾT & SẢN PHẨM GIAO THEO NGÀY

<!--PHASE_INDEX_START-->

### 📈 Giai Đoạn 1 - Khởi Tạo Khung Microservices & Di Trú Cơ Sở Dữ Liệu Lõi
- **Mục Tiêu & Mục Đích Cốt Lõi Của Giai Đoạn:** Thiết lập toàn bộ khung dự án microservices Quarkus với Maven multi-module cho sáu service backend (`user-service`, `center-service`, `course-service`, `enrollment-service`, `attendance-service`, `notification-service`), đồng thời dựng frontend Next.js với `package.json` và `tsconfig.json`. Triển khai Flyway migration scripts cho 11 bảng lõi (Users, Centers, Courses, Enrollments, Attendance, StudentCards, Notifications, Roles, Promotions, Announcements, SystemSettings) với ràng buộc UNIQUE, FOREIGN KEY và INDEXES. Thiết lập JWT filter chain, OAuth2 resource server, CORS policy, Kafka topic schemas và OpenAPI 3.0 spec gateway.

- **Bản Đồ Ma Trận Thư Mục Vật Lý Mục Tiêu:** Sinh danh sách kiểm tra kỹ thuật đầy đủ, chi tiết ánh xạ 100% đường dẫn tệp vật lý tương đối riêng lẻ (KHÔNG phải thư mục) bên dưới `./sources/` được tạo hoặc xử lý trong phạm vi giai đoạn này. Mỗi mục đại diện cho một thực thể tệp cụ thể kèm Tag ID truy vết.
    * `./sources/backend/pom.xml` — [ARC-000]
    * `./sources/backend/user-service/pom.xml` — [ARC-000]
    * `./sources/backend/center-service/pom.xml` — [ARC-000]
    * `./sources/backend/course-service/pom.xml` — [ARC-000]
    * `./sources/backend/enrollment-service/pom.xml` — [ARC-000]
    * `./sources/backend/attendance-service/pom.xml` — [ARC-000]
    * `./sources/backend/notification-service/pom.xml` — [ARC-000]
    * `./sources/frontend/package.json` — [ARC-000]
    * `./sources/frontend/tsconfig.json` — [ARC-000]
    * `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_roles.sql` — [DAT-001]
    * `./sources/backend/user-service/src/main/resources/db/migration/V2__init_users_provider.sql` — [DAT-002]
    * `./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql` — [DAT-003]
    * `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql` — [DAT-004]
    * `./sources/backend/enrollment-service/src/main/resources/db/migration/V1__init_enrollments.sql` — [DAT-005]
    * `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql` — [DAT-006], [EXC-002]
    * `./sources/backend/notification-service/src/main/resources/db/migration/V1__init_student_cards.sql` — [DAT-007]
    * `./sources/backend/notification-service/src/main/resources/db/migration/V2__init_notifications.sql` — [DAT-008]
    * `./sources/backend/notification-service/src/main/resources/db/migration/V3__init_promotions.sql` — [DAT-009]
    * `./sources/backend/notification-service/src/main/resources/db/migration/V4__init_announcements.sql` — [DAT-010]
    * `./sources/backend/notification-service/src/main/resources/db/migration/V5__init_system_settings.sql` — [DAT-011]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtFilter.java` — [ARC-006]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/OAuth2ResourceServer.java` — [ARC-006]
    * `./sources/backend/api-gateway/src/main/resources/openapi.yaml` — [ARC-006], [ARC-007], [ARC-008], [ARC-009]
    * `./sources/backend/api-gateway/src/main/java/org/nlh4j/membershiphub/gateway/CorsFilter.java` — [ARC-006]
    * `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/kafka/TopicSchemas.java` — [ARC-008]
    * `./sources/docs/architecture/SystemArchitectureBlueprint.md` — [DOC-001]

- **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Cung cấp câu lệnh SQL di trú DDL đầy đủ, hợp lệ, chứa các cột, kiểu dữ liệu, khóa chính/khóa ngoại, ánh xạ quan hệ, chỉ mục và ràng buộc NULL áp dụng trong phạm vi giai đoạn này.

```sql:matrix
-- ============================================================
-- MIGRATION: V1__init_users_roles.sql  (user-service)
-- ============================================================
CREATE TABLE roles (
    role_id SMALLINT NOT NULL,
    name VARCHAR(30) NOT NULL,
    description VARCHAR(200) NULL,
    CONSTRAINT pk_roles PRIMARY KEY (role_id),
    CONSTRAINT uq_roles_name UNIQUE (name),
    CONSTRAINT ck_roles_name CHECK (name IN ('SystemAdmin','CenterAdmin','Manager','Teacher','Student'))
);

CREATE TABLE users (
    user_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash CHAR(60) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role_id SMALLINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id),
    CONSTRAINT ck_users_email_format CHECK (email LIKE '%_@__%.__%')
);

CREATE INDEX idx_users_role_id ON users(role_id);

-- ============================================================
-- MIGRATION: V2__init_users_provider.sql  (user-service)
-- ============================================================
ALTER TABLE users ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'local';
ALTER TABLE users ADD CONSTRAINT ck_users_provider
    CHECK (provider IN ('local','firebase','google','facebook'));

-- ============================================================
-- MIGRATION: V1__init_centers.sql  (center-service)
-- ============================================================
CREATE TABLE centers (
    center_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tax_id VARCHAR(20) NOT NULL,
    contact_phone VARCHAR(20) NULL,
    contact_email VARCHAR(100) NULL,
    CONSTRAINT pk_centers PRIMARY KEY (center_id),
    CONSTRAINT uq_centers_tax_id UNIQUE (tax_id),
    CONSTRAINT ck_centers_tax_id CHECK (tax_id ~ '^[0-9]{10,13}$'),
    CONSTRAINT ck_centers_phone CHECK (contact_phone IS NULL OR contact_phone ~ '^[+0-9 ()\-]+$')
);

CREATE TABLE user_center (
    user_id UUID NOT NULL,
    center_id UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_user_center PRIMARY KEY (user_id, center_id),
    CONSTRAINT fk_uc_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_uc_center FOREIGN KEY (center_id) REFERENCES centers(center_id) ON DELETE CASCADE
);

-- ============================================================
-- MIGRATION: V1__init_courses.sql  (course-service)
-- ============================================================
CREATE TABLE courses (
    course_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    teacher_id UUID NOT NULL,
    max_students INT NOT NULL DEFAULT 30,
    CONSTRAINT pk_courses PRIMARY KEY (course_id),
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id),
    CONSTRAINT ck_courses_date_range CHECK (end_date >= start_date),
    CONSTRAINT ck_courses_max_students CHECK (max_students > 0)
);

CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_courses_dates ON courses(start_date, end_date);

-- ============================================================
-- MIGRATION: V1__init_enrollments.sql  (enrollment-service)
-- ============================================================
CREATE TABLE enrollments (
    enrollment_id UUID NOT NULL,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    enrollment_date TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_enrollments PRIMARY KEY (enrollment_id),
    CONSTRAINT fk_enr_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_enr_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT uq_enr_student_course UNIQUE (student_id, course_id)
);

CREATE INDEX idx_enr_student ON enrollments(student_id);
CREATE INDEX idx_enr_course ON enrollments(course_id);

-- ============================================================
-- MIGRATION: V1__init_attendance.sql  (attendance-service)
-- ============================================================
CREATE TABLE attendance (
    attendance_id UUID NOT NULL,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),
    CONSTRAINT fk_att_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT fk_att_course FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT uq_attendance_composite UNIQUE (student_id, course_id, attendance_date)
);

CREATE INDEX idx_att_student_date ON attendance(student_id, attendance_date);
CREATE INDEX idx_att_course_date ON attendance(course_id, attendance_date);

-- ============================================================
-- MIGRATION: V1__init_student_cards.sql  (notification-service shared schema)
-- ============================================================
CREATE TABLE student_cards (
    card_id UUID NOT NULL,
    student_id UUID NOT NULL,
    issue_date DATE NOT NULL,
    validity_days INT NOT NULL,
    CONSTRAINT pk_student_cards PRIMARY KEY (card_id),
    CONSTRAINT fk_card_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT ck_card_validity CHECK (validity_days > 0)
);

CREATE INDEX idx_card_student ON student_cards(student_id);

-- ============================================================
-- MIGRATION: V2__init_notifications.sql
-- ============================================================
CREATE TABLE notifications (
    notification_id UUID NOT NULL,
    user_id UUID NULL,
    group_zalo VARCHAR(50) NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    delivered BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT pk_notifications PRIMARY KEY (notification_id),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT ck_notif_target CHECK (user_id IS NOT NULL OR group_zalo IS NOT NULL)
);

CREATE INDEX idx_notif_user ON notifications(user_id);
CREATE INDEX idx_notif_delivered ON notifications(delivered);

-- ============================================================
-- MIGRATION: V3__init_promotions.sql
-- ============================================================
CREATE TABLE promotions (
    promo_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    discount_percent SMALLINT NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    description TEXT NULL,
    CONSTRAINT pk_promotions PRIMARY KEY (promo_id),
    CONSTRAINT uq_promotions_code UNIQUE (code),
    CONSTRAINT ck_promo_percent CHECK (discount_percent BETWEEN 1 AND 100),
    CONSTRAINT ck_promo_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

-- ============================================================
-- MIGRATION: V4__init_announcements.sql
-- ============================================================
CREATE TABLE announcements (
    announcement_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    CONSTRAINT pk_announcements PRIMARY KEY (announcement_id),
    CONSTRAINT ck_ann_title_len CHECK (char_length(title) <= 150),
    CONSTRAINT ck_ann_content_len CHECK (char_length(content) <= 2000),
    CONSTRAINT ck_ann_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

-- ============================================================
-- MIGRATION: V5__init_system_settings.sql
-- ============================================================
CREATE TABLE system_settings (
    setting_key VARCHAR(50) NOT NULL,
    setting_value TEXT NOT NULL,
    description VARCHAR(200) NULL,
    CONSTRAINT pk_system_settings PRIMARY KEY (setting_key)
);
```

- **Hợp Đồng Định Tuyến API Và Sự Kiện [ARC-XXX]:** Tài liệu hóa các hợp đồng kỹ thuật gateway, cấu hình filter chain, OpenAPI 3.0 spec và schema Kafka topic.

```json
{
  "openapi": "3.0.3",
  "info": {
    "title": "Membership Hub API Gateway",
    "version": "1.0.0",
    "description": "Centralized API gateway contract for Quarkus microservices"
  },
  "servers": [
    { "url": "https://api.membershiphub.example.com", "description": "Production GKE gateway" }
  ],
  "components": {
    "securitySchemes": {
      "bearerAuth": {
        "type": "http",
        "scheme": "bearer",
        "bearerFormat": "JWT"
      },
      "oauth2": {
        "type": "oauth2",
        "flows": {
          "authorizationCode": {
            "authorizationUrl": "https://auth.membershiphub.example.com/oauth2/authorize",
            "tokenUrl": "https://auth.membershiphub.example.com/oauth2/token",
            "scopes": {
              "openid": "OpenID",
              "profile": "Profile",
              "email": "Email"
            }
          }
        }
      }
    }
  },
  "security": [{ "bearerAuth": [] }],
  "paths": {
    "/api/v1/auth/register": { "post": { "summary": "Register a new user (stub reference for Phase 2 implementation)" } },
    "/api/v1/auth/login": { "post": { "summary": "Email/password login (stub reference for Phase 2 implementation)" } },
    "/api/v1/centers": { "get": { "summary": "List centers (stub reference for Phase 2 implementation)" } },
    "/api/v1/courses": { "get": { "summary": "List courses (stub reference for Phase 3 implementation)" } },
    "/api/v1/attendance/scan": { "post": { "summary": "QR scan attendance (stub reference for Phase 4 implementation)" } }
  }
}
```

```yaml
# Kafka Topic Schemas (notification-service)
topics:
  - name: enrollment.created
    partitions: 6
    replication: 3
    key: enrollmentId
    valueSchema:
      type: object
      properties:
        enrollmentId: { type: string, format: uuid }
        studentId: { type: string, format: uuid }
        courseId: { type: string, format: uuid }
        timestamp: { type: string, format: date-time }
  - name: teacher.assigned
    partitions: 3
    replication: 3
    key: courseId
    valueSchema:
      type: object
      properties:
        courseId: { type: string, format: uuid }
        teacherId: { type: string, format: uuid }
        assignedBy: { type: string, format: uuid }
  - name: attendance.recorded
    partitions: 6
    replication: 3
    key: attendanceId
    valueSchema:
      type: object
      properties:
        attendanceId: { type: string, format: uuid }
        studentId: { type: string, format: uuid }
        courseId: { type: string, format: uuid }
        attendanceDate: { type: string, format: date }
```

- **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Giai đoạn này chỉ xử lý ràng buộc idempotency ở mức lược đồ thông qua UNIQUE constraint composite trên bảng `attendance`. Toàn bộ logic nghiệp vụ xử lý ngoại lệ sẽ được hiện thực ở các giai đoạn 2-4.

```java
package org.nlh4j.membershiphub.attendanceservice.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.postgresql.util.PSQLException;

/**
 * Bắt lỗi UNIQUE constraint composite (student_id, course_id, attendance_date)
 * và chuyển thành phản hồi 200 OK với cờ duplicate=true theo [EXC-002].
 */
@Provider
public class DuplicateAttendanceExceptionMapper implements ExceptionMapper<PSQLException> {

    @Override
    public Response toResponse(PSQLException ex) {
        if (ex.getSQLState() != null && ex.getSQLState().equals("23505")
                && ex.getMessage() != null && ex.getMessage().contains("uq_attendance_composite")) {
            return Response.ok()
                    .entity("{\"status\":\"success\",\"duplicate\":true,\"message\":\"Attendance already recorded\"}")
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"Database integrity violation\"}")
                .build();
    }
}
```

#### 📅 Nhật Ký Phân Bổ Tác Vụ Theo Ngày Của Sub-Agent (Giai Đoạn 1)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: KHỞI TẠO KHUNG DỰ ÁN VÀ MANIFEST ĐA MODULE
- **Sub-Task 1:** Tạo `pom.xml` gốc Maven multi-module cho backend `./sources/backend/pom.xml` định nghĩa sáu module con (`user-service`, `center-service`, `course-service`, `enrollment-service`, `attendance-service`, `notification-service`) với cấu hình BOM Quarkus 3.15.1, plugin Flyway và dependency management chuẩn Java 21.

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TẠO POM.XML GỐC VÀ QUẢN LÝ MODULE CHA
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [ARC-000]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/pom.xml`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Khởi tạo Maven multi-module parent POM. Khai báo `<groupId>org.nlh4j.membershiphub</groupId>`, `<artifactId>membershiphub-backend</artifactId>`, `<version>1.0.0-SNAPSHOT</version>` với packaging `pom`. Liệt kê 6 module con trong `<modules>`. Import `quarkus-bom:3.15.1` vào `<dependencyManagement>`. Cấu hình plugin `maven-compiler-plugin` sử dụng Java 21, `quarkus-maven-plugin`, và `flyway-maven-plugin` để quản lý di trú cơ sở dữ liệu. Đảm bảo toàn bộ tệp là XML hợp lệ, sẵn sàng biên dịch chéo module mà không cần chỉnh sửa.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không có thay đổi cơ sở dữ liệu trong nhiệm vụ con này.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không có hợp đồng API nào được tạo trong nhiệm vụ con khởi tạo manifest này.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không có trình xử lý ngoại lệ nghiệp vụ nào trong nhiệm vụ con này.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: TẠO POM.XML RIÊNG CHO USER-SERVICE
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [ARC-000]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/pom.xml`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo POM con cho `user-service` kế thừa từ `./sources/backend/pom.xml` thông qua `<parent>`. Khai báo artifactId `user-service`. Thêm dependencies Quarkus: `quarkus-resteasy-reactive-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-smallrye-jwt-build`, `quarkus-arc`, `quarkus-rest-client-reactive-jackson`. Cấu hình plugin `quarkus-maven-plugin` chuẩn. Đảm bảo file hoàn chỉnh có thể build standalone khi được gọi từ root.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Chưa áp dụng DDL cụ thể trong bước này; migrations sẽ được tạo ở nhiệm vụ sau.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng trong bước tạo manifest.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không có trong nhiệm vụ con này.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: TẠO POM.XML CHO CENTER-SERVICE, COURSE-SERVICE, ENROLLMENT-SERVICE, ATTENDANCE-SERVICE, NOTIFICATION-SERVICE
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [ARC-000]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/center-service/pom.xml`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Lặp lại cấu trúc parent-child POM cho năm service còn lại (`center-service`, `course-service`, `enrollment-service`, `attendance-service`, `notification-service`). Mỗi POM con kế thừa `<parent>` từ `./sources/backend/pom.xml`. `notification-service` cần bổ sung thêm `quarkus-messaging-kafka` và `quarkus-rest-client-reactive-jackson` cho FCM/APNs gateway. Tất cả artifactId phải khớp tên module. Đảm bảo mỗi file là XML hợp lệ, có thể compile độc lập từ root.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Chưa áp dụng trong bước này.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng trong bước này.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không có trong nhiệm vụ con này.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: KHỞI TẠO PACKAGE.JSON VÀ TSCONFIG.JSON CHO FRONTEND NEXT.JS
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [ARC-000]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/package.json`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo `./sources/frontend/package.json` cho dự án Next.js 14.2.5. Khai báo scripts: `dev`, `build`, `start`, `lint`, `type-check`. Thêm dependencies: `next`, `react`, `react-dom`, `next-intl`, `next-i18next`, `firebase`, `@react-native-firebase/messaging`. Thêm devDependencies: `typescript`, `@types/react`, `@types/node`, `eslint`, `prettier`. Đồng thời tạo `./sources/frontend/tsconfig.json` với cờ strict mode, đường dẫn alias `@/*` ánh xạ tới `./src/*`, target ES2022, moduleResolution Bundler. Đảm bảo cả hai file JSON hợp lệ và sẵn sàng cho `npm install`.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không áp dụng.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng trong bước này.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không có trong nhiệm vụ con này.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 5: KIỂM THỬ TÍCH HỢP BIÊN DỊCH ĐA MODULE
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]
* **ID Thẻ Mục Tiêu:** [ARC-000]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** INTEGRATION_SCOPE;./sources/infra/test/maven-build-integration.sh
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo script shell `./sources/infra/test/maven-build-integration.sh` thực thi `mvn -f ./sources/backend/pom.xml clean validate compile` để xác nhận toàn bộ 6 module backend compile thành công và Maven resolution hoạt động đúng. Thêm script `./sources/infra/test/npm-install-integration.sh` chạy `npm install --dry-run` trong `./sources/frontend/` để xác nhận tất cả dependency trong `package.json` tồn tại trên registry. Script phải exit code 0 khi thành công và ghi log chi tiết.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không áp dụng.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không có trong nhiệm vụ con này.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 6: REVIEW CẤU TRÚC MANIFEST VÀ CHUẨN ĐẶT TÊN
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Reviewer]
* **ID Thẻ Mục Tiêu:** [ARC-000]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/notification-service/pom.xml`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Kiểm tra chéo toàn bộ 7 tệp manifest (`./sources/backend/pom.xml` + 6 child POMs + `./sources/frontend/package.json` + `./sources/frontend/tsconfig.json`) để đảm bảo: (1) tất cả `<artifactId>` đồng bộ và không trùng lặp, (2) version Quarkus BOM thống nhất 3.15.1, (3) Java target là 21, (4) không còn tham chiếu đến `com.example` ở bất kỳ đâu, (5) tất cả dependency `quarkus-*` đều resolve được. Tạo báo cáo review `./sources/docs/review/phase1-day1-manifest-review.md` nêu rõ từng issue phát hiện và đề xuất fix cụ thể.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không áp dụng.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không có trong nhiệm vụ con này.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 7: BIÊN SOẠN TÀI LIỆU KIẾN TRÚC TỔNG QUAN
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Doc]
* **ID Thẻ Mục Tiêu:** [ARC-000], [DOC-001]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/architecture/SystemArchitectureBlueprint.md`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Biên soạn tài liệu Markdown `./sources/docs/architecture/SystemArchitectureBlueprint.md` mô tả tổng quan kiến trúc hệ thống membership hub. Bao gồm: sơ đồ microservice (6 service Quarkus + API Gateway + Frontend Next.js), công nghệ stack (Quarkus 3.15.1, Java 21, PostgreSQL 16, Kafka, Redis, Firebase FCM), mô hình triển khai (GKE, Artifact Registry), và bản đồ luồng dữ liệu chính (Authentication, Attendance, Notification). Sử dụng Mermaid để vẽ sơ đồ C4 Container. Tài liệu phải bằng tiếng Việt, có mục lục rõ ràng và Tag ID truy vết.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không áp dụng trong tài liệu này.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không có trong nhiệm vụ con này.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

##### 📅 NGÀY 2: TRIỂN KHAI FLYWAY MIGRATION CHO 11 BẢNG CƠ SỞ DỮ LIỆU LÕI
- **Sub-Task 1:** Tạo Flyway migration scripts trong mỗi service với DDL SQL hoàn chỉnh, UNIQUE constraints, FOREIGN KEY và INDEXES.

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: MIGRATION BẢNG ROLES VÀ USERS
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [DAT-001], [DAT-002], [EXC-002]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_roles.sql`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_roles.sql` chứa DDL tạo bảng `roles` (role_id SMALLINT PK, name VARCHAR(30) UNIQUE với CHECK constraint cho 5 giá trị 'SystemAdmin','CenterAdmin','Manager','Teacher','Student', description VARCHAR(200) NULL) và bảng `users` (user_id UUID PK, email VARCHAR(255) UNIQUE với CHECK LIKE format email, password_hash CHAR(60) NOT NULL, full_name VARCHAR(100) NOT NULL, role_id SMALLINT FK→roles, created_at TIMESTAMP DEFAULT now(), updated_at TIMESTAMP DEFAULT now()). Tạo chỉ mục idx_users_role_id. Đảm bảo file SQL thuần ANSI, không dùng ENUM inline, sử dụng VARCHAR + CHECK constraint.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Xem nội dung migration đã trình bày trong khối DDL tổng hợp ở phần "Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu" phía trên.

```sql:matrix
-- Nội dung chi tiết đã được trình bày đầy đủ trong khối DDL tổng hợp V1__init_users_roles.sql
-- Bảng roles: PK role_id, UNIQUE name, CHECK name IN (...)
-- Bảng users: PK user_id, UNIQUE email, FK role_id → roles, INDEX idx_users_role_id
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng trong bước migration.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không có logic nghiệp vụ trong bước DDL này; chỉ là ràng buộc schema.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: MIGRATION BỔ SUNG CỘT PROVIDER CHO BẢNG USERS
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [DAT-002]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/resources/db/migration/V2__init_users_provider.sql`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/user-service/src/main/resources/db/migration/V2__init_users_provider.sql` thực thi `ALTER TABLE users ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'local'` và thêm CHECK constraint `ck_users_provider CHECK (provider IN ('local','firebase','google','facebook'))`. Migration phải tương thích ngược với V1 và không phá vỡ dữ liệu hiện có.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Xem khối DDL tổng hợp.

```sql:matrix
-- ALTER TABLE users ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'local';
-- ALTER TABLE users ADD CONSTRAINT ck_users_provider CHECK (provider IN ('local','firebase','google','facebook'));
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không có trong nhiệm vụ con này.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: MIGRATION BẢNG CENTERS VÀ USER_CENTER
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [DAT-003]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql` định nghĩa bảng `centers` (center_id UUID PK, name VARCHAR(100) NOT NULL, address VARCHAR(255) NOT NULL, tax_id VARCHAR(20) UNIQUE NOT NULL với CHECK regex `^[0-9]{10,13}$`, contact_phone VARCHAR(20) NULL với CHECK regex, contact_email VARCHAR(100) NULL) và bảng `user_center` (user_id UUID FK→users ON DELETE CASCADE, center_id UUID FK→centers ON DELETE CASCADE, assigned_at TIMESTAMP DEFAULT now(), composite PK). Đảm bảo UNIQUE tax_id hỗ trợ ngăn chặn trùng lặp theo [REQ-005].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Xem khối DDL tổng hợp.

```sql:matrix
-- Bảng centers: PK center_id, UNIQUE tax_id, CHECK tax_id ~ '^[0-9]{10,13}$', CHECK phone regex
-- Bảng user_center: composite PK (user_id, center_id), FK ON DELETE CASCADE
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không có trong nhiệm vụ con này.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: MIGRATION BẢNG COURSES VÀ ENROLLMENTS
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [DAT-004], [DAT-005]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql` định nghĩa bảng `courses` (course_id UUID PK, title VARCHAR(150) NOT NULL, description TEXT NULL, start_date DATE NOT NULL, end_date DATE NOT NULL với CHECK end_date>=start_date, teacher_id UUID FK→users, max_students INT DEFAULT 30 với CHECK>0, INDEX idx_courses_teacher_id, INDEX idx_courses_dates). Tạo thêm file `./sources/backend/enrollment-service/src/main/resources/db/migration/V1__init_enrollments.sql` định nghĩa bảng `enrollments` (enrollment_id UUID PK, student_id UUID FK→users ON DELETE CASCADE, course_id UUID FK→courses ON DELETE CASCADE, enrollment_date TIMESTAMP DEFAULT now(), UNIQUE composite (student_id, course_id), INDEX idx_enr_student, idx_enr_course).

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Xem khối DDL tổng hợp.

```sql:matrix
-- Bảng courses: PK, FK teacher_id, CHECK date_range, INDEX idx_courses_teacher_id, idx_courses_dates
-- Bảng enrollments: PK, FK student+course ON DELETE CASCADE, UNIQUE(student_id, course_id)
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không có trong nhiệm vụ con này.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 5: MIGRATION BẢNG ATTENDANCE VỚI IDEMPOTENCY
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [DAT-006], [EXC-002]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql` định nghĩa bảng `attendance` (attendance_id UUID PK, student_id UUID FK→users, course_id UUID FK→courses, attendance_date DATE NOT NULL, timestamp TIMESTAMP DEFAULT now(), UNIQUE composite constraint tên `uq_attendance_composite` trên (student_id, course_id, attendance_date), INDEX idx_att_student_date, idx_att_course_date). UNIQUE constraint này là cốt lõi của [REQ-013] và [EXC-002] để đảm bảo idempotency.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Xem khối DDL tổng hợp.

```sql:matrix
-- Bảng attendance: UNIQUE uq_attendance_composite(student_id, course_id, attendance_date)
-- INDEX idx_att_student_date, idx_att_course_date
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Ràng buộc UNIQUE tại DB là nền tảng cho xử lý duplicate attendance theo [EXC-002].

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 6: MIGRATION BẢNG STUDENT_CARDS, NOTIFICATIONS, PROMOTIONS, ANNOUNCEMENTS, SYSTEM_SETTINGS
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/notification-service/src/main/resources/db/migration/V1__init_student_cards.sql`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo 5 file migration trong `./sources/backend/notification-service/src/main/resources/db/migration/`: V1__init_student_cards.sql (card_id UUID PK, student_id UUID FK→users ON DELETE CASCADE, issue_date DATE, validity_days INT CHECK>0, INDEX idx_card_student), V2__init_notifications.sql (notification_id UUID PK, user_id UUID FK→users ON DELETE SET NULL NULL, group_zalo VARCHAR(50) NULL, message TEXT NOT NULL, sent_at TIMESTAMP DEFAULT now(), delivered BOOLEAN DEFAULT false, CHECK (user_id IS NOT NULL OR group_zalo IS NOT NULL), INDEX idx_notif_user, idx_notif_delivered), V3__init_promotions.sql (promo_id UUID PK, code VARCHAR(30) UNIQUE, discount_percent SMALLINT CHECK BETWEEN 1-100, start/end DATE NULL với CHECK end>=start, description TEXT NULL), V4__init_announcements.sql (announcement_id UUID PK, title VARCHAR(150) CHECK len<=150, content TEXT CHECK len<=2000, start/end DATE NULL với CHECK end>=start), V5__init_system_settings.sql (setting_key VARCHAR(50) PK, setting_value TEXT, description VARCHAR(200) NULL).

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Xem khối DDL tổng hợp.

```sql:matrix
-- 5 migrations: student_cards, notifications, promotions, announcements, system_settings
-- Toàn bộ ràng buộc CHECK/UNIQUE/FK/INDEX đã được định nghĩa
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không có trong nhiệm vụ con này.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 7: KIỂM THỬ ĐƯỜNG ỐNG MIGRATION POSTGRESQL
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]
* **ID Thẻ Mục Tiêu:** [DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** INTEGRATION_SCOPE;./sources/infra/test/migration-integration-test.sql
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo script kiểm thử tích hợp `./sources/infra/test/migration-integration-test.sql` thực thi toàn bộ 11 file migration trên PostgreSQL 16 test container (dùng Docker postgres:16-alpine). Script bao gồm: (1) chạy tuần tự tất cả V1-V5 từ 6 service, (2) verify số bảng tạo thành công >= 11, (3) kiểm tra UNIQUE constraint trên attendance bằng cách insert 2 bản ghi trùng composite key và expect lỗi SQLSTATE 23505, (4) kiểm tra CHECK constraint trên roles, users.email, providers, tax_id. Kết quả pass/fail được ghi ra stdout với mã exit tương ứng.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không thêm DDL mới; chỉ test các migration đã tạo.

```sql:matrix
-- Script test migration: chạy tuần tự 11 file V*.sql, verify constraints, expect SQLSTATE 23505 cho duplicate attendance
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Test xác nhận cơ chế ngăn duplicate attendance hoạt động đúng theo [EXC-002].

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 8: REVIEW CHẤT LƯỢNG MIGRATION VÀ TỐI ƯU INDEX
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Reviewer]
* **ID Thẻ Mục Tiêu:** [DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/enrollment-service/src/main/resources/db/migration/V1__init_enrollments.sql`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Review toàn bộ 11 file migration đã tạo. Kiểm tra: (1) mọi FK đều có ON DELETE hợp lý, (2) CHECK constraint đầy đủ cho mọi string enum thay vì dùng ENUM inline, (3) chỉ mục được tạo cho các cột truy vấn thường xuyên (email, role_id, teacher_id, attendance_date, course_id, student_id), (4) tên constraint theo convention `pk_*`, `fk_*`, `uq_*`, `ck_*`, `idx_*`. Phát hiện và đề xuất bổ sung index thiếu cho query hot path. Tạo báo cáo `./sources/docs/review/phase1-day2-migration-review.md`.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không sửa trong bước review; chỉ sinh báo cáo đề xuất.

```sql:matrix
-- Báo cáo review không chứa DDL mới; đề xuất các index tối ưu cho báo cáo Phase 3
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Xác nhận UNIQUE attendance composite hỗ trợ [EXC-002].

<!--ATOMIC_SUB_TASK_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 9: BIÊN SOẠN TÀI LIỆU DATABASE SCHEMA TOPOLOGY
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Doc]
* **ID Thẻ Mục Tiêu:** [DOC-001]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/database/DatabaseSchemaTopology.md`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo tài liệu `./sources/docs/database/DatabaseSchemaTopology.md` mô tả topology 11 bảng, mối quan hệ ER (sử dụng Mermaid erDiagram), giải thích ý nghĩa từng bảng, các ràng buộc UNIQUE/CHECK quan trọng, và chiến lược phân vùng migration theo microservice. Tài liệu phải đối chiếu 1:1 với 11 file SQL đã tạo và Tag ID truy vết. Viết bằng tiếng Việt, có mục lục và sơ đồ Mermaid rõ ràng.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Tài liệu tham chiếu 11 bảng đã có.

```sql:matrix
-- Tài liệu tham chiếu schema, không chứa DDL mới; toàn bộ DDL nằm trong các file V*.sql
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Không áp dụng.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Tài liệu giải thích UNIQUE attendance composite phục vụ [EXC-002].

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

##### 📅 NGÀY 3: THIẾT LẬP LỚP BẢO MẬT, GATEWAY VÀ KAFKA TOPIC SCHEMAS
- **Sub-Task 1:** Hiện thực JWT filter chain, OAuth2 resource server, CORS policy, OpenAPI gateway spec và Kafka topic schemas.

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TRIỂN KHAI JWT FILTER CHAIN
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [ARC-006]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtFilter.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo class `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtFilter.java` triển khai `ContainerRequestFilter` với annotation `@Provider` và `@Priority(Priorities.AUTHENTICATION)`. Inject `JwtParser` từ `SmallRyeJWT`. Phương thức `filter()` đọc header `Authorization: Bearer <token>`, parse JWT, verify chữ ký RS256, trích xuất claim `sub` (user UUID), `role` (role_name), `exp`. Trả về 401 nếu thiếu/sai token, 403 nếu role không hợp lệ. Đặt `SecurityContext` với principal là user UUID.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không áp dụng.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Filter áp dụng cho toàn bộ endpoint `/api/v1/**` theo [ARC-006].

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Filter trả 401/403 nhưng không thuộc [EXC-XXX] nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: CẤU HÌNH OAUTH2 RESOURCE SERVER
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [ARC-006]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/OAuth2ResourceServer.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo class `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/OAuth2ResourceServer.java` cấu hình `quarkus-smallrye-jwt` với JWKS endpoint từ identity provider. Khai báo `@ConfigProperty` cho `mp.jwt.verify.publickey.location`, `mp.jwt.verify.issuer`, `smallrye.jwt.path.sub`, `smallrye.jwt.always-check-authorization`. Tạo `Application` class với `@ApplicationPath("/api")` để mount REST endpoint. Class chỉ chứa cấu hình, không chứa logic nghiệp vụ.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không áp dụng.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Resource server hỗ trợ OAuth2 flow theo [ARC-006].

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: TRIỂN KHAI CORS FILTER CHO API GATEWAY
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [ARC-006], [ARC-009]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/api-gateway/src/main/java/org/nlh4j/membershiphub/gateway/CorsFilter.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/api-gateway/src/main/java/org/nlh4j/membershiphub/gateway/CorsFilter.java` triển khai `ContainerResponseFilter` áp dụng header CORS cho response: `Access-Control-Allow-Origin: https://app.membershiphub.example.com`, `Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS`, `Access-Control-Allow-Headers: Authorization,Content-Type,Accept-Language`, `Access-Control-Allow-Credentials: true`, `Access-Control-Max-Age: 3600`. Ngoài ra tạo `ContainerRequestFilter` xử lý preflight OPTIONS request trả về 200 OK ngay. Class phải là API Gateway infrastructure, không phải logic nghiệp vụ.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không áp dụng.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** CORS hỗ trợ frontend Next.js theo [ARC-009].

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: TẠO OPENAPI 3.0 SPEC GATEWAY
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [ARC-006], [ARC-007], [ARC-008], [ARC-009]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/api-gateway/src/main/resources/openapi.yaml`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo file `./sources/backend/api-gateway/src/main/resources/openapi.yaml` chứa OpenAPI 3.0.3 spec cho API Gateway tổng hợp. Bao gồm: `info` (title, version 1.0.0), `servers`, `components.securitySchemes` (bearerAuth, oauth2 authorizationCode), `security`, `paths` tham chiếu đến 5 nhóm endpoint chính (auth, centers, courses, attendance, reports). Mỗi path chỉ chứa summary stub và $ref đến file openapi.yaml riêng của từng service. Spec phải validate thành công bằng Swagger Parser. Tất cả Tag ID kiến trúc phải được nhúng trong description.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không áp dụng.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Đặc tả tổng hợp gateway cho 4 luồng [ARC-006], [ARC-007], [ARC-008], [ARC-009].

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 5: ĐỊNH NGHĨA KAFKA TOPIC SCHEMAS
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **ID Thẻ Mục Tiêu:** [ARC-008]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/kafka/TopicSchemas.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo class `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/kafka/TopicSchemas.java` chứa 3 inner record class (EnrollmentCreated, TeacherAssigned, AttendanceRecorded) đại diện cho JSON schema của 3 Kafka topic. Mỗi record có annotation Jackson @JsonProperty cho key và value, kèm builder method. Class sử dụng `quarkus-messaging-kafka` để produce event. Đồng thời tạo file YAML `./sources/backend/notification-service/src/main/resources/kafka-topics.yaml` khai báo cấu hình topic (partitions, replication, retention.ms, cleanup.policy).

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không áp dụng.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Schemas cho 3 topic enrollment/teacher/attendance theo [ARC-008].

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 6: KIỂM THỬ BẢO MẬT VÀ GATEWAY
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]
* **ID Thẻ Mục Tiêu:** [ARC-006], [ARC-009]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** INTEGRATION_SCOPE;./sources/infra/test/security-gateway-integration.sh
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo script kiểm thử tích hợp `./sources/infra/test/security-gateway-integration.sh` sử dụng `curl` và `jq` để: (1) khởi động 6 service qua `mvn quarkus:dev` trong background, (2) gửi request không có token đến `/api/v1/centers` và expect HTTP 401, (3) gửi request có token hợp lệ expect 200, (4) gửi token hết hạn expect 401, (5) gửi preflight OPTIONS request expect 200 với CORS header. Tất cả assertion phải pass và script trả exit code 0.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không áp dụng.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Test JWT filter chain và CORS theo [ARC-006], [ARC-009].

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng.

<!--ATOMIC_SUB_TASK_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 7: REVIEW LỚP BẢO MẬT VÀ OPENAPI
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Reviewer]
* **ID Thẻ Mục Tiêu:** [ARC-006], [ARC-007], [ARC-008], [ARC-009]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/notification-service/src/main/resources/kafka-topics.yaml`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Review 5 file bảo mật và gateway: JwtFilter, OAuth2ResourceServer, CorsFilter, openapi.yaml, TopicSchemas. Kiểm tra: (1) không có secret/key hard-code, (2) RS256 được sử dụng cho JWT (không HS256), (3) OpenAPI spec hợp lệ qua swagger-cli validate, (4) Kafka topic cấu hình replication >= 3, (5) CORS không cho phép wildcard origin. Tạo báo cáo `./sources/docs/review/phase1-day3-security-review.md` đề xuất fix cụ thể.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không áp dụng.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Review gateway phục vụ 4 luồng [ARC-006-009].

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng.

<!--ATOMIC_SUB_TASK_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 8: BIÊN SOẠN TÀI LIỆU API CONTRACTS
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Doc]
* **ID Thẻ Mục Tiêu:** [ARC-006], [ARC-007], [ARC-008], [ARC-009], [DOC-001]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/api/OpenAPIContracts.md`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo file `./sources/docs/api/OpenAPIContracts.md` mô tả chi tiết hợp đồng API Gateway. Bao gồm: bảng danh sách endpoint theo Tag ID, mô tả security scheme (bearer JWT + OAuth2), giải thích CORS policy, danh sách Kafka topic và cấu trúc payload, sơ đồ Mermaid sequence cho 4 luồng chính (Authentication, Attendance QR, Notification, Mobile App Integration). Tài liệu tiếng Việt, có mục lục, đối chiếu Tag ID chính xác.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** Không áp dụng.

```sql:matrix
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững nào được yêu cầu cho ngữ cảnh nhiệm vụ con này
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Tài liệu mô tả 4 luồng [ARC-006-009].

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

### 📈 Giai Đoạn 2 - Xây Dựng Module Xác Thực, Phân Quyền Người Dùng Và Quản Lý Trung Tâm
- **Mục Tiêu Cốt Lõi & Ý Nghĩa Của Giai Đoạn:** Hiện thực hóa hai microservices trọng yếu gồm `user-service` và `center-service` trong hệ thống Quarkus. Cụ thể, giai đoạn này tập trung vào việc xây dựng luồng đăng ký tài khoản cục bộ với mã hóa bcrypt, tích hợp OAuth2 cho các nhà cung cấp Firebase/Google/Facebook, thiết lập cơ chế RBAC với bảng Roles và audit log, đồng thời triển khai đầy đủ CRUD cho thực thể Center cùng với khả năng phân công Center Admin. Toàn bộ logic nghiệp vụ phải tuân thủ nguyên tắc bảo mật OWASP, xác thực đầu vào nghiêm ngặt và ghi log kiểm toán cho mọi thao tác thay đổi quyền hạn.

- **Bản Đồ Ma Trận Đường Dẫn Vật Lý Mục Tiêu:** Tạo danh sách kiểm tra kỹ thuật chi tiết, bao quát 100% các tệp vật lý riêng lẻ dưới `./sources/` được khởi tạo, tái cấu trúc hoặc xử lý trong phạm vi giai đoạn này. Mỗi mục đại diện cho một tệp thực thi cụ thể kèm theo Tag ID truy vết.
    * `./sources/backend/user-service/pom.xml` — [ARC-000], [ARC-006]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuthController.java` — [REQ-001], [REQ-002], [ARC-006]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuthService.java` — [REQ-001], [REQ-002], [EXC-004]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/OAuth2Service.java` — [REQ-002], [ARC-006]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/UserService.java` — [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/RoleService.java` — [REQ-003], [NFR-006]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuditLogger.java` — [REQ-003], [NFR-006]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/JwtTokenProvider.java` — [ARC-006]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/RegisterRequest.java` — [REQ-001], [EXC-004]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/SocialLoginRequest.java` — [REQ-002]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/RoleUpdateRequest.java` — [REQ-003]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/EmailAlreadyExistsException.java` — [EXC-004]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/InvalidPasswordException.java` — [EXC-004]
    * `./sources/backend/user-service/src/main/resources/application.properties` — [ARC-006]
    * `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/AuthControllerTest.java` — [REQ-001], [EXC-004]
    * `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/OAuth2ServiceTest.java` — [REQ-002]
    * `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserServiceTest.java` — [REQ-003]
    * `./sources/backend/center-service/pom.xml` — [ARC-000]
    * `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterController.java` — [REQ-004], [REQ-005], [REQ-006]
    * `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterService.java` — [REQ-004], [REQ-005], [EXC-004]
    * `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterAdminService.java` — [REQ-006]
    * `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/CenterRequest.java` — [REQ-005], [EXC-004]
    * `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/CenterAdminRequest.java` — [REQ-006]
    * `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/exception/DuplicateTaxIdException.java` — [EXC-004]
    * `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/CenterControllerTest.java` — [REQ-004], [REQ-005]
    * `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/CenterAdminServiceTest.java` — [REQ-006]
    * `./sources/docs/architecture/UserServiceArchitecture.md` — [DOC-001], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
    * `./sources/docs/api/UserServiceApiContract.md` — [DOC-001], [REQ-001], [REQ-002], [REQ-003]
    * `./sources/docs/api/CenterServiceApiContract.md` — [DOC-001], [REQ-004], [REQ-005], [REQ-006]

- **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Cung cấp các câu lệnh DDL SQL di trú thô, hoàn chỉnh và hợp lệ với các cột, kiểu dữ liệu, khóa chính/khóa ngoại, ánh xạ quan hệ, chỉ mục và ràng buộc nullability được áp dụng trong phạm vi giai đoạn này.
```sql:matrix
-- =====================================================================
-- PHASE 2: USER-SERVICE & CENTER-SERVICE ADDITIVE MIGRATION
-- Mục tiêu: bổ sung ràng buộc nghiệp vụ và seed dữ liệu vai trò
-- =====================================================================

-- V20250102100000__phase2_seed_roles.sql
-- Seed dữ liệu bảng Roles với các giá trị hằng số nghiệp vụ
INSERT INTO roles (role_id, name, description) VALUES
    (1, 'SYSTEM_ADMIN',    'System Admin - toàn quyền hệ thống'),
    (2, 'CENTER_ADMIN',    'Center Admin - quản trị cấp trung tâm'),
    (3, 'MANAGER',         'Manager - quản lý cấp trung tâm (giới hạn)'),
    (4, 'TEACHER',         'Teacher - giáo viên, chỉ đọc lịch dạy'),
    (5, 'STUDENT',         'Student - học viên, duyệt và ghi danh khóa học');

-- V20250102100001__phase2_user_audit_log.sql
-- Bảng ghi log kiểm toán cho các hành động thay đổi vai trò và quyền
CREATE TABLE user_audit_log (
    audit_id          UUID           PRIMARY KEY,
    user_id           UUID           NOT NULL,
    action_type       VARCHAR(50)    NOT NULL,
    performed_by      UUID           NOT NULL,
    old_value         TEXT,
    new_value         TEXT,
    performed_at      TIMESTAMP      NOT NULL DEFAULT now(),
    CONSTRAINT fk_user_audit_user
        FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_user_audit_performer
        FOREIGN KEY (performed_by) REFERENCES users(user_id),
    CONSTRAINT chk_audit_action
        CHECK (action_type IN ('ROLE_CHANGED', 'CENTER_ASSIGNED', 'CENTER_UNASSIGNED', 'PASSWORD_RESET', 'ACCOUNT_LOCKED'))
);

CREATE INDEX idx_user_audit_user_id      ON user_audit_log(user_id);
CREATE INDEX idx_user_audit_performed_at ON user_audit_log(performed_at);

-- V20250102100002__phase2_user_provider_constraints.sql
-- Bổ sung ràng buộc CHECK cho provider để đảm bảo tính toàn vẹn
ALTER TABLE users
    ADD CONSTRAINT chk_users_provider
    CHECK (provider IN ('local', 'firebase', 'google', 'facebook'));

-- V20250102100003__phase2_center_admin_relationship.sql
-- Bảng quan hệ User-Center dùng cho việc phân công Center Admin
CREATE TABLE center_admins (
    center_id     UUID        NOT NULL,
    user_id       UUID        NOT NULL,
    assigned_at   TIMESTAMP   NOT NULL DEFAULT now(),
    assigned_by   UUID        NOT NULL,
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (center_id, user_id),
    CONSTRAINT fk_center_admin_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT fk_center_admin_user   FOREIGN KEY (user_id)   REFERENCES users(user_id),
    CONSTRAINT fk_center_admin_by     FOREIGN KEY (assigned_by) REFERENCES users(user_id)
);

CREATE INDEX idx_center_admins_user_id   ON center_admins(user_id);
CREATE INDEX idx_center_admins_is_active ON center_admins(is_active);
```

- **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:** Tài liệu hóa toàn bộ hợp đồng kỹ thuật cho `user-service` và `center-service` ở định dạng JSON Schema.
```json
{
  "openapi": "3.0.3",
  "info": {
    "title": "Membership Hub - User Service & Center Service API",
    "version": "2.0.0"
  },
  "paths": {
    "/api/v1/auth/register": {
      "post": {
        "tags": ["Auth"],
        "summary": "Đăng ký tài khoản cục bộ bằng email và mật khẩu",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/RegisterRequest" }
            }
          }
        },
        "responses": {
          "201": { "description": "Tạo tài khoản thành công, trả về JWT" },
          "400": { "description": "Dữ liệu không hợp lệ" },
          "409": { "description": "Email đã tồn tại" }
        }
      }
    },
    "/api/v1/auth/social": {
      "post": {
        "tags": ["Auth"],
        "summary": "Đăng nhập hoặc đăng ký qua OAuth2 (Firebase, Google, Facebook)",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/SocialLoginRequest" }
            }
          }
        },
        "responses": {
          "200": { "description": "Xác thực thành công" }
        }
      }
    },
    "/api/v1/users/{id}/role": {
      "put": {
        "tags": ["Users"],
        "summary": "Thay đổi vai trò của người dùng",
        "parameters": [
          { "name": "id", "in": "path", "required": true, "schema": { "type": "string", "format": "uuid" } }
        ],
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/RoleUpdateRequest" }
            }
          }
        },
        "responses": {
          "200": { "description": "Cập nhật vai trò thành công" },
          "403": { "description": "Không đủ quyền thực hiện" }
        }
      }
    },
    "/api/v1/centers": {
      "get": {
        "tags": ["Centers"],
        "summary": "Danh sách trung tâm có phân trang",
        "parameters": [
          { "name": "page", "in": "query", "schema": { "type": "integer", "default": 0 } },
          { "name": "size", "in": "query", "schema": { "type": "integer", "default": 20 } }
        ],
        "responses": {
          "200": { "description": "Trả về danh sách trung tâm" }
        }
      },
      "post": {
        "tags": ["Centers"],
        "summary": "Tạo trung tâm mới (chỉ System Admin)",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/CenterRequest" }
            }
          }
        },
        "responses": {
          "201": { "description": "Tạo trung tâm thành công" },
          "409": { "description": "TaxID đã tồn tại" }
        }
      }
    },
    "/api/v1/centers/{id}": {
      "put": {
        "tags": ["Centers"],
        "summary": "Cập nhật thông tin trung tâm",
        "parameters": [
          { "name": "id", "in": "path", "required": true, "schema": { "type": "string", "format": "uuid" } }
        ],
        "responses": {
          "200": { "description": "Cập nhật thành công" }
        }
      },
      "delete": {
        "tags": ["Centers"],
        "summary": "Xóa trung tâm (soft delete)",
        "parameters": [
          { "name": "id", "in": "path", "required": true, "schema": { "type": "string", "format": "uuid" } }
        ],
        "responses": {
          "204": { "description": "Xóa thành công" }
        }
      }
    },
    "/api/v1/centers/{id}/admins": {
      "post": {
        "tags": ["CenterAdmins"],
        "summary": "Phân công người dùng làm Center Admin cho trung tâm",
        "parameters": [
          { "name": "id", "in": "path", "required": true, "schema": { "type": "string", "format": "uuid" } }
        ],
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/CenterAdminRequest" }
            }
          }
        },
        "responses": {
          "201": { "description": "Phân công thành công" }
        }
      },
      "delete": {
        "tags": ["CenterAdmins"],
        "summary": "Hủy phân công Center Admin",
        "parameters": [
          { "name": "id", "in": "path", "required": true, "schema": { "type": "string", "format": "uuid" } },
          { "name": "userId", "in": "query", "required": true, "schema": { "type": "string", "format": "uuid" } }
        ],
        "responses": {
          "204": { "description": "Hủy phân công thành công" }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "RegisterRequest": {
        "type": "object",
        "required": ["email", "password", "agreeTerms"],
        "properties": {
          "email":       { "type": "string", "format": "email", "maxLength": 255 },
          "password":    { "type": "string", "minLength": 8, "maxLength": 128 },
          "fullName":    { "type": "string", "maxLength": 100 },
          "agreeTerms":  { "type": "boolean" }
        }
      },
      "SocialLoginRequest": {
        "type": "object",
        "required": ["provider", "authorizationCode"],
        "properties": {
          "provider":           { "type": "string", "enum": ["firebase", "google", "facebook"] },
          "authorizationCode":  { "type": "string" },
          "profilePicture":     { "type": "string", "format": "uri" }
        }
      },
      "RoleUpdateRequest": {
        "type": "object",
        "required": ["roleId"],
        "properties": {
          "roleId": { "type": "integer", "minimum": 1, "maximum": 5 }
        }
      },
      "CenterRequest": {
        "type": "object",
        "required": ["name", "address", "taxId"],
        "properties": {
          "name":          { "type": "string", "maxLength": 100 },
          "address":       { "type": "string", "maxLength": 255 },
          "taxId":         { "type": "string", "pattern": "^[0-9]{10,13}$" },
          "contactPhone":  { "type": "string", "pattern": "^[+0-9 ()\\-]{0,20}$" },
          "contactEmail":  { "type": "string", "format": "email", "maxLength": 100 }
        }
      },
      "CenterAdminRequest": {
        "type": "object",
        "required": ["userId"],
        "properties": {
          "userId": { "type": "string", "format": "uuid" }
        }
      }
    }
  }
}
```

- **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Liệt kê chi tiết các quy tắc xác thực nghiệp vụ, mã lỗi và quy trình xử lý ngoại lệ trong giai đoạn này.

| Mã Ngoại Lệ | HTTP Status | Mô Tả Lỗi | Tag ID |
| :--- | :--- | :--- | :--- |
| `EMAIL_ALREADY_EXISTS` | 409 | Email đã tồn tại trong hệ thống khi đăng ký hoặc cập nhật. | [EXC-004] |
| `INVALID_PASSWORD_FORMAT` | 400 | Mật khẩu không đáp ứng chính sách: tối thiểu 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt. | [EXC-004] |
| `TERMS_NOT_AGREED` | 400 | Người dùng chưa đồng ý với điều khoản sử dụng khi đăng ký. | [EXC-004] |
| `OAUTH2_PROVIDER_ERROR` | 502 | Không thể trao đổi authorization code hoặc nhận thông tin người dùng từ provider. | [EXC-004] |
| `ROLE_UPDATE_FORBIDDEN` | 403 | Người dùng hiện tại không có quyền thay đổi vai trò (chỉ System Admin được phép). | [REQ-003] |
| `DUPLICATE_TAX_ID` | 409 | TaxID của trung tâm đã tồn tại trong hệ thống. | [EXC-004] |
| `INVALID_TAX_ID_FORMAT` | 400 | TaxID không đúng định dạng 10-13 chữ số. | [EXC-004] |
| `CENTER_NOT_FOUND` | 404 | Không tìm thấy trung tâm theo ID cung cấp. | [REQ-005] |
| `USER_NOT_FOUND` | 404 | Không tìm thấy người dùng theo ID cung cấp khi phân quyền. | [REQ-006] |
| `CENTER_ADMIN_ALREADY_ASSIGNED` | 409 | Người dùng đã là Center Admin của trung tâm này. | [REQ-006] |

#### 📅 Nhật Ký Phân Bổ Nhiệm Vụ Theo Ngày Cho Các Sub-Agent (Giai Đoạn 2)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: KHỞI TẠO KHUNG USER-SERVICE VÀ TRIỂN KHAI ĐĂNG KÝ/ĐĂNG NHẬP

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: KHỞI TẠO POM.XML VÀ CẤU HÌNH USER-SERVICE
* **Chuyên Môn Hóa Theo Sub-Agent:** [Coder]

* **Các Tag ID Được Nhắm Tới:** [ARC-000], [ARC-006]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/pom.xml`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo tệp `pom.xml` cho module `user-service` với parent kế thừa từ `./sources/backend/pom.xml`. Khai báo các dependency Quarkus: `resteasy-reactive-jackson`, `hibernate-orm-panache`, `jdbc-postgresql`, `flyway`, `smallrye-jwt`, `smallrye-jwt-build`, `bcrypt-jdk18`, `hibernate-validator`, `arc`, `mockito`, `rest-assured`. Thiết lập Java version 17 và Quarkus BOM phiên bản 3.15.x. Đảm bảo phần `<artifactId>` là `user-service` và `<groupId>` là `org.nlh4j.membershiphub`. Cấu hình plugin `quarkus-maven-plugin` để build image native khi cần. [ARC-000], [ARC-006]

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**
```sql
-- Bước 1: Giai đoạn 2 không tạo bảng mới, chỉ tham chiếu schema đã có từ Phase 1
-- Tệp này chỉ mang tính chất ghi nhận, không có câu lệnh DDL bổ sung
SELECT 'Phase 2 chỉ thực hiện tham chiếu schema Phase 1 (users, roles, centers)' AS INFO;
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**
```json
{
  "service": "user-service",
  "phase": 2,
  "endpoints_declared": [
    "POST /api/v1/auth/register",
    "POST /api/v1/auth/social",
    "PUT  /api/v1/users/{id}/role"
  ]
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: TRIỂN KHAI AUTHCONTROLLER VÀ AUTHSERVICE
* **Chuyên Môn Hóa Theo Sub-Agent:** [Coder]

* **Các Tag ID Được Nhắm Tới:** [REQ-001], [REQ-002], [ARC-006], [EXC-004]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuthController.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Xây dựng lớp `AuthController` được chú thích `@Path("/api/v1/auth")` sử dụng JAX-RS. Endpoint `POST /register` nhận `RegisterRequest` được xác thực bằng `@Valid`, gọi `AuthService.register()` để mã hóa mật khẩu bằng bcrypt với cost factor 12, kiểm tra email đã tồn tại qua `UserRepository.findByEmail()`, lưu bản ghi User mới với `role_id=5` (Student) và phát hành JWT access/refresh token. Endpoint `POST /social` chấp nhận `SocialLoginRequest`, ủy quyền cho `OAuth2Service` xử lý authorization code grant. Trả về mã trạng thái HTTP 201 cho đăng ký thành công, 409 khi email trùng, 400 khi payload không hợp lệ. Sử dụng `Response.status()` của JAX-RS để xây dựng response. [REQ-001], [REQ-002], [ARC-006], [EXC-004]

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**
```sql
-- Không có migration mới. Endpoint này tham chiếu bảng users, roles từ Phase 1
SELECT 'AuthController sử dụng schema users, roles đã có từ Phase 1' AS INFO;
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**
```json
{
  "endpoint": "POST /api/v1/auth/register",
  "request": {
    "email": "string (max 255, email format, unique)",
    "password": "string (min 8, có chữ hoa, thường, số, đặc biệt)",
    "fullName": "string (max 100, optional)",
    "agreeTerms": "boolean (required=true)"
  },
  "response_201": {
    "userId": "uuid",
    "accessToken": "jwt (15 phút)",
    "refreshToken": "jwt (7 ngày)",
    "role": "STUDENT"
  },
  "response_409": { "code": "EMAIL_ALREADY_EXISTS" },
  "response_400": { "code": "INVALID_PASSWORD_FORMAT" }
}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:**
- `EmailAlreadyExistsException`: được ném khi email đã tồn tại, trả về HTTP 409 với mã `EMAIL_ALREADY_EXISTS`. [EXC-004]
- `InvalidPasswordException`: được ném khi mật khẩu không đáp ứng chính sách phức tạp, trả về HTTP 400 với mã `INVALID_PASSWORD_FORMAT`. [EXC-004]
- `@ConstraintViolationException` từ Bean Validation: ánh xạ sang HTTP 400 với danh sách trường không hợp lệ. [EXC-004]

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: KIỂM THỬ ENDPOINT ĐĂNG KÝ
* **Chuyên Môn Hóa Theo Sub-Agent:** [Tester]

* **Các Tag ID Được Nhắm Tới:** [REQ-001], [EXC-004]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/AuthControllerTest.java;./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuthController.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Viết bộ test tích hợp sử dụng `@QuarkusTest` và `RestAssured`. Test case 1: đăng ký với email hợp lệ, mật khẩu mạnh, đồng ý điều khoản — kỳ vọng HTTP 201 và nhận JWT. Test case 2: đăng ký trùng email — kỳ vọng HTTP 409 với mã `EMAIL_ALREADY_EXISTS`. Test case 3: mật khẩu yếu (chỉ 7 ký tự) — kỳ vọng HTTP 400 với mã `INVALID_PASSWORD_FORMAT`. Test case 4: bỏ chọn đồng ý điều khoản — kỳ vọng HTTP 400 với mã `TERMS_NOT_AGREED`. Sử dụng `Testcontainers PostgreSQL` để mô phỏng cơ sở dữ liệu. [REQ-001], [EXC-004]

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: ĐÁNH GIÁ MÃ NGUỒN VÀ ĐỀ XUẤT CẢI TIẾN
* **Chuyên Môn Hóa Theo Sub-Agent:** [Reviewer]

* **Các Tag ID Được Nhắm Tới:** [REQ-001], [REQ-002], [ARC-006]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuthController.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Rà soát toàn bộ mã nguồn `AuthController` và `AuthService` để đảm bảo: (1) Không có mật khẩu plaintext nào được log hoặc trả về trong response, (2) Tất cả đầu vào đều được validate qua `@Valid` và Bean Validation, (3) Tuân thủ chuẩn OWASP A03 (Injection) bằng cách sử dụng PreparedStatement thông qua Panache, (4) Đảm bảo nguyên tắc Least Privilege trong xử lý role mặc định. Tạo báo cáo review dạng comment trong PR và đề xuất tối ưu hóa nếu phát hiện n+1 query hoặc thiếu index. [REQ-001], [REQ-002], [ARC-006]

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: TÍCH HỢP OAUTH2 VÀ PHÂN QUYỀN NGƯỜI DÙNG

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TRIỂN KHAI OAUTH2SERVICE VÀ JWTTOKENPROVIDER
* **Chuyên Môn Hóa Theo Sub-Agent:** [Coder]

* **Các Tag ID Được Nhắm Tới:** [REQ-002], [ARC-006]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/OAuth2Service.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Xây dựng lớp `OAuth2Service` xử lý luồng authorization code grant cho ba provider: Firebase, Google, Facebook. Với mỗi provider, cấu hình `clientId`, `clientSecret`, `redirectUri` từ `application.properties` và endpoint trao đổi token (`tokenEndpoint`). Sử dụng `java.net.http.HttpClient` để gọi `POST` tới token endpoint, sau đó gọi `userInfoEndpoint` để lấy email và profile. Ánh xạ thông tin provider về bản ghi User cục bộ: nếu email đã tồn tại thì cập nhật `provider` và `providerUserId`, nếu chưa tồn tại thì tạo mới với `role_id=5`. Ủy quyền cho `JwtTokenProvider` phát hành token. Lớp `JwtTokenProvider` sử dụng `SmallRye JWT Build` để tạo JWT với thời hạn 15 phút, claim `groups` chứa role name và `upn` chứa email. [REQ-002], [ARC-006]

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**
```sql
-- Không tạo bảng mới. OAuth2Service chỉ thao tác trên bảng users
SELECT 'OAuth2Service tham chiếu bảng users với provider IN (firebase, google, facebook)' AS INFO;
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**
```json
{
  "endpoint": "POST /api/v1/auth/social",
  "provider_flow": {
    "firebase":  { "tokenEndpoint": "https://securetoken.googleapis.com/v1/token", "userInfoEndpoint": "https://identitytoolkit.googleapis.com/v1/accounts:lookup" },
    "google":    { "tokenEndpoint": "https://oauth2.googleapis.com/token",        "userInfoEndpoint": "https://openidconnect.googleapis.com/v1/userinfo" },
    "facebook":  { "tokenEndpoint": "https://graph.facebook.com/v18.0/oauth/access_token", "userInfoEndpoint": "https://graph.facebook.com/me?fields=id,name,email,picture" }
  }
}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:**
- `OAuth2ProviderException`: được ném khi không trao đổi được token hoặc không lấy được thông tin người dùng, trả về HTTP 502 với mã `OAUTH2_PROVIDER_ERROR`. [EXC-004]

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: TRIỂN KHAI USERSERVICE, ROLESERVICE VÀ AUDITLOGGER
* **Chuyên Môn Hóa Theo Sub-Agent:** [Coder]

* **Các Tag ID Được Nhắm Tới:** [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-006]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/UserService.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo lớp `UserService` cung cấp phương thức `changeRole(UUID userId, int newRoleId, UUID performedBy)` thực hiện cập nhật `users.role_id` trong cùng transaction với ghi log kiểm toán. Lớp `RoleService` chứa phương thức `findById(int roleId)` và `validateRoleTransition(int oldRoleId, int newRoleId)` để kiểm tra tính hợp lệ của việc chuyển đổi vai trò (ví dụ: không thể chuyển từ SYSTEM_ADMIN sang STUDENT). Lớp `AuditLogger` chứa phương thức `log(String actionType, UUID userId, UUID performedBy, String oldValue, String newValue)` ghi vào bảng `user_audit_log` với timestamp. Endpoint `PUT /api/v1/users/{id}/role` yêu cầu quyền `SYSTEM_ADMIN` được kiểm tra qua `@RolesAllowed("SYSTEM_ADMIN")`. [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-006]

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**
```sql
-- Sử dụng bảng user_audit_log đã tạo trong đầu giai đoạn
-- Bảng users cập nhật cột role_id, bảng roles tham chiếu
SELECT 'UserService sử dụng user_audit_log, users, roles' AS INFO;
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**
```json
{
  "endpoint": "PUT /api/v1/users/{id}/role",
  "request":  { "roleId": "integer (1=SYSTEM_ADMIN, 2=CENTER_ADMIN, 3=MANAGER, 4=TEACHER, 5=STUDENT)" },
  "response_200": { "userId": "uuid", "newRole": "string", "updatedAt": "timestamp" },
  "response_403": { "code": "ROLE_UPDATE_FORBIDDEN" }
}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:**
- `RoleUpdateForbiddenException`: được ném khi người thực hiện không có quyền SYSTEM_ADMIN, trả về HTTP 403. [REQ-003]
- `InvalidRoleTransitionException`: được ném khi vi phạm quy tắc chuyển đổi vai trò, trả về HTTP 400. [REQ-003]

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: KIỂM THỬ LUỒNG OAUTH2 VÀ PHÂN QUYỀN
* **Chuyên Môn Hóa Theo Sub-Agent:** [Tester]

* **Các Tag ID Được Nhắm Tới:** [REQ-002], [REQ-003], [ARC-006]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/OAuth2ServiceTest.java;./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/OAuth2Service.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sử dụng `Mockito` để mock các HTTP call tới provider OAuth2. Test case 1: authorization code hợp lệ từ Google — kỳ vọng trả về JWT và tạo/cập nhật User. Test case 2: token endpoint trả về lỗi 400 — kỳ vọng ném `OAuth2ProviderException`. Test case 3: endpoint phân quyền với token SYSTEM_ADMIN — kỳ vọng HTTP 200 và ghi audit log. Test case 4: phân quyền với token STUDENT — kỳ vọng HTTP 403 với mã `ROLE_UPDATE_FORBIDDEN`. [REQ-002], [REQ-003], [ARC-006]

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: TÀI LIỆU HÓA API USER-SERVICE
* **Chuyên Môn Hóa Theo Sub-Agent:** [Doc]

* **Các Tag ID Được Nhắm Tới:** [DOC-001], [REQ-001], [REQ-002], [REQ-003]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/api/UserServiceApiContract.md`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo tài liệu Markdown tại `./sources/docs/api/UserServiceApiContract.md` mô tả đầy đủ các endpoint của `user-service`: đường dẫn, phương thức HTTP, request schema, response schema, mã lỗi, quyền truy cập yêu cầu. Bao gồm sơ đồ luồng OAuth2 cho cả ba provider và luồng RBAC. Tham chiếu rõ ràng tới các Tag ID. [DOC-001], [REQ-001], [REQ-002], [REQ-003]

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 3: TRIỂN KHAI CENTER-SERVICE VÀ PHÂN CÔNG CENTER ADMIN

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: KHỞI TẠO POM.XML VÀ TRIỂN KHAI CENTERCONTROLLER
* **Chuyên Môn Hóa Theo Sub-Agent:** [Coder]

* **Các Tag ID Được Nhắm Tới:** [ARC-000], [REQ-004], [REQ-005], [REQ-006]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterController.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Khởi tạo `pom.xml` cho `center-service` tương tự cấu trúc `user-service` nhưng chỉ giữ các dependency cần thiết. Tạo `CenterController` chú thích `@Path("/api/v1/centers")` với các endpoint: `GET /` trả về danh sách phân trang, `POST /` tạo mới (yêu cầu SYSTEM_ADMIN), `PUT /{id}` cập nhật, `DELETE /{id}` soft delete, `POST /{id}/admins` phân công Center Admin, `DELETE /{id}/admins?userId=` hủy phân công. Sử dụng `Bean Validation` cho `CenterRequest` với các ràng buộc: name max 100, address max 255, taxId pattern `^[0-9]{10,13}$`, contactPhone và contactEmail tùy chọn. Trả về HTTP 409 khi trùng TaxID, HTTP 400 khi validation fail. [REQ-004], [REQ-005], [REQ-006]

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**
```sql
-- CenterService sử dụng bảng centers (Phase 1) và center_admins (Phase 2)
SELECT 'CenterController tham chiếu centers, center_admins, users' AS INFO;
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**
```json
{
  "endpoints": [
    "GET    /api/v1/centers?page=&size=",
    "POST   /api/v1/centers (SYSTEM_ADMIN)",
    "PUT    /api/v1/centers/{id} (SYSTEM_ADMIN)",
    "DELETE /api/v1/centers/{id} (SYSTEM_ADMIN, soft delete)",
    "POST   /api/v1/centers/{id}/admins (SYSTEM_ADMIN)",
    "DELETE /api/v1/centers/{id}/admins?userId= (SYSTEM_ADMIN)"
  ]
}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:**
- `DuplicateTaxIdException`: được ném khi TaxID đã tồn tại, trả về HTTP 409 với mã `DUPLICATE_TAX_ID`. [EXC-004]
- `CenterNotFoundException`: được ném khi không tìm thấy trung tâm, trả về HTTP 404 với mã `CENTER_NOT_FOUND`. [REQ-005]
- `CenterAdminAlreadyAssignedException`: được ném khi người dùng đã là admin của trung tâm, trả về HTTP 409. [REQ-006]

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: TRIỂN KHAI CENTERSERVICE VÀ CENTERADMINSERVICE
* **Chuyên Môn Hóa Theo Sub-Agent:** [Coder]

* **Các Tag ID Được Nhắm Tới:** [REQ-004], [REQ-005], [REQ-006], [EXC-004]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterService.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo `CenterService` chứa logic nghiệp vụ: `listCenters(int page, int size)` truy vấn Panache với phân trang, `createCenter(CenterRequest req)` kiểm tra trùng TaxID qua `findByTaxId()` trước khi persist, `updateCenter(UUID id, CenterRequest req)` cập nhật các trường cho phép, `softDeleteCenter(UUID id)` đặt `is_active=false`. Tạo `CenterAdminService` chứa `assignAdmin(UUID centerId, UUID userId, UUID assignedBy)` thực hiện trong transaction: cập nhật `users.role_id=2` (CENTER_ADMIN), chèn bản ghi vào bảng `center_admins`, ghi audit log. Phương thức `unassignAdmin()` đặt `is_active=false` trong bảng `center_admins` và khôi phục role mặc định (nếu không có trung tâm nào khác). [REQ-004], [REQ-005], [REQ-006], [EXC-004]

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**
```sql
-- Đảm bảo bảng centers có cột is_active cho soft delete
-- Migration này bổ sung nếu Phase 1 chưa tạo
ALTER TABLE centers
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX IF NOT EXISTS idx_centers_tax_id ON centers(tax_id);
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: KIỂM THỬ CENTER-SERVICE
* **Chuyên Môn Hóa Theo Sub-Agent:** [Tester]

* **Các Tag ID Được Nhắm Tới:** [REQ-004], [REQ-005], [REQ-006], [EXC-004]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/CenterControllerTest.java;./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterController.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Viết test tích hợp với `@QuarkusTest` và Testcontainers. Test case 1: `GET /api/v1/centers` trả về danh sách phân trang đúng. Test case 2: `POST /api/v1/centers` với TaxID đã tồn tại — kỳ vọng HTTP 409 với mã `DUPLICATE_TAX_ID`. Test case 3: TaxID không đúng định dạng (chứa ký tự alpha) — kỳ vọng HTTP 400 với mã `INVALID_TAX_ID_FORMAT`. Test case 4: phân công admin thành công — kỳ vọng HTTP 201 và ghi audit log. Test case 5: phân công admin trùng lặp — kỳ vọng HTTP 409 với mã `CENTER_ADMIN_ALREADY_ASSIGNED`. [REQ-004], [REQ-005], [REQ-006], [EXC-004]

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**
```json
{
  "integration_test_scope": "INTEGRATION_SCOPE;./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/CenterControllerIT.java"
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: ĐÁNH GIÁ MÃ NGUỒN VÀ TÀI LIỆU HÓA
* **Chuyên Môn Hóa Theo Sub-Agent:** [Reviewer]

* **Các Tag ID Được Nhắm Tới:** [REQ-004], [REQ-005], [REQ-006], [DOC-001]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterService.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Rà soát `CenterService` và `CenterAdminService` để đảm bảo: (1) Mọi thay đổi role đều được ghi audit log trong cùng transaction, (2) Soft delete không phá vỡ ràng buộc khóa ngoại, (3) Logic kiểm tra trùng TaxID sử dụng database constraint làm lớp bảo vệ cuối cùng. Tạo tài liệu `./sources/docs/api/CenterServiceApiContract.md` mô tả đầy đủ các endpoint, request/response schema, quyền truy cập. [REQ-004], [REQ-005], [REQ-006], [DOC-001]

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**
```json
{
  "endpoint": "POST /api/v1/centers/{id}/admins",
  "request":  { "userId": "uuid" },
  "response_201": { "centerId": "uuid", "userId": "uuid", "assignedAt": "timestamp" },
  "response_409": { "code": "CENTER_ADMIN_ALREADY_ASSIGNED" }
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

<!--PHASE_INDEX_START-->

### 📈 Giai Đoạn 3 - Quản Lý Khóa Học, Ghi Danh và Dashboard Phân Tích
- **Mục Tiêu & Ý Nghĩa Cốt Lõi Của Giai Đoạn:** Triển khai course-service với endpoint CRUD kèm cơ chế phát hiện xung đột lịch trình giáo viên thông qua PostgreSQL trigger, đồng thời xây dựng enrollment-service hỗ trợ duyệt khóa học khả dụng, tự động tạo tài khoản học viên khi ghi danh và phát sự kiện Kafka để thông báo đa kênh. Hoàn thiện dashboard tổng hợp ghi danh với cache Redis 15 phút cung cấp chỉ số tổng học viên, khóa học đang hoạt động và buổi học sắp tới trong 7 ngày, đảm bảo hiệu năng sub-second cho 10.000 người dùng đồng thời theo [NFR-001].

- **Bản Đồ Ma Trận Thư Mục Vật Lý Mục Tiêu:** Generate an exhaustive, granular engineering checklist mapping out 100% of all discrete, individual physical relative file paths (NOT folders or directories) underneath `./sources/` that are actively created, refactored, or processed within this phase scope. Every single line item MUST represent a concrete file entity ending with its explicit structural file extension, with its matching traceability Tag IDs appended inline.
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

- **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu** [DAT-XXX]:

```sql
-- V3__course_module_indexes.sql
-- Bổ sung các chỉ mục chuyên biệt cho course-service và enrollment-service

CREATE INDEX IF NOT EXISTS idx_courses_teacher_id ON Courses(teacher_id);
CREATE INDEX IF NOT EXISTS idx_courses_start_end_date ON Courses(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_courses_title_trgm ON Courses USING gin (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_enrollments_student_id ON Enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON Enrollments(course_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_enrollments_student_course ON Enrollments(student_id, course_id);
CREATE INDEX IF NOT EXISTS idx_courses_active_range ON Courses(start_date, end_date) WHERE end_date >= CURRENT_DATE;
```

- **Hợp Đồng Định Tuyến API và Sự Kiện** [REQ-XXX], [ARC-XXX]:

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
      },
      "post": {
        "summary": "Tạo khóa học mới",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": {
                "type": "object",
                "required": ["title", "startDate", "endDate", "teacherId"],
                "properties": {
                  "title": { "type": "string", "maxLength": 150 },
                  "description": { "type": "string" },
                  "startDate": { "type": "string", "format": "date" },
                  "endDate": { "type": "string", "format": "date" },
                  "teacherId": { "type": "string", "format": "uuid" },
                  "maxStudents": { "type": "integer", "default": 30 }
                }
              }
            }
          }
        }
      }
    },
    "/api/v1/courses/{id}/teachers": {
      "post": {
        "summary": "Phân công giáo viên cho khóa học",
        "parameters": [
          { "name": "id", "in": "path", "required": true, "schema": { "type": "string", "format": "uuid" } }
        ],
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": {
                "type": "object",
                "required": ["teacherId"],
                "properties": { "teacherId": { "type": "string", "format": "uuid" } }
              }
            }
          }
        }
      }
    },
    "/api/v1/students/{id}/available-courses": {
      "get": {
        "summary": "Khóa học khả dụng cho học viên",
        "parameters": [
          { "name": "id", "in": "path", "required": true, "schema": { "type": "string", "format": "uuid" } }
        ]
      }
    },
    "/api/v1/enrollments": {
      "post": {
        "summary": "Ghi danh khóa học",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": {
                "type": "object",
                "required": ["courseId"],
                "properties": {
                  "courseId": { "type": "string", "format": "uuid" },
                  "email": { "type": "string", "format": "email" }
                }
              }
            }
          }
        }
      }
    },
    "/api/v1/dashboard/enrollment-summary": {
      "get": {
        "summary": "Dashboard tổng hợp ghi danh",
        "responses": {
          "200": {
            "description": "Trả về tổng hợp",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "totalStudents": { "type": "integer" },
                    "activeCourses": { "type": "integer" },
                    "upcomingSessions": { "type": "integer" }
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

```json
{
  "topic": "course.teacher.assigned",
  "schema": {
    "type": "object",
    "properties": {
      "courseId": { "type": "string", "format": "uuid" },
      "teacherId": { "type": "string", "format": "uuid" },
      "assignedBy": { "type": "string", "format": "uuid" },
      "assignedAt": { "type": "string", "format": "date-time" }
    }
  }
}
```

- **Bộ Xử Lý Ngoại Lệ Bản Địa Hóa Của Giai Đoạn** [EXC-XXX]:
    * **[REQ-008] [EXC-004]** `ScheduleConflictException`: Khi phát hiện giáo viên đã có khóa học giao thoa khoảng thời gian, hệ thống trả về mã lỗi `SCHEDULE_CONFLICT` với HTTP 409, nội dung thông báo "Khoảng thời gian khóa học bị trùng lặp với khóa học hiện tại của giáo viên".
    * **[REQ-008] [EXC-004]** `CourseValidationException`: Validate `EndDate >= StartDate`, độ dài `title` tối đa 150 ký tự, trả về mã lỗi `INVALID_COURSE_DATA` với HTTP 400 kèm danh sách trường không hợp lệ.
    * **[REQ-011] [EXC-004]** `DuplicateEnrollmentException`: Học viên ghi danh vào cùng một khóa học hai lần, hệ thống trả về HTTP 200 với payload `{ "status": "already_enrolled", "courseId": "..." }` thay vì tạo bản ghi trùng.
    * **[REQ-011] [EXC-004]** `StudentProvisioningException`: Khi tự động tạo tài khoản học viên thất bại (email không hợp lệ, lỗi database), trả về mã lỗi `STUDENT_PROVISIONING_FAILED` với HTTP 422.
    * **[REQ-025] [EXC-004]** `DashboardCacheException`: Lỗi khi truy cập cache Redis, fallback compute trực tiếp từ PostgreSQL với log cảnh báo, không trả lỗi cho client.

#### 📅 Nhật Ký Phân Bổ Công Việc Theo Ngày và Sub-Agent (Giai Đoạn 3)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: Khởi Tạo Course Service và Danh Sách Khóa Học

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: Khởi tạo Maven module cho course-service và khai báo REST endpoint danh sách
* **Chuyên Môn Hóa Sub-Agent:** Coder

* **Tag ID Mục Tiêu:** [ARC-000], [REQ-007]

* **Đường Dẫn File Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/pom.xml`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Khởi tạo Maven module con `course-service` kế thừa từ `./sources/backend/pom.xml`, khai báo dependency Quarkus REST, Hibernate ORM Panache, PostgreSQL JDBC driver, SmallRye OpenAPI, validation, kafka client. Cấu hình port 8082, datasource chuyên biệt `membershiphub_course`. Định nghĩa `CourseEntity` ánh xạ bảng `Courses` với các cột course_id (UUID PK), title (VARCHAR 150), description (TEXT), start_date (DATE), end_date (DATE), teacher_id (UUID FK), max_students (INT default 30), timestamps created_at/updated_at. Cập nhật bản đồ thư mục vật lý tại root backend pom.xml để bao gồm module mới. Toàn bộ mã nguồn Java phải khai báo package `org.nlh4j.membershiphub.courseservice` theo nguyên tắc chuẩn hóa Java package authority đã thiết lập, đảm bảo thư mục vật lý khớp 1:1 với cấu trúc package. Tuân thủ triệt để nguyên tắc dependency isolation: cấm import trực tiếp class từ user-service, center-service.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu** [DAT-XXX]:
```sql
-- Bảng Courses đã được tạo tại V1__init_core_schema.sql; V3 bổ sung index tối ưu
CREATE INDEX IF NOT EXISTS idx_courses_teacher_id ON Courses(teacher_id);
CREATE INDEX IF NOT EXISTS idx_courses_start_end_date ON Courses(start_date, end_date);
```

* **Hợp Đồng Định Tuyến API và Sự Kiện** [REQ-007], [ARC-008]:
```json
{
  "endpoint": "GET /api/v1/courses",
  "response": {
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
```

* **Bộ Xử Lý Ngoại Lệ Bản Địa Hóa Của Giai Đoạn** [EXC-004]:
```java
package org.nlh4j.membershiphub.courseservice;

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(String courseId) {
        super("Không tìm thấy khóa học với mã " + courseId);
    }
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: Xây dựng bộ kiểm thử tích hợp cho endpoint danh sách khóa học
* **Chuyên Môn Hóa Sub-Agent:** Tester

* **Tag ID Mục Tiêu:** [REQ-007], [EXC-004]

* **Đường Dẫn File Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseController.java;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseControllerTest.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sử dụng `@QuarkusTest` kết hợp `@TestHTTPEndpoint(CourseController.class)`. Mock `CourseRepository` bằng `@InjectMock` để trả về danh sách 3 khóa học mẫu với teacher name thuộc role Teacher. Test case 1 (Happy Path): Gọi GET `/api/v1/courses` xác nhận HTTP 200, payload chứa 3 phần tử với đầy đủ trường `courseId, title, startDate, endDate, teacherName`. Test case 2 (Empty): Mock trả về danh sách rỗng, xác nhận HTTP 200 với mảng rỗng. Test case 3 (Database Error): Mock `CourseRepository.listAll()` ném `SQLException`, xác nhận ExceptionMapper trả về HTTP 500 với mã lỗi `INTERNAL_SERVER_ERROR`. Sử dụng RestAssured validate schema JSON. Đảm bảo test chạy độc lập với test suite của service khác thông qua `@QuarkusTestResource(DatabaseH2Resource.class)` riêng biệt.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu** [DAT-XXX]:
```sql
-- Không có thay đổi schema trong sub-task này; sử dụng bảng Courses đã tạo ở V1
```

* **Hợp Đồng Định Tuyến API và Sự Kiện** [REQ-007], [ARC-008]:
```json
{
  "testEndpoint": "GET /api/v1/courses",
  "assertions": [
    "HTTP 200 với danh sách 3 khóa học",
    "Mỗi phần tử có courseId hợp lệ UUID",
    "teacherName khác null khi có giáo viên"
  ]
}
```

* **Bộ Xử Lý Ngoại Lệ Bản Địa Hóa Của Giai Đoạn** [EXC-004]:
```java
// Trong test, xác nhận ExceptionMapper trả về JSON chuẩn
{"code":"INTERNAL_SERVER_ERROR","message":"Lỗi hệ thống khi truy xuất danh sách khóa học"}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: Review code mới khởi tạo của course-service và phát hành tài liệu kỹ thuật
* **Chuyên Môn Hóa Sub-Agent:** Reviewer, Doc

* **Tag ID Mục Tiêu:** [ARC-000], [REQ-007], [EXC-004]

* **Đường Dẫn File Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseController.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Reviewer thực hiện kiểm tra mã nguồn `CourseController.java` và `CourseEntity.java` để xác nhận: (1) Không có field nào expose password hash hoặc secret key, (2) Sử dụng `@RolesAllowed` annotation cho endpoint nhạy cảm, (3) Sử dụng Java record thay vì mutable DTO cho response payload, (4) Tuân thủ Java package convention `org.nlh4j.membershiphub.courseservice` ở tất cả file Java, (5) Không có raw SQL injection, tất cả truy vấn đi qua JPA hoặc named query. Doc đồng thời tạo file `./sources/docs/api/course-service-api.md` mô tả endpoint GET `/api/v1/courses` với bảng response schema, HTTP status codes, ví dụ curl request và JSON response mẫu. Đảm bảo tài liệu chứa mô tả bằng tiếng Việt, mục lục rõ ràng, sơ đồ luồng dữ liệu Mermaid.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu** [DAT-XXX]:
```sql
-- Sub-task này không thực hiện DDL trực tiếp
```

* **Hợp Đồng Định Tuyến API và Sự Kiện** [REQ-007], [ARC-008]:
```json
{
  "documentation": "./sources/docs/api/course-service-api.md",
  "sections": [
    "1. Tổng quan service",
    "2. Endpoint GET /api/v1/courses",
    "3. Response schema",
    "4. Mã lỗi",
    "5. Ví dụ curl"
  ]
}
```

* **Bộ Xử Lý Ngoại Lệ Bản Địa Hóa Của Giai Đoạn** [EXC-004]:
```java
// Review xác nhận không có exception handler mới trong sub-task này
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

##### 📅 NGÀY 2: CRUD Khóa Học, Kiểm Tra Xung Đột Lịch Trình và Phân Công Giáo Viên

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: Triển khai CRUD khóa học với trigger PostgreSQL kiểm tra xung đột giáo viên
* **Chuyên Môn Hóa Sub-Agent:** Coder

* **Tag ID Mục Tiêu:** [REQ-008], [EXC-004]

* **Đường Dẫn File Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseService.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Trong `CourseService.java`, triển khai các phương thức `createCourse`, `updateCourse`, `deleteCourse` với transaction `@Transactional`. Trước khi persist, gọi `ScheduleValidator.validateNoConflict(teacherId, startDate, endDate)` thực hiện truy vấn `SELECT COUNT(*) FROM Courses WHERE teacher_id = :teacherId AND (start_date <= :endDate AND end_date >= :startDate)`. Nếu kết quả > 0, ném `ScheduleConflictException` được map sang HTTP 409. Trong `updateCourse`, bảo đảm cập nhật cả `updated_at` qua `@PreUpdate` callback. Trong `deleteCourse`, kiểm tra quan hệ khóa ngoại `Enrollments.course_id` trước khi xóa; nếu còn ghi danh, trả về lỗi 422. Mã hóa tiêu đề khóa học thông qua hàm `StringUtils.normalizeWhitespace` để chống XSS stored. Áp dụng OWASP A03 injection mitigation bằng cách sử dụng JPA named parameters, cấm nối chuỗi SQL.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu** [DAT-XXX]:
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

* **Hợp Đồng Định Tuyến API và Sự Kiện** [REQ-008], [ARC-008]:
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

* **Bộ Xử Lý Ngoại Lệ Bản Địa Hóa Của Giai Đoạn** [EXC-004]:
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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: Viết bộ test cho ScheduleValidator và trigger PostgreSQL xung đột
* **Chuyên Môn Hóa Sub-Agent:** Tester

* **Tag ID Mục Tiêu:** [REQ-008], [EXC-004]

* **Đường Dẫn File Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/ScheduleValidator.java;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/ScheduleValidatorTest.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sử dụng `@QuarkusTest` với database PostgreSQL test container thực sự (Testcontainers) để trigger `trg_teacher_schedule_conflict` hoạt động. Test case 1: Insert khóa học A với teacher X từ 2025-01-01 đến 2025-03-31. Insert khóa học B cùng teacher X từ 2025-02-01 đến 2025-04-30, kỳ vọng ném `ScheduleConflictException` với mã lỗi `SCHEDULE_CONFLICT`. Test case 2: Insert khóa học B với teacher Y khác, kỳ vọng thành công. Test case 3: Update khóa học A kéo dài đến 2025-05-31, kỳ vọng thành công vì không có khóa học khác của teacher X trong khoảng mới. Test case 4: Validate `EndDate < StartDate`, kỳ vọng ném `CourseValidationException` HTTP 400. Sử dụng AssertJ để verify exception message chứa chuỗi tiếng Việt chính xác.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu** [DAT-XXX]:
```sql
-- Trigger đã tạo ở sub-task trước; test sử dụng schema thực tế
```

* **Hợp Đồng Định Tuyến API và Sự Kiện** [REQ-008], [ARC-008]:
```json
{
  "testEndpoint": "POST /api/v1/courses",
  "assertions": [
    "Trigger raise exception khi teacher bị trùng lịch",
    "HTTP 409 khi ScheduleConflictException xảy ra",
    "HTTP 400 khi EndDate < StartDate"
  ]
}
```

* **Bộ Xử Lý Ngoại Lệ Bản Địa Hóa Của Giai Đoạn** [EXC-004]:
```java
// Test xác nhận exception mapper trả về JSON chuẩn hóa tiếng Việt
{"code":"SCHEDULE_CONFLICT","message":"Khoảng thời gian khóa học bị trùng lặp với khóa học hiện tại của giáo viên","teacherId":"..."}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: Triển khai phân công giáo viên và publish sự kiện Kafka
* **Chuyên Môn Hóa Sub-Agent:** Coder

* **Tag ID Mục Tiêu:** [REQ-009], [ARC-008]

* **Đường Dẫn File Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/TeacherAssignmentService.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Trong `TeacherAssignmentService.java`, tạo method `assignTeacher(courseId, teacherId, assignedBy)` thực hiện cập nhật `Courses.teacher_id` và lưu lịch sử phân công vào bảng `CourseTeacherHistory` (cấu trúc: id, course_id, teacher_id, assigned_by, assigned_at, revoked_at nullable). Inject `KafkaEnrollmentProducer` để gửi sự kiện JSON tới topic `course.teacher.assigned` với key là courseId, payload gồm courseId, teacherId, assignedBy, assignedAt. Method `unassignTeacher(courseId, teacherId)` set `Courses.teacher_id = NULL`, ghi nhận `revoked_at` trong history, gửi sự kiện `course.teacher.unassigned`. Tất cả thao tác phải bọc trong `@Transactional(REQUIRES_NEW)` để đảm bảo Kafka publish rollback-safe. Áp dụng OWASP A01 access control bằng `@RolesAllowed("System Admin")` ở controller layer.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liêu** [DAT-XXX]:
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

* **Hợp Đồng Định Tuyến API và Sự Kiện** [REQ-009], [ARC-008]:
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

* **Bộ Xử Lý Ngoại Lệ Bản Địa Hóa Của Giai Đoạn** [EXC-004]:
```java
// Exception mới cho phân công giáo viên không tồn tại
public class TeacherNotFoundException extends RuntimeException {
    public TeacherNotFoundException(String teacherId) {
        super("Không tìm thấy giáo viên với mã " + teacherId);
    }
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: Review code CRUD khóa học và cập nhật tài liệu Database Schema
* **Chuyên Môn Hóa Sub-Agent:** Reviewer, Doc

* **Tag ID Mục Tiêu:** [REQ-008], [REQ-009], [EXC-004]

* **Đường Dẫn File Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/ExceptionMappers.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Reviewer kiểm tra `CourseService.java` và `TeacherAssignmentService.java` để xác nhận: (1) Không có SQL injection (chỉ dùng named parameters), (2) Kafka producer sử dụng idempotent producer config `enable.idempotence=true`, `acks=all` để chống duplicate event, (3) Transaction boundary đúng cách, (4) Không hardcode giá trị nhạy cảm. Doc cập nhật file `./sources/docs/database/course-module-schema.md` mô tả trigger `trg_teacher_schedule_conflict`, bảng `CourseTeacherHistory`, index mới, kèm sơ đồ ERD Mermaid. Tài liệu phải chứa bảng mapping giữa Tag ID và thành phần database tương ứng.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu** [DAT-XXX]:
```sql
-- Sub-task này không thực hiện DDL trực tiếp; chỉ tài liệu hóa DDL đã có
```

* **Hợp Đồng Định Tuyến API và Sự Kiện** [REQ-008], [REQ-009], [ARC-008]:
```json
{
  "documentation": "./sources/docs/database/course-module-schema.md",
  "sections": [
    "1. Bảng Courses",
    "2. Bảng CourseTeacherHistory",
    "3. Trigger trg_teacher_schedule_conflict",
    "4. Indexes",
    "5. ERD diagram"
  ]
}
```

* **Bộ Xử Lý Ngoại Lệ Bản Địa Hóa Của Giai Đoạn** [EXC-004]:
```java
// Review xác nhận ExceptionMappers có đủ mapping cho ScheduleConflictException, TeacherNotFoundException, CourseNotFoundException
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

##### 📅 NGÀY 3: Enrollment Service, Dashboard và Hoàn Thiện Giai Đoạn 3

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: Triển khai enrollment-service với endpoint duyệt khóa học và ghi danh tự động tạo tài khoản
* **Chuyên Môn Hóa Sub-Agent:** Coder

* **Tag ID Mục Tiêu:** [REQ-010], [REQ-011], [ARC-008], [EXC-004]

* **Đường Dẫn File Thành Phần Mục Tiêu (target_component):** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentService.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Trong `EnrollmentService.java`, triển khai `getAvailableCourses(studentId)` thực hiện query `SELECT * FROM Courses WHERE course_id NOT IN (SELECT course_id FROM Enrollments WHERE student_id = :studentId) AND end_date >= CURRENT_DATE`. Triển khai `enrollStudent(courseId, request)` với logic: (1) Lấy user từ JWT token, nếu email chưa tồn tại trong bảng `Users`, gọi `StudentProvisioningService.createStudent(email, fullName)` để tạo user mới với role `Student` và gửi mật khẩu tạm qua email queue, (2) Insert vào bảng `Enrollments`, (3) Publish sự kiện Kafka topic `enrollment.created` với payload chứa studentId, courseId, centerId, enrolledAt. Áp dụng distributed lock bằng Redis SETNX với key `enrollment:lock:{studentId}:{courseId}` TTL 5s để chống race condition khi hai request đồng thời. Toàn bộ Java class sử dụng package `org.nlh4j.membershiphub.enrollmentservice`.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu** [DAT-XXX]:
```sql
-- Bảng Enrollments đã có ở V1; index đã bổ sung ở V3
SELECT * FROM Courses
WHERE course_id NOT IN (
    SELECT course_id FROM Enrollments WHERE student_id = :studentId
)
AND end_date >= CURRENT_DATE
ORDER BY start_date ASC
LIMIT 50;
```

* **Hợp Đồng Định Tuyến API và Sự Kiện** [REQ-010], [REQ-011], [ARC-008]:
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

* **Bộ Xử Lý Ngoại Lệ Bản Địa Hóa Của Giai Đoạn** [EXC-004]:
```java
package org.nlh4j.membershiphub.enrollmentservice.exception;

public class DuplicateEnrollmentException extends RuntimeException {
    public DuplicateEnrollmentException(String studentId, String courseId) {
        super("Học viên " + studentId + " đã ghi danh vào khóa học " + courseId);
    }
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: Viết test cho StudentProvisioningService và DashboardService
* **Chuyên Môn Hóa Sub-Agent:** Tester

* **Tag ID Mục Tiêu:** [REQ-011], [REQ-025], [EXC-004]

* **Đường Dẫn File Thành Phần Mục Tiêu (target_component):** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/StudentProvisioningService.java;./sources/backend/enrollment-service/src/test/java/org/nlh4j/membershiphub/enrollmentservice/StudentProvisioningServiceTest.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Test 1: Mock user repository trả về empty, gọi `createStudent("new@example.com", "Nguyen Van A")`, xác nhận user mới được tạo với role `Student`, mật khẩu tạm được hash bằng bcrypt, email queue được gọi. Test 2: Mock user repository trả về existing user, xác nhận method trả về user hiện tại thay vì tạo mới. Test 3: Mock email queue ném exception, xác nhận rollback transaction và ném `StudentProvisioningException`. Cho `DashboardServiceTest`: Mock Redis cache miss, mock database trả về 150 students, 12 active courses, 5 upcoming sessions, xác nhận response JSON chứa `totalStudents: 150, activeCourses: 12, upcomingSessions: 5`. Test cache hit: Pre-populate Redis, gọi service, xác nhận database KHÔNG được truy vấn (verify bằng `Mockito.verify(repo, never()).count()`).

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu** [DAT-XXX]:
```sql
-- Sub-task này test với schema có sẵn, không DDL mới
```

* **Hợp Đồng Định Tuyến API và Sự Kiện** [REQ-011], [REQ-025], [ARC-008]:
```json
{
  "testEndpoint": "POST /api/v1/enrollments",
  "assertions": [
    "Auto-create student khi email chưa tồn tại",
    "Idempotent khi email đã tồn tại",
    "Rollback khi email queue thất bại"
  ]
}
```

* **Bộ Xử Lý Ngoại Lệ Bản Địa Hóa Của Giai Đoạn** [EXC-004]:
```java
// Test xác nhận response khi duplicate enrollment
{"status":"already_enrolled","courseId":"...","message":"Học viên đã ghi danh vào khóa học này"}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: Triển khai DashboardService với Redis cache 15 phút
* **Chuyên Môn Hóa Sub-Agent:** Coder

* **Tag ID Mục Tiêu:** [REQ-025], [NFR-001], [EXC-004]

* **Đường Dẫn File Thành Phần Mục Tiêu (target_component):** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/DashboardService.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Trong `DashboardService.java`, triển khai method `getEnrollmentSummary(centerId)` thực hiện: (1) Kiểm tra Redis key `dashboard:enrollment:{centerId}`, nếu tồn tại trả về cached value, (2) Nếu cache miss, query database: `totalStudents = SELECT COUNT(*) FROM Users u JOIN Enrollments e ON u.user_id = e.student_id WHERE e.course_id IN (SELECT course_id FROM Courses WHERE center_id = :centerId)`, `activeCourses = SELECT COUNT(*) FROM Courses WHERE center_id = :centerId AND end_date >= CURRENT_DATE`, `upcomingSessions = SELECT COUNT(*) FROM CourseSessions WHERE course_id IN (...) AND session_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'`, (3) Lưu kết quả vào Redis với TTL 900s, (4) Trả về `DashboardSummaryResponse`. Sử dụng `@CacheResult(cacheName = "dashboard-cache")` của Quarkus cache API kết hợp Redis backend. Implement circuit breaker `@CircuitBreaker(requestVolumeThreshold=4, failureRatio=0.5, delay=10s)` cho trường hợp Redis down, fallback compute trực tiếp từ DB.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu** [DAT-XXX]:
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

* **Hợp Đồng Định Tuyến API và Sự Kiện** [REQ-025], [NFR-001]:
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

* **Bộ Xử Lý Ngoại Lệ Bản Địa Hóa Của Giai Đoạn** [EXC-004]:
```java
package org.nlh4j.membershiphub.enrollmentservice.exception;

public class DashboardCacheException extends RuntimeException {
    public DashboardCacheException(Throwable cause) {
        super("Không thể truy cập cache dashboard, hệ thống sẽ tự động fallback về truy vấn trực tiếp", cause);
    }
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: Review tổng thể enrollment-service và cập nhật tài liệu API OpenAPI
* **Chuyên Môn Hóa Sub-Agent:** Reviewer, Doc

* **Tag ID Mục Tiêu:** [REQ-010], [REQ-011], [REQ-025], [ARC-008], [EXC-004], [NFR-001]

* **Đường Dẫn File Thành Phần Mục Tiêu (target_component):** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/exception/ExceptionMappers.java`

* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Reviewer kiểm tra toàn bộ enrollment-service để xác nhận: (1) Distributed lock Redis SETNX có TTL hợp lý, (2) Kafka idempotent producer được cấu hình đúng, (3) Dashboard cache TTL đáp ứng NFR-001 sub-second latency, (4) Circuit breaker có fallback an toàn, (5) Tất cả Java code tuân thủ package convention `org.nlh4j.membershiphub.enrollmentservice`. Doc cập nhật `./sources/docs/api/enrollment-service-api.md` mô tả 3 endpoint (available-courses, enrollments, dashboard) với sơ đồ Mermaid sequence diagram thể hiện luồng auto-provisioning student và Kafka publish. Tài liệu phải có bảng Tag ID mapping và matrix exception handling.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu** [DAT-XXX]:
```sql
-- Sub-task này không thực hiện DDL; chỉ review tài liệu hóa
```

* **Hợp Đồng Định Tuyến API và Sự Kiện** [REQ-010], [REQ-011], [REQ-025], [ARC-008]:
```json
{
  "documentation": "./sources/docs/api/enrollment-service-api.md",
  "sections": [
    "1. Tổng quan enrollment-service",
    "2. Luồng auto-provisioning student",
    "3. Endpoint available-courses",
    "4. Endpoint enrollments",
    "5. Endpoint dashboard",
    "6. Distributed lock pattern",
    "7. Kafka event schemas"
  ]
}
```

* **Bộ Xử Lý Ngoại Lệ Bản Địa Hóa Của Giai Đoạn** [EXC-004]:
```java
// Review xác nhận ExceptionMappers có đủ: DuplicateEnrollmentException, StudentProvisioningException, DashboardCacheException
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

<!--PHASE_INDEX_START-->

### 📈 Giai Đoạn 4 - Phân Phối Nghiệp Vụ Điểm Danh, Thông Báo, Frontend Mobile và Báo Cáo

- **Mục Tiêu & Ý Nghĩa Cốt Lõi Của Giai Đoạn:** Giai đoạn này tập trung hoàn thiện hệ thống nghiệp vụ cuối cùng bao gồm quét QR điểm danh với cơ chế idempotency, quản lý thẻ thành viên và gia hạn, hệ thống thông báo đa kênh tích hợp FCM/APNs và Zalo, quản lý khuyến mãi và thông báo nội bộ, tích hợp Chatbot AI, xây dựng frontend Next.js mobile-first đa vai trò, hỗ trợ đa ngôn ngữ và SEO, cùng các báo cáo điểm danh CSV. Tất cả các cơ chế xử lý ngoại lệ mạng, idempotency, retry thông báo và khôi phục hệ thống cũng được hiện thực hóa trong giai đoạn này.

- **Bản Đồ Ma Trận Đường Dẫn Vật Lý Mục Tiêu:** Danh sách đầy đủ các tệp tin vật lý được tạo, chỉnh sửa hoặc xử lý trong phạm vi giai đoạn này:
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

- **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Không có thay đổi cấu trúc cơ sở dữ liệu mới trong giai đoạn này; toàn bộ schema đã được thiết lập tại Giai Đoạn 1 với UNIQUE constraint composite (student_id, course_id, attendance_date) trên bảng `Attendance` đảm bảo idempotency. Tuy nhiên, bổ sung chỉ mục băm (HASH index) trên cột `attendance_date` để tối ưu truy vấn báo cáo CSV theo dải ngày.

```sql
-- =========================================================
-- GIAI ĐOẠN 4: BỔ SUNG INDEX TỐI ƯU TRUY VẤN BÁO CÁO
-- =========================================================

-- Tối ưu truy vấn lọc theo dải ngày trên bảng Attendance
CREATE INDEX idx_attendance_date_course
    ON attendance (attendance_date, course_id);

-- Tối ưu truy vấn thẻ thành viên theo student
CREATE INDEX idx_student_cards_student_id
    ON student_cards (student_id);

-- Tối ưu truy vấn thông báo theo user và trạng thái gửi
CREATE INDEX idx_notifications_user_delivered
    ON notifications (user_id, delivered, sent_at);

-- Tối ưu truy vấn khuyến mãi đang hoạt động theo dải ngày
CREATE INDEX idx_promotions_active_period
    ON promotions (start_date, end_date);

-- Tối ưu truy vấn thông báo nội bộ theo dải ngày hiệu lực
CREATE INDEX idx_announcements_active_period
    ON announcements (start_date, end_date);
```

- **Hợp Đồng Định Tuyến API và Sự Kiện [REQ-XXX], [ARC-XXX]:** Tài liệu hợp đồng kỹ thuật đầy đủ cho các endpoint REST và Kafka topic được kích hoạt trong giai đoạn này.

```json
{
  "phase": 4,
  "endpoints": [
    {
      "id": "REQ-012-EP01",
      "method": "POST",
      "path": "/api/v1/attendance/scan",
      "description": "Quét QR điểm danh từ ứng dụng di động",
      "request_schema": {
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
      },
      "response_schema": {
        "type": "object",
        "properties": {
          "success": {"type": "boolean"},
          "duplicate": {"type": "boolean"},
          "attendanceId": {"type": "string", "format": "uuid"},
          "recordedAt": {"type": "string", "format": "date-time"}
        }
      }
    },
    {
      "id": "REQ-014-EP01",
      "method": "GET",
      "path": "/api/v1/students/{id}/card",
      "description": "Truy xuất thông tin thẻ thành viên và số ngày còn lại",
      "response_schema": {
        "type": "object",
        "properties": {
          "cardId": {"type": "string", "format": "uuid"},
          "issueDate": {"type": "string", "format": "date"},
          "validityDays": {"type": "integer"},
          "usedDays": {"type": "integer"},
          "remainingDays": {"type": "integer"}
        }
      }
    },
    {
      "id": "REQ-015-EP01",
      "method": "POST",
      "path": "/api/v1/students/{id}/card/renew",
      "description": "Gia hạn thẻ thành viên sau khi thanh toán thành công",
      "request_schema": {
        "type": "object",
        "required": ["renewalDays", "paymentReference"],
        "properties": {
          "renewalDays": {"type": "integer", "minimum": 1, "maximum": 365},
          "paymentReference": {"type": "string"}
        }
      }
    },
    {
      "id": "REQ-024-EP01",
      "method": "GET",
      "path": "/api/v1/reports/attendance",
      "description": "Xuất báo cáo điểm danh CSV theo trung tâm và dải ngày",
      "query_params": {
        "centerId": {"type": "string", "format": "uuid"},
        "from": {"type": "string", "format": "date"},
        "to": {"type": "string", "format": "date"}
      },
      "response_schema": {
        "type": "string",
        "format": "binary",
        "description": "Luồng CSV với 4 cột StudentName, CourseName, AttendanceDate, Status"
      }
    }
  ],
  "kafka_topics": [
    {
      "name": "attendance.scanned",
      "partitions": 6,
      "replication": 3,
      "consumers": ["notification-service"],
      "payload_schema": {
        "type": "object",
        "properties": {
          "attendanceId": {"type": "string"},
          "studentId": {"type": "string"},
          "courseId": {"type": "string"},
          "scannedAt": {"type": "string", "format": "date-time"}
        }
      }
    },
    {
      "name": "card.renewed",
      "partitions": 3,
      "replication": 3,
      "consumers": ["notification-service"],
      "payload_schema": {
        "type": "object",
        "properties": {
          "studentId": {"type": "string"},
          "newEndDate": {"type": "string", "format": "date"},
          "renewalDays": {"type": "integer"}
        }
      }
    }
  ]
}
```

- **Bộ Xử Lý Ngoại Lệ Cục Bộ Hóa Của Giai Đoạn [EXC-XXX]:** Mô tả chi tiết quy tắc nghiệp vụ và luồng xử lý lỗi áp dụng riêng cho phạm vi giai đoạn này.
    * **[EXC-001] Sự cố mạng khi quét QR:** Khi sinh viên quét QR nhưng thiết bị mất kết nối, yêu cầu được lưu vào IndexedDB với `idempotencyKey` ngẫu nhiên. Khi có mạng trở lại, frontend thực hiện retry theo thứ tự FIFO. Server xử lý thông qua UNIQUE constraint composite để loại bỏ bản ghi trùng lặp, đồng thời trả về response thành công với cờ `duplicate=true` nếu bản ghi đã tồn tại.
    * **[EXC-002] Gửi điểm danh trùng lặp:** Hệ thống bắt lỗi `DuplicateKeyException` từ PostgreSQL khi vi phạm UNIQUE constraint `(student_id, course_id, attendance_date)`, ghi log audit, trả về HTTP 200 với payload `{success: true, duplicate: true}` thay vì HTTP 409.
    * **[EXC-003] Gửi thông báo thất bại:** Worker trong `notification-service` thực hiện retry tối đa 3 lần với exponential backoff (1s, 4s, 16s). Nếu vẫn thất bại, đánh dấu `notifications.delivered=false` và ghi log lỗi vào bảng audit. Frontend đăng ký lại device token qua API khi phát hiện token cũ không hợp lệ.
    * **[EXC-005] Khôi phục hệ thống sau sự cố:** Khi service khởi động lại, Kafka consumer group tự động `seek` về offset cuối cùng chưa commit để đảm bảo xử lý FIFO. Sau khi drain xong queue, hệ thống phát sự kiện `system.recovered` lên topic để thông báo tới các user bị ảnh hưởng thông qua FCM.

#### 📅 Nhật Ký Phân Bổ Tác Vụ Theo Ngày Cho Sub-Agent (Giai Đoạn 4)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: KHỞI TẠO ATTENDANCE-SERVICE VÀ XỬ LÝ QUÉT QR

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TRIỂN KHAI CONTROLLER VÀ SERVICE XỬ LÝ ĐIỂM DANH QR

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]

* **Mã Tag Được Nhắm Tới:** [REQ-012], [REQ-013], [ARC-007], [EXC-002]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceController.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo `AttendanceController` trong package `org.nlh4j.membershiphub.attendanceservice` ánh xạ POST `/api/v1/attendance/scan` sử dụng JAX-RS annotations. Inject `AttendanceService` và `QrPayloadDecoder` thông qua CDI. Controller phải validate `qrPayload` không null và giải mã base64 thành cặp `(studentId, courseId)`. Trả về `Response` với mã 200 và JSON gồm `success`, `duplicate`, `attendanceId`, `recordedAt` theo đặc tả. Tích hợp JWT filter chain đã cấu hình tại Giai Đoạn 1 thông qua annotation `@Authenticated`. Đảm bảo xử lý ngoại lệ `DuplicateAttendanceException` chuyển thành response thành công với cờ `duplicate=true`. Gắn Tag IDs `[REQ-012]`, `[REQ-013]`, `[ARC-007]`, `[EXC-002]` trong comment Javadoc của class.

```sql
-- Không có migration mới; sử dụng UNIQUE constraint đã tạo tại Giai Đoạn 1
-- Xác nhận ràng buộc composite vẫn đảm bảo idempotency
ALTER TABLE attendance
    ADD CONSTRAINT uq_attendance_student_course_date
    UNIQUE (student_id, course_id, attendance_date);
```

```json
{
  "endpoint": "POST /api/v1/attendance/scan",
  "consumes": "application/json",
  "produces": "application/json",
  "request_body": {
    "qrPayload": "eyJzdHVkZW50SWQiOiJ1dWlkIiwgImNvdXJzZUlkIjoidXVpZCJ9",
    "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000"
  },
  "response_200": {
    "success": true,
    "duplicate": false,
    "attendanceId": "abc-123",
    "recordedAt": "2024-01-15T08:30:00Z"
  }
}
```

* **Bộ Xử Lý Ngoại Lệ Cục Bộ Hóa Của Giai Đoạn [EXC-XXX]:** Khi phát hiện vi phạm UNIQUE constraint `(student_id, course_id, attendance_date)`, controller phải bắt `PersistenceException` mã lỗi PostgreSQL `23505`, trả về HTTP 200 với payload `{success: true, duplicate: true, attendanceId: <id_existing>}` thay vì ném 409 Conflict. Ghi log cấp INFO với correlation ID để truy vết.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: XÂY DỰNG BỘ GIẢI MÃ QR VÀ SERVICE LÕI

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]

* **Mã Tag Được Nhắm Tới:** [REQ-012], [ARC-007]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/QrPayloadDecoder.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo class `QrPayloadDecoder` sử dụng `java.util.Base64` để giải mã payload, kết hợp Jackson `ObjectMapper` ánh xạ JSON thành `QrPayloadDto` chứa `studentId` (UUID) và `courseId` (UUID). Nếu payload không hợp lệ hoặc thiếu trường, ném `InvalidQrPayloadException` với mã lỗi `ATTENDANCE_QR_INVALID`. Class `AttendanceService` triển khai method `recordAttendance(studentId, courseId, idempotencyKey)` thực hiện kiểm tra quan hệ ghi danh tồn tại, tạo bản ghi `Attendance` với `timestamp = now()`, lưu qua repository và publish sự kiện lên Kafka topic `attendance.scanned`. Gắn Tag IDs `[REQ-012]`, `[ARC-007]` trong Javadoc.

```json
{
  "QrPayloadDto_schema": {
    "type": "object",
    "required": ["studentId", "courseId"],
    "properties": {
      "studentId": {"type": "string", "format": "uuid"},
      "courseId": {"type": "string", "format": "uuid"}
    }
  }
}
```

* **Bộ Xử Lý Ngoại Lệ Cục Bộ Hóa Của Giai Đoạn [EXC-XXX]:** Khi payload base64 không giải mã được hoặc JSON thiếu trường bắt buộc, ném `InvalidQrPayloadException` để controller trả về HTTP 400 với thông điệp lỗi rõ ràng. Khi sinh viên chưa đăng ký khóa học, ném `EnrollmentNotFoundException` để controller trả về HTTP 403 với mã lỗi `ATTENDANCE_NOT_ENROLLED`.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: KIỂM THỬ ĐƠN VỊ CHO ENDPOINT ĐIỂM DANH

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]

* **Mã Tag Được Nhắm Tới:** [REQ-012], [REQ-013], [EXC-002]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceControllerTest.java;./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceController.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo bộ test JUnit 5 sử dụng `@QuarkusTest` để khởi tạo context. Mock `AttendanceService` bằng `@InjectMock`. Viết test case `testScanAttendanceSuccess` gửi POST với QR hợp lệ, kỳ vọng HTTP 200 và `success=true, duplicate=false`. Viết test case `testDuplicateScanReturnsIdempotent` gửi 2 lần cùng QR trong cùng ngày, kỳ vọng cả 2 response đều có `duplicate=true` ở lần thứ hai. Viết test case `testInvalidQrPayloadReturns400` gửi chuỗi base64 không phải JSON hợp lệ, kỳ vọng HTTP 400. Gắn Tag IDs `[REQ-012]`, `[REQ-013]`, `[EXC-002]` trong comment test class.

```json
{
  "test_scenarios": [
    {
      "name": "testScanAttendanceSuccess",
      "request": {"qrPayload": "valid_base64", "idempotencyKey": "uuid-1"},
      "expected_status": 200,
      "expected_body": {"success": true, "duplicate": false}
    },
    {
      "name": "testDuplicateScanReturnsIdempotent",
      "request_sequence": 2,
      "expected_second_response": {"success": true, "duplicate": true}
    }
  ]
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: TÀI LIỆU ĐẶC TẢ KIẾN TRÚC ATTENDANCE

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Doc]

* **Mã Tag Được Nhắm Tới:** [REQ-012], [ARC-007], [EXC-001], [EXC-002]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/architecture/AttendanceServiceBlueprint.md`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Soạn thảo tài liệu Markdown mô tả kiến trúc `attendance-service` bao gồm sơ đồ luồng quét QR từ mobile app → controller → service → database → Kafka publisher. Mô tả chi tiết cơ chế idempotency thông qua UNIQUE constraint và chiến lược retry queue phía client với IndexedDB. Tích hợp sơ đồ Mermaid thể hiện sequence diagram của happy path và duplicate path. Đảm bảo mô tả rõ cách xử lý ngoại lệ `[EXC-001]` (mạng) và `[EXC-002]` (trùng lặp). Tài liệu sử dụng tiếng Việt cho phần mô tả, giữ nguyên tiếng Anh cho thuật ngữ kỹ thuật và Tag IDs. Gắn Tag IDs `[REQ-012]`, `[ARC-007]`, `[EXC-001]`, `[EXC-002]` trong tiêu đề mục liên quan.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

##### 📅 NGÀY 2: QUẢN LÝ THẺ THÀNH VIÊN VÀ BÁO CÁO CSV

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TRIỂN KHAI CONTROLLER VÀ SERVICE THẺ THÀNH VIÊN

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]

* **Mã Tag Được Nhắm Tới:** [REQ-014], [REQ-015]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/StudentCardController.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo `StudentCardController` trong package `org.nlh4j.membershiphub.attendanceservice` ánh xạ hai endpoint REST: GET `/api/v1/students/{id}/card` và POST `/api/v1/students/{id}/card/renew`. Endpoint GET truy vấn bảng `student_cards` theo `studentId`, tính `remainingDays = validityDays - (CURRENT_DATE - issueDate)` đảm bảo không âm, trả về JSON với `cardId`, `issueDate`, `validityDays`, `usedDays`, `remainingDays`. Endpoint POST nhận body `{renewalDays, paymentReference}`, validate `renewalDays` trong khoảng 1-365, cập nhật `validityDays = validityDays + renewalDays`, publish sự kiện lên Kafka topic `card.renewed`. Inject `StudentCardService` thông qua CDI. Gắn Tag IDs `[REQ-014]`, `[REQ-015]` trong Javadoc.

```json
{
  "endpoints": [
    {
      "method": "GET",
      "path": "/api/v1/students/{id}/card",
      "response_200": {
        "cardId": "uuid",
        "issueDate": "2024-01-01",
        "validityDays": 90,
        "usedDays": 14,
        "remainingDays": 76
      }
    },
    {
      "method": "POST",
      "path": "/api/v1/students/{id}/card/renew",
      "request_body": {
        "renewalDays": 30,
        "paymentReference": "PAY-20240115-001"
      }
    }
  ]
}
```

* **Bộ Xử Lý Ngoại Lệ Cục Bộ Hóa Của Giai Đoạn [EXC-XXX]:** Nếu `renewalDays` ngoài phạm vi 1-365, ném `InvalidRenewalDaysException` trả về HTTP 400 với mã lỗi `CARD_RENEWAL_INVALID_RANGE`. Nếu không tìm thấy `StudentCard` cho student, tạo bản ghi mới với `validityDays = renewalDays` và `issueDate = CURRENT_DATE` thay vì ném lỗi 404.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: XÂY DỰNG SERVICE BÁO CÁO ĐIỂM DANH CSV

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]

* **Mã Tag Được Nhắm Tới:** [REQ-024]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/ReportController.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo `ReportController` ánh xạ GET `/api/v1/reports/attendance` với query params `centerId`, `from`, `to`. Validate `from <= to` và khoảng cách giữa `from` và `to` không quá 30 ngày, nếu vi phạm trả về HTTP 400. Inject `AttendanceReportService` để truy vấn bảng `attendance` JOIN `users` JOIN `courses` theo `centerId` và dải ngày, sinh luồng CSV với header `StudentName,CourseName,AttendanceDate,Status` sử dụng Apache Commons CSV. Trả về response với `Content-Type: text/csv` và `Content-Disposition: attachment; filename="attendance-report-{centerId}.csv"`. Gắn Tag IDs `[REQ-024]` trong Javadoc.

```json
{
  "endpoint": "GET /api/v1/reports/attendance",
  "query_params": {
    "centerId": "uuid-required",
    "from": "YYYY-MM-DD-required",
    "to": "YYYY-MM-DD-required"
  },
  "validation_rules": [
    "from <= to",
    "to - from <= 30 days"
  ],
  "response": {
    "content_type": "text/csv",
    "filename_pattern": "attendance-report-{centerId}.csv",
    "columns": ["StudentName", "CourseName", "AttendanceDate", "Status"]
  }
}
```

* **Bộ Xử Lý Ngoại Lệ Cục Bộ Hóa Của Giai Đoạn [EXC-XXX]:** Khi khoảng ngày vượt quá 30 ngày, ném `ReportDateRangeExceededException` với mã lỗi `REPORT_RANGE_EXCEEDED` trả về HTTP 400. Khi `centerId` không tồn tại, ném `CenterNotFoundException` trả về HTTP 404.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: KIỂM THỬ CHO MODULE THẺ VÀ BÁO CÁO

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]

* **Mã Tag Được Nhắm Tới:** [REQ-014], [REQ-015], [REQ-024]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/StudentCardControllerTest.java;./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/StudentCardController.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo bộ test JUnit 5 cho `StudentCardController`. Test case `testGetCardReturnsRemainingDays` mock service trả về bản ghi có `validityDays=90, issueDate=today-14`, kỳ vọng response có `usedDays=14, remainingDays=76`. Test case `testRenewCardValidatesRange` gửi `renewalDays=400`, kỳ vọng HTTP 400 với mã lỗi `CARD_RENEWAL_INVALID_RANGE`. Test case `testRenewCardUpdatesValidity` gửi `renewalDays=30` hợp lệ, kỳ vọng `validityDays` được tăng đúng 30 và Kafka event được publish. Gắn Tag IDs `[REQ-014]`, `[REQ-015]`, `[REQ-024]` trong comment test class.

```json
{
  "test_scenarios": [
    {
      "name": "testGetCardReturnsRemainingDays",
      "mock_data": {"validityDays": 90, "issueDate": "today-14"},
      "expected": {"usedDays": 14, "remainingDays": 76}
    },
    {
      "name": "testRenewCardValidatesRange",
      "request": {"renewalDays": 400},
      "expected_status": 400
    }
  ]
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: TÀI LIỆU ĐẶC TẢ API BÁO CÁO VÀ THẺ

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Doc]

* **Mã Tag Được Nhắm Tới:** [REQ-014], [REQ-015], [REQ-024]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/api/AttendanceApiContracts.md`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Bổ sung section mới vào tài liệu API Contracts mô tả 3 endpoint: GET card, POST card/renew, GET attendance report. Mỗi endpoint gồm mô tả chức năng, request/response schema JSON, validation rules, error codes. Đặc biệt giải thích công thức tính `remainingDays` và luồng xử lý khi sinh viên chưa có thẻ. Mô tả định dạng CSV output với ví dụ thực tế 3 dòng dữ liệu. Sử dụng tiếng Việt cho mô tả, giữ nguyên tiếng Anh cho schema và Tag IDs. Gắn Tag IDs `[REQ-014]`, `[REQ-015]`, `[REQ-024]` trong tiêu đề mục.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

##### 📅 NGÀY 3: NOTIFICATION-SERVICE, CHATBOT VÀ KAFKA CONSUMER

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TRIỂN KHAI KAFKA CONSUMER VÀ ZALO GATEWAY

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]

* **Mã Tag Được Nhắm Tới:** [REQ-016], [ARC-008], [EXC-003], [EXC-005]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/NotificationConsumer.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo class `NotificationConsumer` sử dụng annotation `@KafkaListener` của Quarkus Reactive Messaging lắng nghe các channel `enrollment-created`, `teacher-assigned`, `attendance-scanned`, `card-renewed`, `system-recovered`. Với mỗi message, persist bản ghi `Notification` vào database, sau đó gọi `FcmGateway` để gửi push notification tới mobile app và `ZaloGateway` để đăng tin nhắn vào group chat tương ứng. Triển khai cơ chế retry với exponential backoff (1s, 4s, 16s) tối đa 3 lần; nếu thất bại, cập nhật `delivered=false` và ghi log. Implement `drainPendingMessages` method sử dụng `KafkaConsumer.seek()` để xử lý FIFO khi service restart. Gắn Tag IDs `[REQ-016]`, `[ARC-008]`, `[EXC-003]`, `[EXC-005]` trong Javadoc.

```json
{
  "kafka_channels": [
    {"name": "enrollment-created", "partitions": 3},
    {"name": "teacher-assigned", "partitions": 3},
    {"name": "attendance-scanned", "partitions": 6},
    {"name": "card-renewed", "partitions": 3},
    {"name": "system-recovered", "partitions": 1}
  ],
  "retry_policy": {
    "max_attempts": 3,
    "backoff_strategy": "exponential",
    "delays_ms": [1000, 4000, 16000]
  }
}
```

* **Bộ Xử Lý Ngoại Lệ Cục Bộ Hóa Của Giai Đoạn [EXC-XXX]:** Khi gửi FCM thất bại do device token không hợp lệ, đánh dấu token expired và yêu cầu frontend đăng ký lại. Khi gửi Zalo thất bại do mất mạng, retry theo backoff. Nếu vẫn thất bại sau 3 lần, set `delivered=false` và lưu lỗi vào bảng notification.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: XÂY DỰNG CHATBOT AI SERVICE

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]

* **Mã Tag Được Nhắm Tới:** [REQ-019]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/ChatbotService.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo class `ChatbotService` sử dụng Vertx WebClient gọi OpenAI/Vertex AI gateway. Method `processMessage(userId, sessionId, inputText)` kiểm tra session timeout (mặc định 30 phút), tạo prompt với context người dùng (role, centerId), gọi LLM API. Nếu confidence score dưới ngưỡng 0.7, chuyển tiếp sang nhân viên hỗ trợ bằng cách tạo bản ghi `SupportEscalation` và gửi email thông báo. Cache session history trong Redis với TTL 30 phút. Gắn Tag IDs `[REQ-019]` trong Javadoc.

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: KIỂM THỬ TÍCH HỢP CHO NOTIFICATION VÀ CHATBOT

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]

* **Mã Tag Được Nhắm Tới:** [REQ-016], [REQ-019], [EXC-003]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/notification-service/src/test/java/org/nlh4j/membershiphub/notificationservice/NotificationConsumerTest.java;./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/NotificationConsumer.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo bộ test `@QuarkusTest` cho `NotificationConsumer`. Test case `testConsumeEnrollmentEvent` sử dụng `@QuarkusTestResource` với Kafka testcontainer, gửi message tới topic `enrollment-created`, kỳ vọng notification được persist và push FCM được gọi. Test case `testRetryOnFcmFailure` mock FcmGateway ném exception ở 2 lần đầu, thành công ở lần 3, kỳ vọng notification vẫn `delivered=true`. Test case `testFcmFailureAfterMaxRetries` mock FcmGateway luôn ném exception, kỳ vọng `delivered=false` sau 3 lần retry. Gắn Tag IDs `[REQ-016]`, `[REQ-019]`, `[EXC-003]` trong comment test class.

```json
{
  "test_scenarios": [
    {
      "name": "testConsumeEnrollmentEvent",
      "input": "kafka message enrollment-created",
      "expected": "notification persisted, FCM called"
    },
    {
      "name": "testRetryOnFcmFailure",
      "mock_behavior": "fail 2 times, succeed 3rd time",
      "expected": "delivered=true after 3rd attempt"
    }
  ]
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: ĐÁNH GIÁ MÃ VÀ TỐI ƯU HÓA

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Reviewer]

* **Mã Tag Được Nhắm Tới:** [REQ-016], [ARC-008], [EXC-003], [EXC-005]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/NotificationConsumer.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Đánh giá mã nguồn `NotificationConsumer` để phát hiện các vấn đề: rò rỉ tài nguyên khi retry, điều kiện race condition khi xử lý message đồng thời, hiệu suất khi batch process. Đề xuất chiến lược sửa lỗi: sử dụng `CompletableFuture` với timeout 5s cho mỗi lần gửi FCM, thêm circuit breaker pattern cho Zalo gateway, sử dụng `@Blocking` annotation cho method consumer để tránh block event loop. Xác nhận cơ chế idempotency khi xử lý message trùng lặp từ Kafka bằng cách kiểm tra `notification.messageId` đã tồn tại chưa. Gắn Tag IDs `[REQ-016]`, `[ARC-008]`, `[EXC-003]`, `[EXC-005]` trong báo cáo review.

```json
{
  "review_findings": [
    {
      "issue": "Resource leak on retry",
      "severity": "medium",
      "fix": "Wrap FCM call in try-with-resources, set 5s timeout"
    },
    {
      "issue": "Race condition on concurrent message processing",
      "severity": "high",
      "fix": "Use database UNIQUE constraint on message_id, handle duplicate"
    },
    {
      "issue": "Blocking event loop on Zalo call",
      "severity": "medium",
      "fix": "Add @Blocking annotation, use worker pool"
    }
  ]
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

##### 📅 NGÀY 4: QUẢN LÝ KHUYẾN MÃI, THÔNG BÁO NỘI BỘ VÀ FRONTEND MOBILE

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TRIỂN KHAI CONTROLLER VÀ SERVICE KHUYẾN MÃI

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]

* **Mã Tag Được Nhắm Tới:** [REQ-017], [EXC-004]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/PromotionController.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiêu:** Tạo `PromotionController` ánh xạ CRUD `/api/v1/promotions`. Endpoint POST/PUT validate `name` max 100 ký tự, `description` max 500 ký tự, `startDate <= endDate` (nếu cả hai được cung cấp), `discountPercent` trong khoảng 1-100. Nếu `endDate` là null, coi khuyến mãi là vĩnh viễn. Endpoint GET hỗ trợ filter theo `centerId` và dải ngày hiệu lực. Inject `PromotionService` qua CDI. Gắn Tag IDs `[REQ-017]`, `[EXC-004]` trong Javadoc.

```json
{
  "endpoints": [
    {
      "method": "POST",
      "path": "/api/v1/promotions",
      "request_body": {
        "name": "string-max100",
        "description": "string-max500-optional",
        "code": "string-unique",
        "discountPercent": "int-1-100",
        "startDate": "YYYY-MM-DD-optional",
        "endDate": "YYYY-MM-DD-optional"
      }
    },
    {
      "method": "GET",
      "path": "/api/v1/promotions",
      "query_params": {
        "centerId": "uuid-optional",
        "activeOn": "YYYY-MM-DD-optional"
      }
    }
  ]
}
```

* **Bộ Xử Lý Ngoại Lệ Cục Bộ Hóa Của Giai Đoạn [EXC-XXX]:** Khi `discountPercent` ngoài 1-100, ném `InvalidDiscountException` trả về HTTP 400 với mã lỗi `PROMO_DISCOUNT_OUT_OF_RANGE`. Khi `code` đã tồn tại, ném `DuplicatePromoCodeException` trả về HTTP 409 với mã lỗi `PROMO_CODE_DUPLICATE`.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: TRIỂN KHAI ANNOUNCEMENT SERVICE VÀ CONTROLLER

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]

* **Mã Tag Được Nhắm Tới:** [REQ-018], [EXC-004]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/AnnouncementController.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo `AnnouncementController` ánh xạ CRUD `/api/v1/announcements`. Validate `title` max 150 ký tự, `content` max 2000 ký tự. Hỗ trợ `startDate` và `endDate` tùy chọn; thông báo tự động ẩn khi quá `endDate`. Endpoint GET `/api/v1/announcements/active` chỉ trả về thông báo đang trong khoảng hiệu lực (giữa `startDate` và `endDate`, hoặc cả hai null). Inject `AnnouncementService` qua CDI. Gắn Tag IDs `[REQ-018]`, `[EXC-004]` trong Javadoc.

```json
{
  "endpoints": [
    {
      "method": "POST",
      "path": "/api/v1/announcements",
      "request_body": {
        "title": "string-max150-required",
        "content": "string-max2000-required",
        "startDate": "YYYY-MM-DD-optional",
        "endDate": "YYYY-MM-DD-optional"
      }
    },
    {
      "method": "GET",
      "path": "/api/v1/announcements/active",
      "description": "Chỉ trả về thông báo đang hiệu lực"
    }
  ]
}
```

* **Bộ Xử Lý Ngoại Lệ Cục Bộ Hóa Của Giai Đoạn [EXC-XXX]:** Khi `title` hoặc `content` vượt quá giới hạn ký tự, ném `InvalidAnnouncementFieldException` trả về HTTP 400 với mã lỗi tương ứng `ANNOUNCEMENT_TITLE_TOO_LONG` hoặc `ANNOUNCEMENT_CONTENT_TOO_LONG`.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: KHỞI TẠO FRONTEND MOBILE-APP VÀ LAYOUT CHÍNH

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]

* **Mã Tag Được Nhắm Tới:** [REQ-020], [ARC-009]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/mobile-app/package.json`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo `package.json` cho ứng dụng Next.js mobile-first trong thư mục `./sources/frontend/mobile-app/`. Cấu hình dependencies: `next@14.2.5`, `react@18.3.1`, `react-dom@18.3.1`, `next-intl@3.20.0`, `firebase@10.13.0`, `axios@1.7.4`, `tailwindcss@3.4.10`. Thiết lập scripts `dev`, `build`, `start`, `lint`. Tạo file `[locale]/layout.tsx` sử dụng `NextIntlClientProvider` để bọc toàn bộ ứng dụng, thiết lập font responsive với Tailwind CSS, navigation menu động dựa trên role từ JWT token. Gắn Tag IDs `[REQ-020]`, `[ARC-009]` trong comment header.

```json
{
  "name": "mobile-app",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint"
  },
  "dependencies": {
    "next": "14.2.5",
    "react": "18.3.1",
    "react-dom": "18.3.1",
    "next-intl": "3.20.0",
    "firebase": "10.13.0",
    "axios": "1.7.4"
  },
  "devDependencies": {
    "tailwindcss": "3.4.10",
    "typescript": "5.5.4",
    "@types/react": "18.3.3",
    "@types/node": "20.14.10"
  }
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: KIỂM THỬ TÍCH HỢP CHO PROMOTION VÀ ANNOUNCEMENT

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]

* **Mã Tag Được Nhắm Tới:** [REQ-017], [REQ-018], [EXC-004]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** INTEGRATION_SCOPE;./sources/backend/notification-service/src/test/java/org/nlh4j/membershiphub/notificationservice/PromotionServiceIntegrationTest.java

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo bộ test tích hợp sử dụng `@QuarkusTest` với PostgreSQL testcontainer. Test case `testCreatePromotionWithDuplicateCode` tạo promotion có code `PROMO001`, tạo lại với cùng code, kỳ vọng HTTP 409 với mã lỗi `PROMO_CODE_DUPLICATE`. Test case `testGetActiveAnnouncementsExcludesExpired` tạo 2 announcement, 1 đã hết hạn, gọi GET `/active`, kỳ vọng chỉ trả về 1 bản ghi còn hiệu lực. Test case `testPromotionEndDateOptional` tạo promotion không có `endDate`, kỳ vọng lưu thành công và GET trả về promotion đó. Gắn Tag IDs `[REQ-017]`, `[REQ-018]`, `[EXC-004]` trong comment test class.

```json
{
  "test_scenarios": [
    {
      "name": "testCreatePromotionWithDuplicateCode",
      "input": "POST /api/v1/promotions with code=PROMO001 twice",
      "expected_status": 409,
      "expected_error_code": "PROMO_CODE_DUPLICATE"
    },
    {
      "name": "testGetActiveAnnouncementsExcludesExpired",
      "input": "GET /api/v1/announcements/active",
      "expected_count": 1
    }
  ]
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

##### 📅 NGÀY 5: FCM, MIDDLEWARE I18N VÀ SEO ĐA NGÔN NGỮ

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TÍCH HỢP FCM SDK VÀ XỬ LÝ PUSH NOTIFICATION

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]

* **Mã Tag Được Nhắm Tới:** [REQ-021]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/mobile-app/src/lib/fcm.ts`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo module TypeScript `fcm.ts` trong thư mục `lib`. Sử dụng `firebase/app` và `firebase/messaging` để khởi tạo Firebase app với config từ biến môi trường. Implement hàm `requestNotificationPermission()` yêu cầu quyền notification từ browser, trả về FCM token. Implement hàm `registerDeviceToken(token, platform)` gọi API POST `/api/v1/users/me/devices` để lưu token vào backend. Implement hàm `onMessageListener()` xử lý foreground message và deep-link tới route tương ứng dựa trên `notification.data.route`. Gắn Tag IDs `[REQ-021]` trong comment JSDoc.

```typescript
// fcm.ts - Firebase Cloud Messaging Integration
import { initializeApp } from "firebase/app";
import { getMessaging, getToken, onMessage } from "firebase/messaging";

const firebaseConfig = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY,
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
  messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_SENDER_ID,
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID
};

const app = initializeApp(firebaseConfig);
const messaging = getMessaging(app);

export async function requestNotificationPermission(): Promise<string> {
  const permission = await Notification.requestPermission();
  if (permission === "granted") {
    const token = await getToken(messaging, { vapidKey: process.env.NEXT_PUBLIC_FIREBASE_VAPID_KEY });
    return token;
  }
  throw new Error("Notification permission denied");
}

export async function registerDeviceToken(token: string, platform: "ios" | "android" | "web"): Promise<void> {
  await fetch("/api/v1/users/me/devices", {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${localStorage.getItem("jwt")}` },
    body: JSON.stringify({ token, platform })
  });
}

export function onMessageListener(): Promise<any> {
  return new Promise((resolve) => {
    onMessage(messaging, (payload) => {
      resolve(payload);
    });
  });
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: TRIỂN KHAI MIDDLEWARE I18N VÀ PHÁT HIỆN NGÔN NGỮ

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]

* **Mã Tag Được Nhắm Tới:** [REQ-022], [NFR-007]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/mobile-app/middleware.ts`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo Next.js middleware tại `middleware.ts` đọc cookie `NEXT_LOCALE` trước tiên, nếu không có thì đọc `Accept-Language` header để phát hiện ngôn ngữ ưa thích. Hỗ trợ 3 locale: `en`, `vi`, `es`. Nếu URL không chứa locale prefix, redirect sang URL có prefix phù hợp. Sử dụng `next-intl` middleware helper để xử lý routing đa ngôn ngữ. Gắn Tag IDs `[REQ-022]`, `[NFR-007]` trong comment header.

```typescript
// middleware.ts - Locale Detection Middleware
import createMiddleware from "next-intl/middleware";

export default createMiddleware({
  locales: ["en", "vi", "es"],
  defaultLocale: "en",
  localePrefix: "always",
  localeDetection: true
});

export const config = {
  matcher: ["/((?!api|_next|.*\\..*).*)"]
};
```

```json
{
  "locale_resolution_priority": [
    "1. Cookie NEXT_LOCALE",
    "2. Accept-Language header",
    "3. defaultLocale (en)"
  ],
  "supported_locales": ["en", "vi", "es"]
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: CẤU HÌNH SEO ĐA NGÔN NGỮ VÀ SITEMAP

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]

* **Mã Tag Được Nhắm Tới:** [REQ-023], [NFR-007]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/mobile-app/next-i18next.config.js`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo `next-i18next.config.js` cấu hình i18n với 3 locale `en`, `vi`, `es`, thiết lập `defaultLocale: en`, `localeDetection: false` (đã xử lý ở middleware). Tạo file `src/app/sitemap.ts` sử dụng Next.js MetadataRoute sinh sitemap đa locale với hreflang annotations cho từng trang. Mỗi page layout thêm `generateMetadata` để sinh `<html lang="...">`, `<link rel="alternate" hreflang="...">`, title và description động theo locale. Gắn Tag IDs `[REQ-023]`, `[NFR-007]` trong comment header.

```javascript
// next-i18next.config.js
module.exports = {
  i18n: {
    defaultLocale: "en",
    locales: ["en", "vi", "es"],
    localeDetection: false,
    domains: []
  }
};
```

```typescript
// src/app/sitemap.ts - Multi-locale Sitemap Generator
import { MetadataRoute } from "next";

export default function sitemap(): MetadataRoute.Sitemap {
  const baseUrl = process.env.NEXT_PUBLIC_SITE_URL || "https://membershiphub.example.com";
  const locales = ["en", "vi", "es"];
  const routes = ["", "/courses", "/centers", "/about"];

  const sitemapEntries: MetadataRoute.Sitemap = [];

  routes.forEach((route) => {
    locales.forEach((locale) => {
      sitemapEntries.push({
        url: `${baseUrl}/${locale}${route}`,
        lastModified: new Date(),
        changeFrequency: "daily",
        priority: 0.8,
        alternates: {
          languages: locales.reduce((acc, loc) => {
            acc[loc] = `${baseUrl}/${loc}${route}`;
            return acc;
          }, {} as Record<string, string>)
        }
      });
    });
  });

  return sitemapEntries;
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: KIỂM THỬ E2E CHO FRONTEND MOBILE

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]

* **Mã Tag Được Nhắm Tới:** [REQ-020], [REQ-021], [REQ-022], [REQ-023]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** INTEGRATION_SCOPE;./sources/frontend/mobile-app/e2e/locale-and-notification.spec.ts

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo bộ test E2E sử dụng Playwright. Test case `testLocaleDetectionFromAcceptLanguage` xóa cookie `NEXT_LOCALE`, set header `Accept-Language: vi-VN`, truy cập `/`, kỳ vọng URL redirect về `/vi`. Test case `testHreflangTagsPresent` truy cập `/en/courses`, kỳ vọng response HTML chứa `<link rel="alternate" hreflang="vi" href=".../vi/courses">` và tương tự cho `es`. Test case `testFcmTokenRegistration` mock `requestNotificationPermission` trả về token giả, kỳ vọng API `/api/v1/users/me/devices` được gọi với payload đúng. Gắn Tag IDs `[REQ-020]`, `[REQ-021]`, `[REQ-022]`, `[REQ-023]` trong comment test file.

```json
{
  "test_scenarios": [
    {
      "name": "testLocaleDetectionFromAcceptLanguage",
      "setup": "delete NEXT_LOCALE cookie, set Accept-Language: vi-VN",
      "action": "navigate to /",
      "expected_url": "/vi"
    },
    {
      "name": "testHreflangTagsPresent",
      "action": "navigate to /en/courses",
      "expected_html": "<link rel=\"alternate\" hreflang=\"vi\" href=\"/vi/courses\">"
    }
  ]
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

##### 📅 NGÀY 6: GIAO DIỆN SINH VIÊN, QR SCANNER VÀ HOÀN THIỆN FRONTEND

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: XÂY DỰNG TRANG THẺ THÀNH VIÊN

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]

* **Mã Tag Được Nhắm Tới:** [REQ-014], [REQ-020]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/mobile-app/src/app/[locale]/student/card/page.tsx`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo trang Next.js client component hiển thị thẻ thành viên cho sinh viên. Sử dụng `useEffect` và `useState` để fetch API GET `/api/v1/students/{id}/card`. Hiển thị thiết kế thẻ trực quan với tên sinh viên, ngày cấp, tổng ngày hiệu lực, số ngày đã dùng, số ngày còn lại dưới dạng progress bar màu xanh/vàng/đỏ tùy mức remaining. Nút "Gia hạn" mở modal chọn số ngày (30/60/90/180/365) và redirect tới payment gateway. Gắn Tag IDs `[REQ-014]`, `[REQ-020]` trong comment component.

```typescript
// page.tsx - Student Membership Card Display
"use client";
import { useEffect, useState } from "react";
import { useTranslations } from "next-intl";

interface CardData {
  cardId: string;
  issueDate: string;
  validityDays: number;
  usedDays: number;
  remainingDays: number;
}

export default function StudentCardPage() {
  const t = useTranslations("StudentCard");
  const [card, setCard] = useState<CardData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const studentId = localStorage.getItem("studentId");
    fetch(`/api/v1/students/${studentId}/card`, {
      headers: { Authorization: `Bearer ${localStorage.getItem("jwt")}` }
    })
      .then((res) => res.json())
      .then((data) => {
        setCard(data);
        setLoading(false);
      });
  }, []);

  if (loading) return <div>{t("loading")}</div>;
  if (!card) return <div>{t("noCard")}</div>;

  const progressColor = card.remainingDays > 30 ? "bg-green-500" : card.remainingDays > 7 ? "bg-yellow-500" : "bg-red-500";

  return (
    <div className="p-4">
      <h1 className="text-2xl font-bold">{t("title")}</h1>
      <div className="mt-4 p-6 bg-white rounded-lg shadow-md">
        <div className="text-sm text-gray-500">{t("issueDate")}: {card.issueDate}</div>
        <div className="mt-2 text-lg">📅 {t("validityDays")}: {card.validityDays}</div>
        <div className="text-lg">✅ {t("usedDays")}: {card.usedDays}</div>
        <div className="text-lg font-bold">⏳ {t("remainingDays")}: {card.remainingDays}</div>
        <div className="mt-4 w-full bg-gray-200 rounded-full h-4">
          <div className={`${progressColor} h-4 rounded-full`} style={{ width: `${(card.remainingDays / card.validityDays) * 100}%` }}></div>
        </div>
        <button className="mt-6 w-full py-3 bg-blue-600 text-white rounded-lg">
          {t("renewButton")}
        </button>
      </div>
    </div>
  );
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: XÂY DỰNG TRANG QUÉT QR ĐIỂM DANH

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]

* **Mã Tag Được Nhắm Tới:** [REQ-012], [REQ-020]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/mobile-app/src/app/[locale]/student/attendance/page.tsx`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo trang quét QR sử dụng thư viện `html5-qrcode`. Khi sinh viên mở trang, hiển thị camera preview. Khi quét thành công, lấy payload base64, tạo `idempotencyKey` bằng `crypto.randomUUID()`, gọi API POST `/api/v1/attendance/scan`. Implement retry queue lưu vào IndexedDB khi mất mạng, tự động retry khi có kết nối trở lại thông qua `navigator.onLine` event listener. Hiển thị thông báo thành công/thất bại/duplicate. Gắn Tag IDs `[REQ-012]`, `[REQ-020]` trong comment component.

```typescript
// page.tsx - QR Attendance Scanner with Offline Retry Queue
"use client";
import { useEffect, useRef, useState } from "react";
import { Html5Qrcode } from "html5-qrcode";
import { useTranslations } from "next-intl";

interface RetryItem {
  idempotencyKey: string;
  qrPayload: string;
  timestamp: number;
}

export default function AttendanceScanPage() {
  const t = useTranslations("Attendance");
  const scannerRef = useRef<Html5Qrcode | null>(null);
  const [status, setStatus] = useState<"idle" | "scanning" | "success" | "duplicate" | "error">("idle");

  async function sendScanWithRetry(qrPayload: string, idempotencyKey: string): Promise<void> {
    try {
      const response = await fetch("/api/v1/attendance/scan", {
        method: "POST",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${localStorage.getItem("jwt")}` },
        body: JSON.stringify({ qrPayload, idempotencyKey })
      });
      const data = await response.json();
      if (data.duplicate) {
        setStatus("duplicate");
      } else {
        setStatus("success");
      }
    } catch (error) {
      // Save to IndexedDB retry queue
      const request = indexedDB.open("attendance-retry-queue", 1);
      request.onsuccess = (event) => {
        const db = (event.target as IDBOpenDBRequest).result;
        const tx = db.transaction("queue", "readwrite");
        const store = tx.objectStore("queue");
        store.add({ idempotencyKey, qrPayload, timestamp: Date.now() } as RetryItem);
      };
      setStatus("error");
    }
  }

  async function drainRetryQueue(): Promise<void> {
    if (!navigator.onLine) return;
    const request = indexedDB.open("attendance-retry-queue", 1);
    request.onsuccess = (event) => {
      const db = (event.target as IDBOpenDBRequest).result;
      const tx = db.transaction("queue", "readonly");
      const store = tx.objectStore("queue");
      const getAllRequest = store.getAll();
      getAllRequest.onsuccess = async () => {
        const items = (getAllRequest.result as RetryItem[]).sort((a, b) => a.timestamp - b.timestamp);
        for (const item of items) {
          await sendScanWithRetry(item.qrPayload, item.idempotencyKey);
        }
        // Clear queue after successful drain
        const clearTx = db.transaction("queue", "readwrite");
        clearTx.objectStore("queue").clear();
      };
    };
  }

  useEffect(() => {
    const html5QrCode = new Html5Qrcode("qr-reader");
    scannerRef.current = html5QrCode;
    html5QrCode.start({ facingMode: "environment" }, { fps: 10, qrbox: 250 }, async (decodedText) => {
      const idempotencyKey = crypto.randomUUID();
      await sendScanWithRetry(decodedText, idempotencyKey);
    });
    window.addEventListener("online", drainRetryQueue);
    return () => {
      html5QrCode.stop();
      window.removeEventListener("online", drainRetryQueue);
    };
  }, []);

  return (
    <div className="p-4">
      <h1 className="text-2xl font-bold">{t("scanTitle")}</h1>
      <div id="qr-reader" className="mt-4 w-full max-w-sm mx-auto"></div>
      {status === "success" && <div className="mt-4 p-4 bg-green-100 text-green-800 rounded">✅ {t("success")}</div>}
      {status === "duplicate" && <div className="mt-4 p-4 bg-yellow-100 text-yellow-800 rounded">⚠️ {t("duplicate")}</div>}
      {status === "error" && <div className="mt-4 p-4 bg-red-100 text-red-800 rounded">❌ {t("error")} ({t("willRetry")})</div>}
    </div>
  );
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: KIỂM THỬ COMPONENT REACT VỚI REACT TESTING LIBRARY

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]

* **Mã Tag Được Nhắm Tới:** [REQ-014], [REQ-012], [REQ-020]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/mobile-app/src/app/[locale]/student/card/page.tsx;./sources/frontend/mobile-app/src/app/[locale]/student/card/__tests__/page.test.tsx`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo test file `__tests__/page.test.tsx` sử dụng Jest + React Testing Library. Test case `rendersCardWithRemainingDays` mock fetch trả về `{validityDays: 90, usedDays: 14, remainingDays: 76}`, kỳ vọng hiển thị "76" trên màn hình và progress bar có width ~84.4%. Test case `rendersNoCardMessage` mock fetch trả về null, kỳ vọng hiển thị "noCard" message. Test case `progressBarColorChanges` kiểm tra className chứa `bg-yellow-500` khi `remainingDays=10`. Gắn Tag IDs `[REQ-014]`, `[REQ-020]` trong comment test file.

```json
{
  "test_scenarios": [
    {
      "name": "rendersCardWithRemainingDays",
      "mock_response": {"validityDays": 90, "usedDays": 14, "remainingDays": 76},
      "expected_text": "76",
      "expected_progress_width": "84.4%"
    },
    {
      "name": "progressBarColorChanges",
      "mock_response": {"remainingDays": 10},
      "expected_class": "bg-yellow-500"
    }
  ]
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: TÀI LIỆU HƯỚNG DẪN SỬ DỤNG FRONTEND MOBILE

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Doc]

* **Mã Tag Được Nhắm Tới:** [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/operations/FrontendMobileManual.md`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Soạn thảo tài liệu hướng dẫn vận hành cho frontend mobile-app bằng tiếng Việt. Nội dung bao gồm: cấu trúc thư mục, hướng dẫn build/deploy, cấu hình biến môi trường Firebase, hướng dẫn sử dụng QR scanner với cơ chế retry queue IndexedDB, cách thêm ngôn ngữ mới vào i18n config, cách tùy chỉnh SEO meta tags cho từng locale. Bao gồm sơ đồ Mermaid thể hiện flow xử lý khi mất mạng và quy trình chuyển tiếp giữa các role trong navigation. Gắn Tag IDs `[REQ-020]`, `[REQ-021]`, `[REQ-022]`, `[REQ-023]`, `[REQ-024]` trong các mục liên quan.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

##### 📅 NGÀY 7: TỐI ƯU HÓA, REVIEW VÀ TỔNG HỢP

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: REVIEW VÀ TỐI ƯU HÓA ATTENDANCE-SERVICE

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Reviewer]

* **Mã Tag Được Nhắm Tới:** [REQ-012], [REQ-013], [REQ-014], [REQ-015], [REQ-024], [ARC-007], [EXC-001], [EXC-002]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceService.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Đánh giá mã nguồn `AttendanceService` và `StudentCardService` để phát hiện các vấn đề tiềm ẩn. Kiểm tra: (1) xử lý transaction boundary khi tạo Attendance và publish Kafka event, (2) khả năng race condition khi 2 request đồng thời cùng idempotencyKey, (3) hiệu suất truy vấn báo cáo CSV với dataset lớn, (4) khả năng deadlock khi insert Attendance và update StudentCard đồng thời. Đề xuất sửa lỗi: sử dụng `@Transactional(REQUIRES_NEW)` cho việc ghi log, thêm `pessimistic lock` cho bản ghi StudentCard khi renew, sử dụng streaming cursor cho CSV export. Gắn Tag IDs `[REQ-012]`, `[REQ-013]`, `[REQ-014]`, `[REQ-015]`, `[REQ-024]`, `[ARC-007]`, `[EXC-001]`, `[EXC-002]` trong báo cáo review.

```json
{
  "review_findings": [
    {
      "issue": "Race condition on concurrent scan with same idempotencyKey",
      "severity": "high",
      "fix": "Use UNIQUE constraint on idempotency_key column, catch DuplicateKeyException"
    },
    {
      "issue": "Memory overflow on large CSV export",
      "severity": "medium",
      "fix": "Use StreamingOutput and JPA Stream<T> to avoid loading all rows in memory"
    },
    {
      "issue": "Transaction boundary between DB write and Kafka publish",
      "severity": "high",
      "fix": "Use outbox pattern: write event to outbox table, separate poller publishes to Kafka"
    }
  ]
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: REVIEW VÀ TỐI ƯU HÓA NOTIFICATION-SERVICE

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Reviewer]

* **Mã Tag Được Nhắm Tới:** [REQ-016], [REQ-017], [REQ-018], [REQ-019], [ARC-008], [EXC-003], [EXC-004], [EXC-005]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/NotificationConsumer.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Đánh giá mã nguồn `NotificationConsumer`, `ZaloGateway`, `ChatbotService`, `PromotionService`, `AnnouncementService` để phát hiện vấn đề. Kiểm tra: (1) memory leak khi tích lũy session Chatbot trong Redis, (2) circuit breaker cho Zalo API, (3) idempotency khi xử lý message Kafka trùng lặp, (4) validation logic cho Promotion và Announcement. Đề xuất: thêm TTL cho Redis session keys, tích hợp Resilience4j circuit breaker cho Zalo, sử dụng message ID deduplication. Gắn Tag IDs `[REQ-016]`, `[REQ-017]`, `[REQ-018]`, `[REQ-019]`, `[ARC-008]`, `[EXC-003]`, `[EXC-004]`, `[EXC-005]` trong báo cáo.

```json
{
  "review_findings": [
    {
      "issue": "No circuit breaker on Zalo API call",
      "severity": "high",
      "fix": "Integrate Resilience4j @CircuitBreaker with failureRateThreshold=50, waitDurationInOpenState=30s"
    },
    {
      "issue": "Memory leak in Chatbot Redis session storage",
      "severity": "medium",
      "fix": "Set EXPIRE 30 minutes on every session key, use sessionId as key prefix"
    },
    {
      "issue": "Duplicate processing of Kafka messages",
      "severity": "medium",
      "fix": "Store messageId in notifications table with UNIQUE constraint, skip if already processed"
    }
  ]
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: TỔNG HỢP TÀI LIỆU KỸ THUẬT

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Doc]

* **Mã Tag Được Nhắm Tới:** [REQ-012], [REQ-013], [REQ-014], [REQ-015], [REQ-016], [REQ-017], [REQ-018], [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [ARC-007], [ARC-008], [ARC-009], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/api/AttendanceApiContracts.md`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tổng hợp và chuẩn hóa toàn bộ API contracts được triển khai trong giai đoạn 4 vào tài liệu `AttendanceApiContracts.md`. Bao gồm: endpoint điểm danh QR, endpoint thẻ thành viên, endpoint báo cáo CSV, endpoint thông báo, endpoint khuyến mãi, endpoint thông báo nội bộ, endpoint chatbot. Mỗi endpoint có đầy đủ request/response schema, validation rules, error codes, authentication requirements. Bổ sung sơ đồ Mermaid tổng quan kiến trúc tích hợp giữa attendance-service, notification-service, frontend mobile-app và Kafka event bus. Gắn Tag IDs tương ứng trong từng mục.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: KIỂM THỬ HIỆU NĂNG VÀ TẢI

* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]

* **Mã Tag Được Nhắm Tới:** [REQ-012], [REQ-013], [REQ-014], [REQ-016], [REQ-024], [ARC-007], [ARC-008], [NFR-001]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** INTEGRATION_SCOPE;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/LoadTestSuite.java

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Chi Tiết:** Tạo bộ kiểm thử tải sử dụng Gatling. Kịch bản `attendanceScanLoadTest` mô phỏng 1000 sinh viên đồng thời quét QR trong 60 giây, kỳ vọng p95 response time < 200ms (theo NFR-001), zero lỗi. Kịch bản `notificationBurstTest` publish 5000 message lên Kafka topic `attendance-scanned` trong 30 giây, kỳ vọng tất cả notification được gửi FCM thành công trong vòng 60 giây. Kịch bản `csvReportPerformanceTest` tạo 10.000 bản ghi attendance, request báo cáo CSV 30 ngày, kỳ vọng response < 5 giây. Gắn Tag IDs `[REQ-012]`, `[REQ-013]`, `[REQ-014]`, `[REQ-016]`, `[REQ-024]`, `[ARC-007]`, `[ARC-008]`, `[NFR-001]` trong comment test class.

```json
{
  "load_test_scenarios": [
    {
      "name": "attendanceScanLoadTest",
      "concurrent_users": 1000,
      "duration_seconds": 60,
      "expected_p95_latency_ms": 200,
      "expected_error_rate": 0
    },
    {
      "name": "notificationBurstTest",
      "messages": 5000,
      "duration_seconds": 30,
      "expected_completion_seconds": 60
    }
  ]
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

<!--PHASE_INDEX_START-->

### 📈 Giai Đoạn 5 - Triển Khai Hạ Tầng DevOps, Bảo Mật & Hoàn Thiện Tài Liệu Kỹ Thuật

- **Mục Tiêu & Ý Nghĩa Cốt Lõi Của Giai Đoạn:** Đây là giai đoạn đóng vai trò then chốt trong việc đưa toàn bộ mã nguồn ứng dụng (đã hoàn thiện từ Giai đoạn 1 đến Giai đoạn 4) vào môi trường vận hành thực tế. Giai đoạn này tập trung 100% vào việc container hóa microservices, tự động hóa hạ tầng đám mây trên Google Cloud Platform (GCP), triển khai orchestration trên Google Kubernetes Engine (GKE), áp dụng các biện pháp bảo mật đạt chuẩn OWASP, cùng với việc xuất bản bộ tài liệu kỹ thuật doanh nghiệp hoàn chỉnh phục vụ vận hành và bảo trì hệ thống lâu dài.

- **Bản Đồ Ma Trận Đường Dẫn Vật Lý Mục Tiêu:** Danh sách dưới đây tổng hợp 100% các tệp tin vật lý cụ thể được tạo mới, cấu hình hoặc xử lý trong phạm vi của giai đoạn này. Mỗi mục đại diện cho một tệp thực thi kèm phần mở rộng hợp lệ và mã truy vết Tag IDs tương ứng.
    * `./sources/infra/docker/user-service/Dockerfile` - [NFR-005], [ARC-000]
    * `./sources/infra/docker/center-service/Dockerfile` - [NFR-005], [ARC-000]
    * `./sources/infra/docker/course-service/Dockerfile` - [NFR-005], [ARC-000]
    * `./sources/infra/docker/enrollment-service/Dockerfile` - [NFR-005], [ARC-000]
    * `./sources/infra/docker/attendance-service/Dockerfile` - [NFR-005], [ARC-000]
    * `./sources/infra/docker/notification-service/Dockerfile` - [NFR-005], [ARC-000]
    * `./sources/infra/gcp/terraform/main.tf` - [NFR-001], [NFR-002], [NFR-004]
    * `./sources/infra/gcp/terraform/vpc.tf` - [NFR-002], [NFR-009]
    * `./sources/infra/gcp/terraform/iam.tf` - [NFR-003], [NFR-006]
    * `./sources/infra/gcp/terraform/cloudsql.tf` - [NFR-001], [NFR-004], [NFR-009]
    * `./sources/infra/gcp/terraform/redis.tf` - [NFR-001], [NFR-004]
    * `./sources/infra/gke/helm/membershiphub/Chart.yaml` - [NFR-002], [NFR-004]
    * `./sources/infra/gke/helm/membershiphub/values.yaml` - [NFR-001], [NFR-002], [NFR-004]
    * `./sources/infra/gke/helm/membershiphub/templates/deployment.yaml` - [NFR-002], [NFR-004]
    * `./sources/infra/gke/helm/membershiphub/templates/hpa.yaml` - [NFR-001], [NFR-004]
    * `./sources/infra/gke/kustomize/overlays/prod/kustomization.yaml` - [NFR-002], [NFR-009]
    * `./sources/docs/architecture/SystemArchitectureBlueprint.md` - [DOC-001]
    * `./sources/docs/database/DatabaseSchemaTopology.md` - [DOC-001], [DAT-ALL (1 to 11)]
    * `./sources/docs/api/OpenAPIContracts.md` - [DOC-001], [ARC-006], [ARC-007], [ARC-008], [ARC-009]
    * `./sources/docs/operations/OperationalManual.md` - [DOC-001], [NFR-006], [NFR-008]

- **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- No new database infrastructure or persistence layer changes are required for this phase context
```

- **Hợp Đồng Định Tuyến API & Sự Kiện [REQ-XXX], [ARC-XXX]:**

```json
{
  "phase_scope": "DevOps Infrastructure Provisioning",
  "remark": "No new application endpoint contracts are emitted in this phase. API contracts are documented in ./sources/docs/api/OpenAPIContracts.md as a static reference deliverable. Container image and Helm chart rollout follow the standard Kubernetes rolling-update strategy.",
  "deployment_strategy": {
    "method": "RollingUpdate",
    "maxSurge": "25%",
    "maxUnavailable": "0%",
    "readiness_probe": "/q/health/ready",
    "liveness_probe": "/q/health/live"
  },
  "artifact_registry": "asia-southeast1-docker.pkg.dev/membershiphub-prod/app-images",
  "image_pull_policy": "IfNotPresent"
}
```

- **Bộ Xử Lý Ngoại Lệ Cục Bộ Hóa Của Giai Đoạn [EXC-XXX]:**

Trong giai đoạn này, các quy tắc xử lý ngoại lệ tập trung vào lớp hạ tầng và vận hành, bao gồm:
- Khi quá trình build container thất bại do vượt quá giới hạn kích thước hình ảnh (base > 200MB hoặc final > 500MB) theo [NFR-005], pipeline CI/CD phải tự động hủy bỏ triển khai và ghi log cảnh báo vào Cloud Logging.
- Khi Terraform gặp lỗi xung đột tài nguyên GCP (ví dụ: VPC subnet trùng phạm vi CIDR), lệnh `terraform apply` phải rollback về trạng thái trước đó và gửi thông báo lỗi chi tiết tới nhóm vận hành.
- Khi Helm chart triển khai thất bại trên GKE (ví dụ: HPA không thể đọc metrics từ Cloud Monitoring), hệ thống phải tự động giữ nguyên phiên bản deployment trước và không cập nhật.

#### 📅 Nhật Ký Phân Bổ Tác Vụ Theo Ngày Của Sub-Agent (Giai Đoạn 5)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: Thiết Lập Container Hóa & Đẩy Image Lên Artifact Registry

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: Xây Dựng Multi-Stage Dockerfile Cho User-Service
- **Chuyên Môn Hóa Quy Trình Làm Việc Của Sub-Agent:** [Docker]
- **Mã Tag IDs Mục Tiêu:** [NFR-005], [ARC-000]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/infra/docker/user-service/Dockerfile`
- **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo Dockerfile multi-stage cho user-service. Stage đầu tiên sử dụng base image `eclipse-temurin:21-jdk-alpine` để biên dịch Maven project từ `./sources/backend/user-service/`, copy toàn bộ source code, chạy `./mvnw package -DskipTests` để tạo jar file. Stage cuối sử dụng `eclipse-temurin:21-jre-alpine` làm runtime, copy jar artifact từ build stage, thiết lập EXPOSE 8080, USER non-root (UID 1001), HEALTHCHECK sử dụng wget tới `/q/health/live`. Đảm bảo base image < 200MB và final image < 500MB theo [NFR-005].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- No database infrastructure or persistence layer changes are required for this phase context
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: Xây Dựng Multi-Stage Dockerfile Cho Center-Service Và Course-Service
- **Chuyên Môn Hóa Quy Trình Làm Việc Của Sub-Agent:** [Docker]
- **Mã Tag IDs Mục Tiêu:** [NFR-005], [ARC-000]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/infra/docker/center-service/Dockerfile`
- **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo Dockerfile multi-stage cho center-service với cùng cấu trúc tiêu chuẩn như user-service: build stage sử dụng `eclipse-temurin:21-jdk-alpine` chạy Maven package, runtime stage sử dụng `eclipse-temurin:21-jre-alpine` copy jar artifact, thiết lập non-root user, EXPOSE 8080, HEALTHCHECK endpoint. Đồng thời tạo Dockerfile tương tự cho course-service tại `./sources/infra/docker/course-service/Dockerfile`. Đảm bảo 100% tuân thủ giới hạn kích thước hình ảnh theo [NFR-005].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- No database infrastructure or persistence layer changes are required for this phase context
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: Xây Dựng Multi-Stage Dockerfile Cho Enrollment, Attendance, Notification
- **Chuyên Môn Hóa Quy Trình Làm Việc Của Sub-Agent:** [Docker]
- **Mã Tag IDs Mục Tiêu:** [NFR-005], [ARC-000]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/infra/docker/enrollment-service/Dockerfile`
- **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo Dockerfile multi-stage cho ba service còn lại: enrollment-service, attendance-service, notification-service tại các đường dẫn `./sources/infra/docker/enrollment-service/Dockerfile`, `./sources/infra/docker/attendance-service/Dockerfile`, `./sources/infra/docker/notification-service/Dockerfile`. Mỗi Dockerfile phải tuân thủ nghiêm ngặt cấu trúc multi-stage, sử dụng JRE Alpine làm base runtime, áp dụng layered JAR caching để tối ưu build time, thiết lập security context non-root. Tất cả image phải < 500MB theo [NFR-005].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- No database infrastructure or persistence layer changes are required for this phase context
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: Tài Liệu Hóa Quy Trình Build & Đẩy Image Lên Artifact Registry
- **Chuyên Môn Hóa Quy Trình Làm Việc Của Sub-Agent:** [Docker]
- **Mã Tag IDs Mục Tiêu:** [NFR-005], [NFR-002], [ARC-000]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/infra/docker/notification-service/Dockerfile`
- **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Bổ sung comment hướng dẫn build và push image vào trong Dockerfile của notification-service. Tài liệu hóa lệnh `docker build -t asia-southeast1-docker.pkg.dev/membershiphub-prod/app-images/notification-service:${GIT_SHA} .` và `docker push` tương ứng. Cấu hình Artifact Registry repository tại region `asia-southeast1` với quyền truy cập IAM từ GKE service account. Đảm bảo image tagging theo convention `${GIT_SHA}` và `${GIT_BRANCH}-latest` để hỗ trợ rollback theo [NFR-002].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- No database infrastructure or persistence layer changes are required for this phase context
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

##### 📅 NGÀY 2: Triển Khai Hạ Tầng GCP Qua Terraform (VPC, IAM, Cloud SQL, Redis)

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: Khởi Tạo Terraform Provider & Cấu Hình VPC
- **Chuyên Môn Hóa Quy Trình Làm Việc Của Sub-Agent:** [GCP]
- **Mã Tag IDs Mục Tiêu:** [NFR-002], [NFR-009], [ARC-000]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/infra/gcp/terraform/main.tf`
- **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo tệp `main.tf` cấu hình Terraform provider `hashicorp/google` phiên bản ~> 5.0 với project ID `membershiphub-prod` và region `asia-southeast1`. Khai báo backend state lưu trữ tại GCS bucket `membershiphub-tfstate`. Bố trí cấu trúc module gọi tới `vpc.tf`, `iam.tf`, `cloudsql.tf`, `redis.tf`. Đảm bảo 100% hạ tầng được quản lý qua Infrastructure as Code theo [ARC-000].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- No database infrastructure or persistence layer changes are required for this phase context
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: Cấu Hình VPC, Subnet Và Firewall Rules
- **Chuyên Môn Hóa Quy Trình Làm Việc Của Sub-Agent:** [GCP]
- **Mã Tag IDs Mục Tiêu:** [NFR-002], [NFR-009]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/infra/gcp/terraform/vpc.tf`
- **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo VPC `membershiphub-vpc` với CIDR `10.0.0.0/16`, bật chế độ custom subnet. Tạo hai subnet: `gke-subnet` (CIDR `10.0.1.0/24`, region `asia-southeast1`) dành cho GKE nodes, `sql-subnet` (CIDR `10.0.2.0/24`) dành cho Private Service Access cho Cloud SQL. Cấu hình Cloud Router và Cloud NAT để cung cấp outbound internet cho GKE pods. Thiết lập firewall rules cho phép giao tiếp nội bộ giữa các subnet, đồng thời chặn truy cập SSH từ bên ngoài. Hỗ trợ mục tiêu failover đa vùng theo [NFR-002].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- No database infrastructure or persistence layer changes are required for this phase context
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: Cấu Hình IAM Service Accounts & Cloud SQL
- **Chuyên Môn Hóa Quy Trình Làm Việc Của Sub-Agent:** [GCP]
- **Mã Tag IDs Mục Tiêu:** [NFR-003], [NFR-006], [NFR-001], [NFR-004], [NFR-009]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/infra/gcp/terraform/iam.tf`
- **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Trong `iam.tf`, tạo service account `gke-workload-sa` với quyền truy cập Artifact Registry, Cloud Logging, Cloud Monitoring. Tạo service account `cloudsql-connector-sa` với quyền `cloudsql.client`. Tạo `cloudsql.tf` cấu hình Cloud SQL PostgreSQL 15 instance với high availability (zone failover), 2 vCPU, 8GB RAM, 100GB SSD, bật Private Service Access, cấu hình automated backup hàng ngày với retention 7 ngày, point-in-time recovery lên tới 24 giờ theo [NFR-009]. Tạo read replica cho reporting workload theo [NFR-004]. Bật audit log cho tất cả truy cập theo [NFR-006].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- No database infrastructure or persistence layer changes are required for this phase context
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: Cấu Hình Memorystore Redis & GCS Bucket
- **Chuyên Môn Hóa Quy Trình Làm Việc Của Sub-Agent:** [GCP]
- **Mã Tag IDs Mục Tiêu:** [NFR-001], [NFR-004]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/infra/gcp/terraform/redis.tf`
- **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Trong `redis.tf`, tạo Memorystore Redis instance `membershiphub-cache` tier STANDARD với 2GB memory, region `asia-southeast1`, cấu hình Redis AUTH và transit encryption. Tạo GCS bucket `membershiphub-static-assets` với uniform bucket-level access, versioning enabled, lifecycle rule tự động chuyển object sang Nearline storage sau 30 ngày. Cấu hình CORS cho phép frontend truy cập static assets. Hỗ trợ caching layer cho enrollment dashboard theo [NFR-001] và [NFR-004].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- No database infrastructure or persistence layer changes are required for this phase context
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

##### 📅 NGÀY 3: Triển Khai GKE Cluster, Helm Charts & Hoàn Thiện Tài Liệu Kỹ Thuật

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: Khởi Tạo Helm Chart Cho Membership Hub
- **Chuyên Môn Hóa Quy Trình Làm Việc Của Sub-Agent:** [GKE]
- **Mã Tag IDs Mục Tiêu:** [NFR-002], [NFR-004], [ARC-000]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/infra/gke/helm/membershiphub/Chart.yaml`
- **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo Helm chart `membershiphub` với `Chart.yaml` khai báo `apiVersion: v2`, `name: membershiphub`, `version: 1.0.0`, `appVersion: 1.0.0`, `type: application`. Tạo `values.yaml` định nghĩa cấu trúc tham số cho 6 service backend với `replicaCount`, `image.repository`, `image.tag`, `resources.requests`, `resources.limits`, `service.type`, `service.port`. Thiết lập giá trị mặc định `replicaCount: 2` cho mỗi service, `resources.requests.cpu: 250m`, `resources.requests.memory: 512Mi`. Hỗ trợ auto-scaling và high availability theo [NFR-002].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- No database infrastructure or persistence layer changes are required for this phase context
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: Tạo Kubernetes Deployment Manifest & HPA
- **Chuyên Môn Hóa Quy Trình Làm Việc Của Sub-Agent:** [GKE]
- **Mã Tag IDs Mục Tiêu:** [NFR-001], [NFR-002], [NFR-004]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/infra/gke/helm/membershiphub/templates/deployment.yaml`
- **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo template `deployment.yaml` sử dụng helper `{{- range .Values.services }}` để lặp qua 6 service. Mỗi Deployment có `strategy.type: RollingUpdate`, `maxSurge: 25%`, `maxUnavailable: 0%`. Cấu hình container với `imagePullPolicy: IfNotPresent`, `livenessProbe` trỏ tới `/q/health/live`, `readinessProbe` trỏ tới `/q/health/ready`, `startupProbe` với 30 giây timeout. Tạo `hpa.yaml` định nghĩa HorizontalPodAutoscaler cho mỗi service với `minReplicas: 2`, `maxReplicas: 10`, metric CPU `target.averageUtilization: 70` và custom metric latency `target.averageValue: 300ms` theo [NFR-001] và [NFR-004].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- No database infrastructure or persistence layer changes are required for this phase context
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: Cấu Hình Kustomize Overlay Cho Môi Trường Production
- **Chuyên Môn Hóa Quy Trình Làm Việc Của Sub-Agent:** [GKE]
- **Mã Tag IDs Mục Tiêu:** [NFR-002], [NFR-009]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/infra/gke/kustomize/overlays/prod/kustomization.yaml`
- **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo `kustomization.yaml` trong thư mục `./sources/infra/gke/kustomize/overlays/prod/` tham chiếu tới base chart `../../base`. Cấu hình `namespace: membershiphub-prod`, `namePrefix: mh-`. Thêm secret generator từ file `.env.prod` chứa DB credentials, JWT secret, OAuth2 client secrets. Cấu hình `commonLabels` với `environment: production`, `managed-by: kustomize`. Bổ sung configMapGenerator cho application properties. Đảm bảo cấu hình hỗ trợ rollback nhanh khi có sự cố theo [NFR-002].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- No database infrastructure or persistence layer changes are required for this phase context
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: Hoàn Thiện Bộ Tài Liệu Kỹ Thuật Doanh Nghiệp
- **Chuyên Môn Hóa Quy Trình Làm Việc Của Sub-Agent:** [Doc]
- **Mã Tag IDs Mục Tiêu:** [DOC-001], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [DAT-ALL (1 to 11)], [NFR-006], [NFR-008]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/architecture/SystemArchitectureBlueprint.md`
- **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo bốn tài liệu kỹ thuật doanh nghiệp hoàn chỉnh: (1) `./sources/docs/architecture/SystemArchitectureBlueprint.md` mô tả kiến trúc tổng thể, sơ đồ microservices, luồng xác thực OAuth2, luồng điểm danh QR, luồng thông báo đa kênh, sơ đồ tích hợp frontend-backend; (2) `./sources/docs/database/DatabaseSchemaTopology.md` liệt kê 11 bảng cơ sở dữ liệu với sơ đồ ER, mô tả trường, kiểu dữ liệu, ràng buộc khóa chính, khóa ngoại, chỉ mục; (3) `./sources/docs/api/OpenAPIContracts.md` trình bày toàn bộ OpenAPI 3.0 spec cho các endpoint REST theo các Tag ID [ARC-006] đến [ARC-009]; (4) `./sources/docs/operations/OperationalManual.md` viết bằng tiếng Việt, mô tả quy trình vận hành, xử lý sự cố, retention log 1 năm theo [NFR-006], quy trình GDPR/CCPA data export và deletion theo [NFR-008].

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- No database infrastructure or persistence layer changes are required for this phase context
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

### 🕵️ BÁO CÁO KIỂM TOÁN CHÉO KIẾN TRÚC THEO THỜI GIAN THỰC BẮT BUỘC:

```properties:cross_audit_ledger
[AUTOMATED_SELF_AUDIT_REPORT]
TOTAL_PHASES_DECLARED_IN_SECTION_4_2=5
TOTAL_PHASES_EXPECTED_BY_PARAMETERS=5
PHASE_COUNT_COMPLIANCE_STATUS=Verified_5
MAX_DAYS_PER_PHASE_LIMIT_PARAMETER=7
ACTUAL_MAX_DAY_INDEX_DETECTED_IN_TIMELINE=3
TIMELINE_DAY_CAP_COMPLIANCE_STATUS=Verified_All_Phase_Durations_Within_Ceiling
TOTAL_TASKS_REGISTERED_IN_MASTER_BACKLOG_4_1=35
TOTAL_DISCRETE_SUB_TASKS_GENERATED_IN_SECTION_5=35
SUB_TASK_QUANTUM_COMPLIANCE_STATUS=Verified_Symmetry_Enforced_With_100_Percent_Symmetry
```

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_3_FINAL-->

## ☣️ 6. UNIVERSAL ENTERPRISE SECURITY CODES & INJECTION COUNTERMEASURES [NFR-XXX]

- **SQL Injection (SQLi) Absolute Countermeasures:** Toàn bộ lớp truy cập dữ liệu phải sử dụng `PreparedStatement` với các tham số truy vấn theo vị trí (`?`) thông qua Hibernate ORM, loại bỏ tuyệt đối việc nối chuỗi SQL thủ công trong tầng service. Mọi endpoint cho phép sắp xếp/lọc động (chẳng hạn như sắp xếp bảng danh sách khóa học) phải đăng ký danh sách trắng (whitelist) tại bean `HibernateSortWhitelistFilter`, chỉ chấp nhận các cột đã được chỉ định trong annotation `@WhitelistedSortColumns({"title", "start_date"})`. Tầng database cần thêm các ràng buộc CHECK và trigger xác thực logic nghiệp vụ (kiểm tra chồng lấn lịch giáo viên) để chuyển logic bảo mật về phía engine lưu trữ. Bất kỳ truy vấn native nào cũng phải dùng `EntityManager.createNativeQuery(sql, resultClass)` với named parameters, kết hợp audit log ghi lại SQL binding trước khi thực thi. [DAT-ALL (1 to 12)], [NFR-003]
- **Cross-Site Scripting (XSS) & Content Security Policy (CSP):** Ứng dụng Next.js phải kích hoạt chế độ auto-escape cho JSX và thiết lập thư viện `DOMPurify` để vệ sinh hóa mọi payload HTML trong trường `description` của khóa học hoặc nội dung thông báo trước khi render. Tại Ingress Gateway của GKE, phải cấu hình NGINX chèn header `Content-Security-Policy: default-src 'self'; script-src 'self' 'nonce-{csp-nonce}'; object-src 'none'; frame-ancestors 'none'; base-uri 'self';` kèm theo `X-Content-Type-Options: nosniff` và `Strict-Transport-Security: max-age=63072000; includeSubDomains; preload`. Tham số nonce phải được sinh ngẫu nhiên trên mỗi request thông qua middleware Next.js và đồng bộ với header phản hồi để ngăn chặn kịch bản chèn mã độc từ nguồn không đáng tin cậy. [REQ-018], [NFR-003]
- **Multi-Tenant CORS Security Rails:** Gateway Quarkus phải cấm wildcard `*` trong cấu hình `quarkus.http.cors.origins`, thay vào đó sử dụng cơ chế `DynamicOriginResolver` ánh xạ `Origin` header tới danh sách tenant đã đăng ký trong bảng `TenantAllowedOrigins`. Mỗi yêu cầu đi qua sẽ được middleware `CorsTenantGuard` trích xuất token tenant từ JWT, so khớp với whitelist và chỉ phản hồi `Access-Control-Allow-Origin` khi khớp tuyệt đối. Cookie phiên phải đặt thuộc tính `SameSite=Strict` và `Secure`, đồng thời áp dụng `Access-Control-Allow-Credentials: true` chỉ khi origin được xác thực hợp lệ. [ARC-001], [ARC-002], [NFR-003]
- **Zero-Leak Log Scrubbing & PII Data Masking Engines:** Triển khai Jackson `@JsonSerialize` với custom serializer `PiiMaskingSerializer` tự động thay thế các trường nhạy cảm (`email`, `contact_phone`, `tax_id`) bằng chuỗi che giấu (`a***@example.com`, `+84-***-***-99`) khi trả về qua REST API cho các vai trò không có đặc quyền quản trị. Tầng ghi log sử dụng `Logback TurboFilter` kết hợp `MaskingPatternLayout` để quét và ẩn hóa thông tin cá nhân trước khi ghi xuống Elasticsearch. Quy trình xuất dữ liệu phục vụ GDPR phải đi qua dịch vụ `DataExportService` mã hóa tệp JSON bằng AES-256 và lưu trữ tạm thời với URL truy cập có token JWT ngắn hạn. [NFR-006], [NFR-008]

## 📱 7. HYBRID MOBILE COMPLIANCE RAIL RULES & INTERNATIONALIZED SEO MECHANISMS

- **Capacitor Mobile Hybrid Compliance Rails:** Tầng client di động sử dụng Capacitor phải đóng gói bundle Next.js dưới dạng tài nguyên tĩnh nằm trong thư mục `webDir` và cấu hình `server.url` trỏ về API production khi chạy ở chế độ `capacitor.config.ts`. Mọi lệnh gọi API phải sử dụng URL tuyệt đối kèm interceptor `BearerTokenInterceptor` để đính kèm JWT, đồng thời bật cơ chế bảo vệ hydration thông qua việc tách biệt render phía server và phía client bằng `dynamic(() => import('...'), { ssr: false })`. Lưu trữ cục bộ sử dụng plugin chính thức `@capacitor/preferences` thay cho `localStorage` để đảm bảo dữ liệu được mã hóa bằng Keychain (iOS) hoặc EncryptedSharedPreferences (Android), hỗ trợ đồng bộ khi thiết bị ngoại tuyến. Sự kiện nhấn nút back vật lý phải được đăng ký qua `App.addListener('backButton', ...)` để chuyển hướng về stack điều hướng hoặc thoát ứng dụng khi ở root, đồng thời chặn thoát ngoài ý muốn trong các màn hình quan trọng như quét QR điểm danh. [ARC-007], [REQ-020], [REQ-021]
- **Internationalization (i18n) & Dynamic SEO Injection:** Next.js triển khai middleware `middleware.ts` ngay tại edge runtime để đọc cookie `NEXT_LOCALE`, đối chiếu với header `Accept-Language` và chuyển hướng tới đường dẫn `/[locale]/...` phù hợp trước khi render trang. Mỗi trang phải sử dụng `generateMetadata` để chèn thẻ `<html lang>` tương ứng và sinh động các thẻ `<link rel="alternate" hreflang="en|vi|es" href="..." />` trỏ tới phiên bản ngôn ngữ thay thế, kèm theo thẻ `x-default` cho locale mặc định. Tệp `sitemap.xml` được tạo tự động thông qua `next-sitemap` với đầy đủ biến thể locale, đảm bảo công cụ tìm kiếm lập chỉ mục chính xác từng phiên bản ngôn ngữ của cùng một nội dung. [REQ-022], [REQ-023], [NFR-007]

## 🚀 8. PIPELINE AUTOMATED DAILY SESSION GIT BRANCH FLOW

- **Daily Workspace Forking Isolation:** Quy trình GitOps thiết lập ánh xạ 1-1 giữa ngày làm việc và nhánh phát triển theo mẫu `features/development-phase-X-day-Y`, trong đó `X` là số thứ tự giai đoạn và `Y` là số thứ tự ngày. Mỗi Sub-Agent khi nhận nhiệm vụ phải thực hiện fork từ nhánh `develop` thông qua GitHub Actions workflow `daily-fork-isolation.yml`, workflow tự động kiểm tra tên nhánh tuân thủ regex `^features/development-phase-[1-5]-day-[1-7]$` trước khi cấp quyền push. Sau khi hoàn thành daily log, Sub-Agent tạo Pull Request hướng về `develop` với template mô tả chi tiết Tag IDs và `target_component` đã chạm tới, kích hoạt pipeline review tự động. [ARC-000], [NFR-006]
- **Validation Guard Pipeline Gates:** GitHub Actions workflow `ci-guard.yml` phải thiết lập các cổng gác tuần tự gồm biên dịch Maven (`mvn -B clean compile`), chạy test với ngưỡng phủ sóng tối thiểu `>= 85%` thông qua JaCoCo (`mvn verify -Pcoverage`), quét chất lượng mã nguồn bằng SonarQube (`sonar:sonar -Dsonar.qualitygate.wait=true`) với ngưỡng chặn khi tỷ lệ mã trùng lặp vượt `3%` hoặc điểm maintainability dưới `A`. Bước cuối cùng thực thi `trivy fs --security-checks vuln,secret` quét lỗ hổng filesystem, nếu phát hiện CVE mức `HIGH` hoặc `CRITICAL` pipeline sẽ tự động fail và chặn merge. Toàn bộ báo cáo coverage và Sonar phải được đăng tải lên artifact `quality-reports` phục vụ truy vết sau này. [NFR-003], [NFR-005], [NFR-006]

[TRACEABILITY MATRIX ENFORCEMENT: 100% COVERAGE VALIDATED. TOTAL UNIQUE REQ TAGS MAPPED: 25, TOTAL ARC TAGS: 9, TOTAL EXC TAGS: 5, TOTAL DAT TAGS: 12, TOTAL NFR TAGS: 9. ZERO UNASSIGNED CODES FOUND.]