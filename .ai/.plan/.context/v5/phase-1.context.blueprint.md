# Giai đoạn 1: <!--PHASE_NAME_START-->Thiết lập hạ tầng cơ sở và xác thực cốt lõi<!--PHASE_NAME_END-->

## 📊 Bảng kiểm soát tài liệu

| Mục | Chi tiết |
| :--- | :--- |
| **ID Bản thiết kế** | ARCH-20260818163158 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 1 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Thiết lập hạ tầng cơ sở và xác thực cốt lõi<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn này tập trung vào việc xây dựng nền tảng cơ sở của hệ thống quản lý hội viên, bao gồm khởi tạo lược đồ cơ sở dữ liệu PostgreSQL cho các thực thể người dùng, vai trò và trung tâm; triển khai toàn bộ luồng xác thực người dùng (email/mật khẩu, OAuth2 Firebase/Google/Facebook); cấp phát JWT access token (hết hạn 15 phút) và refresh token (hết hạn 7 ngày); triển khai cơ chế phân quyền RBAC với 5 vai trò được định nghĩa; xây dựng API CRUD quản lý trung tâm với kiểm tra trùng lặp mã số thuế; đảm bảo tất cả thành phần tuân thủ các yêu cầu bảo mật cốt lõi, bao gồm biện pháp chống OWASP Top 10, mã hóa dữ liệu và kiểm soát truy cập nghiêm ngặt. Tất cả tệp được tạo trong giai đoạn này nằm trong phạm vi thư mục backend cốt lõi và tài liệu kỹ thuật, không can thiệp vào các module chức năng khác của hệ thống.<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày.Giờ** | 2026/08/18 16:31:58 |
| **Tác giả** | Kiến trúc sư hệ thống doanh nghiệp (Đặc vụ SA) |
| **Phê duyệt** | Đang chờ xem xét quản trị kỹ thuật |

## 1. Phạm vi hoạt động và mục tiêu của giai đoạn
Giai đoạn 1 là giai đoạn khởi tạo cơ sở của toàn bộ hệ thống membership-hub, tập trung vào 4 mục tiêu chính:
1. Khởi tạo schema cơ sở dữ liệu PostgreSQL cho 3 thực thể cốt lõi: vai trò (roles), người dùng (users) và trung tâm (centers), bao gồm tất cả ràng buộc khóa ngoại, chỉ mục tối ưu truy vấn và ràng buộc CHECK đảm bảo tính toàn vẹn dữ liệu.
2. Triển khai toàn bộ luồng xác thực người dùng: đăng ký/đăng nhập email/mật khẩu, xác thực OAuth2 với Firebase/Google/Facebook, cấp phát JWT access token (hết hạn 15 phút) và refresh token (hết hạn 7 ngày), tích hợp Firebase Admin SDK để xác thực thông tin người dùng từ nhà cung cấp OAuth2.
3. Triển khai cơ chế phân quyền RBAC với 5 vai trò được định nghĩa (System Admin, Center Admin, Manager, Teacher, Student), đảm bảo quyền truy cập được cách ly theo trung tâm, áp dụng kiểm tra quyền ở tầng API cho tất cả endpoint.
4. Xây dựng API CRUD quản lý trung tâm cho System Admin, bao gồm kiểm tra trùng lặp mã số thuế, phân quyền quản trị trung tâm cho người dùng, đảm bảo tất cả thao tác đều tuân thủ chính sách bảo mật và kiểm soát truy cập.

