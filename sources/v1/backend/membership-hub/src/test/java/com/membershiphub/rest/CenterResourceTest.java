package org.nlh4j.saas.membership-hub.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestSecurity;
import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Integration tests for {@link CenterResource} covering CRUD operations and RBAC enforcement.
 *
 * @verifies [REQ-004], [REQ-005], [REQ-006]
 */
@QuarkusTest
@QuarkusTestResource(PostgreSQLContainer.class)
public class CenterResourceTest {

    /* --------------------------------------------------------------------- */
    /*  Constants – API endpoints and test payloads                           */
    /* --------------------------------------------------------------------- */
    private static final String BASE_URL = "/api/v1";
    private static final String CENTERS_ENDPOINT = BASE_URL + "/centers";
    private static final String ADMIN_CENTERS_ENDPOINT = BASE_URL + "/admin/centers";
    private static final String ADMIN_CENTER_ADMINS_ENDPOINT = BASE_URL + "/admin/centers/%s/admins";

    /* --------------------------------------------------------------------- */
    /*  Helper – Build a JSON payload for a center                            */
    /* --------------------------------------------------------------------- */
    private String centerPayload(String name, String address, String taxId,
                                String phone, String email) {
        return String.format(
                "{\"name\":\"%s\",\"address\":\"%s\",\"taxId\":\"%s\",\"contactPhone\":\"%s\",\"contactEmail\":\"%s\"}",
                name, address, taxId, phone, email);
    }

