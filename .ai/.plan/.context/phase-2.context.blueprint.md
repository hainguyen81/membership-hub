# Giai đoạn 2: <!--PHASE_NAME_START-->Triển khai quản lý trung tâm, khóa học và đăng ký học viên<!--PHASE_NAME_END-->

## 📊 Bảng kiểm soát tài liệu

| Mục | Chi tiết |
| :--- | :--- |
| **ID Bản thiết kế** | ARCH-20260818163158 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 2 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Triển khai quản lý trung tâm, khóa học và đăng ký học viên<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn này tập trung vào việc triển khai toàn bộ chức năng quản lý khóa học (CRUD, kiểm tra xung đột lịch trình giáo viên/địa điểm, phân công giáo viên), chức năng duyệt và đăng ký khóa học cho học viên (tự động tạo tài khoản Student nếu chưa tồn tại), tích hợp quét mã QR điểm danh có tính chất idempotent đảm bảo chỉ ghi nhận 1 bản ghi điểm danh mỗi học viên/khóa học/ngày, xử lý các ngoại lệ liên quan đến mất kết nối mạng và gửi điểm danh trùng lặp, bao phủ toàn bộ yêu cầu chức năng từ [REQ-007] đến [REQ-013] cùng các thẻ dữ liệu, ngoại lệ và kiến trúc liên quan.<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày.Giờ** | 2026/08/18 16:31:58 |
| **Tác giả** | Kiến trúc sư hệ thống doanh nghiệp (Đặc vụ SA) |
| **Phê duyệt** | Đang chờ xem xét quản trị kỹ thuật |

## 1. Phạm vi hoạt động và mục tiêu của giai đoạn
Giai đoạn 2 là giai đoạn triển khai các module nghiệp vụ cốt lõi cho quản lý đa trung tâm và đào tạo. Giai đoạn này xây dựng nền tảng dữ liệu cho 3 thực thể chính: trung tâm (centers), khóa học (courses) và ghi danh (enrollments), bao gồm các ràng buộc khóa ngoại, chỉ mục tối ưu và ràng buộc CHECK đảm bảo tính toàn vẹn. Tiếp theo, giai đoạn triển khai API REST CRUD cho quản lý trung tâm với kiểm tra trùng lặp mã số thuế và phân quyền Center Admin; API quản lý khóa học với logic phát hiện xung đột lịch trình giáo viên/phòng học và phân công giáo viên; API đăng ký khóa học cho học viên với cơ chế tự động tạo tài khoản Student nếu chưa tồn tại. Giai đoạn cũng bao gồm phát triển giao diện frontend cho danh sách khóa học, form đăng ký và trình quét QR điểm danh, cùng toàn bộ bộ kiểm thử tích hợp và end-to-end. Tất cả thành phần tuân thủ kiến trúc RBAC đã triển khai ở giai đoạn 1 và các yêu cầu bảo mật OWASP Top 10.

## 2. Phạm vi kỹ thuật được phép và ranh giới thư mục
- **Thư mục backend:** `./sources/backend/membership-hub/src/main/java/com/membershiphub/rest/`, `./sources/backend/membership-hub/src/main/java/com/membershiphub/service/`, `./sources/backend/membership-hub/src/main/resources/db/migration/`
- **Thư mục frontend:** `./sources/frontend/web/course/src/components/`
- **Thư mục tài liệu:** `./sources/docs/api/`, `./sources/docs/data-dictionary/`
- **Endpoint API được phép triển khai:**
  - `GET /api/v1/centers`, `GET /api/v1/centers/{centerId}`
  - `POST /api/v1/admin/centers`, `PUT /api/v1/admin/centers/{centerId}`, `DELETE /api/v1/admin/centers/{centerId}`
  - `POST /api/v1/admin/centers/{centerId}/admins`, `DELETE /api/v1/admin/centers/{centerId}/admins/{userId}`
  - `GET /api/v1/courses`, `POST /api/v1/courses`, `PUT /api/v1/courses/{courseId}`, `DELETE /api/v1/courses/{courseId}`
  - `POST /api/v1/courses/{courseId}/assign-teacher`, `DELETE /api/v1/courses/{courseId}/assign-teacher/{teacherId}`
  - `GET /api/v1/courses/available`, `POST /api/v1/enrollments`

