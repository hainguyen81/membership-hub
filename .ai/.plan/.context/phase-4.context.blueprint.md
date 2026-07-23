# PHASE 4 CONTEXT BLUEPRINT: membership-hub

## 1. Phase Operational Scope & Objectives
- Integrate backend payment microservice with existing authentication, authorization, and Kafka notification layers.
- Implement a secure, multi-tenant payment gateway supporting card, digital wallet, and internal credit methods.
- Develop frontend payment UI and integration flows (Next.js/React) that consume the payment API and display real‑time transaction status.
- Establish end‑to‑end integration tests covering payment creation, webhook handling, and Kafka event propagation.
- Generate comprehensive technical documentation, Docker/Kubernetes manifests, and CI/CD pipelines for deployment on GCP Cloud Run and GKE.
- Perform static security analysis, OWASP compliance validation, and final verification of all integrated components.

## 2. Allowed Technical Scope & Directory Boundaries
- **Backend Java Services (Quarkus)**
  - `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/` – core service & controller packages
  - `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/` – unit & integration test packages
  - `./sources/backend/resources/application.yml` – Quarkus configuration
  - `./sources/backend/src/main/resources/db/migration/V*__*.sql` – Flyway migration scripts
- **Frontend (Next.js)**
  - `./sources/frontend/src/pages/payments.tsx` – payment UI page
  - `./sources/frontend/src/components/PaymentForm.tsx` – reusable payment form
  - `./sources/frontend/tests/payment.e2e.spec.ts` – Playwright/E2E test suite
- **Documentation**
  - `./sources/backend/docs/payment-api.md` – REST API specification
  - `./sources/backend/docs/integration-guide.md` – integration & deployment guide
- **Container & Cloud**
  - `./sources/backend/Dockerfile` – multi‑stage build for payment service
  - `./sources/backend/docker-compose.yml` – local dev stack
  - `./sources/backend/gcp/payment-service.yaml` – Cloud Run service definition
  - `./sources/backend/k8s/payment-deployment.yaml` – GKE deployment manifest
- **Allowed Endpoints**
  - `POST /api/v1/payments` – create payment (JSON body: amount, currency, method, tenantId)
  - `GET /api/v1/payments/{id}` – retrieve payment status
  - `POST /api/v1/payments/{id}/webhook` – external gateway webhook callback
  - All endpoints require `Authorization: Bearer <token>` and enforce tenant‑scope via `X-Tenant-ID` header.

## 3. Dedicated Sub-Agent Functional Directives
- **coder**
  - Implement `PaymentService` with AES‑256 encryption for card data, parameterized queries, and tenant‑isolated persistence.
  - Implement `PaymentController` exposing the allowed REST endpoints, injecting `PaymentService`, and applying OWASP A01/A02 controls (input validation, rate limiting, CSRF tokens for web UI).
  - Develop Next.js payment UI components (`PaymentForm`, `payments` page) that call the payment API and display real‑time status via Kafka events.
- **tester**
  - Write unit test for `PaymentService` (`PaymentServiceTest.java`) covering business logic, encryption, and tenant isolation.
  - Write integration/E2E test for payment flow using `INTEGRATION_SCOPE;./sources/frontend/tests/payment.e2e.spec.ts`.
- **reviewer**
  - Perform static code analysis on `PaymentService.java` and `PaymentController.java` for OWASP compliance, SQL injection prevention, and secure coding standards.
- **doc**
  - Produce `payment-api.md` documenting all payment endpoints, request/response schemas, authentication requirements, and error codes.
  - Produce `integration-guide.md` describing backend‑frontend integration, Kafka event contracts, and deployment steps.
- **docker**
  - Update `Dockerfile` to include payment service binary, copy configuration, and define health‑check endpoint.
  - Update `docker-compose.yml` to start payment service, PostgreSQL, and Kafka for local testing.
- **GCP**
  - Create `payment-service.yaml` defining Cloud Run service, memory/cpu limits, IAM service account with `cloudsql.user` role, and Cloud SQL connection.
- **GKE**
  - Create `payment-deployment.yaml` defining Deployment, Service, ConfigMap (application.yml), and Secret (DB credentials) for GKE cluster.

## 4. Phase Definition of Done (DoD)
- Payment microservice fully functional with secure CRUD operations and tenant isolation.
- Frontend payment UI integrated, responsive, and capable of creating/retrieving payments.
- All unit tests passing (>90% coverage) and integration/E2E tests passing.
- OWASP compliance verified: input validation, output encoding, encryption, parameterized queries, rate limiting, and secure headers.
- Technical documentation (`payment-api.md`, `integration-guide.md`) completed and stored in `./sources/backend/docs/`.
- Docker images built, tested locally, and pushed to artifact registry.
- Cloud Run and GKE deployments configured, tested, and ready for production traffic.
- Performance profiling indicates sub‑second latency for payment creation and <1% error rate.

