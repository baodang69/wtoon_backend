package com.example.wtoon.repository;

import com.example.wtoon.entity.Chapter;
import com.example.wtoon.entity.ChapterImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterImageRepository extends JpaRepository<ChapterImage, Long> {
    // Spring Data JPA derived query method - không cần custom implementation
    List<ChapterImage> findAllByChapterOrderByImagePageAsc(Chapter chapter);
}