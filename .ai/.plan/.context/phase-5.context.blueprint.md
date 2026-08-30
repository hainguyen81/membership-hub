# Giai đoạn 5: <!--PHASE_NAME_START-->Tích Hợp Giao Diện Responsive, Báo Cáo Dashboard, Hạ Tầng DevOps và Tài Liệu Doanh Nghiệp<!--PHASE_NAME_END-->

## 📊 Kiểm Soát Tài Liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Bản Thiết Kế** | ARCH-20260829225017 |
| **Tên Dự Án** | membership-hub |
| **Giai đoạn** | 5 |
| **Tên Giai Đoạn** | <!--PHASE_NAME_START-->Tích Hợp Giao Diện Responsive, Báo Cáo Dashboard, Hạ Tầng DevOps và Tài Liệu Doanh Nghiệp<!--PHASE_NAME_END--> |
| **Mô Tả** | <!--PHASE_DESC_START-->Giai đoạn 5 hoàn thiện tầng giao diện responsive đa vai trò trên Next.js kết hợp NativeWind, tích hợp phát hiện ngôn ngữ thông qua next-intl, hỗ trợ SEO đa ngôn ngữ với hreflang, triển khai microservice báo cáo điểm danh CSV với giới hạn 30 ngày, dashboard tổng hợp tuyển sinh thời gian thực với cache 15 phút, đóng gói toàn bộ image Docker đa giai đoạn dưới 500MB, cung cấp hạ tầng GCP thông qua Terraform (VPC, GKE Autopilot, Cloud SQL PostgreSQL, IAM, KMS), triển khai manifest GKE với HPA theo CPU/latency, và hoàn thiện bộ tài liệu kiến trúc doanh nghiệp (C4, OpenAPI, GDPR/CCPA, SEO/i18n) đặt tại ./sources/docs/<!--PHASE_DESC_END--> |
| **Phiên Bản** | 1.0 (Đường cơ sở) |
| **Ngày Giờ** | 2026/08/29 22:50:17 |
| **Tác Giả** | Kiến Trúc Sư Hệ Thống Doanh Nghiệp (SA Agent) |
| **Phê Duyệt** | Đang chờ Rà Soát Quản Trị Kỹ Thuật |

## 1. Phạm Vi Hoạt Động Và Mục Tiêu Giai Đoạn

Giai đoạn 5 đóng vai trò giai đoạn cuối cùng và quan trọng bậc nhất trong hệ thống membership-hub, tập trung hoàn thiện toàn bộ tầng giao diện người dùng, hạ tầng DevOps sẵn sàng production và bộ tài liệu kiến trúc doanh nghiệp. Phạm vi kỹ thuật cốt lõi bao gồm 7 nhiệm vụ backlog chính được phân bổ: Nhiệm vụ 20 (Giao diện responsive cho ứng dụng di động Next.js), Nhiệm vụ 22 (Phát hiện ngôn ngữ mặc định cho khách truy cập), Nhiệm vụ 23 (SEO đa ngôn ngữ en/vi/es), Nhiệm vụ 24 (Xuất báo cáo điểm danh CSV), Nhiệm vụ 25 (Dashboard tổng hợp tuyển sinh thời gian thực), Nhiệm vụ 31 (Hạ tầng DevOps Docker + Terraform GCP + GKE Manifest), và Nhiệm vụ 32 (Tài liệu kiến trúc và vận hành doanh nghiệp). Theo kế hoạch phân bổ trong bảng tổng hợp đa giai đoạn 4.2 của bối cảnh dự án toàn cục, giai đoạn 5 được phân bổ chính xác khoảng ngày "Ngày 1 - 5" nghĩa là Relative_Z = 5 ngày làm việc liên tục.

Trên tầng frontend Next.js tại `./sources/frontend/web-app/`, giai đoạn này xây dựng layout gốc với App Router, component `RoleNavMenu` điều hướng theo 5 vai trò (Student, Teacher, Manager, Center Admin, System Admin) thông qua prop `session.role`. Tích hợp `next-intl@3.17.2` cho đa ngôn ngữ với middleware phát hiện locale từ cookie `NEXT_LOCALE` và fallback `Accept-Language` header. Hỗ trợ SEO với thẻ `<html lang>`, thẻ `hreflang` cho 3 ngôn ngữ (en, vi, es) và sitemap.xml đa ngôn ngữ. Styling responsive sử dụng TailwindCSS 3.4.10 kết hợp NativeWind 4.1.23 cho màn hình di động.

Trên tầng backend microservices, giai đoạn này triển khai 2 microservice mới: `report-service` với REST endpoint `GET /api/v1/reports/attendance` xuất CSV giới hạn khoảng ngày tối đa 30 ngày, các cột StudentName, CourseName, AttendanceDate, Status, phát sinh `InvalidDateRangeException` nếu vượt quá giới hạn, hỗ trợ streaming CSV để tránh OOM với dữ liệu lớn. Microservice `dashboard-service` với REST endpoint `GET /api/v1/dashboard/enrollment-summary` cache 15 phút thông qua Redis, trả về cards `totalStudents`, `activeCourses`, `upcomingSessions` cho dashboard Center Admin. View materialized `mv_enrollment_summary` tổng hợp dữ liệu từ 3 bảng centers, courses, enrollments.

