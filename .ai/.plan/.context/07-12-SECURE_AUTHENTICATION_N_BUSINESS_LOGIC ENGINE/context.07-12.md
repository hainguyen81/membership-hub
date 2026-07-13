# PHASE 2 CONTEXT: SECURE AUTHENTICATION & BUSINESS LOGIC ENGINE (DAYS 7 - 12)

## DAY 7: NEXT.JS UNIFIED FEDERATED LOGIN INTERFACE
### Target Path: ./sources/frontend/src/app/[locale]/login/page.tsx
### Architecture Requirements
Develop a secure login gateway mapping credentials entry components alongside external multi-provider single-sign-on (SSO) interfaces.
- Build UI elements utilizing Tailwind CSS layered on top of Shadcn UI components. Ensure clear loading, success, and rejection layouts.
- Integrate global client transport interceptors inside Axios/Fetch environments. The layer must capture newly issued corporate tokens, cache them within secured browser instances, and dynamically inject tokens into standard Authorization: Bearer outbound headers.

## DAY 8: SECURITY & IDENTITY VERIFICATION TESTING SUITE
### Target Path: ./sources/backend/src/test/java/org/nlh4j/saas/membership/hub/service/AuthServiceTest.java
### Architecture Requirements
Structure isolated behavioral test suites safeguarding critical access control boundaries across backend and frontend environments.
- Backend: Author unit test cases using QuarkusComponentTest to verify credentials rejection flows, incorrect tenant parameters routing, and custom token claims verification logic.
- Frontend: Implement component testing inside Jest using React Testing Library to simulate client credential input rejections and lifecycle route redirect states.
- Ensure all automated testing models operate free from live third-party network configurations to guarantee zero execution blocking loops inside the pipeline.

## DAY 9: CRYPTOGRAPHIC DYNAMIC QR ENGINE SERVICES
### Target Path: ./sources/backend/src/main/java/org/nlh4j/saas/membership/hub/service/QrEngineService.java
### Architecture Requirements
Implement an enterprise-grade symmetric cryptographic service securing check-in transaction tokens against duplication or tampering vectors.
- Engineer data serialization methods utilizing the AES-256-GCM encryption block algorithm. The generated payload string must bundle student metadata parameters: student_id, card_id, tenant_id, and a precision tracking timestamp.
- Formulate a strict decryption utility containing real-time temporal verification guard controls. The evaluation logic must instantly reject decrypted data structures containing timestamps older than 30 seconds to negate screenshot-sharing replay attacks.

## DAY 10: IDEMPOTENT CHECK-IN TRANSACTIONS CONTROLLER
### Target Path: ./sources/backend/src/main/java/org/nlh4j/saas/membership/hub/web/AttendanceResource.java
### Architecture Requirements
Expose high-throughput non-blocking REST endpoints ingesting dynamic encrypted student barcode payloads.
- Connect incoming string packages straight to the QrEngineService layer to unmarshal core tracking parameters safely.
- Enforce transaction idempotency controls by executing an assertion check inside the database bounded by the composite index matching (tenant_id, student_id, CURRENT_DATE). If an existing check-in entry matches, short-circuit processing loops instantly and return an HTTP 200 OK status coupled with a structural duplication warning flag.
- Enforce strict OpenAPI 3.0 specs compliance: Decorate endpoints with @Operation(summary = "Process dynamic QR checkin", description = "Decrupts token and performs idempotent gym-style checkin logs assertion"). Inject @APIResponse(responseCode = "200", description = "Attendance recorded or duplicated match absorbed gracefully") and @APIResponse(responseCode = "400", description = "Expired or corrupted QR code security string payload").

## DAY 11: DYNAMIC TIME-BASED CARD SUBSCRIPTION MANAGER
### Target Path: ./sources/backend/src/main/java/org/nlh4j/saas/membership/hub/service/impl/CardEvaluationServiceImpl.java
### Architecture Requirements
Develop a dynamic duration engine reflecting explicit gym management tracking properties, completely free from stagnant static integer day attributes.
- Initialize entity domains, repositories, MapStruct mappers, and immutable validation DTO models tracking sys_promotions and notification_delivery_logs.
- Author subscription tracking methods that compute time balances on-the-fly at runtime by measuring the difference between the system clock and the hard expiration parameters: remaining_days = max(0, expired_at - CURRENT_DATE). If tracking returns 0 values, execute immediate state mutations to switch target account visibility flags to an inactive state.
- Expose basic underlying reactive queries fetching active campaigns and student-specific delivery logs bounded strictly by tenant isolation rules.
- Ingest and generate persistent domain entities mappings, repositories frameworks, DTO blueprints, and MapStruct mappers configurations tracking both sys_promotions and notification_delivery_logs enterprise schema blocks.

## DAY 12: MOBILE MEMBERSHIP DATA TERMINAL LAYOUT
### Target Path: ./sources/frontend/src/app/[locale]/app/dashboard/page.tsx
### Architecture Requirements
Design a modern di động viewport console mapping individual profile parameters directly into localized student interfaces.
- Embed interactive canvas rendering frameworks that display dynamic secure barcodes polled from backend cryptographic engines.
- Mount immediate layout alert boxes catching profile state updates to render high-visibility cards notifying students of time limits: e.g., "Your pass has X days remaining".
- Optimize code layouts to maintain strict layout compatibility rules with mobile tai thỏ and notch styles, enforcing proper Tailwind CSS environment bounds variables (pt-[env(safe-area-inset-top)]).
