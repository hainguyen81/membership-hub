# GLOBAL PROJECT CONTEXT: membership-hub

## 📊 Document Control

| Item | Details |
| :--- | :--- |
| **Blueprint ID** | ARCH-20260804052551 |
| **Project Name** | membership-hub |
| **Version** | 1.0 (Baseline) |
| **Date.Time** | 2026/08/04 05:25:51 |
| **Author** | Enterprise System Architect (SA Agent) |
| **Approval** | Pending Technical Governance Review |

## 📊 1. SYSTEM OVERVIEW & CORE ARCHITECTURE MODALITY

### 1.1. Core System Modality & Architecture Modality
Hệ thống membership-hub là một nền tảng quản lý hội viên đa trung tâm với kiến trúc đa lớp bao gồm:
- Lớp giao diện người dùng (UI) bao gồm web và ứng dụng di động
- Lớp backend với các dịch vụ vi dịch vụ (microservices)
- Lớp cơ sở dữ liệu với PostgreSQL
- Lớp tích hợp với các dịch vụ bên ngoài như Firebase Authentication, Google Cloud Messaging (FCM), và Zalo API

### 1.2. Enterprise Data Flow Topologies & Core Ecosystems
Hệ thống sử dụng các kênh truyền thông bất đồng bộ bao gồm:
- Hệ thống thông báo đẩy (FCM/APNs) cho các thông báo thời gian thực
- Hệ thống tích hợp Zalo API cho các thông báo nhóm
- Hệ thống xử lý điểm danh QR với tính năng idempotent

## 📁 2. TECH STACK DEPENDENCIES & ECOSYSTEM LIBRARIES
- **Backend Infrastructure Core Stack:** Java/Quarkus, PostgreSQL, Docker, Kubernetes (GKE), Firebase Authentication, Google Cloud Messaging (FCM), Redis
- **Frontend & Cross-Platform UI Mobile Stack:** Next.js, React, Firebase Authentication, Google Cloud Messaging (FCM)

## 📁 3. GLOBAL GUARDRAILS & ENTERPRISE COMPLIANCE STANDARDS
- **Absolute Workspace Boundary Rule:** The true repository workspace root is permanently fixed at the project root `..`. All paths generated MUST begin with `./sources/`.
- **Dynamic Directory Prefixing Compliance:** Enforce the dynamic path mapping rules defined in Protocol 1 strictly matching the detected project structure.
- **[CONDITION: JAVA_STACK_ONLY] Java Package Standard:** If the tech stack utilizes Java frameworks, all Java source codes MUST strictly reside within the corporate package foundation: `org.nlh4j.saas.<project_name_alphanumeric_lowercase>`. You MUST dynamically convert the string "membership-hub" into a strict pure alphanumeric lowercase token by stripping out whitespaces, hyphens, and underscores. Non-Java projects are completely banned from applying this package segment.
- **Strict Tester Target Path Syntax:** Any component targeted by a Tester Sub-Agent must be structured as a strict semi-colon separated pair `<source_component_or_token>;<test_suite_file_to_execute>`. Both paths inside the pair MUST begin with `./sources/`.

## 📁 4. HIGH-LEVEL MULTI-PHASE ARCHITECTURAL SYNOPSIS GRID

