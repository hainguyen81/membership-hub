# ENTERPRISE SYSTEM CONTEXT & STRICT GENERATION BLUEPRINT
## SYSTEM IDENTITY: MULTI-TENANT MEMBERSHIP HUB & ATTENDANCE CORE ENGINE
## PACKAGING ROOT: org.nlh4j.saas.membership.hub.*
## SOURCE CODE TARGET: ./sources/

---

## 1. TỔNG QUAN DỰ ÁN & KIẾN TRÚC HỆ THỐNG (SYSTEM OVERVIEW)

### 1.1. Bối cảnh và Mục tiêu (Mission Statement)
Hệ thống là một giải pháp SaaS Enterprise (Software-as-a-Service) đa thuê bao (Multi-Tenant Management) phục vụ quản lý học viên, thẻ thành viên, điểm danh thông minh qua mã QR dynamic. Hệ thống phục vụ song song hai đối tượng:
1. Web Admin (B2B): Dành cho chủ các trung tâm (Fitness, Gym, Trung tâm đào tạo) quản lý vận hành, cấu hình gói thẻ và giám sát biến động.
2. Mobile App (B2C): Dành cho học viên điểm danh, tra cứu thời hạn thẻ real-time, nhận thông báo đẩy qua đa kênh.

### 1.2. Sơ đồ Luồng Dữ liệu Kiến trúc (Core Architecture Flow)
Hệ thống tuân thủ nghiêm ngặt mô hình Event-Driven Architecture (EDA) kết hợp CQRS / Reactive Core để đảm bảo khả năng chịu tải cực cao tại các khung giờ cao điểm:
```
[Mobile App QR Scan] -> [Quarkus Gateway/API (Tenant Filter)]
                            | (Idempotent Check & Update DB)
                            |--> [PostgreSQL (Discriminator: tenant_id)]
                            |
                            |--> [Kafka: attendance-events] (Reactive Emit)
                                        |
                                        |--> [Kafka Consumer / Dispatcher Router]
                                                 |--> [Topic: zalo-personal] -> API Zalo OA (User)
                                                 |--> [Topic: zalo-group]    -> API Zalo Group CRM
                                                 |--> [Topic: mobile-push]   -> Firebase FCM Engine
```

---

## 2. CÔNG NGHỆ SỬ DỤNG (TECH STACK SPECIFICATIONS)

Mọi đoạn mã nguồn được sinh ra bắt buộc phải sử dụng chính xác các phiên bản và thư viện thuộc hệ sinh thái công nghệ sau:

### 2.1. Backend Core Stack
- Framework: Quarkus 3.x (Java 21 LTS), tối ưu hóa chế độ Reactive qua Mutiny Framework và Hibernate Reactive với Panache.
- Dependency Injection: CDI (Contexts and Dependency Injection) chuẩn Quarkus ArC.
- REST Engine: RESTEasy Reactive (Jackson làm JSON Provider).
- Database Driver: Vert.x Reactive PostgreSQL Client.
- Messaging Service: Quarkus SmallRye Reactive Messaging kết hợp Apache Kafka Client.
- Security: Quarkus OIDC (OpenID Connect) Reactive, tích hợp JOSE / Nimbus-JWT để verify tokens.
- Tiện ích mở rộng Backend: quarkus-hibernate-reactive-panache, quarkus-reactive-pg-client, quarkus-smallrye-reactive-messaging-kafka, quarkus-resteasy-reactive-jackson, quarkus-oidc, mapstruct, và **quarkus-smallrye-openapi**.

### 2.2. Frontend & Hybrid Mobile Stack
- Web/Mobile Native Base: Next.js 14+ (App Router) kết hợp TypeScript mã nguồn nghiêm ngặt (strict: true).
- Internationalization (i18n): next-intl xử lý định tuyến ngữ cảnh locale động.
- Styles & UI: Tailwind CSS + Shadcn UI / Radix Primitives.
- Mobile Runtime Wrapper: Capacitor Native Layer giúp build ứng dụng phân phối trực tiếp lên iOS và Android.

---

## 3. KIẾN TRÚC SỞ DỮ LIỆU ĐA THUÊ BAO (MULTI-TENANCY POSTGRESQL SPEC)

Hệ thống áp dụng chiến lược Shared Database, Discriminator Column. Tất cả các bảng dữ liệu nghiệp vụ bắt buộc phải chứa cột tenant_id làm khóa phân tách dữ liệu.

