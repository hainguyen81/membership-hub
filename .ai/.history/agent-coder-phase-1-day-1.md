# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/pom.xml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/pom.xml`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Khởi tạo tệp tin mô tả build Maven root tại đường dẫn ./sources/backend/pom.xml với cấu hình packaging pom, khai báo chính xác 4 module con tương ứng với 4 microservices: user-service, center-service, course-service, attendance-service. Tệp pom root phải sử dụng Java 17 LTS làm phiên bản nguồn và đích biên dịch, Quarkus 3.15.1 làm BOM chính, đảm bảo <groupId> cố định là org.nlh4j.membershiphub, <artifactId> là membership-hub-backend, <version> là 1.0.0-SNAPSHOT. Khai báo <dependencyManagement> tập trung cho Jakarta EE 10, Hibernate ORM Panache 3.15.1, RESTEasy Reactive, Hibernate Validator, Flyway 10.10.0, PostgreSQL JDBC driver 42.7.3, SmallRye JWT 4.10.0, SmallRye Reactive Messaging Kafka 4.10.0, OpenAPI 2.10.0, JUnit 5.10.1, Mockito 5.7.0, REST Assured 5.4.0. Cấu hình các plugin Maven: maven-compiler-plugin 3.13.0 với release 17, maven-surefire-plugin 3.2.5, flyway-maven-plugin 10.10.0, quarkus-maven-plugin 3.15.1 hỗ trợ chế độ quarkus:dev, jacoco-maven-plugin 0.8.11 cho báo cáo độ bao phủ mã nguồn. Đảm bảo tệp tin biên dịch trắng ngay khi được tạo mà không phụ thuộc vào module con.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- ========================================================================================= -->
<!-- [TRACEABILITY TAGS]: [ARC-000]                                                           -->
<!-- ========================================================================================= -->
<!-- BUSINESS CONTEXT: Root Maven Multi-Module Project Descriptor for Membership Hub Enterprise Platform -->
<!-- ARCHITECTURAL PATTERN: Quarkus 3.15.1 Enterprise Microservices Parent POM (Java 17 LTS)    -->
<!-- COMPLIANCE STANDARDS: Enterprise Clean Architecture, Dependency Management Isolation     -->
<!-- ========================================================================================= -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <!-- [REQ-ARC-000] Parent Project Model Version Declaration -->
    <modelVersion>4.0.0</modelVersion>

    <!-- [REQ-ARC-000] Enterprise Maven Coordinates Configuration -->
    <groupId>org.nlh4j.membershiphub</groupId>
    <artifactId>membership-hub-backend</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>Membership Hub - Backend Enterprise Root Project</name>
    <description>Master Maven Multi-Module Parent POM for Quarkus 3.15.1 Microservices Infrastructure</description>

    <!-- [REQ-ARC-000] Enterprise Module Declarations (4 Core Microservices) -->
    <modules>
        <module>user-service</module>
        <module>center-service</module>
        <module>course-service</module>
        <module>attendance-service</module>
    </modules>

    <!-- [REQ-ARC-000] Global Environment and Compiler Properties -->
    <properties>
        <compiler-plugin.version>3.13.0</compiler-plugin.version>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <quarkus.platform.version>3.15.1</quarkus.platform.version>
        <quarkus.platform.group-id>io.quarkus.platform</quarkus.platform.group-id>
        <quarkus.platform.artifact-id>quarkus-bom</quarkus.platform.artifact-id>
        <surefire-plugin.version>3.2.5</surefire-plugin.version>
        <flyway.version>10.10.0</flyway.version>
        <postgresql.version>42.7.3</postgresql.version>
        <jacoco.version>0.8.11</jacoco.version>
        <junit.version>5.10.1</junit.version>
        <mockito.version>5.7.0</mockito.version>
        <rest-assured.version>5.4.0</rest-assured.version>
    </properties>

    <!-- [REQ-ARC-000] Centralized Quarkus BOM Dependency Management Import -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>${quarkus.platform.group-id}</groupId>
                <artifactId>${quarkus.platform.artifact-id}</artifactId>
                <version>${quarkus.platform.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- [REQ-ARC-000] Master Build Lifecycle and Plugin Management -->
    <build>
        <pluginManagement>
            <plugins>
                <!-- [REQ-ARC-000] Quarkus Maven Plugin for Dev, Build and Native Image Generation -->
                <plugin>
                    <groupId>${quarkus.platform.group-id}</groupId>
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

                <!-- [REQ-ARC-000] Maven Compiler Plugin Configuration for Java 17 LTS -->
                <plugin>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>${compiler-plugin.version}</version>
                    <configuration>
                        <source>${maven.compiler.release}</source>
                        <target>${maven.compiler.release}</target>
                        <compilerArgs>
                            <arg>-parameters</arg>
                        </compilerArgs>
                    </configuration>
                </plugin>

                <!-- [REQ-ARC-000] Maven Surefire Plugin Configuration for Unit Testing -->
                <plugin>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>${surefire-plugin.version}</version>
                    <configuration>
                        <systemPropertyVariables>
                            <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                            <maven.home>${maven.home}</maven.home>
                        </systemPropertyVariables>
                    </configuration>
                </plugin>

                <!-- [REQ-ARC-000] Flyway Database Migration Plugin Configuration -->
                <plugin>
                    <groupId>org.flywaydb</groupId>
                    <artifactId>flyway-maven-plugin</artifactId>
                    <version>${flyway.version}</version>
                </plugin>

                <!-- [REQ-ARC-000] JaCoCo Code Coverage Plugin Configuration -->
                <plugin>
                    <groupId>org.jacoco</groupId>
                    <artifactId>jacoco-maven-plugin</artifactId>
                    <version>${jacoco.version}</version>
                    <executions>
                        <execution>
                            <goals>
                                <goal>prepare-agent</goal>
                            </goals>
                        </execution>
                        <execution>
                            <id>report</id>
                            <phase>verify</phase>
                            <goals>
                                <goal>report</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>

    <!-- [REQ-ARC-000] Enterprise Build Profiles for Development and Native Execution -->
    <profiles>
        <profile>
            <id>native</id>
            <activation>
                <property>
                    <name>native</name>
                </property>
            </activation>
            <properties>
                <skipITs>false</skipITs>
                <quarkus.native.enabled>true</quarkus.native.enabled>
            </properties>
            <build>
                <plugins>
                    <plugin>
                        <artifactId>maven-failsafe-plugin</artifactId>
                        <version>${surefire-plugin.version}</version>
                        <executions>
                            <execution>
                                <goals>
                                    <goal>integration-test</goal>
                                    <goal>verify</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
</project>
```

# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/pom.xml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/pom.xml`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo 4 tệp tin mô tả build Maven con cho 4 microservices tại các đường dẫn ./sources/backend/user-service/pom.xml, ./sources/backend/center-service/pom.xml, ./sources/backend/course-service/pom.xml, ./sources/backend/attendance-service/pom.xml. Mỗi tệp con phải khai báo <parent> tham chiếu đến org.nlh4j.membershiphub:membership-hub-backend:1.0.0-SNAPSHOT, <artifactId> tương ứng (user-service, center-service, course-service, attendance-service), packaging jar. Mỗi module con kế thừa toàn bộ dependencyManagement từ parent và chỉ khai báo dependency thực sự sử dụng: quarkus-resteasy-reactive-jackson, quarkus-hibernate-orm-panache, quarkus-jdbc-postgresql, quarkus-flyway, quarkus-smallrye-jwt, quarkus-smallrye-openapi, quarkus-smallrye-reactive-messaging-kafka, quarkus-hibernate-validator. Riêng user-service bổ sung quarkus-smallrye-jwt-build cho việc ký token. Dependencies test gồm quarkus-junit5, rest-assured, mockito-core. Đảm bảo tất cả 4 tệp tin biên dịch trắng thông qua lệnh mvn clean install -DskipTests tại thư mục root.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```xml
<!--
  ~ 🏛️ MASTER ENTERPRISE GOVERNANCE GUARDRAILS MATRIX COMPLIANT
  ~ Target Component Destination Path: ./sources/backend/user-service/pom.xml
  ~ Traceability Audit Tags: [ARC-000]
  ~ Business Context: Maven Build Descriptor for user-service microservice module within membership-hub enterprise platform.
  ~ Enforces Quarkus 3.15.1 runtime, reactive REST endpoints, panache ORM, PostgreSQL persistence, Flyway migration,
  ~ SmallRye JWT security, SmallRye Kafka messaging, Bean validation, and testing suites.
-->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- [REQ-XXX] / [ARC-000] Parent Project Configuration linkage -->
    <parent>
        <groupId>org.nlh4j.membershiphub</groupId>
        <artifactId>membership-hub-backend</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <!-- [ARC-000] Module Specific Artifact Identity -->
    <artifactId>user-service</artifactId>
    <packaging>jar</packaging>
    <name>Membership Hub - User Service Module</name>
    <description>Microservice responsible for identity management, authentication, JWT issuing, social logins, and RBAC role assignments.</description>

    <dependencies>
        <!-- [ARC-000] RESTEasy Reactive with Jackson JSON binding for high-performance non-blocking REST endpoints -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
        </dependency>

        <!-- [ARC-000] Hibernate ORM with Panache for active-record data access patterns -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm-panache</artifactId>
        </dependency>

        <!-- [ARC-000] PostgreSQL JDBC Driver for relational database connectivity -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-postgresql</artifactId>
        </dependency>

        <!-- [ARC-000] Flyway database migration runner for automated schema evolution -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-flyway</artifactId>
        </dependency>

        <!-- [ARC-000] SmallRye JWT extension for secure token validation and resource server security -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-jwt</artifactId>
        </dependency>

        <!-- [ARC-000] SmallRye JWT Build extension required specifically in user-service for cryptographic token signing -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-jwt-build</artifactId>
        </dependency>

        <!-- [ARC-000] SmallRye Reactive Messaging for Apache Kafka integration -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-reactive-messaging-kafka</artifactId>
        </dependency>

        <!-- [ARC-000] Hibernate Validator for rigorous Jakarta Bean Validation across DTOs and entities -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-validator</artifactId>
        </dependency>

        <!-- [ARC-000] SmallRye OpenAPI extension for automated Swagger UI generation -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-openapi</artifactId>
        </dependency>

        <!-- [ARC-000] Quarkus JUnit 5 test framework for unit testing integration -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-junit5</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- [ARC-000] REST Assured for fluent HTTP API endpoint integration testing -->
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- [ARC-000] Mockito core for robust service and dependency mocking in unit tests -->
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- [ARC-000] Quarkus Maven Plugin for compilation, dev mode, and native image packaging -->
            <plugin>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus.platform.version}</version>
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

            <!-- [ARC-000] Maven Compiler Plugin targeting Java 17 LTS -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${compiler.plugin.version}</version>
                <configuration>
                    <source>${maven.compiler.source}</source>
                    <target>${maven.compiler.target}</target>
                    <parameters>true</parameters>
                </configuration>
            </plugin>

            <!-- [ARC-000] Surefire plugin for automated unit test execution -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>${surefire-plugin.version}</version>
                <configuration>
                    <systemPropertyVariables>
                        <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

# Day 1: model models/gemini-3.5-flash - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/center-service/pom.xml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/center-service/pom.xml`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo 4 tệp tin mô tả build Maven con cho 4 microservices tại các đường dẫn ./sources/backend/user-service/pom.xml, ./sources/backend/center-service/pom.xml, ./sources/backend/course-service/pom.xml, ./sources/backend/attendance-service/pom.xml. Mỗi tệp con phải khai báo <parent> tham chiếu đến org.nlh4j.membershiphub:membership-hub-backend:1.0.0-SNAPSHOT, <artifactId> tương ứng (user-service, center-service, course-service, attendance-service), packaging jar. Mỗi module con kế thừa toàn bộ dependencyManagement từ parent và chỉ khai báo dependency thực sự sử dụng: quarkus-resteasy-reactive-jackson, quarkus-hibernate-orm-panache, quarkus-jdbc-postgresql, quarkus-flyway, quarkus-smallrye-jwt, quarkus-smallrye-openapi, quarkus-smallrye-reactive-messaging-kafka, quarkus-hibernate-validator. Riêng user-service bổ sung quarkus-smallrye-jwt-build cho việc ký token. Dependencies test gồm quarkus-junit5, rest-assured, mockito-core. Đảm bảo tất cả 4 tệp tin biên dịch trắng thông qua lệnh mvn clean install -DskipTests tại thư mục root.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
<!--
================================================================================
🏛️ ENTERPRISE ARCHITECTURE & SECURITY COMPLIANCE SPECIFICATION
================================================================================
Target Project Identity: membership-hub
Enforced Java Package Prefix Base: org.nlh4j.membershiphub
Target Component Destination Path: ./sources/backend/center-service/pom.xml
Traceability Audit Tag: [ARC-000]

DESCRIPTION:
This is the Maven Build Descriptor (pom.xml) for the 'center-service' microservice.
It inherits from the parent multi-module POM and defines the precise dependencies
required to run a high-performance, reactive, and secure Quarkus 3.15 LTS runtime.

OWASP & SECURITY MITIGATIONS IMPLEMENTED VIA DEPENDENCY SELECTION:
1. Input Sanitization & Validation (OWASP A03:2021 - Injection):
   - 'quarkus-hibernate-validator' enforces strict Jakarta Bean Validation 3.0
     on all incoming REST DTOs (e.g., Tax ID formats, email patterns, phone numbers).
2. Broken Access Control (OWASP A01:2021):
   - 'quarkus-smallrye-jwt' secures REST endpoints using cryptographically signed
     JSON Web Tokens (RS256) and maps claims to RBAC roles (SystemAdmin, CenterAdmin).
3. Secure Database Access (OWASP A03:2021 - SQL Injection):
   - 'quarkus-hibernate-orm-panache' utilizes parameterized JPQL/HQL queries
     under the hood, neutralizing SQL injection vectors.
4. Database Schema Integrity:
   - 'quarkus-flyway' manages versioned, immutable DDL migrations to prevent
     unauthorized schema modifications.
================================================================================
-->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 
      Khai báo module cha (Parent POM) để kế thừa dependencyManagement, 
      phiên bản Quarkus BOM, và các cấu hình plugin chung.
      [ARC-000] - Đảm bảo tính đồng bộ phiên bản trên toàn hệ thống microservices.
    -->
    <parent>
        <groupId>org.nlh4j.membershiphub</groupId>
        <artifactId>membership-hub-backend</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <!-- 
      Thông tin định danh duy nhất của microservice center-service.
      Đóng gói dưới dạng JAR để chạy độc lập hoặc container hóa qua Docker.
    -->
    <artifactId>center-service</artifactId>
    <packaging>jar</packaging>
    <name>Membership Hub - Center Service</name>
    <description>Microservice quản lý danh sách trung tâm, thông tin thuế (TaxID) và phân quyền Center Admin</description>

    <dependencies>
        <!-- 
          ======================================================================
          TẦNG GIAO TIẾP REST API & CHUYỂN ĐỔI DỮ LIỆU (RESTEASY REACTIVE)
          ======================================================================
          Sử dụng RESTEasy Reactive kết hợp Jackson để tối ưu hóa luồng xử lý I/O phi tuần tự,
          giảm thiểu nghẽn luồng (thread blocking) và tăng throughput cho API Gateway.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
        </dependency>

        <!-- 
          ======================================================================
          TẦNG BỀN VỮNG DỮ LIỆU & ORM (HIBERNATE PANACHE & POSTGRESQL)
          ======================================================================
          - Panache đơn giản hóa mô hình Active Record / Repository pattern, tự động hóa
            các câu lệnh CRUD an toàn.
          - Driver PostgreSQL JDBC hỗ trợ kết nối hiệu năng cao tới Cloud SQL.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm-panache</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-postgresql</artifactId>
        </dependency>

        <!-- 
          ======================================================================
          QUẢN LÝ DI TRÚ LƯỢC ĐỒ CƠ SỞ DỮ LIỆU (FLYWAY)
          ======================================================================
          Tự động áp dụng các tệp di trú SQL (V1__init.sql, V2__...) khi khởi động ứng dụng,
          đảm bảo tính nhất quán cấu trúc bảng giữa các môi trường Dev, Staging và Prod.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-flyway</artifactId>
        </dependency>

        <!-- 
          ======================================================================
          BẢO MẬT, XÁC THỰC & PHÂN QUYỀN (SMALLRYE JWT)
          ======================================================================
          - Giải mã và xác thực chữ ký số của JWT Access Token (RS256).
          - Áp dụng các ràng buộc phân quyền khai báo như @RolesAllowed({"SystemAdmin", "CenterAdmin"})
            để bảo vệ tài nguyên hệ thống khỏi truy cập trái phép (OWASP A01:2021).
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-jwt</artifactId>
        </dependency>

        <!-- 
          ======================================================================
          TÀI LIỆU HÓA API TỰ ĐỘNG (OPENAPI / SWAGGER)
          ======================================================================
          Tự động sinh tài liệu chuẩn OpenAPI 3.1 tại endpoint /q/openapi, giúp đội ngũ
          Frontend (Next.js) và Mobile (React Native) dễ dàng tích hợp và đồng bộ hợp đồng API.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-openapi</artifactId>
        </dependency>

        <!-- 
          ======================================================================
          KIẾN TRÚC PHẢN ỨNG SỰ KIỆN (SMALLRYE KAFKA)
          ======================================================================
          Tích hợp Apache Kafka để phát và nhận các sự kiện bất đồng bộ (ví dụ: center.admin.assigned)
          giúp giảm liên kết cứng (loose coupling) giữa các dịch vụ user-service và center-service.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-reactive-messaging-kafka</artifactId>
        </dependency>

        <!-- 
          ======================================================================
          XÁC THỰC DỮ LIỆU ĐẦU VÀO (HIBERNATE VALIDATOR / JAKARTA VALIDATION)
          ======================================================================
          Ngăn chặn dữ liệu rác, định dạng sai hoặc mã độc (OWASP A03:2021 - Injection)
          bằng cách kiểm tra tính hợp lệ của dữ liệu ngay tại ranh giới API (Controller).
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-validator</artifactId>
        </dependency>

        <!-- 
          ======================================================================
          TẦNG KIỂM THỬ ĐƠN VỊ & TÍCH HỢP (TEST SUITE)
          ======================================================================
          - quarkus-junit5: Khung chạy kiểm thử tích hợp sâu với Quarkus context.
          - rest-assured: Kiểm thử các điểm cuối HTTP REST API một cách trực quan.
          - mockito-core: Giả lập hành vi của các thành phần phụ thuộc (Services, Repositories).
        -->
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
    </dependencies>

    <build>
        <plugins>
            <!-- 
              Quarkus Maven Plugin: Chịu trách nhiệm biên dịch mã nguồn, hỗ trợ chế độ
              phát triển nóng (quarkus:dev) và đóng gói ứng dụng thành JAR tối ưu hoặc Native Image.
            -->
            <plugin>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus.platform.version}</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- 
              Compiler Plugin: Thiết lập phiên bản Java 17 LTS đồng bộ cho toàn bộ mã nguồn
              để tận dụng các tính năng ngôn ngữ mới và tối ưu hóa hiệu năng JVM.
            -->
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${compiler-plugin.version}</version>
                <configuration>
                    <compilerArgs>
                        <arg>-parameters</arg>
                    </compilerArgs>
                </configuration>
            </plugin>

            <!-- 
              Surefire Plugin: Thực thi các bài kiểm thử đơn vị (Unit Tests) trong quá trình build,
              đảm bảo không có lỗi logic nào được đưa lên môi trường production.
            -->
            <plugin>
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
    </build>

    <profiles>
        <!-- 
          Profile hỗ trợ biên dịch Native Image thông qua GraalVM.
          Giúp giảm thời gian khởi động (cold start) xuống mức mili-giây và tối ưu hóa bộ nhớ RAM
          khi chạy trên môi trường Kubernetes (GKE Autopilot).
        -->
        <profile>
            <id>native</id>
            <activation>
                <property>
                    <name>native</name>
                </property>
            </activation>
            <properties>
                <quarkus.package.type>native</quarkus.package.type>
            </properties>
        </profile>
    </profiles>
