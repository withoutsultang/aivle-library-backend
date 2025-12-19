#!/bin/bash
# scripts/start.sh

# jar 파일 이름을 정확히 지정하거나 와일드카드 사용
APP_NAME=$(ls /home/ubuntu/app/backend-0.0.1-SNAPSHOT.jar)
REPOSITORY=/home/ubuntu/app

echo "> 서비스 실행 시작"
cd $REPOSITORY

# 기존 프로세스 종료
PID=$(lsof -ti:8080 || true)
if [ -n "$PID" ]; then
  kill -15 "$PID"
  sleep 5
fi

# 실행 (nohup.out도 app 폴더에 생성)
nohup java -jar $APP_NAME > $REPOSITORY/nohup.out 2>&1 &

echo "> 서비스 실행 완료"