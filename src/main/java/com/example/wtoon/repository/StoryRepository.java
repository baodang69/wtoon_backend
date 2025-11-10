package com.example.wtoon.repository;

import com.example.wtoon.entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, String> {
    @Query("SELECT s FROM Story s " +
            "WHERE s.lastChapterSyncAt IS NULL OR s.updatedAt > s.lastChapterSyncAt " +
            "ORDER BY s.updatedAt DESC")
    List<Story> findStoriesToSyncChapters(Pageable pageable);

    @Query("SELECT DISTINCT s FROM Story s LEFT JOIN FETCH s.categories")
    Page<Story> findAll(Pageable pageable);

    @Query("SELECT DISTINCT s FROM Story s " +
            "LEFT JOIN FETCH s.categories " +
            "LEFT JOIN FETCH s.chapters " +
            "WHERE s.slug = :slug")
    Optional<Story> findBySlugWithDetails(String slug);

    @Query("SELECT s FROM Story s JOIN s.categories c WHERE c.id = :categoryId")
    Page<Story> findAllByCategory(@Param("categoryId") String categoryId, Pageable pageable);

    Optional<Story> findBySlug(String slug);
}

