# Xây dựng dịch vụ thông báo, khuyến mãi, và API di động 3: <!--PHASE_NAME_START-->Xây dựng dịch vụ thông báo, khuyến mãi, và API di động<!--PHASE_NAME_END-->

## 📊 Document Control

| Mục | Chi tiết |
| :--- | :--- |
| **ID Kiến trúc** | ARCH-20260804165526 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 3 |
| **Tên Giai đoạn** | <!--PHASE_NAME_START-->Xây dựng dịch vụ thông báo, khuyến mãi, và API di động<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn này triển khai các dịch vụ thông báo push và Zalo, khuyến mãi, thông báo nội bộ, chatbot AI, và API di động, đồng thời thực hiện kiểm thử, xử lý ngoại lệ và bảo mật cho toàn bộ hệ thống.<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Baseline) |
| **Ngày/Giờ** | 2026/08/04 16:55:26 |
| **Tác giả** | Enterprise System Architect (SA Agent) |
| **Phê duyệt** | Pending Technical Governance Review |

## 1. Phạm vi và mục tiêu của Giai đoạn
Giai đoạn 3 tập trung vào việc triển khai các dịch vụ thông báo push và Zalo, khuyến mãi, thông báo nội bộ, chatbot AI, và API di động. Các dịch vụ này sẽ được xây dựng với kiến trúc microservices, sử dụng Quarkus, PostgreSQL, Redis, Firebase Admin SDK, và Zalo API. Mỗi dịch vụ sẽ được bảo vệ bằng JWT, tuân thủ OWASP Top 10, và được kiểm thử đầy đủ với coverage ≥ 90 %. Ngoài ra, các ngoại lệ và tính năng bảo mật sẽ được xử lý và ghi nhận theo chuẩn audit.

## 2. Phạm vi kỹ thuật cho phép & Giới hạn thư mục (tệp, đường dẫn và điểm cuối)
- **Thư mục**  
  - `./sources/backend/notification-service`  
  - `./sources/backend/promotion-service`  
  - `./sources/backend/announcement-service`  
  - `./sources/backend/chatbot-service`  
  - `./sources/backend/mobile-service`  
  - `./sources/backend/database/migrations`  
- **Điểm cuối API**  
  - `POST /api/notifications` → tạo thông báo  
  - `POST /api/promotions` → tạo khuyến mãi  
  - `POST /api/announcements` → tạo thông báo nội bộ  
  - `POST /api/chatbot/message` → gửi tin nhắn chatbot  
  - `GET /api/mobile/cards` → lấy thông tin thẻ di động

## 3. Hướng dẫn chức năng dành cho Sub-Agent
- **Coder**: Xây dựng controller, service, repository, tích hợp Firebase, Zalo, và AI; áp dụng OWASP Top 10 bảo mật cho API và dữ liệu; đảm bảo mã nguồn tuân thủ chuẩn OWASP và thực hiện unit test.  
- **Tester**: Viết và thực thi test API, kiểm tra i18n, SEO meta tags, bảo mật; thực hiện integration test cho mobile-service; đảm bảo coverage ≥ 90 %.  
- **Reviewer**: Đánh giá code quality, kiểm tra exception handling, performance, và bảo mật; thực hiện static analysis và OWASP review; ghi nhận các vấn đề bảo mật và đề xuất cải tiến.

## 4. Định nghĩa Hoàn thành Giai đoạn (DoD)
- Tất cả yêu cầu [REQ-016]–[REQ-025] và dữ liệu [DAT-007]–[DAT-011] được triển khai và kiểm thử.  
- Coverage test ≥ 90 % cho các module notification, promotion, announcement, chatbot, mobile.  
- Kiểm tra OWASP (SQLi, XSS, CSRF, JWT) đạt 100 %.  
- Tất cả tag ID được map đầy đủ, không còn tag chưa được sử dụng.

