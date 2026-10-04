#!/usr/bin/env bash
set -euo pipefail
umask 077
repository=yellow-pang/health-center-smart-reservation
runner_root=${RUNNER_ROOT:-/Users/tro/actions-runner-health-center}
script_root=$(cd "$(dirname "$0")" && pwd)
[[ "$(uname -s)/$(uname -m)" == Darwin/arm64 && "$(id -u)" != 0 ]] || {
  echo 'Run as the logged-in Mac ARM64 operating user, without sudo.' >&2; exit 1;
}
if [[ -e "$runner_root/.runner" ]]; then
  echo 'Runner already configured. Use its svc.sh status/start commands.' >&2
  exit 1
fi
python3 "$script_root/validate-directory.py" runner "$runner_root"
for tool in gh python3 docker curl; do command -v "$tool" >/dev/null; done
gh auth status
[[ "$(gh api "repos/$repository/actions/permissions/fork-pr-contributor-approval" --jq .approval_policy)" == all_external_contributors ]] || {
  echo 'Configure approval for every external PR workflow before registering this runner.' >&2; exit 1;
}
temporary=$(mktemp -d)
trap 'rm -rf "$temporary"; unset registration_token' EXIT
gh api repos/actions/runner/releases/latest > "$temporary/release.json"
python3 - "$temporary" <<'PY'
import hashlib, json, pathlib, re, sys, urllib.request
directory = pathlib.Path(sys.argv[1])
release = json.loads((directory / 'release.json').read_text())
assets = [a for a in release['assets'] if re.fullmatch(r'actions-runner-osx-arm64-[0-9.]+\.tar\.gz', a['name'])]
if len(assets) != 1:
    raise SystemExit('Cannot identify the official macOS ARM64 runner archive')
asset = assets[0]
digest = asset.get('digest', '')
if not re.fullmatch(r'sha256:[a-f0-9]{64}', digest):
    raise SystemExit('Official release has no SHA-256 digest; verify it manually before installing')
url = asset['browser_download_url']
if not url.startswith('https://github.com/actions/runner/releases/download/'):
    raise SystemExit('Unexpected runner download origin')
archive = directory / 'runner.tar.gz'
urllib.request.urlretrieve(url, archive)
if hashlib.sha256(archive.read_bytes()).hexdigest() != digest.split(':')[1]:
    raise SystemExit('Runner archive checksum mismatch')
print('Official runner archive checksum verified.')
PY
# Recheck after downloading; do not overwrite files created while setup was running.
python3 "$script_root/validate-directory.py" runner "$runner_root"
mkdir -p "$runner_root"
chmod 700 "$runner_root"
tar -xzf "$temporary/runner.tar.gz" -C "$runner_root"
cd "$runner_root"
registration_token=$(gh api --method POST "repos/$repository/actions/runners/registration-token" --jq .token)
./config.sh --unattended --url "https://github.com/$repository" \
  --token "$registration_token" --name health-center-mac-mini \
  --labels health-center-prod --work _work
unset registration_token
./svc.sh install
./svc.sh start
echo 'Runner installed as a user LaunchAgent. Mac login and OrbStack are required for deployment.'
