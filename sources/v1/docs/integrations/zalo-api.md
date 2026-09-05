markdown
# Zalo API Integration Documentation
## Document Metadata
| Attribute | Value |
|-----------|-------|
| Project Name | membership-hub |
| Document Version | 1.0 |
| Last Updated | 2026-08-18 |
| Targeted Requirement Tags | [REQ-016], [ARC-008] |
| Owner | Integration Team |
| Status | Approved |
| Related Documentation | [./sources/docs/architecture/authentication-flow.md](./sources/docs/architecture/authentication-flow.md), [./sources/docs/api/notification-promotion-api.md](./sources/docs/api/notification-promotion-api.md) |

---

## 1. Overview
This document defines the end-to-end integration between the membership-hub notification service and the Zalo Official Account (OA) API, enabling multi-channel broadcast and targeted notification delivery. The integration is built to satisfy requirement [REQ-016] (multi-channel notification system for course assignments, enrollment confirmations, attendance alerts, and announcements) and aligns with the event-driven architecture defined in [ARC-008].

Key capabilities covered:
- Broadcast notifications to Zalo groups associated with training centers
- Targeted notifications to individual Zalo users
- Template-based structured notifications for standardized alerts
- Fault-tolerant retry mechanism for transient delivery failures
- Audit logging for all delivery attempts per [NFR-006]

---

## 2. Data Dictionary for Zalo Integration Entities
All entities below are directly mapped to requirement [REQ-016] and architecture component [ARC-008].
| Table Name | Field Name | Data Type | Constraints | Description | Traceability Tags |
|------------|------------|-----------|-------------|-------------|-------------------|
| notifications | group_zalo | VARCHAR(255) | NULLABLE | Zalo Group ID or Official Account OA ID for broadcast notifications, populated when Zalo delivery is requested | [REQ-016], [ARC-008] |
| notifications | message | TEXT | NOT NULL, MAX 2000 CHARS | Notification payload content, supports Zalo message formatting (plain text, rich media, quick replies) | [REQ-016], [ARC-008] |
| notifications | retry_count | INT | NOT NULL, DEFAULT 0, MIN 0, MAX 3 | Number of retry attempts for failed Zalo API calls, capped at 3 per [REQ-016] retry policy | [REQ-016], [ARC-008] |
| notifications | delivered | BOOLEAN | NOT NULL, DEFAULT FALSE | Flag indicating successful delivery confirmation from Zalo API | [REQ-016], [ARC-008] |
| system_settings | zalo_oa_id | VARCHAR(64) | NOT NULL, UNIQUE | Official Account ID for membership-hub Zalo integration, stored encrypted per [NFR-003] | [ARC-008] |
| system_settings | zalo_api_secret | VARCHAR(128) | NOT NULL | Encrypted API secret for Zalo OA OAuth 2.0 authentication, stored exclusively in GCP Secret Manager per [NFR-003] | [ARC-008] |

---

