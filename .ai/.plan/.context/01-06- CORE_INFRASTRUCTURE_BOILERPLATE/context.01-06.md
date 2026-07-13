# PHASE 1 CONTEXT: CORE INFRASTRUCTURE BOILERPLATE (DAYS 1 - 6)

## DAY 1: MULTI-TENANCY DATABASE SCHEMA SPECIFICATION
### Target Path: ./sources/backend/src/main/resources/db/migration/V1.0.0__init_schema.sql
### Architecture Requirements
Generate a highly-optimized PostgreSQL DDL migration script implementing a Shared Database, Discriminator Column multi-tenancy model. Every query must be isolated implicitly via a tenant_id column.
- Create table sys_tenants to serve as the master organization record registry.
- Create table app_users containing a strict compound unique constraint on (tenant_id, email) to prevent cross-tenant identity contamination.
- Create table student_cards implementing a dynamic gym-style countdown infrastructure tracking membership duration.
- Create table attendance_logs mapping checking operations with a unique compound constraint on (tenant_id, student_id, checkin_date) to natively reject duplicate entries on the same calendar day.
- Create table sys_promotions to manage marketing token parameters, discount rates, and operational duration per center boundary.
- Create table notification_delivery_logs establishing an enterprise audit trail tracking outbox dispatches across channels (ZALO_PERSONAL, ZALO_GROUP, MOBILE_PUSH_FCM) initialized with a PENDING state, including fields for payload data and error messages.
- All primary and foreign keys must utilize explicit indexing profiles (idx_*_tenant) to sustain fast lookups during peak entry windows.

## DAY 2: QUARKUS REACTIVE CORE & EXTENSIONS SETUP
### Target Path: ./sources/backend/pom.xml
### Architecture Requirements
Construct the foundational enterprise configuration files bootstrapping a non-blocking reactive application core environment with automated API specification support.
- Configure pom.xml targeting Quarkus 3.x and Java 21 LTS specifications.
- Load required technical extensions: Panache Hibernate Reactive, Vert.x Reactive PostgreSQL Client, SmallRye Reactive Messaging for Apache Kafka integration, RESTEasy Reactive Jackson, compile-time MapStruct mapping injection, and quarkus-smallrye-openapi for automated documentation.
- Configure application.properties setting up asynchronous reactive connection pool properties, connection timeout gates, and immediate internal transaction isolation limits.
- Inject global Swagger UI configurations into application.properties to dynamically enable the /q/swagger-ui route inside local Dev/Staging environments (quarkus.swagger-ui.always-include=false for Production sandboxing).
- Register an OpenID Connect (OIDC) Bearer JWT security scheme definition and a global multi-tenant parameter header layout (X-Tenant-Id) within the SmallRye OpenAPI bootstrap context.
- Establish native configurations wiring automated Dev Services containers for PostgreSQL and Kafka Docker networks.
- Configure Swagger UI pathways inside application.properties: enable `/q/swagger-ui` route within Dev/Staging layers, but secure production cloud nodes strictly by hardcoding `quarkus.swagger-ui.always-include=false` to cloaking endpoints schemas and mitigate infrastructure exposure risks.

## DAY 3: NEXT.JS MULTI-LANGUAGE BOILERPLATE DEVELOPMENT
### Target Path: ./sources/frontend/package.json
### Architecture Requirements
Initialize a modern Frontend client framework optimized for highly responsive multi-tenant operational rendering states.
- Configure a strict TypeScript Next.js 14+ application tree deploying structural App Router architectures.
- Integrate the next-intl enterprise package at the root routing engine to intercept, format, and handle multi-language localized domain variables (/vi/, /en/).
- Isolate workspace layouts into clear structural folder layers: design /src/app/[locale]/admin to encapsulate B2B enterprise operator views, and configure /src/app/[locale]/app to handle high-fidelity B2C mobile client viewport rendering frames.

## DAY 4: EDGE-LAYER LOCALE DETECTION MIDDLEWARE & DYNAMIC SEO META
### Target Path: ./sources/frontend/src/middleware.ts
### Architecture Requirements
Develop high-performance routing interceptors executing at the edge layer to maintain instant internationalization configurations.
- Author an optimized Next.js Edge Middleware managing client localization inspecting active choice cookies, Capacitor native hardware locale responses, and HTTP Accept-Language context headers sequentially.
- Build automated, dynamic metadata generation systems injecting structured Alternate HTML hypermedia configurations (hreflang indexing mappings) across public landing zones to prevent duplication penalties on web crawler indexing databases.
- Script a dynamic sitemap.ts routing engine compiling explicit search maps independently across language segments.

## DAY 5: MULTI-TENANT USER REPOSITORY & INTERNAL AUTH PLATFORM
### Target Path: ./sources/backend/src/main/java/org/nlh4j/saas/membership/hub/service/AuthService.java
### Architecture Requirements
Build the internal multi-tenant credential authorization core following defensive security patterns with full API documentation.
- Map the User data domain using native Quarkus Panache Hibernate abstractions. Inject global system-wide @FilterDef and @Filter attributes, forcing the automatic appending of tenant_id = :tenantId criteria on every runtime database access.
- Implement credentials parsing operations executing password hash evaluations via BCrypt with an enterprise workload factor set to 12.
- Develop a security engine issuing asymmetric RS256 JSON Web Tokens (JWT) signed via a private key infrastructure, passing down validated tenant_id and structural security group claims inside the cryptographic payload signature.
- Decorate the Authentication endpoints with mandatory OpenAPI annotations: @Operation detailing login summary and description, @APIResponse mapping response codes 200 (Success Token Response) and 401 (Unauthorized Credentials Entry), and @Parameter specs for inbound context targets.

## DAY 6: FEDERATED OIDC AUTHENTICATION GATE & JIT PROVISIONING
### Target Path: ./sources/backend/src/main/java/org/nlh4j/saas/membership/hub/config/SecurityOidcFilter.java
### Architecture Requirements
Extend the identity engine to support multi-provider federated login ecosystems for external mobile clients.
- Construct a reactive Quarkus security filter validating inbound social identity headers passed down by Firebase, Google, or Facebook profiles.
- Interface with provider remote JWKS certificate key arrays dynamically to perform real-time cryptographic signature checks.
- Implement an automated Just-In-Time (JIT) identity provisioning database mutation system: upon confirmation of a valid third-party signature where the email is absent from app_users under the current tenant context, execute an immediate inline execution inserting a new user record seamlessly.
- Expose the JIT Callback endpoints fully documented via OpenAPI: declare @Operation data contracts, @APIResponse 200 (JIT Provisioned Profile Token), 400 (Bad Cryptographic Provider Identity), and @Parameter models for header access control interception configurations.
