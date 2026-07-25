import os
import sys
import runpy
from pathlib import Path

from modules import register_packages

# load current folder as python package
AGENTS_PACKAGE_PATH = os.environ.get('AGENTS_PACKAGE_PATH')

# load project workspace as python package
PROJECT_WORKSPACE_PACKAGE_PATH = os.environ.get('PROJECT_WORKSPACE')

# load GitHub workflow scripts folder as python package
GITHUB_WORKFLOWS_SCRIPTS_PACKAGE_PATH = ".github/scripts"

# register packages
register_packages([
    AGENTS_PACKAGE_PATH,
    PROJECT_WORKSPACE_PACKAGE_PATH,
    GITHUB_WORKFLOWS_SCRIPTS_PACKAGE_PATH
])

# Check whether need to launch module
if len(sys.argv) > 1:
    # parse module name to run
    target_module = sys.argv[1]
    # cut 'launcher.py' name and keep all arguments of module to run
    sys.argv = sys.argv[1:]
    # run module with arguments from command-line (excluded launcher) in same process
    print(f"⚙🚀 Launching module {target_module} with arguments: { sys.argv }...")
    runpy.run_module(target_module, run_name='__main__')