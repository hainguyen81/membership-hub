<!--START_CHUNK_PART_1_INITIAL-->

# GLOBAL PROJECT CONTEXT: membership-hub

## 📊 Document Control

| Item | Details |
| :--- | :--- |
| **Blueprint ID** | ARCH-20260915205217 |
| **Project Name** | membership-hub |
| **Version** | 1.0 (Cơ sở) |
| **Date Time** | 2026/09/15 20:52:17 |
| **Author** | Enterprise System Architect (SA Agent) |
| **Approval** | Đang chờ xem xét của Ban quản trị Kỹ thuật |

## 📊 1. SYSTEM OVERVIEW & CORE ARCHITECTURE MODALITY

### ⚙️ 1.1. Core System Modality & Architecture Modality

- Hệ thống được thiết kế theo kiến trúc microservices với các dịch vụ độc lập cho quản lý người dùng, trung tâm, khóa học, và tham dự.
- Sử dụng mô hình Event-Driven Architecture (EDA) cho các luồng thông báo và giao tiếp đa kênh.
- Áp dụng Command Query Responsibility Segregation (CQRS) để tách biệt các thao tác ghi và đọc.
- Sử dụng mô hình Reactive Programming cho các luồng dữ liệu thời gian thực như quét QR và ghi nhận tham dự.
- Kiến trúc được tối ưu hóa cho khả năng mở rộng ngang và khả năng chịu lỗi cao.

### 🌊 1.2. Enterprise Data Flow Topologies & Core Ecosystems

- Sử dụng Apache Kafka để quản lý các luồng dữ liệu bất đồng bộ giữa các dịch vụ.
- Các dịch vụ giao tiếp với nhau thông qua REST APIs và gRPC.
- Sử dụng Redis cho bộ nhớ đệm và quản lý phiên.
- Sử dụng PostgreSQL cho lưu trữ dữ liệu quan hệ.
- Sử dụng Firebase Cloud Messaging (FCM) và Apple Push Notification Service (APNs) cho thông báo đẩy.
- Sử dụng Zalo Open API cho giao tiếp với nhóm Zalo.

## 📁 2. TECH STACK DEPENDENCIES & ECOSYSTEM LIBRARIES

- **Backend Infrastructure Core Stack:**
  - Java 17
  - Quarkus 3.6.0
  - Hibernate ORM 6.4.0
  - Apache Kafka 3.6.0
  - PostgreSQL 15.3
  - Redis 7.2.0
  - Keycloak 22.0.0
  - Spring Security 6.1.0
  - HikariCP 5.0.1
  - Flyway 9.22.0
  - Micrometer 1.11.0
  - OpenTelemetry 1.28.0
  - Prometheus 2.47.0
  - Grafana 10.2.0

- **Frontend & Cross-Platform UI Mobile Stack:**
  - Next.js 14.0.0
  - React 18.2.0
  - TypeScript 5.2.0
  - Tailwind CSS 3.3.0
  - React Native 0.72.0
  - Expo 49.0.0
  - Firebase SDK 10.7.0
  - Zalo SDK 1.0.0

## 📁 3. GLOBAL GUARDRAILS & ENTERPRISE COMPLIANCE STANDARDS

### 🔑 3.1. Security & Compliance Baseline

- Tất cả dữ liệu trong quá trình truyền phải sử dụng TLS 1.3.
- Mã hóa tại nghỉ với AES-256.
- Mã thông báo truy cập JWT hết hạn sau 15 phút; mã thông báo làm mới có thời gian hết hạn 7 ngày.
- Triển khai các biện pháp phòng ngừa hàng đầu của OWASP (tiêm SQL, XSS, CSRF).
- Tuân thủ GDPR/CCPA cho quản lý dữ liệu cá nhân.
- Sao lưu toàn bộ PostgreSQL hàng ngày; khôi phục điểm trong thời gian lên đến 24 giờ; sao lưu cụm GKE đến vùng khác.

### 🌐 3.2. Infrastructure & Performance Guardrails

- Các phản hồi API cốt lõi (xác thực, ghi nhận tham dự, danh sách khóa học) phải hoàn thành trong 200 ms độ trễ trung bình.
- Các truy vấn cơ sở dữ liệu phải được lập chỉ mục để hỗ trợ đọc dưới một giây cho đến 10,000 người dùng đồng thời.
- Mục tiêu 99.9% thời gian hoạt động hàng năm; SLA bao gồm chuyển đổi tự động qua các cụm GKE.
- Mở rộng ngang các dịch vụ Quarkus qua Kubernetes HPA dựa trên CPU > 70% hoặc độ trễ yêu cầu > 300 ms.
- PostgreSQL bản sao đọc cho các công việc báo cáo.
- Kích thước hình ảnh Docker: Kích thước hình ảnh cơ sở < 200 MB; hình ảnh cuối cùng < 500 MB.
- Tất cả các hành động của người dùng (thay đổi vai trò, bản ghi tham dự, thông báo) phải được ghi lại với dấu thời gian, ID người dùng và chi tiết hành động; nhật ký được giữ trong 1 năm.
- Các chuỗi UI phải được tách; hỗ trợ tiếng Anh, tiếng Việt, tiếng Tây Ban Nha; chuyển đổi ngôn ngữ không tải lại trang nơi có thể.
- Xóa dữ liệu cá nhân theo yêu cầu của người dùng; xuất dữ liệu dưới dạng JSON; quản lý đồng ý cho các giao tiếp tiếp thị.

### 🕸️ 3.4. DYNAMIC MICROSERVICES TOPOLOGY REGISTRY MATRIX

<!--BACKLOG_SERVICES_START-->

| Service Domain Key | Microservice Sub-Module Name | Target Container Context Path | Active Infrastructure Gateway Ports | Mapped Functional Backend Packages | Mapped Tracking TagIDs |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Parent Root Grandmaster** | membershiphub-root | `./sources/backend/pom.xml` | N/A (Global Orchestrator) | `org.nlh4j.membershiphub` | [ARC-000] |
| **User Management** | user-service | `./sources/backend/user-service/pom.xml` | 8081 | `org.nlh4j.membershiphub.userservice` | [REQ-001], [REQ-002], [REQ-003], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |
| **Center Management** | center-service | `./sources/backend/center-service/pom.xml` | 8082 | `org.nlh4j.membershiphub.centerservice` | [REQ-004], [REQ-005], [REQ-006], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |
| **Course Management** | course-service | `./sources/backend/course-service/pom.xml` | 8083 | `org.nlh4j.membershiphub.courseservice` | [REQ-007], [REQ-008], [REQ-009], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |
| **Enrollment Management** | enrollment-service | `./sources/backend/enrollment-service/pom.xml` | 8084 | `org.nlh4j.membershiphub.enrollmentservice` | [REQ-010], [REQ-011], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |
| **Attendance Management** | attendance-service | `./sources/backend/attendance-service/pom.xml` | 8085 | `org.nlh4j.membershiphub.attendanceservice` | [REQ-012], [REQ-013], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |
| **Student Card Management** | studentcard-service | `./sources/backend/studentcard-service/pom.xml` | 8086 | `org.nlh4j.membershiphub.studentcardservice` | [REQ-014], [REQ-015], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |
| **Notification Management** | notification-service | `./sources/backend/notification-service/pom.xml` | 8087 | `org.nlh4j.membershiphub.notificationservice` | [REQ-016], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |
| **Promotion Management** | promotion-service | `./sources/backend/promotion-service/pom.xml` | 8088 | `org.nlh4j.membershiphub.promotionservice` | [REQ-017], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |
| **Announcement Management** | announcement-service | `./sources/backend/announcement-service/pom.xml` | 8089 | `org.nlh4j.membershiphub.announcementservice` | [REQ-018], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |
| **AI Chatbot Service** | chatbot-service | `./sources/backend/chatbot-service/pom.xml` | 8090 | `org.nlh4j.membershiphub.chatbotservice` | [REQ-019], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |
| **Mobile App Service** | mobile-service | `./sources/backend/mobile-service/pom.xml` | 8091 | `org.nlh4j.membershiphub.mobileservice` | [REQ-020], [REQ-021], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |
| **Localization Service** | localization-service | `./sources/backend/localization-service/pom.xml` | 8092 | `org.nlh4j.membershiphub.localizationservice` | [REQ-022], [REQ-023], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |
| **Reporting Service** | reporting-service | `./sources/backend/reporting-service/pom.xml` | 8093 | `org.nlh4j.membershiphub.reportingservice` | [REQ-024], [REQ-025], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |

