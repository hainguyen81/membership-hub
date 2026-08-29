# Giai đoạn 1: <!--PHASE_NAME_START-->Khởi Tạo Nền Tảng Đa Vi Dịch Vụ Và Di Trú Cơ Sở Dữ Liệu<!--PHASE_NAME_END-->

## 📊 Kiểm Soát Tài Liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Bản Thiết Kế** | ARCH-20260829125322 |
| **Tên Dự Án** | membership-hub |
| **Giai Đoạn** | 1 |
| **Tên Giai Đoạn** | <!--PHASE_NAME_START-->Khởi Tạo Nền Tảng Đa Vi Dịch Vụ Và Di Trú Cơ Sở Dữ Liệu<!--PHASE_NAME_END--> |
| **Mô Tả** | <!--PHASE_DESC_START-->Giai đoạn này thiết lập toàn bộ khung xương kiến trúc cho hệ thống Membership Hub, bao gồm khởi tạo descriptor Maven đa mô-đun cho sáu vi dịch vụ Quarkus, biểu mẫu gốc Next.js, di trú toàn bộ mười một bảng cơ sở dữ liệu quan hệ thông qua tập lệnh Flyway, đặc tả hợp đồng OpenAPI và sơ đồ Mermaid cho bốn luồng nghiệp vụ cốt lõi. Tất cả tài sản được theo dõi bằng hệ thống thẻ TagID chuẩn doanh nghiệp, đảm bảo tính nhất quán cho các giai đoạn phát triển tiếp theo. Phạm vi tập trung tuyệt đối vào hạ tầng nền tảng, không triển khai xử lý ngoại lệ nghiệp vụ hay luồng nghiệp vụ chi tiết.<!--PHASE_DESC_END--> |
| **Phiên Bản** | 1.0 (Đường Cơ Sở) |
| **Thời Gian** | 2026/08/29 12:53:22 |
| **Tác Giả** | Kiến Trúc Sư Hệ Thống Doanh Nghiệp (SA Agent) |
| **Phê Duyệt** | Đang Chờ Đánh Giá Quản Trị Kỹ Thuật |

## 1. Phạm Vi Hoạt Động & Mục Tiêu Giai Đoạn

Giai đoạn 1 đóng vai trò nền tảng khởi tạo cho toàn bộ dự án Membership Hub, thực hiện ba nhiệm vụ cốt lõi được phân bổ theo bảng tóm tắt đa giai đoạn: Nhiệm vụ 1 (khởi tạo dự án đa dịch vụ và biểu mẫu xây dựng gốc), Nhiệm vụ 16 (khởi tạo hạ tầng cơ sở dữ liệu và di trú schema), và Nhiệm vụ 18 (hợp đồng tích hợp hệ thống và API Gateway). Phạm vi kéo dài từ Ngày 1 đến Ngày 4 với tổng cộng hai mươi chín nhiệm vụ phụ được phân bổ cho bốn tác nhân chuyên biệt: Coder chịu trách nhiệm sinh descriptor Maven cho từng vi dịch vụ và tập lệnh di trú Flyway DDL; Tester xây dựng kịch bản kiểm thử tích hợp xác minh tính hợp lệ của schema và hợp đồng OpenAPI; Reviewer thực hiện đánh giá mã tĩnh và kiểm định cấu trúc descriptor; Doc soạn thảo tài liệu blueprint kiến trúc, sơ đồ quan hệ thực thể, và đặc tả Mermaid cho bốn luồng nghiệp vụ cốt lõi.

Các tài sản kỹ thuật bắt buộc phải sinh ra bao gồm: descriptor gốc `./sources/backend/pom.xml` khai báo Quarkus BOM 3.15.1 và Java 21, sáu descriptor vi dịch vụ (`user-service`, `center-service`, `course-service`, `attendance-service`, `notification-service`, `reporting-service`), biểu mẫu Next.js (`./sources/frontend/web-app/package.json` và `tsconfig.json`), mười một tập lệnh di trú SQL cho mười một bảng dữ liệu (Roles, Users, Centers, Courses, Enrollments, Attendance, StudentCards, Notifications, Promotions, Announcements, SystemSettings), tệp OpenAPI 3.0.3 đặc tả hợp đồng cho bốn luồng nghiệp vụ (Xác thực, Điểm danh, Thông báo, Tích hợp di động), tài liệu blueprint kiến trúc tại `./sources/docs/architecture/blueprint.md`, và kịch bản bash kiểm thử tích hợp Maven build. Toàn bộ tài sản phải được gắn thẻ truy xuất theo hệ thống TagID `[ARC-000]`, `[ARC-006]` đến `[ARC-009]`, `[DAT-001]` đến `[DAT-011]` để đảm bảo khả năng truy vết đầy đủ trong các giai đoạn tiếp theo.

## 2. Phạm Vi Kỹ Thuật Cho Phép & Ranh Giới Thư Mục

Danh sách tệp vật lý và điểm cuối được phép sinh ra trong giai đoạn này:

* `./sources/backend/pom.xml` [ARC-000]
* `./sources/backend/user-service/pom.xml` [ARC-000]
* `./sources/backend/center-service/pom.xml` [ARC-000]
* `./sources/backend/course-service/pom.xml` [ARC-000]
* `./sources/backend/attendance-service/pom.xml` [ARC-000]
* `./sources/backend/notification-service/pom.xml` [ARC-000]
* `./sources/backend/reporting-service/pom.xml` [ARC-000]
* `./sources/backend/user-service/src/main/resources/db/migration/V1__init_roles_and_users.sql` [DAT-001], [DAT-002]
* `./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql` [DAT-007]
* `./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql` [DAT-003]
* `./sources/backend/center-service/src/main/resources/db/migration/V2__init_promotions_announcements.sql` [DAT-009], [DAT-010]
* `./sources/backend/center-service/src/main/resources/db/migration/V3__init_system_settings.sql` [DAT-011]
* `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql` [DAT-004]
* `./sources/backend/course-service/src/main/resources/db/migration/V2__init_enrollments.sql` [DAT-005]
* `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql` [DAT-006]
* `./sources/backend/notification-service/src/main/resources/db/migration/V1__init_notifications.sql` [DAT-008]
* `./sources/frontend/web-app/package.json` [ARC-000]
* `./sources/frontend/web-app/tsconfig.json` [ARC-000]
* `./sources/docs/architecture/blueprint.md` [ARC-006], [ARC-007], [ARC-008], [ARC-009]
* `./sources/docs/api/openapi-spec.yaml` [ARC-006], [ARC-007], [ARC-008], [ARC-009]
* `./sources/infra/test/maven-build-integration.sh` [ARC-000]
* `./sources/infra/test/openapi-spec-validation.sh` [ARC-006], [ARC-007], [ARC-008], [ARC-009]
* `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserSchemaMigrationIT.java` [DAT-001], [DAT-002]
* `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceSchemaMigrationIT.java` [DAT-004], [DAT-005], [DAT-006], [DAT-007]
* `./sources/backend/notification-service/src/test/java/org/nlh4j/membershiphub/notificationservice/NotificationSchemaMigrationIT.java` [DAT-008], [DAT-009], [DAT-010], [DAT-011]

* **RÀNG BUỘC BẮT BUỘC VỀ BIỂU MẪU NỀN TẢNG**:
  - Khi khởi tạo blueprint vòng đời hoạt động (giới hạn trong Giai đoạn 1 - NGÀY 1), phải chèn và khai báo descriptor xây dựng hạ tầng kho lưu trữ chính trước khi phát sinh bất kỳ thành phần mã nguồn ứng dụng nào.
  - Đối với kiến trúc backend Microservices, phải thực thi định nghĩa đường dẫn bắt buộc của descriptor dự án cha `./sources/backend/pom.xml` và các biểu mẫu mô-đun con `./sources/backend/<service-name>/pom.xml`.
  - Đối với lớp giao diện Frontend, phải thực thi đăng ký đường dẫn cấu hình rõ ràng `./sources/frontend/package.json` và `./sources/frontend/tsconfig.json`. Tất cả tài sản khung được sinh ra phải ánh xạ chính xác tới mã kiến trúc hệ thống `[ARC-000]`.

