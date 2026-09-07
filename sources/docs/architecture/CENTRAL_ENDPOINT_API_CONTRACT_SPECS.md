# Attendance Service Architecture Documentation

## Document Control Metadata

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Bản Thiết Kế** | ARCH-20260829223421 |
| **Tên Tài Liệu** | Attendance Service Architecture Documentation |
| **Phiên Bản** | 1.0 (Đường cơ sở) |
| **Ngày Giờ** | 2026/08/29 22:34:21 |
| **Tác Giả** | Kiến Trúc Sư Hệ Thống Doanh Nghiệp (SA Agent) |
| **Phê Duyệt** | Đang chờ Rà Soát Quản Trị Kỹ Thuật |

## 1. Tổng Quan Hệ Thống

### 1.1 Giới Thiệu Attendance Service
Attendance Service là một microservice trong hệ thống Membership Hub, chuyên trách xử lý các nghiệp vụ điểm danh sinh viên thông qua mã QR. Dịch vụ này đóng vai trò quan trọng trong việc đảm bảo tính chính xác, an toàn và khả năng mở rộng của quy trình điểm danh, hỗ trợ các tính năng như xác thực QR, kiểm tra đăng ký khóa học, xử lý trùng lặp, và tích hợp với hệ thống notification thông qua Kafka.

### 1.2 Kiến Trúc Tổng Thể
- **Công nghệ chính**: Quarkus 3.15 LTS, Hibernate ORM Panache, SmallRye Reactive Messaging Kafka
- **Cơ sở dữ liệu**: PostgreSQL với bảng `attendance` phân vùng theo `attendance_date`
- **Giao tiếp**: REST API + Kafka events
- **Xác thực**: JWT Bearer token qua API Gateway
- **Cache**: Redis cho session và counters
- **Observability**: SLF4J + OpenTelemetry

## 2. C4 Container Architecture Components

### 2.1 Container: Attendance Service (Backend Microservice)