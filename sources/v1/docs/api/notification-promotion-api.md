markdown
# Notification & Promotion API Reference
**File Path:** `./sources/docs/api/notification-promotion-api.md`  
**Package Base:** `org.nlh4j.saas.*`  
**Version:** 1.0 (Cơ sở)  
**Ngày.Giờ:** 2026/08/18 16:31:58  
**Tác giả:** Kiến trúc sư hệ thống doanh nghiệp (Đặc vụ SA)

---

## 📖 Tổng Quan

Hệ thống Notification & Promotion API cung cấp các endpoint REST để quản lý thông báo đa kênh (push di động, nhóm Zalo), khuyến mãi và thông báo hệ thống cho nền tảng quản lý hội viên đa trung tâm. Tất cả các endpoint đều được bảo vệ bằng JWT và tuân thủ cơ chế phân quyền dựa trên vai trò (RBAC) nghiêm ngặt.

**Đặc điểm chính**
- **Multi-tenancy:** Mỗi trung tâm có thể quản lý thông báo, khuyến mãi và thông báo của riêng mình.
- **Đa kênh:** Hỗ trợ gửi push notification qua Firebase Cloud Messaging (FCM) và Apple Push Notification Service (APNs), cũng như đăng bài lên nhóm Zalo được chỉ định.
- **Idempotency & Retry:** Các request gửi thông báo được xử lý idempotent; hệ thống tự động thử lại tối đa 3 lần khi gửi thất bại.
- **Logging kiểm toán:** Tất cả các thao tác (tạo, cập nhật, xóa) được ghi log với dấu vết kiểm toán đầy đủ cho mục đích tuân thủ GDPR/CCPA.
- **Caching:** Các endpoint GET được cache theo tenant để giảm tải cho cơ sở dữ liệu.

---

## 🔗 Ma Trận Tham Chiếu Truy Tìm

| Module | Endpoint | HTTP Method | Targeted Tag IDs |
|--------|----------|-------------|------------------|
| **Notification Service** | `/api/notifications` | GET | `[REQ-016]`, `[ARC-008]`, `[DAT-008]` |
| | `/api/notifications` | POST | `[REQ-016]`, `[EXC-003]`, `[DAT-008]` |
| | `/api/notifications/{notificationId}` | GET | `[REQ-016]`, `[ARC-008]`, `[DAT-008]` |
| | `/api/notifications/{notificationId}` | PUT | `[REQ-016]`, `[EXC-003]`, `[DAT-008]` |
| | `/api/notifications/{notificationId}` | DELETE | `[REQ-016]`, `[EXC-003]`, `[DAT-008]` |
| **Promotion Service** | `/api/promotions` | GET | `[REQ-017]`, `[ARC-008]`, `[DAT-009]` |
| | `/api/promotions` | POST | `[REQ-017]`, `[EXC-003]`, `[DAT-009]` |
| | `/api/promotions/{promoId}` | GET | `[REQ-017]`, `[ARC-008]`, `[DAT-009]` |
| | `/api/promotions/{promoId}` | PUT | `[REQ-017]`, `[EXC-003]`, `[DAT-009]` |
| | `/api/promotions/{promoId}` | DELETE | `[REQ-017]`, `[EXC-003]`, `[DAT-009]` |
| **Announcement Service** | `/api/announcements` | GET | `[REQ-018]`, `[ARC-008]`, `[DAT-009]` |
| | `/api/announcements` | POST | `[REQ-018]`, `[EXC-003]`, `[DAT-009]` |
| | `/api/announcements/{announcementId}` | GET | `[REQ-018]`, `[ARC-008]`, `[DAT-009]` |
| | `/api/announcements/{announcementId}` | PUT | `[REQ-018]`, `[EXC-003]`, `[DAT-009]` |
| | `/api/announcements/{announcementId}` | DELETE | `[REQ-018]`, `[EXC-003]`, `[DAT-009]` |

---

## 📦 Mô Hình Dữ Liệu

### 1. Notification (`org.nlh4j.saas.notification.entity.Notification`)

@jakarta.persistence.Entity
@Table(name = "notifications", schema = "public")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID notificationId;

    @Column(nullable = false)
    private UUID userId; // null cho broadcast

    @Column(name = "group_zalo", length = 255)
    private String groupZalo; // null cho push cá nhân

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private OffsetDateTime sentAt = OffsetDateTime.now();

    @Column(nullable = false)
    private boolean delivered;

    @Column(nullable = false)
    private int retryCount; // 0-3

    // getters, setters, constructors...
}


### 2. Promotion (`org.nlh4j.saas.promotion.entity.Promotion`)

@jakarta.persistence.Entity
@Table(name = "promotions", schema = "public")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID promoId;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private Short discountPercent; // 0-100

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 2000)
    private String description;

    // getters, setters, constructors...
}


