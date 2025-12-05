package com.aivle8.backend.service;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.dto.BookResponseDto;

import java.util.List;

public interface BookService {
    Book getBook(Long id);

    List<BookResponseDto> getAllBooks();
}
