<!--START_CHUNK_PART_1_INITIAL-->

# BỐI CẢNH DỰ ÁN TOÀN CỤC: membership-hub

## 📊 Kiểm Soát Tài Liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Bản Thiết Kế** | ARCH-20260829223421 |
| **Tên Dự Án** | membership-hub |
| **Phiên Bản** | 1.0 (Đường cơ sở) |
| **Ngày Giờ** | 2026/08/29 22:34:21 |
| **Tác Giả** | Kiến Trúc Sư Hệ Thống Doanh Nghiệp (SA Agent) |
| **Phê Duyệt** | Đang chờ Rà Soát Quản Trị Kỹ Thuật |

## 📊 1. TỔNG QUAN HỆ THỐNG & MÔ HÌNH KIẾN TRÚC CỐT LÕI

### ⚙️ 1.1. Phương Thức Lõi & Mô Hình Kiến Trúc
- Nền tảng quản lý thành viên đa trung tâm, hỗ trợ điểm danh QR thời gian thực [ARC-007].
- Kiến trúc vi dịch vụ phân tách theo miền nghiệp vụ: user-service, center-service, course-service, enrollment-service, attendance-service, card-service, notification-service, promotion-service, reporting-service [ARC-000].
- Xác thực lai OAuth2 + JWT: hỗ trợ local email/password, Firebase, Google, Facebook [ARC-006], [REQ-001], [REQ-002].
- Mô hình phản ứng sự kiện (EDA) với Apache Kafka cho các luồng notification, attendance, enrollment [ARC-008], [REQ-016], [REQ-021].
- Cổng API Gateway định tuyến tập trung với rate limiting, JWT validation, audit logging [NFR-001], [NFR-006].
- Phân tách CQRS cho reporting-service: lệnh ghi đồng bộ, truy vấn phục vụ dashboard qua PostgreSQL read replica [NFR-004], [REQ-025].
- Quarkus 3.15 LTS làm runtime chính cho backend, tối ưu GraalVM native image [NFR-005].
- Hibernate ORM với Panache, xác thực dữ liệu chặt chẽ qua Bean Validation 3.0 [REQ-001]–[REQ-018].
- Giao tiếp liên service đồng bộ REST + bất đồng bộ Kafka topic theo mô hình Outbox [NFR-003].

### 🌊 1.2. Topologies Luồng Dữ Liệu & Hệ Sinh Thái Lõi
- Kafka topic `attendance.scan.requested` ingest payload QR từ mobile app, đảm bảo idempotency [REQ-012], [REQ-013], [EXC-001], [EXC-002].
- Kafka topic `notification.outbound` fan-out tới FCM/APNs gateway và Zalo OA webhook [REQ-016], [REQ-021], [EXC-003].
- Kafka topic `enrollment.registered` kích hoạt cấp quyền truy cập, tạo thẻ thành viên tự động, đẩy thông báo [REQ-011], [REQ-014].
- REST API Gateway expose OpenAPI 3.1 cho Next.js frontend, hỗ trợ offline cache và bearer token [ARC-009], [REQ-020].
- Webhook ra ngoài: Zalo OA, payment gateway cho gia hạn thẻ [REQ-015].
- Redis cache cho session, locale, dashboard counters với TTL cấu hình [NFR-001], [NFR-007].
- PostgreSQL primary + read replica (cross-region) phục vụ workload báo cáo [NFR-004], [NFR-009].
- Flyway migration quản lý schema, partition theo `attendance_date` cho tải lớn [NFR-001].
- HPA Kubernetes scale theo CPU > 70% hoặc latency P95 > 300ms [NFR-004].

## 📁 2. TECH STACK DEPENDENCIES & ECOSYSTEM LIBRARIES
- **Backend Infrastructure Core Stack:** Quarkus 3.15.1 (RESTEasy Reactive, Hibernate Reactive + Panache, SmallRye Reactive Messaging Kafka, OpenID Connect/OAuth2, Quarkus Cache, Quarkus Scheduler, SmallRye Health, SmallRye JWT, Quarkus Flyway, Quarkus Micrometer, Hibernate Validator 8.0, BCrypt qua Bouncy Castle).
- **Frontend & Cross-Platform UI Mobile Stack:** Next.js 14.2.15 (App Router, Server Components, i18next đa ngôn ngữ en/vi/es, TailwindCSS, React Query, Zod validation, next-intl cho hreflang SEO), React Native 0.75.4 (Expo SDK 51, React Navigation, Reanimated, AsyncStorage, FCM SDK, APNs SDK, react-native-vision-camera QR scanner, Zustand state), TypeScript 5.5, Vite 5.4 dành cho web app con, Vitest 2.0.

## 📁 3. GLOBAL GUARDRAILS & ENTERPRISE COMPLIANCE STANDARDS

### 🔑 3.1. Security & Compliance Baseline
- TLS 1.3 bắt buộc cho toàn bộ traffic in-transit, mã hóa AES-256 tại rest [NFR-003].
- JWT access token 15 phút, refresh token 7 ngày, rotation và blacklist qua Redis [ARC-006], [NFR-003].
- OWASP Top 10 mitigations: prepared statements (Hibernate), output encoding, CSRF token cho Next.js, CSP headers [NFR-003].
- Bcrypt cost factor 12 lưu `password_hash`, OAuth2 state + PKCE cho social login [REQ-001], [REQ-002].
- Audit log ghi lại role change, attendance, notification; lưu trữ 1 năm [NFR-006].
- GDPR/CCPA: quy trình xóa dữ liệu theo yêu cầu, export JSON, consent management marketing [NFR-008].
- Phân tách tenant theo `center_id` cho Center Admin, isolation bằng row-level security trong PostgreSQL [ARC-002], [ARC-003].
- Hash chain cho audit log chống tamper, log shipping về Cloud Logging [NFR-006].

### 🌐 3.2. Infrastructure & Performance Guardrails
- API lõi (auth, attendance, course list) phải đạt P95 < 200ms [NFR-001].
- Hỗ trợ 10.000 concurrent user, query dưới 1 giây nhờ index B-tree và partial index [NFR-001].
- HPA scale khi CPU > 70% hoặc latency > 300ms; min 2 pod, max 20 pod [NFR-004].
- PostgreSQL connection pool HikariCP size 30, connection timeout 3s, idle timeout 600s.
- Redis cache eviction LRU, TTL mặc định 300s, namespace theo service.
- Kafka partition `attendance.scan.requested` = 12, `notification.outbound` = 6, retention 7 ngày.
- Backup PostgreSQL full hằng ngày, PITR 24h, replica region `asia-southeast2` [NFR-009].
- Giới hạn kích thước Docker image: base < 200MB, cuối cùng < 500MB [NFR-005].
- SLA 99.9% uptime với multi-zone GKE cluster và failover tự động [NFR-002], [NFR-004].

### 🥞 3.3. ARCHITECTURAL STACK MATRIX
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

### 📦 4.1. DANH SÁCH TỔNG THỂ CÁC HẠNG MỤC SẢN PHẨM KIẾN TRÚC

Bản đồ tổng thể hạng mục sản phẩm (Master Product Tasks Backlog) dưới đây mô tả toàn bộ các khối công việc kỹ thuật phân tán cần thiết để hiện thực hóa nền tảng Membership Hub — hệ thống microservices quản lý thành viên đa trung tâm. Bốn dịch vụ backend cốt lõi (`user-service`, `center-service`, `course-service`, `attendance-service`) giao tiếp theo mô hình REST + Kafka, sử dụng cơ sở dữ liệu PostgreSQL phân vùng theo nghiệp vụ, với tầng xác thực tập trung (JWT/OAuth2). Tầng frontend Next.js tiêu thụ API REST và hỗ trợ SEO đa ngôn ngữ. Hạ tầng DevOps triển khai qua GKE với container hóa Docker, tự động hóa Terraform cho GCP và manifest Kubernetes cho GKE. Tất cả mọi token truy vết (`[REQ-XXX]`, `[EXC-XXX]`, `[DAT-XXX]`, `[ARC-XXX]`, `[NFR-XXX]`, `[DOC-XXX]`) đều được gán chính xác từng dòng để đảm bảo khả năng truy nguyên 100% từ yêu cầu nghiệp vụ thô đến triển khai thực tế.

#### [MA TRẬN SỐ HỌC HỆ THỐNG]
> - **Tổng số thẻ [REQ]:** 25 Thẻ
> - **Tổng số thẻ [EXC]:** 5 Thẻ
> - **Tổng số thẻ [ARC]:** 9 Thẻ
> - **Tổng số thẻ [DAT]:** 12 Thẻ
> - **Tổng số thẻ [NFR]:** 9 Thẻ
> - ➡️ **Tổng số thẻ SRS:** 60 Thẻ

<!--BACKLOG_SYNOPSIS_GRID_START-->

| No. | Task | Technical Purpose / Deliverables Summary | Type | TagID |
| :--- | :--- | :--- | :--- | :--- |
| 1 | Scaffolding & Build Descriptors cho toàn bộ hệ thống | Sinh mô tả build root `./sources/backend/pom.xml` cha Multi-Module Maven, các mô tả build con cho 4 dịch vụ (`user-service`, `center-service`, `course-service`, `attendance-service`), mô tả `package.json` và `tsconfig.json` cho frontend Next.js theo quy ước gói `org.nlh4j.membershiphub`. | Application Code | [ARC-000] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 2 | Endpoint đăng ký người dùng mới | Triển khai REST POST `/api/v1/users/register` với xác thực email, mật khẩu mạnh, đồng ý điều khoản; sinh bản ghi user với role mặc định Student/Teacher, cấp JWT; tích hợp bean validation Jakarta. | Application Code | [REQ-001] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 3 | Xác thực Social OAuth2 (Firebase/Google/Facebook) | Triển khai REST POST `/api/v1/auth/social` tiếp nhận provider token, quy đổi OAuth2 code sang thông tin user, đồng bộ bản ghi local, cấp JWT. | Application Code | [REQ-002] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 4 | Gán và thay đổi vai trò người dùng (RBAC) | Triển khai REST PUT `/api/v1/users/{id}/role` cho phép Admin cập nhật role, ghi audit log, kích hoạt lại phiên bảo mật; tích hợp phân quyền theo `[ARC-001]`, `[ARC-002]`, `[ARC-003]`, `[ARC-004]`, `[ARC-005]`. | Application Code | [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 5 | Danh sách trung tâm cho mọi người dùng đã xác thực | Triển khai REST GET `/api/v1/centers` trả về trang dữ liệu gồm Name, Address, TaxID, AdminContact; bảo vệ route bằng JWT middleware. | Application Code | [REQ-004] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 6 | Quản lý CRUD trung tâm (System Admin) | Triển khai REST POST `/api/v1/centers`, PUT `/api/v1/centers/{id}`, DELETE `/api/v1/centers/{id}` với kiểm tra trùng lặp TaxID; áp dụng validation Bean. | Application Code | [REQ-005] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 7 | Gán/huỷ gán Center Admin cho trung tâm | Triển khai REST POST `/api/v1/centers/{id}/admins` và DELETE tương ứng, cập nhật role thành Center Admin và ghi nhận center_id; đảm bảo phân quyền `[ARC-002]`. | Application Code | [REQ-006], [ARC-002] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 8 | Danh sách khoá học cho người dùng xác thực | Triển khai REST GET `/api/v1/courses` trả về CourseID, Title, StartDate, EndDate, TeacherName; hỗ trợ phân trang. | Application Code | [REQ-007] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 9 | CRUD khoá học và kiểm tra xung đột lịch | Triển khai REST POST/PUT/DELETE `/api/v1/courses` với logic overlap check dựa trên teacher_id và khoảng ngày; dùng ràng buộc exclusion tại DB. | Application Code | [REQ-008] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 10 | Gán/huỷ gán giáo viên cho khoá học | Triển khai REST POST `/api/v1/courses/{id}/teachers` và DELETE; đẩy sự kiện Kafka `teacher-assigned` để gửi push notification. | Application Code | [REQ-009] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 11 | Duyệt khoá học cho sinh viên (loại trừ đã đăng ký) | Triển khai REST GET `/api/v1/students/courses/available` với filter enrollment tồn tại; trả capacity và schedule. | Application Code | [REQ-010] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 12 | Đăng ký khoá học cho sinh viên | Triển khai REST POST `/api/v1/enrollments` tự sinh tài khoản Student nếu chưa có, tạo enrollment, đẩy Kafka `enrollment-created` cho notification + Zalo. | Application Code | [REQ-011] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 13 | Ghi nhận điểm danh QR cho sinh viên | Triển khai REST POST `/api/v1/attendance/scan` giải mã payload base64 (studentID, courseID), kiểm tra enrollment, tạo bản ghi attendance idempotent. | Application Code | [REQ-012], [REQ-013] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 14 | Xem thẻ thành viên và số ngày còn lại | Triển khai REST GET `/api/v1/students/{id}/card` tính remaining_days, used_days, total_days. | Application Code | [REQ-014] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 15 | Gia hạn thẻ thành viên | Triển khai REST POST `/api/v1/students/{id}/card/renew` với renewal_days (1-365), cập nhật EndDate, đẩy thông báo xác nhận. | Application Code | [REQ-015] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 16 | Kích hoạt thông báo đa kênh (Push + Zalo) | Triển khai REST POST `/api/v1/notifications` đẩy sự kiện Kafka `notification-queue` tiêu thụ bởi worker FCM/APNs và Zalo bot; hỗ trợ retry 3 lần `[EXC-003]`. | Application Code | [REQ-016], [REQ-021], [EXC-003] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 17 | Quản lý khuyến mãi (CRUD) | Triển khai REST CRUD `/api/v1/promotions` với StartDate/EndDate tuỳ chọn; hỗ trợ perpetual promotion khi không có EndDate. | Application Code | [REQ-017] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 18 | Quản lý thông báo chung (CRUD) | Triển khai REST CRUD `/api/v1/announcements` với auto-hide khi quá expiry; phát broadcast toàn site. | Application Code | [REQ-018] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 19 | Tích hợp AI Chatbot tư vấn | Triển khai REST POST `/api/v1/chatbot/query` gọi Vertex AI/Gemini; cơ chế escalate khi độ tin cậy thấp. | Application Code | [REQ-019] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 20 | Giao diện responsive cho ứng dụng di động (Next.js) | Sinh layout điều hướng theo role, component NativeWind, màn hình học viên/giáo viên/quản trị; tích hợp React Native Web wrapper. | Application Code | [REQ-020] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 21 | Đẩy Push Notification qua FCM/APNs | Triển khai REST POST `/api/v1/devices/register` lưu device token; worker gửi push qua FCM/APNs với retry theo `[EXC-003]`. | Application Code | [REQ-021] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 22 | Phát hiện ngôn ngữ mặc định cho khách truy cập | Triển khai middleware Next.js đọc cookie, fallback Accept-Language; tích hợp `next-intl` cho i18n. | Application Code | [REQ-022] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 23 | SEO đa ngôn ngữ (en, vi, es) | Sinh trang động với `<html lang>` và thẻ `hreflang` cho từng locale; sitemap đa ngôn ngữ. | Application Code | [REQ-023] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 24 | Xuất báo cáo điểm danh CSV | Triển khai REST GET `/api/v1/reports/attendance` xuất CSV với StudentName, CourseName, AttendanceDate, Status; giới hạn 30 ngày. | Application Code | [REQ-024] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 25 | Dashboard tổng hợp tuyển sinh thời gian thực | Triển khai REST GET `/api/v1/dashboard/enrollment-summary` cache 15 phút; cards totalStudents, activeCourses, upcomingSessions. | Application Code | [REQ-025] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 26 | Khởi tạo & di trú Database Schema tổng thể | Sinh script Flyway V1__init.sql cho 12 bảng nghiệp vụ (Users, Centers, Courses, Enrollments, Attendance, StudentCards, Notifications, Roles, Promotions, Announcements, SystemSettings, AuditLogs), index, ràng buộc FK và composite unique `(student_id, course_id, attendance_date)`. | Application Code | [DAT-ALL (1 to 12)] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 27 | Luồng xác thực và phát hành JWT/Refresh | Cấu hình OAuth2 Resource Server với JWT 15 phút và refresh token 7 ngày, hỗ trợ email/password + 3 social provider, ghi log kiểm toán; tuân thủ `[NFR-003]`. | Application Code | [ARC-006], [NFR-003] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 28 | Luồng xử lý điểm danh QR đầu cuối | Sinh REST endpoint chuẩn + idempotency key + cơ chế retry sau khi mất mạng `[EXC-001]`; tích hợp FIFO khi khôi phục `[EXC-005]`. | Application Code | [ARC-007], [EXC-001], [EXC-002], [EXC-005] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 29 | Hợp đồng tích hợp Notification đa kênh | Thiết kế schema Kafka topic `notification-queue` với payload chuẩn JSON, đảm bảo phân phối tới mobile + Zalo; xử lý retry & dead-letter theo `[EXC-003]`. | Application Code | [ARC-008], [EXC-003] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 30 | Tích hợp Mobile App với Backend qua REST | Định nghĩa OpenAPI 3.1, bearer token auth, hỗ trợ offline cache (Service Worker + IndexedDB) trong frontend Next.js. | Application Code | [ARC-009] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 31 | Hạ tầng DevOps: Docker + Terraform GCP + GKE Manifest | Sinh Dockerfile đa giai đoạn (image <500MB), script Terraform cung cấp VPC, GKE Autopilot, Cloud SQL PostgreSQL, IAM, KMS, manifest GKE HPA theo CPU/latency. | DevOps Infrastructure | [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 32 | Tài liệu kiến trúc & vận hành doanh nghiệp | Sinh bộ tài liệu: kiến trúc tổng thể, sơ đồ C4, tài liệu API (OpenAPI), tài liệu DB, hướng dẫn DevOps, GDPR/CCPA, quy trình SEO/i18n, đặt tại `./sources/docs/`. | Enterprise Documentation | [DOC-001] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| **SUMMARY** | **Tổng số thẻ truy vết đã che phủ:** 60 | **Tổng số nhiệm vụ:** 32 | **Trạng thái:** ĐÃ XÁC MINH | **Mức độ bao phủ:** 100% |

<!--BACKLOG_SYNOPSIS_GRID_END-->

<!--END_CHUNK_PART_1_BACKLOG_4_1-->

<!--START_CHUNK_PART_1_MATRIX_4_2-->

### 🔭 4.2. MA TRẬN TỔNG HỢP ĐA GIAI ĐOẠN

#### [VÒNG ĐỜI SỐ HỌC MA TRẬN]
> - **Tổng số hạng mục Backlog:** 32 Nhiệm vụ
> - **Tổng số thẻ Backlog:** 60 Thẻ
> - **Tổng số nhiệm vụ đã phân bổ:** 32 Nhiệm vụ
> - **Tổng số thẻ đã phân bổ:** 60 Thẻ

<!--PHASE_SYNOPSIS_GRID_START-->

| Phase | Khoảng Ngày | Mã Nhiệm Vụ Được Bao Phủ | Cấu Phần Kiến Trúc / Đường Dẫn Module | Tóm Tắt Sản Phẩm Kỹ Thuật Bàn Giao | Sub-Agent Được Phân Công | Mã Thẻ Truy Vết |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Giai đoạn 1 | Ngày 1 - 3 | Nhiệm vụ 1, Nhiệm vụ 26, Nhiệm vụ 27 | `./sources/backend/`, `./sources/backend/user-service/`, `./sources/backend/center-service/`, `./sources/backend/course-service/`, `./sources/backend/attendance-service/`, `./sources/frontend/` | Khởi tạo scaffolding root Maven multi-module cho 4 microservices backend với quy ước gói `org.nlh4j.membershiphub`, đồng thời sinh mô tả `package.json` và `tsconfig.json` cho frontend Next.js. Triển khai script di trú Flyway V1__init.sql cho 12 bảng nghiệp vụ với ràng buộc FK, index và composite unique `(student_id, course_id, attendance_date)`. Cấu hình OAuth2 Resource Server với JWT 15 phút và refresh token 7 ngày, tích hợp email/password và 3 social provider, ghi log kiểm toán tuân thủ bảo mật OWASP. | Coder, Tester, Reviewer, Doc | [ARC-000], [ARC-006], [DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [DAT-012], [NFR-003] <!--REGISTERED_PHASE_ROW--> |
| Giai đoạn 2 | Ngày 1 - 7 | Nhiệm vụ 2, Nhiệm vụ 3, Nhiệm vụ 4, Nhiệm vụ 5, Nhiệm vụ 6, Nhiệm vụ 7, Nhiệm vụ 8 | `./sources/backend/user-service/`, `./sources/backend/center-service/` | Phát triển REST endpoint đăng ký người dùng mới với xác thực email, mật khẩu mạnh và bean validation Jakarta. Tích hợp xác thực Social OAuth2 qua Firebase/Google/Facebook. Hiện thực hoá cơ chế gán và thay đổi vai trò người dùng theo ma trận RBAC 5 cấp độ với audit log. Xây dựng REST API danh sách trung tâm có phân trang, REST CRUD trung tâm cho System Admin với kiểm tra trùng lặp TaxID, và endpoint gán/huỷ gán Center Admin với cập nhật role và ghi nhận center_id. | Coder, Tester, Reviewer, Doc | [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [EXC-004] <!--REGISTERED_PHASE_ROW--> |
| Giai đoạn 3 | Ngày 1 - 7 | Nhiệm vụ 9, Nhiệm vụ 10, Nhiệm vụ 11, Nhiệm vụ 12, Nhiệm vụ 13, Nhiệm vụ 28 | `./sources/backend/course-service/`, `./sources/backend/attendance-service/` | Phát triển REST API danh sách khoá học, CRUD khoá học với logic overlap check dựa trên teacher_id và khoảng ngày sử dụng ràng buộc exclusion tại DB. Hiện thực hoá endpoint gán/huỷ gán giáo viên cho khoá học kèm đẩy sự kiện Kafka `teacher-assigned`. Xây dựng endpoint duyệt khoá học cho sinh viên với filter loại trừ enrollment tồn tại. Triển khai endpoint đăng ký khoá học tự sinh tài khoản Student và đẩy Kafka `enrollment-created`. Xây dựng REST POST điểm danh QR với giải mã payload base64, kiểm tra enrollment, idempotency key, cơ chế retry sau mất mạng và FIFO khi khôi phục. | Coder, Tester, Reviewer, Doc | [REQ-007], [REQ-008], [REQ-009], [REQ-010], [REQ-011], [REQ-012], [REQ-013], [ARC-007], [EXC-001], [EXC-002], [EXC-005] <!--REGISTERED_PHASE_ROW--> |
| Giai đoạn 4 | Ngày 1 - 7 | Nhiệm vụ 14, Nhiệm vụ 15, Nhiệm vụ 16, Nhiệm vụ 17, Nhiệm vụ 18, Nhiệm vụ 19, Nhiệm vụ 29, Nhiệm vụ 30 | `./sources/backend/user-service/`, `./sources/backend/center-service/`, `./sources/backend/course-service/`, `./sources/backend/attendance-service/`, `./sources/frontend/` | Triển khai REST endpoint xem thẻ thành viên với tính toán remaining_days, used_days, total_days. Hiện thực hoá endpoint gia hạn thẻ với renewal_days 1-365 và cập nhật EndDate. Xây dựng REST endpoint kích hoạt thông báo đa kênh đẩy sự kiện Kafka `notification-queue` cho worker FCM/APNs và Zalo bot với retry 3 lần. Phát triển REST CRUD khuyến mãi với StartDate/EndDate tuỳ chọn, REST CRUD thông báo chung với auto-hide khi quá expiry, REST tích hợp AI Chatbot gọi Vertex AI/Gemini. Hoàn thiện hợp đồng tích hợp Notification đa kênh, OpenAPI 3.1 cho mobile app, bearer token auth, offline cache với Service Worker và IndexedDB trong frontend Next.js. | Coder, Tester, Reviewer, Doc | [REQ-014], [REQ-015], [REQ-016], [REQ-017], [REQ-018], [REQ-019], [REQ-021], [ARC-008], [ARC-009], [EXC-003] <!--REGISTERED_PHASE_ROW--> |
| Giai đoạn 5 | Ngày 1 - 5 | Nhiệm vụ 20, Nhiệm vụ 22, Nhiệm vụ 23, Nhiệm vụ 24, Nhiệm vụ 25, Nhiệm vụ 31, Nhiệm vụ 32 | `./sources/frontend/`, `./sources/infra/`, `./sources/docs/` | Sinh giao diện responsive theo role với NativeWind, màn hình học viên/giáo viên/quản trị và React Native Web wrapper. Tích hợp `next-intl` cho phát hiện ngôn ngữ qua cookie và Accept-Language, SEO đa ngôn ngữ với thẻ `hreflang` và sitemap. Triển khai REST endpoint xuất báo cáo CSV giới hạn 30 ngày, dashboard tuyển sinh thời gian thực với cache 15 phút. Sinh Dockerfile đa giai đoạn image dưới 500MB, script Terraform cung cấp VPC, GKE Autopilot, Cloud SQL PostgreSQL, IAM, KMS, manifest GKE HPA theo CPU/latency. Hoàn thiện bộ tài liệu kiến trúc, sơ đồ C4, OpenAPI, GDPR/CCPA, SEO/i18n đặt tại `./sources/docs/`. | Coder, Tester, Reviewer, Doc, Docker, GCP, GKE | [REQ-020], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [NFR-001], [NFR-002], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009], [DOC-001] <!--REGISTERED_PHASE_ROW--> |
| **Kiểm Định** | **Xác Minh Phân Bổ Backlog Tổng Thể** | **Tổng Số Giai Đoạn:** 5 | **Tổng Số Thẻ Backlog:** 60 | **Tổng Số Thẻ Đã Phân Bổ:** 60 | **Tổng Số Nhiệm Vụ Đã Phân Bổ:** 32 | **Trạng Thái & Tuân Thủ:** Đã Xác Minh (100%) |

<!--PHASE_SYNOPSIS_GRID_END-->

<!--END_CHUNK_PART_1_MATRIX_4_2-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

## 🔬 5. CHI TIẾT CHUYÊN SÂU TỪNG GIAI ĐOẠN & SỔ NHẬT KÝ CÔNG VIỆC THEO NGÀY

### 📈 Giai đoạn 1 - Khởi Tạo Nền Tảng & Cơ Sở Dữ Liệu
- **Mục Tiêu Cốt Lõi & Ý Nghĩa Giai Đoạn:** Thiết lập bộ khung kiến trúc đa dịch vụ (microservices) từ đầu với quy ước gói `org.nlh4j.membershiphub`, đồng thời xây dựng nền tảng dữ liệu quan hệ chuẩn ANSI SQL cho 12 bảng nghiệp vụ cốt lõi thông qua hệ thống di trú Flyway. Cấu hình máy chủ tài nguyên OAuth2 với cơ chế phát hành JWT 15 phút và refresh token 7 ngày, tích hợp đầy đủ 3 nhà cung cấp danh tính xã hội (Firebase, Google, Facebook), ghi log kiểm toán tuân thủ chuẩn bảo mật OWASP. Mục tiêu chính là đảm bảo 100% mô tả build (`pom.xml`, `package.json`, `tsconfig.json`) có thể biên dịch trắng (blank compilation) ngay sau khi giai đoạn kết thúc.

- **Ma Trận Đường Dẫn Vật Lý Mục Tiêu:** Bản đồ chi tiết 100% tệp tin vật lý được tạo, tinh chỉnh hoặc xử lý trong phạm vi giai đoạn này, tuân thủ nghiêm ngặt quy ước gói `org.nlh4j.membershiphub`:
    * `./sources/backend/pom.xml` — [ARC-000]
    * `./sources/backend/user-service/pom.xml` — [ARC-000]
    * `./sources/backend/center-service/pom.xml` — [ARC-000]
    * `./sources/backend/course-service/pom.xml` — [ARC-000]
    * `./sources/backend/attendance-service/pom.xml` — [ARC-000]
    * `./sources/frontend/package.json` — [ARC-000]
    * `./sources/frontend/tsconfig.json` — [ARC-000]
    * `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_and_roles.sql` — [DAT-001], [DAT-008]
    * `./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql` — [DAT-002]
    * `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql` — [DAT-003]
    * `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql` — [DAT-004], [DAT-005]
    * `./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql` — [DAT-006]
    * `./sources/backend/attendance-service/src/main/resources/db/migration/V2__init_notifications.sql` — [DAT-007]
    * `./sources/backend/center-service/src/main/resources/db/migration/V2__init_promotions.sql` — [DAT-009]
    * `./sources/backend/course-service/src/main/resources/db/migration/V2__init_announcements.sql` — [DAT-010]
    * `./sources/backend/user-service/src/main/resources/db/migration/V3__init_system_settings.sql` — [DAT-011]
    * `./sources/backend/user-service/src/main/resources/db/migration/V3__init_audit_logs.sql` — [DAT-012]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java` — [ARC-006], [NFR-003]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/ResourceServerConfig.java` — [ARC-006], [NFR-003]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistry.java` — [ARC-006], [REQ-002]
    * `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProviderTest.java` — [ARC-006]
    * `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistryTest.java` — [ARC-006], [REQ-002]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/AuthAuditLogger.java` — [NFR-003], [NFR-006]

- **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql:matrix
-- ============================================
-- FILE: V1__init_users_and_roles.sql
-- SCOPE: Users & Roles
-- ============================================
CREATE TABLE roles (
    role_id SMALLINT NOT NULL,
    name VARCHAR(30) NOT NULL,
    description VARCHAR(200),
    CONSTRAINT pk_roles PRIMARY KEY (role_id),
    CONSTRAINT uq_roles_name UNIQUE (name),
    CONSTRAINT ck_roles_name CHECK (name IN ('SYSTEM_ADMIN','CENTER_ADMIN','MANAGER','TEACHER','STUDENT'))
);

CREATE TABLE users (
    user_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash CHAR(60) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role_id SMALLINT NOT NULL,
    provider VARCHAR(20) NOT NULL DEFAULT 'local',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES roles(role_id),
    CONSTRAINT ck_users_provider CHECK (provider IN ('local','firebase','google','facebook'))
);

CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_created_at ON users(created_at);

-- ============================================
-- FILE: V1__init_centers.sql
-- SCOPE: Centers
-- ============================================
CREATE TABLE centers (
    center_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tax_id VARCHAR(20) NOT NULL,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    CONSTRAINT pk_centers PRIMARY KEY (center_id),
    CONSTRAINT uq_centers_tax_id UNIQUE (tax_id)
);

CREATE INDEX idx_centers_name ON centers(name);

-- ============================================
-- FILE: V1__init_courses.sql
-- SCOPE: Courses
-- ============================================
CREATE TABLE courses (
    course_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    teacher_id UUID NOT NULL,
    max_students INT NOT NULL DEFAULT 30,
    center_id UUID NOT NULL,
    CONSTRAINT pk_courses PRIMARY KEY (course_id),
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id),
    CONSTRAINT fk_courses_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT ck_courses_date_range CHECK (end_date >= start_date),
    CONSTRAINT ck_courses_max_students CHECK (max_students > 0)
);

CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_courses_start_date ON courses(start_date);

-- ============================================
-- FILE: V1__init_enrollments_attendance.sql
-- SCOPE: Enrollments & Attendance
-- ============================================
CREATE TABLE enrollments (
    enrollment_id UUID NOT NULL,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    enrollment_date TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_enrollments PRIMARY KEY (enrollment_id),
    CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(course_id)
);

CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);

CREATE TABLE attendance (
    attendance_id UUID NOT NULL,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),
    CONSTRAINT uq_attendance_student_course_date UNIQUE (student_id, course_id, attendance_date),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT fk_attendance_course FOREIGN KEY (course_id) REFERENCES courses(course_id)
);

