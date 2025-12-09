# 📚 모두의 서재

> 누구나 작가가 되어 자유롭게 글을 집필하고 공개할 수 있는 창작 플랫폼, 
'모두의 서재'의 백엔드 리포지토리입니다.
> 

본 프로젝트는 사용자 인증, 도서 데이터 관리, 대용량 텍스트 처리, 그리고 AI 표지 이미지 저장을 위한 견고한 API 서버를 구축하는 것을 목표로 합니다.

## 🛠 Tech Stack

| Category | Technology |
| --- | --- |
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.5.8 |
| **Security** | Spring Security, JWT, OAuth 2.0 (Naver) |
| **Database** | H2 DataBase, Spring Data JPA |
| **Build Tool** | Gradle |

## 🏗 System Architecture & Package Structure

<img width="1024" height="559" alt="image (9)" src="https://github.com/user-attachments/assets/ba2b74aa-5632-4d4b-bf24-d0c612eb092d" />

<img width="1024" height="559" alt="image (10)" src="https://github.com/user-attachments/assets/d66f8b0d-1cca-4547-8cbc-115ba228744c" />

### 1. 계층 구조 (Layered Architecture)

본 프로젝트는 유지보수성과 확장성을 고려하여 **Controller - Service - Repository**의 전형적인 계층형 아키텍처를 따릅니다.

```
├─ controller                        // 요청 진입점 (BookController 등)
│   └─ BookController.java           
├─ service                           // 비즈니스 로직 (AuthService, BookService 등)
│   ├─ AuthService.java
│   ├─ JwtService.java
│   ├─ NaverAuthService.java
│   ├─ BookService.java              // 인터페이스 정의
│   └─ BookServiceImpl.java          // 비즈니스 로직 구현
├─ repository                        // DB 접근 (BookRepository, UserRepository 등)
│   ├─ BookRepository.java           
│   ├─ BookDetailRepository.java     
│   └─ UserRepository.java          
├─ domain                            // 엔티티 정의 (Book, BookDetail, User)
│   ├─ Book.java
│   ├─ BookDetail.java
│   └─ User.java
├─ dto                               // 데이터 전송 객체 (Request/Response DTO)
│   ├─ BookCreateRequestDto.java
│   ├─ BookUpdateRequestDto.java
│   ├─ BookResponseDto.java
│   └─ UserDto.java
├─ global
│   ├─ common                        // 공통 응답 포맷
│   │   ├─ BaseResponse.java
│   │   ├─ PageResponse.java
│   │   └─ ResponseMessage.java
│   └─ exception                     // 전역 예외 처리
│       ├─ CustomException.java
│       ├─ ErrorCode.java
│       └─ GlobalExceptionHandler.java
└─ config                            // Security, JWT, Web 설정
    ├─ JwtFilter.java
    ├─ SecurityConfig.java
    └─ WebConfig.java
```

### 2. 주요 도메인 모델링 (ERD 설계 포인트)

- **User**: 네이버 OAuth를 통해 가입된 사용자 정보 관리
- **Book ↔ BookDetail 분리**:
    - 목록 조회 성능 향상을 위해 도서의 메타데이터(`Book`)와 대용량 본문(`BookDetail`)을 1:1 관계로 분리하여 설계했습니다.

## ✨ Key Features (API)

### 1. 사용자 인증 (Auth)

- **Naver OAuth 2.0**: 소셜 로그인을 통한 간편 회원가입 및 로그인
- **JWT (Json Web Token)**: 로그인 성공 시 Access Token 발급 및 Stateless 인증 구현
- **Security Filter**: 보호된 리소스(글 쓰기/수정/삭제) 접근 시 토큰 검증

### 2. 도서 관리 (Book Management)

- **도서 등록**: 제목, 카테고리, 요약, 본문 저장 (AI 생성 표지 URL 포함)
- **도서 목록 조회**:
    - 페이징 처리 (Pageable, 15개씩)
    - 정렬 기능 (최신순 / 조회수순 등)
- **도서 상세 조회**:
    - 상세 조회 시 조회수(view count) 자동 증가 로직 포함
    - 본문(`content`) 데이터 Lazy Loading 처리 고려
- **수정 및 삭제**: 작성자 본인 확인(User ID 매칭) 후 권한 부여

## 🚀 Getting Started

### Prerequisites

- Java 17+

### Installation & Run

1. **Clone the repository**
    
    ```
    git clone https://github.com/KT-AIVLE-School-4th-Mini-Project/BackEnd.git
    ```
    
2. **Configure Environment**
`src/main/resources/.env` 파일을 생성하고 키값을 입력합니다. 이후 구성 편집을 통해 환경병수로 등록합니다.
    
    ```
    NAVER_CLIENT_ID={NAVER_CLIENT_ID}
    NAVER_CLIENT_SECRET={NAVER_CLIENT_SECRET}
    NAVER_REDIRECT_URI=http://localhost:8080/api/v1/auth/naver/callback
    NAVER_STATE=SOME_RANDOM_STRING
    JWT_SECRET={SECRET_KEY}
    JWT_EXPIRATION_MS={JWT_EXPIRATION_MS}
    ```
    
3. **Build & Run**

## 📂 Documentation

더 자세한 문서는 `/docs` 폴더를 참고해주세요.
