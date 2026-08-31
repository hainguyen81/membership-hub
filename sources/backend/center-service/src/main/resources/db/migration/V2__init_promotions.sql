-- ============================================
-- FILE: V2__init_student_cards.sql
-- SCOPE: Student Cards - Quản lý thẻ học viên
-- Traceability Tags: [DAT-006], [DAT-007], [DAT-009]
-- Phase: 1 - Day 2
-- Sub-Agent: Coder
-- Description: Khởi tạo bảng student_cards lưu trữ thông tin thẻ học viên,
--              bao gồm issue_date, validity_days, remaining_days, end_date.
--              Ràng buộc UNIQUE trên student_id đảm bảo mỗi học viên chỉ có một thẻ.
-- ============================================

CREATE TABLE student_cards (
    card_id UUID NOT NULL,
    student_id UUID NOT NULL,
    issue_date DATE NOT NULL,
    validity_days INT NOT NULL,
    remaining_days INT NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT pk_student_cards PRIMARY KEY (card_id),
    CONSTRAINT uq_student_cards_student UNIQUE (student_id),
    CONSTRAINT fk_student_cards_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT ck_student_cards_validity CHECK (validity_days > 0),
    CONSTRAINT ck_student_cards_remaining CHECK (remaining_days >= 0)
);

CREATE INDEX idx_student_cards_student_id ON student_cards(student_id);