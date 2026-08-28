typescript
// Traceability Tags: [REQ-007], [REQ-010], [REQ-011], [REQ-012], [REQ-013]
// Project: membership-hub
// Component: CourseList (Frontend Web)
// Purpose: Displays list of courses with role-based actions (enrollment, teacher assignment, QR attendance scanning)
// Complies with: RBAC access control, idempotent attendance processing, offline queue support for attendance scans, OWASP security standards

import React, { useState, useEffect, useCallback } from 'react';
// [REQ-007] Import axios for secure API calls to backend services
import axios from 'axios';
// [REQ-012] Import reusable QR scanner component for attendance capture
import QRScanner from '../common/QRScanner';
// [REQ-010] Import notification hook for user feedback (success/error/info toasts)
import { useNotification } from '../../../../hooks/useNotification';
// [REQ-003] Import auth hook to retrieve current user role, enrollment status, and auth context
import { useAuth } from '../../../../hooks/useAuth';
// [REQ-022] Import i18n translation function for multi-language support (EN/VI/ES)
import { useTranslation } from 'next-i18next';
// Import typed API response models to ensure type safety
import { Course, Enrollment, AttendanceResponse, ApiError } from '../../../../types/api';

// ==============================================
// ENTERPRISE CONSTANTS (No hardcoded literals allowed per governance rules)
// ==============================================
// [REQ-007] Centralized API endpoint definitions to avoid hardcoded URLs
const API_ENDPOINTS = {
  GET_COURSES: '/api/v1/courses',
  ENROLL_COURSE: '/api/v1/enrollments',
  ATTENDANCE_SCAN: '/api/v1/attendance/scan',
  ASSIGN_TEACHER: '/api/v1/courses',
} as const;

// [REQ-012] Localized error message keys for attendance operations
const ATTENDANCE_ERROR_KEYS = {
  NETWORK_ERROR: 'attendance.errors.networkError',
  DUPLICATE_SCAN: 'attendance.errors.duplicateScan',
  INVALID_QR: 'attendance.errors.invalidQR',
  NOT_ENROLLED: 'attendance.errors.notEnrolled',
  SCAN_FAILED: 'attendance.errors.scanFailed',
} as const;

// [REQ-003] RBAC role enum definitions aligned with backend role matrix
const USER_ROLES = {
  STUDENT: 'Student',
  TEACHER: 'Teacher',
  CENTER_ADMIN: 'Center Admin',
  SYSTEM_ADMIN: 'System Admin',
  MANAGER: 'Manager',
} as const;

// Pagination configuration for course list
const DEFAULT_PAGE_SIZE = 20;

// ==============================================
// TYPE DEFINITIONS
// ==============================================
interface CourseListProps {
  // [REQ-007] Optional filter to show courses for a specific center
  centerId?: string;
}

interface AttendanceScanPayload {
  qrCode: string;
  timestamp: string;
}

