# Giai đoạn 2: <!--PHASE_NAME_START-->Xây Dựng Module Xác Thực, Phân Quyền Người Dùng Và Quản Lý Trung Tâm<!--PHASE_NAME_END-->

## 📊 Kiểm Soát Tài Liệu

| Mục | Chi tiết |
| :--- | :--- |
| **Mã bản thiết kế** | ARCH-20260828112120 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 2 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Xây Dựng Module Xác Thực, Phân Quyền Người Dùng Và Quản Lý Trung Tâm<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn 2 tập trung hiện thực hóa hai microservices trọng yếu gồm user-service và center-service trong hệ thống Quarkus. Giai đoạn này xây dựng luồng đăng ký tài khoản cục bộ với mã hóa bcrypt, tích hợp OAuth2 cho các nhà cung cấp Firebase, Google, Facebook, thiết lập cơ chế RBAC với bảng Roles và audit log, đồng thời triển khai đầy đủ CRUD cho thực thể Center cùng với khả năng phân công Center Admin. Toàn bộ logic nghiệp vụ phải tuân thủ nguyên tắc bảo mật OWASP, xác thực đầu vào nghiêm ngặt và ghi log kiểm toán cho mọi thao tác thay đổi quyền hạn.<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Đường cơ sở) |
| **Ngày giờ** | 2026/08/28 11:21:20 |
| **Tác giả** | Kiến trúc sư hệ thống doanh nghiệp (SA Agent) |
| **Phê duyệt** | Đang chờ đánh giá quản trị kỹ thuật |

## 1. Phạm Vi Hoạt Động & Mục Tiêu Của Giai Đoạn

Giai đoạn 2 thuộc dự án membership-hub tập trung xây dựng hai microservices nền tảng gồm `user-service` và `center-service` nhằm hiện thực hóa toàn bộ luồng xác thực, phân quyền và quản lý trung tâm đa chi nhánh. Phạm vi hoạt động cốt lõi của giai đoạn này bao gồm năm trụ cột chính: (1) Triển khai endpoint POST `/api/v1/auth/register` trong user-service với mã hóa mật khẩu bcrypt cost factor 12, kiểm tra tính duy nhất email, tạo bản ghi User với vai trò mặc định `Student` và phát hành JWT access token 15 phút kèm refresh token 7 ngày; (2) Tích hợp OAuth2 cho ba nhà cung cấp Firebase, Google, Facebook thông qua authorization code grant, ánh xạ thông tin provider về bản ghi User cục bộ, tự động tạo tài khoản mới nếu email chưa tồn tại; (3) Thiết lập cơ chế RBAC với endpoint PUT `/api/v1/users/{id}/role` chỉ dành cho System Admin, bổ sung bảng `user_audit_log` ghi lại toàn bộ thay đổi quyền hạn với retention 1 năm theo [NFR-006]; (4) Triển khai center-service với endpoint GET `/api/v1/centers` phân trang, CRUD POST/PUT/DELETE với ràng buộc UNIQUE TaxID và validation regex theo chuẩn OWASP; (5) Xây dựng cơ chế phân công Center Admin thông qua bảng trung gian `center_admins` với quan hệ composite primary key, tự động chuyển đổi vai trò người dùng sang Center Admin và ghi log kiểm toán trong cùng transaction.

Mục tiêu kỹ thuật cụ thể bao gồm việc tuân thủ nghiêm ngặt quy ước đặt tên package `org.nlh4j.membershiphub.userservice` và `org.nlh4j.membershiphub.centerservice` cho toàn bộ mã nguồn Java, áp dụng nguyên tắc PreparedStatement thông qua Hibernate ORM Panache để loại bỏ tuyệt đối SQL injection, đảm bảo mọi endpoint nhạy cảm đều yêu cầu JWT hợp lệ thông qua filter chain đã thiết lập ở Giai đoạn 1, sử dụng Bean Validation với các ràng buộc `@NotNull`, `@Email`, `@Pattern` và `@Size` cho mọi DTO đầu vào, đồng thời xây dựng bộ exception mapper chuyển đổi mã lỗi nghiệp vụ sang HTTP status code chuẩn (409 cho xung đột dữ liệu, 400 cho validation, 404 cho không tìm thấy, 403 cho không đủ quyền).

## 2. Phạm Vi Kỹ Thuật Được Phép & Ranh Giới Thư Mục

Danh sách kiểm tra kỹ thuật dưới đây định nghĩa 100% các tệp vật lý được phép khởi tạo trong phạm vi giai đoạn này, mỗi mục đại diện cho một tệp cụ thể kèm Tag ID truy vết:

