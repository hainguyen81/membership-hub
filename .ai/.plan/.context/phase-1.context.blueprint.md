<!--START_CHUNK_PHASE_1-->

# Giai Đoạn 1: <!--PHASE_NAME_START-->Khởi Tạo Khung Microservices, Di Trú Cơ Sở Dữ Liệu Lõi Và Nền Tảng Bảo Mật Gateway<!--PHASE_NAME_END-->

## 📊 Kiểm Soát Tài Liệu

| Mục | Chi tiết |
| :--- | :--- |
| **Mã bản thiết kế** | ARCH-20260828112120 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 1 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Khởi Tạo Khung Microservices, Di Trú Cơ Sở Dữ Liệu Lõi Và Nền Tảng Bảo Mật Gateway<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn 1 tập trung vào việc thiết lập toàn bộ khung dự án microservices Quarkus với Maven multi-module cho sáu service backend, đồng thời khởi tạo frontend Next.js. Triển khai Flyway migration scripts cho 11 bảng cơ sở dữ liệu lõi với các ràng buộc UNIQUE, FOREIGN KEY, INDEXES tuân thủ chuẩn OWASP. Thiết lập lớp bảo mật nền tảng gồm JWT filter chain, OAuth2 resource server, CORS policy, OpenAPI 3.0 gateway spec và Kafka topic schemas. Giai đoạn này đặt nền móng kiến trúc vững chắc cho toàn bộ hệ thống membership-hub, đảm bảo 100% tuân thủ các tiêu chuẩn bảo mật doanh nghiệp và khả năng mở rộng theo chiều ngang.<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Đường cơ sở) |
| **Ngày giờ** | 2026/08/28 11:21:20 |
| **Tác giả** | Kiến trúc sư hệ thống doanh nghiệp (SA Agent) |
| **Phê duyệt** | Đang chờ đánh giá quản trị kỹ thuật |

## 1. Phạm Vi Hoạt Động & Mục Tiêu Của Giai Đoạn

Giai đoạn 1 thuộc dự án membership-hub tập trung vào việc xây dựng nền tảng kiến trúc vững chắc cho toàn bộ hệ thống quản lý thành viên đa trung tâm. Phạm vi hoạt động cốt lõi bao gồm bốn trụ cột chính: (1) Khởi tạo khung dự án Maven multi-module cho sáu microservice backend gồm `user-service`, `center-service`, `course-service`, `enrollment-service`, `attendance-service` và `notification-service` sử dụng Quarkus 3.15.1 trên nền tảng JDK 21 LTS; (2) Thiết lập frontend Next.js 14.2.5 với đầy đủ cấu hình TypeScript strict mode và manifest package chuẩn; (3) Triển khai Flyway migration scripts cho 11 bảng cơ sở dữ liệu lõi (Users, Roles, Centers, User_Center, Courses, Enrollments, Attendance, StudentCards, Notifications, Promotions, Announcements, SystemSettings) với đầy đủ ràng buộc UNIQUE, FOREIGN KEY, CHECK constraint và INDEXES tối ưu; (4) Xây dựng lớp bảo mật nền tảng gồm JWT filter chain xác thực RS256, OAuth2 resource server với JWKS endpoint, CORS policy cho phép kết nối từ frontend, OpenAPI 3.0 spec tổng hợp cho API Gateway và Kafka topic schemas cho ba topic chính (enrollment.created, teacher.assigned, attendance.recorded).

Mục tiêu kỹ thuật cụ thể của giai đoạn bao gồm việc đảm bảo mọi microservice expose đầy đủ endpoint `/q/health/ready`, `/q/health/live` và `/q/metrics` phục vụ Kubernetes probe, toàn bộ mã nguồn Java sử dụng package convention `org.nlh4j.membershiphub.<service>` theo chuẩn doanh nghiệp, áp dụng quy tắc đặt tên constraint thống nhất (`pk_*`, `fk_*`, `uq_*`, `ck_*`, `idx_*`), và đặc biệt thiết lập UNIQUE constraint composite `(student_id, course_id, attendance_date)` trên bảng `attendance` để đảm bảo idempotency cho luồng quét QR điểm danh theo yêu cầu nghiệp vụ [REQ-013] và [EXC-002]. Toàn bộ schema và mã nguồn phải tuân thủ chuẩn OWASP Top 10, hỗ trợ mã hóa bcrypt cost 12, ngăn chặn SQL injection thông qua PreparedStatement và Hibernate ORM Panache.

## 2. Phạm Vi Kỹ Thuật Được Phép & Ranh Giới Thư Mục

Danh sách kiểm tra kỹ thuật dưới đây định nghĩa 100% các tệp vật lý được phép khởi tạo trong phạm vi giai đoạn này, mỗi mục đại diện cho một tệp cụ thể kèm Tag ID truy vết:

* `./sources/backend/pom.xml` — [ARC-000]
* `./sources/backend/user-service/pom.xml` — [ARC-000]
* `./sources/backend/center-service/pom.xml` — [ARC-000]
* `./sources/backend/course-service/pom.xml` — [ARC-000]
* `./sources/backend/enrollment-service/pom.xml` — [ARC-000]
* `./sources/backend/attendance-service/pom.xml` — [ARC-000]
* `./sources/backend/notification-service/pom.xml` — [ARC-000]
* `./sources/frontend/package.json` — [ARC-000]
* `./sources/frontend/tsconfig.json` — [ARC-000]
* `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_roles.sql` — [DAT-001]
* `./sources/backend/user-service/src/main/resources/db/migration/V2__init_users_provider.sql` — [DAT-002]
* `./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql` — [DAT-003]
* `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql` — [DAT-004]
* `./sources/backend/enrollment-service/src/main/resources/db/migration/V1__init_enrollments.sql` — [DAT-005]
* `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql` — [DAT-006], [EXC-002]
* `./sources/backend/notification-service/src/main/resources/db/migration/V1__init_student_cards.sql` — [DAT-007]
* `./sources/backend/notification-service/src/main/resources/db/migration/V2__init_notifications.sql` — [DAT-008]
* `./sources/backend/notification-service/src/main/resources/db/migration/V3__init_promotions.sql` — [DAT-009]
* `./sources/backend/notification-service/src/main/resources/db/migration/V4__init_announcements.sql` — [DAT-010]
* `./sources/backend/notification-service/src/main/resources/db/migration/V5__init_system_settings.sql` — [DAT-011]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtFilter.java` — [ARC-006]
* `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/OAuth2ResourceServer.java` — [ARC-006]
* `./sources/backend/api-gateway/src/main/resources/openapi.yaml` — [ARC-006], [ARC-007], [ARC-008], [ARC-009]
* `./sources/backend/api-gateway/src/main/java/org/nlh4j/membershiphub/gateway/CorsFilter.java` — [ARC-006]
* `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/kafka/TopicSchemas.java` — [ARC-008]
* `./sources/backend/notification-service/src/main/resources/kafka-topics.yaml` — [ARC-008]
* `./sources/infra/test/maven-build-integration.sh` — [ARC-000]
* `./sources/infra/test/npm-install-integration.sh` — [ARC-000]
* `./sources/infra/test/migration-integration-test.sql` — [DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011]
* `./sources/infra/test/security-gateway-integration.sh` — [ARC-006], [ARC-009]
* `./sources/docs/architecture/SystemArchitectureBlueprint.md` — [DOC-001], [ARC-000]
* `./sources/docs/database/DatabaseSchemaTopology.md` — [DOC-001]
* `./sources/docs/api/OpenAPIContracts.md` — [DOC-001], [ARC-006], [ARC-007], [ARC-008], [ARC-009]
* `./sources/docs/review/phase1-day1-manifest-review.md` — [ARC-000]
* `./sources/docs/review/phase1-day2-migration-review.md` — [DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011]
* `./sources/docs/review/phase1-day3-security-review.md` — [ARC-006], [ARC-007], [ARC-008], [ARC-009]

* **BẮT BUỘC VỀ BỘ KHUNG NỀN TẢNG**: Khi khởi tạo blueprint vòng đời hoạt động (giới hạn cụ thể trong Giai đoạn 1 - NGÀY 1), cần phải tiêm và khai báo rõ ràng các bộ mô tả cấu trúc hạ tầng kho lưu trữ chính trước khi tạo bất kỳ thành phần mã nguồn ứng dụng nào. Đối với kiến trúc backend Microservices, phải thực thi định nghĩa đường dẫn bắt buộc của bộ mô tả dự án cha `./sources/backend/pom.xml` và các bộ mô tả module con riêng biệt `./sources/backend/<tên-dịch-vụ>/pom.xml`. Đối với lớp giao diện Frontend, cần thực thi đăng ký đường dẫn cấu hình rõ ràng của `./sources/frontend/package.json` và `./sources/frontend/tsconfig.json`. Toàn bộ tài sản khung được tạo ra phải ánh xạ chặt chẽ tới mã theo dõi kiến trúc hệ thống `[ARC-000]`.

## 3. Chỉ Thị Chức Năng Chuyên Biệt Cho Các Sub-Agent

*   **Coder**: Đóng vai trò Nhà phát triển ứng dụng cao cấp. Chịu trách nhiệm triển khai mã nguồn ứng dụng thuần túy trên cả dịch vụ backend và ứng dụng client frontend/mobile. Bị cấm viết bộ kiểm thử hoặc bản kê khai hạ tầng.
* **Tester**: Đóng vai trò Trưởng phòng QC/QA. Chuyên về kỹ thuật bộ kiểm thử, xác nhận hợp lệ và cổng gác chất lượng. Chịu trách nhiệm tạo JUnit, kiểm thử tích hợp, kiểm thử tự động E2E và script xác nhận hợp lệ hiệu năng. Bị cấm sửa đổi mã sản phẩm. Nếu nhiệm vụ phụ liên quan đến phạm vi tích hợp hoặc end-to-end tổng thể, phải xuất chính xác chuỗi ký tự `INTEGRATION_SCOPE` làm tham số đầu tiên của cặp dấu chấm phẩy.
* **Doc**: Đóng vai trò Technical Writer chính và Kiến trúc sư hệ thống doanh nghiệp. Chuyên biên soạn tài liệu đặc tả kỹ thuật toàn diện, tài liệu tham chiếu schema, bản thiết kế hệ thống và danh mục kiến trúc doanh nghiệp phù hợp với các lớp cấu trúc dự án đang hoạt động. Mọi tệp tài liệu kỹ thuật được tạo ra phải được liệt kê dưới dạng thực thể đường dẫn tệp rõ ràng kết thúc bằng phần mở rộng `.md` và nằm hoàn toàn trong bố cục lưu trữ tập trung `./sources/docs/`.
*   **Reviewer**: Chịu trách nhiệm xác minh trình biên dịch, cổng gác phân tích tĩnh và vá lỗi phòng thủ. Chuyên về kiểm toán chất lượng mã, giải quyết lỗi biên dịch, sửa lỗ hổng bảo mật OWASP và xử lý các blocker cổng chất lượng SonarQube.
*   **Docker**: Chuyên biệt về container hóa, kỹ thuật Dockerfile multi-stage, tối ưu gói và đẩy tài sản image ứng dụng đã xác minh lên DockerHub.
*   **GCP**: Chuyên về tự động hóa đám mây trong Google Cloud Platform. Chịu trách nhiệm xây dựng và đẩy image lên Google Cloud Artifact Registry (GCR), điều phối môi trường container nguyên bản trên Google Cloud Run.
*   **GKE**: Chuyên về điều phối container sản xuất bên trong Google Kubernetes Engine. Chịu trách nhiệm xây dựng bản kê khai triển khai Kubernetes, điều khiển định tuyến, cấu hình HPA, biểu đồ Helm và triển khai khối lượng công việc microservice vào cụm GKE đang hoạt động.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn (DoD)

Giai đoạn 1 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: (1) Toàn bộ 9 tệp manifest cấu hình bao gồm `pom.xml` gốc, 6 tệp `pom.xml` con cho sáu microservice backend, `package.json` và `tsconfig.json` cho frontend được tạo thành công và có thể biên dịch chéo module mà không phát sinh lỗi. (2) Toàn bộ 11 tệp Flyway migration scripts được thực thi thành công trên PostgreSQL 16 test container, tạo ra chính xác 11 bảng lõi với 100% ràng buộc UNIQUE, FOREIGN KEY, CHECK và INDEXES theo đặc tả. (3) UNIQUE constraint composite `(student_id, course_id, attendance_date)` trên bảng `attendance` hoạt động đúng, khi insert trùng composite key hệ thống trả về lỗi SQLSTATE 23505 xác nhận cơ chế idempotency cho [REQ-013] và [EXC-002]. (4) Lớp bảo mật JWT filter chain trả về HTTP 401 cho request không có token, HTTP 200 cho request có token hợp lệ, xác nhận [ARC-006] hoạt động. (5) CORS policy cho phép preflight OPTIONS request từ `https://app.membershiphub.example.com` với đầy đủ header theo đặc tả. (6) OpenAPI 3.0 spec tại `./sources/backend/api-gateway/src/main/resources/openapi.yaml` validate thành công bằng Swagger Parser với 5 nhóm endpoint chính (auth, centers, courses, attendance, reports). (7) Bộ ba tài liệu kỹ thuật tại `./sources/docs/` gồm System Architecture Blueprint, Database Schema Topology và OpenAPI Contracts được biên soạn đầy đủ bằng tiếng Việt với mục lục rõ ràng và Tag ID truy vết chính xác. (8) 100% Tag ID của giai đoạn (gồm [ARC-000], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [DAT-001] đến [DAT-011], [DOC-001], [EXC-002]) được ánh xạ đầy đủ trong các tệp mã nguồn và tài liệu.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->Khởi Tạo Khung Dự Án Và Bản Kê Khai Đa Module<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 1.1: Tạo tệp pom.xml gốc và quản lý module cha
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Khởi tạo Maven multi-module parent POM tại đường dẫn `./sources/backend/pom.xml` với `<groupId>org.nlh4j.membershiphub</groupId>`, `<artifactId>membershiphub-backend</artifactId>`, `<version>1.0.0-SNAPSHOT</version>` và packaging `pom`. Liệt kê chính xác sáu module con trong thẻ `<modules>` theo thứ tự: `user-service`, `center-service`, `course-service`, `enrollment-service`, `attendance-service`, `notification-service`. Import `quarkus-bom:3.15.1` vào `<dependencyManagement>` để quản lý phiên bản thống nhất cho tất cả dependency Quarkus. Cấu hình `maven-compiler-plugin` với `<source>21</source>` và `<target>21</target>`, đồng thời khai báo `quarkus-maven-plugin` và `flyway-maven-plugin` để hỗ trợ build và di trú cơ sở dữ liệu. Đảm bảo tệp là XML hợp lệ, sẵn sàng cho `mvn clean validate compile` thành công mà không cần chỉnh sửa.

