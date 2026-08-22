# Day 3: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/center-service/src/main/java/org/nlh4j/membership_hub/center/CenterService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/center-service/src/main/java/org/nlh4j/membership_hub/center/CenterService.java`
*   Traceability Audit Tags For This Task: ['[REQ-005]', '[ARC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Xây dựng dịch vụ quản lý trung tâm (CRUD)']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-005]', '[ARC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.center;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.nlh4j.saas.membership_hub.center.model.Center;
import org.nlh4j.saas.membership_hub.center.repository.CenterRepository;

import java.util.List;
import java.util.UUID;

/**
 * Service class for managing Center entities with CRUD operations and RBAC compliance.
 * Traceability Tags: [REQ-005], [ARC-002]
 */
@Service
public class CenterService {

    private static final Logger logger = LoggerFactory.getLogger(CenterService.class);

    /* --------------------------------------------------------------------- */
    /* CONSTANTS – Anti‑Magic‑Numbers enforcement – all configuration values   */
    /* --------------------------------------------------------------------- */
    /** Error message template when a center is not found. */
    public static final String CENTER_NOT_FOUND = "Center not found with id: %s";
    /** Error message template when a duplicate tax ID is supplied. */
    public static final String CENTER_DUPLICATE_TAX_ID = "Duplicate tax ID: %s";
    /** Error message template when a delete is attempted on a center with related data. */
    public static final String CENTER_DELETE_WITH_ASSOCIATED_DATA = "Attempt to delete center with associated enrollments or courses: %s";

    /* --------------------------------------------------------------------- */
    /* DEPENDENCY INJECTION – Repository layer for data access               */
    /* --------------------------------------------------------------------- */
    @Autowired
    private CenterRepository centerRepository;

    /**
     * Create a new Center.
     * Traceability Tags: [REQ-005], [ARC-002]
     *
     * @param center Center entity to create
     * @return created Center
     */
    @Transactional
    public Center createCenter(Center center) {
        logger.info("[ENTRY] createCenter called with centerName: {}", center.getName());
        try {
            // Validate tax ID uniqueness – prevents duplicate centers
            if (centerRepository.existsByTaxId(center.getTaxId())) {
                String errorMsg = String.format(CENTER_DUPLICATE_TAX_ID, center.getTaxId());
                logger.error("[ERROR] [REQ-005] [ARC-002] Duplicate tax ID encountered: {}", errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
            Center saved = centerRepository.save(center);
            logger.debug("[DEBUG] Center created with id: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            // Comprehensive exception logging – preserves original cause and Tag IDs
            logger.error("[ERROR] [REQ-005] [ARC-002] Failed to create center. Raw error: {}", e.getMessage(), e);
            throw e;
        } finally {
            logger.info("[EXIT] createCenter completed");
        }
    }

    /**
     * Retrieve a Center by its ID.
     * Traceability Tags: [REQ-005], [ARC-002]
     *
     * @param id Center UUID
     * @return Center or throws IllegalArgumentException if not found
     */
    @Transactional(readOnly = true)
    public Center getCenterById(UUID id) {
        logger.info("[ENTRY] getCenterById called with id: {}", id);
        try {
            return centerRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMsg = String.format(CENTER_NOT_FOUND, id);
                    logger.error("[ERROR] [REQ-005] [ARC-002] Center not found: {}", errorMsg);
                    return new IllegalArgumentException(errorMsg);
                });
        } catch (Exception e) {
            logger.error("[ERROR] [REQ-005] [ARC-002] Error retrieving center. Raw error: {}", e.getMessage(), e);
            throw e;
        } finally {
            logger.info("[EXIT] getCenterById completed");
        }
    }

    /**
     * Update an existing Center.
     * Traceability Tags: [REQ-005], [ARC-002]
     *
     * @param center Center entity with updated data
     * @return updated Center
     */
    @Transactional
    public Center updateCenter(Center center) {
        logger.info("[ENTRY] updateCenter called with id: {}", center.getId());
        try {
            Center existing = getCenterById(center.getId());
            // Apply updates to mutable fields
            existing.setName(center.getName());
            existing.setAddress(center.getAddress());
            existing.setTaxId(center.getTaxId());
            existing.setContactPhone(center.getContactPhone());
            existing.setContactEmail(center.getContactEmail());
            Center updated = centerRepository.save(existing);
            logger.debug("[DEBUG] Center updated with id: {}", updated.getId());
            return updated;
        } catch (Exception e) {
            logger.error("[ERROR] [REQ-005] [ARC-002] Failed to update center. Raw error: {}", e.getMessage(), e);
            throw e;
        } finally {
            logger.info("[EXIT] updateCenter completed");
        }
    }

    /**
     * Delete a Center by its ID.
     * Traceability Tags: [REQ-005], [ARC-002]
     *
     * @param id Center UUID to delete
     */
    @Transactional
    public void deleteCenter(UUID id) {
        logger.info("[ENTRY] deleteCenter called with id: {}", id);
        try {
            Center center = getCenterById(id);
            // Business rule: cannot delete if there are associated courses or enrollments
            if (!center.getCourses().isEmpty() || !center.getEnrollments().isEmpty()) {
                String errorMsg = String.format(CENTER_DELETE_WITH_ASSOCIATED_DATA, id);
                logger.error("[ERROR] [REQ-005] [ARC-002] Cannot delete center with associated data: {}", errorMsg);
                throw new IllegalStateException(errorMsg);
            }
            centerRepository.deleteById(id);
            logger.debug("[DEBUG] Center deleted with id: {}", id);
        } catch (Exception e) {
            logger.error("[ERROR] [REQ-005] [ARC-002] Failed to delete center. Raw error: {}", e.getMessage(), e);
            throw e;
        } finally {
            logger.info("[EXIT] deleteCenter completed");
        }
    }

    /**
     * Retrieve all Centers.
     * Traceability Tags: [REQ-005], [ARC-002]
     *
     * @return list of all Centers
     */
    @Transactional(readOnly = true)
    public List<Center> findAllCenters() {
        logger.info("[ENTRY] findAllCenters called");
        try {
            List<Center> centers = centerRepository.findAll();
            logger.debug("[DEBUG] Retrieved {} centers", centers.size());
            return centers;
        } catch (Exception e) {
            logger.error("[ERROR] [REQ-005] [ARC-002] Failed to retrieve all centers. Raw error: {}", e.getMessage(), e);
            throw e;
        } finally {
            logger.info("[EXIT] findAllCenters completed");
        }
    }
}
```

# Day 3: model openai/gpt-oss-20b:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/center-service/src/main/java/org/nlh4j/membership_hub/center/CenterResource.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/center-service/src/main/java/org/nlh4j/membership_hub/center/CenterResource.java`
*   Traceability Audit Tags For This Task: ['[REQ-004]', '[REQ-005]', '[ARC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Xây dựng API CRUD quản lý trung tâm']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-004]', '[REQ-005]', '[ARC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * REST controller for managing centers (CRUD operations).
 *
 * <p>Traceability tags: [REQ-004], [REQ-005], [ARC-002]</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Expose endpoints for listing, retrieving, creating, updating and deleting centers.</li>
 *   <li>Enforce RBAC: only users with the {@code SYSTEM_ADMIN} role may access these endpoints.</li>
 *   <li>Validate input data using Bean Validation annotations.</li>
 *   <li>Delegate persistence logic to {@link CenterService}.</li>
 *   <li>Provide comprehensive logging and exception handling compliant with enterprise audit requirements.</li>
 * </ul>
 *
 * @author
 * @since 1.0
 */
package org.nlh4j.saas.membership_hub.center;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * DTO representing a center. Validation annotations are used to enforce business rules.
 */
class CenterDTO {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Address must not be blank")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @NotBlank(message = "Tax ID must not be blank")
    @Pattern(regexp = "^[0-9]{10,13}$", message = "Tax ID must be 10 to 13 digits")
    private String taxId;

    @Pattern(regexp = "^\\+?[0-9]{7,20}$", message = "Contact phone must be a valid phone number")
    private String contactPhone;

    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Contact email must be a valid email address")
    private String contactEmail;

    // Getters and setters omitted for brevity
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
 * REST controller for center CRUD operations.
 *
 * @traceability [REQ-004], [REQ-005], [ARC-002]
 */
@RestController
@RequestMapping(CenterResource.BASE_PATH)
@Validated
public class CenterResource {

    /* --------------------------------------------------------------------- */
    /*  Constants (no hard‑coded literals in business logic)                 */
    /* --------------------------------------------------------------------- */

    /** Base API path for center resources. */
    static final String BASE_PATH = "/api/v1/centers";

    /** Role required to access these endpoints. */
    static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";

    /** Tag identifiers for traceability. */
    static final String TAG_REQ_004 = "[REQ-004]";
    static final String TAG_REQ_005 = "[REQ-005]";
    static final String TAG_ARC_002 = "[ARC-002]";

    /** Error messages. */
    static final String MSG_CENTER_NOT_FOUND = "Center not found";
    static final String MSG_DUPLICATE_TAX_ID = "Duplicate tax ID";
    static final String MSG_INVALID_INPUT = "Invalid input";

    /* --------------------------------------------------------------------- */
    /*  Logger (audit compliant, no sensitive data logged)                    */
    /* --------------------------------------------------------------------- */

    private static final Logger logger = LoggerFactory.getLogger(CenterResource.class);

    /* --------------------------------------------------------------------- */
    /*  Dependencies (injected by Spring)                                    */
    /* --------------------------------------------------------------------- */

    @Autowired
    private CenterService centerService;

    /* --------------------------------------------------------------------- */
    /*  GET /centers – list all centers                                      */
    /* --------------------------------------------------------------------- */

    @GetMapping
    @PreAuthorize("hasRole('" + ROLE_SYSTEM_ADMIN + "')")
    public ResponseEntity<List<CenterDTO>> getAllCenters() {
        logger.info("[{}] Entry: getAllCenters()", TAG_REQ_004);
        try {
            List<Center> centers = centerService.findAll();
            List<CenterDTO> dtos = centers.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            logger.debug("[{}] Retrieved {} centers", TAG_REQ_004, dtos.size());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("[{}] Unexpected error in getAllCenters: {}", TAG_REQ_004, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, MSG_INVALID_INPUT, e);
        } finally {
            logger.info("[{}] Exit: getAllCenters()", TAG_REQ_004);
        }
    }

    /* --------------------------------------------------------------------- */
    /*  GET /centers/{id} – retrieve a single center                         */
    /* --------------------------------------------------------------------- */

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('" + ROLE_SYSTEM_ADMIN + "')")
    public ResponseEntity<CenterDTO> getCenterById(@PathVariable("id") UUID id) {
        logger.info("[{}] Entry: getCenterById(id={})", TAG_REQ_004, id);
        try {
            Optional<Center> opt = centerService.findById(id);
            if (opt.isEmpty()) {
                logger.warn("[{}] Center not found: {}", TAG_REQ_004, id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_CENTER_NOT_FOUND);
            }
            CenterDTO dto = toDTO(opt.get());
            logger.debug("[{}] Center found: {}", TAG_REQ_004, id);
            return ResponseEntity.ok(dto);
        } catch (ResponseStatusException e) {
            throw e; // re‑throw to preserve status
        } catch (Exception e) {
            logger.error("[{}] Unexpected error in getCenterById: {}", TAG_REQ_004, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, MSG_INVALID_INPUT, e);
        } finally {
            logger.info("[{}] Exit: getCenterById(id={})", TAG_REQ_004, id);
        }
    }

    /* --------------------------------------------------------------------- */
    /*  POST /centers – create a new center                                  */
    /* --------------------------------------------------------------------- */

    @PostMapping
    @PreAuthorize("hasRole('" + ROLE_SYSTEM_ADMIN + "')")
    @Transactional
    public ResponseEntity<CenterDTO> createCenter(@Valid @RequestBody CenterDTO dto) {
        logger.info("[{}] Entry: createCenter(dto={})", TAG_REQ_005, dto.getName());
        try {
            Center center = toEntity(dto);
            Center saved = centerService.save(center);
            CenterDTO responseDto = toDTO(saved);
            logger.debug("[{}] Center created with id={}", TAG_REQ_005, saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (DuplicateTaxIdException e) {
            logger.warn("[{}] Duplicate tax ID: {}", TAG_REQ_005, dto.getTaxId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, MSG_DUPLICATE_TAX_ID, e);
        } catch (Exception e) {
            logger.error("[{}] Unexpected error in createCenter: {}", TAG_REQ_005, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, MSG_INVALID_INPUT, e);
        } finally {
            logger.info("[{}] Exit: createCenter(dto={})", TAG_REQ_005, dto.getName());
        }
    }

    /* --------------------------------------------------------------------- */
    /*  PUT /centers/{id} – update an existing center                        */
    /* --------------------------------------------------------------------- */

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('" + ROLE_SYSTEM_ADMIN + "')")
    @Transactional
    public ResponseEntity<CenterDTO> updateCenter(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CenterDTO dto) {
        logger.info("[{}] Entry: updateCenter(id={}, dto={})", TAG_REQ_005, id, dto.getName());
        try {
            Optional<Center> opt = centerService.findById(id);
            if (opt.isEmpty()) {
                logger.warn("[{}] Center not found for update: {}", TAG_REQ_005, id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_CENTER_NOT_FOUND);
            }
            Center existing = opt.get();
            // Update mutable fields
            existing.setName(dto.getName());
            existing.setAddress(dto.getAddress());
            existing.setTaxId(dto.getTaxId());
            existing.setContactPhone(dto.getContactPhone());
            existing.setContactEmail(dto.getContactEmail());
            Center updated = centerService.save(existing);
            CenterDTO responseDto = toDTO(updated);
            logger.debug("[{}] Center updated: {}", TAG_REQ_005, id);
            return ResponseEntity.ok(responseDto);
        } catch (DuplicateTaxIdException e) {
            logger.warn("[{}] Duplicate tax ID on update: {}", TAG_REQ_005, dto.getTaxId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, MSG_DUPLICATE_TAX_ID, e);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[{}] Unexpected error in updateCenter: {}", TAG_REQ_005, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, MSG_INVALID_INPUT, e);
        } finally {
            logger.info("[{}] Exit: updateCenter(id={}, dto={})", TAG_REQ_005, id, dto.getName());
        }
    }

    /* --------------------------------------------------------------------- */
    /*  DELETE /centers/{id} – delete a center                               */
    /* --------------------------------------------------------------------- */

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('" + ROLE_SYSTEM_ADMIN + "')")
    @Transactional
    public ResponseEntity<Void> deleteCenter(@PathVariable("id") UUID id) {
        logger.info("[{}] Entry: deleteCenter(id={})", TAG_REQ_005, id);
        try {
            if (!centerService.existsById(id)) {
                logger.warn("[{}] Center not found for deletion: {}", TAG_REQ_005, id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_CENTER_NOT_FOUND);
            }
            centerService.deleteById(id);
            logger.debug("[{}] Center deleted: {}", TAG_REQ_005, id);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[{}] Unexpected error in deleteCenter: {}", TAG_REQ_005, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, MSG_INVALID_INPUT, e);
        } finally {
            logger.info("[{}] Exit: deleteCenter(id={})", TAG_REQ_005, id);
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Helper methods – DTO ↔ Entity conversion                            */
    /* --------------------------------------------------------------------- */

    /**
     * Convert a {@link Center} entity to a {@link CenterDTO}.
     *
     * @param entity the entity to convert
     * @return the DTO representation
     */
    private CenterDTO toDTO(Center entity) {
        CenterDTO dto = new CenterDTO();
        dto.setName(entity.getName());
        dto.setAddress(entity.getAddress());
        dto.setTaxId(entity.getTaxId());
        dto.setContactPhone(entity.getContactPhone());
        dto.setContactEmail(entity.getContactEmail());
        return dto;
    }

    /**
     * Convert a {@link CenterDTO} to a {@link Center} entity.
     *
     * @param dto the DTO to convert
     * @return the entity representation
     */
    private Center toEntity(CenterDTO dto) {
        Center entity = new Center();
        entity.setName(dto.getName());
        entity.setAddress(dto.getAddress());
        entity.setTaxId(dto.getTaxId());
        entity.setContactPhone(dto.getContactPhone());
        entity.setContactEmail(dto.getContactEmail());
        return entity;
    }
}
```

# Day 3: model openai/gpt-oss-20b:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/center-service/src/main/java/org/nlh4j/membership_hub/center/CenterAdminService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/center-service/src/main/java/org/nlh4j/membership_hub/center/CenterAdminService.java`
*   Traceability Audit Tags For This Task: ['[REQ-006]', '[ARC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai chức năng gán/huỷ gán quản trị viên trung tâm']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-006]', '[ARC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.center;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.nlh4j.saas.membership_hub.user.UserRepository;
import org.nlh4j.saas.membership_hub.user.User;
import org.nlh4j.saas.membership_hub.center.CenterRepository;
import org.nlh4j.saas.membership_hub.center.Center;
import org.nlh4j.saas.membership_hub.center.CenterAdminRepository;
import org.nlh4j.saas.membership_hub.center.CenterAdmin;

/**
 * Service responsible for assigning and unassigning center administrators.
 *
 * <p>
 * This service implements the business logic for the {@code /admin/centers/{centerId}/admins}
 * endpoint. It ensures that only existing users can be granted or revoked the
 * {@code Center Admin} role for a specific center. All operations are performed
 * within a single transaction to guarantee consistency.
 * </p>
 *
 * @traceability [REQ-006], [ARC-002]
 */
@Service
public class CenterAdminService {

    /* --------------------------------------------------------------------- */
    /*  Constants (no hard‑coded literals in business logic)                 */
    /* --------------------------------------------------------------------- */

    /** Message key for user not found error. */
    private static final String MSG_USER_NOT_FOUND = "User with ID %d does not exist";

    /** Message key for center not found error. */
    private static final String MSG_CENTER_NOT_FOUND = "Center with ID %d does not exist";

    /** Message key for duplicate assignment error. */
    private static final String MSG_ALREADY_ASSIGNED = "User %d is already a Center Admin for center %d";

    /** Message key for successful assignment. */
    private static final String MSG_ASSIGN_SUCCESS = "User %d assigned as Center Admin for center %d";

    /** Message key for successful unassignment. */
    private static final String MSG_UNASSIGN_SUCCESS = "User %d unassigned from Center Admin role for center %d";

    /* --------------------------------------------------------------------- */
    /*  Logger (audit trail)                                                */
    /* --------------------------------------------------------------------- */

    private static final Logger logger = LoggerFactory.getLogger(CenterAdminService.class);

    /* --------------------------------------------------------------------- */
    /*  Repositories (injected by Spring)                                  */
    /* --------------------------------------------------------------------- */

    private final UserRepository userRepository;
    private final CenterRepository centerRepository;
    private final CenterAdminRepository centerAdminRepository;

    /**
     * Constructor injection of required repositories.
     *
     * @param userRepository          repository for {@link User} entities
     * @param centerRepository        repository for {@link Center} entities
     * @param centerAdminRepository   repository for {@link CenterAdmin} entities
     */
    public CenterAdminService(UserRepository userRepository,
                              CenterRepository centerRepository,
                              CenterAdminRepository centerAdminRepository) {
        this.userRepository = userRepository;
        this.centerRepository = centerRepository;
        this.centerAdminRepository = centerAdminRepository;
    }

    /* --------------------------------------------------------------------- */
    /*  Public API                                                          */
    /* --------------------------------------------------------------------- */

    /**
     * Assigns a user as a Center Administrator for the specified center.
     *
     * <p>
     * The method performs the following steps atomically:
     * <ul>
     *   <li>Validate that the center exists.</li>
     *   <li>Validate that the user exists.</li>
     *   <li>Check for an existing assignment to avoid duplicates.</li>
     *   <li>Persist a new {@link CenterAdmin} record.</li>
     * </ul>
     * </p>
     *
     * @param centerId the ID of the center
     * @param userId   the ID of the user to assign
     * @throws IllegalArgumentException if the center or user does not exist,
     *                                  or if the assignment already exists
     */
    @Transactional
    public void assignCenterAdmin(Long centerId, Long userId) {
        logger.info("[PROCESS] Assigning user {} as Center Admin for center {}", userId, centerId);

        // Validate center existence
        Center center = centerRepository.findById(centerId)
                .orElseThrow(() -> {
                    String msg = String.format(MSG_CENTER_NOT_FOUND, centerId);
                    logger.error("[ERROR] {}", msg);
                    return new IllegalArgumentException(msg);
                });

        // Validate user existence
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String msg = String.format(MSG_USER_NOT_FOUND, userId);
                    logger.error("[ERROR] {}", msg);
                    return new IllegalArgumentException(msg);
                });

        // Prevent duplicate assignment
        Optional<CenterAdmin> existing = centerAdminRepository.findByCenterAndUser(center, user);
        if (existing.isPresent()) {
            String msg = String.format(MSG_ALREADY_ASSIGNED, userId, centerId);
            logger.warn("[WARN] {}", msg);
            throw new IllegalStateException(msg);
        }

        // Persist new assignment
        CenterAdmin assignment = new CenterAdmin();
        assignment.setCenter(center);
        assignment.setUser(user);
        centerAdminRepository.save(assignment);

        logger.info("[SUCCESS] {}", String.format(MSG_ASSIGN_SUCCESS, userId, centerId));
    }

    /**
     * Unassigns a user from the Center Administrator role for the specified center.
     *
     * <p>
     * The method performs the following steps atomically:
     * <ul>
     *   <li>Validate that the center exists.</li>
     *   <li>Validate that the user exists.</li>
     *   <li>Locate the existing assignment.</li>
     *   <li>Delete the assignment record.</li>
     * </ul>
     * </p>
     *
     * @param centerId the ID of the center
     * @param userId   the ID of the user to unassign
     * @throws IllegalArgumentException if the center or user does not exist,
     *                                  or if no assignment exists
     */
    @Transactional
    public void unassignCenterAdmin(Long centerId, Long userId) {
        logger.info("[PROCESS] Unassigning user {} from Center Admin role for center {}", userId, centerId);

        // Validate center existence
        Center center = centerRepository.findById(centerId)
                .orElseThrow(() -> {
                    String msg = String.format(MSG_CENTER_NOT_FOUND, centerId);
                    logger.error("[ERROR] {}", msg);
                    return new IllegalArgumentException(msg);
                });

        // Validate user existence
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String msg = String.format(MSG_USER_NOT_FOUND, userId);
                    logger.error("[ERROR] {}", msg);
                    return new IllegalArgumentException(msg);
                });

        // Locate assignment
        CenterAdmin assignment = centerAdminRepository.findByCenterAndUser(center, user)
                .orElseThrow(() -> {
                    String msg = String.format("No Center Admin assignment found for user %d and center %d", userId, centerId);
                    logger.warn("[WARN] {}", msg);
                    return new IllegalStateException(msg);
                });

        // Delete assignment
        centerAdminRepository.delete(assignment);

        logger.info("[SUCCESS] {}", String.format(MSG_UNASSIGN_SUCCESS, userId, centerId));
    }
}
```

