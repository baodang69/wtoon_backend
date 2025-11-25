package com.example.wtoon.service;

import com.example.wtoon.dto.response.StoryDetailDTO;
import com.example.wtoon.dto.response.StorySummaryDTO;
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
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoryService {

    private final StoryRepository storyRepository;
    private final StoryMapper storyMapper;
    private final CrawlService crawlService;
    private final EntityManager entityManager;
    
    private static final String API_BASE_URL = "https://otruyenapi.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    public Page<StorySummaryDTO> getStories(Pageable pageable) {
        Page<Story> storiesPage = storyRepository.getAllStoriesWithCategories(pageable);
        return storiesPage.map(storyMapper::toSummaryDto);
    }

    @Transactional
    public Optional<StoryDetailDTO> getStoryBySlug(String slug) {
        Optional<Story> storyOpt = storyRepository.getStoryDetailBySlug(slug);
        
        if (storyOpt.isPresent()) {
            Story story = storyOpt.get();
            
            // Check nếu chapters rỗng hoặc chưa sync -> crawl on-demand
            if (story.getChapters() == null || story.getChapters().isEmpty()) {
                log.info("Story {} chưa có chapters. Đang crawl on-demand...", slug);
                try {
                    // Tạo WebClient cho crawl
                    WebClient crawlClient = WebClient.builder()
                            .baseUrl(API_BASE_URL)
                            .defaultHeader("User-Agent", USER_AGENT)
                            .build();
                    
                    crawlService.processAndSaveChapters(crawlClient, story);
                    
                    // Đẩy tất cả thay đổi (Chapters vừa insert) xuống DB ngay lập tức & xóa cache của object story này và tải lại mới tinh từ DB
                    entityManager.flush();
                    entityManager.refresh(story);
                    log.info("Đã crawl thành công chapters cho story {}", slug);
                } catch (Exception e) {
                    log.error("Lỗi khi crawl on-demand chapters cho story {}: {}", slug, e.getMessage());
                    // Vẫn trả về story nhưng không có chapters
                }
            }
            return Optional.of(storyMapper.toDetailDto(story));
        }
        return Optional.empty();
    }

    public Page<StorySummaryDTO> getStoriesByCategory(String categoryId, Pageable pageable) {
        Page<Story> storiesPage = storyRepository.getStoriesByCategoryId(categoryId, pageable);
        return storiesPage.map(storyMapper::toSummaryDto);
    }

    @Async
    @Transactional
    public void incrementViewCount(String slug) {
        Optional<Story> storyOpt = storyRepository.findBySlug(slug);

        if (storyOpt.isPresent()) {
            Story story = storyOpt.get();
            long currentViews = (story.getViewCount() == null) ? 0 : story.getViewCount();
            story.setViewCount(currentViews + 1);
            storyRepository.save(story);
        }
    }
}