# Giai đoạn 1: <!--PHASE_NAME_START-->Khởi Tạo Nền Tảng Đa Dịch Vụ, Cơ Sở Dữ Liệu Và Xác Thực Bảo Mật<!--PHASE_NAME_END-->

## 📊 Kiểm Soát Tài Liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Bản Thiết Kế** | ARCH-20260829225017 |
| **Tên Dự Án** | membership-hub |
| **Giai đoạn** | 1 |
| **Tên Giai Đoạn** | <!--PHASE_NAME_START-->Khởi Tạo Nền Tảng Đa Dịch Vụ, Cơ Sở Dữ Liệu Và Xác Thực Bảo Mật<!--PHASE_NAME_END--> |
| **Mô Tả** | <!--PHASE_DESC_START-->Giai đoạn 1 tập trung thiết lập toàn bộ bộ khung kiến trúc đa dịch vụ cho nền tảng membership-hub, bao gồm khởi tạo mô tả build Maven multi-module cho 4 microservices backend, cấu hình mô tả gói npm cho frontend Next.js, di trú Flyway cho 12 bảng nghiệp vụ chuẩn ANSI SQL, đồng thời triển khai hạ tầng bảo mật OAuth2 Resource Server với JWT 15 phút, refresh token 7 ngày và tích hợp 3 nhà cung cấp Social Provider. Toàn bộ hạ tầng phải biên dịch sạch ngay từ ngày đầu tiên và đảm bảo tuân thủ tiêu chuẩn OWASP Top 10 với audit log 1 năm theo NFR-006<!--PHASE_DESC_END--> |
| **Phiên Bản** | 1.0 (Baseline) |
| **Ngày Giờ** | 2026/08/29 22:50:17 |
| **Tác Giả** | Kiến Trúc Sư Hệ Thống Doanh Nghiệp (SA Agent) |
| **Phê Duyệt** | Đang chờ Rà Soát Quản Trị Kỹ Thuật |

## 1. Phạm Vi Hoạt Động Và Mục Tiêu Giai Đoạn

Giai đoạn 1 đóng vai trò nền tảng khởi tạo toàn bộ hệ thống membership-hub theo mô hình microservices phân tách theo miền nghiệp vụ. Phạm vi kỹ thuật cốt lõi được phân bổ cho giai đoạn này bao gồm 3 nhiệm vụ backlog chính: Nhiệm vụ 1 (Scaffolding và Build Descriptors), Nhiệm vụ 26 (Khởi tạo và Di trú Database Schema tổng thể) và Nhiệm vụ 27 (Luồng xác thực và phát hành JWT/Refresh). Theo kế hoạch phân bổ trong bảng tổng hợp đa giai đoạn ở mục 4.2, giai đoạn 1 được phân bổ chính xác 3 ngày làm việc (từ Ngày 1 đến Ngày 3), phù hợp với biên tính toán Relative_Z = 3.

Mục tiêu cốt lõi của giai đoạn là xây dựng 4 microservices backend (`user-service`, `center-service`, `course-service`, `attendance-service`) dưới quy ước gói `org.nlh4j.membershiphub` sử dụng Quarkus 3.15.1 làm runtime chính với tối ưu GraalVM native image, đảm bảo 100% mô tả build có thể biên dịch trắng ngay từ đầu. Đồng thời, giai đoạn này xây dựng nền tảng dữ liệu quan hệ chuẩn ANSI SQL cho 12 bảng nghiệp vụ cốt lõi thông qua hệ thống di trú Flyway (Roles, Users, Centers, Courses, Enrollments, Attendance, StudentCards, Notifications, Promotions, Announcements, SystemSettings, AuditLogs) với đầy đủ ràng buộc khóa ngoại, chỉ mục tối ưu và composite unique `(student_id, course_id, attendance_date)` đảm bảo idempotency cho luồng điểm danh QR ở các giai đoạn sau.

Hạ tầng bảo mật OAuth2 Resource Server được cấu hình với JWT access token 15 phút và refresh token 7 ngày, hỗ trợ xác thực đa kênh bao gồm email/password cho người dùng nội bộ và 3 social provider (Firebase, Google, Facebook) thông qua cơ chế đổi OAuth2 ID token sang thông tin user. Tất cả hành động xác thực phải được ghi log kiểm toán thông qua lớp `AuthAuditLogger` tuân thủ chuẩn bảo mật OWASP, đảm bảo dấu vết kiểm toán đầy đủ phục vụ tuân thủ NFR-003 và NFR-006 với thời gian lưu trữ tối thiểu 1 năm. Mục tiêu chính là toàn bộ mô tả build và database schema có thể triển khai tức thì lên môi trường development ngay khi giai đoạn kết thúc, sẵn sàng cho giai đoạn 2 phát triển logic nghiệp vụ.

## 2. Phạm Vi Kỹ Thuật Cho Phép Và Ranh Giới Thư Mục

Danh sách đầy đủ các tệp tin vật lý được phép tạo mới trong giai đoạn 1, tuân thủ nghiêm ngặt quy ước gói `org.nlh4j.membershiphub` và ranh giới thư mục doanh nghiệp:

