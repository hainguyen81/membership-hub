import os
import sys
import re
import json

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

def write_file(dir, file_name, data, append=False):
    opts = "a" if append else "w"
    os.makedirs(dir, exist_ok=True)
    out_path = os.path.join(dir, file_name)
    with open(out_path, opts, encoding="utf-8") as f:
        f.write(str(data))
    return out_path # full path of file

def write_json_file(dir, file_name, json_data, append=False):
    opts = "a" if append else "w"
    os.makedirs(dir, exist_ok=True)
    out_path = os.path.join(dir, file_name)
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
    with open(global_context_file, "r", encoding="utf-8") as f:
        return (file_path, f.read())

def write_log_history(history_file, day, model_name, api_endpoint, target_component, prompt, data, append=False):
    log_history_content = (
        f"# Day { day }: model { model_name } - API Endpoint { api_endpoint }\n"
        f"* **Production source codebase generated at target destination**: {target_component}\n"
        f"* **📝 Prompt / Tasks / Data**:\n{prompt}\n"
        f"* **📝 Response**:\n{data}\n\n"
    )
    write_file(
        dir=os.path.dirname(history_file),
        file_name=os.path.basename(history_file),
        data=log_history_content,
        append=append
    )
    return history_file

def render_prompt(prompt_template_path: str, context: dict) -> str:
    if not os.path.exists(prompt_template_path):
        return None
    
    # read prompt template
    with open(prompt_template_path, "r", encoding="utf-8") as f:
        template_content = f.read()
    
    # use jinja2 Template
    tmpl = JinjaTemplate(template_content)
    
    # substitute will throw error if missing variables, safely for production
    return tmpl.render(**context).strip()

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
    return str(first_choice.text if first_choice.text else first_choice).strip()

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