CREATE INDEX idx_attendance_course_date ON attendance(course_id, attendance_date);
CREATE INDEX idx_attendance_student_date ON attendance(student_id, attendance_date);

-- ============================================
-- FILE: V2__init_student_cards.sql
-- SCOPE: Student Cards
-- ============================================
CREATE TABLE student_cards (
    card_id UUID NOT NULL,
    student_id UUID NOT NULL,
    issue_date DATE NOT NULL,
    validity_days INT NOT NULL,
    remaining_days INT NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT pk_student_cards PRIMARY KEY (card_id),
    CONSTRAINT uq_student_cards_student UNIQUE (student_id),
    CONSTRAINT fk_student_cards_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT ck_student_cards_validity CHECK (validity_days > 0),
    CONSTRAINT ck_student_cards_remaining CHECK (remaining_days >= 0)
);

-- ============================================
-- FILE: V2__init_notifications.sql
-- SCOPE: Notifications
-- ============================================
CREATE TABLE notifications (
    notification_id UUID NOT NULL,
    user_id UUID,
    group_zalo VARCHAR(50),
    message TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    delivered BOOLEAN NOT NULL DEFAULT false,
    retry_count INT NOT NULL DEFAULT 0,
    CONSTRAINT pk_notifications PRIMARY KEY (notification_id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT ck_notifications_target CHECK ((user_id IS NOT NULL) OR (group_zalo IS NOT NULL))
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_sent_at ON notifications(sent_at);

-- ============================================
-- FILE: V2__init_promotions.sql
-- SCOPE: Promotions
-- ============================================
CREATE TABLE promotions (
    promo_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    discount_percent SMALLINT NOT NULL,
    start_date DATE,
    end_date DATE,
    description TEXT,
    center_id UUID NOT NULL,
    CONSTRAINT pk_promotions PRIMARY KEY (promo_id),
    CONSTRAINT uq_promotions_code UNIQUE (code),
    CONSTRAINT fk_promotions_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT ck_promotions_discount CHECK (discount_percent BETWEEN 1 AND 100),
    CONSTRAINT ck_promotions_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

-- ============================================
-- FILE: V2__init_announcements.sql
-- SCOPE: Announcements
-- ============================================
CREATE TABLE announcements (
    announcement_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    start_date DATE,
    end_date DATE,
    center_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_announcements PRIMARY KEY (announcement_id),
    CONSTRAINT fk_announcements_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT ck_announcements_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

-- ============================================
-- FILE: V3__init_system_settings.sql
-- SCOPE: System Settings
-- ============================================
CREATE TABLE system_settings (
    setting_key VARCHAR(50) NOT NULL,
    setting_value TEXT NOT NULL,
    description VARCHAR(200),
    CONSTRAINT pk_system_settings PRIMARY KEY (setting_key)
);

-- ============================================
-- FILE: V3__init_audit_logs.sql
-- SCOPE: Audit Logs
-- ============================================
CREATE TABLE audit_logs (
    log_id UUID NOT NULL,
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    occurred_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_audit_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_occurred_at ON audit_logs(occurred_at);
```

- **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{
  "openapi": "3.1.0",
  "info": {
    "title": "Membership Hub Authentication API",
    "version": "1.0.0",
    "description": "Hợp đồng xác thực tập trung cho 4 microservices backend"
  },
  "components": {
    "securitySchemes": {
      "bearerAuth": {
        "type": "http",
        "scheme": "bearer",
        "bearerFormat": "JWT"
      }
    }
  },
  "paths": {
    "/api/v1/auth/login": {
      "post": {
        "tags": ["Authentication"],
        "summary": "Đăng nhập bằng email và mật khẩu",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": {
                "type": "object",
                "required": ["email", "password"],
                "properties": {
                  "email": {"type": "string", "format": "email", "maxLength": 255},
                  "password": {"type": "string", "minLength": 8}
                }
              }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Cấp JWT access 15 phút + refresh 7 ngày",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "accessToken": {"type": "string"},
                    "refreshToken": {"type": "string"},
                    "expiresIn": {"type": "integer", "example": 900},
                    "tokenType": {"type": "string", "example": "Bearer"}
                  }
                }
              }
            }
          },
          "401": {"description": "Sai thông tin đăng nhập"}
        }
      }
    },
    "/api/v1/auth/refresh": {
      "post": {
        "tags": ["Authentication"],
        "summary": "Làm mới access token bằng refresh token",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": {
                "type": "object",
                "required": ["refreshToken"],
                "properties": {"refreshToken": {"type": "string"}}
              }
            }
          }
        },
        "responses": {
          "200": {"description": "Cấp access token mới"},
          "401": {"description": "Refresh token hết hạn hoặc không hợp lệ"}
        }
      }
    },
    "/api/v1/auth/social": {
      "post": {
        "tags": ["Authentication"],
        "summary": "Xác thực Social OAuth2 (Firebase/Google/Facebook)",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": {
                "type": "object",
                "required": ["provider", "idToken"],
                "properties": {
                  "provider": {"type": "string", "enum": ["firebase", "google", "facebook"]},
                  "idToken": {"type": "string", "description": "OAuth2 ID token từ provider"},
                  "profilePicture": {"type": "string", "format": "uri", "nullable": true}
                }
              }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Xác thực thành công, cấp JWT",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "accessToken": {"type": "string"},
                    "refreshToken": {"type": "string"},
                    "userId": {"type": "string", "format": "uuid"},
                    "role": {"type": "string"}
                  }
                }
              }
            }
          },
          "400": {"description": "Provider không hỗ trợ hoặc idToken không hợp lệ"}
        }
      }
    }
  }
}
```

- **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Giai đoạn này chưa hiện thực hóa logic nghiệp vụ chuyên sâu nên không phát sinh ngoại lệ nghiệp vụ riêng biệt. Toàn bộ ngoại lệ bảo mật và xác thực được xử lý trong lớp bảo mật tập trung (`ResourceServerConfig`) tuân thủ [NFR-003], các ngoại lệ nghiệp vụ [EXC-001] đến [EXC-005] sẽ được đóng gói trong các giai đoạn tiếp theo khi hiện thực hóa endpoint tương ứng.

#### 📅 Sổ Nhật Ký Phân Bổ Nhiệm Vụ Theo Ngày & Sub-Agent (Giai đoạn 1)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: KHỞI TẠO SCAFFOLDING DỰ ÁN & MÔ TẢ BUILD ĐA MODULE

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TẠO MÔ TẢ BUILD ROOT MAVEN ĐA MODULE

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [ARC-000]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/pom.xml`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Khởi tạo tệp tin mô tả build Maven root `pom.xml` tại đường dẫn `./sources/backend/pom.xml` với cấu hình packaging `pom`, khai báo 4 module con là `user-service`, `center-service`, `course-service`, `attendance-service`. Sử dụng Java 17 LTS, Quarkus 3.15.1 (phiên bản ổn định enterprise mới nhất), dependency management tập trung cho Jakarta EE 10, Hibernate ORM Panache 3.15.1, RESTEasy Reactive, Hibernate Validator, Flyway 10.10.0, PostgreSQL JDBC driver 42.7.3, SmallRye JWT 4.10.0, SmallRye Reactive Messaging Kafka 4.10.0, OpenAPI 2.10.0, JUnit 5.10.1, Mockito 5.7.0, REST Assured 5.4.0. Khai báo `<parent>` không tham chiếu, `<groupId>` cố định `org.nlh4j.membershiphub`, `<artifactId>` là `membership-hub-backend`, phiên bản `1.0.0-SNAPSHOT`. Cấu hình plugin `maven-compiler-plugin` 3.13.0 với release 17, `maven-surefire-plugin` 3.2.5, `flyway-maven-plugin` 10.10.0, `quarkus-maven-plugin` 3.15.1, và `jacoco-maven-plugin` 0.8.11 cho báo cáo độ bao phủ mã nguồn. Đảm bảo tệp tin biên dịch trắng ngay khi được tạo mà không phụ thuộc vào module con.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Giai đoạn 1 chỉ khởi tạo mô tả build, chưa tạo DDL
```

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ tạo mô tả build gốc, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: TẠO MÔ TẢ BUILD CHO 4 MICROSERVICES CON

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [ARC-000]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/pom.xml`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo 4 tệp tin mô tả build Maven con cho 4 microservices tại các đường dẫn `./sources/backend/user-service/pom.xml`, `./sources/backend/center-service/pom.xml`, `./sources/backend/course-service/pom.xml`, `./sources/backend/attendance-service/pom.xml`. Mỗi tệp con phải khai báo `<parent>` tham chiếu đến `org.nlh4j.membershiphub:membership-hub-backend:1.0.0-SNAPSHOT`, `<artifactId>` tương ứng (`user-service`, `center-service`, `course-service`, `attendance-service`), packaging `jar`. Mỗi module con kế thừa toàn bộ `dependencyManagement` từ parent và chỉ khai báo các dependency thực sự sử dụng: `quarkus-resteasy-reactive-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-smallrye-openapi`, `quarkus-smallrye-reactive-messaging-kafka`, `quarkus-hibernate-validator`. `user-service` thêm `quarkus-smallrye-jwt-build` cho việc ký token. Dependencies test gồm `quarkus-junit5`, `rest-assured`, `mockito-core`. Đảm bảo tất cả 4 tệp tin biên dịch trắng thông qua lệnh `mvn clean install -DskipTests` tại thư mục root.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Giai đoạn 1 chỉ khởi tạo mô tả build, chưa tạo DDL
```

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ tạo mô tả build cho microservices, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: TẠO MÔ TẢ FRONTEND NEXT.JS VÀ CẤU HÌNH TYPESCRIPT

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [ARC-000]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/package.json`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Khởi tạo tệp tin `package.json` tại `./sources/frontend/package.json` cho ứng dụng Next.js 14.2.5 với App Router, khai báo các dependency: `next@14.2.5`, `react@18.3.1`, `react-dom@18.3.1`, `next-intl@3.17.2` cho đa ngôn ngữ, `tailwindcss@3.4.10` cho styling responsive, `nativewind@4.1.23` cho màn hình mobile, `axios@1.7.4` cho HTTP client, `zustand@4.5.4` cho state management, `react-hook-form@7.53.0` cho form binding, `zod@3.23.8` cho validation, `firebase@10.13.0` cho FCM, `@react-native-firebase/messaging@20.4.0` cho push mobile. Dev dependencies: `typescript@5.5.4`, `@types/react@18.3.3`, `@types/node@20.16.5`, `eslint@8.57.0`, `prettier@3.3.3`. Scripts: `dev`, `build`, `start`, `lint`, `type-check`. Đồng thời tạo tệp `./sources/frontend/tsconfig.json` với `compilerOptions` bật `strict: true`, `target: "ES2022"`, `module: "esnext"`, `moduleResolution: "bundler"`, `jsx: "preserve"`, `incremental: true`, `paths` ánh xạ `@/*` tới `./src/*`, `plugins` cho Next.js, include `src/**/*`, `next-env.d.ts`, exclude `node_modules`. Đảm bảo cả hai tệp tin cấu hình biên dịch trắng qua lệnh `npm install --dry-run`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững trong giai đoạn này
```

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ khởi tạo mô tả frontend, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: KIỂM THỬ TÍCH HỢP BIÊN DỊCH ĐA MODULE

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [ARC-000]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** INTEGRATION_SCOPE;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/MavenBuildIntegrationTest.java
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử tích hợp Maven `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/MavenBuildIntegrationTest.java` với mục đích xác minh rằng toàn bộ cấu trúc đa module Maven gồm `pom.xml` root và 4 `pom.xml` con biên dịch thành công qua lệnh `mvn clean install -DskipTests`. Sử dụng `ProcessBuilder` để thực thi lệnh Maven, kiểm tra `exit code` bằng 0, xác nhận các tệp tin `target/*.jar` được tạo ra cho cả 4 module, đảm bảo không có lỗi dependency resolution. Annotation `@QuarkusTest` kết hợp `@Order(1)` để chạy đầu tiên trong pipeline kiểm thử. Bao gồm assertion rằng mô tả `pom.xml` chứa `<groupId>org.nlh4j.membershiphub</groupId>`, phiên bản Java là 17, Quarkus BOM 3.15.1 được import đúng. Test phải PASS với mã thoát 0 từ Maven.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững trong giai đoạn này
```

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ kiểm thử biên dịch tích hợp, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 5: ĐÁNH GIÁ CHUẨN SCAFFOLDING & QUY ƯỚC GÓI

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Reviewer]
* **Mã Thẻ Truy Vết:** [ARC-000]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/pom.xml`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer thực hiện đánh giá chuyên sâu tệp `pom.xml` root tại `./sources/backend/pom.xml` và 4 tệp `pom.xml` con tại `./sources/backend/user-service/pom.xml`, `./sources/backend/center-service/pom.xml`, `./sources/backend/course-service/pom.xml`, `./sources/backend/attendance-service/pom.xml`. Xác minh rằng 100% package Java sử dụng quy ước `org.nlh4j.membershiphub.<service-name>` (không có `com.example`, không có ký tự gạch ngang `-` hoặc gạch dưới `_` trong cấu trúc gói), version Java 17, Quarkus 3.15.1 là phiên bản stable enterprise. Kiểm tra không tồn tại dependency bị conflict, không có dependency lỏng lẻo (SNAPSHOT) ngoại trừ các module nội bộ, plugin Maven đều có version rõ ràng, không sử dụng `<dependency>` mà thiếu `<version>` trong các module con (phải kế thừa từ parent BOM). Đánh giá việc sử dụng `quarkus-maven-plugin` để hỗ trợ `quarkus:dev` mode. Tạo báo cáo đánh giá gồm checklist tuân thủ chuẩn, danh sách cảnh báo tiềm ẩn và đề xuất cải tiến hiệu năng biên dịch.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững trong giai đoạn này
```

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ đánh giá chuẩn scaffold, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 6: SOẠN THẢO TÀI LIỆU KIẾN TRÚC SCAFFOLDING

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Doc]
* **Mã Thẻ Truy Vết:** [ARC-000], [DOC-001]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/scaffolding-architecture.md`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tài liệu Markdown `./sources/docs/scaffolding-architecture.md` trình bày chi tiết kiến trúc scaffolding của dự án Membership Hub, gồm: sơ đồ cây thư mục đa module Maven root và 4 microservices con, quy ước đặt tên gói `org.nlh4j.membershiphub.<service-name>`, bảng ánh xạ version Quarkus 3.15.1, Java 17 LTS, danh sách toàn bộ dependency chuẩn hóa với phiên bản cụ thể. Đặc tả cấu trúc frontend Next.js 14.2.5 với App Router, các dependency thiết yếu (`next-intl`, `nativewind`, `zustand`, `react-hook-form`, `zod`). Mục tiêu là chuẩn hóa đầu vào cho toàn bộ team phát triển, đảm bảo quy trình biên dịch thống nhất và có thể tích hợp liền mạch với CI/CD. Tài liệu phải chứa sơ đồ Mermaid thể hiện mối quan hệ cha-con giữa các module Maven.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu hoặc tầng bền vững trong giai đoạn này
```

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ soạn thảo tài liệu, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: KHỞI TẠO & DI TRÚ CƠ SỞ DỮ LIỆU FLYWAY ĐA BẢNG

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TẠO SCRIPT DI TRÚ V1 CHO NGƯỜI DÙNG, VAI TRÒ & TRUNG TÂM

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [DAT-001], [DAT-002], [DAT-008]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_and_roles.sql`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp di trú Flyway `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_and_roles.sql` chứa DDL ANSI SQL chuẩn hóa cho 2 bảng `roles` và `users` theo đặc tả ở mục 5.1 (Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL). Bảng `roles` gồm `role_id SMALLINT NOT NULL` (PK), `name VARCHAR(30) NOT NULL UNIQUE`, `description VARCHAR(200)`, ràng buộc CHECK tên vai trò thuộc tập `('SYSTEM_ADMIN','CENTER_ADMIN','MANAGER','TEACHER','STUDENT')`. Bảng `users` gồm `user_id UUID NOT NULL` (PK), `email VARCHAR(255) NOT NULL UNIQUE`, `password_hash CHAR(60) NOT NULL`, `full_name VARCHAR(100) NOT NULL`, `role_id SMALLINT NOT NULL` (FK tham chiếu `roles.role_id`), `provider VARCHAR(20) NOT NULL DEFAULT 'local'` với CHECK thuộc tập `('local','firebase','google','facebook')`, `created_at TIMESTAMP NOT NULL DEFAULT now()`, `updated_at TIMESTAMP NOT NULL DEFAULT now()`. Tạo 2 chỉ mục `idx_users_role_id` và `idx_users_created_at` để tối ưu hóa truy vấn. Đồng thời tạo tệp di trú `./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql` cho bảng `centers` với `center_id UUID PK`, `name VARCHAR(100) NOT NULL`, `address VARCHAR(255) NOT NULL`, `tax_id VARCHAR(20) NOT NULL UNIQUE`, `contact_phone VARCHAR(20)`, `contact_email VARCHAR(100)`, chỉ mục `idx_centers_name`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Đã thể hiện đầy đủ ở mục 5.1 phía trên.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ tạo script di trú, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: TẠO SCRIPT DI TRÚ V1 CHO KHOÁ HỌC, ĐĂNG KÝ & ĐIỂM DANH

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [DAT-003], [DAT-004], [DAT-005]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo 2 tệp di trú Flyway. Tệp `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql` chứa DDL cho bảng `courses` gồm `course_id UUID PK`, `title VARCHAR(150) NOT NULL`, `description TEXT`, `start_date DATE NOT NULL`, `end_date DATE NOT NULL`, `teacher_id UUID NOT NULL` (FK tham chiếu `users.user_id`), `max_students INT NOT NULL DEFAULT 30` với CHECK > 0, `center_id UUID NOT NULL` (FK tham chiếu `centers.center_id`), ràng buộc CHECK `end_date >= start_date`, chỉ mục `idx_courses_teacher_id` và `idx_courses_start_date`. Tệp `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql` chứa DDL cho bảng `enrollments` (`enrollment_id UUID PK`, `student_id UUID NOT NULL FK users`, `course_id UUID NOT NULL FK courses`, `enrollment_date TIMESTAMP NOT NULL DEFAULT now()`, ràng buộc UNIQUE `(student_id, course_id)` để tránh đăng ký trùng) và bảng `attendance` (`attendance_id UUID PK`, `student_id UUID NOT NULL FK users`, `course_id UUID NOT NULL FK courses`, `attendance_date DATE NOT NULL`, `timestamp TIMESTAMP NOT NULL DEFAULT now()`, ràng buộc UNIQUE composite `(student_id, course_id, attendance_date)` đảm bảo idempotency theo [REQ-013], 2 chỉ mục `idx_attendance_course_date` và `idx_attendance_student_date`).

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Đã thể hiện đầy đủ ở mục 5.1 phía trên.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ tạo script di trú, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: TẠO SCRIPT DI TRÚ V2 CHO THẺ HỌC VIÊN, THÔNG BÁO & KHUYẾN MÃI

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [DAT-006], [DAT-007], [DAT-009]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo 3 tệp di trú Flyway V2. Tệp `./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql` chứa DDL cho bảng `student_cards` gồm `card_id UUID PK`, `student_id UUID NOT NULL UNIQUE FK users`, `issue_date DATE NOT NULL`, `validity_days INT NOT NULL CHECK > 0`, `remaining_days INT NOT NULL CHECK >= 0`, `end_date DATE NOT NULL`. Tệp `./sources/backend/attendance-service/src/main/resources/db/migration/V2__init_notifications.sql` chứa DDL cho bảng `notifications` (`notification_id UUID PK`, `user_id UUID FK users nullable`, `group_zalo VARCHAR(50) nullable`, `message TEXT NOT NULL`, `sent_at TIMESTAMP NOT NULL DEFAULT now()`, `delivered BOOLEAN NOT NULL DEFAULT false`, `retry_count INT NOT NULL DEFAULT 0`, ràng buộc CHECK `(user_id IS NOT NULL) OR (group_zalo IS NOT NULL)` đảm bảo phải có ít nhất một kênh nhận, 2 chỉ mục `idx_notifications_user_id` và `idx_notifications_sent_at`). Tệp `./sources/backend/center-service/src/main/resources/db/migration/V2__init_promotions.sql` chứa DDL cho bảng `promotions` (`promo_id UUID PK`, `code VARCHAR(30) NOT NULL UNIQUE`, `discount_percent SMALLINT NOT NULL CHECK BETWEEN 1 AND 100`, `start_date DATE nullable`, `end_date DATE nullable`, `description TEXT`, `center_id UUID NOT NULL FK centers`, ràng buộc CHECK `end_date IS NULL OR end_date >= start_date`).

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Đã thể hiện đầy đủ ở mục 5.1 phía trên.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ tạo script di trú, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: TẠO SCRIPT DI TRÚ V2/V3 CHO THÔNG BÁO CHUNG, CÀI ĐẶT HỆ THỐNG & AUDIT LOG

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [DAT-010], [DAT-011], [DAT-012]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/resources/db/migration/V2__init_announcements.sql`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo 3 tệp di trú Flyway bổ sung. Tệp `./sources/backend/course-service/src/main/resources/db/migration/V2__init_announcements.sql` chứa DDL cho bảng `announcements` gồm `announcement_id UUID PK`, `title VARCHAR(150) NOT NULL`, `content TEXT NOT NULL`, `start_date DATE nullable`, `end_date DATE nullable`, `center_id UUID NOT NULL FK centers`, `created_at TIMESTAMP NOT NULL DEFAULT now()`, ràng buộc CHECK `end_date IS NULL OR end_date >= start_date`. Tệp `./sources/backend/user-service/src/main/resources/db/migration/V3__init_system_settings.sql` chứa DDL cho bảng `system_settings` (`setting_key VARCHAR(50) PK`, `setting_value TEXT NOT NULL`, `description VARCHAR(200)`). Tệp `./sources/backend/user-service/src/main/resources/db/migration/V3__init_audit_logs.sql` chứa DDL cho bảng `audit_logs` (`log_id UUID PK`, `user_id UUID FK users nullable`, `action VARCHAR(100) NOT NULL`, `details TEXT`, `occurred_at TIMESTAMP NOT NULL DEFAULT now()`, 2 chỉ mục `idx_audit_logs_user_id` và `idx_audit_logs_occurred_at`) phục vụ [NFR-006] ghi log kiểm toán 1 năm.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Đã thể hiện đầy đủ ở mục 5.1 phía trên.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ tạo script di trú, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 5: KIỂM THỬ TÍCH HỢP FLYWAY MIGRATION

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [DAT-012]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** INTEGRATION_SCOPE;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/FlywayMigrationIntegrationTest.java
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử tích hợp `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/FlywayMigrationIntegrationTest.java` xác minh toàn bộ script di trú Flyway V1, V2, V3 chạy thành công và tạo đủ 12 bảng nghiệp vụ. Sử dụng `@QuarkusTest` với profile `test`, cấu hình Testcontainers PostgreSQL 1.20.4 (`org.testcontainers:postgresql:1.20.4`) để khởi tạo cơ sở dữ liệu PostgreSQL 16-alpine trong Docker. Truy vấn `information_schema.tables` xác nhận sự tồn tại của 12 bảng (`roles`, `users`, `centers`, `courses`, `enrollments`, `attendance`, `student_cards`, `notifications`, `promotions`, `announcements`, `system_settings`, `audit_logs`). Kiểm tra ràng buộc UNIQUE composite `(student_id, course_id, attendance_date)` trên bảng `attendance` bằng cách insert 2 bản ghi trùng lặp và xác nhận exception. Xác minh CHECK constraints từng bảng hoạt động đúng. Kiểm tra toàn bộ khóa ngoại FK bằng cách thử insert giá trị không tồn tại.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Đã thể hiện đầy đủ ở mục 5.1 phía trên.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ kiểm thử di trú, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 6: ĐÁNH GIÁ CHUẨN DDL ANSI SQL & TỐI ƯU CHỈ MỤC

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Reviewer]
* **Mã Thẻ Truy Vết:** [DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [DAT-012]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer thực hiện đánh giá toàn diện 12 script di trú Flyway trên 4 microservices. Xác minh 100% tuân thủ ANSI SQL (không sử dụng ENUM đặc thù database, thay thế bằng VARCHAR + CHECK constraint). Kiểm tra các chỉ mục được tạo phù hợp với pattern truy vấn: `idx_users_role_id` cho truy vấn RBAC, `idx_courses_teacher_id` cho chức năng gán giáo viên, `idx_attendance_course_date` và `idx_attendance_student_date` cho báo cáo, `idx_notifications_user_id` cho gửi push, `idx_audit_logs_occurred_at` cho truy vấn log theo thời gian. Đánh giá ràng buộc UNIQUE composite `(student_id, course_id, attendance_date)` đảm bảo idempotency [REQ-013]. Rà soát toàn bộ FK đảm bảo tính toàn vẹn tham chiếu và ON DELETE phù hợp. Tạo báo cáo đánh giá gồm các khuyến nghị partitioning cho bảng `audit_logs` khi dữ liệu vượt 10 triệu bản ghi.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Đã thể hiện đầy đủ ở mục 5.1 phía trên.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ đánh giá chuẩn DDL, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 7: SOẠN THẢO TÀI LIỆU SƠ ĐỒ CƠ SỞ DỮ LIỆU

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Doc]
* **Mã Thẻ Truy Vết:** [DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [DAT-012], [DOC-001]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/database-schema.md`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tài liệu `./sources/docs/database-schema.md` trình bày sơ đồ quan hệ (ERD) của 12 bảng nghiệp vụ, sử dụng Mermaid `erDiagram` để thể hiện mối quan hệ giữa `users`, `roles`, `centers`, `courses`, `enrollments`, `attendance`, `student_cards`, `notifications`, `promotions`, `announcements`, `system_settings`, `audit_logs`. Mô tả chi tiết từng cột với kiểu dữ liệu, ràng buộc, chỉ mục, khóa ngoại. Bao gồm bảng ánh xạ Tag ID `[DAT-XXX]` tới từng bảng, giải thích ý nghĩa nghiệp vụ của từng trường. Tài liệu phải chứa sơ đồ Mermaid `flowchart` thể hiện trình tự áp dụng script V1, V2, V3 trong pipeline Flyway.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Đã thể hiện đầy đủ ở mục 5.1 phía trên.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ soạn thảo tài liệu, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 3: CẤU HÌNH BẢO MẬT OAUTH2, JWT VÀ SOCIAL PROVIDER

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: HIỆN THỰC HÓA JWT TOKEN PROVIDER

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [ARC-006], [NFR-003]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java` hiện thực hóa lớp `JwtTokenProvider` với annotation `@ApplicationScoped`, sử dụng `Jwt.issuer()` từ SmallRye JWT Build. Triển khai phương thức `generateAccessToken(String userId, String role, String provider)` trả về JWT có thời hạn 15 phút, claim `sub` chứa userId, claim `group` chứa role, claim `iss` là `membership-hub`, claim `aud` là `membership-hub-client`. Phương thức `generateRefreshToken(String userId)` sinh refresh token với thời hạn 7 ngày, claim `type` là `refresh`. Phương thức `validateToken(String token)` kiểm tra chữ ký bằng khóa RSA 2048-bit, xác minh thời hạn và issuer. Phương thức `getClaims(String token)` trả về `JsonWebToken` đã giải mã. Tích hợp `@ConfigProperty(name = "mp.jwt.verify.issuer")` và `mp.jwt.verify.publickey.location`. Sử dụng thuật toán RS256.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Không có thay đổi cơ sở dữ liệu trong giai đoạn này.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ tạo lớp tiện ích, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: HIỆN THỰC HÓA RESOURCE SERVER CONFIG

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [ARC-006], [NFR-003]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/ResourceServerConfig.java`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/ResourceServerConfig.java` cấu hình `@ApplicationPath("/api/v1")`, lớp `ResourceServerConfig` implement `SecurityIdentityAugmentor` từ Quarkus Security. Sử dụng `@Produces` cho `SecurityIdentity`, bổ sung role vào `SecurityIdentity` từ claim `group` trong JWT. Áp dụng annotation `@DenyAll`, `@RolesAllowed`, `@PermitAll` cho các REST endpoint. Cấu hình `quarkus.http.auth.proactive=false` để cho phép truy cập không xác thực vào `/api/v1/auth/login` và `/api/v1/auth/social`. Tích hợp `quarkus.smallrye-jwt.enabled=true`, `mp.jwt.verify.issuer=membership-hub`, `mp.jwt.verify.publickey.location=publicKey.pem`, `smallrye.jwt.sign.key.location=privateKey.pem`. Đảm bảo tất cả endpoint khác yêu cầu JWT hợp lệ, trả về HTTP 401 khi thiếu token, HTTP 403 khi không đủ quyền.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Không có thay đổi cơ sở dữ liệu trong giai đoạn này.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ cấu hình bảo mật, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: HIỆN THỰC HÓA SOCIAL AUTH PROVIDER REGISTRY

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [ARC-006], [REQ-002]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistry.java`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistry.java` hiện thực hóa interface `SocialAuthProvider` gồm `String getName()`, `SocialUserInfo verifyToken(String idToken)`. Tạo 3 implementation: `FirebaseAuthProvider` (xác minh ID Token qua Firebase Admin SDK 9.2.0, endpoint `https://identitytoolkit.googleapis.com/v1/accounts:lookup`), `GoogleAuthProvider` (xác minh qua Google API `https://oauth2.googleapis.com/tokeninfo?id_token=`), `FacebookAuthProvider` (xác minh qua `https://graph.facebook.com/v18.0/debug_token`). Lớp `SocialAuthProviderRegistry` với annotation `@ApplicationScoped` chứa map `Map<String, SocialAuthProvider>` được inject tất cả `Instance<SocialAuthProvider>`, cung cấp phương thức `SocialUserInfo authenticate(String providerName, String idToken)` tra cứu provider theo tên. Lớp `SocialUserInfo` là POJO gồm `String email`, `String fullName`, `String providerId`, `String profilePictureUrl`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Không có thay đổi cơ sở dữ liệu trong giai đoạn này.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ tạo registry provider, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: HIỆN THỰC HÓA AUTH AUDIT LOGGER

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [NFR-003], [NFR-006]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/AuthAuditLogger.java`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/AuthAuditLogger.java` hiện thực hóa lớp `AuthAuditLogger` với annotation `@ApplicationScoped`, inject `AuditLogRepository` (Panache Repository). Phương thức `logAuthEvent(UUID userId, String action, String details)` tạo bản ghi `AuditLog` với `action` thuộc tập `LOGIN_SUCCESS`, `LOGIN_FAILED`, `LOGOUT`, `SOCIAL_AUTH_SUCCESS`, `SOCIAL_AUTH_FAILED`, `TOKEN_REFRESH`, `ROLE_CHANGED`, lưu `details` dạng JSON chứa IP, User-Agent. Annotation `@Transactional` đảm bảo ghi log trong cùng transaction với nghiệp vụ. Cấu hình logger SLF4J với mức INFO, output định dạng JSON cho stack ELK, tích hợp OpenTelemetry tracing.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Không có thay đổi cơ sở dữ liệu trong giai đoạn này.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ tạo audit logger, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 5: KIỂM THỬ ĐƠN VỊ JWT TOKEN PROVIDER

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [ARC-006]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProviderTest.java`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử đơn vị `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProviderTest.java` sử dụng JUnit 5 (`@QuarkusTest`) kiểm thử toàn diện lớp `JwtTokenProvider`. Test case 1: `generateAccessToken_returnsValidJwt` xác minh JWT chứa claim `sub`, `group`, `iss=membership-hub`, `exp` trong tương lai 15 phút. Test case 2: `generateRefreshToken_returnsSevenDayToken` xác minh refresh token có thời hạn đúng 7 ngày. Test case 3: `validateToken_acceptsValidToken` xác nhận token hợp lệ. Test case 4: `validateToken_rejectsExpiredToken` xác nhận token hết hạn bị từ chối. Test case 5: `validateToken_rejectsInvalidSignature` xác nhận token sai chữ ký bị từ chối. Sử dụng khóa RSA test fixture nội bộ.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Không có thay đổi cơ sở dữ liệu trong giai đoạn này.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ kiểm thử đơn vị, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 6: KIỂM THỬ ĐƠN VỊ SOCIAL AUTH PROVIDER REGISTRY

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [ARC-006], [REQ-002]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistry.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistryTest.java`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử đơn vị `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistryTest.java` sử dụng JUnit 5 kết hợp Mockito 5.7.0 (`@InjectMock`). Mock 3 provider `FirebaseAuthProvider`, `GoogleAuthProvider`, `FacebookAuthProvider` trả về `SocialUserInfo` giả lập. Test case 1: `authenticate_withFirebase_returnsUserInfo` xác minh gọi provider đúng tên. Test case 2: `authenticate_withGoogle_returnsUserInfo` xác minh luồng Google. Test case 3: `authenticate_withFacebook_returnsUserInfo` xác minh luồng Facebook. Test case 4: `authenticate_withUnknownProvider_throwsException` xác minh ném `UnsupportedProviderException` khi tên provider không hợp lệ. Test case 5: `authenticate_withInvalidToken_throwsException` xác minh ném `InvalidTokenException` khi token không hợp lệ.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Không có thay đổi cơ sở dữ liệu trong giai đoạn này.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ kiểm thử đơn vị, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 7: ĐÁNH GIẢI PHÁP BẢO MẬT & TUÂN THỦ OWASP

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Reviewer]
* **Mã Thẻ Truy Vết:** [ARC-006], [NFR-003]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/ResourceServerConfig.java`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer thực hiện đánh giá chuyên sâu lớp bảo mật `ResourceServerConfig` và `JwtTokenProvider` đối chiếu với OWASP Top 10. Kiểm tra: (1) Khóa RSA 2048-bit đảm bảo bảo mật mật mã, (2) JWT sử dụng thuật toán RS256 không cho phép `none`, (3) Xác minh claim `iss` và `aud` ngăn chặn tấn công token confusion, (4) Refresh token lưu cơ sở dữ liệu cho phép thu hồi (revocation), (5) Rate limiting cho `/api/v1/auth/login` ngăn brute-force (cần bổ sung Bucket4j 8.10.0), (6) CSRF token cho endpoint mutation, (7) AuditLogger ghi log đầy đủ theo [NFR-006], (8) Truy vấn SQL sử dụng JPQL parameter binding ngăn SQLi. Tạo báo cáo gồm ma trận rủi ro, đề xuất bổ sung HTTP Security Headers (`X-Content-Type-Options`, `X-Frame-Options`, `Strict-Transport-Security`).

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Không có thay đổi cơ sở dữ liệu trong giai đoạn này.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ đánh giá bảo mật, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 8: SOẠN THẢO TÀI LIỆU BẢO MẬT & XÁC THỰC

* **Chuyên Biệt Hóa Quy Trình Sub-Agent:** [Doc]
* **Mã Thẻ Truy Vết:** [ARC-006], [NFR-003], [DOC-001]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/security-authentication.md`
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tài liệu `./sources/docs/security-authentication.md` trình bày toàn diện kiến trúc bảo mật và xác thực. Bao gồm: (1) Sơ đồ Mermaid `sequenceDiagram` thể hiện luồng đăng nhập email/password và cấp JWT, (2) Sơ đồ Mermaid `sequenceDiagram` thể hiện luồng Social OAuth2 với 3 provider, (3) Sơ đồ Mermaid `sequenceDiagram` thể hiện luồng refresh token, (4) Bảng mô tả cấu trúc JWT (header, payload, signature), (5) Chính sách mật khẩu mạnh, (6) Quy trình thu hồi token, (7) Ma trận RBAC ánh xạ `[ARC-001]` đến `[ARC-005]`, (8) Tuân thủ OWASP Top 10 với checklist cụ thể, (9) Hướng dẫn xử lý sự cố (forgot password, locked account, MFA trong tương lai). Tài liệu phải dùng sơ đồ Mermaid, bảng Markdown, không chứa thuật ngữ chưa giải thích.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Không có thay đổi cơ sở dữ liệu trong giai đoạn này.

* **Đặc Tả Hợp Đồng API & Sự Kiện Định Tuyến [REQ-XXX], [ARC-XXX]:**

```json
{}
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Theo Giai Đoạn [EXC-XXX]:** Không áp dụng — nhiệm vụ này chỉ soạn thảo tài liệu, không chứa logic nghiệp vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

### 📈 Giai đoạn 2 - Quản Lý Người Dùng, Trung Tâm Và Phân Quyền RBAC

- **Mục Tiêu Cốt Lõi & Mục Đích Của Giai Đoạn:** Giai đoạn 2 tập trung xây dựng hai trụ cột nghiệp vụ nền tảng của hệ thống Membership Hub: quản lý danh tính người dùng và quản lý hồ sơ trung tâm. Cụ thể, giai đoạn này hiện thực hóa toàn bộ luồng đăng ký tài khoản mới với xác thực email/mật khẩu theo chuẩn OWASP, tích hợp xác thực xã hội qua OAuth2 (Firebase/Google/Facebook), cơ chế gán và thay đổi vai trò người dùng theo ma trận phân quyền 5 cấp độ (RBAC) tuân thủ các token kiến trúc `[ARC-001]`, `[ARC-002]`, `[ARC-003]`, `[ARC-004]`, `[ARC-005]`. Song song đó, hệ thống REST API quản lý trung tâm được thiết lập với đầy đủ thao tác CRUD, kiểm tra trùng lặp TaxID, cơ chế gán/huỷ gán Center Admin với ranh giới phân quyền chặt chẽ. Mọi endpoint đều được bảo vệ bởi JWT middleware và ghi log kiểm toán phục vụ tuân thủ `[NFR-006]`.

- **Bản Đồ Ma Trận Đường Dẫn Vật Lý Mục Tiêu:** Danh sách đầy đủ các tệp tin vật lý được tạo, tái cấu trúc hoặc xử lý trong phạm vi giai đoạn này, ánh xạ chính xác đến thẻ truy vết tương ứng.

    *   `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/AuthController.java` — [REQ-001], [REQ-002]
    *   `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/UserController.java` — [REQ-003]
    *   `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/AuthService.java` — [REQ-001], [REQ-002]
    *   `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/UserRoleService.java` — [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
    *   `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/SocialAuthService.java` — [REQ-002]
    *   `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/RegisterRequest.java` — [REQ-001]
    *   `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/SocialAuthRequest.java` — [REQ-002]
    *   `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/RoleUpdateRequest.java` — [REQ-003]
    *   `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/AuthResponse.java` — [REQ-001], [REQ-002]
    *   `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtAuthFilter.java` — [ARC-006], [NFR-003]
    *   `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SocialTokenVerifier.java` — [REQ-002]
    *   `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java` — [EXC-004]
    *   `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/audit/AuditLogger.java` — [NFR-006]
    *   `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/CenterController.java` — [REQ-004], [REQ-005], [REQ-006]
    *   `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterService.java` — [REQ-004], [REQ-005]
    *   `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminService.java` — [REQ-006], [ARC-002]
    *   `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/CenterRequest.java` — [REQ-005]
    *   `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/CenterResponse.java` — [REQ-004]
    *   `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/CenterAdminRequest.java` — [REQ-006]
    *   `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/repository/CenterRepository.java` — [REQ-004], [REQ-005]
    *   `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/exception/CenterExceptionHandler.java` — [EXC-004]
    *   `./sources/docs/architecture/phase-2-rbac-matrix.md` — [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DOC-001]
    *   `./sources/docs/api/user-center-contracts.md` — [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [DOC-001]

- **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-001], [DAT-002], [DAT-008], [DAT-012]:**

```sql:matrix
-- Bảng Users (Bổ sung chỉ mục và ràng buộc nghiệp vụ cho Giai đoạn 2)
ALTER TABLE Users ADD CONSTRAINT chk_users_email_format 
    CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');
CREATE INDEX idx_users_role_id ON Users(role_id);
CREATE INDEX idx_users_provider ON Users(provider);
CREATE INDEX idx_users_email_lower ON Users(LOWER(email));

-- Bảng Roles (Bổ sung dữ liệu danh mục 5 vai trò chuẩn)
INSERT INTO Roles (role_id, name, description) VALUES
    (1, 'SystemAdmin', 'Global super-user with full permissions across all centers'),
    (2, 'CenterAdmin', 'Center-level manager with full permissions within own center'),
    (3, 'Manager', 'Sub-admin with announcement and student management rights'),
    (4, 'Teacher', 'Read-only access to assigned courses and student lists'),
    (5, 'Student', 'Course browsing, enrollment and card view access')
ON CONFLICT (role_id) DO NOTHING;

-- Bảng AuditLogs (Ghi log kiểm toán cho mọi hành động quản lý người dùng và trung tâm)
CREATE TABLE AuditLogs (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_entity VARCHAR(50) NOT NULL,
    target_id UUID,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    timestamp TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_logs_user_id ON AuditLogs(user_id);
CREATE INDEX idx_audit_logs_timestamp ON AuditLogs(timestamp);
CREATE INDEX idx_audit_logs_target ON AuditLogs(target_entity, target_id);

-- Bảng UserSocialAccounts (Lưu trữ thông tin liên kết social provider)
CREATE TABLE UserSocialAccounts (
    social_account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    profile_picture_url VARCHAR(500),
    linked_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_social_provider CHECK (provider IN ('local', 'firebase', 'google', 'facebook')),
    CONSTRAINT uk_social_provider UNIQUE (provider, provider_user_id)
);
CREATE INDEX idx_social_accounts_user_id ON UserSocialAccounts(user_id);
```

- **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [ARC-006]:**

```json
{
  "openapi": "3.1.0",
  "info": {
    "title": "User & Center Management API",
    "version": "1.0.0",
    "description": "REST contracts for user registration, social authentication, RBAC role assignment and center management"
  },
  "paths": {
    "/api/v1/users/register": {
      "post": {
        "tags": ["Authentication"],
        "summary": "Register a new user with email and password",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/RegisterRequest" }
            }
          }
        },
        "responses": {
          "201": { "description": "User registered successfully" },
          "400": { "description": "Invalid input or duplicate email" }
        }
      }
    },
    "/api/v1/auth/social": {
      "post": {
        "tags": ["Authentication"],
        "summary": "Authenticate via Firebase/Google/Facebook OAuth2",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/SocialAuthRequest" }
            }
          }
        },
        "responses": {
          "200": { "description": "Social authentication successful" }
        }
      }
    },
    "/api/v1/users/{id}/role": {
      "put": {
        "tags": ["User Management"],
        "summary": "Assign or update user role",
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
          "200": { "description": "Role updated" }
        }
      }
    },
    "/api/v1/centers": {
      "get": {
        "tags": ["Centers"],
        "summary": "List all centers with pagination",
        "responses": {
          "200": { "description": "Paginated list of centers" }
        }
      },
      "post": {
        "tags": ["Centers"],
        "summary": "Create a new center (System Admin only)",
        "responses": {
          "201": { "description": "Center created" },
          "409": { "description": "Duplicate TaxID conflict" }
        }
      }
    },
    "/api/v1/centers/{id}": {
      "put": { "tags": ["Centers"], "summary": "Update center" },
      "delete": { "tags": ["Centers"], "summary": "Delete center" }
    },
    "/api/v1/centers/{id}/admins": {
      "post": {
        "tags": ["Centers"],
        "summary": "Assign Center Admin to center",
        "responses": { "200": { "description": "Admin assigned" } }
      },
      "delete": {
        "tags": ["Centers"],
        "summary": "Unassign Center Admin from center"
      }
    }
  },
  "components": {
    "schemas": {
      "RegisterRequest": {
        "type": "object",
        "required": ["email", "password", "agreedToTerms"],
        "properties": {
          "email": { "type": "string", "format": "email", "maxLength": 255 },
          "password": { "type": "string", "minLength": 8, "maxLength": 128 },
          "fullName": { "type": "string", "maxLength": 100 },
          "agreedToTerms": { "type": "boolean" }
        }
      },
      "SocialAuthRequest": {
        "type": "object",
        "required": ["provider", "idToken"],
        "properties": {
          "provider": { "type": "string", "enum": ["firebase", "google", "facebook"] },
          "idToken": { "type": "string" },
          "profilePicture": { "type": "string", "format": "uri" }
        }
      },
      "RoleUpdateRequest": {
        "type": "object",
        "required": ["roleId"],
        "properties": {
          "roleId": { "type": "integer", "minimum": 1, "maximum": 5 }
        }
      },
      "AuthResponse": {
        "type": "object",
        "properties": {
          "accessToken": { "type": "string" },
          "refreshToken": { "type": "string" },
          "expiresIn": { "type": "integer" },
          "userId": { "type": "string", "format": "uuid" },
          "role": { "type": "string" }
        }
      }
    }
  }
}
```

- **Trình Xử Lý Ngoại Lệ Cục Bộ Hoá Của Giai Đoạn [EXC-004]:** Trong giai đoạn này, mọi lỗi xác thực đầu vào đều được chuẩn hoá thông qua cơ chế Bean Validation của Jakarta kết hợp với `GlobalExceptionHandler` tập trung. Cụ thể: (1) Lỗi `MethodArgumentNotValidException` được bắt và trả về mảng JSON gồm `field`, `message`, `rejectedValue` cho từng trường vi phạm, bao gồm email sai định dạng, mật khẩu yếu, thiếu checkbox đồng ý điều khoản, TaxID trùng lặp, định dạng số điện thoại không hợp lệ; (2) Lỗi `ConstraintViolationException` khi các ràng buộc tầng service bị vi phạm (ví dụ trùng email, trùng TaxID) được ánh xạ thành mã lỗi `EMAIL_ALREADY_EXISTS`, `TAX_ID_CONFLICT`; (3) Lỗi `AuthenticationException` từ JWT filter trả về `401 UNAUTHORIZED` với thông điệp chuẩn; (4) Lỗi `AccessDeniedException` khi người dùng không đủ quyền trả về `403 FORBIDDEN`; (5) Mọi ngoại lệ khác được bắt bởi `Exception` mặc định trả về `500 INTERNAL_SERVER_ERROR` nhưng không để lộ stack trace ra response.

#### 📅 Nhật Ký Phân Bổ Nhiệm Vụ Theo Ngày Cho Sub-Agent (Giai đoạn 2)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: KHỞI TẠO AUTHCONTROLLER VÀ LUỒNG ĐĂNG KÝ NGƯỜI DÙNG

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: TRIỂN KHAI CONTROLLER ĐĂNG KÝ NGƯỜI DÙNG

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Coder]

* **Mã Thẻ Truy Vết:** [REQ-001], [EXC-004], [ARC-006]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/AuthController.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo lớp `AuthController` được đánh dấu `@RestController` với đường dẫn gốc `/api/v1`. Triển khai endpoint `POST /api/v1/users/register` nhận `RegisterRequest`, ủy quyền cho `AuthService.register()`. Áp dụng `@Valid` để kích hoạt Bean Validation, trả về `ResponseEntity` với mã 201 và `AuthResponse` chứa JWT access token 15 phút cùng refresh token 7 ngày tuân thủ `[NFR-003]`. Toàn bộ endpoint phải được bảo vệ bởi `JwtAuthFilter` ngoại trừ chính nó. Sử dụng annotation `@AuditLogged` để ghi log kiểm toán phục vụ `[NFR-006]`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-001]:**
```sql:matrix
-- Bảng Users đã được tạo ở Giai đoạn 1, bổ sung index tối ưu cho luồng đăng ký
CREATE UNIQUE INDEX uk_users_email_unique ON Users(LOWER(email));
CREATE INDEX idx_users_created_at ON Users(created_at);
```

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-001], [ARC-006]:**
```json
{
  "endpoint": "POST /api/v1/users/register",
  "headers": { "Content-Type": "application/json" },
  "requestBody": {
    "email": "user@example.com",
    "password": "Str0ng!Pass",
    "fullName": "Nguyen Van A",
    "agreedToTerms": true
  },
  "response": {
    "status": 201,
    "body": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "rt_8f3kd92kd...",
      "expiresIn": 900,
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "role": "Student"
    }
  }
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: VIẾT BỘ KIỂM THỬ ĐĂNG KÝ NGƯỜI DÙNG

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Tester]