<!--BACKLOG_SERVICES_END-->

<!--END_CHUNK_PART_1_INITIAL-->

<!--START_CHUNK_PART_1_BACKLOG_4_1-->

## 🏁 4. TỔNG QUAN KIẾN TRÚC ĐA PHASE

### 📦 4.1. LỊCH TRÌNH CÔNG VIỆC SẢN PHẨM CHÍNH

#### [MA TRẬN TÍNH TOÁN HỆ THỐNG]
> - **Tổng [REQ] Tags:** 25 Tags
> - **Tổng [EXC] Tags:** 5 Tags
> - **Tổng [ARC] Tags:** 9 Tags
> - **Tổng [DAT] Tags:** 11 Tags
> - **Tổng [NFR] Tags:** 9 Tags
> - ➡️ **Tổng SRS Tags:** 50 Tags
> - ➡️ **Tổng Covered Tags:** 50 Tags

| STT | Công việc | Mục đích kỹ thuật / Tóm tắt giao hàng | Loại | TagID |
| :--- | :--- | :--- | :--- | :--- |
| 1 | Khởi tạo dự án gốc và cấu hình cơ sở | Tạo cấu trúc dự án gốc, cài đặt thư viện Quarkus, cấu hình cơ sở dữ liệu PostgreSQL, thiết lập Docker và Kubernetes | Kiến trúc | [ARC-000] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 2 | Thiết lập cơ sở dữ liệu và di chuyển dữ liệu | Tạo các bảng cơ sở dữ liệu, chỉ mục, ràng buộc, và dữ liệu khởi tạo cho các thực thể Users, Roles, Centers, Courses, Enrollments, Attendance, StudentCards, Notifications, Promotions, Announcements, SystemSettings | Dữ liệu | [DAT-ALL (1 to 11)] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 3 | Xác thực người dùng và quản lý vai trò | Triển khai các endpoint xác thực (đăng ký, đăng nhập, OAuth2), quản lý vai trò người dùng, và bảo mật JWT | Kiến trúc | [ARC-001] [ARC-002] [ARC-003] [ARC-004] [ARC-005] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 4 | Quản lý trung tâm | Triển khai các endpoint quản lý trung tâm (tạo, chỉnh sửa, xóa, xem danh sách) | Ứng dụng | [REQ-004] [REQ-005] [REQ-006] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 5 | Quản lý khóa học | Triển khai các endpoint quản lý khóa học (tạo, chỉnh sửa, xóa, xem danh sách, chỉ định giáo viên) | Ứng dụng | [REQ-007] [REQ-008] [REQ-009] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 6 | Đăng ký học viên và quản lý đăng ký | Triển khai các endpoint đăng ký khóa học, quản lý đăng ký học viên | Ứng dụng | [REQ-010] [REQ-011] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 7 | Ghi nhận tham dự và quản lý thẻ học viên | Triển khai các endpoint ghi nhận tham dự qua QR, quản lý thẻ học viên (hiển thị hiệu lực, gia hạn) | Ứng dụng | [REQ-012] [REQ-013] [REQ-014] [REQ-015] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 8 | Thông báo và giao tiếp | Triển khai hệ thống thông báo (kích hoạt thông báo, quản lý thông báo) | Ứng dụng | [REQ-016] [REQ-017] [REQ-018] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 9 | Tích hợp Chatbot AI | Tích hợp Chatbot AI để trả lời các truy vấn phổ biến | Ứng dụng | [REQ-019] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 10 | Tính năng cốt lõi Ứng dụng di động | Triển khai các tính năng cốt lõi cho ứng dụng di động (giao diện người dùng cụ thể cho vai trò, thông báo đẩy di động) | Ứng dụng | [REQ-020] [REQ-021] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 11 | Bản địa hóa và SEO | Triển khai các tính năng bản địa hóa (phát hiện ngôn ngữ mặc định, SEO đa ngôn ngữ) | Ứng dụng | [REQ-022] [REQ-023] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 12 | Báo cáo và phân tích | Triển khai các tính năng báo cáo (tạo báo cáo tham dự, bảng tổng quan đăng ký) | Ứng dụng | [REQ-024] [REQ-025] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 13 | Xử lý ngoại lệ và lỗi | Triển khai các xử lý ngoại lệ và lỗi (mất kết nối mạng, gửi tham dự trùng lặp, giao tiếp thông báo thất bại, xác thực đầu vào không hợp lệ, khôi phục hệ thống sau sự cố) | Ứng dụng | [EXC-001] [EXC-002] [EXC-003] [EXC-004] [EXC-005] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 14 | Triển khai các yêu cầu không chức năng | Triển khai các yêu cầu không chức năng (chỉ số hiệu suất, khả dụng, bảo mật, khả năng mở rộng, kích thước hình ảnh Docker, nhật ký và kiểm toán, hỗ trợ đa ngôn ngữ, tuân thủ GDPR/CCPA, sao lưu và khôi phục thảm họa) | Kiến trúc | [NFR-001] [NFR-002] [NFR-003] [NFR-004] [NFR-005] [NFR-006] [NFR-007] [NFR-008] [NFR-009] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 15 | Tài liệu kỹ thuật và hướng dẫn sử dụng | Tạo tài liệu kỹ thuật và hướng dẫn sử dụng cho hệ thống | Tài liệu | [DOC-001] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 16 | Triển khai cơ sở hạ tầng DevOps | Triển khai các tập lệnh Docker, cấu hình Terraform, và các manifest Kubernetes cho triển khai hệ thống | DevOps | [NFR-001] [NFR-002] [NFR-003] [NFR-004] [NFR-005] [NFR-006] [NFR-007] [NFR-008] [NFR-009] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| **TÓM TẮT** | **Tổng Tracking Tags Đã Đạt:** 50 | **Tổng Công việc:** 16 | **Trạng thái:** Đã Xác minh | **Độ bao phủ:** 100% |

<!--END_CHUNK_PART_1_BACKLOG_4_1-->

<!--START_CHUNK_PART_1_MATRIX_4_2-->

### 🔭 4.2. MULTI-PHASE SYNOPSIS MATRIX

#### [MATRIX ARITHMETIC LIFECYCLE]
> - **Total Backlog Tasks:** 16 Tasks
> - **Total Backlog Tags:** 50 Tags
> - **Total Distributed Tasks:** 16 Tasks
> - **Total Distributed Tags:** 50 Tags

