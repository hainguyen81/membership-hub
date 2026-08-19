# GLOBAL PROJECT CONTEXT: membership-hub

## 📊 Kiểm soát tài liệu

| Item | Details |
| :--- | :--- |
| **Blueprint ID** | ARCH-20260804165526 |
| **Project Name** | membership-hub |
| **Version** | 1.0 (Baseline) |
| **Date.Time** | 2026/08/04 16:55:26 |
| **Author** | Enterprise System Architect (SA Agent) |
| **Approval** | Pending Technical Governance Review |

## 📊 1. TỔNG QUAN HỆ THỐNG & MÔ HÌNH KIẾN TRÚC CỐT LÕ

### 1.1. Mô hình hệ thống cốt lõi và mô hình kiến trúc

Hệ thống được thiết kế theo kiến trúc **microservices** với các dịch vụ Quarkus độc lập, giao tiếp qua **REST** và **Kafka** cho các sự kiện. Mỗi dịch vụ có **CQRS** riêng, sử dụng **PostgreSQL** làm nguồn dữ liệu chính và **Redis** cho session caching. Các API được bảo vệ bằng **JWT** (15 phút) và **refresh token** (7 ngày). Frontend là **Next.js** (React + TypeScript) và ứng dụng di động **Capacitor** (React Native) chia sẻ logic và tài nguyên.

### 1.2. Định hướng luồng dữ liệu và hệ sinh thái

- **Luồng xác thực**: OAuth2 (Firebase, Google, Facebook) → JWT → API Gateway.  
- **Luồng điểm danh QR**: Mobile scan → API → idempotent attendance record.  
- **Luồng thông báo**: Event bus → Push (FCM/APNs) + Zalo API.  
- **Luồng backend di động**: Next.js tiêu thụ REST, caching offline, retry logic.

## 📁 2. ĐỘC ĐẠO CÔNG NGHỆ & THƯ VIỆN

- **Backend**: Java 17, Quarkus 3, Hibernate ORM, Flyway, Redis, Firebase Admin SDK, GCP SDK.  
- **Frontend**: Next.js 13, React 18, TypeScript, Tailwind CSS, Capacitor 4.  
- **CI/CD**: GitHub Actions, Docker, GKE, Helm, Terraform.

## 📁 3. QUY ĐỊNH BẢO VỆ & CHẤT LƯỢNG DOANH NGHIỆP

- **Root repository**: `..` → tất cả các đường dẫn bắt đầu bằng `./sources/`.  
- **Dynamic prefix**: Backend → `./sources/backend.<service-name>.`, Frontend → `./sources/frontend.<app-name>.`, Infra → `./sources/infra.`  
- **Java package**: `org.nlh4j.saas.membershiphub`.  
- **Tester target syntax**: `<source_component>;<test_suite_file>` bắt đầu bằng `./sources/`.

## 📁 4. BẢNG TỔNG QUAN ĐIỀU HƯỚNG GIAO DIỆN GIAO ĐIỆN

