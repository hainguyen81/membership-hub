# Giai đoạn 3: <!--PHASE_NAME_START-->Triển khai dịch vụ hội viên, thông báo đa kênh và quản lý khuyến mãi<!--PHASE_NAME_END-->

## 📊 Bảng kiểm soát tài liệu

| Mục | Chi tiết |
| :--- | :--- |
| **ID Bản thiết kế** | ARCH-20260818163158 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 3 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Triển khai dịch vụ hội viên, thông báo đa kênh và quản lý khuyến mãi<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn này tập trung vào triển khai các chức năng quản lý thẻ hội viên kỹ thuật số (hiển thị số ngày còn lại hiệu lực, gia hạn thẻ với tích hợp thanh toán), hệ thống thông báo đa kênh (push di động qua FCM/APNs, đăng bài lên nhóm Zalo) với cơ chế xử lý lỗi và retry tự động, quản lý khuyến mãi và thông báo có thời hạn hiển thị tùy chọn, đảm bảo tất cả các chức năng này tuân thủ các yêu cầu nghiệp vụ [REQ-014] đến [REQ-018], ràng buộc kỹ thuật về hiệu suất và bảo mật đã được định nghĩa.<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày.Giờ** | 2026/08/18 16:31:58 |
| **Tác giả** | Kiến trúc sư hệ thống doanh nghiệp (Đặc vụ SA) |
| **Phê duyệt** | Đang chờ xem xét quản trị kỹ thuật |

## 1. Phạm vi hoạt động và mục tiêu của giai đoạn
Giai đoạn 3 triển khai các module nghiệp vụ nâng cao cho nền tảng quản lý hội viên, bao gồm: (1) Dịch vụ thẻ hội viên với logic tính toán ngày còn lại hiệu lực, gia hạn thẻ tích hợp cổng thanh toán và cơ chế tự động cập nhật trạng thái; (2) Hệ thống thông báo đa kênh hỗ trợ push notification qua FCM/APNs và đăng bài nhóm Zalo với cơ chế hàng đợi, retry tối đa 3 lần và ghi log chi tiết; (3) Module quản lý khuyến mãi và thông báo hệ thống với kiểm tra ngày hiệu lực, tự động ẩn nội dung hết hạn thông qua scheduled job. Tất cả thành phần được tích hợp với kiến trúc RBAC và hạ tầng đã triển khai, tuân thủ OWASP Top 10, và được bổ sung đầy đủ tài liệu kỹ thuật.

## 2. Phạm vi kỹ thuật được phép và ranh giới thư mục
- **Thư mục backend:** `./sources/backend/membership-service/`, `./sources/backend/notification-service/`, `./sources/backend/promotion-service/`
- **Thư mục frontend:** `./sources/frontend/web/membership/`
- **Thư mục tài liệu:** `./sources/docs/api/`, `./sources/docs/data-dictionary/`, `./sources/docs/integrations/`
- **Endpoint API được phép triển khai:**
  - `GET /api/membership/card`
  - `POST /api/membership/renew`
  - `POST /api/notifications/send`
  - `GET /api/promotions`, `POST /api/promotions`
  - `GET /api/announcements`, `POST /api/announcements`

## 3. Chỉ thị chức năng cho đại lý phụ chuyên biệt
*   **Coder**: Đóng vai trò là Nhà phát triển ứng dụng cấp Cao/Chính. Chịu trách nhiệm triển khai mã nguồn ứng dụng thuần túy trên lớp dịch vụ backend (REST controllers, services, repositories) và ứng dụng khách frontend (React components). Bị cấm viết bộ kiểm thử hoặc manifest hạ tầng.
*   **Tester**: Đóng vai trò là Kiểm soát chất lượng (QC/QA) cấp Lead/Chính. Chuyên về kỹ thuật bộ kiểm thử, xác thực và cổng chất lượng. Chịu trách nhiệm tạo các bộ kiểm thử đơn vị, tích hợp và end-to-end. Bị cấm sửa mã nguồn ứng dụng sản xuất. Nếu phạm vi kiểm thử tích hợp không thể cô lập thành một tệp mã ứng dụng cụ thể, phải sử dụng literal token `INTEGRATION_SCOPE` làm tham số đầu tiên của cặp dấu chấm phẩy.
*   **Doc**: Hoạt động như là Nhà viết kỹ thuật chính và Kiến trúc sư hệ thống doanh nghiệp. Chuyên về biên soạn tài liệu đặc tả kỹ thuật toàn diện, tài liệu tham chiếu schema, bản vẽ hệ thống và danh mục kiến trúc doanh nghiệp. Mỗi tệp tài liệu kỹ thuật được tạo phải có phần mở rộng `.md` và nằm nghiêm ngặt trong `./sources/docs/`.
*   **Reviewer**: Chịu trách nhiệm xác minh trình biên dịch, cổng phân tích tĩnh và vá lỗi phòng thủ. Chuyên về kiểm toán chất lượng mã nguồn, giải quyết lỗi biên dịch, sửa lỗ hổng bảo mật OWASP và đề xuất tối ưu truy vấn.