## 3. System Architecture & Event Pipeline
The Zalo integration follows the event-driven notification architecture defined in [ARC-008], with decoupled event producers and fault-tolerant delivery processing.
mermaid
flowchart LR
    A[System Event Trigger<br/>(Course Assignment, Enrollment, Announcement)] --> B[Kafka Event Broker<br/>Topic: notification.send<br/>Partitions: 2, Retention: 7 days]
    B --> C[Notification Service<br/>./sources/backend/notification-service/src/main/java/com/hub/notification/NotificationService.java]
    C --> D{Zalo Delivery<br/>Eligibility Check}
    D -->|Has group_zalo or user Zalo ID target| E[ZaloNotificationSender<br/>./sources/backend/notification-service/src/main/java/com/hub/notification/ZaloNotificationSender.java]
    D -->|No Zalo target| F[Skip Zalo Delivery<br/>Process other channels (FCM/APNs)]
    E --> G[Zalo Official Account API<br/>https://business.openapi.zalo.me]
    G -->|Success 200 OK| H[Update delivered = TRUE<br/>Log success event per [NFR-006]]
    G -->|Failure 4xx/5xx| I[Retry Queue<br/>Max 3 attempts, 5min exponential backoff]
    I -->|Retry < 3| E
    I -->|Retry >= 3| J[Update delivered = FALSE<br/>Alert System Admin<br/>Log error with traceability tags [REQ-016], [ARC-008]]


### 3.1 Traceability Matrix Reference
| Architecture Component | Description | Mapped Tags |
|------------------------|-------------|-------------|
| Kafka Event Broker (`notification.send` topic) | Decoupled event queue for notification dispatch, ensures no message loss during Zalo API outages | [ARC-008], [REQ-016] |
| ZaloNotificationSender Service | Core integration class for Zalo OA API calls, implements retry logic and idempotency | [REQ-016], [ARC-008] |
| Retry Queue Mechanism | Fault tolerance layer for transient Zalo API failures, max 3 retries per [REQ-016] | [REQ-016], [NFR-006] |
| System Admin Alerting | Failure notification for undelivered Zalo messages after max retries | [REQ-016], [NFR-006] |

---

## 4. Zalo Official Account API Integration Specifications
All external API calls to Zalo use OAuth 2.0 authentication, with credentials stored in GCP Secret Manager per [NFR-003] security requirements.
### 4.1 Prerequisites
1. Active Zalo Official Account (OA) registered for membership-hub with approved message templates
2. OA ID and API Secret stored in GCP Secret Manager under the `zalo-oa-credentials` key
3. OA configured with required permissions: `message:send`, `group:manage`, `message:template`
4. IP whitelist configured in Zalo OA console to allow traffic from GKE cluster egress IP ranges

### 4.2 Core Zalo API Endpoints Used
| HTTP Method | Zalo API Endpoint | Purpose | Request Headers | Request Body Schema | Success Response (200 OK) | Traceability Tags |
|-------------|-------------------|---------|-----------------|---------------------|---------------------------|-------------------|
| POST | https://business.openapi.zalo.me/v2.0/oa/message | Send plain text/rich media message to individual user or Zalo group | `Authorization: Bearer <Zalo Access Token>`<br/>`Content-Type: application/json`<br/>`X-Idempotency-Key: <UUID>` | json { "recipient": { "user_id": "<Zalo User ID>" \| "group_id": "<Zalo Group ID>" }, "message": { "text": "<Notification content, max 2000 chars>" } }  | json { "error": 0, "message": "success", "data": { "message_id": "<Zalo Unique Message ID>" } }  | [REQ-016], [ARC-008] |
| POST | https://business.openapi.zalo.me/v2.0/oa/message/template | Send pre-approved template message for structured notifications (course assignments, attendance alerts) | Same as above | json { "recipient": { "user_id": "<Zalo User ID>" }, "template_name": "<Approved Zalo Template Name>", "template_data": { "param1": "<Dynamic Value 1>", "param2": "<Dynamic Value 2>" } }  | Same as above | [REQ-016], [ARC-008] |
| GET | https://business.openapi.zalo.me/v2.0/oa/message/{message_id}/status | Check real-time delivery status of sent Zalo message | Same as above | N/A | json { "error": 0, "data": { "status": "sent \| delivered \| read \| failed" } }  | [REQ-016], [ARC-008] |

---

## 5. Internal Notification Service Endpoints (Zalo Trigger)
These internal REST endpoints are part of the notification service, and trigger Zalo delivery when a `groupZalo` target or user Zalo ID is specified in the request payload, per [REQ-016] and [ARC-008].
| HTTP Method | Internal Endpoint | Request Headers | Request Body Schema | Success Response (202 Accepted) | Error Responses | Traceability Tags |
|-------------|-------------------|-----------------|---------------------|---------------------------------|-----------------|-------------------|
| POST | /api/v1/notifications/send | `Authorization: Bearer <JWT Token>`<br/>`Content-Type: application/json` | json { "userId": "<UUID (optional, for user-specific Zalo notifications)>", "groupZalo": "<String (optional, Zalo Group ID/OA ID for broadcast)>", "message": "<String (required, max 2000 chars)>", "templateName": "<String (optional, for template messages)>", "templateData": "<Object (optional, template dynamic parameters)>" }  | json { "message": "Notification queued", "notificationId": "<UUID>", "zaloTarget": "<groupZalo or userId>" }  | 400 Bad Request: Missing required `message` field<br/>401 Unauthorized: Invalid/expired JWT token<br/>403 Forbidden: User lacks permission to send notifications<br/>502 Bad Gateway: Zalo API delivery failed (triggers retry per [REQ-016]) | [REQ-016], [ARC-008] |
| GET | /api/v1/notifications/{notificationId}/status | `Authorization: Bearer <JWT Token>` | N/A | json { "notificationId": "<UUID>", "delivered": "<Boolean>", "zaloDeliveryStatus": "<sent \| delivered \| read \| failed>", "retryCount": "<Integer>" }  | 404 Not Found: Notification ID does not exist<br/>403 Forbidden: User lacks permission to view notification status | [REQ-016], [ARC-008] |

---

## 6. Implementation Guidelines
All implementation must adhere to enterprise coding standards, security requirements [NFR-003], and traceability mandates.
### 6.1 Core Implementation Class
The primary integration class is `ZaloNotificationSender` located at:
`./sources/backend/notification-service/src/main/java/com/hub/notification/ZaloNotificationSender.java`

Key implementation requirements:
1. **Authentication**: Retrieve Zalo OA access token from GCP Secret Manager at service startup, implement automatic token refresh when the token expires (Zalo access tokens are valid for 1 hour)
2. **Retry Logic**: Implement exponential backoff retry for transient Zalo API failures (5xx errors, 429 rate limit errors), max 3 retries per [REQ-016], log each retry attempt with timestamp and error details per [NFR-006]
3. **Idempotency**: Generate a unique `X-Idempotency-Key` UUID for each Zalo message request to avoid duplicate delivery if retries are triggered
4. **Logging**: Inject log statements at entry/exit of all public methods, log all errors with traceability tags [REQ-016], [ARC-008] per enterprise logging mandates
5. **Error Handling**: Catch all Zalo API exceptions, forward the original caught exception context in custom `ZaloNotificationException` per enterprise exception handling rules

### 6.2 Kafka Event Integration
The Zalo sender subscribes to the `notification.send` Kafka topic defined in [ARC-008], with the following production-grade configuration:
| Kafka Configuration Property | Value | Description |
|------------------------------|-------|-------------|
| bootstrap.servers | ${KAFKA_BOOTSTRAP_SERVERS} | Kafka cluster endpoint loaded from environment variables, no hardcoded values per enterprise guardrails |
| group.id | zalo-notification-consumer | Unique consumer group ID for Zalo notification processing |
| max.poll.interval.ms | 300000 | Maximum time between poll calls to avoid consumer timeout during high load |
| enable.auto.commit | false | Manual offset commit after successful Zalo delivery to avoid message loss |
| auto.offset.reset | earliest | Process all pending notifications on consumer startup after outage |

All Kafka message payloads must include the `group_zalo` field to trigger Zalo delivery, per [ARC-008] event pipeline design.

---

## 7. Error Handling & Retry Policy
All error handling follows the enterprise exception handling framework and [REQ-016] notification reliability requirements.
| Error Scenario | Zalo API Error Code | Handling Logic | User-Facing Message | Traceability Tags |
|----------------|---------------------|----------------|---------------------|-------------------|
| Invalid Zalo OA credentials | 401 Unauthorized | Log error with full context, trigger system admin alert via email/Slack, do not retry | System error: Notification delivery failed, please contact support | [REQ-016], [EXC-003], [NFR-003] |
| Rate limit exceeded | 429 Too Many Requests | Wait 5 minutes, retry up to 3 times with exponential backoff | N/A (background retry, no end-user impact) | [REQ-016], [EXC-003] |
| Invalid recipient (user/group ID) | 400 Bad Request | Log error, mark notification as failed, do not retry | N/A (invalid target, no end-user impact) | [REQ-016], [EXC-003] |
| Transient server error | 5xx Internal Server Error | Exponential backoff retry (1min, 2min, 5min intervals), max 3 attempts | N/A (background retry, no end-user impact) | [REQ-016], [EXC-003] |
| Max retries exceeded | N/A | Update `delivered = FALSE` in notifications table, send high-priority alert to system admin | System error: Notification delivery failed, please contact support | [REQ-016], [EXC-003], [NFR-006] |

All error logs must include the following mandatory context per enterprise logging mandates:
1. Subsystem name: `zalo-notification-service`
2. Raw error message string from Zalo API response
3. Explicit traceability tags: [REQ-016], [ARC-008]
4. Notification ID and masked target group/user ID (per [NFR-006] PII masking rules)

---

## 8. Security & Compliance
All Zalo integration components adhere to enterprise security standards defined in [NFR-003]:
1. **Credential Management**: Zalo OA API secret and access token are stored exclusively in GCP Secret Manager, never hardcoded in source code, configuration files, or Docker images
2. **Data Masking**: All Zalo user IDs and group IDs are masked in application logs per [NFR-006] audit logging requirements, using the pattern `***MASKED_ZALO_ID***`
3. **Network Security**: All outbound traffic to Zalo API is routed through GKE egress firewall rules, only allowlisted Zalo API IP ranges are permitted
4. **Data Privacy**: Notification content sent to Zalo does not include unencrypted PII (email, phone number, full address) per GDPR/CCPA requirements [NFR-008]
5. **Audit Logging**: All Zalo delivery attempts (success/failure) are logged with timestamp, notification ID, masked target ID, and delivery status, stored for 1 year per [NFR-006]

---

## 9. Testing Requirements
All Zalo integration components must meet the following testing criteria per enterprise QA mandates, with minimum 85% code coverage.
### 9.1 Unit Test Cases (Coverage ≥ 90%)
| Test Case ID | Test Scenario | Expected Result | Traceability Tags |
|--------------|---------------|-----------------|-------------------|
| ZALO-UNIT-001 | Successful send of text message to Zalo group | Message is queued, `delivered = FALSE` initially, retry count = 0 | [REQ-016], [ARC-008] |
| ZALO-UNIT-002 | Zalo API returns 429 rate limit error | Retry is scheduled after 5 minutes, retry count increments to 1 | [REQ-016], [EXC-003] |
| ZALO-UNIT-003 | Zalo API returns 401 invalid credentials | Error is logged with full context, system admin alert is triggered, no retry | [REQ-016], [EXC-003], [NFR-003] |
| ZALO-UNIT-004 | Max retries (3) exceeded for transient 5xx error | `delivered = FALSE` is set in database, system admin alert is triggered | [REQ-016], [EXC-003] |
| ZALO-UNIT-005 | Invalid Zalo group ID in request payload | Error is logged, notification is marked as failed, no retry | [REQ-016], [EXC-003] |

### 9.2 Integration Test Cases
| Test Case ID | Test Scenario | Expected Result | Traceability Tags |
|--------------|---------------|-----------------|-------------------|
| ZALO-INT-001 | End-to-end flow: Course assignment event triggers Kafka message, Zalo message delivered to test group | Message appears in Zalo group within 10 seconds, `delivered = TRUE` in database | [REQ-016], [ARC-008] |
| ZALO-INT-002 | Zalo API outage simulation (mock 503 error) | Notifications are queued in Kafka, delivered automatically when Zalo API recovers, no message loss | [REQ-016], [EXC-003] |
| ZALO-INT-003 | Send template message with dynamic parameters | Template message is rendered correctly in Zalo with correct parameter values | [REQ-016], [ARC-008] |

---

## 10. Operations & Troubleshooting
### 10.1 Common Issues & Resolutions
| Issue | Root Cause | Resolution | Traceability Tags |
|-------|------------|------------|-------------------|
| Zalo messages not being delivered | Zalo OA access token expired | Rotate access token in Zalo OA console, update secret in GCP Secret Manager, restart notification service | [REQ-016], [ARC-008] |
| High retry count for Zalo messages | Zalo API rate limit exceeded or OA suspended | Check Zalo OA console for suspension status, request rate limit increase from Zalo support | [REQ-016], [EXC-003] |
| Duplicate Zalo messages sent | Idempotency key not generated correctly | Fix idempotency key generation logic in `ZaloNotificationSender` to use UUID v4 | [REQ-016], [ARC-008] |
| PII leaked in application logs | Data masking annotation missing on notification fields | Add `@JsonSerialize` masking annotation to PII fields per [NFR-006] | [NFR-003], [NFR-006] |

### 10.2 Monitoring Metrics
The following metrics are monitored via GCP Cloud Monitoring for Zalo integration health, with alerts configured for threshold breaches:
| Metric Name | Description | Alert Threshold | Traceability Tags |
|-------------|-------------|-----------------|-------------------|
| zalo_notification_send_success_rate | Percentage of successful Zalo deliveries per hour | < 95% | [REQ-016], [NFR-002] |
| zalo_notification_retry_count | Number of retry attempts per minute | > 10 | [REQ-016], [EXC-003] |
| zalo_notification_failure_count | Number of failed Zalo deliveries per minute | > 5 | [REQ-016], [EXC-003] |
| kafka_notification_queue_lag | Number of pending notifications in `notification.send` Kafka queue | > 1000 | [ARC-008], [NFR-004] |

---

## 11. Traceability Compliance Validation
| Requirement Tag | Coverage Status | Document Section Reference |
|----------------|-----------------|----------------------------|
| [REQ-016] | 100% Covered | Sections 2, 3, 4, 5, 6, 7, 9, 10 |
| [ARC-008] | 100% Covered | Sections 2, 3, 4, 5, 6, 7, 9, 10 |