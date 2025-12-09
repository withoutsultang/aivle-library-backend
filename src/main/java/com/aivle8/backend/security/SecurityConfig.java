// src/main/com/aivle8/backend/security/SecurityConfig.java
package com.aivle8.backend.security;

import com.aivle8.backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 클래스입니다.
 * - JWT 기반 인증을 적용했습니다.
 * - /api/v1/auth/** 경로는 인증(로그인)을 제외했습니다.
 * - /h2-console/** 경로는 H2 콘솔 접근을 위해 열어두었습니다.
 * - 나머지 API는 접근 시 반드시 로그인(인증)이 필요하도록 설정했습니다.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;

    /**
     * JWT 필터를 Spring Bean으로 등록합니다.
     */
    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtService);
    }

    /**
     * SecurityFilterChain 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 비활성화 (REST API는 세션 기반이 아니므로 불필요)
                .csrf(csrf -> csrf.disable())

                // 2. H2 콘솔을 위해 frame 옵션 비활성화
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // 3. 세션 상태를 Stateless로 설정 (JWT 사용)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. 요청 경로별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**", "/h2-console/**").permitAll() // 로그인, H2 콘솔 허용
                        .anyRequest().authenticated() // 그 외 요청은 인증 필요
                )

                // 5. JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthenticationManager Bean 등록
     * (로그인이나 인증 처리 서비스에서 필요)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
