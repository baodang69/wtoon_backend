package com.example.wtoon.service;

import com.example.wtoon.dto.external.WtoonListResponse;
import com.example.wtoon.dto.external.StoryDetailResponse;
import com.example.wtoon.entity.Category;
import com.example.wtoon.entity.Chapter;
import com.example.wtoon.entity.Story;
import com.example.wtoon.repository.CategoryRepository;
import com.example.wtoon.repository.ChapterRepository;
import com.example.wtoon.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlService {

    private final WebClient webClient;
    private final StoryRepository storyRepository;
    private final CategoryRepository categoryRepository;
    private final ChapterRepository chapterRepository;

    private static final String API_BASE_URL = "https://otruyenapi.com";
    private static final String API_PATH_LIST = "/v1/api/danh-sach/danh-sach";
    private static final String API_PATH_DETAIL = "/v1/api/truyen-tranh";
    private static final String BASE_IMAGE_PATH = "/uploads/comics/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    
    private static final int MAX_QUICK_PAGES = 5;
    private static final int JOB_3_PAGE_SIZE = 20;
    private static final int DELAY_BETWEEN_REQUESTS = 500;
    private static final int DELAY_ON_ERROR = 2000;

    // ==================== SCHEDULED JOBS ====================

    @Scheduled(cron = "0 0 15 * * *")
    public void initialFullCrawlJob() {
        log.info("--- BẮT ĐẦU JOB 1 (Full Crawl) ---");
        crawlStoryList(Integer.MAX_VALUE, "Job 1");
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void quickUpdateJob() {
        log.info("--- BẮT ĐẦU JOB 2 (Quick Update - {} trang) ---", MAX_QUICK_PAGES);
        crawlStoryList(MAX_QUICK_PAGES, "Job 2");
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void chapterSyncJob() {
        log.info("--- BẮT ĐẦU JOB 3 (Sync Chapter) ---");

        Pageable pageable = PageRequest.of(0, JOB_3_PAGE_SIZE);
        List<Story> storiesToUpdate = storyRepository.getStoriesToSyncChapters(pageable);

        if (storiesToUpdate.isEmpty()) {
            log.info("Job 3: Không có truyện nào cần đồng bộ chapter.");
            return;
        }

        WebClient crawlClient = createCrawlClient();
        for (Story story : storiesToUpdate) {
            try {
                processAndSaveChapters(crawlClient, story);
                Thread.sleep(DELAY_BETWEEN_REQUESTS);
            } catch (Exception e) {
                log.error("Job 3: Lỗi khi xử lý slug {}: {}", story.getSlug(), e.getMessage());
            }
        }
        log.info("--- HOÀN TẤT JOB 3 ---");
    }

    // ==================== PUBLIC METHODS ====================

    /**
     * Crawl chapters cho một story cụ thể (dùng cho on-demand crawl)
     */
    @Transactional
    public void crawlChaptersForStory(Story story) throws Exception {
        WebClient crawlClient = createCrawlClient();
        processAndSaveChapters(crawlClient, story);
    }

    // ==================== PRIVATE HELPERS ====================

    private WebClient createCrawlClient() {
        return webClient.mutate()
                .baseUrl(API_BASE_URL)
                .defaultHeader("User-Agent", USER_AGENT)
                .build();
    }

    /**
     * Crawl danh sách truyện với số trang tối đa
     */
    private void crawlStoryList(int maxPages, String jobName) {
        int currentPage = 1;
        int totalPages = 1;
        WebClient crawlClient = createCrawlClient();

        do {
            final int pageToCrawl = currentPage;
            try {
                WtoonListResponse response = crawlClient.get()
                        .uri(uriBuilder -> uriBuilder.path(API_PATH_LIST).queryParam("page", pageToCrawl).build())
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError(), this::handleError)
                        .bodyToMono(WtoonListResponse.class)
                        .block();

                if (response == null || response.getData() == null || response.getData().getItems() == null) {
                    log.warn("{}: Không nhận được dữ liệu hợp lệ tại trang {}.", jobName, pageToCrawl);
                    break;
                }

                // Tính tổng số trang ở lần đầu
                if (currentPage == 1 && maxPages == Integer.MAX_VALUE) {
                    var pagination = response.getData().getParams().getPagination();
                    totalPages = (int) Math.ceil((double) pagination.getTotalItems() / pagination.getTotalItemsPerPage());
                    log.info("{}: Tổng số trang cần crawl: {}", jobName, totalPages);
                } else if (maxPages != Integer.MAX_VALUE) {
                    totalPages = maxPages;
                }

                String cdnDomain = response.getData().getDomainCdnImage();
                for (WtoonListResponse.StoryItem item : response.getData().getItems()) {
                    try {
                        processAndSaveStory(item, cdnDomain);
                    } catch (Exception e) {
                        log.error("{}: LỖI LƯU truyện {}: {}", jobName, item.getName(), e.getMessage());
                    }
                }
                
                log.info("{}: Đã xử lý thành công trang {}/{}", jobName, pageToCrawl, totalPages);
                currentPage++;
                Thread.sleep(DELAY_BETWEEN_REQUESTS);

            } catch (Exception e) {
                log.error("{}: Lỗi tại trang {}: {}", jobName, pageToCrawl, e.getMessage());
                currentPage++;
                try {
                    Thread.sleep(DELAY_ON_ERROR);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } while (currentPage <= totalPages);
        
        log.info("--- HOÀN TẤT {}. Đã quét {} trang ---", jobName, currentPage - 1);
    }

    @Transactional
    public void processAndSaveStory(WtoonListResponse.StoryItem item, String cdnDomain) {
        Story story = storyRepository.findById(item.getId()).orElse(new Story());

        story.setId(item.getId());
        story.setSlug(item.getSlug());
        story.setName(item.getName());
        story.setStatus(item.getStatus());
        story.setCdnDomain(cdnDomain);
        story.setThumbUrl(BASE_IMAGE_PATH + item.getThumbUrl());
        story.setLastSyncedAt(LocalDateTime.now());

        // Parse updatedAt
        try {
            story.setUpdatedAt(Instant.parse(item.getUpdatedAt())
                    .atZone(ZoneId.of("UTC")).toLocalDateTime());
        } catch (Exception e) {
            log.warn("Lỗi parse updatedAt cho truyện {}", item.getName());
            if (story.getUpdatedAt() == null) {
                story.setUpdatedAt(LocalDateTime.now());
            }
        }

        // Xử lý Categories
        Set<Category> managedCategories = new HashSet<>();
        for (WtoonListResponse.CategoryItem catDto : item.getCategory()) {
            Category category = categoryRepository.findById(catDto.getId())
                    .orElseGet(() -> {
                        Category newCat = new Category();
                        newCat.setId(catDto.getId());
                        newCat.setName(catDto.getName());
                        newCat.setSlug(catDto.getSlug());
                        return newCat;
                    });
            managedCategories.add(category);
        }
        story.setCategories(managedCategories);

        storyRepository.save(story);
        log.debug("Đã lưu/cập nhật Story ID: {}", story.getId());
    }

    @Transactional
    public void processAndSaveChapters(WebClient crawlClient, Story story) throws Exception {
        log.info("Đang xử lý chapters cho truyện: {}", story.getSlug());

        StoryDetailResponse response = crawlClient.get()
                .uri(API_PATH_DETAIL + "/" + story.getSlug())
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), this::handleError)
                .bodyToMono(StoryDetailResponse.class)
                .block();

        if (response == null || response.getData() == null || response.getData().getItem() == null) {
            log.warn("Không tìm thấy data detail cho slug: {}", story.getSlug());
            story.setLastChapterSyncAt(LocalDateTime.now());
            storyRepository.save(story);
            return;
        }

        var item = response.getData().getItem();
        
        // Cập nhật thông tin truyện
        story.setDescription(item.getContent());
        if (item.getAuthor() != null && !item.getAuthor().isEmpty()) {
            story.setAuthor(String.join(", ", item.getAuthor()));
        }

        // Lưu chapters mới
        int newChaptersCount = 0;
        if (item.getChapters() != null && !item.getChapters().isEmpty()) {
            List<StoryDetailResponse.ChapterItem> chapterItems = item.getChapters().get(0).getServerData();

            for (StoryDetailResponse.ChapterItem dto : chapterItems) {
                String chapterId = extractIdFromApiData(dto.getChapterApiData());
                if (chapterId == null) {
                    log.warn("Không thể tách ID từ: {}", dto.getChapterApiData());
                    continue;
                }

                if (!chapterRepository.existsById(chapterId)) {
                    Chapter newChapter = new Chapter();
                    newChapter.setId(chapterId);
                    newChapter.setStory(story);
                    newChapter.setChapterName(dto.getChapterName());
                    newChapter.setChapterTitle(dto.getChapterTitle());
                    newChapter.setChapterApiData(dto.getChapterApiData());
                    chapterRepository.save(newChapter);
                    newChaptersCount++;
                }
            }
        }

        story.setLastChapterSyncAt(LocalDateTime.now());
        storyRepository.save(story);
        log.info("Hoàn tất {}. Phát hiện {} chapter mới.", story.getSlug(), newChaptersCount);
    }

    private String extractIdFromApiData(String apiDataUrl) {
        if (apiDataUrl == null || apiDataUrl.isEmpty() || !apiDataUrl.contains("/")) {
            return null;
        }
        return apiDataUrl.substring(apiDataUrl.lastIndexOf('/') + 1);
    }

    private Mono<? extends Throwable> handleError(org.springframework.web.reactive.function.client.ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    log.error("HTTP ERROR {}: {}", response.statusCode().value(), errorBody);
                    return Mono.error(new RuntimeException("API Client Error: " + response.statusCode().value()));
                });
    }
}
