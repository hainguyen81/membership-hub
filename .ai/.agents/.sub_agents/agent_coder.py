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
# request agent_helper from `site-packages/load_modules.pth`
agent_helper = sys.modules["agent_helper"]

# super agent
from agent_super import AbstractAgent

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
SYSTEM_PROMPT_FILE          = agent_helper.resolve_absolute_path(".ai/.agents/.sub_agents/agent_coder.prompt.system.md")
USER_PROMPT_FILE            = agent_helper.resolve_absolute_path(".ai/.agents/.sub_agents/agent_coder.prompt.user.md")

class CoderAgent(AbstractAgent):
    def __init__(self, project_name, phase_str, day_num):
        super().__init__(
            project_name=project_name,
            agent_id="Coder",
            phase_str=phase_str,
            day_num=day_num
        )
    
    def agent_secrets_key(self) -> str:
        pass

    def agent_log_file(self) -> str:
        return agent_helper.resolve_absolute_path(f".ai/.history/agent-coder-day-{self.day_num}.md")
    
    def system_prompt_template(self) -> str:
        return SYSTEM_PROMPT_FILE
    
    def user_prompt_template(self) -> str:
        return USER_PROMPT_FILE
    
    def pre_execute(self):
        pass

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    parser.add_argument("--project-name", required=True)
    args = parser.parse_args()
    CoderAgent(
        project_name=args.project_name,
        phase_str=args.phase,
        day_num=args.day
    ).execute()
