package com.aivle8.backend.service;

import com.aivle8.backend.dto.*;

import java.util.List;

public interface BookService {
    BookResponseDto getBook(Long bookId);
    BookUpdateResponseDto updateBook(Long bookId, BookUpdateRequestDto bookDto);
    BookDetailResponseDto getBookDetail(Long bookId);
    void deleteBook(Long bookId);
    List<BookResponseDto> getAllBooks();
    BookCreateResponseDto createBook(BookCreateRequestDto request);
}
