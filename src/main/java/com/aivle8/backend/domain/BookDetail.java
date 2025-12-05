package com.aivle8.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_details")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BookDetail {

    @Id
    private Long bookId; // PK이자 FK

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "book_id")
    private Book book;

    @Lob
    @Column(nullable = false)
    private String content;

    @Lob
    @Column(nullable = false, length = 200)
    private String description;
}
