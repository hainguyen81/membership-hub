import sys
import os
import importlib.util
from pathlib import Path

def load_folder_as_package(folder_path):
    """
    Auto load folder (incuding subfolder) to Python package in sys.modules.
    Package (alias) is folder name, if it contains '.', will be transformed to '_'.
    """
    folder_path = Path(folder_path).resolve()
    if not folder_path.is_dir():
        raise FileNotFoundError(f"Not found folder path: {folder_path}")

    # 1. convert folder name to make it as package name (alias)
    raw_folder_name = folder_path.name
    package_alias = raw_folder_name.replace('.', '_')
    
    print(f"📦 Loading '{raw_folder_name}' with package alias '{package_alias}'...")

    # 2. Include parent folder to sys.path for Python
    parent_dir = str(folder_path.parent)
    if parent_dir not in sys.path:
        sys.path.insert(0, parent_dir)

    # 3. craete virtual package or use real package if it contains __init__.py
    init_file = folder_path / "__init__.py"
    if init_file.exists():
        spec = importlib.util.spec_from_file_location(package_alias, str(init_file))
    else:
        # if folder has no __init__.py, create package namespace
        spec = importlib.util.spec_from_namespace_package(package_alias, [str(folder_path)])

    root_package = importlib.util.module_from_spec(spec)
    sys.modules[package_alias] = root_package
    spec.loader.exec_module(root_package)

    # 4. scan all file .py and subfolder
    for root, dirs, files in os.walk(folder_path):
        current_dir_path = Path(root)
        
        # calculate subfolder path with root folder
        rel_parts = current_dir_path.relative_to(folder_path).parts
        
        # create sub-package (ex: 'my_pkg.sub1.sub2')
        # and replace '.' in subfolder name if neccessary
        cleaned_parts = [p.replace('.', '_') for p in rel_parts]
        current_package_parts = [package_alias] + cleaned_parts
        current_package_name = ".".join(current_package_parts)

        # register subfolder as sub-packages
        if root != str(folder_path):
            sub_init = current_dir_path / "__init__.py"
            if sub_init.exists():
                sub_spec = importlib.util.spec_from_file_location(current_package_name, str(sub_init))
            else:
                sub_spec = importlib.util.spec_from_namespace_package(current_package_name, [root])
            
            sub_package = importlib.util.module_from_spec(sub_spec)
            sys.modules[current_package_name] = sub_package
            sub_spec.loader.exec_module(sub_package)
            
            # include sub-package to parent package to call under format pkg.sub_pkg
            parent_package_name = ".".join(current_package_parts[:-1])
            setattr(sys.modules[parent_package_name], current_package_parts[-1], sub_package)

        # 5. load all file .py in subfolder as module
        for file in files:
            if file.endswith('.py') and file != '__init__.py':
                module_name_raw = Path(file).stem
                # replace '.' in file name if neccessary (ex: api.v1.py -> api_v1)
                module_name = module_name_raw.replace('.', '_')
                
                full_module_name = f"{current_package_name}.{module_name}"
                file_path = current_dir_path / file

                # Load module from file
                mod_spec = importlib.util.spec_from_file_location(full_module_name, str(file_path))
                module = importlib.util.module_from_spec(mod_spec)
                sys.modules[full_module_name] = module
                mod_spec.loader.exec_module(module)

                # include module to parent package to call
                setattr(sys.modules[current_package_name], module_name, module)

    return root_package