### 3.1. Database Schema DDL (Flyway / Liquibase Spec)
Target Path: ./sources/backend/src/main/resources/db/migration/V1.0.0__init_schema.sql
```
CREATE TABLE sys_tenants (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    company_code VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE app_users (
    id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL REFERENCES sys_tenants(id),
    email VARCHAR(191) NOT NULL,
    password_hash VARCHAR(255),
    full_name VARCHAR(255),
    provider VARCHAR(20) NOT NULL DEFAULT 'INTERNAL',
    provider_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_tenant_email UNIQUE (tenant_id, email)
);

CREATE TABLE student_cards (
    id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL REFERENCES sys_tenants(id),
    student_id VARCHAR(50) NOT NULL REFERENCES app_users(id),
    card_number VARCHAR(100) NOT NULL,
    remaining_days INT NOT NULL,
    expired_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_card_tenant_number UNIQUE (tenant_id, card_number)
);

CREATE TABLE attendance_logs (
    id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL REFERENCES sys_tenants(id),
    student_id VARCHAR(50) NOT NULL REFERENCES app_users(id),
    card_id VARCHAR(50) NOT NULL REFERENCES student_cards(id),
    checkin_date DATE NOT NULL,
    checked_in_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_attendance_day UNIQUE (tenant_id, student_id, checkin_date)
);

CREATE INDEX idx_users_tenant ON app_users(tenant_id);
CREATE INDEX idx_cards_tenant_student ON student_cards(tenant_id, student_id);
CREATE INDEX idx_attendance_tenant_date ON attendance_logs(tenant_id, checkin_date);

CREATE TABLE sys_promotions (
    id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL REFERENCES sys_tenants(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    discount_rate DECIMAL(5,2) DEFAULT 0.00,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification_delivery_logs (
    id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL REFERENCES sys_tenants(id),
    student_id VARCHAR(50) NOT NULL REFERENCES app_users(id),
    channel VARCHAR(20) NOT NULL,
    template_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    delivery_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_promotions_tenant ON sys_promotions(tenant_id, status);
CREATE INDEX idx_notif_logs_tenant_student ON notification_delivery_logs(tenant_id, student_id);
```

### 3.2. Quy tắc Cách ly Dữ liệu Đa thuê bao tự động (Automated Tenant Isolation)
AI khi generate các lớp Entity/Repository phải sử dụng @FilterDef và @Filter của Hibernate để tự động tiêm điều kiện tenant_id = :tenantId vào mọi câu lệnh SQL SELECT/UPDATE/DELETE. Không cho phép kỹ sư viết thủ công điều kiện tenant trong chuỗi truy vấn JQL/HQL.

---

## 4. KIẾN TRÚC XÁC THỰC KẾT HỢP (HYBRID AUTHENTICATION ARCHITECTURE)

Hệ thống hỗ trợ cơ chế xác thực kép (Dual-Engine Authentication):

### 4.1. Luồng Xác thực Nội bộ (Internal Authentication)
- Người dùng sử dụng Email + Mật khẩu. Mật khẩu bắt buộc phải được băm bằng thuật toán BCrypt với độ phức tạp (work factor) tối thiểu là 12.
- Hệ thống backend tự phát hành token JWT ngắn hạn (Short-lived JWT Access Token) được ký bằng thuật toán mã hóa bất đối xứng RS256.

### 4.2. Luồng Xác thực Bên thứ ba (Social OAuth2 Federated Engine)
- Tích hợp linh hoạt với Firebase, Google và Facebook Sign-In.
- Cơ chế JIT (Just-In-Time) Provisioning: Khi nhận yêu cầu đăng nhập bằng Social Token từ Mobile App, Gateway/OIDC Filter của Backend sẽ verify token đó trực tiếp với Authorization Server của nhà cung cấp. Nếu Signature hợp lệ và Email chưa tồn tại trong app_users của Tenant chỉ định, hệ thống tự động khởi tạo bản ghi User mới với cờ provider tương ứng.

---

## 5. LOGIC DIỂM DANH IDEMPOTENT & TRỪ NGÀY KIỂU PHÒNG GYM

### 5.1. Cơ chế Điểm danh Không trùng lặp (Idempotent Scanning Logic)
- Mã QR hiển thị trên ứng dụng học viên là một chuỗi Token được mã hóa AES-256 chứa cấu trúc dữ liệu JSON gồm: student_id, card_id, tenant_id, và timestamp (thời gian tạo, hết hạn sau 30 giây để chống gian lận chụp ảnh màn hình).
- Khi API Điểm danh nhận payload, hệ thống bóc tách dữ liệu và thực hiện truy vấn kiểm tra sự tồn tại của bản ghi trong bảng attendance_logs dựa trên bộ tổ hợp khóa (tenant_id, student_id, CURRENT_DATE).
- Luật xử lý: Nếu bản ghi đã tồn tại, API trả về HTTP Status 200 OK ngay lập tức kèm thông điệp cảnh báo "Đã ghi nhận điểm danh trước đó" mà không thực hiện ghi đè hoặc chèn mới bản ghi DB. Nếu chưa có, tiến hành ghi log điểm danh mới.

### 5.2. Logic Tính toán và Trừ ngày Thẻ kiểu Phòng Gym
- Khác với hình thức mua gói lượt học (trừ theo số lần quét), hệ thống áp dụng cơ chế đếm ngược thời gian thực (Gym-style Days Countdown tracking).
- Giá trị remaining_days trả về cho người dùng không phải là một giá trị tĩnh lưu cố định trong DB, mà được tính toán động (Dynamic Runtime Evaluation) theo công thức: Remaining_days = max(0, expired_at - CURRENT_DATE).
- Sau mỗi lượt quét thành công đầu tiên trong ngày, hệ thống cập nhật đồng bộ trạng thái kích hoạt của thẻ, ghi nhận lịch sử và kích hoạt luồng bắn sự kiện thông báo.

