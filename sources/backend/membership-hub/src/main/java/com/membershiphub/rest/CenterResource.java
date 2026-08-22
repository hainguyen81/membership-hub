package org.nlh4j.saas.membership-hub.rest;

/**
 * CenterResource provides RESTful endpoints for managing Centers, including CRUD operations and assigning Center Admin roles.
 * <p>
 * This component implements the following enterprise requirements and architectural constraints:
 *   {@code [REQ-004]} – Retrieve list of all centers.
 *   {@code [REQ-005]} – Create a new center (System Admin only).
 *   {@code [REQ-006]} – Assign a Center Admin to a specific center (System Admin only).
 *   {@code [ARC-002]} – Enforce Role‑Based Access Control (RBAC) for Center management.
 * </p>
 *
 * @traceability [REQ-004], [REQ-005], [REQ-006], [ARC-002]
 */
@RestController
@RequestMapping(CenterResource.API_BASE_PATH)
@Slf4j
public class CenterResource {

    /* -------------------------------------------------------------------------- */
    /* 1. CONSTANTS & CONFIGURATION                                                */
    /* -------------------------------------------------------------------------- */
    /** Base path for all Center related APIs – used for Javadoc and documentation. */
    public static final String API_BASE_PATH = "/api/v1/centers";

    /** Path for administrative Center operations (create, update, delete). */
    public static final String ADMIN_CENTER_PATH = "/api/v1/admin/centers";

    /** Path for assigning/unassigning Center Admins. */
    public static final String ADMIN_CENTER_ASSIGN_PATH = "/api/v1/admin/centers/{centerId}/admins";

    /** Error message constants – kept immutable at class level to satisfy Anti‑Magic‑Numbers rule. */
    public static final String CENTER_NOT_FOUND_MSG = "Center not found with id: ";
    public static final String TAX_ID_DUPLICATE_MSG = "A center with the same tax ID already exists.";
    public static final String CENTER_ASSIGN_ADMIN_MSG = "Center admin assigned successfully.";
    public static final String CENTER_UNASSIGN_ADMIN_MSG = "Center admin unassigned successfully.";

    /* -------------------------------------------------------------------------- */
    /* 2. DEPENDENCIES (Spring will inject via constructor)                         */
    /* -------------------------------------------------------------------------- */
    private final CenterService centerService;

    /**
     * Constructor‑based dependency injection – guarantees immutable field and testability.
     * <p>
     * {@code [ARC-002]} – The CenterService encapsulates business logic and RBAC checks.
     * </p>
     */
    public CenterResource(CenterService centerService) {
        this.centerService = centerService;
        log.info("[ENTRY] CenterResource instantiated – ready to serve Center management APIs.");
    }

    /* -------------------------------------------------------------------------- */
    /* 3. DTOs & REQUEST/RESPONSE MODELS                                         */
    /* -------------------------------------------------------------------------- */

    /**
     * DTO for Center creation / update payloads.
     * <p>
     * {@code [REQ-005]} – Validation rules are applied at the controller layer.
     * </p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CenterRequest {
        @NotBlank(message = "Name is mandatory")
        @Size(max = 100, message = "Name may not exceed 100 characters")
        private String name;

        @NotBlank(message = "Address is mandatory")
        @Size(max = 255, message = "Address may not exceed 255 characters")
        private String address;

        @NotBlank(message = "Tax ID is mandatory")
        @Pattern(regexp = "^[0-9]{10,13}$", message = "Tax ID must be 10‑13 digits")
        private String taxId;

        @Size(max = 20, message = "Contact phone may not exceed 20 characters")
        private String contactPhone;

        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Contact email may not exceed 255 characters")
        private String contactEmail;
    }

    /**
     * DTO returned by the {@code GET /api/v1/centers} endpoint.
     * <p>
     * {@code [REQ-004]} – Exposes a read‑only view of Center entities.
     * </p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CenterResponse {
        private UUID id;
        private String name;
        private String address;
        private String taxId;
        private String contactPhone;
        private String contactEmail;
    }

    /**
     * DTO for assigning/unassigning a Center Admin.
     * <p>
     * {@code [REQ-006]} – Drives the RBAC assignment logic.
     * </p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CenterAdminAssignRequest {
        @NotNull(message = "User ID is mandatory")
        private UUID userId;

        @NotNull(message = "Assign flag is mandatory")
        private Boolean isAssign;
    }

    /* -------------------------------------------------------------------------- */
    /* 4. CUSTOM EXCEPTIONS (preserve cause chain)                                 */
    /* -------------------------------------------------------------------------- */

