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
# request agent_helper from `.libs/project_agents_package_loader.py`
from _ai._agents import agent_helper

# Now Python can seamlessly see and import the centralized helper utility cleanly!
from _ai._agents._sub_agents.helper import render_prompt

# super agent
from _ai._agents._sub_agents.agent_super import AbstractAgent

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
SYSTEM_PROMPT_FILE          = agent_helper.resolve_absolute_path(".ai/.agents/.sub_agents/agent_reviewer.prompt.system.md")
USER_PROMPT_FILE            = agent_helper.resolve_absolute_path(".ai/.agents/.sub_agents/agent_reviewer.prompt.user.md")
BACKEND_WORKSPACE           = agent_helper.resolve_absolute_path("sources/backend")
FRONTEND_WORKSPACE          = agent_helper.resolve_absolute_path("sources/frontend")

class BugFixerAgent(AbstractAgent):
    def __init__(self, phase_str, day_num):
        super().__init__(
            agent_id="Reviewer",
            phase_str=phase_str,
            day_num=day_num
        )
    
    def agent_secrets_key(self) -> str:
        pass

    def agent_log_file(self) -> str:
        return agent_helper.resolve_absolute_path(f".ai/.history/agent-reviewer-day-{self.day_num}.md")
    
    def system_prompt_template(self) -> str:
        return SYSTEM_PROMPT_FILE
    
    def user_prompt_template(self) -> str:
        return USER_PROMPT_FILE
    
    def pre_execute(self):
        pass
    
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
    
    def check_project_initialized(self, target_component):
        if "backend" in target_component:
            pom_path = os.path.join(BACKEND_WORKSPACE, "pom.xml")
            # if not found pom.xml, it means project empty or be initializing
            return (os.path.exists(pom_path), pom_path)
        else:
            package_path = os.path.join(FRONTEND_WORKSPACE, "package.json")
            # if not found package.json, it means project empty or be initializing
            return (os.path.exists(package_path), package_path)
    
    def execute_task(self, project_name, global_context, day_context, source_component, target_component, sub_tasks):
        # check whether project had been initialized
        project_initialized, project_main_component = self.check_project_initialized(target_component)
        print(f"[ ℹ️ {self.agent_id} Agent | F.Y.I ] Project {project_name} had been initialized?. {project_initialized} - Project Main Component: {project_main_component}")
        print(f"            - Target Component: {target_component}")
        
        # build system prompt
        system_prompt_context = self.build_system_prompt_context(global_context, day_context, target_component)
        system_prompt = render_prompt(self.system_prompt_template(), system_prompt_context)
        
        # test component 3 time(s)
        user_prompt = None
        latest_response = None
        success = False
        max_iterations = 3
        for iteration in range(1, max_iterations + 1):
            # only compile project when it had been initialized
            is_clean, compiler_log = self.run_compile_check(target_component, project_initialized)
            if is_clean:
                print(f"[ ✅ {self.agent_id} Agent | SUCCESS ] Target codebase component compiled cleanly on iteration loop: {iteration}!")
                latest_response = f"Target codebase component {target_component} compiled cleanly on iteration loop {iteration}"
                success = True
                break
            
            # build user prompt
            print(f"[ ⚠️ {self.agent_id} Agent | WARNING ] Build check failed on validation loop: {iteration}. Ingesting raw error logs...")
            user_prompt_context = self.build_user_prompt_context(source_component, target_component, sub_tasks, compiler_error_logs=compiler_log)
            user_prompt = render_prompt(self.user_prompt_template(), user_prompt_context)
            
            # agent do job
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
                print(f"[ 💀 {self.agent_id} Agent | RECOVERY ] API transaction exception caught. Swapping model: {str(e)}")
                latest_response = str(e) if not latest_response else latest_response
                self.active_model_index += 1
                if not self.rotate_model():
                    success = False
                    break
        
        if not success:
            print("[ 💀 {self.agent_id} Agent | CRITICAL ] Structural compiler repairs failed within maximum iteration bounds.")
        return (success, system_prompt, user_prompt, latest_response)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    args = parser.parse_args()
    print(f"🩹 Initiating compiler analysis and automated code healing routines for Phase { args.phase } Day { args.day }...")
    BugFixerAgent(
        phase_str=args.phase,
        day_num=args.day
    ).execute()