* `./sources/backend/user-service/pom.xml` — [ARC-000], [ARC-006]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuthController.java` — [REQ-001], [REQ-002], [ARC-006]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuthService.java` — [REQ-001], [REQ-002], [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/OAuth2Service.java` — [REQ-002], [ARC-006]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/UserService.java` — [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/RoleService.java` — [REQ-003], [NFR-006]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuditLogger.java` — [REQ-003], [NFR-006]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/JwtTokenProvider.java` — [ARC-006]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/RegisterRequest.java` — [REQ-001], [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/SocialLoginRequest.java` — [REQ-002]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/RoleUpdateRequest.java` — [REQ-003]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/EmailAlreadyExistsException.java` — [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/InvalidPasswordException.java` — [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/RoleUpdateForbiddenException.java` — [REQ-003]
* `./sources/backend/user-service/src/main/resources/application.properties` — [ARC-006]
* `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/AuthControllerTest.java` — [REQ-001], [EXC-004]
* `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/OAuth2ServiceTest.java` — [REQ-002]
* `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserServiceTest.java` — [REQ-003]
* `./sources/backend/center-service/pom.xml` — [ARC-000]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterController.java` — [REQ-004], [REQ-005], [REQ-006]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterService.java` — [REQ-004], [REQ-005], [EXC-004]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterAdminService.java` — [REQ-006]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/CenterRequest.java` — [REQ-005], [EXC-004]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/CenterAdminRequest.java` — [REQ-006]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/exception/DuplicateTaxIdException.java` — [EXC-004]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/exception/CenterNotFoundException.java` — [REQ-005]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/exception/CenterAdminAlreadyAssignedException.java` — [REQ-006]
* `./sources/backend/center-service/src/main/resources/db/migration/V2__phase2_center_admin_relationship.sql` — [REQ-006], [NFR-006]
* `./sources/backend/center-service/src/main/resources/application.properties` — [ARC-006]
* `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/CenterControllerTest.java` — [REQ-004], [REQ-005]
* `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/CenterAdminServiceTest.java` — [REQ-006]
* `./sources/docs/architecture/UserServiceArchitecture.md` — [DOC-001], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* `./sources/docs/api/UserServiceApiContract.md` — [DOC-001], [REQ-001], [REQ-002], [REQ-003]
* `./sources/docs/api/CenterServiceApiContract.md` — [DOC-001], [REQ-004], [REQ-005], [REQ-006]

* **BẮT BUỘC VỀ BỘ KHUNG NỀN TẢNG**: Khi khởi tạo blueprint vòng đời hoạt động (giới hạn cụ thể trong Giai đoạn 2 - NGÀY 1), cần phải tiêm và khai báo rõ ràng các bộ mô tả cấu trúc hạ tầng kho lưu trữ chính trước khi tạo bất kỳ thành phần mã nguồn nghiệp vụ nào. Đối với kiến trúc backend Microservices, phải thực thi định nghĩa đường dẫn bắt buộc của bộ mô tả dự án cha `./sources/backend/pom.xml` và các bộ mô tả module con riêng biệt `./sources/backend/<tên-dịch-vụ>/pom.xml`. Toàn bộ tài sản khung được tạo ra phải ánh xạ chặt chẽ tới mã theo dõi kiến trúc hệ thống `[ARC-000]`.

## 3. Chỉ Thị Chức Năng Chuyên Biệt Cho Các Sub-Agent

*   **Coder**: Đóng vai trò Nhà phát triển ứng dụng cao cấp. Chịu trách nhiệm triển khai mã nguồn nghiệp vụ thuần túy trên dịch vụ backend user-service và center-service. Bị cấm viết bộ kiểm thử hoặc bản kê khai hạ tầng.
* **Tester**: Đóng vai trò Trưởng phòng QC/QA. Chuyên về kỹ thuật bộ kiểm thử, xác nhận hợp lệ và cổng gác chất lượng. Chịu trách nhiệm tạo JUnit, kiểm thử tích hợp sử dụng Testcontainers PostgreSQL và REST Assured. Bị cấm sửa đổi mã sản phẩm. Nếu nhiệm vụ phụ liên quan đến phạm vi tích hợp, phải xuất chính xác chuỗi ký tự `INTEGRATION_SCOPE` làm tham số đầu tiên của cặp dấu chấm phẩy.
* **Doc**: Đóng vai trò Technical Writer chính và Kiến trúc sư hệ thống doanh nghiệp. Chuyên biên soạn tài liệu đặc tả kỹ thuật toàn diện, tài liệu tham chiếu API, bản thiết kế kiến trúc dịch vụ và danh mục RBAC doanh nghiệp. Mọi tệp tài liệu kỹ thuật được tạo ra phải được liệt kê dưới dạng thực thể đường dẫn tệp rõ ràng kết thúc bằng phần mở rộng `.md` và nằm hoàn toàn trong bố cục lưu trữ tập trung `./sources/docs/`.
*   **Reviewer**: Chịu trách nhiệm xác minh trình biên dịch, cổng gác phân tích tĩnh và vá lỗi phòng thủ. Chuyên về kiểm toán chất lượng mã, giải quyết lỗi biên dịch, sửa lỗ hổng bảo mật OWASP và xử lý các blocker cổng chất lượng SonarQube.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn (DoD)

Giai đoạn 2 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: (1) Endpoint POST `/api/v1/auth/register` xử lý thành công 100% test case bao gồm đăng ký hợp lệ trả về HTTP 201 kèm JWT, đăng ký trùng email trả về HTTP 409 với mã `EMAIL_ALREADY_EXISTS`, mật khẩu yếu trả về HTTP 400 với mã `INVALID_PASSWORD_FORMAT`, không đồng ý điều khoản trả về HTTP 400 với mã `TERMS_NOT_AGREED`. (2) Endpoint POST `/api/v1/auth/social` xử lý thành công authorization code từ ba provider Firebase, Google, Facebook, tự động tạo bản ghi User mới nếu email chưa tồn tại, cập nhật `provider` cho user hiện tại nếu email đã tồn tại. (3) Endpoint PUT `/api/v1/users/{id}/role` chỉ chấp nhận JWT của System Admin, từ chối các vai trò khác với HTTP 403 mã `ROLE_UPDATE_FORBIDDEN`, đồng thời ghi bản ghi audit vào bảng `user_audit_log` với `action_type='ROLE_CHANGED'` trong cùng transaction. (4) Bảng `user_audit_log` được tạo thành công với đầy đủ ràng buộc FOREIGN KEY, CHECK constraint cho `action_type` và INDEX trên `user_id`, `performed_at`. (5) Endpoint GET `/api/v1/centers` trả về danh sách phân trang với `page` và `size` query parameter, đảm bảo thời gian phản hồi dưới 200ms. (6) Endpoint POST `/api/v1/centers` kiểm tra trùng `tax_id` trả về HTTP 409 mã `DUPLICATE_TAX_ID`, validation sai định dạng email hoặc số điện thoại trả về HTTP 400. (7) Endpoint POST `/api/v1/centers/{id}/admins` cập nhật vai trò user sang `Center Admin`, tạo bản ghi trong bảng `center_admins` với `is_active=true`, ghi log audit `CENTER_ASSIGNED` trong cùng transaction. (8) 100% Tag ID của giai đoạn (gồm [REQ-001] đến [REQ-006], [ARC-001] đến [ARC-006], [EXC-004], [NFR-006], [DOC-001]) được ánh xạ đầy đủ trong mã nguồn và tài liệu. (9) Mọi mã nguồn Java tuân thủ package convention `org.nlh4j.membershiphub.userservice` và `org.nlh4j.membershiphub.centerservice`. (10) Mật khẩu được mã hóa bằng bcrypt cost factor 12, không bao giờ lưu trữ plaintext hoặc log ra console theo [NFR-003].

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->Khởi Tạo User-Service Và Triển Khai Đăng Ký/Đăng Nhập Tài Khoản Cục Bộ<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 1.1: Khởi tạo pom.xml cho user-service với đầy đủ dependency bảo mật
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/user-service/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [ARC-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Cập nhật tệp pom.xml cho module `user-service` tại đường dẫn `./sources/backend/user-service/pom.xml` kế thừa từ `./sources/backend/pom.xml` thông qua khối `<parent>` với `<groupId>org.nlh4j.membershiphub</groupId>`, `<artifactId>membershiphub-backend</artifactId>`, `<version>1.0.0-SNAPSHOT</version>`. Khai báo `<artifactId>user-service</artifactId>`. Bổ sung đầy đủ các dependency Quarkus cần thiết cho chức năng xác thực và quản lý người dùng: `quarkus-resteasy-reactive-jackson` cho REST endpoint, `quarkus-hibernate-orm-panache` cho ORM, `quarkus-jdbc-postgresql` cho kết nối database, `quarkus-flyway` cho migration, `quarkus-smallrye-jwt` và `quarkus-smallrye-jwt-build` cho phát hành và xác minh JWT, `quarkus-rest-client-reactive-jackson` cho HTTP client gọi OAuth2 provider, `quarkus-hibernate-validator` cho Bean Validation, `quarkus-arc` cho CDI. Thiết lập `<java.version>21</java.version>`, `<maven.compiler.source>21</maven.compiler.source>`, `<maven.compiler.target>21</maven.compiler.target>`. Đảm bảo tệp XML hợp lệ, biên dịch thành công thông qua `mvn -f ./sources/backend/user-service/pom.xml compile`.

#### 📝 Nhiệm vụ phụ 1.2: Triển khai AuthController và AuthService xử lý đăng ký tài khoản
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuthController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [REQ-002], [ARC-006], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp `AuthController` tại đường dẫn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuthController.java` thuộc package `org.nlh4j.membershiphub.userservice`. Class chú thích `@Path("/api/v1/auth")` sử dụng JAX-RS, đăng ký hai endpoint: `POST /register` và `POST /social`. Endpoint đăng ký nhận `RegisterRequest` được xác thực bằng `@Valid`, ủy quyền xử lý cho `AuthService.register()`. Trước khi gọi service, kiểm tra `agreeTerms` phải là `true`, nếu không ném ngoại lệ với mã `TERMS_NOT_AGREED` và HTTP 400. Trả về `Response.status(Response.Status.CREATED).entity(authResponse).build()` cho đăng ký thành công với HTTP 201, `Response.status(Response.Status.CONFLICT)` cho email trùng, `Response.status(Response.Status.BAD_REQUEST)` cho validation fail. Endpoint social nhận `SocialLoginRequest`, ủy quyền cho `OAuth2Service.handleSocialLogin()`. Đồng thời tạo lớp `AuthService` tại đường dẫn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuthService.java` thực thi logic nghiệp vụ: inject `UserRepository`, `RoleRepository`, `BCryptPasswordEncoder` (cost factor 12), `JwtTokenProvider`. Phương thức `register(RegisterRequest req)` thực hiện kiểm tra email tồn tại qua `userRepository.findByEmail()`, nếu tồn tại ném `EmailAlreadyExistsException`. Validate mật khẩu mạnh bằng regex `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,128}$`, nếu không thỏa ném `InvalidPasswordException`. Tạo bản ghi User mới với `role_id=5` (Student), `provider='local'`, mã hóa mật khẩu qua BCrypt. Phát hành JWT access token 15 phút và refresh token 7 ngày thông qua `JwtTokenProvider`. Trả về `AuthResponse` chứa `userId`, `accessToken`, `refreshToken`, `role`. Toàn bộ thao tác phải bọc trong annotation `@Transactional`.

* **Hợp đồng định tuyến API và sự kiện [REQ-001], [REQ-002], [ARC-006]:** <!--START_API_CONTRACT-->
```json
{
  "openapi": "3.0.3",
  "info": {
    "title": "Membership Hub - User Service Authentication API",
    "version": "2.0.0"
  },
  "paths": {
    "/api/v1/auth/register": {
      "post": {
        "tags": ["Auth"],
        "summary": "Đăng ký tài khoản cục bộ bằng email và mật khẩu",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/RegisterRequest" }
            }
          }
        },
        "responses": {
          "201": { "description": "Tạo tài khoản thành công, trả về JWT" },
          "400": { "description": "Dữ liệu không hợp lệ" },
          "409": { "description": "Email đã tồn tại" }
        }
      }
    },
    "/api/v1/auth/social": {
      "post": {
        "tags": ["Auth"],
        "summary": "Đăng nhập hoặc đăng ký qua OAuth2 (Firebase, Google, Facebook)",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/SocialLoginRequest" }
            }
          }
        },
        "responses": {
          "200": { "description": "Xác thực thành công" }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "RegisterRequest": {
        "type": "object",
        "required": ["email", "password", "agreeTerms"],
        "properties": {
          "email":       { "type": "string", "format": "email", "maxLength": 255 },
          "password":    { "type": "string", "minLength": 8, "maxLength": 128 },
          "fullName":    { "type": "string", "maxLength": 100 },
          "agreeTerms":  { "type": "boolean" }
        }
      },
      "SocialLoginRequest": {
        "type": "object",
        "required": ["provider", "authorizationCode"],
        "properties": {
          "provider":           { "type": "string", "enum": ["firebase", "google", "facebook"] },
          "authorizationCode":  { "type": "string" },
          "profilePicture":     { "type": "string", "format": "uri" }
        }
      }
    }
  }
}
```
<!--END_API_CONTRACT-->

* **Bộ xử lý ngoại lệ cục bộ hóa của giai đoạn [EXC-004]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.userservice.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Bắt ngoại lệ EmailAlreadyExistsException và trả về HTTP 409 với mã lỗi EMAIL_ALREADY_EXISTS.
 * Áp dụng cho endpoint POST /api/v1/auth/register và POST /api/v1/auth/social.
 */
@Provider
public class EmailAlreadyExistsExceptionMapper implements ExceptionMapper<EmailAlreadyExistsException> {

    @Override
    public Response toResponse(EmailAlreadyExistsException ex) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("EMAIL_ALREADY_EXISTS", ex.getMessage()))
                .type("application/json; charset=utf-8")
                .build();
    }
}

/**
 * Bắt ngoại lệ InvalidPasswordException và trả về HTTP 400 với mã lỗi INVALID_PASSWORD_FORMAT.
 */
@Provider
public class InvalidPasswordExceptionMapper implements ExceptionMapper<InvalidPasswordException> {

    @Override
    public Response toResponse(InvalidPasswordException ex) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("INVALID_PASSWORD_FORMAT", ex.getMessage()))
                .type("application/json; charset=utf-8")
                .build();
    }
}

/**
 * Bắt ngoại lệ TermsNotAgreedException và trả về HTTP 400 với mã lỗi TERMS_NOT_AGREED.
 */
@Provider
public class TermsNotAgreedExceptionMapper implements ExceptionMapper<TermsNotAgreedException> {

    @Override
    public Response toResponse(TermsNotAgreedException ex) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("TERMS_NOT_AGREED", ex.getMessage()))
                .type("application/json; charset=utf-8")
                .build();
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 Nhiệm vụ phụ 1.3: Kiểm thử endpoint đăng ký tài khoản
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/AuthControllerTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp kiểm thử tích hợp tại đường dẫn `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/AuthControllerTest.java` thuộc package `org.nlh4j.membershiphub.userservice`. Sử dụng annotation `@QuarkusTest` kết hợp `@TestHTTPResource` để khởi tạo context Quarkus. Sử dụng `@QuarkusTestResource` với Testcontainers PostgreSQL 16-alpine để mô phỏng cơ sở dữ liệu, đảm bảo mỗi test chạy trên schema sạch. Sử dụng `RestAssured` để thực hiện HTTP request. Test case 1 (`testRegisterSuccess`): gửi POST `/api/v1/auth/register` với body `{"email":"newuser@example.com","password":"Strong@123","fullName":"Nguyen Van A","agreeTerms":true}`, kỳ vọng HTTP 201, response chứa `userId` định dạng UUID, `accessToken` chứa JWT hợp lệ với claim `sub` và `groups` chứa `Student`, `role` trả về là `STUDENT`. Test case 2 (`testRegisterDuplicateEmail`): đăng ký hai lần với cùng email, kỳ vọng request thứ hai trả về HTTP 409 và body chứa `code` là `EMAIL_ALREADY_EXISTS`. Test case 3 (`testRegisterWeakPassword`): gửi mật khẩu `weak`, kỳ vọng HTTP 400 với `code` là `INVALID_PASSWORD_FORMAT`. Test case 4 (`testRegisterTermsNotAgreed`): gửi `agreeTerms: false`, kỳ vọng HTTP 400 với `code` là `TERMS_NOT_AGREED`. Test case 5 (`testRegisterPasswordHashed`): verify bản ghi User trong database có `password_hash` khác plaintext và tuân thủ định dạng BCrypt (bắt đầu bằng `$2a$12$`).

#### 📝 Nhiệm vụ phụ 1.4: Biên soạn tài liệu kiến trúc user-service
##### Sub-Agent được phân công: Doc
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/docs/architecture/UserServiceArchitecture.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu Markdown tại đường dẫn `./sources/docs/architecture/UserServiceArchitecture.md` mô tả kiến trúc chi tiết của user-service. Nội dung bắt buộc gồm: (1) Mục lục rõ ràng; (2) Sơ đồ Mermaid `graph TD` thể hiện cấu trúc thành phần gồm `AuthController`, `OAuth2Service`, `UserService`, `RoleService`, `AuditLogger`, `JwtTokenProvider` cùng các repository tương ứng; (3) Sơ đồ Mermaid `sequenceDiagram` mô tả luồng đăng ký từ client gửi request đến khi nhận JWT; (4) Bảng mô tả năm vai trò RBAC theo Tag ID từ [ARC-001] đến [ARC-005] với cột `Role Name`, `Role ID`, `Quyền hạn`, `Endpoint được phép`; (5) Phần mô tả cơ chế bảo mật gồm BCrypt cost 12, JWT 15 phút, refresh token 7 ngày, rotation key thông qua Keycloak; (6) Bảng đối chiếu Tag ID với thành phần mã nguồn tương ứng. Tài liệu viết bằng tiếng Việt, mục lục rõ ràng, sơ đồ Mermaid hợp lệ.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->Tích Hợp OAuth2 Và Triển Khai Phân Quyền RBAC Với Audit Log<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 2.1: Triển khai OAuth2Service và JwtTokenProvider
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/OAuth2Service.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [ARC-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp `OAuth2Service` tại đường dẫn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/OAuth2Service.java` thuộc package `org.nlh4j.membershiphub.userservice`. Sử dụng annotation `@ApplicationScoped` để đăng ký CDI bean. Inject `Vertx WebClient` cho HTTP client bất đồng bộ, `UserRepository` và `JwtTokenProvider`. Phương thức `handleSocialLogin(SocialLoginRequest request)` thực hiện: (1) Dựa vào `request.getProvider()` lấy cấu hình `tokenEndpoint` và `userInfoEndpoint` tương ứng (Firebase: `https://securetoken.googleapis.com/v1/token`, Google: `https://oauth2.googleapis.com/token`, Facebook: `https://graph.facebook.com/v18.0/oauth/access_token`); (2) Gọi HTTP POST đến `tokenEndpoint` với grant_type `authorization_code`, `code`, `client_id`, `client_secret`, `redirect_uri` để trao đổi lấy `access_token`; (3) Gọi HTTP GET đến `userInfoEndpoint` với Bearer token nhận được để lấy email, name, picture; (4) Tìm user qua `userRepository.findByEmail(providerEmail)`. Nếu chưa tồn tại, tạo bản ghi mới với `role_id=5` (Student), `provider` tương ứng, `password_hash` là chuỗi UUID ngẫu nhiên (không dùng để đăng nhập). Nếu đã tồn tại, cập nhật `provider` nếu thay đổi. (5) Phát hành JWT thông qua `JwtTokenProvider`. Bắt tất cả ngoại lệ từ provider (timeout, 4xx, 5xx) và ném `OAuth2ProviderException` với mã lỗi `OAUTH2_PROVIDER_ERROR`. Đồng thời tạo lớp `JwtTokenProvider` tại đường dẫn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/JwtTokenProvider.java` sử dụng `SmallRye JWT Build` để phát hành JWT với thuật toán RS256, claim `sub` chứa userId, claim `groups` chứa role name, claim `upn` chứa email, `exp` là `now + 15 phút` cho access token và `now + 7 ngày` cho refresh token. Inject khóa bí mật từ `@ConfigProperty(name="jwt.private.key.location")` và `jwt.public.key.location`.

* **Hợp đồng định tuyến API và sự kiện [REQ-002], [ARC-006]:** <!--START_API_CONTRACT-->
```json
{
  "endpoint": "POST /api/v1/auth/social",
  "provider_flow": {
    "firebase":  { "tokenEndpoint": "https://securetoken.googleapis.com/v1/token", "userInfoEndpoint": "https://identitytoolkit.googleapis.com/v1/accounts:lookup" },
    "google":    { "tokenEndpoint": "https://oauth2.googleapis.com/token",        "userInfoEndpoint": "https://openidconnect.googleapis.com/v1/userinfo" },
    "facebook":  { "tokenEndpoint": "https://graph.facebook.com/v18.0/oauth/access_token", "userInfoEndpoint": "https://graph.facebook.com/me?fields=id,name,email,picture" }
  }
}
```
<!--END_API_CONTRACT-->

* **Bộ xử lý ngoại lệ cục bộ hóa của giai đoạn [EXC-004]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.userservice.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Bắt ngoại lệ OAuth2ProviderException khi không trao đổi được token hoặc không lấy được
 * thông tin người dùng từ provider, trả về HTTP 502 với mã lỗi OAUTH2_PROVIDER_ERROR.
 */
@Provider
public class OAuth2ProviderExceptionMapper implements ExceptionMapper<OAuth2ProviderException> {

    @Override
    public Response toResponse(OAuth2ProviderException ex) {
        return Response.status(Response.Status.BAD_GATEWAY)
                .entity(new ErrorResponse("OAUTH2_PROVIDER_ERROR", ex.getMessage()))
                .type("application/json; charset=utf-8")
                .build();
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 Nhiệm vụ phụ 2.2: Tạo migration bảng user_audit_log và UserService, RoleService, AuditLogger
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/center-service/src/main/resources/db/migration/V2__phase2_center_admin_relationship.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp migration tại đường dẫn `./sources/backend/center-service/src/main/resources/db/migration/V2__phase2_center_admin_relationship.sql` chứa DDL tạo bảng `user_audit_log` gồm `audit_id UUID NOT NULL PRIMARY KEY`, `user_id UUID NOT NULL` với FOREIGN KEY tham chiếu `users(user_id)`, `action_type VARCHAR(50) NOT NULL` với CHECK constraint `IN ('ROLE_CHANGED', 'CENTER_ASSIGNED', 'CENTER_UNASSIGNED', 'PASSWORD_RESET', 'ACCOUNT_LOCKED')`, `performed_by UUID NOT NULL` với FOREIGN KEY tham chiếu `users(user_id)`, `old_value TEXT NULL`, `new_value TEXT NULL`, `performed_at TIMESTAMP NOT NULL DEFAULT now()`. Bổ sung INDEX `idx_user_audit_user_id` trên cột `user_id` và INDEX `idx_user_audit_performed_at` trên cột `performed_at` để tối ưu truy vấn audit log. Đồng thời tạo tệp migration bổ sung `V20250102100001__phase2_user_audit_log.sql` trong cùng thư mục chứa DDL tương tự để đảm bảo tính tương thích ngược (lưu ý: tệp này sẽ được merge vào tệp V2 chính, xem chi tiết tại tệp V2). Đồng thời tạo lớp `UserService` tại đường dẫn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/UserService.java` thực thi phương thức `changeRole(UUID userId, int newRoleId, UUID performedBy)` với annotation `@Transactional(REQUIRES_NEW)`. Phương thức này gọi `roleService.findById(newRoleId)` kiểm tra vai trò hợp lệ, cập nhật `users.role_id = newRoleId`, gọi `auditLogger.log("ROLE_CHANGED", userId, performedBy, oldRoleName, newRoleName)` để ghi log trong cùng transaction. Lớp `RoleService` tại đường dẫn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/RoleService.java` chứa phương thức `findById(int roleId)` trả về Optional<Role> và `validateRoleTransition(int oldRoleId, int newRoleId)` kiểm tra quy tắc chuyển đổi (ví dụ: cấm chuyển System Admin sang Student). Lớp `AuditLogger` tại đường dẫn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuditLogger.java` chứa phương thức `log(String actionType, UUID userId, UUID performedBy, String oldValue, String newValue)` insert vào bảng `user_audit_log`. Endpoint `PUT /api/v1/users/{id}/role` chú thích `@RolesAllowed("SYSTEM_ADMIN")` để chỉ System Admin mới có quyền truy cập.

