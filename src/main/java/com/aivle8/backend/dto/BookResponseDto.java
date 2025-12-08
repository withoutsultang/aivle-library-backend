package com.aivle8.backend.dto;

import com.aivle8.backend.domain.Book;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookResponseDto {

    private Long bookId;                  // PK
    private String title;
    private String category;
    private int viewCount;
    private LocalDateTime bookCreated;
    private LocalDateTime bookUpdated;
    private String coverImageUrl;

    private UserDto user;                 // 작성자 정보

    public static BookResponseDto fromEntity(Book book) {
        if (book == null) return null;

        return BookResponseDto.builder()
                .bookId(book.getBookId())
                .title(book.getTitle())
                .category(book.getCategory())
                .viewCount(book.getViewCount())
                .bookCreated(book.getBookCreated())
                .bookUpdated(book.getBookUpdated())
                .coverImageUrl(book.getCoverImageUrl())
                .user(UserDto.fromEntity(book.getUser()))
                .build();
    }

    public Book toEntity() {
        Book book = new Book();
        book.setBookId(this.bookId);
        book.setTitle(this.title);
        book.setCategory(this.category);
        book.setViewCount(this.viewCount);
        book.setBookCreated(this.bookCreated);
        book.setBookUpdated(this.bookUpdated);
        book.setCoverImageUrl(this.coverImageUrl);

        if (this.user != null) {
            book.setUser(this.user.toEntity());
        }

        return book;
    }
}
