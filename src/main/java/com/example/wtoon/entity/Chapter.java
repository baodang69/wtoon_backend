package com.example.wtoon.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "chapter", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"story_id", "chapter_name"}),
        @UniqueConstraint(columnNames = {"chapterApiData"})
})
@Data
public class Chapter {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    @JsonIgnore
    private Story story;

    @Column(nullable = false)
    private String chapterName;

    @Column(nullable = false)
    private String chapterTitle;

    @Column(nullable = false, unique = true)
    private String chapterApiData;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isContentSynced = false;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();
}