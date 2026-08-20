# Day 4: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: ./sources/docs/rbac-policy.md
* **Production source codebase generated at TARGET destination**: ./sources/docs/center-management-spec.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.saas.
*   Target Documentation Destination Path: `./sources/docs/center-management-spec.md`


*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Hoàn thiện tài liệu kỹ thuật cho giai đoạn 1']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above. 

Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[REQ-002]', '[REQ-003]', '[REQ-004]', '[REQ-005]', '[REQ-006]', '[ARC-001]', '[ARC-002]', '[ARC-006]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
# Center Management Specification
**File Path:** `./sources/docs/center-management-spec.md`  
**Version:** 1.0 (Cơ sở)  
**Ngày tạo:** 2026/08/18 16:31:58  
**Tác giả:** Kiến trúc sư hệ thống doanh nghiệp (Đặc vụ SA)

## 📖 1. Tổng quan

Tài liệu này mô tả **Center Management API** và **chính sách RBAC** dành cho hệ thống **membership-hub**. Nó bao gồm các endpoint CRUD cho các trung tâm, chức năng gán/huỷ gán Center Admin, và ma trận quyền truy cập chi tiết cho các vai trò liên quan đến trung tâm.

**Phạm vi:** Tài liệu này chỉ áp dụng cho các chức năng quản lý trung tâm (tạo, đọc, cập nhật, xóa) và phân quyền quản trị viên trung tâm. Tất cả các endpoint đều được bảo vệ bởi **RBAC** và chỉ có thể được truy cập bởi **System Admin** hoặc **Center Admin** được chỉ định.

**Đối tượng mục tiêu:** Nhà phát triển backend, kỹ sư QA, chuyên gia bảo mật, và quản trị viên hệ thống.

---

## 📚 2. Tài liệu tham chiếu API

### 2.1 Danh sách endpoint

| # | HTTP Method | Full Endpoint | Request Headers | Path/Query Parameters | JSON Request Payload Schema | JSON Response Schema (Success) | JSON Response Schema (Error) | Targeted Tag IDs |
|---|-------------|---------------|-----------------|-----------------------|-----------------------------|--------------------------------|------------------------------|------------------|
| 1 | `GET` | `/api/v1/centers` | `Authorization: Bearer <JWT>` | `page: int (optional, default 1)`<br>`size: int (optional, default 20)` | *Không có* | `[{ "centerId": "UUID", "name": "string", "address": "string", "taxId": "string", "contactPhone": "string", "contactEmail": "string" }]` | `{ "error": "FORBIDDEN", "message": "Insufficient privileges" }` | `[REQ-004], [ARC-002]` |
| 2 | `GET` | `/api/v1/centers/{centerId}` | `Authorization: Bearer <JWT>` | `centerId: UUID (path)` | *Không có* | `{ "centerId": "UUID", "name": "string", "address": "string", "taxId": "string", "contactPhone": "string", "contactEmail": "string" }` | `{ "error": "NOT_FOUND", "message": "Center not found" }` | `[REQ-004], [ARC-002]` |
| 3 | `POST` | `/api/v1/admin/centers` | `Authorization: Bearer <JWT>` | *Không có* | `{ "name": "string (bắt buộc, max 100)", "address": "string (bắt buộc)", "taxId": "string (bắt buộc, định dạng 10-13 chữ số)", "contactPhone": "string (optional)", "contactEmail": "string (optional, định dạng email)" }` | `{ "centerId": "UUID", "name": "string", "address": "string", "taxId": "string", "contactPhone": "string", "contactEmail": "string", "message": "Center created successfully" }` | `{ "error": "VALIDATION_FAILED", "message": "<list of validation errors>" }`<br>`{ "error": "TAX_ID_CONFLICT", "message": "Mã số thuế đã tồn tại" }` | `[REQ-005], [ARC-002]` |
| 4 | `PUT` | `/api/v1/admin/centers/{centerId}` | `Authorization: Bearer <JWT>` | `centerId: UUID (path)` | `{ "name": "string (optional)", "address": "string (optional)", "taxId": "string (optional, định dạng 10-13 chữ số)", "contactPhone": "string (optional)", "contactEmail": "string (optional, định dạng email)" }` | `{ "centerId": "UUID", "message": "Center updated successfully" }` | `{ "error": "NOT_FOUND", "message": "Center not found" }`<br>`{ "error": "TAX_ID_CONFLICT", "message": "Mã số thuế đã tồn tại" }` | `[REQ-005], [ARC-002]` |
| 5 | `DELETE` | `/api/v1/admin/centers/{centerId}` | `Authorization: Bearer <JWT>` | `centerId: UUID (path)` | *Không có* | `{ "centerId": "UUID", "message": "Center deleted successfully" }` | `{ "error": "NOT_FOUND", "message": "Center not found" }`<br>`{ "error": "CONFLICT", "message": "Trung tâm vẫn còn khóa học hoặc học viên đang hoạt động" }` | `[REQ-005], [ARC-002]` |
| 6 | `POST` | `/api/v1/admin/centers/{centerId}/admins` | `Authorization: Bearer <JWT>` | `centerId: UUID (path)` | `{ "userId": "UUID (bắt buộc)" }` | `{ "centerId": "UUID", "userId": "UUID", "message": "User assigned as Center Admin successfully" }` | `{ "error": "NOT_FOUND", "message": "Center or User not found" }`<br>`{ "error": "FORBIDDEN", "message": "Chỉ System Admin mới có thể gán quyền Center Admin" }` | `[REQ-006], [ARC-002]` |
| 7 | `DELETE` | `/api/v1/admin/centers/{centerId}/admins/{userId}` | `Authorization: Bearer <JWT>` | `centerId: UUID (path)`<br>`userId: UUID (path)` | *Không có* | `{ "centerId": "UUID", "userId": "UUID", "message": "Center Admin quyền được gỡ bỏ thành công" }` | `{ "error": "NOT_FOUND", "message": "Assignment không tồn tại" }`<br>`{ "error": "FORBIDDEN", "message": "Chỉ System Admin mới có thể huỷ gán quyền Center Admin" }` | `[REQ-006], [ARC-002]` |

