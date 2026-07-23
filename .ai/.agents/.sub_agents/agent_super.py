# .ai/.agents/.sub-agents/agent-tester.py
import os
import sys
import json
import re
import argparse
from datetime import datetime
from openai import OpenAI
from abc import ABC, abstractmethod

# ==============================================================================
# 🏢 ENTERPRISE INTER-PACKAGE ROUTING LAYER
# ==============================================================================
# Programmatically appends the parent directory (.ai/.agents/) into Python's runtime
# search path array. This completely unlocks importing 'agent_helper.py'.
# ==============================================================================
# request agent_helper from `.libs/project_agents_package_loader.py`
from _ai._agents import agent_helper

# Now Python can seamlessly see and import the centralized helper utility cleanly!
from _ai._agents._sub_agents.helper import write_log_history, write_file, read_json_file, read_file_raw, render_prompt, parseOpenAIResponseData

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
MODELS_POOL_PATH            = agent_helper.resolve_absolute_path(".ai/.agents/.models/models.json")
STEPS_PLAN_DIR              = agent_helper.resolve_absolute_path(".ai/.plan/.steps")

class AbstractAgent(ABC):
    def __init__(self, agent_id, phase_str, day_num):
        self.agent_id = agent_id if agent_id else "Super"
        self.phase_str = phase_str
        self.day_num = int(day_num)
        self.secrets_key = self.agent_secrets_key()
        self.secrets = self.load_secrets()
        self.initialize()
    
    def initialize(self):
        self.models_pool = self.load_models_pool()
        self.active_model_index = 0
        self.client = None
        self.current_model_config = None
        self.rotate_model()
    
    @abstractmethod
    def agent_secrets_key(self) -> str:
        pass
    
    def agent_secrets(self, key, defVal=None):
        return self.secrets.get(key, defVal) if self.secrets and key and len(key) > 0 else defVal
    
    def load_secrets(self):
        if not self.secrets_key or len(self.secrets_key) <= 0:
            print(f"[ 💀 {self.agent_id} Agent | WARN ] Not found secrets key to load secrets!")
            return None
        
        # load secrets from environment
        raw_secrets = os.environ.get(self.secrets_key)
        if not raw_secrets:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL ] The environment variable '{self.secrets_key}' is completely absent.")
            sys.exit(1)
        
        # parse secrets to JSON
        try:
            return json.loads(raw_secrets)
        except Exception as e:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL ] Failed to parse environment '{self.secrets_key}' JSON string: {str(e)}")
            sys.exit(1)
    
    def load_models_pool(self):
        with open(MODELS_POOL_PATH, "r", encoding="utf-8") as f:
            return json.load(f)
    
    def rotate_model(self):
        raw_json_secrets = os.environ.get("AI_MODELS_KEYS_JSON")
        if not raw_json_secrets:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL ERROR ] The environment variable 'AI_MODELS_KEYS_JSON' is completely absent.")
            return False
        
        try:
            secrets_dict = json.loads(raw_json_secrets)
        except Exception as e:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL ERROR ] Failed to parse AI_MODELS_KEYS_JSON string: {str(e)}")
            return False

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
                print(f"⚠️ {self.agent_id} Agent | Ignore this config due to invalid 'model_name': {target_model_name} or 'model_endpoint': {target_model_endpoint}")
                self.active_model_index += 1
                continue # 🔄 Immediately jumps to the next iteration of the while loop
            
            api_key = secrets_dict.get(target_model_endpoint)
            if api_key:
                self.current_model_config = config
                self.client = OpenAI(api_key=api_key, base_url=target_model_endpoint)
                print(f"[ 💀 {self.agent_id} Agent | FAILOVER ENGAGED ] Successfully authenticated model: {target_model_name} | endpoint: {target_model_endpoint}")
                return True
            self.active_model_index += 1
        print(f"[ 💀 {self.agent_id} Agent | CRITICAL ERROR ] Exhausted all registered fallback models.")
        return False
    
    @abstractmethod
    def agent_log_file(self) -> str:
        pass
    
    @abstractmethod
    def system_prompt_template(self) -> str:
        pass
    
    @abstractmethod
    def user_prompt_template(self) -> str:
        pass
    
    def build_system_prompt_context(self, project_name, global_context, day_context, target_component, **kwargs):
        return {
            "project_name": project_name,
            "global_context": global_context,
            "day_context": day_context,
            "target_component": target_component
            **kwargs
        }
    
    def build_user_prompt_context(self, project_name, source_component, target_component, sub_tasks, **kwargs):
        if os.path.exists(source_component):
            lang_code = "typescript" if source_component.endswith(('.ts', '.tsx', '.js')) else "java"
            _, source_payload = read_file_raw(source_component.strip())
            source_payload = f"```{lang_code}\n{source_payload.strip()}\n```"
        else:
            source_component = "INTEGRATION_SCOPE"
            source_payload = None
        return {
            "project_name": project_name,
            "source_component": source_component,
            "source_payload": source_payload,
            "sub_tasks": sub_tasks.strip(),
            **kwargs
        }
    
    def chat(self, system_prompt, user_prompt):
        response = self.client.chat.completions.create(
            model=self.current_model_config["model_name"],
            messages=[{"role": "system", "content": system_prompt}, {"role": "user", "content": user_prompt}],
            temperature=0.1
        )
        raw_response = parseOpenAIResponseData(response)
        clean_response = raw_response.replace("```java", "").replace("```ts", "").replace("```tsx", "").replace("```", "").strip()
        return (raw_response, clean_response)
    
    def write_log(self, log_file, target_component, user_prompt, data):
        return write_log_history(
            history_file=log_file,
            day=self.day_num,
            model_name=self.current_model_config["model_name"],
            api_endpoint=self.current_model_config["api_endpoint"],
            target_component=target_component,
            prompt=user_prompt,
            data=data
        )
    
    def process_chat(self, target_component, response_data):
        write_file(
            dir=os.path.dirname(target_component),
            file_name=os.path.basename(target_component),
            data=response_data
        )
        print(f"[ ✅ {self.agent_id} Agent - SUCCESS | Model {self.current_model_config['model_name']} | API Endpoint {self.current_model_config['api_endpoint']} | Day {self.day_num} ] Committed to: { target_component }")
    
    def execute_task(self, project_name, global_context, day_context, source_component, target_component, sub_tasks):
        # build system prompt
        system_prompt_context = self.build_system_prompt_context(project_name, global_context, day_context, target_component)
        system_prompt = render_prompt(self.system_prompt_template(), system_prompt_context)
        
        # build user prompt
        user_prompt_context = self.build_user_prompt_context(project_name, source_component, target_component, sub_tasks)
        user_prompt = render_prompt(self.user_prompt_template(), user_prompt_context)
        
        # agent do job
        latest_response = None
        success = False
        try:
            raw_response, clean_response = self.chat(
                system_prompt=system_prompt,
                user_prompt=user_prompt
            )
            latest_response = raw_response
            
            # write generated code
            self.process_chat(
                target_component=target_component,
                response_data=clean_response
            )
            success = True
        except Exception as e:
            print(f"[ 💀 {self.agent_id} Agent | ERROR ] Exception caught on model {self.current_model_config['model_name']}: {str(e)}")
            latest_response = str(e) if not latest_response else latest_response
        
        # result
        return (success, system_prompt, user_prompt, latest_response)
    
    @abstractmethod
    def pre_execute(self):
        pass
        
    def execute(self):
        # read JSON steps
        phase_step_file = f"phase-{self.phase_str}.steps.json"
        steps_path, steps_data = read_json_file(os.path.join(STEPS_PLAN_DIR, phase_step_file))
        if not steps_data:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL ERROR ] Not found phase steps JSON file { phase_step_file }")
            sys.exit(1)
        
        # parse project name from phase steps data
        datetimeStr = datetime.now().strftime("%Y%m%d%H%M%S")
        defaultPrjName = f"project-{datetimeStr}"
        project_name = steps_data["project_name"] if steps_data["project_name"] else defaultPrjName
        
        # check agent from JSON steps
        target_day = next((d for d in steps_data["days"] if d["day"] == self.day_num), None)
        is_matched_agent = target_day and (self.agent_id == target_day["agent"] or target_day["desc"].startswith(self.agent_id))
        if not is_matched_agent:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL WARN ] Step Day { self.day_num }, File { phase_step_file } has no any task!")
            sys.exit(0)
        
        # tracing
        print(f"[ 💀 {self.agent_id} Agent | INFO ] Step Day { self.day_num }, File { phase_step_file }, Execute Agent Project {project_name}...")
        
        # pre-execute
        self.pre_execute()
        
        # check whether exists any components for this agent
        components = target_day["components"] if target_day else []
        components = components if components else []
        if len(components) <= 0:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL WARN ] Step Day { self.day_num }, File { phase_step_file } has no any components!")
            sys.exit(0)
        
        # read global context md
        global_context_file, global_context = read_file_raw(agent_helper.resolve_absolute_path(steps_data["global_context_file"]))
        if not global_context:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL ERROR ] Not found GLOBAL project context markdown { global_context_file }")
            sys.exit(1)
        
        # request phase context
        phase_context_file, phase_context = read_file_raw(agent_helper.resolve_absolute_path(target_day["context_file"]))
        if not phase_context:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL ERROR ] Not found PHASE context markdown { phase_context_file }")
            sys.exit(1)
        
        # prepare prompt context
        pattern = rf"(## {target_day['context_section']}:.*?)((?=\n## DAY )|\Z)"
        day_context = re.search(pattern, phase_context, re.DOTALL | re.IGNORECASE).group(1).strip()
        sub_tasks_in_day = [t for t in target_day["sub_tasks"] if self.agent_id == t['agent'] or t['desc'].startswith(self.agent_id)]
        sub_tasks = "\n".join([f"- {t['desc']}" for t in sub_tasks_in_day])
        
        # do agent
        log_history_file = self.agent_log_file()
        latest_target_component = None
        latest_user_prompt = None
        while True:
            try:
                # iterate every task in day
                for sub_task in sub_tasks_in_day:
                    components = sub_task['components']
                    if not components or len(components) <= 0:
                        print(f"[ 💀 {self.agent_id} Agent | CRITICAL WARN ] Step Day { self.day_num }, File { phase_step_file } has no any task components!")
                        continue
                    
                    # iterate every target component
                    for component in components:
                        componentParts = component.split(";")
                        source_component = componentParts[0] if len(componentParts) > 0 else "INTEGRATION_SCOPE"
                        target_component = componentParts[1] if len(componentParts) > 1 else ""
                        latest_target_component = target_component
                        
                        # check if invalid target component
                        if len(target_component) <= 0:
                            print(f"[ 💀 {self.agent_id} Agent | CRITICAL WARN ] Step Day { self.day_num }, File { phase_step_file }, Target Component not found to do")
                            continue
                        
                        # execute task
                        success, system_prompt, user_prompt, raw_response = self.execute_task(
                            project_name=project_name,
                            global_context=global_context,
                            day_context=day_context,
                            source_component=source_component,
                            target_component=target_component,
                            sub_tasks=sub_tasks
                        )
                        
                        # for tracing
                        latest_user_prompt = user_prompt
                        if not success:
                            raise RuntimeError(raw_response)
                        
                        # write AI response log
                        self.write_log(
                            log_file=log_history_file,
                            target_component=target_component,
                            user_prompt=user_prompt,
                            data=raw_response
                        )
                
                # done tasks
                return True
            except Exception as e:
                print(f"[ 💀 {self.agent_id} Agent | ERROR ] Exception caught on model {self.current_model_config['model_name']}: {str(e)}")
                # write log
                self.write_log(
                    log_file=log_history_file,
                    target_component=latest_target_component,
                    user_prompt=latest_user_prompt,
                    data=str(e)
                )
                self.active_model_index += 1
                if not self.rotate_model():
                    return False

