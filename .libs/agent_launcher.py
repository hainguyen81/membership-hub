import os
import sys
import runpy
from pathlib import Path

from modules import FolderPackageFinder

# load current folder as python package
agents_package_path = os.environ.get('AGENTS_PACKAGE_PATH')
phys_agents_package_path = Path(agents_package_path).resolve()
if phys_agents_package_path.is_dir():
    agentsPackageFinder = FolderPackageFinder(str(phys_agents_package_path));
    sys.meta_path.insert(0, agentsPackageFinder)
    print(f"✅ Registered {agents_package_path} to sys.meta_path for finding with alias: {agentsPackageFinder.alias()}")

else:
    print(f"⛔ Not define enviroment 'AGENTS_PACKAGE_PATH' to register package/module finder")

# load project workspace as python package
project_workspace_path = os.environ.get('PROJECT_WORKSPACE')
phys_project_workspace_path = Path(project_workspace_path).resolve()
if phys_project_workspace_path.is_dir():
    projectWorkspacePackageFinder = FolderPackageFinder(str(phys_project_workspace_path));
    sys.meta_path.insert(0, projectWorkspacePackageFinder)
    print(f"✅ Registered {project_workspace_path} to sys.meta_path for finding with alias: {projectWorkspacePackageFinder.alias()}")

else:
    print(f"⛔ Not define enviroment 'AGENTS_PACKAGE_PATH' to register package/module finder")

# Check whether need to launch module
if len(sys.argv) > 1:
    # parse module name to run
    target_module = sys.argv[1]
    # cut 'launcher.py' name and keep all arguments of module to run
    sys.argv = sys.argv[1:]
    # run module with arguments from command-line (excluded launcher) in same process
    print(f"⚙🚀 Launching module {target_module} with arguments: { sys.argv }...")
    runpy.run_module(target_module, run_name='__main__')