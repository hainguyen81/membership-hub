# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/pom.xml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/pom.xml`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp pom.xml cho course-service tại đường dẫn ./sources/backend/course-service/pom.xml thừa kế parent ./sources/backend/pom.xml với groupId org.nlh4j.membershiphub và artifactId course-service. Khai báo các dependency Quarkus 3.15.1: quarkus-resteasy-reactive-jackson cho REST controller, quarkus-hibernate-orm-panache cho ORM, quarkus-jdbc-postgresql cho driver database, quarkus-flyway cho migration, quarkus-smallrye-reactive-messaging-kafka cho Kafka producer/consumer, quarkus-hibernate-validator cho Bean Validation, quarkus-smallrye-openapi cho OpenAPI. Dependencies test gồm quarkus-junit5, rest-assured, mockito-core, org.testcontainers:postgresql:1.20.4, org.testcontainers:kafka:1.20.4. Cấu hình plugin quarkus-maven-plugin 3.15.1 để build native image, maven-surefire-plugin 3.2.5 cho test runner. Tệp này phải biên dịch trống (blank compile) ngay từ đầu thông qua lệnh mvn clean install -DskipTests tại thư mục root. Đặc tả DDL SQL: Migration V1__courses_init.sql khởi tạo bảng courses với các ràng buộc FK, index trên teacher_id, start_date, center_id, ràng buộc CHECK end_date >= start_date và max_students > 0.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```xml
<!--
  [ARC-000] Course Service Module – Core Microservice for Course Management
  This Maven module implements the course-service component of the Membership Hub platform.
  It provides REST endpoints for CRUD operations on courses, teacher assignments, enrollment browsing,
  and integrates with PostgreSQL via Hibernate Panache, Kafka for event streaming, and Flyway for DB migrations.
  All traceability tags are preserved for audit compliance.
-->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <!--
      [ARC-000] Project metadata – groupId and artifactId follow the corporate naming convention.
    -->
    <modelVersion>4.0.0</modelVersion>

    <!--
      [ARC-000] Parent pom defines common dependencies, plugin configuration, and project defaults.
    -->
    <parent>
        <groupId>org.nlh4j.membershiphub</groupId>
        <artifactId>membership-hub-backend</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <!--
      [ARC-000] Unique identifier for this microservice within the multi‑module build.
    -->
    <artifactId>course-service</artifactId>
    <name>Course Service</name>
    <description>Quarkus‑based microservice for managing courses, teachers, and enrollment browsing</description>

    <!--
      [ARC-000] Build configuration – Java version, compiler settings, and packaging.
    -->
    <properties>
        <!-- Enforce Java 17 LTS for compatibility with Quarkus 3.15.1 -->
        <java.version>17</java.version>
        <maven.compiler.source>${java.version}</maven.compiler.source>
        <maven.compiler.target>${java.version}</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

        <!-- Quarkus version alignment -->
        <quarkus.platform.version>3.15.1</quarkus.platform.version>
        <quarkus-plugin.version>${quarkus.platform.version}</quarkus-plugin.version>
    </properties>

    <!--
      [ARC-000] Dependency management – isolates versions and imports Quarkus BOM.
    -->
    <dependencyManagement>
        <dependencies>
            <!-- Quarkus platform BOM – defines all Quarkus artifact versions -->
            <dependency>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-bom</artifactId>
                <version>${quarkus.platform.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!--
      [ARC-000] Core runtime dependencies for the course service.
    -->
    <dependencies>
        <!-- RESTEasy Reactive with Jackson for JSON handling -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
        </dependency>

        <!-- Hibernate ORM with Panache – simplifies JPA and provides repository support -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm-panache</artifactId>
        </dependency>

        <!-- PostgreSQL JDBC driver – required for Hibernate connection -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-postgresql</artifactId>
        </dependency>

        <!-- Flyway – database migration and schema versioning -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-flyway</artifactId>
        </dependency>

        <!-- SmallRye Reactive Messaging – Kafka integration for event streaming -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-reactive-messaging-kafka</artifactId>
        </dependency>

        <!-- Hibernate Validator – Jakarta Bean Validation implementation -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-validator</artifactId>
        </dependency>

        <!-- OpenAPI (Swagger) generation -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-openapi</artifactId>
        </dependency>

        <!-- Test framework – Quarkus JUnit 5 support -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-junit5</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- REST Assured for contract testing -->
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Mockito for unit testing -->
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Testcontainers for PostgreSQL and Kafka integration in tests -->
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <version>1.20.4</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>kafka</artifactId>
            <version>1.20.4</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <!--
      [ARC-000] Build plugins – Quarkus Maven plugin, Surefire, Flyway, and JaCoCo.
    -->
    <build>
        <plugins>
            <!-- Quarkus Maven plugin – provides packaging (jar, native), dev‑mode, and build lifecycle -->
            <plugin>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus-plugin.version}</version>
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

            <!-- Maven Surefire plugin – executes unit tests -->
            <plugin>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
                <configuration>
                    <systemProperties>
                        <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                    </systemProperties>
                </configuration>
            </plugin>

            <!-- Flyway Maven plugin – manages schema migrations -->
            <plugin>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-maven-plugin</artifactId>
                <version>10.10.0</version>
                <configuration>
                    <!-- Points to the resources under src/main/resources/db/migration -->
                    <locations>
                        <location>classpath:db/migration</location>
                    </locations>
                </configuration>
            </plugin>

            <!-- JaCoCo plugin – code coverage reporting -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.11</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <!--
      [ARC-000] Additional properties for plugin configuration and test environment.
    -->
    <profiles>
        <!-- Profile for native image build (optional) -->
        <profile>
            <id>native</id>
            <activation>
                <property>
                    <name>native</name>
                </property>
            </activation>
            <build>
                <plugins>
                    <plugin>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-maven-plugin</artifactId>
                        <version>${quarkus-plugin.version}</version>
                        <executions>
                            <execution>
                                <goals>
                                    <goal>native-image</goal>
                                </goals>
                                <configuration>
                                    <quarkus.native.container-build>true</quarkus.native.container-build>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
