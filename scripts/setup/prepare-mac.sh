#!/usr/bin/env bash
set -euo pipefail
umask 077
repo_root=$(cd "$(dirname "$0")/../.." && pwd)
deploy_root=${DEPLOY_ROOT:-/Users/tro/services/health-center}
allow_initial_adoption=0
if [[ "${1:-}" == --allow-initial-adoption ]]; then
  allow_initial_adoption=1
  shift
fi
if [[ "$#" -gt 1 || "${1:-}" == --* ]]; then
  echo 'Usage: bash scripts/setup/prepare-mac.sh [--allow-initial-adoption] [source-env]' >&2
  exit 1
fi
source_env=${1:-"$repo_root/.env"}
[[ "$(uname -s)/$(uname -m)" == Darwin/arm64 ]] || { echo 'Run on the production Apple Silicon Mac.' >&2; exit 1; }
python3 "$repo_root/scripts/setup/validate-directory.py" deploy "$deploy_root"
if [[ "$allow_initial_adoption" == 1 && ( -e "$deploy_root/current" || -L "$deploy_root/current" ) ]]; then
  echo 'Initial adoption cannot be authorized after a managed current release exists.' >&2
  exit 1
fi
[[ "$(docker info --format '{{.OSType}}/{{.Architecture}}')" =~ ^linux/(aarch64|arm64)$ ]] || {
  echo 'Select the OrbStack Linux ARM64 Docker context first.' >&2; exit 1;
}
[[ "$(docker inspect health-center-postgres --format '{{.State.Health.Status}}')" == healthy ]] || {
  echo 'The existing health-center-postgres must be healthy.' >&2; exit 1;
}
[[ -f "$source_env" ]] || { echo 'Pass the existing Health Center .env file.' >&2; exit 1; }
mkdir -p "$deploy_root/shared" "$deploy_root/releases" "$deploy_root/backups"
chmod 700 "$deploy_root" "$deploy_root/shared" "$deploy_root/releases" "$deploy_root/backups"
if [[ -e "$deploy_root/shared/production.env" ]]; then
  python3 - "$deploy_root/shared/production.env" <<'PY'
from pathlib import Path
import stat, sys
if stat.S_IMODE(Path(sys.argv[1]).stat().st_mode) & 0o077:
    raise SystemExit('Existing production.env must have mode 600; review its permissions before setup')
PY
  echo 'production.env already exists; preserving it.'
else
  cp "$source_env" "$deploy_root/shared/production.env"
  chmod 600 "$deploy_root/shared/production.env"
  echo 'Copied the existing environment into the private deployment directory.'
fi
if [[ "$allow_initial_adoption" == 1 ]]; then
  python3 - "$deploy_root/shared/allow-initial-adoption" <<'PY'
import os, pathlib, stat, sys
marker = pathlib.Path(sys.argv[1])
if marker.is_symlink() or (marker.exists() and not marker.is_file()):
    raise SystemExit('Initial adoption marker must be a regular, non-symlink file')
if marker.exists():
    if stat.S_IMODE(marker.stat().st_mode) != 0o600:
        raise SystemExit('Existing initial adoption marker must have mode 600')
else:
    descriptor = os.open(marker, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    with os.fdopen(descriptor, 'w') as handle:
        handle.write('One-time authorization to back up and adopt the existing database on first deployment.\n')
PY
  echo 'Authorized initial database adoption once; deployment consumes the marker after verification.'
fi
echo 'Host directories are ready. No containers or database contents were changed.'
echo 'The deployment Compose forces prod, disables demo initialization, and limits each service to its own variables.'
