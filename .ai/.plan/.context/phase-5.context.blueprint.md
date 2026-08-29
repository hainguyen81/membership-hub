# Giai đoạn 5: <!--PHASE_NAME_START-->Bảo Mật Tích Hợp, Vận Hành DevOps Và Giám Sát Đa Vùng<!--PHASE_NAME_END-->

## 📊 Kiểm Soát Tài Liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Bản Thiết Kế** | ARCH-20260829125322 |
| **Tên Dự Án** | membership-hub |
| **Giai đoạn** | 5 |
| **Tên Giai Đoạn** | <!--PHASE_NAME_START-->Bảo Mật Tích Hợp, Vận Hành DevOps Và Giám Sát Đa Vùng<!--PHASE_NAME_END--> |
| **Mô Tả** | <!--PHASE_DESC_START-->Giai đoạn 5 củng cố bức tường bảo mật toàn hệ thống theo chuẩn OWASP Top 10, hoàn thiện khả năng mã hóa dữ liệu TLS 1.3 và AES-256, đóng gói ứng dụng qua Docker đa giai đoạn với kích thước ảnh dưới 500MB, tự động hóa cung cấp hạ tầng GCP bằng Terraform (VPC, IAM, Cloud SQL PostgreSQL), triển khai điều phối GKE với HPA tự co giãn theo CPU > 70% hoặc độ trễ > 300ms kèm failover đa vùng, đồng thời cấu hình giám sát Stackdriver, lưu giữ log kiểm toán 1 năm, sao lưu point-in-time 24 giờ và soạn thảo sổ tay khôi phục sau thảm họa đảm bảo SLA 99,9% cùng tuân thủ NFR-001 đến NFR-009.<!--PHASE_DESC_END--> |
| **Phiên Bản** | 1.0 (Đường Cơ Sở) |
| **Thời Gian** | 2026/08/29 12:53:22 |
| **Tác Giả** | Kiến Trúc Sư Hệ Thống Doanh Nghiệp (SA Agent) |
| **Phê Duyệt** | Đang Chờ Đánh Giá Quản Trị Kỹ Thuật |

## 1. Phạm Vi Hoạt Động & Mục Tiêu Giai Đoạn

Giai đoạn 5 thực hiện năm nhiệm vụ cốt lõi được phân bổ theo bảng tóm tắt đa giai đoạn: Nhiệm vụ 17 (bảo mật tích hợp và tuân thủ chuẩn doanh nghiệp), Nhiệm vụ 20 (container hóa Docker đa giai đoạn và đẩy Registry), Nhiệm vụ 21 (cung cấp hạ tầng GCP và Terraform), Nhiệm vụ 22 (điều phối cụm GKE và HPA), Nhiệm vụ 23 (giám sát, ghi log và kiểm toán bảo mật). Phạm vi kéo dài từ Ngày 1 đến Ngày 3 theo phân bổ chính thức trong ma trận tóm tắt đa giai đoạn.

Các tài sản kỹ thuật bắt buộc phải sinh ra bao gồm: tài liệu blueprint bảo mật tích hợp và tuân thủ `./sources/docs/security-compliance-blueprint.md` mô tả chi tiết biện pháp phòng chống SQLi, XSS, CSRF theo OWASP Top 10 kèm chính sách mã hóa TLS 1.3, AES-256, tuân thủ GDPR/CCPA; sổ tay khôi phục sau thảm họa `./sources/docs/disaster-recovery-runbook.md` với chỉ số RTO 30 phút và RPO 5 phút; sáu Dockerfile đa giai đoạn cho sáu vi dịch vụ tại `./sources/infra/docker/` đảm bảo kích thước ảnh cuối dưới 500MB theo NFR-005; bốn module Terraform tại `./sources/infra/terraform/` cung cấp VPC, IAM least-privilege, Cloud SQL PostgreSQL 15 HA với mã hóa CMEK và point-in-time recovery; bốn manifest Kubernetes tại `./sources/infra/gke/` cho Deployment, HPA, Ingress và Kustomization hỗ trợ failover đa vùng; hai tệp giám sát tại `./sources/infra/monitoring/` cấu hình Stackdriver alerts và chính sách lưu giữ log 365 ngày. Toàn bộ tài sản phải được gắn thẻ truy xuất theo hệ thống TagID `[NFR-001]`, `[NFR-002]`, `[NFR-003]`, `[NFR-004]`, `[NFR-005]`, `[NFR-006]`, `[NFR-007]`, `[NFR-008]`, `[NFR-009]`, `[ARC-001]`, `[ARC-002]`, `[ARC-003]`, `[ARC-004]`, `[ARC-005]` đảm bảo khả năng truy vết đầy đủ.

