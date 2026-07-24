# .ai/.agents/agent_super.py

import os
import sys
import json
import re
import argparse
from datetime import datetime
from openai import OpenAI

# for abstract class
from abc import ABC, abstractmethod

# agent helper
from _ai._agents.agent_helper import (
    resolve_absolute_path,
    write_file,
    read_json_file,
    render_prompt,
    parseOpenAIResponseData,
    exception_stacktrace,
    kwargs_by_key
)

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
MODELS_POOL_PATH            = resolve_absolute_path("sources/agents/models/models.json")

class AbstractAgent(ABC):
    def __init__(self, agent_id, **kwargs):
        self.agent_id = agent_id if agent_id else "Super"
        self.kwargs = kwargs
        self.secrets_key = self.agent_secrets_key()
        self.secrets = self.load_secrets(self.secrets_key)
        self.initialize()
    
    def initialize(self):
        self.models_secrets_key = self.agent_models_secrets_key()
        self.models_secrets = self.load_secrets(self.models_secrets_key)
        self.initialize_models()
    
    def initialize_models(self):
        self.models_pool = self.load_models_pool()
        self.active_model_index = 0
        self.client = None
        self.current_model_config = None
        if not self.rotate_model():
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL ] Not found any available AI models to execute!")
            sys.exit(1)
    
    def get_kwargs(self, key: str):
        return kwargs_by_key(self.kwargs, key)
    
    def agent_models_secrets_key(self) -> str:
        return "AI_MODELS_KEYS_JSON"
    
    @abstractmethod
    def agent_secrets_key(self) -> str:
        pass
    
    def agent_secrets(self, key, defVal=None):
        return self.secrets.get(key, defVal) if self.secrets and key and len(key) > 0 else defVal
    
    def load_secrets(self, secrets_key):
        if not secrets_key or len(secrets_key) <= 0:
            print(f"[ 💀 {self.agent_id} Agent | WARN ] Invalid secrets key to load secrets!")
            return None
        
        # load secrets from environment
        raw_secrets = os.environ.get(secrets_key)
        if not raw_secrets:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL ] The environment variable '{secrets_key}' is completely absent.")
            sys.exit(1)
        
        # parse secrets to JSON
        try:
            return json.loads(raw_secrets)
        except Exception as e:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL ] Failed to parse environment '{secrets_key}' JSON string: {exception_stacktrace(e)}")
            sys.exit(1)
    
    def load_models_pool(self):
        _, models_json = read_json_file(MODELS_POOL_PATH)
        return models_json
    
    def rotate_model(self):
        if not self.models_secrets or len(self.models_secrets) <= 0:
            print(f"[ 💀 {self.agent_id} Agent | WARN ] Not found any models secrets to rotate!")
            return False

        while 0 <= self.active_model_index < len(self.models_pool):
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
            
            api_key = self.models_secrets.get(target_model_endpoint)
            if api_key:
                self.current_model_config = config
                self.client = OpenAI(api_key=api_key, base_url=target_model_endpoint)
                print(f"[ 💀 {self.agent_id} Agent | FAILOVER ENGAGED ] Successfully authenticated model: {target_model_name} | endpoint: {target_model_endpoint}")
                return True
            self.active_model_index += 1
        print(f"[ 💀 {self.agent_id} Agent | CRITICAL ERROR ] Exhausted all registered fallback models: model_interation {self.active_model_index} models number {len(self.models_pool)}")
        return False
    
    @abstractmethod
    def agent_log_file(self) -> str:
        pass
    
    def write_log(self, data, append=False):
        return write_file(
            file=self.agent_log_file(),
            data=data,
            append=append
        )
    
    @abstractmethod
    def system_prompt_template(self) -> str:
        pass
    
    def build_system_prompt_context(self, **kwargs):
        return { **kwargs }
    
    def build_system_prompt(self, **kwargs) -> str:
        system_prompt_context = self.build_system_prompt_context(**kwargs)
        return render_prompt(self.system_prompt_template(), system_prompt_context)
    
    @abstractmethod
    def user_prompt_template(self) -> str:
        pass
    
    def build_user_prompt_context(self, **kwargs):
        return { **kwargs }
    
    def build_user_prompt(self, **kwargs) -> str:
        user_prompt_context = self.build_user_prompt_context(**kwargs)
        return render_prompt(self.user_prompt_template(), user_prompt_context)
    
    def agent_temperature(self):
        return 0.1
    
    def chat(self, system_prompt, user_prompt, **kwargs):
        response = self.client.chat.completions.create(
            model=self.current_model_config["model_name"],
            messages=[{"role": "system", "content": system_prompt}, {"role": "user", "content": user_prompt}],
            temperature=self.agent_temperature()
        )
        raw_response = parseOpenAIResponseData(response)
        return (raw_response, self.clean_response(raw_response, **kwargs))
    
    def clean_response(self, raw_response, **kwargs):
        return raw_response
    
    @abstractmethod
    def process_chat(self, response_data, **kwargs):
        pass
    
    @abstractmethod
    def pre_execute(self, **kwargs):
        pass
    
    def __ai_execute__(self, **kwargs):
        # ask AI
        raw_response, clean_response = self.chat(
            system_prompt=kwargs_by_key(key="system_prompt", **kwargs),
            user_prompt=kwargs_by_key(key="user_prompt", **kwargs),
            **kwargs
        )
        latest_response = raw_response
        
        # process AI response
        self.process_chat(clean_response, **kwargs)
        print(f"[ ✅ {self.agent_id} Agent - SUCCESS | Model {self.current_model_config['model_name']} | API Endpoint {self.current_model_config['api_endpoint']} ] Process successfully!")
        
        # return new values kwargs
        return {
            **kwargs,
            "clean_response": clean_response,
            "raw_response": raw_response,
            "latest_response": latest_response
        }
    
    def __execute__(self, **kwargs):
        # agent do job
        system_prompt = None
        user_prompt = None
        latest_response = None
        success = False
        try:
            # build system prompt
            system_prompt = self.build_system_prompt(**kwargs)
            # build user prompt
            user_prompt = self.build_user_prompt(**kwargs)
            
            # build new values kwargs
            kwargs = {
                **kwargs,
                "system_prompt": system_prompt,
                "user_prompt": user_prompt
            }
            
            # ask AI execution
            kwargs = self.__ai_execute__(**kwargs)
            latest_response = kwargs_by_key(key="latest_response", **kwargs)
            success = True
        except Exception as e:
            print(f"[ 💀 {self.agent_id} Agent | ERROR ] Exception caught on model {self.current_model_config['model_name']}: {exception_stacktrace(e)}")
            latest_response = exception_stacktrace(e) if not latest_response else latest_response
        
        # result
        return (success, system_prompt, user_prompt, latest_response)
    
    def __handle_execute_exception__(self, e, **kwargs):
        model_name = self.current_model_config['model_name'] if self.current_model_config else None
        print(f"[ 💀 {self.agent_id} Agent | ERROR ] Exception caught on model {model_name}: {exception_stacktrace(e)}")
        # write log
        self.write_log(
            data=exception_stacktrace(e),
            append=True
        )
    
    def __rotate_next_model__(self):
        self.active_model_index += 1
        return self.rotate_model()
    
    def __do_execute__(self, **kwargs):
        # internal execution
        success, system_prompt, user_prompt, raw_response = self.__execute__(**kwargs)
        if not success:
            raise RuntimeError(raw_response) # response is exception stack-trace from `__execute__`
        
        # done tasks
        return {
            **kwargs,
            "system_prompt": system_prompt,
            "user_prompt": user_prompt,
            "raw_response": raw_response
        }
    
    def execute(self, **kwargs):
        # pre-execute
        kwargs = self.pre_execute(**kwargs)
        
        # execute
        while True:
            try:
                # internal execution
                kwargs = self.__do_execute__(**kwargs)
                # done tasks
                return True
            except Exception as e:
                self.__handle_execute_exception__(e, **kwargs)
                # rotate next model
                if not self.__rotate_next_model__():
                    return False