* **Đặc tả DDL SQL lược đồ cơ sở dữ liệu [REQ-003], [NFR-006]:** <!--START_DDL_MIGRATION-->
```sql
-- ============================================================
-- MIGRATION: V2__phase2_center_admin_relationship.sql  (center-service shared schema)
-- Tag ID: [REQ-003], [REQ-006], [NFR-006]
-- ============================================================

-- Bảng ghi log kiểm toán cho các hành động thay đổi vai trò và quyền
CREATE TABLE user_audit_log (
    audit_id          UUID           PRIMARY KEY,
    user_id           UUID           NOT NULL,
    action_type       VARCHAR(50)    NOT NULL,
    performed_by      UUID           NOT NULL,
    old_value         TEXT,
    new_value         TEXT,
    performed_at      TIMESTAMP      NOT NULL DEFAULT now(),
    CONSTRAINT fk_user_audit_user
        FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_user_audit_performer
        FOREIGN KEY (performed_by) REFERENCES users(user_id),
    CONSTRAINT chk_audit_action
        CHECK (action_type IN ('ROLE_CHANGED', 'CENTER_ASSIGNED', 'CENTER_UNASSIGNED', 'PASSWORD_RESET', 'ACCOUNT_LOCKED'))
);

CREATE INDEX idx_user_audit_user_id      ON user_audit_log(user_id);
CREATE INDEX idx_user_audit_performed_at ON user_audit_log(performed_at);

-- Bảng quan hệ User-Center dùng cho việc phân công Center Admin
CREATE TABLE center_admins (
    center_id     UUID        NOT NULL,
    user_id       UUID        NOT NULL,
    assigned_at   TIMESTAMP   NOT NULL DEFAULT now(),
    assigned_by   UUID        NOT NULL,
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (center_id, user_id),
    CONSTRAINT fk_center_admin_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT fk_center_admin_user   FOREIGN KEY (user_id)   REFERENCES users(user_id),
    CONSTRAINT fk_center_admin_by     FOREIGN KEY (assigned_by) REFERENCES users(user_id)
);

CREATE INDEX idx_center_admins_user_id   ON center_admins(user_id);
CREATE INDEX idx_center_admins_is_active ON center_admins(is_active);
```
<!--END_DDL_MIGRATION-->

