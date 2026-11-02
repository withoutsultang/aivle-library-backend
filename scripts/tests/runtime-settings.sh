#!/usr/bin/env bash
# Contract test: no AWS requests, Java processes or real listener termination.
set -euo pipefail
# Native Windows jq needs binary output when called through Git Bash.
if [[ "${OSTYPE:-}" == msys* || "${OSTYPE:-}" == cygwin* ]]; then
  jq() { command jq -b "$@"; }
  export -f jq
fi
repo_dir=$(cd "$(dirname "$0")/../.." && pwd)
fixture_dir=$(mktemp -d)
trap 'rm -f "$fixture_dir/app.jar" "$fixture_dir/started" "$fixture_dir/nohup.out"; rmdir "$fixture_dir"' EXIT
touch "$fixture_dir/app.jar"
export APP_DIR="$fixture_dir" TEST_MARKER="$fixture_dir/started"
export LIBRARY_HOST_CONFIG=/dev/null
export DB_SECRET_ARN='arn:aws:secretsmanager:us-west-1:123456789012:secret:test-fixture'
aws() {
  if [ "$1" = secretsmanager ]; then
    if [ "${FAIL_SECRET_READ:-false}" = true ]; then return 1; fi
    printf '{"password":"runtime-fixture-only"}'
  else
    printf '%s' '{"Parameters":[{"Name":"/fixture/DB_URL","Value":"jdbc:mysql://fixture/library"},{"Name":"/fixture/DB_USERNAME","Value":"fixture"},{"Name":"/fixture/JWT_SECRET","Value":"fixture"},{"Name":"/fixture/S3_BUCKET_NAME","Value":"fixture"},{"Name":"/fixture/NAVER_CLIENT_ID","Value":"fixture"},{"Name":"/fixture/NAVER_CLIENT_SECRET","Value":"fixture"},{"Name":"/fixture/NAVER_REDIRECT_URI","Value":"https://example.test/callback"},{"Name":"/fixture/NAVER_STATE","Value":"fixture"}]}'
  fi
}
lsof() { return 1; }
nohup() {
  test "$DB_PASSWORD" = runtime-fixture-only
  test "$1" = java
  touch "$TEST_MARKER"
}
export -f aws lsof nohup
bash "$repo_dir/scripts/start.sh"
for attempt in {1..20}; do
  if [ -f "$TEST_MARKER" ]; then break; fi
  sleep 0.1
done
test -f "$TEST_MARKER"
rm -f "$TEST_MARKER"
# A missing secret must fail before any application launch.
export FAIL_SECRET_READ=true
if bash "$repo_dir/scripts/start.sh" >/dev/null 2>&1; then
  echo 'Missing runtime secret unexpectedly succeeded'
  exit 1
fi
test ! -f "$TEST_MARKER"
echo 'Runtime secret injection and fail-before-launch contracts passed'