## 2. Phạm Vi Kỹ Thuật Cho Phép & Ranh Giới Thư Mục

Danh sách tệp vật lý và tài sản được phép sinh ra trong giai đoạn này:

* `./sources/docs/security-compliance-blueprint.md` [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-003], [NFR-008]
* `./sources/docs/disaster-recovery-runbook.md` [NFR-009], [NFR-006]
* `./sources/infra/docker/user-service.Dockerfile` [NFR-005]
* `./sources/infra/docker/center-service.Dockerfile` [NFR-005]
* `./sources/infra/docker/course-service.Dockerfile` [NFR-005]
* `./sources/infra/docker/attendance-service.Dockerfile` [NFR-005]
* `./sources/infra/docker/notification-service.Dockerfile` [NFR-005]
* `./sources/infra/docker/reporting-service.Dockerfile` [NFR-005]
* `./sources/infra/terraform/main.tf` [NFR-002], [NFR-004]
* `./sources/infra/terraform/vpc.tf` [NFR-002]
* `./sources/infra/terraform/iam.tf` [NFR-003]
* `./sources/infra/terraform/cloudsql.tf` [NFR-001], [NFR-008]
* `./sources/infra/gke/deployment.yaml` [NFR-002], [NFR-004]
* `./sources/infra/gke/hpa.yaml` [NFR-004]
* `./sources/infra/gke/ingress.yaml` [NFR-002]
* `./sources/infra/gke/kustomization.yaml` [NFR-004]
* `./sources/infra/monitoring/stackdriver-alerts.yaml` [NFR-002], [NFR-006]
* `./sources/infra/monitoring/log-retention.tf` [NFR-006]

* **RÀNG BUỘC BẮT BUỘC VỀ TÀI SẢN CƠ SỞ**:
  - Cấu trúc Dockerfile phải tuân thủ nguyên tắc đa giai đoạn với stage build sử dụng JDK và stage runtime sử dụng JRE Alpine nhằm giữ kích thước ảnh cuối dưới 500MB theo NFR-005.
  - Tất cả module Terraform phải khai báo provider `google` với region `asia-southeast1` và bật đầy đủ Google APIs cần thiết thông qua `google_project_service`.
  - Manifest GKE phải sử dụng HPA với ngưỡng CPU > 70% hoặc độ trễ yêu cầu > 300ms theo NFR-004, tích hợp readiness probe `/health/ready` và liveness probe `/health/live`.
  - Tài liệu blueprint phải mô tả chi tiết biện pháp phòng chống OWASP Top 10, chính sách mã hóa TLS 1.3, AES-256, và quy trình tuân thủ GDPR/CCPA theo NFR-003, NFR-008.

## 3. Chỉ Thị Chức Năng Chuyên Biệt Cho Tác Nhân Phụ

*   **Coder**: Đóng vai trò Nhà Phát Triển Ứng Dụng Cao Cấp. Chịu trách nhiệm triển khai mã nguồn ứng dụng thuần túy. Bị cấm viết bộ kiểm thử, biểu mẫu Docker, hoặc kịch bản Terraform.

* **Tester**: Đóng vai trò Trưởng Nhóm Kiểm Thử/Đảm Bảo Chất Lượng. Chuyên về kỹ thuật bộ kiểm thử, xác thực và cổng chất lượng. Chịu trách nhiệm sinh script kiểm thử tích hợp build, xác minh kích thước ảnh Docker, đo lường hiệu năng hạ tầng, và xác thực quy trình rollback. Bị cấm sửa đổi mã nguồn sản phẩm hoặc biểu mẫu hạ tầng. Nếu nhiệm vụ phụ liên quan đến phạm vi tích hợp tổng thể mà không thể khoanh vùng một tệp cụ thể, phải xuất chính xác chuỗi ký tự `INTEGRATION_SCOPE` làm tham số đầu tiên của cặp phân tách bằng dấu chấm phẩy.

