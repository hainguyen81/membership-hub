# Day 4: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/api/course-openapi.yaml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Documentation Destination Path: `./sources/docs/api/course-openapi.yaml`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Bổ sung vào tệp ./sources/docs/api/course-openapi.yaml các endpoint GET /api/v1/students/courses/available và POST /api/v1/enrollments với mô tả chi tiết response schema, mã lỗi 403 (insufficient privileges), 404 (course not found), 409 (course full, already enrolled). Tham chiếu [REQ-010], [REQ-011]. Bổ sung security scheme BearerAuth cho các endpoint bảo vệ. Bao gồm ví dụ request/response cho từng trường hợp sử dụng.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[REQ-010]', '[REQ-011]', '[DOC-001]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
# Membership Hub - Course & Enrollment Service API Specification

- **Target Destination Path:** `./sources/docs/api/course-openapi.yaml`
- **Root Java Package Prefix:** `org.nlh4j.membershiphub`
- **Module Architecture Subsystem:** `course-service` (`org.nlh4j.membershiphub.courseservice`)
- **Integration Target Subsystem:** `attendance-service`, `user-service`, `notification-service`
- **Target Traceability Tag IDs:** `[REQ-010]`, `[REQ-011]`, `[DOC-001]`, `[ARC-000]`, `[ARC-007]`, `[ARC-008]`, `[DAT-003]`, `[DAT-004]`, `[EXC-004]`, `[NFR-001]`, `[NFR-003]`

---

## 1. Traceability Matrix Reference

| Module / Component Handle | Physical Implementation Path | Inherited Tag IDs | Enterprise Operational Purpose |
| :--- | :--- | :--- | :--- |
| **Course Browse Controller** | `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/StudentCourseBrowseController.java` | `[REQ-010]`, `[DOC-001]`, `[ARC-007]` | REST endpoint cho phép học viên duyệt danh sách khoá học khả dụng (loại trừ các khoá học đã đăng ký trước đó). |
| **Enrollment Controller** | `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/EnrollmentController.java` | `[REQ-011]`, `[DOC-001]`, `[ARC-007]`, `[ARC-008]` | REST endpoint xử lý đăng ký khoá học, tự động đồng bộ tài khoản học viên, quản lý hạn ngạch lớp học và phát sinh sự kiện Kafka. |
| **Course Service & Repository** | `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseBrowseService.java` | `[REQ-010]`, `[DAT-003]`, `[NFR-001]` | Tầng xử lý nghiệp vụ lọc khoá học bằng truy vấn SQL `NOT EXISTS` kết hợp subquery trên bảng `enrollments`. |
| **Enrollment Service & Outbox** | `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/EnrollmentService.java` | `[REQ-011]`, `[DAT-004]`, `[EXC-004]` | Thực thi transaction phân tán ghi dữ liệu ghi danh và gửi sự kiện `enrollment-created` vào Kafka topic `enrollment-events`. |
| **Security & JWT Verification** | `./sources/backend/course-service/src/main/resources/application.properties` | `[ARC-006]`, `[NFR-003]` | Xác thực Bearer JWT token qua MicroProfile JWT, thực thi RBAC phân quyền `Student` và `Admin`. |
| **OpenAPI 3.1 YAML Contract** | `./sources/docs/api/course-openapi.yaml` | `[DOC-001]`, `[REQ-010]`, `[REQ-011]` | Hợp đồng giao tiếp giao diện lập trình ứng dụng REST chuẩn hóa cho web-app Next.js và mobile client Capacitor. |

---

## 2. API Endpoint Specifications

