package com.example.wtoon.mapper;

import com.example.wtoon.dto.response.ChapterInfoDTO;
import com.example.wtoon.dto.response.StoryDetailDTO;
import com.example.wtoon.dto.response.StorySummaryDTO;
import com.example.wtoon.entity.Chapter;
import com.example.wtoon.entity.Story;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StoryMapper {
    StorySummaryDTO toSummaryDto(Story story);
    List<StorySummaryDTO> toSummaryDtoList(List<Story> stories);

    @Mapping(target = "chapters", source = "chapters")
    StoryDetailDTO toDetailDto(Story story);

    ChapterInfoDTO toChapterInfoDto(Chapter chapter);
    List<ChapterInfoDTO> toChapterInfoDtoList(List<Chapter> chapters);
}