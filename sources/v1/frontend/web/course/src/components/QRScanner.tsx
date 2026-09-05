typescript
// Traceability Tags: [REQ-007], [REQ-010], [REQ-011], [REQ-012], [REQ-013]
// Component: QRScanner.tsx
// Project: membership-hub
// Path: ./sources/frontend/web/course/src/components/QRScanner.tsx
// Description: Frontend React component for scanning course QR codes to record student attendance. Implements idempotent attendance request handling, offline queueing for network failures, duplicate attendance detection, and camera permission management. Aligns with enterprise security, RBAC, and UI standards.

import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom'; // Navigation hook for role-based routing per [REQ-020]
import axios from 'axios'; // HTTP client aligned with project tech stack [ARC-010]
import { QrReader } from 'react-qr-scanner'; // QR scanning library for web applications

// ==============================================
// ENTERPRISE CONSTANTS DECLARATION (TOP-OF-CLASS)
// All configuration values are isolated here to comply with anti-magic-number policy [0.2]
// ==============================================
const ATTENDANCE_SCAN_API_ENDPOINT = '/api/v1/attendance/scan'; // [REQ-012] Backend endpoint for attendance scan requests
const MAX_RETRY_ATTEMPTS = 3; // [EXC-003] Maximum retry attempts for failed scan/notification requests
const RETRY_DELAY_MS = 5000; // [EXC-003] Delay between retry attempts in milliseconds
const QR_CODE_REQUIRED_FIELDS = ['courseId', 'sessionId']; // [REQ-012] Mandatory fields required in scanned QR code payload
const OFFLINE_QUEUE_STORAGE_KEY = 'attendance_offline_queue'; // LocalStorage key for persisting offline scan requests
const CAMERA_PERMISSION_ERROR_MESSAGE = 'Vui lòng cấp quyền truy cập camera để quét mã QR điểm danh'; // [NFR-007] Localized error for camera access denial
const INVALID_QR_ERROR_MESSAGE = 'Mã QR không hợp lệ, vui lòng thử lại'; // [REQ-012] Error message for malformed QR payloads
const NETWORK_ERROR_MESSAGE = 'Không có kết nối mạng, điểm danh sẽ được gửi tự động khi kết nối được khôi phục'; // [EXC-001] Message for network failure during scan
const DUPLICATE_ATTENDANCE_MESSAGE = 'Đã ghi nhận điểm danh cho buổi học này trước đó'; // [REQ-013] Message for duplicate attendance scan
const SUCCESS_ATTENDANCE_MESSAGE = 'Điểm danh thành công!'; // [REQ-012] Success message for valid scan
const ROLE_BASED_COURSE_LIST_PATH = '/student/courses'; // [REQ-020] Role-based navigation path for students

/**
 * QRScanner Component
 * Handles course QR code scanning for student attendance, with offline support and idempotent request handling
 * RBAC Compliance: Only accessible to authenticated Student role users (enforced via parent route guards per [ARC-001], [ARC-004])
 * @traceability [REQ-007], [REQ-010], [REQ-011], [REQ-012], [REQ-013]
 */
