# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/PromotionService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/PromotionService.java`
*   Traceability Audit Tags For This Task: ['[REQ-017]', '[REQ-018]', '[DAT-009]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai toàn bộ logic nghiệp vụ cho module quản lý khuyến mãi và thông báo hệ thống. Đối với khuyến mãi, xây dựng các phương thức CRUD với kiểm tra xác thực đầu vào nghiêm ngặt: phần trăm giảm giá phải nằm trong khoảng 0-100, ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu, mã khuyến mãi phải duy nhất. Đối với thông báo, triển khai logic lọc thông báo đang hoạt động dựa trên ngày hiện tại và cơ chế tự động ẩn thông báo sau ngày kết thúc thông qua scheduled job chạy hàng ngày. Tất cả các endpoint phải được bảo vệ bằng bộ lọc RBAC, chỉ cho phép Center Admin và Manager truy cập. Sử dụng prepared statements của Hibernate ORM để ngăn chặn SQL injection. Áp dụng làm sạch dữ liệu đầu vào để chống XSS.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-017]', '[REQ-018]', '[DAT-009]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * 🚀  ENTERPRISE SYSTEM DATA LAYER INJECTION - PROMOTION SERVICE MODULE
 *
 * 📋  Traceability Tags: [REQ-017], [REQ-018], [DAT-009

# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/PromotionController.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/PromotionController.java`
*   Traceability Audit Tags For This Task: ['[REQ-017]', '[REQ-018]', '[DAT-009]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai toàn bộ logic nghiệp vụ cho module quản lý khuyến mãi và thông báo hệ thống. Đối với khuyến mãi, xây dựng các phương thức CRUD với kiểm tra xác thực đầu vào nghiêm ngặt: phần trăm giảm giá phải nằm trong khoảng 0-100, ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu, mã khuyến mãi phải duy nhất. Đối với thông báo, triển khai logic lọc thông báo đang hoạt động dựa trên ngày hiện tại và cơ chế tự động ẩn thông báo sau ngày kết thúc thông qua scheduled job chạy hàng ngày. Tất cả các endpoint phải được bảo vệ bằng bộ lọc RBAC, chỉ cho phép Center Admin và Manager truy cập. Sử dụng prepared statements của Hibernate ORM để ngăn chặn SQL injection. Áp dụng làm sạch dữ liệu đầu vào để chống XSS.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-017]', '[REQ-018]', '[DAT-009]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.promotion;

/**
 * Controller for managing promotions and announcements.
 * Traceability Tags: [REQ-017], [REQ-018], [DAT-009]
 */
@Traceable(tags = {"[REQ-017]", "[REQ-018]", "[DAT-009]"}) // giả định annotation tùy chỉnh cho traceability
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Slf4j
public class PromotionController {

    /* ==================== CONSTANTS (Top‑of‑Class) ==================== */
    // Validation & error codes – anti‑magic‑numbers enforcement
    public static final String ERR_PROMO_CODE_DUPLICATE = "PROMOTION_CODE_DUPLICATE";
    public static final String ERR_PROMO_DISCOUNT_OUT_OF_RANGE = "PROMOTION_DISCOUNT_OUT_OF_RANGE";
    public static final String ERR_PROMO_DATE_INVALID = "PROMOTION_DATE_INVALID";
    public static final String ERR_ANNOUNCE_NOT_FOUND = "ANNOUNCEMENT_NOT_FOUND";
    public static final String ERR_ANNOUNCE_DATE_INVALID = "ANNOUNCEMENT_DATE_INVALID";
    public static final String ERR_RBAC_FORBIDDEN = "RBAC_FORBIDDEN";

    /* ==================== REPOSITORIES ==================== */
    private final PromotionRepository promotionRepo;
    private final AnnouncementRepository announcementRepo;

    /* ==================== ENDPOINTS – PROMOTIONS ==================== */

    /**
     * Tạo mới một khuyến mãi.
     * Traceability Tags: [REQ-017]
     */
    @PostMapping
    public ResponseEntity<Promotion> createPromotion(@Valid @RequestBody PromotionDto dto) {
        log.info("[ENTRY] createPromotion – dto: {}", dto);
        try {
            // Input sanitization – basic example (escape HTML entities)
            String sanitizedCode = sanitizeHtml(dto.getCode());
            if (promotionRepo.existsByCode(sanitizedCode)) {
                log.warn("[VALIDATION] Duplicate promotion code – code: {}", sanitizedCode);
                throw new IllegalArgumentException(ERR_PROMO_CODE_DUPLICATE);
            }
            if (dto.getDiscountPercent() < 0 || dto.getDiscountPercent() > 100) {
                log.warn("[VALIDATION] Discount percent out of range – value: {}", dto.getDiscountPercent());
                throw new IllegalArgumentException(ERR_PROMO_DISCOUNT_OUT_OF_RANGE);
            }
            if (dto.getEndDate() != null && dto.getStartDate() != null &&
                dto.getEndDate().isBefore(dto.getStartDate())) {
                log.warn("[VALIDATION] End date before start date – start: {}, end: {}", dto.getStartDate(), dto.getEndDate());
                throw new IllegalArgumentException(ERR_PROMO_DATE_INVALID);
            }

            Promotion promo = Promotion.builder()
                .code(sanitizedCode)
                .discountPercent(dto.getDiscountPercent())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .description(sanitizeHtml(dto.getDescription()))
                .build();

            Promotion saved = promotionRepo.save(promo); // Prepared‑statement usage via JPA
            log.info("[EXIT] createPromotion – created id: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException iae) {
            log.error("[CRITICAL FAIL] [REQ-017] Promotion validation failed – raw error: {}", iae.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("[CRITICAL FAIL] [REQ-017] Promotion processing failed due to unexpected error – raw error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy danh sách tất cả khuyến mãi.
     * Traceability Tags: [REQ-017]
     */
    @GetMapping
    public ResponseEntity<List<Promotion>> getAllPromotions() {
        log.info("[ENTRY] getAllPromotions");
        try {
            List<Promotion> list = promotionRepo.findAll();
            log.info("[EXIT] getAllPromotions – returned {} records", list.size());
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("[CRITICAL FAIL] [REQ-017] Failed to retrieve promotions – raw error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy thông tin khuyến mãi theo ID.
     * Traceability Tags: [REQ-017]
     */
    @GetMapping("/{id}")
    public ResponseEntity<Promotion> getPromotionById(@PathVariable UUID id) {
        log.info("[ENTRY] getPromotionById – id: {}", id);
        try {
            return promotionRepo.findById(id)
                .map(p -> {
                    log.info("[EXIT] getPromotionById – found");
                    return ResponseEntity.ok(p);
                })
                .orElseGet(() -> {
                    log.warn("[WARN] Promotion not found – id: {}", id);
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            log.error("[CRITICAL FAIL] [REQ-017] Failed to retrieve promotion by id – raw error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Cập nhật khuyến mãi.
     * Traceability Tags: [REQ-017]
     */
    @PutMapping("/{id}")
    public ResponseEntity<Promotion> updatePromotion(@PathVariable UUID id, @Valid @RequestBody PromotionDto dto) {
        log.info("[ENTRY] updatePromotion – id: {}, dto: {}", id, dto);
        try {
            return promotionRepo.findById(id).map(existing -> {
                // Validate discount range
                if (dto.getDiscountPercent() < 0 || dto.getDiscountPercent() > 100) {
                    throw new IllegalArgumentException(ERR_PROMO_DISCOUNT_OUT_OF_RANGE);
                }
                // Validate date logic if provided
                if (dto.getStartDate() != null && dto.getEndDate() != null &&
                    dto.getEndDate().isBefore(dto.getStartDate())) {
                    throw new IllegalArgumentException(ERR_PROMO_DATE_INVALID);
                }
                // Ensure code uniqueness if changed
                String newCode = sanitizeHtml(dto.getCode());
                if (!newCode.equals(existing.getCode()) && promotionRepo.existsByCode(newCode)) {
                    throw new IllegalArgumentException(ERR_PROMO_CODE_DUPLICATE);
                }

                // Apply updates
                existing.setCode(newCode);
                existing.setDiscountPercent(dto.getDiscountPercent());
                existing.setStartDate(dto.getStartDate());
                existing.setEndDate(dto.getEndDate());
                existing.setDescription(sanitizeHtml(dto.getDescription()));
                Promotion updated = promotionRepo.save(existing);
                log.info("[EXIT] updatePromotion – updated id: {}", updated.getId());
                return ResponseEntity.ok(updated);
            }).orElseGet(() -> {
                log.warn("[WARN] Promotion not found for update – id: {}", id);
                return ResponseEntity.notFound().build();
            });
        } catch (IllegalArgumentException iae) {
            log.error("[CRITICAL FAIL] [REQ-017] Promotion update validation failed – raw error: {}", iae.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("[CRITICAL FAIL] [REQ-017] Promotion update processing failed – raw error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Xóa khuyến mãi.
     * Traceability Tags: [REQ-017]
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable UUID id) {
        log.info("[ENTRY] deletePromotion – id: {}", id);
        try {
            if (!promotionRepo.existsById(id)) {
                log.warn("[WARN] Promotion not found for deletion – id: {}", id);
                return ResponseEntity.notFound().build();
            }
            promotionRepo.deleteById(id);
            log.info("[EXIT] deletePromotion – deleted id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("[CRITICAL FAIL] [REQ-017] Promotion deletion failed – raw error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /* ==================== ENDPOINTS – ANNOUNCEMENTS ==================== */

    /**
     * Tạo mới một thông báo hệ thống.
     * Traceability Tags: [REQ-018]
     */
    @PostMapping("/announcements")
    public ResponseEntity<Announcement> createAnnouncement(@Valid @RequestBody AnnouncementDto dto) {
        log.info("[ENTRY] createAnnouncement – dto: {}", dto);
        try {
            if (dto.getStartDate() != null && dto.getEndDate() != null &&
                dto.getEndDate().isBefore(dto.getStartDate())) {
                throw new IllegalArgumentException(ERR_ANNOUNCE_DATE_INVALID);
            }

            Announcement ann = Announcement.builder()
                .title(sanitizeHtml(dto.getTitle()))
                .content(sanitizeHtml(dto.getContent()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .hidden(false)
                .build();

            Announcement saved = announcementRepo.save(ann);
            log.info("[EXIT] createAnnouncement – created id: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException iae) {
            log.error("[CRITICAL FAIL] [REQ-018] Announcement validation failed – raw error: {}", iae.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("[CRITICAL FAIL] [REQ-018] Announcement processing failed – raw error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy danh sách thông báo đang hoạt động (không bị ẩn và trong khoảng thời gian hiệu lực).
     * Traceability Tags: [REQ-018]
     */
    @GetMapping("/announcements/active")
    public ResponseEntity<List<Announcement>> getActiveAnnouncements() {
        log.info("[ENTRY] getActiveAnnouncements");
        try {
            LocalDate now = LocalDate.now();
            List<Announcement> active = announcementRepo.findByHiddenFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now);
            log.info("[EXIT] getActiveAnnouncements – returned {} records", active.size());
            return ResponseEntity.ok(active);
        } catch (Exception e) {
            log.error("[CRITICAL FAIL] [REQ-018] Failed to retrieve active announcements – raw error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy tất cả thông báo (dành cho quản trị viên).
     * Traceability Tags: [REQ-018]
     */
    @GetMapping("/announcements")
    public ResponseEntity<List<Announcement>> getAllAnnouncements() {
        log.info("[ENTRY] getAllAnnouncements");
        try {
            List<Announcement> list = announcementRepo.findAll();
            log.info("[EXIT] getAllAnnouncements – returned {} records", list.size());
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("[CRITICAL FAIL] [REQ-018] Failed to retrieve all announcements – raw error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy thông báo theo ID.
     * Traceability Tags: [REQ-018]
     */
    @GetMapping("/announcements/{id}")
    public ResponseEntity<Announcement> getAnnouncementById(@PathVariable UUID id) {
        log.info("[ENTRY] getAnnouncementById – id: {}", id);
        try {
            return announcementRepo.findById(id)
                .map(a -> {
                    log.info("[EXIT] getAnnouncementById – found");
                    return ResponseEntity.ok(a);
                })
                .orElseGet(() -> {
                    log.warn("[WARN] Announcement not found – id: {}", id);
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            log.error("[CRITICAL FAIL] [REQ-018] Failed to retrieve announcement by id – raw error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Cập nhật thông báo.
     * Traceability Tags: [REQ-018]
     */
    @PutMapping("/announcements/{id}")
    public ResponseEntity<Announcement> updateAnnouncement(@PathVariable UUID id, @Valid @RequestBody AnnouncementDto dto) {
        log.info("[ENTRY] updateAnnouncement – id: {}, dto: {}", id, dto);
        try {
            return announcementRepo.findById(id).map(existing -> {
                if (dto.getStartDate() != null && dto.getEndDate() != null &&
                    dto.getEndDate().isBefore(dto.getStartDate())) {
                    throw new IllegalArgumentException(ERR_ANNOUNCE_DATE_INVALID);
                }

                existing.setTitle(sanitizeHtml(dto.getTitle()));
                existing.setContent(sanitizeHtml(dto.getContent()));
                existing.setStartDate(dto.getStartDate());
                existing.setEndDate(dto.getEndDate());
                // hidden flag can be updated by admin if needed
                Announcement updated = announcementRepo.save(existing);
                log.info("[EXIT] updateAnnouncement – updated id: {}", updated.getId());
                return ResponseEntity.ok(updated);
            }).orElseGet(() -> {
                log.warn("[WARN] Announcement not found for update – id: {}", id);
                return ResponseEntity.notFound().build();
            });
        } catch (IllegalArgumentException iae) {
            log.error("[CRITICAL FAIL] [REQ-018] Announcement update validation failed – raw error: {}", iae.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("[CRITICAL FAIL] [REQ-018] Announcement update processing failed – raw error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Xóa thông báo.
     * Traceability Tags: [REQ-018]
     */
    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable UUID id) {
        log.info("[ENTRY] deleteAnnouncement – id: {}", id);
        try {
            if (!announcementRepo.existsById(id)) {
                log.warn("[WARN] Announcement not found for deletion – id: {}", id);
                return ResponseEntity.notFound().build();
            }
            announcementRepo.deleteById(id);
            log.info("[EXIT] deleteAnnouncement – deleted id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("[CRITICAL FAIL] [REQ-018] Announcement deletion failed – raw error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /* ==================== SCHEDULED JOB – AUTO‑HIDE EXPIRED ANNOUNCEMENTS ==================== */

    /**
     * Job chạy hàng ngày để tự động ẩn các thông báo đã hết hạn (endDate < today).
     * Traceability Tags: [REQ-018]
     */
    @Scheduled(cron = "0 0 2 * * ?") // hàng ngày lúc 02:00 sáng
    public void autoHideExpiredAnnouncements() {
        log.info("[SCHEDULED] autoHideExpiredAnnouncements – starting");
        try {
            LocalDate today = LocalDate.now();
            List<Announcement> expired = announcementRepo.findByHiddenFalseAndEndDateBefore(today);
            if (!expired.isEmpty()) {
                expired.forEach(a -> a.setHidden(true));
                announcementRepo.saveAll(expired);
                log.info("[SCHEDULED] autoHideExpiredAnnouncements – hidden {} announcements", expired.size());
            } else {
                log.info("[SCHEDULED] autoHideExpiredAnnouncements – no announcements to hide");
            }
        } catch (Exception e) {
            log.error("[CRITICAL FAIL] [REQ-018] Scheduled auto‑hide announcements failed – raw error: {}", e.getMessage(), e);
        }
    }

    /* ==================== UTILITY METHODS ==================== */

    /**
     * Làm sạch dữ liệu đầu vào đơn giản để chống XSS (sử dụng HtmlUtils).
     */
    private String sanitizeHtml(String input) {
        if (input == null) return null;
        return org.springframework.web.util.HtmlUtils.htmlEscape(input);
    }

    /* ==================== INNER ENTITY DEFINITIONS (for brevity) ==================== */

    @Entity
    @Table(name = "promotions")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Promotion {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;

        @Column(nullable = false, unique = true)
        private String code;

        @Column(nullable = false)
        private Integer discountPercent;

        private LocalDate startDate;
        private LocalDate endDate;

        @Column(columnDefinition = "TEXT")
        private String description;

        @CreationTimestamp
        private Timestamp createdAt;

        @UpdateTimestamp
        private Timestamp updatedAt;
    }

    @Entity
    @Table(name = "announcements")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Announcement {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;

        @Column(nullable = false)
        private String title;

        @Column(columnDefinition = "TEXT", nullable = false)
        private String content;

        private LocalDate startDate;
        private LocalDate endDate;

        @Column(nullable = false)
        private Boolean hidden = false;

        @CreationTimestamp
        private Timestamp createdAt;

        @UpdateTimestamp
        private Timestamp updatedAt;
    }

    /* ==================== REPOSITORY INTERFACES ==================== */

    interface PromotionRepository extends JpaRepository<Promotion, UUID> {
        boolean existsByCode(String code);
    }

    interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {
        List<Announcement> findByHiddenFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate start, LocalDate end);
        List<Announcement> findByHiddenFalseAndEndDateBefore(LocalDate date);
    }

    /* ==================== DTO CLASSES ==================== */

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class PromotionDto {
        private String code;
        private Integer discountPercent;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class AnnouncementDto {
        private String title;
        private String content;
        private LocalDate startDate;
        private LocalDate endDate;
    }
}
```

# Day 1: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/AnnouncementService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/AnnouncementService.java`
*   Traceability Audit Tags For This Task: ['[REQ-017]', '[REQ-018]', '[DAT-009]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai toàn bộ logic nghiệp vụ cho module quản lý khuyến mãi và thông báo hệ thống. Đối với khuyến mãi, xây dựng các phương thức CRUD với kiểm tra xác thực đầu vào nghiêm ngặt: phần trăm giảm giá phải nằm trong khoảng 0-100, ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu, mã khuyến mãi phải duy nhất. Đối với thông báo, triển khai logic lọc thông báo đang hoạt động dựa trên ngày hiện tại và cơ chế tự động ẩn thông báo sau ngày kết thúc thông qua scheduled job chạy hàng ngày. Tất cả các endpoint phải được bảo vệ bằng bộ lọc RBAC, chỉ cho phép Center Admin và Manager truy cập. Sử dụng prepared statements của Hibernate ORM để ngăn chặn SQL injection. Áp dụng làm sạch dữ liệu đầu vào để chống XSS.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-017]', '[REQ-018]', '[DAT-009]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.promotion;

// ====================== ENTERPRISE IMPORT LAYER ======================
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.nlh4j.saas.membership_hub.auth.RbacService;
import org.nlh4j.saas.membership_hub.entity.Announcement;
import org.nlh4j.saas.membership_hub.entity.Promotion;
import org.nlh4j.saas.membership_hub.entity.Role;
import org.nlh4j.saas.membership_hub.entity.User;
import org.nlh4j.saas.membership_hub.exception.PromotionCodeAlreadyExistsException;
import org.nlh4j.saas.membership_hub.exception.UnauthorizedAccessException;
import org.nlh4j.saas.membership_hub.exception.ValidationException;
import org.nlh4j.saas.membership_hub.notification.NotificationService;
import org.nlh4j.saas.membership_hub.repository.AnnouncementRepository;
import org.nlh4j.saas.membership_hub.repository.PromotionRepository;
import org.nlh4j.saas.membership_hub.security.SecurityContext;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import io.quarkus.scheduler.Scheduled;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for managing promotions and system announcements.
 * Implements business logic for CRUD operations, input validation, XSS sanitization,
 * RBAC access control, and automatic expiration of announcements.
 *
 * @traceability [REQ-017], [REQ-018], [DAT-009]
 * @author Enterprise Core Engineering Team
 * @version 1.0.0
 */
@ApplicationScoped
public class AnnouncementService {

    // ====================== ENTERPRISE CONSTANTS (NO HARDCODED LITERALS IN LOGIC) ======================
    // [REQ-017] Promotion validation constants
    public static final int MIN_DISCOUNT_PERCENT = 0;
    public static final int MAX_DISCOUNT_PERCENT = 100;
    public static final int MAX_PROMO_CODE_LENGTH = 50;
    public static final int MAX_PROMO_DESCRIPTION_LENGTH = 500;
    // [REQ-018] Announcement validation constants
    public static final int MAX_ANNOUNCEMENT_TITLE_LENGTH = 150;
    public static final int MAX_ANNOUNCEMENT_CONTENT_LENGTH = 2000;
    // [EXC-003] Notification retry constants
    public static final int MAX_NOTIFICATION_RETRY_COUNT = 3;
    public static final long NOTIFICATION_RETRY_DELAY_MS = 300000; // 5 minutes
    // Scheduled job constants
    public static final String SCHEDULED_JOB_CRON = "0 0 0 * * ?"; // Run daily at midnight UTC
    // ====================== END OF CONSTANTS ======================

    // Enterprise standard SLF4J logger for audit and process tracing [NFR-006]
    private static final Logger LOG = Logger.getLogger(AnnouncementService.class);

    // Injected dependencies via Quarkus CDI [ARC-010]
    @Inject
    PromotionRepository promotionRepository;
    @Inject
    AnnouncementRepository announcementRepository;
    @Inject
    RbacService rbacService;
    @Inject
    NotificationService notificationService;
    @Inject
    SecurityContext securityContext;

    /**
     * Validates that the current user has the required role to access promotion/announcement management endpoints.
     * Only Center Admin and Manager roles are allowed per [REQ-017], [REQ-018].
     *
     * @param currentUser The currently authenticated user
     * @throws UnauthorizedAccessException if the user does not have the required role
     */
    private void validateRbac(User currentUser) {
        LOG.debug("[PROMOTION_SERVICE] [REQ-017] [REQ-018] Validating RBAC for user: {}", currentUser.getUserId());
        boolean hasAccess = rbacService.hasRole(currentUser, Role.CENTER_ADMIN, Role.MANAGER);
        if (!hasAccess) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] [REQ-018] Unauthorized access attempt by user: {} with role: {}", currentUser.getUserId(), currentUser.getRole());
            throw new UnauthorizedAccessException("Access denied. Only Center Admin and Manager can manage promotions and announcements.");
        }
        LOG.debug("[PROMOTION_SERVICE] [REQ-017] [REQ-018] RBAC validation passed for user: {}", currentUser.getUserId());
    }

    /**
     * Sanitizes user input to prevent XSS attacks by removing malicious HTML/script tags [NFR-003].
     * Uses Jsoup with a basic whitelist to allow only safe HTML elements.
     *
     * @param input The raw user input string
     * @return The sanitized safe string
     */
    private String sanitizeInput(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        // Clean input with basic whitelist (allows only safe tags like b, i, p, etc.)
        return Jsoup.clean(input, Whitelist.basic());
    }

    /**
     * Validates promotion business rules per [REQ-017].
     *
     * @param promotion The promotion entity to validate
     * @throws ValidationException if any validation rule is violated
     */
    private void validatePromotion(Promotion promotion) {
        LOG.debug("[PROMOTION_SERVICE] [REQ-017] Validating promotion data for code: {}", promotion.getCode());
        // Validate discount percentage range [REQ-017]
        if (promotion.getDiscountPercent() < MIN_DISCOUNT_PERCENT || promotion.getDiscountPercent() > MAX_DISCOUNT_PERCENT) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] Invalid discount percentage: {} for promo code: {}", promotion.getDiscountPercent(), promotion.getCode());
            throw new ValidationException("Discount percentage must be between " + MIN_DISCOUNT_PERCENT + " and " + MAX_DISCOUNT_PERCENT);
        }
        // Validate date range: end date must be >= start date if end date is provided [REQ-017]
        if (promotion.getEndDate() != null && promotion.getEndDate().isBefore(promotion.getStartDate())) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] Invalid date range for promo code: {}: startDate={}, endDate={}", promotion.getCode(), promotion.getStartDate(), promotion.getEndDate());
            throw new ValidationException("End date must be greater than or equal to start date");
        }
        // Validate promo code length [REQ-017]
        if (promotion.getCode().length() > MAX_PROMO_CODE_LENGTH) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] Promo code exceeds max length: {} for code: {}", MAX_PROMO_CODE_LENGTH, promotion.getCode());
            throw new ValidationException("Promotion code must not exceed " + MAX_PROMO_CODE_LENGTH + " characters");
        }
        // Validate description length if provided [REQ-017]
        if (promotion.getDescription() != null && promotion.getDescription().length() > MAX_PROMO_DESCRIPTION_LENGTH) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] Promo description exceeds max length: {} for code: {}", MAX_PROMO_DESCRIPTION_LENGTH, promotion.getCode());
            throw new ValidationException("Promotion description must not exceed " + MAX_PROMO_DESCRIPTION_LENGTH + " characters");
        }
        LOG.debug("[PROMOTION_SERVICE] [REQ-017] Promotion validation passed for code: {}", promotion.getCode());
    }

    /**
     * Validates announcement business rules per [REQ-018].
     *
     * @param announcement The announcement entity to validate
     * @throws ValidationException if any validation rule is violated
     */
    private void validateAnnouncement(Announcement announcement) {
        LOG.debug("[PROMOTION_SERVICE] [REQ-018] Validating announcement data for title: {}", announcement.getTitle());
        // Validate title length and presence [REQ-018]
        if (announcement.getTitle() == null || announcement.getTitle().isBlank() || announcement.getTitle().length() > MAX_ANNOUNCEMENT_TITLE_LENGTH) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-018] Invalid announcement title length: {} for title: {}", announcement.getTitle() != null ? announcement.getTitle().length() : 0, announcement.getTitle());
            throw new ValidationException("Announcement title is required and must not exceed " + MAX_ANNOUNCEMENT_TITLE_LENGTH + " characters");
        }
        // Validate content length and presence [REQ-018]
        if (announcement.getContent() == null || announcement.getContent().isBlank() || announcement.getContent().length() > MAX_ANNOUNCEMENT_CONTENT_LENGTH) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-018] Invalid announcement content length: {} for title: {}", announcement.getContent() != null ? announcement.getContent().length() : 0, announcement.getTitle());
            throw new ValidationException("Announcement content is required and must not exceed " + MAX_ANNOUNCEMENT_CONTENT_LENGTH + " characters");
        }
        // Validate date range: end date must be >= start date if end date is provided [REQ-018]
        if (announcement.getEndDate() != null && announcement.getEndDate().isBefore(announcement.getStartDate())) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-018] Invalid date range for announcement: {}: startDate={}, endDate={}", announcement.getTitle(), announcement.getStartDate(), announcement.getEndDate());
            throw new ValidationException("Announcement end date must be greater than or equal to start date");
        }
        LOG.debug("[PROMOTION_SERVICE] [REQ-018] Announcement validation passed for title: {}", announcement.getTitle());
    }

    // ====================== PROMOTION MANAGEMENT METHODS [REQ-017] ======================
    /**
     * Creates a new promotion with validated data and sanitized input.
     * Only accessible by Center Admin and Manager roles per [REQ-017].
     *
     * @param promotion The promotion entity to create
     * @return The created promotion entity
     * @throws UnauthorizedAccessException if the current user does not have permission
     * @throws ValidationException if the promotion data is invalid
     * @throws PromotionCodeAlreadyExistsException if the promo code already exists
     */
    @Transactional
    public Promotion createPromotion(Promotion promotion) {
        String operation = "CREATE_PROMOTION";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-017] Starting operation: {} for user: {}", operation, currentUser.getUserId());
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Sanitize all user input to prevent XSS [NFR-003]
            promotion.setCode(sanitizeInput(promotion.getCode()));
            if (promotion.getDescription() != null) {
                promotion.setDescription(sanitizeInput(promotion.getDescription()));
            }
            // Step 3: Validate business rules [REQ-017]
            validatePromotion(promotion);
            // Step 4: Check for duplicate promo code (unique constraint enforced at DB layer via prepared statements [NFR-003])
            if (promotionRepository.existsByCode(promotion.getCode())) {
                LOG.warn("[PROMOTION_SERVICE] [REQ-017] Duplicate promo code detected: {}", promotion.getCode());
                throw new PromotionCodeAlreadyExistsException("Promotion code already exists: " + promotion.getCode());
            }
            // Step 5: Set default active status
            promotion.setActive(true);
            // Step 6: Persist promotion using Hibernate prepared statements (prevents SQL injection [NFR-003])
            Promotion createdPromotion = promotionRepository.persist(promotion);
            // Step 7: Send notification to relevant users about new promotion [REQ-016]
            notificationService.sendPromotionNotification(createdPromotion);
            LOG.info("[PROMOTION_SERVICE] [REQ-017] Operation {} completed successfully. Created promotion ID: {}", operation, createdPromotion.getPromoId());
            return createdPromotion;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-017] Operation {} failed for user: {}. Raw error: {}", operation, currentUser.getUserId(), e.getMessage(), e);
            // Re-throw custom exception with original cause to preserve stack trace [GLOBAL_GOVERNANCE_MATRIX 0.3]
            if (e instanceof PromotionCodeAlreadyExistsException || e instanceof ValidationException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to create promotion", e);
        }
    }

    /**
     * Updates an existing promotion with validated data and sanitized input.
     * Only accessible by Center Admin and Manager roles per [REQ-017].
     *
     * @param promoId The ID of the promotion to update
     * @param updatedPromotion The updated promotion data
     * @return The updated promotion entity
     * @throws EntityNotFoundException if the promotion does not exist
     * @throws UnauthorizedAccessException if the current user does not have permission
     * @throws ValidationException if the updated data is invalid
     * @throws PromotionCodeAlreadyExistsException if the new promo code conflicts with an existing one
     */
    @Transactional
    public Promotion updatePromotion(UUID promoId, Promotion updatedPromotion) {
        String operation = "UPDATE_PROMOTION";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-017] Starting operation: {} for user: {} for promo ID: {}", operation, currentUser.getUserId(), promoId);
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Fetch existing promotion from database (using prepared statement query [NFR-003])
            Promotion existingPromotion = promotionRepository.findById(promoId)
                    .orElseThrow(() -> {
                        LOG.warn("[PROMOTION_SERVICE] [REQ-017] Promotion not found for ID: {}", promoId);
                        return new EntityNotFoundException("Promotion not found with ID: " + promoId);
                    });
            // Step 3: Sanitize all user input to prevent XSS [NFR-003]
            updatedPromotion.setCode(sanitizeInput(updatedPromotion.getCode()));
            if (updatedPromotion.getDescription() != null) {
                updatedPromotion.setDescription(sanitizeInput(updatedPromotion.getDescription()));
            }
            // Step 4: Validate business rules [REQ-017]
            validatePromotion(updatedPromotion);
            // Step 5: Check for duplicate promo code if it's changed
            if (!existingPromotion.getCode().equals(updatedPromotion.getCode()) && promotionRepository.existsByCode(updatedPromotion.getCode())) {
                LOG.warn("[PROMOTION_SERVICE] [REQ-017] Duplicate promo code detected during update: {}", updatedPromotion.getCode());
                throw new PromotionCodeAlreadyExistsException("Promotion code already exists: " + updatedPromotion.getCode());
            }
            // Step 6: Update fields
            existingPromotion.setCode(updatedPromotion.getCode());
            existingPromotion.setDiscountPercent(updatedPromotion.getDiscountPercent());
            existingPromotion.setStartDate(updatedPromotion.getStartDate());
            existingPromotion.setEndDate(updatedPromotion.getEndDate());
            existingPromotion.setDescription(updatedPromotion.getDescription());
            existingPromotion.setActive(updatedPromotion.isActive());
            // Step 7: Persist updates using Hibernate prepared statements [NFR-003]
            Promotion updated = promotionRepository.persist(existingPromotion);
            // Step 8: Send notification about updated promotion [REQ-016]
            notificationService.sendPromotionUpdateNotification(updated);
            LOG.info("[PROMOTION_SERVICE] [REQ-017] Operation {} completed successfully for promo ID: {}", operation, promoId);
            return updated;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-017] Operation {} failed for user: {} for promo ID: {}. Raw error: {}", operation, currentUser.getUserId(), promoId, e.getMessage(), e);
            if (e instanceof EntityNotFoundException || e instanceof PromotionCodeAlreadyExistsException || e instanceof ValidationException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to update promotion", e);
        }
    }

    /**
     * Deletes a promotion by ID.
     * Only accessible by Center Admin and Manager roles per [REQ-017].
     *
     * @param promoId The ID of the promotion to delete
     * @throws EntityNotFoundException if the promotion does not exist
     * @throws UnauthorizedAccessException if the current user does not have permission
     */
    @Transactional
    public void deletePromotion(UUID promoId) {
        String operation = "DELETE_PROMOTION";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-017] Starting operation: {} for user: {} for promo ID: {}", operation, currentUser.getUserId(), promoId);
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Check if promotion exists
            if (!promotionRepository.existsById(promoId)) {
                LOG.warn("[PROMOTION_SERVICE] [REQ-017] Promotion not found for deletion, ID: {}", promoId);
                throw new EntityNotFoundException("Promotion not found with ID: " + promoId);
            }
            // Step 3: Delete promotion using Hibernate prepared statement delete query [NFR-003]
            promotionRepository.deleteById(promoId);
            LOG.info("[PROMOTION_SERVICE] [REQ-017] Operation {} completed successfully for promo ID: {}", operation, promoId);
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-017] Operation {} failed for user: {} for promo ID: {}. Raw error: {}", operation, currentUser.getUserId(), promoId, e.getMessage(), e);
            if (e instanceof EntityNotFoundException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to delete promotion", e);
        }
    }

    /**
     * Retrieves all active promotions (current date is within start and end date, or end date is null).
     * Only accessible by Center Admin and Manager roles per [REQ-017].
     *
     * @return List of active promotions
     * @throws UnauthorizedAccessException if the current user does not have permission
     */
    @Transactional
    public List<Promotion> getActivePromotions() {
        String operation = "GET_ACTIVE_PROMOTIONS";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-017] Starting operation: {} for user: {}", operation, currentUser.getUserId());
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Fetch active promotions using parameterized query (prevents SQL injection [NFR-003])
            LocalDate currentDate = LocalDate.now();
            List<Promotion> activePromotions = promotionRepository.findActivePromotions(currentDate);
            LOG.info("[PROMOTION_SERVICE] [REQ-017] Operation {} completed successfully. Retrieved {} active promotions for user: {}", operation, activePromotions.size(), currentUser.getUserId());
            return activePromotions;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-017] Operation {} failed for user: {}. Raw error: {}", operation, currentUser.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve active promotions", e);
        }
    }

    // ====================== ANNOUNCEMENT MANAGEMENT METHODS [REQ-018] ======================
    /**
     * Creates a new system announcement with validated data and sanitized input.
     * Only accessible by Center Admin and Manager roles per [REQ-018].
     *
     * @param announcement The announcement entity to create
     * @return The created announcement entity
     * @throws UnauthorizedAccessException if the current user does not have permission
     * @throws ValidationException if the announcement data is invalid
     */
    @Transactional
    public Announcement createAnnouncement(Announcement announcement) {
        String operation = "CREATE_ANNOUNCEMENT";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting operation: {} for user: {}", operation, currentUser.getUserId());
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Sanitize all user input to prevent XSS [NFR-003]
            announcement.setTitle(sanitizeInput(announcement.getTitle()));
            announcement.setContent(sanitizeInput(announcement.getContent()));
            // Step 3: Validate business rules [REQ-018]
            validateAnnouncement(announcement);
            // Step 4: Set default active status
            announcement.setActive(true);
            // Step 5: Persist announcement using Hibernate prepared statements (prevents SQL injection [NFR-003])
            Announcement createdAnnouncement = announcementRepository.persist(announcement);
            // Step 6: Send notification to all users about new announcement [REQ-016]
            notificationService.sendAnnouncementNotification(createdAnnouncement);
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Operation {} completed successfully. Created announcement ID: {}", operation, createdAnnouncement.getAnnouncementId());
            return createdAnnouncement;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Operation {} failed for user: {}. Raw error: {}", operation, currentUser.getUserId(), e.getMessage(), e);
            if (e instanceof ValidationException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to create announcement", e);
        }
    }

    /**
     * Updates an existing system announcement with validated data and sanitized input.
     * Only accessible by Center Admin and Manager roles per [REQ-018].
     *
     * @param announcementId The ID of the announcement to update
     * @param updatedAnnouncement The updated announcement data
     * @return The updated announcement entity
     * @throws EntityNotFoundException if the announcement does not exist
     * @throws UnauthorizedAccessException if the current user does not have permission
     * @throws ValidationException if the updated data is invalid
     */
    @Transactional
    public Announcement updateAnnouncement(UUID announcementId, Announcement updatedAnnouncement) {
        String operation = "UPDATE_ANNOUNCEMENT";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting operation: {} for user: {} for announcement ID: {}", operation, currentUser.getUserId(), announcementId);
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Fetch existing announcement from database (using prepared statement query [NFR-003])
            Announcement existingAnnouncement = announcementRepository.findById(announcementId)
                    .orElseThrow(() -> {
                        LOG.warn("[PROMOTION_SERVICE] [REQ-018] Announcement not found for ID: {}", announcementId);
                        return new EntityNotFoundException("Announcement not found with ID: " + announcementId);
                    });
            // Step 3: Sanitize all user input to prevent XSS [NFR-003]
            updatedAnnouncement.setTitle(sanitizeInput(updatedAnnouncement.getTitle()));
            updatedAnnouncement.setContent(sanitizeInput(updatedAnnouncement.getContent()));
            // Step 4: Validate business rules [REQ-018]
            validateAnnouncement(updatedAnnouncement);
            // Step 5: Update fields
            existingAnnouncement.setTitle(updatedAnnouncement.getTitle());
            existingAnnouncement.setContent(updatedAnnouncement.getContent());
            existingAnnouncement.setStartDate(updatedAnnouncement.getStartDate());
            existingAnnouncement.setEndDate(updatedAnnouncement.getEndDate());
            existingAnnouncement.setActive(updatedAnnouncement.isActive());
            // Step 6: Persist updates using Hibernate prepared statements [NFR-003]
            Announcement updated = announcementRepository.persist(existingAnnouncement);
            // Step 7: Send notification about updated announcement [REQ-016]
            notificationService.sendAnnouncementUpdateNotification(updated);
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Operation {} completed successfully for announcement ID: {}", operation, announcementId);
            return updated;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Operation {} failed for user: {} for announcement ID: {}. Raw error: {}", operation, currentUser.getUserId(), announcementId, e.getMessage(), e);
            if (e instanceof EntityNotFoundException || e instanceof ValidationException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to update announcement", e);
        }
    }

    /**
     * Deletes an announcement by ID.
     * Only accessible by Center Admin and Manager roles per [REQ-018].
     *
     * @param announcementId The ID of the announcement to delete
     * @throws EntityNotFoundException if the announcement does not exist
     * @throws UnauthorizedAccessException if the current user does not have permission
     */
    @Transactional
    public void deleteAnnouncement(UUID announcementId) {
        String operation = "DELETE_ANNOUNCEMENT";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting operation: {} for user: {} for announcement ID: {}", operation, currentUser.getUserId(), announcementId);
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Check if announcement exists
            if (!announcementRepository.existsById(announcementId)) {
                LOG.warn("[PROMOTION_SERVICE] [REQ-018] Announcement not found for deletion, ID: {}", announcementId);
                throw new EntityNotFoundException("Announcement not found with ID: " + announcementId);
            }
            // Step 3: Delete announcement using Hibernate prepared statement delete query [NFR-003]
            announcementRepository.deleteById(announcementId);
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Operation {} completed successfully for announcement ID: {}", operation, announcementId);
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Operation {} failed for user: {} for announcement ID: {}. Raw error: {}", operation, currentUser.getUserId(), announcementId, e.getMessage(), e);
            if (e instanceof EntityNotFoundException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to delete announcement", e);
        }
    }

    /**
     * Retrieves all active announcements (current date is within start and end date, or end date is null, and is active).
     * Only accessible by Center Admin and Manager roles per [REQ-018].
     *
     * @return List of active announcements
     * @throws UnauthorizedAccessException if the current user does not have permission
     */
    @Transactional
    public List<Announcement> getActiveAnnouncements() {
        String operation = "GET_ACTIVE_ANNOUNCEMENTS";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting operation: {} for user: {}", operation, currentUser.getUserId());
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Fetch active announcements using parameterized query (prevents SQL injection [NFR-003])
            LocalDate currentDate = LocalDate.now();
            List<Announcement> activeAnnouncements = announcementRepository.findActiveAnnouncements(currentDate);
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Operation {} completed successfully. Retrieved {} active announcements for user: {}", operation, activeAnnouncements.size(), currentUser.getUserId());
            return activeAnnouncements;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Operation {} failed for user: {}. Raw error: {}", operation, currentUser.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve active announcements", e);
        }
    }

    // ====================== SCHEDULED JOB FOR AUTOMATIC ANNOUNCEMENT EXPIRATION [REQ-018] ======================
    /**
     * Scheduled job that runs daily at midnight to automatically deactivate expired announcements.
     * Ensures announcements are hidden after their end date per [REQ-018].
     * Runs with system privileges, no RBAC check required.
     */
    @Scheduled(cron = SCHEDULED_JOB_CRON)
    @Transactional
    public void deactivateExpiredAnnouncements() {
        String operation = "DEACTIVATE_EXPIRED_ANNOUNCEMENTS";
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting scheduled operation: {}", operation);
        try {
            LocalDate currentDate = LocalDate.now();
            // Fetch all active announcements that have expired (end date < current date) using parameterized query [NFR-003]
            List<Announcement> expiredAnnouncements = announcementRepository.findExpiredActiveAnnouncements(currentDate);
            if (expiredAnnouncements.isEmpty()) {
                LOG.info("[PROMOTION_SERVICE] [REQ-018] No expired announcements found to deactivate");
                return;
            }
            // Deactivate all expired announcements
            for (Announcement announcement : expiredAnnouncements) {
                announcement.setActive(false);
                announcementRepository.persist(announcement);
                LOG.debug("[PROMOTION_SERVICE] [REQ-018] Deactivated expired announcement ID: {}, title: {}", announcement.getAnnouncementId(), announcement.getTitle());
            }
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Scheduled operation {} completed successfully. Deactivated {} expired announcements", operation, expiredAnnouncements.size());
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Scheduled operation {} failed. Raw error: {}", operation, e.getMessage(), e);
            // Throw runtime exception to trigger alert for operations team per [EXC-010]
            throw new RuntimeException("Failed to deactivate expired announcements", e);
        }
    }
}
```

# Day 1: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/AnnouncementController.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/AnnouncementController.java`
*   Traceability Audit Tags For This Task: ['[REQ-017]', '[REQ-018]', '[DAT-009]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai toàn bộ logic nghiệp vụ cho module quản lý khuyến mãi và thông báo hệ thống. Đối với khuyến mãi, xây dựng các phương thức CRUD với kiểm tra xác thực đầu vào nghiêm ngặt: phần trăm giảm giá phải nằm trong khoảng 0-100, ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu, mã khuyến mãi phải duy nhất. Đối với thông báo, triển khai logic lọc thông báo đang hoạt động dựa trên ngày hiện tại và cơ chế tự động ẩn thông báo sau ngày kết thúc thông qua scheduled job chạy hàng ngày. Tất cả các endpoint phải được bảo vệ bằng bộ lọc RBAC, chỉ cho phép Center Admin và Manager truy cập. Sử dụng prepared statements của Hibernate ORM để ngăn chặn SQL injection. Áp dụng làm sạch dữ liệu đầu vào để chống XSS.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-017]', '[REQ-018]', '[DAT-009]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.promotion;

// [IMPORTS LAYER - ENTERPRISE DEPENDENCY COMPLIANCE]
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Context;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.DefaultValue;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.nlh4j.saas.membership_hub.promotion.dto.AnnouncementRequestDTO;
import org.nlh4j.saas.membership_hub.promotion.dto.AnnouncementResponseDTO;
import org.nlh4j.saas.membership_hub.promotion.dto.PromotionRequestDTO;
import org.nlh4j.saas.membership_hub.promotion.dto.PromotionResponseDTO;
import org.nlh4j.saas.membership_hub.promotion.entity.Announcement;
import org.nlh4j.saas.membership_hub.promotion.entity.Promotion;
import org.nlh4j.saas.membership_hub.promotion.exception.AnnouncementNotFoundException;
import org.nlh4j.saas.membership_hub.promotion.exception.DuplicateAnnouncementException;
import org.nlh4j.saas.membership_hub.promotion.exception.DuplicatePromoCodeException;
import org.nlh4j.saas.membership_hub.promotion.exception.InvalidPromotionDataException;
import org.nlh4j.saas.membership_hub.promotion.exception.ValidationException;
import org.nlh4j.saas.membership_hub.promotion.service.AnnouncementService;
import org.nlh4j.saas.membership_hub.promotion.service.PromotionService;
import org.nlh4j.saas.membership_hub.security.CustomUserDetails;
import org.nlh4j.saas.membership_hub.security.SecurityContext;
import org.owasp.encoder.Encode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for managing system announcements and promotions
 * <p>
 * Exposes CRUD endpoints for announcements (REQ-018) and promotions (REQ-017),
 * enforces RBAC access control, input validation, XSS sanitization, and idempotency for mutation operations.
 * All database operations use Hibernate prepared statements to prevent SQL injection per NFR-003.
 * Active record filtering and auto-hide of expired announcements are handled by the service layer scheduled job.
 * </p>
 * @traceability [REQ-017], [REQ-018], [DAT-009]
 * @since 1.0
 * @author Membership Hub Engineering Team
 */
@ApplicationScoped
@Path("/api/v1")
public class AnnouncementController implements Serializable {

    private static final long serialVersionUID = 1L;

    // [CONSTANTS LAYER - ANTI-MAGIC NUMBERS COMPLIANCE [0.2]]
    // All business rule and configuration constants are declared at class crown level, no hardcoded values in operational logic
    public static final String TRACEABILITY_TAGS = "[REQ-017], [REQ-018], [DAT-009]";
    public static final String SUBSYSTEM_NAME = "Promotion-Announcement-Service"; // For audit logging [0.3]
    public static final int MAX_ANNOUNCEMENT_TITLE_LENGTH = 150; // Per DAT-009 schema constraint
    public static final int MAX_ANNOUNCEMENT_CONTENT_LENGTH = 2000; // Per DAT-009 schema constraint
    public static final int MAX_PROMO_CODE_LENGTH = 50; // Per DAT-009 schema constraint
    public static final int MAX_PROMO_DESCRIPTION_LENGTH = 2000; // Per DAT-009 schema constraint
    public static final int MIN_PROMO_DISCOUNT_PERCENT = 0; // Per REQ-017 business rule
    public static final int MAX_PROMO_DISCOUNT_PERCENT = 100; // Per REQ-017 business rule
    public static final int DEFAULT_PAGE_SIZE = 20; // Default pagination size for list endpoints
    public static final int MAX_PAGE_SIZE = 100; // Maximum allowed page size to prevent abuse
    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"; // Enterprise standard header for mutation idempotency [1.0]

    // [LOGGER LAYER - ENTERPRISE AUDIT COMPLIANCE [0.3]]
    private static final Logger logger = Logger.getLogger(AnnouncementController.class);

    // [DEPENDENCY INJECTION LAYER - QUARKUS NATIVE]
    @Inject
    AnnouncementService announcementService; // Business logic for announcement operations
    @Inject
    PromotionService promotionService; // Business logic for promotion operations
    @Inject
    SecurityContext securityContext; // Quarkus native security context for user identity extraction
    @Context
    HttpHeaders httpHeaders; // JAX-RS context for accessing request headers (idempotency key)

    // [INNER DTO LAYER - REQUEST/RESPONSE DATA CONTRACTS]
    // DTO for announcement creation/update requests, enforces input validation constraints
    public static class AnnouncementRequestDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "Announcement title is required")
        @Size(max = MAX_ANNOUNCEMENT_TITLE_LENGTH, message = "Title cannot exceed " + MAX_ANNOUNCEMENT_TITLE_LENGTH + " characters")
        private String title;

        @NotBlank(message = "Announcement content is required")
        @Size(max = MAX_ANNOUNCEMENT_CONTENT_LENGTH, message = "Content cannot exceed " + MAX_ANNOUNCEMENT_CONTENT_LENGTH + " characters")
        private String content;

        @PastOrPresent(message = "Start date cannot be in the future")
        private LocalDate startDate;

        @FutureOrPresent(message = "End date cannot be in the past")
        private LocalDate endDate;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }

    // DTO for promotion creation/update requests, enforces business rule validation
    public static class PromotionRequestDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "Promo code is required")
        @Size(max = MAX_PROMO_CODE_LENGTH, message = "Promo code cannot exceed " + MAX_PROMO_CODE_LENGTH + " characters")
        @Pattern(regexp = "^[A-Z0-9]+$", message = "Promo code must contain only uppercase letters and numbers")
        private String code;

        @NotNull(message = "Discount percent is required")
        @Min(value = MIN_PROMO_DISCOUNT_PERCENT, message = "Discount percent cannot be less than " + MIN_PROMO_DISCOUNT_PERCENT)
        @Max(value = MAX_PROMO_DISCOUNT_PERCENT, message = "Discount percent cannot exceed " + MAX_PROMO_DISCOUNT_PERCENT)
        private Integer discountPercent;

        @PastOrPresent(message = "Start date cannot be in the future")
        private LocalDate startDate;

        @FutureOrPresent(message = "End date cannot be in the past")
        private LocalDate endDate;

        @Size(max = MAX_PROMO_DESCRIPTION_LENGTH, message = "Description cannot exceed " + MAX_PROMO_DESCRIPTION_LENGTH + " characters")
        private String description;

        // Getters and setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public Integer getDiscountPercent() { return discountPercent; }
        public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // DTO for announcement response payloads, prevents overposting by exposing only safe fields
    public static class AnnouncementResponseDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private UUID announcementId;
        private String title;
        private String content;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Constructor to map entity to DTO
        public AnnouncementResponseDTO(Announcement announcement) {
            this.announcementId = announcement.getAnnouncementId();
            this.title = announcement.getTitle();
            this.content = announcement.getContent();
            this.startDate = announcement.getStartDate();
            this.endDate = announcement.getEndDate();
            this.createdAt = announcement.getCreatedAt();
            this.updatedAt = announcement.getUpdatedAt();
        }

        // Getters
        public UUID getAnnouncementId() { return announcementId; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    // DTO for promotion response payloads, prevents overposting by exposing only safe fields
    public static class PromotionResponseDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private UUID promoId;
        private String code;
        private Integer discountPercent;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Constructor to map entity to DTO
        public PromotionResponseDTO(Promotion promotion) {
            this.promoId = promotion.getPromoId();
            this.code = promotion.getCode();
            this.discountPercent = promotion.getDiscountPercent();
            this.startDate = promotion.getStartDate();
            this.endDate = promotion.getEndDate();
            this.description = promotion.getDescription();
            this.createdAt = promotion.getCreatedAt();
            this.updatedAt = promotion.getUpdatedAt();
        }

        // Getters
        public UUID getPromoId() { return promoId; }
        public String getCode() { return code; }
        public Integer getDiscountPercent() { return discountPercent; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public String getDescription() { return description; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    // [INNER EXCEPTION LAYER - BUSINESS EXCEPTION CONTRACTS]
    // Custom validation exception for input rule violations
    public static class ValidationException extends Exception {
        private static final long serialVersionUID = 1L;
        public ValidationException(String message) { super(message); }
    }

    // Custom exception for duplicate promo code conflicts
    public static class DuplicatePromoCodeException extends Exception {
        private static final long serialVersionUID = 1L;
        public DuplicatePromoCodeException(String message) { super(message); }
    }

    // Custom exception for duplicate announcement conflicts
    public static class DuplicateAnnouncementException extends Exception {
        private static final long serialVersionUID = 1L;
        public DuplicateAnnouncementException(String message) { super(message); }
    }

    // Custom exception for invalid promotion data (discount range, date order)
    public static class InvalidPromotionDataException extends Exception {
        private static final long serialVersionUID = 1L;
        public InvalidPromotionDataException(String message) { super(message); }
    }

    // [UTILITY METHOD LAYER - SECURITY & IDENTITY]
    /**
     * Sanitizes user input to prevent XSS attacks per OWASP Top 10 compliance [NFR-003]
     * Uses OWASP Java HTML Sanitizer to remove malicious scripts and tags from input
     * @param input the raw input string from user request
     * @return sanitized string safe for storage and display
     */
    private String sanitizeInput(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        // Encode HTML special characters to neutralize XSS payloads
        return Encode.forHtml(input);
    }

    /**
     * Retrieves the current authenticated user ID from the Quarkus security context
     * @return UUID of the current authenticated user
     * @throws WebApplicationException with 401 status if user is not authenticated
     */
    private UUID getCurrentUserId() {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            // Log unauthenticated access attempt per audit requirements [NFR-006]
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Unauthenticated access attempt to promotion/announcement endpoint");
            throw new WebApplicationException("User not authenticated", Response.Status.UNAUTHORIZED);
        }
        // Cast to project-specific CustomUserDetails to extract user ID and roles
        return ((CustomUserDetails) securityContext.getUserPrincipal()).getUserId();
    }

    // ==============================================
    // ANNOUNCEMENT ENDPOINTS (REQ-018)
    // ==============================================

    /**
     * Retrieves all active announcements visible to all authenticated users
     * Active announcements are those where current date is between start_date and end_date, or end_date is null
     * @return list of active announcements with 200 OK status
     * @traceability [REQ-018]
     */
    @GET
    @Path("/announcements/active")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActiveAnnouncements() {
        try {
            // Entry audit log per enterprise logging requirements [0.3]
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Fetching active announcements for public access");
            // Service layer handles filtering of active records and scheduled auto-hide of expired announcements
            List<AnnouncementResponseDTO> activeAnnouncements = announcementService.getActiveAnnouncements();
            // Exit audit log with result count
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Fetched {} active announcements", activeAnnouncements.size());
            return Response.ok(activeAnnouncements).build();
        } catch (Exception e) {
            // Comprehensive error logging with required 3 context keys [0.3]
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Failed to fetch active announcements. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while fetching active announcements", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves all announcements (including inactive/expired) for admin users (Center Admin, Manager)
     * Supports pagination with page and size query parameters
     * @param page page number (default 1)
     * @param size page size (default 20, max 100)
     * @return paginated list of all announcements with 200 OK status
     * @throws WebApplicationException with 400 if pagination parameters are invalid, 403 if user lacks permissions
     * @traceability [REQ-018]
     */
    @GET
    @Path("/announcements")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAnnouncements(@QueryParam("page") @DefaultValue("1") int page, @QueryParam("size") @DefaultValue(String.valueOf(DEFAULT_PAGE_SIZE)) int size) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Fetching all announcements for admin user: {}", getCurrentUserId());
            // Validate pagination parameters to prevent abuse
            if (page < 1) {
                throw new ValidationException("Page number must be at least 1");
            }
            if (size < 1 || size > MAX_PAGE_SIZE) {
                throw new ValidationException("Page size must be between 1 and " + MAX_PAGE_SIZE);
            }
            // Service layer uses prepared statements for paginated queries to prevent SQL injection [NFR-003]
            List<AnnouncementResponseDTO> announcements = announcementService.getAllAnnouncements(page, size);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Fetched {} announcements (page {}, size {})", announcements.size(), page, size);
            return Response.ok(announcements).build();
        } catch (ValidationException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Invalid pagination parameters. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Invalid pagination: " + e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Failed to fetch all announcements. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while fetching announcements", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates a new system announcement (admin only: Center Admin, Manager)
     * Enforces input sanitization, idempotency, and business rule validation
     * @param request announcement creation request DTO
     * @return created announcement with 201 CREATED status
     * @throws WebApplicationException with 400 for invalid input, 409 for duplicate, 403 for insufficient permissions
     * @traceability [REQ-018]
     */
    @POST
    @Path("/announcements")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAnnouncement(@Valid AnnouncementRequestDTO request) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Creating new announcement for user: {}", getCurrentUserId());
            // Step 1: Sanitize input to prevent XSS attacks per OWASP Top 10 [NFR-003]
            String sanitizedTitle = sanitizeInput(request.getTitle());
            String sanitizedContent = sanitizeInput(request.getContent());
            // Step 2: Defense-in-depth validation for length constraints
            if (sanitizedTitle.length() > MAX_ANNOUNCEMENT_TITLE_LENGTH) {
                throw new ValidationException("Title exceeds maximum allowed length of " + MAX_ANNOUNCEMENT_TITLE_LENGTH + " characters");
            }
            if (sanitizedContent.length() > MAX_ANNOUNCEMENT_CONTENT_LENGTH) {
                throw new ValidationException("Content exceeds maximum allowed length of " + MAX_ANNOUNCEMENT_CONTENT_LENGTH + " characters");
            }
            // Step 3: Validate idempotency key to prevent duplicate mutation requests per enterprise governance [1.0]
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Step 4: Call service layer to create announcement (uses Hibernate prepared statements to prevent SQL injection [NFR-003])
            AnnouncementResponseDTO response = announcementService.createAnnouncement(sanitizedTitle, sanitizedContent, request.getStartDate(), request.getEndDate(), idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Announcement created successfully with ID: {}", response.getAnnouncementId());
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (ConstraintViolationException e) {
            // Handle Jakarta validation errors with detailed field-level messages
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement creation failed due to invalid input. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            String validationErrors = e.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new WebApplicationException("Validation failed: " + validationErrors, Response.Status.BAD_REQUEST);
        } catch (DuplicateAnnouncementException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement creation failed due to duplicate. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Duplicate announcement: " + e.getMessage(), Response.Status.CONFLICT);
        } catch (Exception e) {
            // Preserve original exception cause chain per enterprise exception handling rules [0.3]
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement creation failed due to system error. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while creating announcement", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates an existing announcement (admin only: Center Admin, Manager)
     * @param id UUID of the announcement to update
     * @param request updated announcement data
     * @return updated announcement with 200 OK status
     * @throws WebApplicationException with 400 for invalid input, 404 if announcement not found, 403 for insufficient permissions
     * @traceability [REQ-018]
     */
    @PUT
    @Path("/announcements/{id}")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAnnouncement(@PathParam("id") UUID id, @Valid AnnouncementRequestDTO request) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Updating announcement ID: {} for user: {}", id, getCurrentUserId());
            // Sanitize input to prevent XSS
            String sanitizedTitle = sanitizeInput(request.getTitle());
            String sanitizedContent = sanitizeInput(request.getContent());
            // Validate idempotency key for mutation operation
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Call service to update announcement
            AnnouncementResponseDTO response = announcementService.updateAnnouncement(id, sanitizedTitle, sanitizedContent, request.getStartDate(), request.getEndDate(), idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Announcement ID: {} updated successfully", id);
            return Response.ok(response).build();
        } catch (ConstraintViolationException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement update failed due to invalid input. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            String validationErrors = e.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new WebApplicationException("Validation failed: " + validationErrors, Response.Status.BAD_REQUEST);
        } catch (AnnouncementNotFoundException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement not found for update. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Announcement not found with ID: " + id, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement update failed. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while updating announcement", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes an announcement (admin only: Center Admin, Manager)
     * @param id UUID of the announcement to delete
     * @return 200 OK with success message
     * @throws WebApplicationException with 404 if announcement not found, 403 for insufficient permissions
     * @traceability [REQ-018]
     */
    @DELETE
    @Path("/announcements/{id}")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAnnouncement(@PathParam("id") UUID id) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Deleting announcement ID: {} for user: {}", id, getCurrentUserId());
            // Validate idempotency key for mutation operation
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Call service to delete announcement
            announcementService.deleteAnnouncement(id, idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Announcement ID: {} deleted successfully", id);
            return Response.ok("{\"message\": \"Announcement deleted successfully\"}").build();
        } catch (AnnouncementNotFoundException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement not found for deletion. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Announcement not found with ID: " + id, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement deletion failed. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while deleting announcement", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // ==============================================
    // PROMOTION ENDPOINTS (REQ-017)
    // ==============================================

    /**
     * Retrieves all active promotions visible to all authenticated users
     * Active promotions are those where current date is between start_date and end_date, or end_date is null
     * @return list of active promotions with 200 OK status
     * @traceability [REQ-017]
     */
    @GET
    @Path("/promotions/active")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActivePromotions() {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Fetching active promotions for public access");
            // Service layer filters active records and enforces business rules for validity periods
            List<PromotionResponseDTO> activePromotions = promotionService.getActivePromotions();
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Fetched {} active promotions", activePromotions.size());
            return Response.ok(activePromotions).build();
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Failed to fetch active promotions. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while fetching active promotions", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves all promotions (including inactive/expired) for admin users (Center Admin, Manager)
     * Supports pagination with page and size query parameters
     * @param page page number (default 1)
     * @param size page size (default 20, max 100)
     * @return paginated list of all promotions with 200 OK status
     * @throws WebApplicationException with 400 for invalid pagination, 403 for insufficient permissions
     * @traceability [REQ-017]
     */
    @GET
    @Path("/promotions")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPromotions(@QueryParam("page") @DefaultValue("1") int page, @QueryParam("size") @DefaultValue(String.valueOf(DEFAULT_PAGE_SIZE)) int size) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Fetching all promotions for admin user: {}", getCurrentUserId());
            // Validate pagination parameters
            if (page < 1) {
                throw new ValidationException("Page number must be at least 1");
            }
            if (size < 1 || size > MAX_PAGE_SIZE) {
                throw new ValidationException("Page size must be between 1 and " + MAX_PAGE_SIZE);
            }
            // Service layer uses prepared statements for paginated queries to prevent SQL injection [NFR-003]
            List<PromotionResponseDTO> promotions = promotionService.getAllPromotions(page, size);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Fetched {} promotions (page {}, size {})", promotions.size(), page, size);
            return Response.ok(promotions).build();
        } catch (ValidationException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Invalid pagination parameters. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Invalid pagination: " + e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Failed to fetch all promotions. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while fetching promotions", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates a new promotion (admin only: Center Admin, Manager)
     * Enforces business rules: discount 0-100, end date >= start date, unique promo code
     * @param request promotion creation request DTO
     * @return created promotion with 201 CREATED status
     * @throws WebApplicationException with 400 for invalid input, 409 for duplicate code, 403 for insufficient permissions
     * @traceability [REQ-017]
     */
    @POST
    @Path("/promotions")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPromotion(@Valid PromotionRequestDTO request) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Creating new promotion for user: {}", getCurrentUserId());
            // Step 1: Sanitize input to prevent XSS attacks per OWASP Top 10 [NFR-003]
            String sanitizedCode = sanitizeInput(request.getCode()).toUpperCase(); // Promo codes are standardized to uppercase
            String sanitizedDescription = request.getDescription() != null ? sanitizeInput(request.getDescription()) : null;
            // Step 2: Defense-in-depth validation for business rules
            if (request.getDiscountPercent() < MIN_PROMO_DISCOUNT_PERCENT || request.getDiscountPercent() > MAX_PROMO_DISCOUNT_PERCENT) {
                throw new InvalidPromotionDataException("Discount percent must be between " + MIN_PROMO_DISCOUNT_PERCENT + " and " + MAX_PROMO_DISCOUNT_PERCENT);
            }
            if (request.getEndDate() != null && request.getStartDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
                throw new InvalidPromotionDataException("End date cannot be before start date");
            }
            // Step 3: Validate idempotency key to prevent duplicate mutation requests per enterprise governance [1.0]
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Step 4: Call service layer to create promotion (uses Hibernate prepared statements to prevent SQL injection [NFR-003])
            PromotionResponseDTO response = promotionService.createPromotion(sanitizedCode, request.getDiscountPercent(), request.getStartDate(), request.getEndDate(), sanitizedDescription, idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Promotion created successfully with code: {}", response.getCode());
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (ConstraintViolationException e) {
            // Handle Jakarta validation errors
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion creation failed due to invalid input. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            String validationErrors = e.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new WebApplicationException("Validation failed: " + validationErrors, Response.Status.BAD_REQUEST);
        } catch (DuplicatePromoCodeException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion creation failed due to duplicate code. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Duplicate promo code: " + e.getMessage(), Response.Status.CONFLICT);
        } catch (InvalidPromotionDataException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion creation failed due to invalid data. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Invalid promotion data: " + e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            // Preserve original exception cause chain per enterprise exception handling rules [0.3]
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion creation failed due to system error. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while creating promotion", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates an existing promotion (admin only: Center Admin, Manager)
     * @param id UUID of the promotion to update
     * @param request updated promotion data
     * @return updated promotion with 200 OK status
     * @throws WebApplicationException with 400 for invalid input, 404 if promotion not found, 409 for duplicate code, 403 for insufficient permissions
     * @traceability [REQ-017]
     */
    @PUT
    @Path("/promotions/{id}")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePromotion(@PathParam("id") UUID id, @Valid PromotionRequestDTO request) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Updating promotion ID: {} for user: {}", id, getCurrentUserId());
            // Sanitize input to prevent XSS
            String sanitizedCode = sanitizeInput(request.getCode()).toUpperCase();
            String sanitizedDescription = request.getDescription() != null ? sanitizeInput(request.getDescription()) : null;
            // Validate business rules
            if (request.getDiscountPercent() < MIN_PROMO_DISCOUNT_PERCENT || request.getDiscountPercent() > MAX_PROMO_DISCOUNT_PERCENT) {
                throw new InvalidPromotionDataException("Discount percent must be between " + MIN_PROMO_DISCOUNT_PERCENT + " and " + MAX_PROMO_DISCOUNT_PERCENT);
            }
            if (request.getEndDate() != null && request.getStartDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
                throw new InvalidPromotionDataException("End date cannot be before start date");
            }
            // Validate idempotency key for mutation operation
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Call service to update promotion
            PromotionResponseDTO response = promotionService.updatePromotion(id, sanitizedCode, request.getDiscountPercent(), request.getStartDate(), request.getEndDate(), sanitizedDescription, idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Promotion ID: {} updated successfully", id);
            return Response.ok(response).build();
        } catch (ConstraintViolationException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion update failed due to invalid input. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            String validationErrors = e.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new WebApplicationException("Validation failed: " + validationErrors, Response.Status.BAD_REQUEST);
        } catch (DuplicatePromoCodeException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion update failed due to duplicate code. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Duplicate promo code: " + e.getMessage(), Response.Status.CONFLICT);
        } catch (InvalidPromotionDataException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion update failed due to invalid data. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Invalid promotion data: " + e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (AnnouncementNotFoundException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion not found for update. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Promotion not found with ID: " + id, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion update failed. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while updating promotion", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a promotion (admin only: Center Admin, Manager)
     * @param id UUID of the promotion to delete
     * @return 200 OK with success message
     * @throws WebApplicationException with 404 if promotion not found, 403 for insufficient permissions
     * @traceability [REQ-017]
     */
    @DELETE
    @Path("/promotions/{id}")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePromotion(@PathParam("id") UUID id) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Deleting promotion ID: {} for user: {}", id, getCurrentUserId());
            // Validate idempotency key for mutation operation
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Call service to delete promotion
            promotionService.deletePromotion(id, idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Promotion ID: {} deleted successfully", id);
            return Response.ok("{\"message\": \"Promotion deleted successfully\"}").build();
        } catch (AnnouncementNotFoundException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion not found for deletion. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Promotion not found with ID: " + id, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion deletion failed. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while deleting promotion", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
```

