-- ============================================
-- FILE: V2__init_promotions.sql
-- SCOPE: Promotions - Khuyến mãi theo trung tâm
-- Traceability Tags: [DAT-006], [DAT-007], [DAT-009]
-- Phase: 1 - Day 2
-- Sub-Agent: Coder
-- Description: Khởi tạo bảng promotions lưu trữ mã khuyến mãi,
--              phần trăm giảm giá, khoảng thời gian áp dụng và liên kết trung tâm.
--              Ràng buộc UNIQUE trên code đảm bảo không trùng lặp mã khuyến mãi.
--              Ràng buộc CHECK discount_percent BETWEEN 1 AND 100 đảm bảo giá trị hợp lệ.
--              Ràng buộc CHECK end_date IS NULL OR end_date >= start_date hỗ trợ khuyến mãi vĩnh viễn.
-- ============================================

-- Tạo bảng promotions với các ràng buộc toàn vẹn
CREATE TABLE promotions (
    -- Khóa chính duy nhất cho mỗi khuyến mãi
    promo_id UUID NOT NULL,
    -- Mã khuyến mãi (ví dụ: SUMMER2024) phải duy nhất toàn hệ thống
    code VARCHAR(30) NOT NULL,
    -- Phần trăm giảm giá từ 1% đến 100%
    discount_percent SMALLINT NOT NULL,
    -- Ngày bắt đầu áp dụng (có thể null nếu chưa xác định)
    start_date DATE,
    -- Ngày kết thúc áp dụng (có thể null cho khuyến mãi vĩnh viễn)
    end_date DATE,
    -- Mô tả chi tiết khuyến mãi
    description TEXT,
    -- Khóa ngoại tham chiếu trung tâm áp dụng khuyến mãi
    center_id UUID NOT NULL,
    -- Khóa chính
    CONSTRAINT pk_promotions PRIMARY KEY (promo_id),
    -- Ràng buộc duy nhất trên mã khuyến mãi để tránh trùng lặp
    CONSTRAINT uq_promotions_code UNIQUE (code),
    -- Ràng buộc khóa ngoại đến bảng centers
    CONSTRAINT fk_promotions_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    -- Ràng buộc phần trăm giảm giá trong khoảng hợp lệ
    CONSTRAINT ck_promotions_discount CHECK (discount_percent BETWEEN 1 AND 100),
    -- Ràng buộc ngày kết thúc phải sau ngày bắt đầu (nếu có ngày kết thúc)
    CONSTRAINT ck_promotions_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

-- Chỉ mục tối ưu truy vấn danh sách khuyến mãi theo trung tâm
CREATE INDEX idx_promotions_center_id ON promotions(center_id);

-- Chỉ mục tối ưu truy vấn khuyến mãi đang hoạt động theo khoảng ngày
CREATE INDEX idx_promotions_date_range ON promotions(start_date, end_date);