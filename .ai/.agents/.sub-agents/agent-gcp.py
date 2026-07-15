# .ai/.agents/.sub-agents/agent-gcp.py
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
FRONTEND_DOCKERFILE         = resolve_absolute_path("sources/frontend/Dockerfile")
agent_working_history_file  = resolve_absolute_path(".ai/.history/agent-gcp.md")

class GCPAgent:
    """
    Cloud Infrastructure Agent. Parses centralized GCP_SECRETS JSON from GitHub,
    authenticates against GCP, builds GraalVM container images, and pushes to GAR.
    """
    def __init__(self, phase_str, day_num):
        self.phase_str = phase_str
        self.day_num = int(day_num)
        self.image_tag = f"day-{self.day_num}"
        self.secrets = self.load_gcp_secrets()

    def load_gcp_secrets(self):
        raw_secrets = os.environ.get("GCP_SECRETS")
        if not raw_secrets:
            print("[ 💀 GCP-AGENT CRITICAL ] The environment variable 'GCP_SECRETS' is completely absent.")
            sys.exit(1)
        try:
            return json.loads(raw_secrets)
        except Exception as e:
            print(f"[ 💀 GCP-AGENT CRITICAL ] Failed to parse GCP_SECRETS JSON string: {str(e)}")
            sys.exit(1)

    def authenticate_gcp(self):
        print("[GCP-AGENT] Authenticating context with Google Cloud Platform SDK...")
        sa_key = self.secrets.get("GCP_SA_KEY")
        project_id = self.secrets.get("GCP_PROJECT_ID")
        region = self.secrets.get("GCP_REGION")

        if sa_key and project_id and region:
            with open("gcp-key.json", "w", encoding="utf-8") as f:
                f.write(sa_key)
            subprocess.run(["gcloud", "auth", "activate-service-account", f"--key-file=gcp-key.json"], check=True)
            subprocess.run(["gcloud", "config", "set", "project", project_id], check=True)
            subprocess.run(["gcloud", "auth", "configure-docker", f"{region}-docker.pkg.dev"], check=True)
            os.remove("gcp-key.json")
        else:
            print("[ ⚠️ GCP-AGENT WARNING ] Missing parameters inside GCP_SECRETS. Relying on active local shell auth context.")
    
    def write_history(self, registry_image, tag):
        # write working history
        history_content = (
            f"# Day {self.day_num}: Image already published safely to GAR\n"
            f"* **Image Registry**: {registry_image}\n"
            f"* **Tag**: {tag}\n"
        )
        with open(agent_working_history_file, "a", encoding="utf-8") as file:
            file.write(history_content)

    def execute_gcp_pipeline(self):
        steps_path = f"{STEPS_PLAN_DIR}/phase-{self.phase_str}.agent.steps.json"
        with open(steps_path, "r", encoding="utf-8") as f:
            steps_data = json.load(f)
        target_day = next((d for d in steps_data["days"] if d["day"] == self.day_num), None)

        # FIXED RULE: Ingest target identifier name directly from the steps configurations
        gar_repo_name = target_day.get("gcp_component", "none")
        
        # UNIFIED CHECK GATE: If explicitly marked as 'none', skip the compilation cleanly
        if gar_repo_name == "none":
            print("[ ⚠️ GCP-AGENT SKIP ] Step is explicitly marked as 'none'. Skipping container build pipeline layout configurations smoothly.")
            return

        self.authenticate_gcp()

        is_backend = "backend" in target_day["target_component"]
        dockerfile_path = BACKEND_DOCKERFILE if is_backend else FRONTEND_DOCKERFILE
        workspace_path = resolve_absolute_path("sources/backend") if is_backend else resolve_absolute_path("sources/frontend")
        
        if not os.path.exists(dockerfile_path):
            print(f"[ ⚠️ GCP-AGENT SKIP ] Target container instruction blueprint absent at: {dockerfile_path}")
            return

        project_id = self.secrets.get("GCP_PROJECT_ID")
        region = self.secrets.get("GCP_REGION")

        # Dynamic clean string repository mapping using your exact prefix parameter name (e.g. gcp-membership-hub-backend)
        registry_image = f"{region}-docker.pkg.dev/{project_id}/{gar_repo_name}:{self.image_tag}"

        print(f"[GCP-AGENT BUILD] Compiling multi-stage container artifact: {registry_image}")
        subprocess.run(["docker", "build", "-t", registry_image, "-f", dockerfile_path, workspace_path], check=True)

        print(f"[GCP-AGENT PUSH] Uploading image binary up to Google Artifact Registry...")
        subprocess.run(["docker", "push", registry_image], check=True)
        print(f"[ ✅ GCP-AGENT SUCCESS ] Image version {self.image_tag} published safely to GAR!")
        
        # write history
        self.write_history(registry_image, self.image_tag)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    args = parser.parse_args()
    GCPAgent(args.phase, args.day).execute_gcp_pipeline()