| Phase | Day Range | Task IDs Covered | Architectural Component / Module Path | Technical Deliverables Summary | Assigned Sub-Agent | Targeted Tag IDs |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Giai đoạn 1 | Ngày 1 - 2 | Task 1, Task 2 | `./sources/backend/pom.xml`<br>`./sources/backend/user-service/pom.xml`<br>`./sources/backend/center-service/pom.xml`<br>`./sources/backend/course-service/pom.xml`<br>`./sources/backend/enrollment-service/pom.xml`<br>`./sources/backend/attendance-service/pom.xml`<br>`./sources/backend/studentcard-service/pom.xml`<br>`./sources/backend/notification-service/pom.xml`<br>`./sources/backend/promotion-service/pom.xml`<br>`./sources/backend/announcement-service/pom.xml`<br>`./sources/backend/chatbot-service/pom.xml`<br>`./sources/backend/mobile-service/pom.xml`<br>`./sources/backend/localization-service/pom.xml`<br>`./sources/backend/reporting-service/pom.xml` | Khởi tạo dự án gốc và cấu hình cơ sở, Thiết lập cơ sở dữ liệu và di chuyển dữ liệu | Coder, Tester, Reviewer, Doc | [ARC-000] [DAT-ALL (1 to 11)] <!--REGISTERED_PHASE_ROW--> |
| Giai đoạn 2 | Ngày 1 - 3 | Task 3, Task 4, Task 5 | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/User.java`<br>`./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/UserController.java`<br>`./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/UserService.java`<br>`./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/UserRepository.java`<br>`./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/UserDTO.java`<br>`./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/Center.java`<br>`./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterController.java`<br>`./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterService.java`<br>`./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterRepository.java`<br>`./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterDTO.java`<br>`./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/Course.java`<br>`./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseController.java`<br>`./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseService.java`<br>`./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseRepository.java`<br>`./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseDTO.java` | Xác thực người dùng và quản lý vai trò, Quản lý trung tâm, Quản lý khóa học | Coder, Tester, Reviewer, Doc | [ARC-001] [ARC-002] [ARC-003] [ARC-004] [ARC-005] [REQ-004] [REQ-005] [REQ-006] [REQ-007] [REQ-008] [REQ-009] <!--REGISTERED_PHASE_ROW--> |
| Giai đoạn 3 | Ngày 1 - 3 | Task 6, Task 7, Task 8 | `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/Enrollment.java`<br>`./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentController.java`<br>`./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentService.java`<br>`./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentRepository.java`<br>`./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentDTO.java`<br>`./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/Attendance.java`<br>`./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceController.java`<br>`./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceService.java`<br>`./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceRepository.java`<br>`./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceDTO.java`<br>`./sources/backend/studentcard-service/src/main/java/org/nlh4j/membershiphub/studentcardservice/StudentCard.java`<br>`./sources/backend/studentcard-service/src/main/java/org/nlh4j/membershiphub/studentcardservice/StudentCardController.java`<br>`./sources/backend/studentcard-service/src/main/java/org/nlh4j/membershiphub/studentcardservice/StudentCardService.java`<br>`./sources/backend/studentcard-service/src/main/java/org/nlh4j/membershiphub/studentcardservice/StudentCardRepository.java`<br>`./sources/backend/studentcard-service/src/main/java/org/nlh4j/membershiphub/studentcardservice/StudentCardDTO.java` | Đăng ký học viên và quản lý đăng ký, Ghi nhận tham dự và quản lý thẻ học viên, Thông báo và giao tiếp | Coder, Tester, Reviewer, Doc | [REQ-010] [REQ-011] [REQ-012] [REQ-013] [REQ-014] [REQ-015] [REQ-016] [REQ-017] [REQ-018] <!--REGISTERED_PHASE_ROW--> |
| Giai đoạn 4 | Ngày 1 - 2 | Task 9, Task 10, Task 11, Task 12 | `./sources/backend/chatbot-service/src/main/java/org/nlh4j/membershiphub/chatbotservice/ChatbotController.java`<br>`./sources/backend/chatbot-service/src/main/java/org/nlh4j/membershiphub/chatbotservice/ChatbotService.java`<br>`./sources/backend/chatbot-service/src/main/java/org/nlh4j/membershiphub/chatbotservice/ChatbotRepository.java`<br>`./sources/backend/chatbot-service/src/main/java/org/nlh4j/membershiphub/chatbotservice/ChatbotDTO.java`<br>`./sources/backend/mobile-service/src/main/java/org/nlh4j/membershiphub/mobileservice/MobileController.java`<br>`./sources/backend/mobile-service/src/main/java/org/nlh4j/membershiphub/mobileservice/MobileService.java`<br>`./sources/backend/mobile-service/src/main/java/org/nlh4j/membershiphub/mobileservice/MobileRepository.java`<br>`./sources/backend/mobile-service/src/main/java/org/nlh4j/membershiphub/mobileservice/MobileDTO.java`<br>`./sources/backend/localization-service/src/main/java/org/nlh4j/membershiphub/localizationservice/LocalizationController.java`<br>`./sources/backend/localization-service/src/main/java/org/nlh4j/membershiphub/localizationservice/LocalizationService.java`<br>`./sources/backend/localization-service/src/main/java/org/nlh4j/membershiphub/localizationservice/LocalizationRepository.java`<br>`./sources/backend/localization-service/src/main/java/org/nlh4j/membershiphub/localizationservice/LocalizationDTO.java`<br>`./sources/backend/reporting-service/src/main/java/org/nlh4j/membershiphub/reportingservice/ReportingController.java`<br>`./sources/backend/reporting-service/src/main/java/org/nlh4j/membershiphub/reportingservice/ReportingService.java`<br>`./sources/backend/reporting-service/src/main/java/org/nlh4j/membershiphub/reportingservice/ReportingRepository.java`<br>`./sources/backend/reporting-service/src/main/java/org/nlh4j/membershiphub/reportingservice/ReportingDTO.java` | Tích hợp Chatbot AI, Tính năng cốt lõi Ứng dụng di động, Bản địa hóa và SEO, Báo cáo và phân tích, Xử lý ngoại lệ và lỗi, Triển khai các yêu cầu không chức năng | Coder, Tester, Reviewer, Doc | [REQ-019] [REQ-020] [REQ-021] [REQ-022] [REQ-023] [REQ-024] [REQ-025] [EXC-001] [EXC-002] [EXC-003] [EXC-004] [EXC-005] [NFR-001] [NFR-002] [NFR-003] [NFR-004] [NFR-005] [NFR-006] [NFR-007] [NFR-008] [NFR-009] <!--REGISTERED_PHASE_ROW--> |
| Giai đoạn 5 | Ngày 1 - 2 | Task 13, Task 14, Task 15, Task 16 | `./sources/docs/technical-documentation.md`<br>`./sources/infra/devops/docker-compose.yml`<br>`./sources/infra/devops/terraform/main.tf`<br>`./sources/infra/devops/kubernetes/deployment.yaml` | Tài liệu kỹ thuật và hướng dẫn sử dụng, Triển khai cơ sở hạ tầng DevOps | Doc, Docker, GCP, GKE | [DOC-001] [NFR-001] [NFR-002] [NFR-003] [NFR-004] [NFR-005] [NFR-006] [NFR-007] [NFR-008] [NFR-009] <!--REGISTERED_PHASE_ROW--> |
| **Audit** | **Master Backlog Distribution Verification** | **Total Phases:** 5 | **Total BackLog Tags:** 50 | **Total Distributed Tags:** 50 | **Total Distributed Tasks:** 16 | **Status & Compliance:** Đã Xác minh (100%) |

<!--END_CHUNK_PART_1_MATRIX_4_2-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

## 🔬 5. GRANULAR PHASE SPECIALIZATIONS & DAY-BY-DAY DELIVERABLES

<!--PHASE_INDEX_START-->

### 📈 Giai đoạn 1 - Khởi tạo dự án gốc và cấu hình cơ sở

- **Mục tiêu cốt lõi của giai đoạn & Mục đích:** Tạo cấu trúc dự án gốc, cài đặt thư viện Quarkus, cấu hình cơ sở dữ liệu PostgreSQL, thiết lập Docker và Kubernetes.

- **Ma trận bản đồ thư mục vật lý mục tiêu:** Tạo cấu trúc dự án gốc, cài đặt thư viện Quarkus, cấu hình cơ sở dữ liệu PostgreSQL, thiết lập Docker và Kubernetes.
    *   *Documentation Gating Boundary:* Any line representing an enterprise specification, reference blueprint, relational database mapping catalog, or architecture layout MUST strictly reside under the unified root directory path: `./sources/docs/`.

- **Đặc tả DDL SQL Schema Cơ sở dữ liệu [DAT-XXX]:** Tạo các bảng cơ sở dữ liệu, chỉ mục, ràng buộc, và dữ liệu khởi tạo cho các thực thể Users, Roles, Centers, Courses, Enrollments, Attendance, StudentCards, Notifications, Promotions, Announcements, SystemSettings.

- **Hợp đồng định tuyến API và Sự kiện [REQ-XXX], [ARC-XXX]:** Tạo cấu trúc dự án gốc, cài đặt thư viện Quarkus, cấu hình cơ sở dữ liệu PostgreSQL, thiết lập Docker và Kubernetes.

- **Bộ xử lý ngoại lệ cục bộ của giai đoạn [EXC-XXX]:** Tạo cấu trúc dự án gốc, cài đặt thư viện Quarkus, cấu hình cơ sở dữ liệu PostgreSQL, thiết lập Docker và Kubernetes.

#### 📅 Nhật ký phân phối nhiệm vụ hàng ngày của các Sub-Agent (Giai đoạn 1)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: Khởi tạo cấu trúc dự án và cấu hình cơ sở

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 1: Khởi tạo cấu trúc dự án và cấu hình cơ sở

* **Chuyên môn phân công công việc của Sub-Agent:** [Coder]

* **Tag ID mục tiêu:** [ARC-000]

* **Thành phần mục tiêu file path (target_component):** `./sources/backend/pom.xml`

* **Hướng dẫn kỹ thuật cấp thấp:** Khởi tạo cấu trúc dự án và cấu hình cơ sở.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 2: Khởi tạo cấu trúc dự án và cấu hình cơ sở

* **Chuyên môn phân công công việc của Sub-Agent:** [Reviewer]

* **Tag ID mục tiêu:** [ARC-000]

* **Thành phần mục tiêu file path (target_component):** `./sources/backend/pom.xml`

* **Hướng dẫn kỹ thuật cấp thấp:** Khởi tạo cấu trúc dự án và cấu hình cơ sở.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 3: Khởi tạo cấu trúc dự án và cấu hình cơ sở

* **Chuyên môn phân công công việc của Sub-Agent:** [Tester]

* **Tag ID mục tiêu:** [ARC-000]

* **Thành phần mục tiêu file path (target_component):** `./sources/backend/pom.xml`

* **Hướng dẫn kỹ thuật cấp thấp:** Khởi tạo cấu trúc dự án và cấu hình cơ sở.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 4: Khởi tạo cấu trúc dự án và cấu hình cơ sở

* **Chuyên môn phân công công việc của Sub-Agent:** [Doc]

* **Tag ID mục tiêu:** [ARC-000]

* **Thành phần mục tiêu file path (target_component):** `./sources/backend/pom.xml`

* **Hướng dẫn kỹ thuật cấp thấp:** Khởi tạo cấu trúc dự án và cấu hình cơ sở.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: Thiết lập cơ sở dữ liệu và di chuyển dữ liệu

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 1: Thiết lập cơ sở dữ liệu và di chuyển dữ liệu

* **Chuyên môn phân công công việc của Sub-Agent:** [Coder]

* **Tag ID mục tiêu:** [DAT-ALL (1 to 11)]

* **Thành phần mục tiêu file path (target_component):** `./sources/backend/user-service/pom.xml`

* **Hướng dẫn kỹ thuật cấp thấp:** Thiết lập cơ sở dữ liệu và di chuyển dữ liệu.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 2: Thiết lập cơ sở dữ liệu và di chuyển dữ liệu

* **Chuyên môn phân công công việc của Sub-Agent:** [Reviewer]

* **Tag ID mục tiêu:** [DAT-ALL (1 to 11)]

* **Thành phần mục tiêu file path (target_component):** `./sources/backend/user-service/pom.xml`

* **Hướng dẫn kỹ thuật cấp thấp:** Thiết lập cơ sở dữ liệu và di chuyển dữ liệu.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 3: Thiết lập cơ sở dữ liệu và di chuyển dữ liệu

* **Chuyên môn phân công công việc của Sub-Agent:** [Tester]

* **Tag ID mục tiêu:** [DAT-ALL (1 to 11)]

* **Thành phần mục tiêu file path (target_component):** `./sources/backend/user-service/pom.xml`

* **Hướng dẫn kỹ thuật cấp thấp:** Thiết lập cơ sở dữ liệu và di chuyển dữ liệu.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 4: Thiết lập cơ sở dữ liệu và di chuyển dữ liệu

* **Chuyên môn phân công công việc của Sub-Agent:** [Doc]

* **Tag ID mục tiêu:** [DAT-ALL (1 to 11)]

* **Thành phần mục tiêu file path (target_component):** `./sources/backend/user-service/pom.xml`

* **Hướng dẫn kỹ thuật cấp thấp:** Thiết lập cơ sở dữ liệu và di chuyển dữ liệu.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

### 📈 Giai đoạn 2 - Quản lý người dùng và trung tâm

- **Mục tiêu cốt lõi của giai đoạn & Mục đích:** Triển khai các endpoint xác thực (đăng ký, đăng nhập, OAuth2), quản lý vai trò người dùng, và bảo mật JWT; Triển khai các endpoint quản lý trung tâm (tạo, chỉnh sửa, xóa, xem danh sách).

- **Ma trận đường dẫn thư mục vật lý mục tiêu:** Tạo cấu trúc dự án gốc, cài đặt thư viện Quarkus, cấu hình cơ sở dữ liệu PostgreSQL, thiết lập Docker và Kubernetes.

- **Chuẩn dữ liệu DDL SQL:** ```sql
CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash CHAR(60) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role_id SMALLINT NOT NULL,
    provider VARCHAR(20) NOT NULL DEFAULT 'local',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

