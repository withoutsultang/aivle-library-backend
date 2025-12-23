package com.aivle8.backend.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @Column(nullable = false, length = 45)
    private String title;

    @Column(nullable = false, length = 45)
    private String category;

    @Column(nullable = true, length = 500)
    private String coverImageUrl;

    @Column(nullable = false)
    private int viewCount;

    /** 생성 시 자동 timestamp */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime bookCreated;

    /** 업데이트 시 자동 timestamp */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime bookUpdated;

    // --- 연관관계 ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private BookDetail bookDetail;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @JsonManagedReference // Book → BookLike 순환 방지
    private List<BookLike> bookLikes;

    @PrePersist
    public void onCreate() {
        this.viewCount = 0;
    }

    @PreUpdate
    public void onUpdate() {
        this.bookUpdated = LocalDateTime.now();
    }

    public void increaseViewCount() {
        this.viewCount++;
    }
}
