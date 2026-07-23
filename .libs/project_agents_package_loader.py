import os
from pathlib import Path

from modules import load_folder_as_package

def load_project_agents_package():
    AGENTS_PACKAGE_PATH = os.environ.get('AGENTS_PACKAGE_PATH')
    agents_folder = Path(AGENTS_PACKAGE_PATH).resolve()
    if agents_folder.is_dir():
        agents_package = load_folder_as_package(str(agents_folder))
    
    else:
        print(f"❌ Not found enviroment 'AGENTS_PACKAGE_PATH' or its value is not an existing folder to load...")

# load agents package
load_project_agents_package()
