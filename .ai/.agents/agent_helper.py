# ==============================================================================
# 🛠️ ENTERPRISE PIPELINE ENVIRONMENT-BASED PATH RESOLVER
# ==============================================================================
# Programmatically retrieves the absolute root directory of the active project 
# using GitHub Actions infrastructure environment tokens instead of brittle backtracking.
# ==============================================================================

import os
import sys
import json
import logging
import re
import json
import traceback
from pathlib import Path

# to load prompt template
from jinja2 import Template as JinjaTemplate

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

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
BLUEPRINT_WORKING_HISTORY_FILE = "sources/output/architecture-blueprint.md"

# logging configuration
# logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s]: %(message)s")
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)

def resolve_absolute_path(relative_target_path):
    """
    Ingests a relative path string and safely interpolates it using the absolute 
    workspace anchor provided natively by the GitHub Actions Runner environment.
    """
    # 🚀 CORE RAIL: Ingest the absolute repository root path straight from GitHub infrastructure
    # Fallback to current working directory (os.getcwd()) if executing on a local machine
    current_directory_path = os.getcwd()
    github_workspace = os.environ.get("GITHUB_WORKSPACE", '')
    project_workspace = os.environ.get("PROJECT_WORKSPACE", '')
    # print(f"CURRENT WORKING DIR: { current_directory_path } | GITHUB_WORKSPACE: { github_workspace } | PROJECT_WORKSPACE: { project_workspace }")
    repo_root_path = os.environ.get("PROJECT_WORKSPACE", os.environ.get("GITHUB_WORKSPACE", os.getcwd()))
    
    # Clean up the incoming string parameters by removing leading path descriptors
    cleaned_relative_path = relative_target_path.removeprefix("./")
    
    # Synthesize the non-negotiable absolute hardware computing path destinations
    absolute_hardware_path = os.path.join(repo_root_path, cleaned_relative_path)
    
    # full path from root workspace
    return absolute_hardware_path

def json_raw_content(raw_content):
    """Securely serialize input telemetry payloads into structural double-quoted strings."""
    # If the payload is already a memory object list or dictionary
    if isinstance(raw_content, (dict, list)):
        return json.dumps(raw_content, indent=4, ensure_ascii=False)
    
    if isinstance(raw_content, str):
        cleaned_str = raw_content.strip()
        # If it is a stringified JSON layout, decode and encode with indentation rules
        if (cleaned_str.startswith("{") or cleaned_str.startswith("[")) and '"' in cleaned_str:
            try:
                return json.dumps(json.loads(cleaned_str), indent=4, ensure_ascii=False)
            except Exception:
                pass
    
    return str(raw_content)

def exception_stacktrace(e) -> str:
    stacktrace = traceback.format_exception(type(e), e, e.__traceback__) if isinstance(e, BaseException) or isinstance(e, Exception) else None
    return None if not e else f"{str(e)}: {stacktrace}" if stacktrace else str(e)

def write_file(file, data, dir=None, append=False):
    checked_dir = dir if dir else os.path.dirname(file)
    checked_file = os.path.basename(file) if not dir else file
    opts = "a" if append else "w"
    os.makedirs(checked_dir, exist_ok=True)
    out_path = os.path.join(checked_dir, checked_file)
    with open(out_path, opts, encoding="utf-8") as f:
        f.write(str(data))
    return out_path # full path of file

def write_json_file(file, json_data, dir=None, append=False):
    checked_dir = dir if dir else os.path.dirname(file)
    checked_file = os.path.basename(file) if not dir else file
    opts = "a" if append else "w"
    os.makedirs(checked_dir, exist_ok=True)
    out_path = os.path.join(checked_dir, checked_file)
    with open(out_path, opts, encoding="utf-8") as f:
        json.dump(json_data, f, ensure_ascii=False, indent=4)
    return out_path # full path of file

def read_json_file(file_path):
    if not os.path.exists(file_path):
        return (None, None)
    
    # read json file
    with open(file_path, "r", encoding="utf-8") as f:
        return (file_path, json.load(f))

def read_file_raw(file_path):
    if not os.path.exists(file_path):
        return (None, None)
    
    # read file
    with open(file_path, "r", encoding="utf-8") as f:
        return (file_path, f.read())

def write_blueprint_log(phase_idx, instruction, prompt, raw_content, is_step, model_name=None, out_dir=None):
    pattern = r"\{.*\}|\[.*\]"
    raw_content = json_raw_content(raw_content)
    is_json = bool(re.search(pattern, raw_content, re.DOTALL))
    model_name_safe = f"AI Model: {model_name} - " if model_name and len(model_name) > 0 else ""
    if phase_idx <= 0:
        header_title = f"# {model_name_safe}Global Prompt:\n\n{prompt}\n\n"
    elif not is_step:
        header_title = f"# {model_name_safe}Phase {phase_idx} - Prompt:\n\n{prompt}\n\n"
    else:
        header_title = f"# {model_name_safe}Phase {phase_idx} STEPS - Prompt:\n\n{prompt}\n\n"
    instruction_block = f"# System Instruction\n\n{instruction}\n\n"
    if is_json:
        response_block = f"# Raw Response / Exception:\n\n```json\n{raw_content}\n```\n\n"
    else:
        response_block = f"# Raw Response / Exception:\n\n```text\n{raw_content}\n```\n\n"
    log_content = header_title + instruction_block + response_block
    log_file = resolve_absolute_path(BLUEPRINT_WORKING_HISTORY_FILE)
    if out_dir and len(out_dir) > 0:
        log_file = os.path.join(out_dir, "architecture-blueprint.md")
    write_file(os.path.dirname(log_file), os.path.basename(log_file), log_content, append=True)

def delete_file(file):
    if os.path.exists(file):
        os.remove(file)

