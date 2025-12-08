package com.aivle8.backend.controller;

import com.aivle8.backend.dto.*;
import com.aivle8.backend.global.common.BaseResponse;
import com.aivle8.backend.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/books")
@RequiredArgsConstructor
public class BookController {
    public final BookService bookService;

    /**
     * 도서 일반 정보 조회
     * GET api/v1/books/{bookId}
     */
    @GetMapping("/{bookId}")
    public BaseResponse<BookResponseDto> getBook(@PathVariable("bookId") Long bookId) {
        return BaseResponse.success(bookService.getBook(bookId));
    }

    /**
     * 도서 목록 확인
     * GET /api/v1/books
     */
    @GetMapping
    public BaseResponse<List<BookResponseDto>> getBooks() {
        return BaseResponse.success(bookService.getAllBooks());
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
     * 도서 상세 정보 조회
     * GET api/v1/books/{bookId}/detail
     */
    @GetMapping("/{bookId}/detail")
    public BaseResponse<BookDetailResponseDto> getBookDetail(@PathVariable("bookId") Long bookId) {
        return BaseResponse.success(bookService.getBookDetail(bookId));
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
}
