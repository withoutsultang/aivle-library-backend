# 환경 설정·실행·테스트

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


## 테스트와 현재 제약

JDK 17에서 `test bootJar`를 실행해 기존 단위 테스트 7개와 패키징이 통과했습니다. MySQL·AWS·네이버 OAuth를 사용하는 전체 서비스 기동과 통합 흐름은 별도 환경에서 확인해야 합니다.