* **Doc**: Đóng vai trò Chuyên Viên Viết Tài Liệu Kỹ Thuật và Kiến Trúc Sư Hệ Thống Doanh Nghiệp. Chuyên về biên soạn tài liệu đặc tả kỹ thuật toàn diện, blueprint bảo mật, sổ tay vận hành, sơ đồ Mermaid mô tả luồng và quy trình khôi phục. Mỗi tệp tài liệu kỹ thuật được sinh ra phải được liệt kê dưới dạng thực thể đường dẫn tệp rõ ràng kết thúc bằng phần mở rộng `.md` và nằm hoàn toàn trong sơ đồ lưu trữ tập trung: `./sources/docs/`.

*   **Reviewer**: Chịu trách nhiệm xác minh trình biên dịch, cổng phân tích tĩnh và vá lỗi phòng thủ. Chuyên về đánh giá chất lượng tài liệu, giải quyết lỗ hổng bảo mật OWASP, xử lý các blocker cổng chất lượng và xác nhận tuân thủ NFR.

*   **Docker**: Chuyên biệt về container hóa, kỹ thuật Dockerfile đa giai đoạn, tối ưu gói và đẩy tài sản ảnh ứng dụng đã xác minh lên Artifact Registry.

*   **GCP**: Chuyên về tự động hóa đám mây trong Google Cloud Platform. Chịu trách nhiệm xây dựng kịch bản Terraform cung cấp VPC, IAM, Cloud SQL, cấu hình mã hóa at-rest, và tích hợp Secret Manager.

*   **GKE**: Chuyên về điều phối container sản xuất bên trong Google Kubernetes Engine. Chịu trách nhiệm xây dựng manifest triển khai Kubernetes, điều khiển định tuyến, cấu hình HPA, biểu đồ Helm và triển khai tải công việc microservice vào cụm GKE đang hoạt động.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn

Giai đoạn 5 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: một trăm phần trăm danh mục kiểm soát OWASP Top 10 đã có biện pháp giảm thiểu rõ ràng ứng với từng mã kiến trúc ARC-001 đến ARC-005 trong tài liệu blueprint; chính sách mã hóa TLS 1.3 cho đường truyền và AES-256 cho dữ liệu lưu trữ được đặc tả đầy đủ kèm chu kỳ xoay vòng khóa 90 ngày; quy trình GDPR/CCPA mô tả chi tiết endpoint xóa dữ liệu, xuất JSON và quản lý consent marketing; sáu Dockerfile đa giai đoạn được xác minh biên dịch thành công với kích thước ảnh cuối dưới 500MB theo NFR-005; bốn module Terraform `main.tf`, `vpc.tf`, `iam.tf`, `cloudsql.tf` cung cấp hạ tầng GCP đầy đủ với Cloud SQL PostgreSQL 15 HA, mã hóa CMEK, point-in-time recovery 24 giờ theo NFR-009; bốn manifest GKE triển khai Deployment, HPA với ngưỡng CPU > 70% hoặc độ trễ > 300ms, Ingress TLS 1.3 và Kustomization hỗ trợ overlay staging/production; Stackdriver alerts cấu hình đầy đủ cho uptime check 99,9% SLA, lỗi 5xx > 1%, độ trễ p95 > 400ms với log retention 365 ngày theo NFR-006; sổ tay khôi phục sau thảm họa mô tả chi tiết quy trình failover đa vùng với RTO 30 phút và RPO 5 phút; script kiểm thử tích hợp xác minh build Docker và triển khai GKE hoạt động đúng kỳ vọng; một trăm phần trăm mã TagID được phân bổ cho giai đoạn 5 phải được ánh xạ chính xác trong báo cáo đánh giá cuối giai đoạn.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->THIẾT LẬP BẢO MẬT OWASP VÀ ĐẶC TẢ TUÂN THỦ GDPR/CCPA<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 1.1: Soạn thảo blueprint bảo mật và tuân thủ
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/security-compliance-blueprint.md

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-003], [NFR-008]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tài liệu blueprint bảo mật cấp doanh nghiệp tại `./sources/docs/security-compliance-blueprint.md`. Tài liệu phải mô tả chi tiết chiến lược phòng chống SQL Injection thông qua `PreparedStatement` và JPA parameterized query cho toàn bộ vi dịch vụ Quarkus; biện pháp chống XSS thông qua `DOMPurify` cho dữ liệu đầu vào từ biểu mẫu web, sử dụng `dangerouslySetInnerHTML` kết hợp pipeline làm sạch; cơ chế bảo vệ CSRF bằng double submit cookie token cho mọi endpoint thay đổi trạng thái. Đặc tả chính sách mã hóa TLS 1.3 cho mọi kênh truyền dữ liệu, mã hóa at-rest AES-256 cho Cloud SQL PostgreSQL và Cloud Storage, chu kỳ xoay vòng khóa 90 ngày. Tích hợp bộ tiêu chuẩn OWASP Top 10 vào checklist CI/CD cho từng mã ARC-001 đến ARC-005. Bổ sung phần tuân thủ GDPR/CCPA mô tả quy trình xóa dữ liệu theo yêu cầu chủ thể, xuất JSON dữ liệu cá nhân, cơ chế thu hồi consent marketing, lưu giữ nhật ký đồng ý 1 năm. Đảm bảo tài liệu chứa sơ đồ Mermaid mô tả luồng phát hiện và phản ứng sự cố bảo mật, ma trận phân loại dữ liệu nhạy cảm, và danh sách kiểm tra kiểm toán bảo mật cuối giai đoạn.

