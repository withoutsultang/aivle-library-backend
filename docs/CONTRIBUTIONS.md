# 김건우 · 백엔드 기여

## 팀 프로젝트에서 맡은 일

아래는 김건우(`withoutsultang`)의 작성 커밋과 연결되는 기여입니다. 링크는 현재 공개 저장소에서 읽을 수 있는 대응 커밋을 사용합니다.

|주제|구현·변경 내용|작성 근거|
|---|---|---|
|목록 DTO 매핑|Entity 데이터를 화면 응답 계약으로 변환|[7e4ec62](https://github.com/withoutsultang/aivle-library-backend/commit/a50f582e6840b497c49ae40568137ffc6bf1b8ce)|
|도서 등록 API|Controller·Service에서 사용자·도서·본문 연결|[7b8e43f](https://github.com/withoutsultang/aivle-library-backend/commit/14e10bccb41f032890fc4a407e071ebe7c7ecf59)|
|Validation 핸들러|검증 메시지와 HTTP 400 공통 응답|[574cea8](https://github.com/withoutsultang/aivle-library-backend/commit/99c06c1e8b99197c478163d3cba58fc4b17cb3f3)|
|인기 도서 조회|조회수 내림차순과 상위 N개 조회|[db31610](https://github.com/withoutsultang/aivle-library-backend/commit/babecdd082ccaf634df20bd3b6d823d6ce37ded5)|
|좋아요 데이터 모델|BookLike 관계·유니크 제약·Repository·DTO 확장|[d119ca8](https://github.com/withoutsultang/aivle-library-backend/commit/89f3cf9e58c37e29e49042389dff4cb96be11545)|
|JWT·작성자 인가|principal 전달과 수정·삭제 권한 처리|[022e764](https://github.com/withoutsultang/aivle-library-backend/commit/3499a29c6fb570381627146a0e2b0ad6857efe17)|
|health 경로 연동|배포 검사와 애플리케이션 경로 연결|[28f4c11](https://github.com/withoutsultang/aivle-library-backend/commit/b9d18af415e7e684267a11e6a26de54f930a6c56)|
|OAuth redirect 설정|프론트엔드 주소 외부화|[1295830](https://github.com/withoutsultang/aivle-library-backend/commit/b20b6682331931aebd735e4ad1b52ad02d69adcd)|
|RDS 설정|MySQL datasource와 실행 환경 설정|[2951c74](https://github.com/withoutsultang/aivle-library-backend/commit/81c352b6fcb1131564b09a3c56183a558fafc666)|
|SSM·JAR 설정|외부 설정 주입과 배포 실행 파일 구성|[ccbb714](https://github.com/withoutsultang/aivle-library-backend/commit/13d078cf8be52d26e08877b44cb2c43547e67a89)|

## 협업 범위

도서·인증·UI·스토리지·조회수·인프라를 팀에서 나눠 구성했습니다. 김건우는 백엔드 API·데이터 계약·인증/인가 개선·배포 연동을 중심으로 참여했고, 프론트엔드 연결 환경과 이미지 요청 설정을 함께 다뤘습니다. S3 이미지 저장과 DynamoDB 조회수 확장, 초기 화면 컴포넌트·모니터링 구성은 팀 협업의 결과입니다.

## 2026년 10월 · 개인 공개본 고도화

이미지 저장 key·접근 URL 처리, 상태 검사·CORS, 원고 수정 흐름, 사용자 상태와 표지 fallback을 개선했습니다. 테스트·빌드·코드 검사와 CI를 함께 정리해 코드를 검토하고 다시 실행하기 쉬운 형태로 구성했습니다.

- 서비스 설계: [ARCHITECTURE.md](ARCHITECTURE.md)
- 검증 명령과 결과: [VERIFICATION.md](VERIFICATION.md)
- 팀 원본과 공개본의 이력 대응: [history-commit-map.tsv](history-commit-map.tsv)
