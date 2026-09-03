# Day 7: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserCenterIntegrationTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserCenterIntegrationTest.java` (Must map to sources/backend/ or sources/frontend/)




### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserCenterIntegrationTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo tệp kiểm thử tích hợp liên service tại đường dẫn ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserCenterIntegrationTest.java sử dụng Testcontainers (PostgreSQL 16-alpine + Redis 7-alpine) kết hợp WireMock. Tạo 4 kịch bản end-to-end: (1) endToEnd_registerAssignRoleAccessCenter_succeeds - mô phỏng luồng hoàn chỉnh từ đăng ký user mới qua POST /api/v1/users/register với payload JSON {\\"email\\":\\"user@test.com\\",\\"password\\":\\"Strong@123\\",\\"agreedToTerms\\":true} → gán role CenterAdmin qua PUT /api/v1/users/{id}/role với payload {\\"newRoleId\\":2,\\"reason\\":\\"promotion\\"} → truy cập endpoint center GET /api/v1/centers?page=0&size=20&sort=name,asc thành công với HTTP 200 và response chứa content mảng các trung tâm; (2) endToEnd_roleChangeFromCenterAdminToStudent_blocksAccess - xác minh sau khi đổi role từ CenterAdmin (roleId=2) về Student (roleId=5) qua PUT /api/v1/users/{id}/role, endpoint center quản trị POST /api/v1/centers trả về HTTP 403 với mã INSUFFICIENT_PRIVILEGES và JWT cũ bị thêm vào Redis blacklist với key jwt:blacklist:<userId>; (3) endToEnd_socialAuthGoogle_linksAndReusesAccount - mô phỏng đăng ký qua Google OAuth2 với idToken giả lập từ WireMock → liên kết social account trong bảng UserSocialAccounts với provider=GOOGLE → đăng nhập lần sau với cùng idToken không tạo user mới (isNewUser=false); (4) endToEnd_auditLogsConsistentBetweenServices - xác minh đồng bộ audit log giữa user-service và center-service thông qua cùng bảng AuditLogs với các action USER_REGISTERED, ROLE_CHANGED, CENTER_ADMIN_ASSIGNED, CENTER_ADMIN_UNASSIGNED.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[REQ-003]', '[REQ-006]', '[ARC-001]', '[ARC-002]', '[ARC-006]', '[NFR-003]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```markdown
```java
/**
 * User Center Integration Test Suite
 * Validates end-to-end workflows for user registration, role assignment,
 * social authentication linkage, and cross-service audit log consistency.
 * @verifies [REQ-001], [REQ-003], [REQ-006], [ARC-001], [ARC-002], [ARC-006], [NFR-003]
 */
package org.nlh4j.membershiphub.userservice;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RedisContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.github.tomakehurst.wiremock.junit5.WireMockRule;
import static io.restassured.RestAssured.given;

/**
 * End-to-end: Register new user → assign CenterAdmin role → fetch centers list successfully.
 * @verifies [REQ-001], [ARC-001], [ARC-002], [ARC-006], [NFR-003]
 */
@Test
public void endToEnd_registerAssignRoleAccessCenter_succeeds() {
    // Business requirement: Register a new user with valid email, strong password, and agreed terms.
    // Expected: HTTP 201 Created with user ID and JWT access token.
    String registerPayload = "{\"email\":\"user@test.com\",\"password\":\"Strong@123\",\"agreedToTerms\":true}";
    Response registerResponse = given()
        .contentType("application/json")
        .body(registerPayload)
    .when()
        .post("/api/v1/users/register")
    .then()
        .statusCode(201)
        .extract()
        .response();

    String userId = registerResponse.jsonPath().getString("userId");
    String accessToken = registerResponse.jsonPath().getString("accessToken");

    // Business requirement: Assign CenterAdmin role (roleId=2) to the registered user.
    // This grants center-level management privileges, enforcing [ARC-001] and [ARC-002] RBAC.
    given()
        .contentType("application/json")
        .body("{\"newRoleId\":2,\"reason\":\"promotion\"}")
        .header("Authorization", "Bearer " + accessToken)
    .when()
        .put("/api/v1/users/" + userId + "/role")
    .then()
        .statusCode(200)
        .body("newRoleId", is(2));

    // Business requirement: As CenterAdmin, fetch centers list should succeed with HTTP 200.
    // Validates role-based access control and API gateway routing.
    given()
        .header("Authorization", "Bearer " + accessToken)
        .queryParam("page", 0)
        .queryParam("size", 20)
        .queryParam("sort", "name,asc")
    .when()
        .get("/api/v1/centers")
    .then()
        .statusCode(200)
        .body("content", hasSize(greaterThan(0)))
        .body("content.*.name", not(is(emptyOrNullString())));
}

/**
 * Verify role downgrade from CenterAdmin to Student blocks center access with 403.
 * @verifies [REQ-003], [ARC-001], [ARC-002], [ARC-006], [NFR-003]
 */
@Test
public void endToEnd_roleChangeFromCenterAdminToStudent_blocksAccess() {
    // Business requirement: Register a new user and assign CenterAdmin role.
    String registerPayload = "{\"email\":\"admin@test.com\",\"password\":\"Strong@123\",\"agreedToTerms\":true}";
    Response registerResponse = given()
        .contentType("application/json")
        .body(registerPayload)
    .when()
        .post("/api/v1/users/register")
    .then()
        .statusCode(201)
        .extract()
        .response();

    String userId = registerResponse.jsonPath().getString("userId");
    String adminToken = registerResponse.jsonPath().getString("accessToken");

    // Downgrade role from CenterAdmin (2) to Student (roleId=5).
    // Business reason: demotion or role reassignment.
    given()
        .contentType("application/json")
        .body("{\"newRoleId\":5,\"reason\":\"demotion\"}")
        .header("Authorization", "Bearer " + adminToken)
    .when()
        .put("/api/v1/users/" + userId + "/role")
    .then()
        .statusCode(200)
        .body("newRoleId", is(5));

    // Business requirement: Access to center management endpoints must be blocked
    // for users with insufficient privileges, returning HTTP 403 FORBIDDEN.
    // Enforces least-privilege access control per [ARC-001] and [ARC-002].
    given()
        .header("Authorization", "Bearer " + adminToken)
    .when()
        .get("/api/v1/centers?page=0&size=20&sort=name,asc")
    .then()
        .statusCode(403)
        .body("error", is("INSUFFICIENT_PRIVILEGES"));

    // Business requirement: JWT must be invalidated and added to Redis blacklist
    // to ensure session revocation, complying with [NFR-003] security standards.
    String blacklistKey = "jwt:blacklist:" + userId;
    String blacklistedValue = redis.execute("GET", blacklistKey);
    // Assert blacklist entry exists and contains user identifier for auditability.
    if (blacklistedValue == null) {
        throw new AssertionError("JWT blacklist entry not found in Redis");
    }
    if (!blacklistedValue.contains(userId)) {
        throw new AssertionError("Blacklisted JWT must contain user ID: " + userId);
    }
}

/**
 * Simulate Google OAuth2 registration → link social account → reuses account on re-login.
 * @verifies [ARC-006], [NFR-003]
 */
@Test
public void endToEnd_socialAuthGoogle_linksAndReusesAccount() {
    // Business requirement: WireMock mock Google OAuth2 tokeninfo endpoint
    // to simulate valid Google ID token verification without external network calls.
    wireMock.stubFor(
        wireMock.get(urlEqualTo("/oauth2/v3/tokeninfo"))
            .willReturn(wireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyString("{\"email\":\"googleuser@test.com\",\"name\":\"Google User\",\"sub\":\"google-123456\"")));

    // Business requirement: Register/social authenticate via Google OAuth2 provider.
    // Expected: User account created, JWT issued, social account linkage recorded.
    String socialPayload = "{\"provider\":\"google\",\"idToken\":\"fake-google-id-token\"}";
    Response socialResponse = given()
        .contentType("application/json")
        .body(socialPayload)
    .when()
        .post("/api/v1/auth/social")
    .then()
        .statusCode(200)
        .extract()
        .response();

    String userId = socialResponse.jsonPath().getString("userId");
    String accessToken = socialResponse.jsonPath().getString("accessToken");
    boolean isNewUser = socialResponse.jsonPath().getBoolean("isNewUser");

    // First login via social auth must create new user account,
    // validating [ARC-006] OAuth2 integration and onboarding flow.
    if (!isNewUser) {
        throw new AssertionError("First social authentication must create new user account");
    }

    // Business requirement: Linked social account must be persisted in UserSocialAccounts table.
    // Verifies data integrity and cross-service state consistency per [ARC-006] mapping.
    String jdbcUrl = postgres.getJdbcUrl();
    try (var conn = java.sql.DriverManager.getConnection(jdbcUrl, "mhub", "mhub")) {
        var stmt = conn.createStatement();
        var rs = stmt.executeQuery(
            "SELECT provider, provider_user_id FROM UserSocialAccounts WHERE user_id = '" + userId + "'");
        if (!rs.next()) {
            throw new AssertionError("Google social account linkage must be recorded in DB");
        }
        if (!"GOOGLE".equals(rs.getString("provider"))) {
            throw new AssertionError("Provider must be recorded as GOOGLE");
        }
        if (!"google-123456".equals(rs.getString("provider_user_id"))) {
            throw new AssertionError("Provider user ID must match Google sub identifier");
        }
    }

    // Business requirement: Re-login with same Google ID token must reuse existing account,
    // not create duplicate user, ensuring [NFR-003] authentication consistency.
    given()
        .contentType("application/json")
        .header("Authorization", "Bearer " + accessToken)
    .when()
        .get("/api/v1/users/me")
    .then()
        .statusCode(200)
        .body("userId", is(userId))
        .body("isNewUser", is(false));
}

/**
 * Verify audit log consistency between user-service and center-service via AuditLogs table.
 * @verifies [REQ-006]
 */
@Test
public void endToEnd_auditLogsConsistentBetweenServices() {
    // Business requirement: Execute user lifecycle actions that trigger audit logging.
    // Register a new user triggers USER_REGISTERED audit event.
    String registerPayload = "{\"email\":\"audit@test.com\",\"password\":\"Audit@123\",\"agreedToTerms\":true}";
    given()
        .contentType("application/json")
        .body(registerPayload)
    .when()
        .post("/api/v1/users/register")
    .then()
        .statusCode(201);

    // Step 2: Role change triggers ROLE_CHANGED audit event.
    Response regResponse = given()
        .contentType("application/json")
        .body(registerPayload)
    .when()
        .post("/api/v1/users/register")
    .then()
        .statusCode(201)
        .extract()
        .response();
    String userId = regResponse.jsonPath().getString("userId");

    given()
        .contentType("application/json")
        .body("{\"newRoleId\":2,\"reason\":\"proxy\"}")
        .header("Authorization", "Bearer " + regResponse.jsonPath().getString("accessToken"))
    .when()
        .put("/api/v1/users/" + userId + "/role")
    .then()
        .statusCode(200);

    // Business requirement: Query AuditLogs table to verify consistency of recorded events
    // between user-service and center-service, using shared PostgreSQL instance.
    // This supports compliance [NFR-006] and full traceability [DAT-XXX] across microservices.
    String jdbcUrl = postgres.getJdbcUrl();
    try (var conn = java.sql.DriverManager.getConnection(jdbcUrl, "mhub", "mhub")) {
        var stmt = conn.createStatement();

        // Verify USER_REGISTERED audit entry exists with correct target ID.
        var rsUserRegistered = stmt.executeQuery(
            "SELECT action, target_entity, target_id FROM AuditLogs WHERE action = 'USER_REGISTERED' AND target_id = '" + userId + "'");
        if (!rsUserRegistered.next()) {
            throw new AssertionError("Audit log must contain USER_REGISTERED entry");
        }
        if (!"USER_REGISTERED".equals(rsUserRegistered.getString("action"))) {
            throw new AssertionError("Action type must be USER_REGISTERED");
        }

        // Verify ROLE_CHANGED audit entry with correct old/new role values.
        var rsRoleChanged = stmt.executeQuery(
            "SELECT action, old_value, new_value FROM AuditLogs WHERE action = 'ROLE_CHANGED' AND target_id = '" + userId + "'");
        if (!rsRoleChanged.next()) {
            throw new AssertionError("Audit log must contain ROLE_CHANGED entry");
        }
        if (!"5".equals(rsRoleChanged.getString("old_value"))) {
            throw new AssertionError("Old role value must be 5 (Student) prior to change");
        }
        if (!"2".equals(rsRoleChanged.getString("new_value"))) {
            throw new AssertionError("New role value must be 2 (CenterAdmin) after change");
        }

        // Verify CENTER_ADMIN_ASSIGNED and CENTER_ADMIN_UNASSIGNED audit entries
        // are present from the phase's executed test suite operations,
        // ensuring [REQ-006] traceability across all user lifecycle events.
        var rsActions = stmt.executeQuery(
            "SELECT COUNT(*) FROM AuditLogs WHERE action IN ('USER_REGISTERED', 'ROLE_CHANGED', 'CENTER_ADMIN_ASSIGNED', 'CENTER_ADMIN_UNASSIGNED')");
        rsActions.next();
        int actionCount = rsActions.getInt(1);
        if (actionCount != 4) {
            throw new AssertionError("Audit logs must record all specified action types for traceability; found: " + actionCount);
        }
    }
}
```
```

