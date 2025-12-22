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

@Service // 이 클래스를 Spring 서비스 Bean으로 등록
@RequiredArgsConstructor // final 필드들을 인자로 받는 생성자를 자동 생성하여 의존성 주입
public class NaverAuthService { // 네이버 소셜 로그인 및 인증 처리를 담당하는 서비스 클래스

    @Value("${naver.client-id}") private String clientId; // 설정 파일에서 네이버 클라이언트 ID 값을 주입받음
    @Value("${naver.client-secret}") private String clientSecret; // 설정 파일에서 네이버 클라이언트 Secret 값을 주입받음
    @Value("${naver.redirect-uri}") private String redirectUri; // 설정 파일에서 네이버 리다이렉트 URI 값을 주입받음
    @Value("${naver.state}") private String state; // 설정 파일에서 CSRF 방지용 상태(state) 값을 주입받음

    private final UserRepository userRepository; // 사용자 DB 작업을 위한 리포지토리 객체 주입
    private final JwtService jwtService; // JWT(토큰) 생성/처리를 위한 서비스 객체 주입
    private final RestTemplate restTemplate = new RestTemplate(); // 외부 API 통신을 위한 RestTemplate 객체 생성

    public String generateNaverLoginUrl() { // 네이버 로그인 페이지로 사용자를 이동시키기 위한 URL을 생성하는 메서드
        return UriComponentsBuilder // URI 빌더 시작
                .fromHttpUrl("https://nid.naver.com/oauth2.0/authorize") // 네이버 인증 서버의 기본 URL 설정
                .queryParam("response_type", "code") // 응답 유형: 'code' (Authorization Code 요청)
                .queryParam("client_id", clientId) // 발급받은 클라이언트 ID 추가
                .queryParam("redirect_uri", redirectUri) // 인증 성공 후 돌아올 URI 추가
                .queryParam("state", state) // 보안을 위한 상태(state) 값 추가
                .toUriString(); // 최종 로그인 요청 URL을 문자열로 반환
    }

