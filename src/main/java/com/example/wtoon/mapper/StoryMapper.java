package com.example.wtoon.mapper;

import com.example.wtoon.dto.response.ChapterInfoDTO;
import com.example.wtoon.dto.response.StoryDetailDTO;
import com.example.wtoon.dto.response.StorySummaryDTO;
import com.example.wtoon.entity.Chapter;
import com.example.wtoon.entity.Story;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StoryMapper {
    StorySummaryDTO toSummaryDto(Story story);
    List<StorySummaryDTO> toSummaryDtoList(List<Story> stories);

    @Mapping(target = "chapters", source = "chapters")
    StoryDetailDTO toDetailDto(Story story);

    ChapterInfoDTO toChapterInfoDto(Chapter chapter);
    List<ChapterInfoDTO> toChapterInfoDtoList(List<Chapter> chapters);

    @Named("mapAndSortChapters")
    default List<ChapterInfoDTO> mapAndSortChapters(Set<Chapter> chapters) {
        if (chapters == null) {
            return null;
        }

        return chapters.stream()
                .map(this::toChapterInfoDto) // Gọi hàm convert đơn lẻ ở trên
                .sorted(Comparator.comparingDouble(c -> {
                    try {
                        // Logic sắp xếp thông minh: 46.1, 10, 1...
                        return Double.parseDouble(c.getChapterName());
                    } catch (NumberFormatException e) {
                        // Nếu là chữ ("Extra"), đẩy xuống cuối
                        return Double.MAX_VALUE;
                    }
                }))
                .collect(Collectors.toList());
    }
}