## 5.3. LOGIC CẢNH BÁO HẾT HẠN THẺ & CHĂM SÓC KHÁCH HÀNG TỰ ĐỘNG
- **Hàng rào cảnh báo 3 ngày (Expiration Threshold):** Hệ thống Web Admin phải thực hiện quét động thời gian thực (Dynamic Scan) các bản ghi trong bảng `student_cards` có số ngày hiệu lực còn lại `remaining_days <= 3`. Các học viên thuộc nhóm này sẽ được gắn cờ cảnh báo rủi ro `CRITICAL_EXPIRATION` để hiển thị nổi bật trên màn hình Dashboard của chủ trung tâm.
- **Cơ chế Trigger Zalo OA chủ động:** Từ giao diện Web Admin, khi người dùng kích hoạt hành động "Gửi thông báo gia hạn", hệ thống backend không được xử lý đồng bộ để tránh nghẽn luồng. Thay vào đó, API phải gửi ngay một lệnh điều hướng tác vụ vào Kafka topic `notification-zalo-personal` kèm theo mã mẫu tin nhắn (Zalo Template ID) cấu hình sẵn: *"Thẻ của bạn sẽ hết hạn vào ngày [Ngày]. Vui lòng đóng tiền trước ngày [Ngày] để nhận ưu đãi gia hạn"*.
- **Thông báo khóa học mới:** Hệ thống hỗ trợ gửi tin nhắn hàng loạt theo phân khúc học viên (Segmented Broadcast) qua Zalo Group CRM hoặc Zalo OA để thông báo lịch khai giảng các khóa học mới dựa trên sở thích hoặc lịch sử tập luyện của học viên.
- **Quản lý & Phân phối Chiến dịch Khuyến mãi:** Cho phép Admin tạo mã ưu đãi hoặc chương trình giảm giá gia hạn thẻ (lưu trữ trong bảng sys_promotions). Khi một chiến dịch khuyến mãi được kích hoạt, hệ thống hỗ trợ lập lịch và phân phối tin nhắn hàng loạt (Bulk Push) đến phân khúc nhóm học viên mục tiêu qua các cổng kết nối ngoại vi.
- **Nhật ký Phân phối Thông báo (Notification Delivery Audit Trail):** Mọi tin nhắn gửi đi từ hệ thống (quét mã điểm danh tự động, nhắc gia hạn tự động, hoặc admin bấm gửi thông báo khuyến mãi thủ công) BẮT BUỘC phải tạo một bản ghi ở trạng thái PENDING trong bảng notification_delivery_logs. Sau khi các Consumer phía backend kết nối thành công với API bên thứ ba (Zalo Open API / Firebase FCM) và nhận phản hồi kết quả, trạng thái này phải được cập nhật ngay thành SENT hoặc FAILED kèm mã lỗi và nội dung chi tiết để phục vụ tra cứu, đối soát lịch sử trên giao diện Web Admin UI.

---

## 6. KIẾN TRÚC EVENT-DRIVEN & ĐỒNG THỜI GỬI THÔNG BÁO QUA KAFKA

Mọi tác vụ điểm danh sau khi vượt qua bước kiểm tra trùng lặp sẽ kích hoạt quy trình xử lý bất đồng bộ thông qua Apache Kafka để giải phóng luồng xử lý chính của API, duy trì phản hồi dưới 50ms.

### 6.1. Thiết kế Mô hình Kafka Topics
- Topic Gốc: attendance-events (Chứa payload điểm danh gốc, phân vùng - Partition theo tenant_id).
- Topic Điều hướng Hạ nguồn (Downstream Split Topics):
  - notification-zalo-personal: Xử lý gửi tin nhắn trực tiếp đến số điện thoại học viên qua Zalo OA.
  - notification-zalo-group: Xử lý gửi tin thông báo vào Zalo Group của lớp học / trung tâm nơi học viên sinh hoạt.
  - notification-push-fcm: Xử lý đẩy Notification thời gian thực lên thiết bị di động cá nhân qua Firebase Cloud Messaging.

### 6.2. Cơ chế Phân tách Luồng Đồng thời (Asynchronous Fan-Out Router)
Một Service Kafka Consumer chịu trách nhiệm lắng nghe Topic gốc attendance-events. Khi nhận được một sự kiện mới, Consumer này sử dụng cơ chế xử lý Reactive Streams phân phối đồng thời dữ liệu (Fan-out) sang 3 Emitter hạ nguồn độc lập. Việc gửi tin lên 3 kênh này phải diễn ra song song và không chặn nhau (Non-blocking I/O).

### 6.3. Mô hình Xử lý Lỗi và Khôi phục (Resilience & DLQ Patterns)
Mỗi Consumer Group phải được trang bị cơ chế Retry Topic (thử lại tối đa 3 lần với khoảng cách tăng dần exponential backoff) và Dead Letter Queue (DLQ). Khi một cổng API bên thứ ba (như Zalo API) bị sập, tin nhắn lỗi sẽ được cách ly vào topic DLQ tương ứng để xử lý thủ công, tránh gây nghẽn toàn bộ luồng xử lý sự kiện của hệ thống.

---

## 7. KIẾN TRÚC CẦU NỐI DI ĐỘNG & SEO ĐA NGÔN NGỮ (FRONTEND SPEC)

### 7.1. Định vị và Nhận diện Ngôn ngữ Tự động (Locale Detection Middleware)
Ứng dụng Next.js sử dụng một Middleware chạy ở tầng Edge để giải quyết bài toán nhận diện ngôn ngữ theo thứ tự ưu tiên nghiêm ngặt sau:
1. Ưu tiên 1: Trạng thái cấu hình ngôn ngữ do người dùng chủ động lựa chọn trong lịch sử, được lưu trong Cookie hoặc WebStorage/LocalStorage.
2. Ưu tiên 2: Đối với môi trường chạy ứng dụng Native di động qua Capacitor, kiểm tra trực tiếp thông qua API Device.getLanguageCode().
3. Ưu tiên 3: Đọc giá trị tiêu đề HTTP Accept-Language gửi lên từ Browser của người dùng.
4. Fallback: Trả về ngôn ngữ mặc định hệ thống (vi).

