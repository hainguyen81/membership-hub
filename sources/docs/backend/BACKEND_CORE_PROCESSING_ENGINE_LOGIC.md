flowchart TD
    A[Mobile App Scans QR] -->|POST /api/v1/attendance/scan| B(AttendanceController)
    B -->|Extract Payload & IdempotencyKey| C{QrPayloadDecoder}
    C -->|Decode Base64| D[Extract studentID & courseID]
    D --> E{Validate Enrollment}
    E -->|Not Enrolled| F[Throw EnrollmentRequiredException - 403]
    E -->|Enrolled| G{Check Idempotency}
    G -->|Duplicate Found [EXC-002]| H[Return 200 OK with duplicate=true]
    G -->|New Scan| I[Persist Attendance Record]
    I --> J[Publish Kafka Event: attendance-recorded]
    J --> K[Return 201 Created]
    
    %% Fault Tolerance Paths
    A -->|Network Drop [EXC-001]| L[Store in Local IndexedDB Queue]
    L -->|Connection Restored| M[FIFO Recovery Queue [EXC-005]]
    M -->|Re-submit Scans| B