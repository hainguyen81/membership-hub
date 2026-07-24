#!/bin/bash
export STATE_FILE=".ai/.agents/.states/pipeline_state.json"
export PLAN_FILE=".ai/.plan/plan.spec.json"

# 🧹 CLEAN WORKSPACE
echo "🧹 [WIPE] Vaporizing all untracked and modified files locally..."
git reset --hard HEAD
git clean -ffdx

# make sure directory existing
mkdir -p "$(dirname "$STATE_FILE")"
mkdir -p "$(dirname "$PLAN_FILE")"

# 🐍 call python script to calculate running day/phase
python3 .github/scripts/ci-cd.agent-manager.state-resolver.py

# adapt calculated running day/phase to GitHub Actions Enviroment
source .agent_resolved_state && rm -f .agent_resolved_state

# 🏁 if project already finished, remove schedule
if [ "$PROJECT_ENDED" = "true" ]; then
  echo "🎉✨ [PROJECT ENDED] All phases and target development timelines executed successfully!"
  echo "🛑🛑 Triggering automated schedule workflow removal protocols..."
  gh workflow disable "${GITHUB_WORKFLOW}" || true
  echo "✅🏁 Schedule automated execution successfully disabled. Terminating pipeline gracefully."
  exit 0
fi

# 📤 export calculated running day/phase to GITHUB_OUTPUT for later sub-agents steps
echo "run_day=$RESOLVED_DAY" >> "$GITHUB_OUTPUT"
echo "run_phase=$RESOLVED_PHASE" >> "$GITHUB_OUTPUT"
echo "phase_ended=$PHASE_ENDED" >> "$GITHUB_OUTPUT"

# 🌐 define branch for sub-agents working
BRANCH_NAME="features/development-phase-$RESOLVED_PHASE-day-$RESOLVED_DAY"
echo "🌐 [GIT ROUTER] Target environment workspace branch assigned: $BRANCH_NAME"

rm -f "$STATE_FILE"
git checkout -- "$STATE_FILE" 2>/dev/null || true

echo "🔄 [GIT FETCH] Fetching standard mainline development trunk baseline..."
git fetch origin features/development || true
git checkout -f features/development || git checkout -f master

# 🌿 Initialize independent sub-branch tracking layer cleanly
if git show-ref --verify --quiet "refs/heads/$BRANCH_NAME" || git show-ref --verify --quiet "refs/remotes/origin/$BRANCH_NAME"; then
  echo "🌿 [GIT MERGE] Existing target branch detected. Synchronizing latest trunk modifications..."
  rm -f "$STATE_FILE"
  git checkout -f "$BRANCH_NAME"
  git merge origin/features/development --no-edit || true

else
  echo "🚀 [GIT FORK] Provisioning pristine branch [$BRANCH_NAME] initialized from stable trunk root"
  git checkout -f -b "$BRANCH_NAME"
fi
 
# push changes to development phase/day branch before sub-agents running
git push origin HEAD:"refs/heads/$BRANCH_NAME" || true

# Save state later, when sub-agents run success fully
# # 🚀 ABSOLUTE WRITE OVERWRITE: Re-write the correct synced JSON payload right inside the active workspace branch
# if [ "$SHOULD_SAVE_STATE" = "true" ]; then
#   echo "💾 [STATE SAVE] Serializing current matrix back to file tracking storage..."
#   mkdir -p "$(dirname "$STATE_FILE")"
#   echo "{\"current_day\": $RESOLVED_DAY, \"current_phase\": $RESOLVED_PHASE}" > "$STATE_FILE"
#   
#   git add "$STATE_FILE"
#   git commit -m "chore(pipeline): record cron incremental state to Phase $RESOLVED_PHASE Day $RESOLVED_DAY [AI Loop]" || true
#   git push origin HEAD:"refs/heads/$BRANCH_NAME" --force || true
#   echo "✨ [SUCCESS] Dynamic matrix pointers synchronized upstream!"
# else
#   echo "⚠️ [MANUAL MODE] Skipping state serialization rules. $STATE_FILE remains unmodified."
# fi
