package com.example.wtoon.controller;

import com.example.wtoon.dto.response.StoryDetailDTO;
import com.example.wtoon.dto.response.StorySummaryDTO;
import com.example.wtoon.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;


@RestController
@RequestMapping("/api/v1/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @GetMapping
    public ResponseEntity<Page<StorySummaryDTO>> getStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String categoryId){
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<StorySummaryDTO> storiesPage;
        if (categoryId == null || categoryId.isEmpty()){
            storiesPage = storyService.getStories(pageable);
        } else {
            storiesPage = storyService.getStoriesByCategory(categoryId, pageable);
        }
        return ResponseEntity.ok(storiesPage);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<StoryDetailDTO> getStoryDetails(@PathVariable String slug) {
        StoryDetailDTO storyDto = storyService.getStoryBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Story not found with slug: " + slug
                ));

        return ResponseEntity.ok(storyDto);
    }
}