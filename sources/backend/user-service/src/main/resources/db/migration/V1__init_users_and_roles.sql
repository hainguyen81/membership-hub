-- ============================================================================
-- FLYWAY MIGRATION: V1__init_users_and_roles.sql
-- SCOPE: Users & Roles (Phase 1 - Database Schema Initialization)
-- TAGS: [DAT-001], [DAT-008], [ARC-000], [REQ-001], [REQ-002], [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-003], [NFR-006]
-- DESCRIPTION: Comprehensive initialization of Users and Roles tables with enterprise-grade security, RBAC, and audit compliance
-- ============================================================================

-- ============================================
-- TABLE: roles
-- SCOPE: Core RBAC foundation for Membership Hub
-- ============================================
CREATE TABLE roles (
    role_id SMALLINT NOT NULL,
    name VARCHAR(30) NOT NULL,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_roles PRIMARY KEY (role_id),
    CONSTRAINT uq_roles_name UNIQUE (name),
    CONSTRAINT ck_roles_name CHECK (name IN ('SYSTEM_ADMIN','CENTER_ADMIN','MANAGER','TEACHER','STUDENT')),
    CONSTRAINT ck_roles_created_at CHECK (created_at <= updated_at)
);

-- ============================================
-- TABLE: users
-- SCOPE: User identity management with OAuth2 + JWT authentication
-- ============================================
CREATE TABLE users (
    user_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash CHAR(60) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role_id SMALLINT NOT NULL,
    provider VARCHAR(20) NOT NULL DEFAULT 'local',
    center_id UUID NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    last_login_at TIMESTAMP NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    profile_picture_url VARCHAR(500) NULL,
    preferences JSONB NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES roles(role_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_users_center FOREIGN KEY (center_id) REFERENCES centers(center_id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT ck_users_provider CHECK (provider IN ('local','firebase','google','facebook')),
    CONSTRAINT ck_users_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'),
    CONSTRAINT ck_users_full_name_not_empty CHECK (trim(full_name) <> ''),
    CONSTRAINT ck_users_created_before_updated CHECK (created_at <= updated_at),
    CONSTRAINT ck_users_password_hash_length CHECK (char_length(password_hash) = 60)
);

-- ============================================
-- INDEXES FOR USERS TABLE
-- SCOPE: Query performance optimization for RBAC and tenant isolation
-- ============================================
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_center_id ON users(center_id);
CREATE INDEX idx_users_email_lower ON users(lower(email));
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_provider ON users(provider);
CREATE INDEX idx_users_active_role ON users(is_active, role_id) WHERE is_active = true;

-- ============================================
-- TABLE: centers
-- SCOPE: Multi-tenant isolation foundation
-- ============================================
CREATE TABLE centers (
    center_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tax_id VARCHAR(20) NOT NULL,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_centers PRIMARY KEY (center_id),
    CONSTRAINT uq_centers_tax_id UNIQUE (tax_id),
    CONSTRAINT uq_centers_email_unique UNIQUE (contact_email),
    CONSTRAINT ck_centers_name_not_empty CHECK (trim(name) <> ''),
    CONSTRAINT ck_centers_address_not_empty CHECK (trim(address) <> ''),
    CONSTRAINT ck_centers_tax_id_format CHECK (tax_id ~ '^[0-9]{10,13}$'),
    CONSTRAINT ck_centers_phone_format CHECK (contact_phone IS NULL OR contact_phone ~ '^[+0-9 ()-]+$'),
    CONSTRAINT ck_centers_email_format CHECK (contact_email IS NULL OR contact_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'),
    CONSTRAINT ck_centers_created_before_updated CHECK (created_at <= updated_at)
);

-- ============================================
-- INDEXES FOR CENTERS TABLE
-- SCOPE: Tenant lookup and administrative queries
-- ============================================
CREATE INDEX idx_centers_name_lower ON centers(lower(name));
CREATE INDEX idx_centers_tax_id ON centers(tax_id);
CREATE INDEX idx_centers_active ON centers(is_active) WHERE is_active = true;
CREATE INDEX idx_centers_created_at ON centers(created_at);

-- ============================================
-- TABLE: courses
-- SCOPE: Educational course management with schedule conflict prevention
-- ============================================
CREATE TABLE courses (
    course_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    teacher_id UUID NOT NULL,
    max_students INT NOT NULL DEFAULT 30,
    center_id UUID NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_courses PRIMARY KEY (course_id),
    CONSTRAINT uq_courses_code_center UNIQUE (course_code, center_id),
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_courses_center FOREIGN KEY (center_id) REFERENCES centers(center_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_courses_date_range CHECK (end_date >= start_date),
    CONSTRAINT ck_courses_max_students_positive CHECK (max_students > 0),
    CONSTRAINT ck_courses_title_not_empty CHECK (trim(title) <> ''),
    CONSTRAINT ck_courses_status_valid CHECK (status IN ('PLANNING','ACTIVE','COMPLETED','CANCELLED')),
    CONSTRAINT ck_courses_created_before_updated CHECK (created_at <= updated_at)
);

-- ============================================
-- INDEXES FOR COURSES TABLE
-- SCOPE: Schedule queries and teacher/course lookups
-- ============================================
CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_courses_center_id ON courses(center_id);
CREATE INDEX idx_courses_dates ON courses(start_date, end_date);
CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_courses_created_at ON courses(created_at);
CREATE INDEX idx_courses_code_center ON courses(course_code, center_id);

-- ============================================
-- TABLE: enrollments
-- SCOPE: Student-course enrollment management with capacity control
-- ============================================
CREATE TABLE enrollments (
    enrollment_id UUID NOT NULL,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    enrollment_date TIMESTAMP NOT NULL DEFAULT now(),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    grade DECIMAL(5,2) NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_enrollments PRIMARY KEY (enrollment_id),
    CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_enrollments_status_valid CHECK (status IN ('ACTIVE','COMPLETED','DROPPED','INACTIVE')),
    CONSTRAINT ck_enrollments_grade_range CHECK (grade IS NULL OR (grade >= 0 AND grade <= 100)),
    CONSTRAINT ck_enrollments_created_before_updated CHECK (created_at <= updated_at)
);

-- ============================================
-- INDEXES FOR ENROLLMENTS TABLE
-- SCOPE: Student progress and course capacity queries
-- ============================================
CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);
CREATE INDEX idx_enrollments_status ON enrollments(status);
CREATE INDEX idx_enrollments_enrollment_date ON enrollments(enrollment_date);
CREATE INDEX idx_enrollments_active_student ON enrollments(student_id, status) WHERE status = 'ACTIVE';

-- ============================================
-- TABLE: attendance
-- SCOPE: QR-based attendance tracking with idempotency guarantees
-- ============================================
CREATE TABLE attendance (
    attendance_id UUID NOT NULL,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    location VARCHAR(255) NULL,
    qr_payload_hash VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),
    CONSTRAINT uq_attendance_student_course_date UNIQUE (student_id, course_id, attendance_date),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_attendance_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_attendance_status_valid CHECK (status IN ('PRESENT','ABSENT','LATE','EXCUSED')),
    CONSTRAINT ck_attendance_created_before_updated CHECK (created_at <= updated_at)
);

-- ============================================
-- INDEXES FOR ATTENDANCE TABLE
-- SCOPE: Daily reporting and student attendance analytics
-- ============================================
CREATE INDEX idx_attendance_student_date ON attendance(student_id, attendance_date);
CREATE INDEX idx_attendance_course_date ON attendance(course_id, attendance_date);
CREATE INDEX idx_attendance_status ON attendance(status);
CREATE INDEX idx_attendance_attendance_date ON attendance(attendance_date);
CREATE INDEX idx_attendance_qr_hash ON attendance(qr_payload_hash) WHERE qr_payload_hash IS NOT NULL;

-- ============================================
-- TABLE: student_cards
-- SCOPE: Membership card lifecycle management
-- ============================================
CREATE TABLE student_cards (
    card_id UUID NOT NULL,
    student_id UUID NOT NULL,
    issue_date DATE NOT NULL,
    validity_days INT NOT NULL,
    remaining_days INT NOT NULL,
    end_date DATE NOT NULL,
    card_number VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_student_cards PRIMARY KEY (card_id),
    CONSTRAINT uq_student_cards_student UNIQUE (student_id),
    CONSTRAINT uq_student_cards_number UNIQUE (card_number),
    CONSTRAINT fk_student_cards_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_student_cards_validity_positive CHECK (validity_days > 0),
    CONSTRAINT ck_student_cards_remaining_non_negative CHECK (remaining_days >= 0),
    CONSTRAINT ck_student_cards_end_date_future CHECK (end_date >= issue_date),
    CONSTRAINT ck_student_cards_status_valid CHECK (status IN ('ACTIVE','EXPIRED','SUSPENDED','REVOKED')),
    CONSTRAINT ck_student_cards_created_before_updated CHECK (created_at <= updated_at)
);

-- ============================================
-- INDEXES FOR STUDENT_CARDS TABLE
-- SCOPE: Card validation and student lookup
-- ============================================
CREATE INDEX idx_student_cards_student_id ON student_cards(student_id);
CREATE INDEX idx_student_cards_end_date ON student_cards(end_date);
CREATE INDEX idx_student_cards_status ON student_cards(status);
CREATE INDEX idx_student_cards_card_number ON student_cards(card_number);

-- ============================================
-- TABLE: notifications
-- SCOPE: Multi-channel notification dispatch system
-- ============================================
CREATE TABLE notifications (
    notification_id UUID NOT NULL,
    user_id UUID NULL,
    group_zalo VARCHAR(50) NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    delivered BOOLEAN NOT NULL DEFAULT false,
    delivery_channel VARCHAR(20) NOT NULL DEFAULT 'PUSH',
    priority VARCHAR(10) NOT NULL DEFAULT 'NORMAL',
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    last_attempt_at TIMESTAMP NULL,
    error_message TEXT NULL,
    payload JSONB NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_notifications PRIMARY KEY (notification_id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT ck_notifications_target_check CHECK ((user_id IS NOT NULL) OR (group_zalo IS NOT NULL)),
    CONSTRAINT ck_notifications_channel_valid CHECK (delivery_channel IN ('PUSH','ZALO','EMAIL','IN_APP')),
    CONSTRAINT ck_notifications_priority_valid CHECK (priority IN ('LOW','NORMAL','HIGH','URGENT')),
    CONSTRAINT ck_notifications_created_before_updated CHECK (created_at <= updated_at)
);

-- ============================================
-- INDEXES FOR NOTIFICATIONS TABLE
-- SCOPE: Notification delivery and retry management
-- ============================================
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_group_zalo ON notifications(group_zalo);
CREATE INDEX idx_notifications_sent_at ON notifications(sent_at);
CREATE INDEX idx_notifications_status ON notifications(delivered);
CREATE INDEX idx_notifications_retry ON notifications(retry_count, last_attempt_at) WHERE delivered = false;

-- ============================================
-- TABLE: promotions
-- SCOPE: Marketing promotion management
-- ============================================
CREATE TABLE promotions (
    promo_id UUID NOT NULL,
    center_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    discount_percent SMALLINT NOT NULL,
    start_date DATE,
    end_date DATE,
    is_perpetual BOOLEAN NOT NULL DEFAULT false,
    max_uses INT NULL,
    used_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_promotions PRIMARY KEY (promo_id),
    CONSTRAINT uq_promotions_code_center UNIQUE (code, center_id),
    CONSTRAINT fk_promotions_center FOREIGN KEY (center_id) REFERENCES centers(center_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_promotions_creator FOREIGN KEY (created_by) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_promotions_discount_range CHECK (discount_percent BETWEEN 1 AND 100),
    CONSTRAINT ck_promotions_date_logic CHECK (
        (is_perpetual = true) OR 
        (is_perpetual = false AND start_date IS NOT NULL AND end_date IS NOT NULL AND end_date >= start_date)
    ),
    CONSTRAINT ck_promotions_status_valid CHECK (status IN ('DRAFT','ACTIVE','INACTIVE','EXPIRED')),
    CONSTRAINT ck_promotions_usage_counts CHECK (used_count <= COALESCE(max_uses, 1000000)),
    CONSTRAINT ck_promotions_created_before_updated CHECK (created_at <= updated_at)
);

-- ============================================
-- INDEXES FOR PROMOTIONS TABLE
-- SCOPE: Promotion lookup and usage tracking
-- ============================================
CREATE INDEX idx_promotions_center_id ON promotions(center_id);
CREATE INDEX idx_promotions_status ON promotions(status);
CREATE INDEX idx_promotions_dates ON promotions(start_date, end_date);
CREATE INDEX idx_promotions_code_center ON promotions(code, center_id);

-- ============================================
-- TABLE: announcements
-- SCOPE: System-wide announcement management
-- ============================================
CREATE TABLE announcements (
    announcement_id UUID NOT NULL,
    center_id UUID NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    start_date DATE,
    expiry_date DATE,
    target_audience VARCHAR(20) NOT NULL DEFAULT 'ALL',
    published_by UUID NOT NULL,
    published_at TIMESTAMP NOT NULL DEFAULT now(),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_announcements PRIMARY KEY (announcement_id),
    CONSTRAINT fk_announcements_center FOREIGN KEY (center_id) REFERENCES centers(center_id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_announcements_publisher FOREIGN KEY (published_by) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_announcements_date_range CHECK (
        (expiry_date IS NULL OR start_date IS NULL) OR (expiry_date >= start_date)
    ),
    CONSTRAINT ck_announcements_target_audience_valid CHECK (target_audience IN ('ALL','STUDENT','TEACHER','ADMIN','CENTER_ADMIN')),
    CONSTRAINT ck_announcements_title_not_empty CHECK (trim(title) <> ''),
    CONSTRAINT ck_announcements_content_not_empty CHECK (trim(content) <> ''),
    CONSTRAINT ck_announcements_created_before_updated CHECK (created_at <= updated_at)
);

-- ============================================
-- INDEXES FOR ANNOUNCEMENTS TABLE
-- SCOPE: Announcement delivery and audience targeting
-- ============================================
CREATE INDEX idx_announcements_center_id ON announcements(center_id);
CREATE INDEX idx_announcements_active_expiry ON announcements(is_active, expiry_date);
CREATE INDEX idx_announcements_target_audience ON announcements(target_audience);
CREATE INDEX idx_announcements_published_at ON announcements(published_at);

-- ============================================
-- TABLE: system_settings
-- SCOPE: Global application configuration
-- ============================================
CREATE TABLE system_settings (
    setting_key VARCHAR(50) NOT NULL,
    setting_value TEXT NOT NULL,
    setting_type VARCHAR(20) NOT NULL DEFAULT 'STRING',
    description VARCHAR(200),
    is_sensitive BOOLEAN NOT NULL DEFAULT false,
    center_id UUID NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_system_settings PRIMARY KEY (setting_key),
    CONSTRAINT fk_system_settings_center FOREIGN KEY (center_id) REFERENCES centers(center_id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT ck_system_settings_key_format CHECK (setting_key ~ '^[a-z][a-z_]*$'),
    CONSTRAINT ck_system_settings_type_valid CHECK (setting_type IN ('STRING','NUMBER','BOOLEAN','JSON','PASSWORD')),
    CONSTRAINT ck_system_settings_created_before_updated CHECK (created_at <= updated_at)
);

-- ============================================
-- INDEXES FOR SYSTEM_SETTINGS TABLE
-- SCOPE: Configuration lookup and tenant isolation
-- ============================================
CREATE INDEX idx_system_settings_center_id ON system_settings(center_id);
CREATE INDEX idx_system_settings_type ON system_settings(setting_type);

-- ============================================
-- TABLE: audit_logs
-- SCOPE: Comprehensive audit trail for compliance and security
-- ============================================
CREATE TABLE audit_logs (
    log_id UUID NOT NULL,
    user_id UUID NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id UUID NULL,
    old_values JSONB NULL,
    new_values JSONB NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    session_id VARCHAR(100) NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    center_id UUID NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_audit_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_audit_logs_center FOREIGN KEY (center_id) REFERENCES centers(center_id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT ck_audit_logs_action_not_empty CHECK (trim(action) <> ''),
    CONSTRAINT ck_audit_logs_resource_type_not_empty CHECK (trim(resource_type) <> ''),
    CONSTRAINT ck_audit_logs_severity_valid CHECK (severity IN ('DEBUG','INFO','WARN','ERROR','CRITICAL')),
    CONSTRAINT ck_audit_logs_created_before_timestamp CHECK (created_at <= timestamp)
);

-- ============================================
-- INDEXES FOR AUDIT_LOGS TABLE
-- SCOPE: Compliance reporting and security investigation
-- ============================================
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_center_id ON audit_logs(center_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_logs_severity ON audit_logs(severity);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);

-- ============================================
-- TABLE: device_tokens
-- SCOPE: Push notification device registration
-- ============================================
CREATE TABLE device_tokens (
    token_id UUID NOT NULL,
    user_id UUID NOT NULL,
    token_value VARCHAR(255) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    app_version VARCHAR(30) NULL,
    device_model VARCHAR(100) NULL,
    os_version VARCHAR(50) NULL,
    fcm_token VARCHAR(255) NULL,
    apns_token VARCHAR(255) NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_used_at TIMESTAMP NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_device_tokens PRIMARY KEY (token_id),
    CONSTRAINT uq_device_tokens_token_value UNIQUE (token_value),
    CONSTRAINT fk_device_tokens_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT ck_device_tokens_platform_valid CHECK (platform IN ('IOS','ANDROID','WEB')),
    CONSTRAINT ck_device_tokens_token_not_empty CHECK (trim(token_value) <> '')
);

-- ============================================
-- INDEXES FOR DEVICE_TOKENS TABLE
-- SCOPE: Push notification delivery and device management
-- ============================================
CREATE INDEX idx_device_tokens_user_id ON device_tokens(user_id);
CREATE INDEX idx_device_tokens_active ON device_tokens(is_active) WHERE is_active = true;
CREATE INDEX idx_device_tokens_platform ON device_tokens(platform);

-- ============================================
-- TABLE: course_teacher_mapping
-- SCOPE: Many-to-many relationship between courses and teachers
-- ============================================
CREATE TABLE course_teacher_mapping (
    mapping_id UUID NOT NULL,
    course_id UUID NOT NULL,
    teacher_id UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT now(),
    assigned_by UUID NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT pk_course_teacher_mapping PRIMARY KEY (mapping_id),
    CONSTRAINT uq_course_teacher_unique UNIQUE (course_id, teacher_id),
    CONSTRAINT fk_mapping_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_mapping_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_mapping_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_mapping_assigned_at_not_future CHECK (assigned_at <= now())
);

-- ============================================
-- INDEXES FOR COURSE_TEACHER_MAPPING TABLE
-- SCOPE: Course-teacher relationship queries
-- ============================================
CREATE INDEX idx_course_teacher_course ON course_teacher_mapping(course_id);
CREATE INDEX idx_course_teacher_teacher ON course_teacher_mapping(teacher_id);
CREATE INDEX idx_course_teacher_assigned_at ON course_teacher_mapping(assigned_at);

-- ============================================
-- TABLE: card_renewal_history
-- SCOPE: Membership card renewal audit trail
-- ============================================
CREATE TABLE card_renewal_history (
    renewal_id UUID NOT NULL,
    card_id UUID NOT NULL,
    student_id UUID NOT NULL,
    renewal_days INT NOT NULL,
    previous_end_date DATE NOT NULL,
    new_end_date DATE NOT NULL,
    payment_reference VARCHAR(100) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    processed_at TIMESTAMP NOT NULL DEFAULT now(),
    processed_by UUID NOT NULL,
    CONSTRAINT pk_card_renewal_history PRIMARY KEY (renewal_id),
    CONSTRAINT fk_renewal_card FOREIGN KEY (card_id) REFERENCES student_cards(card_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_renewal_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_renewal_processed_by FOREIGN KEY (processed_by) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_renewal_days_positive CHECK (renewal_days > 0),
    CONSTRAINT ck_renewal_payment_status_valid CHECK (payment_status IN ('COMPLETED','PENDING','FAILED','REFUNDED'))
);

-- ============================================
-- INDEXES FOR CARD_RENEWAL_HISTORY TABLE
-- SCOPE: Renewal audit and financial reporting
-- ============================================
CREATE INDEX idx_card_renewal_card ON card_renewal_history(card_id);
CREATE INDEX idx_card_renewal_student ON card_renewal_history(student_id);
CREATE INDEX idx_card_renewal_processed_at ON card_renewal_history(processed_at);
CREATE INDEX idx_card_renewal_payment_status ON card_renewal_history(payment_status);

-- ============================================================================
-- PARTITIONING STRATEGY FOR HIGH-VOLUME TABLES
-- ============================================================================
-- For production deployment with >10M records, consider partitioning these tables:
-- 1. audit_logs - Partition by timestamp (monthly) for compliance reporting
-- 2. attendance - Partition by attendance_date (daily) for daily reporting
-- 3. notifications - Partition by sent_at (monthly) for delivery analytics
-- 4. card_renewal_history - Partition by processed_at (quarterly) for financial reporting

-- Example partitioning syntax for audit_logs (PostgreSQL 12+):
-- ALTER TABLE audit_logs SET TABLESPACE pg_default;
-- CREATE INDEX idx_audit_logs_timestamp_partition ON audit_logs(timestamp) WHERE timestamp >= CURRENT_DATE - INTERVAL '1 year';

-- ============================================================================
-- END OF V1__INIT_USERS_AND_ROLES.SQL
-- ============================================================================