* **Hợp đồng định tuyến API và sự kiện [REQ-003], [ARC-006]:** <!--START_API_CONTRACT-->
```json
{
  "endpoint": "PUT /api/v1/users/{id}/role",
  "request":  { "roleId": "integer (1=SYSTEM_ADMIN, 2=CENTER_ADMIN, 3=MANAGER, 4=TEACHER, 5=STUDENT)" },
  "response_200": { "userId": "uuid", "newRole": "string", "updatedAt": "timestamp" },
  "response_403": { "code": "ROLE_UPDATE_FORBIDDEN" }
}
```
<!--END_API_CONTRACT-->

* **Bộ xử lý ngoại lệ cục bộ hóa của giai đoạn [REQ-003]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.userservice.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Bắt ngoại lệ RoleUpdateForbiddenException khi người thực hiện không có quyền SYSTEM_ADMIN,
 * trả về HTTP 403 với mã lỗi ROLE_UPDATE_FORBIDDEN.
 */
@Provider
public class RoleUpdateForbiddenExceptionMapper implements ExceptionMapper<RoleUpdateForbiddenException> {

    @Override
    public Response toResponse(RoleUpdateForbiddenException ex) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorResponse("ROLE_UPDATE_FORBIDDEN", ex.getMessage()))
                .type("application/json; charset=utf-8")
                .build();
    }
}

