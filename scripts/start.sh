#!/bin/bash

# 1. 경로 설정
REPOSITORY=/home/ubuntu/app
PROJECT_NAME=backend # 본인의 프로젝트명으로 수정 가능

echo "> 현재 위치에서 .env 파일을 찾아 환경 변수를 설정합니다."
if [ -f "$REPOSITORY/.env" ]; then
    # .env 파일 내용을 환경 변수로 내보내기 (보안 유지)
    export $(grep -v '^#' $REPOSITORY/.env | xargs)
    echo "> 환경 변수 로드 완료"
else
    echo "> .env 파일이 없습니다. 경로를 확인해주세요."
fi

echo "> 현재 구동 중인 애플리케이션 PID 확인"
CURRENT_PID=$(lsof -ti:8080)

if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 구동 중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

echo "> 새 애플리케이션 배포"
# plain.jar를 제외한 진짜 실행 파일 선택
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | grep -v "plain" | tail -n 1)

echo "> JAR Name: $JAR_NAME"
chmod +x $JAR_NAME

echo "> $JAR_NAME 실행"
# 환경 변수가 적용된 상태로 백그라운드 실행
nohup java -jar $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &

echo "> 서비스 실행 완료"