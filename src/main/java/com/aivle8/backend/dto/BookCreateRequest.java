package com.aivle8.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCreateRequest {
    private String title;
    private String description;
    private String category;
    private String content;
    private String coverImageUrl;
}

