# Giai Đoạn 5: <!--PHASE_NAME_START-->Triển Khai Hạ Tầng DevOps, Bảo Mật và Hoàn Thiện Tài Liệu Kỹ Thuật<!--PHASE_NAME_END-->

## 📊 Quản Lý Tài Liệu

| Mục | Chi tiết |
| :--- | :--- |
| **Mã bản thiết kế** | ARCH-20260828112120 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 5 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Triển Khai Hạ Tầng DevOps, Bảo Mật và Hoàn Thiện Tài Liệu Kỹ Thuật<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn 5 đóng vai trò then chốt trong việc đưa toàn bộ mã nguồn ứng dụng đã hoàn thiện từ Giai đoạn 1 đến Giai đoạn 4 vào môi trường vận hành thực tế. Giai đoạn này tập trung 100% vào container hóa sáu microservices Quarkus thông qua multi-stage Dockerfile tối ưu với giới hạn kích thước base image dưới 200MB và final image dưới 500MB, tự động hóa hạ tầng đám mây trên Google Cloud Platform thông qua Terraform (VPC, IAM, Cloud SQL PostgreSQL, Memorystore Redis, GCS), triển khai orchestration trên Google Kubernetes Engine với Helm charts và HorizontalPodAutoscaler tự động scale khi CPU vượt 70% hoặc độ trễ vượt 300ms, áp dụng các biện pháp bảo mật đạt chuẩn OWASP Top 10 với TLS 1.3, AES-256 at-rest, GDPR/CCPA data export, audit log retention 1 năm, cùng với việc xuất bản bộ tài liệu kỹ thuật doanh nghiệp hoàn chỉnh phục vụ vận hành và bảo trì hệ thống lâu dài.<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Baseline) |
| **Ngày giờ** | 2026/08/28 11:21:20 |
| **Tác giả** | Enterprise System Architect (SA Agent) |
| **Phê duyệt** | Đang chờ đánh giá quản trị kỹ thuật |

## 1. Phạm Vi Hoạt Động và Mục Tiêu Của Giai Đoạn

Giai đoạn 5 thuộc dự án membership-hub tập trung vào bốn trụ cột kỹ thuật chính được phân bổ từ bảng tổng hợp sản phẩm tổng thể. Trụ cột thứ nhất là container hóa toàn bộ sáu microservices Quarkus (user-service, center-service, course-service, enrollment-service, attendance-service, notification-service) thông qua multi-stage Dockerfile sử dụng base image `eclipse-temurin:21-jre-alpine` cho runtime và `eclipse-temurin:21-jdk-alpine` cho build stage, đảm bảo giới hạn kích thước base image dưới 200MB và final image dưới 500MB theo [NFR-005], sau đó đẩy image lên Google Artifact Registry tại region `asia-southeast1` với quy ước tagging theo `${GIT_SHA}` và `${GIT_BRANCH}-latest` để hỗ trợ rollback nhanh.

Trụ cột thứ hai là tự động hóa hạ tầng GCP thông qua Terraform, bao gồm tạo VPC `membershiphub-vpc` với CIDR `10.0.0.0/16` và hai subnet chuyên biệt (gke-subnet cho GKE nodes, sql-subnet cho Cloud SQL Private Service Access), cấu hình Cloud SQL PostgreSQL 15 instance với high availability zone failover, automated backup hàng ngày retention 7 ngày, point-in-time recovery 24 giờ và read replica cho reporting workload theo [NFR-009], triển khai Memorystore Redis instance tier STANDARD 2GB làm caching layer cho enrollment dashboard, cùng cấu hình IAM service accounts với nguyên tắc least privilege và bật audit log cho mọi truy cập theo [NFR-006].

Trụ cột thứ ba là triển khai orchestration trên Google Kubernetes Engine thông qua Helm chart `membershiphub` với 6 service deployment, mỗi service sử dụng RollingUpdate strategy với `maxSurge: 25%` và `maxUnavailable: 0%`, tích hợp HorizontalPodAutoscaler với `minReplicas: 2, maxReplicas: 10` dựa trên metric CPU target 70% và latency 300ms theo [NFR-001] và [NFR-004], kèm theo Kustomize overlay cho môi trường production với namespace `membershiphub-prod` và secret generator chứa thông tin nhạy cảm từ Google Secret Manager. Áp dụng các biện pháp bảo mật TLS 1.3, AES-256 at-rest, OWASP Top 10 mitigations, GDPR/CCPA data export endpoint và audit log retention 1 năm theo [NFR-003], [NFR-006], [NFR-008].

