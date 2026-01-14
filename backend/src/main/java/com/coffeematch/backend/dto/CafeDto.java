package com.coffeematch.backend.dto;

import com.coffeematch.backend.entity.Cafe;
import java.util.List;
import java.util.stream.Collectors;

public class CafeDto {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String description;
    private String imageUrl;
    private Double internalRatingAvg;
    private Integer reviewCount;
    private Integer bookmarkCount;
    private List<MenuDto> menus;

    public CafeDto(Cafe cafe) {
        this.id = cafe.getId();
        this.name = cafe.getName();
        this.address = cafe.getAddress();
        this.phone = cafe.getPhone();
        this.description = cafe.getDescription();
        this.imageUrl = cafe.getImageUrl();
        this.internalRatingAvg = cafe.getInternalRatingAvg();
        this.reviewCount = cafe.getReviewCount();
        this.bookmarkCount = cafe.getBookmarkCount();
        if (cafe.getMenus() != null) {
            this.menus = cafe.getMenus().stream().map(MenuDto::new).collect(Collectors.toList());
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Double getInternalRatingAvg() {
        return internalRatingAvg;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public Integer getBookmarkCount() {
        return bookmarkCount;
    }

    public List<MenuDto> getMenus() {
        return menus;
    }
}