#### 📝 NHIỆM VỤ PHỤ 1.2: Kiểm tra đối chiếu blueprint bảo mật
##### Tác Nhân Được Phân Công: Reviewer
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/security-compliance-blueprint.md

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-003], [NFR-008]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Thực hiện rà soát toàn diện tài liệu blueprint bảo mật tại `./sources/docs/security-compliance-blueprint.md`. Xác nhận 100% danh mục kiểm soát OWASP Top 10 đã có biện pháp giảm thiểu rõ ràng ứng với từng mã kiến trúc ARC-001 đến ARC-005. Xác nhận cấu hình TLS 1.3, AES-256 và chính sách khóa đã phù hợp với NFR-003. Đánh giá tính đầy đủ của quy trình GDPR/CCPA theo NFR-008 bao gồm endpoint xóa dữ liệu, xuất JSON và quản lý consent. Phát hành nhận xét và danh sách điều chỉnh nếu phát hiện khoảng trống. Đảm bảo sơ đồ Mermaid phản ứng sự cố phải mô tả rõ ràng các bước phát hiện, phân loại mức độ nghiêm trọng, kênh thông báo và thời gian phản ứng tối đa.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->CONTAINER HÓA DOCKER ĐA GIAI ĐOẠN VÀ ĐẨY ARTIFACT REGISTRY<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 2.1: Xây dựng Dockerfile đa giai đoạn cho user-service
##### Tác Nhân Được Phân Công: Docker
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/infra/docker/user-service.Dockerfile

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-005]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo Dockerfile đa giai đoạn cho `user-service` tại `./sources/infra/docker/user-service.Dockerfile`. Stage build sử dụng `eclipse-temurin:21-jdk-jammy` chạy Maven wrapper với cache layer; stage runtime sử dụng `eclipse-temurin:21-jre-jammy` Alpine để giữ kích thước ảnh cuối dưới 500MB theo NFR-005. Tích hợp non-root user UID 1001, healthcheck gọi `/health/live`, label OCI đầy đủ (org.opencontainers.image.title, org.opencontainers.image.version, org.opencontainers.image.source) và ARG cho phiên bản JDK. Thực thi kiểm thử cục bộ `docker build` đảm bảo biên dịch sạch, sau đó đẩy image lên Artifact Registry region `asia-southeast1` với tag `user-service:1.0.0-rc.1`.