### 7.2. Chuẩn SEO Đa Ngôn ngữ cho Cả Web và App Mobile
Mọi trang giao diện công khai (Public Pages) bắt buộc phải tích hợp cấu hình Metadata động:
- Tự động sinh danh sách thẻ link rel="alternate" hreflang="x" href="url" tương ứng với toàn bộ các ngôn ngữ hệ thống hỗ trợ để tránh lỗi trùng lặp nội dung theo tiêu chuẩn của Google Search Index.
- Cấu hình tệp sitemap.js và robots.js động để tự động bóc tách sơ đồ trang web theo từng phân vùng ngôn ngữ địa phương (/vi/, /en/).

## 7.3. HÀNG RÀO BẢO VỆ NEXT.JS KHI ĐÓNG GÓI DI ĐỘNG (CAPACITOR SPEC)

Để đảm bảo mã nguồn Next.js App Router vận hành mượt mà, không lỗi khi đóng gói thành ứng dụng di động native (iOS/Android) thông qua Capacitor, AI bắt buộc phải tuân thủ nghiêm ngặt các hàng rào bảo vệ (safety rails) sau trong quá trình sinh code:

### 7.3.1. Tuyệt đối dùng Client-Side Fetching & Đường dẫn API Tuyệt đối
- **Cấm SSR trên Mobile:** Toàn bộ tính năng Render phía Server (Server Components, `getServerSideProps` hoặc fetch dữ liệu động trực tiếp trên server) đều bị CẤM đối với các trang thuộc phân hệ Mobile (`/app/[locale]/app/`). Tất cả các trang này phải sử dụng Client-Side Fetching thuần túy thông qua cờ `'use client'` kết hợp với `useEffect` hoặc SWR/React Query.
- **Đường dẫn API Tuyệt đối (Absolute URLs):** Mọi request gọi API từ ứng dụng di động bắt buộc phải sử dụng đường dẫn tuyệt đối, được cấu hình qua biến môi trường (Ví dụ: `process.env.NEXT_PUBLIC_API_BASE_URL`). Việc sử dụng đường dẫn tương đối (`/api/v1/...`) sẽ gây lỗi mất kết nối hoàn toàn do môi trường mobile chạy dưới giao thức file native (`capacitor://` hoặc `http://localhost`).

### 7.3.2. Cơ chế Chống lỗi Hydration & Khớp dịch dữ liệu Storage
- **Chống lỗi Hydration Mismatch:** AI không được phép render trực tiếp các giá trị đọc từ bộ nhớ (Cookie, LocalStorage) ngay trong lần render đầu tiên của component. Tất cả các tác vụ đọc trạng thái đăng nhập, ngôn ngữ bắt buộc phải nằm trong hook `useEffect` sau khi component đã mount xong lên DOM, hoặc sử dụng cơ chế Dynamic Import với cấu hình `{ ssr: false }` để tránh lỗi trắng màn hình (Hydration Error).
- **Lớp trừu tượng hóa Storage:** Không gọi trực tiếp `window.localStorage` một cách riêng lẻ. AI phải viết một Service trung gian xử lý lưu trữ dữ liệu, tự động chuyển đổi sang thư viện `@capacitor/preferences` nếu phát hiện ứng dụng đang chạy trong môi trường app di động native.

### 7.3.3. Tương thích Phần cứng Thiết bị & Ranh giới Giao diện
- **Xử lý Tai thỏ và Nốt ruồi (Safe Area Insets):** Toàn bộ giao diện Layout của Mobile App bắt buộc phải tích hợp các thuộc tính padding của Tailwind CSS bám theo biến môi trường CSS của thiết bị: `pt-[env(safe-area-inset-top)]` và `pb-[env(safe-area-inset-bottom)]`. Quy tắc này đảm bảo các nút bấm hoặc thanh điều hướng không bị che khuất bởi phần khuyết tật màn hình hoặc thanh tác vụ hệ thống.
- **Kiểm soát nút Quay lại Phần cứng (Hardware Back Button):** Đối với môi trường Android, các màn hình chức năng (như quét mã QR hoặc hộp thoại pop-up kết quả) phải đăng ký lắng nghe sự kiện nút Back của hệ điều hành thông qua API `App.addListener('backButton', ...)` của Capacitor. Khi người dùng bấm nút Back cứng, hệ thống phải đóng pop-up/modal lại một cách native thay vì thoát đột ngột làm crash toàn bộ trạng thái ứng dụng.

---

## 8. QUẢN LÝ CONTEXT & MÔ HÌNH DTO-MAPPER ENTERPRISE

### 8.1. Cấu trúc Giao tiếp Dữ liệu
Tuyệt đối không để lộ các lớp Entity thuộc tầng cơ sở dữ liệu ra ngoài tầng API Controller. Tất cả dữ liệu đầu vào và đầu ra phải thông qua các lớp Data Transfer Object (DTO) độc lập.
- Tầng Mapping: Sử dụng thư viện MapStruct cấu hình ở chế độ componentModel = "jakarta" để biên dịch tự động các lớp Mapper thành mã nguồn Java thuần túy ở thời điểm compile-time.
- Tầng Validation: Sử dụng jakarta.validation.constraints (@NotNull, @NotBlank, @Size, @Pattern) trên mọi trường dữ liệu của DTO đầu vào, kết hợp với @Valid tại tầng Resource/Controller.

