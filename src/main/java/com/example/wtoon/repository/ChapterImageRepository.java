package com.example.wtoon.repository;

import com.example.wtoon.entity.ChapterImage;
import com.example.wtoon.repository.custom.ChapterImageRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterImageRepository extends JpaRepository<ChapterImage, Long>, ChapterImageRepositoryCustom {
}