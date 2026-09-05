package org.nlh4j.saas.membership-hub.membership;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.quarkus.scheduler.Scheduled;

/**
 * Service responsible for managing membership cards.
 *
 * <p>This service calculates the remaining validity days for each membership card
 * based on {@code issueDate} and {@code validityDays}. A scheduled job runs
 * daily to update the {@code remainingDays} field automatically.</p>
 *
 * @traceability [REQ-014], [DAT-007]
 */
@ApplicationScoped
public class MembershipService {

    /* --------------------------------------------------------------------- */
    /*  Constants (no hard‑coded literals in business logic)                 */
    /* --------------------------------------------------------------------- */
    /** Cron expression for the daily scheduled job (02:00 UTC). */
    private static final String DAILY_CRON = "0 0 2 * * ?";

    /** Default timezone used for date calculations. */
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    /** Minimum remaining days allowed (non‑negative). */
    private static final int MIN_REMAINING_DAYS = 0;

    /* --------------------------------------------------------------------- */
    /*  Dependencies                                                        */
    /* --------------------------------------------------------------------- */
    @Inject
    Logger logger; // injected via CDI

    @Inject
    EntityManager em; // JPA EntityManager for persistence operations

    /* --------------------------------------------------------------------- */
    /*  Public API                                                          */
    /* --------------------------------------------------------------------- */

    /**
     * Calculates and returns the remaining validity days for a specific card.
     *
     * @param cardId the unique identifier of the membership card
     * @return the number of days remaining, never negative
     * @throws IllegalArgumentException if the card does not exist
     */
    @Transactional
    public int getRemainingDays(UUID cardId) {
        logger.debug("[PROCESS] Calculating remaining days for card: {}", cardId);

        StudentCard card = em.find(StudentCard.class, cardId);
        if (card == null) {
            logger.error("[ERROR] Card not found: {}", cardId);
            throw new IllegalArgumentException("Card not found: " + cardId);
        }

        int remaining = computeRemainingDays(card.getIssueDate(), card.getValidityDays());
        logger.debug("[RESULT] Remaining days for card {}: {}", cardId, remaining);
        return remaining;
    }

    /**
     * Updates the {@code remainingDays} field for all membership cards.
     *
     * <p>This method is invoked by the scheduled job defined by {@link #DAILY_CRON}.</p>
     */
    @Transactional
    @Scheduled(cron = DAILY_CRON)
    public void updateAllRemainingDays() {
        logger.info("[SCHEDULED] Updating remaining days for all cards at {}", LocalDate.now(DEFAULT_ZONE));

        try {
            List<StudentCard> cards = em.createQuery(
                    "SELECT c FROM StudentCard c", StudentCard.class)
                    .getResultList();

            for (StudentCard card : cards) {
                int newRemaining = computeRemainingDays(card.getIssueDate(), card.getValidityDays());
                card.setRemainingDays(newRemaining);
                em.merge(card);
                logger.debug("[UPDATE] Card {}: remainingDays set to {}", card.getCardId(), newRemaining);
            }
        } catch (PersistenceException e) {
            logger.error("[CRITICAL FAIL] [ARC-014] Failed to update remaining days: {}", e.getMessage(), e);
            // Rethrow to let the scheduler handle retry if configured
            throw e;
        }

        logger.info("[SCHEDULED] Completed updating remaining days for all cards");
    }

    /* --------------------------------------------------------------------- */
    /*  Helper Methods                                                      */
    /* --------------------------------------------------------------------- */

    /**
     * Computes the remaining days given the issue date and validity period.
     *
     * @param issueDate    the date the card was issued
     * @param validityDays the total number of days the card is valid
     * @return the remaining days, never negative
     */
    private int computeRemainingDays(LocalDate issueDate, int validityDays) {
        // Guard against null dates
        if (issueDate == null) {
            logger.warn("[WARN] Issue date is null; defaulting remaining days to {}", MIN_REMAINING_DAYS);
            return MIN_REMAINING_DAYS;
        }

        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        Period period = Period.between(issueDate, today);
        int daysElapsed = period.getDays() + period.getMonths() * 30 + period.getYears() * 365;

        int remaining = validityDays - daysElapsed;
        return Math.max(remaining, MIN_REMAINING_DAYS);
    }
}

/* --------------------------------------------------------------------- */
/*  Entity definition (simplified for illustration)                      */
/* --------------------------------------------------------------------- */
import java.time.LocalDate;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * JPA entity representing a membership card.
 *
 * @traceability [DAT-007]
 */
@Entity
@Table(name = "student_cards")
class StudentCard {

    @Id
    @Column(name = "card_id", nullable = false, updatable = false)
    private UUID cardId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "validity_days", nullable = false)
    private int validityDays;

    @Column(name = "remaining_days", nullable = false)
    private int remainingDays;

    // Getters and setters omitted for brevity

    public UUID getCardId() { return cardId; }
    public void setCardId(UUID cardId) { this.cardId = cardId; }

    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public int getValidityDays() { return validityDays; }
    public void setValidityDays(int validityDays) { this.validityDays = validityDays; }

    public int getRemainingDays() { return remainingDays; }
    public void setRemainingDays(int remainingDays) { this.remainingDays = remainingDays; }
}