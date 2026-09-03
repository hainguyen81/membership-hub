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