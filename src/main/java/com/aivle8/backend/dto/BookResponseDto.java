package com.aivle8.backend.dto;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.BookDetail;
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

    /**
     * Entity → DTO 변환 Mapper
     */
    public static BookResponseDto fromEntity(Book book) {
        if (book == null) return null;

        BookDetail detail = book.getBookDetail();

        return BookResponseDto.builder()
                .bookId(book.getBookId())
                .title(book.getTitle())
                .category(book.getCategory())
                .description(detail != null ? detail.getDescription() : null)
                .content(detail != null ? detail.getContent() : null)
                .viewCount(book.getViewCount())
                .bookCreated(book.getBookCreated())
                .bookUpdated(book.getBookUpdated())
                .coverImageUrl(book.getCoverImageUrl())
                .user(UserDto.fromEntity(book.getUser()))
                .build();
    }
}
