# GLOBAL PROJECT CONTEXT: membership-hub

## 1. Executive Summary & Tech Stack Blueprint
The membership-hub project is a multi-tenant, scalable application with both web and mobile components. The tech stack consists of Java 17, Quarkus, Kafka, Postgres, and Docker, with deployment on Google Cloud Platform (GCP) and Google Kubernetes Engine (GKE). The project requires a robust authentication system, with support for internal authentication and external authentication via Firebase, Google, and Facebook. The application will have various roles, including System Admin, Admin, Manager, Teacher, and Student, each with distinct permissions and access levels.

## 2. Global Guardrails & Enterprise Compliance Standards
- **Absolute Workspace Boundary Rule:** The true repository workspace root is permanently fixed at the project root `./`.
- **Mandatory Path Subdirectory Rule:** Every single file path, configuration, script, diagram, or test asset generated across all prompts MUST be strictly placed inside the `./sources/` directory.
- **Conditional Path Prefixing:** 
  * All Backend service logics, microservices, configurations, database schemas, and backend tests must be prefixed with: `./sources/backend/`.
  * All Frontend user interfaces, responsive views, mobile apps, state management packages, and client-side tests must be prefixed with: `./sources/frontend/`.
- **Java Enterprise Package Standard:** Java source codes MUST strictly reside within the corporate package foundation: `org.nlh4j.saas.membershiphub`.
- **Strict Package-to-Path Mapping:** All physical Java files under `./sources/backend/src/main/java/` or `./sources/backend/src/test/java/` MUST follow the exact subdirectory layout matching the calculated lowercase token.
- **Strict Tester Target Path Syntax:** Any component targeted by a Tester Sub-Agent must be structured as a strict semi-colon separated pair `<source_component_or_token>;<test_suite_file_to_execute>`.

## 3. High-Level Multi-Phase Architectural Synopsis Grid
The following table outlines the distribution of components and requirements across the 5 phases:

| Phase | Duration (Days) | Description | Sub-Agents |
| --- | --- | --- | --- |
| 1 | 3 | Project setup, backend framework initialization, database schema design | coder, docker, GCP |
| 2 | 4 | Implementation of core business logic, authentication system, and role-based access control | coder, tester, reviewer |
| 3 | 3 | Development of frontend user interfaces, mobile app, and responsive views | coder, tester |
| 4 | 4 | Integration of backend and frontend components, implementation of payment gateway, and testing | coder, tester, reviewer |
| 5 | 3 | Deployment on GCP and GKE, performance profiling, security verification, and production readiness | docker, GCP, GKE, reviewer |

The following requirements are allocated to each phase:

**Phase 1:**

* Project setup and initialization
* Backend framework initialization (Quarkus)
* Database schema design (Postgres)
* Docker configuration

**Phase 2:**

* Implementation of core business logic
* Authentication system (internal and external)
* Role-based access control
* Unit testing and integration testing

**Phase 3:**

* Development of frontend user interfaces (web and mobile)
* Responsive views and mobile app
* State management and client-side testing

**Phase 4:**

* Integration of backend and frontend components
* Implementation of payment gateway
* Testing and debugging

**Phase 5:**

* Deployment on GCP and GKE
* Performance profiling and optimization
* Security verification and OWASP compliance
* Production readiness and deployment

Each phase is designed to be completed within a duration of 1-7 days, with a total project duration of 17 days. The sub-agents allocated to each phase are responsible for completing the specific tasks and requirements outlined in the phase description.