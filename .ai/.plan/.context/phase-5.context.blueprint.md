# Giai đoạn 5: <!--PHASE_NAME_START-->Triển khai Hạ tầng DevOps, Bảo mật Toàn hệ thống và Tài liệu Doanh nghiệp<!--PHASE_NAME_END-->

## 📊 Bảng kiểm soát tài liệu

| Mục | Chi tiết |
| :--- | :--- |
| **ID Bản thiết kế** | ARCH-20260818163158 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 5 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Triển khai Hạ tầng DevOps, Bảo mật Toàn hệ thống và Tài liệu Doanh nghiệp<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn này triển khai lớp kiểm soát truy cập dựa trên vai trò (RBAC) toàn hệ thống đảm bảo tuân thủ OWASP Top 10, xây dựng toàn bộ hạ tầng DevOps (Docker đa giai đoạn, Terraform provisioning tài nguyên GCP, GKE orchestration với HPA tự động scale, pipeline CI/CD GitHub Actions), và hoàn thiện toàn bộ tài liệu doanh nghiệp (bản vẽ kiến trúc, tài liệu tham chiếu API, hướng dẫn vận hành) để đáp ứng tất cả các yêu cầu phi chức năng về hiệu suất, khả năng sẵn sàng, bảo mật và khả năng mở rộng.<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày.Giờ** | 2026/08/18 16:31:58 |
| **Tác giả** | Kiến trúc sư hệ thống doanh nghiệp (Đặc vụ SA) |
| **Phê duyệt** | Đang chờ xem xét quản trị kỹ thuật |

## 1. Phạm vi hoạt động và mục tiêu của giai đoạn
Giai đoạn 5 là giai đoạn hoàn thiện cuối cùng của dự án, tập trung vào việc đóng gói hệ thống thành sản phẩm sản xuất ổn định và bảo mật. Giai đoạn này sẽ triển khai lớp bảo mật RBAC toàn hệ thống dưới dạng bộ lọc JAX-RS toàn cục, đảm bảo phân quyền chặt chẽ theo 5 vai trò người dùng và cách ly quyền theo trung tâm. Tiếp theo, giai đoạn sẽ xây dựng toàn bộ hạ tầng DevOps bao gồm Dockerfile đa giai đoạn tối ưu kích thước, Terraform modules để provisioning GCP (VPC, Cloud SQL, Redis, IAM), Kubernetes manifests với HPA tự động scale và rolling update strategy, cùng pipeline CI/CD tự động với GitHub Actions. Cuối cùng, giai đoạn sẽ hoàn thiện toàn bộ tài liệu doanh nghiệp: bản vẽ kiến trúc hệ thống, tài liệu tham chiếu API REST, hướng dẫn cài đặt và vận hành, hướng dẫn sao lưu và phục hồi thảm họa. Tất cả thành phần phải tuân thủ các yêu cầu phi chức năng về hiệu suất (độ trễ API <200ms), khả năng sẵn sàng (99.9% uptime), bảo mật (TLS 1.3, mã hóa AES-256, OWASP Top 10), khả năng mở rộng ngang, ghi log audit lưu trữ 1 năm, đa ngôn ngữ, và tuân thủ GDPR/CCPA.

## 2. Phạm vi kỹ thuật được phép và ranh giới thư mục
- **Thư mục backend bảo mật:** `./sources/backend/auth/src/main/java/org/nlh4j/membership_hub/security/`, `./sources/backend/auth/src/test/java/org/nlh4j/membership_hub/`
- **Thư mục hạ tầng:** `./sources/infra/terraform/`, `./sources/infra/docker/`, `./sources/infra/gke/`, `./sources/infra/.github/workflows/`, `./sources/infra/test/`
- **Thư mục tài liệu:** `./sources/docs/architecture/`, `./sources/docs/api/`, `./sources/docs/operations/`
- **Endpoint và cấu hình hạ tầng được phép triển khai:**
  - RBAC Middleware (JAX-RS ContainerRequestFilter) cho tất cả endpoint backend
  - Dockerfile đa giai đoạn cho tất cả dịch vụ backend và frontend
  - Terraform modules cho VPC, Cloud SQL (PostgreSQL), Redis, IAM, Cloud Storage
  - Kubernetes Deployment, Service, HPA manifests cho tất cả dịch vụ
  - GitHub Actions CI/CD pipeline với các stages: build, test, security scan, push to GCR, deploy to GKE
  - Tài liệu kiến trúc hệ thống, tài liệu API REST, hướng dẫn cài đặt và vận hành