Trụ cột cuối cùng là hoàn thiện bộ tài liệu kỹ thuật doanh nghiệp bốn mảng chính: System Architecture Blueprint mô tả tổng quan microservices và luồng dữ liệu OAuth2, Database Schema Topology liệt kê 11 bảng với sơ đồ ER, OpenAPI Contracts tổng hợp toàn bộ endpoint REST theo các Tag ID từ [ARC-006] đến [ARC-009], và Operational Manual bằng tiếng Việt hướng dẫn vận hành, xử lý sự cố, quy trình GDPR/CCPA. Toàn bộ hạ tầng phải được quản lý qua Infrastructure as Code, đảm bảo reproducibility và khả năng rollback khi có sự cố.

## 2. Phạm Vi Kỹ Thuật Được Phép và Ranh Giới Thư Mục

Danh sách kiểm tra kỹ thuật dưới đây định nghĩa 100% các tệp vật lý được phép khởi tạo trong phạm vi giai đoạn này, mỗi mục đại diện cho một tệp cụ thể kèm Tag ID truy vết:

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

**BẮT BUỘC VỀ BỘ KHUNG NỀN TẢNG**: Trong NGÀY 1 của Giai đoạn 5, Sub-Agent [Doc] phải khởi tạo hệ thống tài liệu framework bằng cách tạo bốn tệp tài liệu kỹ thuật doanh nghiệp tại `./sources/docs/`, đảm bảo cấu trúc thư mục tài liệu tập trung phục vụ truy vết và vận hành dài hạn. Toàn bộ tài sản tài liệu khung phải ánh xạ chặt chẽ tới mã theo dõi `[DOC-001]`.

## 3. Chỉ Thị Chức Năng Chuyên Biệt Cho Các Sub-Agent

* **Docker**: Chuyên gia container hóa và tối ưu hóa image. Chịu trách nhiệm xây dựng multi-stage Dockerfile cho sáu service backend theo chuẩn doanh nghiệp, đảm bảo giới hạn kích thước image, thiết lập non-root user, healthcheck endpoint, và cấu hình Artifact Registry. Bị cấm viết mã nguồn nghiệp vụ backend hoặc frontend.
* **GCP**: Chuyên gia tự động hóa hạ tầng Google Cloud Platform. Chịu trách nhiệm tạo và quản lý Terraform scripts cho VPC, IAM, Cloud SQL, Memorystore Redis, GCS. Bị cấm triển khai ứng dụng trực tiếp lên GKE hoặc viết Kubernetes manifests.
* **GKE**: Chuyên gia orchestration Google Kubernetes Engine. Chịu trách nhiệm xây dựng Helm charts, Kubernetes deployment manifests, HPA configurations, Kustomize overlays cho các môi trường. Bị cấm thay đổi mã nguồn ứng dụng hoặc tạo tài nguyên GCP cấp thấp.
* **Tester**: Chuyên gia kiểm thử và cổng gác chất lượng. Chịu trách nhiệm tạo các bộ kiểm thử tích hợp cho container images (smoke test, vulnerability scan), Terraform plan validation, Helm chart lint, và load test cho hạ tầng. Nếu nhiệm vụ phụ liên quan đến phạm vi tích hợp, phải xuất chính xác chuỗi ký tự `INTEGRATION_SCOPE` làm tham số đầu tiên của cặp dấu chấm phẩy.
* **Doc**: Technical Writer chính và Enterprise Systems Architect. Chuyên biên soạn tài liệu đặc tả kỹ thuật toàn diện, tài liệu tham chiếu API, bản thiết kế kiến trúc hệ thống và sơ đồ cơ sở dữ liệu. Mọi tệp tài liệu kỹ thuật được tạo ra phải được liệt kê dưới dạng thực thể đường dẫn tệp rõ ràng kết thúc bằng phần mở rộng `.md` và nằm hoàn toàn trong bố cục lưu trữ tập trung `./sources/docs/`.
* **Reviewer**: Chịu trách nhiệm xác minh compiler, cổng gác phân tích tĩnh, kiểm toán bảo mật OWASP, xác nhận tuân thủ giới hạn kích thước image, và đánh giá chất lượng tài liệu kỹ thuật.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn (DoD)