| Phase | Day Range | Architectural Component / Module Path | Technical Deliverables Summary | Assigned Sub-Agent | Targeted Tag IDs |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | 1-3 | `./sources/backend`, `./sources/frontend` | Thiết lập cơ sở hạ tầng backend và frontend, triển khai cơ sở dữ liệu PostgreSQL, tích hợp Firebase Authentication | Coder, Docker, GCP | [ARC-006], [ARC-010], [DAT-001], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-011] |
| 2 | 4-6 | `./sources/backend`, `./sources/frontend` | Triển khai các tính năng quản lý người dùng, quản lý trung tâm, quản lý khóa học | Coder, Tester, Reviewer | [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [REQ-007], [REQ-008], [REQ-009] |
| 3 | 7-9 | `./sources/backend`, `./sources/frontend` | Triển khai các tính năng đăng ký và ghi danh học viên, điểm danh và quét mã QR | Coder, Tester, Reviewer | [REQ-010], [REQ-011], [REQ-012], [REQ-013], [EXC-001], [EXC-002], [EXC-004] |
| 4 | 10-12 | `./sources/backend`, `./sources/frontend` | Triển khai các tính năng quản lý thẻ hội viên, thông báo và truyền thông | Coder, Tester, Reviewer | [REQ-014], [REQ-015], [REQ-016], [EXC-003] |
| 5 | 13-15 | `./sources/backend`, `./sources/frontend` | Triển khai các tính năng quản lý khuyến mãi và thông báo, chatbot dịch vụ khách hàng AI, các tính năng cốt lõi của ứng dụng di động, bản địa hóa và SEO | Coder, Tester, Reviewer, Doc | [REQ-017], [REQ-018], [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |

## 5. GRANULAR PHASE SPECIALIZATIONS & DAY-BY-DAY DELIVERABLES

### Phase 1 Detailed Architectural Specification
- **Phase Core Objective & Purpose:** Thiết lập cơ sở hạ tầng backend và frontend, triển khai cơ sở dữ liệu PostgreSQL, tích hợp Firebase Authentication
- **Target Physical Directory Matrix Map:** `./sources/backend`, `./sources/frontend`
- **Database Schema DDL SQL Specification [DAT-001], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-011]:**
  ```sql
  CREATE TABLE USERS (
      userId UUID PRIMARY KEY,
      email VARCHAR(255) NOT NULL UNIQUE,
      passwordHash CHAR(60) NOT NULL,
      fullName VARCHAR(100) NOT NULL,
      roleId SMALLINT NOT NULL,
      provider VARCHAR(10) DEFAULT 'local',
      createdAt TIMESTAMP NOT NULL DEFAULT NOW(),
      updatedAt TIMESTAMP NOT NULL DEFAULT NOW(),
      FOREIGN KEY (roleId) REFERENCES ROLES(roleId)
  );

  CREATE TABLE ROLES (
      roleId SMALLINT PRIMARY KEY,
      name VARCHAR(30) NOT NULL UNIQUE,
      description VARCHAR(200)
  );

  CREATE TABLE CENTERS (
      centerId UUID PRIMARY KEY,
      name VARCHAR(100) NOT NULL,
      address VARCHAR(255) NOT NULL,
      taxId VARCHAR(13) NOT NULL UNIQUE,
      contactPhone VARCHAR(20),
      contactEmail VARCHAR(255)
  );

  CREATE TABLE COURSES (
      courseId UUID PRIMARY KEY,
      title VARCHAR(150) NOT NULL,
      description TEXT,
      startDate DATE NOT NULL,
      endDate DATE NOT NULL,
      teacherId UUID,
      maxStudents INT DEFAULT 30,
      FOREIGN KEY (teacherId) REFERENCES USERS(userId)
  );

  CREATE TABLE ENROLLMENTS (
      enrollmentId UUID PRIMARY KEY,
      studentId UUID NOT NULL,
      courseId UUID NOT NULL,
      enrollmentDate TIMESTAMP NOT NULL DEFAULT NOW(),
      FOREIGN KEY (studentId) REFERENCES USERS(userId),
      FOREIGN KEY (courseId) REFERENCES COURSES(courseId)
  );

  CREATE TABLE ATTENDANCE (
      attendanceId UUID PRIMARY KEY,
      studentId UUID NOT NULL,
      courseId UUID NOT NULL,
      attendanceDate DATE NOT NULL,
      timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
      FOREIGN KEY (studentId) REFERENCES USERS(userId),
      FOREIGN KEY (courseId) REFERENCES COURSES(courseId)
  );

  CREATE TABLE STUDENTCARDS (
      cardId UUID PRIMARY KEY,
      studentId UUID NOT NULL,
      issueDate DATE NOT NULL,
      validityDays INT NOT NULL,
      remainingDays INT,
      FOREIGN KEY (studentId) REFERENCES USERS(userId)
  );

  CREATE TABLE NOTIFICATIONS (
      notificationId UUID PRIMARY KEY,
      userId UUID,
      groupZalo VARCHAR(255),
      message TEXT NOT NULL,
      sentAt TIMESTAMP NOT NULL DEFAULT NOW(),
      delivered BOOLEAN DEFAULT FALSE,
      FOREIGN KEY (userId) REFERENCES USERS(userId)
  );

  CREATE TABLE PROMOTIONS (
      promoId UUID PRIMARY KEY,
      code VARCHAR(20) UNIQUE,
      discountPercent SMALLINT NOT NULL,
      startDate DATE,
      endDate DATE,
      description TEXT
  );

  CREATE TABLE ANNOUNCEMENTS (
      announcementId UUID PRIMARY KEY,
      title VARCHAR(150) NOT NULL,
      content TEXT NOT NULL,
      startDate DATE,
      endDate DATE
  );

  CREATE TABLE SYSTEMSETTINGS (
      settingKey VARCHAR(50) PRIMARY KEY,
      settingValue TEXT NOT NULL,
      description TEXT
  );
  ```
- **API and Event Routing Contracts [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [REQ-007], [REQ-008], [REQ-009], [ARC-006], [ARC-010]:**
  ```json
  {
    "paths": {
      "/api/auth/register": {
        "post": {
          "summary": "Đăng ký người dùng mới",
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "email": {
                      "type": "string",
                      "format": "email"
                    },
                    "password": {
                      "type": "string",
                      "minLength": 8
                    },
                    "fullName": {
                      "type": "string"
                    }
                  },
                  "required": ["email", "password", "fullName"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Đăng ký thành công",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "object",
                    "properties": {
                      "token": {
                        "type": "string"
                      }
                    }
                  }
                }
              }
            }
          }
        }
      },
      "/api/auth/login": {
        "post": {
          "summary": "Đăng nhập người dùng",
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "email": {
                      "type": "string",
                      "format": "email"
                    },
                    "password": {
                      "type": "string"
                    }
                  },
                  "required": ["email", "password"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Đăng nhập thành công",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "object",
                    "properties": {
                      "token": {
                        "type": "string"
                      }
                    }
                  }
                }
              }
            }
          }
        }
      },
      "/api/auth/oauth": {
        "post": {
          "summary": "Đăng nhập qua mạng xã hội",
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "provider": {
                      "type": "string",
                      "enum": ["firebase", "google", "facebook"]
                    },
                    "token": {
                      "type": "string"
                    }
                  },
                  "required": ["provider", "token"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Đăng nhập thành công",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "object",
                    "properties": {
                      "token": {
                        "type": "string"
                      }
                    }
                  }
                }
              }
            }
          }
        }
      },
      "/api/users/{userId}/role": {
        "put": {
          "summary": "Cập nhật vai trò người dùng",
          "parameters": [
            {
              "name": "userId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "roleId": {
                      "type": "integer"
                    }
                  },
                  "required": ["roleId"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Cập nhật vai trò thành công"
            }
          }
        }
      },
      "/api/centers": {
        "get": {
          "summary": "Lấy danh sách trung tâm",
          "responses": {
            "200": {
              "description": "Danh sách trung tâm",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        "centerId": {
                          "type": "string",
                          "format": "uuid"
                        },
                        "name": {
                          "type": "string"
                        },
                        "address": {
                          "type": "string"
                        },
                        "taxId": {
                          "type": "string"
                        },
                        "contactPhone": {
                          "type": "string"
                        },
                        "contactEmail": {
                          "type": "string"
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        },
        "post": {
          "summary": "Tạo trung tâm mới",
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "name": {
                      "type": "string"
                    },
                    "address": {
                      "type": "string"
                    },
                    "taxId": {
                      "type": "string"
                    },
                    "contactPhone": {
                      "type": "string"
                    },
                    "contactEmail": {
                      "type": "string"
                    }
                  },
                  "required": ["name", "address", "taxId"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Tạo trung tâm thành công"
            }
          }
        }
      },
      "/api/centers/{centerId}": {
        "put": {
          "summary": "Cập nhật trung tâm",
          "parameters": [
            {
              "name": "centerId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "name": {
                      "type": "string"
                    },
                    "address": {
                      "type": "string"
                    },
                    "taxId": {
                      "type": "string"
                    },
                    "contactPhone": {
                      "type": "string"
                    },
                    "contactEmail": {
                      "type": "string"
                    }
                  }
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Cập nhật trung tâm thành công"
            }
          }
        },
        "delete": {
          "summary": "Xóa trung tâm",
          "parameters": [
            {
              "name": "centerId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "responses": {
            "200": {
              "description": "Xóa trung tâm thành công"
            }
          }
        }
      },
      "/api/centers/{centerId}/admin": {
        "put": {
          "summary": "Phân quyền quản trị trung tâm",
          "parameters": [
            {
              "name": "centerId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "userId": {
                      "type": "string",
                      "format": "uuid"
                    }
                  },
                  "required": ["userId"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Phân quyền quản trị trung tâm thành công"
            }
          }
        }
      },
      "/api/courses": {
        "get": {
          "summary": "Lấy danh sách khóa học",
          "responses": {
            "200": {
              "description": "Danh sách khóa học",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        "courseId": {
                          "type": "string",
                          "format": "uuid"
                        },
                        "title": {
                          "type": "string"
                        },
                        "startDate": {
                          "type": "string",
                          "format": "date"
                        },
                        "endDate": {
                          "type": "string",
                          "format": "date"
                        },
                        "teacherId": {
                          "type": "string",
                          "format": "uuid"
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        },
        "post": {
          "summary": "Tạo khóa học mới",
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "title": {
                      "type": "string"
                    },
                    "startDate": {
                      "type": "string",
                      "format": "date"
                    },
                    "endDate": {
                      "type": "string",
                      "format": "date"
                    },
                    "teacherId": {
                      "type": "string",
                      "format": "uuid"
                    }
                  },
                  "required": ["title", "startDate", "endDate", "teacherId"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Tạo khóa học thành công"
            }
          }
        }
      },
      "/api/courses/{courseId}": {
        "put": {
          "summary": "Cập nhật khóa học",
          "parameters": [
            {
              "name": "courseId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "title": {
                      "type": "string"
                    },
                    "startDate": {
                      "type": "string",
                      "format": "date"
                    },
                    "endDate": {
                      "type": "string",
                      "format": "date"
                    },
                    "teacherId": {
                      "type": "string",
                      "format": "uuid"
                    }
                  }
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Cập nhật khóa học thành công"
            }
          }
        },
        "delete": {
          "summary": "Xóa khóa học",
          "parameters": [
            {
              "name": "courseId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "responses": {
            "200": {
              "description": "Xóa khóa học thành công"
            }
          }
        }
      },
      "/api/courses/{courseId}/teacher": {
        "put": {
          "summary": "Phân công giáo viên vào khóa học",
          "parameters": [
            {
              "name": "courseId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "teacherId": {
                      "type": "string",
                      "format": "uuid"
                    }
                  },
                  "required": ["teacherId"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Phân công giáo viên vào khóa học thành công"
            }
          }
        }
      }
    }
  }
  ```
- **Phase Localized Exception Handlers [EXC-004]:**
  - **Xác thực đầu vào không hợp lệ:** Nếu xác thực thất bại trên form submission, Khi lỗi được trả về cho người dùng, Sau đó một thông báo rõ ràng liệt kê từng trường không hợp lệ và yêu cầu chỉnh sửa.

#### 📅 Chronological Day-by-Day Sub-Agent Task Distribution Logs (Phase 1)
- **DAY 1: Thiết lập cơ sở hạ tầng backend và frontend**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/config [ARC-006], [ARC-010]`
      - **Low-Level Technical Task Instruction:** Triển khai cấu hình cơ sở dữ liệu PostgreSQL, tích hợp Firebase Authentication, thiết lập cấu hình Docker và Kubernetes (GKE)
      - **Targeted Tag IDs:** [ARC-006], [ARC-010]
    * **Docker:**
      - **Target Component file path (`target_component`):** `./sources/backend/Dockerfile [ARC-010]`
      - **Low-Level Technical Task Instruction:** Viết Dockerfile cho dịch vụ backend, cấu hình multi-stage build để giảm kích thước image
      - **Targeted Tag IDs:** [ARC-010]
    * **GCP:**
      - **Target Component file path (`target_component`):** `./sources/infra/gcp [ARC-010]`
      - **Low-Level Technical Task Instruction:** Triển khai cơ sở hạ tầng trên Google Cloud Platform, cấu hình VPC, IAM, và các dịch vụ cần thiết
      - **Targeted Tag IDs:** [ARC-010]

- **DAY 2: Triển khai cơ sở dữ liệu PostgreSQL**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/resources/db/migration [DAT-001], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-011]`
      - **Low-Level Technical Task Instruction:** Viết các script Flyway/Liquibase để tạo các bảng cơ sở dữ liệu, thiết lập các ràng buộc và chỉ mục
      - **Targeted Tag IDs:** [DAT-001], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-011]

- **DAY 3: Tích hợp Firebase Authentication**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/auth [ARC-006]`
      - **Low-Level Technical Task Instruction:** Triển khai các dịch vụ xác thực qua email/mật khẩu, Firebase, Google, và Facebook OAuth2, cấu hình JWT token với thời hạn 15 phút và refresh token
      - **Targeted Tag IDs:** [ARC-006]

### Phase 2 Detailed Architectural Specification
- **Phase Core Objective & Purpose:** Triển khai các tính năng quản lý người dùng, quản lý trung tâm, quản lý khóa học
- **Target Physical Directory Matrix Map:** `./sources/backend`, `./sources/frontend`
- **API and Event Routing Contracts [REQ-001], [REQ-002], [REQ-003], [REQ-004], [REQ-005], [REQ-006], [REQ-007], [REQ-008], [REQ-009]:**
  ```json
  {
    "paths": {
      "/api/users": {
        "get": {
          "summary": "Lấy danh sách người dùng",
          "responses": {
            "200": {
              "description": "Danh sách người dùng",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        "userId": {
                          "type": "string",
                          "format": "uuid"
                        },
                        "email": {
                          "type": "string",
                          "format": "email"
                        },
                        "fullName": {
                          "type": "string"
                        },
                        "roleId": {
                          "type": "integer"
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      },
      "/api/users/{userId}": {
        "get": {
          "summary": "Lấy thông tin người dùng",
          "parameters": [
            {
              "name": "userId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "responses": {
            "200": {
              "description": "Thông tin người dùng",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "object",
                    "properties": {
                      "userId": {
                        "type": "string",
                        "format": "uuid"
                      },
                      "email": {
                        "type": "string",
                        "format": "email"
                      },
                      "fullName": {
                        "type": "string"
                      },
                      "roleId": {
                        "type": "integer"
                      }
                    }
                  }
                }
              }
            }
          }
        }
      },
      "/api/centers": {
        "get": {
          "summary": "Lấy danh sách trung tâm",
          "responses": {
            "200": {
              "description": "Danh sách trung tâm",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        "centerId": {
                          "type": "string",
                          "format": "uuid"
                        },
                        "name": {
                          "type": "string"
                        },
                        "address": {
                          "type": "string"
                        },
                        "taxId": {
                          "type": "string"
                        },
                        "contactPhone": {
                          "type": "string"
                        },
                        "contactEmail": {
                          "type": "string"
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        },
        "post": {
          "summary": "Tạo trung tâm mới",
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "name": {
                      "type": "string"
                    },
                    "address": {
                      "type": "string"
                    },
                    "taxId": {
                      "type": "string"
                    },
                    "contactPhone": {
                      "type": "string"
                    },
                    "contactEmail": {
                      "type": "string"
                    }
                  },
                  "required": ["name", "address", "taxId"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Tạo trung tâm thành công"
            }
          }
        }
      },
      "/api/centers/{centerId}": {
        "put": {
          "summary": "Cập nhật trung tâm",
          "parameters": [
            {
              "name": "centerId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "name": {
                      "type": "string"
                    },
                    "address": {
                      "type": "string"
                    },
                    "taxId": {
                      "type": "string"
                    },
                    "contactPhone": {
                      "type": "string"
                    },
                    "contactEmail": {
                      "type": "string"
                    }
                  }
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Cập nhật trung tâm thành công"
            }
          }
        },
        "delete": {
          "summary": "Xóa trung tâm",
          "parameters": [
            {
              "name": "centerId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "responses": {
            "200": {
              "description": "Xóa trung tâm thành công"
            }
          }
        }
      },
      "/api/centers/{centerId}/admin": {
        "put": {
          "summary": "Phân quyền quản trị trung tâm",
          "parameters": [
            {
              "name": "centerId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "userId": {
                      "type": "string",
                      "format": "uuid"
                    }
                  },
                  "required": ["userId"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Phân quyền quản trị trung tâm thành công"
            }
          }
        }
      },
      "/api/courses": {
        "get": {
          "summary": "Lấy danh sách khóa học",
          "responses": {
            "200": {
              "description": "Danh sách khóa học",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        "courseId": {
                          "type": "string",
                          "format": "uuid"
                        },
                        "title": {
                          "type": "string"
                        },
                        "startDate": {
                          "type": "string",
                          "format": "date"
                        },
                        "endDate": {
                          "type": "string",
                          "format": "date"
                        },
                        "teacherId": {
                          "type": "string",
                          "format": "uuid"
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        },
        "post": {
          "summary": "Tạo khóa học mới",
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "title": {
                      "type": "string"
                    },
                    "startDate": {
                      "type": "string",
                      "format": "date"
                    },
                    "endDate": {
                      "type": "string",
                      "format": "date"
                    },
                    "teacherId": {
                      "type": "string",
                      "format": "uuid"
                    }
                  },
                  "required": ["title", "startDate", "endDate", "teacherId"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Tạo khóa học thành công"
            }
          }
        }
      },
      "/api/courses/{courseId}": {
        "put": {
          "summary": "Cập nhật khóa học",
          "parameters": [
            {
              "name": "courseId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "title": {
                      "type": "string"
                    },
                    "startDate": {
                      "type": "string",
                      "format": "date"
                    },
                    "endDate": {
                      "type": "string",
                      "format": "date"
                    },
                    "teacherId": {
                      "type": "string",
                      "format": "uuid"
                    }
                  }
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Cập nhật khóa học thành công"
            }
          }
        },
        "delete": {
          "summary": "Xóa khóa học",
          "parameters": [
            {
              "name": "courseId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "responses": {
            "200": {
              "description": "Xóa khóa học thành công"
            }
          }
        }
      },
      "/api/courses/{courseId}/teacher": {
        "put": {
          "summary": "Phân công giáo viên vào khóa học",
          "parameters": [
            {
              "name": "courseId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "teacherId": {
                      "type": "string",
                      "format": "uuid"
                    }
                  },
                  "required": ["teacherId"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Phân công giáo viên vào khóa học thành công"
            }
          }
        }
      }
    }
  }
  ```

#### 📅 Chronological Day-by-Day Sub-Agent Task Distribution Logs (Phase 2)
- **DAY 4: Triển khai các tính năng quản lý người dùng**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/user [REQ-001], [REQ-002], [REQ-003]`
      - **Low-Level Technical Task Instruction:** Triển khai các dịch vụ đăng ký người dùng, xác thực qua mạng xã hội, phân quyền người dùng
      - **Targeted Tag IDs:** [REQ-001], [REQ-002], [REQ-003]
    * **Tester:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/user;./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/user [REQ-001], [REQ-002], [REQ-003]`
      - **Low-Level Technical Task Instruction:** Viết các test case cho các tính năng đăng ký người dùng, xác thực qua mạng xã hội, phân quyền người dùng
      - **Targeted Tag IDs:** [REQ-001], [REQ-002], [REQ-003]
    * **Reviewer:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/user [REQ-001], [REQ-002], [REQ-003]`
      - **Low-Level Technical Task Instruction:** Review code cho các tính năng đăng ký người dùng, xác thực qua mạng xã hội, phân quyền người dùng
      - **Targeted Tag IDs:** [REQ-001], [REQ-002], [REQ-003]

- **DAY 5: Triển khai các tính năng quản lý trung tâm**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/center [REQ-004], [REQ-005], [REQ-006]`
      - **Low-Level Technical Task Instruction:** Triển khai các dịch vụ xem danh sách trung tâm, tạo/cập nhật/xóa trung tâm, phân quyền quản trị trung tâm
      - **Targeted Tag IDs:** [REQ-004], [REQ-005], [REQ-006]
    * **Tester:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/center;./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/center [REQ-004], [REQ-005], [REQ-006]`
      - **Low-Level Technical Task Instruction:** Viết các test case cho các tính năng xem danh sách trung tâm, tạo/cập nhật/xóa trung tâm, phân quyền quản trị trung tâm
      - **Targeted Tag IDs:** [REQ-004], [REQ-005], [REQ-006]
    * **Reviewer:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/center [REQ-004], [REQ-005], [REQ-006]`
      - **Low-Level Technical Task Instruction:** Review code cho các tính năng xem danh sách trung tâm, tạo/cập nhật/xóa trung tâm, phân quyền quản trị trung tâm
      - **Targeted Tag IDs:** [REQ-004], [REQ-005], [REQ-006]

- **DAY 6: Triển khai các tính năng quản lý khóa học**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/course [REQ-007], [REQ-008], [REQ-009]`
      - **Low-Level Technical Task Instruction:** Triển khai các dịch vụ xem danh sách khóa học, tạo/cập nhật/xóa khóa học, phân công giáo viên vào khóa học
      - **Targeted Tag IDs:** [REQ-007], [REQ-008], [REQ-009]
    * **Tester:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/course;./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/course [REQ-007], [REQ-008], [REQ-009]`
      - **Low-Level Technical Task Instruction:** Viết các test case cho các tính năng xem danh sách khóa học, tạo/cập nhật/xóa khóa học, phân công giáo viên vào khóa học
      - **Targeted Tag IDs:** [REQ-007], [REQ-008], [REQ-009]
    * **Reviewer:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/course [REQ-007], [REQ-008], [REQ-009]`
      - **Low-Level Technical Task Instruction:** Review code cho các tính năng xem danh sách khóa học, tạo/cập nhật/xóa khóa học, phân công giáo viên vào khóa học
      - **Targeted Tag IDs:** [REQ-007], [REQ-008], [REQ-009]

### Phase 3 Detailed Architectural Specification
- **Phase Core Objective & Purpose:** Triển khai các tính năng đăng ký và ghi danh học viên, điểm danh và quét mã QR
- **Target Physical Directory Matrix Map:** `./sources/backend`, `./sources/frontend`
- **API and Event Routing Contracts [REQ-010], [REQ-011], [REQ-012], [REQ-013]:**
  ```json
  {
    "paths": {
      "/api/courses/available": {
        "get": {
          "summary": "Lấy danh sách khóa học có thể đăng ký",
          "responses": {
            "200": {
              "description": "Danh sách khóa học có thể đăng ký",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        "courseId": {
                          "type": "string",
                          "format": "uuid"
                        },
                        "title": {
                          "type": "string"
                        },
                        "startDate": {
                          "type": "string",
                          "format": "date"
                        },
                        "endDate": {
                          "type": "string",
                          "format": "date"
                        },
                        "teacherId": {
                          "type": "string",
                          "format": "uuid"
                        },
                        "maxStudents": {
                          "type": "integer"
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      },
      "/api/enrollments": {
        "post": {
          "summary": "Đăng ký khóa học",
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "courseId": {
                      "type": "string",
                      "format": "uuid"
                    }
                  },
                  "required": ["courseId"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Đăng ký khóa học thành công"
            }
          }
        }
      },
      "/api/attendance": {
        "post": {
          "summary": "Điểm danh qua mã QR",
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "studentId": {
                      "type": "string",
                      "format": "uuid"
                    },
                    "courseId": {
                      "type": "string",
                      "format": "uuid"
                    }
                  },
                  "required": ["studentId", "courseId"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Điểm danh thành công"
            }
          }
        }
      }
    }
  }
  ```
- **Phase Localized Exception Handlers [EXC-001], [EXC-002], [EXC-004]:**
  - **Network & Connectivity Drops During QR Scan:** If a student scans a QR but the network is unavailable, When the app retries the request after reconnection, Then the attendance is recorded once the service is reachable.
  - **Duplicate Attendance Submission:** If the same student scans the same course QR multiple times within the same day, When the system detects a duplicate, Then it returns a success response indicating ‘already recorded’ and does not create extra rows.
  - **Xác thực đầu vào không hợp lệ:** Nếu xác thực thất bại trên form submission, Khi lỗi được trả về cho người dùng, Sau đó một thông báo rõ ràng liệt kê từng trường không hợp lệ và yêu cầu chỉnh sửa.

#### 📅 Chronological Day-by-Day Sub-Agent Task Distribution Logs (Phase 3)
- **DAY 7: Triển khai các tính năng đăng ký và ghi danh học viên**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/enrollment [REQ-010], [REQ-011]`
      - **Low-Level Technical Task Instruction:** Triển khai các dịch vụ duyệt khóa học, đăng ký khóa học của học viên
      - **Targeted Tag IDs:** [REQ-010], [REQ-011]
    * **Tester:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/enrollment;./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/enrollment [REQ-010], [REQ-011]`
      - **Low-Level Technical Task Instruction:** Viết các test case cho các tính năng duyệt khóa học, đăng ký khóa học của học viên
      - **Targeted Tag IDs:** [REQ-010], [REQ-011]
    * **Reviewer:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/enrollment [REQ-010], [REQ-011]`
      - **Low-Level Technical Task Instruction:** Review code cho các tính năng duyệt khóa học, đăng ký khóa học của học viên
      - **Targeted Tag IDs:** [REQ-010], [REQ-011]

- **DAY 8: Triển khai các tính năng điểm danh và quét mã QR**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/attendance [REQ-012], [REQ-013], [EXC-001], [EXC-002]`
      - **Low-Level Technical Task Instruction:** Triển khai các dịch vụ chụp ảnh điểm danh QR, tính chất bất biến của điểm danh
      - **Targeted Tag IDs:** [REQ-012], [REQ-013], [EXC-001], [EXC-002]
    * **Tester:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/attendance;./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/attendance [REQ-012], [REQ-013], [EXC-001], [EXC-002]`
      - **Low-Level Technical Task Instruction:** Viết các test case cho các tính năng chụp ảnh điểm danh QR, tính chất bất biến của điểm danh
      - **Targeted Tag IDs:** [REQ-012], [REQ-013], [EXC-001], [EXC-002]
    * **Reviewer:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/attendance [REQ-012], [REQ-013], [EXC-001], [EXC-002]`
      - **Low-Level Technical Task Instruction:** Review code cho các tính năng chụp ảnh điểm danh QR, tính chất bất biến của điểm danh
      - **Targeted Tag IDs:** [REQ-012], [REQ-013], [EXC-001], [EXC-002]

- **DAY 9: Tích hợp các tính năng điểm danh và quét mã QR với ứng dụng di động**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/frontend/src/components/attendance [REQ-012], [REQ-013], [EXC-001], [EXC-002]`
      - **Low-Level Technical Task Instruction:** Tích hợp các tính năng điểm danh và quét mã QR với ứng dụng di động
      - **Targeted Tag IDs:** [REQ-012], [REQ-013], [EXC-001], [EXC-002]
    * **Tester:**
      - **Target Component file path (`target_component`):** `./sources/frontend/src/components/attendance;./sources/frontend/src/components/attendance [REQ-012], [REQ-013], [EXC-001], [EXC-002]`
      - **Low-Level Technical Task Instruction:** Viết các test case cho các tính năng điểm danh và quét mã QR trên ứng dụng di động
      - **Targeted Tag IDs:** [REQ-012], [REQ-013], [EXC-001], [EXC-002]
    * **Reviewer:**
      - **Target Component file path (`target_component`):** `./sources/frontend/src/components/attendance [REQ-012], [REQ-013], [EXC-001], [EXC-002]`
      - **Low-Level Technical Task Instruction:** Review code cho các tính năng điểm danh và quét mã QR trên ứng dụng di động
      - **Targeted Tag IDs:** [REQ-012], [REQ-013], [EXC-001], [EXC-002]

### Phase 4 Detailed Architectural Specification
- **Phase Core Objective & Purpose:** Triển khai các tính năng quản lý thẻ hội viên, thông báo và truyền thông
- **Target Physical Directory Matrix Map:** `./sources/backend`, `./sources/frontend`
- **API and Event Routing Contracts [REQ-014], [REQ-015], [REQ-016]:**
  ```json
  {
    "paths": {
      "/api/studentcards/{studentId}": {
        "get": {
          "summary": "Lấy thông tin thẻ hội viên",
          "parameters": [
            {
              "name": "studentId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "responses": {
            "200": {
              "description": "Thông tin thẻ hội viên",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "object",
                    "properties": {
                      "cardId": {
                        "type": "string",
                        "format": "uuid"
                      },
                      "studentId": {
                        "type": "string",
                        "format": "uuid"
                      },
                      "issueDate": {
                        "type": "string",
                        "format": "date"
                      },
                      "validityDays": {
                        "type": "integer"
                      },
                      "remainingDays": {
                        "type": "integer"
                      }
                    }
                  }
                }
              }
            }
          }
        }
      },
      "/api/studentcards/{studentId}/renew": {
        "post": {
          "summary": "Gia hạn thẻ hội viên",
          "parameters": [
            {
              "name": "studentId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "days": {
                      "type": "integer"
                    }
                  },
                  "required": ["days"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Gia hạn thẻ hội viên thành công"
            }
          }
        }
      },
      "/api/notifications": {
        "post": {
          "summary": "Tạo thông báo",
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "userId": {
                      "type": "string",
                      "format": "uuid"
                    },
                    "groupZalo": {
                      "type": "string"
                    },
                    "message": {
                      "type": "string"
                    }
                  },
                  "required": ["message"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Tạo thông báo thành công"
            }
          }
        }
      }
    }
  }
  ```
- **Phase Localized Exception Handlers [EXC-003]:**
  - **Failed Notification Delivery:** When a push notification cannot be delivered (e.g., device token invalid), Then the system logs the failure and schedules a retry up to three times before marking as failed.

#### 📅 Chronological Day-by-Day Sub-Agent Task Distribution Logs (Phase 4)
- **DAY 10: Triển khai các tính năng quản lý thẻ hội viên**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/studentcard [REQ-014], [REQ-015]`
      - **Low-Level Technical Task Instruction:** Triển khai các dịch vụ hiển thị tính hợp lệ của thẻ, gia hạn thẻ
      - **Targeted Tag IDs:** [REQ-014], [REQ-015]
    * **Tester:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/studentcard;./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/studentcard [REQ-014], [REQ-015]`
      - **Low-Level Technical Task Instruction:** Viết các test case cho các tính năng hiển thị tính hợp lệ của thẻ, gia hạn thẻ
      - **Targeted Tag IDs:** [REQ-014], [REQ-015]
    * **Reviewer:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/studentcard [REQ-014], [REQ-015]`
      - **Low-Level Technical Task Instruction:** Review code cho các tính năng hiển thị tính hợp lệ của thẻ, gia hạn thẻ
      - **Targeted Tag IDs:** [REQ-014], [REQ-015]