## 2. Phạm vi kỹ thuật được phép và ranh giới thư mục
Tất cả tệp và đường dẫn được tạo/xử lý trong giai đoạn này phải nằm trong các ranh giới thư mục sau, tuân thủ cấu trúc phân lớp doanh nghiệp:
### Thư mục backend cốt lõi
- `./sources/backend/migrations/`: Lưu trữ script migration Flyway cho cơ sở dữ liệu
- `./sources/backend/auth-service/`: Dịch vụ xác thực và quản lý token
- `./sources/backend/user-service/`: Dịch vụ quản lý người dùng và vai trò
- `./sources/backend/center-service/`: Dịch vụ quản lý trung tâm
### Thư mục tài liệu
- `./sources/docs/`: Lưu trữ tất cả tài liệu kỹ thuật, đặc tả API, từ điển dữ liệu
### Danh sách tệp cụ thể được phép tạo
1. `./sources/backend/migrations/V1__init_user_center_schema.sql` [DAT-001], [DAT-003]
2. `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/AuthResource.java` [REQ-001], [REQ-002], [ARC-006]
3. `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/AuthService.java` [REQ-001], [REQ-002]
4. `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/TokenService.java` [REQ-001], [ARC-006]
5. `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/OAuth2Service.java` [REQ-002], [EXC-004]
6. `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/RbacFilter.java` [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
7. `./sources/backend/user-service/src/main/java/org/nlh4j/membership_hub/user/UserResource.java` [REQ-003], [REQ-004], [ARC-001]
8. `./sources/backend/user-service/src/main/java/org/nlh4j/membership_hub/user/UserService.java` [REQ-003], [ARC-001]
9. `./sources/backend/user-service/src/main/java/org/nlh4j/membership_hub/user/RoleService.java` [REQ-003], [ARC-001]
10. `./sources/backend/center-service/src/main/java/org/nlh4j/membership_hub/center/CenterResource.java` [REQ-004], [REQ-005], [REQ-006], [ARC-002]
11. `./sources/backend/center-service/src/main/java/org/nlh4j/membership_hub/center/CenterService.java` [REQ-005], [ARC-002]
12. `./sources/backend/center-service/src/main/java/org/nlh4j/membership_hub/center/CenterAdminService.java` [REQ-006], [ARC-002]
13. `./sources/docs/auth-api-spec.md` [REQ-001], [REQ-002], [ARC-006]
14. `./sources/docs/rbac-policy.md` [REQ-003], [ARC-001], [ARC-002]
15. `./sources/docs/center-management-spec.md` [REQ-004], [REQ-005], [REQ-006]
### Đường dẫn endpoint API được phép triển khai
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/oauth2/{provider}`
- `POST /api/v1/admin/users/{userId}/role`
- `GET /api/v1/centers`
- `GET /api/v1/centers/{centerId}`
- `POST /api/v1/admin/centers`
- `PUT /api/v1/admin/centers/{centerId}`
- `DELETE /api/v1/admin/centers/{centerId}`
- `POST /api/v1/admin/centers/{centerId}/admins`
- `DELETE /api/v1/admin/centers/{centerId}/admins/{userId}`

## 3. Chỉ thị chức năng cho đại lý phụ chuyên biệt
*   **Coder**: Đóng vai trò là Nhà phát triển ứng dụng cấp Cao/Chính. Chịu trách nhiệm triển khai mã nguồn ứng dụng thuần túy trên cả lớp dịch vụ backend và ứng dụng khách frontend/di động. Bị cấm viết bộ kiểm thử hoặc manifest hạ tầng.
*   **Tester**: Đóng vai trò là Kiểm soát chất lượng (QC/QA) cấp Lead/Chính. Chuyên về kỹ thuật bộ kiểm thử, xác thực và cổng chất lượng. Chịu trách nhiệm tạo các bộ kiểm thử JUnit, kiểm thử tích hợp, tự động hóa kiểm thử E2E và kịch bản xác thực hiệu suất. Bị cấm sửa mã nguồn ứng dụng sản xuất. Nếu mục tiêu nhiệm vụ liên quan đến phạm vi kiểm thử tích hợp hoặc end-to-end mà không có tệp mã ứng dụng cụ thể nào có thể cô lập, bạn PHẢI xuất ra literal token `INTEGRATION_SCOPE` là tham số đầu tiên của cặp dấu chấm phẩy (ví dụ: `INTEGRATION_SCOPE;./sources/backend/tests/integration/WorkflowTest.java`).
*   **Doc**: Hoạt động như là Nhà viết kỹ thuật chính và Kiến trúc sư hệ thống doanh nghiệp. Chuyên về biên soạn tài liệu đặc tả kỹ thuật toàn diện, tài liệu tham chiếu schema, bản vẽ hệ thống và danh mục kiến trúc doanh nghiệp phù hợp với các lớp ngăn xếp kiến trúc đang hoạt động của dự án. Mỗi tệp tài liệu kỹ thuật được tạo PHẢI được liệt kê là thực thể đường dẫn tệp cụ thể có phần mở rộng `.md` và nằm nghiêm ngặt trong bố cục lưu trữ tập trung: `./sources/docs/`.
*   **Reviewer**: Chịu trách nhiệm xác minh trình biên dịch, cổng phân tích tĩnh và vá lỗi phòng thủ. Chuyên về kiểm toán chất lượng mã nguồn, giải quyết lỗi biên dịch, sửa lỗ hổng bảo mật OWASP và giải quyết các chặn cổng chất lượng SonarQube.
*   **Docker**: Chuyên nghiệp nghiêm ngặt về đóng gói container, kỹ thuật Dockerfile đa giai đoạn, tối ưu gói và đẩy tài sản hình ảnh ứng dụng đã xác minh lên DockerHub.
*   **GCP**: Chuyên nghiệp về tự động hóa đám mây trong Google Cloud Platform. Chịu trách nhiệm xây dựng và đẩy hình ảnh lên Google Cloud Artifact Registry (GCR), và điều phối môi trường container một cách tự nhiên trên Google Cloud Run.
*   **GKE**: Chuyên nghiệp về điều phối container sản xuất bên trong Google Kubernetes Engine. Chịu trách nhiệm xây dựng manifest triển khai Kubernetes, điều khiển định tuyến, cấu hình HPA, biểu đồ Helm và triển khai khối lượng công việc microservices vào cụm GKE đang hoạt động.

## 4. Định nghĩa hoàn thành giai đoạn (DoD)
Giai đoạn 1 được đánh giá là hoàn thành thành công khi đáp ứng tất cả các mốc định lượng sau:
1. Hoàn thành 100% các thẻ theo dõi yêu cầu được phân bổ cho Giai đoạn 1: [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [EXC-004], [DAT-001], [DAT-003], [ARC-001], [ARC-002], [ARC-006], không có thẻ nào bị bỏ sót.
2. Tất cả bộ kiểm thử đơn vị và tích hợp đạt độ bao phủ mã nguồn tối thiểu 90%, không có lỗi nghiêm trọng nào còn tồn tại sau khi rà soát.
3. Tất cả endpoint API được triển khai đầy đủ theo hợp đồng định tuyến đã định nghĩa, tuân thủ các tiêu chuẩn bảo mật OWASP Top 10 (chống SQL injection, XSS, CSRF, xác thực đầu vào nghiêm ngặt).
4. Lược đồ cơ sở dữ liệu được triển khai chính xác với tất cả ràng buộc khóa ngoại, chỉ mục và ràng buộc CHECK, đảm bảo tính toàn vẹn dữ liệu và hiệu suất truy vấn tối ưu.
5. Cơ chế RBAC hoạt động chính xác, ngăn chặn mọi truy cập trái phép, quyền truy cập được áp dụng ngay lập tức sau khi thay đổi vai trò người dùng, không có lỗ hổng bypass quyền.
6. Tất cả tài liệu kỹ thuật (đặc tả API, chính sách RBAC, đặc tả quản lý trung tâm) được hoàn thiện, rõ ràng và đồng bộ với phiên bản triển khai thực tế.

## 5. Nhật ký thực hiện kiến trúc theo ngày

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->Khởi tạo schema cơ sở dữ liệu và dịch vụ xác thực cốt lõi<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 1.1: Triển khai migration khởi tạo schema bảng người dùng, vai trò và trung tâm
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/migrations/V1__init_user_center_schema.sql`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[DAT-001], [DAT-003]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết script migration ANSI SQL chuẩn để tạo 3 bảng: `roles` (role_id PK kiểu SMALLINT, name VARCHAR 30 NOT NULL UNIQUE, description VARCHAR 200), `users` (user_id PK kiểu UUID mặc định `gen_random_uuid()`, email VARCHAR 255 NOT NULL UNIQUE, password_hash CHAR 60 NOT NULL, full_name VARCHAR 100 NOT NULL, role_id SMALLINT NOT NULL tham chiếu đến `roles(role_id)`, provider VARCHAR 20 NOT NULL mặc định 'local' với ràng buộc CHECK chỉ chấp nhận các giá trị 'local', 'firebase', 'google', 'facebook', created_at và updated_at TIMESTAMP NOT NULL mặc định CURRENT_TIMESTAMP), `centers` (center_id PK UUID mặc định `gen_random_uuid()`, name VARCHAR 100 NOT NULL, address VARCHAR 255 NOT NULL, tax_id VARCHAR 13 NOT NULL UNIQUE với ràng buộc CHECK chỉ chấp nhận 10-13 chữ số, contact_phone VARCHAR 20, contact_email VARCHAR 255 với ràng buộc CHECK đúng định dạng email). Tạo chỉ mục cho các trường thường xuyên truy vấn: `idx_users_email` trên `users(email)`, `idx_users_role_id` trên `users(role_id)`, `idx_centers_tax_id` trên `centers(tax_id)`. Đảm bảo tất cả ràng buộc khóa ngoại được định nghĩa chính xác với hành động ON DELETE CASCADE phù hợp, ngăn chặn dữ liệu rác khi xóa bản ghi cha.

<!--START_DDL_MIGRATION-->
```sql
-- Khởi tạo schema cho các bảng người dùng, vai trò và trung tâm
CREATE TABLE IF NOT EXISTS roles (
    role_id SMALLINT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash CHAR(60) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role_id SMALLINT NOT NULL REFERENCES roles(role_id),
    provider VARCHAR(20) NOT NULL DEFAULT 'local' CHECK (provider IN ('local', 'firebase', 'google', 'facebook')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS centers (
    center_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tax_id VARCHAR(13) NOT NULL UNIQUE CHECK (tax_id ~ '^[0-9]{10,13}$'),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255) CHECK (contact_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Tạo index cho các trường thường xuyên truy vấn
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_centers_tax_id ON centers(tax_id);
```
<!--END_DDL_MIGRATION-->

#### 📝 Công việc phụ 1.2: Xây dựng dịch vụ xác thực email/mật khẩu và cấp phát JWT token
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/AuthService.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-001], [ARC-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic xác thực đầu vào cho email và mật khẩu: kiểm tra định dạng email hợp lệ, kiểm tra độ mạnh mật khẩu tối thiểu 8 ký tự có chữ hoa, chữ thường, số và ký tự đặc biệt. Sử dụng thư viện BCrypt để băm mật khẩu trước khi lưu vào cơ sở dữ liệu. Triển khai logic cấp phát JWT access token có thời hạn 15 phút và refresh token có thời hạn 7 ngày, kèm cơ chế làm mới token hợp lệ. Lưu trữ refresh token đã mã hóa trong cơ sở dữ liệu để xác thực khi thực hiện yêu cầu làm mới token. Đảm bảo tất cả thao tác xử lý mật khẩu và token tuân thủ các tiêu chuẩn bảo mật OWASP Top 10, không lộ thông tin nhạy cảm trong log.

#### 📝 Công việc phụ 1.3: Xây dựng endpoint đăng ký và đăng nhập người dùng
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/AuthResource.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-001], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng endpoint `POST /api/v1/auth/register` để xử lý yêu cầu đăng ký người dùng: xác thực đầu vào đầy đủ, tạo bản ghi người dùng mới với vai trò mặc định là Student, trả về JWT access token và refresh token khi đăng ký thành công. Xây dựng endpoint `POST /api/v1/auth/login` để xử lý đăng nhập với email/mật khẩu, xác thực thông tin và cấp token tương tự. Triển khai xử lý ngoại lệ `VALIDATION_INPUT_INVALID`: nếu có trường không hợp lệ, trả về mã 400 kèm danh sách chi tiết lỗi từng trường, không tiết lộ thông tin nhạy cảm về cấu trúc cơ sở dữ liệu. Đảm bảo tất cả yêu cầu đều có kiểm tra xác thực đầu vào nghiêm ngặt, ngăn chặn tấn công injection.

#### 📝 Công việc phụ 1.4: Viết bộ kiểm thử đơn vị cho dịch vụ xác thực và endpoint đăng ký
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/AuthService.java;./sources/backend/auth-service/src/test/java/org/nlh4j/membership_hub/auth/AuthServiceTest.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-001], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử đơn vị cho `AuthService`: kiểm tra xác thực email hợp lệ/không hợp lệ, kiểm tra độ mạnh mật khẩu, kiểm tra băm mật khẩu đúng định dạng bcrypt 60 ký tự, kiểm tra cấp phát JWT token có thời hạn chính xác 15 phút cho access token và 7 ngày cho refresh token. Viết kiểm thử cho endpoint đăng ký: kiểm tra đăng ký thành công với thông tin hợp lệ, kiểm tra trả về lỗi 400 khi thiếu trường bắt buộc, kiểm tra trả về lỗi khi email đã tồn tại. Đảm bảo độ bao phủ mã ít nhất 90% cho các tệp liên quan, không có trường hợp kiểm thử nào bị bỏ sót các nhánh điều kiện quan trọng.

#### 📝 Công việc phụ 1.5: Xây dựng tài liệu đặc tả API cho luồng xác thực người dùng
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/auth-api-spec.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-001], [REQ-002], [ARC-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết tài liệu đặc tả API cho các endpoint xác thực: đăng ký, đăng nhập, làm mới token, đăng xuất. Mô tả chi tiết tham số yêu cầu, phản hồi thành công, phản hồi lỗi, mã lỗi HTTP tương ứng, yêu cầu xác thực (nếu có). Bao gồm ví dụ payload JSON cho mỗi trường hợp, ghi rõ các ràng buộc đầu vào (độ dài mật khẩu, định dạng email) và các mã lỗi có thể xảy ra (400, 401, 409). Đảm bảo tài liệu tuân thủ chuẩn Markdown của dự án, dễ đọc cho cả đội phát triển và đội vận hành.

<!--START_EXC_HANDLER>
```json
// Trình xử lý ngoại lệ cục bộ của Giai đoạn 1 [EXC-004]
{
  "exception_handlers": [
    {
      "error_code": "VALIDATION_INPUT_INVALID",
      "http_status": 400,
      "trigger_condition": "Các trường đầu vào không đạt yêu cầu kiểm tra (email không đúng định dạng, mật khẩu không đủ mạnh, thiếu trường bắt buộc)",
      "behavior": "Trả về phản hồi lỗi chi tiết liệt kê từng trường không hợp lệ, yêu cầu người dùng chỉnh sửa trước khi gửi lại, không tiết lộ thông tin nhạy cảm về cấu trúc hệ thống"
    },
    {
      "error_code": "OAUTH2_AUTH_FAILED",
      "http_status": 401,
      "trigger_condition": "Trao đổi mã xác thực OAuth2 với nhà cung cấp thất bại, hoặc thông tin người dùng không hợp lệ",
      "behavior": "Trả về thông báo lỗi xác thực thất bại, yêu cầu người dùng thử lại hoặc chọn phương thức đăng nhập khác, ghi log chi tiết lỗi cho mục đích kiểm tra"
    },
    {
      "error_code": "TAX_ID_DUPLICATE",
      "http_status": 409,
      "trigger_condition": "Mã số thuế của trung tâm mới trùng với bản ghi đã tồn tại trong hệ thống",
      "behavior": "Trả về lỗi xung đột, ngăn chặn tạo/cập nhật trung tâm, yêu cầu nhập mã số thuế khác, ghi log sự kiện cho mục đích kiểm tra"
    }
  ]
}
```
<!--END_EXC_HANDLER-->

---

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->Triển khai xác thực OAuth2 và cơ chế phân quyền RBAC<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 2.1: Tích hợp luồng xác thực OAuth2 với Firebase, Google và Facebook
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/OAuth2Service.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-002], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic xử lý xác thực OAuth2 cho 3 nhà cung cấp: Firebase, Google, Facebook. Xây dựng luồng trao đổi mã xác thực (auth code) lấy thông tin người dùng từ nhà cung cấp, kiểm tra tính hợp lệ của mã và xác thực chữ ký của mã xác thực. Nếu người dùng đã tồn tại trong hệ thống, cập nhật thông tin xác thực; nếu chưa tồn tại, tạo bản ghi người dùng mới với vai trò Student. Cấp JWT token tương tự luồng đăng nhập email/mật khẩu. Triển khai xử lý ngoại lệ khi trao đổi mã xác thực thất bại, trả về lỗi 401 với thông báo rõ ràng, không tiết lộ chi tiết kỹ thuật nhạy cảm. Đảm bảo tích hợp với Firebase Admin SDK đúng chuẩn, xác thực chữ ký của mã xác thực từ nhà cung cấp OAuth2.

#### 📝 Công việc phụ 2.2: Xây dựng endpoint quản lý vai trò người dùng (RBAC)
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membership_hub/user/RoleService.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-003], [ARC-001]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic gán/thay đổi vai trò người dùng: nhận ID người dùng và ID vai trò mới, cập nhật trường `role_id` trong bảng `users`. Áp dụng ngay quyền truy cập tương ứng với vai trò mới mà không yêu cầu người dùng đăng nhập lại. Triển khai kiểm tra quyền: chỉ System Admin mới có quyền thực hiện thao tác thay đổi vai trò, sử dụng bộ lọc RBAC đã triển khai để xác thực quyền trước khi xử lý yêu cầu.

#### 📝 Công việc phụ 2.3: Xây dựng endpoint lấy danh sách người dùng và quản lý vai trò
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membership_hub/user/UserResource.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-003], [ARC-001]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng endpoint `GET /api/v1/admin/users` để lấy danh sách người dùng với thông tin vai trò tương ứng, hỗ trợ lọc theo vai trò và tìm kiếm theo tên/email, phân trang kết quả để tối ưu hiệu suất với khối lượng dữ liệu lớn. Xây dựng endpoint `PUT /api/v1/admin/users/{userId}/role` để cập nhật vai trò người dùng, kèm kiểm tra quyền truy cập của người thực hiện thao tác, đảm bảo chỉ System Admin mới có quyền truy cập các endpoint này.