    public Map<String, Object> getUserInfo(String code, String receivedState) { // 네이버로부터 받은 인증 코드와 상태 값으로 사용자 정보를 처리하는 메서드
        if (!state.equals(receivedState)) { // 서버에 저장된 state 값과 요청으로 받은 state 값이 일치하는지 확인 (CSRF 방지)
            throw new CustomException(ErrorCode.BAD_REQUEST, "상태 값 불일치"); // 불일치하면 보안 예외 발생
        }

        // 1. Access Token 발급 요청
        String tokenRequestUrl = UriComponentsBuilder // Access Token 발급 요청 URL 생성 시작
                .fromHttpUrl("https://nid.naver.com/oauth2.0/token") // 네이버 토큰 발급 서버 URL 설정
                .queryParam("grant_type", "authorization_code") // 인증 코드를 사용하여 토큰을 요청하는 방식 지정
                .queryParam("client_id", clientId) // 클라이언트 ID 추가
                .queryParam("client_secret", clientSecret) // 클라이언트 Secret 추가
                .queryParam("code", code) // 로그인 성공 후 받은 인증 코드(code) 추가
                .queryParam("state", state) // state 값 추가
                .toUriString(); // 최종 토큰 요청 URL을 문자열로 반환

        Map<String, Object> tokenResponse = // 네이버 토큰 API를 호출하고 응답(JSON)을 Map 형태로 받음
                restTemplate.getForObject(tokenRequestUrl, Map.class); // GET 요청 실행

        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) { // 응답 유효성과 access_token 포함 여부 확인
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "네이버 토큰 발급 실패"); // 실패 시 예외 발생
        }

        String accessToken = (String) tokenResponse.get("access_token"); // 응답에서 Access Token 값을 추출

        // 2. 사용자 정보 조회 요청
        String profileRequestUrl = "https://openapi.naver.com/v1/nid/me"; // 네이버 사용자 프로필 정보 조회 API URL
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders(); // HTTP 요청 헤더 객체 생성
        headers.set("Authorization", "Bearer " + accessToken); // 헤더에 "Authorization: Bearer [Access Token]" 설정하여 인증

        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers); // 헤더를 포함하는 HTTP 엔티티 생성

        org.springframework.http.ResponseEntity<Map> profileResponse = // 네이버 프로필 API를 호출하고 응답(JSON)을 Map 형태로 받음
                restTemplate.exchange(profileRequestUrl, // 프로필 요청 URL
                        org.springframework.http.HttpMethod.GET, // HTTP GET 메서드 사용
                        entity, // 요청 엔티티(인증 헤더 포함) 전송
                        Map.class); // 응답 본문을 Map 타입으로 받음

        if (profileResponse.getBody() == null || !profileResponse.getBody().containsKey("response")) { // 응답 본문 유효성과 'response' 키 포함 여부 확인
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "네이버 사용자 정보 조회 실패"); // 실패 시 예외 발생
        }

        Map<String, Object> response = (Map<String, Object>) profileResponse.getBody().get("response"); // 실제 사용자 정보가 담긴 'response' 객체 추출
        String email = (String) response.get("email"); // 이메일 추출
        String nickname = (String) response.get("nickname"); // 닉네임 추출
        String naverId = (String) response.get("id"); // 네이버 고유 ID 추출 (사용자 식별자)
        String profileImage = (String) response.get("profile_image"); // 프로필 이미지 URL 추출

        // 3. naverId 기준 기존 유저 조회 및 정보 업데이트
        Optional<User> userOptional = userRepository.findByNaverId(naverId); // 네이버 ID로 데이터베이스에서 사용자 조회
        User user; // 최종적으로 사용할 User 객체 선언

        if (userOptional.isPresent()) { // Optional에 User 객체가 존재하면 (기존 가입자)
            // 기존 유저: 이메일/닉네임/프로필 이미지 업데이트 로직 시작
            user = userOptional.get(); // 기존 User 객체를 가져옴
            boolean updated = false; // 정보 변경 여부를 체크할 플래그

            if (!email.equals(user.getEmail())) { // 이메일이 변경되었는지 확인
                user.setEmail(email); // 이메일 업데이트
                updated = true;
            }
            if (!nickname.equals(user.getNickname())) { // 닉네임이 변경되었는지 확인
                user.setNickname(nickname); // 닉네임 업데이트
                updated = true;
            }
            if (profileImage != null && !profileImage.equals(user.getProfileImage())) { // 프로필 이미지가 변경되었는지 확인 (null 체크 포함)
                user.setProfileImage(profileImage); // 프로필 이미지 URL 업데이트
                updated = true;
            }

            if (updated) { // 변경된 내용이 있다면
                userRepository.save(user); // 데이터베이스에 저장(업데이트)
            }
        } else { // Optional에 User 객체가 존재하지 않으면 (신규 가입자)
            // 신규 유저 생성 및 저장 로직 시작
            user = User.builder() // User.builder()를 사용하여 새로운 User 객체 생성
                    .naverId(naverId) // 네이버 ID 설정
                    .email(email) // 이메일 설정
                    .nickname(nickname) // 닉네임 설정
                    .profileImage(profileImage) // 프로필 이미지 URL 설정
                    .build(); // User 객체 빌드 완료
            userRepository.save(user); // 데이터베이스에 저장(신규 등록)
        }

        // 4. JWT 발급
        String jwt = jwtService.createToken(user); // 인증이 완료된 User 객체를 기반으로 JWT 토큰 생성


        return Map.of( // 생성된 JWT와 User 객체를 Map 형태로 묶어서 반환
                "jwt", jwt, // 키 'jwt'에 토큰 문자열을 값으로 저장
                "user", user // 키 'user'에 처리된 User 객체를 값으로 저장
        );
    }
}