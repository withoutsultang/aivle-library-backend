#!/usr/bin/env bash
set -euo pipefail
HEALTH_URL="${HEALTH_URL:-http://localhost:8080/api/v1/health}"
for attempt in {1..10}; do
  code=$(curl --connect-timeout 1 --max-time 2 -s -o /dev/null -w '%{http_code}' "$HEALTH_URL" || true)
  if [ "$code" = "200" ]; then
    echo "Application health check passed"
    exit 0
  fi
  echo "Waiting for readiness ($attempt/10, HTTP $code)"
  sleep 2
done
echo "Application health check failed"
exit 1
