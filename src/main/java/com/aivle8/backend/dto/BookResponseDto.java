package com.aivle8.backend.dto;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.BookLike;
import com.aivle8.backend.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BookResponseDto {

    private Long bookId;
    private String title;
    private String category;
    private int viewCount;
    private LocalDateTime bookCreated;
    private LocalDateTime bookUpdated;
    private String coverImageUrl;
    private UserDto user;
    private long likeCount;
    private boolean liked;

    private static final String S3_BASE_URL = "https://assets.example.invalid/";

    /** 기존 호출 호환용 (로그인 사용자 정보 없이) */
    public static BookResponseDto fromEntity(Book book) {
        return fromEntity(book, null);
    }

    /**
     * Book 엔티티 + 현재 로그인 사용자 기준으로 DTO 생성
     */
    public static BookResponseDto fromEntity(Book book, User currentUser) {
        if (book == null) return null;

        // 1. 좋아요 정보 처리
        boolean liked = false;
        if (currentUser != null && book.getBookLikes() != null) {
            liked = book.getBookLikes().stream()
                    .anyMatch(bl -> bl.getUser().getUserId().equals(currentUser.getUserId()));
        }

        long likeCount = book.getBookLikes() != null ? book.getBookLikes().size() : 0;

        // 2. 이미지 URL 변환 로직 추가
        String imageUrl = book.getCoverImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
            // "ai-images/uuid.png" 처럼 키값만 들어있는 경우 S3 주소를 앞에 붙여줌
            imageUrl = S3_BASE_URL + imageUrl;
        }

        return BookResponseDto.builder()
                .bookId(book.getBookId())
                .title(book.getTitle())
                .category(book.getCategory())
                .viewCount(book.getViewCount())
                .bookCreated(book.getBookCreated())
                .bookUpdated(book.getBookUpdated())
                .coverImageUrl(imageUrl) // 가공된 URL 적용
                .user(UserDto.fromEntity(book.getUser()))
                .likeCount(likeCount)
                .liked(liked)
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

        // 역직렬화(toEntity) 시에는 URL에서 다시 S3 키만 추출할 필요가 있을 수 있지만,
        // 보통 조회용 DTO이므로 그대로 둡니다.
        book.setCoverImageUrl(this.coverImageUrl);

        if (this.user != null) {
            book.setUser(this.user.toEntity());
        }

        return book;
    }

    public static BookResponseDto fromBookLikeEntity(BookLike bookLike) {
        if (bookLike == null || bookLike.getBook() == null) return null;
        return fromEntity(bookLike.getBook(), bookLike.getUser());
    }
}