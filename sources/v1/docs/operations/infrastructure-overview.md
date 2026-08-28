markdown
# Tổng quan Hạ tầng Hệ thống Membership Hub
> **Tài liệu:** Infrastructure Overview  
> **Phiên bản:** 1.0  
> **Ngày cập nhật:** 2026-08-18  
> **Tác giả:** Đội Kiến trúc Hệ thống & DevOps  
> **Trạng thái:** Đã phê duyệt  
> **Thẻ theo dõi liên quan:** [NFR-001], [NFR-002], [NFR-004], [NFR-005], [NFR-009]

## Mục lục
1. [Mục đích & Phạm vi tài liệu](#1-mục-đích--phạm-vi-tài-liệu)
2. [Ma trận Theo dõi Yêu cầu](#2-ma-trận-theo-dõi-yêu-cầu)
3. [Tổng quan Kiến trúc Hạ tầng](#3-tổng-quan-kiến-trúc-hạ-tầng)
4. [Thành phần Hạ tầng GCP](#4-thành-phần-hạ-tầng-gcp)
5. [Hạ tầng dưới dạng Mã (IaC) với Terraform](#5-hạ-tầng-dưới-dạng-mã-iac-với-terraform)
6. [Chiến lược Đóng gói Container (Docker)](#6-chiến-lược-đóng-gói-container-docker)
7. [Orchestration Workload trên GKE](#7-orchestration-workload-trên-gke)
8. [Pipeline CI/CD Tự động hóa](#8-pipeline-ci-cd-tự-động-hóa)
9. [Ràng buộc Bảo mật & Tuân thủ](#9-ràng-buộc-bảo-mật--tuân-thủ)
10. [Sổ tay Vận hành](#10-sổ-tay-vận-hành)
11. [Tài liệu Liên quan](#11-tài-liệu-liên-quan)

---

## 1. Mục đích & Phạm vi tài liệu
Tài liệu này mô tả toàn bộ hạ tầng đám mây, quy trình đóng gói container, orchestration workload và pipeline CI/CD cho hệ thống `membership-hub`, phục vụ làm cơ sở cho đội phát triển, vận hành và kiểm thử. Tất cả cấu hình được tuân thủ các yêu cầu phi chức năng (NFR) và ràng buộc kiến trúc (ARC) đã được định nghĩa trong đặc tả hệ thống.

Phạm vi bao gồm:
- Hạ tầng GCP: VPC, Cloud SQL, Redis, GKE, Cloud Storage, IAM, Monitoring & Logging
- Hạ tầng dưới dạng mã (IaC) với Terraform
- Chiến lược đóng gói container đa giai đoạn với Docker
- Quản lý workload trên GKE với Kubernetes manifests
- Pipeline CI/CD tự động hóa với GitHub Actions
- Sổ tay vận hành, sao lưu và phục hồi thảm họa

---

## 2. Ma trận Theo dõi Yêu cầu
Ma trận này ánh xạ trực tiếp các yêu cầu phi chức năng được chỉ định đến các thành phần và phần nội dung liên quan trong tài liệu, đảm bảo tuân thủ quy tắc truy xuất nguồn gốc doanh nghiệp.

| Mã Yêu cầu | Mô tả Yêu cầu | Thành phần / Phần tài liệu liên quan |
|------------|---------------|-------------------------------------|
| [NFR-001] | Độ trễ API cốt lõi <200ms, hỗ trợ 10.000 người dùng đồng thời | [3. Tổng quan Kiến trúc Hạ tầng](#3-tổng-quan-kiến-trúc-hạ-tầng), [7. Orchestration Workload trên GKE](#7-orchestration-workload-trên-gke), [8. Pipeline CI/CD Tự động hóa](#8-pipeline-ci-cd-tự-động-hóa), [10. Sổ tay Vận hành](#10-sổ-tay-vận-hành) |
| [NFR-002] | Thời gian hoạt động 99.9% hàng năm, chế độ failover tự động giữa các cụm GKE | [3. Tổng quan Kiến trúc Hạ tầng](#3-tổng-quan-kiến-trúc-hạ-tầng), [4. Thành phần Hạ tầng GCP](#4-thành-phần-hạ-tầng-gcp), [7. Orchestration Workload trên GKE](#7-orchestration-workload-trên-gke), [10. Sổ tay Vận hành](#10-sổ-tay-vận-hành) |
| [NFR-004] | Quy mô ngang tự động qua HPA khi CPU >70% hoặc độ trễ >300ms, sử dụng bản sao đọc PostgreSQL cho khối lượng công việc báo cáo | [4. Thành phần Hạ tầng GCP](#4-thành-phần-hạ-tầng-gcp), [5. Hạ tầng dưới dạng Mã (IaC) với Terraform](#5-hạ-tầng-dưới-dạng-mã-iac-với-terraform), [7. Orchestration Workload trên GKE](#7-orchestration-workload-trên-gke) |
| [NFR-005] | Kích thước hình ảnh Docker cuối <500MB | [6. Chiến lược Đóng gói Container (Docker)](#6-chiến-lược-đóng-gói-container-docker), [8. Pipeline CI/CD Tự động hóa](#8-pipeline-ci-cd-tự-động-hóa) |
| [NFR-009] | Sao lưu PostgreSQL hàng ngày, hỗ trợ PITR 24 giờ, sao lưu cụm GKE sang vùng riêng | [4. Thành phần Hạ tầng GCP](#4-thành-phần-hạ-tầng-gcp), [5. Hạ tầng dưới dạng Mã (IaC) với Terraform](#5-hạ-tầng-dưới-dạng-mã-iac-với-terraform), [10. Sổ tay Vận hành](#10-sổ-tay-vận-hành) |

---

## 3. Tổng quan Kiến trúc Hạ tầng
Hệ thống được triển khai trên nền tảng GCP theo mô hình microservices, tất cả workload chạy trên GKE trong các subnet riêng, không có public IP được gán trực tiếp cho dịch vụ backend hoặc cơ sở dữ liệu. Lưu lượng người dùng chỉ đi qua Ingress Gateway được bảo vệ bởi firewall rules.

mermaid
flowchart TD
    subgraph GCP_Project [membership-hub-prod]
        subgraph VPC [VPC Riêng (CIDR: 10.0.0.0/16)]
            subgraph Public_Subnet [Subnet Công khai (10.0.1.0/24)]
                Ingress[Ingress Gateway<br/>(GKE Ingress Controller)]
                NAT[NAT Gateway]
            end
            subgraph Private_Subnet [Subnet Riêng (10.0.2.0/24)]
                GKE[GKE Cluster vùng asia-southeast1<br/>(3 AZ: a, b, c) [NFR-002], [NFR-004]]
                CloudSQL[(Cloud SQL PostgreSQL 15<br/>1 bản chính + 2 bản sao đọc<br/>[NFR-004], [NFR-009])]
                Redis[(Redis Cluster 7.0<br/>3 shards, 3 replicas/shard<br/>[ARC-009])]
                GKE_Backup[GKE Cluster Backup<br/>Lưu trữ vùng us-central2<br/>[NFR-009]]
            end
        end
        CloudStorage[(Cloud Storage<br/>Bucket: membership-hub-reports<br/>[REQ-024], [NFR-009])]
        CloudLogging[Cloud Logging<br/>Lưu trữ log 1 năm<br/>[NFR-006]]
        CloudMonitoring[Cloud Monitoring<br/>Cảnh báo uptime, độ trễ<br/>[NFR-001], [NFR-002]]
        SecretManager[Secret Manager<br/>Lưu trữ thông tin xác thực<br/>[NFR-003]]
        Firebase[Firebase Authentication<br/>Xác thực OAuth2/JWT<br/>[ARC-006]]
    end
    Internet[Internet]
    MobileApp[Ứng dụng Di động React Native]
    WebApp[Ứng dụng Web Next.js]
    ZaloAPI[API Zalo]
    FCM[Firebase Cloud Messaging]

    Internet --> Ingress
    MobileApp --> Internet
    WebApp --> Internet
    Ingress --> GKE
    GKE --> CloudSQL
    GKE --> Redis
    GKE --> CloudStorage
    GKE --> Firebase
    GKE --> ZaloAPI
    GKE --> FCM
    GKE --> CloudLogging
    GKE --> CloudMonitoring
    GKE --> SecretManager
    CloudSQL --> CloudLogging
    Redis --> CloudLogging
    GKE_Backup --> GKE


### Đặc điểm kiến trúc chính:
1. **Cô lập mạng nghiêm ngặt**: Tất cả workload backend và cơ sở dữ liệu nằm trong subnet riêng, không có public IP, lưu lượng chỉ được phép đi qua Ingress Gateway trên port 443, giảm thiểu bề mặt tấn công [NFR-003]
2. **Sẵn sàng cao**: GKE cluster triển khai trên 3 AZ, Cloud SQL có cấu hình high availability với standby instance, Redis có 3 replicas mỗi shard, đảm bảo thời gian hoạt động 99.9% [NFR-002]
3. **Khả năng mở rộng**: HPA tự động scale số lượng pod dựa trên ngưỡng CPU và độ trễ, bản sao đọc PostgreSQL xử lý khối lượng công việc báo cáo không ảnh hưởng đến cơ sở dữ liệu chính [NFR-001], [NFR-004]
4. **Bảo mật tập trung**: Tất cả thông tin xác thực được lưu trữ trong Secret Manager, truy cập được kiểm soát chặt chẽ qua IAM, dữ liệu được mã hóa ở trạng thái nghỉ và khi truyền [NFR-003]

---

## 4. Thành phần Hạ tầng GCP
Tất cả thành phần hạ tầng được định nghĩa dưới dạng mã (IaC) với Terraform, đảm bảo tính nhất quán và có thể tái tạo giữa các môi trường.

### 4.1 VPC & Mạng
- **Cấu hình VPC**: CIDR `10.0.0.0/16`, chia thành 2 subnet:
  - Public subnet (10.0.1.0/24): chứa Ingress Gateway và NAT Gateway, chỉ cho phép lưu lượng vào từ Internet trên port 443
  - Private subnet (10.0.2.0/24): chứa toàn bộ workload backend, cơ sở dữ liệu và dịch vụ nội bộ, không có route đến Internet trực tiếp
- **Firewall Rules**:
  - Cho phép lưu lượng vào Ingress từ 0.0.0.0/0 trên port 443 (HTTPS)
  - Cho phép lưu lượng nội bộ giữa các dịch vụ trong private subnet trên các port cần thiết (5432 cho PostgreSQL, 6379 cho Redis, 9092 cho Kafka, 8080 cho dịch vụ nội bộ)
  - Chặn tất cả lưu lượng còn lại
- **Tệp tham chiếu**: `./sources/infra/terraform/main.tf` (module `vpc`) [NFR-002], [NFR-003]

### 4.2 Cloud SQL (PostgreSQL)
- **Phiên bản**: PostgreSQL 15, loại instance `db-custom-4-16384` (4 vCPU, 16GB RAM)
- **Cấu hình high availability**: 1 bản chính + 1 standby instance ở AZ khác, tự động failover khi bản chính gặp sự cố [NFR-002]
- **Bản sao đọc**: 2 bản sao đọc được cấu hình để xử lý khối lượng công việc báo cáo, giảm tải cho cơ sở dữ liệu chính [NFR-004]
- **Sao lưu**: Sao lưu tự động hàng ngày lúc 02:00 UTC, lưu trữ 7 ngày, hỗ trợ phục hồi điểm thời gian (PITR) 24 giờ [NFR-009]
- **Mã hóa**: Dữ liệu ở trạng thái nghỉ mã hóa AES-256, dữ liệu khi truyền mã hóa TLS 1.3 [NFR-003]
- **Tệp tham chiếu**: `./sources/infra/terraform/main.tf` (module `cloudsql`) [NFR-004], [NFR-009]

### 4.3 Redis Cluster
- **Phiên bản**: Redis 7.0, chế độ cluster, 3 shards, 3 replicas mỗi shard
- **Use case**: Lưu trữ phiên làm việc người dùng, cache dữ liệu khóa học, hàng đợi điểm danh pending khi mất kết nối mạng [ARC-009]
- **Cấu hình**: Chính sách xóa LRU, thời gian hết hạn phiên làm việc 24 giờ, giới hạn bộ nhớ tối đa 8GB [ARC-009]
- **Bảo mật**: Triển khai trong private subnet, không có public access, xác thực bằng mật khẩu được lưu trong Secret Manager [NFR-003]
- **Tệp tham chiếu**: `./sources/infra/terraform/main.tf` (module `redis`) [ARC-009]

### 4.4 GKE Cluster
- **Phiên bản**: GKE 1.28, regional cluster triển khai trên 3 AZ (asia-southeast1-a, asia-southeast1-b, asia-southeast1-c)
- **Node pools**:
  1. Node pool hệ thống: 2 node `e2-standard-4`, chạy các workload hệ thống (Ingress, monitoring agent)
  2. Node pool ứng dụng: 3 node khởi tạo `e2-standard-8`, tự động scale từ 3 đến 20 node dựa trên tải [NFR-004]
- **Tính năng bảo mật**: Workload Identity được bật để xác thực với dịch vụ GCP mà không cần quản lý khóa service account [NFR-003]
- **Tệp tham chiếu**: `./sources/infra/terraform/main.tf` (module `gke`), `./sources/infra/gke/deployment.yaml` [NFR-002], [NFR-004]

### 4.5 Cloud Storage
- **Bucket**: `membership-hub-reports`, lớp lưu trữ `STANDARD`, bật versioning
- **Use case**: Lưu trữ file báo cáo điểm danh CSV, file sao lưu GKE cluster [REQ-024], [NFR-009]
- **Kiểm soát truy cập**: Chỉ service account của dịch vụ backend có quyền đọc/ghi, tắt quyền truy cập công khai hoàn toàn [NFR-003]
- **Tệp tham chiếu**: `./sources/infra/terraform/main.tf` (module `storage`) [NFR-009]

### 4.6 IAM & Quản lý Bí mật
- **Nguyên tắc đặc quyền tối thiểu**: Mỗi service account chỉ được cấp quyền truy cập cần thiết cho chức năng của nó, không cấp quyền `Owner` hoặc `Editor` cho bất kỳ tài nguyên nào [NFR-003]
- **Secret Manager**: Tất cả thông tin xác thực (credential cơ sở dữ liệu, khóa Firebase, khóa API Zalo, token FCM) được lưu trữ trong Secret Manager, được mount động vào pod GKE tại thời gian chạy, không hardcode trong mã nguồn hoặc hình ảnh Docker [NFR-003]
- **Tệp tham chiếu**: `./sources/infra/terraform/variables.tf` (biến IAM), `./sources/infra/terraform/main.tf` (module `iam`) [NFR-003]

### 4.7 Giám sát & Ghi log
- **Cloud Logging**: Bật ghi log cho tất cả workload GKE, Cloud SQL, Redis, lưu trữ log 1 năm để đáp ứng yêu cầu ghi log kiểm toán [NFR-006]
- **Cloud Monitoring**: Cấu hình dashboard theo dõi độ trễ API, tỷ lệ lỗi, mức sử dụng CPU/bộ nhớ pod, hiệu suất cơ sở dữ liệu, uptime của tất cả dịch vụ [NFR-001], [NFR-002]
- **Cảnh báo**: Cấu hình cảnh báo tự động cho các sự kiện: CPU >70% trong 5 phút, độ trễ API >300ms trong 2 phút, pod restart, lỗi kết nối cơ sở dữ liệu, sao lưu thất bại [NFR-001], [NFR-002], [NFR-009]
- **Tệp tham chiếu**: `./sources/infra/gcp/monitoring-backup.yaml` [NFR-001], [NFR-002], [NFR-006], [NFR-009]

---

## 5. Hạ tầng dưới dạng Mã (IaC) với Terraform
Toàn bộ hạ tầng GCP được định nghĩa dưới dạng mã với Terraform, đảm bảo tính nhất quán giữa các môi trường (staging, production) và có thể tái tạo nhanh chóng trong trường hợp thảm họa.

### 5.1 Cấu trúc thư mục Terraform

./sources/infra/terraform/
├── main.tf                # Tệp chính gọi các module con
├── variables.tf           # Định nghĩa biến đầu vào immutable cho toàn bộ hạ tầng
├── outputs.tf             # Xuất giá trị đầu ra (địa chỉ tài nguyên, thông tin kết nối) cho pipeline CI/CD và ứng dụng
├── modules/               # Các module con tái sử dụng
│   ├── vpc/               # Module cấu hình VPC, subnet, firewall, NAT
│   ├── cloudsql/          # Module cấu hình Cloud SQL, bản sao đọc, sao lưu, PITR
│   ├── redis/             # Module cấu hình Redis cluster
│   ├── gke/               # Module cấu hình GKE cluster, node pools, workload identity
│   ├── storage/           # Module cấu hình Cloud Storage bucket, IAM bindings
│   └── iam/               # Module cấu hình service account, IAM roles, Secret Manager secrets
└── README.md              # Hướng dẫn sử dụng module Terraform


### 5.2 Quy ước biến và đầu ra
- Tất cả biến immutable (vùng GCP, ID dự án, loại instance, dải CIDR, thông số sao lưu) được định nghĩa trong `variables.tf` với ràng buộc kiểu dữ liệu và giá trị mặc định, không hardcode giá trị trong các tệp module
- Tất cả đầu ra cần thiết cho pipeline CI/CD và cấu hình ứng dụng được xuất trong `outputs.tf`, bao gồm địa chỉ kết nối cơ sở dữ liệu, địa chỉ Redis, tên bucket Cloud Storage, thông tin xác thực GKE
- Tất cả tài nguyên được gắn thẻ `cost_center` và `environment` để theo dõi chi phí [NFR-004]

### 5.3 Ánh xạ yêu cầu NFR
- [NFR-001]: Cấu hình VPC và GKE được tối ưu để giảm độ trễ mạng, đảm bảo độ trễ API <200ms
- [NFR-002]: Tất cả tài nguyên có tính sẵn sàng cao (multi-AZ, failover tự động) được cấu hình trong module Terraform
- [NFR-004]: Thông số auto-scaling cho GKE và cấu hình bản sao đọc PostgreSQL được định nghĩa trong module
- [NFR-009]: Cấu hình sao lưu tự động, PITR và sao lưu vùng cho GKE được định nghĩa trong module

---

## 6. Chiến lược Đóng gói Container (Docker)
Tất cả hình ảnh Docker được xây dựng theo quy trình đa giai đoạn (multi-stage build) để tối thiểu kích thước và loại bỏ các công cụ build không cần thiết trong môi trường production, đảm bảo kích thước hình ảnh cuối <500MB theo yêu cầu [NFR-005].

### 6.1 Cấu trúc Dockerfile
Tệp Dockerfile chung được định nghĩa tại `./sources/infra/docker/Dockerfile`, hỗ trợ build cho cả backend Quarkus (Java 21) và frontend Next.js:
1. **Giai đoạn build**:
   - Backend: sử dụng base image `maven:3.9-eclipse-temurin-21` để build artifact JAR
   - Frontend: sử dụng base image `node:20-alpine` để build static assets
2. **Giai đoạn runtime**:
   - Backend: sử dụng base image `distroless/java:21` (không có shell, không có công cụ hệ thống) để chạy JAR file, giảm thiểu lỗ hổng bảo mật
   - Frontend: sử dụng base image `nginx:alpine` để phục vụ static assets

### 6.2 Tối ưu kích thước hình ảnh
- Sử dụng tệp `.dockerignore` tại `./sources/infra/docker/.dockerignore` để loại bỏ các tệp không cần thiết khỏi bối cảnh build:
  
  .git
  .env
  node_modules
  target/
  build/
  *.log
  *.md
  test/
  
- Kết hợp các lệnh `RUN` trong cùng một lớp để giảm số lượng layer, xóa cache của trình quản lý gói (apt, maven, npm) trên cùng một dòng lệnh để giảm kích thước layer
- Kích thước hình ảnh cuối: backend ~180MB, frontend ~120MB, tổng kích thước <500MB [NFR-005]

### 6.3 Tích hợp với CI/CD
Pipeline CI/CD tự động kiểm tra kích thước hình ảnh sau khi build, dừng pipeline nếu kích thước vượt quá ngưỡng 500MB [NFR-005].

---

## 7. Orchestration Workload trên GKE
Tất cả workload backend và frontend được triển khai trên GKE dưới dạng manifest Kubernetes, tuân thủ các tiêu chuẩn bảo mật và hiệu suất doanh nghiệp.

### 7.1 Cấu trúc manifest GKE
Tất cả manifest được định nghĩa trong thư mục `./sources/infra/gke/`:
- `deployment.yaml`: Định nghĩa Deployment cho tất cả dịch vụ backend (auth-service, course-service, enrollment-service, attendance-service, membership-service, notification-service, promotion-service, chatbot-service, report-service) và frontend (web-app, mobile-app)
- `service.yaml`: Định nghĩa Service để expose các dịch vụ nội bộ và Ingress Gateway
- `hpa.yaml`: Định nghĩa Horizontal Pod Autoscaler cho tất cả dịch vụ backend
- `configmap.yaml`: Lưu trữ cấu hình chung cho tất cả dịch vụ (URL dịch vụ nội bộ, thông số cấu hình)
- `secret-provider.yaml`: Cấu hình mount bí mật từ Secret Manager vào pod

### 7.2 Cấu hình Deployment
Mỗi Deployment được cấu hình với các tham số bắt buộc:
- **Resource limits và requests**: Định nghĩa rõ ràng giới hạn và yêu cầu CPU/bộ nhớ cho mỗi pod, đảm bảo hiệu suất ổn định và ngăn chặn một pod chiếm hết tài nguyên node [NFR-001]
- **Probes**:
  - Liveness probe: kiểm tra sức khỏe của pod, tự động restart pod nếu probe thất bại
  - Readiness probe: kiểm tra pod đã sẵn sàng xử lý yêu cầu chưa, chỉ thêm pod vào pool xử lý yêu cầu khi probe thành công [NFR-002]
- **Pod anti-affinity**: Cấu hình quy tắc chống ảnh hưởng để phân bổ các pod của cùng một dịch vụ trên các AZ khác nhau, đảm bảo tính sẵn sàng cao khi một AZ gặp sự cố [NFR-002]
- **ConfigMap và Secret mount**: Tất cả cấu hình và bí mật được mount từ ConfigMap và Secret Manager, không hardcode giá trị trong manifest hoặc mã nguồn [NFR-003]

### 7.3 Cấu hình HPA (Horizontal Pod Autoscaler)
HPA được cấu hình cho tất cả dịch vụ backend với quy tắc tự động scale:
- **Trigger 1**: Tỷ lệ sử dụng CPU trung bình của pod >70% trong 1 phút [NFR-004]
- **Trigger 2**: Độ trễ yêu cầu trung bình >300ms trong 2 phút (sử dụng custom metric từ Cloud Monitoring) [NFR-001], [NFR-004]
- **Giới hạn scale**: Số lượng pod tối thiểu 3, tối đa 20 cho mỗi dịch vụ
- **Tệp tham chiếu**: `./sources/infra/gke/hpa.yaml` [NFR-001], [NFR-004]

### 7.4 Ghi log
Tất cả container được cấu hình ghi log ra stdout/stderr, được thu thập bởi agent Fluentbit và gửi đến Cloud Logging, không lưu trữ log cục bộ trên đĩa ephemeral của pod để tránh mất log khi pod bị xóa [NFR-006].

---

## 8. Pipeline CI/CD Tự động hóa
Pipeline CI/CD được xây dựng với GitHub Actions, tự động hóa toàn bộ quy trình từ khi có commit đến triển khai lên môi trường production, đảm bảo chất lượng mã và bảo mật.

### 8.1 Cấu trúc pipeline
Tệp workflow được định nghĩa tại `./sources/infra/.github/workflows/ci-cd-pipeline.yaml`, kích hoạt khi có push lên nhánh `main`/`develop` hoặc tạo pull request:
1. **Kiểm tra mã nguồn**: Checkout mã nguồn từ repository, cài đặt các phụ thuộc
2. **Build & Kiểm thử**: Build backend và frontend, chạy bộ kiểm thử đơn vị và tích hợp, đảm bảo độ phủ mã >=85%
3. **Quét bảo mật**: Chạy OWASP Dependency Check để quét lỗ hổng trong phụ thuộc, chạy SonarQube để kiểm tra chất lượng mã, dừng pipeline nếu phát hiện lỗ hổng nghiêm trọng hoặc chất lượng mã không đạt ngưỡng
4. **Build Docker Image**: Xây dựng hình ảnh Docker đa giai đoạn, kiểm tra kích thước hình ảnh <500MB [NFR-005]
5. **Push lên GCR**: Đẩy hình ảnh đã được tag theo commit SHA lên Google Container Registry (GCR)
6. **Triển khai lên GKE**: Áp dụng các manifest Kubernetes lên GKE cluster, thực hiện rolling update không thời gian chết
7. **Kiểm thử hậu triển khai**: Chạy kiểm thử smoke để xác nhận tất cả dịch vụ hoạt động bình thường
8. **Ghi log audit**: Ghi lại tất cả sự kiện pipeline vào Cloud Logging, lưu trữ 1 năm [NFR-006]

### 8.2 Ràng buộc truy cập
Chỉ các maintainer được ủy quyền mới có quyền kích hoạt triển khai lên môi trường production, tất cả thao tác triển khai đều được ghi log audit đầy đủ [NFR-003], [NFR-006].

---

## 9. Ràng buộc Bảo mật & Tuân thủ
Tất cả thành phần hạ tầng được cấu hình để tuân thủ các yêu cầu bảo mật doanh nghiệp và OWASP Top 10:
1. **Bảo mật mạng**: Tất cả workload nằm trong private subnet, không có public IP, firewall rules chỉ cho phép lưu lượng cần thiết, TLS 1.3 được bắt buộc cho tất cả kết nối [NFR-003]
2. **Quản lý danh tính và truy cập**: Nguyên tắc đặc quyền tối thiểu cho IAM, Workload Identity cho GKE pods, không hardcode thông tin xác thực trong mã nguồn hoặc hình ảnh Docker [NFR-003]
3. **Bảo mật dữ liệu**: Dữ liệu ở trạng thái nghỉ mã hóa AES-256, dữ liệu khi truyền mã hóa TLS 1.3, dữ liệu PII được masking trước khi ghi log hoặc trả về qua API [NFR-003], [NFR-006]
4. **Bảo mật ứng dụng**: RBAC được triển khai cho tất cả endpoint API, chống SQL injection bằng prepared statements, chống XSS bằng làm sạch đầu vào và header CSP, chống CSRF bằng token CSRF [NFR-003]
5. **Tuân thủ**: Ghi log kiểm toán cho tất cả hành động nhạy cảm, lưu trữ 1 năm, hỗ trợ xóa dữ liệu người dùng và xuất dữ liệu theo yêu cầu GDPR/CCPA [NFR-006], [NFR-008]

---

## 10. Sổ tay Vận hành
### 10.1 Sao lưu & Phục hồi thảm họa
- **Sao lưu PostgreSQL**: Sao lưu tự động hàng ngày lúc 02:00 UTC, sao lưu gia tăng mỗi 15 phút, lưu trữ 7 ngày, hỗ trợ PITR 24 giờ [NFR-009]
- **Sao lưu GKE**: Sao lưu tự động hàng ngày của trạng thái cluster và persistent volume, lưu trữ trong Cloud Storage ở vùng `us-central2` (vùng riêng so với vùng production) [NFR-009]
- **Quy trình phục hồi**: Hướng dẫn chi tiết phục hồi cơ sở dữ liệu từ bản sao lưu, phục hồi cluster GKE từ bản sao lưu, kiểm tra phục hồi hàng quý để đảm bảo tính khả thi

### 10.2 Giám sát & Cảnh báo
- **Dashboard theo dõi**: Dashboard Cloud Monitoring được cấu hình sẵn để theo dõi độ trễ API, tỷ lệ lỗi, mức sử dụng tài nguyên, số lượng người dùng hoạt động, số lượng điểm danh hàng ngày
- **Quy tắc cảnh báo**:
  - Cảnh báo nghiêm trọng: Downtime dịch vụ >1 phút, độ trễ API >500ms trong 5 phút, lỗi kết nối cơ sở dữ liệu, sao lưu thất bại
  - Cảnh báo cảnh báo: CPU sử dụng >70% trong 10 phút, số lượng pod <3, dung lượng ổ đĩa >80%
- **Đường dây nóng**: Quy trình leo thang sự cố được định nghĩa rõ ràng, cảnh báo được gửi đến đội vận hành qua email, SMS và Slack

### 10.3 Mở rộng & Bảo trì
- **Mở rộng tự động**: HPA tự động scale số lượng pod dựa trên tải, không cần can thiệp thủ công cho các đợt tải cao đột biến [NFR-004]
- **Triển khai không thời gian chết**: Sử dụng chiến lược rolling update của Kubernetes, triển khai phiên bản mới mà không gián đoạn dịch vụ, có thể rollback về phiên bản cũ trong vòng 1 phút nếu phát hiện lỗi [NFR-002]
- **Bảo trì định kỳ**: Cập nhật phiên bản GKE, node image và cơ sở dữ liệu vào khung thời gian bảo trì được thông báo trước cho người dùng

---

## 11. Tài liệu Liên quan
| Tệp tài liệu | Mô tả | Thẻ theo dõi liên quan |
|--------------|-------|------------------------|
| `./sources/docs/architecture/system-architecture.md` | Tài liệu kiến trúc hệ thống tổng thể, sơ đồ luồng dữ liệu, ma trận RBAC | [ARC-001] đến [ARC-010] |
| `./sources/docs/operations/installation-guide.md` | Hướng dẫn cài đặt hệ thống trên môi trường cục bộ và production | [NFR-001] đến [NFR-009] |
| `./sources/docs/operations/backup-recovery-guide.md` | Hướng dẫn chi tiết sao lưu và phục hồi thảm họa | [NFR-009] |
| `./sources/docs/api/rest-api-reference.md` | Tài liệu tham chiếu API REST cho tất cả dịch vụ backend | [REQ-001] đến [REQ-025] |
| `./sources/infra/terraform/README.md` | Hướng dẫn sử dụng module Terraform, quy trình cập nhật hạ tầng | [NFR-001], [NFR-002], [NFR-004], [NFR-009] |
| `./sources/infra/gke/README.md` | Hướng dẫn triển khai, giám sát và xử lý sự cố workload trên GKE | [NFR-002], [NFR-004], [NFR-006] |
| `./sources/infra/docker/README.md` | Hướng dẫn build hình ảnh Docker cho backend và frontend | [NFR-005] |

---

## Phụ lục: Danh sách tệp hạ tầng tham chiếu
| Đường dẫn tệp | Mô tả | Thẻ theo dõi |
|---------------|-------|--------------|
| `./sources/infra/terraform/main.tf` | Tệp chính Terraform, gọi các module hạ tầng | [NFR-001], [NFR-002], [NFR-004], [NFR-009] |
| `./sources/infra/terraform/variables.tf` | Định nghĩa biến đầu vào immutable cho hạ tầng | [NFR-001], [NFR-002], [NFR-004], [NFR-009] |
| `./sources/infra/terraform/outputs.tf` | Định nghĩa đầu ra tài nguyên hạ tầng | [NFR-001], [NFR-002], [NFR-004], [NFR-009] |
| `./sources/infra/docker/Dockerfile` | Dockerfile đa giai đoạn cho backend và frontend | [NFR-005] |
| `./sources/infra/docker/.dockerignore` | Danh sách tệp/thư mục bị loại bỏ khi build Docker | [NFR-005] |
| `./sources/infra/gke/deployment.yaml` | Manifest Kubernetes Deployment cho tất cả dịch vụ | [NFR-002], [NFR-004] |
| `./sources/infra/gke/hpa.yaml` | Manifest Kubernetes HPA cho tự động scale | [NFR-001], [NFR-004] |
| `./sources/infra/gke/service.yaml` | Manifest Kubernetes Service cho expose dịch vụ | [NFR-002], [NFR-004] |
| `./sources/infra/.github/workflows/ci-cd-pipeline.yaml` | Pipeline CI/CD GitHub Actions | [NFR-001], [NFR-005], [NFR-006] |
| `./sources/infra/gcp/monitoring-backup.yaml` | Cấu hình giám sát, cảnh báo và sao lưu GCP | [NFR-001], [NFR-002], [NFR-006], [NFR-009] |