## 5. DAY-BY-DAY ARCHITECTURAL EXECUTION LOGS

### DAY 1: BACKEND PAYMENT CORE IMPLEMENTATION
#### SUB-TASK 1.1: Implement PaymentService with OWASP security controls
##### Assigned Sub-Agent: coder
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/service/PaymentService.java
    *   **Architectural Requirements:**
        *   Use AES‑256 encryption for sensitive payment fields (card numbers, CVV) with key stored in Quarkus Vault.
        *   Implement tenant‑scoped JPA queries using `tenant_id` filter to enforce multi‑tenancy.
        *   Apply parameterized queries via Spring Data JPA to prevent SQL injection.
        *   Include validation annotations (e.g., `@NotNull`, `@Positive`) and throw `ValidationException` on invalid input.
        *   Emit Kafka event `PaymentCreatedEvent` with encrypted payload for downstream notifications.
#### SUB-TASK 1.2: Implement PaymentController REST endpoints
##### Assigned Sub-Agent: coder
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/controller/PaymentController.java
    *   **Architectural Requirements:**
        *   Expose `POST /api/v1/payments` accepting JSON body with fields `amount`, `currency`, `method`, `tenantId`.
        *   Validate `tenantId` against authenticated user’s tenant scope.
        *   Apply rate limiting (100 requests/minute per tenant) using Quarkus `RateLimit` interceptor.
        *   Return `201 Created` with payment ID and location header.
        *   Include CSRF token validation for web UI POST requests.
#### SUB-TASK 1.3: Create payment API documentation
##### Assigned Sub-Agent: doc
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/docs/payment-api.md
    *   **Architectural Requirements:**
        *   Document endpoint URLs, HTTP methods, request/response schemas, error codes, and authentication headers.
        *   Include example payloads, security considerations, and OWASP compliance notes.
#### SUB-TASK 1.4: Update Dockerfile for payment service
##### Assigned Sub-Agent: docker
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/Dockerfile
    *   **Architectural Requirements:**
        *   Use multi‑stage build: first stage for Maven compile, second stage for lightweight Quarkus runner.
        *   Copy `target/membershiphub-payment-service-runner.jar` as application binary.
        *   Set JVM flags for memory limits (`-Xmx512m`).
        *   Define health‑check endpoint `/q/health` for container orchestration.

### DAY 2: FRONTEND UI & TESTING FOUNDATION
#### SUB-TASK 2.1: Develop Next.js payment UI components
##### Assigned Sub-Agent: coder
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/frontend/src/components/PaymentForm.tsx
    *   **Architectural Requirements:**
        *   Build a controlled form with fields for amount, payment method (card/digital wallet), and tenant selection.
        *   Integrate with `POST /api/v1/payments` via `fetch` with automatic `Authorization` header injection.
        *   Display real‑time validation errors and success messages.
        *   Include CSRF token in form submission for web security.
*   **Target Path:** ./sources/frontend/src/pages/payments.tsx
    *   **Architectural Requirements:**
        *   Render `PaymentForm` and a list of recent payments fetched via `GET /api/v1/payments` (filtered by tenant).
        *   Subscribe to Kafka WebSocket (via `socket.io`) to receive `PaymentCreatedEvent` updates and update UI instantly.
#### SUB-TASK 2.2: Write unit test for PaymentService
##### Assigned Sub-Agent: tester
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/service/PaymentService.java;./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/service/PaymentServiceTest.java
    *   **Architectural Requirements:**
        *   Verify encryption/decryption round‑trip for card data.
        *   Validate tenant isolation by asserting queries filter on `tenant_id`.
        *   Test parameterized query safety by attempting injection payloads (should be sanitized).
        *   Mock Kafka producer to confirm event emission.
#### SUB-TASK 2.3: Perform static security analysis on PaymentService
##### Assigned Sub-Agent: reviewer
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/service/PaymentService.java
    *   **Architectural Requirements:**
        *   Run OWASP Dependency Check for vulnerable libraries.
        *   Validate absence of hardcoded credentials.
        *   Ensure all SQL queries use JPA criteria or native query placeholders.
        *   Confirm encryption key not stored in source code.
#### SUB-TASK 2.4: Configure Cloud Run service definition
##### Assigned Sub-Agent: GCP
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/gcp/payment-service.yaml
    *   **Architectural Requirements:**
        *   Define container image repository (`gcr.io/<project>/membershiphub-payment`).
        *   Set memory/cpu limits (`512Mi`, `1vCPU`).
        *   Attach Cloud SQL instance service account with `cloudsql.user` role.
        *   Configure environment variables for `TENANT_INTERVAL` and `KAFKA_BOOTSTRAP_SERVERS`.
        *   Enable public invocation with IAM authentication.