#### 📝 NHIỆM VỤ PHỤ 2.2: Xây dựng Dockerfile cho center-service
##### Tác Nhân Được Phân Công: Docker
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/infra/docker/center-service.Dockerfile

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-005]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo Dockerfile đa giai đoạn cho `center-service` tại `./sources/infra/docker/center-service.Dockerfile` đối chiếu kích thước ảnh cuối dưới 500MB theo NFR-005. Đảm bảo stage build sử dụng Maven với cache layer, stage runtime sử dụng JRE Alpine, thêm probe `/health/ready` và cấu hình ENTRYPOINT chạy ứng dụng Quarkus với JVM ergonomics (`-XX:MaxRAMPercentage=75.0`, `-XX:+UseG1GC`). Đẩy image lên Artifact Registry với tag `center-service:1.0.0-rc.1`.

#### 📝 NHIỆM VỤ PHỤ 2.3: Xây dựng Dockerfile cho course-service
##### Tác Nhân Được Phân Công: Docker
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/infra/docker/course-service.Dockerfile

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-005]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo Dockerfile đa giai đoạn cho `course-service` tại `./sources/infra/docker/course-service.Dockerfile` đảm bảo tổng kích thước ảnh cuối dưới 500MB theo NFR-005. Bổ sung ARG cho cấu hình Quarkus profile (`quarkus.profile=prod`), ENV cho JWT issuer (`JWT_ISSUER`), tích hợp OpenTelemetry agent nhằm phục vụ giám sát. Sử dụng multi-stage với layer copy riêng cho dependencies Maven để tận dụng cache Docker. Đẩy image lên Artifact Registry với tag `course-service:1.0.0-rc.1`.

#### 📝 NHIỆM VỤ PHỤ 2.4: Xây dựng Dockerfile cho attendance-service
##### Tác Nhân Được Phân Công: Docker
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/infra/docker/attendance-service.Dockerfile

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-005]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo Dockerfile đa giai đoạn cho `attendance-service` tại `./sources/infra/docker/attendance-service.Dockerfile` đảm bảo kích thước ảnh cuối dưới 500MB theo NFR-005. Tối ưu build với `--no-transfer-progress`, copy chỉ artifact JAR sau giai đoạn package, expose cổng 8080 và cấu hình healthcheck gọi `/health/ready`. Tích hợp ENV cho Kafka bootstrap servers và database connection string. Đẩy image lên Artifact Registry với tag `attendance-service:1.0.0-rc.1`.

#### 📝 NHIỆM VỤ PHỤ 2.5: Xây dựng Dockerfile cho notification-service và reporting-service
##### Tác Nhân Được Phân Công: Docker
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/infra/docker/notification-service.Dockerfile

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-005]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo Dockerfile đa giai đoạn cho `notification-service` tại `./sources/infra/docker/notification-service.Dockerfile` đảm bảo kích thước ảnh cuối dưới 500MB theo NFR-005, đồng thời tạo Dockerfile tương ứng cho `reporting-service` tại `./sources/infra/docker/reporting-service.Dockerfile` trong cùng nhiệm vụ phụ. Tích hợp biến môi trường cho cấu hình FCM/APNs/Zalo (`FCM_SERVER_KEY`, `APNS_KEY_ID`, `ZALO_OA_ACCESS_TOKEN`), giữ ảnh runtime ở JRE Alpine, đẩy cả hai image lên Artifact Registry với tag semantic `notification-service:1.0.0-rc.1` và `reporting-service:1.0.0-rc.1`.

#### 📝 NHIỆM VỤ PHỤ 2.6: Kiểm thử tích hợp quy trình build Docker đa dịch vụ
##### Tác Nhân Được Phân Công: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/infra/test/maven-build-integration.sh

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-005]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo script tại `./sources/infra/test/maven-build-integration.sh` thực thi `docker build` cho toàn bộ sáu Dockerfile, kiểm tra exit code, đo dung lượng ảnh tạo ra và xác nhận mỗi ảnh đều có kích thước dưới 500MB theo NFR-005. Script phải dừng ngay khi phát hiện ảnh vượt ngưỡng, đồng thời tự động đẩy kết quả vào pipeline log để phục vụ audit. Bổ sung bước `docker inspect` xác nhận label OCI, non-root user và cấu hình healthcheck tồn tại.

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->CUNG CẤP HẠ TẦNG GCP, ĐIỀU PHỐI GKE VÀ GIÁM SÁT STACKDRIVER<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 3.1: Kịch bản Terraform cho VPC, IAM và cơ sở dữ liệu
##### Tác Nhân Được Phân Công: GCP
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/infra/terraform/main.tf

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-003], [NFR-004], [NFR-008]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Khởi tạo module Terraform `main.tf` tại `./sources/infra/terraform/main.tf` liên kết các module con `vpc.tf`, `iam.tf`, `cloudsql.tf` để cung cấp hạ tầng GCP. Bật API `compute.googleapis.com`, `sqladmin.googleapis.com`, `container.googleapis.com`, `iam.googleapis.com`, `monitoring.googleapis.com` thông qua `google_project_service`. Định nghĩa provider `google` với region `asia-southeast1` và biến đầu vào cho project ID, môi trường, và tags chi phí. Tài liệu phải thể hiện rõ chiến lược multi-AZ, gắn nhãn tài nguyên tuân thủ chính sách FinOps và ghi chú hỗ trợ failover NFR-002.

