package com.example.wtoon.dto.request.detail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class StoryDetailItem {
    @JsonProperty("_id")
    private String id;
    private String name;
    private String content;
    private List<String> author;
    private List<ChapterGroup> chapters;
}