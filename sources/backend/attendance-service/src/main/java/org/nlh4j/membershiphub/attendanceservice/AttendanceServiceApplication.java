<!-- Traceability Tags: [ARC-000], [REQ-012] -->
<project>
  <modelVersion>4.0.0</modelVersion>

  <!-- Parent: Membership Hub Backend (Multi‑Module Maven) -->
  <parent>
    <groupId>org.nlh4j.membershiphub</groupId>
    <artifactId>membership-hub-backend</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>attendance-service</artifactId>
  <packaging>jar</packaging>
  <name>attendance-service</name>
  <description>Attendance Service for Membership Hub – QR scan, idempotency, Kafka integration</description>

  <properties>
    <java.version>17</java.version>
    <quarkus.platform.version>3.15.1</quarkus.platform.version>
    <maven.compiler.source>${java.version}</maven.compiler.source>
    <maven.compiler.target>${java.version}</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <quarkus.package.type>jar</quarkus.package.type>
    <!-- Production hardening -->
    <quarkus.banner.enabled>false</quarkus.banner.enabled>
    <quarkus.http.port>8080</quarkus.http.port>
    <quarkus.http.host>0.0.0.0</quarkus.http.host>
  </properties>

  <dependencyManagement>
    <dependencies>
      <!-- Quarkus BOM – ensures consistent version across all Quarkus artifacts -->
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
    <!-- Core Quarkus runtime -->
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

    <!-- Test & Integration -->
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
      <!-- Quarkus Maven Plugin – builds the native / jar package -->
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

      <!-- Standard Maven Compiler -->
      <plugin>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.13.0</version>
        <configuration>
          <source>${java.version}</source>
          <target>${java.version}</target>
        </configuration>
      </plugin>

      <!-- Maven Surefire for unit tests -->
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