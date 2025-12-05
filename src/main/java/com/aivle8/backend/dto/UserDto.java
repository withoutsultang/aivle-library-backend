package com.aivle8.backend.dto;

import com.aivle8.backend.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDto {

    private Long userId;        // PK
    private String nickname;    // 닉네임
    private String email;       // 이메일
    private String profileImage; // 프로필 이미지 URL

    public static UserDto fromEntity(User user) {
        if (user == null) return null;

        return UserDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .build();
    }

    public User toEntity() {
        User user = new User();
        user.setUserId(this.userId);              // 보통 update 용도
        user.setNickname(this.nickname);
        user.setEmail(this.email);
        user.setProfileImage(this.profileImage);
        return user;
    }
}