## 3. Chỉ thị chức năng cho đại lý phụ chuyên biệt
*   **Coder**: Đóng vai trò là Nhà phát triển ứng dụng cấp Cao/Chính. Chịu trách nhiệm triển khai mã nguồn ứng dụng thuần túy trên lớp dịch vụ backend (REST controllers, services, repositories) và ứng dụng khách frontend (React components). Bị cấm viết bộ kiểm thử hoặc manifest hạ tầng.
*   **Tester**: Đóng vai trò là Kiểm soát chất lượng (QC/QA) cấp Lead/Chính. Chuyên về kỹ thuật bộ kiểm thử, xác thực và cổng chất lượng. Chịu trách nhiệm tạo các bộ kiểm thử tích hợp và end-to-end, kịch bản xác thực luồng chức năng. Bị cấm sửa mã nguồn ứng dụng sản xuất. Nếu phạm vi kiểm thử tích hợp hoặc end-to-end không thể cô lập thành một tệp mã ứng dụng cụ thể, phải sử dụng literal token `INTEGRATION_SCOPE` làm tham số đầu tiên của cặp dấu chấm phẩy.
*   **Doc**: Hoạt động như là Nhà viết kỹ thuật chính và Kiến trúc sư hệ thống doanh nghiệp. Chuyên về biên soạn tài liệu đặc tả kỹ thuật toàn diện, tài liệu tham chiếu schema, bản vẽ hệ thống và danh mục kiến trúc doanh nghiệp. Mỗi tệp tài liệu kỹ thuật được tạo phải có phần mở rộng `.md` và nằm nghiêm ngặt trong `./sources/docs/`.
*   **Reviewer**: Chịu trách nhiệm xác minh trình biên dịch, cổng phân tích tĩnh và vá lỗi phòng thủ. Chuyên về kiểm toán chất lượng mã nguồn, giải quyết lỗi biên dịch, sửa lỗ hổng bảo mật OWASP và đề xuất tối ưu truy vấn.

## 4. Định nghĩa hoàn thành giai đoạn (DoD)
1. Hoàn thành 100% các thẻ theo dõi yêu cầu được phân bổ cho Giai đoạn 2: [REQ-004] đến [REQ-013], [EXC-001], [EXC-002], [DAT-003] đến [DAT-006], [ARC-002], [ARC-003], [ARC-007], không có thẻ nào bị bỏ sót.
2. Tất cả bộ kiểm thử đơn vị, tích hợp và end-to-end đạt độ bao phủ mã nguồn tối thiểu 85%, không có lỗi nghiêm trọng nào còn tồn tại sau khi rà soát.
3. Tất cả endpoint API được triển khai đầy đủ theo hợp đồng định tuyến đã định nghĩa, tuân thủ các tiêu chuẩn bảo mật OWASP Top 10 (chống SQL injection, XSS, CSRF, xác thực đầu vào nghiêm ngặt).
4. Lược đồ cơ sở dữ liệu được triển khai chính xác với tất cả ràng buộc khóa ngoại, chỉ mục và ràng buộc CHECK, đảm bảo tính toàn vẹn dữ liệu và hiệu suất truy vấn tối ưu.
5. Cơ chế kiểm tra xung đột lịch trình giáo viên hoạt động chính xác, trả về lỗi 409 khi phát hiện trùng lặp.
6. Cơ chế điểm danh idempotent được triển khai đầy đủ ở backend và frontend, đảm bảo chỉ 1 bản ghi mỗi học viên/khóa học/ngày, xử lý đúng trường hợp mất kết nối và trùng lặp.
7. Tất cả tài liệu kỹ thuật (đặc tả API, từ điển dữ liệu, kiến trúc module) được hoàn thiện, rõ ràng và đồng bộ với phiên bản triển khai thực tế.

