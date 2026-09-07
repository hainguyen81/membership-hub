<!--
  [ARC-000], [REQ-012]
  ====================================================================================
  FILE: ./sources/backend/attendance-service/pom.xml
  SCOPE: Maven build descriptor for attendance-service microservice
  TRACEABILITY: [ARC-000] (System Architecture), [REQ-012] (QR Attendance Scan)
  DESCRIPTION: Multi‑module Maven pom for attendance-service with Quarkus 3.15.1 runtime,
              Hibernate ORM Panache, Reactive Messaging Kafka, Flyway migrations,
              and comprehensive test infrastructure using Testcontainers.
  ====================================================================================
-->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <!-- Inherit the enterprise‑wide parent pom that defines common dependencies,
       Quarkus version, Java toolchain, and shared configuration. -->
  <parent>
    <groupId>org.nlh4j.membershiphub</groupId>
    <artifactId>membership-hub-backend</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <!-- This module represents the attendance‑service microservice. -->
  <artifactId>attendance-service</artifactId>
  <packaging>jar</packaging>

  <dependencies>
    <!-- ---------------------------------------------------------------------- -->
    <!-- RUNTIME DEPENDENCIES – Core Quarkus extensions for REST, DB, messaging, -->
    <!-- validation, caching, and OpenAPI generation. -->
    <!-- ---------------------------------------------------------------------- -->
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

    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-cache</artifactId>
    </dependency>

    <!-- ---------------------------------------------------------------------- -->
    <!-- TEST DEPENDENCIES – Unit & integration tests with Testcontainers for -->
    <!-- PostgreSQL and Kafka. -->
    <!-- ---------------------------------------------------------------------- -->
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
      <!-- Quarkus Maven plugin – responsible for building the native / jar -->
      <!-- Quarkus application and managing the generated resources. -->
      <plugin>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-maven-plugin</artifactId>
        <version>3.15.1</version>
        <executions>
          <execution>
            <goals>
              <goal>build</goal>
            </goals>
          </execution>
        </executions>
      </plugin>

      <!-- Standard Maven compiler plugin – enforces Java 17 LTS as the -->
      <!-- language level for the entire module. -->
      <plugin>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.13.0</version>
        <configuration>
          <source>17</source>
          <target>17</target>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>