## 3. Chỉ thị chức năng cho đại lý phụ chuyên biệt
*   **Coder**: Đóng vai trò là Nhà phát triển ứng dụng cấp Cao/Chính. Chịu trách nhiệm triển khai mã nguồn ứng dụng thuần túy trên lớp dịch vụ backend (REST controllers, services, repositories) và ứng dụng khách frontend. Bị cấm viết bộ kiểm thử hoặc manifest hạ tầng.
*   **Tester**: Đóng vai trò là Kiểm soát chất lượng (QC/QA) cấp Lead/Chính. Chuyên về kỹ thuật bộ kiểm thử, xác thực và cổng chất lượng. Chịu trách nhiệm tạo các bộ kiểm thử đơn vị, tích hợp và end-to-end. Bị cấm sửa mã nguồn ứng dụng sản xuất. Nếu phạm vi kiểm thử tích hợp không thể cô lập thành một tệp mã ứng dụng cụ thể, phải sử dụng literal token `INTEGRATION_SCOPE` làm tham số đầu tiên của cặp dấu chấm phẩy.
*   **Doc**: Hoạt động như là Nhà viết kỹ thuật chính và Kiến trúc sư hệ thống doanh nghiệp. Chuyên về biên soạn tài liệu đặc tả kỹ thuật toàn diện, tài liệu tham chiếu schema, bản vẽ hệ thống và danh mục kiến trúc doanh nghiệp. Mỗi tệp tài liệu kỹ thuật được tạo phải có phần mở rộng `.md` và nằm nghiêm ngặt trong `./sources/docs/`.
*   **Reviewer**: Chịu trách nhiệm xác minh trình biên dịch, cổng phân tích tĩnh và vá lỗi phòng thủ. Chuyên về kiểm toán chất lượng mã nguồn, giải quyết lỗi biên dịch, sửa lỗ hổng bảo mật OWASP và đề xuất tối ưu truy vấn.
*   **Docker**: Chuyên về container hóa, xây dựng Dockerfile đa giai đoạn, tối ưu gói phần mềm và đẩy hình ảnh ứng dụng đã xác minh lên DockerHub.
*   **GCP**: Chuyên về tự động hóa đám mây trên Google Cloud Platform. Chịu trách nhiệm xây dựng và đẩy hình ảnh lên Google Cloud Artifact Registry (GCR), và điều phối môi trường container một cách native trên Google Cloud Run.
*   **GKE**: Chuyên về điều phối container sản xuất bên trong Google Kubernetes Engine. Chịu trách nhiệm xây dựng manifest triển khai Kubernetes, điều khiển định tuyến, cấu hình HPA, Helm charts, và triển khai workload microservices vào cụm GKE đang hoạt động.

## 4. Định nghĩa hoàn thành giai đoạn (DoD)
1. Hoàn thành 100% các thẻ theo dõi yêu cầu được phân bổ cho Giai đoạn 5: [REQ-003], [ARC-001] đến [ARC-010], [NFR-001] đến [NFR-009], không có thẻ nào bị bỏ sót.
2. Bộ lọc RBAC toàn cục hoạt động chính xác, ngăn chặn mọi truy cập trái phép với độ trễ xác thực <10ms, đạt độ bao phủ kiểm thử 100% cho lớp trung gian.
3. Hình ảnh Docker cuối cùng của tất cả dịch vụ có kích thước dưới 500MB, đạt yêu cầu NFR-005.
4. Hạ tầng GCP được provisioning thành công qua Terraform với đầy đủ VPC, Cloud SQL, Redis, IAM, đảm bảo mã hóa AES-256 và TLS 1.3.
5. Manifest GKE triển khai thành công với HPA tự động scale (CPU >70% hoặc độ trễ >300ms), rolling update strategy, và khả năng chịu lỗi.
6. Pipeline CI/CD tự động hóa thành công từ commit đến triển khai, bao gồm build, test, security scan, và deployment.
7. Toàn bộ tài liệu doanh nghiệp được hoàn thiện: kiến trúc hệ thống, tài liệu API REST, hướng dẫn cài đặt và vận hành, hướng dẫn sao lưu và phục hồi.
8. Tất cả yêu cầu phi chức năng về hiệu suất (độ trễ API <200ms), khả năng sẵn sàng (99.9% uptime), bảo mật (TLS 1.3, AES-256), khả năng mở rộng, ghi log audit lưu trữ 1 năm, đa ngôn ngữ, tuân thủ GDPR/CCPA, sao lưu PostgreSQL hàng ngày và phục hồi điểm thời lên đến 24 giờ được đáp ứng và ghi chú đầy đủ trong tài liệu.