#### 📝 Nhiệm vụ phụ 1.2: Tạo tệp pom.xml riêng cho user-service
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/user-service/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp POM con cho `user-service` tại đường dẫn `./sources/backend/user-service/pom.xml` kế thừa từ `./sources/backend/pom.xml` thông qua khối `<parent>` với `<groupId>org.nlh4j.membershiphub</groupId>`, `<artifactId>membershiphub-backend</artifactId>` và `<version>1.0.0-SNAPSHOT</version>`. Khai báo `<artifactId>user-service</artifactId>`. Thêm đầy đủ dependencies Quarkus cần thiết cho chức năng xác thực và quản lý người dùng: `quarkus-resteasy-reactive-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-smallrye-jwt-build`, `quarkus-arc`, `quarkus-rest-client-reactive-jackson`, `quarkus-hibernate-validator`. Cấu hình `quarkus-maven-plugin` chuẩn với extension `quarkus-container-image-jib`. Đảm bảo tệp có thể build standalone khi được gọi trực tiếp từ root thông qua `mvn -f ./sources/backend/user-service/pom.xml compile`.

#### 📝 Nhiệm vụ phụ 1.3: Tạo tệp pom.xml cho năm service còn lại
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/notification-service/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Lặp lại cấu trúc parent-child POM cho năm service còn lại tại các đường dẫn `./sources/backend/center-service/pom.xml`, `./sources/backend/course-service/pom.xml`, `./sources/backend/enrollment-service/pom.xml`, `./sources/backend/attendance-service/pom.xml`, `./sources/backend/notification-service/pom.xml`. Mỗi POM con kế thừa `<parent>` từ `./sources/backend/pom.xml` với cùng `<groupId>` và `<version>`, đồng thời đặt `<artifactId>` khớp chính xác tên module. Service `notification-service` cần bổ sung thêm dependency `quarkus-messaging-kafka` để hỗ trợ tích hợp Apache Kafka 3.7.0, `quarkus-rest-client-reactive-jackson` cho FCM/APNs gateway, và `quarkus-smallrye-health`. Service `attendance-service` cần thêm `quarkus-redis-client` cho idempotency key cache. Tất cả artifactId phải khớp tên module và đảm bảo mỗi tệp là XML hợp lệ có thể compile độc lập từ root thông qua Maven multi-module.

#### 📝 Nhiệm vụ phụ 1.4: Khởi tạo tệp package.json và tsconfig.json cho frontend Next.js
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/frontend/package.json`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp `./sources/frontend/package.json` cho dự án Next.js 14.2.5 với cấu trúc chuẩn. Khai báo `name: "membershiphub-frontend"`, `version: "1.0.0"`, `private: true`. Định nghĩa scripts: `dev` chạy `next dev`, `build` chạy `next build`, `start` chạy `next start`, `lint` chạy `next lint`, `type-check` chạy `tsc --noEmit`. Thêm dependencies: `next: 14.2.5`, `react: 18.3.1`, `react-dom: 18.3.1`, `next-intl: 3.17.2`, `next-i18next: 14.1.2`, `firebase: 10.13.0`, `axios: 1.7.4`. Thêm devDependencies: `typescript: 5.5.4`, `@types/react: 18.3.3`, `@types/node: 20.14.10`, `eslint: 8.57.0`, `prettier: 3.3.3`, `tailwindcss: 3.4.10`. Đồng thời tạo tệp `./sources/frontend/tsconfig.json` với `"strict": true`, `"target": "ES2022"`, `"module": "ESNext"`, `"moduleResolution": "Bundler"`, đường dẫn alias `"@/*": ["./src/*"]`. Đảm bảo cả hai tệp JSON hợp lệ, sẵn sàng cho `npm install` thành công.

#### 📝 Nhiệm vụ phụ 1.5: Kiểm thử tích hợp biên dịch đa module
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/infra/test/maven-build-integration.sh
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo script shell `./sources/infra/test/maven-build-integration.sh` thực thi tuần tự các bước kiểm thử: (1) `mvn -f ./sources/backend/pom.xml clean validate` để xác nhận Maven resolution hoạt động đúng; (2) `mvn -f ./sources/backend/pom.xml compile` để biên dịch toàn bộ sáu module backend; (3) `cd ./sources/frontend && npm install --dry-run` để xác nhận tất cả dependency trong `package.json` tồn tại trên npm registry. Đồng thời tạo script `./sources/infra/test/npm-install-integration.sh` chạy `npm install --dry-run` riêng cho frontend. Mỗi script phải ghi log chi tiết kết quả từng bước ra stdout, exit code 0 khi tất cả assertion pass, exit code khác 0 kèm thông điệp lỗi rõ ràng khi thất bại. Bổ sung shebang `#!/bin/bash` và `set -e` để dừng ngay khi gặp lỗi.

#### 📝 Nhiệm vụ phụ 1.6: Đánh giá cấu trúc manifest và chuẩn đặt tên
##### Sub-Agent được phân công: Reviewer
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/notification-service/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Kiểm tra chéo toàn bộ 9 tệp manifest gồm `./sources/backend/pom.xml`, 6 tệp `pom.xml` con cho sáu microservice backend, `./sources/frontend/package.json` và `./sources/frontend/tsconfig.json` để đảm bảo: (1) tất cả `<artifactId>` đồng bộ và không trùng lặp giữa các module; (2) phiên bản Quarkus BOM thống nhất 3.15.1 trong tất cả tệp; (3) Java target là 21 cho toàn bộ; (4) không còn tham chiếu đến `com.example` ở bất kỳ đâu trong codebase; (5) tất cả dependency `quarkus-*` đều resolve được thông qua BOM. Tạo báo cáo review tại `./sources/docs/review/phase1-day1-manifest-review.md` nêu rõ từng issue phát hiện, đề xuất fix cụ thể và liệt kê 100% Tag ID được xác minh.

