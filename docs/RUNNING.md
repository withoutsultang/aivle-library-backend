# 환경 설정과 실행

## 테스트·빌드

JDK 17을 설치한 뒤 다음을 실행합니다. AWS 계정과 DB 없이 단위 테스트를 실행할 수 있습니다.

```bash
./gradlew test bootJar
```

Windows: `.\gradlew.bat test bootJar`. 산출물: `build/libs/app.jar`.

## API 서버

1. MySQL DB와 사용자 계정을 준비합니다.
2. [.env.example](../.env.example)의 변수들을 실행 환경에 주입합니다. 이 파일은 변수 목록이며 자동 로드되지 않습니다.
3. 처음 개발용 스키마를 생성할 때 `DB_DDL_AUTO=update`로 실행할 수 있습니다. 기본값은 기존 스키마 확인을 위한 `validate`입니다.
4. S3 버킷과 기본 표지 `ai-images/default-cover.png`, DynamoDB 테이블(숫자 파티션 키 `book_id`)을 구성합니다.
5. SDK 기본 자격증명 체인에 본인의 AWS 권한을 설정하고 네이버 OAuth callback을 등록합니다.
6. `./gradlew bootRun`으로 시작합니다. 기본 포트는 `8080`입니다.

```bash
curl http://localhost:8080/api/v1/health
```

## 외부 설정

|변수|용도|
|---|---|
|DB_URL, DB_USERNAME, DB_PASSWORD|MySQL 접속|
|S3_BUCKET_NAME, AWS_REGION|표지 스토리지·리전|
|DYNAMODB_TABLE|조회수 테이블|
|NAVER_CLIENT_ID, NAVER_CLIENT_SECRET, NAVER_REDIRECT_URI, NAVER_STATE|네이버 로그인 설정|
|JWT_SECRET, JWT_EXPIRATION_MS|32바이트 이상 서명 키·유효 시간|
|FRONTEND_BASE_URL, CORS_ALLOWED_ORIGINS|프론트 주소·허용 origin 목록|
|OPENAI_API_KEY, IMAGE_MODEL|서버 이미지 생성 API 설정|
|IMAGE_SOURCE_HOSTS|이미지 다운로드 허용 HTTPS 호스트, 쉼표 구분|

애플리케이션은 환경변수에서 설정을 받습니다. EC2 배포의 `scripts/start.sh`는 SSM 경로에서 설정을 읽어 같은 프로세스 환경에 전달합니다. 초기 예시 데이터가 필요한 개발 환경에서는 `SEED_DATA_ENABLED=true`를 설정합니다.

## 배포

EC2에 JDK 17, AWS CLI, jq, lsof를 준비합니다. `buildspec.yml`이 JAR·appspec·scripts를 구성하고 CodeDeploy가 시작 스크립트를 실행합니다. `ValidateService`는 `/api/v1/health`의 HTTP 200을 확인합니다. SSM 경로는 `SSM_PARAMETER_PATH`, 실행 디렉터리는 `APP_DIR`로 지정합니다. 실제 자격증명은 저장소에 커밋하지 않습니다.
