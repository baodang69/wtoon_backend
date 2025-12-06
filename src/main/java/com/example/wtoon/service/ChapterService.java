package com.example.wtoon.service;

import com.example.wtoon.dto.external.ChapterContentResponse;
import com.example.wtoon.entity.Chapter;
import com.example.wtoon.entity.ChapterImage;
import com.example.wtoon.exception.ExternalApiException;
import com.example.wtoon.exception.ResourceNotFoundException;
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

        // Tìm chapter, throw 404 nếu không có
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", chapterId));

        log.info("Found chapter: {}, isContentSynced: {}", chapter.getChapterName(), chapter.isContentSynced());

        // Crawl on-demand nếu chưa sync
        if (!chapter.isContentSynced()) {
            log.info("Lần đầu đọc Chapter {}. Đang crawl từ API gốc...", chapterId);
            fetchAndSaveChapterImages(chapter);
        } else {
            log.info("Chapter {} đã được cache. Đang lấy từ DB...", chapterId);
        }

        List<ChapterImage> images = chapterImageRepository.findAllByChapterOrderByImagePageAsc(chapter);
        log.info("Found {} images for chapter {}", images.size(), chapterId);

        return images.stream()
                .map(image -> String.format("%s/%s/%s",
                        image.getDomainCdn(),
                        image.getChapterPath(),
                        image.getImageFile()))
                .collect(Collectors.toList());
    }

    @Transactional
    protected void fetchAndSaveChapterImages(Chapter chapter) {
        log.info("Fetching and saving chapter images for chapter: {}", chapter.getId());

        String apiUrl = chapter.getChapterApiData();
        log.info("Calling API URL: {}", apiUrl);

        ChapterContentResponse response;
        try {
            response = webClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToMono(ChapterContentResponse.class)
                    .block();
        } catch (Exception e) {
            throw new ExternalApiException("Failed to call chapter API: " + apiUrl, e);
        }

        // Validate response
        if (response == null || response.getData() == null || response.getData().getItem() == null) {
            throw new ExternalApiException("Invalid response from chapter API");
        }

        ChapterContentResponse.ChapterContentData data = response.getData();
        List<ChapterContentResponse.ImageFile> imageFiles = data.getItem().getChapterImage();

        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new ExternalApiException("No images found in chapter API response");
        }

        String domainCdn = data.getDomainCdn();
        String chapterPath = data.getItem().getChapterPath();
        log.info("Domain CDN: {}, Chapter Path: {}, Images: {}", domainCdn, chapterPath, imageFiles.size());

        // Build và save images
        List<ChapterImage> imagesToSave = new ArrayList<>();
        for (ChapterContentResponse.ImageFile imgFile : imageFiles) {
            ChapterImage newImage = new ChapterImage();
            newImage.setChapter(chapter);
            newImage.setDomainCdn(domainCdn);
            newImage.setChapterPath(chapterPath);
            newImage.setImagePage(imgFile.getImagePage());
            newImage.setImageFile(imgFile.getImageFile());
            imagesToSave.add(newImage);
        }

        chapterImageRepository.saveAll(imagesToSave);
        chapter.setContentSynced(true);
        chapterRepository.save(chapter);
        log.info("Saved {} images for chapter {}", imagesToSave.size(), chapter.getId());
    }
}