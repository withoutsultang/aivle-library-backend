#!/bin/bash
set -euo pipefail

REPOSITORY=/home/ubuntu/app
# 수정: 특정 이름을 지정하는 대신 .jar로 끝나는 파일을 찾도록 변경했습니다.
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)
PARAM_PATH="/portfolio/library/runtime"
AWS_REGION="us-west-1"

echo "> 현재 실행 중인 애플리케이션 PID 확인"
CURRENT_PID=$(lsof -ti:8080 || true)

if [ -n "$CURRENT_PID" ]; then
  echo "> kill -15 $CURRENT_PID"
  kill -15 "$CURRENT_PID" || true
  sleep 5
else
  echo "> 현재 실행 중인 애플리케이션이 없으므로 종료하지 않습니다."
fi

echo "> 새 애플리케이션 배포 준비"

# 수정: 파일 존재 여부 체크 로직 보완
if [ -z "$JAR_NAME" ] || [ ! -f "$JAR_NAME" ]; then
  echo "> 에러: JAR 파일을 찾을 수 없습니다."
  ls -al "$REPOSITORY"
  exit 1
fi

echo "> 실행 파일: $JAR_NAME"
chmod +x "$JAR_NAME"

echo "> Load env from SSM Parameter Store: $PARAM_PATH (Region: $AWS_REGION)"

while read -r name value; do
  key=$(basename "$name")
  export "$key=$value"
done < <(aws --region "$AWS_REGION" ssm get-parameters-by-path \
  --path "$PARAM_PATH" \
  --with-decryption \
  --recursive \
  --query "Parameters[*].[Name,Value]" \
  --output text)

# 필수 env 체크
: "${DB_URL:?DB_URL is required}"
: "${DB_USERNAME:?DB_USERNAME is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"
: "${JWT_SECRET:?JWT_SECRET is required}"

JWT_EXPIRATION_MS=${JWT_EXPIRATION_MS:-3600000}

echo "> 애플리케이션 실행"
# JAR_NAME 변수에 이미 전체 경로가 담기도록 수정되었습니다.
nohup java \
  -Dspring.datasource.url="$DB_URL" \
  -Dspring.datasource.username="$DB_USERNAME" \
  -Dspring.datasource.password="$DB_PASSWORD" \
  -Djwt.secret="$JWT_SECRET" \
  -Djwt.expiration-ms="$JWT_EXPIRATION_MS" \
  -Dnaver.client-id="${NAVER_CLIENT_ID:-}" \
  -Dnaver.client-secret="${NAVER_CLIENT_SECRET:-}" \
  -Dnaver.redirect-uri="${NAVER_REDIRECT_URI:-}" \
  -Dnaver.state="${NAVER_STATE:-}" \
  -jar "$JAR_NAME" > "$REPOSITORY/nohup.out" 2>&1 &

echo "> Started. tail -f $REPOSITORY/nohup.out"