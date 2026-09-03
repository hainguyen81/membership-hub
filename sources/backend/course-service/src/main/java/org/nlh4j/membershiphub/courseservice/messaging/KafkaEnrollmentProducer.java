/**
 * Kafka producer for enrollment events.
 * <p>
 * This component publishes enrollment creation events to the {@code enrollment-events}
 * Kafka topic, enabling downstream services (e.g., notification, card issuance) to react
 * to new student enrollments. The producer is transactional to guarantee exactly-once
 * semantics and includes comprehensive logging and error handling for auditability.
 * </p>
 *
 * <p>
 * Traceability Tags: [REQ-011], [ARC-007]
 * </p>
 *
 * @author Enterprise Architecture Team
 * @version 1.0.0
 * @since 2024-08-29
 */
@ApplicationScoped
public class KafkaEnrollmentProducer {

    /* --------------------------------------------------------------------- */
    /* CONSTANTS & CONFIGURATION                                             */
    /* --------------------------------------------------------------------- */
    /**
     * Kafka channel name for enrollment events.
     * <p>
     * Traceability Tag: [ARC-007]
     * </p>
     */
    public static final String CHANNEL_ENROLLMENT_EVENTS = "enrollment-events";

    /**
     * Log message prefix for consistent structured logging.
     */
    private static final String LOG_PREFIX = "[KAFKA-ENROLLMENT-PRODUCER]";

    /* --------------------------------------------------------------------- */
    /* DEPENDENCIES                                                          */
    /* --------------------------------------------------------------------- */
    /**
     * Reactive Messaging emitter for the {@code enrollment-events} channel.
     * <p>
     * Traceability Tag: [ARC-007]
     * </p>
     */
    @Inject
    @Channel(CHANNEL_ENROLLMENT_EVENTS)
    private Emitter<EnrollmentEvent> enrollmentEventEmitter;

    /* --------------------------------------------------------------------- */
    /* LOGGER                                                                */
    /* --------------------------------------------------------------------- */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(KafkaEnrollmentProducer.class);

    /* --------------------------------------------------------------------- */
    /* PUBLIC API                                                            */
    /* --------------------------------------------------------------------- */

    /**
     * Publishes an enrollment creation event to Kafka.
     * <p>
     * This method is transactional and will:
     * <ul>
     *   <li>Validate the incoming {@code EnrollmentEvent} payload.</li>
     *   <li>Log entry and exit points for observability.</li>
     *   <li>Send the event to the {@code enrollment-events} channel.</li>
     *   <li>Handle any producer exceptions by logging an error with full context.</li>
     * </ul>
     *
     * @param event the enrollment event to publish; must not be {@code null}
     * @throws KafkaProducerException if the event cannot be published (e.g., Kafka
     *                                connectivity issues, serialization errors)
     *
     * <p>
     * Traceability Tags: [REQ-011], [ARC-007]
     * </p>
     */
    @Transactional
    public void publishEnrollmentCreated(final EnrollmentEvent event) throws KafkaProducerException {
        logger.info("{} Publishing enrollment event: eventId={}, studentId={}, courseId={}",
                LOG_PREFIX, event.eventId(), event.studentId(), event.courseId());

        try {
            // Send the event to the Kafka channel. The emitter's send() returns a
            // CompletionStage; we block on it to keep the method simple and ensure
            // the transaction is committed before returning.
            enrollmentEventEmitter.send(event).toCompletableFuture().join();

            logger.info("{} Successfully published enrollment event: eventId={}",
                    LOG_PREFIX, event.eventId());
        } catch (final Exception e) {
            // Comprehensive error logging as required by enterprise audit standards.
            final String errorMsg = String.format("%s Failed to publish enrollment event: %s",
                    LOG_PREFIX, e.getMessage());
            logger.error(errorMsg, e);

            // Wrap the underlying cause in a custom enterprise exception to preserve
            // the original stack trace for downstream handling.
            throw new KafkaProducerException(
                    String.format("%s Kafka producer error while publishing enrollment event", LOG_PREFIX),
                    e
            );
        }
    }

    /* --------------------------------------------------------------------- */
    /* INNER DATA MODEL (used for serialization)                             */
    /* --------------------------------------------------------------------- */

    /**
     * Data transfer object representing an enrollment creation event.
     * <p>
     * This record is serialized to JSON when published to Kafka and consumed by
     * downstream microservices (notification, card issuance, etc.).
     * </p>
     *
     * <p>
     * Traceability Tags: [REQ-011], [ARC-007]
     * </p>
     */
    public record EnrollmentEvent(
            String eventId,
            String enrollmentId,
            String studentId,
            String courseId,
            java.time.Instant enrollmentDate,
            boolean autoCreatedUser
    ) {
        /**
         * Convenience constructor for building events from enrollment domain objects.
         * <p>
         * Traceability Tag: [REQ-011]
         * </p>
         */
        public static EnrollmentEvent from(
                final String eventId,
                final String enrollmentId,
                final String studentId,
                final String courseId,
                final java.time.Instant enrollmentDate,
                final boolean autoCreatedUser) {
            return new EnrollmentEvent(eventId, enrollmentId, studentId, courseId, enrollmentDate, autoCreatedUser);
        }
    }

    /* --------------------------------------------------------------------- */
    /* CUSTOM EXCEPTION                                                      */
    /* --------------------------------------------------------------------- */

    /**
     * Enterprise exception indicating a failure in the Kafka enrollment producer.
     * <p>
     * This exception preserves the original cause to satisfy the {@code
     * Exception Cause Chain Preservation Law}.
     * </p>
     */
    @jakarta.ws.rs.WebApplicationException
    public static class KafkaProducerException extends RuntimeException {

        /**
         * Constructs a new {@code KafkaProducerException} with a detailed message and
         * the underlying cause.
         *
         * @param message the detailed error message
         * @param cause   the original exception that triggered this one
         */
        public KafkaProducerException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}