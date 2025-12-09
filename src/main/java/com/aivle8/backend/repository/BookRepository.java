package com.aivle8.backend.repository;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("SELECT b FROM Book b ORDER BY b.viewCount DESC")
    List<Book> findTopByViewCount(Pageable pageable);
    List<Book> findByUser(User user); // 내가 쓴 도서 조회용

}
