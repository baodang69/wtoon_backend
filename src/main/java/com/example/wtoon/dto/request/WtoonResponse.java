package com.example.wtoon.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class WtoonResponse {
    private String status;
    private ResponseData data;

    @Data
    public static class ResponseData {
        private List<StoryItem> items;
        private ResponseParams params;
        @JsonProperty("APP_DOMAIN_CDN_IMAGE")
        private String domainCdnImage;
    }

    @Data
    public static class ResponseParams {
        private Pagination pagination;
    }

    @Data
    public static class Pagination {
        private int totalItems;
        private int totalItemsPerPage;
        private int currentPage;
    }

    @Data
    public static class StoryItem {
        @JsonProperty("_id")
        private String id;
        private String name;
        private String slug;
        private String status;
        @JsonProperty("thumb_url")
        private String thumbUrl;
        private String updatedAt; // Dạng String, sẽ parse sau
        private List<CategoryDTO> category;
    }

    @Data
    public static class CategoryDTO {
        private String id;
        private String name;
        private String slug;
    }
}