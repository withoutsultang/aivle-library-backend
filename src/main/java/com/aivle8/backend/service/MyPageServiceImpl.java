// src/main/java/com/aivle8/backend/service/MyPageServiceImpl.java
package com.aivle8.backend.service;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.BookLike;
import com.aivle8.backend.domain.User;
import com.aivle8.backend.dto.BookResponseDto;
import com.aivle8.backend.global.exception.CustomException;
import com.aivle8.backend.global.exception.ErrorCode;
import com.aivle8.backend.repository.BookLikeRepository;
import com.aivle8.backend.repository.BookRepository;
import com.aivle8.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

// src/main/java/com/aivle8/backend/service/MyPageServiceImpl.java
@Service
@RequiredArgsConstructor
@Transactional
public class MyPageServiceImpl implements MyPageService {

    private final BookRepository bookRepository;
    private final BookLikeRepository bookLikeRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private BookResponseDto coverUrl(BookResponseDto dto) {
        dto.setCoverImageUrl(s3Service.getPresignedUrl(dto.getCoverImageUrl()));
        return dto;
    }

    private User getCurrentUser() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);

        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    @Override
    public List<BookResponseDto> getMyBooks() {
        User user = getCurrentUser();
        return bookRepository.findByUser(user).stream()
                .map(BookResponseDto::fromEntity).map(this::coverUrl) // Book → BookResponseDto
                .toList();
    }

    @Override
    public List<BookResponseDto> getLikedBooks() {
        User user = getCurrentUser();
        return bookLikeRepository.findByUser(user).stream()
                .map(BookResponseDto::fromBookLikeEntity).map(this::coverUrl) // BookLike → BookResponseDto
                .toList();
    }
}
