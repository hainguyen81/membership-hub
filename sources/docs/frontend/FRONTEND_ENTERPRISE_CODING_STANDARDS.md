```markdown
# 🏛️ FRONTEND ENTERPRISE CODING STANDARDS
*(Conceptual Init: Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)*

## 📊 1. TỔNG QUAN KIẾN TRÚC Scaffolding
- **Sơ Đồ Cây Thư Mục Đa Module Maven:** Dự án Membership Hub sử dụng cấu trúc đa module Maven với root `./sources/backend/pom.xml` quản lý 4 microservices backend chính: `user-service`, `center-service`, `course-service`, `attendance-service`.
- **Quy Ước Đặt Tên Gói:** Toàn bộ mã nguồn Java sử dụng quy ước gói `org.nlh4j.membershiphub.<service-name>`.
- **Version Dependencies:** 
  * Quarkus 3.15.1
  * Java 17 LTS
  * Spring Boot 2.7.3 (if applicable)
  * Hibernate ORM 3.15.1
  * Flyway 10.10.0
  * PostgreSQL JDBC driver 42.7.3

## 🌐 1.1. FRONTEND ARCHITECTURE
- **Next.js 14.2.15:** Sử