Trên tầng hạ tầng DevOps tại `./sources/infra/`, giai đoạn này sinh 7 Dockerfile đa giai đoạn cho 6 microservices backend (user-service, center-service, course-service, attendance-service, report-service, dashboard-service) và frontend sử dụng base image `eclipse-temurin:21-jre-jammy` (image cơ sở <200MB, image cuối cùng <500MB). Module Terraform cung cấp VPC với subnet riêng (10.10.0.0/16), GKE Autopilot cluster, Cloud SQL PostgreSQL 15 high availability, IAM service account với Workload Identity, KMS keyring mã hóa AES-256, Cloud Storage bucket cho backup. Manifest GKE cho 6 microservice backend với Deployment (replicas 2, resource requests/limits, liveness/readiness probe), Service ClusterIP, HPA scale khi CPU > 70% hoặc latency > 300ms, Ingress NGINX với TLS 1.3, NetworkPolicy hạn chế traffic giữa namespace.

Cuối cùng, bộ tài liệu kiến trúc doanh nghiệp được hoàn thiện tại `./sources/docs/` bao gồm: System Overview, sơ đồ C4 Context/Container, Microservices Decomposition, OpenAPI 3.1 tổng hợp, ERD database, Terraform deployment guide, GKE orchestration guide, CI/CD pipeline guide, GDPR/CCPA compliance, Security Baseline OWASP, runbook vận hành, Disaster Recovery plan, i18n/SEO implementation guide, Responsive design guide.

Mục tiêu chính của giai đoạn là toàn bộ hệ thống có thể triển khai lên môi trường GKE production thông qua lệnh `terraform apply` và `kubectl apply`, đáp ứng SLA 99.9% uptime, hỗ trợ 10.000 concurrent user với P95 < 200ms, đồng thời cung cấp đầy đủ tài liệu cho team vận hành và mobile team phát triển ứng dụng native.

## 2. Phạm Vi Kỹ Thuật Cho Phép Và Ranh Giới Thư Mục

Danh sách đầy đủ các tệp tin vật lý được phép tạo mới hoặc tái cấu trúc trong giai đoạn 5, tuân thủ nghiêm ngặt quy ước gói `org.nlh4j.membershiphub` và ranh giới thư mục doanh nghiệp, phân tách theo từng nhóm nghiệp vụ frontend, backend microservice mới, hạ tầng DevOps và tài liệu:

