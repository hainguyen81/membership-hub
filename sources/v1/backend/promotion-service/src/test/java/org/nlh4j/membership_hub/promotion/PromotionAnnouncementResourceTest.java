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