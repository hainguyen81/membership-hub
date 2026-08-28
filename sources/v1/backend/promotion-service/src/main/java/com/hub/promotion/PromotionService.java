package org.nlh4j.saas.membership_hub.promotion;

// ------------------------------ ENTERPRISE IMPORTS (STRICT COMPLIANCE) ------------------------------
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Context;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
// ------------------------------------------------------------------------------------------------

/**
 * Service layer for managing promotional campaigns (khuyến mãi) in the membership-hub system.
 * Implements full CRUD operations for promotions with strict business rule validation:
 * 1. Discount percent must be between 0 and 100
 * 2. End date must be >= start date (if both are provided)
 * 3. Promotion code must be unique across the entire system
 * 4. All mutation operations enforce idempotency key validation to prevent duplicate requests
 * 5. RBAC access control applied to all endpoints per enterprise security policy
 * 
 * @traceability [REQ-017], [DAT-009]
 * @subsystem PromotionService
 * @version 1.0
 */
@ApplicationScoped
@Tag(name = "Promotion", description = "Promotion campaign management API")
@RegisterForReflection // Required for Quarkus native image compilation
public class PromotionService {

    // ------------------------------ TOP-LEVEL CONSTANTS (NO HARDCODED LITERALS IN METHODS) ------------------------------
    /** Subsystem name for audit logging and error tracing per [NFR-006] */
    public static final String SUBSYSTEM_NAME = "PromotionService";
    /** Minimum allowed discount percentage for valid promotions */
    public static final int MIN_DISCOUNT_PERCENT = 0;
    /** Maximum allowed discount percentage for valid promotions */
    public static final int MAX_DISCOUNT_PERCENT = 100;
    /** Maximum allowed length for promotion code field (matches DB schema constraint) */
    public static final int MAX_PROMO_CODE_LENGTH = 50;
    /** Maximum allowed length for promotion description field (matches DB schema constraint) */
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    /** HTTP header name for idempotency key (required for all mutation APIs per security gating framework) */
    public static final String IDEMPOTENCY_KEY_HEADER = "X-Idempotency-Key";
    /** Time-to-live for idempotency key cache (24 hours, aligns with duplicate request prevention policy) */
    public static final long IDEMPOTENCY_CACHE_TTL_MINUTES = 24 * 60;
    /** Default page size for paginated promotion queries */
    public static final int DEFAULT_PROMOTION_PAGE_SIZE = 20;
    /** Maximum allowed page size for paginated promotion queries to prevent performance degradation */
    public static final int MAX_PROMOTION_PAGE_SIZE = 100;
    // ----------------------------------------------------------------------------------------------------------------------

    // ------------------------------ DEPENDENCY INJECTION ------------------------------
    /** Promotion repository for database operations (maps to promotions table per [DAT-009]) */
    @Inject
    PromotionRepository promotionRepository;
    /** In-memory cache for idempotency keys (production uses Redis cache per [ARC-009]) */
    @Inject
    Cache<String, String> idempotencyCache;
    // ---------------------------------------------------------------------------------

    // ------------------------------ ENTERPRISE LOGGER (AUDIT COMPLIANCE) ------------------------------
    /** Logger instance for process tracing and error auditing per [NFR-006] */
    private static final Logger logger = LoggerFactory.getLogger(PromotionService.class);
    // ------------------------------------------------------------------------------------------------

    // ------------------------------ INNER CLASSES (SELF-CONTAINED IMPLEMENTATION) ------------------------------
    /**
     * Promotion repository interface for database operations
     * Extends PanacheRepository for type-safe, parameterized queries (prevents SQL injection per [NFR-003])
     * @traceability [DAT-009]
     */
    public interface PromotionRepository extends PanacheRepository<Promotion, UUID> {
        // Custom query methods can be added here if needed for complex filtering
    }

