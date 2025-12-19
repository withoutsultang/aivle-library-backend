#!/bin/bash

REPOSITORY=/home/ubuntu/app
PROJECT_NAME=backend

echo "> 현재 실행 중인 애플리케이션 PID 확인"
CURRENT_PID=$(pgrep -fl "$PROJECT_NAME" | grep jar | awk '{print $1}')

if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 실행 중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 "$CURRENT_PID"
    sleep 5
fi

echo "> 새 애플리케이션 배포 준비"

LATEST_JAR=""
LATEST_TIME=0

# 최신 JAR 파일 찾기
for jar in "$REPOSITORY"/*.jar; do
    [ -e "$jar" ] || continue

    if [[ "$jar" != *"plain.jar"* ]]; then
        MOD_TIME=$(stat -c %Y "$jar")
        if [ "$MOD_TIME" -gt "$LATEST_TIME" ]; then
            LATEST_TIME=$MOD_TIME
            LATEST_JAR=$jar
        fi
    fi
done

if [ -z "$LATEST_JAR" ]; then
    echo "> 에러: 실행 가능한 JAR 파일을 찾을 수 없습니다."
    exit 1
fi

echo "> 최신 JAR 파일: $LATEST_JAR"
chmod +x "$LATEST_JAR"

echo "> $LATEST_JAR 실행 (환경 변수 주입)"

# [중요] .bashrc의 변수를 명시적으로 로드 (혹시 모를 상황 대비)
source /home/ubuntu/.bashrc

# 단 한 번만 실행합니다.
# 시스템 프로퍼티(-D) 방식으로 주입하여 application.yml의 설정값을 대체합니다.
nohup java \
    -Djwt.secret="$JWT_SECRET" \
    -Djwt.expiration-ms="$JWT_EXPIRATION_MS" \
    -Dnaver.client-id="$NAVER_CLIENT_ID" \
    -Dnaver.client-secret="$NAVER_CLIENT_SECRET" \
    -Dnaver.redirect-uri="$NAVER_REDIRECT_URI" \
    -Dnaver.state="$NAVER_STATE" \
    -jar "$LATEST_JAR" > "$REPOSITORY/nohup.out" 2>&1 &

echo "> 배포 완료"