def delete_log(out_dir=None):
    log_file = resolve_absolute_path(BLUEPRINT_WORKING_HISTORY_FILE)
    if out_dir and len(out_dir) > 0:
        log_file = os.path.join(out_dir, "architecture-blueprint.md")
    delete_file(file=log_file)

def render_prompt(template: str, context: dict) -> str:
    if not os.path.exists(template):
        return None
    
    # read prompt template
    _, template_content = read_file_raw(template)
    
    # use jinja2 Template
    tmpl = JinjaTemplate(template)
    
    # substitute will throw error if missing variables, safely for production
    return tmpl.render(**context).strip()

def render_kwargs_prompt(template: str, **kwargs) -> str:
    return render_prompt(template=template, context={ **kwargs })

def validateOpenAIResponse(response):
    if not response or not hasattr(response, 'choices') or not response.choices:
        raise RuntimeError(f"[API Upstream Error 404]: No Response Found")
    
    # 1. Check response choices
    choices_data = response.choices
    if not isinstance(choices_data, list) or len(choices_data) <= 0:
        raise RuntimeError(f"[API Upstream Error 404]: Response Choices is empty/None")
    
    # parse first choice
    first_choice = choices_data[0]
        
    # 2. Check finish_reason or error response
    if first_choice.finish_reason == 'error' or hasattr(response, 'error') or (hasattr(first_choice, 'error') and choice.error):
        # parse error
        err_detail = getattr(response, 'error', None) or getattr(first_choice, 'error', {})
        err_msg = err_detail.get('message', 'Unknown upstream aggregator timeout')
        err_code = err_detail.get('code', 500)
        raise RuntimeError(f"[API Upstream Error {err_code}]: {err_msg}")
        
    # 3. check content whether is None (although finish_reason is `stop`)
    if not hasattr(first_choice, 'message') or not first_choice.message or first_choice.message.content is None:
        raise ValueError(f"[API Upstream Error 404]: AI response content is empty/None.")
    
    # Guard against malformed message blocks or unexpected payload closures
    return first_choice

def parseOpenAIResponseData(response):
    """
    Safely parses text responses from OpenAI completion models.
    Protects the runtime from attribute errors if content fields are blank or null.
    """
    first_choice = validateOpenAIResponse(response)
    
    # Guard against malformed message blocks or unexpected payload closures
    message_obj = first_choice.message
    if hasattr(message_obj, 'content') and message_obj.content:
        return message_obj.content.strip()
    
    # Safe fallback if choice format changes or breaks unexpectedly
    return str(first_choice).strip()

def splitOpenAIResponseJsonData(raw_data):
    clean_json_str = raw_data.strip()
    
    # 💡 Use find() to split json block
    lower_raw = clean_json_str.lower()
    start_tag = "```json"
    end_tag = "```"
    
    if start_tag in lower_raw:
        start_idx = lower_raw.find(start_tag) + len(start_tag)
        end_idx = lower_raw.find(end_tag, start_idx)
        if end_idx != -1:
            clean_json_str = clean_json_str[start_idx:end_idx].strip()
    
    elif "```" in lower_raw:
        start_idx = lower_raw.find("```") + 3
        end_idx = lower_raw.find("```", start_idx)
        if end_idx != -1:
            clean_json_str = clean_json_str[start_idx:end_idx].strip()
    
    return clean_json_str

def parseOpenAIResponseJsonData(response):
    """
    Extracts and deserializes raw response texts into fully validated Python dict layouts.
    Leverages non-greedy structural indexing to filter out conversational agent summaries.
    """
    # Ingest text payload through the hardened safety parser above
    raw_data = parseOpenAIResponseData(response)
    
    if not raw_data:
        return (None, None)
        
    # Pattern 1: Targeted scan for standard markdown language JSON codeblocks
    json_match = re.search(r"```json\s*([\s\S]*?)\s*```", raw_data, re.DOTALL)
    if json_match:
        try:
            clean_json_str = json_match.group(1).strip()
            return (raw_data, json.loads(clean_json_str))
        except Exception:
            pass # Continue evaluating alternative pattern structures if parsing breaks
            
    # Pattern 2: Generic codeblock fallback without language tags
    json_match = re.search(r"```\s*([\s\S]*?)\s*```", raw_data, re.DOTALL)
    if json_match:
        try:
            clean_json_str = json_match.group(1).strip()
            return (raw_data, json.loads(clean_json_str))
        except Exception:
            pass

    # Pattern 3: Hardened bracket boundary locator leveraging non-greedy isolation
    # Fixes the broken greedy regex logic to ensure text outside the curly braces is safely ignored
    try:
        return (raw_data, json.loads(splitOpenAIResponseJsonData(raw_data)))
    except Exception as e:
        json_match = re.search(r"(\{[\s\S]*\})", raw_data, re.DOTALL)
        if json_match:
            try:
                clean_json_str = json_match.group(1).strip()
                return (raw_data, json.loads(clean_json_str))
            except Exception:
                pass
        
        else:
            pass
            
    # Final Fallback Layer: Treat the whole string as literal plain text payload
    try:
        return (raw_data, json.loads(raw_data.strip()))
    except Exception as final_error:
        print(f"⚠️  [PARSER WARNING] Local string-to-json mapping failed: {final_error}")
        return (raw_data, None)

def count_files_by_pattern(dir, file_filter_pattern) -> int:
    folder_path = Path(dir).resolve()
    if not folder_path.is_dir():
        return 0
    
    file_pattern = file_filter_pattern.strip() if file_filter_pattern.strip() else "*"
    return sum(1 for item in folder_path.glob(file_pattern) if item.is_file())

def kwargs_by_key(key: str, **kwargs):
    return (kwargs or {}).get(key) if key else None
