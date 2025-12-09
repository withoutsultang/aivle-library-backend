// src/main/java/com/aivle8/backend/service/BookLikeServiceImpl.java
package com.aivle8.backend.service;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.BookLike;
import com.aivle8.backend.domain.User;
import com.aivle8.backend.global.exception.CustomException;
import com.aivle8.backend.global.exception.ErrorCode;
import com.aivle8.backend.repository.BookLikeRepository;
import com.aivle8.backend.repository.BookRepository;
import com.aivle8.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookLikeServiceImpl implements BookLikeService {

    private final BookRepository bookRepository;
    private final BookLikeRepository bookLikeRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);

        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    @Override
    public void likeBook(Long bookId) {
        User user = getCurrentUser();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));

        boolean alreadyLiked = book.getBookLikes().stream()
                .anyMatch(bl -> bl.getUser().getUserId().equals(user.getUserId()));
        if (alreadyLiked) throw new CustomException(ErrorCode.BAD_REQUEST, "이미 좋아요한 도서입니다.");

        BookLike bookLike = BookLike.builder()
                .book(book)
                .user(user)
                .build();
        bookLikeRepository.save(bookLike);
    }

    @Override
    public void unlikeBook(Long bookId) {
        User user = getCurrentUser();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));

        bookLikeRepository.findByUserAndBook(user, book)
                .ifPresentOrElse(
                        bookLikeRepository::delete,
                        () -> { throw new CustomException(ErrorCode.BAD_REQUEST, "좋아요하지 않은 도서입니다."); }
                );
    }
}
