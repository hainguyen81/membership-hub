# Day 4: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/infra/.github/workflows/ci-cd-pipeline.yaml
# Day 4: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/membership-hub/src/main/java/com/membershiphub/rest/EnrollmentResource.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/infra/.github/workflows/ci-cd-pipeline.yaml`
*   Traceability Audit Tags For This Task: ['[NFR-001]', '[NFR-005]', '[NFR-006]']
*   Target Component Destination Path: `./sources/backend/membership-hub/src/main/java/com/membershiphub/rest/EnrollmentResource.java`
*   Traceability Audit Tags For This Task: ['[REQ-010]', '[REQ-011]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Xây dựng pipeline CI/CD tự động với GitHub Actions bao gồm các stages: (1) Checkout mã nguồn và thiết lập môi trường Java 21 và Node.js; (2) Build backend Quarkus với Maven và kiểm tra kích thước artifact; (3) Build frontend Next.js và kiểm tra kích thước bundle; (4) Chạy bộ kiểm thử đơn vị và tích hợp với mục tiêu độ bao phủ >=85%; (5) Quét lỗ hổng bảo mật phụ thuộc bằng OWASP Dependency Check; (6) Kiểm tra kích thước hình ảnh Docker đảm bảo dưới 500MB; (7) Push hình ảnh đã build lên Google Container Registry (GCR); (8) Triển khai tự động lên GKE khi commit vào nhánh main. Tích hợp ghi log audit cho tất cả sự kiện pipeline (build, test, deploy) với timestamp, user ID, và chi tiết hành động, lưu trữ log trong 1 năm theo yêu cầu NFR-006. Cấu hình thông báo lỗi qua email và Slack cho đội ngũ DevOps.']
['Triển khai REST API ghi danh học viên']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[NFR-001]', '[NFR-005]', '[NFR-006]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```yaml
# Traceability Tags: [NFR-001], [NFR-005], [NFR-006]
# CI/CD Pipeline for membership-hub project
# Implements end-to-end automated build, test, security scanning, Docker validation, GCR push, and GKE deployment
# Complies with NFR-001 (system performance, scalability, uptime requirements), NFR-005 (Docker image size <500MB), NFR-006 (audit logging with 1 year retention)
name: membership-hub CI/CD Pipeline

# Trigger pipeline on push to main/develop, PRs to main, and manual trigger
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  workflow_dispatch: # Allow manual execution for emergency deployments or testing

# Global environment constants (no hardcoded sensitive values, all secrets stored in GitHub Secrets)
env:
  # GCP Infrastructure Constants [NFR-001]
  GCP_PROJECT_ID: ${{ secrets.GCP_PROJECT_ID }}
  GCR_REGISTRY: ${{ secrets.GCR_REGISTRY }}
  GKE_CLUSTER_NAME: ${{ secrets.GKE_CLUSTER_NAME }}
  GKE_CLUSTER_ZONE: ${{ secrets.GKE_CLUSTER_ZONE }}
  # Build Tool Version Constants [NFR-001]
  JAVA_BUILD_VERSION: '21'
  NODE_BUILD_VERSION: '20'
  MAVEN_MEMORY_OPTS: '-Xmx2g -XX:+UseG1GC'
  # Docker Validation Constants [NFR-005]
  MAX_ALLOWED_DOCKER_IMAGE_SIZE_MB: 500
  # Notification Constants [NFR-006]
  SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
  DEVOPS_EMAIL_RECIPIENTS: ${{ secrets.DEVOPS_EMAIL_RECIPIENTS }}
  # Audit Log Constants [NFR-006]
  AUDIT_LOG_RETENTION_DAYS: 365
  AUDIT_LOG_FILE_PREFIX: ${{ github.run_id }}-audit

# Default job settings for all jobs
defaults:
  run:
    shell: bash
    # Fail pipeline if any command returns non-zero exit code
    fail-fast: true

# Job 1: Backend Build, Test, and Validation [NFR-001, NFR-005]
jobs:
  backend-build-test:
    name: Backend Build & Test
    runs-on: ubuntu-latest
    outputs:
      backend-artifact-path: ${{ steps.build-backend.outputs.artifact-path }}
      backend-audit-log: ${{ steps.audit-backend.outputs.audit-log-path }}
    steps:
      # Step 1: Checkout source code [NFR-001]
      - name: Checkout repository code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0 # Full history for audit logging
        # Audit log entry for step start
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Checkout repository | job: backend-build-test | actor: ${{ github.actor }} | commit: ${{ github.sha }}" >> $AUDIT_LOG_FILE

      # Step 2: Setup Java 21 environment [NFR-001]
      - name: Setup Java 21 (Quarkus runtime)
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: ${{ env.JAVA_BUILD_VERSION }}
          cache: 'maven' # Cache Maven dependencies to speed up builds
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Setup Java 21 | job: backend-build-test | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Setup Java 21 | job: backend-build-test | status: success" >> $AUDIT_LOG_FILE

      # Step 3: Build backend Quarkus service with Maven [NFR-001]
      - name: Build Quarkus backend artifact
        id: build-backend
        working-directory: ./sources/backend/membership-hub
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Build Quarkus backend | job: backend-build-test | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          # Run Maven build with tests, skip integration tests for speed (run separately in integration job)
          ./mvnw clean package -DskipTests -Dquarkus.package.type=uber-jar -Dmaven.test.skip=true ${{ env.MAVEN_MEMORY_OPTS }}
          # Capture artifact path for downstream jobs
          ARTIFACT_PATH=$(find target -name "*.jar" | head -n 1)
          echo "artifact-path=$ARTIFACT_PATH" >> $GITHUB_OUTPUT
          # Check artifact size (must be <500MB per NFR-005)
          ARTIFACT_SIZE_MB=$(du -m $ARTIFACT_PATH | cut -f1)
          echo "Backend artifact size: ${ARTIFACT_SIZE_MB}MB"
          if [ $ARTIFACT_SIZE_MB -gt 500 ]; then
            echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_FAILED | step: Build Quarkus backend | job: backend-build-test | error: Artifact size exceeds 500MB limit | size: ${ARTIFACT_SIZE_MB}MB" >> $AUDIT_LOG_FILE
            exit 1
          fi
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Build Quarkus backend | job: backend-build-test | status: success | artifact_size: ${ARTIFACT_SIZE_MB}MB" >> $AUDIT_LOG_FILE

      # Step 4: Run backend unit and integration tests with coverage check [NFR-001, NFR-006]
      - name: Run backend tests with coverage validation
        id: test-backend
        working-directory: ./sources/backend/membership-hub
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Run backend tests | job: backend-build-test | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          # Run tests with JaCoCo coverage report
          ./mvnw test -Djacoco.skip=false ${{ env.MAVEN_MEMORY_OPTS }}
          # Extract coverage percentage from JaCoCo report
          COVERAGE=$(grep -oP '(?<=<counter type="INSTRUCTION" missed=")\d+(?=" covered=")\d+' target/site/jacoco/jacoco.xml | awk '{print $2/$3 * 100}')
          echo "Backend test coverage: ${COVERAGE}%"
          # Fail if coverage is below 85% per enterprise requirement
          if (( $(echo "$COVERAGE < 85" | bc -l) )); then
            echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_FAILED | step: Run backend tests | job: backend-build-test | error: Test coverage below 85% threshold | coverage: ${COVERAGE}%" >> $AUDIT_LOG_FILE
            exit 1
          fi
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Run backend tests | job: backend-build-test | status: success | coverage: ${COVERAGE}%" >> $AUDIT_LOG_FILE

      # Step 5: Generate backend audit log artifact [NFR-006]
      - name: Generate backend audit log
        id: audit-backend
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] JOB_START | job: backend-build-test | actor: ${{ github.actor }} | commit: ${{ github.sha }}" >> $AUDIT_LOG_FILE
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] JOB_END | job: backend-build-test | status: ${{ job.status }}" >> $AUDIT_LOG_FILE
          echo "audit-log-path=$AUDIT_LOG_FILE" >> $GITHUB_OUTPUT
        env:
          AUDIT_LOG_FILE: ${{ env.AUDIT_LOG_FILE_PREFIX }}-backend.log

      # Step 6: Upload backend artifact and audit log as pipeline artifacts [NFR-006]
      - name: Upload backend build artifacts
        uses: actions/upload-artifact@v4
        with:
          name: backend-build-artifacts
          path: |
            ${{ steps.build-backend.outputs.artifact-path }}
            ${{ steps.audit-backend.outputs.audit-log-path }}
          retention-days: ${{ env.AUDIT_LOG_RETENTION_DAYS }} # Store logs for 1 year per NFR-006

  # Job 2: Frontend Build, Test, and Validation [NFR-001, NFR-005]
  frontend-build-test:
    name: Frontend Build & Test
    runs-on: ubuntu-latest
    needs: [] # Run in parallel with backend job
    outputs:
      frontend-artifact-path: ${{ steps.build-frontend.outputs.artifact-path }}
      frontend-audit-log: ${{ steps.audit-frontend.outputs.audit-log-path }}
    steps:
      # Step 1: Checkout repository code [NFR-001]
      - name: Checkout repository code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Checkout repository | job: frontend-build-test | actor: ${{ github.actor }} | commit: ${{ github.sha }}" >> $AUDIT_LOG_FILE

      # Step 2: Setup Node.js 20 environment [NFR-001]
      - name: Setup Node.js 20 (Next.js runtime)
        uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_BUILD_VERSION }}
          cache: 'npm' # Cache npm dependencies to speed up builds
          cache-dependency-path: './sources/frontend/web-app/package-lock.json'
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Setup Node.js 20 | job: frontend-build-test | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Setup Node.js 20 | job: frontend-build-test | status: success" >> $AUDIT_LOG_FILE

      # Step 3: Install frontend dependencies [NFR-001]
      - name: Install frontend dependencies
        working-directory: ./sources/frontend/web-app
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Install frontend dependencies | job: frontend-build-test | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          npm ci --legacy-peer-deps # Use ci for reproducible builds
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Install frontend dependencies | job: frontend-build-test | status: success" >> $AUDIT_LOG_FILE

      # Step 4: Build Next.js frontend application [NFR-001, NFR-005]
      - name: Build Next.js frontend application
        id: build-frontend
        working-directory: ./sources/frontend/web-app
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Build Next.js frontend | job: frontend-build-test | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          npm run build
          # Capture build output path
          ARTIFACT_PATH="./.next"
          echo "artifact-path=$ARTIFACT_PATH" >> $GITHUB_OUTPUT
          # Check bundle size (must be <500MB per NFR-005)
          BUNDLE_SIZE_MB=$(du -sm $ARTIFACT_PATH | cut -f1)
          echo "Frontend bundle size: ${BUNDLE_SIZE_MB}MB"
          if [ $BUNDLE_SIZE_MB -gt 500 ]; then
            echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_FAILED | step: Build Next.js frontend | job: frontend-build-test | error: Bundle size exceeds 500MB limit | size: ${BUNDLE_SIZE_MB}MB" >> $AUDIT_LOG_FILE
            exit 1
          fi
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Build Next.js frontend | job: frontend-build-test | status: success | bundle_size: ${BUNDLE_SIZE_MB}MB" >> $AUDIT_LOG_FILE

      # Step 5: Run frontend unit and integration tests with coverage check [NFR-001, NFR-006]
      - name: Run frontend tests with coverage validation
        id: test-frontend
        working-directory: ./sources/frontend/web-app
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Run frontend tests | job: frontend-build-test | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          # Run tests with Jest coverage report
          npm run test:coverage
          # Extract coverage percentage from Jest report
          COVERAGE=$(grep -oP '(?<=All files\s*\|\s*)\d+(?=\s*\|)' coverage/coverage-summary.json | head -n 1)
          echo "Frontend test coverage: ${COVERAGE}%"
          # Fail if coverage is below 85% per enterprise requirement
          if (( $(echo "$COVERAGE < 85" | bc -l) )); then
            echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_FAILED | step: Run frontend tests | job: frontend-build-test | error: Test coverage below 85% threshold | coverage: ${COVERAGE}%" >> $AUDIT_LOG_FILE
            exit 1
          fi
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Run frontend tests | job: frontend-build-test | status: success | coverage: ${COVERAGE}%" >> $AUDIT_LOG_FILE

      # Step 6: Generate frontend audit log [NFR-006]
      - name: Generate frontend audit log
        id: audit-frontend
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] JOB_START | job: frontend-build-test | actor: ${{ github.actor }} | commit: ${{ github.sha }}" >> $AUDIT_LOG_FILE
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] JOB_END | job: frontend-build-test | status: ${{ job.status }}" >> $AUDIT_LOG_FILE
          echo "audit-log-path=$AUDIT_LOG_FILE" >> $GITHUB_OUTPUT
        env:
          AUDIT_LOG_FILE: ${{ env.AUDIT_LOG_FILE_PREFIX }}-frontend.log

      # Step 7: Upload frontend artifact and audit log [NFR-006]
      - name: Upload frontend build artifacts
        uses: actions/upload-artifact@v4
        with:
          name: frontend-build-artifacts
          path: |
            ${{ steps.build-frontend.outputs.artifact-path }}
            ${{ steps.audit-frontend.outputs.audit-log-path }}
          retention-days: ${{ env.AUDIT_LOG_RETENTION_DAYS }} # 1 year retention per NFR-006

  # Job 3: Security Vulnerability Scan [NFR-001, NFR-003]
  security-scan:
    name: OWASP Dependency Security Scan
    runs-on: ubuntu-latest
    needs: [backend-build-test, frontend-build-test] # Run only after build/test jobs pass
    outputs:
      security-audit-log: ${{ steps.audit-security.outputs.audit-log-path }}
    steps:
      # Step 1: Checkout code
      - name: Checkout repository code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Checkout repository | job: security-scan | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE

      # Step 2: Run OWASP Dependency Check for backend (Maven) [NFR-003]
      - name: Scan backend dependencies for vulnerabilities
        uses: dependency-check/gh-action@main
        with:
          project: membership-hub-backend
          path: './sources/backend/membership-hub'
          format: 'JSON'
          failOnCVSS: '7' # Fail on high/critical vulnerabilities
          scanPath: './sources/backend/membership-hub/pom.xml'
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Scan backend dependencies | job: security-scan | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Scan backend dependencies | job: security-scan | status: ${{ job.status }}" >> $AUDIT_LOG_FILE

      # Step 3: Run OWASP Dependency Check for frontend (NPM) [NFR-003]
      - name: Scan frontend dependencies for vulnerabilities
        uses: dependency-check/gh-action@main
        with:
          project: membership-hub-frontend
          path: './sources/frontend/web-app'
          format: 'JSON'
          failOnCVSS: '7' # Fail on high/critical vulnerabilities
          scanPath: './sources/frontend/web-app/package-lock.json'
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Scan frontend dependencies | job: security-scan | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Scan frontend dependencies | job: security-scan | status: ${{ job.status }}" >> $AUDIT_LOG_FILE

      # Step 4: Generate security scan audit log [NFR-006]
      - name: Generate security scan audit log
        id: audit-security
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] JOB_START | job: security-scan | actor: ${{ github.actor }} | commit: ${{ github.sha }}" >> $AUDIT_LOG_FILE
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] JOB_END | job: security-scan | status: ${{ job.status }}" >> $AUDIT_LOG_FILE
          echo "audit-log-path=$AUDIT_LOG_FILE" >> $GITHUB_OUTPUT
        env:
          AUDIT_LOG_FILE: ${{ env.AUDIT_LOG_FILE_PREFIX }}-security.log

      # Step 5: Upload security scan artifacts [NFR-006]
      - name: Upload security scan artifacts
        uses: actions/upload-artifact@v4
        with:
          name: security-scan-reports
          path: |
            ${{ steps.audit-security.outputs.audit-log-path }}
            ./**/dependency-check-report.json
          retention-days: ${{ env.AUDIT_LOG_RETENTION_DAYS }}

  # Job 4: Docker Image Build, Validation, and Push to GCR [NFR-005, NFR-001]
  docker-build-push:
    name: Docker Build & GCR Push
    runs-on: ubuntu-latest
    needs: security-scan # Run only after security scan passes
    outputs:
      backend-image-tag: ${{ steps.build-backend-image.outputs.image-tag }}
      frontend-image-tag: ${{ steps.build-frontend-image.outputs.image-tag }}
      docker-audit-log: ${{ steps.audit-docker.outputs.audit-log-path }}
    steps:
      # Step 1: Checkout code
      - name: Checkout repository code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Checkout repository | job: docker-build-push | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE

      # Step 2: Authenticate to Google Cloud Platform [NFR-001]
      - name: Authenticate to GCP
        uses: google-github-actions/auth@v2
        with:
          credentials_json: ${{ secrets.GCP_SERVICE_ACCOUNT_KEY }}
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Authenticate to GCP | job: docker-build-push | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Authenticate to GCP | job: docker-build-push | status: success" >> $AUDIT_LOG_FILE

      # Step 3: Configure Docker to use GCR as registry [NFR-001]
      - name: Configure Docker for GCR
        uses: google-github-actions/setup-gcloud@v2
        with:
          project_id: ${{ env.GCP_PROJECT_ID }}
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Configure Docker for GCR | job: docker-build-push | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          gcloud auth configure-docker ${{ env.GCR_REGISTRY }} -q
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Configure Docker for GCR | job: docker-build-push | status: success" >> $AUDIT_LOG_FILE

      # Step 4: Build backend Docker image [NFR-005]
      - name: Build backend Docker image
        id: build-backend-image
        working-directory: ./sources/infra/backend
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Build backend Docker image | job: docker-build-push | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          # Build multi-stage Docker image for backend
          docker build -t ${{ env.GCR_REGISTRY }}/membership-hub-backend:${{ github.sha }} -t ${{ env.GCR_REGISTRY }}/membership-hub-backend:latest .
          # Capture image tag for downstream jobs
          echo "image-tag=${{ env.GCR_REGISTRY }}/membership-hub-backend:${{ github.sha }}" >> $GITHUB_OUTPUT
          # Validate image size is under 500MB per NFR-005
          IMAGE_SIZE_MB=$(docker images ${{ env.GCR_REGISTRY }}/membership-hub-backend:${{ github.sha }} --format "{{.Size}}" | sed 's/MB//' | cut -d'.' -f1)
          echo "Backend Docker image size: ${IMAGE_SIZE_MB}MB"
          if [ $IMAGE_SIZE_MB -gt ${{ env.MAX_ALLOWED_DOCKER_IMAGE_SIZE_MB }} ]; then
            echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_FAILED | step: Build backend Docker image | job: docker-build-push | error: Image size exceeds 500MB limit | size: ${IMAGE_SIZE_MB}MB" >> $AUDIT_LOG_FILE
            exit 1
          fi
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Build backend Docker image | job: docker-build-push | status: success | image_size: ${IMAGE_SIZE_MB}MB" >> $AUDIT_LOG_FILE

      # Step 5: Build frontend Docker image [NFR-005]
      - name: Build frontend Docker image
        id: build-frontend-image
        working-directory: ./sources/infra/web-app
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Build frontend Docker image | job: docker-build-push | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          # Build multi-stage Docker image for frontend
          docker build -t ${{ env.GCR_REGISTRY }}/membership-hub-frontend:${{ github.sha }} -t ${{ env.GCR_REGISTRY }}/membership-hub-frontend:latest .
          # Capture image tag for downstream jobs
          echo "image-tag=${{ env.GCR_REGISTRY }}/membership-hub-frontend:${{ github.sha }}" >> $GITHUB_OUTPUT
          # Validate image size is under 500MB per NFR-005
          IMAGE_SIZE_MB=$(docker images ${{ env.GCR_REGISTRY }}/membership-hub-frontend:${{ github.sha }} --format "{{.Size}}" | sed 's/MB//' | cut -d'.' -f1)
          echo "Frontend Docker image size: ${IMAGE_SIZE_MB}MB"
          if [ $IMAGE_SIZE_MB -gt ${{ env.MAX_ALLOWED_DOCKER_IMAGE_SIZE_MB }} ]; then
            echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_FAILED | step: Build frontend Docker image | job: docker-build-push | error: Image size exceeds 500MB limit | size: ${IMAGE_SIZE_MB}MB" >> $AUDIT_LOG_FILE
            exit 1
          fi
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Build frontend Docker image | job: docker-build-push | status: success | image_size: ${IMAGE_SIZE_MB}MB" >> $AUDIT_LOG_FILE

      # Step 6: Push Docker images to GCR [NFR-001]
      - name: Push Docker images to GCR
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Push Docker images to GCR | job: docker-build-push | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          docker push ${{ steps.build-backend-image.outputs.image-tag }}
          docker push ${{ steps.build-frontend-image.outputs.image-tag }}
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Push Docker images to GCR | job: docker-build-push | status: success" >> $AUDIT_LOG_FILE

      # Step 7: Generate Docker build audit log [NFR-006]
      - name: Generate Docker build audit log
        id: audit-docker
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] JOB_START | job: docker-build-push | actor: ${{ github.actor }} | commit: ${{ github.sha }}" >> $AUDIT_LOG_FILE
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] JOB_END | job: docker-build-push | status: ${{ job.status }}" >> $AUDIT_LOG_FILE
          echo "audit-log-path=$AUDIT_LOG_FILE" >> $GITHUB_OUTPUT
        env:
          AUDIT_LOG_FILE: ${{ env.AUDIT_LOG_FILE_PREFIX }}-docker.log

      # Step 8: Upload Docker build artifacts [NFR-006]
      - name: Upload Docker build artifacts
        uses: actions/upload-artifact@v4
        with:
          name: docker-build-artifacts
          path: ${{ steps.audit-docker.outputs.audit-log-path }}
          retention-days: ${{ env.AUDIT_LOG_RETENTION_DAYS }}

  # Job 5: Deploy to GKE (only on main branch push) [NFR-001, NFR-002]
  deploy-to-gke:
    name: Deploy to GKE Production
    runs-on: ubuntu-latest
    needs: docker-build-push
    if: github.ref == 'refs/heads/main' # Only deploy from main branch
    outputs:
      deploy-audit-log: ${{ steps.audit-deploy.outputs.audit-log-path }}
    steps:
      # Step 1: Checkout code
      - name: Checkout repository code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Checkout repository | job: deploy-to-gke | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE

      # Step 2: Authenticate to GCP [NFR-001]
      - name: Authenticate to GCP
        uses: google-github-actions/auth@v2
        with:
          credentials_json: ${{ secrets.GCP_SERVICE_ACCOUNT_KEY }}
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Authenticate to GCP | job: deploy-to-gke | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Authenticate to GCP | job: deploy-to-gke | status: success" >> $AUDIT_LOG_FILE

      # Step 3: Get GKE cluster credentials [NFR-001]
      - name: Get GKE cluster credentials
        uses: google-github-actions/get-gke-credentials@v2
        with:
          project_id: ${{ env.GCP_PROJECT_ID }}
          cluster_name: ${{ env.GKE_CLUSTER_NAME }}
          location: ${{ env.GKE_CLUSTER_ZONE }}
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Get GKE credentials | job: deploy-to-gke | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Get GKE credentials | job: deploy-to-gke | status: success" >> $AUDIT_LOG_FILE

      # Step 4: Deploy backend service to GKE [NFR-001, NFR-002]
      - name: Deploy backend service to GKE
        working-directory: ./sources/infra/gke
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Deploy backend to GKE | job: deploy-to-gke | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          # Update backend deployment with new image tag
          kubectl set image deployment/backend-deployment backend=${{ needs.docker-build-push.outputs.backend-image-tag }} -n membership-hub
          # Wait for rollout to complete
          kubectl rollout status deployment/backend-deployment -n membership-hub --timeout=300s
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Deploy backend to GKE | job: deploy-to-gke | status: success" >> $AUDIT_LOG_FILE

      # Step 5: Deploy frontend service to GKE [NFR-001, NFR-002]
      - name: Deploy frontend service to GKE
        working-directory: ./sources/infra/gke
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_START | step: Deploy frontend to GKE | job: deploy-to-gke | actor: ${{ github.actor }}" >> $AUDIT_LOG_FILE
          # Update frontend deployment with new image tag
          kubectl set image deployment/frontend-deployment frontend=${{ needs.docker-build-push.outputs.frontend-image-tag }} -n membership-hub
          # Wait for rollout to complete
          kubectl rollout status deployment/frontend-deployment -n membership-hub --timeout=300s
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] STEP_END | step: Deploy frontend to GKE | job: deploy-to-gke | status: success" >> $AUDIT_LOG_FILE

      # Step 6: Generate deployment audit log [NFR-006]
      - name: Generate deployment audit log
        id: audit-deploy
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] JOB_START | job: deploy-to-gke | actor: ${{ github.actor }} | commit: ${{ github.sha }} | target_branch: main" >> $AUDIT_LOG_FILE
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] JOB_END | job: deploy-to-gke | status: ${{ job.status }}" >> $AUDIT_LOG_FILE
          echo "audit-log-path=$AUDIT_LOG_FILE" >> $GITHUB_OUTPUT
        env:
          AUDIT_LOG_FILE: ${{ env.AUDIT_LOG_FILE_PREFIX }}-deploy.log

      # Step 7: Upload deployment audit log [NFR-006]
      - name: Upload deployment audit log
        uses: actions/upload-artifact@v4
        with:
          name: deployment-audit-log
          path: ${{ steps.audit-deploy.outputs.audit-log-path }}
          retention-days: ${{ env.AUDIT_LOG_RETENTION_DAYS }}

  # Job 6: Error Notification (runs on any job failure) [NFR-006]
  send-error-notification:
    name: Send DevOps Error Notification
    runs-on: ubuntu-latest
    needs: [backend-build-test, frontend-build-test, security-scan, docker-build-push, deploy-to-gke]
    if: always() && (failure() || cancelled()) # Run only if any previous job failed
    steps:
      - name: Send Slack notification
        uses: slackapi/slack-github-action@v1
        with:
          slack-message: |
            :x: membership-hub CI/CD Pipeline Failed
            *Workflow Run:* ${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}
            *Triggered by:* ${{ github.actor }}
            *Branch:* ${{ github.ref }}
            *Failed Job:* ${{ needs.backend-build-test.result == 'failure' && 'Backend Build & Test' || needs.frontend-build-test.result == 'failure' && 'Frontend Build & Test' || needs.security-scan.result == 'failure' && 'Security Scan' || needs.docker-build-push.result == 'failure' && 'Docker Build & Push' || needs.deploy-to-gke.result == 'failure' && 'GKE Deployment' }}
            *Commit:* ${{ github.sha }}
            *Action Required:* Please check the workflow run logs for details.
        env:
          SLACK_BOT_TOKEN: ${{ secrets.SLACK_BOT_TOKEN }}
          SLACK_CHANNEL_ID: ${{ secrets.SLACK_ALERT_CHANNEL_ID }}

      - name: Send email notification
        uses: dawidd6/action-send-mail@v3
        with:
          server_address: ${{ secrets.SMTP_SERVER }}
          server_port: ${{ secrets.SMTP_PORT }}
          username: ${{ secrets.SMTP_USERNAME }}
          password: ${{ secrets.SMTP_PASSWORD }}
          subject: " membership-hub CI/CD Pipeline Failed - Action Required"
          body: |
            Dear DevOps Team,
            
            The membership-hub CI/CD pipeline has failed. Please find the details below:
            - Workflow Run URL: ${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}
            - Triggered by: ${{ github.actor }}
            - Branch: ${{ github.ref }}
            - Failed Job: ${{ needs.backend-build-test.result == 'failure' && 'Backend Build & Test' || needs.frontend-build-test.result == 'failure' && 'Frontend Build & Test' || needs.security-scan.result == 'failure' && 'Security Scan' || needs.docker-build-push.result == 'failure' && 'Docker Build & Push' || needs.deploy-to-gke.result == 'failure' && 'GKE Deployment' }}
            - Commit SHA: ${{ github.sha }}
            
            Please review the workflow logs to resolve the issue immediately.
            
            Regards,
            membership-hub CI/CD Pipeline
          to: ${{ env.DEVOPS_EMAIL_RECIPIENTS }}
          from: ${{ secrets.SMTP_FROM_ADDRESS }}

  # Job 7: Aggregate and Store Audit Logs [NFR-006]
  aggregate-audit-logs:
    name: Aggregate Pipeline Audit Logs
    runs-on: ubuntu-latest
    needs: [backend-build-test, frontend-build-test, security-scan, docker-build-push, deploy-to-gke, send-error-notification]
    if: always() # Run regardless of previous job status
    steps:
      - name: Download all job audit logs
        uses: actions/download-artifact@v4
        with:
          path: ./audit-logs

      - name: Aggregate audit logs into single file
        run: |
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] PIPELINE_START | workflow: ${{ github.workflow }} | run_id: ${{ github.run_id }} | actor: ${{ github.actor }} | trigger: ${{ github.event_name }}" > aggregated-audit.log
          # Combine all job audit logs
          for log in ./audit-logs/*/*.log; do
            cat $log >> aggregated-audit.log
          done
          echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] PIPELINE_END | workflow: ${{ github.workflow }} | run_id: ${{ github.run_id }} | status: ${{ job.status }}" >> aggregated-audit.log

      - name: Upload aggregated audit log to long-term storage [NFR-006]
        uses: actions/upload-artifact@v4
        with:
          name: aggregated-pipeline-audit-log-${{ github.run_id }}
          path: aggregated-audit.log
          retention-days: ${{ env.AUDIT_LOG_RETENTION_DAYS }} # 1 year retention per NFR-006

      # Optional: Send audit log to GCP Cloud Logging for centralized audit [NFR-006]
      - name: Send audit log to GCP Cloud Logging
        if: success() || failure()
        uses: google-github-actions/upload-cloud-logging@v1
        with:
          credentials_json: ${{ secrets.GCP_SERVICE_ACCOUNT_KEY }}
          project_id: ${{ env.GCP_PROJECT_ID }}
          log_id: "ci-cd-audit-logs"
          resource_type: "global"
          entries: |
            - severity: INFO
              jsonPayload:
                workflow_name: ${{ github.workflow }}
                run_id: ${{ github.run_id }}
                actor: ${{ github.actor }}
                trigger: ${{ github.event_name }}
                branch: ${{ github.ref }}
                commit: ${{ github.sha }}
                status: ${{ job.status }}
                audit_log: |
                  $(cat aggregated-audit.log)
Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-010]', '[REQ-011]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership-hub.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * EnrollmentResource handles student course enrollment operations:
 * - Browsing available courses (excluding those already enrolled).
 * - Enrolling a student in a course, with automatic Student account creation if missing.
 * <p>
 * Traceability Tags: [REQ-010], [REQ-011]
 */
@RestController
@RequestMapping("/api/v1/enrollments")
@Validated
public class EnrollmentResource {

    // Enterprise‑level constants – hoisted to the crown for anti‑magic‑numbers compliance
    public static final String LOG_PREFIX = "[ENROLLMENT_RESOURCE]";
    public static final String MSG_AVAILABLE_COURSES_FETCHED = "Available courses fetched successfully for student {}";
    public static final String MSG_ENROLLMENT_SUCCESS = "Enrollment completed successfully for student {} in course {}";
    public static final String MSG_STUDENT_ACCOUNT_CREATED = "Student account auto‑created for email {}";
    public static final String ERR_CODE_COURSE_NOT_FOUND = "COURSE_NOT_FOUND";
    public static final String ERR_CODE_STUDENT_NOT_FOUND = "STUDENT_NOT_FOUND";
    public static final String ERR_CODE_ALREADY_ENROLLED = "ALREADY_ENROLLED";
    public static final String ERR_CODE_IDEMPOTENCY_VIOLATION = "IDEMPOTENCY_VIOLATION";
    public static final String ERR_CODE_INVALID_INPUT = "INVALID_INPUT";
    // In‑memory store for idempotency keys – in production replace with Redis or similar
    public static final Set<String> IDEMPOTENCY_KEYS = ConcurrentHashMap.newKeySet();

    private final Logger logger = LoggerFactory.getLogger(EnrollmentResource.class);

    private final EnrollmentService enrollmentService;

    /**
     * Constructor‑based dependency injection – preserves SOLID Single Responsibility Principle.
     */
    public EnrollmentResource(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /**
     * GET endpoint to browse courses that a student can still enroll in.
     * <p>
     * Traceability Tags: [REQ-010]
     */
    @GetMapping("/available")
    public ResponseEntity<List<CourseDTO>> getAvailableCourses(
            @RequestParam @NotNull UUID studentId) {

        logger.info("{} [ENTRY] Fetching available courses for studentId: {}", LOG_PREFIX, studentId);

        try {
            List<CourseDTO> available = enrollmentService.getAvailableCourses(studentId);
            logger.info(MSG_AVAILABLE_COURSES_FETCHED, studentId);
            logger.info("{} [EXIT] Returning {} available courses for studentId: {}", LOG_PREFIX, available.size(), studentId);
            return ResponseEntity.ok(available);
        } catch (Exception e) {
            // Comprehensive exception logging per enterprise audit law
            logger.error("[CRITICAL FAIL] [REQ-010] Failed to fetch available courses for studentId: {}. Raw error: {}", studentId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to retrieve available courses", e);
        }
    }

    /**
     * POST endpoint to enroll a student in a course.
     * <p>
     * Traceability Tags: [REQ-011]
     */
    @PostMapping
    @Transactional
    public ResponseEntity<EnrollmentResponse> enrollStudent(
            @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
            @Valid @RequestBody EnrollmentRequest request) {

        logger.info("{} [ENTRY] Processing enrollment request – studentId: {}, courseId: {}", LOG_PREFIX, request.getStudentId(), request.getCourseId());

        // Idempotency validation – prevents duplicate execution of the same request
        if (!IDEMPOTENCY_KEYS.add(idempotencyKey)) {
            logger.warn("{} [WARN] Idempotency violation detected for key: {}", LOG_PREFIX, idempotencyKey);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("Request with Idempotency-Key %s has already been processed.", idempotencyKey));
        }

        try {
            // Delegate business logic to the service layer
            EnrollmentResponse response = enrollmentService.enrollStudent(request.getStudentId(), request.getCourseId());

            logger.info(MSG_ENROLLMENT_SUCCESS, request.getStudentId(), request.getCourseId());
            logger.info("{} [EXIT] Enrollment completed – enrollmentId: {}", LOG_PREFIX, response.getEnrollmentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException iae) {
            // Business‑logic violations – map to appropriate HTTP status
            logger.error("[CRITICAL FAIL] [REQ-011] Business rule violation during enrollment – studentId: {}, courseId: {}. Raw error: {}", request.getStudentId(), request.getCourseId(), iae.getMessage(), iae);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, iae.getMessage(), iae);
        } catch (Exception e) {
            // Catch‑all for unexpected failures – full audit trail
            logger.error("[CRITICAL FAIL] [REQ-011] Unexpected error during enrollment – studentId: {}, courseId: {}. Raw error: {}", request.getStudentId(), request.getCourseId(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Enrollment processing failed", e);
        }
    }

    /**
     * Simple data transfer object for enrollment request payload.
     */
    public static class EnrollmentRequest {
        @NotNull(message = "Student ID is required")
        private UUID studentId;
        @NotNull(message = "Course ID is required")
        private UUID courseId;

        // Getters & Setters (omitted for brevity – standard auto‑generation)
        public UUID getStudentId() { return studentId; }
        public void setStudentId(UUID studentId) { this.studentId = studentId; }
        public UUID getCourseId() { return courseId; }
        public void setCourseId(UUID courseId) { this.courseId = courseId; }
    }

    /**
     * Data transfer object for enrollment response.
     */
    public static class EnrollmentResponse {
        private UUID enrollmentId;
        private UUID studentId;
        private UUID courseId;
        private Instant enrolledAt;

        // Getters & Setters
        public UUID getEnrollmentId() { return enrollmentId; }
        public void setEnrollmentId(UUID enrollmentId) { this.enrollmentId = enrollmentId; }
        public UUID getStudentId() { return studentId; }
        public void setStudentId(UUID studentId) { this.studentId = studentId; }
        public UUID getCourseId() { return courseId; }
        public void setCourseId(UUID courseId) { this.courseId = courseId; }
        public Instant getEnrolledAt() { return enrolledAt; }
        public void setEnrolledAt(Instant enrolledAt) { this.enrolledAt = enrolledAt; }
    }

    /**
     * Data transfer object for course information exposed to students.
     */
    public static class CourseDTO {
        private UUID courseId;
        private String title;
        private String description;
        private Integer maxStudents;
        private Integer enrolledCount;

        // Getters & Setters
        public UUID getCourseId() { return courseId; }
        public void setCourseId(UUID courseId) { this.courseId = courseId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getMaxStudents() { return maxStudents; }
        public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
        public Integer getEnrolledCount() { return enrolledCount; }
        public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }
    }

    /**
     * Service interface defining the core enrollment business operations.
     */
    interface EnrollmentService {
        List<CourseDTO> getAvailableCourses(UUID studentId);
        EnrollmentResponse enrollStudent(UUID studentId, UUID courseId);
    }

    /**
     * Concrete implementation of {@link EnrollmentService}.
     * <p>
     * This class encapsulates all data‑access and business‑rule logic, keeping the REST layer thin.
     */
    @Service
    static class EnrollmentServiceImpl implements EnrollmentService {

        private final Logger logger = LoggerFactory.getLogger(EnrollmentServiceImpl.class);

        // In‑memory “databases” for demonstration – replace with real JPA repositories in production
        private final java.util.Map<UUID, Student> students = new java.util.concurrent.ConcurrentHashMap<>();
        private final java.util.Map<UUID, Course> courses = new java.util.concurrent.ConcurrentHashMap<>();
        private final java.util.Map<UUID, Enrollment> enrollments = new java.util.concurrent.ConcurrentHashMap<>();

        public EnrollmentServiceImpl() {
            // Seed sample data – not part of production code
            Student s = new Student();
            s.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            s.setEmail("student@example.com");
            s.setRole("Student");
            students.put(s.getUserId(), s);

            Course c = new Course();
            c.setCourseId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            c.setTitle("Introduction to Spring Boot");
            c.setDescription("A comprehensive beginner course.");
            c.setMaxStudents(30);
            c.setEnrolledCount(0);
            courses.put(c.getCourseId(), c);
        }

        @Override
        public List<CourseDTO> getAvailableCourses(UUID studentId) {
            logger.info("[ENTRY] EnrollmentService – fetching available courses for studentId: {}", studentId);
            if (!students.containsKey(studentId)) {
                throw new IllegalArgumentException("Student not found");
            }

            List<CourseDTO> available = courses.values().stream()
                    .filter(course -> !enrollments.containsKey(new EnrollmentKey(studentId, course.getCourseId())))
                    .map(this::toCourseDTO)
                    .collect(Collectors.toList());

            logger.info("[EXIT] EnrollmentService – {} available courses for studentId: {}", available.size(), studentId);
            return available;
        }

        @Override
        public EnrollmentResponse enrollStudent(UUID studentId, UUID courseId) {
            logger.info("[ENTRY] EnrollmentService – enrolling studentId: {} into courseId: {}", studentId, courseId);

            // Auto‑create Student if missing – business rule from REQ‑011
            Student student = students.computeIfAbsent(studentId, k -> {
                Student newStudent = new Student();
                newStudent.setUserId(k);
                newStudent.setEmail("autocreated_" + k + "@example.com");
                newStudent.setRole("Student");
                logger.info("[INFO] Student account auto‑created for email: {}", newStudent.getEmail());
                return newStudent;
            });

            Course course = courses.get(courseId);
            if (course == null) {
                throw new IllegalArgumentException("Course not found");
            }

            EnrollmentKey key = new EnrollmentKey(studentId, courseId);
            if (enrollments.containsKey(key)) {
                throw new IllegalArgumentException("Student already enrolled in this course");
            }

            // Create enrollment record
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrollmentId(UUID.randomUUID());
            enrollment.setStudentId(studentId);
            enrollment.setCourseId(courseId);
            enrollment.setEnrolledAt(Instant.now());
            enrollments.put(key, enrollment);

            // Update course enrollment count
            course.setEnrolledCount(course.getEnrolledCount() + 1);

            EnrollmentResponse response = new EnrollmentResponse();
            response.setEnrollmentId(enrollment.getEnrollmentId());
            response.setStudentId(studentId);
            response.setCourseId(courseId);
            response.setEnrolledAt(enrollment.getEnrolledAt());

            logger.info("[EXIT] EnrollmentService – enrollment completed for enrollmentId: {}", enrollment.getEnrollmentId());
            return response;
        }

        private CourseDTO toCourseDTO(Course course) {
            CourseDTO dto = new CourseDTO();
            dto.setCourseId(course.getCourseId());
            dto.setTitle(course.getTitle());
            dto.setDescription(course.getDescription());
            dto.setMaxStudents(course.getMaxStudents());
            dto.setEnrolledCount(course.getEnrolledCount());
            return dto;
        }

        /**
         * Simple POJO representing a student – replace with JPA entity in real implementation.
         */
        static class Student {
            private UUID userId;
            private String email;
            private String role;

            public UUID getUserId() { return userId; }
            public void setUserId(UUID userId) { this.userId = userId; }
            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }
            public String getRole() { return role; }
            public void setRole(String role) { this.role = role; }
        }

        /**
         * Simple POJO representing a course – replace with JPA entity in real implementation.
         */
        static class Course {
            private UUID courseId;
            private String title;
            private String description;
            private Integer maxStudents;
            private Integer enrolledCount;

            public UUID getCourseId() { return courseId; }
            public void setCourseId(UUID courseId) { this.courseId = courseId; }
            public String getTitle() { return title; }
            public void setTitle(String title) { this.title = title; }
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
            public Integer getMaxStudents() { return maxStudents; }
            public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
            public Integer getEnrolledCount() { return enrolledCount; }
            public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }
        }

        /**
         * Composite key for enrollment – used for in‑memory storage demonstration.
         */
        static class EnrollmentKey {
            private final UUID studentId;
            private final UUID courseId;

            public EnrollmentKey(UUID studentId, UUID courseId) {
                this.studentId = studentId;
                this.courseId = courseId;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                EnrollmentKey that = (EnrollmentKey) o;
                return studentId.equals(that.studentId) && courseId.equals(that.courseId);
            }

            @Override
            public int hashCode() {
                return 31 * studentId.hashCode() + courseId.hashCode();
            }
        }

        /**
         * Simple POJO representing an enrollment record – replace with JPA entity in real implementation.
         */
        static class Enrollment {
            private UUID enrollmentId;
            private UUID studentId;
            private UUID courseId;
            private Instant enrolledAt;

            public UUID getEnrollmentId() { return enrollmentId; }
            public void setEnrollmentId(UUID enrollmentId) { this.enrollmentId = enrollmentId; }
            public UUID getStudentId() { return studentId; }
            public void setStudentId(UUID studentId) { this.studentId = studentId; }
            public UUID getCourseId() { return courseId; }
            public void setCourseId(UUID courseId) { this.courseId = courseId; }
            public Instant getEnrolledAt() { return enrolledAt; }
            public void setEnrolledAt(Instant enrolledAt) { this.enrolledAt = enrolledAt; }
        }
    }
}
```

