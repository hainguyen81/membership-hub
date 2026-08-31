-- ============================================
-- FILE: V2__init_promotions.sql
-- SCOPE: Promotions
-- TAG: [DAT-009]
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
    CONSTRAINT fk_promotions_center FOREIGN KEY (center_id) REFERENCES centers(center_id) ON DELETE RESTRICT,
    CONSTRAINT ck_promotions_discount CHECK (discount_percent BETWEEN 1 AND 100),
    CONSTRAINT ck_promotions_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

-- Index cho truy vấn khuyến mãi theo trung tâm
CREATE INDEX idx_promotions_center_id ON promotions(center_id);

-- Index cho truy vấn khuyến mãi đang hoạt động theo khoảng ngày
CREATE INDEX idx_promotions_date_range ON promotions(start_date, end_date);