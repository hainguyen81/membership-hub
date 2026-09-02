package org.nlh4j.membershiphub.courseservice.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * CourseResponse DTO representing a course summary returned by the Course API.
 * Traceability: [REQ-007], [REQ-008]
 */
public class CourseResponse {
    /**
     * Unique identifier of the course.
     * Traceability: [REQ-007], [REQ-008]
     */
    private UUID courseId;

    /**
     * Title of the course (max 150 characters).
     * Traceability: [REQ-007], [REQ-008]
     */
    private String title;

    /**
     * Start date of the course.
     * Traceability: [REQ-007], [REQ-008]
     */
    private LocalDate startDate;

    /**
     * End date of the course.
     * Traceability: [REQ-007], [REQ-008]
     */
    private LocalDate endDate;

    /**
     * Name of the teacher assigned to the course.
     * Traceability: [REQ-007], [REQ-008]
     */
    private String teacherName;

    // Default constructor for frameworks
    public CourseResponse() {
    }

    /**
     * Full constructor for creating immutable response.
     * Traceability: [REQ-007], [REQ-008]
     */
    public CourseResponse(UUID courseId, String title, LocalDate startDate, LocalDate endDate, String teacherName) {
        this.courseId = courseId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.teacherName = teacherName;
    }

    // Getters and setters
    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

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

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}