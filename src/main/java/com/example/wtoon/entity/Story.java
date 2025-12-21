package com.example.wtoon.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "story")
@Data
public class Story {
    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String status;

    @Column(nullable = true)
    private String thumbUrl;

    @Column(nullable = false)
    private String cdnDomain;

    @Column(nullable = true)
    private String description;

    @Column(nullable = true)
    private String author;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "story_category",
            joinColumns = @JoinColumn(name = "story_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    @OneToMany(mappedBy = "story", fetch = FetchType.LAZY)
    @OrderBy("chapterName ASC")
    private List<Chapter> chapters;

    @Column(name = "view_count", columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount = 0L;
    
    private LocalDateTime updatedAt;
    private LocalDateTime lastSyncedAt;
    private LocalDateTime lastChapterSyncAt;
}