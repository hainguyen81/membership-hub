markdown
# membership-hub System Installation & Operations Guide
**Project ID:** ARCH-20260818163158  
**Version:** 1.0 (Production Baseline)  
**Last Updated:** 2026/08/18  
**Author:** Enterprise Technical Writing & System Architecture Team  
**Status:** Approved for Production Deployment  
**Covered Traceability Tags:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]

---

## Table of Contents
1. [Overview & Traceability Matrix Reference](#1-overview--traceability-matrix-reference)
2. [Prerequisites](#2-prerequisites)
3. [Environment Variable Configuration](#3-environment-variable-configuration)
4. [Database Initialization](#4-database-initialization)
5. [Docker Image Build & Push](#5-docker-image-build--push)
6. [GKE Cluster Deployment](#6-gke-cluster-deployment)
7. [DNS & Ingress Configuration](#7-dns--ingress-configuration)
8. [Post-Deployment Verification](#8-post-deployment-verification)
9. [Monitoring & Alerting Setup](#9-monitoring--alerting-setup)
10. [Backup & Disaster Recovery](#10-backup--disaster-recovery)
11. [Service Scaling Procedures](#11-service-scaling-procedures)
12. [Version Update & CI/CD Pipeline](#12-version-update--cicd-pipeline)
13. [Troubleshooting Guide](#13-troubleshooting-guide)
14. [Compliance & Security Audit Checklist](#14-compliance--security-audit-checklist)
15. [Appendix: File Path & Tag Index](#15-appendix-file-path--tag-index)

---

## 1. Overview & Traceability Matrix Reference
This guide provides end-to-end executable instructions for deploying, operating, and maintaining the membership-hub multi-tenant membership management system on Google Kubernetes Engine (GKE). All steps are aligned with the project's enterprise governance guardrails and mapped to the required architectural and non-functional requirement tags.

| Documentation Section | Mapped Traceability Tags | Purpose |
| :--- | :--- | :--- |
| Prerequisites | [ARC-010], [NFR-001], [NFR-002], [NFR-003] | Define required tooling and cloud account setup to meet performance, availability, and security requirements |
| Environment Configuration | [ARC-006], [NFR-003], [NFR-008] | Secure configuration of secrets, JWT settings, and GDPR/CCPA-aligned data handling parameters |
| Database Initialization | [DAT-001], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [ARC-010], [NFR-009] | Initialize PostgreSQL schema with all business entity tables, indexes, and constraints |
| Docker Build | [NFR-005] | Build multi-stage, minimal-footprint container images for all services |
| GKE Deployment | [NFR-002], [NFR-004], [ARC-010] | Deploy highly available, auto-scaling workloads with health checks and resource limits |
| DNS & Ingress | [NFR-002], [NFR-003], [NFR-007] | Configure secure, multi-tenant-aware ingress with TLS 1.3 and SEO-friendly routing |
| Monitoring | [NFR-001], [NFR-002], [NFR-006] | Set up latency, uptime, and audit log monitoring to meet performance and compliance requirements |
| Backup & Recovery | [NFR-009], [NFR-008] | Implement daily backups, PITR, and cross-region disaster recovery to meet data retention and GDPR requirements |
| Scaling | [NFR-004] | Configure horizontal pod autoscaling and manual scaling procedures for load spikes |
| CI/CD | [NFR-002], [NFR-005], [NFR-006] | Automate build, test, and deployment with quality gates for code coverage and image size |
| Troubleshooting | [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [ARC-001], [ARC-006], [ARC-007] | Resolve common operational and application-level faults |
| Compliance | [NFR-003], [NFR-008] | Validate adherence to OWASP Top 10, GDPR/CCPA, and enterprise security policies |

---

## 2. Prerequisites
All prerequisites are required to meet the system's non-functional requirements for security, performance, and availability.

### 2.1 Required Tooling
| Tool | Version | Purpose | Mapped Tags |
| :--- | :--- | :--- | :--- |
| Google Cloud SDK | 456.0.0+ | GCP resource provisioning and GKE management | [NFR-002], [NFR-004] |
| Terraform | 1.7.0+ | Infrastructure as Code (IaC) deployment | [NFR-002], [NFR-004], [NFR-009] |
| Docker | 24.0.0+ | Multi-stage container image building | [NFR-005] |
| kubectl | 1.29.0+ | Kubernetes cluster management | [NFR-002], [NFR-004] |
| Java Development Kit | 21 LTS | Backend service compilation | [ARC-010] |
| Node.js | 18.18.0+ | Frontend web and mobile app build | [ARC-010] |
| Firebase CLI | 12.0.0+ | Firebase Authentication and FCM configuration | [ARC-006], [REQ-021] |

### 2.2 GCP Account Setup
1. Create a new GCP project with ID `membership-hub-prod` [NFR-002]
2. Enable the following required APIs:
   - Cloud SQL API
   - GKE API
   - Cloud Storage API
   - Secret Manager API
   - Cloud Logging API
   - Cloud Monitoring API
   - Firebase Hosting API [ARC-006]
3. Create a service account with the following least-privilege roles (no wildcard permissions per [NFR-003]):
   - `roles/container.admin` (GKE cluster management)
   - `roles/cloudsql.admin` (PostgreSQL management)
   - `roles/secretmanager.admin` (Secret management)
   - `roles/monitoring.admin` (Monitoring and alerting)
   - `roles/storage.admin` (Backup storage)
4. Download the service account JSON key and store it securely in local path `./sources/infra/gcp/service-account.json` [NFR-003]

### 2.3 Firebase Project Setup
1. Create a Firebase project linked to the GCP project `membership-hub-prod` [ARC-006]
2. Enable Authentication providers: Email/Password, Google, Facebook [REQ-001], [REQ-002]
3. Generate FCM server key and APNs authentication key for push notifications [REQ-021], [ARC-008]
4. Create Zalo Official Account and obtain API key for group messaging [ARC-008]

---

## 3. Environment Variable Configuration
All environment variables are stored in GCP Secret Manager for production environments to avoid hardcoding sensitive values per [NFR-003]. Local development can use `.env` files excluded from version control via `.dockerignore`.

### 3.1 Backend Service (Quarkus) Environment Variables
| Variable Name | Purpose | Mapped Tags | Default Value |
| :--- | :--- | :--- | :--- |
| `QUARKUS_DATASOURCE_JDBC_URL` | PostgreSQL connection string | [ARC-010], [NFR-003] | `jdbc:postgresql://<db-ip>:5432/membership_hub` |
| `QUARKUS_DATASOURCE_USERNAME` | Database username | [NFR-003] | `membership_app` |
| `QUARKUS_DATASOURCE_PASSWORD` | Database password (stored in Secret Manager) | [NFR-003] | N/A (fetched from Secret Manager) |
| `JWT_SECRET` | JWT signing secret (256-bit minimum) | [ARC-006], [NFR-003] | N/A (fetched from Secret Manager) |
| `JWT_ACCESS_TOKEN_EXPIRY` | Access token expiration time (15 minutes) | [ARC-006] | `900` |
| `JWT_REFRESH_TOKEN_EXPIRY` | Refresh token expiration time (7 days) | [ARC-006] | `604800` |
| `FIREBASE_PROJECT_ID` | Firebase project ID for Auth integration | [ARC-006] | `membership-hub-prod` |
| `FCM_SERVER_KEY` | Firebase Cloud Messaging server key | [ARC-008], [REQ-021] | N/A (fetched from Secret Manager) |
| `ZALO_API_KEY` | Zalo Official Account API key | [ARC-008], [REQ-016] | N/A (fetched from Secret Manager) |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka cluster bootstrap servers | [ARC-008] | `kafka-service:9092` |
| `REDIS_HOST` | Redis cluster host for session caching | [ARC-009] | `redis-service:6379` |
| `REDIS_PASSWORD` | Redis authentication password | [NFR-003] | N/A (fetched from Secret Manager) |
| `PAYMENT_GATEWAY_API_KEY` | Payment gateway integration key | [REQ-015] | N/A (fetched from Secret Manager) |
| `AUDIT_LOG_RETENTION_DAYS` | Audit log retention period (1 year) | [NFR-006] | `365` |

### 3.2 Frontend Web (Next.js) Environment Variables
| Variable Name | Purpose | Mapped Tags | Default Value |
| :--- | :--- | :--- | :--- |
| `NEXT_PUBLIC_API_BASE_URL` | Backend API base URL | [ARC-010] | `https://api.membership-hub.com/v1` |
| `NEXT_PUBLIC_FIREBASE_API_KEY` | Firebase client API key | [ARC-006] | N/A (fetched from public Firebase config) |
| `NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN` | Firebase auth domain | [ARC-006] | `membership-hub-prod.firebaseapp.com` |
| `NEXT_PUBLIC_FIREBASE_PROJECT_ID` | Firebase project ID | [ARC-006] | `membership-hub-prod` |
| `NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID` | FCM sender ID | [ARC-008] | N/A (from Firebase project settings) |
| `NEXT_PUBLIC_DEFAULT_LOCALE` | Default locale (vi/en/es) | [NFR-007] | `vi` |
| `NEXT_PUBLIC_SUPPORTED_LOCALES` | Comma-separated list of supported locales | [NFR-007] | `vi,en,es` |

### 3.3 Mobile App (React Native) Environment Variables
| Variable Name | Purpose | Mapped Tags | Default Value |
| :--- | :--- | :--- | :--- |
| `API_BASE_URL` | Backend API base URL | [ARC-010] | `https://api.membership-hub.com/v1` |
| `FCM_SENDER_ID` | FCM sender ID for push notifications | [ARC-008], [REQ-021] | N/A (from Firebase project settings) |
| `OFFLINE_CACHE_TTL` | Offline data cache TTL (24 hours) | [ARC-009] | `86400` |
| `QR_SCANNER_TIMEOUT` | QR scan timeout (10 seconds) | [REQ-012] | `10000` |

---

## 4. Database Initialization
All database migrations are managed via Flyway and stored in the path `./sources/backend/membership-hub/src/main/resources/db/migration/`. Migrations must be run in sequential order to ensure schema integrity [ARC-010], [NFR-009].

### 4.1 Pre-Initialization Checks
1. Verify PostgreSQL 15+ is deployed on GCP Cloud SQL with the following configuration [NFR-003], [NFR-009]:
   - High availability enabled with automatic failover
   - AES-256 encryption at rest enabled
   - TLS 1.3 enforced for all connections
   - Automated daily backups enabled with 30-day retention
   - Point-in-Time Recovery (PITR) enabled with 24-hour recovery window [NFR-009]
2. Create the initial database `membership_hub` and user `membership_app` with least-privilege permissions (only DML/DDL access to required tables) [NFR-003]

### 4.2 Migration Execution Steps
Run the following migrations in order using the Flyway Maven plugin or CLI:
bash
# [DAT-001], [DAT-003] Initialize user, role, and center tables
mvn flyway:migrate -Dflyway.sqlMigrationPrefix=V1 -Dflyway.locations=filesystem:./sources/backend/membership-hub/src/main/resources/db/migration/

# [DAT-004], [DAT-005] Initialize course and enrollment tables
mvn flyway:migrate -Dflyway.sqlMigrationPrefix=V2

# [DAT-006] Initialize attendance table with idempotency constraint
mvn flyway:migrate -Dflyway.sqlMigrationPrefix=V3

# [DAT-007] Initialize student membership card table
mvn flyway:migrate -Dflyway.sqlMigrationPrefix=V4

# [DAT-008] Initialize notification table
mvn flyway:migrate -Dflyway.sqlMigrationPrefix=V5

# [DAT-009] Initialize promotion and announcement tables
mvn flyway:migrate -Dflyway.sqlMigrationPrefix=V6

# [DAT-010], [DAT-011] Initialize audit log and system settings tables
mvn flyway:migrate -Dflyway.sqlMigrationPrefix=V7


### 4.3 Post-Migration Validation
1. Verify all 11 core business tables are created with correct constraints and indexes [DAT-ALL]
2. Run the database integration test suite to validate schema integrity:
   bash
   ./sources/backend/membership-hub/mvnw test -Dtest=*IntegrationTest
   
   [DAT-001], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011]

---

## 5. Docker Image Build & Push
All services use multi-stage Docker builds to minimize image size and eliminate build-time dependencies, meeting the <500MB final image size requirement [NFR-005].

### 5.1 Global .dockerignore Rules
Create a `.dockerignore` file in the root of each service directory to prevent topological leaks:

.git
.env
node_modules
target/
build/
*.log
*.md
!README.md
sources/infra/gcp/service-account.json

[NFR-005]

### 5.2 Backend Service (Quarkus) Docker Build
Use the multi-stage Dockerfile at `./sources/infra/backend/Dockerfile`:
dockerfile
# [NFR-005] Stage 1: Build stage with full JDK 21
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw package -DskipTests -Dquarkus.package.type=uber-jar

# [NFR-005] Stage 2: Production stage with minimal distroless base image
FROM gcr.io/distroless/java21-debian12:latest
WORKDIR /runtime
COPY --from=builder /app/target/membership-hub-1.0.0-runner.jar ./app.jar
EXPOSE 8080
CMD ["app.jar"]

Build and push commands:
bash
# Build image
docker build -t gcr.io/membership-hub-prod/backend:1.0.0 ./sources/infra/backend/

# Validate image size (must be <500MB)
docker images | grep backend

# Push to Google Container Registry (GCR)
docker push gcr.io/membership-hub-prod/backend:1.0.0

[NFR-005]

### 5.3 Frontend Web (Next.js) Docker Build
Use the multi-stage Dockerfile at `./sources/infra/web-app/Dockerfile`:
dockerfile
# [NFR-005] Stage 1: Build stage with Node.js 18
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# [NFR-005] Stage 2: Production stage with minimal nginx-alpine image
FROM nginx:1.25-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

Build and push commands:
bash
docker build -t gcr.io/membership-hub-prod/web-app:1.0.0 ./sources/infra/web-app/
docker push gcr.io/membership-hub-prod/web-app:1.0.0

[NFR-005]

### 5.4 Mobile App (React Native) Docker Build
Use the multi-stage Dockerfile at `./sources/infra/mobile-app/Dockerfile`:
dockerfile
# [NFR-005] Stage 1: Build stage with Node.js 18
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build --platform android && npm run build --platform ios

# [NFR-005] Stage 2: Production stage with minimal base image
FROM alpine:3.19
RUN apk add --no-cache openjdk21
WORKDIR /app
COPY --from=builder /app/android ./android
COPY --from=builder /app/ios ./ios
EXPOSE 8081
CMD ["sh", "-c", "npx react-native start"]

Build and push commands:
bash
docker build -t gcr.io/membership-hub-prod/mobile-app:1.0.0 ./sources/infra/mobile-app/
docker push gcr.io/membership-hub-prod/mobile-app:1.0.0

[NFR-005]

---

## 6. GKE Cluster Deployment
All workloads are deployed to a private GKE cluster with no public IP exposure per [NFR-003]. The cluster is deployed using Terraform manifests at `./sources/infra/terraform/`.

### 6.1 Terraform Infrastructure Provisioning
1. Initialize Terraform working directory:
   bash
   cd ./sources/infra/terraform/
   terraform init
   
   [NFR-002], [NFR-004], [NFR-009]
2. Configure variables in `variables.tf` with region `asia-southeast1` (Ho Chi Minh City) for low latency to target users [NFR-001]
3. Plan and apply infrastructure changes:
   bash
   terraform plan -out=tfplan
   terraform apply tfplan
   
   This provisions the following resources [NFR-002], [NFR-004], [NFR-009]:
   - Private GKE cluster with 3 node pools (system, application, cache)
   - Cloud SQL PostgreSQL instance with high availability
   - Redis cluster for session caching [ARC-009]
   - Cloud Storage bucket for report exports and backups
   - VPC with private subnets for all workloads, no public IPs assigned [NFR-003]
   - Firewall rules allowing ingress only from approved CIDR ranges (no public access) [NFR-003]

### 6.2 Kubernetes Deployment Manifests
Deploy all services using the manifests at `./sources/infra/gke/`. All manifests include mandatory resource limits, health probes, and RBAC configuration per [NFR-002], [NFR-004], [ARC-001].

#### 6.2.1 Backend Service Deployment
yaml
# ./sources/infra/gke/backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend-service
  namespace: membership-hub
  labels:
    app: backend
    version: 1.0.0
spec:
  replicas: 3
  selector:
    matchLabels:
      app: backend
  # [NFR-002] Rolling update strategy for zero downtime deployments
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    metadata:
      labels:
        app: backend
    spec:
      # [NFR-003] Use dedicated service account with least privilege
      serviceAccountName: backend-service-account
      containers:
      - name: backend
        # [NFR-005] Use minimal distroless base image
        image: gcr.io/membership-hub-prod/backend:1.0.0
        ports:
        - containerPort: 8080
        # [NFR-004] Mandatory resource limits and requests
        resources:
          requests:
            cpu: "500m"
            memory: "512Mi"
          limits:
            cpu: "2000m"
            memory: "2Gi"
        # [NFR-002] Mandatory liveness and readiness probes
        livenessProbe:
          httpGet:
            path: /q/health/live
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /q/health/ready
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
        # [NFR-003] Inject secrets from GCP Secret Manager
        env:
        - name: QUARKUS_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: backend-secrets
              key: db-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: backend-secrets
              key: jwt-secret

[NFR-002], [NFR-003], [NFR-004], [NFR-005], [ARC-010]

#### 6.2.2 Horizontal Pod Autoscaler (HPA) Configuration
yaml
# ./sources/infra/gke/hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: backend-hpa
  namespace: membership-hub
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: backend-service
  # [NFR-004] Auto-scale based on CPU >70% or request latency >300ms
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Pods
    pods:
      metric:
        name: request_latency_seconds
      target:
        type: AverageValue
        averageValue: 0.3

[NFR-004]

#### 6.2.3 Service and Ingress Configuration
yaml
# ./sources/infra/gke/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: backend-service
  namespace: membership-hub
spec:
  # [NFR-003] Internal only service, no public exposure
  type: ClusterIP
  selector:
    app: backend
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP

[NFR-003]

Deploy all manifests with:
bash
kubectl apply -f ./sources/infra/gke/

[NFR-002], [NFR-004]

---

## 7. DNS & Ingress Configuration
All public traffic is routed through a GKE Ingress Gateway with TLS 1.3 enforcement and multi-tenant CORS configuration per [NFR-003], [NFR-007].

### 7.1 Cloud DNS Setup
1. Create a managed DNS zone for `membership-hub.com` in Cloud DNS [NFR-002]
2. Create A records pointing to the Ingress Gateway IP:
   - `api.membership-hub.com` → Backend service
   - `app.membership-hub.com` → Web frontend
   - `mobile.membership-hub.com` → Mobile app backend
3. Enable DNSSEC for DNS integrity [NFR-003]

### 7.2 Ingress TLS Configuration
1. Obtain a managed SSL certificate from Google Managed SSL Certificates for all domains [NFR-003]
2. Configure the Ingress resource to use the SSL certificate and enforce TLS 1.3 only, disable older TLS versions [NFR-003]
3. Configure CORS policy to allow only registered tenant origins (no wildcard `*` allowed) [NFR-003]

---

## 8. Post-Deployment Verification
Run the following verification steps to confirm all requirements are met:

### 8.1 Core Flow Verification
| Test Case | Steps | Expected Result | Mapped Tags |
| :--- | :--- | :--- | :--- |
| User Registration | Send POST request to `/api/v1/auth/register` with valid email/password | Returns 201 with JWT access token (15min expiry) and refresh token (7day expiry) | [REQ-001], [ARC-006] |
| OAuth2 Login | Send POST request to `/api/v1/auth/oauth2/google` with valid auth code | Returns 200 with JWT tokens, creates user if not exists | [REQ-002], [EXC-004] |
| RBAC Access | Access `/api/v1/admin/centers` with Student role JWT | Returns 403 Forbidden | [ARC-001], [ARC-002] |
| Course Creation | Send POST request to `/api/v1/courses` with valid payload as Center Admin | Returns 201, checks for teacher schedule conflict | [REQ-008], [EXC-001] |
| Attendance QR Scan | Send POST request to `/api/v1/attendance/scan` with valid QR payload | Returns 200 with attendance ID, returns DUPLICATE flag if scanned twice same day | [REQ-012], [REQ-013], [EXC-002] |
| Push Notification | Trigger course assignment event | FCM/APNs notification received on registered device | [REQ-016], [REQ-021], [EXC-003] |
| Membership Card Renewal | Send POST request to `/api/membership/renew` with valid payment transaction ID | Returns 200 with updated remaining days | [REQ-015] |

### 8.2 Performance Verification
1. Run load test with 10,000 concurrent users against core endpoints (auth, course list, attendance scan) [NFR-001]
2. Verify average API latency is <200ms for all core endpoints [NFR-001]
3. Verify database query latency is <1s for all reporting queries [NFR-001]

### 8.3 Security Verification
1. Run OWASP ZAP scan against all public endpoints to confirm no Top 10 vulnerabilities [NFR-003]
2. Verify TLS 1.3 is enforced, no older TLS versions are supported [NFR-003]
3. Verify all sensitive data (email, phone, address) is masked in logs [NFR-003], [NFR-006]
4. Verify CORS policy blocks unregistered tenant origins [NFR-003]

---

## 9. Monitoring & Alerting Setup
All monitoring and logging is configured via GCP Cloud Operations to meet audit and uptime requirements [NFR-002], [NFR-006].

### 9.1 Logging Configuration
1. Enable Cloud Audit Logs for all GCP services (admin activity, data access) [NFR-006]
2. Enable VPC Flow Logs for all VPC subnets to monitor network traffic [NFR-006]
3. Configure application logging to output all logs to `stdout`/`stderr` for GKE log collection [NFR-006]
4. Set audit log retention to 1 year to meet compliance requirements [NFR-006]
5. Configure log-based metrics for critical events:
   - `auth_failure_count`: Count of failed login attempts
   - `attendance_scan_count`: Count of QR scan events
   - `notification_failure_count`: Count of failed notification sends [EXC-003]

### 9.2 Alerting Configuration
Create the following alerts in Cloud Monitoring [NFR-002], [NFR-001]:
| Alert Name | Condition | Threshold | Mapped Tags |
| :--- | :--- | :--- | :--- |
| API Latency High | Average latency of `/api/v1/auth/login` > 200ms | 5 minutes | [NFR-001] |
| Service Down | Backend service readiness probe fails | 1 minute | [NFR-002] |
| DB Connection Error | PostgreSQL connection error rate > 1% | 2 minutes | [NFR-001] |
| Notification Failure | Notification send failure rate > 5% | 5 minutes | [EXC-003] |
| Disk Usage High | PostgreSQL disk usage > 80% | 10 minutes | [NFR-009] |
| Pod Count Low | Backend pod count < 3 | 1 minute | [NFR-002] |

---

## 10. Backup & Disaster Recovery
All backup and recovery procedures are designed to meet 99.9% uptime and GDPR/CCPA data retention requirements [NFR-002], [NFR-008], [NFR-009].

### 10.1 PostgreSQL Backup Configuration
1. Enable automated daily backups for Cloud SQL PostgreSQL with 30-day retention [NFR-009]
2. Enable Point-in-Time Recovery (PITR) with 24-hour recovery window to support data restoration to any point in the last 24 hours [NFR-009]
3. Configure cross-region backup replication to `asia-east1` (Taiwan) for disaster recovery [NFR-009]
4. Schedule weekly full backup exports to Cloud Storage bucket `gs://membership-hub-backups` with AES-256 encryption [NFR-003], [NFR-009]

### 10.2 Backup Execution Steps
Manual backup execution command:
bash
# [NFR-009] Create manual backup of PostgreSQL instance
gcloud sql backups create --instance=membership-hub-db --async

# [NFR-009] Export backup to Cloud Storage
gcloud sql export sql membership-hub-db gs://membership-hub-backups/manual-backup-$(date +%Y%m%d).sql --database=membership_hub


### 10.3 Disaster Recovery Procedure
1. **RTO (Recovery Time Objective):** <1 hour
2. **RPO (Recovery Point Objective):** <15 minutes
3. Recovery steps:
   a. Provision a new Cloud SQL instance in the secondary region [NFR-009]
   b. Restore the latest automated backup to the new instance [NFR-009]
   c. If PITR is required, restore to the specific point in time using the 24-hour recovery window [NFR-009]
   d. Update GKE environment variables to point to the new database instance [NFR-003]
   e. Verify data integrity by running the database integration test suite [DAT-ALL]
   f. Redirect DNS traffic to the secondary region if primary region is unavailable [NFR-002]

### 10.4 GDPR/CCPA Data Deletion
To support right-to-erasure requests per [NFR-008]:
1. Execute the stored procedure `sp_delete_user_data(user_id UUID)` to remove all user-related data from all tables
2. Verify deletion by querying the audit log for the user ID
3. Generate a deletion confirmation report and store it for 1 year [NFR-006]

---

## 11. Service Scaling Procedures
All scaling is configured to meet the 10,000 concurrent user requirement with <200ms latency [NFR-001], [NFR-004].

### 11.1 Automatic Scaling (HPA)
HPA is pre-configured for all backend and frontend services per Section 6.2.2. No manual intervention is required for normal load spikes. HPA will automatically scale pods when:
- CPU utilization exceeds 70% for 2 consecutive minutes [NFR-004]
- Request latency exceeds 300ms for 2 consecutive minutes [NFR-004]

### 11.2 Manual Scaling
For planned events (e.g., marketing campaigns, new course enrollment periods):
1. Scale backend service to desired replica count:
   bash
   kubectl scale deployment/backend-service --replicas=10 -n membership-hub
   
2. Scale web frontend service:
   bash
   kubectl scale deployment/web-app-service --replicas=5 -n membership-hub
   
3. Verify pod distribution across availability zones with:
   bash
   kubectl get pods -n membership-hub -o wide
   
[NFR-004]

### 11.3 Database Scaling
1. For read-heavy workloads (reporting, dashboard), add read replicas to the Cloud SQL instance [NFR-004]
2. Update the backend read replica connection string in the `QUARKUS_DATASOURCE_READONLY_JDBC_URL` environment variable [NFR-004]
3. Verify read replica lag is <100ms before routing read traffic to it [NFR-001]

---

## 12. Version Update & CI/CD Pipeline
All deployments are automated via GitHub Actions to ensure zero downtime and compliance with quality gates [NFR-002], [NFR-005], [NFR-006].

### 12.1 CI/CD Pipeline Configuration
The pipeline is defined in `./sources/infra/.github/workflows/ci-cd-pipeline.yaml` and includes the following mandatory gates [NFR-004], [NFR-005], [NFR-006]:
1. **Compilation Gate:** Zero compilation errors for all backend and frontend code
2. **Test Coverage Gate:** Minimum 85% code coverage for all unit and integration tests
3. **Security Scan Gate:** OWASP dependency check with zero critical vulnerabilities
4. **Image Size Gate:** Final Docker image size <500MB for all services [NFR-005]
5. **Lint Gate:** SonarQube quality gate with zero blocker issues

### 12.2 Deployment Process
1. Merge feature branch to `main` branch to trigger the pipeline
2. Pipeline automatically builds, tests, and pushes Docker images to GCR
3. Pipeline performs a rolling update of the GKE deployment with zero downtime [NFR-002]
4. Pipeline runs post-deployment verification tests (Section 8) to confirm functionality
5. If any gate fails, the pipeline aborts and notifies the DevOps team [NFR-006]

### 12.3 Rollback Procedure
If a deployment causes issues:
1. Trigger manual rollback via GitHub Actions UI to revert to the previous stable version
2. Alternatively, rollback via kubectl:
   bash
   kubectl rollout undo deployment/backend-service -n membership-hub
   kubectl rollout undo deployment/web-app-service -n membership-hub
   
3. Verify rollback success by running post-deployment verification tests [NFR-002]

---

## 13. Troubleshooting Guide
Common operational issues and their resolutions, mapped to relevant exception and architecture tags.

### 13.1 Authentication & Authorization Issues
| Issue | Root Cause | Resolution | Mapped Tags |
| :--- | :--- | :--- | :--- |
| JWT token expired error | Access token expired after 15 minutes | Use refresh token to obtain a new access token via `/api/v1/auth/refresh` endpoint | [ARC-006] |
| 403 Forbidden on admin endpoints | User lacks required RBAC role | Verify user role in database, assign correct role via `/api/v1/admin/users/{userId}/role` endpoint | [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005] |
| OAuth2 login failure | Invalid auth code or expired Firebase session | Re-authenticate with OAuth2 provider, verify Firebase configuration | [EXC-004], [ARC-006] |

### 13.2 Attendance & Course Issues
| Issue | Root Cause | Resolution | Mapped Tags |
| :--- | :--- | :--- | :--- |
| Duplicate attendance record error | Unique constraint violation on (studentId, courseId, attendanceDate) | System automatically returns DUPLICATE flag; no action required | [EXC-002], [REQ-013] |
| Attendance scan failed with network error | Mobile device lost network connection during scan | System queues scan request offline and auto-retries when connection is restored | [EXC-001], [REQ-012] |
| Course creation failed with 409 error | Teacher has schedule conflict with existing course | Reschedule teacher or assign a different teacher | [EXC-001], [REQ-008] |

### 13.3 Notification Issues
| Issue | Root Cause | Resolution | Mapped Tags |
| :--- | :--- | :--- | :--- |
| Push notification not received | Invalid FCM/APNs device token | Re-register device token via `/api/v1/notifications/register-token` endpoint | [EXC-003], [REQ-021] |
| Zalo group message failed | Invalid Zalo API key or rate limit exceeded | Verify Zalo API key in Secret Manager, wait for rate limit reset (1 hour) | [EXC-003], [ARC-008] |
| Notification retry count exceeded | Persistent failure to deliver notification (invalid token, API downtime) | Manually delete invalid device token from database, investigate API status | [EXC-003] |

### 13.4 Infrastructure Issues
| Issue | Root Cause | Resolution | Mapped Tags |
| :--- | :--- | :--- | :--- |
| High API latency | Database connection pool exhaustion or CPU overload | Scale HPA replicas, optimize slow database queries, add read replicas | [NFR-001], [NFR-004] |
| Pod crash loop | Out of memory or unhandled exception | Check pod logs via `kubectl logs <pod-name> -n membership-hub`, increase memory limits if OOM | [NFR-004] |
| Database connection timeout | PostgreSQL instance overloaded or network misconfiguration | Check Cloud SQL metrics, verify VPC peering configuration, restart Cloud SQL instance if needed | [NFR-001], [NFR-003] |

---

## 14. Compliance & Security Audit Checklist
All checks must be passed before production deployment and quarterly thereafter to meet OWASP, GDPR/CCPA, and enterprise security requirements [NFR-003], [NFR-008].

| Check Item | Verification Method | Mapped Tags |
| :--- | :--- | :--- |
| All secrets stored in GCP Secret Manager, no hardcoded credentials in code or manifests | Scan codebase for hardcoded secrets with GitGuardian | [NFR-003] |
| TLS 1.3 enforced for all public and internal traffic | Run SSL Labs scan on all public endpoints | [NFR-003] |
| AES-256 encryption enabled for data at rest (Cloud SQL, Cloud Storage) | Verify GCP resource encryption settings | [NFR-003] |
| All SQL queries use prepared statements, no raw string concatenation | Run static code analysis with SonarQube | [NFR-003] |
| All PII data (email, phone, address) is masked in logs | Review log samples for unmasked PII | [NFR-003], [NFR-006] |
| Audit logs retained for 1 year, immutable | Verify Cloud Logging retention settings | [NFR-006] |
| GDPR right-to-erasure functionality works as expected | Run test deletion flow and verify all user data is removed | [NFR-008] |
| RBAC rules enforced for all endpoints, no privilege escalation paths | Run penetration test for role bypass | [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005] |
| Daily backups completed successfully, PITR tested monthly | Review backup execution logs and run PITR drill | [NFR-009] |
| HPA scaling works as expected under load | Run load test and verify pod scaling | [NFR-004] |
| Docker image size <500MB for all services | Review image sizes in GCR | [NFR-005] |
| Multi-tenant CORS policy blocks unregistered origins | Test CORS with unregistered tenant origin | [NFR-003] |

---

## 15. Appendix: File Path & Tag Index
### 15.1 Core File Path Reference
| File Path | Purpose | Mapped Tags |
| :--- | :--- | :--- |
| `./sources/backend/membership-hub/src/main/resources/db/migration/V1__create_users_roles.sql` | User, role, center table migration | [DAT-001], [DAT-003] |
| `./sources/backend/membership-hub/src/main/resources/db/migration/V2__create_centers_courses_enrollments.sql` | Course and enrollment table migration | [DAT-004], [DAT-005] |
| `./sources/backend/membership-hub/src/main/resources/db/migration/V3__create_attendance.sql` | Attendance table migration | [DAT-006] |
| `./sources/backend/membership-hub/src/main/resources/db/migration/V4__create_student_cards.sql` | Membership card table migration | [DAT-007] |
| `./sources/backend/membership-hub/src/main/resources/db/migration/V5__create_notifications.sql` | Notification table migration | [DAT-008] |
| `./sources/backend/membership-hub/src/main/resources/db/migration/V6__create_promotions_announcements.sql` | Promotion and announcement table migration | [DAT-009] |
| `./sources/backend/membership-hub/src/main/resources/db/migration/V7__create_audit_system_settings.sql` | Audit log and system settings table migration | [DAT-010], [DAT-011] |
| `./sources/infra/terraform/main.tf` | GCP infrastructure IaC definition | [NFR-001], [NFR-002], [NFR-004], [NFR-009] |
| `./sources/infra/gke/deployment.yaml` | GKE workload deployment manifest | [NFR-002], [NFR-004] |
| `./sources/infra/gke/hpa.yaml` | Horizontal Pod Autoscaler configuration | [NFR-004] |
| `./sources/infra/docker/Dockerfile` | Multi-stage Docker build definition | [NFR-005] |
| `./sources/backend/auth-service/src/main/java/com/hub/auth/RbacFilter.java` | Global RBAC filter implementation | [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005] |
| `./sources/backend/auth-service/src/main/java/com/hub/auth/AuthService.java` | JWT authentication service implementation | [ARC-006] |
| `./sources/backend/attendance-service/src/main/java/com/hub/attendance/AttendanceService.java` | Idempotent attendance service implementation | [REQ-012], [REQ-013], [EXC-001], [EXC-002], [ARC-007] |
| `./sources/backend/notification-service/src/main/java/com/hub/notification/NotificationService.java` | Multi-channel notification service implementation | [REQ-016], [EXC-003], [ARC-008] |

### 15.2 Full Tag Index
| Tag Category | Tag ID | Description | Referenced Sections |
| :--- | :--- | :--- | :--- |
| Architecture | [ARC-001] | System Admin RBAC permissions | 6, 8, 13, 14 |
| Architecture | [ARC-002] | Center Admin RBAC permissions | 6, 8, 13, 14 |
| Architecture | [ARC-003] | Manager RBAC permissions | 6, 8, 13, 14 |
| Architecture | [ARC-004] | Teacher RBAC permissions | 6, 8, 13, 14 |
| Architecture | [ARC-005] | Student RBAC permissions | 6, 8, 13, 14 |
| Architecture | [ARC-006] | Firebase OAuth2/JWT authentication flow | 3, 5, 8, 13 |
| Architecture | [ARC-007] | Course, enrollment, attendance service architecture | 4, 8, 13 |
| Architecture | [ARC-008] | Multi-channel notification (FCM/APNs/Zalo) architecture | 3, 5, 8, 13 |
| Architecture | [ARC-009] | Redis offline caching and session management | 3, 6 |
| Architecture | [ARC-010] | Core tech stack (Quarkus, PostgreSQL, GKE) | 2, 4, 5, 6 |
| Non-Functional | [NFR-001] | API latency <200ms, 10k concurrent users | 2, 8, 9, 11, 13 |
| Non-Functional | [NFR-002] | 99.9% uptime, zero downtime deployments | 2, 6, 7, 9, 10, 12 |
| Non-Functional | [NFR-003] | Security (TLS 1.3, AES-256, OWASP Top 10) | 2, 3, 6, 7, 10, 14 |
| Non-Functional | [NFR-004] | Auto-scaling, read replicas, 85% test coverage | 2, 6, 11, 12 |
| Non-Functional | [NFR-005] | Docker image size <500MB | 5, 12, 14 |
| Non-Functional | [NFR-006] | 1-year audit log retention | 3, 9, 12, 14 |
| Non-Functional | [NFR-007] | Multi-language (en/vi/es) and SEO support | 7 |
| Non-Functional | [NFR-008] | GDPR/CCPA compliance, right to erasure | 3, 10, 14 |
| Non-Functional | [NFR-009] | Daily backups, PITR, cross-region DR | 4, 6, 10, 14 |
| Data | [DAT-001] to [DAT-011] | Database schema and table definitions | 4, 8, 15 |
| Exception | [EXC-001] | Teacher schedule conflict error | 8, 13 |
| Exception | [EXC-002] | Duplicate attendance scan error | 8, 13 |
| Exception | [EXC-003] | Notification delivery failure error | 8, 9, 13 |
| Exception | [EXC-004] | OAuth2 authentication failure error | 8, 13 |
| Exception | [EXC-005] | System recovery after outage error | 8, 10 |

---

## Document Control
| Version | Date | Author | Changes |
| :--- | :--- | :--- | :--- |
| 1.0 | 2026/08/18 | Enterprise Technical Writing Team | Initial production-ready release |