Giai đoạn 5 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: (1) Sáu multi-stage Dockerfile được tạo thành công, build image thành công, base image < 200MB, final image < 500MB theo [NFR-005], non-root user UID 1001, EXPOSE 8080, HEALTHCHECK endpoint `/q/health/live`. (2) Image được đẩy lên Google Artifact Registry tại `asia-southeast1-docker.pkg.dev/membershiphub-prod/app-images/` với tag theo `${GIT_SHA}`. (3) Terraform scripts `main.tf`, `vpc.tf`, `iam.tf`, `cloudsql.tf`, `redis.tf` được tạo đầy đủ, chạy thành công `terraform plan` và `terraform apply` không lỗi, tạo VPC với CIDR `10.0.0.0/16`, Cloud SQL PostgreSQL 15 với high availability, Memorystore Redis 2GB, GCS bucket cho static assets. (4) Helm chart `membershiphub` được tạo với Chart.yaml apiVersion v2, values.yaml định nghĩa 6 service, deployment.yaml sử dụng RollingUpdate strategy, hpa.yaml cấu hình minReplicas 2, maxReplicas 10, target CPU 70% và latency 300ms. (5) Kustomize overlay `prod/kustomization.yaml` được tạo với namespace `membershiphub-prod`, secret generator từ Google Secret Manager. (6) Bốn tài liệu kỹ thuật doanh nghiệp hoàn thiện: System Architecture Blueprint, Database Schema Topology với 11 bảng, OpenAPI Contracts, Operational Manual tiếng Việt với hướng dẫn GDPR/CCPA. (7) 100% Tag ID giai đoạn 5 ([NFR-001] đến [NFR-009], [ARC-000], [DOC-001], [DAT-ALL (1 to 11)]) được ánh xạ đầy đủ trong cấu hình hạ tầng và tài liệu.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->Khởi Tạo Tài Liệu Framework và Container Hóa Microservices<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 1.1: Khởi tạo framework tài liệu kỹ thuật doanh nghiệp
##### Sub-Agent được phân công: Doc
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/docs/architecture/SystemArchitectureBlueprint.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [ARC-006], [ARC-007], [ARC-008], [ARC-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo bốn tệp tài liệu kỹ thuật framework tại thư mục `./sources/docs/`. Tệp thứ nhất `./sources/docs/architecture/SystemArchitectureBlueprint.md` mô tả kiến trúc tổng thể hệ thống membership-hub bao gồm sơ đồ container C4 với 6 microservices Quarkus, API Gateway Spring Cloud Gateway, Frontend Next.js, các công nghệ stack (Quarkus 3.15.1, Java 21, PostgreSQL 16, Kafka 3.7.0, Redis 7.2, Firebase FCM), mô hình triển khai GKE với HPA, và bốn luồng dữ liệu chính (Authentication OAuth2 theo [ARC-006], Attendance QR theo [ARC-007], Notification đa kênh theo [ARC-008], Mobile App Integration theo [ARC-009]). Tệp thứ hai `./sources/docs/database/DatabaseSchemaTopology.md` liệt kê 11 bảng cơ sở dữ liệu với sơ đồ ER Mermaid, mô tả chi tiết từng bảng (Users, Roles, Centers, Courses, Enrollments, Attendance, StudentCards, Notifications, Promotions, Announcements, SystemSettings), các ràng buộc UNIQUE, FOREIGN KEY, CHECK constraint, và chiến lược phân vùng migration theo microservice. Tệp thứ ba `./sources/docs/api/OpenAPIContracts.md` tổng hợp OpenAPI 3.0 spec cho toàn bộ endpoint REST theo Tag ID từ [ARC-006] đến [ARC-009], bao gồm auth/register, auth/social, users/role, centers CRUD, courses CRUD, attendance/scan, students/card, reports/attendance, promotions, announcements, chatbot/message. Tệp thứ tư `./sources/docs/operations/OperationalManual.md` viết bằng tiếng Việt, mô tả quy trình vận hành hệ thống, hướng dẫn xử lý sự cố thường gặp, quy trình GDPR/CCPA data export và deletion theo [NFR-008], cấu hình audit log retention 1 năm theo [NFR-006]. Mỗi tài liệu phải có mục lục rõ ràng, sơ đồ Mermaid minh họa, và bảng Tag ID mapping tương ứng.

#### 📝 Nhiệm vụ phụ 1.2: Xây dựng multi-stage Dockerfile cho user-service
##### Sub-Agent được phân công: Docker
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/infra/docker/user-service/Dockerfile`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-005], [ARC-000]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp `Dockerfile` tại đường dẫn `./sources/infra/docker/user-service/Dockerfile` sử dụng cú pháp multi-stage build theo chuẩn doanh nghiệp. Stage build sử dụng base image `eclipse-temurin:21-jdk-alpine` với tag pin cụ thể, thực thi `WORKDIR /build`, copy toàn bộ nội dung từ `./sources/backend/user-service/` bao gồm `pom.xml` và thư mục `src/`, sau đó chạy lệnh `./mvnw package -DskipTests -Dmaven.test.skip=true` để tạo fat JAR. Stage runtime sử dụng base image `eclipse-temurin:21-jre-alpine` với tag tương ứng, copy file JAR đã build từ stage build vào `/app/app.jar`, thiết lập `EXPOSE 8080`, `USER 1001:1001` để chạy với non-root user, `HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 CMD wget --no-verbose --tries=1 --spider http://localhost:8080/q/health/live || exit 1`, `ENTRYPOINT ["java", "-jar", "/app/app.jar"]`. Đảm bảo base image < 200MB và final image < 500MB theo [NFR-005] thông qua việc sử dụng JRE alpine thay vì JDK, áp dụng layer caching cho Maven dependencies. Comment trong Dockerfile phải ghi rõ version base image, lệnh build local và lệnh push lên Artifact Registry.

#### 📝 Nhiệm vụ phụ 1.3: Xây dựng multi-stage Dockerfile cho center-service và course-service
##### Sub-Agent được phân công: Docker
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/infra/docker/center-service/Dockerfile`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-005], [ARC-000]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo hai tệp `Dockerfile` tại các đường dẫn `./sources/infra/docker/center-service/Dockerfile` và `./sources/infra/docker/course-service/Dockerfile` với cấu trúc multi-stage tương tự user-service. Mỗi Dockerfile phải đảm bảo: (1) Stage build sử dụng `eclipse-temurin:21-jdk-alpine`, copy source code từ thư mục tương ứng (`./sources/backend/center-service/` hoặc `./sources/backend/course-service/`), chạy `./mvnw package -DskipTests`. (2) Stage runtime sử dụng `eclipse-temurin:21-jre-alpine`, copy JAR từ build stage, `EXPOSE 8080`, `USER 1001:1001`, `HEALTHCHECK` trỏ tới `/q/health/live`. (3) Layer caching tối ưu bằng cách copy `pom.xml` trước, chạy `mvn dependency:go-offline` để cache dependencies, sau đó mới copy source code. (4) Kích thước base image < 200MB và final image < 500MB theo [NFR-005]. Comment trong Dockerfile phải ghi rõ Tag ID tương ứng.

#### 📝 Nhiệm vụ phụ 1.4: Xây dựng multi-stage Dockerfile cho enrollment, attendance, notification
##### Sub-Agent được phân công: Docker
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/infra/docker/enrollment-service/Dockerfile`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-005], [ARC-000]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo ba tệp `Dockerfile` tại các đường dẫn `./sources/infra/docker/enrollment-service/Dockerfile`, `./sources/infra/docker/attendance-service/Dockerfile`, `./sources/infra/docker/notification-service/Dockerfile` với cấu trúc multi-stage đồng nhất. Mỗi Dockerfile phải tuân thủ nghiêm ngặt: stage build với `eclipse-temurin:21-jdk-alpine`, copy source code từ `./sources/backend/<service-name>/`, `./mvnw package -DskipTests`, `--no-transfer-progress` để giảm log noise. Stage runtime với `eclipse-temurin:21-jre-alpine`, copy JAR, `EXPOSE 8080`, `USER 1001:1001`, `HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 CMD wget --no-verbose --tries=1 --spider http://localhost:8080/q/health/live || exit 1`. Đảm bảo 100% tuân thủ giới hạn kích thước base image < 200MB và final image < 500MB theo [NFR-005]. Tất cả image phải tương thích với Kubernetes readiness probe `/q/health/ready` và liveness probe `/q/health/live`.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->Triển Khai Hạ Tầng GCP Qua Terraform<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 2.1: Khởi tạo Terraform provider và cấu hình VPC
##### Sub-Agent được phân công: GCP
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/infra/gcp/terraform/main.tf`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-009], [ARC-000]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp `main.tf` tại đường dẫn `./sources/infra/gcp/terraform/main.tf` cấu hình Terraform provider `hashicorp/google` phiên bản `~> 5.0` với `project = "membershiphub-prod"`, `region = "asia-southeast1"`. Khai báo `terraform` block yêu cầu phiên bản `>= 1.5.0` và cấu hình `backend "gcs"` lưu trữ state tại bucket `membershiphub-tfstate` với prefix `prod/`. Bố trí cấu trúc module bằng cách include các file `vpc.tf`, `iam.tf`, `cloudsql.tf`, `redis.tf` thông qua các resource declarations trong cùng thư mục hoặc sử dụng `module` blocks tham chiếu chéo. Khai báo các `google_project_service` APIs cần enable: `compute.googleapis.com`, `sqladmin.googleapis.com`, `redis.googleapis.com`, `container.googleapis.com`, `artifactregistry.googleapis.com`, `secretmanager.googleapis.com`, `cloudbuild.googleapis.com`, `monitoring.googleapis.com`, `logging.googleapis.com`. Đảm bảo 100% hạ tầng được quản lý qua Infrastructure as Code theo [ARC-000] và hỗ trợ mục tiêu failover đa vùng theo [NFR-002].

#### 📝 Nhiệm vụ phụ 2.2: Cấu hình VPC, subnet và firewall rules
##### Sub-Agent được phân công: GCP
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/infra/gcp/terraform/vpc.tf`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp `vpc.tf` tại đường dẫn `./sources/infra/gcp/terraform/vpc.tf` định nghĩa resource `google_compute_network` với tên `membershiphub-vpc`, auto_create_subnetworks = false, routing_mode = REGIONAL. Tạo resource `google_compute_subnetwork` cho `gke-subnet` với CIDR `10.0.1.0/24`, region `asia-southeast1`, secondary IP ranges cho pods (`10.4.0.0/14`) và services (`10.8.0.0/20`), private_ip_google_access = true. Tạo resource `google_compute_subnetwork` cho `sql-subnet` với CIDR `10.0.2.0/24` dành cho Private Service Access cho Cloud SQL, enable với `private_ip_google_access = true`. Tạo resource `google_compute_router` và `google_compute_router_nat` để cung cấp outbound internet cho GKE pods không có external IP. Cấu hình firewall rules: `allow-internal` cho phép giao tiếp nội bộ giữa các subnet trong VPC, `allow-health-check` cho phép Google Cloud Health Check ranges (130.211.0.0/22, 35.191.0.0/16) truy cập GKE nodes. Tạo resource `google_compute_global_address` cho `private-services-range` với CIDR `10.10.0.0/16` phục vụ Private Service Access. Hỗ trợ mục tiêu failover đa vùng theo [NFR-002] và sao lưu dự phòng liên vùng theo [NFR-009].

