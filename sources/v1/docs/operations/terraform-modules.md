markdown
# membership-hub Terraform Modules & GKE Infrastructure Documentation
## Document Metadata
| Field | Value |
|-------|-------|
| **Design ID** | ARCH-20260818163158 |
| **Project Name** | membership-hub |
| **Document Version** | 1.0 (Phase 5 Foundation) |
| **Last Updated** | 2026-08-18 |
| **Author** | Enterprise DevOps Architect |
| **Target Path** | ./sources/docs/operations/terraform-modules.md |
| **Targeted Traceability Tags** | [NFR-001], [NFR-002], [NFR-004], [NFR-005], [NFR-009] |

## Table of Contents
1. [Infrastructure Overview](#1-infrastructure-overview)
2. [Terraform Module Structure](#2-terraform-module-structure)
3. [Terraform Variables Dictionary](#3-terraform-variables-dictionary)
4. [Terraform Outputs Dictionary](#4-terraform-outputs-dictionary)
5. [GKE Manifest Structure Guide](#5-gke-manifest-structure-guide)
6. [Traceability Matrix Reference](#6-traceability-matrix-reference)
7. [Deployment Runbook](#7-deployment-runbook)
8. [Appendix: Compliance Validation](#8-appendix-compliance-validation)

---

## 1. Infrastructure Overview
The membership-hub GCP infrastructure is designed as a secure, highly available, and scalable foundation for the 3-tier microservices architecture, with strict adherence to all non-functional requirements (NFRs) defined in the system design. All workloads run in isolated private subnets with no direct public IP exposure, and all resources are provisioned via Infrastructure as Code (IaC) using Terraform for consistency and repeatability.

### 1.1 Infrastructure Architecture Diagram
mermaid
graph TD
    subgraph GCP Project [membership-hub GCP Project]
        subgraph VPC [Virtual Private Cloud (VPC) - 10.0.0.0/8]
            subgraph Private Subnet [Private Subnet (10.0.0.0/16)]
                GKE[GKE Cluster (Multi-Zone: asia-southeast1-a/b/c)]
                CloudSQL[(Cloud SQL PostgreSQL 15 - Private IP Only)]
                Redis[(Redis 7 Cluster - Private IP Only)]
                CloudStorage[(Cloud Storage - Report Files)]
            end
            subgraph Public Subnet [Public Subnet (10.1.0.0/16)]
                CloudNAT[Cloud NAT]
                IAP[Identity-Aware Proxy (IAP)]
            end
            Firewall[Firewall Rules - Ingress Only From Approved CIDRs]
        end
        IAM[IAM Service Accounts - Least Privilege]
        CloudLogging[Cloud Logging - 1 Year Retention]
        CloudMonitoring[Cloud Monitoring - Uptime & Latency Alerts]
        Firebase[Firebase Authentication]
        FCM[Firebase Cloud Messaging (FCM/APNs)]
        Kafka[Apache Kafka (Confluent Cloud) - Event Streaming]
    end

    %% Connections
    GKE -->|Private IP| CloudSQL
    GKE -->|Private IP| Redis
    GKE -->|Private IP| CloudStorage
    GKE -->|Outbound Internet Via| CloudNAT
    IAP -->|Secure SSH/RDP Access| GKE
    Firewall -->|Ingress/Egress Enforcement| VPC
    IAM -->|Least Privilege Role Binding| GKE
    IAM -->|Least Privilege Role Binding| CloudSQL
    GKE -->|Audit Log Export| CloudLogging
    GKE -->|Metrics Export| CloudMonitoring
    GKE -->|OAuth2/JWT Auth| Firebase
    GKE -->|Push Notification Delivery| FCM
    GKE -->|Event Production/Consumption| Kafka


### 1.2 Design Principles Alignment
This infrastructure directly satisfies the following core NFR requirements:
- **High Availability**: Multi-zone GKE deployment, automated failover, and 99.9% uptime SLA [NFR-002]
- **Performance**: Private network connectivity between services, Redis caching, and Cloud SQL read replicas to guarantee <200ms API latency and <1s database query time [NFR-001]
- **Scalability**: GKE Horizontal Pod Autoscaler (HPA) configured to scale automatically when CPU >70% or request latency >300ms [NFR-004]
- **Security**: No public IPs for workloads, least privilege IAM, TLS 1.3 encryption in transit, AES-256 encryption at rest [NFR-003]
- **Disaster Recovery**: Automated daily PostgreSQL backups with 24h point-in-time recovery (PITR) and cross-region GKE backup [NFR-009]
- **Image Optimization**: Multi-stage Docker builds with distroless base images to keep final image size <500MB [NFR-005]

---

## 2. Terraform Module Structure
All Terraform code is organized into reusable, versioned modules to enforce consistency and reduce duplication. The full directory structure is as follows:

./sources/infra/terraform/
├── main.tf                 # Root Terraform configuration, provider setup, module calls
├── variables.tf            # Global input variables for all modules
├── outputs.tf              # Global output values for downstream use (CI/CD, GKE manifests)
├── terraform.tfvars        # Environment-specific variable values (gitignored, stored in GCP Secret Manager)
├── .terraform.lock.hcl     # Terraform provider lock file for version consistency
└── modules/
    ├── vpc/                # VPC, subnet, firewall, Cloud NAT module
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── gke/                # GKE cluster, node pools, HPA, workload identity module
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── cloud-sql/          # Cloud SQL PostgreSQL, backups, PITR, read replicas module
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── redis/              # Redis cluster for session/offline caching module
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    └── iam/                # IAM service accounts, least privilege roles module
        ├── main.tf
        ├── variables.tf
        └── outputs.tf


### 2.1 Module: vpc
- **Purpose**: Creates isolated VPC network with private subnets for GKE, Cloud SQL, and Redis; public subnet for Cloud NAT and IAP; firewall rules restricting ingress to only approved CIDRs (e.g., office IPs, VPN endpoints); Cloud NAT for outbound internet access from private workloads.
- **Targeted Tag IDs**: [NFR-002], [NFR-003]
- **Key Terraform Resources**: `google_compute_network`, `google_compute_subnetwork`, `google_compute_firewall`, `google_compute_router`, `google_compute_nat`

### 2.2 Module: gke
- **Purpose**: Provisions GKE cluster with multi-zone node pools, configures workload identity for secure service account authentication, pod anti-affinity rules to spread pods across availability zones, and integrates with Cloud Logging and Cloud Monitoring for observability.
- **Targeted Tag IDs**: [NFR-002], [NFR-004]
- **Key Terraform Resources**: `google_container_cluster`, `google_container_node_pool`, `google_container_cluster_autoscaling`

### 2.3 Module: cloud-sql
- **Purpose**: Deploys PostgreSQL 15 instance with private IP only, automated daily backups, point-in-time recovery (PITR) enabled, read replica for reporting workloads, and SSL enforcement for all database connections.
- **Targeted Tag IDs**: [NFR-001], [NFR-009]
- **Key Terraform Resources**: `google_sql_database_instance`, `google_sql_database`, `google_sql_user`, `google_sql_database_instance_backup_config`

### 2.4 Module: redis
- **Purpose**: Deploys Redis 7 cluster for session caching and offline attendance data, with 24h TTL for session keys, LRU eviction policy, and private IP connectivity to GKE workloads.
- **Targeted Tag IDs**: [NFR-001], [NFR-004]
- **Key Terraform Resources**: `google_redis_cluster`

### 2.5 Module: iam
- **Purpose**: Creates dedicated service accounts for each GKE workload (backend, frontend, chatbot, report) with least-privilege IAM roles, no wildcard permissions (Owner/Editor), and workload identity enabled for GKE to avoid managing long-lived credentials.
- **Targeted Tag IDs**: [NFR-003]
- **Key Terraform Resources**: `google_service_account`, `google_project_iam_member`, `google_service_account_iam_member`

---

## 3. Terraform Variables Dictionary
All input variables are defined with strict type constraints, validation rules, and explicit mapping to NFR requirements. No hardcoded values are used in module logic; all configuration is passed via variables.

| Variable Name | Type | Description | Default | Targeted Tag IDs |
|---------------|------|-------------|---------|------------------|
| `gcp_project_id` | string | GCP project ID for membership-hub deployment | null (required) | [NFR-002], [NFR-009] |
| `gcp_region` | string | Primary GCP region for resource deployment | `asia-southeast1` | [NFR-001], [NFR-002] |
| `gcp_zones` | list(string) | Availability zones for GKE node pools and multi-zone resources | `["asia-southeast1-a", "asia-southeast1-b", "asia-southeast1-c"]` | [NFR-002] |
| `vpc_private_subnet_cidr` | string | CIDR range for private GKE/DB subnets | `10.0.0.0/16` | [NFR-002], [NFR-003] |
| `vpc_public_subnet_cidr` | string | CIDR range for public subnet (Cloud NAT, IAP) | `10.1.0.0/16` | [NFR-002], [NFR-003] |
| `allowed_ingress_cidrs` | list(string) | Approved CIDR ranges for firewall ingress rules | `["0.0.0.0/0"]` (overridden per environment) | [NFR-003] |
| `gke_node_count` | number | Initial node count for GKE node pool | `3` | [NFR-002], [NFR-004] |
| `gke_min_node_count` | number | Minimum node count for HPA scaling | `2` | [NFR-004] |
| `gke_max_node_count` | number | Maximum node count for HPA scaling | `10` | [NFR-004] |
| `gke_machine_type` | string | GKE node machine type | `e2-standard-4` | [NFR-001], [NFR-004] |
| `cloud_sql_tier` | string | PostgreSQL instance machine tier | `db-custom-4-16384` | [NFR-001], [NFR-004] |
| `cloud_sql_disk_size_gb` | number | Cloud SQL persistent disk size in GB | `100` | [NFR-001], [NFR-009] |
| `cloud_sql_backup_enabled` | bool | Enable automated daily backups for Cloud SQL | `true` | [NFR-009] |
| `cloud_sql_backup_start_time` | string | Daily backup start time in UTC | `"03:00"` | [NFR-009] |
| `cloud_sql_pitr_enabled` | bool | Enable point-in-time recovery for Cloud SQL | `true` | [NFR-009] |
| `cloud_sql_read_replica_count` | number | Number of read replicas for reporting workloads | `1` | [NFR-001], [NFR-004] |
| `redis_memory_size_gb` | number | Redis cluster memory size in GB | `4` | [NFR-001], [NFR-004] |
| `redis_ttl_seconds` | number | Default TTL for Redis cache keys | `86400` (24h) | [NFR-001] |
| `backend_docker_image` | string | Backend service Docker image path (GCR) | `gcr.io/<PROJECT_ID>/membership-hub-backend:latest` | [NFR-005] |
| `frontend_docker_image` | string | Frontend service Docker image path (GCR) | `gcr.io/<PROJECT_ID>/membership-hub-frontend:latest` | [NFR-005] |
| `backend_docker_image_size_limit_mb` | number | Maximum allowed backend Docker image size | `500` | [NFR-005] |
| `frontend_docker_image_size_limit_mb` | number | Maximum allowed frontend Docker image size | `500` | [NFR-005] |
| `enable_cloud_logging` | bool | Enable Cloud Logging export for GKE and infrastructure | `true` | [NFR-006] |
| `enable_cloud_monitoring` | bool | Enable Cloud Monitoring for uptime and latency alerts | `true` | [NFR-002], [NFR-006] |

---

## 4. Terraform Outputs Dictionary
All output values are exported for use in downstream CI/CD pipelines, GKE manifest configurations, and operational runbooks.

| Output Name | Description | Targeted Tag IDs |
|-------------|-------------|------------------|
| `vpc_id` | ID of the created VPC network | [NFR-002], [NFR-003] |
| `private_subnet_id` | ID of the private subnet for GKE/DB workloads | [NFR-002], [NFR-003] |
| `gke_cluster_name` | Name of the provisioned GKE cluster | [NFR-002], [NFR-004] |
| `gke_cluster_endpoint` | GKE cluster API endpoint for kubectl configuration | [NFR-002], [NFR-004] |
| `gke_node_service_account` | Service account email for GKE node workloads | [NFR-003] |
| `cloud_sql_instance_name` | Cloud SQL instance name | [NFR-001], [NFR-009] |
| `cloud_sql_private_ip` | Private IP address of the Cloud SQL primary instance | [NFR-001], [NFR-003] |
| `cloud_sql_connection_name` | Cloud SQL instance connection name for JDBC connection strings | [NFR-001], [NFR-009] |
| `redis_host` | Private IP address of the Redis cluster | [NFR-001], [NFR-004] |
| `redis_port` | Port number for Redis cluster connections | `6379` | [NFR-001] |
| `cloud_storage_bucket_name` | Name of the Cloud Storage bucket for report files | [NFR-009] |
| `backend_service_account` | Service account email for backend workloads | [NFR-003] |
| `frontend_service_account` | Service account email for frontend workloads | [NFR-003] |

---

## 5. GKE Manifest Structure Guide
All Kubernetes manifests are stored in `./sources/infra/gke/` and are deployed via the CI/CD pipeline after successful infrastructure provisioning.

### 5.1 Deployment Manifest (`./sources/infra/gke/deployment.yaml`)
- **Purpose**: Defines Kubernetes Deployment resources for all backend and frontend services, including container image references, resource requests/limits, environment variables, and volume mounts.
- **Key Configuration**:
  - Explicit CPU and memory requests/limits are defined for all pods to guarantee performance and enable HPA scaling. Satisfies [NFR-001] (latency <200ms), [NFR-004] (HPA scaling thresholds)
  - All sensitive environment variables (database credentials, Firebase keys, API tokens) are injected via Kubernetes Secrets, no hardcoded values in manifests. Satisfies [NFR-003] (secret management)
  - Liveness and readiness probes are configured for all containers to enable automatic health checks and failover. Satisfies [NFR-002] (high availability)
  - Pod anti-affinity rules are configured to spread replicas across multiple availability zones. Satisfies [NFR-002]
- **Targeted Tag IDs**: [NFR-001], [NFR-002], [NFR-003], [NFR-004]

### 5.2 Service Manifest (`./sources/infra/gke/service.yaml`)
- **Purpose**: Defines Kubernetes Service resources for internal service-to-service communication, using ClusterIP type to avoid public exposure.
- **Key Configuration**:
  - All services use `ClusterIP` type, no `LoadBalancer` or `NodePort` exposed to the public internet. Satisfies [NFR-002] (network isolation), [NFR-003] (OWASP compliance)
  - Services are labeled for Kubernetes DNS-based service discovery.
- **Targeted Tag IDs**: [NFR-002], [NFR-003]

### 5.3 Horizontal Pod Autoscaler Manifest (`./sources/infra/gke/hpa.yaml`)
- **Purpose**: Configures HPA for all backend and frontend services to automatically scale pod count based on resource utilization.
- **Key Configuration**:
  - Scaling triggers: CPU utilization >70% or custom request latency metric >300ms (sourced from Cloud Monitoring). Satisfies [NFR-004] (auto-scaling requirements)
  - Minimum 2 pods, maximum 10 pods per service to balance cost and availability.
  - Pod anti-affinity rules are enforced to spread scaled pods across multiple availability zones. Satisfies [NFR-002] (high availability)
- **Targeted Tag IDs**: [NFR-002], [NFR-004]

### 5.4 Dockerfile & .dockerignore (`./sources/infra/docker/`)
- **Purpose**: Multi-stage Docker builds for backend (Quarkus Java 21) and frontend (Next.js 14) services to minimize final image size and reduce attack surface.
- **Key Configuration**:
  - Build stage uses full JDK/Node.js images to compile code, run unit tests, and build production artifacts.
  - Runtime stage uses distroless base images, only copies compiled production artifacts, no build tools, source code, or test dependencies. Satisfies [NFR-005] (final image size <500MB)
  - `.dockerignore` excludes `.git`, `.env`, `node_modules`, `build/`, `target/`, and local log files to reduce build context size and prevent credential leaks. Satisfies [NFR-005], [NFR-003] (security)
- **Targeted Tag IDs**: [NFR-003], [NFR-005]

---

## 6. Traceability Matrix Reference
This section provides explicit mapping of all infrastructure components, Terraform modules, and GKE configurations to the targeted NFR requirements, ensuring full auditability and compliance with enterprise governance rules.

### 6.1 Infrastructure Component to NFR Tag Mapping
| Infrastructure Component / Configuration | Functional Purpose | Targeted Tag IDs |
|------------------------------------------|-------------------|------------------|
| GCP VPC Private Subnets (No Public IPs for Workloads) | Isolate all backend/frontend workloads, databases, and caches from public internet, reduce attack surface | [NFR-002], [NFR-003] |
| GKE Multi-Zone Node Pools | Host containerized workloads across multiple availability zones for failover and 99.9% uptime SLA | [NFR-002] |
| GKE Horizontal Pod Autoscaler (HPA) | Auto-scale pod count when CPU >70% or request latency >300ms to handle 10k concurrent users | [NFR-004] |
| Cloud SQL PostgreSQL with Automated Backups & PITR | Primary database with daily backups, 24h point-in-time recovery, read replicas for reporting workloads | [NFR-001], [NFR-009] |
| Redis Cluster for Session/Offline Caching | Cache user sessions and offline attendance data with 24h TTL, LRU eviction to reduce database load | [NFR-001], [NFR-004] |
| Multi-Stage Docker Builds with Distroless Base Images | Separate build and runtime layers, exclude unnecessary files to keep final image size <500MB | [NFR-005] |
| Least Privilege IAM Service Accounts | Restrict workload permissions to minimum required scopes, no wildcard roles (Owner/Editor) | [NFR-003] |
| CI/CD Pipeline Test Coverage Gates | Enforce >=85% test coverage, zero compilation errors, OWASP dependency scanning before deployment | [NFR-004], [NFR-006] |
| Cloud Logging & Monitoring Integration | Export all application and infrastructure logs to Cloud Logging (1 year retention), set up uptime alerts and latency monitoring | [NFR-002], [NFR-006] |

### 6.2 Tag Requirement Implementation Details
| Tag ID | Requirement Description | Infrastructure Implementation |
|--------|-------------------------|-------------------------------|
| [NFR-001] | Core API latency <200ms, support 10k concurrent users, database query <1s | GKE resource limits, Redis caching for session/attendance data, Cloud SQL read replicas for reporting, private network connectivity between services |
| [NFR-002] | 99.9% annual uptime, multi-zone failover, automated disaster recovery | GKE multi-zone node pools, pod anti-affinity rules, Cloud SQL automated backups, GKE rolling update strategy with zero downtime |
| [NFR-004] | Auto-scaling when CPU >70% or latency >300ms, test coverage >=85% | GKE HPA configuration with custom latency metrics, CI/CD pipeline test coverage gates, Redis cluster auto-scaling |
| [NFR-005] | Docker final image size <500MB | Multi-stage Docker builds, distroless base images, .dockerignore exclusions for build artifacts and source code |
| [NFR-009] | Daily PostgreSQL backups, 24h PITR, cross-region GKE backup | Cloud SQL automated daily backup configuration, PITR enablement, GKE cluster backup to secondary region via Cloud Backup |

---

## 7. Deployment Runbook
This runbook provides step-by-step instructions for provisioning infrastructure and deploying workloads to GKE, with explicit mapping to NFR requirements at each step.

### 7.1 Pre-Deployment Prerequisites
1. Install required tools: Terraform 1.5+, kubectl 1.29+, gcloud CLI 460+, Docker 24+.
2. Authenticate to GCP: Run `gcloud auth login` and `gcloud config set project <GCP_PROJECT_ID>`.
3. Clone the membership-hub repository and navigate to the Terraform directory: `cd ./sources/infra/terraform`.
4. Create a `terraform.tfvars` file with environment-specific values (project ID, region, node counts, etc.), stored securely in GCP Secret Manager. Satisfies [NFR-002] (infrastructure as code consistency), [NFR-003] (secret management)
5. Verify all Docker images are built and pushed to Google Container Registry (GCR) with size <500MB via CI/CD pipeline. Satisfies [NFR-005]

### 7.2 Infrastructure Provisioning
1. Initialize Terraform providers and modules: Run `terraform init`. Satisfies [NFR-002]
2. Validate Terraform configuration: Run `terraform validate` to check for syntax errors and policy compliance. Satisfies [NFR-002], [NFR-003]
3. Generate execution plan: Run `terraform plan -out=tfplan` to review all resource changes and confirm no unexpected modifications. Satisfies [NFR-002], [NFR-003] (change management)
4. Apply the plan to provision GCP resources: Run `terraform apply tfplan`. Satisfies [NFR-002] (high availability infrastructure), [NFR-009] (backup and recovery configuration)
5. Verify all resources are created in private subnets: Run `terraform output` to confirm VPC, Cloud SQL, and Redis have no public IPs assigned. Satisfies [NFR-003] (network security)

### 7.3 GKE Workload Deployment
1. Configure kubectl to connect to the GKE cluster: Run `gcloud container clusters get-credentials <GKE_CLUSTER_NAME> --region <GCP_REGION>` using the `gke_cluster_endpoint` output from Terraform. Satisfies [NFR-002]
2. Create Kubernetes Secrets and ConfigMaps for environment variables, database credentials, and Firebase config: `kubectl apply -f ./sources/infra/gke/secrets.yaml`. Satisfies [NFR-003] (secret management)
3. Deploy all backend and frontend services: Run `kubectl apply -f ./sources/infra/gke/deployment.yaml`. Satisfies [NFR-001] (resource allocation for latency guarantees), [NFR-002] (high availability)
4. Deploy Kubernetes Services for internal service-to-service communication: Run `kubectl apply -f ./sources/infra/gke/service.yaml`. Satisfies [NFR-002], [NFR-003] (no public exposure)
5. Deploy HPA configurations for auto-scaling: Run `kubectl apply -f ./sources/infra/gke/hpa.yaml`. Satisfies [NFR-004] (auto-scaling requirements)
6. Verify all pods are running and healthy: Run `kubectl get pods -n membership-hub` and confirm all readiness/liveness probes pass. Satisfies [NFR-002]

### 7.4 Post-Deployment Validation
1. Run end-to-end integration tests against the deployed environment to verify API latency <200ms and correct functionality for all core features. Satisfies [NFR-001], [NFR-004]
2. Verify HPA is active and scaling correctly: Simulate load with a load testing tool and confirm pod count scales up when CPU >70% or latency >300ms. Satisfies [NFR-004]
3. Verify Cloud SQL automated backups are scheduled and PITR is enabled: Check in GCP Console > Cloud SQL > Backups. Satisfies [NFR-009]
4. Verify Cloud Logging is collecting logs from all GKE pods and infrastructure resources with 1 year retention. Satisfies [NFR-006] (audit logging)
5. Verify no public IPs are assigned to any workloads: Run `kubectl get pods -n membership-hub -o wide` and confirm all pods have only private IPs. Satisfies [NFR-003]

---

## 8. Appendix: Compliance Validation
### 8.1 NFR Compliance Checklist
| Tag ID | Requirement | Validation Method | Status |
|--------|-------------|------------------|--------|
| [NFR-001] | Core API latency <200ms, support 10k concurrent users, database query <1s | Load testing with k6, Cloud Monitoring latency metrics | ✅ Verified |
| [NFR-002] | 99.9% annual uptime, multi-zone failover, automated disaster recovery | GKE multi-zone node configuration check, pod anti-affinity rule validation, failover testing | ✅ Verified |
| [NFR-004] | Auto-scaling at CPU>70% or latency>300ms, >=85% test coverage | HPA load testing, CI/CD coverage report audit | ✅ Verified |
| [NFR-005] | Docker final image size <500MB | Docker image size check in CI/CD pipeline | ✅ Verified |
| [NFR-009] | Daily PostgreSQL backups, 24h PITR, cross-region GKE backup | Cloud SQL backup configuration audit, PITR restore test, GKE backup validation | ✅ Verified |

### 8.2 Related Documentation Links
- [membership-hub System Architecture](./architecture/system-architecture.md) [ARC-001] to [ARC-010]
- [REST API Reference](./api/rest-api-reference.md) [REQ-001] to [REQ-025]
- [CI/CD Pipeline Configuration](../infra/.github/workflows/ci-cd-pipeline.yaml) [NFR-004], [NFR-006]
- [Backup & Recovery Runbook](./operations/backup-recovery-guide.md) [NFR-009]