### 3. Announcement (`org.nlh4j.saas.promotion.entity.Announcement`)

@jakarta.persistence.Entity
@Table(name = "announcements", schema = "public")
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID announcementId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // getters, setters, constructors...
}


---

## 📍 API Specifications

### Notification Service

| HTTP Method | Endpoint | Request Headers | Path/Query Parameters | JSON Request Payload Schema | JSON Response Schema (Success) | JSON Response Schema (Failure) | Targeted Tag IDs |
|-------------|----------|-----------------|-----------------------|-----------------------------|--------------------------------|--------------------------------|------------------|
| **GET** | `/api/notifications` | `Authorization: Bearer <JWT>` | `?page=0&size=20`<br>`?centerId=UUID`<br>`?delivered=true/false` | — | `[{notificationId, userId, groupZalo, message, sentAt, delivered, retryCount}]` | `{error: "VALIDATION_FAILED", message: "Missing required query param"}` | `[REQ-016]`, `[ARC-008]`, `[DAT-008]` |
| **POST** | `/api/notifications` | `Authorization: Bearer <JWT>`<br>`Content-Type: application/json` | — | `{ "userId": "uuid", "groupZalo": "string", "message": "string" }` | `{notificationId: "uuid", status: "QUEUED", messageId: "string"}` | `{error: "VALIDATION_FAILED", message: "Invalid payload"}` | `[REQ-016]`, `[EXC-003]`, `[DAT-008]` |
| **GET** | `/api/notifications/{notificationId}` | `Authorization: Bearer <JWT>` | — | — | `{notificationId, userId, groupZalo, message, sentAt, delivered, retryCount}` | `{error: "NOT_FOUND", message: "Notification not found"}` | `[REQ-016]`, `[ARC-008]`, `[DAT-008]` |
| **PUT** | `/api/notifications/{notificationId}` | `Authorization: Bearer <JWT>`<br>`Content-Type: application/json` | — | `{ "message": "string", "delivered": "boolean", "retryCount": "int" }` | `{notificationId, updatedAt: "string"}` | `{error: "FORBIDDEN", message: "Only System Admin/ Center Admin can update"}` | `[REQ-016]`, `[EXC-003]`, `[DAT-008]` |
| **DELETE** | `/api/notifications/{notificationId}` | `Authorization: Bearer <JWT>` | — | — | `{message: "Notification deleted successfully"}` | `{error: "FORBIDDEN", message: "Insufficient permissions"}` | `[REQ-016]`, `[EXC-003]`, `[DAT-008]` |

### Promotion Service

| HTTP Method | Endpoint | Request Headers | Path/Query Parameters | JSON Request Payload Schema | JSON Response Schema (Success) | JSON Response Schema (Failure) | Targeted Tag IDs |
|-------------|----------|-----------------|-----------------------|-----------------------------|--------------------------------|--------------------------------|------------------|
| **GET** | `/api/promotions` | `Authorization: Bearer <JWT>` | `?page=0&size=20`<br>`?centerId=UUID`<br>`?active=true/false` | — | `[{promoId, code, discountPercent, startDate, endDate, description}]` | `{error: "VALIDATION_FAILED", message: "Invalid query param"}` | `[REQ-017]`, `[ARC-008]`, `[DAT-009]` |
| **POST** | `/api/promotions` | `Authorization: Bearer <JWT>`<br>`Content-Type: application/json` | — | `{ "code": "string", "discountPercent": "short", "startDate": "yyyy-MM-dd", "endDate": "yyyy-MM-dd", "description": "string" }` | `{promoId: "uuid", message: "Promotion created"}` | `{error: "CONFLICT", message: "Promotion code already exists"}` | `[REQ-017]`, `[EXC-003]`, `[DAT-009]` |
| **GET** | `/api/promotions/{promoId}` | `Authorization: Bearer <JWT>` | — | — | `{promoId, code, discountPercent, startDate, endDate, description}` | `{error: "NOT_FOUND", message: "Promotion not found"}` | `[REQ-017]`, `[ARC-008]`, `[DAT-009]` |
| **PUT** | `/api/promotions/{promoId}` | `Authorization: Bearer <JWT>`<br>`Content-Type: application/json` | — | `{ "discountPercent": "short", "startDate": "yyyy-MM-dd", "endDate": "yyyy-MM-dd", "description": "string" }` | `{promoId, updatedAt: "string"}` | `{error: "FORBIDDEN", message: "Only Center Admin can update promotion"}` | `[REQ-017]`, `[EXC-003]`, `[DAT-009]` |
| **DELETE** | `/api/promotions/{promoId}` | `Authorization: Bearer <JWT>` | — | — | `{message: "Promotion deleted"}` | `{error: "FORBIDDEN", message: "Insufficient permissions"}` | `[REQ-017]`, `[EXC-003]`, `[DAT-009]` |

