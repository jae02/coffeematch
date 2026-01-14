package com.coffeematch.backend.controller;

import com.coffeematch.backend.dto.CafeDetailDto;
import com.coffeematch.backend.dto.ReviewRequestDto;
import com.coffeematch.backend.entity.Cafe;
import com.coffeematch.backend.entity.Keyword;
import com.coffeematch.backend.service.CafeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cafes")
public class CafeController {

    private final CafeService cafeService;

    public CafeController(CafeService cafeService) {
        this.cafeService = cafeService;
    }

    @GetMapping
    public List<Cafe> getCafes(@RequestParam(required = false) String keyword) {
        return cafeService.getAllCafes(keyword);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CafeDetailDto> getCafe(@PathVariable Long id, Principal principal) {
        String email = (principal != null) ? principal.getName() : null;
        CafeDetailDto cafeDetail = cafeService.getCafeDetails(email, id);
        return ResponseEntity.ok(cafeDetail);
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> addReview(@PathVariable Long id, @RequestBody ReviewRequestDto reviewDto,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        cafeService.addReview(principal.getName(), id, reviewDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<?> voteKeyword(@PathVariable Long id, @RequestBody Map<String, Long> request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        Long keywordId = request.get("keywordId");
        try {
            cafeService.voteKeyword(principal.getName(), id, keywordId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/bookmark")
    public ResponseEntity<?> toggleBookmark(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        boolean isBookmarked = cafeService.toggleBookmark(principal.getName(), id);
        return ResponseEntity.ok(isBookmarked);
    }

    @GetMapping("/keywords")
    public List<Keyword> getKeywords() {
        return cafeService.getAllKeywords();
    }
}
