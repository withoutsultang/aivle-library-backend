#!/bin/bash
set -euo pipefail

REPOSITORY=/home/ubuntu/app
JAR_NAME=app.jar
PARAM_PATH="/portfolio/library/runtime"
# 에러 해결: 리전 정보를 수동으로 고정했습니다.
AWS_REGION="us-west-1"

echo "> 현재 실행 중인 애플리케이션 PID 확인(8080)"
CURRENT_PID=$(lsof -ti:8080 || true)

if [ -n "$CURRENT_PID" ]; then
  echo "> kill -15 $CURRENT_PID"
  kill -15 "$CURRENT_PID" || true
  sleep 5
else
  echo "> 현재 실행 중인 애플리케이션이 없으므로 종료하지 않습니다."
fi

echo "> 새 애플리케이션 배포 준비"

if [ ! -f "$REPOSITORY/$JAR_NAME" ]; then
  echo "> 에러: $REPOSITORY/$JAR_NAME 파일을 찾을 수 없습니다."
  echo "> 현재 디렉토리 파일 목록:"
  ls -al "$REPOSITORY"
  exit 1
fi

echo "> 실행 파일: $REPOSITORY/$JAR_NAME"
chmod +x "$REPOSITORY/$JAR_NAME"

echo "> Load env from SSM Parameter Store: $PARAM_PATH (Region: $AWS_REGION)"

# SSM에서 파라미터를 읽어와 환경변수로 등록합니다.
while read -r name value; do
  key=$(basename "$name")
  export "$key=$value"
done < <(aws --region "$AWS_REGION" ssm get-parameters-by-path \
  --path "$PARAM_PATH" \
  --with-decryption \
  --recursive \
  --query "Parameters[*].[Name,Value]" \
  --output text)

# 필수 env 체크 (하나라도 없으면 여기서 스크립트가 중단됩니다)
: "${DB_URL:?DB_URL is required}"
: "${DB_USERNAME:?DB_USERNAME is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"
: "${JWT_SECRET:?JWT_SECRET is required}"

# 기본값 설정 및 숫자 체크
JWT_EXPIRATION_MS=${JWT_EXPIRATION_MS:-3600000}
case "$JWT_EXPIRATION_MS" in
  ''|[!0-9]*) echo "JWT_EXPIRATION_MS must be a number, got: '$JWT_EXPIRATION_MS'"; exit 1;;
esac

echo "> 애플리케이션 실행"
# 주의: 아래 변수들이 SSM에 없을 경우 빈 문자열로 주입될 수 있으니 확인이 필요했습니다.
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
  -jar "$REPOSITORY/$JAR_NAME" > "$REPOSITORY/nohup.out" 2>&1 &

echo "> Started. tail -n 50 $REPOSITORY/nohup.out"