## 5. Nhật ký thực hiện kiến trúc theo ngày

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->Khởi tạo lược đồ cơ sở dữ liệu cho trung tâm, khóa học và ghi danh<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 1.1: Triển khai migration Flyway tạo bảng CENTERS, COURSES, ENROLLMENTS
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-hub/src/main/resources/db/migration/V2__create_centers_courses_enrollments.sql`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[DAT-003], [DAT-004], [DAT-005], [ARC-010], [REQ-008]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết script migration ANSI SQL chuẩn để tạo 3 bảng: `centers` (centerId PK UUID DEFAULT gen_random_uuid(), name VARCHAR 100 NOT NULL, address VARCHAR 255 NOT NULL, taxId VARCHAR 13 NOT NULL UNIQUE với ràng buộc CHECK chỉ chấp nhận 10-13 chữ số, contactPhone VARCHAR 20, contactEmail VARCHAR 255 với ràng buộc CHECK đúng định dạng email), `courses` (courseId PK UUID DEFAULT gen_random_uuid(), title VARCHAR 150 NOT NULL, description TEXT, startDate DATE NOT NULL, endDate DATE NOT NULL với ràng buộc CHECK (startDate < endDate), teacherId UUID NOT NULL tham chiếu users(userId) ON DELETE CASCADE, maxStudents INT NOT NULL DEFAULT 30), `enrollments` (enrollmentId PK UUID DEFAULT gen_random_uuid(), studentId UUID NOT NULL tham chiếu users(userId) ON DELETE CASCADE, courseId UUID NOT NULL tham chiếu courses(courseId) ON DELETE CASCADE, enrollmentDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, ràng buộc UNIQUE (studentId, courseId)). Tạo chỉ mục cho các trường thường xuyên truy vấn: idx_courses_dates trên courses(startDate, endDate), idx_enrollments_student trên enrollments(studentId), idx_enrollments_course trên enrollments(courseId). Đảm bảo tất cả ràng buộc khóa ngoại và CHECK được định nghĩa chính xác theo chuẩn PostgreSQL.

<!--START_DDL_MIGRATION-->
```sql
-- Triển khai schema cho các bảng liên quan đến trung tâm, khóa học và đăng ký trong giai đoạn 2
-- Bảng trung tâm [DAT-003]
CREATE TABLE centers (
    centerId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    taxId VARCHAR(13) NOT NULL UNIQUE CHECK (taxId ~ '^[0-9]{10,13}$'),
    contactPhone VARCHAR(20),
    contactEmail VARCHAR(255) CHECK (contactEmail ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Bảng khóa học [DAT-004]
CREATE TABLE courses (
    courseId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL,
    description TEXT,
    startDate DATE NOT NULL,
    endDate DATE NOT NULL,
    teacherId UUID NOT NULL REFERENCES users(userId) ON DELETE CASCADE,
    maxStudents INT NOT NULL DEFAULT 30,
    CONSTRAINT chk_course_dates CHECK (startDate < endDate)
);

-- Bảng đăng ký [DAT-005]
CREATE TABLE enrollments (
    enrollmentId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    studentId UUID NOT NULL REFERENCES users(userId) ON DELETE CASCADE,
    courseId UUID NOT NULL REFERENCES courses(courseId) ON DELETE CASCADE,
    enrollmentDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_enrollment_student_course UNIQUE (studentId, courseId)
);

-- Tạo index cho các truy vấn thường dùng
CREATE INDEX idx_courses_dates ON courses(startDate, endDate);
CREATE INDEX idx_enrollments_student ON enrollments(studentId);
CREATE INDEX idx_enrollments_course ON enrollments(courseId);
```
<!--END_DDL_MIGRATION-->

#### 📝 Công việc phụ 1.2: Viết bộ kiểm thử tích hợp cho lược đồ cơ sở dữ liệu
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-hub/src/test/java/com/membershiphub/integration/CourseSchemaIntegrationTest.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-008]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết bộ kiểm thử tích hợp sử dụng Testcontainers hoặc embedded PostgreSQL để xác minh lược đồ cơ sở dữ liệu vừa tạo. Kiểm tra tính toàn vẹn khóa ngoại giữa các bảng centers, courses, enrollments với users; kiểm tra ràng buộc duy nhất trên taxId của centers và cặp (studentId, courseId) của enrollments; kiểm tra ràng buộc CHECK cho startDate < endDate và định dạng taxId; kiểm tra chỉ mục được tạo đúng và truy vấn với điều kiện lọc trả về kết quả chính xác. Đảm bảo các test case bao phủ cả trường hợp dữ liệu hợp lệ và vi phạm ràng buộc.

#### 📝 Công việc phụ 1.3: Cập nhật từ điển dữ liệu cho các bảng trung tâm, khóa học và ghi danh
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/data-dictionary/centers-courses-enrollments.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[DAT-003], [DAT-004], [DAT-005]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Biên soạn tài liệu từ điển dữ liệu chi tiết cho 3 bảng: centers, courses, enrollments. Mô tả đầy đủ từng trường (tên, kiểu dữ liệu, ràng buộc, mô tả nghiệp vụ), sơ đồ ERD mô tả mối quan hệ giữa các bảng và với bảng users, các chỉ mục được tạo và mục đích sử dụng, các ràng buộc toàn vẹn dữ liệu. Tài liệu phải được định dạng Markdown chuẩn, dễ đọc cho cả đội phát triển và quản trị cơ sở dữ liệu.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->Triển khai API quản lý trung tâm và phân quyền quản trị<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 2.1: Triển khai REST API quản lý trung tâm (CRUD và phân quyền)
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-hub/src/main/java/com/membershiphub/rest/CenterResource.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-004], [REQ-005], [REQ-006], [ARC-002]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai các endpoint REST cho quản lý trung tâm: `GET /api/v1/centers` (lấy danh sách, hỗ trợ phân trang, trả về name, address, taxId, contactPhone, contactEmail), `GET /api/v1/centers/{centerId}` (chi tiết), `POST /api/v1/admin/centers` (tạo mới, kiểm tra trùng taxId trả về 409), `PUT /api/v1/admin/centers/{centerId}` (cập nhật), `DELETE /api/v1/admin/centers/{centerId}` (xóa, kiểm tra không có khóa học hoặc học viên đang hoạt động). Triển khai endpoint `POST /api/v1/admin/centers/{centerId}/admins` (gán Center Admin) và `DELETE /api/v1/admin/centers/{centerId}/admins/{userId}` (huỷ gán). Áp dụng bộ lọc RBAC cho tất cả endpoint, chỉ System Admin được phép thực hiện các thao tác tạo/sửa/xóa/phân quyền. Đảm bảo tất cả đầu vào được làm sạch, sử dụng prepared statements để ngăn SQL injection.

<!--START_API_CONTRACT>
```json
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
<!--END_API_CONTRACT>

#### 📝 Công việc phụ 2.2: Viết bộ kiểm thử đơn vị và tích hợp cho API quản lý trung tâm
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-hub/src/test/java/com/membershiphub/rest/CenterResourceTest.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-004], [REQ-005], [REQ-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết bộ kiểm thử tích hợp sử dụng Quarkus test framework và REST assured để kiểm tra toàn bộ API quản lý trung tâm. Các kịch bản bao gồm: (1) Lấy danh sách trung tâm trả về đúng định dạng và phân trang; (2) Tạo trung tâm thành công với thông tin hợp lệ; (3) Tạo trung tâm với taxId trùng lặp trả về 409; (4) Cập nhật trung tâm thành công; (5) Xóa trung tâm thành công khi không có ràng buộc; (6) Gán Center Admin thành công; (7) Huỷ gán Center Admin thành công; (8) Từ chối truy cập khi không có quyền System Admin. Đảm bảo độ bao phủ mã đạt trên 90%.

#### 📝 Công việc phụ 2.3: Viết tài liệu đặc tả API cho quản lý trung tâm
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/api/center-api.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-004], [REQ-005], [REQ-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Biên soạn tài liệu đặc tả API Markdown cho tất cả endpoint quản lý trung tâm. Mô tả chi tiết từng endpoint: phương thức HTTP, đường dẫn, tham số yêu cầu (path, query, body), schema phản hồi thành công, mã lỗi (400, 403, 409), ví dụ payload JSON. Bao gồm hướng dẫn xác thực (JWT token), phân quyền truy cập (chỉ System Admin), và xử lý lỗi trùng lặp mã số thuế. Đảm bảo tài liệu tuân thủ chuẩn OpenAPI/Markdown của dự án.

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->Triển khai API quản lý khóa học và kiểm tra xung đột lịch trình<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 3.1: Triển khai REST API quản lý khóa học và phân công giáo viên
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-hub/src/main/java/com/membershiphub/rest/CourseResource.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-007], [REQ-008], [REQ-009], [ARC-003]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai các endpoint REST cho quản lý khóa học: `GET /api/v1/courses` (lấy danh sách với thông tin giáo viên, lịch trình, sĩ số, hỗ trợ lọc theo centerId và phân trang), `POST /api/v1/courses` (tạo mới, kiểm tra xung đột lịch trình giáo viên trước khi lưu, trả về 409 nếu trùng), `PUT /api/v1/courses/{courseId}` (cập nhật, kiểm tra xung đột lịch trình khi thay đổi giáo viên hoặc thời gian), `DELETE /api/v1/courses/{courseId}` (xóa). Triển khai endpoint `POST /api/v1/courses/{courseId}/assign-teacher` (phân công giáo viên, kiểm tra xung đột lịch, gửi thông báo) và `DELETE /api/v1/courses/{courseId}/assign-teacher/{teacherId}` (huỷ phân công). Áp dụng kiểm tra quyền RBAC: System Admin và Center Admin được phép quản lý khóa học. Đảm bảo tất cả truy vấn sử dụng prepared statements, đầu vào được làm sạch để ngăn SQL injection và XSS.

<!--START_API_CONTRACT>
```json
[
  {
    "endpoint": "/api/v1/courses",
    "method": "GET",
    "description": "Lấy danh sách tất cả khóa học với thông tin giáo viên và lịch trình",
    "request": {
      "queryParams": {
        "centerId": "UUID (tùy chọn, lọc theo trung tâm)",
        "page": "INT (tùy chọn, mặc định 1)",
        "size": "INT (tùy chọn, mặc định 20)"
      }
    },
    "response": {
      "status": 200,
      "body": [
        {
          "courseId": "UUID",
          "title": "STRING",
          "description": "STRING",
          "startDate": "DATE (YYYY-MM-DD)",
          "endDate": "DATE (YYYY-MM-DD)",
          "teacherId": "UUID",
          "teacherName": "STRING",
          "maxStudents": "INT",
          "enrolledCount": "INT"
        }
      ]
    }
  },
  {
    "endpoint": "/api/v1/courses",
    "method": "POST",
    "description": "Tạo mới khóa học (chỉ System Admin/Center Admin)",
    "request": {
      "body": {
        "title": "STRING (bắt buộc, max 150 ký tự)",
        "description": "STRING (tùy chọn)",
        "startDate": "DATE (bắt buộc, YYYY-MM-DD)",
        "endDate": "DATE (bắt buộc, YYYY-MM-DD)",
        "teacherId": "UUID (bắt buộc)",
        "maxStudents": "INT (tùy chọn, mặc định 30)"
      }
    },
    "response": {
      "status": 201,
      "body": {
        "courseId": "UUID",
        "message": "Tạo khóa học thành công"
      }
    },
    "error": {
      "status": 409,
      "body": {
        "error": "CONFLICT",
        "message": "Giáo viên có lịch trình trùng lặp trong khoảng thời gian khóa học"
      }
    }
  },
  {
    "endpoint": "/api/v1/courses/{courseId}/assign-teacher",
    "method": "POST",
    "description": "Phân công giáo viên vào khóa học và gửi thông báo",
    "request": {
      "body": {
        "teacherId": "UUID (bắt buộc)"
      }
    },
    "response": {
      "status": 200,
      "body": {
        "message": "Phân công giáo viên thành công, thông báo đã được xếp hàng"
      }
    }
  }
]
```
<!--END_API_CONTRACT>

#### 📝 Công việc phụ 3.2: Viết bộ kiểm thử tích hợp cho API quản lý khóa học
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-hub/src/test/java/com/membershiphub/rest/CourseResourceTest.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-007], [REQ-008], [REQ-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết bộ kiểm thử tích hợp sử dụng Quarkus test framework và REST assured. Các kịch bản: (1) Lấy danh sách khóa học trả về đúng định dạng và phân trang; (2) Tạo khóa học thành công với thông tin hợp lệ; (3) Tạo khóa học trùng lịch giáo viên trả về lỗi 409; (4) Cập nhật khóa học thành công; (5) Xóa khóa học thành công; (6) Phân công giáo viên thành công và kiểm tra thông báo được xếp hàng; (7) Huỷ phân công giáo viên thành công; (8) Từ chối truy cập khi không có quyền. Đảm bảo độ bao phủ mã đạt trên 90%, kiểm tra cả trường hợp dữ liệu biên và lỗi.

#### 📝 Công việc phụ 3.3: Rà soát logic nghiệp vụ và tối ưu truy vấn dịch vụ khóa học
##### Đại lý phụ được phân công: [Reviewer]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-hub/src/main/java/com/membershiphub/service/CourseService.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-008]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Rà soát toàn bộ logic nghiệp vụ trong CourseService, tập trung vào cơ chế kiểm tra xung đột lịch trình giáo viên. Phát hiện và sửa các lỗ hổng bảo mật (SQL injection, thiếu kiểm tra quyền), tối ưu truy vấn kiểm tra trùng lặp lịch (sử dụng chỉ mục phù hợp, tránh full table scan), đảm bảo logic xung đột tính cả các trường hợp cạnh nhau (startDate nằm trong khoảng khóa học khác, endDate nằm trong khoảng, hoặc bao phủ toàn bộ). Đề xuất và triển khai các cải tiến về cấu trúc mã nguồn, đảm bảo tuân thủ OWASP Top 10.

### 🌤️ NGÀY 4: <!--DAY_HEADER_START-->Triển khai API ghi danh học viên và tự động tạo tài khoản<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 4.1: Triển khai REST API ghi danh học viên
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-hub/src/main/java/com/membershiphub/rest/EnrollmentResource.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-010], [REQ-011]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai endpoint `GET /api/v1/courses/available` để lấy danh sách khóa học có sẵn cho học viên, lọc ra các khóa học học viên đã đăng ký, trả về thông tin courseId, title, startDate, endDate, teacherName, maxStudents, remainingSlots. Triển khai endpoint `POST /api/v1/enrollments` để đăng ký khóa học: kiểm tra khóa học còn chỗ, kiểm tra học viên chưa đăng ký, nếu học viên chưa có tài khoản local thì tự động tạo tài khoản Student với email được cung cấp, tạo bản ghi enrollment, xếp hàng thông báo cho học viên và nhóm Zalo của trung tâm. Áp dụng kiểm tra quyền RBAC, chỉ Student mới được đăng ký. Đảm bảo tất cả thao tác có kiểm tra đầu vào nghiêm ngặt.

