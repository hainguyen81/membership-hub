import os
import sys
import importlib
import importlib.util
from pathlib import Path
from importlib.abc import MetaPathFinder, Loader
from importlib.machinery import ModuleSpec

import re

# -------------------------------------------------
# MAPPING SPECIAL CHARACTERS TABLE for PYTHON PACKAGE / MODULE NAME
# -------------------------------------------------
# Original                                  Encoded                     Algorithm                   Original Package/Module     Encoded Package/Module
# -------------------------------------------------
#   Space ( )                               _0s_                        Replace                     idea gen                    idea_0s_gen
#   Hiphen (-)                              _0h_                        Replace                     my-agent                    my_0h_agent
#   Dot (.)                                 _0d_                        Replace                     agent.v1                    agent_0d_v1
#   Underscore (_)                          _0u_                        Replace                     sys_log                     sys_0u_log
#   Starting with number (0-9)              Insert _0n_ more at first   Add prefix                  2026_tool                   _0n_2026_tool
#   Special Characters (!, @, #,...)        _x + Hex + _                _x + hex(ord(c)) + _        ai@tool                     ai_x40_tool
#                                                                                                                               (40 is hex of @)
#   Emoji / Vietnamese (💡, á,...)           _u + Unicode + _            _u + hex(ord(c)) + _        💡_agent                     _u1f4a1__0u_agent
# -------------------------------------------------
class ModuleNameMapper:
    @staticmethod
    def encode(text: str) -> str:
        """real name to map"""
        if not text:
            return ""
        
        result = []
        # convert every special character
        for char in text:
            if char == ' ':
                result.append('_0s_')
            elif char == '-':
                result.append('_0h_')
            elif char == '.':
                result.append('_0d_')
            # elif char == '_':
            #    result.append('_0u_')
            elif char.isalnum() and ord(char) < 128:
                # keep original character
                result.append(char)
            else:
                # special character, emoji, vietnamese -> to Hex
                code = ord(char)
                if code < 256:
                    result.append(f'_x{code:02x}_')
                else:
                    result.append(f'_u{code:04x}_')
                    
        encoded_str = "".join(result)
        
        # if starting with number character
        if encoded_str and encoded_str[0].isdigit():
            encoded_str = "_0n_" + encoded_str
            
        return encoded_str

    @staticmethod
    def decode(encoded_text: str) -> str:
        """Revert to real name"""
        if not encoded_text:
            return ""
            
        # 1. for first number character
        if encoded_text.startswith("_0n_"):
            encoded_text = encoded_text[4:]
            
        # 2. special characters under Hex (_xHH_ hoặc _uHHHH_)
        def replace_hex(match):
            val = match.group(1) or match.group(2)
            return chr(int(val, 16))
            
        # regex to scan hex
        pattern_hex = r'_u([0-9a-fA-F]{4})_|_x([0-9a-fA-F]{2})_'
        decoded_text = re.sub(pattern_hex, replace_hex, encoded_text)
        
        # 3. decode remain special characters
        decoded_text = decoded_text.replace('_0s_', ' ')
        decoded_text = decoded_text.replace('_0h_', '-')
        decoded_text = decoded_text.replace('_0d_', '.')
        decoded_text = decoded_text.replace('_0u_', '_')
        
        # real name
        return decoded_text

class FolderPackageFinder(MetaPathFinder):
    def __init__(self, folder_path):
        self.folder_path = Path(folder_path).resolve()
        if not self.folder_path.is_dir():
            raise FileNotFoundError(f"Not found folder path: {self.folder_path}")
        
        # 1. Define root Package Alias
        self.root_alias = ModuleNameMapper.encode(self.folder_path.name)
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
        # (**Note:** because alias already encoded special characters, so we must scan folder to find matching)
        current_phys_path = self.folder_path
        print(f"✅ Search from root {current_phys_path} | Alias: {self.root_alias} | Packages: {search_name}")
        
        # loop to find
        for part in parts[1:]:
            # check folder/file after replacing '.' to '_' that matched with 'part'
            found = False
            
            # CASE 1: current path is folder -> scan sub-folder/sub-file
            if current_phys_path.is_dir():
                for item in current_phys_path.iterdir():
                    cleaned_item_name = ModuleNameMapper.encode(item.stem) if item.is_file() else ModuleNameMapper.encode(item.name)
                    is_matched = cleaned_item_name == part
                    # print(f"- ✅ Package {item.name} | Alias: {cleaned_item_name} | Matched-Part: {part}?. {is_matched}")
                    if is_matched:
                        current_phys_path = item
                        found = True
                        break
                        
            # CASE 2: current path is file -> 'part' is Class/Function in file
            elif current_phys_path.is_file() and current_phys_path.suffix == '.py':
                cleaned_item_name = ModuleNameMapper.encode(current_phys_path.name) if item.is_file() else ModuleNameMapper.encode(current_phys_path.name)
                is_matched = cleaned_item_name == part
                # print(f"- ✅ Module {current_phys_path.name} | Alias: {cleaned_item_name} | Matched-Part: {part}?. {is_matched}")
                if is_matched:
                    current_phys_path = item
                    found = True
                    break
            
            # if 
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


# register list of packages
def register_packages(packages):
    if not packages or not isinstance(packages, list):
        return
    
    for package in packages:
        package_path = Path(package).resolve() if package else None
        if package_path and package_path.is_dir():
            packageFinder = FolderPackageFinder(str(package_path));
            sys.meta_path.insert(0, packageFinder)
            print(f"✅ Registered {package_path} to sys.meta_path for finding with alias: {packageFinder.alias()}")
        
        else:
            print(f"⛔ Not found package folder path {package} to register package/module finder")

# load current folder as python package
current_folder = os.path.dirname(os.path.abspath(__file__))
register_packages([ current_folder ])