* `./sources/frontend/web-app/src/app/layout.tsx` — [REQ-020], [REQ-022], [REQ-023]
* `./sources/frontend/web-app/src/app/dashboard/page.tsx` — [REQ-025], [REQ-020]
* `./sources/frontend/web-app/src/components/navigation/RoleNavMenu.tsx` — [REQ-020]
* `./sources/frontend/web-app/src/i18n/request.ts` — [REQ-022], [REQ-023], [NFR-007]
* `./sources/frontend/web-app/middleware.ts` — [REQ-022], [REQ-023]
* `./sources/frontend/web-app/src/app/sitemap.ts` — [REQ-023]
* `./sources/frontend/web-app/src/app/robots.ts` — [REQ-023]
* `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/controller/ReportController.java` — [REQ-024]
* `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/service/AttendanceReportService.java` — [REQ-024]
* `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/dto/AttendanceReportDto.java` — [REQ-024]
* `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/exception/InvalidDateRangeException.java` — [REQ-024], [EXC-004]
* `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/exception/GlobalExceptionMapper.java` — [REQ-024], [EXC-004]
* `./sources/backend/dashboard-service/src/main/java/org/nlh4j/membershiphub/dashboardservice/controller/EnrollmentDashboardController.java` — [REQ-025]
* `./sources/backend/dashboard-service/src/main/java/org/nlh4j/membershiphub/dashboardservice/service/EnrollmentSummaryService.java` — [REQ-025]
* `./sources/backend/dashboard-service/src/main/java/org/nlh4j/membershiphub/dashboardservice/dto/EnrollmentSummaryDto.java` — [REQ-025]
* `./sources/backend/dashboard-service/src/main/resources/application.properties` — [REQ-025]
* `./sources/frontend/web-app/src/app/attendance-report/page.tsx` — [REQ-024]
* `./sources/frontend/web-app/src/app/enrollment-dashboard/page.tsx` — [REQ-025]
* `./sources/frontend/web-app/src/app/[locale]/layout.tsx` — [REQ-022], [REQ-023], [NFR-007]
* `./sources/infra/docker/user-service.Dockerfile` — [NFR-005], [NFR-004]
* `./sources/infra/docker/center-service.Dockerfile` — [NFR-005], [NFR-004]
* `./sources/infra/docker/course-service.Dockerfile` — [NFR-005], [NFR-004]
* `./sources/infra/docker/attendance-service.Dockerfile` — [NFR-005], [NFR-004]
* `./sources/infra/docker/report-service.Dockerfile` — [NFR-005], [NFR-004]
* `./sources/infra/docker/dashboard-service.Dockerfile` — [NFR-005], [NFR-004]
* `./sources/infra/docker/frontend.Dockerfile` — [NFR-005], [NFR-004]
* `./sources/infra/terraform/main.tf` — [NFR-002], [NFR-004], [NFR-008], [NFR-009]
* `./sources/infra/terraform/vpc.tf` — [NFR-002], [NFR-004]
* `./sources/infra/terraform/gke.tf` — [NFR-002], [NFR-004]
* `./sources/infra/terraform/cloudsql.tf` — [NFR-004], [NFR-009]
* `./sources/infra/terraform/iam.tf` — [NFR-003], [NFR-008]
* `./sources/infra/terraform/kms.tf` — [NFR-003], [NFR-008]
* `./sources/infra/terraform/storage.tf` — [NFR-009]
* `./sources/infra/k8s/user-service/deployment.yaml` — [NFR-001], [NFR-002], [NFR-004]
* `./sources/infra/k8s/user-service/hpa.yaml` — [NFR-001], [NFR-004]
* `./sources/infra/k8s/user-service/service.yaml` — [NFR-001], [NFR-004]
* `./sources/infra/k8s/center-service/deployment.yaml` — [NFR-001], [NFR-002], [NFR-004]
* `./sources/infra/k8s/center-service/hpa.yaml` — [NFR-001], [NFR-004]
* `./sources/infra/k8s/center-service/service.yaml` — [NFR-001], [NFR-004]
* `./sources/infra/k8s/course-service/deployment.yaml` — [NFR-001], [NFR-002], [NFR-004]
* `./sources/infra/k8s/course-service/hpa.yaml` — [NFR-001], [NFR-004]
* `./sources/infra/k8s/course-service/service.yaml` — [NFR-001], [NFR-004]
* `./sources/infra/k8s/attendance-service/deployment.yaml` — [NFR-001], [NFR-002], [NFR-004]
* `./sources/infra/k8s/attendance-service/hpa.yaml` — [NFR-001], [NFR-004]
* `./sources/infra/k8s/attendance-service/service.yaml` — [NFR-001], [NFR-004]
* `./sources/infra/k8s/report-service/deployment.yaml` — [NFR-001], [NFR-002], [NFR-004]
* `./sources/infra/k8s/report-service/hpa.yaml` — [NFR-001], [NFR-004]
* `./sources/infra/k8s/report-service/service.yaml` — [NFR-001], [NFR-004]
* `./sources/infra/k8s/dashboard-service/deployment.yaml` — [NFR-001], [NFR-002], [NFR-004]
* `./sources/infra/k8s/dashboard-service/hpa.yaml` — [NFR-001], [NFR-004]
* `./sources/infra/k8s/dashboard-service/service.yaml` — [NFR-001], [NFR-004]
* `./sources/infra/k8s/frontend/deployment.yaml` — [NFR-001], [NFR-002]
* `./sources/infra/k8s/frontend/service.yaml` — [NFR-001]
* `./sources/infra/k8s/ingress.yaml` — [NFR-001], [NFR-003]
* `./sources/infra/k8s/network-policy.yaml` — [NFR-003]
* `./sources/infra/k8s/configmap.yaml` — [NFR-001], [NFR-007]
* `./sources/infra/k8s/secret.yaml` — [NFR-003]
* `./sources/infra/gcp/cloudbuild.yaml` — [NFR-004]
* `./sources/infra/gcp/artifact-registry.tf` — [NFR-005]
* `./sources/docs/architecture/01-system-overview.md` — [DOC-001]
* `./sources/docs/architecture/02-c4-context.md` — [DOC-001]
* `./sources/docs/architecture/03-c4-container.md` — [DOC-001]
* `./sources/docs/architecture/04-microservices-decomposition.md` — [DOC-001]
* `./sources/docs/api/openapi.yaml` — [DOC-001], [ARC-009]
* `./sources/docs/database/01-schema-overview.md` — [DOC-001]
* `./sources/docs/database/02-erd-diagram.md` — [DOC-001]
* `./sources/docs/devops/01-terraform-deployment.md` — [DOC-001]
* `./sources/docs/devops/02-gke-orchestration.md` — [DOC-001]
* `./sources/docs/devops/03-cicd-pipeline.md` — [DOC-001]
* `./sources/docs/compliance/01-gdpr-ccpa.md` — [DOC-001], [NFR-008]
* `./sources/docs/compliance/02-security-baseline.md` — [DOC-001], [NFR-003]
* `./sources/docs/operations/01-runbook.md` — [DOC-001], [NFR-006]
* `./sources/docs/operations/02-disaster-recovery.md` — [DOC-001], [NFR-009]
* `./sources/docs/seo/01-internationalization.md` — [DOC-001], [REQ-023]
* `./sources/docs/seo/02-hreflang-implementation.md` — [DOC-001], [REQ-023]
* `./sources/docs/frontend/01-responsive-design.md` — [DOC-001], [REQ-020]

* **RÀNG BUỘC BẮT BUỘC VỀ BỘ KHUNG NỀN TẢNG**:
  - Tất cả tài sản mã nguồn ứng dụng trong giai đoạn 5 phải kế thừa bộ khung build descriptors đã được khởi tạo ở Giai đoạn 1, bao gồm `./sources/backend/pom.xml` (root parent) và 6 tệp con `./sources/backend/<service-name>/pom.xml` cho `user-service`, `center-service`, `course-service`, `attendance-service`, `report-service`, `dashboard-service`. Do giai đoạn 5 bổ sung 2 microservice mới (`report-service`, `dashboard-service`) nên cần mở rộng danh sách module trong `./sources/backend/pom.xml` parent.
  - Tệp `./sources/frontend/package.json` và `./sources/frontend/tsconfig.json` đã được khởi tạo ở Giai đoạn 1, không tái tạo trong giai đoạn này.
  - Toàn bộ mã nguồn mới phải tuân thủ quy ước gói `org.nlh4j.membershiphub.<service-name>` và được truy vết bằng các mã thẻ quy định trong ma trận phân bổ ngay từng dòng lệnh.

