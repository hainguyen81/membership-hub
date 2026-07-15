# .ai/.agents/.sub-agents/agent-coder.py
import os
import sys
import json
import re
import argparse
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
agent_working_history_file  = resolve_absolute_path(".ai/.history/agent-coder.md")

class CoderAgent:
    """
    Principal Software Architect Agent responsible for analyzing domain requirements,
    verifying multi-tenant isolation contexts, and generating production-grade raw source code.
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
        """
        Orchestrates model failover swapping loops by programmatically reading the 
        centralized AI_MODELS_KEYS_JSON secret dictionary using the exact model name from models.json as key.
        """
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
            
            # Lookup the API Key inside the GitHub Secret JSON dictionary using the model_name from models.json
            api_key = secrets_dict.get(target_model_endpoint)
            if api_key:
                self.current_model_config = config
                self.client = OpenAI(api_key=api_key, base_url=target_model_endpoint)
                print(f"[ 💀 FAILOVER ENGAGED ] Coder Agent successfully authenticated model: {target_model_name} | endpoint: {target_model_endpoint}")
                return
            else:
                print(f"[ ⚠️ WARNING] API key missing inside GitHub JSON for model: {target_model_name} | endpoint: {target_model_endpoint}. Skipping tier.")
                self.active_model_index += 1
                
        print("[ 💀 CRITICAL SHUTDOWN ] Coder Agent exhausted all registered AI models without valid keys.")
        sys.exit(1)
    
    def write_history(self, target_component, user_prompt):
        history_content = (
            f"# Day {self.day_num}: model {self.current_model_config['model_name']} - API Endpoint {self.current_model_config['api_endpoint']}\n"
            f"* **Production source codebase generated at target destination**: {target_component}\n"
            f"* **📝 Prompt / Tasks**:\n-------------------------------------------------\n{user_prompt}\n\n"
        )
        with open(agent_working_history_file, "a", encoding="utf-8") as file:
            file.write(history_content)
    
    def write_log(self, target_component, content):
        agent_working_response_file = resolve_absolute_path(f".ai/.history/agent-coder-ai-day-{self.day_num}.md")
        log_content = (
            f"# Day {self.day_num}: model {self.current_model_config['model_name']} - API Endpoint {self.current_model_config['api_endpoint']}\n\n"
            f"* **{target_component}**\n\n---\n\n"
            f"{content}\n\n"
            f"---\n\n"
        )
        with open(agent_working_response_file, "a", encoding="utf-8") as file:
            file.write(log_content)

    def generate_code(self):
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
        
        system_prompt = f"{global_context}\n\n## TODAY REQUIREMENTS:\n{day_context}\n\nRole: Principal Software Architect. Generate code for path: {target_day['target_component']}. Output ONLY code without explanations."
        sub_tasks = "\n".join([f"- {t['desc']}" for t in target_day["sub_tasks"] if "coder" in t['agent'] or "Coder Agent" in t['desc']])
        user_prompt = f"Execute Sub-tasks:\n{sub_tasks}"
        
        while True:
            try:
                response = self.client.chat.completions.create(
                    model=self.current_model_config["model_name"],
                    messages=[{"role": "system", "content": system_prompt}, {"role": "user", "content": user_prompt}],
                    temperature=0.1
                )
                # code_out = response.choices.message.content
                choice = response.choices[0]
                if hasattr(choice, "message"):
                    code_out = choice.message.content
                elif isinstance(choice, dict) and "message" in choice:
                    code_out = choice["message"]["content"]
                else:
                    # Direct text string fallback array
                    code_out = response.choices[0].text
                clean_code = code_out.replace("```java", "").replace("```sql", "").replace("```json", "").replace("```markdown", "").replace("```ts", "").replace("```tsx", "").replace("```", "").strip()
                
                # write AI response log
                self.write_log(target_day["target_component"], test_out)
                
                target_component = resolve_absolute_path(target_day["target_component"])
                os.makedirs(os.path.dirname(target_component), exist_ok=True)
                with open(target_component, "w", encoding="utf-8") as f:
                    f.write(clean_code)
                print(f"[ ✅ CODER SUCCESS | Model {self.current_model_config['model_name']} | API Endpoint {self.current_model_config['api_endpoint']} | Day {self.day_num} ] Production source codebase generated at target destination: {target_day['target_component']}")
                
                # write working history
                self.write_history(target_day['target_component'], user_prompt)
                break
            except Exception as e:
                print(f"[ 💀 CODER LLM EXHAUSTED ] Failed execution on model {self.current_model_config['model_name']}: {str(e)}")
                self.write_log(target_day["target_component"], str(e))
                self.active_model_index += 1
                self.rotate_model()

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    args = parser.parse_args()
    CoderAgent(args.phase, args.day).generate_code()
