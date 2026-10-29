# 검증 기록

2026-10-29 · 개인 공개본 고도화 검증

## 테스트와 패키징

`./gradlew test bootJar` 성공 · **26개 테스트 통과** · `build/libs/app.jar` 생성.
로컬에서는 캐시된 의존성으로 같은 작업을 `--offline` 옵션과 함께 실행했습니다.

|검증 영역|테스트 수|확인한 동작|
|---|---:|---|
|기존 도메인·요청 검증·클래스 로드|7|조회수·생성/수정 시간·필수 값·요약 길이|
|도서 변경 흐름|5|작성자 인가, HTTP 201, 기존 표지 유지, 교체 업로드 순서·실패 보존|
|표지 저장|6|서명 URL, 동일 이미지 식별, 기본 표지 보호, 출처 제한, PNG 저장·입력 검증|
|AI 표지 요청 계약|4|서버 요청 형식, PNG 응답, 서비스 설정·공급자 오류 처리|
|인증·CORS|3|공개 health 경로, 보호 경로, JSON·Bearer 요청 허용 origin|
|조회수 동기화|1|DynamoDB 여러 페이지의 도서 조회수 반영|

외부 공급자는 MockRestServiceServer·Mockito로 대체해 반복 가능한 테스트로 구성했습니다. 실제 유료 이미지 생성이나 AWS·네이버 운영 환경 호출은 이 검증에 포함하지 않습니다.

## 배포 스크립트

시작·상태 검사 스크립트의 Bash 문법을 확인했습니다. 상태 검사에는 HTTP 200/500 fixture를 적용해 **200일 때 성공, 500일 때 10회 후 실패**하는 동작을 확인했습니다.

## 자동 검사

[CI workflow](../.github/workflows/ci.yml)는 push·pull request에서 Java 17 테스트와 JAR 패키징을 실행합니다.
[실행 기록](https://github.com/withoutsultang/aivle-library-backend/actions/workflows/ci.yml)