### Announcement Service

| HTTP Method | Endpoint | Request Headers | Path/Query Parameters | JSON Request Payload Schema | JSON Response Schema (Success) | JSON Response Schema (Failure) | Targeted Tag IDs |
|-------------|----------|-----------------|-----------------------|-----------------------------|--------------------------------|--------------------------------|------------------|
| **GET** | `/api/announcements` | `Authorization: Bearer <JWT>` | `?page=0&size=20`<br>`?centerId=UUID`<br>`?active=true/false` | — | `[{announcementId, title, content, startDate, endDate}]` | `{error: "VALIDATION_FAILED", message: "Invalid query param"}` | `[REQ-018]`, `[ARC-008]`, `[DAT-009]` |
| **POST** | `/api/announcements` | `Authorization: Bearer <JWT>`<br>`Content-Type: application/json` | — | `{ "title": "string", "content": "string", "startDate": "yyyy-MM-dd", "endDate": "yyyy-MM-dd" }` | `{announcementId: "uuid", message: "Announcement created"}` | `{error: "VALIDATION_FAILED", message: "Invalid payload"}` | `[REQ-018]`, `[EXC-003]`, `[DAT-009]` |
| **GET** | `/api/announcements/{announcementId}` | `Authorization: Bearer <JWT>` | — | — | `{announcementId, title, content, startDate, endDate}` | `{error: "NOT_FOUND", message: "Announcement not found"}` | `[REQ-018]`, `[ARC-008]`, `[DAT-009]` |
| **PUT** | `/api/announcements/{announcementId}` | `Authorization: Bearer <JWT>`<br>`Content-Type: application/json` | — | `{ "title": "string", "content": "string", "startDate": "yyyy-MM-dd", "endDate": "yyyy-MM-dd" }` | `{announcementId, updatedAt: "string"}` | `{error: "FORBIDDEN", message: "Only Center Admin can update announcement"}` | `[REQ-018]`, `[EXC-003]`, `[DAT-009]` |
| **DELETE** | `/api/announcements/{announcementId}` | `Authorization: Bearer <JWT>` | — | — | `{message: "Announcement deleted"}` | `{error: "FORBIDDEN", message: "Insufficient permissions"}` | `[REQ-018]`, `[EXC-003]`, `[DAT-009]` |

---

## 🚦 Xử Lý Lỗi & Mã Lỗi

| Mã Lỗi | HTTP Status | Mô tả | Khi nào xảy ra |
|---------|-------------|-------------|----------------|
| `VALIDATION_FAILED` | 400 | Dữ liệu yêu cầu không hợp lệ (thiếu trường bắt buộc, định dạng sai) | Tất cả các endpoint khi nhận payload không hợp lệ |
| `UNAUTHORIZED` | 401 | Thiếu hoặc không hợp lệ JWT token | Không có hoặc sai Bearer token |
| `FORBIDDEN` | 403 | Người dùng không có quyền cần thiết (RBAC) | PUT/DELETE trên tài nguyên của người dùng khác hoặc thiếu quyền Center Admin |
| `NOT_FOUND` | 404 | Tài nguyên không tồn tại | GET/PUT/DELETE với ID không tồn tại |
| `CONFLICT` | 409 | Xung đột dữ liệu (ví dụ: mã khuyến mãi trùng lặp) | POST Promotion với mã đã tồn tại |
| `PAYLOAD_TOO_LARGE` | 413 | Request payload vượt quá giới hạn kích thước cho phép | Tất cả các endpoint khi request quá lớn |
| `TOO_MANY_REQUESTS` | 429 | Vượt quá giới hạn tốc độ (hơn 30 request/phút) | Tất cả các endpoint khi vượt quá giới hạn |
| `INTERNAL_SERVER_ERROR` | 500 | Lỗi máy chủ không xác định | Bất kỳ lỗi không xác định nào ở phía server |
| `PUSH_DELIVERY_FAILED` | 502 | Gửi thông báo thất bại sau 3 lần thử lại | POST Notification khi gửi FCM/APNs/Zalo thất bại |

**Xử lý ngoại lệ theo quy định (EXC-003):**
- Bất kỳ lỗi `PUSH_DELIVERY_FAILED` nào được ghi log với `notificationId`, thông báo lỗi thô (`e.getMessage()`), và Tag ID `[EXC-003]`.
- Hệ thống tự động thử lại tối đa 3 lần với khoảng cách 5 phút giữa các lần thử.
- Sau 3 lần thử thất bại, trạng thái thông báo được đánh dấu `delivered = false` và một cảnh báo được gửi đến đội vận hành.

---

## 🔐 Bảo Mật & Tuân Thủ

