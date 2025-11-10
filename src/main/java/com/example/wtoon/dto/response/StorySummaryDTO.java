package com.example.wtoon.dto.response;

import com.example.wtoon.entity.Category;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorySummaryDTO {
    private String id;
    private String slug;
    private String name;
    private String status;
    private String thumbUrl;
    private String cdnDomain;
    private Set<Category> categories;
    private LocalDateTime updatedAt;
}