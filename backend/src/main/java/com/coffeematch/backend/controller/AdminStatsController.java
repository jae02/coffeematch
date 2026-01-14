package com.coffeematch.backend.controller;

import com.coffeematch.backend.dto.AdminStatsDto;
import com.coffeematch.backend.service.CafeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final CafeService cafeService;

    public AdminStatsController(CafeService cafeService) {
        this.cafeService = cafeService;
    }

    @GetMapping
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(cafeService.getAdminStats());
    }
}
