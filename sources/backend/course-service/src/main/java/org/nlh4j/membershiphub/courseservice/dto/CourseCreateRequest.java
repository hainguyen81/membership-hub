/**
 * 📄 CourseCreateRequest.java
 * 📂 Đường dẫn: ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/CourseCreateRequest.java
 * 🏷️ Thẻ Truy Vết: [REQ-007], [REQ-008]
 * 📝 Mô Tả: DTO yêu cầu tạo mới khóa học, chứa các trường bắt buộc cho việc tạo khóa học mới.
 * 🔒 Bảo Mật: Tất cả các trường đều được xác thực nghiêm ngặt theo Jakarta Bean Validation để ngăn chặn SQL Injection và đảm bảo tính toàn vẹn dữ liệu.
 * 📊 Quy Tắc Kiến Trúc: Tuân thủ quy tắc đặt tên gói org.nlh4j.membershiphub.courseservice.dto, tuân thủ SOLID và OWASP A03.
 */
package org.nlh4j.membershiphub.courseservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 📦 Lớp DTO này được sử dụng trong {@link org.nlh4j.membershiphub.courseservice.controller.CourseController}
 * để nhận payload yêu cầu tạo khóa học từ client.
 *
 * @author Enterprise Backend Team
 * @version 1.0
 * @since 2024-08-29
 */
public class CourseCreateRequest {

    /**
     * 🏷️ [REQ-007] 📝 Tiêu đề khóa học.
     * Bắt buộc, không được null, độ dài tối đa 150 ký tự.
     * Được xác thực bằng @Size để ngăn chặn các cuộc tấn công tràn bộ đệm chuỗi.
     */
    @NotNull(message = "Tiêu đề khóa học không được để trống")
    @Size(max = 150, message = "Tiêu đề khóa học không được vượt quá 150 ký tự")
    private String title;

    /**
     * 🏷️ [REQ-008] 📅 Ngày bắt đầu khóa học.
     * Bắt buộc, không được null, tuân thủ định dạng ISO LocalDate.
     * Được xác thực bằng @NotNull để đảm bảo tính hợp lệ của ngày bắt đầu.
     */
    @NotNull(message = "Ngày bắt đầu khóa học không được để trống")
    private LocalDate startDate;

    /**
     * 🏷️ [REQ-008] 📅 Ngày kết thúc khóa học.
     * Bắt buộc, không được null, tuân thủ định dạng ISO LocalDate.
     * Được xác thực bằng @NotNull để đảm bảo tính hợp lệ của ngày kết thúc.
     */
    @NotNull(message = "Ngày kết thúc khóa học không được để trống")
    private LocalDate endDate;

    /**
     * 🏷️ [REQ-008] 👩‍🏫 ID của giáo viên phụ trách khóa học.
     * Bắt buộc, không được null, phải là UUID hợp lệ.
     * Được xác thực bằng @NotNull để đảm bảo giáo viên được chỉ định.
     */
    @NotNull(message = "ID giáo viên không được để trống")
    private UUID teacherId;

    /**
     * 🏷️ [REQ-008] 🏢 ID của trung tâm tổ chức khóa học.
     * Bắt buộc, không được null, phải là UUID hợp lệ.
     * Được xác thực bằng @NotNull để đảm bảo trung tâm được chỉ định.
     */
    @NotNull(message = "ID trung tâm không được để trống")
    private UUID centerId;

    // -------------------------------------------------------------------------
    // 🛠️ Constructors
    // -------------------------------------------------------------------------

    /**
     * 🏗️ Constructor mặc định (bắt buộc cho reflection và frameworks).
     */
    public CourseCreateRequest() {
    }

    /**
     * 🏗️ Constructor đầy đủ tham số để khởi tạo đối tượng nhanh chóng.
     *
     * @param title      Tiêu đề khóa học
     * @param startDate  Ngày bắt đầu khóa học
     * @param endDate    Ngày kết thúc khóa học
     * @param teacherId  ID của giáo viên
     * @param centerId   ID của trung tâm
     */
    public CourseCreateRequest(String title, LocalDate startDate, LocalDate endDate, UUID teacherId, UUID centerId) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.teacherId = teacherId;
        this.centerId = centerId;
    }

    // -------------------------------------------------------------------------
    // 📌 Getter & Setter Methods
    // -------------------------------------------------------------------------

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(UUID teacherId) {
        this.teacherId = teacherId;
    }

    public UUID getCenterId() {
        return centerId;
    }

    public void setCenterId(UUID centerId) {
        this.centerId = centerId;
    }

    // -------------------------------------------------------------------------
    // 🔄 toString, equals, hashCode (optional, for debugging)
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "CourseCreateRequest{" +
                "title='" + title + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", teacherId=" + teacherId +
                ", centerId=" + centerId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseCreateRequest that = (CourseCreateRequest) o;
        return title.equals(that.title) &&
                startDate.equals(that.startDate) &&
                endDate.equals(that.endDate) &&
                teacherId.equals(that.teacherId) &&
                centerId.equals(that.centerId);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + startDate.hashCode();
        result = 31 * result + endDate.hashCode();
        result = 31 * result + teacherId.hashCode();
        result = 31 * result + centerId.hashCode();
        return result;
    }
}