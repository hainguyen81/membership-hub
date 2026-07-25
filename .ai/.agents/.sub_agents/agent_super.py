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
from _0d_ai._0d_agents.agent_0u_helper import (
    resolve_absolute_path,
    read_file_raw,
    write_file,
    exception_stacktrace,
    kwargs_by_key
)
from _0d_ai._0d_agents.agent_0u_super import AbstractAgent

# Now Python can seamlessly see and import the centralized helper utility cleanly!
from _0d_ai._0d_agents._0d_sub_0u_agents.helper import write_sub_agent_history

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
MODELS_POOL_PATH            = resolve_absolute_path(".ai/.agents/.models/models.json")
STEPS_PLAN_DIR              = resolve_absolute_path(".ai/.plan/.steps")

class AbstractSubAgent(AbstractAgent):
    def __init__(self, agent_id, phase_str, day_num, **kwargs):
        super().__init__(agent_id=agent_id if agent_id else "SubSuper", **kwargs)
        self.phase_str = phase_str
        self.day_num = int(day_num)
    
    def write_history_log(self, log_file, source_component, target_component, user_prompt, data, append=False):
        return write_sub_agent_history(
            history_file=log_file,
            day=self.day_num,
            model_name=self.current_model_config["model_name"],
            api_endpoint=self.current_model_config["api_endpoint"],
            source_component=source_component,
            target_component=target_component,
            prompt=user_prompt,
            data=data,
            append=append
        )
    
    def collect_agent_tasks(self, target_day):
        tasks = []
        for task in target_day['sub_tasks'] if target_day['sub_tasks'] else []:
            if (task["agent"] and self.agent_id.lower() == task["agent"].lower()) or (task["desc"] and task["desc"].startswith(self.agent_id)):
                tasks.append(task)
        return tasks
    
    def collect_agent_components(self, tasks):
        components = []
        for task in tasks if isinstance(tasks, list) else []:
            for component in task["components"] if isinstance(task["components"], list) else []:
                components.append(component)
        return components
    
    # @override
    def build_user_prompt_context(self, **kwargs):
        source_component = self.get_kwargs_by_key(key="source_component", **kwargs)
        if os.path.exists(source_component):
            lang_code = "typescript" if source_component.endswith(('.ts', '.tsx', '.js')) else "java"
            _, source_payload = read_file_raw(source_component.strip())
            source_payload = f"```{lang_code}\n{source_payload.strip()}\n```"
        else:
            source_component = "INTEGRATION_SCOPE"
            source_payload = None
        kwargs = {
            **kwargs,
            "source_component": source_component,
            "source_payload": source_payload
        }
        return super().build_user_prompt_context(**kwargs)
    
    # @ override
    def clean_response(self, raw_response, **kwargs):
        return raw_response.replace("```java", "").replace("```ts", "").replace("```tsx", "").replace("```", "").strip() if raw_response else None
    
    # @ override
    def process_chat(self, response_data, **kwargs):
        target_component = self.get_kwargs_by_key(key="target_component", **kwargs)
        write_file(
            file=target_component,
            data=response_data
        )
        print(f"[ ✅ {self.agent_id} Agent - SUCCESS | Model {self.current_model_config['model_name']} | API Endpoint {self.current_model_config['api_endpoint']} | Day {self.day_num} ] Saved to: { target_component }")
    
    # @ override
    def pre_execute(self, **kwargs):
        # read JSON steps
        phase_step_file = f"phase-{self.phase_str}.steps.json"
        _, steps_data = read_json_file(os.path.join(STEPS_PLAN_DIR, phase_step_file))
        if not steps_data:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL ERROR ] Not found phase steps JSON file { phase_step_file }")
            sys.exit(1)
        
        # parse project name from phase steps data
        datetimeStr = datetime.now().strftime("%Y%m%d%H%M%S")
        defaultPrjName = f"project-{datetimeStr}"
        project_name = steps_data["project_name"] if steps_data["project_name"] else defaultPrjName
        
        # check agent from JSON steps
        target_day = next((d for d in steps_data["days"] if d["day"] == self.day_num), None)
        agent_tasks = self.collect_agent_tasks(target_day)
        if not agent_tasks or len(agent_tasks) <= 0:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL WARN ] Step Day { self.day_num }, File { phase_step_file } has no any task!")
            sys.exit(0)
        
        # tracing
        print(f"[ 💀 {self.agent_id} Agent | INFO ] Step Day { self.day_num }, File { phase_step_file }, Execute Agent Project {project_name}...")
        
        # check whether exists any components for this agent
        components = self.collect_agent_components(agent_tasks)
        if not components or len(components) <= 0:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL WARN ] Step Day { self.day_num }, File { phase_step_file } has no any components!")
            sys.exit(0)
        
        # read global context md
        global_context_file, global_context = read_file_raw(resolve_absolute_path(steps_data["global_context_file"]))
        if not global_context:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL ERROR ] Not found GLOBAL project context markdown { global_context_file }")
            sys.exit(1)
        
        # request phase context
        phase_context_file, phase_context = read_file_raw(resolve_absolute_path(target_day["context_file"]))
        if not phase_context:
            print(f"[ 💀 {self.agent_id} Agent | CRITICAL ERROR ] Not found PHASE context markdown { phase_context_file }")
            sys.exit(1)
        
        # prepare prompt context
        pattern = rf"(## {target_day['context_section']}:.*?)((?=\n## DAY )|\Z)"
        day_context = re.search(pattern, phase_context, re.DOTALL | re.IGNORECASE).group(1).strip()
        sub_tasks = "\n".join([f"- {t['desc']}" for t in agent_tasks])
        
        # return merged new values
        return {
            **kwargs,
            "project_name": project_name,
            "phase_step_file": phase_step_file,
            "agent_tasks": agent_tasks,
            "global_context": global_context,
            "phase_context": phase_context,
            "day_context": day_context,
            "sub_tasks": sub_tasks.strip()
        }
    
    # @override
    def __handle_execute_exception__(self, e, **kwargs):
        model_name = self.current_model_config['model_name'] if self.current_model_config else None
        print(f"[ 💀 {self.agent_id} Agent | ERROR ] Exception caught on model {model_name}: {exception_stacktrace(e)}")
        # write log
        self.write_history_log(
            log_file=self.agent_log_file(),
            source_component=kwargs_by_key(key="latest_source_component", **kwargs),
            target_component=kwargs_by_key(key="latest_target_component", **kwargs),
            user_prompt=kwargs_by_key(key="latest_user_prompt", **kwargs),
            data=exception_stacktrace(e),
            append=True
        )
    
    def __do_task_component__(self, **kwargs):
        # execute task
        success, system_prompt, user_prompt, raw_response = self.__execute__(
            project_name=kwargs_by_key(key="project_name", **kwargs),
            global_context=kwargs_by_key(key="global_context", **kwargs),
            day_context=kwargs_by_key(key="day_context", **kwargs),
            source_component=kwargs_by_key(key="source_component", **kwargs),
            target_component=kwargs_by_key(key="target_component", **kwargs),
            sub_tasks=kwargs_by_key(key="sub_tasks", **kwargs)
        )
        
        # for tracing
        kwargs = {
            **kwargs,
            "system_prompt": system_prompt,
            "user_prompt": user_prompt,
            "latest_system_prompt": system_prompt,
            "latest_user_prompt": user_prompt,
            "raw_response": raw_response
        }
        if not success:
            raise RuntimeError(raw_response)
        
        # return new kwargs
        return { **kwargs }
    
    # @ override
    def __do_execute__(self, **kwargs):
        # extract arguments
        phase_step_file = kwargs_by_key(key="phase_step_file", **kwargs)
        agent_tasks = kwargs_by_key(key="agent_tasks", **kwargs)
        
        # iterate every task in day
        for sub_task in agent_tasks:
            components = sub_task['components']
            if not components or len(components) <= 0:
                print(f"[ 💀 {self.agent_id} Agent | CRITICAL WARN ] Step Day { self.day_num }, File { phase_step_file } has no any task components!")
                continue
            
            # iterate every target component
            for component in components:
                componentParts = component.split(";")
                source_component = componentParts[0] if len(componentParts) > 1 else "INTEGRATION_SCOPE"
                target_component = componentParts[0] if 0 < len(componentParts) < 2 else componentParts[1] if len(componentParts) > 1 else ""
                kwargs = {
                    **kwargs,
                    "source_component": source_component,
                    "target_component": target_component,
                    "latest_source_component": source_component,
                    "latest_target_component": target_component
                }
                
                # check if invalid target component
                if len(target_component) <= 0:
                    print(f"[ 💀 {self.agent_id} Agent | CRITICAL WARN ] Step Day { self.day_num }, File { phase_step_file }, Target Component not found to do")
                    continue
                
                # do task component
                kwargs = self.__do_task_component__(**kwargs)
                
                # write AI response log
                self.write_history_log(
                    log_file=log_history_file,
                    source_component=kwargs_by_key(key="source_component", **kwargs),
                    target_component=kwargs_by_key(key="target_component", **kwargs),
                    user_prompt=kwargs_by_key(key="user_prompt", **kwargs),
                    data=kwargs_by_key(key="raw_response", **kwargs),
                    append=True
                )
            
            # end components interation
        
        # end sub-tasks interation
        
        # return new values dict
        return { **kwargs }

