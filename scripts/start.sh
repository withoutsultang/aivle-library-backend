#!/usr/bin/env bash
set -euo pipefail
APP_DIR="${APP_DIR:-/home/ubuntu/app}"
JAR_PATH="$APP_DIR/app.jar"
PARAM_PATH="${SSM_PARAMETER_PATH:-/portfolio/library/runtime/}"
AWS_REGION="${AWS_REGION:-us-west-1}"
test -f "$JAR_PATH"
command -v jq >/dev/null
parameters=$(aws ssm get-parameters-by-path --region "$AWS_REGION" --path "$PARAM_PATH" --with-decryption --recursive --output json)
while IFS= read -r row; do
  entry=$(printf '%s' "$row" | base64 --decode)
  name=$(printf '%s' "$entry" | jq -r '.Name')
  value=$(printf '%s' "$entry" | jq -r '.Value')
  key="${name##*/}"
  case "$key" in
    DB_*|NAVER_*|JWT_*|S3_BUCKET_NAME|AWS_REGION|FRONTEND_BASE_URL|CORS_ALLOWED_ORIGINS|DYNAMODB_TABLE|IMAGE_SOURCE_HOSTS|OPENAI_API_KEY|IMAGE_MODEL|SERVER_PORT)
      export "$key=$value" ;;
  esac
done < <(printf '%s' "$parameters" | jq -r '.Parameters[] | @base64')
: "${DB_URL:?DB_URL is required}"
: "${DB_USERNAME:?DB_USERNAME is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"
: "${JWT_SECRET:?JWT_SECRET is required}"
: "${S3_BUCKET_NAME:?S3_BUCKET_NAME is required}"
: "${NAVER_CLIENT_ID:?NAVER_CLIENT_ID is required}"
: "${NAVER_CLIENT_SECRET:?NAVER_CLIENT_SECRET is required}"
: "${NAVER_REDIRECT_URI:?NAVER_REDIRECT_URI is required}"
: "${NAVER_STATE:?NAVER_STATE is required}"
port="${SERVER_PORT:-8080}"
pid=$(lsof -tiTCP:"$port" -sTCP:LISTEN || true)
if [ -n "$pid" ]; then
  kill -TERM $pid
  for attempt in {1..20}; do
    if ! lsof -tiTCP:"$port" -sTCP:LISTEN >/dev/null; then break; fi
    sleep 1
  done
  if lsof -tiTCP:"$port" -sTCP:LISTEN >/dev/null; then
    echo "Application port is still in use"
    exit 1
  fi
fi
nohup java -jar "$JAR_PATH" > "$APP_DIR/nohup.out" 2>&1 &
echo "Application started; readiness is checked by ValidateService"
