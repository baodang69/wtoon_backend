package com.example.wtoon.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorySummaryResponseDTO {
    private String id;
    private String slug;
    private String name;
    private String status;
    private String thumbUrl;
    private String cdnDomain;
    private Set<CategoryResponseDTO> categories;
    private LocalDateTime updatedAt;
}