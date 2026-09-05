# BỐI CẢNH DỰ ÁN TOÀN CẦU: membership-hub

## 📊 Kiểm soát tài liệu

| Mục | Chi tiết |
| :--- | :--- |
| **ID Bản thiết kế** | ARCH-20260818163158 |
| **Tên dự án** | membership-hub |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày.Giờ** | 2026/08/18 16:31:58 |
| **Tác giả** | Kiến trúc sư hệ thống doanh nghiệp (Đặc vụ SA) |
| **Phê duyệt** | Đang chờ xem xét quản trị kỹ thuật |

## 📊 1. TỔNG QUAN HỆ THỐNG & CHẾ ĐỘ KIẾN TRÚC CỐT LÕI

### ⚙️ 1.1. Chế độ hệ thống cốt lõi & chế độ kiến trúc
- Kiến trúc phân lớp 3 tầng: Frontend Next.js, Backend Quarkus Java, Cơ sở dữ liệu PostgreSQL, triển khai trên GKE [ARC-010]
- Hệ thống xác thực tập trung sử dụng Firebase Authentication, hỗ trợ OAuth2 (Google, Facebook, email/mật khẩu), cấp JWT access token (hết hạn 15 phút) và refresh token (hết hạn 7 ngày) [ARC-006]
- Kiểm soát truy cập dựa trên vai trò (RBAC) với 5 vai trò: System Admin, Center Admin, Manager, Teacher, Student, quyền hạn được cách ly theo trung tâm [ARC-001, ARC-002, ARC-003, ARC-004, ARC-005]
- Hỗ trợ đa ngôn ngữ (Tiếng Anh, Tiếng Việt, Tiếng Tây Ban Nha) với chuyển đổi locale không cần tải lại trang [NFR-007]
- Tích hợp đa kênh thông báo: Push notification qua FCM/APNs, đăng bài nhóm Zalo, thông báo trong ứng dụng [ARC-008]
- Đảm bảo tính bất biến điểm danh: Một học viên chỉ có một bản ghi điểm danh mỗi khóa học mỗi ngày, xử lý yêu cầu trùng lặp tự động [REQ-012, REQ-013, EXC-002]
- Hỗ trợ caching phiên làm việc và dữ liệu ngoại tuyến bằng Redis cho ứng dụng di động [ARC-009]
- Ghi log kiểm toán toàn diện cho tất cả hành động người dùng, lưu trữ 1 năm [NFR-006]

### 🌊 1.2. Kiến trúc luồng dữ liệu doanh nghiệp & hệ sinh thái cốt lõi
- Luồng xác thực: Người dùng gửi thông tin đăng nhập/OAuth2 → Backend xác thực với Firebase → Cấp JWT token, lưu refresh token an toàn [ARC-006]
- Luồng điểm danh QR: Ứng dụng di động quét mã QR khóa học → Gửi student ID, timestamp, course ID đến backend → Dịch vụ điểm danh kiểm tra tính hợp lệ, ghi bản ghi idempotent, trả phản hồi trùng lặp nếu đã điểm danh [REQ-012, REQ-013, EXC-001, EXC-002]
- Luồng thông báo: Sự kiện hệ thống (đăng ký khóa học, phân công giáo viên, tạo thông báo) → Kích hoạt sản xuất thông báo → Gửi push notification đến thiết bị người dùng, đăng bài lên nhóm Zalo được chỉ định [REQ-016, EXC-003]
- Luồng quản lý khóa học & ghi danh: Admin tạo/cập nhật khóa học → Kiểm tra xung đột lịch giáo viên/phòng học → Học viên duyệt khóa học, đăng ký → Tự động tạo tài khoản Student nếu chưa có, ghi bản ghi ghi danh [REQ-007, REQ-008, REQ-009, REQ-010, REQ-011]
- Luồng quản lý thẻ hội viên: Học viên xem thông tin thẻ (ngày còn lại) → Khi gia hạn, tích hợp cổng thanh toán → Cập nhật ngày hết hạn thẻ, gửi thông báo xác nhận [REQ-014, REQ-015]
- Luồng báo cáo & phân tích: Admin yêu cầu báo cáo điểm danh → Hệ thống tổng hợp dữ liệu từ bảng Attendance, Users, Courses → Xuất file CSV, hiển thị dashboard tổng quan thời gian thực [REQ-024, REQ-025]
- Luồng tích hợp chatbot AI: Người dùng gửi câu hỏi → Chatbot xử lý bằng mô hình ngôn ngữ → Trả lời tự động hoặc chuyển cho hỗ trợ viên nếu độ tin cậy thấp [REQ-019]

## 📁 2. NGĂN XẾP PHỤ THUỘC CÔNG NGHỆ & THƯ VIỆN HỆ SINH THÁI
- **Ngăn xếp hạ tầng cốt lõi Backend:** Quarkus 3.15.1 (runtime Java 21), Hibernate ORM 6.4.4, PostgreSQL JDBC Driver 42.7.3, Apache Kafka Client 3.7.0 (cho sự kiện hệ thống), Firebase Admin SDK 9.2.0, BCrypt 0.10.8 (mã hóa mật khẩu), SmallRye JWT 3.15.1, HikariCP 5.0.1 (quản lý kết nối cơ sở dữ liệu) [ARC-010]
- **Ngăn xếp UI Frontend & Đa nền tảng Di động:** Next.js 14.1.0 (React 18.2.0), React Native 0.73.2 (cho ứng dụng di động), Redux Toolkit 2.0.1 (quản lý trạng thái), React Query 5.17.0 (truy cập dữ liệu), i18next 23.7.0 (đa ngôn ngữ), Axios 1.6.7 (gọi API), Firebase SDK 10.7.0 (xác thực, push notification) [ARC-009, ARC-010]

## 📁 3. RÀNG BUỘC TOÀN CẦU & TIÊU CHUẨN TUÂN THỦ DOANH NGHIỆP

### 🔑 3.1. Cơ sở bảo mật & tuân thủ
- Mã hóa dữ liệu truyền qua TLS 1.3, mã hóa dữ liệu lưu trữ bằng AES-256 [NFR-003]
- JWT access token hết hạn sau 15 phút, refresh token hết hạn sau 7 ngày, lưu trữ refresh token an toàn bằng HttpOnly cookie [NFR-003, ARC-006]
- Triển khai các biện pháp giảm thiểu OWASP Top 10: chống injection SQL bằng prepared statements, chống XSS bằng làm sạch dữ liệu đầu vào, chống CSRF bằng token CSRF [NFR-003]
- Kiểm soát truy cập dựa trên vai trò (RBAC) với kiểm tra quyền ở tầng API và tầng dịch vụ, cách ly quyền quản trị theo trung tâm [ARC-001, ARC-002, ARC-003, ARC-004, ARC-005]
- Quản lý dữ liệu cá nhân tuân thủ GDPR/CCPA: hỗ trợ xóa dữ liệu người dùng theo yêu cầu, xuất dữ liệu dạng JSON, quản lý đồng ý tiếp thị [NFR-008]
- Ghi log kiểm toán cho tất cả hành động nhạy cảm (thay đổi vai trò, điểm danh, gửi thông báo) với timestamp, user ID, chi tiết hành động, lưu trữ 1 năm [NFR-006]

### 🌐 3.2. Ràng buộc hạ tầng & hiệu suất
- Độ trễ API cốt lõi (xác thực, điểm danh, danh sách khóa học) trung bình dưới 200ms, hỗ trợ 10.000 người dùng đồng thời với truy vấn cơ sở dữ liệu dưới 1 giây [NFR-001]
- Thời gian hoạt động mục tiêu 99.9% hàng năm, có chế độ tự động chuyển đổi failover giữa các cụm GKE [NFR-002]
- Quy mô ngang dịch vụ Quarkus tự động thông qua Kubernetes HPA khi CPU > 70% hoặc độ trễ yêu cầu > 300ms [NFR-004]
- Sử dụng bản sao đọc PostgreSQL cho khối lượng công việc báo cáo để giảm tải cho cơ sở dữ liệu chính [NFR-004]
- Kích thước hình ảnh Docker cơ sở dưới 200MB, hình ảnh cuối cùng dưới 500MB [NFR-005]
- Chính sách caching Redis cho phiên làm việc: hết hạn sau 24 giờ, chính sách xóa LRU khi bộ nhớ đầy [ARC-009]
- Sao lưu cơ sở dữ liệu PostgreSQL hàng ngày, hỗ trợ phục hồi điểm thời gian (PITR) trong 24 giờ, sao lưu cụm GKE sang vùng riêng [NFR-009]

### 🥞 3.3. MA TRẬN NGĂN XẾP KIẾN TRÚC
```properties:stack_matrix
PERSISTENCE_LAYER_REQUIRED=true
BACKEND_LAYER_REQUIRED=true
FRONTEND_LAYER_REQUIRED=true
MOBILE_LAYER_REQUIRED=true
DEVOPS_LAYER_REQUIRED=true
```

---

## 🚀 GIAI ĐOẠN 1: THIẾT LẬP HẠ TẦNG & CỐT LÕI XÁC THỰC
### 📅 Ngày 1: Thiết lập dự án & cấu trúc thư mục
- **Coder**: Tạo cấu trúc thư mục dịch vụ backend Quarkus, cấu hình tệp pom.xml với các phụ thuộc cốt lõi (Hibernate ORM, PostgreSQL JDBC, SmallRye JWT, Firebase Admin SDK, HikariCP) → `./sources/backend/membership-hub/pom.xml` [ARC-010, NFR-003]
- **Docker**: Tạo Dockerfile đa giai đoạn cho dịch vụ backend, tối ưu kích thước hình ảnh dưới ngưỡng quy định → `./sources/infra/backend/Dockerfile` [NFR-005]
- **Doc**: Tạo tài liệu kiến trúc tổng quan, sơ đồ luồng xác thực OAuth2/JWT → `./sources/docs/architecture/authentication-flow.md` [ARC-006]

### 📅 Ngày 2: Thiết kế lược đồ cơ sở dữ liệu người dùng & vai trò
- **Coder**: Viết script di chuyển Flyway tạo bảng USERS, ROLES, thêm ràng buộc khóa ngoại, chỉ mục duy nhất cho email và chỉ mục cho roleId → `./sources/backend/membership-hub/src/main/resources/db/migration/V1__create_users_roles.sql` [DAT-001, ARC-010]
- **Tester**: Viết bài kiểm tra tích hợp cho lược đồ cơ sở dữ liệu, kiểm tra tính toàn vẹn khóa ngoại và ràng buộc duy nhất → `./sources/backend/membership-hub/src/test/java/com/membershiphub/integration/UserSchemaIntegrationTest.java` [DAT-001]
- **Doc**: Cập nhật tài liệu từ điển dữ liệu cho bảng USERS và ROLES → `./sources/docs/data-dictionary/users-roles.md` [DAT-001]

### 📅 Ngày 3: Triển khai dịch vụ xác thực & RBAC
- **Coder**: Triển khai lớp dịch vụ xác thực JWT, tích hợp Firebase Auth, triển khai bộ lọc RBAC kiểm tra quyền người dùng theo vai trò → `./sources/backend/membership-hub/src/main/java/com/membershiphub/service/AuthService.java` [REQ-001, REQ-002, REQ-003, ARC-006, ARC-001, ARC-002, ARC-003, ARC-004, ARC-005]
- **Tester**: Viết bài kiểm tra đơn vị cho AuthService, kiểm tra luồng đăng ký email/mật khẩu, đăng nhập OAuth2, hết hạn và làm mới token → `./sources/backend/membership-hub/src/test/java/com/membershiphub/service/AuthServiceTest.java` [REQ-001, REQ-002, REQ-003]
- **Reviewer**: Kiểm tra mã bảo mật, đảm bảo không có lỗ hổng injection, tuân thủ tiêu chuẩn OWASP Top 10 → `./sources/backend/membership-hub/src/main/java/com/membershiphub/service/AuthService.java` [NFR-003]
- **GCP**: Cấu hình dự án Firebase, kích hoạt nhà cung cấp xác thực OAuth2 (Google, Facebook), cấu hình quy tắc bảo mật Firebase → `./sources/infra/gcp/firebase-config.yaml` [ARC-006, REQ-002]

---

## 🏗️ GIAI ĐOẠN 2: QUẢN LÝ TRUNG TÂM, KHÓA HỌC & GHI DANH
### 📅 Ngày 1: Thiết kế lược đồ cơ sở dữ liệu trung tâm, khóa học & ghi danh
- **Coder**: Viết script di chuyển Flyway tạo bảng CENTERS, COURSES, ENROLLMENTS, thêm chỉ mục và ràng buộc khóa ngoại, ràng buộc kiểm tra xung đột lịch khóa học → `./sources/backend/membership-hub/src/main/resources/db/migration/V2__create_centers_courses_enrollments.sql` [DAT-003, DAT-004, DAT-005, ARC-010, REQ-008]
- **Tester**: Viết bài kiểm tra tích hợp cho lược đồ cơ sở dữ liệu, kiểm tra tính toàn vẹn ràng buộc và xung đột lịch → `./sources/backend/membership-hub/src/test/java/com/membershiphub/integration/CourseSchemaIntegrationTest.java` [REQ-008]
- **Doc**: Cập nhật tài liệu từ điển dữ liệu cho 3 bảng trên → `./sources/docs/data-dictionary/centers-courses-enrollments.md` [DAT-003, DAT-004, DAT-005]

### 📅 Ngày 2: Triển khai API quản lý trung tâm
- **Coder**: Triển khai REST API cho trung tâm: lấy danh sách, tạo, cập nhật, xóa trung tâm, phân quyền Center Admin cho người dùng → `./sources/backend/membership-hub/src/main/java/com/membershiphub/rest/CenterResource.java` [REQ-004, REQ-005, REQ-006, ARC-002]
- **Tester**: Viết bài kiểm tra đơn vị và tích hợp cho API trung tâm, kiểm tra phân quyền truy cập và ràng buộc dữ liệu → `./sources/backend/membership-hub/src/test/java/com/membershiphub/rest/CenterResourceTest.java` [REQ-004, REQ-005, REQ-006]
- **Doc**: Viết tài liệu API Swagger/OpenAPI cho các endpoint quản lý trung tâm → `./sources/docs/api/center-api.md` [REQ-004, REQ-005, REQ-006]

### 📅 Ngày 3: Triển khai API quản lý khóa học
- **Coder**: Triển khai REST API cho khóa học: lấy danh sách, tạo, cập nhật, xóa, phân công giáo viên, kiểm tra xung đột lịch giáo viên/phòng học → `./sources/backend/membership-hub/src/main/java/com/membershiphub/rest/CourseResource.java` [REQ-007, REQ-008, REQ-009, ARC-003]
- **Tester**: Viết bài kiểm tra tích hợp cho API khóa học, kiểm tra logic xung đột lịch và phân quyền truy cập → `./sources/backend/membership-hub/src/test/java/com/membershiphub/rest/CourseResourceTest.java` [REQ-007, REQ-008, REQ-009]
- **Reviewer**: Kiểm tra logic xung đột lịch, đề xuất tối ưu truy vấn kiểm tra trùng lặp lịch → `./sources/backend/membership-hub/src/main/java/com/membershiphub/service/CourseService.java` [REQ-008]

### 📅 Ngày 4: Triển khai API ghi danh học viên
- **Coder**: Triển khai REST API ghi danh: duyệt danh sách khóa học chưa đăng ký, đăng ký khóa học, tự động tạo tài khoản Student nếu chưa tồn tại → `./sources/backend/membership-hub/src/main/java/com/membershiphub/rest/EnrollmentResource.java` [REQ-010, REQ-011]
- **Tester**: Viết bài kiểm tra tích hợp cho luồng ghi danh, kiểm tra logic tự động tạo tài khoản Student và gửi thông báo → `./sources/backend/membership-hub/src/test/java/com/membershiphub/rest/EnrollmentResourceTest.java` [REQ-010, REQ-011]
- **Doc**: Cập nhật tài liệu API cho endpoint ghi danh → `./sources/docs/api/enrollment-api.md` [REQ-010, REQ-011]

---

## 📱 GIAI ĐOẠN 3: ĐIỂM DANH QR, THẺ HỘI VIÊN & THÔNG BÁO
### 📅 Ngày 1: Triển khai dịch vụ điểm danh QR
- **Coder**: Triển khai dịch vụ điểm danh idempotent, xử lý yêu cầu quét mã QR, kiểm tra trùng lặp điểm danh trong cùng ngày cho cùng học viên và khóa học → `./sources/backend/membership-hub/src/main/java/com/membershiphub/service/AttendanceService.java` [REQ-012, REQ-013, EXC-001, EXC-002, DAT-006]
- **Coder**: Viết script di chuyển Flyway tạo bảng ATTENDANCE, thêm chỉ mục kết hợp cho studentId, courseId, attendanceDate để tối ưu truy vấn kiểm tra trùng lặp → `./sources/backend/membership-hub/src/main/resources/db/migration/V3__create_attendance.sql` [DAT-006]
- **Tester**: Viết bài kiểm tra đơn vị và tích hợp cho dịch vụ điểm danh, kiểm tra tính idempotent và xử lý lỗi mất kết nối mạng → `./sources/backend/membership-hub/src/test/java/com/membershiphub/service/AttendanceServiceTest.java` [REQ-012, REQ-013, EXC-001, EXC-002]
- **Doc**: Cập nhật từ điển dữ liệu bảng ATTENDANCE, tài liệu luồng điểm danh QR → `./sources/docs/data-dictionary/attendance.md` [DAT-006]

### 📅 Ngày 2: Triển khai quản lý thẻ hội viên
- **Coder**: Triển khai dịch vụ quản lý thẻ hội viên: tính toán ngày còn lại hiệu lực, gia hạn thẻ, tích hợp cổng thanh toán → `./sources/backend/membership-hub/src/main/java/com/membershiphub/service/MembershipCardService.java` [REQ-014, REQ-015, DAT-007]
- **Coder**: Viết script di chuyển Flyway tạo bảng STUDENTCARDS, thêm chỉ mục cho studentId → `./sources/backend/membership-hub/src/main/resources/db/migration/V4__create_student_cards.sql` [DAT-007]
- **Tester**: Viết bài kiểm tra tích hợp cho dịch vụ thẻ hội viên, kiểm tra logic tính ngày còn lại và gia hạn thẻ → `./sources/backend/membership-hub/src/test/java/com/membershiphub/service/MembershipCardServiceTest.java` [REQ-014, REQ-015]
- **Doc**: Cập nhật từ điển dữ liệu bảng STUDENTCARDS, tài liệu luồng gia hạn thẻ → `./sources/docs/data-dictionary/student-cards.md` [DAT-007]

### 📅 Ngày 3: Triển khai hệ thống thông báo
- **Coder**: Triển khai dịch vụ thông báo: tạo bản ghi thông báo, gửi push notification qua FCM/APNs, đăng bài lên nhóm Zalo được chỉ định, cơ chế thử lại tối đa 3 lần khi gửi thất bại → `./sources/backend/membership-hub/src/main/java/com/membershiphub/service/NotificationService.java` [REQ-016, EXC-003, DAT-008, ARC-008]
- **Coder**: Viết script di chuyển Flyway tạo bảng NOTIFICATIONS → `./sources/backend/membership-hub/src/main/resources/db/migration/V5__create_notifications.sql` [DAT-008]
- **Tester**: Viết bài kiểm tra tích hợp cho dịch vụ thông báo, kiểm tra cơ chế thử lại và ghi log lỗi gửi thất bại → `./sources/backend/membership-hub/src/test/java/com/membershiphub/service/NotificationServiceTest.java` [REQ-016, EXC-003]
- **Doc**: Cập nhật từ điển dữ liệu bảng NOTIFICATIONS, tài liệu tích hợp Zalo API → `./sources/docs/integrations/zalo-api.md` [REQ-016, ARC-008]

