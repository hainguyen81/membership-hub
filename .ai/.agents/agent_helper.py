# ==============================================================================
# 🛠️ ENTERPRISE PIPELINE ENVIRONMENT-BASED PATH RESOLVER
# ==============================================================================
# Programmatically retrieves the absolute root directory of the active project 
# using GitHub Actions infrastructure environment tokens instead of brittle backtracking.
# ==============================================================================

import os

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