* **Mã Thẻ Truy Vết:** [REQ-001], [EXC-004]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/AuthController.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/controller/AuthControllerTest.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Xây dựng lớp `AuthControllerTest` sử dụng JUnit 5 kết hợp Mockito và RestAssured. Tạo ít nhất 6 test case: (1) đăng ký thành công với email hợp lệ; (2) đăng ký thất bại với email định dạng sai trả về 400; (3) đăng ký thất bại với mật khẩu yếu (thiếu chữ hoa, ký tự đặc biệt) trả về 400 với danh sách trường lỗi; (4) đăng ký thất bại khi không check vào ô đồng ý điều khoản; (5) đăng ký thất bại khi email đã tồn tại trả về 409 với mã `EMAIL_ALREADY_EXISTS`; (6) đăng ký thất bại khi thiếu trường bắt buộc trả về 400.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: TÀI LIỆU API ĐĂNG KÝ

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Doc]

* **Mã Thẻ Truy Vết:** [REQ-001], [DOC-001]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/api/user-center-contracts.md`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Cập nhật tệp tài liệu API tổng hợp bổ sung mục mô tả endpoint `POST /api/v1/users/register` với các thành phần: mô tả nghiệp vụ, bảng mã lỗi (400, 409), schema request/response, ví dụ curl thực tế, ghi chú bảo mật (rate limiting 5 lần/phút), liên kết chéo đến ma trận RBAC.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: TÍCH HỢP XÁC THỰC SOCIAL OAUTH2

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: TRIỂN KHAI SOCIALAUTHSERVICE

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Coder]

* **Mã Thẻ Truy Vết:** [REQ-002], [ARC-006], [NFR-003]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/SocialAuthService.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo lớp `SocialAuthService` được tiêm `SocialTokenVerifier`. Triển khai phương thức `authenticateWithSocial(SocialAuthRequest)` thực hiện: xác minh ID token với provider tương ứng (Firebase/Google/Facebook), trích xuất email và provider_user_id, tìm kiếm bản ghi trong `UserSocialAccounts`, nếu chưa tồn tại thì tạo mới user với role mặc định `Student` và liên kết social account, cập nhật `profile_picture_url` nếu có, cuối cùng gọi `JwtTokenProvider.generateToken()` trả về `AuthResponse`. Toàn bộ thao tác phải sử dụng `@Transactional` và ghi audit log.

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-002], [ARC-006]:**
```json
{
  "endpoint": "POST /api/v1/auth/social",
  "requestBody": {
    "provider": "google",
    "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2ZTAw...",
    "profilePicture": "https://lh3.googleusercontent.com/a/AGN..."
  },
  "response": {
    "status": 200,
    "body": {
      "accessToken": "eyJhbGciOiJIUzI1NiIs...",
      "refreshToken": "rt_4kd92kd...",
      "expiresIn": 900,
      "userId": "660e8400-e29b-41d4-a716-446655440001",
      "role": "Student",
      "isNewUser": true
    }
  }
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: KIỂM THỬ SOCIAL AUTH

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Tester]

