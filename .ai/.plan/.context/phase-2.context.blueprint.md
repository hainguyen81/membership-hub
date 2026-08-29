# Giai Đoạn 2: <!--PHASE_NAME_START-->Xây Dựng Xác Thực, Phân Quyền RBAC Và Quản Lý Trung Tâm<!--PHASE_NAME_END-->

## 📊 Kiểm Soát Tài Liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Bản Thiết Kế** | ARCH-20260829125322 |
| **Tên Dự Án** | membership-hub |
| **Giai Đoạn** | 2 |
| **Tên Giai Đoạn** | <!--PHASE_NAME_START-->Xây Dựng Xác Thực, Phân Quyền RBAC Và Quản Lý Trung Tâm<!--PHASE_NAME_END--> |
| **Mô Tả** | <!--PHASE_DESC_START-->Giai đoạn này tập trung kiến lập nền tảng bảo mật và quản trị nòng cốt của hệ thống Membership Hub, bao gồm toàn bộ luồng xác thực người dùng qua email mật khẩu và OAuth2 với Firebase, Google, Facebook kèm cấp phát JWT 15 phút và refresh token 7 ngày. Đồng thời, hệ thống phân quyền RBAC 5 cấp được thực thi thông qua bộ lọc bảo mật của Quarkus, đảm bảo phân tách quyền hạn chặt chẽ giữa System Admin, Center Admin, Manager, Teacher và Student. Module quản lý trung tâm cung cấp đầy đủ thao tác CRUD với cơ chế phát hiện và xử lý xung đột mã số thuế, hỗ trợ gán và thu hồi quyền Center Admin theo từng đơn vị. Bên cạnh đó, bộ tài liệu kiến trúc doanh nghiệp bao gồm blueprint tổng thể, ma trận phân quyền chi tiết và sổ tay vận hành được khởi tạo nhằm phục vụ công tác kiểm toán và bàn giao vận hành. Toàn bộ tài sản được truy vết bằng hệ thống thẻ TagID chuẩn doanh nghiệp, đảm bảo tính nhất quán và khả năng kiểm định chéo.<!--PHASE_DESC_END--> |
| **Phiên Bản** | 1.0 (Đường Cơ Sở) |
| **Thời Gian** | 2026/08/29 12:53:22 |
| **Tác Giả** | Kiến Trúc Sư Hệ Thống Doanh Nghiệp (SA Agent) |
| **Phê Duyệt** | Đang Chờ Đánh Giá Quản Trị Kỹ Thuật |

## 1. Phạm Vi Hoạt Động & Mục Tiêu Giai Đoạn

Giai đoạn 2 thực hiện bốn nhiệm vụ cốt lõi được phân bổ theo bảng tóm tắt đa giai đoạn: Nhiệm vụ 2 (đăng ký và xác thực người dùng), Nhiệm vụ 3 (phân quyền và quản lý vai trò), Nhiệm vụ 4 (quản lý trung tâm), và Nhiệm vụ 19 (tài liệu kỹ thuật doanh nghiệp). Phạm vi kéo dài từ Ngày 1 đến Ngày 5 với tổng cộng hai mươi nhiệm vụ phụ được phân bổ cho bốn tác nhân chuyên biệt: Coder chịu trách nhiệm xây dựng entity JPA, dịch vụ xác thực, bộ lọc bảo mật, dịch vụ quản lý trung tâm và các bộ xử lý ngoại lệ; Tester xây dựng bộ kiểm thử đơn vị và tích hợp cho các luồng nghiệp vụ trọng yếu; Reviewer thực hiện đánh giá mã tĩnh, kiểm định logic bảo mật và tuân thủ chuẩn OWASP; Doc soạn thảo tài liệu blueprint kiến trúc, ma trận phân quyền RBAC và sổ tay vận hành.

Các tài sản kỹ thuật bắt buộc phải sinh ra bao gồm: toàn bộ thực thể JPA cho User, Role, Center cùng các DTO yêu cầu và phản hồi, dịch vụ AuthService xử lý đăng ký, đăng nhập và cấp phát JWT, dịch vụ SocialAuthService tích hợp OAuth2 với ba nhà cung cấp, bộ lọc RbacFilter thực thi ma trận phân quyền năm cấp, CenterService xử lý CRUD trung tâm và gán Center Admin, tập lệnh di trú V2_1 bổ sung dữ liệu hạt giống cho bảng Roles kèm tài khoản System Admin mặc định, các bộ xử lý ngoại lệ toàn cục cho phản hồi lỗi chuẩn hóa, cùng ba tài liệu kỹ thuật doanh nghiệp gồm blueprint tổng thể, sổ tay vận hành và ma trận phân quyền. Toàn bộ tài sản phải được gắn thẻ truy xuất theo hệ thống TagID `[REQ-001]`, `[REQ-002]`, `[REQ-003]`, `[REQ-004]`, `[REQ-005]`, `[REQ-006]`, `[ARC-001]`, `[ARC-002]`, `[ARC-003]`, `[ARC-004]`, `[ARC-005]`, `[ARC-006]`, `[EXC-004]`, `[DOC-001]` để đảm bảo khả năng truy vết đầy đủ.

## 2. Phạm Vi Kỹ Thuật Cho Phép & Ranh Giới Thư Mục

