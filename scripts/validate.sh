#!/bin/bash
echo "> 서비스 확인을 위해 15초 대기..."
sleep 15 # 서버가 부팅될 시간을 충분히 줍니다

# 8080 포트가 정상적으로 열렸는지 확인
curl -s localhost:8080/actuator/health | grep "UP"