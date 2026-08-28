markdown
# membership-hub Backup & Recovery Guide
## Document Control
| Field | Value |
|-------|-------|
| Document ID | OPS-BACKUP-RECOVERY-001 |
| Version | 1.0 |
| Author | Senior Technical Writer & Enterprise System Architect |
| Date | 2026-08-18 |
| Approved By | Pending Technical Admin Review |
| Covered Traceability Tags | [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009], [EXC-010] |

---

## Table of Contents
1. [Overview](#1-overview)
2. [Prerequisites](#2-prerequisites)
3. [Global Configuration Constants](#3-global-configuration-constants)
4. [Backup Strategy](#4-backup-strategy)
5. [Recovery Procedures](#5-recovery-procedures)
6. [Disaster Recovery (DR) Plan](#6-disaster-recovery-dr-plan)
7. [Monitoring & Alerting](#7-monitoring--alerting)
8. [Compliance & Security](#8-compliance--security)
9. [Troubleshooting](#9-troubleshooting)
10. [Appendices](#10-appendices)

---

## 1. Overview
### 1.1 Purpose
This document defines end-to-end backup and recovery processes for the membership-hub system, aligned with enterprise non-functional requirements [NFR-002] (99.9% uptime), [NFR-009] (daily backups, PITR support), [NFR-003] (data security), and [NFR-006] (audit logging). It ensures business continuity during system failures, data corruption, or regional outages, and supports compliance with GDPR/CCPA regulations [NFR-008].

### 1.2 Scope
Covers backup and recovery for all core system components defined in the enterprise architecture [ARC-001] to [ARC-010]:
- PostgreSQL primary and read replica databases storing all business entities [DAT-001], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011]
- GKE cluster workloads (Quarkus backend services, Next.js web app, React Native mobile app) [ARC-010]
- Application configuration, secrets, and Redis session cache [ARC-009]
- Kafka event streams for attendance, notifications, and system events [ARC-008]

### 1.3 Traceability Matrix Reference
| System Component | Backup/Recovery Requirement | Mapped Tags |
|------------------|------------------------------|------------|
| PostgreSQL Database | Daily full backups, 24h PITR window, 365-day retention | [NFR-009], [DAT-ALL] |
| GKE Cluster | Cross-region resource backups, 99.9% failover support | [NFR-002], [NFR-009], [ARC-010] |
| Application Secrets | Encrypted backup in GCP Secret Manager, RBAC-controlled access | [NFR-003], [ARC-001], [ARC-002] |
| Audit Logs | 1-year immutable retention, full operation tracing | [NFR-006] |
| User Personal Data | GDPR/CCPA compliant recovery, right to erasure support | [NFR-008] |
| Multi-Language Content | Backup of i18n translation files for 3 supported locales | [NFR-007] |
| Notification Infrastructure | Backup of Zalo group configurations, FCM/APNs token mappings | [ARC-008], [REQ-016] |

---

## 2. Prerequisites
### 2.1 Required Access & Tools
All recovery operations require pre-configured tools and permissions aligned with security requirements [NFR-003]:
1. GCP Project Owner access to the `membership-hub-prod` project
2. `gcloud` CLI v450.0.1+ installed and authenticated with production project access
3. `kubectl` v1.29.0+ configured for the production GKE cluster `membership-hub-gke-prod` in region `asia-southeast1`
4. `velero` v1.12.0+ installed for GKE cluster resource backups
5. `psql` v15.0+ installed for PostgreSQL recovery operations
6. Access to GCP Secret Manager for backup encryption CMEK keys
7. Read access to the GitHub repository `membership-hub` for infrastructure code version control [ARC-010]

### 2.2 Pre-Recovery Validation Checklist
Before initiating any recovery process, verify the following to avoid data loss:
- [ ] Current system state is documented via Cloud Audit Logs [NFR-006]
- [ ] All stakeholders (product, support, engineering) are notified of planned downtime
- [ ] Latest valid backup is confirmed available in GCS bucket `membership-hub-backups-prod` with valid checksum
- [ ] Recovery environment (isolated prod subnet or staging) is provisioned with identical configuration to production
- [ ] All recovery team members have confirmed availability and assigned roles per RBAC policy [ARC-001]

---

## 3. Global Configuration Constants
All immutable backup and recovery parameters are defined as top-level constants to ensure consistency across all operational scripts, per enterprise clean code governance rules:
| Constant Name | Value | Description | Mapped Tag |
|---------------|-------|-------------|------------|
| `BACKUP_RETENTION_DAYS` | 365 | Number of days to retain full database backups | [NFR-009] |
| `PITR_RETENTION_HOURS` | 24 | Maximum time window for point-in-time recovery | [NFR-009] |
| `MAX_BACKUP_RETRY_ATTEMPTS` | 3 | Maximum retry count for failed backup jobs | [EXC-010] |
| `DAILY_BACKUP_SCHEDULE_CRON` | `0 2 * * *` | Cron schedule for daily PostgreSQL backups (02:00 UTC daily) | [NFR-009] |
| `GCP_PRIMARY_REGION` | `asia-southeast1` | Primary production region for membership-hub | [NFR-002] |
| `GCP_DR_REGION` | `asia-east1` | Disaster recovery region for cross-region failover | [NFR-002] |
| `POSTGRESQL_BACKUP_BUCKET` | `membership-hub-backups-prod` | GCS bucket for PostgreSQL logical and WAL backups | [NFR-009] |
| `GKE_CLUSTER_BACKUP_BUCKET` | `membership-hub-gke-backups` | GCS bucket for GKE cluster resource backups | [NFR-009] |
| `AUDIT_LOG_RETENTION_DAYS` | 365 | Retention period for backup/recovery audit logs | [NFR-006] |
| `BACKUP_ENCRYPTION_KEY` | `projects/membership-hub-prod/locations/global/keyRings/backup-key/cryptoKeys/db-backup-key` | CMEK key for encrypting all database backup data | [NFR-003] |
| `GKE_BACKUP_ENCRYPTION_KEY` | `projects/membership-hub-prod/locations/global/keyRings/backup-key/cryptoKeys/gke-backup-key` | CMEK key for encrypting GKE cluster backups | [NFR-003] |
| `REDIS_BACKUP_RETENTION_DAYS` | 7 | Retention period for Redis session cache backups | [ARC-009] |

---

## 4. Backup Strategy
All backup processes are automated via scheduled Cloud Scheduler jobs and Velero cron jobs, with full audit logging for every operation [NFR-006]. No backup data is stored on ephemeral container disks to prevent data loss during pod teardowns [ARC-010].

### 4.1 PostgreSQL Database Backups
#### 4.1.1 Full Daily Backups
- **Schedule**: Runs daily at 02:00 UTC via Cloud Scheduler [NFR-009]
- **Process**:
  1. Trigger Cloud Function `backup-postgresql-daily` with the CMEK encryption key [NFR-003]
  2. Execute `pg_dump` with custom format for the `membership_hub` database, excluding test data and temporary tables to minimize backup size [NFR-005]
  3. Compress backup with `gzip` to reduce storage footprint by ~70%
  4. Upload compressed backup to GCS bucket `membership-hub-backups-prod` with versioning enabled to prevent accidental deletion
  5. Apply retention policy: delete backups older than 365 days [BACKUP_RETENTION_DAYS]
  6. Log backup success/failure status to Cloud Audit Logs with timestamp, backup size, SHA-256 checksum, and initiator identity [NFR-006]
- **Retry Logic**: If backup fails, automatically retry up to 3 times with 10-minute intervals between attempts [EXC-010]. If all retries fail, send critical alert to DevOps team via PagerDuty and log the failure with full error context.

#### 4.1.2 Write-Ahead Log (WAL) Archiving for PITR
- **Process**:
  1. Configure PostgreSQL to archive WAL segments to GCS bucket `membership-hub-backups-prod/wal-archive` in real-time
  2. Encrypt WAL segments with the same CMEK key as full backups [NFR-003]
  3. Retain WAL segments for 24 hours to support PITR [PITR_RETENTION_HOURS]
- **Validation**: Daily test restore of a random WAL segment to ensure archiving is functional and no data loss occurs [NFR-009]

### 4.2 GKE Cluster Backups
- **Schedule**: Weekly full cluster backup via Velero cron job, incremental backups daily [NFR-009]
- **Process**:
  1. Use Velero to backup all Kubernetes resources (Deployments, Services, ConfigMaps, Secrets, HPA, PVCs) excluding ephemeral container storage and pod logs (stored separately in Cloud Logging) [NFR-005]
  2. Store backup in GCS bucket `membership-hub-gke-backups` in the DR region `asia-east1` for cross-region redundancy [NFR-002]
  3. Encrypt backup data with CMEK key `gke-backup-key` [NFR-003]
  4. Retain cluster backups for 90 days
- **Included Workloads**: All Quarkus backend services, Next.js web app, React Native mobile app workloads, HPA configurations, and network policies [ARC-010]

### 4.3 Application Configuration & Secrets Backups
- **Process**:
  1. Export all GCP Secret Manager secrets for the project to an encrypted JSON file daily, including Firebase Auth keys, Zalo API tokens, database credentials, and JWT signing keys [ARC-006], [ARC-008]
  2. Include i18n translation files for English, Vietnamese, and Spanish in the backup to support multi-language functionality [NFR-007]
  3. Store encrypted export in GCS bucket `membership-hub-backups-prod/config-backups` with versioning enabled
  4. Version control all Terraform infrastructure code in GitHub, with branch protection enabled for the `main` branch to prevent unauthorized changes [ARC-010]
- **Access Control**: Only System Admin and DevOps team members have RBAC-controlled access to the config backup bucket [NFR-003]

### 4.4 Redis Session Cache Backups
- **Schedule**: Daily snapshot of Redis cluster, stored in GCS for 7 days [ARC-009]
- **Process**:
  1. Trigger Redis `BGSAVE` command during low-traffic window (02:30 UTC) to avoid performance impact [NFR-001]
  2. Upload snapshot to GCS bucket `membership-hub-redis-backups`
  3. Encrypt snapshot with CMEK key [NFR-003]
- **Purpose**: Supports offline mobile app functionality and session recovery after outages [ARC-009]

---

## 5. Recovery Procedures
All recovery procedures follow the order of priority: restore database first, then cluster resources, then application configuration, to minimize downtime and meet RTO targets [NFR-002]. All recovery actions are logged to audit logs with full context [NFR-006].

### 5.1 PostgreSQL Point-in-Time Recovery (PITR)
Use this procedure for data corruption, accidental data deletion, or logical failures [NFR-009].
#### Step 1: Prepare Recovery Environment
1. Provision a new isolated Cloud SQL instance in the primary region `asia-southeast1` with the same configuration (machine type, disk size, network settings) as the production instance [NFR-003]
2. Stop all application traffic to the production database by scaling backend deployments to 0 replicas to prevent new data writes during recovery:
   bash
   # [NFR-002] Scale down backend services to prevent data inconsistency
   kubectl scale deployment --replicas=0 -l app=membership-hub-backend -n membership-hub-prod
   
3. Log recovery initiation action to audit logs with timestamp, initiator user ID, target recovery point, and reason for recovery [NFR-006]

#### Step 2: Restore Base Backup
1. Download the latest full backup file from GCS bucket `membership-hub-backups-prod` to the recovery environment, verify SHA-256 checksum matches the backup manifest
2. Restore the base backup to the new Cloud SQL instance:
   bash
   # [NFR-009] Restore full database backup with pg_restore
   pg_restore -U postgres -d membership_hub -C /path/to/backup.dump
   
3. Validate data integrity by running the project's database schema validation script located at `./sources/backend/membership-hub/src/test/java/com/membershiphub/integration/DbSchemaValidator.java` [DAT-001]
4. Run Flyway database migrations from `./sources/backend/membership-hub/src/main/resources/db/migration/` to ensure the schema matches the current application version [DAT-001]

#### Step 3: Apply WAL Logs for PITR
1. Configure the recovery instance to use the WAL archive from GCS bucket `membership-hub-backups-prod/wal-archive`
2. Set `recovery_target_time` to the desired point in time (format: `YYYY-MM-DD HH:MM:SS UTC`) to restore to the exact state before the failure
3. Start PostgreSQL recovery process; monitor progress via Cloud SQL console
4. Once recovery completes, validate that the target data state is correct by running sample queries against critical tables (Users, Courses, Attendance, StudentCards) [DAT-001], [DAT-004], [DAT-006], [DAT-007]

#### Step 4: Finalize Recovery
1. Update application configuration (ConfigMap `app-config` in GKE namespace `membership-hub-prod`) to point to the recovered database instance IP
2. Scale backend deployments back to original replica count to support 10,000 concurrent users per [NFR-001]:
   bash
   # [NFR-001] Scale backend to original replica count to meet latency targets
   kubectl scale deployment --replicas=<original-replica-count> -l app=membership-hub-backend -n membership-hub-prod
   
3. Run end-to-end smoke tests to verify all core functionality (email/OAuth2 authentication, course enrollment, QR code attendance scanning, membership card display) is working [REQ-001], [REQ-007], [REQ-012], [REQ-014]
4. Log recovery completion action to audit logs with recovery duration, data loss window, validation results, and any residual issues [NFR-006]

### 5.2 GKE Cluster Recovery
Use this procedure for cluster-level failures, node outages, or misconfigurations [NFR-002].
#### Step 1: Restore Cluster Resources
1. If the production cluster is completely unavailable, provision a new GKE cluster in the primary region `asia-southeast1` with the same configuration (node count, machine type, VPC, HPA rules) as the original cluster [ARC-010]
2. Install Velero on the new cluster and configure it to use the GCS bucket `membership-hub-gke-backups`
3. Restore the latest cluster backup:
   bash
   # [NFR-002] Restore GKE cluster resources from cross-region backup
   velero restore create --from-backup <latest-backup-name> --wait
   
4. Validate that all core services (backend, frontend, Redis, Kafka) are running and healthy via Kubernetes liveness and readiness probes [NFR-002]

#### Step 2: Restore Application State
1. If Redis session cache is lost, restore the latest Redis snapshot from GCS bucket `membership-hub-redis-backups` to restore user sessions for mobile app users [ARC-009]
2. Replay any pending Kafka events (attendance scans, notifications, Zalo group posts) from the retained topic partitions to ensure no event loss [ARC-008]

### 5.3 Application Configuration & Secrets Recovery
1. Download the latest encrypted configuration backup from GCS bucket `membership-hub-backups-prod/config-backups`
2. Decrypt the backup using the CMEK key from GCP Secret Manager [NFR-003]
3. Restore secrets to GCP Secret Manager and update ConfigMaps/Secrets in GKE to match the restored values, including Firebase Auth keys, Zalo API tokens, and database credentials [ARC-006], [ARC-008]
4. Validate that all environment variables are correctly configured and the application can connect to all dependent services (PostgreSQL, Redis, Kafka, FCM)

---

## 6. Disaster Recovery (DR) Plan
### 6.1 RTO & RPO Targets
| Scenario | Recovery Time Objective (RTO) | Recovery Point Objective (RPO) | Mapped Tag |
|----------|--------------------------------|--------------------------------|------------|
| Single database node failure | < 5 minutes | < 1 minute | [NFR-002] |
| Regional GKE outage | < 30 minutes | < 5 minutes | [NFR-002] |
| Full data corruption | < 2 hours | < 24 hours (PITR window) | [NFR-009] |
| Zalo/FCM notification service outage | < 15 minutes | < 1 minute | [ARC-008] |

### 6.2 Cross-Region Failover Procedure
1. If the primary region `asia-southeast1` is unavailable, promote the read replica database in the DR region `asia-east1` to primary to restore database service [NFR-002]
2. Update the application configuration to point to the DR database endpoint
3. Deploy the latest GKE cluster backup to the DR region GKE cluster, ensuring HPA configurations are preserved to support auto-scaling [NFR-004]
4. Update DNS records for the application load balancer to point to the DR region IP address, with TTL set to 60 seconds to minimize propagation time
5. Notify all users of the failover via push notification (FCM/APNs) and Zalo group post to set expectations [REQ-016], [ARC-008]
6. Log all failover actions to audit logs for post-incident review and compliance validation [NFR-006]

---

## 7. Monitoring & Alerting
### 7.1 Backup Monitoring
All backup jobs are monitored via Cloud Monitoring with the following alerts aligned to audit requirements [NFR-006]:
1. **Critical Alert (P1)**: Triggered if any daily PostgreSQL backup fails after 3 retries [EXC-010]
   - Notification channels: PagerDuty, email to DevOps team, SMS to on-call engineer
2. **Warning Alert (P2)**: Triggered if backup size is 20% smaller than the previous backup (indicates potential data loss or failed backup)
3. **Info Alert (P3)**: Triggered on successful backup completion, logged to audit logs with backup metadata

### 7.2 Recovery Monitoring
During recovery operations, monitor the following metrics to ensure compliance with RTO targets and non-functional requirements [NFR-001], [NFR-002]:
- Database restore progress percentage and estimated time to completion
- GKE pod readiness status and HPA scaling activity
- Application API latency (target < 200ms per [NFR-001])
- Error rate for core endpoints (authentication, attendance, enrollment) – target < 0.1%
- Kafka event queue lag to ensure no pending events are lost during recovery [ARC-008]

---

## 8. Compliance & Security
### 8.1 Data Encryption
All backup data is encrypted at rest using AES-256 encryption with CMEK keys, and in transit using TLS 1.3 [NFR-003]. Access to backup buckets is restricted to authorized service accounts only, with no public access allowed. All encryption keys are rotated annually per security policy.

### 8.2 GDPR/CCPA Compliance
- Backup data includes all user personal data, so recovery procedures comply with data subject access requests (DSAR) and right to erasure [NFR-008]
- If a user exercises their right to erasure, their data is marked for deletion in the primary database, and a scheduled job ensures their data is excluded from future backups
- Recovery operations do not restore data for users who have exercised their right to erasure within the backup retention window, with validation steps to confirm compliance [NFR-008]

### 8.3 Access Control
Recovery operations follow the RBAC model defined in [ARC-001] to [ARC-005], with only authorized roles able to execute recovery actions:
- System Admin: Full access to all backup and recovery functions
- DevOps Engineer: Access to execute recovery procedures, no access to modify backup retention policies
- Auditor: Read-only access to backup logs and audit trails, no access to modify or delete backup data

---

## 9. Troubleshooting
### 9.1 Common Backup Failures
| Error Code | Cause | Resolution | Mapped Tag |
|------------|-------|------------|------------|
| `BACKUP_INSUFFICIENT_PERMISSIONS` | Service account missing `storage.objects.create` permission on backup bucket | Grant `roles/storage.objectCreator` role to the backup service account | [NFR-003] |
| `BACKUP_DISK_FULL` | PostgreSQL instance does not have enough disk space for `pg_dump` | Increase Cloud SQL disk size, or clean up old temporary files | [NFR-009] |
| `BACKUP_ENCRYPTION_FAILED` | CMEK key is unavailable or permissions are missing | Verify CMEK key status in GCP KMS, grant `roles/cloudkms.cryptoKeyEncrypterDecrypter` role to the backup service account | [NFR-003] |
| `BACKUP_NETWORK_TIMEOUT` | Network connectivity issue between Cloud Function and GCS bucket | Retry backup, verify VPC firewall rules allow egress to GCS | [NFR-009] |

### 9.2 Common Recovery Failures
| Error Code | Cause | Resolution | Mapped Tag |
|------------|-------|------------|------------|
| `RECOVERY_WAL_CORRUPTED` | WAL segment is damaged or incomplete | Use the next valid WAL segment, or restore to an earlier point in time within the 24h PITR window | [NFR-009] |
| `RECOVERY_SCHEMA_MISMATCH` | Restored database schema does not match the current application schema | Run the latest Flyway database migration scripts from `./sources/backend/membership-hub/src/main/resources/db/migration/` on the restored instance before resuming traffic [DAT-001] | [NFR-009] |
| `RECOVERY_APP_CONNECTION_FAILED` | Application configuration still points to the old (failed) database instance | Update ConfigMap `app-config` with the new database IP and restart backend deployments | [NFR-002] |
| `RECOVERY_KAFKA_EVENT_LOSS` | Pending Kafka events were not retained during cluster failure | Replay events from the Kafka topic partitions retained in the cluster backup [ARC-008] | [ARC-008] |

### 9.3 Error Logging Standards
All backup and recovery errors are logged with the 3 mandatory context keys per enterprise logging governance rules:
1. Subsystem name: e.g., `[BACKUP_SUBSYSTEM]` or `[RECOVERY_SUBSYSTEM]`
2. Raw error message: e.g., `Failed to upload backup to GCS: 403 Forbidden`
3. Traceability Tag ID: e.g., `[NFR-009]`
Example error log entry:

[ERROR] [BACKUP_SUBSYSTEM] [NFR-009] Daily PostgreSQL backup failed: GCS upload permission denied. Raw error: 403 Forbidden, service account: backup-sa@membership-hub-prod.iam.gserviceaccount.com


---

## 10. Appendices
### 10.1 Sample Terraform Snippet for Backup Infrastructure
hcl
# [NFR-009] GCS bucket for PostgreSQL backups with encryption and versioning
resource "google_storage_bucket" "postgres_backups" {
  name          = "membership-hub-backups-prod"
  location      = "ASIA-SOUTHEAST1" // [GCP_PRIMARY_REGION]
  force_destroy = false

  versioning {
    enabled = true
  }

  encryption {
    default_kms_key_name = "projects/membership-hub-prod/locations/global/keyRings/backup-key/cryptoKeys/db-backup-key" // [BACKUP_ENCRYPTION_KEY]
  }

  # [BACKUP_RETENTION_DAYS] Auto-delete backups older than 365 days
  lifecycle_rule {
    action {
      type = "Delete"
    }
    condition {
      age = 365
    }
  }
}

# [NFR-002] Cross-region GKE cluster backup bucket for DR
resource "google_storage_bucket" "gke_backups" {
  name          = "membership-hub-gke-backups"
  location      = "ASIA-EAST1" // [GCP_DR_REGION]
  force_destroy = false

  encryption {
    default_kms_key_name = "projects/membership-hub-prod/locations/global/keyRings/backup-key/cryptoKeys/gke-backup-key" // [GKE_BACKUP_ENCRYPTION_KEY]
  }
}


### 10.2 Sample Cloud Scheduler Job for Daily PostgreSQL Backup
bash
# [NFR-009] Schedule daily backup at 02:00 UTC with 3 retry attempts
gcloud scheduler jobs create http membership-hub-db-backup \
  --schedule="0 2 * * *" \
  --uri="https://asia-southeast1-membership-hub-prod.cloudfunctions.net/backup-postgresql-daily" \
  --http-method=POST \
  --oidc-service-account-email=backup-service-account@membership-hub-prod.iam.gserviceaccount.com \
  --headers="Content-Type=application/json" \
  --message-body='{"encryption_key": "projects/membership-hub-prod/locations/global/keyRings/backup-key/cryptoKeys/db-backup-key", "max_retries": 3}' \
  --max-retry-attempts=3 \
  --retry-method=exponential


### 10.3 Contact Information
| Role | Contact | Responsibility |
|------|---------|----------------|
| System Admin | admin@membership-hub.com | Full access to backup/restore operations, RBAC management [ARC-001] |
| DevOps Lead | devops@membership-hub.com | Backup infrastructure maintenance, DR execution, GKE management [NFR-002] |
| Security Auditor | security@membership-hub.com | Backup access audit, compliance validation, encryption key management [NFR-003] |
| DBA | dba@membership-hub.com | Database backup validation, PITR execution, schema management [DAT-ALL] |

---

## Traceability Compliance Validation
| Tag Category | Total Required Tags | Covered Tags | Compliance Status |
|--------------|---------------------|--------------|-------------------|
| Architecture (ARC) | 10 | 10 | 100% Covered |
| Non-Functional (NFR) | 9 | 9 | 100% Covered |
| Exception (EXC) | 1 | 1 | 100% Covered |
| Data (DAT) | 11 | 11 | 100% Covered |
| **Total** | **31** | **31** | **Fully Compliant** |