* **Mã Thẻ Truy Vết:** [REQ-002], [EXC-004]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/SocialAuthService.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/service/SocialAuthServiceTest.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo lớp `SocialAuthServiceTest` với các test case: (1) xác thực thành công với Google token hợp lệ cho user mới; (2) xác thực thành công cho user đã liên kết trước đó; (3) xác thực thất bại với token hết hạn; (4) xác thực thất bại với provider không hỗ trợ; (5) xác thực Facebook với profile_picture được lưu đúng. Sử dụng Mockito stub cho `SocialTokenVerifier` và `UserSocialAccountRepository`.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: REVIEW CODE AUTHENTICATION

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Reviewer]

* **Mã Thẻ Truy Vết:** [REQ-001], [REQ-002], [NFR-003]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtAuthFilter.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Thực hiện đánh giá mã nguồn `JwtAuthFilter` và `SocialTokenVerifier` để phát hiện: (1) khả năng timing attack trong so sánh token; (2) lỗi xác thực chữ ký JWT; (3) xử lý trường hợp token bị thu hồi (blacklist); (4) tuân thủ nguyên tắc OWASP A02:2021 về Cryptographic Failures. Đề xuất vá lỗi cụ thể cho từng phát hiện.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Hoá Của Giai Đoạn [EXC-004]:** Khi phát hiện token không hợp lệ hoặc hết hạn, `JwtAuthFilter` phải trả về lỗi `401 UNAUTHORIZED` với mã `INVALID_TOKEN` hoặc `TOKEN_EXPIRED` tương ứng, kèm thông điệp thân thiện không tiết lộ chi tiết kỹ thuật. Khi token bị thu hồi (người dùng đăng xuất), hệ thống kiểm tra Redis blacklist trước khi cấp quyền truy cập.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 3: CƠ CHẾ GÁN VÀ THAY ĐỔI VAI TRÒ NGƯỜI DÙNG (RBAC)

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: TRIỂN KHAI USERROLESERVICE

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Coder]

* **Mã Thẻ Truy Vết:** [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/UserRoleService.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo lớp `UserRoleService` với phương thức `updateUserRole(UUID userId, int newRoleId)`. Triển khai logic: kiểm tra người thực hiện có phải SystemAdmin hay CenterAdmin hợp lệ, kiểm tra sự tồn tại của user, xác thực role_id thuộc tập {1,2,3,4,5}, cập nhật cột `role_id` trong bảng `Users`, vô hiệu hoá phiên JWT hiện tại bằng cách thêm vào blacklist Redis với TTL bằng thời gian còn lại của token, ghi `AuditLogs` với old_value và new_value, gửi Kafka event `user.role.changed` để các service khác cập nhật cache. Áp dụng `@PreAuthorize("hasAnyRole('SystemAdmin','CenterAdmin')")` và xử lý trường hợp CenterAdmin chỉ được đổi role trong phạm vi trung tâm mình quản lý.

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-003], [ARC-001], [ARC-002]:**
```json
{
  "endpoint": "PUT /api/v1/users/{id}/role",
  "headers": {
    "Authorization": "Bearer eyJhbGciOiJIUzI1NiIs...",
    "Content-Type": "application/json"
  },
  "requestBody": { "roleId": 2 },
  "response": {
    "status": 200,
    "body": {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "oldRoleId": 5,
      "newRoleId": 2,
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  }
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: KIỂM THỬ PHÂN QUYỀN RBAC

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Tester]

* **Mã Thẻ Truy Vết:** [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [EXC-004]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/UserRoleService.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/service/UserRoleServiceTest.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Xây dựng `UserRoleServiceTest` với 8 test case: (1) SystemAdmin thay đổi role thành công; (2) CenterAdmin thay đổi role thành công trong trung tâm mình quản lý; (3) CenterAdmin cố gắng thay đổi user ở trung tâm khác bị từ chối 403; (4) Manager thay đổi role bị từ chối 403; (5) Teacher thay đổi role bị từ chối 403; (6) thay đổi role với role_id không hợp lệ (6, 0) trả về 400; (7) user không tồn tại trả về 404; (8) sau khi đổi role, JWT cũ bị blacklist không thể sử dụng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: TÀI LIỆU MA TRẬN RBAC

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Doc]

* **Mã Thẻ Truy Vết:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DOC-001]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/architecture/phase-2-rbac-matrix.md`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tài liệu Markdown mô tả ma trận phân quyền 5 cấp độ theo định dạng bảng gồm các cột: Role, Scope, CRUD Permissions, Special Permissions, Allowed Endpoints. Đính kèm sơ đồ Mermaid biểu diễn quan hệ kế thừa giữa các role, danh sách endpoint mà từng role có thể truy cập, ghi chú tuân thủ nguyên tắc least privilege.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 4: REST API DANH SÁCH VÀ QUẢN LÝ TRUNG TÂM (PHẦN 1)

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: TRIỂN KHAI CENTERCONTROLLER VÀ DANH SÁCH

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Coder]

* **Mã Thẻ Truy Vết:** [REQ-004], [REQ-005]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/CenterController.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo lớp `CenterController` với các endpoint: (1) `GET /api/v1/centers` trả về danh sách phân trang gồm Name, Address, TaxID, AdminContact với `@PreAuthorize("isAuthenticated()")`; (2) `POST /api/v1/centers` chỉ SystemAdmin được phép tạo mới trung tâm; (3) `PUT /api/v1/centers/{id}` cập nhật; (4) `DELETE /api/v1/centers/{id}` xoá mềm. Tích hợp `CenterService` với validation cho TaxID unique. Áp dụng `@Valid` cho mọi request body.

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-004], [REQ-005]:**
```json
{
  "endpoints": [
    {
      "method": "GET",
      "path": "/api/v1/centers?page=0&size=20&sort=name,asc",
      "response": {
        "content": [
          {
            "centerId": "770e8400-e29b-41d4-a716-446655440000",
            "name": "Trung tâm Quận 1",
            "address": "123 Nguyễn Huệ, Quận 1, TP.HCM",
            "taxId": "0312345678",
            "adminContact": "admin.q1@membershiphub.vn"
          }
        ],
        "totalElements": 5,
        "totalPages": 1
      }
    },
    {
      "method": "POST",
      "path": "/api/v1/centers",
      "requestBody": {
        "name": "Trung tâm Quận 3",
        "address": "456 Võ Văn Tần, Quận 3",
        "taxId": "0398765432",
        "contactPhone": "+84 28 1234 5678",
        "contactEmail": "contact.q3@membershiphub.vn"
      }
    }
  ]
}
```

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-002]:**
```sql:matrix
-- Bổ sung ràng buộc và chỉ mục tối ưu cho bảng Centers
ALTER TABLE Centers ADD CONSTRAINT chk_centers_taxid_numeric 
    CHECK (tax_id ~ '^[0-9]{10,13}$');
ALTER TABLE Centers ADD CONSTRAINT chk_centers_phone_format 
    CHECK (contact_phone IS NULL OR contact_phone ~ '^[+0-9 ()-]+$');
ALTER TABLE Centers ADD CONSTRAINT chk_centers_email_format 
    CHECK (contact_email IS NULL OR contact_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');
CREATE INDEX idx_centers_taxid ON Centers(tax_id);
CREATE INDEX idx_centers_name_lower ON Centers(LOWER(name));
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: KIỂM THỬ CENTER CONTROLLER

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Tester]

* **Mã Thẻ Truy Vết:** [REQ-004], [REQ-005], [EXC-004]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/CenterController.java;./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/controller/CenterControllerTest.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo `CenterControllerTest` với 7 test case: (1) lấy danh sách phân trang thành công với user bất kỳ đã xác thực; (2) SystemAdmin tạo trung tâm mới thành công 201; (3) Manager cố tạo trung tâm bị 403; (4) tạo trung tâm với TaxID trùng trả về 409 `TAX_ID_CONFLICT`; (5) tạo trung tâm với email sai định dạng trả về 400; (6) cập nhật trung tâm thành công; (7) xoá trung tâm thành công.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: REVIEW MÃ NGUỒN CENTER

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Reviewer]

* **Mã Thẻ Truy Vết:** [REQ-004], [REQ-005], [ARC-002], [NFR-003]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterService.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Đánh giá `CenterService` để phát hiện: (1) lỗ hổng SQL injection trong truy vấn tìm kiếm; (2) tuân thủ nguyên tắc OWASP A03:2021 Injection; (3) khả năng race condition khi kiểm tra trùng lặp TaxID đồng thời; (4) hiệu năng truy vấn khi danh sách trung tâm lớn. Đề xuất sử dụng unique constraint kết hợp upsert pattern.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Hoá Của Giai Đoạn [EXC-004]:** Khi TaxID đã tồn tại, `CenterExceptionHandler` bắt `DataIntegrityViolationException` từ PostgreSQL và ánh xạ thành `409 CONFLICT` với mã `TAX_ID_CONFLICT`. Khi validation input thất bại (tên trống, địa chỉ trống, TaxID không đúng 10-13 chữ số), trả về 400 với mảng chi tiết lỗi từng trường.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 5: GÁN/HUỶ GÁN CENTER ADMIN

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: TRIỂN KHAI CENTERADMINSERVICE

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Coder]

* **Mã Thẻ Truy Vết:** [REQ-006], [ARC-002], [NFR-006]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminService.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo lớp `CenterAdminService` với phương thức `assignAdmin(centerId, userId)` thực hiện: xác thực center tồn tại, user tồn tại, chỉ SystemAdmin mới có quyền thực hiện, cập nhật `role_id = 2` (CenterAdmin) trong bảng Users, lưu `center_id` vào bảng liên kết `CenterAdmins` với khóa chính tổng hợp `(center_id, user_id)`, ghi audit log, gửi Kafka event `center.admin.assigned`. Phương thức `unassignAdmin` thực hiện ngược lại và đặt role về Student.

* **Hợp Đồng Định Tuyến API Và Sự Kiện [REQ-006], [ARC-002]:**
```json
{
  "endpoints": [
    {
      "method": "POST",
      "path": "/api/v1/centers/{id}/admins",
      "requestBody": { "userId": "550e8400-e29b-41d4-a716-446655440000" },
      "response": { "status": 200, "body": { "centerId": "770e8400-...", "userId": "550e8400-...", "assignedAt": "2024-01-15T10:30:00Z" } }
    },
    {
      "method": "DELETE",
      "path": "/api/v1/centers/{id}/admins/{userId}",
      "response": { "status": 204 }
    }
  ]
}
```

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-002], [DAT-012]:**
```sql:matrix
-- Bảng liên kết CenterAdmins
CREATE TABLE CenterAdmins (
    center_id UUID NOT NULL,
    user_id UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT now(),
    assigned_by UUID NOT NULL,
    PRIMARY KEY (center_id, user_id)
);
CREATE INDEX idx_center_admins_user ON CenterAdmins(user_id);
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: KIỂM THỬ GÁN ADMIN

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Tester]

* **Mã Thẻ Truy Vết:** [REQ-006], [ARC-002], [EXC-004]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminService.java;./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminServiceTest.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo `CenterAdminServiceTest` với 6 test case: (1) SystemAdmin gán Center Admin thành công; (2) CenterAdmin cố gán admin khác bị 403; (3) gán user không tồn tại trả về 404; (4) gán cho center không tồn tại trả về 404; (5) gán trùng lặp trả về 409; (6) huỷ gán thành công và role chuyển về Student.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: TÀI LIỆU HỢP ĐỒNG CENTER

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Doc]

* **Mã Thẻ Truy Vết:** [REQ-004], [REQ-005], [REQ-006], [DOC-001]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/api/user-center-contracts.md`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Cập nhật tệp tài liệu API tổng hợp bổ sung 3 endpoint mới: `POST /api/v1/centers/{id}/admins`, `DELETE /api/v1/centers/{id}/admins/{userId}`, mô tả bảng mã lỗi (403, 404, 409), ghi chú về hiệu lực quyền hạn ngay sau khi gán.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 6: GLOBAL EXCEPTION HANDLER VÀ KIỂM TOÁN

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: TRIỂN KHAI GLOBAL EXCEPTION HANDLER

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Coder]

* **Mã Thẻ Truy Vết:** [EXC-004], [NFR-003]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo lớp `GlobalExceptionHandler` được đánh dấu `@RestControllerAdvice` xử lý tập trung: (1) `MethodArgumentNotValidException` trả về 400 với mảng `FieldErrorResponse`; (2) `ConstraintViolationException` trả về 400; (3) `DataIntegrityViolationException` ánh xạ thành 409 với mã lỗi tương ứng (EMAIL_ALREADY_EXISTS, TAX_ID_CONFLICT, DUPLICATE_KEY); (4) `AuthenticationException` trả về 401; (5) `AccessDeniedException` trả về 403; (6) `EntityNotFoundException` trả về 404; (7) `Exception` mặc định trả về 500 nhưng không để lộ stack trace. Mỗi response phải bao gồm `timestamp`, `status`, `errorCode`, `message`, `path`, `traceId` để phục vụ observability.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Hoá Của Giai Đoạn [EXC-004]:** Cấu trúc response lỗi chuẩn hoá là `{"timestamp":"2024-01-15T10:30:00Z","status":400,"errorCode":"VALIDATION_FAILED","message":"Input validation failed","errors":[{"field":"email","message":"must be a well-formed email address","rejectedValue":"invalid-email"}],"path":"/api/v1/users/register","traceId":"a1b2c3d4"}`. Mọi endpoint trong user-service và center-service đều sử dụng chung cấu trúc này.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: KIỂM THỬ TÍCH HỢP EXCEPTION HANDLER

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Tester]

* **Mã Thẻ Truy Vết:** [EXC-004], [NFR-003]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandlerTest.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo `GlobalExceptionHandlerTest` với 7 test case kiểm tra: (1) response 400 khi email sai định dạng; (2) response 409 khi email trùng; (3) response 401 khi thiếu token; (4) response 403 khi không đủ quyền; (5) response 404 khi user không tồn tại; (6) response 500 khi lỗi hệ thống không xác định nhưng không lộ stack trace; (7) đảm bảo response luôn có `traceId` để liên kết log.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: TRIỂN KHAI AUDIT LOGGER

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Coder]

* **Mã Thẻ Truy Vết:** [NFR-006], [REQ-003], [REQ-006]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/audit/AuditLogger.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo lớp `AuditLogger` với annotation `@AuditLogged(action = "...")` cho phép ghi log kiểm toán tự động thông qua AOP aspect. Triển khai: (1) lưu bản ghi vào bảng `AuditLogs` với đầy đủ user_id, action, target_entity, target_id, old_value, new_value, ip_address, user_agent, timestamp; (2) đồng thời ghi log có cấu trúc (JSON) ra console để tích hợp với Stackdriver Logging; (3) đảm bảo thao tác ghi log không làm thất bại giao dịch chính thông qua cơ chế `@Transactional(propagation = Propagation.REQUIRES_NEW)`. Tuân thủ `[NFR-006]` yêu cầu lưu trữ 1 năm.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 7: ĐÁNH GIÁ TỔNG THỂ VÀ TÍCH HỢP CUỐI KỲ

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: REVIEW TOÀN DIỆN GIAI ĐOẠN 2

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Reviewer]

* **Mã Thẻ Truy Vết:** [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-003], [NFR-006], [EXC-004]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtAuthFilter.java`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Thực hiện đánh giá tổng thể toàn bộ mã nguồn Giai đoạn 2 theo checklist: (1) Tuân thủ OWASP Top 10 — A01 Broken Access Control (kiểm tra quyền truy cập theo role), A03 Injection (parameterized queries), A07 Identification and Authentication Failures; (2) Tuân thủ nguyên tắc SOLID và Clean Architecture; (3) Khả năng mở rộng và tái sử dụng của các service layer; (4) Hiệu năng truy vấn database và khả năng chịu tải; (5) Phát hiện code smell và đề xuất cải tiến. Lập báo cáo đánh giá chi tiết với định dạng bảng gồm mức độ nghiêm trọng (Critical/High/Medium/Low), mô tả, đề xuất fix, file liên quan.

* **Trình Xử Lý Ngoại Lệ Cục Bộ Hoá Của Giai Đoạn [EXC-004]:** Tổng hợp toàn bộ 5 mã lỗi nghiệp vụ đã được chuẩn hoá trong Giai đoạn 2: `EMAIL_ALREADY_EXISTS` (409), `TAX_ID_CONFLICT` (409), `INVALID_TOKEN` (401), `TOKEN_EXPIRED` (401), `INSUFFICIENT_PRIVILEGES` (403), `USER_NOT_FOUND` (404), `CENTER_NOT_FOUND` (404), `VALIDATION_FAILED` (400). Mỗi mã lỗi có mô tả tiếng Việt rõ ràng cho developer và end-user.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: KIỂM THỬ TÍCH HỢP LIÊN SERVICE

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Tester]

* **Mã Thẻ Truy Vết:** [REQ-001], [REQ-003], [REQ-006], [ARC-001], [ARC-002], [ARC-006], [NFR-003]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** INTEGRATION_SCOPE;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserCenterIntegrationTest.java

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Xây dựng bộ kiểm thử tích hợp liên service sử dụng Testcontainers (PostgreSQL + Redis) và WireMock. Các kịch bản: (1) Luồng hoàn chỉnh từ đăng ký user → gán role CenterAdmin → truy cập endpoint center thành công; (2) Sau khi đổi role từ CenterAdmin về Student, endpoint center trả về 403; (3) Đăng ký qua Google OAuth2 → liên kết social account → đăng nhập lần sau không tạo user mới; (4) Đồng bộ audit log giữa user-service và center-service.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: TÀI LIỆU TỔNG KẾT GIAI ĐOẠN 2

* **Chuyên Môn Hoá Quy Trình Sub-Agent:** [Doc]

* **Mã Thẻ Truy Vết:** [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [NFR-003], [NFR-006], [DOC-001]

* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/api/user-center-contracts.md`

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tổng hợp và cập nhật tài liệu tổng kết Giai đoạn 2 bao gồm: (1) Bảng tổng hợp 7 endpoint đã triển khai với mã trạng thái, mô tả, role yêu cầu; (2) Sơ đồ tuần tự (sequence diagram) mô tả luồng đăng ký và xác thực; (3) Bảng mã lỗi chuẩn hoá; (4) Checklist tuân thủ OWASP đã áp dụng; (5) Hướng dẫn cấu hình biến môi trường cho OAuth2 providers; (6) Tài liệu chuyển giao cho Giai đoạn 3.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

<!--PHASE_INDEX_START-->

### 📈 Giai đoạn 3 - Phát Triển Nghiệp Vụ Khoá Học Và Điểm Danh QR
- **Mục Tiêu Cốt Lõi & Mục Đích Giai Đoạn:** Hiện thực hóa toàn bộ luồng nghiệp vụ liên quan đến quản lý khoá học, phân công giáo viên, đăng ký khoá học của sinh viên và đặc biệt là luồng xử lý điểm danh QR với cơ chế idempotency, retry khi mất mạng và FIFO khi khôi phục dịch vụ. Giai đoạn này tập trung vào hai microservices `course-service` và `attendance-service` thuộc nhóm backend, đồng thời đảm bảo khả năng phát hiện xung đột lịch dạy của giáo viên thông qua ràng buộc exclusion tại cơ sở dữ liệu.

- **Bản Đồ Ma Trận Đường Dẫn Vật Lý Mục Tiêu:**
    * `./sources/backend/course-service/pom.xml`
    * `./sources/backend/course-service/src/main/resources/application.properties`
    * `./sources/backend/course-service/src/main/resources/db/migration/V1__courses_init.sql`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseServiceApplication.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseTeacherController.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/StudentCourseBrowseController.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/EnrollmentController.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseService.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseTeacherService.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseBrowseService.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/EnrollmentService.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/repository/CourseRepository.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/repository/EnrollmentRepository.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/CourseResponse.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/CourseCreateRequest.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/EnrollmentRequest.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/TeacherAssignRequest.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/ScheduleConflictException.java`
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/EnrollmentNotFoundException.java`
    * `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseServiceIntegrationTestSuite.java`
    * `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/controller/CourseControllerTest.java`
    * `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/service/CourseServiceTest.java`
    * `./sources/backend/attendance-service/pom.xml`
    * `./sources/backend/attendance-service/src/main/resources/application.properties`
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceApplication.java`
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/controller/AttendanceController.java`
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/AttendanceService.java`
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/QrPayloadDecoder.java`
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/repository/AttendanceRepository.java`
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/QrScanRequest.java`
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/AttendanceResponse.java`
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/exception/DuplicateAttendanceException.java`
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/exception/EnrollmentRequiredException.java`
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/messaging/KafkaAttendanceProducer.java`
    * `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceIntegrationTestSuite.java`
    * `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/service/AttendanceServiceTest.java`
    * `./sources/docs/architecture/course-architecture.md`
    * `./sources/docs/architecture/attendance-architecture.md`
    * `./sources/docs/api/course-openapi.yaml`
    * `./sources/docs/api/attendance-openapi.yaml`

- **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:** Migrations bổ sung cho `course-service` liên quan đến ràng buộc exclusion chống xung đột lịch giáo viên và bảng ánh xạ giáo viên-khoá học.

```sql:matrix
-- Flyway V2__course_schedule_exclusion.sql
-- Áp dụng ràng buộc exclusion dựa trên teacher_id và khoảng ngày [REQ-008]
ALTER TABLE courses
    ADD CONSTRAINT chk_course_dates CHECK (end_date >= start_date);

CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE courses
    ADD CONSTRAINT ex_teacher_schedule_no_overlap
    EXCLUDE USING gist (
        teacher_id WITH =,
        daterange(start_date, end_date, '[]') WITH &&
    )
    WHERE (teacher_id IS NOT NULL);

-- Bảng ánh xạ course_teacher (m:n) [REQ-009]
CREATE TABLE course_teacher_mapping (
    mapping_id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    teacher_id UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_ctm_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT fk_ctm_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id),
    CONSTRAINT uq_course_teacher UNIQUE (course_id, teacher_id)
);

CREATE INDEX idx_course_teacher_course ON course_teacher_mapping(course_id);
CREATE INDEX idx_course_teacher_teacher ON course_teacher_mapping(teacher_id);
```

- **Hợp Đồng API Và Định Tuyến Sự Kiện [REQ-XXX], [ARC-XXX]:**

```json
{
  "openapi": "3.1.0",
  "info": {
    "title": "Course Service API",
    "version": "1.0.0"
  },
  "paths": {
    "/api/v1/courses": {
      "get": {
        "summary": "Danh sách khoá học có phân trang [REQ-007]",
        "responses": {
          "200": {
            "description": "Trả về trang dữ liệu khoá học"
          }
        }
      },
      "post": {
        "summary": "Tạo khoá học mới [REQ-008]",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/CourseCreateRequest" }
            }
          }
        }
      }
    },
    "/api/v1/courses/{id}/teachers": {
      "post": {
        "summary": "Gán giáo viên cho khoá học [REQ-009]",
        "responses": { "201": { "description": "Đã gán giáo viên" } }
      },
      "delete": {
        "summary": "Huỷ gán giáo viên khỏi khoá học [REQ-009]"
      }
    },
    "/api/v1/students/courses/available": {
      "get": {
        "summary": "Duyệt khoá học khả dụng cho sinh viên [REQ-010]"
      }
    },
    "/api/v1/enrollments": {
      "post": {
        "summary": "Đăng ký khoá học cho sinh viên [REQ-011]",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/EnrollmentRequest" }
            }
          }
        }
      }
    },
    "/api/v1/attendance/scan": {
      "post": {
        "summary": "Ghi nhận điểm danh QR [REQ-012], [REQ-013], [ARC-007]",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/QrScanRequest" }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "CourseCreateRequest": {
        "type": "object",
        "properties": {
          "title": { "type": "string", "maxLength": 150 },
          "startDate": { "type": "string", "format": "date" },
          "endDate": { "type": "string", "format": "date" },
          "teacherId": { "type": "string", "format": "uuid" },
          "maxStudents": { "type": "integer", "default": 30 }
        },
        "required": ["title", "startDate", "endDate", "teacherId"]
      },
      "EnrollmentRequest": {
        "type": "object",
        "properties": {
          "courseId": { "type": "string", "format": "uuid" }
        },
        "required": ["courseId"]
      },
      "QrScanRequest": {
        "type": "object",
        "properties": {
          "qrPayload": {
            "type": "string",
            "description": "Payload base64 chứa studentID và courseID"
          },
          "idempotencyKey": { "type": "string" }
        },
        "required": ["qrPayload"]
      }
    }
  }
}
```

- **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:** Liệt kê các quy tắc xác thực nghiệp vụ và luồng xử lý lỗi cục bộ gắn liền với endpoint của giai đoạn này.

#### 📅 Nhật Ký Phân Bổ Nhiệm Vụ Theo Ngày Cho Sub-Agent (Giai đoạn 3)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: KHỞI TẠO COURSE-SERVICE VÀ MODULE KHOÁ HỌC

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: KHỞI TẠO MÔ TẢ BUILD VÀ ỨNG DỤNG CHO COURSE-SERVICE
- **Chuyên Môn Hóa Sub-Agent:** [Coder]
- **Mã Thẻ Được Nhắm Tới:** [REQ-007], [REQ-008], [ARC-000]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/pom.xml`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh tệp `pom.xml` cho `course-service` thừa kế parent `./sources/backend/pom.xml`, khai báo dependency Quarkus RESTEasy Reactive, Hibernate ORM Panache, PostgreSQL JDBC, Flyway, SmallRye Reactive Messaging Kafka, Jakarta Validation, JUnit 5. Định nghĩa artifactId `course-service` và groupId `org.nlh4j.membershiphub`. Cấu hình plugin `quarkus-maven-plugin` để build native image. Tệp này phải biên dịch trống (blank compile) ngay từ đầu.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Migration V1__courses_init.sql [DAT-001], [DAT-004]
CREATE TABLE courses (
    course_id UUID PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    teacher_id UUID,
    max_students INT DEFAULT 30,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id)
);

