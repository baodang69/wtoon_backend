package com.example.wtoon.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterInfoDTO {
    private String id;
    private String chapterName;
    private String chapterTitle;
    private LocalDateTime createdAt;
}