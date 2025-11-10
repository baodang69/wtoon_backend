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

    private String name;
    private String status;
    private String thumbUrl;
    private String cdnDomain;
    private String description;
    private String author;

    // Ánh xạ N:N với Category
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
    private LocalDateTime updatedAt;
    private LocalDateTime lastSyncedAt;
    private LocalDateTime lastChapterSyncAt;
}