package com.aivle8.backend.dto;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.BookDetail;
import com.aivle8.backend.global.util.ImageUrlUtil;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookUpdateResponseDto {
    private String title;
    private String category;
    private String description;
    private String content;
    private String coverImageUrl;

    public static BookUpdateResponseDto fromEntity(Book book) {
        if (book == null) return null;

        BookDetail detail = book.getBookDetail();

        String imageUrl = ImageUrlUtil.toFullUrl(book.getCoverImageUrl());

        return BookUpdateResponseDto.builder()
                .title(book.getTitle())
                .category(book.getCategory())
                .description(detail != null ? detail.getDescription() : null)
                .content(detail != null ? detail.getContent() : null)
                .coverImageUrl(imageUrl)
                .build();
    }
}