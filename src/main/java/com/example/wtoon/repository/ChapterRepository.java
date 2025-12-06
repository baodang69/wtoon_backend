package com.example.wtoon.repository;

import com.example.wtoon.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, String> {
    // existsById đã có sẵn từ JpaRepository
}