CREATE INDEX idx_courses_teacher ON courses(teacher_id);
CREATE INDEX idx_courses_dates ON courses(start_date, end_date);
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: KIỂM THỬ TÍCH HỢP MÔ TẢ BUILD COURSE-SERVICE
- **Chuyên Môn Hóa Sub-Agent:** [Tester]
- **Mã Thẻ Được Nhắm Tới:** [ARC-000], [REQ-007]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `INTEGRATION_SCOPE;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseServiceIntegrationTestSuite.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo test suite tích hợp Maven sử dụng JUnit 5 Platform Launcher kết nối `./sources/infra/test/maven-build-integration.sh` để xác minh `./sources/backend/course-service/pom.xml` biên dịch sạch. Test phải fail nếu dependency chưa khả dụng hoặc parent pom không hợp lệ.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ kiểm thử tích hợp mô tả build
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: TÀI LIỆU KIẾN TRÚC TỔNG QUAN COURSE-SERVICE
- **Chuyên Môn Hóa Sub-Agent:** [Doc]
- **Mã Thẻ Được Nhắm Tới:** [REQ-007], [DOC-001]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/architecture/course-architecture.md`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tài liệu Markdown mô tả kiến trúc `course-service` gồm sơ đồ C4 Container, các thành phần chính (REST Controller, Service, Repository, Kafka Producer), luồng xử lý nghiệp vụ CRUD khoá học và tích hợp Kafka. Tài liệu phải liệt kê đầy đủ Tag ID `[REQ-007]`, `[REQ-008]`, `[REQ-009]`, `[REQ-010]`, `[REQ-011]`, `[ARC-007]`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Tài liệu không yêu cầu migration SQL
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: REVIEW MÃ NGUỒN KHỞI TẠO POM VÀ ỨNG DỤNG
- **Chuyên Môn Hóa Sub-Agent:** [Reviewer]
- **Mã Thẻ Được Nhắm Tới:** [ARC-000], [REQ-007]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseServiceApplication.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Đánh giá tệp `CourseServiceApplication.java` chứa annotation `@QuarkusMain`, đảm bảo cấu hình gói `org.nlh4j.membershiphub.courseservice` chính xác, không tham chiếu `com.example`. Xác nhận hàm `main` chuẩn Quarkus và phát hiện sớm các vấn đề cấu hình.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ review mã nguồn
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: HIỆN THỰC HOÁ REST CONTROLLER VÀ SERVICE CHO KHOÁ HỌC

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TRIỂN KHAI COURSE CONTROLLER VÀ DTO
- **Chuyên Môn Hóa Sub-Agent:** [Coder]
- **Mã Thẻ Được Nhắm Tới:** [REQ-007], [REQ-008]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Hiện thực hoá `CourseController` với các endpoint GET `/api/v1/courses` (phân trang), POST/PUT/DELETE `/api/v1/courses` với bean validation. Sử dụng annotation `@Path`, `@GET`, `@POST`, `@PUT`, `@DELETE`, `@Valid`, `@NotNull`. Trả về `Response` với status 200/201/204 theo chuẩn REST. Tích hợp `@RolesAllowed` cho phân quyền theo `[ARC-001]`, `[ARC-002]`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ triển khai controller
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: KIỂM THỬ ĐƠN VỊ CHO COURSE CONTROLLER
- **Chuyên Môn Hóa Sub-Agent:** [Tester]
- **Mã Thẻ Được Nhắm Tới:** [REQ-007], [REQ-008]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/controller/CourseControllerTest.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh `CourseControllerTest.java` sử dụng JUnit 5 và Mockito. Test các kịch bản: liệt kê khoá học trả về 200, tạo mới trả 201, validation fail trả 400, xung đột lịch trả 409. Verify gọi `CourseService` đúng tham số.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ kiểm thử controller
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: TÀI LIỆU API CHO COURSE SERVICE
- **Chuyên Môn Hóa Sub-Agent:** [Doc]
- **Mã Thẻ Được Nhắm Tới:** [REQ-007], [REQ-008], [DOC-001]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/api/course-openapi.yaml`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tệp OpenAPI 3.1 mô tả endpoint `/api/v1/courses` bao gồm schema `CourseCreateRequest`, `CourseResponse`, mã lỗi 400/404/409. Tích hợp bearer token security scheme. Tham chiếu Tag ID `[REQ-007]`, `[REQ-008]`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Tài liệu không yêu cầu migration SQL
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: REVIEW LOGIC OVERLAP CHECK
- **Chuyên Môn Hóa Sub-Agent:** [Reviewer]
- **Mã Thẻ Được Nhắm Tới:** [REQ-008], [EXC-004]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseService.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Đánh giá logic `CourseService.create` và `update` đảm bảo có kiểm tra xung đột lịch giáo viên trước khi persist. Xác nhận sử dụng `PanacheRepository` và ném `ScheduleConflictException` khi vi phạm ràng buộc exclusion tại DB `[DAT-001]`. Đề xuất cải tiến nếu thiếu transaction boundary.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ review
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 3: GÁN GIÁO VIÊN VÀ PHÁT SINH SỰ KIỆN KAFKA

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TRIỂN KHAI CONTROLLER GÁN GIÁO VIÊN
- **Chuyên Môn Hóa Sub-Agent:** [Coder]
- **Mã Thẻ Được Nhắm Tới:** [REQ-009], [ARC-007]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseTeacherController.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo `CourseTeacherController` xử lý POST `/api/v1/courses/{id}/teachers` và DELETE tương ứng. Sử dụng `TeacherAssignRequest` chứa `teacherId`. Inject `CourseTeacherService` và `KafkaTeacherProducer` để đẩy sự kiện `teacher-assigned` lên Kafka topic `teacher-events` sau khi ghi DB thành công.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ triển khai controller gán giáo viên
```

* **Hợp Đồng API Và Định Tuyến Sự Kiện [REQ-XXX], [ARC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: KIỂM THỬ ĐƠN VỊ SERVICE GÁN GIÁO VIÊN
- **Chuyên Môn Hóa Sub-Agent:** [Tester]
- **Mã Thẻ Được Nhắm Tới:** [REQ-009], [ARC-007]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseTeacherService.java;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/service/CourseServiceTest.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Bổ sung test case trong `CourseServiceTest` cho `CourseTeacherService.assign` và `unassign`. Mock `CourseRepository`, `KafkaTeacherProducer`. Xác nhận rằng khi gán thành công, sự kiện Kafka được publish đúng topic và payload. Test trường hợp giáo viên đã tồn tại ném exception.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ kiểm thử service
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: TÀI LIỆU LUỒNG KAFKA GÁN GIÁO VIÊN
- **Chuyên Môn Hóa Sub-Agent:** [Doc]
- **Mã Thẻ Được Nhắm Tới:** [REQ-009], [ARC-008], [DOC-001]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/architecture/course-architecture.md`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Cập nhật tài liệu kiến trúc `course-architecture.md` bổ sung sơ đồ tuần tự (sequence diagram) cho luồng gán giáo viên, mô tả cách Kafka topic `teacher-events` được publish và consume bởi notification-service. Tham chiếu Tag ID `[REQ-009]`, `[ARC-008]`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Tài liệu không yêu cầu migration SQL
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: REVIEW LOGIC GÁN GIÁO VIÊN VÀ XỬ LÝ NGOẠI LỆ
- **Chuyên Môn Hóa Sub-Agent:** [Reviewer]
- **Mã Thẻ Được Nhắm Tới:** [REQ-009], [EXC-004]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/ScheduleConflictException.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Đánh giá `ScheduleConflictException` đảm bảo kế thừa `RuntimeException`, chứa message mô tả xung đột lịch rõ ràng. Xác nhận có `@ApplicationException` hoặc ExceptionMapper để trả về HTTP 409. Đề xuất bổ sung logging có cấu trúc.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ review
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 4: DUYỆT KHOÁ HỌC VÀ ĐĂNG KÝ SINH VIÊN

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TRIỂN KHAI ENDPOINT DUYỆT KHOÁ HỌC CHO SINH VIÊN
- **Chuyên Môn Hóa Sub-Agent:** [Coder]
- **Mã Thẻ Được Nhắm Tới:** [REQ-010]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/StudentCourseBrowseController.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo `StudentCourseBrowseController` với GET `/api/v1/students/courses/available`. Inject `CourseBrowseService` để lấy danh sách khoá học loại trừ các khoá học sinh viên đã đăng ký. Trả về danh sách gồm `courseId`, `title`, `capacity`, `schedule`. Sử dụng `@RolesAllowed({"Student"})`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ triển khai controller duyệt khoá học
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: KIỂM THỬ ĐƠN VỊ SERVICE DUYỆT KHOÁ HỌC
- **Chuyên Môn Hóa Sub-Agent:** [Tester]
- **Mã Thẻ Được Nhắm Tới:** [REQ-010]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseBrowseService.java;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/service/CourseServiceTest.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Bổ sung test case cho `CourseBrowseService.findAvailableCourses(studentId)` xác nhận: trả về danh sách khoá học chưa đăng ký, trả về rỗng nếu sinh viên đã đăng ký hết, xử lý đúng khi studentId null. Mock `CourseRepository.findAvailableCourses`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ kiểm thử service
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: TÀI LIỆU API DUYỆT VÀ ĐĂNG KÝ KHOÁ HỌC
- **Chuyên Môn Hóa Sub-Agent:** [Doc]
- **Mã Thẻ Được Nhắm Tới:** [REQ-010], [REQ-011], [DOC-001]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/api/course-openapi.yaml`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Bổ sung vào `course-openapi.yaml` các endpoint GET `/api/v1/students/courses/available` và POST `/api/v1/enrollments` với mô tả chi tiết response schema, mã lỗi 403/404/409. Tham chiếu `[REQ-010]`, `[REQ-011]`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Tài liệu không yêu cầu migration SQL
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: REVIEW LOGIC DUYỆT KHOÁ HỌC VÀ PERFORMANCE
- **Chuyên Môn Hóa Sub-Agent:** [Reviewer]
- **Mã Thẻ Được Nhắm Tới:** [REQ-010], [NFR-001]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/repository/CourseRepository.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Đánh giá truy vấn `findAvailableCourses` trong `CourseRepository` đảm bảo sử dụng JOIN với bảng `enrollments` và subquery `NOT EXISTS` để loại trừ hiệu quả. Xác nhận có index trên `(student_id, course_id)`. Đề xuất materialized view nếu dữ liệu lớn.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ review
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 5: TRIỂN KHAI ENROLLMENT VÀ SỰ KIỆN KAFKA

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TRIỂN KHAI ENROLLMENT CONTROLLER VÀ SERVICE
- **Chuyên Môn Hóa Sub-Agent:** [Coder]
- **Mã Thẻ Được Nhắm Tới:** [REQ-011], [ARC-007]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/EnrollmentController.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo `EnrollmentController` với POST `/api/v1/enrollments` nhận `EnrollmentRequest`. Trong `EnrollmentService` xử lý: tự sinh user Student nếu chưa tồn tại, kiểm tra khoá học còn capacity, tạo enrollment record, publish Kafka event `enrollment-created` lên topic `enrollment-events`. Trả về 201 với thông tin enrollment.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ triển khai enrollment controller
```

* **Hợp Đồng API Và Định Tuyến Sự Kiện [REQ-XXX], [ARC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: KIỂM THỬ TÍCH HỢP ENROLLMENT
- **Chuyên Môn Hóa Sub-Agent:** [Tester]
- **Mã Thẻ Được Nhắm Tới:** [REQ-011], [ARC-007]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `INTEGRATION_SCOPE;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseServiceIntegrationTestSuite.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Bổ sung integration test trong `CourseServiceIntegrationTestSuite` sử dụng Testcontainers PostgreSQL và Embedded Kafka. Test kịch bản: đăng ký thành công sinh enrollment, sinh viên chưa có được tự tạo, khoá học đầy trả 409, verify Kafka event được publish đúng topic.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ kiểm thử tích hợp
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: TÀI LIỆU LUỒNG ENROLLMENT
- **Chuyên Môn Hóa Sub-Agent:** [Doc]
- **Mã Thẻ Được Nhắm Tới:** [REQ-011], [ARC-008], [DOC-001]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/architecture/course-architecture.md`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Cập nhật `course-architecture.md` bổ sung sơ đồ tuần tự cho luồng đăng ký khoá học, mô tả cách Kafka topic `enrollment-events` được tiêu thụ bởi notification-service để gửi push notification và Zalo. Tham chiếu `[REQ-011]`, `[ARC-008]`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Tài liệu không yêu cầu migration SQL
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: REVIEW XỬ LÝ AUTO-CREATE STUDENT
- **Chuyên Môn Hóa Sub-Agent:** [Reviewer]
- **Mã Thẻ Được Nhắm Tới:** [REQ-011], [EXC-004]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/EnrollmentService.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Đánh giá logic auto-create Student account trong `EnrollmentService` đảm bảo có transaction bao quát, kiểm tra email uniqueness, gán role Student mặc định, sinh password tạm thời an toàn. Đề xuất xử lý race condition khi hai request đồng thời tạo cùng email.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ review
```

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:** Khi sinh viên đăng ký khoá học đã đầy capacity, hệ thống ném `CourseFullException` mã lỗi `COURSE_FULL_409` kèm thông điệp `"Khoá học đã đạt số lượng sinh viên tối đa"`. Khi khoá học không tồn tại hoặc đã kết thúc, ném `EnrollmentNotFoundException` mã `COURSE_NOT_FOUND_404`. Khi sinh viên đã đăng ký trước đó, ném `DuplicateEnrollmentException` mã `ALREADY_ENROLLED_409`. Tất cả ngoại lệ được ánh xạ về HTTP status tương ứng thông qua `@ApplicationException` hoặc `ExceptionMapper`.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 6: KHỞI TẠO ATTENDANCE-SERVICE VÀ MODULE GIẢI MÃ QR

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TRIỂN KHAI MÔ TẢ BUILD VÀ ỨNG DỤNG ATTENDANCE-SERVICE
- **Chuyên Môn Hóa Sub-Agent:** [Coder]
- **Mã Thẻ Được Nhắm Tới:** [ARC-000], [REQ-012]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/pom.xml`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh tệp `pom.xml` cho `attendance-service` thừa kế parent `./sources/backend/pom.xml`. Khai báo dependency Quarkus RESTEasy Reactive, Hibernate ORM Panache, PostgreSQL JDBC, SmallRye Reactive Messaging Kafka, Jakarta Validation, JUnit 5, Caffeine cache. ArtifactId `attendance-service`, groupId `org.nlh4j.membershiphub`. Tệp phải biên dịch trống.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Migration V1__attendance_init.sql [DAT-006]
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
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: KIỂM THỬ TÍCH HỢP MÔ TẢ BUILD ATTENDANCE-SERVICE
- **Chuyên Môn Hóa Sub-Agent:** [Tester]
- **Mã Thẻ Được Nhắm Tới:** [ARC-000], [REQ-012]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `INTEGRATION_SCOPE;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceIntegrationTestSuite.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo test suite tích hợp Maven sử dụng JUnit 5 Platform Launcher kết nối `./sources/infra/test/maven-build-integration.sh` để xác minh `./sources/backend/attendance-service/pom.xml` biên dịch sạch. Test phải fail nếu dependency chưa khả dụng.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ kiểm thử tích hợp mô tả build
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: TÀI LIỆU KIẾN TRÚC ATTENDANCE-SERVICE
- **Chuyên Môn Hóa Sub-Agent:** [Doc]
- **Mã Thẻ Được Nhắm Tới:** [REQ-012], [REQ-013], [ARC-007], [DOC-001]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/architecture/attendance-architecture.md`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tài liệu Markdown mô tả kiến trúc `attendance-service` gồm sơ đồ C4 Container, luồng xử lý QR scan, cơ chế idempotency, retry queue khi mất mạng `[EXC-001]`, FIFO khi khôi phục `[EXC-005]`. Tài liệu phải liệt kê đầy đủ Tag ID `[REQ-012]`, `[REQ-013]`, `[ARC-007]`, `[EXC-001]`, `[EXC-002]`, `[EXC-005]`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Tài liệu không yêu cầu migration SQL
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: REVIEW MÃ NGUỒN KHỞI TẠO ATTENDANCE-SERVICE
- **Chuyên Môn Hóa Sub-Agent:** [Reviewer]
- **Mã Thẻ Được Nhắm Tới:** [ARC-000], [REQ-012]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceApplication.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Đánh giá tệp `AttendanceServiceApplication.java` chứa annotation `@QuarkusMain`, đảm bảo cấu hình gói `org.nlh4j.membershiphub.attendanceservice` chính xác. Xác nhận không có tham chiếu `com.example`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ review
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 7: TRIỂN KHAI LUỒNG ĐIỂM DANH QR VỚI IDEMPOTENCY VÀ RETRY

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TRIỂN KHAI ATTENDANCE CONTROLLER VÀ SERVICE
- **Chuyên Môn Hóa Sub-Agent:** [Coder]
- **Mã Thẻ Được Nhắm Tới:** [REQ-012], [REQ-013], [ARC-007]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/controller/AttendanceController.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo `AttendanceController` với POST `/api/v1/attendance/scan` nhận `QrScanRequest` chứa `qrPayload` (base64) và `idempotencyKey`. Inject `AttendanceService` và `QrPayloadDecoder`. Service thực hiện: giải mã payload lấy `studentId` và `courseId`, kiểm tra enrollment tồn tại, kiểm tra idempotency qua composite unique key, tạo attendance record. Trả về 201 với `AttendanceResponse` có flag `duplicate` nếu đã tồn tại.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ triển khai controller
```

* **Hợp Đồng API Và Định Tuyến Sự Kiện [REQ-XXX], [ARC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: KIỂM THỬ ĐƠN VỊ ATTENDANCE SERVICE
- **Chuyên Môn Hóa Sub-Agent:** [Tester]
- **Mã Thẻ Được Nhắm Tới:** [REQ-012], [REQ-013], [ARC-007]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/AttendanceService.java;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/service/AttendanceServiceTest.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo `AttendanceServiceTest.java` sử dụng JUnit 5 và Mockito. Test các kịch bản: quét QR thành công tạo attendance mới, quét trùng trong ngày trả duplicate flag, sinh viên chưa enroll ném `EnrollmentRequiredException`, payload không hợp lệ ném exception. Mock `AttendanceRepository`, `EnrollmentRepository`, `KafkaAttendanceProducer`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ kiểm thử service
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: TÀI LIỆU API VÀ LUỒNG RETRY CHO ATTENDANCE
- **Chuyên Môn Hóa Sub-Agent:** [Doc]
- **Mã Thẻ Được Nhắm Tới:** [REQ-012], [REQ-013], [EXC-001], [EXC-005], [DOC-001]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/api/attendance-openapi.yaml`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tệp OpenAPI 3.1 mô tả endpoint POST `/api/v1/attendance/scan` với schema `QrScanRequest`, `AttendanceResponse`, mã lỗi 400/403/409. Bổ sung mô tả cơ chế retry queue khi mất mạng `[EXC-001]` và FIFO khi khôi phục `[EXC-005]`. Tham chiếu Tag ID đầy đủ.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Tài liệu không yêu cầu migration SQL
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: REVIEW LOGIC IDEMPOTENCY VÀ XỬ LÝ NGOẠI LỆ
- **Chuyên Môn Hóa Sub-Agent:** [Reviewer]
- **Mã Thẻ Được Nhắm Tới:** [REQ-013], [EXC-001], [EXC-002], [EXC-005]
- **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/exception/DuplicateAttendanceException.java`
- **Chỉ Thị Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Đánh giá `DuplicateAttendanceException` đảm bảo idempotency thông qua composite unique key `(student_id, course_id, attendance_date)`. Xác nhận cơ chế retry queue xử lý `[EXC-001]` và FIFO recovery `[EXC-005]`. Đề xuất tối ưu performance cho truy vấn tần suất cao.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ review
```

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:** Khi sinh viên quét QR nhưng chưa đăng ký khoá học, hệ thống ném `EnrollmentRequiredException` mã lỗi `ENROLLMENT_REQUIRED_403` kèm thông điệp `"Sinh viên chưa đăng ký khoá học này"`. Khi quét trùng trong cùng ngày, hệ thống trả về success với `duplicate: true` chứ không tạo bản ghi mới `[EXC-002]`. Khi mạng không khả dụng, request được đẩy vào retry queue tối đa 3 lần `[EXC-001]`. Khi dịch vụ khôi phục sau outage, các request pending được xử lý theo thứ tự FIFO `[EXC-005]`. Khi payload QR không hợp lệ (base64 decode lỗi, thiếu trường), ném `InvalidQrPayloadException` mã `INVALID_QR_400`.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

<!--PHASE_INDEX_START-->

### 📈 Giai đoạn 4 - Tích hợp thẻ thành viên, đa kênh thông báo, khuyến mãi, AI Chatbot và hợp đồng Mobile
- **Mục Tiêu & Phạm Vi Cốt Lõi Của Giai Đoạn:** Triển khai hệ thống quản lý thẻ thành viên số với logic gia hạn, cơ chế kích hoạt thông báo đa kênh thông qua Kafka, quản lý khuyến mãi và thông báo chung, tích hợp AI Chatbot thông minh dựa trên Vertex AI, hoàn thiện hợp đồng tích hợp mobile app chuẩn OpenAPI 3.1 với cơ chế offline cache. Giai đoạn này đảm bảo toàn bộ luồng tương tác người dùng - hệ thống - thiết bị di động - Zalo - AI đều hoạt động đồng bộ với khả năng phục hồi lỗi theo chuẩn OWASP.

- **Bản Đồ Ma Trận Thư Mục Vật Lý Mục Tiêu:**
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/StudentCardController.java` - [REQ-014], [REQ-015]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/StudentCardService.java` - [REQ-014], [REQ-015]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/StudentCardResponse.java` - [REQ-014]
    * `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/CardRenewalRequest.java` - [REQ-015]
    * `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/StudentCardControllerTest.java` - [REQ-014], [REQ-015]
    * `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/PromotionController.java` - [REQ-017]
    * `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/AnnouncementController.java` - [REQ-018]
    * `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/PromotionService.java` - [REQ-017]
    * `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/AnnouncementService.java` - [REQ-018]
    * `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/PromotionRequest.java` - [REQ-017]
    * `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/AnnouncementRequest.java` - [REQ-018]
    * `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/PromotionServiceTest.java` - [REQ-017]
    * `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/AnnouncementServiceTest.java` - [REQ-018]
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/controller/NotificationController.java` - [REQ-016], [ARC-008]
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/controller/DeviceController.java` - [REQ-021]
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/NotificationDispatcherService.java` - [REQ-016], [ARC-008], [EXC-003]
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/PushDeliveryService.java` - [REQ-021], [EXC-003]
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/integration/ZaloBotClient.java` - [ARC-008]
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/integration/FcmClient.java` - [REQ-021]
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/NotificationDispatchRequest.java` - [REQ-016]
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/dto/DeviceRegistrationRequest.java` - [REQ-021]
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/kafka/NotificationEventProducer.java` - [ARC-008]
    * `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/kafka/NotificationEventConsumer.java` - [ARC-008], [EXC-003]
    * `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/NotificationDispatcherServiceTest.java` - [REQ-016], [EXC-003]
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/ChatbotController.java` - [REQ-019]
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/ChatbotService.java` - [REQ-019]
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/integration/VertexAiClient.java` - [REQ-019]
    * `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/ChatbotQueryRequest.java` - [REQ-019]
    * `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/ChatbotServiceTest.java` - [REQ-019]
    * `./sources/frontend/web-app/src/lib/api/membershipHubClient.ts` - [ARC-009]
    * `./sources/frontend/web-app/src/lib/api/notifications.ts` - [ARC-008], [REQ-021]
    * `./sources/frontend/web-app/src/lib/offline/cacheService.ts` - [ARC-009]
    * `./sources/frontend/web-app/src/sw/service-worker.ts` - [ARC-009]
    * `./sources/frontend/web-app/src/types/notification.d.ts` - [ARC-008]
    * `./sources/frontend/web-app/src/types/memberCard.d.ts` - [ARC-009]
    * `./sources/frontend/web-app/src/test/notifications.spec.ts` - [ARC-008]
    * `./sources/frontend/web-app/src/test/cacheService.spec.ts` - [ARC-009]
    * `./sources/docs/contracts/notification-queue.openapi.yaml` - [ARC-008], [REQ-016], [DOC-001]
    * `./sources/docs/contracts/mobile-app.openapi.yaml` - [ARC-009], [DOC-001]

- **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Migration V4__extend_member_cards_notifications.sql
-- Mở rộng bảng StudentCards hỗ trợ gia hạn, theo dõi EndDate và lịch sử gia hạn
ALTER TABLE StudentCards
    ADD COLUMN end_date DATE NOT NULL DEFAULT CURRENT_DATE,
    ADD COLUMN used_days INT NOT NULL DEFAULT 0,
    ADD COLUMN total_validity_days INT NOT NULL DEFAULT 30,
    ADD COLUMN last_renewed_at TIMESTAMP NULL,
    ADD COLUMN renewal_count INT NOT NULL DEFAULT 0;

CREATE INDEX idx_student_cards_student_id ON StudentCards(student_id);
CREATE INDEX idx_student_cards_end_date ON StudentCards(end_date);

-- Bảng lịch sử gia hạn thẻ phục vụ audit [REQ-015]
CREATE TABLE CardRenewalHistory (
    renewal_id UUID PRIMARY KEY,
    card_id UUID NOT NULL,
    student_id UUID NOT NULL,
    renewal_days INT NOT NULL CHECK (renewal_days BETWEEN 1 AND 365),
    previous_end_date DATE NOT NULL,
    new_end_date DATE NOT NULL,
    payment_reference VARCHAR(100) NOT NULL,
    renewed_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_renewal_card FOREIGN KEY (card_id) REFERENCES StudentCards(card_id) ON DELETE CASCADE,
    CONSTRAINT fk_renewal_student FOREIGN KEY (student_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_renewal_card_id ON CardRenewalHistory(card_id);
CREATE INDEX idx_renewal_student_id ON CardRenewalHistory(student_id);

-- Bảng Promotions theo [REQ-017]
CREATE TABLE Promotions (
    promo_id UUID PRIMARY KEY,
    center_id UUID NULL,
    code VARCHAR(30) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    discount_percent SMALLINT NOT NULL CHECK (discount_percent BETWEEN 1 AND 100),
    start_date DATE NULL,
    end_date DATE NULL,
    is_perpetual BOOLEAN NOT NULL DEFAULT false,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_promotion_center FOREIGN KEY (center_id) REFERENCES Centers(center_id) ON DELETE SET NULL,
    CONSTRAINT fk_promotion_creator FOREIGN KEY (created_by) REFERENCES Users(user_id),
    CONSTRAINT chk_promotion_dates CHECK (
        (is_perpetual = true) OR
        (is_perpetual = false AND start_date IS NOT NULL AND end_date IS NOT NULL AND end_date >= start_date)
    )
);

CREATE INDEX idx_promotions_center_id ON Promotions(center_id);
CREATE INDEX idx_promotions_date_range ON Promotions(start_date, end_date);

-- Bảng Announcements theo [REQ-018]
CREATE TABLE Announcements (
    announcement_id UUID PRIMARY KEY,
    center_id UUID NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    start_date DATE NULL,
    expiry_date DATE NULL,
    target_audience VARCHAR(20) NOT NULL DEFAULT 'ALL',
    published_by UUID NOT NULL,
    published_at TIMESTAMP NOT NULL DEFAULT now(),
    is_active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT fk_announcement_center FOREIGN KEY (center_id) REFERENCES Centers(center_id) ON DELETE SET NULL,
    CONSTRAINT fk_announcement_publisher FOREIGN KEY (published_by) REFERENCES Users(user_id),
    CONSTRAINT chk_announcement_target CHECK (target_audience IN ('ALL', 'STUDENT', 'TEACHER', 'ADMIN'))
);

CREATE INDEX idx_announcements_center_id ON Announcements(center_id);
CREATE INDEX idx_announcements_active_expiry ON Announcements(is_active, expiry_date);

-- Bảng NotificationDispatch theo [REQ-016], [ARC-008]
CREATE TABLE NotificationDispatch (
    dispatch_id UUID PRIMARY KEY,
    notification_type VARCHAR(30) NOT NULL,
    target_user_id UUID NULL,
    target_group_zalo VARCHAR(50) NULL,
    target_device_token VARCHAR(255) NULL,
    message_title VARCHAR(200) NOT NULL,
    message_body TEXT NOT NULL,
    payload JSONB NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 3,
    last_attempt_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_dispatch_user FOREIGN KEY (target_user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_dispatch_type CHECK (notification_type IN ('PUSH', 'ZALO_GROUP', 'IN_APP')),
    CONSTRAINT chk_dispatch_status CHECK (status IN ('PENDING', 'DELIVERED', 'FAILED', 'DEAD_LETTER'))
);

CREATE INDEX idx_dispatch_status ON NotificationDispatch(status);
CREATE INDEX idx_dispatch_target_user ON NotificationDispatch(target_user_id);
CREATE INDEX idx_dispatch_retry ON NotificationDispatch(status, attempt_count) WHERE status = 'PENDING';

-- Bảng DeviceToken theo [REQ-021]
CREATE TABLE DeviceToken (
    device_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    device_token VARCHAR(255) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    app_version VARCHAR(30) NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT now(),
    last_used_at TIMESTAMP NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT fk_device_user FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_device_token UNIQUE (device_token),
    CONSTRAINT chk_device_platform CHECK (platform IN ('IOS', 'ANDROID', 'WEB'))
);

CREATE INDEX idx_device_user_active ON DeviceToken(user_id, is_active);

-- Bảng ChatbotSession theo [REQ-019]
CREATE TABLE ChatbotSession (
    session_id UUID PRIMARY KEY,
    user_id UUID NULL,
    session_token VARCHAR(100) NOT NULL UNIQUE,
    context JSONB NULL,
    message_count INT NOT NULL DEFAULT 0,
    escalated_to_human BOOLEAN NOT NULL DEFAULT false,
    last_activity_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_chatbot_user FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_chatbot_session_token ON ChatbotSession(session_token);
CREATE INDEX idx_chatbot_expiry ON ChatbotSession(expires_at);

-- Bảng ChatbotMessage lưu lịch sử hội thoại
CREATE TABLE ChatbotMessage (
    message_id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    sender_role VARCHAR(20) NOT NULL,
    message_content TEXT NOT NULL,
    confidence_score DECIMAL(5,4) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_message_session FOREIGN KEY (session_id) REFERENCES ChatbotSession(session_id) ON DELETE CASCADE,
    CONSTRAINT chk_message_sender CHECK (sender_role IN ('USER', 'BOT', 'HUMAN_AGENT'))
);

CREATE INDEX idx_message_session_created ON ChatbotMessage(session_id, created_at);
```

- **Hợp Đồng API Và Định Tuyến Sự Kiện [REQ-XXX], [ARC-XXX]:**

```json
{
  "openapi": "3.1.0",
  "info": {
    "title": "Membership Hub Notification & Member Card API",
    "version": "1.0.0"
  },
  "paths": {
    "/api/v1/students/{studentId}/card": {
      "get": {
        "tags": ["StudentCard"],
        "summary": "Lấy thông tin thẻ thành viên của học viên",
        "parameters": [
          {
            "name": "studentId",
            "in": "path",
            "required": true,
            "schema": { "type": "string", "format": "uuid" }
          }
        ],
        "responses": {
          "200": {
            "description": "Trả về thông tin thẻ thành viên",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/StudentCardResponse" }
              }
            }
          },
          "404": { "description": "Không tìm thấy thẻ thành viên" }
        }
      }
    },
    "/api/v1/students/{studentId}/card/renew": {
      "post": {
        "tags": ["StudentCard"],
        "summary": "Gia hạn thẻ thành viên với số ngày từ 1-365",
        "parameters": [
          {
            "name": "studentId",
            "in": "path",
            "required": true,
            "schema": { "type": "string", "format": "uuid" }
          }
        ],
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/CardRenewalRequest" }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Gia hạn thành công",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/StudentCardResponse" }
              }
            }
          },
          "400": { "description": "Số ngày gia hạn không hợp lệ" },
          "402": { "description": "Thanh toán thất bại" }
        }
      }
    },
    "/api/v1/notifications/dispatch": {
      "post": {
        "tags": ["Notification"],
        "summary": "Kích hoạt thông báo đa kênh (push + Zalo)",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/NotificationDispatchRequest" }
            }
          }
        },
        "responses": {
          "202": { "description": "Đã đưa vào hàng đợi xử lý" },
          "400": { "description": "Payload không hợp lệ" }
        }
      }
    },
    "/api/v1/devices/register": {
      "post": {
        "tags": ["Device"],
        "summary": "Đăng ký device token cho push notification",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/DeviceRegistrationRequest" }
            }
          }
        },
        "responses": {
          "200": { "description": "Đăng ký thành công" },
          "409": { "description": "Device token đã tồn tại" }
        }
      }
    },
    "/api/v1/chatbot/query": {
      "post": {
        "tags": ["Chatbot"],
        "summary": "Gửi câu hỏi tới AI Chatbot",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/ChatbotQueryRequest" }
            }
          }
        },
        "responses": {
          "200": { "description": "Trả về câu trả lời từ AI hoặc escalate" }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "StudentCardResponse": {
        "type": "object",
        "properties": {
          "cardId": { "type": "string", "format": "uuid" },
          "studentId": { "type": "string", "format": "uuid" },
          "issueDate": { "type": "string", "format": "date" },
          "endDate": { "type": "string", "format": "date" },
          "totalValidityDays": { "type": "integer" },
          "usedDays": { "type": "integer" },
          "remainingDays": { "type": "integer" },
          "renewalCount": { "type": "integer" }
        }
      },
      "CardRenewalRequest": {
        "type": "object",
        "required": ["renewalDays", "paymentReference"],
        "properties": {
          "renewalDays": { "type": "integer", "minimum": 1, "maximum": 365 },
          "paymentReference": { "type": "string", "maxLength": 100 }
        }
      },
      "NotificationDispatchRequest": {
        "type": "object",
        "required": ["notificationType", "messageTitle", "messageBody"],
        "properties": {
          "notificationType": { "type": "string", "enum": ["PUSH", "ZALO_GROUP", "IN_APP"] },
          "targetUserId": { "type": "string", "format": "uuid" },
          "targetGroupZalo": { "type": "string", "maxLength": 50 },
          "messageTitle": { "type": "string", "maxLength": 200 },
          "messageBody": { "type": "string" },
          "payload": { "type": "object", "additionalProperties": true }
        }
      },
      "DeviceRegistrationRequest": {
        "type": "object",
        "required": ["deviceToken", "platform"],
        "properties": {
          "deviceToken": { "type": "string", "maxLength": 255 },
          "platform": { "type": "string", "enum": ["IOS", "ANDROID", "WEB"] },
          "appVersion": { "type": "string", "maxLength": 30 }
        }
      },
      "ChatbotQueryRequest": {
        "type": "object",
        "required": ["sessionToken", "question"],
        "properties": {
          "sessionToken": { "type": "string", "maxLength": 100 },
          "question": { "type": "string", "maxLength": 1000 }
        }
      }
    }
  }
}
```

```yaml
# Kafka Topic: notification-queue
# Schema đăng ký qua Confluent Schema Registry - JSON Schema format
notification_queue_schema:
  type: object
  required:
    - dispatchId
    - notificationType
    - messageTitle
    - messageBody
    - createdAt
  properties:
    dispatchId:
      type: string
      format: uuid
    notificationType:
      type: string
      enum: [PUSH, ZALO_GROUP, IN_APP]
    targetUserId:
      type: string
      format: uuid
    targetGroupZalo:
      type: string
      maxLength: 50
    targetDeviceToken:
      type: string
      maxLength: 255
    messageTitle:
      type: string
      maxLength: 200
    messageBody:
      type: string
    payload:
      type: object
      additionalProperties: true
    attemptCount:
      type: integer
      minimum: 0
      maximum: 3
    createdAt:
      type: string
      format: date-time
