# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/resources/db/migration/V1__init_roles_and_users.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/resources/db/migration/V1__init_roles_and_users.sql` (Must map to sources/backend/ or sources/frontend/)
*   Context Module Context Reference Path: `INTEGRATION_SCOPE`

### SOURCE CODE UNDER AUDIT (VERIFICATION TARGET)
* **Target Code Component Payload For Comprehensive Review:**
<EXISTING_CODE_UNDER_AUDIT>

</EXISTING_CODE_UNDER_AUDIT>


### ❌ REAL RAW COMPILER ERROR LOGS (CRITICAL FIX TARGET)
The codebase above triggered the following compiler or runtime exceptions. You MUST analyze this stack trace or log error text to pinpoint and auto-patch the root cause:
```text
True
```
*   Operational Modality Activated: COMPILER_FIXER_MODE


### 📋 EXECUTION SUB-TASKS TO ENFORCE
['Đánh giá tệp SQL V1__init_roles_and_users.sql xác minh: (1) tất cả kiểu dữ liệu tuân thủ chuẩn ANSI SQL (không dùng ENUM); (2) provider được biểu diễn bằng VARCHAR(20) NOT NULL kết hợp CHECK (provider IN (...)); (3) ràng buộc FK giữa users.role_id và roles.role_id đúng; (4) chỉ mục idx_users_email và idx_users_role_id đủ để hỗ trợ truy vấn tần suất cao; (5) gen_random_uuid() được sử dụng đúng cho UUID PRIMARY KEY. Đồng thời đánh giá V1__init_centers.sql xác minh ràng buộc tax_id chỉ chấp nhận chuỗi số 10-13 ký tự thông qua biểu thức chính quy. Lập biên bản đánh giá với điểm tuân thủ.']

---

### ⚙️ LEAD REVIEWER/FIXER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, sub-task specifications, and potential compiler log streams provided above. Automatically evaluate the error signatures or static code anomalies to construct either a fully patched backend Java component or a frontend TypeScript/Next.js asset. 

Ensure that you read the exact Tag IDs from the `['[DAT-001]', '[DAT-002]', '[DAT-003]']` variable, merge them seamlessly with any pre-existing tags inside the source payload, and permanently inject/preserve those exact Tag IDs within the generated code's Javadoc documentation or file header comments. The final code must be completely self-contained and compiler-ready. Emit the production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
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
```

