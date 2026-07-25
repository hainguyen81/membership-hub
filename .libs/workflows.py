import os
import sys
import runpy
from pathlib import Path

from modules import FolderPackageFinder

GITHUB_WORKFLOWS_SCRIPTS_PATH = ".github/scripts"

# load GitHub workflow scripts folder as python package
def registerGitHubWorkflows():
    github_workflows_scripts_path = Path(GITHUB_WORKFLOWS_SCRIPTS_PATH).resolve()
    if github_workflows_scripts_path.is_dir():
        githubWorkflowsScriptsPackageFinder = FolderPackageFinder(str(github_workflows_scripts_path));
        sys.meta_path.append(githubWorkflowsScriptsPackageFinder)
        print(f"✅ Registered {github_workflows_scripts_path} to sys.meta_path for finding with alias: {githubWorkflowsScriptsPackageFinder.alias()}")

    else:
        print(f"⛔ Not found GH Workflows Scripts {GITHUB_WORKFLOWS_SCRIPTS_PATH} to register package/module finder")