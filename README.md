# 모두의 서재 — Backend

누구나 글을 집필하고 도서 형태로 공개할 수 있는 창작 플랫폼의 백엔드입니다. **KT AIVLE 팀 프로젝트**를 개인 포트폴리오 검토용으로 정리한 독립 저장소이며, 팀 전체의 결과물을 개인 단독 성과로 주장하지 않습니다.

- 원본: [KT-AIVLE-School-4th-Mini-Project/BackEnd](https://github.com/KT-AIVLE-School-4th-Mini-Project/BackEnd)
- 프론트엔드: [aivle-library-frontend](https://github.com/withoutsultang/aivle-library-frontend)
- 기준 브랜치: `dev`

## 목적과 주요 기능

도서 등록·목록·상세·수정·삭제, 정렬·페이지네이션, 관심 도서, 마이페이지와 네이버 로그인/JWT 인증을 제공합니다. 표지 파일은 S3를 이용하고, 조회수 처리에는 DynamoDB와 동기화 스케줄러가 포함됩니다. 외부 서비스의 실동작은 이번 검증 범위에 포함하지 않았습니다.

## 기술 스택

Java 17, Spring Boot 3.5.8, Spring Security, Spring Data JPA, JWT, Gradle Wrapper 9.2.1을 사용합니다. 기본 실행 DB는 **MySQL**이며 H2는 테스트 의존성입니다. AWS S3·DynamoDB·Parameter Store 연동 코드가 포함됩니다. 정확한 의존성과 버전은 `build.gradle`을 기준으로 합니다.

## 구조

```text
src/main/java/com/aivle8/backend/
  controller/       API 요청
  service/          도서·인증·마이페이지·스토리지 로직
  repository/       데이터 접근
  domain/           도서·본문·사용자·관심 도서 엔티티
  dto/              요청·응답
  security/         인증 필터·접근 제어
  config/           AWS·웹·초기 데이터 설정
  scheduler/        조회수 동기화
  global/           공통 응답·예외 처리
src/main/resources/application.yaml
src/test/           기존 단위 테스트·테스트 설정
scripts/            원본 배포 스크립트
```

## 실행

```sh
git clone https://github.com/withoutsultang/aivle-library-backend.git
cd aivle-library-backend
git switch dev
```

JDK 17과 접근 가능한 MySQL, 본인이 관리하는 AWS 리소스·권한, 네이버 OAuth 애플리케이션이 필요합니다. 실제 자격증명은 저장소에 넣지 말고 운영체제 환경변수나 본인의 비밀 관리 도구로 주입합니다. `.env.example`은 필요한 변수 목록이며 자동 로드 파일이 아닙니다.

필요한 변수는 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `S3_BUCKET_NAME`, `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`, `NAVER_REDIRECT_URI`, `NAVER_STATE`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `FRONTEND_BASE_URL`, `FRONTEND_REDIRECT_PATH`입니다. AWS 인증은 SDK 기본 자격증명 체인을 사용합니다.

기본 설정은 AWS Parameter Store의 `/portfolio/library/config/`를 가져옵니다. 원본 `scripts/start.sh`는 별도로 `/portfolio/library/runtime/`에서 값을 읽으므로 두 경로를 같은 것으로 가정하면 안 됩니다. DynamoDB 클라이언트와 테이블 사용에는 원본의 `us-west-1` 및 `PortfolioBookViews` 설정이 남아 있습니다. 서버 주소·S3 URL·DynamoDB 테이블 이름·Parameter Store 경로는 공개용 예시값으로 치환했습니다. `config/WebConfig.java`, `security/SecurityConfig.java`, `global/util/ImageUrlUtil.java`, `scheduler/ViewCountSyncScheduler.java`, `service/BookServiceImpl.java`, `application.yaml`, `scripts/start.sh`의 예시 설정을 본인 리소스에 맞춘 환경에서 실행해야 합니다. 원본 조직의 인프라를 이용할 권한이 있다고 가정하지 마세요.

```sh
# macOS/Linux
./gradlew test bootJar
./gradlew bootRun

# Windows PowerShell
.\gradlew.bat test bootJar
.\gradlew.bat bootRun
```

API는 기본 포트 `8080`을 사용하며 빌드 결과는 `build/libs/app.jar`입니다. `bootRun`은 외부 DB·AWS·OAuth 설정 완료 후 사용하는 명령입니다. 이번 작업에서는 단위 테스트와 패키징을 검증했고, 외부 서비스 연결과 서버 기동·로그인·저장 흐름은 검증하지 않았습니다. 초기 데이터 등록 코드가 있으므로 실서비스 DB를 검증용으로 사용하지 마세요.

## 개인 기여의 근거

원본의 `withoutsultang` 작성 커밋에서 확인되는 변경 이력은 다음과 같습니다. 아래는 해당 변경에 참여했다는 근거이며 기능 전체의 단독 개발이나 성능 개선 수치를 의미하지 않습니다.

| 원본 커밋 | 확인되는 변경 |
| --- | --- |
| `7b8e43f` | 도서 등록 Controller·Service 변경 |
| `574cea8` | 유효성 검사 예외 처리 추가 |
| `db31610` | 조회수 기준 도서 정렬 변경 |
| `d119ca8` | 관심 도서 엔티티·저장소 및 응답 DTO 변경 |
| `022e764` | JWT 필터와 도서 API 응답 처리 변경 |
| `ccbb714` | Parameter Store 환경변수 주입 관련 빌드·배포 스크립트 변경 |

커밋 식별자는 원본 기준입니다. 정리본의 대응 커밋은 `docs/history-commit-map.tsv`에서 확인할 수 있습니다. 담당 범위의 최종 설명과 정량 성과는 본인 확인 후 보완할 수 있습니다.

## 기록·공개 상태

원본 브랜치 7개와 커밋 190개의 연결 관계를 보존했습니다. 과거 고정 DB 비밀번호 3개를 환경변수로 치환하고, 현재 파일과 전체 기록의 원본 서버 주소·AWS 식별자를 예시값으로 가렸습니다. 변경된 과거 파일 객체는 총 31개입니다. 백엔드 커밋 해시 190개가 바뀌었고 기존 서명 40개는 제거됐습니다. 작성자·커미터·날짜·메시지·부모 순서는 보존했습니다. 태그는 원본에 없었습니다.

2026-10-27 검증: `test bootJar` 성공, 기존 테스트 7개 통과. 전체 Git 객체와 커밋 메타데이터를 별도 패턴으로 검사했고 Gitleaks 추가 검사도 수행했습니다. 자동 검사는 알려지지 않은 비밀정보의 부재를 보증하지 않습니다. 과거 DB 비밀번호가 실제 자격증명이었는지 불명확하므로 민감값으로 처리했으며, 실제 사용 이력이 있다면 폐기·재발급이 필요합니다.

교육기관·팀 및 이미지·데이터·작성자 정보의 공개 허용 여부는 저장소 소유자의 확인을 근거로 기록했습니다. 원본에 프로젝트 LICENSE가 없어 임의로 새 라이선스를 부여하지 않았습니다. 제3자 의존성·외부 이미지·서비스에는 각각의 조건이 적용됩니다.

이 저장소는 비공개 검토본으로 준비됐습니다. Public 전환은 별도 검토·승인 후 진행합니다. Git 기록 복사에 Issues, PR 대화·리뷰, Actions 실행 기록·비밀값·설정, 배포 리소스는 포함되지 않습니다.
