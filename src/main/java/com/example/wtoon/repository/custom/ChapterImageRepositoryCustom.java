package com.example.wtoon.repository.custom;

import com.example.wtoon.entity.Chapter;
import com.example.wtoon.entity.ChapterImage;

import java.util.List;

public interface ChapterImageRepositoryCustom {
    List<ChapterImage> findAllByChapterOrderByImagePageAsc(Chapter chapter);
}