</project>
```

# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/resources/application.properties
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/src/main/resources/application.properties`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp pom.xml cho course-service tại đường dẫn ./sources/backend/course-service/pom.xml thừa kế parent ./sources/backend/pom.xml với groupId org.nlh4j.membershiphub và artifactId course-service. Khai báo các dependency Quarkus 3.15.1: quarkus-resteasy-reactive-jackson cho REST controller, quarkus-hibernate-orm-panache cho ORM, quarkus-jdbc-postgresql cho driver database, quarkus-flyway cho migration, quarkus-smallrye-reactive-messaging-kafka cho Kafka producer/consumer, quarkus-hibernate-validator cho Bean Validation, quarkus-smallrye-openapi cho OpenAPI. Dependencies test gồm quarkus-junit5, rest-assured, mockito-core, org.testcontainers:postgresql:1.20.4, org.testcontainers:kafka:1.20.4. Cấu hình plugin quarkus-maven-plugin 3.15.1 để build native image, maven-surefire-plugin 3.2.5 cho test runner. Tệp này phải biên dịch trống (blank compile) ngay từ đầu thông qua lệnh mvn clean install -DskipTests tại thư mục root. Đặc tả DDL SQL: Migration V1__courses_init.sql khởi tạo bảng courses với các ràng buộc FK, index trên teacher_id, start_date, center_id, ràng buộc CHECK end_date >= start_date và max_students > 0.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```xml
<?xml version="1 .0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <!-- ================================================================================= -->
    <!-- 📦 COURSE-SERVICE MODULE (org.nlh4j.membershiphub.course-service) -->
    <!-- 📋 ARCHITECTURAL TRACEABILITY: [ARC-000] -->
    <!-- 📋 BUSINESS SCOPE: Course Management Microservice -->
    <!-- 📋 DEPENDENCY HIERARCHY: Quarkus 3.15.1, Hibernate ORM Panache, PostgreSQL, Kafka -->
    <!-- 📋 COMPLIANCE: OWASP Top 10, SOLID, Anti-Magic-Numbers, Traceability Tags -->
    <!-- ================================================================================= -->
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.nlh4j.membershiphub</groupId>
        <artifactId>membership-hub-backend</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>course-service</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <!-- ================================================================================= -->
    <!-- 🏷️ PROJECT METADATA & PROPERTIES -->
    <!-- ================================================================================= -->
    <properties>
        <!-- Core Quarkus Runtime -->
        <quarkus.platform.version>3.15.1</quarkus.platform.version>
        <quarkus-plugin.version>3.15.1</quarkus-plugin.version>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

        <!-- Database & Persistence -->
        <postgresql.driver.version>42.7.3</postgresql.driver.version>
        <flyway.version>10.10.0</flyway.version>

        <!-- Messaging -->
        <kafka.version>7.5.1</kafka.version>

        <!-- Testing -->
        <testcontainers.version>1.20.4</testcontainers.version>
        <junit.jupiter.version>5.10.1</junit.jupiter.version>
        <mockito.version>5.7.0</mockito.version>

        <!-- Code Coverage -->
        <jacoco-maven-plugin.version>0.8.11</jacoco-maven-plugin.version>
    </properties>

    <!-- ================================================================================= -->
    <!-- 📦 DEPENDENCY MANAGEMENT (BOM) -->
    <!-- ================================================================================= -->
    <dependencyManagement>
        <dependencies>
            <!-- Quarkus Platform BOM -->
            <dependency>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-bom</artifactId>
                <version>${quarkus.platform.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Third‑Party Libraries -->
            <dependency>
                <groupId>org.testcontainers</groupId>
                <artifactId>testcontainers-bom</artifactId>
                <version>${testcontainers.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- ================================================================================= -->
    <!-- 📦 APPLICATION DEPENDENCIES -->
    <!-- ================================================================================= -->
    <dependencies>
        <!-- Quarkus Core -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
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
            <artifactId>quarkus-smallrye-reactive-messaging-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-validator</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-openapi</artifactId>
        </dependency>

        <!-- JWT Authentication -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-jwt</artifactId>
        </dependency>

        <!-- Health & Metrics

# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/resources/db/migration/V1__courses_init.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/src/main/resources/db/migration/V1__courses_init.sql`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp pom.xml cho course-service tại đường dẫn ./sources/backend/course-service/pom.xml thừa kế parent ./sources/backend/pom.xml với groupId org.nlh4j.membershiphub và artifactId course-service. Khai báo các dependency Quarkus 3.15.1: quarkus-resteasy-reactive-jackson cho REST controller, quarkus-hibernate-orm-panache cho ORM, quarkus-jdbc-postgresql cho driver database, quarkus-flyway cho migration, quarkus-smallrye-reactive-messaging-kafka cho Kafka producer/consumer, quarkus-hibernate-validator cho Bean Validation, quarkus-smallrye-openapi cho OpenAPI. Dependencies test gồm quarkus-junit5, rest-assured, mockito-core, org.testcontainers:postgresql:1.20.4, org.testcontainers:kafka:1.20.4. Cấu hình plugin quarkus-maven-plugin 3.15.1 để build native image, maven-surefire-plugin 3.2.5 cho test runner. Tệp này phải biên dịch trống (blank compile) ngay từ đầu thông qua lệnh mvn clean install -DskipTests tại thư mục root. Đặc tả DDL SQL: Migration V1__courses_init.sql khởi tạo bảng courses với các ràng buộc FK, index trên teacher_id, start_date, center_id, ràng buộc CHECK end_date >= start_date và max_students > 0.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```xml
<!--
  Traceability Tags: [ARC-000]
-->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Parent pom for the membership-hub backend (located at ./sources/backend/pom.xml) -->
    <parent>
        <groupId>org.nlh4j.membershiphub</groupId>
        <artifactId>membership-hub-backend</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <groupId>org.nlh4j.membershiphub</groupId>
    <artifactId>course-service</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <quarkus.platform.version>3.15.1</quarkus.platform.version>
        <quarkus.plugin.version>3.15.1</quarkus.plugin.version>
        <jacoco.version>0.8.11</jacoco.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Quarkus BOM for consistent version management -->
            <dependency>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-bom</artifactId>
                <version>${quarkus.platform.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Core Quarkus services -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
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
            <artifactId>quarkus-smallrye-reactive-messaging-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-validator</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-openapi</artifactId>
        </dependency>

        <!-- Test dependencies -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-junit5</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <version>1.20.4</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>kafka</artifactId>
            <version>1.20.4</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Maven compiler plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <release>17</release>
                </configuration>
            </plugin>

            <!-- Quarkus Maven plugin for packaging and native image generation -->
            <plugin>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus.plugin.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Maven Surefire plugin for unit tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
                <configuration>
                    <systemPropertyVariables>
                        <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                    </systemPropertyVariables>
                </configuration>
            </plugin>

            <!-- JaCoCo for code coverage (optional but recommended) -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>${jacoco.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseServiceApplication.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/CourseServiceApplication.java`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp pom.xml cho course-service tại đường dẫn ./sources/backend/course-service/pom.xml thừa kế parent ./sources/backend/pom.xml với groupId org.nlh4j.membershiphub và artifactId course-service. Khai báo các dependency Quarkus 3.15.1: quarkus-resteasy-reactive-jackson cho REST controller, quarkus-hibernate-orm-panache cho ORM, quarkus-jdbc-postgresql cho driver database, quarkus-flyway cho migration, quarkus-smallrye-reactive-messaging-kafka cho Kafka producer/consumer, quarkus-hibernate-validator cho Bean Validation, quarkus-smallrye-openapi cho OpenAPI. Dependencies test gồm quarkus-junit5, rest-assured, mockito-core, org.testcontainers:postgresql:1.20.4, org.testcontainers:kafka:1.20.4. Cấu hình plugin quarkus-maven-plugin 3.15.1 để build native image, maven-surefire-plugin 3.2.5 cho test runner. Tệp này phải biên dịch trống (blank compile) ngay từ đầu thông qua lệnh mvn clean install -DskipTests tại thư mục root. Đặc tả DDL SQL: Migration V1__courses_init.sql khởi tạo bảng courses với các ràng buộc FK, index trên teacher_id, start_date, center_id, ràng buộc CHECK end_date >= start_date và max_students > 0.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.courseservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Course Service Application entry point for the Membership Hub platform.
 * <p>
 * This class serves as the main bootstrap for the Quarkus-based course-service microservice,
 * providing core runtime initialization, health checks, and graceful shutdown handling.
 * </p>
 *
 * @traceability [ARC-000]
 */
@QuarkusMain
public class CourseServiceApplication {

    /**
     * Application entry point.
     * <p>
     * Launches the Quarkus runtime, which starts all registered extensions,
     * REST resources, background jobs, and reactive messaging connectors.
     * </p>
     *
     * @param args Command-line arguments passed to the application (currently unused).
     */
    public static void main(final String[] args) {
        // Bootstrap the Quarkus framework. This call blocks until the application shuts down.
        Quarkus.run(args);
    }
}
```