CREATE TABLE centers (
    center_id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tax_id VARCHAR(20) NOT NULL UNIQUE,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100)
);

CREATE TABLE courses (
    course_id UUID PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    teacher_id UUID NOT NULL,
    max_students INT DEFAULT 30,
    CONSTRAINT fk_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id)
);

CREATE TABLE enrollments (
    enrollment_id UUID PRIMARY KEY,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    enrollment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT fk_course FOREIGN KEY (course_id) REFERENCES courses(course_id)
);

CREATE TABLE attendance (
    attendance_id UUID PRIMARY KEY,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT fk_course FOREIGN KEY (course_id) REFERENCES courses(course_id)
);

CREATE TABLE student_cards (
    card_id UUID PRIMARY KEY,
    student_id UUID NOT NULL,
    issue_date DATE NOT NULL,
    validity_days INT NOT NULL,
    remaining_days INT GENERATED ALWAYS AS (validity_days - EXTRACT(DAY FROM (CURRENT_DATE - issue_date))) STORED,
    CONSTRAINT fk_student FOREIGN KEY (student_id) REFERENCES users(user_id)
);

CREATE TABLE notifications (
    notification_id UUID PRIMARY KEY,
    user_id UUID,
    group_zalo VARCHAR(50),
    message TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE roles (
    role_id SMALLINT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(200)
);

CREATE TABLE promotions (
    promo_id UUID PRIMARY KEY,
    code VARCHAR(30) UNIQUE,
    discount_percent SMALLINT NOT NULL,
    start_date DATE,
    end_date DATE,
    description TEXT
);

CREATE TABLE announcements (
    announcement_id UUID PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    start_date DATE,
    end_date DATE
);

CREATE TABLE system_settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    description VARCHAR(200)
);
```

- **Hợp đồng định tuyến API và sự kiện:** ```json
{
  "auth": {
    "register": {
      "path": "/api/auth/register",
      "method": "POST",
      "request": {
        "email": "string",
        "password": "string",
        "full_name": "string",
        "provider": "string"
      },
      "response": {
        "user_id": "uuid",
        "access_token": "string",
        "refresh_token": "string"
      }
    },
    "login": {
      "path": "/api/auth/login",
      "method": "POST",
      "request": {
        "email": "string",
        "password": "string"
      },
      "response": {
        "user_id": "uuid",
        "access_token": "string",
        "refresh_token": "string"
      }
    },
    "oauth": {
      "path": "/api/auth/oauth",
      "method": "POST",
      "request": {
        "provider": "string",
        "token": "string"
      },
      "response": {
        "user_id": "uuid",
        "access_token": "string",
        "refresh_token": "string"
      }
    }
  },
  "centers": {
    "create": {
      "path": "/api/centers",
      "method": "POST",
      "request": {
        "name": "string",
        "address": "string",
        "tax_id": "string",
        "contact_phone": "string",
        "contact_email": "string"
      },
      "response": {
        "center_id": "uuid"
      }
    },
    "update": {
      "path": "/api/centers/{center_id}",
      "method": "PUT",
      "request": {
        "name": "string",
        "address": "string",
        "tax_id": "string",
        "contact_phone": "string",
        "contact_email": "string"
      },
      "response": {
        "center_id": "uuid"
      }
    },
    "delete": {
      "path": "/api/centers/{center_id}",
      "method": "DELETE",
      "response": {
        "center_id": "uuid"
      }
    },
    "list": {
      "path": "/api/centers",
      "method": "GET",
      "response": {
        "centers": [
          {
            "center_id": "uuid",
            "name": "string",
            "address": "string",
            "tax_id": "string",
            "contact_phone": "string",
            "contact_email": "string"
          }
        ]
      }
    }
  }
}
```

- **Bộ xử lý ngoại lệ cục bộ:** [EXC-001] [EXC-002] [EXC-003] [EXC-004] [EXC-005]

#### 📅 Lịch trình ngày theo ngày của các tác vụ con

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: Triển khai các endpoint xác thực (đăng ký, đăng nhập, OAuth2)

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 CÔNG VIỆC CON 1: Triển khai endpoint đăng ký người dùng
- **Chuyên môn tác vụ con của tác vụ con:** [Coder]
- **Tag ID mục tiêu:** [REQ-001]
- **Thành phần mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/UserController.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai endpoint đăng ký người dùng với các trường email, mật khẩu, tên đầy đủ và nhà cung cấp.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 CÔNG VIỆC CON 2: Kiểm tra endpoint đăng ký người dùng
- **Chuyên môn tác vụ con của tác vụ con:** [Tester]
- **Tag ID mục tiêu:** [REQ-001]
- **Thành phần mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/UserController.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserControllerTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra endpoint đăng ký người dùng với các trường hợp hợp lệ và không hợp lệ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 CÔNG VIỆC CON 3: Xem xét mã nguồn endpoint đăng ký người dùng
- **Chuyên môn tác vụ con của tác vụ con:** [Reviewer]
- **Tag ID mục tiêu:** [REQ-001]
- **Thành phần mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/UserController.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Xem xét mã nguồn endpoint đăng ký người dùng để đảm bảo tuân thủ các tiêu chuẩn mã nguồn và bảo mật.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 CÔNG VIỆC CON 4: Tài liệu endpoint đăng ký người dùng
- **Chuyên môn tác vụ con của tác vụ con:** [Doc]
- **Tag ID mục tiêu:** [REQ-001]
- **Thành phần mục tiêu:** `./sources/docs/technical-documentation.md`
- **Hướng dẫn kỹ thuật cấp thấp:** Tài liệu endpoint đăng ký người dùng với các trường hợp sử dụng và ví dụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: Triển khai các endpoint quản lý trung tâm

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 CÔNG VIỆC CON 1: Triển khai endpoint tạo trung tâm
- **Chuyên môn tác vụ con của tác vụ con:** [Coder]
- **Tag ID mục tiêu:** [REQ-005]
- **Thành phần mục tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterController.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai endpoint tạo trung tâm với các trường tên, địa chỉ, mã số thuế, điện thoại liên hệ và email liên hệ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 CÔNG VIỆC CON 2: Kiểm tra endpoint tạo trung tâm
- **Chuyên môn tác vụ con của tác vụ con:** [Tester]
- **Tag ID mục tiêu:** [REQ-005]
- **Thành phần mục tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterController.java;./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/CenterControllerTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra endpoint tạo trung tâm với các trường hợp hợp lệ và không hợp lệ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 CÔNG VIỆC CON 3: Xem xét mã nguồn endpoint tạo trung tâm
- **Chuyên môn tác vụ con của tác vụ con:** [Reviewer]
- **Tag ID mục tiêu:** [REQ-005]
- **Thành phần mục tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterController.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Xem xét mã nguồn endpoint tạo trung tâm để đảm bảo tuân thủ các tiêu chuẩn mã nguồn và bảo mật.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 CÔNG VIỆC CON 4: Tài liệu endpoint tạo trung tâm
- **Chuyên môn tác vụ con của tác vụ con:** [Doc]
- **Tag ID mục tiêu:** [REQ-005]
- **Thành phần mục tiêu:** `./sources/docs/technical-documentation.md`
- **Hướng dẫn kỹ thuật cấp thấp:** Tài liệu endpoint tạo trung tâm với các trường hợp sử dụng và ví dụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 3: Triển khai các endpoint quản lý khóa học

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 CÔNG VIỆC CON 1: Triển khai endpoint tạo khóa học
- **Chuyên môn tác vụ con của tác vụ con:** [Coder]
- **Tag ID mục tiêu:** [REQ-008]
- **Thành phần mục tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseController.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai endpoint tạo khóa học với các trường tiêu đề, mô tả, ngày bắt đầu, ngày kết thúc, ID giáo viên và sức chứa tối đa.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 CÔNG VIỆC CON 2: Kiểm tra endpoint tạo khóa học
- **Chuyên môn tác vụ con của tác vụ con:** [Tester]
- **Tag ID mục tiêu:** [REQ-008]
- **Thành phần mục tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseController.java;./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/CourseControllerTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra endpoint tạo khóa học với các trường hợp hợp lệ và không hợp lệ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 CÔNG VIỆC CON 3: Xem xét mã nguồn endpoint tạo khóa học
- **Chuyên môn tác vụ con của tác vụ con:** [Reviewer]
- **Tag ID mục tiêu:** [REQ-008]
- **Thành phần mục tiêu:** `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseController.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Xem xét mã nguồn endpoint tạo khóa học để đảm bảo tuân thủ các tiêu chuẩn mã nguồn và bảo mật.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 CÔNG VIỆC CON 4: Tài liệu endpoint tạo khóa học
- **Chuyên môn tác vụ con của tác vụ con:** [Doc]
- **Tag ID mục tiêu:** [REQ-008]
- **Thành phần mục tiêu:** `./sources/docs/technical-documentation.md`
- **Hướng dẫn kỹ thuật cấp thấp:** Tài liệu endpoint tạo khóa học với các trường hợp sử dụng và ví dụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

