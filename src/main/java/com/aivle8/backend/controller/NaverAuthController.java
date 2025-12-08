// src/main/java/com/aivle8/backend/controller/NaverAuthController.java
package com.aivle8.backend.controller;

import com.aivle8.backend.global.common.BaseResponse;
import com.aivle8.backend.service.NaverAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/naver")
@RequiredArgsConstructor
public class NaverAuthController {

    private final NaverAuthService naverAuthService;

    @GetMapping("/login-url")
    public ResponseEntity<BaseResponse<Map<String, String>>> getLoginUrl() {
        String loginUrl = naverAuthService.generateNaverLoginUrl();
        return ResponseEntity.ok(BaseResponse.success(Map.of("loginUrl", loginUrl)));
    }

    @PostMapping("/callback")
    public ResponseEntity<BaseResponse<Map<String, Object>>> callback(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String state = request.get("state");
        Map<String, Object> result = naverAuthService.getUserInfo(code, state);
        return ResponseEntity.ok(BaseResponse.success(result));
    }
}
