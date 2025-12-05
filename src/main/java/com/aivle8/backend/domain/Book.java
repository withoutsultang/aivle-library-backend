package com.aivle8.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                // PK

    @Column(nullable = false, length = 200)
    private String title;           // 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;         // 본문

    @Column(nullable = false, length = 500)
    private String description;     // 책 설명(요약)

    @Column(nullable = false, length = 100)
    private String category;        // 카테고리

    @Column(nullable = true, length = 500)
    private String coverImageUrl;   // AI로 생성된 표지 이미지 URL

    @Column(nullable = false)
    private int viewCount;          // 조회수

    @Column(nullable = false)
    private LocalDateTime bookCreated; // 등록일

    @Column(nullable = false)
    private LocalDateTime bookUpdated; // 수정일

    // --- 엔티티 저장/수정 시 자동 시간 설정 ---
    @PrePersist
    public void onCreate() {
        this.bookCreated = LocalDateTime.now();
        this.bookUpdated = LocalDateTime.now();
        this.viewCount = 0; // 신규 생성 시 조회수 0
    }

    @PreUpdate
    public void onUpdate() {
        this.bookUpdated = LocalDateTime.now();
    }
}