#### 📝 Công việc phụ 2.4: Viết bộ kiểm thử đơn vị cho luồng OAuth2 và RBAC
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/OAuth2Service.java;./sources/backend/auth-service/src/test/java/org/nlh4j/membership_hub/auth/OAuth2ServiceTest.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-002], [REQ-003], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử cho luồng OAuth2: kiểm tra xác thực thành công với từng nhà cung cấp (Firebase, Google, Facebook), kiểm tra tạo tài khoản mới khi người dùng OAuth2 chưa tồn tại, kiểm tra cập nhật thông tin người dùng đã tồn tại, kiểm tra xử lý lỗi khi mã xác thực không hợp lệ. Viết kiểm thử cho chức năng RBAC: kiểm tra cập nhật vai trò thành công, kiểm tra từ chối truy cập khi người dùng không có quyền thay đổi vai trò, kiểm tra quyền truy cập được áp dụng ngay sau khi thay đổi vai trò. Đảm bảo độ bao phủ mã đạt trên 90% cho các tệp liên quan.

<!--START_API_CONTRACT>
```json
// Hợp đồng API cho luồng xác thực và quản lý người dùng/vai trò
[
  {
    "endpoint": "/api/v1/auth/register",
    "method": "POST",
    "description": "Đăng ký người dùng mới với email/mật khẩu",
    "request": {
      "body": {
        "email": "string (required, định dạng email hợp lệ)",
        "password": "string (required, tối thiểu 8 ký tự, có chữ hoa, chữ thường, số, ký tự đặc biệt)",
        "fullName": "string (required, tối đa 100 ký tự)",
        "provider": "string (tùy chọn, giá trị: local, firebase, google, facebook, mặc định local)"
      }
    },
    "response": {
      "status": 201,
      "body": {
        "userId": "uuid",
        "email": "string",
        "role": "string (Student/Teacher)",
        "accessToken": "string (JWT, hết hạn 15 phút)",
        "refreshToken": "string (hết hạn 7 ngày)"
      }
    },
    "error": {
      "status": 400,
      "body": { "error": "VALIDATION_FAILED", "message": "Danh sách lỗi trường không hợp lệ" }
    }
  },
  {
    "endpoint": "/api/v1/auth/oauth2/{provider}",
    "method": "POST",
    "description": "Xác thực OAuth2 với nhà cung cấp (Firebase/Google/Facebook)",
    "request": {
      "body": {
        "authCode": "string (required, mã xác thực từ nhà cung cấp OAuth2)"
      }
    },
    "response": {
      "status": 200,
      "body": "Tương tự response đăng ký"
    },
    "error": {
      "status": 401,
      "body": { "error": "OAUTH2_AUTH_FAILED", "message": "Xác thực OAuth2 thất bại" }
    }
  },
  {
    "endpoint": "/api/v1/admin/users/{userId}/role",
    "method": "POST",
    "description": "Gán vai trò mới cho người dùng",
    "request": {
      "body": {
        "roleId": "smallint (required, ID vai trò từ bảng roles)"
      }
    },
    "response": {
      "status": 200,
      "body": { "message": "Cập nhật vai trò thành công" }
    },
    "error": {
      "status": 403,
      "body": { "error": "FORBIDDEN", "message": "Không có quyền thực hiện thao tác này" }
    }
  },
  {
    "endpoint": "/api/v1/centers",
    "method": "GET",
    "description": "Lấy danh sách tất cả trung tâm",
    "response": {
      "status": 200,
      "body": [
        {
          "centerId": "uuid",
          "name": "string",
          "address": "string",
          "taxId": "string",
          "contactPhone": "string",
          "contactEmail": "string"
        }
      ]
    }
  },
  {
    "endpoint": "/api/v1/admin/centers",
    "method": "POST",
    "description": "Tạo trung tâm mới (chỉ System Admin)",
    "request": {
      "body": {
        "name": "string (required)",
        "address": "string (required)",
        "taxId": "string (required, 10-13 chữ số)",
        "contactPhone": "string (tùy chọn)",
        "contactEmail": "string (tùy chọn, định dạng email hợp lệ)"
      }
    },
    "response": {
      "status": 201,
      "body": "Object trung tâm vừa tạo"
    },
    "error": {
      "status": 409,
      "body": { "error": "TAX_ID_CONFLICT", "message": "Mã số thuế đã tồn tại" }
    }
  },
  {
    "endpoint": "/api/v1/admin/centers/{centerId}/admins",
    "method": "POST",
    "description": "Gán quản trị viên cho trung tâm",
    "request": {
      "body": {
        "userId": "uuid (required)",
        "isAssign": "boolean (required, true để gán, false để huỷ gán)"
      }
    },
    "response": {
      "status": 200,
      "body": { "message": "Thao tác phân quyền trung tâm thành công" }
    }
  }
]
```
<!--END_API_CONTRACT-->

