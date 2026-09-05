typescript
// Traceability Tags: [REQ-007], [REQ-010], [REQ-011], [REQ-012], [REQ-013]
// Component: EnrollmentForm.tsx
// Description: Frontend component for student course enrollment, supporting course selection via dropdown or QR scan, auto-student account creation, and integration with backend enrollment API
// Business Context: Allows students to browse available courses, enroll in selected courses, and use QR code scanning to quickly select courses for enrollment, aligning with the system's idempotent enrollment and QR integration requirements

import React, { useState, useEffect } from 'react';
import { useAuth } from '@/contexts/AuthContext'; // [REQ-001] Import auth context for user role and authentication state
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'; // [NFR-004] Use React Query for efficient data fetching and caching
import axios from 'axios'; // [ARC-010] Use Axios for HTTP API calls as per tech stack
import { useTranslation } from 'react-i18next'; // [REQ-022] Import i18n for multi-language support
import QRScanner from '@/components/QRScanner'; // [REQ-012] Import QR scanner component for course selection

// -------------------------- CONSTANTS DECLARATION (NO HARDCODED LITERALS IN LOGIC) --------------------------
// [NFR-003] All configuration values isolated as top-level constants for maintainability and security
const API_ENDPOINTS = {
  GET_AVAILABLE_COURSES: '/api/v1/courses/available',
  POST_ENROLLMENT: '/api/v1/enrollments',
} as const;

const ERROR_MESSAGES = {
  VALIDATION_FAILED: 'validation_failed',
  ENROLLMENT_FAILED: 'enrollment_failed',
  COURSE_NOT_FOUND: 'course_not_found',
  ALREADY_ENROLLED: 'already_enrolled',
  NETWORK_ERROR: 'network_error',
  UNAUTHORIZED: 'unauthorized',
  FORBIDDEN: 'forbidden',
} as const;

const VALIDATION_RULES = {
  EMAIL_REGEX: /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/,
  MIN_PASSWORD_LENGTH: 8,
} as const;

const IDEMPOTENCY_KEY_HEADER = 'Idempotency-Key'; // [REQ-011] Idempotency key header to prevent duplicate enrollment requests

// -------------------------- TYPE DEFINITIONS --------------------------
interface Course {
  courseId: string;
  title: string;
  startDate: string;
  endDate: string;
  teacherName: string;
  maxStudents: number;
  remainingSlots: number;
}

interface EnrollmentFormData {
  courseId: string;
  studentEmail?: string;
}

