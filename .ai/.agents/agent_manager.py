# .ai/.agents/agent-manager.py
import os
import sys
import json
import argparse
import subprocess

# Now Python can seamlessly see and import the centralized helper utility cleanly!
from agent_helper import resolve_absolute_path

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
STEPS_PLAN_DIR   = resolve_absolute_path(".ai/.agents/.steps")
SUB_AGENTS_DIR   = resolve_absolute_path(".ai/.agents/.sub-agents")

class EnterpriseMultiAgentManager:
    """
    Central Core Orchestrator Console. Governs daily environment feature branching isolation configurations,
    monitors sequential standalone sub-agent script executions, and handles conditional cloud dual registry releases.
    """
    def __init__(self, phase_id, day_num, is_release_triggered):
        self.phase_int = int(phase_id)
        self.phase_str = f"{self.phase_int:02d}" 
        self.day_num = int(day_num)
        self.target_branch = f"features/development-day-{self.day_num}"
        self.is_release_triggered = is_release_triggered

    def setup_daily_git_branch(self):
        print(f"[MANAGER-GIT] Swapping active environment context into daily branch: {self.target_branch}")
        subprocess.run(["git", "checkout", "features/development"], capture_output=True)
        subprocess.run(["git", "pull", "origin", "features/development"], capture_output=True)
        subprocess.run(["git", "checkout", "-b", self.target_branch], capture_output=True)

    def execute_emergency_purge(self):
        print("\n" + "="*80)
        print("[ 💀 CRITICAL SHUTDOWN ] MULTI-AGENT PIPELINE CONTEXT FAILURE OR RESOURCE TOKENS PURGED!")
        print(f"[PURGING CACHE] Wiping dirty, uncommitted changes for Day {self.day_num} to guarantee clean baselines.")
        print("="*80)
        subprocess.run(["git", "reset", "--hard", "HEAD"], capture_output=True)
        subprocess.run(["git", "checkout", "features/development"], capture_output=True)
        sys.exit(1)

    def push_daily_git_branch(self, target_day):
        print(f"[MANAGER-GIT] All pipeline validation thresholds passed. Committing structural assets...")
        target_component = resolve_absolute_path(target_day["target_component"])
        subprocess.run(["git", "add", target_component], capture_output=True)
        test_component = resolve_absolute_path(target_day["test_component"])
        subprocess.run(["git", "add", test_component], capture_output=True)
        doc_component = resolve_absolute_path(target_day["doc_component"])
        subprocess.run(["git", "add", doc_component], capture_output=True)
        commit_msg = f"feat(day-{self.day_num}): complete enterprise sub-tasks for day {self.day_num} [AI Pipeline]"
        subprocess.run(["git", "commit", "-m", commit_msg], capture_output=True)
        subprocess.run(["git", "push", "origin", self.target_branch], capture_output=True)
        print(f"[ ✅ MANAGER SUCCESS ] Isolated feature branch {self.target_branch} pushed safely to remote origin!")

    def execute_pipeline(self):
        self.setup_daily_git_branch()
        
        steps_path = f"{STEPS_PLAN_DIR}/phase-{self.phase_str}.agent.steps.json"
        if not os.path.exists(steps_path):
            self.execute_emergency_purge()
            
        with open(steps_path, "r", encoding="utf-8") as f:
            steps_data = json.load(f)
        target_day = next((d for d in steps_data["days"] if d["day"] == self.day_num), None)
        
        # Act 1-4: Core Development Multi-Agent Execution Loop
        print("\n[STEP 1/7] Activating Standalone Coder Agent...")
        if subprocess.run(["python", f"{SUB_AGENTS_DIR}/agent-coder.py", "--phase", self.phase_str, "--day", str(self.day_num)]).returncode != 0:
            self.execute_emergency_purge()

        print("\n[STEP 2/7] Activating Standalone Tester Agent...")
        if subprocess.run(["python", f"{SUB_AGENTS_DIR}/agent-tester.py", "--phase", self.phase_str, "--day", str(self.day_num)]).returncode != 0:
            self.execute_emergency_purge()

        print("\n[STEP 3/7] Activating Standalone Bug Fixer Agent & Real Compiler Gate...")
        if subprocess.run(["python", f"{SUB_AGENTS_DIR}/agent-fixer.py", "--phase", self.phase_str, "--day", str(self.day_num)]).returncode != 0:
            self.execute_emergency_purge()

        print("\n[STEP 4/7] Activating Standalone Documentation Agent...")
        if subprocess.run(["python", f"{SUB_AGENTS_DIR}/agent-doc.py", "--phase", self.phase_str, "--day", str(self.day_num)]).returncode != 0:
            self.execute_emergency_purge()

        # CONDITIONAL DISTRIBUTED CLOUD MULTI-REGISTRY RELEASE ENGINE GATEWAY
        if self.is_release_triggered:
            # Step 5: Execute Google Artifact Registry Images Push Workflow
            print("\n[STEP 5/7] [MANUAL RELEASE] Activating Standalone GCP Registry Infrastructure Agent...")
            if subprocess.run(["python", f"{SUB_AGENTS_DIR}/agent-gcp.py", "--phase", self.phase_str, "--day", str(self.day_num)]).returncode != 0:
                print("[ 💀 DEPLOY-ERROR ] GCP container image publication failed. Halting deployment pipeline.")
                self.execute_emergency_purge()

            # NEW PIPELINE WORKLOAD NODE: Step 6: Execute Docker Hub Public/Internal Registry Push Workflow
            print("\n[STEP 6/7] [MANUAL RELEASE] Activating Standalone Docker Hub Registry Infrastructure Agent...")
            if subprocess.run(["python", f"{SUB_AGENTS_DIR}/agent-docker.py", "--phase", self.phase_str, "--day", str(self.day_num)]).returncode != 0:
                print("[ 💀 DEPLOY-ERROR ] Docker Hub public registry image publication failed. Halting deployment pipeline.")
                self.execute_emergency_purge()

            # Step 7: Execute GKE Pod Cluster Workloads Rollout Update Workflow
            print("\n[STEP 7/7] [MANUAL RELEASE] Activating Standalone GKE Container Orchestration Agent...")
            if subprocess.run(["python", f"{SUB_AGENTS_DIR}/agent-gke.py", "--phase", self.phase_str, "--day", str(self.day_num)]).returncode != 0:
                print("[ 💀 DEPLOY-ERROR ] GKE cluster application workload rollout update failed. Halting pipeline.")
                self.execute_emergency_purge()
        else:
            print("\n[STEPS 5,6,7/7] [RELEASE SKIPPED] Manual release flag absent. Assets retained locally.")

        # Complete and push daily baseline deliverables up to repository server host
        self.push_daily_git_branch(target_day)
        print(f"\n[ ✅ ORCHESTRATOR COMPLETE ] Day {self.day_num} operations successfully completed!")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", required=True, help="Target Phase formatted string or number (e.g., 1 or 01)")
    parser.add_argument("--day", required=True, help="Target Day sequence position (e.g., 1)")
    parser.add_argument("--release", action="store_true", help="Trigger manual production rollout deployment to all cloud registries")
    args = parser.parse_args()
    
    EnterpriseMultiAgentManager(args.phase, args.day, is_release_triggered=args.release).execute_pipeline()
