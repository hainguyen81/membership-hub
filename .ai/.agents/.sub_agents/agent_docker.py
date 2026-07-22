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
# request agent_helper from `site-packages/load_modules.pth`
agent_helper = sys.modules["agent_helper"]

# super agent
from agent_super import AbstractAgent

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
BACKEND_DOCKERFILE          = agent_helper.resolve_absolute_path("sources/backend/src/main/docker/Dockerfile.native")
FRONTEND_DOCKERFILE         = agent_helper.resolve_absolute_path("sources/frontend/Dockerfile")

class DockerHubAgent(AbstractAgent):
    def __init__(self, project_name, phase_str, day_num):
        super().__init__(
            project_name=project_name,
            agent_id="Docker",
            phase_str=phase_str,
            day_num=day_num
        )
    
    def initialize(self):
        self.image_tag = f"day-{self.day_num}"
        self.docker_repo = self.docker_hub_repo()
        self.docker_namespace = self.docker_hub_namespace()
        self.docker_image = self.docker_hub_image()
    
    def agent_secrets_key(self) -> str:
        return "DOCKERHUB_SECRETS"

    def authenticate_dockerhub(self):
        print(f"[ {self.agent_id} Agent ] Attaching secure registry authorization handshakes...")
        username = self.agent_secrets("DOCKERHUB_USERNAME")
        password = self.agent_secrets("DOCKERHUB_PASSWORD")

        if username and password:
            login_process = subprocess.Popen(
                ["docker", "login", "-u", username, "--password-stdin"],
                stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True
            )
            stdout, stderr = login_process.communicate(input=password)
            if login_process.returncode != 0:
                print(f"[ 💀 {self.agent_id} Agent | ERROR] Authentication verification failed natively: {stderr}")
                sys.exit(1)
            print(f"[ ✅ {self.agent_id} Agent | SUCCESS ] Docker Hub authentication session activated successfully.")
        else:
            print(f"[ ⚠️ {self.agent_id} Agent | WARNING ] Missing data keys parameters inside DOCKERHUB_SECRETS mapping registry.")
    
    def agent_log_file(self) -> str:
        return agent_helper.resolve_absolute_path(f".ai/.history/agent-docker-day-{self.day_num}.md")
    
    def system_prompt_template(self) -> str:
        return None
    
    def user_prompt_template(self) -> str:
        return None
    
    def docker_hub_repo(self) -> str:
        return self.agent_secrets("DOCKERHUB_REPO")
    
    def docker_hub_namespace(self) -> str:
        return self.agent_secrets("DOCKERHUB_NAMESPACE", self.agent_secrets("DOCKERHUB_USERNAME"))
    
    def docker_hub_image(self) -> str:
        return f"{self.docker_namespace}/{self.docker_repo}:{self.image_tag}"
    
    def pre_execute(self):
        # validate repository
        if not self.docker_repo or len(self.docker_repo.strip()) <= 0:
            print(f"[ ⚠️ {self.agent_id} Agent | SKIP ] Not found 'DOCKERHUB_REPO' enviroment to publish docker images.")
            sys.exit(0)
        
        # log-in repository
        self.authenticate_dockerhub()

    def execute_task(self, global_context, day_context, source_component, target_component, sub_tasks):
        is_backend = "backend" in target_component
        dockerfile_path = BACKEND_DOCKERFILE if is_backend else FRONTEND_DOCKERFILE
        workspace_path = agent_helper.resolve_absolute_path("sources/backend") if is_backend else agent_helper.resolve_absolute_path("sources/frontend")
        
        # check whether exists docker file
        if not os.path.exists(dockerfile_path):
            print(f"[ ⚠️ {self.agent_id} Agent | SKIP ] Target container instruction blueprint absent at: {dockerfile_path}")
            return (True, None, None, f"[ ⚠️ {self.agent_id} Agent | SKIP ] Target container instruction blueprint absent at: {dockerfile_path}")
        
        # build image
        print(f"[ {self.agent_id} Agent | BUILD ] Packaging multi-stage application image component: {self.docker_image}")
        subprocess.run(["docker", "build", "-t", self.docker_image, "-f", dockerfile_path, workspace_path], check=True)
        
        # push image to DockerHub
        print(f"[ {self.agent_id} Agent | PUSH ] Streaming production release tag across remote Docker Hub brokers pipelines...")
        subprocess.run(["docker", "push", self.docker_image], check=True)
        print(f"[ ✅ {self.agent_id} Agent | SUCCESS] Image package {self.docker_image} successfully committed upstream!")
        
        # result
        return (True, None, None, f"Image package {self.docker_image} successfully committed upstream!")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True)
    parser.add_argument("--day", required=True)
    parser.add_argument("--project-name", required=True)
    args = parser.parse_args()
    DockerHubAgent(
        project_name=args.project_name,
        phase_str=args.phase,
        day_num=args.day
    ).execute()
