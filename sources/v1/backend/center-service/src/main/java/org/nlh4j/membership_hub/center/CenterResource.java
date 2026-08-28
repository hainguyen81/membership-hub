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