package com.aivle8.backend.controller;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.dto.BookCreateRequestDto;
import com.aivle8.backend.dto.BookResponseDto;
import com.aivle8.backend.dto.UserDto;
import com.aivle8.backend.global.common.BaseResponse;
import com.aivle8.backend.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 도서 목록 확인
     * GET /books/api/v1/books
     */
    @GetMapping("/api/v1/books")
    public BaseResponse<List<BookResponseDto>> getBooks() {
        return BaseResponse.success(bookService.getAllBooks());
    }
    /**
     * 신규 도서 등록
     * POST /books/api/v1/books
     */
    @PostMapping("/api/v1/books")
    public BaseResponse<Long> createBook(@RequestBody @Valid BookCreateRequestDto request) {

        Long createdId = bookService.createBook(request);

        return BaseResponse.created(createdId);
    }

}
