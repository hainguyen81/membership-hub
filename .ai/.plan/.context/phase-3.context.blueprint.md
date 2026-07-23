# PHASE 3 CONTEXT BLUEPRINT: membership-hub

## 1. Phase Operational Scope & Objectives
- Establish a unified frontend foundation using Next.js (web) and React Native (mobile) to support multi‑tenant membership management.
- Implement responsive, role‑based UI screens for **System Admin, Admin, Manager, Teacher, and Student** covering all required management functions (centers, courses, teachers, students, promotions, announcements, AI chat, QR attendance, token expiry, push notifications).
- Deploy client‑side state management (Redux Toolkit / Zustand) and internationalization (i18n) with locale detection from user preference, browser, or device.
- Ensure OWASP‑compliant frontend security: input sanitization, CSRF tokens, XSS protection, secure storage of tokens, and safe API calls.
- Generate comprehensive technical documentation and client‑side test suites achieving ≥80 % coverage.

## 2. Allowed Technical Scope & Directory Boundaries (Files, paths, and endpoints)
- **Web UI:** `./sources/frontend/src/pages/`, `./sources/frontend/src/components/`, `./sources/frontend/src/features/`, `./sources/frontend/src/lib/` (i18n, api utilities).
- **Mobile UI:** `./sources/frontend/mobile/src/screens/`, `./sources/frontend/mobile/src/components/`, `./sources/frontend/mobile/src/services/`.
- **State Management:** `./sources/frontend/src/store/` (Redux slices) and `./sources/frontend/src/hooks/` (custom hooks).
- **Testing:** `./sources/frontend/tests/` (Jest unit tests), `./sources/frontend/tests/e2e/` (Cypress/Detox integration).
- **Documentation:** `./sources/frontend/docs/` (architecture, component APIs, deployment notes).
- **Configuration:** `./sources/frontend/next.config.js`, `./sources/frontend/package.json`, `./sources/frontend/mobile/package.json`.
- **Endpoints (client‑facing):** All REST/GraphQL calls are abstracted via `./sources/frontend/src/lib/apiClient.ts`; no direct path exposure beyond `./sources/`.

## 3. Dedicated Sub-Agent Functional Directives
- **coder:** Create source files, implement UI components, configure i18n, set up state management, build mobile screens, integrate QR scanner, push notification services, and embed OWASP security controls (input validation, CSRF tokens, secure token storage).
- **tester:** Write unit tests for React components, integration/E2E tests for multi‑screen workflows, and ensure test coverage meets ≥80 % for each module.
- **doc:** Produce markdown architecture docs, component READMEs, and API usage guides placed under `./sources/frontend/docs/`.
- **reviewer:** Perform static code analysis, linting, and security scanning on individual source files (e.g., auth, API client) to enforce coding standards and OWASP compliance.

## 4. Phase Definition of Done (DoD)
- All required web and mobile screens implemented and responsive across devices.
- Multi‑language support functional with locale detection and fallback.
- State management centralized; all role‑based data flows secured.
- OWASP baseline controls applied (XSS mitigation, CSRF tokens, secure storage, input sanitization).
- Client‑side test suites executed with ≥80 % coverage; integration/E2E tests pass.
- Technical documentation generated for architecture, components, and deployment.
- Mobile builds configured for iOS/Android (Metro bundler, native modules).
- No open linting or security violations in reviewed source files.

## 5. DAY‑BY‑DAY ARCHITECTURAL EXECUTION LOGS

### DAY 1: Foundational Frontend Setup & Documentation
#### SUB‑TASK 1.1: Initialize Next.js Project & i18n Configuration
##### Assigned Sub-Agent: coder
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/frontend/next.config.js
    *   **Architectural Requirements:**
        *   Configure basePath, trailingSlash, and output directory for production builds.
        *   Integrate i18n with `locales` (en, vi) and `defaultLocale` detection from user preference, browser, or device.
        *   Enable OWASP‑compliant CSP headers via `headers()` to mitigate XSS.
*   **Target Path:** ./sources/frontend/src/lib/i18n.ts
    *   **Architectural Requirements:**
        *   Implement `react-i18next` with namespace-based resource loading.
        *   Add locale detection middleware that reads `Accept-Language` header and user‑stored preference.
        *   Sanitize locale values to prevent injection attacks.

