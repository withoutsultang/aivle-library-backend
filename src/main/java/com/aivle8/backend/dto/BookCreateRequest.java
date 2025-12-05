package com.aivle8.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCreateRequest {

    @Size(min = 1, max = 45, message = "제목은 1~45자여야 합니다.")
    private String title;

    @Size(min = 1, max = 45, message = "카테고리는 1~45자여야 합니다.")
    private String category;

    @Size(min = 1, max = 200, message = "설명은 1~200자여야 합니다.")
    private String description;

    @Size(min = 1, message = "내용은 비어 있을 수 없습니다.")
    private String content;

    private String coverImageUrl;
}
