#!/usr/bin/env bash
set -euo pipefail
version=1.7.12
case "$(uname -s)/$(uname -m)" in
  Linux/x86_64) platform=linux_amd64; checksum=8aca8db96f1b94770f1b0d72b6dddcb1ebb8123cb3712530b08cc387b349a3d8 ;;
  Darwin/arm64) platform=darwin_arm64; checksum=aba9ced2dee8d27fecca3dc7feb1a7f9a52caefa1eb46f3271ea66b6e0e6953f ;;
  *) echo 'Run workflow validation on Linux x64 or macOS ARM64.' >&2; exit 1 ;;
esac
temporary=$(mktemp -d)
trap 'rm -rf "$temporary"' EXIT
archive="$temporary/actionlint.tar.gz"
curl --fail --silent --show-error --location --retry 3 \
  "https://github.com/rhysd/actionlint/releases/download/v$version/actionlint_${version}_${platform}.tar.gz" -o "$archive"
python3 - "$archive" "$checksum" <<'PY'
import hashlib, pathlib, sys
if hashlib.sha256(pathlib.Path(sys.argv[1]).read_bytes()).hexdigest() != sys.argv[2]:
    raise SystemExit('actionlint checksum mismatch')
PY
tar -xzf "$archive" -C "$temporary" actionlint
"$temporary/actionlint" -color -shellcheck=''