## 3. Chỉ Thị Chức Năng Cho Từng Sub-Agent

* **Coder**: Đóng vai trò lập trình viên ứng dụng chính. Chịu trách nhiệm hiện thực hóa toàn bộ controller, service, DTO, exception handler trong package `controller`, `service`, `dto`, `exception` của 2 microservice mới `report-service` và `dashboard-service`. Song song đó, Coder cũng chịu trách nhiệm phát triển tầng frontend Next.js tại `./sources/frontend/web-app/src/app/`, component điều hướng, layout, middleware i18n, sitemap. Bị cấm viết bộ kiểm thử, tài liệu hoặc cấu hình hạ tầng Docker/Terraform/GKE.

* **Tester**: Đóng vai trò kiểm thử viên chính. Tạo bộ kiểm thử đơn vị JUnit 5 kết hợp Mockito cho `ReportController`, `AttendanceReportService`, `EnrollmentDashboardController`, `EnrollmentSummaryService`. Xây dựng bộ kiểm thử tích hợp sử dụng Testcontainers (PostgreSQL, Redis) cho microservice report và dashboard. Đối với frontend, tạo bộ kiểm thử Jest kết hợp React Testing Library cho `RoleNavMenu`, `middleware.ts`. Bị cấm sửa đổi mã nguồn sản phẩm.

* **Doc**: Soạn thảo bộ tài liệu kiến trúc tổng thể tại `./sources/docs/` bao gồm: System Overview, sơ đồ C4 Context/Container, Microservices Decomposition, OpenAPI 3.1 tổng hợp, ERD database, Terraform deployment guide, GKE orchestration guide, CI/CD pipeline guide, GDPR/CCPA compliance, Security Baseline OWASP, runbook vận hành, Disaster Recovery plan, i18n/SEO implementation guide, Responsive design guide. Bị cấm viết mã nguồn sản phẩm.

* **Reviewer**: Thực hiện rà soát chất lượng mã nguồn theo checklist OWASP Top 10, đánh giá tính đúng đắn của logic truy vấn báo cáo CSV với parameter binding, validation khoảng ngày, cache dashboard. Xác minh tính bảo mật của việc sử dụng JPQL parameter binding, validation input với `@Valid`, mã hóa PII. Phát hiện sớm các vấn đề race condition, memory leak trong streaming CSV, thread safety trong dashboard cache.

* **Docker**: Chuyên trách container hóa. Sinh 7 Dockerfile đa giai đoạn (multi-stage build) cho 6 microservice backend và frontend sử dụng base image Eclipse Temurin 21 JRE, đảm bảo kích thước image cuối cùng < 500MB, cấu hình HEALTHCHECK, USER non-root, ENTRYPOINT tối ưu GC.

* **GCP**: Chuyên trách tự động hóa đám mây Google Cloud Platform. Sinh mã Terraform cung cấp VPC, GKE Autopilot cluster, Cloud SQL PostgreSQL 15 high availability, IAM service account với Workload Identity, KMS keyring mã hóa AES-256, Cloud Storage bucket cho backup. Cấu hình biến `project_id`, `region`, `db_password` qua Secret Manager.

* **GKE**: Chuyên trách điều phối container production trong Google Kubernetes Engine. Sinh manifest Kubernetes cho 6 microservice backend và frontend với Deployment (replicas 2, resource requests/limits, liveness/readiness probe), Service ClusterIP, HPA scale khi CPU > 70% hoặc latency > 300ms, Ingress NGINX với TLS 1.3, NetworkPolicy hạn chế traffic giữa namespace.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn (DoD)

