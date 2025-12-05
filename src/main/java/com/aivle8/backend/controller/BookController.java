package com.aivle8.backend.controller;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.dto.BookResponseDto;
import com.aivle8.backend.dto.UserDto;
import com.aivle8.backend.global.common.BaseResponse;
import com.aivle8.backend.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 도서 전체 조회
     * GET /books/api/v1/books
     */
    @GetMapping("/api/v1/books")
    public BaseResponse<?> getBooks() {
        List<Book> books = bookService.getAllBooks();
        List<BookResponseDto> bookDtos = books.stream()
                .map(book -> BookResponseDto.builder()
                        .bookId(book.getBookId())
                        .title(book.getTitle())
                        .category(book.getCategory())
                        .description(book.getBookDetail().getDescription())
                        .content(book.getBookDetail().getContent())
                        .viewCount(book.getViewCount())
                        .bookCreated(book.getBookCreated())
                        .bookUpdated(book.getBookUpdated())
                        .coverImageUrl(book.getCoverImageUrl())
                        .user(UserDto.builder()
                                .userId(book.getUser().getUserId())
                                .nickname(book.getUser().getNickname())
                                .email(book.getUser().getEmail())
                                .profileImage(book.getUser().getProfileImage())
                                .build())
                        .build()
                ).toList();

        return BaseResponse.success(
                new Object() {
                    public final List<BookResponseDto> booksList = bookDtos;
                }
        );
    }
}
