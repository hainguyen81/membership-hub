# GIAI ĐOẠN 4: <!--PHASE_NAME_START-->Tích hợp Chatbot AI, Báo cáo Điểm danh và Quản lý Khuyến mãi<!--PHASE_NAME_END-->

## 📊 Bảng kiểm soát tài liệu

| Mục | Chi tiết |
| :--- | :--- |
| **ID Bản thiết kế** | ARCH-20260818163158 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 4 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Tích hợp Chatbot AI, Báo cáo Điểm danh và Quản lý Khuyến mãi<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn này triển khai các tính năng bổ sung bao gồm quản lý khuyến mãi và thông báo hệ thống có thời hạn hiệu lực, tích hợp chatbot AI hỗ trợ khách hàng với cơ chế chuyển tiếp hỗ trợ con người khi độ tin cậy thấp, xây dựng dịch vụ báo cáo điểm danh định dạng CSV và dashboard tổng quan ghi danh thời gian thực, đảm bảo tích hợp liền mạch với các dịch vụ backend hiện có và tuân thủ các yêu cầu nghiệp vụ [REQ-017] đến [REQ-019], [REQ-024], [REQ-025].<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày.Giờ** | 2026/08/18 16:31:58 |
| **Tác giả** | Kiến trúc sư hệ thống doanh nghiệp (Đặc vụ SA) |
| **Phê duyệt** | Đang chờ xem xét quản trị kỹ thuật |

## 1. Phạm vi hoạt động và mục tiêu của giai đoạn
Giai đoạn 4 tập trung vào việc mở rộng hệ thống với các module nghiệp vụ nâng cao: (1) Quản lý khuyến mãi và thông báo hệ thống với cơ chế tự động ẩn nội dung hết hạn thông qua scheduled job và lọc chính xác khi truy vấn; (2) Tích hợp chatbot AI hỗ trợ khách hàng tự động hoặc chuyển tiếp đến hỗ trợ viên khi độ tin cậy của phản hồi dưới 0.7; (3) Xây dựng dịch vụ báo cáo điểm danh định dạng CSV hàng ngày theo trung tâm và dashboard tổng quan ghi danh thời gian thực với cơ chế cập nhật real-time. Tất cả các thành phần được tích hợp với kiến trúc RBAC, hạ tầng đã triển khai, tuân thủ OWASP Top 10 và được bổ sung đầy đủ tài liệu kỹ thuật.

## 2. Phạm vi kỹ thuật được phép và ranh giới thư mục
- **Thư mục backend:** `./sources/backend/promotion-service/`, `./sources/backend/notification-service/`, `./sources/backend/chatbot-service/`, `./sources/backend/report-service/`
- **Thư mục tài liệu:** `./sources/docs/data-dictionary/`, `./sources/docs/integrations/`, `./sources/docs/user-guide/`
- **Endpoint API được phép triển khai:**
  - `GET /api/promotions`, `POST /api/promotions`
  - `GET /api/announcements`, `POST /api/announcements`
  - `POST /api/v1/chatbot/message`
  - `GET /api/v1/reports/attendance/csv`
  - `GET /api/v1/dashboard/enrollment`

## 3. Chỉ thị chức năng cho đại lý phụ chuyên biệt
*   **Coder**: Đóng vai trò là Nhà phát triển ứng dụng cấp Cao/Chính. Chịu trách nhiệm triển khai mã nguồn ứng dụng thuần túy trên lớp dịch vụ backend (REST controllers, services, repositories) và ứng dụng khách frontend. Bị cấm viết bộ kiểm thử hoặc manifest hạ tầng.
*   **Tester**: Đóng vai trò là Kiểm soát chất lượng (QC/QA) cấp Lead/Chính. Chuyên về kỹ thuật bộ kiểm thử, xác thực và cổng chất lượng. Chịu trách nhiệm tạo các bộ kiểm thử đơn vị, tích hợp và end-to-end. Bị cấm sửa mã nguồn ứng dụng sản xuất. Nếu phạm vi kiểm thử tích hợp không thể cô lập thành một tệp mã ứng dụng cụ thể, phải sử dụng literal token `INTEGRATION_SCOPE` làm tham số đầu tiên của cặp dấu chấm phẩy.
*   **Doc**: Hoạt động như là Nhà viết kỹ thuật chính và Kiến trúc sư hệ thống doanh nghiệp. Chuyên về biên soạn tài liệu đặc tả kỹ thuật toàn diện, tài liệu tham chiếu schema, bản vẽ hệ thống và danh mục kiến trúc doanh nghiệp. Mỗi tệp tài liệu kỹ thuật được tạo phải có phần mở rộng `.md` và nằm nghiêm ngặt trong `./sources/docs/`.
*   **Reviewer**: Chịu trách nhiệm xác minh trình biên dịch, cổng phân tích tĩnh và vá lỗi phòng thủ. Chuyên về kiểm toán chất lượng mã nguồn, giải quyết lỗi biên dịch, sửa lỗ hổng bảo mật OWASP và đề xuất tối ưu truy vấn.