* `./sources/backend/pom.xml` — [ARC-000]
* `./sources/backend/user-service/pom.xml` — [ARC-000]
* `./sources/backend/center-service/pom.xml` — [ARC-000]
* `./sources/backend/course-service/pom.xml` — [ARC-000]
* `./sources/backend/attendance-service/pom.xml` — [ARC-000]
* `./sources/frontend/package.json` — [ARC-000]
* `./sources/frontend/tsconfig.json` — [ARC-000]
* `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_and_roles.sql` — [DAT-001], [DAT-008]
* `./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql` — [DAT-002]
* `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql` — [DAT-003]
* `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql` — [DAT-004], [DAT-005]
* `./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql` — [DAT-006]
* `./sources/backend/attendance-service/src/main/resources/db/migration/V2__init_notifications.sql` — [DAT-007]
* `./sources/backend/center-service/src/main/resources/db/migration/V2__init_promotions.sql` — [DAT-009]
* `./sources/backend/course-service/src/main/resources/db/migration/V2__init_announcements.sql` — [DAT-010]
* `./sources/backend/user-service/src/main/resources/db/migration/V3__init_system_settings.sql` — [DAT-011]
* `./sources/backend/user-service/src/main/resources/db/migration/V3__init_audit_logs.sql` — [DAT-012]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java` — [ARC-006], [NFR-003]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/ResourceServerConfig.java` — [ARC-006], [NFR-003]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistry.java` — [ARC-006], [REQ-002]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/AuthAuditLogger.java` — [NFR-003], [NFR-006]
* `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProviderTest.java` — [ARC-006]
* `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistryTest.java` — [ARC-006], [REQ-002]
* `./sources/docs/scaffolding-architecture.md` — [DOC-001]
* `./sources/docs/database-schema.md` — [DOC-001]
* `./sources/docs/security-authentication.md` — [DOC-001]

* **RÀNG BUỘC BẮT BUỘC VỀ BỘ KHUNG NỀN TẢNG**:
  - Trong Ngày 1 của giai đoạn, tệp mô tả build root Maven `./sources/backend/pom.xml` và 4 tệp mô tả build con cho microservices phải được khởi tạo trước bất kỳ mã nguồn ứng dụng nào.
  - Đối với frontend Next.js, bắt buộc phải khởi tạo `./sources/frontend/package.json` và `./sources/frontend/tsconfig.json` như tài sản cấu hình workspace.
  - Toàn bộ tài sản khởi tạo workspace phải được truy vết bằng mã thẻ kiến trúc `[ARC-000]`.

## 3. Chỉ Thị Chức Năng Cho Từng Sub-Agent

* **Coder**: Đóng vai trò lập trình viên ứng dụng chính. Chịu trách nhiệm tạo mô tả build Maven multi-module và 4 mô tả build con cho microservices backend, khởi tạo cấu hình npm và TypeScript cho frontend Next.js, đồng thời hiện thực hóa lớp `JwtTokenProvider`, `ResourceServerConfig`, `SocialAuthProviderRegistry` và `AuthAuditLogger` trong package bảo mật của user-service. Bị cấm viết bộ kiểm thử hoặc tài liệu.

* **Tester**: Đóng vai trò kiểm thử viên chính. Tạo bộ kiểm thử đơn vị JUnit 5 cho `JwtTokenProvider` (5 test case) và `SocialAuthProviderRegistry` (5 test case), xây dựng bộ kiểm thử tích hợp Maven Build Integration Test và Flyway Migration Integration Test. Bị cấm sửa đổi mã nguồn sản phẩm.

* **Doc**: Soạn thảo 3 tài liệu Markdown quan trọng: tài liệu kiến trúc scaffolding, tài liệu sơ đồ cơ sở dữ liệu, tài liệu bảo mật và xác thực. Tất cả tệp tài liệu phải kết thúc bằng phần mở rộng `.md` và nằm trong thư mục tập trung `./sources/docs/`.

* **Reviewer**: Thực hiện rà soát chất lượng mã nguồn theo checklist OWASP Top 10, đánh giá tuân thủ quy ước gói `org.nlh4j.membershiphub`, xác minh cấu hình Maven đúng chuẩn và phát hiện sớm các vấn đề bảo mật tiềm ẩn.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn (DoD)

Giai đoạn 1 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: (1) 100% tệp mô tả build Maven và npm biên dịch trắng thông qua lệnh `mvn clean install -DskipTests` và `npm install --dry-run`; (2) Toàn bộ 12 bảng nghiệp vụ được tạo thành công qua 3 phiên bản di trú Flyway (V1, V2, V3) với đầy đủ ràng buộc khóa ngoại và chỉ mục; (3) Lớp `JwtTokenProvider` sinh và xác minh thành công JWT với thuật toán RS256, thời hạn 15 phút cho access token và 7 ngày cho refresh token; (4) `SocialAuthProviderRegistry` đăng ký thành công 3 provider Firebase, Google, Facebook với khả năng mở rộng qua CDI; (5) `AuthAuditLogger` ghi log kiểm toán thành công cho 7 loại hành động (`LOGIN_SUCCESS`, `LOGIN_FAILED`, `LOGOUT`, `SOCIAL_AUTH_SUCCESS`, `SOCIAL_AUTH_FAILED`, `TOKEN_REFRESH`, `ROLE_CHANGED`); (6) 100% thẻ truy vết `[ARC-000]`, `[ARC-006]`, `[DAT-001]` đến `[DAT-012]`, `[NFR-003]`, `[NFR-006]` được ánh xạ đầy đủ vào mã nguồn và tài liệu; (7) 100% bộ kiểm thử JUnit đạt trạng thái PASS với code coverage >= 85% cho lớp bảo mật.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->KHỞI TẠO BỘ KHUNG DỰ ÁN VÀ MÔ TẢ BUILD ĐA MODULE<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 1.1: Tạo mô tả build root Maven đa module

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Khởi tạo tệp tin mô tả build Maven root tại đường dẫn `./sources/backend/pom.xml` với cấu hình packaging `pom`, khai báo chính xác 4 module con tương ứng với 4 microservices: `user-service`, `center-service`, `course-service`, `attendance-service`. Tệp pom root phải sử dụng Java 17 LTS làm phiên bản nguồn và đích biên dịch, Quarkus 3.15.1 làm BOM chính, đảm bảo `<groupId>` cố định là `org.nlh4j.membershiphub`, `<artifactId>` là `membership-hub-backend`, `<version>` là `1.0.0-SNAPSHOT`. Khai báo `<dependencyManagement>` tập trung cho Jakarta EE 10, Hibernate ORM Panache 3.15.1, RESTEasy Reactive, Hibernate Validator, Flyway 10.10.0, PostgreSQL JDBC driver 42.7.3, SmallRye JWT 4.10.0, SmallRye Reactive Messaging Kafka 4.10.0, OpenAPI 2.10.0, JUnit 5.10.1, Mockito 5.7.0, REST Assured 5.4.0. Cấu hình các plugin Maven: `maven-compiler-plugin` 3.13.0 với release 17, `maven-surefire-plugin` 3.2.5, `flyway-maven-plugin` 10.10.0, `quarkus-maven-plugin` 3.15.1 hỗ trợ chế độ `quarkus:dev`, `jacoco-maven-plugin` 0.8.11 cho báo cáo độ bao phủ mã nguồn. Đảm bảo tệp tin biên dịch trắng ngay khi được tạo mà không phụ thuộc vào module con.

