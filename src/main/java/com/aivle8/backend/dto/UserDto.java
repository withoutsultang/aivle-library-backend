package com.aivle8.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDto {
    private Long userId;          // PK
    private String nickname;      // 닉네임
    private String email;         // 이메일
    private String profileImage;  // 프로필 이미지 URL
}