---

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->Triển khai quản lý trung tâm và phân quyền quản trị trung tâm<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 3.1: Xây dựng dịch vụ quản lý trung tâm (CRUD)
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membership_hub/center/CenterService.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-005], [ARC-002]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic CRUD cho bảng `centers`: tạo trung tâm mới với kiểm tra trùng lặp mã số thuế (trả về lỗi 409 nếu trùng), cập nhật thông tin trung tâm, xóa trung tâm (kiểm tra không có khóa học hoặc học viên đang hoạt động trước khi xóa), lấy thông tin chi tiết trung tâm theo ID. Đảm bảo tất cả các thao tác chỉ được thực hiện bởi System Admin, sử dụng bộ lọc RBAC để xác thực quyền trước khi xử lý bất kỳ yêu cầu nào. Tất cả các tham số đầu vào phải được kiểm tra và làm sạch để ngăn chặn tấn công SQL injection và XSS.

#### 📝 Công việc phụ 3.2: Xây dựng API CRUD quản lý trung tâm
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membership_hub/center/CenterResource.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-004], [REQ-005], [ARC-002]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng các endpoint REST cho quản lý trung tâm: `GET /api/v1/centers` (lấy danh sách tất cả trung tâm, trả về các trường name, address, taxId, contactPhone, contactEmail, hỗ trợ phân trang), `GET /api/v1/centers/{centerId}` (lấy chi tiết trung tâm), `POST /api/v1/admin/centers` (tạo trung tâm mới), `PUT /api/v1/admin/centers/{centerId}` (cập nhật trung tâm), `DELETE /api/v1/admin/centers/{centerId}` (xóa trung tâm). Áp dụng kiểm tra quyền truy cập cho tất cả các endpoint, chỉ cho phép System Admin thực hiện các thao tác tạo, sửa, xóa. Đảm bảo tất cả phản hồi lỗi đều tuân thủ định dạng chuẩn của hệ thống, không tiết lộ thông tin nhạy cảm về cấu trúc cơ sở dữ liệu.

