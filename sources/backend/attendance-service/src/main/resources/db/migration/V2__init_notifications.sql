-- ============================================
-- FILE: V2__init_notifications.sql
-- SCOPE: Notifications
-- TAGS: [DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [DAT-012]
-- ============================================

-- Bảng notifications lưu trữ hàng đợi và lịch sử thông báo đa kênh
-- Hỗ trợ cơ chế retry [EXC-003] và dead letter queue
-- Thiết kế tuân thủ ANSI SQL, không sử dụng ENUM đặc thù database

CREATE TABLE notifications (
    notification_id UUID NOT NULL,
    user_id UUID NULL,
    group_zalo VARCHAR(50) NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    delivered BOOLEAN NOT NULL DEFAULT false,
    retry_count INT NOT NULL DEFAULT 0,
    CONSTRAINT pk_notifications PRIMARY KEY (notification_id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT ck_notifications_target CHECK ((user_id IS NOT NULL) OR (group_zalo IS NOT NULL)),
    CONSTRAINT ck_notifications_delivered CHECK (delivered IN (false, true)),
    CONSTRAINT ck_notifications_retry_count CHECK (retry_count >= 0)
);

-- Index tối ưu cho truy vấn lấy thông báo của user cụ thể (push notification)
CREATE INDEX idx_notifications_user_id ON notifications(user_id);

-- Index tối ưu cho truy vấn lịch sử gửi theo thời gian (retry queue processing)
CREATE INDEX idx_notifications_sent_at ON notifications(sent_at);

-- Khuyến nghị partitioning cho bảng audit_logs khi dữ liệu vượt 10 triệu bản ghi:
--   - Sử dụng PostgreSQL declarative partitioning theo cột occurred_at (range theo tháng)
--   - Hoặc sử dụng pg_partman extension để tự động tạo partition hàng tháng
--   - Chuyển các partition cũ sang tablespace riêng để tối ưu storage