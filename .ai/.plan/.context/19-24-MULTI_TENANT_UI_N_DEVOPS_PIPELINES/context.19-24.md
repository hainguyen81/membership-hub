# PHASE 4 CONTEXT: MULTI-TENANT UI & DEVOPS PIPELINES (DAYS 19 - 24)

## GLOBAL REGISTRY MANAGEMENT ARCHITECTURE ENFORCEMENT (DOCKER HUB SPEC)
- Every application image compiled for the public registry must strictly bám theo alphanumeric production naming matrices: `<DOCKERHUB_NAMESPACE>/<app_domain>-service:day-<X>`.
- The compilation workflow must directly inherit base optimization layer specifications defined inside the target native Dockerfiles without injecting dynamic static environment overhead variables at image bake-time to protect cross-tenant security containment definitions.

## DAY 19: ENTERPRISE WEB PORTAL ADMINISTRATIVE DASHBOARD & CRM RESOURCE
- Target Component: ./sources/frontend/src/app/[locale]/admin/dashboard/page.tsx & org.nlh4j.saas.membership.hub.web.AdminCrmResource

### Architecture Requirements
Construct a modern administrative management console rendering complex business analytics charts for B2B center operators and deploy corresponding backend CRM endpoints.
- **Frontend Dashboard UI Layout:** Build clean, high-fidelity visual Tailwind CSS and Shadcn UI data table grids rendering active multi-tenant student membership parameters, remaining subscription countdown windows, and a real-time checkpoint entry feed. Implement a dedicated, localized dynamic alarm layout panel filtering candidates showing high-risk states where remaining_days <= 3 (`CRITICAL_EXPIRATION`). Embed separate, independent interactive interface tab configurations allowing administrative operators to provision promotion tokens (`sys_promotions`) and audit continuous delivery logs reading history channels (`notification_delivery_logs`) data states directly.
- **Backend Admin CRM Resource Integration:** Expose secure, non-blocking REST infrastructure endpoints within the root enterprise namespace enabling operational management. The service layer must execute automated multi-tenant isolation by extracting tenant credentials straight from cryptographically verified user Bearer JWT signature strings. Implement processing routers enabling operators to manually invoke custom alert push packages (routing payloads straight to Kafka cluster streams), register new promotion campaign discount records into the `sys_promotions` database, and query data tables sorting historic outbox delivery tracks natively.
- **Strict OpenAPI Swagger Annotation Rules:** Every administrative API endpoint control mapping must include 100% comprehensive documentation decoration nodes powered by microprofile specification properties to maximize Swagger UI `/q/swagger-ui` layout visibility:
  * Manually triggered notification gates must register `@Operation(summary = "Trigger manual renewal notice via Kafka", description = "Asynchronously pushes an enterprise promotion or expiration notice parameter package straight to Kafka messaging consumers")`.
  * Define explicit response frameworks declaring `@APIResponse(responseCode = "202", description = "Notice request accepted and piped to asynchronous event stream successfully")` for successful operations, alongside distinct failure profiles `@APIResponse(responseCode = "403", description = "Access denied due to invalid or cross-tenant discriminator credentials verification failures")`.
  * Enforce parameterized metadata tracking across querying and data lookup inputs using explicit `@Parameter(description = "...", required = true)` documentation blocks to enable full browser validation testing parameters execution.

## DAY 20: HYBRID MOBILE CAPACITOR COMPILATION STRUCTURING
### Target Path: ./sources/frontend/capacitor.config.json
### Architecture Requirements
Configure build-automation configurations packaging Next.js frontend clients into fully independent static artifacts suitable for platform-native execution shells.
- Declare baseline native bridge execution properties inside capacitor.config.json defining targeted webview parameters.
- Structure automated shell automation scripts running production optimizations via next build tied directly to static bundle next export output settings.
- Direct compiled web bundle assets into platform-specific container spaces mapped under /ios and /android sub-project zones.

## DAY 21: TOTAL SYSTEM END-TO-END AUTOMATED VERIFICATION
### Target Path: ./sources/frontend/tests/e2e/attendance-flow.spec.ts
### Architecture Requirements
Develop full-scale end-to-end automation scripts utilizing the Playwright testing engine to challenge complete integration systems under real headless browser profiles.
- Model multi-viewport student interactions simulating language swaps, logins, barcode generation, and account balance tracking.
- Test administrative dashboard behaviors: execute on-demand Zalo alert dispatches, provision promotion records, and trace delivery log updates across the view layout.
- Scrape document tree components confirming alternate multi-language headers (hreflang metadata nodes) comply with search indexing standards.

## DAY 22: OPTIMIZED GRAALVM DOCKERIZATION ENVIRONMENT STACKS
### Target Path: ./sources/backend/src/main/docker/Dockerfile.native
### Architecture Requirements
Author a clean, multi-stage native container image definition optimized for fast cluster deployment and low computing overhead.
- Base the compilation phase on a Mandrel Image Builder environment to compile Quarkus code directly into optimized native binary structures running free from Java runtime memory footprint patterns.
- Assemble a unified local environment configuration stack (docker-compose.yml) coordinating the network launch parameters of the application binary container, frontend interfaces, data storage, cache systems, and Kafka channels.

## DAY 23: PRODUCTION ENTERPRISE KUBERNETES DEPLOYMENT MANIFESTS
### Target Path: ./sources/infrastructure/k8s/deployment.yaml
### Architecture Requirements
Generate production-grade declarative infrastructure manifest specifications orchestrating execution on high-availability Google Kubernetes Engine (GKE) compute networks.
- Structure deployment layouts specifying strict compute resources allocations, limits parameters, and secure liveness/readiness probes gates.
- Configure Ingress controller routing maps equipped with explicit URL path translation and rewrite rules to guide incoming multi-language web requests seamlessly.
- Implement an automated Horizontal Pod Autoscaler (HPA) script set to dynamically expand compute node instances between 2 and 10 active pods whenever hardware limits surpass 75%.

## DAY 24: CONTINUOUS INTEGRATION & DELIVERY AUTOMATION PIPELINE
### Target Path: .github/workflows/deploy.yml
### Architecture Requirements
Formulate an elegant automated delivery integration workflow orchestration script running entirely via the GitHub Actions cloud scheduling layer.
- Bind pipeline execution triggers to process automatically only upon official codebase modification pushes hitting the centralized production master branch.
- Structure clear execution boundaries passing code blocks through consecutive processing states: static security scanning (SAST), code coverage metrics checking, binary compilation, Docker image publication to Google GAR registries, and zero-downtime rolling updates execution on GKE cluster nodes.
