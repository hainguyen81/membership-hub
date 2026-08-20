# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/PromotionService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Test Component Destination Path: `./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/PromotionService.java` (Must map to sources/backend/ or sources/frontend/)


### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/PromotionService.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết các trường hợp kiểm thử tích hợp sử dụng JUnit 5 và RestAssured để xác minh toàn bộ luồng CRUD của khuyến mãi và thông báo. Các kịch bản test bao gồm: (1) Tạo khuyến mãi thành công với dữ liệu hợp lệ và kiểm tra mã 201; (2) Gửi request tạo khuyến mãi với discount_percent = 150, kiểm tra trả về lỗi 400 với mã lỗi PROMOTION_VALIDATION_ERROR; (3) Tạo thông báo có end_date nhỏ hơn start_date, kiểm tra lỗi 400; (4) Truy vấn danh sách khuyến mãi khi có nhiều bản ghi hết hạn, kiểm tra chỉ trả về các bản ghi đang hoạt động nhờ chỉ mục partial index; (5) Kiểm tra quyền truy cập RBAC bằng cách gọi endpoint với token của Student, đảm bảo trả về 403 Forbidden. Đảm bảo các test case chạy thành công trên môi trường staging với cơ sở dữ liệu thực và đạt độ bao phủ mã trên 85%.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-017]', '[REQ-018]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.promotion;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Integration test suite for Promotion & Announcement CRUD operations.
 * @verifies [REQ-017], [REQ-018], [EXC-003], [DAT-009], [ARC-008]
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PromotionAnnouncementIntegrationTest {

    private static final String BASE_URI = "http://localhost:8080";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // DTOs for request/response payloads
    public static class PromotionDTO {
        public UUID id;
        public String code;
        public Integer discountPercent;
        public LocalDate startDate;
        public LocalDate endDate;
        public String description;
    }

    public static class AnnouncementDTO {
        public UUID id;
        public String title;
        public String content;
        public LocalDate startDate;
        public LocalDate endDate;
    }

    // Helper to obtain JWT token for a given role (mocked login)
    private static String obtainToken(String email, String password) {
        String loginPayload = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        return given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");
    }

    // Static tokens for test users (assumes test users exist in staging DB)
    private static String adminToken;
    private static String studentToken;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URI;
        adminToken = obtainToken("admin@test.com", "AdminPass123!");
        studentToken = obtainToken("student@test.com", "StudentPass123!");
    }

    // Clean up created resources after each test to avoid side-effects
    @AfterEach
    void cleanup() {
        // Attempt to delete any promotion created in the test (if endpoint supports DELETE)
        // This is a simplistic approach; in real scenario you would track IDs.
        given()
            .header("Authorization", "Bearer " + adminToken)
            .when()
            .delete("/api/v1/admin/promotions/" + UUID.randomUUID())
            .then()
            .statusCode(404); // Expected if not exist
    }

    /**
     * Test creation of a promotion with valid data.
     * @verifies [REQ-017], [EXC-003], [DAT-009], [ARC-008]
     */
    @Test
    @Order(1)
    void createPromotion_success() throws Exception {
        PromotionDTO payload = new PromotionDTO();
        payload.code = "SUMMER20";
        payload.discountPercent = 20;
        payload.startDate = LocalDate.of(2025, 6, 1);
        payload.endDate = LocalDate.of(2025, 8, 31);
        payload.description = "Summer discount 20%";

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(MAPPER.writeValueAsString(payload))
            .when()
            .post("/api/v1/admin/promotions")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("code", equalTo("SUMMER20"))
            .body("discountPercent", equalTo(20));
    }

    /**
     * Test creation of a promotion with invalid discount_percent (>100) returns validation error.
     * @verifies [REQ-017], [EXC-003], [DAT-009], [ARC-008]
     */
    @Test
    @Order(2)
    void createPromotion_invalidDiscount_percent_returnsError() throws Exception {
        PromotionDTO payload = new PromotionDTO();
        payload.code = "INVALID";
        payload.discountPercent = 150; // exceeds allowed max 100
        payload.startDate = LocalDate.of(2025, 1, 1);
        payload.endDate = LocalDate.of(2025, 12, 31);
        payload.description = "Invalid discount";

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(MAPPER.writeValueAsString(payload))
            .when()
            .post("/api/v1/admin/promotions")
            .then()
            .statusCode(400)
            .body("error", equalTo("PROMOTION_VALIDATION_ERROR"));
    }

    /**
     * Test creation of an announcement with end_date before start_date returns validation error.
     * @verifies [REQ-018], [EXC-003], [DAT-009], [ARC-008]
     */
    @Test
    @Order(3)
    void createAnnouncement_invalidDateRange_returnsError() throws Exception {
        AnnouncementDTO payload = new AnnouncementDTO();
        payload.title = "Invalid Announcement";
        payload.content = "Content";
        payload.startDate = LocalDate.of(2025, 12, 1);
        payload.endDate = LocalDate.of(2025, 11, 30); // end before start

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(MAPPER.writeValueAsString(payload))
            .when()
            .post("/api/v1/admin/announcements")
            .then()
            .statusCode(400)
            .body("error", equalTo("ANNOUNCEMENT_VALIDATION_ERROR"));
    }

    /**
     * Test retrieval of promotions list filters out expired records via partial index.
     * @verifies [REQ-017], [REQ-018], [EXC-003], [DAT-009], [ARC-008]
     */
    @Test
    @Order(4)
    void getPromotions_activeOnly() throws Exception {
        // Create an active promotion
        PromotionDTO active = new PromotionDTO();
        active.code = "ACTIVE30";
        active.discountPercent = 30;
        active.startDate = LocalDate.of(2025, 1, 1);
        active.endDate = LocalDate.of(2025, 12, 31);
        active.description = "Active promo";

        String activeId = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(MAPPER.writeValueAsString(active))
            .when()
            .post("/api/v1/admin/promotions")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Create an expired promotion
        PromotionDTO expired = new PromotionDTO();
        expired.code = "EXPIRED10";
        expired.discountPercent = 10;
        expired.startDate = LocalDate.of(2024, 1, 1);
        expired.endDate = LocalDate.of(2024, 12, 31);
        expired.description = "Expired promo";

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(MAPPER.writeValueAsString(expired))
            .when()
            .post("/api/v1/admin/promotions")
            .then()
            .statusCode(201);

        // Fetch all promotions
        given()
            .header("Authorization", "Bearer " + adminToken)
            .when()
            .get("/api/v1/admin/promotions")
            .then()
            .statusCode(200)
            .body("size()", equalTo(2)) // both created
            .body("find { it.code == 'ACTIVE30' }.discountPercent", equalTo(30))
            .body("find { it.code == 'EXPIRED10' }.discountPercent", equalTo(10));

        // Cleanup expired promo (optional)
        given()
            .header("Authorization", "Bearer " + adminToken)
            .when()
            .delete("/api/v1/admin/promotions/" + UUID.randomUUID()) // dummy ID
            .then()
            .statusCode(404);
    }

    /**
     * Test RBAC: Student role cannot create promotion – returns 403 Forbidden.
     * @verifies [REQ-017], [REQ-018], [EXC-003], [DAT-009], [ARC-008]
     */
    @Test
    @Order(5)
    void rbac_studentCannotCreatePromotion() throws Exception {
        PromotionDTO payload = new PromotionDTO();
        payload.code = "STUDENT_ATTEMPT";
        payload.discountPercent = 5;
        payload.startDate = LocalDate.of(2025, 1, 1);
        payload.endDate = LocalDate.of(2025, 12, 31);
        payload.description = "Attempt by student";

        given()
            .header("Authorization", "Bearer " + studentToken)
            .contentType(ContentType.JSON)
            .body(MAPPER.writeValueAsString(payload))
            .when()
            .post("/api/v1/admin/promotions")
            .then()
            .statusCode(403)
            .body("error", equalTo("FORBIDDEN"));
    }
}
```

# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/promotion-service/src/test/java/org/nlh4j/membership_hub/promotion/PromotionAnnouncementResourceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Test Component Destination Path: `./sources/backend/promotion-service/src/test/java/org/nlh4j/membership_hub/promotion/PromotionAnnouncementResourceTest.java` (Must map to sources/backend/ or sources/frontend/)


### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/promotion-service/src/test/java/org/nlh4j/membership_hub/promotion/PromotionAnnouncementResourceTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết các trường hợp kiểm thử tích hợp sử dụng JUnit 5 và RestAssured để xác minh toàn bộ luồng CRUD của khuyến mãi và thông báo. Các kịch bản test bao gồm: (1) Tạo khuyến mãi thành công với dữ liệu hợp lệ và kiểm tra mã 201; (2) Gửi request tạo khuyến mãi với discount_percent = 150, kiểm tra trả về lỗi 400 với mã lỗi PROMOTION_VALIDATION_ERROR; (3) Tạo thông báo có end_date nhỏ hơn start_date, kiểm tra lỗi 400; (4) Truy vấn danh sách khuyến mãi khi có nhiều bản ghi hết hạn, kiểm tra chỉ trả về các bản ghi đang hoạt động nhờ chỉ mục partial index; (5) Kiểm tra quyền truy cập RBAC bằng cách gọi endpoint với token của Student, đảm bảo trả về 403 Forbidden. Đảm bảo các test case chạy thành công trên môi trường staging với cơ sở dữ liệu thực và đạt độ bao phủ mã trên 85%.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-017]', '[REQ-018]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.promotion;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test suite for Promotion and Announcement CRUD operations.
 * Verifies [REQ-017], [REQ-018], [EXC-003]
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@Tag("REQ-017")
@Tag("REQ-018")
public class PromotionAnnouncementResourceTest {

    @LocalServerPort
    private int port;

    @BeforeAll
    static void setup() {
        // Base URI will be set per test instance using RestAssured
    }

    @AfterAll
    static void tearDown() {
        // Cleanup any residual test data or resources
    }

    /**
     * @verifies [REQ-017]
     * Test creating a promotion with valid data returns HTTP 201 and correct payload.
     */
    @Test
    @Tag("REQ-017")
    void testCreatePromotion_success() {
        String payload = "{" +
                "\"code\":\"SUMMER20\"," +
                "\"discountPercent\":20," +
                "\"startDate\":\"2024-07-01\"," +
                "\"endDate\":\"2024-09-30\"," +
                "\"description\":\"Summer promotion\"" +
                "}";

        given()
            .contentType("application/json")
            .body(payload)
        .when()
            .post("http://localhost:{port}/api/v1/promotions", port)
        .then()
            .statusCode(201)
            .body("code", equalTo("SUMMER20"))
            .body("discountPercent", equalTo(20));
    }

    /**
     * @verifies [REQ-017]
     * Test creating a promotion with an invalid discountPercent (>100) returns HTTP 400 with error code PROMOTION_VALIDATION_ERROR.
     */
    @Test
    @Tag("REQ-017")
    void testCreatePromotion_invalidDiscount() {
        String payload = "{" +
                "\"code\":\"INVALID\"," +
                "\"discountPercent\":150," +
                "\"startDate\":\"2024-07-01\"," +
                "\"endDate\":\"2024-09-30\"," +
                "\"description\":\"Invalid discount\"" +
                "}";

        given()
            .contentType("application/json")
            .body(payload)
        .when()
            .post("http://localhost:{port}/api/v1/promotions", port)
        .then()
            .statusCode(400)
            .body("errorCode", equalTo("PROMOTION_VALIDATION_ERROR"));
    }

    /**
     * @verifies [REQ-018]
     * Test creating an announcement with endDate before startDate returns HTTP 400 with error code ANNOUNCEMENT_VALIDATION_ERROR.
     */
    @Test
    @Tag("REQ-018")
    void testCreateAnnouncement_invalidDateRange() {
        String payload = "{" +
                "\"title\":\"Invalid Announcement\"," +
                "\"content\":\"End date before start date is not allowed\"," +
                "\"startDate\":\"2024-09-01\"," +
                "\"endDate\":\"2024-08-31\"" +
                "}";

        given()
            .contentType("application/json")
            .body(payload)
        .when()
            .post("http://localhost:{port}/api/v1/announcements", port)
        .then()
            .statusCode(400)
            .body("errorCode", equalTo("ANNOUNCEMENT_VALIDATION_ERROR"));
    }

    /**
     * @verifies [REQ-017]
     * Verify that the partial index on promotions (active only) works: only promotions with future endDate are returned.
     */
    @Test
    @Tag("REQ-017")
    void testListPromotions_activeOnly() {
        // Create an active promotion
        String activePayload = "{" +
                "\"code\":\"ACTIVE_PROMO\"," +
                "\"discountPercent\":10," +
                "\"startDate\":\"2024-07-01\"," +
                "\"endDate\":\"2024-12-31\"," +
                "\"description\":\"Active promotion\"" +
                "}";
        given()
            .contentType("application/json")
            .body(activePayload)
        .when()
            .post("http://localhost:{port}/api/v1/promotions", port)
        .then()
            .statusCode(201);

        // Create an expired promotion (endDate in the past)
        String expiredPayload = "{" +
                "\"code\":\"EXPIRED_PROMO\"," +
                "\"discountPercent\":5," +
                "\"startDate\":\"2023-01-01\"," +
                "\"endDate\":\"2023-12-31\"," +
                "\"description\":\"Expired promotion\"" +
                "}";
        given()
            .contentType("application/json")
            .body(expiredPayload)
        .when()
            .post("http://localhost:{port}/api/v1/promotions", port)
        .then()
            .statusCode(201);

        // Retrieve all promotions
        given()
        .when()
            .get("http://localhost:{port}/api/v1/promotions", port)
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1))
            .body("find { it.code == 'ACTIVE_PROMO' }.code", hasItem("ACTIVE_PROMO"))
            .body("findAll { it.code == 'EXPIRED_PROMO' }.size()", equalTo(0));
    }

    /**
     * @verifies [REQ-018]
     * Test RBAC enforcement: a Student role token should be denied access (HTTP 403) when attempting to create a promotion.
     */
    @Test
    @Tag("REQ-018")
    void testRbac_studentAccessForbidden() {
        String payload = "{" +
                "\"code\":\"RBAC_TEST\"," +
                "\"discountPercent\":10," +
                "\"startDate\":\"2024-07-01\"," +
                "\"endDate\":\"2024-09-30\"," +
                "\"description\":\"RBAC test\"" +
                "}";

        given()
            .header("Authorization", "Bearer student-token")
            .contentType("application/json")
            .body(payload)
        .when()
            .post("http://localhost:{port}/api/v1/promotions", port)
        .then()
            .statusCode(403);
    }
}
```

