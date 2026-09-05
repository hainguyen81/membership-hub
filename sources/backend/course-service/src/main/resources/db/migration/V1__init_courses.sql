-- ============================================================================
-- Enterprise Database Migration Script: V1__init_courses.sql
-- Target Service: course-service
-- Module Context: org.nlh4j.membershiphub.courseservice
--
-- Traceability Verification Tags:
-- [DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006],
-- [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [DAT-012],
-- [REQ-007], [REQ-008], [REQ-009], [ARC-000], [NFR-001], [NFR-003]
--
-- Compliance: ANSI SQL / PostgreSQL 15+ Standards
-- Security: Multi-tenant tenant isolation via center_id, strict check constraints
-- ============================================================================

CREATE TABLE IF NOT EXISTS courses (
    course_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    teacher_id UUID NOT NULL,
    max_students INT NOT NULL DEFAULT 30,
    center_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_courses PRIMARY KEY (course_id),
    CONSTRAINT ck_courses_date_range CHECK (end_date >= start_date),
    CONSTRAINT ck_courses_max_students CHECK (max_students > 0),
    CONSTRAINT ck_courses_status CHECK (status IN ('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED'))
);

-- Query Pattern Optimization Indexes [NFR-001], [REQ-007], [REQ-008], [REQ-009]
CREATE INDEX IF NOT EXISTS idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX IF NOT EXISTS idx_courses_center_id ON courses(center_id);
CREATE INDEX IF NOT EXISTS idx_courses_start_date ON courses(start_date);
CREATE INDEX IF NOT EXISTS idx_courses_end_date ON courses(end_date);
CREATE INDEX IF NOT EXISTS idx_courses_dates ON courses(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_courses_center_status ON courses(center_id, status);

COMMENT ON TABLE courses IS 'Miền dữ liệu khóa học trực thuộc trung tâm và phân công giảng viên [DAT-003]';
COMMENT ON COLUMN courses.course_id IS 'Khóa chính định danh khóa học dạng UUID';
COMMENT ON COLUMN courses.title IS 'Tên tiêu đề của khóa học [REQ-007]';
COMMENT ON COLUMN courses.start_date IS 'Ngày bắt đầu khóa học [REQ-007]';
COMMENT ON COLUMN courses.end_date IS 'Ngày kết thúc khóa học [REQ-007]';
COMMENT ON COLUMN courses.teacher_id IS 'Khóa ngoại tham chiếu giảng viên phụ trách khóa học [REQ-009]';
COMMENT ON COLUMN courses.center_id IS 'Khóa ngoại phân vùng trung tâm đào tạo [ARC-002]';
COMMENT ON COLUMN courses.max_students IS 'Số lượng học viên tối đa tiếp nhận [REQ-008]';
COMMENT ON COLUMN courses.status IS 'Trạng thái vòng đời khóa học (DRAFT, ACTIVE, COMPLETED, CANCELLED)';