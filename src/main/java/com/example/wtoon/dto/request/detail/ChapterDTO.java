package com.example.wtoon.dto.request.detail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChapterDTO {
    @JsonProperty("chapter_name")
    private String chapterName;
    @JsonProperty("chapter_title")
    private String chapterTitle;
    @JsonProperty("chapter_api_data")
    private String chapterApiData;
}