## 4. Định nghĩa hoàn thành giai đoạn (DoD)
1. Hoàn thành 100% các thẻ theo dõi yêu cầu được phân bổ cho Giai đoạn 3: [REQ-014] đến [REQ-018], [EXC-003], [DAT-007] đến [DAT-009], [ARC-008], không có thẻ nào bị bỏ sót.
2. Tất cả bộ kiểm thử đơn vị, tích hợp đạt độ bao phủ mã nguồn tối thiểu 85%, không có lỗi nghiêm trọng nào còn tồn tại sau khi rà soát.
3. Tất cả endpoint API được triển khai đầy đủ theo hợp đồng định tuyến đã định nghĩa, tuân thủ các tiêu chuẩn bảo mật OWASP Top 10 (chống SQL injection, XSS, CSRF, xác thực đầu vào nghiêm ngặt).
4. Lược đồ cơ sở dữ liệu được triển khai chính xác với tất cả ràng buộc khóa ngoại, chỉ mục và ràng buộc CHECK, đảm bảo tính toàn vẹn dữ liệu và hiệu suất truy vấn tối ưu.
5. Cơ chế retry thông báo hoạt động chính xác, tối đa 3 lần thử, ghi log chi tiết và đánh dấu trạng thái gửi thất bại đúng theo yêu cầu.
6. Hệ thống khuyến mãi và thông báo tự động ẩn nội dung hết hạn đúng theo cấu hình thời gian, lọc chính xác khi truy vấn.
7. Tất cả tài liệu kỹ thuật (từ điển dữ liệu, đặc tả API, hướng dẫn tích hợp) được hoàn thiện, rõ ràng và đồng bộ với phiên bản triển khai thực tế.

## 5. NHẬT KÝ THỰC HIỆN KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->Triển khai dịch vụ thẻ hội viên cốt lõi và schema cơ sở dữ liệu<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 1.1: Xây dựng lớp nghiệp vụ cốt lõi của dịch vụ thẻ hội viên
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipService.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-014], [DAT-007]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic tính toán số ngày còn lại hiệu lực của thẻ hội viên dựa trên issue_date và validity_days, đảm bảo giá trị remaining_days được cập nhật tự động mỗi ngày qua scheduled job, tuân thủ các ràng buộc NOT NULL và CHECK cho các trường dữ liệu.

#### 📝 Công việc phụ 1.2: Xây dựng endpoint API và repository truy cập dữ liệu thẻ hội viên
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipController.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-014], [DAT-007]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xây dựng endpoint GET /api/membership/card trả về thông tin thẻ hội viên của người dùng đã xác thực, tích hợp kiểm tra quyền truy cập RBAC để đảm bảo chỉ người dùng sở hữu thẻ hoặc quản trị viên mới có thể xem thông tin.

<!--START_API_CONTRACT>
```json
[
  {
    "endpoint": "/api/membership/card",
    "method": "GET",
    "description": "Lấy thông tin thẻ hội viên của người dùng đã xác thực",
    "request": {
      "headers": {
        "Authorization": "Bearer <JWT token>"
      }
    },
    "response": {
      "status": 200,
      "body": {
        "cardId": "uuid",
        "studentId": "uuid",
        "issueDate": "date",
        "validityDays": 30,
        "remainingDays": 15,
        "expiryDate": "date"
      }
    }
  }
]
```
<!--END_API_CONTRACT-->

