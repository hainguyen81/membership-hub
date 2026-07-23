import os
import importlib
from pathlib import Path

from modules import load_folder_as_package

# load all agents folder by enviroment 'AGENTS_PACKAGE_PATH' as python package
def load_project_agents_package():
    AGENTS_PACKAGE_PATH = os.environ.get('AGENTS_PACKAGE_PATH')
    agents_folder = Path(AGENTS_PACKAGE_PATH).resolve()
    if agents_folder.is_dir():
        # load enviroment agents package
        return load_folder_as_package(str(agents_folder))
    
    else:
        print(f"❌ Not found enviroment 'AGENTS_PACKAGE_PATH' or its value is not an existing folder to load...")
        return None

# load agents package
if __name__ == "__main__":
    agents_package = load_project_agents_package()
    if agents_package:
        importlib.import_module(agents_package.__name__)
        print(f"===> ✅ Successfully loaded and verified package: {agents_package.__name__}")