| HTTP Method | Full Endpoint | Targeted Tag IDs | Request Headers | Path / Query Parameters | JSON Request Payload Schema | JSON Response Schema (Success & Error Codes) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/students/courses/available` | `[REQ-010]`, `[DOC-001]`, `[NFR-001]`, `[NFR-003]` | `Authorization: Bearer <JWT>`<br>`Accept: application/json` | **Query Parameters:**<br>- `page` (int, default: 0)<br>- `size` (int, default: 20)<br>- `search` (string, optional)<br>- `centerId` (UUID, optional) | *None (HTTP GET request)* | **200 OK:** Paginated array of `AvailableCourseResponse`<br>**401 Unauthorized:** `ErrorResponse` (`UNAUTHORIZED_TOKEN`)<br>**403 Forbidden:** `ErrorResponse` (`INSUFFICIENT_PRIVILEGES`)<br>**500 Internal Error:** `ErrorResponse` (`INTERNAL_SERVER_ERROR`) |
| `POST` | `/api/v1/enrollments` | `[REQ-011]`, `[DOC-001]`, `[ARC-007]`, `[ARC-008]`, `[EXC-004]` | `Authorization: Bearer <JWT>`<br>`Content-Type: application/json`<br>`Idempotency-Key: <UUID>` | *None* | `EnrollmentRequest`<br>`{ "courseId": "UUID" }` | **201 Created:** `EnrollmentResponse`<br>**400 Bad Request:** `ErrorResponse` (`INVALID_PAYLOAD`)<br>**401 Unauthorized:** `ErrorResponse` (`UNAUTHORIZED_TOKEN`)<br>**403 Forbidden:** `ErrorResponse` (`INSUFFICIENT_PRIVILEGES`)<br>**404 Not Found:** `ErrorResponse` (`COURSE_NOT_FOUND`)<br>**409 Conflict:** `ErrorResponse` (`COURSE_FULL` hoặc `ALREADY_ENROLLED`)<br>**500 Internal Error:** `ErrorResponse` (`INTERNAL_SERVER_ERROR`) |

---

## 3. Data Transfer Objects (DTO) & Payload Schema Details

### 3.1. DTO `AvailableCourseResponse` (`[REQ-010]`)
Mô hình dữ liệu đại diện cho khoá học mà học viên hiện tại chưa ghi danh và còn chỗ trống:
- `courseId` (`UUID`, bắt buộc): Định danh duy nhất của khoá học.
- `title` (`string`, bắt buộc, max: 150): Tên khoá học.
- `description` (`string`, tuỳ chọn): Nội dung tóm tắt chi tiết chương trình học.
- `startDate` (`string`, định dạng `date`, bắt buộc): Ngày khai giảng khóa học.
- `endDate` (`string`, định dạng `date`, bắt buộc): Ngày bế giảng khóa học.
- `teacherId` (`UUID`, bắt buộc): Mã định danh giáo viên đứng lớp.
- `teacherName` (`string`, bắt buộc): Họ tên giáo viên phụ trách.
- `maxStudents` (`integer`, bắt buộc, tối thiểu: 1): Sức chứa tối đa của khóa học.
- `enrolledStudents` (`integer`, bắt buộc): Số lượng học viên hiện tại đã ghi danh thành công.
- `remainingSlots` (`integer`, bắt buộc): Số vị trí trống còn khả dụng (`maxStudents - enrolledStudents`).
- `centerId` (`UUID`, bắt buộc): Mã trung tâm tổ chức lớp học.
- `centerName` (`string`, bắt buộc): Tên chi nhánh trung tâm tổ chức.

### 3.2. DTO `EnrollmentRequest` (`[REQ-011]`)
Mô hình tiếp nhận yêu cầu ghi danh khoá học từ ứng dụng phía máy khách:
- `courseId` (`UUID`, bắt buộc): Mã UUID của khóa học mục tiêu mà sinh viên muốn đăng ký tham gia.

### 3.3. DTO `EnrollmentResponse` (`[REQ-011]`)
Mô hình phản hồi dữ liệu sau khi ghi danh thành công:
- `enrollmentId` (`UUID`, bắt buộc): Mã định danh bản ghi ghi danh sinh viên.
- `studentId` (`UUID`, bắt buộc): Mã định danh sinh viên được cấp phát hoặc liên kết.
- `courseId` (`UUID`, bắt buộc): Mã định danh khóa học đã ghi danh.
- `enrollmentDate` (`string`, định dạng `date-time`, bắt buộc): Thời điểm hệ thống ghi nhận bản ghi.
- `autoCreatedUser` (`boolean`, bắt buộc): Cờ xác định tài khoản người dùng có được hệ thống tự động sinh trong luồng này hay không.
- `status` (`string`, enum: `["ACTIVE", "PENDING_PAYMENT", "CANCELLED"]`, bắt buộc): Trạng thái ghi danh.

### 3.4. DTO `ErrorResponse` (`[EXC-004]`)
Mô hình phản hồi lỗi đồng nhất cho toàn bộ hệ sinh thái dịch vụ:
- `timestamp` (`string`, định dạng `date-time`, bắt buộc): Thời điểm phát sinh lỗi.
- `status` (`integer`, bắt buộc): Mã trạng thái HTTP (HTTP Status Code).
- `errorCode` (`string`, bắt buộc): Mã định danh lỗi nghiệp vụ (ví dụ: `COURSE_FULL`, `ALREADY_ENROLLED`).
- `message` (`string`, bắt buộc): Thông điệp mô tả lỗi chi tiết phục vụ hiển thị cho người dùng hoặc debug.
- `path` (`string`, bắt buộc): Đường dẫn URI endpoint nơi ngoại lệ phát sinh.
- `traceId` (`string`, bắt buộc): Mã truy vết lỗi phân tán OpenTelemetry / Cloud Logging.
- `details` (`array of ErrorDetail`, tuỳ chọn): Danh sách chi tiết các trường vi phạm quy chuẩn validation.

---

## 4. Operational Failure Scenarios & Error Mapping Matrix

| HTTP Status | Business Error Code | Contextual Root Cause & Failure Trigger | Client Action & Recovery Lifecycle |
| :--- | :--- | :--- | :--- |
| **`400 BAD REQUEST`** | `INVALID_PAYLOAD` | Trường `courseId` bị rỗng, null, hoặc sai định dạng chuẩn UUID RFC 4122. | Client bắt buộc sửa định dạng chuỗi UUID trước khi thử lại yêu cầu. |
| **`401 UNAUTHORIZED`** | `UNAUTHORIZED_TOKEN` | Bearer token bị thiếu, hết hạn (quá 15 phút theo `[NFR-003]`), hoặc chữ ký số không khớp với RSA public key của auth server. | Client kích hoạt luồng làm mới phiên đăng nhập qua endpoint `/api/v1/auth/refresh`. |
| **`403 FORBIDDEN`** | `INSUFFICIENT_PRIVILEGES` | Token xác thực hợp lệ nhưng không chứa vai trò `STUDENT` trong claim `groups` (ví dụ: `TEACHER` hoặc tài khoản bị khóa). | Hệ thống chặn thao tác và thông báo quyền hạn không đủ. |
| **`404 NOT FOUND`** | `COURSE_NOT_FOUND` | `courseId` cung cấp không tồn tại trong cơ sở dữ liệu hoặc đã bị xóa mềm (`is_deleted = true`). | Client thông báo khoá học không còn tồn tại trên giao diện. |
| **`409 CONFLICT`** | `COURSE_FULL` | Số lượng sinh viên đã ghi danh bằng đúng giá trị `max_students` của khoá học (`[EXC-004]`). | Client khoá nút đăng ký trên giao diện, hiển thị thông báo lớp học đã kín chỗ. |
| **`409 CONFLICT`** | `ALREADY_ENROLLED` | Học viên đã tồn tại bản ghi ghi danh cho khoá học này (`uq_enrollments_student_course` vi phạm). | Client thông báo học viên đã tham gia khoá học này trước đó. |

---

## 5. Event-Driven Architecture (EDA) & Kafka Outbox Pipeline

Khi một yêu cầu đăng ký khoá học (`POST /api/v1/enrollments`) được xử lý thành công tại `course-service`, hệ thống thực thi mẫu thiết kế Transactional Outbox Pattern nhằm đảm bảo tính nhất quán cuối cùng (Eventual Consistency) theo `[ARC-008]`.

### 5.1. Kafka Topic Configuration
- **Topic Name:** `enrollment-events`
- **Partitions:** `12`
- **Replication Factor:** `3`
- **Retention Period:** `7 days` (`604800000 ms`)
- **Cleanup Policy:** `delete`
- **Producer Config:** `acks=all`, `enable.idempotence=true`

### 5.2. Event Payload Contract (`enrollment-created`)
```json
{
  "eventId": "b3e21820-4a87-4ee4-9b29-9e1201dc9b28",
  "eventType": "enrollment-created",
  "timestamp": "2026-08-30T08:15:30Z",
  "aggregateId": "48f1f72a-6a56-42d4-a149-14a9ec04da9a",
  "payload": {
    "enrollmentId": "48f1f72a-6a56-42d4-a149-14a9ec04da9a",
    "studentId": "09c919d3-524a-4ff4-b816-096d2994e4d5",
    "courseId": "8b0fd294-b25c-43a9-a9a7-0e69ebdb8e50",
    "courseTitle": "Lập trình Java Microservices Chuyên sâu",
    "centerId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "enrollmentDate": "2026-08-30T08:15:29.842Z",
    "autoCreatedUser": false
  }
}
```

### 5.3. Downstream Consumers
1. **`attendance-service`:** Khởi tạo cấu hình điểm danh và phân quyền điểm danh QR cho học viên trong khóa học tương ứng (`[REQ-012]`, `[REQ-013]`).
2. **`user-service` (Student Card Engine):** Kích hoạt phát hành hoặc cập nhật hạn sử dụng của thẻ thành viên số (`[REQ-014]`).
3. **`notification-service`:** Tiêu thụ sự kiện để đẩy thông báo xác nhận qua Firebase Cloud Messaging (FCM), Apple Push Notification service (APNs) và Webhook Zalo Official Account (`[REQ-016]`, `[REQ-021]`).

---

## 6. OpenAPI 3.1.0 Specification File (`course-openapi.yaml`)

```yaml
openapi: 3.1.0
info:
  title: Membership Hub - Course & Enrollment Service API
  version: 1.0.0
  description: >
    Tài liệu đặc tả hợp đồng giao diện lập trình ứng dụng RESTful cho Course Service
    thuộc hệ thống Membership Hub. Cung cấp chức năng duyệt khoá học khả dụng và
    đăng ký ghi danh khoá học cho sinh viên, hỗ trợ xác thực JWT Bearer và kiến trúc sự kiện Kafka.
    Truy vết yêu cầu: [REQ-010], [REQ-011], [DOC-001], [ARC-007], [ARC-008].
  contact:
    name: Technical Architecture Governance Team
    email: architecture@membershiphub.org