#### 📝 Công việc phụ 3.3: Triển khai chức năng gán/huỷ gán quản trị viên trung tâm
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membership_hub/center/CenterAdminService.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-006], [ARC-002]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng endpoint `POST /api/v1/admin/centers/{centerId}/admins` để gán người dùng làm Center Admin cho trung tâm cụ thể: cập nhật vai trò của người dùng thành Center Admin, lưu thông tin trung tâm được quản lý vào hồ sơ người dùng. Xây dựng endpoint `DELETE /api/v1/admin/centers/{centerId}/admins/{userId}` để huỷ gán quyền Center Admin, đặt lại vai trò của người dùng về Student. Đảm bảo chỉ System Admin mới có quyền thực hiện các thao tác này, kiểm tra quyền ở tầng controller trước khi xử lý yêu cầu.

#### 📝 Công việc phụ 3.4: Viết bộ kiểm thử tích hợp cho API quản lý trung tâm
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `INTEGRATION_SCOPE;./sources/backend/center-service/src/test/java/org/nlh4j/membership_hub/center/CenterIntegrationTest.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-004], [REQ-005], [REQ-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử tích hợp cho API quản lý trung tâm: kiểm tra lấy danh sách trung tâm trả về đúng định dạng, kiểm tra tạo trung tâm thành công với thông tin hợp lệ, kiểm tra trả về lỗi 409 khi mã số thuế trùng lặp, kiểm tra cập nhật thông tin trung tâm thành công, kiểm tra xóa trung tâm thành công, kiểm tra gán/huỷ gán quản trị viên trung tâm hoạt động đúng. Kiểm tra rằng các thao tác bị từ chối khi người dùng không có quyền System Admin, đảm bảo độ bao phủ mã đạt trên 90%.

<!--START_API_CONTRACT>
```json
// Hợp đồng API cho dịch vụ quản lý trung tâm
[
  {
    "endpoint": "/api/v1/centers",
    "method": "GET",
    "description": "Lấy danh sách tất cả trung tâm (công khai cho người dùng đã xác thực)",
    "request": {
      "queryParams": {
        "page": "INT (tùy chọn, mặc định 1)",
        "size": "INT (tùy chọn, mặc định 20)"
      }
    },
    "response": {
      "status": 200,
      "body": [
        {
          "centerId": "uuid",
          "name": "string",
          "address": "string",
          "taxId": "string",
          "contactPhone": "string",
          "contactEmail": "string"
        }
      ]
    }
  },
  {
    "endpoint": "/api/v1/admin/centers/{centerId}",
    "method": "PUT",
    "description": "Cập nhật thông tin trung tâm (chỉ System Admin)",
    "request": {
      "body": {
        "name": "string (required)",
        "address": "string (required)",
        "contactPhone": "string (tùy chọn)",
        "contactEmail": "string (tùy chọn)"
      }
    },
    "response": {
      "status": 200,
      "body": "Object trung tâm đã cập nhật"
    },
    "error": {
      "status": 409,
      "body": { "error": "TAX_ID_CONFLICT", "message": "Mã số thuế đã tồn tại" }
    }
  },
  {
    "endpoint": "/api/v1/admin/centers/{centerId}/admins/{userId}",
    "method": "DELETE",
    "description": "Huỷ gán quyền Center Admin cho người dùng",
    "response": {
      "status": 200,
      "body": { "message": "Thao tác phân quyền trung tâm thành công" }
    },
    "error": {
      "status": 403,
      "body": { "error": "FORBIDDEN", "message": "Không có quyền thực hiện thao tác này" }
    }
  }
]
```
<!--END_API_CONTRACT-->

---

### 🌤️ NGÀY 4: <!--DAY_HEADER_START-->Hoàn thiện phân quyền RBAC và kiểm thử toàn diện giai đoạn<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 4.1: Triển khai bộ lọc phân quyền RBAC toàn cục cho tất cả endpoint
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/RbacFilter.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng bộ lọc JAX-RS toàn cục để kiểm tra quyền truy cập của người dùng trước khi xử lý yêu cầu. Định nghĩa ma trận quyền truy cập cho từng vai trò: System Admin (toàn quyền trên tất cả trung tâm và tài nguyên), Center Admin (toàn quyền trong trung tâm của mình, không truy cập được trung tâm khác), Manager (quyền quản lý học viên, tạo thông báo, xem danh sách khóa học, không chỉnh sửa khóa học hoặc chỉ định giáo viên), Teacher (quyền xem khóa học của mình, danh sách học viên, lịch dạy, chỉ đọc), Student (quyền duyệt khóa học, đăng ký, xem thẻ hội viên). Áp dụng bộ lọc cho tất cả các endpoint, trả về lỗi 403 Forbidden nếu người dùng không có quyền truy cập, kèm thông báo chi tiết về quyền bị thiếu. Đảm bảo bộ lọc không có lỗ hổng bypass quyền, tuân thủ đầy đủ OWASP Top 10.

#### 📝 Công việc phụ 4.2: Viết bộ kiểm thử đơn vị cho bộ lọc RBAC
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/RbacFilter.java;./sources/backend/auth-service/src/test/java/org/nlh4j/membership_hub/auth/RbacFilterTest.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử cho bộ lọc RBAC: kiểm tra truy cập thành công khi người dùng có quyền phù hợp với vai trò, kiểm tra trả về lỗi 403 khi người dùng không có quyền, kiểm tra quyền truy cập của Center Admin chỉ áp dụng cho trung tâm mà họ quản lý, kiểm tra quyền của Manager không cho phép chỉnh sửa khóa học. Đảm bảo độ bao phủ mã 100% cho bộ lọc RBAC, bao gồm tất cả các nhánh điều kiện và trường hợp ngoại lệ.

#### 📝 Công việc phụ 4.3: Rà soát mã nguồn và sửa lỗi cho các thành phần giai đoạn 1
##### Đại lý phụ được phân công: [Reviewer]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/AuthResource.java;./sources/backend/user-service/src/main/java/org/nlh4j/membership_hub/user/UserResource.java;./sources/backend/center-service/src/main/java/org/nlh4j/membership_hub/center/CenterResource.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [EXC-004], [ARC-001], [ARC-002], [ARC-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Rà soát toàn bộ mã nguồn của các dịch vụ auth, user, center để phát hiện lỗi cú pháp, lỗi logic, điểm yếu bảo mật (ví dụ: lỗi SQL injection, thiếu kiểm tra quyền, lỗi xử lý ngoại lệ). Sửa tất cả các lỗi được phát hiện, đảm bảo mã nguồn tuân thủ tiêu chuẩn mã hóa doanh nghiệp và yêu cầu OWASP Top 10. Đảm bảo tất cả các thẻ theo dõi yêu cầu được triển khai đầy đủ, không có thẻ nào bị bỏ sót. Kiểm tra tất cả các thông báo lỗi trả về cho người dùng không tiết lộ thông tin nhạy cảm về hệ thống.

#### 📝 Công việc phụ 4.4: Hoàn thiện tài liệu kỹ thuật cho giai đoạn 1
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/rbac-policy.md;./sources/docs/center-management-spec.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [ARC-001], [ARC-002], [ARC-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Hoàn thiện tài liệu chính sách RBAC, mô tả chi tiết quyền truy cập của từng vai trò, quy trình gán/thay đổi vai trò, xử lý ngoại lệ liên quan (lỗi 403, lỗi xác thực). Hoàn thiện tài liệu đặc tả quản lý trung tâm, mô tả chi tiết các endpoint, tham số, phản hồi, xử lý lỗi (lỗi 409 trùng mã số thuế, lỗi 403 thiếu quyền). Đảm bảo tài liệu được viết rõ ràng, dễ hiểu cho đội phát triển và đội vận hành, bao gồm ví dụ sử dụng và hướng dẫn xử lý sự cố thường gặp.