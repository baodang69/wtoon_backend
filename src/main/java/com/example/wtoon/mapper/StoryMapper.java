package com.example.wtoon.mapper;

import com.example.wtoon.dto.response.ChapterInfoResponseDTO;
import com.example.wtoon.dto.response.StoryDetailResponseDTO;
import com.example.wtoon.dto.response.StorySummaryResponseDTO;
import com.example.wtoon.entity.Chapter;
import com.example.wtoon.entity.Story;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CategoryMapper.class})
public interface StoryMapper {

    StorySummaryResponseDTO toSummaryDto(Story story);
    
    List<StorySummaryResponseDTO> toSummaryDtoList(List<Story> stories);

    @Mapping(target = "chapters", source = "chapters", qualifiedByName = "mapAndSortChapters")
    StoryDetailResponseDTO toDetailDto(Story story);

    ChapterInfoResponseDTO toChapterInfoDto(Chapter chapter);

    @Named("mapAndSortChapters")
    default List<ChapterInfoResponseDTO> mapAndSortChapters(List<Chapter> chapters) {
        if (chapters == null) {
            return null;
        }
        return chapters.stream()
                .map(this::toChapterInfoDto)
                .sorted(Comparator.comparingDouble(c -> {
                    try {
                        return Double.parseDouble(c.getChapterName());
                    } catch (NumberFormatException e) {
                        return Double.MAX_VALUE;
                    }
                }))
                .collect(Collectors.toList());
    }
}