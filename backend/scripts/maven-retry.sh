#!/bin/sh
set -u

if [ "$#" -eq 0 ]; then
  echo 'Usage: sh maven-retry.sh <Maven goals and options>' >&2
  exit 2
fi

log_file=$(mktemp)
trap 'rm -f "$log_file"' 0
trap 'exit 130' INT
trap 'exit 143' TERM
attempt=1
max_attempts=3

while [ "$attempt" -le "$max_attempts" ]; do
  printf 'Maven attempt %s/%s\n' "$attempt" "$max_attempts"
  # -U rechecks missing releases after a failed download cached by Maven.
  if mvn -B --no-transfer-progress -U "$@" > "$log_file" 2>&1; then
    cat "$log_file"
    exit 0
  else
    status=$?
    cat "$log_file"
  fi

  if [ "$status" -ge 128 ]; then
    exit "$status"
  fi

  # Retry repository availability errors, while compile/configuration errors fail immediately.
  if ! grep -Eq 'Could not transfer (artifact|metadata).*(status code: (408|429|500|502|503|504)([^0-9]|$)|timed out|Connection reset|Connection refused|Temporary failure in name resolution|UnknownHostException)' "$log_file"; then
    echo 'Maven failed without a retryable repository transfer error.' >&2
    exit "$status"
  fi
  if [ "$attempt" -eq "$max_attempts" ]; then
    echo 'Maven repository transfer still failed after 3 attempts.' >&2
    exit "$status"
  fi

  delay=$((attempt * 10))
  printf 'Repository transfer failed; retrying Maven in %s seconds.\n' "$delay" >&2
  sleep "$delay"
  attempt=$((attempt + 1))
done
