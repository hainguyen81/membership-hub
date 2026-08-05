# Giai đoạn 1: <!--PHASE_NAME_START-->phase_1<!--PHASE_NAME_END--> | Mô tả: Thiết lập cơ sở hạ tầng backend và frontend, triển khai cơ sở dữ liệu PostgreSQL, tích hợp Firebase Authentication
## 📊 Document Control

| Mục | Chi tiết |
| :--- | :--- |
| **ID Blueprint** | ARCH-20260804052551 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 1 |
| **Tên kỹ thuật giai đoạn** | <!--PHASE_NAME_START-->phase_1<!--PHASE_NAME_END--> |
| **Mô tả** | Thiết lập cơ sở hạ tầng backend và frontend, triển khai cơ sở dữ liệu PostgreSQL, tích hợp Firebase Authentication |
| **Phiên bản** | 1.0 (Baseline) |
| **Ngày/Giờ** | 2026/08/04 05:25:51 |
| **Tác giả** | Enterprise System Architect (SA Agent) |
| **Phê duyệt** | Pending Technical Governance Review |

## 1. Phạm vi hoạt động và mục tiêu của giai đoạn
Giai đoạn 1 tập trung vào việc thiết lập cơ sở hạ tầng backend và frontend, triển khai cơ sở dữ liệu PostgreSQL, và tích hợp Firebase Authentication. Các nhiệm vụ chính bao gồm:
- Thiết lập cấu hình cơ sở dữ liệu PostgreSQL
- Tích hợp Firebase Authentication
- Thiết lập cấu hình Docker và Kubernetes (GKE)
- Viết Dockerfile cho dịch vụ backend
- Triển khai cơ sở hạ tầng trên Google Cloud Platform

## 2. Phạm vi kỹ thuật và ranh giới thư mục được phép
- `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/config`
- `./sources/backend/src/main/resources/db/migration`
- `./sources/backend/Dockerfile`
- `./sources/infra/gcp`

## 3. Hướng dẫn chức năng chuyên dụng cho các tác vụ con
- **Coder:** Triển khai cấu hình cơ sở dữ liệu PostgreSQL, tích hợp Firebase Authentication, thiết lập cấu hình Docker và Kubernetes (GKE), viết Dockerfile cho dịch vụ backend, triển khai cơ sở hạ tầng trên Google Cloud Platform.
- **Docker:** Viết Dockerfile cho dịch vụ backend, cấu hình multi-stage build để giảm kích thước image.
- **GCP:** Triển khai cơ sở hạ tầng trên Google Cloud Platform, cấu hình VPC, IAM, và các dịch vụ cần thiết.

## 4. Định nghĩa hoàn thành giai đoạn (DoD)
- Hoàn thành 100% các nhiệm vụ được chỉ định cho giai đoạn 1.
- Đảm bảo tuân thủ các tiêu chuẩn bảo mật OWASP.
- Đảm bảo hoàn thành các bài kiểm tra chức năng cho các yêu cầu được phân bổ.
- Đảm bảo 100% các Tag ID được ánh xạ.

## 5. NHẬT KÝ THỰC HIỆN KIẾN TRÚC THEO NGÀY

### NGÀY 1: Thiết lập cơ sở hạ tầng backend và frontend

#### NHIỆM VỤ CON 1.1: Thiết lập cấu hình cơ sở dữ liệu PostgreSQL, tích hợp Firebase Authentication, thiết lập cấu hình Docker và Kubernetes (GKE)
##### Người phụ trách: Coder
##### Yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/config`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006], [ARC-010]<!--END_TAGS-->

#### NHIỆM VỤ CON 1.2: Viết Dockerfile cho dịch vụ backend, cấu hình multi-stage build để giảm kích thước image
##### Người phụ trách: Docker
##### Yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/Dockerfile`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-010]<!--END_TAGS-->

#### NHIỆM VỤ CON 1.3: Triển khai cơ sở hạ tầng trên Google Cloud Platform, cấu hình VPC, IAM, và các dịch vụ cần thiết
##### Người phụ trách: GCP
##### Yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/infra/gcp`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-010]<!--END_TAGS-->

### NGÀY 2: Triển khai cơ sở dữ liệu PostgreSQL

#### NHIỆM VỤ CON 2.1: Viết các script Flyway/Liquibase để tạo các bảng cơ sở dữ liệu, thiết lập các ràng buộc và chỉ mục
##### Người phụ trách: Coder
##### Yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/src/main/resources/db/migration`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-001], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-011]<!--END_TAGS-->

### NGÀY 3: Tích hợp Firebase Authentication

#### NHIỆM VỤ CON 3.1: Triển khai các dịch vụ xác thực qua email/mật khẩu, Firebase, Google, và Facebook OAuth2, cấu hình JWT token với thời hạn 15 phút và refresh token
##### Người phụ trách: Coder
##### Yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/auth`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006]<!--END_TAGS-->