#### 📝 NHIỆM VỤ PHỤ 3.2: Định nghĩa VPC và IAM an toàn
##### Tác Nhân Được Phân Công: GCP
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/infra/terraform/vpc.tf

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-003]<!--END_TAGS-->

* **Hng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo module `vpc.tf` tại `./sources/infra/terraform/vpc.tf` cung cấp mạng VPC chế độ tùy chỉnh với dải CIDR `10.20.0.0/16`, subnet cho GKE và Cloud SQL trên hai zone, firewall cho phép truy cập nội bộ giữa các vi dịch vụ và chặn SSH từ internet theo NFR-002. Bổ sung `google_compute_router` và `google_compute_nat` để cấp Internet egress cho node GKE. Định nghĩa Cloud NAT cho phép egress ổn định, đồng thời giới hạn truy cập API từ internet thông qua Private Google Access.

#### 📝 NHIỆM VỤ PHỤ 3.3: Cấu hình IAM tối thiểu theo nguyên tắc đặc quyền tối thiểu
##### Tác Nhân Được Phân Công: GCP
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/infra/terraform/iam.tf

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-003], [NFR-008]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo module `iam.tf` tại `./sources/infra/terraform/iam.tf` cấp service account riêng cho mỗi vi dịch vụ với quyền hạn chế theo nguyên tắc least privilege, role `roles/cloudsql.client` cho backend, `roles/artifactregistry.reader` cho pull image, `roles/monitoring.metricWriter` cho xuất chỉ số. Bổ sung IAM binding cho phép Cloud Build triển khai lên GKE và kích hoạt Workload Identity theo NFR-003, NFR-008.

#### 📝 NHIỆM VỤ PHỤ 3.4: Khởi tạo Cloud SQL PostgreSQL với mã hóa và sao lưu
##### Tác Nhân Được Phân Công: GCP
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/infra/terraform/cloudsql.tf

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-001], [NFR-008], [NFR-009]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo module `cloudsql.tf` tại `./sources/infra/terraform/cloudsql.tf` cung cấp Cloud SQL PostgreSQL 15 HA với cờ `database_version = "POSTGRES_15"`, bật mã hóa at-rest bằng CMEK, cấu hình sao lưu tự động hằng ngày, point-in-time recovery lên tới 24 giờ, kích hoạt query insights và read replica cho workload báo cáo theo NFR-001, NFR-008, NFR-009. Định nghĩa `google_sql_user` cho từng vi dịch vụ, cấu hình `private_network` trỏ tới VPC đã tạo.

#### 📝 NHIỆM VỤ PHỤ 3.5: Triển khai GKE Deployment, HPA và Ingress
##### Tác Nhân Được Phân Công: GKE
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/infra/gke/deployment.yaml

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-004]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo manifest `deployment.yaml` tại `./sources/infra/gke/deployment.yaml` cho cụm GKE Autopilot với ba replica khởi tạo cho mỗi vi dịch vụ, sử dụng image từ Artifact Registry, gắn service account riêng và secret cho kết nối Cloud SQL. Cấu hình readiness probe `/health/ready` và liveness probe `/health/live`, thiết lập resource request/limit theo ngưỡng NFR-001, đồng thời gắn nhãn topology cho Kustomize.

