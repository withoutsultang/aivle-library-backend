# API 빠르게 보기

기준 경로: `/api/v1`. 인증이 필요한 API는 `Authorization: Bearer <JWT>`를 사용합니다.

|메서드|경로|내용|
|---|---|---|
|GET|`/health`|공개 상태 검사|
|GET|`/auth/naver/login-url`|로그인 URL|
|GET|`/auth/naver/callback`|OAuth callback|
|GET|`/me`|사용자 정보|
|GET|`/books`|페이지·정렬 조회|
|GET|`/books/popular?limit=10`|인기 도서|
|POST|`/covers/generate`|인증된 서버 이미지 생성|
|POST|`/books`|도서 등록, HTTP 201|
|GET|`/books/{id}`|메타데이터·조회수 증가|
|GET|`/books/{id}/detail`|소개·본문|
|PUT|`/books/{id}`|작성자 도서 수정|
|DELETE|`/books/{id}`|작성자 도서 삭제|
|PATCH|`/books/{id}/cover-url`|작성자 표지 변경|
|POST|`/books/{id}/like`|좋아요|
|POST|`/books/{id}/unlike`|좋아요 취소|
|GET|`/mypage/mybooks`|내가 쓴 도서|
|GET|`/mypage/likebooks`|관심 도서|

목록 정렬: `LATEST`, `POPULAR`, `TITLE_ASC`, `TITLE_DESC`. 기본 페이지 크기: 15.
응답은 `status`, `message`, `data`를 사용하고 검증 실패는 HTTP 400, 작성자 권한 실패는 HTTP 403으로 전달합니다.
