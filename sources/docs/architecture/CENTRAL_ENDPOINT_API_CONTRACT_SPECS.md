# Attendance Service Architecture Documentation

## Document Control Metadata

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Bản Thiết Kế** | ARCH-20260829223421 |
| **Tên Tài Liệu** | Attendance Service Architecture Documentation & Central Endpoint API Contract Specs |
| **Phiên Bản** | 1.0 (Đường cơ sở) |
| **Ngày Giờ** | 2026/08/29 22:34:21 |
| **Tác Giả** | Kiến Trúc Sư Hệ Thống Doanh Nghiệp (SA Agent) |
| **Phê Duyệt** | Đang chờ Rà Soát Quản Trị Kỹ Thuật |
| **Đường Dẫn Vật Lý** | `./sources/docs/architecture/CENTRAL_ENDPOINT_API_CONTRACT_SPECS.md` |

---

## 1. Tổng Quan Hệ Thống

### 1.1 Giới Thiệu Attendance Service
Attendance Service là một microservice độc lập thuộc hệ sinh thái **membership-hub**, chịu trách nhiệm quản lý, điều phối và xác nhận toàn bộ quy trình điểm danh học viên tại các trung tâm thành viên thông qua việc quét mã QR thời gian thực. Hệ thống được thiết kế theo tiêu chuẩn phản ứng sự kiện (Event-Driven Architecture) với khả năng mở rộng đàn hồi, đảm bảo tính bất biến dữ liệu, loại bỏ trùng lặp thông qua cơ chế idempotency nghiêm ngặt và xử lý chịu lỗi ngoại tuyến (offline-first fault tolerance).

### 1.2 Kiến Trúc Tổng Thể & Hạ Tầng Runtime
- **Mã định danh gói lõi (Java Package Base)**: `org.nlh4j.membershiphub.attendanceservice`
- **Nền tảng thực thi**: Quarkus 3.15.1 LTS (RESTEasy Reactive, Hibernate ORM Panache, SmallRye Reactive Messaging Kafka, SmallRye JWT, SmallRye Health)
- **Tối ưu hóa máy ảo**: Hỗ trợ GraalVM Native Image cho thời gian khởi động < 50ms và tiêu thụ bộ nhớ thấp (`[NFR-005]`)
- **Tầng lưu trữ bền vững (Persistence Layer)**: PostgreSQL 16 phân vùng dữ liệu theo `attendance_date` với các chỉ mục tổng hợp tối ưu (`[NFR-001]`, `[NFR-004]`)
- **Tầng đệm và chống trùng lặp**: Redis Cluster kết hợp Caffeine local cache để lưu trữ Idempotency Keys và Session
- **Hệ thống truyền tải sự kiện (Event Streaming)**: Apache Kafka (Kafka Topic: `attendance.scan.requested`, `attendance-events`)
- **Cơ chế bảo mật**: Zero-Trust, JWT Bearer Token (RS256 với 2048-bit keypair), chuẩn xác thực Jakarta Validation 3.0 (`[ARC-006]`, `[NFR-003]`)

---

## 2. C4 Container Architecture Components

### 2.1 Container: Attendance Service (Backend Microservice)
Attendance Service bao gồm các phân tầng kiến trúc chuyên biệt tuân thủ nguyên tắc Single Responsibility Principle (SRP) và mô hình Clean Architecture:

- **REST Controller Layer (`org.nlh4j.membershiphub.attendanceservice.controller`)**: Xử lý các yêu cầu HTTP Ingress từ API Gateway. Endpoint lõi `POST /api/v1/attendance/scan` tiếp nhận payload điểm danh QR, thực hiện xác thực JWT và kích hoạt pipeline nghiệp vụ (`[REQ-012]`, `[ARC-007]`).
- **Service & Decoder Layer (`org.nlh4j.membershiphub.attendanceservice.service`)**:
  - `AttendanceService`: Điều phối luồng nghiệp vụ kiểm tra đăng ký khóa học, ràng buộc idempotency và ghi nhận điểm danh (`[REQ-012]`, `[REQ-013]`).
  - `QrPayloadDecoder`: Giải mã chuỗi Base64 chứa thông tin mã hóa gồm `student_id` và `course_id`, kiểm tra tính toàn vẹn chữ ký số của mã QR (`[ARC-007]`).
- **Repository Layer (`org.nlh4j.membershiphub.attendanceservice.repository`)**: Tương tác với cơ sở dữ liệu PostgreSQL thông qua Hibernate ORM Panache, thực thi các ràng buộc duy nhất phức hợp `(student_id, course_id, attendance_date)` (`[DAT-004]`).
- **Messaging & Event Layer (`org.nlh4j.membershiphub.attendanceservice.messaging`)**: `KafkaAttendanceProducer` đẩy các sự kiện thành công (`attendance-events`) lên Apache Kafka để đồng bộ dữ liệu tới reporting và notification service (`[ARC-008]`).

---

## 3. Luồng Xử Lý Điểm Danh QR (QR Scan Processing Flow)

Quy trình xử lý điểm danh QR thời gian thực được mô tả chi tiết qua biểu đồ luồng dưới đây, tuân thủ nghiêm ngặt các tiêu chuẩn chống trùng lặp và phục hồi lỗi mạng ngoại tuyến (`[EXC-001]`, `[EXC-002]`, `[EXC-005]`):