> **Ghi chú:**
> - Tất cả các endpoint yêu cầu **JWT token hợp lệ** trong header `Authorization`.
> - Các endpoint `POST`, `PUT`, `DELETE` yêu cầu người dùng có vai trò **System Admin**.
> - Các endpoint `GET` có thể được truy cập bởi **System Admin** và **Center Admin** (chỉ có thể truy cập trung tâm của họ).

### 2.2 Ma trận tham chiếu API

| Endpoint | Yêu cầu chính | Kiến trúc | Ghi chú |
|----------|----------------|----------|-------|
| `GET /api/v1/centers` | `[REQ-004]` – Lấy danh sách trung tâm | `[ARC-002]` – Quản lý trung tâm | Hỗ trợ phân trang |
| `GET /api/v1/centers/{centerId}` | `[REQ-004]` – Lấy chi tiết trung tâm | `[ARC-002]` – Quản lý trung tâm | |
| `POST /api/v1/admin/centers` | `[REQ-005]` – Tạo trung tâm | `[ARC-002]` – Quản lý trung tâm | Kiểm tra trùng lặp mã số thuế |
| `PUT /api/v1/admin/centers/{centerId}` | `[REQ-005]` – Cập nhật trung tâm | `[ARC-002]` – Quản lý trung tâm | |
| `DELETE /api/v1/admin/centers/{centerId}` | `[REQ-005]` – Xóa trung tâm | `[ARC-002]` – Quản lý trung tâm | Kiểm tra ràng buộc khóa ngoại |
| `POST /api/v1/admin/centers/{centerId}/admins` | `[REQ-006]` – Gán Center Admin | `[ARC-002]` – Quản lý trung tâm | Chỉ System Admin mới có thể thực hiện |
| `DELETE /api/v1/admin/centers/{centerId}/admins/{userId}` | `[REQ-006]` – Huỷ gán Center Admin | `[ARC-002]` – Quản lý trung tâm | Chỉ System Admin mới có thể thực hiện |

---

## 🔐 3. Chính sách RBAC cho Trung tâm

### 3.1 Ma trận quyền truy cập

| Vai trò | Quyền truy cập vào `GET /api/v1/centers` | Quyền truy cập vào `GET /api/v1/centers/{centerId}` | Quyền truy cập vào `POST /api/v1/admin/centers` | Quyền truy cập vào `PUT /api/v1/admin/centers/{centerId}` | Quyền truy cập vào `DELETE /api/v1/admin/centers/{centerId}` | Quyền truy cập vào `POST /api/v1/admin/centers/{centerId}/admins` | Quyền truy cập vào `DELETE /api/v1/admin/centers/{centerId}/admins/{userId}` |
|------|----------------------------------|-----------------------------------|----------------------------------|-----------------------------------|-----------------------------------|-----------------------------------------------|---------------------------------------------------|
| **System Admin** | ✅ | ✅ (tất cả) | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Center Admin** | ✅ (chỉ trung tâm của họ) | ✅ (chỉ trung tâm của họ) | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Manager** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Teacher** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Student** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

### 3.2 Quy tắc thực thi RBAC

1. **System Admin** có toàn quyền đối với tất cả các trung tâm.
2. **Center Admin** chỉ có thể truy cập các endpoint `GET` liên quan đến trung tâm được chỉ định (`centerId`). Họ **không** có quyền thực hiện các thao tác thay đổi (POST, PUT, DELETE) hoặc gán quyền quản trị viên trung tâm.
3. **Manager**, **Teacher**, và **Student** bị từ chối truy cập tất cả các endpoint quản lý trung tâm.
4. Tất cả các endpoint đều được bảo vệ bởi **RbacFilter** (`org.nlh4j.saas.auth.RbacFilter`), thực thi ma trận trên cơ sở mỗi yêu cầu.