#### 📝 Nhiệm vụ phụ 1.7: Biên soạn tài liệu kiến trúc tổng quan
##### Sub-Agent được phân công: Doc
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/docs/architecture/SystemArchitectureBlueprint.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000], [DOC-001]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Biên soạn tài liệu Markdown `./sources/docs/architecture/SystemArchitectureBlueprint.md` mô tả tổng quan kiến trúc hệ thống membership-hub. Nội dung bắt buộc gồm: (1) Sơ đồ microservice gồm 6 service Quarkus (user-service, center-service, course-service, enrollment-service, attendance-service, notification-service), API Gateway, Frontend Next.js; (2) Công nghệ stack: Quarkus 3.15.1, Java 21, PostgreSQL 16, Apache Kafka 3.7.0, Redis 7.2, Firebase FCM; (3) Mô hình triển khai: GKE, Artifact Registry, Helm chart; (4) Bản đồ luồng dữ liệu chính: Authentication Flow [ARC-006], Attendance QR Processing Flow [ARC-007], Notification Delivery Flow [ARC-008], Mobile App Backend Integration Flow [ARC-009]. Sử dụng Mermaid để vẽ sơ đồ C4 Container. Tài liệu phải viết bằng tiếng Việt, có mục lục rõ ràng và Tag ID truy vết đầy đủ ở mỗi phần.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->Triển Khai Flyway Migration Cho 11 Bảng Cơ Sở Dữ Liệu Lõi<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 2.1: Migration bảng roles và users
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_roles.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-001]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp SQL tại đường dẫn `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_roles.sql` chứa DDL tạo hai bảng `roles` và `users`. Bảng `roles` gồm `role_id SMALLINT NOT NULL PRIMARY KEY`, `name VARCHAR(30) NOT NULL UNIQUE`, `description VARCHAR(200) NULL` với CHECK constraint `name IN ('SystemAdmin','CenterAdmin','Manager','Teacher','Student')`. Bảng `users` gồm `user_id UUID NOT NULL PRIMARY KEY`, `email VARCHAR(255) NOT NULL UNIQUE` với CHECK `email LIKE '%_@__%.__%'`, `password_hash CHAR(60) NOT NULL`, `full_name VARCHAR(100) NOT NULL`, `role_id SMALLINT NOT NULL` với FOREIGN KEY tham chiếu `roles(role_id)`, `created_at TIMESTAMP NOT NULL DEFAULT now()`, `updated_at TIMESTAMP NOT NULL DEFAULT now()`. Bổ sung INDEX `idx_users_role_id` trên cột `role_id`. Đảm bảo file SQL thuần ANSI, không sử dụng ENUM inline mà dùng VARCHAR kết hợp CHECK constraint.

* **Đặc tả DDL SQL lược đồ cơ sở dữ liệu [DAT-001]:** <!--START_DDL_MIGRATION-->
```sql
-- ============================================================
-- MIGRATION: V1__init_users_roles.sql  (user-service)
-- Tag ID: [DAT-001]
-- ============================================================
CREATE TABLE roles (
    role_id SMALLINT NOT NULL,
    name VARCHAR(30) NOT NULL,
    description VARCHAR(200) NULL,
    CONSTRAINT pk_roles PRIMARY KEY (role_id),
    CONSTRAINT uq_roles_name UNIQUE (name),
    CONSTRAINT ck_roles_name CHECK (name IN ('SystemAdmin','CenterAdmin','Manager','Teacher','Student'))
);

CREATE TABLE users (
    user_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash CHAR(60) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role_id SMALLINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id),
    CONSTRAINT ck_users_email_format CHECK (email LIKE '%_@__%.__%')
);

CREATE INDEX idx_users_role_id ON users(role_id);
```
<!--END_DDL_MIGRATION-->

#### 📝 Nhiệm vụ phụ 2.2: Migration bổ sung cột provider cho bảng users
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/user-service/src/main/resources/db/migration/V2__init_users_provider.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-002]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp SQL tại đường dẫn `./sources/backend/user-service/src/main/resources/db/migration/V2__init_users_provider.sql` thực thi lệnh `ALTER TABLE users ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'local'` và thêm ràng buộc `ALTER TABLE users ADD CONSTRAINT ck_users_provider CHECK (provider IN ('local','firebase','google','facebook'))`. Migration phải tương thích ngược với V1, không phá vỡ dữ liệu hiện có, đảm bảo giá trị mặc định `'local'` cho mọi bản ghi User đã tồn tại trước khi áp dụng migration.

* **Đặc tả DDL SQL lược đồ cơ sở dữ liệu [DAT-002]:** <!--START_DDL_MIGRATION-->
```sql
-- ============================================================
-- MIGRATION: V2__init_users_provider.sql  (user-service)
-- Tag ID: [DAT-002]
-- ============================================================
ALTER TABLE users ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'local';
ALTER TABLE users ADD CONSTRAINT ck_users_provider
    CHECK (provider IN ('local','firebase','google','facebook'));
```
<!--END_DDL_MIGRATION-->

#### 📝 Nhiệm vụ phụ 2.3: Migration bảng centers và user_center
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-003]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp SQL tại đường dẫn `./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql` định nghĩa hai bảng `centers` và `user_center`. Bảng `centers` gồm `center_id UUID NOT NULL PRIMARY KEY`, `name VARCHAR(100) NOT NULL`, `address VARCHAR(255) NOT NULL`, `tax_id VARCHAR(20) NOT NULL UNIQUE` với CHECK `tax_id ~ '^[0-9]{10,13}$'`, `contact_phone VARCHAR(20) NULL` với CHECK regex số điện thoại, `contact_email VARCHAR(100) NULL`. Bảng `user_center` thiết lập quan hệ nhiều-nhiều giữa users và centers với composite PRIMARY KEY `(user_id, center_id)`, FOREIGN KEY tham chiếu `users(user_id)` và `centers(center_id)` với `ON DELETE CASCADE`, cùng cột `assigned_at TIMESTAMP NOT NULL DEFAULT now()`. Ràng buộc UNIQUE `tax_id` đảm bảo ngăn chặn trùng lặp theo yêu cầu nghiệp vụ.

* **Đặc tả DDL SQL lược đồ cơ sở dữ liệu [DAT-003]:** <!--START_DDL_MIGRATION-->
```sql
-- ============================================================
-- MIGRATION: V1__init_centers.sql  (center-service)
-- Tag ID: [DAT-003]
-- ============================================================
CREATE TABLE centers (
    center_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tax_id VARCHAR(20) NOT NULL,
    contact_phone VARCHAR(20) NULL,
    contact_email VARCHAR(100) NULL,
    CONSTRAINT pk_centers PRIMARY KEY (center_id),
    CONSTRAINT uq_centers_tax_id UNIQUE (tax_id),
    CONSTRAINT ck_centers_tax_id CHECK (tax_id ~ '^[0-9]{10,13}$'),
    CONSTRAINT ck_centers_phone CHECK (contact_phone IS NULL OR contact_phone ~ '^[+0-9 ()\-]+$')
);

CREATE TABLE user_center (
    user_id UUID NOT NULL,
    center_id UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_user_center PRIMARY KEY (user_id, center_id),
    CONSTRAINT fk_uc_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_uc_center FOREIGN KEY (center_id) REFERENCES centers(center_id) ON DELETE CASCADE
);
```
<!--END_DDL_MIGRATION-->

