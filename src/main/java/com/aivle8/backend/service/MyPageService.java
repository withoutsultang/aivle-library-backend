// src/main/java/com/aivle8/backend/service/MyPageService.java
package com.aivle8.backend.service;

import com.aivle8.backend.dto.BookResponseDto;

import java.util.List;

public interface MyPageService {
    List<BookResponseDto> getMyBooks();      // 내가 쓴 도서
    List<BookResponseDto> getLikedBooks();   // 내가 좋아요 누른 도서
}