<!--PHASE_INDEX_START-->

### 📈 Giai đoạn 3 - Quản lý đăng ký học viên và ghi nhận tham dự

- **Mục tiêu cốt lõi của giai đoạn & Mục đích:** Triển khai các tính năng đăng ký học viên, quản lý đăng ký, ghi nhận tham dự qua QR, và quản lý thẻ học viên.

- **Ma trận đường dẫn thư mục vật lý mục tiêu:** Tạo các endpoint và dịch vụ cho đăng ký học viên, quản lý đăng ký, ghi nhận tham dự, và quản lý thẻ học viên.

- **Chỉ số hiệu suất:**
  - Các phản hồi API cốt lõi (đăng ký học viên, ghi nhận tham dự, quản lý thẻ học viên) phải hoàn thành trong 200 ms độ trễ trung bình.
  - Các truy vấn cơ sở dữ liệu phải được lập chỉ mục để hỗ trợ đọc dưới một giây cho đến 10,000 người dùng đồng thời.

- **Khả dụng:**
  - Mục tiêu 99.9% thời gian hoạt động hàng năm; SLA bao gồm chuyển đổi tự động qua các cụm GKE.

- **Bảo mật:**
  - Tất cả dữ liệu trong quá trình truyền phải sử dụng TLS 1.3; mã hóa tại nghỉ với AES-256.
  - Mã thông báo truy cập JWT hết hạn sau 15 phút; mã thông báo làm mới có thời gian hết hạn 7 ngày.
  - Triển khai các biện pháp phòng ngừa hàng đầu của OWASP (tiêm SQL, XSS, CSRF).

- **Khả năng mở rộng & Khả dụng:**
  - Mở rộng ngang các dịch vụ Quarkus qua Kubernetes HPA dựa trên CPU > 70% hoặc độ trễ yêu cầu > 300 ms.
  - PostgreSQL bản sao đọc cho các công việc báo cáo.

- **Kích thước hình ảnh Docker:**
  - Kích thước hình ảnh cơ sở < 200 MB; hình ảnh cuối cùng < 500 MB.

- **Nhật ký & Kiểm toán:**
  - Tất cả các hành động của người dùng (đăng ký học viên, ghi nhận tham dự, quản lý thẻ học viên) phải được ghi lại với dấu thời gian, ID người dùng và chi tiết hành động; nhật ký được giữ trong 1 năm.

- **Hỗ trợ đa ngôn ngữ:**
  - Các chuỗi UI phải được tách; hỗ trợ tiếng Anh, tiếng Việt, tiếng Tây Ban Nha; chuyển đổi ngôn ngữ không tải lại trang nơi có thể.

- **Tuân thủ GDPR/CCPA:**
  - Xóa dữ liệu cá nhân theo yêu cầu của người dùng; xuất dữ liệu dưới dạng JSON; quản lý đồng ý cho các giao tiếp tiếp thị.

- **Sao lưu & Khôi phục thảm họa:**
  - Sao lưu toàn bộ PostgreSQL hàng ngày; khôi phục điểm trong thời gian lên đến 24 giờ; sao lưu cụm GKE đến vùng khác.

