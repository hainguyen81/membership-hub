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
# request agent_helper from `.libs/project_agents_package_loader.py`
from _ai._agents import agent_helper

# super agent
from _ai._agents._sub_agents.agent_super import AbstractAgent

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
BACKEND_DOCKERFILE          = agent_helper.resolve_absolute_path("sources/backend/src/main/docker/Dockerfile.native")
FRONTEND_DOCKERFILE         = agent_helper.resolve_absolute_path("sources/frontend/Dockerfile")

class GcpAgent(AbstractAgent):
    def __init__(self, phase_str, day_num):
        super().__init__(
            agent_id="Docker",
            phase_str=phase_str,
            day_num=day_num
        )
    
    def initialize(self):
        self.image_tag = f"day-{self.day_num}"
        self.gcp_repo = self.gcp_cloud_repo()
        self.gcp_project = self.gcp_cloud_project()
        self.gcp_region = self.gcp_cloud_region()
        self.gcp_image = self.gcp_cloud_image()
        self.gcp_sa_key = self.gcp_cloud_sa_key()
    
    def agent_secrets_key(self) -> str:
        return "GCP_SECRETS"
    
    def agent_log_file(self) -> str:
        return agent_helper.resolve_absolute_path(f".ai/.history/agent-gcp-day-{self.day_num}.md")
    
    def system_prompt_template(self) -> str:
        return None
    
    def user_prompt_template(self) -> str:
        return None

    def authenticate_gcp(self):
        print(f"[ {self.agent_id} Agent ] Authenticating context with Google Cloud Platform SDK...")
        if self.gcp_sa_key and self.gcp_project and self.gcp_region:
            with open("gcp-key.json", "w", encoding="utf-8") as f:
                f.write(self.gcp_sa_key)
            subprocess.run(["gcloud", "auth", "activate-service-account", f"--key-file=gcp-key.json"], check=True)
            subprocess.run(["gcloud", "config", "set", "project", self.gcp_project], check=True)
            subprocess.run(["gcloud", "auth", "configure-docker", f"{self.gcp_region}-docker.pkg.dev"], check=True)
            os.remove("gcp-key.json")
        else:
            print(f"[ ⚠️ {self.agent_id} Agent | WARNING ] Missing parameters inside GCP_SECRETS. Relying on active local shell auth context.")
    
    def gcp_cloud_repo(self) -> str:
        return os.environ.get("GCP_REPO")
    
    def gcp_cloud_project(self) -> str:
        return self.agent_secrets("GCP_PROJECT_ID")
    
    def gcp_cloud_region(self) -> str:
        return self.agent_secrets("GCP_REGION")
    
    def gcp_cloud_sa_key(self) -> str:
        return self.agent_secrets("GCP_SA_KEY")
    
    def gcp_cloud_image(self) -> str:
        return f"{self.gcp_region}-docker.pkg.dev/{self.gcp_project}/{self.gcp_repo}:{self.image_tag}"
    
    def pre_execute(self):
        # validate repository
        if not self.gcp_repo or len(self.gcp_repo.strip()) <= 0:
            print(f"[ ⚠️ {self.agent_id} Agent | SKIP ] Not found 'GCP_REPO' enviroment to publish image for deploying.")
            sys.exit(0)
        
        # log-in repository
        self.authenticate_gcp()
    
    def execute_task(self, project_name, global_context, day_context, source_component, target_component, sub_tasks):
        is_backend = "backend" in target_component
        dockerfile_path = BACKEND_DOCKERFILE if is_backend else FRONTEND_DOCKERFILE
        workspace_path = resolve_absolute_path("sources/backend") if is_backend else resolve_absolute_path("sources/frontend")
        
        if not os.path.exists(dockerfile_path):
            print(f"[ ⚠️ {self.agent_id} Agent | SKIP ] Target container instruction blueprint absent at: {dockerfile_path}")
            return (True, None, None, f"[ ⚠️ {self.agent_id} Agent | SKIP ] Target container instruction blueprint absent at: {dockerfile_path}")
        
        # build image
        print(f"[ {self.agent_id} Agent | BUILD ] Compiling multi-stage container artifact: {self.gcp_image}")
        subprocess.run(["docker", "build", "-t", self.gcp_image, "-f", dockerfile_path, workspace_path], check=True)

        print(f"[ {self.agent_id} Agent | PUSH ] Uploading image binary up to Google Artifact Registry...")
        subprocess.run(["docker", "push", self.gcp_image], check=True)
        print(f"[ ✅ {self.agent_id} Agent | SUCCESS ] Image version {self.image_tag} published safely to GAR!")
        
        # result
        return (True, None, None, f"Image version {self.image_tag} published safely to GAR!")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    args = parser.parse_args()
    print(f"☁️ Connecting to Google Cloud SDK components to route compiled GAR assets for Phase { args.phase } Day { args.day }...")
    GcpAgent(
        phase_str=args.phase,
        day_num=args.day
    ).execute()
