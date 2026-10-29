# 김건우 · 4차 개발과 5차 AWS 기여

**4차: 백엔드 기능·API 계약·인증/인가 개선 → 5차: AWS 구축·환경 설정·배포 전반**

## 4차 · 로컬 플랫폼 개발

사용자·도서·본문을 API로 연결하고 요청 검증·응답 DTO·작성자 권한을 다뤘습니다.

|주제|직접 구현·개선한 내용|공개 커밋|
|---|---|---|
|도서 목록·DTO|Entity→응답 DTO 매핑, 목록 API 계약 구성|[a50f582](https://github.com/withoutsultang/aivle-library-backend/commit/a50f582e6840b497c49ae40568137ffc6bf1b8ce)|
|도서 등록 API|Controller·Service에서 사용자·도서·본문 연결|[14e10bc](https://github.com/withoutsultang/aivle-library-backend/commit/14e10bccb41f032890fc4a407e071ebe7c7ecf59)|
|Validation|검증 오류 메시지와 HTTP 400 공통 응답|[99c06c1](https://github.com/withoutsultang/aivle-library-backend/commit/99c06c1e8b99197c478163d3cba58fc4b17cb3f3)|
|인기 도서 조회|조회수 내림차순·상위 N개·페이지 요청|[babecdd](https://github.com/withoutsultang/aivle-library-backend/commit/babecdd082ccaf634df20bd3b6d823d6ce37ded5)|
|좋아요 데이터 모델|BookLike 관계·유니크 제약·Repository·DTO|[89f3cf9](https://github.com/withoutsultang/aivle-library-backend/commit/89f3cf9e58c37e29e49042389dff4cb96be11545)|
|JWT·작성자 인가|사용자 principal 전달과 수정·삭제 권한 연결|[3499a29](https://github.com/withoutsultang/aivle-library-backend/commit/3499a29c6fb570381627146a0e2b0ad6857efe17)|

## 5차 · AWS 인프라·환경 설정·배포 전반

**담당 범위: AWS 전반의 구축·환경 설정·배포. 수행 방식: 주요 구성을 직접 작업하고 일부 항목은 팀원과 공동 수행.**

- **웹 전달·실행 환경:** S3·CloudFront 프론트 배포와 ALB·EC2·ASG API 실행 환경 연결
- **네트워크·권한:** VPC 기반 네트워크와 배포·실행에 필요한 AWS 권한 구성
- **데이터·설정:** RDS MySQL 연결 전환, S3·DynamoDB 팀 구현 연동, Parameter Store 설정 주입
- **CI/CD:** CodePipeline·CodeBuild·CodeDeploy, 프론트/백엔드 산출물, 수동 승인·SNS 연결
- **배포 후 연결·관측:** OAuth redirect·CORS·API 경로·health 검사 조정, CloudWatch 운영 환경을 팀원과 함께 구성

**개발 코드가 AWS에서 실행되기 위한 설정·라우팅·저장·배포 경로를 함께 연결한 역할**입니다. 전체 담당 범위와 코드상 대표 작업을 함께 정리했습니다.

|대표 작업|구현·변경 내용|공개 커밋|
|---|---|---|
|빌드·테스트 명세|Java corretto17, `clean test build`, JAR artifact와 테스트 환경 설정 작성|[36d9432](https://github.com/withoutsultang/aivle-library-backend/commit/36d943279d91b985d3ac33a847e8fda39c8be102)|
|배포 상태 검사|health Controller·검사 URL·Spring Security 허용 경로 정렬|[b9d18af](https://github.com/withoutsultang/aivle-library-backend/commit/b9d18af415e7e684267a11e6a26de54f930a6c56)|
|OAuth 배포 주소|로그인 callback의 frontend URL 외부화|[b20b668](https://github.com/withoutsultang/aivle-library-backend/commit/b20b6682331931aebd735e4ad1b52ad02d69adcd)|
|CloudFront CORS|CloudFront 배포 origin에 맞춰 허용 출처 조정|[f575a70](https://github.com/withoutsultang/aivle-library-backend/commit/f575a70f1144b77dd2e7d237cd419fffa06b56e3)|
|ALB CORS|ALB 연결 환경에 맞춰 허용 출처 조정|[95f1713](https://github.com/withoutsultang/aivle-library-backend/commit/95f1713436579409db2febf65f614315de8c9d2a)|
|RDS 연결 전환|H2 runtime에서 MySQL connector·환경변수 datasource로 전환|[81c352b](https://github.com/withoutsultang/aivle-library-backend/commit/81c352b6fcb1131564b09a3c56183a558fafc666)|
|SSM·실행 파일|Parameter Store 읽기·필수 변수 검사·Java 설정 주입, `app.jar` 고정|[13d078c](https://github.com/withoutsultang/aivle-library-backend/commit/13d078cf8be52d26e08877b44cb2c43547e67a89)|

상세 구성은 [AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md), 전체 구조는 [ARCHITECTURE.md](ARCHITECTURE.md)에서 볼 수 있습니다.

## 협업과 서비스 결과

네이버 로그인·AI 표지·도서 작성·탐색·본문 화면을 갖춘 모두의 서재를 팀으로 개발하고 AWS 배포 환경으로 확장했습니다. 백엔드 API 개발과 AWS 환경 연결을 맡아 프론트엔드·데이터·배포 사이의 접점을 다뤘습니다. 화면 구현, S3·DynamoDB 기능 코드, 모니터링 등에는 팀원의 구현과 공동 작업이 함께 포함됩니다.

## 개인 공개본 고도화 · 2026년 10월

도서 표지의 key·서명 URL 처리, HTTP 201, CORS·상태 검사, DynamoDB 페이지 순회와 서버 AI 표지 API를 개선했습니다. 백엔드 26개 테스트·패키징과 GitHub Actions를 구성했습니다. 이는 4·5차 당시 수행 내용과 구분한 후속 개선입니다.

- [검증 기록](VERIFICATION.md)
- [팀 원본·공개본의 커밋 대응](history-commit-map.tsv)