#### 📝 Nhiệm vụ phụ 2.4: Migration bảng courses và enrollments
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-004], [DAT-005]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo hai tệp SQL. Tệp thứ nhất tại `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql` định nghĩa bảng `courses` gồm `course_id UUID NOT NULL PRIMARY KEY`, `title VARCHAR(150) NOT NULL`, `description TEXT NULL`, `start_date DATE NOT NULL`, `end_date DATE NOT NULL` với CHECK `end_date >= start_date`, `teacher_id UUID NOT NULL` với FOREIGN KEY tham chiếu `users(user_id)`, `max_students INT NOT NULL DEFAULT 30` với CHECK `max_students > 0`, bổ sung INDEX `idx_courses_teacher_id` và INDEX `idx_courses_dates`. Tệp thứ hai tại `./sources/backend/enrollment-service/src/main/resources/db/migration/V1__init_enrollments.sql` định nghĩa bảng `enrollments` gồm `enrollment_id UUID NOT NULL PRIMARY KEY`, `student_id UUID NOT NULL` với FOREIGN KEY `ON DELETE CASCADE` tham chiếu `users(user_id)`, `course_id UUID NOT NULL` với FOREIGN KEY `ON DELETE CASCADE` tham chiếu `courses(course_id)`, `enrollment_date TIMESTAMP NOT NULL DEFAULT now()`, UNIQUE composite `(student_id, course_id)`, kèm INDEX `idx_enr_student` và `idx_enr_course`.

* **Đặc tả DDL SQL lược đồ cơ sở dữ liệu [DAT-004], [DAT-005]:** <!--START_DDL_MIGRATION-->
```sql
-- ============================================================
-- MIGRATION: V1__init_courses.sql  (course-service)
-- Tag ID: [DAT-004]
-- ============================================================
CREATE TABLE courses (
    course_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    teacher_id UUID NOT NULL,
    max_students INT NOT NULL DEFAULT 30,
    CONSTRAINT pk_courses PRIMARY KEY (course_id),
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id),
    CONSTRAINT ck_courses_date_range CHECK (end_date >= start_date),
    CONSTRAINT ck_courses_max_students CHECK (max_students > 0)
);

CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_courses_dates ON courses(start_date, end_date);

-- ============================================================
-- MIGRATION: V1__init_enrollments.sql  (enrollment-service)
-- Tag ID: [DAT-005]
-- ============================================================
CREATE TABLE enrollments (
    enrollment_id UUID NOT NULL,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    enrollment_date TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_enrollments PRIMARY KEY (enrollment_id),
    CONSTRAINT fk_enr_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_enr_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT uq_enr_student_course UNIQUE (student_id, course_id)
);

CREATE INDEX idx_enr_student ON enrollments(student_id);
CREATE INDEX idx_enr_course ON enrollments(course_id);
```
<!--END_DDL_MIGRATION-->

#### 📝 Nhiệm vụ phụ 2.5: Migration bảng attendance với idempotency
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-006], [EXC-002]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp SQL tại đường dẫn `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql` định nghĩa bảng `attendance` gồm `attendance_id UUID NOT NULL PRIMARY KEY`, `student_id UUID NOT NULL` với FOREIGN KEY tham chiếu `users(user_id)`, `course_id UUID NOT NULL` với FOREIGN KEY tham chiếu `courses(course_id)`, `attendance_date DATE NOT NULL`, `timestamp TIMESTAMP NOT NULL DEFAULT now()`. Đặc biệt bổ sung UNIQUE composite constraint đặt tên `uq_attendance_composite` trên bộ ba cột `(student_id, course_id, attendance_date)` - đây là ràng buộc cốt lõi đảm bảo idempotency cho [REQ-013] và [EXC-002], ngăn chặn việc tạo bản ghi điểm danh trùng lặp khi học viên quét QR nhiều lần trong cùng ngày. Bổ sung INDEX `idx_att_student_date` trên `(student_id, attendance_date)` và INDEX `idx_att_course_date` trên `(course_id, attendance_date)` để tối ưu truy vấn báo cáo.

* **Đặc tả DDL SQL lược đồ cơ sở dữ liệu [DAT-006], [EXC-002]:** <!--START_DDL_MIGRATION-->
```sql
-- ============================================================
-- MIGRATION: V1__init_attendance.sql  (attendance-service)
-- Tag ID: [DAT-006], [EXC-002]
-- ============================================================
CREATE TABLE attendance (
    attendance_id UUID NOT NULL,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),
    CONSTRAINT fk_att_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT fk_att_course FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT uq_attendance_composite UNIQUE (student_id, course_id, attendance_date)
);

CREATE INDEX idx_att_student_date ON attendance(student_id, attendance_date);
CREATE INDEX idx_att_course_date ON attendance(course_id, attendance_date);
```
<!--END_DDL_MIGRATION-->

* **Bộ xử lý ngoại lệ cục bộ hóa của giai đoạn [EXC-002]:** <!--START_EXC_HANDLER-->
```java
package org.nlh4j.membershiphub.attendanceservice.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.postgresql.util.PSQLException;

/**
 * Bắt lỗi UNIQUE constraint composite (student_id, course_id, attendance_date)
 * và chuyển thành phản hồi 200 OK với cờ duplicate=true theo [EXC-002].
 */
@Provider
public class DuplicateAttendanceExceptionMapper implements ExceptionMapper<PSQLException> {

    @Override
    public Response toResponse(PSQLException ex) {
        if (ex.getSQLState() != null && ex.getSQLState().equals("23505")
                && ex.getMessage() != null && ex.getMessage().contains("uq_attendance_composite")) {
            return Response.ok()
                    .entity("{\"status\":\"success\",\"duplicate\":true,\"message\":\"Attendance already recorded\"}")
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"Database integrity violation\"}")
                .build();
    }
}
```
<!--END_EXC_HANDLER-->

#### 📝 Nhiệm vụ phụ 2.6: Migration bảng student_cards, notifications, promotions, announcements, system_settings
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/notification-service/src/main/resources/db/migration/V1__init_student_cards.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo năm tệp SQL trong thư mục `./sources/backend/notification-service/src/main/resources/db/migration/`. Tệp V1__init_student_cards.sql tạo bảng `student_cards` gồm `card_id UUID NOT NULL PRIMARY KEY`, `student_id UUID NOT NULL` với FOREIGN KEY `ON DELETE CASCADE` tham chiếu `users(user_id)`, `issue_date DATE NOT NULL`, `validity_days INT NOT NULL` với CHECK `validity_days > 0`, INDEX `idx_card_student`. Tệp V2__init_notifications.sql tạo bảng `notifications` gồm `notification_id UUID NOT NULL PRIMARY KEY`, `user_id UUID NULL` với FOREIGN KEY `ON DELETE SET NULL`, `group_zalo VARCHAR(50) NULL`, `message TEXT NOT NULL`, `sent_at TIMESTAMP NOT NULL DEFAULT now()`, `delivered BOOLEAN NOT NULL DEFAULT false`, CHECK `(user_id IS NOT NULL OR group_zalo IS NOT NULL)`, INDEX `idx_notif_user` và `idx_notif_delivered`. Tệp V3__init_promotions.sql tạo bảng `promotions` gồm `promo_id UUID NOT NULL PRIMARY KEY`, `code VARCHAR(30) NOT NULL UNIQUE`, `discount_percent SMALLINT NOT NULL` với CHECK BETWEEN 1 AND 100, `start_date DATE NULL`, `end_date DATE NULL` với CHECK `end_date IS NULL OR end_date >= start_date`, `description TEXT NULL`. Tệp V4__init_announcements.sql tạo bảng `announcements` gồm `announcement_id UUID NOT NULL PRIMARY KEY`, `title VARCHAR(150) NOT NULL` với CHECK `char_length(title) <= 150`, `content TEXT NOT NULL` với CHECK `char_length(content) <= 2000`, `start_date DATE NULL`, `end_date DATE NULL` với CHECK `end_date IS NULL OR end_date >= start_date`. Tệp V5__init_system_settings.sql tạo bảng `system_settings` gồm `setting_key VARCHAR(50) NOT NULL PRIMARY KEY`, `setting_value TEXT NOT NULL`, `description VARCHAR(200) NULL`.