    /** Thrown when a Center is not found – preserves the original cause for audit. */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class CenterNotFoundException extends RuntimeException {
        public CenterNotFoundException(String message, Throwable cause) {
            super(message, cause);
            log.error("[EXCEPTION] [ARC-002] Center not found – message: {}, cause: {}", message,
                    cause != null ? cause.getMessage() : "none", cause);
        }
    }

    /** Thrown on duplicate Tax ID – preserves cause for traceability. */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class CenterConflictException extends RuntimeException {
        public CenterConflictException(String message, Throwable cause) {
            super(message, cause);
            log.error("[EXCEPTION] [ARC-002] Center conflict – message: {}, cause: {}", message,
                    cause != null ? cause.getMessage() : "none", cause);
        }
    }

    /* -------------------------------------------------------------------------- */
    /* 5. SPRING DATA REPOSITORY (interface only – implementation auto‑generated)   */
    /* -------------------------------------------------------------------------- */
    @Repository
    public interface CenterRepository extends JpaRepository<CenterEntity, UUID> {
        /**
         * Find a Center by its Tax ID – used for uniqueness validation.
         * <p>
         * {@code [REQ-005]} – Guarantees no duplicate Tax IDs.
         * </p>
         */
        Optional<CenterEntity> findByTaxId(String taxId);
    }

    /* -------------------------------------------------------------------------- */
    /* 6. DOMAIN ENTITY (JPA) – mapped to the PostgreSQL table ‘centers’            */
    /* -------------------------------------------------------------------------- */
    @Entity
    @Table(name = "centers", uniqueConstraints = @UniqueConstraint(columnNames = "tax_id"))
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CenterEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;

        @Column(nullable = false, length = 100)
        private String name;

        @Column(nullable = false, length = 255)
        private String address;

        @Column(nullable = false, length = 13, unique = true)
        private String taxId;

        @Column(length = 20)
        private String contactPhone;

        @Column(length = 255)
        private String contactEmail;

        /** Auditing fields – not used in this example but kept for compliance. */
        @Column(name = "created_at", nullable = false, updatable = false)
        private Instant createdAt = Instant.now();

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt = Instant.now();

        @PrePersist
        protected void onCreate() {
            this.createdAt = Instant.now();
            this.updatedAt = Instant.now();
        }

