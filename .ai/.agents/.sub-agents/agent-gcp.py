# .ai/.agents/.sub-agents/agent-gcp.py
import os
import sys
import json
import argparse
import subprocess

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
STEPS_PLAN_DIR = "./.ai/.agents/.steps"

class GCPAgent:
    """
    Cloud Infrastructure Agent. Parses centralized GCP_SECRETS JSON from GitHub,
    authenticates against GCP, builds GraalVM container images, and pushes to GAR.
    """
    def __init__(self, phase_str, day_num):
        self.phase_str = phase_str
        self.day_num = int(day_num)
        self.image_tag = f"day-{self.day_num}"
        # Parse centralized secrets dictionary dynamically from environment variables
        self.secrets = self.load_gcp_secrets()

    def load_gcp_secrets(self):
        """Loads and parses the centralized GCP_SECRETS JSON string."""
        raw_secrets = os.environ.get("GCP_SECRETS")
        if not raw_secrets:
            print("[GCP-AGENT CRITICAL] The environment variable 'GCP_SECRETS' is completely absent.")
            sys.exit(1)
        try:
            return json.loads(raw_secrets)
        except Exception as e:
            print(f"[GCP-AGENT CRITICAL] Failed to parse GCP_SECRETS JSON string: {str(e)}")
            sys.exit(1)

    def authenticate_gcp(self):
        """Authenticates the workspace session natively using service account credentials from JSON pool."""
        print("[GCP-AGENT] Authenticating context with Google Cloud Platform SDK via centralized JSON...")
        sa_key = self.secrets.get("GCP_SA_KEY")
        project_id = self.secrets.get("GCP_PROJECT_ID")
        region = self.secrets.get("GCP_REGION")

        if sa_key and project_id and region:
            # Securely dump the Service Account JSON string to a temporary local credentials file
            with open("gcp-key.json", "w", encoding="utf-8") as f:
                f.write(sa_key)
            subprocess.run(["gcloud", "auth", "activate-service-account", f"--key-file=gcp-key.json"], check=True)
            subprocess.run(["gcloud", "config", "set", "project", project_id], check=True)
            subprocess.run(["gcloud", "auth", "configure-docker", f"{region}-docker.pkg.dev"], check=True)
            # Instantly evict the transient structural credentials key file from the disk layout
            os.remove("gcp-key.json")
        else:
            print("[GCP-AGENT WARNING] Missing parameters inside GCP_SECRETS. Relying on active local shell auth context.")

    def execute_gcp_pipeline(self):
        """Orchestrates image compilation and container registry pushes to GAR."""
        steps_path = f"{STEPS_PLAN_DIR}/phase-{self.phase_str}.agent.steps.json"
        with open(steps_path, "r", encoding="utf-8") as f:
            steps_data = json.load(f)
        target_day = next((d for d in steps_data["days"] if d["day"] == self.day_num), None)

        self.authenticate_gcp()

        if "infrastructure/k8s" in target_day["target_component"]:
            print("[GCP-AGENT SKIP] Infrastructure Day detected. No application Docker compilation required.")
            return

        is_backend = "backend" in target_day["target_component"]
        app_domain = "backend" if is_backend else "frontend"
        dockerfile_path = f"./sources/{app_domain}/src/main/docker/Dockerfile.native" if is_backend else f"./sources/{app_domain}/Dockerfile"
        
        if not os.path.exists(dockerfile_path):
            print(f"[GCP-AGENT SKIP] Target container instruction blueprint absent at: {dockerfile_path}")
            return

        project_id = self.secrets.get("GCP_PROJECT_ID")
        region = self.secrets.get("GCP_REGION")
        gar_repository = self.secrets.get("GAR_REPOSITORY")

        registry_image = f"{region}-docker.pkg.dev/{project_id}/{gar_repository}/{app_domain}-service:{self.image_tag}"

        print(f"[GCP-AGENT BUILD] Compiling multi-stage container artifact: {registry_image}")
        subprocess.run(["docker", "build", "-t", registry_image, "-f", dockerfile_path, f"./sources/{app_domain}"], check=True)

        print(f"[GCP-AGENT PUSH] Uploading image binary up to Google Artifact Registry...")
        subprocess.run(["docker", "push", registry_image], check=True)
        print(f"[GCP-AGENT SUCCESS] Image version {self.image_tag} published safely to GAR!")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    args = parser.parse_args()
    GCPAgent(args.phase, args.day).execute_gcp_pipeline()