Danh sách tệp vật lý và điểm cuối được phép sinh ra trong giai đoạn này:

* `./sources/backend/user-service/pom.xml` [ARC-000]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/User.java` [REQ-001], [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/Role.java` [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/RegistrationRequest.java` [REQ-001], [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/LoginRequest.java` [REQ-001], [ARC-006], [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/SocialAuthRequest.java` [REQ-002], [ARC-006]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/AuthResponse.java` [REQ-001], [REQ-002], [ARC-006]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/dto/RoleUpdateRequest.java` [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuthService.java` [REQ-001], [REQ-002], [ARC-006], [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/SocialAuthService.java` [REQ-002], [ARC-006]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/UserService.java` [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/JwtTokenProvider.java` [REQ-001], [REQ-002], [ARC-006]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/PasswordEncoderService.java` [REQ-001], [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/InvalidCredentialsException.java` [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/DuplicateEmailException.java` [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/WeakPasswordException.java` [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/InvalidProviderTokenException.java` [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/UnauthorizedRoleException.java` [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java` [EXC-004]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/RbacFilter.java` [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SecurityContextProducer.java` [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/repository/UserRepository.java` [REQ-001], [REQ-003], [DAT-001]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/repository/RoleRepository.java` [REQ-003], [DAT-008]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/AuthController.java` [REQ-001], [REQ-002], [ARC-006]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/UserController.java` [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* `./sources/backend/user-service/src/main/resources/db/migration/V2_1__seed_roles_and_admin.sql` [DAT-008], [ARC-001]
* `./sources/backend/center-service/pom.xml` [ARC-000]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/Center.java` [REQ-004], [REQ-005], [REQ-006], [DAT-002]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/CenterCreateRequest.java` [REQ-005], [EXC-004]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/CenterUpdateRequest.java` [REQ-005], [EXC-004]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/CenterAdminAssignRequest.java` [REQ-006]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/dto/CenterResponse.java` [REQ-004]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterService.java` [REQ-004], [REQ-005], [REQ-006]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/repository/CenterRepository.java` [REQ-004], [REQ-005], [DAT-002]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/exception/DuplicateTaxIdException.java` [REQ-005], [EXC-004]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/exception/CenterNotFoundException.java` [REQ-005], [EXC-004]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/exception/CenterAccessDeniedException.java` [REQ-006], [EXC-004]
* `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/controller/CenterController.java` [REQ-004], [REQ-005], [REQ-006]
* `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/AuthServiceTest.java` [REQ-001], [REQ-002], [ARC-006], [EXC-004]
* `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/SocialAuthIntegrationTest.java` [REQ-002], [ARC-006], [EXC-004]
* `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/RbacFilterTest.java` [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [EXC-004]
* `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserServicesTestSuite.java` [ARC-000], [REQ-001], [DAT-001]
* `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/CenterServiceIntegrationTest.java` [REQ-004], [REQ-005], [REQ-006], [EXC-004]
* `./sources/docs/architecture/blueprint.md` [DOC-001], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006]
* `./sources/docs/operations/runbook.md` [DOC-001]
* `./sources/docs/architecture/rbac-matrix.md` [DOC-001], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]

* **RÀNG BUỘC BẮT BUỘC VỀ BIỂU MẪU NỀN TẢNG**:
  - Cấu trúc package Java phải tuân thủ nghiêm ngặt quy ước `org.nlh4j.membershiphub.<tên-dịch-vụ>` cho mọi tệp nguồn.
  - Mọi thay đổi schema cơ sở dữ liệu phải thông qua tập tin migration Flyway versioned, cấm sửa đổi trực tiếp.
  - Tất cả REST endpoint phải khai báo JSON contract rõ ràng với request/response schema và HTTP status code tiêu chuẩn.
  - Cam kết OWASP Top 10: chuẩn bị câu lệnh parameterized chống SQL injection, escape output chống XSS.

## 3. Chỉ Thị Chức Năng Chuyên Biệt Cho Tác Nhân Phụ

*   **Coder**: Đóng vai trò Nhà Phát Triển Ứng Dụng Cao Cấp. Chịu trách nhiệm triển khai mã nguồn ứng dụng thuần túy trên cả dịch vụ backend, bao gồm entity JPA, dịch vụ nghiệp vụ, bộ điều khiển REST, bộ lọc bảo mật và bộ xử lý ngoại lệ. Bị cấm viết bộ kiểm thử hoặc biểu mẫu hạ tầng.

* **Tester**: Đóng vai trò Trưởng Nhóm Kiểm Thử/Đảm Bảo Chất Lượng. Chuyên về kỹ thuật bộ kiểm thử, xác thực và cổng chất lượng. Chịu trách nhiệm sinh JUnit, kiểm thử tích hợp, kiểm thử tự động đầu-cuối và kịch bản xác thực hiệu năng. Bị cấm sửa đổi mã nguồn sản phẩm. Nếu nhiệm vụ phụ liên quan đến phạm vi tích hợp tổng thể hoặc đầu-cuối mà không thể khoanh vùng một tệp mã nguồn cụ thể, phải xuất chính xác chuỗi ký tự `INTEGRATION_SCOPE` làm tham số đầu tiên của cặp phân tách bằng dấu chấm phẩy.

* **Doc**: Đóng vai trò Chuyên Viên Viết Tài Liệu Kỹ Thuật và Kiến Trúc Sư Hệ Thống Doanh Nghiệp. Chuyên về biên soạn tài liệu đặc tả kỹ thuật toàn diện, tài liệu tham chiếu schema, blueprint hệ thống và danh mục kiến trúc doanh nghiệp phù hợp với các lớp topology dự án đang hoạt động. Mỗi tệp tài liệu kỹ thuật được sinh ra phải được liệt kê dưới dạng thực thể đường dẫn tệp rõ ràng kết thúc bằng phần mở rộng `.md` và nằm hoàn toàn trong sơ đồ lưu trữ tập trung: `./sources/docs/`.

*   **Reviewer**: Chịu trách nhiệm xác minh trình biên dịch, cổng phân tích tĩnh và vá lỗi phòng thủ. Chuyên về đánh giá chất lượng mã, giải quyết lỗi biên dịch, sửa lỗ hổng bảo mật OWASP và xử lý các blocker cổng chất lượng SonarQube.

*   **Docker**: Chuyên biệt về container hóa, kỹ thuật Dockerfile đa giai đoạn, tối ưu gói và đẩy tài sản ảnh ứng dụng đã xác minh lên DockerHub.

*   **GCP**: Chuyên về tự động hóa đám mây trong Google Cloud Platform. Chịu trách nhiệm xây dựng và đẩy ảnh lên Google Cloud Artifact Registry (GCR), điều phối môi trường container nguyên bản trên Google Cloud Run.

*   **GKE**: Chuyên về điều phối container sản xuất bên trong Google Kubernetes Engine. Chịu trách nhiệm xây dựng manifest triển khai Kubernetes, điều khiển định tuyến, cấu hình HPA, biểu đồ Helm và triển khai tải công việc microservice vào cụm GKE đang hoạt động.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn

Giai đoạn 2 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: một trăm phần trăm endpoint xác thực (đăng ký, đăng nhập, OAuth2 với ba nhà cung cấp, làm mới token) hoạt động đúng theo đặc tả với phản hồi JWT 15 phút và refresh token 7 ngày; bộ lọc RbacFilter thực thi chính xác ma trận phân quyền 5 cấp thông qua annotation `@RbacRequired`; dịch vụ CenterService xử lý đầy đủ thao tác CRUD với cơ chế phát hiện trùng lặp TaxID và trả về mã lỗi 409 theo chuẩn HTTP; tập lệnh di trú V2_1 bổ sung thành công năm vai trò RBAC và tài khoản System Admin mặc định; toàn bộ bộ xử lý ngoại lệ toàn cục chuẩn hóa phản hồi lỗi với mã lỗi và thông điệp bản địa hóa tiếng Việt; bộ kiểm thử đơn vị và tích hợp phủ sóng tối thiểu 85 phần trăm các luồng nghiệp vụ trọng yếu; ba tài liệu kỹ thuật (blueprint, sổ tay vận hành, ma trận phân quyền) được soạn thảo đầy đủ với sơ đồ Mermaid minh họa; một trăm phần trăm mã TagID `[REQ-001]`, `[REQ-002]`, `[REQ-003]`, `[REQ-004]`, `[REQ-005]`, `[REQ-006]`, `[ARC-001]` đến `[ARC-006]`, `[EXC-004]`, `[DOC-001]` được ánh xạ chính xác trong báo cáo đánh giá cuối giai đoạn. Mọi vi phạm chuẩn OWASP Top 10 phải được phát hiện và khắc phục trong quá trình review.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->THIẾT LẬP MODULE XÁC THỰC VÀ MA TRẬN RBAC<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 1.1: Cấu hình module User-Service và entity User
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/User.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [REQ-001], [REQ-003], [DAT-001]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo module Maven con `user-service` kế thừa từ `pom.xml` gốc, khai báo dependency Quarkus RESTEasy Reactive, Hibernate ORM Panache, JWT SmallRye, BCrypto. Đồng thời tạo entity JPA `User.java` ánh xạ bảng `users` với các trường `user_id`, `email`, `password_hash`, `full_name`, `role_id`, `provider`, `created_at`, `updated_at`; định nghĩa quan hệ `@ManyToOne` với `Role.java`, áp dụng ràng buộc unique trên cặp (email, provider) theo Tag [DAT-001]. Đảm bảo tuân thủ nguyên tắc OWASP bằng cách sử dụng prepared statement thông qua Panache, không nối chuỗi SQL thô.

* **Database Schema DDL SQL Specification [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi hạ tầng cơ sở dữ liệu trong nhiệm vụ phụ này; bảng users đã được tạo tại Giai đoạn 1
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX]:** <!--START_API_CONTRACT-->
```java
package org.nlh4j.membershiphub.userservice;

// Quarkus User entity - Phase 2 DAY 1 SUB-TASK 1
// Tags: [REQ-001], [REQ-003], [DAT-001]
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uq_users_email_provider", columnNames = {"email", "provider"})
})
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "user_id", columnDefinition = "uuid")
    public UUID userId;

    @Column(name = "email", nullable = false, length = 255)
    public String email;

    @Column(name = "password_hash", nullable = false, length = 60, columnDefinition = "char(60)")
    public String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    public String fullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_users_role"))
    public Role role;

    @Column(name = "provider", nullable = false, length = 20, columnDefinition = "varchar(20)")
    public String provider = "local";

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.userservice.exception;

// InvalidCredentialsException - Phase 2 DAY 1
// Tags: [EXC-004]
public class InvalidCredentialsException extends RuntimeException {
    private final String errorCode = "INVALID_CREDENTIALS";

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 1.2: Kiểm thử tích hợp build cho User-Service
##### Tác Nhân Được Phân Công: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserServicesTestSuite.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [REQ-001], [DAT-001]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng script kiểm thử tích hợp Maven build pipeline cho `user-service` xác nhận khả năng biên dịch cross-module, xác minh rằng entity `User` ánh xạ chính xác sang bảng `users`, kiểm tra các ràng buộc unique constraint (email, provider) được sinh ra đúng. Bao gồm test hồi quy cho việc load classpath và dependency resolution từ parent `pom.xml`. Sử dụng Testcontainers PostgreSQL 16 để xác minh schema thực tế.

* **Database Schema DDL SQL Specification [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ phụ này
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 1.3: Rà soát mã nguồn và chuẩn hóa entity User
##### Tác Nhân Được Phân Công: Reviewer
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/User.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [REQ-003], [DAT-001]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Rà soát entity `User.java` đảm bảo tuân thủ quy ước Java Enterprise (đặt tên trường `camelCase` nhưng ánh xạ `snake_case` qua `@Column`), xác nhận rằng trường `passwordHash` sử dụng `char(60)` cho bcrypt, đảm bảo lazy loading đúng cách cho quan hệ `Role`, xác minh annotation `@PreUpdate` cập nhật `updatedAt` tự động. Đề xuất chiến lược fix nếu phát hiện bottleneck truy vấn và đảm bảo không có SQL injection vector.

#### 📝 NHIỆM VỤ PHỤ 1.4: Soạn thảo tài liệu ma trận phân quyền RBAC
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/architecture/rbac-matrix.md

* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Soạn thảo tài liệu kỹ thuật doanh nghiệp mô tả chi tiết ma trận RBAC 5 cấp, bao gồm bảng ánh xạ quyền hạn theo từng vai trò (System Admin, Center Admin, Manager, Teacher, Student), sơ đồ luồng phân quyền dạng Mermaid, các ràng buộc truy cập chéo trung tâm, và quy trình leo thang khi vi phạm. Tài liệu phải ở định dạng Markdown tiêu chuẩn với các tiêu đề phân cấp rõ ràng.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->XÂY DỰNG AUTHSERVICE, JWTTOKENPROVIDER VÀ CÁC DTO XÁC THỰC<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 2.1: Triển khai AuthService và JwtTokenProvider
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuthService.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [REQ-002], [ARC-006]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng `AuthService.java` xử lý logic đăng ký và đăng nhập, mã hóa mật khẩu bằng BCrypt (cost factor 10), validate định dạng email và độ mạnh mật khẩu theo Tag [REQ-001], tạo JWT access token 15 phút và refresh token 7 ngày thông qua `JwtTokenProvider` sử dụng thuật toán HS256 với secret key từ biến môi trường. Tích hợp `PasswordEncoderService` để so sánh hash an toàn. Mọi truy vấn cơ sở dữ liệu phải sử dụng prepared statement thông qua Panache.

* **API and Event Routing Contracts [DAT-XXX]:** <!--START_API_CONTRACT-->
```java
package org.nlh4j.membershiphub.userservice;

// AuthService - Phase 2 DAY 2 SUB-TASK 1
// Tags: [REQ-001], [REQ-002], [ARC-006], [EXC-004]
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class AuthService {

    @Inject UserRepository userRepository;
    @Inject RoleRepository roleRepository;
    @Inject PasswordEncoderService passwordEncoder;
    @Inject JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegistrationRequest request) {
        validateEmail(request.getEmail());
        validatePassword(request.getPassword());
        if (!request.isTermsAccepted()) {
            throw new InvalidCredentialsException("Điều khoản chưa được chấp nhận");
        }
        if (userRepository.existsByEmailAndProvider(request.getEmail(), "local")) {
            throw new DuplicateEmailException("Email đã được đăng ký");
        }
        User user = new User();
        user.email = request.getEmail();
        user.passwordHash = passwordEncoder.encode(request.getPassword());
        user.fullName = request.getFullName();
        user.role = roleRepository.findById(5);
        user.provider = "local";
        userRepository.persist(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndProvider(request.getEmail(), "local")
            .orElseThrow(() -> new InvalidCredentialsException("Thông tin đăng nhập không hợp lệ"));
        if (!passwordEncoder.matches(request.getPassword(), user.passwordHash)) {
            throw new InvalidCredentialsException("Thông tin đăng nhập không hợp lệ");
        }
        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidCredentialsException("Refresh token không hợp lệ");
        }
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId);
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(900);
        response.setTokenType("Bearer");
        response.setUserId(user.userId.toString());
        response.setRole(user.role.name);
        return response;
    }

    private void validateEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") || email.length() > 255) {
            throw new InvalidCredentialsException("Định dạng email không hợp lệ");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8
            || !password.matches(".*[A-Z].*")
            || !password.matches(".*[a-z].*")
            || !password.matches(".*[0-9].*")
            || !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new WeakPasswordException("Mật khẩu không đáp ứng yêu cầu về độ mạnh");
        }
    }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.userservice.exception;

// DuplicateEmailException - Phase 2 DAY 2
// Tags: [EXC-004]
public class DuplicateEmailException extends RuntimeException {
    private final String errorCode = "EMAIL_DUPLICATE";

    public DuplicateEmailException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 2.2: Kiểm thử đơn vị AuthService
##### Tác Nhân Được Phân Công: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/AuthService.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/AuthServiceTest.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [REQ-002], [ARC-006], [EXC-004]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Viết bộ test JUnit5 + Mockito cho `AuthService` xác minh các kịch bản: đăng ký thành công với email hợp lệ và mật khẩu mạnh, từ chối đăng ký khi email trùng lặp, từ chối khi mật khẩu yếu (thiếu chữ hoa/thường/số/ký tự đặc biệt), đăng nhập thành công với credentials đúng, từ chối khi sai mật khẩu, refresh token hợp lệ được cấp access token mới. Sử dụng `@ParameterizedTest` để phủ nhiều trường hợp biên.

* **Database Schema DDL SQL Specification [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ phụ này
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 2.3: Đánh giá bảo mật logic xác thực
##### Tác Nhân Được Phân Công: Reviewer
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/JwtTokenProvider.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [ARC-006], [NFR-003]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Kiểm tra `JwtTokenProvider` đảm bảo secret key không hardcode, sử dụng thuật toán ký HS256 an toàn, xác minh TTL access token đúng 900 giây và refresh token đúng 604800 giây, kiểm tra tính nguyên tử khi cấp token (không có race condition), đảm bảo exception khi token không hợp lệ được ném đúng cách. Đánh giá tuân thủ OWASP A02 (Cryptographic Failures) và A07 (Identification and Authentication Failures).

#### 📝 NHIỆM VỤ PHỤ 2.4: Cập nhật tài liệu Blueprint kiến trúc
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/architecture/blueprint.md

* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [ARC-006]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Bổ sung vào blueprint kiến trúc doanh nghiệp phần mô tả luồng xác thực chi tiết: biểu đồ tuần tự Mermaid cho quy trình đăng ký/đăng nhập, sơ đồ Mermaid cho OAuth2 flow với Firebase/Google/Facebook, cấu trúc JWT token và claim definitions, bảng mapping endpoint với Tag ID, chính sách mã hóa TLS 1.3 tại chỗ theo NFR-003.

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->TÍCH HỢP SOCIAL AUTHENTICATION VỚI FIREBASE, GOOGLE VÀ FACEBOOK<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 3.1: Triển khai SocialAuthService và xử lý OAuth2
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/SocialAuthService.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [ARC-006]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng `SocialAuthService` hỗ trợ xác thực qua 3 nhà cung cấp Firebase, Google, Facebook. Sử dụng client HTTP `java.net.http.HttpClient` để gọi endpoint verify token từ mỗi provider, trích xuất thông tin email và profile picture, tạo hoặc cập nhật user local với provider tương ứng, cấp JWT token theo chuẩn chung. Áp dụng cache token verification trong 5 phút để giảm tải. Mọi cuộc gọi HTTP ra bên ngoài phải sử dụng TLS 1.3 theo NFR-003.

* **API and Event Routing Contracts [DAT-XXX]:** <!--START_API_CONTRACT-->
```java
package org.nlh4j.membershiphub.userservice;

// SocialAuthService - Phase 2 DAY 3 SUB-TASK 1
// Tags: [REQ-002], [ARC-006]
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@ApplicationScoped
public class SocialAuthService {

    @Inject UserRepository userRepository;
    @Inject RoleRepository roleRepository;
    @Inject JwtTokenProvider jwtTokenProvider;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Transactional
    public AuthResponse authenticateSocial(SocialAuthRequest request, String provider) {
        SocialUserInfo userInfo = verifyProviderToken(request.getProviderToken(), provider);
        if (userInfo == null) {
            throw new InvalidProviderTokenException("Token từ nhà cung cấp không hợp lệ");
        }
        User user = userRepository.findByEmailAndProvider(userInfo.getEmail(), provider)
            .orElseGet(() -> createSocialUser(userInfo, provider));
        if (userInfo.getProfilePicture() != null) {
            user.profilePictureUrl = userInfo.getProfilePicture();
        }
        userRepository.persist(user);
        return buildAuthResponse(user);
    }

    private SocialUserInfo verifyProviderToken(String token, String provider) {
        String verificationUrl = switch (provider) {
            case "firebase" -> "https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=" + System.getenv("FIREBASE_API_KEY");
            case "google" -> "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;
            case "facebook" -> "https://graph.facebook.com/debug_token?input_token=" + token + "&access_token=" + System.getenv("FB_APP_TOKEN");
            default -> throw new InvalidProviderTokenException("Nhà cung cấp không được hỗ trợ: " + provider);
        };
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(verificationUrl))
                .GET()
                .timeout(java.time.Duration.ofSeconds(5))
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseUserInfo(response.body(), provider);
            }
        } catch (Exception e) {
            // Ghi log lỗi và ném ngoại lệ
        }
        return null;
    }

    private User createSocialUser(SocialUserInfo info, String provider) {
        User user = new User();
        user.email = info.getEmail();
        user.fullName = info.getFullName();
        user.provider = provider;
        user.role = roleRepository.findById(5);
        user.passwordHash = "";
        return user;
    }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.userservice.exception;

// InvalidProviderTokenException - Phase 2 DAY 3
// Tags: [EXC-004]
public class InvalidProviderTokenException extends RuntimeException {
    private final String errorCode = "INVALID_PROVIDER_TOKEN";

    public InvalidProviderTokenException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 3.2: Kiểm thử tích hợp luồng Social Auth
##### Tác Nhân Được Phân Công: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/SocialAuthIntegrationTest.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [ARC-006], [EXC-004]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng test tích hợp sử dụng WireMock để giả lập phản hồi từ Firebase, Google, Facebook API. Kiểm thử kịch bản: token hợp lệ từ mỗi provider tạo/cập nhật user đúng cách, token hết hạn trả về lỗi `INVALID_PROVIDER_TOKEN`, user mới được tạo với role Student mặc định, JWT được cấp với claim `provider` chính xác.

* **Database Schema DDL SQL Specification [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ phụ này
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 3.3: Rà soát logic OAuth2 và xử lý provider
##### Tác Nhân Được Phân Công: Reviewer
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/SocialAuthService.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [ARC-006], [NFR-003]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xác minh rằng `SocialAuthService` không lưu trữ token gốc từ provider, chỉ trích xuất thông tin cần thiết (email, name, picture), kiểm tra timeout HTTP client được cấu hình hợp lý (5 giây), đảm bảo xử lý race condition khi hai request social auth đồng thời cho cùng email, đề xuất fix strategy nếu phát hiện điểm nghẽn hiệu năng. Đánh giá tuân thủ OWASP A03 (Injection) khi parse JSON response.

#### 📝 NHIỆM VỤ PHỤ 3.4: Cập nhật tài liệu luồng xác thực
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/architecture/blueprint.md

* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [ARC-006]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Bổ sung sơ đồ tuần tự Mermaid cho 3 luồng OAuth2 (Firebase, Google, Facebook), bảng so sánh claim structure giữa các provider, hướng dẫn cấu hình API key cho từng provider trong biến môi trường, tài liệu xử lý edge case khi provider trả về email null.

### 🌤️ NGÀY 4: <!--DAY_HEADER_START-->TRIỂN KHAI RBAC FILTER, QUẢN LÝ VAI TRÒ VÀ PHÂN QUYỀN<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 4.1: Xây dựng RbacFilter và Security Context
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/RbacFilter.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Tạo `RbacFilter` triển khai `ContainerRequestFilter` của JAX-RS, sử dụng `@Priority(Priorities.AUTHORIZATION)` và annotation `@RbacRequired` tùy chỉnh để khai báo role cần thiết cho từng endpoint. Filter phân tích JWT token, trích xuất role, so sánh với annotation, ném `UnauthorizedRoleException` nếu không khớp. Tích hợp với `SecurityContextProducer` để cung cấp `SecurityContext` cho CDI. Đảm bảo thực thi nguyên tắc phân quyền tối thiểu (least privilege) theo OWASP A01.

* **API and Event Routing Contracts [DAT-XXX]:** <!--START_API_CONTRACT-->
```java
package org.nlh4j.membershiphub.userservice.security;

// RbacFilter - Phase 2 DAY 4 SUB-TASK 1
// Tags: [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.lang.reflect.Method;
import java.util.Arrays;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class RbacFilter implements ContainerRequestFilter {

    @Inject JsonWebToken jwt;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        Method resourceMethod = getResourceMethod(requestContext);
        if (resourceMethod == null) return;

        RbacRequired annotation = resourceMethod.getAnnotation(RbacRequired.class);
        if (annotation == null) {
            annotation = resourceMethod.getDeclaringClass().getAnnotation(RbacRequired.class);
        }
        if (annotation == null) return;

        String[] requiredRoles = annotation.value();
        String userRole = jwt.getClaim("role");
        if (userRole == null || !Arrays.asList(requiredRoles).contains(userRole)) {
            requestContext.abortWith(
                jakarta.ws.rs.core.Response.status(403)
                    .entity("{\"errorCode\":\"INSUFFICIENT_PRIVILEGES\",\"message\":\"Vai trò " + userRole + " không được phép truy cập\"}")
                    .build()
            );
        }
    }

    private Method getResourceMethod(ContainerRequestContext ctx) {
        var resourceMethod = ctx.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
        if (resourceMethod instanceof org.jboss.resteasy.core.ResourceMethodInvoker invoker) {
            return invoker.getMethod();
        }
        return null;
    }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.userservice.exception;

// UnauthorizedRoleException - Phase 2 DAY 4
// Tags: [EXC-004]
public class UnauthorizedRoleException extends RuntimeException {
    private final String errorCode = "INSUFFICIENT_PRIVILEGES";

    public UnauthorizedRoleException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 4.2: Kiểm thử đơn vị RbacFilter
##### Tác Nhân Được Phân Công: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/RbacFilter.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/RbacFilterTest.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [EXC-004]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Viết bộ kiểm thử JUnit5 cho `RbacFilter` xác minh: System Admin có thể truy cập mọi endpoint, Center Admin chỉ truy cập được endpoint trong phạm vi trung tâm của mình, Manager có quyền tạo thông báo và quản lý học viên, Teacher chỉ xem được khóa học được phân công, Student chỉ truy cập endpoint cá nhân. Sử dụng `@QuarkusTest` với JWT token giả lập thông qua `Jwt.issuer()` builder.

* **Database Schema DDL SQL Specification [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ phụ này
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 4.3: Phân tích thiết kế phân quyền đa cấp
##### Tác Nhân Được Phân Công: Reviewer
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/UserService.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Đánh giá logic phân quyền trong `UserService.updateUserRole`, đảm bảo rằng chỉ System Admin mới có thể thay đổi role thành System Admin, kiểm tra cơ chế audit log khi role được thay đổi (theo NFR-006), xác minh quyền Center Admin bị giới hạn trong trung tâm được gán, đề xuất tối ưu hóa caching cho permission check. Đánh giá tuân thủ OWASP A01 (Broken Access Control) và ngăn chặn IDOR.

#### 📝 NHIỆM VỤ PHỤ 4.4: Cập nhật tài liệu vận hành
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/operations/runbook.md

* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [ARC-001]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Bổ sung sổ tay vận hành các quy trình: cách cấp/thu hồi quyền System Admin, quy trình khôi phục khi Center Admin bị khóa tài khoản, checklist audit role hàng tháng, biểu mẫu yêu cầu thay đổi role, dashboard giám sát số lượng user theo từng role. Tài liệu phải bằng tiếng Việt và sử dụng định dạng Markdown chuẩn.

### 🌤️ NGÀY 5: <!--DAY_HEADER_START-->XÂY DỰNG MODULE CENTER-SERVICE VÀ QUẢN LÝ TRUNG TÂM<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 5.1: Triển khai CenterService và Entity Center kèm migration seed
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterService.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-004], [REQ-005], [REQ-006], [DAT-002]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng `CenterService` xử lý danh sách, CRUD trung tâm, gán/hủy gán Center Admin. Validate trường name (max 100), address (max 255), tax_id (10-13 chữ số, unique), contact phone/email optional theo regex. Khi tạo trung tâm mới, kiểm tra trùng lặp tax_id bằng query database trước khi insert, ném `DuplicateTaxIdException` nếu xung đột. Gán Center Admin cập nhật role user thành Center Admin kèm centerId. Đồng thời tạo tập tin migration V2_1 bổ sung dữ liệu hạt giống cho 5 vai trò RBAC và tài khoản System Admin mặc định.

* **Database Schema DDL SQL Specification [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- =====================================================================
-- V2_1__seed_roles_and_admin.sql
-- Bổ sung dữ liệu hạt giống vai trò RBAC và tài khoản System Admin
-- =====================================================================

INSERT INTO roles (role_id, name, description) VALUES (1, 'System Admin', 'Toàn quyền trên toàn hệ thống');
INSERT INTO roles (role_id, name, description) VALUES (2, 'Center Admin', 'Toàn quyền trong trung tâm được gán');
INSERT INTO roles (role_id, name, description) VALUES (3, 'Manager', 'Quản lý cấp dưới với quyền hạn chế');
INSERT INTO roles (role_id, name, description) VALUES (4, 'Teacher', 'Chỉ xem lịch giảng dạy được phân công');
INSERT INTO roles (role_id, name, description) VALUES (5, 'Student', 'Duyệt khóa học và quản lý thẻ cá nhân');

INSERT INTO users (user_id, email, password_hash, full_name, role_id, provider, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin@membershiphub.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Quản trị viên hệ thống mặc định',
    1,
    'local',
    now(),
    now()
);

CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_centers_tax_id ON centers(tax_id);
CREATE UNIQUE INDEX uq_users_email_provider ON users(email, provider);
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX]:** <!--START_API_CONTRACT-->
```java
package org.nlh4j.membershiphub.centerservice;

// CenterService - Phase 2 DAY 5 SUB-TASK 1
// Tags: [REQ-004], [REQ-005], [REQ-006], [DAT-002], [EXC-004]
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CenterService {

    @Inject CenterRepository centerRepository;

    @Transactional
    public CenterResponse createCenter(CenterCreateRequest request) {
        validateName(request.getName());
        validateAddress(request.getAddress());
        validateTaxId(request.getTaxId());
        if (centerRepository.existsByTaxId(request.getTaxId())) {
            throw new DuplicateTaxIdException("Mã số thuế đã tồn tại trong hệ thống");
        }
        Center center = new Center();
        center.name = request.getName();
        center.address = request.getAddress();
        center.taxId = request.getTaxId();
        center.contactPhone = request.getContactPhone();
        center.contactEmail = request.getContactEmail();
        centerRepository.persist(center);
        return mapToResponse(center);
    }

    @Transactional
    public CenterResponse updateCenter(UUID centerId, CenterUpdateRequest request) {
        Center center = centerRepository.findById(centerId);
        if (center == null) {
            throw new CenterNotFoundException("Không tìm thấy trung tâm");
        }
        if (request.getName() != null) {
            validateName(request.getName());
            center.name = request.getName();
        }
        if (request.getAddress() != null) {
            validateAddress(request.getAddress());
            center.address = request.getAddress();
        }
        center.contactPhone = request.getContactPhone();
        center.contactEmail = request.getContactEmail();
        centerRepository.persist(center);
        return mapToResponse(center);
    }

    @Transactional
    public void deleteCenter(UUID centerId) {
        Center center = centerRepository.findById(centerId);
        if (center == null) {
            throw new CenterNotFoundException("Không tìm thấy trung tâm");
        }
        centerRepository.delete(center);
    }

    public List<CenterResponse> listCenters() {
        return centerRepository.listAll().stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional
    public void assignCenterAdmin(UUID centerId, UUID userId) {
        Center center = centerRepository.findById(centerId);
        if (center == null) {
            throw new CenterNotFoundException("Không tìm thấy trung tâm");
        }
        center.adminUserId = userId;
        centerRepository.persist(center);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > 100) {
            throw new InvalidCenterDataException("Tên trung tâm phải có độ dài từ 1 đến 100 ký tự");
        }
    }

    private void validateAddress(String address) {
        if (address == null || address.isBlank() || address.length() > 255) {
            throw new InvalidCenterDataException("Địa chỉ phải có độ dài từ 1 đến 255 ký tự");
        }
    }

    private void validateTaxId(String taxId) {
        if (taxId == null || !taxId.matches("^[0-9]{10,13}$")) {
            throw new InvalidCenterDataException("Mã số thuế phải là chuỗi số từ 10 đến 13 chữ số");
        }
    }

    private CenterResponse mapToResponse(Center center) {
        CenterResponse response = new CenterResponse();
        response.setCenterId(center.centerId.toString());
        response.setName(center.name);
        response.setAddress(center.address);
        response.setTaxId(center.taxId);
        response.setContactPhone(center.contactPhone);
        response.setContactEmail(center.contactEmail);
        response.setAdminUserId(center.adminUserId != null ? center.adminUserId.toString() : null);
        return response;
    }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.centerservice.exception;

// DuplicateTaxIdException - Phase 2 DAY 5
// Tags: [REQ-005], [EXC-004]
public class DuplicateTaxIdException extends RuntimeException {
    private final String errorCode = "CENTER_TAXID_CONFLICT";

    public DuplicateTaxIdException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```

```java
package org.nlh4j.membershiphub.centerservice.exception;

// CenterNotFoundException - Phase 2 DAY 5
// Tags: [REQ-005], [EXC-004]
public class CenterNotFoundException extends RuntimeException {
    private final String errorCode = "CENTER_NOT_FOUND";

    public CenterNotFoundException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```

```java
package org.nlh4j.membershiphub.centerservice.exception;

// CenterAccessDeniedException - Phase 2 DAY 5
// Tags: [REQ-006], [EXC-004]
public class CenterAccessDeniedException extends RuntimeException {
    private final String errorCode = "CENTER_ACCESS_DENIED";

    public CenterAccessDeniedException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 NHIỆM VỤ PHỤ 5.2: Kiểm thử tích hợp Center-Service
##### Tác Nhân Được Phân Công: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/CenterServiceIntegrationTest.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-004], [REQ-005], [REQ-006], [EXC-004]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Xây dựng test tích hợp sử dụng `@QuarkusTest` với Testcontainers PostgreSQL 16. Kiểm thử: tạo trung tâm thành công, từ chối khi tax_id trùng lặp, cập nhật thông tin trung tâm, xóa trung tâm, danh sách trung tâm trả về đầy đủ thông tin, gán Center Admin cập nhật đúng userId, validate input cho từng trường name/address/taxId. Xác minh rằng constraint CHECK chỉ chấp nhận tax_id từ 10-13 chữ số.

* **Database Schema DDL SQL Specification [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- Không có thay đổi cơ sở dữ liệu trong nhiệm vụ phụ này
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 5.3: Đánh giá kiến trúc module Center
##### Tác Nhân Được Phân Công: Reviewer
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/CenterService.java

* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-004], [REQ-005], [REQ-006]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Rà soát `CenterService` đảm bảo logic gán Center Admin không vi phạm quyền chéo trung tâm, kiểm tra xử lý transaction khi xóa trung tâm có dữ liệu liên quan (khóa học, học viên), đề xuất chiến lược soft-delete thay vì hard-delete, xác minh caching cho danh sách trung tâm. Đánh giá tuân thủ OWASP A01 (Broken Access Control) khi truy vấn danh sách trung tâm phải lọc theo center_id của người dùng.

#### 📝 NHIỆM VỤ PHỤ 5.4: Hoàn thiện tài liệu Blueprint tổng thể
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/architecture/blueprint.md

* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006]<!--END_TAGS-->

* **Low-Level Technical Task Instruction:** Hoàn thiện blueprint kiến trúc doanh nghiệp với: sơ đồ Mermaid tổng quan hệ thống (System Context Diagram), sơ đồ Container Diagram cho 6 vi dịch vụ, sơ đồ Component Diagram cho user-service và center-service, bảng tổng hợp endpoint API với Tag ID mapping, ma trận phân quyền đầy đủ, checklist bảo mật OWASP Top 10. Tài liệu phải ở định dạng Markdown với tiêu đề phân cấp rõ ràng.