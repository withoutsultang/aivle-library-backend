// src/main/java/com/aivle8/backend/controller/BookLikeController.java
package com.aivle8.backend.controller;

import com.aivle8.backend.global.common.BaseResponse;
import com.aivle8.backend.service.BookLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookLikeController {

    private final BookLikeService bookLikeService;

    @PostMapping("/{bookId}/like")
    public BaseResponse<String> likeBook(@PathVariable Long bookId) {
        bookLikeService.likeBook(bookId);
        return BaseResponse.success("좋아요 완료");
    }

    @PostMapping("/{bookId}/unlike")
    public BaseResponse<String> unlikeBook(@PathVariable Long bookId) {
        bookLikeService.unlikeBook(bookId);
        return BaseResponse.success("좋아요 취소 완료");
    }
}