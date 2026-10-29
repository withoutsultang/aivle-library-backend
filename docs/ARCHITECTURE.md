# 설계 포인트

## API와 데이터 경계

Controller는 요청과 응답 계약을, Service는 사용자·도서·본문 작업과 권한 확인을, Repository는 데이터 접근을 담당합니다.
Book과 BookDetail을 1:1로 분리해 목록 메타데이터와 본문 조회의 목적을 구분했습니다. DTO는 Entity와 API 응답 사이의 계약입니다.

## 인증과 작성자 권한

네이버 callback에서 사용자 정보를 저장하고 JWT를 발급합니다. JwtFilter가 subject의 사용자 ID를 principal로 전달하고, 도서 Service가 작성자 ID와 비교한 후 수정·삭제를 수행합니다. health와 preflight는 인증 토큰에 독립된 경로로 처리합니다.

## 표지 저장과 조회

DB에는 S3 object key를 저장하고 응답 단계에서 서명 URL을 만듭니다. 수정 요청의 URL이 현재 key에 대응하면 그대로 유지합니다. 새 표지를 저장한 뒤 이전 표지 정리 순서로 처리하고, 공통 기본 표지는 삭제 대상에서 제외합니다. 다운로드는 설정한 HTTPS 출처, 제한 시간과 최대 10 MiB 범위를 사용합니다.

## 조회수와 읽기 모델

DynamoDB 원자적 UpdateItem으로 조회수를 증가시키고, 스케줄러는 Scan paginator로 모든 페이지를 순회해 MySQL 인기 정렬의 기준 값을 갱신합니다. 리전·테이블·동기화 주기는 외부 설정으로 지정할 수 있습니다.

## 배포 흐름

```mermaid
flowchart LR
    Code[Git Source] --> Test[Unit Test]
    Test --> Build[Gradle bootJar]
    Build --> Review[Manual Approval]
    Review --> Deploy[CodeDeploy / EC2]
    SSM[Parameter Store] --> Start[start.sh]
    Deploy --> Start
    Start --> Health[HTTP 200 readiness]
```

팀 프로젝트에서 사용한 AWS 배포 흐름을 기준으로 배포 스크립트와 명세를 보관합니다. 개인 저장소의 GitHub Actions는 테스트·패키징만 실행합니다. AWS 배포는 본인이 구성한 환경에서 별도로 실행합니다.


## 서버 기반 이미지 생성

인증된 사용자의 표지 요청은 `/api/v1/covers/generate`로 전달합니다. 백엔드는 서버 환경변수 `OPENAI_API_KEY`로 Images API를 호출하고 PNG Base64 결과를 반환합니다. 사용자가 선택한 표지는 PNG signature와 크기를 검사한 뒤 S3 key로 저장합니다. 모델은 `IMAGE_MODEL`로 설정하며 기본값은 `gpt-image-2.5-sunburst`입니다. 표준 크기는 `1024x1024`, `1024x1536`, `1536x1024`입니다.

기준: [OpenAI Images API 공식 문서](https://developers.openai.com/api/reference/resources/images/methods/generate), 2026-10-29 확인. 테스트는 API 계약과 데이터 처리를 검증하며 실제 유료 생성 요청은 실행하지 않습니다.
