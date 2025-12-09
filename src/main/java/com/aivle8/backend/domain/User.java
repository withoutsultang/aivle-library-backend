package com.aivle8.backend.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = true)
    private String naverId; // 추가

    @Column(nullable = true, length = 500)
    private String profileImage;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // --- User ↔ Book (1:N) ---
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Book> books;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference // User → BookLike 직렬화 허용
    private List<BookLike> likes;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
