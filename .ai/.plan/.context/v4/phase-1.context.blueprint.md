# Phase 1: Thiết lập Xác thực và Quản lý Người dùng

## 📊 Document Control

| Item | Details |
| :--- | :--- |
| **Blueprint ID** | ARCH-20260804165526 |
| **Project Name** | membership-hub |
| **Phase** | 1 |
| **Phase Name** | Thiết lập Xác thực và Quản lý Người dùng |
| **Description** | Giai đoạn này tập trung vào việc xây dựng nền tảng xác thực, quản lý người dùng và quyền truy cập, triển khai JWT, chuẩn hoá API, và thực hiện kiểm thử đơn vị. |
| **Version** | 1.0 (Baseline) |
| **Date/Time** | 2026/08/04 16:55:26 |
| **Author** | Enterprise System Architect (SA Agent) |
| **Approval** | Pending Technical Governance Review |

## 1. Phase Operational Scope & Objectives
Giai đoạn 1 thực hiện các chức năng chính: đăng ký, đăng nhập, lấy thông tin người dùng, quản lý vai trò, tạo và duy trì schema Users & Roles, phát hành JWT, và thực hiện kiểm thử đơn vị. Mọi hoạt động phải tuân thủ các yêu cầu bảo mật OWASP, hiệu năng, và khả năng mở rộng theo NFR.

## 2. Allowed Technical Scope & Directory Boundaries (Files, paths, and endpoints)
- **Directories**  
  - `./sources/backend/auth-service`  
  - `./sources/backend/user-service`  
  - `./sources/backend/role-service`  
  - `./sources/backend/database/migrations`  
- **REST Endpoints**  
  - `POST /api/auth/register` → Body: `{email, password, provider}` → Response: `{token, refreshToken}`  
  - `POST /api/auth/login` → Body: `{email, password}` → Response: `{token, refreshToken}`  
  - `GET /api/auth/me` → Header: `Authorization: Bearer <token>` → Response: `{userId, email, role}`  

## 3. Dedicated Sub-Agent Functional Directives
- **Coder**: Xây dựng controller, service, repository, JWT provider, validation, exception handling, và unit test cho dịch vụ auth, user, role.  
- **Tester**: Viết và thực thi các test unit và integration, mock Firebase, kiểm tra token expiration, idempotent, và bảo mật.  
- **Reviewer**: Đánh giá code quality, kiểm tra OWASP, profiling hiệu năng, đề xuất cải tiến.  
- **Doc**: Tài liệu chi tiết API, quy trình triển khai, và hướng dẫn bảo mật.  

## 4. Phase Definition of Done (DoD)
- Tất cả yêu cầu [REQ-001]–[REQ-005] và [ARC-001]–[ARC-005] được triển khai và kiểm thử.  
- Coverage test ≥ 90 % cho các module auth, user, role.  
- Kiểm tra OWASP (SQLi, XSS, CSRF, JWT) đạt mức 100 %.  
- Tất cả tag ID được map đầy đủ, không còn tag chưa được sử dụng.  
- Đã thực hiện review code, performance profiling, và tối ưu.  

## 5. DAY-BY-DAY ARCHITECTURAL EXECUTION LOGS

### DAY 1: XÂY ĐẾN API ĐĂNG KÝ VÀ XÁC THỰC

#### SUB-TASK 1.1: Xây dựng API đăng ký, đăng nhập và schema Users & Roles
##### Assigned Sub-Agent: Coder
##### Targeted Components & Technical Requirements:
* **Target Path**: `./sources/backend/auth-service`
* **Traceability Tag Tokens**: <!--START_TAGS-->[REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]<!--END_TAGS-->

### DAY 2: THỰC HIỆN KIỂM THỬ TÍNH NĂNG XÁC THỰC

#### SUB-TASK 2.1: Viết test integration, kiểm tra token expiration và idempotent
##### Assigned Sub-Agent: Tester
##### Targeted Components & Technical Requirements:
* **Target Path**: `./sources/backend/auth-service;./sources/backend/auth-service/src/test/java/com/membershiphub/auth/AuthServiceTest.java`
* **Traceability Tag Tokens**: <!--START_TAGS-->[ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010], [EXC-004], [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]<!--END_TAGS-->

### DAY 3: ĐÁNH GIÁ BẢO MẬT VÀ TIỆN ĐIỆN

#### SUB-TASK 3.1: Đánh giá bảo mật, chuẩn hoá API và kiểm tra hiệu năng
##### Assigned Sub-Agent: Reviewer
##### Targeted Components & Technical Requirements:
* **Target Path**: `./sources/backend/auth-service`
* **Traceability Tag Tokens**: <!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]<!--END_TAGS-->