### 3.3 Ghi chú về triển khai

- **JWT Claims:** Token chứa `role` (ví dụ: `SYSTEM_ADMIN`, `CENTER_ADMIN`) và `centerId` (nếu vai trò là Center Admin).
- **Kiểm tra quyền truy cập:** Lớp `RbacFilter` giải mã JWT, lấy thông tin vai trò/người dùng, và so sánh với endpoint yêu cầu. Nếu không có quyền truy cập, filter trả về HTTP **403** với JSON lỗi được định nghĩa trong bảng trên.
- **Kiểm tra tính toàn vẹn dữ liệu:** Khi System Admin thực hiện thao tác DELETE, service kiểm tra các bản ghi khóa ngoại trong bảng `courses` và `enrollments`. Nếu có khóa học hoặc học viên đang hoạt động, thao tác DELETE bị từ chối với mã lỗi **409** (`[REQ-005]`).

---

## 🗄️ 4. Mô hình dữ liệu (Trung tâm)

### 4.1 Schema (Flyway Migration)

File: `./sources/backend/center-service/src/main/resources/db/migration/V1__create_centers.sql`

```sql
CREATE TABLE centers (
    center_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tax_id VARCHAR(13) NOT NULL UNIQUE CHECK (tax_id ~ '^[0-9]{10,13}$'),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255) CHECK (contact_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_centers_tax_id ON centers(tax_id);
```

### 4.2 Ràng buộc nghiệp vụ

- **tax_id:** Phải là chuỗi 10-13 chữ số duy nhất.
- **contact_email:** Phải tuân theo định dạng email chuẩn.
- **Tên trung tâm, địa chỉ:** Không được để trống (NOT NULL).

---

## 📦 5. Triển khai & Vận hành

### 5.1 Containerization (Docker)

- **Image:** `org.nlh4j.saas/center-service:1.0`
- **Base Image:** `eclipse-temurin:21-jdk-alpine`
- **Layer:** Build stage → Runtime stage (size < 200MB)
- **Environment Variables:**
  ```bash
  SPRING_PROFILES_ACTIVE=prod
  CENTER_SERVICE_JDBC_URL=jdbc:postgresql://<DB_HOST>:<DB_PORT>/membership
  CENTER_SERVICE_JDBC_USER=${DB_USER}
  CENTER_SERVICE_JDBC_PASSWORD=${DB_PASSWORD}
  ```
- **Health Checks:** Liveness (`/actuator/health`) và Readiness (`/actuator/readiness`) được expose trên port `8080`.

### 5.2 Kubernetes (GKE)

- **Namespace:** `membership-hub`
- **Deployment:** `center-service` (replica count 2, resource request 200m CPU / 256Mi memory)
- **Service:** `clusterIP` expose port `8080`
- **HPA:** Scale dựa trên CPU usage > 70% hoặc độ trễ yêu cầu > 300ms.
- **ConfigMap:** Mount `application.yml` với các giá trị cấu hình trên.

### 5.3 CI/CD Pipeline (GitHub Actions)

1. **Build:** `mvn clean package`
2. **Test:** `mvn test` (độ bao phủ >= 85%)
3. **Lint:** `sonarqube` analysis
4. **Push Docker:** `docker build -t ...` → `docker push`
5. **Deploy:** `kubectl apply -f ./sources/infra/gke/deployment.yaml`

---

## 📊 6. Giám sát & Ghi log

- **Logging:** SLF4J/Logback với JSON layout, ghi log ở mức `INFO` cho các thao tác CRUD trung tâm tại điểm vào và điểm ra.
- **Metrics:** Spring Boot Actuator (`/metrics`) expose cho Prometheus.
- **Alerting:** Cảnh báo khi tỷ lệ lỗi endpoint `POST /api/v1/admin/centers` vượt quá 1% trong 5 phút.

---

## 🔗 7. Tài liệu tham khảo & liên kết

- **[REQ-001]** – Xác thực người dùng & cấp JWT token
- **[REQ-002]** – Xác thực OAuth2 (Firebase/Google/Facebook)
- **[REQ-003]** – Quản lý vai trò người dùng (RBAC)
- **[REQ-004]** – Lấy danh sách trung tâm
- **[REQ-005]** – Tạo/Cập nhật/Xóa trung tâm
- **[REQ-006]** – Gán/huỷ gán Center Admin
- **[ARC-001]** – Tổng quan về RBAC
- **[ARC-002]** – Quản lý trung tâm
- **[ARC-006]** – Xác thực & cấp JWT token

---

## 📌 8. Phiên bản & Lịch sử

| Phiên bản | Ngày | Tác giả | Tóm tắt thay đổi |
|---------|-----|--------|----------------|
| 1.0 | 2026/08/18 | Kiến trúc sư hệ thống doanh nghiệp | Phiên bản cơ sở – Triển khai tài liệu kỹ thuật cho giai đoạn 1 |

---

*End of Document.*
```