* **Đặc tả DDL SQL lược đồ cơ sở dữ liệu [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011]:** <!--START_DDL_MIGRATION-->
```sql
-- ============================================================
-- MIGRATION: V1__init_student_cards.sql  (notification-service shared schema)
-- Tag ID: [DAT-007]
-- ============================================================
CREATE TABLE student_cards (
    card_id UUID NOT NULL,
    student_id UUID NOT NULL,
    issue_date DATE NOT NULL,
    validity_days INT NOT NULL,
    CONSTRAINT pk_student_cards PRIMARY KEY (card_id),
    CONSTRAINT fk_card_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT ck_card_validity CHECK (validity_days > 0)
);

CREATE INDEX idx_card_student ON student_cards(student_id);

-- ============================================================
-- MIGRATION: V2__init_notifications.sql
-- Tag ID: [DAT-008]
-- ============================================================
CREATE TABLE notifications (
    notification_id UUID NOT NULL,
    user_id UUID NULL,
    group_zalo VARCHAR(50) NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    delivered BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT pk_notifications PRIMARY KEY (notification_id),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT ck_notif_target CHECK (user_id IS NOT NULL OR group_zalo IS NOT NULL)
);

CREATE INDEX idx_notif_user ON notifications(user_id);
CREATE INDEX idx_notif_delivered ON notifications(delivered);

-- ============================================================
-- MIGRATION: V3__init_promotions.sql
-- Tag ID: [DAT-009]
-- ============================================================
CREATE TABLE promotions (
    promo_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    discount_percent SMALLINT NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    description TEXT NULL,
    CONSTRAINT pk_promotions PRIMARY KEY (promo_id),
    CONSTRAINT uq_promotions_code UNIQUE (code),
    CONSTRAINT ck_promo_percent CHECK (discount_percent BETWEEN 1 AND 100),
    CONSTRAINT ck_promo_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

-- ============================================================
-- MIGRATION: V4__init_announcements.sql
-- Tag ID: [DAT-010]
-- ============================================================
CREATE TABLE announcements (
    announcement_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    CONSTRAINT pk_announcements PRIMARY KEY (announcement_id),
    CONSTRAINT ck_ann_title_len CHECK (char_length(title) <= 150),
    CONSTRAINT ck_ann_content_len CHECK (char_length(content) <= 2000),
    CONSTRAINT ck_ann_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

-- ============================================================
-- MIGRATION: V5__init_system_settings.sql
-- Tag ID: [DAT-011]
-- ============================================================
CREATE TABLE system_settings (
    setting_key VARCHAR(50) NOT NULL,
    setting_value TEXT NOT NULL,
    description VARCHAR(200) NULL,
    CONSTRAINT pk_system_settings PRIMARY KEY (setting_key)
);
```
<!--END_DDL_MIGRATION-->

#### 📝 Nhiệm vụ phụ 2.7: Kiểm thử đường ống migration PostgreSQL
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/infra/test/migration-integration-test.sql
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo script kiểm thử tích hợp tại đường dẫn `./sources/infra/test/migration-integration-test.sql` thực thi toàn bộ quy trình kiểm thử di trú trên PostgreSQL 16. Script phải bao gồm các bước: (1) Khởi tạo schema tạm thời bằng `CREATE SCHEMA test_migration;` và `SET search_path TO test_migration;`; (2) Thực thi tuần tự 11 tệp migration từ sáu service theo thứ tự phụ thuộc (users, roles, centers, courses, enrollments, attendance, student_cards, notifications, promotions, announcements, system_settings); (3) Verify số bảng tạo thành công lớn hơn hoặc bằng 11 bằng truy vấn `SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'test_migration';`; (4) Kiểm tra UNIQUE constraint trên bảng attendance bằng cách insert hai bản ghi trùng composite key `(student_id, course_id, attendance_date)` và expect lỗi SQLSTATE 23505; (5) Kiểm tra CHECK constraint trên bảng roles với giá trị không hợp lệ như `'InvalidRole'`, expect lỗi CHECK violation; (6) Kiểm tra CHECK constraint trên cột email của users với giá trị không chứa ký tự `@`, expect lỗi CHECK violation. Kết quả pass/fail phải được ghi ra stdout với mã exit tương ứng (0 cho pass, 1 cho fail) để pipeline CI/CD có thể tự động đánh giá.

#### 📝 Nhiệm vụ phụ 2.8: Đánh giá chất lượng migration và tối ưu index
##### Sub-Agent được phân công: Reviewer
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/enrollment-service/src/main/resources/db/migration/V1__init_enrollments.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Review toàn bộ 11 tệp migration đã tạo ở các bước trước. Kiểm tra chéo bốn tiêu chí chất lượng: (1) Mọi FOREIGN KEY đều có `ON DELETE` được chỉ định hợp lý (CASCADE cho quan hệ sở hữu, SET NULL cho quan hệ tham chiếu, RESTRICT cho quan hệ bảo vệ); (2) CHECK constraint đầy đủ cho mọi string enum thay vì dùng PostgreSQL ENUM inline, đảm bảo khả năng mở rộng dễ dàng; (3) Chỉ mục được tạo cho các cột truy vấn thường xuyên bao gồm `email`, `role_id`, `teacher_id`, `attendance_date`, `course_id`, `student_id`, `delivered`, `start_date`/`end_date`; (4) Tên constraint tuân thủ convention thống nhất: `pk_*` cho PRIMARY KEY, `fk_*` cho FOREIGN KEY, `uq_*` cho UNIQUE, `ck_*` cho CHECK, `idx_*` cho INDEX. Phát hiện và đề xuất bổ sung các index thiếu cho các hot path query. Tạo báo cáo chi tiết tại đường dẫn `./sources/docs/review/phase1-day2-migration-review.md` liệt kê từng issue phát hiện, đề xuất fix cụ thể và xác nhận 100% Tag ID [DAT-001] đến [DAT-011] đã được xác minh.

