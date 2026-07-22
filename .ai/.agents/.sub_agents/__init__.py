import os
import sys

# put folder into python search path
CURRENT_ROOT = os.path.abspath("./") # from project root folder
TARGET_DIR = os.path.join(CURRENT_ROOT, ".ai", ".agents", ".sub_agents")

if TARGET_DIR not in sys.path:
    sys.path.insert(0, TARGET_DIR)