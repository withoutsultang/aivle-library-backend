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
        return BaseResponse.success(bookService.getAllBooks());
    }
}
