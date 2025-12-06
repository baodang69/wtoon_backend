package com.example.wtoon.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * DTO để parse response nội dung chapter từ API bên ngoài
 */
@Data
public class ChapterContentResponse {
    private ChapterContentData data;

    @Data
    public static class ChapterContentData {
        @JsonProperty("domain_cdn")
        private String domainCdn;
        private ContentItem item;
    }

    @Data
    public static class ContentItem {
        @JsonProperty("chapter_path")
        private String chapterPath;
        @JsonProperty("chapter_image")
        private List<ImageFile> chapterImage;
    }

    @Data
    public static class ImageFile {
        @JsonProperty("image_page")
        private int imagePage;
        @JsonProperty("image_file")
        private String imageFile;
    }
}
