# ==============================================================================
# 🛠️ ENTERPRISE PIPELINE ENVIRONMENT-BASED PATH RESOLVER
# ==============================================================================
# Programmatically retrieves the absolute root directory of the active project 
# using GitHub Actions infrastructure environment tokens instead of brittle backtracking.
# ==============================================================================

import os
import json

def resolve_absolute_path(relative_target_path):
    """
    Ingests a relative path string and safely interpolates it using the absolute 
    workspace anchor provided natively by the GitHub Actions Runner environment.
    """
    # 🚀 CORE RAIL: Ingest the absolute repository root path straight from GitHub infrastructure
    # Fallback to current working directory (os.getcwd()) if executing on a local machine
    current_directory_path = os.getcwd()
    github_workspace = os.environ.get("GITHUB_WORKSPACE", '')
    project_workspace = os.environ.get("PROJECT_WORKSPACE", '')
    print(f"CURRENT WORKING DIR: { current_directory_path } | GITHUB_WORKSPACE: { github_workspace } | PROJECT_WORKSPACE: { project_workspace }")
    repo_root_path = os.environ.get("PROJECT_WORKSPACE", os.environ.get("GITHUB_WORKSPACE", os.getcwd()))
    
    # Clean up the incoming string parameters by removing leading path descriptors
    cleaned_relative_path = relative_target_path.removeprefix("./")
    
    # Synthesize the non-negotiable absolute hardware computing path destinations
    absolute_hardware_path = os.path.join(repo_root_path, cleaned_relative_path)
    
    # full path from root workspace
    return absolute_hardware_path

def json_raw_content(raw_content):
    """Securely serialize input telemetry payloads into structural double-quoted strings."""
    # If the payload is already a memory object list or dictionary
    if isinstance(raw_content, (dict, list)):
        return json.dumps(raw_content, indent=4, ensure_ascii=False)
    
    if isinstance(raw_content, str):
        cleaned_str = raw_content.strip()
        # If it is a stringified JSON layout, decode and encode with indentation rules
        if (cleaned_str.startswith("{") or cleaned_str.startswith("[")) and '"' in cleaned_str:
            try:
                return json.dumps(json.loads(cleaned_str), indent=4, ensure_ascii=False)
            except Exception:
                pass
    
    return str(raw_content)