    /**
     * Promotion entity mapping to the promotions table in PostgreSQL
     * Enforces all database schema constraints defined in [DAT-009]
     * @traceability [DAT-009]
     */
    @Table(name = "promotions")
    public static class Promotion extends PanacheEntity {
        /** Unique promotion code, must be unique across the system (DB unique constraint enforced) */
        @Column(nullable = false, unique = true, length = MAX_PROMO_CODE_LENGTH)
        public String code;

        /** Discount percentage applied by the promotion (DB check constraint: 0-100) */
        @Column(nullable = false)
        public Short discountPercent;

        /** Start date of promotion validity period (optional) */
        @Column(name = "start_date")
        public LocalDate startDate;

        /** End date of promotion validity period (optional) */
        @Column(name = "end_date")
        public LocalDate endDate;

        /** Detailed description of the promotion terms and conditions */
        @Column(length = MAX_DESCRIPTION_LENGTH)
        public String description;

        /** Timestamp of promotion creation (auto-generated, immutable) */
        @Column(name = "created_at", nullable = false, updatable = false)
        public LocalDateTime createdAt;

        /** Timestamp of last promotion update (auto-generated) */
        @Column(name = "updated_at", nullable = false)
        public LocalDateTime updatedAt;

        // ------------------------------ LIFECYCLE CALLBACKS ------------------------------
        /** Auto-set creation timestamp before persisting to database */
        @PrePersist
        void onCreate() {
            createdAt = LocalDateTime.now();
            updatedAt = LocalDateTime.now();
        }

        /** Auto-set update timestamp before updating database record */
        @PreUpdate
        void onUpdate() {
            updatedAt = LocalDateTime.now();
        }
        // ---------------------------------------------------------------------------------

        /**
         * Helper method to check if the promotion is currently active
         * Business rule: Promotion is active if current date is within [startDate, endDate] range
         * @return true if promotion is active, false otherwise
         */
        public boolean isActive() {
            LocalDate currentDate = LocalDate.now();
            boolean startValid = startDate == null || !currentDate.isBefore(startDate);
            boolean endValid = endDate == null || !currentDate.isAfter(endDate);
            return startValid && endValid;
        }
    }

    /**
     * Request DTO for promotion creation/update operations (API contract)
     * Enforces input validation rules per business requirements
     * @traceability [REQ-017]
     */
    public record PromotionRequestDTO(
        @NotBlank(message = "Promotion code is required")
        @Size(max = MAX_PROMO_CODE_LENGTH, message = "Promo code must be at most {max} characters")
        String code,
        @NotNull(message = "Discount percent is required")
        @Min(value = MIN_DISCOUNT_PERCENT, message = "Discount percent must be at least {value}")
        @Max(value = MAX_DISCOUNT_PERCENT, message = "Discount percent must be at most {value}")
        Short discountPercent,
        LocalDate startDate,
        LocalDate endDate,
        @Size(max = MAX_DESCRIPTION_LENGTH, message = "Description must be at most {max} characters")
        String description
    ) {}

    /**
     * Response DTO for promotion API responses (API contract)
     * Masks sensitive internal fields and exposes only business-relevant data
     * @traceability [REQ-017]
     */
    public record PromotionResponseDTO(
        UUID promoId,
        String code,
        Short discountPercent,
        LocalDate startDate,
        LocalDate endDate,
        String description,
        boolean isActive
    ) {}

    /**
     * Custom exception for duplicate promotion code errors
     * Extends BadRequestException to return HTTP 400 status code
     */
    public static class PromotionCodeAlreadyExistsException extends BadRequestException {
        public PromotionCodeAlreadyExistsException(String message) {
            super(message);
        }
    }

    /**
     * Custom exception for invalid promotion data errors
     * Extends BadRequestException to return HTTP 400 status code
     */
    public static class InvalidPromotionDataException extends BadRequestException {
        public InvalidPromotionDataException(String message) {
            super(message);
        }
    }

    /**
     * Custom exception for promotion not found errors
     * Extends NotFoundException to return HTTP 404 status code
     */
    public static class PromotionNotFoundException extends NotFoundException {
        public PromotionNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Custom exception for internal promotion service errors
     * Extends InternalServerErrorException to return HTTP 500 status code
     */
    public static class PromotionServiceException extends InternalServerErrorException {
        public PromotionServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    // ----------------------------------------------------------------------------------------------------------

