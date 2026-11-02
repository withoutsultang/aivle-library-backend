# 검증 기록

## 2026-11-03 · 로컬 데모와 실제 API·SQL 통합 검증

|검증|확인 결과|
|---|---|
|Playwright Chromium 브라우저 E2E|**6개 통과** · 등록·수정·본문·권한·입력 검증·삭제|
|Spring·Security·JPA·H2 통합 테스트|**3개 통과** · 관계형 행 생성·변경·삭제와 거부 요청의 원문 유지|
|기존 단위·컴포넌트 테스트|백엔드 **26개**, 프론트 **10개** 통과|
|데모 분리|운영 JAR의 데모·DB 관측·H2·Mockito 제외, 운영 프론트의 데모 세션·표지 UI 제외 검사|
|시연 실행|등록·수정 SQL 확인을 포함한 **1개 녹화 시나리오** 통과, 실제 화면 8장·WebM·MP4·GIF|

상세 실행과 범위는 [DEMO.md](DEMO.md)에서 확인합니다. 브라우저의 CRUD 응답은 가짜 응답으로 대체하지 않으며, 독립된 SQL 조회로 저장 결과를 확인합니다. 네이버·OpenAI·AWS는 로컬 어댑터로 분리하고 H2를 사용한 검증이며, 운영 클라우드 검증과 구분합니다.

---


## 2026-11-02 · AWS 인프라 코드 검증

Terraform 1.13.5·AWS provider 6.9.0 기준으로 `fmt -check`·`validate`와 모의 제공자 테스트 **4개 시나리오**를 실행했습니다. 비공개 ALB·DB, 2 AZ 서브넷, S3 공개 차단, RDS 관리형 비밀·암호화·삭제 보호, 앱 포트·health·DynamoDB 키 계약, API·정적 캐시, OAuth callback과 배포 단계·용량 검증을 포함합니다.

추가로 SecureString 설정 도구의 Python 테스트 **4개**, SPA·OAuth·정적 자산·API 경로 **8개 입력**, 실행 시 비밀번호 주입·읽기 실패 중단 **2개 경로**를 테스트했습니다. 시작 스크립트·EC2 bootstrap의 Bash 문법도 확인했습니다. [Infrastructure workflow](../.github/workflows/infrastructure.yml)는 이 검증을 AWS 인증 없이 실행합니다.

이 결과는 코드·계약 검증입니다. 실제 AWS 리소스 생성, 계정별 quota·권한, AMI bootstrap·OAuth·업무 여정은 실행 가이드에 따라 별도로 검증합니다. 5차 당시의 배포 결과와 이번 개인 고도화 테스트를 구분합니다.

---

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
