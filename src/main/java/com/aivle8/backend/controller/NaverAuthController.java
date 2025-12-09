// src/main/java/com/aivle8/backend/controller/NaverAuthController.java
package com.aivle8.backend.controller; // 이 클래스가 속한 패키지 선언 (컨트롤러 계층)

import com.aivle8.backend.global.common.BaseResponse; // API 응답의 표준 형식(성공/실패 및 데이터)을 정의한 클래스 임포트
import com.aivle8.backend.service.NaverAuthService; // 네이버 인증 및 사용자 정보 처리 로직을 담당하는 서비스 클래스 임포트
import lombok.RequiredArgsConstructor; // Lombok: final 필드(naverAuthService)를 주입받는 생성자 자동 생성
import org.springframework.http.ResponseEntity; // HTTP 응답 전체(상태 코드, 헤더, 본문)를 표현하는 클래스 임포트
import org.springframework.web.bind.annotation.*; // Spring Web 관련 어노테이션들 임포트 (*: RestController, RequestMapping, GetMapping 등)

import java.util.Map; // 데이터를 키-값 쌍으로 주고받기 위한 Map 인터페이스 임포트

@RestController // 이 클래스가 REST API의 엔드포인트(요청 처리자)임을 명시
@RequestMapping("/api/v1/auth/naver") // 이 컨트롤러의 모든 메서드에 적용되는 기본 URI 경로 설정
@RequiredArgsConstructor // final 필드들을 주입받는 생성자 자동 생성
public class NaverAuthController { // 네이버 소셜 로그인 관련 HTTP 요청을 처리하는 컨트롤러 클래스

    // 의존성 주입: 네이버 인증 로직을 수행할 서비스 객체
    private final NaverAuthService naverAuthService;

    // 1. 네이버 로그인 시작 URL 제공 엔드포인트
    @GetMapping("/login-url") // GET 요청: "/api/v1/auth/naver/login-url" 경로에 매핑
    public ResponseEntity<BaseResponse<Map<String, String>>> getLoginUrl() { // 네이버 로그인 URL을 반환하는 메서드
        // 서비스 계층의 메서드를 호출하여 네이버 인증 서버로 이동할 URL을 생성
        String loginUrl = naverAuthService.generateNaverLoginUrl();
        // 성공 응답 (HTTP 200 OK)과 함께, 생성된 로그인 URL을 JSON 형태로 래핑하여 반환
        return ResponseEntity.ok(BaseResponse.success(Map.of("loginUrl", loginUrl)));
    }

    // 2. 네이버 인증 후 리다이렉트(콜백) 처리 엔드포인트
    @GetMapping("/callback") // GET 요청: "/api/v1/auth/naver/callback" 경로에 매핑
    public ResponseEntity<BaseResponse<Map<String, Object>>> callback( // 네이버 인증 서버로부터 리다이렉트된 요청을 처리하는 메서드
                                                                       @RequestParam String code, // 쿼리 파라미터로 전송된 인증 코드(code)를 받음
                                                                       @RequestParam String state // 쿼리 파라미터로 전송된 상태 값(state)을 받음
    ) {
        // 서비스 계층의 메서드를 호출하여 인증 코드와 상태 값을 네이버에 전달하고,
        // Access Token 발급, 사용자 정보 조회, 회원가입/정보 업데이트 및 JWT 생성 로직을 모두 수행
        Map<String, Object> result = naverAuthService.getUserInfo(code, state);
        // 최종적으로 생성된 JWT 토큰과 사용자 정보를 포함한 결과를 성공 응답 (HTTP 200 OK)으로 반환
        return ResponseEntity.ok(BaseResponse.success(result));
    }
}