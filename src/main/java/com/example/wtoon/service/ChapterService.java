package com.example.wtoon.service;

import com.example.wtoon.dto.request.content.ChapterContentResponse;
import com.example.wtoon.entity.Chapter;
import com.example.wtoon.entity.ChapterImage;
import com.example.wtoon.repository.ChapterImageRepository;
import com.example.wtoon.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final ChapterImageRepository chapterImageRepository;
    private final WebClient webClient;

    public List<String> getChapterContent(String chapterId) {
        log.info("Getting chapter content for ID: {}", chapterId);
        
        try {
            Chapter chapter = chapterRepository.findById(chapterId)
                    .orElseThrow(() -> new RuntimeException("Chapter not found with id: " + chapterId));
            
            log.info("Found chapter: {}, isContentSynced: {}", chapter.getChapterName(), chapter.isContentSynced());

            if (!chapter.isContentSynced()) {
                log.info("Lần đầu đọc Chapter {}. Đang crawl từ API gốc...", chapterId);
                try {
                    fetchAndSaveChapterImages(chapter);
                } catch (Exception e) {
                    log.error("Lỗi khi crawl on-demand chapter {}: {}", chapterId, e.getMessage(), e);
                    throw new RuntimeException("Failed to fetch chapter content from source: " + e.getMessage());
                }
            } else {
                log.info("Chapter {} đã được cache. Đang lấy từ DB...", chapterId);
            }
            
            List<ChapterImage> images = chapterImageRepository.findAllByChapterOrderByImagePageAsc(chapter);
            log.info("Found {} images for chapter {}", images.size(), chapterId);
            
            if (images.isEmpty()) {
                log.warn("No images found for chapter {}", chapterId);
            }
            
            return images.stream()
                    .map(image -> {
                        String imageUrl = String.format("%s/%s/%s",
                                image.getDomainCdn(),
                                image.getChapterPath(),
                                image.getImageFile());
                        log.debug("Image URL: {}", imageUrl);
                        return imageUrl;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting chapter content for ID {}: {}", chapterId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    protected void fetchAndSaveChapterImages(Chapter chapter) {
        log.info("Fetching and saving chapter images for chapter: {}", chapter.getId());
        
        try {
            // 1. Gọi API gốc (link lưu trong chapter.chapterApiData)
            String apiUrl = chapter.getChapterApiData();
            log.info("Calling API URL: {}", apiUrl);
            
            ChapterContentResponse response = webClient.get()
                    .uri(apiUrl) // Dùng link API đã lưu
                    .retrieve()
                    .bodyToMono(ChapterContentResponse.class)
                    .block(); // Chờ (block) vì người dùng đang chờ

            if (response == null) {
                throw new RuntimeException("API response is null");
            }
            
            if (response.getData() == null) {
                throw new RuntimeException("API response data is null");
            }
            
            if (response.getData().getItem() == null) {
                throw new RuntimeException("API response item is null");
            }

            ChapterContentResponse.ChapterContentData data = response.getData();
            String domainCdn = data.getDomainCdn();
            String chapterPath = data.getItem().getChapterPath();
            
            log.info("Domain CDN: {}, Chapter Path: {}", domainCdn, chapterPath);

            List<ChapterImage> imagesToSave = new ArrayList<>();
            List<ChapterContentResponse.ImageFile> imageFiles = data.getItem().getChapterImage();
            
            if (imageFiles == null || imageFiles.isEmpty()) {
                throw new RuntimeException("No images found in API response");
            }
            
            log.info("Found {} images to save", imageFiles.size());

            for (ChapterContentResponse.ImageFile imgFile : imageFiles) {
                ChapterImage newImage = new ChapterImage();
                newImage.setChapter(chapter);
                newImage.setDomainCdn(domainCdn);
                newImage.setChapterPath(chapterPath);
                newImage.setImagePage(imgFile.getImagePage());
                newImage.setImageFile(imgFile.getImageFile());

                imagesToSave.add(newImage);
                log.debug("Added image: page={}, file={}", imgFile.getImagePage(), imgFile.getImageFile());
            }

            List<ChapterImage> savedImages = chapterImageRepository.saveAll(imagesToSave);
            log.info("Saved {} images to database", savedImages.size());
            
            chapter.setContentSynced(true);
            chapterRepository.save(chapter);
            log.info("Marked chapter {} as content synced", chapter.getId());
            
        } catch (Exception e) {
            log.error("Error fetching and saving chapter images for chapter {}: {}", chapter.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to fetch and save chapter images: " + e.getMessage(), e);
        }
    }
}