## 4. Định nghĩa hoàn thành giai đoạn (DoD)
1. Hoàn thành 100% các thẻ theo dõi yêu cầu được phân bổ cho Giai đoạn 4: [REQ-017], [REQ-018], [REQ-019], [REQ-024], [REQ-025], [EXC-003], [EXC-005], [DAT-009], [DAT-010], [DAT-011], [ARC-008], [ARC-009], không có thẻ nào bị bỏ sót.
2. Tất cả bộ kiểm thử đơn vị, tích hợp đạt độ bao phủ mã nguồn tối thiểu 85%, không có lỗi nghiêm trọng nào còn tồn tại sau khi rà soát.
3. Tất cả endpoint API được triển khai đầy đủ theo hợp đồng định tuyến đã định nghĩa, tuân thủ các tiêu chuẩn bảo mật OWASP Top 10 (chống SQL injection, XSS, CSRF, xác thực đầu vào nghiêm ngặt).
4. Lược đồ cơ sở dữ liệu được triển khai chính xác với tất cả ràng buộc khóa ngoại, chỉ mục và ràng buộc CHECK, đảm bảo tính toàn vẹn dữ liệu và hiệu suất truy vấn tối ưu.
5. Cơ chế retry thông báo hoạt động chính xác, tối đa 3 lần thử, ghi log chi tiết và đánh dấu trạng thái gửi thất bại đúng theo yêu cầu.
6. Hệ thống khuyến mãi và thông báo tự động ẩn nội dung hết hạn đúng theo cấu hình thời gian, lọc chính xác khi truy vấn.
7. Tất cả tài liệu kỹ thuật (từ điển dữ liệu, đặc tả API, hướng dẫn tích hợp) được hoàn thiện, rõ ràng và đồng bộ với phiên bản triển khai thực tế.

