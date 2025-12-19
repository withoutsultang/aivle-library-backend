#!/bin/bash
JAR_NAME=$(ls -tr /home/ec2-user/app/*.jar | tail -1)
echo "> 실행할 JAR 파일: $JAR_NAME"

# 기존에 8080 포트를 쓰는 프로세스가 있다면 종료
CURRENT_PID=$(lsof -ti:8080)
if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 구동 중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

# 새 버전 실행
nohup java -jar $JAR_NAME > /home/ec2-user/app/nohup.out 2>&1 &