#### 📝 Công việc phụ 1.3: Tạo migration DDL và kiểm tra tính toàn vẹn schema thẻ hội viên
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-service/src/main/resources/db/migration/V3__create_student_cards.sql`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[DAT-007]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết migration ANSI SQL tạo bảng student_cards với các ràng buộc khóa chính, khóa ngoại đến bảng users, ràng buộc CHECK cho validity_days và remaining_days, chạy migration trên môi trường staging để xác nhận không có lỗi.

<!--START_DDL_MIGRATION-->
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

-- Tạo index cho truy vấn thường dùng
CREATE INDEX idx_student_cards_student_id ON student_cards(student_id);
```
<!--END_DDL_MIGRATION-->

#### 📝 Công việc phụ 1.4: Viết bộ kiểm thử đơn vị cho logic nghiệp vụ thẻ hội viên
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-service/src/test/java/com/hub/membership/MembershipServiceTest.java;./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipService.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-014], [DAT-007]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử cho logic tính toán remaining_days, xử lý thẻ hết hạn, đảm bảo độ phủ mã 100% cho các nhánh điều kiện trong MembershipService.

#### 📝 Công việc phụ 1.5: Khởi tạo tài liệu kiến trúc và từ điển dữ liệu cho module thẻ hội viên
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/data-dictionary/student-cards.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[DAT-007]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Biên soạn tài liệu từ điển dữ liệu chi tiết cho bảng student_cards, mô tả đầy đủ từng trường (tên, kiểu dữ liệu, ràng buộc, mô tả nghiệp vụ), sơ đồ ERD mô tả mối quan hệ với bảng users, các chỉ mục được tạo và mục đích sử dụng, các ràng buộc toàn vẹn dữ liệu. Tài liệu phải được định dạng Markdown chuẩn, dễ đọc cho cả đội phát triển và quản trị cơ sở dữ liệu.

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->Triển khai chức năng gia hạn thẻ và dịch vụ thông báo cốt lõi<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 2.1: Xây dựng endpoint gia hạn thẻ hội viên và tích hợp logic thanh toán
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipController.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-015]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Thêm endpoint POST /api/membership/renew, tích hợp với cổng thanh toán để xác nhận giao dịch thành công trước khi cập nhật remaining_days và issue_date của thẻ, gửi thông báo xác nhận cho người dùng sau khi gia hạn thành công.

<!--START_API_CONTRACT>
```json
[
  {
    "endpoint": "/api/membership/renew",
    "method": "POST",
    "description": "Gia hạn thẻ hội viên với tích hợp thanh toán",
    "request": {
      "headers": {
        "Authorization": "Bearer <JWT token>"
      },
      "body": {
        "renewalDays": 30,
        "paymentTransactionId": "string"
      }
    },
    "response": {
      "status": 200,
      "body": {
        "cardId": "uuid",
        "remainingDays": 45,
        "expiryDate": "date"
      }
    },
    "error": {
      "status": 402,
      "body": { "error": "Payment failed" }
    }
  }
]
```
<!--END_API_CONTRACT-->

#### 📝 Công việc phụ 2.2: Xây dựng lớp dịch vụ thông báo cốt lõi và trình gửi FCM
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/notification-service/src/main/java/com/hub/notification/NotificationService.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-016], [EXC-003]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic xếp hàng thông báo, tích hợp với Firebase Cloud Messaging để gửi thông báo đẩy di động, thêm cơ chế ghi log lỗi và đếm số lần thử lại cho trường hợp gửi thất bại.

<!--START_EXC_HANDLER>
```json
{
  "exception_handlers": [
    {
      "error_code": "NOTIFICATION_SEND_FAILED",
      "http_status": 502,
      "trigger_condition": "Không thể gửi thông báo (token thiết bị không hợp lệ, lỗi kết nối API Zalo)",
      "behavior": "Hệ thống ghi log lỗi với chi tiết và timestamp, tự động thử lại tối đa 3 lần với khoảng cách 5 phút. Nếu sau 3 lần thử vẫn thất bại, đánh dấu delivered = false và gửi cảnh báo cho quản trị viên."
    }
  ]
}
```
<!--END_EXC_HANDLER-->