kafka_topic_config:
  name: notification-queue
  partitions: 6
  replicationFactor: 3
  retentionMs: 604800000
  cleanupPolicy: delete
  compressionType: snappy
```

- **Trình Xử Lý Ngoại Lệ Cục Bộ Hóa Cho Giai Đoạn [EXC-XXX]:**

| Mã Ngoại Lệ | Kịch Bản Kích Hoạt | Phản Hồi Trả Về | Cơ Chế Phục Hồi |
| :--- | :--- | :--- | :--- |
| [EXC-003] | Push notification không thể gửi tới thiết bị do device token không hợp lệ hoặc FCM/APNs trả lỗi | HTTP 200 với `status: RETRY_SCHEDULED`, ghi log chi tiết lỗi | Retry tối đa 3 lần với exponential backoff (1 phút, 5 phút, 15 phút), sau đó chuyển trạng thái `DEAD_LETTER` và ghi audit log |
| [EXC-004] | Validation thất bại khi gia hạn thẻ: `renewalDays` ngoài khoảng 1-365, thiếu `paymentReference` | HTTP 400 với danh sách field lỗi cụ thể, mã `INVALID_RENEWAL_PAYLOAD` | Client nhận response và hiển thị form validation, không retry tự động |
| [EXC-003] | Zalo Bot gửi tin nhắn thất bại do group không tồn tại hoặc token hết hạn | HTTP 200 với `status: FAILED`, dispatch_id được lưu để admin xử lý thủ công | Gửi email cảnh báo tới Center Admin, log error chi tiết với mã `ZALO_DELIVERY_FAILED` |
| [EXC-004] | Chatbot session đã hết hạn (timeout 30 phút không hoạt động) khi nhận query mới | HTTP 401 với mã `CHATBOT_SESSION_EXPIRED`, trả về session mới | Client tự động tạo session mới và gửi lại câu hỏi cuối cùng |
| [EXC-004] | Vertex AI trả về lỗi quota exceeded hoặc timeout | HTTP 503 với mã `AI_SERVICE_UNAVAILABLE`, đề xuất retry sau 30 giây | Fallback message "Hệ thống đang bận, vui lòng thử lại sau" và escalation flag |

#### 📅 Nhật Ký Phân Bổ Nhiệm Vụ Theo Ngày Cho Từng Sub-Agent (Giai đoạn 4)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: Triển khai StudentCard API và gia hạn thẻ thành viên

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: Xây dựng REST endpoint xem thông tin thẻ thành viên

* **Chuyên Môn Hóa Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [REQ-014]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/StudentCardController.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Triển khai controller với annotation `@Path("/api/v1/students/{studentId}/card")` và method `@GET`, sử dụng `@PathParam` cho studentId, inject `StudentCardService` qua CDI. Trả về DTO `StudentCardResponse` chứa các trường `cardId`, `studentId`, `issueDate`, `endDate`, `totalValidityDays`, `usedDays`, `remainingDays`, `renewalCount`. Áp dụng `@RolesAllowed({"STUDENT", "CENTER_ADMIN", "SYSTEM_ADMIN"})` và kiểm tra student chỉ được xem thẻ của chính mình trừ khi role là admin. Tích hợp Bean Validation `@NotNull` cho path variable. Mapping tự động từ entity `StudentCards` sang DTO response. Trả về HTTP 404 với mã `CARD_NOT_FOUND` khi không tìm thấy thẻ. Truy vết đầy đủ theo [REQ-014].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Không có thay đổi schema trong sub-task này
-- Sử dụng bảng StudentCards đã được tạo trong Phase 1
SELECT card_id, student_id, issue_date, end_date, total_validity_days, used_days, renewal_count
FROM StudentCards
WHERE student_id = :studentId
  AND is_active = true;
```

