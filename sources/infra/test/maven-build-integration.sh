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