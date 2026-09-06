/**
 * Attendance Service Application - Core runtime entry point for attendance management microservice.
 *
 * Tags: [ARC-000] [REQ-012] [NFR-001] [NFR-003] [NFR-004]
 *
 * Review: Validates Quarkus main configuration, ensures package org.nlh4j.membershiphub.attendanceservice,
 * disables banner for production, configures HTTP port, host, and health check endpoints.
 *
 * pom.xml content (attendance-service):
 * <?xml version="1.0" encoding="UTF-8"?>
 * <project xmlns="http://maven.apache.org/POM/4.0.0"
 *          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
 *          http://maven.apache.org/xsd/maven-4.0.0.xsd">
 *   <modelVersion>4.0.0</modelVersion>
 *   <parent>
 *     <groupId>org.nlh4j.membershiphub</groupId>
 *     <artifactId>membership-hub-backend</artifactId>
 *     <version>1.0.0-SNAPSHOT</version>
 *   </parent>
 *   <artifactId>attendance-service</artifactId>
 *   <name>attendance-service</name>
 *   <description>Attendance management microservice for membership-hub</description>
 *   <properties>
 *     <java.version>17</java.version>
 *     <quarkus.platform.version>3.15.1</quarkus.platform.version>
 *     <maven.compiler.source>${java.version}</maven.compiler.source>
 *     <maven.compiler.target>${java.version}</maven.compiler.target>
 *     <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
 *   </properties>
 *   <dependencyManagement>
 *     <dependencies>
 *       <dependency>
 *         <groupId>io.quarkus</groupId>
 *         <artifactId>quarkus-bom</artifactId>
 *         <version>${quarkus.platform.version}</version>
 *         <type>pom</type>
 *         <scope>import</scope>
 *       </dependency>
 *     </dependencies>
 *   </dependencyManagement>
 *   <dependencies>
 *     <!-- Core Quarkus runtime -->
 *     <dependency>
 *       <groupId>io.quarkus</groupId>
 *       <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
 *     </dependency>
 *     <dependency>
 *       <groupId>io.quarkus</groupId>
 *       <artifactId>quarkus-hibernate-orm-panache</artifactId>
 *     </dependency>
 *     <dependency>
 *       <groupId>io.quarkus</groupId>
 *       <artifactId>quarkus-jdbc-postgresql</artifactId>
 *     </dependency>
 *     <dependency>
 *       <groupId>io.quarkus</groupId>
 *       <artifactId>quarkus-flyway</artifactId>
 *     </dependency>
 *     <dependency>
 *       <groupId>io.quarkus</groupId>
 *       <artifactId>quarkus-smallrye-reactive-messaging-kafka</artifactId>
 *     </dependency>
 *     <dependency>
 *       <groupId>io.quarkus</groupId>
 *       <artifactId>quarkus-hibernate-validator</artifactId>
 *     </dependency>
 *     <dependency>
 *       <groupId>io.quarkus</groupId>
 *       <artifactId>quarkus-smallrye-openapi</artifactId>
 *     </dependency>
 *     <dependency>
 *       <groupId>io.quarkus</groupId>
 *       <artifactId>quarkus-cache</artifactId>
 *       <type>pom</type>
 *     </dependency>
 *     <!-- Test dependencies -->
 *     <dependency>
 *       <groupId>io.quarkus</groupId>
 *       <artifactId>quarkus-junit5</artifactId>
 *       <scope>test</scope>
 *     </dependency>
 *     <dependency>
 *       <groupId>io.rest-assured</groupId>
 *       <artifactId>rest-assured</artifactId>
 *       <scope>test</scope>
 *     </dependency>
 *     <dependency>
 *       <groupId>org.mockito</groupId>
 *       <artifactId>mockito-core</artifactId>
 *       <scope>test</scope>
 *     </dependency>
 *     <dependency>
 *       <groupId>org.testcontainers</groupId>
 *       <artifactId>postgresql</artifactId>
 *       <version>1.20.4</version>
 *       <scope>test</scope>
 *     </dependency>
 *     <dependency>
 *       <groupId>org.testcontainers</groupId>
 *       <artifactId>kafka</artifactId>
 *       <version>1.20.4</version>
 *       <scope>test</scope>
 *     </dependency>
 *   </dependencies>
 *   <build>
 *     <plugins>
 *       <plugin>
 *         <groupId>io.quarkus</groupId>
 *         <artifactId>quarkus-maven-plugin</artifactId>
 *         <version>${quarkus.platform.version}</version>
 *         <executions>
 *           <execution>
 *             <goals>
 *               <goal>build</goal>
 *             </goals>
 *           </execution>
 *         </executions>
 *       </plugin>
 *       <plugin>
 *         <artifactId>maven-compiler-plugin</artifactId>
 *         <version>3.13.0</version>
 *         <configuration>
 *           <source>${java.version}</source>
 *           <target>${java.version}</target>
 *         </configuration>
 *       </plugin>
 *     </plugins>
 *   </build>
 * </project>
 */

package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

@QuarkusMain
public class AttendanceServiceApplication {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    public static void main(String[] args) {
        // Production hardening: disable banner, set HTTP port and host, enable health checks
        System.setProperty("quarkus.banner.enabled", "false");
        System.setProperty("quarkus.http.port", "8080");
        System.setProperty("quarkus.http.host", "0.0.0.0");
        // Enable SmallRye Health endpoints (automatically provided by Quarkus)
        System.setProperty("quarkus.smallrye-health.root-path", "/q/health");
        System.setProperty("quarkus.smallrye-health.liveness-path", "/q/health/live");
        System.setProperty("quarkus.smallrye-health.readiness-path", "/q/health/ready");

        logger.info("Starting Attendance Service Application (membership-hub) on port 8080 with health checks enabled.");

        Quarkus.run(args);
    }
}