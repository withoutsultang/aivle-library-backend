package com.aivle8.backend.config;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.BookDetail;
import com.aivle8.backend.domain.User;
import com.aivle8.backend.repository.BookRepository;
import com.aivle8.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0 || bookRepository.count() > 0) {
            return;
        }
        User user = User.builder()
                .nickname("tempUser")
                .email("temp@example.com")
                .profileImage("https://picsum.photos/200/300")
                .build();

        userRepository.save(user);

        Book book = Book.builder()
                .title("테스트 책 1")
                .category("IT")
                .coverImageUrl("https://picsum.photos/200/300")
                .user(user)
                .build();

        BookDetail detail = BookDetail.builder()
                .book(book)
                .description("이것은 테스트용 책입니다.")
                .content("테스트 내용 본문입니다.")
                .build();

        book.setBookDetail(detail);

        bookRepository.save(book);

        for (int i = 2; i <= 5; i++) {
            Book b = Book.builder()
                    .title("테스트 책 " + i)
                    .category("IT")
                    .coverImageUrl("https://picsum.photos/200/300")
                    .user(user)
                    .build();

            BookDetail d = BookDetail.builder()
                    .book(b)
                    .description("테스트 책 " + i + " 설명")
                    .content("테스트 책 " + i + " 내용 본문")
                    .build();

            b.setBookDetail(d);

            bookRepository.save(b);
        }
    }
}
