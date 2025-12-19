package com.aivle8.backend.controller; // 이 클래스가 속한 패키지 선언 (컨트롤러 계층)

import com.aivle8.backend.global.common.BaseResponse; // API 응답의 표준 형식(성공/실패 및 데이터)을 정의한 클래스 임포트
import com.aivle8.backend.service.NaverAuthService; // 네이버 인증 및 사용자 정보 처리 로직을 담당하는 서비스 클래스 임포트
import lombok.RequiredArgsConstructor; // Lombok: final 필드(naverAuthService)를 주입받는 생성자 자동 생성
import org.springframework.http.ResponseEntity; // HTTP 응답 전체(상태 코드, 헤더, 본문)를 표현하는 클래스 임포트
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*; // Spring Web 관련 어노테이션들 임포트 (*: RestController, RequestMapping, GetMapping 등)
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Value;


import java.util.Map; // 데이터를 키-값 쌍으로 주고받기 위한 Map 인터페이스 임포트

@Controller
@RequestMapping("/api/v1/auth/naver") // 이 컨트롤러의 모든 메서드에 적용되는 기본 URI 경로 설정
@RequiredArgsConstructor
public class NaverAuthController {

    private final NaverAuthService naverAuthService;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Value("${app.frontend-redirect-path:/oauth/success}")
    private String frontendRedirectPath;

    @GetMapping("/login-url")
    @ResponseBody // 이 메서드의 반환 값은 HTTP 응답 본문으로 직접 사용됩니다.
    public ResponseEntity<BaseResponse<Map<String, String>>> getLoginUrl() {
        String loginUrl = naverAuthService.generateNaverLoginUrl();
        return ResponseEntity.ok(BaseResponse.success(Map.of("loginUrl", loginUrl)));
    }

    @GetMapping("/callback")
    public String callback(
            @RequestParam String code,
            @RequestParam String state
    ) {

        Map<String, Object> result = naverAuthService.getUserInfo(code, state);
        String accessToken = (String) result.get("jwt");

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path(frontendRedirectPath)
                .queryParam("token", accessToken)
                .build()
                .toUriString();

        return "redirect:" + redirectUrl;
    }
}