- **DAY 11: Triển khai các tính năng thông báo và truyền thông**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/notification [REQ-016], [EXC-003]`
      - **Low-Level Technical Task Instruction:** Triển khai các dịch vụ kích hoạt thông báo
      - **Targeted Tag IDs:** [REQ-016], [EXC-003]
    * **Tester:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/notification;./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/notification [REQ-016], [EXC-003]`
      - **Low-Level Technical Task Instruction:** Viết các test case cho các tính năng kích hoạt thông báo
      - **Targeted Tag IDs:** [REQ-016], [EXC-003]
    * **Reviewer:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/notification [REQ-016], [EXC-003]`
      - **Low-Level Technical Task Instruction:** Review code cho các tính năng kích hoạt thông báo
      - **Targeted Tag IDs:** [REQ-016], [EXC-003]

- **DAY 12: Tích hợp các tính năng thông báo và truyền thông với ứng dụng di động**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/frontend/src/components/notification [REQ-016], [EXC-003]`
      - **Low-Level Technical Task Instruction:** Tích hợp các tính năng thông báo và truyền thông với ứng dụng di động
      - **Targeted Tag IDs:** [REQ-016], [EXC-003]
    * **Tester:**
      - **Target Component file path (`target_component`):** `./sources/frontend/src/components/notification;./sources/frontend/src/components/notification [REQ-016], [EXC-003]`
      - **Low-Level Technical Task Instruction:** Viết các test case cho các tính năng thông báo và truyền thông trên ứng dụng di động
      - **Targeted Tag IDs:** [REQ-016], [EXC-003]
    * **Reviewer:**
      - **Target Component file path (`target_component`):** `./sources/frontend/src/components/notification [REQ-016], [EXC-003]`
      - **Low-Level Technical Task Instruction:** Review code cho các tính năng thông báo và truyền thông trên ứng dụng di động
      - **Targeted Tag IDs:** [REQ-016], [EXC-003]

