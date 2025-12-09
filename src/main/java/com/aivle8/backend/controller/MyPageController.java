// src/main/java/com/aivle8/backend/controller/MyPageController.java
package com.aivle8.backend.controller;

import com.aivle8.backend.dto.BookResponseDto;
import com.aivle8.backend.global.common.BaseResponse;
import com.aivle8.backend.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// src/main/java/com/aivle8/backend/controller/MyPageController.java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/mybooks")
    public BaseResponse<List<BookResponseDto>> getMyBooks() {
        List<BookResponseDto> result = myPageService.getMyBooks();
        return BaseResponse.success(result); // 기존 private 생성자 호출 대신 static 메서드 사용
    }

    @GetMapping("/likebooks")
    public BaseResponse<List<BookResponseDto>> getLikedBooks() {
        List<BookResponseDto> result = myPageService.getLikedBooks();
        return BaseResponse.success(result); // static 메서드 사용
    }
}