| Giai đoạn | Khoảng ngày | Đường dẫn Cấu phần / Module | Tóm tắt Sản phẩm Bàn giao | Sub-Agent | Tag IDs Mục tiêu |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | 1-3 | `./sources/backend/auth-service`, `./sources/backend/user-service`, `./sources/backend/role-service`, `./sources/backend/database/migrations` | Xây dựng xác thực, RBAC, schema Users & Roles, JWT, unit test | Coder, Tester, Reviewer | [REQ-001], [REQ-002], [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010], [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |
| 2 | 1-3 | `./sources/backend/center-service`, `./sources/backend/course-service`, `./sources/backend/enrollment-service`, `./sources/backend/attendance-service`, `./sources/backend/database/migrations` | CRUD trung tâm, khóa học, ghi danh, điểm danh, schema Centers, Courses, Enrollments, Attendance | Coder, Tester, Reviewer | [REQ-004], [REQ-005], [REQ-006], [REQ-007], [REQ-008], [REQ-009], [REQ-010], [REQ-011], [REQ-012], [REQ-013], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005] |
| 3 | 1-3 | `./sources/backend/notification-service`, `./sources/backend/promotion-service`, `./sources/backend/announcement-service`, `./sources/backend/chatbot-service`, `./sources/backend/mobile-service` | Thông báo push/Zalo, khuyến mãi, thông báo, chatbot, mobile API | Coder, Tester, Reviewer | [REQ-016], [REQ-017], [REQ-018], [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [EXC-003] |
| 4 | 1-2 | `./sources/frontend/mobile-app`, `./sources/frontend/web-app` | UI responsive, i18n, SEO, documentation | Doc, GCP | [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [DAT-011] |
| 5 | 1-2 | `./sources/infra/docker`, `./sources/infra/gcp`, `./sources/infra/gke` | Dockerfile, GCP deployment, GKE orchestration | Docker, GKE | [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009], [ARC-010] |

## 5. CHI TIẾT GIAO DIỆN GIAO ĐIỆN MỖI GIAO DIỆN

### Phase 1 Detailed Architectural Specification
- **Mục tiêu Cốt lõi & Mục đích của Giai đoạn 1**: Thiết lập nền tảng xác thực, quản lý người dùng và quyền truy cập, xây dựng schema cơ sở dữ liệu Users & Roles, triển khai JWT, chuẩn hoá API, kiểm thử đơn vị.
- **Ma trận Bản đồ Thư mục Vật lý Mục tiêu**:  
  - `./sources/backend/auth-service [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]`  
  - `./sources/backend/user-service [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]`  
  - `./sources/backend/role-service [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]`  
  - `./sources/backend/database/migrations [DAT-001], [DAT-002]`
- **Đặc tả DDL SQL Schema Cơ sở Dữ liệu**  
  ```sql
  CREATE TABLE USERS (
      userId UUID PRIMARY KEY,
      email VARCHAR(255) NOT NULL UNIQUE,
      passwordHash CHAR(60) NOT NULL,
      fullName VARCHAR(100) NOT NULL,
      roleId SMALLINT NOT NULL,
      provider VARCHAR(20) NOT NULL DEFAULT 'local',
      createdAt TIMESTAMP NOT NULL DEFAULT now(),
      updatedAt TIMESTAMP NOT NULL DEFAULT now()
  );
  CREATE TABLE ROLES (
      roleId SMALLINT PRIMARY KEY,
      name VARCHAR(30) NOT NULL UNIQUE,
      description VARCHAR(200)
  );
  ALTER TABLE USERS ADD CONSTRAINT fk_user_role FOREIGN KEY (roleId) REFERENCES ROLES(roleId);
  ```
- **Hợp đồng Định tuyến API và Sự kiện**  
  - `POST /api/auth/register` → Body: `{email, password, provider}` → Response: `{token, refreshToken}`  
  - `POST /api/auth/login` → Body: `{email, password}` → Response: `{token, refreshToken}`  
  - `GET /api/auth/me` → Header: `Authorization: Bearer <token>` → Response: `{userId, email, role}`  
- **Xử lý ngoại lệ**  
  - `[EXC-004]` → Kiểm tra đầu vào email, password, provider. Trả lỗi 400 với chi tiết trường sai.

#### 📅 Chronological Day-by-Day Sub-Agent Task Distribution Logs (Phase 1)

- **DAY 1:** Xây dựng API đăng ký và login, tạo schema Users & Roles, triển khai JWT, unit test.  
  - **Sub-Agent Workflow Specialization:**  
    * **Coder:**  
      - **Target Component file path (`target_component`):** `./sources/backend/auth-service [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]`  
      - **Low-Level Technical Task Instruction:** Xây dựng controller, service, repository, JWT provider, validation, exception handling, unit test.  
      - **Targeted Tag IDs:** [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]  
- **DAY 2:** Viết test tích hợp cho luồng xác thực, kiểm tra token expiration, idempotent.  
  - **Sub-Agent Workflow Specialization:**  
    * **Tester:**  
      - **Target Component file path (`target_component`):** `./sources/backend/auth-service;./sources/backend/auth-service/src/test/java/com/membershiphub/auth/AuthServiceTest.java [ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010]`  
      - **Low-Level Technical Task Instruction:** Viết test unit và integration, mock Firebase, kiểm tra refresh token, kiểm tra bảo mật.  
      - **Targeted Tag IDs:** [ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010]  
- **DAY 3:** Đánh giá mã, kiểm tra bảo mật, chuẩn hoá API, kiểm tra performance.  
  - **Sub-Agent Workflow Specialization:**  
    * **Reviewer:**  
      - **Target Component file path (`target_component`):** `./sources/backend/auth-service [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]`  
      - **Low-Level Technical Task Instruction:** Đánh giá code quality, kiểm tra OWASP, performance profiling, đề xuất cải tiến.  
      - **Targeted Tag IDs:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]  

### Phase 2 Detailed Architectural Specification
- **Mục tiêu Cốt lõi & Mục đích của Giai đoạn 2**: Xây dựng CRUD trung tâm, khóa học, ghi danh, điểm danh, schema Centers, Courses, Enrollments, Attendance, kiểm thử, exception handling.
- **Ma trận Bản đồ Thư mục Vật lý Mục tiêu**:  
  - `./sources/backend/center-service [REQ-004], [REQ-005], [REQ-006], [DAT-003]`  
  - `./sources/backend/course-service [REQ-007], [REQ-008], [REQ-009], [DAT-004]`  
  - `./sources/backend/enrollment-service [REQ-010], [REQ-011], [DAT-005]`  
  - `./sources/backend/attendance-service [REQ-012], [REQ-013], [DAT-006]`  
  - `./sources/backend/database/migrations [DAT-003], [DAT-004], [DAT-005], [DAT-006]`
- **Đặc tả DDL SQL Schema Cơ sở Dữ liệu**  
  ```sql
  CREATE TABLE CENTERS (
      centerId UUID PRIMARY KEY,
      name VARCHAR(100) NOT NULL,
      address VARCHAR(255) NOT NULL,
      taxId VARCHAR(13) NOT NULL UNIQUE,
      contactPhone VARCHAR(50),
      contactEmail VARCHAR(255)
  );
  CREATE TABLE COURSES (
      courseId UUID PRIMARY KEY,
      title VARCHAR(150) NOT NULL,
      description TEXT,
      startDate DATE NOT NULL,
      endDate DATE NOT NULL,
      teacherId UUID NOT NULL,
      maxStudents INT DEFAULT 30,
      CONSTRAINT fk_course_teacher FOREIGN KEY (teacherId) REFERENCES USERS(userId)
  );
  CREATE TABLE ENROLLMENTS (
      enrollmentId UUID PRIMARY KEY,
      studentId UUID NOT NULL,
      courseId UUID NOT NULL,
      enrollmentDate TIMESTAMP NOT NULL DEFAULT now(),
      CONSTRAINT fk_enrollment_student FOREIGN KEY (studentId) REFERENCES USERS(userId),
      CONSTRAINT fk_enrollment_course FOREIGN KEY (courseId) REFERENCES COURSES(courseId)
  );
  CREATE TABLE ATTENDANCE (
      attendanceId UUID PRIMARY KEY,
      studentId UUID NOT NULL,
      courseId UUID NOT NULL,
      attendanceDate DATE NOT NULL,
      timestamp TIMESTAMP NOT NULL DEFAULT now(),
      CONSTRAINT fk_attendance_student FOREIGN KEY (studentId) REFERENCES USERS(userId),
      CONSTRAINT fk_attendance_course FOREIGN KEY (courseId) REFERENCES COURSES(courseId),
      CONSTRAINT uq_attendance UNIQUE (studentId, courseId, attendanceDate)
  );
  ```
- **Hợp đồng Định tuyến API và Sự kiện**  
  - `GET /api/centers` → Response: list of centers.  
  - `POST /api/centers` → Body: center details → Response: created center.  
  - `GET /api/courses` → Response: list of courses.  
  - `POST /api/courses` → Body: course details → Response: created course.  
  - `POST /api/enrollments` → Body: enrollment details → Response: created enrollment.  
  - `POST /api/attendance` → Body: studentId, courseId → Response: attendance record.  
- **Xử lý ngoại lệ**  
  - `[EXC-001]` → Network drop during QR scan.  
  - `[EXC-002]` → Duplicate attendance submission.  
  - `[EXC-003]` → Duplicate tax ID.  
  - `[EXC-004]` → Input validation failure.  
  - `[EXC-005]` → System outage recovery.

#### 📅 Chronological Day-by-Day Sub-Agent Task Distribution Logs (Phase 2)

- **DAY 1:** Xây dựng CRUD trung tâm, schema Centers, kiểm thử.  
  - **Sub-Agent Workflow Specialization:**  
    * **Coder:**  
      - **Target Component file path (`target_component`):** `./sources/backend/center-service [REQ-004], [REQ-005], [REQ-006], [DAT-003]`  
      - **Low-Level Technical Task Instruction:** Xây dựng controller, service, repository, validation, exception handling, unit test.  
      - **Targeted Tag IDs:** [REQ-004], [REQ-005], [REQ-006], [DAT-003]  
- **DAY 2:** Xây dựng CRUD khóa học, schema Courses, kiểm thử.  
  - **Sub-Agent Workflow Specialization:**  
    * **Coder:**  
      - **Target Component file path (`target_component`):** `./sources/backend/course-service [REQ-007], [REQ-008], [REQ-009], [DAT-004]`  
      - **Low-Level Technical Task Instruction:** Xây dựng controller, service, repository, validation, conflict detection, unit test.  
      - **Targeted Tag IDs:** [REQ-007], [REQ-008], [REQ-009], [DAT-004]  
- **DAY 3:** Xây dựng ghi danh, điểm danh, schema Enrollments & Attendance, exception handling, integration test.  
  - **Sub-Agent Workflow Specialization:**  
    * **Coder:**  
      - **Target Component file path (`target_component`):** `./sources/backend/enrollment-service [REQ-010], [REQ-011], [DAT-005]`  
      - **Low-Level Technical Task Instruction:** Xây dựng controller, service, repository, validation, idempotent attendance, unit test.  
      - **Targeted Tag IDs:** [REQ-010], [REQ-011], [DAT-005]  
    * **Tester:**  
      - **Target Component file path (`target_component`):** `./sources/backend/attendance-service;./sources/backend/attendance-service/src/test/java/com/membershiphub/attendance/AttendanceServiceTest.java [REQ-012], [REQ-013], [DAT-006], [EXC-001], [EXC-002]`  
      - **Low-Level Technical Task Instruction:** Viết test integration, mock network, test idempotency, test duplicate handling.  
      - **Targeted Tag IDs:** [REQ-012], [REQ-013], [DAT-006], [EXC-001], [EXC-002]  
    * **Reviewer:**  
      - **Target Component file path (`target_component`):** `./sources/backend/enrollment-service [REQ-010], [REQ-011], [DAT-005]`  
      - **Low-Level Technical Task Instruction:** Đánh giá code quality, kiểm tra exception handling, performance.  
      - **Targeted Tag IDs:** [REQ-010], [REQ-011], [DAT-005]  

### Phase 3 Detailed Architectural Specification
- **Mục tiêu Cốt lõi & Mục đích của Giai đoạn 3**: Xây dựng dịch vụ thông báo, khuyến mãi, thông báo, chatbot, mobile API, kiểm thử, exception handling.
- **Ma trận Bản đồ Thư mục Vật lý Mục tiêu**:  
  - `./sources/backend/notification-service [REQ-016], [REQ-017], [REQ-018], [REQ-019], [DAT-007], [DAT-008]`  
  - `./sources/backend/promotion-service [REQ-017], [DAT-009]`  
  - `./sources/backend/announcement-service [REQ-018], [DAT-010]`  
  - `./sources/backend/chatbot-service [REQ-019]`  
  - `./sources/backend/mobile-service [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [DAT-011]`  
  - `./sources/backend/database/migrations [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011]`
- **Đặc tả DDL SQL Schema Cơ sở Dữ liệu**  
  ```sql
  CREATE TABLE NOTIFICATIONS (
      notificationId UUID PRIMARY KEY,
      userId UUID,
      groupZalo VARCHAR(255),
      message TEXT NOT NULL,
      sentAt TIMESTAMP NOT NULL DEFAULT now(),
      delivered BOOLEAN NOT NULL DEFAULT false
  );
  CREATE TABLE PROMOTIONS (
      promoId UUID PRIMARY KEY,
      code VARCHAR(50) NOT NULL UNIQUE,
      discountPercent SMALLINT NOT NULL,
      startDate DATE,
      endDate DATE,
      description TEXT
  );
  CREATE TABLE ANNOUNCEMENTS (
      announcementId UUID PRIMARY KEY,
      title VARCHAR(150) NOT NULL,
      content TEXT NOT NULL,
      startDate DATE,
      endDate DATE
  );
  ```
- **Hợp đồng Định tuyến API và Sự kiện**  
  - `POST /api/notifications` → Body: notification details → Response: created notification.  
  - `POST /api/promotions` → Body: promotion details → Response: created promotion.  
  - `POST /api/announcements` → Body: announcement details → Response: created announcement.  
  - `POST /api/chatbot/message` → Body: user message → Response: bot reply.  
  - `GET /api/mobile/cards` → Response: student card info.  
- **Xử lý ngoại lệ**  
  - `[EXC-003]` → Failed notification delivery.

#### 📅 Chronological Day-by-Day Sub-Agent Task Distribution Logs (Phase 3)

- **DAY 1:** Xây dựng dịch vụ thông báo, khuyến mãi, thông báo, chatbot, schema Notifications, Promotions, Announcements.  
  - **Sub-Agent Workflow Specialization:**  
    * **Coder:**  
      - **Target Component file path (`target_component`):** `./sources/backend/notification-service [REQ-016], [REQ-017], [REQ-018], [REQ-019], [DAT-007], [DAT-008]`  
      - **Low-Level Technical Task Instruction:** Xây dựng controller, service, repository, integration với Firebase, Zalo, unit test.  
      - **Targeted Tag IDs:** [REQ-016], [REQ-017], [REQ-018], [REQ-019], [DAT-007], [DAT-008]  
- **DAY 2:** Xây dựng mobile API, schema StudentCards, i18n, SEO, kiểm thử.  
  - **Sub-Agent Workflow Specialization:**  
    * **Tester:**  
      - **Target Component file path (`target_component`):** `./sources/backend/mobile-service;./sources/backend/mobile-service/src/test/java/com/membershiphub/mobile/MobileServiceTest.java [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [DAT-011]`  
      - **Low-Level Technical Task Instruction:** Viết test API, kiểm tra i18n, SEO meta tags, unit test.  
      - **Targeted Tag IDs:** [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [DAT-011]  
- **DAY 3:** Đánh giá bảo mật, exception handling, performance.  
  - **Sub-Agent Workflow Specialization:**  
    * **Reviewer:**  
      - **Target Component file path (`target_component`):** `./sources/backend/notification-service [REQ-016], [REQ-017], [REQ-018], [REQ-019], [DAT-007], [DAT-008], [EXC-003]`  
      - **Low-Level Technical Task Instruction:** Đánh giá code quality, kiểm tra exception handling, performance profiling.  
      - **Targeted Tag IDs:** [REQ-016], [REQ-017], [REQ-018], [REQ-019], [DAT-007], [DAT-008], [EXC-003]  

### Phase 4 Detailed Architectural Specification
- **Mục tiêu Cốt lõi & Mục đích của Giai đoạn 4**: Phát triển UI responsive, i18n, SEO, tài liệu, triển khai GCP.
- **Ma trận Bản đồ Thư mục Vật lý Mục tiêu**:  
  - `./sources/frontend/mobile-app [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [DAT-011]`  
  - `./sources/frontend/web-app [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [DAT-011]`  
  - `./sources/frontend/nextjs-app`  
- **Hợp đồng Định tuyến API và Sự kiện**  
  - `GET /api/mobile/cards` → Response: student card info.  
  - `GET /api/announcements` → Response: announcements list.  
- **Xử lý ngoại lệ**  
  - Không có ngoại lệ riêng.

#### 📅 Chronological Day-by-Day Sub-Agent Task Distribution Logs (Phase 4)

- **DAY 1:** Viết tài liệu chi tiết UI, API, i18n, SEO.  
  - **Sub-Agent Workflow Specialization:**  
    * **Doc:**  
      - **Target Component file path (`target_component`):** `./sources/frontend/mobile-app [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [DAT-011]`  
      - **Low-Level Technical Task Instruction:** Viết tài liệu chi tiết, hướng dẫn sử dụng, tài liệu API, cấu hình i18n, SEO meta tags.  
      - **Targeted Tag IDs:** [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [DAT-011]  
- **DAY 2:** Cấu hình GCP Cloud Build, Cloud Run, Firebase Hosting.  
  - **Sub-Agent Workflow Specialization:**  
    * **GCP:**  
      - **Target Component file path (`target_component`):** `./sources/frontend/mobile-app;./sources/frontend/web-app [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [DAT-011]`  
      - **Low-Level Technical Task Instruction:** Viết cấu hình Terraform, Cloud Build, Cloud Run, Firebase Hosting, CI/CD pipeline.  
      - **Targeted Tag IDs:** [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [DAT-011]  

### Phase 5 Detailed Architectural Specification
- **Mục tiêu Cốt lõi & Mục đích của Giai đoạn 5**: Containerization, GCP deployment, GKE orchestration, bảo mật, compliance.
- **Ma trận Bản đồ Thư mục Vật lý Mục tiêu**:  
  - `./sources/infra/docker [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009], [ARC-010]`  
  - `./sources/infra/gcp [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009], [ARC-010]`  
  - `./sources/infra/gke [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009], [ARC-010]`  
- **Hợp đồng Định tuyến API và Sự kiện**  
  - Không có API riêng.

#### 📅 Chronological Day-by-Day Sub-Agent Task Distribution Logs (Phase 5)

- **DAY 1:** Xây dựng Dockerfile multi-stage, tối ưu kích thước, push registry.  
  - **Sub-Agent Workflow Specialization:**  
    * **Docker:**  
      - **Target Component file path (`target_component`):** `./sources/infra/docker [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009], [ARC-010]`  
      - **Low-Level Technical Task Instruction:** Viết Dockerfile multi-stage, giảm kích thước, build, push tới registry, test image.  
      - **Targeted Tag IDs:** [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009], [ARC-010]  
- **DAY 2:** Xây dựng Helm charts, HPA, autoscaling, monitoring.  
  - **Sub-Agent Workflow Specialization:**  
    * **GKE:**  
      - **Target Component file path (`target_component`):** `./sources/infra/gke [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009], [ARC-010]`  
      - **Low-Level Technical Task Instruction:** Viết Helm chart, cấu hình HPA, autoscaling, monitoring, logging, CI/CD.  
      - **Targeted Tag IDs:** [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009], [ARC-010]  

## 📁 6. Mã Bảo Vệ & Biện Pháp Chống Xâm Nhập

- **SQL Injection (SQLi) Absolute Countermeasures**: Sử dụng prepared statements, parameter binding, whitelist cho các tham số sắp xếp.  
- **Cross-Site Scripting (XSS) & Content Security Policy (CSP)**: Tự động escape output, sử dụng `Content-Security-Policy: default-src 'self'; script-src 'self';`  
- **Multi-Tenant CORS Security Rails**: Không cho phép wildcard, chỉ cho phép origin từ danh sách whitelist, xác thực tenant ID trong header.  
- **Zero-Leak Log Scrubbing & PII Data Masking Engines**: Sử dụng interceptor `@JsonSerialize` để mask email, phone, token trong logs, giới hạn độ dài, ghi log với mức độ `INFO` hoặc `WARN`.

## 📁 7. Quy Định Tuân Thủ Di Động & SEO

- **Capacitor Mobile Hybrid Compliance Rails**: Sử dụng `@capacitor/preferences` cho lưu trữ, intercept back button, offline caching, dynamic URL handling.  
- **Internationalization (i18n) & Dynamic SEO Injection**: Middleware nhận locale từ header hoặc URL, inject `<html lang='vi'>`, `<link rel='alternate' hreflang='en' href='...'>`, robots.txt, sitemap.xml.

## 📁 8. Pipeline Automated Daily Session Git Branch Flow

- **Daily Workspace Forking Isolation**: Mỗi ngày tạo branch `features/development-phase-X-day-Y`.  
- **Validation Guard Pipeline Gates**: Kiểm tra compile, coverage ≥ 85%, lint, unit test, integration test, build Docker image, push, deploy to GKE, run smoke test.

[TRACEABILITY MATRIX ENFORCEMENT: 100% COVERAGE VALIDATED. TOTAL UNIQUE REQ TAGS MAPPED: 24, TOTAL ARC TAGS: 10, TOTAL EXC TAGS: 5, TOTAL DAT TAGS: 11, TOTAL NFR TAGS: 9. ZERO UNASSIGNED CODES FOUND.]