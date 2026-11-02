# 모두의 서재 · 실제 API·SQL로 체험하는 로컬 데모

**2026-11-03 개인 공개본 고도화.** 예시 계정·도서·표지로 현재 React 화면을 실행하고, 실제 Spring Security·Controller·Service·JPA·SQL DB를 연결한 시연을 추가했습니다.

![로컬 데모 실제 실행 미리보기](assets/demo/showcase-preview.gif)

[화면 갤러리·시연 영상·실행 절차](https://github.com/withoutsultang/aivle-library-frontend/blob/dev/docs/DEMO.md)

## 백엔드 실행

Java 17을 사용합니다.

```bash
./gradlew demoRun
```

Windows: `.\gradlew.bat demoRun`. 서버는 `127.0.0.1:18080`에만 바인딩됩니다. 프론트 저장소를 나란히 배치하고 `npm run dev:demo`를 실행하면 `http://127.0.0.1:15173`에서 체험합니다.

## 실제 처리와 데모 연결

|실제 서비스 코드로 실행|로컬 데모 어댑터|
|---|---|
|Controller·Service·DTO 검증·JWT 필터·작성자 인가|네이버 대신 예시 계정으로 로컬 세션 발급|
|JPA 엔티티·Repository·SQL 저장·조회·변경·삭제|MySQL/RDS 대신 실제 H2 메모리 SQL DB|
|표지 PNG 검증·key 저장·기존 표지 유지 흐름|OpenAI·S3 대신 예시 PNG와 메모리 객체 저장|
|본문·도서·사용자의 관계 저장|DynamoDB 호출은 로컬 테스트 대역|

`src/demo`는 별도의 소스 집합입니다. 운영용 JAR에는 예시 로그인·표지 공급자·SQL 관측 경로·H2·Mockito를 넣지 않습니다. 데모 전용 설정과 생성된 서명 키는 실행 중에만 사용합니다.

## Spring·SQL 통합 검증

```bash
./gradlew test integrationTest verifyProductionJar
```

기존 단위 테스트 26개와 별도로 실제 Spring 애플리케이션 문맥·보안 필터·JPA·H2를 띄워 3개 통합 테스트를 실행합니다.

1. HTTP 201 등록 → SQL 행 확인 → 수정된 본문 확인 → 도서·본문 행 삭제 확인
2. 타인의 수정 요청 403 → SQL 원문 유지
3. 잘못된 토큰 401·잘못된 DTO 400 → DB 행 수 유지

브라우저에서는 같은 백엔드와 프론트 빌드를 연결해 6개 전체 흐름을 검사합니다. [브라우저 테스트와 결과](https://github.com/withoutsultang/aivle-library-frontend/blob/dev/docs/DEMO.md)

운영 JAR 검사는 데모 클래스와 H2·Mockito가 들어가면 실패합니다. 네이버·유료 이미지·AWS 리소스의 실제 운영 호출 없이 반복 가능한 검증 경로를 제공합니다. 과거 4·5차 작업과 구분한 현재 공개본의 로컬 재현입니다.
