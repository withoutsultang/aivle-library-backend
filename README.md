# 📚 모두의 서재 · Backend

> 글을 쓰는 사람과 읽는 사람을 연결하는 창작 플랫폼

누구나 자신의 글을 도서로 등록하고, 다른 사람의 작품을 읽고 관심 도서를 모아볼 수 있는 서비스입니다. AI 표지 생성 기능을 연동해 글 작성부터 표지 선택, 도서 공개까지 하나의 흐름으로 구성했습니다.

**KT AIVLE 팀 프로젝트 · 개발 참여: 김건우 ([withoutsultang](https://github.com/withoutsultang))**

[Frontend 저장소](https://github.com/withoutsultang/aivle-library-frontend) · [원본 팀 저장소](https://github.com/KT-AIVLE-School-4th-Mini-Project/BackEnd)

## 주요 기능

| 기능 | 내용 |
| --- | --- |
| 도서 작성·관리 | 도서 등록, 상세·본문 조회, 작성자 권한에 따른 수정·삭제 |
| 도서 탐색 | 페이지네이션과 최신순·조회수순·제목순 정렬 |
| 사용자 인증 | 네이버 OAuth 로그인, JWT 기반 API 인증 |
| 개인 서재 | 관심 도서와 내가 작성한 도서 조회 |
| 표지 이미지 | 외부 이미지 URL을 이용한 S3 저장·교체·삭제 |
| 조회수 관리 | DynamoDB 조회수 갱신과 DB 동기화 스케줄러 |

위 기능은 팀이 함께 구현한 서비스 범위입니다. 개인 담당 내용은 아래에 구분했습니다.

## 담당 역할

### 도서 등록·목록 API

- 도서 등록 Controller·Service를 구현하고 사용자와 도서·본문 데이터를 연결했습니다.
- 목록 조회 결과를 DTO로 변환하고 API 응답을 개선했습니다.
- 조회수 기준 인기 도서 조회와 정렬 기능을 추가했습니다.

### 입력 검증과 API 오류 응답

- 요청 DTO의 유효성 검사 실패를 공통 예외 처리기에 연결했습니다.
- 잘못된 입력에 HTTP 400과 검증 메시지를 반환하도록 처리했습니다.
- JWT 필터와 도서 API의 토큰·응답 처리 개선에 참여했습니다.

### 관심 도서 데이터와 배포 설정

- 관심 도서 엔티티·Repository와 관련 응답 DTO를 추가했습니다.
- AWS Parameter Store의 환경변수를 애플리케이션에 전달하도록 빌드·시작 스크립트를 수정했습니다.
- 프론트엔드 주소와 OAuth 콜백, CORS 등 서비스 연동 설정을 조정했습니다.

개인 기여에 대응하는 변경 이력은 [기여 내역](docs/CONTRIBUTIONS.md)에 정리했습니다.

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| 언어·프레임워크 | Java 17, Spring Boot 3.5.8 |
| API·인증 | Spring Web, Spring Security, JWT, Naver OAuth |
| 데이터 | Spring Data JPA, MySQL, DynamoDB |
| 스토리지·설정 | AWS S3, Parameter Store |
| 빌드·테스트 | Gradle, JUnit 5, H2 |

## 서비스 구조

```text
React 클라이언트
  └─ Spring Boot API
       ├─ Spring Security · JWT · Naver OAuth
       ├─ JPA · MySQL              사용자·도서·본문·관심 도서
       ├─ S3                      표지 이미지
       └─ DynamoDB · Scheduler    조회수 갱신·동기화
```

### 코드에서 살펴볼 부분

- [BookServiceImpl](src/main/java/com/aivle8/backend/service/BookServiceImpl.java): 도서 등록·목록·수정·삭제 로직과 외부 스토리지 연동
- [GlobalExceptionHandler](src/main/java/com/aivle8/backend/global/exception/GlobalExceptionHandler.java): 비즈니스 예외·입력 검증 오류의 공통 응답 처리
- [BookLike](src/main/java/com/aivle8/backend/domain/BookLike.java): 사용자와 관심 도서의 관계
- [시작 스크립트](scripts/start.sh): Parameter Store 설정을 읽어 실행 환경에 주입하는 과정

## 프로젝트 구조

```text
src/main/java/com/aivle8/backend/
├── controller/   API 요청 처리
├── service/      도서·인증·마이페이지·스토리지 로직
├── repository/   데이터 접근
├── domain/       엔티티
├── dto/          요청·응답 모델
├── security/     JWT 필터·접근 제어
├── config/       AWS·웹 설정
├── scheduler/    조회수 동기화
└── global/       공통 응답·예외 처리
src/test/         단위 테스트
scripts/          배포·실행 스크립트
```

## 실행 방법

JDK 17을 사용합니다. 서비스 실행에는 MySQL과 AWS·네이버 OAuth 설정이 필요합니다.

```sh
git clone https://github.com/withoutsultang/aivle-library-backend.git
cd aivle-library-backend
git switch dev
./gradlew test bootJar
./gradlew bootRun
```

Windows에서는 `./gradlew` 대신 `.\gradlew.bat`를 사용합니다. 설정 항목은 [.env.example](.env.example), 환경 구성과 테스트 명령은 [실행 가이드](docs/RUNNING.md)를 참고해 주세요. 저장소의 인프라 예시값은 실행 환경에 맞게 설정해야 합니다.

## 관련 문서

- [개인 기여와 변경 이력](docs/CONTRIBUTIONS.md)
- [환경 설정·실행·테스트](docs/RUNNING.md)
- [제3자 자료와 의존성 고지](docs/THIRD_PARTY.md)