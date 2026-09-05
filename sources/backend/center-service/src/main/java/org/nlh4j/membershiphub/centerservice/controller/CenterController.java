// [REQ-004] [REQ-005]
package org.nlh4j.membershiphub.centerservice.controller;

// [REQ-004] [REQ-005] Import necessary enterprise packages and validation frameworks
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.nlh4j.membershiphub.centerservice.service.CenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Enterprise Center Controller for managing multi-tenant membership hubs.
 * Exposes REST endpoints for listing, creating, updating, and soft-deleting centers.
 * 
 * @traceability [REQ-004], [REQ-005]
 */
@RestController
@RequestMapping("/api/v1/centers")
@Validated
public class CenterController {

    // [REQ-004] [REQ-005] Top-of-class immutable constants and enterprise logger
    private static final Logger logger = LoggerFactory.getLogger(CenterController.class);
    private static final String PROCESS_LOG_TEMPLATE = "[PROCESS] CenterController operation: {} initiated for tenant/system context";
    private static final String COMPLETION_LOG_TEMPLATE = "[PROCESS] CenterController operation: {} completed successfully";
    private static final String ERROR_LOG_TEMPLATE = "[CRITICAL FAIL] [ARC-007] CenterController operation failed during {}. Raw error: {}";

    private final CenterService centerService;

    /**
     * Constructor-based dependency injection for CenterService.
     * 
     * @param centerService the center business service node
     */
    public CenterController(CenterService centerService) {
        this.centerService = centerService;
    }

    /**
     * Retrieves a paginated list of centers for any authenticated user.
     * 
     * @param pageable pagination and sorting parameters (default size=20, sort=name,asc)
     * @return ResponseEntity containing a page of CenterResponse DTOs
     * @traceability [REQ-004]
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CenterResponse>> listCenters(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        logger.info(PROCESS_LOG_TEMPLATE, "listCenters");
        try {
            // [REQ-004] Delegate pagination query directly to service and indexed database layer
            Page<CenterResponse> centers = centerService.listCenters(pageable);
            logger.info(COMPLETION_LOG_TEMPLATE, "listCenters");
            return ResponseEntity.ok(centers);
        } catch (Exception e) {
            logger.error(ERROR_LOG_TEMPLATE, "listCenters", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a new center in the system. Restricted exclusively to System Administrators.
     * 
     * @param request the validated center creation payload
     * @return ResponseEntity with HTTP 201 Created and the created CenterResponse
     * @traceability [REQ-005]
     */
    @PostMapping
    @PreAuthorize("hasRole('SystemAdmin')")
    public ResponseEntity<CenterResponse> createCenter(@Valid @RequestBody CenterRequest request) {
        logger.info(PROCESS_LOG_TEMPLATE, "createCenter");
        try {
            // [REQ-005] Validate TaxID uniqueness and invoke service layer persistence
            CenterResponse createdCenter = centerService.createCenter(request);
            logger.info(COMPLETION_LOG_TEMPLATE, "createCenter");
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCenter);
        } catch (Exception e) {
            logger.error(ERROR_LOG_TEMPLATE, "createCenter", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates an existing center's details. Accessible by System Admins or Center Admins.
     * 
     * @param id the unique UUID of the center
     * @param request the updated center information payload
     * @return ResponseEntity with HTTP 200 OK and the updated CenterResponse
     * @traceability [REQ-005]
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SystemAdmin','CenterAdmin')")
    public ResponseEntity<CenterResponse> updateCenter(
            @PathVariable UUID id,
            @Valid @RequestBody CenterRequest request) {
        logger.info(PROCESS_LOG_TEMPLATE, "updateCenter");
        try {
            // [REQ-005] Execute secure atomic update of center entity attributes
            CenterResponse updatedCenter = centerService.updateCenter(id, request);
            logger.info(COMPLETION_LOG_TEMPLATE, "updateCenter");
            return ResponseEntity.ok(updatedCenter);
        } catch (Exception e) {
            logger.error(ERROR_LOG_TEMPLATE, "updateCenter", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Performs a soft delete on a center by marking is_deleted as true. Restricted to System Admins.
     * 
     * @param id the unique UUID of the center to soft delete
     * @return ResponseEntity with HTTP 204 No Content upon successful execution
     * @traceability [REQ-005]
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SystemAdmin')")
    public ResponseEntity<Void> deleteCenter(@PathVariable UUID id) {
        logger.info(PROCESS_LOG_TEMPLATE, "deleteCenter");
        try {
            // [REQ-005] Execute soft deletion flag update and audit log recording
            centerService.softDeleteCenter(id);
            logger.info(COMPLETION_LOG_TEMPLATE, "deleteCenter");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error(ERROR_LOG_TEMPLATE, "deleteCenter", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * DTO representing the Center creation and update request payload with strict Jakarta Bean Validation.
     * Enforces strict regex validation for TaxID [REQ-005] and email formatting.
     */
    public static class CenterRequest {
        @NotBlank(message = "Center name must not be blank")
        @Size(max = 100, message = "Center name cannot exceed 100 characters")
        private String name;

        @NotBlank(message = "Address must not be blank")
        @Size(max = 255, message = "Address cannot exceed 255 characters")
        private String address;

        @NotBlank(message = "Tax ID must not be blank")
        @Pattern(regexp = "^[0-9]{10,13}$", message = "Tax ID must consist of 10 to 13 digits")
        private String taxId;

        @Size(max = 20, message = "Contact phone cannot exceed 20 characters")
        private String contactPhone;

        @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2}$", message = "Invalid email format")
        @Size(max = 100, message = "Contact email cannot exceed 100 characters")
        private String contactEmail;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getTaxId() { return taxId; }
        public void setTaxId(String taxId) { this.taxId = taxId; }
        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
        public String getContactEmail() { return contactEmail; }
        public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    }

    /**
     * DTO representing the Center response payload returned to clients.
     */
    public static class CenterResponse {
        private UUID centerId;
        private String name;
        private String address;
        private String taxId;
        private String adminContact;

        public UUID getCenterId() { return centerId; }
        public void setCenterId(UUID centerId) { this.centerId = centerId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getTaxId() { return taxId; }
        public void setTaxId(String taxId) { this.taxId = taxId; }
        public String getAdminContact() { return adminContact; }
        public void setAdminContact(String adminContact) { this.adminContact = adminContact; }
    }
}