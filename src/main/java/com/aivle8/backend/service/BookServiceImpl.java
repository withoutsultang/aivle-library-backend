package com.aivle8.backend.service;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.BookDetail;
import com.aivle8.backend.domain.User;
import com.aivle8.backend.dto.BookCreateRequestDto;
import com.aivle8.backend.dto.BookResponseDto;
import com.aivle8.backend.global.exception.CustomException;
import com.aivle8.backend.global.exception.ErrorCode;
import com.aivle8.backend.repository.BookDetailRepository;
import com.aivle8.backend.repository.BookRepository;
import com.aivle8.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    public Book getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    @Override
    public List<BookResponseDto> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(BookResponseDto::fromEntity)
                .toList();
    }

    @Override
    public Long createBook(BookCreateRequestDto request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Book book = Book.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .coverImageUrl(request.getCoverImageUrl())
                .viewCount(0)
                .user(user)
                .build();

        bookRepository.save(book);

        BookDetail detail = BookDetail.builder()
                .book(book)                    // OneToOne 연결
                .content(request.getContent())
                .description(request.getDescription())
                .build();

        bookDetailRepository.save(detail);

        return book.getBookId();
    }

}