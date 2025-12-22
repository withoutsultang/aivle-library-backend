package com.aivle8.backend;

import com.aivle8.backend.domain.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BookTest {

    @Test
    @DisplayName("Book 생성 시 조회수(viewCount)는 0으로 초기화됨을 보장한다")
    void bookCreation_initializesViewCount() {
        Book book = Book.builder()
                .title("클린 코드")
                .category("개발")
                .build();

        book.onCreate();

        assertEquals(0, book.getViewCount());
    }

    @Test
    @DisplayName("조회수 증가 메서드는 호출 횟수만큼 정확히 증가함을 보장한다")
    void increaseViewCount_worksCorrectly() {
        Book book = Book.builder()
                .title("클린 코드")
                .category("개발")
                .build();

        book.onCreate();
        book.increaseViewCount();
        book.increaseViewCount();

        assertEquals(2, book.getViewCount());
    }

    @Test
    @DisplayName("Book 업데이트 시 수정 시각(bookUpdated)이 반드시 기록됨을 보장한다")
    void onUpdate_setsUpdatedTimestamp() {
        Book book = Book.builder()
                .title("클린 코드")
                .category("개발")
                .build();

        book.onUpdate();

        assertNotNull(book.getBookUpdated());
    }
}
