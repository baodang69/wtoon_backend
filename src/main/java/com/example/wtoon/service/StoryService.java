package com.example.wtoon.service;

import com.example.wtoon.dto.response.StoryDetailResponseDTO;
import com.example.wtoon.dto.response.StorySummaryResponseDTO;
import com.example.wtoon.entity.Story;
import com.example.wtoon.mapper.StoryMapper;
import com.example.wtoon.repository.StoryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoryService {

    private final StoryRepository storyRepository;
    private final StoryMapper storyMapper;
    private final CrawlService crawlService;
    private final EntityManager entityManager;

    public Page<StorySummaryResponseDTO> getStories(Pageable pageable) {
        Page<Story> storiesPage = storyRepository.getAllStoriesWithCategories(pageable);
        return storiesPage.map(storyMapper::toSummaryDto);
    }

    @Transactional
    public Optional<StoryDetailResponseDTO> getStoryBySlug(String slug) {
        Optional<Story> storyOpt = storyRepository.getStoryDetailBySlug(slug);
        
        if (storyOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Story story = storyOpt.get();
        
        // Crawl on-demand nếu chưa có chapters
        if (story.getChapters() == null || story.getChapters().isEmpty()) {
            log.info("Story {} chưa có chapters. Đang crawl on-demand...", slug);
            try {
                crawlService.crawlChaptersForStory(story);
                entityManager.flush();
                entityManager.refresh(story);
                log.info("Đã crawl thành công chapters cho story {}", slug);
            } catch (Exception e) {
                log.error("Lỗi khi crawl on-demand chapters cho story {}: {}", slug, e.getMessage());
            }
        }
        
        return Optional.of(storyMapper.toDetailDto(story));
    }

    public Page<StorySummaryResponseDTO> getStoriesByCategory(String categoryId, Pageable pageable) {
        Page<Story> storiesPage = storyRepository.getStoriesByCategoryId(categoryId, pageable);
        return storiesPage.map(storyMapper::toSummaryDto);
    }

    public Page<StorySummaryResponseDTO> getStoriesByDaysOfWeek(String dow, Pageable pageable) {
        Page<Story> storiesPage = storyRepository.getStoriesByDaysOfWeek(dow, pageable);
        return storiesPage.map(storyMapper::toSummaryDto);
    }

    @Async
    @Transactional
    public void incrementViewCount(String slug) {
        storyRepository.findBySlug(slug).ifPresent(story -> {
            story.setViewCount(story.getViewCount() == null ? 1 : story.getViewCount() + 1);
            storyRepository.save(story);
        });
    }
}
