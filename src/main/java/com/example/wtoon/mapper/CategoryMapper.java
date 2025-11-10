package com.example.wtoon.mapper;

import com.example.wtoon.dto.response.CategoryResponseDTO;
import com.example.wtoon.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    CategoryResponseDTO toCategoryDto(Category category);
    List<CategoryResponseDTO> toCategoryDtoList(List<Category> categories);
}
