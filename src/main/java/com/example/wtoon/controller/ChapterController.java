package com.example.wtoon.controller;

import com.example.wtoon.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    @GetMapping("/{id}/content")
    public ResponseEntity<List<String>> getChapterContent(@PathVariable String id) {
        List<String> imageUrls = chapterService.getChapterContent(id);
        return ResponseEntity.ok(imageUrls);
    }
}