<!--START_API_CONTRACT>
```json
[
  {
    "endpoint": "/api/v1/courses/available",
    "method": "GET",
    "description": "Lấy danh sách khóa học có sẵn cho học viên (loại trừ khóa đã đăng ký)",
    "request": {
      "headers": {
        "Authorization": "Bearer <JWT token>"
      },
      "queryParams": {
        "page": "INT (tùy chọn)",
        "size": "INT (tùy chọn)"
      }
    },
    "response": {
      "status": 200,
      "body": [
        {
          "courseId": "UUID",
          "title": "STRING",
          "startDate": "DATE",
          "endDate": "DATE",
          "teacherName": "STRING",
          "maxStudents": "INT",
          "remainingSlots": "INT"
        }
      ]
    }
  },
  {
    "endpoint": "/api/v1/enrollments",
    "method": "POST",
    "description": "Đăng ký khóa học cho học viên, tự động tạo tài khoản nếu chưa tồn tại",
    "request": {
      "headers": {
        "Authorization": "Bearer <JWT token>"
      },
      "body": {
        "courseId": "UUID (bắt buộc)",
        "studentEmail": "STRING (tùy chọn, dùng để tạo tài khoản mới nếu học viên chưa có tài khoản)"
      }
    },
    "response": {
      "status": 201,
      "body": {
        "enrollmentId": "UUID",
        "message": "Đăng ký khóa học thành công"
      }
    }
  }
]
```
<!--END_API_CONTRACT>

