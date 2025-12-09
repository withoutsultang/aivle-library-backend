// src/main/java/com/aivle8/backend/security/AuthService.java
package com.aivle8.backend.security;

import com.aivle8.backend.domain.User;
import com.aivle8.backend.repository.UserRepository;
import com.aivle8.backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    /**
     * Naver 로그인 시 DB 조회 및 JWT 발급
     */
    @Transactional
    public String loginOrRegister(String naverId, String nickname, String email, String profileImage) {
        // 1. naverId로 사용자 조회
        Optional<User> optionalUser = userRepository.findByNaverId(naverId);

        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            // 2. 없으면 새로 생성
            user = User.builder()
                    .naverId(naverId)
                    .nickname(nickname)
                    .email(email)
                    .profileImage(profileImage)
                    .build();
            userRepository.save(user);
        }

        // 3. JWT 생성
        return jwtService.createToken(user);
    }
}