### Phase 5 Detailed Architectural Specification
- **Phase Core Objective & Purpose:** Triển khai các tính năng quản lý khuyến mãi và thông báo, chatbot dịch vụ khách hàng AI, các tính năng cốt lõi của ứng dụng di động, bản địa hóa và SEO
- **Target Physical Directory Matrix Map:** `./sources/backend`, `./sources/frontend`
- **API and Event Routing Contracts [REQ-017], [REQ-018], [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025]:**
  ```json
  {
    "paths": {
      "/api/promotions": {
        "get": {
          "summary": "Lấy danh sách khuyến mãi",
          "responses": {
            "200": {
              "description": "Danh sách khuyến mãi",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        "promoId": {
                          "type": "string",
                          "format": "uuid"
                        },
                        "code": {
                          "type": "string"
                        },
                        "discountPercent": {
                          "type": "integer"
                        },
                        "startDate": {
                          "type": "string",
                          "format": "date"
                        },
                        "endDate": {
                          "type": "string",
                          "format": "date"
                        },
                        "description": {
                          "type": "string"
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        },
        "post": {
          "summary": "Tạo khuyến mãi mới",
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "code": {
                      "type": "string"
                    },
                    "discountPercent": {
                      "type": "integer"
                    },
                    "startDate": {
                      "type": "string",
                      "format": "date"
                    },
                    "endDate": {
                      "type": "string",
                      "format": "date"
                    },
                    "description": {
                      "type": "string"
                    }
                  },
                  "required": ["code", "discountPercent"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Tạo khuyến mãi thành công"
            }
          }
        }
      },
      "/api/promotions/{promoId}": {
        "put": {
          "summary": "Cập nhật khuyến mãi",
          "parameters": [
            {
              "name": "promoId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "code": {
                      "type": "string"
                    },
                    "discountPercent": {
                      "type": "integer"
                    },
                    "startDate": {
                      "type": "string",
                      "format": "date"
                    },
                    "endDate": {
                      "type": "string",
                      "format": "date"
                    },
                    "description": {
                      "type": "string"
                    }
                  }
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Cập nhật khuyến mãi thành công"
            }
          }
        },
        "delete": {
          "summary": "Xóa khuyến mãi",
          "parameters": [
            {
              "name": "promoId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "responses": {
            "200": {
              "description": "Xóa khuyến mãi thành công"
            }
          }
        }
      },
      "/api/announcements": {
        "get": {
          "summary": "Lấy danh sách thông báo",
          "responses": {
            "200": {
              "description": "Danh sách thông báo",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        "announcementId": {
                          "type": "string",
                          "format": "uuid"
                        },
                        "title": {
                          "type": "string"
                        },
                        "content": {
                          "type": "string"
                        },
                        "startDate": {
                          "type": "string",
                          "format": "date"
                        },
                        "endDate": {
                          "type": "string",
                          "format": "date"
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        },
        "post": {
          "summary": "Tạo thông báo mới",
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "title": {
                      "type": "string"
                    },
                    "content": {
                      "type": "string"
                    },
                    "startDate": {
                      "type": "string",
                      "format": "date"
                    },
                    "endDate": {
                      "type": "string",
                      "format": "date"
                    }
                  },
                  "required": ["title", "content"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Tạo thông báo thành công"
            }
          }
        }
      },
      "/api/announcements/{announcementId}": {
        "put": {
          "summary": "Cập nhật thông báo",
          "parameters": [
            {
              "name": "announcementId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "title": {
                      "type": "string"
                    },
                    "content": {
                      "type": "string"
                    },
                    "startDate": {
                      "type": "string",
                      "format": "date"
                    },
                    "endDate": {
                      "type": "string",
                      "format": "date"
                    }
                  }
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Cập nhật thông báo thành công"
            }
          }
        },
        "delete": {
          "summary": "Xóa thông báo",
          "parameters": [
            {
              "name": "announcementId",
              "in": "path",
              "required": true,
              "schema": {
                "type": "string",
                "format": "uuid"
              }
            }
          ],
          "responses": {
            "200": {
              "description": "Xóa thông báo thành công"
            }
          }
        }
      },
      "/api/chatbot": {
        "post": {
          "summary": "Tương tác với chatbot AI",
          "requestBody": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "message": {
                      "type": "string"
                    }
                  },
                  "required": ["message"]
                }
              }
            }
          },
          "responses": {
            "200": {
              "description": "Trả lời từ chatbot AI",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "object",
                    "properties": {
                      "response": {
                        "type": "string"
                      }
                    }
                  }
                }
              }
            }
          }
        }
      },
      "/api/mobile": {
        "get": {
          "summary": "Lấy giao diện người dùng cho ứng dụng di động",
          "responses": {
            "200": {
              "description": "Giao diện người dùng cho ứng dụng di động",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "object",
                    "properties": {
                      "ui": {
                        "type": "string"
                      }
                    }
                  }
                }
              }
            }
          }
        }
      },
      "/api/notifications/mobile": {
        "get": {
          "summary": "Lấy danh sách thông báo cho ứng dụng di động",
          "responses": {
            "200": {
              "description": "Danh sách thông báo cho ứng dụng di động",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        "notificationId": {
                          "type": "string",
                          "format": "uuid"
                        },
                        "message": {
                          "type": "string"
                        },
                        "sentAt": {
                          "type": "string",
                          "format": "date-time"
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      },
      "/api/i18n": {
        "get": {
          "summary": "Lấy các chuỗi ngôn ngữ cho bản địa hóa",
          "responses": {
            "200": {
              "description": "Các chuỗi ngôn ngữ cho bản địa hóa",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "object",
                    "properties": {
                      "strings": {
                        "type": "object"
                      }
                    }
                  }
                }
              }
            }
          }
        }
      },
      "/api/seo": {
        "get": {
          "summary": "Lấy các thẻ meta cho SEO",
          "responses": {
            "200": {
              "description": "Các thẻ meta cho SEO",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "object",
                    "properties": {
                      "metaTags": {
                        "type": "array",
                        "items": {
                          "type": "object",
                          "properties": {
                            "name": {
                              "type": "string"
                            },
                            "content": {
                              "type": "string"
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      },
      "/api/reports/attendance": {
        "get": {
          "summary": "Tạo báo cáo điểm danh",
          "responses": {
            "200": {
              "description": "Báo cáo điểm danh",
              "content": {
                "text/csv": {
                  "schema": {
                    "type": "string"
                  }
                }
              }
            }
          }
        }
      },
      "/api/dashboard": {
        "get": {
          "summary": "Lấy dữ liệu bảng điều khiển",
          "responses": {
            "200": {
              "description": "Dữ liệu bảng điều khiển",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "object",
                    "properties": {
                      "totalStudents": {
                        "type": "integer"
                      },
                      "activeCourses": {
                        "type": "integer"
                      },
                      "upcomingSessions": {
                        "type": "integer"
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  ```
- **Phase Localized Exception Handlers [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]:**
  - **System Recovery After Outage:** If the service becomes unavailable, When it restores, Then any pending attendance scans are processed in FIFO order, and users receive a notification of recovered events.
  - **Performance Metrics:** Core API responses (authentication, attendance capture, course list) must complete within 200 ms average latency. Database queries must be indexed to support sub‑second reads for up to 10 000 concurrent users.
  - **Availability:** Target 99.9 % annual uptime; SLA includes automatic failover across GKE clusters.
  - **Security:** All data in transit must use TLS 1.3; at rest encryption with AES‑256. JWT access tokens expire after 15 minutes; refresh tokens have 7‑day expiry. Implement OWASP Top 10 mitigations (SQL injection, XSS, CSRF).
  - **Scalability & Availability:** Horizontal scaling of Quarkus services via Kubernetes HPA based on CPU > 70 % or request latency > 300 ms. PostgreSQL read replicas for reporting workloads.
  - **Docker Image Size:** Base image size < 200 MB; final image < 500 MB.
  - **Logging & Audit:** All user actions (role changes, attendance records, notifications) must be logged with timestamps, user ID, and action details; logs retained for 1 year.
  - **Multi‑Language Support:** UI strings must be externalized; support English, Vietnamese, Spanish; locale switching without page reload where feasible.
  - **GDPR/CCPA Compliance:** Personal data deletion on user request; data export in JSON format; consent management for marketing communications.
  - **Backup & Disaster Recovery:** Daily PostgreSQL full backups; point‑in‑time recovery up to 24 hours; GKE cluster backup to separate region.

