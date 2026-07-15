# .ai/.agents/.sub-agents/agent-gke.py
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
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
STEPS_PLAN_DIR              = resolve_absolute_path(".ai/.agents/.steps")
agent_working_history_file  = resolve_absolute_path(".ai/.history/agent-gke.md")

class GKEAgent:
    """
    Kubernetes Release Engineer Agent. Connects securely to GKE cluster API, 
    applies orchestration files, and triggers safe zero-downtime rolling updates.
    """
    def __init__(self, phase_str, day_num):
        self.phase_str = phase_str
        self.day_num = int(day_num)
        self.image_tag = f"day-{self.day_num}"
        self.secrets = self.load_gke_secrets()

    def load_gke_secrets(self):
        raw_secrets = os.environ.get("GKE_SECRETS")
        if not raw_secrets:
            print("[ 💀 GKE-AGENT CRITICAL ] The environment variable 'GKE_SECRETS' is completely absent.")
            sys.exit(1)
        try:
            return json.loads(raw_secrets)
        except Exception as e:
            print(f"[ 💀 GKE-AGENT CRITICAL ] Failed to parse GKE_SECRETS JSON string: {str(e)}")
            sys.exit(1)

    def configure_gke_credentials(self):
        cluster_name = self.secrets.get("GKE_CLUSTER_NAME")
        region = self.secrets.get("GCP_REGION")
        project_id = self.secrets.get("GCP_PROJECT_ID")

        if cluster_name and region and project_id:
            print(f"[GKE-AGENT] Fetching security credentials context for cluster registry: {cluster_name}")
            subprocess.run([
                "gcloud", "container", "clusters", "get-credentials", cluster_name,
                f"--region={region}", f"--project={project_id}"
            ], check=True)
        else:
            print("[ ⚠️ GKE-AGENT WARNING ] Missing data keys inside GKE_SECRETS array map framework parameters.")
    
    def write_history(self, registry_image, deployment_name, tag, region, project, desc):
        # write working history
        history_content = (
            f"# Day {self.day_num}: Successfully deployed container to GKE pods clusters\n" if not desc else f"## {desc}"
            f"* **Image Registry**: {registry_image}\n"
            f"* **Deployment**: {deployment_name}\n"
            f"* **Tag**: {tag}\n"
            f"* **Region**: {region}\n"
            f"* **Project**: {project}\n"
        )
        with open(agent_working_history_file, "a", encoding="utf-8") as file:
            file.write(history_content)

    def execute_gke_deployment(self):
        steps_path = f"{STEPS_PLAN_DIR}/phase-{self.phase_str}.agent.steps.json"
        with open(steps_path, "r", encoding="utf-8") as f:
            steps_data = json.load(f)
        target_day = next((d for d in steps_data["days"] if d["day"] == self.day_num), None)

        # FIXED RULE: Ingest target deployment string identifier directly from the steps mappings
        k8s_deployment_name = target_day.get("gke_component", "none")
        
        # UNIFIED CHECK GATE: If explicitly marked as 'none', skip the GKE deployment parameters cleanly
        if k8s_deployment_name == "none":
            print("[ ⚠️ GKE-AGENT SKIP ] Step is explicitly marked as 'none'. Skipping GKE cluster rollout update loops framework entirely.")
            return

        self.configure_gke_credentials()

        # Standard Microservice Application Rollout Logic using your custom prefixed parameters name (e.g. gke-membership-hub-backend)
        is_backend = "backend" in target_day["target_component"]
        app_domain = "backend" if is_backend else "frontend"
        
        project_id = self.secrets.get("GCP_PROJECT_ID")
        region = self.secrets.get("GCP_REGION")
        
        # Read the clean matching GAR repository mapping to compile image context accurately
        gar_repo_name = target_day.get("gcp_component")
        registry_image = f"{region}-docker.pkg.dev/{project_id}/{gar_repo_name}:{self.image_tag}"

        # Check if the target day represents a dedicated infrastructure day targeting raw K8s deployment manifests (like Day 23)
        if "infrastructure/k8s" in target_day["target_component"]:
            print(f"[GKE-AGENT] Applying raw enterprise infrastructure update manifests: {target_day['target_component']}")
            target_component = resolve_absolute_path(target_day["target_component"])
            subprocess.run(["kubectl", "apply", "-f", target_component], check=True)
            print("[ ✅ GKE-AGENT SUCCESS ] Cloud infrastructure manifest rules applied securely on GKE compute pools!")
        
            # write history
            self.write_history(registry_image, f"deployment/{k8s_deployment_name}", self.image_tag, region, project_id, "Cloud infrastructure manifest rules applied securely on GKE compute pools")
            return

        print(f"[GKE-AGENT ROLLOUT] Activating safe, zero-downtime rolling update across container workloads for deployment: {k8s_deployment_name}")
        subprocess.run([
            "kubectl", "set", "image", f"deployment/{k8s_deployment_name}",
            f"{app_domain}-container={registry_image}"
        ], check=True)
        
        subprocess.run(["kubectl", "rollout", "status", f"deployment/{k8s_deployment_name}"], check=True)
        print(f"[ ✅ GKE-AGENT SUCCESS ] Successfully deployed container version {self.image_tag} to GKE pods clusters!")
        
        # write history
        self.write_history(registry_image, f"deployment/{k8s_deployment_name}", self.image_tag, region, project_id)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    args = parser.parse_args()
    GKEAgent(args.phase, args.day).execute_gke_deployment()