## 3. Chỉ Thị Chức Năng Chuyên Biệt Cho Tác Nhân Phụ

*   **Coder**: Đóng vai trò Nhà Phát Triển Ứng Dụng Cao Cấp. Chịu trách nhiệm triển khai mã nguồn ứng dụng thuần túy trên cả dịch vụ backend và ứng dụng frontend/mobile. Bị cấm viết bộ kiểm thử hoặc biểu mẫu hạ tầng.

* **Tester**: Đóng vai trò Trưởng Nhóm Kiểm Thử/Đảm Bảo Chất Lượng. Chuyên về kỹ thuật bộ kiểm thử, xác thực và cổng chất lượng. Chịu trách nhiệm sinh JUnit, kiểm thử tích hợp, kiểm thử tự động đầu-cuối và kịch bản xác thực hiệu năng. Bị cấm sửa đổi mã nguồn sản phẩm. Nếu nhiệm vụ phụ liên quan đến phạm vi tích hợp tổng thể hoặc đầu-cuối mà không thể khoanh vùng một tệp mã nguồn cụ thể, phải xuất chính xác chuỗi ký tự `INTEGRATION_SCOPE` làm tham số đầu tiên của cặp phân tách bằng dấu chấm phẩy (ví dụ: `INTEGRATION_SCOPE;./sources/backend/tests/integration/WorkflowTest.java`).

* **Doc**: Đóng vai trò Chuyên Viên Viết Tài Liệu Kỹ Thuật và Kiến Trúc Sư Hệ Thống Doanh Nghiệp. Chuyên về biên soạn tài liệu đặc tả kỹ thuật toàn diện, tài liệu tham chiếu schema, blueprint hệ thống và danh mục kiến trúc doanh nghiệp phù hợp với các lớp topology dự án đang hoạt động. Mỗi tệp tài liệu kỹ thuật được sinh ra phải được liệt kê dưới dạng thực thể đường dẫn tệp rõ ràng kết thúc bằng phần mở rộng `.md` và nằm hoàn toàn trong sơ đồ lưu trữ tập trung: `./sources/docs/`.

*   **Reviewer**: Chịu trách nhiệm xác minh trình biên dịch, cổng phân tích tĩnh và vá lỗi phòng thủ. Chuyên về đánh giá chất lượng mã, giải quyết lỗi biên dịch, sửa lỗ hổng bảo mật OWASP và xử lý các blocker cổng chất lượng SonarQube.

*   **Docker**: Chuyên biệt về container hóa, kỹ thuật Dockerfile đa giai đoạn, tối ưu gói và đẩy tài sản ảnh ứng dụng đã xác minh lên DockerHub.

*   **GCP**: Chuyên về tự động hóa đám mây trong Google Cloud Platform. Chịu trách nhiệm xây dựng và đẩy ảnh lên Google Cloud Artifact Registry (GCR), điều phối môi trường container nguyên bản trên Google Cloud Run.

*   **GKE**: Chuyên về điều phối container sản xuất bên trong Google Kubernetes Engine. Chịu trách nhiệm xây dựng manifest triển khai Kubernetes, điều khiển định tuyến, cấu hình HPA, biểu đồ Helm và triển khai tải công việc microservice vào cụm GKE đang hoạt động.

## 4. Định Nghĩa Hoàn Thành Giai Đoạn