#### 📝 Công việc phụ 4.2: Viết bộ kiểm thử tích hợp cho luồng ghi danh
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-hub/src/test/java/com/membershiphub/rest/EnrollmentResourceTest.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-010], [REQ-011]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết bộ kiểm thử tích hợp sử dụng Quarkus test framework và REST assured. Các kịch bản: (1) Lấy danh sách khóa học có sẵn trả về đúng danh sách đã loại trừ khóa đã đăng ký; (2) Đăng ký khóa học thành công với học viên đã có tài khoản; (3) Đăng ký khóa học thành công và tự động tạo tài khoản Student khi cung cấp email mới; (4) Từ chối đăng ký khi khóa học đã đủ sĩ số; (5) Từ chối đăng ký khi học viên đã đăng ký khóa học đó; (6) Kiểm tra thông báo được gửi đến học viên và nhóm Zalo sau khi đăng ký thành công; (7) Từ chối truy cập khi không có quyền Student. Đảm bảo độ bao phủ mã đạt trên 90%.

#### 📝 Công việc phụ 4.3: Cập nhật tài liệu API cho endpoint ghi danh
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/api/enrollment-api.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-010], [REQ-011]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Biên soạn tài liệu đặc tả API cho các endpoint ghi danh: lấy danh sách khóa học có sẵn và đăng ký khóa học. Mô tả chi tiết tham số yêu cầu (header Authorization, query params, body), phản hồi thành công, mã lỗi (400, 401, 403, 409), quy tắc tự động tạo tài khoản Student, quy tắc kiểm tra sĩ số và trùng lặp đăng ký. Bao gồm ví dụ payload JSON và hướng dẫn xử lý lỗi cho frontend.

