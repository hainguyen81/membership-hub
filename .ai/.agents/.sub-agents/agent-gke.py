# .ai/.agents/.sub-agents/agent-gke.py
import os
import sys
import json
import argparse
import subprocess

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
STEPS_PLAN_DIR = "./.ai/.agents/.steps"

class GKEAgent:
    """
    Kubernetes Release Engineer Agent. Parses centralized GKE_SECRETS JSON from GitHub,
    connects securely to GKE cluster API, and triggers rolling updates.
    """
    def __init__(self, phase_str, day_num):
        self.phase_str = phase_str
        self.day_num = int(day_num)
        self.image_tag = f"day-{self.day_num}"
        # Parse centralized cluster secrets dictionary dynamically
        self.secrets = self.load_gke_secrets()

    def load_gke_secrets(self):
        """Loads and parses the centralized GKE_SECRETS JSON string."""
        raw_secrets = os.environ.get("GKE_SECRETS")
        if not raw_secrets:
            print("[GKE-AGENT CRITICAL] The environment variable 'GKE_SECRETS' is completely absent.")
            sys.exit(1)
        try:
            return json.loads(raw_secrets)
        except Exception as e:
            print(f"[GKE-AGENT CRITICAL] Failed to parse GKE_SECRETS JSON string: {str(e)}")
            sys.exit(1)

    def configure_gke_credentials(self):
        """Fetches runtime authentication structures mapping down to active GKE Kubernetes endpoints."""
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
            print("[GKE-AGENT WARNING] Missing data keys inside GKE_SECRETS array map framework parameters.")

    def execute_gke_deployment(self):
        """Connects to the cluster and applies raw manifests or container image rolling updates."""
        steps_path = f"{STEPS_PLAN_DIR}/phase-{self.phase_str}.agent.steps.json"
        with open(steps_path, "r", encoding="utf-8") as f:
            steps_data = json.load(f)
        target_day = next((d for d in steps_data["days"] if d["day"] == self.day_num), None)

        # Ingest cluster endpoint tokens
        self.configure_gke_credentials()

        # CASE 1: Infrastructure Day with raw Kubernetes YAML manifests
        if "infrastructure/k8s" in target_day["target_component"]:
            print(f"[GKE-AGENT] Applying raw enterprise infrastructure update manifests: {target_day['target_component']}")
            subprocess.run(["kubectl", "apply", "-f", target_day["target_component"]], check=True)
            print("[GKE-AGENT SUCCESS] Cloud infrastructure manifest rules applied safely on GKE compute pools!")
            return

        # CASE 2: Standard Application Service Update Rollout
        is_backend = "backend" in target_day["target_component"]
        app_domain = "backend" if is_backend else "frontend"
        
        project_id = self.secrets.get("GCP_PROJECT_ID")
        region = self.secrets.get("GCP_REGION")
        gar_repository = self.secrets.get("GAR_REPOSITORY")
        
        registry_image = f"{region}-docker.pkg.dev/{project_id}/{gar_repository}/{app_domain}-service:{self.image_tag}"

        print(f"[GKE-AGENT ROLLOUT] Activating safe, zero-downtime rolling update across container workloads...")
        k8s_deployment_name = f"{app_domain}-service-deployment"
        subprocess.run([
            "kubectl", "set", "image", f"deployment/{k8s_deployment_name}",
            f"{app_domain}-container={registry_image}"
        ], check=True)
        
        subprocess.run(["kubectl", "rollout", "status", f"deployment/{k8s_deployment_name}"], check=True)
        print(f"[GKE-AGENT SUCCESS] Successfully deployed container version {self.image_tag} to GKE pods clusters!")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    args = parser.parse_args()
    GKEAgent(args.phase, args.day).execute_gke_deployment()