#### 📝 NHIỆM VỤ PHỤ 1.2: Tạo mô tả build cho 4 microservices con

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo 4 tệp tin mô tả build Maven con cho 4 microservices tại các đường dẫn `./sources/backend/user-service/pom.xml`, `./sources/backend/center-service/pom.xml`, `./sources/backend/course-service/pom.xml`, `./sources/backend/attendance-service/pom.xml`. Mỗi tệp con phải khai báo `<parent>` tham chiếu đến `org.nlh4j.membershiphub:membership-hub-backend:1.0.0-SNAPSHOT`, `<artifactId>` tương ứng (`user-service`, `center-service`, `course-service`, `attendance-service`), packaging `jar`. Mỗi module con kế thừa toàn bộ `dependencyManagement` từ parent và chỉ khai báo dependency thực sự sử dụng: `quarkus-resteasy-reactive-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-smallrye-openapi`, `quarkus-smallrye-reactive-messaging-kafka`, `quarkus-hibernate-validator`. Riêng `user-service` bổ sung `quarkus-smallrye-jwt-build` cho việc ký token. Dependencies test gồm `quarkus-junit5`, `rest-assured`, `mockito-core`. Đảm bảo tất cả 4 tệp tin biên dịch trắng thông qua lệnh `mvn clean install -DskipTests` tại thư mục root.

#### 📝 NHIỆM VỤ PHỤ 1.3: Tạo mô tả frontend Next.js và cấu hình TypeScript

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/frontend/package.json`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Khởi tạo tệp tin `package.json` tại `./sources/frontend/package.json` cho ứng dụng Next.js 14.2.15 với App Router. Khai báo các dependency: `next@14.2.15`, `react@18.3.1`, `react-dom@18.3.1`, `next-intl@3.17.2` cho đa ngôn ngữ, `tailwindcss@3.4.10` cho styling responsive, `nativewind@4.1.23` cho màn hình mobile, `axios@1.7.4` cho HTTP client, `zustand@4.5.4` cho state management, `react-hook-form@7.53.0` cho form binding, `zod@3.23.8` cho validation, `firebase@10.13.0` cho FCM, `@react-native-firebase/messaging@20.4.0` cho push mobile. Dev dependencies: `typescript@5.5.4`, `@types/react@18.3.3`, `@types/node@20.16.5`, `eslint@8.57.0`, `prettier@3.3.3`. Scripts: `dev`, `build`, `start`, `lint`, `type-check`. Đồng thời tạo tệp `./sources/frontend/tsconfig.json` với `compilerOptions` bật `strict: true`, `target: "ES2022"`, `module: "esnext"`, `moduleResolution: "bundler"`, `jsx: "preserve"`, `incremental: true`, `paths` ánh xạ `@/*` tới `./src/*`, `plugins` cho Next.js, include `src/**/*`, `next-env.d.ts`, exclude `node_modules`. Đảm bảo cả hai tệp tin cấu hình biên dịch trắng qua lệnh `npm install --dry-run`.

#### 📝 NHIỆM VỤ PHỤ 1.4: Kiểm thử tích hợp biên dịch đa module

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `INTEGRATION_SCOPE;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/MavenBuildIntegrationTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử tích hợp Maven tại đường dẫn `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/MavenBuildIntegrationTest.java` với mục đích xác minh rằng toàn bộ cấu trúc đa module Maven gồm `pom.xml` root và 4 `pom.xml` con biên dịch thành công qua lệnh `mvn clean install -DskipTests`. Sử dụng `ProcessBuilder` để thực thi lệnh Maven, kiểm tra `exit code` bằng 0, xác nhận các tệp tin `target/*.jar` được tạo ra cho cả 4 module, đảm bảo không có lỗi dependency resolution. Annotation `@QuarkusTest` kết hợp `@Order(1)` để chạy đầu tiên trong pipeline kiểm thử. Bao gồm assertion rằng mô tả `pom.xml` chứa `<groupId>org.nlh4j.membershiphub</groupId>`, phiên bản Java là 17, Quarkus BOM 3.15.1 được import đúng. Test phải PASS với mã thoát 0 từ Maven.