### 🌤️ NGÀY 5: <!--DAY_HEADER_START-->Phát triển giao diện frontend, kiểm tra E2E và hoàn thiện tài liệu<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 5.1: Phát triển các thành phần frontend cho danh sách khóa học, đăng ký và quét QR
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/frontend/web/course/src/components/CourseList.tsx;./sources/frontend/web/course/src/components/CourseDetail.tsx;./sources/frontend/web/course/src/components/EnrollmentForm.tsx;./sources/frontend/web/course/src/components/QRScanner.tsx`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-007], [REQ-010], [REQ-011], [REQ-012], [REQ-013]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng 4 thành phần React/Next.js: (1) `CourseList.tsx`: hiển thị danh sách khóa học dạng lưới, bao gồm tiêu đề, lịch trình, giáo viên, sĩ số còn lại, hỗ trợ lọc và phân trang; (2) `CourseDetail.tsx`: hiển thị chi tiết khóa học, mô tả, lịch trình, nút đăng ký; (3) `EnrollmentForm.tsx`: form xác nhận đăng ký khóa học, tích hợp gọi API đăng ký, xử lý trạng thái tải và lỗi; (4) `QRScanner.tsx`: tích hợp trình quét QR (sử dụng thư viện như react-qr-reader), xử lý payload từ mã QR, gọi API điểm danh, hiển thị trạng thái điểm danh (thành công, trùng lặp, lỗi mạng). Tất cả thành phần phải responsive, tích hợp với Redux/React Query để quản lý state, xử lý đa ngôn ngữ thông qua i18n. Đảm bảo giao diện phù hợp với cả web và di động.

#### 📝 Công việc phụ 5.2: Viết bộ kiểm thử end-to-end cho luồng đăng ký và điểm danh QR
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `INTEGRATION_SCOPE;./sources/frontend/web/course/src/test/e2e/CourseEnrollmentE2ETest.java;INTEGRATION_SCOPE;./sources/frontend/web/course/src/test/e2e/AttendanceQRScanE2ETest.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-007], [REQ-010], [REQ-011], [REQ-012], [REQ-013]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết bộ kiểm thử E2E sử dụng Cypress hoặc Playwright để kiểm tra toàn bộ luồng người dùng: (1) Học viên duyệt danh sách khóa học và đăng ký thành công; (2) Học viên đăng ký khóa học đã đăng ký trước đó nhận lỗi; (3) Học viên quét QR điểm danh thành công; (4) Học viên quét QR trùng lặp nhận thông báo đã ghi nhận; (5) Học viên quét QR khi mất kết nối mạng, điểm danh được ghi nhận sau khi khôi phục kết nối; (6) Kiểm tra thông báo được gửi đến học viên sau khi đăng ký. Đảm bảo các test case chạy ổn định trên môi trường CI/CD, có cơ chế chờ và retry cho các yếu tố bất đồng bộ.

