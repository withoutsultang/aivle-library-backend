package com.aivle8.backend.dto;

import com.aivle8.backend.domain.BookDetail;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookDetailResponseDto{

    private Long bookId;                  // PK
    private String description;
    private String content;

    public static BookDetailResponseDto fromEntity(BookDetail bookDetail) {
        if (bookDetail == null) return null;

        return BookDetailResponseDto.builder()
                .bookId(bookDetail.getBookId())
                .description(bookDetail.getDescription())
                .content(bookDetail.getContent())
                .build();
    }

    public BookDetail toEntity() {
        BookDetail bookDetail = new BookDetail();
        bookDetail.setBookId(this.bookId);
        bookDetail.setDescription(this.description);
        bookDetail.setContent(this.content);

        return bookDetail;
    }
}