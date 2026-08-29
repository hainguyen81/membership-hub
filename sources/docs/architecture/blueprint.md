```markdown
# 🏛️ Kiến Trúc Hệ Thống & Sơ Đồ Quan Hệ Thực Thể (ERD) - Membership Hub

- **Mã Bản Thiết Kế:** ARCH-20260829122721
- **Tên Dự Án:** membership-hub
- **Phiên Bản:** 1.0 (Đường Cơ Sở)
- **Gói Cơ Sở (Package Base):** `org.nlh4j.membershiphub`
- **Đường Dẫn Tài Liệu:** `./sources/docs/architecture/blueprint.md`

---

## 📑 1. TỔNG QUAN VỀ SƠ ĐỒ QUAN HỆ THỰC THỂ (ERD) - PHẦN 1: ROLES, USERS, CENTERS

Tài liệu này đặc tả chi tiết cấu trúc lược đồ cơ sở dữ liệu quan hệ cho các thực thể nền tảng cốt lõi của hệ thống **Membership Hub**, bao gồm: `roles` (Vai trò hệ thống), `users` (Người dùng), và `centers` (Trung tâm đào tạo). Toàn bộ các bảng được thiết kế tuân thủ chuẩn ANSI SQL, sử dụng kiểu dữ liệu tiêu chuẩn, ràng buộc toàn vẹn nghiêm ngặt (Primary Key, Foreign Key, Check Constraints, Unique Constraints) và các chỉ mục (Indexes) nhằm tối ưu hóa hiệu suất truy vấn theo yêu cầu phi chức năng [NFR-001].

---

## 📊 2. CHI TIẾT CẤU TRÚC BẢNG DỮ LIỆU

### 2.1. Bảng: `roles` (Quản lý Phân quyền Vai trò)
Bảng `roles` lưu trữ định nghĩa các vai trò trong hệ thống RBAC 5 cấp [ARC-001, ARC-002, ARC-003, ARC-004, ARC-005].

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả Nghiệp Vụ |
| :--- | :--- | :--- | :--- |
| `role_id` | `SMALLINT` | `PRIMARY KEY` | Mã định danh vai trò (1: SystemAdmin, 2: CenterAdmin, 3: Manager, 4: Teacher, 5: Student) |
| `name` | `VARCHAR(30)` | `UNIQUE`, `NOT NULL` | Tên định danh của vai trò (Ví dụ: SystemAdmin) |
| `description` | `VARCHAR(200)` | `NULL` | Mô tả chi tiết phạm vi quyền hạn của vai trò trong hệ thống |

**Chỉ mục hỗ trợ truy vấn:**
- `PRIMARY KEY (role_id)`
- `UNIQUE INDEX idx_roles_name ON roles(name)`

---

### 2.2. Bảng: `users` (Quản lý Người dùng & Xác thực)
Bảng `users` lưu trữ thông tin định danh, thông tin xác thực và liên kết vai trò của toàn bộ người dùng trong hệ thống [REQ-001, REQ-002, ARC-006].

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả Nghiệp Vụ |
| :--- | :--- | :--- | :--- |
| `user_id` | `UUID` | `PRIMARY KEY`, `DEFAULT gen_random_uuid()` | Mã định danh duy nhất của người dùng (UUID v4) |
| `email` | `VARCHAR(255)` | `UNIQUE`, `NOT NULL` | Địa chỉ thư điện tử dùng để đăng nhập và nhận thông báo |
| `password_hash` | `CHAR(60)` | `NOT NULL` | Chuỗi mã hóa mật khẩu bằng thuật toán BCrypt (cost factor 10) |
| `full_name` | `VARCHAR(100)` | `NOT NULL` | Họ và tên đầy đủ của người dùng |
| `role_id` | `SMALLINT` | `NOT NULL`, `FOREIGN KEY` | Khóa ngoại trỏ đến bảng `roles(role_id)` xác định phân quyền |
| `provider` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'local'` | Nhà cung cấp định danh (`local`, `firebase`, `google`, `facebook`) |
| `created_at` | `TIMESTAMP` | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Thời điểm bản ghi được khởi tạo |
| `updated_at` | `TIMESTAMP` | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Thời điểm bản ghi được cập nhật gần nhất |

**Ràng buộc toàn vẹn bổ sung:**
- `CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id)`
- `CONSTRAINT chk_users_provider CHECK (provider IN ('local','firebase','google','facebook'))`

**Chỉ mục hỗ trợ truy vấn:**
- `PRIMARY KEY (user_id)`
- `UNIQUE INDEX idx_users_email ON users(email)`
- `INDEX idx_users_role_id ON users(role_id)`

---

