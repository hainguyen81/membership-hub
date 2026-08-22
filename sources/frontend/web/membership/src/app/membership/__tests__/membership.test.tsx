typescript
/**
 * membership-hub Phase 3 Frontend Integration Test Suite
 * 
 * Test Coverage Tags: [REQ-014], [REQ-015], [REQ-017], [REQ-018]
 * Target Component: ./sources/frontend/web/membership/src/app/membership/__tests__/membership.test.tsx
 * 
 * This suite validates the integration of Phase 3 frontend components:
 * - Membership card display with remaining days calculation [REQ-014]
 * - Membership renewal workflow with payment integration [REQ-015]
 * - Promotions listing with active status filtering [REQ-017]
 * - Announcements display with expiration auto-hide [REQ-018]
 * 
 * Architecture Context: Next.js App Router, React Query, Firebase Auth
 * Testing Strategy: Integration tests with mocked API layer, full provider bootstrap
 */

import React from 'react';
import { render, screen, waitFor, fireEvent, act } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '@/contexts/AuthContext';
import MembershipCardPage from '@/app/membership/page';
import RenewMembershipPage from '@/app/membership/renew/page';
import PromotionsPage from '@/app/promotions/page';
import AnnouncementsPage from '@/app/announcements/page';
import { membershipApi, promotionApi, announcementApi } from '@/services/api';

// ============================================================================
// [CONST-001] MOCK DATASETS - Isolated top-of-class constants per enterprise policy
// ============================================================================

/**
 * Mock membership card response for happy path testing [REQ-014]
 * Validates: remaining_days calculation, expiry_date display, card status
 */
const MOCK_MEMBERSHIP_CARD_ACTIVE = {
  cardId: '123e4567-e89b-12d3-a456-426614174000',
  studentId: '123e4567-e89b-12d3-a456-426614174001',
  issueDate: '2024-01-01',
  validityDays: 30,
  remainingDays: 15,
  expiryDate: '2024-01-31',
};

/**
 * Mock expired membership card for edge case testing [REQ-014]
 * Validates: zero remaining days handling, expired status UI
 */
const MOCK_MEMBERSHIP_CARD_EXPIRED = {
  cardId: '123e4567-e89b-12d3-a456-426614174000',
  studentId: '123e4567-e89b-12d3-a456-426614174001',
  issueDate: '2024-01-01',
  validityDays: 30,
  remainingDays: 0,
  expiryDate: '2024-01-31',
};

/**
 * Mock successful renewal response [REQ-015]
 * Validates: remaining_days update, expiry_date extension, confirmation UI
 */
const MOCK_RENEWAL_SUCCESS = {
  cardId: '123e4567-e89b-12d3-a456-426614174000',
  remainingDays: 45,
  expiryDate: '2024-03-15',
};

/**
 * Mock active promotions for happy path testing [REQ-017]
 * Validates: promotion code display, discount percentage, date range filtering
 */
const MOCK_PROMOTIONS_ACTIVE = [
  {
    promoId: '123e4567-e89b-12d3-a456-426614174000',
    code: 'SUMMER10',
    discountPercent: 10,
    startDate: '2024-06-01',
    endDate: '2024-08-31',
    description: 'Giảm 10% khóa học hè',
  },
];

/**
 * Mock expired promotions for edge case testing [REQ-017]
 * Validates: expired promotions are filtered out from UI
 */
const MOCK_PROMOTIONS_EXPIRED = [
  {
    promoId: '123e4567-e89b-12d3-a456-426614174000',
    code: 'EXPIRED',
    discountPercent: 20,
    startDate: '2024-01-01',
    endDate: '2024-01-31',
    description: 'Expired promo',
  },
];

/**
 * Mock active announcements for happy path testing [REQ-018]
 * Validates: announcement title/content display, date range filtering
 */
