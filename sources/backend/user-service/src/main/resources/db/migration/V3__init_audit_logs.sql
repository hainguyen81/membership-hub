-- ============================================
-- FILE: V3__init_audit_logs.sql
-- SCOPE: Audit Logs
-- TAGS: [DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [DAT-012]
-- ============================================

-- Bảng AuditLogs ghi lại toàn bộ hành động kiểm toán trong hệ thống Membership Hub
-- Tuân thủ 100% ANSI SQL, không sử dụng ENUM đặc thù database
-- Thiết kế tối ưu cho các pattern truy vấn: RBAC, báo cáo thời gian thực, truy vấn log theo khoảng thời gian
-- Khuyến nghị partitioning khi dữ liệu vượt 10 triệu bản ghi (xem cuối file)

CREATE TABLE audit_logs (
    log_id UUID NOT NULL,
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    target_entity VARCHAR(50) NOT NULL,
    target_id UUID,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    occurred_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_audit_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT ck_audit_action CHECK (action IN (
        'LOGIN_SUCCESS', 'LOGIN_FAILED', 'LOGOUT', 'SOCIAL_AUTH_SUCCESS', 'SOCIAL_AUTH_FAILED',
        'TOKEN_REFRESH', 'ROLE_CHANGED', 'USER_CREATED', 'USER_UPDATED', 'USER_DELETED',
        'CENTER_CREATED', 'CENTER_UPDATED', 'CENTER_DELETED', 'CENTER_ADMIN_ASSIGNED', 'CENTER_ADMIN_UNASSIGNED',
        'COURSE_CREATED', 'COURSE_UPDATED', 'COURSE_DELETED', 'TEACHER_ASSIGNED', 'TEACHER_UNASSIGNED',
        'ENROLLMENT_CREATED', 'ENROLLMENT_CANCELLED',
        'ATTENDANCE_SCANNED', 'ATTENDANCE_MANUAL',
        'CARD_RENEWED', 'CARD_ISSUED',
        'PROMOTION_CREATED', 'PROMOTION_UPDATED', 'PROMOTION_DELETED',
        'ANNOUNCEMENT_CREATED', 'ANNOUNCEMENT_UPDATED', 'ANNOUNCEMENT_DELETED',
        'NOTIFICATION_SENT', 'NOTIFICATION_FAILED',
        'CHATBOT_QUERY', 'CHATBOT_ESCALATED', 'SYSTEM_CONFIG_CHANGED'
    )),
    CONSTRAINT ck_audit_target_entity CHECK (target_entity IN (
        'USER', 'CENTER', 'COURSE', 'ENROLLMENT', 'ATTENDANCE', 'STUDENT_CARD',
        'PROMOTION', 'ANNOUNCEMENT', 'NOTIFICATION', 'CHATBOT_SESSION', 'SYSTEM_SETTING', 'AUTH'
    ))
);

-- Index tối ưu cho truy vấn RBAC: lấy lịch sử hành động của user cụ thể
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);

-- Index tối ưu cho truy vấn log theo thời gian (báo cáo, dashboard, compliance audit)
CREATE INDEX idx_audit_logs_occurred_at ON audit_logs(occurred_at);

-- Index tối ưu cho truy vấn theo đối tượng nghiệp vụ cụ thể (ví dụ: theo dõi thay đổi của một khoá học)
CREATE INDEX idx_audit_logs_target ON audit_logs(target_entity, target_id);

-- Index tối ưu cho lọc theo loại hành động (ví dụ: tất cả login success/failed)
CREATE INDEX idx_audit_logs_action ON audit_logs(action);

-- Composite index cho truy vấn thời gian thực real-time dashboard theo user và action
CREATE INDEX idx_audit_logs_user_action_time ON audit_logs(user_id, action, occurred_at);

-- Composite index cho truy vấn audit log theo khoảng thời gian và đối tượng
CREATE INDEX idx_audit_logs_entity_time ON audit_logs(target_entity, occurred_at);

-- ============================================
-- KHUYẾN NGHỊ PARTITIONING CHO BẢNG AUDIT_LOGS
-- Khi dữ liệu vượt 10 triệu bản ghi, áp dụng một trong các chiến lược:
-- ============================================
-- 1. RANGE PARTITIONING theo tháng (phù hợp cho truy vấn theo khoảng thời gian):
--    CREATE TABLE audit_logs_2024_01 PARTITION OF audit_logs
--    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
--
-- 2. HASH PARTITIONING nếu truy vấn chủ yếu theo user_id:
--    CREATE TABLE audit_logs_part_0 PARTITION OF audit_logs
--    FOR VALUES WITH (MODULUS 4, REMAINDER 0);
--
-- 3. Kết hợp BRIN index nếu dữ liệu có thứ tự thời gian tự nhiên:
--    CREATE INDEX idx_audit_logs_brin_time ON audit_logs USING BRIN(occurred_at);
--
-- 4. Chính sách lưu trữ: Tạo job định kỳ xóa/archive các bản ghi cũ hơn 1 năm vào cold storage
--    để đảm bảo hiệu năng truy vấn và tuân thủ [NFR-006] lưu trữ 1 năm.
-- ============================================