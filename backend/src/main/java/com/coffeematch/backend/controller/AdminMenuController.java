package com.coffeematch.backend.controller;

import com.coffeematch.backend.dto.MenuDto;
import com.coffeematch.backend.dto.MenuRequestDto;
import com.coffeematch.backend.service.CafeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminMenuController {

    private final CafeService cafeService;

    public AdminMenuController(CafeService cafeService) {
        this.cafeService = cafeService;
    }

    @PostMapping("/cafes/{cafeId}/menus")
    public ResponseEntity<MenuDto> addMenu(@PathVariable Long cafeId, @RequestBody MenuRequestDto request) {
        return ResponseEntity.ok(cafeService.addMenu(cafeId, request));
    }

    @PutMapping("/menus/{menuId}")
    public ResponseEntity<MenuDto> updateMenu(@PathVariable Long menuId, @RequestBody MenuRequestDto request) {
        return ResponseEntity.ok(cafeService.updateMenu(menuId, request));
    }

    @DeleteMapping("/menus/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long menuId) {
        cafeService.deleteMenu(menuId);
        return ResponseEntity.ok().build();
    }
}
