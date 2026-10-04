#!/usr/bin/env bash
set -euo pipefail
# CI database only. Do not accidentally launch this test against the local production DB.
if [[ "${DB_NAME:-}" != health_center_ci || "${DB_USERNAME:-}" != health_ci ]]; then
  echo 'Smoke tests require the disposable health_center_ci database and health_ci user.' >&2
  exit 1
fi
export SPRING_PROFILES_ACTIVE=prod
export SPRING_FLYWAY_BASELINE_ON_MIGRATE=false
export SPRING_SQL_INIT_MODE=never
export ACCOUNT_RECOVERY_EXPOSE_DEVELOPMENT_TOKEN=false
export QUEUE_AUTO_CLOSE_ENABLED=false
export INFO_APP_VERSION=ci-smoke
export SERVER_PORT=18080
temporary=$(mktemp -d)
pid=''
cleanup() {
  if [[ -n "$pid" ]]; then kill "$pid" 2>/dev/null || true; wait "$pid" 2>/dev/null || true; fi
  rm -rf "$temporary"
}
trap cleanup EXIT
jar=$(find backend/target -maxdepth 1 -name '*.jar' -print -quit)
[[ -n "$jar" ]] || { echo 'Build the backend jar first.' >&2; exit 1; }
java -jar "$jar" > "$temporary/backend.log" 2>&1 &
pid=$!
for ((attempt=0; attempt<60; attempt++)); do
  if ! kill -0 "$pid" 2>/dev/null; then
    tail -80 "$temporary/backend.log" >&2
    exit 1
  fi
  if curl --fail --silent --max-time 2 http://127.0.0.1:18080/actuator/health > "$temporary/health.json"; then
    if python3 - "$temporary/health.json" <<'PY'
import json, sys
assert json.load(open(sys.argv[1]))['status'] == 'UP'
PY
    then
      curl --fail --silent --show-error http://127.0.0.1:18080/actuator/info | \
        python3 -c 'import json,sys; assert json.load(sys.stdin)["app"]["version"] == "ci-smoke"'
      echo 'Production profile startup, database health and release version verified.'
      exit 0
    fi
  fi
  sleep 2
done
tail -80 "$temporary/backend.log" >&2
echo 'Backend did not become healthy within 120 seconds.' >&2
exit 1