#### 📝 Nhiệm vụ phụ 2.3: Cấu hình IAM service accounts và Cloud SQL
##### Sub-Agent được phân công: GCP
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/infra/gcp/terraform/iam.tf`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-003], [NFR-006], [NFR-001], [NFR-004], [NFR-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp `iam.tf` tại đường dẫn `./sources/infra/gcp/terraform/iam.tf` định nghĩa các service accounts và IAM bindings. Tạo resource `google_service_account` `gke-workload-sa` với display name "GKE Workload Identity", cấp các roles: `roles/artifactregistry.reader`, `roles/logging.logWriter`, `roles/monitoring.metricWriter`, `roles/cloudsql.client`. Tạo resource `google_service_account` `cloudsql-connector-sa` với role `roles/cloudsql.client`. Tạo resource `google_service_account` `terraform-deployer-sa` với các roles cần thiết để chạy Terraform. Sử dụng Workload Identity binding thông qua `google_service_account_iam_binding` để liên kết Kubernetes service accounts với GCP service accounts. Tạo tệp `cloudsql.tf` tại đường dẫn `./sources/infra/gcp/terraform/cloudsql.tf` cấu hình resource `google_sql_database_instance` với tên `membershiphub-postgres`, database_version `POSTGRES_15`, region `asia-southeast1`, tier `db-custom-2-8192` (2 vCPU, 8GB RAM), availability_type `REGIONAL` cho high availability zone failover, disk_size 100GB, disk_type `PD_SSD`. Bật `point_in_time_recovery` với retention 24 giờ, `backup_configuration` với `enabled = true`, `start_time = "02:00"`, `point_in_time_recovery_enabled = true`, transaction_log_retention_days 7. Tạo read replica thông qua `google_sql_database_instance` với name suffix `-replica` cho reporting workload. Cấu hình `ip_configuration` với `ipv4_enabled = false`, `private_network` trỏ tới VPC, `database_flags` chứa `cloudsql.iam_authentication = on`. Bật audit log cho tất cả truy cập thông qua `settings.database_flags` và `insights_config` theo [NFR-006].

#### 📝 Nhiệm vụ phụ 2.4: Cấu hình Memorystore Redis và GCS bucket
##### Sub-Agent được phân công: GCP
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/infra/gcp/terraform/redis.tf`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-001], [NFR-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp `redis.tf` tại đường dẫn `./sources/infra/gcp/terraform/redis.tf` cấu hình resource `google_redis_instance` với tên `membershiphub-cache`, tier `STANDARD`, memory_size_gb = 2, region `asia-southeast1`, location_id = `asia-southeast1-a`, redis_version = `REDIS_7_2`, transit_encryption_mode = `SERVER_AUTHENTICATION`, auth_enabled = true. Cấu hình `connect_mode = PRIVATE_SERVICE_ACCESS` và `network` trỏ tới VPC `membershiphub-vpc`. Tạo resource `google_redis_instance` phụ cho session storage nếu cần tách biệt cache layer và session layer. Tạo resource `google_storage_bucket` tại đường dẫn `./sources/infra/gcp/terraform/storage.tf` (hoặc bổ sung vào `main.tf`) với tên `membershiphub-static-assets`, location `ASIA-SOUTHEAST1`, uniform_bucket_level_access = true, versioning enabled, lifecycle_rule tự động chuyển object sang Nearline storage sau 30 ngày và Coldline sau 90 ngày. Cấu hình CORS policy cho phép frontend Next.js truy cập static assets thông qua `cors` block. Hỗ trợ caching layer cho enrollment dashboard theo [NFR-001] và high availability infrastructure theo [NFR-004].

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->Triển Khai GKE Cluster, Helm Charts và Tổng Hợp Tài Liệu<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 3.1: Khởi tạo Helm chart cho membership hub
##### Sub-Agent được phân công: GKE
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/infra/gke/helm/membershiphub/Chart.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-004], [ARC-000]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp `Chart.yaml` tại đường dẫn `./sources/infra/gke/helm/membershiphub/Chart.yaml` với cấu trúc Helm 3 chuẩn. Khai báo `apiVersion: v2`, `name: membershiphub`, `version: 1.0.0`, `appVersion: 1.0.0`, `type: application`, `description: Membership Hub microservices Helm chart`, `maintainers` với tên và email. Tạo tệp `values.yaml` tại đường dẫn `./sources/infra/gke/helm/membershiphub/values.yaml` định nghĩa cấu trúc tham số cho 6 service backend thông qua map `services:` chứa các key `user-service`, `center-service`, `course-service`, `enrollment-service`, `attendance-service`, `notification-service`. Mỗi service có các tham số: `replicaCount: 2`, `image.repository: asia-southeast1-docker.pkg.dev/membershiphub-prod/app-images/<service-name>`, `image.tag: latest`, `image.pullPolicy: IfNotPresent`, `resources.requests.cpu: 250m`, `resources.requests.memory: 512Mi`, `resources.limits.cpu: 1000m`, `resources.limits.memory: 1Gi`, `service.type: ClusterIP`, `service.port: 8080`. Cấu hình global `hpa.enabled: true`, `hpa.minReplicas: 2`, `hpa.maxReplicas: 10`, `hpa.targetCPUUtilizationPercentage: 70`, `hpa.targetLatencyMilliseconds: 300`. Thiết lập giá trị mặc định phù hợp với yêu cầu auto-scaling và high availability theo [NFR-002] và [NFR-004].

