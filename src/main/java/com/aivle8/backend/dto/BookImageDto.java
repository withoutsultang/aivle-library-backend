package com.aivle8.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BookImageDto {
    private Long bookImageId;       // PK
    private String coverImageUrl;   // 이미지 URL
    private LocalDateTime createdAt; // 생성 시간
}
