```markdown
# Course Service Architecture

## Enrollment Flow Sequence Diagram

```mermaid
sequenceDiagram
    participant Student
    participant CourseService
    participant EnrollmentService
    participant KafkaProducer
    participant NotificationService
    participant PushGateway
    participant ZaloBot

    Student->>CourseService: POST /api/v1/enrollments (courseId)
    CourseService->>CourseService: Validate course exists
    alt Course not found
        CourseService->>Student: 400 Course not found
    else Course exists
        CourseService->>EnrollmentService: Check if student exists
        alt Student not exists
            EnrollmentService->>UserService: Auto-create Student
            UserService->>EnrollmentService: Return studentId
        end
        EnrollmentService->>EnrollmentService: Create enrollment record
        EnrollmentService->>KafkaProducer: Publish enrollment-created event
        KafkaProducer->>NotificationService: Consume enrollment-events
        NotificationService->>PushGateway: Send push notification to student
        NotificationService->>ZaloBot: Send Zalo message to center
    end
```

## Enrollment Flow Overview (Flowchart)

```mermaid
flowchart TD
    A[Student] --> B[POST /api/v1/enrollments]
    B --> C{Validate course exists?}
    C -->|No| D[Return 400]
    C -->|Yes| E{Student exists?}
    E -->|No| F[Auto-create Student]
    F --> G[Create Enrollment]
    G --> H[Publish Kafka event]
    H --> I[Notification Service]
    I --> J[Send Push Notification]
    I --> K[Send Zalo Message]
```

## Traceability Matrix Reference

| Architectural Component | Description | Targeted Tag IDs |
|------------------------|-------------|------------------|
| Enrollment Flow Sequence Diagram | End-to-end enrollment processing with auto-creation and notification dispatch | [REQ-011], [ARC-008] |
| Enrollment Flow Overview (Flowchart) | High-level visual of enrollment steps and downstream notifications | [REQ-011], [ARC-008] |
| Course Service Architecture Documentation | Overall architecture documentation for course-service | [DOC-001] |

## Additional Notes
- All components reside under the Java package `org.nlh4j.membershiphub.courseservice`.
- Kafka topic `enrollment-events` is produced by `org.nlh4j.membershiphub.courseservice.kafka.EnrollmentEventProducer`.
- Notification service consumes from `org.nlh4j.membershiphub.attendanceservice.kafka.NotificationEventConsumer`.
```