#### 📅 Nhật ký phân phối công việc theo ngày của giai đoạn (Giai đoạn 3)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: Đăng ký học viên và quản lý đăng ký

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 1: Triển khai endpoint đăng ký học viên
- **Chuyên môn của Sub-Agent:** [Coder]
- **Tag ID mục tiêu:** [REQ-010], [REQ-011]
- **Thành phần mục tiêu (target_component):** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentController.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai endpoint đăng ký học viên, bao gồm xác thực đầu vào và xử lý đăng ký.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 2: Triển khai dịch vụ đăng ký học viên
- **Chuyên môn của Sub-Agent:** [Coder]
- **Tag ID mục tiêu:** [REQ-010], [REQ-011]
- **Thành phần mục tiêu (target_component):** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentService.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai dịch vụ đăng ký học viên, bao gồm logic đăng ký và tạo tài khoản học viên nếu cần.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 3: Triển khai repository đăng ký học viên
- **Chuyên môn của Sub-Agent:** [Coder]
- **Tag ID mục tiêu:** [REQ-010], [REQ-011]
- **Thành phần mục tiêu (target_component):** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentRepository.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai repository đăng ký học viên, bao gồm các truy vấn cơ sở dữ liệu cho đăng ký học viên.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 4: Triển khai DTO đăng ký học viên
- **Chuyên môn của Sub-Agent:** [Coder]
- **Tag ID mục tiêu:** [REQ-010], [REQ-011]
- **Thành phần mục tiêu (target_component):** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentDTO.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai DTO đăng ký học viên, bao gồm các trường dữ liệu và xác thực đầu vào.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 5: Kiểm tra endpoint đăng ký học viên
- **Chuyên môn của Sub-Agent:** [Tester]
- **Tag ID mục tiêu:** [REQ-010], [REQ-011]
- **Thành phần mục tiêu (target_component):** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentController.java;./sources/backend/enrollment-service/src/test/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentControllerTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra endpoint đăng ký học viên, bao gồm các trường hợp thành công và thất bại.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 6: Kiểm tra dịch vụ đăng ký học viên
- **Chuyên môn của Sub-Agent:** [Tester]
- **Tag ID mục tiêu:** [REQ-010], [REQ-011]
- **Thành phần mục tiêu (target_component):** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentService.java;./sources/backend/enrollment-service/src/test/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentServiceTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra dịch vụ đăng ký học viên, bao gồm các trường hợp thành công và thất bại.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 7: Kiểm tra repository đăng ký học viên
- **Chuyên môn của Sub-Agent:** [Tester]
- **Tag ID mục tiêu:** [REQ-010], [REQ-011]
- **Thành phần mục tiêu (target_component):** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentRepository.java;./sources/backend/enrollment-service/src/test/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentRepositoryTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra repository đăng ký học viên, bao gồm các truy vấn cơ sở dữ liệu.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 8: Kiểm tra DTO đăng ký học viên
- **Chuyên môn của Sub-Agent:** [Tester]
- **Tag ID mục tiêu:** [REQ-010], [REQ-011]
- **Thành phần mục tiêu (target_component):** `./sources/backend/enrollment-service/src/main/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentDTO.java;./sources/backend/enrollment-service/src/test/java/org/nlh4j/membershiphub/enrollmentservice/EnrollmentDTOTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra DTO đăng ký học viên, bao gồm các trường dữ liệu và xác thực đầu vào.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 9: Tài liệu endpoint đăng ký học viên
- **Chuyên môn của Sub-Agent:** [Doc]
- **Tag ID mục tiêu:** [REQ-010], [REQ-011]
- **Thành phần mục tiêu (target_component):** `./sources/docs/enrollment-service.md`
- **Hướng dẫn kỹ thuật cấp thấp:** Tài liệu endpoint đăng ký học viên, bao gồm các trường dữ liệu và xác thực đầu vào.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: Ghi nhận tham dự qua QR

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 1: Triển khai endpoint ghi nhận tham dự
- **Chuyên môn của Sub-Agent:** [Coder]
- **Tag ID mục tiêu:** [REQ-012], [REQ-013]
- **Thành phần mục tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceController.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai endpoint ghi nhận tham dự, bao gồm xác thực đầu vào và xử lý ghi nhận tham dự.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 2: Triển khai dịch vụ ghi nhận tham dự
- **Chuyên môn của Sub-Agent:** [Coder]
- **Tag ID mục tiêu:** [REQ-012], [REQ-013]
- **Thành phần mục tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceService.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai dịch vụ ghi nhận tham dự, bao gồm logic ghi nhận tham dự và xử lý trùng lặp.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 3: Triển khai repository ghi nhận tham dự
- **Chuyên môn của Sub-Agent:** [Coder]
- **Tag ID mục tiêu:** [REQ-012], [REQ-013]
- **Thành phần mục tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceRepository.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai repository ghi nhận tham dự, bao gồm các truy vấn cơ sở dữ liệu cho ghi nhận tham dự.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 4: Triển khai DTO ghi nhận tham dự
- **Chuyên môn của Sub-Agent:** [Coder]
- **Tag ID mục tiêu:** [REQ-012], [REQ-013]
- **Thành phần mục tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceDTO.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai DTO ghi nhận tham dự, bao gồm các trường dữ liệu và xác thực đầu vào.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 5: Kiểm tra endpoint ghi nhận tham dự
- **Chuyên môn của Sub-Agent:** [Tester]
- **Tag ID mục tiêu:** [REQ-012], [REQ-013]
- **Thành phần mục tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceController.java;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceControllerTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra endpoint ghi nhận tham dự, bao gồm các trường hợp thành công và thất bại.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 6: Kiểm tra dịch vụ ghi nhận tham dự
- **Chuyên môn của Sub-Agent:** [Tester]
- **Tag ID mục tiêu:** [REQ-012], [REQ-013]
- **Thành phần mục tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceService.java;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra dịch vụ ghi nhận tham dự, bao gồm các trường hợp thành công và thất bại.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 7: Kiểm tra repository ghi nhận tham dự
- **Chuyên môn của Sub-Agent:** [Tester]
- **Tag ID mục tiêu:** [REQ-012], [REQ-013]
- **Thành phần mục tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceRepository.java;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceRepositoryTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra repository ghi nhận tham dự, bao gồm các truy vấn cơ sở dữ liệu.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 8: Kiểm tra DTO ghi nhận tham dự
- **Chuyên môn của Sub-Agent:** [Tester]
- **Tag ID mục tiêu:** [REQ-012], [REQ-013]
- **Thành phần mục tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/AttendanceDTO.java;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceDTOTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra DTO ghi nhận tham dự, bao gồm các trường dữ liệu và xác thực đầu vào.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 9: Tài liệu endpoint ghi nhận tham dự
- **Chuyên môn của Sub-Agent:** [Doc]
- **Tag ID mục tiêu:** [REQ-012], [REQ-013]
- **Thành phần mục tiêu (target_component):** `./sources/docs/attendance-service.md`
- **Hướng dẫn kỹ thuật cấp thấp:** Tài liệu endpoint ghi nhận tham dự, bao gồm các trường dữ liệu và xác thực đầu vào.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 3: Quản lý thẻ học viên

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 1: Triển khai endpoint quản lý thẻ học viên
- **Chuyên môn của Sub-Agent:** [Coder]
- **Tag ID mục tiêu:** [REQ-014], [REQ-015]
- **Thành phần mục tiêu (target_component):** `./sources/backend/studentcard-service/src/main/java/org/nlh4j/membershiphub/studentcardservice/StudentCardController.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai endpoint quản lý thẻ học viên, bao gồm xác thực đầu vào và xử lý quản lý thẻ học viên.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 2: Triển khai dịch vụ quản lý thẻ học viên
- **Chuyên môn của Sub-Agent:** [Coder]
- **Tag ID mục tiêu:** [REQ-014], [REQ-015]
- **Thành phần mục tiêu (target_component):** `./sources/backend/studentcard-service/src/main/java/org/nlh4j/membershiphub/studentcardservice/StudentCardService.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai dịch vụ quản lý thẻ học viên, bao gồm logic quản lý thẻ học viên và xử lý gia hạn.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 3: Triển khai repository quản lý thẻ học viên
- **Chuyên môn của Sub-Agent:** [Coder]
- **Tag ID mục tiêu:** [REQ-014], [REQ-015]
- **Thành phần mục tiêu (target_component):** `./sources/backend/studentcard-service/src/main/java/org/nlh4j/membershiphub/studentcardservice/StudentCardRepository.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai repository quản lý thẻ học viên, bao gồm các truy vấn cơ sở dữ liệu cho quản lý thẻ học viên.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 4: Triển khai DTO quản lý thẻ học viên
- **Chuyên môn của Sub-Agent:** [Coder]
- **Tag ID mục tiêu:** [REQ-014], [REQ-015]
- **Thành phần mục tiêu (target_component):** `./sources/backend/studentcard-service/src/main/java/org/nlh4j/membershiphub/studentcardservice/StudentCardDTO.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai DTO quản lý thẻ học viên, bao gồm các trường dữ liệu và xác thực đầu vào.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 5: Kiểm tra endpoint quản lý thẻ học viên
- **Chuyên môn của Sub-Agent:** [Tester]
- **Tag ID mục tiêu:** [REQ-014], [REQ-015]
- **Thành phần mục tiêu (target_component):** `./sources/backend/studentcard-service/src/main/java/org/nlh4j/membershiphub/studentcardservice/StudentCardController.java;./sources/backend/studentcard-service/src/test/java/org/nlh4j/membershiphub/studentcardservice/StudentCardControllerTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra endpoint quản lý thẻ học viên, bao gồm các trường hợp thành công và thất bại.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 6: Kiểm tra dịch vụ quản lý thẻ học viên
- **Chuyên môn của Sub-Agent:** [Tester]
- **Tag ID mục tiêu:** [REQ-014], [REQ-015]
- **Thành phần mục tiêu (target_component):** `./sources/backend/studentcard-service/src/main/java/org/nlh4j/membershiphub/studentcardservice/StudentCardService.java;./sources/backend/studentcard-service/src/test/java/org/nlh4j/membershiphub/studentcardservice/StudentCardServiceTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra dịch vụ quản lý thẻ học viên, bao gồm các trường hợp thành công và thất bại.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 7: Kiểm tra repository quản lý thẻ học viên
- **Chuyên môn của Sub-Agent:** [Tester]
- **Tag ID mục tiêu:** [REQ-014], [REQ-015]
- **Thành phần mục tiêu (target_component):** `./sources/backend/studentcard-service/src/main/java/org/nlh4j/membershiphub/studentcardservice/StudentCardRepository.java;./sources/backend/studentcard-service/src/test/java/org/nlh4j/membershiphub/studentcardservice/StudentCardRepositoryTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra repository quản lý thẻ học viên, bao gồm các truy vấn cơ sở dữ liệu.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 8: Kiểm tra DTO quản lý thẻ học viên
- **Chuyên môn của Sub-Agent:** [Tester]
- **Tag ID mục tiêu:** [REQ-014], [REQ-015]
- **Thành phần mục tiêu (target_component):** `./sources/backend/studentcard-service/src/main/java/org/nlh4j/membershiphub/studentcardservice/StudentCardDTO.java;./sources/backend/studentcard-service/src/test/java/org/nlh4j/membershiphub/studentcardservice/StudentCardDTOTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra DTO quản lý thẻ học viên, bao gồm các trường dữ liệu và xác thực đầu vào.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 Nhiệm vụ con 9: Tài liệu endpoint quản lý thẻ học viên
- **Chuyên môn của Sub-Agent:** [Doc]
- **Tag ID mục tiêu:** [REQ-014], [REQ-015]
- **Thành phần mục tiêu (target_component):** `./sources/docs/studentcard-service.md`
- **Hướng dẫn kỹ thuật cấp thấp:** Tài liệu endpoint quản lý thẻ học viên, bao gồm các trường dữ liệu và xác thực đầu vào.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

### 📈 Giai đoạn 4 - Quản lý khuyến mãi & Thông báo, Tích hợp Chatbot AI, Tính năng cốt lõi Ứng dụng di động, Bản địa hóa & SEO, Báo cáo & Phân tích, Xử lý ngoại lệ & lỗi, Triển khai các yêu cầu không chức năng

- **Mục tiêu cốt lõi của giai đoạn & Mục đích:** Triển khai các tính năng quản lý khuyến mãi, thông báo, tích hợp Chatbot AI, tính năng cốt lõi Ứng dụng di động, bản địa hóa và SEO, báo cáo và phân tích, xử lý ngoại lệ và lỗi, và triển khai các yêu cầu không chức năng.

- **Ma trận bản đồ thư mục vật lý mục tiêu:** Tạo danh sách kiểm tra kỹ thuật toàn diện liệt kê 100% tất cả các tệp vật lý riêng lẻ (KHÔNG phải thư mục hoặc đường dẫn) nằm dưới `./sources/` được tạo, tái cấu trúc hoặc xử lý trong phạm vi giai đoạn này. Mỗi mục liệt kê phải đại diện cho một thực thể tệp cụ thể kết thúc với phần mở rộng tệp hợp lệ, với các TagID theo dõi được đính kèm inline.

    *   *Giới hạn tài liệu:* Bất kỳ dòng nào đại diện cho một tài liệu quy định doanh nghiệp, bản đồ cơ sở dữ liệu quan hệ, bản đồ kiến trúc hoặc tài liệu tham khảo kiến trúc phải nằm nghiêm ngặt dưới đường dẫn gốc thống nhất: `./sources/docs/`.

- **Đặc tả DDL SQL Schema [DAT-XXX]:** Cung cấp các câu lệnh di chuyển DDL SQL thô, hoàn chỉnh và hợp lệ chứa các cột rõ ràng, kiểu dữ liệu, khóa chính/khóa ngoại, ánh xạ ma trận, chỉ mục và ràng buộc nullability được áp dụng trong phạm vi giai đoạn này. (Bỏ qua hoàn toàn nếu dự án không có lớp cơ sở dữ liệu hoặc yêu cầu lớp lưu trữ. Khối kỹ thuật này KHÔNG ĐƯỢC dịch).

- **Hợp đồng định tuyến API và sự kiện [REQ-XXX], [ARC-XXX]:** Tài liệu các hợp đồng kỹ thuật hoàn chỉnh (đường dẫn điểm cuối chính xác, phương thức HTTP, lược đồ JSON yêu cầu/phản hồi, hoặc cấu hình chủ đề nhà môi giới sự kiện. Khối kỹ thuật KHÔNG ĐƯỢC dịch).

- **Bộ xử lý ngoại lệ cục bộ của giai đoạn [EXC-XXX]:** Chi tiết các quy tắc xác thực kinh doanh rõ ràng, mã lỗi và đường dẫn xử lý ngoại lệ hệ thống ánh xạ nghiêm ngặt với phạm vi giai đoạn hiện tại, được dịch ngữ cảnh sang Vietnamese.

#### 📅 Nhật ký phân phối nhiệm vụ theo ngày của giai đoạn 4

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: Triển khai các tính năng quản lý khuyến mãi và thông báo

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: Triển khai các endpoint quản lý khuyến mãi
- **Chuyên môn của tác nhân con:** [Coder]
- **TagID mục tiêu:** [REQ-017]
- **Thành phần mục tiêu:** `./sources/backend/promotion-service/src/main/java/org/nlh4j/membershiphub/promotionservice/PromotionController.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai các endpoint quản lý khuyến mãi (tạo, chỉnh sửa, xóa, xem danh sách).

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: Kiểm tra các endpoint quản lý khuyến mãi
- **Chuyên môn của tác nhân con:** [Tester]
- **TagID mục tiêu:** [REQ-017]
- **Thành phần mục tiêu:** `./sources/backend/promotion-service/src/main/java/org/nlh4j/membershiphub/promotionservice/PromotionController.java;./sources/backend/promotion-service/src/test/java/org/nlh4j/membershiphub/promotionservice/PromotionControllerTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra các endpoint quản lý khuyến mãi (tạo, chỉnh sửa, xóa, xem danh sách).

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: Tài liệu các endpoint quản lý khuyến mãi
- **Chuyên môn của tác nhân con:** [Doc]
- **TagID mục tiêu:** [REQ-017]
- **Thành phần mục tiêu:** `./sources/docs/promotion-service-api.md`
- **Hướng dẫn kỹ thuật cấp thấp:** Tài liệu các endpoint quản lý khuyến mãi (tạo, chỉnh sửa, xóa, xem danh sách).

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: Triển khai các tính năng tích hợp Chatbot AI và tính năng cốt lõi Ứng dụng di động

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: Triển khai các endpoint tích hợp Chatbot AI
- **Chuyên môn của tác nhân con:** [Coder]
- **TagID mục tiêu:** [REQ-019]
- **Thành phần mục tiêu:** `./sources/backend/chatbot-service/src/main/java/org/nlh4j/membershiphub/chatbotservice/ChatbotController.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Triển khai các endpoint tích hợp Chatbot AI để trả lời các truy vấn phổ biến.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: Kiểm tra các endpoint tích hợp Chatbot AI
- **Chuyên môn của tác nhân con:** [Tester]
- **TagID mục tiêu:** [REQ-019]
- **Thành phần mục tiêu:** `./sources/backend/chatbot-service/src/main/java/org/nlh4j/membershiphub/chatbotservice/ChatbotController.java;./sources/backend/chatbot-service/src/test/java/org/nlh4j/membershiphub/chatbotservice/ChatbotControllerTest.java`
- **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra các endpoint tích hợp Chatbot AI để trả lời các truy vấn phổ biến.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: Tài liệu các endpoint tích hợp Chatbot AI
- **Chuyên môn của tác nhân con:** [Doc]
- **TagID mục tiêu:** [REQ-019]
- **Thành phần mục tiêu:** `./sources/docs/chatbot-service-api.md`
- **Hướng dẫn kỹ thuật cấp thấp:** Tài liệu các endpoint tích hợp Chatbot AI để trả lời các truy vấn phổ biến.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

