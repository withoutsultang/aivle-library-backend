package com.aivle8.backend.service;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.BookDetail;
import com.aivle8.backend.domain.User;
import com.aivle8.backend.dto.*;
import com.aivle8.backend.global.exception.CustomException;
import com.aivle8.backend.global.exception.ErrorCode;
import com.aivle8.backend.repository.BookDetailRepository;
import com.aivle8.backend.repository.BookRepository;
import com.aivle8.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    @Transactional
    public BookUpdateResponseDto updateBook(Long bookId, BookUpdateRequestDto dto) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 도서를 찾을 수 없습니다."));

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
    public void deleteBook(Long bookId) {
        Book b = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 도서를 찾을 수 없습니다."));
        bookRepository.delete(b);
    }

    @Override
    public List<BookResponseDto> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(BookResponseDto::fromEntity)
                .toList();
    }

    @Override
    public List<BookResponseDto> getPopularBooks(int limit){
        Pageable pageable = PageRequest.of(0, limit);
        return bookRepository.findTopByViewCount(pageable).stream()
                .map(BookResponseDto::fromEntity)
                .toList();
    }

    @Override
    public BookCreateResponseDto createBook(BookCreateRequestDto request) {

        // TODO: 추후 회원 기능 개발 후 userId로 조회하도록 변경
//        User user = userRepository.findById(request.getUserId())
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 임시 개발용 유저 기본 ID = 1
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "임시 유저가 없습니다. DB에서 생성해주세요."));
        Book book = Book.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .coverImageUrl(request.getCoverImageUrl())
                .user(user)
                .build();

        BookDetail detail = BookDetail.builder()
                .book(book)                    // OneToOne 연결
                .content(request.getContent())
                .description(request.getDescription())
                .build();

        book.setBookDetail(detail);

        bookRepository.save(book);

        return BookCreateResponseDto.builder()
                .bookId(book.getBookId())
                .build();
    }

}