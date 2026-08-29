-- =====================================================================
-- V1__init_roles_and_users.sql
-- Membership Hub Database Migration Script - Part 1
-- Tags: [DAT-001], [DAT-002], [DAT-003]
-- Description: Khởi tạo bảng roles, users theo chuẩn ANSI SQL.
-- =====================================================================

CREATE TABLE IF NOT EXISTS roles (
    role_id SMALLINT PRIMARY KEY,
    name VARCHAR(30) UNIQUE NOT NULL,
    description VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash CHAR(60) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role_id SMALLINT NOT NULL,
    provider VARCHAR(20) NOT NULL DEFAULT 'local',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id),
    CONSTRAINT chk_users_provider CHECK (provider IN ('local', 'firebase', 'google', 'facebook'))
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);