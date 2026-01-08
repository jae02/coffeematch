package com.coffeematch.backend.controller;

import com.coffeematch.backend.dto.ReviewRequestDto;
import com.coffeematch.backend.entity.Cafe;
import com.coffeematch.backend.entity.Review;
import com.coffeematch.backend.repository.CafeRepository;
import com.coffeematch.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cafes")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class CafeController {

    private final CafeRepository cafeRepository;
    private final ReviewRepository reviewRepository;

    @GetMapping
    public List<Cafe> getCafes(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return cafeRepository.findByNameContainingIgnoreCase(keyword);
        }
        return cafeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cafe> getCafe(@PathVariable Long id) {
        return cafeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<Review> addReview(@PathVariable Long id, @RequestBody ReviewRequestDto reviewDto) {
        return cafeRepository.findById(id).map(cafe -> {
            Review review = new Review();
            review.setCafe(cafe);
            review.setAuthor(reviewDto.getAuthor());
            review.setRating(reviewDto.getRating());
            review.setContent(reviewDto.getContent());
            Review savedReview = reviewRepository.save(review);
            return ResponseEntity.ok(savedReview);
        }).orElse(ResponseEntity.notFound().build());
    }
}