Giai đoạn 1 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng sau: một trăm phần trăm mười một bảng cơ sở dữ liệu đã được sinh ra với ràng buộc khóa chính, khóa ngoại, kiểm tra CHECK và chỉ mục tối ưu thông qua tập lệnh Flyway versioned; toàn bộ bảy descriptor Maven (một cha và sáu vi dịch vụ con) biên dịch sạch với Quarkus BOM 3.15.1 và Java 21 LTS; biểu mẫu gốc Next.js 14.2.15 hoàn chỉnh với mọi phụ thuộc được khai báo đúng phiên bản; hợp đồng OpenAPI 3.0.3 cho bốn luồng nghiệp vụ cốt lõi (Xác thực, Điểm danh, Thông báo, Tích hợp di động) được xác minh tính hợp lệ qua công cụ redocly/cli; kịch bản kiểm thử tích hợp Maven build xác nhận tất cả sáu vi dịch vụ tải và phân giải đúng dependency; tài liệu blueprint kiến trúc tại `./sources/docs/architecture/blueprint.md` chứa sơ đồ quan hệ thực thể đầy đủ cho mười một bảng và bốn sơ đồ Mermaid sequenceDiagram; một trăm phần trăm mã TagID `[ARC-000]`, `[ARC-006]`, `[ARC-007]`, `[ARC-008]`, `[ARC-009]`, `[DAT-001]` đến `[DAT-011]` được ánh xạ chính xác trong báo cáo đánh giá cuối giai đoạn. Mọi vi phạm chuẩn OWASP Top 10 (SQL injection, XSS, CSRF) phải được phát hiện và khắc phục trong quá trình review.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->KHỞI TẠO DESCRIPTOR DỰ ÁN ĐA MÔ-ĐUN VÀ BIỂU MẪU XÂY DỰNG GỐC<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 1.1: Sinh descriptor pom.xml gốc và cấu hình đa vi dịch vụ
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/pom.xml

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/pom.xml` với packaging `pom` làm descriptor cha, khai báo `modules` chứa 6 vi dịch vụ (`user-service`, `center-service`, `course-service`, `attendance-service`, `notification-service`, `reporting-service`). Cấu hình `parent` tham chiếu Quarkus BOM phiên bản 3.15.1, thiết lập `properties` cho Java 21, định nghĩa `dependencyManagement` quản lý phiên bản chuẩn: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-smallrye-jwt-build`, `quarkus-smallrye-health`, `quarkus-micrometer-registry-prometheus`, `quarkus-arc`, `quarkus-test-junit5`. Đảm bảo không chứa ký tự gạch ngang `-` hoặc gạch dưới `_` trong bất kỳ định danh kỹ thuật nào, chỉ sử dụng chữ thường alphanumeric.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.nlh4j</groupId>
    <artifactId>membershiphub</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>Membership Hub Root</name>

    <parent>
        <groupId>io.quarkus.platform</groupId>
        <artifactId>quarkus-bom</artifactId>
        <version>3.15.1</version>
        <relativePath/>
    </parent>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <quarkus.platform.version>3.15.1</quarkus.platform.version>
        <surefire-plugin.version>3.2.5</surefire-plugin.version>
    </properties>

    <modules>
        <module>user-service</module>
        <module>center-service</module>
        <module>course-service</module>
        <module>attendance-service</module>
        <module>notification-service</module>
        <module>reporting-service</module>
    </modules>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-bom</artifactId>
                <version>${quarkus.platform.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>${surefire-plugin.version}</version>
                    <configuration>
                        <systemPropertyVariables>
                            <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                            <maven.home>${maven.home}</maven.home>
                        </systemPropertyVariables>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

#### 📝 NHIỆM VỤ PHỤ 1.2: Sinh descriptor pom.xml cho user-service
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/pom.xml

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/user-service/pom.xml` kế thừa từ descriptor cha, định nghĩa `artifactId` là `user-service`. Khai báo các dependency thiết yếu: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-smallrye-jwt-build`, `quarkus-smallrye-health`. Cấu hình plugin `quarkus-maven-plugin` với phiên bản tương thích BOM. Đảm bảo `groupId` là `org.nlh4j.membershiphub.userservice`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.nlh4j</groupId>
        <artifactId>membershiphub</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>user-service</artifactId>
    <groupId>org.nlh4j.membershiphub.userservice</groupId>
    <name>User Service</name>

    <dependencies>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-jackson</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm-panache</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-flyway</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-jwt</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-jwt-build</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-health</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus.platform.version}</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                            <goal>generate-code</goal>
                            <goal>generate-code-tests</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 📝 NHIỆM VỤ PHỤ 1.3: Sinh descriptor pom.xml cho center-service
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/center-service/pom.xml

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/center-service/pom.xml` với `groupId` `org.nlh4j.membershiphub.centerservice`, `artifactId` `center-service`, kế thừa từ descriptor cha. Bao gồm các dependency: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-smallrye-health`. Cấu hình plugin `quarkus-maven-plugin` chuẩn với execution `build`, `generate-code`, `generate-code-tests`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.nlh4j</groupId>
        <artifactId>membershiphub</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>center-service</artifactId>
    <groupId>org.nlh4j.membershiphub.centerservice</groupId>
    <name>Center Service</name>

    <dependencies>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-jackson</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm-panache</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-flyway</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-jwt</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-health</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus.platform.version}</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                            <goal>generate-code</goal>
                            <goal>generate-code-tests</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 📝 NHIỆM VỤ PHỤ 1.4: Sinh descriptor pom.xml cho course-service
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/course-service/pom.xml

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/course-service/pom.xml` với `groupId` `org.nlh4j.membershiphub.courseservice`, `artifactId` `course-service`. Khai báo dependency: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-messaging-kafka` (cho sự kiện thông báo), `quarkus-smallrye-health`. Tích hợp plugin `quarkus-maven-plugin` chuẩn.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.nlh4j</groupId>
        <artifactId>membershiphub</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>course-service</artifactId>
    <groupId>org.nlh4j.membershiphub.courseservice</groupId>
    <name>Course Service</name>

    <dependencies>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-jackson</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm-panache</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-flyway</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-jwt</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-messaging-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-health</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus.platform.version}</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                            <goal>generate-code</goal>
                            <goal>generate-code-tests</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 📝 NHIỆM VỤ PHỤ 1.5: Sinh descriptor pom.xml cho attendance-service
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/pom.xml

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/attendance-service/pom.xml` với `groupId` `org.nlh4j.membershiphub.attendanceservice`, `artifactId` `attendance-service`. Bao gồm dependency: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-smallrye-health`. Cấu hình plugin `quarkus-maven-plugin` chuẩn cho build và code generation.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.nlh4j</groupId>
        <artifactId>membershiphub</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>attendance-service</artifactId>
    <groupId>org.nlh4j.membershiphub.attendanceservice</groupId>
    <name>Attendance Service</name>

    <dependencies>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-jackson</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm-panache</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-flyway</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-jwt</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-health</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus.platform.version}</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                            <goal>generate-code</goal>
                            <goal>generate-code-tests</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 📝 NHIỆM VỤ PHỤ 1.6: Sinh descriptor pom.xml cho notification-service
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/notification-service/pom.xml

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/notification-service/pom.xml` với `groupId` `org.nlh4j.membershiphub.notificationservice`, `artifactId` `notification-service`. Khai báo dependency: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-messaging-kafka` (cho sự kiện), `quarkus-rest-client` (cho FCM/APNs/Zalo API), `quarkus-smallrye-health`. Tích hợp `quarkus-maven-plugin` chuẩn.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.nlh4j</groupId>
        <artifactId>membershiphub</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>notification-service</artifactId>
    <groupId>org.nlh4j.membershiphub.notificationservice</groupId>
    <name>Notification Service</name>

    <dependencies>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-jackson</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm-panache</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-flyway</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-jwt</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-messaging-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-client</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-health</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus.platform.version}</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                            <goal>generate-code</goal>
                            <goal>generate-code-tests</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 📝 NHIỆM VỤ PHỤ 1.7: Sinh descriptor pom.xml cho reporting-service
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/reporting-service/pom.xml

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/reporting-service/pom.xml` với `groupId` `org.nlh4j.membershiphub.reportingservice`, `artifactId` `reporting-service`. Bao gồm dependency: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-flyway`, `quarkus-smallrye-jwt`, `quarkus-scheduler` (cho refresh dashboard), `quarkus-smallrye-health`. Cấu hình plugin `quarkus-maven-plugin` chuẩn.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.nlh4j</groupId>
        <artifactId>membershiphub</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>reporting-service</artifactId>
    <groupId>org.nlh4j.membershiphub.reportingservice</groupId>
    <name>Reporting Service</name>

    <dependencies>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-jackson</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm-panache</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-flyway</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-jwt</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-scheduler</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-health</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus.platform.version}</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                            <goal>generate-code</goal>
                            <goal>generate-code-tests</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 📝 NHIỆM VỤ PHỤ 1.8: Sinh biểu mẫu gốc Next.js
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/frontend/web-app/package.json

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/frontend/web-app/package.json` cho ứng dụng web Next.js, khai báo `name` là `membershiphub-webapp`, phiên bản `1.0.0`. Liệt kê `scripts`: `dev` (next dev), `build` (next build), `start` (next start), `lint` (next lint), `test` (jest). Khai báo `dependencies`: `next@14.2.15`, `react@18.3.1`, `react-dom@18.3.1`, `axios@1.7.7`, `next-i18next@15.3.1`, `firebase@10.14.1`, `firebase-admin@12.6.0`, `@react-oauth/google@0.12.1`, `react-facebook-login@4.1.1`, `qrcode-reader@1.0.4`, `react-qr-scanner@1.0.0-alpha.11`, `recharts@2.13.0`. Khai báo `devDependencies`: `typescript@5.6.3`, `@types/react@18.3.11`, `@types/node@22.7.5`, `jest@29.7.0`, `jest-environment-jsdom@29.7.0`, `@testing-library/react@16.0.1`, `eslint@8.57.1`, `eslint-config-next@14.2.15`.

```json
{
  "name": "membershiphub-webapp",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "test": "jest"
  },
  "dependencies": {
    "next": "14.2.15",
    "react": "18.3.1",
    "react-dom": "18.3.1",
    "axios": "1.7.7",
    "next-i18next": "15.3.1",
    "firebase": "10.14.1",
    "firebase-admin": "12.6.0",
    "@react-oauth/google": "0.12.1",
    "react-facebook-login": "4.1.1",
    "qrcode-reader": "1.0.4",
    "react-qr-scanner": "1.0.0-alpha.11",
    "recharts": "2.13.0"
  },
  "devDependencies": {
    "typescript": "5.6.3",
    "@types/react": "18.3.11",
    "@types/node": "22.7.5",
    "jest": "29.7.0",
    "jest-environment-jsdom": "29.7.0",
    "@testing-library/react": "16.0.1",
    "eslint": "8.57.1",
    "eslint-config-next": "14.2.15"
  }
}
```

#### 📝 NHIỆM VỤ PHỤ 1.9: Sinh cấu hình tsconfig.json
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/frontend/web-app/tsconfig.json

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/frontend/web-app/tsconfig.json` cấu hình biên dịch TypeScript cho Next.js 14. Thiết lập `target` ES2022, `module` ESNext, `moduleResolution` Bundler, `lib` DOM/ES2022. Kích hoạt `strict`, `noEmit`, `esModuleInterop`, `skipLibCheck`, `forceConsistentCasingInFileNames`, `resolveJsonModule`, `isolatedModules`, `jsx` preserve. Bao gồm `baseUrl` là `.` và `paths` ánh xạ `@/*` tới `./src/*`.

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["dom", "dom.iterable", "esnext"],
    "allowJs": true,
    "skipLibCheck": true,
    "strict": true,
    "noEmit": true,
    "esModuleInterop": true,
    "module": "esnext",
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "jsx": "preserve",
    "incremental": true,
    "forceConsistentCasingInFileNames": true,
    "plugins": [
      {
        "name": "next"
      }
    ],
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["next-env.d.ts", "**/*.ts", "**/*.tsx", ".next/types/**/*.ts"],
  "exclude": ["node_modules"]
}
```

#### 📝 NHIỆM VỤ PHỤ 1.10: Kiểm thử tích hợp xây dựng đa mô-đun
##### Tác Nhân Được Phân Công: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/infra/test/maven-build-integration.sh

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/infra/test/maven-build-integration.sh` chứa kịch bản bash kiểm thử tích hợp. Kịch bản phải thực hiện `mvn clean validate` tại thư mục `./sources/backend/` để xác nhận tất cả 6 descriptor `pom.xml` vi dịch vụ tải và phân giải đúng các dependency từ BOM Quarkus 3.15.1. Thoát với mã 0 nếu thành công, mã khác 0 nếu thất bại. In log rõ ràng cho mỗi vi dịch vụ.