## 5. NHẬT KÝ THỰC HIỆN KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1:
<!--DAY_HEADER_START-->Triển khai quản lý khuyến mãi và thông báo hệ thống<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 1.1: Xây dựng dịch vụ quản lý khuyến mãi và thông báo
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/PromotionService.java;./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/PromotionController.java;./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/AnnouncementService.java;./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/AnnouncementController.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-017], [REQ-018], [DAT-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai toàn bộ logic nghiệp vụ cho module quản lý khuyến mãi và thông báo hệ thống. Đối với khuyến mãi, xây dựng các phương thức CRUD với kiểm tra xác thực đầu vào nghiêm ngặt: phần trăm giảm giá phải nằm trong khoảng 0-100, ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu, mã khuyến mãi phải duy nhất. Đối với thông báo, triển khai logic lọc thông báo đang hoạt động dựa trên ngày hiện tại và cơ chế tự động ẩn thông báo sau ngày kết thúc thông qua scheduled job chạy hàng ngày. Tất cả các endpoint phải được bảo vệ bằng bộ lọc RBAC, chỉ cho phép Center Admin và Manager truy cập. Sử dụng prepared statements của Hibernate ORM để ngăn chặn SQL injection. Áp dụng làm sạch dữ liệu đầu vào để chống XSS.

<!--START_DDL_MIGRATION>
```sql
-- Tạo bảng khuyến mãi và thông báo
CREATE TABLE promotions (
    promo_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_percent SMALLINT NOT NULL CHECK (discount_percent BETWEEN 0 AND 100),
    start_date DATE,
    end_date DATE,
    description TEXT,
    CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE TABLE announcements (
    announcement_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL CHECK (length(title) <= 150),
    content TEXT NOT NULL CHECK (length(content) <= 2000),
    start_date DATE,
    end_date DATE,
    CHECK (end_date IS NULL OR end_date >= start_date)
);

-- Tạo index cho các truy vấn thường dùng
CREATE INDEX idx_promotions_active ON promotions(start_date, end_date) WHERE end_date IS NULL OR end_date >= CURRENT_DATE;
CREATE INDEX idx_announcements_active ON announcements(start_date, end_date) WHERE end_date IS NULL OR end_date >= CURRENT_DATE;
```
<!--END_DDL_MIGRATION-->

<!--START_API_CONTRACT>
```json
[
  {
    "endpoint": "/api/promotions",
    "method": "GET",
    "description": "Lấy danh sách khuyến mãi đang hoạt động",
    "request": {
      "queryParams": {
        "page": "INT (tùy chọn, mặc định 1)",
        "size": "INT (tùy chọn, mặc định 20)"
      }
    },
    "response": {
      "status": 200,
      "body": [
        {
          "promoId": "uuid",
          "code": "string",
          "discountPercent": 10,
          "startDate": "date",
          "endDate": "date",
          "description": "string"
        }
      ]
    }
  },
  {
    "endpoint": "/api/promotions",
    "method": "POST",
    "description": "Tạo khuyến mãi mới",
    "request": {
      "headers": {
        "Authorization": "Bearer <JWT token>"
      },
      "body": {
        "code": "SUMMER10",
        "discountPercent": 10,
        "startDate": "2024-06-01",
        "endDate": "2024-08-31",
        "description": "Giảm 10% khóa học hè"
      }
    },
    "response": {
      "status": 201,
      "body": {
        "promoId": "uuid"
      }
    }
  },
  {
    "endpoint": "/api/announcements",
    "method": "GET",
    "description": "Lấy danh sách thông báo đang hoạt động",
    "response": {
      "status": 200,
      "body": [
        {
          "announcementId": "uuid",
          "title": "string",
          "content": "string",
          "startDate": "date",
          "endDate": "date"
        }
      ]
    }
  },
  {
    "endpoint": "/api/announcements",
    "method": "POST",
    "description": "Tạo thông báo mới",
    "request": {
      "headers": {
        "Authorization": "Bearer <JWT token>"
      },
      "body": {
        "title": "Thông báo nghỉ lễ",
        "content": "Trung tâm nghỉ lễ 30/4",
        "startDate": "2024-04-29",
        "endDate": "2024-05-01"
      }
    },
    "response": {
      "status": 201,
      "body": {
        "announcementId": "uuid"
      }
    }
  }
]
```
<!--END_API_CONTRACT-->

<!--START_EXC_HANDLER>
```json
{
  "exception_handlers": [
    {
      "error_code": "PROMOTION_VALIDATION_ERROR",
      "http_status": 400,
      "trigger_condition": "Ngày kết thúc nhỏ hơn ngày bắt đầu hoặc phần trăm giảm giá ngoài khoảng 0-100",
      "behavior": "Trả về lỗi 400 Bad Request với thông báo chi tiết các trường không hợp lệ."
    },
    {
      "error_code": "ANNOUNCEMENT_VALIDATION_ERROR",
      "http_status": 400,
      "trigger_condition": "Tiêu đề vượt quá 150 ký tự, nội dung vượt quá 2000 ký tự, hoặc ngày kết thúc nhỏ hơn ngày bắt đầu",
      "behavior": "Trả về lỗi 400 Bad Request với thông báo chi tiết các trường không hợp lệ."
    }
  ]
}
```
<!--END_EXC_HANDLER-->

#### 📝 Công việc phụ 1.2: Viết bộ kiểm thử tích hợp cho API khuyến mãi và thông báo
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/PromotionService.java;./sources/backend/promotion-service/src/test/java/org/nlh4j/membership_hub/promotion/PromotionAnnouncementResourceTest.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-017], [REQ-018]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử tích hợp sử dụng JUnit 5 và RestAssured để xác minh toàn bộ luồng CRUD của khuyến mãi và thông báo. Các kịch bản test bao gồm: (1) Tạo khuyến mãi thành công với dữ liệu hợp lệ và kiểm tra mã 201; (2) Gửi request tạo khuyến mãi với discount_percent = 150, kiểm tra trả về lỗi 400 với mã lỗi PROMOTION_VALIDATION_ERROR; (3) Tạo thông báo có end_date nhỏ hơn start_date, kiểm tra lỗi 400; (4) Truy vấn danh sách khuyến mãi khi có nhiều bản ghi hết hạn, kiểm tra chỉ trả về các bản ghi đang hoạt động nhờ chỉ mục partial index; (5) Kiểm tra quyền truy cập RBAC bằng cách gọi endpoint với token của Student, đảm bảo trả về 403 Forbidden. Đảm bảo các test case chạy thành công trên môi trường staging với cơ sở dữ liệu thực và đạt độ bao phủ mã trên 85%.

