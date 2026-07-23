# PHASE 5 CONTEXT BLUEPRINT: membership-hub

## 1. Phase Operational Scope & Objectives
Deploy the membership-hub application to production on Google Cloud Platform (GCP) and Google Kubernetes Engine (GKE). This phase encompasses building Docker images, configuring CI/CD pipelines, provisioning GKE clusters, deploying services with Kubernetes manifests, performing performance profiling and load testing, conducting security verification (static analysis, secret scanning, OWASP compliance), and producing comprehensive operational documentation and runbooks to ensure production readiness.

## 2. Allowed Technical Scope & Directory Boundaries (Files, paths, and endpoints)
- `./sources/backend/` – Java backend code, Dockerfiles, scripts.
- `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/` – Core Java services, security utilities.
- `./sources/backend/src/main/docker/` – Dockerfile and build artifacts.
- `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/` – Unit tests.
- `./sources/infrastructure/gcp/` – GCP configuration (Artifact Registry, Cloud Build).
- `./sources/infrastructure/gke/` – GKE cluster and deployment manifests.
- `./sources/scripts/` – Build, load‑test, security‑scan scripts.
- `./sources/tests/` – Integration / performance test suites.
- `./sources/docs/` – Phase‑5 readiness and incident runbooks.
- `./sources/frontend/` – (if needed for health‑check UI, static assets).

All file paths must be absolute to the workspace and prefixed with `./sources/`. Java source files must reside under the package segment `org/nlh4j/saas/membershiphub`.

## 3. Dedicated Sub-Agent Functional Directives
- **coder**: Implement Docker image builds, load‑test scripts, and any required backend utilities; embed OWASP security controls (parameterized queries, AES‑256 PII encryption, tenant isolation).
- **docker**: Manage Dockerignore, image building, and push to GCP Artifact Registry.
- **GCP**: Configure Artifact Registry, Cloud Build pipeline, and IAM/Secret Manager integrations.
- **GKE**: Provision GKE cluster, create Kubernetes deployment/service manifests, enforce pod security and multi‑tenant labeling.
- **tester**: Execute performance and integration tests, using the `INTEGRATION_SCOPE` token for cross‑component verification.
- **reviewer**: Perform static code analysis and secret scanning on individual Java source files to enforce OWASP compliance.
- **doc**: Produce Phase‑5 readiness documentation and incident response runbooks.

## 4. Phase Definition of Done (DoD)
- Docker image built, tagged, and pushed to GCP Artifact Registry.
- Cloud Build pipeline defined and validated (triggers, security scanning step).
- GKE cluster provisioned with workload identity and network policies.
- Kubernetes deployment ready with resource limits, liveness/readiness probes, and tenant labels.
- Load‑test results show <1% error rate and response times within SLA; metrics exported to Cloud Monitoring.
- Static analysis and secret scanning report zero critical findings; OWASP checklist passed.
- Phase‑5 readiness documentation and incident runbook completed and stored in `./sources/docs/`.
- All artifacts stored under `./sources/` with proper version control; CI/CD pipeline can promote to production on success.

## 5. DAY-BY-DAY ARCHITECTURAL EXECUTION LOGS

### DAY 1: Docker Image Build and Push
#### SUB-TASK 1.1: Create Dockerfile and build script
##### Assigned Sub-Agent: coder
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/src/main/docker/Dockerfile
    *   **Architectural Requirements:**
        *   Multi‑stage build using openjdk:17-jdk-slim; copy compiled JAR; set health‑check endpoint `/actuator/health`.
        *   Enforce OWASP A01 (SQL injection) by ensuring all DB access uses parameterized queries.
        *   Configure environment variables for tenant isolation (`TENANT_ID`).
        *   Disable debug mode in production build.
#### SUB-TASK 1.2: Define .dockerignore and build script
##### Assigned Sub-Agent: docker
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/src/main/docker/.dockerignore
    *   **Architectural Requirements:**
        *   Exclude `target/`, `.git/`, `*.iml`, `src/main/docker/` to keep image size minimal.
*   **Target Path:** ./sources/scripts/build-push.sh
    *   **Architectural Requirements:**
        *   Use `gcloud builds submit` or `docker build` + `docker push` to Artifact Registry.
        *   Tag image with `${PROJECT_ID}/membership-hub:${COMMIT_SHA}`.
        *   Enforce IAM permissions for Cloud Build Service Account.