#### 📝 Công việc phụ 2.3: Viết bộ kiểm thử đơn vị cho chức năng gia hạn thẻ
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-service/src/test/java/com/hub/membership/MembershipRenewalTest.java;./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipController.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-015]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử cho endpoint gia hạn thẻ, bao gồm trường hợp thanh toán thành công, thanh toán thất bại, thẻ hết hạn, đảm bảo xử lý đúng các ngoại lệ nghiệp vụ.

#### 📝 Công việc phụ 2.4: Viết bộ kiểm thử đơn vị cho dịch vụ thông báo cốt lõi
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/notification-service/src/test/java/com/hub/notification/NotificationServiceTest.java;./sources/backend/notification-service/src/main/java/com/hub/notification/NotificationService.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-016], [EXC-003]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử cho logic gửi thông báo, xử lý lỗi gửi thất bại, cơ chế retry, đảm bảo số lần thử lại không vượt quá 3 lần.

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->Hoàn thiện hệ thống thông báo đa kênh và triển khai quản lý khuyến mãi, thông báo<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 3.1: Xây dựng trình gửi thông báo nhóm Zalo và tích hợp hàng đợi sự kiện
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/notification-service/src/main/java/com/hub/notification/ZaloNotificationSender.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-016], [ARC-008]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai tích hợp API Zalo để gửi tin nhắn đến nhóm Zalo được chỉ định, đảm bảo xử lý lỗi rate limit và lỗi xác thực API Zalo, ghi log chi tiết cho mỗi lần gửi thông báo.

#### 📝 Công việc phụ 3.2: Xây dựng dịch vụ và controller quản lý khuyến mãi
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/promotion-service/src/main/java/com/hub/promotion/PromotionService.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-017], [DAT-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic CRUD cho khuyến mãi, thêm kiểm tra xác thực đầu vào (phần trăm giảm giá 0-100, ngày kết thúc >= ngày bắt đầu), lọc khuyến mãi đang hoạt động dựa trên ngày hiện tại.

#### 📝 Công việc phụ 3.3: Xây dựng dịch vụ và controller quản lý thông báo hệ thống
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/promotion-service/src/main/java/com/hub/promotion/AnnouncementService.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-018], [DAT-009]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai logic CRUD cho thông báo hệ thống, thêm kiểm tra ngày hiệu lực, tự động ẩn thông báo sau ngày kết thúc thông qua scheduled job, lọc thông báo đang hoạt động khi truy vấn.

#### 📝 Công việc phụ 3.4: Viết bộ kiểm thử tích hợp cho luồng gửi thông báo đa kênh
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `INTEGRATION_SCOPE;./sources/backend/notification-service/src/test/java/com/hub/notification/NotificationIntegrationTest.java`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-016], [EXC-003]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết kịch bản kiểm thử tích hợp cho luồng gửi thông báo qua FCM và Zalo, bao gồm trường hợp gửi thành công, gửi thất bại và retry, đảm bảo thông báo được gửi đến đúng đích và trạng thái được cập nhật chính xác.

#### 📝 Công việc phụ 3.5: Viết tài liệu tham chiếu API cho các dịch vụ giai đoạn 3
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/api/notification-promotion-api.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-016], [REQ-017], [REQ-018]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu API mô tả chi tiết tất cả các endpoint của dịch vụ thông báo, khuyến mãi và thông báo, bao gồm tham số yêu cầu, phản hồi, mã lỗi và ví dụ sử dụng.

#### 📝 Công việc phụ 3.6: Cập nhật từ điển dữ liệu và tài liệu tích hợp Zalo
##### Đại lý phụ được phân công: [Doc]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/docs/integrations/zalo-api.md`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-016], [ARC-008]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Biên soạn tài liệu tích hợp Zalo API, mô tả cấu trúc tin nhắn, xử lý lỗi rate limit, cơ chế retry, và hướng dẫn cấu hình nhóm Zalo cho trung tâm. Cập nhật từ điển dữ liệu cho bảng notifications với các trường message, delivered, retry_count.