#### 📝 Công việc phụ 1.3: Cập nhật từ điển dữ liệu và tài liệu tích hợp
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/data-dictionary/promotions-announcements.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[DAT-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Biên soạn tài liệu từ điển dữ liệu chuẩn doanh nghiệp cho hai bảng promotions và announcements. Tài liệu phải bao gồm: (1) Mô tả chi tiết từng trường dữ liệu bao gồm tên trường, kiểu dữ liệu, ràng buộc NOT NULL/UNIQUE/CHECK, mô tả nghiệp vụ; (2) Sơ đồ ERD mô tả cấu trúc bảng và mối quan hệ (nếu có); (3) Danh sách chỉ mục (index) được tạo trên từng bảng cùng mục đích tối ưu truy vấn; (4) Các ràng buộc toàn vẹn dữ liệu và cách xử lý vi phạm. Định dạng tài liệu theo chuẩn Markdown với cấu trúc rõ ràng, dễ đọc và dễ bảo trì cho cả đội phát triển và quản trị cơ sở dữ liệu.

### 🌤️ NGÀY 2:
<!--DAY_HEADER_START-->Tích hợp chatbot AI và cơ sở dữ liệu hệ thống<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 2.1: Xây dựng dịch vụ chatbot AI backend
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/ChatbotService.java;./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/ChatbotController.java;./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/AIServiceClient.java;./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/EscalationService.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-019]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai kiến trúc tách biệt các thành phần chatbot: ChatbotController xử lý HTTP request và xác thực JWT; ChatbotService chứa logic nghiệp vụ; AIServiceClient tích hợp với mô hình AI bên thứ ba (ví dụ: Google Dialogflow) qua REST API; EscalationService xử lý chuyển tiếp hỗ trợ con người. Endpoint POST /api/v1/chatbot/message nhận payload {message, sessionId}, gọi AIServiceClient để nhận phản hồi và confidence score. Nếu confidence >= 0.7, trả về phản hồi AI trực tiếp; nếu confidence < 0.7, kích hoạt EscalationService để tạo ticket hỗ trợ và trả về escalate = true. Tất cả tương tác phải được ghi log chi tiết vào bảng audit_log với user_id, action, details và timestamp để phục vụ kiểm tra và cải tiến mô hình.

<!--START_API_CONTRACT>
```json
[
  {
    "endpoint": "/api/v1/chatbot/message",
    "method": "POST",
    "description": "Xử lý truy vấn chatbot AI",
    "request": {
      "headers": {
        "Authorization": "Bearer <JWT token>"
      },
      "body": {
        "message": "string",
        "sessionId": "uuid"
      }
    },
    "response": {
      "status": 200,
      "body": {
        "reply": "string",
        "confidence": 0.95,
        "escalate": false
      }
    }
  }
]
```
<!--END_API_CONTRACT-->

