# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/infra/test/maven-build-integration.sh
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/infra/test/maven-build-integration.sh` (Must map to sources/backend/ or sources/frontend/)




### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/infra/test/maven-build-integration.sh. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo tệp ./sources/infra/test/maven-build-integration.sh chứa kịch bản bash kiểm thử tích hợp. Kịch bản phải thực hiện mvn clean validate tại thư mục ./sources/backend/ để xác nhận tất cả 6 descriptor pom.xml vi dịch vụ tải và phân giải đúng các dependency từ BOM Quarkus 3.15.1. Thoát với mã 0 nếu thành công, mã khác 0 nếu thất bại. In log rõ ràng cho mỗi vi dịch vụ.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```bash
#!/usr/bin/env bash
# =====================================================================
# 🏛️ MASTER ENTERPRISE GOVERNANCE GUARDRAILS & TRACEABILITY HEADER
# Target Component Path: ./sources/infra/test/maven-build-integration.sh
# Enterprise Identity: membership-hub (org.nlh4j.membershiphub)
# Active Traceability Tags: [ARC-000]
# Business Context: Automated integration test script validating multi-module
#                   Maven descriptor structures and Quarkus 3.15.1 BOM dependency resolution.
# =====================================================================

set -euo pipefail

# [REQ-000] Top-level immutable configuration constants
readonly BACKEND_ROOT="./sources/backend"
readonly REQUIRED_SERVICES=(
    "user-service"
    "center-service"
    "course-service"
    "attendance-service"
    "notification-service"
    "reporting-service"
)
readonly MAVEN_COMMAND="mvn clean validate -B -q"

# [REQ-000] Enterprise logging utility with ISO-8601 timestamps and subsystem correlation
log_info() {
    echo "[INFO] [$(date -u +"%Y-%m-%dT%H:%M:%SZ")] [ARC-000] $1"
}

log_error() {
    echo "[CRITICAL FAIL] [$(date -u +"%Y-%m-%dT%H:%M:%SZ")] [ARC-000] $1" >&2
}

log_success() {
    echo "[SUCCESS] [$(date -u +"%Y-%m-%dT%H:%M:%SZ")] [ARC-000] $1"
}

# [ARC-000] Entry gate validation for workspace topology
log_info "Initializing multi-module Maven build integration verification..."

if [ ! -d "${BACKEND_ROOT}" ]; then
    log_error "Target backend root directory does not exist: ${BACKEND_ROOT}"
    exit 1
fi

# [ARC-000] Execute clean validation at the root backend aggregator layer
log_info "Executing parent POM validation and transitive dependency resolution..."
cd "${BACKEND_ROOT}"

if ! ${MAVEN_COMMAND}; then
    log_error "Parent aggregator POM validation or Quarkus 3.15.1 BOM resolution failed."
    exit 2
fi

log_success "Parent POM validation passed successfully."

# [ARC-000] Granular verification loop across all microservice descriptors
for SERVICE in "${REQUIRED_SERVICES[@]}"; do
    log_info "Verifying service descriptor integrity for subsystem: ${SERVICE}"
    
    if [ -f "${SERVICE}/pom.xml" ]; then
        log_info "Descriptor pom.xml verified present for service: ${SERVICE}"
    else
        log_error "Missing required pom.xml descriptor for microservice: ${SERVICE}"
        exit 3
    fi
done

log_success "All 6 microservice descriptors successfully verified and resolved."
log_info "Multi-module integration validation completed with zero failures."
exit 0
```

