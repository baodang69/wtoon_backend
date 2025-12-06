package com.example.wtoon.repository.custom;

import com.example.wtoon.entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StoryRepositoryCustom {
    List<Story> getStoriesToSyncChapters(Pageable pageable);
    
    Page<Story> getAllStoriesWithCategories(Pageable pageable);
    
    Optional<Story> getStoryDetailBySlug(String slug);
    
    Page<Story> getStoriesByCategoryId(String categoryId, Pageable pageable);

    Page<Story> getStoriesByDaysOfWeek(String daysOfWeek, Pageable pageable);
}