// ==============================================
// COURSE LIST COMPONENT IMPLEMENTATION
// ==============================================
const CourseList: React.FC<CourseListProps> = ({ centerId }) => {
  // ==============================================
  // STATE MANAGEMENT (React hooks for component state)
  // ==============================================
  // [REQ-007] State to store fetched course list data
  const [courses, setCourses] = useState<Course[]>([]);
  // [REQ-007] Loading state for initial course fetch operation
  const [isLoading, setIsLoading] = useState<boolean>(true);
  // [REQ-007] Error state to display fetch failures to users
  const [fetchError, setFetchError] = useState<string | null>(null);
  // [REQ-010] Loading state for enrollment request to prevent duplicate submissions
  const [isEnrolling, setIsEnrolling] = useState<boolean>(false);
  // [REQ-012] State to control QR scanner modal visibility
  const [isQRScannerOpen, setIsQRScannerOpen] = useState<boolean>(false);
  // [REQ-012] State to store course selected for attendance scanning
  const [selectedCourseForAttendance, setSelectedCourseForAttendance] = useState<Course | null>(null);
  // [REQ-013] State to store last attendance scan result for user feedback
  const [lastScanResult, setLastScanResult] = useState<AttendanceResponse | null>(null);
  // [REQ-003] Current authenticated user data from auth context
  const { user } = useAuth();
  // [REQ-022] Translation function for localized UI text
  const { t } = useTranslation('common');
  // [REQ-010] Notification service for user feedback toasts
  const { showSuccess, showError, showInfo } = useNotification();

  // ==============================================
  // BUSINESS LOGIC: FETCH COURSE LIST
  // ==============================================
  /**
   * Fetches list of courses from backend API with role-based filtering
   * [REQ-007] Implements course listing with schedule, teacher, and capacity info
   * Automatically filters out enrolled courses for student users per [REQ-010]
   * @returns Promise<void>
   */
  const fetchCourses = useCallback(async () => {
    // [REQ-007] Reset loading and error state before fetch
    setIsLoading(true);
    setFetchError(null);

    try {
      // [REQ-007] Build query parameters with optional center filter and pagination
      const queryParams = new URLSearchParams();
      if (centerId) queryParams.append('centerId', centerId);
      queryParams.append('page', '1');
      queryParams.append('size', DEFAULT_PAGE_SIZE.toString());

      // [REQ-007] Execute authenticated API call (JWT auto-included via HttpOnly cookie per [NFR-003])
      const response = await axios.get(`${API_ENDPOINTS.GET_COURSES}?${queryParams.toString()}`);
      let fetchedCourses: Course[] = response.data;

      // [REQ-010] Filter courses for student users: exclude already enrolled courses
      if (user?.role === USER_ROLES.STUDENT) {
        // [REQ-010] Use user's enrolled course IDs from auth context to filter
        const enrolledCourseIds = user.enrolledCourseIds || [];
        fetchedCourses = fetchedCourses.filter(course => !enrolledCourseIds.includes(course.courseId));
      }

      // [REQ-007] Update component state with filtered course list
      setCourses(fetchedCourses);
    } catch (error) {
      // [REQ-007] Handle API errors with user-friendly localized message
      const apiError = error as ApiError;
      setFetchError(apiError.message || t('courses.errors.fetchFailed'));
      // [NFR-006] Audit log for failed course fetch (no sensitive data exposed)
      console.error(`[CourseList] [REQ-007] Course fetch failed: ${apiError.message}`);
    } finally {
      setIsLoading(false);
    }
  }, [centerId, user, t]);

  // ==============================================
  // LIFECYCLE: FETCH COURSES ON MOUNT AND FILTER CHANGE
  // ==============================================
  useEffect(() => {
    // [REQ-007] Trigger course fetch when component mounts or centerId filter changes
    fetchCourses();
  }, [fetchCourses]);

  // ==============================================
  // BUSINESS LOGIC: COURSE ENROLLMENT
  // ==============================================
  /**
   * Handles student enrollment in a selected course
   * [REQ-010], [REQ-011] Implements enrollment flow with auto-create student account if needed
   * @param courseId - Unique identifier of the course to enroll in
   * @returns Promise<void>
   */
  const handleEnroll = async (courseId: string) => {
    // [REQ-010] Prevent duplicate enrollment requests (idempotency guard)
    if (isEnrolling) return;

    setIsEnrolling(true);
    try {
      // [REQ-011] Call enrollment API, backend auto-creates student account if email is provided and user does not exist
      const response = await axios.post(API_ENDPOINTS.ENROLL_COURSE, {
        courseId,
        studentEmail: user?.email, // Pass user email for auto-account creation flow
      });

      // [REQ-010] Show success notification to user
      showSuccess(t('courses.enrollment.success'));
      // [REQ-010] Refresh course list to remove enrolled course from available list
      await fetchCourses();
      // [REQ-016] Backend triggers enrollment confirmation notification (push/Zalo) automatically
    } catch (error) {
      const apiError = error as ApiError;
      // [REQ-011] Handle enrollment errors (course full, already enrolled, validation errors)
      showError(apiError.message || t('courses.enrollment.failed'));
      // [NFR-006] Audit log for enrollment failure
      console.error(`[CourseList] [REQ-011] Enrollment failed for course ${courseId}: ${apiError.message}`);
    } finally {
      setIsEnrolling(false);
    }
  };

  // ==============================================
  // BUSINESS LOGIC: ATTENDANCE QR SCANNING
  // ==============================================
  /**
   * Opens QR scanner modal for selected course
   * [REQ-012] Triggers QR scan flow for attendance recording
   * @param course - Course object to record attendance for
   */
  const openQRScanner = (course: Course) => {
    // [REQ-012] RBAC guard: only allow scan if user is enrolled in the course
    if (!user?.enrolledCourseIds?.includes(course.courseId)) {
      showError(t(ATTENDANCE_ERROR_KEYS.NOT_ENROLLED));
      return;
    }
    setSelectedCourseForAttendance(course);
    setIsQRScannerOpen(true);
    setLastScanResult(null);
  };

  /**
   * Handles processed QR code from scanner and submits attendance request
   * [REQ-012], [REQ-013] Implements idempotent attendance processing and offline queue support
   * @param qrCode - Scanned QR code string containing course and session metadata
   */
  const handleQRScanned = async (qrCode: string) => {
    // [REQ-012] Close scanner modal immediately after scan
    setIsQRScannerOpen(false);

    // [REQ-012] Input validation for QR code format
    if (!qrCode.trim()) {
      showError(t(ATTENDANCE_ERROR_KEYS.INVALID_QR));
      return;
    }

    try {
      // [REQ-013] Prepare attendance scan payload with timestamp for idempotency check
      const payload: AttendanceScanPayload = {
        qrCode,
        timestamp: new Date().toISOString(),
      };

      // [REQ-012] Submit scan request to backend (handles idempotency at database layer)
      const response = await axios.post(API_ENDPOINTS.ATTENDANCE_SCAN, payload);
      const scanResult: AttendanceResponse = response.data;

      // [REQ-013] Handle duplicate scan response (idempotent behavior per business rule)
      if (scanResult.status === 'DUPLICATE') {
        showInfo(t('attendance.success.duplicate', { date: scanResult.attendanceDate }));
        // [NFR-006] Log duplicate scan at debug level (not an error condition)
        console.debug(`[CourseList] [REQ-013] Duplicate attendance scan for course ${selectedCourseForAttendance?.courseId}`);
      } else {
        // [REQ-012] Show success message for new attendance record
        showSuccess(t('attendance.success.recorded'));
        // [REQ-016] Backend triggers attendance confirmation push notification automatically
      }

      // [REQ-012] Update state with scan result for UI feedback
      setLastScanResult(scanResult);
      // [REQ-010] Refresh course list to update attendance status display
      await fetchCourses();
    } catch (error) {
      const apiError = error as ApiError;
      // [EXC-001] Handle network error during scan (offline queue support)
      if (apiError.code === 'ATTENDANCE_NETWORK_ERROR') {
        showInfo(t(ATTENDANCE_ERROR_KEYS.NETWORK_ERROR));
        // [EXC-001] Store pending scan in local storage for automatic retry when connection is restored
        const pendingScans = JSON.parse(localStorage.getItem('pendingAttendanceScans') || '[]');
        pendingScans.push({
          qrCode,
          courseId: selectedCourseForAttendance?.courseId,
          timestamp: new Date().toISOString(),
        });
        localStorage.setItem('pendingAttendanceScans', JSON.stringify(pendingScans));
        console.warn(`[CourseList] [EXC-001] Attendance scan queued for retry due to network error`);
      } else {
        // [REQ-012] Handle other scan errors (invalid QR, server errors)
        showError(apiError.message || t(ATTENDANCE_ERROR_KEYS.SCAN_FAILED));
        console.error(`[CourseList] [REQ-012] Attendance scan failed: ${apiError.message}`);
      }
    }
  };

  // ==============================================
  // BUSINESS LOGIC: ASSIGN TEACHER TO COURSE (ADMIN ONLY)
  // ==============================================
  /**
   * Assigns a teacher to a course (System Admin / Center Admin only)
   * [REQ-009] Implements teacher assignment with automatic notification trigger
   * @param courseId - Unique identifier of the course to update
   * @param teacherId - Unique identifier of the teacher to assign
   */
  const handleAssignTeacher = async (courseId: string, teacherId: string) => {
    try {
      // [REQ-009] Call backend API to assign teacher to course
      await axios.post(`${API_ENDPOINTS.ASSIGN_TEACHER}/${courseId}/assign-teacher`, {
        teacherId,
      });
      // [REQ-009] Show success notification to admin user
      showSuccess(t('courses.teacherAssignment.success'));
      // [REQ-016] Backend automatically sends notification to assigned teacher via push/Zalo
      // [REQ-007] Refresh course list to update displayed teacher information
      await fetchCourses();
    } catch (error) {
      const apiError = error as ApiError;
      showError(apiError.message || t('courses.teacherAssignment.failed'));
      console.error(`[CourseList] [REQ-009] Teacher assignment failed for course ${courseId}: ${apiError.message}`);
    }
  };

  // ==============================================
  // UI RENDER: INDIVIDUAL COURSE CARD
  // ==============================================
  /**
   * Renders individual course card with role-based action buttons
   * [REQ-007], [REQ-010], [REQ-012], [REQ-013] Implements all course-related user actions with RBAC guards
   * @param course - Course data object to render
   * @returns JSX element for course card
   */
  const renderCourseCard = (course: Course) => {
    // [REQ-003] Determine user role for RBAC rendering
    const isStudent = user?.role === USER_ROLES.STUDENT;
    const isAdmin = [USER_ROLES.SYSTEM_ADMIN, USER_ROLES.CENTER_ADMIN].includes(user?.role || '');
    const isEnrolled = user?.enrolledCourseIds?.includes(course.courseId);
    // [REQ-013] Check if user has already attended this course today (returned by backend)
    const hasAttendedToday = course.userAttendanceStatus === 'PRESENT';

    return (
      <div key={course.courseId} className="bg-white rounded-lg shadow-md p-6 mb-4 hover:shadow-lg transition-shadow">
        {/* [REQ-007] Render core course information */}
        <div className="mb-4">
          <h3 className="text-xl font-semibold text-gray-800">{course.title}</h3>
          <p className="text-gray-600 mt-1 line-clamp-2">{course.description}</p>
          <div className="mt-3 text-sm text-gray-500 space-y-1">
            {/* [REQ-007] Display course schedule and teacher details */}
            <p>📅 {new Date(course.startDate).toLocaleDateString()} - {new Date(course.endDate).toLocaleDateString()}</p>
            <p>👨‍🏫 {course.teacherName || t('courses.noTeacherAssigned')}</p>
            <p>👥 {course.enrolledCount}/{course.maxStudents} {t('courses.studentsEnrolled')}</p>
          </div>
        </div>

        {/* [REQ-010] Enrollment button for students (only if not enrolled and slots available) */}
        {isStudent && !isEnrolled && course.enrolledCount < course.maxStudents && (
          <button
            onClick={() => handleEnroll(course.courseId)}
            disabled={isEnrolling}
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:bg-blue-300 transition-colors"
            aria-label={t('courses.enrollment.button')}
          >
            {isEnrolling ? t('courses.enrollment.loading') : t('courses.enrollment.button')}
          </button>
        )}

        {/* [REQ-012] Attendance scan button for enrolled students who haven't attended today */}
        {isStudent && isEnrolled && !hasAttendedToday && (
          <button
            onClick={() => openQRScanner(course)}
            className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition-colors ml-2"
            aria-label={t('attendance.scanButton')}
          >
            {t('attendance.scanButton')}
          </button>
        )}

        {/* [REQ-013] Show attendance status if already recorded for today */}
        {isStudent && hasAttendedToday && (
          <span className="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium">
            {t('attendance.status.present')}
          </span>
        )}

        {/* [REQ-009] Assign teacher button for admin users only */}
        {isAdmin && (
          <button
            onClick={() => {
              // In production, replace with modal for teacher selection
              const selectedTeacherId = prompt(t('courses.teacherAssignment.prompt'));
              if (selectedTeacherId) handleAssignTeacher(course.courseId, selectedTeacherId);
            }}
            className="bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700 transition-colors ml-2"
            aria-label={t('courses.teacherAssignment.button')}
          >
            {t('courses.teacherAssignment.button')}
          </button>
        )}
      </div>
    );
  };

  // ==============================================
  // MAIN UI RENDER
  // ==============================================
  return (
    <div className="container mx-auto px-4 py-8">
      {/* [REQ-007] Page header with localized title */}
      <h1 className="text-3xl font-bold text-gray-800 mb-6">{t('courses.title')}</h1>

      {/* [REQ-007] Loading spinner while fetching courses */}
      {isLoading && (
        <div className="flex justify-center items-center h-64" role="status" aria-label="Loading courses">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <span className="sr-only">{t('courses.loading')}</span>
        </div>
      )}

      {/* [REQ-007] Error message with retry option */}
      {fetchError && !isLoading && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4" role="alert">
          {fetchError}
          <button
            onClick={fetchCourses}
            className="ml-4 underline text-red-800 hover:text-red-900 font-medium"
            aria-label={t('common.retry')}
          >
            {t('common.retry')}
          </button>
        </div>
      )}

      {/* [REQ-007] Empty state when no courses are available */}
      {!isLoading && !fetchError && courses.length === 0 && (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg">{t('courses.noCoursesAvailable')}</p>
        </div>
      )}

      {/* [REQ-007] Responsive course grid layout */}
      {!isLoading && !fetchError && courses.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {courses.map(renderCourseCard)}
        </div>
      )}

      {/* [REQ-012] QR Scanner Modal for attendance scanning */}
      {isQRScannerOpen && selectedCourseForAttendance && (
        <QRScanner
          onScan={handleQRScanned}
          onClose={() => setIsQRScannerOpen(false)}
          courseName={selectedCourseForAttendance.title}
        />
      )}

      {/* [REQ-013] Toast notification for last scan result */}
      {lastScanResult && (
        <div className={`fixed bottom-4 right-4 px-6 py-4 rounded-lg shadow-lg ${
          lastScanResult.status === 'DUPLICATE' ? 'bg-yellow-100 text-yellow-800 border border-yellow-300' : 'bg-green-100 text-green-800 border border-green-300'
        }`} role="status" aria-live="polite">
          <p className="font-semibold">
            {lastScanResult.status === 'DUPLICATE' ? t('attendance.duplicateTitle') : t('attendance.successTitle')}
          </p>
          <p>{lastScanResult.message}</p>
        </div>
      )}
    </div>
  );
};

export default CourseList;