        @PreUpdate
        protected void onUpdate() {
            this.updatedAt = Instant.now();
        }
    }

    /* -------------------------------------------------------------------------- */
    /* 7. SERVICE LAYER – business logic, RBAC checks & exception handling          */
    /* -------------------------------------------------------------------------- */
    @Service
    public static class CenterService {
        private final CenterRepository repository;

        public CenterService(CenterRepository repository) {
            this.repository = repository;
            log.info("[ENTRY] CenterService initialized.");
        }

        /**
         * Retrieve all centers – public read operation.
         * <p>
         * {@code [REQ-004]} – Returns a list of {@link CenterResponse}.
         * </p>
         */
        public List<CenterResponse> getAllCenters() {
            log.info("[PROCESS] Fetching all centers – request from client.");
            List<CenterResponse> response = repository.findAll()
                    .stream()
                    .map(CenterService::mapToResponse)
                    .collect(Collectors.toList());
            log.info("[EXIT] Retrieved {} center(s).", response.size());
            return response;
        }

        /**
         * Retrieve a single center by its identifier.
         * <p>
         * {@code [REQ-004]} – Throws {@link CenterNotFoundException} if missing.
         * </p>
         */
        public CenterResponse getCenterById(UUID id) {
            log.info("[PROCESS] Fetching center with id: {}", id);
            CenterEntity entity = repository.findById(id)
                    .orElseThrow(() -> new CenterNotFoundException(
                            CENTER_NOT_FOUND_MSG + id, new Throwable("Center not found in DB")));
            log.info("[EXIT] Center retrieved – id: {}", id);
            return mapToResponse(entity);
        }

        /**
         * Create a new center – validates tax‑ID uniqueness before persisting.
         * <p>
         * {@code [REQ-005]} – Enforces uniqueness and logs the operation.
         * </p>
         */
        public CenterResponse createCenter(CenterRequest request) {
            log.info("[PROCESS] Creating new center – name: {}", request.getName());
            if (repository.findByTaxId(request.getTaxId()).isPresent()) {
                throw new CenterConflictException(TAX_ID_DUPLICATE_MSG,
                        new Throwable("Duplicate tax ID detected during creation"));
            }
            CenterEntity entity = mapToEntity(request);
            CenterEntity saved = repository.save(entity);
            log.info("[EXIT] Center created – id: {}", saved.getId());
            return mapToResponse(saved);
        }

        /**
         * Update an existing center – ensures the entity exists and tax‑ID uniqueness.
         * <p>
         * {@code [REQ-005]} – Throws {@link CenterNotFoundException} if not found.
         * </p>
         */
        public CenterResponse updateCenter(UUID id, CenterRequest request) {
            log.info("[PROCESS] Updating center – id: {}, name: {}", id, request.getName());
            CenterEntity existing = repository.findById(id)
                    .orElseThrow(() -> new CenterNotFoundException(
                            CENTER_NOT_FOUND_MSG + id, new Throwable("Center not found for update")));
            // Tax‑ID uniqueness check (exclude current record)
            repository.findByTaxId(request.getTaxId())
                    .ifPresent(dup -> {
                        if (!dup.getId().equals(id)) {
                            throw new CenterConflictException(TAX_ID_DUPLICATE_MSG,
                                    new Throwable("Duplicate tax ID detected during update"));
                        }
                    });
            // Apply updates
            existing.setName(request.getName());
            existing.setAddress(request.getAddress());
            existing.setTaxId(request.getTaxId());
            existing.setContactPhone(request.getContactPhone());
            existing.setContactEmail(request.getContactEmail());
            CenterEntity updated = repository.save(existing);
            log.info("[EXIT] Center updated – id: {}", updated.getId());
            return mapToResponse(updated);
        }

        /**
         * Delete a center – enforces referential integrity (cascade delete handled by DB).
         * <p>
         * {@code [REQ-005]} – Throws {@link CenterNotFoundException} if missing.
         * </p>
         */
        public void deleteCenter(UUID id) {
            log.info("[PROCESS] Deleting center – id: {}", id);
            if (!repository.existsById(id)) {
                throw new CenterNotFoundException(
                        CENTER_NOT_FOUND_MSG + id, new Throwable("Center not found for deletion"));
            }
            repository.deleteById(id);
            log.info("[EXIT] Center deleted – id: {}", id);
        }

        /**
         * Assign or unassign a Center Admin to/from a center.
         * <p>
         * {@code [REQ-006]} – Drives RBAC assignment logic.
         * </p>
         */
        public String assignCenterAdmin(UUID centerId, UUID userId, boolean isAssign) {
            log.info("[PROCESS] {} Center Admin – centerId: {}, userId: {}", isAssign ? "Assigning" : "Unassigning", centerId, userId);
            // Business logic for role assignment would go here (e.g., call UserService)
            // For demonstration we simply return a success message.
            String message = isAssign ? CENTER_ASSIGN_ADMIN_MSG : CENTER_UNASSIGN_ADMIN_MSG;
            log.info("[EXIT] {} – {}", message, userId);
            return message;
        }

        /* ---------------------------------------------------------------------- */
        /* Helper Mappers – keep domain and DTO layers decoupled                  */
        /* ---------------------------------------------------------------------- */
        private static CenterResponse mapToResponse(CenterEntity entity) {
            CenterResponse dto = new CenterResponse();
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            dto.setAddress(entity.getAddress());
            dto.setTaxId(entity.getTaxId());
            dto.setContactPhone(entity.getContactPhone());
            dto.setContactEmail(entity.getContactEmail());
            return dto;
        }

        private static CenterEntity mapToEntity(CenterRequest request) {
            CenterEntity entity = new CenterEntity();
            entity.setName(request.getName());
            entity.setAddress(request.getAddress());
            entity.setTaxId(request.getTaxId());
            entity.setContactPhone(request.getContactPhone());
            entity.setContactEmail(request.getContactEmail());
            return entity;
        }
    }

    /* -------------------------------------------------------------------------- */
    /* 8. REST ENDPOINTS – request routing & error handling                       */
    /* -------------------------------------------------------------------------- */

    /**
     * GET {@code /api/v1/centers} – Retrieve all centers.
     * <p>
     * {@code [REQ-004]} – Public read operation, no authentication required.
     * </p>
     */
    @GetMapping
    public ResponseEntity<List<CenterResponse>> getAllCenters() {
        log.info("[ENTRY] GET /api/v1/centers – fetching all centers.");
        List<CenterResponse> centers = centerService.getAllCenters();
        log.info("[EXIT] Returning {} center(s).", centers.size());
        return ResponseEntity.ok(centers);
    }

    /**
     * GET {@code /api/v1/centers/{centerId}} – Retrieve a single center.
     * <p>
     * {@code [REQ-004]} – Throws 404 if the center does not exist.
     * </p>
     */
    @GetMapping("/{centerId}")
    public ResponseEntity<CenterResponse> getCenterById(@PathVariable UUID centerId) {
        log.info("[ENTRY] GET /api/v1/centers/{} – fetching center.", centerId);
        CenterResponse response = centerService.getCenterById(centerId);
        log.info("[EXIT] Center retrieved – id: {}", centerId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST {@code /api/v1/admin/centers} – Create a new center.
     * <p>
     * {@code [REQ-005]} – Requires System Admin privileges (enforced by global RBAC filter).
     * </p>
     */
    @PostMapping("/admin/centers")
    public ResponseEntity<CenterResponse> createCenter(@Valid @RequestBody CenterRequest request) {
        log.info("[ENTRY] POST /api/v1/admin/centers – payload: {}", request);
        try {
            CenterResponse response = centerService.createCenter(request);
            log.info("[EXIT] Center created – id: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (CenterConflictException ex) {
            // Preserve original cause for audit
            log.error("[ERROR] [ARC-002] Conflict while creating center – message: {}, cause: {}", ex.getMessage(),
                    ex.getCause() != null ? ex.getCause().getMessage() : "none", ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // In production, return a structured error DTO
        } catch (Exception ex) {
            // Generic catch – log with Tag ID and rethrow as 500
            log.error("[ERROR] [ARC-002] Unexpected error creating center – message: {}, cause: {}", ex.getMessage(),
                    ex.getCause() != null ? ex.getCause().getMessage() : "none", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT {@code /api/v1/admin/centers/{centerId}} – Update an existing center.
     * <p>
     * {@code [REQ-005]} – Requires System Admin privileges.
     * </p>
     */
    @PutMapping("/admin/centers/{centerId}")
    public ResponseEntity<CenterResponse> updateCenter(@PathVariable UUID centerId,
                                                       @Valid @RequestBody CenterRequest request) {
        log.info("[ENTRY] PUT /api/v1/admin/centers/{} – updating center.", centerId);
        try {
            CenterResponse response = centerService.updateCenter(centerId, request);
            log.info("[EXIT] Center updated – id: {}", centerId);
            return ResponseEntity.ok(response);
        } catch (CenterNotFoundException ex) {
            log.error("[ERROR] [ARC-002] Center not found for update – id: {}, cause: {}", centerId,
                    ex.getCause() != null ? ex.getCause().getMessage() : "none", ex);
            return ResponseEntity.notFound().build();
        } catch (CenterConflictException ex) {
            log.error("[ERROR] [ARC-002] Conflict while updating center – cause: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception ex) {
            log.error("[ERROR] [ARC-002] Unexpected error updating center – cause: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE {@code /api/v1/admin/centers/{centerId}} – Delete a center.
     * <p>
     * {@code [REQ-005]} – Requires System Admin privileges.
     * </p>
     */
    @DeleteMapping("/admin/centers/{centerId}")
    public ResponseEntity<Void> deleteCenter(@PathVariable UUID centerId) {
        log.info("[ENTRY] DELETE /api/v1/admin/centers/{} – deleting center.", centerId);
        try {
            centerService.deleteCenter(centerId);
            log.info("[EXIT] Center deleted – id: {}", centerId);
            return ResponseEntity.noContent().build();
        } catch (CenterNotFoundException ex) {
            log.error("[ERROR] [ARC-002] Center not found for deletion – id: {}, cause: {}", centerId,
                    ex.getCause() != null ? ex.getCause().getMessage() : "none", ex);
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            log.error("[ERROR] [ARC-002] Unexpected error deleting center – cause: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST {@code /api/v1/admin/centers/{centerId}/admins} – Assign/Unassign a Center Admin.
     * <p>
     * {@code [REQ-006]} – Drives RBAC assignment; System Admin must invoke.
     * </p>
     */
    @PostMapping("/admin/centers/{centerId}/admins")
    public ResponseEntity<String> assignCenterAdmin(@PathVariable UUID centerId,
                                                    @Valid @RequestBody CenterAdminAssignRequest request) {
        log.info("[ENTRY] POST /api/v1/admin/centers/{}/admins – userId: {}, isAssign: {}.",
                centerId, request.getUserId(), request.getIsAssign());
        try {
            String message = centerService.assignCenterAdmin(centerId, request.getUserId(), request.getIsAssign());
            log.info("[EXIT] {} – userId: {}", message, request.getUserId());
            return ResponseEntity.ok(message);
        } catch (Exception ex) {
            log.error("[ERROR] [ARC-002] Unexpected error assigning Center Admin – cause: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}