### 8.2. Cấu trúc Package Chuẩn hóa (Enterprise Package Topology)
Tất cả các dịch vụ, kho lưu trữ, bộ điều khiển, thực thể, và DTO bắt buộc phải được đặt chính xác trong các thư mục con tương ứng thuộc nhánh gói gốc sau:
```
org.nlh4j.saas.membership.hub
├── config          # Hệ thống cấu hình toàn cục (Security, Kafka, Multi-Tenancy Engine)
├── domain          # Thực thể nghiệp vụ lõi (Entities) và các kiểu Enum định nghĩa trạng thái
├── repository      # Các lớp thao tác cơ sở dữ liệu (PanacheRepositoryBase)
├── service         # Giao diện (Interface) và Lớp thực thi (Implementation) xử lý logic nghiệp vụ
├── web             # API Resource/Controllers xử lý REST requests đầu vào
│   ├── dto         # Các lớp DTO phân tách rõ ràng Request và Response
│   └── mapper      # Cấu hình giao diện ánh xạ dữ liệu MapStruct Mappers
└── integration     # Cầu nối tích hợp hệ thống bên thứ ba (ZaloClient, FCMClient, FirebaseVerifier)
```

## 8.3. QUY CHUẨN ĐẶC TẢ API TỰ ĐỘNG QUA OPENAPI VÀ SWAGGER UI

Để đảm bảo các kỹ sư Frontend và các hệ thống bên thứ ba có thể dễ dàng tích hợp, mọi API Endpoint được sinh ra phía Backend bắt buộc phải được tài liệu hóa tự động bằng OpenAPI 3.0 thông qua extension `quarkus-smallrye-openapi`.

### 8.3.1. Hàng rào Annotation bắt buộc tại tầng Web Resource
AI Coder Agent khi sinh mã nguồn cho các lớp Controller/Resource phải tích hợp đầy đủ các cấu trúc Annotation chuẩn sau để Swagger UI tự động render trực quan:
- **Đặc tả hàm (`@Operation`):** Bắt buộc phải có thuộc tính `summary` (mô tả ngắn tác vụ bằng tiếng Anh) và `description` (mô tả chi tiết luồng xử lý).
- **Đặc tả phản hồi (`@APIResponse`):** Mỗi API phải khai báo tối thiểu 2 trạng thái phản hồi: `@APIResponse(responseCode = "200", description = "Success")` cho luồng xử lý thành công, và các mã lỗi tương ứng (`400` cho Validation Error, `401`/`403` cho Security/Tenant Error, `500` cho System Error) kèm theo thuộc tính `content` trỏ đến cấu trúc DTO lỗi chuẩn.
- **Đặc tả tham số (`@Parameter`):** Mọi tham số truyền vào qua `@PathParam`, `@QueryParam` hoặc tiêu đề HTTP (như mã Tenant hoặc Token) phải đi kèm Annotation `@Parameter(description = "...", required = true)` để hiển thị rõ ràng trên giao diện thử nghiệm Swagger.

### 8.3.2. Cấu hình hiển thị và Bảo mật trên Swagger UI
- **Tích hợp lá chắn Đa thuê bao:** Trên giao diện Swagger UI công khai, hệ thống phải cấu hình một trường nhập liệu Header toàn cục (Global Security Scheme) cho `X-Tenant-Id` hoặc `Authorization` Bearer Token. Điều này cho phép kiểm thử viên có thể đính kèm mã Tenant hoặc Token xác thực trực tiếp trên trình duyệt khi gọi thử API.
- **Cách ly môi trường Production:** Cấu hình trong `application.properties` phải bật Swagger UI tự động ở môi trường Dev/Staging để phục vụ phát triển, nhưng phải tắt hoàn toàn tính năng hiển thị tài liệu này khi deploy lên cụm GKE Production nhằm ngăn chặn rò rỉ cấu trúc hệ thống (Endpoint Leakage).

---

## 9. QUY CHUẨN ENTERPRISE SECURITY & BẢO VỆ CHỐNG SQL INJECTION

- SQL Injection Prevention: Không sử dụng nối chuỗi chuỗi trực tiếp để tạo câu lệnh SQL. Toàn bộ các truy vấn động phải dùng cơ chế tham số hóa (Parameterized Queries) do Hibernate Reactive / Panache cung cấp.
- XSS Protection: Tầng Frontend Next.js phải bật cơ chế Content Security Policy (CSP) chặt chẽ, mọi chuỗi text render ra màn hình phải được mã hóa thực thể HTML tự động thông qua cơ chế mặc định của React JSX.
- CORS Configuration: Cấu hình chính xác danh sách white-list tên miền được phép truy cập hệ thống theo từng môi trường, tuyệt đối không dùng giá trị đại diện * trong môi trường Production.
- Data Masking: Các thông tin nhạy cảm của học viên như mật khẩu băm, mã OTP, số điện thoại phải được ẩn đi (masking) trong toàn bộ các tệp nhật ký hệ thống (Application Logs).

---

## 10. TRỌN BỘ MÃ NGUỒN MẪU TIÊU CHUẨN (PRODUCTION BLUEPRINTS)

