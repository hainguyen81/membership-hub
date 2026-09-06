<!-- [ARC-000], [REQ-012] -->
<!-- ==================================================================================== -->
<!-- FILE: ./sources/backend/attendance-service/pom.xml -->
<!-- SCOPE: Attendance Service - Maven Multi‑Module Build Descriptor -->
<!-- TRACEABILITY: [ARC-000] (System Architecture), [REQ-012] (QR Attendance Scan) -->
<!-- DESCRIPTION: Maven POM for the attendance‑service microservice. Inherits the -->
<!--              parent project org.nlh4j.membershiphub:membership‑hub‑backend:1.0.0‑SNAPSHOT. -->
<!--              Declares Quarkus 3.15.1 core and test dependencies, including Kafka, -->
<!--              PostgreSQL, Flyway, and Testcontainers for integration testing. -->
<!-- ==================================================================================== -->

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <!-- ------------------------------------------------------------------------------ -->
    <!-- PARENT PROJECT – Shared BOM, dependency management, and common plugin config -->
    <!-- ------------------------------------------------------------------------------ -->
    <parent>
        <groupId>org.nlh4j.membershiphub</groupId>
        <artifactId>membership-hub-backend</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <!-- ------------------------------------------------------------------------------ -->
    <!-- PROJECT METADATA -->
    <!-- ------------------------------------------------------------------------------ -->
    <artifactId>attendance-service</artifactId>
    <name>attendance-service</name>
    <description>Microservice for attendance tracking, QR scan processing, and notification dispatch</description>

    <properties>
        <!-- Enforce Java 17 LTS for compatibility with Quarkus 3.15.1 -->
        <java.version>17</java.version>
        <quarkus.platform.version>3.15.1</quarkus.platform.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <!-- ------------------------------------------------------------------------------ -->
    <!-- DEPENDENCY MANAGEMENT – Import Quarkus BOM for version alignment -->
    <!-- ------------------------------------------------------------------------------ -->
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

    <!-- ------------------------------------------------------------------------------ -->
    <!-- RUNTIME DEPENDENCIES -->
    <!-- ------------------------------------------------------------------------------ -->
    <dependencies>
        <!-- Core Quarkus -->
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

        <!-- -------------------------------------------------------------------------- -->
        <!-- TEST DEPENDENCIES – Unit & Integration -->
        <!-- -------------------------------------------------------------------------- -->
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

        <!-- Testcontainers for PostgreSQL & Kafka -->
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

    <!-- ------------------------------------------------------------------------------ -->
    <!-- BUILD PLUGINS -->
    <!-- ------------------------------------------------------------------------------ -->
    <build>
        <plugins>
            <!-- Quarkus Maven Plugin – builds the native / jar -->
            <plugin>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus.platform.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Maven Compiler Plugin -->
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>

            <!-- Maven Surefire Plugin -->
            <plugin>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
                <configuration>
                    <systemProperties>
                        <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                    </systemProperties>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>