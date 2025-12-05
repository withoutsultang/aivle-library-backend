package com.aivle8.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCreateRequestDto {

    @NotBlank(message = "제목은 필수 입력 값입니다.")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "카테고리는 필수 입력 값입니다.")
    @Size(max = 100, message = "카테고리는 100자 이하여야 합니다.")
    private String category;

    @NotBlank(message = "설명은 필수 입력 값입니다.")
    private String description;

    @NotBlank(message = "내용은 필수 입력 값입니다.")
    private String content;

    private String coverImageUrl; // 초기 등록 시에는 null 가능

    @NotNull(message = "사용자 ID는 필수 입력 값입니다.")
    private Long userId;
}