### 10.1. Tầng Logic Nghiệp Vụ Điểm Danh (Idempotent Core Service)
Target Path: ./sources/backend/src/main/java/org/nlh4j/saas/membership/hub/service/impl/AttendanceServiceImpl.java
```
package org.nlh4j.saas.membership.hub.service.impl;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.nlh4j.saas.membership.hub.domain.AttendanceLog;
import org.nlh4j.saas.membership.hub.repository.AttendanceLogRepository;
import org.nlh4j.saas.membership.hub.service.AttendanceService;
import org.nlh4j.saas.membership.hub.web.dto.CheckinRequest;
import org.nlh4j.saas.membership.hub.web.dto.CheckinResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core Implementation of Attendance Validation Processing Service.
 * Implements strict non-blocking reactive paradigms via SmallRye Mutiny.
 *
 * @author Principal Spring Boot & Quarkus Engineer
 */
@ApplicationScoped
public class AttendanceServiceImpl implements AttendanceService {

    @Inject
    AttendanceLogRepository attendanceLogRepository;

    @Inject
    @Channel("attendance-events")
    Emitter<String> kafkaEmitter;

    @Override
    @WithTransaction
    public Uni<CheckinResponse> processStudentCheckin(CheckinRequest request) {
        LocalDate today = LocalDate.now();

        return attendanceLogRepository.findDuplicateCheckin(request.getTenantId(), request.getStudentId(), today)
                .flatMap(isDuplicate -> {
                    if (isDuplicate) {
                        CheckinResponse redundantResponse = new CheckinResponse();
                        redundantResponse.setStatus("SUCCESS");
                        redundantResponse.setMessage("Attendance already recorded for today.");
                        redundantResponse.setIsDuplicatedScan(true);
                        return Uni.createFrom().item(redundantResponse);
                    }

                    AttendanceLog newLog = new AttendanceLog();
                    newLog.setId(UUID.randomUUID().toString());
                    newLog.setTenantId(request.getTenantId());
                    newLog.setStudentId(request.getStudentId());
                    newLog.setCardId(request.getCardId());
                    newLog.setCheckinDate(today);
                    newLog.setCheckedInAt(LocalDateTime.now());

                    return attendanceLogRepository.persist(newLog)
                            .map(persistedLog -> {
                                String jsonPayload = String.format(
                                    "{\"eventId\":\"%s\",\"tenantId\":\"%s\",\"studentId\":\"%s\"}",
                                    persistedLog.getId(), persistedLog.getTenantId(), persistedLog.getStudentId()
                                );
                                kafkaEmitter.send(jsonPayload);

                                CheckinResponse successResponse = new CheckinResponse();
                                successResponse.setStatus("SUCCESS");
                                successResponse.setMessage("Check-in processed successfully.");
                                successResponse.setIsDuplicatedScan(false);
                                return successResponse;
                            });
                });
    }
}
```

## 10.2. Tầng Logic Nghiệp Vụ Web Admin Quản Lý Thẻ & Trigger Zalo OA
Target Path: ./sources/backend/src/main/java/org/nlh4j/saas/membership/hub/service/impl/AdminMembershipServiceImpl.java
```
package org.nlh4j.saas.membership.hub.service.impl;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.nlh4j.saas.membership.hub.repository.StudentCardRepository;
import org.nlh4j.saas.membership.hub.service.AdminMembershipService;
import org.nlh4j.saas.membership.hub.web.dto.ZaloNotificationRequest;

/**
 * Enterprise Service executing Administrative Membership operations.
 * Handles strict multi-tenant analytics and proactive Zalo OA event triggering.
 *
 * @author Principal Spring Boot & Quarkus Engineer
 */
@ApplicationScoped
public class AdminMembershipServiceImpl implements AdminMembershipService {

    @Inject
    StudentCardRepository studentCardRepository;

    @Inject
    @Channel("notification-zalo-personal")
    Emitter<String> zaloPersonalEmitter;

    @Override
    @WithTransaction
    public Uni<Void> triggerManualExpirationNotice(ZaloNotificationRequest request) {
        // Enforce boundary verification ensuring the request template matches enterprise security rules
        String jsonPayload = String.format(
            "{\"tenantId\":\"%s\",\"studentId\":\"%s\",\"templateType\":\"EXPIRATION_ALERT\",\"dueDate\":\"%s\"}",
            request.getTenantId(), request.getStudentId(), request.getDueDate()
        );
        
        // Non-blocking asynchronous emit straight into Kafka channel to decouple external Zalo API overhead
        zaloPersonalEmitter.send(jsonPayload);
        return Uni.createFrom().voidItem();
    }
}
```

---

## 11. HÀNG RÀO BẢO VỆ & HƯỚNG DẪN DÀNH CHO AI GENERATE CODE (SAFETY RAILS)

Khi nhận được yêu cầu phát triển bất kỳ cấu phần nào từ người dùng, AI phải kiểm tra và tự áp dụng các bộ lọc sau:
1. No Explanations Policy: Không viết văn bản giải thích dài dòng ở đầu hoặc cuối câu trả lời để tiết kiệm tối đa lượng token giới hạn của tài khoản. Chỉ xuất ra cấu trúc file và mã nguồn hoàn chỉnh.
2. Context Retention Integrity: Đọc kỹ thông tin cấu trúc cơ sở dữ liệu và các tệp tin đã được sinh ra ở các ngày làm việc trước đó (lưu tại .ai-agents/contexts/) trước khi viết mã nguồn mới, tránh tình trạng xung đột hoặc sai lệch kiểu dữ liệu hệ thống.
3. Mandatory English Javadocs: Mọi định nghĩa hàm lớp, các khối lệnh điều kiện phức tạp bắt buộc phải đi kèm chú thích kỹ lưỡng (Docs) bằng TIẾNG ANH theo đúng văn phong chuẩn Enterprise.
4. Single-file Code Block Isolation: Gói gọn mã nguồn của mỗi tệp tin vào đúng một block markdown kèm theo chú thích đường dẫn chính xác ở dòng đầu tiên.

