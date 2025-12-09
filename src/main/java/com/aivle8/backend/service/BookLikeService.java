// src/main/java/com/aivle8/backend/service/BookLikeService.java
package com.aivle8.backend.service;

public interface BookLikeService {
    void likeBook(Long bookId);
    void unlikeBook(Long bookId);
}