servers:
  - url: https://api.membershiphub.org/api/v1
    description: Production API Gateway
  - url: http://localhost:8080/api/v1
    description: Local Quarkus Development Runtime

security:
  - BearerAuth: []

paths:
  /students/courses/available:
    get:
      tags:
        - Student Course Browsing
      summary: Duyệt danh sách khoá học khả dụng cho sinh viên [REQ-010]
      description: >
        Trả về danh sách các khoá học có lịch học trong tương lai hoặc đang hoạt động
        mà học viên hiện tại chưa ghi danh, đồng thời sĩ số hiện tại chưa đạt giới hạn
        tối đa (maxStudents). Yêu cầu quyền vai trò STUDENT.
      operationId: getAvailableCoursesForStudent
      parameters:
        - name: page
          in: query
          description: Số chỉ mục trang phân trang (bắt đầu từ 0).
          required: false
          schema:
            type: integer
            default: 0
            minimum: 0
        - name: size
          in: query
          description: Số lượng bản ghi tối đa trên một trang.
          required: false
          schema:
            type: integer
            default: 20
            minimum: 1
            maximum: 100
        - name: search
          in: query
          description: Từ khoá tìm kiếm theo tiêu đề hoặc mô tả khoá học.
          required: false
          schema:
            type: string
        - name: centerId
          in: query
          description: Bộ lọc theo mã trung tâm tổ chức khoá học.
          required: false
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Truy vấn danh sách khoá học khả dụng thành công.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AvailableCoursePageResponse'
              examples:
                successList:
                  summary: Danh sách khoá học khả dụng hợp lệ
                  value:
                    content:
                      - courseId: "8b0fd294-b25c-43a9-a9a7-0e69ebdb8e50"
                        title: "Lập trình Java Microservices Chuyên sâu"
                        description: "Khoá đào tạo Quarkus 3.15, Kafka và kiến trúc phản ứng doanh nghiệp."
                        startDate: "2026-09-01"
                        endDate: "2026-11-30"
                        teacherId: "12f0a8d9-3174-4b5c-8977-bc65da112345"
                        teacherName: "Nguyễn Văn Thầy"
                        maxStudents: 30
                        enrolledStudents: 18
                        remainingSlots: 12
                        centerId: "3fa85f64-5717-4562-b3fc-2c963f66afa6"
                        centerName: "Trung Tâm Membership Hub Quận 1"
                    page: 0
                    size: 20
                    totalElements: 1
                    totalPages: 1
        '401':
          $ref: '#/components/responses/401Unauthorized'
        '403':
          $ref: '#/components/responses/403Forbidden'
        '500':
          $ref: '#/components/responses/500InternalServerError'

  /enrollments:
    post:
      tags:
        - Course Enrollment
      summary: Đăng ký ghi danh khoá học cho sinh viên [REQ-011]
      description: >
        Tiếp nhận yêu cầu đăng ký tham gia khoá học của sinh viên. Hệ thống kiểm tra:
        1. Khoá học tồn tại và đang mở.
        2. Sinh viên chưa từng ghi danh khoá học này trước đó.
        3. Khoá học còn vị trí trống (enrolled < maxStudents).
        Nếu hợp lệ, bản ghi ghi danh được lưu trữ, và sự kiện Kafka enrollment-created
        được đẩy tới topic enrollment-events để kích hoạt cấp thẻ và thông báo.
      operationId: createEnrollment
      parameters:
        - name: Idempotency-Key
          in: header
          description: Khoá chống trùng lặp yêu cầu gửi lại do mạng chập chờn.
          required: false
          schema:
            type: string
            format: uuid
      requestBody:
        description: Thông tin mã khoá học cần đăng ký.
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/EnrollmentRequest'
            examples:
              standardRequest:
                summary: Yêu cầu ghi danh khoá học tiêu chuẩn
                value:
                  courseId: "8b0fd294-b25c-43a9-a9a7-0e69ebdb8e50"
      responses:
        '201':
          description: Đăng ký khoá học thành công. Bản ghi đã được khởi tạo.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/EnrollmentResponse'
              examples:
                enrollmentCreated:
                  summary: Ghi danh hoàn tất
                  value:
                    enrollmentId: "48f1f72a-6a56-42d4-a149-14a9ec04da9a"
                    studentId: "09c919d3-524a-4ff4-b816-096d2994e4d5"
                    courseId: "8b0fd294-b25c-43a9-a9a7-0e69ebdb8e50"
                    enrollmentDate: "2026-08-30T08:15:29.842Z"
                    autoCreatedUser: false
                    status: "ACTIVE"
        '400':
          $ref: '#/components/responses/400BadRequest'
        '401':
          $ref: '#/components/responses/401Unauthorized'
        '403':
          $ref: '#/components/responses/403Forbidden'
        '404':
          description: Không tìm thấy khoá học yêu cầu.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              examples:
                courseNotFound:
                  summary: Khoá học không tồn tại
                  value:
                    timestamp: "2026-08-30T08:15:30.120Z"
                    status: 404
                    errorCode: "COURSE_NOT_FOUND"
                    message: "Không tìm thấy khoá học với định danh được cung cấp."
                    path: "/api/v1/enrollments"
                    traceId: "trace-9912048-bbf3-4e12"
        '409':
          description: Xung đột trạng thái ghi danh (Lớp đã đầy hoặc đã đăng ký trước đó).
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              examples:
                courseFullError:
                  summary: Khoá học đã đạt sĩ số tối đa [EXC-004]
                  value:
                    timestamp: "2026-08-30T08:15:30.120Z"
                    status: 409
                    errorCode: "COURSE_FULL"
                    message: "Khoá học đã đạt số lượng sinh viên tối đa."
                    path: "/api/v1/enrollments"
                    traceId: "trace-1029384-aaf2-4d99"
                alreadyEnrolledError:
                  summary: Sinh viên đã ghi danh khoá học này
                  value:
                    timestamp: "2026-08-30T08:15:30.120Z"
                    status: 409
                    errorCode: "ALREADY_ENROLLED"
                    message: "Sinh viên đã đăng ký khoá học này trước đó."
                    path: "/api/v1/enrollments"
                    traceId: "trace-7744123-cfe1-4822"
        '500':
          $ref: '#/components/responses/500InternalServerError'