#### 📝 Công việc phụ 5.3: Rà soát toàn bộ mã nguồn giai đoạn 2 và xác nhận tuân thủ
##### Đại lý phụ được phân công: [Reviewer]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/course-service/*;./sources/backend/enrollment-service/*;./sources/backend/attendance-service/*;./sources/frontend/web/course/*`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-007], [REQ-008], [REQ-009], [REQ-010], [REQ-011], [REQ-012], [REQ-013], [EXC-001], [EXC-002]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Rà soát toàn bộ mã nguồn backend và frontend của giai đoạn 2 để đảm bảo 100% yêu cầu chức năng được triển khai đầy đủ. Kiểm tra cơ chế idempotent của điểm danh hoạt động đúng ở cả backend và frontend; xử lý ngoại lệ mất kết nối mạng và điểm danh trùng lặp đúng theo yêu cầu; kiểm tra logic xung đột lịch trình giáo viên chính xác; phát hiện và sửa lỗ hổng bảo mật (SQL injection, XSS, thiếu kiểm tra quyền); đảm bảo mã nguồn tuân thủ tiêu chuẩn mã hóa doanh nghiệp và OWASP Top 10. Ghi nhận tất cả lỗi nghiêm trọng và đề xuất giải pháp sửa chữa.

#### 📝 Công việc phụ 5.4: Tổng hợp tài liệu kiến trúc và hướng dẫn vận hành module
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/course-module-architecture.md;./sources/docs/operations-guide.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-007], [REQ-008], [REQ-009], [REQ-010], [REQ-011], [REQ-012], [REQ-013], [ARC-007]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Biên soạn tài liệu kiến trúc module khóa học, đăng ký và điểm danh, mô tả cấu trúc các service, luồng dữ liệu chính, sơ đồ tương tác giữa các thành phần. Cập nhật hướng dẫn vận hành với các kịch bản xử lý sự cố thường gặp: mất kết nối khi quét QR, xung đột lịch trình giáo viên, lỗi đăng ký khóa học đã đủ sĩ số. Đảm bảo tài liệu đầy đủ cho đội vận hành và hỗ trợ người dùng, bao gồm các bước kiểm tra và khắc phục sự cố.