```bash
#!/usr/bin/env bash
set -euo pipefail

BACKEND_ROOT="./sources/backend"
SERVICES=("user-service" "center-service" "course-service" "attendance-service" "notification-service" "reporting-service")

echo "============================================================"
echo "  TÍCH HỢP XÂY DỰNG ĐA MÔ-ĐUN - MEMBERSHIP HUB"
echo "============================================================"

if [ ! -d "${BACKEND_ROOT}" ]; then
    echo "[LỖI] Không tìm thấy thư mục backend gốc: ${BACKEND_ROOT}"
    exit 1
fi

cd "${BACKEND_ROOT}"
echo "[INFO] Đang chạy 'mvn clean validate' tại ${BACKEND_ROOT}"
mvn clean validate -B -q

for SERVICE in "${SERVICES[@]}"; do
    if [ -f "${SERVICE}/pom.xml" ]; then
        echo "[OK] Descriptor pom.xml tồn tại cho vi dịch vụ: ${SERVICE}"
    else
        echo "[LỖI] Thiếu descriptor pom.xml cho vi dịch vụ: ${SERVICE}"
        exit 2
    fi
done

echo "============================================================"
echo "  TẤT CẢ DESCRIPTOR ĐÃ ĐƯỢC XÁC MINH THÀNH CÔNG"
echo "============================================================"
exit 0
```

#### 📝 NHIỆM VỤ PHỤ 1.11: Đánh giá mã và kiểm định cấu trúc descriptor
##### Tác Nhân Được Phân Công: Reviewer
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/pom.xml

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Thực hiện đánh giá mã tĩnh (code review) cho descriptor `./sources/backend/pom.xml` và toàn bộ 6 descriptor vi dịch vụ con. Xác minh: (1) tất cả `groupId` phải tuân thủ quy ước `org.nlh4j.membershiphub.<servicename>` không chứa ký tự `-` hoặc `_`; (2) tất cả `artifactId` đều ở dạng chữ thường alphanumeric; (3) mọi tham chiếu `<parent>` đều trỏ về `membershiphub` gốc phiên bản `1.0.0-SNAPSHOT`; (4) phiên bản Quarkus BOM `3.15.1` được nhập đúng trong `dependencyManagement`; (5) plugin `quarkus-maven-plugin` được khai báo trong từng vi dịch vụ. Sinh báo cáo đánh giá với điểm số tuân thủ và đề xuất sửa lỗi nếu phát hiện bất thường.

#### 📝 NHIỆM VỤ PHỤ 1.12: Soạn thảo tài liệu biên bản khởi tạo dự án
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/architecture/blueprint.md

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Soạn thảo biên bản khởi tạo dự án tại `./sources/docs/architecture/blueprint.md` mô tả tổng quan cấu trúc monorepo, sơ đồ quan hệ cha-con giữa `membershiphub` gốc và 6 vi dịch vụ, bản đồ thư mục vật lý (theo chuẩn Unix), quy ước đặt tên package Java (`org.nlh4j.membershiphub.<servicename>`), quy ước cấu hình Maven (Java 21, Quarkus BOM 3.15.1), cùng danh sách plugin tích hợp bắt buộc. Tài liệu phải ở định dạng Markdown tiêu chuẩn với các tiêu đề phân cấp rõ ràng.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->DI TRÚ SCHEMA CƠ SỞ DỮ LIỆU BẰNG FLYWAY DDL - PHẦN 1 (USERS, ROLES, CENTERS)<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 2.1: Tạo tập lệnh di trú V1 - bảng Roles và Users
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/resources/db/migration/V1__init_roles_and_users.sql

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-001], [DAT-002]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/user-service/src/main/resources/db/migration/V1__init_roles_and_users.sql` chứa DDL SQL chuẩn ANSI. Bảng `roles` (`role_id` SMALLINT PRIMARY KEY, `name` VARCHAR(30) UNIQUE NOT NULL, `description` VARCHAR(200)). Bảng `users` (`user_id` UUID PRIMARY KEY DEFAULT gen_random_uuid(), `email` VARCHAR(255) UNIQUE NOT NULL, `password_hash` CHAR(60) NOT NULL, `full_name` VARCHAR(100) NOT NULL, `role_id` SMALLINT NOT NULL, `provider` VARCHAR(20) NOT NULL DEFAULT 'local' với CHECK (provider IN ('local','firebase','google','facebook')), `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (role_id) REFERENCES roles(role_id)). Tạo chỉ mục `idx_users_email`, `idx_users_role_id`. Chèn dữ liệu khởi tạo 5 vai trò RBAC.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- =====================================================================
-- V1__init_roles_and_users.sql
-- Khởi tạo bảng Roles và Users cho Membership Hub
-- =====================================================================

CREATE TABLE roles (
    role_id SMALLINT PRIMARY KEY,
    name VARCHAR(30) UNIQUE NOT NULL,
    description VARCHAR(200)
);

CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash CHAR(60) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role_id SMALLINT NOT NULL,
    provider VARCHAR(20) NOT NULL DEFAULT 'local',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id),
    CONSTRAINT chk_users_provider CHECK (provider IN ('local','firebase','google','facebook'))
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_id ON users(role_id);

INSERT INTO roles (role_id, name, description) VALUES
    (1, 'SystemAdmin', 'Quản trị viên hệ thống toàn cục'),
    (2, 'CenterAdmin', 'Quản trị viên cấp trung tâm'),
    (3, 'Manager', 'Quản lý cấp dưới'),
    (4, 'Teacher', 'Giáo viên chỉ xem lịch giảng dạy'),
    (5, 'Student', 'Học viên đăng ký khóa học');
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 2.2: Tạo tập lệnh di trú V1 - bảng Centers
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-003]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/center-service/src/main/resources/db/migration/V1__init_centers.sql` định nghĩa bảng `centers` (`center_id` UUID PRIMARY KEY DEFAULT gen_random_uuid(), `name` VARCHAR(100) NOT NULL, `address` VARCHAR(255) NOT NULL, `tax_id` VARCHAR(20) UNIQUE NOT NULL với CHECK (tax_id ~ '^[0-9]{10,13}$'), `contact_phone` VARCHAR(20), `contact_email` VARCHAR(100), `admin_user_id` UUID, `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (admin_user_id) REFERENCES users(user_id)). Tạo chỉ mục `idx_centers_tax_id`, `idx_centers_admin_user_id`.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- =====================================================================
-- V1__init_centers.sql
-- Khởi tạo bảng Centers cho Membership Hub
-- =====================================================================