#### 📝 Nhiệm vụ phụ 3.2: Tạo Kubernetes deployment manifest và HPA
##### Sub-Agent được phân công: GKE
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/infra/gke/helm/membershiphub/templates/deployment.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-001], [NFR-002], [NFR-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp template `deployment.yaml` tại đường dẫn `./sources/infra/gke/helm/membershiphub/templates/deployment.yaml` sử dụng helper `{{- range $name, $service := .Values.services }}` để lặp qua 6 service trong values. Mỗi Deployment có `apiVersion: apps/v1`, `kind: Deployment`, `metadata.name: {{ $name }}`, `metadata.labels` chứa `app: {{ $name }}`, `chart: membershiphub-{{ $.Chart.Version }}`, `release: {{ $.Release.Name }}`. Cấu hình `spec.strategy.type: RollingUpdate`, `spec.strategy.rollingUpdate.maxSurge: 25%`, `spec.strategy.rollingUpdate.maxUnavailable: 0%`. Cấu hình `spec.template.spec.containers` với `image: {{ $service.image.repository }}:{{ $service.image.tag }}`, `imagePullPolicy: {{ $service.image.pullPolicy }}`, `name: {{ $name }}`, `ports: containerPort: {{ $service.service.port }}`, `livenessProbe: httpGet: path: /q/health/live, port: {{ $service.service.port }}, initialDelaySeconds: 60, periodSeconds: 30`, `readinessProbe: httpGet: path: /q/health/ready, port: {{ $service.service.port }}, initialDelaySeconds: 30, periodSeconds: 10`, `startupProbe: httpGet: path: /q/health/ready, port: {{ $service.service.port }}, failureThreshold: 30, periodSeconds: 10`, `resources: requests/limits` theo values. Tạo tệp `hpa.yaml` tại đường dẫn `./sources/infra/gke/helm/membershiphub/templates/hpa.yaml` định nghĩa HorizontalPodAutoscaler với `apiVersion: autoscaling/v2`, `kind: HorizontalPodAutoscaler`, `spec.minReplicas: 2`, `spec.maxReplicas: 10`, `spec.metrics` chứa `type: Resource, resource: name: cpu, target: type: Utilization, averageUtilization: 70` và `type: Pods, pods: metric: name: http_request_duration_seconds, target: type: AverageValue, averageValue: 300m` theo [NFR-001] và [NFR-004]. Tạo tệp `service.yaml` định nghĩa Service cho mỗi microservice với ClusterIP type.

#### 📝 Nhiệm vụ phụ 3.3: Cấu hình Kustomize overlay cho môi trường production
##### Sub-Agent được phân công: GKE
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/infra/gke/kustomize/overlays/prod/kustomization.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp `kustomization.yaml` tại đường dẫn `./sources/infra/gke/kustomize/overlays/prod/kustomization.yaml` tham chiếu tới base thông qua `resources:` chứa danh sách các manifest Kubernetes (deployment, service, hpa, configmap, secret). Cấu hình `namespace: membershiphub-prod`, `namePrefix: mh-`. Thêm `secretGenerator` tham chiếu tới file `.env.prod` (sử dụng `envs:` directive) chứa DB credentials từ Google Secret Manager, JWT secret, OAuth2 client secrets, Zalo OA access token, OpenAI API key. Cấu hình `configMapGenerator` cho application properties của từng service. Cấu hình `commonLabels` với `environment: production`, `managed-by: kustomize`, `app.kubernetes.io/part-of: membershiphub`. Bổ sung `patchesStrategicMerge` hoặc `patches` để ghi đè replicaCount, image tag cho môi trường production. Đảm bảo cấu hình hỗ trợ rollback nhanh khi có sự cố theo [NFR-002] và sao lưu dự phòng liên vùng theo [NFR-009]. Tạo thêm tệp `namespace.yaml` định nghĩa namespace `membershiphub-prod` với labels chuẩn Kubernetes recommended labels.

#### 📝 Nhiệm vụ phụ 3.4: Kiểm thử tích hợp cho container images và hạ tầng
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/infra/test/devops-infrastructure-test.sh
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-005], [NFR-002], [NFR-004], [NFR-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp script shell `./sources/infra/test/devops-infrastructure-test.sh` thực thi chuỗi kiểm thử tích hợp cho toàn bộ hạ tầng. Test case 1 (`docker-image-size-validation`): Chạy `docker images` sau khi build 6 service, parse output để xác nhận base image < 200MB và final image < 500MB theo [NFR-005]. Test case 2 (`dockerfile-security-scan`): Chạy `trivy image` quét lỗ hổng bảo mật trên image đã build, kỳ vọng không có CVE mức CRITICAL hoặc HIGH. Test case 3 (`dockerfile-non-root-check`): Chạy `docker inspect` kiểm tra User được thiết lập UID 1001, không phải root. Test case 4 (`healthcheck-validation`): Chạy container trong background, sử dụng `docker exec` gọi `wget --spider http://localhost:8080/q/health/live`, kỳ vọng HTTP 200. Test case 5 (`terraform-plan-validation`): Chạy `terraform init && terraform plan -out=plan.tfplan` trong thư mục `./sources/infra/gcp/terraform/`, kỳ vọng exit code 0, plan tạo đầy đủ VPC, Cloud SQL, Redis, GCS, IAM. Test case 6 (`helm-lint-validation`): Chạy `helm lint ./sources/infra/gke/helm/membershiphub/`, kỳ vọng không có lỗi syntax hoặc template rendering. Test case 7 (`kustomize-build-validation`): Chạy `kustomize build ./sources/infra/gke/kustomize/overlays/prod/`, kỳ vọng sinh ra manifest YAML hợp lệ. Test case 8 (`manifest-security-check`): Sử dụng `kube-score` hoặc `polaris` đánh giá security score, kỳ vọng không có critical issues. Gắn Tag ID trong comment script header.

#### 📝 Nhiệm vụ phụ 3.5: Đánh giá cuối cùng và xác nhận hoàn thành giai đoạn 5
##### Sub-Agent được phân công: Reviewer
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/docs/review/phase5-final-review.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-008], [NFR-009], [ARC-000], [DOC-001]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Reviewer thực hiện đánh giá tổng thể cuối cùng cho toàn bộ Giai đoạn 5. Kiểm tra cross-cutting: (1) Tất cả 9 Tag ID phi chức năng từ [NFR-001] đến [NFR-009] đã được ánh xạ đầy đủ trong cấu hình container, Terraform, Helm chart, Kustomize overlay. (2) Tất cả 6 Dockerfile đảm bảo base image < 200MB, final image < 500MB, non-root user, healthcheck endpoint. (3) Terraform scripts bao gồm đầy đủ VPC, IAM, Cloud SQL với high availability và PITR, Memorystore Redis, GCS bucket. (4) Helm chart cấu hình HPA với minReplicas 2, maxReplicas 10, target CPU 70% và latency 300ms. (5) Kustomize overlay production sử dụng namespace riêng, secret generator từ Google Secret Manager. (6) Bốn tài liệu kỹ thuật doanh nghiệp (System Architecture Blueprint, Database Schema Topology, OpenAPI Contracts, Operational Manual) hoàn thiện, bằng tiếng Việt, có mục lục, sơ đồ Mermaid, bảng Tag ID mapping. (7) Operational Manual mô tả quy trình GDPR/CCPA data export và deletion theo [NFR-008], audit log retention 1 năm theo [NFR-006]. Tạo báo cáo tổng hợp tại đường dẫn `./sources/docs/review/phase5-final-review.md` liệt kê coverage matrix giữa Tag ID và file cấu hình, các issue còn tồn đọng (nếu có), và xác nhận định nghĩa hoàn thành giai đoạn. Gắn Tag ID đầy đủ trong báo cáo.