    /* --------------------------------------------------------------------- */
    /*  Happy Path – Create a center                                           */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "systemAdmin", roles = {"system-admin"})
    @DisplayName("Create Center – Success [REQ-004]")
    void testCreateCenterSuccess() {
        // Arrange – valid center data
        String payload = centerPayload("Alpha Center", "123 Main St", "1234567890",
                "555-0100", "alpha@example.com");

        // Act – POST to create center
        given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                // Assert – HTTP 201 and returned fields match input
                .statusCode(201)
                .body("name", equalTo("Alpha Center"))
                .body("taxId", equalTo("1234567890"));
    }

    /* --------------------------------------------------------------------- */
    /*  Edge Case – Duplicate taxId conflict                                  */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "systemAdmin", roles = {"system-admin"})
    @DisplayName("Create Center – Duplicate Tax ID [REQ-005]")
    void testCreateCenterDuplicateTaxId() {
        // Arrange – create first center
        String payload1 = centerPayload("Beta Center", "456 Elm St", "9876543210",
                "555-0200", "beta@example.com");
        given()
                .contentType(ContentType.JSON)
                .body(payload1)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                .statusCode(201);

        // Act – attempt to create second center with same taxId
        String payload2 = centerPayload("Gamma Center", "789 Oak St", "9876543210",
                "555-0300", "gamma@example.com");
        given()
                .contentType(ContentType.JSON)
                .body(payload2)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                // Assert – HTTP 409 Conflict
                .statusCode(409)
                .body("error", equalTo("TAX_ID_CONFLICT"));
    }

    /* --------------------------------------------------------------------- */
    /*  Happy Path – Retrieve centers list                                    */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "systemAdmin", roles = {"system-admin"})
    @DisplayName("Get Centers – List [REQ-004]")
    void testGetCenters() {
        // Arrange – ensure at least one center exists
        String payload = centerPayload("Delta Center", "1010 Maple St", "1112223334",
                "555-0400", "delta@example.com");
        given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                .statusCode(201);

        // Act – GET list
        given()
        .when()
                .get(CENTERS_ENDPOINT)
        .then()
                // Assert – list contains the newly created center
                .statusCode(200)
                .body("name", hasItem("Delta Center"));
    }

    /* --------------------------------------------------------------------- */
    /*  Happy Path – Update a center                                           */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "systemAdmin", roles = {"system-admin"})
    @DisplayName("Update Center – Success [REQ-005]")
    void testUpdateCenterSuccess() {
        // Arrange – create center to update
        String payloadCreate = centerPayload("Epsilon Center", "2020 Birch St", "5556667778",
                "555-0500", "epsilon@example.com");
        String centerId = given()
                .contentType(ContentType.JSON)
                .body(payloadCreate)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                .statusCode(201)
                .extract()
                .path("centerId");

        // Act – update center name
        String payloadUpdate = "{\"name\":\"Epsilon Center Updated\",\"address\":\"2020 Birch St\",\"taxId\":\"5556667778\",\"contactPhone\":\"555-0500\",\"contactEmail\":\"epsilon@example.com\"}";
        given()
                .contentType(ContentType.JSON)
                .body(payloadUpdate)
        .when()
                .put(ADMIN_CENTERS_ENDPOINT + "/" + centerId)
        .then()
                // Assert – HTTP 200 and updated name
                .statusCode(200)
                .body("name", equalTo("Epsilon Center Updated"));
    }

    /* --------------------------------------------------------------------- */
    /*  Happy Path – Delete a center                                           */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "systemAdmin", roles = {"system-admin"})
    @DisplayName("Delete Center – Success [REQ-006]")
    void testDeleteCenterSuccess() {
        // Arrange – create center to delete
        String payload = centerPayload("Zeta Center", "3030 Cedar St", "9998887776",
                "555-0600", "zeta@example.com");
        String centerId = given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                .statusCode(201)
                .extract()
                .path("centerId");

        // Act – delete center
        given()
        .when()
                .delete(ADMIN_CENTERS_ENDPOINT + "/" + centerId)
        .then()
                // Assert – HTTP 204 No Content
                .statusCode(204);

        // Verify – center no longer appears in list
        given()
        .when()
                .get(CENTERS_ENDPOINT)
        .then()
                .statusCode(200)
                .body("centerId", not(hasItem(centerId)));
    }

    /* --------------------------------------------------------------------- */
    /*  Happy Path – Assign Center Admin                                       */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "systemAdmin", roles = {"system-admin"})
    @DisplayName("Assign Center Admin – Success [REQ-006]")
    void testAssignCenterAdminSuccess() {
        // Arrange – create center
        String payloadCenter = centerPayload("Eta Center", "4040 Walnut St", "2223334445",
                "555-0700", "eta@example.com");
        String centerId = given()
                .contentType(ContentType.JSON)
                .body(payloadCenter)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                .statusCode(201)
                .extract()
                .path("centerId");

        // Act – assign a user (userId is arbitrary for test)
        String assignPayload = "{\"userId\":\"123e4567-e89b-12d3-a456-426614174000\",\"isAssign\":true}";
        given()
                .contentType(ContentType.JSON)
                .body(assignPayload)
        .when()
                .post(String.format(ADMIN_CENTER_ADMINS_ENDPOINT, centerId))
        .then()
                // Assert – HTTP 200 OK
                .statusCode(200)
                .body("message", equalTo("Thao tác phân quyền trung tâm thành công"));
    }

    /* --------------------------------------------------------------------- */
    /*  Exception Case – Unauthorized access                                 */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "student", roles = {"student"})
    @DisplayName("Create Center – Unauthorized [REQ-006]")
    void testCreateCenterUnauthorized() {
        // Arrange – valid payload
        String payload = centerPayload("Theta Center", "5050 Spruce St", "4445556667",
                "555-0800", "theta@example.com");

        // Act – attempt to create center as a student
        given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                // Assert – HTTP 403 Forbidden
                .statusCode(403);
    }

    /* --------------------------------------------------------------------- */
    /*  Exception Case – Delete non-existent center                          */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "systemAdmin", roles = {"system-admin"})
    @DisplayName("Delete Center – Not Found [REQ-006]")
    void testDeleteCenterNotFound() {
        // Act – delete a random UUID
        given()
        .when()
                .delete(ADMIN_CENTERS_ENDPOINT + "/00000000-0000-0000-0000-000000000000")
        .then()
                // Assert – HTTP 404 Not Found
                .statusCode(404);
    }
}