/**
 * Bắt ngoại lệ InvalidRoleTransitionException khi vi phạm quy tắc chuyển đổi vai trò,
 * trả về HTTP 400 với mã lỗi INVALID_ROLE_TRANSITION.
 */
@Provider
public class InvalidRoleTransitionExceptionMapper implements ExceptionMapper<InvalidRoleTransitionException> {

    @Override
    public Response toResponse(InvalidRoleTransitionException ex) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("INVALID_ROLE_TRANSITION", ex.getMessage()))
                .type("application/json; charset=utf-8")
                .build();
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 Nhiệm vụ phụ 2.3: Kiểm thử luồng OAuth2 và phân quyền
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/OAuth2ServiceTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [REQ-003], [ARC-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp kiểm thử tại đường dẫn `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/OAuth2ServiceTest.java` thuộc package `org.nlh4j.membershiphub.userservice`. Sử dụng `@QuarkusTest` kết hợp `@InjectMock` để mock `Vertx WebClient` nhằm giả lập HTTP call tới provider OAuth2. Test case 1 (`testGoogleSocialLoginSuccess`): mock HTTP response từ Google token endpoint trả về `access_token`, mock userInfo endpoint trả về JSON chứa `email: "newgoogle@example.com"`, `name: "Google User"`. Gọi `oauth2Service.handleSocialLogin()` với provider=google, kỳ vọng trả về `AuthResponse` chứa `accessToken` và `role` là `STUDENT`, bản ghi User mới được tạo trong database với `provider='google'`. Test case 2 (`testOAuth2ProviderError`): mock HTTP response trả về lỗi 400 từ token endpoint, kỳ vọng ném `OAuth2ProviderException` và exception mapper trả về HTTP 502. Test case 3 (`testChangeRoleAsSystemAdmin`): tạo user với role SYSTEM_ADMIN, gọi PUT `/api/v1/users/{otherUserId}/role` với body `{"roleId": 2}`, kỳ vọng HTTP 200 và bản ghi audit được ghi vào `user_audit_log` với `action_type='ROLE_CHANGED'`. Test case 4 (`testChangeRoleAsStudentForbidden`): gọi cùng endpoint nhưng với JWT của Student, kỳ vọng HTTP 403 với mã `ROLE_UPDATE_FORBIDDEN`.

#### 📝 Nhiệm vụ phụ 2.4: Tài liệu hóa API user-service
##### Sub-Agent được phân công: Doc
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/docs/api/UserServiceApiContract.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [REQ-001], [REQ-002], [REQ-003]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu Markdown tại đường dẫn `./sources/docs/api/UserServiceApiContract.md` mô tả chi tiết toàn bộ API của user-service. Nội dung bắt buộc gồm: (1) Mục lục rõ ràng; (2) Bảng danh sách endpoint gồm `POST /api/v1/auth/register`, `POST /api/v1/auth/social`, `PUT /api/v1/users/{id}/role` với cột Method, Path, Mô tả, Authentication, Request Schema, Response Schema, Error Codes; (3) Mô tả chi tiết luồng OAuth2 cho cả ba provider Firebase, Google, Facebook với sơ đồ Mermaid sequence diagram; (4) Bảng mã lỗi HTTP 400, 403, 409 với giải thích nguyên nhân và cách khắc phục; (5) Ví dụ curl command cho từng endpoint kèm JSON request/response mẫu. Tài liệu viết bằng tiếng Việt, tham chiếu chính xác các Tag ID [REQ-001] đến [REQ-003] và [ARC-006].

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->Triển Khai Center-Service Và Phân Công Center Admin Với Audit Log<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 3.1: Khởi tạo pom.xml và triển khai CenterController
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [REQ-004], [REQ-005], [REQ-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp `pom.xml` cho `center-service` tại đường dẫn `./sources/backend/center-service/pom.xml` với cấu trúc parent-child POM tương tự user-service nhưng chỉ giữ các dependency thiết yếu: `quarkus-resteasy-reactive-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-hibernate-validator`, `quarkus-arc`. Tạo lớp `CenterController` tại đường dẫn `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterController.java` thuộc package `org.nlh4j.membershiphub.centerservice`. Class chú thích `@Path("/api/v1/centers")` sử dụng JAX-RS. Đăng ký sáu endpoint: `GET /` với `@RolesAllowed({"SYSTEM_ADMIN", "CENTER_ADMIN", "MANAGER", "TEACHER", "STUDENT"})` trả về danh sách phân trang, `POST /` với `@RolesAllowed("SYSTEM_ADMIN")` tạo mới, `PUT /{id}` cập nhật, `DELETE /{id}` soft delete, `POST /{id}/admins` phân công Center Admin, `DELETE /{id}/admins` hủy phân công. Inject `CenterService` và `CenterAdminService` thông qua CDI. Tất cả DTO đầu vào phải sử dụng Bean Validation với các ràng buộc `@NotNull`, `@Size(max=100)` cho name, `@Size(max=255)` cho address, `@Pattern(regexp="^[0-9]{10,13}$")` cho taxId, `@Pattern(regexp="^[+0-9 ()\\-]{0,20}$")` cho contactPhone, `@Email` cho contactEmail. Trả về HTTP 409 khi trùng TaxID, HTTP 400 khi validation fail, HTTP 404 khi không tìm thấy trung tâm.

* **Hợp đồng định tuyến API và sự kiện [REQ-004], [REQ-005], [REQ-006]:** <!--START_API_CONTRACT-->
```json
{
  "endpoints": [
    "GET    /api/v1/centers?page=&size=",
    "POST   /api/v1/centers (SYSTEM_ADMIN)",
    "PUT    /api/v1/centers/{id} (SYSTEM_ADMIN)",
    "DELETE /api/v1/centers/{id} (SYSTEM_ADMIN, soft delete)",
    "POST   /api/v1/centers/{id}/admins (SYSTEM_ADMIN)",
    "DELETE /api/v1/centers/{id}/admins?userId= (SYSTEM_ADMIN)"
  ]
}
```
<!--END_API_CONTRACT-->

* **Bộ xử lý ngoại lệ cục bộ hóa của giai đoạn [EXC-004]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.centerservice.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Bắt ngoại lệ DuplicateTaxIdException khi TaxID đã tồn tại, trả về HTTP 409 với mã DUPLICATE_TAX_ID.
 */
@Provider
public class DuplicateTaxIdExceptionMapper implements ExceptionMapper<DuplicateTaxIdException> {

    @Override
    public Response toResponse(DuplicateTaxIdException ex) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("DUPLICATE_TAX_ID", ex.getMessage()))
                .type("application/json; charset=utf-8")
                .build();
    }
}

