// src/main/java/com/aivle8/backend/repository/BookLikeRepository.java
package com.aivle8.backend.repository;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.BookLike;
import com.aivle8.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookLikeRepository extends JpaRepository<BookLike, Long> {
    Optional<BookLike> findByUserAndBook(User user, Book book);
    void deleteByUserAndBook(User user, Book book);
    List<BookLike> findByUser(User user);
    long countByBook(Book book);
}
