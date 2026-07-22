### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: {{ project_name }}
*   Enforced Java Package Prefix Base: org.nlh4j.saas.{{ project_name }}
*   Target Component Destination Path: `{{ target_component }}` (Must map to sources/backend/ or sources/frontend/)
*   Context Module Context Reference Path: `{{ source_component }}`

### 📁 SOURCE CODE UNDER AUDIT (VERIFICATION TARGET)
The following code block is the implementation that requires direct security review or immediate compilation patch:
{{ source_payload }}

{% if compiler_error_logs and compiler_error_logs.strip() != "" %}
### ❌ REAL RAW COMPILER ERROR LOGS (CRITICAL FIX TARGET)
The codebase above triggered the following compiler or runtime exceptions. You MUST analyze this stack trace or log error text to pinpoint and auto-patch the root cause:
```text
{{ compiler_error_logs }}
```
*   Operational Modality Activated: COMPILER_FIXER_MODE
{% else %}
### 🔍 ARCHITECTURAL HARDENING DIRECTIVE
*   Operational Modality Activated: STATIC_ANALYSIS_&_SECURITY_REVIEW_MODE
[INSTRUCTION FOR AI: No compilation error logs provided. Perform deep static analysis on the source code above. Refactor the code if it violates memory safety, multi-tenancy isolation boundaries, encryption of private fields like CCCD/Phone, or database calculation principles.]
{% endif %}

### 📋 EXECUTION SUB-TASKS TO ENFORCE
{{ sub_tasks }}

---

### ⚙️ LEAD REVIEWER/FIXER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, sub-task specifications, and potential compiler log streams provided above. Automatically evaluate the error signatures or static code anomalies to construct either a fully patched backend Java component or a frontend TypeScript/Next.js asset. Ensure the final code is completely self-contained and compiler-ready. Emit the production source code inside a single valid markdown code block now.
