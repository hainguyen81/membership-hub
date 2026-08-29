```graph TD
    classDef root fill:#1e293b,stroke:#3b82f6,stroke-width:2px,color:#fff;
    classDef backend fill:#0f172a,stroke:#10b981,stroke-width:2px,color:#fff;
    classDef service fill:#1e1e38,stroke:#8b5cf6,stroke-width:1px,color:#fff;
    classDef frontend fill:#1e1e38,stroke:#f59e0b,stroke-width:1px,color:#fff;
    classDef infra fill:#1e1e38,stroke:#06b6d4,stroke-width:1px,color:#fff;

    Root["./ (membershiphub Root Workspace)"]:::root

    Root --> SourcesBackend["./sources/backend/ (Maven Multi-Module Parent)"]:::backend
    Root --> SourcesFrontend["./sources/frontend/web-app/ (Next.js 14 + Capacitor)"]:::frontend
    Root --> SourcesInfra["./sources/infra/ (Terraform, Docker, K8s manifests)"]:::infra
    Root --> SourcesDocs["./sources/docs/ (Architecture & API Blueprints)"]:::infra

    SourcesBackend --> UserSvc["user-service (org.nlh4j.membershiphub.userservice)"]:::service
    SourcesBackend --> CenterSvc["center-service (org.nlh4j.membershiphub.centerservice)"]:::service
    SourcesBackend --> CourseSvc["course-service (org.nlh4j.membershiphub.courseservice)"]:::service
    SourcesBackend --> AttendSvc["attendance-service (org.nlh4j.membershiphub.attendanceservice)"]:::service
    SourcesBackend --> NotifSvc["notification-service (org.nlh4j.membershiphub.notificationservice)"]:::service
    SourcesBackend --> ReportSvc["reporting-service (org.nlh4j.membershiphub.reportingservice)"]:::service
```
