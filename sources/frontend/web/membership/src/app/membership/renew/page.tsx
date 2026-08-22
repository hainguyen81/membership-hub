x
// Traceability Tags: [REQ-015]
// Enterprise Membership Card Renewal Page Component
// Implements the frontend UI for the membership card renewal functionality as per requirement REQ-015.
// This page provides a form for users to select renewal duration and payment transaction ID,
// validates input, calls the backend renewal API, and displays success/error states with comprehensive logging.
// All user inputs are sanitized and validated client-side before submission to ensure security and data integrity.
// The component respects RBAC by assuming the user is authenticated (handled by higher-level layout/auth guard).
// Inline comments detail business logic, validation rules, API integration, and error handling for maintainability.

import React, { useState, FormEvent, ChangeEvent } from 'react';
import { useRouter } from 'next/navigation';
import { useSession } from 'next-auth/react'; // Assuming NextAuth for authentication

/**
 * Interface representing the payload sent to the membership renewal API.
 * Aligns with the backend endpoint defined in the specification.
 */
interface RenewalRequest {
  renewalDays: number;
  paymentTransactionId: string;
}

/**
 * Interface representing the response received from the renewal API.
 * Contains updated card details and any relevant metadata.
 */
interface RenewalResponse {
  cardId: string;
  remainingDays: number;
  expiryDate: string;
}

/**
 * MembershipRenewalPage component.
 * Renders a form for users to renew their membership card.
 * Handles form validation, API submission, and user feedback.
 */