#### 📝 NHIỆM VỤ PHỤ 3.6: Cấu hình HPA, Ingress và Kustomization
##### Tác Nhân Được Phân Công: GKE
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/infra/gke/hpa.yaml

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-004]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo manifest `hpa.yaml` tại `./sources/infra/gke/hpa.yaml` định nghĩa HorizontalPodAutoscaler với ngưỡng CPU > 70% hoặc độ trễ yêu cầu > 300ms, tối thiểu 3 pod và tối đa 20 pod theo NFR-004. Tạo `ingress.yaml` tại `./sources/infra/gke/ingress.yaml` cho Cloud Load Balancer với TLS 1.3 và IP tĩnh. Tạo `kustomization.yaml` tại `./sources/infra/gke/kustomization.yaml` liệt kê toàn bộ deployment, service, HPA, ingress và secret, hỗ trợ overlay staging/production.

#### 📝 NHIỆM VỤ PHỤ 3.7: Cấu hình giám sát Stackdriver và chính sách lưu giữ log
##### Tác Nhân Được Phân Công: GCP
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/infra/monitoring/stackdriver-alerts.yaml

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-006]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo manifest `stackdriver-alerts.yaml` tại `./sources/infra/monitoring/stackdriver-alerts.yaml` định nghĩa cảnh báo uptime check 99,9% SLA, cảnh báo lỗi 5xx > 1% trong 5 phút, cảnh báo độ trễ p95 > 400ms theo NFR-002. Cấu hình log-based metric cho audit log, đồng thời tạo `log-retention.tf` tại `./sources/infra/monitoring/log-retention.tf` thiết lập Log Sink lưu trữ 365 ngày theo NFR-006. Tích hợp notification channel gửi email và Slack khi vi phạm SLA.

#### 📝 NHIỆM VỤ PHỤ 3.8: Kiểm thử tích hợp hạ tầng GKE và giám sát
##### Tác Nhân Được Phân Công: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/infra/test/maven-build-integration.sh

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-004], [NFR-006]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo script tích hợp mở rộng tại `./sources/infra/test/maven-build-integration.sh` để thực thi `kubectl apply -k` với Kustomize overlay staging, đợi rollout hoàn tất và xác nhận HPA đã đọc metric. Script phải kích hoạt tải giả lập nhằm xác nhận ngưỡng CPU > 70% kích hoạt scale-out theo NFR-004, đồng thời xác minh Stackdriver đã thu thập log cấu hình kiểm toán theo NFR-006. Báo cáo kết quả vào pipeline log.

#### 📝 NHIỆM VỤ PHỤ 3.9: Soạn thảo sổ tay khôi phục sau thảm họa
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/disaster-recovery-runbook.md

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-006], [NFR-009]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo sổ tay vận hành `disaster-recovery-runbook.md` tại `./sources/docs/disaster-recovery-runbook.md` mô tả quy trình failover đa vùng giữa `asia-southeast1` và `asia-east1`, danh sách kiểm tra RTO 30 phút và RPO 5 phút theo NFR-009. Tài liệu phải bao gồm kịch bản mất Cloud SQL, mất cluster GKE, mất Artifact Registry kèm câu lệnh Terraform/Pulumi khôi phục, quy trình xác minh toàn vẹn dữ liệu, và danh sách liên lạc khẩn cấp. Đồng thời mô tả quy trình trích xuất audit log 1 năm từ Stackdriver theo NFR-006.

#### 📝 NHIỆM VỤ PHỤ 3.10: Đánh giá sổ tay khôi phục và chiến lược failover
##### Tác Nhân Được Phân Công: Reviewer
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/disaster-recovery-runbook.md

* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-006], [NFR-009]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Rà soát sổ tay `disaster-recovery-runbook.md` tại `./sources/docs/disaster-recovery-runbook.md`, xác nhận các chỉ số RTO/RPO đáp ứng NFR-009, danh sách kiểm tra đầy đủ cho cả ba kịch bản mất dịch vụ (mất Cloud SQL, mất cluster GKE, mất Artifact Registry), đồng thời đảm bảo cơ chế xuất audit log 1 năm theo NFR-006 đã rõ ràng. Đưa ra nhận xét và yêu cầu chỉnh sửa nếu phát hiện khoảng trống trong quy trình failover hoặc thiếu bước xác minh toàn vẹn dữ liệu sau khôi phục.