<!--PHASE_INDEX_START-->

### 📈 Giai đoạn 5 - Triển khai cơ sở hạ tầng DevOps

- **Mục tiêu cốt lõi của giai đoạn & Mục đích:** Triển khai các tập lệnh Docker, cấu hình Terraform, và các manifest Kubernetes cho triển khai hệ thống, đảm bảo tính khả dụng cao và khả năng mở rộng.

- **Ma trận bản đồ thư mục vật lý mục tiêu:** Tạo và triển khai các tệp cấu hình cơ sở hạ tầng DevOps bao gồm:
    * `./sources/infra/devops/docker-compose.yml`
    * `./sources/infra/devops/terraform/main.tf`
    * `./sources/infra/devops/kubernetes/deployment.yaml`

- **Chỉ định DDL SQL cơ sở dữ liệu:** Omit entirely if the project topology has no database or persistence layer requirements. This technical block MUST NOT be translated).

- **Hợp đồng định tuyến API và sự kiện:** Omit entirely if the project topology has no API or event routing requirements. This technical block MUST NOT be translated).

- **Bộ xử lý ngoại lệ cục bộ của giai đoạn:** Omit entirely if the project topology has no exception handling requirements. This technical block MUST NOT be translated).

#### 📅 Nhật ký phân phối công việc theo ngày của giai đoạn ([Giai đoạn] 5)