Giai đoạn 5 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: (1) Layout gốc Next.js tích hợp `next-intl` provider, component `RoleNavMenu` hiển thị menu theo 5 vai trò; (2) Middleware phát hiện locale từ cookie `NEXT_LOCALE` và fallback `Accept-Language` header, chuyển hướng đúng về `/[locale]/...`; (3) Thẻ `<html lang>`, thẻ `hreflang` cho 3 ngôn ngữ (en, vi, es), sitemap.xml đa ngôn ngữ được sinh đúng; (4) Microservice `report-service` với endpoint `GET /api/v1/reports/attendance` xuất CSV giới hạn khoảng ngày tối đa 30 ngày, từ chối khoảng ngày > 30 ngày với mã `INVALID_DATE_RANGE`; (5) Microservice `dashboard-service` với endpoint `GET /api/v1/dashboard/enrollment-summary` cache 15 phút thông qua Redis, trả về cards `totalStudents`, `activeCourses`, `upcomingSessions`; (6) View materialized `mv_enrollment_summary` được tạo với unique index `ux_mv_enrollment_summary_center`; (7) 7 Dockerfile đa giai đoạn cho 6 microservice backend và frontend, image cuối cùng < 500MB, HEALTHCHECK `/q/health/ready`; (8) Module Terraform cung cấp VPC, GKE Autopilot, Cloud SQL PostgreSQL, IAM, KMS, Cloud Storage bucket; (9) Manifest GKE cho 6 microservice backend với HPA scale khi CPU > 70% hoặc latency > 300ms, Ingress NGINX với TLS 1.3, NetworkPolicy hạn chế traffic; (10) Bộ tài liệu kiến trúc doanh nghiệp hoàn thiện tại `./sources/docs/` gồm System Overview, C4 Context/Container, Microservices Decomposition, OpenAPI 3.1, ERD, Terraform deployment, GKE orchestration, CI/CD, GDPR/CCPA, Security Baseline, runbook, Disaster Recovery, i18n/SEO, Responsive design; (11) 100% thẻ truy vết REQ-020, REQ-022, REQ-023, REQ-024, REQ-025, NFR-001 đến NFR-009, DOC-001 được ánh xạ đầy đủ vào mã nguồn, hạ tầng và tài liệu; (12) 100% bộ kiểm thử JUnit và Jest đạt trạng thái PASS với code coverage >= 80% cho các lớp controller, service và frontend module.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->KHỞI TẠO LAYOUT RESPONSIVE, ĐIỀU HƯỚNG THEO ROLE VÀ HẠ TẦNG I18N<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 1.1: Cấu hình layout gốc và điều hướng theo vai trò

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/frontend/web-app/src/app/layout.tsx`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-020], [NFR-007]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh layout gốc của ứng dụng Next.js 14 với App Router tại đường dẫn `./sources/frontend/web-app/src/app/layout.tsx`. Tích hợp `next-intl` provider thông qua `NextIntlClientProvider`, `LocaleProvider` và cấu hình font Inter đa ngôn ngữ thông qua `next/font/google`. Liên kết `<RoleNavMenu />` để hiển thị menu điều hướng theo 5 role (Student, Teacher, Manager, Center Admin, System Admin) thông qua prop `session.role` lấy từ server component thông qua `getServerSession()`. Đảm bảo semantic HTML với `<html lang={locale}>` tuân thủ [REQ-023] và [NFR-007]. Cấu hình metadata API để sinh thẻ `og:locale`, `og:title`, `og:description` tự động.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục frontend layout
```
<!--END_DDL_MIGRATION-->

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
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
<!--END_API_CONTRACT-->

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```typescript
// Hạn chế quyền truy cập menu theo role, fallback về dashboard nếu role không khớp
export function resolveMenuForRole(items: MenuItem[], role: Role): MenuItem[] {
  return items.filter((item) => item.roles.includes(role));
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 1.2: Kiểm thử đơn vị layout và menu điều hướng

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/frontend/web-app/src/test/layout-role-menu.spec.tsx
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-020], [NFR-007]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Biên soạn bộ kiểm thử React Testing Library tại đường dẫn `./sources/frontend/web-app/src/test/layout-role-menu.spec.tsx` cho `RoleNavMenu` xác nhận danh sách menu hiển thị đúng theo từng role (Student thấy 3 menu items, Center Admin thấy 6 menu items), locale switch cập nhật nhãn thông qua `next-intl`, và thuộc tính `lang` trên thẻ `<html>` phản ánh locale hiện tại. Tích hợp snapshot test cho layout gốc. Mock `getServerSession()` trả về session với role tương ứng.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục kiểm thử frontend
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 1.3: Đánh giá mã layout và đề xuất cải tiến

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/frontend/web-app/src/app/layout.tsx`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-020], [NFR-007]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer đánh giá cấu trúc layout gốc tại đường dẫn `./sources/frontend/web-app/src/app/layout.tsx` và component `RoleNavMenu` tại `./sources/frontend/web-app/src/components/navigation/RoleNavMenu.tsx`: xác nhận tách biệt server/client component đúng chuẩn Next.js App Router, kiểm tra SEO meta tags (title, description, og:*), phân tích khả năng mở rộng khi thêm role mới, đề xuất lazy-load icon và giảm re-render không cần thiết. Ghi nhận phát hiện vào biên bản review để Coder xử lý. Kiểm tra tuân thủ OWASP A05 Security Misconfiguration thông qua header CSP.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục review layout
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 1.4: Biên soạn tài liệu thiết kế responsive

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/frontend/01-responsive-design.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [REQ-020]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tài liệu kiến trúc frontend responsive tại đường dẫn `./sources/docs/frontend/01-responsive-design.md` gồm: nguyên tắc Mobile-First, bảng breakpoint (sm/md/lg/xl), cấu trúc NativeWind tokens, danh sách component dùng chung, hướng dẫn kiểm thử trên thiết bị thật. Đính kèm sơ đồ Mermaid phân cấp layout và mô tả luồng điều hướng theo role. Tài liệu phải chứa checklist tuân thủ WCAG 2.1 AA cho accessibility.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục tài liệu frontend
```
<!--END_DDL_MIGRATION-->

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->TÍCH HỢP NEXT-INTL, MIDDLEWARE PHÁT HIỆN NGÔN NGỮ VÀ HỖ TRỢ SEO ĐA NGÔN NGỮ<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 2.1: Triển khai middleware phát hiện ngôn ngữ và i18n

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/frontend/web-app/middleware.ts`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-022], [REQ-023], [NFR-007]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Cài đặt Next.js middleware tại đường dẫn `./sources/frontend/web-app/middleware.ts` kiểm tra cookie `NEXT_LOCALE`; nếu không có, đọc `Accept-Language` header và chuyển hướng sang đường dẫn `/[locale]/...` phù hợp với một trong 3 ngôn ngữ được hỗ trợ (en, vi, es). Tích hợp danh sách locale cho phép và cơ chế ghi nhận lựa chọn ngôn ngữ của người dùng vào cookie với thời hạn 1 năm. Cấu hình `matcher` loại trừ các đường dẫn API, `_next`, file tĩnh.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục middleware ngôn ngữ
```
<!--END_DDL_MIGRATION-->

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```typescript
// Cấu hình matcher cho middleware
export const config = {
  matcher: ['/((?!api|_next|.*\\..*).*)']
};
```
<!--END_API_CONTRACT-->

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```typescript
// Xử lý locale không hợp lệ: fallback về 'en' mặc định
const SUPPORTED_LOCALES = ['en', 'vi', 'es'] as const;
type SupportedLocale = (typeof SUPPORTED_LOCALES)[number];

