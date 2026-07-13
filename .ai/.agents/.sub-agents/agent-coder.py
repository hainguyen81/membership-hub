# .ai/.agents/.sub-agents/agent-coder.py
import os
import sys
import json
import re
import argparse
from openai import OpenAI

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
MODELS_POOL_PATH = "./.ai/.agents/.models/models.json"
STEPS_PLAN_DIR   = "./.ai/.agents/.steps"

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
            
            # Lookup the API Key inside the GitHub Secret JSON dictionary using the model_name from models.json
            api_key = secrets_dict.get(target_model_name)
            
            if api_key:
                self.current_model_config = config
                self.client = OpenAI(api_key=api_key, base_url=config["api_endpoint"])
                print(f"[FAILOVER ENGAGED] Coder Agent successfully authenticated model: {target_model_name}")
                return
            else:
                print(f"[WARNING] API key missing inside GitHub JSON for model: {target_model_name}. Skipping tier.")
                self.active_model_index += 1
                
        print("[CRITICAL SHUTDOWN] Coder Agent exhausted all registered AI models without valid keys.")
        sys.exit(1)

    def generate_code(self):
        steps_path = f"{STEPS_PLAN_DIR}/phase-{self.phase_str}.agent.steps.json"
        with open(steps_path, "r", encoding="utf-8") as f:
            steps_data = json.load(f)
        with open(steps_data["global_context_file"], "r", encoding="utf-8") as f:
            global_context = f.read()
            
        target_day = next((d for d in steps_data["days"] if d["day"] == self.day_num), None)
        with open(target_day["context_file"], "r", encoding="utf-8") as f:
            phase_content = f.read()
            
        pattern = rf"(## {target_day['context_section']}:.*?)((?=\n## DAY )|\Z)"
        day_context = re.search(pattern, phase_content, re.DOTALL | re.IGNORECASE).group(1).strip()
        
        system_prompt = f"{global_context}\n\n## TODAY REQUIREMENTS:\n{day_context}\n\nRole: Principal Software Architect. Generate code for path: {target_day['target_component']}. Output ONLY code without explanations."
        user_prompt = "Execute Sub-tasks:\n" + "\n".join([f"- {t['desc']}" for t in target_day["sub_tasks"]])
        
        while True:
            try:
                response = self.client.chat.completions.create(
                    model=self.current_model_config["model_name"],
                    messages=[{"role": "system", "content": system_prompt}, {"role": "user", "content": user_prompt}],
                    temperature=0.1
                )
                code_out = response.choices.message.content
                clean_code = code_out.replace("```java", "").replace("```sql", "").replace("```json", "").replace("```markdown", "").replace("```ts", "").replace("```tsx", "").replace("```", "").strip()
                
                os.makedirs(os.path.dirname(target_day["target_component"]), exist_ok=True)
                with open(target_day["target_component"], "w", encoding="utf-8") as f:
                    f.write(clean_code)
                print(f"[CODER SUCCESS] Production source codebase generated at target destination: {target_day['target_component']}")
                break
            except Exception as e:
                print(f"[CODER LLM EXHAUSTED] Failed execution on model {self.current_model_config['model_name']}: {str(e)}")
                self.active_model_index += 1
                self.rotate_model()

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    args = parser.parse_args()
    CoderAgent(args.phase, args.day).generate_code()
