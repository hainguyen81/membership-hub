# Giai đoạn 2: <!--PHASE_NAME_START-->Quản Lý Người Dùng, Trung Tâm Và Phân Quyền RBAC<!--PHASE_NAME_END-->

## 📊 Kiểm Soát Tài Liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Bản Thiết Kế** | ARCH-20260829225017 |
| **Tên Dự Án** | membership-hub |
| **Giai đoạn** | 2 |
| **Tên Giai Đoạn** | <!--PHASE_NAME_START-->Quản Lý Người Dùng, Trung Tâm Và Phân Quyền RBAC<!--PHASE_NAME_END--> |
| **Mô Tả** | <!--PHASE_DESC_START-->Giai đoạn 2 tập trung xây dựng hai trụ cột nghiệp vụ nền tảng của hệ thống Membership Hub: quản lý danh tính người dùng và quản lý hồ sơ trung tâm. Giai đoạn này hiện thực hóa toàn bộ luồng đăng ký tài khoản mới với xác thực email/mật khẩu theo chuẩn OWASP, tích hợp xác thực xã hội qua OAuth2 (Firebase/Google/Facebook), cơ chế gán và thay đổi vai trò người dùng theo ma trận phân quyền 5 cấp độ (RBAC), đồng thời thiết lập REST API quản lý trung tâm với đầy đủ thao tác CRUD, kiểm tra trùng lặp TaxID, cơ chế gán/huỷ gán Center Admin với ranh giới phân quyền chặt chẽ<!--PHASE_DESC_END--> |
| **Phiên Bản** | 1.0 (Baseline) |
| **Ngày Giờ** | 2026/08/29 22:50:17 |
| **Tác Giả** | Kiến Trúc Sư Hệ Thống Doanh Nghiệp (SA Agent) |
| **Phê Duyệt** | Đang chờ Rà Soát Quản Trị Kỹ Thuật |

## 1. Phạm Vi Hoạt Động Và Mục Tiêu Giai Đoạn

Giai đoạn 2 đóng vai trò trụ cột nghiệp vụ thứ hai trong hệ thống membership-hub, tập trung vào việc hiện thực hóa toàn bộ luồng quản lý danh tính người dùng và quản lý hồ sơ trung tâm dựa trên bộ khung microservices đã được thiết lập từ Giai đoạn 1. Phạm vi kỹ thuật cốt lõi của giai đoạn này bao gồm 7 nhiệm vụ backlog chính được phân bổ: Nhiệm vụ 2 (Endpoint đăng ký người dùng mới), Nhiệm vụ 3 (Xác thực Social OAuth2), Nhiệm vụ 4 (Gán và thay đổi vai trò người dùng theo RBAC), Nhiệm vụ 5 (Danh sách trung tâm cho người dùng xác thực), Nhiệm vụ 6 (Quản lý CRUD trung tâm), Nhiệm vụ 7 (Gán/huỷ gán Center Admin cho trung tâm) và Nhiệm vụ 8 (Danh sách khoá học). Theo kế hoạch phân bổ trong bảng tổng hợp đa giai đoạn, giai đoạn 2 được phân bổ chính xác 7 ngày làm việc (từ Ngày 1 đến Ngày 7), phù hợp với biên tính toán Relative_Z = 7.

Mục tiêu cốt lõi của giai đoạn là xây dựng bốn luồng nghiệp vụ quan trọng trên hai microservices chính: `user-service` và `center-service`. Cụ thể, trên `user-service` sẽ hiện thực hóa endpoint POST `/api/v1/users/register` với xác thực email theo chuẩn RFC 5322, mật khẩu mạnh tuân thủ OWASP (tối thiểu 8 ký tự, bao gồm chữ hoa, chữ thường, chữ số và ký tự đặc biệt), đồng ý điều khoản bắt buộc, sinh bản ghi user với role mặc định Student/Teacher thông qua Bean Validation 3.0 và tích hợp JWT 15 phút cùng refresh token 7 ngày. Endpoint POST `/api/v1/auth/social` tiếp nhận ID token từ 3 provider Firebase, Google, Facebook thông qua cơ chế đổi OAuth2 code sang thông tin user, đồng bộ hóa bản ghi local và cấp JWT tương ứng. Endpoint PUT `/api/v1/users/{id}/role` cho phép System Admin hoặc Center Admin cập nhật vai trò người dùng theo ma trận RBAC 5 cấp độ, kèm theo cơ chế vô hiệu hóa phiên bảo mật hiện tại thông qua blacklist Redis và ghi nhận audit log tuân thủ [NFR-006].

Trên `center-service`, giai đoạn này triển khai REST API quản lý trung tâm với GET `/api/v1/centers` trả về danh sách phân trang gồm Name, Address, TaxID, AdminContact được bảo vệ bởi JWT middleware. CRUD operations POST/PUT/DELETE `/api/v1/centers` chỉ dành cho System Admin với kiểm tra trùng lặp TaxID và validation Bean đầy đủ cho các trường name (max 100 ký tự), address (max 255 ký tự), tax_id (10-13 chữ số, unique), contact_phone (định dạng số điện thoại), contact_email (định dạng email). Endpoint POST/DELETE `/api/v1/centers/{id}/admins` cho phép gán hoặc huỷ gán Center Admin với cập nhật role thành Center Admin và ghi nhận center_id vào bảng liên kết CenterAdmins, đảm bảo phân quyền theo [ARC-002]. Ngoài ra, giai đoạn này cũng bổ sung REST API GET `/api/v1/courses` trên course-service để hỗ trợ danh sách khoá học cho người dùng xác thực với các trường CourseID, Title, StartDate, EndDate, TeacherName và hỗ trợ phân trang.

