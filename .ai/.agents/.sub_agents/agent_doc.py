# .ai/.agents/.sub-agents/agent-doc.py
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
from _0d_ai._0d_agents.agent_0u_helper import (
    resolve_absolute_path,
    exception_stacktrace,
    kwargs_by_key
)

# super agent
from _0d_ai._0d_agents._0d_sub_0u_agents.agent_0u_super import AbstractSubAgent

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
AGENT_ID                    = "Doc"
SYSTEM_PROMPT_FILE          = resolve_absolute_path(".ai/.agents/.sub_agents/agent_doc.prompt.system.md")
USER_PROMPT_FILE            = resolve_absolute_path(".ai/.agents/.sub_agents/agent_doc.prompt.user.md")

class DocumentationAgent(AbstractSubAgent):
    def __init__(self, phase_str, day_num):
        super().__init__(
            agent_id=AGENT_ID,
            phase_str=phase_str,
            day_num=day_num
        )
    
    # @override
    def agent_secrets_key(self) -> str:
        pass
    
    # @override
    def agent_log_file(self) -> str:
        return resolve_absolute_path(f".ai/.history/agent-doc-day-{self.day_num}.md")
    
    # @override
    def system_prompt_template(self) -> str:
        return SYSTEM_PROMPT_FILE
    
    # @override
    def user_prompt_template(self) -> str:
        return USER_PROMPT_FILE

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    args = parser.parse_args()
    print(f"📝 Activating technical documentation parsing and synchronization for Phase { args.phase } Day { args.day }...")
    DocumentationAgent(
        phase_str=args.phase,
        day_num=args.day
    ).execute()
