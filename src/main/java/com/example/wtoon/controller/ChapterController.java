package com.example.wtoon.controller;

import com.example.wtoon.entity.Chapter;
import com.example.wtoon.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/chapters")
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService chapterService;

    @GetMapping("/{id}/content")
    public ResponseEntity<?> getChapterContent(@PathVariable String id) {
        try {
            List<String> imageUrls = chapterService.getChapterContent(id);
            return ResponseEntity.ok(imageUrls);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage(),
                "chapterId", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", "An unexpected error occurred",
                "chapterId", id
            ));
        }
    }
}
