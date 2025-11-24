package com.example.wtoon.repository.custom;

import com.example.wtoon.entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StoryRepositoryCustom {
    List<Story> findStoriesToSyncChapters(Pageable pageable);
    
    Page<Story> findAllWithCategories(Pageable pageable);
    
    Optional<Story> findBySlugWithDetails(String slug);
    
    Page<Story> findAllByCategory(String categoryId, Pageable pageable);
}
