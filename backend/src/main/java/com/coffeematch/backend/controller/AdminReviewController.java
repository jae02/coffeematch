package com.coffeematch.backend.controller;

import com.coffeematch.backend.entity.Review;
import com.coffeematch.backend.service.CafeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    private final CafeService cafeService;

    public AdminReviewController(CafeService cafeService) {
        this.cafeService = cafeService;
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(cafeService.getAllReviews());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        cafeService.deleteReview(id);
        return ResponseEntity.ok().build();
    }
}
