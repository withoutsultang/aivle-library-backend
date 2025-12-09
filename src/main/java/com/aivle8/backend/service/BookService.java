package com.aivle8.backend.service;

import com.aivle8.backend.domain.enums.BookSortType;
import com.aivle8.backend.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookService {
    BookResponseDto getBook(Long bookId);
    BookUpdateResponseDto updateBook(Long bookId, BookUpdateRequestDto bookDto, Long userId);
    BookDetailResponseDto getBookDetail(Long bookId);
    void deleteBook(Long bookId, Long userId);
    Page<BookResponseDto> getBooks(BookSortType sort, int page, int size);
    BookCreateResponseDto createBook(BookCreateRequestDto request, Long userId);
    List<BookResponseDto> getPopularBooks(int limit);
    void updateBookCoverUrl(Long bookId, BookCoverUrlUpdateRequestDto bookCoverUrlUpdateRequestDto, Long userId);
}
