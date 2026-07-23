# .ai/.agents/agent-manager.py
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

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
STEPS_PLAN_DIR   = agent_helper.resolve_absolute_path(".ai/.plan/.steps")
SUB_AGENTS_DIR   = agent_helper.resolve_absolute_path(".ai/.agents/.sub_agents")

class EnterpriseMultiAgentManager:
    """
    Central Core Orchestrator Console. Governs daily environment feature branching isolation configurations,
    monitors sequential standalone sub-agent script executions, and handles conditional cloud dual registry releases.
    """
    def __init__(self, phase_id, day_num, is_release_triggered):
        self.agent_id = "Manager"
        self.phase_int = int(phase_id)
        self.phase_str = f"{self.phase_int:02d}" 
        self.day_num = int(day_num)
        self.target_branch = f"features/development-day-{self.day_num}"
        self.is_release_triggered = is_release_triggered
    
    # set-up daily branch to commit from `features/development`
    def setup_daily_git_branch(self):
        print(f"[ {self.agent_id} Agent | GIT ] Swapping active environment context into daily branch: {self.target_branch}")
        subprocess.run(["git", "checkout", "features/development"], capture_output=True)
        subprocess.run(["git", "pull", "origin", "features/development"], capture_output=True)
        subprocess.run(["git", "checkout", "-b", self.target_branch], capture_output=True)
    
    # reset daily branch when exception emergency
    def execute_emergency_purge(self):
        print("\n" + "="*80)
        print(f"[ 💀 {self.agent_id} Agent | CRITICAL SHUTDOWN ] MULTI-AGENT PIPELINE CONTEXT FAILURE OR RESOURCE TOKENS PURGED!")
        print(f"[ {self.agent_id} Agent | PURGING CACHE ] Wiping dirty, uncommitted changes for Day {self.day_num} to guarantee clean baselines.")
        print("="*80)
        subprocess.run(["git", "reset", "--hard", "HEAD"], capture_output=True)
        subprocess.run(["git", "checkout", "features/development"], capture_output=True)
        sys.exit(1)
    
    # commit & push changes to daily branch
    def push_daily_git_branch(self, target_day):
        # extract target components
        pushed_components = []
        for task in target_day.get("sub_tasks") if target_day.get("sub_tasks") and len(target_day.get("sub_tasks")) > 0 else []:
            for component in task.get("components") if task.get("components") and len(task.get("components")) > 0 else []:
                componentParts = component.split(";")
                for componentPart in componentParts if isinstance(componentParts, list) and len(componentParts) > 0 else []:
                    if os.path.exists(componentPart):
                        pushed_components.append(componentPart)
        if len(pushed_components) <= 0:
            print(f"[ {self.agent_id} Agent | WARN ] Not found any target component to commit/push!")
            sys.exit(0)
        
        print(f"[ {self.agent_id} Agent | GIT ] All pipeline validation thresholds passed. Committing structural assets...")
        # add components to push GIT
        for component in pushed_components:
            pushed_component = agent_helper.resolve_absolute_path(component)
            subprocess.run(["git", "add", pushed_component], capture_output=True)
        # commit GIT
        commit_msg = f"feat(day-{self.day_num}): complete enterprise sub-tasks for day {self.day_num} [AI Pipeline]"
        subprocess.run(["git", "commit", "-m", commit_msg], capture_output=True)
        # push GIT
        subprocess.run(["git", "push", "origin", self.target_branch], capture_output=True)
        print(f"[ ✅ {self.agent_id} Agent | SUCCESS ] Isolated feature branch {self.target_branch} pushed safely to remote origin!")
    
    # execute sub agents
    def execute(self):
        self.setup_daily_git_branch()
        
        # read daily steps
        steps_path = f"{STEPS_PLAN_DIR}/phase-{self.phase_str}.agent.steps.json"
        if not os.path.exists(steps_path):
            self.execute_emergency_purge()
        
        # extract daily tasks
        with open(steps_path, "r", encoding="utf-8") as f:
            steps_data = json.load(f)
        target_day = next((d for d in steps_data["days"] if d["day"] == self.day_num), None)
        
        # Act 1-4: Core Development Multi-Agent Execution Loop
        print(f"\n[ {self.agent_id} Agent | STEP 1/7 ] Activating Standalone Coder Agent...")
        if subprocess.run(["python", f"{SUB_AGENTS_DIR}/agent_coder.py", "--phase", self.phase_str, "--day", str(self.day_num)]).returncode != 0:
            self.execute_emergency_purge()

        print(f"\n[ {self.agent_id} Agent | STEP 2/7 ] Activating Standalone Tester Agent...")
        if subprocess.run(["python", f"{SUB_AGENTS_DIR}/agent_tester.py", "--phase", self.phase_str, "--day", str(self.day_num)]).returncode != 0:
            self.execute_emergency_purge()

        print(f"\n[ {self.agent_id} Agent | STEP 3/7 ] Activating Standalone Bug Fixer Agent & Real Compiler Gate...")
        if subprocess.run(["python", f"{SUB_AGENTS_DIR}/agent_reviewer.py", "--phase", self.phase_str, "--day", str(self.day_num)]).returncode != 0:
            self.execute_emergency_purge()

        print(f"\n[ {self.agent_id} Agent | STEP 4/7 ] Activating Standalone Documentation Agent...")
        if subprocess.run(["python", f"{SUB_AGENTS_DIR}/agent_doc.py", "--phase", self.phase_str, "--day", str(self.day_num)]).returncode != 0:
            self.execute_emergency_purge()

        # CONDITIONAL DISTRIBUTED CLOUD MULTI-REGISTRY RELEASE ENGINE GATEWAY
        if self.is_release_triggered:
            # Step 5: Execute Docker Hub Public/Internal Registry Push Workflow
            print(f"\n[ {self.agent_id} Agent | STEP 5/7 ] [MANUAL RELEASE] Activating Standalone Docker Hub Registry Infrastructure Agent...")
            if subprocess.run(["python", f"{SUB_AGENTS_DIR}/agent_docker.py", "--phase", self.phase_str, "--day", str(self.day_num)]).returncode != 0:
                print(f"[ 💀 {self.agent_id} Agent | DEPLOY-ERROR ] Docker Hub public registry image publication failed. Halting deployment pipeline.")
                self.execute_emergency_purge()
            
            # Step 6: Execute Google Artifact Registry Images Push Workflow
            print(f"\n[ {self.agent_id} Agent | STEP 6/7 ] [MANUAL RELEASE] Activating Standalone GCP Registry Infrastructure Agent...")
            if subprocess.run(["python", f"{SUB_AGENTS_DIR}/agent_gcp.py", "--phase", self.phase_str, "--day", str(self.day_num)]).returncode != 0:
                print(f"[ 💀 {self.agent_id} Agent | DEPLOY-ERROR ] GCP container image publication failed. Halting deployment pipeline.")
                self.execute_emergency_purge()

            # Step 7: Execute GKE Pod Cluster Workloads Rollout Update Workflow
            print(f"\n[ {self.agent_id} Agent | STEP 7/7 ] [MANUAL RELEASE] Activating Standalone GKE Container Orchestration Agent...")
            if subprocess.run(["python", f"{SUB_AGENTS_DIR}/agent_gke.py", "--phase", self.phase_str, "--day", str(self.day_num)]).returncode != 0:
                print(f"[ 💀 {self.agent_id} Agent | DEPLOY-ERROR ] GKE cluster application workload rollout update failed. Halting pipeline.")
                self.execute_emergency_purge()
        else:
            print(f"\n[ {self.agent_id} Agent | STEPS 5,6,7/7 ] [RELEASE SKIPPED] Manual release flag absent. Assets retained locally.")

        # Complete and push daily baseline deliverables up to repository server host
        self.push_daily_git_branch(target_day)
        print(f"\n[ ✅ {self.agent_id} Agent | ORCHESTRATOR COMPLETE ] Day {self.day_num} operations successfully completed!")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True, help="Target Phase formatted string or number (e.g., 1 or 01)")
    parser.add_argument("--day", required=True, help="Target Day sequence position (e.g., 1)")
    parser.add_argument("--release", action="store_true", help="Trigger manual production rollout deployment to all cloud registries")
    args = parser.parse_args()
    
    print(f"🏢 Enterprise SaaS AI Manager Agent: Phase { args.phase } Day { args.day } Release { args.release }...")
    EnterpriseMultiAgentManager(
        phase_id=args.phase,
        day_num=args.day,
        is_release_triggered=args.release
    ).execute()
