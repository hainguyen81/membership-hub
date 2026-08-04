# Giai đoạn 2: <!--PHASE_NAME_START-->phase_2<!--PHASE_NAME_END--> | Mô tả: Triển khai các tính năng quản lý người dùng, quản lý trung tâm, quản lý khóa học
## 📊 Document Control

| Mục | Chi tiết |
| :--- | :--- |
| **ID Blueprint** | ARCH-20260804052551 |
| **Tên dự án** | membership-hub |
| **Giai đoạn** | 2 |
| **Tên kỹ thuật giai đoạn** | <!--PHASE_NAME_START-->phase_2<!--PHASE_NAME_END--> |
| **Mô tả** | Triển khai các tính năng quản lý người dùng, quản lý trung tâm, quản lý khóa học |
| **Phiên bản** | 1.0 (Baseline) |
| **Ngày/Giờ** | 2026/08/04 05:25:51 |
| **Tác giả** | Enterprise System Architect (SA Agent) |
| **Phê duyệt** | Pending Technical Governance Review |

## 1. Phạm vi hoạt động và mục tiêu của giai đoạn
Giai đoạn 2 tập trung vào việc triển khai các tính năng quản lý người dùng, quản lý trung tâm, và quản lý khóa học. Các nhiệm vụ chính bao gồm:
- Triển khai các dịch vụ đăng ký người dùng, xác thực qua mạng xã hội, phân quyền người dùng.
- Triển khai các dịch vụ xem danh sách trung tâm, tạo/cập nhật/xóa trung tâm, phân quyền quản trị trung tâm.
- Triển khai các dịch vụ xem danh sách khóa học, tạo/cập nhật/xóa khóa học, phân công giáo viên vào khóa học.

## 2. Phạm vi kỹ thuật và ranh giới thư mục được phép
- `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/user`
- `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/center`
- `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/course`

## 3. Hướng dẫn chức năng chuyên dụng cho các tác vụ con
- **Coder:** Triển khai các dịch vụ đăng ký người dùng, xác thực qua mạng xã hội, phân quyền người dùng, xem danh sách trung tâm, tạo/cập nhật/xóa trung tâm, phân quyền quản trị trung tâm, xem danh sách khóa học, tạo/cập nhật/xóa khóa học, phân công giáo viên vào khóa học.
- **Tester:** Viết các test case cho các tính năng đăng ký người dùng, xác thực qua mạng xã hội, phân quyền người dùng, xem danh sách trung tâm, tạo/cập nhật/xóa trung tâm, phân quyền quản trị trung tâm, xem danh sách khóa học, tạo/cập nhật/xóa khóa học, phân công giáo viên vào khóa học.
- **Reviewer:** Review code cho các tính năng đăng ký người dùng, xác thực qua mạng xã hội, phân quyền người dùng, xem danh sách trung tâm, tạo/cập nhật/xóa trung tâm, phân quyền quản trị trung tâm, xem danh sách khóa học, tạo/cập nhật/xóa khóa học, phân công giáo viên vào khóa học.

## 4. Định nghĩa hoàn thành giai đoạn (DoD)
- Hoàn thành 100% các nhiệm vụ được chỉ định cho giai đoạn 2.
- Đảm bảo tuân thủ các tiêu chuẩn bảo mật OWASP.
- Đảm bảo hoàn thành các bài kiểm tra chức năng cho các yêu cầu được phân bổ.
- Đảm bảo 100% các Tag ID được ánh xạ.

## 5. NHẬT KÝ THỰC HIỆN KIẾN TRÚC THEO NGÀY

### NGÀY 4: Triển khai các tính năng quản lý người dùng

#### NHIỆM VỤ CON 4.1: Triển khai các dịch vụ đăng ký người dùng, xác thực qua mạng xã hội, phân quyền người dùng
##### Người phụ trách: Coder
##### Yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/user`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [REQ-002], [REQ-003]<!--END_TAGS-->

#### NHIỆM VỤ CON 4.2: Viết các test case cho các tính năng đăng ký người dùng, xác thực qua mạng xã hội, phân quyền người dùng
##### Người phụ trách: Tester
##### Yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/user;./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/user`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [REQ-002], [REQ-003]<!--END_TAGS-->

#### NHIỆM VỤ CON 4.3: Review code cho các tính năng đăng ký người dùng, xác thực qua mạng xã hội, phân quyền người dùng
##### Người phụ trách: Reviewer
##### Yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/user`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [REQ-002], [REQ-003]<!--END_TAGS-->

### NGÀY 5: Triển khai các tính năng quản lý trung tâm

#### NHIỆM VỤ CON 5.1: Triển khai các dịch vụ xem danh sách trung tâm, tạo/cập nhật/xóa trung tâm, phân quyền quản trị trung tâm
##### Người phụ trách: Coder
##### Yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/center`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-004], [REQ-005], [REQ-006]<!--END_TAGS-->

#### NHIỆM VỤ CON 5.2: Viết các test case cho các tính năng xem danh sách trung tâm, tạo/cập nhật/xóa trung tâm, phân quyền quản trị trung tâm
##### Người phụ trách: Tester
##### Yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/center;./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/center`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-004], [REQ-005], [REQ-006]<!--END_TAGS-->

#### NHIỆM VỤ CON 5.3: Review code cho các tính năng xem danh sách trung tâm, tạo/cập nhật/xóa trung tâm, phân quyền quản trị trung tâm
##### Người phụ trách: Reviewer
##### Yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/center`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-004], [REQ-005], [REQ-006]<!--END_TAGS-->

### NGÀY 6: Triển khai các tính năng quản lý khóa học

#### NHIỆM VỤ CON 6.1: Triển khai các dịch vụ xem danh sách khóa học, tạo/cập nhật/xóa khóa học, phân công giáo viên vào khóa học
##### Người phụ trách: Coder
##### Yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/course`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-007], [REQ-008], [REQ-009]<!--END_TAGS-->

#### NHIỆM VỤ CON 6.2: Viết các test case cho các tính năng xem danh sách khóa học, tạo/cập nhật/xóa khóa học, phân công giáo viên vào khóa học
##### Người phụ trách: Tester
##### Yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/course;./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/course`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-007], [REQ-008], [REQ-009]<!--END_TAGS-->

#### NHIỆM VỤ CON 6.3: Review code cho các tính năng xem danh sách khóa học, tạo/cập nhật/xóa khóa học, phân công giáo viên vào khóa học
##### Người phụ trách: Reviewer
##### Yêu cầu kỹ thuật:
* **Đường dẫn mục tiêu:** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/course`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-007], [REQ-008], [REQ-009]<!--END_TAGS-->