/**
 * Bắt ngoại lệ CenterNotFoundException khi không tìm thấy trung tâm, trả về HTTP 404.
 */
@Provider
public class CenterNotFoundExceptionMapper implements ExceptionMapper<CenterNotFoundException> {

    @Override
    public Response toResponse(CenterNotFoundException ex) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("CENTER_NOT_FOUND", ex.getMessage()))
                .type("application/json; charset=utf-8")
                .build();
    }
}

/**
 * Bắt ngoại lệ CenterAdminAlreadyAssignedException khi người dùng đã là admin,
 * trả về HTTP 409 với mã CENTER_ADMIN_ALREADY_ASSIGNED.
 */
@Provider
public class CenterAdminAlreadyAssignedExceptionMapper implements ExceptionMapper<CenterAdminAlreadyAssignedException> {

    @Override
    public Response toResponse(CenterAdminAlreadyAssignedException ex) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("CENTER_ADMIN_ALREADY_ASSIGNED", ex.getMessage()))
                .type("application/json; charset=utf-8")
                .build();
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 Nhiệm vụ phụ 3.2: Triển khai CenterService và CenterAdminService
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-004], [REQ-005], [REQ-006], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp `CenterService` tại đường dẫn `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterService.java` thuộc package `org.nlh4j.membershiphub.centerservice`. Sử dụng annotation `@ApplicationScoped`. Inject `CenterRepository` và `EntityManager`. Phương thức `listCenters(int page, int size)` sử dụng Panache với phân trang, trả về `PanacheQuery<Center>` với `page(page).size(size)`. Phương thức `createCenter(CenterRequest req)` thực hiện: kiểm tra `centerRepository.findByTaxId(req.getTaxId())`, nếu đã tồn tại ném `DuplicateTaxIdException`. Tạo bản ghi Center mới với UUID ngẫu nhiên, persist qua `entityManager.persist()`. Phương thức `updateCenter(UUID id, CenterRequest req)` tìm center qua id, nếu không tồn tại ném `CenterNotFoundException`, cập nhật các trường name, address, contactPhone, contactEmail. Phương thức `softDeleteCenter(UUID id)` thực hiện soft delete bằng cách cập nhật cờ `is_active=false` (cột bổ sung trong schema, migration được thực hiện thông qua `ALTER TABLE centers ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE`). Tạo lớp `CenterAdminService` tại đường dẫn `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterAdminService.java` thực thi phương thức `assignAdmin(UUID centerId, UUID userId, UUID assignedBy)` với annotation `@Transactional`. Phương thức kiểm tra center tồn tại, kiểm tra user tồn tại, kiểm tra quan hệ admin đã tồn tại (ném `CenterAdminAlreadyAssignedException` nếu có). Cập nhật `users.role_id=2` (CENTER_ADMIN), insert bản ghi vào bảng `center_admins` với `is_active=true`, gọi `auditLogger.log("CENTER_ASSIGNED", userId, assignedBy, null, centerId.toString())` trong cùng transaction. Phương thức `unassignAdmin()` đặt `is_active=false` và ghi log `CENTER_UNASSIGNED`.