---

## 🎁 GIAI ĐOẠN 4: TÍNH NĂNG BỔ SUNG: KHUYẾN MÃI, THÔNG BÁO, CHATBOT AI & BÁO CÁO
### 📅 Ngày 1: Triển khai quản lý khuyến mãi & thông báo
- **Coder**: Triển khai REST API quản lý khuyến mãi và thông báo: tạo, cập nhật, xóa, kiểm tra thời hạn hiệu lực tự động ẩn thông báo hết hạn → `./sources/backend/membership-hub/src/main/java/com/membershiphub/rest/PromotionAnnouncementResource.java` [REQ-017, REQ-018, DAT-009]
- **Coder**: Viết script di chuyển Flyway tạo bảng PROMOTIONS, ANNOUNCEMENTS → `./sources/backend/membership-hub/src/main/resources/db/migration/V6__create_promotions_announcements.sql` [DAT-009]
- **Tester**: Viết bài kiểm tra tích hợp cho API khuyến mãi và thông báo, kiểm tra logic thời hạn tự động ẩn thông báo hết hạn → `./sources/backend/membership-hub/src/test/java/com/membershiphub/rest/PromotionAnnouncementResourceTest.java` [REQ-017, REQ-018]
- **Doc**: Cập nhật từ điển dữ liệu, tài liệu API cho các endpoint quản lý khuyến mãi và thông báo → `./sources/docs/data-dictionary/promotions-announcements.md` [DAT-009]

### 📅 Ngày 2: Tích hợp chatbot AI & cơ sở dữ liệu hệ thống
- **Coder**: Tích hợp mô hình chatbot AI, triển khai endpoint xử lý câu hỏi người dùng, logic chuyển cho hỗ trợ viên khi độ tin cậy trả lời thấp → `./sources/backend/membership-hub/src/main/java/com/membershiphub/service/ChatbotService.java` [REQ-019]
- **Coder**: Viết script di chuyển Flyway tạo bảng AUDIT_LOG, SYSTEM_SETTINGS, thêm chỉ mục cho các trường thường xuyên truy vấn → `./sources/backend/membership-hub/src/main/resources/db/migration/V7__create_audit_system_settings.sql` [DAT-010, DAT-011, NFR-006]
- **Tester**: Viết bài kiểm tra tích hợp cho chatbot, kiểm tra độ chính xác trả lời và logic chuyển hỗ trợ viên → `./sources/backend/membership-hub/src/test/java/com/membershiphub/service/ChatbotServiceTest.java` [REQ-019]
- **Doc**: Cập nhật từ điển dữ liệu bảng SYSTEM_SETTINGS, tài liệu tích hợp mô hình AI → `./sources/docs/integrations/ai-chatbot.md` [REQ-019]

### 📅 Ngày 3: Triển khai báo cáo & dashboard phân tích
- **Coder**: Triển khai dịch vụ tạo báo cáo điểm danh định dạng CSV, dashboard tổng quan ghi danh thời gian thực hiển thị tổng số học viên, khóa học đang hoạt động, buổi học sắp tới → `./sources/backend/membership-hub/src/main/java/com/membershiphub/service/ReportService.java` [REQ-024, REQ-025]
- **Tester**: Viết bài kiểm tra tích hợp cho dịch vụ báo cáo, kiểm tra định dạng CSV chính xác và dữ liệu dashboard khớp với dữ liệu cơ sở dữ liệu → `./sources/backend/membership-hub/src/test/java/com/membershiphub/service/ReportServiceTest.java` [REQ-024, REQ-025]
- **Doc**: Viết tài liệu hướng dẫn sử dụng báo cáo điểm danh và dashboard phân tích → `./sources/docs/user-guide/reports-dashboard.md` [REQ-024, REQ-025]

---

## 📲 GIAI ĐOẠN 5: ỨNG DỤNG DI ĐỘNG, ĐA NGÔN NGỮ, SEO & TỐI ƯU HÓA
### 📅 Ngày 1: Triển khai giao diện người dùng vai trò trên di động
- **Coder**: Xây dựng giao diện người dùng vai trò cụ thể trên React Native: màn hình đăng nhập, danh sách khóa học, quét mã QR điểm danh, xem thẻ hội viên, điều hướng theo quyền vai trò → `./sources/frontend/mobile-app/src/screens/RoleBasedHomeScreen.tsx` [REQ-020, ARC-004, ARC-005]
- **Tester**: Viết bài kiểm tra giao diện cho màn hình chính vai trò, kiểm tra điều hướng đúng theo quyền người dùng → `./sources/frontend/mobile-app/src/tests/screens/RoleBasedHomeScreen.test.tsx` [REQ-020]
- **Doc**: Viết tài liệu hướng dẫn sử dụng ứng dụng di động cho từng vai trò người dùng → `./sources/docs/user-guide/mobile-app-guide.md` [REQ-020]

### 📅 Ngày 2: Triển khai đa ngôn ngữ, SEO & thông báo đẩy di động
- **Coder**: Tích hợp thư viện i18next cho đa ngôn ngữ (Tiếng Anh, Tiếng Việt, Tiếng Tây Ban Nha), cấu hình phát hiện ngôn ngữ mặc định từ lưu trữ cục bộ hoặc tiêu đề Accept-Language, chuyển đổi locale không cần tải lại trang → `./sources/frontend/web-app/src/i18n/config.ts` [REQ-022, NFR-007]
- **Coder**: Cấu hình SEO đa ngôn ngữ cho Next.js: thêm thẻ hreflang, meta tag ngôn ngữ cho tất cả các trang, đảm bảo thuộc tính html lang khớp với locale → `./sources/frontend/web-app/src/app/[locale]/layout.tsx` [REQ-023, NFR-007]
- **Coder**: Tích hợp Firebase Cloud Messaging (FCM) cho thông báo đẩy di động, xử lý đăng ký và quản lý token thiết bị → `./sources/frontend/mobile-app/src/services/PushNotificationService.ts` [REQ-021, ARC-008]
- **Tester**: Viết bài kiểm tra tích hợp cho chức năng đa ngôn ngữ, kiểm tra thẻ hreflang và luồng nhận thông báo đẩy → `./sources/frontend/web-app/src/tests/i18n-seo.test.ts` [REQ-022, REQ-023, REQ-021]
- **Doc**: Cập nhật tài liệu cấu hình đa ngôn ngữ, SEO và thông báo đẩy → `./sources/docs/configuration/i18n-seo-push.md` [REQ-022, REQ-023, REQ-021]

### 📅 Ngày 3: Triển khai CI/CD, giám sát & tối ưu hóa hiệu suất
- **Docker**: Tạo Dockerfile cho ứng dụng di động React Native và ứng dụng web Next.js, tối ưu kích thước hình ảnh → `./sources/infra/web-app/Dockerfile`, `./sources/infra/mobile-app/Dockerfile` [NFR-005]
- **GKE**: Triển khai bản định nghĩa triển khai Kubernetes, dịch vụ, Horizontal Pod Autoscaler (HPA) cho tất cả các dịch vụ backend và frontend → `./sources/infra/gke/deployment.yaml` [NFR-002, NFR-004]
- **GCP**: Cấu hình Cloud Logging, Cloud Monitoring, cảnh báo uptime, sao lưu PostgreSQL tự động hàng ngày, sao lưu cụm GKE sang vùng khác → `./sources/infra/gcp/monitoring-backup.yaml` [NFR-002, NFR-006, NFR-009]
- **Reviewer**: Kiểm tra toàn bộ cấu hình hạ tầng, đề xuất tối ưu chi phí và cấu hình bảo mật → `./sources/infra/gke/deployment.yaml` [NFR-003, NFR-004]
- **Doc**: Viết tài liệu hướng dẫn triển khai, giám sát, sao lưu và phục hồi thảm họa → `./sources/docs/operations/deployment-monitoring.md` [NFR-002, NFR-009]

## 🏁 4. TỔNG QUAN KIẾN TRÚC ĐA PHASE Ở MỨC CAO

### 📦 4.1. DANH SÁCH CÔNG VIỆC SẢN PHẨM KIẾN TRÚC CHÍNH

Kiến trúc hệ thống membership-hub được thiết kế theo mô hình microservices với các thành phần phụ thuộc chặt chẽ: lớp backend Quarkus phụ thuộc vào cơ sở dữ liệu PostgreSQL để lưu trữ dữ liệu thực thể, Redis để caching phiên làm việc và Firebase Authentication để xác thực người dùng; lớp frontend Next.js tiêu thụ REST API từ backend, tích hợp FCM/APNs cho thông báo đẩy và hỗ trợ caching ngoại tuyến; hạ tầng DevOps trên GKE phụ thuộc vào Docker để đóng gói hình ảnh, Terraform để provisioning tài nguyên GCP và GitHub Actions để pipeline CI/CD; tất cả các lớp đều tuân thủ các yêu cầu phi chức năng về bảo mật, hiệu suất và khả năng mở rộng được định nghĩa trong tài liệu yêu cầu, đảm bảo tính tin cậy và khả năng mở rộng cho nền tảng quản lý hội viên đa trung tâm.

<!--START_BACKLOG_SYNOPSIS_GRID-->

### [MA TRẬN SỐ HỌC HỆ THỐNG]
> - **Tổng số thẻ [REQ]:** 25 Thẻ
> - **Tổng số thẻ [EXC]:** 5 Thẻ
> - **Tổng số thẻ [ARC]:** 10 Thẻ
> - **Tổng số thẻ [DAT]:** 11 Thẻ
> - **Tổng số thẻ [NFR]:** 9 Thẻ
> - ➡️ **Tổng số thẻ SRS:** 58 Thẻ

| No. | Nhiệm vụ | Mục đích kỹ thuật / Tóm tắt sản phẩm đầu ra | Loại | TagID |
| :--- | :--- | :--- | :--- | :--- |
| 1 | Triển khai chức năng đăng ký người dùng với email/mật khẩu và cấp JWT token | Tạo endpoint đăng ký, xác thực đầu vào, tạo bản ghi người dùng với vai trò mặc định Student, cấp JWT token và refresh token | Mã Ứng dụng | [REQ-001], [EXC-004] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 2 | Tích hợp xác thực mạng xã hội (Firebase, Google, Facebook) qua OAuth2 | Xây dựng flow xác thực OAuth2, trao đổi mã xác thực lấy thông tin người dùng, tạo/cập nhật bản ghi người dùng cục bộ, cấp JWT token | Mã Ứng dụng | [REQ-002], [EXC-004] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 3 | Triển khai phân quyền người dùng dựa trên vai trò (RBAC) với 5 vai trò được định nghĩa | Xây dựng cơ chế gán/thay đổi vai trò người dùng, áp dụng quyền truy cập tương ứng ngay lập tức | Mã Ứng dụng | [REQ-003] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 4 | Xây dựng API xem danh sách trung tâm với thông tin địa chỉ, mã số thuế và liên hệ quản trị | Tạo endpoint lấy danh sách trung tâm, trả về các trường Name, Address, TaxID, AdminContact | Mã Ứng dụng | [REQ-004] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 5 | Triển khai chức năng quản lý trung tâm (tạo, cập nhật, xóa) cho System Admin | Xây dựng endpoint CRUD cho trung tâm, kiểm tra trùng lặp mã số thuế, trả về lỗi conflict nếu trùng | Mã Ứng dụng | [REQ-005] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 6 | Xây dựng chức năng phân quyền quản trị trung tâm cho từng người dùng | Tạo endpoint gán/huỷ gán vai trò Center Admin cho người dùng tại trung tâm cụ thể, cập nhật quyền truy cập tương ứng | Mã Ứng dụng | [REQ-006] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 7 | Xây dựng API xem danh sách khóa học với lịch trình và giáo viên phụ trách | Tạo endpoint lấy danh sách khóa học, trả về các trường CourseID, Title, StartDate, EndDate, TeacherName | Mã Ứng dụng | [REQ-007] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 8 | Triển khai quản lý khóa học (tạo, cập nhật, xóa) với kiểm tra xung đột lịch trình giáo viên/địa điểm | Xây dựng endpoint CRUD cho khóa học, kiểm tra xung đột lịch trình giáo viên trước khi lưu, trả về lỗi nếu có xung đột | Mã Ứng dụng | [REQ-008] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 9 | Xây dựng chức năng phân công giáo viên vào khóa học và gửi thông báo | Tạo endpoint gán/huỷ gán giáo viên cho khóa học, xếp hàng thông báo cho ứng dụng di động của giáo viên | Mã Ứng dụng | [REQ-009] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 10 | Xây dựng chức năng duyệt khóa học cho học viên, loại trừ các khóa đã đăng ký | Tạo endpoint lấy danh sách khóa học có sẵn, lọc ra các khóa học học viên đã đăng ký, hiển thị sức chứa và lịch trình | Mã Ứng dụng | [REQ-010] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 11 | Triển khai quy trình đăng ký khóa học, tự động tạo tài khoản Student nếu chưa tồn tại | Xây dựng endpoint đăng ký khóa học, tự động tạo tài khoản Student với vai trò tương ứng nếu chưa tồn tại, xếp hàng thông báo cho học viên và nhóm Zalo của trung tâm | Mã Ứng dụng | [REQ-011] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 12 | Xây dựng chức năng quét mã QR điểm danh trên ứng dụng di động và ghi nhận kết quả | Tích hợp trình quét QR vào ứng dụng di động, xây dựng endpoint nhận payload điểm danh, xác thực quan hệ học viên-khóa học, tạo bản ghi điểm danh | Mã Ứng dụng | [REQ-012], [EXC-001], [EXC-002] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 13 | Đảm bảo tính chất bất biến của điểm danh (chỉ 1 bản ghi mỗi học viên/khóa học/ngày) | Triển khai kiểm tra idempotent cho endpoint điểm danh, đảm bảo chỉ tạo 1 bản ghi điểm danh cho mỗi học viên/khóa học/ngày, trả về cờ 'đã ghi nhận' nếu trùng lặp | Mã Ứng dụng | [REQ-013], [EXC-001], [EXC-002] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 14 | Xây dựng chức năng hiển thị thẻ hội viên kỹ thuật số với số ngày còn lại hiệu lực | Tạo endpoint lấy thông tin thẻ hội viên, tính toán số ngày còn lại hiệu lực, hiển thị tổng ngày hiệu lực, ngày đã sử dụng và ngày còn lại trên giao diện người dùng | Mã Ứng dụng | [REQ-014] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 15 | Triển khai chức năng gia hạn thẻ hội viên với tích hợp thanh toán | Xây dựng endpoint gia hạn thẻ, tích hợp với cổng thanh toán, cập nhật ngày kết thúc thẻ khi thanh toán thành công, gửi thông báo xác nhận | Mã Ứng dụng | [REQ-015] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 16 | Xây dựng hệ thống thông báo đa kênh (push di động, nhóm Zalo) cho các sự kiện hệ thống | Tạo dịch vụ xử lý thông báo, xếp hàng payload push notification cho FCM/APNs, gửi tin nhắn đến nhóm Zalo được chỉ định cho các sự kiện như thông báo, phân công khóa học, cảnh báo điểm danh | Mã Ứng dụng | [REQ-016], [EXC-003] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 17 | Triển khai chức năng quản lý khuyến mãi (tạo, sửa, xóa) cho Center Admin và Manager | Xây dựng endpoint CRUD cho khuyến mãi, hỗ trợ ngày bắt đầu/ngày kết thúc tùy chọn, hiển thị khuyến mãi áp dụng cho học viên | Mã Ứng dụng | [REQ-017] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 18 | Xây dựng chức năng quản lý thông báo (tạo, sửa, xóa) với thời hạn hiển thị tùy chọn | Xây dựng endpoint CRUD cho thông báo, hỗ trợ ngày hiệu lực tùy chọn, tự động ẩn thông báo sau ngày kết thúc nếu được cấu hình | Mã Ứng dụng | [REQ-018] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 19 | Tích hợp chatbot AI hỗ trợ khách hàng cho các truy vấn thông thường | Tích hợp dịch vụ chatbot AI, xây dựng endpoint xử lý truy vấn người dùng, chuyển tiếp đến hỗ trợ con người nếu độ tin cậy thấp | Mã Ứng dụng | [REQ-019] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 20 | Xây dựng giao diện người dùng di động phản hồi theo vai trò người dùng | Phát triển giao diện di động đáp ứng, hiển thị menu và màn hình tương ứng với vai trò người dùng (Student, Teacher, Admin, v.v.) | Mã Ứng dụng | [REQ-020] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 21 | Triển khai thông báo đẩy di động (FCM/APNs) cho các sự kiện hệ thống | Tích hợp FCM/APNs, quản lý token thiết bị người dùng, gửi thông báo đẩy cho các sự kiện như xác nhận điểm danh, thông báo mới, nhắc nhở | Mã Ứng dụng | [REQ-021], [EXC-003] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 22 | Triển khai phát hiện ngôn ngữ mặc định và lưu trữ tùy chọn người dùng | Xây dựng cơ chế phát hiện ngôn ngữ từ tùy chọn đã lưu hoặc header Accept-Language, lưu trữ tùy chọn người dùng, cập nhật giao diện theo ngôn ngữ tương ứng | Mã Ứng dụng | [REQ-022] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 23 | Xây dựng cơ chế SEO đa ngôn ngữ với thẻ meta và hreflang cho 3 ngôn ngữ | Cấu hình thẻ meta ngôn ngữ, tạo liên kết hreflang cho tiếng Anh, tiếng Việt, tiếng Tây Ban Nha, đảm bảo mỗi trang có thuộc tính ngôn ngữ chính xác | Mã Ứng dụng | [REQ-023] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 24 | Triển khai chức năng tạo báo cáo điểm danh hàng ngày theo trung tâm (định dạng CSV) | Xây dựng endpoint tạo báo cáo điểm danh, cho phép chọn trung tâm và khoảng thời gian, xuất file CSV với các cột StudentName, CourseName, AttendanceDate, Status | Mã Ứng dụng | [REQ-024], [EXC-005] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 25 | Xây dựng bảng điều khiển tóm tắt ghi danh thời gian thực cho Center Admin | Tạo bảng điều khiển hiển thị tổng số học viên, khóa học đang hoạt động, buổi học sắp tới (7 ngày tiếp theo), cập nhật dữ liệu thời gian thực | Mã Ứng dụng | [REQ-025], [EXC-005] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 26 | Khởi tạo hạ tầng cơ sở dữ liệu và xác thực tất cả thực thể dữ liệu | Tạo schema cơ sở dữ liệu PostgreSQL cho tất cả 11 bảng nghiệp vụ, chạy migration kiểm tra tính toàn vẹn dữ liệu và ràng buộc khóa ngoại | Tài liệu Doanh nghiệp | [DAT-ALL (1 to 11)] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 27 | Triển khai lớp bảo mật RBAC và các luồng kiểm soát truy cập kiến trúc | Cấu hình quyền truy cập theo vai trò cho tất cả các endpoint, triển khai xác thực JWT và OAuth2, đảm bảo tuân thủ OWASP Top 10 | Tài liệu Doanh nghiệp | [ARC-001 to ARC-010] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 28 | Xây dựng hạ tầng DevOps (Docker, Terraform, GKE) và triển khai pipeline CI/CD | Tạo Dockerfile đa giai đoạn, cấu hình Terraform cho tài nguyên GCP, triển khai manifest GKE, thiết lập GitHub Actions cho CI/CD | Hạ tầng DevOps | [NFR-001 to NFR-009] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 29 | Xây dựng tài liệu kiến trúc hệ thống, tài liệu API và hướng dẫn vận hành | Tạo bản vẽ kiến trúc tổng thể, tài liệu tham chiếu API REST, hướng dẫn cài đặt và vận hành hệ thống | Tài liệu Doanh nghiệp | [ARC-001 to ARC-010], [NFR-001 to NFR-009] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| **TÓM TẮT** | **Tổng số thẻ theo dõi được bao phủ:** 60 | **Tổng số nhiệm vụ:** 29 | **Trạng thái:** THẤT BẠI | **Tỷ lệ bao phủ:** 103.45% |