<!--START_DDL_MIGRATION>
```sql
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
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_delivered ON notifications(delivered);
CREATE INDEX idx_promotions_active ON promotions(start_date, end_date) WHERE end_date IS NULL OR end_date >= CURRENT_DATE;
CREATE INDEX idx_announcements_active ON announcements(start_date, end_date) WHERE end_date IS NULL OR end_date >= CURRENT_DATE;
```
<!--END_DDL_MIGRATION-->

<!--START_API_CONTRACT>
```json
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
      "trigger_condition": "Tiêu đề hoặc nội dung vượt quá độ dài tối đa, hoặc ngày kết thúc nhỏ hơn ngày bắt đầu",
      "behavior": "Trả về lỗi 400 Bad Request với thông báo chi tiết các trường không hợp lệ."
    }
  ]
}
```
<!--END_EXC_HANDLER-->

### 🌤️ NGÀY 4: <!--DAY_HEADER_START-->Triển khai giao diện người dùng cho các chức năng giai đoạn 3 và kiểm tra cuối cùng<!--DAY_HEADER_END-->

#### 📝 Công việc phụ 4.1: Xây dựng giao diện hiển thị thẻ hội viên trên frontend web
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/frontend/web/membership/src/app/membership/page.tsx`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-014]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Phát triển giao diện trang thẻ hội viên, hiển thị tổng ngày hiệu lực, ngày đã sử dụng, ngày còn lại và ngày hết hạn, tích hợp gọi API lấy thông tin thẻ và xử lý trạng thái tải và lỗi.

#### 📝 Công việc phụ 4.2: Xây dựng giao diện gia hạn thẻ hội viên trên frontend web
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/frontend/web/membership/src/app/membership/renew/page.tsx`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-015]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Phát triển giao diện trang gia hạn thẻ, cho phép người dùng chọn số ngày gia hạn, tích hợp với cổng thanh toán, hiển thị thông báo thành công/thất bại sau khi thực hiện gia hạn.

#### 📝 Công việc phụ 4.3: Xây dựng giao diện quản lý khuyến mãi và thông báo cho quản trị viên
##### Đại lý phụ được phân công: [Coder]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/frontend/web/membership/src/app/promotions/page.tsx`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-017], [REQ-018]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Phát triển giao diện quản lý khuyến mãi và thông báo cho quản trị viên, bao gồm chức năng tạo, sửa, xóa khuyến mãi và thông báo, hiển thị danh sách các mục đang hoạt động và đã hết hạn.

#### 📝 Công việc phụ 4.4: Viết bộ kiểm thử đơn vị cho các thành phần frontend giai đoạn 3
##### Đại lý phụ được phân công: [Tester]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/frontend/web/membership/src/app/membership/__tests__/membership.test.tsx;./sources/frontend/web/membership/src/app/membership/page.tsx`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-014], [REQ-015], [REQ-017], [REQ-018]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Viết các trường hợp kiểm thử cho các thành phần frontend trang thẻ hội viên, trang gia hạn và trang quản lý khuyến mãi/thông báo, đảm bảo hiển thị đúng dữ liệu và xử lý đúng các trạng thái tải, lỗi và thành công.

#### 📝 Công việc phụ 4.5: Rà soát mã nguồn toàn bộ giai đoạn 3 để đảm bảo tuân thủ RBAC và bảo mật
##### Đại lý phụ được phân công: [Reviewer]
##### Thành phần và yêu cầu kỹ thuật mục tiêu:
* **Đường dẫn tệp mục tiêu:** `./sources/backend/membership-service, ./sources/backend/notification-service, ./sources/backend/promotion-service, ./sources/frontend/web/membership`
* **Thẻ theo dõi truy xuất:** <!--START_TAGS-->[REQ-014], [REQ-015], [REQ-016], [REQ-017], [REQ-018], [ARC-001], [ARC-002], [NFR-003]<!--END_TAGS-->
* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Rà soát toàn bộ mã nguồn của giai đoạn 3 để phát hiện lỗ hổng bảo mật (SQL injection, XSS, truy cập trái phép), đảm bảo tất cả các endpoint đều có kiểm tra quyền RBAC, dữ liệu nhạy cảm được mã hóa đúng cách, đề xuất và triển khai các giải pháp sửa lỗi nếu phát hiện vấn đề.