#!/bin/bash
REPOSITORY=/home/ubuntu/app

echo "> .env 파일을 시스템 환경 변수로 등록합니다."
if [ -f "$REPOSITORY/.env" ]; then
    # 주석 제외, 빈 줄 제외하고 export
    export $(grep -v '^#' $REPOSITORY/.env | xargs)
    echo "> 환경 변수 로드 완료"
else
    echo "> [경고] .env 파일이 없습니다!"
fi

# 기존 프로세스 종료
CURRENT_PID=$(lsof -ti:8080)
if [ -n "$CURRENT_PID" ]; then
    kill -15 $CURRENT_PID
    sleep 5
fi

# JAR 실행
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | grep -v "plain" | tail -n 1)
nohup java -jar $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &