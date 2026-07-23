import os
import sys
import importlib
import importlib.util
from pathlib import Path
from importlib.abc import MetaPathFinder, Loader
from importlib.machinery import ModuleSpec

class FolderPackageFinder(MetaPathFinder):
    def __init__(self, folder_path):
        self.folder_path = Path(folder_path).resolve()
        if not self.folder_path.is_dir():
            raise FileNotFoundError(f"Not found folder path: {self.folder_path}")
        
        # 1. Define root Package Alias
        self.root_alias = self.folder_path.name.replace('.', '_')
        print(f"📦 Custom Finder registered for folder '{self.folder_path.name}' as alias '{self.root_alias}'")
    
    def alias(self) -> str:
        return self.root_alias
    
    # find spec
    def find_spec(self, fullname, path, target=None):
        # Support python -m including trap extension .__main__
        is_main = False
        search_name = fullname
        if fullname.endswith(".__main__"):
            is_main = True
            search_name = fullname[:-9] # split ".__main__" to calculate relative path

        # if module doesn;t start with root alias; then ignoring
        if not search_name.startswith(self.root_alias):
            # print(f"⛔ (1) Package/Module {search_name} not matching with registered root package: {self.root_alias}")
            return None

        # 2. split module name by '.' (ex: 'my_pkg.sub1.module1' -> ['my_pkg', 'sub1', 'module1'])
        parts = search_name.split('.')
        
        # mapping from alias to real folder structure
        # (**Note:** because alias already replaced '.' to '_', so we must scan folder to find matching)
        current_phys_path = self.folder_path
        
        # loop to find
        for part in parts[1:]:
            # check folder/file after replacing '.' to '_' that matched with 'part'
            found = False
            if current_phys_path.is_dir():
                for item in current_phys_path.iterdir():
                    cleaned_item_name = item.stem.replace('.', '_') if item.is_file() else item.name.replace('.', '_')
                    if cleaned_item_name == part:
                        current_phys_path = item
                        found = True
                        break
            if not found:
                print(f"⛔ (2) Package/Module {part} is not found from registered root package: {self.root_alias}")
                return None # not found any physical matching file/folder

        # 3. if found, return matching spec
        if current_phys_path.is_dir():
            # process package (folder)
            init_file = current_phys_path / "__init__.py"
            if init_file.exists():
                spec = importlib.util.spec_from_file_location(fullname, str(init_file))
            else:
                spec = ModuleSpec(fullname, None, is_package=True)
                spec.submodule_search_locations = [str(current_phys_path)]
            print(f"✅ Found package {fullname} from resgitered root package: {self.root_alias}")
            return spec
        
        # if found module file
        elif current_phys_path.is_file() and current_phys_path.suffix == '.py':
            # process Module (File .py)
            print(f"✅ Found module {fullname} from resgitered root package: {self.root_alias}")
            return importlib.util.spec_from_file_location(fullname, str(current_phys_path))
        
        print(f"⛔ (3) Package/Module {part} is not found from registered root package: {self.root_alias}")
        return None

# load current folder as python package
current_folder = os.path.dirname(os.path.abspath(__file__))
folderPackageFinder = FolderPackageFinder(current_folder);
sys.meta_path.insert(0, folderPackageFinder)
print(f"✅ Registered {current_folder} to sys.meta_path for finding with alias: {folderPackageFinder.alias()}")
