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
            @RequestParam(required = false) String categoryId) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<StorySummaryResponseDTO> storiesPage;
        
        if (categoryId == null || categoryId.isEmpty()) {
            storiesPage = storyService.getStories(pageable);
        } else {
            storiesPage = storyService.getStoriesByCategory(categoryId, pageable);
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