#### 📝 NHIỆM VỤ PHỤ 1.5: Đánh giá chuẩn scaffolding và quy ước gói

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer thực hiện đánh giá chuyên sâu tệp `pom.xml` root tại `./sources/backend/pom.xml` và 4 tệp `pom.xml` con tại `./sources/backend/user-service/pom.xml`, `./sources/backend/center-service/pom.xml`, `./sources/backend/course-service/pom.xml`, `./sources/backend/attendance-service/pom.xml`. Xác minh rằng 100% package Java sử dụng quy ước `org.nlh4j.membershiphub.<service-name>` (không có `com.example`, không có ký tự gạch ngang `-` hoặc gạch dưới `_` trong cấu trúc gói), version Java 17, Quarkus 3.15.1 là phiên bản stable enterprise. Kiểm tra không tồn tại dependency bị conflict, không có dependency lỏng lẻo (SNAPSHOT) ngoại trừ các module nội bộ, plugin Maven đều có version rõ ràng, không sử dụng `<dependency>` mà thiếu `<version>` trong các module con (phải kế thừa từ parent BOM). Đánh giá việc sử dụng `quarkus-maven-plugin` để hỗ trợ `quarkus:dev` mode. Tạo báo cáo đánh giá gồm checklist tuân thủ chuẩn, danh sách cảnh báo tiềm ẩn và đề xuất cải tiến hiệu năng biên dịch.

#### 📝 NHIỆM VỤ PHỤ 1.6: Soạn thảo tài liệu kiến trúc scaffolding

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/scaffolding-architecture.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tài liệu Markdown `./sources/docs/scaffolding-architecture.md` trình bày chi tiết kiến trúc scaffolding của dự án Membership Hub, gồm: sơ đồ cây thư mục đa module Maven root và 4 microservices con, quy ước đặt tên gói `org.nlh4j.membershiphub.<service-name>`, bảng ánh xạ version Quarkus 3.15.1, Java 17 LTS, danh sách toàn bộ dependency chuẩn hóa với phiên bản cụ thể. Đặc tả cấu trúc frontend Next.js 14.2.15 với App Router, các dependency thiết yếu (`next-intl`, `nativewind`, `zustand`, `react-hook-form`, `zod`). Mục tiêu là chuẩn hóa đầu vào cho toàn bộ team phát triển, đảm bảo quy trình biên dịch thống nhất và có thể tích hợp liền mạch với CI/CD. Tài liệu phải chứa sơ đồ Mermaid thể hiện mối quan hệ cha-con giữa các module Maven.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->KHỞI TẠO VÀ DI TRÚ CƠ SỞ DỮ LIỆU FLYWAY ĐA BẢNG<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 2.1: Tạo script di trú V1 cho người dùng, vai trò và trung tâm

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_and_roles.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-001], [DAT-002], [DAT-008]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp di trú Flyway `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_and_roles.sql` chứa DDL ANSI SQL chuẩn hóa cho 2 bảng `roles` và `users` theo đặc tả chi tiết. Bảng `roles` gồm `role_id SMALLINT NOT NULL` (PK), `name VARCHAR(30) NOT NULL UNIQUE`, `description VARCHAR(200)`, ràng buộc CHECK tên vai trò thuộc tập `('SYSTEM_ADMIN','CENTER_ADMIN','MANAGER','TEACHER','STUDENT')`. Bảng `users` gồm `user_id UUID NOT NULL` (PK), `email VARCHAR(255) NOT NULL UNIQUE`, `password_hash CHAR(60) NOT NULL`, `full_name VARCHAR(100) NOT NULL`, `role_id SMALLINT NOT NULL` (FK tham chiếu `roles.role_id`), `provider VARCHAR(20) NOT NULL DEFAULT 'local'` với CHECK thuộc tập `('local','firebase','google','facebook')`, `created_at TIMESTAMP NOT NULL DEFAULT now()`, `updated_at TIMESTAMP NOT NULL DEFAULT now()`. Tạo 2 chỉ mục `idx_users_role_id` và `idx_users_created_at` để tối ưu hóa truy vấn. Đồng thời tạo tệp di trú `./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql` cho bảng `centers` với `center_id UUID PK`, `name VARCHAR(100) NOT NULL`, `address VARCHAR(255) NOT NULL`, `tax_id VARCHAR(20) NOT NULL UNIQUE`, `contact_phone VARCHAR(20)`, `contact_email VARCHAR(100)`, chỉ mục `idx_centers_name`.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- ============================================
-- FILE: V1__init_users_and_roles.sql
-- SCOPE: Users & Roles
-- ============================================
CREATE TABLE roles (
    role_id SMALLINT NOT NULL,
    name VARCHAR(30) NOT NULL,
    description VARCHAR(200),
    CONSTRAINT pk_roles PRIMARY KEY (role_id),
    CONSTRAINT uq_roles_name UNIQUE (name),
    CONSTRAINT ck_roles_name CHECK (name IN ('SYSTEM_ADMIN','CENTER_ADMIN','MANAGER','TEACHER','STUDENT'))
);

CREATE TABLE users (
    user_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash CHAR(60) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role_id SMALLINT NOT NULL,
    provider VARCHAR(20) NOT NULL DEFAULT 'local',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES roles(role_id),
    CONSTRAINT ck_users_provider CHECK (provider IN ('local','firebase','google','facebook'))
);

CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_created_at ON users(created_at);

-- ============================================
-- FILE: V1__init_centers.sql
-- SCOPE: Centers
-- ============================================
CREATE TABLE centers (
    center_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tax_id VARCHAR(20) NOT NULL,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    CONSTRAINT pk_centers PRIMARY KEY (center_id),
    CONSTRAINT uq_centers_tax_id UNIQUE (tax_id)
);

