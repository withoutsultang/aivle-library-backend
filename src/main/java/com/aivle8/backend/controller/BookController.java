package com.aivle8.backend.controller;

import com.aivle8.backend.domain.enums.BookSortType;
import com.aivle8.backend.dto.*;
import com.aivle8.backend.global.common.BaseResponse;
import com.aivle8.backend.global.common.PageResponse;
import com.aivle8.backend.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {
    public final BookService bookService;

    /**
     * 도서 목록 확인
     * GET /api/v1/books?sort=LATEST&page=0&size=10
     */
    @GetMapping
    public BaseResponse<PageResponse<BookResponseDto>> getBooks(
            @RequestParam(defaultValue = "LATEST") BookSortType sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Page<BookResponseDto> result = bookService.getBooks(sort, page, size);

        PageResponse<BookResponseDto> response = new PageResponse<>(
                result.getContent(),
                result.getTotalPages(),
                result.getTotalElements(),
                result.getNumber()
        );

        return BaseResponse.success(response);
    }


    /**
     * 신규 도서 등록
     * POST api/v1/books
     */
    @PostMapping
    public BaseResponse<BookCreateResponseDto> createBook(@RequestBody @Valid BookCreateRequestDto request) {
        BookCreateResponseDto createdId = bookService.createBook(request);

        return BaseResponse.created(createdId);
    }

    /**
     * 도서 일반 정보 조회
     * GET api/v1/books/{bookId}
     */
    @GetMapping("/{bookId}")
    public BaseResponse<BookResponseDto> getBook(@PathVariable("bookId") Long bookId) {
        return BaseResponse.success(bookService.getBook(bookId));
    }

    /**
     * 조회수 기준 인기 도서 정보 조회
     * GET api/v1/books/popular?limit={limit}
     */
    @GetMapping("/popular")
    public BaseResponse<List<BookResponseDto>> getPopularBooks(@RequestParam(defaultValue = "10") int limit) {
        return BaseResponse.success(bookService.getPopularBooks(limit));
    }

    /**
     * 도서 수정
     * PUT api/v1/books/{bookId}
     */
    @PutMapping("/{bookId}")
    public BaseResponse<BookUpdateResponseDto> updateBook(@PathVariable("bookId") Long bookId, @RequestBody @Valid BookUpdateRequestDto book) {
        return BaseResponse.success(bookService.updateBook(bookId, book));
    }

    /**
     * 도서 삭제
     * DELETE api/v1/books/{bookId}
     */
    @DeleteMapping("/{bookId}")
    public BaseResponse<Void> deleteBook(@PathVariable("bookId") Long bookId) {
        bookService.deleteBook(bookId);
        return BaseResponse.success(null);
    }

    /**
     * 도서 상세 정보 조회
     * GET api/v1/books/{bookId}/detail
     */
    @GetMapping("/{bookId}/detail")
    public BaseResponse<BookDetailResponseDto> getBookDetail(@PathVariable("bookId") Long bookId) {
        return BaseResponse.success(bookService.getBookDetail(bookId));
    }

    /**
     * 도서 커버 이미지 변경
     * GET api/v1/books/{bookId}/detail
     */
    @PatchMapping("/{bookId}/cover-url")
    public BaseResponse<Void> updateBookCoverUrl(@PathVariable("bookId") Long bookId, @RequestBody @Valid BookCoverUrlUpdateRequestDto bookCoverUrlUpdateRequestDto) {
        bookService.updateBookCoverUrl(bookId, bookCoverUrlUpdateRequestDto);
        return BaseResponse.success(null);
    }
}
