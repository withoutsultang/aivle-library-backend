package com.aivle8.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCoverUrlUpdateRequestDto {
    @NotBlank(message = "이미지는 필수 입력 값입니다.")
    private String coverImageUrl;
}
