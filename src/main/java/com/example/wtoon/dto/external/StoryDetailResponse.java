package com.example.wtoon.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * DTO để parse response chi tiết truyện từ API bên ngoài
 */
@Data
public class StoryDetailResponse {
    private String status;
    private StoryDetailData data;

    @Data
    public static class StoryDetailData {
        private StoryDetailItem item;
    }

    @Data
    public static class StoryDetailItem {
        @JsonProperty("_id")
        private String id;
        private String name;
        private String content;
        private List<String> author;
        private List<ChapterGroup> chapters;
    }

    @Data
    public static class ChapterGroup {
        @JsonProperty("server_data")
        private List<ChapterItem> serverData;
    }

    @Data
    public static class ChapterItem {
        @JsonProperty("chapter_name")
        private String chapterName;
        @JsonProperty("chapter_title")
        private String chapterTitle;
        @JsonProperty("chapter_api_data")
        private String chapterApiData;
    }
}
