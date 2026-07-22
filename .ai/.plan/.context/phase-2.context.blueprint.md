# PHASE 2 CONTEXT BLUEPRINT: membership-hub
## 1. Phase Operational Scope & Objectives
The primary objective of Phase 2 is to develop the backend of the membership-hub project using Java 17, Quarkus, Kafka, and Postgres. This phase will focus on implementing the core features, including user authentication, course management, attendance tracking, and notification systems. The scope of this phase includes designing and implementing the database schema, creating RESTful APIs for backend services, and integrating Kafka for event-driven architecture.

## 2. Allowed Technical Scope & Directory Boundaries (Files, paths, and endpoints)
The technical scope for Phase 2 includes:
- **Database Schema Design**: Designing the Postgres database schema to support user authentication, course management, attendance tracking, and notification systems.
- **Backend API Development**: Creating RESTful APIs using Java 17 and Quarkus for user authentication, course management, attendance tracking, and notification systems.
- **Kafka Integration**: Integrating Kafka for event-driven architecture to handle notifications and attendance tracking.
- **Directory Structure**:
  - `src/main/java`: Java source files for the backend application.
  - `src/main/resources`: Configuration files and database schema definitions.
  - `src/test/java`: Unit tests and integration tests for the backend application.
- **Endpoints**:
  - `/api/auth`: User authentication endpoints.
  - `/api/courses`: Course management endpoints.
  - `/api/attendance`: Attendance tracking endpoints.
  - `/api/notifications`: Notification system endpoints.

## 3. Dedicated Sub-Agent Functional Directives
The following sub-agents will be involved in Phase 2:
- **Coder**: Responsible for designing and implementing the database schema, creating RESTful APIs, and integrating Kafka.
- **Tester**: Responsible for creating unit tests and integration tests for the backend application.
- **Reviewer**: Responsible for reviewing the code and ensuring it meets the project's coding standards and best practices.
- **DevOps**: Responsible for setting up the development environment, configuring Kafka, and ensuring the backend application is deployable.

## 4. Phase Definition of Done (DoD)
Phase 2 is considered complete when:
- The database schema is designed and implemented.
- RESTful APIs for user authentication, course management, attendance tracking, and notification systems are created and tested.
- Kafka is integrated for event-driven architecture.
- Unit tests and integration tests are written and passed.
- Code reviews are completed, and the code meets the project's coding standards and best practices.
- The backend application is deployable and configured for further development.