### 2.3. Bảng: `centers` (Quản lý Trung tâm Đào tạo)
Bảng `centers` quản lý thông tin các cơ sở đào tạo, bao gồm thông tin liên lạc, mã số thuế và quản trị viên trực thuộc trung tâm [REQ-004, REQ-005, REQ-006, ARC-002].

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả Nghiệp Vụ |
| :--- | :--- | :--- | :--- |
| `center_id` | `UUID` | `PRIMARY KEY`, `DEFAULT gen_random_uuid()` | Mã định danh duy nhất của trung tâm đào tạo (UUID v4) |
| `name` | `VARCHAR(100)` | `NOT NULL` | Tên chính thức của trung tâm đào tạo |
| `address` | `VARCHAR(255)` | `NOT NULL` | Địa chỉ vật lý của trung tâm |
| `tax_id` | `VARCHAR(20)` | `UNIQUE`, `NOT NULL` | Mã số thuế của trung tâm (phải là chuỗi số từ 10 đến 13 ký tự) |
| `contact_phone` | `VARCHAR(20)` | `NULL` | Số điện thoại liên hệ chính của trung tâm |
| `contact_email` | `VARCHAR(100)` | `NULL` | Hòm thư điện tử liên hệ chính của trung tâm |
| `admin_user_id` | `UUID` | `NULL`, `FOREIGN KEY` | Khóa ngoại trỏ tới `users(user_id)` chỉ định Center Admin quản lý trung tâm |
| `created_at` | `TIMESTAMP` | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Thời điểm khởi tạo bản ghi trung tâm |
| `updated_at` | `TIMESTAMP` | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Thời điểm cập nhật bản ghi trung tâm gần nhất |

**Ràng buộc toàn vẹn bổ sung:**
- `CONSTRAINT fk_centers_admin FOREIGN KEY (admin_user_id) REFERENCES users(user_id)`
- `CONSTRAINT chk_centers_taxid CHECK (tax_id ~ '^[0-9]{10,13}$')`

**Chỉ mục hỗ trợ truy vấn:**
- `PRIMARY KEY (center_id)`
- `UNIQUE INDEX idx_centers_tax_id ON centers(tax_id)`
- `INDEX idx_centers_admin_user_id ON centers(admin_user_id)`

---

## 🗺️ 3. TRACEABILITY MATRIX REFERENCE (ĐỐI CHIẾU MÃ TRUY XUẤT)

Bảng dưới đây ánh xạ các thành phần cơ sở dữ liệu và quy tắc nghiệp vụ trong phần này trở lại các thẻ yêu cầu (Requirements) và kiến trúc (Architecture) tương ứng nhằm đảm bảo tính toàn vẹn của hệ thống:

| Mã Thẻ Định Danh (Tag ID) | Phân Loại | Mô Tả Phạm Vi Áp Dụng & Tọa Độ Thành Phần |
| :--- | :--- | :--- |
| **`[DAT-001]`** | Database | Khởi tạo schema và ràng buộc toàn vẹn cho bảng `roles` (Vai trò hệ thống) |
| **`[DAT-002]`** | Database | Khởi tạo schema, khóa ngoại và chỉ mục cho bảng `users` (Quản lý người dùng) |
| **`[DAT-003]`** | Database | Khởi tạo schema, biểu thức chính quy kiểm tra Mã số thuế và khóa ngoại cho bảng `centers` |
| **`[REQ-001]`** | Requirement | Đăng ký tài khoản người dùng qua email/mật khẩu với kiểm tra tính duy nhất |
| **`[REQ-002]`** | Requirement | Tích hợp xác thực mạng xã hội (Firebase, Google, Facebook) |
| **`[REQ-004]`** | Requirement | Thao tác danh sách và thông tin chi tiết các trung tâm đào tạo |
| **`[REQ-005]`** | Requirement | Tạo mới và cập nhật trung tâm với cơ chế kiểm soát xung đột Mã số thuế (TaxID) |
| **`[REQ-006]`** | Requirement | Chỉ định và phân quyền quản trị viên cấp trung tâm (Center Admin) |
| **`[ARC-001]`** | Architecture | Phân quyền vai trò hệ thống cấp độ 1: System Admin |
| **`[ARC-002]`** | Architecture | Phân quyền vai trò hệ thống cấp độ 2: Center Admin (cách ly dữ liệu theo trung tâm) |
| **`[ARC-003]`** | Architecture | Phân quyền vai trò hệ thống cấp độ 3: Manager |
| **`[ARC-004]`** | Architecture | Phân quyền vai trò hệ thống cấp độ 4: Teacher |
| **`[ARC-005]`** | Architecture | Phân quyền vai trò hệ thống cấp độ 5: Student |
| **`[ARC-006]`** | Architecture | Luồng xác thực OAuth2 kết hợp JWT token và refresh token |
| **`[NFR-001]`** | Non-Functional | Đảm bảo hiệu suất truy vấn cơ sở dữ liệu dưới 1 giây với hệ thống chỉ mục tối ưu |
```