CREATE TABLE centers (
    center_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tax_id VARCHAR(20) UNIQUE NOT NULL,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    admin_user_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_centers_admin FOREIGN KEY (admin_user_id) REFERENCES users(user_id),
    CONSTRAINT chk_centers_taxid CHECK (tax_id ~ '^[0-9]{10,13}$')
);

CREATE INDEX idx_centers_tax_id ON centers(tax_id);
CREATE INDEX idx_centers_admin_user_id ON centers(admin_user_id);
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 2.3: Kiểm thử tích hợp di trú V1 user và center
##### Tác Nhân Được Phân Công: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserSchemaMigrationIT.java

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-001], [DAT-002], [DAT-003]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo lớp kiểm thử tích hợp Flyway `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserSchemaMigrationIT.java` sử dụng `@QuarkusTest`. Cấu hình Testcontainers PostgreSQL 16, thực thi `@QuarkusTestResource` để khởi tạo container. Inject `Flyway` bean, gọi `flyway.migrate()` và xác minh các bảng `roles`, `users` tồn tại thông qua truy vấn JDBC metadata. Bổ sung kiểm tra ràng buộc CHECK `chk_users_provider` bằng cách chèn giá trị không hợp lệ và kỳ vọng ngoại lệ `SQLException`.

```java
package org.nlh4j.membershiphub.userservice;

import io.quarkus.test.junit.QuarkusTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@QuarkusTest
public class UserSchemaMigrationIT {

    @Inject
    Flyway flyway;

    @Inject
    DataSource dataSource;

    @Test
    void testRolesAndUsersTablesExist() throws Exception {
        Assertions.assertNotNull(flyway, "Flyway phải được inject");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT table_name FROM information_schema.tables " +
                    "WHERE table_schema='public' AND table_name IN ('roles','users')")) {
                int count = 0;
                while (rs.next()) {
                    count++;
                }
                Assertions.assertEquals(2, count, "Phải tồn tại đúng 2 bảng roles và users");
            }
        }
    }
}
```

#### 📝 NHIỆM VỤ PHỤ 2.4: Đánh giá thiết kế schema Users và Centers
##### Tác Nhân Được Phân Công: Reviewer
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/resources/db/migration/V1__init_roles_and_users.sql

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-001], [DAT-002], [DAT-003]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Đánh giá tệp SQL V1__init_roles_and_users.sql xác minh: (1) tất cả kiểu dữ liệu tuân thủ chuẩn ANSI SQL (không dùng `ENUM`); (2) provider được biểu diễn bằng `VARCHAR(20) NOT NULL` kết hợp `CHECK (provider IN (...))`; (3) ràng buộc FK giữa `users.role_id` và `roles.role_id` đúng; (4) chỉ mục `idx_users_email` và `idx_users_role_id` đủ để hỗ trợ truy vấn tần suất cao; (5) `gen_random_uuid()` được sử dụng đúng cho UUID PRIMARY KEY. Đồng thời đánh giá V1__init_centers.sql xác minh ràng buộc `tax_id` chỉ chấp nhận chuỗi số 10-13 ký tự thông qua biểu thức chính quy. Lập biên bản đánh giá với điểm tuân thủ.

#### 📝 NHIỆM VỤ PHỤ 2.5: Soạn thảo tài liệu mô tả schema
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/architecture/blueprint.md

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-001], [DAT-002], [DAT-003]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Bổ sung section "Sơ đồ quan hệ thực thể" vào tệp `./sources/docs/architecture/blueprint.md` mô tả chi tiết các bảng `roles`, `users`, `centers` bao gồm: từng cột với kiểu dữ liệu, ràng buộc, mô tả nghiệp vụ, các chỉ mục hỗ trợ truy vấn, mối quan hệ FK giữa các bảng. Tài liệu phải có bảng Markdown rõ ràng cho từng bảng. Sử dụng ngôn ngữ tiếng Việt cho phần mô tả, giữ nguyên tên cột tiếng Anh trong schema.

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->DI TRÚ SCHEMA CƠ SỞ DỮ LIỆU BẰNG FLYWAY DDL - PHẦN 2 (COURSES, ENROLLMENTS, ATTENDANCE, STUDENTCARDS)<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 3.1: Tạo tập lệnh di trú V1 - bảng Courses
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-004]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/course-service/src/main/resources/db/migration/V1__init_courses.sql`. Bảng `courses` (`course_id` UUID PRIMARY KEY DEFAULT gen_random_uuid(), `title` VARCHAR(150) NOT NULL, `description` TEXT, `start_date` DATE NOT NULL, `end_date` DATE NOT NULL, `teacher_id` UUID NOT NULL, `max_students` INT NOT NULL DEFAULT 30, `center_id` UUID, `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (teacher_id) REFERENCES users(user_id), FOREIGN KEY (center_id) REFERENCES centers(center_id), CONSTRAINT chk_courses_dates CHECK (end_date >= start_date)). Tạo chỉ mục `idx_courses_teacher_id`, `idx_courses_center_id`, `idx_courses_dates`.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- =====================================================================
-- V1__init_courses.sql
-- Khởi tạo bảng Courses cho Membership Hub
-- =====================================================================

CREATE TABLE courses (
    course_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    teacher_id UUID NOT NULL,
    max_students INT NOT NULL DEFAULT 30,
    center_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id),
    CONSTRAINT fk_courses_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT chk_courses_dates CHECK (end_date >= start_date)
);

CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_courses_center_id ON courses(center_id);
CREATE INDEX idx_courses_dates ON courses(start_date, end_date);
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 3.2: Tạo tập lệnh di trú V2 - bảng Enrollments
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/course-service/src/main/resources/db/migration/V2__init_enrollments.sql

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-005]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/course-service/src/main/resources/db/migration/V2__init_enrollments.sql`. Bảng `enrollments` (`enrollment_id` UUID PRIMARY KEY DEFAULT gen_random_uuid(), `student_id` UUID NOT NULL, `course_id` UUID NOT NULL, `enrollment_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' với CHECK (status IN ('ACTIVE','DROPPED','COMPLETED')), FOREIGN KEY (student_id) REFERENCES users(user_id), FOREIGN KEY (course_id) REFERENCES courses(course_id), UNIQUE (student_id, course_id)). Tạo chỉ mục `idx_enrollments_student_id`, `idx_enrollments_course_id`, `idx_enrollments_status`.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- =====================================================================
-- V2__init_enrollments.sql
-- Khởi tạo bảng Enrollments cho Membership Hub
-- =====================================================================

CREATE TABLE enrollments (
    enrollment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    enrollment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT chk_enrollments_status CHECK (status IN ('ACTIVE','DROPPED','COMPLETED')),
    CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id)
);

CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);
CREATE INDEX idx_enrollments_status ON enrollments(status);
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 3.3: Tạo tập lệnh di trú V1 - bảng Attendance với idempotency key
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-006]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql`. Bảng `attendance` (`attendance_id` UUID PRIMARY KEY DEFAULT gen_random_uuid(), `student_id` UUID NOT NULL, `course_id` UUID NOT NULL, `attendance_date` DATE NOT NULL, `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `status` VARCHAR(20) NOT NULL DEFAULT 'PRESENT' với CHECK (status IN ('PRESENT','ABSENT','LATE')), `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (student_id) REFERENCES users(user_id), FOREIGN KEY (course_id) REFERENCES courses(course_id), UNIQUE (student_id, course_id, attendance_date)). Tạo chỉ mục `idx_attendance_student_id`, `idx_attendance_course_id`, `idx_attendance_date`.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- =====================================================================
-- V1__init_attendance.sql
-- Khởi tạo bảng Attendance với idempotency key
-- =====================================================================

CREATE TABLE attendance (
    attendance_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT fk_attendance_course FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT chk_attendance_status CHECK (status IN ('PRESENT','ABSENT','LATE')),
    CONSTRAINT uq_attendance_idempotency UNIQUE (student_id, course_id, attendance_date)
);

CREATE INDEX idx_attendance_student_id ON attendance(student_id);
CREATE INDEX idx_attendance_course_id ON attendance(course_id);
CREATE INDEX idx_attendance_date ON attendance(attendance_date);
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 3.4: Tạo tập lệnh di trú V2 - bảng StudentCards
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-007]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/user-service/src/main/resources/db/migration/V2__init_student_cards.sql`. Bảng `student_cards` (`card_id` UUID PRIMARY KEY DEFAULT gen_random_uuid(), `student_id` UUID NOT NULL UNIQUE, `issue_date` DATE NOT NULL, `validity_days` INT NOT NULL, `remaining_days` INT NOT NULL, `end_date` DATE NOT NULL, `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' với CHECK (status IN ('ACTIVE','EXPIRED','SUSPENDED')), `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (student_id) REFERENCES users(user_id), CONSTRAINT chk_student_cards_validity CHECK (validity_days > 0 AND remaining_days >= 0)). Tạo chỉ mục `idx_student_cards_student_id`, `idx_student_cards_status`, `idx_student_cards_end_date`.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- =====================================================================
-- V2__init_student_cards.sql
-- Khởi tạo bảng StudentCards cho Membership Hub
-- =====================================================================

