package org.nlh4j.saas.membership_hub.report;

// -------------------------- ENTERPRISE IMPORTS (COMPLIANCE LAYER) --------------------------
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.nlh4j.saas.membership_hub.attendance.service.AttendanceService;
import org.nlh4j.saas.membership_hub.report.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;
// -------------------------- END OF ENTERPRISE IMPORTS --------------------------

/**
 * REST Controller for generating attendance reports in CSV format.
 * Handles filtered daily attendance reports by center and date range,
 * with pre-processing of pending attendance records from system recovery scenarios.
 * 
 * @traceability [REQ-024], [EXC-005]
 * @author Enterprise Core Engineering Team
 * @version 1.0.0
 * @since 2024-06-01
 */
public class AttendanceReportController {

    // ====================== TOP-OF-CLASS ENTERPRISE CONSTANTS (NO MAGIC NUMBERS/STRINGS) ======================
    // [REQ-024] API endpoint configuration
    public static final String API_REPORT_ATTENDANCE_CSV = "/api/v1/reports/attendance/csv";
    public static final String CSV_CONTENT_TYPE = "text/csv; charset=UTF-8";
    public static final String CSV_FILE_PREFIX = "attendance-report-";
    public static final String CSV_FILE_EXTENSION = ".csv";
    // [REQ-024] CSV column headers (fixed structure per requirement)
    public static final String CSV_HEADER_STUDENT_NAME = "StudentName";
    public static final String CSV_HEADER_COURSE_NAME = "CourseName";
    public static final String CSV_HEADER_ATTENDANCE_DATE = "AttendanceDate";
    public static final String CSV_HEADER_STATUS = "Status";
    // [EXC-005] System recovery and report configuration
    public static final int MAX_REPORT_DATE_RANGE_DAYS = 365; // Prevent excessive load from overly large date ranges
    public static final String STATUS_PRESENT = "Có mặt";
    public static final String STATUS_ABSENT = "Vắng mặt";
    // [NFR-006] Standardized audit log message templates
    public static final String LOG_MSG_REQUEST_RECEIVED = "Received attendance CSV report request | CenterId: {} | StartDate: {} | EndDate: {}";
    public static final String LOG_MSG_PENDING_PROCESSED = "Processed {} pending attendance records in FIFO order per [EXC-005] recovery policy";
    public static final String LOG_MSG_REPORT_SUCCESS = "Successfully generated attendance CSV report | RecordCount: {} | CenterId: {}";
    public static final String LOG_MSG_ERROR_PREFIX = "[CRITICAL FAIL] [REQ-024] [EXC-005] Attendance report generation failed | Subsystem: AttendanceReportController | RawError: {}";
    // ====================== END OF TOP-OF-CLASS CONSTANTS ======================

    // Enterprise standard SLF4J logger initialization [NFR-006]
    private static final Logger logger = LoggerFactory.getLogger(AttendanceReportController.class);

    // Dependency injection for business logic services (SOLID Single Responsibility Principle)
    @Inject
    ReportService reportService; // Handles report data aggregation using read replica per NFR-004

    @Inject
    AttendanceService attendanceService; // Handles pending attendance processing per [EXC-005]

    /**
     * Generates and streams a CSV attendance report for a specified center and date range.
     * Processes all pending attendance records (from system downtime) in FIFO order before report generation
     * to ensure no data loss per system recovery policy [EXC-005].
     * Access restricted to authorized roles per RBAC matrix [ARC-001, ARC-002].
     * 
     * @param centerId UUID of the training center to generate report for (required, valid UUID format)
     * @param startDate Start date of report range (YYYY-MM-DD, required, cannot be future date)
     * @param endDate End date of report range (YYYY-MM-DD, required, cannot be future date, must be >= startDate)
     * @return Streaming CSV response with download headers
     * @traceability [REQ-024], [EXC-005]
     */
    @GET
    @Path(API_REPORT_ATTENDANCE_CSV)
    @Produces(CSV_CONTENT_TYPE)
    @RolesAllowed({"SYSTEM_ADMIN", "CENTER_ADMIN", "MANAGER"}) // [ARC-001, ARC-002] RBAC access control enforcement
    public StreamingOutput getAttendanceReportCsv(
            @QueryParam("centerId") @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "Invalid UUID format for centerId") String centerId,
            @QueryParam("startDate") @PastOrPresent(message = "Start date cannot be in the future") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @QueryParam("endDate") @PastOrPresent(message = "End date cannot be in the future") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // [NFR-006] Audit log: Entry point of report generation workflow
        logger.info(LOG_MSG_REQUEST_RECEIVED, centerId, startDate, endDate);

