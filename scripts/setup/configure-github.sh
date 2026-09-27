#!/usr/bin/env bash
set -euo pipefail
repository=yellow-pang/health-center-smart-reservation
if [[ "${1:-}" != --apply ]]; then
  echo 'Usage: bash scripts/setup/configure-github.sh --apply'
  echo 'Creates production (main only), main PR/CI protection, and external PR workflow approval after CI is on dev.'
  exit 1
fi
temporary=$(mktemp -d)
trap 'rm -rf "$temporary"' EXIT

# Complete every read/compatibility check before changing remote settings.
get_optional() {
  local endpoint=$1 output=$2
  if gh api "$endpoint" > "$output" 2> "$temporary/error"; then
    return 0
  fi
  if grep -q 'HTTP 404' "$temporary/error"; then
    printf 'null\n' > "$output"
  else
    cat "$temporary/error" >&2
    return 1
  fi
}
gh api "repos/$repository/contents/.github/workflows/ci.yml?ref=dev" >/dev/null
gh api apps/github-actions > "$temporary/app.json"
get_optional "repos/$repository/environments/production" "$temporary/environment.json"
get_optional "repos/$repository/branches/main/protection" "$temporary/protection.json"
gh api "repos/$repository/actions/permissions/fork-pr-contributor-approval" > "$temporary/fork-policy.json"
if [[ "$(cat "$temporary/environment.json")" != null ]]; then
  gh api "repos/$repository/environments/production/deployment-branch-policies?per_page=100" > "$temporary/policies.json"
else
  printf '{"branch_policies":[]}\n' > "$temporary/policies.json"
fi

python3 - "$temporary" <<'PY'
import json, pathlib, sys
directory = pathlib.Path(sys.argv[1])
read = lambda name: json.loads((directory / name).read_text())
app = read('app.json')
app_id = app.get('id')
if app.get('slug') != 'github-actions' or not isinstance(app_id, int) or app_id <= 0:
    raise SystemExit('Cannot identify the official GitHub Actions app')
environment = read('environment.json')
policies = read('policies.json')['branch_policies']
if environment is not None:
    if environment.get('deployment_branch_policy') != {
        'protected_branches': False, 'custom_branch_policies': True,
    }:
        raise SystemExit('Existing production environment has a different branch policy; review it manually. Nothing changed.')
    if any(p['name'] != 'main' or p.get('type', 'branch') != 'branch' for p in policies):
        raise SystemExit('Unexpected production branch policy exists; review it manually. Nothing changed.')
protection = read('protection.json')
if protection is not None:
    checks = protection.get('required_status_checks') or {}
    ci_required = any(check.get('context') == 'CI required' and check.get('app_id') == app_id
                      for check in checks.get('checks', []))
    compatible = (checks.get('strict') is True and ci_required
                  and protection.get('enforce_admins', {}).get('enabled') is True
                  and protection.get('required_pull_request_reviews') is not None
                  and protection.get('allow_force_pushes', {}).get('enabled') is False
                  and protection.get('allow_deletions', {}).get('enabled') is False)
    if not compatible:
        raise SystemExit('Existing main protection must require PRs and strict CI required from GitHub Actions, enforce admins, and forbid force pushes/deletion. Review it manually. Nothing changed.')
approval = read('fork-policy.json').get('approval_policy')
if approval not in {'first_time_contributors_new_to_github', 'first_time_contributors', 'all_external_contributors'}:
    raise SystemExit('Unknown fork PR approval policy; review it manually. Nothing changed.')
plan = {'environment': environment is None, 'branch_policy': not policies,
        'protection': protection is None, 'fork_policy': approval != 'all_external_contributors'}
(directory / 'plan.json').write_text(json.dumps(plan))
(directory / 'new-environment.json').write_text(json.dumps({
    'deployment_branch_policy': {'protected_branches': False, 'custom_branch_policies': True},
}))
(directory / 'new-protection.json').write_text(json.dumps({
    'required_status_checks': {'strict': True, 'contexts': [],
                              'checks': [{'context': 'CI required', 'app_id': app_id}]},
    'enforce_admins': True,
    'required_pull_request_reviews': {'required_approving_review_count': 0, 'dismiss_stale_reviews': True},
    'restrictions': None, 'allow_force_pushes': False, 'allow_deletions': False,
}))
PY
planned() {
  python3 -c 'import json,sys; sys.exit(0 if json.load(open(sys.argv[1]))[sys.argv[2]] else 1)' "$temporary/plan.json" "$1"
}
# This gate must be active before registering a public-repository self-hosted runner.
# Approval is a maintainer review step, not a sandbox for approved PR code.
if planned fork_policy; then
  gh api --method PUT "repos/$repository/actions/permissions/fork-pr-contributor-approval" \
    -f approval_policy=all_external_contributors >/dev/null
fi
if planned protection; then
  gh api --method PUT "repos/$repository/branches/main/protection" --input "$temporary/new-protection.json" >/dev/null
fi
if planned environment; then
  gh api --method PUT "repos/$repository/environments/production" --input "$temporary/new-environment.json" >/dev/null
fi
if planned branch_policy; then
  gh api --method POST "repos/$repository/environments/production/deployment-branch-policies" \
    -f name=main -f type=branch >/dev/null
fi
echo 'Verified production main-only, main PR/CI protection, and approval for every external contributor workflow.'