CREATE TABLE student_cards (
    card_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL UNIQUE,
    issue_date DATE NOT NULL,
    validity_days INT NOT NULL,
    remaining_days INT NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_cards_student FOREIGN KEY (student_id) REFERENCES users(user_id),
    CONSTRAINT chk_student_cards_validity CHECK (validity_days > 0 AND remaining_days >= 0),
    CONSTRAINT chk_student_cards_status CHECK (status IN ('ACTIVE','EXPIRED','SUSPENDED'))
);

CREATE INDEX idx_student_cards_student_id ON student_cards(student_id);
CREATE INDEX idx_student_cards_status ON student_cards(status);
CREATE INDEX idx_student_cards_end_date ON student_cards(end_date);
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 3.5: Kiểm thử tích hợp di trú V2 course, enrollment, attendance
##### Tác Nhân Được Phân Công: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceSchemaMigrationIT.java

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-004], [DAT-005], [DAT-006], [DAT-007]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo lớp kiểm thử tích hợp Flyway `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceSchemaMigrationIT.java` sử dụng `@QuarkusTest` và Testcontainers PostgreSQL 16. Inject `Flyway` và `DataSource`, thực thi `flyway.migrate()`, xác minh các bảng `courses`, `enrollments`, `attendance`, `student_cards` tồn tại. Đặc biệt kiểm tra ràng buộc `uq_attendance_idempotency` bằng cách chèn 2 bản ghi với cùng `(student_id, course_id, attendance_date)` và kỳ vọng `PSQLException` với mã lỗi ràng buộc duy nhất. Xác minh CHECK `chk_attendance_status` từ chối giá trị ngoài tập cho phép.

```java
package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.test.junit.QuarkusTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@QuarkusTest
public class AttendanceSchemaMigrationIT {

    @Inject
    Flyway flyway;

    @Inject
    DataSource dataSource;

    @Test
    void testSchemaTablesExist() throws Exception {
        Assertions.assertNotNull(flyway);
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            try (var rs = stmt.executeQuery(
                    "SELECT count(*) FROM information_schema.tables " +
                    "WHERE table_schema='public' AND table_name IN " +
                    "('courses','enrollments','attendance','student_cards')")) {
                rs.next();
                Assertions.assertEquals(4, rs.getInt(1));
            }
        }
    }
}
```

#### 📝 NHIỆM VỤ PHỤ 3.6: Đánh giá thiết kế schema Courses và Attendance
##### Tác Nhân Được Phân Công: Reviewer
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-004], [DAT-005], [DAT-006], [DAT-007]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Đánh giá tệp `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_attendance.sql` xác minh: (1) ràng buộc `uq_attendance_idempotency` UNIQUE (student_id, course_id, attendance_date) đảm bảo idempotency đúng theo yêu cầu REQ-013; (2) kiểu `status` dùng VARCHAR(20) kết hợp CHECK thay vì ENUM; (3) các chỉ mục phục vụ truy vấn theo ngày và theo học viên/khóa học. Đồng thời đánh giá schema `courses`, `enrollments`, `student_cards` xác nhận ràng buộc ngày `end_date >= start_date`, UNIQUE `(student_id, course_id)` trong enrollments ngăn đăng ký trùng. Lập báo cáo đánh giá.

#### 📝 NHIỆM VỤ PHỤ 3.7: Soạn thảo tài liệu sơ đồ ERD
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/architecture/blueprint.md

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-004], [DAT-005], [DAT-006], [DAT-007]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Bổ sung section "Sơ đồ quan hệ thực thể - Phần 2" vào `./sources/docs/architecture/blueprint.md` mô tả chi tiết các bảng `courses`, `enrollments`, `attendance`, `student_cards`. Mỗi bảng phải có bảng Markdown với các cột: Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả. Mô tả quan hệ FK giữa các bảng, đặc biệt nhấn mạnh khóa tổng hợp UNIQUE `(student_id, course_id, attendance_date)` đảm bảo idempotency. Tài liệu dùng tiếng Việt cho phần giải thích.

### 🌤️ NGÀY 4: <!--DAY_HEADER_START-->DI TRÚ SCHEMA CƠ SỞ DỮ LIỆU BẰNG FLYWAY DDL - PHẦN 3 (NOTIFICATIONS, PROMOTIONS, ANNOUNCEMENTS, SYSTEMSETTINGS) & HỢP ĐỒNG API<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ PHỤ 4.1: Tạo tập lệnh di trú V1 - bảng Notifications
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/notification-service/src/main/resources/db/migration/V1__init_notifications.sql

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-008]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/notification-service/src/main/resources/db/migration/V1__init_notifications.sql`. Bảng `notifications` (`notification_id` UUID PRIMARY KEY DEFAULT gen_random_uuid(), `user_id` UUID, `group_zalo` VARCHAR(50), `message` TEXT NOT NULL, `channel` VARCHAR(20) NOT NULL với CHECK (channel IN ('PUSH','ZALO','EMAIL','SMS')), `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' với CHECK (status IN ('PENDING','SENT','FAILED','DELIVERED')), `retry_count` INT NOT NULL DEFAULT 0, `sent_at` TIMESTAMP, `delivered` BOOLEAN NOT NULL DEFAULT FALSE, `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (user_id) REFERENCES users(user_id)). Tạo chỉ mục `idx_notifications_user_id`, `idx_notifications_status`, `idx_notifications_channel`, `idx_notifications_sent_at`.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- =====================================================================
-- V1__init_notifications.sql
-- Khởi tạo bảng Notifications cho Membership Hub
-- =====================================================================

