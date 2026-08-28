package org.nlh4j.saas.membership-hub.chatbot;

/**
 * EscalationService
 * --------------------------------------------------------------
 * Dịch vụ xử lý chuyển tiếp yêu cầu hỗ trợ khách hàng khi mức độ tin cậy
 * của phản hồi AI thấp (confidence < 0.7).
 *
 * Chức năng chính:
 *   • Tạo ticket hỗ trợ nội bộ với các thông tin liên quan.
 *   • Ghi log kiểm toán chi tiết vào bảng AUDIT_LOG để phục vụ mục đích
 *     phân tích, kiểm tra và cải tiến mô hình AI.
 *
 * @traceability [REQ-019]
 */
@Service
@Transactional
public class EscalationService {

    /* ==================== CONSTANTS (Top‑of‑Class) ==================== */
    /** Prefix dùng chung cho tất cả log message của service này. */
    public static final String LOG_PREFIX = "EscalationService";

    /** Độ trễ tối đa chấp nhận được cho thao tác ghi log kiểm toán (ms). */
    public static final long AUDIT_LOG_TIMEOUT_MS = 500L;

    /** Tên bảng kiểm toán trong cơ sở dữ liệu. */
    public static final String AUDIT_TABLE = "audit_log";

    /* ==================== DEPENDENCIES ==================== */
    private final AuditLogRepository auditLogRepository;
    private final SupportTicketRepository supportTicketRepository;

    /**
     * Constructor‑based DI – đảm bảo tính kiểm soát và dễ unit‑test.
     */
    public EscalationService(final AuditLogRepository auditLogRepository,
                             final SupportTicketRepository supportTicketRepository) {
        this.auditLogRepository = auditLogRepository;
        this.supportTicketRepository = supportTicketRepository;
    }

    /* ==================== PUBLIC API ==================== */