const MOCK_ANNOUNCEMENTS_ACTIVE = [
  {
    announcementId: '123e4567-e89b-12d3-a456-426614174000',
    title: 'Thông báo nghỉ lễ',
    content: 'Trung tâm nghỉ lễ 30/4',
    startDate: '2024-04-29',
    endDate: '2024-05-01',
  },
];

/**
 * Mock expired announcements for edge case testing [REQ-018]
 * Validates: expired announcements auto-hide from UI
 */
const MOCK_ANNOUNCEMENTS_EXPIRED = [
  {
    announcementId: '123e4567-e89b-12d3-a456-426614174000',
    title: 'Old Announcement',
    content: 'This should be hidden',
    startDate: '2024-01-01',
    endDate: '2024-01-31',
  },
];

// ============================================================================
// [MOCK-001] API SERVICE MOCKS - Isolated mock implementations
// ============================================================================

jest.mock('@/services/api', () => ({
  membershipApi: {
    getCard: jest.fn(),
    renewCard: jest.fn(),
  },
  promotionApi: {
    getPromotions: jest.fn(),
  },
  announcementApi: {
    getAnnouncements: jest.fn(),
  },
}));

// ============================================================================
// [HELPER-001] TEST WRAPPER FACTORY - Bootstraps full runtime infrastructure
// ============================================================================

/**
 * Creates a test wrapper with all required providers for Phase 3 components
 * Includes: QueryClient (React Query), AuthProvider (Firebase Auth context)
 * This ensures tests run with full runtime infrastructure context [REQ-014][REQ-015][REQ-017][REQ-018]
 */
const createPhase3TestWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        refetchOnWindowFocus: false,
        staleTime: Infinity,
      },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        {children}
      </AuthProvider>
    </QueryClientProvider>
  );
};

// ============================================================================
// [TEST-SUITE-001] PHASE 3 FRONTEND INTEGRATION TEST SUITE
// Coverage: [REQ-014], [REQ-015], [REQ-017], [REQ-018]
// ============================================================================

