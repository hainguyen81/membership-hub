typescript
// Traceability Tags: [REQ-014]
// This component implements the digital membership card display functionality as per requirement REQ-014.
// It fetches the student's membership card details from the backend API, handles loading, error, and success states,
// and presents the card information in a responsive, accessible UI. The implementation follows OWASP security best
// practices (input validation, authentication checks, safe API calls) and includes comprehensive logging for audit
// and monitoring purposes.

import React, { useState, useEffect, useCallback } from 'react';
// Next.js App Router page component; uses NextAuth for session management and a custom logger for structured logging.
import { useSession } from 'next-auth/react';
import { logger } from '@/lib/logger';

/**
 * MembershipCard interface defines the shape of the membership card data returned by the API.
 * All fields are required to ensure type safety and validation before rendering.
 */
interface MembershipCard {
  cardId: string;
  studentId: string;
  issueDate: string;
  validityDays: number;
  remainingDays: number;
  expiryDate: string;
}

/**
 * MembershipPage component renders the digital membership card for the authenticated student.
 * It performs authentication checks, validates input, logs all critical actions, and gracefully handles errors.
 */
export default function MembershipPage() {
  // Entry log for component mount/unmount lifecycle
  useEffect(() => {
    logger.info('[ENTRY] MembershipPage component mounted');
    return () => {
      logger.info('[EXIT] MembershipPage component unmounted');
    };
  }, []);

  // State management for membership card data, loading, and error conditions
  const [card, setCard] = useState<MembershipCard | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const { data: session } = useSession();

  /**
   * fetchMembershipCard is a memoized function that:
   *   - Validates the session and user ID.
   *   - Performs OWASP‑style input validation (UUID format).
   *   - Calls the API endpoint `/api/membership/card` with proper authentication headers.
   *   - Logs all HTTP interactions for auditability.
   *   - Handles both HTTP errors and unexpected exceptions with detailed logging.
   */
  const fetchMembershipCard = useCallback(async () => {
    // Security check: ensure the user is authenticated
    if (!session?.user?.id) {
      logger.warn('[SECURITY] MembershipPage fetch attempted without a valid session');
      setError('Unauthorized access');
      return;
    }

    try {
      const userId = session.user.id;

      // OWASP Input Validation: ensure userId conforms to UUID format
      const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
      if (!uuidRegex.test(userId)) {
        logger.error(`[VALIDATION] Invalid userId format received: ${userId}`);
        setError('Invalid user identifier');
        return;
      }

      // Log the API call for audit trail (traceability)
      logger.info(`[API_CALL] GET /api/membership/card?userId=${userId}`);

      // Perform the API request with authentication and proper error handling
      const response = await fetch(`/api/membership/card?userId=${encodeURIComponent(userId)}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          // Include the JWT token if the API requires it
          'Authorization': `Bearer ${session.accessToken}`,
        },
        credentials: 'same-origin', // send cookies if needed
      });

      // Log HTTP status for monitoring and debugging
      logger.debug(`[HTTP] Response status: ${response.status}`);

      if (!response.ok) {
        // Capture error details for logging and user feedback
        const errorText = await response.text();
        logger.error(`[HTTP_ERROR] Failed to fetch membership card: ${response.status} ${errorText}`);
        setError(`Failed to load membership card (HTTP ${response.status})`);
        return;
      }

      // Parse JSON response and validate schema
      const data: MembershipCard = await response.json();

      // Basic schema validation to ensure data integrity
      if (!data.cardId || typeof data.remainingDays !== 'number') {
        logger.error(`[VALIDATION] Received malformed membership card data: ${JSON.stringify(data)}`);
        setError('Invalid data format received from server');
        return;
      }

      // Success path: update state and log success
      logger.info(`[SUCCESS] Membership card fetched successfully. Card ID: ${data.cardId}`);
      setCard(data);
      setError(null);
    } catch (err) {
      // Comprehensive exception logging per enterprise guidelines
      const errorMessage = err instanceof Error ? err.message : String(err);
      logger.error(`[CRITICAL_FAIL] [REQ-014] Membership card fetch failed due to unexpected error. Raw error: ${errorMessage}`);
      setError('An unexpected error occurred while loading membership card');
    } finally {
      setLoading(false);
    }
  }, [session]);

  // Trigger data fetch on component mount or when session changes
  useEffect(() => {
    fetchMembershipCard();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fetchMembershipCard]);

  // Render loading spinner while data is being fetched
  if (loading) {
    logger.debug('[RENDER] Displaying loading spinner');
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-50">
        <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-b-4 border-blue-500" />
        <span className="ml-3 text-gray-700">Loading membership card...</span>
      </div>
    );
  }

  // Render error UI with a retry button
  if (error) {
    logger.debug(`[RENDER] Displaying error UI: ${error}`);
    return (
      <div className="flex flex-col items-center justify-center min-h-screen bg-red-50 p-6">
        <h2 className="text-2xl font-semibold text-red-700 mb-4">Error Loading Membership Card</h2>
        <p className="text-red-600 mb-6">{error}</p>
        <button
          onClick={fetchMembershipCard}
          className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 transition-colors"
        >
          Retry
        </button>
      </div>
    );
  }

  // Render the membership card details in a responsive grid
  logger.debug('[RENDER] Displaying membership card details');
  return (
    <main className="max-w-2xl mx-auto p-6 bg-white shadow-lg rounded-lg">
      <h1 className="text-3xl font-bold mb-6 text-gray-800">Digital Membership Card</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {/* Card ID */}
        <div className="bg-gray-100 p-4 rounded">
          <p className="text-sm text-gray-500">Card ID</p>
          <p className="font-mono text-gray-800">{card.cardId}</p>
        </div>
        {/* Student ID */}
        <div className="bg-gray-100 p-4 rounded">
          <p className="text-sm text-gray-500">Student ID</p>
          <p className="font-mono text-gray-800">{card.studentId}</p>
        </div>
        {/* Issue Date */}
        <div className="bg-gray-100 p-4 rounded">
          <p className="text-sm text-gray-500">Issue Date</p>
          <p className="text-gray-800">{new Date(card.issueDate).toLocaleDateString()}</p>
        </div>
        {/* Validity Days */}
        <div className="bg-gray-100 p-4 rounded">
          <p className="text-sm text-gray-500">Validity (Days)</p>
          <p className="text-gray-800">{card.validityDays}</p>
        </div>
        {/* Remaining Days (highlighted if expired) */}
        <div className="bg-gray-100 p-4 rounded">
          <p className="text-sm text-gray-500">Remaining Days</p>
          <p className={`text-2xl font-semibold ${card.remainingDays < 0 ? 'text-red-600' : 'text-green-600'}`}>
            {card.remainingDays < 0 ? 'Expired' : card.remainingDays}
          </p>
        </div>
        {/* Expiry Date */}
        <div className="bg-gray-100 p-4 rounded">
          <p className="text-sm text-gray-500">Expiry Date</p>
          <p className="text-gray-800">{new Date(card.expiryDate).toLocaleDateString()}</p>
        </div>
      </div>

      {/* QR Code placeholder – in production integrate a QR code library (e.g., qrcode.react) */}
      <div className="mt-8">
        <h2 className="text-xl font-semibold mb-2 text-gray-700">QR Code Placeholder</h2>
        <div className="w-48 h-48 bg-gray-200 flex items-center justify-center text-gray-500">
          QR Code (Card ID)
        </div>
      </div>
    </main>
  );
}