package com.example.wtoon.dto.request.detail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ChapterGroup {
    @JsonProperty("server_data")
    private List<ChapterDTO> serverData;
}