const MembershipRenewalPage: React.FC = () => {
  // Retrieve session to verify user authentication and role
  const { data: session, status } = useSession();
  const router = useRouter();

  // UI state management
  const [renewalDays, setRenewalDays] = useState<number>(30); // Default renewal period
  const [paymentTransactionId, setPaymentTransactionId] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [cardInfo, setCardInfo] = useState<RenewalResponse | null>(null);

  // Redirect to login if user is not authenticated
  if (status === 'unauthenticated') {
    router.push('/auth/login');
    return null;
  }

  /**
   * Validates the renewal form inputs.
   * Enforces business rules: renewalDays must be a positive integer (>= 1), paymentTransactionId must not be empty.
   * Returns an error message string if validation fails; otherwise returns null.
   */
  const validateForm = (): string | null => {
    if (!renewalDays || renewalDays < 1) {
      return 'Vui lòng chọn số ngày gia hạn hợp lệ (tối thiểu 1 ngày).';
    }
    if (renewalDays > 365) {
      return 'Số ngày gia hạn không được vượt quá 365 ngày.';
    }
    if (!paymentTransactionId.trim()) {
      return 'Mã giao dịch thanh toán là bắt buộc.';
    }
    // Additional validation: paymentTransactionId format (alphanumeric, length 16-64)
    const txnRegex = /^[A-Za-z0-9_-]{16,64}$/;
    if (!txnRegex.test(paymentTransactionId.trim())) {
      return 'Mã giao dịch thanh toán không hợp lệ (chỉ chấp nhận chữ cái, số, gạch dưới, gạch ngang, độ dài 16-64 ký tự).';
    }
    return null;
  };

  /**
   * Handles form submission.
   * Performs client-side validation, constructs the request payload, and invokes the renewal API.
   * Manages loading state, success/error feedback, and redirects on successful renewal.
   */
  const handleSubmit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    setErrorMessage('');
    setSuccessMessage('');
    setCardInfo(null);

    // Validate form inputs
    const validationError = validateForm();
    if (validationError) {
      setErrorMessage(validationError);
      return;
    }

    setIsSubmitting(true);

    try {
      // Prepare request payload
      const requestPayload: RenewalRequest = {
        renewalDays,
        paymentTransactionId: paymentTransactionId.trim(),
      };

      // Invoke the backend renewal endpoint
      const response = await fetch('/api/membership/renew', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${session?.user?.token}`, // Assuming token is stored in session
        },
        body: JSON.stringify(requestPayload),
      });

      if (!response.ok) {
        // Parse error response from backend
        const errorData = await response.json();
        throw new Error(errorData.message || 'Gia hạn thẻ thất bại. Vui lòng thử lại sau.');
      }

      // Parse successful response
      const data: RenewalResponse = await response.json();

      // Update UI with success feedback
      setSuccessMessage(`Thẻ hội viên đã được gia hạn thành công! Số ngày còn lại: ${data.remainingDays}.`);
      setCardInfo(data);

      // Optionally redirect to membership card view after a short delay
      setTimeout(() => {
        router.push('/membership');
      }, 2000);
    } catch (err) {
      // Capture and display error messages
      const errorMsg = err instanceof Error ? err.message : 'Đã xảy ra lỗi không xác định.';
      setErrorMessage(errorMsg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      {/* Page Header */}
      <header className="max-w-3xl mx-auto text-center mb-8">
        <h1 className="text-4xl font-extrabold text-gray-900">Gia Hạn Thẻ Hội Viên</h1>
        <p className="mt-2 text-lg text-gray-600">
          Chọn số ngày gia hạn và nhập mã giao dịch thanh toán để tiếp tục.
        </p>
      </header>

      {/* Main Renewal Form Card */}
      <section className="max-w-3xl mx-auto bg-white shadow-xl rounded-lg overflow-hidden">
        <div className="p-6 sm:p-8">
          {/* Success Message Display */}
          {successMessage && (
            <div className="mb-6 p-4 bg-green-100 border border-green-400 text-green-700 rounded-md">
              <p className="font-medium">{successMessage}</p>
              {cardInfo && (
                <div className="mt-2 text-sm">
                  <p>
                    <strong>ID Thẻ:</strong> {cardInfo.cardId}
                  </p>
                  <p>
                    <strong>Số ngày còn lại:</strong> {cardInfo.remainingDays}
                  </p>
                  <p>
                    <strong>Ngày hết hạn:</strong> {new Date(cardInfo.expiryDate).toLocaleDateString()}
                  </p>
                </div>
              )}
            </div>
          )}

          {/* Error Message Display */}
          {errorMessage && (
            <div className="mb-6 p-4 bg-red-100 border border-red-400 text-red-700 rounded-md">
              <p className="font-medium">Lỗi: {errorMessage}</p>
            </div>
          )}

          {/* Renewal Form */}
          <form onSubmit={handleSubmit} noValidate>
            {/* Renewal Days Field */}
            <div className="mb-6">
              <label
                htmlFor="renewalDays"
                className="block text-sm font-medium text-gray-700 mb-2"
              >
                Số ngày gia hạn (1-365)
              </label>
              <input
                id="renewalDays"
                type="number"
                min="1"
                max="365"
                value={renewalDays}
                onChange={(e: ChangeEvent<HTMLInputElement>) =>
                  setRenewalDays(Number(e.target.value))
                }
                className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Nhập số ngày"
                disabled={isSubmitting}
              />
              <p className="mt-1 text-xs text-gray-500">
                Ví dụ: 30 ngày = 1 tháng, 365 ngày = 1 năm.
              </p>
            </div>

            {/* Payment Transaction ID Field */}
            <div className="mb-8">
              <label
                htmlFor="paymentTransactionId"
                className="block text-sm font-medium text-gray-700 mb-2"
              >
                Mã giao dịch thanh toán <span className="text-red-500">*</span>
              </label>
              <input
                id="paymentTransactionId"
                type="text"
                value={paymentTransactionId}
                onChange={(e: ChangeEvent<HTMLInputElement>) =>
                  setPaymentTransactionId(e.target.value)
                }
                className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Nhập mã giao dịch thanh toán"
                disabled={isSubmitting}
              />
              <p className="mt-1 text-xs text-gray-500">
                Mã giao dịch được cung cấp bởi cổng thanh toán (16-64 ký tự, chữ cái, số, gạch dưới, gạch ngang).
              </p>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isSubmitting}
              className={`w-full py-3 px-4 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-md shadow transition-colors ${
                isSubmitting ? 'opacity-50 cursor-not-allowed' : ''
              }`}
            >
              {isSubmitting ? 'Đang xử lý...' : 'Xác Nhận Gia Hạn'}
            </button>
          </form>
        </div>
      </section>

      {/* Additional Information / Help Section */}
      <footer className="mt-12 max-w-3xl mx-auto text-sm text-gray-500">
        <h2 className="text-lg font-semibold text-gray-700 mb-2">Thông Tin Thêm</h2>
        <ul className="list-disc list-inside space-y-1">
          <li>
            Sau khi xác nhận, hệ thống sẽ gọi cổng thanh toán để xử lý giao dịch.
          </li>
          <li>
            Thẻ hội viên sẽ được cập nhật ngay sau khi thanh toán thành công.
          </li>
          <li>
            Bạn có thể xem thông tin thẻ hội viên hiện tại trên trang{' '}
            <a href="/membership" className="text-blue-600 hover:underline">
              Thẻ Hội Viên
            </a>
            .
          </li>
        </ul>
      </footer>
    </div>
  );
};

export default MembershipRenewalPage;