describe('Phase 3 Membership Frontend Integration [REQ-014][REQ-015][REQ-017][REQ-018]', () => {
  // [SETUP-001] Clear all mocks before each test to ensure test isolation
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ========================================================================
  // [REQ-014] MEMBERSHIP CARD DISPLAY COMPONENT TESTS
  // ========================================================================
  describe('Membership Card Display Component [REQ-014]', () => {
    test('renders active membership card with correct remaining days [REQ-014]', async () => {
      // [REQ-014] Happy Path: Digital membership card displays remaining validity days
      // Business Logic: Card with 15 remaining days should display "15" and expiry date
      (membershipApi.getCard as jest.Mock).mockResolvedValue(MOCK_MEMBERSHIP_CARD_ACTIVE);

      render(<MembershipCardPage />, { wrapper: createPhase3TestWrapper() });

      // Assert: Verify remaining days calculation and display
      await waitFor(() => {
        expect(screen.getByText(/remaining days/i)).toBeInTheDocument();
        expect(screen.getByText('15')).toBeInTheDocument();
        expect(screen.getByText(/expiry date/i)).toBeInTheDocument();
      });
    });

    test('handles expired membership card with zero remaining days [REQ-014]', async () => {
      // [REQ-014] Edge Case: Card with 0 remaining days shows expired status
      // Business Logic: remaining_days = 0 triggers expired UI state
      (membershipApi.getCard as jest.Mock).mockResolvedValue(MOCK_MEMBERSHIP_CARD_EXPIRED);

      render(<MembershipCardPage />, { wrapper: createPhase3TestWrapper() });

      // Assert: Verify expired status badge and zero days display
      await waitFor(() => {
        expect(screen.getByText(/expired/i)).toBeInTheDocument();
        expect(screen.getByText('0')).toBeInTheDocument();
      });
    });

    test('handles network error when fetching membership card [REQ-014]', async () => {
      // [REQ-014] Exception Case: API network failure handling
      // Business Logic: Network error should display user-friendly error message
      (membershipApi.getCard as jest.Mock).mockRejectedValue(new Error('Network connection failed'));

      render(<MembershipCardPage />, { wrapper: createPhase3TestWrapper() });

      // Assert: Verify error boundary catches and displays error state
      await waitFor(() => {
        expect(screen.getByText(/failed to load membership card/i)).toBeInTheDocument();
        expect(screen.getByRole('alert')).toBeInTheDocument();
      });
    });
  });

  // ========================================================================
  // [REQ-015] MEMBERSHIP RENEWAL COMPONENT TESTS
  // ========================================================================
  describe('Membership Renewal Component [REQ-015]', () => {
    test('successfully renews membership with valid payment transaction [REQ-015]', async () => {
      // [REQ-015] Happy Path: Renewal with payment integration
      // Business Logic: Payment success updates remaining_days and expiry_date
      (membershipApi.renewCard as jest.Mock).mockResolvedValue(MOCK_RENEWAL_SUCCESS);

      render(<RenewMembershipPage />, { wrapper: createPhase3TestWrapper() });

      // Act: Simulate user entering renewal days and submitting payment
      const daysInput = screen.getByLabelText(/renewal days/i);
      fireEvent.change(daysInput, { target: { value: '30' } });

      const submitButton = screen.getByRole('button', { name: /confirm renewal/i });
      fireEvent.click(submitButton);

      // Assert: Verify API call with correct payload and success UI update
      await waitFor(() => {
        expect(membershipApi.renewCard).toHaveBeenCalledWith({
          renewalDays: 30,
          paymentTransactionId: expect.any(String),
        });
        expect(screen.getByText(/renewal successful/i)).toBeInTheDocument();
        expect(screen.getByText('45')).toBeInTheDocument();
      });
    });

    test('handles payment gateway failure during renewal [REQ-015]', async () => {
      // [REQ-015] Exception Case: Payment failure (HTTP 402)
      // Business Logic: Payment failure prevents card update, shows error
      (membershipApi.renewCard as jest.Mock).mockRejectedValue({
        error: 'Payment failed',
        status: 402,
      });

      render(<RenewMembershipPage />, { wrapper: createPhase3TestWrapper() });

      // Act: Attempt renewal with failing payment
      const submitButton = screen.getByRole('button', { name: /confirm renewal/i });
      fireEvent.click(submitButton);

      // Assert: Verify error message and no state change
      await waitFor(() => {
        expect(screen.getByText(/payment failed/i)).toBeInTheDocument();
        expect(screen.getByRole('alert')).toBeInTheDocument();
      });
    });

    test('validates renewal days input constraints [REQ-015]', async () => {
      // [REQ-015] Edge Case: Invalid renewal days (negative value)
      // Business Logic: Form validation prevents submission with invalid days
      render(<RenewMembershipPage />, { wrapper: createPhase3TestWrapper() });

      // Act: Enter invalid negative days
      const daysInput = screen.getByLabelText(/renewal days/i);
      fireEvent.change(daysInput, { target: { value: '-5' } });

      const submitButton = screen.getByRole('button', { name: /confirm renewal/i });
      fireEvent.click(submitButton);

      // Assert: Validation error shown, API not called
      await waitFor(() => {
        expect(screen.getByText(/invalid number of days/i)).toBeInTheDocument();
        expect(membershipApi.renewCard).not.toHaveBeenCalled();
      });
    });
  });

  // ========================================================================
  // [REQ-017] PROMOTIONS MANAGEMENT COMPONENT TESTS
  // ========================================================================
  describe('Promotions Management Component [REQ-017]', () => {
    test('displays active promotions list with correct details [REQ-017]', async () => {
      // [REQ-017] Happy Path: Active promotions displayed with all details
      // Business Logic: Promotions within date range are visible to students
      (promotionApi.getPromotions as jest.Mock).mockResolvedValue(MOCK_PROMOTIONS_ACTIVE);

      render(<PromotionsPage />, { wrapper: createPhase3TestWrapper() });

      // Assert: Verify promotion code, discount, and description are rendered
      await waitFor(() => {
        expect(screen.getByText('SUMMER10')).toBeInTheDocument();
        expect(screen.getByText(/10%/i)).toBeInTheDocument();
        expect(screen.getByText(/Giảm 10% khóa học hè/i)).toBeInTheDocument();
      });
    });

    test('filters out expired promotions from display [REQ-017]', async () => {
      // [REQ-017] Edge Case: Expired promotions filtered by date range
      // Business Logic: end_date < CURRENT_DATE promotions are excluded
      (promotionApi.getPromotions as jest.Mock).mockResolvedValue(MOCK_PROMOTIONS_EXPIRED);

      render(<PromotionsPage />, { wrapper: createPhase3TestWrapper() });

      // Assert: Verify expired promotion is not displayed, empty state shown
      await waitFor(() => {
        expect(screen.queryByText('EXPIRED')).not.toBeInTheDocument();
        expect(screen.getByText(/no active promotions/i)).toBeInTheDocument();
      });
    });

    test('handles API error when fetching promotions [REQ-017]', async () => {
      // [REQ-017] Exception Case: API failure handling
      // Business Logic: Network error shows fallback error message
      (promotionApi.getPromotions as jest.Mock).mockRejectedValue(new Error('API Error'));

      render(<PromotionsPage />, { wrapper: createPhase3TestWrapper() });

      // Assert: Verify error state is rendered
      await waitFor(() => {
        expect(screen.getByText(/failed to load promotions/i)).toBeInTheDocument();
      });
    });
  });

  // ========================================================================
  // [REQ-018] ANNOUNCEMENTS MANAGEMENT COMPONENT TESTS
  // ========================================================================
  describe('Announcements Management Component [REQ-018]', () => {
    test('displays active announcements with title and content [REQ-018]', async () => {
      // [REQ-018] Happy Path: Active announcements displayed correctly
      // Business Logic: Announcements within validity period are visible
      (announcementApi.getAnnouncements as jest.Mock).mockResolvedValue(MOCK_ANNOUNCEMENTS_ACTIVE);

      render(<AnnouncementsPage />, { wrapper: createPhase3TestWrapper() });

      // Assert: Verify announcement title and content are rendered
      await waitFor(() => {
        expect(screen.getByText('Thông báo nghỉ lễ')).toBeInTheDocument();
        expect(screen.getByText('Trung tâm nghỉ lễ 30/4')).toBeInTheDocument();
      });
    });

    test('hides announcements after expiration date [REQ-018]', async () => {
      // [REQ-018] Edge Case: Expired announcements auto-hidden
      // Business Logic: end_date < CURRENT_DATE announcements are filtered out
      (announcementApi.getAnnouncements as jest.Mock).mockResolvedValue(MOCK_ANNOUNCEMENTS_EXPIRED);

      render(<AnnouncementsPage />, { wrapper: createPhase3TestWrapper() });

      // Assert: Verify expired announcement is not visible
      await waitFor(() => {
        expect(screen.queryByText('Old Announcement')).not.toBeInTheDocument();
        expect(screen.getByText(/no active announcements/i)).toBeInTheDocument();
      });
    });

    test('handles API error when fetching announcements [REQ-018]', async () => {
      // [REQ-018] Exception Case: API failure handling
      // Business Logic: Network error displays user-friendly error message
      (announcementApi.getAnnouncements as jest.Mock).mockRejectedValue(new Error('API Error'));

      render(<AnnouncementsPage />, { wrapper: createPhase3TestWrapper() });

      // Assert: Verify error state is displayed
      await waitFor(() => {
        expect(screen.getByText(/failed to load announcements/i)).toBeInTheDocument();
      });
    });
  });
});