#### 📝 Nhiệm vụ phụ 2.9: Biên soạn tài liệu Database Schema Topology
##### Sub-Agent được phân công: Doc
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/docs/database/DatabaseSchemaTopology.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu Markdown tại đường dẫn `./sources/docs/database/DatabaseSchemaTopology.md` mô tả toàn bộ topology 11 bảng cơ sở dữ liệu. Nội dung bắt buộc gồm: (1) Mục lục rõ ràng; (2) Sơ đồ ER tổng thể sử dụng cú pháp Mermaid `erDiagram` thể hiện quan hệ giữa 11 bảng; (3) Phần mô tả chi tiết cho từng bảng gồm tên bảng, Tag ID tương ứng, danh sách cột với kiểu dữ liệu, ràng buộc PRIMARY KEY/UNIQUE/CHECK/FOREIGN KEY, INDEX liên quan; (4) Giải thích ý nghĩa nghiệp vụ của từng bảng và mối quan hệ giữa chúng; (5) Chiến lược phân vùng migration theo microservice, giải thích lý do bảng `student_cards`, `notifications`, `promotions`, `announcements`, `system_settings` được đặt trong schema của `notification-service`; (6) Bảng đối chiếu 1:1 giữa 11 Tag ID `[DAT-001]` đến `[DAT-011]` với tên tệp migration tương ứng. Tài liệu phải viết bằng tiếng Việt, có sơ đồ Mermaid rõ ràng và Tag ID truy vết chính xác ở mỗi mục.

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->Thiết Lập Lớp Bảo Mật, Gateway Và Kafka Topic Schemas<!--DAY_HEADER_END-->

#### 📝 Nhiệm vụ phụ 3.1: Triển khai JWT Filter Chain
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtFilter.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo class Java tại đường dẫn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtFilter.java` thuộc package `org.nlh4j.membershiphub.userservice.security`. Class triển khai interface `ContainerRequestFilter` với annotation `@Provider` và `@Priority(Priorities.AUTHENTICATION)`. Inject `JwtParser` từ SmallRye JWT thông qua `@Inject`. Phương thức `filter(ContainerRequestContext requestContext)` đọc header `Authorization` với định dạng `Bearer <token>`, gọi `jwtParser.parse(token)` để xác minh chữ ký RS256 và parse các claim. Trích xuất claim `sub` (user UUID), `groups` (danh sách role), `exp` (thời gian hết hạn). Khi thiếu token hoặc token không hợp lệ, gọi `requestContext.abortWith(Response.status(401).build())`. Khi token hết hạn, abort với 401 và thông điệp "Token expired". Khi role không hợp lệ so với yêu cầu endpoint, abort với 403. Lưu principal là user UUID vào `SecurityContext` thông qua `requestContext.setSecurityContext()`. Đảm bảo filter chỉ áp dụng cho các đường dẫn bắt đầu bằng `/api/v1/`.

* **Hợp đồng định tuyến API và sự kiện [ARC-006]:** <!--START_API_CONTRACT-->
```json
{
  "openapi": "3.0.3",
  "info": {
    "title": "Membership Hub - JWT Filter Security Contract",
    "version": "1.0.0",
    "description": "JWT filter chain specification cho mọi endpoint /api/v1/** theo [ARC-006]"
  },
  "components": {
    "securitySchemes": {
      "bearerAuth": {
        "type": "http",
        "scheme": "bearer",
        "bearerFormat": "JWT"
      }
    }
  },
  "security": [{ "bearerAuth": [] }],
  "paths": {
    "/api/v1/auth/register": {
      "post": {
        "summary": "Reference endpoint - triển khai chi tiết ở Giai đoạn 2",
        "security": []
      }
    }
  }
}
```
<!--END_API_CONTRACT-->

#### 📝 Nhiệm vụ phụ 3.2: Cấu hình OAuth2 Resource Server
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/OAuth2ResourceServer.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo class Java tại đường dẫn `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/OAuth2ResourceServer.java` thuộc package `org.nlh4j.membershiphub.userservice.security`. Class đóng vai trò cấu hình tập trung cho `quarkus-smallrye-jwt` thông qua việc khai báo các `@ConfigProperty`. Các thuộc tính bắt buộc gồm: `mp.jwt.verify.publickey.location` trỏ tới JWKS endpoint từ identity provider, `mp.jwt.verify.issuer` định danh issuer hợp lệ, `smallrye.jwt.path.sub` thiết lập đường dẫn claim sub, `smallrye.jwt.always-check-authorization` bật chế độ kiểm tra quyền liên tục. Đồng thời tạo class `Application` với annotation `@ApplicationPath("/api")` để mount toàn bộ REST endpoint theo context path chuẩn. Class chỉ chứa cấu hình, không chứa logic nghiệp vụ.

#### 📝 Nhiệm vụ phụ 3.3: Triển khai CORS Filter cho API Gateway
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/api-gateway/src/main/java/org/nlh4j/membershiphub/gateway/CorsFilter.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006], [ARC-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo class Java tại đường dẫn `./sources/backend/api-gateway/src/main/java/org/nlh4j/membershiphub/gateway/CorsFilter.java` thuộc package `org.nlh4j.membershiphub.gateway`. Class triển khai hai interface: `ContainerResponseFilter` để áp dụng CORS header cho mọi response, và `ContainerRequestFilter` để xử lý preflight OPTIONS request. Trong `ContainerResponseFilter.filter()`, thêm các header sau vào response: `Access-Control-Allow-Origin: https://app.membershiphub.example.com` (cấm wildcard), `Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS`, `Access-Control-Allow-Headers: Authorization,Content-Type,Accept-Language`, `Access-Control-Allow-Credentials: true`, `Access-Control-Max-Age: 3600`. Trong `ContainerRequestFilter.filter()`, kiểm tra nếu method là OPTIONS, gọi `requestContext.abortWith(Response.status(200).build())` để trả về 200 OK ngay cho preflight request. Class phải là API Gateway infrastructure, không chứa logic nghiệp vụ.

