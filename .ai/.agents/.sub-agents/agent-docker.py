# .ai/.agents/.sub-agents/agent-docker.py
import os
import sys
import json
import argparse
import subprocess

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

# Now Python can seamlessly see and import the centralized helper utility cleanly!
from agent_helper import resolve_absolute_path

# ==============================================================================
# GLOBAL CONFIGURATION PATHS & BLUEPRINTS - STRICT PATH ALIGNMENT
# ==============================================================================
STEPS_PLAN_DIR              = resolve_absolute_path(".ai/.agents/.steps")
BACKEND_DOCKERFILE          = resolve_absolute_path("sources/backend/src/main/docker/Dockerfile.native")
FRONTEND_DOCKERFILE         = resolve_absolute_path(("sources/frontend/Dockerfile")
agent_working_history_file  = resolve_absolute_path(".ai/.history/agent-docker.md")

class DockerHubAgent:
    """
    Cloud Registry Agent. Parses centralized DOCKERHUB_SECRETS JSON from GitHub,
    authenticates against Docker Hub infrastructure, and pushes upstream with strict 'none' checking logic.
    """
    def __init__(self, phase_str, day_num):
        self.phase_str = phase_str
        self.day_num = int(day_num)
        self.image_tag = f"day-{self.day_num}"
        self.secrets = self.load_dockerhub_secrets()

    def load_dockerhub_secrets(self):
        raw_secrets = os.environ.get("DOCKERHUB_SECRETS")
        if not raw_secrets:
            print("[ 💀 DOCKERHUB-AGENT CRITICAL ] The environment variable 'DOCKERHUB_SECRETS' is completely absent.")
            sys.exit(1)
        try:
            return json.loads(raw_secrets)
        except Exception as e:
            print(f"[ 💀 DOCKERHUB-AGENT CRITICAL ] Failed to parse DOCKERHUB_SECRETS JSON string: {str(e)}")
            sys.exit(1)

    def authenticate_dockerhub(self):
        print("[DOCKERHUB-AGENT] Attaching secure registry authorization handshakes...")
        username = self.secrets.get("DOCKERHUB_USERNAME")
        password = self.secrets.get("DOCKERHUB_PASSWORD")

        if username and password:
            login_process = subprocess.Popen(
                ["docker", "login", "-u", username, "--password-stdin"],
                stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True
            )
            stdout, stderr = login_process.communicate(input=password)
            if login_process.returncode != 0:
                print(f"[ 💀 DOCKERHUB-AGENT ERROR] Authentication verification failed natively: {stderr}")
                sys.exit(1)
            print("[ ✅ DOCKERHUB-AGENT SUCCESS ] Docker Hub authentication session activated successfully.")
        else:
            print("[ ⚠️ DOCKERHUB-AGENT WARNING ] Missing data keys parameters inside DOCKERHUB_SECRETS mapping registry.")
    
    def write_history(self, target_image, desc):
        # write working history
        history_content = (
            f"# Day {self.day_num}: Build DockerHub Image {target_image}\n"
            f"* **Target Image Tag**: {target_image}\n"
        )
        with open(agent_working_history_file, "a", encoding="utf-8") as file:
            file.write(history_content)

    def execute_dockerhub_pipeline(self):
        steps_path = f"{STEPS_PLAN_DIR}/phase-{self.phase_str}.agent.steps.json"
        with open(steps_path, "r", encoding="utf-8") as f:
            steps_data = json.load(f)
        target_day = next((d for d in steps_data["days"] if d["day"] == self.day_num), None)

        # FIXED RULE: Ingest target repository identifier name directly from the steps configurations
        dockerhub_repo_name = target_day.get("dockerhub_component", "none")
        
        # UNIFIED CHECK GATE: If explicitly marked as 'none', skip the registry push pipeline cleanly
        if dockerhub_repo_name == "none":
            print("[ ⚠️ DOCKERHUB-AGENT SKIP ] Step is explicitly marked as 'none'. Skipping public container registry push parameters.")
            return

        self.authenticate_dockerhub()

        is_backend = "backend" in target_day["target_component"]
        dockerfile_path = BACKEND_DOCKERFILE if is_backend else FRONTEND_DOCKERFILE
        workspace_path = resolve_absolute_path("sources/backend") if is_backend else resolve_absolute_path("sources/frontend")
        
        if not os.path.exists(dockerfile_path):
            print(f"[ ⚠️ DOCKERHUB-AGENT SKIP ] Target container instruction blueprint absent at: {dockerfile_path}")
            return

        # Smart fallback priority lookup pattern matching central JSON parameters
        namespace = self.secrets.get("DOCKERHUB_NAMESPACE", self.secrets.get("DOCKERHUB_USERNAME"))
        
        # Coupled mapping dynamically using your custom prefixed repository parameter value (e.g. docker-membership-hub-backend)
        dockerhub_target_image = f"{namespace}/{dockerhub_repo_name}:{self.image_tag}"

        print(f"[DOCKERHUB-AGENT BUILD] Packaging multi-stage application image component: {dockerhub_target_image}")
        subprocess.run(["docker", "build", "-t", dockerhub_target_image, "-f", dockerfile_path, workspace_path], check=True)

        print(f"[DOCKERHUB-AGENT PUSH] Streaming production release tag across remote Docker Hub brokers pipelines...")
        subprocess.run(["docker", "push", dockerhub_target_image], check=True)
        print(f"[ ✅ DOCKERHUB-AGENT SUCCESS] Image package {dockerhub_target_image} successfully committed upstream!")
        
        # write history
        self.write_history(dockerhub_target_image)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    args = parser.parse_args()
    DockerHubAgent(args.phase, args.day).execute_dockerhub_pipeline()