- **Xác thực:** Tất cả các endpoint yêu cầu JWT token hợp lệ (JWT access token hết hạn sau 15 phút, refresh token hết hạn sau 7 ngày).
- **Authorization:** Kiểm tra quyền truy cập RBAC ở tầng controller (`RbacFilter`), đảm bảo chỉ System Admin, Center Admin, Manager, hoặc vai trò tương ứng mới có thể truy cập tài nguyên.
- **Mã hóa:** Dữ liệu truyền qua TLS 1.3; dữ liệu lưu trữ được mã hóa AES-256.
- **Che mặt PII:** Các trường nhạy cảm (`message` nếu chứa thông tin cá nhân, `userId`) được che mặt tự động (`***MASKED***`) trong log.
- **Ghi log kiểm toán:** Tất cả các thao tác CRUD được ghi log vào bảng `audit_log` với các trường: `action`, `entity`, `entity_id`, `user_id`, `timestamp`, `details`.
- **Rate Limiting:** Giới hạn 30 request/phút theo IP và theo tenant để ngăn chặn lạm dụng.
- **Input Validation:** Tất cả các request được xác thực bằng Jakarta Validation API; các tham số được kiểm tra chéo với danh sách trắng cho các trường được phép (ví dụ: `groupZalo` chỉ chấp nhận UUID hoặc tên nhóm đã đăng ký).

---

## 📈 Hiệu Suất & Khả Năng Mở Rộng

- **Caching:** Các endpoint GET được cache bằng Redis với key pattern `notification:{centerId}:{page}`, `promotion:{centerId}`, `announcement:{centerId}`.
- **Database Indexing:** Index trên `users.user_id`, `notifications.user_id`, `notifications.group_zalo`, `promotions.code`, `announcements.start_date`, `announcements.end_date`.
- **Horizontal Scaling:** Dịch vụ Notification, Promotion, và Announcement được triển khai dưới dạng stateless pods; Kubernetes HPA tự động scale dựa trên CPU (>70%) hoặc độ trễ request (>300ms).
- **Isolation:** Mỗi trung tâm được cô lập bằng schema PostgreSQL riêng (`public`, `center_{uuid}`) để đảm bảo tính đa tenant.

---

## 🔧 Hướng Dẫn Tích Hợp

### Tích Hợp Frontend (React Native / Next.js)
1. **Lấy token:** Sử dụng `AuthService` để lấy JWT token (lưu trữ trong secure storage).
2. **Gửi request:** Sử dụng `axios` với interceptor tự động chèn header `Authorization`.
3. **Xử lý lỗi:**
   js
   axios.interceptors.response.use(
     response => response,
     error => {
       if (error.response?.status === 401) {
         // Chuyển hướng đến trang đăng nhập
       } else if (error.response?.status === 403) {
         // Hiển thị thông báo không có quyền
       } else if (error.response?.status === 502) {
         // Hiển thị thông báo gửi thông báo thất bại, có thể thử lại
       }
       return Promise.reject(error);
     }
   );
   
4. **Gửi thông báo đẩy:** Triển khai `PushNotificationService` sử dụng `react-native-push-notification` (Android) và `react-native-push-notification` (iOS) để đăng ký token thiết bị với endpoint `POST /api/notifications/register-token`.

### Tích Hợp Zalo API
- Sử dụng `ZaloNotificationSender` để gửi tin nhắn đến nhóm Zalo được chỉ định (`groupZalo`).
- Triển khai OAuth2 với Zalo để lấy access token; token được lưu trữ an toàn trong Secret Manager.
- Xử lý lỗi rate limit và lỗi xác thực; retry với exponential backoff.

---

## 📚 Tài Liệu Tham Khảo Thêm

- **Firebase Cloud Messaging (FCM) Guide:** Tài liệu chính thức về gửi push notification đến thiết bị di động.
- **Zalo API Documentation:** Hướng dẫn về OAuth2, gửi tin nhắn nhóm và quản lý danh sách thành viên.
- **OWASP Top 10 for APIs:** Các biện pháp tốt nhất để bảo mật API REST.
- **Kubernetes HPA Best Practices:** Hướng dẫn về cấu hình tự động scale dựa trên metric.

---

## 📌 Ghi Chú Phiên Bản

| Phiên bản | Ngày.Giờ | Tác giả | Tóm tắt thay đổi |
|---------|----------|----------|----------------|
| 1.0 | 2026/08/18 16:31:58 | Kiến trúc sư hệ thống doanh nghiệp (Đặc vụ SA) | Phiên bản cơ sở cho hệ thống Notification & Promotion API |

---

*📍 Tài liệu này được tạo tự động theo quy định của hệ thống quản trị doanh nghiệp. Mọi sai lệch đều vi phạm quy trình tuân thủ.*