package com.aivle8.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "books")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String category; // 카테고리 추가

    @Column(nullable = false)
    private int viewCount;

    @Column(nullable = false)
    private LocalDateTime bookCreated;

    @Column(nullable = false)
    private LocalDateTime bookUpdated;

    // --- 연관관계 ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private BookDetail bookDetail;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookImage> bookImages;

    @PrePersist
    public void onCreate() {
        this.bookCreated = LocalDateTime.now();
        this.bookUpdated = LocalDateTime.now();
        this.viewCount = 0;
    }

    @PreUpdate
    public void onUpdate() {
        this.bookUpdated = LocalDateTime.now();
    }
}
