package com.example.wtoon.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * DTO để parse response danh sách truyện từ API bên ngoài
 */
@Data
public class WtoonListResponse {
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
        private String updatedAt;
        private List<CategoryItem> category;
    }

    @Data
    public static class CategoryItem {
        private String id;
        private String name;
        private String slug;
    }
}
