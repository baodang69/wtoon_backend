package com.example.wtoon.service;

import com.example.wtoon.dto.response.CategoryResponseDTO;
import com.example.wtoon.entity.Category;
import com.example.wtoon.mapper.CategoryMapper;
import com.example.wtoon.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponseDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toCategoryDtoList(categories);
    }
}