// -------------------------- COMPONENT IMPLEMENTATION --------------------------
const EnrollmentForm: React.FC = () => {
  // [REQ-001] Get authenticated user state from auth context
  const { user, isAuthenticated } = useAuth();
  const { t } = useTranslation(); // [REQ-022] Initialize i18n translation function
  const queryClient = useQueryClient();

  // -------------------------- STATE MANAGEMENT --------------------------
  const [selectedCourse, setSelectedCourse] = useState<Course | null>(null);
  const [studentEmail, setStudentEmail] = useState<string>('');
  const [isQRScannerOpen, setIsQRScannerOpen] = useState<boolean>(false);
  const [formError, setFormError] = useState<string>('');
  const [formSuccess, setFormSuccess] = useState<string>('');

  // -------------------------- RBAC ACCESS CONTROL --------------------------
  // [ARC-001], [ARC-004] Only students can access enrollment form, enforce role-based access control
  if (!isAuthenticated || user?.role !== 'Student') {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {t('errors.access_denied', 'Bạn không có quyền truy cập trang này')}
        </div>
      </div>
    );
  }

  // -------------------------- DATA FETCHING: AVAILABLE COURSES --------------------------
  // [REQ-010] Fetch list of available courses for student, excluding already enrolled courses
  const { data: availableCourses = [], isLoading: isLoadingCourses, error: coursesError } = useQuery<Course[]>({
    queryKey: ['availableCourses'],
    queryFn: async () => {
      const response = await axios.get(API_ENDPOINTS.GET_AVAILABLE_COURSES, {
        headers: {
          Authorization: `Bearer ${user?.token}`,
        },
      });
      return response.data;
    },
    enabled: isAuthenticated && user?.role === 'Student', // Only fetch if user is authenticated student
    staleTime: 5 * 60 * 1000, // Cache course list for 5 minutes to reduce API calls [NFR-004]
  });

  // -------------------------- ENROLLMENT MUTATION --------------------------
  // [REQ-011] Mutation to submit enrollment request, with idempotency key to prevent duplicate submissions
  const enrollmentMutation = useMutation({
    mutationFn: async (formData: EnrollmentFormData) => {
      // [NFR-003] Generate unique idempotency key for each request to prevent duplicate processing
      const idempotencyKey = `${user?.userId}-${formData.courseId}-${Date.now}`;
      
      const payload = {
        courseId: formData.courseId,
        ...(formData.studentEmail && { studentEmail: formData.studentEmail }), // Only include email if provided for auto-account creation
      };

      const response = await axios.post(API_ENDPOINTS.POST_ENROLLMENT, payload, {
        headers: {
          Authorization: `Bearer ${user?.token}`,
          [IDEMPOTENCY_KEY_HEADER]: idempotencyKey, // [REQ-013] Enforce idempotency for enrollment requests
        },
      });

      return response.data;
    },
    onSuccess: () => {
      // [REQ-011] Invalidate course list cache to reflect updated enrollment status
      queryClient.invalidateQueries({ queryKey: ['availableCourses'] });
      setFormSuccess(t('enrollment.success', 'Đăng ký khóa học thành công!'));
      setFormError('');
      // Reset form after successful enrollment
      setSelectedCourse(null);
      setStudentEmail('');
    },
    onError: (error: any) => {
      // [EXC-004] Comprehensive error handling with user-friendly messages
      const errorCode = error.response?.data?.error;
      let errorMessage = t('enrollment.generic_error', 'Đăng ký khóa học thất bại, vui lòng thử lại');

      switch (errorCode) {
        case ERROR_MESSAGES.VALIDATION_FAILED:
          errorMessage = t('enrollment.validation_error', 'Thông tin nhập không hợp lệ');
          break;
        case ERROR_MESSAGES.ALREADY_ENROLLED:
          errorMessage = t('enrollment.already_enrolled', 'Bạn đã đăng ký khóa học này rồi');
          break;
        case ERROR_MESSAGES.COURSE_NOT_FOUND:
          errorMessage = t('enrollment.course_not_found', 'Khóa học không tồn tại');
          break;
        case ERROR_MESSAGES.NETWORK_ERROR:
          errorMessage = t('errors.network', 'Lỗi kết nối mạng, vui lòng thử lại sau');
          break;
        case ERROR_MESSAGES.UNAUTHORIZED:
          errorMessage = t('errors.unauthorized', 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại');
          break;
        case ERROR_MESSAGES.FORBIDDEN:
          errorMessage = t('errors.forbidden', 'Bạn không có quyền đăng ký khóa học này');
          break;
        default:
          if (error.response?.data?.message) {
            errorMessage = error.response.data.message;
          }
      }

      setFormError(errorMessage);
      setFormSuccess('');
    },
  });

  // -------------------------- EVENT HANDLERS --------------------------
  // [REQ-012] Handle QR code scan result to auto-select course
  const handleQRScan = (qrData: string) => {
    try {
      // Parse QR code data to extract courseId (QR format: "courseId:{uuid}|sessionId:{uuid}")
      const courseIdMatch = qrData.match(/courseId:([^|]+)/);
      if (courseIdMatch && courseIdMatch[1]) {
        const scannedCourseId = courseIdMatch[1];
        const foundCourse = availableCourses.find(course => course.courseId === scannedCourseId);
        if (foundCourse) {
          setSelectedCourse(foundCourse);
          setIsQRScannerOpen(false);
          setFormError('');
        } else {
          setFormError(t('enrollment.qr_course_not_found', 'Mã QR không thuộc về khóa học có sẵn để đăng ký'));
        }
      } else {
        setFormError(t('enrollment.invalid_qr', 'Mã QR không hợp lệ'));
      }
    } catch (err) {
      setFormError(t('enrollment.qr_parse_error', 'Không thể đọc mã QR, vui lòng thử lại'));
    }
  };

  // [REQ-011] Handle form submission for enrollment
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');
    setFormSuccess('');

    // Input validation
    if (!selectedCourse) {
      setFormError(t('enrollment.select_course', 'Vui lòng chọn khóa học để đăng ký'));
      return;
    }

    // Validate email if user does not have existing student account (auto-creation flow)
    if (!user?.studentId && !studentEmail) {
      setFormError(t('enrollment.email_required', 'Vui lòng nhập email để tạo tài khoản học viên'));
      return;
    }

    if (!user?.studentId && studentEmail && !VALIDATION_RULES.EMAIL_REGEX.test(studentEmail)) {
      setFormError(t('enrollment.invalid_email', 'Địa chỉ email không hợp lệ'));
      return;
    }

    // Submit enrollment request
    enrollmentMutation.mutate({
      courseId: selectedCourse.courseId,
      studentEmail: studentEmail || undefined,
    });
  };

  // -------------------------- RENDER --------------------------
  return (
    <div className="max-w-2xl mx-auto p-6 bg-white rounded-lg shadow-md">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">
        {t('enrollment.title', 'Đăng ký khóa học')}
      </h2>

      {/* Success Message Display */}
      {formSuccess && (
        <div className="mb-4 p-3 bg-green-50 border border-green-200 text-green-700 rounded-lg">
          {formSuccess}
        </div>
      )}

      {/* Error Message Display */}
      {formError && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg">
          {formError}
        </div>
      )}

      {/* Loading State for Course List */}
      {isLoadingCourses && (
        <div className="flex items-center justify-center py-8">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <span className="ml-2 text-gray-600">{t('common.loading', 'Đang tải...')}</span>
        </div>
      )}

      {/* Error State for Course List */}
      {coursesError && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg">
          {t('courses.load_error', 'Không thể tải danh sách khóa học, vui lòng thử lại sau')}
        </div>
      )}

      {/* Enrollment Form */}
      {!isLoadingCourses && !coursesError && (
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Course Selection Field */}
          <div>
            <label htmlFor="course" className="block text-sm font-medium text-gray-700 mb-2">
              {t('enrollment.select_course_label', 'Chọn khóa học')}
            </label>
            <div className="flex gap-2">
              <select
                id="course"
                value={selectedCourse?.courseId || ''}
                onChange={(e) => {
                  const course = availableCourses.find(c => c.courseId === e.target.value);
                  setSelectedCourse(course || null);
                  setFormError('');
                }}
                className="flex-1 p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                required
              >
                <option value="">{t('enrollment.select_course_placeholder', '-- Chọn khóa học --')}</option>
                {availableCourses.map((course) => (
                  <option key={course.courseId} value={course.courseId}>
                    {course.title} ({t('courses.teacher', 'Giáo viên')}: {course.teacherName}, {t('courses.schedule', 'Lịch')}: {new Date(course.startDate).toLocaleDateString()} - {new Date(course.endDate).toLocaleDateString()})
                  </option>
                ))}
              </select>
              {/* [REQ-012] QR Scan Button for quick course selection */}
              <button
                type="button"
                onClick={() => setIsQRScannerOpen(true)}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                title={t('enrollment.scan_qr', 'Quét mã QR khóa học')}
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z" />
                </svg>
              </button>
            </div>
            {/* Selected Course Details Display */}
            {selectedCourse && (
              <div className="mt-2 p-3 bg-blue-50 rounded-lg">
                <p className="text-sm text-gray-700">
                  <span className="font-medium">{t('courses.title', 'Tên khóa học')}:</span> {selectedCourse.title}
                </p>
                <p className="text-sm text-gray-700">
                  <span className="font-medium">{t('courses.teacher', 'Giáo viên')}:</span> {selectedCourse.teacherName}
                </p>
                <p className="text-sm text-gray-700">
                  <span className="font-medium">{t('courses.schedule', 'Lịch học')}:</span> {new Date(selectedCourse.startDate).toLocaleDateString()} - {new Date(selectedCourse.endDate).toLocaleDateString()}
                </p>
                <p className="text-sm text-gray-700">
                  <span className="font-medium">{t('courses.remaining_slots', 'Sĩ số còn lại')}:</span> {selectedCourse.remainingSlots}/{selectedCourse.maxStudents}
                </p>
              </div>
            )}
          </div>

          {/* [REQ-011] Email Input for Auto-Student Account Creation (only if user has no existing student ID) */}
          {!user?.studentId && (
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                {t('enrollment.email_label', 'Địa chỉ email (để tạo tài khoản học viên)')}
              </label>
              <input
                type="email"
                id="email"
                value={studentEmail}
                onChange={(e) => setStudentEmail(e.target.value)}
                className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder={t('enrollment.email_placeholder', 'Nhập địa chỉ email của bạn')}
                required
              />
              <p className="mt-1 text-sm text-gray-500">
                {t('enrollment.email_hint', 'Email này sẽ được dùng để tạo tài khoản học viên nếu bạn chưa có tài khoản')}
              </p>
            </div>
          )}

          {/* Submit Button */}
          <button
            type="submit"
            disabled={enrollmentMutation.isPending || !selectedCourse}
            className="w-full py-2 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:bg-gray-400 disabled:cursor-not-allowed"
          >
            {enrollmentMutation.isPending ? t('common.loading', 'Đang xử lý...') : t('enrollment.submit', 'Đăng ký khóa học')}
          </button>
        </form>
      )}

      {/* [REQ-012] QR Scanner Modal */}
      {isQRScannerOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold">
                {t('enrollment.qr_scanner_title', 'Quét mã QR khóa học')}
              </h3>
              <button
                onClick={() => setIsQRScannerOpen(false)}
                className="text-gray-500 hover:text-gray-700"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            <QRScanner onScan={handleQRScan} onClose={() => setIsQRScannerOpen(false)} />
          </div>
        </div>
      )}
    </div>
  );
};

export default EnrollmentForm;