// src/main/java/com/aivle8/backend/service/JwtService.java
package com.aivle8.backend.service;

import com.aivle8.backend.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    /**
     * .env 또는 application.properties에서 지정한 JWT_SECRET를 주입받습니다.
     */
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    /**
     * 주입받은 문자열(secret)을 Key 객체로 변환하는 헬퍼 메서드입니다.
     * HMAC-SHA256 알고리즘용 Key 생성.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 사용자 정보를 기반으로 JWT 토큰을 생성합니다.
     */
    public String createToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .claim("email", user.getEmail())
                .claim("nickname", user.getNickname())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                // 기존 Key 객체 대신 getSigningKey()를 사용하도록 수정했습니다.
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