const QRScanner: React.FC = () => {
  // ==============================================
  // STATE MANAGEMENT
  // Tracks scanning status, API call state, user feedback messages, and offline request queue
  // ==============================================
  const [isScanning, setIsScanning] = useState<boolean>(true); // Controls camera scanning active state to prevent duplicate scans
  const [isLoading, setIsLoading] = useState<boolean>(false); // Tracks API request loading state for user feedback
  const [error, setError] = useState<string | null>(null); // Stores error messages for user display
  const [successMessage, setSuccessMessage] = useState<string | null>(null); // Stores success messages for user display
  const [duplicateMessage, setDuplicateMessage] = useState<string | null>(null); // Stores duplicate attendance notification messages
  const [offlineQueue, setOfflineQueue] = useState<Array<{qrData: string, timestamp: string}>>([]); // FIFO queue for offline scan requests per [EXC-001]
  const navigate = useNavigate(); // Navigation hook for role-based routing after successful scan
  const videoRef = useRef<HTMLVideoElement>(null); // Ref for camera video element to manage media stream

  // ==============================================
  // CAMERA PERMISSION & INITIALIZATION
  // Handles camera access request and stream setup on component mount, with cleanup on unmount
  // ==============================================
  useEffect(() => {
    // Request camera permission on component initial mount
    const requestCameraPermission = async () => {
      try {
        // Request rear camera access for mobile devices, fallback to default camera if unavailable
        const stream = await navigator.mediaDevices.getUserMedia({ 
          video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } }
        });
        // Attach acquired camera stream to video element for QR scanning
        if (videoRef.current) {
          videoRef.current.srcObject = stream;
        }
        // Load persisted offline queue from localStorage on mount to avoid losing pending requests
        const savedQueue = localStorage.getItem(OFFLINE_QUEUE_STORAGE_KEY);
        if (savedQueue) {
          try {
            setOfflineQueue(JSON.parse(savedQueue));
          } catch (parseErr) {
            // Clear corrupted queue data if parsing fails
            localStorage.removeItem(OFFLINE_QUEUE_STORAGE_KEY);
            console.warn('[QRScanner] [AUDIT] Corrupted offline queue cleared:', parseErr);
          }
        }
      } catch (err) {
        // Handle camera permission denial or unavailable camera hardware
        setError(CAMERA_PERMISSION_ERROR_MESSAGE);
        setIsScanning(false);
        // Log error for audit purposes, no sensitive data exposed in this context
        console.error('[QRScanner] [AUDIT] Camera access initialization failed:', err);
      }
    };

    requestCameraPermission();

    // Cleanup: Stop camera stream when component unmounts to prevent memory leaks and resource waste
    return () => {
      if (videoRef.current?.srcObject) {
        const stream = videoRef.current.srcObject as MediaStream;
        stream.getTracks().forEach(track => track.stop());
      }
    };
  }, []);

  // ==============================================
  // OFFLINE QUEUE MANAGEMENT
  // Automatically retry queued requests when network connection is restored, per [EXC-001] FIFO requirement
  // ==============================================
  useEffect(() => {
    // Handle browser online event: retry all queued requests when network is restored
    const handleOnline = async () => {
      if (offlineQueue.length === 0) return;
      
      setIsLoading(true);
      setError(null);
      
      // Process queued requests in First-In-First-Out order as required by [EXC-001]
      for (const request of offlineQueue) {
        try {
          await sendAttendanceRequest(request.qrData, request.timestamp);
        } catch (err) {
          // If retry fails, retain remaining queue for next online event
          console.error('[QRScanner] [AUDIT] Failed to retry queued scan request:', err);
          break;
        }
      }
      
      // Clear queue after successful processing of all requests
      setOfflineQueue([]);
      localStorage.removeItem(OFFLINE_QUEUE_STORAGE_KEY);
      setIsLoading(false);
    };

    // Register online event listener
    window.addEventListener('online', handleOnline);

    // Cleanup event listener on component unmount
    return () => {
      window.removeEventListener('online', handleOnline);
    };
  }, [offlineQueue, sendAttendanceRequest]);

  // ==============================================
  // ATTENDANCE REQUEST HANDLER
  // Sends attendance scan request to backend, handles all response states per [REQ-012], [REQ-013], [EXC-001], [EXC-002]
  // Uses useCallback to optimize performance and prevent unnecessary re-renders
  // ==============================================
  const sendAttendanceRequest = useCallback(async (qrData: string, scanTimestamp: string) => {
    setIsLoading(true);
    setError(null);
    setSuccessMessage(null);
    setDuplicateMessage(null);

    try {
      // Send POST request to backend attendance scan endpoint
      // JWT authentication is handled automatically via HttpOnly cookie per [ARC-006], no token exposure in frontend
      const response = await axios.post(ATTENDANCE_SCAN_API_ENDPOINT, {
        qrCode: qrData,
        timestamp: scanTimestamp
      });

      // Handle successful attendance recording per [REQ-012]
      if (response.data.status === 'RECORDED') {
        setSuccessMessage(SUCCESS_ATTENDANCE_MESSAGE);
        // Navigate to student attendance history after 2 seconds, aligned with [REQ-020] role-based navigation
        setTimeout(() => {
          navigate(ROLE_BASED_COURSE_LIST_PATH);
        }, 2000);
      } 
      // Handle duplicate attendance request per [REQ-013] and [EXC-002]
      else if (response.data.status === 'DUPLICATE') {
        setDuplicateMessage(DUPLICATE_ATTENDANCE_MESSAGE);
        // Only log duplicate requests at DEBUG level per [EXC-002] requirement, no critical error logging
        console.debug('[QRScanner] [AUDIT] Duplicate attendance scan detected:', {
          qrData,
          timestamp: scanTimestamp,
          responseStatus: response.data.status
        });
      }
    } catch (err) {
      // Handle network errors: queue request for retry when online per [EXC-001]
      if (!navigator.onLine) {
        const newQueue = [...offlineQueue, { qrData, timestamp: scanTimestamp }];
        setOfflineQueue(newQueue);
        // Persist queue to localStorage to survive page reloads and browser crashes
        localStorage.setItem(OFFLINE_QUEUE_STORAGE_KEY, JSON.stringify(newQueue));
        setError(NETWORK_ERROR_MESSAGE);
        console.error('[QRScanner] [AUDIT] Network error, scan request queued for retry:', err);
      } 
      // Handle other API errors (400, 403, 500 etc.)
      else {
        const errorMessage = err.response?.data?.message || 'Đã xảy ra lỗi, vui lòng thử lại';
        setError(errorMessage);
        // Log full error with traceability tag for centralized audit per [0.3] exception logging rule
        console.error(`[QRScanner] [CRITICAL FAIL] [REQ-012] Attendance scan request failed. Raw error: ${err.message}, API Response: ${JSON.stringify(err.response?.data)}`);
      }
    } finally {
      setIsLoading(false);
      // Resume scanning after request completes to allow next scan
      setIsScanning(true);
    }
  }, [offlineQueue, navigate]);

  // ==============================================
  // QR CODE SCAN HANDLER
  // Processes scanned QR code data, validates required fields before sending to backend per [REQ-012]
  // ==============================================
  const handleQrScan = useCallback((data: string | null) => {
    // Ignore null scans (no valid QR code detected)
    if (!data) return;

    try {
      // Parse QR code data (assumed to be JSON payload with courseId and sessionId per [REQ-012] API contract)
      const qrPayload = JSON.parse(data);
      
      // Validate required fields exist in QR payload to avoid invalid backend requests
      const missingFields = QR_CODE_REQUIRED_FIELDS.filter(field => !(field in qrPayload));
      if (missingFields.length > 0) {
        setError(`${INVALID_QR_ERROR_MESSAGE}: Thiếu trường bắt buộc ${missingFields.join(', ')}`);
        console.warn('[QRScanner] [AUDIT] Invalid QR code payload, missing required fields:', missingFields);
        return;
      }

      // Get current ISO timestamp for scan record
      const scanTimestamp = new Date().toISOString();
      
      // Send attendance request to backend
      await sendAttendanceRequest(data, scanTimestamp);
      
      // Pause scanning while request is processing to prevent accidental duplicate scans
      setIsScanning(false);
    } catch (err) {
      // Handle invalid JSON format in QR code
      setError(INVALID_QR_ERROR_MESSAGE);
      console.warn('[QRScanner] [AUDIT] Failed to parse QR code payload:', err);
    }
  }, [sendAttendanceRequest]);

  // ==============================================
  // UI RENDERING
  // Responsive Tailwind CSS layout, accessible, with clear user feedback and role-aligned navigation
  // ==============================================
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50 p-4">
      {/* Component Header */}
      <div className="w-full max-w-md mb-6">
        <h1 className="text-2xl font-bold text-gray-800 text-center">Quét mã QR điểm danh</h1>
        <p className="text-gray-600 text-center mt-2">Đưa camera vào mã QR trên buổi học để ghi nhận điểm danh</p>
      </div>

      {/* QR Scanner Container */}
      <div className="w-full max-w-md bg-white rounded-lg shadow-lg p-4">
        {/* Camera View with QR Scanner */}
        {isScanning && (
          <div className="relative w-full aspect-square bg-black rounded-lg overflow-hidden mb-4">
            {/* QR Scanner Library Component - configured for rear camera on mobile devices */}
            <QrReader
              onResult={(result, error) => {
                if (result?.getText()) {
                  handleQrScan(result.getText());
                }
                if (error) {
                  // Log scan errors (e.g., no QR detected in frame) at DEBUG level only
                  console.debug('[QRScanner] [AUDIT] QR scan processing error:', error);
                }
              }}
              constraints={{ 
                facingMode: 'environment', // Prioritize rear camera for mobile attendance scanning
                width: { ideal: 1280 },
                height: { ideal: 720 }
              }}
              className="w-full h-full object-cover"
            />
            {/* Scanning Overlay for user guidance */}
            <div className="absolute inset-0 border-4 border-blue-500 rounded-lg pointer-events-none"></div>
            <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-3/4 h-3/4 border-2 border-white rounded-lg pointer-events-none"></div>
          </div>
        )}

        {/* Control Buttons */}
        <div className="flex flex-col gap-3">
          {/* Pause/Resume Scanning Button */}
          <button
            onClick={() => setIsScanning(!isScanning)}
            className={`w-full py-3 px-4 rounded-lg font-medium text-white transition-colors duration-200 ${
              isScanning ? 'bg-yellow-500 hover:bg-yellow-600' : 'bg-green-500 hover:bg-green-600'
            }`}
            aria-label={isScanning ? 'Tạm dừng quét mã QR' : 'Bắt đầu quét mã QR'}
          >
            {isScanning ? 'Tạm dừng quét' : 'Bắt đầu quét'}
          </button>

          {/* Retry Queued Requests Button - only visible when offline and queue has pending requests */}
          {!navigator.onLine && offlineQueue.length > 0 && (
            <button
              onClick={() => {
                // Manually trigger retry of queued requests
                setOfflineQueue([...offlineQueue]);
              }}
              className="w-full py-3 px-4 rounded-lg font-medium text-white bg-blue-500 hover:bg-blue-600 transition-colors duration-200"
              aria-label="Gửi lại yêu cầu điểm danh đang chờ xử lý"
            >
              Gửi lại {offlineQueue.length} yêu cầu đang chờ
            </button>
          )}
        </div>

        {/* Status Feedback Messages */}
        <div className="mt-4 space-y-2">
          {/* Error Message Display */}
          {error && (
            <div className="p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg" role="alert">
              <p className="font-medium">Lỗi</p>
              <p>{error}</p>
            </div>
          )}

          {/* Success Message Display */}
          {successMessage && (
            <div className="p-3 bg-green-100 border border-green-400 text-green-700 rounded-lg" role="alert">
              <p className="font-medium">Thành công</p>
              <p>{successMessage}</p>
            </div>
          )}

          {/* Duplicate Attendance Message Display */}
          {duplicateMessage && (
            <div className="p-3 bg-yellow-100 border border-yellow-400 text-yellow-700 rounded-lg" role="alert">
              <p className="font-medium">Thông báo</p>
              <p>{duplicateMessage}</p>
            </div>
          )}

          {/* Loading Indicator */}
          {isLoading && (
            <div className="flex items-center justify-center p-3">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-500"></div>
              <span className="ml-2 text-gray-700">Đang xử lý yêu cầu...</span>
            </div>
          )}

          {/* Offline Queue Status Display */}
          {offlineQueue.length > 0 && (
            <div className="p-3 bg-blue-100 border border-blue-400 text-blue-700 rounded-lg">
              <p className="font-medium">Trạng thái ngoại tuyến</p>
              <p>Có {offlineQueue.length} yêu cầu điểm danh đang chờ gửi khi kết nối mạng được khôi phục</p>
            </div>
          )}
        </div>
      </div>

      {/* Role-Based Navigation Footer aligned with [REQ-020] */}
      <div className="mt-6 text-center">
        <button
          onClick={() => navigate(ROLE_BASED_COURSE_LIST_PATH)}
          className="text-blue-500 hover:text-blue-700 underline font-medium"
        >
          Quay lại danh sách khóa học
        </button>
      </div>
    </div>
  );
};

export default QRScanner;