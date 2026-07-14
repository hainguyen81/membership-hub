# .ai/.agents/.sub-agents/agent-fixer.py
import os
import sys
import json
import re
import argparse
import subprocess
from openai import OpenAI

# ==============================================================================
# 🏢 ENTERPRISE INTER-PACKAGE ROUTING LAYER
# ==============================================================================
# Programmatically appends the parent directory (.ai/.agents/) into Python's runtime
# search path array. This completely unlocks importing 'agent_helper.py'.
# ==============================================================================
CURRENT_SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__)) # .ai/.agents/.sub-agents/
PARENT_AGENTS_DIR  = os.path.abspath(os.path.join(CURRENT_SCRIPT_DIR, "../")) # .ai/.agents/

# jump to `agent_helper.py` folder path
if PARENT_AGENTS_DIR not in sys.path:
    sys.path.insert(0, PARENT_AGENTS_DIR)

# Now Python can seamlessly see and import the centralized helper utility cleanly!
from agent_helper import resolve_absolute_path

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
MODELS_POOL_PATH  = resolve_absolute_path(".ai/.agents/.models/models.json")
STEPS_PLAN_DIR    = resolve_absolute_path(".ai/.agents/.steps")
BACKEND_WORKSPACE = resolve_absolute_path("sources/backend")
FRONTEND_WORKSPACE= resolve_absolute_path("sources/frontend")

class BugFixerAgent:
    """
    Senior Security Architect and Compiler Specialist Agent. Triggers native command-line compilers,
    intercepts dirty stack error outputs, and auto-patches code syntax or type discrepancies iteratively.
    """
    def __init__(self, phase_str, day_num):
        self.phase_str = phase_str
        self.day_num = int(day_num)
        self.models_pool = self.load_models_pool()
        self.active_model_index = 0
        self.client = None
        self.current_model_config = None
        self.rotate_model()

    def load_models_pool(self):
        with open(MODELS_POOL_PATH, "r", encoding="utf-8") as f:
            return json.load(f)

    def rotate_model(self):
        raw_json_secrets = os.environ.get("AI_MODELS_KEYS_JSON")
        if not raw_json_secrets:
            print("[CRITICAL ERROR] The environment variable 'AI_MODELS_KEYS_JSON' is completely absent.")
            sys.exit(1)
            
        try:
            secrets_dict = json.loads(raw_json_secrets)
        except Exception as e:
            print(f"[CRITICAL ERROR] Failed to parse AI_MODELS_KEYS_JSON string: {str(e)}")
            sys.exit(1)

        while self.active_model_index < len(self.models_pool):
            config = self.models_pool[self.active_model_index]
            target_model_name = config["model_name"]
            target_model_endpoint = config["api_endpoint"]
            
            print("==============================================")
            print("🔍 DEBUG: 'config':")
            try:
                print(json.dumps(config, indent=4, ensure_ascii=False))
            except Exception:
                print(f"Exception while dump 'config' json: {type(config)} - Config: {config}")
            print("==============================================")
            
            # If endpoint is missing, None, empty "", or just whitespaces "   ", skip it cleanly
            if not target_model_name or not target_model_endpoint or not str(target_model_endpoint).strip():
                print(f"Ignore this config due to invalid 'model_name': {target_model_name} or 'model_endpoint': {target_model_endpoint}")
                self.active_model_index += 1
                continue # 🔄 Immediately jumps to the next iteration of the while loop
            
            api_key = secrets_dict.get(target_model_endpoint)
            if api_key:
                self.current_model_config = config
                self.client = OpenAI(api_key=api_key, base_url=target_model_endpoint)
                print(f"[FAILOVER ENGAGED] Fixer Agent successfully authenticated model: {target_model_name} | endpoint: {target_model_endpoint}")
                return
            self.active_model_index += 1
        print("[CRITICAL ERROR] Bug Fixer Agent exhausted all registered recovery models.")
        sys.exit(1)

    def run_compile_check(self, target_path):
        if "backend" in target_path:
            result = subprocess.run(["mvn", "clean", "test-compile"], cwd=BACKEND_WORKSPACE, capture_output=True, text=True, timeout=120)
        else:
            result = subprocess.run(["npm", "run", "build"], cwd=FRONTEND_WORKSPACE, capture_output=True, text=True, timeout=120)
        return (result.returncode == 0, result.stdout + "\n" + result.stderr)

    def fix_bugs(self):
        steps_path = f"{STEPS_PLAN_DIR}/phase-{self.phase_str}.agent.steps.json"
        with open(steps_path, "r", encoding="utf-8") as f:
            steps_data = json.load(f)
        
        global_context_file = resolve_absolute_path(steps_data["global_context_file"])
        with open(global_context_file, "r", encoding="utf-8") as f:
            global_context = f.read()
            
        target_day = next((d for d in steps_data["days"] if d["day"] == self.day_num), None)
        context_file = resolve_absolute_path(target_day["context_file"])
        with open(context_file, "r", encoding="utf-8") as f:
            phase_content = f.read()
        pattern = rf"(## {target_day['context_section']}:.*?)((?=\n## DAY )|\Z)"
        day_context = re.search(pattern, phase_content, re.DOTALL | re.IGNORECASE).group(1).strip()
        
        system_prompt = f"{global_context}\n\n## TODAY REQUIREMENTS:\n{day_context}\n\nRole: Elite Security Architect & Code Compiler Fixer. Analyze source code along with real raw compiler error logs. Auto-patch code perfectly. Output ONLY clean executable code blocks."
        
        max_iterations = 3
        for iteration in range(1, max_iterations + 1):
            target_component = resolve_absolute_path(target_day["target_component"])
            is_clean, compiler_log = self.run_compile_check(target_component)
            if is_clean:
                print(f"[FIXER SUCCESS] Target codebase component compiled cleanly on iteration loop: {iteration}!")
                return True
                
            print(f"[FIXER WARNING] Build check failed on validation loop: {iteration}. Ingesting raw error logs...")
            with open(target_component, "r", encoding="utf-8") as f:
                current_code = f.read()
                
            user_prompt = f"Current Faulty Source Code Implementation:\n{current_code}\n\nReal-time Compiler Error Logs Output Traces:\n{compiler_log}"
            
            try:
                response = self.client.chat.completions.create(
                    model=self.current_model_config["model_name"],
                    messages=[{"role": "system", "content": system_prompt}, {"role": "user", "content": user_prompt}],
                    temperature=0.1
                )
                # patched_code = response.choices.message.content
                choice = response.choices[0]
                if hasattr(choice, "message"):
                    patched_code = choice.message.content
                elif isinstance(choice, dict) and "message" in choice:
                    patched_code = choice["message"]["content"]
                else:
                    # Direct text string fallback array
                    patched_code = response.choices[0].text
                clean_code = patched_code.replace("```java", "").replace("```ts", "").replace("```tsx", "").replace("```", "").strip()
                
                with open(target_component, "w", encoding="utf-8") as f:
                    f.write(clean_code)
            except Exception as e:
                print(f"[FIXER RECOVERY] API transaction exception caught. Swapping model: {str(e)}")
                self.active_model_index += 1
                self.rotate_model()
                
        print("[FIXER CRITICAL] Structural compiler repairs failed within maximum iteration bounds.")
        return False

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    args = parser.parse_args()
    success = BugFixerAgent(args.phase, args.day).fix_bugs()
    if not success:
        sys.exit(1)
