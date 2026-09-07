membership-hub/
├── sources/
│   ├── backend/
│   │   ├── pom.xml (Root Parent POM - org.nlh4j.membershiphub:membership-hub-backend:1.0.0-SNAPSHOT)
│   │   ├── user-service/
│   │   │   ├── pom.xml
│   │   │   └── src/main/java/org/nlh4j/membershiphub/userservice/
│   │   ├── center-service/
│   │   │   ├── pom.xml
│   │   │   └── src/main/java/org/nlh4j/membershiphub/centerservice/
│   │   ├── course-service/
│   │   │   ├── pom.xml
│   │   │   └── src/main/java/org/nlh4j/membershiphub/courseservice/
│   │   └── attendance-service/
│   │       ├── pom.xml
│   │       └── src/main/java/org/nlh4j/membershiphub/attendanceservice/
│   │           ├── AttendanceServiceApplication.java
│   │           ├── controller/
│   │           │   └── AttendanceController.java
│   │           ├── service/
│   │           │   ├── AttendanceService.java
│   │           │   └── QrPayloadDecoder.java
│   │           ├── repository/
│   │           │   └── AttendanceRepository.java
│   │           ├── messaging/
│   │           │   └── KafkaAttendanceProducer.java
│   │           ├── dto/
│   │           │   ├── QrScanRequest.java
│   │           │   └── AttendanceResponse.java
│   │           └── exception/
│   │               ├── DuplicateAttendanceException.java
│   │               ├── EnrollmentRequiredException.java
│   │               └── InvalidQrPayloadException.java
│   ├── frontend/
│   │   ├── package.json
│   │   └── tsconfig.json
│   └── docs/
│       └── backend/
│           └── BACKEND_ENTERPRISE_CODING_STANDARDS.md