# .ai/.agents/.sub-agents/agent-fixer.py
import os
import sys
import json
import re
import argparse
import subprocess
import yaml
import xml.etree.ElementTree as ET
from jproperties import Properties
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
MODELS_POOL_PATH            = resolve_absolute_path(".ai/.agents/.models/models.json")
STEPS_PLAN_DIR              = resolve_absolute_path(".ai/.agents/.steps")
BACKEND_WORKSPACE           = resolve_absolute_path("sources/backend")
FRONTEND_WORKSPACE          = resolve_absolute_path("sources/frontend")
agent_working_history_file  = resolve_absolute_path(".ai/.history/agent-fixer.md")

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
            print("[ 💀 CRITICAL ERROR ] The environment variable 'AI_MODELS_KEYS_JSON' is completely absent.")
            sys.exit(1)
            
        try:
            secrets_dict = json.loads(raw_json_secrets)
        except Exception as e:
            print(f"[ 💀 CRITICAL ERROR ] Failed to parse AI_MODELS_KEYS_JSON string: {str(e)}")
            sys.exit(1)

        while self.active_model_index < len(self.models_pool):
            config = self.models_pool[self.active_model_index]
            # target_model_name = config["model_name"]
            # target_model_endpoint = config["api_endpoint"]
            target_model_name = config.get("model_name") if isinstance(config, dict) else None
            target_model_endpoint = config.get("api_endpoint") if isinstance(config, dict) else None
            
            print("==============================================")
            print("🔍 DEBUG: 'config':")
            try:
                print(json.dumps(config, indent=4, ensure_ascii=False))
            except Exception:
                print(f"⚠️ Exception while dump 'config' json: {type(config)} - Config: {config}")
            print("==============================================")
            
            # If endpoint is missing, None, empty "", or just whitespaces "   ", skip it cleanly
            if not target_model_name or not target_model_endpoint or not str(target_model_endpoint).strip():
                print(f"⚠️ Ignore this config due to invalid 'model_name': {target_model_name} or 'model_endpoint': {target_model_endpoint}")
                self.active_model_index += 1
                continue # 🔄 Immediately jumps to the next iteration of the while loop
            
            api_key = secrets_dict.get(target_model_endpoint)
            if api_key:
                self.current_model_config = config
                self.client = OpenAI(api_key=api_key, base_url=target_model_endpoint)
                print(f"[ ⚠️ FAILOVER ENGAGED ] Fixer Agent successfully authenticated model: {target_model_name} | endpoint: {target_model_endpoint}")
                return
            self.active_model_index += 1
        print("[ 💀 CRITICAL ERROR ] Bug Fixer Agent exhausted all registered recovery models.")
        sys.exit(1)
    
    def run_compile_check(self, target_path, check_by_compile):
        # parse file extension (ex: '.sql', '.json')
        file_name = os.path.basename(target_path).lower()
        _, file_extension = os.path.splitext(target_path.lower())
        if not check_by_compile:
            check_by_compile = (file_name == 'pom.xml' or file_name == 'package.json')
        
        # if SQL
        if file_extension == '.sql':
            # use sqlfluff (linter to check SQL, need `pip install sqlfluff`)
            # --dialect ansi to check syntax SQL following global standards
            result = subprocess.run([ "sqlfluff", "lint", target_path, "--dialect", "ansi" ], capture_output=True, text=True, timeout=120)
            if not check_by_compile:
                return (result.returncode == 0, result.stdout + "\n" + result.stderr)
        
        # if YAML, YML file
        elif file_extension in ['.yaml', '.yml']:
            # need `pip install PyYAML`
            try:
                with open(target_path, 'r', encoding='utf-8') as f:
                    yaml.safe_load(f) # read YAML, YML file
                if not check_by_compile:
                    return (True, "YAML: Cú pháp hoàn toàn hợp lệ.")
            except yaml.YAMLError as e:
                return (False, f"YAML Syntax Error:\n{str(e)}")
        
        # if XML file
        elif file_extension == '.xml' and file_name != 'pom.xml':
            # Python lib, no need to install
            try:
                ET.parse(target_path) # read XML
                if not check_by_compile:
                    return (True, "XML: XML Syntax correct.")
            except ET.ParseError as e:
                return (False, f"XML Syntax Error: {str(e)}")
        
        # if properties file
        elif file_extension == '.properties' or file_extension == '.env':
            # need `pip install jproperties`
            try:
                configs = Properties()
                with open(target_path, 'rb') as f: # format properties, read under byte
                    configs.load(f)
                if not check_by_compile:
                    return (True, "Properties: Syntax correct.")
            except Exception as e:
                return (False, f"Properties Syntax Error: {str(e)}")
        
        # if file JSON
        elif file_extension == '.json':
            # use Python lib to parse, no need to call subprocess
            try:
                with open(target_path, 'r', encoding='utf-8') as f:
                    import json
                    json.load(f)
                if not check_by_compile:
                    return (True, "JSON Validated Successfully")
            except Exception as e:
                return (False, f"JSON Syntax Error: {str(e)}")
        
        # check by build project
        pom_path = os.path.join(BACKEND_WORKSPACE, "pom.xml")
        package_path = os.path.join(FRONTEND_WORKSPACE, "package.json")
        if "backend" in target_path and os.path.exists(pom_path):
            # build to check error
            result = subprocess.run(["mvn", "clean", "test-compile"], cwd=BACKEND_WORKSPACE, capture_output=True, text=True, timeout=120)
            # return compile result
            return (result.returncode == 0, result.stdout + "\n" + result.stderr)
        
        elif "frontend" in target_path and os.path.exists(package_path):
            # build to check error
            result = subprocess.run(["npm", "run", "build"], cwd=FRONTEND_WORKSPACE, capture_output=True, text=True, timeout=120)
            # return compile result
            return (result.returncode == 0, result.stdout + "\n" + result.stderr)
        
        elif "backend" in target_path:
            return (True, "Project hasn't been initialized yet. Not found project main component: pom.xml")
        
        else:
            return (True, "Project hasn't been initialized yet. Not found project main component: package.json")
    
    def write_history(self, itegration, target_component, component_desc, user_prompt, compiler_log):
        # write working history
        history_content = (
            f"# Day {self.day_num}: model {self.current_model_config['model_name']} - API Endpoint {self.current_model_config['api_endpoint']}\n" if itegration == 1 else f"## Interation {itegration}: {component_desc}"
            f"* **{component_desc}**: {target_component}\n" if itegration == 1 else f"* **Target Component**: {target_component}"
            f"* **Compile Result**:\n{compiler_log}"
            f"* **📝 Prompt / Tasks**:\n-------------------------------------------------\n{user_prompt}\n\n"
        )
        with open(agent_working_history_file, "a", encoding="utf-8") as file:
            file.write(history_content)
    
    def write_log(self, target_component, content):
        agent_working_response_file = resolve_absolute_path(f".ai/.history/agent-fixer-ai-day-{self.day_num}.md")
        log_content = (
            f"# Day {self.day_num}: model {self.current_model_config['model_name']} - API Endpoint {self.current_model_config['api_endpoint']}\n\n"
            f"* **{target_component}**\n\n---\n\n"
            f"{content}\n\n"
            f"---\n\n"
        )
        with open(agent_working_response_file, "a", encoding="utf-8") as file:
            file.write(log_content)
    
    def check_project_initialized(self, target_component):
        if "backend" in target_component:
            pom_path = os.path.join(BACKEND_WORKSPACE, "pom.xml")
            # if not found pom.xml, it means project empty or be initializing
            return (os.path.exists(pom_path), pom_path)
        else:
            package_path = os.path.join(FRONTEND_WORKSPACE, "package.json")
            # if not found package.json, it means project empty or be initializing
            return (os.path.exists(package_path), package_path)

    def fix_bugs(self):
        steps_path = f"{STEPS_PLAN_DIR}/phase-{self.phase_str}.agent.steps.json"
        with open(steps_path, "r", encoding="utf-8") as f:
            steps_data = json.load(f)
        
        global_context_file = resolve_absolute_path(steps_data["global_context_file"])
        with open(global_context_file, "r", encoding="utf-8") as f:
            global_context = f.read()
            
        target_day = next((d for d in steps_data["days"] if d["day"] == self.day_num), None)
        target_component = resolve_absolute_path(target_day["target_component"])
        context_file = resolve_absolute_path(target_day["context_file"])
        with open(context_file, "r", encoding="utf-8") as f:
            phase_content = f.read()
        pattern = rf"(## {target_day['context_section']}:.*?)((?=\n## DAY )|\Z)"
        day_context = re.search(pattern, phase_content, re.DOTALL | re.IGNORECASE).group(1).strip()
        
        system_prompt = f"{global_context}\n\n## TODAY REQUIREMENTS:\n{day_context}\n\nRole: Elite Security Architect & Code Compiler Fixer. Analyze source code along with real raw compiler error logs. Auto-patch code perfectly. Output ONLY clean executable code blocks."
        sub_tasks = "\nFix errors and execute sub-tasks".join([f"- {t['desc']}" for t in target_day["sub_tasks"] if "fixer" in t['agent'] or "Bug Fixer Agent" in t['desc']])
        
        # check whether project had been initialized
        project_initialized, project_main_component = self.check_project_initialized(target_day["target_component"])
        print(f"[ ℹ️ F.Y.I ] Project had been initialized?. {project_initialized} - Project Main Component: {project_main_component}")
        print(f"            - Target Component: {target_day['target_component']}")
        
        # test component 3 time(s)
        max_iterations = 3
        for iteration in range(1, max_iterations + 1):
            # only compile project when it had been initialized
            is_clean, compiler_log = self.run_compile_check(target_component, project_initialized)
            if is_clean:
                print(f"[ ✅ FIXER SUCCESS ] Target codebase component compiled cleanly on iteration loop: {iteration}!")
                self.write_history(iteration, target_day["target_component"], "Target codebase component compiled cleanly on iteration loop {iteration}", sub_tasks, compiler_log)
                return True
            
            print(f"[ ⚠️ FIXER WARNING ] Build check failed on validation loop: {iteration}. Ingesting raw error logs...")
            with open(target_component, "r", encoding="utf-8") as f:
                current_code = f.read()
            
            
            user_prompt = f"Current Faulty Source Code Implementation:\n{current_code}\n\nReal-time Compiler Error Logs Output Traces:\n{compiler_log}\n{sub_tasks}"
            
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
                
                # write AI response
                self.write_log(target_day["target_component"], patched_code)
                
                with open(target_component, "w", encoding="utf-8") as f:
                    f.write(clean_code)
                
                # write history if neccessary
                self.write_history(iteration, target_day["target_component"], "Responsed the fixes for target codebase component on iteration loop {iteration}", user_prompt, compiler_log)
            except Exception as e:
                print(f"[ 💀 FIXER RECOVERY ] API transaction exception caught. Swapping model: {str(e)}")
                self.write_log(target_day["target_component"], str(e))
                self.active_model_index += 1
                self.rotate_model()
                
        print("[ 💀 FIXER CRITICAL ] Structural compiler repairs failed within maximum iteration bounds.")
        return False

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    args = parser.parse_args()
    success = BugFixerAgent(args.phase, args.day).fix_bugs()
    if not success:
        sys.exit(1)