## 5. NHẬT KÝ THỰC HIỆN KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1:
<!--DAY_HEADER_START-->Triển khai lớp trung gian RBAC toàn cục và kiểm thử đơn vị quyền truy cập<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 1.1: Triển khai lớp trung gian RBAC toàn cục
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/auth/src/main/java/org/nlh4j/membership_hub/security/RbacMiddleware.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai lớp trung gian RBAC dưới dạng JAX-RS ContainerRequestFilter để xác thực quyền truy cập của người dùng dựa trên vai trò được lưu trong JWT token. Áp dụng ma trận quyền truy cập: System Admin (toàn quyền trên tất cả trung tâm), Center Admin (toàn quyền trong trung tâm được phân công), Manager (quyền quản lý học viên, thông báo, không chỉnh sửa khóa học), Teacher (quyền xem khóa học, danh sách học viên, lịch dạy - chỉ đọc), Student (quyền duyệt khóa học, đăng ký, xem thẻ hội viên). Lớp trung gian phải trích xuất vai trò từ JWT token, so khớp với endpoint được yêu cầu và phương thức HTTP, trả về lỗi 403 Forbidden với thông báo chi tiết nếu người dùng không có quyền. Đảm bảo kiểm tra quyền được áp dụng cho tất cả endpoint trước khi xử lý yêu cầu. Sử dụng prepared statements của Hibernate ORM để truy vấn thông tin vai trò, ngăn chặn SQL injection. Áp dụng làm sạch dữ liệu đầu vào để chống XSS. Đảm bảo không có lỗ hổng bypass quyền truy cập.

<!--START_EXC_HANDLER>
```json
{
  "exception_handlers": [
    {
      "error_code": "RBAC_ACCESS_DENIED",
      "http_status": 403,
      "trigger_condition": "Người dùng không có quyền truy cập tài nguyên dựa trên vai trò",
      "behavior": "Trả về mã lỗi 403 Forbidden với thông báo chi tiết về quyền bị thiếu và tài nguyên bị từ chối."
    }
  ]
}
```
<!--END_EXC_HANDLER-->

#### 📝 Công việc phụ 1.2: Viết bộ kiểm thử đơn vị cho lớp trung gian RBAC
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/auth/src/test/java/org/nlh4j/membership_hub/RbacMiddlewareTest.java;./sources/backend/auth/src/main/java/org/nlh4j/membership_hub/security/RbacMiddleware.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết bộ kiểm thử đơn vị toàn diện cho lớp RbacMiddleware sử dụng JUnit 5 và Mockito. Các kịch bản test bắt buộc: (1) Người dùng có vai trò System Admin truy cập endpoint quản lý trung tâm thành công; (2) Người dùng có vai trò Student truy cập endpoint quản lý khóa học bị từ chối với mã 403; (3) Center Admin truy cập tài nguyên của trung tâm khác bị từ chối; (4) Manager truy cập endpoint chỉnh sửa khóa học bị từ chối; (5) Teacher truy cập endpoint đăng ký khóa học bị từ chối; (6) JWT token hết hạn hoặc không hợp lệ bị từ chối truy cập với mã 401; (7) Kiểm tra độ trễ xác thực quyền dưới 10ms. Đảm bảo độ bao phủ mã đạt trên 90% bằng JaCoCo và tất cả test case pass trên môi trường CI/CD.

