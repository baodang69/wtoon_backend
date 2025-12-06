package com.example.wtoon.controller;

import com.example.wtoon.dto.response.StoryDetailResponseDTO;
import com.example.wtoon.dto.response.StorySummaryResponseDTO;
import com.example.wtoon.exception.ResourceNotFoundException;
import com.example.wtoon.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @GetMapping
    public ResponseEntity<Page<StorySummaryResponseDTO>> getStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String daysOfWeek) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").ascending());
        Page<StorySummaryResponseDTO> storiesPage;
        
        if (StringUtils.hasText(categoryId)) {
            storiesPage = storyService.getStoriesByCategory(categoryId, pageable);
        }
        else if (StringUtils.hasText(daysOfWeek)) {
            storiesPage = storyService.getStoriesByDaysOfWeek(daysOfWeek, pageable);
        } else {
            storiesPage = storyService.getStories(pageable);
        }
        
        return ResponseEntity.ok(storiesPage);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<StoryDetailResponseDTO> getStoryDetails(@PathVariable String slug) {
        StoryDetailResponseDTO storyDto = storyService.getStoryBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Story", "slug", slug));
        storyService.incrementViewCount(slug);
        return ResponseEntity.ok(storyDto);
    }
}