CREATE INDEX idx_centers_name ON centers(name);
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 2.2: Tạo script di trú V1 cho khoá học, đăng ký và điểm danh

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-003], [DAT-004], [DAT-005]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo 2 tệp di trú Flyway. Tệp `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql` chứa DDL cho bảng `courses` gồm `course_id UUID PK`, `title VARCHAR(150) NOT NULL`, `description TEXT`, `start_date DATE NOT NULL`, `end_date DATE NOT NULL`, `teacher_id UUID NOT NULL` (FK tham chiếu `users.user_id`), `max_students INT NOT NULL DEFAULT 30` với CHECK > 0, `center_id UUID NOT NULL` (FK tham chiếu `centers.center_id`), ràng buộc CHECK `end_date >= start_date`, chỉ mục `idx_courses_teacher_id` và `idx_courses_start_date`. Tệp `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql` chứa DDL cho bảng `enrollments` (`enrollment_id UUID PK`, `student_id UUID NOT NULL FK users`, `course_id UUID NOT NULL FK courses`, `enrollment_date TIMESTAMP NOT NULL DEFAULT now()`, ràng buộc UNIQUE `(student_id, course_id)` để tránh đăng ký trùng) và bảng `attendance` (`attendance_id UUID PK`, `student_id UUID NOT NULL FK users`, `course_id UUID NOT NULL FK courses`, `attendance_date DATE NOT NULL`, `timestamp TIMESTAMP NOT NULL DEFAULT now()`, ràng buộc UNIQUE composite `(student_id, course_id, attendance_date)` đảm bảo idempotency theo REQ-013, 2 chỉ mục `idx_attendance_course_date` và `idx_attendance_student_date`).

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- ============================================
-- FILE: V1__init_courses.sql
-- SCOPE: Courses
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
    CONSTRAINT pk_courses PRIMARY KEY (course_id),
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id),
    CONSTRAINT fk_courses_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT ck_courses_date_range CHECK (end_date >= start_date),
    CONSTRAINT ck_courses_max_students CHECK (max_students > 0)
);

CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_courses_start_date ON courses(start_date);

-- ============================================
-- FILE: V1__init_enrollments_attendance.sql
-- SCOPE: Enrollments & Attendance
-- ============================================
CREATE TABLE enrollments (
    enrollment_id UUID NOT NULL,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    enrollment_date TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_enrollments PRIMARY KEY (enrollment_id),
    CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(course_id)
);

CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);

CREATE TABLE attendance (
    attendance_id UUID NOT NULL,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),
    CONSTRAINT uq_attendance_student_course_date UNIQUE (student_id, course_id, attendance_date),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT fk_attendance_course FOREIGN KEY (course_id) REFERENCES courses(course_id)
);

CREATE INDEX idx_attendance_course_date ON attendance(course_id, attendance_date);
CREATE INDEX idx_attendance_student_date ON attendance(student_id, attendance_date);
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 2.3: Tạo script di trú V2 cho thẻ học viên, thông báo và khuyến mãi

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-006], [DAT-007], [DAT-009]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo 3 tệp di trú Flyway V2. Tệp `./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql` chứa DDL cho bảng `student_cards` gồm `card_id UUID PK`, `student_id UUID NOT NULL UNIQUE FK users`, `issue_date DATE NOT NULL`, `validity_days INT NOT NULL CHECK > 0`, `remaining_days INT NOT NULL CHECK >= 0`, `end_date DATE NOT NULL`. Tệp `./sources/backend/attendance-service/src/main/resources/db/migration/V2__init_notifications.sql` chứa DDL cho bảng `notifications` (`notification_id UUID PK`, `user_id UUID FK users nullable`, `group_zalo VARCHAR(50) nullable`, `message TEXT NOT NULL`, `sent_at TIMESTAMP NOT NULL DEFAULT now()`, `delivered BOOLEAN NOT NULL DEFAULT false`, `retry_count INT NOT NULL DEFAULT 0`, ràng buộc CHECK `(user_id IS NOT NULL) OR (group_zalo IS NOT NULL)` đảm bảo phải có ít nhất một kênh nhận, 2 chỉ mục `idx_notifications_user_id` và `idx_notifications_sent_at`). Tệp `./sources/backend/center-service/src/main/resources/db/migration/V2__init_promotions.sql` chứa DDL cho bảng `promotions` (`promo_id UUID PK`, `code VARCHAR(30) NOT NULL UNIQUE`, `discount_percent SMALLINT NOT NULL CHECK BETWEEN 1 AND 100`, `start_date DATE nullable`, `end_date DATE nullable`, `description TEXT`, `center_id UUID NOT NULL FK centers`, ràng buộc CHECK `end_date IS NULL OR end_date >= start_date`).

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- ============================================
-- FILE: V2__init_student_cards.sql
-- SCOPE: Student Cards
-- ============================================
CREATE TABLE student_cards (
    card_id UUID NOT NULL,
    student_id UUID NOT NULL,
    issue_date DATE NOT NULL,
    validity_days INT NOT NULL,
    remaining_days INT NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT pk_student_cards PRIMARY KEY (card_id),
    CONSTRAINT uq_student_cards_student UNIQUE (student_id),
    CONSTRAINT fk_student_cards_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT ck_student_cards_validity CHECK (validity_days > 0),
    CONSTRAINT ck_student_cards_remaining CHECK (remaining_days >= 0)
);