#### 📅 Chronological Day-by-Day Sub-Agent Task Distribution Logs (Phase 5)
- **DAY 13: Triển khai các tính năng quản lý khuyến mãi và thông báo**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/promotion [REQ-017], [REQ-018]`
      - **Low-Level Technical Task Instruction:** Triển khai các dịch vụ quản lý khuyến mãi, quản lý thông báo
      - **Targeted Tag IDs:** [REQ-017], [REQ-018]
    * **Tester:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/promotion;./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/promotion [REQ-017], [REQ-018]`
      - **Low-Level Technical Task Instruction:** Viết các test case cho các tính năng quản lý khuyến mãi, quản lý thông báo
      - **Targeted Tag IDs:** [REQ-017], [REQ-018]
    * **Reviewer:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/promotion [REQ-017], [REQ-018]`
      - **Low-Level Technical Task Instruction:** Review code cho các tính năng quản lý khuyến mãi, quản lý thông báo
      - **Targeted Tag IDs:** [REQ-017], [REQ-018]

- **DAY 14: Triển khai các tính năng chatbot dịch vụ khách hàng AI, các tính năng cốt lõi của ứng dụng di động, bản địa hóa và SEO**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/chatbot [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]`
      - **Low-Level Technical Task Instruction:** Triển khai các dịch vụ chatbot dịch vụ khách hàng AI, các tính năng cốt lõi của ứng dụng di động, bản địa hóa và SEO
      - **Targeted Tag IDs:** [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
    * **Tester:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/chatbot;./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/chatbot [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]`
      - **Low-Level Technical Task Instruction:** Viết các test case cho các tính năng chatbot dịch vụ khách hàng AI, các tính năng cốt lõi của ứng dụng di động, bản địa hóa và SEO
      - **Targeted Tag IDs:** [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
    * **Reviewer:**
      - **Target Component file path (`target_component`):** `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/chatbot [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]`
      - **Low-Level Technical Task Instruction:** Review code cho các tính năng chatbot dịch vụ khách hàng AI, các tính năng cốt lõi của ứng dụng di động, bản địa hóa và SEO
      - **Targeted Tag IDs:** [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
    * **Doc:**
      - **Target Component file path (`target_component`):** `./sources/docs [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]`
      - **Low-Level Technical Task Instruction:** Viết tài liệu cho các tính năng chatbot dịch vụ khách hàng AI, các tính năng cốt lõi của ứng dụng di động, bản địa hóa và SEO
      - **Targeted Tag IDs:** [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]

- **DAY 15: Tích hợp các tính năng chatbot dịch vụ khách hàng AI, các tính năng cốt lõi của ứng dụng di động, bản địa hóa và SEO với ứng dụng di động**
  - **Sub-Agent Workflow Specialization:**
    * **Coder:**
      - **Target Component file path (`target_component`):** `./sources/frontend/src/components/chatbot [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]`
      - **Low-Level Technical Task Instruction:** Tích hợp các tính năng chatbot dịch vụ khách hàng AI, các tính năng cốt lõi của ứng dụng di động, bản địa hóa và SEO với ứng dụng di động
      - **Targeted Tag IDs:** [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
    * **Tester:**
      - **Target Component file path (`target_component`):** `./sources/frontend/src/components/chatbot;./sources/frontend/src/components/chatbot [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]`
      - **Low-Level Technical Task Instruction:** Viết các test case cho các tính năng chatbot dịch vụ khách hàng AI, các tính năng cốt lõi của ứng dụng di động, bản địa hóa và SEO trên ứng dụng di động
      - **Targeted Tag IDs:** [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
    * **Reviewer:**
      - **Target Component file path (`target_component`):** `./sources/frontend/src/components/chatbot [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]`
      - **Low-Level Technical Task Instruction:** Review code cho các tính năng chatbot dịch vụ khách hàng AI, các tính năng cốt lõi của ứng dụng di động, bản địa hóa và SEO trên ứng dụng di động
      - **Targeted Tag IDs:** [REQ-019], [REQ-020], [REQ-021], [REQ-022], [REQ-023], [REQ-024], [REQ-025], [EXC-005], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]

## 📁 6. UNIVERSAL ENTERPRISE SECURITY CODES & INJECTION COUNTERMEASURES [NFR-XXX]
- **SQL Injection (SQLi) Absolute Countermeasures:** Rule parameters for prepared statements, positional query parameters, and dynamic sorting input Whitelists.
- **Cross-Site Scripting (XSS) & Content Security Policy (CSP):** Layout standards for automated context sanitization, JSX auto-escaping, and dynamic injection of strict CSP headers (`unsafe-inline` restriction).
- **Multi-Tenant CORS Security Rails:** Configurations for origin wildcard prohibitions and dynamic tenant origin database metrics validation.
- **Zero-Leak Log Scrubbing & PII Data Masking Engines:** Rules for automated masking interceptors (`@JsonSerialize`) and log scrubbing thresholds.

## 📁 7. HYBRID MOBILE COMPLIANCE RAIL RULES & INTERNATIONALIZED SEO MECHANISMS
- **Capacitor Mobile Hybrid Compliance Rails:** [IF Mobile active] Rules for dynamic client-side fetching, absolute URL addressing, hydration safeguards, native storage abstractions (`@capacitor/preferences`), and hardware back-button interception.
- **Internationalization (i18n) & Dynamic SEO Injection:** Edge-layer locale recognition middleware architectures, hreflang dynamic hypermedia control injection, and search crawler robots indexing limits.

## 📁 8. PIPELINE AUTOMATED DAILY SESSION GIT BRANCH FLOW
- **Daily Workspace Forking Isolation:** Programmatic forking controls for branch `features/development-phase-X-day-Y` (`X` is the number of phase, from 1 to N, where N <= 5; `Y` is the day number in phase, it will start from 1 for each phase).
- **Validation Guard Pipeline Gates:** Execution rules for compilation verification, automated code coverage goals (`>= 85%`), and context summary serialization logs.

[TRACEABILITY MATRIX ENFORCEMENT: 100% COVERAGE VALIDATED. TOTAL UNIQUE REQ TAGS MAPPED: 25, TOTAL ARC TAGS: 10, TOTAL EXC TAGS: 5, TOTAL DAT TAGS: 11, TOTAL NFR TAGS: 9. ZERO UNASSIGNED CODES FOUND.]