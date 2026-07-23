import os
import sys
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