#### 📝 Nhiệm vụ phụ 3.3: Kiểm thử center-service và phân công admin
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/CenterControllerTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-004], [REQ-005], [REQ-006], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp kiểm thử tại đường dẫn `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/CenterControllerTest.java` thuộc package `org.nlh4j.membershiphub.centerservice`. Sử dụng `@QuarkusTest` kết hợp Testcontainers PostgreSQL 16. Test case 1 (`testListCentersPagination`): tạo 25 bản ghi Center, gọi GET `/api/v1/centers?page=0&size=10`, kỳ vọng HTTP 200, response chứa 10 phần tử, tổng số bản ghi 25. Test case 2 (`testCreateCenterDuplicateTaxId`): tạo center với taxId `1234567890`, tạo lần hai với cùng taxId, kỳ vọng HTTP 409 với mã `DUPLICATE_TAX_ID`. Test case 3 (`testCreateCenterInvalidTaxIdFormat`): gửi taxId `invalid123`, kỳ vọng HTTP 400 với mã validation. Test case 4 (`testAssignAdminSuccess`): tạo user và center, gọi POST `/api/v1/centers/{centerId}/admins` với body `{"userId":"..."}`, kỳ vọng HTTP 201, bản ghi `center_admins` được tạo với `is_active=true`, `users.role_id` được cập nhật thành 2, bản ghi audit `CENTER_ASSIGNED` được ghi. Test case 5 (`testAssignAdminDuplicate`): gọi endpoint phân công hai lần cho cùng user và center, kỳ vọng lần thứ hai trả về HTTP 409 với mã `CENTER_ADMIN_ALREADY_ASSIGNED`.

#### 📝 Nhiệm vụ phụ 3.4: Tài liệu hóa API center-service
##### Sub-Agent được phân công: Doc
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/docs/api/CenterServiceApiContract.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [REQ-004], [REQ-005], [REQ-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu Markdown tại đường dẫn `./sources/docs/api/CenterServiceApiContract.md` mô tả chi tiết toàn bộ API của center-service. Nội dung bắt buộc gồm: (1) Mục lục rõ ràng; (2) Bảng danh sách sáu endpoint với Method, Path, Mô tả, Authentication Required, Request Schema, Response Schema, Error Codes; (3) Sơ đồ Mermaid sequence diagram mô tả luồng phân công Center Admin từ khi System Admin gửi request đến khi ghi log audit; (4) Bảng mã lỗi HTTP 400, 404, 409 với giải thích chi tiết; (5) Ví dụ curl command cho từng endpoint kèm JSON request/response mẫu; (6) Phần giải thích cơ chế soft delete và cách truy vấn các trung tâm đã xóa. Tài liệu viết bằng tiếng Việt, tham chiếu chính xác các Tag ID [REQ-004] đến [REQ-006] và [EXC-004].