export function isSupportedLocale(value: string): value is SupportedLocale {
  return (SUPPORTED_LOCALES as readonly string[]).includes(value);
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 2.2: Kiểm thử middleware và i18n

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/frontend/web-app/src/test/middleware-i18n.spec.ts
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-022], [REQ-023], [NFR-007]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo bộ kiểm thử tích hợp tại đường dẫn `./sources/frontend/web-app/src/test/middleware-i18n.spec.ts` với Jest + msw mô phỏng header `Accept-Language` đa dạng (en-US, vi-VN, es-ES) và cookie `NEXT_LOCALE`. Xác nhận middleware chuyển hướng đúng URL `/vi/courses`, `/es/centers`, ghi nhận cookie khi người dùng đổi ngôn ngữ, và fallback về locale mặc định `en` khi giá trị không được hỗ trợ (ví dụ: `fr-FR`).

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục kiểm thử middleware
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 2.3: Đánh giá cơ chế i18n và SEO

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/frontend/web-app/src/app/[locale]/layout.tsx`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-022], [REQ-023], [NFR-007]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Kiểm tra tệp `[locale]/layout.tsx` tại đường dẫn `./sources/frontend/web-app/src/app/[locale]/layout.tsx` đảm bảo: thẻ `<html lang>` chính xác theo locale, thẻ meta `og:locale`, thẻ `alternate` hreflang đầy đủ cho 3 ngôn ngữ (en, vi, es) cùng thẻ `x-default`, sơ đồ sitemap.xml đa ngôn ngữ tại `./sources/frontend/web-app/src/app/sitemap.ts`, robots.txt tại `./sources/frontend/web-app/src/app/robots.ts` không chặn crawler. Đánh giá hiệu năng tải bản dịch thông qua chunk splitting.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục review i18n
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 2.4: Soạn thảo tài liệu SEO đa ngôn ngữ

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/seo/01-internationalization.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [REQ-023]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Biên soạn tài liệu hướng dẫn triển khai SEO đa ngôn ngữ tại các đường dẫn `./sources/docs/seo/01-internationalization.md` và `./sources/docs/seo/02-hreflang-implementation.md` gồm: chiến lược URL (path-based locale), cấu hình `next-intl`, danh sách hreflang, hướng dẫn tạo sitemap đa ngôn ngữ, checklist Google Search Console. Bao gồm ví dụ thực tế cho locale `vi` với tiêu đề tiếng Việt và cấu trúc URL `/vi/khoa-hoc`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục tài liệu SEO
```
<!--END_DDL_MIGRATION-->

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->XÂY DỰNG BÁO CÁO ĐIỂM DANH CSV VÀ DASHBOARD TUYỂN SINH<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 3.1: Triển khai microservice báo cáo và xuất CSV

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/controller/ReportController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-024], [NFR-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh `ReportController` Quarkus REST tại đường dẫn `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/controller/ReportController.java` với endpoint `GET /api/v1/reports/attendance?centerId&startDate&endDate`. Endpoint sử dụng `AttendanceReportService` để truy vấn dữ liệu từ PostgreSQL thông qua JPA, đóng gói CSV thông qua `CsvWriter` (OpenCSV) với streaming để tránh OOM. Giới hạn khoảng ngày tối đa 30 ngày và phát sinh `InvalidDateRangeException` nếu vi phạm. Phản hồi `text/csv` với header `Content-Disposition: attachment; filename="attendance.csv"`. Annotation `@RolesAllowed({"CENTER_ADMIN", "SYSTEM_ADMIN"})` đảm bảo phân quyền.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Bổ sung chỉ mục phục vụ truy vấn báo cáo
CREATE INDEX IF NOT EXISTS ix_attendance_center_date
    ON attendance (course_id, attendance_date);
CREATE INDEX IF NOT EXISTS ix_enrollments_course_student
    ON enrollments (course_id, student_id);

-- View materialized tổng hợp dữ liệu báo cáo
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
<!--END_DDL_MIGRATION-->

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
```json
{
  "openapi": "3.1.0",
  "info": {
    "title": "Membership Hub - Reporting API",
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
<!--END_API_CONTRACT-->

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```java
// [EXC-004] ràng buộc khoảng ngày báo cáo tối đa 30 ngày
package org.nlh4j.membershiphub.reportservice.exception;

public class InvalidDateRangeException extends RuntimeException {
    public InvalidDateRangeException(String message) {
        super(message);
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 3.2: Kiểm thử tích hợp microservice báo cáo

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/backend/report-service/src/test/java/org/nlh4j/membershiphub/reportservice/ReportServicesTestSuite.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-024], [NFR-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Biên soạn bộ kiểm thử tích hợp tại đường dẫn `./sources/backend/report-service/src/test/java/org/nlh4j/membershiphub/reportservice/ReportServicesTestSuite.java` với Quarkus Test + RestAssured, fixture PostgreSQL Testcontainers, mô phỏng dữ liệu attendance cho 7 ngày. Xác nhận endpoint trả về CSV đúng cột (StudentName, CourseName, AttendanceDate, Status), từ chối khoảng ngày > 30 ngày với mã `INVALID_DATE_RANGE`, và xử lý truy vấn trong vòng 200ms. Mock Redis cache cho dashboard service với TTL 900 giây.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục kiểm thử báo cáo
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 3.3: Đánh giá microservice báo cáo

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/service/AttendanceReportService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-024], [NFR-001], [NFR-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Kiểm tra mã truy vấn SQL tại đường dẫn `./sources/backend/report-service/src/main/java/org/nlh4j/membershiphub/reportservice/service/AttendanceReportService.java` có sử dụng parameter binding, tránh SQL injection theo OWASP A03, có tận dụng index `(course_id, attendance_date)`. Đánh giá logic streaming CSV để tránh OOM với dữ liệu lớn. Xác nhận tường lửa cho phép chỉ Center Admin truy cập tài nguyên. Kiểm tra mã hóa PII thông qua PiiMaskingSerializer cho trường tax_id và email.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục review báo cáo
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 3.4: Soạn thảo tài liệu báo cáo và dashboard

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/operations/01-runbook.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [REQ-024], [REQ-025]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Bổ sung runbook vận hành tại đường dẫn `./sources/docs/operations/01-runbook.md` cho dashboard và báo cáo: quy trình xuất CSV, xử lý sự cố timeout truy vấn, hướng dẫn rebuild materialized view `REFRESH MATERIALIZED VIEW CONCURRENTLY mv_enrollment_summary`, danh sách chỉ số giám sát (latency P95, row count, cache hit ratio). Cập nhật sơ đồ luồng dữ liệu từ PostgreSQL đến dashboard với sơ đồ Mermaid.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục tài liệu runbook
```
<!--END_DDL_MIGRATION-->

### 🌤️ NGÀY 4: <!--DAY_HEADER_START-->HẠ TẦNG DOCKER ĐA GIAI ĐOẠN VÀ ĐẨY IMAGE LÊN REGISTRY<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 4.1: Sinh Dockerfile đa giai đoạn cho microservices Quarkus

##### Sub-Agent được phân công: Docker
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/infra/docker/user-service.Dockerfile`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-005], [NFR-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Biên soạn 7 Dockerfile đa giai đoạn (multi-stage build) cho 6 microservice backend (`user-service`, `center-service`, `course-service`, `attendance-service`, `report-service`, `dashboard-service`) tại đường dẫn `./sources/infra/docker/<service-name>.Dockerfile` và frontend tại `./sources/infra/docker/frontend.Dockerfile`. Sử dụng base image giai đoạn `builder` là `maven:3.9-eclipse-temurin-21` chạy `./mvnw package -DskipTests`, giai đoạn `runtime` sử dụng `eclipse-temurin:21-jre-jammy` (image cơ sở <200MB), sao chép JAR đã build, cấu hình `USER 1000` (không chạy với root), `HEALTHCHECK` dùng `curl -fsS http://localhost:8080/q/health/ready || exit 1`, và thiết lập `JAVA_OPTS` để tối ưu GC với `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`. Kích thước image cuối cùng phải <500MB tuân thủ [NFR-005]. Sử dụng `.dockerignore` loại trừ `target/`, `.git/`, `node_modules/`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục Dockerfile
```
<!--END_DDL_MIGRATION-->

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
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
<!--END_API_CONTRACT-->

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```dockerfile
# Hạn chế quyền và đảm bảo container chạy với UID không phải root
ONBUILD USER 1000
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 4.2: Kiểm thử tích hợp build và push Docker image

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/infra/test/maven-build-integration.sh
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-005], [NFR-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo script tích hợp shell `maven-build-integration.sh` tại đường dẫn `./sources/infra/test/maven-build-integration.sh` gọi `docker build` cho từng microservice với tag `membershiphub-<service>:v5.0.0`, chạy `docker image inspect` kiểm tra kích thước dưới 500MB, `docker run --rm` health check. Kết hợp `trivy image` quét lỗ hổng bảo mật cơ bản theo NFR-003. Verify container chạy với UID 1000 không phải root.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục kiểm thử Docker build
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 4.3: Soạn thảo tài liệu triển khai container

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/devops/01-terraform-deployment.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [NFR-005]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Bổ sung tài liệu quy trình build và push container tại đường dẫn `./sources/docs/devops/01-terraform-deployment.md`: hướng dẫn `docker buildx` multi-arch (linux/amd64, linux/arm64), chiến lược tag immutable (sử dụng git SHA), cấu hình Artifact Registry, tích hợp Cloud Build thông qua `./sources/infra/gcp/cloudbuild.yaml`. Mô tả biện pháp rà soát bảo mật image với Trivy và quy trình rollback khi image lỗi.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục tài liệu container
```
<!--END_DDL_MIGRATION-->

### 🌤️ NGÀY 5: <!--DAY_HEADER_START-->CUNG CẤP HẠ TẦNG GCP VỚI TERRAFORM VÀ TRIỂN KHAI GKE<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 5.1: Sinh mã Terraform cho VPC, GKE, Cloud SQL, IAM, KMS

##### Sub-Agent được phân công: GCP
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/infra/terraform/main.tf`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-003], [NFR-004], [NFR-008], [NFR-009]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh module Terraform tại đường dẫn `./sources/infra/terraform/main.tf` cung cấp: VPC với subnet riêng (`10.10.0.0/16`) thông qua `./sources/infra/terraform/vpc.tf`, GKE Autopilot cluster tại `./sources/infra/terraform/gke.tf` với Workload Identity, Cloud SQL PostgreSQL 15 với high availability tại `./sources/infra/terraform/cloudsql.tf`, IAM service account cho workload identity tại `./sources/infra/terraform/iam.tf`, KMS keyring mã hóa AES-256 tại `./sources/infra/terraform/kms.tf`, Cloud Storage bucket cho backup tại `./sources/infra/terraform/storage.tf`. Cấu hình biến `project_id`, `region`, `db_password` qua Secret Manager. Bật Private Service Access và Cloud Logging.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục Terraform GCP
```
<!--END_DDL_MIGRATION-->

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
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
<!--END_API_CONTRACT-->

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
```hcl
# Bảo vệ chống xoá nhầm database production
lifecycle {
  prevent_destroy = true
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 5.2: Kiểm thử tích hợp cấu hình Terraform

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/infra/test/terraform-integration.sh
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh script `terraform-integration.sh` tại đường dẫn `./sources/infra/test/terraform-integration.sh` chạy `terraform init -backend=false`, `terraform validate`, `terraform plan -out=tfplan` với mock provider. Tích hợp `tflint` và `checkov` quét cấu hình sai lệch so với baseline bảo mật. Verify các module VPC, GKE, Cloud SQL, IAM, KMS, Storage đều có resource được khai báo đầy đủ.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục kiểm thử Terraform
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 5.3: Triển khai manifest GKE với HPA, Ingress, Network Policy

##### Sub-Agent được phân công: GKE
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/infra/k8s/user-service/deployment.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-001], [NFR-002], [NFR-003], [NFR-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Sinh manifest Kubernetes cho 6 microservice backend tại các đường dẫn `./sources/infra/k8s/<service-name>/deployment.yaml`, `./sources/infra/k8s/<service-name>/hpa.yaml`, `./sources/infra/k8s/<service-name>/service.yaml`. Mỗi Deployment cấu hình: replicas 2, resource requests (cpu 250m, memory 512Mi) và limits (cpu 1000m, memory 1Gi), liveness probe `/q/health/live`, readiness probe `/q/health/ready`, env từ ConfigMap và Secret. HPA với minReplicas 2, maxReplicas 10, scale khi CPU > 70% hoặc latency > 300ms. Ingress NGINX tại `./sources/infra/k8s/ingress.yaml` với TLS 1.3. NetworkPolicy tại `./sources/infra/k8s/network-policy.yaml` hạn chế traffic giữa namespace. Tích hợp PodDisruptionBudget đảm bảo availability.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục manifest GKE
```
<!--END_DDL_MIGRATION-->

* **Đặc Tả Hợp Đồng API Và Sự Kiện [REQ-XXX], [ARC-XXX]:**

<!--START_API_CONTRACT-->
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
<!--END_API_CONTRACT-->

* **Bộ Xử Lý Ngoại Lệ Giai Đoạn [EXC-XXX]:**

<!--START_EXC_HANDLER-->
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
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 5.4: Kiểm thử tích hợp manifest GKE

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/infra/test/gke-manifest-integration.sh
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-001], [NFR-002], [NFR-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo script `gke-manifest-integration.sh` tại đường dẫn `./sources/infra/test/gke-manifest-integration.sh` sử dụng `kubeconform` xác thực schema manifest, `kubectl --dry-run=server apply` trong cluster kind. Kiểm tra HPA, NetworkPolicy, Ingress khớp cấu hình mong đợi và không có quyền mở rộng ngoài namespace `membership-hub` cho phép. Verify `runAsNonRoot: true` cho mọi Pod.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục kiểm thử GKE
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 5.5: Hoàn thiện bộ tài liệu kiến trúc và vận hành

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/architecture/01-system-overview.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [NFR-006], [NFR-009]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Hoàn thiện bộ tài liệu kiến trúc tổng thể tại các đường dẫn `./sources/docs/architecture/01-system-overview.md`, `./sources/docs/architecture/02-c4-context.md`, `./sources/docs/architecture/03-c4-container.md`, `./sources/docs/architecture/04-microservices-decomposition.md`, `./sources/docs/api/openapi.yaml`, `./sources/docs/database/01-schema-overview.md`, `./sources/docs/database/02-erd-diagram.md`, `./sources/docs/devops/01-terraform-deployment.md`, `./sources/docs/devops/02-gke-orchestration.md`, `./sources/docs/devops/03-cicd-pipeline.md`, `./sources/docs/compliance/01-gdpr-ccpa.md`, `./sources/docs/compliance/02-security-baseline.md`, `./sources/docs/operations/01-runbook.md`, `./sources/docs/operations/02-disaster-recovery.md`. Bao gồm sơ đồ Mermaid mô tả luồng triển khai từ CI/CD đến GKE. Tài liệu phải có chỉ mục liên kết chéo giữa các file và checklist triển khai cuối cùng.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi lược đồ cơ sở dữ liệu cho hạng mục tài liệu tổng thể
```
<!--END_DDL_MIGRATION-->