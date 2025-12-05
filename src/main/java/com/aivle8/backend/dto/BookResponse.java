package com.aivle8.backend.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class BookResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private String content;
    private String coverImageUrl;
    private int viewCount;
    private LocalDateTime bookCreated;
    private LocalDateTime bookUpdated;
}