#### 📝 Công việc phụ 2.2: Tạo migration DDL cho bảng audit_log và system_settings
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/chatbot-service/src/main/resources/db/migration/V7__create_audit_system_settings.sql`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[DAT-010], [DAT-011], [NFR-006]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết migration SQL ANSI tạo hai bảng: audit_log và system_settings. Bảng audit_log lưu trữ toàn bộ hành động người dùng với các trường: audit_id (PK), user_id (FK đến users, ON DELETE SET NULL), action (VARCHAR 100, NOT NULL), details (JSONB linh hoạt), ip_address (INET), user_agent (TEXT), timestamp (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP). Bảng system_settings lưu cấu hình hệ thống với các trường: setting_key (VARCHAR 100, PK), setting_value (TEXT, NOT NULL), description (TEXT), updated_at (TIMESTAMP). Tạo chỉ mục cho audit_log trên user_id và timestamp để tối ưu truy vấn lịch sử. Tạo chỉ mục cho system_settings trên setting_key. Đảm bảo tất cả ràng buộc khóa ngoại và CHECK được định nghĩa chính xác.

<!--START_DDL_MIGRATION>
```sql
-- Tạo bảng audit_log
CREATE TABLE audit_log (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    details JSONB,
    ip_address INET,
    user_agent TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng system_settings
CREATE TABLE system_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tạo index cho các truy vấn thường dùng
CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX idx_system_settings_key ON system_settings(setting_key);
```
<!--END_DDL_MIGRATION-->

#### 📝 Công việc phụ 2.3: Viết bộ kiểm thử đơn vị cho dịch vụ chatbot
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/ChatbotService.java;./sources/backend/chatbot-service/src/test/java/org/nlh4j/membership_hub/chatbot/ChatbotServiceTest.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-019]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết bộ kiểm thử đơn vị sử dụng JUnit 5 và Mockito để kiểm tra toàn bộ logic của ChatbotService. Các trường hợp test bắt buộc: (1) Truy vấn hợp lệ với confidence >= 0.7, kiểm tra trả về reply từ AI và escalate = false; (2) Truy vấn có confidence = 0.5, kiểm tra escalate = true và ticket hỗ trợ được tạo thông qua EscalationService; (3) Request thiếu trường message hoặc sessionId, kiểm tra ném ra IllegalArgumentException với thông báo rõ ràng; (4) Request với JWT token hết hạn, kiểm tra trả về lỗi 401 Unauthorized; (5) Kiểm tra phương thức ghi log audit được gọi đúng với đầy đủ tham số. Đảm bảo độ bao phủ mã đạt trên 90% bằng JaCoCo và tất cả test case pass trên môi trường CI/CD.

#### 📝 Công việc phụ 2.4: Tạo tài liệu tích hợp chatbot AI
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/integrations/ai-chatbot.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-019]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Biên soạn tài liệu tích hợp chatbot AI theo chuẩn kỹ thuật doanh nghiệp. Tài liệu phải bao gồm: (1) Kiến trúc tổng quan hệ thống chatbot, sơ đồ luồng dữ liệu từ người dùng đến mô hình AI và quay lại; (2) Cấu hình mô hình AI: tên endpoint, khóa xác thực, tham số nhiệt độ, max_tokens, ngôn ngữ hỗ trợ; (3) Cơ chế chuyển tiếp hỗ trợ con người: điều kiện kích hoạt (confidence < 0.7), luồng tạo ticket, thông báo cho hỗ trợ viên; (4) Xử lý ngoại lệ: lỗi kết nối API AI, lỗi xác thực, timeout, giới hạn tần suất; (5) Hướng dẫn tích hợp frontend: cấu trúc payload request/response, xử lý trạng thái loading và lỗi, hiển thị tin nhắn đang soạn thảo. Tài liệu phải được lưu trữ tại `./sources/docs/integrations/ai-chatbot.md` với định dạng Markdown chuẩn.

### 🌤️ NGÀY 3:
<!--DAY_HEADER_START-->Triển khai báo cáo điểm danh và dashboard ghi danh thời gian thực<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 3.1: Xây dựng dịch vụ báo cáo điểm danh CSV
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/report-service/src/main/java/org/nlh4j/membership_hub/report/AttendanceReportController.java;./sources/backend/report-service/src/main/java/org/nlh4j/membership_hub/report/CSVExportService.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-024], [EXC-005]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng endpoint GET /api/v1/reports/attendance/csv cho phép lọc theo centerId, startDate và endDate. Triển khai logic truy vấn dữ liệu điểm danh bằng cách JOIN các bảng users, courses, enrollments và attendance để lấy đầy đủ thông tin StudentName, CourseName, AttendanceDate, Status. Trước khi tạo báo cáo, kiểm tra và xử lý tất cả các bản ghi điểm danh pending (trạng thái chưa xử lý sau sự cố) theo thứ tự FIFO để đảm bảo không bỏ sót dữ liệu. Sử dụng thư viện OpenCSV để tạo file CSV với encoding UTF-8 hỗ trợ tiếng Việt, thiết lập header Content-Disposition để trình duyệt tự động tải file. Tối ưu truy vấn với chỉ mục phù hợp và cân nhắc sử dụng bản sao đọc PostgreSQL để giảm tải cho cơ sở dữ liệu chính, đảm bảo thời gian phản hồi dưới 200ms.

<!--START_API_CONTRACT>
```json
[
  {
    "endpoint": "/api/v1/reports/attendance/csv",
    "method": "GET",
    "description": "Xuất báo cáo điểm danh định dạng CSV theo trung tâm và khoảng thời gian",
    "request": {
      "queryParams": {
        "centerId": "uuid (bắt buộc đối với Center Admin, tùy chọn đối với System Admin)",
        "startDate": "YYYY-MM-DD (bắt buộc)",
        "endDate": "YYYY-MM-DD (bắt buộc)"
      }
    },
    "response": {
      "status": 200,
      "body": "File CSV với các cột: StudentName, CourseName, AttendanceDate, Status",
      "headers": {
        "Content-Type": "text/csv; charset=utf-8",
        "Content-Disposition": "attachment; filename=\"attendance_report.csv\""
      }
    }
  }
]
```
<!--END_API_CONTRACT-->

<!--START_EXC_HANDLER>
```json
{
  "exception_handlers": [
    {
      "error_code": "REPORT_GENERATION_FAILED",
      "http_status": 500,
      "trigger_condition": "Lỗi truy vấn cơ sở dữ liệu, lỗi tạo file CSV, hoặc lỗi xử lý điểm danh pending",
      "behavior": "Trả về lỗi 500 Internal Server Error với thông báo chi tiết, ghi log lỗi với stack trace để đội vận hành kiểm tra và xử lý."
    },
    {
      "error_code": "REPORT_ACCESS_DENIED",
      "http_status": 403,
      "trigger_condition": "Người dùng không có quyền truy cập báo cáo của trung tâm được chỉ định",
      "behavior": "Trả về lỗi 403 Forbidden với thông báo thiếu quyền truy cập."
    }
  ]
}
```
<!--END_EXC_HANDLER-->

#### 📝 Công việc phụ 3.2: Xây dựng dashboard ghi danh thời gian thực
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/report-service/src/main/java/org/nlh4j/membership_hub/report/EnrollmentDashboardController.java;./sources/backend/report-service/src/main/java/org/nlh4j/membership_hub/report/DashboardMetricsService.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-025]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng endpoint GET /api/v1/dashboard/enrollment trả về dữ liệu tổng hợp: totalStudents (tổng số học viên đã đăng ký ít nhất một khóa học), activeCourses (số khóa học có startDate <= CURRENT_DATE và endDate >= CURRENT_DATE), upcomingSessions (số buổi học sắp tới trong 7 ngày tiếp theo dựa trên lịch trình khóa học). Tích hợp WebSocket để đẩy cập nhật thời gian thực đến frontend khi có sự kiện đăng ký mới, hủy đăng ký hoặc tạo khóa học mới. Áp dụng caching với Redis, đặt thời gian hết hạn cache 5 phút để giảm tải truy vấn. Đảm bảo endpoint có kiểm tra quyền RBAC toàn diện: Center Admin chỉ xem được dashboard của trung tâm mình quản lý, System Admin xem được toàn hệ thống.

<!--START_API_CONTRACT>
```json
[
  {
    "endpoint": "/api/v1/dashboard/enrollment",
    "method": "GET",
    "description": "Lấy dữ liệu tổng hợp dashboard ghi danh thời gian thực",
    "request": {
      "headers": {
        "Authorization": "Bearer <JWT token>"
      },
      "queryParams": {
        "centerId": "uuid (tùy chọn, bắt buộc với Center Admin, tùy chọn với System Admin)"
      }
    },
    "response": {
      "status": 200,
      "body": {
        "totalStudents": 150,
        "activeCourses": 12,
        "upcomingSessions": 8
      }
    }
  }
]
```
<!--END_API_CONTRACT-->

#### 📝 Công việc phụ 3.3: Viết bộ kiểm thử tích hợp cho báo cáo và dashboard
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/report-service/src/main/java/org/nlh4j/membership_hub/report/AttendanceReportController.java;./sources/backend/report-service/src/test/java/org/nlh4j/membership_hub/report/ReportIntegrationTest.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-024], [REQ-025], [EXC-005]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết integration test sử dụng Testcontainers để khởi động cơ sở dữ liệu PostgreSQL thực và kiểm tra toàn bộ luồng báo cáo và dashboard. Các kịch bản test: (1) Chèn 10.000 bản ghi điểm danh giả lập, gọi endpoint báo cáo CSV và kiểm tra thời gian phản hồi dưới 200ms, nội dung file CSV chính xác với các cột StudentName, CourseName, AttendanceDate, Status; (2) Kiểm tra dashboard phản ánh đúng số liệu sau khi có đăng ký mới thông qua WebSocket; (3) Giả lập sự cố hệ thống bằng cách chèn điểm danh pending, khôi phục và kiểm tra xử lý FIFO đúng thứ tự không bị mất dữ liệu; (4) Kiểm tra quyền truy cập RBAC từ chối Student truy cập dashboard với mã 403. Đảm bảo tất cả test case đều pass trên môi trường CI/CD và đạt độ bao phủ mã trên 85%.

#### 📝 Công việc phụ 3.4: Rà soát và tối ưu hiệu suất báo cáo
##### Đại lý phụ được phân công: [Reviewer]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/report-service/src/main/java/org/nlh4j/membership_hub/report/AttendanceReportController.java;./sources/backend/report-service/src/main/java/org/nlh4j/membership_hub/report/EnrollmentDashboardController.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-024], [REQ-025], [EXC-005]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Thực hiện rà soát mã nguồn toàn bộ module report-service để đảm bảo chất lượng và bảo mật. Kiểm tra các điểm: (1) Tất cả truy vấn cơ sở dữ liệu đều sử dụng prepared statements của Hibernate ORM, không có nối chuỗi SQL trực tiếp để ngăn chặn SQL injection; (2) Kiểm tra logic xử lý điểm danh pending đúng thứ tự FIFO, không có điều kiện race condition; (3) Phân tích hiệu suất truy vấn báo cáo với khối lượng dữ liệu lớn (hơn 10.000 bản ghi), đề xuất tối ưu bằng cách thêm chỉ mục phù hợp hoặc sử dụng bản sao đọc PostgreSQL; (4) Kiểm tra xử lý ngoại lệ khôi phục hệ thống đảm bảo không mất dữ liệu điểm danh; (5) Đảm bảo tuân thủ OWASP Top 10, đặc biệt là kiểm soát truy cập dữ liệu theo vai trò và mã hóa dữ liệu nhạy cảm trong log. Ghi nhận tất cả lỗi và đề xuất giải pháp sửa chữa trước khi chuyển sang giai đoạn tiếp theo.

#### 📝 Công việc phụ 3.5: Viết tài liệu hướng dẫn sử dụng báo cáo và dashboard
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/user-guide/reports-dashboard.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-024], [REQ-025]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Biên soạn tài liệu hướng dẫn sử dụng chi tiết cho chức năng báo cáo điểm danh CSV và dashboard ghi danh thời gian thực. Tài liệu phải bao gồm: (1) Hướng dẫn truy cập và sử dụng chức năng xuất báo cáo: cách chọn trung tâm, khoảng thời gian, định dạng file đầu ra, cách đọc và phân tích dữ liệu CSV; (2) Hướng dẫn đọc dashboard: giải thích các chỉ số totalStudents, activeCourses, upcomingSessions, cập nhật thời gian thực qua WebSocket; (3) Xử lý sự cố thường gặp: lỗi tạo báo cáo (do quyền truy cập, do dữ liệu không hợp lệ), dashboard không cập nhật (do lỗi WebSocket, do cache hết hạn); (4) Liên hệ hỗ trợ kỹ thuật khi gặp vấn đề vượt quá quyền hạn người dùng. Tài liệu phải được viết bằng tiếng Việt rõ ràng, có hình ảnh minh họa giao diện và ví dụ cụ thể.