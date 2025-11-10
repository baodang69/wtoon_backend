package com.example.wtoon.service;

import com.example.wtoon.dto.response.StoryDetailDTO;
import com.example.wtoon.dto.response.StorySummaryDTO;
import com.example.wtoon.entity.Story;
import com.example.wtoon.mapper.StoryMapper;
import com.example.wtoon.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final StoryMapper storyMapper;

    public Page<StorySummaryDTO> getStories(Pageable pageable) {
        Page<Story> storiesPage = storyRepository.findAll(pageable);
        return storiesPage.map(storyMapper::toSummaryDto);
    }

    public Optional<StoryDetailDTO> getStoryBySlug(String slug) {
        Optional<Story> storyOpt = storyRepository.findBySlugWithDetails(slug);
        return storyOpt.map(storyMapper::toDetailDto);
    }

    public Page<StorySummaryDTO> getStoriesByCategory(String categoryId, Pageable pageable) {
        Page<Story> storiesPage = storyRepository.findAllByCategory(categoryId, pageable);
        return storiesPage.map(storyMapper::toSummaryDto);
    }
}