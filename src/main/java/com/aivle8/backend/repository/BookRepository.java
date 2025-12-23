package com.aivle8.backend.repository;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // ⭐ 이거 추가해야 함
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // ⭐ 파라미터 바인딩용 추가
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // ⭐ 트랜잭션 추가

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("SELECT b FROM Book b ORDER BY b.viewCount DESC")
    List<Book> findTopByViewCount(Pageable pageable);

    List<Book> findByUser(User user); // 내가 쓴 도서 조회용

    @Modifying // 데이터를 수정할 때 필수
    @Transactional // 수정 쿼리 실행 시 필수
    @Query("UPDATE Book b SET b.viewCount = :viewCount WHERE b.bookId = :bookId")
    void updateViewCount(@Param("bookId") Long bookId, @Param("viewCount") Long viewCount);
}