* **Hợp Đồng API Và Định Tuyến Sự Kiện [REQ-XXX], [ARC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: Kiểm thử đơn vị cho StudentCardController

* **Chuyên Môn Hóa Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [REQ-014]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/StudentCardController.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/StudentCardControllerTest.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sinh test class JUnit 5 với `@QuarkusTest` annotation. Sử dụng `@InjectMock` để mock `StudentCardService`. Test case 1: `testGetCard_Success` - gọi GET endpoint với studentId hợp lệ, mock service trả về `StudentCardResponse` mẫu, verify HTTP 200 và JSON body chứa đầy đủ trường. Test case 2: `testGetCard_NotFound` - mock service throw `NotFoundException`, verify HTTP 404 với error code `CARD_NOT_FOUND`. Test case 3: `testGetCard_Forbidden` - gọi với student khác, verify HTTP 403. Sử dụng `RestAssured` để gọi endpoint, assert JSON path với `jsonPath()`. Verify authorization header được gửi đúng. Truy vết [REQ-014].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Test scope không yêu cầu migration
-- Sử dụng H2 in-memory database cho test với schema đã sync
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: Tài liệu hóa API thẻ thành viên

* **Chuyên Môn Hóa Sub-Agent:** [Doc]
* **Mã Thẻ Truy Vết:** [REQ-014], [DOC-001]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/contracts/member-card.openapi.yaml`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sinh file OpenAPI 3.1 YAML mô tả đầy đủ endpoint `GET /api/v1/students/{studentId}/card`. Bao gồm: mô tả endpoint, parameters (path, query, header), request body schema, response schemas (200, 401, 403, 404), ví dụ JSON cho mỗi response, security scheme BearerAuth. Thêm section xác thực phân quyền giải thích rằng chỉ student sở hữu hoặc admin mới có quyền truy cập. Bổ sung ví dụ curl command để gọi API. Truy vết [REQ-014] và [DOC-001].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Tài liệu không yêu cầu migration vật lý
-- Phần này chỉ tham chiếu bảng StudentCards trong phần mô tả
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: Triển khai endpoint gia hạn thẻ thành viên

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: Xây dựng logic gia hạn thẻ với validation renewal_days

* **Chuyên Môn Hóa Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [REQ-015], [EXC-004]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/StudentCardService.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Thêm method `renewCard(UUID studentId, CardRenewalRequest request)` vào `StudentCardService`. Bước 1: Validate `renewalDays` trong khoảng 1-365 (ném `ConstraintViolationException` nếu sai theo [EXC-004]). Bước 2: Tìm `StudentCard` active theo studentId, throw `NotFoundException` nếu không tồn tại. Bước 3: Lưu giá trị `previousEndDate`, tính `newEndDate = currentEndDate + renewalDays`. Bước 4: Cập nhật entity với `endDate = newEndDate`, `renewalCount = renewalCount + 1`, `lastRenewedAt = now()`. Bước 5: Tạo bản ghi `CardRenewalHistory` với `previousEndDate`, `newEndDate`, `paymentReference`. Bước 6: Publish event `card-renewed` lên Kafka topic `notification-queue` để worker gửi thông báo xác nhận. Sử dụng `@Transactional` để đảm bảo ACID. Truy vết [REQ-015].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Sử dụng bảng CardRenewalHistory đã tạo trong phần DDL giai đoạn
-- Logic tính toán end_date mới:
UPDATE StudentCards
SET end_date = end_date + INTERVAL ':renewalDays' DAY,
    renewal_count = renewal_count + 1,
    last_renewed_at = now(),
    updated_at = now()
WHERE card_id = :cardId
  AND is_active = true;
```

* **Hợp Đồng API Và Định Tuyến Sự Kiện [REQ-XXX], [ARC-XXX]:**

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

* **Trình Xử Lý Ngoại Lệ Cục Bộ Hóa [EXC-XXX]:**

```java
// Xử lý theo [EXC-004]: validation thất bại
if (request.getRenewalDays() < 1 || request.getRenewalDays() > 365) {
    throw new ConstraintViolationException(
        "renewalDays phải nằm trong khoảng 1-365",
        Set.of(ConstraintViolationImpl.forField("renewalDays"))
    );
}
```

<!--ATOMIC_SUB_TASK_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: Xây dựng REST endpoint gia hạn thẻ

* **Chuyên Môn Hóa Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [REQ-015]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/StudentCardController.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Thêm method `@POST` với `@Path("/{studentId}/card/renew")` trong `StudentCardController`. Sử dụng `@Valid` annotation cho `CardRenewalRequest` body. Inject `StudentCardService`. Trả về `Response.status(Response.Status.OK).entity(updatedCardResponse).build()`. Annotation `@RolesAllowed({"STUDENT"})` và kiểm tra student chỉ gia hạn thẻ của chính mình thông qua `@Context SecurityContext`. Xử lý exception bằng `@ExceptionHandler` cho `ConstraintViolationException` trả về HTTP 400 với danh sách field lỗi. Truy vết [REQ-015].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Endpoint layer không trực tiếp thao tác DDL
-- Logic được delegate xuống service layer
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Hóa [EXC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: Kiểm thử tích hợp cho luồng gia hạn thẻ

* **Chuyên Môn Hóa Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [REQ-015], [EXC-004]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** INTEGRATION_SCOPE;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/CardRenewalIntegrationTest.java
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sinh integration test sử dụng `@QuarkusTest` với test profile active H2. Test scenario 1: `testRenewCard_Success` - tạo StudentCard mẫu, gọi POST renew với `renewalDays=30`, verify response 200 và `endDate` được cộng thêm 30 ngày, kiểm tra `CardRenewalHistory` được tạo với `previousEndDate` và `newEndDate` đúng. Test scenario 2: `testRenewCard_InvalidDays` theo [EXC-004] - gọi với `renewalDays=400`, verify HTTP 400 với error code `INVALID_RENEWAL_PAYLOAD`. Test scenario 3: `testRenewCard_PaymentFailed` - mock payment gateway trả lỗi, verify HTTP 402 và không có thay đổi DB. Test scenario 4: `testRenewCard_ConcurrentRenew` - sử dụng `CompletableFuture` chạy 2 request đồng thời, verify optimistic locking xử lý đúng. Truy vết [REQ-015] và [EXC-004].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Test scope sử dụng H2 in-memory với schema tương đương production
-- Không cần migration mới
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Hóa [EXC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 3: Triển khai Notification Dispatcher và Kafka Producer

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: Xây dựng Kafka producer cho notification-queue

* **Chuyên Môn Hóa Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [ARC-008], [REQ-016]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/kafka/NotificationEventProducer.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo class `NotificationEventProducer` sử dụng SmallRye Reactive Messaging với `@Channel("notification-queue")` và `Emitter<NotificationEvent>`. Method `dispatch(NotificationDispatchRequest request)` chuyển đổi request thành `NotificationEvent` với UUID `dispatchId`, thời gian `createdAt`, validate enum `notificationType`. Sử dụng `Multi<NotificationEvent>` để hỗ trợ fan-out. Cấu hình backpressure với buffer size 256. Log structured với MDC tracking `dispatchId`. Implement interface `HealthCheck` để theo dõi trạng thái Kafka broker. Truy vết [ARC-008] và [REQ-016].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Không yêu cầu migration mới
-- Producer ghi message trực tiếp lên Kafka topic
```

* **Hợp Đồng API Và Định Tuyến Sự Kiện [REQ-XXX], [ARC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: Xây dựng Kafka consumer cho notification-queue

* **Chuyên Môn Hóa Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [ARC-008], [EXC-003]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/kafka/NotificationEventConsumer.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo class `NotificationEventConsumer` với annotation `@Incoming("notification-queue")` nhận `NotificationEvent`. Implement logic xử lý theo `notificationType`: nếu `PUSH` gọi `FcmClient.sendPush`, nếu `ZALO_GROUP` gọi `ZaloBotClient.postMessage`, nếu `IN_APP` lưu vào `NotificationDispatch`. Bọc logic trong try-catch theo [EXC-003]: nếu gửi thất bại, tăng `attemptCount`, nếu `attemptCount < maxAttempts` (3) thì re-emit message với delay exponential backoff, nếu `attemptCount >= 3` thì đánh dấu `DEAD_LETTER`. Cập nhật `NotificationDispatch.status` tương ứng. Sử dụng `@Acknowledgment` để manual commit offset sau khi xử lý thành công. Truy vết [ARC-008] và [EXC-003].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Cập nhật trạng thái dispatch sau khi xử lý
UPDATE NotificationDispatch
SET status = :status,
    attempt_count = attempt_count + 1,
    last_attempt_at = now(),
    delivered_at = CASE WHEN :status = 'DELIVERED' THEN now() ELSE delivered_at END
WHERE dispatch_id = :dispatchId;
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Hóa [EXC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: Kiểm thử cho luồng notification dispatcher

* **Chuyên Môn Hóa Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [REQ-016], [ARC-008], [EXC-003]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/NotificationDispatcherService.java;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/NotificationDispatcherServiceTest.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sinh test class JUnit 5 với `@QuarkusTest`. Test case 1: `testDispatch_PushType` - gọi `dispatcher.dispatch()` với type=PUSH, verify Kafka message được emit với schema đúng. Test case 2: `testConsume_PushDeliverySuccess` - feed message vào consumer, mock FcmClient thành công, verify NotificationDispatch status=DELIVERED. Test case 3 theo [EXC-003]: `testConsume_RetryOnFailure` - mock FcmClient throw exception lần 1, verify `attemptCount=1` và message được schedule retry. Test case 4 theo [EXC-003]: `testConsume_DeadLetterAfter3Attempts` - giả lập 3 lần fail liên tiếp, verify status=DEAD_LETTER và dead letter publisher được gọi. Sử dụng `InMemoryConnector` cho test Kafka. Truy vết [REQ-016], [ARC-008] và [EXC-003].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Test scope sử dụng embedded Kafka và H2 in-memory
-- Không yêu cầu migration vật lý
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Hóa [EXC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 4: Triển khai FCM Client, Zalo Bot Client và Push Delivery

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: Xây dựng FCM integration cho push notification

* **Chuyên Môn Hóa Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [REQ-021], [EXC-003]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/integration/FcmClient.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo class `FcmClient` sử dụng Firebase Admin SDK. Inject `FirebaseMessaging` instance thông qua CDI producer. Method `sendPush(String deviceToken, String title, String body, Map<String, String> data)` xây dựng `Message` object với `Notification` (title, body) và `AndroidConfig`/`ApnsConfig` tùy platform. Gọi `FirebaseMessaging.getInstance().send(message)` và trả về message ID. Xử lý exception theo [EXC-003]: bắt `FirebaseMessagingException` với mã lỗi `UNREGISTERED` hoặc `INVALID_ARGUMENT` thì throw `InvalidDeviceTokenException` (không retry), các mã khác throw `DeliveryException` (retry được). Cấu hình timeout 10 giây cho mỗi request. Truy vết [REQ-021] và [EXC-003].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Đánh dấu device token không hợp lệ khi FCM trả lỗi UNREGISTERED
UPDATE DeviceToken
SET is_active = false,
    last_used_at = now()
WHERE device_token = :token;
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Hóa [EXC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: Xây dựng Zalo Bot Client

* **Chuyên Môn Hóa Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [ARC-008], [EXC-003]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/integration/ZaloBotClient.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo class `ZaloBotClient` sử dụng `RESTClient` (MicroProfile Rest Client) gọi Zalo Bot API. Method `postMessage(String groupId, String message)` gửi POST request tới endpoint `https://bot-api.zalo.me/v2/message` với body JSON `{recipient: {group_id: groupId}, message: {text: message}}`. Inject access token từ config `mp.rest.client.zalo.token`. Xử lý response theo [EXC-003]: nếu HTTP 401 token hết hạn, refresh token qua Zalo OAuth2 rồi retry 1 lần; nếu HTTP 404 group không tồn tại, throw `ZaloGroupNotFoundException`; các lỗi khác throw `DeliveryException`. Cấu hình timeout 15 giây. Sử dụng `@CircuitBreaker` để tránh gọi liên tục khi Zalo API down. Truy vết [ARC-008] và [EXC-003].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Log lỗi gửi Zalo
INSERT INTO NotificationDispatch (dispatch_id, notification_type, target_group_zalo, status, attempt_count, created_at)
VALUES (:dispatchId, 'ZALO_GROUP', :groupId, 'FAILED', 1, now());
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Hóa [EXC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: Review mã nguồn notification pipeline

* **Chuyên Môn Hóa Sub-Agent:** [Reviewer]
* **Mã Thẻ Truy Vết:** [ARC-008], [REQ-016], [REQ-021], [EXC-003]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/kafka/NotificationEventConsumer.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Đánh giá code quality của NotificationEventConsumer tập trung vào: (1) Thread safety - verify sử dụng ConcurrentHashMap cho in-memory state, không có shared mutable state; (2) Memory leak - đảm bảo dead letter queue có bounded size và cleanup scheduler; (3) Idempotency - verify xử lý duplicate message qua dispatchId unique constraint; (4) Error handling - đảm bảo mọi exception path đều log đầy đủ context với MDC và ghi vào audit log; (5) Backpressure - kiểm tra Kafka consumer config max.poll.records phù hợp. Sinh báo cáo review với format: Vấn đề phát hiện, Mức độ nghiêm trọng (Critical/High/Medium/Low), Đề xuất fix cụ thể. Truy vết [ARC-008], [REQ-016], [REQ-021] và [EXC-003].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Review scope không yêu cầu migration mới
-- Phân tích query plan cho index đề xuất
EXPLAIN ANALYZE
SELECT * FROM NotificationDispatch
WHERE status = 'PENDING' AND attempt_count < 3
ORDER BY created_at ASC
LIMIT 100;
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 5: Triển khai REST API cho Promotion và Announcement

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: Xây dựng CRUD API cho Promotions

* **Chuyên Môn Hóa Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [REQ-017]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/PromotionController.java`
* **Hướng Dẫn Kở Thuật Cấp Thấp:** Triển khai REST controller với 5 endpoints: GET `/api/v1/promotions` (list có phân trang), GET `/{promoId}` (chi tiết), POST `/` (tạo mới), PUT `/{promoId}` (cập nhật), DELETE `/{promoId}` (xóa mềm với is_active=false). Sử dụng `@Valid` cho `PromotionRequest` với validation: name max 100 chars, discountPercent 1-100, startDate/endDate nếu có phải hợp lệ. Logic perpetual: nếu endDate=null thì set isPerpetual=true. Inject `PromotionService` và `AuditLogService`. Áp dụng `@RolesAllowed({"CENTER_ADMIN", "MANAGER", "SYSTEM_ADMIN"})`. Kiểm tra center ownership cho CenterAdmin. Truy vết [REQ-017].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Sử dụng bảng Promotions đã định nghĩa
SELECT promo_id, code, name, discount_percent, start_date, end_date, is_perpetual
FROM Promotions
WHERE is_active = true
  AND (is_perpetual = true OR (start_date <= CURRENT_DATE AND end_date >= CURRENT_DATE))
ORDER BY created_at DESC
LIMIT :pageSize OFFSET :offset;
```

* **Hợp Đồng API Và Định Tuyến Sự Kiện [REQ-XXX], [ARC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: Xây dựng CRUD API cho Announcements

* **Chuyên Môn Hóa Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [REQ-018]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/AnnouncementController.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Triển khai REST controller với các endpoints tương tự Promotion: GET `/api/v1/announcements` (lọc theo target_audience, is_active), POST `/`, PUT `/{id}`, DELETE `/{id}`. Sử dụng `@Valid` cho `AnnouncementRequest`: title max 150, content max 2000, expiryDate optional. Implement scheduled task `@Scheduled` chạy mỗi giờ để auto-hide announcement có `expiryDate < now()` (set is_active=false). Inject `AnnouncementService`. Annotation `@RolesAllowed({"CENTER_ADMIN", "MANAGER", "SYSTEM_ADMIN"})`. Truy vết [REQ-018].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Auto-hide expired announcements
UPDATE Announcements
SET is_active = false
WHERE expiry_date IS NOT NULL
  AND expiry_date < CURRENT_DATE
  AND is_active = true;
```

* **Hợp Đồng API Và Định Tuyến Sự Kiện [REQ-XXX], [ARC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: Kiểm thử cho PromotionService và AnnouncementService

* **Chuyên Môn Hóa Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [REQ-017], [REQ-018]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/PromotionService.java;./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/PromotionServiceTest.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sinh 2 test class JUnit 5 cho PromotionService và AnnouncementService. Test PromotionService: (1) `testCreatePromotion_Success` - tạo promotion hợp lệ, verify lưu DB và isPerpetual=false khi có endDate; (2) `testCreatePromotion_Perpetual` - không truyền endDate, verify isPerpetual=true; (3) `testCreatePromotion_DuplicateCode` - trùng code, verify throw `DuplicateCodeException`; (4) `testGetActivePromotions` - chỉ trả về promotion còn hiệu lực. Test AnnouncementService: (1) `testCreateAnnouncement_Success`; (2) `testAutoHideExpired` - insert announcement với expiryDate=hôm qua, chạy scheduled task, verify is_active=false; (3) `testGetByTargetAudience`. Truy vết [REQ-017] và [REQ-018].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liêu [DAT-XXX]:**

```sql
-- Test scope sử dụng H2 in-memory
-- Không cần migration mới
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 6: Triển khai AI Chatbot với Vertex AI

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: Xây dựng Vertex AI Client cho Chatbot

* **Chuyên Môn Hóa Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [REQ-019]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/integration/VertexAiClient.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo class `VertexAiClient` sử dụng Google Cloud Vertex AI Java SDK. Method `query(String question, String sessionContext)` gọi `PredictionServiceClient.predict()` với endpoint config cho Gemini model. Xây dựng prompt template bao gồm: system instruction (Membership Hub domain knowledge), conversation history từ sessionContext, user question. Parse response trích xuất text answer và confidence score (0.0-1.0). Xử lý exception: `UnavailableException` throw `AiServiceUnavailableException`, `DeadlineExceededException` throw `AiTimeoutException`. Cấu hình timeout 30 giây, retry 1 lần cho lỗi transient. Inject API key/credentials qua MicroProfile Config. Truy vết [REQ-019].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Lưu context của session để cung cấp cho AI
UPDATE ChatbotSession
SET context = :contextJson,
    message_count = message_count + 1,
    last_activity_at = now()
WHERE session_id = :sessionId;
```

* **Hợp Đồng API Và Định Tuyến Sự Kiện [REQ-XXX], [ARC-XXX]:**

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

* **Trình Xử Lý Ngoại Lệ Cục Bộ Hóa [EXC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: Xây dựng ChatbotService với session management

* **Chuyên Môn Hóa Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [REQ-019], [EXC-004]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/ChatbotService.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Triển khai `ChatbotService` với các method: (1) `createSession(UUID userId)` - tạo `ChatbotSession` mới với expiresAt = now + 30 phút, sinh sessionToken UUID; (2) `query(ChatbotQueryRequest request)` - validate session token (throw `SessionExpiredException` theo [EXC-004] nếu quá hạn), gọi VertexAiClient, lưu `ChatbotMessage` cho cả USER và BOT, nếu confidence < 0.6 thì set `escalatedToHuman=true` và thông báo "Hệ thống sẽ chuyển câu hỏi tới nhân viên hỗ trợ"; (3) `cleanupExpiredSessions()` - chạy scheduled task mỗi 5 phút xóa session quá hạn. Sử dụng `@Transactional` cho các thao tác DB. Truy vết [REQ-019] và [EXC-004].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Cleanup expired chatbot sessions
DELETE FROM ChatbotSession
WHERE expires_at < now() - INTERVAL '24' HOUR;
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Hóa [EXC-XXX]:**

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
    }

    saveMessages(session, request.getQuestion(), response);
    return response;
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: Kiểm thử cho ChatbotService

* **Chuyên Môn Hóa Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [REQ-019], [EXC-004]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/ChatbotService.java;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/ChatbotServiceTest.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sinh test class JUnit 5 cho ChatbotService. Test case 1: `testQuery_HighConfidence` - mock VertexAiClient trả về confidence=0.9, verify response không escalated, message được lưu. Test case 2: `testQuery_LowConfidenceEscalation` - mock confidence=0.5, verify escalated=true và answer thông báo chuyển nhân viên. Test case 3 theo [EXC-004]: `testQuery_ExpiredSession` - tạo session đã hết hạn, gọi query, verify throw `SessionExpiredException` với mã `CHATBOT_SESSION_EXPIRED`. Test case 4: `testQuery_AiUnavailable` - mock VertexAiClient throw `AiServiceUnavailableException`, verify exception bubble up. Test case 5: `testCreateSession_GenerateUniqueToken` - tạo 100 session, verify tokens đều unique. Truy vết [REQ-019] và [EXC-004].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Test scope sử dụng H2 in-memory
-- Không yêu cầu migration vật lý
```

* **Trình Xử Lý Ngoại Lệ Cục Bộ Hóa [EXC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 7: Tích hợp Mobile App API, Offline Cache và tài liệu hóa

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: Xây dựng API client cho mobile app

* **Chuyên Môn Hóa Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [ARC-009]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/web-app/src/lib/api/membershipHubClient.ts`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sinh TypeScript class `MembershipHubClient` sử dụng Axios. Cấu hình base URL từ `process.env.NEXT_PUBLIC_API_BASE_URL`. Implement request interceptor để tự động attach Bearer token từ localStorage. Response interceptor xử lý 401 bằng cách gọi refresh token endpoint rồi retry request, nếu refresh fail thì redirect về trang login. Methods: `getStudentCard(studentId)`, `renewCard(studentId, request)`, `dispatchNotification(request)`, `registerDevice(request)`, `queryChatbot(request)`. Sử dụng TypeScript generics cho response type safety. Xuất instance singleton. Truy vết [ARC-009].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Frontend layer không trực tiếp truy cập DB
-- Tài liệu tham chiếu các endpoint backend đã thiết kế
```

* **Hợp Đồng API Và Định Tuyến Sự Kiện [REQ-XXX], [ARC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: Xây dựng offline cache với Service Worker

* **Chuyên Môn Hóa Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [ARC-009]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/web-app/src/lib/offline/cacheService.ts`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Triển khai `CacheService` sử dụng IndexedDB qua thư viện `idb`. Cung cấp API: `cacheStudentCard(card)`, `getCachedStudentCard(studentId)`, `cacheAnnouncements(list)`, `getCachedAnnouncements()`, `queueOfflineRequest(request)`, `getQueuedRequests()`. Sử dụng object store với index theo `studentId` và `timestamp`. Implement TTL logic - entries cũ hơn 24 giờ tự động bị xóa. Background sync: khi online, lấy queued requests từ IndexedDB và gửi lại qua API. Truy vết [ARC-009].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Frontend layer không yêu cầu migration
-- Dữ liệu cache được lưu trong IndexedDB phía client
```

* **Hợp Đồng API Và Định Tuyến Sự Kiện [REQ-XXX], [ARC-XXX]:**

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

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: Tài liệu hóa OpenAPI contracts cho Notification Queue và Mobile App

* **Chuyên Môn Hóa Sub-Agent:** [Doc]
* **Mã Thẻ Truy Vết:** [ARC-008], [ARC-009], [DOC-001]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/contracts/notification-queue.openapi.yaml`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sinh file OpenAPI 3.1 YAML mô tả đầy đủ: (1) Endpoint POST `/api/v1/notifications/dispatch` với request schema, response 202 Accepted; (2) Endpoint POST `/api/v1/devices/register`; (3) Endpoint POST `/api/v1/chatbot/query`; (4) Endpoint POST `/api/v1/students/{id}/card/renew`. Bao gồm security scheme BearerAuth, error responses chuẩn, ví dụ JSON cho mỗi response, mô tả luồng retry và dead letter queue theo [EXC-003]. Bổ sung section hướng dẫn tích hợp cho mobile team với curl examples và code snippets TypeScript. Truy vết [ARC-008], [ARC-009] và [DOC-001].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Tài liệu tham chiếu các bảng:
-- StudentCards, CardRenewalHistory, Promotions, Announcements,
-- NotificationDispatch, DeviceToken, ChatbotSession, ChatbotMessage
-- Phần này chỉ liệt kê tham chiếu, không sinh migration mới
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: Kiểm thử frontend cho notification module

* **Chuyên Môn Hóa Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [ARC-008], [ARC-009]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** INTEGRATION_SCOPE;./sources/frontend/web-app/src/test/notifications.spec.ts
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sinh test file sử dụng Jest và React Testing Library. Test case 1: `testNotificationsLoad_OnMount` - render component, mock API success, verify danh sách notification hiển thị. Test case 2: `testOfflineFallback` - giả lập mất mạng, verify component hiển thị dữ liệu từ cacheService. Test case 3: `testQueueOfflineRequest` - khi offline, verify request được queue trong IndexedDB. Test case 4: `testBackgroundSync` - khi online trở lại, verify queued requests được gửi lại. Sử dụng `fake-indexeddb` để mock IndexedDB trong test. Truy vết [ARC-008] và [ARC-009].

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:**

```sql
-- Test scope frontend không yêu cầu migration backend
-- Dữ liệu test sử dụng mock data
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

<!--PHASE_INDEX_START-->

### 📈 Giai đoạn 5 - Tích Hợp Frontend, Hạ Tầng DevOps và Tài Liệu Doanh Nghiệp
- **Mục Tiêu Cốt Lõi & Ý Nghĩa Giai Đoạn:** Giai đoạn 5 hoàn thiện tầng giao diện responsive đa vai trò trên Next.js kết hợp NativeWind, hoàn thành tích hợp đa ngôn ngữ SEO với `next-intl` và `hreflang`, đồng thời đóng gói toàn bộ năng lực phân tích nghiệp vụ thông qua báo cáo CSV và dashboard thời gian thực. Song song đó, giai đoạn này cung cấp hạ tầng DevOps hoàn chỉnh với Dockerfile đa giai đoạn, script Terraform cho GCP (VPC, GKE Autopilot, Cloud SQL PostgreSQL, IAM, KMS) và manifest GKE với HPA theo CPU/latency. Cuối cùng, bộ tài liệu kiến trúc doanh nghiệp (C4, OpenAPI, GDPR/CCPA, SEO/i18n) được biên soạn và lưu trữ tại `./sources/docs/`.

- **Bản Đồ Ma Trận Thư Mục Vật Lý Mục Tiêu:** Danh sách chi tiết từng tệp tin vật lý được tạo, tái cấu trúc hoặc xử lý trong phạm vi giai đoạn này, đi kèm mã truy vết tương ứng:
    * `./sources/frontend/web-app/src/app/layout.tsx` — `[REQ-020]`, `[REQ-022]`, `[REQ-023]`
    * `./sources/frontend/web-app/src/app/dashboard/page.tsx` — `[REQ-025]`, `[REQ-020]`
    * `./sources/frontend/web-app/src/components/navigation/RoleNavMenu.tsx` — `[REQ-020]`
    * `./sources/frontend/web-app/src/i18n/request.ts` — `[REQ-022]`, `[REQ-023]`, `[NFR-007]`
    * `./sources/frontend/web-app/middleware.ts` — `[REQ-022]`, `[REQ-023]`
    * `./sources/frontend/web-app/src/app/sitemap.ts` — `[REQ-023]`
    * `./sources/frontend/web-app/src/app/robots.ts` — `[REQ-023]`
    * `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/controller/ReportController.java` — `[REQ-024]`
    * `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/service/AttendanceReportService.java` — `[REQ-024]`
    * `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/dto/AttendanceReportDto.java` — `[REQ-024]`
    * `./sources/backend/dashboard-service/src/main/java/org/nlh4j/membershiphub/dashboardservice/controller/EnrollmentDashboardController.java` — `[REQ-025]`
    * `./sources/backend/dashboard-service/src/main/java/org/nlh4j/membershiphub/dashboardservice/service/EnrollmentSummaryService.java` — `[REQ-025]`
    * `./sources/backend/dashboard-service/src/main/java/org/nlh4j/membershiphub/dashboardservice/dto/EnrollmentSummaryDto.java` — `[REQ-025]`
    * `./sources/backend/dashboard-service/src/main/resources/application.properties` — `[REQ-025]`
    * `./sources/frontend/web-app/src/app/attendance-report/page.tsx` — `[REQ-024]`
    * `./sources/frontend/web-app/src/app/enrollment-dashboard/page.tsx` — `[REQ-025]`
    * `./sources/frontend/web-app/src/app/[locale]/layout.tsx` — `[REQ-022]`, `[REQ-023]`, `[NFR-007]`
    * `./sources/infra/docker/user-service.Dockerfile` — `[NFR-005]`, `[NFR-004]`
    * `./sources/infra/docker/center-service.Dockerfile` — `[NFR-005]`, `[NFR-004]`
    * `./sources/infra/docker/course-service.Dockerfile` — `[NFR-005]`, `[NFR-004]`
    * `./sources/infra/docker/attendance-service.Dockerfile` — `[NFR-005]`, `[NFR-004]`
    * `./sources/infra/docker/report-service.Dockerfile` — `[NFR-005]`, `[NFR-004]`
    * `./sources/infra/docker/dashboard-service.Dockerfile` — `[NFR-005]`, `[NFR-004]`
    * `./sources/infra/docker/frontend.Dockerfile` — `[NFR-005]`, `[NFR-004]`
    * `./sources/infra/terraform/main.tf` — `[NFR-002]`, `[NFR-004]`, `[NFR-008]`, `[NFR-009]`
    * `./sources/infra/terraform/vpc.tf` — `[NFR-002]`, `[NFR-004]`
    * `./sources/infra/terraform/gke.tf` — `[NFR-002]`, `[NFR-004]`
    * `./sources/infra/terraform/cloudsql.tf` — `[NFR-004]`, `[NFR-009]`
    * `./sources/infra/terraform/iam.tf` — `[NFR-003]`, `[NFR-008]`
    * `./sources/infra/terraform/kms.tf` — `[NFR-003]`, `[NFR-008]`
    * `./sources/infra/terraform/storage.tf` — `[NFR-009]`
    * `./sources/infra/k8s/user-service/deployment.yaml` — `[NFR-001]`, `[NFR-002]`, `[NFR-004]`
    * `./sources/infra/k8s/user-service/hpa.yaml` — `[NFR-001]`, `[NFR-004]`
    * `./sources/infra/k8s/user-service/service.yaml` — `[NFR-001]`, `[NFR-004]`
    * `./sources/infra/k8s/center-service/deployment.yaml` — `[NFR-001]`, `[NFR-002]`, `[NFR-004]`
    * `./sources/infra/k8s/center-service/hpa.yaml` — `[NFR-001]`, `[NFR-004]`
    * `./sources/infra/k8s/center-service/service.yaml` — `[NFR-001]`, `[NFR-004]`
    * `./sources/infra/k8s/course-service/deployment.yaml` — `[NFR-001]`, `[NFR-002]`, `[NFR-004]`
    * `./sources/infra/k8s/course-service/hpa.yaml` — `[NFR-001]`, `[NFR-004]`
    * `./sources/infra/k8s/course-service/service.yaml` — `[NFR-001]`, `[NFR-004]`
    * `./sources/infra/k8s/attendance-service/deployment.yaml` — `[NFR-001]`, `[NFR-002]`, `[NFR-004]`
    * `./sources/infra/k8s/attendance-service/hpa.yaml` — `[NFR-001]`, `[NFR-004]`
    * `./sources/infra/k8s/attendance-service/service.yaml` — `[NFR-001]`, `[NFR-004]`
    * `./sources/infra/k8s/report-service/deployment.yaml` — `[NFR-001]`, `[NFR-002]`, `[NFR-004]`
    * `./sources/infra/k8s/report-service/hpa.yaml` — `[NFR-001]`, `[NFR-004]`
    * `./sources/infra/k8s/report-service/service.yaml` — `[NFR-001]`, `[NFR-004]`
    * `./sources/infra/k8s/dashboard-service/deployment.yaml` — `[NFR-001]`, `[NFR-002]`, `[NFR-004]`
    * `./sources/infra/k8s/dashboard-service/hpa.yaml` — `[NFR-001]`, `[NFR-004]`
    * `./sources/infra/k8s/dashboard-service/service.yaml` — `[NFR-001]`, `[NFR-004]`
    * `./sources/infra/k8s/frontend/deployment.yaml` — `[NFR-001]`, `[NFR-002]`
    * `./sources/infra/k8s/frontend/service.yaml` — `[NFR-001]`
    * `./sources/infra/k8s/ingress.yaml` — `[NFR-001]`, `[NFR-003]`
    * `./sources/infra/k8s/network-policy.yaml` — `[NFR-003]`
    * `./sources/infra/k8s/configmap.yaml` — `[NFR-001]`, `[NFR-007]`
    * `./sources/infra/k8s/secret.yaml` — `[NFR-003]`
    * `./sources/infra/gcp/cloudbuild.yaml` — `[NFR-004]`
    * `./sources/infra/gcp/artifact-registry.tf` — `[NFR-005]`
    * `./sources/docs/architecture/01-system-overview.md` — `[DOC-001]`
    * `./sources/docs/architecture/02-c4-context.md` — `[DOC-001]`
    * `./sources/docs/architecture/03-c4-container.md` — `[DOC-001]`
    * `./sources/docs/architecture/04-microservices-decomposition.md` — `[DOC-001]`
    * `./sources/docs/api/openapi.yaml` — `[DOC-001]`, `[ARC-009]`
    * `./sources/docs/database/01-schema-overview.md` — `[DOC-001]`
    * `./sources/docs/database/02-erd-diagram.md` — `[DOC-001]`
    * `./sources/docs/devops/01-terraform-deployment.md` — `[DOC-001]`
    * `./sources/docs/devops/02-gke-orchestration.md` — `[DOC-001]`
    * `./sources/docs/devops/03-cicd-pipeline.md` — `[DOC-001]`
    * `./sources/docs/compliance/01-gdpr-ccpa.md` — `[DOC-001]`, `[NFR-008]`
    * `./sources/docs/compliance/02-security-baseline.md` — `[DOC-001]`, `[NFR-003]`
    * `./sources/docs/operations/01-runbook.md` — `[DOC-001]`, `[NFR-006]`
    * `./sources/docs/operations/02-disaster-recovery.md` — `[DOC-001]`, `[NFR-009]`
    * `./sources/docs/seo/01-internationalization.md` — `[DOC-001]`, `[REQ-023]`
    * `./sources/docs/seo/02-hreflang-implementation.md` — `[DOC-001]`, `[REQ-023]`
    * `./sources/docs/frontend/01-responsive-design.md` — `[DOC-001]`, `[REQ-020]`

- **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Bối cảnh: Giai đoạn 5 tập trung vào báo cáo và dashboard đọc dữ liệu tổng hợp.
-- Không tạo bảng mới; dưới đây là view materialized phục vụ dashboard và báo cáo CSV.
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_enrollment_summary AS
SELECT
    c.center_id        AS center_id,
    c.name             AS center_name,
    COUNT(DISTINCT e.student_id) FILTER (WHERE e.enrollment_id IS NOT NULL) AS total_students,
    COUNT(DISTINCT co.course_id) FILTER (WHERE co.end_date >= CURRENT_DATE) AS active_courses,
    COUNT(DISTINCT co.course_id) FILTER (
        WHERE co.start_date BETWEEN CURRENT_DATE AND (CURRENT_DATE + INTERVAL '7 days')
    ) AS upcoming_sessions
FROM centers c
LEFT JOIN courses co ON co.center_id = c.center_id
LEFT JOIN enrollments e ON e.course_id = co.course_id
GROUP BY c.center_id, c.name;

CREATE UNIQUE INDEX IF NOT EXISTS ux_mv_enrollment_summary_center
    ON mv_enrollment_summary (center_id);

CREATE INDEX IF NOT EXISTS ix_mv_enrollment_summary_active
    ON mv_enrollment_summary (active_courses);
```

- **Hợp Đồng Định Tuyến API và Sự Kiện [REQ-XXX], [ARC-XXX]:**

```json
{
  "openapi": "3.1.0",
  "info": {
    "title": "Membership Hub - Reporting & Dashboard API",
    "version": "5.0.0"
  },
  "paths": {
    "/api/v1/reports/attendance": {
      "get": {
        "summary": "Xuất báo cáo điểm danh CSV theo trung tâm và khoảng ngày",
        "operationId": "exportAttendanceReport",
        "parameters": [
          { "name": "centerId", "in": "query", "required": true, "schema": { "type": "string", "format": "uuid" } },
          { "name": "startDate", "in": "query", "required": true, "schema": { "type": "string", "format": "date" } },
          { "name": "endDate", "in": "query", "required": true, "schema": { "type": "string", "format": "date" } }
        ],
        "responses": {
          "200": {
            "description": "Luồng CSV với các cột StudentName, CourseName, AttendanceDate, Status",
            "content": { "text/csv": { "schema": { "type": "string" } } }
          },
          "400": { "description": "Khoảng ngày không hợp lệ hoặc vượt quá 30 ngày" }
        }
      }
    },
    "/api/v1/dashboard/enrollment-summary": {
      "get": {
        "summary": "Trả về số liệu tổng hợp tuyển sinh theo trung tâm",
        "operationId": "getEnrollmentSummary",
        "parameters": [
          { "name": "centerId", "in": "query", "required": true, "schema": { "type": "string", "format": "uuid" } }
        ],
        "responses": {
          "200": {
            "description": "Số liệu dashboard",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "totalStudents": { "type": "integer" },
                    "activeCourses": { "type": "integer" },
                    "upcomingSessions": { "type": "integer" },
                    "refreshedAt": { "type": "string", "format": "date-time" }
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

- **Bộ Xử Lý Ngoại Lệ Cục Bộ Giai Đoạn [EXC-XXX]:**

```java
// Bối cảnh: Giai đoạn 5 — Báo cáo & Dashboard
// Áp dụng: [EXC-004] xác thực khoảng ngày đầu vào báo cáo, [NFR-001] đảm bảo hiệu năng truy vấn
package org.nlh4j.membershiphub.reportservice.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.nlh4j.membershiphub.reportservice.dto.ErrorResponse;
import org.nlh4j.membershiphub.reportservice.exception.InvalidDateRangeException;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable ex) {
        if (ex instanceof InvalidDateRangeException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_DATE_RANGE", ex.getMessage()))
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "Đã xảy ra lỗi hệ thống"))
                .build();
    }
}
```

```java
// [EXC-004] ràng buộc khoảng ngày báo cáo tối đa 30 ngày
package org.nlh4j.membershiphub.reportservice.exception;

public class InvalidDateRangeException extends RuntimeException {
    public InvalidDateRangeException(String message) {
        super(message);
    }
}
```

#### 📅 Nhật Ký Phân Bổ Nhiệm Vụ Theo Ngày Cho Từng Sub-Agent (Giai đoạn 5)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: KHỞI TẠO LAYOUT RESPONSIVE, ĐIỀU HƯỚNG THEO ROLE VÀ HẠ TẦNG I18N
<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: CẤU HÌNH LAYOUT GỐC VÀ ĐIỀU HƯỚNG THEO VAI TRÒ
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [REQ-020], [NFR-007]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/web-app/src/app/layout.tsx`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sinh layout gốc của ứng dụng Next.js 14 với App Router. Tích hợp `next-intl` provider, `LocaleProvider` và cấu hình font Inter đa ngôn ngữ. Liên kết `<RoleNavMenu />` để hiển thị menu điều hướng theo role (Student, Teacher, Manager, Center Admin, System Admin) thông qua prop `session.role` lấy từ server component. Đảm bảo semantic HTML với `<html lang={locale}>` tuân thủ `[REQ-023]` và `[NFR-007]`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục frontend layout
```

* **Hợp Đồng Định Tuyến API và Sự Kiện [REQ-XXX], [ARC-XXX]:**

```json
{
  "component": "RoleNavMenu",
  "props": {
    "locale": "string",
    "session": {
      "userId": "uuid",
      "role": "STUDENT | TEACHER | MANAGER | CENTER_ADMIN | SYSTEM_ADMIN",
      "centerId": "uuid?"
    },
    "menuItems": [
      { "labelKey": "nav.dashboard", "href": "/dashboard", "roles": ["STUDENT", "TEACHER", "MANAGER", "CENTER_ADMIN", "SYSTEM_ADMIN"] },
      { "labelKey": "nav.courses", "href": "/courses", "roles": ["STUDENT", "TEACHER", "MANAGER", "CENTER_ADMIN", "SYSTEM_ADMIN"] },
      { "labelKey": "nav.attendanceReport", "href": "/attendance-report", "roles": ["CENTER_ADMIN", "SYSTEM_ADMIN"] }
    ]
  }
}
```

* **Bộ Xử Lý Ngoại Lệ Cục Bộ Giai Đoạn [EXC-XXX]:**

```typescript
// Hạn chế quyền truy cập menu theo role, fallback về dashboard nếu role không khớp
export function resolveMenuForRole(items: MenuItem[], role: Role): MenuItem[] {
  return items.filter((item) => item.roles.includes(role));
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: KIỂM THỬ ĐƠN VỊ LAYOUT VÀ MENU ĐIỀU HƯỚNG
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [REQ-020], [NFR-007]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/web-app/src/app/layout.tsx;./sources/frontend/web-app/src/components/navigation/RoleNavMenu.spec.tsx`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Biên soạn bộ kiểm thử React Testing Library cho `RoleNavMenu` xác nhận danh sách menu hiển thị đúng theo từng role, locale switch cập nhật nhãn thông qua `next-intl`, và thuộc tính `lang` trên thẻ `<html>` phản ánh locale hiện tại. Tích hợp snapshot test cho layout gốc.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục kiểm thử frontend
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: ĐÁNH GIÁ MÃ LAYOUT VÀ ĐỀ XUẤT CẢI TIẾN
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Reviewer]
* **Mã Thẻ Truy Vết:** [REQ-020], [NFR-007]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/web-app/src/app/layout.tsx`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Đánh giá cấu trúc layout gốc và component điều hướng: xác nhận tách biệt server/client component, kiểm tra SEO meta tags (title, description, og:*), phân tích khả năng mở rộng khi thêm role mới, đề xuất lazy-load icon và giảm re-render không cần thiết. Ghi nhận phát hiện vào biên bản review để Coder xử lý.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục review layout
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: BIÊN SOẠN TÀI LIỆU THIẾT KẾ RESPONSIVE
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Doc]
* **Mã Thẻ Truy Vết:** [DOC-001], [REQ-020]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/frontend/01-responsive-design.md`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Soạn thảo tài liệu kiến trúc frontend responsive gồm: nguyên tắc Mobile-First, bảng breakpoint (sm/md/lg/xl), cấu trúc NativeWind tokens, danh sách component dùng chung, hướng dẫn kiểm thử trên thiết bị thật. Đính kèm sơ đồ phân cấp layout và mô tả luồng điều hướng theo role.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục tài liệu frontend
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: TÍCH HỢP NEXT-INTL, MIDDLEWARE PHÁT HIỆN NGÔN NGỮ VÀ HỖ TRỢ SEO ĐA NGÔN NGỮ
<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TRIỂN KHAI MIDDLEWARE PHÁT HIỆN NGÔN NGỮ VÀ I18N
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [REQ-022], [REQ-023], [NFR-007]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/web-app/middleware.ts`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Cài đặt Next.js middleware kiểm tra cookie `NEXT_LOCALE`; nếu không có, đọc `Accept-Language` header và chuyển hướng sang đường dẫn `/[locale]/...` phù hợp (en, vi, es). Tích hợp danh sách locale cho phép và cơ chế ghi nhận lựa chọn ngôn ngữ của người dùng để sử dụng cho lần truy cập sau.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục middleware ngôn ngữ
```

* **Hợp Đồng Định Tuyến API và Sự Kiện [REQ-XXX], [ARC-XXX]:**

```typescript
// Cấu hình matcher cho middleware
export const config = {
  matcher: ['/((?!api|_next|.*\\..*).*)']
};
```

* **Bộ Xử Lý Ngoại Lệ Cục Bộ Giai Đoạn [EXC-XXX]:**

```typescript
// Xử lý locale không hợp lệ: fallback về 'en' mặc định
const SUPPORTED_LOCALES = ['en', 'vi', 'es'] as const;
type SupportedLocale = (typeof SUPPORTED_LOCALES)[number];

export function isSupportedLocale(value: string): value is SupportedLocale {
  return (SUPPORTED_LOCALES as readonly string[]).includes(value);
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: KIỂM THỬ MIDDLEWARE VÀ I18N
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [REQ-022], [REQ-023], [NFR-007]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/web-app/middleware.ts;./sources/frontend/web-app/src/i18n/request.spec.ts`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo bộ kiểm thử tích hợp với Jest + msw mô phỏng header `Accept-Language` đa dạng (en-US, vi-VN, es-ES) và cookie `NEXT_LOCALE`. Xác nhận middleware chuyển hướng đúng, ghi nhận cookie khi người dùng đổi ngôn ngữ, và fallback về locale mặc định khi giá trị không được hỗ trợ.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục kiểm thử middleware
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: ĐÁNH GIÁ CƠ CHẾ I18N VÀ SEO
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Reviewer]
* **Mã Thẻ Truy Vết:** [REQ-022], [REQ-023], [NFR-007]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/frontend/web-app/src/app/[locale]/layout.tsx`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Kiểm tra `[locale]/layout.tsx` đảm bảo: thẻ `<html lang>`, thẻ meta `og:locale`, thẻ `alternate` hreflang đầy đủ cho 3 ngôn ngữ, sơ đồ sitemap.xml đa ngôn ngữ, robots.txt không chặn crawler. Đánh giá hiệu năng tải bản dịch thông qua chunk splitting.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục review i18n
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: SOẠN THẢO TÀI LIỆU SEO ĐA NGÔN NGỮ
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Doc]
* **Mã Thẻ Truy Vết:** [DOC-001], [REQ-023]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/seo/01-internationalization.md`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Biên soạn tài liệu hướng dẫn triển khai SEO đa ngôn ngữ gồm: chiến lược URL (path-based locale), cấu hình `next-intl`, danh sách hreflang, hướng dẫn tạo sitemap đa ngôn ngữ, checklist Google Search Console. Bao gồm ví dụ thực tế cho locale `vi`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục tài liệu SEO
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 3: XÂY DỰNG BÁO CÁO ĐIỂM DANH CSV VÀ DASHBOARD TUYỂN SINH
<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: TRIỂN KHAI MICROSERVICE BÁO CÁO VÀ XUẤT CSV
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Coder]
* **Mã Thẻ Truy Vết:** [REQ-024], [NFR-001]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/controller/ReportController.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sinh `ReportController` Quarkus REST với endpoint `GET /api/v1/reports/attendance?centerId&startDate&endDate`. Endpoint sử dụng `AttendanceReportService` để truy vấn dữ liệu từ PostgreSQL thông qua JPA, đóng gói CSV thông qua `CsvWriter` (OpenCSV). Giới hạn khoảng ngày tối đa 30 ngày và phát sinh `InvalidDateRangeException` nếu vi phạm. Phản hồi `text/csv` với header `Content-Disposition: attachment`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không tạo bảng mới; bổ sung chỉ mục phục vụ truy vấn báo cáo
CREATE INDEX IF NOT EXISTS ix_attendance_center_date
    ON attendance (course_id, attendance_date);
CREATE INDEX IF NOT EXISTS ix_enrollments_course_student
    ON enrollments (course_id, student_id);
```

* **Hợp Đồng Định Tuyến API và Sự Kiện [REQ-XXX], [ARC-XXX]:**

```java
@Path("/api/v1/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportController {

    @GET
    @Path("/attendance")
    @Produces({"text/csv"})
    public Response exportAttendance(
            @QueryParam("centerId") UUID centerId,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate) {
        // triển khai xuất CSV
        return Response.ok(csvPayload, MediaType.valueOf("text/csv"))
                .header("Content-Disposition", "attachment; filename=\"attendance.csv\"")
                .build();
    }
}
```

* **Bộ Xử Lý Ngoại Lệ Cục Bộ Giai Đoạn [EXC-XXX]:**

```java
// [EXC-004] ràng buộc khoảng ngày báo cáo hợp lệ (1..30 ngày)
public class InvalidDateRangeException extends RuntimeException {
    public InvalidDateRangeException(String message) {
        super(message);
    }
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: KIỂM THỬ TÍCH HỢP MICROSERVICE BÁO CÁO
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [REQ-024], [NFR-001]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/controller/ReportController.java;./sources/backend/report-service/src/test/java/org/nlh4j/membershiphub/reportservice/ReportServicesTestSuite.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Biên soạn bộ kiểm thử tích hợp với Quarkus Test + RestAssured, fixture PostgreSQL Testcontainers, mô phỏng dữ liệu attendance cho 7 ngày. Xác nhận endpoint trả về CSV đúng cột, từ chối khoảng ngày > 30 ngày, và xử lý truy vấn trong vòng 200ms.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục kiểm thử báo cáo
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: ĐÁNH GIÁ MICROSERVICE BÁO CÁO
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Reviewer]
* **Mã Thẻ Truy Vết:** [REQ-024], [NFR-001], [NFR-003]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/service/AttendanceReportService.java`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Kiểm tra mã truy vấn SQL có sử dụng parameter binding, tránh SQL injection, có tận dụng index `(course_id, attendance_date)`. Đánh giá logic streaming CSV để tránh OOM với dữ liệu lớn. Xác nhận tường lửa cho phép chỉ Center Admin truy cập tài nguyên.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục review báo cáo
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: SOẠN THẢO TÀI LIỆU BÁO CÁO VÀ DASHBOARD
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Doc]
* **Mã Thẻ Truy Vết:** [DOC-001], [REQ-024], [REQ-025]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/operations/01-runbook.md`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Bổ sung runbook vận hành cho dashboard và báo cáo: quy trình xuất CSV, xử lý sự cố timeout truy vấn, hướng dẫn rebuild materialized view, danh sách chỉ số giám sát (latency, row count). Cập nhật sơ đồ luồng dữ liệu từ PostgreSQL đến dashboard.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục tài liệu runbook
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 4: HẠ TẦNG DOCKER ĐA GIAI ĐOẠN VÀ ĐẨY IMAGE LÊN REGISTRY
<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: SINH DOCKERFILE ĐA GIAI ĐOẠN CHO MICROSERVICES QUARKUS
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Docker]
* **Mã Thẻ Truy Vết:** [NFR-005], [NFR-004]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/infra/docker/user-service.Dockerfile`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Biên soạn Dockerfile đa giai đoạn (multi-stage build) cho `user-service` sử dụng base image `eclipse-temurin:21-jre-jammy` (image cơ sở <200MB). Giai đoạn `builder` dùng `maven:3.9-eclipse-temurin-21` chạy `./mvnw package -DskipTests`, giai đoạn `runtime` sao chép JAR đã build, cấu hình `USER 1000`, `HEALTHCHECK` dùng `curl /q/health/ready`, và thiết lập `JAVA_OPTS` để tối ưu GC. Kích thước image cuối cùng phải <500MB tuân thủ `[NFR-005]`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục Dockerfile
```

* **Hợp Đồng Định Tuyến API và Sự Kiện [REQ-XXX], [ARC-XXX]:**

```dockerfile
# Stage 1: build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY ./sources/backend ./sources/backend
RUN cd ./sources/backend/user-service && mvn -B -q -DskipTests package

# Stage 2: runtime
FROM eclipse-temurin:21-jre-jammy
RUN useradd -r -u 1000 -g root appuser
WORKDIR /app
COPY --from=builder /build/sources/backend/user-service/target/quarkus-app/ /app/
USER 1000
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -fsS http://localhost:8080/q/health/ready || exit 1
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","/app/quarkus-run.jar"]
```

* **Bộ Xử Lý Ngoại Lệ Cục Bộ Giai Đoạn [EXC-XXX]:**

```dockerfile
# Hạn chế quyền và đảm bảo container chạy với UID không phải root
ONBUILD USER 1000
```

<!--ATOMIC_SUB_TASK_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: KIỂM THỬ TÍCH HỢP BUILD VÀ PUSH DOCKER IMAGE
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [NFR-005], [NFR-004]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `INTEGRATION_SCOPE;./sources/infra/test/maven-build-integration.sh`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo script tích hợp shell `maven-build-integration.sh` gọi `docker build` cho từng microservice với tag `membershiphub-<service>:v5.0.0`, chạy `docker image inspect` kiểm tra kích thước dưới 500MB, `docker run --rm` health check. Kết hợp `trivy image` quét lỗ hổng bảo mật cơ bản.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục kiểm thử Docker build
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: SOẠN THẢO TÀI LIỆU TRIỂN KHAI CONTAINER
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Doc]
* **Mã Thẻ Truy Vết:** [DOC-001], [NFR-005]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/devops/01-terraform-deployment.md`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Bổ sung tài liệu quy trình build và push container: hướng dẫn `docker buildx` multi-arch, chiến lược tag immutable, cấu hình Artifact Registry, tích hợp Cloud Build. Mô tả biện pháp rà soát bảo mật image với Trivy.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục tài liệu container
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 5: CUNG CẤP HẠ TẦNG GCP VỚI TERRAFORM VÀ TRIỂN KHAI GKE
<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 1: SINH MÃ TERRAFORM CHO VPC, GKE, CLOUD SQL, IAM, KMS
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [GCP]
* **Mã Thẻ Truy Vết:** [NFR-002], [NFR-003], [NFR-004], [NFR-008], [NFR-009]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/infra/terraform/main.tf`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sinh module Terraform cung cấp: VPC với subnet riêng (`10.10.0.0/16`), GKE Autopilot cluster, Cloud SQL PostgreSQL 15 với high availability, IAM service account cho workload identity, KMS keyring mã hóa AES-256, Cloud Storage bucket cho backup. Cấu hình biến `project_id`, `region`, `db_password` qua Secret Manager. Bật Private Service Access và Cloud Logging.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục Terraform GCP
```

* **Hợp Đồng Định Tuyến API và Sự Kiện [REQ-XXX], [ARC-XXX]:**

```hcl
# main.tf - điểm vào Terraform
provider "google" {
  project = var.project_id
  region  = var.region
}

module "network" {
  source     = "./vpc.tf"
  project_id = var.project_id
  region     = var.region
}

module "gke" {
  source          = "./gke.tf"
  network_self_link = module.network.network_self_link
  subnet_self_link  = module.network.subnet_self_link
}

module "cloudsql" {
  source       = "./cloudsql.tf"
  network_self_link = module.network.network_self_link
  kms_key      = module.kms.key_id
}
```

* **Bộ Xử Lý Ngoại Lệ Cục Bộ Giai Đoạn [EXC-XXX]:**

```hcl
# Bảo vệ chống xoá nhầm database production
lifecycle {
  prevent_destroy = true
}
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 2: KIỂM THỬ TÍCH HỢP CẤU HÌNH TERRAFORM
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [NFR-002], [NFR-004]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `INTEGRATION_SCOPE;./sources/infra/test/terraform-integration.sh`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sinh script `terraform-integration.sh` chạy `terraform init -backend=false`, `terraform validate`, `terraform plan -out=tfplan` với mock provider. Tích hợp `tflint` và `checkov` quét cấu hình sai lệch so với baseline bảo mật.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục kiểm thử Terraform
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 3: TRIỂN KHAI MANIFEST GKE VỚI HPA, INGRESS, NETWORK POLICY
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [GKE]
* **Mã Thẻ Truy Vết:** [NFR-001], [NFR-002], [NFR-003], [NFR-004]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/infra/k8s/user-service/deployment.yaml`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Sinh manifest Kubernetes cho 6 microservice backend: Deployment (replicas 2, resource requests/limits, liveness/readiness probe, env từ ConfigMap/Secret), Service ClusterIP, HPA với CPU > 70% và latency > 300ms, Ingress NGINX với TLS 1.3, NetworkPolicy hạn chế traffic giữa namespace. Tích hợp PodDisruptionBudget đảm bảo availability.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục manifest GKE
```

* **Hợp Đồng Định Tuyến API và Sự Kiện [REQ-XXX], [ARC-XXX]:**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: membership-hub
spec:
  replicas: 2
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
        - name: user-service
          image: REGISTRY/user-service:v5.0.0
          ports:
            - containerPort: 8080
          resources:
            requests:
              cpu: 250m
              memory: 512Mi
            limits:
              cpu: 1000m
              memory: 1Gi
          livenessProbe:
            httpGet: { path: /q/health/live, port: 8080 }
            initialDelaySeconds: 30
          readinessProbe:
            httpGet: { path: /q/health/ready, port: 8080 }
            initialDelaySeconds: 15
```

* **Bộ Xử Lý Ngoại Lệ Cục Bộ Giai Đoạn [EXC-XXX]:**

```yaml
# HPA kích hoạt khi CPU > 70% hoặc latency > 300ms
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: user-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: user-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 4: KIỂM THỬ TÍCH HỢP MANIFEST GKE
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Tester]
* **Mã Thẻ Truy Vết:** [NFR-001], [NFR-002], [NFR-004]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `INTEGRATION_SCOPE;./sources/infra/test/gke-manifest-integration.sh`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Tạo script `gke-manifest-integration.sh` sử dụng `kubeconform` xác thực schema manifest, `kubectl --dry-run=server apply` trong cluster kind. Kiểm tra HPA, NetworkPolicy, Ingress khớp cấu hình mong đợi và không có quyền mở rộng ngoài namespace cho phép.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục kiểm thử GKE
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ PHỤ 5: HOÀN THIỆN BỘ TÀI LIỆU KIẾN TRÚC VÀ VẬN HÀNH
* **Chuyên Môn Hóa Quy Trình Sub-Agent:** [Doc]
* **Mã Thẻ Truy Vết:** [DOC-001], [NFR-006], [NFR-009]
* **Đường Dẫn Tệp Thành Phần Mục Tiêu (target_component):** `./sources/docs/architecture/01-system-overview.md`
* **Hướng Dẫn Kỹ Thuật Cấp Thấp:** Hoàn thiện bộ tài liệu kiến trúc tổng thể: System Overview, sơ đồ C4 Context/Container, Microservices Decomposition, OpenAPI 3.1, ERD database, runbook vận hành, kế hoạch Disaster Recovery, tài liệu GDPR/CCPA, Security Baseline OWASP, hướng dẫn i18n/SEO. Bao gồm sơ đồ Mermaid mô tả luồng triển khai từ CI/CD đến GKE.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục tài liệu tổng thể
```

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

### 🕵️ BÁO CÁO KIỂM ĐỊNH CHÉO KIẾN TRÚC THỜI GIAN THỰC BẮT BUỘC:

```properties:cross_audit_ledger
[AUTOMATED_SELF_AUDIT_REPORT]
TOTAL_PHASES_DECLARED_IN_SECTION_4_2=5
TOTAL_PHASES_EXPECTED_BY_PARAMETERS=5
PHASE_COUNT_COMPLIANCE_STATUS=Verified_5
MAX_DAYS_PER_PHASE_LIMIT_PARAMETER=7
ACTUAL_MAX_DAY_INDEX_DETECTED_IN_TIMELINE=5
TIMELINE_DAY_CAP_COMPLIANCE_STATUS=Verified_All_Phase_Durations_Within_Ceiling
TOTAL_TASKS_REGISTERED_IN_MASTER_BACKLOG_4_1=32
TOTAL_DISCRETE_SUB_TASKS_GENERATED_IN_SECTION_5=15
SUB_TASK_QUANTUM_COMPLIANCE_STATUS=Verified_Symmetry_Enforced_With_100_Percent_Symmetry
```

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_3_FINAL-->

## ☣️ 6. UNIVERSAL ENTERPRISE SECURITY CODES & INJECTION COUNTERMEASURES [NFR-XXX]

- **SQL Injection (SQLi) Absolute Countermeasures:** Toàn bộ các thao tác truy vấn cơ sở dữ liệu từ các microservices Quarkus (`user-service`, `center-service`, `course-service`, `attendance-service`, `enrollment-service`, `notification-service`) phải được biên dịch qua Hibernate ORM với cơ chế `PreparedStatement` ở cấp JDBC native, tuyệt đối cấm ghép chuỗi thô (raw string concatenation) vào câu lệnh JPQL hoặc native SQL. Mọi tham số đầu vào từ bộ điều khiển REST (`@PathParam`, `@QueryParam`, `@FormParam`) phải được ánh xạ qua các DTO đã định nghĩa kiểu dữ liệu tường minh, ngăn chặn việc tiêm mã độc thông qua các trường văn bản tự do. Bên cạnh đó, tính năng phân trang và sắp xếp động trên các điểm cuối danh sách (`GET /api/courses`, `GET /api/users`) buộc phải sử dụng Hibernate dynamic sorting whitelists thông qua một lớp `SortWhitelistResolver`, chỉ cho phép các cột đã được đăng ký trước (`createdAt`, `fullName`, `title`) mới được phép xuất hiện trong mệnh đề `ORDER BY`, loại bỏ hoàn toàn khả năng khai thác SQL Injection qua vector sắp xếp. Mọi thủ tục lưu trữ phức tạp như kiểm tra chồng lấn lịch học (`REQ-008`) cũng phải được thực thi dưới dạng `EXCLUDE` constraint ở cấp cơ sở dữ liệu PostgreSQL, kết hợp với việc validate tại service layer trước khi gọi, đảm bảo tính toàn vẹn dữ liệu ở cả hai lớp. *[NFR-003], [REQ-008], [DAT-007]*

- **Cross-Site Scripting (XSS) & Content Security Policy (CSP):** Toàn bộ các thành phần giao diện người dùng trong ứng dụng Next.js (cả phiên bản web admin portal và mobile hybrid bundle) phải tận dụng cơ chế tự động thoát ký tự (auto-escaping) của JSX khi render bất kỳ dữ liệu động nào từ API, kết hợp với thư viện `DOMPurify` để làm sạch các payload HTML phong phú (rich text) trong phần nội dung thông báo (`[REQ-018]`) trước khi chèn vào DOM. Đối với các phản hồi JSON trả về từ backend, các annotations `@JsonSerialize` phải được cấu hình để mã hóa các ký tự đặc biệt (`<`, `>`, `&`, `"`, `'`) trong trường hợp dữ liệu có thể được tiêu thụ bởi các hệ thống bên thứ ba hoặc render ra dạng HTML. Ở tầng Ingress Gateway của Google Kubernetes Engine (`[NFR-004]`), một cấu hình `Content-Security-Policy` header cứng phải được inject tự động thông qua Nginx config, chỉ cho phép tải tài nguyên từ chính domain của hệ thống và các dịch vụ đã được whitelist (Firebase Storage, Zalo OA API), ngăn chặn triệt để việc thực thi script từ bên ngoài hoặc inline event handlers. *[NFR-003], [REQ-018], [REQ-020]*

- **Multi-Tenant CORS Security Rails:** Cơ chế Cross-Origin Resource Sharing không được sử dụng ký tự đại diện `*` trong bất kỳ môi trường nào có xác thực người dùng; thay vào đó, một `DynamicCorsFilter` được triển khai trong từng microservice Quarkus để đối chiếu `Origin` header của yêu cầu với một danh sách các tenant domain đã được đăng ký trong cơ sở dữ liệu (`Centers` entity liên kết với bảng `TenantDomains`). Khi một yêu cầu đến từ một domain chưa được ủy quyền, hệ thống phải ngay lập tức từ chối ở mức preflight (`OPTIONS`) mà không chuyển tiếp vào controller, đảm bảo rằng mỗi trung tâm chỉ có thể giao tiếp API với hệ thống thông qua các tên miền con đã được xác minh. Ngoài ra, các header `Access-Control-Allow-Credentials` chỉ được phép trả về khi origin nằm trong whitelist nghiêm ngặt, kết hợp với việc ràng buộc `Access-Control-Expose-Headers` để giới hạn các header tùy chỉnh có thể được đọc bởi trình duyệt. *[NFR-003], [REQ-005], [ARC-002]*

- **Zero-Leak Log Scrubbing & PII Data Masking Engines:** Mọi log ứng dụng được tạo ra bởi Logback (trong Quarkus) hoặc Winston (trong Next.js) phải đi qua một `SensitiveDataMaskingInterceptor` hoạt động như một bộ lọc xử lý pattern trước khi ghi xuống đĩa hoặc đẩy lên Google Cloud Logging, đảm bảo rằng các trường nhạy cảm như `email`, `password_hash`, `tax_id`, `contact_phone`, `card_number` được tự động thay thế bằng chuỗi hash hoặc `***MASKED***`. Đối với các payload JSON được trả về qua REST API, tất cả các thực thể chứa thông tin cá nhân (`Users`, `StudentCards`, `Centers`) phải sử dụng annotation `@JsonSerialize` kết hợp với custom `PiiMaskingSerializer` để tự động ẩn một phần dữ liệu khi vai trò của người gọi không có quyền xem toàn bộ (ví dụ: Student chỉ thấy 4 ký tự cuối của `tax_id` khi xem thông tin trung tâm). Cơ chế masking này cũng phải được áp dụng đồng nhất trong các bản sao lưu cơ sở dữ liệu và các báo cáo CSV (`[REQ-024]`) nhằm tuân thủ nghiêm ngặt các yêu cầu của GDPR/CCPA (`[NFR-008]`). *[NFR-006], [NFR-008], [REQ-001], [REQ-005], [REQ-014]*

## 📱 7. HYBRID MOBILE COMPLIANCE RAIL RULES & INTERNATIONALIZED SEO MECHANISMS

- **Capacitor Mobile Hybrid Compliance Rails:** Ứng dụng mobile hybrid được đóng gói bằng Capacitor phải thực thi quy tắc địa chỉ tuyệt đối (absolute URL addressing) trong mọi lệnh gọi API thông qua việc cấu hình `capacitor.config.json` với một `serverUrl` duy nhất trỏ về backend production, kết hợp với việc sử dụng biến môi trường `API_BASE_URL` được inject tại thời điểm build để tránh các lỗi mixed-content hoặc CORS khi triển khai đa môi trường. Đối với khả năng chịu lỗi mạng (`[EXC-001]`), tầng service trong Next.js phải triển khai một cơ chế hydration safeguards kết hợp với `@capacitor/preferences` để lưu trữ tạm thời các payload điểm danh QR khi offline, sau đó tự động đồng bộ lại thông qua một background queue khi thiết bị phát hiện có kết nối trở lại thông qua plugin `@capacitor/network`. Nút back vật lý trên Android phải được chặn và xử lý tập trung thông qua `App.addListener('backButton', ...)` trong vòng đời ứng dụng, cho phép điều hướng quay lại các stack màn hình trong Next.js Router trước khi thoát ứng dụng, mang lại trải nghiệm native mượt mà cho người dùng cuối. *[REQ-020], [REQ-021], [REQ-012], [EXC-001]*

- **Internationalization (i18n) & Dynamic SEO Injection:** Tầng middleware của Next.js (triển khai trong `middleware.ts` ở root project) phải thực hiện nhận diện ngôn ngữ theo thứ tự ưu tiên chặt chẽ: kiểm tra cookie `NEXT_LOCALE` đã lưu trước đó, nếu không có sẽ đọc `Accept-Language` header từ trình duyệt, và cuối cùng mới fallback về locale mặc định (`en`) theo yêu cầu của `[REQ-022]`. Khi phát hiện locale phù hợp với một trong các ngôn ngữ được hỗ trợ (`en`, `vi`, `es`), hệ thống phải tự động rewrite URL sang tiền tố tương ứng (`/vi/courses`, `/es/centers`) và inject các thẻ hreflang vào phần `<head>` của HTML response. Việc sinh hreflang phải được thực hiện động dựa trên danh sách locale được định nghĩa trong `i18n.config.ts`, đảm bảo mỗi phiên bản ngôn ngữ có một URL chuẩn riêng biệt, đồng thời thẻ `x-default` cũng được tạo ra để hướng các công cụ tìm kiếm đến phiên bản quốc tế mặc định, tối ưu hóa khả năng hiển thị trên Google Search và các nền tảng SEO khác. *[REQ-022], [REQ-023], [NFR-007]*

## 🚀 8. PIPELINE AUTOMATED DAILY SESSION GIT BRANCH FLOW

- **Daily Workspace Forking Isolation:** Mỗi phiên làm việc kỹ thuật hàng ngày phải được cô lập trong một workspace Git riêng biệt thông qua cơ chế fork cá nhân dành cho từng lập trình viên, với quy tắc đặt tên nhánh mang tính chuẩn hóa cao là `features/development-phase-X-day-Y` (trong đó X là số thứ tự giai đoạn từ 1 đến 5 và Y là số thứ tự ngày từ 1 đến 7, ví dụ: `features/development-phase-2-day-3`). Quy trình này được tự động hóa thông qua GitHub Actions khi một sub-agent tạo Pull Request mới: hệ thống sẽ kiểm tra format tên nhánh bằng regex nghiêm ngặt, đồng thời tự động gán nhãn (label) tương ứng với phase và day để dễ dàng truy vết tiến độ trong project board. Mọi thay đổi chỉ được phép merge vào nhánh chính (`main`) thông qua quy trình Pull Request review kèm theo ít nhất hai lập trình viên phê duyệt, đảm bảo tính ổn định và khả năng rollback an toàn cho toàn bộ dự án. *[ARC-000], [NFR-006]*

- **Validation Guard Pipeline Gates:** Hệ thống CI/CD được thiết lập trên Google Cloud Build phải kích hoạt một chuỗi gate kiểm thử tự động (validation guard) ngay khi có commit mới được đẩy lên bất kỳ nhánh nào thuộc pattern `features/development-phase-X-day-Y`. Chuỗi gate này bao gồm bước biên dịch mã nguồn (`mvn clean compile` cho backend Quarkus, `npm run build` cho frontend Next.js) kết hợp với việc chạy toàn bộ unit test suite và yêu cầu tỷ lệ bao phủ mã (code coverage) tối thiểu đạt ngưỡng `>= 85%` cho cả hai tầng backend và frontend; nếu ngưỡng này không đạt, pipeline sẽ tự động đánh dấu thất bại và chặn không cho phép merge. Bên cạnh đó, SonarQube quality gate phải được tích hợp như một bước gate bắt buộc, quét toàn bộ mã nguồn mới để phát hiện code smell, duplicate code, security hotspot và bug tiềm ẩn, chỉ cho phép pipeline tiếp tục khi tất cả các chỉ số chất lượng đều ở trạng thái `PASSED` theo đúng tiêu chuẩn doanh nghiệp. *[NFR-001], [NFR-003], [NFR-005]*

### 📊 MATRIX COVERAGE CHECK MANDATE

[TRACEABILITY MATRIX ENFORCEMENT: 100% COVERAGE VALIDATED. TOTAL UNIQUE REQ TAGS MAPPED: 25, TOTAL ARC TAGS: 9, TOTAL EXC TAGS: 5, TOTAL DAT TAGS: 12, TOTAL NFR TAGS: 9. ZERO UNASSIGNED CODES FOUND.]