#### 📝 Công việc phụ 1.3: Khởi tạo cấu trúc tài liệu kỹ thuật và từ điển dữ liệu hạ tầng
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/operations/infrastructure-overview.md;./sources/docs/operations/terraform-modules.md;./sources/docs/operations/gke-manifests.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[NFR-001], [NFR-002], [NFR-004], [NFR-005], [NFR-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Khởi tạo cấu trúc thư mục và khung tài liệu kỹ thuật cho toàn bộ hạ tầng DevOps của giai đoạn 5. Tạo tài liệu tổng quan hạ tầng mô tả kiến trúc đám mây GCP, cấu hình VPC, Cloud SQL, Redis, GKE. Tạo từ điển dữ liệu cho các tài nguyên hạ tầng (terraform modules, variables, outputs). Tạo hướng dẫn cấu trúc manifest GKE (deployment, service, HPA). Đảm bảo tất cả tài liệu tuân thủ chuẩn Markdown doanh nghiệp, có mục lục, liên kết chéo, và được đặt trong đúng vị trí thư mục `./sources/docs/operations/`. Tài liệu phải phục vụ làm cơ sở cho các tác vụ phát triển và vận hành sau này.

### 🌤️ NGÀY 2:
<!--DAY_HEADER_START-->Rà soát mã RBAC và xây dựng Dockerfile đa giai đoạn<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 2.1: Rà soát mã nguồn lớp trung gian RBAC và sửa lỗi bảo mật
##### Đại lý phụ được phân công: [Reviewer]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/auth/src/main/java/org/nlh4j/membership_hub/security/RbacMiddleware.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-003]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Thực hiện rà soát toàn bộ mã nguồn lớp RbacMiddleware để phát hiện và sửa các lỗ hổng bảo mật tiềm ẩn. Kiểm tra các điểm: (1) Không có lỗi so khớp vai trò không phân biệt chữ hoa chữ thường; (2) Không có lỗi thiếu kiểm tra quyền trên các endpoint ẩn hoặc HTTP methods không phải GET; (3) Không có lỗi bypass quyền qua tham số URL hoặc header đặc biệt; (4) Đảm bảo tuân thủ đầy đủ OWASP Top 10, đặc biệt là kiểm soát truy cập và xác thực đầu vào; (5) Tối ưu hiệu suất xác thực quyền để độ trễ xác thực <10ms; (6) Đảm bảo mã nguồn tuân thủ tiêu chuẩn mã hóa doanh nghiệp và quy tắc đặt tên biến. Ghi nhận tất cả lỗi nghiêm trọng và đề xuất giải pháp sửa chữa.

#### 📝 Công việc phụ 2.2: Xây dựng Dockerfile đa giai đoạn cho tất cả dịch vụ
##### Đại lý phụ được phân công: [Docker]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/infra/docker/Dockerfile;./sources/infra/docker/.dockerignore`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[NFR-005]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng Dockerfile đa giai đoạn cho tất cả dịch vụ backend (Quarkus) và frontend (Next.js). Sử dụng base image nhẹ (alpine hoặc distroless) cho giai đoạn cuối. Loại bỏ các tệp không cần thiết (tệp nguồn, tệp test, công cụ build, Maven/Gradle cache) trong giai đoạn cuối để tối ưu kích thước hình ảnh. Tối ưu cấu hình layer caching bằng cách sao chép tệp phụ thuộc trước khi sao chép mã nguồn. Đảm bảo kích thước hình ảnh cuối cùng dưới 500MB cho tất cả dịch vụ, tuân thủ yêu cầu NFR-005. Tạo file `.dockerignore` để loại bỏ các tệp không cần thiết khỏi ngữ cảnh build.

### 🌤️ NGÀY 3:
<!--DAY_HEADER_START-->Provisioning hạ tầng GCP và tạo manifest GKE<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 3.1: Provisioning hạ tầng GCP bằng Terraform
##### Đại lý phụ được phân công: [GCP]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/infra/terraform/main.tf;./sources/infra/terraform/variables.tf;./sources/infra/terraform/outputs.tf`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[NFR-001], [NFR-002], [NFR-004], [NFR-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Cấu hình và triển khai toàn bộ hạ tầng GCP cần thiết cho hệ thống bằng Terraform. Tạo VPC với subnet riêng cho các dịch vụ backend và frontend, đảm bảo không có kết nối công cộng trực tiếp đến cơ sở dữ liệu. Cấu hình IAM roles với nguyên tắc đặc quyền tối thiểu cho từng dịch vụ. Provisioning Cloud SQL (PostgreSQL) với cấu hình sao lưu tự động hàng ngày, mã hóa dữ liệu lưu trữ AES-256, và kết nối TLS 1.3. Tạo Redis cluster cho caching phiên làm việc với chính sách hết hạn 24 giờ và LRU eviction. Cấu hình Cloud Storage cho lưu trữ tệp báo cáo. Thiết lập Firewall rules chỉ mở các cổng cần thiết (80, 443, 5432, 6379). Đảm bảo tất cả tài nguyên được định nghĩa dưới dạng mã (IaC), có thể reproduce và version control. Tích hợp monitoring và logging từ đầu.

<!--START_EXC_HANDLER>
```json
{
  "exception_handlers": [
    {
      "error_code": "GCP_PROVISIONING_FAILED",
      "http_status": 500,
      "trigger_condition": "Quá trình provisioning tài nguyên GCP qua Terraform thất bại",
      "behavior": "Ghi log lỗi chi tiết, rollback các tài nguyên đã tạo và thông báo cho đội ngũ vận hành."
    }
  ]
}
```
<!--END_EXC_HANDLER-->

#### 📝 Công việc phụ 3.2: Tạo manifest triển khai GKE cho tất cả dịch vụ
##### Đại lý phụ được phân công: [GKE]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/infra/gke/deployment.yaml;./sources/infra/gke/hpa.yaml;./sources/infra/gke/service.yaml`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[NFR-002], [NFR-004], [NFR-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo manifest Kubernetes triển khai cho tất cả dịch vụ backend và frontend lên GKE. Cấu hình resource limits (CPU, memory) cho từng dịch vụ dựa trên khối lượng công việc dự kiến. Thiết lập readiness và liveness probes với các ngưỡng phù hợp để đảm bảo khả năng chịu lỗi. Cấu hình Horizontal Pod Autoscaler (HPA) tự động scale số lượng pod dựa trên ngưỡng CPU >70% hoặc độ trễ yêu cầu >300ms. Cấu hình rolling update strategy với maxSurge và maxUnavailable để đảm bảo không có thời gian chết trong quá trình triển khai phiên bản mới. Tạo Service manifest cho từng dịch vụ với LoadBalancer hoặc NodePort tùy theo nhu cầu. Đảm bảo tất cả dịch vụ có thể giao tiếp với nhau trong cluster thông qua DNS nội bộ.

<!--START_EXC_HANDLER>
```json
{
  "exception_handlers": [
    {
      "error_code": "GKE_DEPLOYMENT_FAILED",
      "http_status": 500,
      "trigger_condition": "Việc triển khai manifest lên GKE thất bại",
      "behavior": "Giữ nguyên phiên bản cũ, ghi log lỗi và kích hoạt cảnh báo cho đội ngũ DevOps."
    }
  ]
}
```
<!--END_EXC_HANDLER-->

### 🌤️ NGÀY 4:
<!--DAY_HEADER_START-->Thiết lập pipeline CI/CD và kiểm thử tích hợp hạ tầng<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 4.1: Xây dựng pipeline CI/CD với GitHub Actions
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/infra/.github/workflows/ci-cd-pipeline.yaml`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[NFR-001], [NFR-005], [NFR-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng pipeline CI/CD tự động với GitHub Actions bao gồm các stages: (1) Checkout mã nguồn và thiết lập môi trường Java 21 và Node.js; (2) Build backend Quarkus với Maven và kiểm tra kích thước artifact; (3) Build frontend Next.js và kiểm tra kích thước bundle; (4) Chạy bộ kiểm thử đơn vị và tích hợp với mục tiêu độ bao phủ >=85%; (5) Quét lỗ hổng bảo mật phụ thuộc bằng OWASP Dependency Check; (6) Kiểm tra kích thước hình ảnh Docker đảm bảo dưới 500MB; (7) Push hình ảnh đã build lên Google Container Registry (GCR); (8) Triển khai tự động lên GKE khi commit vào nhánh main. Tích hợp ghi log audit cho tất cả sự kiện pipeline (build, test, deploy) với timestamp, user ID, và chi tiết hành động, lưu trữ log trong 1 năm theo yêu cầu NFR-006. Cấu hình thông báo lỗi qua email và Slack cho đội ngũ DevOps.

<!--START_EXC_HANDLER>
```json
{
  "exception_handlers": [
    {
      "error_code": "DOCKER_BUILD_FAILED",
      "http_status": 500,
      "trigger_condition": "Quá trình build Docker image thất bại",
      "behavior": "Dừng pipeline CI/CD, ghi log lỗi build chi tiết và thông báo cho nhà phát triển."
    }
  ]
}
```
<!--END_EXC_HANDLER-->

#### 📝 Công việc phụ 4.2: Thực hiện kiểm thử tích hợp hạ tầng toàn hệ thống
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `INTEGRATION_SCOPE;./sources/infra/test/infrastructure-integration-test.yaml`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[NFR-001], [NFR-002], [NFR-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Thực hiện kiểm thử tích hợp toàn bộ hạ tầng để xác minh tất cả thành phần hoạt động đúng như đặc tả. Các kịch bản test: (1) Xác minh tất cả tài nguyên GCP được triển khai chính xác theo đặc tả Terraform (VPC, Cloud SQL, Redis, GKE clusters); (2) Kiểm tra kết nối giữa các dịch vụ (backend, PostgreSQL, Redis, FCM) hoạt động ổn định với độ trễ dưới 200ms; (3) Kiểm tra HPA tự động scale pod khi tải tăng (giả lập tải cao và kiểm tra số lượng pod tăng lên); (4) Kiểm tra pipeline CI/CD chạy thành công từ commit đến triển khai trên môi trường staging; (5) Kiểm tra tính năng sao lưu và phục hồi PostgreSQL hoạt động chính xác (thực hiện backup, xóa bảng, restore và xác minh dữ liệu được khôi phục). Ghi nhận tất cả lỗi phát sinh và đề xuất giải pháp khắc phục. Đảm bảo tất cả test case pass trước khi chuyển sang giai đoạn vận hành.

### 🌤️ NGÀY 5:
<!--DAY_HEADER_START-->Hoàn thiện tài liệu doanh nghiệp và rà soát cuối cùng<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 5.1: Soạn thảo tài liệu kiến trúc hệ thống
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/architecture/system-architecture.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Soạn thảo bản vẽ kiến trúc hệ thống tổng thể theo chuẩn doanh nghiệp. Tài liệu phải bao gồm: (1) Sơ đồ kiến trúc tổng thể thể hiện các lớp: Frontend (Next.js, React Native), Backend (Quarkus microservices), Data Layer (PostgreSQL, Redis), Infrastructure (GKE, Terraform, Docker); (2) Mô tả chi tiết cấu trúc microservices và tương tác giữa các dịch vụ qua REST API và Kafka events; (3) Ma trận RBAC với 5 vai trò người dùng và ma trận quyền truy cập chi tiết; (4) Sơ đồ luồng dữ liệu chính: xác thực OAuth2/JWT, điểm danh QR, thông báo đa kênh, đăng ký khóa học, gia hạn thẻ hội viên; (5) Sơ đồ tương tác giữa các thành phần kiến trúc trong các luồng nghiệp vụ chính; (6) Danh sách các thư viện và phiên bản công nghệ được sử dụng; (7) Sơ đồ hạ tầng GCP và GKE. Đảm bảo tài liệu phản ánh chính xác kiến trúc đã triển khai và tuân thủ tất cả yêu cầu kiến trúc đã định nghĩa.

#### 📝 Công việc phụ 5.2: Soạn thảo tài liệu API và hướng dẫn vận hành
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/api/rest-api-reference.md;./sources/docs/operations/installation-guide.md;./sources/docs/operations/backup-recovery-guide.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Soạn thảo bộ tài liệu doanh nghiệp hoàn chỉnh. Tài liệu API REST phải mô tả đầy đủ tất cả endpoint công khai và nội bộ, bao gồm đường dẫn, phương thức HTTP, schema yêu cầu/phản hồi, mã lỗi, ví dụ sử dụng, và yêu cầu xác thực. Tài liệu hướng dẫn cài đặt phải mô tả chi tiết quy trình triển khai hệ thống trên môi trường GKE, bao gồm cấu hình biến môi trường, khởi tạo cơ sở dữ liệu, triển khai dịch vụ, và cấu hình DNS. Tài liệu hướng dẫn vận hành phải bao gồm quy trình giám sát, xử lý sự cố thường gặp, quy trình sao lưu và phục hồi thảm họa PostgreSQL (bao gồm PITR), quy trình scale dịch vụ, và quy trình cập nhật phiên bản. Đảm bảo tất cả yêu cầu phi chức năng về hiệu suất, bảo mật, khả năng sẵn sàng, tuân thủ GDPR/CCPA được ghi chú đầy đủ trong tài liệu.