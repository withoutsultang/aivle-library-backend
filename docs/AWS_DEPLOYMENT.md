# 5차 · AWS 배포와 환경 구성

## 프로젝트에서 맡은 역할

**김건우는 5차의 AWS 인프라 구성·환경 설정·배포 전반을 담당했습니다.** 네트워크와 실행 환경, 프론트·백엔드 파이프라인, 데이터·설정 연결을 폭넓게 작업했고 일부 항목은 팀원과 공동 수행했습니다.

4차의 목표가 로컬에서 플랫폼 기능을 만드는 것이었다면, 5차는 그 기능을 AWS의 웹 전달·애플리케이션 실행·영속 저장·설정 관리·배포 체계로 연결하는 단계였습니다.

## 서비스 아키텍처

![5차 AWS 서비스 구성](assets/phase-05-aws.png)

### 1. 웹 전달과 API 연결

프론트 빌드 산출물은 S3에 배포하고 CloudFront를 통해 전달하는 구조입니다. CloudFront의 정적 경로는 S3 origin, `/api/v1/*`는 ALB origin으로 연결하며 API behavior는 캐싱 비활성으로 구성했습니다. 클라이언트는 상대 API 경로를 사용하고, 배포 주소에 맞춰 CORS와 네이버 OAuth의 frontend redirect 설정을 조정했습니다.

Vite proxy는 로컬 개발 연결을 담당하고, AWS에서는 CloudFront behavior가 API를 전달합니다. 개발 도구 설정과 배포 시 라우팅의 역할을 구분해 연결한 작업입니다.

### 2. 네트워크·실행·권한

VPC의 두 가용 영역에 걸친 public/private 서브넷 구성과 EC2·ALB·Auto Scaling Group 기반 실행 환경을 다뤘습니다. ALB Target Group은 API 포트와 health 경로로 애플리케이션을 연결하고, ASG는 시작 템플릿 기반으로 인스턴스를 관리합니다. 배포 서비스의 IAM 역할과 실행 환경의 접근 권한도 AWS 환경 구성의 일부로 다뤘습니다.

### 3. 데이터와 설정 외부화

로컬 H2 중심의 실행 설정을 RDS MySQL datasource로 바꾸고 DB 접속 정보를 환경변수로 분리했습니다. 표지 객체는 S3, 조회수는 DynamoDB에 저장하는 팀 구현과 백엔드를 연결했습니다. 앱의 DB·JWT·OAuth·프론트 주소 등 실행 설정은 Parameter Store를 읽는 시작 스크립트에서 주입하도록 구성했습니다.

### 4. 빌드·승인·배포

![5차 AWS CI/CD](assets/phase-05-pipeline.png)

|경로|단계와 산출물|
|---|---|
|Frontend|Source → CodeBuild 테스트 → CodeBuild 빌드 → Manual Approval → S3 Deploy|
|Backend|Source → CodeBuild 테스트 → CodeBuild 빌드 → Manual Approval → CodeDeploy → EC2 배포 그룹|

Java 17 빌드·테스트·JAR 산출물 명세를 작성하고, 배포 스크립트가 참조할 실행 파일명을 `app.jar`로 고정했습니다. 팀 파이프라인 자료에는 분리된 UnitTest·Build 단계와 승인·배포 화면이 남아 있습니다. 프론트는 `dist` 산출물, 백엔드는 JAR·appspec·hook 스크립트를 배포 대상으로 연결했습니다.

### 5. 상태 검사와 운영 관측

배포 검사의 `/api/v1/health`, 애플리케이션 Controller, Spring Security 허용 경로를 맞추는 작업을 수행했습니다. AWS에서는 ALB 대상 상태, ASG 인스턴스 활동과 CloudWatch Agent·지표·대시보드·로그를 함께 확인하는 환경을 구성했습니다. 모니터링을 포함한 일부 작업은 담당 팀원과 협업했습니다. 수동 승인 단계에는 SNS를 연결했습니다.

## 배포 작업의 코드 근거

AWS 전반 담당 범위는 프로젝트 수행자의 설명을 반영했으며, 아래는 그중 저장소 커밋으로 직접 살펴볼 수 있는 대표 작업입니다.

|작업|구현·조정 내용|공개 커밋|
|---|---|---|
|빌드·테스트 명세|Java corretto17, `clean test build`, JAR artifact와 테스트 환경 설정 작성|[36d9432](https://github.com/withoutsultang/aivle-library-backend/commit/36d943279d91b985d3ac33a847e8fda39c8be102)|
|배포 상태 검사|health Controller·검사 URL·Spring Security 허용 경로 정렬|[b9d18af](https://github.com/withoutsultang/aivle-library-backend/commit/b9d18af415e7e684267a11e6a26de54f930a6c56)|
|OAuth 배포 주소|로그인 callback의 frontend URL 외부화|[b20b668](https://github.com/withoutsultang/aivle-library-backend/commit/b20b6682331931aebd735e4ad1b52ad02d69adcd)|
|CloudFront CORS|CloudFront 배포 origin에 맞춰 허용 출처 조정|[f575a70](https://github.com/withoutsultang/aivle-library-backend/commit/f575a70f1144b77dd2e7d237cd419fffa06b56e3)|
|ALB CORS|ALB 연결 환경에 맞춰 허용 출처 조정|[95f1713](https://github.com/withoutsultang/aivle-library-backend/commit/95f1713436579409db2febf65f614315de8c9d2a)|
|RDS 연결 전환|H2 runtime에서 MySQL connector·환경변수 datasource로 전환|[81c352b](https://github.com/withoutsultang/aivle-library-backend/commit/81c352b6fcb1131564b09a3c56183a558fafc666)|
|SSM·실행 파일|Parameter Store 읽기·필수 변수 검사·Java 설정 주입, `app.jar` 고정|[13d078c](https://github.com/withoutsultang/aivle-library-backend/commit/13d078cf8be52d26e08877b44cb2c43547e67a89)|

## 아키텍처 자료의 기준

서비스 구성도와 파이프라인 이미지는 5차의 팀 코드·콘솔 캡처·발표 자료를 재구성했습니다. CloudFront origins/behaviors, S3 산출물·표지 객체, ALB Target Group, VPC·ASG, Parameter Store, front/back pipeline, CloudWatch·SNS 화면을 근거로 사용했습니다. 계정 ID·실제 도메인·구독 주소·접속 정보는 이미지에 넣지 않았습니다.

이 문서의 5차 구성과 현재 개인 저장소의 고도화는 시점을 구분합니다. 현재 GitHub Actions는 테스트·빌드를 수행하며, 2026년 10월의 서버 기반 AI 표지·HTTP 200 상태 검사·DynamoDB 페이지 처리 개선은 [검증 기록](VERIFICATION.md)에 정리했습니다.

PNG는 README 표시용, 같은 이름의 SVG는 편집 가능한 원본입니다. 다이어그램의 인스턴스 수는 구조 예시입니다.
