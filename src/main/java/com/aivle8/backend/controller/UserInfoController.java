package com.aivle8.backend.controller;

import com.aivle8.backend.domain.User;
import com.aivle8.backend.dto.UserDto;
import com.aivle8.backend.global.common.BaseResponse;
import com.aivle8.backend.global.exception.CustomException;
import com.aivle8.backend.global.exception.ErrorCode;
import com.aivle8.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 원래대로면 네이버 로그인 직후 jwt랑 User 정보를 넘겨줘야 하지만, 호출이 제대로 안 됐어서
 * NaverAuthController에선 임시로 토큰만 념겨주게 만들고, 현재 페이지에서 사용자 정보를 넘겨주게 함.
 * 수정 필요해 보임
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserInfoController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserDto>> me(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "인증 정보 없음");
        }

        //인증된 사용자 userId 추출
        String userIdStr = (String) authentication.getPrincipal();
        Long userId = Long.parseLong(userIdStr);

        //userId로 user 정보 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_FOUND, "유저 없음")
                );
        //User 엔티티를 UserDto로 변환하여 응답
        return ResponseEntity.ok(
                BaseResponse.success(UserDto.fromEntity(user))
        );
    }
}