## 12. CHIẾN LƯỢC KIỂM THỬ TỰ ĐỘNG & BLUEPRINT TEST TIÊU CHUẨN
- **Yêu cầu độ bao phủ (Coverage):** Mọi dịch vụ xử lý logic nghiệp vụ và các bộ lọc mở rộng bắt buộc phải duy trì tỷ lệ bao phủ mã nguồn tự động tối thiểu là 85%.
- **Kiểm thử tích hợp phía Backend:** Sử dụng Quarkus Dev Services kết hợp với Testcontainers để khởi chạy các thực thể PostgreSQL và Apache Kafka thực tế trong container. Tuyệt đối nghiêm cấm việc sử dụng cơ sở dữ liệu giả lập trong bộ nhớ (như H2) nhằm bảo toàn tính chính xác của các giao dịch Reactive.
- **Kiểm thử giao diện phía Frontend:** Sử dụng Jest và React Testing Library để kiểm tra các trạng thái định tuyến đa ngôn ngữ và xác thực lỗi Hydration. Các kịch bản kiểm thử diện rộng (E2E) phải được thực thi qua Playwright giả lập môi trường khung màn hình di động.

## 13. KIẾN TRÚC HYBRID MOBILE & CẦU NỐI THIẾT BỊ NATIVE
- **Tương thích vùng hiển thị màn hình:** Giao diện di động chạy trong lớp vỏ Capacitor bắt buộc phải sử dụng các hằng số đệm của môi trường CSS (`pt-[env(safe-area-inset-top)]`, `pb-[env(safe-area-inset-bottom)]`) để không bị che khuất bởi tai thỏ, nốt ruồi hoặc thanh điều hướng hệ thống.
- **Bắt sự kiện phần cứng:** Nút quay lại (Back button) trên thiết bị Android phải được kiểm soát qua hàm lắng nghe sự kiện `App.addListener('backButton', ...)` của Capacitor. Thao tác bấm nút Back của người dùng phải được xử lý cục bộ để đóng các thành phần modal/pop-up thay vì làm tắt ứng dụng đột ngột.

## 14. CỔNG CRM TÍCH HỢP & CẦU NỐI THÔNG BÁO ĐA KÊNH
- **Xử lý phân tách luồng đồng thời:** Luồng thông báo sau điểm danh phải được phân phối bất đồng bộ và song song (không chặn nhau) tới ba mục tiêu: Gửi tin nhắn cá nhân tới học viên qua Zalo OA, cập nhật trạng thái lớp học vào Zalo Group CRM, và đẩy thông báo thời gian thực lên thiết bị qua Firebase Cloud Messaging (FCM).
- **Cách ly dữ liệu lỗi (Poison Pill):** Mọi Consumer lắng nghe qua các cổng tích hợp phải được trang bị cơ chế thử lại (exponential backoff tối đa 3 lần) và cách ly lỗi hoàn toàn khỏi luồng xử lý chính bằng các topic hàng đợi thư chết (Dead Letter Queue - DLQ).

## 15. QUY CHUẨN ĐẶT TÊN PACKAGE & QUẢN LÝ ĐỐI TƯỢNG TRUYỀN TẢI DTO
- **Kiểm soát cấu trúc Package:** Tất cả các lớp lưu trữ (Repository), thực thể (Entity), dịch vụ nghiệp vụ (Service) và cổng kết nối (Client) bắt buộc phải nằm chính xác trong cấu trúc thư mục thuộc gói gốc doanh nghiệp: `org.nlh4j.saas.membership.hub.*`.
- **Đóng gói ranh giới dữ liệu qua DTO:** Tuyệt đối không để lộ các thực thể cơ sở dữ liệu (Entity) ra ngoài tầng API công khai. Mọi dữ liệu đầu vào và đầu ra phải đi qua các đối tượng DTO bất biến, được chuyển đổi tự động thông qua thư viện MapStruct ở thời điểm compile-time.

## 16. QUY TẮC VẬN HÀNH NGHIÊM NGẶT CỦA MÔ HÌNH MULTI-AGENT
Mọi issue phát triển tính năng gửi lên hệ thống bắt buộc phải được xử lý tuần tự qua chuỗi quy trình cô lập gồm 4 Agent độc lập, không được phép bỏ qua bất kỳ bước nào:
- **Coder Agent:** Đọc hiểu file `context.md` tổng và phân đoạn ngữ cảnh của ngày. Sinh mã nguồn nghiệp vụ chuẩn production, xử lý bất đồng bộ và ghi dữ liệu trực tiếp vào đúng đường dẫn chỉ định bởi khóa `target_component`.
- **Tester Agent:** Tiếp nhận file mã nguồn từ Coder Agent, viết trọn bộ kịch bản kiểm thử tự động chất lượng cao và lưu vào đúng đường dẫn `test_component`. Tuyệt đối cấm viết code test rỗng hoặc hàm assert giả.
- **Reviewer & Fixer Agent:** Giám sát nhật ký biên dịch và kết quả chạy test. Thực hiện tối đa 3 lần sửa đổi tự động nhằm vá các lỗ hổng truy vấn thô, loại bỏ kẽ hở bảo mật JWT và dọn dẹp lỗi cú pháp.
- **Documentation Agent:** Xem xét toàn bộ mã nguồn nghiệp vụ và file test đã được phê duyệt để tự động biên soạn tài liệu đặc tả kỹ thuật chi tiết bằng định dạng markdown vào đúng đường dẫn `doc_component`.
- **Deploy Agent (GCP):**
  * Vai trò: Kỹ sư hạ tầng lưu trữ đám mây (Cloud Registry Infrastructure Engineer).
  * Nhiệm vụ: Chỉ kích hoạt thủ công khi có cờ `--release`. Chịu trách nhiệm kết nối xác thực tài khoản Google Cloud Platform, biên dịch và tối ưu hóa Docker hình ảnh (GraalVM Native Image), sau đó đẩy sản phẩm sạch lên kho lưu trữ Google Artifact Registry (GAR).