#### 📝 Nhiệm vụ phụ 3.4: Tạo OpenAPI 3.0 spec cho Gateway
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/api-gateway/src/main/resources/openapi.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006], [ARC-007], [ARC-008], [ARC-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tệp YAML tại đường dẫn `./sources/backend/api-gateway/src/main/resources/openapi.yaml` chứa OpenAPI 3.0.3 spec cho API Gateway tổng hợp. Nội dung bắt buộc gồm: (1) Khối `info` với `title: "Membership Hub API Gateway"`, `version: "1.0.0"`, `description` tham chiếu đến các Tag ID [ARC-006] đến [ARC-009]; (2) Khối `servers` với URL production `https://api.membershiphub.example.com`; (3) Khối `components.securitySchemes` định nghĩa `bearerAuth` (HTTP Bearer với JWT) và `oauth2` (OAuth2 authorization code flow với scopes openid, profile, email); (4) Khối `security` mặc định yêu cầu `bearerAuth`; (5) Khối `paths` tham chiếu đến 5 nhóm endpoint chính gồm `/api/v1/auth/register`, `/api/v1/auth/login`, `/api/v1/centers`, `/api/v1/courses`, `/api/v1/attendance/scan`, `/api/v1/reports/attendance`. Mỗi path chỉ chứa summary stub và `$ref` đến file openapi.yaml riêng của từng service (sẽ triển khai chi tiết ở các giai đoạn sau). Spec phải validate thành công bằng Swagger Parser hoặc swagger-cli.

* **Hợp đồng định tuyến API và sự kiện [ARC-006], [ARC-007], [ARC-008], [ARC-009]:** <!--START_API_CONTRACT-->
```yaml
openapi: 3.0.3
info:
  title: Membership Hub API Gateway
  version: 1.0.0
  description: |
    Centralized API gateway contract cho hệ thống membership-hub.
    Tham chiếu các Tag ID: [ARC-006] Authentication, [ARC-007] Attendance QR,
    [ARC-008] Notification, [ARC-009] Mobile App Integration.
servers:
  - url: https://api.membershiphub.example.com
    description: Production GKE gateway
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
    oauth2:
      type: oauth2
      flows:
        authorizationCode:
          authorizationUrl: https://auth.membershiphub.example.com/oauth2/authorize
          tokenUrl: https://auth.membershiphub.example.com/oauth2/token
          scopes:
            openid: OpenID
            profile: Profile
            email: Email
security:
  - bearerAuth: []
paths:
  /api/v1/auth/register:
    post:
      summary: Register a new user (stub reference for Phase 2)
      security: []
  /api/v1/auth/login:
    post:
      summary: Email/password login (stub reference for Phase 2)
      security: []
  /api/v1/centers:
    get:
      summary: List centers (stub reference for Phase 2)
  /api/v1/courses:
    get:
      summary: List courses (stub reference for Phase 3)
  /api/v1/attendance/scan:
    post:
      summary: QR scan attendance (stub reference for Phase 4)
  /api/v1/reports/attendance:
    get:
      summary: Attendance CSV report (stub reference for Phase 4)
```
<!--END_API_CONTRACT-->

#### 📝 Nhiệm vụ phụ 3.5: Định nghĩa Kafka Topic Schemas
##### Sub-Agent được phân công: Coder
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/kafka/TopicSchemas.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-008]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo class Java tại đường dẫn `./sources/backend/notification-service/src/main/java/org/nlh4j/membershiphub/notificationservice/kafka/TopicSchemas.java` thuộc package `org.nlh4j.membershiphub.notificationservice.kafka`. Class chứa ba inner record class đại diện cho JSON schema của ba Kafka topic chính: (1) `EnrollmentCreated` với các trường `enrollmentId`, `studentId`, `courseId`, `timestamp` định dạng ISO-8601; (2) `TeacherAssigned` với các trường `courseId`, `teacherId`, `assignedBy`, `assignedAt`; (3) `AttendanceRecorded` với các trường `attendanceId`, `studentId`, `courseId`, `attendanceDate` định dạng YYYY-MM-DD. Mỗi record sử dụng annotation Jackson `@JsonProperty` cho key và value, kèm builder method thông qua `@JsonCreator`. Class sử dụng `quarkus-messaging-kafka` để produce event với cấu hình `acks=all`, `compression.type=snappy`, `batch.size=65536`, `linger.ms=20`. Đồng thời tạo tệp YAML tại `./sources/backend/notification-service/src/main/resources/kafka-topics.yaml` khai báo cấu hình topic gồm `enrollment.created` (partitions: 6, replication: 3), `teacher.assigned` (partitions: 3, replication: 3), `attendance.recorded` (partitions: 6, replication: 3), kèm `retention.ms` và `cleanup.policy`.

#### 📝 Nhiệm vụ phụ 3.6: Kiểm thử bảo mật và gateway
##### Sub-Agent được phân công: Tester
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** INTEGRATION_SCOPE;./sources/infra/test/security-gateway-integration.sh
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006], [ARC-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo script shell tại đường dẫn `./sources/infra/test/security-gateway-integration.sh` thực thi bộ kiểm thử tích hợp cho lớp bảo mật gateway. Script sử dụng `curl` kết hợp `jq` để thực hiện năm kịch bản kiểm thử: (1) Khởi động sáu service Quarkus thông qua `mvn quarkus:dev` trong background mode, đợi 60 giây cho service sẵn sàng; (2) Gửi GET request không có header `Authorization` đến `/api/v1/centers`, kỳ vọng HTTP status 401; (3) Gửi GET request với JWT hợp lệ (chuỗi bearer token giả lập đã qua kiểm tra chữ ký), kỳ vọng HTTP 200; (4) Gửi request với JWT hết hạn (sử dụng token có claim `exp` trong quá khứ), kỳ vọng HTTP 401 với thông điệp "Token expired"; (5) Gửi preflight OPTIONS request với header `Origin: https://app.membershiphub.example.com`, kỳ vọng HTTP 200 và response chứa header `Access-Control-Allow-Origin`. Mỗi kịch bản phải có assertion rõ ràng, script trả exit code 0 khi tất cả pass, exit code 1 kèm thông điệp lỗi chi tiết khi thất bại.

#### 📝 Nhiệm vụ phụ 3.7: Đánh giá lớp bảo mật và OpenAPI
##### Sub-Agent được phân công: Reviewer
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/notification-service/src/main/resources/kafka-topics.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006], [ARC-007], [ARC-008], [ARC-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Review năm tệp bảo mật và gateway gồm `JwtFilter.java`, `OAuth2ResourceServer.java`, `CorsFilter.java`, `openapi.yaml` và `TopicSchemas.java`. Kiểm tra năm tiêu chí chất lượng: (1) Không có secret, key hay thông tin nhạy cảm nào được hard-code trong mã nguồn, tất cả phải được inject từ biến môi trường hoặc Secret Manager; (2) RS256 được sử dụng cho chữ ký JWT (cấm HS256 đối với hệ thống production); (3) OpenAPI spec tại `openapi.yaml` hợp lệ và validate thành công thông qua `swagger-cli validate`; (4) Kafka topic cấu hình `replication.factor >= 3` để đảm bảo high availability, `acks=all` cho producer để chống mất dữ liệu; (5) CORS policy không cho phép wildcard origin `*` mà chỉ định danh sách domain cụ thể. Tạo báo cáo review tại `./sources/docs/review/phase1-day3-security-review.md` liệt kê chi tiết từng issue phát hiện, đề xuất fix cụ thể và xác nhận 100% Tag ID [ARC-006] đến [ARC-009] đã được xác minh.

#### 📝 Nhiệm vụ phụ 3.8: Biên soạn tài liệu API Contracts
##### Sub-Agent được phân công: Doc
##### Thành phần mục tiêu & yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/docs/api/OpenAPIContracts.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-006], [ARC-007], [ARC-008], [ARC-009], [DOC-001]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu Markdown tại đường dẫn `./sources/docs/api/OpenAPIContracts.md` mô tả chi tiết hợp đồng API Gateway. Nội dung bắt buộc gồm: (1) Mục lục rõ ràng; (2) Bảng danh sách endpoint theo Tag ID, bao gồm method, path, mô tả chức năng, security scheme yêu cầu; (3) Phần mô tả security scheme gồm `bearerAuth` (HTTP Bearer với JWT) và `oauth2` (authorization code flow); (4) Giải thích chi tiết CORS policy, danh sách allowed origins, allowed methods, allowed headers; (5) Danh sách Kafka topic với cấu hình partitions, replication factor, key, value schema; (6) Sơ đồ Mermaid sequence cho bốn luồng chính: Authentication Flow, Attendance QR Flow, Notification Delivery Flow, Mobile App Integration Flow. Tài liệu phải viết bằng tiếng Việt, có bảng Tag ID mapping chính xác và tham chiếu 1:1 với tệp `openapi.yaml` đã tạo ở nhiệm vụ trước.

<!--END_CHUNK_PHASE_1-->