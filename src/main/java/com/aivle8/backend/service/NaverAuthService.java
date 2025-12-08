// src/main/java/com/aivle8/backend/service/NaverAuthService.java
package com.aivle8.backend.service;

import com.aivle8.backend.domain.User;
import com.aivle8.backend.global.exception.CustomException;
import com.aivle8.backend.global.exception.ErrorCode;
import com.aivle8.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NaverAuthService {

    @Value("${naver.client-id}") private String clientId;
    @Value("${naver.client-secret}") private String clientSecret;
    @Value("${naver.redirect-uri}") private String redirectUri;
    @Value("${naver.state}") private String state;

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RestTemplate restTemplate = new RestTemplate();

    public String generateNaverLoginUrl() {
        return UriComponentsBuilder
                .fromHttpUrl("https://nid.naver.com/oauth2.0/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .toUriString();
    }

    public Map<String, Object> getUserInfo(String code, String receivedState) {
        if (!state.equals(receivedState)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "상태 값 불일치");
        }

        // 1. Access Token 발급
        String tokenRequestUrl = UriComponentsBuilder
                .fromHttpUrl("https://nid.naver.com/oauth2.0/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("code", code)
                .queryParam("state", state)
                .toUriString();

        Map<String, Object> tokenResponse =
                restTemplate.getForObject(tokenRequestUrl, Map.class);

        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "네이버 토큰 발급 실패");
        }

        String accessToken = (String) tokenResponse.get("access_token");

        // 2. 사용자 정보 조회
        String profileRequestUrl = "https://openapi.naver.com/v1/nid/me";
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

        org.springframework.http.ResponseEntity<Map> profileResponse =
                restTemplate.exchange(profileRequestUrl,
                        org.springframework.http.HttpMethod.GET,
                        entity,
                        Map.class);

        if (profileResponse.getBody() == null || !profileResponse.getBody().containsKey("response")) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "네이버 사용자 정보 조회 실패");
        }

        Map<String, Object> response = (Map<String, Object>) profileResponse.getBody().get("response");
        String email = (String) response.get("email");
        String nickname = (String) response.get("nickname");
        String naverId = (String) response.get("id");
        String profileImage = (String) response.get("profile_image");

        // 3. naverId 기준 기존 유저 조회
        Optional<User> userOptional = userRepository.findByNaverId(naverId);
        User user;

        if (userOptional.isPresent()) {
            // 기존 유저: 이메일/닉네임/프로필 이미지 업데이트
            user = userOptional.get();
            boolean updated = false;

            if (!email.equals(user.getEmail())) {
                user.setEmail(email);
                updated = true;
            }
            if (!nickname.equals(user.getNickname())) {
                user.setNickname(nickname);
                updated = true;
            }
            if (profileImage != null && !profileImage.equals(user.getProfileImage())) {
                user.setProfileImage(profileImage);
                updated = true;
            }

            if (updated) {
                userRepository.save(user);
            }
        } else {
            // 신규 유저 생성
            user = User.builder()
                    .naverId(naverId)
                    .email(email)
                    .nickname(nickname)
                    .profileImage(profileImage)
                    .build();
            userRepository.save(user);
        }

        // 4. JWT 발급
        String jwt = jwtService.createToken(user);

        return Map.of(
                "jwt", jwt,
                "user", user
        );
    }
}