- **Deploy Agent (GKE):**
  * Vai trò: Kỹ sư điều phối container Kubernetes (Kubernetes Container Release Engineer).
  * Nhiệm vụ: Chỉ kích hoạt thủ công khi có cờ `--release`. Chịu trách nhiệm kết nối API cụm máy chủ GKE, áp dụng trực tiếp các tệp cấu hình YAML (`infrastructure/k8s`) hoặc thực thi lệnh cập nhật cuốn chiếu (Rolling Update Rollout) trên các Pods đang chạy mà không làm gián đoạn hệ thống.

## 17. QUY TRÌNH QUẢN LÝ NHÁNH GIT TỰ ĐỘNG CHO PHIÊN LÀM VIỆC THEO NGÀY

Để tối ưu hóa dung lượng token của tài khoản AI mô hình miễn phí, ngăn chặn tình trạng tràn ngữ cảnh (Context Bloat), đồng thời đảm bảo mã nguồn được lưu trữ an toàn, hệ thống bắt buộc phải tuân thủ nghiêm ngặt quy trình cô lập nhánh Git tự động sau mỗi ngày làm việc:

### 17.1. Quy tắc Khởi tạo và Cô lập Nhánh (Branch Forking Control)
- **Nhánh gốc cố định (Source Base):** Tất cả các phiên làm việc của từng ngày (Day X) không được phép can thiệp trực tiếp vào các nhánh chính. Mọi tiến trình phải bắt đầu bằng lệnh khởi tạo (fork) một nhánh tính năng mới hoàn toàn độc lập từ nhánh gốc `features/development`.
- **Chuẩn đặt tên nhánh theo ngày (Naming Convention):** Nhánh mới được sinh ra bắt buộc phải tuân thủ chính xác cấu trúc định dạng: `features/development-day-X` (Ví dụ: `features/development-day-1`, `features/development-day-2`).

### 17.2. Điều kiện Kiểm chuẩn Thượng nguồn (Strict Guard Pipeline Gates)
Hệ thống Orchestrator tuyệt đối không được phép thực hiện lệnh commit hoặc đẩy mã nguồn lên kho lưu trữ từ xa (Remote Repository) nếu chưa vượt qua toàn bộ các hàng rào kiểm tra tự động sau:
1. **Lá chắn biên dịch (Compilation Gate):** Dự án Java Backend phải chạy thành công lệnh `mvn clean test-compile` với mã trạng thái trả về bằng 0 (Exit Code 0). Dự án Frontend Next.js phải hoàn thành lệnh `npm run build` không chứa lỗi cú pháp nghiêm trọng.
2. **Lá chắn kiểm thử (Testing Gate):** Tester Agent phải sinh mã kiểm thử thực tế và chạy đạt tỷ lệ bao phủ mã nguồn (Code Coverage) >= 85%, không có bất kỳ ca kiểm thử (test case) nào bị thất bại.
3. **Lá chắn rà soát (Fixer Gate):** Bug Fixer Agent phải hoàn thành việc tự động vá lỗi (Auto-patch) và làm sạch toàn bộ log báo lỗi của hệ thống biên dịch.

### 17.3. Chu trình Đẩy mã nguồn & Giải phóng Bộ nhớ (Commit, Push & Memory Flushing)
- **Commit và Push tự động:** Ngay sau khi các lá chắn ở Mục 16.2 báo trạng thái Sạch (Clean), hệ thống tự động gom toàn bộ mã nguồn nghiệp vụ (`target_component`), mã kiểm thử (`test_component`), và tài liệu kỹ thuật (`doc_component`) vào một commit duy nhất với thông điệp: `feat(day-X): complete enterprise sub-tasks for day X [AI Pipeline]`. Sau đó, tiến hành đẩy (push) nhánh `features/development-day-X` lên máy chủ Git từ xa.
- **Giải phóng ngữ cảnh (Context Reseeding):** Phiên làm việc của ngày hôm đó chính thức khép kín tại đây. Trạng thái mã nguồn của ngày cũ sẽ được đóng gói lại thành một file tóm tắt API/Interface siêu nhẹ đặt tại thư mục hệ thống: `.ai/.plan/.context/history_states/dayX_state.md` (Trong đó X là số ngày tương ứng). Ở ngày làm việc tiếp theo (Day X+1), AI Agent sẽ chỉ nạp file tóm tắt trạng thái này kèm theo file Context Main và Context Phase, hoàn toàn giải phóng và không đọc lại các file mã nguồn thô cũ, giúp tiết kiệm đến 80% dung lượng Token tiêu hao cho các mô hình AI miễn phí.


