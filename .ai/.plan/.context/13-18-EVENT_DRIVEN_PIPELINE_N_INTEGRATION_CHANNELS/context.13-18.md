# PHASE 3 CONTEXT: EVENT-DRIVEN PIPELINE & INTEGRATION CHANNELS (DAYS 13 - 18)

## DAY 13: INTEGRATION TESTING CORE TRANSACTION LOOPS
### Target Path: ./sources/backend/src/test/java/org/nlh4j/saas/membership/hub/service/AttendanceCheckinIntegrationTest.java
### Architecture Requirements
Establish full-fidelity integration testing suites validating operational database isolation states and idempotent write gates under live-container architectures.
- Extend test cases to inherit from standard @QuarkusTest properties configured to communicate directly with isolated Testcontainers instances spinning up true PostgreSQL container modules.
- Simulate rigorous race-condition spikes by firing 5 consecutive duplicate scan requests concurrently across parallel processing threads.
- Assert that only one single data modification is successfully persisted inside attendance_logs while cross-tenant penetration scripts report zero horizontal access leaks.

## DAY 14: KAFKA TRANSACTIONS PRODUCER FABRIC
### Target Path: ./sources/backend/src/main/java/org/nlh4j/saas/membership/hub/integration/KafkaEventProducer.java
### Architecture Requirements
Develop a non-blocking reactive event generation structure connecting business processing approvals directly to remote execution layers.
- Implement outbound event dispatchers leveraging the SmallRye Reactive Messaging framework ecosystem.
- Convert approved check-in instances immediately into lightweight JSON messaging payloads.
- Stream components securely over to the central root Kafka topic: attendance-events. Explicitly assign message partition keys to match the active transaction tenant_id string to guarantee packet sequencing order across cluster nodes.

## DAY 15: CONCURRENT FAN-OUT NOTIFICATION ROUTER
### Target Path: ./sources/backend/src/main/java/org/nlh4j/saas/membership/hub/integration/AttendanceEventRouterConsumer.java
### Architecture Requirements
Deploy a robust background reactive message routing engine processing inbound events free from network latency blockages.
- Configure a stream consumer listening over the primary attendance-events channel.
- Upon capture, immediately invoke an inline data mutation committing an audit log record initialized as PENDING inside the notification_delivery_logs database table, fully storing raw payload elements and scoping user/tenant linkages.
- Utilize the reactive operator blocks native to the Mutiny streaming engine to multiplex a single inbound transaction and dispatch it concurrently into three split downstream queues: notification-zalo-personal, notification-zalo-group, and notification-push-fcm.
- **Requirements Amendment:** The concurrent messaging consumer loop must invoke a non-blocking database insert initializing a transaction log marked as PENDING inside notification_delivery_logs right before executing multiplexing stream forks.

## DAY 16: ZALO OPEN API TRANSACTIONS INTEGRATION GATEWAY
### Target Path: ./sources/backend/src/main/java/org/nlh4j/saas/membership/hub/integration/client/ZaloApiClient.java
### Architecture Requirements
Build external transactional network integrations leveraging native Quarkus declarative REST Client Reactive components with inline audit trail finalization logic.
- Implement secure communication clients targeting foreign Zalo Open API server endpoints pushing personal notices and class group CRM dispatches.
- Format outbound JSON bodies matching certified Zalo communication templates.
- **Audit Update Rule:** Upon receiving responses from external Zalo API nodes, the consumer must intercept the return codes immediately. Trigger an asynchronous non-blocking database update modifying the initial record inside notification_delivery_logs: mutate state from PENDING to SENT on success, or to FAILED on connection rejections, writing full precision third-party error messages to the trace block.
- **Requirements Amendment:** Upon receiving network delivery response parameters from foreign gateways (Zalo Open API / Firebase FCM), the execution thread must asynchronously mutate the initial log record from PENDING to SENT on success, or to FAILED on connection breaks, writing full precision error messages stack traces to the trace block.

## DAY 17: FIREBASE REAL-TIME REMOTE ALERT SYSTEM CLIENT
### Target Path: ./sources/backend/src/main/java/org/nlh4j/saas/membership/hub/integration/client/FcmPushClient.java
### Architecture Requirements
Integrate official Google Firebase Admin SDK components inside the reactive framework domain to govern mobile push notifications with runtime logging support.
- Ingest asynchronous message objects flowing over the isolated notification-push-fcm channel, mapping visual notification templates pulling remaining subscription metrics dynamically.
- Address target terminal receivers using authenticated device keys (fcm_token), executing immediate dispatches.
- **Audit Update Rule:** Immediately intercept the Firebase dispatch outcomes. Perform an inline transaction updating the target record inside notification_delivery_logs: write SENT upon verified mobile package receipt, or switch status to FAILED coupled with exact Firebase API crash traces for UI reporting visibility.
- **Requirements Amendment:** Upon receiving network delivery response parameters from foreign gateways (Zalo Open API / Firebase FCM), the execution thread must asynchronously mutate the initial log record from PENDING to SENT on success, or to FAILED on connection breaks, writing full precision error messages stack traces to the trace block.

## DAY 18: EVENT STREAM RELIABILITY INTEGRATION TESTS
### Target Path: ./sources/backend/src/test/java/org/nlh4j/saas/membership/hub/integration/KafkaNotificationPipelineTest.java
### Architecture Requirements
Author multi-layered asynchronous stream reliability validation suites leveraging Testcontainers architectures to manage an active Apache Kafka orchestration mesh.
- Push validation payloads directly into ingestion streams and trace transmission accuracy across all discrete lower distribution targets.
- Inject deliberate connectivity drop-outs mimicking external API server failures to verify backoff retry profiles operate correctly. Confirm failed payloads migrate to isolated Dead Letter Queues (DLQ) without causing data blockages in the primary channels.