### DAY 2: GCP Cloud Build Pipeline Configuration
#### SUB-TASK 2.1: Define Cloud Build configuration
##### Assigned Sub-Agent: GCP
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/infrastructure/gcp/cloudbuild.yaml
    *   **Architectural Requirements:**
        *   Define steps: `docker-build`, `docker-push`, `security-scan`, `deploy-k8s`.
        *   Use substitution for `${PROJECT_ID}`, `${COMMIT_SHA}`.
        *   Include binary authorization policy to enforce trusted images.
#### SUB-TASK 2.2: Configure Artifact Registry and IAM
##### Assigned Sub-Agent: GCP
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/infrastructure/gcp/artifact-registry.yaml
    *   **Architectural Requirements:**
        *   Create repository `membership-hub` in region `us-central1`.
        *   Grant `cloudbuild@system.gserviceaccount.com` `roles/artifactregistry.writer`.
        *   Enable Secret Manager API for credential management.
### DAY 3: GKE Cluster Provisioning and Deployment
#### SUB-TASK 3.1: Provision GKE cluster
##### Assigned Sub-Agent: GKE
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/infrastructure/gke/cluster.yaml
    *   **Architectural Requirements:**
        *   Use `gcloud container clusters create` with `standard` tier, node pool `e2-medium`.
        *   Enable Workload Identity, network policies, and Resource Quota.
        *   Label cluster with `environment=production`.
#### SUB-TASK 3.2: Create Kubernetes deployment and service manifests
##### Assigned Sub-Agent: GKE
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/infrastructure/gke/deployment.yaml
    *   **Architectural Requirements:**
        *   Deploy container image from Artifact Registry (`${PROJECT_ID}/membership-hub:${COMMIT_SHA}`).
        *   Set resource limits (`cpu: 500m`, `memory: 1Gi`).
        *   Include liveness (`/actuator/health`) and readiness probes.
        *   Add pod security policy enforcement and tenant label `tenant_id: "${TENANT_ID}"`.
### DAY 4: Performance Profiling and Load Testing
#### SUB-TASK 4.1: Create load‑test script
##### Assigned Sub-Agent: coder
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/scripts/load-test.js
    *   **Architectural Requirements:**
        *   Use k6 to simulate concurrent users hitting key endpoints (`/api/auth/login`, `/api/students`, `/api/attendance/qr`).
        *   Include authentication flow with generated tokens.
        *   Assert HTTP status 2xx, response time <500ms, error rate <1%.
        *   Export metrics to Cloud Monitoring via `prometheus` endpoint.
#### SUB-TASK 4.2: Execute load test
##### Assigned Sub-Agent: tester
##### Targeted Components & Technical Requirements:
*   **Target Path:** INTEGRATION_SCOPE;./sources/tests/performance/load-test.spec.js
    *   **Architectural Requirements:**
        *   Run k6 via CI; capture console output and JSON results.
        *   Validate that SLA thresholds are met; fail build if error rate exceeds 1%.
### DAY 5: Security Verification and Documentation
#### SUB-TASK 5.1: Static code analysis on AuthService
##### Assigned Sub-Agent: reviewer
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/service/AuthService.java
    *   **Architectural Requirements:**
        *   Enforce OWASP A01: use parameterized queries for DB access.
        *   Enforce OWASP A02: validate JWT tokens, implement proper session management.
        *   Enforce OWASP A03: encrypt PII fields with AES‑256; store keys in Secret Manager.
        *   Disable debug logging and stack traces in production.
#### SUB-TASK 5.2: Secret scanning on AppConfig
##### Assigned Sub-Agent: reviewer
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/config/AppConfig.java
    *   **Architectural Requirements:**
        *   Ensure no hardcoded credentials; all secrets referenced via `SecretManagerService`.
        *   Apply least‑privilege IAM roles for service accounts.
        *   Validate environment variables are set at runtime.
#### SUB-TASK 5.3: Generate Phase‑5 readiness documentation
##### Assigned Sub-Agent: doc
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/docs/phase5-readiness.md
    *   **Architectural Requirements:**
        *   Include step‑by‑step deployment guide, health‑check verification, monitoring dashboard URLs.
        *   Document security scan results, OWASP compliance checklist, and remediation actions.
        *   Provide rollback procedures and version tagging strategy.
#### SUB-TASK 5.4: Create incident response runbook
##### Assigned Sub-Agent: doc
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/docs/incident-runbook.md
    *   **Architectural Requirements:**
        *   Define escalation matrix, common incident scenarios (pod crashloop, latency spikes, security alerts).
        *   Provide commands for log retrieval (`kubectl logs`, Cloud Logging queries).
        *   Include steps to restart deployments, scale pods, and notify stakeholders.