# Giai đoạn 5: <!--PHASE_NAME_START-->Containerization, triển khai GCP, điều phối GKE, bảo mật, tuân thủ<!--PHASE_NAME_END-->

## 📊 Điều khiển tài liệu

| Mục | Chi tiết |
| :--- | :--- |
| **ID Kiến trúc** | ARCH-20260804165526 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 5 |
| **Tên Giai đoạn** | <!--PHASE_NAME_START-->Containerization, triển khai GCP, điều phối GKE, bảo mật, tuân thủ<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn này tập trung vào việc container hóa toàn bộ dịch vụ, triển khai các dịch vụ lên GCP thông qua Docker và Helm, cấu hình GKE với HPA, autoscaling, monitoring, logging, và đảm bảo tuân thủ các tiêu chuẩn bảo mật OWASP, GDPR, và các yêu cầu hiệu suất, sẵn sàng, bảo mật dữ liệu.<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Baseline) |
| **Ngày/Giờ** | 2026/08/04 16:55:26 |
| **Tác giả** | Enterprise System Architect (SA Agent) |
| **Phê duyệt** | Pending Technical Governance Review |

## Phạm vi và mục tiêu của Giai đoạn

Giai đoạn 5 thực hiện containerization, triển khai dịch vụ lên GCP, điều phối GKE, cấu hình HPA, autoscaling, monitoring, logging, CI/CD, và đảm bảo tuân thủ OWASP, GDPR, NFR và ARC.

## Phạm vi kỹ thuật và ranh giới thư mục

- `./sources/infra/docker` – Dockerfile, multi‑stage build, image push, security scan.  
- `./sources/infra/gcp` – Terraform, Cloud Build, Cloud Run, Firebase Hosting.  
- `./sources/infra/gke` – Helm chart, HPA, autoscaling, monitoring, logging, CI/CD.

## Hướng dẫn chức năng dành cho Sub-Agent

| Sub-Agent | Trách nhiệm chính |
| :--- | :--- |
| Docker | Viết Dockerfile đa giai đoạn, build, push, kiểm thử image. |
| GKE | Viết Helm chart, cấu hình HPA, autoscaling, monitoring, logging, CI/CD. |

## Định nghĩa Hoàn thành Giai đoạn (DoD)

- Tất cả tag ID `[NFR-001]`‑`[NFR-009]` và `[ARC-010]` được map đầy đủ.  
- Docker image có kích thước < 500 MB, base < 200 MB.  
- Helm chart triển khai thành công, HPA hoạt động, autoscaling đáp ứng.  
- Monitoring, logging, CI/CD hoạt động, bảo mật OWASP được kiểm tra.  
- Coverage test ≥ 90 % cho các module liên quan.  
- Đảm bảo tuân thủ NFR, NFR, NFR, NFR, NFR, NFR, NFR, NFR, NFR, ARC.

## LỊCH THỰC HIỆN KIẾT THUẬT NGÀY MỖI NGÀY

### Ngày 1: <!--DAY_HEADER_START-->XÂY DỰNG Dockerfile Đa Giai Đoạn VÀ TỐI ƯU KÍCH THƯỚC<!--DAY_HEADER_END-->

#### Nhiệm vụ phụ 1.1: Viết Dockerfile đa giai đoạn, giảm kích thước, build, push tới registry, kiểm thử hình ảnh
##### Được giao cho: Docker
##### Yêu cầu thành phần và kỹ thuật:
* **Đường dẫn mục tiêu**: ./sources/infra/docker
* **Thẻ mã theo dõi**: <!--START_TAGS-->[NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009], [ARC-010]<!--END_TAGS-->

#### Nhiệm vụ phụ 1.2: Kiểm thử và xác nhận image, thực hiện scan bảo mật, chuẩn hóa metadata
##### Được giao cho: Docker
##### Yêu cầu thành phần và kỹ thuật:
* **Đường dẫn mục tiêu**: ./sources/infra/docker
* **Thẻ mã theo dõi**: <!--START_TAGS-->[NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009], [ARC-010]<!--END_TAGS-->

### Ngày 2: <!--DAY_HEADER_START-->XÂY DỰNG Helm chart, HPA, autoscaling, monitoring, logging, CI/CD<!--DAY_HEADER_END-->

#### Nhiệm vụ phụ 2.1: Viết Helm chart, cấu hình HPA, autoscaling, monitoring, logging, CI/CD
##### Được giao cho: GKE
##### Yêu cầu thành phần và kỹ thuật:
* **Đường dẫn mục tiêu**: ./sources/infra/gke
* **Thẻ mã theo dõi**: <!--START_TAGS-->[NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009], [ARC-010]<!--END_TAGS-->

#### Nhiệm vụ phụ 2.2: Kiểm thử triển khai, xác nhận scaling, monitoring, logging, bảo mật
##### Được giao cho: GKE
##### Yêu cầu thành phần và kỹ thuật:
* **Đường dẫn mục tiêu**: ./sources/infra/gke
* **Thẻ mã theo dõi**: <!--START_TAGS-->[NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009], [ARC-010]<!--END_TAGS-->