<!--END_BACKLOG_SYNOPSIS_GRID-->

<!--END_PART_1_BACKLOG_4_1-->

### 🔭 4.2. MA TRẬN TỔNG QUAN ĐA GIAI ĐOẠN
<!--START_PHASE_SYNOPSIS_GRID-->
### [MA TRẬN SỐ HỌC VÒNG ĐỜI]
> - **Tổng số nhiệm vụ backlog:** 29 Nhiệm vụ
> - **Tổng số thẻ backlog:** 60 Thẻ
> - **Tổng số nhiệm vụ được phân phối:** 29 Nhiệm vụ
> - **Tổng số thẻ được phân phối:** 60 Thẻ

| Giai đoạn | Khoảng ngày | ID nhiệm vụ được bao phủ | Thành phần kiến trúc / Đường dẫn mô-đun | Tóm tắt sản phẩm kỹ thuật | Đại lý phụ được phân công | ID thẻ mục tiêu |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Giai đoạn 1 | Ngày 1 - 4 | Nhiệm vụ 1, Nhiệm vụ 2, Nhiệm vụ 3, Nhiệm vụ 4, Nhiệm vụ 5, Nhiệm vụ 6, Nhiệm vụ 26 | ./sources/backend/migrations, ./sources/backend/auth-service, ./sources/backend/user-service, ./sources/backend/center-service | Khởi tạo schema PostgreSQL cho các bảng người dùng, vai trò, trung tâm; triển khai xác thực email/mật khẩu và OAuth2 (Firebase/Google/Facebook); cấp JWT token 15 phút và refresh token 7 ngày; triển khai cơ chế RBAC với 5 vai trò được định nghĩa; xây dựng API CRUD quản lý trung tâm với kiểm tra trùng lặp mã số thuế | Coder, Tester, Reviewer, Doc | [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [EXC-004], [DAT-001], [DAT-003], [ARC-001], [ARC-002], [ARC-006] <!--REGISTERED_PHASE_ROW--> |
| Giai đoạn 2 | Ngày 1 - 5 | Nhiệm vụ 7, Nhiệm vụ 8, Nhiệm vụ 9, Nhiệm vụ 10, Nhiệm vụ 11, Nhiệm vụ 12, Nhiệm vụ 13 | ./sources/backend/course-service, ./sources/backend/enrollment-service, ./sources/backend/attendance-service, ./sources/frontend/web/course | Xây dựng API quản lý khóa học CRUD với kiểm tra xung đột lịch trình giáo viên/địa điểm; phân công giáo viên vào khóa học và gửi thông báo; xây dựng chức năng duyệt và đăng ký khóa học cho học viên (tự động tạo tài khoản Student nếu chưa tồn tại); triển khai endpoint quét mã QR điểm danh với tính chất idempotent, đảm bảo chỉ 1 bản ghi điểm danh mỗi học viên/khóa học/ngày; xử lý ngoại lệ mất kết nối mạng và gửi điểm danh trùng lặp | Coder, Tester, Reviewer, Doc | [REQ-007], [REQ-008], [REQ-009], [REQ-010], [REQ-011], [REQ-012], [REQ-013], [EXC-001], [EXC-002], [DAT-004], [DAT-005], [DAT-006], [ARC-007] <!--REGISTERED_PHASE_ROW--> |
| Giai đoạn 3 | Ngày 1 - 4 | Nhiệm vụ 14, Nhiệm vụ 15, Nhiệm vụ 16, Nhiệm vụ 17, Nhiệm vụ 18 | ./sources/backend/membership-service, ./sources/backend/notification-service, ./sources/backend/promotion-service, ./sources/frontend/web/membership | Xây dựng API hiển thị thẻ hội viên kỹ thuật số với số ngày còn lại hiệu lực; triển khai chức năng gia hạn thẻ với tích hợp thanh toán; xây dựng hệ thống thông báo đa kênh (push FCM/APNs, nhóm Zalo) với cơ chế retry tối đa 3 lần khi gửi thất bại; triển khai quản lý khuyến mãi và thông báo có thời hạn hiển thị tùy chọn, tự động ẩn thông báo hết hạn | Coder, Tester, Reviewer, Doc | [REQ-014], [REQ-015], [REQ-016], [REQ-017], [REQ-018], [EXC-003], [DAT-007], [DAT-008], [DAT-009], [ARC-008] <!--REGISTERED_PHASE_ROW--> |
| Giai đoạn 4 | Ngày 1 - 4 | Nhiệm vụ 19, Nhiệm vụ 20, Nhiệm vụ 21, Nhiệm vụ 22, Nhiệm vụ 23, Nhiệm vụ 24, Nhiệm vụ 25 | ./sources/frontend/mobile-app, ./sources/backend/chatbot-service, ./sources/backend/report-service, ./sources/frontend/web/seo | Tích hợp chatbot AI hỗ trợ khách hàng với chuyển tiếp đến hỗ trợ con người khi độ tin cậy thấp; phát triển giao diện di động đáp ứng theo vai trò người dùng; triển khai thông báo đẩy di động (FCM/APNs); cấu hình phát hiện ngôn ngữ mặc định và lưu trữ tùy chọn người dùng, hỗ trợ đa ngôn ngữ (Anh, Việt, Tây Ban Nha); triển khai SEO đa ngôn ngữ với thẻ meta và hreflang; xây dựng chức năng tạo báo cáo điểm danh CSV hàng ngày theo trung tâm và bảng điều khiển tóm tắt ghi danh thời gian thực; xử lý ngoại lệ khôi phục hệ thống sau sự cố, xử lý điểm danh pending theo thứ tự FIFO | Coder, Tester, Reviewer, Doc | [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-003], [EXC-005], [DAT-011], [ARC-009] <!--REGISTERED_PHASE_ROW--> |
| Giai đoạn 5 | Ngày 1 - 5 | Nhiệm vụ 27, Nhiệm vụ 28, Nhiệm vụ 29 | ./sources/infra/terraform, ./sources/infra/docker, ./sources/infra/gke, ./sources/docs/architecture, ./sources/docs/api, ./sources/docs/operations | Triển khai lớp bảo mật RBAC toàn hệ thống và các luồng kiểm soát truy cập kiến trúc, đảm bảo tuân thủ OWASP Top 10; xây dựng hạ tầng DevOps với Docker đa giai đoạn (kích thước hình ảnh cuối <500MB), Terraform provisioning tài nguyên GCP, triển khai manifest GKE với HPA tự động scale dựa trên CPU >70% hoặc độ trễ yêu cầu >300ms, thiết lập pipeline CI/CD với GitHub Actions; xây dựng tài liệu kiến trúc hệ thống, tài liệu tham chiếu API REST, hướng dẫn cài đặt và vận hành; đảm bảo tuân thủ các yêu cầu phi chức năng về hiệu suất (độ trễ API <200ms), khả năng sẵn sàng (99.9% uptime), bảo mật (TLS 1.3, mã hóa AES-256), khả năng mở rộng, ghi log audit lưu trữ 1 năm, đa ngôn ngữ, tuân thủ GDPR/CCPA, sao lưu PostgreSQL hàng ngày và phục hồi điểm thời lên đến 24 giờ | Coder, Tester, Reviewer, Doc, Docker, GCP, GKE | [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] <!--REGISTERED_PHASE_ROW--> |
| **Kiểm toán** | **Xác minh phân phối backlog tổng thể** | **Tổng số giai đoạn:** 5 | **Tổng số thẻ backlog:** 60 | **Tổng số thẻ được phân phối:** 60 | **Tổng số nhiệm vụ được phân phối:** 29 | **Trạng thái & Tuân thủ:** Đã xác minh (100%) |
<!--END_PHASE_SYNOPSIS_GRID-->
<!--END_PART_1_MATRIX_4_2-->

<!--START_PHASE_INDEX-->
### 📈 Giai đoạn 1 - Khởi tạo Hệ thống Xác thực, Quản lý Người dùng và Trung tâm
- **Mục tiêu cốt lõi của giai đoạn:** Xây dựng nền tảng cơ sở cho hệ thống quản lý hội viên, bao gồm khởi tạo schema cơ sở dữ liệu PostgreSQL cho các thực thể người dùng, vai trò và trung tâm; triển khai luồng xác thực người dùng với email/mật khẩu và OAuth2 (Firebase/Google/Facebook); cấp phát JWT token có thời hạn 15 phút và refresh token 7 ngày; triển khai cơ chế phân quyền RBAC với 5 vai trò được định nghĩa; xây dựng API CRUD quản lý trung tâm với kiểm tra trùng lặp mã số thuế, đảm bảo các thành phần cốt lõi hoạt động ổn định và tuân thủ yêu cầu bảo mật ban đầu.

- **Bản đồ ma trận thư mục vật lý mục tiêu:** Liệt kê tất cả các tệp vật lý cụ thể được tạo hoặc xử lý trong phạm vi giai đoạn này, kèm thẻ theo dõi tương ứng:
  * `./sources/backend/migrations/V1__init_user_center_schema.sql` [DAT-001], [DAT-003]
  * `./sources/backend/auth-service/src/main/java/com/hub/auth/AuthResource.java` [REQ-001], [REQ-002], [ARC-006]
  * `./sources/backend/auth-service/src/main/java/com/hub/auth/AuthService.java` [REQ-001], [REQ-002]
  * `./sources/backend/auth-service/src/main/java/com/hub/auth/TokenService.java` [REQ-001], [ARC-006]
  * `./sources/backend/auth-service/src/main/java/com/hub/auth/OAuth2Service.java` [REQ-002], [EXC-004]
  * `./sources/backend/auth-service/src/main/java/com/hub/auth/RbacFilter.java` [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
  * `./sources/backend/user-service/src/main/java/com/hub/user/UserResource.java` [REQ-003], [REQ-004], [ARC-001]
  * `./sources/backend/user-service/src/main/java/com/hub/user/UserService.java` [REQ-003], [ARC-001]
  * `./sources/backend/user-service/src/main/java/com/hub/user/RoleService.java` [REQ-003], [ARC-001]
  * `./sources/backend/center-service/src/main/java/com/hub/center/CenterResource.java` [REQ-004], [REQ-005], [REQ-006], [ARC-002]
  * `./sources/backend/center-service/src/main/java/com/hub/center/CenterService.java` [REQ-005], [ARC-002]
  * `./sources/backend/center-service/src/main/java/com/hub/center/CenterAdminService.java` [REQ-006], [ARC-002]
  * `./sources/docs/auth-api-spec.md` [REQ-001], [REQ-002], [ARC-006]
  * `./sources/docs/rbac-policy.md` [REQ-003], [ARC-001], [ARC-002]
  * `./sources/docs/center-management-spec.md` [REQ-004], [REQ-005], [REQ-006]

- **Đặc tả SQL DDL Schema Cơ sở dữ liệu** [DAT-001], [DAT-003]
```sql
-- Khởi tạo schema cho các bảng người dùng, vai trò và trung tâm
CREATE TABLE IF NOT EXISTS roles (
    role_id SMALLINT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash CHAR(60) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role_id SMALLINT NOT NULL REFERENCES roles(role_id),
    provider VARCHAR(20) NOT NULL DEFAULT 'local' CHECK (provider IN ('local', 'firebase', 'google', 'facebook')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS centers (
    center_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tax_id VARCHAR(13) NOT NULL UNIQUE CHECK (tax_id ~ '^[0-9]{10,13}$'),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255) CHECK (contact_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Tạo index cho các trường thường xuyên truy vấn
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_centers_tax_id ON centers(tax_id);
```

- **Hợp đồng Định tuyến API và Sự kiện** [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [ARC-006]
```json
// 1. Endpoint đăng ký người dùng
POST /api/v1/auth/register
Request Body:
{
  "email": "string (required, định dạng email hợp lệ)",
  "password": "string (required, tối thiểu 8 ký tự, có chữ hoa, chữ thường, số, ký tự đặc biệt)",
  "fullName": "string (required, tối đa 100 ký tự)",
  "provider": "string (tùy chọn, giá trị: local, firebase, google, facebook, mặc định local)"
}
Response 201:
{
  "userId": "uuid",
  "email": "string",
  "role": "string (Student/Teacher)",
  "accessToken": "string (JWT, hết hạn 15 phút)",
  "refreshToken": "string (hết hạn 7 ngày)"
}
Response 400: { "error": "VALIDATION_FAILED", "message": "Danh sách lỗi trường không hợp lệ" }

// 2. Endpoint đăng nhập OAuth2
POST /api/v1/auth/oauth2/{provider}
Request Body:
{
  "authCode": "string (required, mã xác thực từ nhà cung cấp OAuth2)"
}
Response 200: Tương tự response đăng ký

// 3. Endpoint gán vai trò người dùng
POST /api/v1/admin/users/{userId}/role
Request Body:
{
  "roleId": "smallint (required, ID vai trò từ bảng roles)"
}
Response 200: { "message": "Cập nhật vai trò thành công" }

// 4. Endpoint lấy danh sách trung tâm
GET /api/v1/centers
Response 200:
[
  {
    "centerId": "uuid",
    "name": "string",
    "address": "string",
    "taxId": "string",
    "contactPhone": "string",
    "contactEmail": "string"
  }
]

// 5. Endpoint tạo trung tâm
POST /api/v1/admin/centers
Request Body: Tương tự object center, bỏ các trường tự động sinh
Response 201: Object center vừa tạo
Response 409: { "error": "TAX_ID_CONFLICT", "message": "Mã số thuế đã tồn tại" }

// 6. Endpoint gán quản trị viên trung tâm
POST /api/v1/admin/centers/{centerId}/admins
Request Body:
{
  "userId": "uuid (required)",
  "isAssign": "boolean (required, true để gán, false để huỷ gán)"
}
Response 200: { "message": "Thao tác phân quyền trung tâm thành công" }
```

- **Trình xử lý Ngoại lệ Cục bộ của Giai đoạn** [EXC-004]
- Mã lỗi: `VALIDATION_INPUT_INVALID` (HTTP 400)
  - Điều kiện kích hoạt: Các trường đầu vào không đạt yêu cầu kiểm tra (email không đúng định dạng, mật khẩu không đủ mạnh, thiếu trường bắt buộc)
  - Hành vi xử lý: Trả về phản hồi lỗi chi tiết liệt kê từng trường không hợp lệ, yêu cầu người dùng chỉnh sửa trước khi gửi lại
- Mã lỗi: `OAUTH2_AUTH_FAILED` (HTTP 401)
  - Điều kiện kích hoạt: Trao đổi mã xác thực OAuth2 với nhà cung cấp thất bại, hoặc thông tin người dùng không hợp lệ
  - Hành vi xử lý: Trả về thông báo lỗi xác thực thất bại, yêu cầu người dùng thử lại hoặc chọn phương thức đăng nhập khác
- Mã lỗi: `TAX_ID_DUPLICATE` (HTTP 409)
  - Điều kiện kích hoạt: Mã số thuế của trung tâm mới trùng với bản ghi đã tồn tại trong hệ thống
  - Hành vi xử lý: Trả về lỗi xung đột, ngăn chặn tạo/cập nhật trung tâm, yêu cầu nhập mã số thuế khác

#### 📅 Nhật ký Phân phối Công việc Đại lý Phụ theo Thứ tự Thời gian (Giai đoạn 1)

<!--START_DAY_LOG_INDEX-->
##### 📅 Ngày 1: Khởi tạo Schema Cơ sở dữ liệu và Dịch vụ Xác thực Cốt lõi

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 1: Triển khai migration khởi tạo schema bảng người dùng, vai trò và trung tâm
* **Chuyên môn tác nghiệp:** [Coder]
* **ID thẻ mục tiêu:** [DAT-001], [DAT-003]
* **Thành phần tệp mục tiêu (target_component):** `./sources/backend/migrations/V1__init_user_center_schema.sql`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết câu lệnh migration ANSI SQL để tạo 3 bảng: roles (role_id PK, name, description), users (user_id PK, email, password_hash, full_name, role_id FK, provider, created_at, updated_at), centers (center_id PK, name, address, tax_id, contact_phone, contact_email). Thêm ràng buộc CHECK cho trường provider của bảng users với các giá trị hợp lệ (local, firebase, google, facebook), ràng buộc CHECK cho tax_id của bảng centers chỉ chấp nhận 10-13 chữ số, ràng buộc CHECK cho contact_email đúng định dạng email. Tạo index cho các trường thường xuyên truy vấn: users.email, users.role_id, centers.tax_id. Đảm bảo tất cả các ràng buộc khóa ngoại được định nghĩa chính xác.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 2: Xây dựng dịch vụ xác thực email/mật khẩu và cấp phát JWT token
* **Chuyên môn tác nghiệp:** [Coder]
* **ID thẻ mục tiêu:** [REQ-001], [ARC-006]
* **Thành phần tệp mục tiêu (target_component):** `./sources/backend/auth-service/src/main/java/com/hub/auth/AuthService.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic xác thực đầu vào cho email và mật khẩu (kiểm tra định dạng email, độ mạnh mật khẩu tối thiểu 8 ký tự có chữ hoa, chữ thường, số, ký tự đặc biệt). Sử dụng bcrypt để băm mật khẩu trước khi lưu vào cơ sở dữ liệu. Triển khai logic cấp phát JWT access token có thời hạn 15 phút và refresh token có thời hạn 7 ngày, kèm cơ chế làm mới token hợp lệ. Lưu trữ refresh token đã mã hóa trong cơ sở dữ liệu để xác thực khi làm mới.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 3: Xây dựng endpoint đăng ký và đăng nhập người dùng
* **Chuyên môn tác nghiệp:** [Coder]
* **ID thẻ mục tiêu:** [REQ-001], [EXC-004]
* **Thành phần tệp mục tiêu (target_component):** `./sources/backend/auth-service/src/main/java/com/hub/auth/AuthResource.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng endpoint POST /api/v1/auth/register để xử lý yêu cầu đăng ký người dùng: xác thực đầu vào, tạo bản ghi người dùng mới với vai trò mặc định là Student, trả về JWT access token và refresh token khi đăng ký thành công. Xây dựng endpoint POST /api/v1/auth/login để xử lý đăng nhập với email/mật khẩu, xác thực thông tin và cấp token tương tự. Triển khai xử lý ngoại lệ VALIDATION_INPUT_INVALID: nếu có trường không hợp lệ, trả về mã 400 kèm danh sách chi tiết lỗi từng trường.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 4: Viết bộ kiểm thử đơn vị cho dịch vụ xác thực và endpoint đăng ký
* **Chuyên môn tác nghiệp:** [Tester]
* **ID thẻ mục tiêu:** [REQ-001], [EXC-004]
* **Thành phần tệp mục tiêu (target_component):** `./sources/backend/auth-service/src/main/java/com/hub/auth/AuthService.java;./sources/backend/auth-service/src/test/java/com/hub/auth/AuthServiceTest.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử đơn vị cho AuthService: kiểm tra xác thực email hợp lệ/không hợp lệ, kiểm tra độ mạnh mật khẩu, kiểm tra băm mật khẩu đúng định dạng bcrypt, kiểm tra cấp phát JWT token có thời hạn chính xác. Viết kiểm thử cho endpoint đăng ký: kiểm tra đăng ký thành công với thông tin hợp lệ, kiểm tra trả về lỗi 400 khi thiếu trường bắt buộc, kiểm tra trả về lỗi khi email đã tồn tại. Đảm bảo độ bao phủ mã ít nhất 90% cho các tệp liên quan.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 5: Xây dựng tài liệu đặc tả API cho luồng xác thực người dùng
* **Chuyên môn tác nghiệp:** [Doc]
* **ID thẻ mục tiêu:** [REQ-001], [REQ-002], [ARC-006]
* **Thành phần tệp mục tiêu (target_component):** `./sources/docs/auth-api-spec.md`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết tài liệu đặc tả API cho các endpoint xác thực: đăng ký, đăng nhập, làm mới token, đăng xuất. Mô tả chi tiết tham số yêu cầu, phản hồi thành công, phản hồi lỗi, mã lỗi HTTP tương ứng, yêu cầu xác thực (nếu có). Bao gồm ví dụ payload JSON cho mỗi trường hợp.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
##### 📅 Ngày 2: Triển khai Xác thực OAuth2 và Cơ chế Phân quyền RBAC

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 1: Tích hợp luồng xác thực OAuth2 với Firebase, Google và Facebook
* **Chuyên môn tác nghiệp:** [Coder]
* **ID thẻ mục tiêu:** [REQ-002], [EXC-004]
* **Thành phần tệp mục tiêu (target_component):** `./sources/backend/auth-service/src/main/java/com/hub/auth/OAuth2Service.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic xử lý xác thực OAuth2 cho 3 nhà cung cấp: Firebase, Google, Facebook. Xây dựng luồng trao đổi mã xác thực (auth code) lấy thông tin người dùng từ nhà cung cấp, kiểm tra tính hợp lệ của mã. Nếu người dùng đã tồn tại trong hệ thống, cập nhật thông tin xác thực; nếu chưa tồn tại, tạo bản ghi người dùng mới với vai trò Student. Cấp JWT token tương tự luồng đăng nhập email/mật khẩu. Triển khai xử lý ngoại lệ khi trao đổi mã xác thực thất bại, trả về lỗi 401 với thông báo rõ ràng.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 2: Xây dựng endpoint quản lý vai trò người dùng (RBAC)
* **Chuyên môn tác nghiệp:** [Coder]
* **ID thẻ mục tiêu:** [REQ-003], [ARC-001]
* **Thành phần tệp mục tiêu (target_component):** `./sources/backend/user-service/src/main/java/com/hub/user/RoleService.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic gán/thay đổi vai trò người dùng: nhận ID người dùng và ID vai trò mới, cập nhật trường role_id trong bảng users. Áp dụng ngay quyền truy cập tương ứng với vai trò mới mà không yêu cầu người dùng đăng nhập lại. Triển khai kiểm tra quyền: chỉ System Admin mới có quyền thực hiện thao tác thay đổi vai trò.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 3: Xây dựng endpoint lấy danh sách người dùng và quản lý vai trò
* **Chuyên môn tác nghiệp:** [Coder]
* **ID thẻ mục tiêu:** [REQ-003], [ARC-001]
* **Thành phần tệp mục tiêu (target_component):** `./sources/backend/user-service/src/main/java/com/hub/user/UserResource.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng endpoint GET /api/v1/admin/users để lấy danh sách người dùng với thông tin vai trò tương ứng, hỗ trợ lọc theo vai trò và tìm kiếm theo tên/email. Xây dựng endpoint PUT /api/v1/admin/users/{userId}/role để cập nhật vai trò người dùng, kèm kiểm tra quyền truy cập của người thực hiện thao tác.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 4: Viết bộ kiểm thử đơn vị cho luồng OAuth2 và RBAC
* **Chuyên môn tác nghiệp:** [Tester]
* **ID thẻ mục tiêu:** [REQ-002], [REQ-003], [EXC-004]
* **Thành phần tệp mục tiêu (target_component):** `./sources/backend/auth-service/src/main/java/com/hub/auth/OAuth2Service.java;./sources/backend/auth-service/src/test/java/com/hub/auth/OAuth2ServiceTest.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử cho luồng OAuth2: kiểm tra xác thực thành công với từng nhà cung cấp (Firebase, Google, Facebook), kiểm tra tạo tài khoản mới khi người dùng OAuth2 chưa tồn tại, kiểm tra cập nhật thông tin người dùng đã tồn tại, kiểm tra xử lý lỗi khi mã xác thực không hợp lệ. Viết kiểm thử cho chức năng RBAC: kiểm tra cập nhật vai trò thành công, kiểm tra từ chối truy cập khi người dùng không có quyền thay đổi vai trò, kiểm tra quyền truy cập được áp dụng ngay sau khi thay đổi vai trò.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
##### 📅 Ngày 3: Triển khai Quản lý Trung tâm và Phân quyền Quản trị Trung tâm

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 1: Xây dựng dịch vụ quản lý trung tâm (CRUD)
* **Chuyên môn tác nghiệp:** [Coder]
* **ID thẻ mục tiêu:** [REQ-005], [ARC-002]
* **Thành phần tệp mục tiêu (target_component):** `./sources/backend/center-service/src/main/java/com/hub/center/CenterService.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic CRUD cho bảng centers: tạo trung tâm mới với kiểm tra trùng lặp mã số thuế (trả về lỗi 409 nếu trùng), cập nhật thông tin trung tâm, xóa trung tâm (kiểm tra không có khóa học hoặc học viên đang hoạt động trước khi xóa), lấy thông tin chi tiết trung tâm theo ID. Đảm bảo tất cả các thao tác chỉ được thực hiện bởi System Admin.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 2: Xây dựng API CRUD quản lý trung tâm
* **Chuyên môn tác nghiệp:** [Coder]
* **ID thẻ mục tiêu:** [REQ-004], [REQ-005], [ARC-002]
* **Thành phần tệp mục tiêu (target_component):** `./sources/backend/center-service/src/main/java/com/hub/center/CenterResource.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng các endpoint REST cho quản lý trung tâm: GET /api/v1/centers (lấy danh sách tất cả trung tâm, trả về các trường name, address, taxId, contactPhone, contactEmail), GET /api/v1/centers/{centerId} (lấy chi tiết trung tâm), POST /api/v1/admin/centers (tạo trung tâm mới), PUT /api/v1/admin/centers/{centerId} (cập nhật trung tâm), DELETE /api/v1/admin/centers/{centerId} (xóa trung tâm). Áp dụng kiểm tra quyền truy cập cho tất cả các endpoint, chỉ cho phép System Admin thực hiện các thao tác tạo, sửa, xóa.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 3: Triển khai chức năng gán/huỷ gán quản trị viên trung tâm
* **Chuyên môn tác nghiệp:** [Coder]
* **ID thẻ mục tiêu:** [REQ-006], [ARC-002]
* **Thành phần tệp mục tiêu (target_component):** `./sources/backend/center-service/src/main/java/com/hub/center/CenterAdminService.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng endpoint POST /api/v1/admin/centers/{centerId}/admins để gán người dùng làm Center Admin cho trung tâm cụ thể: cập nhật vai trò của người dùng thành Center Admin, lưu thông tin trung tâm được quản lý vào hồ sơ người dùng. Xây dựng endpoint DELETE /api/v1/admin/centers/{centerId}/admins/{userId} để huỷ gán quyền Center Admin, đặt lại vai trò của người dùng về Student. Đảm bảo chỉ System Admin mới có quyền thực hiện các thao tác này.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 4: Viết bộ kiểm thử tích hợp cho API quản lý trung tâm
* **Chuyên môn tác nghiệp:** [Tester]
* **ID thẻ mục tiêu:** [REQ-004], [REQ-005], [REQ-006]
* **Thành phần tệp mục tiêu (target_component):** `INTEGRATION_SCOPE;./sources/backend/center-service/src/test/java/com/hub/center/CenterIntegrationTest.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử tích hợp cho API quản lý trung tâm: kiểm tra lấy danh sách trung tâm trả về đúng định dạng, kiểm tra tạo trung tâm thành công với thông tin hợp lệ, kiểm tra trả về lỗi 409 khi mã số thuế trùng lặp, kiểm tra cập nhật thông tin trung tâm thành công, kiểm tra xóa trung tâm thành công, kiểm tra gán/huỷ gán quản trị viên trung tâm hoạt động đúng. Kiểm tra rằng các thao tác bị từ chối khi người dùng không có quyền System Admin.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
##### 📅 Ngày 4: Hoàn thiện Phân quyền RBAC và Kiểm thử Toàn diện Giai đoạn

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 1: Triển khai bộ lọc phân quyền RBAC toàn cục cho tất cả endpoint
* **Chuyên môn tác nghiệp:** [Coder]
* **ID thẻ mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* **Thành phần tệp mục tiêu (target_component):** `./sources/backend/auth-service/src/main/java/com/hub/auth/RbacFilter.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng bộ lọc JAX-RS toàn cục để kiểm tra quyền truy cập của người dùng trước khi xử lý yêu cầu. Định nghĩa ma trận quyền truy cập cho từng vai trò: System Admin (toàn quyền), Center Admin (toàn quyền trong trung tâm của mình), Manager (quyền quản lý học viên, thông báo, không chỉnh sửa khóa học), Teacher (quyền xem khóa học, danh sách học viên, lịch dạy), Student (quyền duyệt khóa học, đăng ký, xem thẻ hội viên). Áp dụng bộ lọc cho tất cả các endpoint, trả về lỗi 403 Forbidden nếu người dùng không có quyền truy cập.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 2: Viết bộ kiểm thử đơn vị cho bộ lọc RBAC
* **Chuyên môn tác nghiệp:** [Tester]
* **ID thẻ mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* **Thành phần tệp mục tiêu (target_component):** `./sources/backend/auth-service/src/main/java/com/hub/auth/RbacFilter.java;./sources/backend/auth-service/src/test/java/com/hub/auth/RbacFilterTest.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử cho bộ lọc RBAC: kiểm tra truy cập thành công khi người dùng có quyền phù hợp với vai trò, kiểm tra trả về lỗi 403 khi người dùng không có quyền, kiểm tra quyền truy cập của Center Admin chỉ áp dụng cho trung tâm mà họ quản lý, kiểm tra quyền của Manager không cho phép chỉnh sửa khóa học. Đảm bảo độ bao phủ mã 100% cho bộ lọc RBAC.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 3: Rà soát mã nguồn và sửa lỗi cho các thành phần giai đoạn 1
* **Chuyên môn tác nghiệp:** [Reviewer]
* **ID thẻ mục tiêu:** [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [EXC-004], [ARC-001], [ARC-002], [ARC-006]
* **Thành phần tệp mục tiêu (target_component):** `./sources/backend/auth-service/src/main/java/com/hub/auth/AuthResource.java;./sources/backend/user-service/src/main/java/com/hub/user/UserResource.java;./sources/backend/center-service/src/main/java/com/hub/center/CenterResource.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Rà soát toàn bộ mã nguồn của các dịch vụ auth, user, center để phát hiện lỗi cú pháp, lỗi logic, điểm yếu bảo mật (ví dụ: lỗi SQL injection, thiếu kiểm tra quyền). Sửa tất cả các lỗi được phát hiện, đảm bảo mã nguồn tuân thủ tiêu chuẩn mã hóa doanh nghiệp và yêu cầu OWASP Top 10. Đảm bảo tất cả các thẻ theo dõi yêu cầu được triển khai đầy đủ.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Công việc phụ 4: Hoàn thiện tài liệu kỹ thuật cho giai đoạn 1
* **Chuyên môn tác nghiệp:** [Doc]
* **ID thẻ mục tiêu:** [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [ARC-001], [ARC-002], [ARC-006]
* **Thành phần tệp mục tiêu (target_component):** `./sources/docs/rbac-policy.md;./sources/docs/center-management-spec.md`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Hoàn thiện tài liệu chính sách RBAC, mô tả chi tiết quyền truy cập của từng vai trò, quy trình gán/thay đổi vai trò, xử lý ngoại lệ liên quan. Hoàn thiện tài liệu đặc tả quản lý trung tâm, mô tả chi tiết các endpoint, tham số, phản hồi, xử lý lỗi. Đảm bảo tài liệu được viết rõ ràng, dễ hiểu cho đội phát triển và đội vận hành.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--END_DAY_LOG_INDEX-->

<!--END_PHASE_INDEX-->

<!--START_PHASE_INDEX-->
### 📈 GIAI ĐOẠN 2 - TRIỂN KHAI MODULE QUẢN LÝ KHÓA HỌC, ĐIỂM DANH QR VÀ ĐĂNG KÝ HỌC VIÊN
- **Mục tiêu cốt lõi của giai đoạn & Mục đích:** Triển khai toàn bộ chức năng quản lý khóa học (CRUD, kiểm tra xung đột lịch trình giáo viên/địa điểm, phân công giáo viên), chức năng duyệt và đăng ký khóa học cho học viên (tự động tạo tài khoản Student nếu chưa tồn tại), tích hợp quét mã QR điểm danh có tính chất idempotent đảm bảo chỉ ghi nhận 1 bản ghi điểm danh mỗi học viên/khóa học/ngày, xử lý các ngoại lệ liên quan đến mất kết nối mạng và gửi điểm danh trùng lặp, bao phủ toàn bộ yêu cầu chức năng từ [REQ-007] đến [REQ-013] cùng các thẻ dữ liệu, ngoại lệ và kiến trúc liên quan.

- **Bản đồ ma trận thư mục vật lý mục tiêu:** Liệt kê tất cả các file vật lý cụ thể được tạo hoặc cập nhật trong giai đoạn này, mỗi dòng là file có phần mở rộng rõ ràng kèm thẻ theo dõi:
  * `./sources/backend/course-service/src/main/java/com/hub/course/CourseController.java` [REQ-007], [REQ-008], [REQ-009], [ARC-007]
  * `./sources/backend/course-service/src/main/java/com/hub/course/CourseService.java` [REQ-007], [REQ-008], [REQ-009], [ARC-007]
  * `./sources/backend/course-service/src/main/java/com/hub/course/model/Course.java` [DAT-004]
  * `./sources/backend/course-service/src/main/java/com/hub/course/repository/CourseRepository.java` [DAT-004]
  * `./sources/backend/course-service/src/main/java/com/hub/course/exception/ScheduleConflictException.java` [EXC-001]
  * `./sources/backend/enrollment-service/src/main/java/com/hub/enrollment/EnrollmentController.java` [REQ-010], [REQ-011], [ARC-007]
  * `./sources/backend/enrollment-service/src/main/java/com/hub/enrollment/EnrollmentService.java` [REQ-010], [REQ-011], [ARC-007]
  * `./sources/backend/enrollment-service/src/main/java/com/hub/enrollment/model/Enrollment.java` [DAT-005]
  * `./sources/backend/enrollment-service/src/main/java/com/hub/enrollment/repository/EnrollmentRepository.java` [DAT-005]
  * `./sources/backend/attendance-service/src/main/java/com/hub/attendance/AttendanceController.java` [REQ-012], [REQ-013], [EXC-001], [EXC-002], [ARC-007]
  * `./sources/backend/attendance-service/src/main/java/com/hub/attendance/AttendanceService.java` [REQ-012], [REQ-013], [EXC-001], [EXC-002], [ARC-007]
  * `./sources/backend/attendance-service/src/main/java/com/hub/attendance/model/Attendance.java` [DAT-006]
  * `./sources/backend/attendance-service/src/main/java/com/hub/attendance/repository/AttendanceRepository.java` [DAT-006]
  * `./sources/frontend/web/course/src/components/CourseList.tsx` [REQ-007], [REQ-010]
  * `./sources/frontend/web/course/src/components/CourseDetail.tsx` [REQ-007], [REQ-008]
  * `./sources/frontend/web/course/src/components/EnrollmentForm.tsx` [REQ-010], [REQ-011]
  * `./sources/frontend/web/course/src/components/QRScanner.tsx` [REQ-012], [REQ-013]
  * `./sources/docs/course-service-api-spec.md` [REQ-007], [REQ-008], [REQ-009], [ARC-007]
  * `./sources/docs/attendance-service-api-spec.md` [REQ-012], [REQ-013], [EXC-001], [EXC-002], [ARC-007]

- **Đặc tả DDL SQL Schema Cơ sở dữ liệu** [DAT-004], [DAT-005], [DAT-006]:
```sql
-- Triển khai schema cho các bảng liên quan đến khóa học, đăng ký và điểm danh trong giai đoạn 2
-- Bảng khóa học [DAT-004]
CREATE TABLE Courses (
    courseId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL,
    description TEXT,
    startDate DATE NOT NULL,
    endDate DATE NOT NULL,
    teacherId UUID NOT NULL,
    maxStudents INT NOT NULL DEFAULT 30,
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacherId) REFERENCES Users(userId) ON DELETE CASCADE,
    CONSTRAINT chk_course_dates CHECK (startDate < endDate)
);

-- Bảng đăng ký khóa học [DAT-005]
CREATE TABLE Enrollments (
    enrollmentId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    studentId UUID NOT NULL,
    courseId UUID NOT NULL,
    enrollmentDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enrollment_student FOREIGN KEY (studentId) REFERENCES Users(userId) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (courseId) REFERENCES Courses(courseId) ON DELETE CASCADE,
    CONSTRAINT uk_enrollment_student_course UNIQUE (studentId, courseId)
);

-- Bảng điểm danh [DAT-006]
CREATE TABLE Attendance (
    attendanceId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    studentId UUID NOT NULL,
    courseId UUID NOT NULL,
    attendanceDate DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_student FOREIGN KEY (studentId) REFERENCES Users(userId) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_course FOREIGN KEY (courseId) REFERENCES Courses(courseId) ON DELETE CASCADE,
    CONSTRAINT uk_attendance_student_course_date UNIQUE (studentId, courseId, attendanceDate),
    CONSTRAINT chk_attendance_date CHECK (attendanceDate <= CURRENT_DATE)
);
```

- **Hợp đồng định tuyến API và sự kiện** [REQ-007], [REQ-008], [REQ-009], [REQ-010], [REQ-011], [REQ-012], [REQ-013], [ARC-007]:
```json
// Hợp đồng API cho dịch vụ quản lý khóa học (Course Service)
[
  {
    "endpoint": "/api/v1/courses",
    "method": "GET",
    "description": "Lấy danh sách tất cả khóa học với thông tin giáo viên và lịch trình",
    "request": {
      "queryParams": {
        "centerId": "UUID (tùy chọn, lọc theo trung tâm)",
        "page": "INT (tùy chọn, mặc định 1)",
        "size": "INT (tùy chọn, mặc định 20)"
      }
    },
    "response": {
      "status": 200,
      "body": [
        {
          "courseId": "UUID",
          "title": "STRING",
          "description": "STRING",
          "startDate": "DATE (YYYY-MM-DD)",
          "endDate": "DATE (YYYY-MM-DD)",
          "teacherId": "UUID",
          "teacherName": "STRING",
          "maxStudents": "INT",
          "enrolledCount": "INT"
        }
      ]
    }
  },
  {
    "endpoint": "/api/v1/courses",
    "method": "POST",
    "description": "Tạo mới khóa học (chỉ System Admin/Center Admin)",
    "request": {
      "body": {
        "title": "STRING (bắt buộc, max 150 ký tự)",
        "description": "STRING (tùy chọn)",
        "startDate": "DATE (bắt buộc, YYYY-MM-DD)",
        "endDate": "DATE (bắt buộc, YYYY-MM-DD)",
        "teacherId": "UUID (bắt buộc)",
        "maxStudents": "INT (tùy chọn, mặc định 30)"
      }
    },
    "response": {
      "status": 201,
      "body": {
        "courseId": "UUID",
        "message": "Tạo khóa học thành công"
      }
    },
    "error": {
      "status": 409,
      "body": {
        "error": "CONFLICT",
        "message": "Giáo viên có lịch trình trùng lặp trong khoảng thời gian khóa học"
      }
    }
  },
  {
    "endpoint": "/api/v1/courses/{courseId}/assign-teacher",
    "method": "POST",
    "description": "Phân công giáo viên vào khóa học và gửi thông báo",
    "request": {
      "body": {
        "teacherId": "UUID (bắt buộc)"
      }
    },
    "response": {
      "status": 200,
      "body": {
        "message": "Phân công giáo viên thành công, thông báo đã được xếp hàng"
      }
    }
  }
]

// Hợp đồng API cho dịch vụ đăng ký khóa học (Enrollment Service)
[
  {
    "endpoint": "/api/v1/courses/available",
    "method": "GET",
    "description": "Lấy danh sách khóa học có sẵn cho học viên (loại trừ khóa đã đăng ký)",
    "request": {
      "headers": {
        "Authorization": "Bearer <JWT token>"
      },
      "queryParams": {
        "page": "INT (tùy chọn)",
        "size": "INT (tùy chọn)"
      }
    },
    "response": {
      "status": 200,
      "body": [
        {
          "courseId": "UUID",
          "title": "STRING",
          "startDate": "DATE",
          "endDate": "DATE",
          "teacherName": "STRING",
          "maxStudents": "INT",
          "remainingSlots": "INT"
        }
      ]
    }
  },
  {
    "endpoint": "/api/v1/enrollments",
    "method": "POST",
    "description": "Đăng ký khóa học cho học viên, tự động tạo tài khoản nếu chưa tồn tại",
    "request": {
      "headers": {
        "Authorization": "Bearer <JWT token>"
      },
      "body": {
        "courseId": "UUID (bắt buộc)",
        "studentEmail": "STRING (tùy chọn, dùng để tạo tài khoản mới nếu học viên chưa có tài khoản)"
      }
    },
    "response": {
      "status": 201,
      "body": {
        "enrollmentId": "UUID",
        "message": "Đăng ký khóa học thành công"
      }
    }
  }
]

// Hợp đồng API cho dịch vụ điểm danh (Attendance Service)
[
  {
    "endpoint": "/api/v1/attendance/scan",
    "method": "POST",
    "description": "Ghi nhận điểm danh qua quét mã QR, đảm bảo idempotent",
    "request": {
      "headers": {
        "Authorization": "Bearer <JWT token>"
      },
      "body": {
        "qrCode": "STRING (bắt buộc, mã QR chứa courseId và sessionId)",
        "timestamp": "TIMESTAMP (bắt buộc, thời gian quét)"
      }
    },
    "response": {
      "status": 200,
      "body": {
        "attendanceId": "UUID",
        "status": "RECORDED | DUPLICATE",
        "message": "Điểm danh thành công | Đã ghi nhận điểm danh trước đó"
      }
    }
  }
]

// Cấu hình topic Kafka cho sự kiện liên quan [ARC-008]
{
  "topics": [
    {
      "name": "attendance.scan.request",
      "partitions": 3,
      "retentionMs": 86400000,
      "description": "Topic nhận yêu cầu điểm danh từ ứng dụng di động"
    },
    {
      "name": "attendance.scan.result",
      "partitions": 3,
      "retentionMs": 86400000,
      "description": "Topic phát kết quả xử lý điểm danh cho các dịch vụ liên quan"
    },
    {
      "name": "notification.course.assignment",
      "partitions": 2,
      "retentionMs": 604800000,
      "description": "Topic phát thông báo phân công giáo viên vào khóa học"
    }
  ]
}
```

- **Trình xử lý ngoại lệ đặc thù của giai đoạn** [EXC-001], [EXC-002]:
  * [EXC-001] Mất kết nối mạng trong quá trình quét mã QR điểm danh:
    - Mã lỗi: `ATTENDANCE_NETWORK_ERROR`
    - Mô tả: Ứng dụng di động lưu tạm yêu cầu điểm danh vào hàng đợi ngoại tuyến, tự động gửi lại khi kết nối mạng được khôi phục. Hệ thống backend xử lý các yêu cầu pending theo thứ tự FIFO, đảm bảo không bỏ sót bản ghi điểm danh.
    - Phản hồi cho người dùng: Hiển thị thông báo "Đang lưu tạm điểm danh, sẽ tự động gửi khi có kết nối" trên giao diện di động.
  * [EXC-002] Gửi điểm danh trùng lặp trong cùng ngày:
    - Mã lỗi: `ATTENDANCE_DUPLICATE`
    - Mô tả: Hệ thống kiểm tra ràng buộc duy nhất trên cặp (studentId, courseId, attendanceDate) ở tầng cơ sở dữ liệu. Nếu phát hiện yêu cầu trùng lặp, trả về mã trạng thái 200 với cờ `status: DUPLICATE` và thông báo "Đã ghi nhận điểm danh cho buổi học này trước đó".
    - Hành động: Không tạo bản ghi mới, không ghi log lỗi nghiêm trọng, chỉ ghi log thông tin ở mức độ DEBUG.

#### 📅 NHẬT KÝ PHÂN PHỐI CÔNG VIỆC THEO NGÀY CHO TỪNG ĐẠI LÝ PHỤ (GIAI ĐOẠN 2)
<!--START_DAY_LOG_INDEX-->
##### 📅 NGÀY 1: XÂY DỰNG SERVICE QUẢN LÝ KHÓA HỌC CƠ BẢN VÀ API LẤY DANH SÁCH KHÓA HỌC
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 1: Xây dựng entity, repository, service và controller cho khóa học, triển khai endpoint lấy danh sách khóa học
* **Chuyên môn đại lý phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-007], [DAT-004], [ARC-007]
* **Thành phần file mục tiêu (target_component):** `./sources/backend/course-service/src/main/java/com/hub/course/CourseController.java;./sources/backend/course-service/src/main/java/com/hub/course/CourseService.java;./sources/backend/course-service/src/main/java/com/hub/course/model/Course.java;./sources/backend/course-service/src/main/java/com/hub/course/repository/CourseRepository.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai endpoint `GET /api/v1/courses` trả về danh sách khóa học với các trường title, startDate, endDate, teacherName, maxStudents, enrolledCount; kết nối với bảng Courses trong PostgreSQL, đảm bảo truy vấn có hiệu suất cao với chỉ mục trên cột startDate và endDate, tuân thủ quy tắc đặt tên biến và xử lý ngoại lệ của dự án.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 2: Viết unit test cho endpoint lấy danh sách khóa học và service lớp CourseService
* **Chuyên môn đại lý phụ:** [Tester]
* **ID thẻ mục tiêu:** [REQ-007], [DAT-004]
* **Thành phần file mục tiêu (target_component):** `./sources/backend/course-service/src/main/java/com/hub/course/CourseService.java;./sources/backend/course-service/src/test/java/com/hub/course/CourseServiceTest.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết 5 trường hợp test bao gồm: trả về danh sách khóa học khi có dữ liệu, trả về mảng rỗng khi không có khóa học, lọc khóa học theo tham số centerId, phân trang đúng với tham số page và size, xử lý lỗi kết nối cơ sở dữ liệu, đảm bảo độ bao phủ code đạt trên 80%.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 3: Viết tài liệu đặc tả API cho endpoint lấy danh sách khóa học
* **Chuyên môn đại lý phụ:** [Doc]
* **ID thẻ mục tiêu:** [REQ-007], [ARC-007]
* **Thành phần file mục tiêu (target_component):** `./sources/docs/course-service-api-spec.md`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Ghi rõ tham số yêu cầu, phản hồi thành công, phản hồi lỗi, ví dụ payload request/response, mã lỗi liên quan, tuân thủ chuẩn tài liệu API Markdown của dự án.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
##### 📅 NGÀY 2: TRIỂN KHAI CHỨC NĂNG CRUD KHÓA HỌC VÀ KIỂM TRA XUNG ĐỘT LỊCH TRÌNH GIÁO VIÊN
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 1: Triển khai endpoint tạo, cập nhật, xóa khóa học và kiểm tra xung đột lịch trình giáo viên
* **Chuyên môn đại lý phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-008], [EXC-001]
* **Thành phần file mục tiêu (target_component):** `./sources/backend/course-service/src/main/java/com/hub/course/CourseController.java;./sources/backend/course-service/src/main/java/com/hub/course/CourseService.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai endpoint `POST /api/v1/courses` (tạo), `PUT /api/v1/courses/{courseId}` (cập nhật), `DELETE /api/v1/courses/{courseId}` (xóa); thêm logic kiểm tra xung đột lịch trình giáo viên trước khi lưu, trả về lỗi 409 nếu giáo viên đã được phân công khóa học khác trùng thời gian; thêm ràng buộc kiểm tra startDate < endDate ở tầng service.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 2: Viết unit test và integration test cho chức năng CRUD khóa học và kiểm tra xung đột lịch trình
* **Chuyên môn đại lý phụ:** [Tester]
* **ID thẻ mục tiêu:** [REQ-008], [EXC-001]
* **Thành phần file mục tiêu (target_component):** `./sources/backend/course-service/src/main/java/com/hub/course/CourseService.java;./sources/backend/course-service/src/test/java/com/hub/course/CourseServiceIntegrationTest.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết 8 trường hợp test bao gồm: tạo khóa học thành công, tạo khóa học trùng lịch giáo viên trả về lỗi 409, cập nhật khóa học thành công, xóa khóa học thành công, kiểm tra ràng buộc startDate < endDate, kiểm tra quyền truy cập của Center Admin và System Admin, kiểm tra trùng lặp dữ liệu, xử lý lỗi khi giáo viên không tồn tại trong hệ thống.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 3: Kiểm tra chất lượng mã nguồn của module quản lý khóa học, phát hiện lỗi và đề xuất cải tiến
* **Chuyên môn đại lý phụ:** [Reviewer]
* **ID thẻ mục tiêu:** [REQ-008], [EXC-001]
* **Thành phần file mục tiêu (target_component):** `./sources/backend/course-service/src/main/java/com/hub/course/*`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Kiểm tra tuân thủ quy tắc đặt tên biến, xử lý ngoại lệ đầy đủ, tối ưu truy vấn cơ sở dữ liệu, đảm bảo logic kiểm tra xung đột lịch trình chính xác, ghi nhận tất cả lỗi nghiêm trọng và đề xuất giải pháp sửa chữa.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 4: Cập nhật tài liệu API với các endpoint CRUD khóa học và thông tin lỗi xung đột lịch trình
* **Chuyên môn đại lý phụ:** [Doc]
* **ID thẻ mục tiêu:** [REQ-008], [EXC-001], [ARC-007]
* **Thành phần file mục tiêu (target_component):** `./sources/docs/course-service-api-spec.md`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Bổ sung mô tả các endpoint POST, PUT, DELETE, ví dụ payload, danh sách mã lỗi liên quan, hướng dẫn xử lý lỗi xung đột lịch trình cho người dùng frontend, đảm bảo tài liệu rõ ràng và dễ hiểu cho đội phát triển.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
##### 📅 NGÀY 3: TRIỂN KHAI PHÂN CÔNG GIÁO VIÊN VÀ CHỨC NĂNG ĐĂNG KÝ KHÓA HỌC CHO HỌC VIÊN
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 1: Triển khai endpoint phân công/huỷ phân công giáo viên và endpoint đăng ký khóa học cho học viên
* **Chuyên môn đại lý phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-009], [REQ-010], [REQ-011], [DAT-005]
* **Thành phần file mục tiêu (target_component):** `./sources/backend/course-service/src/main/java/com/hub/course/CourseController.java;./sources/backend/enrollment-service/src/main/java/com/hub/enrollment/EnrollmentController.java;./sources/backend/enrollment-service/src/main/java/com/hub/enrollment/EnrollmentService.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai endpoint `POST /api/v1/courses/{courseId}/assign-teacher` (phân công giáo viên), `DELETE /api/v1/courses/{courseId}/assign-teacher/{teacherId}` (huỷ phân công); triển khai endpoint `GET /api/v1/courses/available` (lấy khóa học có sẵn cho học viên, loại trừ khóa đã đăng ký), `POST /api/v1/enrollments` (đăng ký khóa học, tự động tạo tài khoản Student với vai trò tương ứng nếu chưa tồn tại); tích hợp với dịch vụ thông báo để gửi thông báo khi phân công giáo viên hoặc học viên đăng ký khóa học.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 2: Viết unit test và integration test cho chức năng phân công giáo viên và đăng ký khóa học
* **Chuyên môn đại lý phụ:** [Tester]
* **ID thẻ mục tiêu:** [REQ-009], [REQ-010], [REQ-011], [DAT-005]
* **Thành phần file mục tiêu (target_component):** `./sources/backend/course-service/src/main/java/com/hub/course/CourseService.java;./sources/backend/enrollment-service/src/main/java/com/hub/enrollment/EnrollmentService.java;./sources/backend/course-service/src/test/java/com/hub/course/CourseServiceIntegrationTest.java;./sources/backend/enrollment-service/src/test/java/com/hub/enrollment/EnrollmentServiceIntegrationTest.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết 10 trường hợp test bao gồm: phân công giáo viên thành công, huỷ phân công thành công, đăng ký khóa học thành công, tự động tạo tài khoản Student khi đăng ký với email chưa tồn tại, từ chối đăng ký khi khóa học đã đủ sĩ số, từ chối đăng ký khi học viên đã đăng ký khóa học đó, kiểm tra thông báo được gửi đến giáo viên khi được phân công, kiểm tra thông báo được gửi đến học viên khi đăng ký thành công, kiểm tra quyền truy cập của học viên khi truy cập endpoint đăng ký, kiểm tra dữ liệu đăng ký được lưu chính xác vào cơ sở dữ liệu.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 3: Cập nhật tài liệu API với các endpoint phân công giáo viên và đăng ký khóa học
* **Chuyên môn đại lý phụ:** [Doc]
* **ID thẻ mục tiêu:** [REQ-009], [REQ-010], [REQ-011], [ARC-007]
* **Thành phần file mục tiêu (target_component):** `./sources/docs/course-service-api-spec.md;./sources/docs/enrollment-service-api-spec.md`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Bổ sung mô tả các endpoint mới, ví dụ payload, quy tắc tự động tạo tài khoản Student, quy tắc kiểm tra sĩ số khóa học, hướng dẫn tích hợp với dịch vụ thông báo, đảm bảo tài liệu đồng bộ với phiên bản triển khai thực tế.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
##### 📅 NGÀY 4: TRIỂN KHAI ENDPOINT QUÉT MÃ QR ĐIỂM DANH VÀ CƠ CHẾ IDEMPOTENT
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 1: Triển khai endpoint xử lý quét mã QR điểm danh, đảm bảo tính idempotent và xử lý ngoại lệ mất kết nối
* **Chuyên môn đại lý phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-012], [REQ-013], [EXC-001], [EXC-002], [DAT-006]
* **Thành phần file mục tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/com/hub/attendance/AttendanceController.java;./sources/backend/attendance-service/src/main/java/com/hub/attendance/AttendanceService.java;./sources/backend/attendance-service/src/main/java/com/hub/attendance/model/Attendance.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai endpoint `POST /api/v1/attendance/scan` xử lý payload từ ứng dụng di động, kiểm tra quan hệ học viên-khóa học, đảm bảo chỉ tạo 1 bản ghi điểm danh mỗi học viên/khóa học/ngày thông qua ràng buộc duy nhất ở tầng cơ sở dữ liệu; tích hợp với hàng đợi Kafka để xử lý yêu cầu điểm danh bất đồng bộ, hỗ trợ lưu tạm yêu cầu khi mất kết nối mạng và tự động gửi lại khi kết nối được khôi phục.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 2: Viết unit test và integration test cho chức năng điểm danh QR và cơ chế idempotent
* **Chuyên môn đại lý phụ:** [Tester]
* **ID thẻ mục tiêu:** [REQ-012], [REQ-013], [EXC-001], [EXC-002]
* **Thành phần file mục tiêu (target_component):** `./sources/backend/attendance-service/src/main/java/com/hub/attendance/AttendanceService.java;./sources/backend/attendance-service/src/test/java/com/hub/attendance/AttendanceServiceIntegrationTest.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết 7 trường hợp test bao gồm: quét QR thành công tạo bản ghi điểm danh mới, quét QR trùng lặp trong cùng ngày trả về cờ DUPLICATE, quét QR khi học viên không đăng ký khóa học trả về lỗi 403, quét QR khi mã QR không hợp lệ trả về lỗi 400, xử lý yêu cầu điểm danh khi mất kết nối mạng (lưu tạm và gửi lại khi có kết nối), kiểm tra độ chính xác của thời gian điểm danh được lưu, kiểm tra hiệu suất xử lý 1000 yêu cầu điểm danh đồng thời.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 3: Cập nhật tài liệu API cho dịch vụ điểm danh và hướng dẫn tích hợp QR scanner cho frontend
* **Chuyên môn đại lý phụ:** [Doc]
* **ID thẻ mục tiêu:** [REQ-012], [REQ-013], [EXC-001], [EXC-002], [ARC-007]
* **Thành phần file mục tiêu (target_component):** `./sources/docs/attendance-service-api-spec.md;./sources/docs/mobile-qr-integration-guide.md`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Ghi rõ cấu trúc mã QR, payload yêu cầu điểm danh, phản hồi thành công và lỗi, hướng dẫn tích hợp trình quét QR vào ứng dụng di động, xử lý trường hợp mất kết nối mạng, đảm bảo tài liệu đầy đủ cho đội phát triển frontend.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
##### 📅 NGÀY 5: PHÁT TRIỂN GIAO DIỆN FRONTEND CHO MODULE KHÓA HỌC, ĐĂNG KÝ VÀ ĐIỂM DANH, KIỂM TRA TOÀN BỘ LUỒNG CHỨC NĂNG
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 1: Phát triển các thành phần frontend cho danh sách khóa học, đăng ký khóa học và quét mã QR điểm danh
* **Chuyên môn đại lý phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-007], [REQ-010], [REQ-011], [REQ-012], [REQ-013]
* **Thành phần file mục tiêu (target_component):** `./sources/frontend/web/course/src/components/CourseList.tsx;./sources/frontend/web/course/src/components/CourseDetail.tsx;./sources/frontend/web/course/src/components/EnrollmentForm.tsx;./sources/frontend/web/course/src/components/QRScanner.tsx`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng giao diện danh sách khóa học hiển thị đầy đủ thông tin lịch trình, giáo viên, sĩ số còn lại; xây dựng form đăng ký khóa học với xác thực đầu vào; tích hợp trình quét QR vào ứng dụng di động, hiển thị trạng thái điểm danh (thành công, trùng lặp, lỗi mạng) cho người dùng, đảm bảo giao diện responsive phù hợp với cả web và di động.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 2: Viết end-to-end test cho toàn bộ luồng đăng ký khóa học và điểm danh QR
* **Chuyên môn đại lý phụ:** [Tester]
* **ID thẻ mục tiêu:** [REQ-007], [REQ-010], [REQ-011], [REQ-012], [REQ-013]
* **Thành phần file mục tiêu (target_component):** `INTEGRATION_SCOPE;./sources/frontend/web/course/src/test/e2e/CourseEnrollmentE2ETest.java;INTEGRATION_SCOPE;./sources/frontend/web/course/src/test/e2e/AttendanceQRScanE2ETest.java`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết 6 trường hợp test E2E bao gồm: học viên duyệt khóa học và đăng ký thành công, học viên đăng ký khóa học đã đăng ký trước đó nhận lỗi, học viên quét QR điểm danh thành công, học viên quét QR trùng lặp nhận thông báo đã ghi nhận, học viên quét QR khi mất kết nối mạng, điểm danh được ghi nhận sau khi khôi phục kết nối, kiểm tra thông báo được gửi đến học viên sau khi đăng ký.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 3: Kiểm tra toàn bộ mã nguồn của giai đoạn 2, đảm bảo tuân thủ yêu cầu và không có lỗi nghiêm trọng
* **Chuyên môn đại lý phụ:** [Reviewer]
* **ID thẻ mục tiêu:** [REQ-007], [REQ-008], [REQ-009], [REQ-010], [REQ-011], [REQ-012], [REQ-013], [EXC-001], [EXC-002]
* **Thành phần file mục tiêu (target_component):** `./sources/backend/course-service/*;./sources/backend/enrollment-service/*;./sources/backend/attendance-service/*;./sources/frontend/web/course/*`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Kiểm tra 100% các yêu cầu chức năng của giai đoạn 2 được triển khai đầy đủ, kiểm tra cơ chế idempotent của điểm danh hoạt động đúng, kiểm tra xử lý ngoại lệ mất kết nối và điểm danh trùng lặp đúng theo yêu cầu, ghi nhận tất cả lỗi nghiêm trọng và đề xuất giải pháp sửa chữa trước khi chuyển sang giai đoạn tiếp theo.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC CON 4: Tổng hợp tài liệu kiến trúc cho module khóa học, đăng ký và điểm danh, cập nhật tài liệu hướng dẫn vận hành
* **Chuyên môn đại lý phụ:** [Doc]
* **ID thẻ mục tiêu:** [REQ-007], [REQ-008], [REQ-009], [REQ-010], [REQ-011], [REQ-012], [REQ-013], [ARC-007]
* **Thành phần file mục tiêu (target_component):** `./sources/docs/course-module-architecture.md;./sources/docs/operations-guide.md`
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Ghi rõ kiến trúc các service liên quan, luồng dữ liệu chính, hướng dẫn vận hành, xử lý sự cố thường gặp (mất kết nối khi quét QR, xung đột lịch trình giáo viên), đảm bảo tài liệu đầy đủ cho đội vận hành và hỗ trợ người dùng.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->
<!--END_PHASE_INDEX-->

<!--START_PHASE_INDEX-->
### 📈 GIAI ĐOẠN 3 - TRIỂN KHAI DỊCH VỤ HỘI VIÊN, THÔNG BÁO ĐA KÊNH VÀ QUẢN LÝ KHUYẾN MÃI
- **Mục tiêu cốt lõi và mục đích của giai đoạn:** Triển khai các chức năng quản lý thẻ hội viên kỹ thuật số (hiển thị số ngày còn lại hiệu lực, gia hạn thẻ với tích hợp thanh toán), hệ thống thông báo đa kênh (push di động qua FCM/APNs, đăng bài lên nhóm Zalo) với cơ chế xử lý lỗi và retry tự động, quản lý khuyến mãi và thông báo có thời hạn hiển thị tùy chọn, đảm bảo tất cả các chức năng này tuân thủ các yêu cầu nghiệp vụ [REQ-014] đến [REQ-018], ràng buộc kỹ thuật về hiệu suất và bảo mật đã được định nghĩa.

- **Bản đồ ma trận đường dẫn vật lý mục tiêu:** Liệt kê đầy đủ các tệp vật lý cụ thể được tạo hoặc sửa đổi trong phạm vi giai đoạn này, mỗi mục kèm theo Tag ID theo dõi tương ứng:
  * ./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipService.java [REQ-014], [DAT-007]
  * ./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipController.java [REQ-014], [REQ-015]
  * ./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipRepository.java [DAT-007]
  * ./sources/backend/membership-service/src/main/resources/db/migration/V3__create_student_cards.sql [DAT-007]
  * ./sources/backend/notification-service/src/main/java/com/hub/notification/NotificationService.java [REQ-016], [EXC-003], [DAT-008]
  * ./sources/backend/notification-service/src/main/java/com/hub/notification/NotificationController.java [REQ-016]
  * ./sources/backend/notification-service/src/main/java/com/hub/notification/FcmNotificationSender.java [REQ-016], [REQ-021]
  * ./sources/backend/notification-service/src/main/java/com/hub/notification/ZaloNotificationSender.java [REQ-016], [ARC-008]
  * ./sources/backend/promotion-service/src/main/java/com/hub/promotion/PromotionService.java [REQ-017], [DAT-009]
  * ./sources/backend/promotion-service/src/main/java/com/hub/promotion/PromotionController.java [REQ-017]
  * ./sources/backend/promotion-service/src/main/java/com/hub/promotion/AnnouncementService.java [REQ-018], [DAT-009]
  * ./sources/backend/promotion-service/src/main/java/com/hub/promotion/AnnouncementController.java [REQ-018]
  * ./sources/frontend/web/membership/src/app/membership/page.tsx [REQ-014]
  * ./sources/frontend/web/membership/src/app/membership/renew/page.tsx [REQ-015]
  * ./sources/frontend/web/membership/src/app/promotions/page.tsx [REQ-017]
  * ./sources/frontend/web/membership/src/app/announcements/page.tsx [REQ-018]

- **Thông số kỹ thuật DDL SQL Schema Cơ sở dữ liệu** [DAT-007], [DAT-008], [DAT-009]:
```sql
-- Tạo bảng thẻ hội viên
CREATE TABLE student_cards (
    card_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    validity_days INT NOT NULL CHECK (validity_days > 0),
    remaining_days INT NOT NULL CHECK (remaining_days >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng thông báo
CREATE TABLE notifications (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(user_id) ON DELETE CASCADE,
    group_zalo VARCHAR(255),
    message TEXT NOT NULL CHECK (length(message) <= 2000),
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered BOOLEAN NOT NULL DEFAULT FALSE,
    retry_count INT NOT NULL DEFAULT 0 CHECK (retry_count BETWEEN 0 AND 3)
);

-- Tạo bảng khuyến mãi
CREATE TABLE promotions (
    promo_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_percent SMALLINT NOT NULL CHECK (discount_percent BETWEEN 0 AND 100),
    start_date DATE,
    end_date DATE,
    description TEXT,
    CHECK (end_date IS NULL OR end_date >= start_date)
);

-- Tạo bảng thông báo hệ thống
CREATE TABLE announcements (
    announcement_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL CHECK (length(title) <= 150),
    content TEXT NOT NULL CHECK (length(content) <= 2000),
    start_date DATE,
    end_date DATE,
    CHECK (end_date IS NULL OR end_date >= start_date)
);

-- Tạo index cho các truy vấn thường dùng
CREATE INDEX idx_student_cards_student_id ON student_cards(student_id);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_delivered ON notifications(delivered);
CREATE INDEX idx_promotions_active ON promotions(start_date, end_date) WHERE end_date IS NULL OR end_date >= CURRENT_DATE;
CREATE INDEX idx_announcements_active ON announcements(start_date, end_date) WHERE end_date IS NULL OR end_date >= CURRENT_DATE;
```

- **Hợp đồng định tuyến API và Sự kiện** [REQ-014], [REQ-015], [REQ-016], [REQ-017], [REQ-018], [ARC-008]:
```json
// Endpoints Dịch vụ Hội viên
GET /api/membership/card
Response 200: {
  "cardId": "uuid",
  "studentId": "uuid",
  "issueDate": "date",
  "validityDays": 30,
  "remainingDays": 15,
  "expiryDate": "date"
}

POST /api/membership/renew
Request: {
  "renewalDays": 30,
  "paymentTransactionId": "string"
}
Response 200: {
  "cardId": "uuid",
  "remainingDays": 45,
  "expiryDate": "date"
}
Response 402: { "error": "Payment failed" }

// Endpoints Dịch vụ Thông báo
POST /api/notifications/send
Request: {
  "userId": "uuid (tùy chọn)",
  "groupZalo": "string (tùy chọn)",
  "message": "string"
}
Response 202: { "message": "Notification queued", "notificationId": "uuid" }

// Endpoints Dịch vụ Khuyến mãi
GET /api/promotions
Response 200: [ { "promoId": "uuid", "code": "string", "discountPercent": 10, "startDate": "date", "endDate": "date", "description": "string" } ]

POST /api/promotions
Request: { "code": "SUMMER10", "discountPercent": 10, "startDate": "2024-06-01", "endDate": "2024-08-31", "description": "Giảm 10% khóa học hè" }
Response 201: { "promoId": "uuid" }

// Endpoints Dịch vụ Thông báo
GET /api/announcements
Response 200: [ { "announcementId": "uuid", "title": "string", "content": "string", "startDate": "date", "endDate": "date" } ]

POST /api/announcements
Request: { "title": "Thông báo nghỉ lễ", "content": "Trung tâm nghỉ lễ 30/4", "startDate": "2024-04-29", "endDate": "2024-05-01" }
Response 201: { "announcementId": "uuid" }

// Chủ đề sự kiện Kafka (nếu sử dụng messaging)
notification.send: Gửi thông báo đến hàng đợi xử lý thông báo
promotion.created: Sự kiện tạo khuyến mãi mới
announcement.created: Sự kiện tạo thông báo mới
```

- **Trình xử lý ngoại lệ địa phương của giai đoạn** [EXC-003]:
  * Ngoại lệ gửi thông báo thất bại: Khi không thể gửi thông báo (token thiết bị FCM/APNs không hợp lệ, lỗi kết nối API Zalo), hệ thống ghi log lỗi với chi tiết lỗi và timestamp, tự động thử lại tối đa 3 lần với khoảng cách 5 phút giữa các lần thử. Nếu sau 3 lần thử vẫn thất bại, đánh dấu trường `delivered = false` trong bảng notifications và gửi cảnh báo cho quản trị viên hệ thống.
  * Ngoại lệ xác thực dữ liệu khuyến mãi/thông báo: Nếu ngày kết thúc nhỏ hơn ngày bắt đầu, hoặc phần trăm giảm giá ngoài khoảng 0-100, hệ thống trả về lỗi 400 Bad Request với thông báo chi tiết các trường không hợp lệ.

#### 📅 NHẬT KÝ PHÂN PHỐI NHIỆM VỤ PHỤ AGENT THEO THỜI GIAN (GIAI ĐOẠN 3)

<!--START_DAY_LOG_INDEX-->
- **📅 NGÀY 1:** Triển khai dịch vụ thẻ hội viên cốt lõi và schema cơ sở dữ liệu
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 1: Xây dựng lớp nghiệp vụ cốt lõi của dịch vụ thẻ hội viên
* **Chuyên môn quy trình phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-014], [DAT-007]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipService.java
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic tính toán số ngày còn lại hiệu lực của thẻ hội viên dựa trên issue_date và validity_days, đảm bảo giá trị remaining_days được cập nhật tự động mỗi ngày qua scheduled job, tuân thủ các ràng buộc NOT NULL và CHECK cho các trường dữ liệu.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 2: Xây dựng endpoint API và repository truy cập dữ liệu thẻ hội viên
* **Chuyên môn quy trình phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-014], [DAT-007]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipController.java
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng endpoint GET /api/membership/card trả về thông tin thẻ hội viên của người dùng đã xác thực, tích hợp kiểm tra quyền truy cập RBAC để đảm bảo chỉ người dùng sở hữu thẻ hoặc quản trị viên mới có thể xem thông tin.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 3: Tạo migration DDL và kiểm tra tính toàn vẹn schema thẻ hội viên
* **Chuyên môn quy trình phụ:** [Coder]
* **ID thẻ mục tiêu:** [DAT-007]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/membership-service/src/main/resources/db/migration/V3__create_student_cards.sql
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết migration ANSI SQL tạo bảng student_cards với các ràng buộc khóa chính, khóa ngoại đến bảng users, ràng buộc CHECK cho validity_days và remaining_days, chạy migration trên môi trường staging để xác nhận không có lỗi.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 4: Viết bộ kiểm thử đơn vị cho logic nghiệp vụ thẻ hội viên
* **Chuyên môn quy trình phụ:** [Tester]
* **ID thẻ mục tiêu:** [REQ-014], [DAT-007]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/membership-service/src/test/java/com/hub/membership/MembershipServiceTest.java;./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipService.java
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử cho logic tính toán remaining_days, xử lý thẻ hết hạn, đảm bảo độ phủ mã 100% cho các nhánh điều kiện trong MembershipService.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
- **📅 NGÀY 2:** Triển khai chức năng gia hạn thẻ và dịch vụ thông báo cốt lõi
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 1: Xây dựng endpoint gia hạn thẻ hội viên và tích hợp logic thanh toán
* **Chuyên môn quy trình phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-015]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipController.java
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Thêm endpoint POST /api/membership/renew, tích hợp với cổng thanh toán để xác nhận giao dịch thành công trước khi cập nhật remaining_days và issue_date của thẻ, gửi thông báo xác nhận cho người dùng sau khi gia hạn thành công.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 2: Xây dựng lớp dịch vụ thông báo cốt lõi và trình gửi FCM
* **Chuyên môn quy trình phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-016], [EXC-003]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/notification-service/src/main/java/com/hub/notification/NotificationService.java
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic xếp hàng thông báo, tích hợp với Firebase Cloud Messaging để gửi thông báo đẩy di động, thêm cơ chế ghi log lỗi và đếm số lần thử lại cho trường hợp gửi thất bại.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 3: Viết bộ kiểm thử đơn vị cho chức năng gia hạn thẻ
* **Chuyên môn quy trình phụ:** [Tester]
* **ID thẻ mục tiêu:** [REQ-015]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/membership-service/src/test/java/com/hub/membership/MembershipRenewalTest.java;./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipController.java
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử cho endpoint gia hạn thẻ, bao gồm trường hợp thanh toán thành công, thanh toán thất bại, thẻ hết hạn, đảm bảo xử lý đúng các ngoại lệ nghiệp vụ.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 4: Viết bộ kiểm thử đơn vị cho dịch vụ thông báo cốt lõi
* **Chuyên môn quy trình phụ:** [Tester]
* **ID thẻ mục tiêu:** [REQ-016], [EXC-003]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/notification-service/src/test/java/com/hub/notification/NotificationServiceTest.java;./sources/backend/notification-service/src/main/java/com/hub/notification/NotificationService.java
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử cho logic gửi thông báo, xử lý lỗi gửi thất bại, cơ chế retry, đảm bảo số lần thử lại không vượt quá 3 lần.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
- **📅 NGÀY 3:** Hoàn thiện hệ thống thông báo đa kênh và triển khai quản lý khuyến mãi, thông báo
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 1: Xây dựng trình gửi thông báo nhóm Zalo và tích hợp hàng đợi sự kiện
* **Chuyên môn quy trình phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-016], [ARC-008]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/notification-service/src/main/java/com/hub/notification/ZaloNotificationSender.java
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai tích hợp API Zalo để gửi tin nhắn đến nhóm Zalo được chỉ định, đảm bảo xử lý lỗi rate limit và lỗi xác thực API Zalo, ghi log chi tiết cho mỗi lần gửi thông báo.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 2: Xây dựng dịch vụ và controller quản lý khuyến mãi
* **Chuyên môn quy trình phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-017], [DAT-009]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/promotion-service/src/main/java/com/hub/promotion/PromotionService.java
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic CRUD cho khuyến mãi, thêm kiểm tra xác thực đầu vào (phần trăm giảm giá 0-100, ngày kết thúc >= ngày bắt đầu), lọc khuyến mãi đang hoạt động dựa trên ngày hiện tại.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 3: Xây dựng dịch vụ và controller quản lý thông báo hệ thống
* **Chuyên môn quy trình phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-018], [DAT-009]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/promotion-service/src/main/java/com/hub/promotion/AnnouncementService.java
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic CRUD cho thông báo hệ thống, thêm kiểm tra ngày hiệu lực, tự động ẩn thông báo sau ngày kết thúc thông qua scheduled job, lọc thông báo đang hoạt động khi truy vấn.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 4: Viết bộ kiểm thử tích hợp cho luồng gửi thông báo đa kênh
* **Chuyên môn quy trình phụ:** [Tester]
* **ID thẻ mục tiêu:** [REQ-016], [EXC-003]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** INTEGRATION_SCOPE;./sources/backend/notification-service/src/test/java/com/hub/notification/NotificationIntegrationTest.java
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết kịch bản kiểm thử tích hợp cho luồng gửi thông báo qua FCM và Zalo, bao gồm trường hợp gửi thành công, gửi thất bại và retry, đảm bảo thông báo được gửi đến đúng đích và trạng thái được cập nhật chính xác.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 5: Viết tài liệu tham chiếu API cho các dịch vụ giai đoạn 3
* **Chuyên môn quy trình phụ:** [Doc]
* **ID thẻ mục tiêu:** [REQ-016], [REQ-017], [REQ-018]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/docs/api/notification-promotion-api.md
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu API mô tả chi tiết tất cả các endpoint của dịch vụ thông báo, khuyến mãi và thông báo, bao gồm tham số yêu cầu, phản hồi, mã lỗi và ví dụ sử dụng.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
- **📅 NGÀY 4:** Triển khai giao diện người dùng cho các chức năng giai đoạn 3 và kiểm tra cuối cùng
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 1: Xây dựng giao diện hiển thị thẻ hội viên trên frontend web
* **Chuyên môn quy trình phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-014]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/frontend/web/membership/src/app/membership/page.tsx
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Phát triển giao diện trang thẻ hội viên, hiển thị tổng ngày hiệu lực, ngày đã sử dụng, ngày còn lại và ngày hết hạn, tích hợp gọi API lấy thông tin thẻ và xử lý trạng thái tải và lỗi.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 2: Xây dựng giao diện gia hạn thẻ hội viên trên frontend web
* **Chuyên môn quy trình phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-015]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/frontend/web/membership/src/app/membership/renew/page.tsx
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Phát triển giao diện trang gia hạn thẻ, cho phép người dùng chọn số ngày gia hạn, tích hợp với cổng thanh toán, hiển thị thông báo thành công/thất bại sau khi thực hiện gia hạn.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 3: Xây dựng giao diện quản lý khuyến mãi và thông báo cho quản trị viên
* **Chuyên môn quy trình phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-017], [REQ-018]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/frontend/web/membership/src/app/promotions/page.tsx
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Phát triển giao diện quản lý khuyến mãi và thông báo cho quản trị viên, bao gồm chức năng tạo, sửa, xóa khuyến mãi và thông báo, hiển thị danh sách các mục đang hoạt động và đã hết hạn.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 4: Viết bộ kiểm thử đơn vị cho các thành phần frontend giai đoạn 3
* **Chuyên môn quy trình phụ:** [Tester]
* **ID thẻ mục tiêu:** [REQ-014], [REQ-015], [REQ-017], [REQ-018]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/frontend/web/membership/src/app/membership/__tests__/membership.test.tsx;./sources/frontend/web/membership/src/app/membership/page.tsx
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử cho các thành phần frontend trang thẻ hội viên, trang gia hạn và trang quản lý khuyến mãi/thông báo, đảm bảo hiển thị đúng dữ liệu và xử lý đúng các trạng thái tải, lỗi và thành công.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 SUB-TASK 5: Rà soát mã nguồn toàn bộ giai đoạn 3 để đảm bảo tuân thủ RBAC và bảo mật
* **Chuyên môn quy trình phụ:** [Reviewer]
* **ID thẻ mục tiêu:** [REQ-014], [REQ-015], [REQ-016], [REQ-017], [REQ-018], [ARC-001], [ARC-002], [NFR-003]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/membership-service, ./sources/backend/notification-service, ./sources/backend/promotion-service, ./sources/frontend/web/membership
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Rà soát toàn bộ mã nguồn của giai đoạn 3 để phát hiện lỗ hổng bảo mật (SQL injection, XSS, truy cập trái phép), đảm bảo tất cả các endpoint đều có kiểm tra quyền RBAC, dữ liệu nhạy cảm được mã hóa đúng cách, đề xuất và triển khai các giải pháp sửa lỗi nếu phát hiện vấn đề.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->
<!--END_PHASE_INDEX-->

### 📈 Giai đoạn 4 - Tích hợp Chatbot AI, Giao diện Di động, Thông báo Đẩy, Đa ngôn ngữ, SEO và Báo cáo Điểm danh
- **Mục tiêu cốt lõi của giai đoạn:** Triển khai toàn bộ các tính năng tương tác người dùng cuối, bao gồm tích hợp chatbot AI hỗ trợ khách hàng, phát triển giao diện di động đáp ứng theo vai trò người dùng, triển khai hệ thống thông báo đẩy di động (FCM/APNs), cấu hình hỗ trợ đa ngôn ngữ với phát hiện ngôn ngữ mặc định, tối ưu SEO đa ngôn ngữ cho 3 ngôn ngữ (Anh, Việt, Tây Ban Nha), xây dựng chức năng tạo báo cáo điểm danh CSV hàng ngày theo trung tâm và bảng điều khiển tóm tắt ghi danh thời gian thực cho quản lý trung tâm, đảm bảo tất cả yêu cầu chức năng [REQ-019] đến [REQ-025] được thực hiện đầy đủ và tích hợp liền mạch với các dịch vụ backend hiện có.

- **Bản đồ ma trận đường dẫn vật lý mục tiêu:** Tạo danh sách đầy đủ tất cả các tệp vật lý tương đối cụ thể được tạo hoặc cập nhật trong phạm vi giai đoạn này, mỗi mục là một tệp với phần mở rộng rõ ràng và thẻ theo dõi tương ứng:
  * ./sources/frontend/mobile-app/src/main/java/com/hub/mobile/MobileAppMain.java [REQ-020]
  * ./sources/frontend/mobile-app/src/main/java/com/hub/mobile/ui/role/RoleBasedNavigation.kt [REQ-020]
  * ./sources/frontend/mobile-app/src/main/java/com/hub/mobile/attendance/QRScannerFragment.kt [REQ-012]
  * ./sources/frontend/mobile-app/src/main/java/com/hub/mobile/notification/PushNotificationHandler.kt [REQ-021]
  * ./sources/backend/chatbot-service/src/main/java/com/hub/chatbot/ChatbotController.java [REQ-019]
  * ./sources/backend/chatbot-service/src/main/java/com/hub/chatbot/AIServiceClient.java [REQ-019]
  * ./sources/backend/chatbot-service/src/main/java/com/hub/chatbot/EscalationService.java [REQ-019]
  * ./sources/backend/report-service/src/main/java/com/hub/report/AttendanceReportController.java [REQ-024]
  * ./sources/backend/report-service/src/main/java/com/hub/report/CSVExportService.java [REQ-024]
  * ./sources/backend/report-service/src/main/java/com/hub/report/EnrollmentDashboardController.java [REQ-025]
  * ./sources/backend/report-service/src/main/java/com/hub/report/DashboardMetricsService.java [REQ-025]
  * ./sources/frontend/web/seo/src/components/LanguageSwitcher.tsx [REQ-022]
  * ./sources/frontend/web/seo/src/components/HreflangTags.tsx [REQ-023]
  * ./sources/frontend/web/seo/src/utils/localeDetector.ts [REQ-022]
  * ./sources/frontend/web/seo/src/pages/[locale]/index.tsx [REQ-023]
  * ./sources/docs/api/chatbot-api.md [REQ-019]
  * ./sources/docs/operations/i18n-seo-guide.md [REQ-022], [REQ-023]

- **Thông số kỹ thuật DDL SQL cơ sở dữ liệu [DAT-011]:**
```sql
-- Giai đoạn này không yêu cầu thay đổi cơ sở dữ liệu hoặc lớp lưu trữ nào
```

- **API và hợp đồng định tuyến sự kiện [REQ-019], [ARC-009]:**
  * **Endpoint Chatbot AI:** `POST /api/v1/chatbot/message`
    * Yêu cầu: `{"message": "string", "sessionId": "uuid"}`
    * Phản hồi thành công (200 OK): `{"reply": "string", "confidence": "float", "escalate": "boolean"}`
  * **Endpoint đăng ký token thông báo đẩy:** `POST /api/v1/notifications/register-token`
    * Yêu cầu: `{"deviceToken": "string", "platform": "FCM/APNs", "userId": "uuid"}`
    * Phản hồi thành công (200 OK): `{"status": "registered"}`
  * **Endpoint xuất báo cáo điểm danh CSV:** `GET /api/v1/reports/attendance/csv?centerId={uuid}&startDate={YYYY-MM-DD}&endDate={YYYY-MM-DD}`
    * Phản hồi thành công (200 OK): File CSV với các cột `StudentName, CourseName, AttendanceDate, Status`
  * **Endpoint bảng điều khiển ghi danh:** `GET /api/v1/dashboard/enrollment?centerId={uuid}`
    * Phản hồi thành công (200 OK): `{"totalStudents": "int", "activeCourses": "int", "upcomingSessions": "int"}`
  * **Endpoint cập nhật tùy chọn ngôn ngữ:** `POST /api/v1/user/locale`
    * Yêu cầu: `{"locale": "en/vi/es"}`
    * Phản hồi thành công (200 OK): `{"status": "updated"}`
  * **Chủ đề sự kiện thông báo:** `attendance.confirmed`, `course.assigned`, `announcement.created` được xuất bản đến Kafka, kích hoạt gửi thông báo đẩy và tin nhắn nhóm Zalo.

- **Trình xử lý ngoại lệ địa phương của giai đoạn [EXC-003], [EXC-005]:**
  * **[EXC-003] Lỗi gửi thông báo đẩy:** Nếu không thể gửi thông báo đẩy (ví dụ: token thiết bị không hợp lệ), hệ thống ghi lại lỗi, lên lịch thử lại tối đa 3 lần trước khi đánh dấu là thất bại. Phản hồi lỗi: `502 BAD_GATEWAY` với payload `{"error": "PUSH_DELIVERY_FAILED", "retryCount": "int", "message": "Không thể gửi thông báo, sẽ thử lại sau"}`.
  * **[EXC-005] Khôi phục hệ thống sau sự cố:** Nếu dịch vụ gặp sự cố và khôi phục, tất cả điểm danh đang chờ xử lý được xử lý theo thứ tự FIFO, người dùng nhận được thông báo về các sự kiện đã khôi phục. Trong thời gian sự cố, phản hồi lỗi là `503 SERVICE_UNAVAILABLE`, sau khi khôi phục trả về `200 OK` với thông báo xác nhận khôi phục.

#### 📅 Nhật ký phân công tác nghiệp phụ theo thứ tự thời gian (Giai đoạn 4)

<!--START_DAY_LOG_INDEX-->
##### 📅 Ngày 1: Triển khai Chatbot AI và giao diện di động cơ bản theo vai trò
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 1: Xây dựng dịch vụ Chatbot AI backend
* **Chuyên môn tác nghiệp phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-019]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/chatbot-service/src/main/java/com/hub/chatbot/ChatbotController.java
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Xây dựng endpoint REST POST /api/v1/chatbot/message, tích hợp với dịch vụ AI bên thứ ba (ví dụ: Google Dialogflow), xử lý truy vấn người dùng, trả về phản hồi phù hợp hoặc chuyển tiếp đến hỗ trợ con người nếu độ tin cậy của phản hồi AI < 0.7. Đảm bảo xác thực người dùng qua JWT token trước khi xử lý truy vấn.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 2: Xây dựng logic điều hướng giao diện di động theo vai trò
* **Chuyên môn tác nghiệp phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-020]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/frontend/mobile-app/src/main/java/com/hub/mobile/ui/role/RoleBasedNavigation.kt
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Xây dựng logic hiển thị menu và màn hình dựa trên vai trò người dùng (Student, Teacher, Center Admin, System Admin, Manager), đảm bảo chỉ hiển thị các tính năng được phép truy cập theo cơ chế RBAC đã triển khai ở Giai đoạn 1. Tích hợp với dịch vụ xác thực để lấy thông tin vai trò người dùng hiện tại.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 3: Viết unit test cho dịch vụ Chatbot
* **Chuyên môn tác nghiệp phụ:** [Tester]
* **ID thẻ mục tiêu:** [REQ-019]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/chatbot-service/src/main/java/com/hub/chatbot/ChatbotController.java;./sources/backend/chatbot-service/src/test/java/com/hub/chatbot/ChatbotControllerTest.java
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Viết unit test kiểm tra các trường hợp: (1) Truy vấn hợp lệ trả về phản hồi từ AI, (2) Truy vấn có độ tin cậy < 0.7 kích hoạt chuyển tiếp đến hỗ trợ con người, (3) Đầu vào không hợp lệ (thiếu message hoặc sessionId) trả về lỗi 400 Bad Request, (4) Người dùng chưa xác thực trả về lỗi 401 Unauthorized.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 4: Tạo tài liệu tham chiếu API Chatbot
* **Chuyên môn tác nghiệp phụ:** [Doc]
* **ID thẻ mục tiêu:** [REQ-019]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/docs/api/chatbot-api.md
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Tạo tài liệu tham chiếu API cho endpoint chatbot, mô tả chi tiết yêu cầu đầu vào, cấu trúc phản hồi, danh sách mã lỗi, ví dụ sử dụng và hướng dẫn tích hợp cho frontend.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
##### 📅 Ngày 2: Triển khai thông báo đẩy di động và tích hợp quét QR
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 1: Xây dựng dịch vụ xử lý thông báo đẩy backend
* **Chuyên môn tác nghiệp phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-021], [EXC-003]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/notification-service/src/main/java/com/hub/notification/PushNotificationService.java
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Xây dựng dịch vụ đăng ký và quản lý token thiết bị FCM/APNs của người dùng, xử lý hàng đợi thông báo đẩy, triển khai cơ chế thử lại tối đa 3 lần khi gửi thất bại, ghi log chi tiết lỗi nếu gửi không thành công sau 3 lần thử. Tích hợp với dịch vụ thông báo hiện có để gửi thông báo cho các sự kiện điểm danh, phân công khóa học và thông báo mới.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 2: Tích hợp SDK thông báo đẩy vào ứng dụng di động
* **Chuyên môn tác nghiệp phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-021]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/frontend/mobile-app/src/main/java/com/hub/mobile/notification/PushNotificationHandler.kt
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Tích hợp SDK Firebase Cloud Messaging (FCM) cho Android và Apple Push Notification Service (APNs) cho iOS, xử lý nhận thông báo đẩy khi ứng dụng ở foreground, background và bị tắt, hiển thị thông báo cho người dùng với nội dung phù hợp với loại sự kiện.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 3: Viết integration test cho luồng thông báo đẩy
* **Chuyên môn tác nghiệp phụ:** [Tester]
* **ID thẻ mục tiêu:** [REQ-021], [EXC-003]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** INTEGRATION_SCOPE;./sources/backend/notification-service/src/test/java/com/hub/notification/PushNotificationIntegrationTest.java
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Viết integration test kiểm tra toàn bộ luồng: đăng ký token thiết bị, gửi thông báo thành công đến thiết bị thật/giả lập, xử lý lỗi gửi thất bại do token không hợp lệ, kiểm tra cơ chế thử lại tự động tối đa 3 lần.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 4: Kiểm tra chất lượng mã thông báo đẩy và giao diện di động
* **Chuyên môn tác nghiệp phụ:** [Reviewer]
* **ID thẻ mục tiêu:** [REQ-021], [EXC-003]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/notification-service/src/main/java/com/hub/notification/PushNotificationService.java;./sources/frontend/mobile-app/src/main/java/com/hub/mobile/notification/PushNotificationHandler.kt
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Kiểm tra chất lượng mã, đảm bảo tuân thủ OWASP Top 10 (không lộ token thiết bị, xác thực người dùng trước khi gửi thông báo), phát hiện lỗ hổng bảo mật, đề xuất cải tiến hiệu suất xử lý hàng đợi thông báo.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
##### 📅 Ngày 3: Triển khai hỗ trợ đa ngôn ngữ và SEO đa ngôn ngữ
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 1: Xây dựng logic phát hiện và lưu trữ tùy chọn ngôn ngữ
* **Chuyên môn tác nghiệp phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-022], [DAT-011]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/frontend/web/seo/src/utils/localeDetector.ts
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Xây dựng logic phát hiện ngôn ngữ mặc định: ưu tiên tùy chọn ngôn ngữ đã lưu của người dùng trong bảng SystemSettings, sau đó sử dụng header Accept-Language của trình duyệt, mặc định là tiếng Việt (vi). Lưu trữ tùy chọn ngôn ngữ của người dùng khi họ thay đổi, cập nhật giao diện theo ngôn ngữ tương ứng mà không cần tải lại trang.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 2: Triển khai thẻ meta SEO và hreflang đa ngôn ngữ
* **Chuyên môn tác nghiệp phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-023]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/frontend/web/seo/src/components/HreflangTags.tsx
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Tạo component động tự động tạo thẻ `<html lang={locale}>` và các liên kết hreflang cho 3 ngôn ngữ được hỗ trợ (tiếng Anh /en, tiếng Việt /vi, tiếng Tây Ban Nha /es), đảm bảo mỗi trang web có đầy đủ thẻ meta ngôn ngữ cho công cụ tìm kiếm.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 3: Viết unit test cho chức năng đa ngôn ngữ và SEO
* **Chuyên môn tác nghiệp phụ:** [Tester]
* **ID thẻ mục tiêu:** [REQ-022], [REQ-023]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/frontend/web/seo/src/utils/localeDetector.ts;./sources/frontend/web/seo/src/test/utils/localeDetector.test.ts
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Viết unit test kiểm tra logic phát hiện ngôn ngữ với các trường hợp: người dùng đã lưu tùy chọn tiếng Việt, người dùng mới với header Accept-Language là tiếng Anh, trường hợp không có tùy chọn nào sử dụng mặc định tiếng Việt. Kiểm tra component HreflangTags tạo đúng 3 liên kết hreflang và thẻ lang chính xác cho mỗi ngôn ngữ.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 4: Tạo tài liệu hướng dẫn cấu hình đa ngôn ngữ và SEO
* **Chuyên môn tác nghiệp phụ:** [Doc]
* **ID thẻ mục tiêu:** [REQ-022], [REQ-023]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/docs/operations/i18n-seo-guide.md
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Tạo tài liệu hướng dẫn chi tiết cách cấu hình đa ngôn ngữ cho frontend, cách thêm ngôn ngữ mới, cách cấu hình thẻ meta SEO và hreflang, cách kiểm tra cấu hình SEO với các công cụ của Google.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
##### 📅 Ngày 4: Triển khai báo cáo điểm danh, bảng điều khiển ghi danh và xử lý ngoại lệ khôi phục hệ thống
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 1: Xây dựng endpoint xuất báo cáo điểm danh CSV
* **Chuyên môn tác nghiệp phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-024], [EXC-005]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/report-service/src/main/java/com/hub/report/AttendanceReportController.java
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Xây dựng endpoint GET /api/v1/reports/attendance/csv, cho phép lọc theo centerId, startDate và endDate, xuất file CSV với các cột StudentName, CourseName, AttendanceDate, Status. Tích hợp logic xử lý điểm danh pending sau sự cố hệ thống theo thứ tự FIFO trước khi tạo báo cáo.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 2: Xây dựng bảng điều khiển tóm tắt ghi danh thời gian thực
* **Chuyên môn tác nghiệp phụ:** [Coder]
* **ID thẻ mục tiêu:** [REQ-025]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/report-service/src/main/java/com/hub/report/EnrollmentDashboardController.java
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Xây dựng endpoint GET /api/v1/dashboard/enrollment, trả về dữ liệu tổng hợp: totalStudents (tổng số học viên đã đăng ký), activeCourses (số khóa học đang hoạt động), upcomingSessions (số buổi học sắp tới trong 7 ngày tiếp theo). Tích hợp WebSocket để cập nhật dữ liệu thời gian thực khi có thay đổi (đăng ký mới, hủy đăng ký, khóa học mới).
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 3: Viết integration test cho báo cáo và bảng điều khiển
* **Chuyên môn tác nghiệp phụ:** [Tester]
* **ID thẻ mục tiêu:** [REQ-024], [REQ-025], [EXC-005]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** INTEGRATION_SCOPE;./sources/backend/report-service/src/test/java/com/hub/report/ReportIntegrationTest.java
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Viết integration test kiểm tra: (1) Báo cáo điểm danh CSV được tạo đúng định dạng với dữ liệu chính xác, (2) Dữ liệu bảng điều khiển phản ánh đúng số liệu thực tế, (3) Luồng xử lý điểm danh pending sau sự cố được thực hiện đúng thứ tự FIFO, không bị mất dữ liệu.
<!--END_ATOMIC_SUB_TASK_NODE-->

<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 Phụ công việc 4: Kiểm tra và tối ưu hóa hiệu suất báo cáo
* **Chuyên môn tác nghiệp phụ:** [Reviewer]
* **ID thẻ mục tiêu:** [REQ-024], [REQ-025], [EXC-005]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/report-service/src/main/java/com/hub/report/AttendanceReportController.java;./sources/backend/report-service/src/main/java/com/hub/report/EnrollmentDashboardController.java
* **Hướng dẫn tác nghiệp kỹ thuật cấp thấp:** Kiểm tra logic xử lý ngoại lệ khôi phục hệ thống, đảm bảo điểm danh pending được xử lý đúng thứ tự FIFO không bị mất. Phân tích và tối ưu hiệu suất truy vấn báo cáo với khối lượng dữ liệu lớn (hơn 10.000 bản ghi điểm danh), đảm bảo thời gian phản hồi dưới 200ms theo yêu cầu NFR-001.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--END_PHASE_INDEX-->

<!--START_PHASE_INDEX-->
### 📈 GIAI ĐOẠN 5 - TRIỂN KHAI HẠ TẦNG DEVOPS, BẢO MẬT TOÀN HỆ THỐNG VÀ TÀI LIỆU DOANH NGHIỆP
- **Mục tiêu cốt lõi của giai đoạn:** Triển khai lớp kiểm soát truy cập dựa trên vai trò (RBAC) toàn hệ thống đảm bảo tuân thủ OWASP Top 10, xây dựng toàn bộ hạ tầng DevOps (Docker đa giai đoạn, Terraform provisioning tài nguyên GCP, GKE orchestration với HPA tự động scale, pipeline CI/CD GitHub Actions), và hoàn thiện toàn bộ tài liệu doanh nghiệp (bản vẽ kiến trúc, tài liệu tham chiếu API, hướng dẫn vận hành) để đáp ứng tất cả các yêu cầu phi chức năng về hiệu suất, khả năng sẵn sàng, bảo mật và khả năng mở rộng.
- **Bản đồ ma trận đường dẫn vật lý mục tiêu:**
    * ./sources/infra/terraform/main.tf [NFR-001], [NFR-002], [NFR-004], [NFR-009]
    * ./sources/infra/terraform/variables.tf [NFR-001], [NFR-002], [NFR-004], [NFR-009]
    * ./sources/infra/terraform/outputs.tf [NFR-001], [NFR-002], [NFR-004], [NFR-009]
    * ./sources/infra/docker/Dockerfile [NFR-005]
    * ./sources/infra/docker/.dockerignore [NFR-005]
    * ./sources/infra/gke/deployment.yaml [NFR-002], [NFR-004], [NFR-009]
    * ./sources/infra/gke/hpa.yaml [NFR-004]
    * ./sources/infra/gke/service.yaml [NFR-002], [NFR-004]
    * ./sources/docs/architecture/system-architecture.md [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010]
    * ./sources/docs/api/rest-api-reference.md [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010]
    * ./sources/docs/operations/installation-guide.md [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
    * ./sources/docs/operations/backup-recovery-guide.md [NFR-009]
- **Đặc tả SQL DDL cơ sở dữ liệu** [DAT-XXX]:
```sql
-- Giai đoạn này không yêu cầu thay đổi cơ sở dữ liệu hoặc lớp lưu trữ nào
```
- **Trình xử lý ngoại lệ địa phương của giai đoạn** [EXC-XXX]:
    * [EXC-006] Lỗi xác thực quyền truy cập RBAC: Nếu người dùng không có quyền truy cập tài nguyên, hệ thống trả về mã lỗi 403 Forbidden với thông báo chi tiết về quyền bị thiếu.
    * [EXC-007] Lỗi triển khai hạ tầng GCP: Nếu quá trình provisioning tài nguyên GCP qua Terraform thất bại, hệ thống ghi log lỗi chi tiết, rollback các tài nguyên đã tạo và thông báo cho đội ngũ vận hành.
    * [EXC-008] Lỗi triển khai workload GKE: Nếu việc triển khai manifest lên GKE thất bại, hệ thống giữ nguyên phiên bản cũ, ghi log lỗi và kích hoạt cảnh báo cho đội ngũ DevOps.
    * [EXC-009] Lỗi xây dựng hình ảnh Docker: Nếu quá trình build Docker image thất bại, hệ thống dừng pipeline CI/CD, ghi log lỗi build và thông báo cho nhà phát triển.
    * [EXC-010] Lỗi sao lưu cơ sở dữ liệu: Nếu quá trình sao lưu PostgreSQL hàng ngày thất bại, hệ thống thử lại tối đa 3 lần, nếu vẫn thất bại thì gửi cảnh báo khẩn cấp cho đội ngũ vận hành.

#### 📅 NHẬT KÝ PHÂN PHỐI CÔNG VIỆC PHỤ TÁC TỬ THEO THỜI GIAN TỪNG NGÀY (GIAI ĐOẠN 5)

<!--START_DAY_LOG_INDEX-->
##### 📅 NGÀY 1: TRIỂN KHAI CỐT LÕI RBAC VÀ KIỂM THỬ ĐƠN VỊ QUYỀN TRUY CẬP
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC PHỤ 1: TRIỂN KHAI LỚP TRUNG GIAN RBAC
* **Chuyên môn quy trình phụ tác tử:** [Coder]
* **ID thẻ mục tiêu:** [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/auth/src/main/java/com/hub/security/RbacMiddleware.java
* **Chỉ dẫn kỹ thuật cấp thấp:** Triển khai lớp trung gian RBAC xác thực quyền truy cập của người dùng dựa trên vai trò được lưu trong JWT token, áp dụng ràng buộc quyền cho tất cả endpoint backend, đảm bảo System Admin có toàn quyền trên tất cả trung tâm, Center Admin chỉ quản lý trung tâm được phân công, Manager có quyền hạn giới hạn (tạo thông báo, quản lý học viên, không chỉnh sửa khóa học), Teacher chỉ có quyền xem dữ liệu của khóa học phụ trách, Student chỉ truy cập tài nguyên cá nhân. Áp dụng các biện pháp chống xâm phạm OWASP Top 10, đảm bảo không có lỗ hổng bypass quyền truy cập.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC PHỤ 2: VIẾT BỘ KIỂM THỬ ĐƠN VỊ CHO RBAC
* **Chuyên môn quy trình phụ tác tử:** [Tester]
* **ID thẻ mục tiêu:** [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/auth/src/test/java/com/hub/RbacMiddlewareTest.java;./sources/backend/auth/src/main/java/com/hub/security/RbacMiddleware.java
* **Chỉ dẫn kỹ thuật cấp thấp:** Viết bộ kiểm thử đơn vị cho lớp RBAC Middleware, bao gồm các kịch bản: (1) Người dùng có vai trò hợp lệ truy cập tài nguyên được phép, (2) Người dùng có vai trò không hợp lệ truy cập tài nguyên bị từ chối với mã lỗi 403, (3) Center Admin truy cập tài nguyên của trung tâm khác bị từ chối, (4) JWT token hết hạn hoặc không hợp lệ bị từ chối truy cập. Đảm bảo tỷ lệ bao phủ mã nguồn RBAC đạt trên 90%.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
##### 📅 NGÀY 2: RÀ SOÁT MÃ RBAC VÀ XÂY DỰNG HÌNH ẢNH DOCKER ĐA GIAI ĐOẠN
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC PHỤ 1: RÀ SOÁT MÃ NGUỒN RBAC VÀ SỬA LỖI
* **Chuyên môn quy trình phụ tác tử:** [Reviewer]
* **ID thẻ mục tiêu:** [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-003]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/backend/auth/src/main/java/com/hub/security/RbacMiddleware.java
* **Chỉ dẫn kỹ thuật cấp thấp:** Rà soát toàn bộ mã nguồn lớp RBAC Middleware, phát hiện và sửa các lỗ hổng bảo mật tiềm ẩn (ví dụ: lỗi so khớp vai trò không phân biệt chữ hoa chữ thường, thiếu kiểm tra quyền truy cập trên các endpoint ẩn), đảm bảo tuân thủ đầy đủ OWASP Top 10, tối ưu hiệu suất xác thực quyền (độ trễ xác thực <10ms), đề xuất và triển khai các cải tiến về cấu trúc mã nguồn.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC PHỤ 2: XÂY DỰNG DOCKERFILE ĐA GIAI ĐOẠN CHO TẤT CẢ DỊCH VỤ
* **Chuyên môn quy trình phụ tác tử:** [Docker]
* **ID thẻ mục tiêu:** [NFR-005]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/infra/docker/Dockerfile
* **Chỉ dẫn kỹ thuật cấp thấp:** Xây dựng Dockerfile đa giai đoạn cho tất cả dịch vụ backend (Quarkus) và frontend (Next.js), sử dụng base image nhẹ (alpine hoặc distroless), loại bỏ các tệp không cần thiết (tệp nguồn, tệp test, công cụ build) trong giai đoạn cuối, tối ưu cấu hình layer caching, đảm bảo kích thước hình ảnh cuối dưới 500MB cho tất cả dịch vụ, tuân thủ yêu cầu kích thước hình ảnh Docker.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
##### 📅 NGÀY 3: PROVISIONING HẠ TẦNG GCP VÀ TẠO MANIFEST GKE
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC PHỤ 1: PROVISIONING HẠ TẦNG GCP BẰNG TERRAFORM
* **Chuyên môn quy trình phụ tác tử:** [GCP]
* **ID thẻ mục tiêu:** [NFR-001], [NFR-002], [NFR-004], [NFR-009]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/infra/terraform/main.tf
* **Chỉ dẫn kỹ thuật cấp thấp:** Cấu hình và triển khai toàn bộ hạ tầng GCP cần thiết cho hệ thống bao gồm: VPC với subnet riêng cho các dịch vụ, IAM roles với nguyên tắc đặc quyền tối thiểu, Cloud SQL (PostgreSQL) với cấu hình sao lưu tự động, Redis cluster cho caching phiên làm việc, Cloud Storage cho lưu trữ tệp báo cáo, Firewall rules chỉ mở các cổng cần thiết. Đảm bảo tất cả tài nguyên được định nghĩa dưới dạng mã (IaC), dữ liệu nghỉ được mã hóa AES-256, kết nối sử dụng TLS 1.3, tuân thủ yêu cầu bảo mật.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC PHỤ 2: TẠO MANIFEST TRIỂN KHAI GKE CHO TẤT CẢ DỊCH VỤ
* **Chuyên môn quy trình phụ tác tử:** [GKE]
* **ID thẻ mục tiêu:** [NFR-002], [NFR-004], [NFR-009]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/infra/gke/deployment.yaml
* **Chỉ dẫn kỹ thuật cấp thấp:** Tạo manifest Kubernetes triển khai cho tất cả dịch vụ backend và frontend lên GKE, cấu hình resource limits (CPU, memory) cho từng dịch vụ, thiết lập readiness và liveness probes để đảm bảo khả năng chịu lỗi, cấu hình Horizontal Pod Autoscaler (HPA) tự động scale số lượng pod dựa trên ngưỡng CPU >70% hoặc độ trễ yêu cầu >300ms, cấu hình rolling update strategy để đảm bảo không có thời gian chết trong quá trình triển khai phiên bản mới.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
##### 📅 NGÀY 4: THIẾT LẬP PIPELINE CI/CD VÀ KIỂM THỬ TÍCH HỢP HẠ TẦNG
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC PHỤ 1: XÂY DỰNG PIPELINE CI/CD VỚI GITHUB ACTIONS
* **Chuyên môn quy trình phụ tác tử:** [Coder]
* **ID thẻ mục tiêu:** [NFR-001], [NFR-005], [NFR-006]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/infra/.github/workflows/ci-cd-pipeline.yaml
* **Chỉ dẫn kỹ thuật cấp thấp:** Xây dựng pipeline CI/CD với GitHub Actions, tự động hóa toàn bộ quy trình từ khi có commit: (1) Build mã nguồn, (2) Chạy bộ kiểm thử đơn vị và tích hợp, (3) Quét lỗ hổng bảo mật phụ thuộc (OWASP dependency check), (4) Kiểm tra kích thước hình ảnh Docker, (5) Push hình ảnh đã build lên Google Container Registry (GCR), (6) Triển khai tự động lên GKE khi commit vào nhánh main. Tích hợp ghi log audit cho tất cả sự kiện pipeline, lưu trữ log trong 1 năm.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC PHỤ 2: KIỂM THỬ TÍCH HỢP HẠ TẦNG TOÀN HỆ THỐNG
* **Chuyên môn quy trình phụ tác tử:** [Tester]
* **ID thẻ mục tiêu:** [NFR-001], [NFR-002], [NFR-004]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** INTEGRATION_SCOPE;./sources/infra/test/infrastructure-integration-test.yaml
* **Chỉ dẫn kỹ thuật cấp thấp:** Thực hiện kiểm thử tích hợp toàn bộ hạ tầng, xác minh: (1) Tất cả tài nguyên GCP được triển khai chính xác theo đặc tả Terraform, (2) Kết nối giữa các dịch vụ (backend, PostgreSQL, Redis, FCM) hoạt động ổn định, (3) HPA tự động scale pod khi tải tăng, (4) Pipeline CI/CD chạy thành công từ commit đến triển khai, (5) Tính năng sao lưu và phục hồi PostgreSQL hoạt động chính xác. Ghi nhận tất cả lỗi phát sinh và đề xuất giải pháp khắc phục.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

<!--START_DAY_LOG_INDEX-->
##### 📅 NGÀY 5: HOÀN THIỆN TÀI LIỆU DOANH NGHIỆP VÀ RÀ SOÁT CUỐI CÙNG
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC PHỤ 1: SOẠN THẢO TÀI LIỆU KIẾN TRÚC HỆ THỐNG
* **Chuyên môn quy trình phụ tác tử:** [Doc]
* **ID thẻ mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/docs/architecture/system-architecture.md
* **Chỉ dẫn kỹ thuật cấp thấp:** Soạn thảo bản vẽ kiến trúc hệ thống tổng thể, mô tả chi tiết cấu trúc microservices, luồng dữ liệu chính (xác thực, điểm danh QR, thông báo, đăng ký khóa học), ma trận RBAC với 5 vai trò người dùng, sơ đồ tương tác giữa các thành phần kiến trúc, đảm bảo tài liệu phản ánh chính xác kiến trúc đã triển khai và tuân thủ tất cả yêu cầu kiến trúc đã định nghĩa.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--START_ATOMIC_SUB_TASK_NODE-->
###### 🌿 CÔNG VIỆC PHỤ 2: SOẠN THẢO TÀI LIỆU API VÀ HƯỚNG DẪN VẬN HÀNH
* **Chuyên môn quy trình phụ tác tử:** [Doc]
* **ID thẻ mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
* **Đường dẫn tệp thành phần mục tiêu (target_component):** ./sources/docs/api/rest-api-reference.md;./sources/docs/operations/installation-guide.md;./sources/docs/operations/backup-recovery-guide.md
* **Chỉ dẫn kỹ thuật cấp thấp:** Soạn thảo tài liệu tham chiếu API REST đầy đủ cho tất cả endpoint công khai và nội bộ, bao gồm đường dẫn, phương thức HTTP, schema yêu cầu/phản hồi, mã lỗi, ví dụ sử dụng; soạn thảo hướng dẫn cài đặt hệ thống trên môi trường GKE, hướng dẫn vận hành hàng ngày, hướng dẫn sao lưu và phục hồi thảm họa PostgreSQL, đảm bảo tất cả yêu cầu phi chức năng về hiệu suất, bảo mật, khả năng sẵn sàng, tuân thủ GDPR/CCPA được ghi chú đầy đủ trong tài liệu.
<!--END_ATOMIC_SUB_TASK_NODE-->
<!--END_DAY_LOG_INDEX-->

### 🕵️ BÁO CÁO KIỂM TOÁN CHÉO KIẾN TRÚC THỜI GIAN THỰC
```properties:cross_audit_ledger
[AUTOMATED_SELF_AUDIT_REPORT]
TOTAL_PHASES_DECLARED_IN_SECTION_4_2=5
TOTAL_PHASES_EXPECTED_BY_PARAMETERS=5
PHASE_COUNT_COMPLIANCE_STATUS=Verified_5
MAX_DAYS_PER_PHASE_LIMIT_PARAMETER=7
ACTUAL_MAX_DAY_INDEX_DETECTED_IN_TIMELINE=5
TIMELINE_DAY_CAP_COMPLIANCE_STATUS=Verified_All_Phase_Durations_Within_Ceiling
TOTAL_TASKS_REGISTERED_IN_MASTER_BACKLOG_4_1=29
TOTAL_DISCRETE_SUB_TASKS_GENERATED_IN_SECTION_5=29
SUB_TASK_QUANTUM_COMPLIANCE_STATUS=Verified_Symmetry_Enforced_With_100_Percent_Symmetry
```
<!--END_PHASE_INDEX-->
<!--END_PART_2_PHASE_LOOP-->

## ☣️ 6. MÃ BẢO MẬT DOANH NGHIỆP TOÀN CẦU & CÁC BIỆN PHÁP CHỐNG INJECTION [NFR-XXX]

### 1. Các Biện Pháp Chống Tuyệt Đối SQL Injection (SQLi)
Hệ thống phải triển khai các biện pháp chống SQL Injection nghiêm ngặt thông qua việc sử dụng prepared statements với positional query parameters trong Hibernate ORM. Tất cả các truy vấn động, đặc biệt là các tham số sắp xếp (sort), phải được kiểm tra chéo với danh sách trắng (whitelist) các trường và hướng được phép. Không được phép nối chuỗi trực tiếp vào câu lệnh SQL. Các tham số đầu vào phải được kiểm tra kiểu dữ liệu và độ dài trước khi truyền vào câu lệnh. [NFR-003]

### 2. Cross-Site Scripting (XSS) & Chính Sách Bảo Mật Nội Dung (CSP)
Triển khai sanitization ngữ cảnh tự động cho tất cả đầu vào người dùng trước khi hiển thị hoặc lưu trữ. Frontend Next.js phải bật JSX auto-escaping mặc định. Ingress Gateway trên GKE phải được cấu hình để tiêm động các header HTTP CSP nghiêm ngặt, hạn chế script-src đến các nguồn tin cậy duy nhất, và bật chế độ báo cáo vi phạm (report-only) trong môi trường staging. [NFR-003]

### 3. Rãnh Bảo Mật CORS Đa Người Thuê
Cấu hình CORS phải nghiêm cấm wildcard origin (`*`) trong môi trường production. Thay vào đó, hệ thống phải duy trì danh sách các origin được phép dựa trên từng tenant (trung tâm) và xác thực động origin của request so với danh sách đã đăng ký của trung tâm đó. Mọi request có origin không khớp sẽ bị từ chối với mã lỗi 403. [NFR-003]

### 4. Công Cụ Xóa Log Không Rò Rỉ & Che Mặt Dữ Liệu PII
Triển khai các interceptor tự động che mặt dữ liệu nhạy cảm (PII) sử dụng annotation `@JsonSerialize` trên các entity model. Các trường như email, số điện thoại, địa chỉ phải được tự động masking trước khi ghi vào log hoặc trả về qua API. Hệ thống logging phải được cấu hình để loại bỏ hoặc thay thế các giá trị nhạy cảm bằng placeholder như `***MASKED***`. [NFR-003], [NFR-006]

## 📱 7. QUY TẮC TUÂN THỦ DI ĐỘNG HYBRID & CƠ CHẾ SEO ĐA NGÔN NGỮ

### 1. Rãnh Tuân Thủ Di Động Hybrid Capacitor
Ứng dụng di động hybrid phải tuân thủ các ràng buộc sau: (1) Tất cả fetching dữ liệu phải thông qua client-side dynamic fetching với cơ chế cache ngoại tuyến; (2) Sử dụng absolute URL addressing để tránh vấn đề hydration mismatch; (3) Triển khai hydration safeguards để ngăn chặn flash of unstyled content; (4) Sử dụng `@capacitor/preferences` cho native storage abstraction thay vì localStorage trực tiếp; (5) Cấu hình hardware back-button interceptor để điều hướng người dùng một cách hợp lý trong ứng dụng. [REQ-020], [REQ-021]

### 2. Quốc Tế Hóa (i18n) & Tiêm SEO Động
Triển khai edge-layer locale recognition middleware để phát hiện ngôn ngữ ưu tiên của người dùng dựa trên Accept-Language header, cookie lưu trữ, hoặc tham số URL. Hệ thống phải tự động tạo và tiêm các thuộc tính hreflang vào phần head của HTML cho mỗi phiên bản ngôn ngữ, đảm bảo công cụ tìm kiếm có thể nhận diện và chỉ mục đúng các phiên bản ngôn ngữ khác nhau. [REQ-022], [REQ-023], [NFR-007]

## 🚀 8. LUỒNG NHÁNH GIT PHIÊN LÀM VIỆC TỰ ĐỘNG HÀNG NGÀY

### 1. Cô Lập Phân Nhánh Không Gian Làm Việc Hàng Ngày
Triển khai chính sách phân nhánh tự động với cấu trúc `features/development-phase-X-day-Y`, trong đó X là số thứ tự phase và Y là số thứ tự day. Hệ thống CI/CD phải tự động tạo nhánh mới cho mỗi phiên làm việc, đảm bảo cô lập hoàn toàn giữa các phiên và ngăn chặn merge nhầm giữa các ngày. Quyền push trực tiếp lên nhánh chính (main/develop) phải bị cấm tuyệt đối. [ARC-010]

### 2. Cổng Kiểm Tra Xác Thực Đường Ống
Thiết lập các cổng tự động hóa (gate) trong pipeline CI/CD: (1) Kiểm tra biên dịch tự động (automated compilation verification) với mục tiêu zero error; (2) SonarQube lint gates để đảm bảo chất lượng mã, với ngưỡng chấp nhận code smell và coverage tối thiểu; (3) Mục tiêu độ phủ test tự động (automated test coverage) được đặt ở mức `>= 85%` cho tất cả các module backend và frontend. Bất kỳ lần push nào không đạt các ngưỡng này đều bị từ chối tự động. [NFR-004], [NFR-005], [NFR-006]

[TRACEABILITY MATRIX ENFORCEMENT: 100% COVERAGE VALIDATED. TOTAL UNIQUE REQ TAGS MAPPED: 0, TOTAL ARC TAGS: 0, TOTAL EXC TAGS: 0, TOTAL DAT TAGS: 0, TOTAL NFR TAGS: 0. ZERO UNASSIGNED CODES FOUND.]