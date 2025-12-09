package com.aivle8.backend.service;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.BookDetail;
import com.aivle8.backend.domain.User;
import com.aivle8.backend.domain.enums.BookSortType;
import com.aivle8.backend.dto.*;
import com.aivle8.backend.global.exception.CustomException;
import com.aivle8.backend.global.exception.ErrorCode;
import com.aivle8.backend.repository.BookDetailRepository;
import com.aivle8.backend.repository.BookRepository;
import com.aivle8.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookDetailRepository bookDetailRepository;


    @Override
    public BookResponseDto getBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 도서를 찾을 수 없습니다."));

        book.increaseViewCount();

        return BookResponseDto.fromEntity(book);
    }

    @Override
    public BookDetailResponseDto getBookDetail(Long bookId) {
        BookDetail detail = bookDetailRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 도서의 세부 정보가 없습니다."));

        return BookDetailResponseDto.fromEntity(detail);
    }

    @Override
    public BookUpdateResponseDto updateBook(Long bookId, BookUpdateRequestDto dto, Long userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 도서를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (!book.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 도서만 수정 가능합니다.");
        }

        book.setTitle(dto.getTitle());
        book.setCategory(dto.getCategory());
        book.setCoverImageUrl(dto.getCoverImageUrl());

        BookDetail detail = book.getBookDetail();

        if (detail == null) {
            detail = new BookDetail();
            detail.setBook(book);
            detail.setBookId(bookId);
            book.setBookDetail(detail);
        }

        detail.setDescription(dto.getDescription());
        detail.setContent(dto.getContent());

        return BookUpdateResponseDto.fromEntity(book);
    }

    @Override
    public void deleteBook(Long bookId, Long userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 도서를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));


        if (!book.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 도서만 삭제 가능합니다.");
        }

        bookRepository.delete(book);
    }

    @Override
    public Page<BookResponseDto> getBooks(BookSortType sort, int page, int size) {

        Sort sortOption = switch (sort) {
            case POPULAR -> Sort.by(Sort.Direction.DESC, "viewCount");
            case TITLE_ASC -> Sort.by(Sort.Direction.ASC, "title");
            case TITLE_DESC -> Sort.by(Sort.Direction.DESC, "title");
            case LATEST -> Sort.by(Sort.Direction.DESC, "bookCreated");
        };

        Pageable pageable = PageRequest.of(page, size, sortOption);

        return bookRepository.findAll(pageable)
                .map(BookResponseDto::fromEntity);
    }

    @Override
    public List<BookResponseDto> getPopularBooks(int limit){
        Pageable pageable = PageRequest.of(0, limit);
        return bookRepository.findTopByViewCount(pageable).stream()
                .map(BookResponseDto::fromEntity)
                .toList();
    }

    @Override
    public BookCreateResponseDto createBook(BookCreateRequestDto request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));


        Book book = Book.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .coverImageUrl(request.getCoverImageUrl())
                .user(user)
                .build();

        BookDetail detail = BookDetail.builder()
                .book(book)
                .content(request.getContent())
                .description(request.getDescription())
                .build();

        book.setBookDetail(detail);

        bookRepository.save(book);

        return BookCreateResponseDto.builder()
                .bookId(book.getBookId())
                .build();
    }

    @Override
    public void updateBookCoverUrl(Long bookId, BookCoverUrlUpdateRequestDto dto, Long userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 도서를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));


        if (!book.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 도서만 커버 변경 가능합니다.");
        }

        book.setCoverImageUrl(dto.getCoverImageUrl());
    }
}
