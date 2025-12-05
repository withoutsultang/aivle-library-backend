package com.aivle8.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookUpdateRequest {
    private String title;
    private String description;
    private String category;
    private String content;
    private String coverImageUrl; // 사용자가 새 URL 줄 수 있음
}
