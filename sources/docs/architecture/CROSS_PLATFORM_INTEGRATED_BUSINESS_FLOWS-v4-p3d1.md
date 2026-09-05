```markdown
# 📊 Scaffolding Architecture Documentation
## 📝 Overview
This document outlines the scaffolding architecture of the Membership Hub project, including the multi-module Maven structure, package naming conventions, and technology stack.

## 📁 Multi-Module Maven Structure
The project follows a multi-module Maven structure, with the root directory `./sources/backend` containing the parent `pom.xml` file. The four microservices are:

* `user-service`
* `center-service`
* `course-service`
* `attendance-service`

Each microservice has its own `pom.xml` file located in its respective directory.

## 📦 Package Naming Conventions
The Java package prefix base is `org.nlh4j.membershiphub`. Each microservice has its own sub-package:

* `org.nlh4j.membershiphub.userservice`
* `org.nlh4j.membershiphub.centerservice`
* `org.nlh4j.membershiphub.courseservice`
* `org.nlh4j.membershiphub.attendanceservice`

## 📊 Technology Stack
The project utilizes the following technology stack:

* **Backend:** Quarkus 3.15.1, Java 17 LTS
* **Frontend:** Next.js 14.2.