CREATE TABLE notifications (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    group_zalo VARCHAR(50),
    message TEXT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    sent_at TIMESTAMP,
    delivered BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_notifications_channel CHECK (channel IN ('PUSH','ZALO','EMAIL','SMS')),
    CONSTRAINT chk_notifications_status CHECK (status IN ('PENDING','SENT','FAILED','DELIVERED'))
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_channel ON notifications(channel);
CREATE INDEX idx_notifications_sent_at ON notifications(sent_at);
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 4.2: Tạo tập lệnh di trú V2 - bảng Promotions và Announcements
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/center-service/src/main/resources/db/migration/V2__init_promotions_announcements.sql

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-009], [DAT-010]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/center-service/src/main/resources/db/migration/V2__init_promotions_announcements.sql`. Bảng `promotions` (`promo_id` UUID PRIMARY KEY DEFAULT gen_random_uuid(), `code` VARCHAR(30) UNIQUE NOT NULL, `discount_percent` SMALLINT NOT NULL với CHECK (discount_percent BETWEEN 0 AND 100), `start_date` DATE, `end_date` DATE, `description` TEXT, `center_id` UUID, `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (center_id) REFERENCES centers(center_id), CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)). Bảng `announcements` (`announcement_id` UUID PRIMARY KEY DEFAULT gen_random_uuid(), `title` VARCHAR(150) NOT NULL, `content` TEXT NOT NULL, `start_date` DATE, `end_date` DATE, `center_id` UUID, `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (center_id) REFERENCES centers(center_id)).

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- =====================================================================
-- V2__init_promotions_announcements.sql
-- Khởi tạo bảng Promotions và Announcements
-- =====================================================================

CREATE TABLE promotions (
    promo_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) UNIQUE NOT NULL,
    discount_percent SMALLINT NOT NULL,
    start_date DATE,
    end_date DATE,
    description TEXT,
    center_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_promotions_center FOREIGN KEY (center_id) REFERENCES centers(center_id),
    CONSTRAINT chk_promotions_discount CHECK (discount_percent BETWEEN 0 AND 100),
    CONSTRAINT chk_promotions_dates CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_promotions_code ON promotions(code);
CREATE INDEX idx_promotions_center_id ON promotions(center_id);
CREATE INDEX idx_promotions_dates ON promotions(start_date, end_date);

CREATE TABLE announcements (
    announcement_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    start_date DATE,
    end_date DATE,
    center_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_announcements_center FOREIGN KEY (center_id) REFERENCES centers(center_id)
);

CREATE INDEX idx_announcements_center_id ON announcements(center_id);
CREATE INDEX idx_announcements_dates ON announcements(start_date, end_date);
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 4.3: Tạo tập lệnh di trú V3 - bảng SystemSettings
##### Tác Nhân Được Phân Công: Coder
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/backend/center-service/src/main/resources/db/migration/V3__init_system_settings.sql

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-011]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/backend/center-service/src/main/resources/db/migration/V3__init_system_settings.sql`. Bảng `system_settings` (`setting_key` VARCHAR(50) PRIMARY KEY, `setting_value` TEXT NOT NULL, `description` VARCHAR(200), `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `updated_by` UUID, FOREIGN KEY (updated_by) REFERENCES users(user_id)). Tạo chỉ mục `idx_system_settings_updated_at`. Chèn dữ liệu khởi tạo: `default_locale=vi`, `dashboard_refresh_minutes=15`, `max_renewal_days=365`, `notification_retry_max=3`.

* **Đặc Tả DDL SQL Lược Đồ Cơ Sở Dữ Liệu [DAT-XXX]:** <!--START_DDL_MIGRATION-->
```sql
-- =====================================================================
-- V3__init_system_settings.sql
-- Khởi tạo bảng SystemSettings cho Membership Hub
-- =====================================================================

CREATE TABLE system_settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    description VARCHAR(200),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    CONSTRAINT fk_system_settings_user FOREIGN KEY (updated_by) REFERENCES users(user_id)
);

CREATE INDEX idx_system_settings_updated_at ON system_settings(updated_at);

INSERT INTO system_settings (setting_key, setting_value, description) VALUES
    ('default_locale', 'vi', 'Locale mặc định cho hệ thống'),
    ('dashboard_refresh_minutes', '15', 'Chu kỳ làm mới dashboard (phút)'),
    ('max_renewal_days', '365', 'Số ngày gia hạn thẻ tối đa'),
    ('notification_retry_max', '3', 'Số lần thử lại gửi thông báo tối đa');
```
<!--END_DDL_MIGRATION-->

#### 📝 NHIỆM VỤ PHỤ 4.4: Tạo hợp đồng OpenAPI cho 4 luồng nghiệp vụ cốt lõi
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/api/openapi-spec.yaml

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-006], [ARC-007], [ARC-008], [ARC-009]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/docs/api/openapi-spec.yaml` đặc tả OpenAPI 3.0.3 cho 4 luồng nghiệp vụ cốt lõi. Luồng Xác thực (ARC-006): `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/oauth/{provider}`. Luồng Điểm danh (ARC-007): `POST /api/attendance/scan` với body chứa `qrPayload` base64. Luồng Thông báo (ARC-008): `POST /api/notifications/dispatch`, `GET /api/notifications/history`. Luồng Tích hợp di động (ARC-009): `GET /api/mobile/dashboard`, `POST /api/mobile/device-token`. Mỗi endpoint khai báo request/response schema, mã trạng thái HTTP, cấu trúc JWT bearer security scheme. Tệp phải hợp lệ theo OpenAPI 3.0 spec.

* **Hợp Đồng API & Định Tuyến Sự Kiện [ARC-XXX]:** <!--START_API_CONTRACT-->
```yaml
openapi: 3.0.3
info:
  title: Membership Hub API
  version: 1.0.0
  description: Hợp đồng tích hợp cho 4 luồng nghiệp vụ cốt lõi: Xác thực, Điểm danh, Thông báo, Tích hợp di động
servers:
  - url: https://api.membershiphub.com
    description: Production
  - url: https://staging-api.membershiphub.com
    description: Staging

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
  schemas:
    AuthRegisterRequest:
      type: object
      required: [email, password, termsAccepted]
      properties:
        email:
          type: string
          format: email
          maxLength: 255
        password:
          type: string
          minLength: 8
        fullName:
          type: string
          maxLength: 100
        termsAccepted:
          type: boolean
    AuthLoginRequest:
      type: object
      required: [email, password]
      properties:
        email:
          type: string
          format: email
        password:
          type: string
    AuthTokenResponse:
      type: object
      properties:
        accessToken:
          type: string
        refreshToken:
          type: string
        expiresIn:
          type: integer
        tokenType:
          type: string
          default: Bearer
    AttendanceScanRequest:
      type: object
      required: [qrPayload]
      properties:
        qrPayload:
          type: string
          description: Base64 mã hóa chứa studentId và courseId
    AttendanceScanResponse:
      type: object
      properties:
        attendanceId:
          type: string
          format: uuid
        recordedAt:
          type: string
          format: date-time
        duplicate:
          type: boolean
    NotificationDispatchRequest:
      type: object
      required: [channel, message, target]
      properties:
        channel:
          type: string
          enum: [PUSH, ZALO, EMAIL, SMS]
        message:
          type: string
        target:
          type: object
          properties:
            userId:
              type: string
              format: uuid
            groupZalo:
              type: string
    MobileDashboardResponse:
      type: object
      properties:
        role:
          type: string
        navigation:
          type: array
          items:
            type: object
        upcomingCourses:
          type: array
          items:
            type: object
    DeviceTokenRequest:
      type: object
      required: [deviceToken, platform]
      properties:
        deviceToken:
          type: string
        platform:
          type: string
          enum: [IOS, ANDROID]

security:
  - bearerAuth: []

paths:
  /api/auth/register:
    post:
      summary: Đăng ký tài khoản mới
      tags: [Authentication]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AuthRegisterRequest'
      responses:
        '201':
          description: Đăng ký thành công
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthTokenResponse'
        '400':
          description: Dữ liệu không hợp lệ
  /api/auth/login:
    post:
      summary: Đăng nhập email/mật khẩu
      tags: [Authentication]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AuthLoginRequest'
      responses:
        '200':
          description: Đăng nhập thành công
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthTokenResponse'
  /api/auth/refresh:
    post:
      summary: Làm mới access token
      tags: [Authentication]
      responses:
        '200':
          description: Token mới
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthTokenResponse'
  /api/auth/oauth/{provider}:
    post:
      summary: Xác thực OAuth2 (Firebase/Google/Facebook)
      tags: [Authentication]
      parameters:
        - name: provider
          in: path
          required: true
          schema:
            type: string
            enum: [firebase, google, facebook]
      responses:
        '200':
          description: Xác thực thành công
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthTokenResponse'
  /api/attendance/scan:
    post:
      summary: Quét QR điểm danh
      tags: [Attendance]
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AttendanceScanRequest'
      responses:
        '200':
          description: Điểm danh thành công
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AttendanceScanResponse'
        '409':
          description: Xung đột - điểm danh đã tồn tại
  /api/notifications/dispatch:
    post:
      summary: Phát thông báo
      tags: [Notifications]
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/NotificationDispatchRequest'
      responses:
        '202':
          description: Thông báo đã được xếp hàng
  /api/mobile/dashboard:
    get:
      summary: Lấy dashboard theo vai trò
      tags: [Mobile]
      security:
        - bearerAuth: []
      responses:
        '200':
          description: Dashboard
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MobileDashboardResponse'
  /api/mobile/device-token:
    post:
      summary: Đăng ký device token FCM/APNs
      tags: [Mobile]
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/DeviceTokenRequest'
      responses:
        '204':
          description: Token đã đăng ký
```
<!--END_API_CONTRACT-->

#### 📝 NHIỆM VỤ PHỤ 4.5: Kiểm thử tích hợp di trú V3 notification, promotion, announcement, systemsetting
##### Tác Nhân Được Phân Công: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/backend/notification-service/src/test/java/org/nlh4j/membershiphub/notificationservice/NotificationSchemaMigrationIT.java

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-008], [DAT-009], [DAT-010], [DAT-011]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo lớp kiểm thử tích hợp Flyway `./sources/backend/notification-service/src/test/java/org/nlh4j/membershiphub/notificationservice/NotificationSchemaMigrationIT.java` sử dụng `@QuarkusTest` và Testcontainers PostgreSQL 16. Inject `Flyway` và `DataSource`, thực thi `flyway.migrate()`, xác minh các bảng `notifications`, `promotions`, `announcements`, `system_settings` tồn tại. Kiểm tra ràng buộc CHECK `chk_notifications_channel` từ chối giá trị ngoài tập cho phép. Xác minh `system_settings` chứa 4 bản ghi khởi tạo (`default_locale`, `dashboard_refresh_minutes`, `max_renewal_days`, `notification_retry_max`).