### DAY 3: INTEGRATION TESTING & DEPLOYMENT PREP
#### SUB-TASK 3.1: Execute end‑to‑end payment flow test
##### Assigned Sub-Agent: tester
##### Targeted Components & Technical Requirements:
*   **Target Path:** INTEGRATION_SCOPE;./sources/frontend/tests/payment.e2e.spec.ts
    *   **Architectural Requirements:**
        *   Launch local Docker stack (via `docker-compose.yml`).
        *   Authenticate as a test tenant, create a payment via UI, verify success toast.
        *   Validate payment persisted via backend `/api/v1/payments/{id}` endpoint.
        *   Confirm Kafka event received and UI updates in real time.
        *   Clean up test data after each scenario.
#### SUB-TASK 3.2: Update docker-compose for payment service
##### Assigned Sub-Agent: docker
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/docker-compose.yml
    *   **Architectural Requirements:**
        *   Define services: `payment-service`, `postgres`, `kafka`.
        *   Link payment-service to PostgreSQL and Kafka networks.
        *   Set environment variables for DB connection and Kafka bootstrap.
        *   Include healthcheck for payment-service using `/q/health`.
#### SUB-TASK 3.3: Create GKE deployment manifest
##### Assigned Sub-Agent: GKE
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/k8s/payment-deployment.yaml
    *   **Architectural Requirements:**
        *   Deployment with image `gcr.io/<project>/membershiphub-payment:latest`.
        *   Resource requests/limits matching Cloud Run spec.
        *   ConfigMap referencing `application.yml`.
        *   Secret for DB credentials (mounted as files).
        *   Service exposing port 8080 with clusterIP.
        *   Ingress rule for `/api/v1/payments/*`.
#### SUB-TASK 3.4: Update integration guide documentation
##### Assigned Sub-Agent: doc
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/docs/integration-guide.md
    *   **Architectural Requirements:**
        *   Step‑by‑step instructions to run local stack (`docker-compose up`).
        *   Deployment flow to GCP Cloud Run and GKE.
        *   Monitoring and logging setup (Cloud Monitoring, Loki).
        *   Troubleshooting sections for payment failures and Kafka connectivity.

### DAY 4: FINAL VERIFICATION, SECURITY & PRODUCTION READINESS
#### SUB-TASK 4.1: Security lint on PaymentController
##### Assigned Sub-Agent: reviewer
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/controller/PaymentController.java
    *   **Architectural Requirements:**
        *   Run OWASP Zap scan for common web vulnerabilities (SQLi, XSS, CSRF).
        *   Validate input sanitization and output encoding.
        *   Ensure proper HTTP status codes and error handling.
        *   Confirm CORS policy restricts to allowed origins.
#### SUB-TASK 4.2: Finalize Dockerfile multi‑stage build
##### Assigned Sub-Agent: docker
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/Dockerfile
    *   **Architectural Requirements:**
        *   Add stage for building native image (`quarkus-native`) for reduced footprint.
        *   Include `COPY --from=build /workspace/application/quarkus-run.jar`.
        *   Set `ENTRYPOINT ["java","-jar","/quarkus-run.jar"]`.
        *   Add `USER 1001` for non‑root execution.
#### SUB-TASK 4.3: Deploy to Cloud Run
##### Assigned Sub-Agent: GCP
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/gcp/deploy-payment.sh
    *   **Architectural Requirements:**
        *   Authenticate with GCP (`gcloud auth activate-service-account`).
        *   Build and push container image (`gcloud builds submit --tag gcr.io/<project>/membershiphub-payment`).
        *   Deploy service (`gcloud run deploy payment-service --image gcr.io/<project>/membershiphub-payment --platform managed --region us-central1 --allow-unauthenticated`).
        *   Update Cloud SQL instance private IP in service env.
#### SUB-TASK 4.4: Deploy to GKE
##### Assigned Sub-Agent: GKE
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/k8s/deploy.sh
    *   **Architectural Requirements:**
        *   Configure kubectl context to GKE cluster.
        *   Apply `payment-deployment.yaml` and `payment-service.yaml`.
        *   Verify rollout status (`kubectl rollout status deployment/payment-service`).
        *   Expose via Ingress with TLS.
#### SUB-TASK 4.5: Finalize payment API documentation
##### Assigned Sub-Agent: doc
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/backend/docs/payment-api-final.md
    *   **Architectural Requirements:**
        *   Consolidate all endpoint details, request/response schemas, error codes.
        *   Add security headers, rate limits, and tenant isolation notes.
        *   Include sample curl commands and OpenAPI snippet.
        *   Append deployment URLs (Cloud Run, GKE) for reference.