Mục tiêu chính là toàn bộ REST API có thể triển khai tức thì lên môi trường development ngay khi giai đoạn kết thúc, sẵn sàng cho giai đoạn 3 phát triển các luồng nghiệp vụ khoá học và điểm danh QR. Tất cả hành động quản lý phải được ghi log kiểm toán thông qua lớp `AuditLogger` tập trung tuân thủ chuẩn bảo mật OWASP, đảm bảo dấu vết kiểm toán đầy đủ phục vụ tuân thủ [NFR-003] và [NFR-006] với thời gian lưu trữ tối thiểu 1 năm.

## 2. Phạm Vi Kỹ Thuật Cho Phép Và Ranh Giới Thư Mục

Danh sách đầy đủ các tệp tin vật lý được phép tạo mới trong giai đoạn 2, tuân thủ nghiêm ngặt quy ước gói `org.nlh4j.membershiphub` và ranh giới thư mục doanh nghiệp:

* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/AuthController.java` — [REQ-001], [REQ-002]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/UserController.java` — [REQ-003]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/AuthService.java` — [REQ-001], [REQ-002]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/UserRoleService.java` — [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/SocialAuthService.java` — [REQ-002]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/RegisterRequest.java` — [REQ-001]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/SocialAuthRequest.java` — [REQ-002]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/RoleUpdateRequest.java` — [REQ-003]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/AuthResponse.java` — [REQ-001], [REQ-002]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtAuthFilter.java` — [ARC-006], [NFR-003]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SocialTokenVerifier.java` — [REQ-002]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java` — [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/audit/AuditLogger.java` — [NFR-006]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/CenterController.java` — [REQ-004], [REQ-005], [REQ-006]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterService.java` — [REQ-004], [REQ-005]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminService.java` — [REQ-006], [ARC-002]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/CenterRequest.java` — [REQ-005]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/CenterResponse.java` — [REQ-004]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/CenterAdminRequest.java` — [REQ-006]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/repository/CenterRepository.java` — [REQ-004], [REQ-005]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/exception/CenterExceptionHandler.java` — [EXC-004]
* `./sources/docs/architecture/phase-2-rbac-matrix.md` — [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DOC-001]
* `./sources/docs/api/user-center-contracts.md` — [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [DOC-001]

* **RÀNG BUỘC BẮT BUỘC VỀ BỘ KHUNG NỀN TẢNG**:
  - Tất cả tài sản mã nguồn ứng dụng trong giai đoạn 2 phải kế thừa bộ khung build descriptors đã được khởi tạo ở Giai đoạn 1 (`./sources/backend/pom.xml`, `./sources/backend/<service-name>/pom.xml`).
  - Tệp `./sources/backend/pom.xml` và 4 tệp con KHÔNG được tái tạo trong giai đoạn này vì đã tồn tại từ Giai đoạn 1.
  - Toàn bộ mã nguồn mới phải tuân thủ quy ước gói `org.nlh4j.membershiphub.<service-name>` và được truy vết bằng các mã thẻ quy định.

## 3. Chỉ Thị Chức Năng Cho Từng Sub-Agent

* **Coder**: Đóng vai trò lập trình viên ứng dụng chính. Chịu trách nhiệm hiện thực hóa toàn bộ controller, service, DTO, exception handler và audit logger trong package `controller`, `service`, `dto`, `security`, `exception` và `audit` của user-service và center-service. Bị cấm viết bộ kiểm thử, tài liệu hoặc cấu hình hạ tầng.

* **Tester**: Đóng vai trò kiểm thử viên chính. Tạo bộ kiểm thử đơn vị JUnit 5 kết hợp Mockito cho AuthController, SocialAuthService, UserRoleService, CenterController, CenterAdminService, GlobalExceptionHandler. Xây dựng bộ kiểm thử tích hợp liên service sử dụng Testcontainers. Bị cấm sửa đổi mã nguồn sản phẩm.

* **Doc**: Soạn thảo 2 tài liệu Markdown quan trọng trong thư mục `./sources/docs/`: tài liệu ma trận RBAC và tài liệu hợp đồng API người dùng và trung tâm. Tất cả tệp tài liệu phải kết thúc bằng phần mở rộng `.md`.

* **Reviewer**: Thực hiện rà soát chất lượng mã nguồn theo checklist OWASP Top 10, đánh giá tuân thủ nguyên tắc least privilege trong ma trận RBAC, xác minh tính đúng đắn của việc áp dụng Bean Validation và JWT middleware, phát hiện sớm các vấn đề bảo mật tiềm ẩn như SQL injection, timing attack, token confusion.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn (DoD)

Giai đoạn 2 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: (1) Endpoint POST `/api/v1/users/register` xử lý thành công luồng đăng ký với đầy đủ validation và trả về JWT 15 phút cùng refresh token 7 ngày; (2) Endpoint POST `/api/v1/auth/social` xác thực thành công qua 3 provider Firebase, Google, Facebook và đồng bộ hóa bản ghi local; (3) Endpoint PUT `/api/v1/users/{id}/role` thực thi đúng ma trận RBAC 5 cấp độ, vô hiệu hóa phiên cũ thông qua Redis blacklist và ghi audit log; (4) Endpoint GET `/api/v1/centers` trả về danh sách phân trang đầy đủ Name, Address, TaxID, AdminContact; (5) CRUD trung tâm với kiểm tra trùng lặp TaxID và validation đầy đủ; (6) Endpoint gán/huỷ gán Center Admin cập nhật đúng role và ghi nhận center_id; (7) 100% thẻ truy vết `[REQ-001]`, `[REQ-002]`, `[REQ-003]`, `[REQ-004]`, `[REQ-005]`, `[REQ-006]`, `[ARC-001]` đến `[ARC-005]`, `[EXC-004]` được ánh xạ đầy đủ vào mã nguồn và tài liệu; (8) 100% bộ kiểm thử JUnit đạt trạng thái PASS với code coverage >= 80% cho các lớp controller và service.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->KHỞI TẠO AUTHCONTROLLER VÀ LUỒNG ĐĂNG KÝ NGƯỜI DÙNG<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 1.1: Triển khai Controller đăng ký người dùng

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/AuthController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [EXC-004], [ARC-006]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo lớp `AuthController` được đánh dấu `@RestController` với đường dẫn gốc `/api/v1`. Triển khai endpoint `POST /api/v1/users/register` nhận `RegisterRequest` thông qua `@RequestBody` với annotation `@Valid` để kích hoạt Bean Validation Jakarta. Ủy quyền xử lý cho `AuthService.register()`. Trả về `ResponseEntity` với mã HTTP 201 Created và `AuthResponse` chứa `accessToken` (JWT 15 phút), `refreshToken` (7 ngày), `expiresIn` (900 giây), `userId` (UUID) và `role` mặc định. Toàn bộ endpoint được bảo vệ bởi `JwtAuthFilter` cho phép truy cập công khai ngoại trừ chính nó. Tích hợp annotation `@AuditLogged` để ghi log kiểm toán phục vụ [NFR-006] với action `USER_REGISTERED`. Đảm bảo xử lý ngoại lệ `MethodArgumentNotValidException` trả về HTTP 400 với mảng chi tiết lỗi từng trường, `EmailAlreadyExistsException` trả về HTTP 409 với mã `EMAIL_ALREADY_EXISTS`, tuân thủ chuẩn OWASP A03 Injection thông qua việc sử dụng JPQL parameter binding trong service layer.

#### 📝 NHIỆM VỤ PHỤ 1.2: Viết bộ kiểm thử đăng ký người dùng

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/AuthController.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/controller/AuthControllerTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [EXC-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử đơn vị `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/controller/AuthControllerTest.java` sử dụng JUnit 5 kết hợp Mockito 5.7.0 và RestAssured 5.4.0. Tạo tối thiểu 6 test case: (1) `register_withValidData_returns201AndJwtToken` xác minh đăng ký thành công với email hợp lệ, mật khẩu mạnh, đồng ý điều khoản trả về HTTP 201 và response body chứa `accessToken`, `refreshToken`, `userId`, `role`; (2) `register_withInvalidEmail_returns400` xác minh email sai định dạng `invalid-email` trả về HTTP 400 với mảng `errors` chứa trường `email`; (3) `register_withWeakPassword_returns400` xác minh mật khẩu `weak` thiếu chữ hoa, ký tự đặc biệt trả về HTTP 400 với danh sách trường lỗi; (4) `register_withoutAgreedToTerms_returns400` xác minh thiếu checkbox đồng ý điều khoản trả về HTTP 400; (5) `register_withDuplicateEmail_returns409` xác minh email đã tồn tại trả về HTTP 409 với mã `EMAIL_ALREADY_EXISTS`; (6) `register_withMissingRequiredField_returns400` xác minh thiếu trường bắt buộc trả về HTTP 400. Sử dụng `@InjectMock` để mock `AuthService` và `AuditLogger`.

#### 📝 NHIỆM VỤ PHỤ 1.3: Tài liệu API đăng ký

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/api/user-center-contracts.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Cập nhật tệp tài liệu API tổng hợp `./sources/docs/api/user-center-contracts.md` bổ sung mục mô tả endpoint `POST /api/v1/users/register` với các thành phần: mô tả nghiệp vụ chi tiết, bảng mã lỗi HTTP 400 (validation failed), 409 (EMAIL_ALREADY_EXISTS), schema request/response đầy đủ, ví dụ curl thực tế với payload JSON, ghi chú bảo mật (rate limiting 5 lần/phút), liên kết chéo đến ma trận RBAC. Đảm bảo tài liệu chứa sơ đồ Mermaid `sequenceDiagram` thể hiện luồng đăng ký từ client đến server và phản hồi JWT.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->TÍCH HỢP XÁC THỰC SOCIAL OAUTH2 VÀ SOCIAL PROVIDER<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 2.1: Triển khai SocialAuthService

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/SocialAuthService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [ARC-006], [NFR-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/SocialAuthService.java` hiện thực hóa lớp `SocialAuthService` được tiêm `SocialTokenVerifier` và `UserSocialAccountRepository`. Triển khai phương thức `authenticateWithSocial(SocialAuthRequest request)` thực hiện theo trình tự: (1) xác minh ID token với provider tương ứng thông qua `SocialTokenVerifier.verify()`, (2) trích xuất email và provider_user_id từ `SocialUserInfo`, (3) tìm kiếm bản ghi trong bảng `UserSocialAccounts` theo `(provider, provider_user_id)`, (4) nếu chưa tồn tại thì tạo mới user với role mặc định `Student` (role_id=5) và liên kết social account, (5) cập nhật `profile_picture_url` nếu có, (6) gọi `JwtTokenProvider.generateAccessToken()` và `generateRefreshToken()` trả về `AuthResponse` với cờ `isNewUser`. Toàn bộ thao tác phải sử dụng `@Transactional` và ghi audit log thông qua `AuthAuditLogger`. Xử lý `UnsupportedProviderException` trả về HTTP 400 với mã `UNSUPPORTED_PROVIDER`, `InvalidTokenException` trả về HTTP 401 với mã `INVALID_SOCIAL_TOKEN`.

#### 📝 NHIỆM VỤ PHỤ 2.2: Kiểm thử Social Auth

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/SocialAuthService.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/service/SocialAuthServiceTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [EXC-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử đơn vị `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/service/SocialAuthServiceTest.java` sử dụng JUnit 5 kết hợp Mockito 5.7.0 (`@InjectMock`). Mock `SocialTokenVerifier` trả về `SocialUserInfo` giả lập cho từng provider. Tạo 5 test case: (1) `authenticateWithGoogle_forNewUser_createsAccountAndReturnsJwt` xác minh xác thực thành công với Google token hợp lệ cho user mới, trả về `AuthResponse` với `isNewUser=true` và role `Student`; (2) `authenticateWithGoogle_forExistingUser_returnsExistingJwt` xác minh xác thực thành công cho user đã liên kết trước đó, trả về `isNewUser=false`; (3) `authenticateWithExpiredToken_throwsInvalidTokenException` xác minh token hết hạn ném `InvalidTokenException`; (4) `authenticateWithUnknownProvider_throwsUnsupportedProviderException` xác minh provider `twitter` không hỗ trợ ném `UnsupportedProviderException`; (5) `authenticateWithFacebook_savesProfilePicture` xác minh Facebook provider lưu đúng `profile_picture_url` vào bảng `UserSocialAccounts`. Mock `UserSocialAccountRepository` để kiểm tra phương thức `findByProviderAndProviderUserId` được gọi đúng tham số.

#### 📝 NHIỆM VỤ PHỤ 2.3: Review mã nguồn Authentication

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtAuthFilter.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [REQ-002], [NFR-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer thực hiện đánh giá chuyên sâu lớp `JwtAuthFilter` và `SocialTokenVerifier` tại đường dẫn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtAuthFilter.java` để phát hiện: (1) khả năng timing attack trong so sánh token sử dụng `MessageDigest.isEqual()` thay vì `String.equals()`, (2) lỗi xác thực chữ ký JWT khi token bị giả mạo, (3) xử lý trường hợp token bị thu hồi (blacklist) thông qua Redis với key `jwt:blacklist:<jti>` và TTL bằng thời gian còn lại của token, (4) tuân thủ nguyên tắc OWASP A02:2021 về Cryptographic Failures. Đề xuất vá lỗi cụ thể cho từng phát hiện và tạo báo cáo review với format: Vấn đề phát hiện, Mức độ nghiêm trọng (Critical/High/Medium/Low), Đề xuất fix, File liên quan.

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->CƠ CHẾ GÁN VÀ THAY ĐỔI VAI TRÒ NGƯỜI DÙNG (RBAC) 5 CẤP ĐỘ<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 3.1: Triển khai UserRoleService

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/UserRoleService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/UserRoleService.java` hiện thực hóa lớp `UserRoleService` với annotation `@ApplicationScoped` và `@PreAuthorize("hasAnyRole('SystemAdmin','CenterAdmin')")`. Triển khai phương thức `updateUserRole(UUID userId, int newRoleId, UUID actingUserId)` thực hiện theo trình tự: (1) kiểm tra người thực hiện có phải SystemAdmin hay CenterAdmin hợp lệ thông qua `SecurityContext`, (2) kiểm tra sự tồn tại của user trong bảng `Users` ném `UserNotFoundException` nếu không tồn tại, (3) xác thực `newRoleId` thuộc tập {1, 2, 3, 4, 5} ném `InvalidRoleException` nếu ngoài phạm vi, (4) lưu `oldRoleId`, cập nhật cột `role_id` trong bảng `Users` và `updated_at`, (5) vô hiệu hóa phiên JWT hiện tại bằng cách thêm vào blacklist Redis với key `jwt:blacklist:<userId>` và TTL bằng thời gian còn lại của token, (6) ghi `AuditLogs` với `old_value` và `new_value` thông qua `AuditLogger`, (7) gửi Kafka event `user.role.changed` lên topic `user-events` với payload JSON chứa `userId`, `oldRoleId`, `newRoleId`, `changedBy`, `timestamp` để các service khác cập nhật cache. Xử lý trường hợp CenterAdmin chỉ được đổi role trong phạm vi trung tâm mình quản lý bằng cách kiểm tra `center_id` của user mục tiêu khớp với `center_id` của CenterAdmin đang thực hiện.

#### 📝 NHIỆM VỤ PHỤ 3.2: Kiểm thử phân quyền RBAC

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/UserRoleService.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/service/UserRoleServiceTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [EXC-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử đơn vị `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/service/UserRoleServiceTest.java` sử dụng JUnit 5 kết hợp Mockito 5.7.0. Tạo 8 test case: (1) `updateRole_bySystemAdmin_returnsSuccess` xác minh SystemAdmin thay đổi role thành công với `oldRoleId=5`, `newRoleId=2`, verify `AuditLogger` được gọi với action `ROLE_CHANGED`; (2) `updateRole_byCenterAdminWithinOwnCenter_returnsSuccess` xác minh CenterAdmin thay đổi role thành công trong trung tâm mình quản lý; (3) `updateRole_byCenterAdminOutsideOwnCenter_throwsAccessDeniedException` xác minh CenterAdmin cố gắng thay đổi user ở trung tâm khác bị từ chối với mã `CROSS_CENTER_FORBIDDEN`; (4) `updateRole_byManager_throwsAccessDeniedException` xác minh Manager thay đổi role bị từ chối với mã `INSUFFICIENT_PRIVILEGES`; (5) `updateRole_byTeacher_throwsAccessDeniedException` xác minh Teacher thay đổi role bị từ chối; (6) `updateRole_withInvalidRoleId_throwsConstraintViolationException` xác minh `roleId=6` hoặc `roleId=0` ném `ConstraintViolationException` trả về HTTP 400; (7) `updateRole_forNonExistentUser_throwsUserNotFoundException` xác minh user không tồn tại trả về HTTP 404; (8) `updateRole_blacklistsOldJwtToken` xác minh sau khi đổi role, JWT cũ bị thêm vào Redis blacklist và `JwtAuthFilter` từ chối token này trong request tiếp theo.

#### 📝 NHIỆM VỤ PHỤ 3.3: Tài liệu ma trận RBAC

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/architecture/phase-2-rbac-matrix.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tài liệu Markdown `./sources/docs/architecture/phase-2-rbac-matrix.md` mô tả ma trận phân quyền 5 cấp độ theo định dạng bảng gồm các cột: Role, Scope, CRUD Permissions, Special Permissions, Allowed Endpoints. Đính kèm sơ đồ Mermaid `flowchart` biểu diễn quan hệ kế thừa giữa các role SystemAdmin, CenterAdmin, Manager, Teacher, Student. Bao gồm danh sách endpoint mà từng role có thể truy cập với mã HTTP tương ứng. Ghi chú tuân thủ nguyên tắc least privilege và giải thích rằng CenterAdmin chỉ có hiệu lực trong phạm vi `center_id` của mình, không thể ảnh hưởng đến trung tâm khác. Tham chiếu các mã thẻ `[ARC-001]`, `[ARC-002]`, `[ARC-003]`, `[ARC-004]`, `[ARC-005]`.

### 🌤️ NGÀY 4: <!--DAY_HEADER_START-->REST API DANH SÁCH VÀ QUẢN LÝ TRUNG TÂM (PHẦN 1) - CONTROLLER VÀ SERVICE LÕI<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 4.1: Triển khai CenterController và danh sách

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/CenterController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-004], [REQ-005]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/CenterController.java` hiện thực hóa lớp `CenterController` với annotation `@RestController` và `@RequestMapping("/api/v1/centers")`. Triển khai 4 endpoint: (1) `GET /api/v1/centers` với `@PreAuthorize("isAuthenticated()")` trả về danh sách phân trang gồm Name, Address, TaxID, AdminContact thông qua `Pageable` với `page`, `size`, `sort` parameters mặc định `size=20`, `sort=name,asc`, gọi `CenterService.listCenters()`; (2) `POST /api/v1/centers` với `@PreAuthorize("hasRole('SystemAdmin')")` nhận `CenterRequest` với `@Valid`, gọi `CenterService.createCenter()`; (3) `PUT /api/v1/centers/{id}` với `@PreAuthorize("hasAnyRole('SystemAdmin','CenterAdmin')")` cập nhật thông tin trung tâm; (4) `DELETE /api/v1/centers/{id}` với `@PreAuthorize("hasRole('SystemAdmin')")` thực hiện soft delete bằng cách cập nhật cờ `is_deleted=true`. Áp dụng `@Valid` cho mọi request body để kích hoạt Bean Validation. Tích hợp `CenterService` với validation cho TaxID unique theo regex `^[0-9]{10,13}$` và email format `^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`. Trả về `ResponseEntity` với mã HTTP 200/201/204 tương ứng. Ghi audit log cho mọi thao tác CRUD thông qua `AuditLogger`.

#### 📝 NHIỆM VỤ PHỤ 4.2: Kiểm thử Center Controller

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/CenterController.java;./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/controller/CenterControllerTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-004], [REQ-005], [EXC-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử đơn vị `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/controller/CenterControllerTest.java` sử dụng JUnit 5 kết hợp Mockito 5.7.0 và RestAssured 5.4.0. Mock `CenterService` và `AuditLogger`. Tạo 7 test case: (1) `listCenters_byAuthenticatedUser_returns200WithPagination` xác minh lấy danh sách phân trang thành công với user bất kỳ đã xác thực, response chứa `content` mảng các trung tâm, `totalElements`, `totalPages`; (2) `createCenter_bySystemAdmin_returns201` xác minh SystemAdmin tạo trung tâm mới thành công với payload hợp lệ, trả về HTTP 201 và response body chứa `centerId`; (3) `createCenter_byManager_returns403` xác minh Manager cố tạo trung tâm bị từ chối với mã `INSUFFICIENT_PRIVILEGES`; (4) `createCenter_withDuplicateTaxId_returns409` xác minh tạo trung tâm với TaxID đã tồn tại trả về HTTP 409 với mã `TAX_ID_CONFLICT`; (5) `createCenter_withInvalidEmail_returns400` xác minh tạo trung tâm với email sai định dạng `invalid-email` trả về HTTP 400 với mảng `errors`; (6) `updateCenter_bySystemAdmin_returns200` xác minh cập nhật trung tâm thành công; (7) `deleteCenter_bySystemAdmin_returns204` xác minh xoá trung tâm thành công với soft delete. Sử dụng `@InjectMock` và `@QuarkusTest`.

#### 📝 NHIỆM VỤ PHỤ 4.3: Review mã nguồn Center

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-004], [REQ-005], [ARC-002], [NFR-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer thực hiện đánh giá chuyên sâu lớp `CenterService` tại đường dẫn `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterService.java` để phát hiện: (1) lỗ hổng SQL injection trong truy vấn tìm kiếm `findByNameContaining` bằng cách sử dụng JPQL `LIKE :pattern` với parameter binding, (2) tuân thủ nguyên tắc OWASP A03:2021 Injection, (3) khả năng race condition khi kiểm tra trùng lặp TaxID đồng thời giữa hai request tạo trung tâm - đề xuất sử dụng unique constraint kết hợp upsert pattern, (4) hiệu năng truy vấn khi danh sách trung tâm lớn - đề xuất sử dụng phân trang server-side với `Pageable` và index trên cột `name` đã có sẵn. Tạo báo cáo review với format: Vấn đề phát hiện, Mức độ nghiêm trọng, Đề xuất fix, File liên quan. Đảm bảo tất cả mã lỗi nghiệp vụ `EMAIL_ALREADY_EXISTS`, `TAX_ID_CONFLICT`, `CENTER_NOT_FOUND` được ánh xạ đúng HTTP status code thông qua `CenterExceptionHandler`.

### 🌤️ NGÀY 5: <!--DAY_HEADER_START-->GÁN/HUỶ GÁN CENTER ADMIN VÀ QUẢN LÝ LIÊN KẾT NGƯỜI DÙNG - TRUNG TÂM<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 5.1: Triển khai CenterAdminService

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-006], [ARC-002], [NFR-006]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminService.java` hiện thực hóa lớp `CenterAdminService` với annotation `@ApplicationScoped`. Triển khai phương thức `assignAdmin(UUID centerId, UUID userId, UUID actingUserId)` thực hiện: (1) xác thực `actingUserId` có role `SystemAdmin` thông qua `SecurityContext`, ném `AccessDeniedException` nếu không đủ quyền, (2) kiểm tra `centerId` tồn tại trong bảng `Centers` ném `CenterNotFoundException` nếu không, (3) kiểm tra `userId` tồn tại trong bảng `Users` ném `UserNotFoundException` nếu không, (4) cập nhật `role_id = 2` (CenterAdmin) trong bảng `Users` thông qua JPQL parameter binding, (5) lưu `center_id` vào bảng liên kết `CenterAdmins` với khóa chính tổng hợp `(center_id, user_id)`, (6) ghi audit log thông qua `AuditLogger` với action `CENTER_ADMIN_ASSIGNED`, (7) gửi Kafka event `center.admin.assigned` lên topic `center-events` với payload chứa `centerId`, `userId`, `assignedBy`, `timestamp`. Phương thức `unassignAdmin(centerId, userId, actingUserId)` thực hiện ngược lại: xoá bản ghi trong `CenterAdmins`, đặt `role_id = 5` (Student) trong `Users` nếu user không thuộc trung tâm nào khác, ghi audit log `CENTER_ADMIN_UNASSIGNED`, gửi Kafka event `center.admin.unassigned`. Toàn bộ thao tác phải sử dụng `@Transactional` đảm bảo ACID.

#### 📝 NHIỆM VỤ PHỤ 5.2: Kiểm thử gán admin

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminService.java;./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminServiceTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-006], [ARC-002], [EXC-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử đơn vị `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminServiceTest.java` sử dụng JUnit 5 kết hợp Mockito 5.7.0. Mock `CenterRepository`, `UserRepository`, `CenterAdminsRepository` và `KafkaProducer`. Tạo 6 test case: (1) `assignAdmin_bySystemAdmin_returnsSuccess` xác minh SystemAdmin gán Center Admin thành công, verify `UserRepository.updateRole` được gọi với `roleId=2`, `CenterAdminsRepository.save` được gọi, `KafkaProducer.send` được gọi với topic `center-events`; (2) `assignAdmin_byCenterAdmin_throwsAccessDeniedException` xác minh CenterAdmin cố gán admin khác bị từ chối với mã `INSUFFICIENT_PRIVILEGES`; (3) `assignAdmin_forNonExistentUser_throwsUserNotFoundException` xác minh gán user không tồn tại trả về HTTP 404; (4) `assignAdmin_forNonExistentCenter_throwsCenterNotFoundException` xác minh gán cho center không tồn tại trả về HTTP 404; (5) `assignAdmin_duplicateAssignment_throwsDataIntegrityViolationException` xác minh gán trùng lặp trả về HTTP 409 với mã `DUPLICATE_ADMIN_ASSIGNMENT`; (6) `unassignAdmin_removesAdminAndResetsRole` xác minh huỷ gán thành công, role chuyển về `Student` (roleId=5) nếu user không thuộc trung tâm nào khác, verify audit log `CENTER_ADMIN_UNASSIGNED` được ghi.

#### 📝 NHIỆM VỤ PHỤ 5.3: Tài liệu hợp đồng Center

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/api/user-center-contracts.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-004], [REQ-005], [REQ-006], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Cập nhật tệp tài liệu API tổng hợp `./sources/docs/api/user-center-contracts.md` bổ sung 3 endpoint mới: (1) `POST /api/v1/centers/{id}/admins` với payload `{ "userId": "UUID" }` trả về HTTP 200 với response chứa `centerId`, `userId`, `assignedAt`, (2) `DELETE /api/v1/centers/{id}/admins/{userId}` trả về HTTP 204, (3) danh sách endpoint CRUD trung tâm đầy đủ. Mô tả bảng mã lỗi 403 (INSUFFICIENT_PRIVILEGES, CROSS_CENTER_FORBIDDEN), 404 (CENTER_NOT_FOUND, USER_NOT_FOUND), 409 (TAX_ID_CONFLICT, DUPLICATE_ADMIN_ASSIGNMENT). Ghi chú về hiệu lực quyền hạn ngay sau khi gán (cache invalidation) và cách thức CenterAdmin chỉ có hiệu lực trong `center_id` của mình.

### 🌤️ NGÀY 6: <!--DAY_HEADER_START-->GLOBAL EXCEPTION HANDLER VÀ KIỂM TOÁN TẬP TRUNG<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 6.1: Triển khai Global Exception Handler

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[EXC-004], [NFR-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java` hiện thực hóa lớp `GlobalExceptionHandler` được đánh dấu `@RestControllerAdvice` xử lý tập trung các ngoại lệ: (1) `MethodArgumentNotValidException` trả về HTTP 400 với mảng `FieldErrorResponse` chứa `field`, `message`, `rejectedValue` cho từng trường vi phạm, (2) `ConstraintViolationException` trả về HTTP 400 với cùng cấu trúc, (3) `DataIntegrityViolationException` ánh xạ thành HTTP 409 với mã lỗi tương ứng `EMAIL_ALREADY_EXISTS`, `TAX_ID_CONFLICT`, `DUPLICATE_KEY` dựa trên phân tích `ConstraintViolation` từ exception, (4) `AuthenticationException` trả về HTTP 401 với mã `UNAUTHENTICATED`, (5) `AccessDeniedException` trả về HTTP 403 với mã `INSUFFICIENT_PRIVILEGES`, (6) `EntityNotFoundException` trả về HTTP 404 với mã `RESOURCE_NOT_FOUND`, (7) `Exception` mặc định trả về HTTP 500 nhưng không để lộ stack trace ra response. Mỗi response phải bao gồm các trường `timestamp` (ISO-8601), `status` (HTTP code), `errorCode` (mã nghiệp vụ), `message` (thông điệp thân thiện), `path` (đường dẫn request), `traceId` (UUID từ OpenTelemetry) để phục vụ observability.

#### 📝 NHIỆM VỤ PHỤ 6.2: Kiểm thử tích hợp Exception Handler

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandlerTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[EXC-004], [NFR-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử đơn vị `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandlerTest.java` sử dụng JUnit 5 kết hợp MockMvc. Tạo 7 test case kiểm tra: (1) `handleMethodArgumentNotValid_returns400WithFieldErrors` xác minh response 400 với mảng `errors` chứa trường `email` khi email sai định dạng `invalid-email`; (2) `handleDataIntegrityViolation_duplicateEmail_returns409` xác minh response 409 với mã `EMAIL_ALREADY_EXISTS`; (3) `handleAuthenticationException_returns401` xác minh response 401 với mã `UNAUTHENTICATED` khi thiếu token; (4) `handleAccessDeniedException_returns403` xác minh response 403 với mã `INSUFFICIENT_PRIVILEGES` khi không đủ quyền; (5) `handleEntityNotFoundException_returns404` xác minh response 404 với mã `USER_NOT_FOUND`; (6) `handleGenericException_returns500WithoutStackTrace` xác minh response 500 với thông điệp chung nhưng không lộ stack trace; (7) `allResponses_includeTraceId` xác minh mọi response đều có trường `traceId` để liên kết log với OpenTelemetry. Sử dụng `MockMvc` để thực thi controller thật kết hợp mock service layer.

#### 📝 NHIỆM VỤ PHỤ 6.3: Triển khai Audit Logger

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/audit/AuditLogger.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-006], [REQ-003], [REQ-006]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/audit/AuditLogger.java` hiện thực hóa lớp `AuditLogger` với annotation `@ApplicationScoped`, inject `AuditLogRepository` (Panache Repository). Tạo annotation `@AuditLogged(action = "...", targetEntity = "...")` cho phép ghi log kiểm toán tự động thông qua AOP aspect. Triển khai phương thức `log(UUID userId, String action, String targetEntity, UUID targetId, String oldValue, String newValue, String ipAddress, String userAgent)`: (1) lưu bản ghi vào bảng `AuditLogs` với đầy đủ trường theo schema đã định, (2) đồng thời ghi log có cấu trúc (JSON) ra console thông qua SLF4J với mức INFO để tích hợp với Google Cloud Logging, (3) sử dụng `@Transactional(propagation = Propagation.REQUIRES_NEW)` để đảm bảo thao tác ghi log không làm thất bại giao dịch chính, (4) hash chain chống tamper bằng cách tính `prev_hash` từ bản ghi trước đó. Tuân thủ `[NFR-006]` yêu cầu lưu trữ 1 năm.

### 🌤️ NGÀY 7: <!--DAY_HEADER_START-->ĐÁNH GIÁ TỔNG THỂ VÀ TÍCH HỢP CUỐI KỲ GIAI ĐOẠN 2<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 7.1: Review toàn diện giai đoạn 2

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtAuthFilter.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-003], [NFR-006], [EXC-004]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer thực hiện đánh giá tổng thể toàn bộ mã nguồn Giai đoạn 2 theo checklist: (1) Tuân thủ OWASP Top 10 - A01 Broken Access Control (kiểm tra quyền truy cập theo role thông qua `@PreAuthorize`), A03 Injection (parameterized queries với JPQL binding), A07 Identification and Authentication Failures (kiểm tra JWT validation, token expiry, blacklist), (2) Tuân thủ nguyên tắc SOLID và Clean Architecture, (3) Khả năng mở rộng và tái sử dụng của các service layer, (4) Hiệu năng truy vấn database và khả năng chịu tải. Lập báo cáo đánh giá chi tiết với định dạng bảng gồm mức độ nghiêm trọng (Critical/High/Medium/Low), mô tả, đề xuất fix, file liên quan. Tổng hợp toàn bộ mã lỗi nghiệp vụ đã được chuẩn hoá trong Giai đoạn 2: `EMAIL_ALREADY_EXISTS` (409), `TAX_ID_CONFLICT` (409), `INVALID_TOKEN` (401), `TOKEN_EXPIRED` (401), `INSUFFICIENT_PRIVILEGES` (403), `USER_NOT_FOUND` (404), `CENTER_NOT_FOUND` (404), `VALIDATION_FAILED` (400), mỗi mã có mô tả tiếng Việt rõ ràng cho developer và end-user.

#### 📝 NHIỆM VỤ PHỤ 7.2: Kiểm thử tích hợp liên service

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserCenterIntegrationTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [REQ-003], [REQ-006], [ARC-001], [ARC-002], [ARC-006], [NFR-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử tích hợp liên service `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserCenterIntegrationTest.java` sử dụng Testcontainers (PostgreSQL 16-alpine + Redis 7-alpine) và WireMock. Tạo 4 kịch bản: (1) `endToEnd_registerAssignRoleAccessCenter_succeeds` mô phỏng luồng hoàn chỉnh từ đăng ký user mới qua `POST /api/v1/users/register` → gán role CenterAdmin qua `PUT /api/v1/users/{id}/role` → truy cập endpoint center `GET /api/v1/centers` thành công với HTTP 200, (2) `endToEnd_roleChangeFromCenterAdminToStudent_blocksAccess` xác minh sau khi đổi role từ CenterAdmin về Student, endpoint center quản trị trả về HTTP 403, (3) `endToEnd_socialAuthGoogle_linksAndReusesAccount` mô phỏng đăng ký qua Google OAuth2 với `idToken` giả lập → liên kết social account → đăng nhập lần sau với cùng `idToken` không tạo user mới, (4) `endToEnd_auditLogsConsistentBetweenServices` xác minh đồng bộ audit log giữa user-service và center-service thông qua cùng bảng `AuditLogs` hoặc cơ chế chia sẻ event.

#### 📝 NHIỆM VỤ PHỤ 7.3: Tài liệu tổng kết giai đoạn 2

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/api/user-center-contracts.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [NFR-003], [NFR-006], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tổng hợp và cập nhật tài liệu tổng kết Giai đoạn 2 tại `./sources/docs/api/user-center-contracts.md` bao gồm: (1) Bảng tổng hợp 7 endpoint đã triển khai với mã trạng thái, mô tả, role yêu cầu, (2) Sơ đồ Mermaid `sequenceDiagram` mô tả luồng đăng ký email/password và cấp JWT, (3) Sơ đồ Mermaid `sequenceDiagram` mô tả luồng Social OAuth2 với 3 provider Firebase/Google/Facebook, (4) Sơ đồ Mermaid `sequenceDiagram` mô tả luồng gán role và vô hiệu hóa phiên, (5) Bảng mã lỗi chuẩn hoá với 8 mã `EMAIL_ALREADY_EXISTS`, `TAX_ID_CONFLICT`, `INVALID_TOKEN`, `TOKEN_EXPIRED`, `INSUFFICIENT_PRIVILEGES`, `USER_NOT_FOUND`, `CENTER_NOT_FOUND`, `VALIDATION_FAILED`, (6) Checklist tuân thủ OWASP Top 10 đã áp dụng với 10 mục kiểm tra, (7) Hướng dẫn cấu hình biến môi trường cho OAuth2 providers (`FIREBASE_API_KEY`, `GOOGLE_CLIENT_ID`, `FACEBOOK_APP_ID`), (8) Tài liệu chuyển giao cho Giai đoạn 3 với danh sách endpoint đã sẵn sàng tích hợp.