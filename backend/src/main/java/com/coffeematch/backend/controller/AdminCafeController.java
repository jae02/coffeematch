package com.coffeematch.backend.controller;

import com.coffeematch.backend.dto.CafeDto;
import com.coffeematch.backend.dto.CafeRequestDto;
import com.coffeematch.backend.dto.CrawlCafeRequestDto;
import com.coffeematch.backend.service.CafeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cafes")
public class AdminCafeController {

    private final CafeService cafeService;

    public AdminCafeController(CafeService cafeService) {
        this.cafeService = cafeService;
    }

    @PostMapping
    public ResponseEntity<CafeDto> createCafe(@RequestBody CafeRequestDto request) {
        return ResponseEntity.ok(cafeService.createCafe(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CafeDto> updateCafe(@PathVariable Long id, @RequestBody CafeRequestDto request) {
        return ResponseEntity.ok(cafeService.updateCafe(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCafe(@PathVariable Long id) {
        cafeService.deleteCafe(id);
        return ResponseEntity.ok().build();
    }

    // Crawler-specific endpoints
    @PostMapping("/crawl")
    public ResponseEntity<CafeDto> createCafeFromCrawl(@RequestBody CrawlCafeRequestDto request) {
        return ResponseEntity.ok(cafeService.createCafeFromCrawl(request));
    }

    @PostMapping("/crawl/batch")
    public ResponseEntity<java.util.List<CafeDto>> createCafesFromCrawlBatch(
            @RequestBody java.util.List<CrawlCafeRequestDto> requests) {
        java.util.List<CafeDto> results = new java.util.ArrayList<>();
        for (CrawlCafeRequestDto request : requests) {
            try {
                results.add(cafeService.createCafeFromCrawl(request));
            } catch (Exception e) {
                // Log error but continue processing other cafes
                System.err.println("Failed to process cafe: " + request.getName() + " - " + e.getMessage());
            }
        }
        return ResponseEntity.ok(results);
    }
}
