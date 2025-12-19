#!/bin/bash

echo "> 서비스 상태 확인을 위해 20초 대기..."
sleep 20

# localhost:8080에 요청을 보내 HTTP 상태 코드를 가져옵니다.
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/)

echo "> HTTP 응답 코드: $HTTP_CODE"

# 200(성공) 또는 404(페이지 없음)가 나오면 서버는 일단 떠 있는 것으로 간주합니다.
# 000(접속 실패)이 아닐 경우 성공으로 처리합니다.
if [ "$HTTP_CODE" -ge 200 ] && [ "$HTTP_CODE" -lt 500 ]; then
    echo "> 서버 응답 확인 완료 (성공)"
    exit 0
else
    echo "> [ERROR] 서버 응답 없음 또는 에러 (HTTP 코드: $HTTP_CODE)"
    exit 1
fi