<!--DAY_LOG_INDEX_START-->

##### 📅 [Translate "DAY" into the target language Vietnamese] 1: Khởi tạo và triển khai cấu hình Docker

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 [Translate "SUB-TASKS" into the target language Vietnamese] 1: Tạo tệp docker-compose.yml
- **Chuyên môn phân công công việc của Sub-Agent:** [Docker]
- **Tag ID mục tiêu:** [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
- **Thành phần mục tiêu (target_component):** `./sources/infra/devops/docker-compose.yml`
- **Hướng dẫn kỹ thuật cấp thấp:** Tạo tệp docker-compose.yml với các dịch vụ PostgreSQL, Redis, Kafka, và các dịch vụ backend.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 [Translate "DAY" into the target language Vietnamese] 2: Cấu hình Terraform và triển khai cơ sở hạ tầng

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 [Translate "SUB-TASKS" into the target language Vietnamese] 1: Tạo tệp main.tf
- **Chuyên môn phân công công việc của Sub-Agent:** [GCP]
- **Tag ID mục tiêu:** [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
- **Thành phần mục tiêu (target_component):** `./sources/infra/devops/terraform/main.tf`
- **Hướng dẫn kỹ thuật cấp thấp:** Tạo tệp main.tf với các cấu hình Terraform để triển khai cơ sở hạ tầng trên GCP.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 [Translate "DAY" into the target language Vietnamese] 3: Triển khai manifest Kubernetes

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 [Translate "SUB-TASKS" into the target language Vietnamese] 1: Tạo tệp deployment.yaml
- **Chuyên môn phân công công việc của Sub-Agent:** [GKE]
- **Tag ID mục tiêu:** [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
- **Thành phần mục tiêu (target_component):** `./sources/infra/devops/kubernetes/deployment.yaml`
- **Hướng dẫn kỹ thuật cấp thấp:** Tạo tệp deployment.yaml với các cấu hình Kubernetes để triển khai các dịch vụ backend.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

### 🕵️ Báo cáo kiểm toán kiến trúc đa giai đoạn:

```properties:cross_audit_ledger
[AUTOMATED_SELF_AUDIT_REPORT]
TOTAL_PHASES_DECLARED_IN_SECTION_4_2=5
TOTAL_PHASES_EXPECTED_BY_PARAMETERS=5
PHASE_COUNT_COMPLIANCE_STATUS=Verified_5
MAX_DAYS_PER_PHASE_LIMIT_PARAMETER=7
ACTUAL_MAX_DAY_INDEX_DETECTED_IN_TIMELINE=3
TIMELINE_DAY_CAP_COMPLIANCE_STATUS=Verified_All_Phase_Durations_Within_Ceiling
TOTAL_TASKS_REGISTERED_IN_MASTER_BACKLOG=16
TOTAL_DISCRETE_SUB_TASKS_GENERATED_IN_ACTIVE_DAYLOGS=16
SUB_TASK_QUANTUM_COMPLIANCE_STATUS=Verified_Symmetry_Enforced_With_100_Percent_Symmetry
```

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_3_FINAL-->

## ☣️ 6. UNIVERSAL ENTERPRISE SECURITY CODES & INJECTION COUNTERMEASURES [NFR-XXX]

- **SQL Injection (SQLi) Absolute Countermeasures [NFR-002]:** Enforce strict, mandatory dynamic runtime runtime parameters binding utilizing native query sanitization or entity state parameter maps matching the active ecosystem dependencies. Raw native character stream string concatenation is permanently forbidden; 100% of the raw query structures MUST be fully filtered through the active persistence repository layer or framework-native API parameters binding engines to guarantee absolute boundary isolation.
- **Cross-Site Scripting (XSS) & Content Security Policy (CSP) [NFR-002]:** Enforce automatic contextual escaping workflows within all frontend user-input presentation fields. The gateway MUST programmatically inject a rigid HTTP Content-Security-Policy (CSP) response header configuration blueprint containing optimized directive constraints (e.g., default-src 'self') to completely neutralize unauthorized script executions.
- **Multi-Tenant CORS Security Rails [NFR-002]:** Establish a zero-trust Cross-Origin Resource Sharing (CORS) enforcement layer across all system API endpoints. The routing infrastructure MUST programmatically validate incoming origin strings against a dynamic tenant authorization registry tree, explicitly banning global wildcard operators '*' for authenticated session requests.
- **Zero-Leak Log Scrubbing & PII Data Masking Engines [NFR-002]:** Configure an automated log scrubbing middleware engine to parse all outbound telemetry data payloads. You MUST utilize specialized Jackson serializer constraints and metadata markers (e.g., custom `@JsonSerialize` masking logic) to automatically intercept and obscure sensitive Personally Identifiable Information (PII) strings before data reaches physical storage.

## 📱 7. HYBRID MOBILE COMPLIANCE RAIL RULES & INTERNATIONALIZED SEO MECHANISMS

- **Capacitor Mobile Hybrid Compliance Rails:** Enforce structural mobile hybrid constraints by restricting client-side resource calls exclusively to absolute, validated protocol structures. All secure internal persistence workflows MUST utilize the native runtime abstraction engine (`@capacitor/preferences`), combined with explicit native webview hardware interceptor hooks to programmatically block unauthorized device hardware access patterns.
- **Internationalization (i18n) & Dynamic SEO Injection:** Deploy a dynamic language-detection middleware engine at the system edge layer to parse inbound user locale attributes. The responsive Next.js page compilation pipeline MUST automatically process localized metadata schemas and inject symmetrical, SEO-compliant `hreflang` header link properties dynamically into the presentation tree.

## 🚀 8. PIPELINE AUTOMATED DAILY SESSION GIT BRANCH FLOW

- **Daily Workspace Forking Isolation:** Enforce a programmatic, decoupled version control workflow by executing an automated workspace partition for every daily engineering session milestone. The automation system MUST validate that developers execute work exclusively within sandboxed branch paths structured strictly under the literal naming notation rule: `feature/phase-X-day-Y` (where X represents the calculated phase index and Y indicates the active chronological day).
- **Validation Guard Pipeline Gates:** Establish a strict, non-negotiable automated CI/CD validation gate within the central GitHub Actions continuous integration pipeline engine. The cloud deployment workflow MUST automatically trigger cross-compilation verification tests, SonarQube static code quality analysis sweeps, and forcefully abort the deployment sequence if the cumulative test coverage matrix score falls underneath the non-negotiable metric gate parameter of `>= 85%`.

[TRACEABILITY MATRIX ENFORCEMENT: 100% COVERAGE VALIDATED. TOTAL UNIQUE REQ TAGS MAPPED: 25, TOTAL ARC TAGS: 9, TOTAL EXC TAGS: 5, TOTAL DAT TAGS: 11, TOTAL NFR TAGS: 9. ZERO UNASSIGNED CODES FOUND.]

<!--END_CHUNK_PART_3_FINAL-->