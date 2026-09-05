markdown
# Tài liệu Hạ tầng GKE & DevOps Giai đoạn 5 - membership-hub
## Metadata Tài liệu
| Thuộc tính | Giá trị |
|------------|---------|
| **ID Tài liệu** | OPS-GKE-20260818-001 |
| **Dự án** | membership-hub |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày tạo** | 2026-08-18 |
| **Tác giả** | Kỹ sư DevOps Doanh nghiệp |
| **Thẻ theo dõi bắt buộc** | [NFR-001], [NFR-002], [NFR-004], [NFR-005], [NFR-009] |
| **Đường dẫn tệp** | `./sources/docs/operations/gke-manifests.md` |
| **Tài liệu liên quan** | [Kiến trúc Hệ thống Tổng quan](./system-architecture.md), [Tài liệu API REST](./rest-api-reference.md), [Hướng dẫn Cài đặt](./installation-guide.md), [Hướng dẫn Sao lưu & Phục hồi](./backup-recovery-guide.md) |

## Mục lục
1. [Mục đích & Phạm vi](#1-mục-đích--phạm-vi)
2. [Kiến trúc Hạ tầng GCP Tổng quan](#2-kiến-trúc-hạ-tầng-gcp-tổng-quan)
3. [Từ điển Tài nguyên Terraform](#3-từ-điển-tài-nguyên-terraform)
4. [Cấu trúc Manifest GKE Chuẩn](#4-cấu-trúc-manifest-gke-chuẩn)
5. [Hướng dẫn Triển khai Vận hành](#5-hướng-dẫn-triển-khai-vận-hành)
6. [Ma trận Theo dõi Yêu cầu](#6-ma-trận-theo-dõi-yêu-cầu)
7. [Phụ lục](#7-phụ-lục)

---

## 1. Mục đích & Phạm vi
Tài liệu này là nguồn thông tin duy nhất (single source of truth) cho toàn bộ hạ tầng DevOps giai đoạn 5 của dự án `membership-hub`, bao gồm:
- Kiến trúc đám mây GCP tuân thủ các yêu cầu phi chức năng (NFR) đã định nghĩa
- Quy ước cấu trúc thư mục và đặt tên cho tài nguyên Terraform
- Cấu trúc chuẩn cho các manifest Kubernetes triển khai lên GKE
- Quy trình triển khai, vận hành và xử lý sự cố hạ tầng

Tất cả nội dung trong tài liệu này được ánh xạ trực tiếp đến các thẻ theo dõi yêu cầu [NFR-001], [NFR-002], [NFR-004], [NFR-005], [NFR-009] đảm bảo tuân thủ kiến trúc đã được phê duyệt.

---

## 2. Kiến trúc Hạ tầng GCP Tổng quan
### 2.1 Sơ đồ kiến trúc
mermaid
graph TD
    A[End User / Mobile App] --> B[Google Cloud Firewall]
    B --> C[VPC: Public Subnet]
    B --> D[VPC: Private Subnet]
    C --> E[Google Container Registry (GCR)]
    C --> F[Bastion Host (Quản trị viên)]
    D --> G[Cụm GKE (membership-hub-gke)]
    D --> H[Cloud SQL PostgreSQL (HA Mode)]
    D --> I[Redis Cluster (Memorystore)]
    G --> J[Cloud Logging & Cloud Monitoring]
    G --> K[Cloud Storage (Reports, Backups)]
    H --> K
    I --> G
    F --> G
    E --> G
    %% Styling
    style G fill:#4CAF50,stroke:#2E7D32,color:#fff
    style H fill:#2196F3,stroke:#1565C0,color:#fff
    style I fill:#FF9800,stroke:#EF6C00,color:#fff
    style K fill:#9C27B0,stroke:#6A1B9A,color:#fff


### 2.2 Mô tả thành phần kiến trúc
| Thành phần | Mô tả | Thẻ theo dõi liên quan |
|------------|-------|------------------------|
| VPC với 2 subnet riêng (Public/Private) | Mạng riêng ảo cô lập toàn bộ tài nguyên, không có tài nguyên tính toán nào có public IP trực tiếp | [NFR-003] (Bảo mật mạng) |
| Cloud SQL PostgreSQL (HA) | Cơ sở dữ liệu chính với chế độ high availability, hỗ trợ PITR 24h, sao lưu hàng ngày | [NFR-001], [NFR-002], [NFR-009] |
| Redis Cluster (Memorystore) | Hệ thống caching phiên làm việc và dữ liệu ngoại tuyến cho ứng dụng di động | [NFR-001], [ARC-009] |
| Cụm GKE (Google Kubernetes Engine) | Nền tảng orchestration container, triển khai tất cả dịch vụ backend và frontend | [NFR-002], [NFR-004] |
| Google Container Registry (GCR) | Kho lưu trữ hình ảnh Docker được quét lỗ hổng bảo mật tự động | [NFR-005] |
| Cloud Logging & Monitoring | Hệ thống ghi log kiểm toán và giám sát hiệu suất, tích hợp cảnh báo tự động | [NFR-002], [NFR-006] |
| Cloud Storage | Lưu trữ file báo cáo điểm danh CSV và sao lưu hạ tầng | [NFR-009] |

---

## 3. Từ điển Tài nguyên Terraform
Tất cả tài nguyên hạ tầng được định nghĩa dưới dạng Infrastructure as Code (IaC) với Terraform, lưu trữ tại đường dẫn `./sources/infra/terraform/`.

### 3.1 Danh sách biến bắt buộc (`variables.tf`)
| Tên biến | Kiểu dữ liệu | Mô tả | Giá trị mặc định | Thẻ theo dõi |
|----------|--------------|--------|------------------|--------------|
| `project_id` | `string` | ID dự án GCP của tổ chức | Bắt buộc nhập | [NFR-001], [NFR-002] |
| `region` | `string` | Vùng triển khai chính (ví dụ: `asia-southeast1`) | `asia-southeast1` | [NFR-001], [NFR-002] |
| `backup_region` | `string` | Vùng riêng để sao lưu hạ tầng | `asia-east2` | [NFR-009] |
| `gke_cluster_name` | `string` | Tên cụm GKE | `membership-hub-gke` | [NFR-002], [NFR-004] |
| `gke_node_count` | `number` | Số lượng node ban đầu của cụm GKE | `3` | [NFR-002], [NFR-004] |
| `db_instance_type` | `string` | Loại instance Cloud SQL PostgreSQL | `db-custom-4-16384` | [NFR-001] |
| `db_ha_enabled` | `bool` | Bật chế độ high availability cho Cloud SQL | `true` | [NFR-002], [NFR-009] |
| `db_backup_retention_days` | `number` | Số ngày lưu trữ sao lưu Cloud SQL | `7` | [NFR-009] |
| `redis_memory_size_gb` | `number` | Kích thước bộ nhớ Redis cluster | `4` | [NFR-001], [ARC-009] |
| `hpa_cpu_threshold` | `number` | Ngưỡng CPU (%) kích hoạt tự động scale pod | `70` | [NFR-004] |
| `hpa_latency_threshold_ms` | `number` | Ngưỡng độ trễ yêu cầu (ms) kích hoạt tự động scale pod | `300` | [NFR-004] |
| `docker_base_image` | `string` | Hình ảnh base cho giai đoạn build Docker | `eclipse-temurin:21-jdk` | [NFR-005] |
| `docker_runtime_image` | `string` | Hình ảnh runtime nhẹ cho giai đoạn cuối Docker | `distroless/java21:latest` | [NFR-005] |

### 3.2 Danh sách output (`outputs.tf`)
| Tên output | Mô tả | Thẻ theo dõi |
|------------|--------|--------------|
| `gke_cluster_endpoint` | Địa chỉ endpoint công khai của cụm GKE | [NFR-002], [NFR-004] |
| `gke_cluster_ca_certificate` | Chứng chỉ CA của cụm GKE để xác thực kubectl | [NFR-003] |
| `db_connection_string` | Chuỗi kết nối Cloud SQL đã mã hóa (lưu trữ trong Secret Manager) | [NFR-001], [NFR-003] |
| `redis_host` | Địa chỉ host Redis cluster nội bộ | [NFR-001], [ARC-009] |
| `gcr_repository_url` | Địa chỉ repository GCR để push hình ảnh Docker | [NFR-005] |
| `cloud_sql_backup_bucket` | Tên bucket Cloud Storage lưu trữ sao lưu Cloud SQL | [NFR-009] |
| `gke_backup_plan_id` | ID kế hoạch sao lưu cụm GKE | [NFR-009] |

### 3.3 Danh sách module Terraform
| Tên module | Đường dẫn | Mục đích | Thẻ theo dõi |
|------------|-----------|----------|--------------|
| `gcp-vpc` | `./sources/infra/terraform/modules/vpc` | Tạo VPC, 2 subnet riêng (public/private), firewall rules chỉ mở cổng cần thiết | [NFR-003], [NFR-002] |
| `gcp-cloud-sql` | `./sources/infra/terraform/modules/cloud-sql` | Provision Cloud SQL PostgreSQL HA, tạo read replica cho báo cáo, cấu hình sao lưu tự động và PITR | [NFR-001], [NFR-002], [NFR-009] |
| `gcp-redis` | `./sources/infra/terraform/modules/redis` | Tạo Redis cluster cho caching phiên làm việc, cấu hình chính sách xóa LRU | [NFR-001], [ARC-009] |
| `gcp-gke` | `./sources/infra/terraform/modules/gke` | Tạo cụm GKE, node pool, IAM service account với quyền hạn tối thiểu, cấu hình logging mặc định | [NFR-002], [NFR-004] |
| `gcp-gcr` | `./sources/infra/terraform/modules/gcr` | Tạo Google Container Registry, bật quét lỗ hổng bảo mật tự động | [NFR-005], [NFR-003] |
| `gcp-monitoring` | `./sources/infra/terraform/modules/monitoring` | Cấu hình Cloud Logging, Cloud Monitoring, cảnh báo uptime và lỗi hệ thống | [NFR-002], [NFR-006] |
| `gcp-backup` | `./sources/infra/terraform/modules/backup` | Cấu hình sao lưu hàng ngày Cloud SQL, sao lưu cụm GKE sang vùng riêng | [NFR-009] |

---

## 4. Cấu trúc Manifest GKE Chuẩn
Tất cả manifest Kubernetes được lưu trữ tại đường dẫn `./sources/infra/gke/`, tuân thủ cấu trúc chuẩn dưới đây.

### 4.1 Cấu trúc thư mục manifest

./sources/infra/gke/
├── namespace.yaml          # Định nghĩa namespace membership-hub
├── configmap.yaml          # Biến môi trường chung cho tất cả dịch vụ
├── secret.yaml             # Tham chiếu đến Secret Manager GCP (không lưu giá trị nhạy cảm trực tiếp)
├── deployment.yaml         # Định nghĩa Deployment cho tất cả dịch vụ backend/frontend
├── service.yaml            # Định nghĩa Service (ClusterIP/LoadBalancer) cho từng dịch vụ
├── hpa.yaml                # Định nghĩa Horizontal Pod Autoscaler cho tất cả dịch vụ có thể scale
├── pdb.yaml                # Định nghĩa Pod Disruption Budget để đảm bảo tính sẵn sàng khi bảo trì
└── network-policy.yaml     # Định nghĩa chính sách mạng cô lập giữa các dịch vụ


### 4.2 Quy ước cấu hình Deployment
Tất cả các deployment phải tuân thủ các quy tắc bắt buộc sau, ánh xạ đến yêu cầu NFR:
1. **Cấu hình resource limits bắt buộc**: Mọi container phải khai báo cả `requests` và `limits` cho CPU và memory, đảm bảo độ trễ API <200ms và hỗ trợ 10k người dùng đồng thời [NFR-001]
2. **Health probes bắt buộc**: Mọi container phải có `livenessProbe` và `readinessProbe` để đảm bảo thời gian hoạt động 99.9% [NFR-002]
3. **Pod Anti-Affinity bắt buộc**: Các pod của cùng dịch vụ phải được phân bổ trên các availability zone khác nhau để tránh mất toàn bộ dịch vụ khi một zone gặp sự cố [NFR-002]
4. **Sử dụng hình ảnh đa giai đoạn**: Tất cả hình ảnh Docker được build bằng multi-stage build, loại bỏ công cụ build và tệp nguồn trong giai đoạn cuối, đảm bảo kích thước hình ảnh cuối <500MB [NFR-005]
5. **Biến môi trường từ bên ngoài**: Tất cả biến môi trường nhạy cảm được lấy từ GCP Secret Manager, không hardcode giá trị trong manifest [NFR-003]

#### Ví dụ cấu hình Deployment cho dịch vụ Backend
yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: membership-hub-backend
  namespace: membership-hub
  labels:
    app: backend
    service: membership-hub
    version: v1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      # Pod Anti-Affinity phân bổ pod trên các AZ khác nhau [NFR-002]
      affinity:
        podAntiAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
          - labelSelector:
              matchLabels:
                app: backend
            topologyKey: topology.kubernetes.io/zone
      containers:
      - name: backend
        # Hình ảnh đa giai đoạn nhẹ, đã quét lỗ hổng bảo mật [NFR-005]
        image: gcr.io/${PROJECT_ID}/membership-hub-backend:latest
        imagePullPolicy: Always
        # Cấu hình resource đảm bảo độ trễ <200ms [NFR-001]
        resources:
          requests:
            cpu: "500m"
            memory: "1Gi"
          limits:
            cpu: "2000m"
            memory: "2Gi"
        # Health probes đảm bảo uptime 99.9% [NFR-002]
        livenessProbe:
          httpGet:
            path: /q/health/live
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /q/health/ready
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
          failureThreshold: 3
        # Biến môi trường từ ConfigMap và Secret Manager [NFR-003]
        envFrom:
        - configMapRef:
            name: backend-config
        - secretRef:
            name: backend-secret
        ports:
        - containerPort: 8080
          name: http
        # Volume mount cho cache và log
        volumes:
        - name: tmp-volume
          emptyDir: {}
        volumeMounts:
        - name: tmp-volume
          mountPath: /tmp


### 4.3 Cấu hình Horizontal Pod Autoscaler (HPA)
HPA được cấu hình để tự động scale số lượng pod dựa trên 2 ngưỡng: CPU >70% hoặc độ trễ yêu cầu >300ms, đáp ứng yêu cầu xử lý tải tăng đột biến [NFR-004].

#### Ví dụ cấu hình HPA cho dịch vụ Backend
yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: backend-hpa
  namespace: membership-hub
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: membership-hub-backend
  metrics:
  # Ngưỡng CPU kích hoạt scale [NFR-004]
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: ${HPA_CPU_THRESHOLD}
  # Ngưỡng độ trễ yêu cầu kích hoạt scale [NFR-004]
  - type: Pods
    pods:
      metric:
        name: request_latency_seconds
      target:
        type: AverageValue
        averageValue: "${HPA_LATENCY_THRESHOLD_MS}m"
  minReplicas: 3
  maxReplicas: 20
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60


### 4.4 Cấu hình Service
Tất cả dịch vụ nội bộ sử dụng loại `ClusterIP`, chỉ các dịch vụ cần expose ra ngoài (như frontend web) sử dụng `LoadBalancer` với IP tĩnh. Không có dịch vụ nào được expose trực tiếp với public IP ngoại trừ cổng vào chính [NFR-003].

### 4.5 Cấu hình .dockerignore chuẩn
Tuân thủ yêu cầu kích thước hình ảnh Docker <500MB [NFR-005], tệp `.dockerignore` phải chứa các mục sau:

.git
.env
node_modules
target/
build/
*.log
*.md
!README.md
docker-compose*.yml
.terraform/
*.tfstate
*.tfstate.backup


---

## 5. Hướng dẫn Triển khai Vận hành
Quy trình triển khai tuân thủ chuẩn CI/CD, đảm bảo không có thời gian chết (zero downtime) và tuân thủ tất cả yêu cầu NFR.

### Bước 1: Chuẩn bị môi trường cục bộ
1. Cài đặt các công cụ bắt buộc: Terraform >=1.5, gcloud CLI >=450.0.0, Docker >=24.0, kubectl >=1.28
2. Xác thực với GCP: Chạy lệnh `gcloud auth login` và `gcloud config set project ${PROJECT_ID}`
3. Cấu hình kết nối kubectl với cụm GKE: Chạy lệnh `gcloud container clusters get-credentials ${GKE_CLUSTER_NAME} --region ${REGION}`
4. Thẻ theo dõi: [NFR-001], [NFR-002]

### Bước 2: Build Docker image đa giai đoạn
1. Chạy lệnh build cho từng dịch vụ:
   bash
   # Build dịch vụ backend
   docker build -t gcr.io/${PROJECT_ID}/membership-hub-backend:latest ./sources/backend/membership-hub
   # Build dịch vụ frontend web
   docker build -t gcr.io/${PROJECT_ID}/membership-hub-web:latest ./sources/frontend/web-app
   # Build ứng dụng di động
   docker build -t gcr.io/${PROJECT_ID}/membership-hub-mobile:latest ./sources/frontend/mobile-app
   
2. Xác minh kích thước hình ảnh: Chạy `docker images` đảm bảo kích thước hình ảnh cuối <500MB [NFR-005]
3. Thẻ theo dõi: [NFR-005]

### Bước 3: Push hình ảnh lên GCR
1. Xác thực với GCR: Chạy lệnh `gcloud auth configure-docker`
2. Push hình ảnh lên repository:
   bash
   docker push gcr.io/${PROJECT_ID}/membership-hub-backend:latest
   docker push gcr.io/${PROJECT_ID}/membership-hub-web:latest
   docker push gcr.io/${PROJECT_ID}/membership-hub-mobile:latest
   
3. Xác minh quét lỗ hổng bảo mật: Kiểm tra trên giao diện GCR không có lỗ hổng nghiêm trọng [NFR-003]
4. Thẻ theo dõi: [NFR-003], [NFR-005]

### Bước 4: Provision hạ tầng GCP bằng Terraform
1. Di chuyển đến thư mục terraform: `cd ./sources/infra/terraform/`
2. Chạy lệnh khởi tạo: `terraform init`
3. Xem trước kế hoạch thay đổi: `terraform plan -out=tfplan`
4. Áp dụng kế hoạch: `terraform apply tfplan`
5. Xác minh tất cả tài nguyên được tạo đúng: Cloud SQL, Redis, GKE, Cloud Storage, IAM roles [NFR-001], [NFR-002], [NFR-004], [NFR-009]
6. Thẻ theo dõi: [NFR-001], [NFR-002], [NFR-004], [NFR-009]

### Bước 5: Triển khai manifest lên GKE
1. Áp dụng namespace và cấu hình chung:
   bash
   kubectl apply -f ./sources/infra/gke/namespace.yaml
   kubectl apply -f ./sources/infra/gke/configmap.yaml
   kubectl apply -f ./sources/infra/gke/secret.yaml
   
2. Triển khai dịch vụ backend:
   bash
   kubectl apply -f ./sources/infra/gke/deployment.yaml
   kubectl apply -f ./sources/infra/gke/service.yaml
   kubectl apply -f ./sources/infra/gke/hpa.yaml
   kubectl apply -f ./sources/infra/gke/pdb.yaml
   
3. Xác minh trạng thái triển khai:
   bash
   # Kiểm tra pod đang chạy
   kubectl get pods -n membership-hub
   # Kiểm tra trạng thái HPA
   kubectl get hpa -n membership-hub
   # Kiểm tra service
   kubectl get svc -n membership-hub
   
4. Thẻ theo dõi: [NFR-002], [NFR-004]

### Bước 6: Cấu hình giám sát và sao lưu
1. Kích hoạt Cloud Logging cho tất cả dịch vụ GKE, đảm bảo log được lưu trữ 1 năm [NFR-006]
2. Cấu hình cảnh báo uptime cho tất cả endpoint công khai, ngưỡng uptime 99.9% [NFR-002]
3. Xác minh sao lưu hàng ngày Cloud SQL được kích hoạt, thời gian lưu trữ sao lưu 7 ngày, hỗ trợ PITR 24h [NFR-009]
4. Xác minh sao lưu cụm GKE sang vùng `backup_region` được kích hoạt hàng ngày [NFR-009]
5. Thẻ theo dõi: [NFR-002], [NFR-006], [NFR-009]

### Bước 7: Xác minh tuân thủ yêu cầu
1. Chạy bộ kiểm thử tích hợp toàn bộ hạ tầng, đảm bảo độ phủ test >=85% [NFR-004]
2. Kiểm tra không có tài nguyên tính toán nào có public IP trực tiếp ngoại trừ bastion host và load balancer công khai [NFR-003]
3. Kiểm tra IAM service account của dịch vụ chỉ có quyền hạn tối thiểu cần thiết, không có quyền Owner/Editor [NFR-003]
4. Thẻ theo dõi: [NFR-003], [NFR-004]

---

## 6. Ma trận Theo dõi Yêu cầu
Ma trận này ánh xạ toàn bộ nội dung tài liệu đến các thẻ yêu cầu NFR được chỉ định, đảm bảo 100% bao phủ yêu cầu phi chức năng của giai đoạn 5.

| Nội dung tài liệu | Thẻ theo dõi liên quan | Mô tả ánh xạ yêu cầu |
|-------------------|------------------------|----------------------|
| Cấu hình VPC Private Subnets, Firewall Rules, IAM đặc quyền tối thiểu | [NFR-003] | Đảm bảo không có lỗ hổng bảo mật mạng, tuân thủ OWASP Top 10, không có public IP trực tiếp cho tài nguyên tính toán nội bộ |
| Cấu hình Cloud SQL HA, PITR 24h, sao lưu hàng ngày | [NFR-002], [NFR-009] | Đảm bảo thời gian hoạt động 99.9% hàng năm, khả năng phục hồi thảm họa trong 24h, sao lưu sang vùng riêng để tránh mất dữ liệu khi toàn bộ vùng chính gặp sự cố |
| Cấu hình HPA GKE dựa trên CPU >70% và độ trễ >300ms, read replica Cloud SQL cho báo cáo | [NFR-004] | Đáp ứng yêu cầu tự động scale khi tải tăng, xử lý 10k người dùng đồng thời, giảm tải cho cơ sở dữ liệu chính khi chạy báo cáo |
| Cấu hình Docker đa giai đoạn, quy tắc .dockerignore, quét lỗ hổng hình ảnh tự động | [NFR-005] | Đáp ứng yêu cầu kích thước hình ảnh Docker base <200MB, hình ảnh cuối <500MB, đảm bảo hình ảnh không chứa lỗ hổng bảo mật |
| Cấu hình resource limits cho container, Cloud SQL instance type, Redis cluster size | [NFR-001] | Đảm bảo độ trễ API cốt lõi (xác thực, điểm danh, danh sách khóa học) trung bình dưới 200ms, hỗ trợ 10k người dùng đồng thời với truy vấn cơ sở dữ liệu dưới 1 giây |
| Cấu hình sao lưu cụm GKE sang vùng riêng, sao lưu Cloud SQL hàng ngày | [NFR-009] | Đảm bảo khả năng phục hồi thảm họa khi toàn bộ vùng chính gặp sự cố, tuân thủ yêu cầu sao lưu dữ liệu hàng ngày |

---

## 7. Phụ lục
### 7.1 Lệnh Terraform thường dùng
| Lệnh | Mô tả |
|------|-------|
| `terraform init` | Khởi tạo thư mục làm việc Terraform, tải các module cần thiết |
| `terraform plan` | Xem trước các thay đổi sẽ được áp dụng lên hạ tầng GCP |
| `terraform apply` | Áp dụng các thay đổi lên hạ tầng GCP |
| `terraform destroy` | Xóa toàn bộ tài nguyên được quản lý bởi Terraform (chỉ dùng cho môi trường test) |
| `terraform state list` | Liệt kê tất cả tài nguyên đang được quản lý bởi Terraform |

### 7.2 Lệnh kubectl thường dùng
| Lệnh | Mô tả |
|------|-------|
| `kubectl get pods -n membership-hub` | Liệt kê tất cả pod đang chạy trong namespace membership-hub |
| `kubectl logs -f <pod-name> -n membership-hub` | Xem log thời gian thực của một pod |
| `kubectl describe hpa -n membership-hub` | Xem chi tiết trạng thái HPA của tất cả dịch vụ |
| `kubectl rollout restart deployment -n membership-hub` | Khởi động lại tất cả deployment để áp dụng hình ảnh mới |
| `kubectl get events -n membership-hub --sort-by='.lastTimestamp'` | Xem sự kiện gần đây của namespace để xử lý sự cố |

### 7.3 Xử lý sự cố thường gặp
1. **Pod không khởi động**: Kiểm tra log pod bằng lệnh `kubectl logs`, xác minh biến môi trường và secret được cấu hình đúng, kiểm tra resource limits có vượt quá node capacity không
2. **HPA không scale**: Kiểm tra cấu hình metrics server trên GKE, xác minh ngưỡng CPU và độ trễ được cấu hình đúng, kiểm tra pod có đủ resource để scale không
3. **Sao lưu thất bại**: Kiểm tra quyền IAM của service account Cloud SQL, xác minh bucket Cloud Storage có quyền ghi không, kiểm tra dung lượng bucket còn đủ không
4. **Kết nối cơ sở dữ liệu thất bại**: Xác minh chuỗi kết nối được lưu trong Secret Manager đúng, kiểm tra firewall rule cho phép kết nối từ GKE đến Cloud SQL, kiểm tra Cloud SQL đang ở trạng thái running

### 7.4 Liên kết tài liệu liên quan
- [Kiến trúc Hệ thống Tổng quan](./system-architecture.md)
- [Tài liệu Tham chiếu API REST](./rest-api-reference.md)
- [Hướng dẫn Cài đặt Hệ thống](./installation-guide.md)
- [Hướng dẫn Sao lưu & Phục hồi Thảm họa](./backup-recovery-guide.md)
- [Chính sách Bảo mật Doanh nghiệp](../../security/enterprise-security-policy.md)