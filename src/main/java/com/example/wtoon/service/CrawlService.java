package com.example.wtoon.service;

import com.example.wtoon.dto.request.WtoonResponse;
import com.example.wtoon.dto.request.detail.ChapterDTO;
import com.example.wtoon.dto.request.detail.StoryDetailResponse;
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
    private static final int MAX_QUICK_PAGES = 5;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int JOB_3_PAGE_SIZE = 20;

    // --- JOB 1 (VÉT CẠN) ---
    @Scheduled(cron = "0 0 3 * * *")
    public void initialFullCrawlJob() {
        log.info("--- BẮT ĐẦU JOB 1 (Full Crawl) ---");
        int currentPage = 1;
        int totalPages = 1;

        WebClient crawlClient = webClient.mutate()
                .baseUrl(API_BASE_URL)
                .defaultHeader("User-Agent", USER_AGENT)
                .build();

        do {
            final int pageToCrawl = currentPage;
            try {
                WtoonResponse response = crawlClient.get()
                        .uri(uriBuilder -> uriBuilder.path(API_PATH_LIST).queryParam("page", pageToCrawl).build())
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError(), this::handleError)
                        .bodyToMono(WtoonResponse.class)
                        .block();

                if (response == null || response.getData() == null || response.getData().getItems() == null) {
                    log.warn("Job 1: Không nhận được dữ liệu hợp lệ tại trang {}.", pageToCrawl);
                    break;
                }

                if (currentPage == 1) {
                    totalPages = (int) Math.ceil((double) response.getData().getParams().getPagination().getTotalItems() / response.getData().getParams().getPagination().getTotalItemsPerPage());
                    log.info("Job 1: Tổng số trang cần crawl: {}", totalPages);
                }

                String cdnDomain = response.getData().getDomainCdnImage();
                for (WtoonResponse.StoryItem item : response.getData().getItems()) {
                    try {
                        processAndSaveStory(item, cdnDomain);
                    } catch (Exception e) {
                        log.error("Job 1: LỖI LƯU truyện {}: {}", item.getName(), e.getMessage());
                    }
                }
                log.info("Job 1: Đã xử lý thành công trang {}/{}", pageToCrawl, totalPages);
                currentPage++;
                Thread.sleep(500);

            } catch (Exception e) {
                log.error("Job 1: Lỗi nghiêm trọng tại trang {}: {}", pageToCrawl, e.getMessage());
                currentPage++;
                try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
            }
        } while (currentPage <= totalPages);
        log.info("--- HOÀN TẤT JOB 1. Đã quét {} trang ---", currentPage - 1);
    }

    // --- JOB 2 (UPDATE NHANH) ---
    @Scheduled(cron = "0 */10 * * * *")
    public void quickUpdateJob() {
        log.info("--- BẮT ĐẦU JOB 2 (Quick Update - {} trang) ---", MAX_QUICK_PAGES);
        int currentPage = 1;

        WebClient crawlClient = webClient.mutate()
                .baseUrl(API_BASE_URL)
                .defaultHeader("User-Agent", USER_AGENT)
                .build();

        do {
            final int pageToCrawl = currentPage;
            try {
                WtoonResponse response = crawlClient.get()
                        .uri(uriBuilder -> uriBuilder.path(API_PATH_LIST).queryParam("page", pageToCrawl).build())
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError(), this::handleError)
                        .bodyToMono(WtoonResponse.class)
                        .block();

                if (response == null || response.getData() == null || response.getData().getItems() == null) {
                    log.warn("Job 2: Không nhận được dữ liệu hợp lệ tại trang {}.", pageToCrawl);
                    break;
                }

                String cdnDomain = response.getData().getDomainCdnImage();
                for (WtoonResponse.StoryItem item : response.getData().getItems()) {
                    try {
                        processAndSaveStory(item, cdnDomain);
                    } catch (Exception e) {
                        log.error("Job 2: LỖI LƯU truyện {}: {}", item.getName(), e.getMessage());
                    }
                }
                log.info("Job 2: Đã xử lý thành công trang {}/{}.", pageToCrawl, MAX_QUICK_PAGES);
                currentPage++;
                Thread.sleep(500);

            } catch (Exception e) {
                log.error("Job 2: Lỗi xảy ra tại trang {}: {}", pageToCrawl, e.getMessage());
                currentPage++;
                try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
            }
        } while (currentPage <= MAX_QUICK_PAGES);
        log.info("--- HOÀN TẤT JOB 2. Đã quét {} trang ---", currentPage - 1);
    }

    // --- JOB 3 (SYNC CHAPTER) ---
    @Scheduled(cron = "0 */5 * * * *")
    public void chapterSyncJob() {
        log.info("--- BẮT ĐẦU JOB 3 (Sync Chapter) ---");

        Pageable pageable = PageRequest.of(0, JOB_3_PAGE_SIZE);
        List<Story> storiesToUpdate = storyRepository.getStoriesToSyncChapters(pageable);

        if (storiesToUpdate.isEmpty()) {
            log.info("Job 3: Không có truyện nào cần đồng bộ chapter.");
            return;
        }

        WebClient crawlClient = webClient.mutate()
                .baseUrl(API_BASE_URL)
                .defaultHeader("User-Agent", USER_AGENT)
                .build();

        for (Story story : storiesToUpdate) {
            try {
                processAndSaveChapters(crawlClient, story);
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("Job 3: Lỗi nghiêm trọng khi xử lý slug {}: {}", story.getSlug(), e.getMessage());
            }
        }
        log.info("--- HOÀN TẤT JOB 3 ---");
    }

    // --- HELPER CHO JOB 1 & 2 ---
    @Transactional
    public void processAndSaveStory(WtoonResponse.StoryItem item, String cdnDomain) {
        Story story = storyRepository.findById(item.getId()).orElse(new Story());

        // Map dữ liệu
        story.setId(item.getId());
        story.setSlug(item.getSlug());
        story.setName(item.getName());
        story.setStatus(item.getStatus());
        story.setCdnDomain(cdnDomain);
        story.setThumbUrl(BASE_IMAGE_PATH + item.getThumbUrl());
        story.setLastSyncedAt(LocalDateTime.now());

        // Parse updatedAt
        try {
            story.setUpdatedAt(Instant.parse(item.getUpdatedAt()).atZone(ZoneId.of("UTC")).toLocalDateTime());
        } catch (Exception e) {
            log.warn("Lỗi parse updatedAt cho truyện {}", item.getName());
            if (story.getUpdatedAt() == null) story.setUpdatedAt(LocalDateTime.now());
        }

        // Xử lý Categories (N:N)
        Set<Category> managedCategories = new HashSet<>();
        for (WtoonResponse.CategoryDTO catDto : item.getCategory()) {
            Category existingCategory = categoryRepository.findById(catDto.getId()).orElse(null);
            if (existingCategory == null) {
                Category newCat = new Category();
                newCat.setId(catDto.getId());
                newCat.setName(catDto.getName());
                newCat.setSlug(catDto.getSlug());
                managedCategories.add(newCat);
            } else {
                managedCategories.add(existingCategory);
            }
        }
        story.setCategories(managedCategories);

        storyRepository.save(story);
        log.info("Đã lưu/cập nhật Story ID: {}", story.getId());
    }

    @Transactional
    public void processAndSaveChapters(WebClient crawlClient, Story story) throws Exception {
        log.info("Job 3: Đang xử lý truyện: {}", story.getSlug());

        // 1. Gọi API Detail Truyện
        StoryDetailResponse response = crawlClient.get()
                .uri(API_PATH_DETAIL + "/" + story.getSlug())
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), this::handleError)
                .bodyToMono(StoryDetailResponse.class)
                .block();

        if (response == null || response.getData() == null || response.getData().getItem() == null) {
            log.warn("Job 3: Không tìm thấy data detail cho slug: {}", story.getSlug());
            story.setLastChapterSyncAt(LocalDateTime.now());
            storyRepository.save(story);
            return;
        }

        // 2. Cập nhật thêm thông tin truyện
        story.setDescription(response.getData().getItem().getContent());
        if (response.getData().getItem().getAuthor() != null && !response.getData().getItem().getAuthor().isEmpty()) {
            story.setAuthor(String.join(", ", response.getData().getItem().getAuthor()));
        }

        // 3. Lọc và lưu Chapter mới
        int newChaptersCount = 0;
        if (response.getData().getItem().getChapters() != null && !response.getData().getItem().getChapters().isEmpty()) {
            List<ChapterDTO> chapterDTOs = response.getData().getItem().getChapters().get(0).getServerData();

            for (ChapterDTO dto : chapterDTOs) {
                // SỬA: Lấy String ID từ link
                String chapterId = extractIdFromApiData(dto.getChapterApiData());
                if (chapterId == null) {
                    log.warn("Job 3: Không thể tách ID từ: {}", dto.getChapterApiData());
                    continue;
                }

                // SỬA: Kiểm tra bằng existsById (dùng String ID)
                if (!chapterRepository.existsById(chapterId)) {
                    Chapter newChapter = new Chapter();

                    newChapter.setId(chapterId); // SỬA: Set String ID làm khóa chính
                    newChapter.setStory(story);
                    newChapter.setChapterName(dto.getChapterName());
                    newChapter.setChapterTitle(dto.getChapterTitle());
                    newChapter.setChapterApiData(dto.getChapterApiData());

                    chapterRepository.save(newChapter);
                    newChaptersCount++;
                }
            }
        }

        // 4. Cập nhật mốc thời gian
        story.setLastChapterSyncAt(LocalDateTime.now());
        storyRepository.save(story);

        log.info("Job 3: Hoàn tất {}. Phát hiện {} chapter mới.", story.getSlug(), newChaptersCount);
    }

    // --- HELPER TÁCH ID (CẦN CHO JOB 3) ---
    private String extractIdFromApiData(String apiDataUrl) {
        if (apiDataUrl == null || apiDataUrl.isEmpty() || !apiDataUrl.contains("/")) {
            return null;
        }
        return apiDataUrl.substring(apiDataUrl.lastIndexOf('/') + 1);
    }

    // --- HELPER XỬ LÝ LỖI HTTP ---
    private Mono<? extends Throwable> handleError(org.springframework.web.reactive.function.client.ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    log.error("HTTP ERROR {}: {}", response.statusCode().value(), errorBody);
                    return Mono.error(new RuntimeException("API Client Error: " + response.statusCode().value()));
                });
    }
}