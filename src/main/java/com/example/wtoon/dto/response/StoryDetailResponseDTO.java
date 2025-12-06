package com.example.wtoon.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryDetailResponseDTO {
    private String id;
    private String slug;
    private String name;
    private String status;
    private String thumbUrl;
    private String cdnDomain;
    private Set<CategoryResponseDTO> categories;
    private LocalDateTime updatedAt;
    private String description;
    private String author;
    private List<ChapterInfoResponseDTO> chapters;
    private Long viewCount;
}