# .ai/.agents/.sub-agents/agent-tester.py
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
# request agent_helper from `.libs/project_agents_package_loader.py`
from _ai._agents import agent_helper

# super agent
from _ai._agents._sub_agents.agent_super import AbstractAgent

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
SYSTEM_PROMPT_FILE          = agent_helper.resolve_absolute_path(".ai/.agents/.sub_agents/agent_tester.prompt.system.md")
USER_PROMPT_FILE            = agent_helper.resolve_absolute_path(".ai/.agents/.sub_agents/agent_tester.prompt.user.md")

class TesterAgent(AbstractAgent):
    def __init__(self, phase_str, day_num):
        super().__init__(
            agent_id="Tester",
            phase_str=phase_str,
            day_num=day_num
        )
    
    def agent_secrets_key(self) -> str:
        pass

    def agent_log_file(self) -> str:
        return agent_helper.resolve_absolute_path(f".ai/.history/agent-tester-day-{self.day_num}.md")
    
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
    args = parser.parse_args()
    print(f"🧪 Activating quality assurance testing synthesis engine for Phase { args.phase } Day { args.day }...")
    TesterAgent(
        phase_str=args.phase,
        day_num=args.day
    ).execute()
