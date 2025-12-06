package com.example.wtoon.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "chapter_image", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"chapter_id", "image_page"})
})
@Data
public class ChapterImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(nullable = false)
    private String domainCdn;

    @Column(nullable = false)
    private String chapterPath;

    @Column(nullable = false)
    private int imagePage;

    @Column(nullable = false)
    private String imageFile;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isLocalCached = false;
}