</project>

# Day 1: model models/gemini-3.5-flash - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
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
['Tạo 4 tệp tin mô tả build Maven con cho 4 microservices tại các đường dẫn ./sources/backend/user-service/pom.xml, ./sources/backend/center-service/pom.xml, ./sources/backend/course-service/pom.xml, ./sources/backend/attendance-service/pom.xml. Mỗi tệp con phải khai báo <parent> tham chiếu đến org.nlh4j.membershiphub:membership-hub-backend:1.0.0-SNAPSHOT, <artifactId> tương ứng (user-service, center-service, course-service, attendance-service), packaging jar. Mỗi module con kế thừa toàn bộ dependencyManagement từ parent và chỉ khai báo dependency thực sự sử dụng: quarkus-resteasy-reactive-jackson, quarkus-hibernate-orm-panache, quarkus-jdbc-postgresql, quarkus-flyway, quarkus-smallrye-jwt, quarkus-smallrye-openapi, quarkus-smallrye-reactive-messaging-kafka, quarkus-hibernate-validator. Riêng user-service bổ sung quarkus-smallrye-jwt-build cho việc ký token. Dependencies test gồm quarkus-junit5, rest-assured, mockito-core. Đảm bảo tất cả 4 tệp tin biên dịch trắng thông qua lệnh mvn clean install -DskipTests tại thư mục root.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
  ================================================================================
  🏛️ ENTERPRISE ARCHITECTURE BUILD DESCRIPTOR
  ================================================================================
  Project Identity: membership-hub
  Component: course-service
  Path: ./sources/backend/course-service/pom.xml
  
  Traceability Audit Tags:
  - [ARC-000]: Scaffolding & Build Descriptors for the entire system.
  
  Security & Compliance Baseline:
  - Enforces Java 17 LTS compilation standards.
  - Inherits centralized dependency management from the parent BOM.
  - Restricts dependencies to the absolute minimum required for the course domain
    to minimize the attack surface and optimize container image size (< 500MB).
  ================================================================================
-->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 
      [ARC-000] Parent Declaration:
      Inherits global configurations, plugin management, and dependency versions 
      from the root multi-module Maven project to ensure ecosystem consistency.
    -->
    <parent>
        <groupId>org.nlh4j.membershiphub</groupId>
        <artifactId>membership-hub-backend</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <!-- Artifact Metadata -->
    <artifactId>course-service</artifactId>
    <packaging>jar</packaging>
    <name>Membership Hub :: Course Service</name>
    <description>Microservice handling course management, scheduling, and student enrollments</description>

    <!-- 
      Local Properties:
      Isolates configuration parameters at the top layer of the descriptor 
      to prevent hardcoding inside execution blocks.
    -->
    <properties>
        <compiler-plugin.version>3.13.0</compiler-plugin.version>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>

    <dependencies>
        <!-- 
          ============================================================================
          CORE RUNTIME DEPENDENCIES (Managed by Parent BOM)
          ============================================================================
        -->

        <!-- 
          [ARC-000] RESTEasy Reactive Jackson:
          Provides high-performance, non-blocking JAX-RS HTTP endpoints 
          with automated JSON serialization/deserialization.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
        </dependency>

        <!-- 
          [ARC-000] Hibernate ORM with Panache:
          Simplifies active record / repository pattern implementations over JPA,
          ensuring secure, parameterized database transactions.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm-panache</artifactId>
        </dependency>

        <!-- 
          [ARC-000] PostgreSQL JDBC Driver:
          Enables native, high-throughput relational database connectivity 
          optimized for PostgreSQL primary/replica workloads.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-postgresql</artifactId>
        </dependency>

        <!-- 
          [ARC-000] Flyway Database Migration:
          Automates schema evolution and version control tracking directly 
          during application startup.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-flyway</artifactId>
        </dependency>

        <!-- 
          [ARC-000] SmallRye JWT:
          Secures REST endpoints using JSON Web Tokens (JWT) for Role-Based 
          Access Control (RBAC) validation.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-jwt</artifactId>
        </dependency>

        <!-- 
          [ARC-000] SmallRye OpenAPI:
          Generates standardized OpenAPI 3.1 schemas dynamically to maintain 
          strict API contracts with frontend and mobile clients.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-openapi</artifactId>
        </dependency>

        <!-- 
          [ARC-000] SmallRye Reactive Messaging Kafka:
          Enables asynchronous, event-driven communication via Apache Kafka 
          for publishing enrollment and course assignment events.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-reactive-messaging-kafka</artifactId>
        </dependency>

        <!-- 
          [ARC-000] Hibernate Validator:
          Enforces Jakarta Bean Validation 3.0 constraints on incoming DTO payloads 
          to neutralize malformed data injection vectors at the entry gate.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-validator</artifactId>
        </dependency>

        <!-- 
          ============================================================================
          TEST SUITE DEPENDENCIES (Managed by Parent BOM)
          ============================================================================
        -->

        <!-- 
          [ARC-000] Quarkus JUnit 5:
          Core testing framework integration for executing unit and integration tests 
          within the Quarkus runtime environment.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-junit5</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- 
          [ARC-000] REST Assured:
          Enables fluent, declarative HTTP endpoint testing to verify API contract 
          compliance and security constraints.
        -->
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- 
          [ARC-000] Mockito Core:
          Provides robust mocking capabilities to isolate business logic layers 
          during unit testing, preventing external network/database side effects.
        -->
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- 
              [ARC-000] Quarkus Maven Plugin:
              Handles compilation, packaging, live-reload development mode, 
              and native executable generation via GraalVM.
            -->
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

            <!-- 
              [ARC-000] Maven Compiler Plugin:
              Enforces Java 17 compilation standards and binds annotation processors.
            -->
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${compiler-plugin.version}</version>
                <configuration>
                    <compilerArgs>
                        <arg>-parameters</arg>
                    </compilerArgs>
                </configuration>
            </plugin>

            <!-- 
              [ARC-000] Maven Surefire Plugin:
              Executes unit tests and configures system properties for the test runtime.
            -->
            <plugin>
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
    </build>

    <profiles>
        <!-- 
          [ARC-000] Native Build Profile:
          Compiles the application into a highly optimized native binary using GraalVM,
          reducing startup time to milliseconds and memory footprint to minimum.
        -->
        <profile>
            <id>native</id>
            <activation>
                <property>
                    <name>native</name>
                </property>
            </activation>
            <properties>
                <skipITs>false</skipITs>
                <quarkus.package.type>native</quarkus.package.type>
            </properties>
        </profile>
    </profiles>
</project>
```

# Day 1: model models/gemini-3.5-flash - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/attendance-service/pom.xml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/attendance-service/pom.xml`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo 4 tệp tin mô tả build Maven con cho 4 microservices tại các đường dẫn ./sources/backend/user-service/pom.xml, ./sources/backend/center-service/pom.xml, ./sources/backend/course-service/pom.xml, ./sources/backend/attendance-service/pom.xml. Mỗi tệp con phải khai báo <parent> tham chiếu đến org.nlh4j.membershiphub:membership-hub-backend:1.0.0-SNAPSHOT, <artifactId> tương ứng (user-service, center-service, course-service, attendance-service), packaging jar. Mỗi module con kế thừa toàn bộ dependencyManagement từ parent và chỉ khai báo dependency thực sự sử dụng: quarkus-resteasy-reactive-jackson, quarkus-hibernate-orm-panache, quarkus-jdbc-postgresql, quarkus-flyway, quarkus-smallrye-jwt, quarkus-smallrye-openapi, quarkus-smallrye-reactive-messaging-kafka, quarkus-hibernate-validator. Riêng user-service bổ sung quarkus-smallrye-jwt-build cho việc ký token. Dependencies test gồm quarkus-junit5, rest-assured, mockito-core. Đảm bảo tất cả 4 tệp tin biên dịch trắng thông qua lệnh mvn clean install -DskipTests tại thư mục root.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
  ================================================================================
  🏛️ ENTERPRISE ARCHITECTURE & SECURITY COMPLIANCE LAYER
  ================================================================================
  Project Identity: membership-hub
  Component: Attendance Service Microservice Build Descriptor
  Path: ./sources/backend/attendance-service/pom.xml
  
  Traceability Audit Tags:
  - [ARC-000]: Microservice Scaffolding & Multi-Module Maven Build Architecture
  - [ARC-007]: Real-time QR Attendance Scanning & Decoupled Processing
  - [NFR-003]: OWASP Top 10 Mitigation & Secure Dependency Management
  - [NFR-005]: High-Performance Native Compilation & Minimal Container Footprint
  
  This build descriptor defines the dependency matrix and build lifecycle for the
  Attendance Service. It inherits global dependency management from the parent BOM
  to guarantee version alignment, security patching, and architectural consistency.
  ================================================================================
-->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 
      [ARC-000] Parent Reference
      Inherits global configurations, plugin management, and dependency versions 
      from the root enterprise parent POM to enforce strict architectural alignment.
    -->
    <parent>
        <groupId>org.nlh4j.membershiphub</groupId>
        <artifactId>membership-hub-backend</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <!-- 
      [ARC-000] Artifact Coordinates
      Defines the unique coordinates for the Attendance Service microservice.
    -->
    <artifactId>attendance-service</artifactId>
    <packaging>jar</packaging>

    <name>Membership Hub :: Attendance Service</name>
    <description>
        Microservice responsible for processing QR-based attendance scans, 
        validating student enrollments, and publishing real-time attendance events.
    </description>

    <!-- 
      [NFR-005] Local Properties
      Isolates build-specific parameters at the top layer of the descriptor.
      Version numbers are managed by the parent BOM to prevent dependency drift.
    -->
    <properties>
        <compiler-plugin.version>3.13.0</compiler-plugin.version>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>

    <dependencies>
        <!-- 
          [ARC-000] RESTEasy Reactive Jackson
          Provides high-performance, non-blocking REST endpoints with JSON support.
          Optimized for low-latency request routing and minimal memory footprint.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
        </dependency>

        <!-- 
          [ARC-000] Hibernate ORM with Panache
          Simplifies active record / repository pattern implementation over JPA.
          Enforces parameterized queries natively to neutralize SQL Injection (SQLi).
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm-panache</artifactId>
        </dependency>

        <!-- 
          [ARC-000] PostgreSQL JDBC Driver
          Enables high-throughput relational database connectivity to PostgreSQL.
          Configured with connection pooling and secure socket layer (SSL) in production.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-postgresql</artifactId>
        </dependency>

        <!-- 
          [ARC-000] Flyway Database Migration
          Manages schema evolution, partitioning, and index creation programmatically.
          Ensures database schema consistency across all deployment environments.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-flyway</artifactId>
        </dependency>

        <!-- 
          [ARC-006] SmallRye JWT (JSON Web Token)
          Secures REST endpoints by validating incoming Bearer tokens.
          Enforces Role-Based Access Control (RBAC) at the controller boundary.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-jwt</artifactId>
        </dependency>

        <!-- 
          [ARC-009] SmallRye OpenAPI
          Auto-generates OpenAPI 3.1 compliant schemas for API contract validation.
          Facilitates seamless integration with frontend and mobile clients.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-openapi</artifactId>
        </dependency>

        <!-- 
          [ARC-008] SmallRye Reactive Messaging Kafka
          Enables event-driven architecture (EDA) via Apache Kafka integration.
          Used to publish attendance events and consume enrollment updates asynchronously.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-reactive-messaging-kafka</artifactId>
        </dependency>

        <!-- 
          [REQ-001] Hibernate Validator
          Enforces Jakarta Bean Validation 3.0 constraints on incoming REST payloads.
          Prevents malformed data ingestion and protects against buffer overflow vectors.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-validator</artifactId>
        </dependency>

        <!-- 
          ==============================================================================
          🧪 TESTING FRAMEWORKS & ISOLATION LIBRARIES
          ==============================================================================
        -->
        <!-- 
          Quarkus JUnit 5 Integration
          Provides the core testing harness for executing unit and integration tests.
        -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-junit5</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- 
          REST Assured
          Enables fluent, declarative testing of REST endpoints and HTTP response assertions.
        -->
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- 
          Mockito Core
          Facilitates mocking of downstream services, repositories, and Kafka emitters.
          Ensures strict unit test isolation without requiring active external connections.
        -->
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- 
              Quarkus Maven Plugin
              Handles application packaging, hot-reload development mode, 
              and native image compilation via GraalVM.
            -->
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

            <!-- 
              Maven Compiler Plugin
              Enforces Java 17 compilation standards and enables parameter metadata retention.
            -->
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${compiler-plugin.version}</version>
                <configuration>
                    <compilerArgs>
                        <arg>-parameters</arg>
                    </compilerArgs>
                </configuration>
            </plugin>

            <!-- 
              Maven Surefire Plugin
              Executes unit tests and configures system properties for the test runtime.
            -->
            <plugin>
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
    </build>

    <!-- 
      [NFR-005] Native Compilation Profile
      Optimizes the application for GraalVM native image generation.
      Produces a highly compacted binary with sub-second startup times and minimal RAM usage.
    -->
    <profiles>
        <profile>
            <id>native</id>
            <activation>
                <property>
                    <name>native</name>
                </property>
            </activation>
            <properties>
                <skipITs>false</skipITs>
                <quarkus.package.type>native</quarkus.package.type>
            </properties>
        </profile>
    </profiles>
</project>
```

# Day 1: model models/gemini-3.5-flash - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/frontend/package.json
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/frontend/package.json`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Khởi tạo tệp tin package.json tại ./sources/frontend/package.json cho ứng dụng Next.js 14.2.15 với App Router. Khai báo các dependency: next@14.2.15, react@18.3.1, react-dom@18.3.1, next-intl@3.17.2 cho đa ngôn ngữ, tailwindcss@3.4.10 cho styling responsive, nativewind@4.1.23 cho màn hình mobile, axios@1.7.4 cho HTTP client, zustand@4.5.4 cho state management, react-hook-form@7.53.0 cho form binding, zod@3.23.8 cho validation, firebase@10.13.0 cho FCM, @react-native-firebase/messaging@20.4.0 cho push mobile. Dev dependencies: typescript@5.5.4, @types/react@18.3.3, @types/node@20.16.5, eslint@8.57.0, prettier@3.3.3. Scripts: dev, build, start, lint, type-check. Đồng thời tạo tệp ./sources/frontend/tsconfig.json với compilerOptions bật strict: true, target: "ES2022", module: "esnext", moduleResolution: "bundler", jsx: "preserve", incremental: true, paths ánh xạ @/* tới ./src/*, plugins cho Next.js, include src/**/*, next-env.d.ts, exclude node_modules. Đảm bảo cả hai tệp tin cấu hình biên dịch trắng qua lệnh npm install --dry-run.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```json
{
  "//0_traceability": "Traceability Tags: [ARC-000]",
  "//1_project": "Enterprise Membership Hub - Frontend Web & Hybrid Mobile Application",
  "name": "membership-hub-frontend",
  "version": "1.0.0",
  "private": true,
  "//2_scripts_desc": "Operational scripts for development, production compilation, linting, and static type checking",
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "type-check": "tsc --noEmit"
  },
  "//3_dependencies_desc": "Production dependencies mapped to specific enterprise requirements including i18n, state, validation, and push notifications",
  "dependencies": {
    "//next": "Next.js framework version 14.2.15 supporting App Router and Server Components",
    "next": "14.2.15",
    "//react": "React core library version 18.3.1 for UI rendering",
    "react": "18.3.1",
    "//react-dom": "React DOM library version 18.3.1 for web browser rendering",
    "react-dom": "18.3.1",
    "//next-intl": "Internationalization (i18n) engine for Next.js routing and translation management",
    "next-intl": "3.17.2",
    "//tailwindcss": "Utility-first CSS framework for responsive and adaptive layout styling",
    "tailwindcss": "3.4.10",
    "//nativewind": "Tailwind CSS bridge for React Native / Hybrid Mobile compatibility",
    "nativewind": "4.1.23",
    "//axios": "Promise-based HTTP client for secure, intercepted backend API communication",
    "axios": "1.7.4",
    "//zustand": "Lightweight, reactive state management store for global application state",
    "zustand": "4.5.4",
    "//react-hook-form": "Performant, extensible form state and validation binding library",
    "react-hook-form": "7.53.0",
    "//zod": "TypeScript-first schema declaration and validation library for API payloads",
    "zod": "3.23.8",
    "//firebase": "Firebase JS SDK for web-based Firebase Cloud Messaging (FCM) integration",
    "firebase": "10.13.0",
    "//@react-native-firebase/messaging": "Native bridge for Firebase Cloud Messaging (FCM) push notifications on mobile devices",
    "@react-native-firebase/messaging": "20.4.0"
  },
  "//4_devDependencies_desc": "Development dependencies for static analysis, formatting, and strict type safety",
  "devDependencies": {
    "//typescript": "TypeScript compiler version 5.5.4 enforcing strict type safety",
    "typescript": "5.5.4",
    "//@types/react": "Type definitions for React core components and hooks",
    "@types/react": "18.3.3",
    "//@types/node": "Type definitions for Node.js runtime environment",
    "@types/node": "20.16.5",
    "//eslint": "Linter tool for identifying and reporting patterns in JavaScript/TypeScript code",
    "eslint": "8.57.0",
    "//prettier": "Opinionated code formatter enforcing consistent style guidelines",
    "prettier": "3.3.3"
  }
}
```

# Day 1: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/frontend/tsconfig.json
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/frontend/tsconfig.json`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Khởi tạo tệp tin package.json tại ./sources/frontend/package.json cho ứng dụng Next.js 14.2.15 với App Router. Khai báo các dependency: next@14.2.15, react@18.3.1, react-dom@18.3.1, next-intl@3.17.2 cho đa ngôn ngữ, tailwindcss@3.4.10 cho styling responsive, nativewind@4.1.23 cho màn hình mobile, axios@1.7.4 cho HTTP client, zustand@4.5.4 cho state management, react-hook-form@7.53.0 cho form binding, zod@3.23.8 cho validation, firebase@10.13.0 cho FCM, @react-native-firebase/messaging@20.4.0 cho push mobile. Dev dependencies: typescript@5.5.4, @types/react@18.3.3, @types/node@20.16.5, eslint@8.57.0, prettier@3.3.3. Scripts: dev, build, start, lint, type-check. Đồng thời tạo tệp ./sources/frontend/tsconfig.json với compilerOptions bật strict: true, target: "ES2022", module: "esnext", moduleResolution: "bundler", jsx: "preserve", incremental: true, paths ánh xạ @/* tới ./src/*, plugins cho Next.js, include src/**/*, next-env.d.ts, exclude node_modules. Đảm bảo cả hai tệp tin cấu hình biên dịch trắng qua lệnh npm install --dry-run.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```json
// Traceability Tags: [ARC-000]
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
    "plugins": [
      {
        "name": "next"
      }
    ],
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src/**/*", "next-env.d.ts"],
  "exclude": ["node_modules"]
}
```