components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: >
        Truyền JWT Access Token được cấp từ /api/v1/auth/login hoặc /api/v1/auth/social.
        Định dạng header: Authorization: Bearer <token>.

  schemas:
    EnrollmentRequest:
      type: object
      required:
        - courseId
      properties:
        courseId:
          type: string
          format: uuid
          description: Mã định danh UUID của khoá học đăng ký.
          example: "8b0fd294-b25c-43a9-a9a7-0e69ebdb8e50"

    EnrollmentResponse:
      type: object
      required:
        - enrollmentId
        - studentId
        - courseId
        - enrollmentDate
        - autoCreatedUser
        - status
      properties:
        enrollmentId:
          type: string
          format: uuid
          description: Mã định danh bản ghi ghi danh.
        studentId:
          type: string
          format: uuid
          description: Mã định danh học viên.
        courseId:
          type: string
          format: uuid
          description: Mã định danh khoá học.
        enrollmentDate:
          type: string
          format: date-time
          description: Thời điểm ghi danh thành công.
        autoCreatedUser:
          type: boolean
          description: Xác định tài khoản sinh viên có được tự động tạo trong quá trình đăng ký hay không.
        status:
          type: string
          enum: [ACTIVE, PENDING_PAYMENT, CANCELLED]
          description: Trạng thái hiện tại của bản ghi ghi danh.

    AvailableCourseResponse:
      type: object
      required:
        - courseId
        - title
        - startDate
        - endDate
        - teacherId
        - teacherName
        - maxStudents
        - enrolledStudents
        - remainingSlots
        - centerId
        - centerName
      properties:
        courseId:
          type: string
          format: uuid
        title:
          type: string
          maxLength: 150
        description:
          type: string
          nullable: true
        startDate:
          type: string
          format: date
        endDate:
          type: string
          format: date
        teacherId:
          type: string
          format: uuid
        teacherName:
          type: string
        maxStudents:
          type: integer
        enrolledStudents:
          type: integer
        remainingSlots:
          type: integer
        centerId:
          type: string
          format: uuid
        centerName:
          type: string

    AvailableCoursePageResponse:
      type: object
      required:
        - content
        - page
        - size
        - totalElements
        - totalPages
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/AvailableCourseResponse'
        page:
          type: integer
        size:
          type: integer
        totalElements:
          type: integer
        totalPages:
          type: integer

    ErrorDetail:
      type: object
      required:
        - field
        - error
      properties:
        field:
          type: string
          description: Tên trường gặp lỗi xác thực dữ liệu.
        error:
          type: string
          description: Nội dung chi tiết của vi phạm xác thực.

    ErrorResponse:
      type: object
      required:
        - timestamp
        - status
        - errorCode
        - message
        - path
        - traceId
      properties:
        timestamp:
          type: string
          format: date-time
          description: Thời điểm hệ thống ghi nhận ngoại lệ.
        status:
          type: integer
          description: Mã trạng thái giao thức HTTP.
        errorCode:
          type: string
          description: Mã lỗi kỹ thuật / nghiệp vụ định danh chuẩn.
        message:
          type: string
          description: Diễn giải chi tiết lý do thất bại.
        path:
          type: string
          description: Điểm cuối API kích hoạt ngoại lệ.
        traceId:
          type: string
          description: Mã định danh phân tán phục vụ truy vết log qua OpenTelemetry / Cloud Logging.
        details:
          type: array
          items:
            $ref: '#/components/schemas/ErrorDetail'
          nullable: true
          description: Mảng chi tiết các trường dữ liệu vi phạm Bean Validation.

  responses:
    400BadRequest:
      description: Dữ liệu tải trọng yêu cầu không hợp lệ hoặc thiếu trường bắt buộc.
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
          examples:
            validationError:
              summary: Vi phạm Bean Validation Jakarta
              value:
                timestamp: "2026-08-30T08:15:30.120Z"
                status: 400
                errorCode: "INVALID_PAYLOAD"
                message: "Dữ liệu đầu vào không vượt qua kiểm tra tính hợp lệ."
                path: "/api/v1/enrollments"
                traceId: "trace-4488123-eeb1-4112"
                details:
                  - field: "courseId"
                    error: "Mã khoá học không được để trống và phải đúng định dạng UUID."

    401Unauthorized:
      description: Người dùng chưa cung cấp token hợp lệ hoặc token đã hết hạn.
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
          examples:
            unauthorizedError:
              summary: Thiếu hoặc sai Bearer Token
              value:
                timestamp: "2026-08-30T08:15:30.120Z"
                status: 401
                errorCode: "UNAUTHORIZED_TOKEN"
                message: "Mã xác thực không hợp lệ hoặc đã hết thời gian hiệu lực."
                path: "/api/v1/students/courses/available"
                traceId: "trace-1199238-dda4-4299"

    403Forbidden:
      description: Người dùng không có quyền truy cập endpoint này (yêu cầu vai trò STUDENT).
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
          examples:
            forbiddenError:
              summary: Quyền hạn không đáp ứng
              value:
                timestamp: "2026-08-30T08:15:30.120Z"
                status: 403
                errorCode: "INSUFFICIENT_PRIVILEGES"
                message: "Tài khoản không được cấp phép thực thi hành động này."
                path: "/api/v1/students/courses/available"
                traceId: "trace-3388192-cca8-4771"

    500InternalServerError:
      description: Lỗi máy chủ nội bộ hoặc gián đoạn kết nối cơ sở dữ liệu / Kafka Broker.
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
          examples:
            internalError:
              summary: Sự cố hệ thống bất ngờ
              value:
                timestamp: "2026-08-30T08:15:30.120Z"
                status: 500
                errorCode: "INTERNAL_SERVER_ERROR"
                message: "Đã xảy ra sự cố nội bộ trong quá trình xử lý yêu cầu. Vui lòng liên hệ quản trị viên."
                path: "/api/v1/enrollments"
                traceId: "trace-9900112-ffa9-4553"
```
```

