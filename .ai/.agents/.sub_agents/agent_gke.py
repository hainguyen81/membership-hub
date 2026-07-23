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
# request agent_helper from `.libs/project_agents_package_loader.py`
from _ai._agents import agent_helper

# super agent
from _ai._agents._sub_agents.agent_gcp import GcpAgent

class GkeAgent(GcpAgent):
    def __init__(self, phase_str, day_num):
        super().__init__(
            agent_id="Docker",
            phase_str=phase_str,
            day_num=day_num
        )
    
    def initialize(self):
        super().initialize()
        self.gke_deployment_name = self.gke_cloud_deployment_name()
        self.gke_cluster_name = self.gke_cloud_cluster()
    
    def agent_secrets_key(self) -> str:
        return "GKE_SECRETS"
    
    def agent_log_file(self) -> str:
        return agent_helper.resolve_absolute_path(f".ai/.history/agent-gke-day-{self.day_num}.md")
    
    def authenticate_gcp(self):
        self.configure_gke_credentials()
    
    def configure_gke_credentials(self):
        if self.gke_cluster_name and self.gcp_region and self.gcp_project:
            print(f"[ {self.agent_id} Agent ] Fetching security credentials context for cluster registry: {self.gke_cluster_name}")
            subprocess.run([
                "gcloud", "container", "clusters", "get-credentials", self.gke_cluster_name,
                f"--region={self.gcp_region}", f"--project={self.gcp_project}"
            ], check=True)
        else:
            print(f"[ ⚠️ {self.agent_id} Agent | WARNING ] Missing data keys inside GKE_SECRETS array map framework parameters.")
    
    def gke_cloud_deployment_name(self) -> str:
        return self.agent_secrets("GKE_DEPLOYMENT_NAME")
    
    def gke_cloud_cluster(self) -> str:
        return self.agent_secrets("GKE_CLUSTER_NAME")
    
    def pre_execute(self):
        # validate repository
        if not self.gke_deployment_name or len(self.gke_deployment_name.strip()) <= 0:
            print(f"[ ⚠️ {self.agent_id} Agent | SKIP ] Not found 'GKE_DEPLOYMENT_NAME' enviroment. Step is explicitly marked as 'none'. Skipping GKE cluster rollout update loops framework entirely.")
            sys.exit(0)
        
        # as super
        super().pre_execute()
    
    def execute_task(self, project_name, global_context, day_context, source_component, target_component, sub_tasks):
        # Standard Microservice Application Rollout Logic using your custom prefixed parameters name (e.g. gke-membership-hub-backend)
        is_backend = "backend" in target_component
        app_domain = f"{self.project_name}-backend" if is_backend else "{self.project_name}-frontend"
        
        # Check if the target day represents a dedicated infrastructure day targeting raw K8s deployment manifests (like Day 23)
        if "infrastructure/k8s" in target_component:
            print(f"[ {self.agent_id} Agent ] Applying raw enterprise infrastructure update manifests: {target_component}")
            target_component = agent_helper.resolve_absolute_path(target_component)
            subprocess.run(["kubectl", "apply", "-f", target_component], check=True)
            print(f"[ ✅ {self.agent_id} Agent | SUCCESS ] Cloud infrastructure manifest rules applied securely on GKE compute pools!")
            return (True, None, None, "Cloud infrastructure manifest rules applied securely on GKE compute pools!")
        
        print(f"[ {self.agent_id} Agent | ROLLOUT ] Activating safe, zero-downtime rolling update across container workloads for deployment: {self.gke_deployment_name}")
        subprocess.run([
            "kubectl", "set", "image", f"deployment/{self.gke_deployment_name}",
            f"{app_domain}-container={self.gcp_image}"
        ], check=True)
        
        subprocess.run(["kubectl", "rollout", "status", f"deployment/{self.gke_deployment_name}"], check=True)
        print(f"[ ✅ {self.agent_id} Agent | SUCCESS ] Successfully deployed container version {self.image_tag} to GKE pods clusters!")
        
        # result
        return (True, None, None, f"Successfully deployed container version {self.image_tag} to GKE pods clusters!")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    args = parser.parse_args()
    print(f"☸️ Initiating secure handshakes towards remote GKE cluster pools for Phase { args.phase } Day { args.day }...")
    GkeAgent(
        phase_str=args.phase,
        day_num=args.day
    ).execute()
