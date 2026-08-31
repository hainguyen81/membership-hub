-- ============================================
-- FILE: V2__init_student_cards.sql
-- SCOPE: Student Cards
-- TAG: [DAT-006]
-- DESCRIPTION: Khởi tạo bảng student_cards lưu trữ thông tin thẻ thành viên.
-- ============================================
CREATE TABLE student_cards (
    card_id UUID NOT NULL,
    student_id UUID NOT NULL,
    issue_date DATE NOT NULL,
    validity_days INT NOT NULL,
    remaining_days INT NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT pk_student_cards PRIMARY KEY (card_id),
    CONSTRAINT fk_student_cards_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT uq_student_cards_student UNIQUE (student_id),
    CONSTRAINT ck_student_cards_validity CHECK (validity_days > 0),
    CONSTRAINT ck_student_cards_remaining CHECK (remaining_days >= 0)
);

CREATE INDEX idx_student_cards_student_id ON student_cards(student_id);

-- ============================================
-- FILE: V2__init_notifications.sql
-- SCOPE: Notifications
-- TAG: [DAT-007]
-- DESCRIPTION: Khởi tạo bảng notifications lưu trữ hàng đợi thông báo đa kênh
--              (Push FCM/APNs, Zalo OA, In-App). Thiết kế theo mô hình outbox
--              với cơ chế retry và dead-letter queue. Đảm bảo mỗi bản ghi có
--              xác định ít nhất một kênh nhận thông qua ràng buộc CHECK.
-- ============================================

-- Bảng chính lưu trữ các dispatch notification
-- Hỗ trợ phân phối đa kênh: push notification (user_id), Zalo group (group_zalo)
CREATE TABLE notifications (
    -- Khóa chính duy nhất cho mỗi bản ghi thông báo, sinh bởi application layer
    notification_id UUID NOT NULL,
    
    -- Tham chiếu đến user nhận thông báo (nullable khi gửi đến Zalo group)
    -- Ràng buộc FK đảm bảo user tồn tại trong bảng users của user-service
    user_id UUID NULL,
    
    -- Mã nhóm Zalo OA (nullable khi gửi push đến user cụ thể)
    -- Độ dài tối đa 50 ký tự phù hợp với định dạng Zalo group ID
    group_zalo VARCHAR(50) NULL,
    
    -- Nội dung thông báo dạng text tự do, không giới hạn độ dài
    -- Hỗ trợ message dài cho Zalo OA và email notification
    message TEXT NOT NULL,
    
    -- Thời điểm hệ thống ghi nhận và đưa vào hàng đợi xử lý
    -- Mặc định là thời gian hiện tại của database server (now())
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    
    -- Trạng thái đã giao thành công hay chưa
    -- Mặc định false, được cập nhật bởi notification worker sau khi deliver
    delivered BOOLEAN NOT NULL DEFAULT false,
    
    -- Số lần đã thử gửi lại (retry) khi gặp lỗi tạm thời
    -- Mặc định 0, tối đa 3 lần theo quy tắc exponential backoff [EXC-003]
    retry_count INT NOT NULL DEFAULT 0,
    
    -- Khóa chính bảng notifications
    CONSTRAINT pk_notifications PRIMARY KEY (notification_id),
    
    -- Ràng buộc khóa ngoại đến bảng users(user_id)
    -- Đảm bảo referential integrity với user-service
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    
    -- Ràng buộc nghiệp vụ: mỗi thông báo phải có ít nhất một kênh nhận
    -- Ngăn chặn bản ghi thông báo không có đích đến (orphan notification)
    -- Logic: user_id IS NOT NULL (push) OR group_zalo IS NOT NULL (Zalo group)
    CONSTRAINT ck_notifications_target CHECK ((user_id IS NOT NULL) OR (group_zalo IS NOT NULL))
);

-- Chỉ mục băm (hash index) cho user_id để tối ưu truy vấn lấy thông báo của user
-- Hỗ trợ hiệu năng cao cho truy vấn: SELECT * FROM notifications WHERE user_id = ?
-- Được sử dụng bởi notification worker khi lấy pending notifications cho user cụ thể
CREATE INDEX idx_notifications_user_id ON notifications(user_id);

-- Chỉ mục B-tree cho sent_at để tối ưu truy vấn lịch sử và retry queue
-- Hỗ trợ truy vấn: SELECT * FROM notifications WHERE sent_at > ? AND delivered = false
-- Được sử dụng bởi scheduled job để xử lý các notification pending theo thứ tự thời gian
CREATE INDEX idx_notifications_sent_at ON notifications(sent_at);

-- ============================================
-- FILE: V2__init_promotions.sql
-- SCOPE: Promotions
-- TAG: [DAT-009]
-- DESCRIPTION: Khởi tạo bảng promotions lưu trữ các chương trình khuyến mãi cho trung tâm.
-- ============================================
CREATE TABLE promotions (
    promo_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    discount_percent SMALLINT NOT NULL,
    start_date DATE,
    end_date DATE,
    description TEXT,
    center_id UUID NOT NULL,
    CONSTRAINT pk_promotions PRIMARY KEY (promo_id),
    CONSTRAINT uq_promotions_code UNIQUE (code),
    CONSTRAINT fk_promotions_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT ck_promotions_discount CHECK (discount_percent BETWEEN 1 AND 100),
    CONSTRAINT ck_promotions_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_promotions_code ON promotions(code);
CREATE INDEX idx_promotions_center_id ON promotions(center_id);