<!--START_EXC_HANDLER>
```json
{
  "exception_handlers": [
    {
      "error_code": "ATTENDANCE_NETWORK_ERROR",
      "http_status": 503,
      "trigger_condition": "Mất kết nối mạng trong quá trình quét mã QR điểm danh",
      "behavior": "Ứng dụng di động lưu tạm yêu cầu điểm danh vào hàng đợi ngoại tuyến, tự động gửi lại khi kết nối mạng được khôi phục. Hệ thống backend xử lý các yêu cầu pending theo thứ tự FIFO, đảm bảo không bỏ sót bản ghi điểm danh."
    },
    {
      "error_code": "ATTENDANCE_DUPLICATE",
      "http_status": 200,
      "trigger_condition": "Gửi điểm danh trùng lặp trong cùng ngày cho cùng học viên và khóa học",
      "behavior": "Hệ thống kiểm tra ràng buộc duy nhất trên cặp (studentId, courseId, attendanceDate) ở tầng cơ sở dữ liệu. Nếu phát hiện yêu cầu trùng lặp, trả về mã trạng thái 200 với cờ status: DUPLICATE và thông báo 'Đã ghi nhận điểm danh cho buổi học này trước đó'. Không tạo bản ghi mới, không ghi log lỗi nghiêm trọng."
    }
  ]
}
```
<!--END_EXC_HANDLER>