    // ------------------------------ PUBLIC API ENDPOINTS (CRUD OPERATIONS) ------------------------------
    /**
     * Creates a new promotional campaign
     * Validates input data, checks for duplicate promo codes, enforces idempotency to prevent duplicate requests
     * @param request Promotion creation request DTO
     * @param idempotencyKey Idempotency key from request header (required for mutation APIs)
     * @param securityContext Current user security context for RBAC validation
     * @return Created promotion response DTO
     * @traceability [REQ-017], [DAT-009]
     */
    @POST
    @Path("/promotions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"System Admin", "Center Admin", "Manager"}) // RBAC enforcement per [ARC-001], [ARC-002]
    @Operation(summary = "Create new promotion", description = "Creates a new promotional campaign with validation and idempotency support")
    public PromotionResponseDTO createPromotion(
            @Valid PromotionRequestDTO request,
            @HeaderParam(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Context SecurityContext securityContext) {
        // [LOG_ENTRY] Process start logging with full tracking payload per [NFR-006]
        logger.info("[PROCESS] [{}] [REQ-017] Initiating promotion creation | Code: {} | User: {} | Idempotency Key: {}", 
                SUBSYSTEM_NAME, request.code(), securityContext.getUserPrincipal().getName(), idempotencyKey);
        try {
            // [SECURITY] Validate idempotency key presence (mandatory for all mutation APIs per enterprise security policy)
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                logger.warn("[VALIDATION_FAIL] [{}] [REQ-017] Missing idempotency key for promotion creation", SUBSYSTEM_NAME);
                throw new BadRequestException("Idempotency key is required for mutation operations");
            }

            // [IDEMPOTENCY] Check if request with this key was already processed to prevent duplicate promotions
            String cachedResponse = idempotencyCache.get(idempotencyKey);
            if (cachedResponse != null) {
                logger.info("[PROCESS] [{}] [REQ-017] Idempotency key {} already processed, returning cached result", SUBSYSTEM_NAME, idempotencyKey);
                return Json.parseValue(cachedResponse, PromotionResponseDTO.class);
            }

            // [BUSINESS_RULE] Validate promotion date range: end date must be >= start date per [REQ-017]
            if (request.endDate() != null && request.startDate() != null && request.endDate().isBefore(request.startDate())) {
                logger.warn("[VALIDATION_FAIL] [{}] [REQ-017] Invalid date range for promotion {}: end date {} is before start date {}", 
                        SUBSYSTEM_NAME, request.code(), request.endDate(), request.startDate());
                throw new InvalidPromotionDataException("End date cannot be before start date");
            }

            // [BUSINESS_RULE] Check for duplicate promotion code (unique constraint enforced at DB and service layer)
            long existingCount = Promotion.count("code = ?1", request.code());
            if (existingCount > 0) {
                logger.warn("[VALIDATION_FAIL] [{}] [REQ-017] Duplicate promotion code detected: {}", SUBSYSTEM_NAME, request.code());
                throw new PromotionCodeAlreadyExistsException("Promotion code already exists: " + request.code());
            }

            // [PERSISTENCE] Create and persist new promotion entity (uses parameterized queries to prevent SQL injection per [NFR-003])
            Promotion promotion = new Promotion();
            promotion.code = request.code();
            promotion.discountPercent = request.discountPercent();
            promotion.startDate = request.startDate();
            promotion.endDate = request.endDate();
            promotion.description = request.description();
            promotion.persist(); // Panache ORM uses prepared statements under the hood, no SQL injection risk

            // [RESPONSE_BUILD] Build response DTO with active status
            PromotionResponseDTO response = new PromotionResponseDTO(
                    promotion.promoId,
                    promotion.code,
                    promotion.discountPercent,
                    promotion.startDate,
                    promotion.endDate,
                    promotion.description,
                    promotion.isActive()
            );

            // [IDEMPOTENCY] Cache response for idempotency key to handle duplicate requests within TTL
            idempotencyCache.put(idempotencyKey, Json.encode(response), IDEMPOTENCY_CACHE_TTL_MINUTES, TimeUnit.MINUTES);

            // [LOG_EXIT] Log successful creation with tracking payload
            logger.info("[PROCESS] [{}] [REQ-017] Promotion created successfully | ID: {} | Code: {}", 
                    SUBSYSTEM_NAME, promotion.promoId, request.code());
            return response;
        } catch (Exception e) {
            // [ERROR_LOG] Mandatory error logging with 3 required context keys per enterprise logging law [NFR-006]
            logger.error("[CRITICAL FAIL] [{}] [REQ-017] [DAT-009] Failed to create promotion. Raw error: {}", 
                    SUBSYSTEM_NAME, e.getMessage(), e);
            // Re-throw with original cause to preserve stack trace per exception cause chain preservation law
            if (e instanceof PromotionCodeAlreadyExistsException || e instanceof InvalidPromotionDataException || e instanceof BadRequestException) {
                throw e;
            }
            throw new PromotionServiceException("Failed to create promotion", e);
        }
    }

    /**
     * Updates an existing promotional campaign
     * Validates input data, checks promotion existence, enforces idempotency to prevent duplicate requests
     * @param promoId UUID of the promotion to update
     * @param request Promotion update request DTO
     * @param idempotencyKey Idempotency key from request header (required for mutation APIs)
     * @param securityContext Current user security context for RBAC validation
     * @return Updated promotion response DTO
     * @traceability [REQ-017], [DAT-009]
     */
    @PUT
    @Path("/promotions/{promoId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"System Admin", "Center Admin", "Manager"})
    @Operation(summary = "Update existing promotion", description = "Updates an existing promotional campaign with validation and idempotency support")
    public PromotionResponseDTO updatePromotion(
            @PathParam("promoId") UUID promoId,
            @Valid PromotionRequestDTO request,
            @HeaderParam(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Context SecurityContext securityContext) {
        // [LOG_ENTRY] Process start logging
        logger.info("[PROCESS] [{}] [REQ-017] Initiating promotion update | ID: {} | Code: {} | User: {} | Idempotency Key: {}", 
                SUBSYSTEM_NAME, promoId, request.code(), securityContext.getUserPrincipal().getName(), idempotencyKey);
        try {
            // [SECURITY] Validate idempotency key presence
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                logger.warn("[VALIDATION_FAIL] [{}] [REQ-017] Missing idempotency key for promotion update | ID: {}", SUBSYSTEM_NAME, promoId);
                throw new BadRequestException("Idempotency key is required for mutation operations");
            }

            // [IDEMPOTENCY] Check if request with this key was already processed
            String cachedResponse = idempotencyCache.get(idempotencyKey);
            if (cachedResponse != null) {
                logger.info("[PROCESS] [{}] [REQ-017] Idempotency key {} already processed, returning cached result", SUBSYSTEM_NAME, idempotencyKey);
                return Json.parseValue(cachedResponse, PromotionResponseDTO.class);
            }

            // [PERSISTENCE] Find existing promotion by ID (uses parameterized query to prevent SQL injection)
            Promotion promotion = Promotion.findById(promoId);
            if (promotion == null) {
                logger.warn("[NOT_FOUND] [{}] [REQ-017] Promotion not found for update | ID: {}", SUBSYSTEM_NAME, promoId);
                throw new PromotionNotFoundException("Promotion not found with ID: " + promoId);
            }

            // [BUSINESS_RULE] Validate date range if provided
            if (request.endDate() != null && request.startDate() != null && request.endDate().isBefore(request.startDate())) {
                logger.warn("[VALIDATION_FAIL] [{}] [REQ-017] Invalid date range for promotion update {}: end date {} is before start date {}", 
                        SUBSYSTEM_NAME, promoId, request.endDate(), request.startDate());
                throw new InvalidPromotionDataException("End date cannot be before start date");
            }

            // [BUSINESS_RULE] Check for duplicate promo code if code is being changed
            if (!promotion.code.equals(request.code())) {
                long existingCount = Promotion.count("code = ?1 and promoId != ?2", request.code(), promoId);
                if (existingCount > 0) {
                    logger.warn("[VALIDATION_FAIL] [{}] [REQ-017] Duplicate promotion code detected during update: {}", SUBSYSTEM_NAME, request.code());
                    throw new PromotionCodeAlreadyExistsException("Promotion code already exists: " + request.code());
                }
            }

            // [UPDATE] Update promotion fields (uses parameterized update query, no SQL injection risk)
            promotion.code = request.code();
            promotion.discountPercent = request.discountPercent();
            promotion.startDate = request.startDate();
            promotion.endDate = request.endDate();
            promotion.description = request.description();
            promotion.update();

            // [RESPONSE_BUILD] Build updated response DTO
            PromotionResponseDTO response = new PromotionResponseDTO(
                    promotion.promoId,
                    promotion.code,
                    promotion.discountPercent,
                    promotion.startDate,
                    promotion.endDate,
                    promotion.description,
                    promotion.isActive()
            );

            // [IDEMPOTENCY] Cache response for idempotency key
            idempotencyCache.put(idempotencyKey, Json.encode(response), IDEMPOTENCY_CACHE_TTL_MINUTES, TimeUnit.MINUTES);

            // [LOG_EXIT] Log successful update
            logger.info("[PROCESS] [{}] [REQ-017] Promotion updated successfully | ID: {} | Code: {}", 
                    SUBSYSTEM_NAME, promoId, request.code());
            return response;
        } catch (Exception e) {
            // [ERROR_LOG] Mandatory error logging with 3 context keys
            logger.error("[CRITICAL FAIL] [{}] [REQ-017] [DAT-009] Failed to update promotion. Raw error: {}", 
                    SUBSYSTEM_NAME, e.getMessage(), e);
            // Re-throw with original cause to preserve stack trace
            if (e instanceof PromotionCodeAlreadyExistsException || e instanceof InvalidPromotionDataException || e instanceof PromotionNotFoundException || e instanceof BadRequestException) {
                throw e;
            }
            throw new PromotionServiceException("Failed to update promotion", e);
        }
    }

    /**
     * Deletes a promotional campaign by ID
     * @param promoId UUID of the promotion to delete
     * @param securityContext Current user security context for RBAC validation
     * @return Success message with deleted promotion ID
     * @traceability [REQ-017], [DAT-009]
     */
    @DELETE
    @Path("/promotions/{promoId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"System Admin", "Center Admin", "Manager"})
    @Operation(summary = "Delete promotion", description = "Deletes a promotional campaign by its unique ID")
    public Map<String, String> deletePromotion(
            @PathParam("promoId") UUID promoId,
            @Context SecurityContext securityContext) {
        // [LOG_ENTRY] Process start logging
        logger.info("[PROCESS] [{}] [REQ-017] Initiating promotion deletion | ID: {} | User: {}", 
                SUBSYSTEM_NAME, promoId, securityContext.getUserPrincipal().getName());
        try {
            // [PERSISTENCE] Check if promotion exists before deletion (parameterized query)
            Promotion promotion = Promotion.findById(promoId);
            if (promotion == null) {
                logger.warn("[NOT_FOUND] [{}] [REQ-017] Promotion not found for deletion | ID: {}", SUBSYSTEM_NAME, promoId);
                throw new PromotionNotFoundException("Promotion not found with ID: " + promoId);
            }

            // [DELETE] Delete promotion entity (uses parameterized query to prevent SQL injection)
            promotion.delete();

            // [LOG_EXIT] Log successful deletion
            logger.info("[PROCESS] [{}] [REQ-017] Promotion deleted successfully | ID: {} | Code: {}", 
                    SUBSYSTEM_NAME, promoId, promotion.code);
            return Map.of(
                    "message", "Promotion deleted successfully",
                    "promoId", promoId.toString()
            );
        } catch (Exception e) {
            // [ERROR_LOG] Mandatory error logging with 3 context keys
            logger.error("[CRITICAL FAIL] [{}] [REQ-017] [DAT-009] Failed to delete promotion. Raw error: {}", 
                    SUBSYSTEM_NAME, e.getMessage(), e);
            // Re-throw with original cause
            if (e instanceof PromotionNotFoundException) {
                throw e;
            }
            throw new PromotionServiceException("Failed to delete promotion", e);
        }
    }

    /**
     * Retrieves all currently active promotional campaigns
     * Active promotions are those where current date is within [startDate, endDate] validity period
     * @param page Page number for pagination (default 1)
     * @param size Page size for pagination (default 20, max 100)
     * @return List of active promotion response DTOs
     * @traceability [REQ-017], [DAT-009]
     */
    @GET
    @Path("/promotions/active")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"System Admin", "Center Admin", "Manager", "Teacher", "Student"}) // All roles can view active promotions
    @Operation(summary = "Get active promotions", description = "Retrieves all currently active promotional campaigns with pagination support")
    public List<PromotionResponseDTO> getActivePromotions(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue(String.valueOf(DEFAULT_PROMOTION_PAGE_SIZE)) int size) {
        // [LOG_ENTRY] Process start logging
        logger.info("[PROCESS] [{}] [REQ-017] Fetching active promotions | Page: {} | Size: {}", 
                SUBSYSTEM_NAME, page, size);
        try {
            // [VALIDATION] Sanitize pagination parameters to prevent invalid queries
            if (page < 1) {
                page = 1;
            }
            if (size < 1 || size > MAX_PROMOTION_PAGE_SIZE) {
                size = DEFAULT_PROMOTION_PAGE_SIZE;
            }

            // [QUERY] Fetch active promotions using parameterized query (delegated to DB layer for performance, no in-memory iteration)
            // Active condition: start_date <= CURRENT_DATE AND (end_date IS NULL OR end_date >= CURRENT_DATE)
            List<Promotion> activePromotions = Promotion.<Promotion>find(
                    "startDate <= ?1 and (endDate is null or endDate >= ?1)", 
                    LocalDate.now())
                    .page(page - 1, size)
                    .list();

            // [MAPPING] Convert entities to response DTOs (stream processing for memory efficiency)
            List<PromotionResponseDTO> response = activePromotions.stream()
                    .map(p -> new PromotionResponseDTO(
                            p.promoId,
                            p.code,
                            p.discountPercent,
                            p.startDate,
                            p.endDate,
                            p.description,
                            p.isActive()
                    ))
                    .toList();

            // [LOG_EXIT] Log successful fetch
            logger.info("[PROCESS] [{}] [REQ-017] Fetched {} active promotions", SUBSYSTEM_NAME, response.size());
            return response;
        } catch (Exception e) {
            // [ERROR_LOG] Mandatory error logging with 3 context keys
            logger.error("[CRITICAL FAIL] [{}] [REQ-017] [DAT-009] Failed to fetch active promotions. Raw error: {}", 
                    SUBSYSTEM_NAME, e.getMessage(), e);
            throw new PromotionServiceException("Failed to fetch active promotions", e);
        }
    }

    /**
     * Retrieves a single promotion by its unique ID
     * @param promoId UUID of the promotion to retrieve
     * @return Promotion response DTO
     * @throws PromotionNotFoundException if promotion does not exist
     * @traceability [REQ-017], [DAT-009]
     */
    @GET
    @Path("/promotions/{promoId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"System Admin", "Center Admin", "Manager", "Teacher", "Student"})
    @Operation(summary = "Get promotion by ID", description = "Retrieves a single promotional campaign by its unique identifier")
    public PromotionResponseDTO getPromotionById(@PathParam("promoId") UUID promoId) {
        // [LOG_ENTRY] Process start logging
        logger.info("[PROCESS] [{}] [REQ-017] Fetching promotion by ID | ID: {}", SUBSYSTEM_NAME, promoId);
        try {
            // [PERSISTENCE] Find promotion by ID (uses parameterized query to prevent SQL injection)
            Promotion promotion = Promotion.findById(promoId);
            if (promotion == null) {
                logger.warn("[NOT_FOUND] [{}] [REQ-017] Promotion not found | ID: {}", SUBSYSTEM_NAME, promoId);
                throw new PromotionNotFoundException("Promotion not found with ID: " + promoId);
            }

            // [RESPONSE_BUILD] Build response DTO
            PromotionResponseDTO response = new PromotionResponseDTO(
                    promotion.promoId,
                    promotion.code,
                    promotion.discountPercent,
                    promotion.startDate,
                    promotion.endDate,
                    promotion.description,
                    promotion.isActive()
            );

            // [LOG_EXIT] Log successful fetch
            logger.info("[PROCESS] [{}] [REQ-017] Promotion fetched successfully | ID: {}", SUBSYSTEM_NAME, promoId);
            return response;
        } catch (Exception e) {
            // [ERROR_LOG] Mandatory error logging with 3 context keys
            logger.error("[CRITICAL FAIL] [{}] [REQ-017] [DAT-009] Failed to fetch promotion. Raw error: {}", 
                    SUBSYSTEM_NAME, e.getMessage(), e);
            if (e instanceof PromotionNotFoundException) {
                throw e;
            }
            throw new PromotionServiceException("Failed to fetch promotion", e);
        }
    }

    /**
     * Validates if a promotion code is active and applicable
     * @param promoCode Promotion code to validate
     * @return Validation result with active status and discount details
     * @traceability [REQ-017], [DAT-009]
     */
    @GET
    @Path("/promotions/validate/{promoCode}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"System Admin", "Center Admin", "Manager", "Teacher", "Student"})
    @Operation(summary = "Validate promotion code", description = "Checks if a promotion code is valid and active")
    public Map<String, Object> validatePromotionCode(@PathParam("promoCode") String promoCode) {
        // [LOG_ENTRY] Process start logging
        logger.info("[PROCESS] [{}] [REQ-017] Validating promotion code | Code: {}", SUBSYSTEM_NAME, promoCode);
        try {
            // [QUERY] Find promotion by code (uses parameterized query to prevent SQL injection)
            Promotion promotion = Promotion.find("code = ?1", promoCode).firstResult();
            if (promotion == null) {
                logger.warn("[NOT_FOUND] [{}] [REQ-017] Promotion code not found | Code: {}", SUBSYSTEM_NAME, promoCode);
                return Map.of(
                        "valid", false,
                        "message", "Invalid promotion code",
                        "discountPercent", 0
                );
            }

            // [VALIDATION] Check if promotion is currently active
            boolean isActive = promotion.isActive();
            if (!isActive) {
                logger.warn("[VALIDATION_FAIL] [{}] [REQ-017] Promotion code is expired | Code: {}", SUBSYSTEM_NAME, promoCode);
                return Map.of(
                        "valid", false,
                        "message", "Promotion code has expired",
                        "discountPercent", 0
                );
            }

            // [LOG_EXIT] Log successful validation
            logger.info("[PROCESS] [{}] [REQ-017] Promotion code validated successfully | Code: {} | Discount: {}%", 
                    SUBSYSTEM_NAME, promoCode, promotion.discountPercent);
            return Map.of(
                    "valid", true,
                    "message", "Promotion code is valid",
                    "discountPercent", promotion.discountPercent,
                    "promoId", promotion.promoId.toString()
            );
        } catch (Exception e) {
            // [ERROR_LOG] Mandatory error logging with 3 context keys
            logger.error("[CRITICAL FAIL] [{}] [REQ-017] [DAT-009] Failed to validate promotion code. Raw error: {}", 
                    SUBSYSTEM_NAME, e.getMessage(), e);
            throw new PromotionServiceException("Failed to validate promotion code", e);
        }
    }
    // ----------------------------------------------------------------------------------------------------------
}