-- ============================================
-- FILE: V2__init_notifications.sql
-- SCOPE: Notifications
-- ============================================
CREATE TABLE notifications (
    notification_id UUID NOT NULL,
    user_id UUID,
    group_zalo VARCHAR(50),
    message TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    delivered BOOLEAN NOT NULL DEFAULT false,
    retry_count INT NOT NULL DEFAULT 0,
    CONSTRAINT pk_notifications PRIMARY KEY (notification_id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT ck_notifications_target CHECK ((user_id IS NOT NULL) OR (group_zalo IS NOT NULL))
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_sent_at ON notifications(sent_at);

-- ============================================
-- FILE: V2__init_promotions.sql
-- SCOPE: Promotions
-- ============================================
CREATE TABLE promotions (
    promo_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    discount_percent SMALLINT NOT NULL,
    start_date DATE,
    end_date DATE,
    description TEXT,
    center_id UUID NOT NULL,
    CONSTRAINT pk_promotions PRIMARY KEY (promo_id),
    CONSTRAINT uq_promotions_code UNIQUE (code),
    CONSTRAINT fk_promotions_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT ck_promotions_discount CHECK (discount_percent BETWEEN 1 AND 100),
    CONSTRAINT ck_promotions_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 2.4: Tạo script di trú V2/V3 cho thông báo chung, cài đặt hệ thống và audit log

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/course-service/src/main/resources/db/migration/V2__init_announcements.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-010], [DAT-011], [DAT-012]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo 3 tệp di trú Flyway bổ sung. Tệp `./sources/backend/course-service/src/main/resources/db/migration/V2__init_announcements.sql` chứa DDL cho bảng `announcements` gồm `announcement_id UUID PK`, `title VARCHAR(150) NOT NULL`, `content TEXT NOT NULL`, `start_date DATE nullable`, `end_date DATE nullable`, `center_id UUID NOT NULL FK centers`, `created_at TIMESTAMP NOT NULL DEFAULT now()`, ràng buộc CHECK `end_date IS NULL OR end_date >= start_date`. Tệp `./sources/backend/user-service/src/main/resources/db/migration/V3__init_system_settings.sql` chứa DDL cho bảng `system_settings` (`setting_key VARCHAR(50) PK`, `setting_value TEXT NOT NULL`, `description VARCHAR(200)`). Tệp `./sources/backend/user-service/src/main/resources/db/migration/V3__init_audit_logs.sql` chứa DDL cho bảng `audit_logs` (`log_id UUID PK`, `user_id UUID FK users nullable`, `action VARCHAR(100) NOT NULL`, `details TEXT`, `occurred_at TIMESTAMP NOT NULL DEFAULT now()`, 2 chỉ mục `idx_audit_logs_user_id` và `idx_audit_logs_occurred_at`) phục vụ NFR-006 ghi log kiểm toán 1 năm.

* **Đặc Tả Lược Đồ Cơ Sở Dữ Liệu DDL SQL [DAT-XXX]:**

<!--START_DDL_MIGRATION-->
```sql
-- ============================================
-- FILE: V2__init_announcements.sql
-- SCOPE: Announcements
-- ============================================
CREATE TABLE announcements (
    announcement_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    start_date DATE,
    end_date DATE,
    center_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_announcements PRIMARY KEY (announcement_id),
    CONSTRAINT fk_announcements_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT ck_announcements_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

-- ============================================
-- FILE: V3__init_system_settings.sql
-- SCOPE: System Settings
-- ============================================
CREATE TABLE system_settings (
    setting_key VARCHAR(50) NOT NULL,
    setting_value TEXT NOT NULL,
    description VARCHAR(200),
    CONSTRAINT pk_system_settings PRIMARY KEY (setting_key)
);

-- ============================================
-- FILE: V3__init_audit_logs.sql
-- SCOPE: Audit Logs
-- ============================================
CREATE TABLE audit_logs (
    log_id UUID NOT NULL,
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    occurred_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_audit_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_occurred_at ON audit_logs(occurred_at);
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 2.5: Kiểm thử tích hợp Flyway migration

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `INTEGRATION_SCOPE;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/FlywayMigrationIntegrationTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [DAT-012]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử tích hợp `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/FlywayMigrationIntegrationTest.java` xác minh toàn bộ script di trú Flyway V1, V2, V3 chạy thành công và tạo đủ 12 bảng nghiệp vụ. Sử dụng `@QuarkusTest` với profile `test`, cấu hình Testcontainers PostgreSQL 1.20.4 (`org.testcontainers:postgresql:1.20.4`) để khởi tạo cơ sở dữ liệu PostgreSQL 16-alpine trong Docker. Truy vấn `information_schema.tables` xác nhận sự tồn tại của 12 bảng (`roles`, `users`, `centers`, `courses`, `enrollments`, `attendance`, `student_cards`, `notifications`, `promotions`, `announcements`, `system_settings`, `audit_logs`). Kiểm tra ràng buộc UNIQUE composite `(student_id, course_id, attendance_date)` trên bảng `attendance` bằng cách insert 2 bản ghi trùng lặp và xác nhận exception. Xác minh CHECK constraints từng bảng hoạt động đúng. Kiểm tra toàn bộ khóa ngoại FK bằng cách thử insert giá trị không tồn tại.

#### 📝 NHIỆM VỤ PHỤ 2.6: Đánh giá chuẩn DDL ANSI SQL và tối ưu chỉ mục

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [DAT-012]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer thực hiện đánh giá toàn diện 12 script di trú Flyway trên 4 microservices. Xác minh 100% tuân thủ ANSI SQL (không sử dụng ENUM đặc thù database, thay thế bằng VARCHAR + CHECK constraint). Kiểm tra các chỉ mục được tạo phù hợp với pattern truy vấn: `idx_users_role_id` cho truy vấn RBAC, `idx_courses_teacher_id` cho chức năng gán giáo viên, `idx_attendance_course_date` và `idx_attendance_student_date` cho báo cáo, `idx_notifications_user_id` cho gửi push, `idx_audit_logs_occurred_at` cho truy vấn log theo thời gian. Đánh giá ràng buộc UNIQUE composite `(student_id, course_id, attendance_date)` đảm bảo idempotency REQ-013. Rà soát toàn bộ FK đảm bảo tính toàn vẹn tham chiếu và ON DELETE phù hợp. Tạo báo cáo đánh giá gồm các khuyến nghị partitioning cho bảng `audit_logs` khi dữ liệu vượt 10 triệu bản ghi.

#### 📝 NHIỆM VỤ PHỤ 2.7: Soạn thảo tài liệu sơ đồ cơ sở dữ liệu

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/database-schema.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [DAT-012], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tài liệu `./sources/docs/database-schema.md` trình bày sơ đồ quan hệ (ERD) của 12 bảng nghiệp vụ, sử dụng Mermaid `erDiagram` để thể hiện mối quan hệ giữa `users`, `roles`, `centers`, `courses`, `enrollments`, `attendance`, `student_cards`, `notifications`, `promotions`, `announcements`, `system_settings`, `audit_logs`. Mô tả chi tiết từng cột với kiểu dữ liệu, ràng buộc, chỉ mục, khóa ngoại. Bao gồm bảng ánh xạ Tag ID `[DAT-XXX]` tới từng bảng, giải thích ý nghĩa nghiệp vụ của từng trường. Tài liệu phải chứa sơ đồ Mermaid `flowchart` thể hiện trình tự áp dụng script V1, V2, V3 trong pipeline Flyway.

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->CẤU HÌNH BẢO MẬT OAUTH2, JWT VÀ SOCIAL PROVIDER<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 3.1: Hiện thực hóa JWT Token Provider

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006], [NFR-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java` hiện thực hóa lớp `JwtTokenProvider` với annotation `@ApplicationScoped`, sử dụng `Jwt.issuer()` từ SmallRye JWT Build. Triển khai phương thức `generateAccessToken(String userId, String role, String provider)` trả về JWT có thời hạn 15 phút, claim `sub` chứa userId, claim `group` chứa role, claim `iss` là `membership-hub`, claim `aud` là `membership-hub-client`. Phương thức `generateRefreshToken(String userId)` sinh refresh token với thời hạn 7 ngày, claim `type` là `refresh`. Phương thức `validateToken(String token)` kiểm tra chữ ký bằng khóa RSA 2048-bit, xác minh thời hạn và issuer. Phương thức `getClaims(String token)` trả về `JsonWebToken` đã giải mã. Tích hợp `@ConfigProperty(name = "mp.jwt.verify.issuer")` và `mp.jwt.verify.publickey.location`. Sử dụng thuật toán RS256.

#### 📝 NHIỆM VỤ PHỤ 3.2: Hiện thực hóa Resource Server Config

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/ResourceServerConfig.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006], [NFR-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/ResourceServerConfig.java` cấu hình `@ApplicationPath("/api/v1")`, lớp `ResourceServerConfig` implement `SecurityIdentityAugmentor` từ Quarkus Security. Sử dụng `@Produces` cho `SecurityIdentity`, bổ sung role vào `SecurityIdentity` từ claim `group` trong JWT. Áp dụng annotation `@DenyAll`, `@RolesAllowed`, `@PermitAll` cho các REST endpoint. Cấu hình `quarkus.http.auth.proactive=false` để cho phép truy cập không xác thực vào `/api/v1/auth/login` và `/api/v1/auth/social`. Tích hợp `quarkus.smallrye-jwt.enabled=true`, `mp.jwt.verify.issuer=membership-hub`, `mp.jwt.verify.publickey.location=publicKey.pem`, `smallrye.jwt.sign.key.location=privateKey.pem`. Đảm bảo tất cả endpoint khác yêu cầu JWT hợp lệ, trả về HTTP 401 khi thiếu token, HTTP 403 khi không đủ quyền.

#### 📝 NHIỆM VỤ PHỤ 3.3: Hiện thực hóa Social Auth Provider Registry

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistry.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006], [REQ-002]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistry.java` hiện thực hóa interface `SocialAuthProvider` gồm `String getName()`, `SocialUserInfo verifyToken(String idToken)`. Tạo 3 implementation: `FirebaseAuthProvider` (xác minh ID Token qua Firebase Admin SDK 9.2.0, endpoint `https://identitytoolkit.googleapis.com/v1/accounts:lookup`), `GoogleAuthProvider` (xác minh qua Google API `https://oauth2.googleapis.com/tokeninfo?id_token=`), `FacebookAuthProvider` (xác minh qua `https://graph.facebook.com/v18.0/debug_token`). Lớp `SocialAuthProviderRegistry` với annotation `@ApplicationScoped` chứa map `Map<String, SocialAuthProvider>` được inject tất cả `Instance<SocialAuthProvider>`, cung cấp phương thức `SocialUserInfo authenticate(String providerName, String idToken)` tra cứu provider theo tên. Lớp `SocialUserInfo` là POJO gồm `String email`, `String fullName`, `String providerId`, `String profilePictureUrl`.

#### 📝 NHIỆM VỤ PHỤ 3.4: Hiện thực hóa Auth Audit Logger

##### Sub-Agent được phân công: Coder
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/AuthAuditLogger.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-003], [NFR-006]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp mã nguồn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/AuthAuditLogger.java` hiện thực hóa lớp `AuthAuditLogger` với annotation `@ApplicationScoped`, inject `AuditLogRepository` (Panache Repository). Phương thức `logAuthEvent(UUID userId, String action, String details)` tạo bản ghi `AuditLog` với `action` thuộc tập `LOGIN_SUCCESS`, `LOGIN_FAILED`, `LOGOUT`, `SOCIAL_AUTH_SUCCESS`, `SOCIAL_AUTH_FAILED`, `TOKEN_REFRESH`, `ROLE_CHANGED`, lưu `details` dạng JSON chứa IP, User-Agent. Annotation `@Transactional` đảm bảo ghi log trong cùng transaction với nghiệp vụ. Cấu hình logger SLF4J với mức INFO, output định dạng JSON cho stack ELK, tích hợp OpenTelemetry tracing.

#### 📝 NHIỆM VỤ PHỤ 3.5: Kiểm thử đơn vị JWT Token Provider

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProviderTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử đơn vị `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProviderTest.java` sử dụng JUnit 5 (`@QuarkusTest`) kiểm thử toàn diện lớp `JwtTokenProvider`. Test case 1: `generateAccessToken_returnsValidJwt` xác minh JWT chứa claim `sub`, `group`, `iss=membership-hub`, `exp` trong tương lai 15 phút. Test case 2: `generateRefreshToken_returnsSevenDayToken` xác minh refresh token có thời hạn đúng 7 ngày. Test case 3: `validateToken_acceptsValidToken` xác nhận token hợp lệ. Test case 4: `validateToken_rejectsExpiredToken` xác nhận token hết hạn bị từ chối. Test case 5: `validateToken_rejectsInvalidSignature` xác nhận token sai chữ ký bị từ chối. Sử dụng khóa RSA test fixture nội bộ.

#### 📝 NHIỆM VỤ PHỤ 3.6: Kiểm thử đơn vị Social Auth Provider Registry

##### Sub-Agent được phân công: Tester
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistry.java;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistryTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006], [REQ-002]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp kiểm thử đơn vị `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistryTest.java` sử dụng JUnit 5 kết hợp Mockito 5.7.0 (`@InjectMock`). Mock 3 provider `FirebaseAuthProvider`, `GoogleAuthProvider`, `FacebookAuthProvider` trả về `SocialUserInfo` giả lập. Test case 1: `authenticate_withFirebase_returnsUserInfo` xác minh gọi provider đúng tên. Test case 2: `authenticate_withGoogle_returnsUserInfo` xác minh luồng Google. Test case 3: `authenticate_withFacebook_returnsUserInfo` xác minh luồng Facebook. Test case 4: `authenticate_withUnknownProvider_throwsException` xác minh ném `UnsupportedProviderException` khi tên provider không hợp lệ. Test case 5: `authenticate_withInvalidToken_throwsException` xác minh ném `InvalidTokenException` khi token không hợp lệ.

#### 📝 NHIỆM VỤ PHỤ 3.7: Đánh giá giải pháp bảo mật và tuân thủ OWASP

##### Sub-Agent được phân công: Reviewer
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/ResourceServerConfig.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006], [NFR-003]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Reviewer thực hiện đánh giá chuyên sâu lớp bảo mật `ResourceServerConfig` và `JwtTokenProvider` đối chiếu với OWASP Top 10. Kiểm tra: (1) Khóa RSA 2048-bit đảm bảo bảo mật mật mã, (2) JWT sử dụng thuật toán RS256 không cho phép `none`, (3) Xác minh claim `iss` và `aud` ngăn chặn tấn công token confusion, (4) Refresh token lưu cơ sở dữ liệu cho phép thu hồi (revocation), (5) Rate limiting cho `/api/v1/auth/login` ngăn brute-force (cần bổ sung Bucket4j 8.10.0), (6) CSRF token cho endpoint mutation, (7) AuditLogger ghi log đầy đủ theo NFR-006, (8) Truy vấn SQL sử dụng JPQL parameter binding ngăn SQLi. Tạo báo cáo gồm ma trận rủi ro, đề xuất bổ sung HTTP Security Headers (`X-Content-Type-Options`, `X-Frame-Options`, `Strict-Transport-Security`).

#### 📝 NHIỆM VỤ PHỤ 3.8: Soạn thảo tài liệu bảo mật và xác thực

##### Sub-Agent được phân công: Doc
##### Thành Phần Mục Tiêu Và Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** `./sources/docs/security-authentication.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006], [NFR-003], [DOC-001]<!--END_TAGS-->
* **Chỉ Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo tài liệu `./sources/docs/security-authentication.md` trình bày toàn diện kiến trúc bảo mật và xác thực. Bao gồm: (1) Sơ đồ Mermaid `sequenceDiagram` thể hiện luồng đăng nhập email/password và cấp JWT, (2) Sơ đồ Mermaid `sequenceDiagram` thể hiện luồng Social OAuth2 với 3 provider, (3) Sơ đồ Mermaid `sequenceDiagram` thể hiện luồng refresh token, (4) Bảng mô tả cấu trúc JWT (header, payload, signature), (5) Chính sách mật khẩu mạnh, (6) Quy trình thu hồi token, (7) Ma trận RBAC ánh xạ `[ARC-001]` đến `[ARC-005]`, (8) Tuân thủ OWASP Top 10 với checklist cụ thể, (9) Hướng dẫn xử lý sự cố (forgot password, locked account, MFA trong tương lai). Tài liệu phải dùng sơ đồ Mermaid, bảng Markdown, không chứa thuật ngữ chưa giải thích.