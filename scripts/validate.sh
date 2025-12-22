#!/bin/bash

echo "> 서비스 상태 확인 시작 (경로: /api/v1/health)"

# 최대 12번, 5초 간격으로 시도 (총 60초 대기 가능)
for retry_count in {1..12}
do
    echo "> 접속 시도 ($retry_count/12)..."

    # 사용자님이 지정하신 경로로 요청을 보냈습니다.
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/health || true)

    echo "> 현재 응답 코드: $HTTP_CODE"

    # 200(OK)이나 404(Not Found) 등, 서버가 응답을 주는 상태라면 프로세스는 살아있는 것이었습니다.
    if [ "$HTTP_CODE" -ne 000 ]; then
        echo "> [성공] 서버가 응답합니다! 배포를 완료합니다."
        exit 0
    fi

    echo "> 아직 서버가 준비되지 않았습니다. 5초 후 다시 시도합니다."
    sleep 5
done

echo "> [실패] 60초 동안 서버 응답이 없어 배포를 중단합니다."
exit 1