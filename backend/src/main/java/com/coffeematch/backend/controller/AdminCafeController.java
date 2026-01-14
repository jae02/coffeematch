package com.coffeematch.backend.controller;

import com.coffeematch.backend.dto.CafeDto;
import com.coffeematch.backend.dto.CafeRequestDto;
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
}