    /**
     * Kích hoạt quy trình chuyển tiếp hỗ trợ khi AI không đủ tự tin.
     *
     * @param sessionId   ID phiên chat của người dùng (dùng để truy vết).
     * @param userId      ID người dùng yêu cầu hỗ trợ (theo JWT).
     * @param aiReply     Phản hồi từ AI (để lưu lại context).
     * @param aiConfidence Điểm tin cậy từ AI (0.0‑1.0).
     * @return {@link EscalationResult} chứa ticketId và trạng thái.
     *
     * @throws IllegalArgumentException nếu các tham số đầu vào không hợp lệ.
     * @throws EscalationException      nếu không thể tạo ticket hoặc ghi log kiểm toán.
     *
     * @traceability [REQ-019]
     */
    @Transactional
    public EscalationResult escalate(final String sessionId,
                                    final UUID userId,
                                    final String aiReply,
                                    final double aiConfidence) {

        logger.info("[ENTRY] {} – Bắt đầu xử lý chuyển tiếp hỗ trợ. sessionId={}, userId={}",
                    LOG_PREFIX, sessionId, userId);

        // Kiểm tra đầu vào theo yêu cầu nghiệp vụ.
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId không được để trống");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId không được null");
        }
        if (aiConfidence < 0.0 || aiConfidence > 1.0) {
            throw new IllegalArgumentException("aiConfidence phải nằm trong khoảng [0,1]");
        }

        EscalationResult result;
        try {
            if (aiConfidence < 0.7) {
                // Tạo ticket hỗ trợ nội bộ.
                SupportTicket ticket = new SupportTicket();
                ticket.setSessionId(sessionId);
                ticket.setUserId(userId);
                ticket.setAiResponse(aiReply);
                ticket.setAiConfidence(aiConfidence);
                ticket.setStatus(SupportTicket.Status.OPEN);
                ticket.setCreatedAt(Instant.now());
                ticket.setUpdatedAt(Instant.now());

                SupportTicket saved = supportTicketRepository.save(ticket);
                logger.debug("[INFO] Ticket hỗ trợ được tạo – ticketId={}", saved.getId());

                // Ghi log kiểm toán.
                AuditLog audit = new AuditLog();
                audit.setUserId(userId);
                audit.setAction("ESCALATION");
                audit.setDetails(String.format(
                        "Session [%s] – AI confidence %.2f → chuyển tiếp hỗ trợ. Ticket ID: %s",
                        sessionId, aiConfidence, saved.getId()));
                audit.setTimestamp(Instant.now());

                // Thực hiện lưu với timeout để tránh treo hệ thống.
                CompletableFuture<AuditLog> auditFuture =
                        CompletableFuture.supplyAsync(() -> auditLogRepository.save(audit))
                                .orTimeout(AUDIT_LOG_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                                .exceptionally(ex -> {
                                    logger.error("[CRITICAL] {} – Ghi log kiểm toán thất bại. sessionId={}",
                                                 LOG_PREFIX, sessionId, ex);
                                    throw new EscalationException(
                                            "Không thể ghi log kiểm toán trong thời gian quy định", ex);
                                });

                auditFuture.thenAccept(auditLog -> logger.info(
                        "[AUDIT] Ghi log kiểm toán thành công – auditId={}", auditLog.getId()));

                result = new EscalationResult(saved.getId(), true, "Đã chuyển tiếp hỗ trợ thành công");
            } else {
                // AI đủ tự tin – không cần chuyển tiếp.
                logger.info("[INFO] {} – AI đủ tự tin (confidence={}), không cần chuyển tiếp.",
                            LOG_PREFIX, aiConfidence);
                result = new EscalationResult(null, false,
                        "AI đã cung cấp phản hồi đủ tin cậy, không cần chuyển tiếp hỗ trợ");
            }

            logger.info("[EXIT] {} – Hoàn tất xử lý chuyển tiếp hỗ trợ. result={}",
                        LOG_PREFIX, result);

        } catch (Exception ex) {
            // Bắt mọi ngoại lệ để đảm bảo hệ thống không bị crash.
            logger.error("[CRITICAL] {} – Xử lý chuyển tiếp hỗ trợ thất bại. sessionId={}",
                         LOG_PREFIX, sessionId, ex);

            // Ném một custom exception để đảm bảo contract lỗi được giữ nguyên.
            throw new EscalationException(
                    "Xử lý chuyển tiếp hỗ trợ thất bại do: " + ex.getMessage(), ex);
        }

        return result;
    }

    /* ==================== INNER DATA MODELS ==================== */

    /**
     * Entity ghi lại các thao tác kiểm toán của hệ thống chatbot.
     * Được lưu trong bảng {@code audit_log}.
     */
    @Entity
    @Table(name = AUDIT_TABLE)
    @Accessors(fluent = true)
    public static class AuditLog {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "user_id", nullable = false)
        private UUID userId;

        @Column(name = "action", nullable = false, length = 50)
        private String action;

        @Column(name = "details", columnDefinition = "TEXT")
        private String details;

        @Column(name = "timestamp", nullable = false)
        private Instant timestamp;

        // Getters / Setters (omitted for brevity – Lombok @Accessors handles it)
    }

    /**
     * Entity lưu thông tin ticket hỗ trợ được tạo ra khi AI không đủ tự tin.
     */
    @Entity
    @Table(name = "support_tickets")
    @Accessors(fluent = true)
    public static class SupportTicket {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "session_id", nullable = false, length = 36)
        private String sessionId;

        @Column(name = "user_id", nullable = false)
        private UUID userId;

        @Column(name = "ai_response", columnDefinition = "TEXT")
        private String aiResponse;

        @Column(name = "ai_confidence")
        private double aiConfidence;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false)
        private Status status = Status.OPEN;

        @Column(name = "created_at", nullable = false)
        private Instant createdAt;

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

        public enum Status {
            OPEN, IN_PROGRESS, RESOLVED, CLOSED
        }

        // Getters / Setters (omitted for brevity)
    }

    /* ==================== REPOSITORIES ==================== */

    public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
        // Mặc định CRUD; có thể thêm các phương thức truy vấn tùy chỉnh nếu cần.
    }

    public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
        // Mặc định CRUD; có thể thêm các phương thức truy vấn tùy chỉnh nếu cần.
    }

    /* ==================== RESPONSE DTO ==================== */

    /**
     * DTO trả về kết quả xử lý chuyển tiếp hỗ trợ.
     */
    @Data
    @AllArgsConstructor
    public static class EscalationResult {

        private final Long ticketId;      // null nếu không tạo ticket
        private final boolean escalated; // true nếu đã chuyển tiếp
        private final String message;     // thông báo chi tiết cho client
    }

    /* ==================== CUSTOM EXCEPTIONS ==================== */

    /**
     * Ngoại lệ tùy chỉnh để bao bọc lỗi xảy ra trong quá trình xử lý chuyển tiếp hỗ trợ.
     * Giữ nguyên cause chain để phục vụ việc debug và monitoring.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class EscalationException extends RuntimeException {

        public EscalationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}