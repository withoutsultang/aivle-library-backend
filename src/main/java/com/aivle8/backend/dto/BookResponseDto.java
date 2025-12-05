package com.aivle8.backend.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class BookResponseDto {

    private Long bookId;                  // PK
    private String title;
    private String category;
    private String description;
    private String content;
    private int viewCount;
    private LocalDateTime bookCreated;
    private LocalDateTime bookUpdated;
    private String coverImageUrl;

    private UserDto user;                 // 작성자 정보
}