```java
package org.nlh4j.membershiphub.notificationservice;

import io.quarkus.test.junit.QuarkusTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@QuarkusTest
public class NotificationSchemaMigrationIT {

    @Inject
    Flyway flyway;

    @Inject
    DataSource dataSource;

    @Test
    void testAllRemainingTablesExist() throws Exception {
        Assertions.assertNotNull(flyway);
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT count(*) FROM information_schema.tables " +
                    "WHERE table_schema='public' AND table_name IN " +
                    "('notifications','promotions','announcements','system_settings')")) {
                rs.next();
                Assertions.assertEquals(4, rs.getInt(1),
                        "Phải tồn tại 4 bảng còn lại");
            }
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT count(*) FROM system_settings")) {
                rs.next();
                Assertions.assertTrue(rs.getInt(1) >= 4,
                        "Phải có ít nhất 4 thiết lập hệ thống mặc định");
            }
        }
    }
}
```

#### 📝 NHIỆM VỤ PHỤ 4.6: Kiểm thử tích hợp hợp đồng OpenAPI
##### Tác Nhân Được Phân Công: Tester
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** INTEGRATION_SCOPE;./sources/infra/test/openapi-spec-validation.sh

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-006], [ARC-007], [ARC-008], [ARC-009]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Tạo tệp `./sources/infra/test/openapi-spec-validation.sh` chứa kịch bản bash kiểm thử tính hợp lệ của tệp OpenAPI. Sử dụng `swagger-cli` hoặc `openapi-spec-validator` thông qua Docker, thực thi `docker run --rm -v $(pwd)/sources/docs/api:/spec redocly/cli lint /spec/openapi-spec.yaml` để xác minh cú pháp. Thoát mã 0 nếu tệp hợp lệ, mã 1 nếu có lỗi cú pháp hoặc tham chiếu schema bị thiếu. In log chi tiết cho mỗi endpoint được phát hiện.

```bash
#!/usr/bin/env bash
set -euo pipefail

SPEC_PATH="./sources/docs/api/openapi-spec.yaml"
DOCKER_IMAGE="redocly/cli:latest"

echo "============================================================"
echo "  KIỂM THỬ TÍNH HỢP LỆ CỦA HỢP ĐỒNG OPENAPI"
echo "============================================================"

if [ ! -f "${SPEC_PATH}" ]; then
    echo "[LỖI] Không tìm thấy tệp đặc tả: ${SPEC_PATH}"
    exit 1
fi

echo "[INFO] Đang chạy redocly/cli lint trên ${SPEC_PATH}"
docker run --rm -v "$(pwd)":/spec "${DOCKER_IMAGE}" lint "/spec/${SPEC_PATH#./}"

echo "============================================================"
echo "  HỢP ĐỒNG OPENAPI ĐÃ ĐƯỢC XÁC MINH THÀNH CÔNG"
echo "============================================================"
exit 0
```

#### 📝 NHIỆM VỤ PHỤ 4.7: Đánh giá tổng thể schema và hợp đồng tích hợp
##### Tác Nhân Được Phân Công: Reviewer
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/api/openapi-spec.yaml

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[DAT-008], [DAT-009], [DAT-010], [DAT-011], [ARC-006], [ARC-007], [ARC-008], [ARC-009]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Đánh giá cuối cùng giai đoạn 1: xác minh toàn bộ 11 bảng đã được tạo với ràng buộc chuẩn ANSI, không sử dụng ENUM, đầy đủ FK và CHECK. Đánh giá hợp đồng OpenAPI: bảo đảm 4 luồng nghiệp vụ (xác thực, điểm danh, thông báo, tích hợp di động) đều có endpoint với security scheme JWT đúng, schema request/response đầy đủ. Sinh báo cáo đánh giá tổng hợp với bảng tuân thủ cho từng Tag ID [DAT-001] đến [DAT-011] và [ARC-006] đến [ARC-009]. Lập danh sách khuyến nghị cải tiến nếu phát hiện bất thường.

#### 📝 NHIỆM VỤ PHỤ 4.8: Hoàn thiện tài liệu kiến trúc tổng thể
##### Tác Nhân Được Phân Công: Doc
##### Thành Phần Mục Tiêu & Yêu Cầu Kỹ Thuật:
* **Đường Dẫn Mục Tiêu:** ./sources/docs/architecture/blueprint.md

* **Mã Thẻ Truy Xuất:** <!--START_TAGS-->[ARC-006], [ARC-007], [ARC-008], [ARC-009]<!--END_TAGS-->

* **Hướng Dẫn Nhiệm Vụ Kỹ Thuật Cấp Thấp:** Bổ sung section "Sơ đồ Mermaid - 4 Luồng nghiệp vụ cốt lõi" vào `./sources/docs/architecture/blueprint.md`. Bao gồm 4 sơ đồ Mermaid sequenceDiagram: (1) Luồng Xác thực (ARC-006) mô tả client gửi email/password đến user-service, kiểm tra credentials, ký JWT 15 phút, trả refresh token 7 ngày; (2) Luồng Điểm danh (ARC-007) mô tả mobile app gửi QR payload đến attendance-service, xác thực quan hệ học viên-khóa học, idempotency check, ghi nhận; (3) Luồng Thông báo (ARC-008) mô tả sự kiện nghiệp vụ publish Kafka topic, notification-service consume, dispatch FCM/APNs/Zalo; (4) Luồng Tích hợp di động (ARC-009) mô tả mobile app gọi REST API qua bearer token, cache offline, đồng bộ khi có mạng. Tài liệu dùng tiếng Việt cho phần mô tả, giữ nguyên tên thực thể tiếng Anh.