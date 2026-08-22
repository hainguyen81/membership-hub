package org.nlh4j.saas.membership_hub.notification;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Core notification service responsible for sending push notifications via Firebase Cloud Messaging (FCM).
 *
 * <p>Traceability: {@code [REQ-016]}, {@code [EXC-003]}</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Build FCM payloads from {@link UserNotification} objects.</li>
 *   <li>Send HTTP POST requests to the FCM endpoint.</li>
 *   <li>Retry failed sends up to {@link #MAX_RETRY_COUNT} times with a delay of {@link #RETRY_DELAY_MINUTES} minutes.</li>
 *   <li>Log entry, exit, success, and error events with sensitive data masked.</li>
 *   <li>Throw {@link NotificationException} on unrecoverable failures, preserving the original cause.</li>
 * </ul>
 */
@ApplicationScoped
public class NotificationService {

    /* --------------------------------------------------------------------- */
    /*  Constants (no hard‑coded literals in business logic)                 */
    /* --------------------------------------------------------------------- */
    private static final String FCM_ENDPOINT_TEMPLATE = "https://fcm.googleapis.com/v1/projects/%s/messages:send";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final String MESSAGE_KEY = "message";
    private static final String NOTIFICATION_KEY = "notification";
    private static final String TITLE_KEY = "title";
    private static final String BODY_KEY = "body";
    private static final String DATA_KEY = "data";
    private static final String TOKEN_KEY = "token";

    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MINUTES = 5;
    private static final long RETRY_DELAY_MILLIS = TimeUnit.MINUTES.toMillis(RETRY_DELAY_MINUTES);

    private static final String LOG_ENTRY = "Processing notification for token: %s";
    private static final String LOG_EXIT = "Completed notification processing for token: %s";
    private static final String LOG_SUCCESS = "Notification sent successfully for token: %s";
    private static final String LOG_ERROR = "Notification failed for token: %s, attempt: %d, error: %s";
    private static final String LOG_RETRY = "Retrying notification for token: %s, attempt: %d";

    /* --------------------------------------------------------------------- */
    /*  Dependencies                                                     */
    /* --------------------------------------------------------------------- */
    @Inject
    private Logger logger; // CDI logger

    @Inject
    private Client httpClient; // JAX-RS client

    @ConfigProperty(name = "fcm.server.key")
    private String fcmServerKey; // FCM server key (kept secret)

    @ConfigProperty(name = "fcm.project.id")
    private String fcmProjectId; // FCM project ID

    @ConfigProperty(name = "fcm.endpoint.template", defaultValue = FCM_ENDPOINT_TEMPLATE)
    private String fcmEndpointTemplate; // Allows override via config

    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON serializer

    /* --------------------------------------------------------------------- */
    /*  Public API                                                        */
    /* --------------------------------------------------------------------- */

    /**
     * Asynchronously sends a push notification.
     *
     * @param notification the notification payload
     * @return a {@link CompletableFuture} that completes when the send operation finishes
     *
     * @traceability [REQ-016], [EXC-003]
     */
    public CompletableFuture<Void> sendNotification(UserNotification notification) {
        // Offload the potentially blocking HTTP call to a worker thread
        return CompletableFuture.runAsync(() -> {
            logger.infof(LOG_ENTRY, maskToken(notification.getToken()));
            try {
                sendFCM(notification);
                logger.infof(LOG_SUCCESS, maskToken(notification.getToken()));
            } catch (NotificationException ex) {
                // Already logged inside sendFCM; rethrow to propagate failure
                throw ex;
            } finally {
                logger.infof(LOG_EXIT, maskToken(notification.getToken()));
            }
        });
    }

    /* --------------------------------------------------------------------- */
    /*  Internal helpers                                                 */
    /* --------------------------------------------------------------------- */

    /**
     * Sends the notification via FCM, retrying on transient failures.
     *
     * @param notification the notification payload
     * @throws NotificationException if all retry attempts fail
     *
     * @traceability [REQ-016], [EXC-003]
     */
    private void sendFCM(UserNotification notification) {
        int attempt = 0;
        while (attempt < MAX_RETRY_COUNT) {
            attempt++;
            try {
                // Build the full endpoint URL
                String endpoint = String.format(fcmEndpointTemplate, fcmProjectId);

                // Construct the JSON payload
                ObjectNode payload = objectMapper.createObjectNode();
                ObjectNode messageNode = payload.putObject(MESSAGE_KEY);
                ObjectNode notificationNode = messageNode.putObject(NOTIFICATION_KEY);
                notificationNode.put(TITLE_KEY, notification.getTitle());
                notificationNode.put(BODY_KEY, notification.getBody());
                if (notification.getData() != null && !notification.getData().isEmpty()) {
                    ObjectNode dataNode = messageNode.putObject(DATA_KEY);
                    notification.getData().forEach(dataNode::put);
                }
                messageNode.put(TOKEN_KEY, notification.getToken());

                // Prepare the HTTP request
                WebTarget target = httpClient.target(endpoint);
                Invocation.Builder requestBuilder = target.request(MediaType.APPLICATION_JSON_TYPE)
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + fcmServerKey)
                        .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON);

                // Execute the POST request
                Response response = requestBuilder.post(Entity.json(payload));

                // Evaluate the response
                if (response.getStatus() >= 200 && response.getStatus() < 300) {
                    // Success – exit the loop
                    response.close();
                    return;
                } else {
                    // Non‑2xx response – treat as transient failure
                    String errorBody = response.readEntity(String.class);
                    response.close();
                    throw new RuntimeException(
                            String.format("FCM responded with status %d: %s",
                                    response.getStatus(), errorBody));
                }
            } catch (Exception e) {
                // Log the error with masked token and attempt count
                logger.errorf(LOG_ERROR, maskToken(notification.getToken()), attempt, e.getMessage());
                if (attempt >= MAX_RETRY_COUNT) {
                    // All attempts exhausted – wrap and rethrow
                    throw new NotificationException(
                            String.format("Failed to send notification after %d attempts",
                                    MAX_RETRY_COUNT), e);
                }
                // Wait before retrying
                logger.infof(LOG_RETRY, maskToken(notification.getToken()), attempt);
                try {
                    Thread.sleep(RETRY_DELAY_MILLIS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new NotificationException("Retry sleep interrupted", ie);
                }
            }
        }
    }

    /**
     * Masks a token for logging purposes (keeps only the last 4 characters).
     *
     * @param token the raw token
     * @return a masked representation
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 4) {
            return "****";
        }
        return "****" + token.substring(token.length() - 4);
    }

    /* --------------------------------------------------------------------- */
    /*  Data model                                                        */
    /* --------------------------------------------------------------------- */

    /**
     * Simple DTO representing a push notification to be sent via FCM.
     *
     * @traceability [REQ-016]
     */
    public static class UserNotification {
        private final String title;
        private final String body;
        private final String token;
        private final Map<String, String> data;

        public UserNotification(String title, String body, String token, Map<String, String> data) {
            this.title = title;
            this.body = body;
            this.token = token;
            this.data = data;
        }

        public String getTitle() {
            return title;
        }

        public String getBody() {
            return body;
        }

        public String getToken() {
            return token;
        }

        public Map<String, String> getData() {
            return data;
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Custom exception                                                   */
    /* --------------------------------------------------------------------- */

    /**
     * Runtime exception thrown when notification delivery fails after all retries.
     *
     * @traceability [EXC-003]
     */
    public static class NotificationException extends RuntimeException {
        public NotificationException(String message, Throwable cause) {
            super(message, cause);
        }

        public NotificationException(String message) {
            super(message);
        }
    }
}