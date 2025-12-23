#!/bin/bash
set -euo pipefail

REPOSITORY=/home/ubuntu/app
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)
PARAM_PATH="/portfolio/library/runtime/"  # 마지막에 /를 붙여주는 것이 인식에 더 좋았습니다.
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

# JAR 파일 체크
if [ -z "$JAR_NAME" ] || [ ! -f "$JAR_NAME" ]; then
  echo "> 에러: JAR 파일을 찾을 수 없습니다."
  exit 1
fi

echo "> 실행 파일: $JAR_NAME"
chmod +x "$JAR_NAME"

echo "> Load env from SSM Parameter Store: $PARAM_PATH"

# 수정 포인트 1: IFS를 사용하여 탭(tab) 구분자를 정확히 인식하도록 변경했습니다.
# 수정 포인트 2: export를 루프 안에서 확실히 수행합니다.
while IFS=$'\t' read -r name value; do
  if [ -n "$name" ]; then
    key=$(basename "$name")
    export "$key"="$value"
    echo "> 로드 완료: $key" # 보안상 값은 출력하지 않고 키 이름만 확인했습니다.
  fi
done < <(aws ssm get-parameters-by-path \
  --region "$AWS_REGION" \
  --path "$PARAM_PATH" \
  --with-decryption \
  --recursive \
  --query "Parameters[*].[Name,Value]" \
  --output text)

# 필수 env 체크 (에러 발생 시 여기서 스크립트가 중단되어 원인을 알 수 있었습니다.)
: "${DB_URL:?DB_URL is required}"
: "${DB_USERNAME:?DB_USERNAME is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"
: "${JWT_SECRET:?JWT_SECRET is required}"
: "${S3_BUCKET_NAME:?S3_BUCKET_NAME is required in SSM}"

echo "> 애플리케이션 실행"
# 수정 포인트 3: yml 구조에 맞게 하이픈(-)과 점(.) 경로를 다시 확인했습니다.
nohup java \
  -Dspring.datasource.url="$DB_URL" \
  -Dspring.datasource.username="$DB_USERNAME" \
  -Dspring.datasource.password="$DB_PASSWORD" \
  -Djwt.secret="$JWT_SECRET" \
  -Djwt.expiration-ms="${JWT_EXPIRATION_MS:-3600000}" \
  -Dcloud.aws.s3.bucket="${S3_BUCKET_NAME}" \
  -Dnaver.client-id="${NAVER_CLIENT_ID:-}" \
  -Dnaver.client-secret="${NAVER_CLIENT_SECRET:-}" \
  -Dnaver.redirect-uri="${NAVER_REDIRECT_URI:-}" \
  -Dnaver.state="${NAVER_STATE:-}" \
  -Dapp.frontend-base-url="${FRONTEND_BASE_URL:-}" \
  -jar "$JAR_NAME" > "$REPOSITORY/nohup.out" 2>&1 &

echo "> 배포가 완료되었습니다."