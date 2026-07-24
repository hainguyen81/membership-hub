#!/usr/bin/env python3
import json
import os
import sys

def main():
    plan_path = os.environ.get("PLAN_FILE", ".ai/.plan/plan.spec.json")
    state_path = os.environ.get("STATE_FILE", ".ai/.agents/.states/pipeline_state.json")
    
    if not os.path.exists(plan_path):
        print(f"❌ [ERROR] Plan specification file not found at: {plan_path}")
        sys.exit(1)
    
    # read plan json file
    with open(plan_path, 'r') as f:
        plan = json.load(f)
    
    # parse plan information
    phases_config = plan.get("phases", [])
    total_phases = plan.get("num_phases", len(phases_config))
    
    # Ingest enviroment for GitHub Actions
    exec_mode = os.environ.get("INPUT_EXEC_MODE", "auto_cron") or "auto_cron"
    scope = os.environ.get("INPUT_TARGET_SCOPE", "")
    val_str = os.environ.get("INPUT_VALUE", "")
    
    final_phase = 1
    final_day = 1
    should_save_state = False
    project_ended = False

    # -------------------------------------------------------------
    # 🕒 CASE 1: AUTO_CRON (Auto cron based on STATE_FILE)
    # -------------------------------------------------------------
    if exec_mode == "auto_cron":
        should_save_state = True
        print("🕒 [MODE] Detected Auto Cron Execution. Resolving historical state matrix...")
        
        # load previous state
        if os.path.exists(state_path):
            with open(state_path, 'r') as f:
                state = json.load(f)
            curr_day = state.get("current_day", 0)
            curr_phase = state.get("current_phase", 1)
            print(f"📖 [READ] Prior stored baseline matrix: Phase {curr_phase} / Day {curr_day}")
        
        # Initial state for first running time
        else:
            curr_day = 0
            curr_phase = 1
            print("🆕 [INIT] Stored baseline not found. Instantiating standard origin baseline (Phase 1 / Day 0)...")
        
        # parse phase config
        phase_meta = next((p for p in phases_config if p["phase"] == curr_phase), None)
        
        # calculate running day/phase
        if phase_meta and curr_day < phase_meta["days"]:
            final_day = curr_day + 1
            final_phase = curr_phase
        else:
            final_phase = curr_phase + 1
            final_day = 1
        
        # exceed phase number, it means project already finished
        if final_phase > total_phases:
            project_ended = True

    # -------------------------------------------------------------
    # 🎛️ TRƯỜNG HỢP 2: MANUAL (Chạy thủ công bằng tay)
    # -------------------------------------------------------------
    else:
        should_save_state = False
        print("🎛️ [MODE] Detected Manual Override Target Mode. Evaluating dynamic constraints...")
        if not val_str:
            print("❌ [ERROR] Manual execution mode requires a target INPUT_VALUE!")
            sys.exit(1)
        
        # based on input value to run manual
        val = int(val_str)
        
        # run by day
        if scope == "by_day":
            # check whether exceed total_days of phases
            total_days_allowed = plan.get("total_days", sum(p["days"] for p in phases_config))
            if val > total_days_allowed or val <= 0:
                print(f"❌ [ERROR] Targeted absolute day ({val}) exceeds project maximum ({total_days_allowed})!")
                sys.exit(1)
            
            # calculate phase that need to run
            accumulated_days = 0
            found = False
            for p in phases_config:
                if accumulated_days < val <= accumulated_days + p["days"]:
                    final_phase = p["phase"]
                    final_day = val - accumulated_days
                    found = True
                    break
                accumulated_days += p["days"]
            
            # not found any phase match calculated running day
            if not found:
                print("❌ [ERROR] Failed to map absolute day metrics to localized Phase structures.")
                sys.exit(1)
        
        # run by phase
        else:
            # exceed total_phases
            if val > total_phases or val <= 0:
                print(f"❌ [ERROR] Targeted Phase ID ({val}) exceeds project schema bounds ({total_phases})!")
                sys.exit(1)
            
            # start inputted phase from day 1
            final_phase = val
            final_day = 1

    # Export calculated values to temporary enviroment file for GitHub Actions
    with open(".agent_resolved_state", "w") as env_f:
        env_f.write(f"RESOLVED_DAY={final_day}\n")
        env_f.write(f"RESOLVED_PHASE={final_phase}\n")
        env_f.write(f"SHOULD_SAVE_STATE={'true' if should_save_state else 'false'}\n")
        env_f.write(f"PROJECT_ENDED={'true' if project_ended else 'false'}\n")

if __name__ == "__main__":
    main()