## 5. DAY-BY-DAY ARCHITECTURAL EXECUTION LOGS

### DAY 1: <!--DAY_HEADER_START-->XÂY ĐẾN DỊCH VỤ THÔNG BÁO VÀ MOBILE API<!--DAY_HEADER_END-->

#### SUB-TASK 1.1: Xây dựng controller, service, repository, và tích hợp Firebase, Zalo, đồng thời áp dụng OWASP Top 10 bảo mật cho API và dữ liệu
##### Sub-Agent được giao: Coder
##### Yêu cầu thành phần & kỹ thuật:
* **Đường dẫn mục tiêu**: ./sources/backend/notification-service
* **Thẻ mã theo dõi**: <!--START_TAGS-->[REQ-016], [REQ-017], [REQ-018], [REQ-019], [DAT-007], [DAT-008]<!--END_TAGS-->

#### SUB-TASK 1.2: Xây dựng promotion-service với mã code duy nhất, kiểm tra trùng lặp, áp dụng OWASP
##### Sub-Agent được giao: Coder
##### Yêu cầu thành phần & kỹ thuật:
* **Đường dẫn mục tiêu**: ./sources/backend/promotion-service
* **Thẻ mã theo dõi**: <!--START_TAGS-->[REQ-017], [DAT-009]<!--END_TAGS-->

#### SUB-TASK 1.3: Xây dựng announcement-service, kiểm tra trùng lặp, bảo mật
##### Sub-Agent được giao: Coder
##### Yêu cầu thành phần & kỹ thuật:
* **Đường dẫn mục tiêu**: ./sources/backend/announcement-service
* **Thẻ mã theo dõi**: <!--START_TAGS-->[REQ-018], [DAT-010]<!--END_TAGS-->

#### SUB-TASK 1.4: Xây dựng chatbot-service, tích hợp AI, bảo mật
##### Sub-Agent được giao: Coder
##### Yêu cầu thành phần & kỹ thuật:
* **Đường dẫn mục tiêu**: ./sources/backend/chatbot-service
* **Thẻ mã theo dõi**: <!--START_TAGS-->[REQ-019]<!--END_TAGS-->

#### SUB-TASK 1.5: Xây dựng mobile-service, API, i18n, SEO, bảo mật
##### Sub-Agent được giao: Coder
##### Yêu cầu thành phần & kỹ thuật:
* **Đường dẫn mục tiêu**: ./sources/backend/mobile-service
* **Thẻ mã theo dõi**: <!--START_TAGS-->[REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [DAT-011]<!--END_TAGS-->

### DAY 2: <!--DAY_HEADER_START-->KIỂM THỬ MOBILE API<!--DAY_HEADER_END-->

#### SUB-TASK 2.1: Viết test API, kiểm tra i18n, SEO meta tags, bảo mật
##### Sub-Agent được giao: Tester
##### Yêu cầu thành phần & kỹ thuật:
* **Đường dẫn mục tiêu**: ./sources/backend/mobile-service;./sources/backend/mobile-service/src/test/java/com/membershiphub/mobile/MobileServiceTest.java
* **Thẻ mã theo dõi**: <!--START_TAGS-->[REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [DAT-011]<!--END_TAGS-->

### DAY 3: <!--DAY_HEADER_START-->ĐÁNH GIÁ BẢO MẬT VÀ XỬ LÝ NGOẠI LỆ<!--DAY_HEADER_END-->

#### SUB-TASK 3.1: Đánh giá code quality, kiểm tra exception handling, performance, bảo mật
##### Sub-Agent được giao: Reviewer
##### Yêu cầu thành phần & kỹ thuật:
* **Đường dẫn mục tiêu**: ./sources/backend/notification-service
* **Thẻ mã theo dõi**: <!--START_TAGS-->[REQ-016], [REQ-017], [REQ-018], [REQ-019], [DAT-007], [DAT-008], [EXC-003]<!--END_TAGS-->