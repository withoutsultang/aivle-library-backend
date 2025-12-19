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

# Globbing을 사용하여 파일 목록을 안전하게 가져옴
LATEST_JAR=""
LATEST_TIME=0

# for loop와 glob을 사용하여 파일명에 특수문자가 있어도 안전하게 처리
for jar in "$REPOSITORY"/*.jar; do
    # 파일이 존재하지 않는 경우(glob 실패) 대비
    [ -e "$jar" ] || continue

    # plain.jar는 실행 파일이 아니므로 제외
    if [[ "$jar" != *"plain.jar"* ]]; then
        # 가장 최근에 수정된 파일을 찾음
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

echo "> $LATEST_JAR 실행"
# 로그와 실행 경로에 모두 절대 경로 사용
nohup java -jar "$LATEST_JAR" > "$REPOSITORY/nohup.out" 2>&1 &