        try {
            // -------------------------- INPUT VALIDATION & BUSINESS RULE ENFORCEMENT --------------------------
            // [REQ-024] Validate mandatory input parameters
            if (centerId == null || centerId.isBlank() || startDate == null || endDate == null) {
                throw new IllegalArgumentException("Missing required parameters: centerId, startDate, endDate");
            }

            // Parse and validate center UUID
            UUID centerUuid = UUID.fromString(centerId);

            // [REQ-024] Enforce valid date range constraints
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
            long daysBetween = startDate.until(endDate).getDays();
            if (daysBetween > MAX_REPORT_DATE_RANGE_DAYS) {
                throw new IllegalArgumentException("Date range exceeds maximum allowed " + MAX_REPORT_DATE_RANGE_DAYS + " days");
            }

            // -------------------------- [EXC-005] PENDING ATTENDANCE PROCESSING (FIFO) --------------------------
            // Process all pending attendance records from system recovery scenarios first to ensure
            // all recovered data is included in the report, per FIFO processing requirement
            int processedPendingCount = attendanceService.processPendingAttendanceFIFO();
            logger.info(LOG_MSG_PENDING_PROCESSED, processedPendingCount);

            // -------------------------- STREAMING REPORT DATA FETCH --------------------------
            // Fetch report data as a stream from service layer (uses PostgreSQL read replica per NFR-004
            // to reduce load on primary database, stream processing avoids full dataset load into memory)
            try (Stream<ReportRow> reportDataStream = reportService.getAttendanceReportData(centerUuid, startDate, endDate)) {

                // -------------------------- CSV STREAMING RESPONSE CONSTRUCTION --------------------------
                // StreamingOutput writes directly to HTTP response stream, no intermediate memory storage
                return output -> {
                    // [NFR-007] Add UTF-8 BOM for Excel compatibility with Vietnamese language content
                    output.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

                    // Initialize OpenCSV writer with UTF-8 encoding
                    try (CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
                        // Write fixed CSV header row per requirement [REQ-024]
                        csvWriter.writeNext(new String[]{
                                CSV_HEADER_STUDENT_NAME,
                                CSV_HEADER_COURSE_NAME,
                                CSV_HEADER_ATTENDANCE_DATE,
                                CSV_HEADER_STATUS
                        });

                        // Track record count for audit logging
                        int[] recordCount = new int[1];

                        // Write data rows in streaming fashion (process one row at a time, no full load)
                        reportDataStream.forEach(row -> {
                            csvWriter.writeNext(new String[]{
                                    row.getStudentName(),
                                    row.getCourseName(),
                                    row.getAttendanceDate().toString(),
                                    row.getStatus()
                            });
                            recordCount[0]++;
                        });

                        // Flush all buffered CSV data to response stream
                        csvWriter.flush();

                        // [NFR-006] Audit log: Successful report generation with record count
                        logger.info(LOG_MSG_REPORT_SUCCESS, recordCount[0], centerUuid);
                    } catch (Exception e) {
                        // [NFR-006] Critical error logging with full context per enterprise exception logging mandate
                        logger.error(LOG_MSG_ERROR_PREFIX, e.getMessage(), e);
                        throw new WebApplicationException("Failed to generate CSV file", Response.Status.INTERNAL_SERVER_ERROR);
                    }
                };
            }

        } catch (IllegalArgumentException e) {
            // [NFR-006] Handle invalid input parameters with 400 Bad Request
            logger.error(LOG_MSG_ERROR_PREFIX, e.getMessage(), e);
            throw new WebApplicationException("Invalid request parameters: " + e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            // [NFR-006] Catch-all for unexpected system errors with 500 Internal Server Error
            logger.error(LOG_MSG_ERROR_PREFIX, e.getMessage(), e);
            throw new WebApplicationException("Internal server error during report generation", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // -------------------------- INNER DTO FOR TYPE-SAFE REPORT DATA --------------------------
    // [REQ-024] Data Transfer Object ensures clear, validated structure for report rows
    public static class ReportRow {
        private String studentName;
        private String courseName;
        private LocalDate attendanceDate;
        private String status;

        // Full parameterized constructor
        public ReportRow(String studentName, String courseName, LocalDate attendanceDate, String status) {
            this.studentName = studentName;
            this.courseName = courseName;
            this.attendanceDate = attendanceDate;
            this.status = status;
        }

        // Getters for field access
        public String getStudentName() { return studentName; }
        public String getCourseName() { return courseName; }
        public LocalDate getAttendanceDate() { return attendanceDate; }
        public String getStatus() { return status; }
    }
}