#### SUB‑TASK 1.2: Create Frontend State Management Layer
##### Assigned Sub-Agent: coder
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/frontend/src/store/index.ts
    *   **Architectural Requirements:**
        *   Set up Redux Toolkit with root reducer combining feature slices.
        *   Persist auth tokens and user roles using `redux-persist` with secure storage (AsyncStorage for mobile, localStorage for web).
        *   Enforce token expiration and refresh logic following OWASP A07:2021 – Identification and Authentication Failures.

#### SUB‑TASK 1.3: Generate Frontend Architecture Documentation
##### Assigned Sub-Agent: doc
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/frontend/docs/FRONTEND_ARCHITECTURE.md
    *   **Architectural Requirements:**
        *   Document project structure, routing, i18n setup, state management, API client patterns.
        *   Include OWASP security controls applied at the frontend layer.
        *   Provide component interaction diagrams and data flow sketches.

### DAY 2: Core Web UI Implementation & Testing
#### SUB‑TASK 2.1: Implement Admin Center Management Screen
##### Assigned Sub-Agent: coder
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/frontend/src/pages/admin/centers.tsx
    *   **Architectural Requirements:**
        *   Build CRUD UI for centers (list, add, edit, delete) with form validation.
        *   Integrate CSRF token in form submissions; sanitize all user inputs.
        *   Apply role‑based access control (only System Admin can access).

#### SUB‑TASK 2.2: Write Unit Tests for Center Management Component
##### Assigned Sub-Agent: tester
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/frontend/src/pages/admin/centers.tsx;./sources/frontend/tests/pages/admin/centers.test.tsx
    *   **Architectural Requirements:**
        *   Test rendering of center list, form fields, and validation logic.
        *   Verify OWASP input sanitization via mocked API responses.
        *   Ensure component re‑renders correctly on store updates.

#### SUB‑TASK 2.3: Implement Manager Dashboard with Course & Student Assignment
##### Assigned Sub-Agent: coder
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/frontend/src/components/manager/CourseAssignment.tsx
    *   **Architectural Requirements:**
        *   UI for selecting existing courses and assigning students.
        *   Use secure API client with JWT token interception.
        *   Enforce minimum password complexity for auto‑generated Teacher accounts (OWASP A02:2021 – Cryptographic Failures).

### DAY 3: Mobile App Development, Integration Testing & Code Review
#### SUB‑TASK 3.1: Build Mobile Student Dashboard with QR Scanner & Token Expiry
##### Assigned Sub-Agent: coder
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/frontend/mobile/src/screens/StudentDashboard.tsx
    *   **Architectural Requirements:**
        *   Implement QR code scanning using `react-native-camera` or equivalent.
        *   On scan, call attendance API and display remaining token days.
        *   Store token expiry locally using secure storage (RNSecureStorage) and enforce expiration warnings.
        *   Integrate push notifications via Firebase Cloud Messaging (FCM) with token refresh handling.

#### SUB‑TASK 3.2: Write Mobile Integration/E2E Tests
##### Assigned Sub-Agent: tester
##### Targeted Components & Technical Requirements:
*   **Target Path:** INTEGRATION_SCOPE;./sources/frontend/tests/mobile/StudentDashboard.e2e.ts
    *   **Architectural Requirements:**
        *   Simulate QR scan flow, attendance submission, and token expiry display.
        *   Validate push notification receipt and UI update.
        *   Ensure tests run on both iOS and Android emulators.

#### SUB‑TASK 3.3: Perform Static Code Analysis & Security Review
##### Assigned Sub-Agent: reviewer
##### Targeted Components & Technical Requirements:
*   **Target Path:** ./sources/frontend/src/components/auth/Login.tsx
    *   **Architectural Requirements:**
        *   Run ESLint, Prettier, and Snyk checks for vulnerabilities.
        *   Verify implementation of OWASP A03:2021 – Injection (SQL/GraphQL) mitigations in login payload.
        *   Confirm secure password handling (bcrypt hashing on client‑side token storage).